/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.SearchType;
import oss.fosslight.common.Url.CONFIGURATION;
import oss.fosslight.domain.Configuration;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.service.ConfigurationService;
import oss.fosslight.service.SearchService;
import oss.fosslight.service.T2UserService;

@Controller
@Slf4j
public class ConfigurationController extends CoTopComponent {

	@Autowired ConfigurationService configurationService;
	@Autowired SearchService searchService;
	@Autowired T2UserService userService;
	
	@GetMapping(value=CONFIGURATION.EDIT, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		model.addAttribute("userInfo", userService.getLoginUserInfo());
		return CONFIGURATION.EDIT_JSP;
	}
	
	@PostMapping(value=CONFIGURATION.SAVE_AJAX, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> updateDefaultSetting(@ModelAttribute Configuration configuration, HttpServletRequest req, HttpServletResponse res){
		HashMap<String,Object> resMap = new HashMap<>();
		try {
			configurationService.updateDefaultSetting(configuration);
			resMap.put("resCd", "10");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			resMap.put("resCd", "20");
		}
		return makeJsonResponseHeader(resMap);
	}
	
	@GetMapping(value=CONFIGURATION.VIEW_SEARCH_CONDITION_AJAX, produces = "text/html; charset=utf-8")
	public String loadDefaultSearchCondition(HttpServletRequest req, HttpServletResponse res, @ModelAttribute Configuration configuration, Model model){

        Object searchFilter = searchService.getSearchFilter(configuration.getDefaultSearchType(), loginUserName());

        if (searchFilter != null) {
			model.addAttribute("searchBean", searchFilter);
        } else {
			switch (SearchType.valueOf(configuration.getDefaultSearchType())) {
			case LICENSE:
				model.addAttribute("searchBean", new LicenseMaster());
				break;
			case OSS:
				model.addAttribute("searchBean", new OssMaster());
				break;
			case PROJECT:
			case SELF_CHECK:
				model.addAttribute("searchBean", new Project());
				break;
			case THIRD_PARTY:
				model.addAttribute("searchBean", new PartnerMaster());
				break;
			default:
				break;
			}
		}
        model.addAttribute("defaultSearchType", configuration.getDefaultSearchType());
		
		return CONFIGURATION.VIEW_SEARCH_CONDITION_JSP;
	}
	
	@PostMapping(value=CONFIGURATION.UPDATE_SEARCH_CONDITION_AJAX)
	public @ResponseBody ResponseEntity<Object> updateDefaultSearchCondition(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res){
		Map<String,Object> resMap = new HashMap<>();
		try {
			
			// Check Comma separated Multiple Checkbox values
			List<String> unnecessaryKeys = new ArrayList<>();
			for (String key : params.keySet()) {
				if (key.startsWith("chk_")) {
					params.put(key.replaceFirst("chk_", ""), params.get(key));
					unnecessaryKeys.add(key);
				}
			}
			if (!unnecessaryKeys.isEmpty()) {
				for (String key : unnecessaryKeys) {
					params.remove(key);
				}
			}
			
			searchService.saveSearchFilter(params, loginUserName());
			resMap.put("resCd", "10");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			resMap.put("resCd", "20");
		}
		return makeJsonResponseHeader(resMap);
	}

//	@PostMapping(value=CONFIGURATION.SAVE_DEFAULT_LOCALE_AJAX, produces = "text/html; charset=utf-8")
//	public @ResponseBody ResponseEntity<Object> saveDefaultLocale(@ModelAttribute Configuration configuration, HttpServletRequest req, HttpServletResponse res){
//		HashMap<String,Object> resMap = new HashMap<>();
//		configurationService.updateDefaultLocale(configuration);
//		resMap.put("resCd", "10");
//		return makeJsonResponseHeader(resMap);
//	}
}
