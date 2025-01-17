/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.HashMap;

import oss.fosslight.domain.Configuration;

public interface ConfigurationService {
	public void updateDefaultSetting(Configuration configuration);
	public String updateDefaultLocaleSetting(Configuration configuration);
//	public void updateDefaultLocale(Configuration configuration);
}
