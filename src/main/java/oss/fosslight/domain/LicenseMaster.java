/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import oss.fosslight.common.CommonFunction;
import oss.fosslight.util.StringUtil;

/**
 * The Class LicenseMaster.
 */
public class LicenseMaster extends ComBean implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2103290178702190921L;
	
	/** The license id. */
	private String licenseId;					//라이센스 ID
	
	/** The license name. */
	private String licenseName;					//라이센스 이름
	
	/** The license name temp. */
	private String licenseNameTemp;					//라이센스 이름
	
	/** The license type. */
	private String licenseType;					//라이센스 타입
	private String licenseTypeFull;					//라이센스 타입
	
	/** The license type val. */
	private String licenseTypeVal;					//라이센스 타입
	
	/** The license nickname. */
	private String licenseNickname;				//라이센스 닉네임
	
	/** The obligation disclosing src yn. */
	private String obligationDisclosingSrcYn;	//Obligation Option 
	
	/** The obligation notification yn. */
	private String obligationNotificationYn;	//Obligation Option
	
	/** The obligation needs check yn. */
	private String obligationNeedsCheckYn;		//Obligation Option
	
	/** The obligation. */
	private String obligation;					//Obligation HTML
	
	/** The obligation code. */
	private String obligationCode;					//Obligation HTML
	
	/** The obligation checks. */
	private String obligationChecks;			//Obligation Check 상태(고지, 소스공개, 체크)
	
	/** The short identifier. */
	private String shortIdentifier;				//Identifier
	
	/** The webpage. */
	private String webpage;						//URL
	private String webpageLinkFormat;						//URL
	private String[] webpages;
	
	/** The internal url. */
	private String internalUrl;
	
	/** The description. */
	private String description;					//descrition
	
	private String descriptionHtml;
	
	/** The license text. */
	private String licenseText;					//라이센스 Text
	
	/** The attribution. */
	private String attribution;					//attribution
	
	/** The req license text yn. */
	private String reqLicenseTextYn;			//reqLicenseTextYn
	
	/** The license nickname str. */
	private String licenseNicknameStr; // pipe구분 닉네임
	
	/** The license nicknames. */
	private String[] licenseNicknames;			
	
	/** The license nickname list. */
	private List<String> licenseNicknameList;
	
	private String obligationType;
	
	
	
	/** The c start date. */
	//날짜검색조건
	private String cStartDate;					//작성일(시작)
	
	/** The c end date. */
	private String cEndDate;					//작성일(끝)
	
	/** The m start date. */
	private String mStartDate;					//수정일(시작)
	
	/** The m end date. */
	private String mEndDate;					//수정일(끝)
	
	/** The component id. */
	//componentsLicnese
	private String componentId;
	
	/** The component license id. */
	private String componentLicenseId;
	
	/** The comment. */
	//comment 이력
	private String comment;
	
	/** The comment cont. */
	private String commentCont;
	
	/** The restriction. */
	private String restriction;
	private String restrictionStr;
	
	/** The restrictions. */
	private String restrictions;
	
	/** The arrRestriction. */
	private String[] arrRestriction;
	
	/** The restrictionList. */
	private String restrictionList;
	
	private String licenseNameAllSearchFlag;
	
	/** sort order **/
	private String sOrder;
	
	private String[] notIncludeArrRestriction;
	
	/**
	 * Instantiates a new license master.
	 */
	public LicenseMaster() {
		super();
	}
	
	/**
	 * Instantiates a new license master.
	 *
	 * @param licenseId the license id
	 */
	public LicenseMaster(String licenseId) {
		super();
		this.licenseId = licenseId;
	}
	
	/**
	 * Gets the license id.
	 *
	 * @return the license id
	 */
	public String getLicenseId() {
		return licenseId;
	}
	
	/**
	 * Sets the license id.
	 *
	 * @param licenseId the new license id
	 */
	public void setLicenseId(String licenseId) {
		this.licenseId = licenseId;
	}
	
	/**
	 * Gets the license name.
	 *
	 * @return the license name
	 */
	public String getLicenseName() {
		return licenseName;
	}
	
	/**
	 * Sets the license name.
	 *
	 * @param licenseName the new license name
	 */
	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}
	
	/**
	 * Gets the license type.
	 *
	 * @return the license type
	 */
	public String getLicenseType() {
		return licenseType;
	}
	
	/**
	 * Sets the license type.
	 *
	 * @param licenseType the new license type
	 */
	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}
	
	/**
	 * Gets the obligation notification yn.
	 *
	 * @return the obligation notification yn
	 */
	public String getObligationNotificationYn() {
		return obligationNotificationYn;
	}
	
	/**
	 * Sets the obligation notification yn.
	 *
	 * @param obligationNotificationYn the new obligation notification yn
	 */
	public void setObligationNotificationYn(String obligationNotificationYn) {
		this.obligationNotificationYn = obligationNotificationYn;
	}
	
	/**
	 * Gets the short identifier.
	 *
	 * @return the short identifier
	 */
	public String getShortIdentifier() {
		return shortIdentifier;
	}
	
	/**
	 * Sets the short identifier.
	 *
	 * @param shortIdentifier the new short identifier
	 */
	public void setShortIdentifier(String shortIdentifier) {
		this.shortIdentifier = shortIdentifier;
	}
	
	/**
	 * Gets the webpage.
	 *
	 * @return the webpage
	 */
	public String getWebpage() {
		return CommonFunction.convertUrlLinkFormat(webpage);
	}
	
	/**
	 * Sets the webpage.
	 *
	 * @param webpage the new webpage
	 */
	public void setWebpage(String webpage) {
		this.webpage = webpage;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
		if(!isEmptyWithLineSeparator(description)) {
			this.descriptionHtml = CommonFunction.makeHtmlLinkTagWithText(CommonFunction.lineReplaceToBR(description));
		}
	}
	
	/**
	 * Gets the license text.
	 *
	 * @return the license text
	 */
	public String getLicenseText() {
		return licenseText;
	}
	
	/**
	 * Sets the license text.
	 *
	 * @param licenseText the new license text
	 */
	public void setLicenseText(String licenseText) {
		this.licenseText = licenseText;
	}
	
	/**
	 * Gets the attribution.
	 *
	 * @return the attribution
	 */
	public String getAttribution() {
		return attribution;
	}
	
	/**
	 * Sets the attribution.
	 *
	 * @param attribution the new attribution
	 */
	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}
	
	/**
	 * Gets the obligation disclosing src yn.
	 *
	 * @return the obligation disclosing src yn
	 */
	public String getObligationDisclosingSrcYn() {
		return obligationDisclosingSrcYn;
	}
	
	/**
	 * Sets the obligation disclosing src yn.
	 *
	 * @param obligationDisclosingSrcYn the new obligation disclosing src yn
	 */
	public void setObligationDisclosingSrcYn(String obligationDisclosingSrcYn) {
		this.obligationDisclosingSrcYn = obligationDisclosingSrcYn;
	}
	
	/**
	 * Gets the obligation needs check yn.
	 *
	 * @return the obligation needs check yn
	 */
	public String getObligationNeedsCheckYn() {
		return obligationNeedsCheckYn;
	}
	
	/**
	 * Sets the obligation needs check yn.
	 *
	 * @param obligationNeedsCheckYn the new obligation needs check yn
	 */
	public void setObligationNeedsCheckYn(String obligationNeedsCheckYn) {
		this.obligationNeedsCheckYn = obligationNeedsCheckYn;
	}
	
	/**
	 * Gets the license nickname.
	 *
	 * @return the license nickname
	 */
	public String getLicenseNickname() {
		return licenseNickname;
	}
	
	/**
	 * Sets the license nickname.
	 *
	 * @param licenseNickname the new license nickname
	 */
	public void setLicenseNickname(String licenseNickname) {
		this.licenseNickname = licenseNickname;
	}
	
	/**
	 * Gets the obligation.
	 *
	 * @return the obligation
	 */
	public String getObligation() {
		return obligation;
	}
	
	/**
	 * Sets the obligation.
	 *
	 * @param obligation the new obligation
	 */
	public void setObligation(String obligation) {
		this.obligation = obligation;
	}
	
	/**
	 * Gets the c start date.
	 *
	 * @return the c start date
	 */
	public String getcStartDate() {
		return cStartDate;
	}
	
	/**
	 * Sets the c start date.
	 *
	 * @param cStartDate the new c start date
	 */
	public void setcStartDate(String cStartDate) {
		this.cStartDate = cStartDate;
	}
	
	/**
	 * Gets the c end date.
	 *
	 * @return the c end date
	 */
	public String getcEndDate() {
		return cEndDate;
	}
	
	/**
	 * Sets the c end date.
	 *
	 * @param cEndDate the new c end date
	 */
	public void setcEndDate(String cEndDate) {
		this.cEndDate = cEndDate;
	}
	
	/**
	 * Gets the m start date.
	 *
	 * @return the m start date
	 */
	public String getmStartDate() {
		return mStartDate;
	}
	
	/**
	 * Sets the m start date.
	 *
	 * @param mStartDate the new m start date
	 */
	public void setmStartDate(String mStartDate) {
		this.mStartDate = mStartDate;
	}
	
	/**
	 * Gets the m end date.
	 *
	 * @return the m end date
	 */
	public String getmEndDate() {
		return mEndDate;
	}
	
	/**
	 * Sets the m end date.
	 *
	 * @param mEndDate the new m end date
	 */
	public void setmEndDate(String mEndDate) {
		this.mEndDate = mEndDate;
	}
	
	/**
	 * Gets the obligation checks.
	 *
	 * @return the obligation checks
	 */
	public String getObligationChecks() {
		return obligationChecks;
	}
	
	/**
	 * Sets the obligation checks.
	 *
	 * @param obligationChecks the new obligation checks
	 */
	public void setObligationChecks(String obligationChecks) {
		this.obligationChecks = obligationChecks;
	}
	
	/**
	 * Gets the component license id.
	 *
	 * @return the component license id
	 */
	public String getComponentLicenseId() {
		return componentLicenseId;
	}
	
	/**
	 * Sets the component license id.
	 *
	 * @param componentLicenseId the new component license id
	 */
	public void setComponentLicenseId(String componentLicenseId) {
		this.componentLicenseId = componentLicenseId;
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
	 * Gets the component id.
	 *
	 * @return the component id
	 */
	public String getComponentId() {
		return componentId;
	}
	
	/**
	 * Sets the component id.
	 *
	 * @param componentId the new component id
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	
	/**
	 * Gets the serialversionuid.
	 *
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	/**
	 * Gets the license nicknames.
	 *
	 * @return the license nicknames
	 */
	public String[] getLicenseNicknames() {
		return licenseNicknames != null ? licenseNicknames.clone() : null;
	}
	
	/**
	 * Sets the license nicknames.
	 *
	 * @param licenseNicknames the new license nicknames
	 */
	public void setLicenseNicknames(String[] licenseNicknames) {
		this.licenseNicknames = licenseNicknames != null ?
			licenseNicknames.clone() : null;
	}
	
	/**
	 * Gets the req license text yn.
	 *
	 * @return the req license text yn
	 */
	public String getReqLicenseTextYn() {
		return reqLicenseTextYn;
	}
	
	/**
	 * Sets the req license text yn.
	 *
	 * @param reqLicenseTextYn the new req license text yn
	 */
	public void setReqLicenseTextYn(String reqLicenseTextYn) {
		this.reqLicenseTextYn = reqLicenseTextYn;
	}
	
	/**
	 * Gets the comment cont.
	 *
	 * @return the comment cont
	 */
	public String getCommentCont() {
		return commentCont;
	}
	
	/**
	 * Sets the comment cont.
	 *
	 * @param commentCont the new comment cont
	 */
	public void setCommentCont(String commentCont) {
		this.commentCont = commentCont;
	}
	
	/**
	 * Gets the license nickname list.
	 *
	 * @return the license nickname list
	 */
	public List<String> getLicenseNicknameList() {
		return licenseNicknameList;
	}
	
	/**
	 * Sets the license nickname list.
	 *
	 * @param licenseNicknameList the new license nickname list
	 */
	public void setLicenseNicknameList(List<String> licenseNicknameList) {
		this.licenseNicknameList = licenseNicknameList;
	}
	
	/**
	 * Adds the license nickname list.
	 *
	 * @param licenseNicknameStr the license nickname str
	 */
	public void addLicenseNicknameList(String licenseNicknameStr) {
		if(this.licenseNicknameList == null) {
			this.licenseNicknameList = new ArrayList<>();
		}
		this.licenseNicknameList.add(licenseNicknameStr);
	}
	
	/**
	 * Gets the license nickname str.
	 *
	 * @return the license nickname str
	 */
	public String getLicenseNicknameStr() {
		return licenseNicknameStr;
	}
	
	/**
	 * Sets the license nickname str.
	 *
	 * @param licenseNicknameStr the new license nickname str
	 */
	public void setLicenseNicknameStr(String licenseNicknameStr) {
		this.licenseNicknameStr = licenseNicknameStr;
	}
	
	/**
	 * Gets the license type val.
	 *
	 * @return the license type val
	 */
	public String getLicenseTypeVal() {
		return licenseTypeVal;
	}
	
	/**
	 * Sets the license type val.
	 *
	 * @param licenseTypeVal the new license type val
	 */
	public void setLicenseTypeVal(String licenseTypeVal) {
		this.licenseTypeVal = licenseTypeVal;
	}
	
	/**
	 * Gets the obligation code.
	 *
	 * @return the obligation code
	 */
	public String getObligationCode() {
		return obligationCode;
	}
	
	/**
	 * Sets the obligation code.
	 *
	 * @param obligationCode the new obligation code
	 */
	public void setObligationCode(String obligationCode) {
		this.obligationCode = obligationCode;
	}
	
	/**
	 * Gets the license name temp.
	 *
	 * @return the license name temp
	 */
	public String getLicenseNameTemp() {
		return licenseNameTemp;
	}
	
	/**
	 * Sets the license name temp.
	 *
	 * @param licenseNameTemp the new license name temp
	 */
	public void setLicenseNameTemp(String licenseNameTemp) {
		this.licenseNameTemp = licenseNameTemp;
	}
	
	/**
	 * Gets the internal url.
	 *
	 * @return the internalUrl
	 */
	public String getInternalUrl() {
		return internalUrl;
	}
	
	/**
	 * Sets the internal url.
	 *
	 * @param internalUrl the internalUrl to set
	 */
	public void setInternalUrl(String internalUrl) {
		this.internalUrl = internalUrl;
	}

	/**
	 * @return the descriptionHtml
	 */
	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	/**
	 * @param descriptionHtml the descriptionHtml to set
	 */
	public void setDescriptionHtml(String descriptionHtml) {
		this.descriptionHtml = descriptionHtml;
	}

	public String getWebpageLinkFormat() {
		if(StringUtil.isEmpty(this.webpageLinkFormat) && !StringUtil.isEmpty(this.webpage)) {
			if(this.webpage.contains(",")){
				String[] webpages = this.webpage.split(",");
				String result = "";
							
				for(int i = 0 ; i < webpages.length ; i++){
					if(i > 0){ result += "<br>"; }
								
					result += "<a href='"+webpages[i]+"' target='_blank'>" + webpages[i] + "</a>";
				}
							
				return result;
			}else{
				return "<a href='"+this.webpage+"' target='_blank'>" + this.webpage + "</a>";
			}
		}
		return webpageLinkFormat;
	}

	public void setWebpageLinkFormat(String webpageLinkFormat) {
		this.webpageLinkFormat = webpageLinkFormat;
	}

	public String getObligationType() {
		return obligationType;
	}

	public void setObligationType(String obligationType) {
		this.obligationType = obligationType;
	}
	
	public String getRestriction() {
		return restriction;
	}

	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
	
	public String getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(String restrictions) {
		this.restrictions = restrictions;
	}
	
	public String[] getArrRestriction() {
		return arrRestriction != null ? arrRestriction.clone() : null;
	}

	public void setArrRestriction(String[] arrRestriction) {
		this.arrRestriction = arrRestriction != null ?
			arrRestriction.clone() : null;
	}
	
	public String getRestrictionList() {
		return restrictionList;
	}

	public void setRestrictionList(String restrictionList) {
		this.restrictionList = restrictionList;
	}

	public String getRestrictionStr() {
		return restrictionStr;
	}

	public void setRestrictionStr(String restrictionStr) {
		this.restrictionStr = restrictionStr;
	}

	public String getLicenseTypeFull() {
		return licenseTypeFull;
	}

	public void setLicenseTypeFull(String licenseTypeFull) {
		this.licenseTypeFull = licenseTypeFull;
	}

	public String getLicenseNameAllSearchFlag() {
		return licenseNameAllSearchFlag;
	}

	public void setLicenseNameAllSearchFlag(String licenseNameAllSearchFlag) {
		this.licenseNameAllSearchFlag = licenseNameAllSearchFlag;
	}

	public String[] getWebpages() {
		return webpages != null ? webpages.clone() : null;
	}

	public void setWebpages(String[] webpages) {
		this.webpages = webpages != null ? webpages.clone() : null;
	}

	public String getsOrder() {
		return sOrder;
	}

	public void setsOrder(String sOrder) {
		this.sOrder = sOrder;
	}

	public String[] getNotIncludeArrRestriction() {
		return notIncludeArrRestriction;
	}

	public void setNotIncludeArrRestriction(String[] notIncludeArrRestriction) {
		this.notIncludeArrRestriction = notIncludeArrRestriction;
	}
}
