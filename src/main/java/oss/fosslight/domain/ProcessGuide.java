/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

public class ProcessGuide extends ComBean implements Serializable {

	private static final long serialVersionUID = -1474263356907429804L;
	
	private String id;
	private String pageTarget;
	private String contents;
	private String replaceContents;
	private String url;
	private String useYn;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPageTarget() {
		return pageTarget;
	}
	public void setPageTarget(String pageTarget) {
		this.pageTarget = pageTarget;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public String getReplaceContents() {
		return replaceContents;
	}
	public void setReplaceContents(String replaceContents) {
		this.replaceContents = replaceContents;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	
}
