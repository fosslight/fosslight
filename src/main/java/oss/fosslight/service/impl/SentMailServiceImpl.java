/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.CoMail;
import oss.fosslight.repository.SentMailMapper;
import oss.fosslight.service.SentMailService;

@Service
public class SentMailServiceImpl extends CoTopComponent implements SentMailService {
	// Mapper
	@Autowired SentMailMapper sentMailMapper;
	
	@Override
	public Map<String, Object> getMailList(CoMail vo) {
		Map<String, Object> map = null;
		
		String filterCondition = CommonFunction.getFilterToString(vo.getFilters());
		
		if (!isEmpty(filterCondition)) {
			vo.setFilterCondition(filterCondition);
		}
		
		int records = sentMailMapper.selectSentMailTotalCount(vo);
		
		if (records > 0) {
			vo.setTotListSize(records);
			
			// Grid paging 처리를 위한 기본 param 설정 Map 생성(반드시 totlistsize를 set 하고 나서 생성해야함)
			map = getGridPagerMap(vo);
			map.put("rows", sentMailMapper.selectSentMailList(vo));
		}
		
		return map == null ? new HashMap<String, Object>() : map;
	}
}
