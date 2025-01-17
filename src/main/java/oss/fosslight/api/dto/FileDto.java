package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;
import oss.fosslight.domain.T2File;

@Data
public class FileDto {
    String fileId;
    String fileSeq;
    String logiNm;
    String orgNm;
    String created;

    public FileDto(T2File file) {
        fileId = file.getOrgFileId();
        created = file.getCreatedDate();
        orgNm = file.getOrigNm();
        logiNm = file.getLogiNm();
        fileSeq = file.getFileSeq();
    }
}
