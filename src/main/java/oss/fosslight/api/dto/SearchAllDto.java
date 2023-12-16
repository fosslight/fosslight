package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SearchAllDto {
    @Builder
    public static class Response {
        List<OssDto> oss;
        List<LicenseDto> licenses;
        List<VulnerabilityDto> vulnerabilities;
    }
}
