/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApiSelfCheckMapper {
	int createSelfCheck(Map<String, Object> param);
	
	int getCreateProjectCnt(@Param("userId") String userId);
	
	int selectProjectCount(@Param("userId") String userId, @Param("prjId") String prjId);
}
