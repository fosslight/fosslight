package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;
import oss.fosslight.domain.OssComponents;

@Data
@Builder
public class SecurityExportItemDto {
    private String ossName;
    private String ossVersion;
    private String score;
    private String vulnerabilityId;
    private String publDate;
    private String modiDate;
    private String vulnSummary;
    private String vulnerabilityResolution;
    private String vulnerabilityLink;
    private String officialPatchLink;
    private String securityPatchLink;
    private String cpeName;
    private String verStartEndRange;
    private String source;
    private String aliasIds;
    private String gridId;

    public static SecurityExportItemDto from(OssComponents item) {
        return SecurityExportItemDto.builder()
                .ossName(toEmpty(item.getOssName()))
                .ossVersion(toEmpty(item.getOssVersion()))
                .score(toEmpty(item.getCvssScore()))
                .vulnerabilityId(toEmpty(item.getCveId()))
                .publDate(toEmpty(item.getPublDate()))
                .modiDate(toEmpty(item.getModiDate()))
                .vulnSummary(toEmpty(item.getVulnSummary()))
                .vulnerabilityResolution(toEmpty(item.getVulnerabilityResolution()))
                .vulnerabilityLink(toEmpty(item.getVulnerabilityLink()))
                .officialPatchLink(toEmpty(item.getOfficialPatchLink()))
                .securityPatchLink(toEmpty(item.getSecurityPatchLink()))
                .cpeName(toEmpty(item.getCpeName()))
                .verStartEndRange(toEmpty(item.getVerStartEndRange()))
                .source("")
                .aliasIds("")
                .gridId(toEmpty(item.getGridId()))
                .build();
    }

    private static String toEmpty(String value) {
        return value == null ? "" : value;
    }
}
