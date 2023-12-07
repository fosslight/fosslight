/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.Url.PROCESSGUIDE;
import oss.fosslight.domain.ProcessGuide;
import oss.fosslight.service.ProcessGuideService;

@Controller
public class ProcessGuideController extends CoTopComponent {
	@Autowired ProcessGuideService processGuideService;
	
	/**
	 * [화면] Code 
	 */
	@GetMapping(value=PROCESSGUIDE.PAGE, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return "system/processGuide :: content";
	}
	
	/**
	 * [API] 코드 목록 조회
	 */
	@GetMapping(value=PROCESSGUIDE.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(
			@ModelAttribute ProcessGuide vo
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

		Map<String, Object> map = processGuideService.getProcessGuideList(vo);
		
		return makeJsonResponseHeader(map);
		
	}
	
	
	@PostMapping(value=PROCESSGUIDE.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(
			@ModelAttribute ProcessGuide vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		processGuideService.setProcessGuide(vo);
		
		return makeJsonResponseHeader();
	}
	
	@RequestMapping(value={PROCESSGUIDE.PROCESS_GUIDE}, method = {RequestMethod.POST, RequestMethod.GET})
	public @ResponseBody ResponseEntity<Object> getPublishedProcessGuide(
			@ModelAttribute ProcessGuide vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		
		return makeJsonResponseHeader(processGuideService.getProcessGuide(vo));
	}
}
