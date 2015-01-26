package gasp.app.api;

import gasp.app.api.ex.NotFound;
import gasp.core.db.Database;
import gasp.core.db.Table;
import gasp.core.db.Task;
import gasp.core.util.GaspIterator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/api/tables")
public class TableCtrl extends BaseCtrl {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Iterator<Table> list(@RequestParam(required = false) String schema) throws Exception {
        return run((callback) -> new Task<Iterator<Table>>() {
            @Override
            public Iterator<Table> run(Connection cx) throws Exception {
                return new GaspIterator<Table>(new Database(cx).tables(schema))
                    .then((v) -> callback.accept(this));
            }
        });
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public @ResponseBody Table get(@PathVariable String name, @RequestParam(required = false) String schema) throws Exception {
        return run(new Task<Optional<Table>>() {
            @Override
            public Optional<Table> run(Connection cx) throws Exception {
                return new Database(cx).table(name, schema);
            }
        }).orElseThrow(() -> new NotFound(
            format("No table named '%s' found", name) + schema != null ? " in schema "+schema : ""));
    }
}
