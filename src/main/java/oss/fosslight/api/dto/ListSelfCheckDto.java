package oss.fosslight.api.dto;

import lombok.Builder;

import java.util.List;

public class ListSelfCheckDto {
    @Builder
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
    }

    @Builder
    public static class Result {
        List<SelfCheckDto> list;
        int totalCount;
    }
}
