/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.SearchType;
import oss.fosslight.domain.History;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.SearchMapper;
import oss.fosslight.service.LicenseService;
import oss.fosslight.service.SearchService;
import oss.fosslight.util.StringUtil;



@Service
@Slf4j
public class SearchServiceImpl extends CoTopComponent implements SearchService {

    @Autowired LicenseService licenseService;
    @Autowired SearchMapper searchMapper;


    @Override
    public void saveOssSearchFilter(OssMaster ossMaster, String userId) {

        String stringjson = new Gson().toJson(ossMaster);
        searchMapper.upsertSearchFilter(stringjson, userId, SearchType.OSS.getName());

    }


    @Override
    public OssMaster getOssSearchFilter(String userId) {
        String filterString = searchMapper.selectOssSearchFilter(userId);
        if (filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, OssMaster.class);
    }


    @Override
    public void saveProjectSearchFilter(Project project, String userId) {

        String stringjson = new Gson().toJson(project);
        searchMapper.upsertSearchFilter(stringjson, userId, SearchType.PROJECT.getName());

    }


    @Override
    public Project getProjectSearchFilter(String userId) {
        String filterString = searchMapper.selectProjectSearchFilter(userId);
        if (filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, Project.class);
    }


    @Override
    public void saveLicenseSearchFilter(LicenseMaster licenseMaster, String userId) {

        try {
            if (isEmpty(licenseMaster.getLicenseNameAllSearchFlag())) {
                licenseMaster.setLicenseNameAllSearchFlag(CoConstDef.FLAG_NO);
            }
            licenseMaster.setTotListSize(licenseService.selectLicenseMasterTotalCount(licenseMaster));

        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }


        String stringjson = new Gson().toJson(licenseMaster);
        searchMapper.upsertSearchFilter(stringjson, userId, SearchType.LICENSE.getName());

    }


    @Override
    public LicenseMaster getLicenseSearchFilter(String userId) {
        String filterString = searchMapper.selectLicenseSearchFilter(userId);
        if (filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, LicenseMaster.class);
    }


    @Override
    public void saveSelfCheckSearchFilter(Project project, String userId) {

        String stringjson = new Gson().toJson(project);
        searchMapper.upsertSearchFilter(stringjson, userId, SearchType.SELF_CHECK.getName());

    }


    @Override
    public Project getSelfCheckSearchFilter(String userId) {
        String filterString = searchMapper.selectSelfCheckSearchFilter(userId);
        if (filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, Project.class);
    }


    @Override
    public void savePartnerSearchFilter(PartnerMaster partnerMaster, String userId) {

        String stringjson = new Gson().toJson(partnerMaster);
        searchMapper.upsertSearchFilter(stringjson, userId, SearchType.THIRD_PARTY.getName());

    }

    @Override
    public void saveVulnerabilitySearchFilter(Vulnerability vulnerability, String userId) {

        String stringjson =new Gson().toJson(vulnerability);
        searchMapper.upsertSearchFilter(stringjson, userId, SearchType.VULNERABILITY.getName());
    }


    @Override
    public PartnerMaster getPartnerSearchFilter(String userId) {
        String filterString = searchMapper.selectPartnerSearchFilter(userId);
        if (filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, PartnerMaster.class);
    }


    @Override
    public Object getSearchFilter(String type, String userId) {

        String jsonString = searchMapper.selectSearchFilter(type, userId);
        Class<?> resultClass = null;

        if (!StringUtil.isBlank(jsonString)) {
            switch (SearchType.valueOf(type)) {

                case LICENSE:
                    resultClass = LicenseMaster.class;
                    break;

                case OSS:
                    resultClass = OssMaster.class;
                    break;

                case PROJECT:
                    resultClass = Project.class;
                    break;

                case THIRD_PARTY:
                    resultClass = PartnerMaster.class;
                    break;

                case SELF_CHECK:
                    resultClass = Project.class;
                    break;

            }
        }else{
            return null;
        }
        return new Gson().fromJson(jsonString, resultClass);
    }
    
	@Override
	public void saveSearchFilter(Map<String, Object> params, String userId) {
		String type = (String)params.get("defaultSearchType");
		params.remove("defaultSearchType");
		
		switch (SearchType.valueOf(type)) {
		case LICENSE:
		case OSS:
			params.put("defaultSearchFlag", hasSearchCondition(params));
			break;

		default:
			break;
		}
		
		String stringjson = new Gson().toJson(params);
		searchMapper.upsertSearchFilter(stringjson, userId, SearchType.valueOf(type).getName());
	}


    private String hasSearchCondition(Map<String, Object> params) {
    	for (Object obj : params.values()) {
    		if (obj instanceof String) {
    			if (!StringUtil.isEmpty((String)obj)) {
    				return "Y";
    			}
    		}
    	}
		return "N";
	}


	@Override
    public History work(Object param) {
        return null;
    }

}
