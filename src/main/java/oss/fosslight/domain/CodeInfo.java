/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Data;

/**
 * Instantiates a new code info.
 */
@Data
@Alias("codeInfo")
public class CodeInfo {

	/** The cd grp id. */
	private String cdGrpId;
	
	/** The cd grp nm. */
	private String cdGrpNm;
	
	/** The cd id. */
	private String cdId;
	
	/**  서브 그룹 코드. */
	private String subGrpCd;
	
	/** The cd nm. */
	private String cdNm;
	
	/** The cd order. */
	private String cdOrder;
	
	/** The remark. */
	private String remark;
	
	/** The use yn. */
	private String useYn;
	
	/** The reg dt. */
	private String regDt;
	
	/** The reg id. */
	private String regId;
	
	/** The mod dt. */
	private String modDt;
	
	/** The mod id. */
	private String modId;
	
	/** The auth mode.<br> 0:조회권한, 1:수정권한 */
	private String authMode;
	
	/** The sub code list. */
	private List<CodeInfo> subCodeList;
}
