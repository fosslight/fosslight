package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {
    String fileId;
    String fileSeq;
    String logiNm;
    String orgNm;
    String created;
}
