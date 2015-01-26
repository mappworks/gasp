package gasp.core.catalog;

import gasp.core.model.Dataset;
import gasp.core.util.Json;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.Map;

/**
 * Mappers used by {@link Catalog}.
 */
class Mappers {

    static final Dataset dataset(ResultSet rs) throws Exception {
        return new Dataset()
            .id(rs.getString("id"))
            .name(rs.getString("name"))
            .title(rs.getString("title"))
            .description(rs.getString("description"))
            .created(rs.getTimestamp("created"))
            .modified(rs.getTimestamp("modified"))
            .creator(rs.getString("creator"))
            .query(rs.getString("query"))
            .meta(Json.from(rs.getString("meta"), Map.class).orElse(Collections.emptyMap()));
    }
}
