/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.Vulnerability;

@Mapper
public interface OsvDataMapper {
	void insertOsvVulnerability(Map<String, Object> param);
	
	void updateOsvVulnerability(Map<String, Object> param);
	
	void insertOsvAlias(Map<String, Object> param);
	
	void insertOsvSeverity(Map<String, Object> param);
	
	void insertOsvAffectedPackage(Map<String, Object> param);
		
	void insertOsvAffectedPackageVersion(Map<String, Object> param);
	
	void insertOsvAffectedPackageRange(Map<String, Object> param);
	
	void insertOsvReferences(Map<String, Object> param);
	
	int deleteOsvAlias(int id);
	
	int deleteOsvSeverity(int id);
	
	int deleteOsvAffectedPackage(int id);
	
	int deleteOsvAffectedPackageVersion(int id);
	
	int deleteOsvAffectedPackageRange(int id);
	
	int deleteOsvReferences(String osvId);
	
	int getOsvAffectedPackageId(Map<String, Object> param);
	
	int getOsvAffectedPackageRangeIndex(int affectedPkgId);
	
	Vulnerability selectOsvVulnerabilityInfo(String osvId);

	List<Vulnerability> selectOsvSecurityListForProject(ProjectIdentification identification);
	
	List<Vulnerability> selectOsvVulnerabilityListByUniqueNick(Map<String, Object> paramMap);
	
	List<Vulnerability> selectOsvVulnerabilityListByPurl(Map<String, Object> paramMap);
	
	List<Vulnerability> selectOsvVulnerabilityListByPackageName(Map<String, Object> paramMap);

	void setGroupConcatMaxLen();
	
	void dropOsvRangeTemp();
	void createOsvRangeTemp();
	void createIndexOsvRangeTemp();
	
	void dropOsvVersionTemp();
	void createOsvVersionTemp();
	void createIndexOsvVersionTemp();
	
	void dropOsvSeverityTemp();
	void createOsvSeverityTemp();
	void createIndexOsvSeverityTemp();
	
	void dropOsvPreMergeTemp();
	void createOsvPreMergeTemp();
	void createIndexOsvPreMergeTemp();
	
	void dropOsvSearchMasterTemp();
	void createOsvSearchMasterTemp();
	void insertOsvSearchMasterData();
	void updateOsvSearchMasterSeverityDefault();
	
	void createIndexMasterId();
	void createIndexMasterWithdrawn();
	void createIndexMasterNameP1();
	void createIndexMasterNameP2();
	void createIndexMasterNameP3();
	void createIndexMasterVersionP1Yn();
	void createIndexMasterVersionP2Yn();
	void createIndexMasterVersionP3Yn();
	void createIndexMasterSevType();
	void createIndexMasterAffectedVersion();

	void dropOsvSearchMaster();
	void renameOsvSearchMasterTemp();
}
