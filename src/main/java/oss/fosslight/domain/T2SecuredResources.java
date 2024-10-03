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

@Setter
@Getter
public class T2SecuredResources implements Serializable{

	@Serial
	private static final long serialVersionUID = 1L;
	
	private String resourceId; // 리소스 ID
	private String resourceName; // 리소스 명
	private String resourcePattern; // 리소스 패턴
	private String description; // 리소스 설명
	private String resourceType; // 리소스 타입 ('URL')
	private Integer sortOrder; // 순위
	private String createDate; // 등록일
	private String modifyDate; // 수정일
	
	private List<T2SecuredResourcesRole> securedResourcesRoles; // 리소스에 맵핑되어 있는 역할 리스트
	
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