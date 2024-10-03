/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;

public class OssComponentsLicense extends ComBean implements Serializable {

	@Serial
	private static final long serialVersionUID = 4907819340622537583L;
	
	private String componentLicenseId;
	private String componentId;
	private String licenseId;
	private String licenseName;
	private String licenseText;
	private String copyrightText;
	private String excludeYn;
	private String licensetype;
	private String lastComponentsId;
	private String editable;
	private String attribution;					//attribution
	private String webpage;
	
	private String obligationType;
	
	private String ossId;

	private String ossLicenseComb;			//라이센스
	public String getComponentLicenseId() {
		return componentLicenseId;
	}
	public void setComponentLicenseId(String componentLicenseId) {
		this.componentLicenseId = componentLicenseId;
	}
	public String getComponentId() {
		return componentId;
	}
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	public String getLicenseId() {
		return licenseId;
	}
	public void setLicenseId(String licenseId) {
		this.licenseId = licenseId;
	}
	public String getLicenseName() {
		return licenseName;
	}
	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}
	public String getLicenseText() {
		return licenseText;
	}
	public void setLicenseText(String licenseText) {
		this.licenseText = licenseText;
	}
	public String getCopyrightText() {
		return copyrightText;
	}
	public void setCopyrightText(String copyrightText) {
		this.copyrightText = copyrightText;
	}
	public String getExcludeYn() {
		return excludeYn;
	}
	public void setExcludeYn(String excludeYn) {
		this.excludeYn = excludeYn;
	}
	
	
	public String getLicensetype() {
		return licensetype;
	}
	public void setLicensetype(String licensetype) {
		this.licensetype = licensetype;
	}
	@Override
	public String toString() {
		return "OssComponentsLicense [componentLicenseId=" + componentLicenseId + ", componentId=" + componentId
				+ ", licenseId=" + licenseId + ", licenseName=" + licenseName + ", licenseText=" + licenseText
				+ ", copyrightText=" + copyrightText + ", excludeYn=" + excludeYn + "]";
	}
	public String getLastComponentsId() {
		return lastComponentsId;
	}
	public void setLastComponentsId(String lastComponentsId) {
		this.lastComponentsId = lastComponentsId;
	}
	public String getOssLicenseComb() {
		return ossLicenseComb;
	}
	public void setOssLicenseComb(String ossLicenseComb) {
		this.ossLicenseComb = ossLicenseComb;
	}
	public String getEditable() {
		return editable;
	}
	public void setEditable(String editable) {
		this.editable = editable;
	}
	public String getAttribution() {
		return attribution;
	}
	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}
	public String getObligationType() {
		return obligationType;
	}
	public void setObligationType(String obligationType) {
		this.obligationType = obligationType;
	}
	public String getOssId() {
		return ossId;
	}
	public void setOssId(String ossId) {
		this.ossId = ossId;
	}
	public String getWebpage() {
		return webpage;
	}
	public void setWebpage(String webpage) {
		this.webpage = webpage;
	}
	
}