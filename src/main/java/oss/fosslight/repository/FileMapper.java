/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.T2File;

@Mapper
public interface FileMapper {
	public int insertFile(T2File file);
	
	public T2File getFileInfo(T2File file);
	
	public T2File getFileKey(T2File file);

	public String getFileId();

	public List<T2File> getFileInfoList(String fileId);

	public int updateFileDelYn(String[] fileSeqs);
	
	public int updateFileDelYnById(String fileId);

	public List<T2File> getFileInfoListKessan(T2File param);

	public int updateFileDelYnKessan(T2File deleteFile);
	
	public int updateFileDelYnByFilePathNm(T2File deleteFile);

	public T2File selectFileInfo(String fileSeq);

	public T2File selectFileInfoById(String fileId);

	public void copyFileInfo(T2File fileInfo);

	public T2File selectFileInfoByName(T2File bean);

	public void upateOrgFileName(T2File packageFileInfo);
	
	public List<T2File> selectPackagingFileInfo(String prjId);
	
	public List<T2File> getReusePackagingInfo();
	
	public T2File selectReuseFileInfo(@Param("prjId") String prjId
			 						, @Param("logiPath") String logiPath);
	
	public int updateReuseChkFileDelYnByFilePathNm(T2File deleteFile);
	
	public int updateBatFile(T2File deleteFile);
	
	public T2File selectReuseFileByFilePathNm(T2File deleteFile);
	
	public List<T2File> getPackgingReuseCntToList(String prjId);
	
	public int getPackgingReuseCnt(String logiNm);
	
	public int setReusePackagingFileHidden(@Param("prjId") String prjId
										 , @Param("logiPath") String logiPath
										 , @Param("logiNm") String logiNm);
	
	public List<T2File> getBinAndroidFileList(@Param("prjId") String prjId, @Param("ossReportId") String ossReportId);

	public T2File getFileInfo2(T2File file);

	public void insertCopyPhysicalFileInfo(T2File fileInfo);

	public T2File selectPackagingVulDOCFileInfo(String prjId);
}