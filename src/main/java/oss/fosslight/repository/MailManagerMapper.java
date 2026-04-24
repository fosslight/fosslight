/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.BinaryMaster;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;

@Mapper
public interface MailManagerMapper {
	// 이메일 전송 이력 등록
	public void insertEmailHistory(CoMail mailVo);
	
	// 이메일 전송 상태 변경
	public void updateSendStatus(CoMail mailVo);
	
	// 이메일 에러 문구 작성
	public void updateErrorMsg(CoMail mailVo);
	
	// Send Mail Run Timeout
	public void updateSendMailRunTimeout(CoMail mailVo);
	
	// 사용자 ID로 이메일 주소 조회
	public List<String> selectMailAddrFromIds(Map<String, String[]> toIds);
	
	public List<String> selectAdminMailAddr();
	
	public Project getProjectInfo(String prjId);
	
	public List<String> setProjectWatcherMailList(String prjId);
	
	public PartnerMaster getPartnerInfo(String partnerId);
	
	public List<String> setPartnerWatcherMailList(String partnerId);
	
	public OssMaster getOssInfoById(String ossId);
	
	public LicenseMaster getLicenseInfoById(String licenseId);
	
	public Project getProjectInfoById(String prjId);
	
	public Project getSelfCheckProjectInfoById(String prjId);
	
	public BinaryMaster getBinaryInfo(String batId);
	
	public String getDistributeReservedUser(String prjId);
	
	public List<String> binaryWatcherMailList(String batId);
	
	public List<Map<String, Object>> getTempMail();
	
	public void deleteTempMail(String mailSeq);

	public List<String> setProjectWatcherMailListNotCheckDivision(String paramPrjId);
	
	public List<String> setSelfCheckWatcherMailListNotCheckDivision(String paramPrjId);

	public List<Map<String, Object>> getComponentOssBasicInfo(@Param("ossId") String ossId);

	public List<Map<String, Object>> getComponentLicenseBasicInfo(@Param("licenseId") String licenseId);

	public List<Map<String, Object>> getComponentProjectBasicInfo(@Param("prjId") String prjId);

	public List<Map<String, Object>> getComponentSelfCheckBasicInfo(@Param("prjId") String prjId);

	public List<Map<String, Object>> getComponentProjectDistributionInfo(@Param("prjId") String prjId);

	public List<Map<String, Object>> getComponentProjectModelInfo(@Param("prjId") String prjId);

	public List<Map<String, Object>> getComponentPartnerBasicInfo(@Param("partnerId") String partnerId);

	public List<Map<String, Object>> getComponentPartnerOssList(@Param("partnerId") String partnerId);

	public List<Map<String, Object>> getComponentDiscloseOssInfo(@Param("partnerId") String partnerId);

	public List<Map<String, Object>> getComponentNotDiscloseOssInfo(@Param("partnerId") String partnerId);

	public List<Map<String, Object>> getComponentBatResult(@Param("batId") String batId);

	public List<Map<String, Object>> getComponentVulnerabilityProject(@Param("prjId") String prjId);

	public List<Map<String, Object>> getComponentVulnerabilityOssInfo(@Param("ossKey") List<String> ossKey);

	public List<Map<String, Object>> getComponentVulnerabilityRecalculatedAll(@Param("ossKey") List<String> ossKey);

	public List<Map<String, Object>> getComponentVulnerabilityRecalculated(@Param("prjId") String prjId, @Param("standardScore") String standardScore);

	public List<Map<String, Object>> getComponentVulnerabilityRemoveRecalculated(@Param("prjId") String prjId, @Param("standardScore") String standardScore);
}