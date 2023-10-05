/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class OssAnalysis extends ComBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public OssAnalysis() {}
			
	public OssAnalysis(String groupId, String ossName, String ossVersion, String ossNickname
			, String licenseName, String ossCopyright, String downloadLocation
			, String homepage, String summaryDescription, String comment, String result, String title) {
		this.groupId = groupId;
		this.ossName = ossName;
		this.ossVersion = ossVersion;
		this.ossNickname = ossNickname;
		this.licenseName = licenseName;
		this.ossCopyright = ossCopyright;
		this.downloadLocation = downloadLocation;
		this.homepage = homepage;
		this.summaryDescription = summaryDescription;
		this.comment = comment;
		this.result = result;
		this.title = title;
	}
	
	private String groupId;
	private String componentId;
	private String prjId;
	private String title;
	private String result;
	private String ossName;
	private String ossNickname;
	private String ossVersion;
	private String licenseName;
	private String licenseDiv;
	private String concludedLicense;
	private String askalonoLicense;
	private String scancodeLicense;
	private String needReviewLicenseAskalono;
	private String needReviewLicenseScanode;
	private String detectedLicense;
	private String downloadLocation;
	private String downloadLocationGroup;
	private String homepage;
	private String ossCopyright;
	private String summaryDescription;
	private String comment;
	private String analysisYn;
	private String completeYn;
	private String referenceOssId;
	private String useYn;
	private String ossType;
}
