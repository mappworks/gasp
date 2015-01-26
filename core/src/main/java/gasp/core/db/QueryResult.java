package gasp.core.db;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
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
}
