/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.common.CommonFunction;
import oss.fosslight.repository.ApiOssMapper;
import oss.fosslight.service.ApiOssService;
import oss.fosslight.util.StringUtil;

@Service
public class ApiOssServiceImpl implements ApiOssService {
	/** The api oss mapper. */
	@Autowired ApiOssMapper apiOssMapper;

	@Override
	public List<Map<String, Object>> getOssInfo(Map<String, Object> paramMap) {
		String rtnOssName = apiOssMapper.getOssName((String) paramMap.get("ossName"));
		
		if (!StringUtil.isEmpty(rtnOssName)) {
			paramMap.replace("ossName", rtnOssName);
		}
		
		return apiOssMapper.getOssInfo(paramMap);
	}

	@Override
	public List<Map<String, Object>> getOssInfoByDownloadLocation(String downloadLocation) {
		return apiOssMapper.getOssInfoByDownloadLocation(downloadLocation);
	}

	@Override
	public List<Map<String, Object>> getLicenseInfo(String licenseName) {
		return apiOssMapper.getLicenseInfo(licenseName);
	}
	
	
	public String[] getOssNickNameListByOssName(String ossName) {
		List<String> nickList = null;
		if (!StringUtil.isEmpty(ossName)) {
			nickList =  apiOssMapper.selectOssNicknameList(ossName);
			if (nickList != null) {
				nickList = nickList.stream()
									.filter(CommonFunction.distinctByKey(nick -> nick.trim().toUpperCase()))
									.collect(Collectors.toList());
			}
		}
		
		nickList = (nickList != null ? nickList : Collections.emptyList());
		return nickList.toArray(new String[nickList.size()]);
	}
	
}