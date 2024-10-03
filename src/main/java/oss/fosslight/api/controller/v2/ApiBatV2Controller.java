/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.service.ApiBatService;
import oss.fosslight.service.T2UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "7. Binary")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
@Profile(value = {"stage", "prod"})
public class ApiBatV2Controller extends CoTopComponent {

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiBatService apibatService;

    @Operation(summary = "Search Binary List", description = "Binary Info 조회")
    @GetMapping(value = {APIV2.FOSSLIGHT_API_BINARY_SEARCH})
    public ResponseEntity<Map<String, Object>> getBinaryInfo(
            @Parameter(hidden=true) @RequestHeader String authorization,
            @Parameter(description = "Binary Name", required = false) @RequestParam(required = false) String fileName,
            @Parameter(description = "Tlsh", required = false) @RequestParam(required = false) String tlsh,
            @Parameter(description = "checksum", required = false) @RequestParam(required = false) String checksum,
            @Parameter(description = "Platform Name", required = false) @RequestParam(required = false) String platformName,
            @Parameter(description = "PlatForm Version", required = false) @RequestParam(required = false) String platformVersion,
            @Parameter(description = "Source Path", required = false) @RequestParam(required = false) String sourcePath) {

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
