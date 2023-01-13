/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.service.CacheService;

@Service
public class CacheServiceImpl implements CacheService {

	@Autowired LicenseMapper licenseMapper;
	
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
		for (String s : getLicenseNames()) {
			list.add(s.toUpperCase());
		}
		return list;
	}

	@Override
	@Cacheable(value="licenseInfoCache", key="{#licenseId}")
	public LicenseMaster getLicenseInfoById(String licenseId) {
		return licenseMapper.getLicenseInfoWithId(licenseId);
	}
}
