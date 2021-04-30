/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import oss.fosslight.domain.BinaryAnalysisResult;

public interface BinaryDataHistoryService {
	Map<String, Object> getBinaryDataHistoryList(BinaryAnalysisResult bean);
}
