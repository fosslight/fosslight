/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.BinaryAnalysisResult;

@Mapper
public interface BinaryDataHistoryMapper {
	int selectBinaryDataHistoryTotalCount(BinaryAnalysisResult bean); 
	
	List<BinaryAnalysisResult> selectBinaryDataHistoryList(BinaryAnalysisResult bean);
}
