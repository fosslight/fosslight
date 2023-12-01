/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.SESSION;

@Controller
public class SessionController extends CoTopComponent{
	
	@GetMapping(value = SESSION.LOGIN, produces = "text/html; charset=utf-8")
	public String user(HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (isLogin()) {
			res.sendRedirect(req.getContextPath() + "/index");
		}
		
		/* 
			TODO - 추후 특정 이슈가 발생하여 server를 일정기간 Server를 내릴때 해당 기능 사용
			
			try {
				SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
				Date d1 = f.parse("2019-06-11 18:00:00");
				Date d2 = f.parse("2019-06-12 09:00:00");
				
				long d1_timestamp = d1.getTime() / 1000;
				long d2_timestamp = d2.getTime() / 1000;
				long today_timestamp = System.currentTimeMillis() / 1000;
	
				if (d1_timestamp < today_timestamp && d2_timestamp > today_timestamp){
					res.sendRedirect(req.getContextPath() + AppConstBean.ERROR_PAGES_DEFAULT);
				}
				
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
		*/
		
		return "main/index";
	}
	
	@GetMapping(value = SESSION.LOGIN_EXPIRED, produces = "text/html; charset=utf-8")
	public void loginExpired(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.sendRedirect(req.getContextPath() + "/index");
	}
	
	@GetMapping(value=SESSION.SESSION_SAVE_KEY_VAL, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> sessionKeyValSave(HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String sesKey){
		final String path = req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
	    final String bestMatchingPattern = req.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
	    String sesVal = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);
		
		putSessionObject(CoConstDef.SESSION_KEY_PREFIX_DEFAULT_SEARCHVALUE + sesKey, sesVal);
		return makeJsonResponseHeader();
	}
}
