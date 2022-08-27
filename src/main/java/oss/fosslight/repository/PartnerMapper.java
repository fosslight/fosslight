/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.PartnerWatcher;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;

@Mapper
public interface PartnerMapper {
	public int selectPartnerMasterTotalCount(PartnerMaster partnerMaster);

	public List<PartnerMaster> selectPartnerList(PartnerMaster partnerMaster);

	public int updateReviewer(PartnerMaster vo);

	public List<PartnerMaster> getPartnerNameList(PartnerMaster partnerMaster);

	public void registPartnerMaster(PartnerMaster partnerMaster);

	public void registPartnerWatcher(PartnerMaster partnerMaster);

	public String selectpartnerLastId();

	public PartnerMaster selectPartnerMaster(PartnerMaster partnerMaster);
	
	public List<PartnerWatcher> selectPartnerWatcher(PartnerMaster partnerMaster);

	public void deleteWatcher(PartnerMaster partnerMaster);

	public void deleteMaster(PartnerMaster partnerMaster);

	public void updateComment(CommentsHistory result);

	public void deleteComment(CommentsHistory commentsHistory);

	public OssComponents getOssComponents(OssComponents ossComponents);

	public List<OssComponentsLicense> getOssLicense(String ossId);

	public void deleteOssComponents(String partnerId);

	public List<OssComponents> selectComponentId(String partnerId);

	public void deleteOssComponentsLicense(OssComponents componentId);

	public void insertOssComponents(OssComponents ossComponents);

	public String selectLastComponentsId();

	public String getLicenseId(OssComponentsLicense ossComponentsLicense);

	public void insertOssComponentsLicense(OssComponentsLicense ossComponentsLicense);

	public List<PartnerMaster> selectPartnerListExcel(PartnerMaster partner);

	public int selectPartnerMasterTotalCountUser(PartnerMaster partnerMaster);

	public List<PartnerMaster> selectPartnerListUser(PartnerMaster partnerMaster);

	public int existPartnerData(PartnerMaster partnerMaster);

	public List<T2Users> getUserList(T2Users t2Users);

	public void changeStatus(PartnerMaster partnerMaster);

	public List<PartnerMaster> getPartnerSwNmList(PartnerMaster partnerMaster);

	public List<PartnerMaster> getPartnerSwVerList(PartnerMaster partnerMaster);

	public List<ProjectIdentification> getPartnerOssAllList(OssComponents param);
	
	public List<PartnerMaster> select3rdMapList(Project project);
	
	public List<PartnerMaster> selectThirdPartyMapList(String prjId);
	
	void updatePartnerMapList(PartnerMaster partnerMaster);
	
	void deletePartnerMapList(PartnerMaster partnerMaster);
	
	void insertPartnerMapList(PartnerMaster partnerMaster);
	
	public List<PartnerMaster> selectPartnerDuplication(PartnerMaster partnerMaster);
	
	public List<PartnerMaster> selectWatchersCheck(PartnerMaster partnerMaster);

	public List<PartnerMaster> getWatcherListByEmail(String email);

	public int checkWatcherAuth(PartnerMaster param);
	
	int existsWatcherByEmail(PartnerMaster project);

	void insertWatcher(PartnerMaster project);

	int existsWatcherByUser(PartnerMaster project);

	void removeWatcher(PartnerMaster project);

	public List<String> getInvateWatcherList(String prjId);
	
	void updatePublicYn(PartnerMaster partnerMaster);
	
	void updateComponentsOssId(PartnerMaster partnerMaster);

	List<PartnerMaster> copyWatcher(PartnerMaster project);
	
	int existsWatcher(PartnerMaster project);
	
	List<PartnerMaster> getProjectToAddList(OssComponents ossComponents);
	
	List<T2File> selectDocumentsFile(String documentsFileId);
	
	String selectDocumentsFileCnt(String documentsFileId);
	
	public int selectPartnerStatusTotalCountUser(PartnerMaster partnerMaster);

	public List<PartnerMaster> selectPartnerStatusUser(PartnerMaster partnerMaster);
	
	List<PartnerMaster> selectOssRefPartnerList(OssMaster ossMaster);
	
	int getOssAnalysisDataCnt(@Param("partnerId") String partnerId);
	
	PartnerMaster getOssAnalysisData(@Param("partnerId") String partnerId);
	
	String getReviewerEmail(@Param("partnerId") String partnerId, @Param("loginUser") String loginUser);

	List<OssComponents> findBinAutoIdentificationWithBinaryText(String partnerId);
	
	void insertBinaryOssComponents(ProjectIdentification projectIdentification);
	
	void updateOssList(ProjectIdentification projectIdentification);

	public int updateDivision(@Param("partnerId") String partnerId, @Param("division") String division);

	void updateDescription(PartnerMaster partnerMaster);
}