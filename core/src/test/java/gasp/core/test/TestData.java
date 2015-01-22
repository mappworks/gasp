package gasp.core.test;

import gasp.core.db.SQL;
import gasp.core.db.Task;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.GZIPInputStream;

import static gasp.core.db.DbUtil.*;
import static java.lang.String.format;

public class TestData {

    static String SCHEMA = "gasp_test";

    Connection cx;

    public static TestData get(Connection cx) throws SQLException {
        return new TestData(cx);
    }

    TestData(Connection cx) throws SQLException {
        this.cx = cx;
        run(new Task<Void>() {
            @Override
            public Void run(Connection cx) throws Exception {
                open(new SQL("CREATE SCHEMA IF NOT EXISTS %s", SCHEMA).compile(cx)).executeUpdate();
                open(new SQL("SET search_path TO %s", SCHEMA).compile(cx)).executeUpdate();
                return null;
            }
        }, cx);
    }

    /**
     * Loads U.S. census data into the database in the table "states".
     * <p>
     *  The dataset contains 52 entries including all 50 states, the District of Columbia, and Puerto Rico
     * </p>
     * {@linkplain https://www.census.gov/geo/maps-data/data/tiger-line.html}
     *
     */
    public TestData setUpStates() throws IOException, SQLException {
        runScript(load("states.sql"), null, null, cx);
        return this;
    }

    public TestData tearDown() throws SQLException {
        run(new Task<Void>() {
            @Override
            public Void run(Connection cx) throws Exception {
                open(new SQL("DROP SCHEMA %s CASCADE", SCHEMA).compile(cx)).executeUpdate();
                return null;
            }
        }, cx);
        return this;
    }

    InputStream load(String filename) throws IOException {
        return new GZIPInputStream(getClass().getResourceAsStream(filename + ".gz"));
    }
}
