/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.repository.ApiPartnerMapper;
import oss.fosslight.service.ApiPartnerService;

@Service
public class ApiPartnerServiceImpl implements ApiPartnerService {
	@Autowired ApiPartnerMapper apiPartnerMapper;
	
	@Override
	public List<Map<String, Object>> getPartnerMasterList(Map<String, Object> paramMap){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		int partnerCnt = apiPartnerMapper.selectPartnerMasterCount(paramMap);
		
		if(partnerCnt > 0) {
			list = apiPartnerMapper.selectPartnerMaster(paramMap);
		}
		
		return list;
	}
}