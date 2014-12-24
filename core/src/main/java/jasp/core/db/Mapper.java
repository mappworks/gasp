package jasp.core.db;

import java.sql.ResultSet;

/**
 * Functional interface to map a row of a result set to an object.
 */
public interface Mapper<T> {

    /**
     * Maps the current row of the result set to the desired object.
     *
     * @param rs The result set.
     */
    T map(ResultSet rs) throws Exception;
}
