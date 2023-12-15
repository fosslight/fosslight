package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SelfCheckFileDeleteDto {
    @Data
    public static class Request {
        String fileId;
    }

    @Builder
    public static class Response {
        List<FileDto> files;
    }
}
