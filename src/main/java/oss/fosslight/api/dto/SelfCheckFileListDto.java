package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SelfCheckFileListDto {
    @Builder
    @Data
    public static class File {
        String fileId;
        String fileSeq;
        String logiName;
        String orgNm;
        String created;
    }

    @Builder
    public static class Response {
        List<File> files;
    }
}
