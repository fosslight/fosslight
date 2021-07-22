/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApiProjectMapper {
	int selectProjectCount(Map<String, Object> paramMap);
	
	Map<String, Object> selectVerificationCheck(@Param("prjId") String prjId);
	
	int updatePackageFile(Map<String, Object> paramMap);
	
	List<Map<String, Object>> selectProject(Map<String, Object> paramMap);

	int selectProjectTotalCount(Map<String, Object> paramMap);
	
	String findIdentificationMaxNvdInfo(String prjId);
	
	List<Map<String, Object>> selectModelList(String prjId);
	
	int getCreateProjectCnt(@Param("userId") String userId);
	
	int checkProject(Map<String, Object> param);
	
	int createProject(Map<String, Object> param);
	
	int makeOssNotice(Map<String, Object> param);
	
	List<Map<String, Object>> selectBomList(Map<String, Object> paramMap);
	
	List<Map<String, Object>> selectBomLicense(@Param("componentId") String componentId);
	
	int checkDistributionType(Map<String, Object> paramMap);
}
