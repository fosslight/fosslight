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
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.APIV2;
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

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiPartnerService apiPartnerService;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");

    @ApiOperation(value = "3rd Party Search", notes = "3rd party 조회")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_SEARCH})
    public ResponseEntity<Map<String, Object>> getPartners(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID List", required = false) @RequestParam(required = false) String[] partnerIdList,
            @ApiParam(value = "Division", required = false) @RequestParam(required = false) String division,
            @ApiParam(value = "Create Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String createDate,
            @ApiParam(value = "Status (PROG:progress, REQ:Request, REV:Review, CONF:Confirm)", required = false, allowableValues = "PROG,REQ,REV,CONF") @RequestParam(required = false) String status,
            @ApiParam(value = "Update Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String updateDate,
            @ApiParam(value = "Creator", required = false) @RequestParam(required = false) String creator) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        try {
            CommonFunction.splitDate(createDate, paramMap, "-", "createDate");
            CommonFunction.splitDate(updateDate, paramMap, "-", "updateDate");

//			paramMap.put("userRole", userInfo.getAuthority());
            paramMap.put("creator", creator);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("division", division);
            paramMap.put("status", status);
            paramMap.put("partnerIdList", partnerIdList);

            resultMap = apiPartnerService.getPartnerMasterList(paramMap);

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }

    }

    @ApiOperation(value = "3rd Party Add Watcher", notes = "3rd Party Add Watcher")
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_ADD_WATCHER})
    public ResponseEntity<Map<String, Object>> addPrjWatcher(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId,
            @ApiParam(value = "Watcher Email", required = true) @RequestParam(required = true) String[] emailList) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

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
            if (!searchFlag) {
                return responseService.errorResponse(HttpStatus.FORBIDDEN);
            }
            for (String email : emailList) {
                boolean ldapCheck = true;
                if (CoConstDef.FLAG_YES.equals(avoidNull(CommonFunction.getProperty("ldap.check.flag")))) {
                    ldapCheck = apiPartnerService.existLdapUserToEmail(email);
                }
                if (!ldapCheck) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
                }
                boolean watcherFlag = apiPartnerService.existsWatcherByEmail(partnerId, email);
                if (watcherFlag) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("partnerId", partnerId);
                    param.put("division", "");
                    param.put("userId", "");
                    param.put("partnerEmail", email);
                    apiPartnerService.insertWatcher(param);
                } else {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
                }
            }
            return new ResponseEntity(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }
    }
}
