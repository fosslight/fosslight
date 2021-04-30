/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import oss.fosslight.repository.CodeManagerMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.OssService;
import oss.fosslight.service.T2UserService;

@Component
public class CoPostConstruct {
	@Autowired T2UserService t2UserService;
	@Autowired OssService ossService;
	
	@Autowired CodeManagerMapper codeManagerMapper;
	@Autowired LicenseMapper licenseMapper;
	@Autowired OssMapper ossMapper;
	@Autowired ProjectMapper projectMapper;
	
	@PostConstruct
	public void initPostConstruct() {
		try {
			CommonFunction.setT2UserService(t2UserService);
			CommonFunction.setOssService(ossService);
			CoCodeManager.setCodeManagerMapper(codeManagerMapper);
			CoCodeManager.setLicenseMapper(licenseMapper);
			CoCodeManager.setOssMapper(ossMapper);
			CoCodeManager.getInstance().init();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
