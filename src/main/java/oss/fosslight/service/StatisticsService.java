/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.Statistics;

public interface StatisticsService extends HistoryConfig{
	public Map<String, Object> getDivisionalProjectChartData(Statistics statistics);
	
	public Map<String, Object> getMostUsedChartData(Statistics statistics);
	
	public Map<String, Object> getUpdatedOssChartData(Statistics statistics);
	
	public Map<String, Object> getUpdatedLicenseChartData(Statistics statistics);
	
	public Map<String, Object> getTrdPartyRelatedChartData(Statistics statistics);
	
	public Map<String, Object> getUserRelatedChartData(Statistics statistics);
}
