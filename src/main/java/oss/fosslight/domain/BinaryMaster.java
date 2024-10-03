/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */


package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BinaryMaster extends ComBean implements Serializable{

	@Serial
	private static final long serialVersionUID = -4375349371642376739L;
	
	private String batId;
	private String batStatus;
	private String batExecTime;
	private String softwareName;
	private String softwareVersion;
	private String partnerName;
	private String binaryFileId;
	private String reportFileId;
	private String useYn;
	private String createdDate1;
	private String createdDate2;
	private String fileName;
	private String division;
	private String prjId;
	private String ossComponentsStr; 
	private String ossComponentsLicenseStr;
	private String batResultCount;
	private String batErrorMsg;
	private String procId;
	private String procStartTime;
	private String userName;
	private String divisionName;

	// BAT_WATCHER
	private String batDivision;
	private String batDivisionName;
	private String batUserId;
	private String batUserName;
	private String batEmail;
	private String[] watchers;
	private List<BinaryMaster> watcherList;
	private ArrayList<Map<String, String>> divisionList;
	private ArrayList<Map<String, String>> emailList;
	
	private List<BatWatcher> batWatcher;
	
	private String[] watcherDivision;
	private String[] watcherUserId;
	
	private String binaryFileName;

	private String partnerId;
	
	private String listKind;
	private String listId;
	
	public BinaryMaster(){}
	//bat 등록용 생성자
	public BinaryMaster(String batId, String registSeq) {
		this.batId = batId;
		this.binaryFileId = registSeq;
	}
	public String getBatId() {
		return batId;
	}
	public void setBatId(String batId) {
		this.batId = batId;
	}
	public String getBatExecTime() {
		return batExecTime;
	}
	public void setBatExecTime(String batExecTime) {
		this.batExecTime = batExecTime;
	}
	public String getPartnerName() {
		return partnerName;
	}
	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}
	public String getBinaryFileId() {
		return binaryFileId;
	}
	public void setBinaryFileId(String binaryFileId) {
		this.binaryFileId = binaryFileId;
	}
	public String getReportFileId() {
		return reportFileId;
	}
	public void setReportFileId(String reportFileId) {
		this.reportFileId = reportFileId;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	public String getSoftwareName() {
		return softwareName;
	}
	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}
	public String getCreatedDate1() {
		return createdDate1;
	}
	public void setCreatedDate1(String createdDate1) {
		this.createdDate1 = createdDate1;
	}
	public String getCreatedDate2() {
		return createdDate2;
	}
	public void setCreatedDate2(String createdDate2) {
		this.createdDate2 = createdDate2;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getBatStatus() {
		return batStatus;
	}
	public void setBatStatus(String batStatus) {
		this.batStatus = batStatus;
	}
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	public String getOssComponentsStr() {
		return ossComponentsStr;
	}
	public void setOssComponentsStr(String ossComponentsStr) {
		this.ossComponentsStr = ossComponentsStr;
	}
	public String getOssComponentsLicenseStr() {
		return ossComponentsLicenseStr;
	}
	public void setOssComponentsLicenseStr(String ossComponentsLicenseStr) {
		this.ossComponentsLicenseStr = ossComponentsLicenseStr;
	}
	public String getBatErrorMsg() {
		return batErrorMsg;
	}
	public void setBatErrorMsg(String batErrorMsg) {
		this.batErrorMsg = batErrorMsg;
	}
	public String getBatResultCount() {
		return batResultCount;
	}
	public void setBatResultCount(String batResultCount) {
		this.batResultCount = batResultCount;
	}
	public List<BatWatcher> getBatWatcher() {
		return batWatcher;
	}
	
	public void addBatWatcher(String division, String userId) {
		if (!isEmpty(division)) {
			BatWatcher bean = new BatWatcher();
			bean.setDivision(division);
			bean.setUserId(userId);
			bean.setBatId(this.batId);
			if (this.batWatcher == null) {
				this.batWatcher = new ArrayList<BatWatcher>();
			}
			this.batWatcher.add(bean);
		}
		
	}
	public void setBatWatcher(List<BatWatcher> batWatcher) {
		this.batWatcher = batWatcher;
	}
	public String[] getWatcherDivision() {
		return watcherDivision != null ? watcherDivision.clone() : null;
	}
	public void setWatcherDivision(String[] watcherDivision) {
		this.watcherDivision = watcherDivision != null ?
			watcherDivision.clone() : null;
	}
	public String[] getWatcherUserId() {
		return watcherUserId != null ? watcherUserId.clone() : null;
	}
	public void setWatcherUserId(String[] watcherUserId) {
		this.watcherUserId = watcherUserId != null ?
			watcherUserId.clone() : null;
	}
	public String getBinaryFileName() {
		return binaryFileName;
	}
	public void setBinaryFileName(String binaryFileName) {
		this.binaryFileName = binaryFileName;
	}
	public String getProcId() {
		return procId;
	}
	public void setProcId(String procId) {
		this.procId = procId;
	}
	
	//WATCHER
	public String getBatDivision() {
		return batDivision;
	}

	public void setBatDivision(String batDivision) {
		this.batDivision = batDivision;
	}
	
	public String getBatDivisionName() {
		return batDivisionName;
	}
	
	public void setBatDivisionName(String batDivisionName) {
		this.batDivisionName = batDivisionName;
	}
	
	public String getBatUserId() {
		return batUserId;
	}
	
	public void setBatUserId(String batUserId) {
		this.batUserId = batUserId;
	}
	
	public String getBatUserName() {
		return batUserName;
	}
	
	public void setBatUserName(String batUserName) {
		this.batUserName = batUserName;
	}
	
	public String getBatEmail() {
		return batEmail;
	}
		
	public void setBatEmail(String batEmail) {
		this.batEmail = batEmail;
	}
	
	public String[] getWatchers() {
		return watchers != null ? watchers.clone() : null;
	}

	public void setWatchers(String[] watchers) {
		this.watchers = watchers != null ? watchers.clone() : null;
	}

	public List<BinaryMaster> getWatcherList() {
		return watcherList;
	}

	public void setWatcherList(List<BinaryMaster> watcherList) {
		this.watcherList = watcherList;
	}
		
	public ArrayList<Map<String, String>> getDivisionList() {
		return divisionList;
	}

	public void setDivisionList(ArrayList<Map<String, String>> divisionList) {
		this.divisionList = divisionList;
	}
		
	public ArrayList<Map<String, String>> getEmailList() {
		return emailList;
	}

	public void setEmailList(ArrayList<Map<String, String>> emailList) {
		this.emailList = emailList;
	}
	
	public String getPartnerId() {
		return partnerId;
	}
	
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
	
	public String getListKind() {
		return listKind;
	}
	
	public void setListKind(String listKind) {
		this.listKind = listKind;
	}
	
	public String getListId() {
		return listId;
	}
	
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getProcStartTime() {
		return procStartTime;
	}
	public void setProcStartTime(String procStartTime) {
		this.procStartTime = procStartTime;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDivisionName() {
		return divisionName;
	}
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}
	
	
}