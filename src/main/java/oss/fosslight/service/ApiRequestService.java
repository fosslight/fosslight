/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import oss.fosslight.domain.ProjectIdentification;

public interface ApiRequestService {
	public void requestRedirectUrl(List<ProjectIdentification> result, List<ProjectIdentification> redirectTargets, List<ProjectIdentification> osoriTargets, Map<String, String> ossInfoNames, Map<String, Set<String>> urlToNameMap);
	
	public void requestOsoriUrl(List<ProjectIdentification> result, List<ProjectIdentification> osoriTargets);
}
