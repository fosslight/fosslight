/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

public class OssLicense extends ComBean implements Serializable {

	private static final long serialVersionUID = -2514402041356425806L;
	
	private String ossId;					//OSS 아이디
	private String licenseDiv;				//Single, Multi 구분
	private String licenseId;				//라이센스ID
	private String ossLicenseIdx;			//라이센스 순서
	private String ossLicenseComb;			//라이센스
	private String ossLicenseText;			//라이센스Text
	private String ossCopyright;			//라이센스 Copyright
	private String licenseName;				//라이센스 이름
	private String licenseNameEx;				//라이센스 이름
	private String licenseType;				//라이센스 타입
	private String obligation;				
	private String obligationChecks;			//Obligation Check 상태(고지, 소스공개, 체크)
	private String restriction;
	
	public String getOssId() {
		return ossId;
	}
	public void setOssId(String ossId) {
		this.ossId = ossId;
	}
	public String getLicenseDiv() {
		return licenseDiv;
	}
	public void setLicenseDiv(String licenseDiv) {
		this.licenseDiv = licenseDiv;
	}
	public String getLicenseId() {
		return licenseId;
	}
	public void setLicenseId(String licenseId) {
		this.licenseId = licenseId;
	}
	public String getOssLicenseIdx() {
		return ossLicenseIdx;
	}
	public void setOssLicenseIdx(String ossLicenseIdx) {
		this.ossLicenseIdx = ossLicenseIdx;
	}
	public String getOssLicenseComb() {
		return ossLicenseComb;
	}
	public void setOssLicenseComb(String ossLicenseComb) {
		this.ossLicenseComb = ossLicenseComb;
	}
	public String getOssLicenseText() {
		return ossLicenseText;
	}
	public void setOssLicenseText(String ossLicenseText) {
		this.ossLicenseText = ossLicenseText;
	}
	public String getOssCopyright() {
		return ossCopyright;
	}
	public void setOssCopyright(String ossCopyright) {
		this.ossCopyright = ossCopyright;
	}
	public String getLicenseName() {
		return licenseName;
	}
	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}
	public String getLicenseType() {
		return licenseType;
	}
	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}
	public String getObligation() {
		return obligation;
	}
	public void setObligation(String obligation) {
		this.obligation = obligation;
	}
	public String getObligationChecks() {
		return obligationChecks;
	}
	public void setObligationChecks(String obligationChecks) {
		this.obligationChecks = obligationChecks;
	}
	public String getLicenseNameEx() {
		return licenseNameEx;
	}
	public void setLicenseNameEx(String licenseNameEx) {
		this.licenseNameEx = licenseNameEx;
	}
	public String getRestriction() {
		return restriction;
	}
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
}
