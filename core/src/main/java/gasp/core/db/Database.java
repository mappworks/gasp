package gasp.core.db;


import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import gasp.core.catalog.Catalog;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static gasp.core.db.DbUtil.run;
import static gasp.core.db.DbUtil.stream;
public class Database {

    static final Set<String> FILTERED_TABLES = Sets.newHashSet(
        "geometry_columns", "geography_columns", "raster_columns", "spatial_ref_sys", "raster_overviews",
        Catalog.TABLE_INFO, Catalog.TABLE_DATASET
    );

    static final String[] TABLE_TYPES = {"TABLE", "VIEW"};

    Connection cx;
    DatabaseMetaData meta;

    public Database(Connection cx) throws SQLException {
        this.cx = cx;
        this.meta = cx.getMetaData();
    }

    public Iterator<Table> tables(String schema) throws SQLException {
        Iterator<Table> it = stream(new Task<ResultSet>() {
            @Override
            public ResultSet run(Connection cx) throws Exception {
                return open(meta.getTables(null, schema, "%", TABLE_TYPES));
            }
        }, Mappers::table, cx);

        return Iterators.filter(it, (t) -> !FILTERED_TABLES.contains(t.name()));
    }

    public Optional<Table> table(String name, String schema) throws SQLException {
        return Optional.ofNullable(run(new Task<Table>() {
            @Override
            public Table run(Connection cx) throws Exception {
                ResultSet tbls = open(meta.getTables(null, schema, name, TABLE_TYPES));
                if (tbls.next()) {
                    Table t = Mappers.table(tbls);

                    ResultSet cols = open(meta.getColumns(null, t.schema(), t.name(), "%"));
                    while (cols.next()) {
                        t.column(new Column()
                            .name(cols.getString("COLUMN_NAME"))
                            .type(cols.getString("TYPE_NAME"))
                            .sqlType(cols.getInt("DATA_TYPE")));
                    }

                    return t;
                }

                return null;
            }
        }, cx));
    }
}
