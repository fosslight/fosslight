/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

public interface SystemConfigurationService {
	Map<String, Object> saveConfiguration(Map<String, Object> configurationMap) throws Exception;

	Map<String, Object> ldapTestConnection(Map<String, Object> configurationMap) throws Exception;
}
