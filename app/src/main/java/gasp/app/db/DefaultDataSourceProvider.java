package gasp.app.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gasp.app.App;
import gasp.core.Config;
import gasp.core.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static gasp.core.Config.GROUP_DATABASE;

/**
 * Default configuration factory that creates connection to PostGIS
 * database from app configuration.
 */
public class DefaultDataSourceProvider implements DataSourceProvider {

    static Logger LOG = LoggerFactory.getLogger(DefaultDataSourceProvider.class);

    @Autowired
    App app;

    String dbUrl;

    @PostConstruct
    public void loadConfig() throws Exception {
        dbUrl = dbBaseUrl(app.config());
    }

    @Override
    public DataSource get(User user, App app) throws Exception {
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setDriverClassName("com.impossibl.postgres.jdbc.PGDriver");
        dbConfig.setJdbcUrl(dbUrl);

        dbConfig.setUsername(user.handle());
        dbConfig.setPassword(new String(user.password()));

        return new HikariDataSource(dbConfig);
    }

    @Override
    public void release(DataSource dataSource) {
        ((HikariDataSource)dataSource).shutdown();
    }

    /**
     * Helper to put together jdbc connection url from app configuration.
     */
    public static String dbBaseUrl(Config config) {
        StringBuilder url = new StringBuilder("jdbc:pgsql://");
        url.append(config.get(GROUP_DATABASE, "host").orElse("localhost"));
        url.append(":").append(config.get(GROUP_DATABASE, "port").orElse("5432"));
        url.append("/").append(config.get(GROUP_DATABASE, "name").orElse("gasp"));
        return url.toString();
    }
}
