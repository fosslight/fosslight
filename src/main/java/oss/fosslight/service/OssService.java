/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.*;

public interface OssService extends HistoryConfig{
	String registOssMaster(OssMaster ossMaster);
	
	int deleteOssMaster(OssMaster ossMaster);
	
	Map<String,Object> getOssMasterList(OssMaster ossMaster);
	
	Map<String,Object> getOssLicenseList(OssMaster ossMaster);
	
	List<OssMaster> getOssNameList();
	
	OssMaster getOssMasterOne(OssMaster ossMaster);
	
	Map<String, Object> getOssPopupList(OssMaster ossMaster);
	
	Map<String, Object> ossMergeCheckList(OssMaster ossMaster);
	
	Map<String, OssMaster> getBasicOssInfoList (OssMaster ossMaster);
	
	Map<String, OssMaster> getBasicOssInfoList (OssMaster ossMaster, boolean useUpperKey);
	
	OssMaster checkExistsOss(OssMaster param);
	
	OssMaster checkExistsOssNickname(OssMaster param);
	
	OssMaster checkExistsOssNickname2(OssMaster param);
	
	Map<String, OssMaster> getBasicOssInfoListById(OssMaster _param);
	
	String checkExistOssConf(String ossId);
	
	OssMaster getOssInfo(String ossId, boolean isMailFormat);
	
	OssMaster getOssInfo(String ossId, String ossName, boolean isMailFormat);
	
	List<Vulnerability> getOssVulnerabilityList(Vulnerability vulnParam);
	
	List<OssMaster> getOssListByName(OssMaster bean);
	
	String[] getOssNickNameListByOssName(String ossName);
	
	void updateLicenseDivDetail(OssMaster bean);
	
	OssMaster getLastModifiedOssInfoByName(OssMaster bean);
	
	String checkVdiff(Map<String, Object> reqMap);
	
	String[] checkNickNameRegOss(String ossName, String[] ossNicknames);
	
	void checkOssLicenseAndObligation(OssMaster ossBean);
	
	void updateLicenseTypeAndObligation(OssMaster ossBean);
	
	void deleteOssWithVersionMerege(OssMaster ossMaster)  throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException ;
	
	Map<String, Object> checkExistsOssDownloadLocation(OssMaster ossMaster);
	
	Map<String, Object> checkExistsOssHomepage(OssMaster ossMaster);
	
	Map<String, Object> checkExistsOssDownloadLocationWithOssName(OssMaster param);
	
	Map<String, Object> checkExistsOssHomepageWithOssName(OssMaster param);
	
	void registOssDownloadLocation(OssMaster ossMaster);
	
	int checkExistsOssByname(OssMaster bean);
	
	List<ProjectIdentification> checkOssName(List<ProjectIdentification> list);
	
	Map<String, Object> saveOssCheckName(ProjectIdentification paramBean, String targetName);
	
	Map<String, Object> saveOssNickname(ProjectIdentification paramBean);
	
	Map<String, Object> saveOssAnalysisList(OssMaster ossBean, String key);
	
	Map<String, Object> getOssAnalysisList(OssMaster ossBean);
	
	int getAnalysisListPage(int rows, String prjId);
	
	Map<String, Object> startAnalysis(String prjId, String fileId);
	
	OssAnalysis getNewestOssInfo(OssAnalysis bean);
	
	Map<String, Object> updateAnalysisComplete(OssAnalysis bean) throws Exception;
	
	OssAnalysis getAutoAnalysisSuccessOssInfo(String referenceOssId);
	
	List<ProjectIdentification> checkOssNameData(List<ProjectIdentification> componentData, Map<String, String> validMap, Map<String, String> diffMap);
	
	List<OssMaster> getOssListBySync(OssMaster bean);

	List<String> getOssListSyncCheck(List<OssMaster> selectOssList, List<OssMaster> standardOssList);

	void syncOssMaster(OssMaster syncBean, boolean declaredLicenseCheckFlag, boolean detectedLicenseCheckFlag, boolean downloadLocationCheckFlag);

	OssMaster makeEmailSendFormat(OssMaster beforeBean);

	String checkOssVersionDiff(OssMaster ossMaster);

	Map<String, List<OssMaster>> updateOssNameVersionDiff(OssMaster ossMaster);

	OssMaster getSaveSesstionOssInfoByName(OssMaster ossMaster);
	
	List<Vulnerability> getOssVulnerabilityList2(OssMaster ossMaster);
}
