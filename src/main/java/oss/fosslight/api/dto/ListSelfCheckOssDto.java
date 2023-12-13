package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class ListSelfCheckOssDto {
    @Builder
    @Data
    public static class Request {
        String projectId;
    }

    @Builder
    public static class Result {
        List<SelfCheckOssDto> list;
    }
}
