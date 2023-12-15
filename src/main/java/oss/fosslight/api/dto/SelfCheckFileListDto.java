package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SelfCheckFileListDto {
    @Builder
    @Data
    public static class File {
        String name;
        String when;
        String id;
        String logiName;
        String seq;
    }

    @Builder
    public static class Response {
        List<File> files;
    }
}
