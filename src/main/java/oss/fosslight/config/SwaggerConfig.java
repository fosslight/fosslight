/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = new HashSet<String>(Arrays.asList("application/json"));
	
    @Bean
    public Docket swaggerApiV1() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(swaggerInfo())
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES).produces(DEFAULT_PRODUCES_AND_CONSUMES).select()
                .apis(RequestHandlerSelectors.basePackage(AppConstBean.APP_COMPONENT_SCAN_PACKAGE+".api.controller"))
                .paths(PathSelectors.ant("/api/v1/**"))
                .build()
                .groupName("v1")
                .useDefaultResponseMessages(false); // 기본으로 세팅되는 200,401,403,404 메시지를 표시 하지 않음
    }

    @Bean
    public Docket swaggerApiV2() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(swaggerInfo())
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES).produces(DEFAULT_PRODUCES_AND_CONSUMES).select()
                .apis(RequestHandlerSelectors.basePackage(AppConstBean.APP_COMPONENT_SCAN_PACKAGE+".api.controller"))
                .paths(PathSelectors.ant("/api/v2/**"))
                .build()
                .groupName("v2");
    }
    
    private ApiInfo swaggerInfo() {
        return new ApiInfoBuilder()
        		.title("FOSSLight Hub Open API")
                .description("") // 시스템설졍이 필요한 경우 기입
                .version("1")
                .build();
    }
}
