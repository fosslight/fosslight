/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import oss.fosslight.common.CommonFunction;

public class ProjectIdentification extends ComBean implements Serializable, Comparable<ProjectIdentification>, Cloneable {
	
	@Override
	public String toString() {
		return "ProjectIdentification [componentId=" + componentId + ", referenceId=" + referenceId + ", referenceDiv="
				+ referenceDiv + ", ossId=" + ossId + ", ossName=" + ossName + ", ossVersion=" + ossVersion
				+ ", downloadLocation=" + downloadLocation + ", homepage=" + homepage + ", mExcludeYn=" + mExcludeYn
				+ ", filePath=" + filePath + ", binaryName=" + binaryName + ", binarySize=" + binarySize
				+ ", binaryNotice=" + binaryNotice + ", refPartnerId=" + refPartnerId + ", reportFileId=" + reportFileId
				+ ", excludeYn=" + excludeYn + ", mergePreDiv=" + mergePreDiv + ", componentLicenseId="
				+ componentLicenseId + ", licenseId=" + licenseId + ", licenseName=" + licenseName + ", licenseText="
				+ licenseText + ", copyrightText=" + copyrightText + ", sExcludeYn=" + sExcludeYn + ", mergeOrder="
				+ mergeOrder + ", licenseDiv=" + licenseDiv + ", ossLicenseComb=" + ossLicenseComb + ", ossCopyright="
				+ ossCopyright + ", grpCnt=" + grpCnt + ", merge=" + merge + ", groupingColumn=" + groupingColumn
				+ ", androidNoticeFileId=" + androidNoticeFileId + ", androidResultFileId=" + androidResultFileId
				+ ", loadFromAndroidProjectFlag=" + loadFromAndroidProjectFlag + ", refPrjId="+ refPrjId
				+ ", obli=" + obli + "]";
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2960062822605199127L;

	/** The component id. */
	// OSS_COMPONENTS
	private String componentId;
	
	/** The reference id. */
	private String referenceId;
	
	/** The reference div. */
	private String referenceDiv;
	
	/** The oss id. */
	private String ossId;
	
	/** The oss name. */
	private String ossName;
	
	/** The check name */
	private String checkName;

	/** The check license */
	private String checkLicense;

	/** The check license and name evidence */
	private String checkedEvidence;
	private String checkedEvidenceType;

	/** The oss name exists yn. */
	private String ossNameExistsYn;
	
	/** The oss version. */
	private String ossVersion;
	
	/** The version diff flag. */
	private String versionDiffFlag;
	
	/** The download location. */
	private String downloadLocation;
	
	/** The homepage. */
	private String homepage;
	
	/** The m exclude yn. */
	private String mExcludeYn;
	
	/** The file path. */
	private String filePath;
	
	/** The binary name. */
	private String binaryName;
	
	/** The binary size. */
	private String binarySize;
	
	/** The binary notice. */
	private String binaryNotice;
	
	/** The custom binary yn. */
	private String customBinaryYn;
	
	/** The ref partner id. */
	private String refPartnerId;
	
	/** The ref partner id. */
	private String refPartnerName;
	
	/** The ref prj id. */
	private String refPrjId;
	
	/** The ref bat id. */
	private String refBatId;
	
	/** The report file id. */
	private String reportFileId;
	
	/** The exclude yn. */
	private String excludeYn;
	
	/** The merge pre div. */
	private String mergePreDiv;
	
	/** The prev obligation type. */
	private String preObligationType;
	
	/** The obligation type. */
	private String obligationType;

	/** The obligation license. */
	private String obligationLicense;
	
	/** The notify. */
	private String notify;
	
	/** The source. */
	private String source;
	
	/** The bat str plus. */
	private String batStrPlus;
	
	/** The bat string match percentage. */
	private String batStringMatchPercentage;

	/** The bat string match percentage float. */
	private String batStringMatchPercentageFloat;
	
	/** The bat percentage. */
	private String batPercentage;
	
	/** The bat score. */
	private String batScore; 
	
	/** The bat checksum. */
	private String batChecksum; 
	
	
	/** The ref component id. */
	private String refComponentId;
	
	/** The ref div. */
	private String refDiv; 
	
	/** The component license id. */
	// OSS_COMPONENTS_LICENSE
	private String componentLicenseId;
	
	/** The license id. */
	private String licenseId;
	
	/** The license name. */
	private String licenseName;
	
	/** The license name exists yn. */
	private String licenseNameExistsYn;
	
	private String licenseUserGuideYn;
	private String licenseUserGuideStr;
	
	/** The license text. */
	private String licenseText;
	
	/** The copyright text. */
	private String copyrightText;
	
	/** The s exclude yn. */
	private String sExcludeYn;
	
	/** The vuln yn. */
	private String vulnYn;
	
	/** The cvss score. */
	private String cvssScore;
	/** The cvss score. */
	private String cvssScore2;
	
	private String cvssScoreMax;
	
	private String cvssScoreMax1;

	private String cvssScoreMax2;

	private String cvssScoreMax3;
	
	private String cvssScoreMax4;
	
	private String cvssScoreMax5;
	
	/** The cve id. */
	private String cveId;
	
	/** The merge order. */
	// OTHER
	private String mergeOrder;
	
	/** The role out license. */
	private String roleOutLicense;
	
	/** The role out license id list. */
	private List<String> roleOutLicenseIdList;
	
	/** The sort android flag. */
	private String sortAndroidFlag;
	
	/** The bom with android flag. */
	private String bomWithAndroidFlag;

	/** The license div. */
	// OSS_MASTER
	private String licenseDiv;

	/** The oss license comb. */
	// OSS_LICENSE
	private String ossLicenseComb;
	
	/** The oss copyright. */
	private String ossCopyright;
	
	/** The license type. */
	private String licenseType;
	
	/** The editable. */
	private String editable;
	
	/** The grp cnt. */
	// OTHER
	private String grpCnt;
	
	/** The merge. */
	private String merge;
	
	/** The notice flag. */
	private String noticeFlag;
	
	/** The main data. */
	private String mainData;
	
	/** The sub data. */
	private String subData;
	
	/** The main data grid list. */
	private List<ProjectIdentification> mainDataGridList;
	
	/** The sub data grid list. */
	private List<List<ProjectIdentification>> subDataGridList;
	
	/** The grouping column. */
	//Grouping
	private String groupingColumn;
	
	/** The obli. */
	private String obli;
	
	/** The save bom flag. */
	private String saveBomFlag;
	
	/** The component id list. */
	private List<String> componentIdList;
	
	/** The component license list. */
	private List<ProjectIdentification> componentLicenseList;
	
	/** The oss components license list. */
	private List<OssComponentsLicense> ossComponentsLicenseList;
	
	/**  Bin(Android)에서 다른 프로젝트에서 laod하는 경우. */
	private String loadFromAndroidProjectFlag;
	
	/** The android csv file id. */
	private String androidCsvFileId;
	
	/** The android notice file id. */
	private String androidNoticeFileId;
	
	/** The android result file id. */
	private String androidResultFileId;
	
	/** The download report flag. */
	private String downloadReportFlag;
	
	/** The component idx. */
	private String componentIdx;
	
	/** The ref component idx. */
	private String refComponentIdx;  			/* 2018-07-17 choye 추가  */
	
	/** The rnum. */
	public String rnum;
	
	/** The vulnerability. */
	private String vulnerability;
	
	private String comments;
	private String incCommentsFlag;
	
	/**
	 * self check 불명확한 oss또는 license에 대해서 obligation 표시 여부
	 */
	private String obligationGrayFlag;
	private String obligationMsg;
	
	/** The oss copyright. */
	private String copyright;
	
	/** The partnerId. */
	private String partnerId;
	
	/** The Restriction. */
	private String restriction;
	
	private String guireportFlag = "N";
	
	private String typeFlag = "N";
	
	private String bulkRegistYn = "N";
	
	private String licenseTypeIdx;
	
	private String adminCheckYn;
	
	private String ossNickName;
	
	private String attribution;
	
	private String ossVersionEmptyFlag;

	/** Check whether the Object is included in the oss list */
	private String checkOssList = "N";
	private String publDate;
	private String patchLink;
	
	private String dependencies;
	private String refOssName;
	
	public String getRedirectLocation() {
		return redirectLocation;
	}

	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

	private String redirectLocation;

	public String getRecommendedNickname() {
		return recommendedNickname;
	}

	public void setRecommendedNickname(String recommendedNickname) {
		this.recommendedNickname = recommendedNickname;
	}

	private String recommendedNickname;

	public String getGuireportFlag() {
		return guireportFlag;
	}

	public void setGuireportFlag(String guireportFlag) {
		this.guireportFlag = guireportFlag;
	}

	public String getObligationGrayFlag() {
		return obligationGrayFlag;
	}

	public void setObligationGrayFlag(String obligationGrayFlag) {
		this.obligationGrayFlag = obligationGrayFlag;
	}
	
	

	public String getObligationMsg() {
		return obligationMsg;
	}

	public void setObligationMsg(String obligationMsg) {
		this.obligationMsg = obligationMsg;
	}

	/**
	 * Gets the check oss list.
	 *
	 * @return the check oss list
	 */
	public String getCheckOssList(){
		return checkOssList;
	}

	/**
	 * Sets the check oss list.
	 *
	 * @param checkOssList the new check oss list
	 */
	public void setCheckOssList(String checkOssList){
		this.checkOssList = checkOssList;
	}

	/**
	 * Gets the component idx.
	 *
	 * @return the component idx
	 */
	public String getComponentIdx() {
		return componentIdx;
	}

	/**
	 * Sets the component idx.
	 *
	 * @param componentIdx the new component idx
	 */
	public void setComponentIdx(String componentIdx) {
		this.componentIdx = componentIdx;
	}
	
	
	/* 2018-07-17 choye 추가  */
	/**
	 * Gets the ref component idx.
	 *
	 * @return the ref component idx
	 */
	public String getRefComponentIdx() {
		return refComponentIdx;
	}

	/**
	 * Sets the reg component idx.
	 *
	 * @param ref componentIdx the new ref component idx
	 */
	public void setRefComponentIdx(String refComponentIdx) {
		this.refComponentIdx = refComponentIdx;
	}

	/**
	 * Gets the component id.
	 *
	 * @return the component id
	 */
	public String getComponentId() {
		return componentId;
	}

	/**
	 * Sets the component id.
	 *
	 * @param componentId the new component id
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	/**
	 * Gets the reference id.
	 *
	 * @return the reference id
	 */
	public String getReferenceId() {
		return referenceId;
	}

	/**
	 * Sets the reference id.
	 *
	 * @param referenceId the new reference id
	 */
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	/**
	 * Gets the reference div.
	 *
	 * @return the reference div
	 */
	public String getReferenceDiv() {
		return referenceDiv;
	}

	/**
	 * Sets the reference div.
	 *
	 * @param referenceDiv the new reference div
	 */
	public void setReferenceDiv(String referenceDiv) {
		this.referenceDiv = referenceDiv;
	}

	/**
	 * Gets the oss id.
	 *
	 * @return the oss id
	 */
	public String getOssId() {
		return ossId;
	}

	/**
	 * Sets the oss id.
	 *
	 * @param ossId the new oss id
	 */
	public void setOssId(String ossId) {
		this.ossId = ossId;
	}

	/**
	 * Gets the oss name.
	 *
	 * @return the oss name
	 */
	public String getOssName() {
		return ossName;
	}

	/**
	 * Sets the oss name.
	 *
	 * @param ossName the new oss name
	 */
	public void setOssName(String ossName) {
		this.ossName = ossName;
	}
	
	/**
	 * Gets the check name.
	 *
	 * @return the check name
	 */
	public String getCheckName() {
		return checkName;
	}
	
	/**
	 * Sets the check name.
	 *
	 * @param checkName the new check name
	 */
	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}

	/**
	 * Gets the check license.
	 *
	 * @return the check license
	 */
	public String getCheckLicense() {
		return checkLicense;
	}

	/**
	 * Sets the check license.
	 *
	 * @param checkLicense the new check license
	 */
	public void setCheckLicense(String checkLicense) {
		this.checkLicense = checkLicense;
	}

	/**
	 * Gets the oss version.
	 *
	 * @return the oss version
	 */
	public String getOssVersion() {
		return ossVersion;
	}

	/**
	 * Sets the oss version.
	 *
	 * @param ossVersion the new oss version
	 */
	public void setOssVersion(String ossVersion) {
		this.ossVersion = ossVersion;
	}

	/**
	 * Gets the version diff flag.
	 *
	 * @return the version diff flag
	 */
	public String getVersionDiffFlag() {
		return versionDiffFlag;
	}
	
	/**
	 * Sets the version diff flag.
	 *
	 * @param versionDiffFlag the new version diff flag
	 */
	public void setVersionDiffFlag(String versionDiffFlag) {
		this.versionDiffFlag = versionDiffFlag;
	}

	/**
	 * Gets the download location.
	 *
	 * @return the download location
	 */
	public String getDownloadLocation() {
		return CommonFunction.convertUrlLinkFormat(downloadLocation);
	}

	/**
	 * Sets the download location.
	 *
	 * @param downloadLocation the new download location
	 */
	public void setDownloadLocation(String downloadLocation) {
		this.downloadLocation = downloadLocation;
	}

	/**
	 * Gets the homepage.
	 *
	 * @return the homepage
	 */
	public String getHomepage() {
		return CommonFunction.convertUrlLinkFormat(homepage);
	}

	/**
	 * Sets the homepage.
	 *
	 * @param homepage the new homepage
	 */
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	/**
	 * Gets the m exclude yn.
	 *
	 * @return the m exclude yn
	 */
	public String getmExcludeYn() {
		return mExcludeYn;
	}

	/**
	 * Sets the m exclude yn.
	 *
	 * @param mExcludeYn the new m exclude yn
	 */
	public void setmExcludeYn(String mExcludeYn) {
		this.mExcludeYn = mExcludeYn;
	}

	/**
	 * Gets the binary name.
	 *
	 * @return the binary name
	 */
	public String getBinaryName() {
		return binaryName;
	}

	/**
	 * Sets the binary name.
	 *
	 * @param binaryName the new binary name
	 */
	public void setBinaryName(String binaryName) {
		this.binaryName = binaryName;
	}

	/**
	 * Gets the binary size.
	 *
	 * @return the binary size
	 */
	public String getBinarySize() {
		return binarySize;
	}

	/**
	 * Sets the binary size.
	 *
	 * @param binarySize the new binary size
	 */
	public void setBinarySize(String binarySize) {
		this.binarySize = binarySize;
	}

	/**
	 * Gets the binary notice.
	 *
	 * @return the binary notice
	 */
	public String getBinaryNotice() {
		return binaryNotice;
	}

	/**
	 * Sets the binary notice.
	 *
	 * @param binaryNotice the new binary notice
	 */
	public void setBinaryNotice(String binaryNotice) {
		this.binaryNotice = binaryNotice;
	}

	/**
	 * Gets the component license id.
	 *
	 * @return the component license id
	 */
	public String getComponentLicenseId() {
		return componentLicenseId;
	}

	/**
	 * Sets the component license id.
	 *
	 * @param componentLicenseId the new component license id
	 */
	public void setComponentLicenseId(String componentLicenseId) {
		this.componentLicenseId = componentLicenseId;
	}

	/**
	 * Gets the license id.
	 *
	 * @return the license id
	 */
	public String getLicenseId() {
		return licenseId;
	}

	/**
	 * Sets the license id.
	 *
	 * @param licenseId the new license id
	 */
	public void setLicenseId(String licenseId) {
		this.licenseId = licenseId;
	}

	/**
	 * Gets the license name.
	 *
	 * @return the license name
	 */
	public String getLicenseName() {
		return licenseName;
	}

	/**
	 * Sets the license name.
	 *
	 * @param licenseName the new license name
	 */
	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	/**
	 * Gets the license text.
	 *
	 * @return the license text
	 */
	public String getLicenseText() {
		return licenseText;
	}

	/**
	 * Sets the license text.
	 *
	 * @param licenseText the new license text
	 */
	public void setLicenseText(String licenseText) {
		this.licenseText = licenseText;
	}

	/**
	 * Gets the copyright text.
	 *
	 * @return the copyright text
	 */
	public String getCopyrightText() {
		return copyrightText;
	}

	/**
	 * Sets the copyright text.
	 *
	 * @param copyrightText the new copyright text
	 */
	public void setCopyrightText(String copyrightText) {
		this.copyrightText = copyrightText;
	}

	/**
	 * Gets the s exclude yn.
	 *
	 * @return the s exclude yn
	 */
	public String getsExcludeYn() {
		return sExcludeYn;
	}

	/**
	 * Sets the s exclude yn.
	 *
	 * @param sExcludeYn the new s exclude yn
	 */
	public void setsExcludeYn(String sExcludeYn) {
		this.sExcludeYn = sExcludeYn;
	}

	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Sets the file path.
	 *
	 * @param filePath the new file path
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Gets the license div.
	 *
	 * @return the license div
	 */
	public String getLicenseDiv() {
		return licenseDiv;
	}

	/**
	 * Sets the license div.
	 *
	 * @param licenseDiv the new license div
	 */
	public void setLicenseDiv(String licenseDiv) {
		this.licenseDiv = licenseDiv;
	}

	/**
	 * Gets the oss license comb.
	 *
	 * @return the oss license comb
	 */
	public String getOssLicenseComb() {
		return ossLicenseComb;
	}

	/**
	 * Sets the oss license comb.
	 *
	 * @param ossLicenseComb the new oss license comb
	 */
	public void setOssLicenseComb(String ossLicenseComb) {
		this.ossLicenseComb = ossLicenseComb;
	}

	/**
	 * Gets the oss copyright.
	 *
	 * @return the oss copyright
	 */
	public String getOssCopyright() {
		return ossCopyright;
	}

	/**
	 * Sets the oss copyright.
	 *
	 * @param ossCopyright the new oss copyright
	 */
	public void setOssCopyright(String ossCopyright) {
		this.ossCopyright = ossCopyright;
	}

	/**
	 * Gets the grp cnt.
	 *
	 * @return the grp cnt
	 */
	public String getGrpCnt() {
		return grpCnt;
	}

	/**
	 * Sets the grp cnt.
	 *
	 * @param grpCnt the new grp cnt
	 */
	public void setGrpCnt(String grpCnt) {
		this.grpCnt = grpCnt;
	}

	/**
	 * Gets the serialversionuid.
	 *
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Gets the grouping column.
	 *
	 * @return the grouping column
	 */
	public String getGroupingColumn() {
		return groupingColumn;
	}

	/**
	 * Sets the grouping column.
	 *
	 * @param groupingColumn the new grouping column
	 */
	public void setGroupingColumn(String groupingColumn) {
		this.groupingColumn = groupingColumn;
	}

	/**
	 * Gets the merge.
	 *
	 * @return the merge
	 */
	public String getMerge() {
		return merge;
	}

	/**
	 * Sets the merge.
	 *
	 * @param merge the new merge
	 */
	public void setMerge(String merge) {
		this.merge = merge;
	}

	/**
	 * Gets the obli.
	 *
	 * @return the obli
	 */
	public String getObli() {
		return obli;
	}

	/**
	 * Sets the obli.
	 *
	 * @param obli the new obli
	 */
	public void setObli(String obli) {
		this.obli = obli;
	}

	/**
	 * Gets the ref partner id.
	 *
	 * @return the ref partner id
	 */
	public String getRefPartnerId() {
		return refPartnerId;
	}

	/**
	 * Sets the ref partner id.
	 *
	 * @param refPartnerId the new ref partner id
	 */
	public void setRefPartnerId(String refPartnerId) {
		this.refPartnerId = refPartnerId;
	}

	/**
	 * Gets the report file id.
	 *
	 * @return the report file id
	 */
	public String getReportFileId() {
		return reportFileId;
	}

	/**
	 * Sets the report file id.
	 *
	 * @param reportFileId the new report file id
	 */
	public void setReportFileId(String reportFileId) {
		this.reportFileId = reportFileId;
	}

	/**
	 * Gets the exclude yn.
	 *
	 * @return the exclude yn
	 */
	public String getExcludeYn() {
		return excludeYn;
	}

	/**
	 * Sets the exclude yn.
	 *
	 * @param excludeYn the new exclude yn
	 */
	public void setExcludeYn(String excludeYn) {
		this.excludeYn = excludeYn;
	}

	/**
	 * Gets the merge order.
	 *
	 * @return the merge order
	 */
	public String getMergeOrder() {
		return mergeOrder;
	}

	/**
	 * Sets the merge order.
	 *
	 * @param mergeOrder the new merge order
	 */
	public void setMergeOrder(String mergeOrder) {
		this.mergeOrder = mergeOrder;
	}

	/**
	 * Gets the merge pre div.
	 *
	 * @return the merge pre div
	 */
	public String getMergePreDiv() {
		return mergePreDiv;
	}

	/**
	 * Sets the merge pre div.
	 *
	 * @param mergePreDiv the new merge pre div
	 */
	public void setMergePreDiv(String mergePreDiv) {
		this.mergePreDiv = mergePreDiv;
	}

	/**
	 * Gets the vuln yn.
	 *
	 * @return the vuln yn
	 */
	public String getVulnYn() {
		return vulnYn;
	}

	/**
	 * Sets the vuln yn.
	 *
	 * @param vulnYn the new vuln yn
	 */
	public void setVulnYn(String vulnYn) {
		this.vulnYn = vulnYn;
	}

	/**
	 * Gets the license type.
	 *
	 * @return the license type
	 */
	public String getLicenseType() {
		return licenseType;
	}

	/**
	 * Sets the license type.
	 *
	 * @param licenseType the new license type
	 */
	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}
	
	public String getPreObligationType() {
		return preObligationType;
	}

	public void setPreObligationType(String preObligationType) {
		this.preObligationType = preObligationType;
	}
	
	/**
	 * Gets the obligation type.
	 *
	 * @return the obligation type
	 */
	public String getObligationType() {
		return obligationType;
	}

	/**
	 * Sets the obligation type.
	 *
	 * @param obligationType the new obligation type
	 */
	public void setObligationType(String obligationType) {
		this.obligationType = obligationType;
	}

	/**
	 * Gets the obligation license.
	 *
	 * @return the obligation license
	 */
	public String getObligationLicense() {
		return obligationLicense;
	}

	/**
	 * Sets the obligation license.
	 *
	 * @param obligationLicense the new obligation license
	 */
	public void setObligationLicense(String obligationLicense) {
		this.obligationLicense = obligationLicense;
	}
	
	/**
	 * Gets the bat string match percentage.
	 *
	 * @return the bat string match percentage
	 */
	public String getBatStringMatchPercentage() {
		return batStringMatchPercentage;
	}

	/**
	 * Sets the bat string match percentage.
	 *
	 * @param batStringMatchPercentage the new bat string match percentage
	 */
	public void setBatStringMatchPercentage(String batStringMatchPercentage) {
		this.batStringMatchPercentage = batStringMatchPercentage;
	}
	
	

	public String getBatStringMatchPercentageFloat() {
		return batStringMatchPercentageFloat;
	}

	public void setBatStringMatchPercentageFloat(String batStringMatchPercentageFloat) {
		this.batStringMatchPercentageFloat = batStringMatchPercentageFloat;
	}

	/**
	 * Gets the bat percentage.
	 *
	 * @return the bat percentage
	 */
	public String getBatPercentage() {
		return batPercentage;
	}

	/**
	 * Sets the bat percentage.
	 *
	 * @param batPercentage the new bat percentage
	 */
	public void setBatPercentage(String batPercentage) {
		this.batPercentage = batPercentage;
	}

	/**
	 * Gets the bat score.
	 *
	 * @return the bat score
	 */
	public String getBatScore() {
		return batScore;
	}

	/**
	 * Sets the bat score.
	 *
	 * @param batScore the new bat score
	 */
	public void setBatScore(String batScore) {
		this.batScore = batScore;
	}

	/**
	 * Gets the bat str plus.
	 *
	 * @return the bat str plus
	 */
	public String getBatStrPlus() {
		return batStrPlus;
	}

	/**
	 * Sets the bat str plus.
	 *
	 * @param batStrPlus the new bat str plus
	 */
	public void setBatStrPlus(String batStrPlus) {
		this.batStrPlus = batStrPlus;
	}

	/**
	 * Gets the cvss score.
	 *
	 * @return the cvss score
	 */
	public String getCvssScore() {
		return cvssScore;
	}

	/**
	 * Sets the cvss score.
	 *
	 * @param cvssScore the new cvss score
	 */
	public void setCvssScore(String cvssScore) {
		this.cvssScore = cvssScore;
	}

	/**
	 * Gets the role out license.
	 *
	 * @return the role out license
	 */
	public String getRoleOutLicense() {
		return roleOutLicense;
	}

	/**
	 * Sets the role out license.
	 *
	 * @param roleOutLicense the new role out license
	 */
	public void setRoleOutLicense(String roleOutLicense) {
		this.roleOutLicense = roleOutLicense;
	}

	/**
	 * Gets the ref component id.
	 *
	 * @return the ref component id
	 */
	public String getRefComponentId() {
		return refComponentId;
	}

	/**
	 * Sets the ref component id.
	 *
	 * @param refComponentId the new ref component id
	 */
	public void setRefComponentId(String refComponentId) {
		this.refComponentId = refComponentId;
	}

	/**
	 * Gets the ref div.
	 *
	 * @return the ref div
	 */
	public String getRefDiv() {
		return refDiv;
	}

	/**
	 * Sets the ref div.
	 *
	 * @param refDiv the new ref div
	 */
	public void setRefDiv(String refDiv) {
		this.refDiv = refDiv;
	}

	/**
	 * Gets the component id list.
	 *
	 * @return the component id list
	 */
	public List<String> getComponentIdList() {
		return componentIdList;
	}

	/**
	 * Sets the component id list.
	 *
	 * @param componentIdList the new component id list
	 */
	public void setComponentIdList(List<String> componentIdList) {
		this.componentIdList = componentIdList;
	}

	/**
	 * Adds the component id list.
	 *
	 * @param s the s
	 */
	public void addComponentIdList(String s) {
		if (this.componentIdList == null) {
			this.componentIdList = new ArrayList<>();
		}
		this.componentIdList.add(s);
	}

	/**
	 * Gets the component license list.
	 *
	 * @return the component license list
	 */
	public List<ProjectIdentification> getComponentLicenseList() {
		return componentLicenseList;
	}

	/**
	 * Sets the component license list.
	 *
	 * @param componentLicenseList the new component license list
	 */
	public void setComponentLicenseList(List<ProjectIdentification> componentLicenseList) {
		this.componentLicenseList = componentLicenseList;
	}
	
	/**
	 * Adds the component license list.
	 *
	 * @param license the license
	 */
	public void addComponentLicenseList(ProjectIdentification license) {
		if (this.componentLicenseList == null) {
			this.componentLicenseList = new ArrayList<>();
		}
		this.componentLicenseList.add(license);
	}

	/**
	 * Gets the oss components license list.
	 *
	 * @return the oss components license list
	 */
	public List<OssComponentsLicense> getOssComponentsLicenseList() {
		return ossComponentsLicenseList;
	}

	/**
	 * Sets the oss components license list.
	 *
	 * @param ossComponentsLicenseList the new oss components license list
	 */
	public void setOssComponentsLicenseList(List<OssComponentsLicense> ossComponentsLicenseList) {
		this.ossComponentsLicenseList = ossComponentsLicenseList;
	}
	
	/**
	 * Adds the oss components license.
	 *
	 * @param bean the bean
	 */
	public void addOssComponentsLicense(OssComponentsLicense bean) {
		if (this.ossComponentsLicenseList == null) {
			this.ossComponentsLicenseList = new ArrayList<>();
		}
		this.ossComponentsLicenseList.add(bean);
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the notify.
	 *
	 * @return the notify
	 */
	public String getNotify() {
		return notify;
	}

	/**
	 * Sets the notify.
	 *
	 * @param notify the new notify
	 */
	public void setNotify(String notify) {
		this.notify = notify;
	}

	/**
	 * Gets the ref prj id.
	 *
	 * @return the ref prj id
	 */
	public String getRefPrjId() {
		return refPrjId;
	}

	/**
	 * Sets the ref prj id.
	 *
	 * @param refPrjId the new ref prj id
	 */
	public void setRefPrjId(String refPrjId) {
		this.refPrjId = refPrjId;
	}

	/**
	 * Gets the ref bat id.
	 *
	 * @return the ref bat id
	 */
	public String getRefBatId() {
		return refBatId;
	}

	/**
	 * Sets the ref bat id.
	 *
	 * @param refBatId the new ref bat id
	 */
	public void setRefBatId(String refBatId) {
		this.refBatId = refBatId;
	}

	/**
	 * Gets the editable.
	 *
	 * @return the editable
	 */
	public String getEditable() {
		return editable;
	}

	/**
	 * Sets the editable.
	 *
	 * @param editable the new editable
	 */
	public void setEditable(String editable) {
		this.editable = editable;
	}

	/**
	 * Gets the bat checksum.
	 *
	 * @return the bat checksum
	 */
	public String getBatChecksum() {
		return batChecksum;
	}

	/**
	 * Sets the bat checksum.
	 *
	 * @param batChecksum the new bat checksum
	 */
	public void setBatChecksum(String batChecksum) {
		this.batChecksum = batChecksum;
	}

	/**
	 * Gets the cve id.
	 *
	 * @return the cve id
	 */
	public String getCveId() {
		return cveId;
	}

	/**
	 * Sets the cve id.
	 *
	 * @param cveId the new cve id
	 */
	public void setCveId(String cveId) {
		this.cveId = cveId;
	}

	/**
	 * Gets the rnum.
	 *
	 * @return the rnum
	 */
	public String getRnum() {
		return rnum;
	}

	/**
	 * Sets the rnum.
	 *
	 * @param rnum the new rnum
	 */
	public void setRnum(String rnum) {
		this.rnum = rnum;
	}

	/**
	 * Gets the main data.
	 *
	 * @return the main data
	 */
	public String getMainData() {
		return mainData;
	}

	/**
	 * Sets the main data.
	 *
	 * @param mainData the new main data
	 */
	public void setMainData(String mainData) {
		this.mainData = mainData;
	}

	/**
	 * Gets the sub data.
	 *
	 * @return the sub data
	 */
	public String getSubData() {
		return subData;
	}

	/**
	 * Sets the sub data.
	 *
	 * @param subData the new sub data
	 */
	public void setSubData(String subData) {
		this.subData = subData;
	}

	/**
	 * Gets the main data grid list.
	 *
	 * @return the main data grid list
	 */
	public List<ProjectIdentification> getMainDataGridList() {
		return mainDataGridList;
	}

	/**
	 * Sets the main data grid list.
	 *
	 * @param mainDataGridList the new main data grid list
	 */
	public void setMainDataGridList(List<ProjectIdentification> mainDataGridList) {
		this.mainDataGridList = mainDataGridList;
	}

	/**
	 * Gets the sub data grid list.
	 *
	 * @return the sub data grid list
	 */
	public List<List<ProjectIdentification>> getSubDataGridList() {
		return subDataGridList;
	}

	/**
	 * Sets the sub data grid list.
	 *
	 * @param subDataGridList the new sub data grid list
	 */
	public void setSubDataGridList(List<List<ProjectIdentification>> subDataGridList) {
		this.subDataGridList = subDataGridList;
	}

	/**
	 * Gets the sort android flag.
	 *
	 * @return the sort android flag
	 */
	public String getSortAndroidFlag() {
		return sortAndroidFlag;
	}

	/**
	 * Sets the sort android flag.
	 *
	 * @param sortAndroidFlag the new sort android flag
	 */
	public void setSortAndroidFlag(String sortAndroidFlag) {
		this.sortAndroidFlag = sortAndroidFlag;
	}

	/**
	 * Gets the custom binary yn.
	 *
	 * @return the custom binary yn
	 */
	public String getCustomBinaryYn() {
		return customBinaryYn;
	}

	/**
	 * Sets the custom binary yn.
	 *
	 * @param customBinaryYn the new custom binary yn
	 */
	public void setCustomBinaryYn(String customBinaryYn) {
		this.customBinaryYn = customBinaryYn;
	}

	/**
	 * Gets the save bom flag.
	 *
	 * @return the save bom flag
	 */
	public String getSaveBomFlag() {
		return saveBomFlag;
	}

	/**
	 * Sets the save bom flag.
	 *
	 * @param saveBomFlag the new save bom flag
	 */
	public void setSaveBomFlag(String saveBomFlag) {
		this.saveBomFlag = saveBomFlag;
	}

	/**
	 * Gets the download report flag.
	 *
	 * @return the download report flag
	 */
	public String getDownloadReportFlag() {
		return downloadReportFlag;
	}

	/**
	 * Sets the download report flag.
	 *
	 * @param downloadReportFlag the new download report flag
	 */
	public void setDownloadReportFlag(String downloadReportFlag) {
		this.downloadReportFlag = downloadReportFlag;
	}

	/**
	 * Gets the bom with android flag.
	 *
	 * @return the bom with android flag
	 */
	public String getBomWithAndroidFlag() {
		return bomWithAndroidFlag;
	}

	/**
	 * Sets the bom with android flag.
	 *
	 * @param bomWithAndroidFlag the new bom with android flag
	 */
	public void setBomWithAndroidFlag(String bomWithAndroidFlag) {
		this.bomWithAndroidFlag = bomWithAndroidFlag;
	}

	/**
	 * Gets the load from android project flag.
	 *
	 * @return the load from android project flag
	 */
	public String getLoadFromAndroidProjectFlag() {
		return loadFromAndroidProjectFlag;
	}

	/**
	 * Sets the load from android project flag.
	 *
	 * @param loadFromAndroidProjectFlag the new load from android project flag
	 */
	public void setLoadFromAndroidProjectFlag(String loadFromAndroidProjectFlag) {
		this.loadFromAndroidProjectFlag = loadFromAndroidProjectFlag;
	}

	/**
	 * Gets the android csv file id.
	 *
	 * @return the android csv file id
	 */
	public String getAndroidCsvFileId() {
		return androidCsvFileId;
	}

	/**
	 * Sets the android csv file id.
	 *
	 * @param androidCsvFileId the new android csv file id
	 */
	public void setAndroidCsvFileId(String androidCsvFileId) {
		this.androidCsvFileId = androidCsvFileId;
	}
	
	/**
	 * Gets the android notice file id.
	 *
	 * @return the android notice file id
	 */
	public String getAndroidNoticeFileId() {
		return androidNoticeFileId;
	}

	/**
	 * Sets the android notice file id.
	 *
	 * @param androidNoticeFileId the new android notice file id
	 */
	public void setAndroidNoticeFileId(String androidNoticeFileId) {
		this.androidNoticeFileId = androidNoticeFileId;
	}

	/**
	 * Gets the android result file id.
	 *
	 * @return the android result file id
	 */
	public String getAndroidResultFileId() {
		return androidResultFileId;
	}

	/**
	 * Sets the android result file id.
	 *
	 * @param androidResultFileId the new android result file id
	 */
	public void setAndroidResultFileId(String androidResultFileId) {
		this.androidResultFileId = androidResultFileId;
	}

	/**
	 * Gets the notice flag.
	 *
	 * @return the notice flag
	 */
	public String getNoticeFlag() {
		return noticeFlag;
	}

	/**
	 * Sets the notice flag.
	 *
	 * @param noticeFlag the new notice flag
	 */
	public void setNoticeFlag(String noticeFlag) {
		this.noticeFlag = noticeFlag;
	}

	/**
	 * Gets the role out license id list.
	 *
	 * @return the role out license id list
	 */
	public List<String> getRoleOutLicenseIdList() {
		return roleOutLicenseIdList;
	}

	/**
	 * Sets the role out license id list.
	 *
	 * @param roleOutLicenseIdList the new role out license id list
	 */
	public void setRoleOutLicenseIdList(List<String> roleOutLicenseIdList) {
		this.roleOutLicenseIdList = roleOutLicenseIdList;
	}
	
	/**
	 * Gets the vulnerability.
	 *
	 * @return the vulnerability
	 */
	public String getVulnerability() {
		return vulnerability;
	}

	/**
	 * Sets the vulnerability.
	 *
	 * @param vulnerability the new vulnerability
	 */
	public void setVulnerability(String vulnerability) {
		this.vulnerability = vulnerability;
	}

	/**
	 * Gets the license name exists yn.
	 *
	 * @return the license name exists yn
	 */
	public String getLicenseNameExistsYn() {
		return licenseNameExistsYn;
	}

	/**
	 * Sets the license name exists yn.
	 *
	 * @param licenseNameExistsYn the new license name exists yn
	 */
	public void setLicenseNameExistsYn(String licenseNameExistsYn) {
		this.licenseNameExistsYn = licenseNameExistsYn;
	}

	/**
	 * Gets the oss name exists yn.
	 *
	 * @return the oss name exists yn
	 */
	public String getOssNameExistsYn() {
		return ossNameExistsYn;
	}

	/**
	 * Sets the oss name exists yn.
	 *
	 * @param ossNameExistsYn the new oss name exists yn
	 */
	public void setOssNameExistsYn(String ossNameExistsYn) {
		this.ossNameExistsYn = ossNameExistsYn;
	}

	/**
	 * @return the licenseUserGuideYn
	 */
	public String getLicenseUserGuideYn() {
		return licenseUserGuideYn;
	}

	/**
	 * @param licenseUserGuideYn the licenseUserGuideYn to set
	 */
	public void setLicenseUserGuideYn(String licenseUserGuideYn) {
		this.licenseUserGuideYn = licenseUserGuideYn;
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

	/**
	 * @return the incCommentsFlag
	 */
	public String getIncCommentsFlag() {
		return incCommentsFlag;
	}

	/**
	 * @param incCommentsFlag the incCommentsFlag to set
	 */
	public void setIncCommentsFlag(String incCommentsFlag) {
		this.incCommentsFlag = incCommentsFlag;
	}

	public String getLicenseUserGuideStr() {
		return licenseUserGuideStr;
	}

	public void setLicenseUserGuideStr(String licenseUserGuideStr) {
		this.licenseUserGuideStr = licenseUserGuideStr;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getRestriction() {
		return restriction;
	}

	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}

	public String getCvssScore2() {
		return cvssScore2;
	}

	public void setCvssScore2(String cvssScore2) {
		this.cvssScore2 = cvssScore2;
	}
	
	public String getTypeFlag() {
		return typeFlag;
	}

	public void setTypeFlag(String typeFlag) {
		this.typeFlag = typeFlag;
	}

	public String getBulkRegistYn() {
		return bulkRegistYn;
	}
	
	public void setBulkRegistYn(String bulkRegistYn) {
		this.bulkRegistYn = bulkRegistYn;
	}	
	
	public String getLicenseTypeIdx() {
		return licenseTypeIdx;
	}

	public void setLicenseTypeIdx(String licenseTypeIdx) {
		this.licenseTypeIdx = licenseTypeIdx;
	}
	
	public String getCvssScoreMax() {
		return cvssScoreMax;
	}

	public void setCvssScoreMax(String cvssScoreMax) {
		this.cvssScoreMax = cvssScoreMax;
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

	@Override
	public int compareTo(ProjectIdentification o) {
		return this.refDiv.compareTo(o.refDiv);
	}
	
	public String getOssNickName() {
		return ossNickName;
	}

	public void setOssNickName(String ossNickName) {
		this.ossNickName = ossNickName;
	}
	
	public ProjectIdentification copy() throws CloneNotSupportedException{
		return (ProjectIdentification)super.clone();
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getCheckedEvidenceType() {
		return checkedEvidenceType;
	}

	public void setCheckedEvidenceType(String checkedEvidenceType) {
		this.checkedEvidenceType = checkedEvidenceType;
	}

	public String getCheckedEvidence() {
		return checkedEvidence;
	}

	public void setCheckedEvidence(String checkedEvidence) {
		this.checkedEvidence = checkedEvidence;
    }

	public String getCvssScoreMax1() {
		return cvssScoreMax1;
	}

	public void setCvssScoreMax1(String cvssScoreMax1) {
		this.cvssScoreMax1 = cvssScoreMax1;
	}

	public String getCvssScoreMax2() {
		return cvssScoreMax2;
	}

	public void setCvssScoreMax2(String cvssScoreMax2) {
		this.cvssScoreMax2 = cvssScoreMax2;
	}

	public String getCvssScoreMax3() {
		return cvssScoreMax3;
	}

	public void setCvssScoreMax3(String cvssScoreMax3) {
		this.cvssScoreMax3 = cvssScoreMax3;
	}
	
	public String getCvssScoreMax4() {
		return cvssScoreMax4;
	}

	public void setCvssScoreMax4(String cvssScoreMax4) {
		this.cvssScoreMax4 = cvssScoreMax4;
	}

	public String getCvssScoreMax5() {
		return cvssScoreMax5;
	}

	public void setCvssScoreMax5(String cvssScoreMax5) {
		this.cvssScoreMax5 = cvssScoreMax5;
	}

	public String getPublDate() {
		return publDate;
	}

	public void setPublDate(String publDate) {
		this.publDate = publDate;
	}

	public String getPatchLink() {
		return patchLink;
	}

	public void setPatchLink(String patchLink) {
		this.patchLink = patchLink;
	}

	public String getOssVersionEmptyFlag() {
		return ossVersionEmptyFlag;
	}

	public void setOssVersionEmptyFlag(String ossVersionEmptyFlag) {
		this.ossVersionEmptyFlag = ossVersionEmptyFlag;
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
