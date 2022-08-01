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
        
        int records = dashboardMapper.selectDashboardJobsTotalCount(paramMap);
        
        project.setTotListSize(records);
        
        map.put("page", project.getCurPage());
        map.put("total", project.getTotBlockSize());
        map.put("records", records);
        
        List<Project> list = dashboardMapper.selectDashboardJobsList(paramMap);
        // TODO bin 변경  > ???
        
        if(list != null) {
			// 코드변환처리
			for(Project bean : list) {
				bean.setStatus( CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_STATUS, bean.getStatus()));
				bean.setOsType(CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, bean.getOsType()));

				// Project priority 
				bean.setPriority(CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, bean.getPriority()));
			}
		}
        
        map.put("rows", list);
        return map;
    }
	
	@Override
    public Map<String, Object> getDashboardCommentsList(CommentsHistory commentsHistory) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        List<String> referenceDivList = new ArrayList<>();
        boolean projectFlag = CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES);
        boolean partnerFlag = CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES);
        
        if(projectFlag) {
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_HIS);
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
        }
        
        if(partnerFlag) {
        	referenceDivList.add(CoConstDef.CD_DTL_COMMENT_PARTNER_HIS);
        }
        
        paramMap.put("loginUserName", loginUserName());
        paramMap.put("loginUserRole", loginUserRole());
        paramMap.put("referenceDivList", referenceDivList);
        
        int records = dashboardMapper.selectDashboardCommentsTotalCount(paramMap);
        commentsHistory.setTotListSize(records);

        map.put("page", commentsHistory.getCurPage());
        map.put("total", commentsHistory.getTotBlockSize());
        map.put("records", records);
        map.put("rows", dashboardMapper.selectDashboardCommentsList(paramMap));
        
        return map;
    }
	
	@Override
    public Map<String, Object> getDashboardOssList(OssMaster ossMaster) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        int records = dashboardMapper.selectDashboardOssTotalCount(ossMaster);
        ossMaster.setTotListSize(records);

        List<OssMaster> ossList = dashboardMapper.selectDashboardOssList(ossMaster);
        
		for(OssMaster item : ossList) {
			if(CoCodeManager.OSS_INFO_BY_ID.containsKey(item.getOssId())) {
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
}
