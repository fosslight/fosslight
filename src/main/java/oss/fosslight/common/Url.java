/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

public final class Url {
	
	/** The Constant TILES_ROOT. */
	public static final String TILES = "tiles";
	public static final String TILES_ROOT = "tiles/admin";
	public static final String TILES_AJAX_ROOT = "tiles/ajax";
	
	public static final class SESSION {
		public static final String PATH = "/session";
		
		public static final String LOGIN = PATH + "/login";
		public static final String LOGIN_JSP = TILES + PATH + "/login";
		
		public static final String LOGIN_EXPIRED = PATH + "/loginExpired";
		public static final String SESSION_SAVE_KEY_VAL = PATH + "KeyValSave/{sesKey}/**";
	}
	
	/**
	 *  관리자 메인 화면.
	 */
	public static final class MAIN {
		public static final String PATH = "/main";
		
		/**  메인 페이지: {@value #INDEX}. */
		public static final String INDEX_EMPTY             = "/";
		public static final String INDEX             	   = "/index";
		public static final String INDEX_JSP			   = TILES + PATH + "/index";
	}
	
	public static final class AUTH {
		public static final String LOGIN	= "";
		public static final String LOGIN_PROC	= "";
		public static final String LOGOUT	= "/";
	}
	
	public static final class DASHBOARD {
		public static final String PATH = "/dashboard";
		
		public static final String LIST =  PATH + "/list";
		public static final String LIST_JSP = TILES_ROOT + PATH + "/list";
		
		public static final String JOBSLIST = PATH + "/jobsListAjax";
		public static final String COMMENTLIST = PATH + "/commentsListAjax";
		public static final String OSSLIST = PATH + "/ossListAjax";
		public static final String LICENSELIST = PATH + "/licenseListAjax";
		public static final String READCONFIRM_ALL = PATH + "/readConfirmAll";
	}
	
	public static final class LICENSE {
		public static final String PATH = "/license";
		
		public static final String LIST =  PATH + "/list";
		public static final String LIST_JSP = TILES_ROOT + PATH + "/list";
		
		public static final String EDIT =  PATH + "/edit";
		public static final String EDIT_JSP = TILES_ROOT + PATH + "/edit";
		public static final String EDIT_ID =  PATH + "/edit/{licenseId}";
		
		public static final String LICENSE_VIEW_JSP = TILES_ROOT + PATH + "/view";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		
		public static final String VALIDATION = PATH + "/validation";
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String DEL_AJAX = PATH + "/delAjax";
		
		public static final String SAVE_COMMENT = PATH + "/saveComment";
		public static final String DELETE_COMMENT = PATH + "/deleteComment";
		
		public static final String LICENSE_TEXT = PATH + "/getLicenseText";
		
		public static final String AUTOCOMPLETE_AJAX = PATH + "/autoCompleteAjax";
		
		public static final String LICENSE_ID = PATH + "/getLicenseId";
	}
	
	public static final class EXCELDOWNLOAD {
		public static final String PATH = "/exceldownload";
		
		public static final String EXCEL_POST = PATH + "/getExcelPost";
		public static final String FILE = PATH + "/getFile";
		public static final String EXCEL_POST_OSS = PATH + "/getExcelPostOss";
		public static final String CHART_EXCEL = PATH + "/getChartExcel";
	}
	
	public static final class COMMENT {
		public static final String PATH = "/comment";
		
		public static final String COMMENT_LIST = PATH + "/getCommentList";
		public static final String MORE_COMMENT_LIST = PATH + "/getMoreCommentList";
		public static final String COMMENT_LIST_JSP = TILES_ROOT + PATH + "/list";
		
		public static final String POPUP = PATH + "/popup/{rDiv}/{rId}";
		public static final String POPUP_JSP  = TILES_ROOT + PATH + "/popup";
		
		public static final String DELETE_COMMENT = PATH + "/deleteComment";
		public static final String UPDATE_COMMENT = PATH + "/updateComment";
		
		public static final String COMMENT_INFO_ID = PATH + "/getCommentInfo/{commId}";
		public static final String DIV_COMMENT_LIST = PATH + "/getDivCommentList";
	}
	
	public static final class PROCESSGUIDE {
		public static final String PATH = "/system/processGuide";
		
		public static final String PAGE = PATH + "";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		
		public static final String PROCESS_GUIDE = PATH + "/getProcessGuide";
	}
	
	public static final class OSS {
		public static final String PATH = "/oss";
		
		public static final String LIST = PATH + "/list";
		public static final String LIST_JSP = TILES_ROOT + PATH + "/list";
		public static final String LIST_AJAX = PATH + "/listAjax";
		
		public static final String EDIT = PATH + "/edit";
		public static final String EDIT_JSP = TILES_ROOT + PATH + "/edit";
		public static final String EDIT_ID =  PATH + "/edit/{ossId}";
		
		public static final String VIEW_JSP = TILES_ROOT + PATH + "/view";
		
		public static final String AUTOCOMPLETE_AJAX = PATH + "/autoCompleteAjax";
		
		public static final String POPUPLIST_ID = PATH + "/ossPopupList/{ossId}";
		public static final String COPY_ID = PATH + "/copy/{ossId}";
		
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String DEL_AJAX = PATH + "/delAjax";
		public static final String DEL_OSS_VERSION_MERGE_AJAX = PATH +  "/delOssWithVersionMeregeAjax";
		public static final String URL_DUPLICATE_VALIDATION = PATH + "/urlDuplicateValidation";
		public static final String VALIDATION = PATH + "/validation";
		public static final String CHECK_EXIST_OSS_CONF = PATH + "/checkExistOssConf";
		public static final String CHECK_VD_DIFF = PATH + "/checkVdiff";
		public static final String SAVE_COMMENT = PATH + "/saveComment";
		public static final String DELTE_COMMENT = PATH + "/deleteComment";
		public static final String OSS_MERGE_CHECK_LIST = PATH + "/ossMergeCheckList/{ossId}/{newOssId}";
		
		public static final String SAVE_SESSION_OSS_INFO = PATH + "/saveSessionOssInfo";
		
		public static final String OSS_LIST_BY_NAME = PATH + "/getOssListByName";
		
		public static final String OSS_BULK_REG = PATH + "/ossBulkReg";
		public static final String OSS_BULK_REG_JSP = TILES_ROOT + PATH + "/ossBulkReg";
		public static final String BULK_REG_AJAX = PATH+"/bulkRegAjax";
		//public static final String OSS_BULK_REG_AJAX = PATH + "/bulkRegAjax";
		public static final String OSS_BULK_REG_AJAX = PATH + "/bulkRegAjax";
		public static final String CSV_FILE = PATH + "/csvFile";
		//public static final String OSS_BULK_REG_AJAX = PATH + "/getOssBulkRegAjax";
		public static final String SAVE_OSS_BULK_REG = PATH + "/saveOssBulkReg";
		
		public static final String OSS_POPUP = PATH + "/osspopup";
		public static final String OSS_POPUP_JSP = TILES_ROOT + PATH + "/osspopup";
		
		public static final String OSS_DETAIL_VIEW_AJAX = PATH + "/ossDetailViewAjax";
		public static final String OSS_DETAILS_VIEW_AJAX_JSP = TILES_AJAX_ROOT + PATH + "/ossDetailview";
		
		public static final String CHECK_EXISTS_OSS_BY_NAME = PATH + "/checkExistsOssByname";
		
		public static final String CHECK_OSS_NAME = PATH + "/checkOssName";
		public static final String CHECK_OSS_NAME_JSP = TILES_ROOT + PATH + "/checkOssNamepopup";
		
		public static final String CHECK_OSS_NAME_AJAX = PATH + "/getCheckOssNameAjax/{targetName}";
		public static final String SAVE_OSS_CHECK_NAME = PATH + "/saveOssCheckName/{targetName}";
		public static final String SAVE_OSS_NICKNAME = PATH + "/saveOssNickname";

		public static final String CHECK_OSS_LICENSE = PATH + "/checkOssLicense";
		public static final String CHECK_OSS_LICENSE_JSP = TILES_ROOT + PATH + "/checkOssLicensepopup";

		public static final String CHECK_OSS_LICENSE_AJAX = PATH + "/getCheckOssLicenseAjax/{targetName}";
		public static final String SAVE_OSS_CHECK_LICENSE = PATH + "/saveOssCheckLicense/{targetName}";

		public static final String SAVE_OSS_ANALYSIS_LIST = PATH + "/saveOssAnalysisList/{targetName}";
		public static final String OSS_AUTO_ANALYSIS = PATH + "/ossAutoAnalysis";
		public static final String OSS_AUTO_ANALYSIS_JSP = TILES_ROOT + PATH + "/ossAutoAnalysispopup";
		public static final String AUTO_ANALYSIS_LIST = PATH + "/getAutoAnalysisList";
		public static final String START_ANALYSIS = PATH + "/startAnalysis"; 
		public static final String ANALYSIS_RESULT_LIST = PATH + "/getAnalysisResultList";
		
		public static final String SET_SESSION_ANALYSIS_RESULT_DATA = PATH + "/setSessionAnalysisResultData";
		public static final String SESSION_ANALYSIS_RESULT_DATA = PATH + "/getSessionAnalysisResultData";
		public static final String SAVE_OSS_ANALYSIS_DATA = PATH + "/saveOssAnalysisData";
		public static final String ANALYSIS_RESULT_DETAIL_ID = PATH + "/getAnalysisResultDetail/{groupId}";
		public static final String ANALYSIS_RESULT_DETAIL_JSP = TILES_ROOT + PATH + "/ossAnalysisResultDetailpopup";
		
		public static final String CHECK_LICENSE_TEXT_VALIDATION = "/checkLicenseText/valid";
		public static final String START_CHECK_LICENSE_TEXT = "/checkLicenseText/start";
		
		public static final String UPDATE_ANALYSIS_COMPLETE = PATH + "/updateAnalysisComplete";
		
		public static final String OSS_SYNC_POPUP = PATH + "/osssyncpopup";
		public static final String OSS_SYNC_POPUP_JSP = TILES_ROOT + PATH + "/osssyncpopup";
		public static final String OSS_SYNC_DETAIL_VIEW_AJAX = PATH + "/ossSyncDetailViewAjax";
		public static final String OSS_SYNC_DETAILS_VIEW_AJAX_JSP = TILES_AJAX_ROOT + PATH + "/ossSyncDetailview";
		public static final String OSS_SYNC_UPDATE = PATH + "/ossSyncUpdate";
		public static final String OSS_SYNC_LIST_VALIDATION = PATH + "/ossSyncListValidation";
		
		public static final String OSS_BULK_EDIT_POPUP = PATH + "/ossBulkEditPopup";
		public static final String OSS_BULK_EDIT_POPUP_JSP = TILES_ROOT + PATH + "/ossBulkEditPopup";
		
		public static final String CHECK_OSS_VERSION_DIFF = PATH + "/checkOssVersionDiff";
		public static final String CHECK_OSS_NAME_DIFF = PATH + "/checkOssNameDiff";
	}
	
	public static final class PROJECT {
		public static final String PATH = "/project";
		
		public static final String LIST = PATH + "/list";
		public static final String LIST_JSP = TILES_ROOT + PATH + "/list";
		
		public static final String EDIT = PATH + "/edit";
		public static final String EDIT_JSP = TILES_ROOT + PATH + "/edit";
		public static final String EDIT_ID =  PATH + "/edit/{prjId}";
		
		public static final String VIEW_ID =  PATH + "/view/{prjId}";
		public static final String VIEW_JSP = TILES_ROOT + PATH + "/view";
		
		public static final String AUTOCOMPLETE_AJAX = PATH + "/autoCompleteAjax";
		public static final String AUTOCOMPLETE_VERSION_AJAX = PATH + "/autoCompleteVersionAjax";
		public static final String AUTOCOMPLETE_MODEL_AJAX = PATH + "/autoCompleteModelAjax";
		
		public static final String USER_ID_LIST = PATH + "/getUserIdList";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String DEL_AJAX = PATH + "/delAjax";
		
		public static final String SAVE_3RD = PATH + "/save3rd";
		public static final String SAVE_SRC = PATH + "/saveSrc"; 
		public static final String SAVE_BIN = PATH + "/saveBin";
		public static final String SAVE_BINANDROID = PATH + "/saveBinAndroid";
		public static final String SAVE_BOM = PATH + "/saveBom";
		public static final String SAVE_BAT = PATH + "/saveBat";
		
		public static final String IDENTIFICATION_GRID_ID_CD = PATH + "/identificationGrid/{prjId}/{code}";
		
		public static final String MODELLIST_AJAX = PATH + "/modellistAjax";
		
		public static final String CATEGORY_CODE_TOJSON = PATH + "/getCategoryCodeToJson";
		
		public static final String OSS_NAMES = PATH + "/getOssNames";
		
		public static final String OSS_VERIONS = PATH + "/getOssVersions";
		public static final String UPDATE_REVIEWER = PATH + "/updateReviewer";
		public static final String UPDATE_REJECT = PATH + "/updateReject";
		public static final String CATEGORY_CODE = PATH + "/getCategoryCode";
		
		public static final String OSS_ID_LICENSES = PATH + "/getOssIdLicenses";
		
		public static final String NICKNAME_CD = PATH + "/nickNameValid/{code}";
		public static final String UPDATE_PROJECT_STATUS = PATH + "/updateProjectStatus";
		
		public static final String IDENTIFICATION_ID_DIV = PATH + "/identification/{prjId}/{initDiv}";
		public static final String IDENTIFICATION_JSP = TILES_ROOT + PATH + "/identification";
		public static final String IDENTIFICATION_CD = PATH + "/identificationProject/{code}";
		public static final String IDENTIFICATION_PROJECT_SERCH_CD = PATH + "/identificationProjectSearch/{code}";
		public static final String IDENTIFIATION_THIRD = PATH + "/identificationThird";
		public static final String IDENTIFICAITON_GRID_POST = PATH + "/identificationGridPost";
		public static final String IDENTIFICATION_MERGED_GRID_ID_CD = PATH + "/identificationMergedGrid/{prjId}/{code}";
		public static final String TRD_OSS = PATH + "/3rdOss";
		
		public static final String ANDROID_SHEET_NAME = PATH + "/androidSheetName";
		
		public static final String OSS_ID_CHECK = PATH + "/getOssIdCheck";
		
		public static final String CHECK_CHANGE_DATA = PATH + "/getCheckChangeData";
		
		public static final String CANCEL_FILE_DEL_SRC = PATH + "/cancelFileDelSrc";
		 
		public static final String FILE_INFO = PATH + "/getFileInfo";
		public static final String TRD_MAP = PATH + "/get3rdMap";
		
		public static final String ADD_WATCHER = PATH + "/addWatcher";
		public static final String REMOVE_WATCHER = PATH + "/removeWatcher";
		public static final String COPY_WATCHER = PATH + "/copyWatcher";
		public static final String SAVE_MODEL_AJAX = PATH + "/saveModelAjax";
		
		public static final String UPDATE_PUBLIC_YN = PATH + "/updatePublicYn";
		
		public static final String PROJECT_TO_ADD_LIST = PATH + "/projectToAddList";
		public static final String ADD_LIST = PATH + "/getAddList";
		public static final String PARTNER_LIST = PATH + "/getPartnerList";
		
		public static final String BIN_CSV_FILE = PATH + "/binCsvFile";
		public static final String CSV_FILE = PATH + "/csvFile";
		public static final String SHEET_DATA = PATH + "/getSheetData";
		
		public static final String SEND_COMMENT = PATH + "/sendComment";
		public static final String ANALYSIS = PATH + "/analysis";
		
		public static final String COPY_ID = PATH + "/copy/{prjId}";
		
		public static final String ANDROID_FILE = PATH + "/androidFile";
		public static final String ANDROID_APPLY = PATH + "/androidApply";
		
		public static final String PARTNER_OSS_FROM_PROJECT = PATH + "/partnerOssFromProject";
		
		public static final String COMMENTS_SAVE = PATH + "/commentsSave";
		public static final String SAVE_COMMENT = PATH + "/saveComment";
		public static final String COMMENTS_IGNORE = PATH + "/commentsIgnore";
		
		public static final String MODEL_FILE = PATH + "/modelFile";
		
		public static final String SUPPLEMEMT_NOTICE_FILE = PATH  + "/getSupplementNoticeFile";
		
		public static final String BOM_COMPARE = PATH  + "/bomCompare/{beforePrjId}/{afterPrjId}";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "/bomCompare";
		public static final String BOM_COMPARE_LIST_AJAX = PATH + "/bomCompare/listAjax";
		
		public static final String PROJECT_STATUS = PATH + "/getProjectStatus";
		public static final String PROJECT_BINARY_FILE = PATH + "/getProjectBinaryFile";
		public static final String PROJECT_BINARY_DB_SAVE = PATH + "/binaryDBSave";
		
		public static final String MAKE_YAML = PATH + "/makeYaml/{code}";
		public static final String PROJECT_DIVISION = PATH + "/updateProjectDivision";
		public static final String UPDATE_COMMENT = PATH + "/updateComment";
	}
	
	public static final class VERIFICATION {
		public static final String PATH = "/project/verification";
		
		public static final String PAGE_ID = PATH + "/{prjId}";
		public static final String PAGE_DIV_ID = PATH + "/{initDiv}/{prjId}";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "";
		
		public static final String REGIST_FILE = PATH + "/registFile";
		public static final String UPLOAD_VERIFICATION = PATH + "/uploadVerification";
		public static final String VERIFY = PATH + "/verify";
		public static final String SAVE_PATH = PATH + "/savePath";
		
		public static final String NOTICE_AJAX = PATH + "/noticeAjax";
		public static final String DOWNLOAD_FILE = PATH + "/downloadFile";
		
		public static final String WGET_URL = PATH + "/wgetUrl";
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String SAVE_NOTICE_AJAX = PATH + "/saveNoticeAjax";
		public static final String MAKE_NOTICE_PREVIEW = PATH + "/makeNoticePreview";
		public static final String DOWNLOAD_NOTICE_PREVIEW = PATH + "/downloadNoticePreview";
		public static final String MAKE_NOTICE_TEXT = PATH + "/makeNoticeText";
		public static final String MAKE_NOTICE_SIMPLE = PATH + "/makeNoticeSimple";
		public static final String MAKE_NOTICE_TEXT_SIMPLE = PATH + "/makeNoticeTextSimple";
		
		public static final String REUSE_PROJECT_SEARCH = PATH +"/reuseProjectSearch";
		public static final String REUSE_PROJECT_PACKAGING_SEARCH = PATH + "/reuseProjectPackagingSearch";
		public static final String REUSE_PACKAGING_FILE = PATH + "/reusePackagingFile";
		
		public static final String DOWNLOAD_PACKAGE = PATH + "/downloadPackage";
		public static final String DOWNLOAD_NOTICE = PATH + "/downloadNotice";
		
		public static final String SEND_COMMENT = PATH + "/sendComment";

		public static final String DOWNLOAD_PACKAGING_MULTI = PATH + "/downloadPackageMulti";
	}
	
//	public static final class DISTRIBUTION {
//		public static final String PATH = "/project/distribution";
//		
//		public static final String PAGE_ID = PATH + "/{prjId}";
//		public static final String PAGE_JSP = TILES_ROOT + PATH + "";
//		
//		public static final String AVAILABLE_CHECK = PATH + "/availableCheck";
//		public static final String SAVE_AJAX = PATH + "/saveAjax";
//		public static final String DISTRIBUTE_ACT = PATH + "/distribute/{action}";
//		
//		public static final String ACTION_LOG_HIS_LIST = PATH + "/getActionLogHisList";
//		public static final String ACTION_LOG_HIS_LIST_JSP = TILES_AJAX_ROOT +  PATH + "/logHis";
//		
//		// complete 이후 packaging file upload, verify, distribution 처리 url 
//		public static final String REGIST_FILE = PATH + "/registFile";
//		public static final String VERIFY = PATH + "/verify";
//	}
	
	public static final class PARTNER {
		public static final String PATH = "/partner";
		
		public static final String LIST = PATH + "/list";
		public static final String LIST_JSP = TILES_ROOT + PATH + "/list";
		
		public static final String EDIT = PATH + "/edit";
		public static final String EDIT_JSP = TILES_ROOT + PATH + "/edit";
		public static final String EDIT_ID =  PATH + "/edit/{partnerId}";
		
		public static final String VIEW_ID =  PATH + "/view/{partnerId}";
		public static final String VIEW_JSP = TILES_ROOT + PATH + "/view";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String CHANGE_DIVISION_AJAX = PATH + "/changeDivisionAjax";
		public static final String DEL_AJAX = PATH + "/delAjax";
		
		public static final String AUTOCOMPLETE_CONF_NM_AJAX = PATH + "/autoCompleteConfNmAjax";
		
		public static final String AUTOCOMPLETE_NM_AJAX = PATH + "/autoCompleteNmAjax";
		
		public static final String AUTOCOMPLETE_SW_NM_AJAX = PATH + "/autoCompleteSwNmAjax";
		
		public static final String AUTOCOMPLETE_CONF_SW_NM_AJAX = PATH + "/autoCompleteConfSwNmAjax";
		
		public static final String USER_LIST = PATH + "/getUserList";

		public static final String UPDATE_REVIEWER = PATH + "/updateReviewer";
		
		public static final String AUTOCOMPLETE_SW_VER_AJAX = PATH + "/autoCompleteSwVerAjax"; 
		
		public static final String AUTOCOMPLETE_CONF_SW_VER_AJAX = PATH + "/autoCompleteConfSwVerAjax";
		
		public static final String ADD_WATCHER = PATH + "/addWatcher";
		public static final String REMOVE_WATCHER = PATH + "/removeWatcher";
		public static final String COPY_WATCHER = PATH + "/copyWatcher";
		
		public static final String UPDATE_PUBLIC_YN = PATH + "/updatePublicYn";
		
		public static final String CHANGE_STATUS = PATH + "/changeStatus";
		
		public static final String OSS_FILE = PATH + "/ossFile";
		public static final String DOCUMENT_FILE = PATH + "/documentsFile";
		
		public static final String SEND_COMMENT = PATH + "/sendComment";
		public static final String SAVE_COMMENT = PATH + "/saveComment";
		public static final String DELETE_COMMENT = PATH + "/deleteComment";
		public static final String COMMENT_LIST = PATH + "/getCommentList";
		
		public static final String SAMPLEDOWNLOAD = PATH + "/sampleDownload";
		
		public static final String FILTERED_LIST = PATH + "/getFilteredList";
		public static final String CHECK_STATUS = PATH + "/checkStatus/{partnerId}";
		
		public static final String NOTICE_TEXT = PATH + "/noticeText";
		
		public static final String SAVE_BINARY_DB = PATH + "/saveBinaryDB";
		
		public static final String MAKE_YAML = PATH + "/makeYaml";
		public static final String PARTNER_DIVISION = PATH + "/updatePartnerDivision";

		public static final String UPDATE_DESCRIPTION = PATH + "/updateDescription";
	}
	
	public static final class USER {
		public static final String PATH = "/system/user";
		
		public static final String LIST = PATH + "";
		public static final String LIST_JSP = TILES_ROOT + PATH + "";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String MOD_AJAX = PATH + "/modAjax";
		
		public static final String DIVISION_LIST = PATH + "/getDivisionList";
		
		public static final String CHECK_EMAIL = PATH + "/checkEmail";
		
		public static final String AUTOCOMPLETE_CRAETOR_AJAX = PATH + "/autoCompleteCreatorAjax";
		public static final String AUTOCOMPLETE_REVIEWER_AJAX = PATH + "/autoCompleteReviewerAjax";
		public static final String AUTOCOMPLETE_CREATOR_DIVISION_AJAX = PATH + "/autoCompleteCreatorDivisionAjax";
		
		public static final String CHANGE_PASSWORD = PATH + "/changePassword";
		public static final String UPDATE_USERNAME_DIVISION = PATH + "/updateUserNameAndDivision";
		
		public static final String TOKEN_PROC = PATH + "/tokenProc/{procType}";
	}
	
	public static final class VULNERABILITY {
		public static final String PATH = "/vulnerability";
		
		public static final String LIST = PATH + "/list";
		public static final String LIST_JSP = TILES_ROOT + PATH + "/list";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String CHECK_CVE_ID = PATH + "/checkCveId";
		
		public static final String AUTOCOMPLETE_AJAX = PATH + "/AutoCompleteAjax";
		public static final String VERSION_AUTOCOMPLETE_AJAX = PATH + "/VersionAutoCompleteAjax";
		
		public static final String VULN_POPUP = PATH + "/vulnpopup";
		public static final String VULN_POPUP_JSP = TILES_ROOT + PATH + "/vulnpopup";
		
		public static final String VULN_LIST = PATH + "/getVulnList";
	}
	
	public static final class SELF_CHECK {
		public static final String PATH = "/selfCheck";
		
		public static final String LIST = PATH + "/list";
		public static final String LIST_JSP = TILES_ROOT + PATH + "/list";
		
		public static final String EDIT = PATH + "/edit";
		public static final String EDIT_ID = PATH + "/edit/{prjId}";
		public static final String EDIT_JSP = TILES_ROOT + PATH + "/edit";
		
		public static final String VIEW_ID = PATH + "/view/{prjId}";
		public static final String VIEW_JSP = TILES_ROOT + PATH + "/view";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		
		public static final String OSSGRID_ID_CD = PATH + "/ossGrid/{prjId}/{code}";
		
		public static final String VIEW_AJAX = PATH + "/selfCheckViewAjax";
		public static final String VIEW_AJAX_JSP = TILES_AJAX_ROOT + PATH + "/view"; 
		
		public static final String LICENSE_POPUP = PATH + "/licensepopup";
		public static final String LICENSE_POPUP_JSP = TILES_ROOT + PATH + "/licensepopup";
		
		public static final String SEND_COMMENT = PATH + "/sendComment";
		public static final String SAVE_SRC = PATH + "/saveSrc";
		
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String DEL_AJAX = PATH + "/delAjax";
		
		public static final String LICENSE_USERGUIDE_HTML_NM = PATH + "/getLicenseUserGuideHtml/{licenseName}";
		public static final String ADD_WATCHER = PATH + "/addWatcher";
		public static final String REMOVE_WATCHER = PATH + "/removeWatcher";
		public static final String COPY_WATCHER = PATH + "/copyWatcher";
		
		public static final String LICENSE_DATA = PATH + "/getLicenseData";
		
		public static final String NOTICE_AJAX = PATH + "/noticeAjax";
		public static final String MAKE_NOTICE_PREVIEW = PATH + "/makeNoticePreview";
		public static final String DOWNLOAD_NOTICE_PREVIEW = PATH + "/downloadNoticePreview";
		public static final String MAKE_NOTICE_TEXT = PATH + "/makeNoticeText";
		public static final String MAKE_NOTICE_SIMPLE = PATH + "/makeNoticeSimple";
		public static final String MAKE_NOTICE_TEXT_SIMPLE = PATH + "/makeNoticeTextSimple";	
			
		public static final String MAKE_YAML = PATH + "/makeYaml";
	}
	
	public static final class COMPLIANCE {
		public static final String PATH = "/compliance";
		
		public static final String MODEL_STATUS = PATH + "/modelStatus";
		public static final String MODEL_STATUS_JSP = TILES_ROOT + PATH + "/modelStatus";
		
		public static final String PARTNER_LIST_STATUS = PATH + "/3rdList";
		public static final String PARTNER_LIST_STATUS_JSP = TILES_ROOT + PATH + "/3rdList";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		
		public static final String MODEL_LIST_AJAX = PATH + "/modelListAjax";
		public static final String READ_MODEL_LIST = PATH + "/readModelList";
		
	}
	
	public static final class EXTERNAL {
		public static final String PATH = "/external";
		
		public static final String PAGE = PATH + "/external";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "/external";
		
		public static final String REQUEST_FL_SCAN = PATH + "/request-fl-scan";
	}
	
	public static final class CODE {
		public static final String PATH = "/system/code";
		
		public static final String PAGE = PATH + "";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String DETAIL_LIST_AJAX = PATH + "/detail/listAjax"; 
		
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		public static final String DETAIL_SAVE_AJAX = PATH + "/detail/saveAjax";
		
		public static final String AUTOCOMPLETE_NO_AJAX = PATH + "/autoCompleteNoAjax";
		public static final String AUTOCOMPLETE_NM_AJAX = PATH + "/autoCompleteNmAjax";
	}
	
	public static final class HISTORY {
		public static final String PATH = "/system/history";
		
		public static final String LIST = PATH + "";
		public static final String LIST_JSP = TILES_ROOT + PATH + "";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String EDIT_IDX = PATH + "/edit/{idx}";
		public static final String EDIT_JSP = TILES_ROOT + PATH + "-edit";
	}
	
	public static final class NOTICE {
		public static final String PATH = "/system/notice";
		
		public static final String LIST = PATH + "";
		public static final String LIST_JSP = TILES_ROOT + PATH + "";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
		public static final String SAVE_AJAX = PATH + "/saveAjax";
		
		public static final String PUBLISHEDT_NOTICE = PATH + "/getPublishedtNotice";
	}
	
	public static final class CONFIGURATION {
		public static final String PATH = "/configuration";
		
		public static final String EDIT = PATH + "/edit";
		public static final String EDIT_JSP = TILES_ROOT + PATH + "/edit";
		
		public static final String SAVE_AJAX = PATH + "/saveAjax";

		public static final String VIEW_SEARCH_CONDITION_AJAX = PATH + "/loadDefaultSearchCondition";
		public static final String VIEW_SEARCH_CONDITION_JSP = TILES_AJAX_ROOT + PATH + "/searchConditionArea";
		
		public static final String UPDATE_SEARCH_CONDITION_AJAX = PATH + "/updateDefaultSearchCondition";
	}
	
	public static final class SENT_MAIL {
		public static final String PATH = "/system/sentMail";
		
		public static final String PAGE = PATH + "";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "";

		public static final String LIST_AJAX = PATH + "/listAjax";
	}
	
	public static final class VULNERABILITY_HISTORY {
		public static final String PATH = "/system/vulnerabilityHistory";
		
		public static final String PAGE = PATH + "";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "";
		
		public static final String LIST_AJAX = PATH + "/listAjax";
	}
	
	public static final class BINARY_DATA_HISTORY {
		public static final String PATH = "/system/binaryDataHistory";
		
		public static final String PAGE = PATH + "";
		public static final String PAGE_JSP = TILES_ROOT + PATH + ""; 
		
		public static final String LIST_AJAX = PATH + "/listAjax";
	}
	
	public static final class DOWNLOAD {
		public static final String PATH = "/download";
		
		public static final String SEQ_FNAME = PATH + "/{seq}/{fName:.+}";
		public static final String BATGUIREPORT_ID_CHECKSUM = PATH + "/batGuiReport/{batId}/{checkSum}";
	}
	
	public static final class STATISTICS {
		public static final String PATH = "/statistics";
		
		public static final String VIEW = PATH + "/view";
		public static final String VIEW_JSP = TILES_ROOT + PATH + "/view";
		
		public static final String DIVISIONAL_PROJECT_CHART = PATH + "/divisionProjectChart";
		public static final String MOST_USED_CHART = PATH + "/mostUsedChart";
		public static final String UPDATED_CHART = PATH + "/updatedChart";
		public static final String TRDPARTY_RELATED_CHART = PATH + "/trdPartyRelatedChart";
		public static final String USER_RELATED_CHART = PATH + "/userRelatedChart";
		
		public static final String STATISTICS_POPUP = PATH + "/statisticspopup";
		public static final String STATISTICS_POPUP_JSP = TILES_ROOT + PATH + "/statisticspopup";
	}
	
	public static final class SYSTEM_CONFIGURATION {
		public static final String PATH = "/system/configuration";
		
		public static final String PAGE = PATH + "";
		public static final String PAGE_JSP = TILES_ROOT + PATH + "";
		
		public static final String SAVE_AJAX = PATH + "/saveAjax";
	}
	
	public static final class MIGRATION {
		public static final String PATH = "/migration";
		
		public static final String COPY_OSS = PATH + "/copyOss";
		public static final String OSDD_LICENSE = PATH + "/osddLicense";
		public static final String EXCEL_TEST = PATH + "/excelTest";
		public static final String NVD_RESET = PATH + "/nvdReset";
		public static final String NVD_BULK_REG = PATH + "/nvdBulkReg";
		public static final String NVD_MERGE = PATH + "/nvdMerge";
		public static final String SELF_VULN = PATH + "/selfvuln";
		public static final String DISTRIBUTION_MODEL_SYNC = PATH + "/distributeModelSync";
		public static final String DISTRIBUTION_RUN_TIMEOUT = PATH + "/distributeRunTimeout";
		public static final String REG_BINARY_DB = PATH + "/regBinaryDB";
		public static final String OBLIGATION_RESET = PATH + "/obligationReset";
		public static final String SET_BAT_RESULT = PATH + "/setbatResult";
		public static final String NVD_DATA_SET = PATH + "/nvdDataSet";
		public static final String DISTRIBUTE_GET_FILE_OBJECT = PATH + "/distributeGetFileObject";
	}
	
	public static final class SPDXDOWNLOAD {
		public static final String PATH = "/spdxdownload";
		
		public static final String SPDX_POST = PATH + "/getSPDXPost";
		public static final String SELFCHECK_SPDX_POST = PATH + "/getSelfcheckSPDXPost";
		public static final String FILE = PATH + "/getFile";
		
	}
	
	public static final class IMAGE_VIEW {
		public static final String PATH = "/imageView";
		
		public static final String IMAGE = PATH + "/{imageName:.+}";
		public static final String GUI_REPORT_ID_NM = PATH + "/guiReport/{batId}/{imageName:.+}";
	}
	
	public static final class IMAGE_UPLOAD {
		public static final String PATH = "/imageupload";
		
		public static final String UPLOAD = PATH + "/upload";
		public static final String UPLOAD2 = PATH + "/upload2";
	}
	
	
	public static final class API {
		public static final String PATH = "/api/v1";
		
		/** 3RD PARTY */
			/** API 3rd Party 조회 */
			public static final String FOSSLIGHT_API_PARTNER_SEARCH			= "/partner_search";
		
		/** OSS */
			/** API OSS List 조회 */
			public static final String FOSSLIGHT_API_OSS_SEARCH				= "/oss_search";
			
			/** API DOWNLOAD LOCATION 조회 */
			public static final String FOSSLIGHT_API_DOWNLOADLOCATION_SEARCH	= "/downloadlocation_search";
			
		/** LICENSE */
			/** API License List 조회 */
			public static final String FOSSLIGHT_API_LICENSE_SEARCH			= "/license_search";
		
		
		/** PROJECT */
			/** API create Project  */
			public static final String FOSSLIGHT_API_PROJECT_CREATE			= "/create_project";
			
			/** API Project List 조회 */
			public static final String FOSSLIGHT_API_PROJECT_SEARCH			= "/prj_search";
			
			/** API Project Model List 조회 */
			public static final String FOSSLIGHT_API_MODEL_SEARCH			= "/model_search";

			/** API Update Project Model */
			public static final String FOSSLIGHT_API_MODEL_UPDATE			= "/model_update";
			
			/** API Project BOM Tab Export */
			public static final String FOSSLIGHT_API_PROJECT_BOM_EXPORT	    = "/prj_bom_export";

			/** API Project BOM Tab Export JSON*/
			public static final String FOSSLIGHT_API_PROJECT_BOM_EXPORT_JSON	    = "/prj_bom_export_json";

			/** API BOM COMPARE */
			public static final String FOSSLIGHT_API_PROJECT_BOM_COMPARE		= "/prj_bom_compare";
			
			/** API OSS Report upload */
			public static final String FOSSLIGHT_API_OSS_REPORT_SRC			= "/oss_report_src";
			public static final String FOSSLIGHT_API_OSS_REPORT_BIN			= "/oss_report_bin";
			public static final String FOSSLIGHT_API_OSS_REPORT_ANDROID		= "/oss_report_android";
			
			/** API Verification Packaging Upload */
			public static final String FOSSLIGHT_API_PACKAGE_UPLOAD			= "/package_upload";
			
			
		/** VULNABILITY */
			/** vulnerability info search */
			public static final String FOSSLIGHT_API_VULNERABILITY_DATA	    = "/vulnerability_data";
			
			/** vulnerability max score info search */
			public static final String FOSSLIGHT_API_VULNERABILITY_MAX_DATA	= "/vulnerability_max_data";
			
			
		/** SELFCHECK */
			/** create SelfCheck */
			public static final String FOSSLIGHT_API_SELFCHECK_CREATE			= "/create_selfcheck";
			
			/** OSS Report upload */
			public static final String FOSSLIGHT_API_OSS_REPORT_SELFCHECK		= "/oss_report_selfcheck";
		
		/** BINARY */
			/** API Binary List 조회 */
			public static final String FOSSLIGHT_API_BINARY_SEARCH			= "/binary_search";
			
		/** CODE */
			/** Code 조회 */
			public static final String FOSSLIGHT_API_CODE_SEARCH				="/code_search";
	}

	public static final class SEARCH {

		public static final String PATH = "/searchFilter";
		public static final String LICENSE = PATH + "/license";
		public static final String OSS = PATH + "/oss";
		public static final String PROJECT = PATH + "/project";
		public static final String SELFCHECK = PATH + "/selfcheck";
		public static final String PARTNER = PATH + "/partner";
		public static final String VULNERABILITY = PATH + "/vulnerability";

	}
	
}
