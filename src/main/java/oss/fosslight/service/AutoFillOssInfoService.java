/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.ProjectIdentification;

public interface AutoFillOssInfoService {
	List<ProjectIdentification> checkOssLicenseData(List<ProjectIdentification> componentData, Map<String, String> validMap, Map<String, String> diffMap);
}