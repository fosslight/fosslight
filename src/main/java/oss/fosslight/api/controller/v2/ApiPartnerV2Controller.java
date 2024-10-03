/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.api.validator.ValuesAllowed;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "2. 3rd Party")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
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

    @Operation(summary = "3rd Party Search", description = "3rd party 조회")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_SEARCH})
    public ResponseEntity<Map<String, Object>> getPartners(
            @Parameter(hidden=true) @RequestHeader String authorization,
            @Parameter(description = "3rd Party ID List", required = false) @RequestParam(required = false) String[] partnerIdList,
            @Parameter(description = "Division", required = false) @RequestParam(required = false) String division,
            @Parameter(description = "Create Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String createDate,
            @Parameter(description = "Status (PROG:progress, REQ:Request, REV:Review, CONF:Confirm)", required = false) @ValuesAllowed(propName = "status", values = {"PROG", "REQ", "REV", "CONF"}) @RequestParam(required = false) String status,
            @Parameter(description = "Update Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String updateDate,
            @Parameter(description = "Creator", required = false) @RequestParam(required = false) String creator,
            @Parameter(description = "Count Per Page (max: 1000, default: 1000)", required = false) @RequestParam(required = false, defaultValue="1000") String countPerPage,
            @Parameter(description = "Page (default 1)", required = false) @RequestParam(required = false, defaultValue="1") String page) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        try {
            var _page = Integer.parseInt(page);
            var _countPerPage = Integer.parseInt(countPerPage);
            if (_page < 0 || _countPerPage < 0 ) {
                throw new NumberFormatException();
            }
            var _offset = (_page - 1) * _countPerPage;

            CommonFunction.splitDate(createDate, paramMap, "-", "createDate");
            CommonFunction.splitDate(updateDate, paramMap, "-", "updateDate");

//			paramMap.put("userRole", userInfo.getAuthority());
            paramMap.put("creator", creator);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("division", division);
            paramMap.put("status", status);
            paramMap.put("partnerIdList", partnerIdList);
            paramMap.put("countPerPage", _countPerPage);
            paramMap.put("offset", _offset);

            resultMap = apiPartnerService.getPartnerMasterList(paramMap);

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }

    }

    @Operation(summary = "3rd Party Add Watcher", description = "3rd Party Add Watcher")
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_ADD_WATCHER})
    public ResponseEntity<Map<String, Object>> addPrjWatcher(
            @Parameter(hidden=true) @RequestHeader String authorization,
            @Parameter(description = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId,
            @Parameter(description = "Watcher Email", required = true) @RequestParam(required = true) String[] emailList) {

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

    @Operation(summary = "3rd Party Export report", description = "3rd Party > Export report")
    @Parameters({
            @Parameter(name = "Authorization", description = "token", required = true)
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_DOWNLOAD})
    public ResponseEntity<FileSystemResource> get3rdDownload(
            @RequestHeader String authorization,
            @Parameter(description = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId,
            @Parameter(description = "Format") @ValuesAllowed(propName = "format", values = {"Spreadsheet"}) @RequestParam String format) {

        String downloadId = "";
        T2File fileInfo = new T2File();
        T2Users userInfo = userService.checkApiUserAuth(authorization);

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
                downloadId = ExcelDownLoadUtil.getExcelDownloadId("partnerCheckList", partnerId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
                fileInfo = fileService.selectFileInfo(downloadId);
            }

            return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Operation(summary = "3rd Party Export Json", description = "3rd Party > Export Json")
    @Parameters({
            @Parameter(name = "Authorization", description = "token", required = true)
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_JSON})
    public ResponseEntity<Map<String, Object>> get3rdAsJson(
            @RequestHeader String authorization,
            @Parameter(description = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> partnerIdList = new ArrayList<String>();
            partnerIdList.add(partnerId);
            String[] partnerIds = partnerIdList.toArray(new String[partnerIdList.size()]);

            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("partnerIdList", partnerIds);
            paramMap.put("readOnly", CoConstDef.FLAG_YES);

            boolean searchFlag = apiPartnerService.existPartnertCnt(paramMap);
            if (searchFlag) {
                resultMap = apiPartnerService.getExportJson(partnerId);
            }
            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
    }
}
