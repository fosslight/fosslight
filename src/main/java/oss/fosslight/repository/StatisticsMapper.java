/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.Statistics;

@Mapper
public interface StatisticsMapper {
	List<Statistics> getChartTitle(Statistics statistics);
	
	List<String> getNoneUser();
	
	List<Statistics> getDivisionalProjectChartData(Statistics statistics);
	
	List<Statistics> getMostUsedOssChartData(Statistics statistics);
	
	List<Statistics> getMostUsedLicenseChartData(Statistics statistics);
	
	List<Statistics> getUpdatedOssChartData(Statistics statistics);
	
	List<Statistics> getUpdatedLicenseChartData(Statistics statistics);
	
	List<Statistics> getTrdPartyRelatedChartData(Statistics statistics);

	List<Statistics> getUserRelatedChartData(Statistics statistics);

	List<Statistics> getMostUsedChartData(Statistics statistics);

	List<Statistics> getUserDivisionChartData(Statistics statistics);

	List<Map<String, Object>> getDelayedProjectList(Statistics statistics);
}