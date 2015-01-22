package gasp.core.catalog;

import gasp.core.db.SQL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class CatalogQuery {

    Integer offset;
    Integer limit;
    List<Sort> orderBy = new ArrayList<>();

    public CatalogQuery offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer offset() {
        return offset;
    }

    public CatalogQuery limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer limit() {
        return limit;
    }

    public CatalogQuery orderBy(String col, boolean asc) {
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

    SQL apply(SQL sql) {
        if (!orderBy.isEmpty()) {
            sql.a(" ORDER BY ");
            sql.a(orderBy.stream().map((s) -> s.col + " " + (s.asc ? "ASC" : "DESC")).collect(Collectors.joining(",")));
        }
        if (offset != null) {
            sql.a(format(" OFFSET %d", offset));
        }
        if (limit != null) {
            sql.a(format(" LIMIT %d", limit));
        }
        return sql;
    }
}
