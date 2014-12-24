package jasp.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A named dataset.
 */
public class Dataset extends ContainedObject {

    String query;
    List<QueryParam> params = new ArrayList<>();

    public String query() {
        return query;
    }

    public Dataset query(String query) {
        this.query = query;
        return this;
    }

    public List<QueryParam> params() {
        return params;
    }

    public Dataset param(QueryParam param) {
        params.add(param);
        return this;
    }

}
