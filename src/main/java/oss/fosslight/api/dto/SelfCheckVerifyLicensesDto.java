package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SelfCheckVerifyLicensesDto {
    @Data
    @Builder
    static class LicenseEntry {
        List<String> value;
        String msg;
    }

    @Data
    @Builder
    static class LicenseCheckResult {
        List<String> gridIds;
        LicenseEntry before;
        LicenseEntry after;
    }

    @Builder
    public static class Response {
        List<LicenseCheckResult> verificationOss;
    }
}
