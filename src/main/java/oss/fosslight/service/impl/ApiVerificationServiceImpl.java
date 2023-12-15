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
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.ApiSelfCheckMapper;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.service.ApiSelfCheckService;
import oss.fosslight.service.ApiVerificationService;
import oss.fosslight.service.FileService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApiVerificationServiceImpl extends CoTopComponent implements ApiVerificationService {
    @Autowired
    VerificationMapper verificationMapper;

    @Autowired
    ApiSelfCheckService apiSelfCheckService;

    @Autowired
    ApiSelfCheckMapper apiSelfCheckMapper;

    @Autowired
    FileService fileService;

    @Autowired
    FileMapper fileMapper;

    @Override
    public ListSelfCheckVerifyOssDto.Result getVerifyOssList(String id) {
        var ossList = verificationMapper.selectSelfCheckVerifyOssList(id);
        return ListSelfCheckVerifyOssDto.Result
                .builder()
                .oss(ossList)
                .build();
    }

    @Override
    public List<T2File> saveUploadedFile(String selfCheckId, List<UploadFile> files) {
        var originalList = apiSelfCheckService.listSelfCheckPackages(selfCheckId);
        var idList = originalList.stream().map(file -> file.getFileId()).collect(Collectors.toList());
        for (UploadFile file : files) {
            idList.add(file.getRegistFileId());
        }
        var idText = String.join(",", idList);
        apiSelfCheckMapper.saveSelfCheckPackages(selfCheckId, idText);
        return apiSelfCheckService.listSelfCheckPackages(selfCheckId);
    }

    @Override
    public List<T2File> deleteSelfCheckPackageFile(String selfCheckId, String fileId) {
        var originalList = apiSelfCheckService.listSelfCheckPackages(selfCheckId);
        var fileToDelete = fileService.selectFileInfoById(fileId);

        fileMapper.updateFileDelYnById(fileId);
        fileService.deletePhysicalFile(fileToDelete, null);

        var seqList = originalList.stream().map(file -> file.getFileId())
                .filter(id -> !Objects.equals(id, fileId))
                .collect(Collectors.toList());
        var idText = String.join(",", seqList);
        apiSelfCheckMapper.saveSelfCheckPackages(selfCheckId, idText);
        return apiSelfCheckService.listSelfCheckPackages(selfCheckId);
    }
}
