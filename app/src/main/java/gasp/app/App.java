package gasp.app;

import com.google.common.base.Throwables;
import gasp.app.db.DataSourceProvider;
import gasp.app.security.AppUser;
import gasp.core.Config;
import gasp.core.catalog.Catalog;
import gasp.core.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Global facade for the application.
 */
public class App {

    /**
     * logger
     */
    public static Logger LOG = LoggerFactory.getLogger(App.class);

    /**
     * enumeration for app state
     */
    public static enum State {
        STARTING, ERROR, STARTED, STOPPING, STOPPED
    }

    /**
     * state of the app
     */
    State state;

    /**
     * core app configuration
     */
    Config config;

    /**
     * provides the app with a data source.
     */
    DataSourceProvider dataSourceProvider;

    /**
     * errors reported on startup
     */
    List<Exception> errors = new ArrayList<>();

    /**
     * spring application context
     */
    ApplicationContext context;

    public State state() {
        return state;
    }

    App state(State state) {
        this.state = state;
        return this;
    }

    public Config config() {
        return config;
    }

    App config(Config config) {
        this.config = config;
        return this;
    }

    public ApplicationContext context() {
        return context;
    }

    App context(ApplicationContext context) {
        this.context = context;
        return this;
    }

    public ServletContext servletContext() {
        if (context instanceof WebApplicationContext) {
            return ((WebApplicationContext) context).getServletContext();
        }
        throw new IllegalStateException("App context is not a webapp context");
    }

    public List<Exception> errors() {
        return errors;
    }

    App error(Exception error) {
        this.errors.add(error);
        return this;
    }

    public App dataSourceProvider(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
        return this;
    }

    public DataSourceProvider dataSourceProvider() {
        return dataSourceProvider;
    }

    public Optional<User> user() {
        return Optional.ofNullable(AppUser.get().map((u) -> u.user()).orElse(null));
    }

    public Catalog catalog() {
        Optional<User> user = user();
        return user.map((u) -> {
            DataSourceProvider dsp = dataSourceProvider();
            DataSource dataSource = null;
            try {
                dataSource = dsp.get(u, this);
                Catalog cat = new Catalog(dataSource, config());
                cat.init();
                return cat.on(Catalog.Event.DISPOSE, (s, c) -> dsp.release(cat.dataSource()));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }).orElseThrow(() -> new IllegalStateException("No authenticated user, unable to obtain catalog"));
    }

    public App release(Catalog catalog) {
        dataSourceProvider().release(catalog.dataSource());
        return this;
    }
}
