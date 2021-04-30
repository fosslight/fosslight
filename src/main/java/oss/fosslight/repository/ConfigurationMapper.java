/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.Configuration;

@Mapper
public interface ConfigurationMapper {
	void updateDefaultTab(Configuration configuration);
}
