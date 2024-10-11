/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;

@Mapper
public interface ProjectMapper {
	int selectProjectTotalCount(Project project);

	List<Project> selectProjectList(Project project);
	
	void updateReviewer(Project project);
	
	void updateReject(Project project);
	
	List<String> selectCategoryCode(String code); 

	Project selectProjectMaster(String prjId);
	
	List<Project> selectModelList(String prjId);
	
	List<Project> selectWatchersList(Project project);

	void insertProjectMaster(Project project);
	
	void insertProjectWatcher(Project project);
	
	void insertProjectModel(Project project);
	
	void deleteProjectMaster(Project project);
	
	void deleteProjectModel(Project project);
	
	void deleteProjectWatcher(Project project);
	
	List<ProjectIdentification> getOssNames(ProjectIdentification identification);
	
	List<ProjectIdentification> getOssVersions(String ossName);
	
	ProjectIdentification getOssId(ProjectIdentification identification);

	List<ProjectIdentification> getOssFindByNameAndVersion(ProjectIdentification identification);
	
	List<ProjectIdentification> getOssFindByVersionAndDownloadLocation(ProjectIdentification identification);

	List<ProjectIdentification> getOssFindByDownloadLocation(ProjectIdentification identification);
	
	List<ProjectIdentification> getLicenses(ProjectIdentification identification);

	String getDivision(Project project);

	OssComponents selectOssComponents(String componentId);

	int registComponents(OssComponents param);

	String selectLastComponent();
	
	int selectExistLicense(OssComponentsLicense ossComponentsLicense);

	int registComponentLicense(OssComponentsLicense ossComponentsLicense);
	
	List<Project> getProjectNameList(Project project);
	
	List<Project> getProjectIdList(Project project);
	
	List<Project> getProjectModelNameList();
	
	List<OssComponents> selectComponentId(ProjectIdentification prj);

	void deleteOssComponentsLicense(OssComponents ossComponents);

	void deleteOssComponents(ProjectIdentification prj);

	OssMaster selectOssNickName(ProjectIdentification projectIdentification);

	void updateFileId(Project project);

	List<T2File> selectCsvFile(@Param("csvFileId") String csvFileId);
	
	void deleteFileBySeq(T2File file);

	List<T2File> selectAndroidCsvFile(Project project);

	List<T2File> selectAndroidNoticeFile(Project project);

	String selectLicenseComb(ProjectIdentification projectIdentification);

	void updateComment(Project result);

	void deleteComment(CommentsHistory commentsHistory);

	List<ProjectIdentification> selectBomList(ProjectIdentification projectIdentification);
	
	List<OssComponents> selectOssRefPrjList1(OssMaster ossMaster);
	
	List<Project> selectUnlimitedOssComponentBomList(Project project);

	List<OssComponentsLicense> selectBomLicense(ProjectIdentification projectIdentification);
	
	List<OssComponentsLicense> selectBomLicenseGrp(ProjectIdentification projectIdentification);
	
	int selectDuplicatedProject(Project project);		//프로젝트 리스트중 Name + Version이 중복될 경우 0 이상의 값 리턴

	List<Project> selectProjectListExcel(Project project);

	String selectLastPrjId();

	List<ProjectIdentification> selectOssComponentsList(Project project);

	List<OssComponentsLicense> selectOssComponentsLicenseList(ProjectIdentification projectIdentification);

	void insertOssComponents(ProjectIdentification projectIdentification);

	void insertOssComponentsLicense(OssComponentsLicense ossComponentsLicense);

	Project selectProjectMaster2(String prjId);

	void updateProjectMaster(Project project);

	void updateReadmeContent(Project project);

	void updateVerifyContents(Project project);
	
	List<ProjectIdentification> identificationSubGrid(ProjectIdentification identification);

	String getLicensesId(String licenseName);

	List<ProjectIdentification> selectIdentificationGridList(ProjectIdentification identification);
	
	ProjectIdentification selectOssComponentInfo(Map<String, String> paramMap);
	
	List<OssComponents> getPartnerOssList(OssComponents ossComponents);
	
	List<ProjectIdentification> getPartnerOssListValidation(OssComponents ossComponents);

	List<ProjectIdentification> getIdentificationProjectSearch(ProjectIdentification projectIdentification);

	void insertOssComponentsThirdParty(OssComponents ossComponents);

	void insertOssComponentsThirdProject(OssComponents ossComponents);

	List<OssComponentsLicense> selectThirdComponent(String prjId);

	Collection<? extends OssComponentsLicense> selectThirdComponent2(String string);

	void insertOssComponentsLicenseThird(OssComponentsLicense ossComponentsLicense);

	void updateExcludeYn(OssComponentsLicense ossComponentsLicense);

	List<Project> getProjectCreator();

	List<Project> getProjectReviwer();

	Project selectProjectDetailExcel(String parameter);

	List<ProjectIdentification> getProjectReportExcelList(ProjectIdentification identification);
	
	List<Project> getProjectVersionList(Project project);

	List<Project> getProjectDivisionList(Project project);
	
	void registPackageFileId(Project project);
	
	void updatePartnerOssList(OssComponents bean);

	void deleteOssComponentsLicenseWithIds(OssComponents bean);

	void deleteOssComponentsWithIds(OssComponents bean);
	
	void deleteOssComponentsWithIds2(OssComponents bean);
	
	void insertOssComponentsCopy(OssComponents bean);

	void insertOssComponentsLicenseCopy(OssComponents bean);

	void updateSrcOssList(ProjectIdentification projectIdentification);

	void insertSrcOssList(ProjectIdentification projectIdentification);

	void updateBom(OssComponents component);

	void updateProjectSubStatus(Project param);
	
	LicenseMaster selectLicenseMaster(LicenseMaster license);
	
	void updateComponentsOssId(Project project);
	
	void updateComponentsLicenseId(Project project);

	List<String> checkChangedIdentification(String prjId);

	void updateIdentificationConfirm(Project project);

	List<T2File> selectAndroidResultFile(Project project);

	List<Map<String, String>> getProjectDownloadExpandInfo(Project param);

	void registBomComponents(ProjectIdentification bean);

	void updateComponentsLicenseInfo(Project project);

	void deleteOssNotice(String prjId);
	
	void makeOssNotice(OssNotice noticeParam);

	Map<String, Object> getNoticeType(String prjId);

	void updateProjectStatusWithComplete(Project bean);

	List<ProjectIdentification> getOssInfoByName(ProjectIdentification projectIdentification);

	List<OssMaster> checkOssNickName(OssMaster bean);

	void updateFileBySeq(T2File file);

	List<OssComponents> selectOssComponentsListByComponentIds(OssComponents param);

	void updateAndroidNoticeFileInfoWithLoadFromProject(Project project);

	List<OssComponentsLicense> getComponentListForLicenseCheck(Project _ossidUpdateParam);

	void updateOssIdToNull(OssComponents _updateParam);

	List<T2File> selectFileInfoById(String fileId);

	void updateComponentsOssInfo(Project project);

	void updateComponentsCopyrightInfo(ProjectIdentification projectIdentification);

	void updateWithoutVerifyYn(OssNotice ossNotice);
	
	List<Project> selectWatchersCheck(Project project);

	List<Project> getWatcherListByEmail(String email);

	List<OssComponents> findBinAutoIdentificationWithBinaryText(String prjId);
	
	List<OssComponentsLicense> getOssComponentsLicenseListByComponentId(String componentId);

	List<OssComponents> findBinAutoIdentificationWithResultText(String prjId);

	void updateIdentifcationProgress(Project bean);

	int existsWatcherByEmail(Project project);

	void insertWatcher(Project project);

	int existsWatcherByUser(Project project);

	void removeWatcher(Project project);
	
	List<Project> copyWatcher(Project project);

	List<Project> selectDeleteModelList(String prjId);

	void updateModelSyncInfo(Project bean);

	void updateReleaseDateProjectModel(Project bean);

	void resetReleaseDateProjectModelFlag(String prjId);

	void deleteProjectModelWithModelName(Project project);

	void updateFilePath(OssComponents newBean);

	void updateDistributeTarget(Project project);

	String selectViewOnlyFlag(Project project);

	void updatePublicYn(Project project);

	List<String> getModelCategoryTemplateArray();
	
	List<String> getModelCategoryTemplateArraySKS();
	
	int existsWatcher(Project project);
	
	public List<Project> selectAddList(Project project);
	
	void insertAddList(Project project);
	
	void deleteAddList(Project project);
	
	int existsAddList(Project project);

	List<String> findIdentificationMaxNvdInfo(@Param("prjId")String prjId, @Param("commponentDiv")String commponentDiv);

	List<String> findIdentificationMaxNvdInfoForVendorProduct(@Param("prjId")String prjId, @Param("commponentDiv")String commponentDiv);
	
	int selectOssComponentMaxIdx(Project project);
	
	Project getProjectBasicInfo(Project project);
	
	List<Project> selectModelInfoList(Project project);
	
	void updateProjectAllowDownloadBitFlag(Project project);
	
	void updateProjectDistributionStatus(@Param("prjId") String prjId, @Param("destributionStatus") String destributionStatus);
	
	List<String> getDeleteOssComponentsLicenseIds(OssComponents bean);
	
	int selectReuseProjectTotalCount(Project project);
	
	List<Project> selectReuseProject(Project project);
	
	List<T2File> selectReusePackagingFileList(@Param("prjId") String prjId);
	
	int getOssAnalysisDataCnt(Project project);
	
	Project getOssAnalysisData(Project project);
	
	String getReviewerEmail(@Param("prjId") String prjId, @Param("loginUser") String loginUser);
	
	int selectProjectCount(Project project);

	void insertStatisticsMostUsedOssInfo(Project project);

	void insertStatisticsMostUsedLicenseInfo(Project project);

	void deleteStatisticsMostUsedInfo(Project project);
	
	int selectAdminCheckCnt(ProjectIdentification projectIdentification);
	
	List<ProjectIdentification> selectAdminCheckList(@Param("prjId") String prjId);
	
	List<Project> selectPartnerRefPrjList(PartnerMaster partner);
	
	void updateFileId2(Project project);

	void updateCopyConfirmStatusProjectStatus(Project project);

	void updateConfirmCopyVerificationDestributionStatus(Project project);

	void updateProjectDivision(Project project);
	
	void deleteProjectDistributeHis(Project project);
	
	List<OssComponentsLicense> selectBomLicenseList(ProjectIdentification identification);
	
	void deleteSecurityData(OssComponents ossComponents);
	
	void insertSecurityData(OssComponents ossComponents);

	List<OssComponents> getSecurityDataList(ProjectIdentification identification);

	int existsWatcherByUserDivistion(Project project);

	void updateWatcherDivision(Project project);

	void updateCveInfoForNotFixedOssInfo(OssMaster ossMaster);

	List<String> selectVulnInfoForIdentification(@Param("vendorProduct")String vendorProduct, @Param("version")String version);
	
	int getSecurityDataCntByProject(Project project);
	
	public List<OssComponents> selectOssComponentsSbomList(ProjectIdentification identification);

	public List<OssComponents> selectOssComponentsListClassAppend(ProjectIdentification identification);

	List<OssComponents> selectVulnerabilityResolutionSecurityList(Project project);

	int copySecurityDataForProjectCnt(Project project);

	void copySecurityDataForProject(Project project);
	
	List<OssComponents> checkSelectDownloadFile(Project project);
	
	List<ProjectIdentification> checkSelectDownloadFileForBOM(Project project);

	List<OssComponents> getDependenciesDataList(Project project);

	int checkProjectDistributeHis(Project project);

	Float getCvssScoreForNotFixed(String prjId);

	List<ProjectIdentification> selectSecurityListForProject(ProjectIdentification identification);
	
	void updateProjectForSecurity(Project project);

	List<String> selectProjectForSecurity();
	
	/**
	 * Delete Component and license row from OSS_COMPONENTS and OSS_COMPONENTS_LICENSE Table
	 * @param referenceId
	 * @param referenceDiv
	 * @return
	 */
	int resetOssComponentsAndLicense(@Param("referenceId")String referenceId, @Param("referenceDiv")String referenceDiv);
	int resetSecurityData(@Param("prjId")String prjId);
	
	void insertOssComponentListWithComponentId(@Param("list")List<ProjectIdentification> OssComponentList);
	void insertOssComponentList(@Param("list") List<ProjectIdentification> OssComponentList);
	void insertOssComponentLicenseList(@Param("list")List<OssComponentsLicense> ossComponentLicenseList);

	List<Map<String, Object>> getCpeInfoAndRangeForProject(ProjectIdentification identification);
}
