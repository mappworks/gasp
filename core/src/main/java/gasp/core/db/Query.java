package gasp.core.db;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * A query (select statement).
 * <p>
 *  Example usage:
 *  <pre>
 *   Connection conn = ...;
 *   Query q = Query.build("SELECT * FROM states WHERE name = ${name}").compile(conn);
 *   q.run("name", "California");
 *  </pre>
 * </p>
 * <p>
 * Queries can be optionally parametrized. A query parameter is a named value to be substituted
 * when the query is executed. Parameters are specified in the format "${&lt;name>}". For example
 * the query:
 * <pre>
 * SELECT * FROM country WHERE population > ${pop}
 * </pre>
 * Specifies a parameter named "pop". Parameter values must be specified in the {@link #run(java.util.Map)} method.
 * </p>
 */
public class Query implements AutoCloseable {

    static Logger LOG = LoggerFactory.getLogger(Query.class);

    QueryBuilder builder;
    PreparedStatement st;
    Map<String,Object> args = new HashMap<>();

    /**
     * Builds a new query.
     *
     * @param sql The query (optionally parametrized) sql.
     *
     * @return The builder for the new query.
     */
    public static QueryBuilder build(String sql) {
        return new QueryBuilder(sql);
    }

    Query(PreparedStatement st, QueryBuilder builder) {
        this.st = st;
        this.builder = builder;
    }

    /**
     * Sets the bounds filter for the query.
     * <p>
     *  This method should only be called if {@link QueryBuilder#bound()} has been called.
     * </p>
     * @param bbox The bounding box.
     */
    public Query bounds(Envelope bbox) {
        Preconditions.checkArgument(!bbox.isNull() && bbox.getWidth()>0 && bbox.getHeight()>0,
            "bounding box must be non-empty");

        args.put("!bbox.minx", bbox.getMinX());
        args.put("!bbox.miny", bbox.getMinY());
        args.put("!bbox.maxx", bbox.getMaxX());
        args.put("!bbox.maxy", bbox.getMaxY());
        return this;
    }

    /**
     * Sets the simplification tolerance for the query.
     * <p>
     *  This method should only be called if {@link QueryBuilder#simplify()} ()} has been called.
     * </p>
     * @param tol Simplification tolerance.
     */
    public Query tolerance(double tol) {
        args.put("!tol", tol);
        return this;
    }

    /**
     * Sets the simplification tolerance for the query from a bounding box and dimensions.
     * <p>
     *  This method should only be called if {@link QueryBuilder#simplify()} ()} has been called.
     * </p>
     */
    public Query tolerance(Envelope bbox, int width, int height) {
        Preconditions.checkArgument(width > 0 && height > 0, "width and height must be > 0");

        return tolerance(Math.max(bbox.getWidth() / ((double) width), bbox.getHeight() / ((double)height)));
    }

    /**
     * Sets the limit/offset of the query.
     * <p>
     * This method should be called before {@link #run(java.util.Map)}. Example:
     * <pre>
     * Query q = Query.build("SELECT * FROM foo").compile(conn);
     * q.page(100,10).run();
     * </pre>
     * </p>
     *
     * @param limit The limit (number of records to return), may be <tt>null</tt> meaning unbounded.
     * @param offset The offset (number of records to skip), may be <tt>null</tt> meaning offset zero.
     */
    public Query page(Integer limit, Integer offset) throws SQLException {
        st.setInt(builder.params.size()+1, limit != null && limit > -1 ? limit : Integer.MAX_VALUE);
        st.setInt(builder.params.size()+2, offset != null && limit > -1 ? offset : 0);
        return this;
    }

    /**
     * Runs the query with no arguments.
     */
    public QueryResult run() throws SQLException {
        return run(Collections.emptyMap());
    }

    /**
     * Runs the query specifying query arguments as alternating parameter name/value pairs.
     *
     * @param param The name of the first argument.
     * @param value The value of the first argument.
     * @param args Additional name/value pairs.
     */
    public QueryResult run(String param, Object value, Object... args) throws SQLException {
        Preconditions.checkArgument(args.length % 2 == 0,
            "run() requires even number of arguments name/value pairs");

        Map<String,Object> map = new HashMap<>();
        map.put(param, value);
        for (int i = 0; i < args.length; i+=2) {
            map.put(args[i].toString(), args[i+1]);
        }

        return run(map);
    }

    /**
     * Runs the query specifying query arguments as a map.
     *
     * @param args The parameter values to substitute into the query.
     */
    public QueryResult run(Map<String,Object> args) throws SQLException {
        if (args != null) {
            this.args.putAll(args);
        }

        return doRun(this.args);
    }

    QueryResult doRun(Map<String,Object>args) throws SQLException {
        List<String> params = builder.params;

        if (!params.isEmpty() && (args == null || args.isEmpty())) {
            throw new IllegalArgumentException("Query has parameters, but no values specified");
        }

        for (int i = 0; i < params.size(); i++) {
            String p = params.get(i);
            if (!args.containsKey(p)) {
                throw new IllegalArgumentException(format("Query specifies parameter '%s', but no value specified", p));
            }

            Object val = args.get(p);
            st.setObject(i+1, val);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(SQL.MARKER, DbUtil.log(builder.sql.buffer(), (i) -> {
                Optional<String> arg = Optional.ofNullable(i < params.size() ? params.get(i) : null);
                return arg.map((a) -> args.get(a)).orElse(null);
            }));
        }

        return new QueryResult(st.executeQuery(), this);
    }

    @Override
    public void close() throws Exception {
        if (st != null) {
            st.close();
            st = null;
        }
    }

    public static class QueryBuilder {
        static Pattern PARAM_REGEX = Pattern.compile("\\$\\{(!?[\\w|\\.]+)\\}");
        static Pattern TRAILING_SEMI = Pattern.compile(";$");

        boolean bounded = false;
        boolean simplified = false;
        boolean raw = false;

        String q;
        SQL sql;
        List<String> params = new ArrayList<>();

        public QueryBuilder(String q) {
            this.q = clean(q);
        }

        public QueryBuilder raw() {
            raw = true;
            return this;
        }

        public QueryBuilder bound() {
            bounded = true;
            return this;
        }

        public QueryBuilder simplify() {
            simplified = true;
            return this;
        }

        public Query compile(Connection cx) throws SQLException {
            // first we "sniff" the query in order to get metadata about it
            try (PreparedStatement st =
                 cx.prepareStatement(format("SELECT * FROM (%s) AS _ LIMIT 0", replaceParams(q)))) {

                // fill in null for all parameters
                for (int i = 0; i < st.getParameterMetaData().getParameterCount(); i++) {
                    st.setObject(i+1, null);
                }

                // execute the query and start rewriting
                sql = new SQL("SELECT ");
                SQL where = new SQL("TRUE");
                try (ResultSet rs = st.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    for (int j = 0; j < meta.getColumnCount(); j++) {
                        String name = meta.getColumnName(j+1);
                        String type = meta.getColumnTypeName(j+1);

                        if ("geometry".equals(type)) {
                            if (simplified) {
                                sql.a("st_simplify(%s, ${!tol})", name);
                            }
                            else {
                                sql.a(name);
                            }

                            if (bounded) {
                                where.a(" AND %s && ST_MakeEnvelope(" +
                                    "${!bbox.minx},${!bbox.miny},${!bbox.maxx},${!bbox.maxy})", name);
                            }
                        }
                        else {
                            sql.a(name);
                        }
                        sql.a(",");
                    }
                    sql.trim(1);
                }
                sql.a(" FROM (%s) AS _ WHERE %s LIMIT ? OFFSET ?", q, where.toString());

                sql.replace(parseParams(sql.toString()));

                Query q = new Query(cx.prepareStatement(sql.toString()), this);
                q.page(null, null);
                return q;
            }
        }

        String clean(String q) {
            // trim and strip off semicolon
            Matcher m = TRAILING_SEMI.matcher(q.trim());
            return m.replaceAll("");
        }

        String parseParams(String sql) {
            params.clear();

            // find all parameters placeholders
            Matcher m = PARAM_REGEX.matcher(sql);
            while (m.find()) {
                String p = m.group(1);
                params.add(p);
            }

            return m.replaceAll("?");
        }

        String replaceParams(String sql) {
            return PARAM_REGEX.matcher(sql).replaceAll("?");
        }
    }
}
