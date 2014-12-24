package gasp.core.db;

import java.util.ArrayList;
import java.util.List;

public class DbQuery {

    Integer offset;
    Integer limit;
    List<Sort> orderBy = new ArrayList<>();

    public DbQuery offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer offset() {
        return offset;
    }

    public DbQuery limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer limit() {
        return limit;
    }

    public DbQuery orderBy(String col, boolean asc) {
        orderBy.add(new Sort(col, asc));
        return this;
    }

    public List<Sort> orderBy() {
        return orderBy;
    }

    public static class Sort {
        final String col;
        final boolean asc;

        Sort(String col, boolean asc) {
            this.col = col;
            this.asc = asc;
        }
    }
}
