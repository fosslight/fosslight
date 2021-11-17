/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package  oss.fosslight.domain;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoMail extends ComBean {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6978575958803751406L;

	/** The snd seq. */
	private String sndSeq;		// 발송순번
	
	/** The msg type. */
	private String msgType;		// 구분
	
	/** The eml title. */
	private String emlTitle;	// 메일제목
	
	/** The eml message. */
	private String emlMessage;	// 메일내용
	
	/** The eml to. */
	private String emlTo;	// 메일내용
	
	/** The eml cc. */
	private String emlCc;	// 메일내용
	
	/** The eml from. */
	private String emlFrom		= "Fosslight";		// 발송자
	
	/** The snd status. */
	private String sndStatus	= "S";	// 발송상태(전송:S, 완료:C, 전송 시간 초과: G)
	
	/** The use flag. */
	private String useFlag;		// 사용여부
	
	/** The creation date. */
	private String creationDate;	// 등록일
	
	/** The creation user id. */
	private String creationUserId;	// 등록자
	
	/** The last update date. */
	private String lastUpdateDate;	// 수정일
	
	/** The last update user id. */
	private String lastUpdateUserId;// 수정자
	
	/** The error msg. */
	private String errorMsg;		// 에러 메시지

	/** The param oss id. */
	private String paramOssId;
	
	/** The param oss ids. */
	private List<String> paramOssKey;
	
	/** The param license id. */
	private String paramLicenseId;
	
	/** The param prj id. */
	private String paramPrjId;
	
	/** The param partner id. */
	private String paramPartnerId;
	
	/** The param bat id. */
	private String paramBatId;
	
	/** The comment. */
	private String comment;
	
	/** The compare data before. */
	private Object compareDataBefore; // 변경사항 (변경전)
	
	/** The compare data after. */
	private Object compareDataAfter; // 변경사항 (변경후)
	
	/** The param prj list. */
	private List<Project> paramPrjList;
	
	/** The receive flag. */
	private String receiveFlag;
	
	/** The param email. */
	private String paramEmail;
	
	/** The param user id. */
	private String paramUserId;
	
	/** The param oss info. */
	private OssMaster paramOssInfo;
	
	/** The param prj info. */
	private Project paramPrjInfo;
	
	/** The param oss list. */
	private List<OssMaster> paramOssList;
	
	/** The param expansion 1. */
	private String paramExpansion1;
	
	/** The param expansion 2. */
	private String paramExpansion2;
	
	/** The param expansion 3. */
	private String paramExpansion3;
	
	/** The param stage. */
	private String stage;
	
	private String jobType;
	
	private Map<String, OssMaster> paramOssInfoMap;
	
	
	/**  Informations *. */
	

	/** Custom Setting */
	private String[] toIds;		// 수신 아이디들
	
	/** The cc ids. */
	private String[] ccIds = new String[]{};		// 참조 아이디들
	
	/** The bcc ids. */
	private String[] bccIds = new String[]{};	// 숨김 참조 아이디들
	
	private String binaryCommitResult;
	
	/**
	 *  //.
	 *
	 * @return the snd seq
	 */
	
	public String getSndSeq() {
		return sndSeq;
	}
	
	/**
	 * Sets the snd seq.
	 *
	 * @param sndSeq the new snd seq
	 */
	public void setSndSeq(String sndSeq) {
		this.sndSeq = sndSeq;
	}
	
	/**
	 * Gets the msg type.
	 *
	 * @return the msg type
	 */
	public String getMsgType() {
		return msgType;
	}
	
	/**
	 * Sets the msg type.
	 *
	 * @param msgType the new msg type
	 */
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	
	/**
	 * Gets the eml title.
	 *
	 * @return the eml title
	 */
	public String getEmlTitle() {
		return emlTitle;
	}
	
	/**
	 * Sets the eml title.
	 *
	 * @param emlTitle the new eml title
	 */
	public void setEmlTitle(String emlTitle) {
		this.emlTitle = emlTitle;
	}
	
	/**
	 * Gets the eml message.
	 *
	 * @return the eml message
	 */
	public String getEmlMessage() {
		return emlMessage;
	}
	
	/**
	 * Sets the eml message.
	 *
	 * @param emlMessage the new eml message
	 */
	public void setEmlMessage(String emlMessage) {
		this.emlMessage = emlMessage;
	}
	
	/**
	 * Gets the eml from.
	 *
	 * @return the eml from
	 */
	public String getEmlFrom() {
		return emlFrom;
	}
	
	/**
	 * Sets the eml from.
	 *
	 * @param emlFrom the new eml from
	 */
	public void setEmlFrom(String emlFrom) {
		this.emlFrom = emlFrom;
	}
	
	/**
	 * Gets the snd status.
	 *
	 * @return the snd status
	 */
	public String getSndStatus() {
		return sndStatus;
	}
	
	/**
	 * Sets the snd status.
	 *
	 * @param sndStatus the new snd status
	 */
	public void setSndStatus(String sndStatus) {
		this.sndStatus = sndStatus;
	}
	
	/**
	 * Gets the use flag.
	 *
	 * @return the use flag
	 */
	public String getUseFlag() {
		return useFlag;
	}
	
	/**
	 * Sets the use flag.
	 *
	 * @param useFlag the new use flag
	 */
	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
	}
	
	/**
	 * Gets the creation date.
	 *
	 * @return the creation date
	 */
	public String getCreationDate() {
		return creationDate;
	}
	
	/**
	 * Sets the creation date.
	 *
	 * @param creationDate the new creation date
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	/**
	 * Gets the creation user id.
	 *
	 * @return the creation user id
	 */
	public String getCreationUserId() {
		return creationUserId;
	}
	
	/**
	 * Sets the creation user id.
	 *
	 * @param creationUserId the new creation user id
	 */
	public void setCreationUserId(String creationUserId) {
		this.creationUserId = creationUserId;
	}
	
	/**
	 * Gets the last update date.
	 *
	 * @return the last update date
	 */
	public String getLastUpdateDate() {
		return lastUpdateDate;
	}
	
	/**
	 * Sets the last update date.
	 *
	 * @param lastUpdateDate the new last update date
	 */
	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	/**
	 * Gets the last update user id.
	 *
	 * @return the last update user id
	 */
	public String getLastUpdateUserId() {
		return lastUpdateUserId;
	}
	
	/**
	 * Sets the last update user id.
	 *
	 * @param lastUpdateUserId the new last update user id
	 */
	public void setLastUpdateUserId(String lastUpdateUserId) {
		this.lastUpdateUserId = lastUpdateUserId;
	}
	
	/**
	 * Gets the error msg.
	 *
	 * @return the error msg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	
	/**
	 * Sets the error msg.
	 *
	 * @param errorMsg the new error msg
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	/**
	 *  Custom Setting.
	 *
	 * @return the str to ids
	 */
	public String getStrToIds() {
		return toIds != null ? StringUtils.join(toIds, "|") : null;
	}
	
	/**
	 * Sets the str to ids.
	 *
	 * @param strToIds the new str to ids
	 */
	public void setStrToIds(String strToIds) {
		this.toIds = strToIds != null ? StringUtils.split(strToIds, "|") : null;
	}
	
	/**
	 * Gets the str to cc ids.
	 *
	 * @return the str to cc ids
	 */
	public String getStrToCcIds() {
		return ccIds != null ? StringUtils.join(ccIds, "|") : null;
	}
	
	/**
	 * Sets the str to cc ids.
	 *
	 * @param strToCcIds the new str to cc ids
	 */
	public void setStrToCcIds(String strToCcIds) {
		this.ccIds = strToCcIds != null ? StringUtils.split(strToCcIds, "|") : null;
	}
	
	/**
	 * Gets the str to bcc ids.
	 *
	 * @return the str to bcc ids
	 */
	public String getStrToBccIds() {
		return bccIds != null ? StringUtils.join(bccIds, "|") : null;
	}
	
	/**
	 * Sets the str to bcc ids.
	 *
	 * @param strToBccIds the new str to bcc ids
	 */
	public void setStrToBccIds(String strToBccIds) {
		this.bccIds = strToBccIds != null ? StringUtils.split(strToBccIds, "|") : null;
	}
	
	/**
	 * Gets the to ids.
	 *
	 * @return the to ids
	 */
	public String[] getToIds() {
		return toIds;
	}
	
	/**
	 * Sets the to ids.
	 *
	 * @param toIds the new to ids
	 */
	public void setToIds(String[] toIds) {
		List<String> temp = new ArrayList<>();
		if(toIds != null) {
			for(String s : toIds) {
				if(!isEmpty(s)) {
					temp.add(s.trim());
				}
			}
		}
		
		this.toIds = temp.toArray(new String[temp.size()]);
	}
	
	/**
	 * Gets the cc ids.
	 *
	 * @return the cc ids
	 */
	public String[] getCcIds() {
		return ccIds;
	}
	
	/**
	 * Sets the cc ids.
	 *
	 * @param ccIds the new cc ids
	 */
	public void setCcIds(String[] ccIds) {
		List<String> temp = new ArrayList<>();
		if(ccIds != null) {
			for(String s : ccIds) {
				if(!isEmpty(s)) {
					temp.add(s.trim());
				}
			}
		}
		this.ccIds = temp.toArray(new String[temp.size()]);
	}
	
	/**
	 * Gets the bcc ids.
	 *
	 * @return the bcc ids
	 */
	public String[] getBccIds() {
		return bccIds;
	}
	
	/**
	 * Sets the bcc ids.
	 *
	 * @param bccIds the new bcc ids
	 */
	public void setBccIds(String[] bccIds) {
		List<String> temp = new ArrayList<>();
		if(bccIds != null) {
			for(String s : bccIds) {
				if(!isEmpty(s)) {
					temp.add(s.trim());
				}
			}
		}
		this.bccIds = temp.toArray(new String[temp.size()]);
	}
	
	/**
	 *  //.
	 *
	 * @return the param oss id
	 */
	
	public String getParamOssId() {
		return paramOssId;
	}
	
	/**
	 * Gets the param prj id.
	 *
	 * @return the param prj id
	 */
	public String getParamPrjId() {
		return paramPrjId;
	}
	
	/**
	 * Sets the param prj id.
	 *
	 * @param paramPrjId the new param prj id
	 */
	public void setParamPrjId(String paramPrjId) {
		this.paramPrjId = paramPrjId;
	}
	
	/**
	 * Gets the param license id.
	 *
	 * @return the param license id
	 */
	public String getParamLicenseId() {
		return paramLicenseId;
	}
	
	/**
	 * Sets the param license id.
	 *
	 * @param paramLicenseId the new param license id
	 */
	public void setParamLicenseId(String paramLicenseId) {
		this.paramLicenseId = paramLicenseId;
	}
	
	/**
	 * Gets the comment.
	 *
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * Sets the comment.
	 *
	 * @param comment the new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Sets the param oss id.
	 *
	 * @param paramOssId the new param oss id
	 */
	public void setParamOssId(String paramOssId) {
		this.paramOssId = paramOssId;
	}
	
	/**
	 * Instantiates a new co mail.
	 *
	 * @param mailType the mail type
	 */
	public CoMail(String mailType) {
		setMsgType(mailType);
	}
	
	/**
	 * Instantiates a new co mail.
	 */
	public CoMail() {}
	
	/**
	 *  UTILITIES *.
	 *
	 * @param path the path
	 * @param model the model
	 * @return the string
	 */
	public String geVelocityTemplateContent(String path, Map<String, Object> model) {
		VelocityContext context = new VelocityContext();
		Writer writer = new StringWriter();
		VelocityEngine vf = new VelocityEngine();
		Properties props = new Properties();
		context.put("TEMPLATE_URL", path); // context 정보
		
	    props.put("resource.loader", "class");
	    props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    props.put("input.encoding", "UTF-8");
	    
	    vf.init(props);
	    
		try {
			Template template = vf.getTemplate(""); // file name
			template.merge(context, writer);
			
			return writer.toString();
		} catch (Exception e) {
			System.out.println("Exception occured while processing velocity template");
			log.error(e.getMessage());
		}
		return "";
	}
	
	/**
	 * Gets the compare data before.
	 *
	 * @return the compare data before
	 */
	public Object getCompareDataBefore() {
		return compareDataBefore;
	}
	
	/**
	 * Sets the compare data before.
	 *
	 * @param compareDataBefore the new compare data before
	 */
	public void setCompareDataBefore(Object compareDataBefore) {
		this.compareDataBefore = compareDataBefore;
	}
	
	/**
	 * Gets the compare data after.
	 *
	 * @return the compare data after
	 */
	public Object getCompareDataAfter() {
		return compareDataAfter;
	}
	
	/**
	 * Sets the compare data after.
	 *
	 * @param compareDataAfter the new compare data after
	 */
	public void setCompareDataAfter(Object compareDataAfter) {
		this.compareDataAfter = compareDataAfter;
	}
	
	/**
	 * Gets the param partner id.
	 *
	 * @return the param partner id
	 */
	public String getParamPartnerId() {
		return paramPartnerId;
	}
	
	/**
	 * Sets the param partner id.
	 *
	 * @param paramPartnerId the new param partner id
	 */
	public void setParamPartnerId(String paramPartnerId) {
		this.paramPartnerId = paramPartnerId;
	}
	
	/**
	 * Gets the param bat id.
	 *
	 * @return the param bat id
	 */
	public String getParamBatId() {
		return paramBatId;
	}
	
	/**
	 * Sets the param bat id.
	 *
	 * @param paramBatId the new param bat id
	 */
	public void setParamBatId(String paramBatId) {
		this.paramBatId = paramBatId;
	}
	
	/**
	 * Gets the param oss ids.
	 *
	 * @return the param oss ids
	 */
	public List<String> getParamOssKey() {
		return paramOssKey;
	}
	
	/**
	 * Sets the param oss ids.
	 *
	 * @param paramOssIds the new param oss ids
	 */
	public void setParamOssKey(List<String> paramOssKey) {
		this.paramOssKey = paramOssKey;
	}
	
	/**
	 * Gets the param prj list.
	 *
	 * @return the param prj list
	 */
	public List<Project> getParamPrjList() {
		return paramPrjList;
	}
	
	/**
	 * Sets the param prj list.
	 *
	 * @param paramPrjList the new param prj list
	 */
	public void setParamPrjList(List<Project> paramPrjList) {
		this.paramPrjList = paramPrjList;
	}
	
	/**
	 * Gets the receive flag.
	 *
	 * @return the receive flag
	 */
	public String getReceiveFlag() {
		return receiveFlag;
	}
	
	/**
	 * Sets the receive flag.
	 *
	 * @param receiveFlag the new receive flag
	 */
	public void setReceiveFlag(String receiveFlag) {
		this.receiveFlag = receiveFlag;
	}
	
	/**
	 * Gets the param email.
	 *
	 * @return the param email
	 */
	public String getParamEmail() {
		return paramEmail;
	}
	
	/**
	 * Sets the param email.
	 *
	 * @param paramEmail the new param email
	 */
	public void setParamEmail(String paramEmail) {
		this.paramEmail = paramEmail;
	}
	
	/**
	 * Gets the param user id.
	 *
	 * @return the param user id
	 */
	public String getParamUserId() {
		return paramUserId;
	}
	
	/**
	 * Sets the param user id.
	 *
	 * @param paramUserId the new param user id
	 */
	public void setParamUserId(String paramUserId) {
		this.paramUserId = paramUserId;
	}
	
	/**
	 * Gets the param oss info.
	 *
	 * @return the paramOssInfo
	 */
	public OssMaster getParamOssInfo() {
		return paramOssInfo;
	}
	
	/**
	 * Sets the param oss info.
	 *
	 * @param paramOssInfo the paramOssInfo to set
	 */
	public void setParamOssInfo(OssMaster paramOssInfo) {
		this.paramOssInfo = paramOssInfo;
	}
	
	/**
	 * Gets the param oss list.
	 *
	 * @return the paramOssList
	 */
	public List<OssMaster> getParamOssList() {
		return paramOssList;
	}
	
	/**
	 * Sets the param oss list.
	 *
	 * @param paramOssList the paramOssList to set
	 */
	public void setParamOssList(List<OssMaster> paramOssList) {
		this.paramOssList = paramOssList;
	}
	
	/**
	 * Gets the param expansion 1.
	 *
	 * @return the param expansion 1
	 */
	public String getParamExpansion1() {
		return paramExpansion1;
	}
	
	/**
	 * Sets the param expansion 1.
	 *
	 * @param paramExpansion1 the new param expansion 1
	 */
	public void setParamExpansion1(String paramExpansion1) {
		this.paramExpansion1 = paramExpansion1;
	}
	
	/**
	 * Gets the param expansion 2.
	 *
	 * @return the param expansion 2
	 */
	public String getParamExpansion2() {
		return paramExpansion2;
	}
	
	/**
	 * Sets the param expansion 2.
	 *
	 * @param paramExpansion2 the new param expansion 2
	 */
	public void setParamExpansion2(String paramExpansion2) {
		this.paramExpansion2 = paramExpansion2;
	}
	
	/**
	 * Gets the param expansion 3.
	 *
	 * @return the param expansion 3
	 */
	public String getParamExpansion3() {
		return paramExpansion3;
	}
	
	/**
	 * Sets the param expansion 3.
	 *
	 * @param paramExpansion3 the new param expansion 3
	 */
	public void setParamExpansion3(String paramExpansion3) {
		this.paramExpansion3 = paramExpansion3;
	}
	
	/**
	 * Gets the param prj info.
	 *
	 * @return the param prj info
	 */
	public Project getParamPrjInfo() {
		return paramPrjInfo;
	}
	
	/**
	 * Sets the param prj info.
	 *
	 * @param paramPrjInfo the new param prj info
	 */
	public void setParamPrjInfo(Project paramPrjInfo) {
		this.paramPrjInfo = paramPrjInfo;
	}
	
	/**
	 * Gets the eml to.
	 *
	 * @return the eml to
	 */
	public String getEmlTo() {
		return emlTo;
	}
	
	/**
	 * Sets the eml to.
	 *
	 * @param emlTo the new eml to
	 */
	public void setEmlTo(String emlTo) {
		this.emlTo = emlTo;
	}
	
	/**
	 * Gets the eml cc.
	 *
	 * @return the eml cc
	 */
	public String getEmlCc() {
		return emlCc;
	}
	
	/**
	 * Sets the eml cc.
	 *
	 * @param emlCc the new eml cc
	 */
	public void setEmlCc(String emlCc) {
		this.emlCc = emlCc;
	}
	
	/**
	 * Gets the stage.
	 *
	 * @return the stage
	 */
	public String getStage() {
		return stage;
	}
	
	/**
	 * Sets the stage.
	 *
	 * @param stage the new stage
	 */
	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public Map<String, OssMaster> getParamOssInfoMap() {
		return paramOssInfoMap;
	}

	public void setParamOssInfoMap(Map<String, OssMaster> paramOssInfoMap) {
		this.paramOssInfoMap = paramOssInfoMap;
	}

	public String getBinaryCommitResult() {
		return binaryCommitResult;
	}

	public void setBinaryCommitResult(String binaryCommitResult) {
		this.binaryCommitResult = binaryCommitResult;
	}

 } 