package gasp.core.test;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

import static gasp.core.db.DbUtil.runScript;

/**
 * Harness for test datasets.
 */
public class TestData {

    Connection cx;

    public static TestData get(DataSource db) throws SQLException {
        return new TestData(db.getConnection());
    }

    TestData(Connection cx) throws SQLException {
        this.cx = cx;
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

    InputStream load(String filename) throws IOException {
        return new GZIPInputStream(getClass().getResourceAsStream(filename + ".gz"));
    }
}
