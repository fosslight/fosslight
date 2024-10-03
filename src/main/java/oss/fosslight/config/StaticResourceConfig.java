/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SuppressWarnings("deprecation")
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
	
	@Autowired Environment env;
	private String rootPath;
	private String publishPath;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		rootPath = env.getProperty("root.dir");
		publishPath = env.getProperty("internal.url.dir.path");
		registry.addResourceHandler(publishPath+"/**").addResourceLocations("file:"+rootPath+publishPath+"/");
	}
}
