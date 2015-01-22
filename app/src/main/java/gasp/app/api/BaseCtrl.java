package gasp.app.api;

import gasp.app.App;
import gasp.app.db.DataSourceProvider;
import gasp.core.catalog.Catalog;
import gasp.core.db.Task;
import gasp.core.util.GaspIterator;
import gasp.core.util.UnsafeFunction;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Base class for api controllers.
 */
public class BaseCtrl {

    @Autowired
    protected App app;

    protected <T> T doWithCatalog(UnsafeFunction<Catalog,T> f) throws Exception {
        Catalog cat = app.catalog();
        try {
            return f.apply(cat);
        }
        finally {
            app.release(cat);
        }
    }

    protected <T> GaspIterator<T> streamWithCatalog(UnsafeFunction<Catalog,Iterator<T>> f) throws Exception {
        Catalog cat = app.catalog();
        try {
            return new GaspIterator<>(f.apply(cat)).onFinish((v) -> app.release(cat));
        }
        catch(Exception e) {
            app.release(cat);
            throw e;
        }
    }

    /**
     * Runs the specified database task.
     *
     * @param t The task to run.
     * @param <T> Return type fo the task.
     *
     * @return The result of the task.
     */
    protected <T> T run(Task<T> t) throws Exception {
        return run((callback) -> {
            Task<T> q = new Task<T>() {
                @Override
                public T run(Connection cx) throws Exception {
                    T result = t.run(cx);
                    callback.accept(t);
                    return result;
                }
            };
            return q;
        });
    }

    /**
     * Runs the specified database task with an explicit callback to signal completion of the task.
     * <p>
     * This version of run is suitable for tasks that need to return an object that require the database
     * connection to remain open.
     * </p>
     *
     * @param f Function taking a callback and producing a task.
     * @param <T> Return type fo the task.
     *
     * @return The result of the task.
     */
    protected <T> T run(UnsafeFunction<Consumer<Task<T>>, Task<T>> f) throws Exception {
        DataSourceProvider dsp = app.dataSourceProvider();
        DataSource ds = dsp.get(app.user().get(), app);

        Consumer<Task<T>> callback = (task) -> {
            task.close();
            dsp.release(ds);
        };

        Task<T> t = f.apply(callback);

        Connection cx = t.open(ds.getConnection());
        try {
            return t.run(cx);
        }
        catch(Exception e) {
            callback.accept(t);
            throw e;
        }
    }

    protected Integer toInt(String val) {
        return Integer.parseInt(val);
    }
}
