package oss.fosslight.api.dto;

import lombok.Data;

@Data
public class LicenseDto implements Comparable<LicenseDto> {
    private String licenseId;
    private String licenseName;
    private String licenseType;
    private String ossId;
    private String licenseText;
    private String obligation;
    private String obligationChecks;

    @Override
    public int compareTo(LicenseDto o) {
        return licenseId.compareTo(o.licenseId);
    }
}
