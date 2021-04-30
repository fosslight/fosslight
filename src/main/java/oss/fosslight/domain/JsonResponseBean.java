/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonResponseBean {
	
	public static final Integer STATUS_CODE_SUCCESS = 1;
	public static final Integer STATUS_CODE_FAIL = 0;
	
	public static final String STATUS_CODE_SUCCESS_STR = "success";
	public static final String STATUS_CODE_FAIL_STR = "fail";
	
	private Integer statusCode;		// 성공(1) 혹은 실패(0)
	private String statusMessage;	// 성공 혹은 실패 메시지(messageProperties[성공, 실패])
	private JsonElement data;			// 데이터 (select = 조회한 데이터들, insert/update/delete = 적용된 row수)
    
	// Default Success Value Setup
	public JsonResponseBean() {
		this.statusCode = STATUS_CODE_SUCCESS;
		this.statusMessage = STATUS_CODE_SUCCESS_STR;
	}
    
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public JsonElement getData() {
		return data;
	}
	public void setData(JsonElement data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("statusCode", statusCode);
		jsonObject.addProperty("statusMessage", statusMessage == null ? "" : statusMessage);
		jsonObject.add("data", data == null ? new JsonObject() : data);
		return jsonObject.toString();
	}
}