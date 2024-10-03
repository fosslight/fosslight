/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class T2Code extends ComBean implements Serializable {
	@Serial
	private static final long serialVersionUID = -3911799499686061439L;

	private String cdNo;
	private String cdNm;
	private String cdExp;
	private String sysCdYn;
	
	private List<T2CodeDtl> t2codeDtl;
	
	private List<T2Code> newRowList;
	private List<T2Code> modifyRowList;
	private List<T2Code> deleteRowList;
	
	// 생성자 기본
	public T2Code() {}
	
	// default setting cdNo 생성자
	public T2Code(String cdNo) {
		setCdNo(cdNo);
	}
	
	public String getCdNo() {
		return cdNo;
	}
	
	public void setCdNo(String cdNo) {
		this.cdNo = cdNo;
	}
	
	public String getCdNm() {
		return cdNm;
	}
	
	public void setCdNm(String cdNm) {
		this.cdNm = cdNm;
	}
	
	public String getCdExp() {
		return cdExp;
	}
	
	public void setCdExp(String cdExp) {
		this.cdExp = cdExp;
	}
	
	public String getSysCdYn() {
		return sysCdYn;
	}
	
	public void setSysCdYn(String sysCdYn) {
		this.sysCdYn = sysCdYn;
	}
	
	public List<T2CodeDtl> getT2codeDtl() {
		return t2codeDtl;
	}
	
	public void setT2codeDtl(List<T2CodeDtl> t2codeDtl) {
		this.t2codeDtl = t2codeDtl;
	}
	
	public List<T2Code> getNewRowList() {
		return newRowList;
	}
	
	public void setNewRowList(List<T2Code> newRowList) {
		this.newRowList = newRowList;
	}
	
	public List<T2Code> getModifyRowList() {
		return modifyRowList;
	}
	
	public void setModifyRowList(List<T2Code> modifyRowList) {
		this.modifyRowList = modifyRowList;
	}
	
	public List<T2Code> getDeleteRowList() {
		return deleteRowList;
	}
	
	public void setDeleteRowList(List<T2Code> deleteRowList) {
		this.deleteRowList = deleteRowList;
	}
}
