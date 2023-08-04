/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApiPartnerMapper {
	int selectPartnerMasterCount(Map<String, Object> paramMap);
	List<Map<String, Object>> selectPartnerMaster(Map<String, Object> paramMap);
	int existsWatcherByEmail(@Param("partnerId") String partnerId, @Param("email") String email);
	void insertWatcher(Map<String, Object> paramMap);
	int existPartnertCnt(Map<String, Object> paramMap);
}
