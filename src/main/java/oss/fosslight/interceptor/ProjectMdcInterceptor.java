/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class ProjectMdcInterceptor implements HandlerInterceptor {
	private static final String MDC_PROJECT_ID = "projectId";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String projectId = resolveProjectId(request);

		if (StringUtils.isNotBlank(projectId)) {
			MDC.put(MDC_PROJECT_ID, projectId);
		}

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		MDC.remove(MDC_PROJECT_ID);
	}

	@SuppressWarnings("unchecked")
	private String resolveProjectId(HttpServletRequest request) {
		Object uriVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		if (uriVariables instanceof Map) {
			Map<String, String> pathVariables = (Map<String, String>) uriVariables;
			String fromPath = firstNonBlank(pathVariables.get("id"), pathVariables.get("prjId"), pathVariables.get("projectId"));
			if (StringUtils.isNotBlank(fromPath)) {
				return fromPath;
			}
		}

		return firstNonBlank(request.getParameter("id"), request.getParameter("prjId"), request.getParameter("projectId"));
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}

		return null;
	}
}
