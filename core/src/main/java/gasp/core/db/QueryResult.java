package gasp.core.db;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.vividsolutions.jts.geom.Geometry;
import gasp.core.db.QueryResult.Serializer;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static gasp.core.db.DbUtil.closeSafe;

/**
 * Result of a {@link gasp.core.db.Query}.
 */
@JsonSerialize(using = Serializer.class)
public class QueryResult extends AbstractIterator<Row> {

    Query q;
    ResultSet rs;
    Row row;
    Optional<Consumer<Query>> callback = Optional.empty();

    public QueryResult(ResultSet rs, Query q) {
        this.rs = rs;
        this.q = q;
        this.row = new Row(rs, this);
    }

    QueryResult(QueryResult other) {
        this(other.rs, other.q);
        this.callback = other.callback;
    }

    /**
     * Specifies a callback to invoke after the query results have been exhausted.
     */
    public QueryResult then(Consumer<Query> call) {
        callback = Optional.ofNullable(call);
        return this;
    }

    /**
     * List of columns contained in the query results.
     */
    public List<Column> columns() {
        try {
            List<Column> cols = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 0; i < meta.getColumnCount(); i++) {
                int ii = i+1;
                cols.add(new Column()
                    .name(meta.getColumnName(ii))
                    .type(meta.getColumnTypeName(ii))
                    .sqlType(meta.getColumnType(ii)));
            }
            return cols;
        }
        catch(SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Returns the index (0-based) of the column in the result.
     *
     * @param column The column name.
     *
     * @return The optional column index.
     */
    public Optional<Integer> indexOf(String column) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 0; i < meta.getColumnCount(); i++) {
                if (column.equals(meta.getColumnName(i+1))) {
                    return Optional.of(i);
                }
            }

            return Optional.empty();
        }
        catch(SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected Row computeNext() {
        try {
            if (rs.next()) {
                return row;
            }

            closeSafe(rs);
            callback.ifPresent((cb) -> cb.accept(null));
            return endOfData();
        }
        catch(SQLException e) {
            Throwables.propagate(e);
            return null;
        }
    }

    /**
     * Returns the query result object in a form serializable as GeoJson.
     */
    public QueryResult toGeoJson() {
        return new GeoJsonQueryResult(this);
    }

    static class Serializer extends StdSerializer<QueryResult> {

        Serializer() {
            super(QueryResult.class);
        }

        @Override
        public void serialize(QueryResult value, JsonGenerator jg, SerializerProvider provider)
            throws IOException, JsonGenerationException {
            jg.writeStartObject();

            List<Column> cols = value.columns();
            jg.writeArrayFieldStart("columns");
            for (Column col : cols) {
                jg.writeObject(col) ;
            }
            jg.writeEndArray();

            jg.writeArrayFieldStart("rows");
            for (Iterator<Row> it = value; it.hasNext(); ) {
                Row row = it.next();
                jg.writeStartArray(cols.size());
                for (Object val : row.list()) {
                    jg.writeObject(val);
                }
                jg.writeEndArray();
            }
            jg.writeEndArray();

            jg.writeEndObject();
        }
    }

    @JsonSerialize(using = GeoJsonSerializer.class)
    class GeoJsonQueryResult extends QueryResult {
        QueryResult result;
        GeoJsonQueryResult(QueryResult result) {
            super(result);
        }
    }

    static class GeoJsonSerializer extends StdSerializer<GeoJsonQueryResult> {

        GeoJsonSerializer() {
            super(GeoJsonQueryResult.class);
        }

        @Override
        public void serialize(GeoJsonQueryResult result, JsonGenerator jg, SerializerProvider provider)
                throws IOException, JsonGenerationException {

            jg.writeStartObject();
            jg.writeStringField("type", "FeatureCollection");
            jg.writeArrayFieldStart("features");

            List<Column> cols = result.columns();
            for (Iterator<Row> it = result; it.hasNext(); ) {
                Row row = it.next();
                jg.writeStartObject();

                jg.writeStringField("type", "Feature");

                // properties
                jg.writeObjectFieldStart("properties");

                Geometry geo = null;
                List<Object> vals = row.list();
                for (int i = 0; i < cols.size(); i++) {


                    Object val = vals.get(i);
                    if (val instanceof Geometry && geo == null) {
                        geo = (Geometry) val;
                        continue;
                    }
                    jg.writeObjectField(cols.get(i).name(), val);
                }

                jg.writeEndObject();

                // geometry
                if (geo != null) {
                    jg.writeObjectField("geometry", geo);
                }

                jg.writeEndObject();
            }

            jg.writeEndArray();
            jg.writeEndObject();
        }
    }
}
