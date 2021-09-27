/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.ApiProjectMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.ApiProjectService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.ExcelUtil;

@Service
@Slf4j
public class ApiProjectServiceImpl extends CoTopComponent implements ApiProjectService {
	@Autowired ApiProjectMapper apiProjectMapper;
	@Autowired ProjectMapper projectMapper;
	@Autowired FileService fileService;
	@Autowired ProjectService projectService;
	
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
		
		try {
			if(resultTxtBean != null) {
				resultFileId = resultTxtBean.getRegistFileId();
			}
			
			// 1) build image를 기준으로 oss data mapping (공통)
			if (!ExcelUtil.readAndroidBuildImage("BIN (Android)", true, sheet, ossReportfileId, resultFileId, reportData, errMsgList)) {
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
			String componentId = (String) li.get("componentId").toString();
			List<Map<String, Object>> listLicense = apiProjectMapper.selectBomLicense(componentId);
			
			li.replace("licenseId", licenseId);
			li.replace("licenseName", licenseName);
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
				
				String tempLicenseName = ((String) temp.get("licenseName"));
				String rtnLicenseName = ((String) rtnBean.get("licenseName"));
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
	
	@Override
	public Map<String, Object> getBomCompare(List<Map<String, Object>> beforeBomList, List<Map<String, Object>> afterBomList){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> addList = new HashMap<String, Object>();
		Map<String, Object> deleteList = new HashMap<String, Object>();
		Map<String, Object> changeList = new HashMap<String, Object>();
		
		for(Map<String, Object> before : beforeBomList) {
			String ossName = (String) before.get("ossName");
			List<Map<String, Object>> afterList = afterBomList.stream().filter(after -> ((String)after.get("ossName")).equals(ossName)).collect(Collectors.toList());
			
			if(afterList.size() == 0) {
				Map<String, Object> deleteMap = new HashMap<String, Object>();
				
				deleteMap.put("name", (String) before.get("ossName"));
				deleteMap.put("version", avoidNull((String) before.get("ossVersion"), ""));
				deleteMap.put("license", Arrays.asList(((String) before.get("licenseName")).split(",")));
				deleteList.put(getCompareKey(before), deleteMap);
			} else {
				if(!((String)afterList.get(0).get("ossVersion")).equals((String) before.get("ossVersion"))
						 || !((String)afterList.get(0).get("licenseName")).equals((String) before.get("licenseName"))) {
					Map<String, Object> changeMap = new HashMap<String, Object>();
					
					changeMap.put("name", (String) before.get("ossName"));
					changeMap.put("prev_version", avoidNull((String) before.get("ossVersion"), ""));
					changeMap.put("prev_license", Arrays.asList(((String) before.get("licenseName")).split(",")));
					changeMap.put("now_version", avoidNull((String) afterList.get(0).get("ossVersion"), ""));
					changeMap.put("now_license", Arrays.asList(((String) afterList.get(0).get("licenseName")).split(",")));
					
					changeList.put(getCompareKey(before), changeMap);
				}
			}
		}
		
		for(Map<String, Object> after : afterBomList) {
			String ossName = (String) after.get("ossName");
			int addTargetCnt = beforeBomList.stream().filter(before -> ((String)before.get("ossName")).equals(ossName)).collect(Collectors.toList()).size();
			
			if(addTargetCnt == 0) {
				Map<String, Object> addMap = new HashMap<String, Object>();
				
				addMap.put("name", (String) after.get("ossName"));
				addMap.put("version", avoidNull((String) after.get("ossVersion"), ""));
				addMap.put("license", Arrays.asList(((String) after.get("licenseName")).split(",")));
				
				addList.put(getCompareKey(after), addMap);
			}
		}
		
		// add, delete, change가 값이없으면 완전일치한 project로 판단. 
		resultMap.put("add", 	addList.values());
		resultMap.put("delete", deleteList.values());
		resultMap.put("change", changeList.values());
		
		return resultMap;
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
}