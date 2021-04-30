/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

public interface ApiBatService {
	public List<Map<String, Object>> getBatList(Map<String, Object> paramMap);
}
