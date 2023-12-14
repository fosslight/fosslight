package oss.fosslight.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class SelfCheckDto {
    String projectId;
    String projectName;
    String projectVersion;
    String created;
    String modified;

    String cveId;
    String cvssScore;

    List<String> packages = new ArrayList<>();
    String report;
    String notice;

    private void addPackageFileId(String id) {
        if (id == null) return;
        packages.add(id);
    }

    public void setPackageFileId(String id) {
        addPackageFileId(id);
    }

    public void setPackageFileId2(String id) {
        addPackageFileId(id);
    }

    public void setPackageFileId3(String id) {
        addPackageFileId(id);
    }
}
