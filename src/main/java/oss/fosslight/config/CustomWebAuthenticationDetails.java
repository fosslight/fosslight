/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import javax.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class CustomWebAuthenticationDetails extends WebAuthenticationDetails  {
	private static final long serialVersionUID = -3984468376168493070L;
	private final String email;

	public CustomWebAuthenticationDetails(HttpServletRequest request) {
		super(request);
		email = request.getParameter(AppConstBean.SECURITY_EMAIL_PARAMETER);
	}
	
	public String getEmail() {
		return email;
	}
}
