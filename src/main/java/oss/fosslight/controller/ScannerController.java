/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.MultipartInputStreamFileResource;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.FileService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.RequestUtil;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.HttpsRequestUtil;
import oss.fosslight.util.StringUtil;

@RestController
@Slf4j
public class ScannerController extends CoTopComponent {
	
	@Autowired ResponseService responseService;
	@Autowired T2UserService userService;
	@Autowired FileService fileService;
	@Autowired SelfCheckService selfCheckService;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping(value = {Url.EXTERNAL.REQUEST_FL_SCAN})
	public CommonResult requestFlScanService(
    		@RequestParam(name = "prjId", required = true) String prjId,
    		@RequestParam(name = "wgetUrl", required = true) String wgetUrl){
		
		try {

			log.info("fl scanner start pid : " + prjId + ", url:" + wgetUrl);
			String scanServiceUrl = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_FL_SCANNER_URL);
			String adminToken = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_ADMIN_TOKEN);
			
			if (StringUtil.isEmpty(scanServiceUrl) || StringUtil.isEmpty(adminToken)) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "FL Scanner Url or Admin token is not configured");
			}
			
			T2Users user = userService.getLoginUserInfo();
			if (user == null || StringUtil.isEmpty(user.getEmail())) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "Login User Email is not configured");
			}
			
			String resBody = "";
			String url = scanServiceUrl + "/api/projects/process_data/";
			
			RestTemplate template = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
	        headers.set("Authorization", adminToken);
			
	        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
 			if (scanServiceUrl.startsWith("https://")) {
 				parts.add("pid", prjId);
 				parts.add("link", wgetUrl);
 				parts.add("email", user.getEmail());
 				parts.add("admin", adminToken);
 			} else {
 				parts.add("pid", prjId);
 				parts.add("link", wgetUrl);
 				parts.add("email", user.getEmail());
 				parts.add("admin", adminToken);
 			}
 			
 			HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, headers);
 			ResponseEntity<Map> res = template.exchange(url, HttpMethod.POST, entity, Map.class);
 			resBody = res.getBody().toString();
 			
			log.info("fl scanner response : " + resBody);
			
			if (StringUtils.isNotEmpty(resBody)) {
		        Map<String, Object> map = res.getBody();
		        if (map.containsKey("project_uuid")) {
		        	String projectUUID = (String) map.get("project_uuid");
		        	
		        	RestTemplate restTemplate = new RestTemplate();
		        	HttpHeaders header = new HttpHeaders();
			        header.setContentType(MediaType.MULTIPART_FORM_DATA);
			        header.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
			        header.set("Authorization", adminToken);
		        	
			        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
					body.add("project_uuid", projectUUID);
					
					HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);
					ResponseEntity<Map> response = restTemplate.exchange(scanServiceUrl + "/scanner_api/project_info/", HttpMethod.POST, requestEntity, Map.class);
					log.info("[project uuid response] {}", response.getStatusCode());
			        if (response.getStatusCode() == HttpStatus.OK) {
			        	Map<String, Object> responseBody = response.getBody();
			        	String prjLink = (String) responseBody.get("project_link");
			        	
			        	Project param = selfCheckService.getProjectBasicInfo(prjId);
		            	if (param != null) {
		            		String comment = "";
		            		if (!isEmpty(param.getComment())) {
		            			comment += param.getComment();
		            		}
		            		if (!comment.contains(prjLink)) {
		            			comment += "<p>Created project : <a href=\"" + prjLink + "\" target=\"_blank\" style=\"color:#0056b3; cursor:pointer;\">" + prjLink + "</a>"
		            					+ ", analysis : " + wgetUrl
		            					+ ", project uuid : " + projectUUID
		            					+ "</p>";
		            		}
		            		param.setComment(comment);
		            		selfCheckService.updateComment(param);
		            	}
			        }
		        }
			}
			return responseService.getSingleResult(resBody);
		} catch (Exception e) {
			log.error("failed request fl scan, pid:" + prjId + ", url:" + wgetUrl, e);
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, e.getMessage());
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@PostMapping(value = {Url.EXTERNAL.REQUEST_FL_SCAN_FILE})
	public CommonResult requestFlScanFileService(MultipartHttpServletRequest req, HttpServletRequest request, HttpServletResponse res) {
		try {
			
			String scanServiceUrl = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_FL_SCANNER_URL);
			String scanApiKey = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_ADMIN_TOKEN);
			if (StringUtil.isEmpty(scanServiceUrl) || StringUtil.isEmpty(scanApiKey)) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "FL Scanner Url or Api Key is not configured");
			}
			
			T2Users user = userService.getLoginUserInfo();
			if (user == null || StringUtil.isEmpty(user.getEmail())) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "Login User Email is not configured");
			}
			
			String processUrl = "/api/projects/process_data/";
			String infoUrl = "/scanner_api/project_info/";
			
			String prjId = req.getParameter("prjId");
			Map<String, MultipartFile> fileMap = req.getFileMap();
			
			RestTemplate restTemplate = new RestTemplate();
			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("pid", prjId);
	        body.add("email", user.getEmail());
			
	        String fileName = "";
	        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
	            String paramName = entry.getKey();
	            MultipartFile file = entry.getValue();
	            fileName = file.getOriginalFilename();
	            MultipartInputStreamFileResource fileResource = new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename());
	            body.add(paramName, fileResource);
	        }
	        
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
	        headers.set("Authorization", scanApiKey);

	        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
	        ResponseEntity<Map> response = restTemplate.postForEntity(scanServiceUrl + processUrl, requestEntity, Map.class);
	        if (response.getStatusCode() == HttpStatus.OK) {
	        	Map<String, Object> responseBody = response.getBody();
	        	
	        	String projectUUID = (String) responseBody.get("project_uuid");
	        	body = new LinkedMultiValueMap<>();
	        	body.add("project_uuid", projectUUID);
	        	
	        	requestEntity = new HttpEntity<>(body, headers);
		        response = restTemplate.exchange(scanServiceUrl + infoUrl, HttpMethod.POST, requestEntity, Map.class);

		        if (response.getStatusCode() == HttpStatus.OK) {
		        	Map<String, Object> resBody = response.getBody();
		        	String prjLink = (String) resBody.get("project_link");
		        	
		        	Project param = selfCheckService.getProjectBasicInfo(prjId);
	            	if (param != null) {
	            		String comment = "";
	            		if (!isEmpty(param.getComment())) {
	            			comment += param.getComment();
	            		}
	            		if (!comment.contains(prjLink)) {
	            			comment += "<p>Created project : <a href=\"" + prjLink + "\" target=\"_blank\" style=\"color:#0056b3; cursor:pointer;\">" + prjLink + "</a>"
	            					+ ", analysis : " + fileName
	            					+ ", project uuid : " + projectUUID
	            					+ "</p>";
	            		}
	            		param.setComment(comment);
	            		selfCheckService.updateComment(param);
	            	}
		        }
		        return responseService.getSingleResult(responseBody);
	        } else {
	        	return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, "Analysis failed");
	        }
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, e.getMessage());
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping(value = {Url.EXTERNAL.REQUEST_FL_SCAN_SEARCH})
	public CommonResult requestFlScanSearchService (@RequestParam(name = "prjId", required = true) String prjId, @RequestParam(name = "projectUUID", required = true) String projectUUID) {
		Map<String, Object> rtnMap = new HashMap<>();
		rtnMap.put("isValid", false);
		
		try {
			log.info("fl scanner search prj UUID:" + projectUUID);
			
			String scanServiceUrl = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_FL_SCANNER_URL);
			String adminToken = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_ADMIN_TOKEN);
			
			if (StringUtil.isEmpty(scanServiceUrl) || StringUtil.isEmpty(adminToken)) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "FL Scanner Url or Admin token is not configured");
			}
			
			T2Users user = userService.getLoginUserInfo();
			if (user == null || StringUtil.isEmpty(user.getEmail())) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "Login User Email is not configured");
			}
			
			String downloadUrl = "/scanner_api/project_result_file_download/";
			String infoUrl = "/scanner_api/project_info/";
			
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	        body.add("project_uuid", projectUUID);
	        body.add("projectUUID", projectUUID);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
	        headers.set("Authorization", adminToken);

	        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
	        ResponseEntity<Map> response = restTemplate.exchange(scanServiceUrl + infoUrl, HttpMethod.POST, requestEntity, Map.class);
	        
	        if (response.getStatusCode() == HttpStatus.OK) {
	        	Map<String, Object> responseBody = response.getBody();
	        	String prjLink = (String) responseBody.get("project_link");
	        	String prjName = (String) responseBody.get("project_name");
	        	String message = "";
	        	if (StringUtils.isNotEmpty(prjLink)) {
	        		message = "loaded from <a href=\"" + prjLink + "\" target=\"_blank\" style=\"color:#0056b3; cursor:pointer;\">" + prjLink + "</a>";
	        	}
	        	
	        	ResponseEntity<byte[]> responseFile = restTemplate.exchange(scanServiceUrl + downloadUrl, HttpMethod.POST, requestEntity, byte[].class);
	        	if (responseFile.getStatusCode() == HttpStatus.OK) {
	        		List<String> errMsgList = new ArrayList<>();
	            	List<Map<String, String>> errList = new ArrayList<>();
	            	List<OssComponents> list = new ArrayList<>();
	            	
		        	byte[] fileBytes = responseFile.getBody();
		        	if (fileBytes != null) {
		        		// save file
		        		String filePath = CommonFunction.getProperty("upload.path");
		        		String logiFileName = UUID.randomUUID() + ".xlsx";
		        		String fileName = prjName.replaceAll(" ", "_") + ".xlsx";
			            try (FileOutputStream fos = new FileOutputStream(filePath + "/" + logiFileName)) {
			                fos.write(fileBytes);
			            }
			            
			            String fileSeq = fileService.registFileDownload(filePath, fileName, logiFileName);
			            String readType = "self";
			            
			            // load data
		        		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
		        				Workbook workbook = new XSSFWorkbook(inputStream)) {
		                    int sheetCount = workbook.getNumberOfSheets();
		                    if (sheetCount > 0) {
		                    	int idx = 0;
		                    	for (int i = 0; i < sheetCount; i++) {
			                        Sheet sheet = workbook.getSheetAt(i);
			                        String sheetName = sheet.getSheetName();
			                        if (!sheetName.trim().equalsIgnoreCase("scanner info")) {
			                        	List<OssComponents> _list = new ArrayList<>();
			    						Map<String, String> errMsgMap = null;
			    						errMsgMap = ExcelUtil.readSheet(workbook, sheet, _list, true, readType, errMsgList);
			    						if (errMsgMap != null) {
			    							errList.add(errMsgMap);
			    						}
			    						for (OssComponents data : _list) {
			    							idx++;
			    							data.setReportKey(String.valueOf(idx));
			    							list.add(data);
			    						}
			                        }
			                    }
		                    	
		                    	Map<String, Object> resultMap = CommonFunction.makeGridDataFromReport(null, null, null, list, fileSeq, readType);
		                    	if (MapUtils.isNotEmpty(resultMap)) {
		                    		List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) resultMap.get("mainData");
		                    		List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
		                    		
		                    		Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(ossComponents, ossComponentsLicense);
		                			ossComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
		                			ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
		                			
		                			if (!StringUtils.isEmpty(fileSeq)) {
		                				Project project = new Project();
			                			project.setPrjId(prjId);
		                				project.setSrcScanFileId(fileSeq);
		                				
		                				List<T2File> scanFile = new ArrayList<>();
		                				scanFile.add(fileService.selectFileInfo(fileSeq));
		                				project.setScanFile(scanFile);
		                				
		                				selfCheckService.registSrcOss(ossComponents, ossComponentsLicense, project);
		                				
		                				String comment = "";
		                				Project param = selfCheckService.getProjectBasicInfo(prjId);
		        		            	if (param != null && !isEmpty(param.getComment())) {
		        		            		comment += param.getComment();
		        		            	}
		        		            	comment += "<p>" + message + "</p>";
		                				project.setComment(comment);
		                        		selfCheckService.updateComment(project);
		                			}
		                    	} else {
		                    		return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, "Can't find result file from " + message);
		                    	}
		                    } else {
		                    	return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, "Can't find result file from " + message);
		                    }
		                } catch (Exception e) {
		                    log.error(e.getMessage(), e);
		                }
		        	} else {
		        		return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, "Can't find result file from " + message);
		        	}
	        	} else {
		        	return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, "Can't find result file from " + message);
		        }
	        } else {
	        	return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, "Can't find information about project UUID");
	        }
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, "Can't find information about project UUID");
		}
		
		return responseService.getSingleResult(rtnMap);
	}
}
