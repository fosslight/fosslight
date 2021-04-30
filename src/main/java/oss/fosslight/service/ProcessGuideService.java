/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import oss.fosslight.domain.ProcessGuide;

public interface ProcessGuideService {
	Map<String, Object> getProcessGuideList(ProcessGuide vo) throws Exception;
	
	public void setProcessGuide(ProcessGuide vo) throws Exception;
	
	Map<String, Object> getProcessGuide(ProcessGuide vo) throws Exception;
}
