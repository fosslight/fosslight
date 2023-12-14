package oss.fosslight.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LicenseDetailsDto extends LicenseDto {
    private List<String> licenseNicknames;
    private String attribution = "";
}
