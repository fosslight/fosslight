/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.MAIN;

@Controller
public class MainController{
	
	@GetMapping(value={MAIN.INDEX_EMPTY, MAIN.INDEX})
	public String index(HttpServletRequest req, HttpServletResponse res, Model model, Principal principal, HttpSession session) {
		model.addAttribute("dashboardFlag", CommonFunction.propertyFlagCheck("menu.dashboard.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("statisticsFlag", CommonFunction.propertyFlagCheck("menu.statistics.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("binarydbFlag", CommonFunction.propertyFlagCheck("menu.binarydb.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("complianceStatusFlag", CommonFunction.propertyFlagCheck("menu.compliancestatus.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("externalLinkFlag", CommonFunction.propertyFlagCheck("menu.externallink.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("checkFlag", CommonFunction.propertyFlagCheck("checkFlag", CoConstDef.FLAG_YES));
		return "main/main";
	}
	
	@RequestMapping(value= { CoConstDef.HEALTH_CHECK_URL }, produces=MediaType.TEXT_HTML_VALUE)
	public void healthCheck( HttpServletRequest req, HttpServletResponse res ) throws IOException {
		String ip = req.getHeader("X-FORWARDED-FOR");
		
		if (ip == null) {
			ip = req.getRemoteAddr();
		}
		
		PrintWriter pw = res.getWriter();
		pw.write(" - Client IP : " + ip);
		pw.close();
	}
}
