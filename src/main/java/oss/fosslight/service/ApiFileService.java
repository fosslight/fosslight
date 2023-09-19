/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import oss.fosslight.domain.UploadFile;

public interface ApiFileService {
	public UploadFile uploadFile(MultipartFile uploadFile);
	
	public UploadFile uploadFile(MultipartFile uploadFile, String filePath);
	
	public UploadFile uploadFile(MultipartFile uploadFile, String filePath, String fileId);
	
	public Map<String, UploadFile> uploadNoticeXMLFile(MultipartFile uploadFile, String prjId);
}
