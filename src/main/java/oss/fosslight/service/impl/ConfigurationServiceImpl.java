/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import oss.fosslight.domain.Configuration;
import oss.fosslight.domain.T2Users;
import oss.fosslight.repository.ConfigurationMapper;
import oss.fosslight.service.ConfigurationService;
import oss.fosslight.service.T2UserService;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {
	// Service
	@Autowired T2UserService userService;
	
	// Mapper
	@Autowired ConfigurationMapper configurationMapper;
	
	@Override
	public void updateDefaultSetting(Configuration configuration) {
		HashMap<String, Object> info = new HashMap<String, Object>();
		configurationMapper.updateDefaultLocale(configuration);
		configurationMapper.updateDefaultTab(configuration);
		
		// Security session에 추가 정보(Cusom)를 저장한다(Map형태)
        SecurityContext sec = SecurityContextHolder.getContext();
        AbstractAuthenticationToken auth = (AbstractAuthenticationToken)sec.getAuthentication();
        
        //User Detail
        T2Users user = new T2Users();
        user.setUserId(auth.getName());
        T2Users getUser = userService.getUserAndAuthorities(user);
        
        info.put("sessUserInfo", getUser);
        
        auth.setDetails(info);
	}

//	@Override
//	public void updateDefaultLocale(Configuration configuration) {
//		HashMap<String, Object> info = new HashMap<String, Object>();
//
//		configurationMapper.updateDefaultLocale(configuration);
//
//		// Security session에 추가 정보(Cusom)를 저장한다(Map형태)
//		SecurityContext sec = SecurityContextHolder.getContext();
//		AbstractAuthenticationToken auth = (AbstractAuthenticationToken)sec.getAuthentication();
//
//		//User Detail
//		T2Users user = new T2Users();
//		user.setUserId(auth.getName());
//		T2Users getUser = userService.getUserAndAuthorities(user);
//
//		info.put("sessUserInfo", getUser);
//
//		auth.setDetails(info);
//	}
}
