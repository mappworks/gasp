package gasp.app.api;

import gasp.core.db.Query;
import gasp.core.db.QueryResult;
import gasp.core.db.Task;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;

@Controller
@RequestMapping("/api/query")
public class QueryCtrl extends BaseCtrl {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody QueryResult execute(
        @RequestParam String q,
        @RequestParam(required = false, defaultValue = "50") Integer n,
        @RequestParam(required = false, defaultValue = "0") Integer p) throws Exception {

        return run((callback) -> new Task<QueryResult>() {
            @Override
            public QueryResult run(Connection cx) throws Exception {
                Task self = this;
                Query query = Query.build(q).paged().compile(cx);
                query.page(n, n*p).run(null);
                return query.run(null).then((v) -> callback.accept(self));
            }
        });
    }
}
