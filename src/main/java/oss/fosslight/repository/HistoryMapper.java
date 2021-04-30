/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.History;

@Mapper
public interface HistoryMapper {
	// 이력 데이터 입력
	public void insertHistoryData(History history);
	
	// 이력 데이터 총 개수 조회
	public int selectHistoryDataTotalCount(History history);
	
	// 이력 데이터 조회
	public List<History> selectHistoryData(History history);
	
	// 이력 데이터 조회
	public History selectOneHistoryData(History history);
	
	// 이전 이력 데이터 조회
	public History selectOneHistoryBeforeData(History history);
}
