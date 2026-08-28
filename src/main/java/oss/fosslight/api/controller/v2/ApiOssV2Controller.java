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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.annotation.InternalApi;
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
import oss.fosslight.service.RefineOssService;
import oss.fosslight.service.T2UserService;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(tags = {"01. OSS & License"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
@Validated
public class ApiOssV2Controller extends CoTopComponent {
    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiOssService apiOssService;

    private final OssService ossService;

    private final ApiLicenseService apiLicenseService;
    
    private final RefineOssService refineOssService;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");


    @ApiOperation(value = "OSS 목록 조회", notes = "OSS 이름, 버전, Download URL 또는 CVE ID로 OSS를 검색합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = ListOssDto.Result.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"list\":[{\"ossId\":\"1\",\"ossType\":\"100\",\"ossTypeMap\":{\"Multi\":\"Y\",\"Dual\":\"N\",\"V-Diff\":\"N\"},\"ossName\":\"sample-oss\",\"ossVersion\":\"1.0.0\",\"licenseName\":\"Apache-2.0\",\"licenseType\":\"PMS\",\"downloadUrl\":\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\",\"downloadUrls\":[\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\"],\"homepageUrl\":\"https://example.org/sample-oss\",\"description\":\"Sample open source component\",\"cveId\":\"CVE-2026-1234\",\"cvssScore\":\"7.5\",\"creator\":\"user01\",\"created\":\"2026-08-01 09:00:00\",\"modifier\":\"user02\",\"modified\":\"2026-08-20 14:30:00\",\"obligations\":[\"Y\",\"N\"],\"obligationTypeMap\":{\"Notice\":\"Y\",\"Source\":\"N\"},\"copyright\":\"Copyright 2026 Example Authors\",\"nicknames\":\"sample|sample-lib\",\"nicknameList\":[\"sample\",\"sample-lib\"],\"attribution\":\"This product includes sample-oss.\",\"exclude\":false}],\"totalCount\":1}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - countPerPage/page 검증 실패\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Input value=0. countPerPage must be larger than 1\"}"))
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
    @GetMapping(value = {APIV2.FOSSLIGHT_API_OSS_SEARCH})
    public @ResponseBody ResponseEntity<ListOssDto.Result> getOssInfo(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "OSS Name", required = false) @RequestParam(required = false) String ossName,
            @ApiParam(value = "OSS Name Exact Flag (values: Y or N)") @RequestParam(required = false, defaultValue="Y") String ossNameExact,
            @ApiParam(value = "OSS Version", required = false) @RequestParam(required = false) String ossVersion,
            @ApiParam(value = "Download Location", required = false) @RequestParam(required = false) String downloadLocation,
            @ApiParam(value = "Download Location Exact Flag (values: Y or N)", required = false) @RequestParam(required = false, defaultValue="Y") String downloadLocationExact,
            @ApiParam(value = "CVE ID", required = false) @RequestParam(required = false) String cveId,
            @ApiParam(value = "Count Per Page (max: 10000)", required = false)
            @Min(value = 1, message="Input value=${validatedValue}. countPerPage must be larger than {value}") @RequestParam(required = false, defaultValue="10000") int countPerPage,
            @ApiParam(value = "Page", required = false)
            @Min(value=1, message="Input value=${validatedValue}. page must be larger than {value}") @RequestParam(required = false, defaultValue="1") int page
    ) {
        // 사용자 인증
        userService.checkApiUserAuth(authorization);

        ListOssDto.Request ossQuery =
                ListOssDto.Request.builder()
                        .ossName(ossName)
                        .url(downloadLocation)
                        .ossVersion(ossVersion)
                        .ossNameExact(Objects.equals(ossNameExact, "Y"))
                        .urlExact(Objects.equals(downloadLocationExact, "Y"))
                        .cveId(cveId != null ? cveId.trim() : null)
                        .build();
        ossQuery.setPage(page);
        ossQuery.setCountPerPage(countPerPage);

        var map = apiOssService.listOss(ossQuery);
        if (!userService.isApiAdmin(authorization) && map.list != null) {
            map.list.forEach(oss -> oss.setExclude(null));
        }
        return ResponseEntity.ok(map);
    }


    @ApiOperation(value = "License 목록 조회", notes = "License 이름으로 License 정보를 검색합니다. 이름의 완전 일치 여부와 페이지 정보를 지정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = ListLicenseDto.Result.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"list\":[{\"licenseId\":\"1\",\"licenseName\":\"Apache License 2.0\",\"licenseType\":\"Permissive\",\"licenseText\":\"Apache License Version 2.0, January 2004\",\"licenseIdentifier\":\"Apache-2.0\",\"homepageUrl\":\"https://www.apache.org/licenses/LICENSE-2.0\",\"description\":\"A permissive open source license.\",\"creator\":\"admin\",\"modifier\":\"admin\",\"created\":\"2026-08-01 09:00:00\",\"modified\":\"2026-08-20 14:30:00\",\"restrictions\":[\"Include License\",\"Notice\"],\"licenseNickname\":\"Apache 2.0\",\"obligations\":[\"Y\",\"N\"],\"attribution\":\"Licensed under the Apache License, Version 2.0.\"}],\"totalCount\":1}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - countPerPage/page 검증 실패\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Input value=0. countPerPage must be larger than 1\"}"))
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
    @GetMapping(value = {APIV2.FOSSLIGHT_API_LICENSE_SEARCH})
    public @ResponseBody ResponseEntity<ListLicenseDto.Result> getLicenseInfo(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "License Name", required = false) @RequestParam(required = false) String licenseName,
            @ApiParam(value = "License Name Exact Flag (values: Y or N)", required = false) @RequestParam(required = false, defaultValue="Y") String licenseNameExact,
            @ApiParam(value = "Count Per Page (max 10000)", required = false)
            @Min(value = 1, message="Input value=${validatedValue}. countPerPage must be larger than {value}") @RequestParam(required = false, defaultValue="10000") int countPerPage,
            @ApiParam(value = "Page", required = false)
            @Min(value=1, message="Input value=${validatedValue}. page must be larger than {value}") @RequestParam(required = false, defaultValue="1") int page) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        ListLicenseDto.Request licenseQuery =
                ListLicenseDto.Request.builder()
                        .licenseName(licenseName)
                        .licenseNameExact(Objects.equals(licenseNameExact, "Y"))
                        .build();
        licenseQuery.setPage(page);
        licenseQuery.setCountPerPage(countPerPage);

        var map = apiLicenseService.listLicenses(licenseQuery);
        return ResponseEntity.ok(map);
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
//
//    @ApiOperation(value = "Search License Info", notes = "License Info 조회")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
//    })
//    @GetMapping(value = {APIV2.FOSSLIGHT_API_LICENSE_SEARCH})
//    public ResponseEntity<Map<String, Object>> getLicenseInfo(
//            @RequestHeader String authorization,
//            @ApiParam(value = "License Name", required = false) @RequestParam(required = false) String licenseName) {
//
//        // 사용자 인증
//        userService.checkApiUserAuth(authorization);
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//
//        try {
//            List<Map<String, Object>> content = apiOssService.getLicenseInfo(licenseName);
//            if (content.size() == 0) {
//                return responseService.errorResponse(HttpStatus.NOT_FOUND, "license not found");
//            }
//            resultMap.put("content", content);
//            return ResponseEntity.ok(resultMap);
//        } catch (Exception e) {
//            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
//                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
//        }
//    }

    @InternalApi
    @ApiOperation(value = "OSS 등록 또는 수정", notes = "관리자 전용 API입니다. OSS Master를 등록하거나 ossId가 있으면 기존 OSS를 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"ossMaster\":{\"ossId\":\"101\",\"ossCommonId\":\"51\",\"ossName\":\"sample-oss\",\"ossVersion\":\"1.0.0\",\"homepage\":\"https://example.org/sample-oss\",\"downloadLocation\":\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\",\"licenseName\":\"Apache-2.0\",\"deactivateFlag\":\"N\",\"comment\":\"Registered through API\"},\"ossId\":\"101\",\"ossCommonId\":\"51\",\"isNew\":true,\"isNewVersion\":false,\"isChangedName\":false,\"isDeactivateFlag\":false,\"isActivateFlag\":false,\"resCd\":\"00\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "권한 없음 - 관리자 아님\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"Unknown error.\"}"))
            )
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_OSS_REGISTER})
    public ResponseEntity<Map<String, Object>> registerOss(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "OSS Master", required = true) @RequestBody(required = true) OssMaster ossMaster) {

        if (userService.isAdmin(authorization)) {
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap = ossService.saveOss(ossMaster);
            resultMap = ossService.sendMailForSaveOss(resultMap);
            return ResponseEntity.ok(resultMap);
        }
        return responseService.errorResponse(HttpStatus.FORBIDDEN,
                CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
    }
    
    @InternalApi
    @ApiOperation(value = "OSS Download Location 정제", notes = "관리자 전용 API입니다. URL 형식 정리, 중복 제거, PURL 생성, GitHub 우선순위 재정렬 중 선택한 작업을 실행합니다. doUpdateFlag=N이면 결과만 확인합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"UPDATE-DOWNLOAD-LOCATION-FORMAT\":{\"reFineTotalCnt\":1,\"reFineItems\":{\"sample-oss_1.0.0\":[\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\"]}}}"))
            ),
            @ApiResponse(code = 400, message = "필수 doUpdateFlag 또는 refineType 누락", examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"msg\":\"'refineType' parameter is missing or misspelled\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(
                    code = 403,
                    message = "권한 없음 / 서버 내부 오류 - 관리자 아님 ⚠️\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\": \"You do not have permission.\"}"))
            )
    })
	@GetMapping(value = {APIV2.FOSSLIGHT_API_OSS_REFINE_DOWNLOAD_LOCATION})
    public ResponseEntity<Map<String, Object>> refineOssDownloadLocation(
            @ApiParam(hidden=true) @RequestHeader String authorization,
    		@ApiParam(value = "OSS Name", required = false) @RequestParam(required = false) String ossName,
    		@ApiParam(value = "Do Update Database", required = true, defaultValue = "N", allowableValues = "N,Y") @RequestParam(required = true) String doUpdateFlag,
    		@ApiParam(value = "Refine Type", required = true, allowableValues = "0.UPDATE DOWNLOAD LOCATION FORMAT,1.REMOVE DUPLICATED DOWNLOAD LOCATION,2.PUT PURL,3.REMOVE DUPLICATED PURL,4.REORDER GITHUB PRIORITY,5.REFINE ALL") @RequestParam(required = true) String refineType){
		
		// 사용자 인증
		if (!userService.isAdmin(authorization)) {
			return responseService.errorResponse(HttpStatus.FORBIDDEN,
					CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
		}
		return ResponseEntity.ok(refineOssService.refineDownloadLocation(ossName, refineType, "Y".equalsIgnoreCase(doUpdateFlag)));
    }
}
