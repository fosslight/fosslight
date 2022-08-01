/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.Project;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.ComplianceService;

@Service
@Slf4j
public class ComplianceServiceImpl implements ComplianceService {
	@Autowired
	ProjectMapper projectMapper;
	
	@Override
	public Map<String, Object> getModelList(Project project) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Project> list = new ArrayList<Project>();
		
		try {
			project.setModelFlag(CoConstDef.FLAG_NO);
			List<Project> projectNameList = projectMapper.selectModelInfoList(project);
			
			if(projectNameList != null && projectNameList.size() > 0) {
				list.addAll(projectNameList);
			}
			
			project.setModelFlag(CoConstDef.FLAG_YES);
			List<Project> modelNameList = projectMapper.selectModelInfoList(project);
			
			if(modelNameList != null && modelNameList.size() > 0) {
				list.addAll(modelNameList);
			}
			
			list = list.stream()
						.filter(CommonFunction.distinctByKey(p -> p.getModelName()+"-"+p.getPrjId()))
						.collect(Collectors.toList());
			
			for(String modelName : project.getModelListInfo()) {
				int duplicateCnt = list.stream()
										.filter(p -> modelName.equals(p.getModelName()))
										.collect(Collectors.toList())
										.size();
				
				if(duplicateCnt == 0) {
					Project prj = new Project();
					prj.setModelName(modelName);
					
					list.add(prj);
				}
			}
			
			
			if(list != null) {
				final Comparator<Project> comp = Comparator.comparing((Project p) -> p.getModelName());
				list = list.stream().sorted(comp).collect(Collectors.toList());
				
				// 코드변환처리
				for(Project bean : list) {
					// DISTRIBUTION_TYPE
					bean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, bean.getDistributionType()));
					// Project Status - delay 기능이 삭제됨. 기존에도 delay를 표시하지 않았으므로 priority도 표시하지 않게 처리함.
					bean.setStatus( CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_STATUS, bean.getStatus()));
					// Project OS Type
					bean.setOsType(CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, bean.getOsType()));
					// Project priority 
					bean.setPriority(CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, bean.getPriority())); // 사용을 하지 않지만 값은 저장해둠.
					// Identification Status
					bean.setIdentificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getIdentificationStatus()));
					// Verification Status
					bean.setVerificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getVerificationStatus()));
					// Distribute Status
					String distributionStatus = CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROCESS.equals(bean.getDestributionStatus()) 
													? CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROGRESS : bean.getDestributionStatus();
					bean.setDestributionStatus(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_STATUS, distributionStatus));
					// DIVISION
					bean.setDivision(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, bean.getDivision()));
				}
			}
			
			map.put("page", project.getCurPage());
			map.put("total", project.getTotBlockSize());
			map.put("records", list.size());
			map.put("rows", list);
		} catch(Exception e) {
			log.debug(e.getMessage());
		}
		
		return map;
	}

}
