/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.controller.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
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
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.API;
import oss.fosslight.service.ApiCodeService;
import oss.fosslight.service.T2UserService;

@Api(tags = {"6. Code"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
public class ApiCodeController extends CoTopComponent {
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiCodeService apiCodeService;
	
	
	@ApiOperation(value = "Search Code Info", notes = "code 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_CODE_SEARCH})
    public CommonResult getVulnerabilityData(
    		@RequestHeader String _token,
    		@ApiParam(value = "code Type (DIV:Division, OS:Os Type, DSTT:Distribution Type, DSTS:Distribution Site, NOTI:NOTICE TYPE, NP:NOTICE PLATFORM, PRI:PRIORITY)", required = true, allowableValues = "DIV,OS,DSTT,DSTS,NOTI,NP,PRI") @RequestParam(required = true) String codeType,
    		@ApiParam(value = "detail Value", required = false) @RequestParam(required = false) String detailValue){
		
		// 사용자 인증
		userService.checkApiUserAuth(_token);
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			List<Map<String, Object>> contents = apiCodeService.getCodeList(codeType, detailValue);
			
			if (contents.size() > 0) {
				result.put("content", contents);
			}
			
			return responseService.getSingleResult(result);
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
	}
}
