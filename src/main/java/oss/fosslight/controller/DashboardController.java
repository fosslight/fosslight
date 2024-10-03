/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.DASHBOARD;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.service.DashboardService;

@Controller
public class DashboardController extends CoTopComponent{
	@Autowired DashboardService dashboardService;
	
	@GetMapping(value=DASHBOARD.LIST)
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		model.addAttribute("userId", loginUserName());
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));
		
		return "dashboard/list";
	}
	
	@GetMapping(value=DASHBOARD.JOBSLIST)
	public @ResponseBody ResponseEntity<Object> jobsListAjax(
			Project project
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		project.setCurPage(1);
		project.setPageListSize(1);
		
		return makeJsonResponseHeader(dashboardService.getDashboardJobsList(project));
	}
	
	@PostMapping(value=DASHBOARD.COMMENTLIST)
    public String commentsListAjax(@RequestBody Map<String, Object> param, HttpServletRequest req, HttpServletResponse res, Model model){
		model.addAttribute("comments", dashboardService.getDashboardCommentsList(param));
		return "dashboard/view/commentsView";
    }
	
	@GetMapping(value=DASHBOARD.OSSLIST)
    public @ResponseBody ResponseEntity<Object> ossListAjax(
            OssMaster ossMaster
            , HttpServletRequest req
            , HttpServletResponse res
            , Model model){
        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));
        ossMaster.setCurPage(page);
        ossMaster.setPageListSize(rows);
        
        return makeJsonResponseHeader(dashboardService.getDashboardOssList(ossMaster));
    }
	
	@GetMapping(value=DASHBOARD.LICENSELIST)
    public @ResponseBody ResponseEntity<Object> licenseListAjax(
            LicenseMaster licenseMaster
            , HttpServletRequest req
            , HttpServletResponse res
            , Model model){
        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));
        licenseMaster.setCurPage(page);
        licenseMaster.setPageListSize(rows);
        
        Map<String, Object> map = dashboardService.getDashboardLicenseList(licenseMaster);
        return makeJsonResponseHeader(map);
    }
	
	@PostMapping(value=DASHBOARD.READCONFIRM_ALL)
	public @ResponseBody ResponseEntity<Object> readConfirmAll(
			@ModelAttribute CommentsHistory commentsHistory
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		String resCd="00";
		HashMap<String,Object> resMap = new HashMap<>(); 
		
		dashboardService.readConfirmAll(commentsHistory);
		
		resCd="10";		
		resMap.put("resCd", resCd);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@GetMapping(value=DASHBOARD.PROGPROJECTCNT)
    public @ResponseBody ResponseEntity<Object> progProjectCnt(
            HttpServletRequest req
            , HttpServletResponse res
            , Model model){
		return makeJsonResponseHeader(dashboardService.getProgProjectCnt());
    }
	
	@GetMapping(value=DASHBOARD.DISCOVEREDEMLLIST)
    public String discoveredEmlListAjax(HttpServletRequest req, HttpServletResponse res, Model model){
		model.addAttribute("discoveredEmlList", dashboardService.getDiscoveredEmlList());
		return "dashboard/view/discoveredEmlView";
    }
	
	@PostMapping(value=DASHBOARD.DISCOVEREDEMLMESSAGE)
    public @ResponseBody ResponseEntity<Object> discoveredEmlMessage(
    		@RequestBody HashMap<String, Object> param
    		, HttpServletRequest req
            , HttpServletResponse res
            , Model model){
		return makeJsonResponseHeader(dashboardService.getDiscoveredEmlMessage(param));
    }
	
	@GetMapping(value=DASHBOARD.NVDDASHBOARDLIST)
    public String nvdDashboardList(HttpServletRequest req, HttpServletResponse res, Model model){
		model.addAttribute("nvdDashboard", dashboardService.getNvdDashboardList());
		return "dashboard/view/nvdDashboardView";
    }
	
	@GetMapping(value=DASHBOARD.LATESTSCOREDVULNS)
    public String latestScoredVulns(HttpServletRequest req, HttpServletResponse res, Model model){
		model.addAttribute("lstestScoredVulns", dashboardService.getLatestScoredVulns());
		return "dashboard/view/latestScoredVulnsView";
    }
}
