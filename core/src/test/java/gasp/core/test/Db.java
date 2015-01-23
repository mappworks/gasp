package gasp.core.test;

import gasp.core.Config;
import gasp.core.db.SQL;
import gasp.core.db.Task;
import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static gasp.core.db.DbUtil.dbBaseUrl;
import static gasp.core.db.DbUtil.run;

/**
 * A test rule that provides a connection to a live database.
 */
public class Db extends ExternalResource {

    /**
     * logger
     */
    static Logger LOG = LoggerFactory.getLogger(Db.class);

    /**
     * test schema name
     */
    static String SCHEMA = "gasp_test";

    /**
     * the underlying data source
     */
    DataSource ds;

    /**
     * Gets the underlying datasource.
     */
    public DataSource get() {
        return ds;
    }

    /**
     * Gets a new connection.
     */
    public Connection conn() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        try {
            ds = connect();
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

    DataSource connect() throws SQLException {
        Config cfg = new Config();
        String url = dbBaseUrl(cfg);

        String user = cfg.get(Config.GROUP_DATABASE, "user").orElse(System.getProperty("user.name"));
        String pass = cfg.get(Config.GROUP_DATABASE, "passwd").orElse(null);

        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(pass);

        // test a connection
        ds.getConnection().close();

        // override config to use test schema
        String schemaProp = Config.toSysProp(Config.GROUP_DATABASE, "schema");
        if (System.getProperty(schemaProp) == null) {
            System.setProperty(schemaProp, SCHEMA);
        }

        // wrap the data source in one that will initialize connections
        return new TestDataSource(ds);
    }

    @Override
    protected void after() {
        try {
            run(new Task<Void>() {
                @Override
                public Void run(Connection cx) throws Exception {
                    open(cx);
                    open(new SQL("DROP SCHEMA %s CASCADE", SCHEMA).compile(cx)).executeUpdate();
                    return null;
                }
            }, ds.getConnection());
        }
        catch(SQLException e) {
            LOG.warn("Error tearing down schema", e);
        }

        try {
            ds.unwrap(PGPoolingDataSource.class).close();
        } catch (SQLException e) {
            LOG.warn("Error closing underlying data source", e);
        }
    }

    static class TestDataSource extends DelegatingDataSource {
        public TestDataSource(DataSource delegate) {
            super(delegate);
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection cx = super.getConnection();
            // initialize the connection before we give it back ensure the test
            // schema exists, and set up the search path
            run(new Task<Void>() {
                @Override
                public Void run(Connection cx) throws Exception {
                    open(new SQL("CREATE SCHEMA IF NOT EXISTS %s", SCHEMA).compile(cx)).executeUpdate();
                    open(new SQL("SET search_path TO %s", SCHEMA).compile(cx)).executeUpdate();
                    return null;
                }
            }, cx);
            return cx;
        }
    }
}
