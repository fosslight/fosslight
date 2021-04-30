/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiPartnerMapper {
	int selectPartnerMasterCount(Map<String, Object> paramMap);
	List<Map<String, Object>> selectPartnerMaster(Map<String, Object> paramMap);
}
