/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.RequestUtil;
import oss.fosslight.util.StringUtil;

@RestController
@Slf4j
public class ScannerController {
	
	@Autowired ResponseService responseService;
	@Autowired T2UserService userService;
	
	@PostMapping(value = {Url.EXTERNAL.REQUEST_FL_SCAN})
	public CommonResult requestFlScanService(
    		@RequestParam(required = true) String prjId,
    		@RequestParam(required = true) String wgetUrl){
		
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
			
			MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
			parts.add("pid", prjId);
			parts.add("link", wgetUrl);
			parts.add("email", user.getEmail());
			parts.add("admin", adminToken);
			String resBody = RequestUtil.post(scanServiceUrl, parts);
			log.info("fl scanner response : " + resBody);
			
			return responseService.getSuccessResult();
		} catch (Exception e) {
			log.error("failed request fl scan, pid:" + prjId + ", url:" + wgetUrl, e);
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, e.getMessage());
		}
	}
}
