package jasp.app.db;

import jasp.app.App;
import jasp.core.model.User;

import javax.sql.DataSource;

/**
 * Factory that creates the app data source.
 */
public interface DataSourceProvider {

    /**
     * Creates the data source.
     *
     * @param user The data source user.
     * @param app The app object.
     */
    DataSource get(User user, App app) throws Exception;

    /**
     * Releases a previously obtained data source.
     * <p>
     * There should be no expectation by the application that this method will
     * actually release any resources associated with the data source. It is
     * simply a callback to the provider to signal that the application no
     * longer requires the data source. It is up the implementation to decide
     * how to manage the data source life cycle.
     * </p>
     */
    void release(DataSource dataSource);

    interface Factory {
        DataSourceProvider create(App app);
    }
}
