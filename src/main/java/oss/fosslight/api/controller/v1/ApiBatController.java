/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.controller.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
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
import oss.fosslight.common.Url.API;
import oss.fosslight.service.ApiBatService;
import oss.fosslight.service.T2UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@Api(tags = {"7. Binary"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
@Profile(value = {"stage","prod"})
public class ApiBatController extends CoTopComponent {
	
	private final ResponseService responseService;
	
	private final T2UserService userService;
	
	private final ApiBatService apibatService;
	
	@ApiOperation(value = "Search Binary List", notes = "Binary Info 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
	@GetMapping(value = {API.FOSSLIGHT_API_BINARY_SEARCH})
    public CommonResult getBinaryInfo(
    		@RequestHeader String _token,
    		@ApiParam(value = "Binary Name", required = false) @RequestParam(required = false) String fileName,
    		@ApiParam(value = "Tlsh", required = false) @RequestParam(required = false) String tlsh,
    		@ApiParam(value = "checksum", required = false) @RequestParam(required = false) String checksum,
    		@ApiParam(value = "Platform Name", required = false) @RequestParam(required = false) String platformName,
    		@ApiParam(value = "PlatForm Version", required = false) @RequestParam(required = false) String platformVersion,
    		@ApiParam(value = "Source Path", required = false) @RequestParam(required = false) String sourcePath){
		
		// 사용자 인증
		userService.checkApiUserAuth(_token);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		// 전부 null이면 parameter error return
		if (!isEmpty(fileName) 
				|| !isEmpty(tlsh) 
				|| !isEmpty(checksum)) {
			paramMap.put("fileName", 		fileName);
			paramMap.put("tlsh", 			tlsh);
			paramMap.put("checksum", 		checksum);
			paramMap.put("platformName", 	platformName);
			paramMap.put("platformVersion", platformVersion);
			paramMap.put("sourcePath", 		sourcePath);
			
			List<Map<String, Object>> contents = apibatService.getBatList(paramMap);
			
			if (contents != null) {
				resultMap.put("content", contents);
			}
			
			return responseService.getSingleResult(resultMap);
			
		} else {
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE
					, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
		}
    }
}
