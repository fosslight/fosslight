/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.repository.ApiSelfCheckMapper;
import oss.fosslight.service.ApiSelfCheckService;
import oss.fosslight.service.SelfCheckService;

@Service
public class ApiSelfCheckServiceImpl implements ApiSelfCheckService {
	@Autowired SelfCheckService selfCheckService;
	@Autowired ApiSelfCheckMapper apiSelfcheckMapper;
	
	@Override
	public int getCreateProjectCnt(String userId) {
		return apiSelfcheckMapper.getCreateProjectCnt(userId);
	}
	
	@Transactional
	@Override
	public Map<String, Object> createSelfCheck(Map<String, Object> paramMap){
		Map<String, Object> result = new HashMap<String, Object>();
		
		apiSelfcheckMapper.createSelfCheck(paramMap);
		
		Long prjId = (Long) paramMap.get("prjId");
		result.put("prjId", prjId.toString());
		
		return result;
	}
	
	@Override
	public boolean existProjectCnt(Map<String, Object> paramMap) {
		int records = apiSelfcheckMapper.selectProjectCount(paramMap);
		
		return records == 1 ? true : false;
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
		return apiSelfcheckMapper.existsWatcherByEmail(prjId, email) > 0 ? false : true;
	}

	@Override
	public void insertWatcher(Map<String, Object> paramMap) {
		apiSelfcheckMapper.insertWatcher(paramMap);
	}
}