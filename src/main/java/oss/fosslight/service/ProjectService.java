/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.*;

public interface ProjectService extends HistoryConfig{
	public Map<String, Object> getProjectList(Project project);
	
	void updateReviewer(Project project);
	
	void updateReject(Project project);
	
	public String getCategoryCode(String code, String gubun);

	public Project getProjectDetail(Project project);

	public Map<String, List<Project>> getModelList(String prjId);
	
	public void registProject(Project project);
	
	public void deleteProject(Project project);
	
	public List<ProjectIdentification> getOssNames(ProjectIdentification identification);
	
	public List<ProjectIdentification> getOssVersions(String ossName);
	
	public Map<String, Object> getOssIdLicenses(ProjectIdentification identification);

	public String getDivision(Project project);

	public void registComponentsThird(String prjId, String identificationSubStatusPartner, List<OssComponents> ossComponents, List<PartnerMaster> thirdPartyList);
	
	public List<Map<String, String>> getProjectNameList(Project project);
	
	public List<Project> getProjectModelNameList();
	
	public Map<String, String> getCategoryCodeToJson(String code);		//카테고리 코드 Json형태를 위한 List로 가공
	
	public boolean existProjectData(Project project);

	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project);
	
	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project, String refDiv);
	
	public void registOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, String refId, String refDiv);
	
	public Map<String, List<String>> nickNameValid(String prjId, List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense);

	public void registBom(String prjId, String merge, List<ProjectIdentification> projectIdentification, List<ProjectIdentification> checkGridBomList);
	
	public void registBom(String prjId, String merge, List<ProjectIdentification> projectIdentification, List<ProjectIdentification> checkGridBomList, String copyPrjId, boolean isCopyConfirm, boolean isAndroid);
	
	public void registBom(String prjId, String merge, List<ProjectIdentification> projectIdentification, List<ProjectIdentification> checkGridBomList, String copyPrjId, boolean isCopyConfirm, boolean isAndroid, boolean isPartner);
	
	public void checkProjectReviewer(Project project);
	
	public Map<String, Object> updateProjectStatus(Project project, boolean isCopyConfirm, boolean isVerificationConfirm) throws Exception;
	
	public List<ProjectIdentification> getBomListExcel(ProjectIdentification bom);
	
	List<Project> getModelListExcel(Project project);

	void registReadmeContent(Project project);

	void registVerifyContents(Project project);

	Map<String, Object> getPartnerList(PartnerMaster partnerMaster);

	Map<String, Object> getIdentificationProject(Project project);

	void registComponentsBat(String prjId, String identificationSubStatusBat, List<ProjectIdentification> ossComponents,
			List<List<ProjectIdentification>> ossComponentsLicense, boolean prjYn);

	Map<String, Object> getIdentificationGridList(ProjectIdentification identification);
	
	Map<String, Object> getIdentificationGridList(ProjectIdentification identification, boolean multiUIFlag);

	Map<String, Object> getPartnerOssList(OssComponents ossComponents);

	Map<String, Object> getIdentificationProjectSearch(ProjectIdentification projectIdentification);

	String getReviewerList(String adminYn);
	
	String getAdminUserList();

	Map<String, Object> getIdentificationThird(OssComponents ossComponents);
	
	List<Project> getProjectVersionList(Project project);

	List<Project> getProjectDivisionList(Project project);

	void updateSubStatus(Project project);

	List<UploadFile> selectAndroidFileDetail(Project project);
	
	void updateProjectIdentificationConfirm(Project project, boolean isCopyConfirm, boolean isVerificationConfirm);
	
	public Map<String, Object> getOssIdCheck(ProjectIdentification projectIdentification);

	String checkChangedIdentification(String prjId, List<ProjectIdentification> partyData,
			List<ProjectIdentification> srcData, List<List<ProjectIdentification>> srcSubData,
			List<ProjectIdentification> batData, List<List<ProjectIdentification>> batSubData, String applicableParty, String applicableSrc, String applicableBat);

	Map<String, Object> applySrcAndroidModel(List<ProjectIdentification> list, List<String> noticeBinaryList) throws IOException;

	Map<String, Map<String, String>> getProjectDownloadExpandInfo(Project param);

	Project getProjectBasicInfo(String prjId);
	
	void cancelFileDel(Project project);

	List<OssComponents> selectOssComponentsListByComponentIds(OssComponents param);

	void updateProjectMaster(Project project);
	
	Map<String, Object> getFileInfo(ProjectIdentification identification);
	
	Map<String, Object> get3rdMapList(Project project);

	List<Project> getWatcherList(Project project);

	List<ProjectIdentification> convertOssNickName(List<ProjectIdentification> ossComponents);

	List<List<ProjectIdentification>> convertLicenseNickName(List<List<ProjectIdentification>> ossComponentsLicense);

	String addWatcher(Project project);

	void removeWatcher(Project project);

	List<Project> copyWatcher(Project project);
	
	boolean existsWatcher(Project project);

	String getPartnerFormatName(String partnerId, boolean onlyName);

	void updateIdentificationConfirmSkipPackaing(Project project);
	
	public void insertProjectModel(Project project);
	
	public void updatePublicYn(Project project);

	Map<String, Object> getProjectToAddList(OssComponents ossComponents);
	
	Map<String, Object> getAddList(Project project);
	
	boolean existsAddList(Project project);
	
	void insertAddList(List<Project> project);
	
	public OssNotice setCheckNotice(Project project);

	Map<String, Object> identificationSubGrid(ProjectIdentification identification);
	
	List<ProjectIdentification> setMergeGridData(List<ProjectIdentification> gridData);
	
	List<ProjectIdentification> setMergeGridDataByAndroid(List<ProjectIdentification> gridData);
	
	String checkValidData(Map<String, Object> map);
	
	String makeNoticeFileContents(Map<String, Object> paramMap);
	
	String makeSupplementFileId(String contents, Project project);
	
	String makeZipFileId(Map<String, Object>paramMap, Project project);

	public List<Map<String, String>> getBomCompare(List<ProjectIdentification> beforeBomList, List<ProjectIdentification> afterBomList, String flag) throws Exception;

	public void deleteStatisticsMostUsedInfo(Project project);
	
	public void addPartnerData(Project project);

	public void insertCopyConfirmStatusBomList(Project project, ProjectIdentification identification);

	public List<String> getPackageFileList(Project project, String filePath);

	public List<ProjectIdentification> selectIdentificationGridList(ProjectIdentification identification);

	public void updateCopyConfirmStatusProjectStatus(Project project);
	
	void copySrcAndroidNoticeFile(Project project);

	public Map<String, List<Project>> updateProjectDivision(Project project);

	public void updateComment(Project project);

	public String checkOssNicknameList(ProjectIdentification identification);

	public void updateFileId(Project project);

	public void deleteProjectRefFiles(Project projectInfo);

	public void deleteUploadFile(Project project);
	
	public Map<String, Object> getSecurityGridList(Project project);

	public void registSecurity(Project project, String tabName, List<OssComponents> ossComponents);

	public List<Project> getProjectIdList(Project project);
	
	public Map<String, Object> getExportDataForSBOMInfo(OssNotice ossNotice);

	public boolean checkReqEntrySecurity(Project project, String tabMenu);

	public void copySecurityDataForProject(Project project, Project bean);
	
	public Map<String, Object> checkSelectDownloadFile(Project project);

	public List<OssComponents> getDependenciesDataList(Project project);

	public void registDepOss(List<ProjectIdentification> ossComponents, List<List<ProjectIdentification>> ossComponentsLicense, Project project);

	public void registBinOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project);

	public void setNoticeFileFormat(Project project, List<String> noticeFileFormatList);

	public void updateSecurityDataForProject(String prjId);
	
	public void updatePreparedStatement(List<ProjectIdentification> updateOssComponentList, List<ProjectIdentification> insertOssComponentList, List<OssComponentsLicense> insertOssComponentLicenseList, List<String> deleteRows);
	
	public void deletePreparedStatement(List<OssComponents> componentIds);

	public void setLoadToList(Map<String, Object> map, String prjId);

	void updateOssComponentList(Project project, String refDiv, String refId, List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense);
	
	public Map<String, Object> changeProjectStatus(Project project);

	public void copyOssComponentList(Project project, boolean isBom);

	public Map<String, Object> getDependencyTreeList(List<ProjectIdentification> ossComponents);
}