package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SelfCheckFileListDto {
    @Builder
    public static class Response {
        List<FileDto> files;
    }
}
