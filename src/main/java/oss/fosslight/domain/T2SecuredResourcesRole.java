/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class T2SecuredResourcesRole implements Serializable{
	private static final long serialVersionUID = 1L;
	private String resourceId;
	private String authority;
}