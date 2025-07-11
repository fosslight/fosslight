/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;

@Service
public interface ApiProjectService {
	public Map<String, Object> selectProjectList(Map<String, Object> paramMap);

	public Map<String, Object> selectProjectList_V1(Map<String, Object> paramMap);

	public boolean checkUserHasProject(T2Users userInfo, String prjId);

	public boolean checkUserAvailableToEditProject(T2Users userInfo, String prjId);

	public boolean checkProjectAvailability(T2Users userInfo, String prjId, String needToUploadReport);

	public boolean existProjectCnt(Map<String, Object> paramMap);

	public Map<String, Object> getSheetData(UploadFile ufile, String prjId, String readType, String[] sheet);
	
	public Map<String, Object> getSheetData(UploadFile ufile, String prjId, String readType, String[] sheet, boolean exactMatchFlag);
	
	public Map<String, Object> readAndroidBuildImage(UploadFile ossReportBean, UploadFile noticeHtmlBean, UploadFile resultTxtBean);
	
	public Map<String, Object> selectVerificationCheck(String prjId);
	
	public boolean updatePackageFile(Map<String, Object> paramMap);
	
	public int getCreateProjectCnt(String prjId);
	
	Map<String, Object> createProject(Map<String, Object> paramMap);
	
	public int makeOssNotice(Map<String, Object> paramMap);
	
	public List<Map<String, Object>> getBomList(String prjId);
	
	public List<Map<String, Object>> setMergeGridData(List<Map<String, Object>> list);

	public Map<String, Object> getBomExportJson(String prjId);
	
	public Map<String, Object> getBomCompare(List<Map<String, Object>> beforeBomList, List<Map<String, Object>> afterBomList);
	
	public boolean checkDistributionType(Map<String, Object> paramMap);
	
	public Map<String, Object> selectModelList(Map<String, Object> paramMap);

	public List<Map<String, Object>> getVerifyOssList(Map<String, Object> project);

	public List<Map<String, Object>> serMergeGridData(List<Map<String, Object>> list);

	public String setClearFiles(Map<String, Object> map);

	public boolean getChangedPackageFile(String prjId, List<String> fileSeqs);

	public Map<String, Object> processVerification(Map<String, Object> map, Map<String, Object> file, Map<String, Object> project);

	public void updateVerifyFileCount(ArrayList<String> arrayList);

	public void updateVerifyFileCount(HashMap<String, Object> hashMap);

	public List<String> getPackageFileList(String prjId);

	public void registBom(String prjId, String string);

	public int existProjectCntBomCompare(Map<String, Object> paramMap);

	public boolean existLdapUserToEmail(String email);

	public boolean existsWatcherByEmail(String prjId, String email);

	public void insertWatcher(Map<String, Object> paramMap);

	public Map<String, Object> selectProjectMaster(String prjId);

	public void getIdentificationGridList(String prjId, String code, List<ProjectIdentification> ossComponentList, List<List<ProjectIdentification>> ossComponentsLicenseList, List<Map<String, Object>> gridDataList);

	public void updateSubStatus(Map<String, Object> param);
	
	public Map<String, Object> getProjectBasicInfo(String prjId);

	public Map<String, Object> getProcessSheetData(Map<String, Object> result, String prjId, String resetFlag, String registFileId, String userId, String comment, String tabGubn, String sheetName, boolean sheetNamesEmptyFlag, boolean loopFlag, int sheetIdx);

	public Map<String, Object> registProjectOssComponent(Map<String, Object> param, String referenceDiv);

	public void processResetTab(String tabName, Project project, List<ProjectIdentification> ossComponents, List<List<ProjectIdentification>> ossComponentsLicense);
}
