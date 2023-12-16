/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import org.apache.ibatis.annotations.Mapper;
import oss.fosslight.api.dto.GetLicenseDetailsDto;
import oss.fosslight.api.dto.LicenseDetailsDto;
import oss.fosslight.api.dto.LicenseDto;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;

import java.util.List;
import java.util.Set;

@Mapper
public interface ApiLicenseMapper {
	int selectLicenseMasterTotalCount(ListLicenseDto.Request request);
	List<LicenseDto> selectLicenseList(ListLicenseDto.Request request);
	LicenseDetailsDto selectLicenseById(String id);
	List<String> selectLicenseNicknameList(String name);
	List<String> getLicenseAutocompleteCandidates(String name);
}
