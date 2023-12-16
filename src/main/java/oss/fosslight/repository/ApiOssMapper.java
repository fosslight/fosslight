/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import oss.fosslight.api.dto.*;
import oss.fosslight.domain.OssMaster;

@Mapper
public interface ApiOssMapper {
	List<Map<String, Object>> getOssInfo(Map<String, Object> paramMap);
	
	String getOssName(String ossName);
	
	List<Map<String, Object>> getOssInfoByDownloadLocation(String downloadLocation);
	
	List<Map<String, Object>> getLicenseInfo(String licenseName);
	
	List<String> selectOssNicknameList(String ossName);

	List<HashMap<String, Object>> getOssInfoAll();

	List<HashMap<String, Object>> getOssInfoAllWithNick();

	List<HashMap<String, Object>> getOssAllNickNameList();

    List<OssDto> selectOssList(ListOssDto.Request query);

	List<OssDto> selectOssSubList(OssMaster query);

	List<LicenseDto> selectOssLicenseList(List<String> ossIdList);
	OssDetailsDto selectOssById(String id);
	List<VulnerabilityDto> getOssVulnerabilityList(String ossId);
	List<OssDto> getOssAutocompleteCandidates();
}
