package gasp.core.db;

import java.sql.Connection;
import java.util.ArrayDeque;

import static gasp.core.db.DbUtil.closeSafe;

/**
 * A task to be run against a database connection.
 * <p>
 * Tasks can use the {@link #open(Object)} method to track resources to be
 * closed when the task completes.
 * </p>
 */
public abstract class Task<T> implements AutoCloseable {

    ArrayDeque<Object> open = new ArrayDeque<>();

    public abstract T run(Connection cx) throws Exception;

    /**
     * Tracks a resource to be closed.
     *
     * @param obj The object to be closed.
     * @param <X> The type of object.
     *
     * @return The opened object.
     */
    public <X> X open(X obj) {
        open.add(obj);
        return obj;
    }

    @Override
    public void close() {
        if (open == null) {
            // already closed
            return;
        }

        while(!open.isEmpty()) {
            closeSafe(open.pop());
        }
        open = null;
    }
}
