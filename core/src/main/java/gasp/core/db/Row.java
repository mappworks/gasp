package gasp.core.db;

import com.google.common.base.Throwables;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Row in the result set of a {@link gasp.core.db.Query}.
 */
public class Row {

    ResultSet rs;
    QueryResult result;

    Row(ResultSet rs, QueryResult result) {
        this.rs = rs;
        this.result = result;
    }

    /**
     * All of the values in the row as a list.
     */
    public List<Object> list() {
        try {
            int n = rs.getMetaData().getColumnCount();
            List<Object> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                list.add(get(i));
            }
            return list;
        }
        catch(SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Gets the row value for the ith (zero-based) column.
     *
     * @param i Zero-based index.
     */
    public Object get(int i) throws SQLException {
        Object obj = rs.getObject(i+1);
        if (obj instanceof PGobject) {
            obj = handle((PGobject) obj);
        }
        return obj;
    }

    Object handle(PGobject obj) {
        String val = obj.getValue();
        if (result.q.builder.raw) {
            return val;
        }

        if ("geometry".equalsIgnoreCase(obj.getType())) {
            try {
                return new WKBReader().read(WKBReader.hexToBytes(val));
            } catch (ParseException e) {
                //TODO: should probably not send the entire string into the message
                throw new IllegalArgumentException("Unable to parse geometry: " + val);
            }
        }

        return val;
    }
}
