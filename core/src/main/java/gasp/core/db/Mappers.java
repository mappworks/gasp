package gasp.core.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mappers used by this package.
 */
class Mappers {

    static Table table(ResultSet rs) throws SQLException {
        return new Table()
            .name(rs.getString("TABLE_NAME"))
            .schema(rs.getString("TABLE_SCHEM"))
            .type(rs.getString("TABLE_TYPE"));
    }
}
