/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AjaxInterceptor implements HandlerInterceptor {
	
	private static final Logger log = LoggerFactory.getLogger(AjaxInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		boolean result = true;
		
        try {
    		SecurityContext sc = SecurityContextHolder.getContext();
			Authentication auth = sc.getAuthentication();
			
			if (auth == null) {
				//Ajax 콜인지 아닌지 판단
                if (isAjaxRequest(request)){
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
			}
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage());
            return false;
        }
        
        return result;
	}
	
    private boolean isAjaxRequest(HttpServletRequest req) {
        String ajaxHeader = "AJAX";
        
        return req.getHeader(ajaxHeader) != null && req.getHeader(ajaxHeader).equals(Boolean.TRUE.toString());
    }
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView) throws Exception {
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}
}
