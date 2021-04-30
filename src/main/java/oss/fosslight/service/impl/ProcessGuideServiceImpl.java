/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.CoTopComponent;
import oss.fosslight.domain.ProcessGuide;
import oss.fosslight.repository.ProcessGuideMapper;
import oss.fosslight.service.ProcessGuideService;

@Service
public class ProcessGuideServiceImpl extends CoTopComponent implements ProcessGuideService{
	
	@Autowired ProcessGuideMapper processGuideMapper;
	
	/**
	 * 코드 목록 조회
	 */
	@Override
	public Map<String, Object> getProcessGuideList(ProcessGuide vo) throws Exception {
		Map<String, Object> map = null;
		int records = processGuideMapper.selectProcessGuideTotalCount(vo);
		
		if(records > 0) {
			vo.setTotListSize(records);
			// Grid paging 처리를 위한 기본 param 설정 Map 생성(반드시 totlistsize를 set 하고 나서 생성해야함)
			map = getGridPagerMap(vo);
			
			List<ProcessGuide> processGuideList =  processGuideMapper.selectProcessGuideList(vo);
			
			map.put("rows", processGuideList);
		}
		
		return map == null ? new HashMap<String, Object>() : map;
	}
	
	@Override
	public void setProcessGuide(ProcessGuide vo) throws Exception {
		processGuideMapper.updateProcessGuide(vo);
	}
	
	@Override
	public Map<String, Object> getProcessGuide(ProcessGuide vo) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		int records = processGuideMapper.selectProcessGuideCount(vo);
		
		if(records > 0) {
			ProcessGuide guide = processGuideMapper.selectProcessGuide(vo);
			
			if(guide != null) {
				map.put("processGuide", guide);
			}
		}
		
		return map;
	}
}
