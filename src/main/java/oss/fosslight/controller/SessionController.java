/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.SESSION;
import oss.fosslight.config.JwtTokenProvider;
import oss.fosslight.domain.T2Authorities;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.CookieUtil;
import oss.fosslight.util.ResponseUtil;
import oss.fosslight.util.StringUtil;

@Controller
@Slf4j
public class SessionController extends CoTopComponent{

    @Autowired private JwtTokenProvider jwtTokenProvider;
	@Autowired T2UserService userService;
	/** The cookie util. */
	@Autowired private CookieUtil cookieUtil;
    
	@GetMapping(value = SESSION.LOGIN, produces = "text/html; charset=utf-8")
	public String login(HttpServletRequest req, HttpServletResponse res) throws IOException {
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
		
		ResponseUtil.setDefaultLocalStorage(res);
		return "session/login";
	}
	
	@PostMapping(value = {"/session/login-proc"})
	public ResponseEntity<Object> loginProc(@Validated @ModelAttribute("managerInfo") T2Users accountInfo, BindingResult bindingResult
										, HttpServletRequest req, HttpServletResponse res, Model model) {

        boolean loginSuccess = false;
        Map<String, Object> rtnMap = new HashMap<>();
        String userId = accountInfo.getUserId();
        String rawPassword = accountInfo.getPassword();
        String email = accountInfo.getEmail();
        
        rtnMap.put("email", email);
        String validMsg = "";
        
		try {
			// 1. validation Check
			if (StringUtil.isEmpty(userId)) {
				validMsg = "Please enter your user id";
			} else if(StringUtil.isEmpty(rawPassword)) {
				validMsg = "Please enter your user pw";
			}
		
			if (!StringUtil.isEmpty(validMsg)) {
				return makeJsonResponseHeader(false, validMsg);
			}
			
	        String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
	        List<String> customAccounts = Arrays.asList(CommonFunction.emptyCheckProperty("custom.accounts", "").split(","));
	        
	        if (CoConstDef.FLAG_YES.equals(ldapFlag) && !customAccounts.contains(userId)) {
	        	rtnMap = userService.checkByADUser(userId, rawPassword, rtnMap);
	        	loginSuccess = (boolean) rtnMap.get("isAuthenticated");
	        } else {
	        	loginSuccess = userService.checkSystemUser(userId, rawPassword);
	        }
	        
	        loginSuccess = true;
			
	        if (loginSuccess) {
//	            List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
//	            T2Users user = new T2Users();
//	            user.setUserId(userId);
//	            T2Users getUser = userService.getUserAndAuthorities(user);
//	            
//	            for (T2Authorities auth : getUser.getAuthoritiesList()) {
//	            	roles.add(new SimpleGrantedAuthority(auth.getAuthority()));
//	            }
	        	
	        	T2Users user = userService.getUser(accountInfo);
				String token = jwtTokenProvider.generateToken(user);
				cookieUtil.addCookie(res, "X-FOSS-AUTH-TOKEN", token, 60*60*24);
				Map<String, String> loginData = new HashMap<>();
				loginData.put("locale", user.getDefaultLocale());
		        return makeJsonResponseHeader(true, null, loginData);
//	            return new UsernamePasswordAuthenticationToken(user_id, user_pw, roles);          
	        } else {
	        	if (rtnMap.containsKey("msg")) {
	        		throw new BadCredentialsException((String) rtnMap.get("msg"));
	        	} else {
	                throw new BadCredentialsException("Bad credentials");
	        	}
	        }

//			// 5. token 값 생성 및 cookie에 담음.
//			String token = jwtTokenProvider.generateToken(accountInfo);
//			cookieUtil.addCookie(res, "X-AUTH-TOKEN-ADM", token, 60*60*24);
//			ResponseUtil.redirect(res, "/index");
//			return null;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}

	}

	@GetMapping(value = SESSION.LOGIN_EXPIRED, produces = "text/html; charset=utf-8")
	public void loginExpired(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ResponseUtil.setDefaultLocalStorage(res);
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
