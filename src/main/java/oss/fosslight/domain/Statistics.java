/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Statistics extends ComBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String[] colorArray; // 추후 관리가 필요함
	private List<String> titleArray;
	private List<Integer> dataSumArray;
	private ArrayList<ArrayList<Integer>> dataArray = new ArrayList<ArrayList<Integer>>();
	private String chartType;
	private String categoryType;
	private String isRawData;
	private String updateType;
	private List<String> noneUser;
	private List<String> excludedTarget;
	
	// STT : Status, REV : Reviewer, DST : Distribution Type
	private String divisionNo;
	private String divisionNm;
	private int divisionOrder;
	private String titleNm;
	private String[] divisionNums;
	
	private int category0Cnt = -1;
	private int category1Cnt = -1;
	private int category2Cnt = -1;
	private int category3Cnt = -1;
	private int category4Cnt = -1;
	private int category5Cnt = -1;
	private int category6Cnt = -1;
	private int category7Cnt = -1;
	private int category8Cnt = -1;
	private int category9Cnt = -1;
	private int category10Cnt = -1;
	private int category11Cnt = -1;
	private int category12Cnt = -1;
	private int category13Cnt = -1;
	private int category14Cnt = -1;
	private int category15Cnt = -1;
	private int category16Cnt = -1;
	private int category17Cnt = -1;
	private int category18Cnt = -1;
	private int category19Cnt = -1;
	private int total;
	
	public void addCategoryCnt(int categoryCnt, int idx) {
		if (this.dataArray.size() == idx) {
			this.dataArray.add(new ArrayList<Integer>());
		}
		this.dataArray.get(idx).add(categoryCnt);
	}
	
	/**
	 * MOST USED OSS & License CHART
	 * */
	private int pieSize;
	private String columnName;
	private int columnCnt;
	private int diffMonthCnt;
	private int categorySize;
	
	/**
	 * UPDATED OSS & License CHART
	 * */
	private List<String> categoryList;
	
	public void addCategoryList(String columnName) {
		if (this.categoryList == null) {
			this.categoryList = new ArrayList<String>();
		}
		this.categoryList.add(columnName);
	}
	
	public void setTotal() {
		int total = 0;
		total += this.category0Cnt > 0 ? this.category0Cnt : 0;
		total += this.category1Cnt > 0 ? this.category1Cnt : 0;
		total += this.category2Cnt > 0 ? this.category2Cnt : 0;
		total += this.category3Cnt > 0 ? this.category3Cnt : 0;
		total += this.category4Cnt > 0 ? this.category4Cnt : 0;
		total += this.category5Cnt > 0 ? this.category5Cnt : 0;
		total += this.category6Cnt > 0 ? this.category6Cnt : 0;
		total += this.category7Cnt > 0 ? this.category7Cnt : 0;
		total += this.category8Cnt > 0 ? this.category8Cnt : 0;
		total += this.category9Cnt > 0 ? this.category9Cnt : 0;
		total += this.category10Cnt > 0 ? this.category10Cnt : 0;
		total += this.category11Cnt > 0 ? this.category11Cnt : 0;
		total += this.category12Cnt > 0 ? this.category12Cnt : 0;
		total += this.category13Cnt > 0 ? this.category13Cnt : 0;
		total += this.category14Cnt > 0 ? this.category14Cnt : 0;
		total += this.category15Cnt > 0 ? this.category15Cnt : 0;
		total += this.category16Cnt > 0 ? this.category16Cnt : 0;
		total += this.category17Cnt > 0 ? this.category17Cnt : 0;
		total += this.category18Cnt > 0 ? this.category18Cnt : 0;
		total += this.category19Cnt > 0 ? this.category19Cnt : 0;
		
		this.total = total;
	}
}
