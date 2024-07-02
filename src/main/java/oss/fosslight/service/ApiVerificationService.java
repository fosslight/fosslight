/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service;

import oss.fosslight.api.dto.ListSelfCheckVerifyOssDto;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;

import java.util.List;

public interface ApiVerificationService {
    ListSelfCheckVerifyOssDto.Result getVerifyOssList(String string);

    List<T2File> saveUploadedFile(String selfCheckId, List<UploadFile> files);

    List<T2File> deleteSelfCheckPackageFile(String selfCheckId, String fileId);
}