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
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.Url.CODE;
import oss.fosslight.domain.T2Code;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.service.CodeService;
import oss.fosslight.validation.T2CoValidationResult;

@Controller
public class CodeController extends CoTopComponent {
	
	@Autowired CodeService CodeService;
	
	@GetMapping(value=CODE.PAGE, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return CODE.PAGE_JSP;
	}
	
	@GetMapping(value=CODE.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> codeList(
			@ModelAttribute T2Code vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		vo.setCurPage(page);
		vo.setPageListSize(rows);
		vo.setSortField(sidx);
		vo.setSortOrder(sord);
		
		Map<String, Object> map = CodeService.getCodeList(vo);
		
		return makeJsonResponseHeader(map);
		
	}
	
	@PostMapping(value=CODE.DETAIL_LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> codeDetailList(
			@ModelAttribute T2CodeDtl vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		ArrayList<T2CodeDtl> codeDetailList = CodeService.getCodeDetailList(vo);
		map.put("codeDetailList", codeDetailList);
		
		return makeJsonResponseHeader(map);
		
	}
	
	@PostMapping(value=CODE.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> codeSave(
			@ModelAttribute T2Code vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		T2CoValidationResult vResult = validateWithAppendix(req, "PROC_MODE", "ADD");
		
		if (!vResult.isValid()) {
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		CodeService.setCode(vo);
		CoCodeManager.getInstance().refreshCodes();
		
		return makeJsonResponseHeader();
	}
	
	@PostMapping(value=CODE.DETAIL_SAVE_AJAX,  produces = {
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

	@GetMapping(value=CODE.AUTOCOMPLETE_NO_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteNoAjax(
			T2Code t2Code
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		List<T2Code> list = CodeService.getcodeList(t2Code);
		
		return makeJsonResponseHeader(list);
	}
	
	@GetMapping(value=CODE.AUTOCOMPLETE_NM_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteNmAjax(
			T2Code t2Code
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		List<T2Code> list = CodeService.getcodeNmList(t2Code);
		
		return makeJsonResponseHeader(list);
	}
}