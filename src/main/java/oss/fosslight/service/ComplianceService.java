/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import oss.fosslight.domain.Project;

public interface ComplianceService {
	public  Map<String, Object> getModelList(Project project);
}
