/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.controller.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.API;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.service.ApiOssService;
import oss.fosslight.service.RefineOssService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.T2UserService;

@Api(tags = {"1. OSS & License"})
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = "/api/v1")
public class ApiOssController extends CoTopComponent {
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiOssService apiOssService;

	private final OssService ossService;
	
	private final RefineOssService refineOssService;

	
	@ApiOperation(value = "Search OSS List", notes = "OSS 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_OSS_SEARCH})
    public CommonResult getOssInfo(
    		@RequestHeader String _token,
    		@ApiParam(value = "OSS Name", required = true) @RequestParam(required = true) String ossName,
    		@ApiParam(value = "OSS Version", required = false) @RequestParam(required = false) String ossVersion){
		
		// 사용자 인증
		userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		try {
			
			paramMap.put("ossName", ossName);
			paramMap.put("ossVersion", ossVersion);
			List<Map<String, Object>> content = apiOssService.getOssInfo(paramMap);
			
			if (content.size() > 0) {
				resultMap.put("content", content);
			}
			
			return responseService.getSingleResult(resultMap);
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
    }
	
	@ApiOperation(value = "Search OSS Info by downloadLocation", notes = "downloadLocation별 OSS Info 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_DOWNLOADLOCATION_SEARCH})
    public CommonResult getOssInfoByDownloadLocation(
    		@RequestHeader String _token,
    		@ApiParam(value = "Download Location", required = true) @RequestParam(required = true) String downloadLocation){
		
		// 사용자 인증
		userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			List<Map<String, Object>> content = apiOssService.getOssInfoByDownloadLocation(downloadLocation);
			
			if (content.size() > 0) {
				resultMap.put("content", content);
			}
			
			return responseService.getSingleResult(resultMap);
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
    }


	@ApiOperation(value = "Search License Info", notes = "License Info 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_LICENSE_SEARCH})
    public CommonResult getLicenseInfo(
    		@RequestHeader String _token,
    		@ApiParam(value = "License Name", required = true) @RequestParam(required = true) String licenseName){
		
		// 사용자 인증
		userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			List<Map<String, Object>> content = apiOssService.getLicenseInfo(licenseName);
		
			
			if (content.size() > 0) {
				resultMap.put("content", content);
			}
			
			return responseService.getSingleResult(resultMap);
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
    }

	@ApiOperation(value = "Register New OSS", notes = "신규 OSS 등록")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
	})
	@PostMapping(value = {API.FOSSLIGHT_API_OSS_REGISTER})
	public CommonResult registerOss(
			@RequestHeader String _token,
			@ApiParam(value = "OSS Master", required = true) @RequestBody(required = true) OssMaster ossMaster) {

		if (userService.isAdmin(_token)) {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			try {
				resultMap = ossService.saveOss(ossMaster);
				resultMap = ossService.sendMailForSaveOss(resultMap);
				return responseService.getSingleResult(resultMap);
			} catch (Exception e) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
			}
		}
		return responseService.getFailResult(CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE
				, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
	}
	
	@ApiOperation(value = "Refine OSS Download Location", notes = "Refine ALL의 경우 다음 순서대로 처리합니다. <ol><li>1.REMOVE DUPLICATED URL</li><li>2.PUT PURL</li><li>3.REMOVE DUPLICATED PURL</li><li>4.REORDER GITHUB PRIORITY</li></ol><br>* doUpdateFlag가 N인 경우 Database를 Update하지 않습니다.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {"/refine-download-location"})
    public CommonResult refineOssDownloadLocation(
    		@RequestHeader String _token,
    		@ApiParam(value = "OSS Name", required = false) @RequestParam(required = false) String ossName,
    		@ApiParam(value = "Do Update Database", required = true, defaultValue = "N", allowableValues = "N,Y") @RequestParam(required = true) String doUpdateFlag,
    		@ApiParam(value = "Refine Type", required = true, defaultValue = "Check on error for purl generation", allowableValues = "Check on error for purl generation,Remove duplicated Downloadlocation,Remove duplicated PURL,Reorder Github Priority,Put PURL,Refine ALL") @RequestParam(required = true) String refineType){
		
		// 사용자 인증
		try {
			if(!userService.isAdmin(_token)) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
			}
			return responseService.getSingleResult(refineOssService.refineDownloadLocation(ossName, refineType, "Y".equalsIgnoreCase(doUpdateFlag)));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
    }
}
