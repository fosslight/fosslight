/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        return (Project) makeSearchFilterObject(filterString, SearchType.PROJECT.getName());
    }


    @SuppressWarnings("unchecked")
	private Object makeSearchFilterObject(String filterString, String searchType) {
    	Object obj = new Object();
    	ObjectMapper mapper = new ObjectMapper();
    	Map<String, String> map = new HashMap<>();
    	
    	try {
			map = mapper.readValue(filterString, Map.class);
    	} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
    	
    	if (searchType.equals(SearchType.PROJECT.getName())) {
    		Project project = new Project();
        	
    		for (String key : map.keySet()) {
				String value = map.get(key);
				if (!isEmpty(value)) {
					switch (key) {
						case "prjId" : project.setPrjId(value);
							break;
						case "prjName" : project.setPrjName(value);
							break;
						case "schStartDate" : project.setSchStartDate(value);
							break;
						case "schEndDate" : project.setSchEndDate(value);
							break;
						case "prjDivision" : project.setPrjDivision(value);
							break;
						case "creator" : project.setCreator(value);
							break;
						case "reviewer" : project.setReviewer(value);
							break;
						case "watchers" : project.setWatchers(new String[] {value});
							break;
						case "distributionType" : project.setDistributionType(value);
							break;
						case "networkServerType" : project.setNetworkServerType(value);
							break;
						case "modelName" : project.setModelName(value );
							break;
						case "statuses" : project.setStatuses(value);
							break;
						case "priority" : project.setPriority(value);
							break;
						case "publicYn" : project.setPublicYn(value);
							break;
						default : break;
					}
				}
			}
			
			obj = project;
    	} else {
    		PartnerMaster partner = new PartnerMaster();
    		
    		for (String key : map.keySet()) {
				String value = map.get(key);
				if (!isEmpty(value)) {
					switch (key) {
						case "partnerId" : partner.setPartnerId(value);
							break;
						case "partnerName" : partner.setPartnerName(value);
							break;
						case "softwareName" : partner.setSoftwareName(value);
							break;
						case "softwareVersion" : partner.setSoftwareVersion(value);
							break;
						case "division" : partner.setDivision(value);
							break;
						case "createdDate1" : partner.setCreatedDate1(value);
							break;
						case "createdDate2" : partner.setCreatedDate2(value);
							break;
						case "creator" : partner.setCreator(value);;
							break;
						case "reviewer" : partner.setReviewer(value);
							break;
						case "watchers" : partner.setWatchers(new String[] {value});
							break;
						case "status" : partner.setStatus(value);
							break;
						case "publicYn" : partner.setPublicYn(value);
							break;
						default : break;
					}
				}
			}
			
			obj = partner;
    	}
        
        return obj;
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
        return (PartnerMaster) makeSearchFilterObject(filterString, SearchType.THIRD_PARTY.getName());
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
        } else{
            return null;
        }
        
        if (SearchType.valueOf(type).equals(SearchType.PROJECT)) {
        	return makeSearchFilterObject(jsonString, SearchType.PROJECT.getName());
        } else if (SearchType.valueOf(type).equals(SearchType.THIRD_PARTY)) {
        	return makeSearchFilterObject(jsonString, SearchType.THIRD_PARTY.getName());
        } else {
        	return new Gson().fromJson(jsonString, resultClass);
        }
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
