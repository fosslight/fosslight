/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.repository.CodeManagerMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.service.OssService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.T2UserService;

@Component
@Slf4j
public class CoPostConstruct {
	@Autowired private T2UserService t2UserService;
	@Autowired private OssService ossService;
	
	@Autowired private CodeManagerMapper codeManagerMapper;
	@Autowired private LicenseMapper licenseMapper;
	@Autowired private OssMapper ossMapper;
	@Autowired private TemplateEngine emailTemplateEngine;
	@Autowired private ProjectService projectService;
	
	@PostConstruct
	public void initPostConstruct() {
		try {
			CommonFunction.setT2UserService(t2UserService);
			CommonFunction.setOssService(ossService);
			CommonFunction.setTemplateEngine(emailTemplateEngine);
			CommonFunction.setProjectService(projectService);
			CoCodeManager.setCodeManagerMapper(codeManagerMapper);
			CoCodeManager.setLicenseMapper(licenseMapper);
			CoCodeManager.setOssMapper(ossMapper);
			CoCodeManager.getInstance().init();
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
