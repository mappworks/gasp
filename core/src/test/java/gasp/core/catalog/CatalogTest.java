package gasp.core.catalog;

import gasp.core.Config;
import gasp.core.db.Task;
import gasp.core.model.Dataset;
import gasp.core.test.Db;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;

import static gasp.core.db.DbUtil.run;
import static java.lang.String.format;
import static gasp.core.catalog.Catalog.TABLE_INFO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CatalogTest {

    @ClassRule
    public static Db db = new Db();

    Catalog cat;

    @Before
    public void init() throws Exception {
        cat = new Catalog(db.get(), new Config());
        cat.init();
    }

    @Test
    public void testInit() throws Exception {
        try (Connection cx = db.conn()) {
            run(new Task<Void>() {
                @Override
                public Void run(Connection cx) throws Exception {
                    ResultSet rs =
                        open(open(cx.createStatement()).executeQuery(format("SELECT * FROM %s", TABLE_INFO)));
                    assertTrue(rs.next());
                    return null;
                }
            }, cx);
        }
    }

    @Test
    public void testAdd() throws Exception {
        Dataset ds = new Dataset().name("foo").query("select * from foo");
        cat.add(ds);

        assertNotNull(ds.id());
        assertTrue(cat.dataset(ds.id()).isPresent());
    }

    @Test
    public void testSave() throws Exception {
        Dataset ds = new Dataset().name("foo").query("select * from foo");
        cat.add(ds);

        ds.name("bar");
        cat.save(ds);

        ds = cat.dataset(ds.id()).get();
        assertEquals("bar", ds.name());
    }
}
