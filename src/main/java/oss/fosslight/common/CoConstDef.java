/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.util.regex.Pattern;

public class CoConstDef {
	
	/** Application의 기본 패키지 명: {@value #APP_DEFAULT_PACKAGE_NAME} */
	public static final String APP_DEFAULT_PACKAGE_NAME     = "oss.fosslight";
	public final static String MAPPER_PACKAGE		= APP_DEFAULT_PACKAGE_NAME+".repository";
	
	public static final String JSP_PROPERTY_URL_PATTERN    = "*.jsp";
	
	//---------------------------------------------------------------------------------------------
	// Tag Library Informations
	//---------------------------------------------------------------------------------------------
	public static final String COMM_TLD_URI  = "/WEB-INF/tlds/common-taglibs.tld";
	public static final String COMM_TLD_PATH = "/WEB-INF/tlds/common-taglibs.tld";
	
	public static final boolean VALIDATION_USE_CAMELCASE = true;
	
	public static final String ENCRYPT_DEFAULT_SALT_KEY = "Fosslight-System";
	
	/** 정적 리소스 종류 */
	private final static String[] STATIC_RES = {"/js","/css","/images","/template", "/font", "/imageView", "/mobile", "/attach", "/sample"};
	/**
	 *  정적 리소스 매핑 URL 패턴 ({@code CLASSPATH_RESOURCE_LOCATIONS}와 순서 맞출 것)
	 *  @see #CLASSPATH_RESOURCE_LOCATIONS
	 */
	public final static String[] STATIC_RESOURCES_URL_PATTERNS = {
			STATIC_RES[0]+"/**",
			STATIC_RES[1]+"/**",
			STATIC_RES[2]+"/**",
			STATIC_RES[3]+"/**",
			STATIC_RES[4]+"/**",
			STATIC_RES[5]+"/**",
			STATIC_RES[6]+"/**",
			STATIC_RES[7]+"/**",
			STATIC_RES[8]+"/**",
			"/favicon.ico","/robots.txt"
	};

	/** 정적 리소스 기본 패키지 classpath */
	private static final String STATIC_PATH = "classpath:/static";
	
	/**
	 * 정적 리소스 위치 ({@code STATIC_RESOURCES_URL_PATTERN}와 순서 맞출 것)
	 * @see #STATIC_RESOURCES_URL_PATTERNS
	 */
	public final static String[] CLASSPATH_RESOURCE_LOCATIONS = {
			STATIC_PATH+STATIC_RES[0]+"/",
			STATIC_PATH+STATIC_RES[1]+"/",
			STATIC_PATH+STATIC_RES[2]+"/",
			STATIC_PATH+STATIC_RES[3]+"/",
			STATIC_PATH+STATIC_RES[4]+"/",
			STATIC_PATH+STATIC_RES[5]+"/",
			STATIC_PATH+STATIC_RES[6]+"/",
			STATIC_PATH+STATIC_RES[7]+"/",
			STATIC_PATH+STATIC_RES[8]+"/",
			STATIC_PATH+"/"
	};

	public static final String HEALTH_CHECK_URL = "/healthCheck";
	
	/** Tiles definition xml path */
	public final static String[] TILES_LAYOUT_XML_PATH = {
		"WEB-INF/tiles.xml"
	};

	/** Runtime에서 JSP의 refresh 적용 여부 : {@value #REFRESH_JSP_ON_RUNTIME} */
	public final static boolean REFRESH_JSP_ON_RUNTIME = true;
	
	public static final int DEFAULT_PAGE_NUMBER = 0;
	public static final int DEFAULT_PAGE_SIZE = 10;
	
	/** 범용 flag YN */
	public static final String FLAG_YES = "Y";
	public static final String FLAG_NO = "N";
	
	public static final String NUM_ZERO = "0";
	
	/** 범용 bit flag */
	public static final int FLAG_A = 0x01;
	public static final int FLAG_B = 0x02;
	public static final int FLAG_C = 0x04;
	public static final int FLAG_D = 0x08;
	public static final int FLAG_E = 0x10;
	public static final int FLAG_F = 0x20;
	public static final int FLAG_G = 0x40;
	public static final int FLAG_H = 0x80;
	public static final int FLAG_I = 0x100;

	/**
	 * System Setting Code List
	 */
	// System Setting
	public static final String CD_SYSTEM_SETTING = "909";
	public static final String CD_LDAP_USED_FLAG = "910";
	public static final String CD_SMTP_USED_FLAG = "911";
	public static final String CD_EXTERNAL_SERVICE_USED_FLAG = "940";
	public static final String CD_EXTERNAL_ANALYSIS_USED_FLAG = "950";
	public static final String CD_HIDE_EMAIL_FLAG = "960";


	// Login Setting
	public static final String CD_LOGIN_SETTING					= "910";
	public static final String CD_LDAP_SERVER_URL 				= "100";
	public static final String CD_LDAP_DOMAIN					= "200";
//	public static final String CD_LDAP_INITIAL_CONTEXT_FACTORY 	= "300";
//	public static final String CD_LDAP_ERROR_49 				= "400";

	// SMTP Setting
	public static final String CD_SMTP_SETTING					= "911";
	public static final String CD_SMTP_SERVICE_HOST				= "100";
	public static final String CD_SMTP_EMAIL_ADDRESS			= "101";
	public static final String CD_SMTP_SERVICE_PORT				= "200";
	public static final String CD_SMTP_SERVICE_ENCODING			= "300";
	public static final String CD_SMTP_SERVICE_USERNAME			= "400";
	public static final String CD_SMTP_SERVICE_PASSWORD			= "401";
	
	// FOSSLight Hub Menu Info
	public static final String CD_MENU_DASHBOARD				= "001";
	public static final String CD_MENU_STATISTICS				= "002";
	public static final String CD_MENU_PROJECT_LIST				= "004";
	public static final String CD_MENU_PARTNER_LIST				= "005";
	public static final String CD_MENU_BAT_LIST					= "006";
	public static final String CD_MENU_BINARY_DB				= "007";
	public static final String CD_MENU_COMPLIANCE_STATUS		= "010";
	public static final String CD_MENU_EXTERNAL_LINK			= "011";
	
	// Dashboard Notice
	public static final String CD_DASHBOARD_DETAIL				= "801";
//	public static final String CD_DASHBOARD_NOTICE				= "100";
	
	// Project List Detail Setting
	public static final String CD_PROJECT_DETAIL				= "918";
	public static final String CD_AUTO_ANALYSIS_FLAG			= "100";
	public static final String CD_NOTICE_FLAG					= "101";
	
	// Notice Info
	public static final String CD_NOTICE_INFO					= "927";
	public static final String CD_NOTICE_HTML_STR				= "HTML";
	public static final String CD_DTL_NOTICE_HTML				= "100";
	public static final String CD_DTL_NOTICE_TEXT				= "101";
	public static final String CD_DTL_NOTICE_SPDX				= "102";
	public static final String CD_NOTICE_INTERNAL_URL			= "970";
	
	/** System Setting Code List End */
	
	public static final String EMPTY_STRING = "";
	
	/** 화면표시 날짜 형식 (yyyy-MM-dd hh:mm:ss) */
	public static final String DISP_FORMAT_DATE_ALL = "yyyy-MM-dd hh:mm:ss";
	
	/** 화면표시 날짜 형식 (yyyy-MM-dd) */
	public static final String DISP_FORMAT_DATE_YYYYMMDD = "yyyy-MM-dd";
	
	/** 모바일 화면표시 날짜 형식 (yyyy.MM.dd hh:mm:ss) */
	public static final String DISP_MOBILE_FORMAT_DATE_ALL_DOT = "yyyy.MM.dd hh:mm:ss";
	
	/** 모바일 화면표시 날짜 형식 (yyyy.MM.dd) */
	public static final String DISP_MOBILE_FORMAT_DATE_YYYYMMDD_DOT = "yyyy.MM.dd";
	
	/** 데이터 베이스 날짜 형식 (yyyy-MM-dd hh:mm:ss) */
	public static final String DATABASE_FORMAT_DATE_ALL = "yyyy-MM-dd HH:mm:ss";
	
	/** 데이터 베이스 날짜 형식 (yyyy-MM-dd hh:mm:ss.SSS) */
	public static final String DATABASE_FORMAT_DATE_TIMESTAMP = "yyyy-MM-dd HH:mm:ss.SSS";
	
	/** 화면표시 날짜 형식 taglib용 full format [yyyy-MM-dd hh:mm:ss] */
	public static final String DISP_FORMAT_DATE_TAG_DEFAULT = "-- ::";
	
	/** 화면표시 날짜 형식 taglib용 simple format [yyyy-MM-dd] */
	public static final String DISP_FORMAT_DATE_TAG_SIMPLE = "--";
	
	// Grid 관련
	public static final String GRID_OPERATION_ADD = "add";
	public static final String GRID_OPERATION_EDIT = "edit";
	public static final String GRID_OPERATION_DELETE = "del";
	public static final String GRID_NEWROW_DEFAULT_PREFIX = "jqg";
	
	/**범용코드정의 */
	public static final String COMMON_SELECTED_ETC = "999";
	
	/** Header Menu Codes (=menuId : 유저 생성시 대메뉴의 비트값을 가져오기 위한 코드) */
	
	/** MAIN MENU > APP MAIN */
	public static final String CD_DTL_MENU_MAIN_APP_MAIN = "800";
	/** MAIN MENU > NOTICE */
	public static final String CD_DTL_MENU_MAIN_NOTICE = "700";
	
	
	/** DEFAULT MAIN MENU CODE */
	public static final String[] DEFAULT_MENUS = {
		CD_DTL_MENU_MAIN_APP_MAIN,				// App 메인
		CD_DTL_MENU_MAIN_NOTICE,					// 공지사항
	};
		
	/** AD login LDAP */
	public static enum AD_LDAP_LOGIN {
		LDAP_SERVER_URL(CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_SERVER_URL)),
		//LDAP_DOMAIN(CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN)),
		//INITIAL_CONTEXT_FACTORY(CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_INITIAL_CONTEXT_FACTORY)),
//		ERROR_49(CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_ERROR_49)),
		INITIAL_CONTEXT_FACTORY("com.sun.jndi.ldap.LdapCtxFactory");
    	private String value;
    	private AD_LDAP_LOGIN(String value) {this.value = value;}
    	public String getValue(){return this.value;}
	}
	
	public static final String PACKAGING_VERIFY_FILENAME_PROPRIETARY = "proprietaryCheckList.txt";
	public static final String PACKAGING_VERIFY_FILENAME_FILE_LIST = "packageStructureInfo.txt";
	
	/** 페이지 설정 코드 */
	public static final String CD_PAGENATION = "100";
	/** BinaryDB Admin 전용 페이지 설정 코드 */
	public static final String CD_BINARYDB_PAGENATION = "906";
	/** 페이징 Default 건수 */
	public static String DISP_PAGENATION_DEFAULT = CoCodeManager.getCodes(CD_PAGENATION).firstElement();
	/** 페이징 선택 가능 건수 (grid에서 설정) */
	public static String DISP_PAGENATION_LIST_STR = CommonFunction.arrayToString(CoCodeManager.getCodes(CD_PAGENATION));
	/** 페이징 선택 가능 건수 (grid에서 설정) */
	public static String DISP_BINARYDB_PAGENATION_LIST_STR = CommonFunction.arrayToString(CoCodeManager.getCodes(CD_BINARYDB_PAGENATION));
	
	/** 페이징이 없는 GRID의 최대 표시 건수 */
	public static String DISP_PAGENATION_MAX = "5000";
	
	public static final String CD_LICENSE_DIV = "203";
	public static final String LICENSE_DIV_MULTI = "M";
	public static final String LICENSE_DIV_SINGLE = "S";
	
	/** BAT 분석 결과 lgematching table에 존재하는 binary가 존재할 경우 표시명 */
	public static final String BAT_MATCHED_STR = "matched";
	
	
	public static final String FILE_GUBUN_EDITOR_IMAGE = "E";
	public static final String FILE_GUBUN_FILE_DOWNLOAD = "DL";
	
	public static final String SESSION_KEY_UPLOAD_REPORT_PARTNER = "REPORT_PARTNER";
	public static final String SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC = "REPORT_PROJECT_SRC";
	public static final String SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN = "REPORT_PROJECT_BIN";
	public static final String SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID = "REPORT_PROJECT_ANDROID";
	public static final String SESSION_KEY_UPLOAD_REPORT_PROJECT_BAT = "REPORT_PROJECT_BAT";
	public static final String SESSION_KEY_UPLOAD_REPORT_BAT = "REPORT_BAT";

	public static final String SESSION_KEY_UPLOAD_REPORT_SELFT_PROJECT = "REPORT_SELF_PROJECT";

	public static final String SESSION_KEY_ANDROID_CHANGED_RESULTTEXT = "BINANDROID_CHANGED_RESULTTEXT";
	public static final String SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE = "REPORT_CHANGEDLICENSE";
	public static final String SESSION_KEY_NEWOSS_DEFAULT_DATA = "NEWOSS_DEFAULT_DATA";
	public static final String SESSION_KEY_NICKNAME_CHANGED = "NICKNAME_CHANGED";
	public static final String SESSION_KEY_OSS_VERSION_CHANGED = "REPORT_OSSVERSION_CHANGED";
	
	public static final String SESSION_KEY_PREFIX_DEFAULT_SEARCHVALUE = "PREFIX_DEFAULT_SEARCHVALUE_";
	public static final String SESSION_KEY_ANALYSIS_RESULT_DATA = "ANALYSIS_RESULT_DATA";
	
	/**
	 * Auto Fill OSS 기능에서 사용하는 Dependency 타입과 url 패턴 정의
	 */
	/* dependency url pattern code */
	public static final Pattern GITHUB_PATTERN = Pattern.compile("((http|https)://github.com/([^/]+)/([^/]+))");
	public static final Pattern NPM_PATTERN = Pattern.compile("((http|https)://www.npmjs.(org|com)/package/([^/]+))");
	public static final Pattern PYPI_PATTERN = Pattern.compile("((http|https)://pypi.org/project/([^/]+))");
	public static final Pattern MAVEN_CENTRAL_PATTERN = Pattern.compile("((http|https)://mvnrepository.com/artifact/([^/]+)/([^/]+)(/([^/]+))?)");
	public static final Pattern MAVEN_GOOGLE_PATTERN = Pattern.compile("((http|https)://maven.google.com/web/index.html#([^:]+):([^:]+)(:[^:]+)?)");
	public static final Pattern PUB_PATTERN = Pattern.compile("((http|https)://pub.dev/packages/([^/]+))");
	public static final Pattern COCOAPODS_PATTERN = Pattern.compile("((http|https)://cocoapods.org/pods/([^/]+))");
	/* nothing match */
	public static final Pattern UNSUPPORTED_PATTERN = Pattern.compile("(?!)");

	/**
	 * Co Code Master - 대표 코드 [S]
	 */
	// -------------- 시스템 대표 코드
	/** 시스템 공통 상수 정의 */
	public static final String CD_SYSTEM_CONSTANT_VALUE								= "S100";
	/** 시스템 공통 상수 - 상세 코드 : 범용 1Page당 표시 레코드 수 */
	public static final String CD_SYSTEM_CONSTANT_VALUE_PAGE_SIZE					= "1";
	/** 시스템 공통 상수 - 상세코드 : 업로드 이미지 파일의 Max Width */
	public static final String CD_SYSTEM_CONSTANT_VALUE_IMAGE_W_MAX					= "2";
	/** 시스템 공통 상수 - 상세코드 : 업로드 아이콘 이미지 파일의 Max Width */
	public static final String CD_SYSTEM_CONSTANT_VALUE_ICON_IMAGE_W_MAX			= "3";
	/** 시스템 공통 상수 - 상세코드 : 게시글의 'New' 뱃지 표시일 */
	public static final String CD_SYSTEM_CONSTANT_VALUE_BOARD_BADGE_DAY_NEW			= "004";
	/** 시스템 공통 상수 - 상세코드 : 게시글의 '마감임박' 뱃지 표시일 */
	public static final String CD_SYSTEM_CONSTANT_VALUE_BOARD_BADGE_DAY_CLOSE		= "005";

	/** grid sort시 숫자형 cast가 필요한 칼럼 */
	public static final String CD_SYSTEM_GRID_SORT_CAST = "101";
	public static final String CD_FILE_ACCEPT = "120";
	/** 사업부 분류 코드 - 200 */
	public static final String CD_USER_DIVISION = "200";
	public static final String CD_USER_DIVISION_EMPTY = "999";
	
	/** 프로젝트 상태 코드 - 205 */
	public static final String CD_PROJECT_STATUS = "205";
	public static final String CD_DTL_PROJECT_STATUS_PROGRESS 	= "PROG";
	public static final String CD_DTL_PROJECT_STATUS_REQUEST 	= "REQ";
	public static final String CD_DTL_PROJECT_STATUS_REVIEW 	= "REV";
	public static final String CD_DTL_PROJECT_STATUS_COMPLETE 	= "COMP";
//	public static final String CD_DTL_PROJECT_STATUS_DELAY		= "DELAY";
	public static final String CD_DTL_PROJECT_STATUS_DROP		= "DROP";
	/** status of project Identification and Packaging */
	public static final String CD_IDENTIFICATION_STATUS		="206";
	public static final String CD_DTL_IDENTIFICATION_STATUS_PROGRESS 	= "PROG";
	public static final String CD_DTL_IDENTIFICATION_STATUS_REQUEST 	= "REQ";
	public static final String CD_DTL_IDENTIFICATION_STATUS_REVIEW		= "REV";
	public static final String CD_DTL_IDENTIFICATION_STATUS_CONFIRM 	= "CONF";
	public static final String CD_DTL_IDENTIFICATION_STATUS_NA = "NA";
	
	/** 분류 타입 코드 - 207 */
	public static final String CD_DISTRIBUTION_TYPE = "207";
	public static final String CD_GENERAL_MODEL = "10";
	public static final String CD_NETWORK_SERVER = "60";
	/** Verifycation Notice Type - 208 */
	public static final String CD_NOTICE_TYPE = "208";
	public static final String CD_NOTICE_TYPE_GENERAL = "10";
	public static final String CD_NOTICE_TYPE_GENERAL_WITHOUT_OSS_VERSION = "70";
	public static final String CD_NOTICE_TYPE_PLATFORM_GENERATED = "80";
	public static final String CD_NOTICE_TYPE_NA = "99";
	/** Verifycation Notice Type - General Model */
	public static final String CD_DTL_NOTICE_TYPE_GENERAL = "10";
	public static final String CD_DTL_NOTICE_TYPE_OTHER = "20";
	public static final String CD_DTL_NOTICE_TYPE_MERGE = "30";
	public static final String CD_DTL_NOTICE_TYPE_ACCOMPANIED = "40";
	public static final String CD_DTL_NOTICE_TYPE_WITHOUTCOMPANYINFO = "50";
	public static final String CD_DTL_NOTICE_TYPE_FULLOPTION = "60";
	
	/** 3RD 파티 delivery form - 209 */
	public static final String CD_PARTNER_DELIVERY_FORM = "209";
	public static final String CD_DTL_PARTNER_DELIVERY_FORM_SRC = "SRC";
	public static final String CD_DTL_PARTNER_DELIVERY_FORM_BIN = "BIN";
	/** OSS Component DIVISION CODE */
	public static final String CD_COMPONENT_DIVISION = "210";
	public static final String CD_DTL_COMPONENT_ID_PARTNER = "10";
	public static final String CD_DTL_COMPONENT_ID_SRC = "11";
	public static final String CD_DTL_COMPONENT_ID_BAT = "12";
	public static final String CD_DTL_COMPONENT_ID_BOM = "13";
	public static final String CD_DTL_COMPONENT_ID_ANDROID = "14";
	public static final String CD_DTL_COMPONENT_ID_BIN = "15";
	public static final String CD_DTL_COMPONENT_PARTNER = "20";
	public static final String CD_DTL_COMPONENT_PARTNER_BAT = "22";
	public static final String CD_DTL_COMPONENT_BAT = "30";
	public static final String CD_DTL_COMPONENT_PACKAGING = "50";
	
	/** 3rd party status */
	public static final String CD_PARTNER_STATUS				= "211";
	public static final String CD_DTL_PARTNER_STATUS_PROGRESS 	= "PROG";
	public static final String CD_DTL_PARTNER_STATUS_REQUEST 	= "REQ";
	public static final String CD_DTL_PARTNER_STATUS_REVIEW		= "REV";
	public static final String CD_DTL_PARTNER_STATUS_CONFIRM 	= "CONF";
	
	public static final String CD_DTL_SELF_COMPONENT_ID = "10";
	
	public static final String CD_BAT_STATUS = "212";
	public static final String CD_DTL_BAT_STATUS_READY = "1";
	public static final String CD_DTL_BAT_STATUS_START = "10";
	public static final String CD_DTL_BAT_STATUS_UPLOAD = "11";
	public static final String CD_DTL_BAT_STATUS_UNPACKING = "20";
	public static final String CD_DTL_BAT_STATUS_LEAF = "30";
	public static final String CD_DTL_BAT_STATUS_AGGREGATE = "40";
	public static final String CD_DTL_BAT_STATUS_POSTRUN = "50";
	public static final String CD_DTL_BAT_STATUS_DONE = "60";
	public static final String CD_DTL_BAT_STATUS_DONE_UNOCCUPIED = "70";
	public static final String CD_DTL_BAT_STATUS_DONE_NOTDETECTED = "71";
	public static final String CD_DTL_BAT_STATUS_CANCELED = "90";
	public static final String CD_DTL_BAT_STATUS_ERROR = "99";
	
	/** OS 타입 코드 - 213 */
	public static final String CD_OS_TYPE = "213";
	public static final String CD_OS_TYPE_ETC = "999";
	/** Status of Distribute */
	public static final String CD_DISTRIBUTE_STATUS		= "215";
	public static final String CD_DTL_DISTRIBUTE_STATUS_PROGRESS = "PROG";
	public static final String CD_DTL_DISTRIBUTE_STATUS_RESERVE = "RSV";
	public static final String CD_DTL_DISTRIBUTE_STATUS_PROCESS = "PROC";
	public static final String CD_DTL_DISTRIBUTE_STATUS_DEPLOIDED = "DONE";
	public static final String CD_DTL_DISTRIBUTE_STATUS_FAILED = "ERROR";
	public static final String CD_DTL_DISTRIBUTE_STATUS_NA = "NA";
	/** Site of Distribute */
	public static final String CD_DISTRIBUTE_SITE_LGE = "LGE";
	public static final String CD_DISTRIBUTE_SITE_SKS = "SKS";
	
	public static final String CD_COMMENT_DIVISION = "214";
	public static final String CD_DTL_COMMENT_PROJECT_USER = "09"; // 코멘트 통함
	public static final String CD_DTL_COMMENT_PROJECT_HIS = "19"; // 코멘트 통함
	public static final String CD_DTL_COMMENT_IDENTIFICAITON_HIS = "10";
	public static final String CD_DTL_COMMENT_IDENTIFICATION_USER = "11";
	public static final String CD_DTL_COMMENT_PACKAGING_HIS = "12";
	public static final String CD_DTL_COMMENT_PACKAGING_USER = "13";
	public static final String CD_DTL_COMMENT_DISTRIBUTION_HIS = "14"; // 코멘트 통함
	public static final String CD_DTL_COMMENT_DISTRIBUTION_USER = "15"; // 코멘트 통함
	public static final String CD_DTL_COMMENT_PARTNER_HIS = "20";
	public static final String CD_DTL_COMMENT_PARTNER_USER = "21";
	public static final String CD_DTL_COMMENT_LICENSE = "30";
	public static final String CD_DTL_COMMENT_OSS = "40";
	
	/** License 타입별 백그라운드 색 - 216 */
	public static final String CD_LICENSE_BACKGROUND = "216";
	/** Multi or Dual license의 obligation type */
	public static final String CD_OBLIGATION_TYPE = "217";
	public static final String CD_DTL_OBLIGATION_NOTICE = "10";
	public static final String CD_DTL_OBLIGATION_DISCLOSURE = "11";
	public static final String CD_DTL_OBLIGATION_NEEDSCHECK = "90";
	public static final String CD_DTL_OBLIGATION_NEEDSCHECK_SELECTED = "99"; // NEED CHECK와 동일하나, 사용자가 적용하지 않음을 선택한 경우
	public static final String CD_DTL_OBLIGATION_NONE = "NONE"; // 검색조건으로만 사용됨
	/** Distribute Code - 218 */
	public static final String CD_DISTRIBUTE_CODE = "218";
	public static final String CD_DTL_DISTRIBUTE_LGE = "LGE";
	public static final String CD_DTL_DISTRIBUTE_SKS = "SKS";
	public static final String CD_DTL_DISTRIBUTE_NA = "NA";
	public static final String DISTRIBUTE_CHECK_URL = "/pservice/osAvailCheck";
	public static final String DISTRIBUTE_DEPLOY_URL = "/pservice/distribute";
	public static final String DISTRIBUTE_DELETE_URL = "/pservice/deleteDescription";
	public static final String DISTRIBUTE_CHECK_DELETE = "/pservice/descriptionDeletedCheck";
	public static final String DISTRIBUTE_LICENSE_URL_UPDATE = "/pservice/uploadLicense";
	public static final String DISTRIBUTE_LICENSE_URL_DELETE = "/pservice/deleteLicense";
	public static final String DISTRIBUTE_AUTH_CALLED_URL = "/pservice/getLoginToken";
	public static final String DISTRIBUTE_AUTH_URL = "/admin/login/loginWithToken";
	public static final String DISTIRBUTE_DUPLICATE_CHECK_URL = "/pservice/getDescKey";
//	public static final String DISTRIBUTE_LICENSE_URL = "/license/";
//	public static final String DISTRIBUTE_MIGRATION_TEST = "/pservice/migTest";
	
	/** Notice File Message Info - 219 */
	public static final String CD_NOTICE_DEFAULT = "219";
	public static final String CD_NOTICE_DEFAULT_SKS = "220";
	public static final String CD_DTL_NOTICE_DEFAULT_FULLNAME = "1";
	public static final String CD_DTL_NOTICE_DEFAULT_SHORTNAME = "2";
	public static final String CD_DTL_NOTICE_DEFAULT_DISTRIBUTE_SITE = "3";
	public static final String CD_DTL_NOTICE_DEFAULT_EMAIL = "4";
	public static final String CD_DTL_NOTICE_DEFAULT_TEMPLATE = "5";
	public static final String CD_DTL_NOTICE_TEXT_TEMPLATE = "6";
	public static final String CD_DTL_NOTICE_DEFAULT_APPENDED = "7";
	public static final String CD_DTL_SUPPLMENT_NOTICE_HTML_TEMPLATE = "8";
	public static final String CD_DTL_SUPPLMENT_NOTICE_TXT_TEMPLATE = "9";
	public static final String CD_DTL_SELFCHECK_NOTICE_DEFAULT_TEMPLATE = "10";
	public static final String CD_DTL_SELFCHECK_NOTICE_TEXT_TEMPLATE = "11";
	
	public static final String CD_NOTICE_DEFAULT_SOFTWARE_TYPE = "221";
	public static final String CD_DTL_NOTICE_DEFAULT_SOFTWARE_TYPE_MODEL = "10";
	public static final String CD_DTL_NOTICE_DEFAULT_SOFTWARE_TYPE_SOFTWARE = "20";
	
	public static final String CD_BINARY_FILENAME_CONVERT = "222";
	
	public static final String CD_DISTRIBUTION_HIS_TYPE = "223";
	public static final String CD_DTL_DISTRIBUTION_HIS_DISTRIBUTE = "D";
	public static final String CD_DTL_DISTRIBUTION_HIS_MODELONLY = "M";
	public static final String CD_DTL_DISTRIBUTION_HIS_SYNCHRONIZED = "S";
	public static final String CD_DTL_DISTRIBUTION_HIS_RESERVED = "R";
	public static final String CD_DTL_DISTRIBUTION_HIS_RESERVED_MODEL = "RM";
	public static final String CD_DTL_DISTRIBUTION_HIS_CANCELED = "C";
	public static final String CD_DTL_DISTRIBUTION_HIS_REJECTED = "RJ";
	public static final String CD_DTL_DISTRIBUTION_HIS_DELETED = "RJOSDD";
	public static final String CD_DTL_DISTRIBUTION_HIS_SYNCHRONIZED_DESC = "CDOSD";
	public static final String CD_DTL_DISTRIBUTION_HIS_SYNCHRONIZED_TYPE = "CSOSD";
	
	public static final String CD_VERIFY_EXCEPTION_WORDS = "224";
	public static final String CD_VERIFY_IGNORE_WORDS = "225";
	
	/** Model 타입 코드 - 500 */
	public static final String CD_MODEL_TYPE = "500";
	/** Model 타입 코드 - 550 */
	public static final String CD_MODEL_TYPE2 = "550";
	/** External Link 코드 - 600 */
	public static final String CD_EXTERNAL_LINK = "600";
	
	/** Mail Resource 타입 코드 */
	public static final String CD_MAIL_TYPE = "102";
	/** Mail Type [FOSSLight] OSS has been registered */
	public static final String CD_MAIL_TYPE_OSS_REGIST = "10";
	/** Mail Type [FOSSLight] OSS has been changed */
	public static final String CD_MAIL_TYPE_OSS_UPDATE = "11";
	/** Mail Type [FOSSLight] OSS add nickname has been updated */
	public static final String CD_MAIL_TYPE_ADDNICKNAME_UPDATE = "750";
	/** Mail Type [FOSSLight] OSS has been changed */
	public static final String CD_MAIL_TYPE_OSS_UPDATE_TYPE = "12";
	/** Mail Type [FOSSLight] OSS has been registered */
	public static final String CD_MAIL_TYPE_OSS_REGIST_NEWVERSION = "13";
	/** Mail Type [FOSSLight] OSS has been rename */
	public static final String CD_MAIL_TYPE_OSS_CHANGE_NAME = "14";
	/** Mail Type [FOSSLight] OSS has been removed */
	public static final String CD_MAIL_TYPE_OSS_RENAME = "18";
	/** Mail Type [FOSSLight] OSS has been removed */
	public static final String CD_MAIL_TYPE_OSS_DELETE = "19";
	
	public static final String CD_MAIL_TYPE_OSS_MODIFIED_COMMENT = "130";
	/** Mail Type [FOSSLight] OSS bas been deactivated */
	public static final String CD_MAIL_TYPE_OSS_DEACTIVATED = "813";
	public static final String CD_MAIL_TYPE_OSS_ACTIVATED = "814";
	
	/** Mail Type [FOSSLight] Open source license has been registered */
	public static final String CD_MAIL_TYPE_LICENSE_REGIST = "20";
	/** Mail Type [FOSSLight] Open source license has been changed */
	public static final String CD_MAIL_TYPE_LICENSE_UPDATE = "21";
	public static final String CD_MAIL_TYPE_LICENSE_UPDATE_TYPE = "22";
	public static final String CD_MAIL_TYPE_LICENSE_RENAME = "23";
	public static final String CD_MAIL_TYPE_LICENSE_MODIFIED_COMMENT = "230";
	
	/** Mail Type [FOSSLight] Open source license has been removed */
	public static final String CD_MAIL_TYPE_LICENSE_DELETE = "29";
	public static final String CD_MAIL_TYPE_PROJECT_REVIEWER_ADD = "30"; // reviewer가 없는 상태에서 새로운 reviewer를 등록한 case
	public static final String CD_MAIL_TYPE_PROJECT_REVIEWER_CHANGED = "99"; // reviewer가 존재하지만 다른 reviewer로 변경한 case
	public static final String CD_MAIL_TYPE_PROJECT_DELETED = "31"; // 추가 : PROJECT 삭제
	public static final String CD_MAIL_TYPE_PROJECT_CHANGED = "32"; // 추가 : PROJECT 변경
	public static final String CD_MAIL_TYPE_PROJECT_CREATED = "33"; // 추가 : PROJECT 변경
	public static final String CD_MAIL_TYPE_PROJECT_COPIED = "37"; // 추가 : PROJECT 복사
	
	
	public static final String CD_MAIL_TYPE_PROJECT_ADDED_COMMENT = "34"; // Project Comemnt 추가
	public static final String CD_MAIL_TYPE_PROJECT_MODIFIED_COMMENT = "340"; // Project Comemnt 수정
	public static final String CD_MAIL_TYPE_PROJECT_COMPLETED = "35"; // Project complete
	public static final String CD_MAIL_TYPE_PROJECT_DROPPED = "812"; // Project drop
	public static final String CD_MAIL_TYPE_PROJECT_REOPENED = "36"; // Project reopen
	public static final String CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT = "38"; // Project RequestToOpen Comment 추가  2018-07-23 choye 추가
	
	/** Project Request Review(Identification) */
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REQ_REVIEW = "40";
	/** Project Identification confirm */
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF = "41";
	/** Project Identification reject (not review) */
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CANCELED_CONF = "42";
	/** Project Identification reject (not review) */
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_ADDED_COMMENT = "43";
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_MODIFIED_COMMENT = "430";
	/** Project Identification reject (by review) */
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REJECT = "44";
	/** Project Identification self reject */
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_SELF_REJECT = "45";
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY = "46";
	/* Project Identification Binary DB Check*/
	public static final String CD_MAIL_TYPE_PROJECT_IDENTIFICATION_BINARY_DATA_COMMIT = "47";
	/** Project Request Review(packaging) */
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_REQ_REVIEW = "50";
	/** Project packaging confirm */
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_CONF = "51";
	/** Project packaging canceled from confirm */
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_CANCELED_CONF = "52";
	/** Project packaging commnets registered */
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_ADDED_COMMENT = "53";
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_MODIFIED_COMMENT = "530";
	/** Project packaging reject (review) */
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_REJECT = "54";
	/** Project packaging self reject */
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_SELF_REJECT = "55";
	public static final String CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY = "56";
	/** Project distribute complete */
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_COMPLETE = "60";
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_RESERVED = "61";
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_CANCELED = "62";
	public static final String CD_MAIL_TYPE_PROJECT_WATCHER_INVATED = "63";
	public static final String CD_MAIL_TYPE_PROJECT_WATCHER_REGISTED = "64";
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_FAILED = "815";

	/** changed USER Info */
	public static final String CD_MAIL_TYPE_CHANGED_USER_INFO = "816";
	
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_ADDED_COMMENT = "65";
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_MODIFIED_COMMENT = "650";
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DELETED = "66"; // OSDD에서 DESCRIPTION이 삭제된 경우
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DIFF_FILE = "67"; // OSDD에서 DESCRIPTION이 삭제된 경우
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_EDIT_FILE = "670";
	public static final String CD_MAIL_TYPE_PROJECT_DISTRIBUTE_REJECT = "68";
	
	/* Partner Binary DB Check*/
	public static final String CD_MAIL_TYPE_PARTNER_BINARY_DATA_COMMIT = "470";
	/** 3rd party reviewer changed */
	public static final String CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED = "70";
	/** 3rd party reviewer changed */
	public static final String CD_MAIL_TYPE_PARTER_REQ_REVIEW = "71";
	/** 3rd party reviewer changed */
	public static final String CD_MAIL_TYPE_PARTER_CONF = "72";
	/** 3rd party reviewer changed */
	public static final String CD_MAIL_TYPE_PARTER_CANCELED_CONF = "73";
	/** 3rd party reviewer changed */
	public static final String CD_MAIL_TYPE_PARTER_ADDED_COMMENT = "74";
	public static final String CD_MAIL_TYPE_PARTER_MODIFIED_COMMENT = "740";
	/** 3rd party Review결과 Admin이 reject한 경우 */
	public static final String CD_MAIL_TYPE_PARTER_REJECT = "75";
	/** 3rd party Request review를 self reject한 경우 */
	public static final String CD_MAIL_TYPE_PARTER_SELF_REJECT = "76";
	public static final String CD_MAIL_TYPE_PARTER_DELETED = "77";
	public static final String CD_MAIL_TYPE_PARTER_WATCHER_INVATED = "78";
	public static final String CD_MAIL_TYPE_PARTER_WATCHER_REGISTED = "79";
	/** Binary Analysis 완료시*/
	public static final String CD_MAIL_TYPE_BAT_COMPLETE = "80";
	/** Binary Analysis 실패시*/
	public static final String CD_MAIL_TYPE_BAT_ERROR = "81";
	public static final String CD_MAIL_TYPE_BAT_WATCHER_INVATED = "82";
	public static final String CD_MAIL_TYPE_BAT_WATCHER_REGISTED = "83";
	/** Self Check Watcher registed */
	public static final String CD_MAIL_TYPE_SELFCHECK_PROJECT_WATCHER_INVATED = "84";
	/** project oss 에서 새로운 취약점이 발견된 경우*/
	public static final String CD_MAIL_TYPE_VULNERABILITY_PROJECT = "90";
	/** project oss 에서 사용하지는 않지만 OSS master에 등록되어 있는 오픈소스에서 로운 취약점이 발견된 경우*/
	public static final String CD_MAIL_TYPE_VULNERABILITY_OSS = "91";
	/** vulnerability score가 9.0 이상에서 9.0 미만으로 변경된 경우 */
	public static final String CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED = "92";
	/** vulnerability score가 9.0 이상에서 9.0 미만으로 변경된 경우 - admin */
	public static final String CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED_ALL = "93";
	/** vulnerability score가 9.0 이상에서 삭제되어 recalculated 대상이 된 경우 */
	public static final String CD_MAIL_TYPE_VULNERABILITY_PROJECT_REMOVE_RECALCULATED = "94";
	public static final String CD_MAIL_TYPE_SYSTEM_ERROR = "99";
	
	/** Mail Type엥 따른 공통 Components 구성 */
	public static final String CD_MAIL_COMPONENT = "103";
	public static final String CD_MAIL_COMPONENT_OSSBASICINFO = "100";
	public static final String CD_MAIL_COMPONENT_LICENSEBASICINFO = "101";
	public static final String CD_MAIL_COMPONENT_PROJECT_BASICINFO = "200";
	public static final String CD_MAIL_COMPONENT_PROJECT_BOMOSSINFO = "201";
	public static final String CD_MAIL_COMPONENT_PROJECT_DISCROSEOSSINFO = "202";
	public static final String CD_MAIL_COMPONENT_PROJECT_DISTRIBUTIONINFO = "203";
	public static final String CD_MAIL_COMPONENT_PROJECT_MODELINFO = "204";
	public static final String CD_MAIL_COMPONENT_PARTNER_BASICINFO = "205";
	public static final String CD_MAIL_COMPONENT_BATRESULT = "206";
	public static final String CD_MAIL_COMPONENT_VULNERABILITY_PRJ = "207";
	public static final String CD_MAIL_COMPONENT_VULNERABILITY_OSS = "208";
	public static final String CD_MAIL_COMPONENT_PARTNER_OSSLIST = "209";

	public static final String CD_MAIL_COMPONENT_PARTNER_DISCLOSEOSSINFO = "215";
	public static final String CD_MAIL_COMPONENT_VULNERABILITY_RECALCULATED = "210";
	public static final String CD_MAIL_COMPONENT_PACKAGING_REQUESTED_URL = "211";
	public static final String CD_MAIL_COMPONENT_VULNERABILITY_PROJECT_RECALCULATED_ALL = "212";
	public static final String CD_MAIL_COMPONENT_SELFCHECK_PROJECT_BASICINFO = "213";
	public static final String CD_MAIL_COMPONENT_VULNERABILITY_REMOVE_RECALCULATED = "214";
	
	/** MAIL 모듈 별 KEY NAME */
	public static final String CD_MAIL_COMPONENT_NAME = "104";
	public static final String CD_MAIL_COMPONENT_TEMPLATE = "110";
	public static final String CD_MAIL_DEFAULT_CONTENTS = "111";
	
	// 이벤트 코드 : TODO MAIL-SERVICE
	public static final String	EVENT_CODE_LICENSE = "300";
	public static final String	EVENT_CODE_OSS = "301";
	public static final String	EVENT_CODE_OSS_LICENSE = "302";
	public static final String	EVENT_CODE_PROJECT = "303";
	public static final String	EVENT_CODE_PROJECT_MODEL = "304";
	public static final String	EVENT_CODE_PROJECT_WATCHER = "305";
	public static final String 	EVENT_CODE_PROJECT_STATUS = "306";
	public static final String	EVENT_CODE_PARTNER = "Partner";
	public static final String	EVENT_CODE_BOM = "BOM";
	//액션 코드 : TODO MAIL-SERVICE
	public static final String	ACTION_CODE_INSERT = "INSERT";
	public static final String	ACTION_CODE_UPDATE = "UPDATE";
	public static final String	ACTION_CODE_DELETE = "DELETE";
	public static final String	ACTION_CODE_PUBLISHED = "PUBLISHED";
	public static final String	ACTION_CODE_CANCELED = "CANCELED";
	public static final String	ACTION_CODE_NEEDED = "NEEDED";
	public static final String	ACTION_CODE_COMPLETED = "COMPLETED";
	
	/** 시스템 공통 상수 정의 Enum */
	public enum CD_SYSTEM_CONSTANT_VALUE {
    	/** 상세코드 : 범용 1Page당 표시 레코드 수 */
    	PAGE_SIZE(CD_SYSTEM_CONSTANT_VALUE_PAGE_SIZE),
    	/** 상세코드 : 업로드 이미지 파일의 Max Width */
    	IMAGE_W_MAX(CD_SYSTEM_CONSTANT_VALUE_IMAGE_W_MAX),
    	/** 상세코드 : 업로드 아이콘 이미지 파일의 Max Width */
    	ICON_IMAGE_W_MAX(CD_SYSTEM_CONSTANT_VALUE_ICON_IMAGE_W_MAX),
    	/** 상세코드 : 게시글의 'New' 뱃지 표시일 */
    	BOARD_BADGE_DAY_NEW(CD_SYSTEM_CONSTANT_VALUE_BOARD_BADGE_DAY_NEW),
    	/** 상세코드 : 게시글의 '마감임박' 뱃지 표시일 */
    	BOARD_BADGE_DAY_CLOSE(CD_SYSTEM_CONSTANT_VALUE_BOARD_BADGE_DAY_CLOSE);
    	/** */
    	private String value;
    	private CD_SYSTEM_CONSTANT_VALUE(String value) {this.value = value;}
    	public String getValue(){return this.value;}
    }	
	/** 라이센스 타입 코드 **/
	public static final String CD_LICENSE_TYPE 									= "201";
	public static final String CD_LICENSE_TYPE_CP 								= "CP";
	public static final String CD_LICENSE_TYPE_WCP 								= "WCP";
	public static final String CD_LICENSE_TYPE_PMS								= "PMS";
	public static final String CD_LICENSE_TYPE_PF								= "PF"; // Proprietary Free
	public static final String CD_LICENSE_TYPE_NA								= "NA"; // Proprietary
	
	/** 라이센스 Restriction 코드 **/
	public static final String CD_LICENSE_RESTRICTION							= "226";
	public static final String CD_LICENSE_NETWORK_RESTRICTION					= "2";
	
	/** 사용자별 Default Tab Menu 코드 */
	public static final String CD_DEFAULT_TAB 									= "701";
	
	/** marquee contents */
	public static final String CD_MARQUEE										= "702";
	public static final String CD_DTL_CONTENTS									= "100";
	
	/** regist domain */
	public static final String CD_REGIST_DOMAIN									= "703";
	public static final String CD_DTL_DEFAULT_DOMAIN							= "100";
	public static final String CD_DTL_ECT_DOMAIN								= "ETC";

	/** 사용자별 Default Locale List 코드 **/
	public static final String CD_DEFAULT_LOCALE 								= "704";

	/** External Service Setting */
	public static final String CD_EXTERNAL_SERVICE_SETTING						= "705";
	public static final String CD_DTL_GITHUB_TOKEN  							= "100";

	/** External Analysis Setting */
	public static final String CD_EXTERNAL_ANALYSIS_SETTING						= "706";
	public static final String CD_DTL_FL_SCANNER_URL 							= "101";
	public static final String CD_DTL_ADMIN_TOKEN								= "102";

	// -------------- 서브메뉴 대표 코드
	/** 대메뉴 */
	public static final String CD_MENU_MAIN 									= "M001";
	
	/** 서브메뉴 - Main */
	public static final String CD_MENU_SUB_MAIN									= "M100";
	
	/** 서브메뉴 - 게시판 관리 */
	public static final String CD_NEWS_SUB_BOARD		 						= "M600";
	
	/** 캠페인 참여 경로 코드 **/
	public enum CAMPAIGN_ROUTE {
		RECOMMEND	("01"),		// 기업 및 지인 추천
		SNS			("02"),		// SNS 채널
		NOTICE		("03"),		// 각종 홈페이지 공지사항
		BROCHURE	("04"),		// 브로셔 홍보
		ETC			("99");		// 기타
		
		private String value;
		private CAMPAIGN_ROUTE(String value) {this.value = value;};
		public String getValue() {return this.value;};
	}
	
	//license guide
//	public static final String CD_LICENSE_GUID 									= "400";
//	public static final String CD_LICENSE_GUID_EXPORT 							= "401";
	public static final String CD_SAMPLE_FILE 									= "121";

	// Excel Download/Export
	public static final String CD_EXCEL_DOWNLOAD								= "901";
	public static final String CD_MAX_ROW_COUNT									= "100";
	
	// Collab Info
	public static final String CD_COLLAB_INFO									= "902";
	public static final String CD_HELP_URL										= "100";
	public static final String CD_PACKAGING_REQUESTED_URL						= "101";
	public static final String CD_SUPPLEMENT_NOTICE_HELP_URL					= "102";
	
	public static final String CD_CHECK_OSS_NAME_URL							= "903";
	
	// check LicenseText Server Info
	public static final String CD_CHECK_LICENSETEXT_SERVER_INFO					= "904";
	public static final String CD_SERVER_URL									= "100";
	public static final String CD_DOWNLOAD_URL									= "200";
	
	public static final String CD_AUTO_ANALYSIS									= "905";
	public static final String CD_IDLE_TIME										= "100";
	
	public static final String CD_BAT_ERROR_STRING_LIST							= "907";
	
	public static final int CD_XLSX_UPLOAD_FILE_SIZE_LIMIT						= 5248000;
	public static final int CD_CSV_UPLOAD_FILE_SIZE_LIMIT						= 5248000;
	
	public static final String CD_ANDROID_NOTICE_XML							= "20";
	
	public static final String CD_CHECK_OSS_SELF 								= "SELF";
	public static final String CD_CHECK_OSS_IDENTIFICATION 						= "IDENTIFICATION";
	public static final String CD_CHECK_OSS_PARTNER 							= "PARTNER";
	
	// Open API Token Proc
	public static final String CD_TOKEN_CREATE_TYPE								= "CREATE";
	public static final String CD_TOKEN_DELETE_TYPE								= "DELETE";
	public static final String CD_MAIL_TOKEN_CREATE_TYPE						= "800";
	public static final String CD_MAIL_TOKEN_DELETE_TYPE						= "801";
	public static final String CD_TOKEN_END_DATE								= "9999-12-31";

	public static final String CD_MAIL_PACKAGING_UPLOAD_SUCCESS					= "810";
	public static final String CD_MAIL_PACKAGING_UPLOAD_FAILURE					= "811";
	
	public static final int CD_OPEN_API_CREATE_PROJECT_LIMIT					= 3;
	public static final int CD_OPEN_API_PACKAGE_FILE_LIMIT						= 3;
	
	// Open API return messsage(error)
	public static final String CD_OPEN_API_MESSAGE								= "908";
	public static final String CD_OPEN_API_USER_NOTFOUND_MESSAGE				= "200";
	public static final String CD_OPEN_API_SIGNIN_FAILED_MESSAGE				= "210";
//	public static final String CD_OPEN_API_EMPTY_ROW_MESSAGE					= "300"; // 사용하지 않음.
	public static final String CD_OPEN_API_PARAMETER_ERROR_MESSAGE				= "310";
	public static final String CD_OPEN_API_CREATE_OVERFLOW_MESSAGE				= "320";
	public static final String CD_OPEN_API_DATA_VALIDERROR_MESSAGE				= "330";
	public static final String CD_OPEN_API_CREATE_PROJECT_DUPLICATE_MESSAGE		= "340";
	public static final String CD_OPEN_API_FILE_NOTEXISTS_MESSAGE				= "400";
	public static final String CD_OPEN_API_FILE_SIZEOVER_MESSAGE				= "410";
	public static final String CD_OPEN_API_EXT_UNSUPPORT_MESSAGE				= "420";
	public static final String CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE			= "430";
	public static final String CD_OPEN_API_PERMISSION_ERROR_MESSAGE				= "500";
	public static final String CD_OPEN_API_COMMUNICATION_ERROR_MESSAGE			= "900";
	public static final String CD_OPEN_API_UNKNOWN_ERROR_MESSAGE				= "999";
	
	public static final String CD_COMMA_CHAR									= ",";
	
	public static final String CD_PLATFORM_GENERATED							= "307";
	public static final String CD_DTL_DEFAULT_PLATFORM							= "1";
	
	public static final String CD_PROJECT_PRIORITY								= "308";
	public static final String CD_PRIORITY_P0									= "10";
	public static final String CD_PRIORITY_P2									= "30";

	public static final String CD_DISTRIBUTE_ACT_IMMEDIATELY					= "I";
	public static final String CD_DISTRIBUTE_ACT_MODEL_ONLY						= "M";
	public static final String CD_DISTRIBUTE_ACT_DESCRIPTION_ONLY				= "DD";
	public static final String CD_DISTRIBUTE_ACT_SOFTWARE_ONLY					= "DS";
	public static final String CD_DISTRIBUTE_ACT_DESCRIPTION_SOFTWARE			= "DA";
	public static final String CD_DISTRIBUTE_ACT_PACKAGING						= "DP";
	public static final String CD_DISTRIBUTE_ACT_NOT_CHANGE						= "DNC";
	
	public static final String CD_LDAP_SEARCH_INFO								= "931";
	public static final String CD_DTL_LDAP_SEARCH_ID							= "100";
	public static final String CD_DTL_LDAP_SEARCH_PW							= "200";
	
	/* SPDX CODE */
	public static final String CD_SPDX											= "spdx";
	public static final String CD_SPDX_RPF										= "spdxRdf";
	public static final String CD_SPDX_TAG										= "spdxTag";
	public static final String CD_SPDX_JSON										= "spdxJson";
	public static final String CD_SPDX_YAML										= "spdxYaml";
}
