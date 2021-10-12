/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.List;

public class OssNotice extends ComBean implements Serializable {

	private static final long serialVersionUID = -3729500877991189740L;
	
	private String prjId;
	private String noticeType = "10";
	private String noticeTypeEtc;
	private String companyNameFull;
	private String companyNameShort;
	private String distributionSiteUrl;
	private String email;
	private String appended;
	private String appendedTEXT;
	private String useCompanyNameTitle;
	private String distributedOtherCompany;
	private String mergedOtherOssNotice;
	private String accompaniedSourceCode;
	private String packageJson;
	private List<OssComponents> ossComponents;
	private String packageFileId;
	private String packageFileId2;
	private String packageFileId3;
	private String userComment;

	private String networkServerFlag = "N";
	private String fileType;
	private String simpleNoticeFlag = "N";
	private String withoutVerifyYn;

	/** binary DB 등록 무시처리 */
	private String ignoreBinaryDbFlag;
	
	private String ossDistributionSite;
	private String ossDistributionSiteYn;
	
	private String refDiv;
	private String refDivUnExists;
	
	private String noticeTextFileId;
	private String simpleHtmlFileId;
	private String simpleTextFileId;
	private String spdxSheetFileId;
	private String spdxRdfFileId;
	private String spdxTagFileId;
	private String spdxJsonFileId;
	private String spdxYamlFileId;

	/** OSS_NOTICE_NEW UI 변경 후 사용되는 properties */
	private String editNoticeYn = "N";
	private String editCompanyYn = "N";
	private String editDistributionSiteUrlYn = "N";
	private String editEmailYn = "N";
	private String hideOssVersionYn = "N";
	private String editAppendedYn = "N";
	private String isSimpleNotice;
	private String previewOnly = "N";
	
	/** //OSS_NOTICE_NEW UI 변경 후 사용되는 properties */
	
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	public String getNoticeType() {
		return noticeType;
	}
	public void setNoticeType(String noticeType) {
		this.noticeType = noticeType;
	}
	public String getNoticeTypeEtc() {
		return noticeTypeEtc;
	}
	public void setNoticeTypeEtc(String noticeTypeEtc) {
		this.noticeTypeEtc = noticeTypeEtc;
	}
	public String getCompanyNameFull() {
		return companyNameFull;
	}
	public void setCompanyNameFull(String companyNameFull) {
		this.companyNameFull = companyNameFull;
	}
	public String getCompanyNameShort() {
		return companyNameShort;
	}
	public void setCompanyNameShort(String companyNameShort) {
		this.companyNameShort = companyNameShort;
	}
	public String getDistributionSiteUrl() {
		return distributionSiteUrl;
	}
	public void setDistributionSiteUrl(String distributionSiteUrl) {
		this.distributionSiteUrl = distributionSiteUrl;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAppended() {
		return appended;
	}
	public void setAppended(String appended) {
		this.appended = appended;
	}
	public String getAppendedTEXT() {
		return appendedTEXT;
	}
	public void setAppendedTEXT(String appendedTEXT) {
		this.appendedTEXT = appendedTEXT;
	}
	public String getUseCompanyNameTitle() {
		return useCompanyNameTitle;
	}
	public void setUseCompanyNameTitle(String useCompanyNameTitle) {
		this.useCompanyNameTitle = useCompanyNameTitle;
	}
	public String getDistributedOtherCompany() {
		return distributedOtherCompany;
	}
	public void setDistributedOtherCompany(String distributedOtherCompany) {
		this.distributedOtherCompany = distributedOtherCompany;
	}
	public String getMergedOtherOssNotice() {
		return mergedOtherOssNotice;
	}
	public void setMergedOtherOssNotice(String mergedOtherOssNotice) {
		this.mergedOtherOssNotice = mergedOtherOssNotice;
	}
	public String getAccompaniedSourceCode() {
		return accompaniedSourceCode;
	}
	public void setAccompaniedSourceCode(String accompaniedSourceCode) {
		this.accompaniedSourceCode = accompaniedSourceCode;
	}
	public String getPackageJson() {
		return packageJson;
	}
	public void setPackageJson(String packageJson) {
		this.packageJson = packageJson;
	}
	public List<OssComponents> getOssComponents() {
		return ossComponents;
	}
	public void setOssComponents(List<OssComponents> ossComponents) {
		this.ossComponents = ossComponents;
	}
	public String getNetworkServerFlag() {
		return networkServerFlag;
	}
	public void setNetworkServerFlag(String networkServerFlag) {
		this.networkServerFlag = networkServerFlag;
	}
	public String getPackageFileId() {
		return packageFileId;
	}
	public void setPackageFileId(String packageFileId) {
		this.packageFileId = packageFileId;
	}
	public String getPackageFileId2() {
		return packageFileId2;
	}
	public void setPackageFileId2(String packageFileId2) {
		this.packageFileId2 = packageFileId2;
	}
	public String getPackageFileId3() {
		return packageFileId3;
	}
	public void setPackageFileId3(String packageFileId3) {
		this.packageFileId3 = packageFileId3;
	}
	public String getUserComment() {
		return userComment;
	}
	public void setUserComment(String userComment) {
		this.userComment = userComment;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	public String getWithoutVerifyYn() {
		return withoutVerifyYn;
	}
	public void setWithoutVerifyYn(String withoutVerifyYn) {
		this.withoutVerifyYn = withoutVerifyYn;
	}
	public String getOssDistributionSite() {
		return ossDistributionSite;
	}
	public void setOssDistributionSite(String ossDistributionSite) {
		this.ossDistributionSite = ossDistributionSite;
	}
	public String getOssDistributionSiteYn() {
		return ossDistributionSiteYn;
	}
	public void setOssDistributionSiteYn(String ossDistributionSiteYn) {
		this.ossDistributionSiteYn = ossDistributionSiteYn;
	}
	public String getSimpleNoticeFlag() {
		return simpleNoticeFlag;
	}
	public void setSimpleNoticeFlag(String simpleNoticeFlag) {
		this.simpleNoticeFlag = simpleNoticeFlag;
	}
	public String getRefDiv() {
		return refDiv;
	}
	public void setRefDiv(String refDiv) {
		this.refDiv = refDiv;
	}
	public String getRefDivUnExists() {
		return refDivUnExists;
	}
	public void setRefDivUnExists(String refDivUnExists) {
		this.refDivUnExists = refDivUnExists;
	}
	public String getNoticeTextFileId() {
		return noticeTextFileId;
	}
	public void setNoticeTextFileId(String noticeTextFileId) {
		this.noticeTextFileId = noticeTextFileId;
	}
	public String getSimpleHtmlFileId() {
		return simpleHtmlFileId;
	}
	public void setSimpleHtmlFileId(String simpleHtmlFileId) {
		this.simpleHtmlFileId = simpleHtmlFileId;
	}
	public String getSimpleTextFileId() {
		return simpleTextFileId;
	}
	public void setSimpleTextFileId(String simpleTextFileId) {
		this.simpleTextFileId = simpleTextFileId;
	}
	public String getSpdxSheetFileId() {
		return spdxSheetFileId;
	}
	public void setSpdxSheetFileId(String spdxSheetFileId) {
		this.spdxSheetFileId = spdxSheetFileId;
	}
	public String getSpdxRdfFileId() {
		return spdxRdfFileId;
	}
	public void setSpdxRdfFileId(String spdxRdfFileId) {
		this.spdxRdfFileId = spdxRdfFileId;
	}
	public String getSpdxTagFileId() {
		return spdxTagFileId;
	}
	public void setSpdxTagFileId(String spdxTagFileId) {
		this.spdxTagFileId = spdxTagFileId;
	}
	public String getSpdxJsonFileId() {
		return spdxJsonFileId;
	}
	public void setSpdxJsonFileId(String spdxJsonFileId) {
		this.spdxJsonFileId = spdxJsonFileId;
	}
	public String getSpdxYamlFileId() {
		return spdxYamlFileId;
	}
	public void setSpdxYamlFileId(String spdxYamlFileId) {
		this.spdxYamlFileId = spdxYamlFileId;
	}
	public String getIgnoreBinaryDbFlag() {
		return ignoreBinaryDbFlag;
	}
	public void setIgnoreBinaryDbFlag(String ignoreBinaryDbFlag) {
		this.ignoreBinaryDbFlag = ignoreBinaryDbFlag;
	}
	
	public String getEditNoticeYn() {
		return editNoticeYn;
	}
	public void setEditNoticeYn(String editNoticeYn) {
		this.editNoticeYn = editNoticeYn;
	}
	
	public String getEditCompanyYn() {
		return editCompanyYn;
	}
	public void setEditCompanyYn(String editCompanyYn) {
		this.editCompanyYn = editCompanyYn;
	}
	
	public String getEditDistributionSiteUrlYn() {
		return editDistributionSiteUrlYn;
	}
	public void setEditDistributionSiteUrlYn(String editDistributionSiteUrlYn) {
		this.editDistributionSiteUrlYn = editDistributionSiteUrlYn;
	}
	
	public String getEditEmailYn() {
		return editEmailYn;
	}
	public void setEditEmailYn(String editEmailYn) {
		this.editEmailYn = editEmailYn;
	}
	
	public String getHideOssVersionYn() {
		return hideOssVersionYn;
	}
	public void setHideOssVersionYn(String hideOssVersionYn) {
		this.hideOssVersionYn = hideOssVersionYn;
	}
	
	public String getEditAppendedYn() {
		return editAppendedYn;
	}
	public void setEditAppendedYn(String editAppendedYn) {
		this.editAppendedYn = editAppendedYn;
	}
	public String getIsSimpleNotice() {
		return isSimpleNotice;
	}
	public void setIsSimpleNotice(String isSimpleNotice) {
		this.isSimpleNotice = isSimpleNotice;
	}
	public String getPreviewOnly() {
		return previewOnly;
	}
	public void setPreviewOnly(String previewOnly) {
		this.previewOnly = previewOnly;
	}
}
