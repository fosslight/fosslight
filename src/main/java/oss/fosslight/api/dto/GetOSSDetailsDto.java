package oss.fosslight.api.dto;

import lombok.Builder;

public class GetOSSDetailsDto {
    @Builder
    public static class Result {
        public OssDetailsDto oss;
    }
}
