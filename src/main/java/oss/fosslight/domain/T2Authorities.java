/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class T2Authorities implements Serializable{
	@Serial
	private static final long serialVersionUID = 1L;
	private String userId;																	// 사용자 Id
	private String authority;																// 권한 아이디
	private String[] userIds;																//사용자 아이디들
	private String[] authoritys;															// 권한 아이디들
	
	public static final String ROLE_USER	= "ROLE_USER";
}