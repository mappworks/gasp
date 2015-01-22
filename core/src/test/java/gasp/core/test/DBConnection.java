package gasp.core.test;

import gasp.core.Config;
import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static gasp.core.db.DbUtil.dbBaseUrl;

public class DBConnection extends ExternalResource {

    Connection cx;

    public Connection get() {
        return cx;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        try {
            cx = connect();
            return super.apply(base, description);
        }
        catch(SQLException e) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    Assume.assumeTrue("Unable to connect: " + e.getMessage(), false);
                }
            };
        }
    }

    Connection connect() throws SQLException {
        Config cfg = new Config();
        String url = dbBaseUrl(cfg);

        String user = cfg.get(Config.GROUP_DATABASE, "user").orElse(System.getProperty("user.name"));
        String pass = cfg.get(Config.GROUP_DATABASE, "passwd").orElse(null);

        return DriverManager.getConnection(url, user, pass);
    }

    @Override
    protected void after() {
        try {
            cx.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
