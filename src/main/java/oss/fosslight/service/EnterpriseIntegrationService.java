/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import org.springframework.http.ResponseEntity;

import oss.fosslight.domain.T2Users;

public interface EnterpriseIntegrationService {
	public void processAnalysisDataInterface(Object object, T2Users userInfo);
	
	public void executeAnalysisUpdate(String jobSeq, String ossId, String prjId, boolean isSendJobData);

	public void syncLicenseToEnterpriseInterface();
	
	public void syncOssToEnterpriseInterface();

	public ResponseEntity<?> executeEnterpriseAnalysis(String targetUrl, String token, String prjId, boolean isResponseRequired);

	public String getEnterpriseAnalysisInfo(String prjId);
}