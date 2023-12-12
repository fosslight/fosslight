/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
		model.addAttribute("jobsList", dashboardService.getCustomDashboardJobsList());
		
		return "dashboard/list";
	}
	
	@GetMapping(value=DASHBOARD.JOBSLIST)
	public @ResponseBody ResponseEntity<Object> jobsListAjax(
			Project project
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		project.setCurPage(page);
		project.setPageListSize(rows);
		
		return makeJsonResponseHeader(dashboardService.getDashboardJobsList(project));
	}
	
	@GetMapping(value=DASHBOARD.COMMENTLIST)
    public @ResponseBody ResponseEntity<Object> commentsListAjax(
    		HttpServletRequest req
            , HttpServletResponse res
            , Model model){
		CommentsHistory commentsHistory = new CommentsHistory();
        return makeJsonResponseHeader(dashboardService.getDashboardCommentsList(commentsHistory));
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
        
        return makeJsonResponseHeader(dashboardService.getDashboardLicenseList(licenseMaster));
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
    public @ResponseBody ResponseEntity<Object> discoveredEmlListAjax(
            HttpServletRequest req
            , HttpServletResponse res
            , Model model){
		return makeJsonResponseHeader(dashboardService.getDiscoveredEmlList());
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
    public @ResponseBody ResponseEntity<Object> nvdDashboardList(
            HttpServletRequest req
            , HttpServletResponse res
            , Model model){
		return makeJsonResponseHeader(dashboardService.getNvdDashboardList());
    }
}
