package oss.fosslight.common;

public enum SearchType {
    OSS("OSS"),
    PROJECT("PROJECT"),
    LICENSE("LICENSE"),
    SELF_CHECK("SELF_CHECK"),
    THIRD_PARTY("THIRD_PARTY"),
    VULNERABILITY("VULNERABILITY");

    private final String name;

    SearchType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
