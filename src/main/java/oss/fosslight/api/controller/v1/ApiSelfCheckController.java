/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.controller.v1;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.reflect.TypeToken;

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
import oss.fosslight.common.T2CoProjectValidator;
import oss.fosslight.common.T2CoValidationResult;
import oss.fosslight.common.Url.API;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.service.ApiFileService;
import oss.fosslight.service.ApiProjectService;
import oss.fosslight.service.ApiSelfCheckService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.T2UserService;

@Api(tags = {"5. SelfCheck"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
public class ApiSelfCheckController extends CoTopComponent {
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiSelfCheckService apiSelfCheckService;
	
	private final ApiFileService apiFileService;
	
	private final ApiProjectService apiProjectService;
	
	private final SelfCheckService selfCheckService;
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Create SelfCheck", notes = "SelfCheck 생성")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_SELFCHECK_CREATE})
    public CommonResult createSelfCheck(
    		@RequestHeader String _token,
    		@ApiParam(value = "Project Name", required = true) @RequestParam(required = true) String prjName,
    		@ApiParam(value = "Project Version", required = false) @RequestParam(required = false) String prjVersion,
    		@ApiParam(value = "OS Type", required = false) @RequestParam(required = false) String osType,
    		@ApiParam(value = "OS Type etc", required = false) @RequestParam(required = false) String osTypeEtc,
    		@ApiParam(value = "Distribution Type", required = false) @RequestParam(required = false) String distributionType){
		
		// 사용자 인증
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			
			int createCnt = apiSelfCheckService.getCreateProjectCnt(userInfo.getUserId());
			
			if(CoConstDef.CD_OPEN_API_CREATE_PROJECT_LIMIT > createCnt) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				String distributionSubCode = "";
				
				if(isEmpty(distributionType)) {
					distributionSubCode = CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL;
				} else {
					String distributionJSON = CoCodeManager.getAllValuesJson(CoConstDef.CD_DISTRIBUTION_TYPE);
					Type collectionType = new TypeToken<List<Map<String, Object>>>() {
					}.getType();
					List<Map<String, Object>> distributionCode = new ArrayList<>();
					distributionCode = (List<Map<String, Object>>) fromJson(distributionJSON, collectionType);
					
					distributionCode = distributionCode.stream().filter(c -> ((String) c.get("cdDtlNm")).toUpperCase().equals(distributionType.toUpperCase())).collect(Collectors.toList());
					
					if(distributionCode.size() > 0) {
						distributionSubCode = (String) distributionCode.get(0).get("cdDtlNo");
					} else {
						distributionSubCode = distributionType;
					}
				}
				
				paramMap.put("prjName", prjName);
				paramMap.put("prjVersion", avoidNull(prjVersion, ""));
				paramMap.put("osType", avoidNull(osType, ""));
				paramMap.put("osTypeEtc", osTypeEtc);
				paramMap.put("distributionType", distributionSubCode);
				paramMap.put("loginUserName", userInfo.getUserId());
				
				result = apiSelfCheckService.createSelfCheck(paramMap);
				String prjId = (String) result.get("prjId");
				
				if(isEmpty(prjId)) {
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
    		@ApiParam(value = "OSS Report > sheetName : 'Self-Check'", required = false) @RequestPart(required = false) MultipartFile ossReport){
		
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;
		
		try {
			boolean searchFlag = apiSelfCheckService.existProjectCnt(userInfo.getUserId(), prjId); // 조회가 안된다면 권한이 없는 project id를 입력함.
			
			if(searchFlag) {
				if(ossReport != null) {
					if(ossReport.getOriginalFilename().contains("xls") // 확장자 xls, xlsx, xlsm 허용
							&& CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > ossReport.getSize()) { // file size 5MB 이하만 허용.
						
						UploadFile bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.
						Map<String, Object> result = apiProjectService.getSheetData(bean, prjId, "Self-Check");
						String errorMsg = (String) result.get("errorMessage");
						List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
						List<List<ProjectIdentification>> ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");
						
						if(!isEmpty(errorMsg)) {
							throw new Exception(); // readData시 문제가 발생할 경우 parameter error로 return 함.
						}
						
						T2CoProjectValidator pv = new T2CoProjectValidator();
						pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
						pv.setValidLevel(pv.VALID_LEVEL_BASIC);
						pv.setAppendix("mainList", ossComponents); // sub grid
						pv.setAppendix("subList", ossComponentsLicense);
						T2CoValidationResult vr = pv.validate(new HashMap<>());
						
						if(!vr.isValid()) {
							return responseService.getFailResult(getMessage("api.dataValidationError.code"), getMessage("api.dataValidationError.msg")); // data validation error
						} else {
							Project project = new Project();
							project.setPrjId(prjId);
							project.setSrcCsvFileId(bean.getRegistFileId()); // set file id
							selfCheckService.registSrcOss(ossComponents, ossComponentsLicense, project);
							
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
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
	}
}
