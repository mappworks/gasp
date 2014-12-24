package gasp.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Query object used to obtain results from a dao.
 */
public class Query {

    Integer limit;
    Integer offset;
    List<String> orderBy = new ArrayList<>();

    public Query limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Integer limit() {
        return limit;
    }

    public Query offset(int offset) {
        this.offset = offset;
        return this;
    }

    public Integer offset() {
        return offset;
    }

    public Query orderBy(String property, boolean asc) {
        orderBy.add(property+" "+ (asc?"ASC":"DESC"));
        return this;
    }

    public List<String> orderBy() {
        return orderBy;
    }
}
