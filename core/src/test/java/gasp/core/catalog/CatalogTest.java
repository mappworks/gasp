package gasp.core.catalog;

import gasp.core.Config;
import gasp.core.db.Task;
import gasp.core.test.Db;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;

import static gasp.core.db.DbUtil.run;
import static java.lang.String.format;
import static gasp.core.catalog.Catalog.TABLE_INFO;
import static org.junit.Assert.assertTrue;

public class CatalogTest {

    @ClassRule
    public static Db db = new Db();

    @Test
    public void testInit() throws Exception {
        Catalog cat = new Catalog(db.get(), new Config());
        cat.init();

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
}
