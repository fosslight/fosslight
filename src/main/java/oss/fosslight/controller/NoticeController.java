/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

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
import oss.fosslight.common.Url.NOTICE;
import oss.fosslight.domain.Notice;
import oss.fosslight.service.NoticeService;
import oss.fosslight.validation.T2CoValidationResult;

@Controller
public class NoticeController extends CoTopComponent {
	@Autowired NoticeService noticeService;

	@GetMapping(value=NOTICE.LIST, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return NOTICE.LIST_JSP;
	}
	
	@GetMapping(value=NOTICE.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(
			@ModelAttribute Notice vo
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

		return makeJsonResponseHeader(noticeService.getNoticeList(vo));
		
	}
	
	@PostMapping(value=NOTICE.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(
			@ModelAttribute Notice vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		T2CoValidationResult vResult = validateWithAppendix(req, "PROC_MODE", "ADD");
		
		if(!vResult.isValid()) {
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		noticeService.setNotice(vo);
		return makeJsonResponseHeader();
	}
	
	@RequestMapping(value=NOTICE.PUBLISHEDT_NOTICE, method = {RequestMethod.POST, RequestMethod.GET})
	public @ResponseBody ResponseEntity<Object> getPublishedtNotice(
			@ModelAttribute Notice vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		return makeJsonResponseHeader(noticeService.getPublishedNotice(vo));
	}
}
