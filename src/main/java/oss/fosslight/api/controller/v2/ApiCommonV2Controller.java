/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
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
    @ApiOperation(value = "Division 병합", notes = "관리자 전용 API입니다. from Division의 사용자와 프로젝트/3rd Party 정보를 to Division으로 이동합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"success\":true}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "필수 from 또는 to 누락",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"message\":\"'from' parameter is missing or misspelled\"}"))
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
                    message = "권한 없음 - 관리자 아님\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
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
                result.put("success", true);
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
    @ApiOperation(value = "Division 추가", notes = "관리자 전용 API입니다. 사용자 Division 코드에 이름과 설명을 추가합니다. 같은 이름이 있으면 success=false와 기존 코드 번호를 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"success\":true,\"cdDtlNo\":\"201\"}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - cdDtlNm 누락\n\n",
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
                    message = "권한 없음 - 관리자 아님\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
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
    @ApiOperation(value = "Division 수정", notes = "관리자 전용 API입니다. 코드 번호에 해당하는 Division 이름 또는 설명을 수정합니다. 생략한 값은 변경하지 않습니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"success\":true,\"cdDtlNo\":\"201\",\"cdDtlNm\":\"Updated Division\",\"cdDtlExp\":\"Updated description\"}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - cdDtlNo 누락 또는 cdDtlNm/cdDtlExp 둘 다 누락\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"At least one of cdDtlNm or cdDtlExp is required.\"}"))
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
                    message = "권한 없음 - 관리자 아님\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 404,
                    message = "리소스 없음 - cdDtlNo not found\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"The resource does not exist or User does not have permissions for the resource (resource example: project)\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
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
    @ApiOperation(value = "Division 목록 조회", notes = "활성 사용자 Division 코드의 번호, 이름, 설명을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"content\":[{\"cdDtlNo\":\"101\",\"cdDtlNm\":\"LGE\",\"cdDtlExp\":\"LGE Division\"}]}"))
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
    @ApiOperation(value = "전체 사용자 기본 정보 조회", notes = "관리자 전용 API입니다. 전체 사용자의 ID, 이름, 이메일, Division, 사용 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"content\":[{\"user_id\":\"admin\",\"user_name\":\"Administrator\",\"email\":\"admin@example.com\",\"division\":\"LGE\",\"use_yn\":\"Y\"}]}"))
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
                    message = "권한 없음 - 관리자 아님\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
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
    @ApiOperation(value = "사용자 Division 수정", notes = "관리자 전용 API입니다. 사용자 ID에 활성 Division 코드를 지정합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"success\":true,\"user_id\":\"user01\",\"division\":\"LGE\"}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - 유효하지 않은 division 코드\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\":\"Invalid division: 999\"}"))
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
                    message = "권한 없음 - 관리자 아님\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"You do not have permission.\"}"))
            ),
            @ApiResponse(
                    code = 404,
                    message = "사용자 없음\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\":\"User id not found: user01\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
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
