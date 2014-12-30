package gasp.core.catalog;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import gasp.core.Config;
import gasp.core.db.Result;
import gasp.core.db.SQL;
import gasp.core.model.Dataset;
import gasp.core.db.DbQuery;
import gasp.core.db.DbSupport;
import gasp.core.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

public class Catalog extends DbSupport implements AutoCloseable {

    public static enum Event {INIT, DISPOSE};

    public static interface Listener {
        void event(Object subject, Catalog cat);
    };

    static final Logger LOG = LoggerFactory.getLogger(Catalog.class);

    static final String TABLE_DATASET = "gasp_dataset";

    Optional<String> schema;
    Multimap<Event,Listener> listeners =
        Multimaps.newListMultimap(Maps.newLinkedHashMap(), ArrayList::new);

    public Catalog(DataSource dataSource, Config config) {
        super(dataSource);
        schema = config.get(Config.GROUP_DATABASE, "schema");
    }

    public Catalog on(Event e, Listener l) {
        listeners.put(e, l);
        return this;
    }

    public void init() throws Exception {
        Map<String,String> vars =
            Collections.singletonMap("scope", schema.map((s) -> s + ".").orElse(""));
        //runScript("drop.sql", Catalog.class, vars);
        runScript("init.sql", Catalog.class, vars);

        fire(Event.INIT, this);
    }

    public Result<Dataset> datasets(DbQuery q) throws Exception {
        return stream((cx, s) -> {
           SQL sql = new SQL("SELECT * FROM %s", TABLE_DATASET).a(q);
           return s.open(sql.log(LOG).compile(cx)).executeQuery();
        }, Mappers.dataset());
    }

    public Optional<Dataset> dataset(final String id) throws Exception {
        return Optional.ofNullable(run((cx,s) -> {
            SQL sql = new SQL()
                .a("SELECT * FROM %s WHERE id = ?", TABLE_DATASET)
                .p(id)
                .log(LOG);

            ResultSet rs = s.open(s.open(sql.compile(cx)).executeQuery());
            if (rs.next()) {
                return Mappers.dataset().map(rs);
            }

            return null;
        }));
    }

    public void add(Dataset ds) throws Exception {
        runInTx((cx,s) -> {
            // insert into object
            StringBuilder cols =
                new StringBuilder(format("INSERT INTO %s (id, name, query", TABLE_DATASET));
            SQL sql = new SQL(" VALUES (?,?,?")
                .p(newId())
                .p(ds.name())
                .p(ds.query());


            if (ds.title() != null) {
                cols.append(", title");
                sql.p(ds.title());
            }

            if (ds.description() != null) {
                cols.append(", description");
                sql.p(ds.title());
            }

            if (!ds.params().isEmpty()) {
                cols.append(", params");
                sql.a(",?").p(Json.to(ds.params()));
            }

            if (!ds.meta().map().isEmpty()) {
                cols.append(", meta");
                sql.a(",?").p(Json.to(ds.meta()));
            }

            if (ds.folder() != null) {
                cols.append(", folder_id");
                sql.p(ds.folder().id());
            }

            sql.a(") RETURNING id");
            sql.buffer().insert(0, cols.append(")").toString());

            ResultSet id = s.open(s.open(sql.log(LOG).compile(cx)).executeQuery());
            id.next();

            return ds.id(id.getString("id"));
        });
    }

    public void save(Dataset ds) throws Exception {
        runInTx((cx,s) -> {
            // insert into object
            SQL sql = new SQL( "UPDATE %s SET name = ?, title = ?, description = ?, query = ?, params = ?, meta = ?"+
                ", folder_id = ? WHERE id = ?", TABLE_DATASET)
                .p(ds.name())
                .p(ds.title())
                .p(ds.description())
                .p(ds.query())
                .p(!ds.params().isEmpty() ? Json.to(ds.params()) : null)
                .p(!ds.meta().map().isEmpty() ? Json.to(ds.meta().map()) : null)
                .p(ds.folder() != null ? ds.folder().id() : null)
                .p(ds.id());

            return s.open(sql.log(LOG).compile(cx)).executeUpdate();
        });
    }

    public void remove(Dataset ds) throws Exception {
        runInTx((cx,s) -> {
            SQL sql = new SQL("DELETE FROM %s WHERE id = ?", TABLE_DATASET)
                .p(ds.id())
                .log(LOG);
            return s.open(sql.compile(cx)).executeUpdate();
        });
    }

    @Override
    public void close() {
        fire(Event.DISPOSE, this);
    }

    String newId() {
        return UUID.randomUUID().toString();
    }

    String scope(String name) {
        return schema.map((s) -> s + "." + name).orElse(name);
    }

    void fire(Event e, Object subject) {
        for (Listener l : listeners.get(e)) {
            try {
                l.event(subject, this);
            }
            catch(Throwable t) {
                LOG.warn("Listener threw exception on event: " + e, t);
            }
        }
    }
}
