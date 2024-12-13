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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.lang.Collections;
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
	
	final String[] colorArray = new String[] {"#70ad47", "#ed7d31", "#a5a5a5", "#ffc000", "#5b9bd5", "#5bd597", "#d55bab", "#d5605c", "#544fc5", "#fcf22f", "#2EFEF7", "#0000FF", "#FF4000", "#FFFF00"};
	
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
		
		List<String> noneUsers = statisticsMapper.getNoneUser();
		if (!Collections.isEmpty(noneUsers)) {
			titleArray.addAll(noneUsers);
		}
		
		titleArray = titleArray.stream().distinct().collect(Collectors.toList());
		
		statistics.setTitleArray(titleArray); // Chart Title
		statistics.setDiffMonthCnt(DateUtil.getDiffMonth(statistics.getStartDate(), statistics.getEndDate()));
//		statistics.setNoneUser(statisticsMapper.getNoneUser());
		statistics.setCategorySize(titleArray.size());
		
		statistics.setUpdateType("ADD");
		List<Statistics> list = statisticsMapper.getUpdatedOssChartData(statistics);
		
		statistics.setUpdateType("MOD");
		list.addAll(statisticsMapper.getUpdatedOssChartData(statistics));
		
		list = list.stream().sorted(Comparator.comparing((Statistics s) -> s.getColumnName())).collect(Collectors.toList());
		
		// Reviewer None Statistic Sum Check
//		if (noneCheck(titleArray, list) > 0) {
//			titleArray.add("NONE");
//		}
		
		if (CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			list.forEach(Statistics::setTotal);
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {

			addCategoryCnt2(chartData, list);
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
		
		List<String> noneUsers = statisticsMapper.getNoneUser();
		if (!Collections.isEmpty(noneUsers)) {
			titleArray.addAll(noneUsers);
		}
		
		titleArray = titleArray.stream().distinct().collect(Collectors.toList());
		
		statistics.setTitleArray(titleArray); // Chart Title
		statistics.setDiffMonthCnt(DateUtil.getDiffMonth(statistics.getStartDate(), statistics.getEndDate()));
//		statistics.setNoneUser(statisticsMapper.getNoneUser());
		statistics.setCategorySize(titleArray.size());
		
		statistics.setUpdateType("ADD");
		List<Statistics> list = statisticsMapper.getUpdatedLicenseChartData(statistics);
		
		statistics.setUpdateType("MOD");
		list.addAll(statisticsMapper.getUpdatedLicenseChartData(statistics));
		
		list = list.stream().sorted(Comparator.comparing((Statistics s) -> s.getColumnName())).collect(Collectors.toList());
		
		// Reviewer None Statistic Sum Check
//		if (noneCheck(titleArray, list) > 0) {
//			titleArray.add("NONE");
//		}
		
		if (CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			list.forEach(Statistics::setTotal);
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			addCategoryCnt2(chartData, list);
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
	        for (int i = 0; i <= 19; i++) {
	            Function<Statistics, Integer> getCategoryCnt = getCategoryCntFunction(i);
	            int cnt = getCategoryCnt.apply(data);
	            if (cnt > -1) {
	                chartData.addCategoryCnt(cnt, categoryIdx++);
	            }
	        }
		});
	}
	
	private void addCategoryCnt2(Statistics chartData, List<Statistics> list) {
	    list.forEach(data -> {
	        int categoryIdx = 0;
	        chartData.addCategoryList(data.getColumnName());
	        for (int i = 0; i <= 19; i++) {
	            Function<Statistics, Integer> getCategoryCnt = getCategoryCntFunction(i);
	            int cnt = getCategoryCnt.apply(data);
	            if (cnt > -1) {
	                chartData.addCategoryCnt(cnt, categoryIdx++);
	            }
	        }
	    });
	}
	private Function<Statistics, Integer> getCategoryCntFunction(int i) {
	    switch (i) {
	        case 0: return Statistics::getCategory0Cnt;
	        case 1: return Statistics::getCategory1Cnt;
	        case 2: return Statistics::getCategory2Cnt;
	        case 3: return Statistics::getCategory3Cnt;
	        case 4: return Statistics::getCategory4Cnt;
	        case 5: return Statistics::getCategory5Cnt;
	        case 6: return Statistics::getCategory6Cnt;
	        case 7: return Statistics::getCategory7Cnt;
	        case 8: return Statistics::getCategory8Cnt;
	        case 9: return Statistics::getCategory9Cnt;
	        case 10: return Statistics::getCategory10Cnt;
	        case 11: return Statistics::getCategory11Cnt;
	        case 12: return Statistics::getCategory12Cnt;
	        case 13: return Statistics::getCategory13Cnt;
	        case 14: return Statistics::getCategory14Cnt;
	        case 15: return Statistics::getCategory15Cnt;
	        case 16: return Statistics::getCategory16Cnt;
	        case 17: return Statistics::getCategory17Cnt;
	        case 18: return Statistics::getCategory18Cnt;
	        case 19: return Statistics::getCategory19Cnt;
	        default: throw new IllegalArgumentException("Invalid category index: " + i);
	    }
	}

	public Map<String, Object> getUserRelatedChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		
		List<Statistics> list = statisticsMapper.getUserRelatedChartData(statistics);
		List<String> titleList = new ArrayList<>();
		titleList.add("Idle user");
		titleList.add("Active user");
		
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
	    int titleArraySize = titleArray.size();
	    for (Statistics data : list) {
            Function<Statistics, Integer> getCategoryCnt = getCategoryCntFunction(titleArraySize);
            int cnt = getCategoryCnt.apply(data);
	        noneSum += cnt;
	    }
	    return noneSum;
	}
}
