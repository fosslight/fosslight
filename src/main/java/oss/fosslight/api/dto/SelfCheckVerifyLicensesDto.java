package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SelfCheckVerifyLicensesDto {
    @Data
    @Builder
    public static class LicenseEntry {
//        List<String> value;
        String value;
        String msg;
    }

    @Data
    @Builder
    public static class LicenseCheckResult {
//        List<String> gridIds;
        String gridId;
        LicenseEntry before;
        LicenseEntry after;
    }

    @Builder
    public static class Response {
        List<LicenseCheckResult> verificationLicenses;
    }
}
