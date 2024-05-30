/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;

public interface VerificationService {
	Map<String, Object> getVerificationOne(Project project);
	
	Map<String, Object> registOssNotice(OssNotice ossNotice);
	
	String getNoticeHtml(OssNotice ossNotice) throws IOException;
	
	boolean getNoticeHtmlFile(OssNotice ossNotice) throws IOException;
	
	boolean getNoticeHtmlFile(OssNotice ossNotice, String html) throws IOException;

	boolean getReviewReportPdfFile(String prjId) throws IOException;

	boolean getReviewReportPdfFile(String prjId, String html) throws IOException;
	
	ResponseEntity<FileSystemResource> getNotice(String fileName, String rESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX) throws IOException;

	ResponseEntity<FileSystemResource> getReviewReport(String fileName, String rESOURCE_PUBLIC_DOWNLOAD_REVIEW_REPORT_FILE_PATH_PREFIX) throws IOException;
	
	ResponseEntity<FileSystemResource> getPackage(String prjId, String rESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX) throws IOException;
	
	ResponseEntity<FileSystemResource> 	getPackageMulti(String prjId, String rESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX, String fileIdx) throws IOException;
	
	List<OssComponents> getVerifyOssList(Project projectMaster);
	
	Map<String, Object> processVerification(Map<Object, Object> map, T2File file, Project project);
	
	void savePath(Map<Object, Object> map);
	
	String getNoticeHtmlFileForPreview(OssNotice ossNotice) throws IOException;
	
	boolean checkNetworkServer(String prjId);
	
	String getNoticeTextFileForPreview(OssNotice ossNotice) throws IOException;
	
	String getNoticeTextFileForPreview(OssNotice ossNotice, boolean isConfirm) throws IOException;
	
	void changePackageFileNameDistributeFormat(String prjId);
	
	Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice);
	
	Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice, boolean isProtocol);
	
	OssNotice selectOssNoticeOne(String prjId);
	
	void updateStatusWithConfirm(Project project, OssNotice ossNotice, boolean copyConfirmFlag) throws Exception;
	
	Map<String, Integer> setAddFileCount(Map<String, Integer> deCompResultMap, String url, int fileCnt) throws Exception;
	
	List<String> sortByValue(Map<String, Integer> map) throws Exception;
	
	void updateVerifyFileCount(Map<String,Object> fileCounts);
	
	void updateVerifyFileCountReset(List<String> fileCounts);
	
	boolean getChangedPackageFile(String prjId, List<String> fileSeqs);
	
	Map<String, Object> getReuseProject(Project project);
	
	Map<String, Object> getReuseProjectPackagingFile(Project project);
	
	boolean setReusePackagingFile(Map<String, Object> map);
	
	List<OssComponents> setMergeGridData(List<OssComponents> gridData);

	void setUploadFileSave(String prjId, String fileSeq, String registFileId) throws Exception;
	
	public void updateProjectAllowDownloadBitFlag(Project project);

	void registOssNoticeConfirmStatus(OssNotice ossNotice);

	String changePackageFileNameCombine(String prjId);
	
	void deleteFile(Map<Object, Object> map);
}