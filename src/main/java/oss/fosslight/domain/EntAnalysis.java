/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class EntAnalysis extends ComBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public EntAnalysis() {}
			
	public EntAnalysis(String referenceId, String division, String totalCount) {
		this.referenceId = referenceId;
		this.division = division;
		this.totalCount = totalCount;
	}
	
	private int jobSeq;
	private String referenceId;
	private String division;
	private String ossName;
	private String ossVersion;
	private String licenseName;
	private String downloadLocation;
	private String homepage;
	private String analysisStartDate;
	private String analysisEndDate;
	private String successCount;
	private String failureCount;
	private String totalCount;
	private String referenceOssId;
}
