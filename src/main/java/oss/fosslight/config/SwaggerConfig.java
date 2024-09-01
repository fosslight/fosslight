/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import java.util.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("application/json")));
    private static final String REFERENCE = "authorization header value";

    @Bean
    Docket swaggerApiV1() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(swaggerInfo())
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES).produces(DEFAULT_PRODUCES_AND_CONSUMES).select()
                .apis(RequestHandlerSelectors.basePackage(AppConstBean.APP_COMPONENT_SCAN_PACKAGE+".api.controller"))
                .paths(PathSelectors.ant("/api/v1/**"))
                .build()
                .groupName("v1")
                .useDefaultResponseMessages(false); // 기본으로 세팅되는 200,401,403,404 메시지를 표시 하지 않음
    }

    @Bean
    Docket swaggerApiV2() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(swaggerInfo())
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES).produces(DEFAULT_PRODUCES_AND_CONSUMES).select()
                .apis(RequestHandlerSelectors.basePackage(AppConstBean.APP_COMPONENT_SCAN_PACKAGE+".api.controller"))
                .paths(PathSelectors.ant("/api/v2/**"))
                .build()
                .groupName("v2")
                .securityContexts(List.of(securityContext()))
                .securitySchemes(List.of(securityScheme()));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(securityReferences())
                .operationSelector(operationContext -> true)
                .build();
    }

    private List<SecurityReference> securityReferences() {
        AuthorizationScope[] authorizationScope = new AuthorizationScope[1];
        authorizationScope[0] = new AuthorizationScope("global", "accessEverything");
        return List.of(new SecurityReference(REFERENCE, authorizationScope));
    }

    private ApiKey securityScheme() {
        String targetHeader = "Authorization";
        return new ApiKey(REFERENCE, targetHeader, "header");
    }

    
    private ApiInfo swaggerInfo() {
        return new ApiInfoBuilder()
        		.title("FOSSLight Hub Open API")
                .description("") // 시스템설졍이 필요한 경우 기입
                .version("1")
                .build();
    }
}
