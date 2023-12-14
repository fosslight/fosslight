/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import oss.fosslight.api.dto.ListSelfCheckDto;
import oss.fosslight.api.dto.SelfCheckDto;

@Mapper
public interface ApiSelfCheckMapper {
	int createSelfCheck(Map<String, Object> param);
	
	int getCreateProjectCnt(@Param("userId") String userId);
	
	int selectProjectCount(Map<String, Object> paramMap);

	Map<String, Object> selectProjectMaster(@Param("prjId") String prjId);

	int existsWatcherByEmail(@Param("prjId") String prjId, @Param("email") String email);

	void insertWatcher(Map<String, Object> paramMap);

	List<SelfCheckDto> selectSelfCheckList(ListSelfCheckDto.Request request);

	int selectSelfCheckTotalCount(ListSelfCheckDto.Request request);

	SelfCheckDto selectSelfCheckById(String id);
}
