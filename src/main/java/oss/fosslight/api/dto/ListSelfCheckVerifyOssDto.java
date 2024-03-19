package oss.fosslight.api.dto;

import lombok.Builder;

import java.util.List;

public class ListSelfCheckVerifyOssDto {
    @Builder
    public static class Result {
        List<OssDto> oss;
    }
}
