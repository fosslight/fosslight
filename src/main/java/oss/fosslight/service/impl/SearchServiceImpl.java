/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service.impl;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.*;
import oss.fosslight.repository.*;
import oss.fosslight.service.LicenseService;
import oss.fosslight.service.SearchService;
import oss.fosslight.util.StringUtil;
import oss.fosslight.common.Type;



@Service
@Slf4j
public class SearchServiceImpl extends CoTopComponent implements SearchService {

    @Autowired LicenseService licenseService;
    @Autowired SearchMapper searchMapper;


    @Override
    public void saveOssSearchFilter(OssMaster ossMaster, String userId) {

        String stringjson = new Gson().toJson(ossMaster);
        searchMapper.upsertSearchFilter(stringjson, userId, Type.OSS.getName());

    }


    @Override
    public OssMaster getOssSearchFilter(String userId) {
        String filterString = searchMapper.selectOssSearchFilter(userId);
        if(filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, OssMaster.class);
    }


    @Override
    public void saveProjectSearchFilter(Project project, String userId) {

        String stringjson = new Gson().toJson(project);
        searchMapper.upsertSearchFilter(stringjson, userId, Type.PROJECT.getName());

    }


    @Override
    public Project getProjectSearchFilter(String userId) {
        String filterString = searchMapper.selectProjectSearchFilter(userId);
        if(filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, Project.class);
    }


    @Override
    public void saveLicenseSearchFilter(LicenseMaster licenseMaster, String userId) {

        try {
            if(isEmpty(licenseMaster.getLicenseNameAllSearchFlag())) {
                licenseMaster.setLicenseNameAllSearchFlag(CoConstDef.FLAG_NO);
            }
            licenseMaster.setTotListSize(licenseService.selectLicenseMasterTotalCount(licenseMaster));

        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }


        String stringjson = new Gson().toJson(licenseMaster);
        searchMapper.upsertSearchFilter(stringjson, userId, Type.LICENSE.getName());

    }


    @Override
    public LicenseMaster getLicenseSearchFilter(String userId) {
        String filterString = searchMapper.selectLicenseSearchFilter(userId);
        if(filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, LicenseMaster.class);
    }


    @Override
    public void saveSelfCheckSearchFilter(Project project, String userId) {

        String stringjson = new Gson().toJson(project);
        searchMapper.upsertSearchFilter(stringjson, userId, Type.SELFCHECK.getName());

    }


    @Override
    public Project getSelfCheckSearchFilter(String userId) {
        String filterString = searchMapper.selectProjectSearchFilter(userId);
        if(filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, Project.class);
    }


    @Override
    public void savePartnerSearchFilter(PartnerMaster partnerMaster, String userId) {

        String stringjson = new Gson().toJson(partnerMaster);
        searchMapper.upsertSearchFilter(stringjson, userId, Type.THIRDPARTY.getName());

    }

    @Override
    public void saveVulnerabilitySearchFilter(Vulnerability vulnerability, String userId) {

        String stringjson =new Gson().toJson(vulnerability);
        searchMapper.upsertSearchFilter(stringjson, userId, Type.VULNERABILITY.getName());
    }


    @Override
    public PartnerMaster getPartnerSearchFilter(String userId) {
        String filterString = searchMapper.selectProjectSearchFilter(userId);
        if(filterString == null) {
            return null;
        }
        return new Gson().fromJson(filterString, PartnerMaster.class);
    }


    @Override
    public Object getSearchFilter(String type, String userId) {

        String jsonString = searchMapper.selectSearchFilter(type, userId);
        Class<?> resultClass = null;

        if (!StringUtil.isBlank(jsonString)) {
            switch (Type.valueOf(type)) {

                case LICENSE:
                    resultClass = LicenseMaster.class;
                    break;

                case OSS:
                    resultClass = OssMaster.class;
                    break;

                case PROJECT:
                    resultClass = Project.class;
                    break;

                case THIRDPARTY:
                    resultClass = PartnerMaster.class;
                    break;

                case SELFCHECK:
                    resultClass = Project.class;
                    break;

            }
        }else{
            return null;
        }
        return new Gson().fromJson(jsonString, resultClass);
    }


    @Override
    public History work(Object param) {
        return null;
    }
}
