package gasp.core.catalog;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import gasp.core.Config;
import gasp.core.Gasp;
import gasp.core.db.DbSupport;
import gasp.core.db.SQL;
import gasp.core.db.Task;
import gasp.core.model.Dataset;
import gasp.core.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

/**
 * Data access object for the Gasp catalog.
 * <p>
 * The catalog provides to persisted datasets.
 * </p>
 */
public class Catalog extends DbSupport {

    public static final String TABLE_INFO = "gasp_info";
    public static final String TABLE_DATASET = "gasp_dataset";

    public static enum Event {INIT, DISPOSE};

    public static interface Listener {
        void event(Object subject, Catalog cat);
    };

    static final Logger LOG = LoggerFactory.getLogger(Catalog.class);

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

    /**
     * Initializes the catalog.
     * <p>
     *  In general this method shouldn't be called by app code, just by the framework.
     * </p>
     */
    public void init() throws Exception {
        run(new CatalogTask<Void>() {
            @Override
            protected Void doRun(Connection cx) throws Exception {
                try {
                    ResultSet rs =
                        open(open(new SQL("SELECT * FROM %s", TABLE_INFO).trace(LOG).compile(cx)).executeQuery());
                    // TODO: check for upgrade
                }
                catch(SQLException e) {
                    // table not found, initialize
                    Map<String,String> vars = Maps.newHashMap();
                    vars.put("scope", schema.map((schema) -> schema + ".").orElse(""));
                    vars.put("version", Gasp.version());
                    vars.put("revision", Gasp.revision());
                    runScript("init.sql", Catalog.class, ";;", vars, cx);
                    fire(Event.INIT, this);
                }
                return null;
            }
        });
    }

    /**
     * Returns a set of datasets as specified by the given query.
     *
     * @param q The query.
     */
    public Iterator<Dataset> datasets(CatalogQuery q) throws Exception {
        return stream(new CatalogTask<ResultSet>() {
            @Override
            protected ResultSet doRun(Connection cx) throws Exception {
                SQL sql = new SQL("SELECT * FROM %s", TABLE_DATASET);
                q.apply(sql);

                return open(sql.log(LOG).compile(cx)).executeQuery();
            }
        }, Mappers::dataset);
    }

    /**
     * Returns the optional dataset for the specified id.
     *
     * @param id The dataset id.
     */
    public Optional<Dataset> dataset(final String id) throws Exception {
        return Optional.ofNullable(run(new CatalogTask<Dataset>() {
            @Override
            protected Dataset doRun(Connection cx) throws Exception {
                SQL sql = new SQL()
                    .a("SELECT * FROM %s WHERE id = ?::uuid", TABLE_DATASET)
                    .p(id)
                    .log(LOG);

                ResultSet rs = open(open(sql.compile(cx)).executeQuery());
                if (rs.next()) {
                    return Mappers.dataset(rs);
                }

                return null;
            }
        }));
    }

    /**
     * Adds a new dataset to the catalog.
     *
     * @param ds The dataset to add.
     */
    public void add(Dataset ds) throws Exception {
        runInTransaction(new CatalogTask<Dataset>() {
            @Override
            protected Dataset doRun(Connection cx) throws Exception {
                // insert into object
                StringBuilder cols =
                    new StringBuilder(format("INSERT INTO %s (id, name, query", TABLE_DATASET));
                SQL sql = new SQL(" VALUES (?::uuid,?,?")
                    .p(newId())
                    .p(ds.name())
                    .p(ds.query());


                if (ds.title() != null) {
                    cols.append(", title");
                    sql.a(",?").p(ds.title());
                }

                if (ds.description() != null) {
                    cols.append(", description");
                    sql.a(",?").p(ds.title());
                }

                if (!ds.meta().map().isEmpty()) {
                    cols.append(", meta");
                    sql.a(",?").p(Json.to(ds.meta()));
                }

                sql.a(") RETURNING id");
                sql.buffer().insert(0, cols.append(")").toString());

                ResultSet id = open(open(sql.log(LOG).compile(cx)).executeQuery());
                id.next();

                return (Dataset) ds.id(id.getString("id"));
            }
        });
    }

    /**
     * Saves changed to a dataset back to the catalog.
     *
     * @param ds The dataset to save.
     */
    public void save(Dataset ds) throws Exception {
        runInTransaction(new CatalogTask<Integer>() {
            @Override
            protected Integer doRun(Connection cx) throws Exception {
                // insert into object
                SQL sql = new SQL("UPDATE %s SET name = ?, title = ?, description = ?, query = ?,  meta = ?::json" +
                    " WHERE id = ?::uuid", TABLE_DATASET)
                    .p(ds.name())
                    .p(ds.title())
                    .p(ds.description())
                    .p(ds.query())
                    .p(!ds.meta().map().isEmpty() ? Json.to(ds.meta().map()) : null)
                    .p(ds.id());

                return open(sql.log(LOG).compile(cx)).executeUpdate();
            }
        });
    }

    /**
     * Removes a dataset from the catalog.
     *
     * @param ds The dataset to remove.
     */
    public void remove(Dataset ds) throws Exception {
        runInTransaction(new CatalogTask<Integer>() {
            @Override
            protected Integer doRun(Connection cx) throws Exception {
                SQL sql = new SQL("DELETE FROM %s WHERE id = ?::uuid", TABLE_DATASET)
                        .p(ds.id())
                        .log(LOG);
                return open(sql.compile(cx)).executeUpdate();
            }
        });
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
            } catch (Throwable t) {
                LOG.warn("Listener threw exception on event: " + e, t);
            }
        }
    }

    /**
     * Custom task that ensures statements run against the proper schema.
     */
    abstract class CatalogTask<T> extends Task<T> {

        @Override
        public final T run(Connection cx) throws Exception {
            // first thing we do is set the search_path based on
            // the configured schema
            if (schema.isPresent()) {
                String s = schema.get();
                open(new SQL("CREATE SCHEMA IF NOT EXISTS %s", s).log(LOG).compile(cx)).execute();
                open(new SQL("SET search_path TO %s", s).log(LOG).compile(cx)).execute();
            }

            return doRun(cx);
        }

        protected abstract T doRun(Connection cx) throws Exception;

        @Override
        protected void close(Object obj) {
            if (obj instanceof Connection) {
                // reset the search_path before we give the connection back
                Connection cx = (Connection) obj;
                try {
                    try (Statement st = cx.createStatement()) {
                        st.execute("SET search_path TO default");
                    }
                }
                catch(Exception e) {
                    LOG.warn("Error occurred resetting connection search_path", e);
                }
            }
            super.close(obj);
        }
    }
}
