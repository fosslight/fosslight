/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class T2CodeDtl extends ComBean implements  Serializable {
	@Serial
	private static final long serialVersionUID = -3911799499686061439L;
	
	private String cdNo;			//코드번호
	private String cdDtlNo;		//상세코드번호
	private String cdSubNo;		//서브코드번호
	private String cdDtlNm;		//상세코드명
	private String cdDtlNm2;		//상세코드명2
	private String cdDtlExp;		//상세코드설명
	private String cdOrder;		//표시순
	private String useYn;			//사용여부
	private String cdDtlNoOrign;	// jqgrid의 수정전 기존 상세코드번호
	
	private List<T2CodeDtl> newRowList;
	private List<T2CodeDtl> modifyRowList;
	private List<T2CodeDtl> deleteRowList;
	
	private String cdDtlNoNew;
	
	// 기본생성자
	public T2CodeDtl() {}
	
	// default setting cdNo
	public T2CodeDtl(String cdNo) {
		setCdNo(cdNo);
	}
	
	// default setting cdNo, cdDtlNo
	public T2CodeDtl(String cdNo, String cdDtlNo) {
		setCdNo(cdNo);
		setCdDtlNo(cdDtlNo);
	}
	
	public String getCdNo() {
		return cdNo;
	}
	public void setCdNo(String cdNo) {
		this.cdNo = cdNo;
	}
	public String getCdDtlNo() {
		return cdDtlNo;
	}
	public void setCdDtlNo(String cdDtlNo) {
		this.cdDtlNo = cdDtlNo;
	}
	public String getCdSubNo() {
		return cdSubNo;
	}
	public void setCdSubNo(String cdSubNo) {
		this.cdSubNo = cdSubNo;
	}
	public String getCdDtlNm() {
		return cdDtlNm;
	}
	public void setCdDtlNm(String cdDtlNm) {
		this.cdDtlNm = cdDtlNm;
	}
	public String getCdDtlNm2() {
		return cdDtlNm2;
	}
	public void setCdDtlNm2(String cdDtlNm2) {
		this.cdDtlNm2 = cdDtlNm2;
	}
	public String getCdDtlExp() {
		return cdDtlExp;
	}
	public void setCdDtlExp(String cdDtlExp) {
		this.cdDtlExp = cdDtlExp;
	}
	public String getCdOrder() {
		return cdOrder;
	}
	public void setCdOrder(String cdOrder) {
		this.cdOrder = cdOrder;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	public List<T2CodeDtl> getNewRowList() {
		return newRowList;
	}
	public void setNewRowList(List<T2CodeDtl> newRowList) {
		this.newRowList = newRowList;
	}
	public List<T2CodeDtl> getModifyRowList() {
		return modifyRowList;
	}
	public void setModifyRowList(List<T2CodeDtl> modifyRowList) {
		this.modifyRowList = modifyRowList;
	}
	public List<T2CodeDtl> getDeleteRowList() {
		return deleteRowList;
	}
	public void setDeleteRowList(List<T2CodeDtl> deleteRowList) {
		this.deleteRowList = deleteRowList;
	}
	public String getCdDtlNoOrign() {
		return cdDtlNoOrign;
	}
	public void setCdDtlNoOrign(String cdDtlNoOrign) {
		this.cdDtlNoOrign = cdDtlNoOrign;
	}
	public String getCdDtlNoNew() {
		return cdDtlNoNew;
	}
	public void setCdDtlNoNew(String cdDtlNoNew) {
		this.cdDtlNoNew = cdDtlNoNew;
	}
}
