/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;


public class Dashboard extends ComBean implements Serializable {

	private static final long serialVersionUID = 9018255826730935058L;

	private String id;
	private String name;
	private String status;
	private String review;
	private String time;
	private String type;
	private String version;
	private String license;
	private String identitier;
	private String website;
	private String ossNoticeDueDate;
	
	public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getReview() {
        return review;
    }
    public void setReview(String review) {
        this.review = review;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getLicense() {
        return license;
    }
    public void setLicense(String license) {
        this.license = license;
    }
    public String getIdentitier() {
        return identitier;
    }
    public void setIdentitier(String identitier) {
        this.identitier = identitier;
    }
    public String getWebsite() {
        return website;
    }
    public void setWebsite(String website) {
        this.website = website;
    }
    public String getOssNoticeDueDate() {
		return ossNoticeDueDate;
	}
	public void setOssNoticeDueDate(String ossNoticeDueDate) {
		this.ossNoticeDueDate = ossNoticeDueDate;
	}
	
	
}
