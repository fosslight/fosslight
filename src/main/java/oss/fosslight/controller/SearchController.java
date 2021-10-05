/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.tools.ant.taskdefs.condition.Http;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.*;
import oss.fosslight.common.Url.SEARCH;
import oss.fosslight.service.SearchService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@Slf4j
public class SearchController extends CoTopComponent {


    @Autowired SearchService searchService;


    @GetMapping(value=SEARCH.LICENSE)
    public @ResponseBody ResponseEntity<Object> saveLicenseSearchFilter(LicenseMaster licenseMaster
            , HttpServletRequest req){

        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));
        String sidx = getSidx(req);
        String sord = req.getParameter("sord");

        licenseMaster.setCurPage(page);
        licenseMaster.setPageListSize(rows);
        licenseMaster.setSortField(sidx);
        licenseMaster.setSortOrder(sord);

        searchService.saveLicenseSearchFilter(licenseMaster, loginUserName());

        return makeJsonResponseHeader("saved");

    }


    @GetMapping(value=SEARCH.OSS)
    public @ResponseBody ResponseEntity<Object> saveOssSearchFilter(OssMaster ossMaster,
                                                                    HttpServletRequest req){

        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));
        String sidx = getSidx(req);

        ossMaster.setSidx(sidx);
        ossMaster.setCurPage(page);
        ossMaster.setPageListSize(rows);

        ossMaster.setHomepage(getHttpPrefix(req.getParameter("homepage")));
        ossMaster.setSearchFlag(CoConstDef.FLAG_YES);
        ossMaster.setSearchFlag(CoConstDef.FLAG_YES);

        searchService.saveOssSearchFilter(ossMaster, loginUserName());


        return makeJsonResponseHeader("saved");
    }


    @GetMapping(value=SEARCH.PARTNER)
    public @ResponseBody ResponseEntity<Object> savePartnerSearchFilter(PartnerMaster partnerMaster,
                                                                        HttpServletRequest req){

        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));
        String sidx = getSidx(req);
        String sord = req.getParameter("sord");

        partnerMaster.setCurPage(page);
        partnerMaster.setPageListSize(rows);
        partnerMaster.setSortField(sidx);
        partnerMaster.setSortOrder(sord);

        searchService.savePartnerSearchFilter(partnerMaster, loginUserName());
        return makeJsonResponseHeader("saved");

    }

    @GetMapping(value=SEARCH.PROJECT)
    public @ResponseBody ResponseEntity<Object> saveProjectSearchFilter(Project project,
                                                                        HttpServletRequest req){
        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));
        String sidx = getSidx(req);
        String sord = req.getParameter("sord");

        project.setCurPage(page);
        project.setPageListSize(rows);
        project.setSortField(sidx);
        project.setSortOrder(sord);

        project.setPublicYn(isEmpty(project.getPublicYn()) ? CoConstDef.FLAG_YES:project.getPublicYn());
        searchService.saveProjectSearchFilter(project, loginUserName());

        return makeJsonResponseHeader("saved");

    }

    @GetMapping(value=SEARCH.SELFCHECK)
    public @ResponseBody ResponseEntity<Object> saveSelfCheckSearchFilter(Project project,
                                                                        HttpServletRequest req){
        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));
        String sidx = getSidx(req);
        String sord = req.getParameter("sord");

        project.setCurPage(page);
        project.setPageListSize(rows);
        project.setSortField(sidx);
        project.setSortOrder(sord);

        searchService.saveSelfCheckSearchFilter(project, loginUserName());

        return makeJsonResponseHeader("saved");


    }


    @GetMapping(value=SEARCH.VULNERABILITY)
    public @ResponseBody ResponseEntity<Object> saveVulnerabilitySearchFilter(Vulnerability vulnerability, HttpServletRequest req){

        int page = Integer.parseInt(req.getParameter("page"));
        int rows = Integer.parseInt(req.getParameter("rows"));

        vulnerability.setCurPage(page);
        vulnerability.setPageListSize(rows);

        searchService.saveVulnerabilitySearchFilter(vulnerability, loginUserName());

        return makeJsonResponseHeader("saved");
    }


    @GetMapping(value=SEARCH.PATH)
    public @ResponseBody ResponseEntity<?> getSearchInfo(@RequestParam("type") String type){

        Object searchFilter = searchService.getSearchFilter(type, loginUserName());

        if (Objects.isNull(searchFilter)){
            return makeJsonResponseHeader(false, "Not Found");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("type", type);
        result.put("searchFilter", searchFilter);

        return makeJsonResponseHeader(result);
    }


    public String getSidx(HttpServletRequest req){

        String sidx = req.getParameter("sidx");
        if(sidx != null) {
            sidx = sidx.split("[,]")[1].trim();
        }
        return sidx;
    }


    public String getHttpPrefix(String homepage){
        List<String> httpPrefixs = Arrays.asList("https://", "http://", "www.");
        if(!httpPrefixs.contains(homepage)){
            homepage = homepage.replaceFirst("^((http|https)://)?(www.)*", "");
        }
        return homepage;
    }


}
