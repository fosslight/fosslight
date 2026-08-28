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
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.service.ApiCodeService;
import oss.fosslight.service.T2UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"06. Code v2"})
@RequiredArgsConstructor
@RestController()
@RequestMapping(value = "/api/v2")
public class ApiCodeV2Controller extends CoTopComponent {

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiCodeService apiCodeService;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");

    @ApiOperation(value = "공통 코드 조회", notes = "codeType에 해당하는 활성 상세 코드를 조회합니다. detailValue를 지정하면 코드명에 포함된 값만 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"content\":[{\"cdDtlNo\":\"101\",\"cdDtlNm\":\"LGE\"}]}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - codeType 누락\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"message\":\"'codeType' parameter is missing or misspelled\"}"))
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
                    code = 404,
                    message = "조회 결과 없음 - 응답 body 없음"
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_CODE_SEARCH})
    public ResponseEntity<Map<String, Object>> getVulnerabilityData(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "code Type (DIV:Division, OS:Os Type, DSTT:Distribution Type, DSTS:Distribution Site, NOTI:NOTICE TYPE, NP:NOTICE PLATFORM, PRI:PRIORITY)", required = true, allowableValues = "DIV,OS,DSTT,DSTS,NOTI,NP,PRI") @RequestParam(required = true) String codeType,
            @ApiParam(value = "detail Value", required = false) @RequestParam(required = false) String detailValue) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> contents = apiCodeService.getCodeList(codeType, detailValue);
        if (contents.size() == 0) {
            return ResponseEntity.notFound().build();
        }
        result.put("content", contents);
        return ResponseEntity.ok(result);
    }
}
