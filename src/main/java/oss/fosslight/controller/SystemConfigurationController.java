/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.SYSTEM_CONFIGURATION;
import oss.fosslight.service.SystemConfigurationService;

@Controller
@Slf4j
public class SystemConfigurationController extends CoTopComponent {
	@Autowired SystemConfigurationService  configurationService;
	
	@GetMapping(value=SYSTEM_CONFIGURATION.PAGE, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		model.addAttribute("rootPath", CommonFunction.getProperty("root.dir"));
		model.addAttribute("dashboardFlag", CommonFunction.propertyFlagCheck("menu.dashboard.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("statisticsFlag", CommonFunction.propertyFlagCheck("menu.statistics.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("binarydbFlag", CommonFunction.propertyFlagCheck("menu.binarydb.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("complianceStatusFlag", CommonFunction.propertyFlagCheck("menu.compliancestatus.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("externalLinkFlag", CommonFunction.propertyFlagCheck("menu.externallink.use.flag", CoConstDef.FLAG_YES));


		return SYSTEM_CONFIGURATION.PAGE_JSP;
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value=SYSTEM_CONFIGURATION.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(
			@RequestBody HashMap<String, Object> map,
			HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		Map<String, Object> result = new HashMap<String, Object>();
		
		String config = (String) map.get("config");
		Type collectionType = new TypeToken<Map<String, Object>>() {}.getType();
		Map<String, Object> configMap = new HashMap<String, Object>();
		configMap = (Map<String, Object>) fromJson(config, collectionType);
		
		try {
			result = configurationService.saveConfiguration(configMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.put("resCd", "00");
			result.put("resMsg", "Error");
		}
		
		return makeJsonResponseHeader(result);
	}
}
