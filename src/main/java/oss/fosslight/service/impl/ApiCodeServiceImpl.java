/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.repository.ApiCodeMapper;
import oss.fosslight.service.ApiCodeService;

@Service
public class ApiCodeServiceImpl implements ApiCodeService {
	@Autowired ApiCodeMapper apiCodeMapper;
	
	public List<Map<String, Object>> getCodeList(String codeType, String detailValue){
		String cdNo = "";
		
		switch(codeType.toUpperCase()) { // code 번호 분류
			case "DIV":
				cdNo = CoConstDef.CD_USER_DIVISION;
				
				break;
			case "OS":
				cdNo = CoConstDef.CD_OS_TYPE;
				
				break;
			case "DSTT":
				cdNo = CoConstDef.CD_DISTRIBUTION_TYPE;
				
				break;
			case "DSTS":
				cdNo = CoConstDef.CD_DISTRIBUTE_CODE;
				
				break;
			case "NOTI":
				cdNo = CoConstDef.CD_NOTICE_TYPE;
				
				break;
			case "NP":
				cdNo = CoConstDef.CD_PLATFORM_GENERATED;
				
				break;
			case "PRI":
				cdNo = CoConstDef.CD_PROJECT_PRIORITY;
				
				break;
			default:
				cdNo = null; // 나올 수 없는 case
				
				break;
		}
		
		return apiCodeMapper.selectCodeList(cdNo, detailValue);
	}
}