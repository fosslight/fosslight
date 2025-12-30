package oss.fosslight.api.dto;

public enum SortDirection {
    ASC("asc"), DESC("desc");

    SortDirection(String value) {
        this.value = value;
    }

    public static Boolean isAsc(SortDirection dir) {
        return dir == ASC;
    }

    public String toString() {
        return value;
    }

    public final String value;
}
