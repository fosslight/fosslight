package oss.fosslight.api.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OssDetailsDto extends OssDto {
    List<String> ossNicknames;
    List<LicenseDto> licenses;
    List<VulnerabilityDto> vulnerabilities;
    Boolean deactivate;

    public void setDeactivate(String deactivateFlag) {
        deactivate = "Y".equals(deactivateFlag);
    }
}
