package oss.fosslight.api.entity;

import oss.fosslight.domain.ComBean;

import java.io.Serializable;

public class ApiDeclaredLicense extends ComBean implements Serializable {

    private String licenseName;

    private String licenseComb;

    public ApiDeclaredLicense(String licenseName, String licenseComb) {
        this.licenseName = licenseName;
        this.licenseComb = licenseComb;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseComb() {
        return licenseComb;
    }

    public void setLicenseComb(String licenseComb) {
        this.licenseComb = licenseComb;
    }
}
