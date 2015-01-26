package gasp.core.db;

import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    QueryBuilder builder;
    PreparedStatement st;

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
     * Sets the limit/offset of the query.
     * <p>
     * The query must be created as paged by calling {@link gasp.core.db.Query.QueryBuilder#paged()}.
     * </p>
     * <p>
     * This method should be called before {@link #run(java.util.Map)}. Example:
     * <pre>
     * Query q = Query.build("SELECT * FROM foo").paged().compile(conn);
     * q.page(100,10).run();
     * </pre>
     * </p>
     *
     * @param limit The limit (number of records to return), may be <tt>null</tt> meaning unbounded.
     * @param offset The offset (number of records to skip), may be <tt>null</tt> meaning offset zero.
     */
    public Query page(Integer limit, Integer offset) throws SQLException {
        Preconditions.checkState(builder.paged, "Can't call page() on a non-paged query");

        st.setInt(builder.params.size()+1, limit != null ? limit : Integer.MAX_VALUE);
        st.setInt(builder.params.size()+2, offset != null ? offset : 0);
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
        static Pattern PARAM_REGEX = Pattern.compile("\\$\\{(\\w+)\\}");

        boolean paged = false;
        boolean raw = false;
        SQL sql;
        List<String> params = new ArrayList<>();

        public QueryBuilder(String q) {
            sql = new SQL(preCompile(q));
        }

        public QueryBuilder paged() {
            sql.a(" LIMIT ? OFFSET ?");
            paged = true;
            return this;
        }

        public QueryBuilder raw() {
            raw = true;
            return this;
        }

        public Query compile(Connection cx) throws SQLException {
            Query q = new Query(cx.prepareStatement(sql.toString()), this);
            if (paged) {
                // default paging defaults
                q.page(null, null);
            }
            return q;
        }

        String preCompile(String sql) {
            // find all parameters placeholders
            Matcher m = PARAM_REGEX.matcher(sql);
            while (m.find()) {
                String p = m.group(1);
                params.add(p);
            }

            return m.replaceAll("?");
        }
    }
}
