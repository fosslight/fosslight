/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import oss.fosslight.domain.History;

public interface HistoryService {
	public void storeData(History history);
	
	public History getData(History history);
	
	public Map<String, Object> getList(History history);
	
	public Map<String, Object> getAsToBeHistoryDataByGrid(History history);
}
