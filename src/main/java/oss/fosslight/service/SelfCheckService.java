/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.BinaryMaster;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.domain.History;

public interface SelfCheckService extends HistoryConfig{
	String getUserList();

	public Map<String, Object> getProjectList(Project project);
	
	public String getCategoryCode(String code, String gubun);

	public Project getProjectDetail(Project project);
	
	public void registProject(Project project);
	
	public void deleteProject(Project project);
	
	public List<ProjectIdentification> getOssNames(ProjectIdentification identification);
	
	public List<ProjectIdentification> getOssVersions(String ossName);
	
	public Map<String, Object> getOssIdLicenses(ProjectIdentification identification);

	public String getDivision(Project project);
	
	public List<Project> getProjectNameList(Project project);
	
	public List<Map<String, Object>> getCategoryCodeToJson(String code);		//카테고리 코드 Json형태를 위한 List로 가공
	
	public boolean existProjectData(Project project);

	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project);
	
	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project, String refDiv);

	public Map<String, List<String>> nickNameValid(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense);

	public List<Project> getProjectListExcel(Project project);

	public Map<String, Object> identificationSubGrid(ProjectIdentification identification);

	Map<String, Object> getPartnerList(PartnerMaster partnerMaster);

	Map<String, Object> getIdentificationProject(Project project);

	Map<String, Object> getIdentificationGridList(ProjectIdentification identification);

	Map<String, Object> getPartnerOssList(OssComponents ossComponents);

	Map<String, Object> getIdentificationProjectSearch(ProjectIdentification projectIdentification);

	String getReviewerList();

	Map<String, Object> getIdentificationThird(OssComponents ossComponents);

	List<Project> getProjectCreator();

	List<Project> getProjectReviwer();

	Project selectProjectDetailExcel(String parameter);

	List<ProjectIdentification> getProjectReportExcelList(ProjectIdentification identification);
	
	List<Project> getProjectVersionList(Project project);

	void registPackageFile(List<UploadFile> list, String prjId);

	HashMap<String, Object> applySrcAndroidModel(Project project);

	void updateSubStatus(Project project);

	List<UploadFile> selectAndroidFileDetail(Project project);

	LicenseMaster getLicenseMaster(LicenseMaster license);
	
	public Map<String, Object> getOssIdCheck(ProjectIdentification projectIdentification);
	
	Map<String, Object> applySrcAndroidModel(List<ProjectIdentification> list, List<String> noticeBinaryList) throws IOException;

	Map<String, Map<String, String>> getProjectDownloadExpandInfo(Project param);

	boolean isPermissiveOnlyAndGeneralNotice(String prjId, boolean isAndroidModel);
	
	void cancelFileDel(Project project);

	List<OssComponents> selectOssComponentsListByComponentIds(OssComponents param);

	Map<String, Object> registBatWithFileUploadByProject(MultipartHttpServletRequest req, T2File file, BinaryMaster binary);

	void updateProjectMaster(Project project);
	
	Map<String, Object> getFileInfo(ProjectIdentification identification);

	Map<String, Object> registBatWithFileUploadByProjectByUrl(HttpServletRequest request, T2File file,
			BinaryMaster binary, Map<Object, Object> map);
	
	Map<String, Object> get3rdMapList(Project project);	
	
	Map<String, Object> getThirdPartyMap(String prjId);

	void updateWithoutVerifyYn(OssNotice ossNotice);

	List<Vulnerability> getAllVulnListWithProject(String prjId);
	
	void addWatcher(Project project);

	void removeWatcher(Project project);
	
	List<Project> copyWatcher(Project project);
	
	boolean existsWatcher(Project project);

	public OssNotice setCheckNotice(Project project);

	public OssNotice selectOssNoticeOne(String prjId);

	public List<OssComponents> getVerifyOssList(Project projectMaster);
	
	public Map<String, Object> getVerificationOne(Project project);

	public Project getProjectBasicInfo(String prjId);

	public boolean getNoticeHtmlFile(OssNotice ossNotice) throws IOException;

	public boolean getNoticeHtmlFile(OssNotice ossNotice, String contents) throws IOException;

	public String getNoticeHtml(OssNotice ossNotice) throws IOException;

	public void updateStatusWithConfirm(Project project, OssNotice ossNotice) throws Exception ;

	public void updateProjectStatus(Project project);

	public History work(Object param);

	public void changePackageFileNameDistributeFormat(String prjId);

	public String getNoticeTextFileForPreview(OssNotice ossNotice, boolean isConfirm) throws IOException;

	public Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice);

	public String getNoticeHtmlFileForPreview(OssNotice ossNotice) throws IOException;

	public String getNoticeTextFileForPreview(OssNotice ossNotice) throws IOException;
	
}
