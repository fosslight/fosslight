package oss.fosslight.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oss.fosslight.api.dto.GetLicenseDetailsDto;
import oss.fosslight.api.dto.LicenseDto;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.repository.ApiLicenseMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.service.ApiLicenseService;

import java.util.ArrayList;

@Service
public class ApiLicenseServiceImpl implements ApiLicenseService {
    @Autowired
    LicenseMapper licenseMapper;

    @Autowired
    ApiLicenseMapper apiLicenseMapper;

    @Override
    public ListLicenseDto.Result listLicenses(ListLicenseDto.Request request) {
        var totalCount = apiLicenseMapper.selectLicenseMasterTotalCount(request);
        var list = apiLicenseMapper.selectLicenseList(request);

        return ListLicenseDto.Result.builder()
                .list(list)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public GetLicenseDetailsDto.Result getLicense(GetLicenseDetailsDto.Request request) {
        var license = apiLicenseMapper.selectLicenseById(request.getId());
        var nicknames = apiLicenseMapper.selectLicenseNicknameList(license.getLicenseName());
        license.setLicenseNicknames(nicknames);
        return GetLicenseDetailsDto.Result.builder()
                .license(license)
                .build();
    }
}
