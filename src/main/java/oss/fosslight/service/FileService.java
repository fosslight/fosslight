/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;


public interface FileService {
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile);
	
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName);

	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String fileId);
	
	public List<UploadFile> uploadNoticeXMLFile(HttpServletRequest req, T2File registFile, String oldFileId, String prjId);
	
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, boolean randomNm, String filePath);
	
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String fileId, boolean randomNm, String filePath);
	
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String fileId, boolean randomNm, String filePath, boolean isOrigFile);

	public T2File selectFileInfo(String fileSeq);

	public String registFileWithFileName(String filePath, String fileName);
	
	public String registFileDownload(String filePath, String fileName, String logiFileName);

	public T2File selectFileInfoById(String fileSeq);

	public String copyFileInfo(String orgFileId);

	public T2File selectFileInfoByLogiName(T2File bean);
	
	public List<UploadFile> uploadWgetFile(HttpServletRequest req, T2File registFile, Map<Object, Object> map, boolean isOrigFile);
	
	public String setClearFiles(Map<Object, Object> map);
	
	public void deleteFiles(String url, List<T2File> uploadFileInfo, String prjId, T2File vulDOCFileInfo);
	
	public List<UploadFile> setReusePackagingFile(String refFileSeq);

	public void deletePhysicalFile(T2File t2File, String flag);

	public String copyPhysicalFile(String fileId);

	T2File uploadSingleFile(MultipartFile mFile, String fileId ,String fileGubn, Path of, boolean useRandomFileName);
}
