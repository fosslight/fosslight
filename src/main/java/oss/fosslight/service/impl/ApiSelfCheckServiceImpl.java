/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.repository.ApiSelfCheckMapper;
import oss.fosslight.service.ApiSelfCheckService;

@Service
public class ApiSelfCheckServiceImpl implements ApiSelfCheckService {
	@Autowired ApiSelfCheckMapper apiSelfcheckMapper;
	
	@Override
	public int getCreateProjectCnt(String userId) {
		return apiSelfcheckMapper.getCreateProjectCnt(userId);
	}
	
	@Transactional
	@Override
	public Map<String, Object> createSelfCheck(Map<String, Object> paramMap){
		Map<String, Object> result = new HashMap<String, Object>();
		
		apiSelfcheckMapper.createSelfCheck(paramMap);
		
		Long prjId = (Long) paramMap.get("prjId");
		result.put("prjId", prjId.toString());
		
		return result;
	}
	
	@Override
	public boolean existProjectCnt(Map<String, Object> paramMap) {
		int records = apiSelfcheckMapper.selectProjectCount(paramMap);
		
		return records == 1 ? true : false;
	}
}