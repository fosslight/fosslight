/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.config.AppConstBean;

@Setter
@Getter
public class T2Roles implements Serializable{

	@Serial
	private static final long serialVersionUID = 1L;
	
	private String authority; // 권한
	private String roleName; // 권한 명
	private String description; // 권한 설명
	private String createDate; // 등록일
	private String modifyDate; // 수정일
	
	private List<T2SecuredResourcesRole> securedResourcesRole; // 권한에 맵핑된 리소스
	
	public void setupDefaultRoleData(){
		this.authority = AppConstBean.SECURITY_ROLE_DEFAULT;
		this.roleName = "모든 사용자가 가지는 기본 역할";
		this.description = "모든 사용자가 가지는 기본 역할. 삭제 불가능.";
		this.setCreateDate();
		this.setModifyDate();
	}
	
	public void setCreateDate() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(CoConstDef.DATABASE_FORMAT_DATE_ALL);
		this.createDate = sdf.format(now);
	}
	public void setModifyDate(){
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(CoConstDef.DATABASE_FORMAT_DATE_ALL);
		this.modifyDate = sdf.format(now);
	}
}