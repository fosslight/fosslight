/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import oss.fosslight.api.dto.*;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;

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

	List<SelfCheckOssDto> listSelfCheckOss(String request);

	GetSelfCheckDetailsDto.Result getSelfCheck(String request);
	List<T2File> listSelfCheckPackages(String id);

	List<SelfCheckVerifyOssDto.OssCheckResult> validateOss(String id);

	List<SelfCheckVerifyLicensesDto.LicenseCheckResult> validateLicenses(String id);
}
