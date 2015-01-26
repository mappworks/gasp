package gasp.app.api;

import gasp.core.db.Query;
import gasp.core.db.QueryResult;
import gasp.core.db.Task;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Controller
@RequestMapping("/api/query")
public class QueryCtrl extends BaseCtrl {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody QueryResult execute(
        @RequestParam String q,
        @RequestParam(required = false, defaultValue = "50") Integer n,
        @RequestParam(required = false, defaultValue = "0") Integer p,
        HttpServletRequest req
    ) throws Exception {

        return run((callback) -> new Task<QueryResult>() {
            @Override
            public QueryResult run(Connection cx) throws Exception {
                Query query = Query.build(q).paged().compile(cx);
                query.page(n, n*p).run(null);

                QueryResult result = query.run(null);
                result.then((v) -> callback.accept(this));

                Optional<MediaType> format = responseFormat(req);
                return APPLICATION_GEOJSON.equals(format.orElse(APPLICATION_JSON)) ? result.toGeoJson() : result;
            }
        });
    }
}
