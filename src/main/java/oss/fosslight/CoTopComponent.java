/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;

import oss.fosslight.common.CommonFunction;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.ComBean;
import oss.fosslight.domain.T2Authorities;
import oss.fosslight.domain.T2Users;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoAdminValidator;

@Component
@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
public class CoTopComponent {
	
	protected static WebApplicationContext applicationContext;
	
	/* Separation by log type_20210802 */
	protected static final Logger scheduler_log = LoggerFactory.getLogger("SCHEDULER_LOG");
	protected static final Logger oss_history_log = LoggerFactory.getLogger("OSS_HISTORY_LOG");
	protected static final Logger oss_auto_analysis_log = LoggerFactory.getLogger("OSS_AUTO_ANALYSIS_LOG");
	
	@SuppressWarnings("static-access")
	@Autowired
	public void setWebApplicationContext(WebApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	/** The message source. */
	protected static MessageSource messageSource;
	
	@SuppressWarnings("static-access")
	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	protected static Environment appEnv;
	
	@SuppressWarnings("static-access")
	@Resource
	public void setEnvironment(Environment env) throws IOException {
		this.appEnv = env;
	}

    public static Boolean isEmpty(String s) {
		return StringUtil.isEmptyTrimmed(s);
	}
    
    public static Boolean isEmptyWithLineSeparator(String s) {
    	if (s != null) {
    		s = s.replaceAll(System.lineSeparator(), "");
    	}
    	
		return StringUtil.isEmptyTrimmed(s);
	}
    
    public static Boolean isNullObject(Object o) {
    	if (o instanceof String) {
    		return StringUtil.isEmptyTrimmed((String) o);
    	} else {
    		return o == null;
    	}
	}
    
    public static String nvl(String s){
        return StringUtil.defaultString(s);
    }
    
    public static String nvl(String checkedString, String defaultString) {
        return StringUtil.isEmptyTrimmed(checkedString) ? defaultString : checkedString;
    }
    
    protected static String loginUserName() {
    	try {
    		return SecurityContextHolder.getContext().getAuthentication().getName(); 
    	} catch(Exception e) { 
    		return "";
    	}
	}
    
    @SuppressWarnings("unchecked")
	protected static String loginUserRole() {
    	String result = "anonymousUser";
    	
    	try {
	    	Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	    	
	    	if (!authorities.isEmpty()) {
	    		for (GrantedAuthority authority : authorities) {
	    			result = (authority.getAuthority()).replaceFirst(AppConstBean.SECURITY_ROLE_PREFIX, "");
	    			
	    			break;
				}
	    	}
    	} catch(Exception e){}
    	
    	return result;
	}
    
    protected static String userRole(T2Users userInfo) {
    	String result = "anonymousUser";
    	if (!isEmpty(userInfo.getAuthority())) {
    		return userInfo.getAuthority();
    	}
    	List<T2Authorities> authList = userInfo.getAuthoritiesList();
    	if (authList != null && !authList.isEmpty()) {
    		result = authList.get(0).getAuthority();
    	}
    	return result;
    }
    
    protected static boolean isLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && !"anonymousUser".equalsIgnoreCase(auth.getName()) && auth.isAuthenticated()) { 
        	return true;
        }
        
    	return false;
    }
    
    protected static String encodePassword(String rawPassword) {
    	if (rawPassword == null || rawPassword.length() < 1) {
    		return null;
    	}
    	
		return new BCryptPasswordEncoder().encode(rawPassword);
	}
    
    
    protected static String toJson(Object obj) {
		Gson gson = CommonFunction.getGsonBuiler();
		
		return gson.toJson(obj);
	}
    
	protected static Object fromJson(String jsonString, Type collectionType) {
		Gson gson = CommonFunction.getGsonBuiler();
		
		return gson.fromJson(jsonString, collectionType);
	}

	protected static ResponseEntity<Object> makeJsonResponseHeader(Object obj) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8");
		
		return new ResponseEntity<Object>(obj, responseHeaders, HttpStatus.OK); 
	}
	
	protected static ResponseEntity<Object> makeJsonResponseHeader() {
		return makeJsonResponseHeader(true, null);
	}

	protected static ResponseEntity<Object> makeJsonResponseHeader(String alertMsg) {
		return makeJsonResponseHeader(true, alertMsg);
	}
	
	protected static ResponseEntity<Object> makeJsonResponseHeader(boolean isValid, String alertMsg) {
		return makeJsonResponseHeader(isValid, alertMsg, null);
	}
	
	protected static ResponseEntity<Object> makeJsonResponseHeader(boolean isValid, String alertMsg, Object obj) {
		return makeJsonResponseHeader(isValid, alertMsg, obj, null);
	}
	
	protected static ResponseEntity<Object> makeJsonResponseHeader(boolean isValid, String alertMsg, Object obj, Object extObj) {
		return makeJsonResponseHeader(isValid, alertMsg, obj, extObj, null);
	}
	
	protected static ResponseEntity<Object> makeJsonResponseHeader(boolean isValid, String alertMsg, Object obj, Object extObj, Object extObj2) {
		return makeJsonResponseHeader(isValid, alertMsg, obj, extObj, extObj2, null);
	}
	
	protected static ResponseEntity<Object> makeJsonResponseHeader(boolean isValid, String alertMsg, Object obj, Object extObj, Object extObj2, Object extObj3) {
		HttpHeaders responseHeaders = new HttpHeaders();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("isValid", isValid ? "true" : "false");
		
		if (!isEmpty(alertMsg)) {
			resultMap.put("validMsg", alertMsg);
		}
		
		if (obj != null) {
			resultMap.put("resultData", obj);
		}
		
		if (extObj != null) {
			resultMap.put("externalData", extObj);
		}
		
		if (extObj2 != null){
			resultMap.put("externalData2", extObj2);
		}
		
		if (extObj3 != null) {
			resultMap.put("externalData3", extObj3);
		}
		
		responseHeaders.set("Content-Type", MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8");
		
		return new ResponseEntity<Object>(resultMap, responseHeaders, HttpStatus.OK); 
	}
	
	protected static Map<String, Object> getGridPagerMap(ComBean vo) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("page", vo.getPage());
		map.put("total", vo.getTotBlockSize());
		map.put("records", vo.getTotListSize());
		
		return map;
	}
	
	
	protected T2CoValidationResult validate(HttpServletRequest req) {
		return validate(req, null, null, null, null);
	}

	protected T2CoValidationResult validateWithAppendix(HttpServletRequest req, String appendixKey, Object appendixObj) {
		return validate(req, null, null, appendixKey, appendixObj);
	}
	
	protected T2CoValidationResult validate(HttpServletRequest req, String ignore) {
		return validate(req, ignore, null, null, null);
	}
	
	protected T2CoValidationResult validate(HttpServletRequest req, String ignore, String hint, String appendixKey, Object appendixObj) {
		T2CoAdminValidator validator = new T2CoAdminValidator();
		
		if (!isEmpty(ignore)) {
			validator.setIgnore(ignore);
		}
		
		if (!isEmpty(hint)) {
			validator.setHint(hint);
		}
		
		if (!isEmpty(appendixKey) && appendixObj != null) {
			validator.setAppendix(appendixKey, appendixObj);
		}
		
		return validator.validateRequest(req);
	}
	
	protected T2CoValidationResult validate(Object sourceVO) {
		T2CoAdminValidator validator = new T2CoAdminValidator();
		return validator.validateObject(sourceVO);
	}
	
	protected static Locale getLocale() {
		return LocaleContextHolder.getLocale();
	}
	
	protected static String getMessage(String code) {
		return messageSource.getMessage(code, null, getLocale());
	}
	
	protected static String getMessage(String code, Object[] args) {
		return messageSource.getMessage(code, args, getLocale());
	}
	
	protected static String getEnvMessage(String code) {
		return appEnv.getProperty(code);
	}

	protected static WebApplicationContext getWebappContext() {
		return applicationContext;
	}
	
	public static ResponseEntity<FileSystemResource> excelToResponseEntity(String excelPath ,String downFileName) throws IOException{
		
		String fullLogiPath = excelPath;
		String encodedFilename = URLEncoder.encode(downFileName,"UTF-8").replace("+", "%20");
		String contentType = CommonFunction.getMsApplicationContentType(downFileName);
		
		ResponseEntity<FileSystemResource> responseEntity = null;
		java.io.File file = new java.io.File(fullLogiPath);
		file.setLastModified(new Date().getTime());
	    FileSystemResource fileSystemResource = new FileSystemResource(file);
	    
	    HttpHeaders responseHeaders = new HttpHeaders();
	    
	    if (!isEmpty(contentType)) {
	    	responseHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
	    }
	    
	    responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodedFilename + ";filename*= UTF-8''" + encodedFilename);
	    responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Long.toString(fileSystemResource.contentLength()));
	    
	    responseEntity = new ResponseEntity<FileSystemResource>(fileSystemResource, responseHeaders, HttpStatus.OK);
		
	    return responseEntity;
	}
	
	public static ResponseEntity<FileSystemResource> noticeToResponseEntity(String filePath ,String downFileName) throws IOException{
	
		String fullLogiPath = filePath;
		String encodedFilename = URLEncoder.encode(downFileName,"UTF-8").replace("+", "%20");
	    
		ResponseEntity<FileSystemResource> responseEntity = null;
		java.io.File file = new java.io.File(fullLogiPath);
	    FileSystemResource fileSystemResource = new FileSystemResource(file);
	    
	    HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
	    responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodedFilename + ";filename*= UTF-8''" + encodedFilename);
	    responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Long.toString(fileSystemResource.contentLength()));
	    
	    responseEntity = new ResponseEntity<FileSystemResource>(fileSystemResource, responseHeaders, HttpStatus.OK);
	    
	    return responseEntity;
	}

	public static ResponseEntity<FileSystemResource> reviewReportToResponseEntity(String filePath, String downFileName) throws IOException {
		return noticeToResponseEntity(filePath, downFileName);
	}
	
	public static String avoidNull(String s) {
		return avoidNull(s, "");
	}
	
	public static String avoidNull(String s, String rep) {
		return isEmpty(s) ? rep : s;
	}
	
	
	public static boolean putSessionObject(String key, Object obj) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		
		if (request != null) {
			HttpSession session = request.getSession();
			
			if (session != null) {
				session.setAttribute(key, obj);
				
				return true;
			}
		}
		
		return false;
	}
	
	public static String getSessionString(String key) {
		return (String) getSessionObject(key, false);
	}
	
	public static Object getSessionObject(String key) {
		return getSessionObject(key, false);
	}
	
	public static Object getSessionObject(String key, boolean oneTimeFlag) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		
		if (request != null) {
			HttpSession session = request.getSession();
			
			if (session != null) {
				Object sessionObj = session.getAttribute(key);
				
				if (oneTimeFlag) {
					try {
						session.removeAttribute(key);
					} catch (Exception e) {
					}
				}
				
				return sessionObj;
			}
		}
		
		return null;
	}
	
	public static void deleteSession(String key) {
		
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			
			if (request != null) {
				HttpSession session = request.getSession();
				
				if (session != null) {
					session.removeAttribute(key);
				}
			}			
		} catch (Exception e) {
		}
	}
	
	public static String httpCodePrint(int code){
		String res = Integer.toString(code);
		
		switch(code){
			case HttpURLConnection.HTTP_ACCEPTED: 		  res = "HTTP_ACCEPTED"; 			break;
			case HttpURLConnection.HTTP_BAD_GATEWAY: 	  res = "HTTP_BAD_GATEWAY"; 		break;
			case HttpURLConnection.HTTP_BAD_METHOD: 	  res = "HTTP_BAD_METHOD"; 			break;
			case HttpURLConnection.HTTP_BAD_REQUEST: 	  res = "HTTP_BAD_REQUEST"; 		break;
			case HttpURLConnection.HTTP_CLIENT_TIMEOUT:   res = "HTTP_CLIENT_TIMEOUT"; 		break;
			case HttpURLConnection.HTTP_CONFLICT: 		  res = "HTTP_CONFLICT"; 			break;
			case HttpURLConnection.HTTP_CREATED: 		  res = "HTTP_CREATED"; 			break;
			case HttpURLConnection.HTTP_ENTITY_TOO_LARGE: res = "HTTP_ENTITY_TOO_LARGE"; 	break;
			case HttpURLConnection.HTTP_FORBIDDEN: 		  res = "HTTP_FORBIDDEN"; 			break;
			case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:  res = "HTTP_GATEWAY_TIMEOUT"; 	break;
			case HttpURLConnection.HTTP_GONE: 			  res = "HTTP_GONE"; 				break;
			case HttpURLConnection.HTTP_INTERNAL_ERROR:   res = "HTTP_INTERNAL_ERROR"; 		break;
			case HttpURLConnection.HTTP_LENGTH_REQUIRED:  res = "HTTP_LENGTH_REQUIRED"; 	break;
			case HttpURLConnection.HTTP_MOVED_PERM: 	  res = "HTTP_MOVED_PERM"; 			break;
			case HttpURLConnection.HTTP_MOVED_TEMP: 	  res = "HTTP_MOVED_TEMP"; 			break;
			case HttpURLConnection.HTTP_MULT_CHOICE: 	  res = "HTTP_MULT_CHOICE"; 		break;
			case HttpURLConnection.HTTP_NO_CONTENT: 	  res = "HTTP_NO_CONTENT"; 			break;
			case HttpURLConnection.HTTP_NOT_ACCEPTABLE:   res = "HTTP_NOT_ACCEPTABLE"; 		break;
			case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:res = "HTTP_NOT_AUTHORITATIVE"; 	break;
			case HttpURLConnection.HTTP_NOT_FOUND: 		  res = "HTTP_NOT_FOUND"; 			break;
			case HttpURLConnection.HTTP_NOT_IMPLEMENTED:  res = "HTTP_NOT_IMPLEMENTED"; 	break;
			case HttpURLConnection.HTTP_NOT_MODIFIED: 	  res = "HTTP_NOT_MODIFIED"; 		break;
			case HttpURLConnection.HTTP_OK: 			  res = "HTTP_OK"; 					break;
			case HttpURLConnection.HTTP_PARTIAL: 		  res = "HTTP_PARTIAL"; 			break;
			case HttpURLConnection.HTTP_PAYMENT_REQUIRED: res = "HTTP_PAYMENT_REQUIRED"; 	break;
			case HttpURLConnection.HTTP_PRECON_FAILED: 	  res = "HTTP_PRECON_FAILED"; 		break;
			case HttpURLConnection.HTTP_PROXY_AUTH: 	  res = "HTTP_PROXY_AUTH"; 			break;
			case HttpURLConnection.HTTP_REQ_TOO_LONG: 	  res = "HTTP_REQ_TOO_LONG"; 		break;
			case HttpURLConnection.HTTP_RESET: 			  res = "HTTP_RESET"; 				break;
			case HttpURLConnection.HTTP_SEE_OTHER: 		  res = "HTTP_SEE_OTHER"; 			break;
			case HttpURLConnection.HTTP_UNAUTHORIZED: 	  res = "HTTP_UNAUTHORIZED"; 		break;
			case HttpURLConnection.HTTP_UNAVAILABLE: 	  res = "HTTP_UNAVAILABLE"; 		break;
			case HttpURLConnection.HTTP_UNSUPPORTED_TYPE: res = "HTTP_UNSUPPORTED_TYPE"; 	break;
			case HttpURLConnection.HTTP_USE_PROXY: 		  res = "HTTP_USE_PROXY"; 			break;
			case HttpURLConnection.HTTP_VERSION: 		  res = "HTTP_VERSION"; 			break;
			default: break;	
		}
		
		return res;
	}
}
