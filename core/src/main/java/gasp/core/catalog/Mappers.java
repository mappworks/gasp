package gasp.core.catalog;

import gasp.core.db.Mapper;
import gasp.core.model.Dataset;
import gasp.core.util.Json;

import java.util.Collections;
import java.util.Map;

class Mappers {

    static final Mapper<Dataset> dataset() {
        return (rs) ->
            new Dataset()
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
