/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.Vulnerability;


@Mapper
public interface SelfCheckMapper {
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

	void updateFileId(Project project);

	List<T2File> selectCsvFile(Project project);

	void deleteFileBySeq(T2File file);

	List<T2File> selectAndroidCsvFile(Project project);

	List<T2File> selectAndroidNoticeFile(Project project);

	String selectLicenseComb(ProjectIdentification projectIdentification);

	void updateComment(CommentsHistory result);

	void deleteComment(CommentsHistory commentsHistory);
	
	List<OssComponents> selectOssRefPrjList(OssMaster ossMaster);
	
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

	void insertOssComponentsCopy(OssComponents bean);

	void insertOssComponentsLicenseCopy(OssComponents bean);

	void updateSrcOssList(ProjectIdentification projectIdentification);

	void insertSrcOssList(ProjectIdentification projectIdentification);

	void updateBom(OssComponents component);

	void updateProjectSubStatus(Project param);
	
	LicenseMaster selectLicenseMaster(LicenseMaster license);
	
	void updateComponentsOssId(Project project);
	
	void updateComponentsLicenseId(Project project);

	void updateIdentificationConfirm(Project project);

	List<T2File> selectAndroidResultFile(Project project);

	List<Map<String, String>> getProjectDownloadExpandInfo(Project param);

	void registBomComponents(ProjectIdentification bean);

	void makeOssNotice(OssNotice noticeParam);

	String getNoticeType(String prjId);

	void updateProjectStatusWithComplete(Project bean);

	void deleteOssComponentsLicenseWithReferenceDiv(ProjectIdentification delParam);

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
	
	String selectComponentIdx(ProjectIdentification prj);

	List<Vulnerability> getAllVulnListWithProject(Project project);

	List<Vulnerability> getAllVulnListWithProjectByNickName(Project project);
	
	int existsWatcherByEmail(Project project);

	void insertWatcher(Project project);

	int existsWatcherByUser(Project project);

	void removeWatcher(Project project);
	
	List<Project> copyWatcher(Project project);
	
	Project getMaxVulnByOssName(Project vnlnUpdBean);

	List<Vulnerability> getAllVulnListWithProjectEmptyVersion(Project param);

	List<Vulnerability> getAllVulnListWithProjectByNickNameEmptyVersion(Project param);
	
	int existsWatcher(Project project);
	
	List<Project> getSelfCheckList(Project project);
	
	List<String> getAllVulnList(Project project);

	OssNotice selectOssNoticeOne(Project project);

	List<OssComponents> selectVerifyOssList(Project project);

	Map<String, Object> getNoticeTypeReturnMap(String prjId);

	void updateNoticeFileInfo(Project projectParam);

	List<OssComponents> selectVerificationNotice(OssNotice ossNotice);

	List<OssComponents> selectVerificationNoticeClassAppend(OssNotice ossNotice);

	Project getProjectBasicInfo(Project project);
	
	void updateNoticeFileInfoEtc(Project project);

	int insertOssNotice(OssNotice ossNotice);
	
	int updateOssNotice(OssNotice ossNotice);

	List<Vulnerability> getAllVulnListWithProject2(Project param);
	
	List<Vulnerability> getAllVulnListWithProjectByNickName2(Project project);
	
	List<Vulnerability> getAllVulnListWithProject3(Project param);
	
	List<Vulnerability> getAllVulnListWithProjectByNickName3(Project project);
}
