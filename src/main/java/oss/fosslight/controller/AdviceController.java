/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import oss.fosslight.CoTopComponent;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.T2Users;

@ControllerAdvice
public class AdviceController extends CoTopComponent{
	
	@ModelAttribute("req")
	public HttpServletRequest getRequest(HttpServletRequest req){
		return req;
	}
	
	@ModelAttribute("sessUserId")
	public String getSessUserId(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth != null) {
			return auth.getName();
		}
		
		return "";
	}
	
	@SuppressWarnings("unchecked")
	@ModelAttribute("sessUserInfo")
	public T2Users getSessUserInfo(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		HashMap<String, Object> sessDetailInfo = null;
		T2Users sessUserInfo = null;
		
		if(auth != null) {			
			try{
				sessDetailInfo = (HashMap<String, Object>) auth.getDetails();
			}catch(Exception e){
			}
			
			if(sessDetailInfo != null){
				sessUserInfo = (T2Users)sessDetailInfo.get("sessUserInfo");
			}else{
			}
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
