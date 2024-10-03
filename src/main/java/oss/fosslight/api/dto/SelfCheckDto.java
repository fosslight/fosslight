package oss.fosslight.api.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import jakarta.xml.bind.annotation.XmlElementDecl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class SelfCheckDto {
    String projectId;
    String projectName;
    String projectVersion;
    String created;
    String modified;

    String cveId;
    String cvssScore;

    List<FileDto> packages = new ArrayList<>();
    String report;
    String notice;
    int ossCount;

    String comment;

    String creator;

    private List<String> fileIds = new ArrayList<>();

    public void setPackageFileIds(String idText) {
        fileIds = Arrays.stream(idText.split(","))
                .map(String::trim).collect(Collectors.toList());

    }
}
