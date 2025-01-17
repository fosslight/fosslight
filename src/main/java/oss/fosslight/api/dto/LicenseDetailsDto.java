package oss.fosslight.api.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class LicenseDetailsDto extends LicenseDto {
    private List<String> licenseNicknames;
}
