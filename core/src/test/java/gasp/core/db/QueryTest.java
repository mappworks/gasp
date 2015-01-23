package gasp.core.db;

import gasp.core.test.Db;
import gasp.core.test.TestData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.util.Collections;

import static com.google.common.collect.Iterators.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class QueryTest {

    @ClassRule
    public static Db db = new Db();

    static TestData testData;

    @BeforeClass
    public static void setUpData() throws Exception {
        testData = TestData.get(db.get()).setUpStates();
    }

    @Test
    public void testSimple() throws Exception {
        try (Connection cx = db.conn()) {
            Query q = Query.build("SELECT * FROM states").compile(cx);
            assertThat(size(q.run(null)), is(52));
        }
    }

    @Test
    public void testWithParams() throws Exception {
        try (Connection cx = db.conn()) {
            Query q = Query.build(
                "SELECT * FROM states WHERE persons > (" +
                    "SELECT persons FROM states WHERE state_abbr = ${abbr}" +
                ")").compile(cx);

            assertThat(size(q.run(Collections.singletonMap("abbr", "NY"))), is(2));
            assertThat(size(q.run(Collections.singletonMap("abbr", "TX"))), is(1));
        }
    }

    @Test
    public void testPaged() throws Exception {
        try (Connection cx = db.conn()) {
            Query q = Query.build("SELECT * FROM states").paged().compile(cx);

            assertThat(size(q.page(10, null).run(null)), is(10));
            assertThat(size(q.page(null, 40).run(null)), is(12));
        }
    }
}