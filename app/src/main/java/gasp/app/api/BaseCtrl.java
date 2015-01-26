package gasp.app.api;

import com.google.common.io.Files;
import gasp.app.App;
import gasp.app.db.DataSourceProvider;
import gasp.core.catalog.Catalog;
import gasp.core.db.Task;
import gasp.core.util.Function;
import gasp.core.util.GaspIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Base class for api controllers.
 */
public class BaseCtrl {

    static final MediaType APPLICATION_GEOJSON = MediaType.valueOf("application/vnd.geo+json");
    static final String APPLICATION_GEOJSON_VALUE = APPLICATION_GEOJSON.toString();

    @Autowired
    protected App app;

    protected <T> T doWithCatalog(Function<Catalog,T> f) throws Exception {
        Catalog cat = app.catalog();
        try {
            return f.apply(cat);
        }
        finally {
            app.release(cat);
        }
    }

    protected <T> GaspIterator<T> streamWithCatalog(Function<Catalog,Iterator<T>> f) throws Exception {
        Catalog cat = app.catalog();
        try {
            return new GaspIterator<>(f.apply(cat)).then((v) -> app.release(cat));
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
                    callback.accept(this);
                    return result;
                }

                @Override
                public void close() {
                    t.close();
                    super.close();
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
    protected <T> T run(Function<Consumer<Task<T>>, Task<T>> f) throws Exception {
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

    /**
     * mapping of filename to extension
     * TODO: make this pluggable
     */
    static Map<String,MediaType> MAPPINGS = new LinkedHashMap<>();
    static {
        MAPPINGS.put("json", APPLICATION_JSON);
        MAPPINGS.put("geojson", APPLICATION_GEOJSON);
    }

    Optional<MediaType> responseFormat(HttpServletRequest req) {
        // look at accepts header
        Optional<String> header = Optional.ofNullable(req.getHeader(HttpHeaders.ACCEPT));
        Optional<MediaType> type = header.map(MediaType::valueOf).filter((t) -> !t.equals(MediaType.ALL));
        return Optional.ofNullable(type.orElseGet(() -> MAPPINGS.get(Files.getFileExtension(req.getRequestURI()))));
    }
}
