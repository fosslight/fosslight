/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class BinaryAnalysisResult.
 */
public class BinaryAnalysisResult extends ComBean implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4907819340622537583L;

	/**  bat Analysis Result Seq. */
	private String batResultId;
	
	/**  binaryname + | + binary size. */
	private List<String> batKeyList;
	
	/** The bat id. */
	private String batId;
	
	/** The binary. */
	private String binaryName;
	
	/** The path. */
	private String filePath;
	
	private String sourcePath;
	
	/** The file type. */
	private String fileType;
	
	/** The check sum. */
	private String checkSum;
	
	/** The sha 1. */
	private String shaKey;
	
	/** The tlsh. */
	private String tlsh;
	
	/** The file size. */
	private String fileSize;
	
	/** The string match percentage. */
	private String stringMatchPercentage;
	
	/** The oss name. */
	private String ossName;
	/** The oss name. */
	private String ossVersion;
	
	/** The license. */
	private String license;
	
	/** The scores. */
	private String scores;

	/** The percentage. */
	private String percentage;
	
	/** The unique matches. */
	private String uniqueMatches;
	
	/** The non unique matches assigned. */
	private String nonUniqueMatchesAssigned;
	
	/** The parentname. */
	private String parentname;
	
	private String platformname;
	
	private String platformversion;
	
	private int tlshDistance = -1;
	
	private String orgTlsh;
	
	private String updatedate;
	
	private String downloadLocation;
	
	private String actionId;
	private String actionType;
	
	private String guireportFlag = "N";
	
	private String comment;
	
	private String filename;
	
	/** The sch start date. */
	private String schStartDate;
	
	/** The sch end date. */
	private String schEndDate;
	
	private String matchName;

	private String prjId;
	
	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	/**
	 * Gets the bat result id.
	 *
	 * @return the bat result id
	 */
	public String getBatResultId() {
		return batResultId;
	}

	/**
	 * Sets the bat result id.
	 *
	 * @param batResultId the new bat result id
	 */
	public void setBatResultId(String batResultId) {
		this.batResultId = batResultId;
	}

	/**
	 * Gets the bat id.
	 *
	 * @return the bat id
	 */
	public String getBatId() {
		return batId;
	}

	/**
	 * Sets the bat id.
	 *
	 * @param batId the new bat id
	 */
	public void setBatId(String batId) {
		this.batId = batId;
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
	 * Gets the file type.
	 *
	 * @return the file type
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * Sets the file type.
	 *
	 * @param fileType the new file type
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * Gets the check sum.
	 *
	 * @return the check sum
	 */
	public String getCheckSum() {
		return checkSum;
	}

	/**
	 * Sets the check sum.
	 *
	 * @param checkSum the new check sum
	 */
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	/**
	 * Gets the tlsh.
	 *
	 * @return the tlsh
	 */
	public String getTlsh() {
		return tlsh;
	}

	/**
	 * Sets the tlsh.
	 *
	 * @param tlsh the new tlsh
	 */
	public void setTlsh(String tlsh) {
		this.tlsh = tlsh;
	}

	/**
	 * Gets the file size.
	 *
	 * @return the file size
	 */
	public String getFileSize() {
		return fileSize;
	}

	/**
	 * Sets the file size.
	 *
	 * @param fileSize the new file size
	 */
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * Gets the string match percentage.
	 *
	 * @return the string match percentage
	 */
	public String getStringMatchPercentage() {
		return stringMatchPercentage;
	}

	/**
	 * Sets the string match percentage.
	 *
	 * @param stringMatchPercentage the new string match percentage
	 */
	public void setStringMatchPercentage(String stringMatchPercentage) {
		this.stringMatchPercentage = stringMatchPercentage;
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
	 * Gets the unique matches.
	 *
	 * @return the unique matches
	 */
	public String getUniqueMatches() {
		return uniqueMatches;
	}

	/**
	 * Sets the unique matches.
	 *
	 * @param uniqueMatches the new unique matches
	 */
	public void setUniqueMatches(String uniqueMatches) {
		this.uniqueMatches = uniqueMatches;
	}

	/**
	 * Gets the non unique matches assigned.
	 *
	 * @return the non unique matches assigned
	 */
	public String getNonUniqueMatchesAssigned() {
		return nonUniqueMatchesAssigned;
	}

	/**
	 * Sets the non unique matches assigned.
	 *
	 * @param nonUniqueMatchesAssigned the new non unique matches assigned
	 */
	public void setNonUniqueMatchesAssigned(String nonUniqueMatchesAssigned) {
		this.nonUniqueMatchesAssigned = nonUniqueMatchesAssigned;
	}

	/**
	 * Gets the scores.
	 *
	 * @return the scores
	 */
	public String getScores() {
		return scores;
	}

	/**
	 * Sets the scores.
	 *
	 * @param scores the new scores
	 */
	public void setScores(String scores) {
		this.scores = scores;
	}

	/**
	 * Gets the percentage.
	 *
	 * @return the percentage
	 */
	public String getPercentage() {
		return percentage;
	}

	/**
	 * Sets the percentage.
	 *
	 * @param percentage the new percentage
	 */
	public void setPercentage(String percentage) {
		this.percentage = percentage;
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
	 * Gets the sha key.
	 *
	 * @return the sha key
	 */
	public String getShaKey() {
		return shaKey;
	}

	/**
	 * Sets the sha key.
	 *
	 * @param shaKey the new sha key
	 */
	public void setShaKey(String shaKey) {
		this.shaKey = shaKey;
	}

	/**
	 * Gets the bat key list.
	 *
	 * @return the bat key list
	 */
	public List<String> getBatKeyList() {
		return batKeyList;
	}

	/**
	 * Sets the bat key list.
	 *
	 * @param batKeyList the new bat key list
	 */
	public void setBatKeyList(List<String> batKeyList) {
		this.batKeyList = batKeyList;
	}
	
	/**
	 * Adds the bat key.
	 *
	 * @param batKey the bat key
	 */
	public void addBatKey(String batKey) {
		if(this.batKeyList == null) {
			this.batKeyList = new ArrayList<>();
		}
		this.batKeyList.add(batKey);
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
	 * Gets the license.
	 *
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * Sets the license.
	 *
	 * @param license the new license
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * Gets the parentname.
	 *
	 * @return the parentname
	 */
	public String getParentname() {
		return parentname;
	}

	/**
	 * Sets the parentname.
	 *
	 * @param parentname the new parentname
	 */
	public void setParentname(String parentname) {
		this.parentname = parentname;
	}

	public int getTlshDistance() {
		return tlshDistance;
	}

	public void setTlshDistance(int tlshDistance) {
		this.tlshDistance = tlshDistance;
	}

	public String getOrgTlsh() {
		return orgTlsh;
	}

	public void setOrgTlsh(String orgTlsh) {
		this.orgTlsh = orgTlsh;
	}

	public String getPlatformname() {
		return platformname;
	}

	public void setPlatformname(String platformname) {
		this.platformname = platformname;
	}

	public String getPlatformversion() {
		return platformversion;
	}

	public void setPlatformversion(String platformversion) {
		this.platformversion = platformversion;
	}

	public String getUpdatedate() {
		return updatedate;
	}

	public void setUpdatedate(String updatedate) {
		this.updatedate = updatedate;
	}

	public String getDownloadLocation() {
		return downloadLocation;
	}

	public void setDownloadLocation(String downloadLocation) {
		this.downloadLocation = downloadLocation;
	}

	public String getGuireportFlag() {
		return guireportFlag;
	}

	public void setGuireportFlag(String guireportFlag) {
		this.guireportFlag = guireportFlag;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getSchStartDate() {
		return schStartDate;
	}

	public void setSchStartDate(String schStartDate) {
		this.schStartDate = schStartDate;
	}

	public String getSchEndDate() {
		return schEndDate;
	}

	public void setSchEndDate(String schEndDate) {
		this.schEndDate = schEndDate;
	}	
	
	public String getMatchName() {
		return matchName;
	}

	public void setMatchName(String matchName) {
		this.matchName = matchName;
	}

	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
 }
