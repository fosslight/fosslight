/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {
    private static final String APPLICATION_JSON = "application/json";
    private static final String REFERENCE = "authorization header value";

    @Bean
    GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomizer(c -> c.info(apiInfo()))
                .consumesToMatch(APPLICATION_JSON)
                .group("v1")
                .packagesToScan(AppConstBean.APP_COMPONENT_SCAN_PACKAGE + ".api.controller")
                .pathsToMatch("/api/v1/**")
                .producesToMatch(APPLICATION_JSON)
                .build();
    }

    @Bean
    GroupedOpenApi apiV2() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomizer(c ->
                        c.info(apiInfo())
                                .addSecurityItem(new SecurityRequirement().addList(REFERENCE))
                                .components(new Components().addSecuritySchemes(REFERENCE, apiKey())))
                .consumesToMatch(APPLICATION_JSON)
                .group("v2")
                .packagesToScan(AppConstBean.APP_COMPONENT_SCAN_PACKAGE + ".api.controller")
                .pathsToMatch("/api/v2/**")
                .producesToMatch(APPLICATION_JSON)
                .build();
    }

    private SecurityScheme apiKey() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(HttpHeaders.AUTHORIZATION);
    }

    private Info apiInfo() {
        return new Info()
                .title("FOSSLight Hub Open API")
                .description("")
                .version("1");
    }
}
