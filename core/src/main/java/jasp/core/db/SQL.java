package jasp.core.db;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Utility class for building SQL queries.
 */
public class SQL {

    StringBuilder buf;
    List<Arg> params;

    public SQL() {
        this("");
    }

    public SQL(String sql, Object... args) {
        buf = new StringBuilder();
        params = new ArrayList<>();

        this.a(sql, args);
    }

    public SQL a(String sql, Object...args) {
        buf.append(format(sql, args));
        return this;
    }

    public SQL a(DbQuery q) {
        if (q == null) {
            return this;
        }
        if (!q.orderBy().isEmpty()) {
            a(" ORDER BY ");
            a(q.orderBy().stream().map((s) -> s.col + " " + (s.asc ? "ASC" : "DESC"))
                    .collect(Collectors.joining(",")));
        }
        if (q.offset() != null) {
            a(format(" OFFSET %d", q.offset()));
        }
        if (q.limit() != null) {
            a(format(" LIMIT %d", q.limit()));
        }
        return this;
    }

    public SQL p(Object val) {
        return p(val, Types.OTHER);
    }

    public SQL p(String val) {
        return p(val, Types.VARCHAR);
    }

    public SQL p(Integer val) {
        return p(val, Types.INTEGER);
    }

    public SQL p(Double val) {
        return p(val, Types.REAL);
    }

    public SQL p(Object val, int type) {
        params.add(new Arg(val, type));
        return this;
    }

    public SQL log(Logger log) {
        if (log.isDebugEnabled()) {
            StringBuilder copy = new StringBuilder(buf);
            int i = -1;
            Iterator<Arg> a = params.iterator();
            while((i = copy.indexOf("?", i+1)) > 0 && a.hasNext()) {
                copy.insert(i, a.next().value);
            }

            log.debug(copy.toString());
        }
        return this;
    }

    public PreparedStatement compile(Connection cx) throws SQLException {
        PreparedStatement ps = cx.prepareStatement(buf.toString());
        for (int i = 0; i < params.size(); i++) {
            Arg a = params.get(i);
            if (a.value != null) {
                ps.setObject(i+1, a.value, a.type);
            }
            else {
                ps.setNull(i+1, a.type);
            }
        }
        return ps;
    }

    public StringBuilder buffer() {
        return buf;
    }

    static class Arg {
        final Object value;
        final int type;

        Arg(Object value, int type) {
            this.value = value;
            this.type = type;
        }
    }
}
