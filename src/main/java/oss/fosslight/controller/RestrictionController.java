/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import io.jsonwebtoken.lang.Arrays;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.Url.RESTRICTION;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.T2Code;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.service.CodeService;
import oss.fosslight.service.RestrictionService;
import oss.fosslight.validation.T2CoValidationResult;

@Controller
public class RestrictionController extends CoTopComponent {
	
	@Autowired CodeService CodeService;
	@Autowired RestrictionService restrictionService;
	
	@GetMapping(value=RESTRICTION.PAGE, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return "system/restriction";
	}
	
	@GetMapping(value=RESTRICTION.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> codeDetailList(
			@ModelAttribute T2CodeDtl vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		ArrayList<T2CodeDtl> codeDetailList = CodeService.getCodeDetailList(vo, true);
		map.put("restrictionList", codeDetailList);
		
		return makeJsonResponseHeader(map);
		
	}
	
	@PostMapping(value=RESTRICTION.SAVE_AJAX,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public @ResponseBody ResponseEntity<Object> codeDetailSave(
			@RequestBody T2Code vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{

		T2CoValidationResult vResult = validate(vo.getT2codeDtl());
		
		if (!vResult.isValid()) {
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		CodeService.setCodeDetails(vo.getT2codeDtl(), vo.getCdNo());
		CoCodeManager.getInstance().refreshCodes();
		
		return makeJsonResponseHeader();
	}
	
	@PostMapping(value=RESTRICTION.CHECK_RESTRICTION)
	public @ResponseBody ResponseEntity<Object> checkRestriction(@PathVariable String cdDtlNo, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		Map<String, Object> rtnMap = restrictionService.getOssLicenseInfobyRestriction(cdDtlNo);
		
		return makeJsonResponseHeader(true, null, rtnMap);
	}
}