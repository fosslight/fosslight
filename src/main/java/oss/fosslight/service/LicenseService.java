/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;

public interface LicenseService extends HistoryConfig{
	int selectLicenseMasterTotalCount(LicenseMaster licenseMaster);
	
	Map<String,Object> getLicenseMasterList(LicenseMaster licenseMaster);
	
	LicenseMaster getLicenseMasterOne(LicenseMaster licenseMaster);						// 라이센스 단일
	
	List<OssMaster> checkExistsUsedOss(String licenseId);
	
	LicenseMaster checkExistsLicense(LicenseMaster param);
	
	List<LicenseMaster> getLicenseMasterListExcel(LicenseMaster license);				// 라이센스 리스트 엑셀용
	
	String registLicenseMaster(LicenseMaster licenseMaster);							// 라이센스 등록
	
	int deleteLicenseMaster(LicenseMaster licenseMaster);								// 라이센스 삭제
	
	void deleteDistributeLicense(LicenseMaster bean, boolean distributionFlag);
	
	void registNetworkServerLicense(String licenseId, String type);
	
	List<OssMaster> updateOssLicenseType(String licenseId);
	
	boolean distributeLicense(String licenseId, boolean distributionFlag);
	
	List<LicenseMaster> getLicenseNameList();											// 라이센스 자동완성
	
	void sendLicenseTypeChangedMail(String licenseId, LicenseMaster beforeBean, LicenseMaster afterBean, String comment);
}
