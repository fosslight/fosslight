/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service;

import oss.fosslight.api.dto.ListSelfCheckVerifyOssDto;

public interface ApiVerificationService {
    ListSelfCheckVerifyOssDto.Result getVerifyOssList(String string);
}