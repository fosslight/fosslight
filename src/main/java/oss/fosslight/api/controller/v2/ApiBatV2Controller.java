/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.service.ApiBatService;
import oss.fosslight.service.T2UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"07. Binary"}, description = " ")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
@Profile(value = {"stage", "prod"})
public class ApiBatV2Controller extends CoTopComponent {

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiBatService apibatService;

    @ApiOperation(value = "Binary 정보 조회", notes = "fileName, tlsh, checksum 중 하나 이상을 사용하여 Binary 매칭 정보를 조회합니다. platformName, platformVersion, sourcePath는 추가 필터입니다.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"content\":[{\"binaryFileName\":\"bash\",\"path\":\"/bin/bash\",\"ossName\":\"bash\",\"ossVersion\":\"5.2.21\",\"license\":\"GPL-3.0-or-later\",\"projectName\":\"sample-platform\",\"checksum\":\"a1b2c3d4e5f6\",\"tlsh\":\"T1A2B3C4D5E6F\",\"updateDate\":\"2026-08-20\",\"downloadlocation\":\"https://ftp.gnu.org/gnu/bash/bash-5.2.21.tar.gz\"}]}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "잘못된 요청 - fileName, tlsh, checksum 중 하나 이상 필수\n\n",
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
                    code = 500,
                    message = "서버 내부 오류\n\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"message\": \"Unknown error.\"}"))
            )
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_BINARY_SEARCH})
    public ResponseEntity<Map<String, Object>> getBinaryInfo(
            @ApiParam(hidden=true) @RequestHeader String authorization,
            @ApiParam(value = "Binary Name", required = false) @RequestParam(required = false) String fileName,
            @ApiParam(value = "Tlsh", required = false) @RequestParam(required = false) String tlsh,
            @ApiParam(value = "checksum", required = false) @RequestParam(required = false) String checksum,
            @ApiParam(value = "Platform Name", required = false) @RequestParam(required = false) String platformName,
            @ApiParam(value = "PlatForm Version", required = false) @RequestParam(required = false) String platformVersion,
            @ApiParam(value = "Source Path", required = false) @RequestParam(required = false) String sourcePath) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        // 전부 null이면 parameter error return
        if (!isEmpty(fileName)
                || !isEmpty(tlsh)
                || !isEmpty(checksum)) {
            paramMap.put("fileName", fileName);
            paramMap.put("tlsh", tlsh);
            paramMap.put("checksum", checksum);
            paramMap.put("platformName", platformName);
            paramMap.put("platformVersion", platformVersion);
            paramMap.put("sourcePath", sourcePath);

            List<Map<String, Object>> contents = apibatService.getBatList(paramMap);

            if (contents != null) {
                resultMap.put("content", contents);
            }

            return ResponseEntity.ok(resultMap);

        } else {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }
    }
}
