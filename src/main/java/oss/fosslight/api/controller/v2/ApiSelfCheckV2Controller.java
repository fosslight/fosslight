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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.api.validator.ValuesAllowed;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.*;
import oss.fosslight.service.*;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"05. SelfCheck"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
@Validated
public class ApiSelfCheckV2Controller extends CoTopComponent {
    private static final String KEY_ERROR_MESSAGE = "errorMessage";
    private static final String KEY_VALID_ERROR = "validError";

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

    private final VerificationService verificationService;

    private final ProjectService projectService;

    @ApiOperation(value = "Self Check 생성", notes = "사용자 소유의 Self Check 프로젝트를 생성하고 프로젝트 ID를 반환합니다. 프로젝트와 Self Check 생성은 하루 최대 3개입니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"prjId\":\"123\"}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - 생성 개수 초과 / 파라미터 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"The number of projects and self-checks that can be created has been exceeded. (Up to 3 per day)\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Unknown error.\"}"))
            )
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_SELFCHECK_CREATE})
    public ResponseEntity<Map<String, Object>> createSelfCheck(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "Project Name", required = true) @RequestParam(required = true) String prjName,
            @ApiParam(value = "Project Version", required = false) @RequestParam(required = false) String prjVersion) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> result = new HashMap<String, Object>();

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
            throw new IllegalStateException("Failed to create self check project.");
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Self Check OSS Report 업로드", notes = "Self-Check, SRC 또는 BIN 시트의 OSS Report를 업로드합니다. resetFlag=N이면 기존 데이터에 추가합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "업로드 처리 성공. 유효한 데이터가 없으면 DB 상세 코드 440을 key로 반환",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"ossComponents\":[{\"componentId\":\"1\",\"referenceId\":\"123\",\"referenceDiv\":\"10\",\"ossId\":\"101\",\"ossName\":\"sample-oss\",\"checkName\":\"Y\",\"checkLicense\":\"Y\",\"checkedEvidence\":\"https://github.com/example/sample-oss\",\"checkedEvidenceType\":\"URL\",\"ossNameExistsYn\":\"Y\",\"ossVersion\":\"1.0.0\",\"versionDiffFlag\":\"N\",\"downloadLocation\":\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\",\"homepage\":\"https://example.org/sample-oss\",\"filePath\":\"src/sample\",\"packageUrl\":\"pkg:github/example/sample-oss@1.0.0\",\"binaryName\":\"libsample.so\",\"binarySize\":\"102400\",\"binaryNotice\":\"Y\",\"customBinaryYn\":\"N\",\"excludeYn\":\"N\",\"obligationType\":\"10\",\"notify\":\"Y\",\"source\":\"OSS Report\",\"licenseId\":\"1\",\"licenseName\":\"Apache-2.0\",\"licenseNameExistsYn\":\"Y\",\"licenseText\":\"Apache License Version 2.0\",\"copyrightText\":\"Copyright 2026 Example Authors\",\"vulnYn\":\"Y\",\"cvssScore\":\"7.5\",\"cveId\":\"CVE-2026-1234\",\"licenseType\":\"PMS\",\"editable\":\"Y\",\"comments\":\"Imported from Self-Check sheet\",\"attribution\":\"This product includes sample-oss.\"}],\"ossComponentsLicense\":[[{\"componentLicenseId\":\"1\",\"componentId\":\"1\",\"licenseId\":\"1\",\"licenseName\":\"Apache-2.0\",\"licenseText\":\"Apache License Version 2.0\",\"copyrightText\":\"Copyright 2026 Example Authors\",\"excludeYn\":\"N\"}]]}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - ossReport 누락",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"The parameter is invalid.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "권한 없음\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 413,
                    message = "파일 크기 초과 (최대 15MB)",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"File size exceeded. (Max size: 15MB for oss report, 4GB for packaging file)\"}"))
            ),
            @ApiResponse(
                    code = 422,
                    message = "데이터 검증 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"api.dataValidationError.msg\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Unknown error.\"}"))
            )
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_OSS_REPORT_SELFCHECK})
    public ResponseEntity<Map<String, Object>> ossReportSelfCheck(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id", required = true) String prjId,
            @ApiParam(value = "OSS Report > sheetName : 'Start with Self-Check, SRC or BIN '") @RequestPart(required = false) MultipartFile ossReport,
            @ApiParam(value = "Reset Flag (YES : Y, NO : N)", allowableValues = "Y,N") @RequestParam(required = false, defaultValue="Y") String resetFlag,
            @ApiParam(value = "Sheet Names") @RequestParam(required = false) String sheetNames) throws Exception {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

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
//							&& CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > bean.getSize()) { // file size 15MB 이하만 허용.

        if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= bean.getSize()) {
            return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
        }
//					UploadFile bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.

        List<String> sheetList = new ArrayList<>();
        boolean sheetNamesEmptyFlag = isEmpty(sheetNames) ? true : false;

        if (sheetNamesEmptyFlag) {
            List<Object> sheets = ExcelUtil.getSheetNames(list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
            for (Object obj : sheets) {
                Map<String, Object> sheetMap = (Map<String, Object>) obj;
                if (sheetMap.containsKey("name")) {
                    sheetList.add((String) sheetMap.get("name"));
                }
            }
        } else {
            if (sheetNames.contains(",")) {
                for (String sheetName : sheetNames.split(",")) {
                    if (!isEmpty(sheetName.trim())) sheetList.add(sheetName.trim());
                }
            } else {
                sheetList.add(sheetNames);
            }
        }

        String[] sheet = sheetList.toArray(new String[sheetList.size()]);
        Map<String, Object> rtnMap = null;
        List<ProjectIdentification> ossComponentList = new ArrayList<>();
        List<List<ProjectIdentification>> ossComponentsLicenseList = new ArrayList<>();

        for (String sheetNm : sheetList) {
            Map<String, Object> result = apiProjectService.getSheetData(bean, prjId, sheetNm, sheet, sheetNamesEmptyFlag);
            resultMap = getSheetDataResult(result);

            if (!resultMap.isEmpty()) {
                rtnMap = resultMap;
                if (rtnMap.containsKey(CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE) ||
                        rtnMap.containsKey(KEY_ERROR_MESSAGE)) {
                    rtnMap = null;
                    continue;
                } else if (rtnMap.containsKey("ossComponents")) {
                    ossComponentList.addAll((List<ProjectIdentification>) rtnMap.get("ossComponents"));
                    List<List<ProjectIdentification>> ossComponentsLicenses = (List<List<ProjectIdentification>>) rtnMap.get("ossComponentsLicense");
                    if (!ossComponentsLicenses.isEmpty()) {
                        ossComponentsLicenseList.addAll(ossComponentsLicenses);
                    }
                } else {
                    break;
                }
            }
        }

        if (rtnMap != null && rtnMap.containsKey(KEY_VALID_ERROR)) {
            return responseService.errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, getMessage("api.dataValidationError.msg")); // data validation error
        }

        if (!ossComponentList.isEmpty()) {
            rtnMap = null;

            if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
                apiSelfCheckService.getIdentificationGridList(prjId, CoConstDef.CD_DTL_SELF_COMPONENT_ID, ossComponentList, ossComponentsLicenseList);
            }

            Project project = new Project();
            project.setPrjId(prjId);
            project.setSrcCsvFileId(bean.getRegistFileId()); // set file id
            selfCheckService.registSrcOss(ossComponentList, ossComponentsLicenseList, project);

            // 정상처리된 경우 세션 삭제
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId));
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSheetDataResult(Map<String, Object> result) {
        Map<String, Object> rtnMap = new HashMap<>();
        String errorMsg = "";

        if (result.containsKey(KEY_ERROR_MESSAGE)) {
            errorMsg = (String) result.get(KEY_ERROR_MESSAGE);
        }

        if (!isEmpty(errorMsg) && errorMsg.toUpperCase().startsWith("THERE ARE NO OSS LISTED")) {
            rtnMap.put(CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE));
            return rtnMap;
        }

        if (!isEmpty(errorMsg)) {
            rtnMap.put(KEY_ERROR_MESSAGE, errorMsg);
            return rtnMap;
        }

        List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
        List<List<ProjectIdentification>> ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");

        if (ossComponents == null || ossComponents.isEmpty()) {
            rtnMap.put(CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE));
            return rtnMap;
        }

        T2CoProjectValidator pv = new T2CoProjectValidator();
        pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
        pv.setValidLevel(pv.VALID_LEVEL_BASIC);
        pv.setAppendix("mainList", ossComponents); // sub grid
        pv.setAppendix("subList", ossComponentsLicense);
        T2CoValidationResult vr = pv.validate(new HashMap<>());

        if (!vr.isValid()) {
            rtnMap.put(KEY_VALID_ERROR, KEY_VALID_ERROR);
        } else {
            rtnMap.put("ossComponents", ossComponents);
            rtnMap.put("ossComponentsLicense", ossComponentsLicense != null ? ossComponentsLicense : new ArrayList<ProjectIdentification>());
        }

        return rtnMap;
    }

    @ApiOperation(value = "Self Check Report 다운로드", notes = "Self Check 결과를 스프레드시트 파일로 다운로드합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공 - Spreadsheet 파일 다운로드",
                    response = org.springframework.core.io.FileSystemResource.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - format 값 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"selfCheckBomDownload.format: Input value='PDF'. 'format' field value should be from list of [Spreadsheet]\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "권한 없음 - 응답 body 없음"
            ),
            @ApiResponse(
                    code = 404,
                    message = "파일 없음\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"File not found.\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Unknown error.\"}"))
            )
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_SELFCHECK_DOWNLOAD})
    public ResponseEntity selfCheckBomDownload(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id", required = true) String prjId,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet")
            @ValuesAllowed(propName = "format", values = { "Spreadsheet"}) @RequestParam String format
    ) throws Exception {
        String downloadId = "";
        T2File fileInfo = new T2File();

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userInfo.getUserId());
        paramMap.put("userRole", userRole(userInfo));
        paramMap.put("prjId", prjId);
        boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
        if (searchFlag) {
            downloadId = ExcelDownLoadUtil.getExcelDownloadId("selfReport", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
            fileInfo = fileService.selectFileInfo(downloadId);

            if (fileInfo == null) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND, "File not found.");
            }

            return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @ApiOperation(value = "Self Check Editor 추가", notes = "Self Check 프로젝트에 이메일 기준 Editor를 추가합니다. LDAP 사용 환경에서는 등록된 사용자만 추가할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - emailList 누락 / 중복 watcher\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"The parameter is invalid.\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "권한 없음\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 404,
                    message = "사용자 없음 - LDAP 사용자 없음\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"User does not exist.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Unknown error.\"}"))
            )
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_SELFCHECK_ADD_EDITOR})
    public ResponseEntity<Map<String, Object>> addPrjEditor(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "Project Id", required = true) @PathVariable(name = "id", required = true) String prjId,
            @ApiParam(value = "Editor Email", required = true) @RequestParam(required = true) String[] emailList) {

        Map<String, Object> resultMap = new HashMap<>();
        String errorCode = CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE; // Default error message

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
            boolean ldapCheck = true;
            if (CoConstDef.FLAG_YES.equals(avoidNull(CommonFunction.getProperty("ldap.check.flag")))) {
                ldapCheck = apiProjectService.existLdapUserToEmail(email);
            }
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
    }

    @ApiOperation(value = "Self Check 조회", notes = "조회 권한이 있는 Self Check 프로젝트의 기본 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"content\":{\"prjId\":\"123\",\"prjName\":\"Sample Self Check\",\"prjVersion\":\"1.0\",\"comment\":\"Initial self-check\",\"commentIdx\":\"501\",\"useYn\":\"Y\",\"srcCsvFileId\":\"10001\",\"creator\":\"user01\",\"createdDate\":\"2026-08-01 09:00:00\",\"modifier\":\"user02\",\"modifiedDate\":\"2026-08-20 14:30:00\",\"prjUserName\":\"홍길동\",\"prjDivisionName\":\"HE Division\"}}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "권한 없음\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Unknown error.\"}"))
            )
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_SELFCHECK_GET})
    public ResponseEntity<Map<String, Object>> getSelfcheck(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "project ID", required = false) @PathVariable(required = true, name = "id") String prjId) {

        Map<String, Object> resultMap = new HashMap<>();

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userInfo.getUserId());
        paramMap.put("userRole", userRole(userInfo));
        paramMap.put("prjId", prjId);

        boolean searchFlag = apiSelfCheckService.existProjectCnt(paramMap);
        if (!searchFlag) {
            return responseService.errorResponse(HttpStatus.FORBIDDEN, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
        }
        var selfCheck = apiSelfCheckService.selectProjectMaster(prjId);
        resultMap.put("content", selfCheck);
        return ResponseEntity.ok(resultMap);
    }
}
