/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.service.ApiLicenseService;
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

    private final ApiLicenseService apiLicenseService;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");


    @ApiOperation(value = "Search OSS List", notes = "OSS 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_OSS_SEARCH})
    public @ResponseBody ResponseEntity<ListOssDto.Result> getOssInfo(
            @RequestHeader String authorization,
            @ApiParam(value = "OSS Name", required = false) @RequestParam(required = false) String ossName,
            @ApiParam(value = "OSS Version", required = false) @RequestParam(required = false) String ossVersion,
            @ApiParam(value = "Download Location", required = false) @RequestParam(required = false) String downloadLocation,
            @ApiParam(value = "Count Per Page (max 10000, default 10000)", required = false) @RequestParam(required = false) String countPerPage,
            @ApiParam(value = "Page (default 1)", required = false) @RequestParam(required = false) String page
    ) {
        try {
            var _page = (page == null ? 1 : Integer.parseInt(page));
            var _countPerPage = (countPerPage == null ? 10000 : Integer.parseInt(countPerPage));
            if (_page < 0 || _countPerPage < 0 ) {
                throw new NumberFormatException();
            }

            ListOssDto.Request ossQuery =
                    ListOssDto.Request.builder().ossName(ossName)
                            .url(downloadLocation)
                            .ossVersion(ossVersion)
                            .build();
            ossQuery.setPage(_page);
            ossQuery.setCountPerPage(_countPerPage);

            var map = apiOssService.listOss(ossQuery);
            return ResponseEntity.ok(map);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @ApiOperation(value = "Search License Info", notes = "License Info 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_LICENSE_SEARCH_TEST})
    public @ResponseBody ResponseEntity<ListLicenseDto.Result> getLicenseInfoTest(
            @RequestHeader String authorization,
            @ApiParam(value = "License Name", required = false) @RequestParam(required = false) String licenseName,
            @ApiParam(value = "Count Per Page (max 10000, default 10000)", required = false) @RequestParam(required = false) String countPerPage,
            @ApiParam(value = "Page (default 1)", required = false) @RequestParam(required = false) String page) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            var _page = (page == null ? 1 : Integer.parseInt(page));
            var _countPerPage = (countPerPage == null ? 10000 : Integer.parseInt(countPerPage));
            if (_page < 0 || _countPerPage < 0 ) {
                throw new NumberFormatException();
            }
            ListLicenseDto.Request licenseQuery =
                    ListLicenseDto.Request.builder().licenseName(licenseName)
                                    .build();
            licenseQuery.setPage(_page);
            licenseQuery.setCountPerPage(_countPerPage);

            var map = apiLicenseService.listLicenses(licenseQuery);
            return ResponseEntity.ok(map);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

//
//    @ApiOperation(value = "Search OSS List", notes = "OSS 조회")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
//    })
//    @GetMapping(value = {APIV2.FOSSLIGHT_API_OSS_SEARCH})
//    public ResponseEntity<Map<String, Object>> getOssInfo(
//            @RequestHeader String authorization,
//            @ApiParam(value = "OSS Name", required = true) @RequestParam(required = true) String ossName,
//            @ApiParam(value = "OSS Version", required = false) @RequestParam(required = false) String ossVersion,
//            @ApiParam(value = "Download Location", required = false) @RequestParam(required = false) String downloadLocation
//    ) {
//
//        // 사용자 인증
//        userService.checkApiUserAuth(authorization);
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        Map<String, Object> paramMap = new HashMap<String, Object>();
//
//        try {
//            paramMap.put("ossName", ossName);
//            paramMap.put("ossVersion", ossVersion);
//            paramMap.put("downloadLocation", downloadLocation);
//            List<Map<String, Object>> content = apiOssService.getOssInfo(paramMap);
//
//            if (content.size() > 0) {
//                resultMap.put("content", content);
//            }
//
//            return ResponseEntity.ok(resultMap);
//        } catch (Exception e) {
//            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
//                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
//        }
//    }

    @ApiOperation(value = "Search License Info", notes = "License Info 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_LICENSE_SEARCH})
    public ResponseEntity<Map<String, Object>> getLicenseInfo(
            @RequestHeader String authorization,
            @ApiParam(value = "License Name", required = false) @RequestParam(required = false) String licenseName) {

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
