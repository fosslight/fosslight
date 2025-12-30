package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SelfCheckVerifyLicensesDto {
    @Data
    @Builder
    public static class LicenseEntry {
        List<String> value;
        String msg;
    }

    @Data
    @Builder
    public static class LicenseCheckResult {
        String gridId;
        String ossName;
        String ossVersion;
        String downloadUrl;
        LicenseEntry before;
        LicenseEntry after;
    }

    @Builder
    public static class Response {
        List<LicenseCheckResult> verificationLicenses;
    }
}
