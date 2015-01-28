package gasp.core.db;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Utility class for building SQL queries.
 */
public class SQL {

    static Marker MARKER = MarkerFactory.getMarker("SQL");

    StringBuilder buf;
    List<Arg> params;

    /**
     * Creates a new empty sql buffer.
     */
    public SQL() {
        this("");
    }

    /**
     * Creates a new sql buffer with the specified formatted string.
     *
     * @param sql The pre-formatted string.
     * @param args Arguments to plug in to the format string.
     *
     * @see {@link String#format(String, Object...)}
     */
    public SQL(String sql, Object... args) {
        buf = new StringBuilder();
        params = new ArrayList<>();

        this.a(sql, args);
    }

    /**
     * Appends a formatted string to the sql buffer.
     *
     * @param sql The pre-formatted string.
     * @param args Arguments to plug in to the format string.
     *
     * @see String#format(String, Object...)
     */
    public SQL a(String sql, Object...args) {
        buf.append(format(sql, args));
        return this;
    }

    /**
     * Adds a parameter value to the sql statement.
     *
     * @see #p(Object,int)
     */
    public SQL p(Object val) {
        return p(val, Types.OTHER);
    }

    /**
     * Adds a String parameter value to the sql statement.
     *
     * @see #p(Object,int)
     */
    public SQL p(String val) {
        return p(val, Types.VARCHAR);
    }

    /**
     * Adds an Integer parameter value to the sql statement.
     *
     * @see #p(Object,int)
     */
    public SQL p(Integer val) {
        return p(val, Types.INTEGER);
    }

    /**
     * Adds a Double parameter value to the sql statement.
     *
     * @see #p(Object,int)
     */
    public SQL p(Double val) {
        return p(val, Types.REAL);
    }

    /**
     * Adds a parameter value to the sql statement.
     * <p>
     * Such values are plugged into the prepared statement during the {@link #compile(java.sql.Connection)}
     * method.
     * </p>
     */
    public SQL p(Object val, int type) {
        params.add(new Arg(val, type));
        return this;
    }

    /**
     * Trims n characters from the buffer.
     */
    public SQL trim(int n) {
        buf.setLength(buf.length()-1);
        return this;
    }

    /**
     * Logs the statement to the specified logger at the debug level.
     */
    public SQL log(Logger log) {
        if (log.isDebugEnabled()) {
            log.debug(MARKER, toLog());
        }
        return this;
    }

    /**
     * Logs the statement to the specified logger at the trace level.
     */
    public SQL trace(Logger log) {
        if (log.isTraceEnabled()) {
            log.trace(MARKER, toLog());
        }
        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }

    String toLog() {
        StringBuilder copy = new StringBuilder(buf);
        int i = -1;
        Iterator<Arg> a = params.iterator();
        while((i = copy.indexOf("?", i+1)) > 0 && a.hasNext()) {
            copy.replace(i, i+1, Optional.ofNullable(a.next().value).map((v) -> v.toString()).orElse("null"));
        }
        return copy.toString();
    }

    /**
     * Compiles the sql statement buffer into a prepared statement.
     *
     * @param cx The connection used to create the prepared statement.
     */
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

    /**
     * Returns the raw string buffer.
     */
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
