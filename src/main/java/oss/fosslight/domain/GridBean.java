/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class GridBean extends ComBean{
	@Serial
	private static final long serialVersionUID = 1481189510461854682L;

	private int result;		// 성공,실패 코드  | 성공 : 0
	private int page;			// 선택 페이지
    private int records;		// 한페이지 표현 개수
    private int totCnt;		// 전체글수
    
    private ArrayList<? extends ComBean> rowList;
    private ComBean bean;
    
    public GridBean() {}
    
    public GridBean(ArrayList<? extends ComBean> list) {
		this.rowList = list;
	}
    
    public GridBean(ComBean comBean) {
    	this.bean = comBean;
    }
    
    public GridBean(ArrayList<? extends ComBean> list, ComBean comBean) {
		this.rowList = list;
		this.bean = comBean;
	}
    
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRecords() {
		return records;
	}
	public void setRecords(int records) {
		this.records = records;
	}
	public int getTotCnt() {
		return totCnt;
	}
	public void setTotCnt(int totCnt) {
		this.totCnt = totCnt;
	}
	public List<? extends ComBean> getRowList() {
		return rowList;
	}
	public void setRows(ArrayList<? extends ComBean> rows) {
		this.rowList = rows;
	}

	public ComBean getBean() {
		return bean;
	}

	public void setBean(ComBean bean) {
		this.bean = bean;
	}
    
}