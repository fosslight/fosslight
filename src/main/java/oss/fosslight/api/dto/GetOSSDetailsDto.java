package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class GetOSSDetailsDto {
    @Builder
    public static class Result {
        public OssDto oss;
    }
}
