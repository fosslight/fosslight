/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public class AjaxSessionTimeoutFilter implements Filter {
	
	private String ajaxHaeder = "AJAX";
	private static final Logger log = LoggerFactory.getLogger(AjaxSessionTimeoutFilter.class.getName());
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        boolean ignoreCheck = "/system/user/saveAjax".equals(req.getRequestURI());
        
        if (!ignoreCheck && isAjaxRequest(req)) {
	        	// spring security check 필요
                // 임시로 수동체크 추가
                Authentication auth = SecurityContextHolder.getContext().getAuthentication(); 
                
                if (auth == null || "anonymousUser".equalsIgnoreCase(auth.getName()) || !auth.isAuthenticated()) {
                	log.error("handle sendError: " + HttpServletResponse.SC_FORBIDDEN);
                	
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                	try {
	                       chain.doFilter(req, res);
	                       return;
	                } catch (AccessDeniedException e) {
	                	log.error("sendError: " + HttpServletResponse.SC_FORBIDDEN);
	                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
	                } catch (AuthenticationException e) {
	                	log.error("sendError: " + HttpServletResponse.SC_UNAUTHORIZED);
	                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	                }
                }
        } else {
        	chain.doFilter(req, res);
        	return;
        }
	}
	
	private boolean isAjaxRequest(HttpServletRequest req) {
		return req.getHeader(ajaxHaeder) != null && req.getHeader(ajaxHaeder).equals(Boolean.TRUE.toString());
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}
	
    public void setAjaxHeader(String ajaxHeader) {
    	this.ajaxHaeder = ajaxHeader;
    }
}
