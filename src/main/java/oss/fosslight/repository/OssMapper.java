/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.OssAnalysis;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.Vulnerability;

@Mapper
public interface OssMapper {
	int selectOssMasterTotalCount(OssMaster ossMaster);			
	
	List<OssMaster> selectOssList(OssMaster ossMaster); 	
	
	List<OssMaster> selectOssSubList(OssMaster ossMaster); 	
	
	List<OssLicense> selectOssLicenseList(OssMaster ossMaster);
	
	List<OssMaster> selectOssNameList();
	
	List<OssMaster> selectOssNicknameList(OssMaster ossMaster); 
	
	List<Vulnerability> getOssVulnerabilityList(Vulnerability vulnParam);
	
	OssMaster selectOssOne(OssMaster ossMaster); 	
	
	List<OssMaster> selectOssNicknameListWithoutOwn(OssMaster ossMaster); 
	
	List<OssMaster> selectOssDownloadLocationList(OssMaster ossMaster);
	
	List<OssMaster> selectOssDetectedLicenseList(OssMaster ossMaster);
	
	int selectOssPopupTotalCount(OssMaster ossMaster);			
	
	List<OssMaster> selectOssPopupList(OssMaster ossMaster);
	
	List<OssMaster> getBasicOssInfoListById(OssMaster ossMaster);
	
	int checkExistOssConfProject(OssMaster ossMaster);
	
	int checkExistOssConfPartner(OssMaster ossMaster);
		
	void insertOssMaster(OssMaster ossMaster);					
	
	int insertOssNickname(OssMaster ossMaster);					
	
	int mergeOssNickname(OssMaster ossMaster);					
	
	int insertOssLicenseDeclared(OssMaster ossMaster);
	
	int insertOssLicenseDetected(OssMaster ossMaster);
	
	int updateOssForProject(OssMaster ossMaster);
	
	int mergeOssName(OssMaster ossMaster);
	
	List<OssMaster> selectOssList2(OssMaster ossMaster); 				
	
	int deleteOssMaster(OssMaster ossMaster);				
	
	int deleteOssNickname(OssMaster ossMaster);					
	
	int deleteOssLicense(OssMaster ossMaster);
	
	List<OssMaster> selectOssListExcel(OssMaster ossMaster);
	
	List<OssMaster> getBasicOssInfoList(OssMaster ossMaster);	
	
	Map<String, Object> selectOssNameMap(OssMaster ossMaster);
	
	OssMaster checkExistsOss(OssMaster param);
	
	OssMaster licenseChecker(OssMaster ossMaster);
	
	OssMaster checkExistsOssNickname(OssMaster param);
	
	OssMaster checkExistsOssname(OssMaster param);
	
	List<Project> getOssChangeForUserList(OssMaster param);
	
	void deleteComponentLicenseByChangeOss(String componentId);
	
	void insertComponentLicenseByChangeOss(OssComponentsLicense bean);
	
	void deleteComponentLicenseIgnoreFirstByChangeOss(OssComponentsLicense deleteParam);
	
	void updateComponentLicenseByOssChange(OssComponentsLicense updateParam);
	
	List<OssMaster> getOssListByName(OssMaster bean);
	
	int checkExistsOssByname(OssMaster bean);
	
	void deleteOssLicenseFlag(String ossId);
	
	void updateOssLicenseFlag(OssMaster updateParam);
	
	void updateOssLicenseVDiffFlag(OssMaster updateParam);
	
	List<OssMaster> apiSelectOssIdList();
	
	OssMaster getLastModifiedOssInfoByName(OssMaster bean);
	
	List<OssMaster> apiGetOssCopyTargetList(OssMaster param);
	
	void apiCopyOssMaster(OssMaster bean);
	
	void apiCopyOssLicense(OssMaster bean);
	
	void apiCopyOssLicenseFlag(OssMaster bean);
	
	int checkHasAnotherVersion(OssMaster ossMaster);
	
	OssMaster getOssBasicInfoForMailContents(String ossId);
	
	List<String> checkNickNameRegOss(String ossName);
	
	void updateLicenseTypeAndObligation(OssMaster ossBean);
	
	OssMaster checkExistsOssNickname2(OssMaster param);
	
	List<OssMaster> getOssInfoAll();
	
	List<OssMaster> getOssInfoAllWithNick();
	
	OssMaster getNvdDataByOssName(OssMaster nvdParam);
	
	void updateNvdData(OssMaster ossMaster);
	
	void changeOssNameByDelete(OssMaster bean);
	
	void mergeOssNickname2(OssMaster nickMergeParam);
	
	int getOssVersionCountByName(String ossName);
	
	String checkExistOssConfByName(String ossName);
	
	List<OssMaster> getBasicOssListByName(String ossName);
	
	List<OssMaster> checkExistsOssDownloadLocation(OssMaster ossMaster);
	
	List<OssMaster> checkExistsOssHomepage(OssMaster ossMaster);
	
	List<OssMaster> getOssAllNickNameList();
	
	List<OssMaster> checkExistsOssDownloadLocationWithOssName(OssMaster param);
	
	List<OssMaster> checkExistsOssHomepageWithOssName(OssMaster param);
	
	OssMaster getNvdDataByOssNameWithoutVer(OssMaster nvdParam);
	
	int existsOssDownloadLocation(OssMaster ossMaster);
	
	void deleteOssDownloadLocation(OssMaster ossMaster);
	
	void insertOssDownloadLocation(OssMaster ossMaster);
	
	String checkOssName(ProjectIdentification bean);
	
	List<OssMaster> checkOssNameUrl(ProjectIdentification bean);
	
	List<OssMaster> checkOssNameUrl2(ProjectIdentification bean);
	
	int updateOssCheckNameBySelfCheck(ProjectIdentification bean);

	int updateOssCheckLicenseBySelfCheck(ProjectIdentification bean);

	int updateOssCheckNameByPartner(ProjectIdentification bean);

	int updateOssCheckLicenseByPartner(ProjectIdentification bean);

	int updateOssCheckName(ProjectIdentification bean);

	int updateOssCheckLicense(ProjectIdentification bean);

	int checkOssNameCnt(ProjectIdentification bean);
	
	int checkOssNameUrlCnt(ProjectIdentification bean);
	
	int checkOssNameUrl2Cnt(ProjectIdentification bean);

	int ossAnalysisListCnt(@Param("prjId") String prjId, @Param("startAnalysisFlag") String startAnalysisFlag, @Param("csvComponentIdList") int[] csvComponentIdList);
	
	void deleteOssAnalysisList(@Param("prjId") String prjId);
	
	void deleteOssAnalysis(@Param("prjId") String prjId);
	
	int insertOssAnalysisList(OssMaster bean);
	
	int updateOssAnalysisList(OssMaster bean);
	
	List<OssAnalysis> selectOssAnalysisList(OssMaster bean);
	
	void setOssAnalysisStatus(OssMaster bean);

	String getOssAnalysisStatus(@Param("prjId") String prjId);
	
	OssAnalysis getNewestOssInfo(OssAnalysis bean);

	OssAnalysis getNewestOssInfo2(OssAnalysis bean);
	
	List<OssMaster> getNewestOssInfoByOssMaster(OssMaster bean);
	
	int updateAnalysisComplete(OssAnalysis bean);
	
	int getAnalysisListPage(@Param("rows") int rows, @Param("prjId") String prjId);
	
	OssAnalysis getAutoAnalysisSuccessOssInfo(@Param("referenceOssId") String referenceOssId);
	
	void setDeactivateFlag(OssMaster ossMaster);

	List<PartnerMaster> getOssNameMergePartnerList(OssMaster ossMaster);

	List<Project> getOssNameMergeProjectList(OssMaster ossMaster);

	void updateOssMasterSync(OssMaster ossMaster);

	void deleteOssLicenseDeclaredSync(OssMaster ossMaster);

	void deleteOssLicenseDetectedSync(OssMaster ossMaster);

	int updateOssComponents(OssMaster ossMaster);

	OssMaster getSaveSesstionOssInfoByName(OssMaster ossMaster);

	List<OssComponents> getConfirmOssComponentsList(OssMaster ossMaster);

	List<Vulnerability> getOssVulnerabilityList2(OssMaster ossMaster);

	List<String> selectMultiOssList(OssMaster ossMaster);

	List<String> getDeactivateOssList();

	List<OssMaster> checkOssNameTotal(ProjectIdentification bean);

	Map<String, Object>  getRecentlyModifiedOss(OssMaster ossMaster);

	List<String> selectVulnInfoForOss(OssMaster ossMaster);

	List<String> checkExistsVendorProductMatchOss(OssMaster ossMaster);

	int checkOssVersionDiff(String ossName);
}
