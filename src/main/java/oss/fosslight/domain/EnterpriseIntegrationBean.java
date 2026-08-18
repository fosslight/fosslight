/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EnterpriseIntegrationBean extends ComBean implements Serializable {
	private static final long serialVersionUID = 3499915410560019672L;
	
	private String jobSeq;
	private String referenceId;
	private String division;
	private String analysisStartDate;
	private String analysisEndDate;
	private int successCount;
	private int failureCount;
	private int totalCount;
}
