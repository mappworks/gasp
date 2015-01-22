package gasp.core.db;

import com.google.common.base.Throwables;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Row {

    ResultSet rs;

    Row(ResultSet rs) {
        this.rs = rs;
    }

    public List<Object> list() {
        try {
            int n = rs.getMetaData().getColumnCount();
            List<Object> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                list.add(rs.getObject(i + 1));
            }
            return list;
        }
        catch(SQLException e) {
            throw Throwables.propagate(e);
        }
    }
    public Object get(int i) throws SQLException {
        return rs.getObject(i+1);
    }

    public Object get(String name) {
        return null;
    }
}
