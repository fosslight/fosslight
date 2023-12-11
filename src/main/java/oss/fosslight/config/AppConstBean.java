/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

/** **************************************************************************
 * Web Application의 Config 변수 저장소
 * 
 ************************************************************************** */
public class AppConstBean {
	
	/** **************************************************************************
	 * Common Value
	 ************************************************************************** */
	public static final String APP_CONFIG_PROPERTIES_PATH = "classpath:application.properties";		// Application Properties Path
	public static final String APP_CONFIG_VALIDATION_PROPERTIES = "classpath:messages/validation.properties";
	public static final String APP_COMPONENT_SCAN_PACKAGE = "oss.fosslight";				// Component Scan Package (=Project Package)
	
	/*
	 * Tiles의 "pageTitle" attribute에 사용되며, "APP_NAME"은 AdviceController에서 @ModelAttribute("pageTitle")로 기본값으로 추가된다. 
	 * 변경을 원할경우 Controller에서 model.addAttribute("pageTitle",StringValue); 에 입력된 값이 설정된다.
	 */
	public static final String APP_NAME = "FOSSLight";
	
	/** **************************************************************************
	 * App Config
	 ************************************************************************** */
	public static final String 	APP_ENCODING = "UTF-8";													// Web Application 인코딩
	
	
	/** **************************************************************************
	 * Web Config
	 ************************************************************************** */
	public static final String 	MESSAGE_SOURCE_DEFAULT_LOCALE = "ko";							// 메시지 소스 디폴트 인코딩
	public static final String 	MESSAGE_SOURCE_DEFAULT_LOCALE_PARAM_NAME = "lang";				// 메시지 소스 변경 파라미터 키
//	public static final String 	RESOURCE_HANDLER = "/**";										// 리소스 URL 맵핑 Path
//	public static final String 	RESOURCE_LOCATIONS = "/WEB-INF/resources/";						// 리소스 물리적 Path
//	
//	
//	public static final String 	VIEW_PREFIX = "/WEB-INF/views/";								// jsp Root Path
//	public static final String 	VIEW_SUFFIX = ".jsp";											// jsp file suffix
	
//	/** **************************************************************************
//	 * Security Config
//	 ************************************************************************** */
	public static final String SECURITY_ROLE_PREFIX = "";
	public static final String SECURITY_ROLE_DEFAULT = "ROLE_USER"; // 사용자 추가시 T2_AUTHORITIES에 기본으로 들어가는 역할
//	
//	public static final String SECURITY_LOGIN_PAGE = "/session/login";
//	public static final String SECURITY_LOGIN_PROCESSING_URL = "/session/login-proc";
//	public static final String SECURITY_LOGOUT_URL = "/session/logout-proc";
//	public static final String SECURITY_LOGOUT_SUCCESS_URL = "/";
//	
//	public static final String SECURITY_USERNAME_PARAMETER = "un";
//	public static final String SECURITY_PASSWORD_PARAMETER = "up";
//	public static final String SECURITY_EMAIL_PARAMETER = "em";
//	
//	public static final String SECURITY_DEFAULT_SUCCESS_URL = "/index";

}
