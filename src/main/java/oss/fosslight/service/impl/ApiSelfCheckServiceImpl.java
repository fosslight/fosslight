/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service.impl;

import java.util.*;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.api.dto.ListSelfCheckDto;
import oss.fosslight.api.dto.SelfCheckDto;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.repository.ApiSelfCheckMapper;
import oss.fosslight.repository.SelfCheckMapper;
import oss.fosslight.service.ApiSelfCheckService;
import oss.fosslight.service.SelfCheckService;

@Service
public class ApiSelfCheckServiceImpl implements ApiSelfCheckService {
    @Autowired
    SelfCheckService selfCheckService;

    @Autowired
    ApiSelfCheckMapper apiSelfcheckMapper;

    @Autowired
    SelfCheckMapper selfcheckMapper;

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

            selfCheck.setPackages(Arrays.asList("a", "b")); // TODO
            selfCheck.setReport("rep"); // TODO
            selfCheck.setNotice("not"); // TODO
        }

        return ListSelfCheckDto.Result.builder()
                .list(results)
                .totalCount(totalCount)
                .build();
    }
}