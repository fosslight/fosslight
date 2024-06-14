/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.repository.DashboardMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.service.DashboardService;

@Service("DashboardService")
public class DashboardServiceImpl extends CoTopComponent implements DashboardService {
	//Mapper
	@Autowired DashboardMapper dashboardMapper;
	@Autowired OssMapper ossMapper;
	
	@Override
	public Map<String, Object> getDashboardJobsList(Project project) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        
        paramMap.put("loginUserName", loginUserName());
        paramMap.put("loginUserRole", loginUserRole());
        paramMap.put("projectFlag", CommonFunction.getProperty("menu.project.use.flag"));
        paramMap.put("partnerFlag", CommonFunction.getProperty("menu.partner.use.flag"));
        
        if ("Progress".equals(project.getIdentificationStatus())) {
            paramMap.put("status", "PROG");
		} else if ("Request".equals(project.getIdentificationStatus())){
			paramMap.put("status", "REQ");
        } else if ("Review".equals(project.getIdentificationStatus())){
            paramMap.put("status", "REV");
        } else if ("Final Review".equals(project.getIdentificationStatus())){
            paramMap.put("status", "FREV");
        }
        
        int records = dashboardMapper.selectDashboardJobsTotalCount(paramMap);
        
        project.setTotListSize(records);
        
        map.put("page", project.getCurPage());
        map.put("total", project.getTotBlockSize());
        map.put("records", records);
 
        List<Project> list = dashboardMapper.selectDashboardJobsList(paramMap);
        
        if (list != null) {
			// 코드변환처리
			for (Project bean : list) {
				bean.setStatus( CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_STATUS, bean.getStatus()));
				String androidFlag = CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equalsIgnoreCase(avoidNull(bean.getNoticeType())) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO;
				bean.setAndroidFlag(androidFlag);
				// Project priority 
				bean.setPriority(CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, bean.getPriority()));
			}
		}
        
        map.put("rows", list);
        return map;
    }
	
	@Override
    public Map<String, Object> getDashboardCommentsList(Map<String, Object> param) {
		boolean moreYn = param.containsKey("moreYn");
		HashMap<String, Object> map = new HashMap<String, Object>();
        List<String> referenceDivList = new ArrayList<>();
        boolean projectFlag = CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES);
        boolean partnerFlag = CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES);
        
        if (projectFlag) {
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_HIS);
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
        }
        
        if (partnerFlag) {
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_PARTNER_HIS);
        }
        
        param.put("loginUserName", loginUserName());
        param.put("loginUserRole", loginUserRole());
        param.put("referenceDivList", referenceDivList);
          
        if (moreYn) {
        	map.put("records", dashboardMapper.selectDashboardCommentsTotalCount(param));
        	map.put("rows", dashboardMapper.selectDashboardCommentsList(param));
        } else {
        	int records = dashboardMapper.selectDashboardCommentsTotalCount(param);
        	if (records > 5) {
        		map.put("moreYn", true);
        		map.put("prjId", (String) param.get("prjId"));
            	map.put("prjDivision", (String) param.get("prjDivision"));
        	}
            map.put("records", records);
            map.put("rows", dashboardMapper.selectDashboardCommentsList(param));
        }
        
        return map;
    }
	
	@Override
    public Map<String, Object> getDashboardOssList(OssMaster ossMaster) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        int records = dashboardMapper.selectDashboardOssTotalCount(ossMaster);
        ossMaster.setTotListSize(records);

        List<OssMaster> ossList = dashboardMapper.selectDashboardOssList(ossMaster);
        
		for (OssMaster item : ossList) {
			if (CoCodeManager.OSS_INFO_BY_ID.containsKey(item.getOssId())) {
				item.setLicenseName(CommonFunction.makeLicenseExpression(CoCodeManager.OSS_INFO_BY_ID.get(item.getOssId()).getOssLicenses()));
			}
		}
		
        map.put("page", ossMaster.getCurPage());
        map.put("total", ossMaster.getTotBlockSize());
        map.put("records", records);
        map.put("rows", ossList);
        
        return map;
    }
	
	@Override
    public Map<String, Object> getDashboardLicenseList(LicenseMaster licenseMaster) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        int records = dashboardMapper.selectDashboardLicenseTotalCount(licenseMaster);
        licenseMaster.setTotListSize(records);
        
        map.put("page", licenseMaster.getCurPage());
        map.put("total", licenseMaster.getTotBlockSize());
        map.put("records", records);
        map.put("rows", dashboardMapper.selectDashboardLicenseList(licenseMaster));
        
        return map;
    }
	
	@Override
    public void readConfirmAll(CommentsHistory commentsHistory) {
    	dashboardMapper.readConfirmAll(commentsHistory);
    }

	@Override
	public List<Map<String, Object>> getProgProjectCnt() {
		List<Map<String, Object>> rtnList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        
        paramMap.put("loginUserName", loginUserName());
        paramMap.put("loginUserRole", loginUserRole());
        paramMap.put("projectFlag", CommonFunction.getProperty("menu.project.use.flag"));
        paramMap.put("partnerFlag", CommonFunction.getProperty("menu.partner.use.flag"));
        
        rtnList = dashboardMapper.selectProgProjectCnt(paramMap);
        return rtnList;
	}

	@Override
	public List<Project> getCustomDashboardJobsList() {
		Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("loginUserName", loginUserName());
        paramMap.put("loginUserRole", loginUserRole());
        paramMap.put("projectFlag", CommonFunction.getProperty("menu.project.use.flag"));
        paramMap.put("partnerFlag", CommonFunction.getProperty("menu.partner.use.flag"));
        
		return dashboardMapper.selectDashboardJobsList(paramMap);
	}

	@Override
	public List<Map<String, Object>> getDiscoveredEmlList() {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("loginUserName", loginUserName());
		return dashboardMapper.getDiscoveredEmlList(paramMap);
	}

	@Override
	public Map<String, Object> getDiscoveredEmlMessage(HashMap<String, Object> param) {
		Map<String, Object> rtnMap = new HashMap<>();
		param.put("loginUserName", loginUserName());
		param.put("loginUserRole", loginUserRole());
		// User email must be set in param
        
		String emlMessage = dashboardMapper.getDiscoveredEmlMessage(param);
		if (emlMessage.indexOf("Vulnerability Information") > -1) {
			emlMessage = emlMessage.split("Vulnerability Information")[1];
			emlMessage = emlMessage.substring(emlMessage.indexOf("<table"), emlMessage.indexOf("</table>")+8);
		} else {
			emlMessage = "";
		}
		rtnMap.put("emlMessage", emlMessage);
		return rtnMap;
	}

	@Override
	public Map<String, Object> getNvdDashboardList() {
		Map<String, Object> rtnMap = new HashMap<>();
		
		// time period
		List<Map<String, Object>> nvdDashboardList = dashboardMapper.getNvdDashboardList();
		if (nvdDashboardList == null) nvdDashboardList = new ArrayList<>();
		rtnMap.put("timePeriodCntList", nvdDashboardList);
		
		// severity
		List<Map<String, Object>> nvdSeverityList = dashboardMapper.getNvdSeverityList();
		if (nvdSeverityList == null) nvdSeverityList = new ArrayList<>();
		rtnMap.put("nvdSeverityList", nvdSeverityList);
		
		return rtnMap;
	}

	@Override
	public List<Map<String, Object>> getLatestScoredVulns() {
		return dashboardMapper.getLatestScoredVulns();
	}
}
