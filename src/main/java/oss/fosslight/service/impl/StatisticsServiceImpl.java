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
	
	final String[] colorArray = new String[] {"#70ad47", "#ed7d31", "#a5a5a5", "#ffc000", "#5b9bd5", "#5bd597", "#d55bab", "#6f5bd5"};
	
	@Override
	public History work(Object param) {
		return null;
	}
	
	@Override
	public Map<String, Object> getDivisionalProjectChartData(Statistics statistics) {
		Map<String, Object> result = new HashMap<String, Object>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		List<Statistics> titleList = new ArrayList<Statistics>();
		List<String> titleArray = new ArrayList<String>();
		
		if(!"NET".equals(statistics.getCategoryType())) {
			titleList = statisticsMapper.getChartTitle(statistics);
		}
		
		if("REV".equals(statistics.getCategoryType())) {
			titleArray.add("unassigned");
			statistics.setNoneUser(statisticsMapper.getNoneUser());
		}
		
		for(Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		if("NET".equals(statistics.getCategoryType())) {
			titleArray.add("Network Service");
			titleArray.add("Others");
		} 
		
		statistics.setTitleArray(titleArray); // Chart Title
		
		List<Statistics> list = statisticsMapper.getDivisionalProjectChartData(statistics);
		
		if("REV".equals(statistics.getCategoryType())) {
			// Reviewer None Statistic Sum Check
			if (noneCheck(titleArray, list) > 0) {
				titleArray.add("NONE");
			}
		}
				
		if(CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			for(Statistics stat : list) {
				stat.setTotal();
			}
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			for(Statistics data : list) {
				int categoryIdx = 0;
				
				if(data.getCategory0Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory0Cnt(), categoryIdx++);
				}
				
				if(data.getCategory1Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory1Cnt(), categoryIdx++);
				}
				
				if(data.getCategory2Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory2Cnt(), categoryIdx++);
				}
				
				if(data.getCategory3Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory3Cnt(), categoryIdx++);
				}
				
				if(data.getCategory4Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory4Cnt(), categoryIdx++);
				}
				
				if(data.getCategory5Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory5Cnt(), categoryIdx++);
				}
				
				if(data.getCategory6Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory6Cnt(), categoryIdx++);
				}
				
				if(data.getCategory7Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory7Cnt(), categoryIdx++);
				}
				
				if(data.getCategory8Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory8Cnt(), categoryIdx++);
				}
			}
			
			chartData.setTitleArray(titleArray); // Chart Title
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public Map<String, Object> getMostUsedChartData(Statistics statistics) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Statistics> list = null;
		
		if("OSS".equals(statistics.getChartType())) {
			list = statisticsMapper.getMostUsedOssChartData(statistics);
		} else if("LICENSE".equals(statistics.getChartType())) {			
			list = statisticsMapper.getMostUsedLicenseChartData(statistics);
		}
		
		result.put("chartData", list);
		
		return result;
	}
	
	public Map<String, Object> getUpdatedOssChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<String, Object>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		
		List<Statistics> titleList = statisticsMapper.getChartTitle(statistics);
		
		List<String> titleArray = new ArrayList<String>();
		
		for(Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		statistics.setTitleArray(titleArray); // Chart Title
		statistics.setDiffMonthCnt(DateUtil.getDiffMonth(statistics.getStartDate(), statistics.getEndDate()));
		statistics.setNoneUser(statisticsMapper.getNoneUser());
		
		statistics.setUpdateType("ADD");
		List<Statistics> list = statisticsMapper.getUpdatedOssChartData(statistics);
		
		statistics.setUpdateType("MOD");
		list.addAll(statisticsMapper.getUpdatedOssChartData(statistics));
		
		list = list.stream().sorted(Comparator.comparing((Statistics s) -> s.getColumnName())).collect(Collectors.toList());
		
		// Reviewer None Statistic Sum Check
		if (noneCheck(titleArray, list) > 0) {
			titleArray.add("NONE");
		}
		
		if(CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			for(Statistics stat : list) {
				stat.setTotal();
			}
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			for(Statistics data : list) {
				int categoryIdx = 0;
				chartData.addCategoryList(data.getColumnName());
				
				if(data.getCategory0Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory0Cnt(), categoryIdx++);
				}
				
				if(data.getCategory1Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory1Cnt(), categoryIdx++);
				}
				
				if(data.getCategory2Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory2Cnt(), categoryIdx++);
				}
				
				if(data.getCategory3Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory3Cnt(), categoryIdx++);
				}
				
				if(data.getCategory4Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory4Cnt(), categoryIdx++);
				}
				
				if(data.getCategory5Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory5Cnt(), categoryIdx++);
				}
				
				if(data.getCategory6Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory6Cnt(), categoryIdx++);
				}
				
				if(data.getCategory7Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory7Cnt(), categoryIdx++);
				}
				
				if(data.getCategory8Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory8Cnt(), categoryIdx++);
				}
			}
			
			chartData.setTitleArray(titleArray); // Chart Title
			
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public Map<String, Object> getUpdatedLicenseChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<String, Object>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		List<Statistics> titleList = statisticsMapper.getChartTitle(statistics);
		List<String> titleArray = new ArrayList<String>();
		
		for(Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		statistics.setTitleArray(titleArray); // Chart Title
		statistics.setDiffMonthCnt(DateUtil.getDiffMonth(statistics.getStartDate(), statistics.getEndDate()));
		statistics.setNoneUser(statisticsMapper.getNoneUser());
		
		statistics.setUpdateType("ADD");
		List<Statistics> list = statisticsMapper.getUpdatedLicenseChartData(statistics);
		
		statistics.setUpdateType("MOD");
		list.addAll(statisticsMapper.getUpdatedLicenseChartData(statistics));
		
		list = list.stream().sorted(Comparator.comparing((Statistics s) -> s.getColumnName())).collect(Collectors.toList());
		
		// Reviewer None Statistic Sum Check
		if (noneCheck(titleArray, list) > 0) {
			titleArray.add("NONE");
		}
		
		if(CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			for(Statistics stat : list) {
				stat.setTotal();
			}
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			for(Statistics data : list) {
				int categoryIdx = 0;
				chartData.addCategoryList(data.getColumnName());
				
				if(data.getCategory0Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory0Cnt(), categoryIdx++);
				}
				
				if(data.getCategory1Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory1Cnt(), categoryIdx++);
				}
				
				if(data.getCategory2Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory2Cnt(), categoryIdx++);
				}
				
				if(data.getCategory3Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory3Cnt(), categoryIdx++);
				}
				
				if(data.getCategory4Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory4Cnt(), categoryIdx++);
				}
				
				if(data.getCategory5Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory5Cnt(), categoryIdx++);
				}
				
				if(data.getCategory6Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory6Cnt(), categoryIdx++);
				}
				
				if(data.getCategory7Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory7Cnt(), categoryIdx++);
				}
				
				if(data.getCategory8Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory8Cnt(), categoryIdx++);
				}
			}

			chartData.setTitleArray(titleArray); // Chart Title
			
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public Map<String, Object> getTrdPartyRelatedChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<String, Object>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		List<Statistics> titleList = statisticsMapper.getChartTitle(statistics);
		List<String> titleArray = new ArrayList<String>();
		
		if("REV".equals(statistics.getCategoryType())) {
			titleArray.add("unassigned");
			statistics.setNoneUser(statisticsMapper.getNoneUser());
		}
		
		for(Statistics title : titleList) {
			titleArray.add(title.getTitleNm());
		}
		
		statistics.setTitleArray(titleArray); // Chart Title
		
		List<Statistics> list = statisticsMapper.getTrdPartyRelatedChartData(statistics);
		
		if("REV".equals(statistics.getCategoryType())) {
			// Reviewer None Statistic Sum Check
			if (noneCheck(titleArray, list) > 0) {
				titleArray.add("NONE");
			}
		}
		
		if(CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			for(Statistics stat : list) {
				stat.setTotal();
			}
			
			titleArray.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleArray);
		} else {
			for(Statistics data : list) {
				int categoryIdx = 0;
				
				if(data.getCategory0Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory0Cnt(), categoryIdx++);
				}
				
				if(data.getCategory1Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory1Cnt(), categoryIdx++);
				}
				
				if(data.getCategory2Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory2Cnt(), categoryIdx++);
				}
				
				if(data.getCategory3Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory3Cnt(), categoryIdx++);
				}
				
				if(data.getCategory4Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory4Cnt(), categoryIdx++);
				}
				
				if(data.getCategory5Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory5Cnt(), categoryIdx++);
				}
				
				if(data.getCategory6Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory6Cnt(), categoryIdx++);
				}
				
				if(data.getCategory7Cnt() > -1) {
					chartData.addCategoryCnt(data.getCategory7Cnt(), categoryIdx++);
				}
			}

			chartData.setTitleArray(titleArray); // Chart Title
			
			result.put("chartData", chartData);
		}
		
		return result;
	}
	
	public Map<String, Object> getUserRelatedChartData(Statistics statistics){
		Map<String, Object> result = new HashMap<String, Object>();
		Statistics chartData = new Statistics();
		chartData.setColorArray(colorArray);
		
		List<Statistics> list = statisticsMapper.getUserRelatedChartData(statistics);
		List<String> titleList = new ArrayList<String>();
		titleList.add("Total");
		titleList.add("Activor");
		
		if(CoConstDef.FLAG_YES.equals(statistics.getIsRawData())) {
			for(Statistics stat : list) {
				stat.setTotal();
			}
			
			titleList.add("Total");
			result.put("chartData", list);
			result.put("titleArray", titleList);
		} else {
			for(Statistics data : list) {
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
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory1Cnt();
				noneSum += cnt;
			}
			break;
		case 2:
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory2Cnt();
				noneSum += cnt;
			}
			break;
		case 3:
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory3Cnt();
				noneSum += cnt;
			}
			break;
		case 4:
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory4Cnt();
				noneSum += cnt;
			}
			break;
		case 5:
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory5Cnt();
				noneSum += cnt;
			}
			break;
		case 6:
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory6Cnt();
				noneSum += cnt;
			}
			break;
		case 7:
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory7Cnt();
				noneSum += cnt;
			}
			break;
		case 8:
			for (int i=0; i<list.size(); i++) {
				int cnt = list.get(i).getCategory8Cnt();
				noneSum += cnt;
			}
			break;
		}
		return noneSum;
	}
}
