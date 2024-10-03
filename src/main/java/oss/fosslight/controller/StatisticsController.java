/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.STATISTICS;
import oss.fosslight.domain.Statistics;
import oss.fosslight.service.StatisticsService;
import oss.fosslight.util.StringUtil;

@Controller
public class StatisticsController extends CoTopComponent{
	/** The statistics service. */
	@Autowired StatisticsService statisticsService;
	
	@GetMapping("/statistics/view")
	public String edit(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));
				
		return "statistics/view";
	}
	
	@GetMapping(value=STATISTICS.DIVISIONAL_PROJECT_CHART)
	public @ResponseBody ResponseEntity<Object> divisionProjectChart(HttpServletRequest req
			, HttpServletResponse res
			, Model model) {
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		String type = req.getParameter("categoryType");
		String isRawData = req.getParameter("isRawData");
		
		Statistics stat = new Statistics();
		stat.setStartDate(startDate);
		stat.setEndDate(endDate);
		stat.setCategoryType(type);
		stat.setIsRawData(isRawData);
		
		return makeJsonResponseHeader(statisticsService.getDivisionalProjectChartData(stat));
	}
	
	@GetMapping(value=STATISTICS.MOST_USED_CHART)
	public @ResponseBody ResponseEntity<Object> mostUsedChart(HttpServletRequest req
			, HttpServletResponse res
			, Model model) {
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		String divisionNo = req.getParameter("divisionNo");
		int pieSize = Integer.parseInt(req.getParameter("pieSize"));
		String chartType = req.getParameter("chartType");
		String isRawData = req.getParameter("isRawData");
		
		Statistics stat = new Statistics();
		stat.setStartDate(startDate);
		stat.setEndDate(endDate);
		if (divisionNo.contains(",")) {
			String[] divisionNums = divisionNo.split(",");
			stat.setDivisionNums(divisionNums);
		}else {
			stat.setDivisionNo(divisionNo);
		}
		stat.setPieSize(pieSize);
		stat.setChartType(chartType);
		stat.setIsRawData(isRawData);
		
		return makeJsonResponseHeader(statisticsService.getMostUsedChartData(stat));
	}
	
	@GetMapping(value=STATISTICS.UPDATED_CHART)
	public @ResponseBody ResponseEntity<Object> updatedOssChart(HttpServletRequest req
			, HttpServletResponse res
			, Model model) {
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		String type = req.getParameter("categoryType");
		String chartType = req.getParameter("chartType");
		String isRawData = req.getParameter("isRawData");
		
		Statistics stat = new Statistics();
		stat.setStartDate(startDate);
		stat.setEndDate(endDate);
		stat.setCategoryType(type);
		stat.setChartType(chartType);
		stat.setIsRawData(isRawData);
		
		if ("OSS".equals(chartType)) {
			return makeJsonResponseHeader(statisticsService.getUpdatedOssChartData(stat));
		} else { // LICENSE
			return makeJsonResponseHeader(statisticsService.getUpdatedLicenseChartData(stat));
		}
	}
	
	@GetMapping(value=STATISTICS.TRDPARTY_RELATED_CHART)
	public @ResponseBody ResponseEntity<Object> trdPartyRelatedChart(HttpServletRequest req
			, HttpServletResponse res
			, Model model) {
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		String type = req.getParameter("categoryType");
		String isRawData = req.getParameter("isRawData");
		
		if ("STT".equals(type)) {
			type = "3rd" + type;
		}
		
		Statistics stat = new Statistics();
		stat.setStartDate(startDate);
		stat.setEndDate(endDate);
		stat.setCategoryType(type);
		stat.setIsRawData(isRawData);
		
		return makeJsonResponseHeader(statisticsService.getTrdPartyRelatedChartData(stat));
	}
	
	@GetMapping(value=STATISTICS.USER_RELATED_CHART)
	public @ResponseBody ResponseEntity<Object> userRelatedChart(HttpServletRequest req
			, HttpServletResponse res
			, Model model) {
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		String isRawData = req.getParameter("isRawData");
		
		Statistics stat = new Statistics();
		stat.setStartDate(startDate);
		stat.setEndDate(endDate);
		stat.setIsRawData(isRawData);
		
		return makeJsonResponseHeader(statisticsService.getUserRelatedChartData(stat));
	}
	
	@GetMapping(value=STATISTICS.STATISTICS_POPUP)
	public String binarypopup (
			@ModelAttribute Statistics statistics
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		String categoryType = req.getParameter("categoryType");
		String divisionNo = req.getParameter("divisionNo");
		String pieSize = req.getParameter("pieSize");
		String chartType = req.getParameter("chartType");
		
		model.addAttribute("chartName", req.getParameter("chartName"));
		
		if (!StringUtil.isEmpty(startDate)) {
			model.addAttribute("startDate", startDate);
		}
		
		if (!StringUtil.isEmpty(endDate)) {
			model.addAttribute("endDate", endDate);
		}
		
		if (!StringUtil.isEmpty(categoryType)) {
			model.addAttribute("categoryType", categoryType);
		}
		
		if (!StringUtil.isEmpty(divisionNo)) {
			model.addAttribute("divisionNo", divisionNo);
		}
		
		if (!StringUtil.isEmpty(pieSize)) {
			model.addAttribute("pieSize", pieSize);
		}
		
		if (!StringUtil.isEmpty(chartType)) {
			model.addAttribute("chartType", chartType);
		}
		
		return "statistics/view/statisticsPopup";
	}
}
