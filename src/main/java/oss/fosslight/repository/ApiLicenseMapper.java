/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import org.apache.ibatis.annotations.Mapper;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;

import java.util.List;
import java.util.Set;

@Mapper
public interface ApiLicenseMapper {
	int selectLicenseMasterTotalCount(LicenseMaster licenseMaster);

	List<LicenseMaster> selectLicenseList(LicenseMaster licenseMaster);
}
