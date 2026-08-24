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
import oss.fosslight.api.annotation.InternalApi;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.ApiCommonService;
import oss.fosslight.service.T2UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"10. Common"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
public class ApiCommonV2Controller extends CoTopComponent {

    private final RestResponseService responseService;
    private final T2UserService userService;
    private final ApiCommonService apiCommonService;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");

    @InternalApi
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

    @InternalApi
    @ApiOperation(value = "Add division", notes = "Add a user division (T2_CODE_DTL, CD_NO=200). Detail Name maps to CD_DTL_NM, Detail Description to CD_DTL_EXP.")
    @PostMapping(value = {APIV2.FOSSLIGHT_API_COMMON_DIVISION})
    public ResponseEntity<Map<String, Object>> addDivision(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Detail name (CD_DTL_NM)", required = true) @RequestParam(required = true) String cdDtlNm,
            @ApiParam(value = "Detail description (CD_DTL_EXP)", required = false) @RequestParam(required = false) String cdDtlExp) {

        T2Users userInfo = userService.checkApiUserAuthAndSetSession(authorization);
        if (!userInfo.getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
            return responseService.errorResponse(HttpStatus.FORBIDDEN);
        }
        if (isEmpty(cdDtlNm)) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
        try {
            Map<String, Object> result = apiCommonService.addDivision(cdDtlNm, cdDtlExp);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("division add error: detailName={}", cdDtlNm, e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @InternalApi
    @ApiOperation(value = "Update division", notes = "Update CD_DTL_NM and/or CD_DTL_EXP for a user division (T2_CODE_DTL, CD_NO=200) by CD_DTL_NO. Omit a field to leave it unchanged; at least one of cdDtlNm or cdDtlExp must be sent.")
    @PutMapping(value = {APIV2.FOSSLIGHT_API_COMMON_UPDATE_DIVISION})
    public ResponseEntity<Map<String, Object>> updateDivision(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Detail code (CD_DTL_NO)", required = true) @RequestParam(required = true) String cdDtlNo,
            @ApiParam(value = "Detail name (CD_DTL_NM)", required = false) @RequestParam(required = false) String cdDtlNm,
            @ApiParam(value = "Detail description (CD_DTL_EXP)", required = false) @RequestParam(required = false) String cdDtlExp) {

        T2Users userInfo = userService.checkApiUserAuthAndSetSession(authorization);
        if (!userInfo.getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
            return responseService.errorResponse(HttpStatus.FORBIDDEN);
        }
        if (isEmpty(cdDtlNo)) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
        if (cdDtlNm == null && cdDtlExp == null) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                    "At least one of cdDtlNm or cdDtlExp is required.");
        }
        try {
            Map<String, Object> result = apiCommonService.updateDivision(cdDtlNo, cdDtlNm, cdDtlExp);
            if (result == null) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("division update error: cdDtlNo={}", cdDtlNo, e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @InternalApi
    @ApiOperation(value = "Get division list", notes = "Get division list (T2_CODE_DTL, CD_NO=200)")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_COMMON_DIVISION})
    public ResponseEntity<Map<String, Object>> getDivisionList(
            @ApiParam(hidden = true) @RequestHeader String authorization) {

        userService.checkApiUserAuth(authorization);
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> contents = apiCommonService.getDivisionList();
            result.put("content", contents);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("division list search error", e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @InternalApi
    @ApiOperation(value = "Get all users (basic)", notes = "Returns all rows from T2_USERS with user_id, user_name, email, division, use_yn")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_COMMON_USERS})
    public ResponseEntity<Map<String, Object>> getAllUsersBasic(
            @ApiParam(hidden = true) @RequestHeader String authorization) {

        T2Users userInfo = userService.checkApiUserAuthAndSetSession(authorization);
        if (!userInfo.getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
            return responseService.errorResponse(HttpStatus.FORBIDDEN);
        }
        Map<String, Object> result = new HashMap<>();
        try {
            List<T2Users> users = userService.getAllUsersBasic();
            List<Map<String, Object>> contents = new ArrayList<>(users.size());
            for (T2Users u : users) {
                Map<String, Object> row = new HashMap<>();
                row.put("user_id", u.getUserId());
                row.put("user_name", u.getUserName());
                row.put("email", u.getEmail());
                row.put("division", u.getDivision());
                row.put("use_yn", u.getUseYn());
                contents.add(row);
            }
            result.put("content", contents);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("users list search error", e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @InternalApi
    @ApiOperation(value = "Update user division", notes = "Admin only. Update T2_USERS.DIVISION by USER_ID.")
    @PutMapping(value = {APIV2.FOSSLIGHT_API_COMMON_USER_DIVISION})
    public ResponseEntity<Map<String, Object>> updateUserDivision(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "User id (USER_ID)", required = true) @RequestParam(required = true) String userId,
            @ApiParam(value = "Division code (DIVISION)", required = true) @RequestParam(required = true) String division) {

        T2Users userInfo = userService.checkApiUserAuthAndSetSession(authorization);
        if (!userInfo.getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
            return responseService.errorResponse(HttpStatus.FORBIDDEN);
        }
        if (isEmpty(userId) || isEmpty(division)) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
        String targetUserId = userId.trim();
        String targetDivision = division.trim();
        if (!userService.existUserId(targetUserId)) {
            return responseService.errorResponse(HttpStatus.NOT_FOUND, "User id not found: " + targetUserId);
        }
        try {
            if (!apiCommonService.existsActiveDivision(targetDivision)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Invalid division: " + targetDivision);
            }
        } catch (Exception e) {
            log.error("division validation error: division={}", targetDivision, e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> result = new HashMap<>();
        try {
            int updated = userService.updateUserDivisionByUserId(targetUserId, targetDivision, userInfo.getUserId());
            if (updated == 0) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND);
            }

            result.put("success", true);
            result.put("user_id", targetUserId);
            result.put("division", targetDivision);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("user division update error: userId={}, division={}", userId, division, e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
