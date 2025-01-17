package oss.fosslight.api.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class GetLicenseDetailsDto {
    @Setter
    @Getter
    @Builder(toBuilder = true)
    public static class Request extends Paging {
        String id;
    }

    @Builder
    public static class Result {
        public LicenseDetailsDto license;
    }
}
