/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;

public class T2File extends ComBean implements Serializable {

	@Serial
	private static final long serialVersionUID = -5448637543126888136L;

	private String fileSeq;
	private String fileId;
	private String gubn;
	private String beforeOrigNm;
	private String origNm;
	private String logiNm;
	private String logiPath;
	private String logiThumbNm;
	private String logiThumbPath;
	private String ext;
	private String size;
	private String contentType;
	
	private String orgFileId;
	private String tabNm;
	private String reuseCnt;
	private String actualFileNm;

	public T2File(String fileSeq) {
		super();
		this.fileSeq = fileSeq;
	}
	
	public T2File() {}

	public String getFileSeq() {
		return fileSeq;
	}
	public void setFileSeq(String fileSeq) {
		this.fileSeq = fileSeq;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getGubn() {
		return gubn;
	}
	public void setGubn(String gubn) {
		this.gubn = gubn;
	}
	public String getBeforeOrigNm() {
		return beforeOrigNm;
	}

	public void setBeforeOrigNm(String beforeOrigNm) {
		if (beforeOrigNm != null) {
			beforeOrigNm = beforeOrigNm.replaceAll("\t", "").trim();
		}
		this.beforeOrigNm = beforeOrigNm;
	}
	
	public String getOrigNm() {
		return origNm;
	}
	
	public void setOrigNm(String origNm) {
		if (origNm != null) {
			origNm = origNm.replaceAll("\t", "").trim();
		}
		this.origNm = origNm;
	}
	public String getLogiNm() {
		return logiNm;
	}
	public void setLogiNm(String logiNm) {
		if (logiNm != null) {
			logiNm = logiNm.replaceAll("\t", "").trim();
		}
		this.logiNm = logiNm;
	}
	public String getLogiPath() {
		return logiPath;
	}
	public void setLogiPath(String logiPath) {
		this.logiPath = logiPath;
	}
	public String getLogiThumbNm() {
		return logiThumbNm;
	}
	public void setLogiThumbNm(String logiThumbNm) {
		this.logiThumbNm = logiThumbNm;
	}
	public String getLogiThumbPath() {
		return logiThumbPath;
	}
	public void setLogiThumbPath(String logiThumbPath) {
		this.logiThumbPath = logiThumbPath;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}

	public String getOrgFileId() {
		return orgFileId;
	}

	public void setOrgFileId(String orgFileId) {
		this.orgFileId = orgFileId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getTabGubn() {
		return tabNm;
	}
	public void setTabGubn(String tabNm) {
		this.tabNm = tabNm;
	}

	public String getReuseCnt() {
		return reuseCnt;
	}

	public void setReuseCnt(String reuseCnt) {
		this.reuseCnt = reuseCnt;
	}

	public String getActualFileNm() {
		return actualFileNm;
	}

	public void setActualFileNm(String actualFileNm) {
		this.actualFileNm = actualFileNm;
	}
}
