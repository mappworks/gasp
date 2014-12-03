package jasp.core.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a result set to an object.
 */
public interface Mapper<T> {

    /**
     * Maps the result set in it's current state (ie, current row) to the
     * desired object.
     */
    T map(ResultSet rs) throws SQLException;
}
