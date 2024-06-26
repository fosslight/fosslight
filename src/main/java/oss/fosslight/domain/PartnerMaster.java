/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The Class partnerMaster.
 */
public class PartnerMaster extends ComBean implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1540732220327083849L;
	
	/** The partner id. */
	private String partnerId;
	
	/** The status. */
	private String status;
	
	private String[] arrStatuses;

	/** The partner name. */
	private String partnerName;
	
	/** The software name. */
	private String softwareName;
	
	/** The software version. */
	private String softwareVersion;
	
	/** The delivery form. */
	private String deliveryForm;
	
	/** The public yn. */
	private String publicYn;
	
	/** The description. */
	private String description;
	
	/** The confirmation file id. */
	private String confirmationFileId;
	
	/** The oss file id. */
	private String ossFileId;
	
	private String binaryFileId;
	
	/** The related DocumentFile file. */
	private List<T2File> documentsFile; // Related documents 파일 객체
	
	private String documentsFileCnt;
	
	private String documentsFileId;
	
	private String delDocumentsFile;

	/** The reviewer. */
	private String reviewer;
	
	/** The reviewer name. */
	private String reviewerName;
	
	/** The use yn. */
	private String useYn;
	
	/** The comment. */
	private String comment;
	
	/** The comment text. */
	private String commentText;
	
	/** The division. */
	private String division;
	
	/** The file name. */
	private String fileName;
	
	/** The file name 2. */
	private String fileName2;
	
	/** The oss name. */
	//검색
	private String ossName;
	
	/** The license name. */
	private String licenseName;
	
	/** The created date 1. */
	private String createdDate1;
	
	/** The created date 2. */
	private String createdDate2;
	
	/** The binary name. */
	private String binaryName;
	
	/** The par division. */
	// PARTNER_WATCHER
	private String parDivision;
	
	/** The par division name. */
	private String parDivisionName;
	
	/** The par user id. */
	private String parUserId;
	
	/** The par user name. */
	private String parUserName;
	
	/** The par email. */
	private String parEmail;
	
	/** The watchers. */
	private String[] watchers;
	
	/** The watcher list. */
	private List<PartnerMaster> watcherList;
	
	/** The division list. */
	private ArrayList<Map<String, String>> divisionList;
	
	/** The email list. */
	private ArrayList<Map<String, String>> emailList;
	
	/** The watcher division. */
	private String[] watcherDivision;
	
	/** The watcher user id. */
	private String[] watcherUserId;
	
	/** The partner watcher. */
	private List<PartnerWatcher> partnerWatcher;
	
	/** The comments histroy. */
	private List<CommentsHistory> commentsHistroy;
	
	/** The oss components. */
	private List<OssComponents> ossComponents;
	
	/** The oss components str. */
	private String ossComponentsStr; 
	
	/** The oss components license str. */
	private String ossComponentsLicenseStr;
	
	/** The user comment. */
	private String userComment;
	
	/** The component count. */
	private String componentCount;
	
	/** The prj id. */
	private String prjId;
	
	/** The third party partner id list. */
	private List<String> thirdPartyPartnerIdList;
	
	
	/** The view only flag. */
	private String viewOnlyFlag;
	
	/** list - prj(project), par(3rdparty), bat */
	private String listKind;
	
	/** listKind pair id */
	private String listId;
	
	private String modelFlag = "N";
	
	private String userRole;
	
	private String ossAnalysisStatus;
	
	private String analysisStartDate;

	private String ossVersion;
	
	private String ossNameMergeFlag;
	
	private String ignoreBinaryDbFlag = "N";
	
	/** The vuln yn. */
	private String vulnYn;
	
	/** The cvss score. */
	private String cvssScore;
	
	/** The cve id. */
	private String cveId;
	
	private String[] partnerIds;
	
	private ArrayList<Map<String, String>> changeWatcherList;
	
	private String copyWatcherLocation;
	
	private int permission;
	
	private int statusPermission;
	
	/*
	 * Gets the partner id.
	 *
	 * @return the partner id
	 */
	public String getPartnerId() {
		return partnerId;
	}
	
	/**
	 * Sets the partner id.
	 *
	 * @param partnerId the new partner id
	 */
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
	
	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Gets the partner name.
	 *
	 * @return the partner name
	 */
	public String getPartnerName() {
		return partnerName;
	}
	
	/**
	 * Sets the partner name.
	 *
	 * @param partnerName the new partner name
	 */
	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}
	
	/**
	 * Gets the software name.
	 *
	 * @return the software name
	 */
	public String getSoftwareName() {
		return softwareName;
	}
	
	/**
	 * Sets the software name.
	 *
	 * @param softwareName the new software name
	 */
	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}
	
	/**
	 * Gets the software version.
	 *
	 * @return the software version
	 */
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	
	/**
	 * Sets the software version.
	 *
	 * @param softwareVersion the new software version
	 */
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}
	
	/**
	 * Gets the delivery form.
	 *
	 * @return the delivery form
	 */
	public String getDeliveryForm() {
		return deliveryForm;
	}
	
	/**
	 * Sets the delivery form.
	 *
	 * @param deliveryForm the new delivery form
	 */
	public void setDeliveryForm(String deliveryForm) {
		this.deliveryForm = deliveryForm;
	}
	
	/**
	 * Gets the public yn.
	 *
	 * @return the public yn
	 */
	public String getPublicYn() {
		return publicYn;
	}
	
	/**
	 * Sets the public yn.
	 *
	 * @param publicYn the new public yn
	 */
	public void setPublicYn(String publicYn) {
		this.publicYn = publicYn;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the confirmation file id.
	 *
	 * @return the confirmation file id
	 */
	public String getConfirmationFileId() {
		return confirmationFileId;
	}
	
	/**
	 * Sets the confirmation file id.
	 *
	 * @param confirmationFileId the new confirmation file id
	 */
	public void setConfirmationFileId(String confirmationFileId) {
		this.confirmationFileId = confirmationFileId;
	}
	
	/**
	 * Gets the oss file id.
	 *
	 * @return the oss file id
	 */
	public String getOssFileId() {
		return ossFileId;
	}
	
	/**
	 * Sets the oss file id.
	 *
	 * @param ossFileId the new oss file id
	 */
	public void setOssFileId(String ossFileId) {
		this.ossFileId = ossFileId;
	}
	
	/**
	 * Gets the binary file id.
	 *
	 * @return the binary file id
	 */
	public String getBinaryFileId() {
		return binaryFileId;
	}
	
	/**
	 * Sets the binary file id.
	 *
	 * @param binaryFileId the new binary file id
	 */
	public void setBinaryFileId(String binaryFileId) {
		this.binaryFileId = binaryFileId;
	}
	
	/**
	 * Gets the reviewer.
	 *
	 * @return the reviewer
	 */
	public String getReviewer() {
		return reviewer;
	}
	
	/**
	 * Sets the reviewer.
	 *
	 * @param reviewer the new reviewer
	 */
	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}
	
	/**
	 * Gets the use yn.
	 *
	 * @return the use yn
	 */
	public String getUseYn() {
		return useYn;
	}
	
	/**
	 * Sets the use yn.
	 *
	 * @param useYn the new use yn
	 */
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	
	/**
	 * Gets the comment.
	 *
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * Sets the comment.
	 *
	 * @param comment the new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Gets the division.
	 *
	 * @return the division
	 */
	public String getDivision() {
		return division;
	}
	
	/**
	 * Sets the division.
	 *
	 * @param division the new division
	 */
	public void setDivision(String division) {
		this.division = division;
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
	 * Gets the created date 1.
	 *
	 * @return the created date 1
	 */
	public String getCreatedDate1() {
		return createdDate1;
	}
	
	/**
	 * Sets the created date 1.
	 *
	 * @param createdDate1 the new created date 1
	 */
	public void setCreatedDate1(String createdDate1) {
		this.createdDate1 = createdDate1;
	}
	
	/**
	 * Gets the created date 2.
	 *
	 * @return the created date 2
	 */
	public String getCreatedDate2() {
		return createdDate2;
	}
	
	/**
	 * Sets the created date 2.
	 *
	 * @param createdDate2 the new created date 2
	 */
	public void setCreatedDate2(String createdDate2) {
		this.createdDate2 = createdDate2;
	}
	
	/**
	 * Gets the partner watcher.
	 *
	 * @return the partner watcher
	 */
	public List<PartnerWatcher> getPartnerWatcher() {
		return partnerWatcher;
	}
	
	/**
	 * Sets the partner watcher.
	 *
	 * @param partnerWatcher the new partner watcher
	 */
	public void setPartnerWatcher(List<PartnerWatcher> partnerWatcher) {
		this.partnerWatcher = partnerWatcher;
	}
	
	/**
	 * Gets the comments histroy.
	 *
	 * @return the comments histroy
	 */
	public List<CommentsHistory> getCommentsHistroy() {
		return commentsHistroy;
	}
	
	/**
	 * Sets the comments histroy.
	 *
	 * @param commentsHistroy the new comments histroy
	 */
	public void setCommentsHistroy(List<CommentsHistory> commentsHistroy) {
		this.commentsHistroy = commentsHistroy;
	}
	
	/**
	 * Gets the comment text.
	 *
	 * @return the comment text
	 */
	public String getCommentText() {
		return commentText;
	}
	
	/**
	 * Sets the comment text.
	 *
	 * @param commentText the new comment text
	 */
	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}
	
	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Sets the file name.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Gets the file name 2.
	 *
	 * @return the file name 2
	 */
	public String getFileName2() {
		return fileName2;
	}
	
	/**
	 * Sets the file name 2.
	 *
	 * @param fileName2 the new file name 2
	 */
	public void setFileName2(String fileName2) {
		this.fileName2 = fileName2;
	}
	
	/**
	 * Gets the oss components.
	 *
	 * @return the oss components
	 */
	public List<OssComponents> getOssComponents() {
		return ossComponents;
	}
	
	/**
	 * Sets the oss components.
	 *
	 * @param ossComponents the new oss components
	 */
	public void setOssComponents(List<OssComponents> ossComponents) {
		this.ossComponents = ossComponents;
	}
	
	/* (non-Javadoc)
	 * @see oss.fosslight.commons.components.bean.ComBean#toString()
	 */
	@Override
	public String toString() {
		return "PartnerMaster [partnerId=" + partnerId + ", status=" + status + ", partnerName=" + partnerName
				+ ", softwareName=" + softwareName + ", softwareVersion=" + softwareVersion + ", deliveryForm="
				+ deliveryForm + ", publicYn=" + publicYn + ", description=" + description + ", confirmationFileId="
				+ confirmationFileId + ", ossFileId=" + ossFileId + ", reviewer=" + reviewer + ", useYn=" + useYn
				+ ", comment=" + comment + ", commentText=" + commentText + ", division=" + division + ", fileName="
				+ fileName + ", fileName2=" + fileName2 + ", ossName=" + ossName + ", licenseName=" + licenseName
				+ ", createdDate1=" + createdDate1 + ", createdDate2=" + createdDate2 + ", partnerWatcher="
				+ partnerWatcher + ", commentsHistroy=" + commentsHistroy + ", ossComponents=" + ossComponents 
				+ ", componentCount="+ componentCount +"]";
	}
	
	/**
	 * Gets the oss components str.
	 *
	 * @return the oss components str
	 */
	public String getOssComponentsStr() {
		return ossComponentsStr;
	}
	
	/**
	 * Sets the oss components str.
	 *
	 * @param ossComponentsStr the new oss components str
	 */
	public void setOssComponentsStr(String ossComponentsStr) {
		this.ossComponentsStr = ossComponentsStr;
	}
	
	/**
	 * Gets the oss components license str.
	 *
	 * @return the oss components license str
	 */
	public String getOssComponentsLicenseStr() {
		return ossComponentsLicenseStr;
	}
	
	/**
	 * Sets the oss components license str.
	 *
	 * @param ossComponentsLicenseStr the new oss components license str
	 */
	public void setOssComponentsLicenseStr(String ossComponentsLicenseStr) {
		this.ossComponentsLicenseStr = ossComponentsLicenseStr;
	}
	
	/**
	 * Gets the watcher division.
	 *
	 * @return the watcher division
	 */
	public String[] getWatcherDivision() {
		return watcherDivision != null ? watcherDivision.clone() : null;
	}
	
	/**
	 * Sets the watcher division.
	 *
	 * @param watcherDivision the new watcher division
	 */
	public void setWatcherDivision(String[] watcherDivision) {
		this.watcherDivision = watcherDivision != null ? 
			watcherDivision.clone() : null;
	}
	
	/**
	 * Gets the watcher user id.
	 *
	 * @return the watcher user id
	 */
	public String[] getWatcherUserId() {
		return watcherUserId != null ? watcherUserId.clone() : null;
	}
	
	/**
	 * Sets the watcher user id.
	 *
	 * @param watcherUserId the new watcher user id
	 */
	public void setWatcherUserId(String[] watcherUserId) {
		this.watcherUserId = watcherUserId != null ?
			watcherUserId.clone() : null;
	}
	
	/**
	 * Gets the user comment.
	 *
	 * @return the user comment
	 */
	public String getUserComment() {
		return userComment;
	}
	
	/**
	 * Sets the user comment.
	 *
	 * @param userComment the new user comment
	 */
	public void setUserComment(String userComment) {
		this.userComment = userComment;
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
	 * Gets the component count.
	 *
	 * @return the component count
	 */
	public String getComponentCount() {
		return componentCount;
	}
	
	/**
	 * Sets the component count.
	 *
	 * @param componentCount the new component count
	 */
	public void setComponentCount(String componentCount) {
		this.componentCount = componentCount;
	}
	
	/**
	 * Gets the prj id.
	 *
	 * @return the prj id
	 */
	public String getPrjId() {
		return prjId;
	}
	
	/**
	 * Sets the prj id.
	 *
	 * @param prjId the new prj id
	 */
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	
	/**
	 * Gets the third party partner id list.
	 *
	 * @return the third party partner id list
	 */
	public List<String> getThirdPartyPartnerIdList() {
		return thirdPartyPartnerIdList;
	}

	/**
	 * Sets the third party partner id list.
	 *
	 * @param thirdPartyPartnerIdList the new third party partner id list
	 */
	public void setThirdPartyPartnerIdList(List<String> thirdPartyPartnerIdList) {
		this.thirdPartyPartnerIdList = thirdPartyPartnerIdList;
	}
	
	
	/**
	 * Gets the par division.
	 *
	 * @return the par division
	 */
	//WATCHER
	public String getParDivision() {
		return parDivision;
	}

	/**
	 * Sets the par division.
	 *
	 * @param parDivision the new par division
	 */
	public void setParDivision(String parDivision) {
		this.parDivision = parDivision;
	}

	/**
	 * Gets the par division name.
	 *
	 * @return the par division name
	 */
	public String getParDivisionName() {
		return parDivisionName;
	}

	/**
	 * Sets the par division name.
	 *
	 * @param parDivisionName the new par division name
	 */
	public void setParDivisionName(String parDivisionName) {
		this.parDivisionName = parDivisionName;
	}

	/**
	 * Gets the par user id.
	 *
	 * @return the par user id
	 */
	public String getParUserId() {
		return parUserId;
	}

	/**
	 * Sets the par user id.
	 *
	 * @param parUserId the new par user id
	 */
	public void setParUserId(String parUserId) {
		this.parUserId = parUserId;
	}

	/**
	 * Gets the par user name.
	 *
	 * @return the par user name
	 */
	public String getParUserName() {
		return parUserName;
	}

	/**
	 * Sets the par user name.
	 *
	 * @param parUserName the new par user name
	 */
	public void setParUserName(String parUserName) {
		this.parUserName = parUserName;
	}
	
	/**
	 * Gets the par email.
	 *
	 * @return the par email
	 */
	public String getParEmail() {
		return parEmail;
	}

	/**
	 * Sets the par email.
	 *
	 * @param parEmail the new par email
	 */
	public void setParEmail(String parEmail) {
		this.parEmail = parEmail;
	}

	/**
	 * Gets the watchers.
	 *
	 * @return the watchers
	 */
	public String[] getWatchers() {
		return watchers != null ? watchers.clone() : null;
	}

	/**
	 * Sets the watchers.
	 *
	 * @param watchers the new watchers
	 */
	public void setWatchers(String[] watchers) {
		this.watchers = watchers != null ? watchers.clone() : null;
	}

	/**
	 * Gets the watcher list.
	 *
	 * @return the watcher list
	 */
	public List<PartnerMaster> getWatcherList() {
		return watcherList;
	}

	/**
	 * Sets the watcher list.
	 *
	 * @param watcherList the new watcher list
	 */
	public void setWatcherList(List<PartnerMaster> watcherList) {
		this.watcherList = watcherList;
	}
	
	/**
	 * Gets the division list.
	 *
	 * @return the division list
	 */
	public ArrayList<Map<String, String>> getDivisionList() {
		return divisionList;
	}

	/**
	 * Sets the division list.
	 *
	 * @param divisionList the division list
	 */
	public void setDivisionList(ArrayList<Map<String, String>> divisionList) {
		this.divisionList = divisionList;
	}
	
	/**
	 * Gets the email list.
	 *
	 * @return the email list
	 */
	public ArrayList<Map<String, String>> getEmailList() {
		return emailList;
	}

	/**
	 * Sets the email list.
	 *
	 * @param emailList the email list
	 */
	public void setEmailList(ArrayList<Map<String, String>> emailList) {
		this.emailList = emailList;
	}
	
	/**
	 * Gets the view only flag.
	 *
	 * @return the viewOnlyFlag
	 */
	public String getViewOnlyFlag() {
		return viewOnlyFlag;
	}
	
	/**
	 * Sets the view only flag.
	 *
	 * @param viewOnlyFlag the viewOnlyFlag to set
	 */
	public void setViewOnlyFlag(String viewOnlyFlag) {
		this.viewOnlyFlag = viewOnlyFlag;
	}
	
	/**
	 * Gets the reviewer name.
	 *
	 * @return the reviewer name
	 */
	public String getReviewerName() {
		return reviewerName;
	}
	
	/**
	 * Sets the reviewer name.
	 *
	 * @param reviewerName the new reviewer name
	 */
	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}
	
	public String[] getArrStatuses() {
		return arrStatuses != null ? arrStatuses.clone() : null;
	}

	public void setArrStatuses(String[] arrStatuses) {
		this.arrStatuses = arrStatuses != null ?
			arrStatuses.clone() : null;
	}
	
	/**
	 * Gets the listKind.
	 *
	 * @return the listKind
	 */
	public String getListKind() {
		return listKind;
	}
	
	/**
	 * Sets the listKind.
	 *
	 * @param listKind the new listKind
	 */
	public void setListKind(String listKind) {
		this.listKind = listKind;
	}
	
	/**
	 * Gets the listId.
	 *
	 * @return the listId
	 */
	public String getListId() {
		return listId;
	}

	/**
	 * Sets the listId.
	 *
	 * @param partnerId the new listId
	 */
	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getModelFlag() {
		return modelFlag;
	}

	public void setModelFlag(String modelFlag) {
		this.modelFlag = modelFlag;
	}
	
	public List<T2File> getDocumentsFile() {
		return documentsFile;
	}

	public void setDocumentsFile(List<T2File> documentsFile) {
		this.documentsFile = documentsFile;
	}
	
	public String getDocumentsFileId() {
		return documentsFileId;
	}

	public void setDocumentsFileId(String documentsFileId) {
		this.documentsFileId = documentsFileId;
	}

	public String getDocumentsFileCnt() {
		return documentsFileCnt;
	}

	public void setDocumentsFileCnt(String documentsFileCnt) {
		this.documentsFileCnt = documentsFileCnt;
	}

	public String getDelDocumentsFile() {
		return delDocumentsFile;
	}

	public void setDelDocumentsFile(String delDocumentsFile) {
		this.delDocumentsFile = delDocumentsFile;
	}
	
	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	
	public String getOssAnalysisStatus() {
		return ossAnalysisStatus;
	}

	public void setOssAnalysisStatus(String ossAnalysisStatus) {
		this.ossAnalysisStatus = ossAnalysisStatus;
	}

	public String getAnalysisStartDate() {
		return analysisStartDate;
	}

	public void setAnalysisStartDate(String analysisStartDate) {
		this.analysisStartDate = analysisStartDate;
	}

	public String getOssVersion() {
		return ossVersion;
	}

	public void setOssVersion(String ossVersion) {
		this.ossVersion = ossVersion;
	}
	
	public String getOssNameMergeFlag() {
		return ossNameMergeFlag;
	}

	public void setOssNameMergeFlag(String ossNameMergeFlag) {
		this.ossNameMergeFlag = ossNameMergeFlag;
	}
	
	public String getIgnoreBinaryDbFlag() {
		return ignoreBinaryDbFlag;
	}

	public void setIgnoreBinaryDbFlag(String ignoreBinaryDbFlag) {
		this.ignoreBinaryDbFlag = ignoreBinaryDbFlag;
	}

	public String getVulnYn() {
		return vulnYn;
	}

	public void setVulnYn(String vulnYn) {
		this.vulnYn = vulnYn;
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

	public String[] getPartnerIds() {
		return partnerIds;
	}

	public void setPartnerIds(String[] partnerIds) {
		this.partnerIds = partnerIds;
	}

	public ArrayList<Map<String, String>> getChangeWatcherList() {
		return changeWatcherList;
	}

	public void setChangeWatcherList(ArrayList<Map<String, String>> changeWatcherList) {
		this.changeWatcherList = changeWatcherList;
	}

	public String getCopyWatcherLocation() {
		return copyWatcherLocation;
	}

	public void setCopyWatcherLocation(String copyWatcherLocation) {
		this.copyWatcherLocation = copyWatcherLocation;
	}

	public int getPermission() {
		return permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}

	public int getStatusPermission() {
		return statusPermission;
	}

	public void setStatusPermission(int statusPermission) {
		this.statusPermission = statusPermission;
	}
	
}