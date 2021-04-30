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
import oss.fosslight.domain.BinaryAnalysisResult;
import oss.fosslight.repository.BinaryDataHistoryMapper;
import oss.fosslight.service.BinaryDataHistoryService;

@Service
public class BinaryDataHistoryServiceImpl extends CoTopComponent implements BinaryDataHistoryService {
	@Autowired BinaryDataHistoryMapper binaryDataHistoryMapper;

	@Override
	public Map<String, Object> getBinaryDataHistoryList(BinaryAnalysisResult bean) {
		Map<String, Object> map = null;
		
		Map<String, String> exceptionMap = new HashMap<>();
		exceptionMap.put("binaryName", "fileName");
		exceptionMap.put("filePath", "pathName");
		exceptionMap.put("tlsh", "tlshCheckSum");
		exceptionMap.put("parentname", "parentName");
		exceptionMap.put("platformname", "platformName");
		exceptionMap.put("platformversion", "platformVersion");
		exceptionMap.put("updatedate", "updateDate");
		exceptionMap.put("createddate", "createdDate");
		
		String filterCondition = CommonFunction.getFilterToString(bean.getFilters(), null, exceptionMap);
		
		if(!isEmpty(filterCondition)) {
			bean.setFilterCondition(filterCondition);
		}
		
		int records = binaryDataHistoryMapper.selectBinaryDataHistoryTotalCount(bean);
		
		if(records > 0) {
			bean.setTotListSize(records);
			
			map = getGridPagerMap(bean);
			map.put("rows", binaryDataHistoryMapper.selectBinaryDataHistoryList(bean));
		}
		
		return map == null ? new HashMap<String, Object>() : map;
	}
}
