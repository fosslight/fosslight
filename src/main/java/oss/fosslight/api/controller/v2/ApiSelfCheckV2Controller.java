/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.*;
import io.swagger.models.Response;
import lombok.RequiredArgsConstructor;
import org.apache.tools.ant.taskdefs.condition.Http;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.*;
import oss.fosslight.service.*;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"5. SelfCheck"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
public class ApiSelfCheckV2Controller extends CoTopComponent {
    @Resource
    private Environment env;
    private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;

    @PostConstruct
    public void setResourcePathPrefix() {
        RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiSelfCheckService apiSelfCheckService;

    private final ApiFileService apiFileService;

    private final ApiProjectService apiProjectService;

    private final SelfCheckService selfCheckService;

    private final FileService fileService;

    @ApiOperation(value = "Create SelfCheck", notes = "SelfCheck 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_SELFCHECK_CREATE})
    public ResponseEntity<Map<String, Object>> createSelfCheck(
            @RequestHeader String authorization,
            @ApiParam(value = "Project Name", required = true) @RequestParam(required = true) String prjName,
            @ApiParam(value = "Project Version", required = false) @RequestParam(required = false) String prjVersion) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            int createCnt = apiSelfCheckService.getCreateProjectCnt(userInfo.getUserId());

            if (CoConstDef.CD_OPEN_API_CREATE_PROJECT_LIMIT <= createCnt) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_CREATE_OVERFLOW_MESSAGE));
            }
            Map<String, Object> paramMap = new HashMap<String, Object>();

            paramMap.put("prjName", prjName);
            paramMap.put("prjVersion", avoidNull(prjVersion, ""));
            paramMap.put("loginUserName", userInfo.getUserId());

            result = apiSelfCheckService.createSelfCheck(paramMap);
            String prjId = (String) result.get("prjId");

            if (isEmpty(prjId)) {
                throw new Exception(); // parameter Error -> create Failure
            }
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "SelfCheck OSS Report", notes = "SelfCheck > oss report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PutMapping(value = {APIV2.FOSSLIGHT_API_OSS_REPORT_SELFCHECK})
    public ResponseEntity<Map<String, Object>> ossReportSelfCheck(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "name", required = true) String prjId,
            @ApiParam(value = "OSS Report > sheetName : 'Start with Self-Check, SRC or BIN '", required = false) @RequestPart(required = false) MultipartFile ossReport,
            @ApiParam(value = "Reset Flag (YES : Y, NO : N, Default : Y)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String resetFlag) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjId);
            boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.

            if (!searchFlag) {

                return responseService.errorResponse(HttpStatus.FORBIDDEN,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
            }
            String oldFileId = "";
            if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
                Map<String, Object> prjInfo = apiSelfCheckService.selectProjectMaster(prjId);
                if (prjInfo.get("srcCsvFileId") != null) {
                    oldFileId = String.valueOf((int) prjInfo.get("srcCsvFileId"));
                }
            }

            if (ossReport == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
            }
            UploadFile bean = null;
            if (!isEmpty(oldFileId)) {
                bean = apiFileService.uploadFile(ossReport, null, oldFileId);
            } else {
                bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.
            }

            List<UploadFile> list = new ArrayList<UploadFile>();
            list.add(bean);
            ArrayList<Object> checkFileLimit = null;
            if (bean.getFileExt().contains("csv")) {
                checkFileLimit = CommonFunction.checkCsvFileLimit(list);
            } else {
                checkFileLimit = CommonFunction.checkXlsxFileLimit(list);
            }

            if (checkFileLimit != null && checkFileLimit.contains("FILE_SIZE_LIMIT_OVER")) {
                return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
            }

//					if (ossReport.getOriginalFilename().contains("xls") // 확장자 xls, xlsx, xlsm 허용
//							&& CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > bean.getSize()) { // file size 5MB 이하만 허용.

            if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= bean.getSize()) {
                return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
            }
//					UploadFile bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.
            String[] sheet = new String[1];
            Map<String, Object> result = apiProjectService.getSheetData(bean, prjId, "Self-Check", sheet);
            String errorMsg = "";
            if (result.containsKey("errorMessage")) {
                errorMsg = (String) result.get("errorMessage");
            }

            List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
            List<List<ProjectIdentification>> ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");

            if (!isEmpty(errorMsg)) {
                throw new Exception(); // readData시 문제가 발생할 경우 parameter error로 return 함.
            }

            T2CoProjectValidator pv = new T2CoProjectValidator();
            pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
            pv.setValidLevel(pv.VALID_LEVEL_BASIC);
            pv.setAppendix("mainList", ossComponents); // sub grid
            pv.setAppendix("subList", ossComponentsLicense);
            T2CoValidationResult vr = pv.validate(new HashMap<>());

            if (!vr.isValid()) {
                return responseService.errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, getMessage("api.dataValidationError.msg")); // data validation error
            } else {
                List<ProjectIdentification> ossComponentList = new ArrayList<>();
                List<List<ProjectIdentification>> ossComponentsLicenseList = new ArrayList<>();

                if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
                    apiSelfCheckService.getIdentificationGridList(prjId, CoConstDef.CD_DTL_SELF_COMPONENT_ID, ossComponentList, ossComponentsLicenseList);
                }

                ossComponentList.addAll(ossComponents);
                ossComponentsLicenseList.addAll(ossComponentsLicense);

                Project project = new Project();
                project.setPrjId(prjId);
                project.setSrcCsvFileId(bean.getRegistFileId()); // set file id
                selfCheckService.registSrcOss(ossComponentList, ossComponentsLicenseList, project);

                // 정상처리된 경우 세션 삭제
                deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId));
                deleteSession(
                        CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));

                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
        }
    }

    @ApiOperation(value = "SelfCheck Export", notes = "SelfCheck > Export")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_EXPORT_SELFCHECK})
    public ResponseEntity<FileSystemResource> selfCheckExport(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id", required = true) String prjId
    ) {
        String downloadId = "";
        T2File fileInfo = new T2File();

        try {
            T2Users userInfo = userService.checkApiUserAuth(authorization);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjId);
            boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
            if (searchFlag) {
                downloadId = ExcelDownLoadUtil.getExcelDownloadId("selfReport", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
                fileInfo = fileService.selectFileInfo(downloadId);

                return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @ApiOperation(value = "SelfCheck Add Watcher", notes = "SelfCheck Add Watcher")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PutMapping(value = {APIV2.FOSSLIGHT_API_SELFCHECK_ADD_WATCHER})
    public ResponseEntity<Map<String, Object>> addPrjWatcher(
            @RequestHeader String authorization,
            @ApiParam(value = "Project Id", required = true) @PathVariable(name = "id", required = true) String prjId,
            @ApiParam(value = "Watcher Email", required = true) @RequestParam(required = true) String[] emailList) {

        Map<String, Object> resultMap = new HashMap<>();
        String errorCode = CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE; // Default error message

        try {
            T2Users userInfo = userService.checkApiUserAuth(authorization);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjId);

            boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap);
            if (!searchFlag) {
                return responseService.errorResponse(HttpStatus.FORBIDDEN, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
            }
            if (emailList == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
            }

            for (String email : emailList) {
                boolean ldapCheck = apiProjectService.existLdapUserToEmail(email);
                if (!ldapCheck) {
                    return responseService.errorResponse(HttpStatus.NOT_FOUND, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_USER_NOTFOUND_MESSAGE));
                }
                boolean watcherFlag = apiSelfCheckService.existsWatcherByEmail(prjId, email);
                if (!watcherFlag) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
                }
                Map<String, Object> param = new HashMap<>();
                param.put("prjId", prjId);
                param.put("division", "");
                param.put("userId", "");
                param.put("email", email);
                apiSelfCheckService.insertWatcher(param);
            }

            return ResponseEntity.ok(resultMap);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }
    }
}
