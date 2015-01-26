package gasp.app.db;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gasp.app.App;
import gasp.core.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static gasp.core.db.DbUtil.dbBaseUrl;

/**
 * Default configuration factory that creates connection to PostGIS
 * database from app configuration.
 */
public class DefaultDataSourceProvider implements DataSourceProvider {

    static Logger LOG = LoggerFactory.getLogger(DefaultDataSourceProvider.class);

    @Autowired
    App app;

    @Autowired
    MetricRegistry metrics;

    String dbUrl;

    @PostConstruct
    public void loadConfig() throws Exception {
        dbUrl = dbBaseUrl(app.config());
    }

    @Override
    public DataSource get(User user, App app) throws Exception {
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setPoolName("DefaultConnectionPool");

        //dbConfig.setDriverClassName("com.impossibl.postgres.jdbc.PGDriver");
        dbConfig.setDriverClassName("org.postgresql.Driver");
        dbConfig.setJdbcUrl(dbUrl);

        dbConfig.setUsername(user.handle());
        dbConfig.setPassword(new String(user.password()));

        // always reset search_path before giving back connection
        dbConfig.setConnectionInitSql("SET search_path TO default");

        // set metrics
        dbConfig.setMetricRegistry(metrics);
        return new HikariDataSource(dbConfig);
    }

    @Override
    public void release(DataSource dataSource) {
        ((HikariDataSource)dataSource).shutdown();
    }


}
