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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.api.validator.ValuesAllowed;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.API;
import oss.fosslight.service.ApiCodeService;
import oss.fosslight.service.T2UserService;

@Tag(name = "6. Code")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
public class ApiCodeController extends CoTopComponent {
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiCodeService apiCodeService;
	
	
	@Operation(summary = "Search Code Info", description = "code 조회")
    @Parameters({
        @Parameter(name = "_token", description = "token", required = true)
    })
	@GetMapping(value = {API.FOSSLIGHT_API_CODE_SEARCH})
    public CommonResult getVulnerabilityData(
    		@RequestHeader String _token,
    		@Parameter(description = "code Type (DIV:Division, OS:Os Type, DSTT:Distribution Type, DSTS:Distribution Site, NOTI:NOTICE TYPE, NP:NOTICE PLATFORM, PRI:PRIORITY)", required = true) @ValuesAllowed(propName = "codeType", values = {"DIV","OS","DSTT","DSTS","NOTI","NP","PRI"}) @RequestParam(required = true) String codeType,
    		@Parameter(description = "detail Value", required = false) @RequestParam(required = false) String detailValue){
		
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
