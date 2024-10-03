/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.Url.SENT_MAIL;
import oss.fosslight.domain.CoMail;
import oss.fosslight.service.SentMailService;

@Controller
public class SentMailController extends CoTopComponent {
	
	@Autowired SentMailService sentMailService;
	
	@GetMapping(value=SENT_MAIL.PAGE, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return "system/sentMail";
	}
	
	@GetMapping(value=SENT_MAIL.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> codeList(
			@ModelAttribute CoMail vo
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
		
		Map<String, Object> map = sentMailService.getMailList(vo);
		
		return makeJsonResponseHeader(map);
		
	}
}
