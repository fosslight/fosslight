/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

public interface RestrictionService {

	Map<String, Object> getOssLicenseInfobyRestriction(String cdDtlNo);
	
}
