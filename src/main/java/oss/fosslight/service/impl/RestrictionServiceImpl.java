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

import io.jsonwebtoken.lang.Arrays;
import oss.fosslight.CoTopComponent;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.service.RestrictionService;

@Service
public class RestrictionServiceImpl extends CoTopComponent implements RestrictionService {

	@Autowired OssMapper ossMapper;
	@Autowired LicenseMapper licenseMapper;
	
	@Override
	public Map<String, Object> getOssLicenseInfobyRestriction(String cdDtlNo) {
		Map<String, Object> rtnMap = new HashMap<>();
		List<String> matchedOssNames = new ArrayList<>();
		List<String> matchedLicenseNames = new ArrayList<>();
		
		List<OssMaster> ossList = ossMapper.getOssInfoByRestriction(cdDtlNo);
		List<LicenseMaster> LicenseList = licenseMapper.getLicenseInfoByRestriction(cdDtlNo);
		
		if (ossList != null && !ossList.isEmpty()) {
			for (OssMaster om : ossList) {
				if (!isEmpty(om.getRestriction())) {
					List<String> restrictions = Arrays.asList(om.getRestriction().split(","));
					for (String restriction : restrictions) {
						if (restriction.equals(cdDtlNo) && !matchedOssNames.contains(om.getOssName())) {
							matchedOssNames.add(om.getOssName());
							break;
						}
					}
				}
			}
		}
		
		if (LicenseList != null && !LicenseList.isEmpty()) {
			for (LicenseMaster lm : LicenseList) {
				if (!isEmpty(lm.getRestriction())) {
					List<String> restrictions = Arrays.asList(lm.getRestriction().split(","));
					for (String restriction : restrictions) {
						if (restriction.equals(cdDtlNo) && !matchedLicenseNames.contains(lm.getLicenseName())) {
							matchedLicenseNames.add(lm.getLicenseName());
							break;
						}
					}
				}
			}
		}
		
		rtnMap.put("matchedOssNames", matchedOssNames);
		rtnMap.put("matchedLicenseNames", matchedLicenseNames);
		
		return rtnMap;
	}
	
}