package gasp.app.api;

import com.vividsolutions.jts.geom.Envelope;
import gasp.core.db.Query;
import gasp.core.db.Query.QueryBuilder;
import gasp.core.db.QueryResult;
import gasp.core.db.Task;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.awt.Dimension;
import java.sql.Connection;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Controller
@RequestMapping("/api/query")
public class QueryCtrl extends BaseCtrl {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody QueryResult execute(@RequestParam String q, HttpServletRequest req) throws Exception {

        return run((callback) -> new Task<QueryResult>() {
            @Override
            public QueryResult run(Connection cx) throws Exception {
                // handle parameters
                RequestParams params = params(req);

                Optional<Double> tol = params.consume("simplify", Double.class);
                Optional<Envelope> box = params.consume("bbox", Envelope.class);
                Optional<Dimension> size = params.consume("width", Integer.class, "height", Integer.class, (w,h) ->
                    w.isPresent() && h.isPresent() ? new Dimension(w.get(), h.get()) : null);

                // build the query
                QueryBuilder qb = Query.build(q);

                if (box.isPresent()) qb.bound();
                if (tol.isPresent() || (box.isPresent() && size.isPresent())) qb.simplify();

                Query query = qb.compile(cx);

                box.ifPresent((b) -> query.bounds(b));
                if (box.isPresent() && size.isPresent()) {
                    query.tolerance(box.get(), size.get().width, size.get().height);
                }

                tol.ifPresent((t) -> query.tolerance(t));


                // paging
                int count = params.consume("count", Integer.class).orElse(-1);
                int offset = count * params.consume("page", Integer.class).orElse(0);
                query.page(count, offset);

                // run it
                QueryResult result = query.run(params.all()).then((x) -> callback.accept(this));

                MediaType format = responseFormat(req).orElse(APPLICATION_JSON);
                return APPLICATION_GEOJSON.equals(format) ? result.toGeoJson() : result;
            }
        });
    }
}
