package jasp.core.data;

import jasp.core.App;

import javax.sql.DataSource;

public interface DataSourceFactory {

    DataSource create(App app) throws Exception;
}
