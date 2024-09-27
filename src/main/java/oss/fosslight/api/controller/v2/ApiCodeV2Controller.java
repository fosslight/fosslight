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
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.service.ApiCodeService;
import oss.fosslight.service.T2UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"6. Code v2"})
@RequiredArgsConstructor
@RestController()
@RequestMapping(value = "/api/v2")
public class ApiCodeV2Controller extends CoTopComponent {

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiCodeService apiCodeService;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");

    @ApiOperation(value = "Search Code Info", notes = "code 조회")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_CODE_SEARCH})
    public ResponseEntity<Map<String, Object>> getVulnerabilityData(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "code Type (DIV:Division, OS:Os Type, DSTT:Distribution Type, DSTS:Distribution Site, NOTI:NOTICE TYPE, NP:NOTICE PLATFORM, PRI:PRIORITY)", required = true, allowableValues = "DIV,OS,DSTT,DSTS,NOTI,NP,PRI") @RequestParam(required = true) String codeType,
            @ApiParam(value = "detail Value", required = false) @RequestParam(required = false) String detailValue) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> contents = apiCodeService.getCodeList(codeType, detailValue);
            if (contents.size() == 0) {
                return ResponseEntity.notFound().build();
            }
            result.put("content", contents);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
