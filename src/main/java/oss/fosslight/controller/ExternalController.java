/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.Url.EXTERNAL;

@Controller
public class ExternalController extends CoTopComponent {
	
	@GetMapping(value=EXTERNAL.PAGE, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res){
		return EXTERNAL.PAGE_JSP;
	}
	
}
