/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;

@Mapper
public interface LicenseMapper {
	int selectLicenseMasterTotalCount(LicenseMaster licenseMaster);	
	
	List<LicenseMaster> selectLicenseList(LicenseMaster licenseMaster); 
	
	LicenseMaster selectLicenseOne(LicenseMaster licenseMaster); 
	
	List<LicenseMaster> selectLicenseNicknameList(LicenseMaster licenseMaster); 			//라이센스 닉네임 목록
	
	List<LicenseMaster> getLicenseInfoInit();
	
	List<LicenseMaster> getLicenseInfoInitNick();
	
	List<LicenseMaster> getRoleOutLicense();
	
	List<OssMaster> checkExistsUsedOss(String licenseId);
	
	LicenseMaster checkExistsLicense(LicenseMaster param);
	
	int deleteLicenseMaster(LicenseMaster licenseMaster);									//특정 라이센스 삭제
	
	int deleteLicenseNickname(LicenseMaster licenseMaster);									//특정 라이센스 닉네임 전체 삭제
	
	String existNetworkServerLicense(String licenseId);
	
	int insertNetworkServerLicense(String licenseId);
	
	int deleteNetworkServerLicense(String licenseId);
	
	List<String> getOssListWithLicenseForTypeCheck(String licenseId);
	
	void insertLicenseMaster(LicenseMaster licenseMaster);									//라이센스 등록
	
	int insertLicenseNickname(LicenseMaster licenseMaster);									//라이센스 닉네임 등록
	
	int updateLicenseMaster(LicenseMaster param);
	
	List<LicenseMaster> selectLicenseNameList(); 											//라이센스 닉네임 목록
	
	List<Project> getLicenseChangeForUserList(String licenseId);
	
	void insertLicenseWebPages(LicenseMaster licenseMaster);
	
	int existsLicenseWebPages(LicenseMaster licenseMaster);
		
	void deleteLicenseWebPages(LicenseMaster licenseMaster);
		
	List<LicenseMaster> selectLicenseWebPageList(LicenseMaster licenseMaster);
	
	// for cache query
	Set<String> getLicenseNames();
	Set<String> getLicenseShortNames();
	Set<String> getLicenseNickNames();
	LicenseMaster getLicenseInfoWithName(String licenseName);
	LicenseMaster getLicenseInfoWithId(String licenseId);

}
