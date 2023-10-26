/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.util.StringUtil;

public class Project extends ComBean implements Serializable {
	
	@Override
	public String toString() {
		return "Project [prjId=" + prjId + ", prjName=" + prjName + ", prjVersion=" + prjVersion + ", distributionType="
				+ distributionType + ", ossNoticeDueDate=" + ossNoticeDueDate + ", comment=" + comment + ", commentIdx="
				+ commentIdx + ", osType=" + osType + ", osTypeEtc=" + osTypeEtc + ", identificationStatus="
				+ identificationStatus + ", verificationStatus=" + verificationStatus + ", destributionName="
				+ destributionName + ", destributionSoftwareType=" + destributionSoftwareType
				+ ", identificationSubStatusPartner=" + identificationSubStatusPartner + ", identificationSubStatusSrc="
				+ identificationSubStatusSrc + ", identificationSubStatusBat=" + identificationSubStatusBat
				+ ", completeYn=" + completeYn + ", reviewer=" + reviewer + ", useYn=" + useYn + ", prjIds="
				+ Arrays.toString(prjIds) + ", category=" + category + ", subcategory=" + subcategory + ", modelName="
				+ modelName + ", releaseDate=" + releaseDate + ", prjDivision=" + prjDivision + ", prjDivisionName="
				+ prjDivisionName + ", prjUserId=" + prjUserId + ", prjUserName=" + prjUserName + ", watchers="
				+ Arrays.toString(watchers) + ", watcherList=" + watcherList + ", modelList=" + modelList + ", copy="
				+ copy + ", oldId=" + oldId + ", lastModifiedTime=" + lastModifiedTime + ", distributeTarget="
				+ distributeTarget + ", destributionStatus=" + destributionStatus + ", distributeMasterCategory="
				+ distributeMasterCategory + ", distributeName=" + distributeName + ", distributeSoftwareType="
				+ distributeSoftwareType + ", distributeDeployYn=" + distributeDeployYn + ", distributeDeployTime="
				+ distributeDeployTime + ", licenseFileName=" + licenseFileName + ", openSourceFileName="
				+ openSourceFileName + ", srcCsvFileId=" + srcCsvFileId + ", srcAndroidCsvFileId=" + srcAndroidCsvFileId
				+ ", srcAndroidNoticeFileId=" + srcAndroidNoticeFileId + ", noticeFileId=" + noticeFileId
				+ ", reviewReportFileId=" + reviewReportFileId + ", srcAndroidResultFileId=" + srcAndroidResultFileId
				+ ", packageFileId=" + packageFileId + ", noticeFile=" + noticeFile + ", reviewReportFile="
				+ reviewReportFile + ", packageFile=" + packageFile + ", csvFile=" + csvFile + ", androidCsvFile="
				+ androidCsvFile + ", androidNoticeFile=" + androidNoticeFile + ", ossId=" + ossId + ", ossName="
				+ ossName + ", licenseName=" + licenseName + ", status=" + status + ", refPartnerId=" + refPartnerId
				+ ", readmeContent=" + readmeContent + ", readmeYn=" + readmeYn + ", verifyFileContent="
				+ verifyFileContent + ", exceptFileContent=" + exceptFileContent + ", schStartDate=" + schStartDate
				+ ", schEndDate=" + schEndDate + ", prjModelJson=" + prjModelJson + ", division=" + division
				+ ", vulnYn=" + vulnYn + ", ossReportFlag=" + ossReportFlag +"]";
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6696666941312232808L;

	/** The prj id. */
	private String prjId;
	
	/** The prj id list. */
	private List<String> prjIdList;
	
	/** The prj name. */
	private String prjName;
	
	/** The prj version. */
	private String prjVersion;
	
	/** The distribution type. */
	private String distributionType;
	
	/**  distribution type 코드의 상세 코드 설명 (유형에 따른 버튼 표시 제어를 위해 필요). */
	private String distributionTypeOfCodeDtlExp;
	
	/** The oss notice due date. */
	private String ossNoticeDueDate;
	
	/** The comment. */
	private String comment;
	
	/** The comment idx. */
	private String commentIdx;
	
	/** The os type. */
	private String osType;
	
	/** The os type etc. */
	private String osTypeEtc;

	/** The notice type. */
	private String noticeType;
	private String noticeTypeEtc;

	/** The use custom notice yn. */
	private String useCustomNoticeYn;
	
	/**  다운로드 허용 플래그. */
	private int allowDownloadBitFlag = 1;
	
	/** The allow download notice HTML yn. */
	private String allowDownloadNoticeHTMLYn;
	
	/** The allow download notice text yn. */
	private String allowDownloadNoticeTextYn;
	
	/** The allow download simple HTML yn. */
	private String allowDownloadSimpleHTMLYn;
	
	/** The allow download simple text yn. */
	private String allowDownloadSimpleTextYn;
	
	/** The allow download SPDX sheet yn. */
	private String allowDownloadSPDXSheetYn;
	
	/** The allow download SPDX rdf yn. */
	private String allowDownloadSPDXRdfYn;
	
	/** The allow download SPDX tag yn. */
	private String allowDownloadSPDXTagYn;

	/** The allow download SPDX json yn. */
	private String allowDownloadSPDXJsonYn;

	/** The allow download SPDX yaml yn. */
	private String allowDownloadSPDXYamlYn;

	/** The allow download CycloneDX json yn. */
	private String allowDownloadCDXJsonYn;
	
	/** The allow download CycloneDX xml yn. */
	private String allowDownloadCDXXmlYn;
	
	/** The identification status. */
	private String identificationStatus;
	
	/** The verification status. */
	private String verificationStatus;
	
	/** The destribution name. */
	private String destributionName;
	
	/** The destribution software type. */
	private String destributionSoftwareType;
	
	/** The identification sub status partner. */
	private String identificationSubStatusPartner;
	
	/** The identification sub status dep. */
	private String identificationSubStatusDep;
	
	/** The identification sub status src. */
	private String identificationSubStatusSrc;
	
	/** The identification sub status bat. */
	private String identificationSubStatusBat;
	
	/** The identification sub status android. */
	private String identificationSubStatusAndroid;
	
	/** The identification sub status bin. */
	private String identificationSubStatusBin;
	
	/** The identification sub status bom. */
	private String identificationSubStatusBom;
	
	/** The complete yn. */
	private String completeYn;
	
	/** The drop yn. */
	private String dropYn;
	
	/** The verify yn */
	private String verifyYn;

	/** The reviewer. */
	private String reviewer;
	
	/** The use yn. */
	private String useYn;
	
	/** The prj ids. */
	private String[] prjIds;
	
	/** The code. */
	private String code;
	
	/** The category. */
	// PROJECT_MODEL
	private String category;
	
	/** The subcategory. */
	private String subcategory;
	
	/** The model name. */
	private String modelName;
	
	/** The release date. */
	private String releaseDate;
	
	/** The model list info. */
	private List<String> modelListInfo;
	
	/** The category nm. */
	private String categoryNm;
	
	/** The act type. */
	private String actType;
	
	/** The act cont. */
	private String actCont;

	/** The prj division. */
	// PROJECT_WATCHER
	private String prjDivision;
	
	/** The prj division name. */
	private String prjDivisionName;
	
	/** The prj user id. */
	private String prjUserId;
	
	/** The prj user name. */
	private String prjUserName;
	
	/** The prj email. */
	private String prjEmail;
	
	/** The watchers. */
	private String[] watchers;
	
	/** The watcher list. */
	private List<Project> watcherList;
	
	/** The model list. */
	private List<Project> modelList;
	
	/** The model delete list. */
	private List<Project> modelDeleteList;
	
	/** The division list. */
	private ArrayList<Map<String, String>> divisionList;
	
	/** The email list. */
	private ArrayList<Map<String, String>> emailList;
	
	/** The watcher list info. */
	private List<String> watcherListInfo;
	
	/** The osdd sync yn. */
	private String osddSyncYn;
	
	/** The osdd sync time. */
	private String osddSyncTime;

	/** The copy. */
	// COPY
	private String copy;
	
	/** The old id. */
	private String oldId;
	
	/** The copy prj id. */
	private String copyPrjId;
	
	/** The copy flag. */
	private String copyFlag = "N";

	/** The last modified time. */
	// DISTRIBUTION
	private String lastModifiedTime;			//최종수정시간
	
	/** The distribute target. */
	private String distributeTarget;			//배포사이트
	
	/** The destribution status. */
	private String destributionStatus; 			// 디스트리뷰트 상태
	
	private String beforeDistributionStatus;

	/** The distribute master category. */
	private String distributeMasterCategory;
	
	/** The distribute name. */
	private String distributeName; 				// 디스크립션 이름
	
	/** The before distribute name. */
	private String beforeDistributeName;		// 배포되었던 디스크립선 이름
	
	/** The distribute software type. */
	private String distributeSoftwareType = CoConstDef.CD_DTL_NOTICE_DEFAULT_SOFTWARE_TYPE_MODEL; 		// 소프트웨어/모델 타입
	
	/** The before distribute software type. */
	private String beforeDistributeSoftwareType; 		// 배포되었던 소프트웨어/모델 타입

	/** The distribute deploy yn. */
	private String distributeDeployYn; 			// 배포유무
	
	/** The distribute deploy time. */
	private String distributeDeployTime; 		// 배포시간
	
	/** The distribute deploy user. */
	private String distributeDeployUser; 		// 배포자
	
	private String beforeDistributeDeployUser;

	/** The distribute rejector. */
	private String distributeRejector; 		// 배포취소자
	
	/** The distribute rejected time. */
	private String distributeRejectedTime; 		// 배포취소일시
	
	/** The distribute deploy error msg. */
	private String distributeDeployErrorMsg; 	// 에러메세지
	
	/** The distribute deploy model yn. */
	private String distributeDeployModelYn = "N";		// 모델만 배포유무
	
	/** The distribute osd key. */
	private String distributeOsdKey;			// descKey
	
	/** The distribute last modified. */
	private String distributeLastModified;		// 배포 최종수정시간
	
	/** The distribute reserved user. */
	private String distributeReservedUser; // 배포 예약자
	
	/** The distribute reserve flag. */
	private String distributeReserveFlag; // 스케줄러 여부
	
	/** The save flag. */
	private String saveFlag;
	
	private String excelDownloadFlag = "N"; // project list excel downlaod 처리 시 Y
	
	// 사용자 comment가 이중등록되는 경우 "Y"으로 설정
	private String ignoreUserCommentReg;
	
	/* The ossReportFlag */
	private String ossReportFlag;
	
	private String ossNameMergeFlag;
	
	private String depCsvFileFlag = "N";
	private String srcCsvFileFlag = "N";
	private String binCsvFileFlag = "N";
	private String binBinaryFileFlag = "N";
	private String srcAndroidCsvFileFlag = "N";
	private String srcAndroidNoticeFileFlag = "N";
	private String srcAndroidNoticeXmlFileFlag = "N";
	private String srcAndroidResultFileFlag = "N";
	
	private String identificationStatusConfFlag = "N";
	
	private String verificationStatusConfFlag = "N";
	
	private int permission;
	
	private int statusPermission;
	
	private String secCode;
	
	private Float standardScore;
	
	public String getIgnoreUserCommentReg() {
		return ignoreUserCommentReg;
	}

	public void setIgnoreUserCommentReg(String ignoreUserCommentReg) {
		this.ignoreUserCommentReg = ignoreUserCommentReg;
	}

	public String getExcelDownloadFlag() {
		return excelDownloadFlag;
	}

	public void setExcelDownloadFlag(String excelDownloadFlag) {
		this.excelDownloadFlag = excelDownloadFlag;
	}

	/** The license file name. */
	private String licenseFileName; // 라이센스 파일명
	
	/** The open source file name. */
	private String openSourceFileName; // 오픈소스 파일명

	/** The bin csv file id. */
	private String depCsvFileId;
	
	/** The src csv file id. */
	// FILE_ID
	private String srcCsvFileId;
	
	/** The bin csv file id. */
	private String binCsvFileId;
	
	/** The bin binary file id. */
	private String binBinaryFileId;
	
	/** The src android csv file id. */
	private String srcAndroidCsvFileId;
	
	/** The src android notice file id. */
	private String srcAndroidNoticeFileId;
	
	/** The src android notice xml id. */
	private String srcAndroidNoticeXmlId;
	
	/** The src android result file id. */
	private String srcAndroidResultFileId;
	
	/** The notice file id. */
	private String noticeFileId; // 라이센스 파일ID

	/** The review report file id. */
	private String reviewReportFileId;
	
	/** The package file id. */
	private String packageFileId; // 오픈소스 파일ID
	private String packageFileId2; // 오픈소스 파일ID
	private String packageFileId3; // 오픈소스 파일ID
	
	/** The notice file. */
	private List<T2File> noticeFile; // 라이센스 파일객체

	/** The review report file. */
	private List<T2File> reviewReportFile;
	
	/** The package file. */
	private List<T2File> packageFile; // 오픈소스 파일객체
	private List<T2File> packageFile2; // 오픈소스 파일객체
	private List<T2File> packageFile3; // 오픈소스 파일객체
	
	/** The dep csv file. */
	private List<T2File> depCsvFile; // csv 파일 객체
	
	/** The csv file. */
	private List<T2File> csvFile; // csv 파일 객체
	
	/** The csv file seq. */
	private List<T2File> csvFileSeq; // csv Add Seq 파일 객체
	
	/** The bin csv file. */
	private List<T2File> binCsvFile; // csv 파일 객체
	
	/** The bin binary file. */
	private List<T2File> binBinaryFile; // csv 파일 객체
	
	/** The android csv file. */
	private List<T2File> androidCsvFile; // 안드로이드 csv 파일 객체
	
	/** The android notice file. */
	private List<T2File> androidNoticeFile; // 안드로이드 notice 파일 객체
	
	/** The android result file. */
	private List<T2File> androidResultFile; // 안드로이드 Result 파일 객체

	/** The notice file info. */
	private T2File noticeFileInfo; // 라이센스 파일객체
	
	/** The package file info. */
	private T2File packageFileInfo; // 오픈소스 파일객체
	private T2File packageFileInfo2; // 오픈소스 파일객체
	private T2File packageFileInfo3; // 오픈소스 파일객체
	
	/** The notice text file id. */
	private String noticeTextFileId;
	
	/** The simple html file id. */
	private String simpleHtmlFileId;
	
	/** The simple text file id. */
	private String simpleTextFileId;
	
	/** The spdx sheet file id. */
	private String spdxSheetFileId;
	
	/** The spdx rdf file id. */
	private String spdxRdfFileId;
	
	/** The spdx tag file id. */
	private String spdxTagFileId;

	/** The spdx json file id. */
	private String spdxJsonFileId;

	/** The spdx yaml file id. */
	private String spdxYamlFileId;
	
	/** The zip file id. */
	private String zipFileId;
	
	/** The oss id. */
	// OSS 검색용
	private String ossId;
	
	/** The oss name. */
	private String ossName;
	private String[] ossNickNames;
	
	/** The oss version. */
	private String ossVersion;
	
	/** The license name. */
	private String licenseName;
	
	/** The status. */
	private String status;
	private String statuses;
	private String[] arrStatuses;
	
	/** The status org. */
	private String statusOrg;
	
	/** The ref partner id. */
	private String refPartnerId;

	/** The readme content. */
	// README 파일 용
	private String readmeContent;
	
	/** The readme file name. */
	private String readmeFileName;
	
	/** The readme yn. */
	private String readmeYn;

	/** The verify file content. */
	private String verifyFileContent;
	
	/** The verify file count. */
	private String verifyFileCount;
	
	/** The except file content. */
	private String exceptFileContent;
	
	/** The sch start date. */
	// OTHER
	private String schStartDate;
	
	/** The sch end date. */
	private String schEndDate;
	
	/** The prj model json. */
	private String prjModelJson;
	
	/** The prj delete model json. */
	private String prjDeleteModelJson;
	
	/** The division. */
	private String division;
	
	/** The vuln yn. */
	private String vulnYn;
	
	/** The cvss score. */
	private String cvssScore;
	
	/** The cve id. */
	private String cveId;
	
	/** The android sheet num. */
	private String[] androidSheetNum;
	
	/** The binary name. */
	private String binaryName;
	private String schBinaryName;
	
	/** The partner name. */
	private String partnerName;
	
	/** The bin src flag. */
	private String binSrcFlag;
	
	/** The software name. */
	private String softwareName;
	
	/** The reference div. */
	private String referenceDiv;
	
	/** The comm ref div. */
	private String commRefDiv;
	
	/** The android flag. */
	private String androidFlag;
	
	/** The network server flag. */
	private String networkServerFlag = "N";
	
	/** The load from android project flag. */
	private String loadFromAndroidProjectFlag = "N";
	
	/** The creator nm. */
	private String creatorNm;
	
	/** The vulnerability. */
	private String vulnerability;
	
	/** The watcher str. */
	private String watcherStr;
	
	/** The upd vuln. */
	private String updVuln;
	
	/** The distribute check yn. */
	private String distributeCheckYn;
	
	/** The sent osdd del mail. */
	private String sentOsddDelMail;
	
	/** The sent osdd diff file mail. */
	private String sentOsddDiffFileMail;
	
	/** The need package file reset. */
	private String needPackageFileReset;
	
	/** The reviewer name. */
	private String reviewerName;
	
	/** The osdd notice file name. */
	private String osddNoticeFileName;
	
	/** The osdd notice file E-TAG. */
	private String osddNoticeFileEtag;
	
	/** The osdd source file name. */
	private String osddSourceFileName;
	
	/** The osdd source file name. */
	private String osddSourceFileName2;

	/** The osdd source file name. */
	private String osddSourceFileName3;
	
	/** The osdd source file E-TAG. */
	private String osddSourceFileEtag;
	
	/** The osdd source file E-TAG. */
	private String osddSourceFileEtag2;
	
	/** The osdd source file E-TAG. */
	private String osddSourceFileEtag3;

	private String stage;
	
	private String prjDivisionId;
	
	/** The publicYn. */
	private String publicYn;
	
	/** The viewOnlyFlag. */
	private String viewOnlyFlag;

	/** selfcheck 등 vulnerability 추출 대상에 상위버전 모함여부 */
	private String versionMatchedFlag = "N";
	
	private String resetDistributionStatus = "N";
	
	/* 2018-07-19 choye 추가  */
	/** The commId. */
	private String commId;
	
	/** The statusRequestYn. */
	private String statusRequestYn;
	
	/* 2018-07-27 choye 추가  */
	/** The delOsdd. */
	private String delOsdd;
	
	private String ossCount;
	
	private String deleteMemo;
	
	private String componentCount;
	
	private String referenceId;
	
	/** packaging file delete YN */ 
	private String deleteFlag;
	
	/** packaging verify checking flag */
	private String statusVerifyYn;
	
	private String userRole;
	
	private String reuseKeyword;
	
	private String ossAnalysisStatus;
	
	private String analysisStartDate;
	
	private String modelFlag;
	
	private String changeStatusFlag;

	private String ossNameTemp;
	
	private String dependencies;
	
	/** The cyclonedx file id. */
	private String cdxJsonFileId;
	private String cdxXmlFileId;
	
	/**
	 * Gets the upd vuln.
	 *
	 * @return the upd vuln
	 */
	public String getUpdVuln() {
		return updVuln;
	}

	/**
	 * Sets the upd vuln.
	 *
	 * @param updVuln the new upd vuln
	 */
	public void setUpdVuln(String updVuln) {
		this.updVuln = updVuln;
	}

	/**
	 * Gets the watcher str.
	 *
	 * @return the watcher str
	 */
	public String getWatcherStr() {
		return watcherStr;
	}

	/**
	 * Sets the watcher str.
	 *
	 * @param watcherStr the new watcher str
	 */
	public void setWatcherStr(String watcherStr) {
		this.watcherStr = watcherStr;
	}
	
	/** Package verication 미수행여부. */
	private String skipPackageFlag = "N";
	
	/** The without verify yn. */
	private String withoutVerifyYn;
	
	/** binary DB 등록 무시처리 */
	private String ignoreBinaryDbFlag;

	/**  사용자 임시저장중인 comment. */
	private String userComment;
	
	/** list - prj(project), par(3rdparty), bat */
	private String listKind;
	
	/** listKind pair id */
	private String listId;
	
	private String BomCnt;
	
	private String discloseCnt;
	
	private String productGroup;
	
	private List<String> productGroups;
    
	private String targetName;
	
	private String networkServerType;

	private String priority;
	
	private String reProcessDistributionFlag = "N";
	
	private String changedNoticeYn = "N";
	
	private String modelListAppendFlag = "N";
	
	private String modelSeq;
	
	private String deleteOsddFlag;

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
	 * Gets the prj name.
	 *
	 * @return the prj name
	 */
	public String getPrjName() {
		return prjName;
	}

	/**
	 * Sets the prj name.
	 *
	 * @param prjName the new prj name
	 */
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	/**
	 * Gets the prj version.
	 *
	 * @return the prj version
	 */
	public String getPrjVersion() {
		return prjVersion;
	}

	/**
	 * Sets the prj version.
	 *
	 * @param prjVersion the new prj version
	 */
	public void setPrjVersion(String prjVersion) {
		this.prjVersion = prjVersion;
	}

	/**
	 * Gets the distribution type.
	 *
	 * @return the distribution type
	 */
	public String getDistributionType() {
		return distributionType;
	}

	/**
	 * Sets the distribution type.
	 *
	 * @param distributionType the new distribution type
	 */
	public void setDistributionType(String distributionType) {
		this.distributionType = distributionType;
		if (!isEmpty(distributionType) && isEmpty(this.distributionTypeOfCodeDtlExp)) {
			this.distributionTypeOfCodeDtlExp = CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE, distributionType);
		}
	}

	/**
	 * Gets the oss notice due date.
	 *
	 * @return the oss notice due date
	 */
	public String getOssNoticeDueDate() {
		return ossNoticeDueDate;
	}

	/**
	 * Sets the oss notice due date.
	 *
	 * @param ossNoticeDueDate the new oss notice due date
	 */
	public void setOssNoticeDueDate(String ossNoticeDueDate) {
		this.ossNoticeDueDate = ossNoticeDueDate;
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
	 * Gets the comment idx.
	 *
	 * @return the comment idx
	 */
	public String getCommentIdx() {
		return commentIdx;
	}

	/**
	 * Sets the comment idx.
	 *
	 * @param commentIdx the new comment idx
	 */
	public void setCommentIdx(String commentIdx) {
		this.commentIdx = commentIdx;
	}

	/**
	 * Gets the os type.
	 *
	 * @return the os type
	 */
	public String getOsType() {
		return osType;
	}

	/**
	 * Sets the os type.
	 *
	 * @param osType the new os type
	 */
	public void setOsType(String osType) {
		this.osType = osType;
	}

	/**
	 * Gets the os type etc.
	 *
	 * @return the os type etc
	 */
	public String getOsTypeEtc() {
		return osTypeEtc;
	}

	/**
	 * Sets the os type etc.
	 *
	 * @param osTypeEtc the new os type etc
	 */
	public void setOsTypeEtc(String osTypeEtc) {
		this.osTypeEtc = osTypeEtc;
	}

	/**
	 * Gets the identification status.
	 *
	 * @return the identification status
	 */
	public String getIdentificationStatus() {
		return identificationStatus;
	}

	/**
	 * Sets the identification status.
	 *
	 * @param identificationStatus the new identification status
	 */
	public void setIdentificationStatus(String identificationStatus) {
		this.identificationStatus = identificationStatus;
	}

	/**
	 * Gets the verification status.
	 *
	 * @return the verification status
	 */
	public String getVerificationStatus() {
		return verificationStatus;
	}

	/**
	 * Sets the verification status.
	 *
	 * @param verificationStatus the new verification status
	 */
	public void setVerificationStatus(String verificationStatus) {
		this.verificationStatus = verificationStatus;
	}

	/**
	 * Gets the destribution name.
	 *
	 * @return the destribution name
	 */
	public String getDestributionName() {
		return destributionName;
	}

	/**
	 * Sets the destribution name.
	 *
	 * @param destributionName the new destribution name
	 */
	public void setDestributionName(String destributionName) {
		this.destributionName = destributionName;
	}

	/**
	 * Gets the destribution software type.
	 *
	 * @return the destribution software type
	 */
	public String getDestributionSoftwareType() {
		return destributionSoftwareType;
	}

	/**
	 * Sets the destribution software type.
	 *
	 * @param destributionSoftwareType the new destribution software type
	 */
	public void setDestributionSoftwareType(String destributionSoftwareType) {
		this.destributionSoftwareType = destributionSoftwareType;
	}

	/**
	 * Gets the identification sub status partner.
	 *
	 * @return the identification sub status partner
	 */
	public String getIdentificationSubStatusPartner() {
		return identificationSubStatusPartner;
	}

	/**
	 * Sets the identification sub status partner.
	 *
	 * @param identificationSubStatusPartner the new identification sub status partner
	 */
	public void setIdentificationSubStatusPartner(String identificationSubStatusPartner) {
		this.identificationSubStatusPartner = identificationSubStatusPartner;
	}

	/**
	 * Gets the identification sub status src.
	 *
	 * @return the identification sub status src
	 */
	public String getIdentificationSubStatusSrc() {
		return identificationSubStatusSrc;
	}

	/**
	 * Sets the identification sub status src.
	 *
	 * @param identificationSubStatusSrc the new identification sub status src
	 */
	public void setIdentificationSubStatusSrc(String identificationSubStatusSrc) {
		this.identificationSubStatusSrc = identificationSubStatusSrc;
	}

	/**
	 * Gets the identification sub status bat.
	 *
	 * @return the identification sub status bat
	 */
	public String getIdentificationSubStatusBat() {
		return identificationSubStatusBat;
	}

	/**
	 * Sets the identification sub status bat.
	 *
	 * @param identificationSubStatusBat the new identification sub status bat
	 */
	public void setIdentificationSubStatusBat(String identificationSubStatusBat) {
		this.identificationSubStatusBat = identificationSubStatusBat;
	}

	/**
	 * Gets the complete yn.
	 *
	 * @return the complete yn
	 */
	public String getCompleteYn() {
		return completeYn;
	}

	/**
	 * Sets the complete yn.
	 *
	 * @param completeYn the new complete yn
	 */
	public void setCompleteYn(String completeYn) {
		this.completeYn = completeYn;
	}

	/**
	 * Gets the drop yn.
	 *
	 * @return the drop yn
	 */
	public String getDropYn() {
		return dropYn;
	}

	/**
	 * Sets the drop yn.
	 *
	 * @param dropYn the new drop yn
	 */
	public void setDropYn(String dropYn) {
		this.dropYn = dropYn;
	}
	
	public String getVerifyYn() {
		return verifyYn;
	}
	
	public void setVerifyYn(String verifyYn) {
		this.verifyYn = verifyYn;
	}
	
	/**
	 * Gets the reviewer.
	 *
	 * @return the reviewer
	 */
	public String getReviewer() {
		return reviewer;
	}

	/**
	 * Sets the reviewer.
	 *
	 * @param reviewer the new reviewer
	 */
	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}
	
	/**
	 * Gets the user use yn.
	 *
	 * @return the use yn
	 */
	public String getUseYn() {
		return useYn;
	}

	/**
	 * Sets the user use yn.
	 *
	 * @param useYn the new use yn
	 */
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	
	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the category.
	 *
	 * @param category the new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Gets the subcategory.
	 *
	 * @return the subcategory
	 */
	public String getSubcategory() {
		return subcategory;
	}

	/**
	 * Sets the subcategory.
	 *
	 * @param subcategory the new subcategory
	 */
	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}

	/**
	 * Gets the model name.
	 *
	 * @return the model name
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * Sets the model name.
	 *
	 * @param modelName the new model name
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 * Gets the release date.
	 *
	 * @return the release date
	 */
	public String getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Sets the release date.
	 *
	 * @param releaseDate the new release date
	 */
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	/**
	 * Gets the model list info.
	 *
	 * @return the model list info
	 */
	public List<String> getModelListInfo() {
		return modelListInfo;
	}
	
	/**
	 * Sets the model list info.
	 *
	 * @param modelListInfo the new model list info
	 */
	public void setModelListInfo(List<String> modelListInfo) {
		this.modelListInfo = modelListInfo;
	}

	/**
	 * Gets the prj division.
	 *
	 * @return the prj division
	 */
	public String getPrjDivision() {
		return prjDivision;
	}

	/**
	 * Sets the prj division.
	 *
	 * @param prjDivision the new prj division
	 */
	public void setPrjDivision(String prjDivision) {
		this.prjDivision = prjDivision;
	}

	/**
	 * Gets the prj division name.
	 *
	 * @return the prj division name
	 */
	public String getPrjDivisionName() {
		return prjDivisionName;
	}

	/**
	 * Sets the prj division name.
	 *
	 * @param prjDivisionName the new prj division name
	 */
	public void setPrjDivisionName(String prjDivisionName) {
		this.prjDivisionName = prjDivisionName;
	}

	/**
	 * Gets the prj user id.
	 *
	 * @return the prj user id
	 */
	public String getPrjUserId() {
		return prjUserId;
	}

	/**
	 * Sets the prj user id.
	 *
	 * @param prjUserId the new prj user id
	 */
	public void setPrjUserId(String prjUserId) {
		this.prjUserId = prjUserId;
	}

	/**
	 * Gets the prj user name.
	 *
	 * @return the prj user name
	 */
	public String getPrjUserName() {
		return prjUserName;
	}

	/**
	 * Sets the prj user name.
	 *
	 * @param prjUserName the new prj user name
	 */
	public void setPrjUserName(String prjUserName) {
		this.prjUserName = prjUserName;
	}
	
	/**
	 * Gets the prj email.
	 *
	 * @return the prj email
	 */
	public String getPrjEmail() {
		return prjEmail;
	}

	/**
	 * Sets the prj email.
	 *
	 * @param prjEmail the new prj email
	 */
	public void setPrjEmail(String prjEmail) {
		this.prjEmail = prjEmail;
	}

	/**
	 * Gets the watchers.
	 *
	 * @return the watchers
	 */
	public String[] getWatchers() {
		return watchers != null ? watchers.clone() : null;
	}

	/**
	 * Sets the watchers.
	 *
	 * @param watchers the new watchers
	 */
	public void setWatchers(String[] watchers) {
		this.watchers = watchers != null ? watchers.clone() : null;
	}

	public void setWatchers(String watcher) {
		if (!isEmpty(watcher)) {
			this.watchers = new String[] {watcher};
		} else {
			this.watchers = null;
		}
	}
	
	/**
	 * Gets the watcher list.
	 *
	 * @return the watcher list
	 */
	public List<Project> getWatcherList() {
		return watcherList;
	}

	/**
	 * Sets the watcher list.
	 *
	 * @param watcherList the new watcher list
	 */
	public void setWatcherList(List<Project> watcherList) {
		this.watcherList = watcherList;
	}
	
	/**
	 * Gets the division list.
	 *
	 * @return the division list
	 */
	public ArrayList<Map<String, String>> getDivisionList() {
		return divisionList;
	}

	/**
	 * Sets the division list.
	 *
	 * @param divisionList the division list
	 */
	public void setDivisionList(ArrayList<Map<String, String>> divisionList) {
		this.divisionList = divisionList;
	}
	
	/**
	 * Gets the email list.
	 *
	 * @return the email list
	 */
	public ArrayList<Map<String, String>> getEmailList() {
		return emailList;
	}

	/**
	 * Sets the email list.
	 *
	 * @param emailList the email list
	 */
	public void setEmailList(ArrayList<Map<String, String>> emailList) {
		this.emailList = emailList;
	}
	
	/**
	 * Gets the model list.
	 *
	 * @return the model list
	 */
	public List<Project> getModelList() {
		return modelList;
	}

	/**
	 * Sets the model list.
	 *
	 * @param modelList the new model list
	 */
	public void setModelList(List<Project> modelList) {
		this.modelList = modelList;
	}

	/**
	 * Gets the destribution status.
	 *
	 * @return the destribution status
	 */
	public String getDestributionStatus() {
		return destributionStatus;
	}

	/**
	 * Sets the destribution status.
	 *
	 * @param destributionStatus the new destribution status
	 */
	public void setDestributionStatus(String destributionStatus) {
		this.destributionStatus = destributionStatus;
	}
	
	public String getBeforeDistributionStatus() {
		return beforeDistributionStatus;
	}

	public void setBeforeDistributionStatus(String beforeDistributionStatus) {
		this.beforeDistributionStatus = beforeDistributionStatus;
	}

	/**
	 * Gets the license file name.
	 *
	 * @return the license file name
	 */
	public String getLicenseFileName() {
		return licenseFileName;
	}

	/**
	 * Sets the license file name.
	 *
	 * @param licenseFileName the new license file name
	 */
	public void setLicenseFileName(String licenseFileName) {
		this.licenseFileName = licenseFileName;
	}

	/**
	 * Gets the open source file name.
	 *
	 * @return the open source file name
	 */
	public String getOpenSourceFileName() {
		return openSourceFileName;
	}

	/**
	 * Sets the open source file name.
	 *
	 * @param openSourceFileName the new open source file name
	 */
	public void setOpenSourceFileName(String openSourceFileName) {
		this.openSourceFileName = openSourceFileName;
	}

	/**
	 * Gets the src csv file id.
	 *
	 * @return the src csv file id
	 */
	public String getSrcCsvFileId() {
		return srcCsvFileId;
	}

	/**
	 * Sets the src csv file id.
	 *
	 * @param srcCsvFileId the new src csv file id
	 */
	public void setSrcCsvFileId(String srcCsvFileId) {
		this.srcCsvFileId = srcCsvFileId;
	}

	/**
	 * Gets the src android csv file id.
	 *
	 * @return the src android csv file id
	 */
	public String getSrcAndroidCsvFileId() {
		return srcAndroidCsvFileId;
	}

	/**
	 * Sets the src android csv file id.
	 *
	 * @param srcAndroidCsvFileId the new src android csv file id
	 */
	public void setSrcAndroidCsvFileId(String srcAndroidCsvFileId) {
		this.srcAndroidCsvFileId = srcAndroidCsvFileId;
	}

	/**
	 * Gets the src android notice file id.
	 *
	 * @return the src android notice file id
	 */
	public String getSrcAndroidNoticeFileId() {
		return srcAndroidNoticeFileId;
	}

	/**
	 * Sets the src android notice file id.
	 *
	 * @param srcAndroidNoticeFileId the new src android notice file id
	 */
	public void setSrcAndroidNoticeFileId(String srcAndroidNoticeFileId) {
		this.srcAndroidNoticeFileId = srcAndroidNoticeFileId;
	}

	/**
	 * Gets the notice file id.
	 *
	 * @return the notice file id
	 */
	public String getNoticeFileId() {
		return noticeFileId;
	}

	/**
	 * Sets the notice file id.
	 *
	 * @param noticeFileId the new notice file id
	 */
	public void setNoticeFileId(String noticeFileId) {
		this.noticeFileId = noticeFileId;
	}

	/**
	 * Gets the review report file id.
	 *
	 * @return the review report file id
	 */
	public String getReviewReportFileId() {
		return reviewReportFileId;
	}

	/**
	 * Sets the review report file id.
	 *
	 * @param reviewReportFileId the new review report file id
	 */
	public void setReviewReportFileId(String reviewReportFileId) {
		this.reviewReportFileId = reviewReportFileId;
	}

	/**
	 * Gets the package file id.
	 *
	 * @return the package file id
	 */
	public String getPackageFileId() {
		return packageFileId;
	}

	/**
	 * Sets the package file id.
	 *
	 * @param packageFileId the new package file id
	 */
	public void setPackageFileId(String packageFileId) {
		this.packageFileId = packageFileId;
	}

	/**
	 * Gets the notice file.
	 *
	 * @return the notice file
	 */
	public List<T2File> getNoticeFile() {
		return noticeFile;
	}

	/**
	 * Sets the notice file.
	 *
	 * @param noticeFile the new notice file
	 */
	public void setNoticeFile(List<T2File> noticeFile) {
		this.noticeFile = noticeFile;
	}

	/**
	 * Get the review report file.
	 *
	 * @return the review report file
	 */
	public List<T2File> getReviewReportFile() {
		return this.reviewReportFile;
	}

	/**
	 * Set the review report file.
	 *
	 * @param reviewReportFile the new review report file
	 */
	public void setReviewReportFile(List<T2File> reviewReportFile) {
		this.reviewReportFile = reviewReportFile;
	}

	/**
	 * Gets the package file.
	 *
	 * @return the package file
	 */
	public List<T2File> getPackageFile() {
		return packageFile;
	}

	/**
	 * Sets the package file.
	 *
	 * @param packageFile the new package file
	 */
	public void setPackageFile(List<T2File> packageFile) {
		this.packageFile = packageFile;
	}

	/**
	 * Gets the csv file.
	 *
	 * @return the csv file
	 */
	public List<T2File> getCsvFile() {
		return csvFile;
	}

	/**
	 * Sets the csv file.
	 *
	 * @param csvFile the new csv file
	 */
	public void setCsvFile(List<T2File> csvFile) {
		this.csvFile = csvFile;
	}

	/**
	 * Gets the android csv file.
	 *
	 * @return the android csv file
	 */
	public List<T2File> getAndroidCsvFile() {
		return androidCsvFile;
	}

	/**
	 * Sets the android csv file.
	 *
	 * @param androidCsvFile the new android csv file
	 */
	public void setAndroidCsvFile(List<T2File> androidCsvFile) {
		this.androidCsvFile = androidCsvFile;
	}

	/**
	 * Gets the android notice file.
	 *
	 * @return the android notice file
	 */
	public List<T2File> getAndroidNoticeFile() {
		return androidNoticeFile;
	}

	/**
	 * Sets the android notice file.
	 *
	 * @param androidNoticeFile the new android notice file
	 */
	public void setAndroidNoticeFile(List<T2File> androidNoticeFile) {
		this.androidNoticeFile = androidNoticeFile;
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
	 * Gets the status org.
	 *
	 * @return the status org
	 */
	public String getStatusOrg() {
		return statusOrg;
	}

	/**
	 * Sets the status org.
	 *
	 * @param statusOrg the new status org
	 */
	public void setStatusOrg(String statusOrg) {
		this.statusOrg = statusOrg;
	}

	/**
	 * Gets the prj model json.
	 *
	 * @return the prj model json
	 */
	public String getPrjModelJson() {
		return prjModelJson;
	}

	/**
	 * Sets the prj model json.
	 *
	 * @param prjModelJson the new prj model json
	 */
	public void setPrjModelJson(String prjModelJson) {
		this.prjModelJson = prjModelJson;
	}

	/**
	 * Gets the copy.
	 *
	 * @return the copy
	 */
	public String getCopy() {
		return copy;
	}

	/**
	 * Sets the copy.
	 *
	 * @param copy the new copy
	 */
	public void setCopy(String copy) {
		this.copy = copy;
	}

	/**
	 * Gets the old id.
	 *
	 * @return the old id
	 */
	public String getOldId() {
		return oldId;
	}

	/**
	 * Sets the old id.
	 *
	 * @param oldId the new old id
	 */
	public void setOldId(String oldId) {
		this.oldId = oldId;
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
	 * Gets the serialversionuid.
	 *
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Gets the prj ids.
	 *
	 * @return the prj ids
	 */
	public String[] getPrjIds() {
		return prjIds != null ? prjIds.clone() : null;
	}

	/**
	 * Sets the prj ids.
	 *
	 * @param prjIds the new prj ids
	 */
	public void setPrjIds(String[] prjIds) {
		this.prjIds = prjIds != null ? prjIds.clone() : null;
	}

	/**
	 * Gets the ref partner id.
	 *
	 * @return the ref partner id
	 */
	public String getRefPartnerId() {
		return refPartnerId;
	}

	/**
	 * Sets the ref partner id.
	 *
	 * @param refPartnerId the new ref partner id
	 */
	public void setRefPartnerId(String refPartnerId) {
		this.refPartnerId = refPartnerId;
	}

	/**
	 * Gets the readme content.
	 *
	 * @return the readme content
	 */
	public String getReadmeContent() {
		return readmeContent;
	}

	/**
	 * Sets the readme content.
	 *
	 * @param readmeContent the new readme content
	 */
	public void setReadmeContent(String readmeContent) {
		this.readmeContent = readmeContent;
	}

	/**
	 * Gets the readme yn.
	 *
	 * @return the readme yn
	 */
	public String getReadmeYn() {
		return readmeYn;
	}

	/**
	 * Sets the readme yn.
	 *
	 * @param readmeYn the new readme yn
	 */
	public void setReadmeYn(String readmeYn) {
		this.readmeYn = readmeYn;
	}

	/**
	 * Gets the distribute master category.
	 *
	 * @return the distribute master category
	 */
	public String getDistributeMasterCategory() {
		return distributeMasterCategory;
	}

	/**
	 * Sets the distribute master category.
	 *
	 * @param distributeMasterCategory the new distribute master category
	 */
	public void setDistributeMasterCategory(String distributeMasterCategory) {
		this.distributeMasterCategory = distributeMasterCategory;
	}

	/**
	 * Gets the distribute name.
	 *
	 * @return the distribute name
	 */
	public String getDistributeName() {
		return distributeName;
	}

	/**
	 * Sets the distribute name.
	 *
	 * @param distributeName the new distribute name
	 */
	public void setDistributeName(String distributeName) {
		this.distributeName = distributeName;
	}
	
	public String getBeforeDistributeName() {
		return beforeDistributeName;
	}

	public void setBeforeDistributeName(String beforeDistributeName) {
		this.beforeDistributeName = beforeDistributeName;
	}
	
	/**
	 * Gets the distribute software type.
	 *
	 * @return the distribute software type
	 */
	public String getDistributeSoftwareType() {
		return distributeSoftwareType;
	}

	/**
	 * Sets the distribute software type.
	 *
	 * @param distributeSoftwareType the new distribute software type
	 */
	public void setDistributeSoftwareType(String distributeSoftwareType) {
		this.distributeSoftwareType = distributeSoftwareType;
	}

	public String getBeforeDistributeSoftwareType() {
		return beforeDistributeSoftwareType;
	}

	public void setBeforeDistributeSoftwareType(String beforeDistributeSoftwareType) {
		this.beforeDistributeSoftwareType = beforeDistributeSoftwareType;
	}
	
	/**
	 * Gets the distribute deploy yn.
	 *
	 * @return the distribute deploy yn
	 */
	public String getDistributeDeployYn() {
		return distributeDeployYn;
	}

	/**
	 * Sets the distribute deploy yn.
	 *
	 * @param distributeDeployYn the new distribute deploy yn
	 */
	public void setDistributeDeployYn(String distributeDeployYn) {
		this.distributeDeployYn = distributeDeployYn;
	}

	/**
	 * Gets the distribute deploy time.
	 *
	 * @return the distribute deploy time
	 */
	public String getDistributeDeployTime() {
		return distributeDeployTime;
	}

	/**
	 * Sets the distribute deploy time.
	 *
	 * @param distributeDeployTime the new distribute deploy time
	 */
	public void setDistributeDeployTime(String distributeDeployTime) {
		this.distributeDeployTime = distributeDeployTime;
	}

	/**
	 * Gets the distribute target.
	 *
	 * @return the distribute target
	 */
	public String getDistributeTarget() {
		return distributeTarget;
	}

	/**
	 * Sets the distribute target.
	 *
	 * @param distributeTarget the new distribute target
	 */
	public void setDistributeTarget(String distributeTarget) {
		this.distributeTarget = distributeTarget;
	}

	/**
	 * Gets the sch start date.
	 *
	 * @return the sch start date
	 */
	public String getSchStartDate() {
		return schStartDate;
	}

	/**
	 * Sets the sch start date.
	 *
	 * @param schStartDate the new sch start date
	 */
	public void setSchStartDate(String schStartDate) {
		this.schStartDate = schStartDate;
	}

	/**
	 * Gets the sch end date.
	 *
	 * @return the sch end date
	 */
	public String getSchEndDate() {
		return schEndDate;
	}

	/**
	 * Sets the sch end date.
	 *
	 * @param schEndDate the new sch end date
	 */
	public void setSchEndDate(String schEndDate) {
		this.schEndDate = schEndDate;
	}

	/**
	 * Gets the verify file content.
	 *
	 * @return the verify file content
	 */
	public String getVerifyFileContent() {
		return verifyFileContent;
	}

	/**
	 * Sets the verify file content.
	 *
	 * @param verifyFileContent the new verify file content
	 */
	public void setVerifyFileContent(String verifyFileContent) {
		this.verifyFileContent = verifyFileContent;
	}

	/**
	 * Gets the except file content.
	 *
	 * @return the except file content
	 */
	public String getExceptFileContent() {
		return exceptFileContent;
	}

	/**
	 * Sets the except file content.
	 *
	 * @param exceptFileContent the new except file content
	 */
	public void setExceptFileContent(String exceptFileContent) {
		this.exceptFileContent = exceptFileContent;
	}

	/**
	 * Etc str.
	 *
	 * @return the string
	 */
	public String etcStr() {
		StringBuilder etc = new StringBuilder();
		etc.append(modifiedDate + "|" + loginUserRole() + "|" + modifier);
		if (!StringUtil.isEmpty(identificationStatus)) {
			etc.append("|I:" + identificationStatus);
		} else {
			etc.append("|I:null");
		}
		if (!StringUtil.isEmpty(verificationStatus)) {
			etc.append("|V:" + verificationStatus);
		} else {
			etc.append("|V:null");
		}
		if (!StringUtil.isEmpty(destributionStatus)) {
			etc.append("|D:" + destributionStatus);
		} else {
			etc.append("|D:null");
		}
		return etc.toString();
	}

	/**
	 * Gets the last modified time.
	 *
	 * @return the last modified time
	 */
	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * Sets the last modified time.
	 *
	 * @param lastModifiedTime the new last modified time
	 */
	public void setLastModifiedTime(String lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
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
	}

	/**
	 * Gets the distribute deploy error msg.
	 *
	 * @return the distribute deploy error msg
	 */
	public String getDistributeDeployErrorMsg() {
		return distributeDeployErrorMsg;
	}

	/**
	 * Sets the distribute deploy error msg.
	 *
	 * @param distributeDeployErrorMsg the new distribute deploy error msg
	 */
	public void setDistributeDeployErrorMsg(String distributeDeployErrorMsg) {
		this.distributeDeployErrorMsg = distributeDeployErrorMsg;
	}

	/**
	 * Gets the distribute deploy model yn.
	 *
	 * @return the distribute deploy model yn
	 */
	public String getDistributeDeployModelYn() {
		return distributeDeployModelYn;
	}

	/**
	 * Sets the distribute deploy model yn.
	 *
	 * @param distributeDeployModelYn the new distribute deploy model yn
	 */
	public void setDistributeDeployModelYn(String distributeDeployModelYn) {
		this.distributeDeployModelYn = distributeDeployModelYn;
	}

	/**
	 * Gets the distribute last modified.
	 *
	 * @return the distribute last modified
	 */
	public String getDistributeLastModified() {
		return distributeLastModified;
	}

	/**
	 * Sets the distribute last modified.
	 *
	 * @param distributeLastModified the new distribute last modified
	 */
	public void setDistributeLastModified(String distributeLastModified) {
		this.distributeLastModified = distributeLastModified;
	}

	/**
	 * Gets the distribute osd key.
	 *
	 * @return the distribute osd key
	 */
	public String getDistributeOsdKey() {
		return distributeOsdKey;
	}

	/**
	 * Sets the distribute osd key.
	 *
	 * @param distributeOsdKey the new distribute osd key
	 */
	public void setDistributeOsdKey(String distributeOsdKey) {
		this.distributeOsdKey = distributeOsdKey;
	}

	/**
	 * Gets the android sheet num.
	 *
	 * @return the android sheet num
	 */
	public String[] getAndroidSheetNum() {
		return androidSheetNum != null ? androidSheetNum.clone() : null;
	}

	/**
	 * Sets the android sheet num.
	 *
	 * @param androidSheetNum the new android sheet num
	 */
	public void setAndroidSheetNum(String[] androidSheetNum) {
		this.androidSheetNum = androidSheetNum != null ?
			androidSheetNum.clone() : null;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the verify file count.
	 *
	 * @return the verify file count
	 */
	public String getVerifyFileCount() {
		return verifyFileCount;
	}

	/**
	 * Sets the verify file count.
	 *
	 * @param verifyFileCount the new verify file count
	 */
	public void setVerifyFileCount(String verifyFileCount) {
		this.verifyFileCount = verifyFileCount;
	}

	/**
	 * Gets the notice file info.
	 *
	 * @return the notice file info
	 */
	public T2File getNoticeFileInfo() {
		return noticeFileInfo;
	}

	/**
	 * Sets the notice file info.
	 *
	 * @param noticeFileInfo the new notice file info
	 */
	public void setNoticeFileInfo(T2File noticeFileInfo) {
		this.noticeFileInfo = noticeFileInfo;
	}

	/**
	 * Gets the package file info.
	 *
	 * @return the package file info
	 */
	public T2File getPackageFileInfo() {
		return packageFileInfo;
	}

	/**
	 * Sets the package file info.
	 *
	 * @param packageFileInfo the new package file info
	 */
	public void setPackageFileInfo(T2File packageFileInfo) {
		this.packageFileInfo = packageFileInfo;
	}

	/**
	 * Gets the binary name.
	 *
	 * @return the binary name
	 */
	public String getBinaryName() {
		return binaryName;
	}

	/**
	 * Sets the binary name.
	 *
	 * @param binaryName the new binary name
	 */
	public void setBinaryName(String binaryName) {
		this.binaryName = binaryName;
	}

	/**
	 * Gets the partner name.
	 *
	 * @return the partner name
	 */
	public String getPartnerName() {
		return partnerName;
	}

	/**
	 * Sets the partner name.
	 *
	 * @param partnerName the new partner name
	 */
	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
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
	 * Gets the save flag.
	 *
	 * @return the save flag
	 */
	public String getSaveFlag() {
		return saveFlag;
	}

	/**
	 * Sets the save flag.
	 *
	 * @param saveFlag the new save flag
	 */
	public void setSaveFlag(String saveFlag) {
		this.saveFlag = saveFlag;
	}

	/**
	 * Gets the src android result file id.
	 *
	 * @return the src android result file id
	 */
	public String getSrcAndroidResultFileId() {
		return srcAndroidResultFileId;
	}

	/**
	 * Sets the src android result file id.
	 *
	 * @param srcAndroidResultFileId the new src android result file id
	 */
	public void setSrcAndroidResultFileId(String srcAndroidResultFileId) {
		this.srcAndroidResultFileId = srcAndroidResultFileId;
	}

	/**
	 * Gets the android result file.
	 *
	 * @return the android result file
	 */
	public List<T2File> getAndroidResultFile() {
		return androidResultFile;
	}

	/**
	 * Sets the android result file.
	 *
	 * @param androidResultFile the new android result file
	 */
	public void setAndroidResultFile(List<T2File> androidResultFile) {
		this.androidResultFile = androidResultFile;
	}

	/**
	 * Gets the prj id list.
	 *
	 * @return the prj id list
	 */
	public List<String> getPrjIdList() {
		return prjIdList;
	}

	/**
	 * Sets the prj id list.
	 *
	 * @param prjIdList the new prj id list
	 */
	public void setPrjIdList(List<String> prjIdList) {
		this.prjIdList = prjIdList;
	}

	/**
	 * Adds the prj id list.
	 *
	 * @param prjId the prj id
	 */
	public void addPrjIdList(String prjId) {
		if (this.prjIdList == null) {
			this.prjIdList = new ArrayList<>();
		}
		this.prjIdList.add(prjId);
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
	 * Gets the bin src flag.
	 *
	 * @return the bin src flag
	 */
	public String getBinSrcFlag() {
		return binSrcFlag;
	}

	/**
	 * Sets the bin src flag.
	 *
	 * @param binSrcFlag the new bin src flag
	 */
	public void setBinSrcFlag(String binSrcFlag) {
		this.binSrcFlag = binSrcFlag;
	}

	/**
	 * Gets the software name.
	 *
	 * @return the software name
	 */
	public String getSoftwareName() {
		return softwareName;
	}

	/**
	 * Sets the software name.
	 *
	 * @param softwareName the new software name
	 */
	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}

	/**
	 * Gets the copy prj id.
	 *
	 * @return the copy prj id
	 */
	public String getCopyPrjId() {
		return copyPrjId;
	}

	/**
	 * Sets the copy prj id.
	 *
	 * @param copyPrjId the new copy prj id
	 */
	public void setCopyPrjId(String copyPrjId) {
		this.copyPrjId = copyPrjId;
	}

	/**
	 * Gets the notice type.
	 *
	 * @return the notice type
	 */
	public String getNoticeType() {
		return noticeType;
	}

	/**
	 * Sets the notice type.
	 *
	 * @param noticeType the new notice type
	 */
	public void setNoticeType(String noticeType) {
		this.noticeType = noticeType;
	}
	
	public String getNoticeTypeEtc() {
		return noticeTypeEtc;
	}

	public void setNoticeTypeEtc(String noticeTypeEtc) {
		this.noticeTypeEtc = noticeTypeEtc;
	}
	
	/**
	 * Gets the user comment.
	 *
	 * @return the user comment
	 */
	public String getUserComment() {
		return userComment;
	}

	/**
	 * Sets the user comment.
	 *
	 * @param userComment the new user comment
	 */
	public void setUserComment(String userComment) {
		this.userComment = userComment;
	}

	/**
	 * Gets the identification sub status android.
	 *
	 * @return the identification sub status android
	 */
	public String getIdentificationSubStatusAndroid() {
		return identificationSubStatusAndroid;
	}

	/**
	 * Sets the identification sub status android.
	 *
	 * @param identificationSubStatusAndroid the new identification sub status android
	 */
	public void setIdentificationSubStatusAndroid(String identificationSubStatusAndroid) {
		this.identificationSubStatusAndroid = identificationSubStatusAndroid;
	}

	/**
	 * Gets the comm ref div.
	 *
	 * @return the comm ref div
	 */
	public String getCommRefDiv() {
		return commRefDiv;
	}

	/**
	 * Sets the comm ref div.
	 *
	 * @param commRefDiv the new comm ref div
	 */
	public void setCommRefDiv(String commRefDiv) {
		this.commRefDiv = commRefDiv;
	}

	/**
	 * Gets the bin csv file id.
	 *
	 * @return the bin csv file id
	 */
	public String getBinCsvFileId() {
		return binCsvFileId;
	}

	/**
	 * Sets the bin csv file id.
	 *
	 * @param binCsvFileId the new bin csv file id
	 */
	public void setBinCsvFileId(String binCsvFileId) {
		this.binCsvFileId = binCsvFileId;
	}

	/**
	 * Gets the bin csv file.
	 *
	 * @return the bin csv file
	 */
	public List<T2File> getBinCsvFile() {
		return binCsvFile;
	}

	/**
	 * Sets the bin csv file.
	 *
	 * @param binCsvFile the new bin csv file
	 */
	public void setBinCsvFile(List<T2File> binCsvFile) {
		this.binCsvFile = binCsvFile;
	}

	/**
	 * Gets the identification sub status bin.
	 *
	 * @return the identification sub status bin
	 */
	public String getIdentificationSubStatusBin() {
		return identificationSubStatusBin;
	}

	/**
	 * Sets the identification sub status bin.
	 *
	 * @param identificationSubStatusBin the new identification sub status bin
	 */
	public void setIdentificationSubStatusBin(String identificationSubStatusBin) {
		this.identificationSubStatusBin = identificationSubStatusBin;
	}

	/**
	 * Gets the android flag.
	 *
	 * @return the android flag
	 */
	public String getAndroidFlag() {
		return androidFlag;
	}

	/**
	 * Sets the android flag.
	 *
	 * @param androidFlag the new android flag
	 */
	public void setAndroidFlag(String androidFlag) {
		this.androidFlag = androidFlag;
	}

	/**
	 * Gets the distribute reserved user.
	 *
	 * @return the distribute reserved user
	 */
	public String getDistributeReservedUser() {
		return distributeReservedUser;
	}

	/**
	 * Sets the distribute reserved user.
	 *
	 * @param distributeReservedUser the new distribute reserved user
	 */
	public void setDistributeReservedUser(String distributeReservedUser) {
		this.distributeReservedUser = distributeReservedUser;
	}

	/**
	 * Gets the distribute reserve flag.
	 *
	 * @return the distribute reserve flag
	 */
	public String getDistributeReserveFlag() {
		return distributeReserveFlag;
	}

	/**
	 * Sets the distribute reserve flag.
	 *
	 * @param distributeReserveFlag the new distribute reserve flag
	 */
	public void setDistributeReserveFlag(String distributeReserveFlag) {
		this.distributeReserveFlag = distributeReserveFlag;
	}

	/**
	 * Gets the network server flag.
	 *
	 * @return the network server flag
	 */
	public String getNetworkServerFlag() {
		return networkServerFlag;
	}

	/**
	 * Sets the network server flag.
	 *
	 * @param networkServerFlag the new network server flag
	 */
	public void setNetworkServerFlag(String networkServerFlag) {
		this.networkServerFlag = networkServerFlag;
	}

	/**
	 * Gets the readme file name.
	 *
	 * @return the readme file name
	 */
	public String getReadmeFileName() {
		return readmeFileName;
	}

	/**
	 * Sets the readme file name.
	 *
	 * @param readmeFileName the new readme file name
	 */
	public void setReadmeFileName(String readmeFileName) {
		this.readmeFileName = readmeFileName;
	}
	
	/**
	 * Gets the csv add file seq.
	 *
	 * @return the csv add file seq
	 */
	public List<T2File> getCsvFileSeq() {
		return csvFileSeq;
	}

	/**
	 * Sets the csv add file seq.
	 *
	 * @param csvFileSeq the new csv add file seq
	 */
	public void setCsvFileSeq(List<T2File> csvFileSeq) {
		this.csvFileSeq = csvFileSeq;
	}

	/**
	 * Gets the skip package flag.
	 *
	 * @return the skip package flag
	 */
	public String getSkipPackageFlag() {
		return skipPackageFlag;
	}

	/**
	 * Sets the skip package flag.
	 *
	 * @param skipPackageFlag the new skip package flag
	 */
	public void setSkipPackageFlag(String skipPackageFlag) {
		this.skipPackageFlag = skipPackageFlag;
	}

	/**
	 * Gets the bin binary file id.
	 *
	 * @return the bin binary file id
	 */
	public String getBinBinaryFileId() {
		return binBinaryFileId;
	}

	/**
	 * Sets the bin binary file id.
	 *
	 * @param binBinaryFileId the new bin binary file id
	 */
	public void setBinBinaryFileId(String binBinaryFileId) {
		this.binBinaryFileId = binBinaryFileId;
	}

	/**
	 * Gets the bin binary file.
	 *
	 * @return the bin binary file
	 */
	public List<T2File> getBinBinaryFile() {
		return binBinaryFile;
	}

	/**
	 * Sets the bin binary file.
	 *
	 * @param binBinaryFile the new bin binary file
	 */
	public void setBinBinaryFile(List<T2File> binBinaryFile) {
		this.binBinaryFile = binBinaryFile;
	}

	/**
	 * Gets the load from android project flag.
	 *
	 * @return the load from android project flag
	 */
	public String getLoadFromAndroidProjectFlag() {
		return loadFromAndroidProjectFlag;
	}

	/**
	 * Sets the load from android project flag.
	 *
	 * @param loadFromAndroidProjectFlag the new load from android project flag
	 */
	public void setLoadFromAndroidProjectFlag(String loadFromAndroidProjectFlag) {
		this.loadFromAndroidProjectFlag = loadFromAndroidProjectFlag;
	}

	/**
	 * Gets the copy flag.
	 *
	 * @return the copy flag
	 */
	public String getCopyFlag() {
		return copyFlag;
	}

	/**
	 * Sets the copy flag.
	 *
	 * @param copyFlag the new copy flag
	 */
	public void setCopyFlag(String copyFlag) {
		this.copyFlag = copyFlag;
	}

	/**
	 * Gets the without verify yn.
	 *
	 * @return the without verify yn
	 */
	public String getWithoutVerifyYn() {
		return withoutVerifyYn;
	}
	
	/**
	 * Sets the without verify yn.
	 *
	 * @param withoutVerifyYn the new without verify yn
	 */
	public void setWithoutVerifyYn(String withoutVerifyYn) {
		this.withoutVerifyYn = withoutVerifyYn;
	}

	/**
	 * Gets the creator nm.
	 *
	 * @return the creator nm
	 */
	public String getCreatorNm() {
		return creatorNm;
	}

	/**
	 * Sets the creator nm.
	 *
	 * @param creatorNm the new creator nm
	 */
	public void setCreatorNm(String creatorNm) {
		this.creatorNm = creatorNm;
	}
	
	/**
	 * Gets the vulnerability.
	 *
	 * @return the vulnerability
	 */
	public String getVulnerability() {
		return vulnerability;
	}

	/**
	 * Sets the vulnerability.
	 *
	 * @param vulnerability the new vulnerability
	 */
	public void setVulnerability(String vulnerability) {
		this.vulnerability = vulnerability;
	}

	/**
	 * Gets the distribution type of code dtl exp.
	 *
	 * @return the distribution type of code dtl exp
	 */
	public String getDistributionTypeOfCodeDtlExp() {
		return distributionTypeOfCodeDtlExp;
	}

	/**
	 * Sets the distribution type of code dtl exp.
	 *
	 * @param distributionTypeOfCodeDtlExp the new distribution type of code dtl exp
	 */
	public void setDistributionTypeOfCodeDtlExp(String distributionTypeOfCodeDtlExp) {
		this.distributionTypeOfCodeDtlExp = distributionTypeOfCodeDtlExp;
	}
	
	/**
	 * Gets the watcher list info.
	 *
	 * @return the watcher list info
	 */
	public List<String> getWatcherListInfo() {
		return watcherListInfo;
	}
	
	/**
	 * Sets the watcher list info.
	 *
	 * @param watcherListInfo the new watcher list info
	 */
	public void setWatcherListInfo(List<String> watcherListInfo) {
		this.watcherListInfo = watcherListInfo;
	}

	/**
	 * Gets the identification sub status bom.
	 *
	 * @return the identificationSubStatusBom
	 */
	public String getIdentificationSubStatusBom() {
		return identificationSubStatusBom;
	}

	/**
	 * Sets the identification sub status bom.
	 *
	 * @param identificationSubStatusBom the identificationSubStatusBom to set
	 */
	public void setIdentificationSubStatusBom(String identificationSubStatusBom) {
		this.identificationSubStatusBom = identificationSubStatusBom;
	}

	/**
	 * Gets the model delete list.
	 *
	 * @return the model delete list
	 */
	public List<Project> getModelDeleteList() {
		return modelDeleteList;
	}

	/**
	 * Sets the model delete list.
	 *
	 * @param modelDeleteList the new model delete list
	 */
	public void setModelDeleteList(List<Project> modelDeleteList) {
		this.modelDeleteList = modelDeleteList;
	}

	/**
	 * Gets the prj delete model json.
	 *
	 * @return the prj delete model json
	 */
	public String getPrjDeleteModelJson() {
		return prjDeleteModelJson;
	}

	/**
	 * Sets the prj delete model json.
	 *
	 * @param prjDeleteModelJson the new prj delete model json
	 */
	public void setPrjDeleteModelJson(String prjDeleteModelJson) {
		this.prjDeleteModelJson = prjDeleteModelJson;
	}

	/**
	 * Gets the osdd sync yn.
	 *
	 * @return the osdd sync yn
	 */
	public String getOsddSyncYn() {
		return osddSyncYn;
	}

	/**
	 * Sets the osdd sync yn.
	 *
	 * @param osddSyncYn the new osdd sync yn
	 */
	public void setOsddSyncYn(String osddSyncYn) {
		this.osddSyncYn = osddSyncYn;
	}

	/**
	 * Gets the osdd sync time.
	 *
	 * @return the osdd sync time
	 */
	public String getOsddSyncTime() {
		return osddSyncTime;
	}

	/**
	 * Sets the osdd sync time.
	 *
	 * @param osddSyncTime the new osdd sync time
	 */
	public void setOsddSyncTime(String osddSyncTime) {
		this.osddSyncTime = osddSyncTime;
	}

	/**
	 * Gets the act type.
	 *
	 * @return the act type
	 */
	public String getActType() {
		return actType;
	}

	/**
	 * Sets the act type.
	 *
	 * @param actType the new act type
	 */
	public void setActType(String actType) {
		this.actType = actType;
	}

	/**
	 * Gets the act cont.
	 *
	 * @return the act cont
	 */
	public String getActCont() {
		return actCont;
	}

	/**
	 * Sets the act cont.
	 *
	 * @param actCont the new act cont
	 */
	public void setActCont(String actCont) {
		this.actCont = actCont;
	}

	/**
	 * Gets the category nm.
	 *
	 * @return the category nm
	 */
	public String getCategoryNm() {
		return categoryNm;
	}

	/**
	 * Sets the category nm.
	 *
	 * @param categoryNm the new category nm
	 */
	public void setCategoryNm(String categoryNm) {
		this.categoryNm = categoryNm;
	}

	/**
	 * Gets the distribute check yn.
	 *
	 * @return the distribute check yn
	 */
	public String getDistributeCheckYn() {
		return distributeCheckYn;
	}

	/**
	 * Sets the distribute check yn.
	 *
	 * @param distributeCheckYn the new distribute check yn
	 */
	public void setDistributeCheckYn(String distributeCheckYn) {
		this.distributeCheckYn = distributeCheckYn;
	}

	/**
	 * Gets the sent osdd del mail.
	 *
	 * @return the sent osdd del mail
	 */
	public String getSentOsddDelMail() {
		return sentOsddDelMail;
	}

	/**
	 * Sets the sent osdd del mail.
	 *
	 * @param sentOsddDelMail the new sent osdd del mail
	 */
	public void setSentOsddDelMail(String sentOsddDelMail) {
		this.sentOsddDelMail = sentOsddDelMail;
	}

	/**
	 * Gets the sent osdd diff file mail.
	 *
	 * @return the sent osdd diff file mail
	 */
	public String getSentOsddDiffFileMail() {
		return sentOsddDiffFileMail;
	}

	/**
	 * Sets the sent osdd diff file mail.
	 *
	 * @param sentOsddDiffFileMail the new sent osdd diff file mail
	 */
	public void setSentOsddDiffFileMail(String sentOsddDiffFileMail) {
		this.sentOsddDiffFileMail = sentOsddDiffFileMail;
	}

	/**
	 * Gets the need package file reset.
	 *
	 * @return the need package file reset
	 */
	public String getNeedPackageFileReset() {
		return needPackageFileReset;
	}

	/**
	 * Sets the need package file reset.
	 *
	 * @param needPackageFileReset the new need package file reset
	 */
	public void setNeedPackageFileReset(String needPackageFileReset) {
		this.needPackageFileReset = needPackageFileReset;
	}
	
	 /**
 	 * Gets the reviewer name.
 	 *
 	 * @return the reviewer name
 	 */
 	public String getReviewerName() {
	        return reviewerName;
	    }

	    /**
    	 * Sets the reviewer name.
    	 *
    	 * @param reviewerName the new reviewer name
    	 */
    	public void setReviewerName(String reviewerName) {
	        this.reviewerName = reviewerName;
	    }
	
	/**
	 * Gets the use custom notice yn.
	 *
	 * @return the use custom notice yn
	 */
	public String getUseCustomNoticeYn() {
		return useCustomNoticeYn;
	}

	/**
	 * Sets the use custom notice yn.
	 *
	 * @param useCustomNoticeYn the new use custom notice yn
	 */
	public void setUseCustomNoticeYn(String useCustomNoticeYn) {
		this.useCustomNoticeYn = useCustomNoticeYn;
	}

	/**
	 * Gets the allow download bit flag.
	 *
	 * @return the allow download bit flag
	 */
	public int getAllowDownloadBitFlag() {
		return allowDownloadBitFlag;
	}

	/**
	 * Sets the allow download bit flag.
	 *
	 * @param allowDownloadBitFlag the new allow download bit flag
	 */
	public void setAllowDownloadBitFlag(int allowDownloadBitFlag) {
		this.allowDownloadBitFlag = allowDownloadBitFlag;
	}

	/**
	 * Gets the allow download notice HTML yn.
	 *
	 * @return the allow download notice HTML yn
	 */
	public String getAllowDownloadNoticeHTMLYn() {
		return allowDownloadNoticeHTMLYn;
	}

	/**
	 * Sets the allow download notice HTML yn.
	 *
	 * @param allowDownloadNoticeHTMLYn the new allow download notice HTML yn
	 */
	public void setAllowDownloadNoticeHTMLYn(String allowDownloadNoticeHTMLYn) {
		this.allowDownloadNoticeHTMLYn = allowDownloadNoticeHTMLYn;
	}

	/**
	 * Gets the allow download notice text yn.
	 *
	 * @return the allow download notice text yn
	 */
	public String getAllowDownloadNoticeTextYn() {
		return allowDownloadNoticeTextYn;
	}

	/**
	 * Sets the allow download notice text yn.
	 *
	 * @param allowDownloadNoticeTextYn the new allow download notice text yn
	 */
	public void setAllowDownloadNoticeTextYn(String allowDownloadNoticeTextYn) {
		this.allowDownloadNoticeTextYn = allowDownloadNoticeTextYn;
	}

	/**
	 * Gets the allow download SPDX sheet yn.
	 *
	 * @return the allow download SPDX sheet yn
	 */
	public String getAllowDownloadSPDXSheetYn() {
		return allowDownloadSPDXSheetYn;
	}

	/**
	 * Sets the allow download SPDX sheet yn.
	 *
	 * @param allowDownloadSPDXSheetYn the new allow download SPDX sheet yn
	 */
	public void setAllowDownloadSPDXSheetYn(String allowDownloadSPDXSheetYn) {
		this.allowDownloadSPDXSheetYn = allowDownloadSPDXSheetYn;
	}

	/**
	 * Gets the allow download simple HTML yn.
	 *
	 * @return the allow download simple HTML yn
	 */
	public String getAllowDownloadSimpleHTMLYn() {
		return allowDownloadSimpleHTMLYn;
	}

	/**
	 * Sets the allow download simple HTML yn.
	 *
	 * @param allowDownloadSimpleHTMLYn the new allow download simple HTML yn
	 */
	public void setAllowDownloadSimpleHTMLYn(String allowDownloadSimpleHTMLYn) {
		this.allowDownloadSimpleHTMLYn = allowDownloadSimpleHTMLYn;
	}

	/**
	 * Gets the allow download simple text yn.
	 *
	 * @return the allow download simple text yn
	 */
	public String getAllowDownloadSimpleTextYn() {
		return allowDownloadSimpleTextYn;
	}

	/**
	 * Sets the allow download simple text yn.
	 *
	 * @param allowDownloadSimpleTextYn the new allow download simple text yn
	 */
	public void setAllowDownloadSimpleTextYn(String allowDownloadSimpleTextYn) {
		this.allowDownloadSimpleTextYn = allowDownloadSimpleTextYn;
	}

	/**
	 * Gets the allow download SPDX rdf yn.
	 *
	 * @return the allow download SPDX rdf yn
	 */
	public String getAllowDownloadSPDXRdfYn() {
		return allowDownloadSPDXRdfYn;
	}

	/**
	 * Sets the allow download SPDX rdf yn.
	 *
	 * @param allowDownloadSPDXRdfYn the new allow download SPDX rdf yn
	 */
	public void setAllowDownloadSPDXRdfYn(String allowDownloadSPDXRdfYn) {
		this.allowDownloadSPDXRdfYn = allowDownloadSPDXRdfYn;
	}

	/**
	 * Gets the allow download SPDX tag yn.
	 *
	 * @return the allow download SPDX tag yn
	 */
	public String getAllowDownloadSPDXTagYn() {
		return allowDownloadSPDXTagYn;
	}

	/**
	 * Sets the allow download SPDX tag yn.
	 *
	 * @param allowDownloadSPDXTagYn the new allow download SPDX tag yn
	 */
	public void setAllowDownloadSPDXTagYn(String allowDownloadSPDXTagYn) {
		this.allowDownloadSPDXTagYn = allowDownloadSPDXTagYn;
	}

	/**
	 * Gets the allow download SPDX json yn.
	 *
	 * @return the allow download SPDX json yn
	 */
	public String getAllowDownloadSPDXJsonYn() {
		return allowDownloadSPDXJsonYn;
	}

	/**
	 * Sets the allow download SPDX json yn.
	 *
	 * @param allowDownloadSPDXJsonYn the new allow download SPDX json yn
	 */
	public void setAllowDownloadSPDXJsonYn(String allowDownloadSPDXJsonYn) {
		this.allowDownloadSPDXJsonYn = allowDownloadSPDXJsonYn;
	}

	/**
	 * Gets the allow download SPDX yaml yn.
	 *
	 * @return the allow download SPDX yaml yn
	 */
	public String getAllowDownloadSPDXYamlYn() {
		return allowDownloadSPDXYamlYn;
	}

	/**
	 * Sets the allow download SPDX yaml yn.
	 *
	 * @param allowDownloadSPDXYamlYn the new allow download SPDX yaml yn
	 */
	public void setAllowDownloadSPDXYamlYn(String allowDownloadSPDXYamlYn) {
		this.allowDownloadSPDXYamlYn = allowDownloadSPDXYamlYn;
	}

	/**
	 * Gets the distribute deploy user.
	 *
	 * @return the distribute deploy user
	 */
	public String getDistributeDeployUser() {
		return distributeDeployUser;
	}

	/**
	 * Sets the distribute deploy user.
	 *
	 * @param distributeDeployUser the new distribute deploy user
	 */
	public void setDistributeDeployUser(String distributeDeployUser) {
		this.distributeDeployUser = distributeDeployUser;
	}
	
	public String getBeforeDistributeDeployUser() {
		return beforeDistributeDeployUser;
	}

	public void setBeforeDistributeDeployUser(String beforeDistributeDeployUser) {
		this.beforeDistributeDeployUser = beforeDistributeDeployUser;
	}

	/**
	 * Gets the distribute rejector.
	 *
	 * @return the distribute rejector
	 */
	public String getDistributeRejector() {
		return distributeRejector;
	}

	/**
	 * Sets the distribute rejector.
	 *
	 * @param distributeRejector the new distribute rejector
	 */
	public void setDistributeRejector(String distributeRejector) {
		this.distributeRejector = distributeRejector;
	}

	/**
	 * Gets the distribute rejected time.
	 *
	 * @return the distribute rejected time
	 */
	public String getDistributeRejectedTime() {
		return distributeRejectedTime;
	}

	/**
	 * Sets the distribute rejected time.
	 *
	 * @param distributeRejectedTime the new distribute rejected time
	 */
	public void setDistributeRejectedTime(String distributeRejectedTime) {
		this.distributeRejectedTime = distributeRejectedTime;
	}

	/**
	 * Gets the osdd notice file name.
	 *
	 * @return the osdd notice file name
	 */
	public String getOsddNoticeFileName() {
		return osddNoticeFileName;
	}

	/**
	 * Sets the osdd notice file name.
	 *
	 * @param osddNoticeFileName the new osdd notice file name
	 */
	public void setOsddNoticeFileName(String osddNoticeFileName) {
		this.osddNoticeFileName = osddNoticeFileName;
	}
	
	/**
	 * Gets the osdd notice file E-TAG.
	 *
	 * @return the osdd notice file E-TAG.
	 */
	public String getOsddNoticeFileEtag() {
		return osddNoticeFileEtag;
	}

	/**
	 * Sets the osdd notice file E-TAG.
	 *
	 * @param osddNoticeFileName the new osdd notice file E-TAG.
	 */
	public void setOsddNoticeFileEtag(String osddNoticeFileEtag) {
		this.osddNoticeFileEtag = osddNoticeFileEtag;
	}

	/**
	 * Gets the osdd source file name.
	 *
	 * @return the osdd source file name
	 */
	public String getOsddSourceFileName() {
		return osddSourceFileName;
	}
	
	/**
	 * Sets the osdd source file name.
	 *
	 * @param osddSourceFileName the new osdd source file name
	 */
	public void setOsddSourceFileName(String osddSourceFileName) {
		this.osddSourceFileName = osddSourceFileName;
	}
	
	public String getOsddSourceFileName2() {
		return osddSourceFileName2;
	}

	public void setOsddSourceFileName2(String osddSourceFileName2) {
		this.osddSourceFileName2 = osddSourceFileName2;
	}

	public String getOsddSourceFileName3() {
		return osddSourceFileName3;
	}

	public void setOsddSourceFileName3(String osddSourceFileName3) {
		this.osddSourceFileName3 = osddSourceFileName3;
	}
	
	public String getOsddSourceFileEtag() {
		return osddSourceFileEtag;
	}

	public void setOsddSourceFileEtag(String osddSourceFileEtag) {
		this.osddSourceFileEtag = osddSourceFileEtag;
	}

	public String getOsddSourceFileEtag2() {
		return osddSourceFileEtag2;
	}

	public void setOsddSourceFileEtag2(String osddSourceFileEtag2) {
		this.osddSourceFileEtag2 = osddSourceFileEtag2;
	}

	public String getOsddSourceFileEtag3() {
		return osddSourceFileEtag3;
	}

	public void setOsddSourceFileEtag3(String osddSourceFileEtag3) {
		this.osddSourceFileEtag3 = osddSourceFileEtag3;
	}
	/**
	 * Gets the notice text file id.
	 *
	 * @return the notice text file id
	 */
	public String getNoticeTextFileId() {
		return noticeTextFileId;
	}

	/**
	 * Sets the notice text file id.
	 *
	 * @param noticeTextFileId the new notice text file id
	 */
	public void setNoticeTextFileId(String noticeTextFileId) {
		this.noticeTextFileId = noticeTextFileId;
	}

	/**
	 * Gets the simple html file id.
	 *
	 * @return the simple html file id
	 */
	public String getSimpleHtmlFileId() {
		return simpleHtmlFileId;
	}

	/**
	 * Sets the simple html file id.
	 *
	 * @param simpleHtmlFileId the new simple html file id
	 */
	public void setSimpleHtmlFileId(String simpleHtmlFileId) {
		this.simpleHtmlFileId = simpleHtmlFileId;
	}

	/**
	 * Gets the simple text file id.
	 *
	 * @return the simple text file id
	 */
	public String getSimpleTextFileId() {
		return simpleTextFileId;
	}

	/**
	 * Sets the simple text file id.
	 *
	 * @param simpleTextFileId the new simple text file id
	 */
	public void setSimpleTextFileId(String simpleTextFileId) {
		this.simpleTextFileId = simpleTextFileId;
	}

	/**
	 * Gets the spdx sheet file id.
	 *
	 * @return the spdx sheet file id
	 */
	public String getSpdxSheetFileId() {
		return spdxSheetFileId;
	}

	/**
	 * Sets the spdx sheet file id.
	 *
	 * @param spdxSheetFileId the new spdx sheet file id
	 */
	public void setSpdxSheetFileId(String spdxSheetFileId) {
		this.spdxSheetFileId = spdxSheetFileId;
	}

	/**
	 * Gets the spdx rdf file id.
	 *
	 * @return the spdx rdf file id
	 */
	public String getSpdxRdfFileId() {
		return spdxRdfFileId;
	}

	/**
	 * Sets the spdx rdf file id.
	 *
	 * @param spdxRdfFileId the new spdx rdf file id
	 */
	public void setSpdxRdfFileId(String spdxRdfFileId) {
		this.spdxRdfFileId = spdxRdfFileId;
	}

	/**
	 * Gets the spdx tag file id.
	 *
	 * @return the spdx tag file id
	 */
	public String getSpdxTagFileId() {
		return spdxTagFileId;
	}

	/**
	 * Sets the spdx tag file id.
	 *
	 * @param spdxTagFileId the new spdx tag file id
	 */
	public void setSpdxTagFileId(String spdxTagFileId) {
		this.spdxTagFileId = spdxTagFileId;
	}

	/**
	 * Gets the spdx json file id.
	 *
	 * @return the spdx json file id
	 */
	public String getSpdxJsonFileId() {
		return spdxJsonFileId;
	}

	/**
	 * Sets the spdx json file id.
	 *
	 * @param spdxJsonFileId the new spdx json file id
	 */
	public void setSpdxJsonFileId(String spdxJsonFileId) {
		this.spdxJsonFileId = spdxJsonFileId;
	}

	/**
	 * Gets the spdx yaml file id.
	 *
	 * @return the spdx yaml file id
	 */
	public String getSpdxYamlFileId() {
		return spdxYamlFileId;
	}

	/**
	 * Sets the spdx yaml file id.
	 *
	 * @param spdxYamlFileId the new spdx yaml file id
	 */
	public void setSpdxYamlFileId(String spdxYamlFileId) {
		this.spdxYamlFileId = spdxYamlFileId;
	}

	/**
	 * Gets the zip file id.
	 *
	 * @return the zip file id
	 */
	public String getZipFileId() {
		return zipFileId;
	}

	/**
	 * Sets the zip file id.
	 *
	 * @param zipFileId the new zip file id
	 */
	public void setZipFileId(String zipFileId) {
		this.zipFileId = zipFileId;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getPrjDivisionId() {
		return prjDivisionId;
	}

	public void setPrjDivisionId(String prjDivisionId) {
		this.prjDivisionId = prjDivisionId;
	}

	public String[] getOssNickNames() {
		return ossNickNames != null ? ossNickNames.clone() : null;
	}

	public void setOssNickNames(String[] ossNickNames) {
		this.ossNickNames = ossNickNames != null ?
			ossNickNames.clone() : null;
	}

	public String getStatuses() {
		return statuses;
	}

	public void setStatuses(String statuses) {
		this.statuses = statuses;
	}

	public String[] getArrStatuses() {
		return arrStatuses != null ? arrStatuses.clone() : null;
	}

	public void setArrStatuses(String[] arrStatuses) {
		this.arrStatuses = arrStatuses != null ?
			arrStatuses.clone() : null;
	}
	
	public String getPublicYn() {
		return publicYn;
	}

	public void setPublicYn(String publicYn) {
		this.publicYn = publicYn;
	}
	
	public String getViewOnlyFlag() {
		return viewOnlyFlag;
	}

	public void setViewOnlyFlag(String viewOnlyFlag) {
		this.viewOnlyFlag = viewOnlyFlag;
	}

	public String getVersionMatchedFlag() {
		return versionMatchedFlag;
	}

	public void setVersionMatchedFlag(String versionMatchedFlag) {
		this.versionMatchedFlag = versionMatchedFlag;
	}

	public String getResetDistributionStatus() {
		return resetDistributionStatus;
	}

	public void setResetDistributionStatus(String resetDistributionStatus) {
		this.resetDistributionStatus = resetDistributionStatus;
	}
	
	/* 2018-07-19 choye 추가  */
	public String getCommId() {
		return commId;
	}

	public void setCommId(String commId) {
		this.commId = commId;
	}
	
	public String getStatusRequestYn() {
		return statusRequestYn;
	}

	public void setStatusRequestYn(String statusRequestYn) {
		this.statusRequestYn = statusRequestYn;
	}
	
	/* 2018-07-27 choye 추가  */
	public String getDelOsdd() {
		return delOsdd;
	}

	public void setDelOsdd(String delOsdd) {
		this.delOsdd = delOsdd;
	}

	public String getOssCount() {
		return ossCount;
	}

	public void setOssCount(String ossCount) {
		this.ossCount = ossCount;
	}

	public String getDeleteMemo() {
		return deleteMemo;
	}

	public void setDeleteMemo(String deleteMemo) {
		this.deleteMemo = deleteMemo;
	}

	public String getIgnoreBinaryDbFlag() {
		return ignoreBinaryDbFlag;
	}

	public void setIgnoreBinaryDbFlag(String ignoreBinaryDbFlag) {
		this.ignoreBinaryDbFlag = ignoreBinaryDbFlag;
	}
	
	public String getListKind() {
		return listKind;
	}

	public void setListKind(String listKind) {
		this.listKind = listKind;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getSchBinaryName() {
		return schBinaryName;
	}

	public void setSchBinaryName(String schBinaryName) {
		this.schBinaryName = schBinaryName;
	}
	
	public String getComponentCount() {
		return componentCount;
	}

	public void setComponentCount(String componentCount) {
		this.componentCount = componentCount;
	}
	
	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	
	public String getBomCnt() {
		return BomCnt;
	}

	public void setBomCnt(String bomCnt) {
		BomCnt = bomCnt;
	}

	public String getDiscloseCnt() {
		return discloseCnt;
	}

	public void setDiscloseCnt(String discloseCnt) {
		this.discloseCnt = discloseCnt;
	}
	
	
	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
	public String getStatusVerifyYn() {
		return statusVerifyYn;
	}

	public void setStatusVerifyYn(String statusVerifyYn) {
		this.statusVerifyYn = statusVerifyYn;
	}

	public String getProductGroup() {
		return productGroup;
	}

	public void setProductGroup(String productGroup) {
		this.productGroup = productGroup;
	}

	public List<String> getProductGroups() {
		return productGroups;
	}

	public void setProductGroups(List<String> productGroups) {
		this.productGroups = productGroups;
	}

	public String getPackageFileId2() {
		return packageFileId2;
	}

	public void setPackageFileId2(String packageFileId2) {
		this.packageFileId2 = packageFileId2;
	}

	public String getPackageFileId3() {
		return packageFileId3;
	}

	public void setPackageFileId3(String packageFileId3) {
		this.packageFileId3 = packageFileId3;
	}

	public List<T2File> getPackageFile2() {
		return packageFile2;
	}

	public void setPackageFile2(List<T2File> packageFile2) {
		this.packageFile2 = packageFile2;
	}

	public List<T2File> getPackageFile3() {
		return packageFile3;
	}

	public void setPackageFile3(List<T2File> packageFile3) {
		this.packageFile3 = packageFile3;
	}

	public T2File getPackageFileInfo2() {
		return packageFileInfo2;
	}

	public void setPackageFileInfo2(T2File packageFileInfo2) {
		this.packageFileInfo2 = packageFileInfo2;
	}

	public T2File getPackageFileInfo3() {
		return packageFileInfo3;
	}

	public void setPackageFileInfo3(T2File packageFileInfo3) {
		this.packageFileInfo3 = packageFileInfo3;
	}
	
	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getReuseKeyword() {
		return reuseKeyword;
	}

	public void setReuseKeyword(String reuseKeyword) {
		this.reuseKeyword = reuseKeyword;
	}
	
	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getOssAnalysisStatus() {
		return ossAnalysisStatus;
	}

	public void setOssAnalysisStatus(String ossAnalysisStatus) {
		this.ossAnalysisStatus = ossAnalysisStatus;
	}

	public String getAnalysisStartDate() {
		return analysisStartDate;
	}

	public void setAnalysisStartDate(String analysisStartDate) {
		this.analysisStartDate = analysisStartDate;
	}

	public String getModelFlag() {
		return modelFlag;
	}

	public void setModelFlag(String modelFlag) {
		this.modelFlag = modelFlag;
	}
	
	public String getChangeStatusFlag() {
		return changeStatusFlag;
	}

	public void setChangeStatusFlag(String changeStatusFlag) {
		this.changeStatusFlag = changeStatusFlag;
	}
	
	public String getSrcAndroidNoticeXmlId() {
		return srcAndroidNoticeXmlId;
	}

	public void setSrcAndroidNoticeXmlId(String srcAndroidNoticeXmlId) {
		this.srcAndroidNoticeXmlId = srcAndroidNoticeXmlId;
	}

	public String getNetworkServerType() {
		return networkServerType;
	}

	public void setNetworkServerType(String networkServerType) {
		this.networkServerType = networkServerType;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	public String getReProcessDistributionFlag() {
		return reProcessDistributionFlag;
	}

	public void setReProcessDistributionFlag(String reProcessDistributionFlag) {
		this.reProcessDistributionFlag = reProcessDistributionFlag;
	}
	
	public String getChangedNoticeYn() {
		return changedNoticeYn;
	}

	public void setChangedNoticeYn(String changedNoticeYn) {
		this.changedNoticeYn = changedNoticeYn;
	}

	public String getOssReportFlag() {
		return ossReportFlag;
	}

	public void setOssReportFlag(String ossReportFlag) {
		this.ossReportFlag = ossReportFlag;
	}
	
	public String getModelListAppendFlag() {
		return modelListAppendFlag;
	}

	public void setModelListAppendFlag(String modelListAppendFlag) {
		this.modelListAppendFlag = modelListAppendFlag;
	}
	
	public String getModelSeq() {
		return modelSeq;
	}

	public void setModelSeq(String modelSeq) {
		this.modelSeq = modelSeq;
	}

	public String getOssNameMergeFlag() {
		return ossNameMergeFlag;
	}

	public void setOssNameMergeFlag(String ossNameMergeFlag) {
		this.ossNameMergeFlag = ossNameMergeFlag;
	}

	public String getSrcCsvFileFlag() {
		return srcCsvFileFlag;
	}

	public void setSrcCsvFileFlag(String srcCsvFileFlag) {
		this.srcCsvFileFlag = srcCsvFileFlag;
	}

	public String getBinCsvFileFlag() {
		return binCsvFileFlag;
	}

	public void setBinCsvFileFlag(String binCsvFileFlag) {
		this.binCsvFileFlag = binCsvFileFlag;
	}

	public String getBinBinaryFileFlag() {
		return binBinaryFileFlag;
	}

	public void setBinBinaryFileFlag(String binBinaryFileFlag) {
		this.binBinaryFileFlag = binBinaryFileFlag;
	}

	public String getSrcAndroidCsvFileFlag() {
		return srcAndroidCsvFileFlag;
	}

	public void setSrcAndroidCsvFileFlag(String srcAndroidCsvFileFlag) {
		this.srcAndroidCsvFileFlag = srcAndroidCsvFileFlag;
	}

	public String getSrcAndroidNoticeFileFlag() {
		return srcAndroidNoticeFileFlag;
	}

	public void setSrcAndroidNoticeFileFlag(String srcAndroidNoticeFileFlag) {
		this.srcAndroidNoticeFileFlag = srcAndroidNoticeFileFlag;
	}

	public String getSrcAndroidNoticeXmlFileFlag() {
		return srcAndroidNoticeXmlFileFlag;
	}

	public void setSrcAndroidNoticeXmlFileFlag(String srcAndroidNoticeXmlFileFlag) {
		this.srcAndroidNoticeXmlFileFlag = srcAndroidNoticeXmlFileFlag;
	}

	public String getSrcAndroidResultFileFlag() {
		return srcAndroidResultFileFlag;
	}

	public void setSrcAndroidResultFileFlag(String srcAndroidResultFileFlag) {
		this.srcAndroidResultFileFlag = srcAndroidResultFileFlag;
	}

	public String getIdentificationStatusConfFlag() {
		return identificationStatusConfFlag;
	}

	public void setIdentificationStatusConfFlag(String identificationStatusConfFlag) {
		this.identificationStatusConfFlag = identificationStatusConfFlag;
	}

	public String getVerificationStatusConfFlag() {
		return verificationStatusConfFlag;
	}

	public void setVerificationStatusConfFlag(String verificationStatusConfFlag) {
		this.verificationStatusConfFlag = verificationStatusConfFlag;
	}

	public int getPermission() {
		return permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}

	public int getStatusPermission() {
		return statusPermission;
	}

	public void setStatusPermission(int statusPermission) {
		this.statusPermission = statusPermission;
	}

	public String getOssNameTemp() {
		return ossNameTemp;
	}

	public void setOssNameTemp(String ossNameTemp) {
		this.ossNameTemp = ossNameTemp;
	}

	public String getDeleteOsddFlag() {
		return deleteOsddFlag;
	}

	public void setDeleteOsddFlag(String deleteOsddFlag) {
		this.deleteOsddFlag = deleteOsddFlag;
	}

	public String getSecCode() {
		return secCode;
	}

	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public String getIdentificationSubStatusDep() {
		return identificationSubStatusDep;
	}

	public void setIdentificationSubStatusDep(String identificationSubStatusDep) {
		this.identificationSubStatusDep = identificationSubStatusDep;
	}

	public String getDepCsvFileId() {
		return depCsvFileId;
	}

	public void setDepCsvFileId(String depCsvFileId) {
		this.depCsvFileId = depCsvFileId;
	}

	public List<T2File> getDepCsvFile() {
		return depCsvFile;
	}

	public void setDepCsvFile(List<T2File> depCsvFile) {
		this.depCsvFile = depCsvFile;
	}

	public String getDepCsvFileFlag() {
		return depCsvFileFlag;
	}

	public void setDepCsvFileFlag(String depCsvFileFlag) {
		this.depCsvFileFlag = depCsvFileFlag;
	}

	public String getAllowDownloadCDXJsonYn() {
		return allowDownloadCDXJsonYn;
	}

	public void setAllowDownloadCDXJsonYn(String allowDownloadCDXJsonYn) {
		this.allowDownloadCDXJsonYn = allowDownloadCDXJsonYn;
	}

	public String getAllowDownloadCDXXmlYn() {
		return allowDownloadCDXXmlYn;
	}

	public void setAllowDownloadCDXXmlYn(String allowDownloadCDXXmlYn) {
		this.allowDownloadCDXXmlYn = allowDownloadCDXXmlYn;
	}

	public String getCdxJsonFileId() {
		return cdxJsonFileId;
	}

	public void setCdxJsonFileId(String cdxJsonFileId) {
		this.cdxJsonFileId = cdxJsonFileId;
	}

	public String getCdxXmlFileId() {
		return cdxXmlFileId;
	}

	public void setCdxXmlFileId(String cdxXmlFileId) {
		this.cdxXmlFileId = cdxXmlFileId;
	}

	public Float getStandardScore() {
		return standardScore;
	}

	public void setStandardScore(Float standardScore) {
		this.standardScore = standardScore;
	}
}
