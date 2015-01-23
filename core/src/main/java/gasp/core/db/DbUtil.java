package gasp.core.db;

import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import gasp.core.Config;
import gasp.core.util.GaspIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

import static gasp.core.Config.GROUP_DATABASE;

/**
 * Utility functions for working with jdbc objects.
 */
public class DbUtil {

    static Logger LOG = LoggerFactory.getLogger(DbUtil.class);

    /**
     * Closes a database object catching, and logging any exceptions.
     *
     * @param obj One of {@link java.sql.Connection}, {@link java.sql.Statement}, or {@link java.sql.ResultSet}.
     */
    public static void closeSafe(Object obj) {
        try {
            if (obj instanceof ResultSet) {
                ((ResultSet) obj).close();
            } else if (obj instanceof Statement) {
                ((Statement) obj).close();
            } else if (obj instanceof Connection) {
                ((Connection) obj).close();
            }
        }
        catch(Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error closing object: " + obj, e);
            }
        }
    }

    /**
     * Helper to put together jdbc connection url from app configuration.
     */
    public static String dbBaseUrl(Config cfg) {
        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(cfg.get(GROUP_DATABASE, "host").orElse("localhost"));
        url.append(":").append(cfg.get(GROUP_DATABASE, "port").orElse("5432"));
        url.append("/").append(cfg.get(GROUP_DATABASE, "name").orElse("gasp"));
        return url.toString();
    }

    /**
     * Runs a script against a connection.
     * <p>
     * Scripts may contain variables, defined as "%var%".
     * </p>
     *
     * @param script The script to run.
     * @param delim The statement delimiter, defaults to ';'.
     * @param vars Variables to substitute while reading the script.
     * @param cx Database connection.
     */
    public static void runScript(InputStream script, String delim, Map<String,String> vars, Connection cx)
        throws IOException, SQLException {

        String end = delim != null ? delim : ";";

        runInTransaction(new Task<Void>() {
            @Override
            public Void run(Connection cx) throws Exception {
                Statement st = open(cx.createStatement());
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(script));

                    String line = null;
                    StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        //line = line.trim();
                        if (line.startsWith("--")) {
                            // comment, ignore
                            continue;
                        }

                        if (vars != null) {
                            for (Map.Entry<String, String> e : vars.entrySet()) {
                                line = line.replace("%" + e.getKey() + "%", e.getValue());
                            }
                        }

                        builder.append(line);
                        if (line.endsWith(end)) {
                            String stmt = builder.toString();
                            LOG.debug(stmt);

                            //st.addBatch(stmt);
                            st.execute(stmt);
                            builder.setLength(0);
                        } else {
                            // add a space
                            builder.append(" ");
                        }
                    }

                    //st.executeBatch();
                    return null;
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        }, cx);
    }

    /**
     * Runs a task against a database connection.
     */
    public static <T> T run(Task<T> t, Connection cx) throws SQLException {
        try (Task<T> q = t) {
            return q.run(cx);
        }
        catch(Exception e) {
            Throwables.propagateIfInstanceOf(e, SQLException.class);
            Throwables.propagate(e);
            return null;
        }
    }

    /**
     * Runs a task against a database within a transaction.
     */
    public static <T> T runInTransaction(Task<T> t, Connection cx) throws SQLException {
        try (Task<T> q = t) {
            cx.setAutoCommit(false);
            try {
                T result = q.run(cx);
                cx.commit();
                return result;
            } catch (Exception e) {
                cx.rollback();
                Throwables.propagateIfInstanceOf(e, SQLException.class);
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * Runs a streaming task against a database connection.
     * <p>
     * A streaming task is one that returns a {@link java.sql.ResultSet} wrapped in
     * an iterator that maps rows to model objects.
     * </p>
     */
    public static <T> Iterator<T> stream(Task<ResultSet> t, Mapper<T> mapper, Connection cx)
        throws SQLException {
        final ResultSet rs;
        try {
            rs = t.run(cx);
            return new GaspIterator<T>(new AbstractIterator<T>() {
                @Override
                protected T computeNext() {
                    try {
                        if (rs.next()) {
                            return mapper.map(rs);
                        }
                        t.close();
                        return endOfData();
                    } catch (Exception e) {
                        t.close();
                        throw Throwables.propagate(e);
                    }
                }
            });
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, SQLException.class);
            throw Throwables.propagate(e);
        }
    }
}
