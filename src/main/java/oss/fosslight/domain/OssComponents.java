/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import oss.fosslight.common.CommonFunction;

public class OssComponents extends ComBean implements Serializable {

	private static final long serialVersionUID = 4907819340622537583L;

	private String componentId;
	private String referenceId;
	private String referenceDiv;
	private String componentIdx;	/* 2018-07-10 choye 추가  */
	private String ossId;
	private String ossName;
	private String ossVersion;
	private String ossCopyright;
	private String downloadLocation;
	private String homepage;
	private String excludeYn;
	private String binaryName;
	private String binarySize;
	/** bin(android) 의 binary txt파일의 source code path 정보 */
	private String sourceCodePath;
	private String binaryNotice;
	private String customBinaryYn;
	private String filePath;
	private String mergePreDiv;
	private String obligationType;
	private String refPartnerId;
	private String refPartnerName;
	private String refPrjId;
	private List<OssComponentsLicense> ossComponentsLicense;
	private String vulnYn;
	private String cvssScore;
	private String cveId;
	
	private String oldSystemFlag;

	private String spdxIdentifier;


	/** The check sum. */
	private String batChecksum;
	
	private String verifyFileCount;

	private String batStringMatchPercentage;
	private String batPercentage;
	private String batScore;

	// ossComponentsLicense
	private String componentLicenseId;
	private String licenseId;
	private String licenseName;
	private String licenseText;
	private String licenseType;
	private String copyrightText;
	private String attribution;					//attribution
	private String ossAttribution;					//attribution

	private String prjName;
	private String prjVersion;
	private String prjId;
	private String distributeTarget;
	
	private List<String> ossComponentsIdList;
	private String refComponentId;
	
	private String checkSum;
	private String tlsh;
	private String comments;
	

	private String guireportFlag = "N";
	private String activateFlag;
	
	/**
	 * son system final list report용
	 */
	private String reportKey;
	
	/**
	 * son system final list ref 대상
	 */
	private List<String> finalListRefList;

	/**
	 * colspan
	 * @return
	 */
	private String rowspan;
	
	private String adminCheckYn;
	
	private String androidFlag;

	private String ossNickName;
	
	private String publDate;
	private String vulnerabilityResolution;
	private String vulnerabilityLink;
	private String officialPatchLink;
	private String securityPatchLink;
	private String securityComments;
	private String cpeName;
	private String verStartEndRange;
	private String cveIdTo;
	private String cvssScoreTo;
	
	private String dependencies;
	private String refOssName;
	
	public String getPublDate() {
		return publDate;
	}

	public void setPublDate(String publDate) {
		this.publDate = publDate;
	}

	public String getVulnerabilityResolution() {
		return vulnerabilityResolution;
	}

	public void setVulnerabilityResolution(String vulnerabilityResolution) {
		this.vulnerabilityResolution = vulnerabilityResolution;
	}

	public String getOfficialPatchLink() {
		return officialPatchLink;
	}

	public void setOfficialPatchLink(String officialPatchLink) {
		this.officialPatchLink = officialPatchLink;
	}

	public String getSecurityPatchLink() {
		return securityPatchLink;
	}

	public void setSecurityPatchLink(String securityPatchLink) {
		this.securityPatchLink = securityPatchLink;
	}

	public String getSecurityComments() {
		return securityComments;
	}

	public void setSecurityComments(String securityComments) {
		this.securityComments = securityComments;
	}

	/* 2018-07-10 choye 추가  */
	public String getComponentIdx() {
		return componentIdx;
	}

	public void setComponentIdx(String componentIdx) {
		this.componentIdx = componentIdx;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getReferenceDiv() {
		return referenceDiv;
	}

	public void setReferenceDiv(String referenceDiv) {
		this.referenceDiv = referenceDiv;
	}
	
	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getOssId() {
		return ossId;
	}

	public void setOssId(String ossId) {
		this.ossId = ossId;
	}

	public String getOssName() {
		return ossName;
	}

	public void setOssName(String ossName) {
		this.ossName = ossName;
	}

	public String getOssVersion() {
		return ossVersion;
	}

	public void setOssVersion(String ossVersion) {
		this.ossVersion = ossVersion;
	}

	public String getOssCopyright() {
		return ossCopyright;
	}

	public void setOssCopyright(String ossCopyright) {
		this.ossCopyright = ossCopyright;
	}

	public String getDownloadLocation() {
		return CommonFunction.convertUrlLinkFormat(downloadLocation);
	}

	public void setDownloadLocation(String downloadLocation) {
		this.downloadLocation = downloadLocation;
	}

	public String getHomepage() {
		return CommonFunction.convertUrlLinkFormat(homepage);
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getExcludeYn() {
		return excludeYn;
	}

	public void setExcludeYn(String excludeYn) {
		this.excludeYn = excludeYn;
	}

	public String getBinaryName() {
		return binaryName;
	}

	public void setBinaryName(String binaryName) {
		this.binaryName = binaryName;
	}

	public String getBinarySize() {
		return binarySize;
	}

	public void setBinarySize(String binarySize) {
		this.binarySize = binarySize;
	}

	public String getBinaryNotice() {
		return binaryNotice;
	}

	public void setBinaryNotice(String binaryNotice) {
		this.binaryNotice = binaryNotice;
	}

	public List<OssComponentsLicense> getOssComponentsLicense() {
		return ossComponentsLicense;
	}

	public void setOssComponentsLicense(List<OssComponentsLicense> ossComponentsLicense) {
		this.ossComponentsLicense = ossComponentsLicense;
	}

	/**
	 * Adds the oss components license.
	 *
	 * @param ossComponentsLicense
	 *            the oss components license
	 */
	public void addOssComponentsLicense(OssComponentsLicense ossComponentsLicense) {
		if (ossComponentsLicense != null) {
			if (this.ossComponentsLicense == null) {
				this.ossComponentsLicense = new ArrayList<>();
			}
			this.ossComponentsLicense.add(ossComponentsLicense);
		}
	}

	public String getComponentLicenseId() {
		return componentLicenseId;
	}

	public void setComponentLicenseId(String componentLicenseId) {
		this.componentLicenseId = componentLicenseId;
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

	@Override
	public String toString() {
		return "OssComponents [componentId=" + componentId + ", referenceId=" + referenceId + ", referenceDiv="
				+ referenceDiv + ", ossId=" + ossId + ", ossName=" + ossName + ", ossVersion=" + ossVersion
				+ ", downloadLocation=" + downloadLocation + ", homepage=" + homepage + ", excludeYn=" + excludeYn
				+ ", binaryName=" + binaryName + ", binarySize=" + binarySize + ", binaryNotice=" + binaryNotice
				+ ", ossComponentsLicense=" + ossComponentsLicense + ", componentLicenseId=" + componentLicenseId
				+ ", licenseId=" + licenseId + ", licenseName=" + licenseName + ", licenseText=" + licenseText
				+ ", copyrightText=" + copyrightText + "]";
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}

	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	public String getMergePreDiv() {
		return mergePreDiv;
	}

	public void setMergePreDiv(String mergePreDiv) {
		this.mergePreDiv = mergePreDiv;
	}

	public String getRefPartnerId() {
		return refPartnerId;
	}

	public void setRefPartnerId(String refPartnerId) {
		this.refPartnerId = refPartnerId;
	}

	public String getRefPrjId() {
		return refPrjId;
	}

	public void setRefPrjId(String refPrjId) {
		this.refPrjId = refPrjId;
	}

	public String getVulnYn() {
		return vulnYn;
	}

	public void setVulnYn(String vulnYn) {
		this.vulnYn = vulnYn;
	}

	public String getBatStringMatchPercentage() {
		return batStringMatchPercentage;
	}

	public void setBatStringMatchPercentage(String batStringMatchPercentage) {
		this.batStringMatchPercentage = batStringMatchPercentage;
	}

	public String getBatPercentage() {
		return batPercentage;
	}

	public void setBatPercentage(String batPercentage) {
		this.batPercentage = batPercentage;
	}

	public String getBatScore() {
		return batScore;
	}

	public void setBatScore(String batScore) {
		this.batScore = batScore;
	}

	public String getObligationType() {
		return obligationType;
	}

	public void setObligationType(String obligationType) {
		this.obligationType = obligationType;
	}

	public List<String> getOssComponentsIdList() {
		return ossComponentsIdList;
	}

	public void setOssComponentsIdList(List<String> ossComponentsIdList) {
		this.ossComponentsIdList = ossComponentsIdList;
	}
	
	public void addOssComponentsIdList(String s) {
		if (this.ossComponentsIdList == null) {
			this.ossComponentsIdList = new ArrayList<>();
		}
		if (!isEmpty(s)) {
			this.ossComponentsIdList.add(s);
		}
	}

	public String getRefComponentId() {
		return refComponentId;
	}

	public void setRefComponentId(String refComponentId) {
		this.refComponentId = refComponentId;
	}

	public List<String> getFinalListRefList() {
		return finalListRefList;
	}

	public void setFinalListRefList(List<String> finalListRefList) {
		this.finalListRefList = finalListRefList;
	}

	public String getReportKey() {
		return reportKey;
	}

	public void setReportKey(String reportKey) {
		this.reportKey = reportKey;
	}

	public String getRowspan() {
		return rowspan;
	}

	public void setRowspan(String rowspan) {
		this.rowspan = rowspan;
	}

	public String getVerifyFileCount() {
		return verifyFileCount;
	}

	public void setVerifyFileCount(String verifyFileCount) {
		this.verifyFileCount = verifyFileCount;
	}

	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	public String getPrjVersion() {
		return prjVersion;
	}

	public void setPrjVersion(String prjVersion) {
		this.prjVersion = prjVersion;
	}

	public String getDistributeTarget() {
		return distributeTarget;
	}

	public void setDistributeTarget(String distributeTarget) {
		this.distributeTarget = distributeTarget;
	}

	public String getBatChecksum() {
		return batChecksum;
	}

	public void setBatChecksum(String batChecksum) {
		this.batChecksum = batChecksum;
	}

	public String getCvssScore() {
		return cvssScore;
	}

	public void setCvssScore(String cvssScore) {
		this.cvssScore = cvssScore;
	}

	public String getCveId() {
		return cveId;
	}

	public void setCveId(String cveId) {
		this.cveId = cveId;
	}

	public String getCustomBinaryYn() {
		return customBinaryYn;
	}

	public void setCustomBinaryYn(String customBinaryYn) {
		this.customBinaryYn = customBinaryYn;
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getOssAttribution() {
		return ossAttribution;
	}

	public void setOssAttribution(String ossAttribution) {
		this.ossAttribution = ossAttribution;
	}

	public String getOldSystemFlag() {
		return oldSystemFlag;
	}

	public void setOldSystemFlag(String oldSystemFlag) {
		this.oldSystemFlag = oldSystemFlag;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	public String getTlsh() {
		return tlsh;
	}

	public void setTlsh(String tlsh) {
		this.tlsh = tlsh;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getGuireportFlag() {
		return guireportFlag;
	}

	public void setGuireportFlag(String guireportFlag) {
		this.guireportFlag = guireportFlag;
	}

	public String getSourceCodePath() {
		return sourceCodePath;
	}

	public void setSourceCodePath(String sourceCodePath) {
		this.sourceCodePath = sourceCodePath;
	}

	public String getRefPartnerName() {
		return refPartnerName;
	}

	public void setRefPartnerName(String refPartnerName) {
		this.refPartnerName = refPartnerName;
	}
	
	public String getAdminCheckYn() {
		return adminCheckYn;
	}

	public void setAdminCheckYn(String adminCheckYn) {
		this.adminCheckYn = adminCheckYn;
	}
	
	public String getAndroidFlag() {
		return androidFlag;
	}

	public void setAndroidFlag(String androidFlag) {
		this.androidFlag = androidFlag;
	}
	
	public String getOssNickName() {
		return ossNickName;
	}

	public void setOssNickName(String ossNickName) {
		this.ossNickName = ossNickName;
	}

	public String getSpdxIdentifier() {
		return spdxIdentifier;
	}

	public void setSpdxIdentifier(String spdxIdentifier) {
		this.spdxIdentifier = spdxIdentifier;
	}

	public String getActivateFlag() {
		return activateFlag;
	}

	public void setActivateFlag(String activateFlag) {
		this.activateFlag = activateFlag;
	}

	public String getVulnerabilityLink() {
		return vulnerabilityLink;
	}

	public void setVulnerabilityLink(String vulnerabilityLink) {
		this.vulnerabilityLink = vulnerabilityLink;
	}

	public String getCpeName() {
		return cpeName;
	}

	public void setCpeName(String cpeName) {
		this.cpeName = cpeName;
	}

	public String getVerStartEndRange() {
		return verStartEndRange;
	}

	public void setVerStartEndRange(String verStartEndRange) {
		this.verStartEndRange = verStartEndRange;
	}

	public String getCveIdTo() {
		return cveIdTo;
	}

	public void setCveIdTo(String cveIdTo) {
		this.cveIdTo = cveIdTo;
	}

	public String getCvssScoreTo() {
		return cvssScoreTo;
	}

	public void setCvssScoreTo(String cvssScoreTo) {
		this.cvssScoreTo = cvssScoreTo;
	}

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public String getRefOssName() {
		return refOssName;
	}

	public void setRefOssName(String refOssName) {
		this.refOssName = refOssName;
	}
}