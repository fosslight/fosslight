/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.jsonldjava.utils.Obj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import oss.fosslight.domain.*;
import oss.fosslight.repository.SearchMapper;
import oss.fosslight.service.ConfigurationService;
import oss.fosslight.service.SearchService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;

@Controller
@Slf4j
public class ConfigurationController extends CoTopComponent {

	@Autowired ConfigurationService configurationService;
	@Autowired SearchService searchService;
	@Autowired SearchMapper searchMapper;
	@Autowired T2UserService userService;
	
	@GetMapping(value=CONFIGURATION.EDIT, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		SecurityContext sec = SecurityContextHolder.getContext();
		AbstractAuthenticationToken auth = (AbstractAuthenticationToken)sec.getAuthentication();

		T2Users user = new T2Users();
		user.setUserId(auth.getName());

		model.addAttribute("sessUserInfo", userService.getUserAndAuthorities(user));
		model.addAttribute("userInfo", userService.getLoginUserInfo());
		return "configuration/edit";
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

	@PostMapping(value=CONFIGURATION.UPDATE_LOCALE_AJAX, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> updateDefaultLocaleSetting(@ModelAttribute Configuration configuration, HttpServletRequest req, HttpServletResponse res){
		HashMap<String,Object> resMap = new HashMap<>();
		try {
			String result = configurationService.updateDefaultLocaleSetting(configuration);
			resMap.put("resCd", "10");
			resMap.put("result", result);
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
		
		return "configuration/fragments/searchConditionArea";
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

			// Extract only fields stored in the existing DB
//			String type = (String)params.get("defaultSearchType");
			String userId = loginUserName();
//			String searchFilterString = searchMapper.selectSearchFilter(type, userId);
//
//			Pattern pattern = Pattern.compile("\"([^\"]+)\":");
//			Matcher matcher = pattern.matcher(searchFilterString);
//
//			List<String> array = new ArrayList<>();
//			while (matcher.find()) {
//				array.add(matcher.group(1));
//			}
//
//			Map<String, Object> _params = new HashMap<>();
//			_params.put("defaultSearchType", type);
//			for(String param : array) {
//				_params.put(param, (String)params.get(param));
//			}

			searchService.saveSearchFilter(params, userId);
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
