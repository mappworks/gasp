package gasp.core.db;

import java.sql.Connection;
import java.util.ArrayDeque;

import static gasp.core.db.DbUtil.closeSafe;

public abstract class Task<T> implements AutoCloseable {

    ArrayDeque<Object> open = new ArrayDeque<>();

    public abstract T run(Connection cx) throws Exception;

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
