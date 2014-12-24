package gasp.core.db;

import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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
     */
    protected <T> T run(Task<T> t) {
        return run(t, (e) -> {
            throw Throwables.propagate(e);
        });
    }

    /**
     * Runs a task against a database connection with an explicit error handler.
     */
    protected <T> T run(Task<T> t, Function<Exception,T> err) {
        try (
            State s = new State();
        ) {
            return t.run(s.open(connection()), s);
        }
        catch(Exception e) {
            return err.apply(e);
        }
    }

    /**
     * Runs a task against a database within a transaction.
     */
    protected <T> T runInTx(Task<T> t) {
        return runInTx(t, (e) -> {
            throw Throwables.propagate(e);
        });
    }

    /**
     * Runs a task against a database within a transaction with an explicit error handler.
     */
    protected <T> T runInTx(Task<T> t, Function<Exception,T> err) {
        try (State s = new State()) {
            Connection cx = s.open(connection());
            cx.setAutoCommit(false);
            try {
                T result = t.run(s.open(connection()), s);
                cx.commit();
                return result;
            }
            catch(Exception e) {
                cx.rollback();
                return err.apply(e);
            }
        }
        catch(SQLException e) {
            return err.apply(e);
        }
    }

    /**
     * Runs a streaming task against a database connection.
     * <p>
     * A streaming task is one that returns a {@link java.sql.ResultSet} wrapped in
     * an iterator that maps rows to model objects.
     * </p>
     */
    protected <T> Result<T> stream(Task<ResultSet> t, Mapper<T> mapper) {
        return stream(t, mapper, (e) -> {
            throw Throwables.propagate(e);
        });
    }

    /**
     * Runs a streaming task against a database connection with an explicit error handler.
     * <p>
     * A streaming task is one that returns a {@link java.sql.ResultSet} wrapped in
     * an iterator that maps rows to model objects.
     * </p>
     */
    protected <T> Result<T> stream(Task<ResultSet> t, Mapper<T> mapper, Function<Exception,Iterable<T>> err) {
        State s = new State();
        final ResultSet rs;
        try {
            rs = t.run(s.open(connection()), s);
            final Result r = new Result();
            return r.set(() -> new AbstractIterator<T>() {
                @Override
                protected T computeNext() {
                    try {
                        if (rs.next()) {
                            return mapper.map(rs);
                        }
                        s.close();
                        r.finish().ifPresent((c) -> {
                            try {
                                ((Consumer)c).accept(null);
                            }
                            catch(Throwable t) {
                                LOG.warn("Result finish callback threw exception", t);
                            }
                        });
                        return endOfData();
                    } catch (Exception e) {
                        s.close();
                        throw Throwables.propagate(e);
                    }
                }
            });
        } catch (Exception e) {
            s.close();
            err.apply(e);
        }
        return null;
    }

    /**
     * Tracks open objects in order to ensure they are closed after a task is run.
     */
    public class State implements AutoCloseable {

        ArrayDeque<Object> toclose = new ArrayDeque<>();

        public <T> T open(T obj) {
            toclose.push(obj);
            return obj;
        }

        @Override
        public void close() {
            while(!toclose.isEmpty()) {
                closeSafe(toclose.pop());
            }
        }

        void closeSafe(Object obj) {
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
    }

    /**
     * Task to be run against a database.
     */
    public interface Task<T> {
        /**
         * Runs the task.
         *
         * @param cx The database connection.
         * @param s State object used to track open objects such as statements and result sets.
         *
         * @return Result of the task.
         */
        T run(Connection cx, State s) throws Exception;
    }

    /**
     * Runs a script against a connection.
     * <p>
     * Scripts are assumed to be on the classpath, relative to the <tt>scope</tt> argument.
     * </p>
     * <p>
     * Scripts may contain variables, defined as "%var%".
     * </p>
     *
     * @param filename The name of the script to run.
     * @param scope The class from which to load the script.
     * @param vars Variables to substitute while reading the script.
     */
    protected void runScript(String filename, Class<?> scope, Map<String,String> vars)
        throws IOException {

        run((cx, s) -> {
            Statement st = s.open(cx.createStatement());
            try (InputStream input = scope.getResourceAsStream(filename)) {
                if (input == null) {
                    throw new IllegalArgumentException(format("No script %s relative to %s", filename, scope.getName()));
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String line = null;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    //line = line.trim();
                    if (line.startsWith("--")) {
                        // comment, ignore
                        continue;
                    }

                    for (Map.Entry<String, String> e : vars.entrySet()) {
                        line = line.replace("%" + e.getKey() + "%", e.getValue());
                    }

                    builder.append(line);
                    if (line.endsWith(";;")) {
                        String stmt = builder.toString();
                        LOG.debug(stmt);

                        //st.addBatch(stmt);
                        st.execute(stmt);
                        builder.setLength(0);
                    }
                    else {
                        // add a space
                        builder.append(" ");
                    }
                }

                //st.executeBatch();
                return null;
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        });
    }
}
