package oss.fosslight.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LicenseDto implements Comparable<LicenseDto> {
    private String licenseId;
    private String licenseName;
    private String licenseType;
    private String ossId;
    private String licenseText;
    private LicenseObligation obligations = new LicenseObligation();
    private String obligationChecks;
    private String licenseIdentifier;
    private String webpage;
    private String description;
    private String creator;
    private String modifier;
    private String created;
    private String modified;
    private List<String> restrictions = new ArrayList<>();

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
    public void setRestriction(String restriction) {
        var split = restriction.split(",");
        restrictions = Arrays.stream(split).map(String::strip)
                .filter(str -> str.length() > 0)
                .collect(Collectors.toList());
    }

    @Data
    public static class LicenseObligation {
        Boolean notice = false;
        Boolean source = false;
        Boolean needsCheck = false;
    }
}
