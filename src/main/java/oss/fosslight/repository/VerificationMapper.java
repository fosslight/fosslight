/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import oss.fosslight.api.dto.OssDto;
import oss.fosslight.domain.File;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;

@Mapper
public interface VerificationMapper {
	OssNotice selectOssNoticeOne(Project project);
	
	OssNotice selectOssNoticeOne2(Project project);
	
	String selectNoticeAppendInfo(@Param("prjId") String prjId);
	
	List<OssComponents> selectVerifyOssList(Project project);

	List<OssDto> selectSelfCheckVerifyOssList(String selfCheckId);

	void updateComment(CommentsHistory commentHistory);
	
	void deleteComment(CommentsHistory commentHistory);
	
	int insertOssNotice(OssNotice ossNotice);
	
	List<OssComponents> selectVerificationNotice(OssNotice ossNotice);
	
	File selectVerificationFile(String packageFileId);
	
	File selectVerificationVulDocFile(String packageVulDocFileId);
	
	T2File selectPackageFileName(@Param("prjId") String prjId, @Param("fileIdx") String fileIdx);
	
	void updateVerificationStatusProgress(OssNotice ossNotice);
	
	void insertPackagingComponents(OssComponents component);
	
	void deletePackagingComponents(OssNotice ossNotice);
	
	void updateNoticeFileInfo(Project projectParam);

	void updateReviewReportFileInfo(Project projectParam);
	
	List<OssComponents> selectVerificationNoticeClassAppend(OssNotice ossNotice);
	
	void updateVerifyFilePath(OssComponents bean);
	
	void updatePackageFile(Project prjParam);
	
	void updateNoticeFileInfoEtc(Project project);
	
	int existsOssNotice(String prjId);
	
	void updateVerifyFileCount(OssComponents bean);
	
	int updateOssNotice(OssNotice ossNotice);
	
	int checkPackagingFileId(@Param("prjId") String prjId, @Param("packageFileId") String packageFileId, @Param("packageFileId2") String packageFileId2
							, @Param("packageFileId3") String packageFileId3, @Param("packageFileId4") String packageFileId4, @Param("packageFileId5") String packageFileId5);
	
	int setPackagingReuseMap(@Param("prjId") String prjId, @Param("fileSeq") String fileSeq
							, @Param("refPrjId") String refPrjId, @Param("refFileSeq") String refFileSeq);
	
	int updatePackagingReuseMap(Project project);

	OssComponents checkOssNickName2(OssComponents bean);

	void deletePackagingFileInfo(T2File file);

	void deleteReuseFileInfo(T2File file);

	int countSameLogiFile(T2File file);
	
	void updateNoticeAppendFile(Project project);
	
	List<T2File> selectNoticeAppendFile(@Param("noticeAppendFileId") String noticeAppendFileId);
}
