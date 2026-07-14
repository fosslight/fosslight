/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

public enum ProjectPermissionType {
	EDIT("edit"),
	VIEW("view");
	
	private final String type;
	
	ProjectPermissionType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
