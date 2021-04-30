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
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.Url.CONFIGURATION;
import oss.fosslight.service.ConfigurationService;

@Controller
public class ConfigurationController extends CoTopComponent {

	@Autowired ConfigurationService configurationService;
	
	@GetMapping(value=CONFIGURATION.EDIT, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		return CONFIGURATION.EDIT_JSP;
	}
	
	@PostMapping(value=CONFIGURATION.SAVE_AJAX, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> listajax(@ModelAttribute oss.fosslight.domain.Configuration configuration, HttpServletRequest req, HttpServletResponse res){
		HashMap<String,Object> resMap = new HashMap<>();
		configurationService.updateDefaultTab(configuration);
		resMap.put("resCd", "10");
		return makeJsonResponseHeader(resMap);
	}
}
