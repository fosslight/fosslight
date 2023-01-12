/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.History;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.Vulnerability;

public interface SelfCheckService extends HistoryConfig{

	public Map<String, Object> getProjectList(Project project);

	public Project getProjectDetail(Project project);
	
	public void registProject(Project project);
	
	public void deleteProject(Project project);

	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project);
	
	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project, String refDiv);

	Map<String, Object> getIdentificationGridList(ProjectIdentification identification);

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

	public History work(Object param);

	public String getNoticeTextFileForPreview(OssNotice ossNotice, boolean isConfirm) throws IOException;
	
	public void registOssNotice(OssNotice ossNotice) throws Exception;
	
	public List<OssComponents> setMergeGridData(List<OssComponents> gridData);

	public Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice);

	public String getNoticeHtmlFileForPreview(OssNotice ossNotice) throws IOException;

	public String getNoticeTextFileForPreview(OssNotice ossNotice) throws IOException;

	public boolean existProjectData(Project project);
	
}
