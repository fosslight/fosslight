/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.CacheService;

@Service
public class CacheServiceImpl implements CacheService {

	@Autowired private LicenseMapper licenseMapper;
	@Autowired private ProjectMapper projectMapper;
	
	@Override
	@Cacheable(value="licenseInfoCache")
	public Set<String> getLicenseNames() {
		Set<String> list = licenseMapper.getLicenseNames();
		list.addAll(licenseMapper.getLicenseShortNames());
		list.addAll(licenseMapper.getLicenseNickNames());
		return list;
	}

	@Override
	@Cacheable(value="licenseInfoCache", key="{#licenseName}")
	public LicenseMaster getLicenseInfo(String licenseName) {
		return licenseMapper.getLicenseInfoWithName(licenseName);
	}

	@Override
	@Cacheable(value="licenseInfoCache")
	public Set<String> getLicenseUpperNames() {
		Set<String> list = new HashSet<>();
		getLicenseNames().forEach(s -> list.add(s.toUpperCase()));
		return list;
	}

	@Override
	@Cacheable(value="licenseInfoCache", key="{#licenseId}")
	public LicenseMaster getLicenseInfoById(String licenseId) {
		return licenseMapper.getLicenseInfoWithId(licenseId);
	}

	@Override
	@Cacheable(value="identificationNvdMaxScoreCache")
	public String findIdentificationMaxNvdInfo(String prjId, String referenceDiv) {
		List<String> customNvdMaxScoreInfoList = new ArrayList<>();
		List<String> nvdMaxScoreInfoList = projectMapper.findIdentificationMaxNvdInfo(prjId, referenceDiv);
		List<String> nvdMaxScoreInfoList2 = projectMapper.findIdentificationMaxNvdInfoForVendorProduct(prjId, referenceDiv);
		
		if (nvdMaxScoreInfoList != null && !nvdMaxScoreInfoList.isEmpty()) {
			nvdMaxScoreInfoList = nvdMaxScoreInfoList.stream().distinct().collect(Collectors.toList());
			String conversionCveInfo = CommonFunction.checkNvdInfoForProduct(CoCodeManager.OSS_INFO_UPPER, nvdMaxScoreInfoList);
			if (conversionCveInfo != null) {
				customNvdMaxScoreInfoList.add(conversionCveInfo);
			}
		}
		
		if (nvdMaxScoreInfoList2 != null && !nvdMaxScoreInfoList2.isEmpty()) {
			customNvdMaxScoreInfoList.addAll(nvdMaxScoreInfoList2);
		}
		
		if (customNvdMaxScoreInfoList != null && !customNvdMaxScoreInfoList.isEmpty()) {
			return CommonFunction.getConversionCveInfoForList(customNvdMaxScoreInfoList);
		}
		return null;
	}
}
