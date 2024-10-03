/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;

public class History extends ComBean implements Serializable {
	@Serial
	private static final long serialVersionUID = 3832152613376387438L;
	private String idx;		// index
	private String hKey;		// master seq
	private String hTitle;	// 제목
	private String hType;	// 이벤트 종류
	private String hTypeNm;	// 이벤트 종류 이름
	private String hComment;	// 수정사유
	private String hEtc;		// 비고
	private String hAction;	// CUD 액션
	private Object hData;	// Data
	
	public History() {
		super();
	}
	public History(String idx) {
		super();
		this.idx = idx;
	}
	
	public String getIdx() {
		return idx;
	}
	public void setIdx(String idx) {
		this.idx = idx;
	}
	public String gethKey() {
		return hKey;
	}
	public void sethKey(String hKey) {
		this.hKey = hKey;
	}
	public String gethTitle() {
		return hTitle;
	}
	public void sethTitle(String hTitle) {
		this.hTitle = hTitle;
	}
	public String gethType() {
		return hType;
	}
	public void sethType(String hType) {
		this.hType = hType;
	}
	public String gethComment() {
		return hComment;
	}
	public void sethComment(String hComment) {
		this.hComment = hComment;
	}
	public String getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String gethAction() {
		return hAction;
	}
	public void sethAction(String hAction) {
		this.hAction = hAction;
	}
	public Object gethData() {
		return hData;
	}
	public void sethData(Object hData) {
		this.hData = hData;
	}
	public String gethEtc() {
		return hEtc;
	}
	public void sethEtc(String hEtc) {
		this.hEtc = hEtc;
	}
	public String gethTypeNm() {
		return hTypeNm;
	}
	public void sethTypeNm(String hTypeNm) {
		this.hTypeNm = hTypeNm;
	}
}
