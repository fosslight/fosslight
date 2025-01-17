/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;


/**
 * The Class partnerMaster
 */
public class PartnerWatcher extends ComBean implements Serializable{

	private static final long serialVersionUID = 5429990726160434346L;
	
	
	private String partnerId;
	private String division;
	private String userId;
	private String userName;
	private String email;
	
	private String parDivision;
	private String parDivisionName;
	private String parUserId;
	private String parUserName;
	private String parEmail;
	
	public String getPartnerId() {
		return partnerId;
	}
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getParDivision() {
		return parDivision;
	}
	public void setParDivision(String parDivision) {
		this.parDivision = parDivision;
	}
	public String getParDivisionName() {
		return parDivisionName;
	}
	public void setParDivisionName(String parDivisionName) {
		this.parDivisionName = parDivisionName;
	}
	public String getParUserId() {
		return parUserId;
	}
	public void setParUserId(String parUserId) {
		this.parUserId = parUserId;
	}
	public String getParUserName() {
		return parUserName;
	}
	public void setParUserName(String parUserName) {
		this.parUserName = parUserName;
	}
	public String getParEmail() {
		return parEmail;
	}
	public void setParEmail(String parEmail) {
		this.parEmail = parEmail;
	}
	@Override
	public String toString() {
		return "PartnerWatcher [partnerId=" + partnerId + ", division=" + division + ", userId=" + userId + "]";
	}
}