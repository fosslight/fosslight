/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.GetOSSDetailsDto;
import oss.fosslight.api.dto.LicenseDto;
import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.api.dto.OssDto;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.repository.ApiOssMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.service.ApiOssService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.OssService;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.StringUtil;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ApiOssServiceImpl extends CoTopComponent implements ApiOssService {
	/** The api oss mapper. */
	@Autowired ApiOssMapper apiOssMapper;

	@Autowired OssService ossService;

	@Autowired HistoryService historyService;

    @Autowired OssMapper ossMapper;

    @Autowired FileService fileService;

    private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;

    @PostConstruct
    public void setResourcePathPrefix() {
        RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
    }

    @Override
    public List<Map<String, Object>> getOssInfo(Map<String, Object> paramMap) {
        String rtnOssName = apiOssMapper.getOssName((String) paramMap.get("ossName"));

        if (!StringUtil.isEmpty(rtnOssName)) {
            paramMap.replace("ossName", rtnOssName);
        }

        return apiOssMapper.getOssInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getOssInfoByDownloadLocation(String downloadLocation) {
        return apiOssMapper.getOssInfoByDownloadLocation(downloadLocation);
    }

    @Override
    public List<Map<String, Object>> getLicenseInfo(String licenseName) {
        return apiOssMapper.getLicenseInfo(licenseName);
    }


    public String[] getOssNickNameListByOssName(String ossName) {
        List<String> nickList = null;
        if (!StringUtil.isEmpty(ossName)) {
            nickList = apiOssMapper.selectOssNicknameList(ossName);
            if (nickList != null) {
                nickList = nickList.stream()
                        .filter(CommonFunction.distinctByKey(nick -> nick.trim().toUpperCase()))
                        .collect(Collectors.toList());
            }
        }

        nickList = (nickList != null ? nickList : Collections.emptyList());
        return nickList.toArray(new String[nickList.size()]);
    }

    @Override
    public ListOssDto.Result listOss(ListOssDto.Request request) {
        var ossMaster = request.toOssMaster();

        request.setVersionCheck(true);
        var list = apiOssMapper.selectOssList(request);

        List<String> multiOssList = ossMapper.selectMultiOssList(ossMaster);
        multiOssList.replaceAll(String::toUpperCase);
        int totalCount = ossMapper.selectOssMasterTotalCount(ossMaster);

        var rows = list.stream().flatMap(oss -> {
            if (!multiOssList.contains(oss.getOssName().toUpperCase())) {
                return Stream.of(oss);
            }
            var query = request.toBuilder()
                    .ossName(oss.getOssName())
                    .ossId(oss.getOssId())
                    .build()
                    .toOssMaster();
            var sublist = apiOssMapper.selectOssSubList(query);
            return sublist.stream();
        }).collect(Collectors.toList());

        // license name 처리
        if (!rows.isEmpty()) {
            var ossIdList = rows.stream().map(OssDto::getOssId).collect(Collectors.toList());
            List<LicenseDto> licenseList = apiOssMapper.selectOssLicenseList(ossIdList);
            rows.forEach(oss -> {
                var licensesForOss = licenseList.stream()
                        .filter(license -> license.getOssId().equals(oss.getOssId()))
                        .map(license -> {
                            var ossLicense = new OssLicense();
                            ossLicense.setLicenseName(license.getLicenseName());
                            ossLicense.setOssLicenseComb(license.getComb());
                            return ossLicense;
                        }).collect(Collectors.toList());
                var licensesString = CommonFunction.makeLicenseExpression(licensesForOss);
                oss.setLicenseName(licensesString);
            });
        }

        return ListOssDto.Result.builder()
                .list(rows)
                .totalCount(totalCount)
                .build();
    }

    public String getOssExcel(ListOssDto.Request request) throws Exception {
        var ossList = listOss(request).list;
        var dataStr = toJson(ossList);
        var downloadId = ExcelDownLoadUtil.getExcelDownloadId(
                "lite-oss",
                dataStr,
                RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX
        );
        return downloadId;
    }

    public List<OssDto> listRecentOss(int limit) {
        var rows = apiOssMapper.selectRecentOss(limit);
        return addLicenseInfo(rows);
    }

    public List<OssDto> listNameSearchResult(String query, int limit) {
        var ossQuery = ListOssDto.Request.builder()
                .ossName(query)
                .build();
        ossQuery.setLimit(limit);
        ossQuery.setVersionCheck(true);
        var rows = apiOssMapper.selectOssList(ossQuery);
        return addLicenseInfo(rows);
    }

    public GetOSSDetailsDto.Result getOss(String id) {
        var oss = apiOssMapper.selectOssById(id);

        var idList = Collections.singletonList(oss.getOssId());
        List<LicenseDto> licenseList = apiOssMapper.selectOssLicenseList(idList);
        oss.setLicenses(licenseList);

        var vulnerabilityList = apiOssMapper.getOssVulnerabilityList(oss.getOssId());
        oss.setVulnerabilities(vulnerabilityList);

        var nicknames = apiOssMapper.selectOssNicknameList(oss.getOssName());
        oss.setOssNicknames(nicknames);

        return GetOSSDetailsDto.Result
                .builder()
                .oss(oss)
                .build();
    }

    private List<OssDto> addLicenseInfo(List<OssDto> list) {
        var ossIdList = list.stream().map(OssDto::getOssId).collect(Collectors.toList());
        List<LicenseDto> licenseList = apiOssMapper.selectOssLicenseList(ossIdList);
        list.forEach(oss -> {
            var licensesForOss = licenseList.stream()
                    .filter(license -> license.getOssId().equals(oss.getOssId()))
                    .map(license -> {
                        var ossLicense = new OssLicense();
                        ossLicense.setLicenseName(license.getLicenseName());
                        ossLicense.setOssLicenseComb(license.getComb());
                        return ossLicense;
                    }).collect(Collectors.toList());
            var licensesString = CommonFunction.makeLicenseExpression(licensesForOss);
            oss.setLicenseName(licensesString);
        });
        return list;
    }
}