/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.SESSION;
import oss.fosslight.config.JwtTokenProvider;
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
	
	private static String SSO_SERVER_URL = CommonFunction.emptyCheckProperty("sso.server.url", "");
	private static String SSO_CLIENT_ID = CommonFunction.emptyCheckProperty("sso.server.client.id", "");
	private static String SSO_CLIENT_SECRET = CommonFunction.emptyCheckProperty("sso.server.client.secret", "");
	private static String SSO_REDIRECT_URL = CommonFunction.emptyCheckProperty("server.domain", "");
	private static String redirectUrl = UriComponentsBuilder.fromUriString(SSO_SERVER_URL + "/auth")
											.queryParam("response_type", "code")
											.queryParam("scope", "email profile openid")
											.queryParam("client_id", SSO_CLIENT_ID)
											.queryParam("redirect_uri", SSO_REDIRECT_URL + "/session/sso/callback")
											.build().toUriString();
	
	@GetMapping(value = SESSION.LOGIN, produces = "text/html; charset=utf-8")
	public void login(HttpServletRequest req, HttpServletResponse res) throws IOException {
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
		if (CoConstDef.FLAG_YES.equals(CommonFunction.emptyCheckProperty("sso.useflag", CoConstDef.FLAG_NO))) {
			res.sendRedirect(redirectUrl);
		} else {
			res.sendRedirect(req.getContextPath() + "/session/loginPage");
		}
	}
	
	@GetMapping(value = SESSION.LOGIN_PAGE)
	public String loginPage(HttpServletRequest req, HttpServletResponse res) throws Exception {
		ResponseUtil.setDefaultLocalStorage(res);
		return "session/login";
	}
	
	@GetMapping(value = SESSION.LOGIN_MNG, produces = "text/html; charset=utf-8")
	public String loginMng(HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (isLogin()) {
			res.sendRedirect(req.getContextPath() + "/index");
		}
		
		ResponseUtil.setDefaultLocalStorage(res);
		return "session/login";
	}
	
	@GetMapping(value = {"/session/sso/callback"})
	public void ssoCallback(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		String code = "";
		
		Enumeration params = req.getParameterNames();
		while (params.hasMoreElements()) {
			String name = (String) params.nextElement();
			if (name.equals("code")) {
				code = req.getParameter(name);
				break;
			}
		}
		
		Map<String, String> param = new HashMap<>();
		param.put("grant_type", "authorization_code");
		param.put("code", code);
		param.put("client_id", SSO_CLIENT_ID);
		param.put("client_secret", SSO_CLIENT_SECRET);
		param.put("scope", "email profile openid");
		param.put("redirect_uri", SSO_REDIRECT_URL + "/session/sso/callback");
		String requestUri = SSO_SERVER_URL + "/token";
		
		try {
			String accessToken = "";
			Map<String, String> tokenObj = requestInfoForSingleSignOn(param, requestUri, MediaType.APPLICATION_FORM_URLENCODED);
			if (tokenObj != null && tokenObj.containsKey("access_token")) {
				accessToken = tokenObj.get("access_token");
				
				param.clear();
				param.put("Authorization", "Bearer " + accessToken);
				
				requestUri = SSO_SERVER_URL + "/userinfo";
				Map<String, String> userInfoObj = requestInfoForSingleSignOn(param, requestUri, MediaType.APPLICATION_JSON);
				if (userInfoObj != null) {
					T2Users accountInfo = new T2Users();
					if (userService.checkBySSOUser(userInfoObj, accountInfo)) {
						T2Users user = userService.getUser(accountInfo);
						String token = jwtTokenProvider.generateToken(user);
						cookieUtil.addCookie(res, "X-FOSS-AUTH-TOKEN", token, 60*60*24);
						if (!isEmpty(user.getDefaultLocale())) {
							res.sendRedirect(SSO_REDIRECT_URL + "/index?lang="+user.getDefaultLocale());
						} else {
							res.sendRedirect(SSO_REDIRECT_URL);
						}
					} else {
						res.sendRedirect(redirectUrl);
					}
				} else {
					res.sendRedirect(redirectUrl);
				}
			} else {
				res.sendRedirect(redirectUrl);
			}
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res.sendRedirect(redirectUrl);
        }
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> requestInfoForSingleSignOn(Map<String, String> param, String requestUri, MediaType mediaType) {
		Map<String, String> rtnMap = null;
		JsonObject jsonObj = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		if (MediaType.APPLICATION_JSON.equals(mediaType)) {
			headers.add("scope", "openid");
			headers.add("Authorization", String.valueOf(param.get("Authorization")));
		} else {
			for (String key : param.keySet() ) {
	            map.add(key, String.valueOf(param.get(key)));
	        }
		}
		
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(requestUri, requestEntity, String.class);
        
        JsonParser jsonParser = new JsonParser();
        String getBody = responseEntity.getBody();
        jsonObj = jsonParser.parse(getBody).getAsJsonObject();
        
        try {
			rtnMap = new ObjectMapper().readValue(jsonObj.toString(), Map.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        
        return rtnMap;
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
