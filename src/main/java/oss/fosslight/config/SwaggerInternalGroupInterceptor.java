/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Blocks direct access to the {@code v2-internal} Swagger document.
 *
 * <p>The {@code /swagger-resources} endpoint only lists the {@code v2-internal} group for
 * authenticated admins (see {@link SwaggerConfig#swaggerResourcesProvider()}), but a user
 * who knows the URL could still request {@code /v2/api-docs?group=v2-internal} directly.
 * This interceptor rejects such requests with HTTP 403 unless the caller is an
 * authenticated {@code ROLE_ADMIN} user (verified via the web-session JWT cookie).
 *
 * <p>Only requests whose {@code group} parameter equals {@code v2-internal} are inspected;
 * all other {@code /v2/api-docs} requests pass through unchanged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerInternalGroupInterceptor implements HandlerInterceptor {

    private static final String INTERNAL_GROUP = "v2-internal";
    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!INTERNAL_GROUP.equals(request.getParameter("group"))) {
            return true;
        }

        String token = jwtTokenProvider.resolveToken(request);
        if (!jwtTokenProvider.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Authentication auth = jwtTokenProvider.getAuthentication(token);
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> ADMIN_AUTHORITY.equals(a.getAuthority()));

        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
