package oss.fosslight.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LicenseDto implements Comparable<LicenseDto> {
    protected String licenseId;
    protected String licenseName;
    protected String licenseType;
    protected String ossId;
    protected String licenseText;
    protected String obligations;
    protected String licenseIdentifier;
    protected String homepageUrl;
    protected String description;
    protected String creator;
    protected String modifier;
    protected String created;
    protected String modified;
    protected List<String> restrictions = new ArrayList<>();

    @Override
    public int compareTo(LicenseDto o) {
        return licenseId.compareTo(o.licenseId);
    }

    public void setRestriction(String restriction) {
        var split = restriction.split(",");
        restrictions = Arrays.stream(split).map(String::strip)
                .filter(str -> str.length() > 0)
                .collect(Collectors.toList());
    }
}
