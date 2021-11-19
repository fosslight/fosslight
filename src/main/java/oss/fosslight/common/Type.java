package oss.fosslight.common;

public enum Type {

    OSS("oss"),
    LICENSE("license"),
    SELFCHECK("selfcheck"),
    PROJECT("project"),
    THIRDPARTY("partner"),
    VULNERABILITY("vulnerability");

    final String name;

    Type(String name){
        this.name = name;
    }

    public String getName(){
            return this.name;
    }

}
