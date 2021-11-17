/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;


/**
 * The Class partnerMaster
 */
public class ReportCombean implements Serializable{

	private static final long serialVersionUID = 6760737950434033025L;
	
	
	private String[] sheetNums;
	private String fileSeq;
	private String no;
	private String name;
	
	private String type;
	private String parameter;
	
	private String androidFileSeq;
	private String androidNoticeFileSeq;
	private String androidResultFileSeq;
	
	public String[] getSheetNums() {
		return sheetNums != null ? sheetNums.clone() : null;
	}
	public void setSheetNums(String[] sheetNums) {
		this.sheetNums = sheetNums;
	}
	public String getFileSeq() {
		return fileSeq;
	}
	public void setFileSeq(String fileSeq) {
		this.fileSeq = fileSeq;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getAndroidFileSeq() {
		return androidFileSeq;
	}
	public void setAndroidFileSeq(String androidFileSeq) {
		this.androidFileSeq = androidFileSeq;
	}
	public String getAndroidNoticeFileSeq() {
		return androidNoticeFileSeq;
	}
	public void setAndroidNoticeFileSeq(String androidNoticeFileSeq) {
		this.androidNoticeFileSeq = androidNoticeFileSeq;
	}
	public String getAndroidResultFileSeq() {
		return androidResultFileSeq;
	}
	public void setAndroidResultFileSeq(String androidResultFileSeq) {
		this.androidResultFileSeq = androidResultFileSeq;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
	
}