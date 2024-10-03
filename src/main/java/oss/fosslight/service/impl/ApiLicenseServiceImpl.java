package oss.fosslight.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.GetLicenseDetailsDto;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.repository.ApiLicenseMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.service.ApiLicenseService;
import oss.fosslight.util.ExcelDownLoadUtil;

import jakarta.annotation.PostConstruct;

@Service
public class ApiLicenseServiceImpl extends CoTopComponent implements ApiLicenseService {
    @Autowired
    LicenseMapper licenseMapper;

    @Autowired
    ApiLicenseMapper apiLicenseMapper;

    private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;

    @PostConstruct
    public void setResourcePathPrefix() {
        RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
    }

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
    public String getLicenseExcel(ListLicenseDto.Request request) throws Exception {
        var licensesList = listLicenses(request).list;
        var dataStr = toJson(licensesList);
        var downloadId = ExcelDownLoadUtil.getExcelDownloadId(
                "lite-licenses",
                dataStr,
                RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX
        );
        return downloadId;
    }

    @Override
    public GetLicenseDetailsDto.Result getLicense(String id) {
        var license = apiLicenseMapper.selectLicenseById(id);
        var nicknames = apiLicenseMapper.selectLicenseNicknameList(license.getLicenseName());
        license.setLicenseNicknames(nicknames);
        return GetLicenseDetailsDto.Result.builder()
                .license(license)
                .build();
    }
}
