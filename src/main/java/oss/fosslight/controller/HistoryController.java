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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.Url.HISTORY;
import oss.fosslight.domain.History;
import oss.fosslight.service.HistoryService;

@Controller
public class HistoryController extends CoTopComponent {
	@Autowired HistoryService historyService;
	
	@GetMapping(value=HISTORY.LIST, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return "system/history";
	}
	
	@GetMapping(value=HISTORY.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(
			History history
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		history.setSortField(req.getParameter("sidx"));
		history.setSortOrder(req.getParameter("sord"));
		return makeJsonResponseHeader(historyService.getList(history));
	}
	
	@GetMapping(value=HISTORY.EDIT_IDX, produces = "text/html; charset=utf-8")
	public String edit(@PathVariable String idx, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		History history = new History(idx);
		Map<String, Object> map = historyService.getAsToBeHistoryDataByGrid(history);
		model.addAttribute("basicInfo", historyService.getData(history));
		model.addAttribute("history", map);

		return "system/history-edit";
	}
}
