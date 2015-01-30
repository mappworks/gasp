package gasp.core.db;

import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import gasp.core.test.Db;
import gasp.core.test.TestData;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Iterators.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
            Query q = Query.build("SELECT * FROM states").compile(cx);

            assertThat(size(q.page(10, null).run(null)), is(10));
            assertThat(size(q.page(null, 40).run(null)), is(12));
        }
    }

    @Test
    public void testGeometry() throws Exception {
        try (Connection cx = db.conn()) {
            QueryResult r = Query.build("SELECT geom FROM states").compile(cx).page(1,0).run(null);
            assertTrue(r.hasNext());

            Object obj = r.next().get(0);
            assertNotNull(obj);
            assertThat(obj, instanceOf(Geometry.class));
        }
    }

    @Test
    public void testBounds() throws Exception {
        try (Connection cx = db.conn()) {
            Query q = Query.build("SELECT * FROM states").bound().compile(cx);
            q.bounds(new Envelope(-109.060062, -102.042089, 36.993016,41.002359));

            Set<String> states = Sets.newHashSet("UT", "AZ", "NM", "WY", "CO", "OK", "KS", "NE");

            QueryResult r = q.run();
            r.forEachRemaining((row) -> states.remove(row.get("state_abbr")));

            assertThat("all states matched", states.isEmpty());
        }
    }
}
