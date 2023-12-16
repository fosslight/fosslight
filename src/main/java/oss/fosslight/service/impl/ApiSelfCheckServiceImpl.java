/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import oss.fosslight.api.dto.*;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.*;
import oss.fosslight.repository.ApiSelfCheckMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.SelfCheckMapper;
import oss.fosslight.service.*;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Service
public class ApiSelfCheckServiceImpl implements ApiSelfCheckService {
    @Autowired
    SelfCheckService selfCheckService;

    @Autowired
    ApiSelfCheckMapper apiSelfcheckMapper;

    @Autowired
    SelfCheckMapper selfcheckMapper;

    @Autowired
    LicenseMapper licenseMapper;

    @Autowired
    FileService fileService;

    @Autowired
    OssService ossService;

    @Autowired
    ProjectService projectService;

    @Autowired
    AutoFillOssInfoService autoFillOssInfoService;

    @Override
    public int getCreateProjectCnt(String userId) {
        return apiSelfcheckMapper.getCreateProjectCnt(userId);
    }

    @Transactional
    @Override
    public Map<String, Object> createSelfCheck(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<String, Object>();

        apiSelfcheckMapper.createSelfCheck(paramMap);

        Long prjId = (Long) paramMap.get("prjId");
        result.put("prjId", prjId.toString());

        return result;
    }

    @Override
    public boolean existProjectCnt(Map<String, Object> paramMap) {
        int records = apiSelfcheckMapper.selectProjectCount(paramMap);

        return records == 1;
    }

    @Override
    public Map<String, Object> selectProjectMaster(String prjId) {
        return apiSelfcheckMapper.selectProjectMaster(prjId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getIdentificationGridList(String prjId, String code, List<ProjectIdentification> ossComponentList, List<List<ProjectIdentification>> ossComponentsLicenseList) {
        ProjectIdentification identification = new ProjectIdentification();
        identification.setReferenceId(prjId);
        identification.setReferenceDiv(code);
        Map<String, Object> map = selfCheckService.getIdentificationGridList(identification);
        if (map.containsKey("mainData")) {
            List<ProjectIdentification> gridDatas = (List<ProjectIdentification>) map.get("mainData");
            if (gridDatas != null && !gridDatas.isEmpty()) {
                List<List<ProjectIdentification>> gridDataLicenses = CommonFunction.setOssComponentLicense(gridDatas);
                Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(gridDatas, gridDataLicenses);
                ossComponentList.addAll((List<ProjectIdentification>) remakeComponentsMap.get("mainList"));
                ossComponentsLicenseList.addAll((List<List<ProjectIdentification>>) remakeComponentsMap.get("subList"));
            }
        }
    }

    @Override
    public boolean existsWatcherByEmail(String prjId, String email) {
        return apiSelfcheckMapper.existsWatcherByEmail(prjId, email) <= 0;
    }

    @Override
    public void insertWatcher(Map<String, Object> paramMap) {
        apiSelfcheckMapper.insertWatcher(paramMap);
    }

    @Override
    public ListSelfCheckDto.Result listSelfChecks(ListSelfCheckDto.Request request) {
        int totalCount = apiSelfcheckMapper.selectSelfCheckTotalCount(request);
        var results = apiSelfcheckMapper.selectSelfCheckList(request);

        Map<String, OssMaster> ossInfoMap = CoCodeManager.OSS_INFO_UPPER;

        for (SelfCheckDto selfCheck : results) {
            List<String> customNvdMaxScoreInfoList = new ArrayList<>();

            List<String> nvdMaxScoreInfoList = selfcheckMapper.findIdentificationMaxNvdInfo(selfCheck.getProjectId());
            List<String> nvdMaxScoreInfoList2 = selfcheckMapper.findIdentificationMaxNvdInfoForVendorProduct(selfCheck.getProjectId());

            if (!nvdMaxScoreInfoList.isEmpty()) {
                String conversionCveInfo = CommonFunction.checkNvdInfoForProduct(ossInfoMap, nvdMaxScoreInfoList);
                if (conversionCveInfo != null) {
                    customNvdMaxScoreInfoList.add(conversionCveInfo);
                }
            }

            if (nvdMaxScoreInfoList2 != null && !nvdMaxScoreInfoList2.isEmpty()) {
                customNvdMaxScoreInfoList.addAll(nvdMaxScoreInfoList2);
            }

            if (!customNvdMaxScoreInfoList.isEmpty()) {
                String conversionCveInfo = CommonFunction.getConversionCveInfoForList(customNvdMaxScoreInfoList);
                if (conversionCveInfo != null) {
                    String[] conversionCveData = conversionCveInfo.split("\\@");
                    selfCheck.setCvssScore(conversionCveData[3]);
                    selfCheck.setCveId(conversionCveData[4]);
                }

                customNvdMaxScoreInfoList.clear();
            }
            var fileIds = selfCheck.getFileIds();

            var files = fileIds.stream().map(id -> {
                var file = fileService.selectFileInfoById(id);
                return new FileDto(file);
            }).collect(Collectors.toList());
            selfCheck.setPackages(files);

            selfCheck.setReport("rep"); // TODO
            selfCheck.setNotice("not"); // TODO
        }

        return ListSelfCheckDto.Result.builder()
                .list(results)
                .totalCount(totalCount)
                .build();
    }


    public List<SelfCheckOssDto> listSelfCheckOss(String id) {
        var identification = new ProjectIdentification();
        identification.setReferenceId(id);
        identification.setReferenceDiv(CoConstDef.CD_DTL_SELF_COMPONENT_ID);
        var res = selfCheckService.getIdentificationGridList(identification);
        List<ProjectIdentification> mainData = (List<ProjectIdentification>) res.get("mainData");

        List<SelfCheckOssDto> list = mainData.stream()
                .map(projectIdentification -> {
                    var oss = new SelfCheckOssDto(projectIdentification);
                    var licenseList = projectIdentification.getComponentLicenseList();

                    var licenseName = projectIdentification.getLicenseName();
                    if (licenseName != null) {
                        var licenseNames = licenseName.split(",");
                        var licenses = Arrays.stream(licenseNames).map(name -> {
                                    var uppercaseName = name.toUpperCase().trim();
                                    var licenseDto = new LicenseDto();
                                    licenseDto.setLicenseName(name);
                                    if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(uppercaseName)) {
                                        var license = CoCodeManager.LICENSE_INFO_UPPER.get(uppercaseName);
                                        var licenseId = license.getLicenseId();
                                        licenseDto.setLicenseId(licenseId);
                                        return licenseDto;
                                    } else if (!uppercaseName.isEmpty()) {
                                        return licenseDto;
                                    }
                                    return null;
                                })
                                .filter(license -> license != null)
                                .collect(Collectors.toList());
                        oss.setLicenses(licenses);
                    }
                    return oss;
                })
                .collect(Collectors.toList());

        return list;
    }

    @Override
    public GetSelfCheckDetailsDto.Result getSelfCheck(String request) {
        var selfCheck = apiSelfcheckMapper.selectSelfCheckById(request);
        return GetSelfCheckDetailsDto.Result.builder()
                .selfCheck(selfCheck)
                .build();
    }

    @Override
    public List<T2File> listSelfCheckPackages(String id) {
        var selfCheckPackageIdsText = apiSelfcheckMapper.selectSelfCheckPackageIdsTextById(id);
        if (selfCheckPackageIdsText == null) {
            return new ArrayList<>();
        }
        List<T2File> files = Arrays.stream(selfCheckPackageIdsText.split(","))
                .map(String::trim)
                .map(fileId -> fileService.selectFileInfoById(fileId))
                .collect(Collectors.toList());
        return files;
    }

    @Override
    public List<SelfCheckVerifyOssDto.OssCheckResult> validateOss(String id) {
        List<ProjectIdentification> result = new ArrayList<>();
        var identification = new ProjectIdentification();
        identification.setReferenceId(id);
        identification.setReferenceDiv(CoConstDef.CD_DTL_SELF_COMPONENT_ID);
        identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
        var identificationMap = selfCheckService.getIdentificationGridList(identification);
        if (identificationMap == null) {
            return null; // implement output for none
        }

        var validator = new T2CoProjectValidator();
        validator.setProcType(validator.PROC_TYPE_IDENTIFICATION_SOURCE);

        var mainData = (List<ProjectIdentification>) identificationMap.get("mainData");
        validator.setAppendix("mainList", mainData);
        validator.setAppendix("subListMap", identificationMap.get("subData"));

        var validationResult = validator.validate();
        Map<String, String> validMap = null;
        Map<String, String> diffMap = null;

        if (!validationResult.isValid()) {
            validMap = validationResult.getValidMessageMap();
            result.addAll(ossService.checkOssNameData(mainData, validMap, null));
        }

        if (!validationResult.isDiff()) {
            diffMap = validationResult.getDiffMessageMap();
            result.addAll(ossService.checkOssNameData(mainData, null, diffMap));
        }

        result.addAll(ossService.checkOssNameData(mainData, null, null));

        if (result.isEmpty()) {
            return new ArrayList<>();
        }

        var checkedResult = ossService.checkOssName(result);

        var partitioned = checkedResult.stream().collect(Collectors.partitioningBy(prj ->
                prj.getCheckOssList().equals("I")));
        var mergedList = Stream.concat(
                partitioned.get(false).stream(),
                partitioned.get(true).stream()
        ).collect(Collectors.toList());

        var resultList = new ArrayList<SelfCheckVerifyOssDto.OssCheckResult>();

        for (var oss : mergedList) {
            var beforeMsg = validMap == null ? null : validMap.get("ossName." + oss.getComponentId());
            String afterMsg = null;
            if (oss.getCheckOssList().equals("I")) {
                afterMsg = "Invalid download location";
            } else if (oss.getRedirectLocation() != null) {
                afterMsg = "redirect url: " + oss.getRedirectLocation();
            }
            var checkResult = SelfCheckVerifyOssDto.OssCheckResult.builder()
                    .gridIds(oss.getComponentIdList())
                    .downloadUrl(oss.getDownloadLocation())
                    .before(SelfCheckVerifyOssDto.OssEntry.builder()
                            .value(oss.getOssName())
                            .msg(beforeMsg)
                            .build())
                    .after(SelfCheckVerifyOssDto.OssEntry.builder()
                            .value(oss.getCheckName())
                            .msg(afterMsg)
                            .build())
                    .build();
            resultList.add(checkResult);
        }

        return resultList;
    }

    public List<SelfCheckVerifyLicensesDto.LicenseCheckResult> validateLicenses(String id) {
        var query = new ProjectIdentification();
        query.setReferenceDiv(CoConstDef.CD_DTL_SELF_COMPONENT_ID);
        query.setReferenceId(id);

        List<ProjectIdentification> result = new ArrayList<>();

        Map<String, Object> map = selfCheckService.getIdentificationGridList(query);

        if (map == null) {
            return new ArrayList<>();
        }

        T2CoProjectValidator validator = new T2CoProjectValidator();

        validator.setProcType(validator.PROC_TYPE_IDENTIFICATION_SOURCE);
        List<ProjectIdentification> mainData = (List<ProjectIdentification>) map.get("mainData");
        validator.setAppendix("mainList", mainData);
        validator.setAppendix("subListMap", map.get("subData"));

        T2CoValidationResult vr = validator.validate();

        if (!vr.isValid()) {
            Map<String, String> validMap = vr.getValidMessageMap();
            result.addAll(autoFillOssInfoService.checkOssLicenseData(mainData, validMap, null));
        }

        if (!vr.isDiff()) {
            Map<String, String> diffMap = vr.getDiffMessageMap();
            result.addAll(autoFillOssInfoService.checkOssLicenseData(mainData, null, diffMap));
        }

        if (result.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> data = autoFillOssInfoService.checkOssLicense(result);

        List<SelfCheckVerifyLicensesDto.LicenseCheckResult> resultList = new ArrayList<>();
        var checkedLicenses = (List<ProjectIdentification>) data.get("checkedData");
        for (ProjectIdentification oss : checkedLicenses) {
            var license = SelfCheckVerifyLicensesDto.LicenseCheckResult.builder()
                    .gridId(oss.getComponentId())
                    .downloadUrl(oss.getDownloadLocation())
                    .after(SelfCheckVerifyLicensesDto.LicenseEntry.builder()
                            .value(Arrays.stream(oss.getLicenseName().split(","))
                                    .map(String::trim).collect(Collectors.toList()))
                            .msg(null)
                            .build())
                    .after(SelfCheckVerifyLicensesDto.LicenseEntry.builder()
                            .value(Arrays.stream(oss.getCheckLicense().split(","))
                                    .map(String::trim).collect(Collectors.toList()))
                            .msg(null)
                            .build())
                    .build();
            resultList.add(license);
        }

        return resultList;
    }
}