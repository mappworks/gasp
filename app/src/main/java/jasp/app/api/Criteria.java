package jasp.app.api;

import jasp.core.db.DbQuery;

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

    public DbQuery toDbQuery() {
        DbQuery q = new DbQuery();
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
