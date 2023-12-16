package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class SelfCheckVerifyOssDto {
    @Data
    @Builder
    public static class OssEntry {
        String value;
        String msg;
    }

    @Data
    @Builder
    public static class OssCheckResult {
        List<String> gridIds;
        OssEntry before;
        OssEntry after;
    }

    @Builder
    public static class Response {
        List<OssCheckResult> verificationOss;
    }
}
