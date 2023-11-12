package oss.fosslight.api.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Paging {
    public enum SortDirection {
        ASC("asc"), DSC("desc");

        SortDirection(String value) {
            this.value = value;
        }

        public final String value;
    }

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
        var sortSplit = sort.toUpperCase().split("-");
        this.sortColumn = sortSplit[0].toUpperCase();
        this.sortDirection = SortDirection.valueOf(sortSplit[1]);
    }
}
