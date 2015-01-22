package gasp.core.db;

import com.google.common.collect.Iterators;
import gasp.core.test.DBConnection;
import gasp.core.test.TestData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;

import static com.google.common.collect.Iterators.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class QueryTest {

    @ClassRule
    public static DBConnection conn = new DBConnection();

    static TestData testData;

    @BeforeClass
    public static void setUpData() throws Exception {
        testData = TestData.get(conn.get()).setUpStates();
    }

    @AfterClass
    public static void tearDownData() throws Exception {
        testData.tearDown();
    }

    @Test
    public void testSimple() throws Exception {
        Query q = Query.build("SELECT * FROM states").compile(conn.get());
        assertThat(size(q.run(null)), is(52));
    }

    @Test
    public void testWithParams() throws Exception {
        Query q = Query.build(
            "SELECT * FROM states WHERE persons > (" +
                "SELECT persons FROM states WHERE state_abbr = ${abbr}" +
            ")").compile(conn.get());

        assertThat(size(q.run(Collections.singletonMap("abbr", "NY"))), is(2));
        assertThat(size(q.run(Collections.singletonMap("abbr", "TX"))), is(1));
    }

    @Test
    public void testPaged() throws Exception {
        Query q = Query.build("SELECT * FROM states").paged().compile(conn.get());

        assertThat(size(q.page(10, null).run(null)), is(10));
        assertThat(size(q.page(null, 40).run(null)), is(12));
    }
}
