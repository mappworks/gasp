package gasp.app.api;

import gasp.app.api.ex.NotFound;
import gasp.core.catalog.Catalog;
import gasp.core.model.Dataset;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Iterator;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/api/datasets", produces = APPLICATION_JSON_VALUE)
public class DatasetCtrl extends BaseCtrl {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Iterator<Dataset> list(Criteria cri) throws Exception {
        return streamWithCatalog((cat) -> cat.datasets(cri.toDbQuery()));

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Dataset get(@PathVariable String id) throws Exception {
        return doWithCatalog((cat) -> dataset(id, cat));
    }

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public @ResponseBody Dataset create(@RequestBody Dataset dataset) throws Exception {
        return doWithCatalog((cat) -> {
            cat.add(dataset);
            return cat.dataset(dataset.id()).get();
        });
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE)
    public @ResponseBody Dataset update(@RequestBody Dataset dataset) throws Exception {
        return doWithCatalog((cat) -> {
            cat.save(dataset);
            return cat.dataset(dataset.id()).get();
        });
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void remove(@PathVariable String id) throws Exception {
        doWithCatalog((cat) -> {
            cat.remove(dataset(id, cat));
            return null;
        });
    }

    Dataset dataset(String id, Catalog cat) throws Exception {
        return cat.dataset(id).orElseThrow(() -> new NotFound(format("No dataset with id %s exists", id)));
    }
}
