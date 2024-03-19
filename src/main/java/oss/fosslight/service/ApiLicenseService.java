package oss.fosslight.service;

import oss.fosslight.api.dto.GetLicenseDetailsDto;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.domain.LicenseMaster;

import java.util.List;

public interface ApiLicenseService {
    ListLicenseDto.Result listLicenses(ListLicenseDto.Request request);
    String getLicenseExcel(ListLicenseDto.Request request) throws Exception;
    GetLicenseDetailsDto.Result getLicense(String id);
}
