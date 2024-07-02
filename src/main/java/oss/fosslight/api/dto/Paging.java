package oss.fosslight.api.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Paging {
    protected int page;
    protected int countPerPage;
    protected SortDirection sortDirection;
    protected String sortColumn;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected String sort;

    public int getOffset() {
        return (page - 1) * countPerPage;
    }

    public void setSort(String sort) {
        if (sort == null || sort.isEmpty() || !sort.contains("-")) return;
        var sortSplit = sort.split("-");
        this.sortColumn = sortSplit[0].toUpperCase();
        if (sortSplit[1].equalsIgnoreCase("ASC")) {
            this.sortDirection = SortDirection.ASC;
        } else {
            this.sortDirection = SortDirection.DESC;
        }
    }

    public String getSortDirection() {
        if (sortDirection == SortDirection.ASC) {
            return "asc";
        } else {
            return "desc";
        }
    }

    public void setLimit(int limit) {
        page = 1;
        countPerPage = limit;
    }
}
