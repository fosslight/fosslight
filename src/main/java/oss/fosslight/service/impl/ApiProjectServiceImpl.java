/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jsonldjava.shaded.com.google.common.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.ShellCommander;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.ApiFileMapper;
import oss.fosslight.repository.ApiOssMapper;
import oss.fosslight.repository.ApiProjectMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.ApiProjectService;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.ApiVulnerabilityService;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.util.YamlUtil;

@Service
@Slf4j
public class ApiProjectServiceImpl extends CoTopComponent implements ApiProjectService {
	@Autowired ApiProjectMapper apiProjectMapper;
	@Autowired ProjectMapper projectMapper;
	@Autowired ApiFileMapper apiFileMapper;
	@Autowired FileService fileService;
	@Autowired ProjectService projectService;
	@Autowired CommentService commentService;
	@Autowired ApiOssMapper apiOssMapper;
	@Autowired ApiVulnerabilityService apiVulnerabilityService;
	
	HashMap<String, HashMap<String, Object>> OSS_INFO_UPPER = new HashMap<>();
	HashMap<String, HashMap<String, Object>> OSS_INFO_BY_ID = new HashMap<>();
	HashMap<String, HashMap<String, Object>> LICENSE_INFO = new HashMap<>();
	HashMap<String, HashMap<String, Object>> LICENSE_INFO_UPPER = new HashMap<>();
	HashMap<String, HashMap<String, Object>> LICENSE_INFO_BY_ID = new HashMap<>();
	
	@Override
	public Map<String, Object> selectProjectList(Map<String, Object> paramMap){
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		int projectCnt = apiProjectMapper.selectProjectTotalCount(paramMap);
		
		if(projectCnt > 0) {
			list = apiProjectMapper.selectProject(paramMap);
			
			for(Map<String, Object> map : list) {
				String prjId = (String) map.get("prjId").toString();
				String status = (String) map.get("status");
				String distributionStatus = (String) map.get("distributionStatus");
				distributionStatus = CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROCESS.equals(distributionStatus) 
										? CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROGRESS : distributionStatus;
				String nvdMaxScore = apiProjectMapper.findIdentificationMaxNvdInfo(prjId);
				
				map.put("DISTRIBUTION_TYPE", CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, (String) map.get("distributionType")));
				map.put("NETWORK_SERVICE", (String) map.get("networkService"));
				map.put("NOTICE", CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, (String) map.get("notice")));
				map.put("NOTICE_PLATFORM", CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, (String) map.get("noticePlatform")));
				map.put("PRIORITY", CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, (String) map.get("priority")));
				map.put("STATUS",CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_STATUS, status));
				map.put("IDENTIFICATION_STATUS", CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, (String) map.get("identificationStatus")));
				map.put("VERIFICATION_STATUS", CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, (String) map.get("verificationStatus")));
				map.put("DISTRIBUTION_STATUS", CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_STATUS, distributionStatus));					
				map.put("VULNERABILITY_SCORE", nvdMaxScore);
//				map.put("MODEL_LIST", apiProjectMapper.selectModelList(prjId));
			}
		}
		
		result.put("content", list);
		result.put("record", projectCnt);
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean existProjectCnt(Map<String, Object> paramMap) {
		String ossReportFlag = (String) paramMap.get("ossReportFlag");
		List<String> prjIdList = (List<String>) paramMap.get("prjId");
		
		if(prjIdList != null) {
			if(isEmpty(ossReportFlag)) {
				ossReportFlag = CoConstDef.FLAG_NO;
				
				paramMap.put("ossReportFlag", ossReportFlag);
			}
			
			int records = apiProjectMapper.selectProjectCount(paramMap);
			
			return records == prjIdList.size() ? true : false;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkDistributionType(Map<String, Object> paramMap) {
		List<String> prjIdList = (List<String>) paramMap.get("prjId");
		
		int records = apiProjectMapper.checkDistributionType(paramMap);
		
		return records == prjIdList.size() ? true : false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getSheetData(UploadFile ufile, String prjId, String readType, String[] sheet) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String errMsg = "";
		List<OssComponents> reportData = new ArrayList<OssComponents>();
		List<String> errMsgList = new ArrayList<>();
		
		try {
			if (!ExcelUtil.readReport(readType, true, sheet, ufile.getRegistSeq(), reportData, errMsgList)) {
				for(String s : errMsgList) { // error 처리
					if(isEmpty(s)) {
						continue;
					}
					
					if(!isEmpty(errMsg)) {
						errMsg += "<br/>";
					}
					
					errMsg += s;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			errMsg = e.getMessage();
		}
		
		for(String s : errMsgList) {
			if(isEmpty(s)) {
				continue;
			}
			
			if(!isEmpty(errMsg)) {
				errMsg += "<br/>";
			}
			
			errMsg += s;
		}
		
		if(isEmpty(errMsg)) {
			 // Excel file Data를 duplicate Data에대해 merge하고 reset & Load 함.
			Map<String, Object> resultMap = CommonFunction.makeGridDataFromReport(null, null, null, reportData, ufile.getRegistSeq(), readType);
			List<ProjectIdentification> OssComponents = (List<ProjectIdentification>) resultMap.get("mainData");
			List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(OssComponents);
			
			Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(OssComponents, ossComponentsLicense);
		
			result.put("ossComponents", (List<ProjectIdentification>) remakeComponentsMap.get("mainList"));
			result.put("ossComponentLicense", (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList"));
		} else {
			result.put("errorMsg", errMsg);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public Map<String, Object> readAndroidBuildImage(UploadFile ossReportBean, UploadFile noticeHtmlBean, UploadFile resultTxtBean){
		Map<String, Object> result = new HashMap<String, Object>();
		String errMsg = "";
		List<OssComponents> reportData = new ArrayList<OssComponents>();
		String[] sheet = new String[1];
		List<String> errMsgList = new ArrayList<>();
		String ossReportfileId = ossReportBean.getRegistFileId();
		String resultFileId = "";
		Map<String, Object> checkHeaderSheetName = new HashMap<String, Object>();
		
		try {
			if(resultTxtBean != null) {
				resultFileId = resultTxtBean.getRegistFileId();
			}
			
			// 1) build image를 기준으로 oss data mapping (공통)
			if (!ExcelUtil.readAndroidBuildImage("BIN (Android)", true, sheet, ossReportfileId, resultFileId, reportData, errMsgList, checkHeaderSheetName)) {
				for(String s : errMsgList) { // error 처리
					if(isEmpty(s)) {
						continue;
					}
					
					if(!isEmpty(errMsg)) {
						errMsg += "<br/>";
					}
					
					errMsg += s;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		for(String s : errMsgList) {
			if(isEmpty(s)) {
				continue;
			}
			
			if(!isEmpty(errMsg)) {
				errMsg += "<br/>";
			}
			
			errMsg += s;
		}
		
		if(isEmpty(errMsg)) {
			// result.text에 의해 변경된 내용이 있을 경우 사용자 표시
			// validator에서 session에 격납한다.
			String resultTextChangeHisStr = "";
			
			if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
					CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileId)) != null) {
				resultTextChangeHisStr = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileId));
			}
	
			// license name이 변경된 내용이 있을 경우 사용자 표시
			String licenseNameChangeHisStr = "";
			
			if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
					CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, ossReportfileId)) != null) {
				licenseNameChangeHisStr = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, ossReportfileId));
			}
	
			// notice html과 비교 분석
			Map<String, Object> noticeCheckResultMap = null;
			List<String> noticeBinaryList = null;
			List<String> versionChangedList = null;
			
			try {
	            String fullName = noticeHtmlBean.getFilePath() + "/" + noticeHtmlBean.getFileName();
	            
	            if("xml".equalsIgnoreCase(FilenameUtils.getExtension(noticeHtmlBean.getFileName()))) {
	                noticeBinaryList = CommonFunction.getAndroidNoticeBinaryXmlList(fullName);
	            } else {
	                noticeBinaryList = CommonFunction.getAndroidNoticeBinaryList(FileUtils.readFileToString(new File(fullName)));
	            }
	            
	            Map<String, Object> convertObj = CommonFunction.convertToProjectIdentificationList(reportData, ossReportfileId);
				noticeCheckResultMap = projectService.applySrcAndroidModel((List<ProjectIdentification>)convertObj.get("resultList"), noticeBinaryList);
				
				if(convertObj.containsKey("versionChangeList")) {
					versionChangedList = (List<String>) convertObj.get("versionChangeList");
				}
			} catch (IOException ioe) {
				log.error(ioe.getMessage(), ioe);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
	
			// validation
			Map<String, Object> resultMap = CommonFunction.mergeGridAndReport(null, null,
					(List<ProjectIdentification>) noticeCheckResultMap.get("reportData"), "BINADROID");
			
			List<ProjectIdentification> OssComponents = (List<ProjectIdentification>) resultMap.get("mainData");
			List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(OssComponents);
			
			String systemChangeHisStr = "";
			
			if(!isEmpty(resultTextChangeHisStr)) {
				systemChangeHisStr = resultTextChangeHisStr;
			}
			
			if(!isEmpty(licenseNameChangeHisStr)) {
				if(!isEmpty(systemChangeHisStr)) {
					systemChangeHisStr += "<br><br>";
				}
				
				systemChangeHisStr += licenseNameChangeHisStr;
			}
			
			if(versionChangedList != null) {
				String versionChangedStr = "<b>The following open source version below has been changed to a registered version</b><br><br>";
				
				for(String s : versionChangedList) {
					versionChangedStr += "<br>" + s;
				}
				
				putSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, ossReportfileId), versionChangedStr);
				
				if(!isEmpty(systemChangeHisStr)) {
					systemChangeHisStr += "<br><br>";
				}
				
				systemChangeHisStr += versionChangedStr;
			}
			
			result.put("ossComponents", OssComponents);
			result.put("ossComponentLicense", ossComponentsLicense);
			result.put("systemChangeHisStr", systemChangeHisStr);
		}  else {
			result.put("errorMsg", errMsg);
		}
		
		return result;
	}
	
	@Override
	public int getCreateProjectCnt(String userId) {
		return apiProjectMapper.getCreateProjectCnt(userId);
	}
	
	@Transactional
	@Override
	public Map<String, Object> createProject(Map<String, Object> paramMap){
		Map<String, Object> result = new HashMap<String, Object>();
		int duplicateCnt = apiProjectMapper.checkProject(paramMap);
		
		if(duplicateCnt == 0) {
			apiProjectMapper.createProject(paramMap);
			
			result.put("prjId", (String) paramMap.get("prjId").toString());
			
			OssNotice noticeParam = new OssNotice();
			noticeParam.setPrjId((String) paramMap.get("prjId").toString());
			noticeParam.setNoticeType(CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL);
			
			projectMapper.makeOssNotice(noticeParam);
		}
		
		return result;
	}
	
	@Transactional
	@Override
	public int makeOssNotice(Map<String, Object> paramMap){
		return apiProjectMapper.makeOssNotice(paramMap);
	}
	
	@Override
	public List<Map<String, Object>> getBomList(String prjId){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("prjId", prjId);
		paramMap.put("roleOutLicense", CoCodeManager.CD_ROLE_OUT_LICENSE);
		
		if(CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST != null && !CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST.isEmpty()) {
			paramMap.put("roleOutLicenseIdList", CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST);
		}
		
		paramMap.put("merge", CoConstDef.FLAG_NO);
		
		List<Map<String, Object>> list = apiProjectMapper.selectBomList(paramMap);
		
		for(Map<String, Object> li : list) {
			String licenseId = CommonFunction.removeDuplicateStringToken(avoidNull((String) li.get("licenseId")).toString(), ",");
			String licenseName = CommonFunction.removeDuplicateStringToken((String) li.get("licenseName"), ",");
			String componentId = String.valueOf(li.get("componentId"));
			List<HashMap<String, Object>> listLicense = apiProjectMapper.selectBomLicense(componentId);
			
			li.put("LICENSE_ID", licenseId);
			li.put("LICENSE_NAME", licenseName);
			li.put("ROLE_OUT_LICENSE", CoCodeManager.CD_ROLE_OUT_LICENSE);
			li.put("OSS_COMPONENTS_LICENSE_LIST", listLicense);
		}
		
		return setMergeGridData(list);
	}
	
	@Override
	public List<Map<String, Object>> setMergeGridData(List<Map<String, Object>> list) {
		List<Map<String, Object>> tempData = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> resultGridData = new ArrayList<Map<String, Object>>();
		String groupColumn = "";
		boolean ossNameEmptyFlag = false;
		
		for(Map<String, Object> li : list) {
			if(isEmpty(groupColumn)) {
				groupColumn = (String) li.get("ossName") + "-" + avoidNull((String) li.get("ossVersion"));
			}
			
			if("-".equals(groupColumn)) {
				if("NA".equals((String) li.get("licenseType"))) {
					ossNameEmptyFlag = true;
				}
			}
			
			String ossVersion = avoidNull((String) li.get("ossVersion"));
			String licenseType = avoidNull((String) li.get("licenseType"));
			
			if(groupColumn.equals((String) li.get("ossName") + "-" + ossVersion) // 같은 groupColumn이면 데이터를 쌓음
					&& !("-".equals((String) li.get("ossName")) 
					&& !"NA".equals(licenseType))
					&& !ossNameEmptyFlag) { // 단, OSS Name: - 이면서, License Type: Proprietary이 아닌 경우 Row를 합치지 않음.
				tempData.add(li);
			} else { // 다른 grouping
				setMergeData(tempData, resultGridData);
				groupColumn = (String) li.get("ossName") + "-" + ossVersion;
				tempData.clear();
				tempData.add(li);
			}
		}
		
		setMergeData(tempData, resultGridData); // bom data의 loop가 끝났지만 tempData에 값이 있다면 해당 값도 merge를 함.
		
		return resultGridData;
	}
	
	@SuppressWarnings("unchecked")
	public static void setMergeData(List<Map<String, Object>> tempData, List<Map<String, Object>> resultGridData){
		if(tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					if(((String) o1.get("licenseName")).length() >= ((String) o2.get("licenseName")).length()) { // license name이 같으면 bomList조회해온 순서 그대로 유지함. license name이 다르면 순서변경
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			Map<String, Object> rtnBean = null;
			for(Map<String, Object> temp : tempData) {
				if(rtnBean == null) {
					rtnBean = temp;
					
					continue;
				}
				
				String tempLicenseName = (String) temp.get("licenseName");
				String rtnLicenseName = (String) rtnBean.get("licenseName");
				String key = (String) temp.get("ossName") + "-" + (String) temp.get("licenseType");
				
				if("--NA".equals(key)) {
					if(!rtnLicenseName.contains(tempLicenseName)) {
						resultGridData.add(rtnBean);
						rtnBean = temp;
						
						continue;
					}
				}
				
				// 동일한 oss name과 version일 경우 license 정보를 중복제거하여 merge 함.
				for(String tempStr : tempLicenseName.split(",")) {
					boolean equalFlag = false;
					
					for(String rtnStr : rtnLicenseName.split(",")) {
						if(rtnStr.equals(tempStr)) {
							equalFlag = true;
							
							break;
						}
					}
					
					if(!equalFlag) {
						rtnBean.replace("LICENSE_NAME", rtnLicenseName + "," + tempStr);
					}
				}
				
				List<Map<String, Object>> rtnComponentLicenseList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> tempOssComponentsLicenseList = (List<Map<String, Object>>) temp.get("ossComponentsLicenseList");
				List<Map<String, Object>> rtnOssComponentsLicenseList = (List<Map<String, Object>>) rtnBean.get("ossComponentsLicenseList");
				
				for(Map<String, Object> list : tempOssComponentsLicenseList) {
					int equalsItemList = (int) rtnOssComponentsLicenseList
														.stream()
														.filter(e -> ((String) list.get("licenseName")).equals((String) e.get("licenseName"))) // 동일한 licenseName을 filter
														.collect(Collectors.toList()) // return을 list로변환
														.size(); // 해당 list의 size
					
					if(equalsItemList == 0) {
						rtnComponentLicenseList.add(list);
					}
				}
				
				rtnOssComponentsLicenseList.addAll(rtnComponentLicenseList);
			}
			
			resultGridData.add(rtnBean);
		}
	}
	
	public Map<String, Object> selectVerificationCheck(String prjId){
		return apiProjectMapper.selectVerificationCheck(prjId);
	}
	
	public boolean updatePackageFile(Map<String, Object> paramMap) {
		return apiProjectMapper.updatePackageFile(paramMap) > 0;
	}

	// Json Format: Yaml Format + Vulnerability
	@Override
	public Map<String, Object> getBomExportJson(String prjId) {
		// Get Yaml Format
		String type = CoConstDef.CD_DTL_COMPONENT_ID_BOM;
		Project project = new Project();
		project.setPrjId(prjId);

		String dataStr = toJson(project);
		Type projectType = new TypeToken<Project>(){}.getType();
		Project projectBean = (Project) fromJson(dataStr, projectType);

		ProjectIdentification _param = new ProjectIdentification();
		_param.setReferenceDiv(type);
		_param.setReferenceId(projectBean.getPrjId());
		_param.setMerge(CoConstDef.FLAG_NO);

		Map<String, Object> map = projectService.getIdentificationGridList(_param, true);
		List<ProjectIdentification> list = (List<ProjectIdentification>) map.get("rows");

		LinkedHashMap<String, List<Map<String, Object>>> resultYamlFormat = YamlUtil.checkYamlFormat(projectService.setMergeGridData(list), type);

		Map<String, Object> resultMap = new HashMap<String, Object>();

		// Integrate Yaml Format, Vulnerability
		for(String resultYamlFormatKey: resultYamlFormat.keySet()){
			List<Map<String, Object>> yamlFormatList = resultYamlFormat.get(resultYamlFormatKey);
			for(Map<String, Object> yamlFormatMap: yamlFormatList) {
				String version = (String) yamlFormatMap.get("version");
				List<Map<String, Object>> maxScoreNvdInfoList = apiVulnerabilityService.selectMaxScoreNvdInfo(resultYamlFormatKey, version);

				if (!maxScoreNvdInfoList.isEmpty()) {
					Map<String, Object> maxScoreNvdInfoMap = apiVulnerabilityService.selectMaxScoreNvdInfo(resultYamlFormatKey, version).get(0);
					yamlFormatMap.put("Vulnerability", maxScoreNvdInfoMap.get("cvssScore"));
				}
			}
			resultMap.put(resultYamlFormatKey, yamlFormatList);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getBomCompare(List<Map<String, Object>> beforeBomList, List<Map<String, Object>> afterBomList){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> addList = new HashMap<String, Object>();
		Map<String, Object> deleteList = new HashMap<String, Object>();
		Map<String, Object> changeList = new HashMap<String, Object>();
		
		List<Map<String, Object>> filteredBeforeBomList = beforeBomList
				.stream()
				.filter(bfList -> afterBomList
									.stream()
									.filter(afList -> 
											((String) bfList.get("ossName") + "||" + (String) bfList.get("ossVersion") + "||" + getLicenseNameSort((String) bfList.get("licenseName")))
											.equalsIgnoreCase((String) afList.get("ossName") + "||" + (String) afList.get("ossVersion") + "||" + getLicenseNameSort((String) afList.get("licenseName")))
											).collect(Collectors.toList()).size() == 0
				).collect(Collectors.toList());
		
		List<Map<String, Object>> filteredAfterBomList = afterBomList
				.stream()
				.filter(afList -> beforeBomList
									.stream()
									.filter(bfList -> 
											((String) afList.get("ossName") + "||" + (String) afList.get("ossVersion") + "||" + getLicenseNameSort((String) afList.get("licenseName")))
											.equalsIgnoreCase((String) bfList.get("ossName") + "||" + (String) bfList.get("ossVersion") + "||" + getLicenseNameSort((String) bfList.get("licenseName")))
											).collect(Collectors.toList()).size() == 0
				).collect(Collectors.toList());
		
		// status > add
		for(Map<String, Object> after : filteredAfterBomList) {
			String ossName = (String) after.get("ossName");
			int addTargetCnt = filteredBeforeBomList.stream().filter(before -> ((String)before.get("ossName")).equals(ossName)).collect(Collectors.toList()).size();
			
			if(addTargetCnt == 0) {
				Map<String, Object> addMap = new LinkedHashMap<String, Object>();
				
				addMap.put("name", (String) after.get("ossName"));
				addMap.put("version", avoidNull((String) after.get("ossVersion"), ""));
				addMap.put("license", Arrays.asList(((String) after.get("licenseName")).split(",")));
				
				addList.put(getCompareKey(after), addMap);
			}
		}
		
		// status > delete
		for(Map<String, Object> before : filteredBeforeBomList) {
			String ossName = (String) before.get("ossName");
			List<Map<String, Object>> afterList = filteredAfterBomList.stream().filter(after -> ((String)after.get("ossName")).equals(ossName)).collect(Collectors.toList());
			
			if(afterList.size() == 0) {
				Map<String, Object> deleteMap = new LinkedHashMap<String, Object>();
				
				deleteMap.put("name", (String) before.get("ossName"));
				deleteMap.put("version", avoidNull((String) before.get("ossVersion"), ""));
				deleteMap.put("license", Arrays.asList(((String) before.get("licenseName")).split(",")));
				deleteList.put(getCompareKey(before), deleteMap);
			}
		}
		
		// status > change
		if(!filteredBeforeBomList.isEmpty() && !filteredAfterBomList.isEmpty()) {
			List<Map<String, Object>> deduplicatedBeforeBomList = new ArrayList<>();
			List<Map<String, Object>> deduplicatedAfterBomList = new ArrayList<>();
			
			boolean firstFlag = true;
			boolean deduplicateFlag = false;
			
			for(Map<String, Object> filteredBeforeBom : filteredBeforeBomList) {
				String ossName = (String) filteredBeforeBom.get("ossName");
				String ossVersion = (String) filteredBeforeBom.get("ossVersion");
				String licenseName = (String) filteredBeforeBom.get("licenseName");
				
				if(firstFlag) {
					List<Map<String, Object>> addBeforeBomList = new ArrayList<>();
					Map<String, Object> addBeforeBom = new LinkedHashMap<>();
					Map<String, Object> addBeforeBom2 = new HashMap<>();
					
					addBeforeBom.put("version", ossVersion);
					addBeforeBom.put("license", Arrays.asList(licenseName.trim().split(",")));
					addBeforeBomList.add(addBeforeBom);
					addBeforeBom2.put(ossName, addBeforeBomList);
					deduplicatedBeforeBomList.add(addBeforeBom2);
					
					firstFlag = false;
				} else {
					for(Map<String, Object> deduplicatedBeforeBom : deduplicatedBeforeBomList) {
						if(deduplicatedBeforeBom.containsKey(ossName)) {
							@SuppressWarnings("unchecked")
							List<Map<String, Object>> orgValues = (List<Map<String, Object>>) deduplicatedBeforeBom.get(ossName);
							
							Map<String, Object> addBeforeBom = new LinkedHashMap<>();
							addBeforeBom.put("version", ossVersion);
							addBeforeBom.put("license", Arrays.asList(licenseName.trim().split(",")));
							orgValues.add(addBeforeBom);
							deduplicatedBeforeBom.replace(ossName, orgValues);
							deduplicateFlag = true;
						}
					}
					
					if(!deduplicateFlag) {
						List<Map<String, Object>> addBeforeBomList = new ArrayList<>();
						Map<String, Object> addBeforeBom = new LinkedHashMap<>();
						Map<String, Object> addBeforeBom2 = new HashMap<>();
						addBeforeBom.put("version", ossVersion);
						addBeforeBom.put("license", Arrays.asList(licenseName.trim().split(",")));
						addBeforeBomList.add(addBeforeBom);
						addBeforeBom2.put(ossName, addBeforeBomList);
						deduplicatedBeforeBomList.add(addBeforeBom2);
					}
					
					deduplicateFlag = false;
				}
			}
			
			for(Map<String, Object> filteredAfterBom : filteredAfterBomList) {
				String ossName = (String) filteredAfterBom.get("ossName");
				String ossVersion = (String) filteredAfterBom.get("ossVersion");
				String licenseName = (String) filteredAfterBom.get("licenseName");
				
				if(firstFlag) {
					List<Map<String, Object>> addAfterBomList = new ArrayList<>();
					Map<String, Object> addAfterBom = new LinkedHashMap<>();
					Map<String, Object> addAfterBom2 = new HashMap<>();
					
					addAfterBom.put("version", ossVersion);
					addAfterBom.put("license", Arrays.asList(licenseName.trim().split(",")));
					addAfterBomList.add(addAfterBom);
					addAfterBom2.put(ossName, addAfterBomList);
					deduplicatedAfterBomList.add(addAfterBom2);
					
					firstFlag = false;
				} else {
					for(Map<String, Object> deduplicatedAfterBom : deduplicatedAfterBomList) {
						if(deduplicatedAfterBom.containsKey(ossName)) {
							@SuppressWarnings("unchecked")
							List<Map<String, Object>> orgValues = (List<Map<String, Object>>) deduplicatedAfterBom.get(ossName);
							
							Map<String, Object> addBeforeBom = new LinkedHashMap<>();
							addBeforeBom.put("version", ossVersion);
							addBeforeBom.put("license", Arrays.asList(licenseName.trim().split(",")));
							orgValues.add(addBeforeBom);
							deduplicatedAfterBom.replace(ossName, orgValues);
							deduplicateFlag = true;
						} 
					}
					
					if(!deduplicateFlag) {
						List<Map<String, Object>> addAfterBomList = new ArrayList<>();
						Map<String, Object> addAfterBom = new LinkedHashMap<>();
						Map<String, Object> addAfterBom2 = new HashMap<>();
						addAfterBom.put("version", ossVersion);
						addAfterBom.put("license", Arrays.asList(licenseName.trim().split(",")));
						addAfterBomList.add(addAfterBom);
						addAfterBom2.put(ossName, addAfterBomList);
						deduplicatedAfterBomList.add(addAfterBom2);
					}
					
					deduplicateFlag = false;
				}
			}
			
			for(Map<String, Object> deduplicatedBeforeBom : deduplicatedBeforeBomList) {
				for(String beforeKey : deduplicatedBeforeBom.keySet()) {
					for(Map<String, Object> deduplicatedAfterBom : deduplicatedAfterBomList) {
						for(String afterKey : deduplicatedAfterBom.keySet()) {
							if(beforeKey.equalsIgnoreCase(afterKey)) {
								Map<String, Object> changeMap = new LinkedHashMap<String, Object>();
								
								changeMap.put("name", afterKey);
								changeMap.put("prev", deduplicatedBeforeBom.get(beforeKey));
								changeMap.put("now", deduplicatedAfterBom.get(afterKey));
								
								changeList.put(afterKey, changeMap);
								break;
							}
						}
					}
				}
			}
		}
		
		// add, delete, change가 값이없으면 완전일치한 project로 판단. 
		resultMap.put("add", 	addList.values());
		resultMap.put("delete", deleteList.values());
		resultMap.put("change", changeList.values());
		
		return resultMap;
	}
	
	private String getLicenseNameSort(String licenseName) {
		String sortedValue = "";
		
		String splitLicenseNm[] = licenseName.trim().split(",");
		Arrays.sort(splitLicenseNm);
		
		for (int i=0; i< splitLicenseNm.length; i++) {
			sortedValue += splitLicenseNm[i];
			if (i<splitLicenseNm.length-1) {
				sortedValue += ", ";
			}
		}
		
		return sortedValue;
	}
	
	@Override
	public Map<String, Object> selectModelList(Map<String, Object> paramMap){
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> list = apiProjectMapper.selectProject(paramMap);
		List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
		
		for(Map<String, Object> map : list) {
			Map<String, Object> modelMap = new HashMap<String, Object>();
			String prjId = (String) map.get("prjId").toString();
			List<Map<String, Object>> modelList = apiProjectMapper.selectModelList(prjId);
			
			modelMap.put("prjId", prjId);
			modelMap.put("modelList", modelList);
			contents.add(modelMap);
		}
		
		result.put("records", list.size());
		result.put("contents", contents);
		
		return result;
	}
	
	private String getCompareKey(Map<String, Object> paramMap) {
		return (String) paramMap.get("ossName") + "|" + avoidNull((String) paramMap.get("ossVersion"), "") + "|" + (String) paramMap.get("licenseName");
	}

	@Override
	public List<Map<String, Object>> getVerifyOssList(Map<String, Object> project) {
		List<Map<String, Object>> verifyFilePathList = apiProjectMapper.selectVerifyOssList(project);
		
		if(verifyFilePathList != null && !verifyFilePathList.isEmpty() && verifyFilePathList.get(0) == null) {
			verifyFilePathList = new ArrayList<>();
		}
		
		return verifyFilePathList;
	}

	@Override
	public List<Map<String, Object>> serMergeGridData(List<Map<String, Object>> gridData) {
		List<Map<String, Object>> tempData = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> resultGridData = new ArrayList<Map<String, Object>>();
		
		String groupColumn = "";

		final Comparator<Map<String, Object>> comp = Comparator.comparing((Map<String, Object> o) -> o.get("ossName")+"|"+ o.get("ossVersion"));
		gridData = gridData.stream().sorted(comp).collect(Collectors.toList());
		
		for(Map<String, Object> info : gridData) {
			if(isEmpty(groupColumn)) {
				groupColumn = (String) info.get("ossName") + "-" + (String) info.get("ossVersion");
			}
						
			if(groupColumn.equals((String) info.get("ossName") + "-" + (String) info.get("ossVersion")) // 같은 groupColumn이면 데이터를 쌓음
					&& !("-".equals((String) info.get("ossName")) 
					&& !"NA".equals((String) info.get("licenseType")))) { // 단, OSS Name: - 이면서, License Type: Proprietary이 아닌 경우 Row를 합치지 않음.
				tempData.add(info);
			} else { // 다른 grouping
				setVerifyMergeData(tempData, resultGridData);
				groupColumn = (String) info.get("ossName") + "-" + (String) info.get("ossVersion");
				tempData.clear();
				
				tempData.add(info);
			}
		}
		
		setVerifyMergeData(tempData, resultGridData);
		
		return resultGridData;
	}

	private void setVerifyMergeData(List<Map<String, Object>> tempData, List<Map<String, Object>> resultGridData) {
		if(tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					if(((String) o1.get("licenseName")).length() >= ((String) o2.get("licenseName")).length()) {
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			Map<String, Object> rtnBean = null;
			
			for(Map<String, Object> temp : tempData) {
				if(rtnBean == null) {
					rtnBean = temp;
					
					continue;
				}
				
				String key = (String) temp.get("ossName") + "-" + (String) temp.get("licenseType");
				
				if("--NA".equals(key)) {
					if(!((String) rtnBean.get("licenseName")).contains((String) temp.get("licenseName"))) {
						resultGridData.add(rtnBean);
						rtnBean = temp;
						
						continue;
					}
				}
				
				for(String licenseName : ((String) temp.get("licenseName")).split(",")) {
					boolean equalFlag = false;
					
					for(String rtnLicenseName : ((String) rtnBean.get("licenseName")).split(",")) {
						if(rtnLicenseName.equals(licenseName)) {
							equalFlag = true;
							
							break;
						}
					}
					
					if(!equalFlag) {
						rtnBean.put("licenseName", (String) rtnBean.get("licenseName") + "," + licenseName);
					}
				}
			}
			
			resultGridData.add(rtnBean);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String setClearFiles(Map<String, Object> map) {
		String deleteComment = "";
		String uploadComment = "";
		String prjId = (String) map.get("prjId");
		List<String> fileSeqs =	(List<String>)map.get("fileSeqs");
		List<Map<String, Object>> uploadFileInfos = new ArrayList<>();
		File file = null;
		
		Map<String, Object> prjParam = new HashMap<String, Object>();
		prjParam.put("prjId", prjId);
		ArrayList<String> newPackagingFileIdList = new ArrayList<String>();
		newPackagingFileIdList.add(fileSeqs.size() > 0 ? fileSeqs.get(0) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 1 ? fileSeqs.get(1) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 2 ? fileSeqs.get(2) : null);
		prjParam.put("packageFileId", newPackagingFileIdList.get(0));
		prjParam.put("packageFileId2", newPackagingFileIdList.get(1));
		prjParam.put("packageFileId3", newPackagingFileIdList.get(2));
		
		for(String fileSeq : fileSeqs){
			Map<String, Object> paramT2File = new HashMap<>();
			
			paramT2File.put("fileSeq", fileSeq);
			uploadFileInfos.add(apiFileMapper.getFileInfo(paramT2File));
		}
						
		String publicUrl = appEnv.getProperty("upload.path", "/upload");
		String packagingUrl = appEnv.getProperty("packaging.path", "/upload/packaging") + "/" + prjId;
		List<Map<String, Object>> result = apiFileMapper.selectPackagingFileInfo(prjId); // verify한 file을 select함.

		if(result.size() > 0){
			for(Map<String, Object> res : result){
				String rtnFilePath = (String) res.get("logiPath");
				String rtnFileName = (String) res.get("logiNm");
				String rtnFileSeq = Integer.toString((int) res.get("fileSeq"));
				
				if(publicUrl.equals(rtnFilePath)){
					// select한 filePath가 upload Dir 일 경우 해당 파일만 삭제함.
					file = new File(rtnFilePath + "/" + rtnFileName);
					
					int reuseCnt = apiFileMapper.getPackgingReuseCnt(rtnFileName);
					
					if(reuseCnt == 0){
						Map<String, Object> delFile = new HashMap<>();
						delFile.put("fileSeq", rtnFileSeq);
						delFile.put("gubn", "A");
						int returnSuccess = apiFileMapper.updateFileDelYnKessan(delFile);
						
						if(returnSuccess > 0) {
							if(file.delete()){
								log.debug(rtnFilePath + "/" + rtnFileName + " is delete success.");
							} else {
								log.debug(rtnFilePath + "/" + rtnFileName + " is delete failed.");
							}
						}
					}
				}
			}
			
			deleteFiles(packagingUrl, uploadFileInfos, prjId); // 'upload/packaging/#{prjId}' 의 Directory가 있는지 체크 후 삭제 처리함.( 현재등록한 file을 제외한 나머지를 삭세처리 )
		} else {
			deleteFiles(packagingUrl, uploadFileInfos, prjId); // verify 한 file이 없을경우 packagingUrl도 같이 검사하여 delete를 함.
		}
		
		// packaging File comment
		try {
			Map<String, Object> project = apiProjectMapper.selectProjectMaster(prjParam);
			ArrayList<String> origPackagingFileIdList = new ArrayList<String>();
			if(project.containsKey("packageFileId")) {
				if(project.get("packageFileId") != null && !("").equals(Integer.toString((int) project.get("packageFileId")))){
					origPackagingFileIdList.add(Integer.toString((int) project.get("packageFileId")));
				}
			}
			if(project.containsKey("packageFileId2")) {
				if(project.get("packageFileId2") != null && !("").equals(Integer.toString((int) project.get("packageFileId2")))){
					origPackagingFileIdList.add(Integer.toString((int) project.get("packageFileId2")));
				}
			}
			if(project.containsKey("packageFileId3")) {
				if(project.get("packageFileId3") != null && !("").equals(Integer.toString((int) project.get("packageFileId3")))){
					origPackagingFileIdList.add(Integer.toString((int) project.get("packageFileId3")));
				}
			}
						
			int idx = 0;
			
			for(String fileId : origPackagingFileIdList){
				Map<String, Object> fileInfo = new HashMap<>();
				
				if(!isEmpty(fileId) && !fileId.equals(newPackagingFileIdList.get(idx))){
					//fileInfo.setFileSeq(fileId);
					fileInfo = apiFileMapper.selectFileInfo(fileId);
					deleteComment += "Packaging file, "+ (String) fileInfo.get("origNm") +", was deleted by "+loginUserName()+". <br>";
				}
				
				if(!isEmpty(newPackagingFileIdList.get(idx)) && !newPackagingFileIdList.get(idx).equals(fileId)){
					//fileInfo.setFileSeq(newPackagingFileIdList.get(idx));
					fileInfo = apiFileMapper.selectFileInfo(newPackagingFileIdList.get(idx));
					oss.fosslight.domain.File resultFile = (oss.fosslight.domain.File) apiProjectMapper.selectVerificationFile(newPackagingFileIdList.get(idx));
					
					if(CoConstDef.FLAG_YES.equals(resultFile.getReuseFlag())){
						uploadComment += "Packaging file, "+ (String) fileInfo.get("origNm")+", was loaded from Project ID: "+resultFile.getRefPrjId()+" by "+loginUserName()+". <br>";
					} else {
						uploadComment += "Packaging file, "+ (String) fileInfo.get("origNm")+", was uploaded by "+loginUserName()+". <br>";
					}
				}
				
				idx++;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}		
		
		apiProjectMapper.updatePackagingReuseMap(prjParam);
		
		return deleteComment + uploadComment;
	}

	private void deleteFiles(String url, List<Map<String, Object>> uploadFileInfos, String prjId) {
		File file = new File(url);
		ArrayList<String> LogiNms = new ArrayList<String>();
		ArrayList<String> reuseNms = new ArrayList<String>();
		
		for(Map<String, Object> uploadFileInfo : uploadFileInfos){
			LogiNms.add((String) uploadFileInfo.get("logiNm"));
		}
		
		// 현재 proejct Packaging File 중 재사용중인 packaging File 이 있다면 제거 불가
		List<Map<String, Object>> reusePackaging = apiFileMapper.getReusePackagingInfo();
		
		for(Map<String, Object> reuse : reusePackaging){
			reuseNms.add((String) reuse.get("logiNm"));
		}
		
		if(file.exists()){
			for(File f : file.listFiles()){
				String fileNm = f.getName();
				
				if(!LogiNms.contains(fileNm)){
					Map<String, Object> delFile = new HashMap<String, Object>();
					delFile.put("logiPath", url);
					delFile.put("logiNm", f.getName());
					
					int returnSuccess = apiFileMapper.updateReuseChkFileDelYnByFilePathNm(delFile);
					
					if(returnSuccess > 0 && !reuseNms.contains(fileNm)){
						if(f.delete()){
							log.debug(url + "/" + f.getName() + " is delete success.");
						}else{
							log.debug(url + "/" + f.getName() + " is delete failed.");
						}
					}
				}
			}
		}
		
		// 재사용을 했었던 file중 다른 project에서도 재사용을 하지 않은 file 있는지 확인하고 재사용을 안한다면 file 삭제 / 추후 reuse하는 다른 project에서도 reuseFlag가 N이 되면 지우는 case이므로 log는 남기지 않음.
		List<Map<String, Object>> reusePackagingFileList = apiFileMapper.getPackgingReuseCntToList(prjId);
		
		for(Map<String, Object> reusePackagingFile : reusePackagingFileList){ // reuseCnt가 0인 값만 불러오고 삭제처리 후 hidden flag를 Y로 변경 그리고 재검색시 조회 불가상태로 만듦.
			File reuseFile = new File((String) reusePackagingFile.get("logiPath"));
			
			if(reuseFile.exists()){
				for(File f : reuseFile.listFiles()){
					if(((String) reusePackagingFile.get("logiNm")).equals(f.getName())){
						Map<String, Object> delFile = new HashMap<>();
						delFile.put("logiPath", reusePackagingFile.get("logiPath"));
						delFile.put("logiNm", f.getName());
						int returnSuccess = apiFileMapper.updateFileDelYnByFilePathNm(delFile);
						String[] refPrjIds = ((String) reusePackagingFile.get("logiPath")).split("/");
						
						String refPrjId = refPrjIds[refPrjIds.length-1];
						String logiPath = (String) reusePackagingFile.get("logiPath");
						String logiNm = f.getName();
						
						apiFileMapper.setReusePackagingFileHidden(refPrjId, logiPath, logiNm);
						
						if(returnSuccess > 0){
							if(f.delete()){
								log.debug(url + "/" + f.getName() + " is delete success.");
							}else{
								log.debug(url + "/" + f.getName() + " is delete failed.");
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean getChangedPackageFile(String prjId, List<String> fileSeqs) {
		String packageFileId = fileSeqs.get(0);
		String packageFileId2 = fileSeqs.size() > 1 ? fileSeqs.get(1) : null;
		String packageFileId3 = fileSeqs.size() > 2 ? fileSeqs.get(2) : null;
		
		int result = apiProjectMapper.checkPackagingFileId(prjId, packageFileId, packageFileId2, packageFileId3);
		
		if(result > 0){
			return false;
		}else{
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> processVerification(Map<String, Object> map, Map<String, Object> file, Map<String, Object> project) {
		String VERIFY_HOME_PATH = CommonFunction.emptyCheckProperty("verify.home.path", "/verify");
		String VERIFY_BIN_PATH = CommonFunction.emptyCheckProperty("verify.bin.path", "/verify");
		String VERIFY_PATH_OUTPUT = CommonFunction.emptyCheckProperty("verify.output.path", "/verify/output");
		String VERIFY_PATH_DECOMP = CommonFunction.emptyCheckProperty("verify.decompress.path", "/verify/decompression");
				
		HashMap<String, Object> resMap = new HashMap<>();
		String resCd = "00";
		String resMsg = "";
		
		log.info("[API] VERIFY START PROJECT ID : " + (String)map.get("prjId"));
		
		int allFileCount = 0;
		String[] result	=	null;
		String readmePath =	"";
		String exceptFileContent = "";
		String prjId =	(String)map.get("prjId");
		String fileSeq =	(String)map.get("fileSeq");
		int packagingFileIdx = (int)map.get("packagingFileIdx");
		List<String> fileSeqs =	(List<String>)map.get("fileSeqs");
		String filePath =	"";
		List<String> gridFilePaths =	(List<String>)map.get("gridFilePaths");
		List<String> gridComponentIds =	(List<String>)map.get("gridComponentIds");
		boolean isChangedPackageFile = (boolean)map.get("isChangedPackageFile");
		String packagingComment = (String)map.get("packagingComment");
		
		List<String> checkExceptionWordsList = CoCodeManager.getCodeNames(CoConstDef.CD_VERIFY_EXCEPTION_WORDS);
		List<String> checkExceptionIgnoreWorksList = CoCodeManager.getCodeNames(CoConstDef.CD_VERIFY_IGNORE_WORDS);
		
		Map<String, Object> prjInfo = null;
		boolean doUpdate = true;
		
		try {
			// 프로젝트 정보 취득 (verification status 에 따라 DB update 여부를 결정
			{
				prjInfo = getProjectBasicInfo(prjId);
				
				if(prjInfo != null && CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals((String) prjInfo.get("verificationStatus"))) {
					doUpdate = false;
				}
			}
			
			File chk_list_file = new File(VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list_"+packagingFileIdx);
			
			if(!chk_list_file.exists()) {
				isChangedPackageFile = true;
			}
			
			file.put("fileSeq" ,fileSeq);
			file = apiFileMapper.getFileInfo(file);
			filePath = (String) file.get("logiPath") + "/" + (String) file.get("logiNm");
			
			log.debug("[API] VERIFY TARGET FILE : " + filePath);
			
			if(packagingFileIdx == 1 && isChangedPackageFile) {
				ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "find " + VERIFY_PATH_OUTPUT + " -maxdepth 1 -name "+prjId+" -type d -exec rm -rf {} \\;"});
			}
			
			String exceptionWordsPatten = "proprietary\\|commercial";
			if(checkExceptionWordsList != null && !checkExceptionWordsList.isEmpty()) {
				exceptionWordsPatten = "";
				
				for(String s : checkExceptionWordsList) {
					if(!isEmpty(exceptionWordsPatten)) {
						exceptionWordsPatten += "\\|";
					}
					
					exceptionWordsPatten += s;
				}
			}
			
			log.info("[API] VERIFY prjName : " + prjInfo.get("prjName"));
			log.info("[API] VERIFY OrigNm : " + file.get("origNm"));
			String projectNm = ((String) prjInfo.get("prjName")).replace(" ", "@@");
			
			if(prjInfo.containsKey("prjVersion") && prjInfo.get("prjVersion") != null && !("").equals((String) prjInfo.get("prjVersion"))){
				projectNm +="_"+((String) prjInfo.get("prjVersion")).replace(" ", "@@");
			}
			
			projectNm +="_"+Integer.toString(packagingFileIdx)+"("+((String) file.get("origNm")).replace(" ", "@@")+")";
			
			String commandStr = VERIFY_BIN_PATH+"/verify "+filePath+" "+prjId+" "+exceptionWordsPatten+" "+projectNm+" "+packagingFileIdx+" "+VERIFY_HOME_PATH;
			log.info("[API] VERIFY COMMAND : " + commandStr);

			log.info("[API] VERIFY START : " + prjId);
			
			if(isChangedPackageFile){ // packageFile을 변경하지 않고 다시 verify할 경우 아래 shellCommander는 중복 동작 하지 않음.
				ShellCommander.shellCommandWaitFor(commandStr);
			}
			
			log.info("[API] VERIFY END : " + prjId);
			
			//STEP 2 : Verify 진행후 특정 위치의 파일리스트 출력
			//STEP 3 : 결과 문자열 리스트값을 배열로 변환 		
			String chk_list_file_path = null;
			
			if(packagingFileIdx == 1) {
				chk_list_file_path = VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list_1";
			} else {
				chk_list_file_path = VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list";
			}
			
			String verify_chk_list = CommonFunction.getStringFromFile(chk_list_file_path).replaceAll(VERIFY_PATH_DECOMP +"/"+ prjId + "/", "");
			log.info("[API] VERIFY Read verify_chk_list END : " + prjId);
			
			result = verify_chk_list.split(System.lineSeparator());
			allFileCount = StringUtils.countMatches(verify_chk_list, "*");
			
			// 압축 해제한 디렉토리를 포함하여 상호비교 하기 위해 제외할 디렉토리 명 추출
			// 두번째 디렉토리 까지 치환할 문자열 추출
			
			log.debug("[API] file.getOrigNm() : " + file.get("origNm"));
			log.debug("[API] file.getExt() : " + file.get("ext"));
			
			String tempFileOrgName = (String) file.get("origNm");
			String tempFileOrgExt = (String) file.get("ext");
			
			log.debug("[API] lastIndexOf(file.getExt()) : " + tempFileOrgName.lastIndexOf(tempFileOrgExt));
			
			String rePath = FilenameUtils.removeExtension(tempFileOrgName);
			
			log.debug("[API] rePath : " + rePath);
			
			if(rePath.indexOf(".tar") > -1){
				rePath = rePath.substring(0, rePath.lastIndexOf(".tar"));
			}
			
			String decompressionDirName = "/" + rePath;
			
			String packageFileName = rePath;
			String decompressionRootPath = "";
			
			// 사용자 입력과 packaging 파일의 디렉토리 정보 비교를 위해
			// 분석 결과를 격납 (dir or file n	ame : count)
			Map<String, Integer> deCompResultMap = new HashMap<>();
			List<String> readmePathList = new ArrayList<String>();
			if(result != null) {
				boolean isFirst = true;
				
				for(String s : result) {
					if(!isEmpty(s) && !(s.contains("(") && s.contains(")"))) {
						// packaging file name의 경우 Path로 인식하지 못하도록 처리함.

						boolean isFile = s.endsWith("*");
						s = s.replace(VERIFY_PATH_DECOMP +"/" + prjId + "/", "");
						s = s.replaceAll("//", "/");
						
						if(s.startsWith("/")) {
							s = s.substring(1);
						}
						
						if(s.endsWith("*")) {
							s = s.substring(0, s.length()-1);
						}
						
						if(s.endsWith("/")) {
							s = s.substring(0, s.length() -1);
						}
						
						if(isFirst) {
							// 첫번째 path를 압축을 푼 처번째 dir로 사용
							decompressionRootPath = s;
							
							isFirst = false;
						}
						
						int cnt = 0;
						
						//파일 path인 경우, 상위 dir의 파일 count를 +1 한다.
						if(isFile){
							String _dir = s;
							
							if(s.toUpperCase().indexOf("README") > -1) {
								readmePathList.add(s);
							}
							
							if(s.indexOf("/") > -1) {
								_dir = s.substring(0, s.lastIndexOf("/"));
							}
							
							if(deCompResultMap.containsKey(_dir)) {
								cnt = deCompResultMap.get(_dir);
							}
							
							cnt++;
							
							deCompResultMap.put(_dir, cnt);
						}
						
						deCompResultMap.put(s, 0);
					}
				}
			}
			
			List<String> paths = sortByValue(deCompResultMap);
			
			for(String path : paths){
				if(deCompResultMap.get(path) != null){
					deCompResultMap = setAddFileCount(deCompResultMap, path, (int)deCompResultMap.get(path));
				}
			}
			
			// 결과 file path에 대해서 4가지 허용 패턴으로 검사한다.
			Map<String, Integer> checkResultMap = new HashMap<>();
			List<String> pathCheckList1 = new ArrayList<>();
			List<String> pathCheckList2 = new ArrayList<>();
			List<String> pathCheckList3 = new ArrayList<>();
			List<String> pathCheckList4 = new ArrayList<>();
			
			List<String> pathCheckList11 = new ArrayList<>();
			List<String> pathCheckList21 = new ArrayList<>();
			List<String> pathCheckList31 = new ArrayList<>();
			List<String> pathCheckList41 = new ArrayList<>();
			
			List<String> pathCheckList12 = new ArrayList<>();
			List<String> pathCheckList22 = new ArrayList<>();
			List<String> pathCheckList32 = new ArrayList<>();
			List<String> pathCheckList42 = new ArrayList<>();
			
			List<String> pathCheckList13 = new ArrayList<>();
			List<String> pathCheckList23 = new ArrayList<>();
			List<String> pathCheckList33 = new ArrayList<>();
			List<String> pathCheckList43 = new ArrayList<>();
			
			List<String> pathCheckList14 = new ArrayList<>();
			List<String> pathCheckList24 = new ArrayList<>();
			List<String> pathCheckList34 = new ArrayList<>();
			List<String> pathCheckList44 = new ArrayList<>();
			
			List<String> pathCheckList15 = new ArrayList<>();
			List<String> pathCheckList25 = new ArrayList<>();
			List<String> pathCheckList35 = new ArrayList<>();
			List<String> pathCheckList45 = new ArrayList<>();

			List<String> pathCheckList16 = new ArrayList<>();
			List<String> pathCheckList26 = new ArrayList<>();
			List<String> pathCheckList36 = new ArrayList<>();
			List<String> pathCheckList46 = new ArrayList<>();
			
			for (String path : deCompResultMap.keySet()) {
				pathCheckList1.add(path);
				pathCheckList2.add("/" + path);
				pathCheckList3.add(path + "/");
				pathCheckList4.add("/"+path + "/");

				String replaceFilePath = path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
				
				if(replaceFilePath.startsWith("/")) {
					replaceFilePath = replaceFilePath.substring(1);
				}
				
				if(replaceFilePath.endsWith("/")) {
					replaceFilePath = replaceFilePath.substring(0, replaceFilePath.length()-1);
				}
				
				pathCheckList11.add(replaceFilePath);
				pathCheckList21.add("/" + replaceFilePath);
				pathCheckList31.add(replaceFilePath + "/");
				pathCheckList41.add("/"+replaceFilePath + "/");
				
				String addRootDir = decompressionDirName + "/" + path;
				
				if(addRootDir.startsWith("/")) {
					addRootDir = addRootDir.substring(1);
				}
				
				if(addRootDir.endsWith("/")) {
					addRootDir = addRootDir.substring(0, addRootDir.length()-1);
				}
				
				pathCheckList12.add(addRootDir);
				pathCheckList22.add("/" + addRootDir);
				pathCheckList32.add(addRootDir + "/");
				pathCheckList42.add("/"+addRootDir + "/");
				
				String addRootDirReplaceFilePath = decompressionDirName + "/" + path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
				
				if(addRootDirReplaceFilePath.startsWith("/")) {
					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(1);
				}
				
				if(addRootDirReplaceFilePath.endsWith("/")) {
					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(0, addRootDirReplaceFilePath.length());
				}
				
				pathCheckList13.add(addRootDirReplaceFilePath);
				pathCheckList23.add("/" + addRootDirReplaceFilePath);
				pathCheckList33.add(addRootDirReplaceFilePath + "/");
				pathCheckList43.add("/"+addRootDirReplaceFilePath + "/");

				String replaceRootDir = path.replaceFirst(packageFileName, "").replaceAll("//", "/");
				if(replaceRootDir.startsWith("/")) {
					replaceRootDir = replaceRootDir.substring(1);
				}
				
				if(replaceRootDir.endsWith("/")) {
					replaceRootDir = replaceRootDir.substring(0, replaceRootDir.length()-1);
				}
				
				pathCheckList14.add(replaceRootDir);
				pathCheckList24.add("/" + replaceRootDir);
				pathCheckList34.add(replaceRootDir + "/");
				pathCheckList44.add("/"+replaceRootDir + "/");
				
				String replaceRootDirReplaceFilePath = replaceRootDir;
				
				if(replaceRootDirReplaceFilePath.endsWith("*")) {
					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
				}
				
				if(replaceRootDirReplaceFilePath.endsWith("/")) {
					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
				}
				
				pathCheckList15.add(replaceRootDirReplaceFilePath);
				pathCheckList25.add("/" + replaceRootDirReplaceFilePath);
				pathCheckList35.add(replaceRootDirReplaceFilePath + "/");
				pathCheckList45.add("/"+replaceRootDirReplaceFilePath + "/");
				
				String replaceDecomFileRootDir = path.replaceFirst(decompressionRootPath, "").replaceAll("//", "/");
				
				if(replaceDecomFileRootDir.startsWith("/")) {
					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(1);
				}
				
				if(replaceDecomFileRootDir.endsWith("/")) {
					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(0, replaceDecomFileRootDir.length()-1);
				}
				
				pathCheckList16.add(replaceDecomFileRootDir);
				pathCheckList26.add("/" + replaceDecomFileRootDir);
				pathCheckList36.add(replaceDecomFileRootDir + "/");
				pathCheckList46.add("/"+replaceDecomFileRootDir + "/");
			}
			
			// 통합 Map 에 모든 허용 패턴을 저장
			int idx = 0;
			
			for(String s : pathCheckList1) {
				checkResultMap.put(s, deCompResultMap.containsKey(s) ? deCompResultMap.get(s) : 0);
				checkResultMap.put(pathCheckList2.get(idx), deCompResultMap.containsKey(pathCheckList2.get(idx)) ? deCompResultMap.get(pathCheckList2.get(idx)) : 0);
				checkResultMap.put(pathCheckList3.get(idx), deCompResultMap.containsKey(pathCheckList3.get(idx)) ? deCompResultMap.get(pathCheckList3.get(idx)) : 0);
				checkResultMap.put(pathCheckList4.get(idx), deCompResultMap.containsKey(pathCheckList4.get(idx)) ? deCompResultMap.get(pathCheckList4.get(idx)) : 0);

				checkResultMap.put(pathCheckList11.get(idx), deCompResultMap.containsKey(pathCheckList11.get(idx)) ? deCompResultMap.get(pathCheckList11.get(idx)) : 0);
				checkResultMap.put(pathCheckList21.get(idx), deCompResultMap.containsKey(pathCheckList21.get(idx)) ? deCompResultMap.get(pathCheckList21.get(idx)) : 0);
				checkResultMap.put(pathCheckList31.get(idx), deCompResultMap.containsKey(pathCheckList31.get(idx)) ? deCompResultMap.get(pathCheckList31.get(idx)) : 0);
				checkResultMap.put(pathCheckList41.get(idx), deCompResultMap.containsKey(pathCheckList41.get(idx)) ? deCompResultMap.get(pathCheckList41.get(idx)) : 0);

				checkResultMap.put(pathCheckList12.get(idx), deCompResultMap.containsKey(pathCheckList12.get(idx)) ? deCompResultMap.get(pathCheckList12.get(idx)) : 0);
				checkResultMap.put(pathCheckList22.get(idx), deCompResultMap.containsKey(pathCheckList22.get(idx)) ? deCompResultMap.get(pathCheckList22.get(idx)) : 0);
				checkResultMap.put(pathCheckList32.get(idx), deCompResultMap.containsKey(pathCheckList32.get(idx)) ? deCompResultMap.get(pathCheckList32.get(idx)) : 0);
				checkResultMap.put(pathCheckList42.get(idx), deCompResultMap.containsKey(pathCheckList42.get(idx)) ? deCompResultMap.get(pathCheckList42.get(idx)) : 0);

				checkResultMap.put(pathCheckList13.get(idx), deCompResultMap.containsKey(pathCheckList13.get(idx)) ? deCompResultMap.get(pathCheckList13.get(idx)) : 0);
				checkResultMap.put(pathCheckList23.get(idx), deCompResultMap.containsKey(pathCheckList23.get(idx)) ? deCompResultMap.get(pathCheckList23.get(idx)) : 0);
				checkResultMap.put(pathCheckList33.get(idx), deCompResultMap.containsKey(pathCheckList33.get(idx)) ? deCompResultMap.get(pathCheckList33.get(idx)) : 0);
				checkResultMap.put(pathCheckList43.get(idx), deCompResultMap.containsKey(pathCheckList43.get(idx)) ? deCompResultMap.get(pathCheckList43.get(idx)) : 0);

				checkResultMap.put(pathCheckList14.get(idx), deCompResultMap.containsKey(pathCheckList14.get(idx)) ? deCompResultMap.get(pathCheckList14.get(idx)) : 0);
				checkResultMap.put(pathCheckList24.get(idx), deCompResultMap.containsKey(pathCheckList24.get(idx)) ? deCompResultMap.get(pathCheckList24.get(idx)) : 0);
				checkResultMap.put(pathCheckList34.get(idx), deCompResultMap.containsKey(pathCheckList34.get(idx)) ? deCompResultMap.get(pathCheckList34.get(idx)) : 0);
				checkResultMap.put(pathCheckList44.get(idx), deCompResultMap.containsKey(pathCheckList44.get(idx)) ? deCompResultMap.get(pathCheckList44.get(idx)) : 0);

				checkResultMap.put(pathCheckList15.get(idx), deCompResultMap.containsKey(pathCheckList15.get(idx)) ? deCompResultMap.get(pathCheckList15.get(idx)) : 0);
				checkResultMap.put(pathCheckList25.get(idx), deCompResultMap.containsKey(pathCheckList25.get(idx)) ? deCompResultMap.get(pathCheckList25.get(idx)) : 0);
				checkResultMap.put(pathCheckList35.get(idx), deCompResultMap.containsKey(pathCheckList35.get(idx)) ? deCompResultMap.get(pathCheckList35.get(idx)) : 0);
				checkResultMap.put(pathCheckList45.get(idx), deCompResultMap.containsKey(pathCheckList45.get(idx)) ? deCompResultMap.get(pathCheckList45.get(idx)) : 0);
				
				String _tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(pathCheckList16.get(idx)), pathCheckList16.get(idx));
				checkResultMap.put(pathCheckList16.get(idx), deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
				_tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(pathCheckList26.get(idx)), pathCheckList26.get(idx));
				checkResultMap.put(pathCheckList26.get(idx), deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
				_tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(pathCheckList36.get(idx)), pathCheckList36.get(idx));
				checkResultMap.put(pathCheckList36.get(idx), deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
				_tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(pathCheckList46.get(idx)), pathCheckList46.get(idx));
				checkResultMap.put(pathCheckList46.get(idx), deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
				
				idx ++;
			}

			
			int gridIdx = 0;
			ArrayList<String> gValidIdxlist = new ArrayList<>();
			HashMap<String,Object> gFileCountMap = new HashMap<>();
			boolean separatorErrFlag = false;
			
			log.info("[API] VERIFY Path Check START -----------------");
			
			for(String gridPath : gridFilePaths){
				if(!separatorErrFlag) {
					separatorErrFlag = gridPath.contains("\\") ? true : false;
				}
				
				//사용자가 * 입력했을때
				if(!gridPath.trim().equals("/*") && !gridPath.trim().equals("/")){
					if(gridPath.endsWith("*")) {
						gridPath = gridPath.substring(0, gridPath.length()-1);
					}
					if(gridPath.startsWith(".")) {
						gridPath = gridPath.substring(1, gridPath.length());
					}
					// 앞뒤 path구분 제거
					if(gridPath.endsWith("/")) {
						gridPath = gridPath.substring(0, gridPath.length()-1);
					}
					if(gridPath.startsWith("/")) {
						gridPath = gridPath.substring(1);
					}
					
					int gFileCount = 0;
					
					/*
					 * SUB_STEP 1. verify 결과 배열을 받아온 grid filepath와 비교하여 실제로 그 path가 존재하는지 확인후 
					 * 존재하지 않을 경우 grid index 저장 
					 */
					boolean resultFlag = false;
					
					if(checkResultMap.containsKey(gridPath)) {
						resultFlag = true;
						gFileCount = checkResultMap.get(gridPath);
					}
					
					if(!resultFlag) {//path가 존재하지않을 때
						gValidIdxlist.add(gridComponentIds.get(gridIdx));
					} else {//path가 존재할 때
						// file을 직접 비교하는 경우 count되지 않기 때문에, 1로 고정
						// resultFlag == true 인경우는 존재하기 해당 path or file 대상이 존재한다는 의미이기 때문에 0이 될 수 없다.
						if(gFileCount == 0) {
							gFileCount = 1;
						}
						gFileCountMap.put(gridComponentIds.get(gridIdx), Integer.toString(gFileCount));
					}
				} else {
					gFileCountMap.put(gridComponentIds.get(gridIdx), Integer.toString(allFileCount));
				}
				
				gridIdx++;
			}
			
			log.info("[API] VERIFY Path Check END -----------------");
			
			//STEP 4 : README 파일 존재 유무 확인(README 여러개 일경우도 생각해야함 ---차후)
			
			// depth가 낮은 readme 파일을 구하기 위해 sort
			if(packagingFileIdx == 1){ // packageFile에서 readMe File은 첫번째 file에서만 찾음.
//				List<String> sortList = new ArrayList<>(deCompResultMap.keySet());
				Collections.sort(readmePathList, new Comparator<String>() {

					@Override
					public int compare(String arg1, String arg2) {
						if(arg1.split("\\/").length > arg2.split("\\/").length) {
							return 1;
						} else if(arg1.split("\\/").length < arg2.split("\\/").length) {
							return -1;
						} else {
							return arg1.compareTo(arg2);
						}
					};
				});
				
//				String lastReadmeFilePath = "";
				for(String r : readmePathList) {
					String _upperPath = avoidNull(r).toUpperCase();
					
					if(_upperPath.endsWith("/")) {
						continue;
					}
					
//					String _currentReadmeFilePath = _upperPath.indexOf("/") < 0 ? _upperPath : _upperPath.substring(0,_upperPath.lastIndexOf("/"));
//					
//					if(!lastReadmeFilePath.equals(_currentReadmeFilePath)) {
//						if(!isEmpty(readmePath)) {
//							break;
//						}
//						
//						lastReadmeFilePath = _currentReadmeFilePath;
//					}
					
					if(_upperPath.indexOf("/") > -1) {
						_upperPath = _upperPath.substring(_upperPath.lastIndexOf("/"), _upperPath.length());
					}
					
					if(_upperPath.indexOf("README") > -1){
						String _readmePath = r.replaceAll("\\n", "");
						
						int afterDepthCnt = StringUtils.countMatches(_readmePath, "/");
						int beforeDepthCnt = StringUtils.countMatches(readmePath, "/");
						if(isEmpty(readmePath) || beforeDepthCnt > afterDepthCnt) {
							readmePath = _readmePath;
						}
					}
				}
			}

			
			String readmeFileName = "";
			//STEP 6 : README 파일 내용 출력
			if(!StringUtil.isEmpty(readmePath)){
				if(readmePath.indexOf("*") > -1){
					readmePath = readmePath.substring(0, readmePath.length()-1);
				}
				
				readmeFileName = readmePath;
				
				if(readmeFileName.indexOf("/") > -1) {
					readmeFileName = readmeFileName.substring(readmeFileName.lastIndexOf("/") + 1);
				}
				
				if(readmePath.indexOf(" ") > -1) {
					log.info("do space replase ok");
					
					readmePath = readmePath.replaceAll(" ", "*");
				}
				
				log.info("[API] readmePath : " + readmePath);
				log.info("[API] readmeFileName : " + readmeFileName);
				log.info("[API] VERIFY Copy Readme file START -----------------");
				log.info("[API] VERIFY README MV PATH : " + VERIFY_PATH_DECOMP +"/" + prjId +"/" + readmePath);
				
				if(isChangedPackageFile){
					ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "cp "+VERIFY_PATH_DECOMP +"/" + prjId +"/" + readmePath+ " " + VERIFY_PATH_OUTPUT +"/" + prjId +"/"});
				}
				
				log.info("[API] VERIFY Copy Readme file END -----------------");
			}
			
			//STEP 7 : README 파일 내용 DB 에 저장
			if(doUpdate && packagingFileIdx == 1) {
				log.debug("[API] VERIFY readme 등록");
				
				project.put("prjId", prjId);
				project.put("readmeFileName", readmeFileName);
				project.put("readmeYn", StringUtil.isEmpty(readmeFileName) ? CoConstDef.FLAG_NO : CoConstDef.FLAG_YES);
			
				registReadmeContent(project);
				
				log.debug("[API] VERIFY readme 등록 완료");
			}
			
			//STEP 8 : Verify 동작 후 Except File Result DB 저장
			log.info("[API] VERIFY Read exceptFileContent file START -----------------");
			
			exceptFileContent = CommonFunction.getStringFromFile(VERIFY_PATH_OUTPUT +"/"+prjId+"/except_file_result", VERIFY_PATH_DECOMP +"/" + prjId +"/", checkExceptionWordsList, checkExceptionIgnoreWorksList);
			
			log.info("[API] VERIFY Read exceptFileContent file END -----------------");
			
			// 2017.03.23 yuns contents 용량이 너무 커서 DB로 관리하지 않음 (flag만 처리, empty여부로 체크하기 때문에 내용이 있을 경우 "Y" 만 등록
			project.put("exceptFileContent", !isEmpty(exceptFileContent) ? CoConstDef.FLAG_YES : "");
			project.put("verifyFileContent", !isEmpty(verify_chk_list) ? CoConstDef.FLAG_YES : "");
			
			if(doUpdate) {
				registVerifyContents(project);
			}
			
			log.debug("[API] VERIFY 파일내용 등록 완료");
			
			// 서버 디렉토리를 replace한 내용으로 새로운 파일로 다시 쓴다.
			if(!isEmpty(exceptFileContent)) {
				log.info("[API] VERIFY writhFile exceptFileContent file START -----------------");
				
				FileUtil.writeFile(VERIFY_PATH_OUTPUT +"/" + prjId, CoConstDef.PACKAGING_VERIFY_FILENAME_PROPRIETARY, exceptFileContent.replaceAll(VERIFY_PATH_DECOMP +"/" + prjId +"/", ""));
				
				log.info("[API] VERIFY writhFile exceptFileContent file END -----------------");
			}
			
			if(!isEmpty(verify_chk_list)) {
				log.info("[API] VERIFY writhFile verify_chk_list file START -----------------");
				
				FileUtil.writeFile(VERIFY_PATH_OUTPUT +"/" + prjId, CoConstDef.PACKAGING_VERIFY_FILENAME_FILE_LIST, verify_chk_list.replaceAll(VERIFY_PATH_DECOMP +"/" + prjId +"/", ""));
				
				log.info("[API] VERIFY writhFile verify_chk_list file END -----------------");
			}
			
			resCd="10";
			if(separatorErrFlag) {
				resMsg = getMessage("verify.path.error");
			} else {
				resMsg= getMessage(gValidIdxlist.isEmpty() ? "msg.common.success" : "msg.common.valid");
			}
			
			resMap.put("verifyValid", gValidIdxlist);
			resMap.put("verifyValidMsg", "path not found.");
			resMap.put("fileCounts", gFileCountMap);
			resMap.put("verifyReadme", readmeFileName);
			resMap.put("verifyCheckList", !isEmpty(verify_chk_list) ? CoConstDef.FLAG_YES : "");
			resMap.put("verifyProprietary", !isEmpty(exceptFileContent) ? CoConstDef.FLAG_YES : "");
			
			//path not found.가 1건이라도 있으면 status_verify_yn의 flag는 N으로 저장함.
			// packagingFileId, filePath는 1번만 저장하며, gValidIdxlist의 값때문에 마지막 fileSeq일때 저장함.
			if(doUpdate && packagingFileIdx == fileSeqs.size()) {
				// verify 버튼 클릭시 file path를 저장한다.
				if(gridComponentIds != null && !gridComponentIds.isEmpty()) {
					int seq = 0;
					
					for(String s : gridComponentIds){
						Map<String, Object> param = new HashMap<>();
						param.put("componentId", s);
						param.put("filePath", gridFilePaths.get(seq++));
						
						apiProjectMapper.updateVerifyFilePath(param);
					}
				}
				
				{						
					Map<String, Object> prjParam = new HashMap<>();
					prjParam.put("prjId", prjId);
					prjParam.put("packageFileId", fileSeqs.get(0));
					
					if(prjInfo.containsKey("destributionStatus") && prjInfo.get("destributionStatus") != null && !("").equals(prjInfo.get("destributionStatus"))){
						prjParam.put("statusVerifyYn", "C");
					} else {
						prjParam.put("statusVerifyYn", CoConstDef.FLAG_YES);
					}
					
					apiProjectMapper.updatePackageFile2(prjParam);
				}
				
				
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
				commHisBean.setReferenceId(prjId); commHisBean.setContents(packagingComment);
				  
				commentService.registComment(commHisBean);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			resCd="20";
			resMsg="process failed. (server error)";
		} finally {
			try {
				if(isChangedPackageFile){
					ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "find " + VERIFY_PATH_DECOMP + " -maxdepth 1 -name "+prjId+" -type d -exec rm -rf {} \\;"});
				}
				
				log.info("[API] VERIFY delete decomp file END -----------------");
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
			}
		}
		
		resMap.put("resCd", resCd);
		resMap.put("resMsg", resMsg);
		
		log.debug("[API] verify 처리 완료 resCd : " + resCd);
		log.debug("[API] verify 처리 완료 resMsg : " + resMsg);
		
		return resMap;
	}

	private void registVerifyContents(Map<String, Object> project) {
		apiProjectMapper.updateVerifyContents(project);
	}

	private void registReadmeContent(Map<String, Object> project) {
		apiProjectMapper.updateReadmeContent(project);
	}

	private Map<String, Object> getProjectBasicInfo(String prjId) {
		Map<String, Object> param = new HashMap<>();
		param.put("prjId", prjId);
		
		return apiProjectMapper.selectProjectMaster2(param);
	}

	@Override
	public void updateVerifyFileCount(ArrayList<String> fileCounts) {
		for(String componentId : fileCounts){
			Map<String, Object> param = new HashMap<>();
			param.put("componentId", componentId);
			param.put("verifyFileCount", " ");
			
			apiProjectMapper.updateVerifyFileCount(param);
		}
	}

	@Override
	public void updateVerifyFileCount(HashMap<String, Object> fileCounts) {
		for(String componentId : fileCounts.keySet()){
			Map<String, Object> param = new HashMap<>();
			param.put("componentId", componentId);
			param.put("verifyFileCount", (String) fileCounts.get(componentId));
			
			apiProjectMapper.updateVerifyFileCount(param);
		}
	}
	
	private String addDecompressionRootPath(String path, boolean flag, String val) {
		return flag ? val : path + "/" + val;
	}

	private Map<String, Integer> setAddFileCount(Map<String, Integer> deCompResultMap, String url, int fileCnt) {
		try {
			url = url.substring(0, url.lastIndexOf("/"));
			
			int pFileCnt = deCompResultMap.get(url);
			deCompResultMap.put(url, fileCnt + pFileCnt);
			
			if(deCompResultMap.get(url.substring(0, url.lastIndexOf("/"))) != null){
				setAddFileCount(deCompResultMap, url, fileCnt);
			}
			
			return deCompResultMap;
		} catch (Exception e) {
			return deCompResultMap;
		}	
	}

	private List<String> sortByValue(Map<String, Integer> map) throws Exception{
		List<String> list = new ArrayList<String>();
        list.addAll(map.keySet());
         
        Collections.sort(list, new Comparator<Object>(){
            public int compare(Object o1,Object o2){
                int o1_depth = o1.toString().split("/").length;
                int o2_depth = o2.toString().split("/").length;
                
                return o1_depth-o2_depth;
            }
        });
        
        return list;
	}

	@Override
	public List<String> getPackageFileList(String prjId) {
		Map<String, Object> packageFile = apiProjectMapper.selectPackageFileList(prjId);
		List<String> packageFileList = new ArrayList<>();
		
		if (packageFile.containsKey("packageFileId")) {
			if(packageFile.get("packageFileId") != null && !("").equals(Integer.toString((int) packageFile.get("packageFileId")))) {
				packageFileList.add(Integer.toString((int) packageFile.get("packageFileId")));
			}
		}
		
		if (packageFile.containsKey("packageFileId2")) {
			if(packageFile.get("packageFileId2") != null && !("").equals(Integer.toString((int) packageFile.get("packageFileId2")))) {
				packageFileList.add(Integer.toString((int) packageFile.get("packageFileId2")));
			}
		}
		
		if (packageFile.containsKey("packageFileId3")) {
			if(packageFile.get("packageFileId3") != null && !("").equals(Integer.toString((int) packageFile.get("packageFileId3")))) {
				packageFileList.add(Integer.toString((int) packageFile.get("packageFileId3")));
			}
		}
		
		return packageFileList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registBom(String prjId, String merge) {
		loadOssInfo();
		loadLicenseInfo();
		
		// delete component
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("referenceId", (String) prjId);
		paramMap.put("referenceDiv", (String) CoConstDef.CD_DTL_COMPONENT_ID_BOM);
		paramMap.put("merge", (String) merge);
		paramMap.put("roleOutLicense", (String) CoCodeManager.CD_ROLE_OUT_LICENSE);
		paramMap.put("saveBomFlag", (String) CoConstDef.FLAG_YES);
		
		List<String> componentId = apiProjectMapper.selectComponentId(paramMap);
		
		// 기존 bom 정보를 모두 물리삭제하고 다시 등록한다.
		if(componentId.size() > 0){
			for (int i = 0; i < componentId.size(); i++) {
				apiProjectMapper.deleteOssComponentsLicense(componentId.get(i));
			}
			
			apiProjectMapper.deleteOssComponents(paramMap);
		}
		
		HashMap<String, Object> mergeListMap = getIdentificationGridList(paramMap);
		
		if(mergeListMap != null && mergeListMap.get("rows") != null) {
			for(HashMap<String, Object> bean : (List<HashMap<String, Object>>) mergeListMap.get("rows")) {
				
				if((bean.get("ossName") == null || "-".equals((String) bean.get("ossName")))) {
					List<HashMap<String, Object>> ocll = (List<HashMap<String, Object>>) bean.get("ossComponentsLicenseList");
					if (ocll.size() > 1) {
						continue;
					}
				}
				
				bean.put("refDiv", bean.get("referenceDiv"));
				bean.put("referenceDiv", CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				bean.put("refComponentId", bean.get("componentId"));
				bean.put("adminCheckYn", CoConstDef.FLAG_NO);
				bean.put("preObligationType", bean.get("obligationType"));
				if (bean.containsKey("licenseName")) {
					if(((String) bean.get("licenseName")).contains("Other")) {
						String licenseNm = (String) bean.get("licenseName");
					}
				}
				bean = findOssIdAndName(bean);
				
				// 컴포넌트 마스터 인서트
				apiProjectMapper.registBomComponents(bean);
				
				List<HashMap<String, Object>> licenseList = findOssLicenseIdAndName(bean);
				
				if (licenseList.size() > 0) {
					for(HashMap<String, Object> licenseBean : licenseList) {
						licenseBean.put("componentId", bean.get("componentId"));
						
						apiProjectMapper.registComponentLicense(licenseBean);
					}
				}
			}
		}
			
		// identification 대상이 없이 처음 저장하는 경우
		Map<String, Object> _tempPrjInfo = new HashMap<>();
		_tempPrjInfo.put("prjId", prjId);
		_tempPrjInfo = apiProjectMapper.selectProjectMaster2(_tempPrjInfo);
		
		if (_tempPrjInfo.get("identificationStatus") == null && ((String) _tempPrjInfo.get("identificationStatus")).trim().length() == 0) {
			_tempPrjInfo.put("identificationStatus", CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			
			apiProjectMapper.updateIdentifcationProgress(_tempPrjInfo);
		}
	}

	private void loadOssInfo() {
		try {
			List<HashMap<String, Object>> list = apiOssMapper.getOssInfoAll();
			List<HashMap<String, Object>> listNick = apiOssMapper.getOssInfoAllWithNick();
			List<HashMap<String, Object>> nickNameList = apiOssMapper.getOssAllNickNameList();
			HashMap<String, String[]> nickNameMap = new HashMap<>();
			HashMap<String, HashMap<String, Object>> _ossMap = new HashMap<>();
			HashMap<String, String> _ossNamesMap = new HashMap<>();
			
			if(nickNameList != null) {
				for(Map<String, Object> bean : nickNameList) {
					if(bean.get("ossNickname") != null) {
						nickNameMap.put((String) bean.get("ossName"), ((String) bean.get("ossNickname")).split(","));
					}
				}
			}
			
			if(list != null) {
				List<HashMap<String, Object>> licenseBeanList = new ArrayList<>();
				
				for(HashMap<String, Object> bean : list) {
					
					HashMap<String, Object> licenseBean = new HashMap<>();
					HashMap<String, Object> targetBean = null;
					String key = (String) bean.get("ossName") +"_"+ avoidNull((String) bean.get("ossVersion")); // oss name을 nick name으로 가져온다.
					key = key.toUpperCase();
					
					if(_ossMap.containsKey(key)) {
						targetBean = _ossMap.get(key);
					} else {
						targetBean = bean;
						
						if(nickNameMap.containsKey(targetBean.get("ossNameTemp"))) {
							targetBean.put("ossNicknames", nickNameMap.get(targetBean.get("ossNameTemp")));
						}
					}
					
					HashMap<String, Object> subBean = new HashMap<>();
					subBean.put("ossId", bean.get("ossId"));
					subBean.put("licenseId", bean.get("licenseId"));
					subBean.put("licenseName", bean.get("licenseName"));
					subBean.put("licenseType", bean.get("ossLicenseType"));
					subBean.put("ossLicenseIdx", bean.get("ossLicenseIdx"));
					subBean.put("ossLicenseComb", bean.get("ossLicenseComb"));
					subBean.put("ossLicenseText", bean.get("ossLicenseText"));
					subBean.put("ossCopyright", bean.get("ossCopyright"));
					
					targetBean.put("licenseType", bean.get("ossLicenseType"));
					
					licenseBean.put(key, subBean);
					licenseBeanList.add(licenseBean);			
					
					targetBean.put("ossLicenses", makeOssLicense(key, licenseBeanList));
									
					if(_ossMap.containsKey(key)) {
						_ossMap.replace(key, targetBean);
					} else {
						_ossMap.put(key, targetBean);
					}
					
					if(!_ossNamesMap.containsKey(((String) bean.get("ossName")).toUpperCase())) {
						_ossNamesMap.put(((String) bean.get("ossName")).toUpperCase(), (String) bean.get("ossName"));
					}
				}
			}
			
			HashMap<String, HashMap<String, Object>> _idMasterMap = new HashMap<>();
			for(HashMap<String, Object> bean : _ossMap.values()) {
				if(!_idMasterMap.containsKey(bean.get("ossId"))) {
					_idMasterMap.put(Integer.toString((int) bean.get("ossId")), bean);
				}
			}
			
			OSS_INFO_BY_ID = _idMasterMap;
			
			if(listNick != null) {

				for(HashMap<String, Object> bean : listNick) {
					String key = (String) bean.get("ossName") +"_"+ avoidNull((String) bean.get("ossVersion")); // oss name을 nick name으로 가져온다.
					String ossNickNameKey = ((String) bean.get("ossName")).toUpperCase();

					if(!_ossNamesMap.containsKey(ossNickNameKey)) {
						_ossNamesMap.put(ossNickNameKey, (String) bean.get("ossNameTemp"));
					}
					
					String sourceKey = ((String) bean.get("ossNameTemp") + "_" + avoidNull((String) bean.get("ossVersion"))).toUpperCase();
					HashMap<String, Object> sourceBean = _ossMap.get(sourceKey);
					
					bean.put("licenseDiv", sourceBean.get("licenseDiv"));
					bean.put("downloadLocation", sourceBean.get("downloadLocation"));
					bean.put("downloadLocationGroup", sourceBean.get("downloadLocationGroup"));
					bean.put("homepage", sourceBean.get("homepage"));
					bean.put("summaryDescription", sourceBean.get("summaryDescription"));
					bean.put("attribution", sourceBean.get("attribution"));
					bean.put("copyright", sourceBean.get("copyright"));
					bean.put("cvssScore", sourceBean.get("cvssScore"));
					bean.put("cveId", sourceBean.get("cveId"));
					bean.put("vulnYn", sourceBean.get("vulnYn"));
					bean.put("vulnRecheck", sourceBean.get("vulnRecheck"));
					bean.put("vulnDate", sourceBean.get("vulnDate"));
					bean.put("licenseType", sourceBean.get("licenseType"));
					bean.put("ossLicenses", sourceBean.get("ossLicenses"));
					bean.put("ossType", sourceBean.get("ossType"));
					bean.put("multiLicenseFlag", sourceBean.get("multiLicenseFlag"));
					bean.put("dualLicenseFlag", sourceBean.get("dualLicenseFlag"));
					bean.put("versionDiffFlag", sourceBean.get("versionDiffFlag"));

					_ossMap.put(key.toUpperCase(), bean);
				}
			
			}
			
			if(!_ossMap.isEmpty()) {
				OSS_INFO_UPPER = _ossMap;
			}
		} catch(Exception e) {
        	log.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<HashMap<String, Object>> makeOssLicense(String key, List<HashMap<String, Object>> licenseBeanList) {
		List<HashMap<String, Object>> ossLicenses = new ArrayList<>();
		
		for (HashMap<String, Object> license : licenseBeanList) {
			if (license.containsKey(key)) {
				ossLicenses.add((HashMap<String, Object>) license.get(key));
			}
		}
		
		if (ossLicenses.size() > 1) {
			Collections.sort(ossLicenses, new Comparator<HashMap<String, Object>>() {
				@Override
				public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
					return Integer.toString((int) o1.get("ossLicenseIdx")).compareTo(Integer.toString((int) o2.get("ossLicenseIdx")));
				}
			});
		}
		
		return ossLicenses;
	}
	
	private void loadLicenseInfo() {
		try {
            List<HashMap<String, Object>> list = apiProjectMapper.getLicenseInfoInit();
            List<HashMap<String, Object>> nickList = apiProjectMapper.getLicenseInfoInitNick();
            
            if(list == null) {
                throw new RuntimeException("SYSTEM ERR GET CODE LICENSE MASTER INFO");
            }
            
            HashMap<String, HashMap<String, Object>> license_info_map = new HashMap<>();
            HashMap<String, HashMap<String, Object>> license_info_upper_map = new HashMap<>();
            HashMap<String, HashMap<String, Object>> license_info_by_id_map = new HashMap<>();
            
            for(HashMap<String, Object> vo : list) {
            	if(vo.containsKey("licenseNicknameStr")) {
            		List<String> licenseNicknameList = new ArrayList<String>();
            		for(String nick : ((String) vo.get("licenseNicknameStr")).split("\\|")) {
            			licenseNicknameList.add(nick);
            		}
            		vo.put("licenseNicknameList", licenseNicknameList);
            	}
            	
            	if(vo.containsKey("restriction")) {
            		if(!("").equals((String) vo.get("restriction"))) {
                		vo.put("restrictionStr", licenseRestrictionList((String) vo.get("restriction")));
            		}
            	}
            	
            	license_info_map.put((String) vo.get("licenseName"),vo);
            	license_info_upper_map.put(((String) vo.get("licenseName")).toUpperCase(),vo);
            	
            	//SHORT_IDENTIFIER
            	if(vo.containsKey("shortIdentifier")) {
            		if(!("").equals(vo.get("shortIdentifier"))) {
            			if(!license_info_map.containsKey(vo.get("shortIdentifier"))) {
                        	license_info_map.put((String) vo.get("shortIdentifier"), vo);
                		}
                		
                		if(!license_info_upper_map.containsKey(((String) vo.get("shortIdentifier")).toUpperCase())) {
                			license_info_upper_map.put(((String) vo.get("shortIdentifier")).toUpperCase(), vo);
                		}
            		}
            	}
            	
            	license_info_by_id_map.put(Integer.toString((int) vo.get("licenseId")), vo);
            }
            
            for(HashMap<String, Object> vo : nickList) {
            	
            	HashMap<String, Object> sourceBean = license_info_by_id_map.get(Integer.toString((int) vo.get("licenseId")));
            	
            	if(vo.containsKey("licenseNicknameList")) {
            		vo.put("licenseNicknameList", sourceBean.get("licenseNicknameList"));
            	}
            	
            	if(vo.containsKey("restrictionStr")) {
            		vo.put("restrictionStr", sourceBean.get("restrictionStr"));
            	}
            	            	
            	license_info_map.put((String) vo.get("licenseName"),vo);
            	license_info_upper_map.put(((String) vo.get("licenseName")).toUpperCase(),vo);
            }
            
            if(!license_info_map.isEmpty()) {
            	LICENSE_INFO = license_info_map;
            }
            
            if(!license_info_upper_map.isEmpty()) {
            	LICENSE_INFO_UPPER = license_info_upper_map;
            }
            
            if(!license_info_by_id_map.isEmpty()) {
            	LICENSE_INFO_BY_ID = license_info_by_id_map;
            }
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
	}
	
	private String licenseRestrictionList(String restrictionStr) {
		String returnStr = "";
		
		if(!isEmpty(restrictionStr)) {
			String restrictionArr[] = restrictionStr.split(",");
			List<String> restrictionList = new ArrayList<>();
			
			for(int i = 0 ; i < restrictionArr.length ; i++){
				restrictionList.add(restrictionArr[i]);
			}
			
            for(String str : restrictionList){
            	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
            }
		}
		
		return returnStr;
	}
	
	private HashMap<String, Object> getIdentificationGridList(Map<String, Object> paramMap) {
		return getIdentificationGridList(paramMap, false);
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Object> getIdentificationGridList(Map<String, Object> paramMap, boolean multiUIFlag) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<HashMap<String, Object>> list = null;
		List<HashMap<String, Object>> listLicense = null;
		
		paramMap.put("roleOutLicense", CoCodeManager.CD_ROLE_OUT_LICENSE);
		
		if(CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST != null && !CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST.isEmpty()) {
			paramMap.put("roleOutLicenseIdList", CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST);
		}
		
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals((String) paramMap.get("referenceDiv"))) {
			Map<String, String> obligationTypeMergeMap = new HashMap<>();
			String reqMergeFlag = (String) paramMap.get("merge");
			
			list = apiProjectMapper.selectMergeBomList(paramMap);
			
			// bom merge 버튼을 클릭하고, 표시대상이 있을 경우, 기존에 저장되어 있는 내용을 취득한다.
			// need check의 저장값을 유지하기 위함
			if(CoConstDef.FLAG_YES.equals(reqMergeFlag) && list != null && !list.isEmpty()) {
				paramMap.put("merge", CoConstDef.FLAG_NO);
				List<HashMap<String, Object>> bomBeforeList = apiProjectMapper.selectMergeBomList(paramMap);
				
				if(bomBeforeList != null) {
					for(Map<String, Object> _orgIdentificationBean : bomBeforeList) {
						obligationTypeMergeMap.put((String) _orgIdentificationBean.get("refComponentId"), (String) _orgIdentificationBean.get("obligationType"));
					}
				}
			}
			
			Map<String, Map<String, Object>> batMergeSrcMap = new HashMap<>();
			Map<String, Map<String, Object>> batMergePartnerMap = new HashMap<>();
			
			for (Map<String, Object> ll : list) {
				ll.put("licenseId", CommonFunction.removeDuplicateStringToken((String) ll.get("licenseId"), ","));
				ll.put("licenseName", CommonFunction.removeDuplicateStringToken((String) ll.get("licenseName"), ","));
  				ll.put("copyrightText", ll.get("copyrightText"));
				ll.put("roleOutLicense", CoCodeManager.CD_ROLE_OUT_LICENSE);
				
				listLicense = apiProjectMapper.selectBomLicense(Integer.toString((int) ll.get("componentId")));
				ll.put("ossComponentsLicenseList", listLicense);
				ll.put("obligationLicense", CoConstDef.FLAG_YES.equals(ll.get("adminCheckYn")) ? CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK : checkObligationSelectedLicense(listLicense));
				
				if(CoConstDef.FLAG_YES.equals(reqMergeFlag)) {
					if(obligationTypeMergeMap.containsKey(Integer.toString((int) ll.get("componentId")))) {
						ll.put("obligationType", obligationTypeMergeMap.get(Integer.toString((int) ll.get("componentId"))));
					} else {
						ll.put("obligationType", ll.get("obligationLicense"));
					}
				}
				
				// grouping 된 file path를 br tag로 변경
				ll.put("filePath", lineReplaceToBR((String) ll.get("filePath")));
				
				if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals((String) ll.get("referenceDiv"))) {
					if(!batMergeSrcMap.containsKey(((String) ll.get("ossName")).toUpperCase())) {
						batMergeSrcMap.put(((String) ll.get("ossName")).toUpperCase(), ll);
					} else if(StringUtil.compareTo(((String) ll.get("ossVersion")), (String) batMergeSrcMap.get(((String) ll.get("ossName")).toUpperCase()).get("ossVersion")) > 0) {
						batMergeSrcMap.replace(((String) ll.get("ossName")).toUpperCase(), ll);
					}
				} else if(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(((String) ll.get("referenceDiv")))) {
					if(!batMergePartnerMap.containsKey(((String) ll.get("ossName")).toUpperCase())) {
						batMergePartnerMap.put(((String) ll.get("ossName")).toUpperCase(), ll);
					} else if (StringUtil.compareTo(((String) ll.get("ossVersion")), (String) batMergePartnerMap.get(((String) ll.get("ossName")).toUpperCase()).get("ossVersion")) > 0) {
						batMergePartnerMap.replace(((String) ll.get("ossName").toString()), ll);
					}
				}
				
				// oss Name은 작성하고, oss Version은 작성하지 않은 case경우 해당 분기문에서 처리
				if(ll.get("cveId") == null 
						&& isEmpty((String) ll.get("ossVersion")) 
						&& !isEmpty((String) ll.get("cvssScoreMax"))
						&& !("-".equals((String) ll.get("ossName")))){ 
					String[] cvssScoreMax = ((String) ll.get("cvssScoreMax")).split("\\@");
					ll.put("cvssScore", cvssScoreMax[0]);
					ll.put("cveId", cvssScoreMax[1]);
				}
			}
			
			// bat merget
			// bat 분석 결과 중에서 oss version이 명시되지 않고, src 또는 3rd party에 동일한 oss 가 존재하는 경우
			// bat 분석 결과를 src 또는 3rd party에 merge 한다.
			List<Map<String, Object>> _list = new ArrayList<>();
			List<String> adminCheckList = new ArrayList<>();
			List<Map<String, Object>> groupList = null;
			Map<String, List<Map<String, Object>>> srcSameLicenseMap = new HashMap<>();
			List<String> egnoreList = new ArrayList<>();
			
			for (Map<String, Object> ll : list) {
				// 이미 추가된 oss의 경우
				if(egnoreList.contains((Integer.toString((int) ll.get("componentId"))))) {
					continue;
				}
				
				int addIdx = -1;
				
				if((String) ll.get("ossName") != null && !("").equals((String) ll.get("ossName"))) {
					String mergeKey = ((String) ll.get("ossName")).toUpperCase();
					// main oss로 표시되는 bat oss의 version이 명시되어 있지 않은 경우
					if(CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(((String) ll.get("referenceDiv"))) && (ll.get("mergePreDiv") == null && ("").equals((String) ll.get("mergePreDiv"))) 
							&& (ll.get("ossVersion") == null && ("").equals((String) ll.get("ossVersion")))) {
						Map<String, Object> refBean = null;
						
						if( batMergeSrcMap.containsKey(mergeKey)) {
							// bat => src
							refBean = batMergeSrcMap.get(mergeKey);
							
							// 설정된 license가 상이하고, bat와 동일한 license가 src에 존재한다면 (최상위 버전이 아닌경우)
							// continue하고 다음 loop에서 merge
							if(!isSameLicense( (List<Map<String, Object>>) refBean.get("ossComponentsLicenseList"), (List<Map<String, Object>>) ll.get("ossComponentsLicenseList"))) {
								String ossNameAndVersion = findBatOssOtherVersionWithLicense(ll, refBean, list);
								
								if(!isEmpty(ossNameAndVersion)) {
									List<Map<String, Object>> _batList = null;
									
									if(srcSameLicenseMap.containsKey(ossNameAndVersion)) {
										_batList = srcSameLicenseMap.get(ossNameAndVersion);
										_batList.add(ll);
										srcSameLicenseMap.replace(ossNameAndVersion, _batList);
									} else {
										_batList = new ArrayList<>();
										_batList.add(ll);
										srcSameLicenseMap.put(ossNameAndVersion, _batList);
									}
									
									continue;
								}
							}
							
							ll.put("ossId", refBean.get("ossId"));
							ll.put("ossName", refBean.get("ossName"));
							ll.put("ossVersion", refBean.get("ossVersion"));
							ll.put("ossComponentsLicenseList", refBean.get("ossComponentsLicenseList"));
							
							// 순서 정렬
							addIdx = findOssAppendIndex(CoConstDef.CD_DTL_COMPONENT_ID_SRC, refBean.get("componentId"), list);
							
							if(addIdx > -1) {
								addIdx = addIdx +1;
								ll.put("groupingColumn", (String) refBean.get("ossName") + (String) refBean.get("ossVersion"));
							}					
						} else if( batMergePartnerMap.containsKey(mergeKey)) {
							// 3rd => bat
							refBean = batMergePartnerMap.get(mergeKey);
							
							// 3rd에 같은 그룹으로 묶여 있는 모든 oss list를 취득
							ll.put("groupingColumn", (String) refBean.get("ossName") + (String) refBean.get("ossVersion"));
							ll.put("ossName", refBean.get("ossName"));
							ll.put("ossVersion", refBean.get("ossVersion"));
							ll.put("ossId", refBean.get("ossId"));
							
							// bin 에 누락된 정보를 3rd의 첫번재 row에서 채워 넣는다.
							// DOWNLOAD_LOCATION
							if(ll.get("downloadLocation") == null) {
								ll.put("downloadLocation", refBean.get("downloadLocation"));
							}
							
							// HOMEPAGE
							if(ll.get("homepage") == null) {
								ll.put("homepage", refBean.get("homepage"));
							}
							
							// license 정보
							ll.put("ossComponentsLicenseList", refBean.get("ossComponentsLicenseList"));
							ll.put("licenseName", refBean.get("licenseName"));
							ll.put("copyrightText", refBean.get("copyrightText"));
							
							ll.put("obligationLicense", refBean.get("obligationLicense"));
							ll.put("obligationType", refBean.get("obligationType"));
							
							// 3rd party의 우선순위가 가장 낮기 때문에, 복수건을 취득하는 경우는 없지만, 기능 확장을 고려해서 list 형으로 반환
							groupList = findOssGroupList(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, (String) batMergePartnerMap.get(mergeKey).get("ossName"), (String) batMergePartnerMap.get(mergeKey).get("ossVersion"), list);
							
							if(groupList != null && !groupList.isEmpty()) {
								for(Map<String, Object> _groupBean : groupList) {
									egnoreList.add((String) _groupBean.get("componentId"));
								}
							}
						}
					} 
					// 3rd party의 경우, bat에 동일한 oss가 없을 경우만 추가 (정렬)
					else if(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals((String) ll.get("referenceDiv")) && isEmpty((String) ll.get("mergePreDiv")) ) {
						if(existsBatOSS((String) ll.get("ossName"), list)) {
							continue;
						}
					}
				}
				// License Restriction 저장
				ll.put("restriction", licenseRestrictionListById((String) ll.get("licenseId")));

				if(addIdx > -1) {
					if(addIdx > _list.size() -1) {
						_list.add(ll);
					} else {
						_list.add(addIdx, ll);
					}
				} else {
					_list.add(ll);
					
					if(groupList != null && !groupList.isEmpty()) {
						_list.addAll(groupList);
					}
				}
				
				if(CoConstDef.FLAG_YES.equals((String) ll.get("adminCheckYn"))) {
					adminCheckList.add((String) ll.get("componentId"));
				}
			}
			
			// src oss중에서 bat와 merge할 수 있는 동일한 oss에 최신 version 외 라이선스까지 동일한 bat가 존재하는 경우
			if(!srcSameLicenseMap.isEmpty()) {
				List<Map<String, Object>> _tmp = new ArrayList<>();
				
				for(Map<String, Object> bean : _list) {
					_tmp.add(bean);
					String _key = (String) bean.get("ossName") + "-" + avoidNull((String) bean.get("ossVersion"));
					
					if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals((String) bean.get("referenceDiv")) && srcSameLicenseMap.containsKey(_key)) {
						for(Map<String, Object> _mergeBean : srcSameLicenseMap.get(_key)) {
							_mergeBean.put("ossId", bean.get("ossId"));
							_mergeBean.put("ossName", bean.get("ossName"));
							_mergeBean.put("ossVersion", bean.get("ossVersion"));
							_mergeBean.put("ossComponentsLicenseList", bean.get("ossComponentsLicenseList"));
							_mergeBean.put("groupingColumn", bean.get("groupingColumn")); // 순서 정렬
							
							_tmp.add(_mergeBean);
						}
					}
				}
				
				_list = _tmp;
			}

			map.put("rows", _list);
			
			if(adminCheckList.size() > 0) {
				map.put("adminCheckList", adminCheckList);
			}
		}
		
		return map;
	}
	
	private String checkObligationSelectedLicense(List<HashMap<String, Object>> listLicense) {
		String rtnVal = "";
		
		if(listLicense != null) {
			for(Map<String, Object> bean : listLicense) {
				if(!CoConstDef.FLAG_YES.equals(bean.get("excludeYn"))) {
					// 확인 가능한 라이선스 중에서만 obligation 대상으로 한다.
					if(bean.containsKey("licenseName")) {
						if(LICENSE_INFO_UPPER.containsKey(((String) bean.get("licenseName")).toUpperCase())) {
							Map<String, Object> license = LICENSE_INFO_UPPER.get(((String) bean.get("licenseName")).toUpperCase());
							
							if(CoConstDef.FLAG_YES.equals(license.get("obligationNeedsCheckYn"))) {
								return CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK;
							} else if(CoConstDef.FLAG_YES.equals(license.get("obligationDisclosingSrcYn"))) {
								rtnVal = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
							} else if(isEmpty(rtnVal) && CoConstDef.FLAG_YES.equals(license.get("obligationNotificationYn"))) {
								rtnVal = CoConstDef.CD_DTL_OBLIGATION_NOTICE;
							}
						}
					}
				}
			}
		}
		
		return rtnVal;
	}
	
	private static String lineReplaceToBR(String s) {
		return avoidNull(s).replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>");
	}
	
	@SuppressWarnings("unchecked")
	private String findBatOssOtherVersionWithLicense(Map<String, Object> ll, Map<String, Object> refBean, List<HashMap<String, Object>> list) {
		for(Map<String, Object> bean : list) {
			if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals((String) bean.get("referenceDiv")) 
					&& ((String) bean.get("ossName")).equals((String) refBean.get("ossName"))
					&& !((String) bean.get("ossVersion")).equals((String) refBean.get("ossVersion"))
					&& ((String) bean.get("ossName")).equals((String) ll.get("ossName"))) {
				if(isSameLicense((List<Map<String, Object>>) bean.get("ossComponentsLicenseList"), (List<Map<String, Object>>) ll.get("ossComponentsLicenseList"))) {
					return (String) bean.get("ossName") + "-" + avoidNull((String) bean.get("ossVersion"));
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isSameLicense(List<Map<String, Object>> list1, List<Map<String, Object>> list2) {
		if(list1 == null && list2 == null) {
			return false;
		}
		
		if((list1 == null && list2 != null) || (list1 != null && list2 == null)) {
			return false;
		}
		
		if(list1.size() != list2.size()) {
			return false;
		}
		
		List<String> licenseNames = new ArrayList<>();
		
		for(Map<String, Object> bean : list1) {
			Map<String, Object> liMaster = LICENSE_INFO.get(bean.get("licenseName"));
			
			if(liMaster == null) {
				licenseNames.add(avoidNull((String) bean.get("licenseName")));
			} else {
				licenseNames.add((String) liMaster.get("licenseName"));
				if(liMaster.containsKey("shortIdentifier")) {
					licenseNames.add((String) liMaster.get("shortIdentifier"));
				}
				if(liMaster.containsKey("licenseNicknameList")) {
					List<String> licenseNicknameList = (List<String>) liMaster.get("licenseNicknameList");
					for (String ln : licenseNicknameList) {
						licenseNames.add(ln);
					}
				}
			}
		}
		
		for(Map<String, Object> bean : list2) {
			if(bean.containsKey("licenseName")) {
				if(!licenseNames.contains((String) bean.get("licenseName"))) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private int findOssAppendIndex(String type, Object object, List<HashMap<String, Object>> list) {
		int idx = 0;
		
		for(Map<String, Object> bean : list) {
			if(type.equals((String) bean.get("referenceDiv")) && ((String) object).equals(bean.get("componentId"))) {
				return idx;
			}
			
			idx++;
		}
		
		return -1;
	}
	
	private List<Map<String, Object>> findOssGroupList(String type, String ossName, String ossVersion, List<HashMap<String, Object>> list) {
		String targetGroup = avoidNull(ossName) + avoidNull(ossVersion);
		List<Map<String, Object>> groupList = new ArrayList<>();
		
		for(Map<String, Object> bean : list) {
			if(type.equals((String) bean.get("referenceDiv")) && targetGroup.equalsIgnoreCase((String) bean.get("groupingColumn"))) {
				groupList.add(bean);
			}
		}
		
		return groupList;
	}
	
	private boolean existsBatOSS(String ossName, List<HashMap<String, Object>> list) {
		for(Map<String, Object> bean : list) {
			if(CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals((String) bean.get("referenceDiv")) && isEmpty((String) bean.get("mergePreDiv")) && isEmpty((String) bean.get("ossVersion"))
					&& ossName.equalsIgnoreCase((String) bean.get("ossName"))) {
				return true;
			}
		}
		
		return false;
	}
	
	private String licenseRestrictionListById(String licenseIdStr) {
		String returnStr = "";
		
		if(licenseIdStr != null) {
			String restrictionStr = "";
			String licenseIdArr[] = licenseIdStr.split(",");
			
			for(int i = 0 ; i < licenseIdArr.length ; i++) {
				Map<String, Object> license = LICENSE_INFO_BY_ID.get(licenseIdArr[i]);
				if(license != null && license.containsKey("restriction") && !("").equals(license.get("restriction"))) {
					restrictionStr += (isEmpty(restrictionStr)?"":",") + (String) license.get("restriction"); 
				}
			}
			
			String restrictionArr[] = restrictionStr.split(",");
			List<String> restrictionList = new ArrayList<>();
			List<String> distinctList = new ArrayList<String>();
			
			// String 배열 -> String 리스트
			for(int i = 0 ; i < restrictionArr.length ; i++){
				restrictionList.add(restrictionArr[i]);
			}
			
			// 중복 제거
            for(String str : restrictionList){
                if (!distinctList.contains(str)) {
                	distinctList.add(str);
                }
            }
            
            for(String str : distinctList){
            	if(!("").equals(str)) {
                	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
            	}
            }
		}
		
		return returnStr;
	}
	
	private HashMap<String, Object> findOssIdAndName(HashMap<String, Object> bean) {
		if(bean != null && bean.containsKey("ossName")) {
			if("N/A".equals((String) bean.get("ossVersion"))) {
				bean.put("ossVersion", "");
			}
			
			String findKey = ((String) bean.get("ossName")).trim() +"_";
			if(bean.containsKey("ossVersion")) {
				findKey = ((String) bean.get("ossVersion")).trim();
			}
			findKey = findKey.toUpperCase();
			Map<String, Object> masterBean = OSS_INFO_UPPER.get(findKey);
			
			if(masterBean != null) {
				bean.put("ossId", masterBean.get("ossId"));
				bean.put("ossName", masterBean.get("ossName"));
			}
		}
		
		return bean;
	}
	
	@SuppressWarnings("unchecked")
	private List<HashMap<String, Object>> findOssLicenseIdAndName(HashMap<String, Object> bean) {
		List<HashMap<String, Object>> ossComponentsLicenseList = null;
		List<HashMap<String, Object>> returnOssComponentsLicenseList = new ArrayList<>();
		// 먼저 license Id는 찾을수 있으면 모두 설정한다.
		if(bean.containsKey("ossComponentsLicenseList")) {
			ossComponentsLicenseList = (List<HashMap<String, Object>>) bean.get("ossComponentsLicenseList");
			for(HashMap<String, Object> licenseBean : ossComponentsLicenseList) {
				if (licenseBean.containsKey("licenseName")) {
					HashMap<String, Object> licenseMaster = (HashMap<String, Object>) LICENSE_INFO_UPPER.get(avoidNull(((String) licenseBean.get("licenseName")), "").trim().toUpperCase());
					
					if(licenseMaster != null) {
						licenseBean.put("licenseId", licenseMaster.get("licenseId"));
						
						if(licenseMaster.containsKey("shortIdentifier") && !("").equals(licenseMaster.get("shortIdentifier"))) {
							licenseBean.put("licenseName", licenseMaster.get("shortIdentifier"));
						}else {
							licenseBean.put("licenseName", licenseMaster.get("licenseName"));
						}	
						
						// OSS_LICENSE 에 설정값이 있는 경우 우선으로 설정하지만, 없는 경우 license master를 기준으로 설정하기 때문에 일단 여기서는 master기준으로 설정
						licenseBean.put("licenseText", licenseMaster.get("licenseText"));
					}
					
					returnOssComponentsLicenseList.add(licenseBean);
				}
			}
		}
		
		// oss에서 추가설정한 license text 및 oss copyright 설정
		if(bean.containsKey("ossId")) {
			HashMap<String, Object> ossMaster = (HashMap<String, Object>) OSS_INFO_BY_ID.get(Integer.toString((int) bean.get("ossId")));
			
			if(ossMaster != null) {
				// oss master에 등록된 licnese순서 기준으로 찾는다 (기본적으로 size가 일치 해야함, multi dual license의 경우)
				if (ossMaster.containsKey("ossLicenses")) {
					List<HashMap<String, Object>> ossLicensesList = (List<HashMap<String, Object>>) ossMaster.get("ossLicenses");
					
					if (ossLicensesList != null && ossLicensesList.size() > 0) {
						
						for(int i=0; i<ossLicensesList.size(); i++) {
							HashMap<String, Object> ossLicense = ossLicensesList.get(i);
							if(returnOssComponentsLicenseList.size() >= i+1 
									&& ossLicense.get("licenseId").equals(returnOssComponentsLicenseList.get(i).get("licenseId"))) {
								// license text
								// oss_license에 존재하는 경우만 설정
								if(ossLicense.containsKey("ossLicenseText") && ossLicense.get("ossLicenseText") != null && ((String) ossLicense.get("ossLicenseText")).length() > 0) {
									returnOssComponentsLicenseList.get(i).put("licenseText", ossLicense.get("ossLicenseText"));
								}
								
								// oss copyright
								if(ossLicense.containsKey("ossCopyright") && ossLicense.get("ossCopyright") != null && ((String) ossLicense.get("ossCopyright")).length() > 0) {
									returnOssComponentsLicenseList.get(i).put("copyrightText", ossLicense.get("ossCopyright"));
								}
							}
						}
					}
				}
			}
		}
		
		return returnOssComponentsLicenseList;
	}
}