package gasp.core.catalog;

import gasp.core.db.Mapper;
import gasp.core.model.Dataset;
import gasp.core.model.QueryParam;
import gasp.core.util.Json;

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
