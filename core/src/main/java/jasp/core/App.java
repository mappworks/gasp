package jasp.core;

import jasp.core.security.Security;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Global state for the application.
 */
public class App {

    public static enum State {
        STARTING, ERROR, STARTED, STOPPING, STOPPED;
    }

    State state;
    Config config;

    Security security;
    DataSource dataSource;

    List<Exception> errors = new ArrayList<>();

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

    public List<Exception> errors() {
        return errors;
    }

    App error(Exception error) {
        this.errors.add(error);
        return this;
    }

    public Security security() {
        return security;
    }

    App security(Security security) {
        this.security = security;
        return this;
    }

    public DataSource dataSource() {
        return dataSource;
    }

    App dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }
}
