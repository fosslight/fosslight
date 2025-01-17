package oss.fosslight.api.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class ListSelfCheckDto {
    @Builder
    @Data
    @EqualsAndHashCode(callSuper=false)
    public static class Request extends Paging {
        String projectId;
        String createdFrom;
        String createdTo;
        String projectName;
        Boolean projectNameExact;
        String licenseName;
        Boolean licenseNameExact;
        String ossName;
        Boolean ossNameExact;
        String creator;
    }

    @Builder
    public static class Result {
        List<SelfCheckDto> list;
        int totalCount;
    }
}
