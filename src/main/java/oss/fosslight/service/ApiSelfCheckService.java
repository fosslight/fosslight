/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import oss.fosslight.api.dto.ListSelfCheckDto;
import oss.fosslight.api.dto.ListSelfCheckOssDto;
import oss.fosslight.domain.ProjectIdentification;

@Service
public interface ApiSelfCheckService {
	int getCreateProjectCnt(String userId);
	
	Map<String, Object> createSelfCheck(Map<String, Object> paramMap);
	
	boolean existProjectCnt(Map<String, Object> paramMap);

	Map<String, Object> selectProjectMaster(String prjId);

	void getIdentificationGridList(String prjId, String code, List<ProjectIdentification> ossComponentList, List<List<ProjectIdentification>> ossComponentsLicenseList);

	boolean existsWatcherByEmail(String prjId, String email);

	void insertWatcher(Map<String, Object> paramMap);

	ListSelfCheckDto.Result listSelfChecks(ListSelfCheckDto.Request request);

	ListSelfCheckOssDto.Result listSelfCheckOss(String request);
}
