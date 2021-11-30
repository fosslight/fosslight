/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApiFileMapper {

	Map<String, Object> getFileInfo(Map<String, Object> paramT2File);

	List<Map<String, Object>> selectPackagingFileInfo(String prjId);

	int getPackgingReuseCnt(String rtnFileName);

	int updateFileDelYnKessan(Map<String, Object> delFile);

	Map<String, Object> selectFileInfo(String fileId);

	List<Map<String, Object>> getReusePackagingInfo();

	int updateReuseChkFileDelYnByFilePathNm(Map<String, Object> delFile);

	List<Map<String, Object>> getPackgingReuseCntToList(String prjId);

	int updateFileDelYnByFilePathNm(Map<String, Object> delFile);

	void setReusePackagingFileHidden(@Param("refPrjId") String refPrjId, @Param("logiPath") String logiPath, @Param("getName") String logiNm);
	
}
