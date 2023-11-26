package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ListLicenseDto {
    @Setter
    @Getter
    @Builder(toBuilder = true)
    public static class Request extends Paging {
    }

    @Builder
    public static class Result {
        List<LicenseDto> list;
    }
}
