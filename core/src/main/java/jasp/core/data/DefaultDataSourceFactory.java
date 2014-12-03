package jasp.core.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jasp.core.App;
import jasp.core.Config;

import jasp.core.security.Passwords;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.Optional;

import static jasp.core.Config.GROUP_DATABASE;

/**
 * Default configuration factory that creates connection to PostGIS
 * database from configuration.
 */
public class DefaultDataSourceFactory implements DataSourceFactory {

    static Logger LOG = LoggerFactory.getLogger(DefaultDataSourceFactory.class);

    @Override
    public DataSource create(App app) throws Exception {
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setDriverClassName("com.impossibl.postgres.jdbc.PGDriver");

        Config config = app.config();

        StringBuilder url = new StringBuilder("jdbc:pgsql://");
        url.append(config.get(GROUP_DATABASE, "host").orElse("localhost"));
        url.append(":").append(config.get(GROUP_DATABASE, "port").orElse("5432"));
        url.append("/").append(config.get(GROUP_DATABASE, "name").orElse("jasp"));
        dbConfig.setJdbcUrl(url.toString());

        dbConfig.setUsername(config.get(GROUP_DATABASE, "user").orElse(System.getProperty("user.name")));
        Optional<String> passwd = config.get(GROUP_DATABASE, "passwd");
        if (passwd.isPresent()) {
            dbConfig.setPassword(Passwords.decrypt(passwd.get(), config));
        }

        HikariDataSource db = new HikariDataSource(dbConfig);
        try(Connection cx = db.getConnection()) {
            LOG.info("Connected to: " + url.toString());
            return db;
        }
    }
}
