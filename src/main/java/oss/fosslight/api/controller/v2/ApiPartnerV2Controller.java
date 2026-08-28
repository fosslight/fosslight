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

@Api(tags = {"02. 3rd Party"}, description = " ")
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

    @ApiOperation(value = "3rd Party 목록 조회", notes = "조회 권한이 있는 3rd Party 프로젝트를 조건과 페이지 정보로 검색합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"list\":[{\"partnerId\":\"1\",\"partnerName\":\"Example Supplier\",\"softwareName\":\"Example SDK\",\"softwareVersion\":\"2.5.0\",\"status\":\"Confirm\",\"modifiedDate\":\"2026-08-20\",\"createdDate\":\"2026-08-01\",\"deliveryForm\":\"Source Code\",\"description\":\"SDK supplied for the TV project\",\"creator\":\"user01\",\"reviewer\":\"reviewer01\",\"division\":\"Division\",\"prjId\":\"6304,6305\"}],\"totalCount\":1}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - countPerPage/page 검증 실패\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Input value=0. countPerPage must be larger than 1\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패\n\n" +
              "**에러 코드 (errorCode):**\n\n" +
              "* `TOKEN_INVALID` - 유효하지 않거나 변조된 토큰 (다시 로그인 필요)\n" +
              "* `TOKEN_EXPIRED` - 토큰 만료 (Refresh Token으로 갱신 필요)",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"errorCode\": \"TOKEN_INVALID\",\"message\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_SEARCH})
    public ResponseEntity<Map<String, Object>> getPartners(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID List") @RequestParam(required = false) String[] partnerIdList,
            @ApiParam(value = "Division") @RequestParam(required = false) String division,
            @ApiParam(value = "Create Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)") @RequestParam(required = false) String createDate,
            @ApiParam(value = "Status (PROG:progress, REQ:Request, REV:Review, CONF:Confirm)", allowableValues = "PROG,REQ,REV,CONF") @RequestParam(required = false) String status,
            @ApiParam(value = "Update Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)") @RequestParam(required = false) String updateDate,
            @ApiParam(value = "Creator") @RequestParam(required = false) String creator,
            @ApiParam(value = "Count Per Page (max: 1000)")
            @Min(value = 1, message="Input value=${validatedValue}. countPerPage must be larger than {value}") @RequestParam(required = false, defaultValue="1000") int countPerPage,
            @ApiParam(value = "Page", required = false)
            @Min(value = 1, message="Input value=${validatedValue}. page must be larger than {value}") @RequestParam(required = false, defaultValue="1") int page) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

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

    }

    @ApiOperation(value = "3rd Party Editor 추가", notes = "3rd Party 프로젝트에 이메일 기준 Editor를 추가합니다. LDAP 사용 환경에서는 등록된 사용자만 추가할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"success\":true}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - LDAP 사용자 없음 / 중복 watcher\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"The parameter is invalid.\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패\n\n" +
              "**에러 코드 (errorCode):**\n\n" +
              "* `TOKEN_INVALID` - 유효하지 않거나 변조된 토큰 (다시 로그인 필요)\n" +
              "* `TOKEN_EXPIRED` - 토큰 만료 (Refresh Token으로 갱신 필요)",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"errorCode\": \"TOKEN_INVALID\",\"message\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "프로젝트 접근 권한 없음 (EDIT)\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\":\"The user does not have edit permissions for Project 123\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
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
        resultMap.put("success", true);
        return new ResponseEntity(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "3rd Party Report 다운로드", notes = "3rd Party 프로젝트의 Check List를 스프레드시트 파일로 다운로드합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공 - Spreadsheet 파일 다운로드",
                    response = FileSystemResource.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "필수 format 누락",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"message\":\"'format' parameter is missing or misspelled\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패\n\n" +
              "**에러 코드 (errorCode):**\n\n" +
              "* `TOKEN_INVALID` - 유효하지 않거나 변조된 토큰 (다시 로그인 필요)\n" +
              "* `TOKEN_EXPIRED` - 토큰 만료 (Refresh Token으로 갱신 필요)",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"errorCode\": \"TOKEN_INVALID\",\"message\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "프로젝트 접근 권한 없음 (EDIT)\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\":\"The user does not have edit permissions for Project 123\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_DOWNLOAD})
    public ResponseEntity<FileSystemResource> get3rdDownload(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id") String partnerId,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet") @RequestParam String format) throws Exception {
        return get3rdDownloadInternal(authorization, partnerId, format);
    }

    @ApiOperation(value = "3rd Party Report 다운로드 (Deprecated)", notes = "이전 경로입니다. /partners/{id}/sbom/file 사용을 권장합니다.", hidden = true)
    @ApiResponses({
            @ApiResponse(code = 200, message = "파일 다운로드 성공", response = FileSystemResource.class),
            @ApiResponse(code = 400, message = "필수 format 누락", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"message\":\"'format' parameter is missing or misspelled\"}"))),
            @ApiResponse(code = 401, message = "인증 실패\n\n" +
              "**에러 코드 (errorCode):**\n\n" +
              "* `TOKEN_INVALID` - 유효하지 않거나 변조된 토큰 (다시 로그인 필요)\n" +
              "* `TOKEN_EXPIRED` - 토큰 만료 (Refresh Token으로 갱신 필요)", 
                    examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"errorCode\": \"TOKEN_INVALID\",\"message\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "3rd Party 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"message\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"message\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {"/partners/{id}/bom/file"})
    public ResponseEntity<FileSystemResource> get3rdDownloadDeprecated(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id") String partnerId,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet") @RequestParam String format) throws Exception {
        return get3rdDownloadInternal(authorization, partnerId, format);
    }

    private ResponseEntity<FileSystemResource> get3rdDownloadInternal(
            String authorization,
            String partnerId,
            String format) throws Exception {

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

    @ApiOperation(value = "3rd Party SBOM JSON 조회", notes = "3rd Party 프로젝트의 SBOM 데이터를 JSON으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"sample-oss\":[{\"version\":\"1.0.0\",\"license\":[\"Apache-2.0\"],\"download location\":\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\",\"homepage\":\"https://example.org/sample-oss\",\"copyright text\":[\"Copyright 2026 Example Authors\"],\"exclude\":false,\"comment\":\"Used by Example SDK\",\"Vulnerability\":\"7.5\"}]}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패\n\n" +
              "**에러 코드 (errorCode):**\n\n" +
              "* `TOKEN_INVALID` - 유효하지 않거나 변조된 토큰 (다시 로그인 필요)\n" +
              "* `TOKEN_EXPIRED` - 토큰 만료 (Refresh Token으로 갱신 필요)",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"errorCode\": \"TOKEN_INVALID\",\"message\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "프로젝트 접근 권한 없음 (EDIT)\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\":\"The user does not have edit permissions for Project 123\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PARTNER_JSON})
    public ResponseEntity<Map<String, Object>> get3rdAsJson(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId) {
        return get3rdAsJsonInternal(authorization, partnerId);
    }

    @ApiOperation(value = "3rd Party SBOM JSON 조회 (Deprecated)", notes = "이전 경로입니다. /partners/{id}/sbom/json-data 사용을 권장합니다.", hidden = true)
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"sample-oss\":[{\"version\":\"1.0.0\",\"license\":[\"Apache-2.0\"],\"download location\":\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\",\"homepage\":\"https://example.org/sample-oss\",\"copyright text\":[\"Copyright 2026 Example Authors\"],\"exclude\":false,\"comment\":\"Used by Example SDK\",\"Vulnerability\":\"7.5\"}]}"))),
            @ApiResponse(code = 401, message = "인증 실패\n\n" +
              "**에러 코드 (errorCode):**\n\n" +
              "* `TOKEN_INVALID` - 유효하지 않거나 변조된 토큰 (다시 로그인 필요)\n" +
              "* `TOKEN_EXPIRED` - 토큰 만료 (Refresh Token으로 갱신 필요)", 
                    examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"errorCode\": \"TOKEN_INVALID\",\"message\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "3rd Party 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"message\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"message\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {"/partners/{id}/bom/json-data"})
    public ResponseEntity<Map<String, Object>> get3rdAsJsonDeprecated(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "3rd Party ID", required = true) @PathVariable(name = "id", required = true) String partnerId) {
        return get3rdAsJsonInternal(authorization, partnerId);
    }

    private ResponseEntity<Map<String, Object>> get3rdAsJsonInternal(
            String authorization,
            String partnerId) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (!apiPartnerService.checkUserHasPartnerProject(userInfo, partnerId)) {
            throw new CProjectNotAvailableException(partnerId);
        }

        resultMap = apiPartnerService.getExportJson(partnerId);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }
}
