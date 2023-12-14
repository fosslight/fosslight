package oss.fosslight.api.dto;

import lombok.Data;
import org.springframework.security.core.parameters.P;
import oss.fosslight.domain.ProjectIdentification;

import java.util.ArrayList;
import java.util.List;

@Data
public class SelfCheckOssDto {
    String gridId;

    String path;
    String ossId;
    String ossName;
    String ossVersion;

    String downloadUrl;
    String homepageUrl;
    String description;
    String copyright;
    String userGuide;

    Boolean vuln;
    String cveId;
    String cvssScore;

    String restrictions;
    List<Character> obligations;
    List<LicenseDto> licenses;

    String attribution;

    Boolean exclude = false;

    public void setObligations(String obligationType) {
        var typeArr = obligationType.toCharArray();
        obligations = new ArrayList<>();
        obligations.add(typeArr[0] == '0' ? 'N' : 'Y');
        obligations.add(typeArr[1] == '0' ? 'N' : 'Y');
    }

    public SelfCheckOssDto(ProjectIdentification projectIdentification) {
        gridId = projectIdentification.getGridId();
        path = projectIdentification.getFilePath();
        ossId = projectIdentification.getOssId();
        ossName = projectIdentification.getOssName();
        ossVersion = projectIdentification.getOssVersion();
        downloadUrl = projectIdentification.getDownloadLocation();
        homepageUrl = projectIdentification.getHomepage();
        copyright = projectIdentification.getCopyrightText();
        userGuide = projectIdentification.getLicenseUserGuideStr();
        cveId = projectIdentification.getCveId();
        cvssScore = projectIdentification.getCvssScore();
        attribution = projectIdentification.getAttribution();
        exclude = "Y".equals(projectIdentification.getExcludeYn());
        restrictions = projectIdentification.getRestriction();
        vuln = "Y".equals(projectIdentification.getVulnYn());

        var obligationType = projectIdentification.getObligationType();
        if (obligationType != null) {
            setObligations(obligationType);
        } else {
            obligations = new ArrayList<>();
        }
    }
}
