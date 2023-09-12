/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"2. 3rd Party"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
public class ApiPartnerV2Controller extends CoTopComponent {
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiPartnerService apiPartnerService; 
	
	protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");
	
	@ApiOperation(value = "3rd Party Search", notes = "3rd party 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_PARTNER_SEARCH})
    public CommonResult getVulnerabilityData(
    		@RequestHeader String _token,
    		@ApiParam(value = "3rd Party ID List", required = false) @RequestParam(required = false) String[] partnerIdList,
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
			
//			paramMap.put("userRole", userInfo.getAuthority());
			paramMap.put("creator", 		creator);
			paramMap.put("userId", 			userInfo.getUserId());
			paramMap.put("userRole", 		userRole(userInfo));
			paramMap.put("division", 		division);
			paramMap.put("status", 			status);
			paramMap.put("partnerIdList", 	partnerIdList);
			
			try {
				resultMap = apiPartnerService.getPartnerMasterList(paramMap);
				
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
	
	@ApiOperation(value = "3rd Party Add Watcher", notes = "3rd Party Add Watcher")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_PARTNER_ADD_WATCHER})
    public CommonResult addPrjWatcher(
    		@RequestHeader String _token,
    		@ApiParam(value = "3rd Party ID", required = true) @RequestParam(required = true) String partnerId,
    		@ApiParam(value = "Watcher Email", required = true) @RequestParam(required = true) String[] emailList){
		
		T2Users userInfo = userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<>();
		String errorCode = CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE; // Default error message
		
		try {
			Map<String, Object> paramMap = new HashMap<>();
			List<String> partnerIdList = new ArrayList<String>();
			partnerIdList.add(partnerId);
			String[] partnerIds = partnerIdList.toArray(new String[partnerIdList.size()]);
			
			paramMap.put("userId", userInfo.getUserId());
			paramMap.put("userRole", userRole(userInfo));
			paramMap.put("partnerIdList", partnerIds);
			paramMap.put("readOnly", CoConstDef.FLAG_NO);
			
			boolean searchFlag = apiPartnerService.existPartnertCnt(paramMap);
			if (searchFlag) {
				if (emailList != null) {
					for (String email : emailList) {
						boolean ldapCheck = apiPartnerService.existLdapUserToEmail(email);
						if (ldapCheck) {
							boolean watcherFlag = apiPartnerService.existsWatcherByEmail(partnerId, email);
							if (watcherFlag) {
								Map<String, Object> param = new HashMap<>();
								param.put("partnerId", partnerId);
								param.put("division", "");
								param.put("userId", "");
								param.put("partnerEmail", email);
								apiPartnerService.insertWatcher(param);
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
