package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class GetSelfCheckDetailsDto {
    @Builder
    public static class Result {
        SelfCheckDto selfCheck;
    }
}
