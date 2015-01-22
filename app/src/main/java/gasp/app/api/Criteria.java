package gasp.app.api;

import gasp.core.catalog.CatalogQuery;

import java.util.Arrays;

/**
 * Bean used to capture paging/filtering parameters for api calls.
 */
public class Criteria {

    Integer page;
    Integer count;
    String sort;
    String filter;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public CatalogQuery toDbQuery() {
        CatalogQuery q = new CatalogQuery();
        if (page != null && count != null) {
            q.offset(page*count);
        }
        if (count != null) {
            q.limit(count);
        }
        if (sort != null) {
            Arrays.asList(sort.split(";")).forEach((s) -> {
                boolean asc = !s.endsWith("-");
                String col = s.replaceAll("(?:\\+|-)$", "");
                q.orderBy(col, asc);
            });
        }
        return q;
    }
}
