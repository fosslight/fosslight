/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.service.ApiOssService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.T2UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"1. OSS & License"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
public class ApiOssV2Controller extends CoTopComponent {
    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiOssService apiOssService;

    private final OssService ossService;


    @ApiOperation(value = "Search OSS List", notes = "OSS 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_OSS_SEARCH})
    public ResponseEntity<Map<String, Object>> getOssInfo(
            @RequestHeader String authorization,
            @ApiParam(value = "OSS Name", required = true) @RequestParam(required = true) String ossName,
            @ApiParam(value = "OSS Version", required = false) @RequestParam(required = false) String ossVersion,
            @ApiParam(value = "Download Location", required = false) @RequestParam(required = false) String downloadLocation
    ) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        try {
            paramMap.put("ossName", ossName);
            paramMap.put("ossVersion", ossVersion);
            paramMap.put("downloadLocation", downloadLocation);
            List<Map<String, Object>> content = apiOssService.getOssInfo(paramMap);

            if (content.size() > 0) {
                resultMap.put("content", content);
            }

            return ResponseEntity.ok(resultMap);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
        }
    }

    @ApiOperation(value = "Search License Info", notes = "License Info 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_LICENSE_SEARCH})
    public ResponseEntity<Map<String, Object>> getLicenseInfo(
            @RequestHeader String authorization,
            @ApiParam(value = "License Name", required = true) @RequestParam(required = true) String licenseName) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            List<Map<String, Object>> content = apiOssService.getLicenseInfo(licenseName);
            if (content.size() == 0) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND, "license not found");
            }
            resultMap.put("content", content);
            return ResponseEntity.ok(resultMap);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
        }
    }

    @ApiOperation(value = "Register New OSS", notes = "신규 OSS 등록")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_OSS_REGISTER})
    public ResponseEntity<Map<String, Object>> registerOss(
            @RequestHeader String authorization,
            @ApiParam(value = "OSS Master", required = true) @RequestBody(required = true) OssMaster ossMaster) {

        if (userService.isAdmin(authorization)) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            try {
                resultMap = ossService.saveOss(ossMaster);
                resultMap = ossService.sendMailForSaveOss(resultMap);
                return ResponseEntity.ok(resultMap);
            } catch (Exception e) {
                return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR
                        , CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
            }
        }
        return responseService.errorResponse(HttpStatus.FORBIDDEN,
                CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
    }
}
