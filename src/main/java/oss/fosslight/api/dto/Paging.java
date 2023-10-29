package oss.fosslight.api.dto;

public class CommonDto {
    public enum SortDirection {
        ASC, DESC
    }

    public static class Paging {
        int page;
        int countPerPage;
        SortDirection sort;
        String sortColumn;
    }
}
