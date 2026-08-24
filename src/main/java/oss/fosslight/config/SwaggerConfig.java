/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SwaggerConfig implements WebMvcConfigurer {
    private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("application/json")));
    private static final String REFERENCE = "authorization header value";

    /** Swagger group name exposed to every user (external + internal). */
    private static final String GROUP_V2 = "v2";
    /** Swagger group name exposed only to authenticated ROLE_ADMIN users. */
    private static final String GROUP_V2_INTERNAL = "v2-internal";

    private final JwtTokenProvider jwtTokenProvider;
    private final SwaggerInternalGroupInterceptor swaggerInternalGroupInterceptor;

    /**
     * Public API group. Excludes any endpoint/controller annotated with
     * {@link oss.fosslight.api.annotation.InternalApi @InternalApi} so that internal-only
     * APIs are hidden from external users.
     */
    @Bean
    Docket swaggerApiV2() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(swaggerInfo())
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES).produces(DEFAULT_PRODUCES_AND_CONSUMES).select()
                .apis(RequestHandlerSelectors.basePackage(AppConstBean.APP_COMPONENT_SCAN_PACKAGE + ".api.controller")
                        .and(notInternalApi()))
                .paths(PathSelectors.ant("/api/v2/**"))
                .build()
                .groupName(GROUP_V2)
                .securityContexts(List.of(securityContext()))
                .securitySchemes(List.of(securityScheme()));
    }

    /**
     * Internal API group. Includes only endpoints/controllers annotated with
     * {@link oss.fosslight.api.annotation.InternalApi @InternalApi}. The group itself is
     * only listed in {@code /swagger-resources} when the caller is an authenticated admin
     * (see {@link #swaggerResourcesProvider()}), and the underlying {@code /v2/api-docs}
     * document is additionally protected by {@link SwaggerInternalGroupInterceptor}.
     */
    @Bean
    Docket swaggerApiV2Internal() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(swaggerInfo())
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES).produces(DEFAULT_PRODUCES_AND_CONSUMES).select()
                .apis(RequestHandlerSelectors.basePackage(AppConstBean.APP_COMPONENT_SCAN_PACKAGE + ".api.controller")
                        .and(internalApi()))
                .paths(PathSelectors.ant("/api/v2/**"))
                .build()
                .groupName(GROUP_V2_INTERNAL)
                .securityContexts(List.of(securityContext()))
                .securitySchemes(List.of(securityScheme()));
    }

    /**
     * Predicate that matches handlers whose declaring class or method is annotated
     * with {@link oss.fosslight.api.annotation.InternalApi @InternalApi}.
     */
    @SuppressWarnings("deprecation") // declaringClass() is deprecated in Springfox 3.0 but still the cleanest API for class-level lookup
    private static java.util.function.Predicate<springfox.documentation.RequestHandler> internalApi() {
        return handler -> {
            if (handler == null) {
                return false;
            }
            // method-level annotation
            if (handler.isAnnotatedWith(oss.fosslight.api.annotation.InternalApi.class)) {
                return true;
            }
            // class-level annotation (fall back)
            return handler.declaringClass() != null
                    && handler.declaringClass().isAnnotationPresent(oss.fosslight.api.annotation.InternalApi.class);
        };
    }

    /** Predicate that is the negation of {@link #internalApi()}. */
    private static java.util.function.Predicate<springfox.documentation.RequestHandler> notInternalApi() {
        return internalApi().negate();
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

    /**
     * Dynamically controls which Swagger groups are listed in {@code /swagger-resources}.
     *
     * <p>The public {@code v2} group is always listed. The {@code v2-internal} group is
     * listed only when the current request carries a valid web-session JWT cookie
     * ({@code X-FOSS-AUTH-TOKEN}) whose authority is {@code ROLE_ADMIN}.
     *
     * <p>This relies on the <em>web login session</em> (cookie), not the API token sent
     * via the {@code Authorization} header, because Swagger UI itself is served from the
     * browser and the cookie is sent automatically on every request to the hub.
     */
    @Bean
    @Primary
    SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> {
            List<SwaggerResource> resources = new ArrayList<>();
            resources.add(swaggerResource(GROUP_V2, "/v2/api-docs?group=" + GROUP_V2));

            if (isCurrentUserAdmin()) {
                resources.add(swaggerResource(GROUP_V2_INTERNAL, "/v2/api-docs?group=" + GROUP_V2_INTERNAL));
            }
            return resources;
        };
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource resource = new SwaggerResource();
        resource.setName(name);
        resource.setLocation(location);
        resource.setSwaggerVersion("2.0");
        return resource;
    }

    /**
     * Returns {@code true} when the current HTTP request has a valid web-session JWT
     * cookie whose authority is {@code ROLE_ADMIN}.
     */
    private boolean isCurrentUserAdmin() {
        try {
            HttpServletRequest req = currentRequest();
            if (req == null) {
                return false;
            }
            String token = jwtTokenProvider.resolveToken(req);
            if (!jwtTokenProvider.validateToken(token)) {
                return false;
            }
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            if (auth == null) {
                return false;
            }
            return auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        } catch (Exception e) {
            log.debug("Failed to resolve admin role for swagger resources", e);
            return false;
        }
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * Registers the interceptor that blocks direct access to the {@code v2-internal}
     * api-docs document for non-admin users (prevents URL-guessing bypass).
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(swaggerInternalGroupInterceptor)
                .addPathPatterns("/v2/api-docs");
    }
}
