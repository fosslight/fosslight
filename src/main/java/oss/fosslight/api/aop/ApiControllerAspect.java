///*
// * Copyright (c) 2021 LG Electronics Inc.
// * SPDX-License-Identifier: AGPL-3.0-only 
// */
//
//package oss.fosslight.api.aop;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import com.google.common.base.Joiner;
//
//import lombok.extern.slf4j.Slf4j;
//import oss.fosslight.common.CoConstDef;
//import oss.fosslight.config.AppConstBean;
//
//@Aspect
//@Component
//@Slf4j
//public class ApiControllerAspect {
//	
//	private final String convertParamJson = CoConstDef.FLAG_YES;
//	
//	@Around("execution(* "+ AppConstBean.APP_COMPONENT_SCAN_PACKAGE
//			+ ".api.controller..*.*(..)) && @annotation(org.springframework.web.bind.annotation.GetMapping)")
//	public Object getLogging(ProceedingJoinPoint pjp) throws Throwable {
//
//		HttpServletRequest request = // 5
//				((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//
//		String params = "";
//		Map<String, String[]> paramMap = request.getParameterMap();
//		
//		if (!paramMap.isEmpty()) {
//			params = " [" + paramMapToString(paramMap) + "]";
//		}
//		
//		long start = System.currentTimeMillis();
//		
//		try {
//			return pjp.proceed(pjp.getArgs()); // 6
//		} finally {
//			long end = System.currentTimeMillis();
//			log.info("IP: {}, method: {}, token: {} {}ms, URL: {}, params: {}", request.getRemoteHost(),request.getMethod(), request.getHeader("_token"),
//					end - start, request.getRequestURI(), params);
//		}
//	}
//	
//	@Around("execution(* "+ AppConstBean.APP_COMPONENT_SCAN_PACKAGE
//			+ ".api.controller..*.*(..)) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
//	public Object postLogging(ProceedingJoinPoint pjp) throws Throwable {
//
//		HttpServletRequest request = // 5
//				((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//
//		Map<String, String[]> paramMap = request.getParameterMap();
//		String params = "";
//		
//		if (!paramMap.isEmpty()) {
//			params = " [" + paramMapToString(paramMap) + "]";
//		}
//		
//		long start = System.currentTimeMillis();
//		
//		try {
//			return pjp.proceed(pjp.getArgs()); // 6
//		} finally {
//			long end = System.currentTimeMillis();
//			log.info("IP: {}, method: {}, token: {} {}ms, URL: {}, params: {}", request.getRemoteHost(),request.getMethod(), request.getHeader("_token"),
//					end - start, request.getRequestURI(), params);
//		}
//	}
//	
//	private String paramMapToString(Map<String, String[]> paramMap) {
//
//		if (CoConstDef.FLAG_YES.equals(convertParamJson)) {
//			JSONObject jsonObject = new JSONObject();
//			List<String> ignoreParamList = new ArrayList<>();
//			
//			for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
//				String key = entry.getKey();
//				
//				if (ignoreParamList.contains(key)) {
//					continue;
//				}
//				
//				String[] value = entry.getValue();
//				
//				try {
//					jsonObject.put(key, Arrays.toString(value));
//				} catch (JSONException e) {
//					//e.printStackTrace();
//				}
//			}
//
//			return jsonObject.toString();			
//		} else {
//			return paramMap.entrySet().stream()
//					.map(entry -> String.format("%s -> (%s)", entry.getKey(), Joiner.on(",").join(entry.getValue())))
//					.collect(Collectors.joining(", "));
//		}
//	}
//}
