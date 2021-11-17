/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
/**
 * JF_FILE Table Entity
 * 파일관리
 * 
 * @author ks-choi
 *
 */
public class File extends ComBean implements Serializable{
	
	private static final long serialVersionUID = 6658712585854634962L;
	
	private String fileSeq;						//파일 순번 (A.I) 20160524 ms-kwon
	private String fileId;						// 파일 ID
	private String gubn;					// 파일 구분값 [A:All, R:Role, U:Login USER, P: Password]
	private String gubnChk;					// GUBN 필드값에 따른 체크값 [A:NULL, R:권한명, U:NULL, P: 암호]
	private String mId;						// 게시판 ID
	private String bId;						// 게시글 ID
	private String origNm;					// 원본 파일명
	private String logiNm;					// 저장된 물리 파일명
	private String logiThumbNm;			// 저장된 물리 파일명(썸네일)
	private String logiPath;					// 저장된 파일 경로
	private String logiThumbPath;		// 저장된 파일 경로(썸네일)
	private String ext;						// 확장자
	private String size;						// 크기
	private String etpId;	
	
	private int width;
	private int height;
	
	private String resultCode;					// 파일 처리 결과코드값
	private String[] fileSeqs;
	private String reuseFlag;
	private String refPrjId;

	public File() {
		super();
	}
	public File(String fileSeq) {
		super();
		this.fileSeq = fileSeq;
	}
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
	public String getGubnChk() {
		return gubnChk;
	}
	public void setGubnChk(String gubnChk) {
		this.gubnChk = gubnChk;
	}
	public String getmId() {
		return mId;
	}
	public void setmId(String mId) {
		this.mId = mId;
	}
	public String getbId() {
		return bId;
	}
	public void setbId(String bId) {
		this.bId = bId;
	}
	public String getOrigNm() {
		return origNm;
	}
	public void setOrigNm(String origNm) {
		this.origNm = origNm;
	}
	public String getLogiNm() {
		return logiNm;
	}
	public void setLogiNm(String logiNm) {
		this.logiNm = logiNm;
	}
	public String getLogiThumbNm() {
		return logiThumbNm;
	}
	public void setLogiThumbNm(String logiThumbNm) {
		this.logiThumbNm = logiThumbNm;
	}
	public String getLogiPath() {
		return logiPath;
	}
	public void setLogiPath(String logiPath) {
		this.logiPath = logiPath;
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
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public String[] getFileSeqs() {
        return fileSeqs != null ? fileSeqs.clone() : null;
	}
	public void setFileSeqs(String[] fileSeqs) {
		this.fileSeqs = fileSeqs;
	}
	public String getEtpId() {
		return etpId;
	}
	public void setEtpId(String etpId) {
		this.etpId = etpId;
	}
	public String getReuseFlag() {
		return reuseFlag;
	}
	public void setReuseFlag(String reuseFlag) {
		this.reuseFlag = reuseFlag;
	}	
	public String getRefPrjId() {
		return refPrjId;
	}
	public void setRefPrjId(String refPrjId) {
		this.refPrjId = refPrjId;
	}
}	