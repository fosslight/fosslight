/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

public interface ApiCodeService {
	public List<Map<String, Object>> getCodeList(String codeType, String detailValue);
}
