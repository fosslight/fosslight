/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;


/**
 * The Class batMaster.
 */
public class ProjectWatcher extends ComBean implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5429990726160434346L;
	
	
	/** The prj id. */
	private String prjId;
	
	/** The division. */
	private String division;
	
	/** The user id. */
	private String userId;
	
	/** The user id. */
	private String userName;
	
	/** The email. */
	private String email;
	
	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param userName the new user name
	 */
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
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the prj id.
	 *
	 * @return the prj id
	 */
	public String getPrjId() {
		return prjId;
	}

	/**
	 * Sets the prj id.
	 *
	 * @param prjId the new prj id
	 */
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	
}