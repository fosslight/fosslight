///*
// * Copyright (c) 2021 LG Electronics Inc.
// * SPDX-License-Identifier: AGPL-3.0-only 
// */
//
//package oss.fosslight.config;
//
//import javax.sql.DataSource;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//
//import oss.fosslight.common.CommonFunction;
//
//@Configuration
//public class DatasourceConfig {
//	@Autowired Environment env;
//	
//	@Bean
//	public DataSource getDataSource() {
//		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
//		dataSourceBuilder.driverClassName(env.getProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver"));
//		String jdbcUrl = CommonFunction.makeJdbcUrl(env.getProperty("spring.datasource.url", "127.0.0.1:3306/fosslight"));
//		
//		dataSourceBuilder.url(jdbcUrl);
//		dataSourceBuilder.username(env.getProperty("spring.datasource.username", "fosslight"));
//		dataSourceBuilder.password(env.getProperty("spring.datasource.password", "fosslight"));
//		return dataSourceBuilder.build();
//	}
//	
//}
