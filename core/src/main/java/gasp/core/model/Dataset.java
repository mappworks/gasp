package gasp.core.model;

import java.util.Date;
import java.util.Map;

/**
 * A named dataset.
 */
public class Dataset extends NamedObject {

    String query;

    public String query() {
        return query;
    }

    public Dataset query(String query) {
        this.query = query;
        return this;
    }

    // overrides to type narrow
    @Override
    public Dataset id(String id) {
        return (Dataset) super.id(id);
    }

    @Override
    public Dataset creator(String creator) {
        return (Dataset) super.creator(creator);
    }

    @Override
    public Dataset created(Date created) {
        return (Dataset) super.created(created);
    }

    @Override
    public Dataset modified(Date modified) {
        return (Dataset) super.modified(modified);
    }

    @Override
    public Dataset tag(String tag) {
        return (Dataset) super.tag(tag);
    }

    @Override
    public Dataset name(String name) {
        return (Dataset) super.name(name);
    }

    @Override
    public Dataset title(String title) {
        return (Dataset) super.title(title);
    }

    @Override
    public Dataset description(String description) {
        return (Dataset) super.description(description);
    }

    @Override
    public Dataset meta(Map<String, Object> kvp) {
        return (Dataset) super.meta(kvp);
    }
}
