/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface ApiSelfCheckService {
	int getCreateProjectCnt(String userId);
	
	Map<String, Object> createSelfCheck(Map<String, Object> paramMap);
	
	boolean existProjectCnt(String userId, String prjId);
}
