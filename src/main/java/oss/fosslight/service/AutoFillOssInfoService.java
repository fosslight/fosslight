/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.ProjectIdentification;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

public interface AutoFillOssInfoService {
	List<ProjectIdentification> checkOssLicenseData(List<ProjectIdentification> componentData, Map<String, String> validMap, Map<String, String> diffMap);
	List<ProjectIdentification> checkOssLicense(List<ProjectIdentification> list);
	Map<String, Object> saveOssCheckLicense(ProjectIdentification paramBean, String targetName);
	
	ParallelFlux<Object> getGithubLicenses(List<String> locations);
	Mono<Object> requestGithubLicense(String location);

	ParallelFlux<Object> getClearlyDefinedLicenses(List<String> locations);
	Mono<Object> requestClearlyDefinedLicense(String location);
}