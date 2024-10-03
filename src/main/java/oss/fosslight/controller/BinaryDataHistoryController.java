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
import oss.fosslight.common.Url.BINARY_DATA_HISTORY;
import oss.fosslight.domain.BinaryAnalysisResult;
import oss.fosslight.service.BinaryDataHistoryService;

@Controller
public class BinaryDataHistoryController extends CoTopComponent {
	@Autowired BinaryDataHistoryService binaryDataHistoryService;
	
	@GetMapping(value=BINARY_DATA_HISTORY.PAGE, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return "system/binaryDataHistory";
	}
	
	@GetMapping(value=BINARY_DATA_HISTORY.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> codeList(
			@ModelAttribute BinaryAnalysisResult bean
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		bean.setCurPage(page);
		bean.setPageListSize(rows);
		bean.setSortField(sidx);
		bean.setSortOrder(sord);
		
		Map<String, Object> map = binaryDataHistoryService.getBinaryDataHistoryList(bean);
		
		return makeJsonResponseHeader(map);
		
	}
}
