package gasp.core.db;

import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class Query implements AutoCloseable {

    QueryBuilder builder;
    PreparedStatement st;

    public static QueryBuilder build(String sql) {
        return new QueryBuilder(sql);
    }

    Query(PreparedStatement st, QueryBuilder builder) {
        this.st = st;
        this.builder = builder;
    }

    public Query page(Integer limit, Integer offset) throws SQLException {
        Preconditions.checkState(builder.paged, "Can't call page() on a non-paged query");

        st.setInt(builder.params.size()+1, limit != null ? limit : Integer.MAX_VALUE);
        st.setInt(builder.params.size()+2, offset != null ? offset : 0);
        return this;
    }

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

        return new QueryResult(st.executeQuery());
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
