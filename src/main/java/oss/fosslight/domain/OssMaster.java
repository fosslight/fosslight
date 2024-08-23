/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import oss.fosslight.common.CommonFunction;
import oss.fosslight.util.StringUtil;

public class OssMaster extends ComBean implements Serializable{

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 702862084569729284L;
	
	/** The group key. */
	private String groupKey;
	
	/** The ref oss id. */
	private String refOssId;
	
	/** The oss id. */
	private String ossId;
	
	/** The oss name. */
	private String ossName;
	
	/**  검색용. */
	private String schOssName;
	
	/** The oss name With Version. */
	private String ossNameVerStr;
	
	/** The oss name temp. */
	private String ossNameTemp;
	
	/** The oss version. */
	private String ossVersion;
	
	/** The copyright. */
	private String copyright;
	
	/** The license div. */
	private String licenseDiv;
	
	/** The tot license txt. */
	private String totLicenseTxt;
	
	/** The download location. */
	private String downloadLocation;
	
	private String downloadLocationGroup;

	/** The download location. */
	private String[] downloadLocations;

	private List<String> detectedLicenses;

	/** The download location link format. */
	private String downloadLocationLinkFormat;
	
	/** The homepage. */
	private String homepage;
	
	/** The homepage link format. */
	private String homepageLinkFormat;
	
	/** The summary description. */
	private String summaryDescription;
	
	/** The oss type. */
	private String ossType;
	
	/** The cvss score. */
	private String cvssScore;
	
	/** The cvss score to. */
	private String cvssScoreTo;
	
	/** The cvss score icon. */
	private String cvssScoreIcon;
	
	/** The multi flag. */
	private String multiFlag;
	
	/** The cve id. */
	private String cveId;
	
	/** The cve id to. */
	private String cveIdTo;
	
	/** The new oss id. */
	private String newOssId;	//ossId 병합을 위한 ossId
	
	private String delOssId;
	
	/**  OSS 삭제시 Merge 결과 표시용. */
	private String mergeStr;
	
	/** The oss id list. */
	private List<String> ossIdList;
	
	/** The license id list. */
	private List<String> licenseIdList;
	
	/** The license comb list. */
	private List<String> licenseCombList;
	
	/** The license id. */
	//OSS_LICENSE
	private String licenseId;
	
	/** The oss license idx. */
	private String ossLicenseIdx;
	
	/** The oss license comb. */
	private String ossLicenseComb;
	
	/** The oss license text. */
	private String ossLicenseText;
	
	/** The oss copyright. */
	private String ossCopyright;
	
	/** The copyrights. */
	private String[] copyrights;
	
	/** The oss licenses. */
	private List<OssLicense> ossLicenses;
	
	/** The oss licenses json. */
	private String ossLicensesJson;
	
	/** The oss nicknames. */
	//OSS_NICKNAME
	private String[] ossNicknames;
	
	private String[] ossNicknameArr;
	
	/** The oss nickname. */
	private String ossNickname;
	
	
	/** The license name. */
	//LICENSE
	private String licenseName;

	/** The license type. */
	private String ossLicenseType;
	
	/** The license type. */
	private String licenseType;
	
	/** The obligation type. */
	private String obligationType;
	

	/** The license type. */
	private String orgLicenseType;
	
	/** The obligation type. */
	private String orgObligationType;
	
	/** The obligation. */
	private String obligation;
	
	/** The obligation checks. */
	private String obligationChecks;
	
	
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
	//components
	private String componentId;
	
	/** The prj id. */
	private String prjId;
	
	/** The vuln summary. */
	private String vulnSummary;
	
	/** The cve cnt. */
	private String cveCnt;
	
	/** The comment. */
	//comment 이력
	private String comment;
	
	/** The multi license flag. */
	private String multiLicenseFlag;
	
	/** The dual license flag. */
	private String dualLicenseFlag;
	
	/** The version diff flag. */
	private String versionDiffFlag;
	
	/** License Operation Expression. */
	private String licenseOprExp;
	
	/**  화면 검색인 경우 Y. */
	private String searchFlag;
	
	/** The oss names. */
	private String[] ossNames;
	
	/**  oss license Name 검색인 경우 최상위 버전 체크 여부 . */
	private String versionCheck;
	
	/**  vulnerability 조회 조건. */
	private String vulnType;
	
	/** The vuln recheck. */
	private String vulnRecheck;
	
	/** The vuln date. */
	private String vulnDate;
	
	/** The vuln yn. */
	private String vulnYn;
	
	private List<Project> refProjectList;
	
	/** The validationType. */
	private String validationType;
	
	/** The sOrder. */
	private String sOrder;
	
	private String publishedDate;

	/** The attribution. */
	private String attribution;					//attribution
	
	/** 일괄등록 처리시 등록 type */
	private String regType;
	
	/** The componentId List. */
	private List<String> componentIdList;
	
	private String ossNameAllSearchFlag;
	
	private String ossTypeSearch;

	private String licenseNameAllSearchFlag;
	private String homepageAllSearchFlag;
	
	private String startAnalysisFlag;
	
	private String analysisYn;
	private String completeYn;
	private String addNicknameYn = "N";
	
	private String referenceDiv;
	
	private String deactivateFlag = "N";
	
	private String[] ossIds;
	private String syncRefOssId;
	private String[] syncItem;
	
	private String ossMergeReferenceId;
	private String registMergeFlag;
	private String mergeOssId;
	private String mergeOssName;
	private String mergeOssVersion;
	
	private String renameFlag = "N";
	
	private String ossCopyFlag = "N";
	
	private String defaultSearchFlag;
	private String[] existOssNickNames;

	private List<String> declaredLicenses;
	private String linkFlag = "N";
	private int[] csvComponentIdList;
	private String[] dashOssNameList;
	private String[] conversionNameList;
	
	private String cvssScoreMax;
	private String cvssScoreMax1;
	private String cvssScoreMax2;
	private String cvssScoreMax3;
	
	private String vulnerabilityCheckFlag;
	private String sndMailCheckFlag;
	
	private Float securityStandardScore;
	private Float standardScore;
	
	// Added when improving OSORI DB function
	private String ossCommonId;
	
	private String ossVersionAlias;
	
	private String[] ossVersionAliasWithColon;
	
	private String[] ossVersionAliases;
	
	private String purl;
	
	private String includeCpe;
	
	private String[] includeCpes;
	
	private String excludeCpe;
	
	private String[] excludeCpes;
	
	private String[] existIncludeCpes;
	
	private String[] existExcludeCpes;
	
	private String purlJson;
	
	private int ossDlIdx;
	
	private int ossPurlIdx;
	
	private String restriction;
	
	private String[] arrRestriction;
	
	private String[] existArrRestriction;
 	
	private List<String> restrictionCdNoList;
	
	private String inCpeMatchFlag;
		
	public int[] getCsvComponentIdList() {
		return csvComponentIdList;
	}

	public void setCsvComponentIdList(int[] csvComponentIdList) {
		this.csvComponentIdList = csvComponentIdList;
	}
	/**
	 * Instantiates a new oss master.
	 */
	public OssMaster() {
		super();
	}
	
	/**
	 * Instantiates a new oss master.
	 *
	 * @param ossId the oss id
	 */
	public OssMaster(String ossId) {
		super();
		this.ossId = ossId;
	}
	
	
	public String getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(String publishedDate) {
		this.publishedDate = publishedDate;
	}

	/**
	 * Instantiates a new oss master.
	 *
	 * @param ossId the oss id
	 * @param licenseId the license id
	 * @param ossLicenseIdx the oss license idx
	 */
	
	public OssMaster(String ossId, String licenseId, String ossLicenseIdx) {
		super();
		this.ossId = ossId;
		this.licenseId = licenseId;
		this.ossLicenseIdx = ossLicenseIdx;
	}
	
	/**
	 * Instantiates a new oss master.
	 *
	 * @param ossLicenseIdx the oss license idx
	 * @param ossId the oss id
	 * @param licenseName the license name
	 * @param ossLicenseComb the oss license comb
	 * @param ossLicenseText the oss license text
	 * @param ossCopyright the oss copyright
	 * @param licenseDiv the license div
	 */
	public OssMaster(String ossLicenseIdx, String ossId, String licenseId, String licenseName, String ossLicenseComb, String ossCopyright, String licenseDiv) {
		super();
		this.ossLicenseIdx = ossLicenseIdx;
		this.ossId = ossId;
		this.licenseId = licenseId;
		this.licenseName = licenseName;
		this.ossLicenseComb = ossLicenseComb;
		this.ossCopyright = ossCopyright;
		this.licenseDiv = licenseDiv;
	}
	
	/**
	 * Gets the oss id.
	 *
	 * @return the oss id
	 */
	public String getOssId() {
		return ossId;
	}
	
	/**
	 * Sets the oss id.
	 *
	 * @param ossId the new oss id
	 */
	public void setOssId(String ossId) {
		this.ossId = ossId;
	}
	
	/**
	 * Gets the oss name.
	 *
	 * @return the oss name
	 */
	public String getOssName() {
		return ossName;
	}
	
	/**
	 * Sets the oss name.
	 *
	 * @param ossName the new oss name
	 */
	public void setOssName(String ossName) {
		this.ossName = ossName;
	}
	
	/**
	 * Gets the oss version.
	 *
	 * @return the oss version
	 */
	public String getOssVersion() {
		return ossVersion;
	}
	
	/**
	 * Sets the oss version.
	 *
	 * @param ossVersion the new oss version
	 */
	public void setOssVersion(String ossVersion) {
		this.ossVersion = ossVersion;
	}
	
	/**
	 * Gets the license div.
	 *
	 * @return the license div
	 */
	public String getLicenseDiv() {
		return licenseDiv;
	}
	
	/**
	 * Sets the license div.
	 *
	 * @param licenseDiv the new license div
	 */
	public void setLicenseDiv(String licenseDiv) {
		this.licenseDiv = licenseDiv;
	}
	
	/**
	 * Gets the tot license txt.
	 *
	 * @return the tot license txt
	 */
	public String getTotLicenseTxt() {
		return totLicenseTxt;
	}
	
	/**
	 * Sets the tot license txt.
	 *
	 * @param totLicenseTxt the new tot license txt
	 */
	public void setTotLicenseTxt(String totLicenseTxt) {
		this.totLicenseTxt = totLicenseTxt;
	}
	
	/**
	 * Gets the homepage.
	 *
	 * @return the homepage
	 */
	public String getHomepage() {
		return CommonFunction.convertUrlLinkFormat(homepage);
	}
	
	/**
	 * Sets the homepage.
	 *
	 * @param homepage the new homepage
	 */
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	
	/**
	 * Gets the summary description.
	 *
	 * @return the summary description
	 */
	public String getSummaryDescription() {
		return summaryDescription;
	}
	
	/**
	 * Sets the summary description.
	 *
	 * @param summaryDescription the new summary description
	 */
	public void setSummaryDescription(String summaryDescription) {
		this.summaryDescription = summaryDescription;
	}
	
	/**
	 * Gets the download location.
	 *
	 * @return the download location
	 */
	public String getDownloadLocation() {
		return CommonFunction.convertUrlLinkFormat(downloadLocation);
	}
	
	/**
	 * Sets the download location.
	 *
	 * @param downloadLocation the new download location
	 */
	public void setDownloadLocation(String downloadLocation) {
		this.downloadLocation = downloadLocation;
	}
	
	public String[] getDownloadLocations() {
		return downloadLocations != null ? downloadLocations.clone() : null;
	}

	public void setDownloadLocations(String[] downloadLocations) {
		this.downloadLocations = downloadLocations != null ?
			downloadLocations.clone() : null;
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
	 * Gets the oss license idx.
	 *
	 * @return the oss license idx
	 */
	public String getOssLicenseIdx() {
		return ossLicenseIdx;
	}
	
	/**
	 * Sets the oss license idx.
	 *
	 * @param ossLicenseIdx the new oss license idx
	 */
	public void setOssLicenseIdx(String ossLicenseIdx) {
		this.ossLicenseIdx = ossLicenseIdx;
	}
	
	/**
	 * Gets the oss license comb.
	 *
	 * @return the oss license comb
	 */
	public String getOssLicenseComb() {
		return ossLicenseComb;
	}
	
	/**
	 * Sets the oss license comb.
	 *
	 * @param ossLicenseComb the new oss license comb
	 */
	public void setOssLicenseComb(String ossLicenseComb) {
		this.ossLicenseComb = ossLicenseComb;
	}
	
	/**
	 * Gets the oss license text.
	 *
	 * @return the oss license text
	 */
	public String getOssLicenseText() {
		return ossLicenseText;
	}
	
	/**
	 * Sets the oss license text.
	 *
	 * @param ossLicenseText the new oss license text
	 */
	public void setOssLicenseText(String ossLicenseText) {
		this.ossLicenseText = ossLicenseText;
	}
	
	/**
	 * Gets the oss copyright.
	 *
	 * @return the oss copyright
	 */
	public String getOssCopyright() {
		return ossCopyright;
	}
	
	/**
	 * Sets the oss copyright.
	 *
	 * @param ossCopyright the new oss copyright
	 */
	public void setOssCopyright(String ossCopyright) {
		this.ossCopyright = ossCopyright;
	}
	
	/**
	 * Gets the oss nickname.
	 *
	 * @return the oss nickname
	 */
	public String getOssNickname() {
		return ossNickname;
	}
	
	/**
	 * Sets the oss nickname.
	 *
	 * @param ossNickname the new oss nickname
	 */
	public void setOssNickname(String ossNickname) {
		this.ossNickname = ossNickname;
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
	 * Gets the copyrights.
	 *
	 * @return the copyrights
	 */
	public String[] getCopyrights() {
		return copyrights != null ? copyrights.clone() : null;
	}
	
	/**
	 * Sets the copyrights.
	 *
	 * @param copyrights the new copyrights
	 */
	public void setCopyrights(String copyrights) {
		String[] copys = copyrights.split("\r\n");
		if (copys[0].equals("") && copys.length == 1){
			this.copyrights = null; 
		}else{
			this.copyrights = copys;
		}
	}
	
	/**
	 * Gets the oss licenses.
	 *
	 * @return the oss licenses
	 */
	public List<OssLicense> getOssLicenses() {
		return ossLicenses;
	}
	
	/**
	 * Sets the oss licenses.
	 *
	 * @param ossLicenses the new oss licenses
	 */
	public void setOssLicenses(List<OssLicense> ossLicenses) {
		this.ossLicenses = ossLicenses;
	}
	
	/**
	 * Gets the oss licenses json.
	 *
	 * @return the oss licenses json
	 */
	public String getOssLicensesJson() {
		return ossLicensesJson;
	}
	
	/**
	 * Sets the oss licenses json.
	 *
	 * @param ossLicensesJson the new oss licenses json
	 */
	public void setOssLicensesJson(String ossLicensesJson) {
		this.ossLicensesJson = ossLicensesJson;
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
	 * Gets the oss type.
	 *
	 * @return the oss type
	 */
	public String getOssType() {
		return ossType;
	}
	
	/**
	 * Sets the oss type.
	 *
	 * @param ossType the new oss type
	 */
	public void setOssType(String ossType) {
		if (ossType != null && ossType.length() == 3) {
			String _rtn = "";
			if ("1".equals(ossType.substring(0, 1))) {
				_rtn += "M";
			}
			if ("1".equals(ossType.substring(1,2))) {
				_rtn += "D";
			}
			if ("1".equals(ossType.substring(2, 3))) {
				_rtn += "V";
			}
			ossType = _rtn;
		}
		this.ossType = ossType;
	}
	
	/**
	 * Gets the obligation type.
	 *
	 * @return the obligation type
	 */
	public String getObligationType() {
		return obligationType;
	}
	
	/**
	 * Sets the obligation type.
	 *
	 * @param obligationType the new obligation type
	 */
	public void setObligationType(String obligationType) {
		this.obligationType = obligationType;
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
	 * Adds the oss license.
	 *
	 * @param bean the bean
	 */
	public void addOssLicense(OssLicense bean) {
		if (bean != null) {
			if (this.ossLicenses == null) {
				this.ossLicenses = new ArrayList<>();
			}
			this.ossLicenses.add(bean);
			
			Collections.sort(this.ossLicenses, new Comparator<OssLicense>() {
				@Override
				public int compare(OssLicense o1, OssLicense o2) {
					return o1.getOssLicenseIdx().compareTo(o2.getOssLicenseIdx());
				}
			});
		}
	}
	
	/**
	 * Gets the cvss score.
	 *
	 * @return the cvss score
	 */
	public String getCvssScore() {
		return cvssScore;
	}
	
	/**
	 * Sets the cvss score.
	 *
	 * @param cvssScore the new cvss score
	 */
	public void setCvssScore(String cvssScore) {
		this.cvssScore = cvssScore;
		setCvssScoreIcon();
	}
	
	/**
	 * Gets the cvss score icon.
	 *
	 * @return the cvss score icon
	 */
	public String getCvssScoreIcon() {
		return cvssScoreIcon;
	}
	
	/**
	 * Sets the cvss score icon.
	 */
	public void setCvssScoreIcon() {
		String convertCvssScore = cvssScore;
		
		if (!StringUtil.isEmpty(convertCvssScore)){
			if (convertCvssScore.indexOf("->") > -1) {
				convertCvssScore = convertCvssScore.split("->")[1].trim();
			}
			
			if (Double.parseDouble(convertCvssScore) <= 3.9){
				cvssScoreIcon = "L";
			}else if (Double.parseDouble(convertCvssScore) <= 6.9){
				cvssScoreIcon = "M";
			}else if (Double.parseDouble(convertCvssScore) <= 10.0){
				cvssScoreIcon = "H";
			}
		}
	}
	
	/**
	 * Gets the multi flag.
	 *
	 * @return the multi flag
	 */
	public String getMultiFlag() {
		return multiFlag;
	}
	
	/**
	 * Sets the multi flag.
	 *
	 * @param multiFlag the new multi flag
	 */
	public void setMultiFlag(String multiFlag) {
		this.multiFlag = multiFlag;
	}
	
	/**
	 * Gets the cve id.
	 *
	 * @return the cve id
	 */
	public String getCveId() {
		return cveId;
	}
	
	/**
	 * Sets the cve id.
	 *
	 * @param cveId the new cve id
	 */
	public void setCveId(String cveId) {
		this.cveId = cveId;
	}
	
	/**
	 * Sets the cve id text.
	 */
	public void setCveIdText() {
		if (!StringUtil.isEmpty(cveId)){
			if (cveId.indexOf("CVE-") == -1){
				cveId = "CVE-"+cveId;
			}
		}
	}
	
	/**
	 * Gets the oss names.
	 *
	 * @return the oss names
	 */
	public String[] getOssNames() {
		return ossNames != null ? ossNames.clone() : null;
	}
	
	/**
	 * Sets the oss names.
	 *
	 * @param ossNames the new oss names
	 */
	public void setOssNames(String[] ossNames) {
		this.ossNames = ossNames != null ? ossNames.clone() : null;
	}
	
	/**
	 * Gets the new oss id.
	 *
	 * @return the new oss id
	 */
	public String getNewOssId() {
		return newOssId;
	}
	
	/**
	 * Sets the new oss id.
	 *
	 * @param newOssId the new new oss id
	 */
	public void setNewOssId(String newOssId) {
		this.newOssId = newOssId;
	}
	
	/**
	 * Gets the license opr exp.
	 *
	 * @return the license opr exp
	 */
	public String getLicenseOprExp() {
		if (isEmpty(licenseOprExp) && this.ossLicenses != null && !this.ossLicenses.isEmpty()) {
			return CommonFunction.makeLicenseExpression(getOssLicenses());
		}
		return licenseOprExp;
	}
	
	/**
	 * Sets the license opr exp.
	 *
	 * @param licenseOprExp the new license opr exp
	 */
	public void setLicenseOprExp(String licenseOprExp) {
		this.licenseOprExp = licenseOprExp;
	}
	
	/**
	 * Gets the copyright.
	 *
	 * @return the copyright
	 */
	public String getCopyright() {
		return copyright;
	}
	
	/**
	 * Sets the copyright.
	 *
	 * @param copyright the new copyright
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	/**
	 * Gets the oss nicknames.
	 *
	 * @return the oss nicknames
	 */
	public String[] getOssNicknames() {
		return ossNicknames != null ? ossNicknames.clone() : null;
	}
	
	/**
	 * Sets the oss nicknames.
	 *
	 * @param ossNicknames the new oss nicknames
	 */
	public void setOssNicknames(String[] ossNicknames) {
		this.ossNicknames = ossNicknames != null ? ossNicknames.clone() : null;
	}
	
	/**
	 * Gets the oss id list.
	 *
	 * @return the oss id list
	 */
	public List<String> getOssIdList() {
		return ossIdList;
	}
	
	/**
	 * Sets the oss id list.
	 *
	 * @param ossIdList the new oss id list
	 */
	public void setOssIdList(List<String> ossIdList) {
		this.ossIdList = ossIdList;
	}
	
	/**
	 * Adds the oss id list.
	 *
	 * @param s the s
	 */
	public void addOssIdList(String s) {
		if (this.ossIdList == null) {
			this.ossIdList = new ArrayList<>();
		}
		if (!isEmpty(s) && !ossIdList.contains(s)) {
			this.ossIdList.add(s);
		}
	}
	
	/**
	 * Adds the license id list.
	 *
	 * @param s the s
	 */
	public void addLicenseIdList(String s) {
		if (this.licenseIdList == null) {
			this.licenseIdList = new ArrayList<>();
		}
		this.licenseIdList.add(s);
	}
	
	/**
	 * Gets the license id list.
	 *
	 * @return the license id list
	 */
	public List<String> getLicenseIdList() {
		return licenseIdList;
	}
	
	/**
	 * Sets the license id list.
	 *
	 * @param licenseIdList the new license id list
	 */
	public void setLicenseIdList(List<String> licenseIdList) {
		this.licenseIdList = licenseIdList;
	}
	
	/**
	 * Gets the ref oss id.
	 *
	 * @return the ref oss id
	 */
	public String getRefOssId() {
		return refOssId;
	}
	
	/**
	 * Sets the ref oss id.
	 *
	 * @param refOssId the new ref oss id
	 */
	public void setRefOssId(String refOssId) {
		this.refOssId = refOssId;
	}
	
	/**
	 * Adds the license comb list.
	 *
	 * @param s the s
	 */
	public void addLicenseCombList(String s) {
		if (this.licenseCombList == null) {
			this.licenseCombList = new ArrayList<>();
		}
		this.licenseCombList.add(s);
	}
	
	/**
	 * Gets the license comb list.
	 *
	 * @return the license comb list
	 */
	public List<String> getLicenseCombList() {
		return licenseCombList;
	}
	
	/**
	 * Sets the license comb list.
	 *
	 * @param licenseCombList the new license comb list
	 */
	public void setLicenseCombList(List<String> licenseCombList) {
		this.licenseCombList = licenseCombList;
	}
	
	/**
	 * Append oss type.
	 *
	 * @param s the s
	 */
	public void appendOssType(String s) {
		if (this.ossType == null){
			this.ossType="";
		}
		this.ossType += s;
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
	
	/**
	 * Gets the vuln summary.
	 *
	 * @return the vuln summary
	 */
	public String getVulnSummary() {
		return vulnSummary;
	}
	
	/**
	 * Sets the vuln summary.
	 *
	 * @param vulnSummary the new vuln summary
	 */
	public void setVulnSummary(String vulnSummary) {
		this.vulnSummary = vulnSummary;
	}
	
	/**
	 * Gets the cve cnt.
	 *
	 * @return the cve cnt
	 */
	public String getCveCnt() {
		return cveCnt;
	}
	
	/**
	 * Sets the cve cnt.
	 *
	 * @param cveCnt the new cve cnt
	 */
	public void setCveCnt(String cveCnt) {
		this.cveCnt = cveCnt;
	}
	
	/**
	 * Sets the cvss score icon.
	 *
	 * @param cvssScoreIcon the new cvss score icon
	 */
	public void setCvssScoreIcon(String cvssScoreIcon) {
		this.cvssScoreIcon = cvssScoreIcon;
	}
	
	/**
	 * Gets the group key.
	 *
	 * @return the group key
	 */
	public String getGroupKey() {
		return groupKey;
	}
	
	/**
	 * Sets the group key.
	 *
	 * @param groupKey the new group key
	 */
	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}
	
	/**
	 * Gets the oss name temp.
	 *
	 * @return the oss name temp
	 */
	public String getOssNameTemp() {
		return ossNameTemp;
	}
	
	/**
	 * Sets the oss name temp.
	 *
	 * @param ossNameTemp the new oss name temp
	 */
	public void setOssNameTemp(String ossNameTemp) {
		this.ossNameTemp = ossNameTemp;
	}
	
	/**
	 * Gets the multi license flag.
	 *
	 * @return the multi license flag
	 */
	public String getMultiLicenseFlag() {
		return multiLicenseFlag;
	}
	
	/**
	 * Sets the multi license flag.
	 *
	 * @param multiLicenseFlag the new multi license flag
	 */
	public void setMultiLicenseFlag(String multiLicenseFlag) {
		this.multiLicenseFlag = multiLicenseFlag;
	}
	
	/**
	 * Gets the dual license flag.
	 *
	 * @return the dual license flag
	 */
	public String getDualLicenseFlag() {
		return dualLicenseFlag;
	}
	
	/**
	 * Sets the dual license flag.
	 *
	 * @param dualLicenseFlag the new dual license flag
	 */
	public void setDualLicenseFlag(String dualLicenseFlag) {
		this.dualLicenseFlag = dualLicenseFlag;
	}
	
	/**
	 * Gets the version diff flag.
	 *
	 * @return the version diff flag
	 */
	public String getVersionDiffFlag() {
		return versionDiffFlag;
	}
	
	/**
	 * Sets the version diff flag.
	 *
	 * @param versionDiffFlag the new version diff flag
	 */
	public void setVersionDiffFlag(String versionDiffFlag) {
		this.versionDiffFlag = versionDiffFlag;
	}

	/**
	 * Gets the search flag.
	 *
	 * @return the search flag
	 */
	public String getSearchFlag() {
		return searchFlag;
	}

	/**
	 * Sets the search flag.
	 *
	 * @param searchFlag the new search flag
	 */
	public void setSearchFlag(String searchFlag) {
		this.searchFlag = searchFlag;
	}
	
	/**
	 * Gets the version check.
	 *
	 * @return the version check
	 */
	public String getVersionCheck() {
		return versionCheck;
	}
	
	/**
	 * Sets the version check.
	 *
	 * @param versionCheck the new version check
	 */
	public void setVersionCheck(String versionCheck) {
		this.versionCheck = versionCheck;
	}

	/**
	 * Gets the oss name ver str.
	 *
	 * @return the oss name ver str
	 */
	public String getOssNameVerStr() {
		return ossNameVerStr;
	}

	/**
	 * Sets the oss name ver str.
	 *
	 * @param ossNameVerStr the new oss name ver str
	 */
	public void setOssNameVerStr(String ossNameVerStr) {
		this.ossNameVerStr = ossNameVerStr;
	}

	/**
	 * Gets the vuln type.
	 *
	 * @return the vuln type
	 */
	public String getVulnType() {
		return vulnType;
	}

	/**
	 * Sets the vuln type.
	 *
	 * @param vulnType the new vuln type
	 */
	public void setVulnType(String vulnType) {
		this.vulnType = vulnType;
	}

	/**
	 * Gets the oss license type.
	 *
	 * @return the ossLicenseType
	 */
	public String getOssLicenseType() {
		return ossLicenseType;
	}

	/**
	 * Sets the oss license type.
	 *
	 * @param ossLicenseType the ossLicenseType to set
	 */
	public void setOssLicenseType(String ossLicenseType) {
		this.ossLicenseType = ossLicenseType;
	}

	/**
	 * Gets the org license type.
	 *
	 * @return the orgLicenseType
	 */
	public String getOrgLicenseType() {
		return orgLicenseType;
	}

	/**
	 * Sets the org license type.
	 *
	 * @param orgLicenseType the orgLicenseType to set
	 */
	public void setOrgLicenseType(String orgLicenseType) {
		this.orgLicenseType = orgLicenseType;
	}

	/**
	 * Gets the org obligation type.
	 *
	 * @return the orgObligationType
	 */
	public String getOrgObligationType() {
		return orgObligationType;
	}

	/**
	 * Sets the org obligation type.
	 *
	 * @param orgObligationType the orgObligationType to set
	 */
	public void setOrgObligationType(String orgObligationType) {
		this.orgObligationType = orgObligationType;
	}

	/**
	 * Gets the vuln recheck.
	 *
	 * @return the vuln recheck
	 */
	public String getVulnRecheck() {
		return vulnRecheck;
	}

	/**
	 * Sets the vuln recheck.
	 *
	 * @param vulnRecheck the new vuln recheck
	 */
	public void setVulnRecheck(String vulnRecheck) {
		this.vulnRecheck = vulnRecheck;
	}

	/**
	 * Gets the download location link format.
	 *
	 * @return the download location link format
	 */
	public String getDownloadLocationLinkFormat() {
		if (StringUtil.isEmpty(this.downloadLocationLinkFormat) && !StringUtil.isEmpty(this.downloadLocation)) {
			if (this.downloadLocation.contains(",")){
				String[] downloadLocations = this.downloadLocation.split(",");
				String result = "";
				
				for (int i = 0 ; i < downloadLocations.length ; i++){
					if (i > 0){ result += "<br>"; }
					
					result += "<a href='"+downloadLocations[i]+"' target='_blank'>" + downloadLocations[i] + "</a>";
				}
				
				return result;
			}else{
				return "<a href='"+this.downloadLocation+"' target='_blank'>" + this.downloadLocation + "</a>";
			}
		}
		return downloadLocationLinkFormat;
	}

	/**
	 * Sets the download location link format.
	 *
	 * @param downloadLocationLinkFormat the new download location link format
	 */
	public void setDownloadLocationLinkFormat(String downloadLocationLinkFormat) {
		this.downloadLocationLinkFormat = downloadLocationLinkFormat;
	}

	/**
	 * Gets the homepage link format.
	 *
	 * @return the homepage link format
	 */
	public String getHomepageLinkFormat() {
		if (StringUtil.isEmpty(this.homepageLinkFormat) && !StringUtil.isEmpty(this.homepage)) {
			return "<a href='"+this.homepage+"' target='_blank'>" + this.homepage + "</a>";
		}
		return homepageLinkFormat;
	}

	/**
	 * Sets the homepage link format.
	 *
	 * @param homepageLinkFormat the new homepage link format
	 */
	public void setHomepageLinkFormat(String homepageLinkFormat) {
		this.homepageLinkFormat = homepageLinkFormat;
	}

	/**
	 * Gets the vuln date.
	 *
	 * @return the vuln date
	 */
	public String getVulnDate() {
		return vulnDate;
	}

	/**
	 * Sets the vuln date.
	 *
	 * @param vulnDate the new vuln date
	 */
	public void setVulnDate(String vulnDate) {
		this.vulnDate = vulnDate;
	}

	/**
	 * Gets the vuln yn.
	 *
	 * @return the vuln yn
	 */
	public String getVulnYn() {
		return vulnYn;
	}

	/**
	 * Sets the vuln yn.
	 *
	 * @param vulnYn the new vuln yn
	 */
	public void setVulnYn(String vulnYn) {
		this.vulnYn = vulnYn;
	}

	/**
	 * Gets the cvss score to.
	 *
	 * @return the cvss score to
	 */
	public String getCvssScoreTo() {
		return cvssScoreTo;
	}

	/**
	 * Sets the cvss score to.
	 *
	 * @param cvssScoreTo the new cvss score to
	 */
	public void setCvssScoreTo(String cvssScoreTo) {
		this.cvssScoreTo = cvssScoreTo;
	}

	/**
	 * Gets the cve id to.
	 *
	 * @return the cve id to
	 */
	public String getCveIdTo() {
		return cveIdTo;
	}

	/**
	 * Sets the cve id to.
	 *
	 * @param cveIdTo the new cve id to
	 */
	public void setCveIdTo(String cveIdTo) {
		this.cveIdTo = cveIdTo;
	}

	/**
	 * Gets the sch oss name.
	 *
	 * @return the sch oss name
	 */
	public String getSchOssName() {
		return schOssName;
	}

	/**
	 * Sets the sch oss name.
	 *
	 * @param schOssName the new sch oss name
	 */
	public void setSchOssName(String schOssName) {
		this.schOssName = schOssName;
	}

	/**
	 * Gets the merge str.
	 *
	 * @return the merge str
	 */
	public String getMergeStr() {
		return mergeStr;
	}

	/**
	 * Sets the merge str.
	 *
	 * @param mergeStr the new merge str
	 */
	public void setMergeStr(String mergeStr) {
		this.mergeStr = mergeStr;
	}

	public String getDelOssId() {
		return delOssId;
	}

	public void setDelOssId(String delOssId) {
		this.delOssId = delOssId;
	}

	public List<Project> getRefProjectList() {
		return refProjectList;
	}

	public void setRefProjectList(List<Project> refProjectList) {
		this.refProjectList = refProjectList;
	}
	
	public String getValidationType() {
		return validationType;
	}

	public void setValidationType(String validationType) {
		this.validationType = validationType;
	}
	
	public String getSOrder() {
		return sOrder;
	}

	public void setSOrder(String sOrder) {
		this.sOrder = sOrder;
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getRegType() {
		return regType;
	}

	public void setRegType(String regType) {
		this.regType = regType;
	}
	
	public String getDownloadLocationGroup() {
		return downloadLocationGroup;
	}

	public void setDownloadLocationGroup(String downloadLocationGroup) {
		// OSS를 삭제하면서 다른 OSS로 rename시, "This oss has multiple version"이라 뜨며 에러 발생. / NullPointException
		if (!isEmpty(downloadLocationGroup)) {
			this.downloadLocations = downloadLocationGroup.split(",");
		}
		
		this.downloadLocationGroup = downloadLocationGroup;
	}

	public String getOssNameAllSearchFlag() {
		return ossNameAllSearchFlag;
	}

	public void setOssNameAllSearchFlag(String ossNameAllSearchFlag) {
		this.ossNameAllSearchFlag = ossNameAllSearchFlag;
	}

	public String getLicenseNameAllSearchFlag() {
		return licenseNameAllSearchFlag;
	}

	public void setLicenseNameAllSearchFlag(String licenseNameAllSearchFlag) {
		this.licenseNameAllSearchFlag = licenseNameAllSearchFlag;
	}

	public String getHomepageAllSearchFlag() {
		return homepageAllSearchFlag;
	}

	public void setHomepageAllSearchFlag(String homepageAllSearchFlag) {
		this.homepageAllSearchFlag = homepageAllSearchFlag;
	}

	public String getOssTypeSearch() {
		return ossTypeSearch;
	}

	public void setOssTypeSearch(String ossTypeSearch) {
		this.ossTypeSearch = ossTypeSearch;
	}

	public List<String> getComponentIdList() {
		return componentIdList;
	}

	public void setComponentIdList(List<String> componentIdList) {
		this.componentIdList = componentIdList;
	}

	public String getStartAnalysisFlag() {
		return startAnalysisFlag;
	}

	public void setStartAnalysisFlag(String startAnalysisFlag) {
		this.startAnalysisFlag = startAnalysisFlag;
	}

	public String getAnalysisYn() {
		return analysisYn;
	}

	public void setAnalysisYn(String analysisYn) {
		this.analysisYn = analysisYn;
	}

	public String getCompleteYn() {
		return completeYn;
	}

	public void setCompleteYn(String completeYn) {
		this.completeYn = completeYn;
	}

	public String getAddNicknameYn() {
		return addNicknameYn;
	}

	public void setAddNicknameYn(String addNicknameYn) {
		this.addNicknameYn = addNicknameYn;
	}	
	
	public void clearDetectLicense() {
		this.detectedLicenses = new ArrayList<>();
	}
	
	public void setDetectedLicense(String detectedLicense) {
		List<String> list = Arrays.asList(detectedLicense.split(","));
		
		this.detectedLicenses = null; // clear
		
		for (String s : list) {
			if (!isEmpty(s)) {
				this.addDetectedLicense(s.trim());
			}
		}
	}
	
	public List<String> getDetectedLicenses() {
		return detectedLicenses == null 
						? new ArrayList<>() 
						: detectedLicenses;
	}
	
	public String getDetectedLicense() {
		return detectedLicenses == null ? "" : String.join(",", detectedLicenses);
	}

	public void setDetectedLicenses(List<String> detectedLicenses) {
		this.detectedLicenses = detectedLicenses;
	}
	
	public void addDetectedLicense(String s) {
		if (this.detectedLicenses == null) {
			this.detectedLicenses = new ArrayList<>();
		}
		
		this.detectedLicenses.add(s);
	}
	
	public String getReferenceDiv() {
		return referenceDiv;
	}

	public void setReferenceDiv(String referenceDiv) {
		this.referenceDiv = referenceDiv;
	}
	
	public String getDeactivateFlag() {
		return deactivateFlag;
	}

	public void setDeactivateFlag(String deactivateFlag) {
		this.deactivateFlag = deactivateFlag;
	}

	public String[] getOssIds() {
		return ossIds != null ? ossIds.clone() : null;
	}

	public void setOssIds(String[] ossIds) {
		this.ossIds = ossIds != null ? ossIds.clone() : null;
	}

	public String getSyncRefOssId() {
		return syncRefOssId;
	}

	public void setSyncRefOssId(String syncRefOssId) {
		this.syncRefOssId = syncRefOssId;
	}

	public String[] getSyncItem() {
		return syncItem != null ? syncItem.clone() : null;
	}

	public void setSyncItem(String[] syncItem) {
		this.syncItem = syncItem != null ? syncItem.clone() : null;
	}

	public String getOssMergeReferenceId() {
		return ossMergeReferenceId;
	}

	public void setOssMergeReferenceId(String ossMergeReferenceId) {
		this.ossMergeReferenceId = ossMergeReferenceId;
	}

	public String getRegistMergeFlag() {
		return registMergeFlag;
	}

	public void setRegistMergeFlag(String registMergeFlag) {
		this.registMergeFlag = registMergeFlag;
	}

	public String getMergeOssName() {
		return mergeOssName;
	}

	public void setMergeOssName(String mergeOssName) {
		this.mergeOssName = mergeOssName;
	}

	public String getMergeOssVersion() {
		return mergeOssVersion;
	}

	public void setMergeOssVersion(String mergeOssVersion) {
		this.mergeOssVersion = mergeOssVersion;
	}

	public String getMergeOssId() {
		return mergeOssId;
	}

	public void setMergeOssId(String mergeOssId) {
		this.mergeOssId = mergeOssId;
	}

	public String getOssCopyFlag() {
		return ossCopyFlag;
	}

	public void setOssCopyFlag(String ossCopyFlag) {
		this.ossCopyFlag = ossCopyFlag;
	}

	public String getDefaultSearchFlag() {
		return defaultSearchFlag;
	}

	public void setDefaultSearchFlag(String defaultSearchFlag) {
		this.defaultSearchFlag = defaultSearchFlag;
	}

	public List<String> getDeclaredLicenses() {
		return declaredLicenses == null
				? new ArrayList<>()
				: declaredLicenses;
	}

	public String getDeclaredLicense() {
		return declaredLicenses == null ? "" : String.join(",", declaredLicenses);
	}

	public void addDeclaredLicense(String s) {
		if (this.declaredLicenses == null) {
			this.declaredLicenses = new ArrayList<>();
		}
		this.declaredLicenses.add(s);
	}

	public void setDeclaredLicense(String declaredLicense) {
		String[] list = declaredLicense.split(",");

		this.declaredLicenses = null; // clear
		for (String s : list) {
			if (!isEmpty(s)) {
				this.addDeclaredLicense(s.trim());
			}
		}
	}

	public String[] getExistOssNickNames() {
		return existOssNickNames;
	}

	public void setExistOssNickNames(String[] existOssNickNames) {
		this.existOssNickNames = existOssNickNames;
	}

	public String getRenameFlag() {
		return renameFlag;
	}

	public void setRenameFlag(String renameFlag) {
		this.renameFlag = renameFlag;
	}

	public String getLinkFlag() {
		return linkFlag;
	}

	public void setLinkFlag(String linkFlag) {
		this.linkFlag = linkFlag;
	}
	
	public String[] getDashOssNameList() {
		return dashOssNameList;
	}

	public void setDashOssNameList(String[] dashOssNameList) {
		this.dashOssNameList = dashOssNameList;
	}

	public String[] getOssNicknameArr() {
		return ossNicknameArr;
	}

	public void setOssNicknameArr(String[] ossNicknameArr) {
		this.ossNicknameArr = ossNicknameArr;
	}

	public String getCvssScoreMax() {
		return cvssScoreMax;
	}

	public void setCvssScoreMax(String cvssScoreMax) {
		this.cvssScoreMax = cvssScoreMax;
	}

	public String getCvssScoreMax1() {
		return cvssScoreMax1;
	}

	public void setCvssScoreMax1(String cvssScoreMax1) {
		this.cvssScoreMax1 = cvssScoreMax1;
	}

	public String getCvssScoreMax2() {
		return cvssScoreMax2;
	}

	public void setCvssScoreMax2(String cvssScoreMax2) {
		this.cvssScoreMax2 = cvssScoreMax2;
	}

	public String getCvssScoreMax3() {
		return cvssScoreMax3;
	}

	public void setCvssScoreMax3(String cvssScoreMax3) {
		this.cvssScoreMax3 = cvssScoreMax3;
	}

	public String[] getConversionNameList() {
		return conversionNameList;
	}

	public void setConversionNameList(String[] conversionNameList) {
		this.conversionNameList = conversionNameList;
	}

	public String getVulnerabilityCheckFlag() {
		return vulnerabilityCheckFlag;
	}

	public void setVulnerabilityCheckFlag(String vulnerabilityCheckFlag) {
		this.vulnerabilityCheckFlag = vulnerabilityCheckFlag;
	}

	public String getSndMailCheckFlag() {
		return sndMailCheckFlag;
	}

	public void setSndMailCheckFlag(String sndMailCheckFlag) {
		this.sndMailCheckFlag = sndMailCheckFlag;
	}

	public Float getSecurityStandardScore() {
		return securityStandardScore;
	}

	public void setSecurityStandardScore(Float securityStandardScore) {
		this.securityStandardScore = securityStandardScore;
	}

	public Float getStandardScore() {
		return standardScore;
	}

	public void setStandardScore(Float standardScore) {
		this.standardScore = standardScore;
	}

	public String getPurlJson() {
		return purlJson;
	}

	public void setPurlJson(String purlJson) {
		this.purlJson = purlJson;
	}

	public String getOssCommonId() {
		return ossCommonId;
	}

	public void setOssCommonId(String ossCommonId) {
		this.ossCommonId = ossCommonId;
	}

	public int getOssDlIdx() {
		return ossDlIdx;
	}

	public void setOssDlIdx(int ossDlIdx) {
		this.ossDlIdx = ossDlIdx;
	}

	public int getOssPurlIdx() {
		return ossPurlIdx;
	}

	public void setOssPurlIdx(int ossPurlIdx) {
		this.ossPurlIdx = ossPurlIdx;
	}

	public String getPurl() {
		return purl;
	}

	public void setPurl(String purl) {
		this.purl = purl;
	}

	public String getIncludeCpe() {
		return includeCpe;
	}

	public void setIncludeCpe(String includeCpe) {
		this.includeCpe = includeCpe;
	}

	public String[] getIncludeCpes() {
		return includeCpes;
	}

	public void setIncludeCpes(String[] includeCpes) {
		this.includeCpes = includeCpes;
	}

	public String getExcludeCpe() {
		return excludeCpe;
	}

	public void setExcludeCpe(String excludeCpe) {
		this.excludeCpe = excludeCpe;
	}

	public String[] getExcludeCpes() {
		return excludeCpes;
	}

	public void setExcludeCpes(String[] excludeCpes) {
		this.excludeCpes = excludeCpes;
	}

	public String getOssVersionAlias() {
		return ossVersionAlias;
	}

	public void setOssVersionAlias(String ossVersionAlias) {
		this.ossVersionAlias = ossVersionAlias;
	}

	public String[] getOssVersionAliases() {
		return ossVersionAliases;
	}

	public void setOssVersionAliases(String[] ossVersionAliases) {
		this.ossVersionAliases = ossVersionAliases;
	}

	public String getRestriction() {
		return restriction;
	}

	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}

	public List<String> getRestrictionCdNoList() {
		return restrictionCdNoList;
	}

	public void setRestrictionCdNoList(List<String> restrictionCdNoList) {
		this.restrictionCdNoList = restrictionCdNoList;
	}

	public String getInCpeMatchFlag() {
		return inCpeMatchFlag;
	}

	public void setInCpeMatchFlag(String inCpeMatchFlag) {
		this.inCpeMatchFlag = inCpeMatchFlag;
	}

	public String[] getExistIncludeCpes() {
		return existIncludeCpes;
	}

	public void setExistIncludeCpes(String[] existIncludeCpes) {
		this.existIncludeCpes = existIncludeCpes;
	}

	public String[] getExistExcludeCpes() {
		return existExcludeCpes;
	}

	public void setExistExcludeCpes(String[] existExcludeCpes) {
		this.existExcludeCpes = existExcludeCpes;
	}

	public String[] getArrRestriction() {
		return arrRestriction;
	}

	public void setArrRestriction(String[] arrRestriction) {
		this.arrRestriction = arrRestriction;
	}

	public String[] getExistArrRestriction() {
		return existArrRestriction;
	}

	public void setExistArrRestriction(String[] existArrRestriction) {
		this.existArrRestriction = existArrRestriction;
	}

	public String[] getOssVersionAliasWithColon() {
		return ossVersionAliasWithColon;
	}

	public void setOssVersionAliasWithColon(String[] ossVersionAliasWithColon) {
		this.ossVersionAliasWithColon = ossVersionAliasWithColon;
	}
}
