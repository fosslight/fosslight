/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v1;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.API;
import oss.fosslight.service.ApiBatService;
import oss.fosslight.service.NvdDataService;
import oss.fosslight.service.T2UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@Api(tags = {"99. Test"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
public class ApiTestController extends CoTopComponent {

    private final ResponseService responseService;

    private final NvdDataService nvdDataService;

    @ApiOperation(value = "Search Binary List", notes = "Binary Info 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {"/test-sync"})
    public CommonResult getBinaryInfo(
            @RequestHeader String _token) {

        String resCd = "";
        try {
            resCd = nvdDataService.executeNvdDataSync();
        } catch (IOException ioe) {
        }
        return responseService.getSingleResult("");
    }
}
