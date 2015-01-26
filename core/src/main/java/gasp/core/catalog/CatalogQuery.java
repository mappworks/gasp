package gasp.core.catalog;

import gasp.core.db.SQL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A query against the {@link gasp.core.catalog.Catalog}.
 */
public class CatalogQuery {

    Integer offset;
    Integer limit;
    List<Sort> orderBy = new ArrayList<>();

    /**
     * Sets the offset (number of records to skip) of the query.
     */
    public CatalogQuery offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Offset (number of records to skip) of the query.
     */
    public Integer offset() {
        return offset;
    }

    /**
     * Sets the limit (number of records to return) of the query.
     */
    public CatalogQuery limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Limit (number of records to return) of the query.
     */
    public Integer limit() {
        return limit;
    }

    /**
     * Adds a sorting clause to the query.
     * <p>
     * <tt>col</tt> should be one of the attributes of {@link gasp.core.model.Dataset}.
     * </p>
     * @param col Column to sort on.
     * @param asc Flag to control ascending/descending sort.
     */
    public CatalogQuery orderBy(String col, boolean asc) {
        orderBy.add(new Sort(col, asc));
        return this;
    }

    /**
     * Sort clauses of the query.
     */
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
