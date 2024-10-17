/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.History;
import oss.fosslight.domain.Statistics;
import oss.fosslight.repository.StatisticsMapper;
import oss.fosslight.service.StatisticsService;
import oss.fosslight.util.DateUtil;

@Service
public class StatisticsServiceImpl extends CoTopComponent implements StatisticsService{
	// Mapper
	@Autowired StatisticsMapper statisticsMapper;
	
	final String[] colorArray = new String[] {"#70ad47", "#ed7d31", "#a5a5a5", "#ffc000", "#5b9bd5", "#5bd597", "#d55bab", "#6f5bd5", "#544fc5"};
	
	@Override
	public History work(Object param) {
		return null;
	}
	
	@Override
	public Map<String, Object> getDivisionalProjectChartData(Statistics statistics) {
		Map<String, Object> result = new HashMap<>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		List<Statistics> titleList = new ArrayList<>();
		List<String> titleArray = new ArrayList<>();
		
		if (!"NET".equals(statistics.getCategoryType())) {
			titleList = statisticsMapper.getChartTitle(statistics);
		}
		
		if ("REV".equals(statistics.getCategoryType())) {
			titleArray.add("unassigned");
			statistics.setNoneUser(statisticsMapper.getNoneUser());
		}
		
		for (Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		if ("NET".equals(statistics.getCategoryType())) {
			titleArray.add("Network Service");
			titleArray.add("Others");
		} 
		
		titleArray = titleArray.stream().distinct().collect(Collectors.toList());
		if ("REV".equals(statistics.getCategoryType())) {
			statistics.setCategorySize(titleArray.size());
		}
		statistics.setTitleArray(titleArray); // Chart Title
		
		List<Statistics> list = statisticsMapper.getDivisionalProjectChartData(statistics);
		
		if ("REV".equals(statistics.getCategoryType())) {
			// Reviewer None Statistic Sum Check
			if (noneCheck(titleArray, list) > 0) {
				titleArray.add("NONE");
			}
		}
				
		if (CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			list.forEach(Statistics::setTotal);
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			addCategoryCnt(chartData, list);
			chartData.setTitleArray(titleArray); // Chart Title
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public Map<String, Object> getMostUsedChartData(Statistics statistics) {
		Map<String, Object> result = new HashMap<>();
		List<Statistics> list = statisticsMapper.getMostUsedChartData(statistics);
		
		/*
		if ("OSS".equals(statistics.getChartType())) {
			list = statisticsMapper.getMostUsedOssChartData(statistics);
		} else if ("LICENSE".equals(statistics.getChartType())) {			
			list = statisticsMapper.getMostUsedLicenseChartData(statistics);
		}
		*/
		result.put("chartData", list);
		
		return result;
	}
	
	public Map<String, Object> getUpdatedOssChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		
		List<Statistics> titleList = statisticsMapper.getChartTitle(statistics);
		
		List<String> titleArray = new ArrayList<>();
		
		for (Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		titleArray = titleArray.stream().distinct().collect(Collectors.toList());
		
		statistics.setTitleArray(titleArray); // Chart Title
		statistics.setDiffMonthCnt(DateUtil.getDiffMonth(statistics.getStartDate(), statistics.getEndDate()));
		statistics.setNoneUser(statisticsMapper.getNoneUser());
		statistics.setCategorySize(titleArray.size());
		
		statistics.setUpdateType("ADD");
		List<Statistics> list = statisticsMapper.getUpdatedOssChartData(statistics);
		
		statistics.setUpdateType("MOD");
		list.addAll(statisticsMapper.getUpdatedOssChartData(statistics));
		
		list = list.stream().sorted(Comparator.comparing((Statistics s) -> s.getColumnName())).collect(Collectors.toList());
		
		// Reviewer None Statistic Sum Check
		if (noneCheck(titleArray, list) > 0) {
			titleArray.add("NONE");
		}
		
		if (CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			list.forEach(Statistics::setTotal);
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			
			list.forEach(data -> {
				int categoryIdx = 0;
				chartData.addCategoryList(data.getColumnName());
				
				if (data.getCategory0Cnt() > -1) {chartData.addCategoryCnt(data.getCategory0Cnt(), categoryIdx++);}
				if (data.getCategory1Cnt() > -1) {chartData.addCategoryCnt(data.getCategory1Cnt(), categoryIdx++);}
				if (data.getCategory2Cnt() > -1) {chartData.addCategoryCnt(data.getCategory2Cnt(), categoryIdx++);}
				if (data.getCategory3Cnt() > -1) {chartData.addCategoryCnt(data.getCategory3Cnt(), categoryIdx++);}
				if (data.getCategory4Cnt() > -1) {chartData.addCategoryCnt(data.getCategory4Cnt(), categoryIdx++);}
				if (data.getCategory5Cnt() > -1) {chartData.addCategoryCnt(data.getCategory5Cnt(), categoryIdx++);}
				if (data.getCategory6Cnt() > -1) {chartData.addCategoryCnt(data.getCategory6Cnt(), categoryIdx++);}
				if (data.getCategory7Cnt() > -1) {chartData.addCategoryCnt(data.getCategory7Cnt(), categoryIdx++);}
				if (data.getCategory8Cnt() > -1) {chartData.addCategoryCnt(data.getCategory8Cnt(), categoryIdx++);}
				if (data.getCategory9Cnt() > -1) {chartData.addCategoryCnt(data.getCategory9Cnt(), categoryIdx++);}
				if (data.getCategory10Cnt() > -1) {chartData.addCategoryCnt(data.getCategory10Cnt(), categoryIdx++);}
				if (data.getCategory11Cnt() > -1) {chartData.addCategoryCnt(data.getCategory11Cnt(), categoryIdx++);}
				if (data.getCategory12Cnt() > -1) {chartData.addCategoryCnt(data.getCategory12Cnt(), categoryIdx++);}
				if (data.getCategory13Cnt() > -1) {chartData.addCategoryCnt(data.getCategory13Cnt(), categoryIdx++);}
				if (data.getCategory14Cnt() > -1) {chartData.addCategoryCnt(data.getCategory14Cnt(), categoryIdx++);}
				if (data.getCategory15Cnt() > -1) {chartData.addCategoryCnt(data.getCategory15Cnt(), categoryIdx++);}
				if (data.getCategory16Cnt() > -1) {chartData.addCategoryCnt(data.getCategory16Cnt(), categoryIdx++);}
				if (data.getCategory17Cnt() > -1) {chartData.addCategoryCnt(data.getCategory17Cnt(), categoryIdx++);}
				if (data.getCategory18Cnt() > -1) {chartData.addCategoryCnt(data.getCategory18Cnt(), categoryIdx++);}
				if (data.getCategory19Cnt() > -1) {chartData.addCategoryCnt(data.getCategory19Cnt(), categoryIdx++);}

			});
			
			chartData.setTitleArray(titleArray); // Chart Title
			
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public Map<String, Object> getUpdatedLicenseChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		List<Statistics> titleList = statisticsMapper.getChartTitle(statistics);
		List<String> titleArray = new ArrayList<>();
		
		for (Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		titleArray = titleArray.stream().distinct().collect(Collectors.toList());
		
		statistics.setTitleArray(titleArray); // Chart Title
		statistics.setDiffMonthCnt(DateUtil.getDiffMonth(statistics.getStartDate(), statistics.getEndDate()));
		statistics.setNoneUser(statisticsMapper.getNoneUser());
		statistics.setCategorySize(titleArray.size());
		
		statistics.setUpdateType("ADD");
		List<Statistics> list = statisticsMapper.getUpdatedLicenseChartData(statistics);
		
		statistics.setUpdateType("MOD");
		list.addAll(statisticsMapper.getUpdatedLicenseChartData(statistics));
		
		list = list.stream().sorted(Comparator.comparing((Statistics s) -> s.getColumnName())).collect(Collectors.toList());
		
		// Reviewer None Statistic Sum Check
		if (noneCheck(titleArray, list) > 0) {
			titleArray.add("NONE");
		}
		
		if (CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			list.forEach(Statistics::setTotal);
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			addCategoryCnt(chartData, list);
			chartData.setTitleArray(titleArray); // Chart Title
			
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public Map<String, Object> getTrdPartyRelatedChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		List<Statistics> titleList = statisticsMapper.getChartTitle(statistics);
		List<String> titleArray = new ArrayList<>();
		
		if ("REV".equals(statistics.getCategoryType())) {
			titleArray.add("unassigned");
			statistics.setNoneUser(statisticsMapper.getNoneUser());
		}
		
		for (Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		titleArray = titleArray.stream().distinct().collect(Collectors.toList());
				
		statistics.setTitleArray(titleArray); // Chart Title
		statistics.setCategorySize(titleArray.size());
		
		List<Statistics> list = statisticsMapper.getTrdPartyRelatedChartData(statistics);
		
		if ("REV".equals(statistics.getCategoryType())) {
			// Reviewer None Statistic Sum Check
			if (noneCheck(titleArray, list) > 0) {
				titleArray.add("NONE");
			}
		}
		
		if (CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			list.forEach(Statistics::setTotal);
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			addCategoryCnt(chartData, list);
			chartData.setTitleArray(titleArray); // Chart Title
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	private void addCategoryCnt(Statistics chartData, List<Statistics> list) {
		list.forEach(data -> {
			int categoryIdx = 0;
			if (data.getCategory0Cnt() > -1) {chartData.addCategoryCnt(data.getCategory0Cnt(), categoryIdx++);}
			if (data.getCategory1Cnt() > -1) {chartData.addCategoryCnt(data.getCategory1Cnt(), categoryIdx++);}
			if (data.getCategory2Cnt() > -1) {chartData.addCategoryCnt(data.getCategory2Cnt(), categoryIdx++);}
			if (data.getCategory3Cnt() > -1) {chartData.addCategoryCnt(data.getCategory3Cnt(), categoryIdx++);}
			if (data.getCategory4Cnt() > -1) {chartData.addCategoryCnt(data.getCategory4Cnt(), categoryIdx++);}
			if (data.getCategory5Cnt() > -1) {chartData.addCategoryCnt(data.getCategory5Cnt(), categoryIdx++);}
			if (data.getCategory6Cnt() > -1) {chartData.addCategoryCnt(data.getCategory6Cnt(), categoryIdx++);}
			if (data.getCategory7Cnt() > -1) {chartData.addCategoryCnt(data.getCategory7Cnt(), categoryIdx++);}
			if (data.getCategory8Cnt() > -1) {chartData.addCategoryCnt(data.getCategory8Cnt(), categoryIdx++);}
			if (data.getCategory9Cnt() > -1) {chartData.addCategoryCnt(data.getCategory9Cnt(), categoryIdx++);}
			if (data.getCategory10Cnt() > -1) {chartData.addCategoryCnt(data.getCategory10Cnt(), categoryIdx++);}
			if (data.getCategory11Cnt() > -1) {chartData.addCategoryCnt(data.getCategory11Cnt(), categoryIdx++);}
			if (data.getCategory12Cnt() > -1) {chartData.addCategoryCnt(data.getCategory12Cnt(), categoryIdx++);}
			if (data.getCategory13Cnt() > -1) {chartData.addCategoryCnt(data.getCategory13Cnt(), categoryIdx++);}
			if (data.getCategory14Cnt() > -1) {chartData.addCategoryCnt(data.getCategory14Cnt(), categoryIdx++);}
			if (data.getCategory15Cnt() > -1) {chartData.addCategoryCnt(data.getCategory15Cnt(), categoryIdx++);}
			if (data.getCategory16Cnt() > -1) {chartData.addCategoryCnt(data.getCategory16Cnt(), categoryIdx++);}
			if (data.getCategory17Cnt() > -1) {chartData.addCategoryCnt(data.getCategory17Cnt(), categoryIdx++);}
			if (data.getCategory18Cnt() > -1) {chartData.addCategoryCnt(data.getCategory18Cnt(), categoryIdx++);}
			if (data.getCategory19Cnt() > -1) {chartData.addCategoryCnt(data.getCategory19Cnt(), categoryIdx++);}
		});
		
	}

	public Map<String, Object> getUserRelatedChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		
		List<Statistics> list = statisticsMapper.getUserRelatedChartData(statistics);
		List<String> titleList = new ArrayList<>();
		titleList.add("Total");
		titleList.add("Activator");
		
		if (CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			list.forEach(Statistics::setTotal);
			
			titleList.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleList);
		} else {
			for (Statistics data : list) {
				int categoryIdx = 0;
				chartData.addCategoryCnt(data.getCategory0Cnt()-data.getCategory1Cnt(), categoryIdx++); // total Cnt = 부서원 - activator
				chartData.addCategoryCnt(data.getCategory1Cnt(), categoryIdx++); // activator Cnt
			}
			
			chartData.setTitleArray(titleList); // Chart Title
			
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public int noneCheck(List<String> titleArray, List<Statistics> list) {
		int noneSum = 0;
		
		switch(titleArray.size()) {
		case 1:
			for (Statistics data : list) {
				noneSum += data.getCategory1Cnt();
			}
			break;
		case 2:
			for (Statistics data : list) {
				noneSum += data.getCategory2Cnt();
			}
			break;
		case 3:
			for (Statistics data : list) {
				noneSum += data.getCategory3Cnt();
			}
			break;
		case 4:
			for (Statistics data : list) {
				noneSum += data.getCategory4Cnt();
			}
			break;
		case 5:
			for (Statistics data : list) {
				noneSum += data.getCategory5Cnt();
			}
			break;
		case 6:
			for (Statistics data : list) {
				noneSum += data.getCategory6Cnt();
			}
			break;
		case 7:
			for (Statistics data : list) {
				noneSum += data.getCategory7Cnt();
			}
			break;
		case 8:
			for (Statistics data : list) {
				noneSum += data.getCategory8Cnt();
			}
			break;
		case 9:
			for (Statistics data : list) {
				noneSum += data.getCategory9Cnt();
			}
			break;
		case 10:
			for (Statistics data : list) {
				noneSum += data.getCategory10Cnt();
			}
			break;
		case 11:
			for (Statistics data : list) {
				noneSum += data.getCategory11Cnt();
			}
			break;
		case 12:
			for (Statistics data : list) {
				noneSum += data.getCategory12Cnt();
			}
			break;
		case 13:
			for (Statistics data : list) {
				noneSum += data.getCategory13Cnt();
			}
			break;
		case 14:
			for (Statistics data : list) {
				noneSum += data.getCategory14Cnt();
			}
			break;
		case 15:
			for (Statistics data : list) {
				noneSum += data.getCategory15Cnt();
			}
			break;
		case 16:
			for (Statistics data : list) {
				noneSum += data.getCategory16Cnt();
			}
			break;
		case 17:
			for (Statistics data : list) {
				noneSum += data.getCategory17Cnt();
			}
			break;
		case 18:
			for (Statistics data : list) {
				noneSum += data.getCategory18Cnt();
			}
			break;
		case 19:
			for (Statistics data : list) {
				noneSum += data.getCategory19Cnt();
			}
			break;
		}
		return noneSum;
	}
}
