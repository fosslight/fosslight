/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import org.apache.commons.collections.map.ListOrderedMap;
import org.springframework.jdbc.support.JdbcUtils;

import java.io.Serial;

public class CamelMap extends ListOrderedMap {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public Object put(Object key, Object value) {
		return super.put(JdbcUtils.convertUnderscoreNameToPropertyName((String) key), value);
	}
}
