/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.ApiCommonService;
import oss.fosslight.service.T2UserService;

import java.util.HashMap;
import java.util.Map;

@Api(tags = {"10.Common"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
public class ApiCommonV2Controller extends CoTopComponent {

    private final RestResponseService responseService;
    private final T2UserService userService;
    private final ApiCommonService apiCommonService;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");

    @ApiOperation(value = "Merge division", notes = "Merge division (from -> to)")
    @PostMapping(value = {APIV2.FOSSLIGHT_API_COMMON_MERGE_DIVISION})
    public ResponseEntity<Map<String, Object>> mergeDivision(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "from", required = true) @RequestParam(required = true) String from,
            @ApiParam(value = "to", required = true) @RequestParam(required = true) String to) {

        T2Users userInfo = userService.checkApiUserAuthAndSetSession(authorization);
        Map<String, Object> result = new HashMap<>();
        if (userInfo.getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
            try {
                apiCommonService.mergeDivision(from, to);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                log.error("division merge error: from={}, to={}", from, to, e);
                return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return responseService.errorResponse(HttpStatus.FORBIDDEN);
        }
    }
}
