/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.advice.CProjectNotAvailableException;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.ApiPartnerService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.ExcelDownLoadUtil;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

@Api(tags = {"2. 3rd Party"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
@Validated
public class ApiPartnerV2Controller extends CoTopComponent {

    private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;
    @PostConstruct
    public void setResourcePathPrefix() {
        RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
    }

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiPartnerService apiPartnerService;

    private final FileService fileService;

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
            @ApiParam(value = "Creator", required = false) @RequestParam(required = false) String creator,
            @ApiParam(value = "Count Per Page (max: 1000, default: 1000)", required = false)
            @Min(value = 1, message="Input value=${validatedValue}. countPerPage must be larger than {value}") @RequestParam(required = false, defaultValue="1000") int countPerPage,
            @ApiParam(value = "Page (default 1)", required = false)
            @Min(value = 1, message="Input value=${validatedValue}. page must be larger than {value}") @RequestParam(required = false, defaultValue="1") int page) {

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
            paramMap.put("countPerPage", countPerPage);
            paramMap.put("offset", (page - 1) * countPerPage);

            resultMap = apiPartnerService.getPartnerMasterList(paramMap);

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }

    }

    @ApiOperation(value = "3rd Party Add Watcher (Deprecated)", notes = "3rd Party Add Watcher (Deprecated)")
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_ADD_WATCHER})
    public ResponseEntity<Map<String, Object>> addPrjWatcher(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId,
            @ApiParam(value = "Watcher Email", required = true) @RequestParam(required = true) String[] emailList) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

        if (!apiPartnerService.checkUserHasPartnerProject(userInfo, partnerId)) {
            throw new CProjectNotAvailableException(partnerId);
        }

        try {
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

    @ApiOperation(value = "3rd Party Add Editor", notes = "3rd Party Add Editor")
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_ADD_EDITOR})
    public ResponseEntity<Map<String, Object>> addPrjEditor(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId,
            @ApiParam(value = "Editor Email", required = true) @RequestParam(required = true) String[] emailList) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

        if (!apiPartnerService.checkUserHasPartnerProject(userInfo, partnerId)) {
            throw new CProjectNotAvailableException(partnerId);
        }

        try {
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

    @ApiOperation(value = "3rd Party Export report (Deprecated)", notes = "3rd Party > Export report (Deprecated)")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_DOWNLOAD_DEPRECATED})
    public ResponseEntity<FileSystemResource> get3rdDownload_deprecated(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet") @RequestParam String format) throws Exception {

        String downloadId = "";
        T2File fileInfo = new T2File();
        T2Users userInfo = userService.checkApiUserAuth(authorization);

        if (!apiPartnerService.checkUserHasPartnerProject(userInfo, partnerId)) {
            throw new CProjectNotAvailableException(partnerId);
        }

        try {
            downloadId = ExcelDownLoadUtil.getExcelDownloadId("partnerCheckList", partnerId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
            fileInfo = fileService.selectFileInfo(downloadId);

            return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
        } catch (java.lang.Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @ApiOperation(value = "3rd Party Export report", notes = "3rd Party > Export report")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_DOWNLOAD})
    public ResponseEntity<FileSystemResource> get3rdDownload(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet") @RequestParam String format) throws Exception {

        String downloadId = "";
        T2File fileInfo = new T2File();
        T2Users userInfo = userService.checkApiUserAuth(authorization);

        if (!apiPartnerService.checkUserHasPartnerProject(userInfo, partnerId)) {
            throw new CProjectNotAvailableException(partnerId);
        }

        try {
            downloadId = ExcelDownLoadUtil.getExcelDownloadId("partnerCheckList", partnerId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
            fileInfo = fileService.selectFileInfo(downloadId);

            return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
        } catch (java.lang.Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @ApiOperation(value = "3rd Party Export Json (Deprecated)", notes = "3rd Party > Export Json (Deprecated)")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_JSON_DEPRECATED})
    public ResponseEntity<Map<String, Object>> get3rdAsJson_deprecated(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (!apiPartnerService.checkUserHasPartnerProject(userInfo, partnerId)) {
            throw new CProjectNotAvailableException(partnerId);
        }

        try {
            resultMap = apiPartnerService.getExportJson(partnerId);

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "3rd Party Export Json", notes = "3rd Party > Export Json")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_JSON})
    public ResponseEntity<Map<String, Object>> get3rdAsJson(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (!apiPartnerService.checkUserHasPartnerProject(userInfo, partnerId)) {
            throw new CProjectNotAvailableException(partnerId);
        }

        try {
            resultMap = apiPartnerService.getExportJson(partnerId);

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
    }
}
