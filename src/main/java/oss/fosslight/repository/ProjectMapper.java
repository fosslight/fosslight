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

	Project selectProjectMaster(Project project);
	
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
	
	List<Project> getProjectModelNameList();
	
	List<OssComponents> selectComponentId(ProjectIdentification prj);

	void deleteOssComponentsLicense(OssComponents ossComponents);

	void deleteOssComponents(ProjectIdentification prj);

	OssMaster selectOssNickName(ProjectIdentification projectIdentification);

	void updateFileId(Project project);

	List<T2File> selectCsvFile(Project project);

	void deleteFileBySeq(T2File file);

	List<T2File> selectAndroidCsvFile(Project project);

	List<T2File> selectAndroidNoticeFile(Project project);

	String selectLicenseComb(ProjectIdentification projectIdentification);

	void updateComment(CommentsHistory result);

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

	Project selectProjectMaster2(Project project);

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

	OssMaster findIdentificationMaxNvdInfo(@Param("prjId")String prjId, @Param("commponentDiv")String commponentDiv);

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
	
	List<Project> selectPartnerRefPrjList(PartnerMaster partner);
	
	void updateFileId2(Project project);

	void updateCopyConfirmStatusProjectStatus(Project project);

	void updateConfirmCopyVerificationDestributionStatus(Project project);
}
