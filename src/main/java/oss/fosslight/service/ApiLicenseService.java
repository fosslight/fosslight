package oss.fosslight.service;

import oss.fosslight.api.dto.GetLicenseDetailsDto;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.domain.LicenseMaster;

import java.util.List;

public interface ApiLicenseService {
    ListLicenseDto.Result listLicenses(ListLicenseDto.Request request);
    GetLicenseDetailsDto.Result getLicense(GetLicenseDetailsDto.Request request);
}
