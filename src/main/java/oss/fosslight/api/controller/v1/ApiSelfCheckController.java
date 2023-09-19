/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.controller.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.API;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.service.ApiFileService;
import oss.fosslight.service.ApiProjectService;
import oss.fosslight.service.ApiSelfCheckService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Api(tags = {"5. SelfCheck"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
public class ApiSelfCheckController extends CoTopComponent {
	@Resource private Environment env;
	private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;
	@PostConstruct
	public void setResourcePathPrefix(){
		RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	}
	
	private boolean ldapCheckFlag = CoConstDef.FLAG_YES.equals(avoidNull(CommonFunction.getProperty("ldap.check.flag"))) ? true : false;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiSelfCheckService apiSelfCheckService;
	
	private final ApiFileService apiFileService;
	
	private final ApiProjectService apiProjectService;
	
	private final SelfCheckService selfCheckService;
	
	private final FileService fileService;
	
	@ApiOperation(value = "Create SelfCheck", notes = "SelfCheck 생성")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_SELFCHECK_CREATE})
    public CommonResult createSelfCheck(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project Name", required = true) @RequestParam(required = true) String prjName,
    		@ApiParam(value = "Project Version", required = false) @RequestParam(required = false) String prjVersion){
		
		// 사용자 인증
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			
			int createCnt = apiSelfCheckService.getCreateProjectCnt(userInfo.getUserId());
			
			if (CoConstDef.CD_OPEN_API_CREATE_PROJECT_LIMIT > createCnt) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				
				paramMap.put("prjName", prjName);
				paramMap.put("prjVersion", avoidNull(prjVersion, ""));
				paramMap.put("loginUserName", userInfo.getUserId());
				
				result = apiSelfCheckService.createSelfCheck(paramMap);
				String prjId = (String) result.get("prjId");
				
				if (isEmpty(prjId)) {
					throw new Exception(); // parameter Error -> create Failure
				}
			} else {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_CREATE_OVERFLOW_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_CREATE_OVERFLOW_MESSAGE));
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
		
		return responseService.getSingleResult(result);
    }
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "SelfCheck OSS Report", notes = "SelfCheck > oss report")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@PostMapping(value = {API.FOSSLIGHT_API_OSS_REPORT_SELFCHECK})
    public CommonResult ossReportSelfCheck(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId,
    		@ApiParam(value = "OSS Report > sheetName : 'Start with Self-Check, SRC or BIN '", required = false) @RequestPart(required = false) MultipartFile ossReport,
    		@ApiParam(value = "Reset Flag (YES : Y, NO : N, Default : Y)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String resetFlag){
		
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;
		
		try {
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjId);
			boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
			
			if (searchFlag) {
				String oldFileId = "";
				if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
					Map<String, Object> prjInfo = apiSelfCheckService.selectProjectMaster(prjId);
					if (prjInfo.get("srcCsvFileId") != null) {
						oldFileId = String.valueOf((int) prjInfo.get("srcCsvFileId"));
					}
				}
				
				if (ossReport != null) {
					UploadFile bean = null;
					if (!isEmpty(oldFileId)) {
						bean = apiFileService.uploadFile(ossReport, null, oldFileId);
					} else {
						bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.
					}

					List<UploadFile> list = new ArrayList<UploadFile>();
					list.add(bean);
					ArrayList<Object> checkFileLimit = null;
					if (bean.getFileExt().contains("csv")) {
						checkFileLimit = CommonFunction.checkCsvFileLimit(list);
					} else {
						checkFileLimit = CommonFunction.checkXlsxFileLimit(list);
					}
					
					if (checkFileLimit != null && checkFileLimit.contains("FILE_SIZE_LIMIT_OVER")) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
					}
					
//					if (ossReport.getOriginalFilename().contains("xls") // 확장자 xls, xlsx, xlsm 허용
//							&& CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > bean.getSize()) { // file size 5MB 이하만 허용.
					
					if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > bean.getSize()) {
//						UploadFile bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.
						String[] sheet = new String[1];
						Map<String, Object> result = apiProjectService.getSheetData(bean, prjId, "Self-Check", sheet);
						String errorMsg = "";
						if (result.containsKey("errorMsg")) {
							errorMsg = (String) result.get("errorMsg");
						}
						
						if (!isEmpty(errorMsg) && errorMsg.toUpperCase().startsWith("THERE ARE NO OSS LISTED")) {
							return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE
									, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE));
						}
						
						if (!isEmpty(errorMsg)) {
							resultMap.put("errorMessage", errorMsg);
						}
						
						List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
						List<List<ProjectIdentification>> ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");
						
						if (ossComponents.isEmpty()) {
							return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE, getMessage("api.upload.file.sheet.no.match", new String[]{"Self-Check*"}));
						}
						
						T2CoProjectValidator pv = new T2CoProjectValidator();
						pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
						pv.setValidLevel(pv.VALID_LEVEL_BASIC);
						pv.setAppendix("mainList", ossComponents); // sub grid
						pv.setAppendix("subList", ossComponentsLicense);
						T2CoValidationResult vr = pv.validate(new HashMap<>());
						
						if (!vr.isValid()) {
							return responseService.getFailResult(getMessage("api.dataValidationError.code"), getMessage("api.dataValidationError.msg")); // data validation error
						} else {
							List<ProjectIdentification> ossComponentList = new ArrayList<>();
							List<List<ProjectIdentification>> ossComponentsLicenseList = new ArrayList<>();
							
							if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
								apiSelfCheckService.getIdentificationGridList(prjId, CoConstDef.CD_DTL_SELF_COMPONENT_ID, ossComponentList, ossComponentsLicenseList);
							}
							
							ossComponentList.addAll(ossComponents);
							ossComponentsLicenseList.addAll(ossComponentsLicense);
							
							Project project = new Project();
							project.setPrjId(prjId);
							project.setSrcCsvFileId(bean.getRegistFileId()); // set file id
							selfCheckService.registSrcOss(ossComponentList, ossComponentsLicenseList, project);
							
							// 정상처리된 경우 세션 삭제
							deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId));
							deleteSession(
									CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));
							
							return responseService.getSingleResult(resultMap);
						}
					} else {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
					}
				} else {
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
				}
			} else {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
	}
	
	@ApiOperation(value = "SelfCheck Export", notes = "SelfCheck > Export")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@PostMapping(value = {API.FOSSLIGHT_API_EXPORT_SELFCHECK})
    public ResponseEntity<FileSystemResource> selfCheckExport(@RequestHeader String _token, @ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId){
		String downloadId = "";
		T2File fileInfo = new T2File();
		
		try {
			T2Users userInfo = userService.checkApiUserAuth(_token);
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjId);
			boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
			if (searchFlag) {
				downloadId = ExcelDownLoadUtil.getExcelDownloadId("selfReport", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
				fileInfo = fileService.selectFileInfo(downloadId);
				
				return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	@ApiOperation(value = "SelfCheck Add Watcher", notes = "SelfCheck Add Watcher")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_SELFCHECK_ADD_WATCHER})
    public CommonResult addPrjWatcher(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project Id", required = true) @RequestParam(required = true) String prjId,
    		@ApiParam(value = "Watcher Email", required = true) @RequestParam(required = true) String[] emailList){
		
		Map<String, Object> resultMap = new HashMap<>();
		String errorCode = CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE; // Default error message
		
		try {
			T2Users userInfo = userService.checkApiUserAuth(_token);
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjId);
			
			boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap);
			if (searchFlag) {
				if (emailList != null) {
					for (String email : emailList) {
						boolean ldapCheck = false;
						if (ldapCheckFlag) {
							ldapCheck = apiProjectService.existLdapUserToEmail(email);
						} else {
							ldapCheck = true;
						}
						
						if (ldapCheck) {
							boolean watcherFlag = apiSelfCheckService.existsWatcherByEmail(prjId, email);
							if (watcherFlag) {
								Map<String, Object> param = new HashMap<>();
								param.put("prjId", prjId);
								param.put("division", "");
								param.put("userId", "");
								param.put("email", email);
								apiSelfCheckService.insertWatcher(param);
							} else {
								errorCode = CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE;
								break;
							}
						} else {
							errorCode = CoConstDef.CD_OPEN_API_USER_NOTFOUND_MESSAGE;
							break;
						}
					}
					
					if (!errorCode.equals(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE)
							&& !errorCode.equals(CoConstDef.CD_OPEN_API_USER_NOTFOUND_MESSAGE)) {
						return responseService.getSingleResult(resultMap);
					}
				} else {
					errorCode = CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE;
				}
			} else {
				errorCode = CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE;
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
		
		return responseService.getFailResult(errorCode, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, errorCode));
	}
}
