package oss.fosslight.api.entity;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.domain.ComBean;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiOssMaster extends ComBean implements Serializable {

    private String ossName;

    private String[] ossNicknames;

    private String ossVersion;

    private List<ApiDeclaredLicense> declaredLicenses;

    private List<String> detectedLicenses;

    private String copyright;

    private String ossType;

    private String obligation;

    private String ossLicenseType;

    private String downloadLocation;

    private String[] downloadLocations;

    private String homepage;

    private String summaryDescription;

    private String attribution;

    private String comment;

    private List<OssLicense> ossLicenses;

    public ApiOssMaster() {
        super();
    }

    public ApiOssMaster(String ossName, List<ApiDeclaredLicense> declaredLicenses) {
        super();
        this.ossName = ossName;
        this.declaredLicenses = declaredLicenses;
    }

    public String getOssName() {
        return ossName;
    }

    public void setOssName(String ossName) {
        this.ossName = ossName;
    }

    public String[] getOssNicknames() {
        return ossNicknames;
    }

    public void setOssNicknames(String[] ossNicknames) {
        this.ossNicknames = ossNicknames;
    }

    public String getOssVersion() {
        return ossVersion;
    }

    public void setOssVersion(String ossVersion) {
        this.ossVersion = ossVersion;
    }

    public List<String> getDetectedLicenses() {
        return detectedLicenses;
    }

    public void setDetectedLicenses(List<String> detectedLicenses) {
        this.detectedLicenses = detectedLicenses;
    }

    public List<ApiDeclaredLicense> getDeclaredLicenses() {
        return declaredLicenses;
    }

    public void setDeclaredLicenses(List<ApiDeclaredLicense> declaredLicenses) {
        this.declaredLicenses = declaredLicenses;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getOssType() {
        return ossType;
    }

    public void setOssType(String ossType) {
        this.ossType = ossType;
    }

    public String getObligation() {
        return obligation;
    }

    public void setObligation(String obligation) {
        this.obligation = obligation;
    }

    public String getOssLicenseType() {
        return ossLicenseType;
    }

    public void setOssLicenseType(String ossLicenseType) {
        this.ossLicenseType = ossLicenseType;
    }

    public String getDownloadLocation() {
        return downloadLocation;
    }

    public void setDownloadLocation(String downloadLocation) {
        this.downloadLocation = downloadLocation;
    }

    public String[] getDownloadLocations() {
        return downloadLocations;
    }

    public void setDownloadLocations(String[] downloadLocations) {
        this.downloadLocations = downloadLocations;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getSummaryDescription() {
        return summaryDescription;
    }

    public void setSummaryDescription(String summaryDescription) {
        this.summaryDescription = summaryDescription;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<OssLicense> getOssLicenses() {
        return ossLicenses;
    }

    public void setOssLicenses(List<OssLicense> ossLicenses) {
        this.ossLicenses = ossLicenses;
    }

    public void setValidOssLicenses() {
        List<OssLicense> ossLicenses = new ArrayList<>();
        Set<String> declaredLicenseNameSet = new HashSet<>();
        for (ApiDeclaredLicense declaredLicense : declaredLicenses) {
            String refineLicenseName = declaredLicense.getLicenseName().trim().toUpperCase();
            if(isEmpty(refineLicenseName)) {
                throw new RuntimeException("LicenseName Not Exist");
            }
            if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(refineLicenseName)) {
                throw new RuntimeException(refineLicenseName + " -> Not Managed License");
            }
            if (declaredLicenseNameSet.contains(refineLicenseName)) {
                continue;
            }
            declaredLicenseNameSet.add(refineLicenseName);
            LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(refineLicenseName);
            OssLicense ossLicense = new OssLicense(licenseMaster.getLicenseId(), licenseMaster.getLicenseName());
            ossLicense.setOssLicenseComb(declaredLicense.getLicenseComb());
            ossLicenses.add(ossLicense);
        }
        this.ossLicenses = ossLicenses;
    }

    public void checkDetectedLicenseManaged() {
        if (detectedLicenses == null) {
            return;
        }
        for (String licenseName : detectedLicenses) {
            LicenseMaster detectedLicenseInfo = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase().trim());
            if (detectedLicenseInfo == null) {
                throw new RuntimeException(licenseName + " -> Not Managed License");
            }
        }
    }

    public void detectedLicenseNotIncludeDeclaredLicense() {
        if (detectedLicenses == null) {
            return;
        }
        Set<String> declaredLicenseNameSet = new HashSet<>();
        for (ApiDeclaredLicense declaredLicense : declaredLicenses) {
            declaredLicenseNameSet.add(declaredLicense.getLicenseName().toUpperCase().trim());
        }

        Set<String> detectedLicenseNameSet = new HashSet<>();
        List<String> uniqueDetectedLicenses = new ArrayList<>();
        for (String detectedLicenseName : detectedLicenses) {
            String refineLicenseName = detectedLicenseName.toUpperCase().trim();
            if (declaredLicenseNameSet.contains(refineLicenseName)) {
                throw new RuntimeException("License included in Declared");
            }
            if(detectedLicenseNameSet.contains(refineLicenseName)) {
                continue;
            }
            detectedLicenseNameSet.add(refineLicenseName);
            uniqueDetectedLicenses.add(detectedLicenseName);
        }
        this.detectedLicenses = uniqueDetectedLicenses;
    }

    public OssMaster toOssMaster() {
        OssMaster ossMaster = new OssMaster();
        ossMaster.setOssName(this.ossName);
        ossMaster.setOssLicenses(this.ossLicenses);
        ossMaster.setOssNicknames(this.ossNicknames);
        ossMaster.setOssVersion(this.ossVersion);
        ossMaster.setDetectedLicenses(this.detectedLicenses);
        ossMaster.setCopyright(this.copyright);
        ossMaster.setOssType(this.ossType);
        ossMaster.setObligation(this.obligation);
        ossMaster.setDownloadLocations(this.downloadLocations);
        ossMaster.setHomepage(this.homepage);
        ossMaster.setSummaryDescription(this.summaryDescription);
        ossMaster.setAttribution(this.attribution);
        ossMaster.setComment(this.comment);
        return ossMaster;
    }
}
