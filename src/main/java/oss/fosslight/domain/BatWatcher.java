/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;


/**
 * The Class batMaster.
 */
public class BatWatcher extends ComBean implements Serializable{

	/** The Constant serialVersionUID. */
	@Serial
	private static final long serialVersionUID = 5429990726160434346L;
	
	
	/** The bat id. */
	private String batId;
	
	/** The division. */
	private String division;
	
	/** The user id. */
	private String userId;
	
	/** The user id. */
	private String userName;
	
	private String email;
	
	private String useYn;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the division.
	 *
	 * @return the division
	 */
	public String getDivision() {
		return division;
	}
	
	/**
	 * Sets the division.
	 *
	 * @param division the new division
	 */
	public void setDivision(String division) {
		this.division = division;
	}
	
	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}
	
	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	/**
	 * Gets the bat id.
	 *
	 * @return the bat id
	 */
	public String getBatId() {
		return batId;
	}
	
	/**
	 * Sets the bat id.
	 *
	 * @param batId the new bat id
	 */
	public void setBatId(String batId) {
		this.batId = batId;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
}