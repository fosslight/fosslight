///*
// * Copyright (c) 2021 LG Electronics Inc.
// * SPDX-License-Identifier: AGPL-3.0-only 
// */
//
//package oss.fosslight.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.view.UrlBasedViewResolver;
//import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
//import org.springframework.web.servlet.view.tiles3.TilesView;
//
//import oss.fosslight.common.CoConstDef;
//
//@Configuration
//public class TilesConfig {
//	
////	@Bean
////	public TilesConfigurer tilesConfigurer() {
////		final TilesConfigurer configurer = new TilesConfigurer();
////
////		configurer.setDefinitions( CoConstDef.TILES_LAYOUT_XML_PATH );		// tiles 설정 파일이 있는 위치
////		configurer.setCheckRefresh( CoConstDef.REFRESH_JSP_ON_RUNTIME );	// tiles 설정 파일이 runtime 중에 변경될 경우 적용 여부
////
////		return configurer;
////	}
//	
////	@Bean
////	public UrlBasedViewResolver urlViewResolver() {
////		UrlBasedViewResolver tilesViewResolver = new UrlBasedViewResolver();
////		tilesViewResolver.setViewClass(TilesView.class);
////		
////		return tilesViewResolver;
////	}
//}
