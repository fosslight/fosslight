/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.*;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.repository.ApiPartnerMapper;
import oss.fosslight.service.ApiPartnerService;
import oss.fosslight.service.ApiVulnerabilityService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.YamlUtil;

@Service
@Slf4j
public class ApiPartnerServiceImpl implements ApiPartnerService {
	@Autowired ApiPartnerMapper apiPartnerMapper;
	@Autowired ProjectService projectService;
	@Autowired ApiVulnerabilityService apiVulnerabilityService;
	
	@Override
	public Map<String, Object> getPartnerMasterList(Map<String, Object> paramMap){
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		int partnerCnt = apiPartnerMapper.selectPartnerMasterCount(paramMap);
		
		if (partnerCnt > 0) {
			list = apiPartnerMapper.selectPartnerMaster(paramMap);
		}
		
		result.put("content", list);
		result.put("record", partnerCnt);
		
		return result;
	}

	@Override
	public boolean existPartnertCnt(Map<String, Object> paramMap) {
		return apiPartnerMapper.existPartnertCnt(paramMap) > 0 ? true : false;
	}

	@Override
	public boolean existLdapUserToEmail(String email) {
		boolean ldapCheckFlag = false;
		try {
			LdapTemplate ldapTemplate = new LdapTemplate(makeLdapContextSource());
			ldapTemplate.afterPropertiesSet();
		
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<String[]> result = ldapTemplate.search(query().where("mail").is(email), new AttributesMapper() {
				public Object mapFromAttributes(Attributes attrs) throws NamingException {
					return new String[]{(String)attrs.get("mail").get(), (String)attrs.get("displayname").get()};
				}
			});
			
			if (result != null && !result.isEmpty()) ldapCheckFlag = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return ldapCheckFlag;
	}
	
	private LdapContextSource makeLdapContextSource() {
		String LDAP_SEARCH_DOMAIN = CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN);
		String LDAP_SEARCH_ID = CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_ID);
		String LDAP_SEARCH_PW = CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_PW);
		
		LdapContextSource contextSource = new LdapContextSource();
		try {
			contextSource.setUrl(CoConstDef.AD_LDAP_LOGIN.LDAP_SERVER_URL.getValue());
			contextSource.setBase("OU=LGE Users, DC=LGE, DC=NET");
			contextSource.setUserDn(LDAP_SEARCH_ID+LDAP_SEARCH_DOMAIN);
			contextSource.setPassword(LDAP_SEARCH_PW);
			CommonFunction.setSslWithCert();
			contextSource.afterPropertiesSet();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return contextSource;
	}

	@Override
	public boolean existsWatcherByEmail(String partnerId, String email) {
		return apiPartnerMapper.existsWatcherByEmail(partnerId, email) > 0 ? false : true;
	}

	@Override
	public void insertWatcher(Map<String, Object> paramMap) {
		apiPartnerMapper.insertWatcher(paramMap);
	}

	@Override
	public Map<String, Object> getExportJson(String partnerId) {

		ProjectIdentification _param = new ProjectIdentification();
		_param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		_param.setReferenceId(partnerId);
		String type = CoConstDef.CD_DTL_COMPONENT_ID_PARTNER;
		Map<String, Object> map = projectService.getIdentificationGridList(_param);

		List<ProjectIdentification> list = (List<ProjectIdentification>) map.get("mainData");
		LinkedHashMap<String, List<Map<String, Object>>> resultYamlFormat = YamlUtil.checkYamlFormat(projectService.setMergeGridData(list), type);

		Map<String, Object> resultMap = new HashMap<String, Object>();

		// Integrate Yaml Format, Vulnerability
		for (String resultYamlFormatKey: resultYamlFormat.keySet()){
			List<Map<String, Object>> yamlFormatList = resultYamlFormat.get(resultYamlFormatKey);
			for (Map<String, Object> yamlFormatMap: yamlFormatList) {
				String version = (String) yamlFormatMap.get("version");
				List<Map<String, Object>> maxScoreNvdInfoList = apiVulnerabilityService.selectMaxScoreNvdInfo(resultYamlFormatKey, version);

				if (!maxScoreNvdInfoList.isEmpty()) {
					Map<String, Object> maxScoreNvdInfoMap = apiVulnerabilityService.selectMaxScoreNvdInfo(resultYamlFormatKey, version).get(0);
					yamlFormatMap.put("Vulnerability", maxScoreNvdInfoMap.get("cvssScore"));
				}
			}
			resultMap.put(resultYamlFormatKey, yamlFormatList);
		}
		return resultMap;
	}
}