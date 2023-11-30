package oss.fosslight.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class LicenseDto implements Comparable<LicenseDto> {
    private String licenseId;
    private String licenseName;
    private String licenseType;
    private String ossId;
    private String licenseText;
    private LicenseObligation obligations = new LicenseObligation();
    private String obligationChecks;
    private String shortIdentifier;
    private String webpage;
    private String description;
    private String creator;
    private String modifier;
    private String created;
    private String modified;
    private String restriction;

    @Override
    public int compareTo(LicenseDto o) {
        return licenseId.compareTo(o.licenseId);
    }

    public void setObligationNotificationYn(String flag) {
        this.obligations.notice = flag.equalsIgnoreCase("y");
    }

    public void setObligationDisclosingSrcYn(String flag) {
        this.obligations.source = flag.equalsIgnoreCase("y");
    }

    public void setObligationNeedsCheckYn(String flag) {
        this.obligations.needsCheck = flag.equalsIgnoreCase("y");
    }

    @Data
    public static class LicenseObligation {
        Boolean notice = false;
        Boolean source = false;
        Boolean needsCheck = false;
    }
}
