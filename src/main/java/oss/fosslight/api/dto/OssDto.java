package oss.fosslight.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OssDto {
    String ossId;
    String ossType;
    String ossName;
    String ossVersion;
    List<LicenseDto> ossLicenses;
    String licenseName;
    String licenseType;
    String downloadUrl;
    String homepageUrl;
    String summaryDescription;
    String cveId;
    String cvssScore;
    String creator;
    String created;
    String modifier;
    String modified;
    String obligations;

}
