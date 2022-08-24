/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.controller.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
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

import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url;
import oss.fosslight.common.Url.API;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.service.ApiFileService;
import oss.fosslight.service.ApiProjectService;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Api(tags = {"3. Project"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
public class ApiProjectController extends CoTopComponent {
	
	@Resource private Environment env;
	private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;
	@PostConstruct
	public void setResourcePathPrefix(){
		RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	}
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiProjectService apiProjectService;
	
	private final FileService fileService;
	
	private final ApiFileService apiFileService;
	
	private final CommentService commentService;
	
	private final ProjectService projectService;
	
	private final HistoryService historyService;
	
	private final CodeMapper codeMapper;
	
	protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");
	
	@ApiOperation(value = "Search Project List", notes = "Project 정보 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {Url.API.FOSSLIGHT_API_PROJECT_SEARCH})
    public CommonResult selectProjectList(
    		@RequestHeader String _token,
    		@ApiParam(value = "project ID List", required = false) @RequestParam(required = false) String[] prjIdList,
    		@ApiParam(value = "Division (\"Check the input value with /api/v1/code_search\")", required = false) @RequestParam(required = false) String division,
    		@ApiParam(value = "Model Name", required = false) @RequestParam(required = false) String modelName,
    		@ApiParam(value = "Create Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String createDate,
    		@ApiParam(value = "Status (PROG:progress, REQ:Request, REV:Review, COMP:Complete, DROP:Drop)", required = false, allowableValues = "PROG,REQ,REV,COMP,DROP") @RequestParam(required = false) String status,
    		@ApiParam(value = "Update Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String updateDate,
    		@ApiParam(value = "Creator", required = false) @RequestParam(required = false) String creator){
		
		// 사용자 인증
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		try {			
			CommonFunction.splitDate(createDate, paramMap, "-", "createDate");
			CommonFunction.splitDate(updateDate, paramMap, "-", "updateDate");
			
//			paramMap.put("userRole", userInfo.getAuthority());
			paramMap.put("creator", 	creator);
			paramMap.put("userId", 		userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("division", 	division);
			paramMap.put("modelName", 	modelName);
			paramMap.put("status", 		status);
			paramMap.put("prjIdList", 	prjIdList);
			
			try {
				resultMap = apiProjectService.selectProjectList(paramMap);
			} catch (Exception e) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
			}
			
			return responseService.getSingleResult(resultMap);
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
    }
	
	@ApiOperation(value = "Search Project List", notes = "Project 정보 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {Url.API.FOSSLIGHT_API_MODEL_SEARCH})
    public CommonResult selectModelList(
    		@RequestHeader String _token,
    		@ApiParam(value = "project ID List", required = true) @RequestParam(required = true) String[] prjIdList){
		
		// 사용자 인증
		userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			try {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("prjIdList", prjIdList);
				
				resultMap = apiProjectService.selectModelList(paramMap);
			} catch (Exception e) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
			}
			
			return responseService.getSingleResult(resultMap);
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Update model list of project", notes = "Basic Information > Model list")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
	})
	@PostMapping(value = {API.FOSSLIGHT_API_MODEL_UPDATE})
	public CommonResult updateModelList(
			@RequestHeader String _token,
			@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId,
			@ApiParam(value = "Model List (ex. MODEL_NAME|ETC > Etc|20220428)", required = false) @RequestParam(required = false) String[] modelListToUpdate,
			@ApiParam(value = "Model List (Spread sheet)", required = false) @RequestPart(required = false) MultipartFile modelReport) {

		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, List<Project>> modelList = null;
		String errorCode = CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE; // Default error message

		try {
			Map<String, Object> paramMap = new HashMap<>();
			List<String> prjIdList = new ArrayList<String>();
			prjIdList.add(prjId);
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjIdList);
			paramMap.put("ossReportFlag", CoConstDef.FLAG_NO);
			paramMap.put("readOnly", CoConstDef.FLAG_NO);

			boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
			if (searchFlag) {
				Project project = projectService.getProjectBasicInfo(prjId);
				if (modelReport != null) {
					if (modelReport.getOriginalFilename().contains("xls") // Allowed file extension: xls, xlsx, xlsm
							&& CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > modelReport.getSize()) { // Max file size :5MB
						modelList = ExcelUtil.getModelList(modelReport, CommonFunction.emptyCheckProperty("upload.path", "/upload"),
								project.getDistributeTarget(), prjId, CoConstDef.FLAG_YES, "0");
					} else {
						errorCode = CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE;
					}
				} else {
					if (modelListToUpdate != null) {
						List<String[]> models = new ArrayList<>();
						for (String strModel : modelListToUpdate) {
							String[] model = strModel.split("\\|");
							if (model.length > 2) models.add(model);
						}
						if (models.size() > 0)
							modelList = ExcelUtil.readModelFromList(models, prjId, CoConstDef.FLAG_YES, "0", project.getDistributeTarget());
					} else {
						errorCode = CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE;
					}
				}

				if (modelList != null) {
					project.setModelList(modelList.get("currentModelList"));
					projectService.insertProjectModel(project);
					return responseService.getSingleResult(resultMap);
				}
			} else {
				errorCode = CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			errorCode = CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE;
		}

		return responseService.getFailResult(errorCode
				, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, errorCode));
	}

	@ApiOperation(value = "Create Project", notes = "project 생성")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_PROJECT_CREATE})
    public CommonResult createProject(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project Name", required = true) @RequestParam(required = true) String prjName,
    		@ApiParam(value = "Project Version", required = false) @RequestParam(required = false) String prjVersion,
    		@ApiParam(value = "OS Type (\"Check the input value with /api/v1/code_search\")", required = true) @RequestParam(required = true) String osType,
    		@ApiParam(value = "OS Type etc", required = false) @RequestParam(required = false) String osTypeEtc,
    		@ApiParam(value = "Distribution Type (\"Check the input value with /api/v1/code_search\")", required = false) @RequestParam(required = false) String distributionType,
    		@ApiParam(value = "Distribution Site (\"Check the input value with /api/v1/code_search\")", required = false) @RequestParam(required = false) String distributionSite,
    		@ApiParam(value = "Network Service (YES : Y, NO : N)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String networkServerType,
    		@ApiParam(value = "OSS Notice (\"Check the input value with /api/v1/code_search\")", required = false) @RequestParam(required = false) String noticeType,
    		@ApiParam(value = "Notice Platform (\"Check the input value with /api/v1/code_search\")", required = false) @RequestParam(required = false) String noticeTypeEtc,
    		@ApiParam(value = "Priority (\"Check the input value with /api/v1/code_search\")", required = false) @RequestParam(required = false) String priority,
    		@ApiParam(value = "comment", required = false) @RequestParam(required = false) String comment){
		
		// 사용자 인증
		T2Users userInfo = userService.checkApiUserAuth(_token);
		int createCnt = apiProjectService.getCreateProjectCnt(userInfo.getUserId());
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			if(CoConstDef.CD_OPEN_API_CREATE_PROJECT_LIMIT > createCnt) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				
				String osTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, osType);
				
				if(isEmpty(osTypeStr)) {
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
				}
				
				if(!isEmpty(distributionType)) {
					String distributionTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, distributionType);
					
					if(isEmpty(distributionTypeStr)) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
					}
				} else {
					distributionType = CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL;
				}
				
				if(!isEmpty(distributionSite)) {
					String distributionSiteStr = CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_CODE, distributionSite);
					
					if(isEmpty(distributionSiteStr)) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
					}
				} else {
					distributionSite = CoConstDef.CD_DTL_DISTRIBUTE_LGE;
				}
				
				
				if(!isEmpty(networkServerType)) {
					if(!CoConstDef.FLAG_YES.equals(networkServerType) 
							&& !CoConstDef.FLAG_NO.equals(networkServerType)) { // NETWORK Service Only는 Y / N만 선택 가능함.
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
					}
				} else {
					networkServerType = CoConstDef.FLAG_NO;
				} 
				
				if(isEmpty(noticeType)) {
					if(!isEmpty(noticeTypeEtc)) {
						String noticeTypeEtcStr = CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, noticeTypeEtc);
						
						if(!isEmpty(noticeTypeEtcStr)) {
							noticeType = CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED;
						} else if(isEmpty(noticeTypeEtcStr)) {
							return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
									, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
						}
					} else {
						noticeType = CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL;
					}
				} else if(!isEmpty(noticeType)) {
					String noticeTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, noticeType);
					
					if(isEmpty(noticeTypeStr)) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
					}
				}
				
				if(!CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(noticeType)) {
					noticeTypeEtc = "";
				} else if(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(noticeType) && isEmpty(noticeTypeEtc)) {
					noticeTypeEtc = CoConstDef.CD_DTL_DEFAULT_PLATFORM;
				} else if(!isEmpty(noticeTypeEtc)) {
					String noticeTypeEtcStr = CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, noticeTypeEtc);
					
					if(!isEmpty(noticeTypeEtcStr)) {
						noticeTypeEtc = noticeTypeEtcStr;
					} else if(isEmpty(noticeTypeEtcStr)) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
					}
				}
				
				if(!isEmpty(priority)) {
					String priorityStr = CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, priority);
					
					if(isEmpty(priorityStr)) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
					}
				} else {
					priority = CoConstDef.CD_PRIORITY_P2;
				}
				
				paramMap.put("prjName", prjName);
				paramMap.put("prjVersion", avoidNull(prjVersion, ""));
				paramMap.put("osType", osType);
				paramMap.put("osTypeEtc", osTypeEtc);
				paramMap.put("distributionType", distributionType);
				paramMap.put("distributionSite", distributionSite);
				paramMap.put("networkServerType", networkServerType);
				paramMap.put("priority", priority);
				paramMap.put("loginUserName", userInfo.getUserId());
				
				result = apiProjectService.createProject(paramMap);
				
				String resultPrjId = (String) result.get("prjId");
				
				Map<String, Object> noticeParamMap = new HashMap<String, Object>();
				noticeParamMap.put("prjId", resultPrjId);
				noticeParamMap.put("noticeType", noticeType);
				noticeParamMap.put("noticeTypeEtc", noticeTypeEtc);
				
				int resultCnt = apiProjectService.makeOssNotice(noticeParamMap);
				
				if(!isEmpty(resultPrjId) && resultCnt > 0) {
					try {
						History h = new History();
						Project project = new Project();
						project.setPrjId(resultPrjId);
						h = projectService.work(project);
						h.setModifier(userInfo.getUserId());
						h.sethAction(CoConstDef.ACTION_CODE_INSERT);
						
						historyService.storeData(h);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					
					if(comment != null) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_USER);
						commentHisBean.setReferenceId(resultPrjId);
						commentHisBean.setExpansion1("SRC");
						commentHisBean.setContents(comment);
						commentService.registComment(commentHisBean, false);
					}
					
					try {
						CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED);
						mailBean.setParamPrjId(resultPrjId);
						String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED));
						comment = avoidNull(comment) + "<br />" + _tempComment;
						mailBean.setComment(comment);
						mailBean.setLoginUserName(userInfo.getUserId());
						mailBean.setLoginUserRole(userInfo.getAuthority());
						CoMailManager.getInstance().sendMail(mailBean);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					
					return responseService.getSingleResult(result);
				} else {
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_CREATE_PROJECT_DUPLICATE_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_CREATE_PROJECT_DUPLICATE_MESSAGE));
				}				
			} else {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_CREATE_OVERFLOW_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_CREATE_OVERFLOW_MESSAGE));
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
    }

	@ApiOperation(value = "Project Bom Tab Export", notes = "Project > Bom tab Export")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {Url.API.FOSSLIGHT_API_PROJECT_BOM_EXPORT})
    public ResponseEntity<FileSystemResource> getPrjBomExport(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId,
    		@ApiParam(value = "Merge & Save Flag (YES : Y, NO : N)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String mergeSaveFlag){
		
		// 사용자 인증
		String downloadId = "";
		T2File fileInfo = new T2File();
		
		try {
			T2Users userInfo = userService.checkApiUserAuth(_token);
			Map<String, Object> paramMap = new HashMap<>();
			List<String> prjIdList = new ArrayList<String>();
			prjIdList.add(prjId);
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjIdList);
			paramMap.put("ossReportFlag", CoConstDef.FLAG_NO);
			paramMap.put("distributionType", "normal");
			
			boolean searchFlag = apiProjectService.existProjectCnt(paramMap);
			
			if(searchFlag) {
				if("Y".equals(mergeSaveFlag)) {
					apiProjectService.registBom(prjId, mergeSaveFlag);
				}
				downloadId = ExcelDownLoadUtil.getExcelDownloadId("bom", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
				fileInfo = fileService.selectFileInfo(downloadId);
			}
			
			return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	@ApiOperation(value = "Project Bom Tab Export Json", notes = "Project > Bom tab Export Json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
	})
	@GetMapping(value = {API.FOSSLIGHT_API_PROJECT_BOM_EXPORT_JSON})
	public CommonResult getPrjBomExportJson(
			@RequestHeader String _token,
			@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId) {

		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			List<String> prjIdList = new ArrayList<String>();
			prjIdList.add(prjId);

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjIdList);
			paramMap.put("distributionType", "normal");

			boolean searchFlag = apiProjectService.existProjectCnt(paramMap);

			if(searchFlag) {
				resultMap = apiProjectService.getBomExportJson(prjId);
			}
			return responseService.getSingleResult(resultMap);
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
	}
	
	@ApiOperation(value = "Project Bom Compare", notes = "Project > Bom tab Compare")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {Url.API.FOSSLIGHT_API_PROJECT_BOM_COMPARE})
    public CommonResult getPrjBomCompare(
    		@RequestHeader String _token,
    		@ApiParam(value = "Before Project id", required = true) @RequestParam(required = true) String beforePrjId,
    		@ApiParam(value = "After Project id", required = true) @RequestParam(required = true) String afterPrjId){
		
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			
			Map<String, Object> paramMap = new HashMap<>();
		
			List<String> prjIdList = new ArrayList<String>();
			prjIdList.add(beforePrjId);
			prjIdList.add(afterPrjId);
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjIdList);
			paramMap.put("distributionType", "normal");
			
			boolean searchFlag = apiProjectService.existProjectCnt(paramMap);
			
			if(searchFlag) {
				List<Map<String, Object>> beforeBomList = apiProjectService.getBomList(beforePrjId);
				List<Map<String, Object>> afterBomList = apiProjectService.getBomList(afterPrjId);
				
				if(beforeBomList == null 
						|| afterBomList == null) { // before, after값 중 하나라도 null이 있으면 비교 불가함.
					throw new Exception();
				}
				
				resultMap.put("contents", apiProjectService.getBomCompare(beforeBomList, afterBomList));
				
				return responseService.getSingleResult(resultMap);
			} else {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Identification OSS Report", notes = "Identification > src > oss report")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@PostMapping(value = {Url.API.FOSSLIGHT_API_OSS_REPORT_SRC})
	public CommonResult ossReportSrc(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId,
    		@ApiParam(value = "OSS Report > sheetName : all sheets starting with 'SRC'", required = false) @RequestPart(required = false) MultipartFile ossReport,
    		@ApiParam(value = "Comment", required = false) @RequestParam(required = false) String comment){
		
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;
		
		try {
			Map<String, Object> paramMap = new HashMap<>();
			List<String> prjIdList = new ArrayList<String>();
			prjIdList.add(prjId);
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjIdList);
			paramMap.put("ossReportFlag", CoConstDef.FLAG_YES);
			paramMap.put("readOnly", CoConstDef.FLAG_NO);
			paramMap.put("distributionType", "normal");
			
			boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
			if(searchFlag) {
				if(ossReport != null) {
					if(ossReport.getOriginalFilename().contains("xls") // 확장자 xls, xlsx, xlsm 허용
							&& CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > ossReport.getSize()) { // file size 5MB 이하만 허용.
						
						boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap); // 잘못된  project에 oss report를 upload하려고 할 경우 ex) src -> bin Android
						if(!checkDistributionTypeFlag) {
							return responseService.getFailResult(CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE
									, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE));
						}
						
						UploadFile bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.

						// get Excel Sheet name starts with SRC
						List<String> sheet = null;
						try {
							sheet = ExcelUtil.getSheetNoStartsWith("SRC", Arrays.asList(bean),
									CommonFunction.emptyCheckProperty("upload.path", "/upload"));
						}  catch (Exception e) {
							log.error(e.getMessage(), e);
						}

						Map<String, Object> result = apiProjectService.getSheetData(bean, prjId, "SRC", 
							sheet != null ? sheet.toArray(new String[sheet.size()]) : ArrayUtils.EMPTY_STRING_ARRAY);
						String errorMsg = (String) result.get("errorMessage");
						List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
						List<List<ProjectIdentification>> ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");
						
						if(!isEmpty(errorMsg)) {
							resultMap.put("errorMessage", errorMsg);
						}
						
						T2CoProjectValidator pv = new T2CoProjectValidator();
						pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
						pv.setValidLevel(pv.VALID_LEVEL_BASIC);
						pv.setAppendix("mainList", ossComponents); // sub grid
						pv.setAppendix("subList", ossComponentsLicense);
						T2CoValidationResult vr = pv.validate(new HashMap<>());
						
						if (!vr.isValid()) {
							return responseService.getFailResult(CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE
									, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE));
						} else {
							Project project = new Project();
							project.setPrjId(prjId);
							project.setSrcCsvFileId(bean.getRegistFileId()); // set file id
							projectService.registSrcOss(ossComponents, ossComponentsLicense, project);
							
							// oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
							try {
								if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
										CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_SRC)) != null) {
									String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
											CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_SRC), true);
									if (!isEmpty(changedLicenseName)) {
										CommentsHistory commentHisBean = new CommentsHistory();
										commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
										commentHisBean.setReferenceId(prjId);
										commentHisBean.setExpansion1("SRC");
										commentHisBean.setContents(changedLicenseName);
										commentService.registComment(commentHisBean, false);
									}
								}
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
							
							if(comment != null) {
								CommentsHistory commentHisBean = new CommentsHistory();
								commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
								commentHisBean.setReferenceId(prjId);
								commentHisBean.setExpansion1("SRC");
								commentHisBean.setContents(comment);
								commentService.registComment(commentHisBean, false);
							}
							
							try {
								History h = new History();
								h = projectService.work(project);
								h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
								project = (Project) h.gethData();
								h.sethEtc(project.etcStr());
								historyService.storeData(h);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
							
							// 정상처리된 경우 세션 삭제
							deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId));
							deleteSession(
									CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));
						}
						
						return responseService.getSingleResult(resultMap);
						
					} else {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
					}
				} else {
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE));
				}
			} else {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
	}
    		
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Identification OSS Report", notes = "Identification > bin > oss report")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@PostMapping(value = {Url.API.FOSSLIGHT_API_OSS_REPORT_BIN})
	public CommonResult ossReportBin(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId,
    		@ApiParam(value = "OSS Report > sheetName : 'BIN'", required = false) @RequestPart(required = false) MultipartFile ossReport,
    		@ApiParam(value = "Binary.txt", required = false) @RequestPart(required = false) MultipartFile binartTxt,
    		@ApiParam(value = "Comment", required = false) @RequestParam(required = false) String comment){
		
		
		T2Users userInfo = userService.checkApiUserAuth(_token); // token이 정상적인 값인지 확인 
		Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;
		
		try {
			Map<String, Object> paramMap = new HashMap<>();
			List<String> prjIdList = new ArrayList<String>();
			prjIdList.add(prjId);
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjIdList);
			paramMap.put("ossReportFlag", CoConstDef.FLAG_YES);
			paramMap.put("distributionType", "normal");
			paramMap.put("readOnly", CoConstDef.FLAG_NO);
			
			boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
			
			if(searchFlag) {
				List<ProjectIdentification> ossComponents = new ArrayList<>();
				List<List<ProjectIdentification>> ossComponentsLicense = null;
				String changeExclude = "";
				String changeAdded = "";
				Project project = new Project();
				UploadFile ossReportBean = null;
				UploadFile binartTxtBean = null;
				
				if(ossReport != null) {
					if(!ossReport.getOriginalFilename().contains("xls")) { // 확장자 xls, xlsx, xlsm 허용
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE));
					} else if(CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= ossReport.getSize()) { // file size 5MB 이하만 허용.
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
					} else {
						
						boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap); // 잘못된  project에 oss report를 upload하려고 할 경우 ex) bin -> bin Android 
						if(!checkDistributionTypeFlag) {
							return responseService.getFailResult(CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE
									, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE));
						}
						
						ossReportBean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.
						String[] sheet = new String[1];
						Map<String, Object> result = apiProjectService.getSheetData(ossReportBean, prjId, "BIN", sheet);
						String errorMsg = (String) result.get("errorMessage");
						ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
						ossComponents = (ossComponents != null ? ossComponents : new ArrayList<>()); 
						ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");
						
						if(!isEmpty(errorMsg)) {
							resultMap.put("errorMessage", errorMsg);
						}
						
						project.setBinCsvFileId(ossReportBean.getRegistFileId()); // set file id
					}
				}
				
				if(binartTxt != null) {
						if(binartTxt.getOriginalFilename().contains("txt")) {
						binartTxtBean = apiFileService.uploadFile(binartTxt); // file 등록 처리 이후 upload된 file정보를 return함.
						String binaryFileId = binartTxtBean.getRegistFileId();
						List<String> binaryTxtList = CommonFunction.getBinaryListBinBinaryTxt(fileService.selectFileInfoById(binaryFileId));
						
						if(binaryTxtList != null && !binaryTxtList.isEmpty()) {
							// 현재 osslist의 binary 목록을 격납
							Map<String, ProjectIdentification> componentBinaryList = new HashMap<>();
							for(ProjectIdentification bean : ossComponents) {
								if(bean != null && !isEmpty(bean.getBinaryName())) {
									componentBinaryList.put(bean.getBinaryName(), bean);
								}
							}
							List<ProjectIdentification> addComponentList = Lists.newArrayList();
							// 존재여부 확인
							for(String binaryNameTxt : binaryTxtList) {
								if(!componentBinaryList.containsKey(binaryNameTxt)) {
									// add 해야할 list
									ProjectIdentification bean = new ProjectIdentification();
									// 화면에서 추가한 것 처럼 jqg로 시작하는 component id를 임시로 설정한다.
									bean.setGridId("jqg_"+binaryFileId+"_"+addComponentList.size());
									bean.setBinaryName(binaryNameTxt);
									addComponentList.add(bean);
									
									changeAdded += "<br> - " + binaryNameTxt;
								}
								// exclude처리된 경우
								else {
									ProjectIdentification bean = componentBinaryList.get(binaryNameTxt);
									if(bean != null && CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
										changeExclude += "<br>" + binaryNameTxt;
									}
								}
							}
							if(addComponentList != null && !addComponentList.isEmpty()) {
								ossComponents.addAll(addComponentList);
							}
						}
						
						project.setBinBinaryFileId(binaryFileId);
					} else {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE));
					}
				}
				
				if(ossReportBean == null 
						&& binartTxtBean == null) {
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE));
				} else {
					T2CoProjectValidator pv = new T2CoProjectValidator();
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BIN);
					pv.setValidLevel(pv.VALID_LEVEL_BASIC);
					pv.setAppendix("mainList", ossComponents); // sub grid
					pv.setAppendix("subList", ossComponentsLicense);
					T2CoValidationResult vr = pv.validate(new HashMap<>());
					
					if (!vr.isValid()) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE));
					} else {
						project.setPrjId(prjId);
						projectService.registSrcOss(ossComponents, ossComponentsLicense, project, CoConstDef.CD_DTL_COMPONENT_ID_BIN); // bin tab
						
						String csvFileId = project.getBinCsvFileId();
						
						if (!isEmpty(changeExclude) || !isEmpty(changeAdded)) {
							String changedByResultTxt = "";
							
							if(!isEmpty(changeAdded)) {
								changedByResultTxt += "<b>The following binaries were added to OSS List automatically because they exist in the binary.txt.</b><br>";
								changedByResultTxt += changeAdded;
							}
							if(!isEmpty(changeExclude)) {
								if(!isEmpty(changedByResultTxt)) {
									changedByResultTxt += "<br><br>";
								}
								changedByResultTxt += "<b>The following binaries are written to the OSS report as excluded, but they are in the binary.txt. Make sure it is not included in the final firmware.</b><br>";
								changedByResultTxt += changeExclude;
							}
							
							CommentsHistory commentHisBean = new CommentsHistory();
							commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commentHisBean.setReferenceId(prjId);
							commentHisBean.setExpansion1("BIN");
							commentHisBean.setContents(changedByResultTxt);
							commentService.registComment(commentHisBean, false);
						}
						
						// 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
						try {
							if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
									CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId)) != null) {
								String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(
										loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId), true);
								if (!isEmpty(changedLicenseName)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("BIN");
									commentHisBean.setContents(changedLicenseName);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}

						// oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
						try {
							if (getSessionObject(
									CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId,
											CoConstDef.CD_DTL_COMPONENT_ID_BIN)) != null) {
								String changedLicenseName = (String) getSessionObject(
										CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED,
												prjId, CoConstDef.CD_DTL_COMPONENT_ID_BIN),
										true);
								if (!isEmpty(changedLicenseName)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("BIN");
									commentHisBean.setContents(changedLicenseName);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						try {
							if(getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId)) != null) {
								String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId), true);
								if(!isEmpty(chagedOssVersion)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("BIN");
									commentHisBean.setContents(chagedOssVersion);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						if(comment != null) {
							CommentsHistory commentHisBean = new CommentsHistory();
							commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commentHisBean.setReferenceId(prjId);
							commentHisBean.setExpansion1("BIN");
							commentHisBean.setContents(comment);
							commentService.registComment(commentHisBean, false);
						}
						
						try {
							History h = new History();
							h = projectService.work(project);
							h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
							project = (Project) h.gethData();
							h.sethEtc(project.etcStr());
							historyService.storeData(h);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						// session 삭제
						deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_BIN, prjId));
						deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN,
								prjId));
					}
				}
			} else {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
		
		return responseService.getSingleResult(resultMap);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Identification OSS Report", notes = "Identification > android > oss report", hidden = true)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@PostMapping(value = {Url.API.FOSSLIGHT_API_OSS_REPORT_ANDROID})
	public CommonResult ossReportAndroid(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId,
    		@ApiParam(value = "OSS Report > sheetName : 'BIN (Android)'", required = true) @RequestPart(required = true) MultipartFile ossReport,
    		@ApiParam(value = "NOTICE.html", required = true) @RequestPart(required = true) MultipartFile noticeHtml,
    		@ApiParam(value = "result.txt", required = false) @RequestPart(required = false) MultipartFile resultTxt,
    		@ApiParam(value = "Comment", required = false) @RequestParam(required = false) String comment){
		
		T2Users userInfo = userService.checkApiUserAuth(_token); // token이 정상적인 값인지 확인 
		Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;
		
		try {
			Map<String, Object> paramMap = new HashMap<>();
			List<String> prjIdList = new ArrayList<String>();
			prjIdList.add(prjId);
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("prjId", prjIdList);
			paramMap.put("ossReportFlag", CoConstDef.FLAG_YES);
			paramMap.put("distributionType", "android");
			paramMap.put("readOnly", CoConstDef.FLAG_NO);
			
			boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
			UploadFile ossReportBean = null;
			
			if(searchFlag) {
				if(!ossReport.getOriginalFilename().contains("xls")){ // 확장자 xls, xlsx, xlsm 허용
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE));
				} else if(CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= ossReport.getSize()) { // file size 5MB 이하만 허용.
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
				} else {
					boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap); // 잘못된  project에 oss report를 upload하려고 할 경우 ex) bin Android -> bin 
					if(!checkDistributionTypeFlag) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE));
					}
					
					ossReportBean = apiFileService.uploadFile(ossReport); // ossReport 등록
				}
				
				String noticeHtmlFileName = noticeHtml.getOriginalFilename();
				String codeExt[] = StringUtil.split(codeMapper.selectExtType(CoConstDef.CD_ANDROID_NOTICE_XML), ",");
				int count = 0;
				
				for (int i = 0; i < codeExt.length; i++) {
					if (noticeHtmlFileName.endsWith(codeExt[i])) {
						count++;
					}
					;
				}
				
				UploadFile noticeHtmlBean = null;
				UploadFile noticeXMLBean = null;
				if(count == 1) {
					Map<String, UploadFile> Files = apiFileService.uploadNoticeXMLFile(noticeHtml, prjId);
					
					noticeHtmlBean = Files.get("noticeHTML");
					noticeXMLBean = Files.get("noticeXML");
				}else if(count == 0) {
					noticeHtmlBean = apiFileService.uploadFile(noticeHtml); // noticeHtml 등록
				} else {
					noticeHtmlBean = null;
				}
				
				UploadFile resultTxtBean = null;
				
				if(resultTxt != null) {
					resultTxtBean = apiFileService.uploadFile(resultTxt); // resultTxt 등록
				}
				
				Map<String, Object> result = apiProjectService.readAndroidBuildImage(ossReportBean, noticeHtmlBean, resultTxtBean);
				
				List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
				List<List<ProjectIdentification>> ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");
				
				if(ossReportBean == null 
						&& noticeHtmlBean == null
						&& resultTxtBean == null) {
					return responseService.getFailResult(CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE
							, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE));
				} else {
					T2CoProjectValidator pv = new T2CoProjectValidator();
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);
					pv.setValidLevel(pv.VALID_LEVEL_BASIC);
					pv.setAppendix("mainList", ossComponents); // sub grid
					pv.setAppendix("subList", ossComponentsLicense);
					T2CoValidationResult vr = pv.validate(new HashMap<>());
					
					if (!vr.isValid()) {
						return responseService.getFailResult(CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE
								, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE));
					} else {
						Project project = new Project(); // detail project vo
						project.setPrjId(prjId);
						project.setSrcAndroidCsvFileId(ossReportBean.getRegistFileId());
						project.setSrcAndroidNoticeFileId(noticeXMLBean != null ? noticeXMLBean.getRegistFileId() : noticeHtmlBean.getRegistFileId());
						project.setSrcAndroidNoticeXmlId(noticeHtmlBean != null ? noticeHtmlBean.getRegistFileId() : "");
						project.setSrcAndroidResultFileId(resultTxtBean != null ? resultTxtBean.getRegistFileId() : "");
						
						projectService.registSrcOss(ossComponents, ossComponentsLicense, project,
								CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
						
						String csvFileId = project.getSrcAndroidCsvFileId();
						String resultFileId = project.getSrcAndroidResultFileId();
						
						try {
							if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
									CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileId)) != null) {
								String changedByResultTxt = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
										CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileId), true);
								if (!isEmpty(changedByResultTxt)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("BIN(Android)");
									commentHisBean.setContents(changedByResultTxt);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						// 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
						try {
							if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
									CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId)) != null) {
								String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
										CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId), true);
								if (!isEmpty(changedLicenseName)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("BIN(Android)");
									commentHisBean.setContents(changedLicenseName);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}

						// oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
						try {
							if (getSessionObject(
									CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId,
											CoConstDef.CD_DTL_COMPONENT_ID_ANDROID)) != null) {
								String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
										CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID),
										true);
								if (!isEmpty(changedLicenseName)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("BIN(Android)");
									commentHisBean.setContents(changedLicenseName);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						try {
							if(getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId)) != null) {
								String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId), true);
								if(!isEmpty(chagedOssVersion)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("BIN(Android)");
									commentHisBean.setContents(chagedOssVersion);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						if(comment != null) {
							CommentsHistory commentHisBean = new CommentsHistory();
							commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commentHisBean.setReferenceId(prjId);
							commentHisBean.setExpansion1("BIN(Android)");
							commentHisBean.setContents(comment);
							commentService.registComment(commentHisBean, false);
						}
						
						try {
							History h = new History();
							h = projectService.work(project);
							h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
							project = (Project) h.gethData();
							h.sethEtc(project.etcStr());
							historyService.storeData(h);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}

					// session 정보 삭제
					deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, prjId));
					deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID,
							prjId));
						
				}
			} else {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
		
		return responseService.getSingleResult(resultMap);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Verification Package File Upload", notes = "Verification > Package File Upload")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@PostMapping(value = {Url.API.FOSSLIGHT_API_PACKAGE_UPLOAD})
	public CommonResult ossReportAndroid(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project id", required = true) @RequestParam(required = true) String prjId,
    		@ApiParam(value = "Package FIle", required = true) @RequestPart(required = true) MultipartFile packageFile,
    		@ApiParam(value = "Verify when file is uploaded (YES : Y, NO : N)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String verifyFlag){
		
		Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;
		
		T2Users userInfo = userService.checkApiUserAuth(_token); // token이 정상적인 값인지 확인 
		Map<String, Object> paramMap = new HashMap<>();
		List<String> prjIdList = new ArrayList<String>();
		prjIdList.add(prjId);
		paramMap.put("userId", userInfo.getUserId());
		paramMap.put("userRole", userRole(userInfo));
		paramMap.put("prjId", prjIdList);
		paramMap.put("readOnly", CoConstDef.FLAG_NO);
		
		boolean searchFlag = apiProjectService.existProjectCnt(paramMap);
		String errorMsg = "";
		String afterFileSeq = "";
		boolean uploadFlag = false;
		
		if(searchFlag) {
			Map<String, Object> result = apiProjectService.selectVerificationCheck(prjId);
			String useYn = (String) result.get("useYn");
			String packageFileSeq = (String) result.get("packageFileSeq").toString();
						
			if(CoConstDef.CD_OPEN_API_PACKAGE_FILE_LIMIT < Integer.parseInt(packageFileSeq)) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "Up to 3 packaging files can be uploaded.");
			}
			
			String filePath = CommonFunction.emptyCheckProperty("packaging.path", "/upload/packaging") + "/" + prjId;
			UploadFile packageFileBean = apiFileService.uploadFile(packageFile, filePath); // packagingFile 등록
			afterFileSeq = packageFileBean.getRegistSeq();
			
			if(CoConstDef.FLAG_YES.equals(useYn) && CoConstDef.CD_OPEN_API_PACKAGE_FILE_LIMIT >= Integer.parseInt(packageFileSeq)) {
				// packaging File comment
				String uploadComment = "Packaging file, "+packageFileBean.getOriginalFilename()+", was uploaded by "+userInfo.getUserId()+". <br>";
				
				paramMap = new HashMap<String, Object>();
				paramMap.put("prjId", prjId);
				paramMap.put("packageFileId", packageFileBean.getRegistSeq());
				paramMap.put("packageFileSeq", packageFileSeq);
				
				apiProjectService.updatePackageFile(paramMap);
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
				commHisBean.setReferenceId(prjId);
				commHisBean.setContents(uploadComment);
				commentService.registComment(commHisBean);
				
				errorMsg = null; // 정상적으로 처리됨.
				uploadFlag = true;
			} else {
				if(!CoConstDef.FLAG_YES.equals(useYn)) {
					errorMsg = "delete project"; // 삭제된 project
				}
			}
		} else {
			errorMsg = "Authrization Issue"; // 해당 사용자 권한으로는 등록할 project가 없음
		}
		
		try {
			String emailType = isEmpty(errorMsg) ? CoConstDef.CD_MAIL_PACKAGING_UPLOAD_SUCCESS : CoConstDef.CD_MAIL_PACKAGING_UPLOAD_FAILURE;
			
			CoMail mailBean = new CoMail(emailType);
			mailBean.setParamPrjId(prjId);
			mailBean.setParamExpansion1(packageFile.getOriginalFilename()); // packaging file name
			mailBean.setParamExpansion2(errorMsg);							// error message
			mailBean.setToIds(new String[] { userInfo.getUserId() });
			mailBean.setLoginUserName(userInfo.getUserId());
			mailBean.setLoginUserRole(userInfo.getAuthority());
			
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		// after upload complete, verify
		if ("Y".equals(verifyFlag) && uploadFlag) {
			try {
				Map<String, Object> file = new HashMap<>();
				
				List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
				Map<String, Object> map = new HashMap<String, Object>();
				Map<String, Object> resMap = null;
				
				List<String> fileSeqs = apiProjectService.getPackageFileList(prjId);
								
				map.put("prjId", prjId);
				map.put("fileSeqs", fileSeqs);
				
				String packagingComment = apiProjectService.setClearFiles(map);
				map.put("packagingComment", packagingComment);
				
				Map<String, Object> project = new HashMap<>();
				project.put("prjId", prjId);
				
				List<Map<String, Object>> list = apiProjectService.getVerifyOssList(project);
				list = apiProjectService.serMergeGridData(list);
				
				List<String> filePaths = new ArrayList<String>();
				List<String> componentsList = new ArrayList<String>();
				
				for (Map<String, Object> ossComponents : list) {
					componentsList.add(Integer.toString((int) ossComponents.get("componentId")));
					filePaths.add((String) ossComponents.get("filePath"));
				}
				
				map.put("gridFilePaths", filePaths);
				map.put("gridComponentIds", componentsList);
				
				boolean isChangedPackageFile = apiProjectService.getChangedPackageFile(prjId, fileSeqs);
				int seq = 1;
				
				map.put("packagingFileIdx", seq);
				map.put("isChangedPackageFile", isChangedPackageFile);
				
				for (String fileSeq : fileSeqs) {
					map.put("fileSeq", fileSeq);
					map.put("packagingFileIdx", seq++);
					map.put("isChangedPackageFile", isChangedPackageFile);
					result.add(apiProjectService.processVerification(map, file, project));
				}
				
				resMap = result.get(0);
				
				if(fileSeqs.size() > 1){
					resMap.put("verifyValid", result.get(result.size()-1).get("verifyValid"));
					resMap.put("fileCounts", result.get(result.size()-1).get("fileCounts"));
				}
				
				apiProjectService.updateVerifyFileCount((ArrayList<String>) resMap.get("verifyValid"));
				apiProjectService.updateVerifyFileCount((HashMap<String,Object>) resMap.get("fileCounts"));
			}catch(Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return responseService.getSingleResult(resultMap);
	}
}
