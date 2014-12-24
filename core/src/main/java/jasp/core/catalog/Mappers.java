package jasp.core.catalog;

import jasp.core.db.Mapper;
import jasp.core.model.Dataset;
import jasp.core.model.QueryParam;
import jasp.core.util.Json;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

class Mappers {

    static final Mapper<Dataset> dataset() {
        return (rs) -> {
            Dataset d = new Dataset();

            d.id(rs.getString("id"));
            d.name(rs.getString("name")).title(rs.getString("title")).description(rs.getString("description"));
            d.created(rs.getTimestamp("created")).modified(rs.getTimestamp("modified"));
            d.creator(rs.getString("creator"));
            d.query(rs.getString("query"));

            d.meta().map().putAll(Json.from(rs.getString("meta"), Map.class).orElse(Collections.emptyMap()));
            d.params().addAll(Json.from(rs.getString("params"), QueryParam[].class)
                    .map((a) -> Arrays.asList(a)).orElse(Collections.emptyList()));

            return d;
        };
    }
}
