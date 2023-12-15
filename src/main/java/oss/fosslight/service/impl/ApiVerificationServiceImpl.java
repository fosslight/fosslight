/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.ListSelfCheckVerifyOssDto;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.service.ApiVerificationService;

@Service
@Slf4j

public class ApiVerificationServiceImpl extends CoTopComponent implements ApiVerificationService {
    @Autowired
    VerificationMapper verificationMapper;

    @Override
    public ListSelfCheckVerifyOssDto.Result getVerifyOssList(String id) {
        var ossList = verificationMapper.selectSelfCheckVerifyOssList(id);
        return ListSelfCheckVerifyOssDto.Result
                .builder()
                .oss(ossList)
                .build();
    }
}
