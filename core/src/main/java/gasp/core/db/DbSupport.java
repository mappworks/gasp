package gasp.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import static java.lang.String.format;

/**
 * Base class for working with jdbc objects.
 */
public abstract class DbSupport {

    static Logger LOG = LoggerFactory.getLogger(DbSupport.class);

    protected DataSource dataSource;

    protected DbSupport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Gets the data source.
     */
    public DataSource dataSource() {
        return dataSource;
    }

    /**
     * Opens a new connection.
     */
    Connection connection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to open connection", e);
        }
    }

    /**
     * Runs a task against a database connection.
     * <p>
     * Errors will be thrown back.
     * </p>
     *
     * @see {@link DbUtil#run(Task, java.sql.Connection)}
     */
    protected <T> T run(Task<T> t) throws SQLException {
        return DbUtil.run(t, t.open(connection()));
    }

    /**
     * Runs a script against the database.
     * <p>
     *  Scripts are looked up on the classpath with the <tt>scope</tt> and <tt>filename</tt> parameters.
     * </p>
     */
    protected void runScript(String filename, Class<?> scope, String delim, Map<String,String> vars)
        throws IOException, SQLException {
        try (
            Connection cx = connection();
        ) {
            runScript(filename, scope, delim, vars, cx);
        }
    }

    /**
     * Runs a script against the database with an existing connection.
     *
     * @see #runScript(String, Class, String, java.util.Map)
     */
    protected void runScript(String filename, Class<?> scope, String delim, Map<String,String> vars, Connection cx)
        throws IOException, SQLException {
        try (InputStream input = scope.getResourceAsStream(filename)) {
            if (input == null) {
                throw new IllegalArgumentException(format("No script %s relative to %s", filename, scope.getName()));
            }

            DbUtil.runScript(input, delim, vars, cx);
        }
    }

    /**
     * Runs a task against a database within a transaction.
     */
    protected <T> T runInTransaction(Task<T> t) throws SQLException {
        return DbUtil.runInTransaction(t, t.open(connection()));
    }

    /**
     * Runs a streaming task against a database connection.
     * <p>
     * A streaming task is one that returns a {@link java.sql.ResultSet} wrapped in
     * an iterator that maps rows to model objects.
     * </p>
     */
    protected <T> Iterator<T> stream(Task<ResultSet> t, Mapper<T> mapper) throws SQLException {
        return DbUtil.stream(t, mapper, t.open(connection()));
    }
}
