package oss.fosslight.api.dto;

import lombok.Data;

@Data
public class OssDto {
    String ossId;
    String ossType;
    String ossName;
    String ossVersion;
    String licenseName;
    String licenseType;
    String downloadUrl;
    String homepageUrl;
    String summaryDescription;
    String cveId;
    String cvssScore;
    String creator;
    String createdDate;
    String modifier;
    String modifiedDate;
}
