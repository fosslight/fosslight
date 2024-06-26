/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;

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
	
	List<HashMap<String, Object>> selectBomLicense(@Param("componentId") String componentId);
	
	int checkDistributionType(Map<String, Object> paramMap);

	List<Map<String, Object>> selectVerifyOssList(Map<String, Object> project);

	Map<String, Object> selectVerificationFile(String fileSeq);

	void updatePackagingReuseMap(Map<String, Object> prjParam);

	int checkPackagingFileId(@Param("prjId") String prjId, @Param("packageFileId") String packageFileId, @Param("packageFileId2") String packageFileId2, @Param("packageFileId3") String packageFileId3);

	void updateVerifyFileCount(Map<String, Object> param);

	void updateVerifyFilePath(Map<String, Object> param);

	void updatePackageFile2(Map<String, Object> prjParam);

	Map<String, Object> selectPackageFileList(@Param("prjId") String prjId);

	List<String> selectComponentId(Map<String, Object> paramMap);

	void deleteOssComponentsLicense(String string);

	void deleteOssComponents(Map<String, Object> paramMap);

	void registBomComponents(Map<String, Object> bean);

	void registComponentLicense(Map<String, Object> licenseBean);

	Map<String, Object> selectProjectMaster2(Map<String, Object> _tempPrjInfo);

	List<Project> getProjectInfo(Project project);
	
	List<ProjectIdentification> selectOssComponentsList(Project project);

	void updateIdentifcationProgress(Map<String, Object> _tempPrjInfo);

	List<HashMap<String, Object>> getLicenseInfoInit();

	List<HashMap<String, Object>> getLicenseInfoInitNick();

	List<HashMap<String, Object>> selectMergeBomList(Map<String, Object> paramMap);

	List<HashMap<String, Object>> getRoleOutLicense();

	Map<String, Object> selectProjectMaster(Map<String, Object> prjParam);

	void updateReadmeContent(Map<String, Object> project);

	void updateVerifyContents(Map<String, Object> project);

	int existsWatcherByEmail(@Param("prjId") String prjId, @Param("email") String email);

	void insertWatcher(Map<String, Object> paramMap);

	void updateProjectSubStatus(Map<String, Object> param);

}
