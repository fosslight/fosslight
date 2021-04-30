/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeDtlBean extends ComBean implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
    /** 코드번호. */
    private String cdNo;
    
    /** 서브 코드 번호. */
    private String cdSubNo;
    
    /** 코드명. */
    private String cdNm;
    
    /** 코드상세번호. */
    private String cdDtlNo;
    
    /** 코드상세명. */
    private String cdDtlNm;
    
    /** 코드상세명2. */
    private String cdDtlNm2;
    
    /** 코드상세설명. */
    private String cdDtlExp;
    
    /** 코드우선순위. */
    private String cdOrder;
    
    private String useYn;
    
	/**
	 * Gets the field values as JsonObject.
	 *
	 * @return the json object
	 */
	public JsonObject toJson(){
		JsonObject jo = new JsonObject();
		jo.addProperty("cdNo", this.cdNo);
		jo.addProperty("cdSubNo", this.cdSubNo);
		jo.addProperty("cdNm", this.cdNm);
		jo.addProperty("cdDtlNo", this.cdDtlNo);
		jo.addProperty("cdDtlNm", this.cdDtlNm);
		jo.addProperty("cdDtlNm2", this.cdDtlNm2);
		jo.addProperty("cdDtlExp", this.cdDtlExp);
		jo.addProperty("cdOrder", this.cdOrder);

		return jo;
	}

}
