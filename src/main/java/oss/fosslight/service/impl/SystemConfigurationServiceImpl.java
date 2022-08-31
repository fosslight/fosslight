/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.service.CodeService;
import oss.fosslight.service.SystemConfigurationService;
import oss.fosslight.util.CryptUtil;
import oss.fosslight.util.StringUtil;

@Service
@Slf4j
public class SystemConfigurationServiceImpl extends CoTopComponent implements SystemConfigurationService {
	// Service
	@Autowired CodeService codeService;
	
	// Mapper
	@Autowired CodeMapper codeMapper;
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> saveConfiguration(Map<String, Object> configurationMap) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		String resCd = "10";
		String resMsg = "Success";
		
		// load detail code
		T2CodeDtl systemSettingAuth = new T2CodeDtl(CoConstDef.CD_SYSTEM_SETTING);
		List<T2CodeDtl> systemSettingAuthList = codeMapper.selectCodeDetailList(systemSettingAuth);
		
		T2CodeDtl loginAuth = new T2CodeDtl(CoConstDef.CD_LOGIN_SETTING);
		List<T2CodeDtl> loginAuthList = codeMapper.selectCodeDetailList(loginAuth);
		
		T2CodeDtl smtpAuth = new T2CodeDtl(CoConstDef.CD_SMTP_SETTING);
		List<T2CodeDtl> smtpAuthList = codeMapper.selectCodeDetailList(smtpAuth);

		T2CodeDtl externalServiceAuth = new T2CodeDtl(CoConstDef.CD_EXTERNAL_SERVICE_SETTING);
		List<T2CodeDtl> tokenAuthList = codeMapper.selectCodeDetailList(externalServiceAuth);

		T2CodeDtl externalAnalysisAuth = new T2CodeDtl(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING);
		List<T2CodeDtl> externalAnalysisAuthList = codeMapper.selectCodeDetailList(externalAnalysisAuth);

		T2CodeDtl defaultTab = new T2CodeDtl(CoConstDef.CD_DEFAULT_TAB);
		List<T2CodeDtl> defaultTabList = codeMapper.selectCodeDetailList(defaultTab);
		
		T2CodeDtl projectDetail = new T2CodeDtl(CoConstDef.CD_PROJECT_DETAIL);
		List<T2CodeDtl> projectDetailList = codeMapper.selectCodeDetailList(projectDetail);
		
		// system auth setting
		systemSettingAuthList.stream().map(c -> {
			switch(c.getCdDtlNo()) {
				case CoConstDef.CD_LDAP_USED_FLAG:
					c.setCdDtlExp((String) configurationMap.get("loginFlag"));
					
					break;
				case CoConstDef.CD_SMTP_USED_FLAG:
					c.setCdDtlExp((String) configurationMap.get("smtpFlag"));
					
					break;
				case CoConstDef.CD_EXTERNAL_SERVICE_USED_FLAG:
					c.setCdDtlExp((String) configurationMap.get("externalServiceFlag"));

					break;

				case CoConstDef.CD_EXTERNAL_ANALYSIS_USED_FLAG:
					c.setCdDtlExp((String) configurationMap.get("externalAnalysisFlag"));

					break;

			}
			
			return c;
		}).collect(Collectors.toList());
		codeService.setCodeDetails(systemSettingAuthList, CoConstDef.CD_SYSTEM_SETTING);
		
		// login detail setting
		Map<String, Object> loginDetailMap = (Map<String, Object>) configurationMap.get("loginDetail");
		
		if(loginDetailMap != null) {
			loginAuthList.stream().map(c -> {
				switch (c.getCdDtlNo()) {
					case CoConstDef.CD_LDAP_SERVER_URL:
						c.setCdDtlExp((String) loginDetailMap.get(CoConstDef.CD_LDAP_SERVER_URL));
						
						break;
				}
				
				return c;
			}).collect(Collectors.toList());

			codeService.setCodeDetails(loginAuthList, CoConstDef.CD_LOGIN_SETTING);
		}
		
		// smtp detail setting
		Map<String, Object> smtpDetailMap = (Map<String, Object>) configurationMap.get("smtpDetail");
		
		if(smtpDetailMap != null) {
			smtpAuthList.stream().map(c -> {
				switch(c.getCdDtlNo()) {
					case CoConstDef.CD_SMTP_SERVICE_HOST:
						c.setCdDtlExp((String) smtpDetailMap.get(CoConstDef.CD_SMTP_SERVICE_HOST));
						
						break;
					case CoConstDef.CD_SMTP_EMAIL_ADDRESS:
						c.setCdDtlExp((String) smtpDetailMap.get(CoConstDef.CD_SMTP_EMAIL_ADDRESS));
						
						break;
					case CoConstDef.CD_SMTP_SERVICE_PORT:
						c.setCdDtlExp((String) smtpDetailMap.get(CoConstDef.CD_SMTP_SERVICE_PORT));
						
						break;
					case CoConstDef.CD_SMTP_SERVICE_ENCODING:
						c.setCdDtlExp((String) smtpDetailMap.get(CoConstDef.CD_SMTP_SERVICE_ENCODING));
						
						break;
					case CoConstDef.CD_SMTP_SERVICE_USERNAME:
						c.setCdDtlExp((String) smtpDetailMap.get(CoConstDef.CD_SMTP_SERVICE_USERNAME));
						
						break;
					case CoConstDef.CD_SMTP_SERVICE_PASSWORD:
						String _pw = (String) smtpDetailMap.get(CoConstDef.CD_SMTP_SERVICE_PASSWORD);
						if(!StringUtil.isEmpty(_pw)) {
							String _encPw = null;
							try {
								_encPw = CryptUtil.encryptAES256(_pw, CoConstDef.ENCRYPT_DEFAULT_SALT_KEY);
							} catch (Exception e) {
								log.error(e.getMessage());
							}
							if(!StringUtil.isEmpty(_encPw)) {
								c.setCdDtlExp(_encPw);
							}
						}
						break;
				}
				
				return c;
			}).collect(Collectors.toList());

			codeService.setCodeDetails(smtpAuthList, CoConstDef.CD_SMTP_SETTING);
		}

		// External Service setting
		Map<String, Object> tokenDetailMap = (Map<String, Object>) configurationMap.get("externalServiceDetail");

		if(tokenDetailMap != null) {
			tokenAuthList.stream().map(c -> {
				switch (c.getCdDtlNo()) {
					case CoConstDef.CD_DTL_GITHUB_TOKEN:
						String token = (String) tokenDetailMap.get(CoConstDef.CD_DTL_GITHUB_TOKEN);
						if(!token.isEmpty()) {
							c.setCdDtlExp(token);
						}
						break;
				}

				return c;
			}).collect(Collectors.toList());

			codeService.setCodeDetails(tokenAuthList, CoConstDef.CD_EXTERNAL_SERVICE_SETTING);
		}

		// External Analysis detail setting
		Map<String, Object> externalAnalysisDetailMap = (Map<String, Object>) configurationMap.get("externalAnalysisDetail");

		if(externalAnalysisDetailMap != null) {
			externalAnalysisAuthList.stream().map(c -> {
				switch(c.getCdDtlNo()) {

					case CoConstDef.CD_DTL_FL_SCANNER_URL:
						c.setCdDtlExp((String) externalAnalysisDetailMap.get(CoConstDef.CD_DTL_FL_SCANNER_URL));

						break;

					case CoConstDef.CD_DTL_ADMIN_TOKEN:
						c.setCdDtlExp((String) externalAnalysisDetailMap.get(CoConstDef.CD_DTL_ADMIN_TOKEN));

						break;
				}

				return c;
			}).collect(Collectors.toList());

			codeService.setCodeDetails(externalAnalysisAuthList, CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING);
		}

		
		// default tab Setting
		defaultTabList.stream().map(c -> {
			switch(c.getCdDtlNo()) {
				case CoConstDef.CD_MENU_DASHBOARD:
					c.setUseYn(CommonFunction.emptyCheckProperty("menu.dashboard.use.flag", CoConstDef.FLAG_NO));
					
					break;
				case CoConstDef.CD_MENU_PROJECT_LIST:
					c.setUseYn(CommonFunction.emptyCheckProperty("menu.project.use.flag", CoConstDef.FLAG_NO));
					
					break;
				case CoConstDef.CD_MENU_PARTNER_LIST:
					c.setUseYn(CommonFunction.emptyCheckProperty("menu.partner.use.flag", CoConstDef.FLAG_NO));
					
					break;
				case CoConstDef.CD_MENU_BAT_LIST:
					c.setUseYn(CommonFunction.emptyCheckProperty("menu.bat.use.flag", CoConstDef.FLAG_NO));
					
					break;
				case CoConstDef.CD_MENU_BINARY_DB:
					c.setUseYn(CommonFunction.emptyCheckProperty("menu.binarydb.use.flag", CoConstDef.FLAG_NO));
					
					break;
			}
			
			return c;
		}).collect(Collectors.toList());
		
		codeService.setCodeDetails(defaultTabList, CoConstDef.CD_DEFAULT_TAB);
		
		Map<String, Object> projectDetailMap = (Map<String, Object>) configurationMap.get("projectDetail");
		
		if(projectDetailMap != null) {
			projectDetailList.stream().map(c -> {
				switch(c.getCdDtlNo()) {
					case CoConstDef.CD_AUTO_ANALYSIS_FLAG:
						c.setCdDtlExp((String) projectDetailMap.get("autoAnalysisFlag"));
						
						break;
					case CoConstDef.CD_NOTICE_FLAG:
						c.setCdDtlExp(CoConstDef.FLAG_YES);
						configurationMap.put("noticeDetailCd", c.getCdSubNo());
						
						break;
				}
				
				return c;
			}).collect(Collectors.toList());
			
			codeService.setCodeDetails(projectDetailList, CoConstDef.CD_PROJECT_DETAIL);
		}
		
		String noticeDetailCd = (String) configurationMap.get("noticeDetailCd");
		T2CodeDtl noticeDetail = new T2CodeDtl(noticeDetailCd);
		List<T2CodeDtl> noticeDetailList = codeMapper.selectCodeDetailList(noticeDetail);
		
		Map<String, Object> noticeDetailMap = (Map<String, Object>) configurationMap.get("noticeDetail");
		
		if(noticeDetailMap != null) {
			noticeDetailList.stream().map(c -> {
				switch(c.getCdDtlNo()) {
					case CoConstDef.CD_DTL_NOTICE_HTML:
						c.setCdDtlExp(CoConstDef.FLAG_YES);
						
						break;
					case CoConstDef.CD_DTL_NOTICE_TEXT:
						c.setCdDtlExp((String) noticeDetailMap.get(CoConstDef.CD_DTL_NOTICE_TEXT));
						
						break;
					case CoConstDef.CD_DTL_NOTICE_SPDX:
						c.setCdDtlExp(CoConstDef.FLAG_YES);
						
						break;
				}
				
				return c;
			}).collect(Collectors.toList());
			
			codeService.setCodeDetails(noticeDetailList, noticeDetailCd);
		}
		
		Map<String, Object> externalDetailMap = (Map<String, Object>) configurationMap.get("externalDetail");
		List<T2CodeDtl> externalList = new ArrayList<T2CodeDtl>();
		
		if(externalDetailMap != null) {
			int seq = 1;
			
			for(String key : externalDetailMap.keySet()) {
				T2CodeDtl externalItem = new T2CodeDtl();
				Map<String, Object> value = (Map<String, Object>) externalDetailMap.get((String) key);
				
				externalItem.setCdNo(CoConstDef.CD_EXTERNAL_LINK);
				externalItem.setCdDtlNo((String) key);
				externalItem.setCdDtlNm((String) value.get("urlKey"));
				externalItem.setCdSubNo("");
				externalItem.setCdDtlExp((String) value.get("urlValue"));
				externalItem.setCdOrder(Integer.toString(seq++));
				externalItem.setUseYn(CoConstDef.FLAG_YES);
				
				externalList.add(externalItem);
			}
			
			codeService.setCodeDetails(externalList, CoConstDef.CD_EXTERNAL_LINK);
		}
		
		CoCodeManager.getInstance().refreshCodes();
		
		result.put("resCd", resCd);
		result.put("resMsg", resMsg);
		
		return result;
	}
}