/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Set;

import oss.fosslight.domain.LicenseMaster;

public interface CacheService {

	Set<String> getLicenseNames();
	Set<String> getLicenseUpperNames();

	LicenseMaster getLicenseInfo(String licenseName);
	LicenseMaster getLicenseInfoById(String licenseId);
}
