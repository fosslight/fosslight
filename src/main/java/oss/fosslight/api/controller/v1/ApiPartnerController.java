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

import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.API;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.ApiPartnerService;
import oss.fosslight.service.T2UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@Api(tags = {"2. 3rd Party"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
public class ApiPartnerController extends CoTopComponent {
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiPartnerService apiPartnerService; 
	
	@ApiOperation(value = "3rd Party Search", notes = "3rd party 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_PARTNER_SEARCH})
    public CommonResult getVulnerabilityData(
    		@RequestHeader String _token,
    		@ApiParam(value = "Division", required = false) @RequestParam(required = false) String division,
    		@ApiParam(value = "Create Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String createDate,
    		@ApiParam(value = "Status (PROG:progress, REQ:Request, REV:Review, CONF:Confirm)", required = false, allowableValues = "PROG,REQ,REV,CONF") @RequestParam(required = false) String status,
    		@ApiParam(value = "Update Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String updateDate,
    		@ApiParam(value = "Creator", required = false) @RequestParam(required = false) String creator){
		
		// 사용자 인증
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		try {			
			CommonFunction.splitDate(createDate, paramMap, "-", "createDate");
			CommonFunction.splitDate(updateDate, paramMap, "-", "updateDate");
			
			paramMap.put("userRole", userInfo.getAuthority());
			paramMap.put("creator", creator);
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("division", division);
			paramMap.put("status", status);
			
			try {
				List<Map<String, Object>> content = apiPartnerService.getPartnerMasterList(paramMap);
				
				if(content.size() > 0) {
					resultMap.put("content", content);
				}
				
				return responseService.getSingleResult(resultMap);
			} catch (Exception e) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
						, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
			}
		} catch (Exception e) {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
		}
    }
}
