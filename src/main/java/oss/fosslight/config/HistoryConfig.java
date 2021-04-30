/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import oss.fosslight.domain.History;

public interface HistoryConfig {
	public static final String INSERT_ACTION = "INSERT";
	public static final String DELETE_ACTION = "DELETE";
	public static final String UPDATE_ACTION = "UPDATE";
	
	public History work(Object param);
}
