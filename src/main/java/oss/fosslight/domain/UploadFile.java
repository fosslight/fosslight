/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;

public class UploadFile extends ComBean implements Serializable{

	@Serial
	private static final long serialVersionUID = 4663446686199447488L;
	
	private String registSeq;
	private String registFileId;
	private String filePath;
	private String fileName;
	private String originalFilename;
	private String inputName;
	private int indexNum;
	private long size;
	private String contentType;
	private boolean uploadSucc;
	private int wgetResult;
	private String fileExt;
	private String actualFilename;
	
	public String getFileExt() {
		return fileExt;
	}
	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}
	public String getRegistSeq() {
		return registSeq;
	}
	public void setRegistSeq(String registSeq) {
		this.registSeq = registSeq;
	}
	public String getRegistFileId() {
		return registFileId;
	}
	public void setRegistFileId(String registFileId) {
		this.registFileId = registFileId;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getOriginalFilename() {
		return originalFilename;
	}
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}
	public String getInputName() {
		return inputName;
	}
	public void setInputName(String inputName) {
		this.inputName = inputName;
	}
	
	public int getIndexNum() {
		return indexNum;
	}
	public void setIndexNum(int indexNum) {
		this.indexNum = indexNum;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public boolean isUploadSucc() {
		return uploadSucc;
	}
	public void setUploadSucc(boolean uploadSucc) {
		this.uploadSucc = uploadSucc;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public int getWgetResult() {
		return wgetResult;
	}
	public void setWgetResult(int wgetResult) {
		this.wgetResult = wgetResult;
	}
	public String getActualFilename() {
		return actualFilename;
	}
	public void setActualFilename(String actualFilename) {
		this.actualFilename = actualFilename;
	}
}