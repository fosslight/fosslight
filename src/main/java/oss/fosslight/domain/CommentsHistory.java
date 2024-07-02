/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;

/**
 * The Class commentsHistory.
 */
public class CommentsHistory extends ComBean implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5662786622538214724L;
	
	/** The comm id. */
	private String commId;
	
	/** The reference id. */
	private String referenceId;
	
	/** The reference div. */
	private String referenceDiv;
	
	/** The contents. */
	private String contents;
	
	/** The use yn. */
	private String useYn;
	
	/** The mail type. */
	private String mailType;
	
	/** The mail send type. */
	private String mailSendType;
	
	/** The status. */
	private String status;
	
	/** The status code. */
    private String statusCode;
	
	/** The sch reference div. */
	private String schReferenceDiv;
	
	/** The sch keyword. */
	private String schKeyword;

	/** The expansion 1. */
	private String expansion1;

	/** The readYn. */
	private String readYn;

	/** The prjName. */
	private String prjName;

	/** The reviewer. */
	private String reviewer;

	/** The user. */
	private String user;

	/** The prjDivisionId. */
	private String prjDivisionId;
	
	/* 2018-07-20 choye 추가 */
	/** The commentsMode. */
	private String commentsMode;
	
	/** The delOsdd. */
	private String delOsdd;
	
	private String moreFlag = CoConstDef.FLAG_NO;
	
	/**
	 * Gets the comm id.
	 *
	 * @return the comm id
	 */
	public String getCommId() {
		return commId;
	}
	
	/**
	 * Sets the comm id.
	 *
	 * @param commId the new comm id
	 */
	public void setCommId(String commId) {
		this.commId = commId;
	}
	
	/**
	 * Gets the reference id.
	 *
	 * @return the reference id
	 */
	public String getReferenceId() {
		return referenceId;
	}
	
	/**
	 * Sets the reference id.
	 *
	 * @param referenceId the new reference id
	 */
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	
	/**
	 * Gets the reference div.
	 *
	 * @return the reference div
	 */
	public String getReferenceDiv() {
		return referenceDiv;
	}
	
	/**
	 * Sets the reference div.
	 *
	 * @param referenceDiv the new reference div
	 */
	public void setReferenceDiv(String referenceDiv) {
		this.referenceDiv = referenceDiv;
	}
	
	/**
	 * Gets the contents.
	 *
	 * @return the contents
	 */
	public String getContents() {
		return contents;
	}
	
	/**
	 * Sets the contents.
	 *
	 * @param contents the new contents
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}
	
	/**
	 * Gets the use yn.
	 *
	 * @return the use yn
	 */
	public String getUseYn() {
		return useYn;
	}
	
	/**
	 * Sets the use yn.
	 *
	 * @param useYn the new use yn
	 */
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	
	/**
	 * Gets the mail type.
	 *
	 * @return the mail type
	 */
	public String getMailType() {
		return mailType;
	}
	
	/**
	 * Sets the mail type.
	 *
	 * @param mailType the new mail type
	 */
	public void setMailType(String mailType) {
		this.mailType = mailType;
	}
	
	/**
	 * Gets the mail send type.
	 *
	 * @return the mail send type
	 */
	public String getMailSendType() {
		return mailSendType;
	}
	
	/**
	 * Sets the mail send type.
	 *
	 * @param mailSendType the new mail send type
	 */
	public void setMailSendType(String mailSendType) {
		this.mailSendType = mailSendType;
	}
	
	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the sch reference div.
	 *
	 * @return the sch reference div
	 */
	public String getSchReferenceDiv() {
		return schReferenceDiv;
	}

	/**
	 * Sets the sch reference div.
	 *
	 * @param schReferenceDiv the new sch reference div
	 */
	public void setSchReferenceDiv(String schReferenceDiv) {
		this.schReferenceDiv = schReferenceDiv;
	}

	/**
	 * Gets the sch keyword.
	 *
	 * @return the sch keyword
	 */
	public String getSchKeyword() {
		return avoidNull(schKeyword).trim();
	}

	/**
	 * Sets the sch keyword.
	 *
	 * @param schKeyword the new sch keyword
	 */
	public void setSchKeyword(String schKeyword) {
		this.schKeyword = schKeyword;
	}

	/**
	 * Gets the sch keyword sql.
	 *
	 * @return the sch keyword sql
	 */
	public String getSchKeywordSql() {
		return CommonFunction.makeSearchQuery(this.schKeyword, "CONTENTS");
	}

	/**
	 * Gets the sch keyword list.
	 *
	 * @return the sch keyword list
	 */
	public String[] getSchKeywordList() {
		return this.schKeyword.split(" ");
	}

	/**
	 * Gets the expansion 1.
	 *
	 * @return the expansion 1
	 */
	public String getExpansion1() {
		return expansion1;
	}

	/**
	 * Sets the expansion 1.
	 *
	 * @param expansion1 the new expansion 1
	 */
	public void setExpansion1(String expansion1) {
		this.expansion1 = expansion1;
	}

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
	
	/**
	 * Sets the readYn.
	 *
	 * @param readYn the new readYn
	 */
	public String getReadYn() {
		return readYn;
	}

	public void setReadYn(String readYn) {
		this.readYn = readYn;
	}
	
	/**
	 * Sets the prjName.
	 *
	 * @param prjName the new prjName
	 */
	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	
	/**
	 * Sets the reviewer.
	 *
	 * @param reviewer the new reviewer
	 */
	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}
	
	/**
	 * Sets the user.
	 *
	 * @param user the new user
	 */
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	/**
	 * Sets the prjDivisionId.
	 *
	 * @param prjDivisionId the new prjDivisionId
	 */
	public String getPrjDivisionId() {
		return prjDivisionId;
	}

	public void setPrjDivisionId(String prjDivisionId) {
		this.prjDivisionId = prjDivisionId;
	}
	
	/**
	 * Sets the commentsMode.
	 *
	 * @param commentsMode the new commentsMode
	 */
	public String getCommentsMode() {
		return commentsMode;
	}

	public void setCommentsMode(String commentsMode) {
		this.commentsMode = commentsMode;
	}
	
	/**
	 * Sets the delOsdd.
	 *
	 * @param delOsdd the new delOsdd
	 */
	public String getDelOsdd() {
		return delOsdd;
	}

	public void setDelOsdd(String delOsdd) {
		this.delOsdd = delOsdd;
	}

	public String getMoreFlag() {
		return moreFlag;
	}

	public void setMoreFlag(String moreFlag) {
		this.moreFlag = moreFlag;
	}
	
}