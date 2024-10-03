/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import oss.fosslight.CoTopComponent;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.T2UserService;

@ControllerAdvice
public class AdviceController extends CoTopComponent{
	@Autowired T2UserService userService;
	
	@ModelAttribute("req")
	public HttpServletRequest getRequest(HttpServletRequest req){
		return req;
	}
	
	@ModelAttribute("sessUserId")
	public String getSessUserId(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth != null) {
			return auth.getName();
		}
		
		return "";
	}
	
	@ModelAttribute("sessUserInfo")
	public T2Users getSessUserInfo(){
		SecurityContext sec = SecurityContextHolder.getContext();
		AbstractAuthenticationToken auth = (AbstractAuthenticationToken)sec.getAuthentication();

		T2Users sessUserInfo = null;
		if (auth.getName() != null && !auth.getName().equals("anonymousUser")) {
			T2Users user = new T2Users();
			user.setUserId(auth.getName());
			sessUserInfo = userService.getUserAndAuthorities(user);
		}
		
		return sessUserInfo;
	}
	
	@ModelAttribute("isMobile")
	public boolean checkMobile(HttpServletRequest req) {
		return req.getHeader("User-Agent").indexOf("Mobile") != -1;
	}
	
	@ModelAttribute("pageTitle") public String pageTitle(){return "";}
	@ModelAttribute("pageTitlePrefix") public String pageTitlePrefix(){return AppConstBean.APP_NAME;}
}
