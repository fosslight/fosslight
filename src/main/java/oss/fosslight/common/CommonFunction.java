/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common; 

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.ComBean;
import oss.fosslight.domain.LicenseHtmlGeneratorFromXml;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssAnalysis;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.service.OssService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoOssValidator;

@Component("CommonFunction")
@Slf4j
public class CommonFunction extends CoTopComponent {
	
	private static T2UserService t2UserService;
	
	public static void setT2UserService(T2UserService service) {
		t2UserService = service;
	}
	
	private static OssService ossService;
	
	public static void setOssService(OssService service) {
		ossService = service;
	}
	
	private static ProjectService projectService;
	
	public static void setProjectService(ProjectService service) {
		projectService = service;
	}
	
	private static PartnerService partnerService;
	
	public static void setPartnerService(PartnerService service) {
		partnerService = service;
	}
	
	public static String getCoConstDefVal(String nm) {
        try {
            return (String) CoConstDef.class.getField(nm).get(null);
        } catch (Exception e) {
            throw new RuntimeException(nm + ":" + e);
        }
    }
    
    public static String formatDate(String dt, String separator) {
        StringBuffer ret = new StringBuffer();
        dt = dt.replaceAll("[^\\d]*", "");
        
        if (dt.length() >= 8) {
            if (CoConstDef.DISP_FORMAT_DATE_TAG_SIMPLE.equals(separator)) {
                // yyyyMMdd
                dt = dt.substring(0, 8);
            } else if (dt.length() >= 14) {
                dt = dt.substring(0, 14);
            }
        }
        
        if (dt.length() < 4) {
            ret.append(dt);
        } else {
            int i = 0;
            ret.append(dt.substring(0, 4));
            if (separator.length() > i) {
                ret.append(separator.charAt(i++));
            }
            dt = dt.substring(4);
            while (dt.length() >= 2) {
                ret.append(dt.substring(0, 2));
                if (separator.length() > i) {
                    ret.append(separator.charAt(i++));
                }
                dt = dt.substring(2);
            }
        }
        return ret.toString();
    }
    
    public static String formatDate(String dt) {
        return formatDate(dt, CoConstDef.DISP_FORMAT_DATE_TAG_DEFAULT);
    }
    
    public static String formatDateSimple(String dt) {
        return formatDate(dt, CoConstDef.DISP_FORMAT_DATE_TAG_SIMPLE);
    }
    
    public static boolean contains(Object[] array, Object item) {
        if (array != null) {
            return Arrays.asList(array).contains(item);
        }
        
        return false;
    }
    
    public static String arrayToString(String[] arr, String sep) {
        String tmpStr = CoConstDef.EMPTY_STRING;
        if (isEmpty(sep)) {
            sep = ","; // default value
        }
        if (arr != null) {
            for (String s : arr) {
                tmpStr += (isEmpty(tmpStr) ? "" : sep) + s.trim();
            }
        }
        return tmpStr;
    }
    
    public static String arrayToStringForSql(String[] arr) {
        String tmpStr = CoConstDef.EMPTY_STRING;
        if (arr != null) {
            for (String s : arr) {
                tmpStr += (isEmpty(tmpStr) ? "" : ",") + "\'" + avoidNull(s) + "\'";
            }
        }
        return tmpStr;
    }
    
    public static String arrayToString(List<String> list) {
    	if (list == null || list.isEmpty()) {
    		return "";
    	}
    	String[] arr = new String[list.size()];
    	return arrayToString(list.toArray(arr), null);
    }
    
    public static String getCurrentDateTime() {
        DateFormat df = new SimpleDateFormat("yyMMdd");
        return df.format(new Date());
    }
    
    public static String getCurrentDateTime(String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }
    
    public static String getCurrentDateTime(String format, String timeZone) {
        DateFormat df = new SimpleDateFormat(format);
        TimeZone tx=TimeZone.getTimeZone( timeZone );
        df.setTimeZone(tx);
        return df.format(new Date());
    }
    
    public static Boolean isEmpty(String s) {
		return StringUtil.isEmptyTrimmed(s);
	}
    
    public static Boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}
    
    public static String avoidNull(String s)
    {
        return StringUtil.defaultString(s);
    }
    
    public static String avoidNull(String s0, String s1)
    {
        return StringUtil.isEmptyTrimmed(s0) ? s1 : s0;
    }
    
	public static HashMap<String, Object> uploadInfoCreate(MultipartFile uploadFile) {
		// 파일관련 변수
		String uploadCode = "01";
        String orgFileName = "";
        String destFileFullPath = "";
        File destFile = null;
        
        // 반환할 map 선언
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
		
		// 파일 null 체크
		if (uploadFile != null && uploadFile.getSize() > 0){
			// STEP 1 : 디렉토리 구조를 정하고 디렉토리를 만든다.
			String thisDate = DateUtil.getCurrentDateTime("yyyy-MM-dd-hh-mm-ss");
			
			String[] thisDateSplit = thisDate.split("\\-");
			
			
	        String year = thisDateSplit[0];
	        String month = thisDateSplit[1];
	        String day = thisDateSplit[2];
	        String hour = thisDateSplit[3];
	        String minute = thisDateSplit[4];
	        String second = thisDateSplit[5];
	        
	        // STEP 1.2 : 파일의 경로를 get
	        String rUploadPath = "";				 
	        
	        // 생성할 디렉토리 경로 완성(년 - 월)
	        rUploadPath += "attach/" +year + "/" + month;
	        
	        // 확장자 추출
	        String fileExt = uploadFile.getOriginalFilename().substring(uploadFile.getOriginalFilename().lastIndexOf("."));
	        
	        // TODO : 확장자 체크 로직 추가
	        
	        // 원본 파일명을 정한다.
	        orgFileName = uploadFile.getOriginalFilename();
	        
	        // 파일명을 정한다.
	        String frontFileName = day + hour + minute + second;
	        String destFileName = frontFileName + "_" + UUID.randomUUID().toString();
	        destFileFullPath = rUploadPath + "/" + destFileName + fileExt;
	        
	        // STEP 2 : 업로드할 full path를 정한다.
	        try {
	        	// 
	        	destFile = new File(rUploadPath, destFileName+fileExt);
	        	
	        } catch(Exception e) {
	        	log.error(e.getMessage(), e);
	        	uploadCode = "00";
	        }
		}
		resultMap.put("uploadCode",uploadCode);
		resultMap.put("orgFileName",orgFileName);
		resultMap.put("destFileFullPath",destFileFullPath);
		resultMap.put("destFile",destFile);
		
		return resultMap;
	}
	
	public static String getMacAddressForm(String s) {
		String ret = "";
		String Hyphen = "-";
		
		if (s.length() == 12) {
			ret += s.substring(0, 2) + Hyphen + s.substring(2, 4) + Hyphen + s.substring(4, 6) + Hyphen + s.substring(6, 8);
			ret += Hyphen + s.substring(8, 10) + Hyphen + s.substring(10, 12);
		}else{
			ret = "형식에 맞지 않습니다.";
		}
		
		return ret;
	}
	
	public static Gson getGsonBuiler() {
		GsonBuilder gbuilder = new GsonBuilder();
		gbuilder.registerTypeAdapter(ComBean.class, new ComBeanSerializer());
		return gbuilder.create();
	}
	
	public static String genOptionUsers(String authority, String userid) {
		List<T2Users> users = t2UserService.getAuthorityUsers(authority);
		
        StringBuffer stringbuffer = new StringBuffer();
        
        for (int k = users.size(), j = 0; j < k; j++) {
            T2Users user = users.get(j);
            stringbuffer.append("    <option value='").append(user.getUserId()).append('\'');
            if (user.getUserId().equalsIgnoreCase(userid)) {
            	stringbuffer.append(" selected");
            }
            stringbuffer.append(">").append(user.getUserName()).append("</option>\n");
        }

        return stringbuffer.toString();
    }
	
	@SuppressWarnings("unchecked")
	public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication(); 
        
        String result = "";
    	try{
	    	Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	    	if (!authorities.isEmpty()){
	    		for (GrantedAuthority authority : authorities) {
	    			result = (authority.getAuthority()).replaceFirst(AppConstBean.SECURITY_ROLE_PREFIX, "");
	    			break;
				}
	    	}
    	} catch(Exception e){}
    	
    	if (auth != null && "ROLE_ADMIN".equalsIgnoreCase(result) && auth.isAuthenticated()) { 
        	return true;
        }
    	
    	if (auth == null) {
			return true;
		}
    	
    	return false;
    }
	
	@SuppressWarnings("unchecked")
	public static boolean isViewer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication(); 
        
        String result = "";
        
    	try{
	    	Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	    	if (!authorities.isEmpty()){
	    		for (GrantedAuthority authority : authorities) {
	    			result = (authority.getAuthority()).replaceFirst(AppConstBean.SECURITY_ROLE_PREFIX, "");
	    			break;
				}
	    	}
    	} catch(Exception e){}
    	
    	if (auth != null && "ROLE_VIEWER".equalsIgnoreCase(result) && auth.isAuthenticated()) { 
        	return true;
        }
    	
    	return false;
    }

	public static String makeLicenseExpressionIdentify(List<ProjectIdentification> list) {
		return makeLicenseExpressionIdentify(list, "");
	}
	
	public static String makeLicenseExpressionIdentify(List<ProjectIdentification> list, String delimiter) {
		
		if (list == null || list.isEmpty()) {
			return "";
		}
		
		List<OssLicense> licenseList = new ArrayList<>();
		
		// 필요한 정보만 재설정한다.
		for (ProjectIdentification bean : list) {
			OssLicense license = new OssLicense();
			license.setOssLicenseComb(bean.getOssLicenseComb());
			license.setLicenseName(bean.getLicenseName());
			
			if (!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())){
				license.setLicenseName(license.getLicenseName());
				licenseList.add(license);
			}
		}
		
		if (isEmpty(delimiter)) {
			return makeLicenseExpression(licenseList);
		}else {
			return makeLicenseString(licenseList, delimiter);
		}
	}
public static String makeRecommendedLicenseString(OssMaster ossmaster, ProjectIdentification bean) {
		List<ProjectIdentification> list = new ArrayList<>();
		boolean dual = false;
		for (OssLicense license : ossmaster.getOssLicenses()) {
			ProjectIdentification prj = new ProjectIdentification();
			prj.setOssLicenseComb(license.getOssLicenseComb());
			prj.setLicenseType(license.getLicenseType());
			prj.setLicenseName(license.getLicenseName());
			list.add(prj);
			if(!isEmpty(license.getOssLicenseComb()) && license.getOssLicenseComb().equals("OR")) {
				dual = true;
			}
		}
		Map<String, LicenseMaster> licenseInfo = CoCodeManager.LICENSE_INFO_UPPER;
		if(dual) {
			if(list.size() != 0) {
				list = CommonFunction.makeLicenseExcludeYn(list);
				String licenseText = CommonFunction.makeLicenseExpressionIdentify(list, ",");
				if(!isEmpty(licenseText)) {
					String[] recommended = licenseText.split(",");
					String[] userinput = bean.getLicenseName().split(",");
					List<String> recType = new ArrayList<>();
					List<String> userType = new ArrayList<>();
					for(String s : recommended) {
						if(licenseInfo.get(s.toUpperCase()) != null){
							recType.add(licenseInfo.get(s.toUpperCase()).getLicenseType());
						}
					}
					for(String s : userinput) {
						if(licenseInfo.get(s.toUpperCase()) != null) {
							userType.add(licenseInfo.get(s.toUpperCase()).getLicenseType());
						}
					}
					List<String> recTypeList = recType.stream().distinct().collect(Collectors.toList());
					List<String> userTypeList = userType.stream().distinct().collect(Collectors.toList());
					if(recTypeList.size() == 1 && userTypeList.size() == 1) {
						if(recTypeList.get(0).equals(userTypeList.get(0))) {
							return null;
						}
					}

					int cnt = 0;
					for(String s : recommended) {
						for(String s2 : userinput) {
							if(s.equals(s2)) {
								cnt++;
							}
						}
					}
					if(cnt == 0) {
						return licenseText;
					}
				}
			}
		}
		return null;
	}
	public static String makeLicenseString(List<OssLicense> list, String delimiter) {
		String result = "";
		for (OssLicense bean : list) {
			if (!isEmpty(result)) {
				result += delimiter;
			}
			
			String licenseName = bean.getLicenseName();
			if (CoCodeManager.LICENSE_INFO.containsKey(licenseName)) {
				licenseName = avoidNull(CoCodeManager.LICENSE_INFO.get(licenseName).getShortIdentifier(), licenseName);
			}
			
			result += licenseName;
		}
		
		return result;
	}
	
	public static String makeLicenseExpression(List<OssLicense> list) {
		return makeLicenseExpression(list, false);
	}
	
	public static String makeLicenseExpression(List<OssLicense> list, boolean htmlLinkType) {
		return makeLicenseExpression(list, htmlLinkType, false);
	}
	
	public static String makeLicenseExpression(List<OssLicense> list, boolean htmlLinkType, boolean spdxConvert) {
		return makeLicenseExpression(list, htmlLinkType, spdxConvert, false);
	}
	
	public static String makeLicenseExpressionMsgType(List<OssLicense> list, boolean msgType) {
		return makeLicenseExpression(list, false, false, msgType);
	}
	
	public static String makeLicenseExpression(List<OssLicense> list, boolean htmlLinkType, boolean spdxConvert, boolean msgType) {
		String rtnVal = "";
		
		List<List<String>> licenseNameList = new ArrayList<>();
		List<String> andLicenseList = new ArrayList<>();
		boolean isFirstLicense = true;
		for (OssLicense bean : list) { 
			if (!isFirstLicense && "OR".equals(bean.getOssLicenseComb())) {
				if (!andLicenseList.isEmpty()) {
					licenseNameList.add(andLicenseList);
					andLicenseList = new ArrayList<>();
					andLicenseList.add(bean.getLicenseName());
				}
			} else {
				andLicenseList.add(bean.getLicenseName());
			}
			isFirstLicense = false;
		}
		
		if (!andLicenseList.isEmpty()) {
			licenseNameList.add(andLicenseList);
		}
		
		// 연삭식 적용 및 Short Identifier 로 변경
		if (!licenseNameList.isEmpty()) {
			int liListSize = licenseNameList.size();
			int liIdx = 0;
			
			for (List<String> combList : licenseNameList) {
				if (!isEmpty(rtnVal)) {
					rtnVal += " OR ";
				}
				String andStr = "";
				int combListSize = combList.size();
				int idx = 0;
				
				for (String s : combList) {
					if (!isEmpty(andStr)) {
						if (msgType) { 
							andStr += ", ";
						} else {
							andStr += " AND ";
						}
					}
					
					LicenseMaster licenseMaster = null;
					
					if (CoCodeManager.LICENSE_INFO.containsKey(s)) {
						s = avoidNull(CoCodeManager.LICENSE_INFO.get(s).getShortIdentifier(), s);
						licenseMaster = CoCodeManager.LICENSE_INFO.get(s);
					}
					
					if (htmlLinkType && licenseMaster != null) {
						if (liListSize == 1) {
							if (combListSize > 1) {
								if (idx == 0) {
									andStr += "<a href='javascript:void(0);' class='urlLink mr-1'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
								} else {
									andStr += "<a href='javascript:void(0);' class='urlLink ml-1'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
								}
							} else {
								andStr += "<a href='javascript:void(0);' class='urlLink'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
							}
						} else {
							if (combListSize > 1) {
								if (liIdx != liListSize -1) {
									if (idx == 0) {
										andStr += "<a href='javascript:void(0);' class='urlLink mr-1'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
									} else {
										andStr += "<a href='javascript:void(0);' class='urlLink ml-1'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
									}
								} else {
									andStr += "<a href='javascript:void(0);' class='urlLink ml-1'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
								}
							} else {
								if (liIdx != liListSize -1) {
									andStr += "<a href='javascript:void(0);' class='urlLink mr-1'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
								} else {
									andStr += "<a href='javascript:void(0);' class='urlLink ml-1'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
								}
							}
						}
					} else {
						if (spdxConvert) {
							// identifier가 없는 경우 라이선스 이름을 spdx 연동 용으로 변경한다.
							s = licenseStrToSPDXLicenseFormat(s);
						}
						andStr += s;
					}
					idx++;
				}
				
				rtnVal += licenseNameList.size() > 1 ? ( combList.size() != 1 && !msgType ? ("(" + andStr + ")") :  andStr ) : andStr;
				liIdx++;
			}
		}
		
		return rtnVal;
	}

	public static String licenseStrToSPDXLicenseFormat(String licenseStr) {
		if (CoCodeManager.LICENSE_INFO.containsKey(licenseStr) && isEmpty(CoCodeManager.LICENSE_INFO.get(licenseStr).getShortIdentifier())) {
			licenseStr = "LicenseRef-" + licenseStr;
			licenseStr = CommonFunction.removeSpecialCharacters(licenseStr, true).replaceAll("\\(", "-").replaceAll("\\)", "");
		}
		return licenseStr;
	}

	public static String makeLicenseFromFiles(OssMaster _ossBean, boolean booleanflag) {
		List<String> resultList = new ArrayList<>(); // declared License
		
		if (_ossBean != null) {
			for (OssLicense license : _ossBean.getOssLicenses()) {
				String licenseName = license.getLicenseName();
				if (booleanflag) {
					licenseName = avoidNull(CoCodeManager.LICENSE_INFO.get(licenseName).getShortIdentifier(), "LicenseRef-" + licenseName);
					if (licenseName.startsWith("LicenseRef-")) {
						licenseName = CommonFunction.removeSpecialCharacters(licenseName, true).replaceAll("\\(", "-").replaceAll("\\)", "");
					}
				}
				
				if (!resultList.contains(licenseName)) {
					resultList.add(licenseName);
				}
			}

			List<String> detectedLicenseList = _ossBean.getDetectedLicenses(); // detected License
			if (detectedLicenseList != null && detectedLicenseList.isEmpty()) {
				for (String licenseName : detectedLicenseList) {
					if (booleanflag) {
						licenseName = avoidNull(CoCodeManager.LICENSE_INFO.get(licenseName).getShortIdentifier(), "LicenseRef-" + licenseName);
						if (licenseName.startsWith("LicenseRef-")) {
							licenseName = CommonFunction.removeSpecialCharacters(licenseName, true).replaceAll("\\(", "-").replaceAll("\\)", "");
						}
					}
					
					if (!resultList.contains(licenseName)) {
						resultList.add(licenseName);
					}
				}
			}
		}
		
		return String.join(",", resultList);
	}
	
	public static String removeSpecialCharacters(String licenseStr, boolean convertDashFlag) {
		String[] patternCheckList = new String[] {"!", "@", "#", "$", "%", "^", "&", "*", ",", "?", "\\", "\"", ":", "{", "}", "|", "<", ">", "/", "_"};
		for (String pattern : patternCheckList) {
			if (licenseStr.contains(pattern)) {
				licenseStr = licenseStr.replaceAll(pattern, "");
			}
		}
		
		if (convertDashFlag) {
			String[] dashConversionPatternCheckList = new String[] {"--", " "};
			for (String pattern : dashConversionPatternCheckList) {
				if (licenseStr.contains(pattern)) {
					licenseStr = licenseStr.replaceAll(pattern, "-");
				}
			}
		}
		return licenseStr;
	}
	
	public static List<ProjectIdentification> makeLicenseExcludeYn(List<ProjectIdentification> list){
		// OR 조건으로 각 list로 구분한다.
		List<List<ProjectIdentification>> andCombLicenseList = new ArrayList<>();
		
		for (ProjectIdentification bean : list) {
			if (andCombLicenseList.isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
				andCombLicenseList.add(new ArrayList<>());
			}
			
			andCombLicenseList.get(andCombLicenseList.size()-1).add(bean);
		}
		
		// 최종 퍼미션
		int selectedIdx = 0;
		int idx = 0;
		List<Integer> permissiveListNA = new ArrayList<>();
		List<Integer> permissiveListPF = new ArrayList<>();
		List<Integer> permissiveListPSM = new ArrayList<>(); // 미사용
		List<Integer> permissiveListWCP = new ArrayList<>(); // 미사용
		List<Integer> permissiveListCP = new ArrayList<>(); // 미사용
		String finalPermissive = null; // or 기준 가장 permissive 한 라이선스가 무었인지
		
		for (List<ProjectIdentification> andList : andCombLicenseList) {
			// 그룹별 퍼미션 취득
			switch (getLicensePermissive(andList)) {
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					finalPermissive = CoConstDef.CD_LICENSE_TYPE_PMS;
					selectedIdx = idx;
					permissiveListPSM.add(selectedIdx);
				
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if (!CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive)) {
						finalPermissive = CoConstDef.CD_LICENSE_TYPE_WCP;
						selectedIdx = idx;
						permissiveListWCP.add(selectedIdx);
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_CP:
					if (!CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive)
							&& !CoConstDef.CD_LICENSE_TYPE_WCP.equals(finalPermissive)) {
						finalPermissive = CoConstDef.CD_LICENSE_TYPE_CP;
						selectedIdx = idx;
						permissiveListCP.add(selectedIdx);
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PF:
					if (!CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive)
							&& !CoConstDef.CD_LICENSE_TYPE_WCP.equals(finalPermissive)
							&& !CoConstDef.CD_LICENSE_TYPE_CP.equals(finalPermissive)) {
						finalPermissive = CoConstDef.CD_LICENSE_TYPE_PF;
						selectedIdx = idx;
						permissiveListPF.add(selectedIdx);
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_NA:
					if (isEmpty(finalPermissive)) {
						finalPermissive = CoConstDef.CD_LICENSE_TYPE_NA;
						selectedIdx = idx;
						permissiveListNA.add(selectedIdx);
					}
					
					break;
			}
			
			idx++;
		}

		// OR 기준으로 다시 조합
		idx = 0;
		List<ProjectIdentification> resultList = new ArrayList<>();
		boolean hasSelected = false;
		
		for (List<ProjectIdentification> andList : andCombLicenseList) {
			boolean _break = false;
			for (ProjectIdentification bean : andList) {
				if (hasSelected) {
					bean.setExcludeYn(CoConstDef.FLAG_YES);
				} else {
					List<Integer> containsList = CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive) ? 
								permissiveListPSM : CoConstDef.CD_LICENSE_TYPE_WCP.equals(finalPermissive) ? 
									permissiveListWCP : CoConstDef.CD_LICENSE_TYPE_CP.equals(finalPermissive) ? 
											permissiveListCP : CoConstDef.CD_LICENSE_TYPE_PF.equals(finalPermissive) ? 
													permissiveListPF : CoConstDef.CD_LICENSE_TYPE_NA.equals(finalPermissive) ? 
															permissiveListNA : new ArrayList<>();

					if (containsList.contains(idx)) {
						bean.setExcludeYn(CoConstDef.FLAG_NO);
						_break = true;
					} else {
						bean.setExcludeYn(CoConstDef.FLAG_YES);
					}
					//bean.setExcludeYn(containsList.contains(idx) ? CoConstDef.FLAG_NO : CoConstDef.FLAG_YES);
				}
				resultList.add(bean);
			}
			
			if (_break) {
				hasSelected = true;
			}
			
			idx++;
		}
		
		return resultList;
	}
	
	private static String getLicensePermissive(List<ProjectIdentification> andList) {
		String rtnVal = "";
		
		for (ProjectIdentification bean : andList) {
			// 가장 Strong한 라이선스부터 case
			switch (bean.getLicenseType()) {
				case CoConstDef.CD_LICENSE_TYPE_NA:
					rtnVal = CoConstDef.CD_LICENSE_TYPE_NA;
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PF:
					if (!CoConstDef.CD_LICENSE_TYPE_NA.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_PF;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_CP:
					if (!CoConstDef.CD_LICENSE_TYPE_NA.equals(rtnVal)
							&& !CoConstDef.CD_LICENSE_TYPE_PF.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_CP;
					}
				
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if (!CoConstDef.CD_LICENSE_TYPE_NA.equals(rtnVal)
							&& !CoConstDef.CD_LICENSE_TYPE_PF.equals(rtnVal)
							&& !CoConstDef.CD_LICENSE_TYPE_CP.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_WCP;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					if (isEmpty(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_PMS;
					}
					
					break;
				default:
					break;
			}
		}
		
		return rtnVal;
	}
	
	public static String getLicensePermissiveType(List<OssLicense> andList) {
		String rtnVal = "";
		
		for (OssLicense bean : andList) {
			// 가장 Strong한 라이선스부터 case
			switch (bean.getLicenseType()) {
				case CoConstDef.CD_LICENSE_TYPE_CP:
					rtnVal = CoConstDef.CD_LICENSE_TYPE_CP;
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if (!CoConstDef.CD_LICENSE_TYPE_CP.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_WCP;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					if (isEmpty(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_PMS;
					}
					
					break;
				default:
					break;
			}
		}
		
		return rtnVal;
	}


	/**
	 * AND 조건에서는 가장 Strong한 license를 기준으로 한다. multilicense OSS 등록시 사용
	 * @param andList
	 * @return
	 */
	public static OssLicense getLicensePermissiveTypeLicense(List<OssLicense> andList) {
		OssLicense rtnVal = null;
		String currentType = "";
		boolean breakFlag = false;
		
		if (andList != null) {
			for (OssLicense bean : andList) {
				// 가장 Strong한 라이선스부터 case
				switch (bean.getLicenseType()) {
					case CoConstDef.CD_LICENSE_TYPE_NA:
						currentType = CoConstDef.CD_LICENSE_TYPE_NA;
						rtnVal = bean;
						breakFlag = true;
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_PF:
						if (!CoConstDef.CD_LICENSE_TYPE_NA.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_PF;
							rtnVal = bean;
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_CP:
						if (!CoConstDef.CD_LICENSE_TYPE_PF.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_CP;
							rtnVal = bean;
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_WCP:
						if (!CoConstDef.CD_LICENSE_TYPE_CP.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_WCP;
							rtnVal = bean;
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_PMS:
						if (isEmpty(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_PMS;
							rtnVal = bean;
						}
						
						break;
					default:
						break;
				}
				
				if (breakFlag) {
					break;
				}
			}
		}
		
		return rtnVal;
	}

	
	public static String getStringFromFile(String path) {
		return getStringFromFile(path, true);
	}
	
	public static String getStringFromFile(String path, boolean lineSeparator) {
		StringBuffer sb = new StringBuffer();
		File file = new File(path);
		
		if (file.exists() && file.isFile()) {
			BufferedReader br = null;  
			InputStreamReader isr = null; 
			FileInputStream fis = null;  
			String temp = "";
			try {
	            // 파일을 읽어들여 File Input 스트림 객체 생성
	            fis = new FileInputStream(file);
	            // File Input 스트림 객체를 이용해 Input 스트림 객체를 생서하는데 인코딩을 UTF-8로 지정
	            isr = new InputStreamReader(fis, "UTF-8");
	            // Input 스트림 객체를 이용하여 버퍼를 생성
	            br = new BufferedReader(isr);
	            // 버퍼를 한줄한줄 읽어들여 내용 추출
	            while ( (temp = br.readLine()) != null) {
	            	if (lineSeparator) {
		            	sb.append(temp).append(System.lineSeparator());
	            	} else {
		            	sb.append(temp).append("<br>");
	            	}
	            }
	        } catch (FileNotFoundException e) {
	        	log.error(e.getMessage(), e);
	             
	        } catch (Exception e) {
	             log.error(e.getMessage(), e);
	        } finally {
	        	if (fis != null) {
	        		try {fis.close();} catch (IOException e) {}
	        	}
	        	if (isr != null) {
	        		try {isr.close();} catch (IOException e) {}
	        	}
	        	if (br != null) {
	        		try {br.close();} catch (IOException e) {}
	        	}
	        }
		} else {
			log.error("파일이 존재하지 않습니다. path : " + path);
		}
		
		file = null;
		
		return sb.toString();
	}
	
	public static String getStringFromFile(String path, String ignorePathStr, List<String> checkList, List<String> ignoreList) {
		StringBuffer sb = new StringBuffer();
		File file = new File(path);
		if (file.exists() && file.isFile()) {
			BufferedReader br = null;  
			InputStreamReader isr = null; 
			FileInputStream fis = null;  
			String temp = "";
			List<String> duplicationCheckList = new ArrayList<>();
			
			try {
	            // 파일을 읽어들여 File Input 스트림 객체 생성
	            fis = new FileInputStream(file);
	            // File Input 스트림 객체를 이용해 Input 스트림 객체를 생서하는데 인코딩을 UTF-8로 지정
	            isr = new InputStreamReader(fis, "UTF-8");
	            // Input 스트림 객체를 이용하여 버퍼를 생성
	            br = new BufferedReader(isr);
	            // 버퍼를 한줄한줄 읽어들여 내용 추출
	            while ( (temp = br.readLine()) != null) {
	            	if (duplicationCheckList.contains(temp.trim())) {
	            		continue;
	            	}
	            	if (checkExceptionWords(temp, ignorePathStr, checkList, ignoreList)) {
	            		String tmp = splitExceptionWords(temp, checkList);
	            		sb.append(tmp).append(System.lineSeparator());
	            	}
	            	duplicationCheckList.add(temp.trim());
	            }
	        } catch (FileNotFoundException e) {
	        	log.error(e.getMessage(), e);
	             
	        } catch (Exception e) {
	             log.error(e.getMessage(), e);
	        } finally {
	        	if (fis != null) {
	        		try {fis.close();} catch (IOException e) {}
	        	}
	        	if (isr != null) {
	        		try {isr.close();} catch (IOException e) {}
	        	}
	        	if (br != null) {
	        		try {br.close();} catch (IOException e) {}
	        	}
	        }
			
		} else {
			log.error("파일이 존재하지 않습니다. path : " + path);
		}
		
		file = null;
		
		return sb.toString();
	}
	
	private static String splitExceptionWords(String temp, List<String> checkList) {
		String tmp = "";
		String filePath = "";
		String filePathTemp = "";
		
		boolean checkFlag = false;
		int filePathIdx = temp.indexOf(":", 0);
		
		if (filePathIdx > -1) {
			filePath = temp.substring(0, filePathIdx);
			filePathTemp = temp.substring(filePathIdx+1, temp.length());
		} else {
			filePathTemp = temp;
		}
		
		int tempLength = filePathTemp.length();
		
		for (String s : checkList) {
			if (filePathTemp.toUpperCase().contains(s.toUpperCase())) {
				int idx = filePathTemp.toUpperCase().indexOf(s.toUpperCase(), 0);
				int subIdx = tempLength - idx;
				String front = "";
				String end = "";
				
				if (idx <= 30) {
					front = filePathTemp.substring(0, idx);
				} else {
					front = filePathTemp.substring(idx-30, idx);
				}
				if (subIdx <= 30) {
					if (subIdx == tempLength) {
						end = filePathTemp.substring(idx, tempLength);
					} else {
						end = filePathTemp.substring(idx > 0 ? idx-1 : idx, tempLength);
					}
				} else {
					end = filePathTemp.substring(idx > 0 ? idx-1 : idx, idx+30);
				}
				if (!isEmpty(filePath)) {
					tmp = filePath + ":" + front + end;
				} else {
					tmp = front + end;
				}
				checkFlag = true;
				break;
			}
		}
		if (!checkFlag) {
			tmp = filePathTemp;
		}
		return tmp;
	}

	private static boolean checkExceptionWords(String line, String ignorePathStr, List<String> checkList, List<String> ignoreList) {
		line = line.replaceAll(ignorePathStr, "");
		line = line.toUpperCase();
		
		for (String s : ignoreList) {
			if (line.replaceAll(" ", "").replaceAll("\t", "").contains(s.toUpperCase().replaceAll(" ", "").replaceAll("\t", ""))) {
				return false;
			}
		}
		
		for (String s : checkList) {
			if (line.contains(s.toUpperCase())) {
				return true;
			}
		}
		
		return false;
	}
	
//	public static String getFileEncode(String filePath) {
//		String encoding = null;
//		java.io.FileInputStream fis = null;
//		
//		try {
//			byte[] buf = new byte[4096];
//		    String fileName = filePath;
//		    fis = new java.io.FileInputStream(fileName);
//
//		    UniversalDetector detector = new UniversalDetector(null);
//
//		    int nread;
//		    
//		    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
//		      detector.handleData(buf, 0, nread);
//		    }
//		    
//		    detector.dataEnd();
//
//		    encoding = detector.getDetectedCharset();
//		    detector.reset();			
//		} catch (Exception e) {
//		} finally {
//			if (fis != null) {
//				try {
//					fis.close();
//				} catch (Exception e2) {
//				}
//			}
//		}
//		
//	    return encoding;
//	}
	
	public static String sortLicenseName(List<OssLicense> ossLicenses) {
		List<String> l = new ArrayList<>();
		
		if (ossLicenses != null && !ossLicenses.isEmpty()){
			for (OssLicense bean : ossLicenses) {
				l.add(avoidNull(bean.getLicenseId()));
			}			
		}
		
		Collections.sort(l);
		
		String rtn = "";
		for (String s : l) {
			rtn += (!isEmpty(s) ? "|" : "") + s;
		}
		
		return rtn;
	}
	
	public static String getShortIdentify(String licenseName) {
		if (CoCodeManager.LICENSE_INFO.containsKey(licenseName)) {
			LicenseMaster bean = CoCodeManager.LICENSE_INFO.get(licenseName);
			
			return !isEmpty(bean.getShortIdentifier()) ? bean.getShortIdentifier() : licenseName;
		}
		
		return licenseName;
	}
	
	public static String getLicenseIdByName(String licenseName) {
		if (!isEmpty(licenseName)) {
			licenseName = licenseName.trim().toUpperCase();
			
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName)) {
				return CoCodeManager.LICENSE_INFO_UPPER.get(licenseName).getLicenseId();
			}
		}

		return null;
	}
	
	public static String getLicenseNameById(String licenseId, String licenseName) {
		if (!isEmpty(licenseId)) {
			if (CoCodeManager.LICENSE_INFO_BY_ID.containsKey(licenseId)) {
				LicenseMaster licenseInfo = CoCodeManager.LICENSE_INFO_BY_ID.get(licenseId);
				if (licenseInfo != null) {
					// short identify를 최우선
					if (!isEmpty(licenseInfo.getShortIdentifier())) {
						return licenseInfo.getShortIdentifier();
					}
					
					if (!isEmpty(licenseInfo.getLicenseNameTemp())) {
						// 정식 명칭으로 변환
						return licenseInfo.getLicenseNameTemp();
					}
				}
			}
		}

		return licenseName;
	}
	
	public static List<OssLicense> changeLicenseNameToShort(List<OssLicense> list) {
		if (list != null) {
			for (OssLicense bean : list) {
				if (CoCodeManager.LICENSE_INFO.containsKey(bean.getLicenseName()) && !isEmpty(CoCodeManager.LICENSE_INFO.get(bean.getLicenseName()).getShortIdentifier())) {
					bean.setLicenseName(CoCodeManager.LICENSE_INFO.get(bean.getLicenseName()).getShortIdentifier());
				}
			}
		}
		
		return list;
	}
	

	public static Map<String, Object> mergeGridAndReport(List<ProjectIdentification> ossComponents,
			List<List<ProjectIdentification>> ossComponentsLicense, List<ProjectIdentification> addComponents, String readType) {
		return makeGridDataFromReport(ossComponents, ossComponentsLicense, addComponents, null, null, readType);
	}
	
	public static Map<String, Object> convertToProjectIdentificationList(List<OssComponents> reportData, String fileSeq) {
		Map<String, Object> resultMap = new HashMap<>();
		List<ProjectIdentification> resultList = new ArrayList<>();
		List<String> versionChangeList = new ArrayList<>(); // version 정보 변경 이력 정보
		List<String> versionChangeCheckList = new ArrayList<>(); // 중복 체크용

		ProjectIdentification gridBean;
		ProjectIdentification gridLicenseBean;
		int keyCnt = 1;
		
		for (OssComponents bean : reportData) {
			gridBean = new ProjectIdentification();
			gridBean.setGridId(CoConstDef.GRID_NEWROW_DEFAULT_PREFIX+fileSeq +"f"+keyCnt++);
			gridBean.setComponentId(bean.getComponentId());
			gridBean.setReferenceId(bean.getReferenceId());
			gridBean.setReferenceDiv(bean.getReferenceDiv());
			gridBean.setOssId(bean.getOssId());
			gridBean.setOssName(bean.getOssName());
			// oss version이 정수형인 경우 분석결과서 서식에 따라, ".0" 이 사라지는 경우를 위해 
			// 기 등록된 oss에 존재하는 경우 자동으로 .0을 채워줌
			if (!isEmpty(bean.getOssName()) && !isEmpty(bean.getOssVersion()) && StringUtil.isNumeric(bean.getOssVersion())) {
				String _test = bean.getOssName().trim() + "_" + bean.getOssVersion().trim();
				String _test2 = bean.getOssName().trim() + "_" + bean.getOssVersion().trim() + ".0";
				if (!CoCodeManager.OSS_INFO_UPPER.containsKey(_test.toUpperCase()) 
						&& CoCodeManager.OSS_INFO_UPPER.containsKey(_test2.toUpperCase())) {
					if (!versionChangeCheckList.contains(bean.getOssName() + "_" + bean.getOssVersion())) {
						versionChangeCheckList.add(bean.getOssName() + "_" + bean.getOssVersion());
						versionChangeList.add(bean.getOssName() + " : " + bean.getOssVersion() + " => " + bean.getOssVersion().trim() + ".0");
					}
					
					bean.setOssVersion(bean.getOssVersion().trim() + ".0");
				}
			}
			
			gridBean.setOssVersion(bean.getOssVersion());
			if (!isEmpty(bean.getDownloadLocation())) gridBean.setDownloadLocation(bean.getDownloadLocation().replaceAll("<[^>]*>", ""));
			if (!isEmpty(bean.getHomepage())) gridBean.setHomepage(bean.getHomepage().replaceAll("<[^>]*>", ""));
			gridBean.setFilePath(bean.getFilePath());
			gridBean.setExcludeYn(bean.getExcludeYn());
			gridBean.setBinaryName(bean.getBinaryName());
			gridBean.setBinaryNotice(bean.getBinaryNotice());
			gridBean.setCustomBinaryYn(bean.getCustomBinaryYn());
			gridBean.setRefPartnerId(bean.getRefPartnerId());
			gridBean.setRefPrjId(bean.getRefPrjId());
			gridBean.setRefComponentId(bean.getRefComponentId());
			gridBean.setMergePreDiv(bean.getMergePreDiv());
			gridBean.setObligationType(bean.getObligationType());
			gridBean.setBatStringMatchPercentage(bean.getBatStringMatchPercentage());
			gridBean.setBatPercentage(bean.getBatPercentage());
			gridBean.setBatScore(bean.getBatScore());
			gridBean.setLicenseDiv((bean.getOssComponentsLicense() != null && bean.getOssComponentsLicense().size() > 1) ? CoConstDef.LICENSE_DIV_MULTI : CoConstDef.LICENSE_DIV_SINGLE);
			
			gridBean.setCopyrightText(bean.getCopyrightText());
			gridBean.setComments(bean.getComments());
			
			if (!isEmpty(bean.getOssNickName())) {
				gridBean.setOssNickName(bean.getOssNickName());
			}
			
			if (!isEmpty(bean.getDependencies())) {
				gridBean.setDependencies(bean.getDependencies());
			}
			
			if (!isEmpty(bean.getCheckSum())) {
				gridBean.setCheckSum(bean.getCheckSum());
			}
			
			if (!isEmpty(bean.getTlsh())) {
				gridBean.setTlsh(bean.getTlsh());
			}
			
			if (!isEmpty(bean.getPackageUrl())) gridBean.setPackageUrl(bean.getPackageUrl());
			
			// license 
			int licenseIdx = 1;
			if (bean.getOssComponentsLicense() != null) {
				String licenseText = "";
				
				for (OssComponentsLicense license : bean.getOssComponentsLicense()) {
					gridLicenseBean = new ProjectIdentification();
					gridLicenseBean.setGridId(gridBean.getGridId() + "-" + licenseIdx++);
					gridLicenseBean.setComponentLicenseId(license.getComponentLicenseId());
					gridLicenseBean.setComponentId(license.getComponentId());
					gridLicenseBean.setLicenseId(license.getLicenseId());
					gridLicenseBean.setLicenseName(CommonFunction.getShortIdentify(license.getLicenseName()));
					gridLicenseBean.setLicenseText(license.getLicenseText());
					gridLicenseBean.setCopyrightText(license.getCopyrightText());
					gridLicenseBean.setExcludeYn(license.getExcludeYn());
					gridLicenseBean.setOssLicenseComb(license.getOssLicenseComb());
					gridLicenseBean.setEditable(license.getEditable());
					gridBean.addComponentLicenseList(gridLicenseBean);
					

					if (!isEmpty(license.getLicenseText())) {
						licenseText += (!isEmpty(licenseText) ? "\r\n" : "") + license.getLicenseText();
					}				
				}
				
				gridBean.setLicenseText(licenseText);
				
				if (CoConstDef.LICENSE_DIV_MULTI.equals(gridBean.getLicenseDiv())) {
					gridBean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(gridBean.getComponentLicenseList(), ","));
				} else {
					gridBean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(gridBean.getComponentLicenseList()));
				}
				
			}
			
			resultList.add(gridBean);
		}
	
		if (!versionChangeList.isEmpty()) {
			resultMap.put("versionChangeList", versionChangeList);
		}
		
		resultMap.put("resultList", resultList);
		
		return resultMap;
	}
	
	public static Map<String, Object> makeGridDataFromReport(List<OssComponents> reportData, String fileSeq) {
		return makeGridDataFromReport(null, null, reportData, fileSeq);
	}


	public static Map<String, Object> makeGridDataFromReport(List<ProjectIdentification> ossComponents,
			List<List<ProjectIdentification>> ossComponentsLicense, List<OssComponents> reportData, String fileSeq) {
		return makeGridDataFromReport(ossComponents, ossComponentsLicense, null, reportData, fileSeq, "");
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> makeGridDataFromReport(List<ProjectIdentification> ossComponents,
			List<List<ProjectIdentification>> ossComponentsLicense,  List<ProjectIdentification> addOssComponents,
			List<OssComponents> reportData, String fileSeq, String readType) {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> subMap = new HashMap<>();

		ProjectIdentification gridLicenseBean;
		List<ProjectIdentification> mainGridList = ossComponents != null ? ossComponents : new ArrayList<>();
		// single license 의 경우 메인grid로 처리하기 때문에 다시 license정보를 생성해줘야함
		for (ProjectIdentification bean : mainGridList) {
			if (CoConstDef.LICENSE_DIV_SINGLE.equals(bean.getLicenseDiv())) {
				gridLicenseBean = new ProjectIdentification();
				gridLicenseBean.setComponentLicenseId(bean.getComponentLicenseId());
				gridLicenseBean.setComponentId(bean.getComponentId());
				gridLicenseBean.setLicenseId(bean.getLicenseId());
				gridLicenseBean.setLicenseName(bean.getLicenseName());
				gridLicenseBean.setLicenseText(bean.getLicenseText());
				gridLicenseBean.setCopyrightText(bean.getCopyrightText());
				gridLicenseBean.setExcludeYn(bean.getExcludeYn());
				gridLicenseBean.setGridId(bean.getGridId()+"-1");
				List<ProjectIdentification> licenseList = new ArrayList<>();
				licenseList.add(gridLicenseBean);
				subMap.put(bean.getGridId(), licenseList);
			}
		}
		
		if (ossComponentsLicense != null) {
			for (List<ProjectIdentification> licenseList : ossComponentsLicense) {
				String gridId = licenseList.get(0).getGridId();
				String key = licenseList.get(0).getGridId().split(gridId.indexOf("-") > -1 ? "-" : "_")[0];
				subMap.put(key, licenseList);
			}
		}
		
		if (addOssComponents != null) {
			String key = "";
			for (ProjectIdentification bean : addOssComponents) {
				String key2 = bean.getBinaryName()+ "-" + bean.getOssName() + "-" + bean.getOssVersion();
				if (key.equals(key2)) {
					ProjectIdentification result = mainGridList.get(mainGridList.size()-1);
					result.setLicenseName(result.getLicenseName() + "," + bean.getLicenseName());
					
					mainGridList.set(mainGridList.size()-1, result);
				}else {
					bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
					
					mainGridList.add(bean);
					key = key2;
				}
				
				if (bean.getComponentLicenseList() != null) {
					subMap.put(bean.getGridId(), bean.getComponentLicenseList());
				}
			}

		}
		
		if (reportData != null) {
			Map<String, Object> convertObj = convertToProjectIdentificationList(reportData, fileSeq);
			List<ProjectIdentification> _list = (List<ProjectIdentification>) convertObj.get("resultList");
			
			if (_list != null) {
				Map<String, ProjectIdentification> sortMap = new TreeMap<String, ProjectIdentification>();
				
				for (ProjectIdentification gridBean : _list) { // 중복제거 및 정렬
					String key = "";
					if ("BIN".equals(readType.toUpperCase()) || "BINANDROID".equals(readType.toUpperCase()) || "PARTNER".equals(readType.toUpperCase())) {
						if (!isEmpty(gridBean.getFilePath()) && isEmpty(gridBean.getBinaryName())) {
							gridBean.setBinaryName(gridBean.getFilePath());
						}
						
						key = gridBean.getBinaryName() + "-" + gridBean.getOssName() + "-" + gridBean.getOssVersion() + "-" + gridBean.getLicenseName() + "-" 
								+ gridBean.getDownloadLocation() + "-" + gridBean.getHomepage() + "-" + gridBean.getCopyrightText() + "-" + gridBean.getExcludeYn();
					} else {
						if (isEmpty(gridBean.getFilePath()) && !isEmpty(gridBean.getBinaryName())) {
							gridBean.setFilePath(gridBean.getBinaryName());
						}
						
						key = gridBean.getFilePath() + "-" + gridBean.getOssName() + "-" + gridBean.getOssVersion() + "-" + gridBean.getLicenseName() + "-" 
								+ gridBean.getDownloadLocation() + "-" + gridBean.getHomepage() + "-" + gridBean.getCopyrightText() + "-" + gridBean.getExcludeYn();
					}
					
					if (!sortMap.keySet().contains(key)) {
						List<ProjectIdentification> resultLicenseList = new ArrayList<ProjectIdentification>();
						List<String> duplicateLicense = new ArrayList<String>();
						
						if (gridBean.getComponentLicenseList() != null) {
							for (ProjectIdentification licenseList : gridBean.getComponentLicenseList()) {
								if (licenseList.getLicenseName().contains(",")){
									ProjectIdentification copyLicenseList = null;
									String[] licenses = licenseList.getLicenseName().split(",");
									for (String li : licenses) {
										if (!duplicateLicense.contains(StringUtil.trim(li).toUpperCase())) {
											try {
												copyLicenseList = licenseList.copy();
												
												copyLicenseList.setLicenseName(StringUtil.trim(li));
												resultLicenseList.add(copyLicenseList);
												duplicateLicense.add(licenseList.getLicenseName());
											} catch (CloneNotSupportedException e) {
												log.error(e.getMessage());
											}
										}
									}
								}else {
									if (!duplicateLicense.contains(licenseList.getLicenseName()) && CoConstDef.FLAG_NO.equals(avoidNull(licenseList.getExcludeYn(), CoConstDef.FLAG_NO))) {
										resultLicenseList.add(licenseList);
										duplicateLicense.add(StringUtil.trim(licenseList.getLicenseName()).toUpperCase());
									}
								}
							}
						}
						
						gridBean.setComponentLicenseList(resultLicenseList);
						sortMap.put(key, gridBean);
					}
				}
				
				String key = "";
				String key3 = "";
				for (ProjectIdentification gridBean : sortMap.values()) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO.get(gridBean.getLicenseName());
					String ossName = isEmpty(gridBean.getOssName()) ? "-" : gridBean.getOssName();
					String key2 = "";
					String key4 = ossName + "-" + (licenseMaster != null ? licenseMaster.getLicenseType() : "");
					String gridId = "";
					
					if ("BIN".equals(readType.toUpperCase()) || "BINANDROID".equals(readType.toUpperCase()) || "PARTNER".equals(readType.toUpperCase())) {
						key2 = gridBean.getBinaryName() + "-" + ossName + "-" + gridBean.getOssVersion() + "-" + gridBean.getExcludeYn();
					}else {
						key2 = gridBean.getFilePath() + "-" + ossName + "-" + gridBean.getOssVersion() + "-" + gridBean.getExcludeYn();
					}
					
					if (!"-".equals(ossName) 
							&& key.equals(key2)
							&& !"--NA".equals(key3)
							&& !"--NA".equals(key4)) { // 단, OSS Name: - 이면서, License Type: Proprietary이 아닌 경우 Row를 합치지 않음.
						ProjectIdentification result = mainGridList.get(mainGridList.size()-1);
						
						if (!result.getLicenseName().contains(StringUtil.trim(gridBean.getLicenseName()))) {
							String resultLicenseName = StringUtil.trim(result.getLicenseName());
							String licenseNameList = StringUtil.trim(gridBean.getLicenseName());
							if (licenseNameList.contains(",")) {
								for (String licensename : licenseNameList.split(",")) {
									if (!resultLicenseName.contains(licensename) && !isEmpty(licensename)) {
										resultLicenseName += "," + licensename;
									}
								}
							}else {
								resultLicenseName += isEmpty(licenseNameList) ? "" : ("," + licenseNameList);
							}
							
							result.setLicenseName(resultLicenseName);
							
							mainGridList.set(mainGridList.size()-1, result);
							gridId = result.getGridId();
						}
					}else {
						mainGridList.add(gridBean);
						key = key2;
						key3 = key4;
					}
					
					if (gridBean.getComponentLicenseList() != null) {
						if (isEmpty(gridId)) {
							subMap.put(gridBean.getGridId(), gridBean.getComponentLicenseList());
						}else {
							List<ProjectIdentification> result = (List<ProjectIdentification>) subMap.get(gridId);
							for (ProjectIdentification subData : gridBean.getComponentLicenseList()) {
								for (ProjectIdentification resultData : result) {
									if (!resultData.getLicenseName().equals(resultData.getLicenseName())) {
										result.add(subData);
										break;
									}
								}
							}
							if (result.size() > 0) {
								subMap.replace(gridId, result);
							}else {
								subMap.replace(gridId, gridBean.getComponentLicenseList());
							}
						}
					}
				}
			}
			
			// version 정보가 자동으로 변경된 Data 체크
			if (convertObj.containsKey("versionChangeList")) {
				resultMap.put("versionChangedList",convertObj.get("versionChangeList"));
			}
						
		}
		resultMap.put("subData", subMap);
		resultMap.put("mainData", mainGridList);

		return resultMap;
	}
	
	public static String makeLicenseObligationStr(String obligationChecks) {
		String rtnStr = "";
		
		if (avoidNull(obligationChecks).length() == 3) {
			// 우선순위가 높은 순으로
			if (CoConstDef.FLAG_YES.equals(obligationChecks.substring(2))) {
				rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK);
			} else if (CoConstDef.FLAG_YES.equals(obligationChecks.substring(1, 2))) {
				rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
			} else if (CoConstDef.FLAG_YES.equals(obligationChecks.substring(0, 1))) {
				rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_NOTICE);
			}
		}
		
		return rtnStr;
	}

	public static String makeLicenseObligationStr(String notice, String disclose, String needsCheck) {
		return makeLicenseObligationStr(avoidNull(notice, CoConstDef.FLAG_NO) + avoidNull(disclose, CoConstDef.FLAG_NO) + avoidNull(needsCheck, CoConstDef.FLAG_NO));
	}
	
	public static String makeCategoryFormat(String distributeTarget, String mainCategoryCd, String subCategoryCode) {
		String categoryCode = CoConstDef.CD_MODEL_TYPE;
		
		return  CoCodeManager.getCodeString(categoryCode, mainCategoryCd) + " > " + CoCodeManager.getCodeString(CoCodeManager.getSubCodeNo(categoryCode, mainCategoryCd), subCategoryCode);
	}
	
	public static String makeCategoryFormat(String distributeTarget, String categoryCd) {
		if (!isEmpty(categoryCd) && categoryCd.length() == 6) {
			return makeCategoryFormat(distributeTarget, categoryCd.substring(0,3), categoryCd.substring(3));
		}
		
		return "";
	}

	public static boolean isSameLicense(List<OssComponentsLicense> list1,
			List<OssComponentsLicense> list2) {
		if (list1 == null && list2 == null) {
			return false;
		}
		
		if ((list1 == null && list2 != null) || (list1 != null && list2 == null)) {
			return false;
		}
		
		if (list1.size() != list2.size()) {
			return false;
		}
		
		List<String> licenseNames = new ArrayList<>();
		
		for (OssComponentsLicense bean : list1) {
			LicenseMaster liMaster = CoCodeManager.LICENSE_INFO.get(bean.getLicenseName());
			if (liMaster == null) {
				licenseNames.add(avoidNull( bean.getLicenseName()));
			} else {
				licenseNames.add(liMaster.getLicenseName());
				if (!isEmpty(liMaster.getShortIdentifier())) {
					licenseNames.add(liMaster.getShortIdentifier());
				}
				if (liMaster.getLicenseNicknameList() != null && !liMaster.getLicenseNicknameList().isEmpty()) {
					licenseNames.addAll(liMaster.getLicenseNicknameList());
				}
			}
		}
		
		for (OssComponentsLicense bean : list2) {
			if (!licenseNames.contains(bean.getLicenseName())) {
				return false;
			}
		}
		
		return true;
	}
	
	public static Map<String, Object> makeSecurityGridDataFromReport(List<OssComponents> ossComponents, List<OssComponents> reportData, String fileSeq, String readType) {
		Map<String, Object> resultMap = new HashMap<>();
		
		if (reportData != null) {
			Map<String, OssComponents> reportMap = new HashMap<>();
			for (OssComponents report : reportData) {
				String key = (report.getOssName() + "_" + report.getOssVersion() + "_" + report.getCveId()).toUpperCase();
				reportMap.put(key, report);
			}
			
			for (OssComponents bean : ossComponents) {
				String key = (bean.getOssName() + "_" + bean.getOssVersion() + "_" + bean.getCveId()).toUpperCase();
				
				if (reportMap.containsKey(key)) {
					OssComponents reportBean = reportMap.get(key);
					
					String vulnerabilityResolution = reportBean.getVulnerabilityResolution();
					String securityPatchLink = reportBean.getSecurityPatchLink();
					String securityComments = reportBean.getSecurityComments();
					
					if (!isEmpty(vulnerabilityResolution)) {
						bean.setVulnerabilityResolution(vulnerabilityResolution);
					}
					if (!isEmpty(securityPatchLink)) {
						if (!avoidNull(vulnerabilityResolution).equalsIgnoreCase("fixed")) {
							bean.setSecurityPatchLink("N/A");
						} else {
							bean.setSecurityPatchLink(securityPatchLink);
						}
					} else {
						bean.setSecurityPatchLink("N/A");
					}
					
					bean.setSecurityComments(avoidNull(securityComments));
				}
			}	
		}
		
		resultMap.put("totalGridData", ossComponents);

		return resultMap;
	}
	
	public static Object identificationSortByValidInfo(List<ProjectIdentification> list, Map<String, String> validMap, Map<String, String> validDiffMap) {
		return identificationSortByValidInfo(list, validMap, validDiffMap, null, false, false);
	}
	
	public static Object identificationSortByValidInfo(List<ProjectIdentification> list, Map<String, String> validMap, Map<String, String> validDiffMap, boolean hideObligation) {
		return identificationSortByValidInfo(list, validMap, validDiffMap, null, hideObligation, false);
	}
	
	public static Object identificationSortByValidInfo(List<ProjectIdentification> list, Map<String, String> validMap, Map<String, String> validDiffMap, Map<String, String> validInfoMap, boolean hideObligation) {
		return identificationSortByValidInfo(list, validMap, validDiffMap, validInfoMap, hideObligation, false);
	}
	
	public static Object identificationSortByValidInfo(List<ProjectIdentification> list, Map<String, String> validMap, Map<String, String> validDiffMap, Map<String, String> validInfoMap, boolean hideObligation, boolean RestrictionFlag) {
		List<ProjectIdentification> sortList = new ArrayList<>();
		List<ProjectIdentification> sortList2 = new ArrayList<>();
		List<ProjectIdentification> sortListOk = new ArrayList<>();
		Map<String, ProjectIdentification> sortMap = new HashMap<>();
		
		List<String> hideObligationColumns = Arrays.asList(new String[]{"OSSNAME", "OSSVERSION", "LICENSENAME"});
		
		// validation 결과에서 componentId별 error가 발생한 칼럼 정보를 격납한다.
		Map<String, List<String>> errorMap = new HashMap<>(); // error level
		Map<String, List<String>> restrictionMap = new HashMap<>(); // restriction level
		Map<String, List<String>> warningMap = new HashMap<>(); // warning level
		Map<String, List<String>> warningVerMap = new HashMap<>(); // warning level (oss version)
		Map<String, List<String>> infoSameMap = new HashMap<>(); // info level (same & similar binary > include license)
		Map<String, List<String>> infoBiMap = new HashMap<>(); // info level (new bianry)
		Map<String, List<String>> infoModifyMap = new HashMap<>(); // info level (modified = new + tlsh > 120)
		Map<String, List<String>> infoOnlyMap = new HashMap<>(); // info level
		Map<String, List<String>> infoCopyrightMap = new HashMap<>(); // info level (copyright)
		Map<String, String> hideObligationIdList = new HashMap<>();
		
		if (RestrictionFlag) {
			for (ProjectIdentification p : list) {
				if (!StringUtil.isEmpty(p.getRestriction()) && CoConstDef.FLAG_NO.equals(p.getExcludeYn())) {
					restrictionMap.put(avoidNull(p.getGridId(), p.getComponentId()), new ArrayList<String>());
				}
			}
		}
		
		if (validMap != null && !validMap.isEmpty()) {
			for (String errKey : validMap.keySet()) {
				if (errKey.indexOf(".") > -1) {
					String _key = errKey.substring(errKey.indexOf(".") + 1, errKey.length());
					if (errorMap.containsKey(_key)) {
						List<String> _list = errorMap.get(_key);
						_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
						errorMap.replace(_key, _list);
					} else {
						List<String> _list = new ArrayList<>();
						_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
						errorMap.put(_key, _list);
					}
					/*
					if (hideObligation) {
						if (hideObligationColumns.contains(errKey.substring(0, errKey.indexOf(".")).toUpperCase())) {
//							hideObligationIdList.put(_key, validMap.get(errKey));
							hideObligationIdList.put(_key, getMessage("msg.project.obligation.unclear"));
						}
					}
					*/
				}
			}			
		}

		
		// warning message 가 포함되어 있는 경우 정렬 (우선순위 3) or oss version warning message "Required" (Priority : 4)
		if (validDiffMap != null && !validDiffMap.isEmpty()) {
			for (String errKey : validDiffMap.keySet()) {
				if (errKey.indexOf(".") > -1) {
					String msg = validDiffMap.get(errKey);
					String _key = errKey.substring(errKey.indexOf(".") + 1, errKey.length());
					
//					if (!CommonFunction.isAdmin()) {
//						if ((errKey.startsWith("ossName") && (msg.startsWith("Deactivated") || msg.startsWith("")))
//								|| (errKey.startsWith("ossVersion") && msg.startsWith(""))
//								|| (errKey.startsWith("licenseName") && (msg.startsWith("") || msg.startsWith("errLv")))) {
//							if (errorMap.containsKey(_key)) {
//								List<String> _list = errorMap.get(_key);
//								_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
//								errorMap.replace(_key, _list);
//							} else {
//								List<String> _list = new ArrayList<>();
//								_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
//								errorMap.put(_key, _list);
//							}
//						}
//					}
					
					// oss version warning message "Required" (Priority : 4)
					if (errKey.startsWith("ossVersion") && msg.equals("Required.")) {
						if (warningVerMap.containsKey(_key)) {
							List<String> _list = warningVerMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							warningVerMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							warningVerMap.put(_key, _list);
						}
					} else {// warning message 가 포함되어 있는 경우 정렬 (우선순위 3)
						String addValue = "";
						if (errKey.startsWith("binaryNotice")) {
							if (msg.equalsIgnoreCase("Found binary in notice.html")) {
								addValue = "FIND";
							} else if (msg.equalsIgnoreCase("Can't find binary in notice.htm")) {
								addValue = "NOTFIND";
							} else if (msg.equalsIgnoreCase("NOTICE Should be \"ok\" in case OSS is used")) {
								addValue = "PERMISSIVE";
							}
						}
						if (warningMap.containsKey(_key)) {
							List<String> _list = warningMap.get(_key);
							String warningValue = errKey.substring(0, errKey.indexOf(".")).toUpperCase();
							_list.add(!isEmpty(addValue) ? warningValue + "_" + addValue : warningValue);
							warningMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							String warningValue = errKey.substring(0, errKey.indexOf(".")).toUpperCase();
							_list.add(!isEmpty(addValue) ? warningValue + "_" + addValue : warningValue);
							warningMap.put(_key, _list);
						}
					}
					
					
					/*
					if (hideObligation) {
						if (hideObligationColumns.contains(errKey.substring(0, errKey.indexOf(".")).toUpperCase())) {
//							hideObligationIdList.put(_key, validDiffMap.get(errKey));
							hideObligationIdList.put(_key, getMessage("msg.project.obligation.unclear"));
						}
					}
					*/
					
				}
			}
		}
		
		// info message가 포함되어 있는 경우 정렬 (우선순위 6)
		if (validInfoMap != null && !validInfoMap.isEmpty()) {
			for (String errKey : validInfoMap.keySet()) {
				if (errKey.indexOf(".") > -1) {
					String _key = errKey.substring(errKey.indexOf(".") + 1, errKey.length());
					
					if (hideObligation) {
						if (hideObligationColumns.contains(errKey.substring(0, errKey.indexOf(".")).toUpperCase())) {
//							hideObligationIdList.put(_key, validInfoMap.get(errKey));
							hideObligationIdList.put(_key, getMessage("msg.project.obligation.unclear"));
						}
					}
					
					// info level message 중에서 new binary message는 info level 상단으로 정렬하기 위해 우선순위 5로 구분하여 격납함
					if (errKey.startsWith("binaryName.") && (validInfoMap.get(errKey).toUpperCase().startsWith("SAME") || validInfoMap.get(errKey).toUpperCase().startsWith("SIMILAR"))) {
						if (infoSameMap.containsKey(_key)) {
							List<String> _list = infoSameMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoSameMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoSameMap.put(_key, _list);
						}
					} else if (errKey.startsWith("binaryName.") && "NEW".equalsIgnoreCase(validInfoMap.get(errKey))) {
						if (infoBiMap.containsKey(_key)) {
							List<String> _list = infoBiMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoBiMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoBiMap.put(_key, _list);
						}
					} else if (errKey.startsWith("binaryName.") && validInfoMap.get(errKey).toUpperCase().startsWith("MODIFIED")) {
						if (infoModifyMap.containsKey(_key)) {
							List<String> _list = infoModifyMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoModifyMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoModifyMap.put(_key, _list);
						}
					} else if (errKey.startsWith("copyrightText.") && validInfoMap.get(errKey).toUpperCase().startsWith("NOT")) {
						if (infoCopyrightMap.containsKey(_key)) {
							List<String> _list = infoCopyrightMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoCopyrightMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoCopyrightMap.put(_key, _list);
						}
					} else {
						if (infoOnlyMap.containsKey(_key)) {
							List<String> _list = infoOnlyMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoOnlyMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoOnlyMap.put(_key, _list);
						}
					}
				}
			}
		}
		
		// bom의 경우 우선순위가 높은 1개 row만 sort대상으로 취급
		String currentGroup = "";
		for (ProjectIdentification bean : list) {
			
			// self check case only
			if (hideObligation) {
				if (hideObligationIdList.containsKey(bean.getGridId())) {
					bean.setObligationGrayFlag(CoConstDef.FLAG_YES);
					bean.setObligationMsg(hideObligationIdList.get(bean.getGridId()));
				} 

				// Need Check license인 경우
				else if (hasNeedCheckLicense(bean, true)) {
					bean.setObligationGrayFlag(CoConstDef.FLAG_YES);
					bean.setObligationMsg(getMessage("msg.info.selfcheck.include.needcheck.licnese"));
				}
				// 선택된 license로 다시 계산해야함 obligation type
				else if (CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())) {
					bean.setObligationType(CommonFunction.getObligationTypeWithSelectedLicense(bean));
				}
			}
			
			if (!(!isEmpty(bean.getGroupingColumn()) && currentGroup.equals(bean.getGroupingColumn()))) {
				// 0(1) : error level
				if (errorMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(errorMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "0", null, validMap), bean);
				} else if (checkMultiLicenseError(errorMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(errorMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(errorMap.get(_id), _id, "0", null, validMap), bean);
				} else if (CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(bean.getObligationLicense())) {
					sortMap.put(makeValidSortKey(new ArrayList<String>(), avoidNull(bean.getGridId(), bean.getComponentId()) , "0", bean.getObligationLicense()), bean);
				}
				// 2 : Restriction
				else if (restrictionMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(restrictionMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "2"), bean);
				}
				// 3 : warning level
				else if (warningMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(warningMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "3", null, validDiffMap), bean);
				} else if (checkMultiLicenseError(warningMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(warningMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(warningMap.get(_id), _id, "3", null, validDiffMap), bean);
				}
				// 4 : warning level (oss version)
				else if (warningVerMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(warningVerMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "4", null, validDiffMap), bean);
				} else if (checkMultiLicenseError(warningVerMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(warningVerMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(warningVerMap.get(_id), _id, "4", null, validDiffMap), bean);
				}
				// 5 : info level (same binary > include license)
				else if (infoSameMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(infoSameMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "5", null, validInfoMap), bean);
				} else if (checkMultiLicenseError(infoSameMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(infoSameMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(infoSameMap.get(_id), _id, "5", null, validInfoMap), bean);
				}
				// 6 : info level (new binary)
				else if (infoBiMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(infoBiMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "6", null, validInfoMap), bean);
				} else if (checkMultiLicenseError(infoBiMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(infoBiMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(infoBiMap.get(_id), _id, "6", null, validInfoMap), bean);
				}
				// 7 : info level (Modified = new + tlsh > 120)
				else if (infoModifyMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					sortMap.put(makeValidSortKey(infoModifyMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "7", null, validInfoMap), bean);
				} else if (checkMultiLicenseError(infoModifyMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(infoBiMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(infoModifyMap.get(_id), _id, "7", null, validInfoMap), bean);
				}
				// 8 : info level
				else if (infoOnlyMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(infoOnlyMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "8", null, validInfoMap), bean);
				} else if (checkMultiLicenseError(infoOnlyMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(infoOnlyMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(infoOnlyMap.get(_id), _id, "8", null, validInfoMap), bean);
				}
				// 10 : info level (copyright)
				else if (infoCopyrightMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					sortMap.put(makeValidSortKey(infoCopyrightMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "10", null, validInfoMap), bean);
				}
				else {
					sortListOk.add(bean);
				}
			} else {
				sortListOk.add(bean);
			}
		}
		
		
		// validation 위치별 재정렬
		if (!sortMap.isEmpty()) {
			List<String> sortKeyList = sortMap.keySet().stream().collect(Collectors.toList());
			if (sortKeyList.size() > 1) {
				Collections.sort(sortKeyList, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						if (new BigDecimal(o1.split("[_]")[0]).compareTo(new BigDecimal(o2.split("[_]")[0])) > 0) {
							return 1;
						} else if (new BigDecimal(o1.split("[_]")[0]).compareTo(new BigDecimal(o2.split("[_]")[0])) == 0) {
							return 0;
						} else {
							return -1;
						}
					}
				});
			}

			for (String sortKey : sortKeyList) {
				sortList.add(sortMap.get(sortKey));
			}
		}

		// subGrid에서 오류가 있는 row
		if (!sortList2.isEmpty()) {
			sortList.addAll(sortList2);
		}
		
		// 오류가 없는 row
		if (!sortListOk.isEmpty()) {
			sortList.addAll(sortListOk);
		}
		
		return sortList;
	}
	
	private static String getObligationTypeWithSelectedLicense(ProjectIdentification identificationBean) {
		if (identificationBean != null && identificationBean.getComponentLicenseList() != null && !identificationBean.getComponentLicenseList().isEmpty()) {

			// oss master의 obligation과는 달리, exclude 이외의 license는 모두 선택중인 license로 판단한다. (AND)
			List<OssLicense> checkForObligationLicenseList = new ArrayList<>();
			
			for (ProjectIdentification bean : identificationBean.getComponentLicenseList()) {
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(bean.getLicenseName()).toUpperCase());
				
				if (master != null) {
					OssLicense license = new OssLicense();
					license.setLicenseName(master.getLicenseNameTemp());
					license.setLicenseId(master.getLicenseId());
					license.setLicenseType(master.getLicenseType());
					
					checkForObligationLicenseList.add(license);
				}
			}
			
			if (!checkForObligationLicenseList.isEmpty()) {
				OssLicense obligationLicense = CommonFunction.getLicensePermissiveTypeLicense(checkForObligationLicenseList);
				
				if (obligationLicense != null) {
					// 선택한 license 중에서 가장 Strong한 라이선스의 obligation 설정에 따라 표시
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(obligationLicense.getLicenseName()).toUpperCase());
					
					if (master != null) {
						if (CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationNeedsCheckYn()))) {
							return CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK;
						} else if (CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationDisclosingSrcYn()))) {
							return CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
						} else if (CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationNotificationYn()))) {
							return CoConstDef.CD_DTL_OBLIGATION_NOTICE;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private static boolean hasNeedCheckLicense(ProjectIdentification bean, boolean withSelectedLicense) {
		if (bean != null && bean.getComponentLicenseList() != null && !bean.getComponentLicenseList().isEmpty()) {
			for (ProjectIdentification license : bean.getComponentLicenseList()) {
				if (withSelectedLicense && CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					continue;
				}
				
				if (CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(license.getObligationType())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static String makeValidSortKey(List<String> list, String _id, String prevSortKey) {
		return makeValidSortKey(list, _id, prevSortKey, null);
	}
	
	private static String makeValidSortKey(List<String> list, String _id, String prevSortKey, String obligationType) {
		return makeValidSortKey(list, _id, prevSortKey, obligationType, null);
	}

	private static String makeValidSortKey(List<String> list, String _id, String prevSortKey, String obligationType, Map<String, String> msgMap) {
		String rtn = "";
		// compareArray 순서대로 정렬된다.
		// 정의되지 않은 경우는 정렬대상에서 제외
		String[] compareArray = new String[]{"NOTICE", "BINARYNOTICE", "BINARYNOTICE_FIND", "BINARYNOTICE_NOTFIND", "BINARYNOTICE_PERMISSIVE", "BINARYNAME", "OSSNAME", "OSSVERSION", "LICENSENAME", "LICENSETEXT", "FILEPATH", "DOWNLOADLOCATION", "HOMEPAGE"};
		Map<String, String> compareSortMap = new LinkedHashMap<>(); // [Identification] Auto ID message있는 Row 정렬 필요.
		int sortIdx = 0;
		
		for (String key : compareArray) {
			compareSortMap.put(key, StringUtil.leftPad(Integer.toString(sortIdx++), 2, "0"));
		}
		
		//String preSortKey = useNextSort ? "2" : "0";
		String preSortKey = avoidNull(prevSortKey, "0");
		boolean hasErrorCode = false;
		
		for (String key : compareSortMap.keySet()) {
			rtn += preSortKey;
			if (list.contains(key)) {
				rtn += makeValidSortValue(_id, key, compareSortMap, msgMap);
			} else {
				rtn += "999";
			}
			
			if (list.contains(key)) {
				hasErrorCode = true;
			}
		}
		
		// error code가 없고, obligation type이 need check 인 경우, error 와 warning 사이에 표시한다.
		if (!isEmpty(obligationType) && CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(obligationType) && !hasErrorCode) {
			preSortKey = "1";
			rtn = "";
			
			for (String key : compareSortMap.keySet()) {
				rtn += preSortKey + (list.contains(key) ? compareSortMap.get(key) + "0" : "999");
			}
		}
		
		rtn += "_" + _id;
		
		return rtn;
	}
	
	private static String makeValidSortValue(String _id, String key, Map<String, String> compareSortMap, Map<String, String> msgMap) {
		String rtnVal = "";
		if (msgMap != null) {
			String msgVal = "";
			switch (key) {
				case "BINARYNOTICE" :
					msgVal = msgMap.get("binaryNotice." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("Formatting")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else if (msgVal.startsWith("NOTICE Should")) {
							rtnVal += compareSortMap.get(key) + "3";
						} else if (msgVal.startsWith("Found binary")) {
							rtnVal += compareSortMap.get(key) + "4";
						} else if (msgVal.startsWith("Can't find")) {
							rtnVal += compareSortMap.get(key) + "5";
						} else {
							rtnVal += compareSortMap.get(key) + "6";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "9";
					}
					break;
				case "BINARYNAME" :
					msgVal = msgMap.get("binaryName." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("Formatting")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else if (msgVal.startsWith("Exists binary")) {
							rtnVal += compareSortMap.get(key) + "3";
						} else if (msgVal.contains("same binary")) {
							rtnVal += compareSortMap.get(key) + "4";
						} else if (msgVal.contains("similar binary")) {
							rtnVal += compareSortMap.get(key) + "5";
						} else if (msgVal.contains("binary has same OSS information")) {
							rtnVal += compareSortMap.get(key) + "6";
						} else if (msgVal.equalsIgnoreCase("New")) {
							rtnVal += compareSortMap.get(key) + "7";
						} else {
							rtnVal += compareSortMap.get(key) + "8";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "9";
					}
					break;
				case "OSSNAME" :
					msgVal = msgMap.get("ossName." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("New")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("Formatting")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else if (msgVal.startsWith("Deactivated")) {
							rtnVal += compareSortMap.get(key) + "3";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "4";
						} else if (msgVal.startsWith("Duplicated")) {
							rtnVal += compareSortMap.get(key) + "5";
						} else if (msgVal.startsWith("Already")) {
							rtnVal += compareSortMap.get(key) + "6";
						} else {
							rtnVal += compareSortMap.get(key) + "7";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "8";
					}
					break;
				case "OSSVERSION" :
					msgVal = msgMap.get("ossVersion." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("New")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("Formatting")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "3";
						} else if (msgVal.startsWith("Duplicated")) {
							rtnVal += compareSortMap.get(key) + "4";
						} else if (msgVal.startsWith("Already")) {
							rtnVal += compareSortMap.get(key) + "5";
						} else if (msgVal.startsWith("Version")) {
							rtnVal += compareSortMap.get(key) + "6";
						} else {
							rtnVal += compareSortMap.get(key) + "7";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "8";
					}
					break;
				case "LICENSENAME" :
					msgVal = msgMap.get("licenseName." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("New")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("Formatting")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else if (msgVal.startsWith("Dual license")) {
							rtnVal += compareSortMap.get(key) + "3";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "4";
						} else if (msgVal.startsWith("No license")) {
							rtnVal += compareSortMap.get(key) + "5";
						} else if (msgVal.startsWith("Specify")) {
							rtnVal += compareSortMap.get(key) + "6";
						} else {
							rtnVal += compareSortMap.get(key) + "7";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "8";
					}
					break;
				case "LICENSETEXT" :
					msgVal = msgMap.get("licenseText." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("Formatting")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else {
							rtnVal += compareSortMap.get(key) + "3";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "8";
					}
					break;
				case "FILEPATH" :
					msgVal = msgMap.get("filePath." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("Formatting")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else {
							rtnVal += compareSortMap.get(key) + "3";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "8";
					}
					break;
				case "DOWNLOADLOCATION" :
					msgVal = msgMap.get("downloadLocation." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("The address should be started")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else if (msgVal.startsWith("Not the same")) {
							rtnVal += compareSortMap.get(key) + "3";
						} else {
							rtnVal += compareSortMap.get(key) + "4";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "8";
					}
					break;
				case "HOMEPAGE" :
					msgVal = msgMap.get("homepage." + _id);
					if (!isEmpty(msgVal)) {
						if (msgVal.startsWith("This field")) {
							rtnVal += compareSortMap.get(key) + "0";
						} else if (msgVal.startsWith("Exceeded")) {
							rtnVal += compareSortMap.get(key) + "1";
						} else if (msgVal.startsWith("The address should be started")) {
							rtnVal += compareSortMap.get(key) + "2";
						} else if (msgVal.startsWith("Not the same")) {
							rtnVal += compareSortMap.get(key) + "3";
						} else {
							rtnVal += compareSortMap.get(key) + "4";
						}
					} else {
						rtnVal += compareSortMap.get(key) + "8";
					}
					break;
				default : 
					rtnVal += compareSortMap.get(key) + "8";
					break;
			}
		} else {
			rtnVal += compareSortMap.get(key) + "9";
		}
		
		return rtnVal;
	}

	private static String checkMultiLicenseError(Map<String, List<String>> addedIdList, List<ProjectIdentification> list) {
		if (list != null) {
			for (ProjectIdentification bean : list) {
				if (addedIdList.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					return avoidNull(bean.getGridId(), bean.getComponentId());
				}
			}
		}
		
		return null;
	}

	public static List<String> getAndroidNoticeBinaryList(String noticeContents) {
		List<String> noticeBinaryList = new ArrayList<>();
		// html 파일내에서 binary name 영역만 찾는다.
		Document doc = Jsoup.parse(noticeContents);
		Elements binaryEmList = doc.select("div.toc ul li");
		binaryEmList.addAll(doc.select("ul[class=file-list] li"));
		binaryEmList.addAll(doc.select("strong"));
		
		for (Element em : binaryEmList) {
			String binaryName = em.text();
			binaryName = binaryName.replaceAll("//", "/").replaceAll("\\u0000", "");
			noticeBinaryList.add(binaryName);
			
			if (binaryName.startsWith("/")) {
				noticeBinaryList.add(binaryName.substring(1));
			} else {
				noticeBinaryList.add("/" + binaryName);
			}

			//if ( binaryName.equals('/')) {
				//log.info("abcde---" + binaryName);
			//}
			// path 정보를 무시하고 binary 파일명만 추가 (binary file은 사전에 중복 제거되어 유니크하다)
			noticeBinaryList.add(FilenameUtils.getName(binaryName));
			if (binaryName.indexOf("/") > -1) {
				noticeBinaryList.add(binaryName.substring(binaryName.lastIndexOf("/")));
				noticeBinaryList.add(binaryName.substring(binaryName.lastIndexOf("/") + 1));
			} else {
				if (!noticeBinaryList.contains( ("/" + binaryName) )) {
					noticeBinaryList.add("/" + binaryName);
				}
			}
		}
		
		binaryEmList = doc.select("div.file-list");
		
		for (Element em : binaryEmList) {
			String _fileStr = (em.html()).replaceAll("<br>", "\n").replaceAll("<br />", "\n").replaceAll("<br/>", "\n").replaceAll("\r\n", "\n").replaceAll("<span>", "").replaceAll("</span>", "");
			
			for (String s : _fileStr.split("\n", -1)) {
				if (isEmpty(s)) {
					continue;
				}
				
				s = s.trim();
				
				// root dir 없이 격납
				if (s.startsWith("/")) {
					noticeBinaryList.add(s.substring(1));
				} else {
					noticeBinaryList.add(s);
				}
				
				// root dir 포함하여 격납
				noticeBinaryList.add("/" + s);
				
				// path 정보를 무시하고 binary 파일명만 추가 (binary file은 사전에 중복 제거되어 유니크하다)
				if (!s.endsWith("/") && s.indexOf("/") > -1) {
					noticeBinaryList.add(s.substring(s.lastIndexOf("/")));
					noticeBinaryList.add(s.substring(s.lastIndexOf("/") + 1));
				}
			}
		}
		
		return noticeBinaryList;
	}
	
	public static List<String> getAndroidNoticeBinaryXmlList(String url) throws Exception {

        List<String> noticeBinaryList = new ArrayList<>();
        
		Document doc = Jsoup.parse(new File(url), "UTF-8").parser(Parser.xmlParser());
		
		Elements fileList = doc.getElementsByTag("file-name");
		
		for (Element el : fileList) {
			if (el.childNodeSize() == 0) {
				continue;
			}
			Node node = el.childNode(0);
            String nodeValue = node.toString();
            
            nodeValue = StringUtil.avoidNull(nodeValue, "").replace("\n", "");
            if (StringUtil.isEmpty(nodeValue)) {
            	continue;
            }
            noticeBinaryList.add(nodeValue);
            
            if (nodeValue.startsWith("/")) {
                noticeBinaryList.add(nodeValue.substring(1));
            } else {
                noticeBinaryList.add("/" + nodeValue);
            }
            
            // path 정보를 무시하고 binary 파일명만 추가 (binary file은 사전에 중복 제거되어 유니크하다)
            if (nodeValue.indexOf("/") > -1) {
                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/")));
                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/") + 1));
            } else {
                if (!noticeBinaryList.contains( ("/" + nodeValue) )) {
                    noticeBinaryList.add("/" + nodeValue);
                }
            }
		}
		
//	    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
//       //DOM 파서로부터 입력받은 파일을 파싱하도록 요청
//        DocumentBuilder db = f.newDocumentBuilder();
//        org.w3c.dom.Document xmlDoc =  db.parse(url);
//        
//        //루트 엘리먼트 접근 
//        org.w3c.dom.Element root = xmlDoc.getDocumentElement();
//        org.w3c.dom.NodeList fileList = root.getElementsByTagName("file-name");
//        
//        for (int i = 0; i < fileList.getLength(); i++) {
//            org.w3c.dom.Node node = fileList.item(i);
//            String nodeValue = node.getNodeValue();
//            noticeBinaryList.add(nodeValue);
//            
//            if (nodeValue.startsWith("/")) {
//                noticeBinaryList.add(nodeValue.substring(1));
//            } else {
//                noticeBinaryList.add("/" + nodeValue);
//            }
//            
//            // path 정보를 무시하고 binary 파일명만 추가 (binary file은 사전에 중복 제거되어 유니크하다)
//            if (nodeValue.indexOf("/") > -1) {
//                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/")));
//                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/") + 1));
//            } else {
//                if (!noticeBinaryList.contains( ("/" + nodeValue) )) {
//                    noticeBinaryList.add("/" + nodeValue);
//                }
//            }
//        }
        
        return noticeBinaryList;
    }
	
    // TODO - 사용하는 곳 없음
	public static String getNoticeFileContents(String noticeContents) {
		// html 파일내에서 binary name 영역만 찾는다.
		Document doc = Jsoup.parse(noticeContents);
		doc.select("html");
		doc.select("header");
		doc.select("body");
		
		return doc.toString();
	}
	
	public static String html2text(String html) {
		return Jsoup.parse(avoidNull(html)).text();
	}
	
	public static String checkObligationSelectedLicense(List<OssComponentsLicense> licenseList) {
		String rtnVal = "";
		
		if (licenseList != null) {
			for (OssComponentsLicense bean : licenseList) {
				if (!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					// 확인 가능한 라이선스 중에서만 obligation 대상으로 한다.
					if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName()).toUpperCase())) {
						LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().toUpperCase());
						
						if (CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationNeedsCheckYn()))) {
							return CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK;
						} else if (CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationDisclosingSrcYn()))) {
							rtnVal = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
						} else if (isEmpty(rtnVal) && CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationNotificationYn()))) {
							rtnVal = CoConstDef.CD_DTL_OBLIGATION_NOTICE;
						}
					}
				}
			}
		}
		
		return rtnVal;
	}
	
	public static String checkObligationSelectedLicense2(List<ProjectIdentification> licenseList) {
		String rtnVal = "";
		
		if (licenseList != null) {
			for (ProjectIdentification bean : licenseList) {
				if (!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					// 확인 가능한 라이선스 중에서만 obligation 대상으로 한다.
					if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName()).toUpperCase())) {
						LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().toUpperCase());
						
						if (CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationNeedsCheckYn()))) {
							return CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK;
						} else if (CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationDisclosingSrcYn()))) {
							rtnVal = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
						} else if (isEmpty(rtnVal) && CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationNotificationYn()))) {
							rtnVal = CoConstDef.CD_DTL_OBLIGATION_NOTICE;
						}
					}
				}
			}
		}
		
		return rtnVal;
	}

	public static String convertUrlLinkFormat(String url) {
//		if (!isEmpty(url) && url.startsWith("www")) {
//			return "http://" + url;
//		}
		
		return url;
	}

	public static String makeOssTypeStr(String ossType) {
		String rtn = "";
		
		if (!isEmpty(ossType)) {
			ossType = ossType.toUpperCase();
			
			if (ossType.indexOf("M") > -1) {
				if (!isEmpty(rtn)) {
					rtn += ", ";
				}
				rtn += "Multi";
			}
			
			if (ossType.indexOf("D") > -1) {
				if (!isEmpty(rtn)) {
					rtn += ", ";
				}
				rtn += "Dual";
			}
			
			if (ossType.indexOf("V") > -1) {
				if (!isEmpty(rtn)) {
					rtn += ", ";
				}
				rtn += "v-Diff";
			}
		}
		
		return rtn;
	}
	
	public static String getMsApplicationContentType(String fileName) {
		String type = "";
		
		if (!isEmpty(fileName)) {
			type = "application/octet-stream";
		}
		
		return type;
	}

	public static String lineReplaceToBR(String s) {
		return avoidNull(s).replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>");
	}
	
	public static String pReplaceToBR(String s) {
		return avoidNull(s).replaceAll("<p>", "").replaceAll("</p>", "<br>");
	}
	
	public static String addBlankTargetToLink(String s) {
		return avoidNull(s).replaceAll("(<a\\s+(?!.*?\\btarget=)[^>]*)(>)", "$1target=\"_blank\"$2");
	}

	public static String brReplaceToLine(String s) {
		return avoidNull(s).replaceAll("\r\n", "\n").replaceAll("<br>", "\n").replaceAll("<br />", "\n").replaceAll("<br/>", "\n").replaceAll("<br \\>", "\n").replaceAll("<br\\>", "\n");
	}

	public static boolean isIgnoreLicense(String licenseName) {
		boolean result = false;
		
		if (!isEmpty(CoCodeManager.CD_ROLE_OUT_LICENSE)) {
			for (String license : avoidNull(licenseName).split(",")) {
				for (String s : CoCodeManager.CD_ROLE_OUT_LICENSE.split("\\|")) {
					if (s.trim().equalsIgnoreCase(license)) {
						result = true;
						break;
					}
				}
			}
		}
		
		// license type이 NA가 아닌 라이선스가 포함되어 있거나, New license인 경우 false
		if (result) {
			for (String license : avoidNull(licenseName).split(",")) {
				if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.toUpperCase())) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
					if (!"NA".equalsIgnoreCase(licenseMaster.getLicenseType())) {
						return false;
					}
				} else {
					return false;
				}
			}	
		}
		
		return result;
	}
	
	public static String htmlEscape(String s) {
		if (!isEmpty(s)) {
			try {
				return CommonFunction.lineReplaceToBR(HtmlUtils.htmlEscape(brReplaceToLine(s)));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return s;
	}
	
	public static boolean isIgnoreLicense(List<OssComponentsLicense> list) {
		if (list != null && !list.isEmpty()) {
			return isIgnoreLicense(list.get(0));
		}
		
		return false;
	}
	
	public static boolean isIgnoreLicense(OssComponentsLicense bean) {
		return isIgnoreLicense(bean.getLicenseName());
	}

	public static String makeSessionKey(String userName, String type, String typeId) {
		return "FOSSLIGHT_SESSION_KEY_" + avoidNull(userName) + "_" + avoidNull(type) + "_" + avoidNull(typeId);
	}
	
	public static String makeSessionKey(String userName, String type, String typeId, String typeId2) {
		return "FOSSLIGHT_SESSION_KEY_" + avoidNull(userName) + "_" + avoidNull(type) + "_" + avoidNull(typeId) + "_" + avoidNull(typeId2);
	}

	public static List<List<ProjectIdentification>> mergeGridAndSession(String sessionKey,
			List<ProjectIdentification> ossComponents, List<List<ProjectIdentification>> ossComponentsLicense) {
		return mergeGridAndSession(sessionKey, ossComponents, ossComponentsLicense, null);
	}
	
	@SuppressWarnings("unchecked")
	public static List<List<ProjectIdentification>> mergeGridAndSession(String sessionKey,
			List<ProjectIdentification> ossComponents, List<List<ProjectIdentification>> ossComponentsLicense, String reportkey) {
		
		List<String> duplicateCheckList = new ArrayList<>();
		// 먼저 세션에 해당 정보가 존재하는지 확인한다.
		Object sessionObj = getSessionObject(sessionKey);
		Object sessionObjReport = null;
		
		if (!isEmpty(reportkey)) {
			sessionObjReport = getSessionObject(reportkey);
		}
		
		if ( (sessionObj != null || sessionObjReport != null) && ossComponents != null) {
			// session에 merge할 정보가 존재하면, 사용자 편집 정보와, session 정보를 모두  Map 형태로 변환하여 상위 grid정보를 기준으로 merge 한다.
			Map<String, List<ProjectIdentification>> subGridInfo = new HashMap<>();
			Map<String, Object> sessionMap = null;
			Map<String, Object> sessionMapReport = null;
			
			if (sessionObj != null) {
				sessionMap = (Map<String, Object>) sessionObj;
			}
			
			if (sessionObjReport != null) {
				sessionMapReport = (Map<String, Object>) sessionObjReport;
			}
			
			if ( (sessionMap != null && sessionMap.containsKey("subData"))  || (sessionMapReport != null && sessionMapReport.containsKey("subData"))) {
				Map<String, List<ProjectIdentification>> subGridSessionInfo = null;
				Map<String, List<ProjectIdentification>> subGridSessionInfoReport = null;
				
				if (sessionMap != null) {
					subGridSessionInfo = (Map<String, List<ProjectIdentification>>) sessionMap.get("subData");
				}
				
				if (sessionMapReport != null) {
					subGridSessionInfoReport = (Map<String, List<ProjectIdentification>>) sessionMapReport.get("subData");
				}
				
				if (ossComponentsLicense != null) {
					for (List<ProjectIdentification> subTables : ossComponentsLicense) {
						String _key = subTables.get(0).getComponentId();
						
						// 최초저장이면 component id는 설정되어 있지 않음
						// 이런경우는 grid id에서 "-" 앞자리를 키로 가져야함
						if (isEmpty(_key)) {
							_key = subTables.get(0).getGridId().split("-")[0];
						}
						
						subGridInfo.put(_key, subTables);
					}
				}

				if (subGridSessionInfo != null && !subGridSessionInfo.isEmpty()) {
					for (ProjectIdentification bean : ossComponents) {
						// subGrid key에 주의 해야함 grid id로 매핑
						if (!duplicateCheckList.contains(bean.getGridId()) && !subGridInfo.containsKey(bean.getGridId()) && subGridSessionInfo.containsKey(bean.getGridId())) {
							duplicateCheckList.add(bean.getGridId());
							
							// multi => single로 변경한 경우 대응
							// subGridInfo 에 해당 grid id 로 값이 없다는 것인 single을 의미한다.
							if (CoConstDef.LICENSE_DIV_SINGLE.equals(bean.getLicenseDiv()) && subGridSessionInfo.get(bean.getGridId()) != null && subGridSessionInfo.get(bean.getGridId()).size() > 1) {
								
							} else {
								ossComponentsLicense.add(subGridSessionInfo.get(bean.getGridId()));
							}
						}
					}					
				}
				
				if (subGridSessionInfoReport != null && !subGridSessionInfoReport.isEmpty()) {
					for (ProjectIdentification bean : ossComponents) {
						// subGrid key에 주의 해야함 grid id로 매핑
						if (!duplicateCheckList.contains(bean.getGridId()) && !subGridInfo.containsKey(bean.getGridId()) && subGridSessionInfoReport.containsKey(bean.getGridId())) {
							duplicateCheckList.add(bean.getGridId());
							ossComponentsLicense.add(subGridSessionInfoReport.get(bean.getGridId()));
						}
					}					
				}
			}
		}
		
		return ossComponentsLicense;
	}

	public static String makeSessionReportKey(String loginUserName, String code, String prjId) {
		String reportKey = null;
		
		if (!isEmpty(code)) {
			switch (code) {
				case CoConstDef.CD_DTL_COMPONENT_ID_SRC:
					reportKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC;
					break;
				case CoConstDef.CD_DTL_COMPONENT_ID_BIN:
					reportKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN;
					break;
				case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID:
					reportKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID;
					break;
				case CoConstDef.CD_DTL_COMPONENT_ID_BAT:
					reportKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BAT;
					break;
				case CoConstDef.CD_DTL_COMPONENT_BAT:
					reportKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_BAT;
					break;
				case CoConstDef.CD_DTL_COMPONENT_PARTNER:
					reportKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PARTNER;
					break;
				default:
					break;
			}
		}
		
		if (!isEmpty(reportKey)) {
			return makeSessionKey(loginUserName(), reportKey, prjId);
		}
		
		return null;
	}

	public static OssComponentsLicense reMakeLicenseBean(ProjectIdentification comLicense, String licenseDiv) {
		final OssComponentsLicense license = new OssComponentsLicense();
		// 컴포넌트 ID 설정
		license.setComponentId(comLicense.getComponentId());
		// 라이센스 ID 설정
		String licenseName = comLicense.getLicenseName();
		
		if (!isEmpty(licenseName)) {
			licenseName = licenseName.replaceAll("\\<.*\\>", ""); // 시점이 맞지않아서 licenseName에 valid Message가 포함되어 저장할 경우 제거 함.
		}
		
		license.setLicenseId(CommonFunction.getLicenseIdByName(licenseName));
		license.setLicenseName(StringUtil.trim(avoidNull(CommonFunction.getLicenseNameById(license.getLicenseId(), licenseName))));
		
		// 기타 설정
		//license.setLicenseName(comLicense.getLicenseName());
		license.setLicenseText(comLicense.getLicenseText());
		license.setCopyrightText(comLicense.getCopyrightText());
		
		if (CoConstDef.LICENSE_DIV_SINGLE.equals(licenseDiv)) {
			license.setExcludeYn(CoConstDef.FLAG_NO);
		}else {
			license.setExcludeYn(avoidNull(comLicense.getExcludeYn(), CoConstDef.FLAG_NO));
		}
		
		return license;
	}
	
	public static List<ProjectIdentification> reMakeLicenseComponentList(List<OssLicense> list, ProjectIdentification selectedLicenseBean) {
		List<ProjectIdentification> licenseList = new ArrayList<>();
		String selectedLicenseName = avoidNull(selectedLicenseBean.getLicenseName());
		boolean hasLicense = false;
		List<String> licenseNameList = new ArrayList<String>();
		
		for (OssLicense bean : list) {
			ProjectIdentification liBean = new ProjectIdentification();
			liBean.setLicenseId(bean.getLicenseId());
			liBean.setOssLicenseComb(bean.getOssLicenseComb());
			liBean.setLicenseName(bean.getLicenseName());
			liBean.setLicenseText(selectedLicenseBean.getLicenseText());
			liBean.setComponentId(selectedLicenseBean.getComponentId());
			liBean.setGridId(selectedLicenseBean.getGridId());
			
			if (bean.getLicenseName().toUpperCase().trim().equals(selectedLicenseName.toUpperCase().trim())) {
				boolean isDup = false;
				
				for (String licenseName : licenseNameList){
					if (licenseName.toUpperCase().trim().equals(bean.getLicenseName().toUpperCase().trim())){
						isDup = true;
					}
				}
				
				if (isDup){
					liBean.setExcludeYn(CoConstDef.FLAG_YES);
				}else{
					liBean.setExcludeYn(CoConstDef.FLAG_NO);
				}
				
				hasLicense = true;
			} else {
				liBean.setExcludeYn(CoConstDef.FLAG_YES);
			}
			
			licenseNameList.add(bean.getLicenseName());
			licenseList.add(liBean);
		}
		
		if (!hasLicense) {
			selectedLicenseBean.setOssLicenseComb("OR");
			licenseList.add(selectedLicenseBean);
		}
		
		return licenseList;
	}
	
	public static List<ProjectIdentification> reMakeLicenseComponentListMultiToSingle(List<ProjectIdentification> list) {
		if (list != null && !list.isEmpty()) {
			List<ProjectIdentification> newLicenseList = new ArrayList<>();
			
			for (ProjectIdentification bean : list) {
				if (!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					if (!isEmpty(bean.getOssLicenseComb())) {
						bean.setOssLicenseComb("");
					}
					
					newLicenseList.add(bean);
					
					return newLicenseList;
				}
			}
		}
		
		return null;
	}

	@SuppressWarnings("deprecation")
	public static List<String> getNoticeBinaryList(T2File noticeFile) {
		List<String> noticeBinaryList = null;
		
		if (noticeFile != null) {
			noticeBinaryList = new ArrayList<>();
			
			try {
			    String noticeFileName = noticeFile.getLogiNm();
			    String fullName = noticeFile.getLogiPath() + "/" + noticeFileName;
			    
			    if ("xml".equalsIgnoreCase(FilenameUtils.getExtension(noticeFileName))) {
			        noticeBinaryList = CommonFunction.getAndroidNoticeBinaryXmlList(fullName);
			    } else {
    				noticeBinaryList = CommonFunction.getAndroidNoticeBinaryList(FileUtils.readFileToString(new File(fullName)));
			    }
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				
				return null;
			}
		}
		
		return noticeBinaryList;
	}

	public static List<String> getExistsBinaryNames(T2File resultFile) {
		List<String> existsBinaryName = null;
		
		if (resultFile != null) {
			existsBinaryName = new ArrayList<>();
			
			try {
				List<String> resultFileContents = FileUtils.readLines(new File(resultFile.getLogiPath() + "/" + resultFile.getLogiNm()), "UTF-8");
				
				for (String s : resultFileContents) {
					if (isEmpty(s)) {
						continue;
					}
					
					if (avoidNull(s).startsWith("<Removed>")) {
					} else {
						String[] resultCols = s.split("\t", -1);
						
						// 0 binary , 1 directory, 5 license
						if (resultCols.length == 7) {
							if (!isEmpty(resultCols[0])) {
								if (!existsBinaryName.contains(resultCols[0])) {
									existsBinaryName.add(resultCols[0]);
								}
							}
						}
					}
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				
				return null;
			}
		}
		
		return existsBinaryName;
	}

	public static Map<String, Object> getAndroidResultFileInfo(T2File resultFileInfo, List<String> existsBinaryName) {
		Map<String, Object> resultMap = new HashMap<>();
		List<String> removedCheckList = new ArrayList<>();
		List<OssComponents> addCheckList = new ArrayList<>();
		List<String> existsResultTextBinaryNameList = new ArrayList<>();
		OssComponents addComponent = null;
		
		try {
			List<String> resultFileContents =FileUtils.readLines(new File(resultFileInfo.getLogiPath() + "/" + resultFileInfo.getLogiNm()), "UTF-8");
			boolean isFirst = true;
			
			int idxBinaryName = -1;
			int idxDirectory = -1;
			int idxNoticeHtml = -1;
			int idxOssName = -1;
			int idxOssVersion = -1;
			int idxLicense = -1;
			int idxTlsh = -1;
			int idxChecksum = -1;
			
			for (String s : resultFileContents) {
				if (isFirst) {
					isFirst = false;
					
					if (!isEmpty(s)) {
						String[] resultCols = s.split("\t", -1);
						int idx = 0;
						
						for (String idxStr : resultCols) {
							idxStr = avoidNull(idxStr).trim().toUpperCase();
							
							switch (idxStr) {
								case "BINARY NAME":
									idxBinaryName = idx;
									
									break;
								case "SOURCE CODE PATH":
									idxDirectory = idx;
									
									break;
								case "NOTICE.HTML":
									idxNoticeHtml = idx;
									
									break;
								case "OSS NAME":
									idxOssName = idx;
									
									break;
								case "OSS VERSION":
									idxOssVersion = idx;
									
									break;
								case "LICENSE":
									idxLicense = idx;
									
									break;
								case "TLSH":
									idxTlsh = idx;
									
									break;
								case "CHECKSUM":
									idxChecksum = idx;
									
									break;
								default:
									break;
							}
							
							idx++;
						}
					}
					
					continue;
				}
				
				if (isEmpty(s)) {
					continue;
				}
				
				if (avoidNull(s).startsWith("<Removed>")) {
					log.debug("Removed : " + s);
				} else {
					//0 binary , 1 directory, 5 license
					if (idxBinaryName > -1){
						String[] resultCols = s.split("\t", -1);
						
						if (!isEmpty(resultCols[idxBinaryName])) {
							/*
							0:Binary/Library file
							1:Directory
							2:NOTICE.html
							3:OSS Name
							4:OSS Version
							5:License
							6:.mk File Path
							
							7:
							8: tlsh
							9: checksum
							*/
							addComponent = new OssComponents();
							addComponent.setBinaryName(resultCols[idxBinaryName]);
							addComponent.setFilePath(resultCols[idxBinaryName]); // binary name의 원본 정보를 등록한다.
							
							if (idxDirectory > -1 && resultCols.length >= idxDirectory +1) {
								addComponent.setSourceCodePath(resultCols[idxDirectory]);
							}
							
							if (idxNoticeHtml > -1 && resultCols.length >= idxNoticeHtml +1) {
								addComponent.setBinaryNotice(resultCols[idxNoticeHtml]);
							}
							
							if (idxOssName > -1 && resultCols.length >= idxOssName +1) {
								addComponent.setOssName(resultCols[idxOssName]);
							}
							
							if (idxOssVersion > -1 && resultCols.length >= idxOssVersion +1) {
								addComponent.setOssVersion(resultCols[idxOssVersion]);
							}
							
							if (idxLicense > -1 && resultCols.length >= idxLicense +1) {
								OssComponentsLicense license = new OssComponentsLicense();
								license.setLicenseName(resultCols[idxLicense]);
								addComponent.addOssComponentsLicense(license);
							}
							
							if (idxTlsh> -1 && resultCols.length >= idxTlsh +1) {
								String _tlsh = avoidNull(resultCols[idxTlsh]);
								if ("-".equals(_tlsh)) {
									_tlsh = "0";
								}
								addComponent.setTlsh(_tlsh);
							}
							
							if (idxChecksum > -1 && resultCols.length >= idxChecksum +1) {
								addComponent.setCheckSum(resultCols[idxChecksum]);
							}
							
							if (!existsBinaryName.contains(resultCols[idxBinaryName])) {
								addCheckList.add(addComponent);
							}
							
							existsResultTextBinaryNameList.add(addComponent.getBinaryName());
						}
					}
				}
			}
			
			resultMap.put("removedCheckList", removedCheckList);
			resultMap.put("addCheckList", addCheckList);
			resultMap.put("existsResultTextBinaryNameList", existsResultTextBinaryNameList);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return resultMap;
	}

	public static String getLicenseUrlByName(String licenseName) {
		if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(licenseName).toUpperCase())) {
			return avoidNull(CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(licenseName).toUpperCase()).getWebpage());
		}
		
		return "";
	}
	
	public static String getSelectedLicenseString(List<ProjectIdentification> list) {
		String rtn = "";
		
		if (list != null) {
			for (ProjectIdentification license : list) {
				if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					if (!isEmpty(rtn)) {
						rtn += ",";
					}
					
					rtn += license.getLicenseName();
				}
			}
		}
		
		return rtn;
	}
	
    public static boolean isEqualsOssMaster(OssMaster oldObject, OssMaster newObject) {
    	try {
            BeanMap map = new BeanMap(oldObject);
            PropertyUtilsBean propUtils = new PropertyUtilsBean();

            for (Object propNameObject : map.keySet()) {
                String propertyName = (String) propNameObject;
                Object property1 = propUtils.getProperty(oldObject, propertyName);
                Object property2 = propUtils.getProperty(newObject, propertyName);
                
                if (property1 == null) {
                	continue;
                }
                
                if (property1.equals(property2)) {
                	
                } else {
                	return false;
                }
            }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

        return true;
    }
    
    public static boolean isEqualsLicenseMaster(LicenseMaster oldObject, LicenseMaster newObject) {
    	try {
            BeanMap map = new BeanMap(oldObject);
            PropertyUtilsBean propUtils = new PropertyUtilsBean();

            for (Object propNameObject : map.keySet()) {
                String propertyName = (String) propNameObject;
                Object property1 = propUtils.getProperty(oldObject, propertyName);
                Object property2 = propUtils.getProperty(newObject, propertyName);
                
                if (property1 == null) {
                	continue;
                }
                
                if (property1.equals(property2)) {
                	
                } else {
                	return false;
                }
            }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

        return true;
    }
    
    public static boolean isEqualsProjectMaster(Project oldObject, Project newObject) {
    	try {
            BeanMap map = new BeanMap(oldObject);
            PropertyUtilsBean propUtils = new PropertyUtilsBean();

            for (Object propNameObject : map.keySet()) {
                String propertyName = (String) propNameObject;
                Object property1 = propUtils.getProperty(oldObject, propertyName);
                Object property2 = propUtils.getProperty(newObject, propertyName);
                
                if (property1 == null) {
                	continue;
                }
                
                if (property1.equals(property2)) {
                	
                } else {
                	return false;	
                }
            }
		} catch (Exception	 e) {
			log.error(e.getMessage(), e);
		}

        return true;
    }
	
	public static boolean existsLicenseName(List<ProjectIdentification> list) {
		if (list != null) {
			for (ProjectIdentification bean : list) {
				// license detail 아이콘 표시 여부를 위해 여기서 license id를 검증한다.
				if (bean.getLicenseName() != null && CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName().toUpperCase().trim()))) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static String makeLicenseInternalUrl(LicenseMaster licenseMaster, boolean distributionFlag) {
		if (licenseMaster != null && !isEmpty(licenseMaster.getLicenseText())) {
			String filePath = appEnv.getProperty("internal.url.dir.path");
			String licenseName = !isEmpty(licenseMaster.getShortIdentifier()) ? licenseMaster.getShortIdentifier() : !isEmpty(licenseMaster.getLicenseNameTemp()) ? licenseMaster.getLicenseNameTemp() : licenseMaster.getLicenseName();
			
			if (!isEmpty(licenseName)) {
				String fileName = licenseName.replaceAll(" ", "_").replaceAll("/", "_").replaceAll("\"", "&#34;") + ".html";
				boolean DEFAULT_URL_FLAG = !CoConstDef.FLAG_YES.equals(CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_NOTICE_INTERNAL_URL));
				if (DEFAULT_URL_FLAG) {
					return licenseMaster.getWebpage();
				} else if (distributionFlag) {
					String distributeUrl = CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTE_CODE, CoConstDef.CD_DTL_DISTRIBUTE_LGE);
					distributeUrl += filePath + "/" + fileName;

					return distributeUrl;
				} else {
					String domain = licenseMaster.getDomain();
					String fileUrl = domain + filePath + "/" + fileName;

					return fileUrl;
				}
			}
		}
		
		return "";
	}
	
	public static String makeHtmlLinkTagWithText(String s) {
		if (!isEmpty(s)) {
			  /* 아래과 같이 사용하여되 되지만 만약 작성자가 직접 태그를 이용하여
			  * 링크를 거는경우 링크가 이상하게 잡히는 경우를 막기위해
			  * < 값은 자동링크생성에서 제외하였습니다.
			  * 확인하고 싶으신분은 아래 regex 를 사용해서 링크를 생성해보세요
			  */
			  //String regex = "([\\p{Alnum}]+)://([a-z0-9.\\p{Punct}\\_]+)";

			  String regex = "([\\p{Alnum}]+)://([a-z0-9.\\-&/%=?:@#$(),.+;~\\_]+)";
			  //String strHTML = "한글사랑 http://www.naver.com test-text";
			  
			  Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			  Matcher m = p.matcher(s);
			  return m.replaceAll("<a href='http://$2' class='urlLink2' target=_blank>http://$2</a>");
		}
		
		return s;
	}
	
	public static String checkLicenseUserGuide(List<ProjectIdentification> componentLicenseList) {
		if (componentLicenseList != null) {
			String guideStr = "";
			
			for (ProjectIdentification bean : componentLicenseList) {
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				if (CoCodeManager.LICENSE_USER_GUIDE_LIST.contains(avoidNull(bean.getLicenseName()).toUpperCase())) {
					LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().trim().toUpperCase());
					
					if (license != null && !isEmpty(license.getDescription())) {
						if (!isEmpty(guideStr)) {
							guideStr += "<br>";
						}
						
						guideStr += license.getDescription();
					}
					
				}
			}
			
			if (!isEmpty(guideStr)) {
				return CommonFunction.makeHtmlLinkTagWithText(CommonFunction.lineReplaceToBR(guideStr));
			}
		}
		
		return "";
	}
	
	public static String escapeToJS(String dataStr) {
		return avoidNull(dataStr).replaceAll("\\", "\\\\").replaceAll("\'", "\\\'").replaceAll("\"", "\\\"").replaceAll("\r\n", "\\n").replaceAll("\n", "\\n").replaceAll("?", "\\?");
	}

	public static String makeValidMsgTohtml(Map<String, String> validMessageMap) {
		String rtnStr = "<b>Please check your entry and try again.</b><br />";
		
		if (validMessageMap != null) {
			int ExceededCnt = 0;
			for (String key : validMessageMap.keySet()) {
				if ("isValid".equalsIgnoreCase(key)) {
					continue;
				}
				String msg = removeLineSeparator(validMessageMap.get(key));
				if (key.indexOf(".") > -1) {
					if (key.startsWith("licenseName") && msg.contains("Exceeded max length")) {
						ExceededCnt++;
					} else {
						rtnStr += "<br />" + key.substring(0, key.indexOf(".")) + " : " + msg;
					}
				} else {
					rtnStr += "<br />" + key + " : " + msg;
				}
			}
			if (ExceededCnt > 0) {
				rtnStr += "<br />" + CommonFunction.getCustomMessage("msg.common.license.exceeded.max.length", String.valueOf(ExceededCnt));
			}
		}
		
		return rtnStr;
	}

	public static List<ProjectIdentification> getItemsExceedingMaxLength(Map<String, String> validMessageMap, List<ProjectIdentification> ossComponents) {
		List<ProjectIdentification> exceedingMaxLengthList = new ArrayList<>();
		List<String> exceedingMaxLengthGridIdList = new ArrayList<>();
		if (validMessageMap != null) {
			for (String key : validMessageMap.keySet()) {
				if ("isValid".equalsIgnoreCase(key)) {
					continue;
				}
				String msg = removeLineSeparator(validMessageMap.get(key));
				if (key.indexOf(".") > -1) {
					if (key.startsWith("licenseName") && msg.contains("Exceeded max length")) {
						exceedingMaxLengthGridIdList.add(key.split("[.]")[1]);
					}
				}
			}
		}
		if (!exceedingMaxLengthGridIdList.isEmpty()) {
			List<ProjectIdentification> reorderList = new ArrayList<>();
			for (ProjectIdentification bean : ossComponents) {
				if (exceedingMaxLengthGridIdList.contains(bean.getGridId())) {
					exceedingMaxLengthList.add(bean);
				} else {
					reorderList.add(bean);
				}
			}
			exceedingMaxLengthList.addAll(reorderList);
		}
		
		return exceedingMaxLengthList;
	}
	
	public static String makeValidMsgTohtml(Map<String, String> validMessageMap, List<ProjectIdentification> ossComponents) {
		List<String> rtnStrList = new ArrayList<>();
		String rtnStr = getMessage("msg.oss.check.ossName.format");
		
		if (validMessageMap != null) {
			for (String key : validMessageMap.keySet()) {
				if (rtnStrList.size() == 10) {
					break;
				}
				
				for (ProjectIdentification pi : ossComponents) {
					if (pi.getGridId().equals(key.substring(key.indexOf(".") + 1, key.length()))) {
						String replaceString = "";
						if (pi.getOssName().contains(",")) {
							replaceString = pi.getOssName().replace(",", "&#44;");
						}else if (pi.getOssName().contains("<")) {
							replaceString = pi.getOssName().replace("<", "&#60;");
						}else if (pi.getOssName().contains(">")) {
							replaceString = pi.getOssName().replace(">", "&#62;");
						}
						if (!isEmpty(replaceString) && !rtnStrList.contains(replaceString)) {
							rtnStrList.add(replaceString);
							rtnStr += "<br />" + "- " + replaceString;
							break;
						}
					}
				}
			}
		}
		
		return rtnStr;
	}
	
	public static Boolean booleanValidationFormatForValidMsg(Map<String, String> validMessageMap, boolean isCheckOssName) {
		boolean validFlag = false;
		
		if (validMessageMap != null) {
			for (String key : validMessageMap.keySet()) {
				if ("isValid".equalsIgnoreCase(key)) {
					continue;
				}
				
				String msg = removeLineSeparator(validMessageMap.get(key));
				if (key.indexOf(".") > -1) {
					if (isCheckOssName) {
						if (msg.contains("Formatting") && key.substring(0, key.indexOf(".")).equals("ossName")) {
							validFlag = true;
							break;
						}
					} else {
						if (key.startsWith("licenseName") && msg.contains("Exceeded max length")) {
							validFlag = true;
							break;
						}
					}
				}
				
			}
		}
		
		return validFlag;
	}

	public static String removeLineSeparator(String s) {
		return avoidNull(s).replaceAll("\r\n", "").replaceAll("\n", "").replaceAll("<br>", "").replaceAll("<br/>", "").replaceAll("<br />", "");
	}
	
	@SuppressWarnings("unused")
	public static Map<String, Object> remakeMutiLicenseComponents(List<ProjectIdentification> ossComponents,
			List<List<ProjectIdentification>> ossComponentsLicense) {
		// 동적으로 oss를 추가해야하는 경우
		List<ProjectIdentification> ossComponentsAddList = new ArrayList<>();
		int addedRowIdx = 1;
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, List<ProjectIdentification>> licenseMap = new HashMap<>();
		boolean isChanged = false;
		
		if (ossComponents != null && ossComponentsLicense != null) {
			if (ossComponentsLicense != null && !ossComponentsLicense.isEmpty()) {
				for (List<ProjectIdentification> list : ossComponentsLicense) {
					for (ProjectIdentification licenseBean : list) {
						String componentId = isEmpty(licenseBean.getComponentId()) ? licenseBean.getGridId().split("-")[0] : licenseBean.getComponentId();
						
						if (!isEmpty(componentId)) {
							licenseMap.put(componentId, list);
							
							break;
						}
					}
				}
			}
			
			for (ProjectIdentification bean : ossComponents) {
				//if (!"-".equals(bean.getOssName()) && !isEmpty(bean.getOssName()) && !StringUtil.contains(bean.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX) && !isEmpty(bean.getComponentId())) {
				if (!"-".equals(bean.getOssName()) && !isEmpty(bean.getOssName()) ) {
					String key = bean.getOssName().toUpperCase().trim() + "_" + avoidNull(bean.getOssVersion()).trim().toUpperCase();
					String componentId = isEmpty(bean.getComponentId()) ? bean.getGridId().split("-")[0] : bean.getComponentId();
					
					if (CoCodeManager.OSS_INFO_UPPER.containsKey(key)) {
						OssMaster ossMaster = CoCodeManager.OSS_INFO_UPPER.get(key);
						
						if (CoConstDef.LICENSE_DIV_MULTI.equals(ossMaster.getLicenseDiv())) {
							//boolean isDiff = false;
							List<ProjectIdentification> licenseList = new ArrayList<>();
							
							if (licenseMap.containsKey(componentId)) {
								licenseList = licenseMap.get(componentId);
							}
							
							// licnese list를 찾는다. 지금은 사용하지 않으나, multi => single로 변경한 경우 사용예정
							// 해당 license 정보를 제외한 list를 만든다.
							
							if (CoConstDef.LICENSE_DIV_SINGLE.equals(avoidNull(bean.getLicenseDiv(), CoConstDef.LICENSE_DIV_SINGLE))) {
								// oss master는 multi license이고, component는 single license로 정의된 경우
								// multi license의 첫번째 license를 선택한 상태로 적용한다.
								// oss master에 해당 license가 없는 경우 마지막에 or 조건으로 추가한다.
								
								ProjectIdentification paramBean = bean;
								// 새로만든 license list를 추가한다.						
								
								if (StringUtil.contains(bean.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
									licenseMap.put(bean.getGridId(), CommonFunction.reMakeLicenseComponentList(ossMaster.getOssLicenses(), paramBean));
								}else{
									licenseMap.replace(componentId, CommonFunction.reMakeLicenseComponentList(ossMaster.getOssLicenses(), paramBean));
								}
								
								bean.setLicenseDiv(CoConstDef.LICENSE_DIV_MULTI);
								isChanged = true;
							}
						} else if (CoConstDef.LICENSE_DIV_SINGLE.equals(ossMaster.getLicenseDiv()) 
								&& CoConstDef.LICENSE_DIV_MULTI.equals(avoidNull(bean.getLicenseDiv(), CoConstDef.LICENSE_DIV_SINGLE))) {
							
						}
					}					
				}
			}
		}
		
		if (!ossComponentsAddList.isEmpty()) {
			ossComponents.addAll(ossComponentsAddList);
		}
		
		resultMap.put("mainList", ossComponents);
		
		if (isChanged) {
			List<List<ProjectIdentification>> newOssComponentsLicense = new ArrayList<>();
			
			for (List<ProjectIdentification> _tmp : licenseMap.values()) {
				newOssComponentsLicense.add(_tmp);
			}
			
			resultMap.put("subList", newOssComponentsLicense);
		} else {
			resultMap.put("subList", ossComponentsLicense);
		}
		
		return resultMap;
	}
	//TODO : Deprecated
	public static String makeSearchQuery(String schKeyword, String field) {
		StringBuffer sb = new StringBuffer();
		
		if (!isEmpty(schKeyword)) {
			for (String s : schKeyword.split(" ")) {
				if (!isEmpty(s)) {
					if (sb.length() > 0) {
						sb.append(" AND ");
					}
					
					sb.append(field + " LIKE '%"+s+"%'");
				}
			}
		}
		
		if (sb.length() > 0) {
			sb.insert(0, " (");
			sb.append(") ");
		}
		
		return sb.toString();
	}

	public static void setOssDownloadLocation(List<Map<String, Object>> binaryList) {
		for (Map<String, Object> binaryMap : binaryList) {
			String ossName = "";
			String ossVersion = "";
			String downloadlocation = "";
			
			if (binaryMap.containsKey("ossName")) {
				ossName = (String) binaryMap.get("ossName");
				
				if (binaryMap.containsKey("ossVersion")) {
					ossVersion = (String) binaryMap.get("ossVersion");
				}
				
				String key = (ossName + "_" + avoidNull(ossVersion)).toUpperCase();
				if (CoCodeManager.OSS_INFO_UPPER.containsKey(key)){
					downloadlocation = avoidNull(CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(ossVersion)).toUpperCase() ).getDownloadLocation());
				}
			}
			
			binaryMap.put("downloadlocation", downloadlocation);
		}
	}

	public static String getPlatformName(String str) {
		if (!isEmpty(str)) {
			String arr[]  = str.trim().split(" ");
			
			if (arr.length > 1) {
				String version = arr[arr.length -1];
				
				if (StringUtil.isFormattedString(version, "[\\.0-9]+")) {
					String _temp = "";
					
					for (int i=0; i<arr.length -1; i++) {
						if (!isEmpty(_temp)) {
							_temp += " ";
						}
						
						_temp += arr[i];
					}
					
					str = _temp.trim();
				}
			}
		}
		
		return str;
	}

	public static String getPlatformVersion(String str) {
		if (!isEmpty(str)) {
			String arr[] = str.trim().split(" ");
			
			if (arr.length > 1) {
				String version = arr[arr.length -1];
				
				if (StringUtil.isFormattedString(version, "[\\.0-9]+")) {
					return version.trim();
				}
			}
		}
		
		return "";
	}
	
	@SuppressWarnings("unused")
	public static String getObligationTypeWithAndLicense(List<OssLicense> andlicenseGroup) {
		if (andlicenseGroup != null) {
			boolean sourceCode = false;
			boolean notice = false;
			boolean needcheck = false;
			
			for (OssLicense bean : andlicenseGroup) {
				LicenseMaster license = CoCodeManager.LICENSE_INFO_BY_ID.get(bean.getLicenseId());
				
				if (license != null) {
					if (CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationDisclosingSrcYn()))) {
						sourceCode = true;
					}
					
					if (CoConstDef.FLAG_YES.equals(avoidNull(license.getObligationNotificationYn()))) {
						notice = true;
					}
				}
			}
			
			if (sourceCode) {
				return CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
			} else if (notice) {
				return CoConstDef.CD_DTL_OBLIGATION_NOTICE;
			}
		}
		
		return "";
	}

	@SuppressWarnings("unused")
	public static List<String> getBinaryListBinBinaryTxt(T2File binaryTextFile) {
		List<String> binaryList = new ArrayList<>();
		
		if (binaryTextFile != null) {
			try {
				List<String> resultFileContents =FileUtils.readLines(new File(binaryTextFile.getLogiPath() + "/" + binaryTextFile.getLogiNm()), "UTF-8");
				boolean isFirst = true;
				
				int idxBinaryName = -1;
				int idxTlsh = -1;
				int idxChecksum = -1;
				
				for (String s : resultFileContents) {
					if (isFirst) {
						isFirst = false;
						
						if (!isEmpty(s)) {
							String[] resultCols = s.split("\t", -1);
							int idx = 0;
							
							for (String idxStr : resultCols) {
								idxStr = avoidNull(idxStr).trim().toUpperCase();
								
								switch (idxStr) {
									case "BINARY NAME":
									case "BINARY":
										idxBinaryName = idx;
										break;
									case "TLSH":
										idxTlsh = idx;
										break;
									case "CHECKSUM":
									case "SHA1SUM":
										idxChecksum = idx;
										break;
	
									default:
										break;
								}
								
								idx++;
							}
						}
						
						continue;
					}
					
					if (isEmpty(s)) {
						continue;
					}

					//0 binary , 1 directory, 5 license
					if (idxBinaryName > -1){
						String[] resultCols = s.split("\t", -1);
						
						if (!isEmpty(resultCols[idxBinaryName])) {
							/*
							0:Binary/Library file
							1: checksum
							2: tlsh
							*/
							
							binaryList.add(resultCols[idxBinaryName]);
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return binaryList;
	}
	
	public static String setLicenseRestrictionList(List<ProjectIdentification> componentLicenseList) {
		String returnStr = "";
		
		if (componentLicenseList != null) {
			String restrictionStr = "";
			
			for (ProjectIdentification bean : componentLicenseList) {
				if (bean.getLicenseName() != null) {
					LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().trim().toUpperCase());
					
					if (license != null && !isEmpty(license.getRestriction()) && !CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
						restrictionStr += (isEmpty(restrictionStr)?"":",") + license.getRestriction(); 
					}
				}
			}
			
			String restrictionArr[] = restrictionStr.split(",");
			List<String> restrictionList = new ArrayList<>();
			List<String> distinctList = new ArrayList<String>();
			
			// String 배열 -> String 리스트
			for (int i = 0 ; i < restrictionArr.length ; i++){
				restrictionList.add(restrictionArr[i]);
			}
			// 중복 제거
            for (String str : restrictionList){
                if (!distinctList.contains(str)) {
                	distinctList.add(str);
                }
            }
            
            for (String str : distinctList){
            	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
            }
		}

		return returnStr;
	}
	
	@SuppressWarnings("unused")
	public static String setLicenseRestrictionList(String restrictionStr) {
		String returnStr = "";
		
		if (!isEmpty(restrictionStr)) {
			String restrictionArr[] = restrictionStr.split(",");
			List<String> restrictionList = new ArrayList<>();
			List<String> distinctList = new ArrayList<String>();
			
			// String 배열 -> String 리스트
			for (int i = 0 ; i < restrictionArr.length ; i++){
				restrictionList.add(restrictionArr[i]);
			}
			
            for (String str : restrictionList){
            	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
            }
		}
		
		return returnStr;
	}
	
	public static String setLicenseRestrictionListById(String licenseIdStr, String ossRestriction) {
		String returnStr = "";
		
		List<String> restrictionList = null;
		List<String> distinctList = null;
		
		if (licenseIdStr != null) {
			restrictionList = new ArrayList<>();
			distinctList = new ArrayList<>();
			
			String restrictionStr = "";
			String licenseIdArr[] = licenseIdStr.split(",");
			
			for (int i = 0 ; i < licenseIdArr.length ; i++) {
				LicenseMaster license = CoCodeManager.LICENSE_INFO_BY_ID.get(licenseIdArr[i]);
				if (license != null && !isEmpty(license.getRestriction())) {
					restrictionStr += (isEmpty(restrictionStr)?"":",") + license.getRestriction(); 
				}
			}
			
			String restrictionArr[] = restrictionStr.split(",");
			
			// String 배열 -> String 리스트
			for (int i = 0 ; i < restrictionArr.length ; i++){
				if (!isEmpty(restrictionArr[i])) {
					restrictionList.add(restrictionArr[i]);
				}
			}
			
			if (!isEmpty(ossRestriction)) {
				List<String> ossRestrictionList = Arrays.asList(ossRestriction.split(","));
				for (String or : ossRestrictionList) {
					if (!isEmpty(or)) {
						restrictionList.add(or);
					}
				}
			}
		} else {
			if (!isEmpty(ossRestriction)) {
				restrictionList = new ArrayList<>();
				distinctList = new ArrayList<>();
				
				List<String> ossRestrictionList = Arrays.asList(ossRestriction.split(","));
				for (String or : ossRestrictionList) {
					if (!isEmpty(or)) {
						restrictionList.add(or);
					}
				}
			}
		}
		
		if (!CollectionUtils.isEmpty(restrictionList)) {
			// 중복 제거
	        for (String str : restrictionList){
	            if (!distinctList.contains(str)) {
	            	distinctList.add(str);
	            }
	        }
	        
	        Map<String, Integer> restrictionMap = new HashMap<>();
	        for (String str : distinctList){
	        	if (!isEmpty(str)) {
	        		String level = CoCodeManager.getSubCodeNoForCodeDtls(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
		        	if (isEmpty(level)) {
		        		restrictionMap.put(str.trim().toUpperCase(), 99);
		        	} else {
		        		restrictionMap.put(str.trim().toUpperCase(), Integer.parseInt(level));
		        	}
	        	}
	        }
	        
	        if (!restrictionMap.isEmpty()) {
	        	List<String> restrictionMapKeys = new ArrayList<>(restrictionMap.keySet());
	        	restrictionMapKeys.sort((o1, o2) -> restrictionMap.get(o2).compareTo(restrictionMap.get(o1)));
		        
		        String level = "";
		        for (String restrictionKey : restrictionMapKeys) {
		        	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, restrictionKey);
		        	String maxLevel = CoCodeManager.getSubCodeNoForCodeDtls(CoConstDef.CD_LICENSE_RESTRICTION, restrictionKey);
		        	if (isEmpty(level)) {
		        		if (isEmpty(maxLevel) && restrictionMap.get(restrictionKey) == 99) {
		        			level = String.valueOf(99);
		        		} else {
		        			level = maxLevel;
		        		}
		        	}
		        }
		        
		        if (!isEmpty(level)) {
		        	returnStr += "|" + level;
		        }
	        }
		}
		
		return returnStr;
	}
	

	public static String getFilterToString(String filters) {
		return getFilterToString(filters, null);
	}

	public static String getFilterToString(String filters, Map<String, String> ambiguousInfo) {
		return getFilterToString(filters, ambiguousInfo, null);
	}
	
	public static String getFilterToString(String filters, Map<String, String> ambiguousInfo, Map<String, String> exceptionMap) {
		return getFilterToString(filters, ambiguousInfo, exceptionMap, false);
	}
	
	@SuppressWarnings("unchecked")
	public static String getFilterToString(String filters, Map<String, String> ambiguousInfo, Map<String, String> exceptionMap, boolean upperFlag) {
		if (ambiguousInfo == null) {
			ambiguousInfo = new HashMap<>();
		}
		
		String filterCondition = "";
		String[] dateField = {"creationDate", "publDate", "modiDate", "regDt"};
		
		if (exceptionMap == null) {
			exceptionMap = new HashMap<>();
		}
		
		exceptionMap.put("copyrighttext", "copyright");
		List<String> numberFormatColumns = new ArrayList<>();
		numberFormatColumns.add("CVSS_SCORE");
		
		if (!isEmpty(filters)) {
			Type collectionType1 = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> filtersMap = (Map<String, Object>) fromJson(filters, collectionType1);
			
			if (filters != null && filtersMap.containsKey("rules")) {
				for (Map<String, String> ruleMap : (List<LinkedTreeMap<String, String>>)filtersMap.get("rules")) {
					String field = ruleMap.get("field");
					String op =  ruleMap.get("op");
					String data = ruleMap.get("data");
					String startCondition = "";
					String endCondition = "";
					boolean isNumberFormat = numberFormatColumns.contains(StringUtil.convertToUnderScore(field).toUpperCase());
					
					if (!isEmpty(field) && !isEmpty(data)) {
						for ( String key : exceptionMap.keySet() ){ 
							if (field.equalsIgnoreCase(key)) {
								field = exceptionMap.get(key);
							}
						}
						
						boolean dateB = false;
						
						for (String dateF : dateField) {
							if (field.equalsIgnoreCase(dateF)) {
								dateB = true;
							}
						}
						
						switch(op) {
							case "eq":
								if (!isNumberFormat) {
									if (upperFlag){
										startCondition = " = UPPER('";
										endCondition = "')";
									}else {
										startCondition = " = '";
										endCondition = "'";
									}
								} else {
									startCondition = " = ";
								}
								
								break;
							case "ne":
								if (!isNumberFormat) {
									startCondition = " <> '";
									endCondition = "'";
								} else {
									startCondition = " <> ";
								}
								
								break;
							case "bw":
								startCondition = " LIKE '";
								endCondition = "%'";
								
								break;
							case "bn":
								startCondition = " NOT LIKE '";
								endCondition = "%'";
								
								break;
							case "ew":
								startCondition = " LIKE '%";
								endCondition = "'";
								
								break;
							case "en":
								startCondition = " NOT LIKE '%";
								endCondition = "'";
								
								break;
							case "cn":
								if (upperFlag){
									startCondition = " LIKE UPPER('%";
									endCondition = "%')";
								}else{
									startCondition = " LIKE '%";
									endCondition = "%'";
								}
								
								break;
							case "nc":
								startCondition = " NOT LIKE '%";
								endCondition = "%'";
								
								break;
							case "lt":
								if (!isNumberFormat) {
									startCondition = " < '";
									endCondition = "'";
								} else {
									startCondition = " < ";
								}
								
								break;
							case "le":
								if (!isNumberFormat) {
									startCondition = " <= '";
									endCondition = "'";
								} else {
									startCondition = " <= ";
								}
								
								break;
							case "gt":
								if (!isNumberFormat) {
									startCondition = " > '";
									endCondition = "'";
								} else {
									startCondition = " > ";
								}
								
								break;
							case "ge":
								if (!isNumberFormat) {
									startCondition = " >= '";
									endCondition = "'";
								} else {
									startCondition = " >= ";
								}
								
								break;
							default:
								if (dateB) {
									startCondition = " = '";
									endCondition = "'";
								}else{
									startCondition = " LIKE '%";
									endCondition = "%'";
								}
								
								break;
						}
						
						if (!upperFlag){
							field = StringUtil.convertToUnderScore(field).toUpperCase();
						}
						
						if (ambiguousInfo.containsKey(field) && !isEmpty(ambiguousInfo.get(field))) {
							field = ambiguousInfo.get(field) + "." + field;
						}
						
						// 예외조건
						if (dateB) {
							filterCondition += " AND " + "DATE_FORMAT(" + field + ", '%Y-%m-%d') " + startCondition + CommonFunction.formatDateSimple(data) + endCondition;
						} else {
							filterCondition += " AND " + field + startCondition + data + endCondition;
						}
					}
				}
			}
		}
		
		return filterCondition;
	}
	
	public static List<ProjectIdentification> replaceOssVersionNA(List<ProjectIdentification> obj) {
		if (obj != null) {
			for (ProjectIdentification bean : obj) {
				if ("N/A".equals(bean.getOssVersion())) {
					bean.setOssVersion("");
				}
			}
		}
		
		return obj;
	}
	
	public static ProjectIdentification findOssIdAndName(ProjectIdentification bean) {
		if (bean != null && !isEmpty(bean.getOssName())) {
			if ("N/A".equals(bean.getOssVersion())) {
				bean.setOssVersion("");
			}
			
			final OssMaster masterBean = CoCodeManager.OSS_INFO_UPPER.get((bean.getOssName().trim() +"_" + avoidNull(bean.getOssVersion()).trim()).toUpperCase());
			if (masterBean != null) {
				bean.setOssId(masterBean.getOssId());
				bean.setOssName(masterBean.getOssName());
			}
		}
		
		if(bean != null && StringUtil.isEmpty(bean.getOssId())) {
			bean.setOssId(null);
		}
		
		return bean;
	}
	
	public static OssComponentsLicense findLicenseIdAndName(OssComponentsLicense bean) {
		return null;
	}

	public static List<OssComponentsLicense> findOssLicenseIdAndName(String ossId,
			List<OssComponentsLicense> ossComponentsLicenseList) {
		// 먼저 license Id는 찾을수 있으면 모두 설정한다.
		if (ossComponentsLicenseList != null) {
			for (OssComponentsLicense licenseBean : ossComponentsLicenseList) {
				LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(licenseBean.getLicenseName(), "").trim().toUpperCase());
				
				if (licenseMaster != null) {
					licenseBean.setLicenseId(licenseMaster.getLicenseId());
					licenseBean.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(),licenseMaster.getLicenseName()));
					
					// OSS_LICENSE 에 설정값이 있는 경우 우선으로 설정하지만, 없는 경우 license master를 기준으로 설정하기 때문에 일단 여기서는 master기준으로 설정
					licenseBean.setLicenseText(licenseMaster.getLicenseText());
				}
			}
		}
		
		// oss에서 추가설정한 license text 및 oss copyright 설정
		if (!isEmpty(ossId)) {
			OssMaster ossMaster = CoCodeManager.OSS_INFO_BY_ID.get(ossId);
			
			if (ossMaster != null) {
				// oss master에 등록된 licnese순서 기준으로 찾는다 (기본적으로 size가 일치 해야함, multi dual license의 경우)
				int idx=0;
				
				for (OssLicense ossLicense : ossMaster.getOssLicenses()) {
					if (ossComponentsLicenseList != null 
							&&ossComponentsLicenseList.size() >= idx+1 
							&& ossLicense.getLicenseId().equals(ossComponentsLicenseList.get(idx).getLicenseId())) {
						// license text
						// oss_license에 존재하는 경우만 설정
						if (!isEmpty(ossLicense.getOssLicenseText())) {
							ossComponentsLicenseList.get(idx).setLicenseText(ossLicense.getOssLicenseText());
						}
						
						// oss copyright
						if (!isEmpty(ossLicense.getOssCopyright())) {
							ossComponentsLicenseList.get(idx).setCopyrightText(ossLicense.getOssCopyright());
						}
					}
					
					idx++;
				}
			}
		}
		
		return ossComponentsLicenseList;
	}
	
	public static List<List<ProjectIdentification>> setOssComponentLicense(List<ProjectIdentification> ossComponent) {
		return setOssComponentLicense(ossComponent, false);
	}
	
	public static List<List<ProjectIdentification>> setOssComponentLicense(List<ProjectIdentification> ossComponent, boolean excludeFlag) {
		return setOssComponentLicense(ossComponent, excludeFlag, false);
	}
	
	public static List<List<ProjectIdentification>> setOssComponentLicense(List<ProjectIdentification> ossComponent, boolean excludeFlag, boolean isNickValid) {
		List<List<ProjectIdentification>> ossComponentLicense = new ArrayList<List<ProjectIdentification>>(); 
		
		for (ProjectIdentification pi : ossComponent) {
			if (excludeFlag && CoConstDef.FLAG_YES.equals(pi.getExcludeYn()) && isEmpty(pi.getLicenseName())) {
				continue;
			}
			
			String convertLicenseName = pi.getLicenseName();
			
			if (convertLicenseName != null && convertLicenseName.contains("\n")) { // 사용자가 oss-report를 통해 license 정보를 입력할 경우 개행이 있을 case가 존재하여 추가함. 
				pi.setLicenseName(convertLicenseName.replace("\n", " "));
			}
			
			if (StringUtil.isEmpty(pi.getComponentId()) && StringUtil.contains(pi.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)) {
				List<ProjectIdentification> licenseList = new ArrayList<ProjectIdentification>();
				
				if (pi.getLicenseName().contains(",")) {
					List<String> licenseNames = Arrays.asList(pi.getLicenseName().split(","));
					pi.setLicenseDiv("M");
					
					for (String nm : licenseNames) {
						ProjectIdentification license = new ProjectIdentification();
						nm = nm.trim(); // license 정보를 입력할때 license 정보 trim 처리함.
						
						license.setGridId(pi.getGridId());
						license.setExcludeYn(CoConstDef.FLAG_NO);
						
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(nm.toUpperCase())) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(nm.toUpperCase());
							String licenseName = "";
							if (!isNickValid) {
								licenseName = avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp());
							} else {
								licenseName = nm;
							}
							license.setLicenseId(licenseMaster.getLicenseId());
							license.setLicenseName(licenseName);
						} else {
							license.setLicenseId("");
							license.setLicenseName(nm);
						}
						
						licenseList.add(license);
					}
				} else {
					licenseList.add(pi);
				}
				
				ossComponentLicense.add(licenseList);
			} else {
				List<ProjectIdentification> licenseList = new ArrayList<ProjectIdentification>();
				
				if (pi.getLicenseName() != null &&  pi.getLicenseName().contains(",")) {
					List<String> licenseNames = Arrays.asList(pi.getLicenseName().split(","));
					pi.setLicenseDiv("M");
					
					for (String nm : licenseNames) {
						ProjectIdentification license = new ProjectIdentification();
						nm = nm.trim(); // license 정보를 입력할때 license 정보 trim 처리함.
						
						license.setComponentId(pi.getComponentId());
						license.setGridId(pi.getGridId()+"-");
						license.setExcludeYn(CoConstDef.FLAG_NO);
						
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(nm.toUpperCase())) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(nm.toUpperCase());
							String licenseName = "";
							if (!isNickValid) {
								licenseName = avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp());
							} else {
								licenseName = nm;
							}
							license.setLicenseId(licenseMaster.getLicenseId());
							license.setLicenseName(licenseName);
						} else {
							license.setLicenseId("");
							license.setLicenseName(nm);
						}
						
						licenseList.add(license);
					}
				} else {
					licenseList.add(pi);
				}
				
				ossComponentLicense.add(licenseList);
			}
		}
		
		return ossComponentLicense;
	}
	
	public static boolean checkLicense(String licenseName) {
		String[] result = licenseName.split(",");
		boolean resultBoolean = true;
		
		for (String name : result) {
			if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(name.toUpperCase())) {
				resultBoolean = false;
				break;
			}
		}
		
		return resultBoolean;
	}
	
	public static T2CoValidationResult getAnalysisValidation(Map<String, Object> map, List<OssAnalysis> list){
		T2CoOssValidator validator = new T2CoOssValidator();
		validator.setAppendix("analysisList", list);
		validator.setVALIDATION_TYPE(validator.VALID_OSSANALYSIS_LIST);
		T2CoValidationResult vr = validator.validate(new HashMap<>());
		Map<String, String> validMap = vr.getValidMessageMap();
		Map<String, String> diffMap = vr.getDiffMessageMap();

		map.put("validMap", validMap);
		map.put("diffMap", diffMap);
		
		return vr;
	}
	
	@SuppressWarnings("unchecked")
	public static void setAnalysisResultList(Map<String, Object> map) {
		List<OssAnalysis> analysisResultList = (List<OssAnalysis>) map.get("rows");
		List<OssAnalysis> analysisList = (List<OssAnalysis>) map.get("analysisList");
		List<OssAnalysis> changeAnalysisResultList = new ArrayList<OssAnalysis>();
		Map<String, Object> valid = new HashMap<String, Object>();
		
		getAnalysisValidation(valid, analysisList);
		
		Map<String, String> errorMsg = (Map<String, String>) valid.get("validMap");
		
		int gridSeq = 1; 
		
		List<String> deactivateOssList = ossService.getDeactivateOssList();
		deactivateOssList.replaceAll(String::toUpperCase);
		
		for (OssAnalysis bean : analysisResultList) {
			OssAnalysis userData = analysisList
										.stream()
										.filter(e -> e.getComponentId().equals(bean.getGridId()))
										.collect(Collectors.toList()).get(0); // 사용자 입력 정보
			
			userData.setGroupId(userData.getGridId()); // groupId는 user가 입력한 row의 grid Id임.
			
			if (CoConstDef.FLAG_YES.equals(userData.getCompleteYn()) && !isEmpty(userData.getReferenceOssId())) {
				
				OssAnalysis successOssInfo = ossService.getAutoAnalysisSuccessOssInfo(userData.getReferenceOssId());
				
				if (successOssInfo != null) {
					if (!isEmpty(successOssInfo.getDownloadLocationGroup())) {
						successOssInfo.setDownloadLocation(successOssInfo.getDownloadLocationGroup());
					}
					
					successOssInfo.setTitle("사용자 등록 정보");
					successOssInfo.setGroupId(userData.getGroupId());
					successOssInfo.setGridId(userData.getGridId());
					successOssInfo.setResult("true");
					successOssInfo.setCompleteYn(userData.getCompleteYn());
					successOssInfo.setReferenceOssId(userData.getReferenceOssId());
					changeAnalysisResultList.add(successOssInfo);
					
					continue;
				}
			}
			
			int latestCnt = 0;
			OssAnalysis latestOssAnalysis = null;
			String analyzedDownloadLocation = "";
			
			userData.setTitle("사용자 작성 정보");
			
			String ossName = userData.getOssName();
			String ossNameTemp = "";
			boolean ossNicknameFlag = false;
			
			String comment  = bean.getComment();
			if (!isEmpty(comment)) userData.setComment(comment);
			
			if (bean.getResult().toUpperCase().equals("TRUE")) {
				List<OssAnalysis> latestRegistrationInfoList = new ArrayList<OssAnalysis>();
				
				int ossNameCnt = errorMsg.entrySet()
						.stream()
						.filter(e -> e.getKey().indexOf(bean.getGridId()) > -1 && e.getKey().indexOf("ossName") > -1)
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
						.size();
				
				int ossVersionCnt = errorMsg.entrySet()
						.stream()
						.filter(e -> e.getKey().indexOf(bean.getGridId()) > -1 && e.getKey().indexOf("ossVersion") > -1)
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
						.size();
				
				String copyright = bean.getOssCopyright();
				
				String askalonoLicense = bean.getAskalonoLicense().replaceAll("\\(\\d+\\)", "");
				String scancodeLicense = bean.getScancodeLicense().replaceAll("\\(\\d+\\)", "");
				
				String duplicateNickname = bean.getOssNickname();
				
				String customOssName = "";
				if (bean.getOssName().contains(";")) {
					customOssName = bean.getOssName().split(";")[0];
				} else {
					customOssName = bean.getOssName();
				}
				
				if (customOssName.endsWith(".git")) {
					customOssName = customOssName.substring(0, customOssName.length()-4);
				}
				
				if (ossNameCnt == 0 && ossVersionCnt > 0) { // ossVersion 대상
					// 사용자 작성정보의 oss name이 취합정보의 nickname에 들어가는 case를 방지함.
					if (!userData.getOssName().toUpperCase().equals(bean.getOssName().toUpperCase())) {
						List<String> duplicateNicknameList = Arrays.asList(duplicateNickname.split(","));
						List<String> nicknameList = new ArrayList<>();
						
						for (String nick : duplicateNicknameList) {
							String customNick = "";
							if (nick.contains(";")) {
								customNick = nick.split(";")[0];
							} else {
								customNick = nick;
							}
							
							if (customNick.endsWith(".git")) {
								customNick = customNick.substring(0, customNick.length()-4);
							}
							
							if (!userData.getOssName().equalsIgnoreCase(customNick)) {
								nicknameList.add(customNick);
							}
						}
						
						if (nicknameList != null && !nicknameList.isEmpty()) {
							duplicateNickname = String.join(",", nicknameList);
						}
					}
				}
				
				String downloadLocation = "";
				if (bean.getDownloadLocation().contains(",")) {
					List<String> downloadLocationSplitList = new ArrayList<>();
					String[] downloadLocationSplit = bean.getDownloadLocation().split("[,]");
					for (String download : downloadLocationSplit) {
						boolean equalsFlag = false;
						for (String customData : downloadLocationSplitList) {
							if (download.equalsIgnoreCase(customData)) {
								equalsFlag = true;
								break;
							}
						}
						
						if (!equalsFlag) {
							downloadLocationSplitList.add(download);
						}
					}
					
					if (downloadLocationSplitList != null && !downloadLocationSplitList.isEmpty()) {
						String customDownloadLocation = "";
						for (String customDownload : downloadLocationSplitList) {
							customDownloadLocation += customDownload + ",";
						}
						
						downloadLocation = customDownloadLocation.substring(0, customDownloadLocation.length()-1);
					}
				} else {
					downloadLocation = bean.getDownloadLocation();
				}
				
				analyzedDownloadLocation = downloadLocation;
				
				OssAnalysis totalAnalysis = new OssAnalysis(userData.getGridId(), customOssName, bean.getOssVersion(), duplicateNickname
						, avoidNull(bean.getConcludedLicense(), null), copyright, downloadLocation
						, bean.getHomepage(), null, comment, bean.getResult(), "취합정보"); // 취합정보
				OssAnalysis askalono = new OssAnalysis(userData.getGridId(), customOssName, bean.getOssVersion(), duplicateNickname
						, askalonoLicense, null, downloadLocation
						, bean.getHomepage(), null, comment, bean.getResult(), "License text파일 분석 결과"); // License text 정보
				OssAnalysis scancode = new OssAnalysis(userData.getGridId(), customOssName, bean.getOssVersion(), duplicateNickname
						, scancodeLicense, copyright, downloadLocation
						, bean.getHomepage(), null, comment, bean.getResult(), "Scancode 분석 결과"); // scancode 정보
				
				List<OssAnalysis> ossAnalysisByNickList = new ArrayList<>();
				OssAnalysis ossInfoByNick = null;
				int idx = 1;
				OssMaster param = new OssMaster();
				
				for (String nick : duplicateNickname.split(",")) {
					if (CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(nick.toUpperCase())) {
						String ossNameByNick = CoCodeManager.OSS_INFO_UPPER_NAMES.get(nick.toUpperCase());
						param.setOssName(ossNameByNick);
						List<OssMaster> ossInfoByNickList = ossService.getOssListByName(param);
						
						if (ossInfoByNickList != null) {
							int deactivateCnt = ossInfoByNickList.stream().filter(e -> e.getDeactivateFlag().equals(CoConstDef.FLAG_YES)).collect(Collectors.toList()).size();
							if (deactivateCnt > 0) {
								continue;
							}
							
							if (ossAnalysisByNickList.size() > 0) {
								String checkDuplicateOssName = ossInfoByNickList.get(0).getOssName();
								int checkDuplicateCnt = ossAnalysisByNickList.stream().filter(e -> e.getOssName().equalsIgnoreCase(checkDuplicateOssName)).collect(Collectors.toList()).size();
								if (checkDuplicateCnt > 0) {
									continue;
								}
							}
							
							final Comparator<OssMaster> comp = Comparator.comparing((OssMaster o) -> o.getModifiedDate()).reversed();
							ossInfoByNickList = ossInfoByNickList.stream().sorted(comp).collect(Collectors.toList());
							
							List<OssLicense> liList = ossInfoByNickList.get(0).getOssLicenses();
							String license = "";
							for (OssLicense li : liList) {
								if (!isEmpty(li.getLicenseId())) {
									LicenseMaster lm = CoCodeManager.LICENSE_INFO_BY_ID.get(li.getLicenseId());
									license += !isEmpty(lm.getShortIdentifier()) ? lm.getShortIdentifier() + "," : li.getLicenseName() + ",";
								} else {
									license += li.getLicenseName() + ",";
								}
							}
							
							String analysisTitle = ossInfoByNickList.get(0).getOssName();
							if (!isEmpty(ossInfoByNickList.get(0).getOssVersion())) {
								analysisTitle += " (" + ossInfoByNickList.get(0).getOssVersion() + ")";
							}
							
							ossInfoByNick = new OssAnalysis(userData.getGridId(), ossInfoByNickList.get(0).getOssName(), bean.getOssVersion(), avoidNull(ossInfoByNickList.get(0).getOssNickname()).replaceAll("<br>", ",")
									, license.substring(0, license.length()-1), ossInfoByNickList.get(0).getCopyright(), ossInfoByNickList.get(0).getDownloadLocation()
									, ossInfoByNickList.get(0).getHomepage(), null, comment, "", analysisTitle + " 최신 등록 정보"); // nick oss 최신정보
							ossInfoByNick.setGridId(CoConstDef.GRID_NEWROW_DEFAULT_PREFIX + idx);
							
							ossAnalysisByNickList.add(ossInfoByNick);
						}
					}
				}
				
				userData.setResult("true");
				
				if (ossNameCnt == 0 && ossVersionCnt > 0) { // ossVersion 대상
					totalAnalysis.setGridId(""+gridSeq++);
					askalono.setGridId(""+gridSeq++);
					scancode.setGridId(""+gridSeq++);
					OssAnalysis newestOssInfo = null;
					OssAnalysis totalNewestOssInfo = null;
					
					try {
						// check if oss name is nickname
						if (CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(userData.getOssName().toUpperCase())) {
							ossNameTemp = CoCodeManager.OSS_INFO_UPPER_NAMES.get(userData.getOssName().toUpperCase());
						}
						
						if (!isEmpty(ossName) && !isEmpty(ossNameTemp) && !ossName.equals(ossNameTemp)) {
							userData.setOssName(ossNameTemp);
							ossNicknameFlag = true;
						}
						
						newestOssInfo = ossService.getNewestOssInfo(userData); // 사용자 정보의 ossName기준 최신 등록정보
						if (newestOssInfo != null) {
							newestOssInfo.setGridId(""+gridSeq++);
							newestOssInfo.setOssVersion(!isEmpty(bean.getOssVersion()) ? bean.getOssVersion() : userData.getOssVersion());
							newestOssInfo.setComment(comment);
						}
						
						if (userData.getOssName().toUpperCase().equals(totalAnalysis.getOssName().toUpperCase())) {
							String newestMergeNickName = "";
							if (newestOssInfo != null) {
								newestMergeNickName = CommonFunction.mergeNickname(totalAnalysis, newestOssInfo.getOssNickname()); // 사용자 작성 정보 & 최신등록정보 nickname Merge
								newestOssInfo.setOssNickname(newestMergeNickName);
							}else {
								newestMergeNickName = CommonFunction.mergeNickname(totalAnalysis, null);
							}
							
							totalAnalysis.setOssNickname(newestMergeNickName);
							askalono.setOssNickname(newestMergeNickName);
							scancode.setOssNickname(newestMergeNickName);
							userData.setOssNickname(newestMergeNickName);
						} else {
							totalNewestOssInfo = ossService.getNewestOssInfo(totalAnalysis); // 사용자 정보의 ossName기준 최신 등록정보
							String totalNewestMergeNickName = "";
							
							if (totalNewestOssInfo != null) {
								totalNewestOssInfo.setGridId(""+gridSeq++);
								totalNewestOssInfo.setOssVersion(userData.getOssVersion());
								totalNewestMergeNickName = CommonFunction.mergeNickname(totalAnalysis, totalNewestOssInfo.getOssNickname()); // 사용자 작성 정보 & 최신등록정보 nickname Merge
								totalNewestOssInfo.setOssNickname(totalNewestMergeNickName);
							}else {
								totalNewestMergeNickName = CommonFunction.mergeNickname(totalAnalysis, null);
							} 
							
							totalAnalysis.setOssNickname(totalNewestMergeNickName);
							askalono.setOssNickname(totalNewestMergeNickName);
							scancode.setOssNickname(totalNewestMergeNickName);
							userData.setOssNickname(totalNewestMergeNickName);
						}
					} catch (Exception newestException) {
						log.error(newestException.getMessage());
					}
					
					if (ossAnalysisByNickList != null && !ossAnalysisByNickList.isEmpty()) {
						for (OssAnalysis oa : ossAnalysisByNickList) {
							String mergeNickname = CommonFunction.mergeNickname(totalAnalysis, oa.getOssNickname());
							oa.setOssNickname(mergeNickname);
							
							if (totalNewestOssInfo != null) {
								if (!totalNewestOssInfo.getOssName().equalsIgnoreCase(oa.getOssName())) {
									mergeDownloadLocation(oa, null, analyzedDownloadLocation, true);
									latestRegistrationInfoList.add(oa); // seq 2 : oss 최신등록 정보
									latestOssAnalysis = oa;
									latestCnt++;
								}
							} else {
								if (newestOssInfo != null) {
									if (!newestOssInfo.getOssName().equalsIgnoreCase(oa.getOssName())) {
										mergeDownloadLocation(oa, null, analyzedDownloadLocation, true);
										latestRegistrationInfoList.add(oa); // seq 2 : oss 최신등록 정보
										latestOssAnalysis = oa;
										latestCnt++;
									}
								} else {
									mergeDownloadLocation(oa, null, analyzedDownloadLocation, true);
									latestRegistrationInfoList.add(oa); // seq 2 : oss 최신등록 정보
									latestOssAnalysis = oa;
									latestCnt++;
								}
							}
						}
					}
					
					if (totalNewestOssInfo != null && !deactivateOssList.contains(totalNewestOssInfo.getOssName().toUpperCase())) {
						mergeDownloadLocation(totalNewestOssInfo, null, analyzedDownloadLocation, true);
						latestRegistrationInfoList.add(totalNewestOssInfo); // seq 3 : 취합정보 최신등록 정보
						latestOssAnalysis = totalNewestOssInfo;
						latestCnt++;
					}
					
					if (newestOssInfo != null && !deactivateOssList.contains(newestOssInfo.getOssName().toUpperCase())) {
						mergeDownloadLocation(newestOssInfo, null, analyzedDownloadLocation, true);
						latestRegistrationInfoList.add(newestOssInfo); // seq 4 : 최신등록 정보
						latestOssAnalysis = newestOssInfo;
						latestCnt++;
						
						if (newestOssInfo.getOssName().toUpperCase().equals(userData.getOssName().toUpperCase())) {
							if (newestOssInfo.getOssNickname() != null) {
								userData.setOssNickname(CommonFunction.mergeNickname(userData, newestOssInfo.getOssNickname()));
							}
							if (userData.getOssNickname() != null) {
								newestOssInfo.setOssNickname(CommonFunction.mergeNickname(newestOssInfo, userData.getOssNickname()));
							}
						}
					}
					
					if (ossNicknameFlag) {
						userData.setOssName(ossName);
					}
					
					if (latestCnt == 1) {
						mergeDownloadLocation(latestOssAnalysis, totalAnalysis, analyzedDownloadLocation, false);
						mergeDownloadLocation(latestOssAnalysis, askalono, analyzedDownloadLocation, false);
						mergeDownloadLocation(latestOssAnalysis, scancode, analyzedDownloadLocation, false);
					}
					
					changeAnalysisResultList.add(totalAnalysis); // seq 1 : 취합 정보
					if (!CollectionUtils.isEmpty(latestRegistrationInfoList)) { // seq 2 : oss 최신등록 정보, seq 3 : 취합정보 최신등록 정보, seq 4 : 최신등록 정보
						changeAnalysisResultList.addAll(latestRegistrationInfoList);
					}
					changeAnalysisResultList.add(askalono);		 // seq 5 : askalono 정보
					changeAnalysisResultList.add(scancode);		 // seq 6 : scancode 정보
					changeAnalysisResultList.add(userData);		 // seq 7 : 사용자 입력 정보
				} else { // ossName 대상
					totalAnalysis.setGridId(""+gridSeq++);
					
					OssAnalysis totalNewestOssInfo = ossService.getNewestOssInfo(totalAnalysis); // 사용자 정보의 ossName기준 최신 등록정보
					
					if (totalNewestOssInfo != null) {
						totalNewestOssInfo.setGridId(""+gridSeq++);
						totalNewestOssInfo.setOssVersion(!isEmpty(bean.getOssVersion()) ? bean.getOssVersion() : userData.getOssVersion());
						totalNewestOssInfo.setComment(comment);
						
						String totalNewestMergeNickName = CommonFunction.mergeNickname(totalAnalysis, totalNewestOssInfo.getOssNickname()); // 사용자 작성 정보 & 최신등록정보 nickname Merge
						
						totalNewestOssInfo.setOssNickname(totalNewestMergeNickName);
						totalAnalysis.setOssNickname(totalNewestMergeNickName);
						
						askalono.setOssNickname(totalNewestMergeNickName);
						scancode.setOssNickname(totalNewestMergeNickName);
						userData.setOssNickname(totalNewestMergeNickName);
					}
					
					askalono.setGridId(""+gridSeq++);
					scancode.setGridId(""+gridSeq++);
					
					if (ossAnalysisByNickList != null && !ossAnalysisByNickList.isEmpty()) {
						for (OssAnalysis oa : ossAnalysisByNickList) {
							String oaMergeNickName = CommonFunction.mergeNickname(totalAnalysis, oa.getOssNickname());
							oa.setOssNickname(oaMergeNickName);
							
							if (totalNewestOssInfo != null) {
								if (!totalNewestOssInfo.getOssName().equalsIgnoreCase(oa.getOssName())) {
									mergeDownloadLocation(oa, null, analyzedDownloadLocation, true);
									latestRegistrationInfoList.add(oa); // seq 2 : oss 최신등록 정보
									latestOssAnalysis = oa;
									latestCnt++;
								}
							} else {
								mergeDownloadLocation(oa, null, analyzedDownloadLocation, true);
								latestRegistrationInfoList.add(oa); // seq 2 : oss 최신등록 정보
								latestOssAnalysis = oa;
								latestCnt++;
							}
						}
					}
					
					if (totalNewestOssInfo != null && !deactivateOssList.contains(totalNewestOssInfo.getOssName().toUpperCase())) {
						mergeDownloadLocation(totalNewestOssInfo, null, analyzedDownloadLocation, true);
						latestRegistrationInfoList.add(totalNewestOssInfo); // seq 3 : 취합정보 최신등록 정보
						latestOssAnalysis = totalNewestOssInfo;
						latestCnt++;
					}
					
					if (latestCnt == 1) {
						mergeDownloadLocation(latestOssAnalysis, totalAnalysis, analyzedDownloadLocation, false);
						mergeDownloadLocation(latestOssAnalysis, askalono, analyzedDownloadLocation, false);
						mergeDownloadLocation(latestOssAnalysis, scancode, analyzedDownloadLocation, false);
					}
					
					changeAnalysisResultList.add(totalAnalysis); // seq 1 : 취합 정보
					if (!CollectionUtils.isEmpty(latestRegistrationInfoList)) { // seq 2 : oss 최신등록 정보, seq 3 : 취합정보 최신등록 정보
						changeAnalysisResultList.addAll(latestRegistrationInfoList);
					}
					changeAnalysisResultList.add(userData);		 // seq 4 : 사용자 입력 정보
					changeAnalysisResultList.add(askalono);		 // seq 5 : askalono 정보
					changeAnalysisResultList.add(scancode);		 // seq 6 : scancode 정보
				}
			} else {
				userData.setResult("false");
				
				changeAnalysisResultList.add(userData); // analysis result가 False이면 사용자 입력정보만 보낸다.
				
				int ossNameCnt = errorMsg.entrySet()
						.stream()
						.filter(e -> e.getKey().indexOf(bean.getGridId()) > -1 && e.getKey().indexOf("ossName") > -1)
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
						.size();
				
				int ossVersionCnt = errorMsg.entrySet()
						.stream()
						.filter(e -> e.getKey().indexOf(bean.getGridId()) > -1 && e.getKey().indexOf("ossVersion") > -1)
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
						.size();
				
				if (ossNameCnt == 0 && ossVersionCnt > 0){
					try {
						// check if oss name is nickname
						if (CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(userData.getOssName().toUpperCase())) {
							ossNameTemp = CoCodeManager.OSS_INFO_UPPER_NAMES.get(userData.getOssName().toUpperCase());
						}
						
						if (!isEmpty(ossName) && !isEmpty(ossNameTemp) && !ossName.equals(ossNameTemp)) {
							userData.setOssName(ossNameTemp);
							ossNicknameFlag = true;
						}
						
						OssAnalysis newestOssInfo = ossService.getNewestOssInfo(userData); // 사용자 정보의 ossName기준 최신 등록정보
						
						if (newestOssInfo != null && !deactivateOssList.contains(newestOssInfo.getOssName().toUpperCase())) {
							newestOssInfo.setOssVersion(!isEmpty(bean.getOssVersion()) ? bean.getOssVersion() : userData.getOssVersion());
							newestOssInfo.setGridId(""+gridSeq++);
							newestOssInfo.setComment(comment);
							
							changeAnalysisResultList.add(newestOssInfo); // seq 2 : 최신등록 정보
						}
						
						if (ossNicknameFlag) {
							userData.setOssName(ossName);
						}
					} catch (Exception newestException) {
						log.error(newestException.getMessage());
					}
				}
			}
		}
		
		changeAnalysisResultList = changeAnalysisResultList.stream().filter(distinctByKey(e -> (e.getTitle() + "|" + e.getOssName() + "|" + e.getOssVersion()).toUpperCase())).collect(Collectors.toList());
		
		getAnalysisValidation(map, changeAnalysisResultList);
		map.replace("rows", changeAnalysisResultList);
		map.remove("analysisList"); // 분석결과 Data에서는 필요없는 data이므로 제거.
	}
	
	private static void mergeDownloadLocation(OssAnalysis latestOssAnalysis, OssAnalysis analysisResults, String analyzedDownloadLocation, boolean latestFlag) {
		if (latestOssAnalysis != null && !StringUtil.isEmpty(analyzedDownloadLocation)) {
			String latestDownloadLocation = latestOssAnalysis.getDownloadLocation();
			List<String> latestDownloadLocationList = new ArrayList<>();
			for (String dl : latestDownloadLocation.split(",")) {
				latestDownloadLocationList.add(dl);
			}
			
			List<String> analyzedDownloadLocationList = new ArrayList<>();
			for (String dl : analyzedDownloadLocation.split(",")) {
				analyzedDownloadLocationList.add(dl);
			}
			
			if (latestFlag) {
				analyzedDownloadLocationList.removeAll(latestDownloadLocationList);
				if (!CollectionUtils.isEmpty(analyzedDownloadLocationList)) {
					latestDownloadLocationList.addAll(analyzedDownloadLocationList);
					latestDownloadLocationList.sort(Comparator.naturalOrder());
				}
			} else {
				latestDownloadLocationList.removeAll(analyzedDownloadLocationList);
				if (!CollectionUtils.isEmpty(latestDownloadLocationList)) {
					analyzedDownloadLocationList.addAll(latestDownloadLocationList);
					analyzedDownloadLocationList.sort(Comparator.naturalOrder());
				}
			}
			
			if (analysisResults != null) {
				analysisResults.setDownloadLocation(String.join(",", analyzedDownloadLocationList));
			} else {
				latestOssAnalysis.setDownloadLocation(String.join(",", latestDownloadLocationList));
			}
		}
	}

	public static ArrayList<Object> checkXlsxFileLimit(List<UploadFile> list){
		ArrayList<Object> result = new ArrayList<Object>();
		
		for (UploadFile f : list) {
			if (f.getSize() > CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT && f.getFileExt().contains("xls")){
				result.add("FILE_SIZE_LIMIT_OVER");
				result.add("The excel file exceeded 5MB.<br>Please delete the blank row or unnecessary data, and then upload it.");
				
				break;
			}
		}
		
		return result;
	}

	public static ArrayList<Object> checkCsvFileLimit(List<UploadFile> list) {
		ArrayList<Object> result = new ArrayList<Object>();
		boolean nextCheck = false;
		
		for (UploadFile f : list) {
			if (f.getSize() > CoConstDef.CD_CSV_UPLOAD_FILE_SIZE_LIMIT && f.getFileExt().contains("csv")){
				result.add("FILE_SIZE_LIMIT_OVER");
				result.add("The file exceeded 5MB.<br>Please delete the blank row or unnecessary data, and then upload it.");
				nextCheck = true;
				break;
			}
			
			if (!nextCheck) {
				boolean tabGubnBoolean = false;
				String commaData = "";
				Scanner sc = null;
				
				try {
					sc = new Scanner(new FileInputStream(new File(f.getFilePath() + "/" + f.getFileName())));
					while (sc.hasNext()) {
						String readLine = sc.nextLine();
						if (!isEmpty(readLine) && !readLine.contains("\t")) {
							tabGubnBoolean = true;
							break;
						}
					}
				} catch (Exception e) {
					log.info(e.getMessage(), e);
				} finally {
					sc.close();
					
					if (tabGubnBoolean) {
						result.add("TAB_GUBN_ERROR");
						result.add(commaData);
						break;
					}
				}
			}
		}

		return result;
	}
	
	public static File convertXMLToHTML(File XmlFile) {
		return convertXMLToHTML(XmlFile, false);
	}
	
	public static File convertXMLToHTML(File XmlFile, boolean isDir) {
		List<File> xmlFiles = new ArrayList<File>();
		File outPath = null;
		
		if (!isDir) {
			if (XmlFile.exists()) {
				String Path = XmlFile.getPath().split("\\.")[0];
				outPath = new File(Path);
				outPath.mkdirs(); // xml file은 upload/android_notice/projectId/* 에 올려두기 때문에 dir를 생성해야 함.
				
				xmlFiles.add(XmlFile);
			}
		} else {
			if (XmlFile.exists()) {
				for (File f : XmlFile.listFiles()) {
					xmlFiles.add((File) f);
				}
			}
			
			outPath = XmlFile;
		}
		
		UUID randomUUID = UUID.randomUUID();
		File outFile = new File(outPath + "/" + randomUUID + ".html");
		
		boolean convertFlag = LicenseHtmlGeneratorFromXml.generateHtml(xmlFiles, outFile);
		if (convertFlag) {
			return outFile;
		} else {
			return null;
		}
	}
	
	public static String mergeNickname(OssAnalysis bean, String newestNickName) {
		String mergeNickname = newestNickName;
		
		if (!isEmpty(mergeNickname)) {
			List<String> nicknameList = new ArrayList<String>();
			
			if (!isEmpty(bean.getOssNickname())) { // nickname이 빈값이 있을 경우 담지 않음.
				nicknameList.addAll(Arrays.asList(bean.getOssNickname().split(",")));
				nicknameList = nicknameList.stream().filter(e -> !e.equalsIgnoreCase(bean.getOssName())).collect(Collectors.toList());
			}
			
			nicknameList.addAll(Arrays.asList(newestNickName.split(",")));
			mergeNickname = String.join(",", nicknameList.stream().sorted().filter(distinctByKey(p -> p.trim().toUpperCase())).collect(Collectors.toList()));
		} else {
			mergeNickname = bean.getOssNickname();
		}
		
		return mergeNickname;
	}
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
	    
	    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
	  final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();
	   
	  return t -> {
		final List<?> keys = Arrays.stream(keyExtractors)
					.map(ke -> ke.apply(t))
					.collect(Collectors.toList());
		 
		return seen.putIfAbsent(keys, Boolean.TRUE) == null;
	  };
	}
  
	public static String replaceSlashToUnderline(String target) {
		return target.replaceAll("[/\\\\]", "_");
	}
	
	private static TemplateEngine templateEngine;
	public static void setTemplateEngine(TemplateEngine engine) {
		templateEngine = engine;
	}
	
	public static String VelocityTemplateToString(String templatePath, Map<String, Object> model) {
		model.put("templateURL", templatePath);
		return VelocityTemplateToString(model);
	}
	
	public static String VelocityTemplateToString(Map<String, Object> model) {		
		Context context = new Context();
		context.setVariable("domain", CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org"));
		context.setVariable("commonFunction", CommonFunction.class);

		model.keySet().stream().filter(key -> !"templateURL".equals(key)).forEach(key -> context.setVariable(key, model.get(key)));
		String convertResultStr;
		try {
			convertResultStr = templateEngine.process((String) model.get("templateURL"), context);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		return convertResultStr;
	}

	public static String getNoticeFileName(String prjId, String prjName, String prjVersion, String dateStr, String fileType) {
		return getNoticeFileName(prjId, prjName, prjVersion, null, dateStr, fileType);
	}
	
	public static String getNoticeFileName(String prjId, String prjName, String prjVersion, String fileName, String dateStr, String fileType) {
		if (isEmpty(fileName)) {
			fileName = "OSSNotice-";
			fileName += prjId + "_" + prjName;
			
			if (!isEmpty(prjVersion)) {
				fileName += "_" + prjVersion;
			}
		}
		
		// file명에 사용할 수 없는 특수문자 체크
		if (!FileUtil.isValidFileName(fileName)) {
			fileName = FileUtil.makeValidFileName(fileName, "_");
		}
		
		fileName += "_" + dateStr;
		
		if (isEmpty(fileType) || fileType == "html"){
			fileName += ".html";
		}else if (fileType == "text"){
			fileName += ".txt";
		} else {
			fileName += fileType;
		}
		
		return fileName;
	}

	public static String getReviewReportFileName(String prjId, String prjName, String prjVersion, String dateStr, String fileType) {
		return getReviewReportFileName(prjId, prjName, prjVersion, null, dateStr, fileType);
	}

	public static String getReviewReportFileName(String prjId, String prjName, String prjVersion, String fileName, String dateStr, String fileType) {
		if (isEmpty(fileName)) {
			fileName = "FOSSLight-Review-";
			fileName += prjId + "_" + prjName;

			if (!isEmpty(prjVersion)) {
				fileName += "_" + prjVersion;
			}
		}

		// file명에 사용할 수 없는 특수문자 체크
		if (!FileUtil.isValidFileName(fileName)) {
			fileName = FileUtil.makeValidFileName(fileName, "_");
		}

		fileName += "_" + dateStr;
		fileName += ".pdf";

		return fileName;
	}
	
	public static String mergedString(String fromStr, String toStr, int compareTo, String separator) {
		if (compareTo >= 0){
			return fromStr + separator + toStr;
		} else {
			return toStr + separator + fromStr;
		}
	}
	
	public static void removeAll(File removeAllFile) {
		File[] allFiles = removeAllFile.listFiles();
		
        if (allFiles != null) {
            for (File file : allFiles) {
            	removeAll(file);
            }
        }
        
        removeAllFile.delete();
	}
	
	public static String removeSpecialChar(String target, int limit) {
		String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
		target = target.replaceAll(match, "");
		
		if (limit > 0) {
			target = target.substring(0, limit);
		}
		
		return target;
	}
	
	// 구분자 문자열 순서대로 중복 제거 
	public static String removeDuplicateStringToken(String str, String token){ 
		String removeDubString = ""; 
		
		if (!StringUtil.isEmpty(str)){
			String[] arr = StringUtil.split(str, token.trim());
			LinkedHashSet<String> ls = new LinkedHashSet<>();
			
			for (int i=0; i<arr.length; i++){
				ls.add(arr[i]);
			}
			
			Iterator<String> it = ls.iterator();
			
			while (it.hasNext()){
				removeDubString +=it.next()+token.trim();
			}
			
			if (!StringUtil.isEmpty(removeDubString)){
				removeDubString=removeDubString.substring(0, removeDubString.lastIndexOf(token.trim()));
			}
		}
		
		return removeDubString;		
	}
	
	public static void splitDate(String str, Map<String, Object> paramMap, String separator, String prefix) {
		if (!isEmpty(str)) {
			String[] strArr = str.split(separator);
			
			paramMap.put(prefix+"From", strArr[0]);
			paramMap.put(prefix+"To", strArr[1]);
		}
	}
	
	public static String tabTitleSubStr(String code, String value) {
		String tabData = CoCodeManager.getCodeString(code, value);
		
		if (tabData.length() > 12) {
			return tabData.substring(0, 11) + "...";
		} else {
			return tabData;
		}
	}
	
	public static String getDiffItemCommentPartner(PartnerMaster beforeBean, PartnerMaster afterBean) {
		String comment = "<p><strong>Division</strong><br />";
		comment += "Before : " + CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, beforeBean.getDivision()) + "<br />";
		comment += "After : <span style='background-color:yellow'>" + CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, afterBean.getDivision()) + "</span><br /></p>";
		
		return comment;
	}
	
	public static String getDiffItemComment(Project beforeBean, Project afterBean) {
		return getDiffItemComment(beforeBean, afterBean, false);
	}
	
	public static String getDiffItemComment(Project beforeBean, Project afterBean, boolean booleanFlag) {
		String comment = "";
		
		if (booleanFlag) {
			// Project Name
			if (!avoidNull(beforeBean.getPrjName()).equals(avoidNull(afterBean.getPrjName()))) {
				comment += "<p><strong>Project Name</strong><br />";
				comment += "Before : " + beforeBean.getPrjName() + "<br />";
				comment += "After : <span style='background-color:yellow'>" + afterBean.getPrjName() + "</span></p>";
			}

			// Project Version
			if (!avoidNull(beforeBean.getPrjVersion()).equals(avoidNull(afterBean.getPrjVersion()))) {
				String beforePrjVersion = isEmpty(beforeBean.getPrjVersion()) ? "N/A" : beforeBean.getPrjVersion();
				String afterPrjVersion = isEmpty(afterBean.getPrjVersion()) ? "N/A" : afterBean.getPrjVersion();
				
				comment += "<p><strong>Project Version</strong><br />";
				comment += "Before : " + beforePrjVersion + "<br />";
				comment += "After : <span style='background-color:yellow'>" + afterPrjVersion + "</span></p>";
			}
			
			// Permission
			if (!avoidNull(beforeBean.getPublicYn()).equals(avoidNull(afterBean.getPublicYn()))) {
				String beforePermission = CoConstDef.FLAG_YES.equals(beforeBean.getPublicYn()) ? "Everyone" : "Creator & Editor";
				String afterPermission = CoConstDef.FLAG_YES.equals(afterBean.getPublicYn()) ? "Everyone" : "Creator & Editor";
				
				comment += "<p><strong>View Permission</strong><br />";
				comment += "Before : " + beforePermission + "<br />";
				comment += "After : <span style='background-color:yellow'>" + afterPermission + "</span></p>";
			}
			
			// Operating System
			if (!avoidNull(beforeBean.getOsType()).equals(avoidNull(afterBean.getOsType()))) {
				comment += "<p><strong>Operating System</strong><br />";
				comment += "Before : " + CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, beforeBean.getOsType()) + "<br />";
				comment += "After : <span style='background-color:yellow'>" + CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, afterBean.getOsType()) + "</span></p>";
			}
			
			// Distribution Type
			if (!avoidNull(beforeBean.getDistributionType()).equals(avoidNull(afterBean.getDistributionType()))) {
				comment += "<p><strong>Distribution Type</strong><br />";
				comment += "Before : " + CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, beforeBean.getDistributionType()) + "<br />";
				comment += "After : <span style='background-color:yellow'>" + CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, afterBean.getDistributionType()) + "</span></p>";
			}
			
			if (!avoidNull(beforeBean.getNetworkServerType()).equals(avoidNull(afterBean.getNetworkServerType()))) {
				comment += "<p><strong>Network Service only?</strong><br />";
				comment += "Before : " + beforeBean.getNetworkServerType() + "<br />";
				comment += "After : <span style='background-color:yellow'>" + afterBean.getNetworkServerType() + "</span></p>";
			}
			
			if (!avoidNull(beforeBean.getDistributeTarget()).equals(avoidNull(afterBean.getDistributeTarget()))) {
				comment += "<p><strong>Distribution Site</strong><br />";
				comment += "Before : " + beforeBean.getDistributeTarget() + "<br />";
				comment += "After : <span style='background-color:yellow'>" + afterBean.getDistributeTarget() + "</span></p>";
			}
			
			if (!avoidNull(beforeBean.getNoticeType()).equals(avoidNull(afterBean.getNoticeType()))) {
				comment += "<p><strong>OSS Notice</strong><br />";
				comment += "Before : " + CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, beforeBean.getNoticeType()) + "<br />";
				comment += "After : <span style='background-color:yellow'>" + CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, afterBean.getNoticeType()) + "</span></p>";
			}
			
			if (!avoidNull(beforeBean.getPriority()).equals(avoidNull(afterBean.getPriority()))) {
				comment += "<p><strong>Priority</strong><br />";
				comment += "Before : " + CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, beforeBean.getPriority()) + "<br />";
				comment += "After : <span style='background-color:yellow'>" + CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, afterBean.getPriority()) + "</span></p>";
			}
			
			String before = "";
			String after = "";
			if (!isEmpty(beforeBean.getComment())) {
				before = Jsoup.parse(beforeBean.getComment()).text();
			}
			if (!isEmpty(afterBean.getComment())) {
				after = Jsoup.parse(afterBean.getComment()).text();
			}
			if (!before.equals(after)) {
				comment += "<p><strong>Additional Information</strong><br />";
				comment += "Before : <br/>" + beforeBean.getComment() + "<br/>";
				comment += "After : <span style='background-color:yellow'><br/>" + afterBean.getComment() + "</span></p>";
			}

			before = avoidNull(beforeBean.getSecMailDesc()).replaceAll("(\r\n|\r|\n|\n\r)", "");
			after = avoidNull(afterBean.getSecMailDesc()).replaceAll("(\r\n|\r|\n|\n\r)", "");

			if (!avoidNull(beforeBean.getSecMailYn()).equals(avoidNull(afterBean.getSecMailYn())) || !before.equals(after)) {
				comment += "<p><strong>Security Mail</strong><br />";
				comment += "Before : " + (beforeBean.getSecMailYn().equals("Y") ? "Enable" : "Disable (" + before+ ")") + "<br />";
				comment += "After : <span style='background-color:yellow'>" + (afterBean.getSecMailYn().equals("Y") ? "Enable" : "Disable (" + after + ")") + "</span><br /></p>";
			}

			before = avoidNull(beforeBean.getSecPersonNm()).replaceAll("(\r\n|\r|\n|\n\r)", "");
			after = avoidNull(afterBean.getSecPersonNm()).replaceAll("(\r\n|\r|\n|\n\r)", "");

			if (!avoidNull(beforeBean.getSecPersonNm()).equals(avoidNull(afterBean.getSecPersonNm())) || !before.equals(after)) {
				comment += "<p><strong>Security Responsible Person</strong><br />";
				comment += "Before : " + avoidNull(beforeBean.getSecPersonNm()) + "<br />";
				comment += "After : <span style='background-color:yellow'>" + avoidNull(afterBean.getSecPersonNm()) + "</span><br /></p>";
			}
			
			// Model Information
			if (!CollectionUtils.isEmpty(beforeBean.getModelList()) || !CollectionUtils.isEmpty(afterBean.getModelList())) {
				List<String> _beforeList = new ArrayList<>();
				if (!CollectionUtils.isEmpty(beforeBean.getModelList())){
					for (int i=0; i < beforeBean.getModelList().size(); i++){
						String categoryName = CommonFunction.makeCategoryFormat(beforeBean.getDistributeTarget(), beforeBean.getModelList().get(i).getCategory());
						String before_str = categoryName + "/" + beforeBean.getModelList().get(i).getModelName() + "/" + beforeBean.getModelList().get(i).getReleaseDate();
						_beforeList.add(before_str);
					}
				}
						 
				List<String> _afterList = new ArrayList<>();
				if (!CollectionUtils.isEmpty(afterBean.getModelList())){
					for (int i=0; i < afterBean.getModelList().size(); i++){
						String categoryName = CommonFunction.makeCategoryFormat(afterBean.getDistributeTarget(), afterBean.getModelList().get(i).getCategory());
						String after_str = categoryName + "/" + afterBean.getModelList().get(i).getModelName() + "/" + afterBean.getModelList().get(i).getReleaseDate();
						_afterList.add(after_str);
					}
				}
				
				List<String> _newBeforeList = new ArrayList<>(_beforeList);
				List<String> _newAfterList = new ArrayList<>(_afterList);

				Collections.sort(_newBeforeList);
				Collections.sort(_newAfterList);
				
				String modelComment = "";
				String beforeComment = "";
				String afterComment = "";
				if (!CollectionUtils.isEmpty(_newBeforeList) && !CollectionUtils.isEmpty(_newAfterList)) {
					List<String> matchList = _newBeforeList.stream().filter(o -> _newAfterList.stream().anyMatch(Predicate.isEqual(o))).collect(Collectors.toList());
					if (matchList.size() != _newBeforeList.size() || matchList.size() != _newAfterList.size()) {
						List<String> beforeNoneMatchList = _newBeforeList.stream().filter(o -> _newAfterList.stream().noneMatch(Predicate.isEqual(o))).collect(Collectors.toList());
						List<String> afterNoneMatchList = _newAfterList.stream().filter(o -> _newBeforeList.stream().noneMatch(Predicate.isEqual(o))).collect(Collectors.toList());
						modelComment = "<p><strong>Model Information</strong><br />";
						beforeComment = "Before : ";
						for (int i=0; i<_newBeforeList.size(); i++) {
							beforeComment += "<span>" + _newBeforeList.get(i) + "</span><br />";
						}
						afterComment = "After : ";
						for (int i=0; i<matchList.size(); i++) {
							afterComment += "<span>" + matchList.get(i) + "</span><br />";
						}
						for (int i=0; i<beforeNoneMatchList.size(); i++) {
							afterComment += "<span style='background-color:red'>" + beforeNoneMatchList.get(i) + "</span><br />";
						}
						for (int i=0; i<afterNoneMatchList.size(); i++) {
							afterComment += "<span style='background-color:yellow'>" + afterNoneMatchList.get(i) + "</span><br />";
						}
						modelComment += beforeComment + afterComment + "</p>";
					}
				} else if (CollectionUtils.isEmpty(_newBeforeList) && !CollectionUtils.isEmpty(_afterList)) {
					modelComment = "<p><strong>Model Information</strong><br />";
					beforeComment = "Before : <br/>";
					afterComment = "After : ";
					for (int i=0; i<_afterList.size(); i++) {
						afterComment += "<span style='background-color:yellow'>" + _afterList.get(i) + "</span>";
						if (i < _afterList.size()-1) {
							afterComment += "<br />";
						}
					}
					modelComment += beforeComment + afterComment + "</p>";
				} else if (!CollectionUtils.isEmpty(_newBeforeList) && CollectionUtils.isEmpty(_afterList)) {
					modelComment = "<p><strong>Model Information</strong><br />";
					beforeComment = "Before : ";
					for (int i=0; i<_beforeList.size(); i++) {
						beforeComment += "<span>" + _beforeList.get(i) + "</span><br />";
					}
					afterComment = "After : ";
					for (int i=0; i<_beforeList.size(); i++) {
						afterComment += "<span style='background-color:red'>" + _beforeList.get(i) + "</span><br />";
					}
					modelComment += beforeComment + afterComment + "</p>";
				}
				if (!isEmpty(modelComment)) {
					comment += modelComment;
				}
			}
		} else {
			// Project Division
			if (!avoidNull(beforeBean.getDivision()).equals(avoidNull(afterBean.getDivision()))) {
				comment += "<p><strong>Division</strong><br />";
				comment += "Before : " + CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, beforeBean.getDivision()) + "<br />";
				comment += "After : <span style='background-color:yellow'>" + CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, afterBean.getDivision()) + "</span><br /></p>";
			}
		}
		
		return comment;
	}
	
	public static void calcTotListSize(Map<String, Object> map) {
		int totListSize = (int) map.get("totListSize");
		int pageListSize = Integer.parseInt((String) map.get("rows"));
		int curPage = Integer.parseInt((String) map.get("page"));
		int blockSize = 10;
		
		if (map.containsKey("blockSize")) {
			blockSize = Integer.parseInt((String) map.get("blockSize"));
		}
		
		int totBlockSize = totListSize/pageListSize < 1 ? 1 : totListSize%pageListSize==0?totListSize/pageListSize:(totListSize/pageListSize)+1;

		int startIndex = (curPage-1)*pageListSize;
		
		int totBlockPage = (totBlockSize / blockSize);
		if (totBlockSize != blockSize) {
			totBlockPage++;
		}
		
		int blockPage = ((curPage-1) / blockSize) + 1;
		
		int blockStart = ((blockPage-1) * blockSize) + 1;
		int blockEnd = blockStart+blockSize-1;
		if (blockEnd > totBlockSize) {
			blockEnd = totBlockSize;
		}
		
		map.put("totBlockSize", totBlockSize);
		map.put("startIndex", startIndex);
		map.put("totBlockPage", totBlockPage);
		map.put("blockStart", blockStart);
		map.put("blockEnd", blockEnd);
		map.put("pageListSize", pageListSize);
		map.put("curPage", curPage);
	}
	
	public static String getProperty(String key){
		try {
			return appEnv.getProperty(key);
		} catch (IllegalArgumentException e) {}
		return "";
	}
	
	public static String emptyCheckProperty(String key) {
		return emptyCheckProperty(key, key);
	}
	
	public static String emptyCheckProperty(String key, String defaultValue) {
		return appEnv.containsProperty(key) ? getProperty(key) : defaultValue;
	}
	
	public static boolean propertyFlagCheck(String key, String checkFlag) {
		String flag = avoidNull(getProperty(key), CoConstDef.FLAG_NO);
		
		return flag.equals(checkFlag);
	}
	
	public static String appendProperty(String beforeKey, String afterKey) {
		String beforeStr = emptyCheckProperty(beforeKey);
		String afterStr = emptyCheckProperty(afterKey, "");
		
		return beforeStr + afterStr;
	}
	
	public static String loginUserName() {
    	try {
    		return SecurityContextHolder.getContext().getAuthentication().getName(); 
    	} catch(Exception e) { 
    		return "";
    	}
	}
	
	public static String getDomain(HttpServletRequest req) {
		return req.getScheme() + "://" + req.getServerName() + ("LOCALHOST".equals(req.getServerName().toUpperCase()) ? ":" + req.getServerPort() + req.getContextPath() : "");		
	}

	public static String makeJdbcUrl(String jdbcUrl) {
		if (StringUtil.isEmpty(jdbcUrl)) {
			return jdbcUrl;
		}
		if (!jdbcUrl.startsWith("jdbc:")) {
			jdbcUrl = "jdbc:mysql://" + jdbcUrl;
		}
		if (!jdbcUrl.contains("serverTimezone")) {
			jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "serverTimezone=UTC";
		}
		if (!jdbcUrl.contains("autoReconnect")) {
			jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "autoReconnect=true";
		}
		return jdbcUrl;
	}
	
	public static String getOssDownloadLocation(String ossName, String ossVersion) {
		if (!isEmpty(ossName) && CoCodeManager.OSS_INFO_UPPER.containsKey( (ossName + "_" + avoidNull(ossVersion)).toUpperCase() )) {
			return avoidNull( CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(ossVersion)).toUpperCase() ).getDownloadLocation() );
		}
		return "";
	}

	public static Object convertStringToMap (String StringMap, String key) {
		Gson gson = CommonFunction.getGsonBuiler();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> resultMap = (Map<String, Object>) gson.fromJson(StringMap , type);
		
		return resultMap.containsKey(key) ? resultMap.get(key) : resultMap;
	}
	
	public static String makeDuplicateErrorMessage(String prjId) {
		String distributionDuplicateMsg = "";
		distributionDuplicateMsg += getMessage("msg.distribute.description.duplicate");
		distributionDuplicateMsg += getMessage("msg.distribute.origin.link", new String[]{prjId});
		
		return distributionDuplicateMsg;
	}

	public static String convertCveIdToLink(String cveId) {
		if (StringUtil.isEmpty(cveId)) {
			return "";
		}
		return cveId.replaceAll("((cve|CVE)-[0-9]{4}-[0-9]{4,})",
				"<a href='https://nvd.nist.gov/vuln/detail/$1' target='_blank'>$1<a/>");
	}

	public static List<ProjectIdentification> removeDuplicateLicense(List<ProjectIdentification> ossComponents) {
		for (ProjectIdentification pri : ossComponents) {
			if (pri.getLicenseName() != null && pri.getLicenseName().contains(",")) {
				List<String> licenseNameList = Arrays.asList(pri.getLicenseName().split(","));
				List<String> licenseNameNicknameCheckList = new ArrayList<>();
				List<String> duplicateList = new ArrayList<>();
				List<Integer> indexList = new ArrayList<>();
				
				for (int j=0; j<licenseNameList.size(); j++) {
	                String licenseName = licenseNameList.get(j).trim();
	                if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
	                	LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
	                    if (licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
	                    	boolean flag = false;
	                    	for (String s : licenseMaster.getLicenseNicknameList()) {
	                    		if (licenseName.equalsIgnoreCase(s)) {
	                    			String disp = avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp());
	                    			licenseNameNicknameCheckList.add(disp);
	                    			flag = true;
	                    			break;
	                    		}
	                    	}
	                    	
	                    	if (!flag) {
	                    		licenseNameNicknameCheckList.add(licenseName);
	                    	}
	                    }else {
	                    	licenseNameNicknameCheckList.add(licenseName);
	                    }
	                }else {
	                	licenseNameNicknameCheckList.add(licenseName);
	                }
				}
				
				for (int i=0; i<licenseNameNicknameCheckList.size(); i++) {
					if (!duplicateList.contains(licenseNameNicknameCheckList.get(i))) {
						duplicateList.add(licenseNameNicknameCheckList.get(i));
						indexList.add(i);
					}
				}
				
				duplicateList = new ArrayList<>();
				for (int i=0; i<indexList.size(); i++) {
					duplicateList.add(licenseNameList.get(indexList.get(i)));
				}
				
				pri.setLicenseName(String.join(",", duplicateList));
			}
		}	
		
		return ossComponents;
	}

	/**
	 * Set Unclear Obligation Flag (Self-Check)
	 * @param list
	 * @param warningCodeMap 
	 * @param errorCodeMap 
	 * @return
	 */
	public static List<ProjectIdentification> identificationUnclearObligationCheck(List<ProjectIdentification> list, Map<String, String> errorCodeMap, Map<String, String> warningCodeMap) {
		List<String> UNCLEAR_OBLIGATION_CODE_LIST = Arrays.asList(new String[] {
				"LICENSE_NAME.REQUIRED" ,"LICENSE_NAME.UNCONFIRMED", "LICENSE_NAME.INCLUDE_MULTI_OPERATE", "LICENSE_NAME.NOLICENSE", "LICENSE_NAME.INCLUDE_DUAL_OPERATE"
		});
		
		List<String> UNCLEAR_OBLIGATION_CODE_NOTLICENSE_LIST = Arrays.asList(new String[] {
				"OSS_NAME.REQUIRED", "OSS_NAME.UNCONFIRMED", "OSS_VERSION.UNCONFIRMED", "OSS_NAME.DEACTIVATED"
		});
		
		List<String> unclearObligationList = new ArrayList<>();
		List<String> unclearObligationNotLicenseList = new ArrayList<>();
		
		if (errorCodeMap != null) {
			for (String key : errorCodeMap.keySet()) {
				if (key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_LIST.contains(errorCodeMap.get(key))) {
					unclearObligationList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
				if (key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_NOTLICENSE_LIST.contains(errorCodeMap.get(key))) {
					unclearObligationNotLicenseList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
			}
		}
		if (warningCodeMap != null) {
			for (String key : warningCodeMap.keySet()) {
				if (key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_LIST.contains(warningCodeMap.get(key))) {
					unclearObligationList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
				if (key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_NOTLICENSE_LIST.contains(warningCodeMap.get(key))) {
					unclearObligationNotLicenseList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
			}
		}
		
		if (list != null) {
			for (ProjectIdentification bean : list) {
				if ("-".equals(bean.getOssName())  
						&& !unclearObligationList.contains(bean.getGridId()) 
						&& isEmpty(bean.getObligationType()) 
						&& !checkIncludeUnconfirmedLicense(bean.getComponentLicenseList())) {
					String obligationType = CommonFunction.getObligationTypeWithSelectedLicense(bean);
					if (!isEmpty(obligationType)) {
						bean.setObligationType(obligationType);
						continue;
					}
				}
				
				// Check New license, the actual message is (Declared : ~~), so check again registered license.
				if (unclearObligationList.contains(bean.getGridId())
						|| (!isEmpty(bean.getObligationType()) && (checkIncludeUnconfirmedLicense(bean.getComponentLicenseList()) || checkIncludeNotDeclaredLicense(bean.getOssName(), bean.getOssVersion(), bean.getComponentLicenseList())))) {
					bean.setObligationGrayFlag(CoConstDef.FLAG_YES);
					bean.setObligationMsg(getMessage("msg.project.obligation.unclear"));
				}
				 
				if (unclearObligationNotLicenseList.contains(bean.getGridId())) {
					if (isEmpty(bean.getObligationType())){
						String obligationType = CommonFunction.getObligationTypeWithSelectedLicense(bean);
						if (!isEmpty(obligationType)) {
							bean.setObligationType(obligationType);
							continue;
						}
					}
				}
			}
		}
		return list;
	}

	private static boolean checkIncludeNotDeclaredLicense(String ossName, String ossVer,
			List<ProjectIdentification> licenseList) {
		if (licenseList != null) {
			List<String> licenseNameList = getAllAvailableLicenseUpperCaseName(ossName, ossVer);
			for (ProjectIdentification license : licenseList) {
				if (license.getLicenseName() != null) {
					if (!licenseNameList.contains(license.getLicenseName().toUpperCase())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static List<String> getAllAvailableLicenseUpperCaseName(String ossName, String ossVer) {
		String key = (avoidNull(ossName).trim() + "_" + avoidNull(ossVer).trim()).toUpperCase();
		List<String> licenseNameList = new ArrayList<>();
		if (CoCodeManager.OSS_INFO_UPPER.containsKey(key)) {
			OssMaster bean = CoCodeManager.OSS_INFO_UPPER.get(key);
			for (OssLicense _temp : Optional.ofNullable(bean.getOssLicenses()).orElse(new ArrayList<>())) {
				licenseNameList.add(_temp.getLicenseName().toUpperCase());
				LicenseMaster _license = CoCodeManager.LICENSE_INFO_BY_ID.get(_temp.getLicenseId());
				if (_license != null) {
					for (String _nick : Optional.ofNullable(_license.getLicenseNicknameList()).orElse(new ArrayList<>())) {
						licenseNameList.add(_nick.toUpperCase());
					}
				}
			}
			
			for (String _temp : Optional.ofNullable(bean.getDetectedLicenses()).orElse(new ArrayList<>())) {
				licenseNameList.add(_temp.toUpperCase());
				LicenseMaster _license = CoCodeManager.LICENSE_INFO_UPPER.get(_temp.toUpperCase());
				if (_license != null) {
					for (String _nick : Optional.ofNullable(_license.getLicenseNicknameList()).orElse(new ArrayList<>())) {
						licenseNameList.add(_nick.toUpperCase());
					}
				}
			}
		}
		return licenseNameList;
	}

	private static boolean checkIncludeUnconfirmedLicense(List<ProjectIdentification> licenseList) {
		if (licenseList != null) {
			for (ProjectIdentification bean : licenseList) {
				if (!isEmpty(bean.getLicenseName()) && !CoCodeManager.LICENSE_INFO_UPPER.containsKey(bean.getLicenseName().toUpperCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<ProjectIdentification> makeLicensePermissiveList(List<ProjectIdentification> licenses, String currentLicense) {
		List<List<ProjectIdentification>> andCombLicenseList = new ArrayList<>();
		
		for (ProjectIdentification bean : licenses) {
			if (andCombLicenseList.size() == 0 || "OR".equals(bean.getOssLicenseComb())) {
				andCombLicenseList.add(new ArrayList<>());
			}
			
			andCombLicenseList.get(andCombLicenseList.size()-1).add(bean);
		}
		
		List<ProjectIdentification> licenseList = new ArrayList<>();
		int pmsCnt = 0;
		int wcpCnt = 0;
		int cpCnt = 0;
		int pfCnt = 0;
		int naCnt = 0;
		
		for (List<ProjectIdentification> andList : andCombLicenseList) {
			switch(getLicensePermissive(andList)) {
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					if (pmsCnt == 0) {
						licenseList = andList;
						pmsCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if (pmsCnt == 0 && wcpCnt == 0) {
						licenseList = andList;
						wcpCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_CP:
					if (pmsCnt == 0 && wcpCnt == 0 && cpCnt == 0) {
						licenseList = andList;
						cpCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PF:
					if (pmsCnt == 0 && wcpCnt == 0 && cpCnt == 0 && pfCnt == 0) {
						licenseList = andList;
						pfCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_NA:
					if (pmsCnt == 0 && wcpCnt == 0 && cpCnt == 0 && pfCnt == 0 && naCnt == 0) {
						licenseList = andList;
						naCnt++;
					}
					
					break;
			}
		}
		
		return licenseList;
	}

	public static List<String> checkUserPermissions(String userId, String[] prjIds, String gubn) {
		List<String> notPermissionList = new ArrayList<>();
		List<String> userIdList = null;
		
		switch(gubn) {
		
		case "project":
			Project param = new Project();
			for (int i=0; i<prjIds.length; i++) {
				userIdList = new ArrayList<>();
				Project bean = projectService.getProjectBasicInfo(prjIds[i]);
				userIdList.add(bean.getCreator());
				
				param.setPrjId(prjIds[i]);
				List<Project> watcherList = projectService.getWatcherList(param);
				if (watcherList != null) {
					for (Project watcher : watcherList) {
						if (!isEmpty(watcher.getPrjUserId()) && !userIdList.contains(watcher.getPrjUserId())) {
							userIdList.add(watcher.getPrjUserId());
						}
					}
				}
				
				if (!isEmpty(userId)) {
					if (!userIdList.contains(userId)) {
						notPermissionList.add(prjIds[i]);
					}
				} else {
					notPermissionList = userIdList;
				}
			}
			break;
		
		case "partner":
			PartnerMaster partner = new PartnerMaster();
			for (int i=0; i<prjIds.length; i++) {
				userIdList = new ArrayList<>();
				partner.setPartnerId(prjIds[i]);
				PartnerMaster bean = partnerService.getPartnerMasterOne(partner);
				
				userIdList.add(bean.getCreator());
				if (bean.getPartnerWatcher() != null) {
					for (String watcher : bean.getPartnerWatcher().stream().map(e -> e.getParUserId()).collect(Collectors.toList())) {
						if (!isEmpty(watcher)) {
							userIdList.add(watcher);
						}
					}
				}
				
				userIdList = userIdList.stream().distinct().collect(Collectors.toList());
				if (!isEmpty(userId)) {
					if (!userIdList.contains(userId)) {
						notPermissionList.add(prjIds[i]);
					}
				} else {
					notPermissionList = userIdList;
				}
			}
			break;
		}
		
		Collections.sort(notPermissionList);
		return notPermissionList;
	}
	
	public static void setSslWithCert() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		// Load CAs from an InputStream
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream caInput = new BufferedInputStream(new FileInputStream(getProperty("lge.root.ca.file")));
		Certificate ca;
		try {
			ca = cf.generateCertificate(caInput);
			log.debug("ca=" + ((X509Certificate) ca).getSubjectDN());
		} finally {
			caInput.close();
		}
		// Create a KeyStore containing our trusted CAs
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		keyStore.setCertificateEntry("ca", ca);
		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);
		// Create an SSLContext that uses our TrustManager
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		SSLContext.setDefault(context);
	}
	
	public void setSslWithNoCert() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext context = SSLContext.getInstance("TLS");
		X509TrustManager tm;
		tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
			public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
			public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
		};
		context.init(null, new TrustManager[]{tm}, null);
		SSLContext.setDefault(context);
	}

	public static String getDeduplicateCveInfo(String referenceId, String cvssScoreMax, Map<String, OssMaster> ossInfoMap, ProjectIdentification identification, List<String> vendorProjectMatchList, String standardScore) {
		String ossName = identification.getOssName();
		String refOssName = avoidNull(identification.getRefOssName(), identification.getOssName());
		String ossVersion = avoidNull(identification.getOssVersion());
		
		OssMaster om = new OssMaster();
		om.setPrjId(referenceId);
		
		boolean vendorProductCheckFlag = false;
		String ossId = null;
		if (!isEmpty(ossName) && !ossName.equals("-")){
			OssMaster bean = ossInfoMap.get((avoidNull(refOssName, ossName)+"_"+ossVersion).toUpperCase());
			if (bean != null) ossId = bean.getOssId();
		}
		
		String[] cvssScoreMaxString = cvssScoreMax.split("\\@");
		String vendorProductName = cvssScoreMaxString[2] + "-" + cvssScoreMaxString[0];
		String existenceOssName = (cvssScoreMaxString[2] + "-" + cvssScoreMaxString[0] + "_" + ossVersion).toUpperCase();
		String product = cvssScoreMaxString[0];
		Float cvssScore = Float.valueOf(cvssScoreMaxString[3]);
		
		om.setOssName(avoidNull(refOssName, ossName));
		om.setOssVersion(ossVersion);
		String[] ossNicknames = null;
		if (!isEmpty(refOssName)) {
			ossNicknames = ossService.getOssNickNameListByOssName(refOssName);
		} else {
			ossNicknames = ossService.getOssNickNameListByOssName(ossName);
		}
		if (ossNicknames != null) om.setOssNicknames(ossNicknames);
		
		om.setOssName(product);
		om.setSchOssName(avoidNull(refOssName, ossName));
		om.setOssVersion(om.getOssVersion().isEmpty() ? "-" : om.getOssVersion());
		
		List<String> cveInfoList = ossService.selectVulnInfoForOss(om);
		List<String> newList = new ArrayList<>();
		
		for (String cveInfo : cveInfoList) {
			if (!cveInfo.equalsIgnoreCase(cvssScoreMax)) newList.add(cveInfo);
		}
		
		if (!newList.isEmpty()) {
			newList = newList.stream().distinct().collect(Collectors.toList());
			if (newList.size() > 1) {
				Collections.sort(newList, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						if (new BigDecimal(o1.split("\\@")[3]).compareTo(new BigDecimal(o2.split("\\@")[3])) > 0) {
							return -1;
						}else {
							return 1;
						}
					}
				});
			}
			return newList.get(0);
		} else {
			return null;
		}
	}
	
	public static String getConversionCveInfo(String referenceId, Map<String, OssMaster> ossInfoMap, ProjectIdentification identification, List<String> cvssScoreMaxVendorProductList, List<String> cvssScoreMaxList, boolean vulnFixedCheckFlag) {
		List<String> rtnScoreList = new ArrayList<>();
		List<String> cvssScoreList = null;
		
		String ossName = identification.getOssName();
		String refOssName = avoidNull(identification.getRefOssName(), identification.getOssName());
		String ossVersion = avoidNull(identification.getOssVersion());
		
		OssMaster om = new OssMaster();
		om.setPrjId(referenceId);
		
		cvssScoreList = cvssScoreMaxList;
		
		if (!cvssScoreList.isEmpty()) {
			String[] cvssScoreMaxString = null;
			for (String cvssScoreMaxStr : cvssScoreList) {
				cvssScoreMaxString = cvssScoreMaxStr.split("\\@");
				String product = cvssScoreMaxString[0];
				
				om.setOssName(avoidNull(refOssName, ossName));
				om.setOssVersion(ossVersion);
				String[] ossNicknames = null;
				if (!isEmpty(refOssName)) {
					ossNicknames = ossService.getOssNickNameListByOssName(refOssName);
				} else {
					ossNicknames = ossService.getOssNickNameListByOssName(ossName);
				}
				if (ossNicknames != null) om.setOssNicknames(ossNicknames);
				
				if (vulnFixedCheckFlag) {
					om.setVulnerabilityCheckFlag(CoConstDef.FLAG_YES);
				} else {
					om.setVulnerabilityCheckFlag(null);
				}
				
				om.setOssName(product);
				om.setSchOssName(avoidNull(refOssName, ossName));
				om.setOssVersion(om.getOssVersion().isEmpty() ? "-" : om.getOssVersion());
				List<String> cveDataList2 = ossService.selectVulnInfoForOss(om);
				if (cveDataList2 != null && !cveDataList2.isEmpty()) rtnScoreList.addAll(cveDataList2);
			}
			
			if (!rtnScoreList.isEmpty()) {
				rtnScoreList = rtnScoreList.stream().distinct().collect(Collectors.toList());
				if (rtnScoreList.size() > 1) {
					Collections.sort(rtnScoreList, new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							if (new BigDecimal(o1.split("\\@")[3]).compareTo(new BigDecimal(o2.split("\\@")[3])) > 0) {
								return -1;
							}else {
								return 1;
							}
						}
					});
				}
				return rtnScoreList.get(0);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String getConversionCveInfoForList(List<String> cvssScoreMaxList) {
		List<String> cvssScoreList = cvssScoreMaxList;
		
		if (!cvssScoreList.isEmpty()) {
			cvssScoreList = cvssScoreList.stream().distinct().collect(Collectors.toList());
			if (cvssScoreList.size() > 1) {
				Collections.sort(cvssScoreList, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						if (new BigDecimal(o1.split("\\@")[3]).compareTo(new BigDecimal(o2.split("\\@")[3])) > 0) {
							return -1;
						}else {
							return 1;
						}
					}
				});
			}
			
			return cvssScoreList.get(0);
		} else {
			return null;
		}
	}

	public static String checkNvdInfoForProduct(Map<String, OssMaster> ossInfoMap, List<String> productCheckList) {
		List<String> rtnScoreList = new ArrayList<>();
		if (productCheckList != null) rtnScoreList.addAll(productCheckList);
//		OssMaster om = new OssMaster();
//		String[] cvssScoreMaxString = null;
//		
//		for (String cvssScoreMaxStr : productCheckList) {
//			cvssScoreMaxString = cvssScoreMaxStr.split("\\@");
//			if (!cvssScoreMaxString[2].isEmpty()) {
//				boolean cvssScoreCheckFlag = false;
//				String ossVersion = !cvssScoreMaxString[0].equals("-") ? cvssScoreMaxString[1] : "";
//				OssMaster bean = ossInfoMap.get((cvssScoreMaxString[0] + "_" + ossVersion).toUpperCase());
//				
//				om.setSchOssName(cvssScoreMaxString[0] + "-" + cvssScoreMaxString[2]);
//				om.setOssVersion(ossVersion);
//				
//				List<String> matchOssIdList = ossService.checkExistsVendorProductMatchOss(om);
//				if (matchOssIdList != null && !matchOssIdList.isEmpty() && bean != null) {
//					for (String matchOssId : matchOssIdList) {
//						if (matchOssId.equals(bean.getOssId())) {
//							cvssScoreCheckFlag = true;
//							break;
//						}
//					}
//				}
//				
//				if (!cvssScoreCheckFlag) {
//					rtnScoreList.add(cvssScoreMaxStr);
//				}
//			} else {
//				rtnScoreList.add(cvssScoreMaxStr);
//			}
//		}
		
		if (!rtnScoreList.isEmpty()) {
			rtnScoreList = rtnScoreList.stream().distinct().collect(Collectors.toList());
			if (rtnScoreList.size() > 1) {
				Collections.sort(rtnScoreList, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						if (new BigDecimal(o1.split("\\@")[3]).compareTo(new BigDecimal(o2.split("\\@")[3])) > 0) {
							return -1;
						}else {
							return 1;
						}
					}
				});
			}
			return rtnScoreList.get(0);
		} else {
			return null;
		}
	}

	public static void getDisplayIdentificationBtn(String identificationStatus, String viewOnlyFlag, Map<String, Object> btnShowMap) {
		if (CommonFunction.isAdmin()) {
			switch (identificationStatus) {
				case "" : btnShowMap.put("requestBtn", CoConstDef.FLAG_YES); break;
				case "PROG": btnShowMap.put("requestBtn", CoConstDef.FLAG_YES); break;
				case "REQ": btnShowMap.put("reviewBtn", CoConstDef.FLAG_YES); break;
				case "REV": btnShowMap.put("confirmBtn", CoConstDef.FLAG_YES); break;
				case "CONF": btnShowMap.put("rejectBtn", CoConstDef.FLAG_YES); break;
			}
			if (!btnShowMap.containsKey("requestBtn")) btnShowMap.put("requestBtn", CoConstDef.FLAG_NO);
			if (!btnShowMap.containsKey("reviewBtn")) btnShowMap.put("reviewBtn", CoConstDef.FLAG_NO);
			if (!btnShowMap.containsKey("confirmBtn")) btnShowMap.put("confirmBtn", CoConstDef.FLAG_NO);
			if (!btnShowMap.containsKey("rejectBtn")) btnShowMap.put("rejectBtn", CoConstDef.FLAG_NO);
		} else if (viewOnlyFlag.equals("Y")) {
			btnShowMap.put("requestBtn", CoConstDef.FLAG_NO);
			btnShowMap.put("reviewBtn", CoConstDef.FLAG_NO);
			btnShowMap.put("confirmBtn", CoConstDef.FLAG_NO);
			btnShowMap.put("rejectBtn", CoConstDef.FLAG_NO);
		} else {
			switch (identificationStatus) {
				case "" : btnShowMap.put("requestBtn", CoConstDef.FLAG_YES); break;
				case "PROG": btnShowMap.put("requestBtn", CoConstDef.FLAG_YES); break;
				case "REQ": btnShowMap.put("rejectBtn", CoConstDef.FLAG_YES); break;
				case "CONF": btnShowMap.put("rejectBtn", CoConstDef.FLAG_YES); break;
			}
			if (!btnShowMap.containsKey("requestBtn")) btnShowMap.put("requestBtn", CoConstDef.FLAG_NO);
			if (!btnShowMap.containsKey("reviewBtn")) btnShowMap.put("reviewBtn", CoConstDef.FLAG_NO);
			if (!btnShowMap.containsKey("confirmBtn")) btnShowMap.put("confirmBtn", CoConstDef.FLAG_NO);
			if (!btnShowMap.containsKey("rejectBtn")) btnShowMap.put("rejectBtn", CoConstDef.FLAG_NO);
		}
	}

	public static String getMessageForVulDOC(HttpServletRequest request, String gubn) {
		String vulDocMsg = null;
		if (gubn.equals("inst")) {
			String installLink = "<a target='_blank' href='http://collab.lge.com/main/x/jhbZeg' style='color:blue;'>VulDOC Privacy and Credential Analyzer - Install</a>";
			String webLink = "<a target='_blank' href='http://collab.lge.com/main/x/Sb2ig' style='color:blue;'>VulDOC Privacy and Credential Analyzer - Web</a>";
			String isInfo = "<a target='_blank' href='http://collab.lge.com/main/x/NyM_cg' style='color:blue;'>";
			
			String lang = "";
			Cookie[] cookies = request.getCookies();
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("lang")) {
					lang = cookie.getValue();
					break;
				}
			}
			String vulDocInfoLink = "<a target='_blank' href='http://collab.lge.com/main/display/SWSEC/%5B6%5D+Getting+Help+and+Support' style='color:blue;'>SW Security Governance Team</a>";
			if (lang.equals("en-US")) {
				isInfo += "sensitive or credential information<a>";
				vulDocMsg = getMessage("msg.project.packaging.vuldoc.instructions" , new String[]{installLink, webLink, isInfo, vulDocInfoLink});
			} else {
				isInfo += "중요 민감 정보</a>";
				vulDocMsg = getMessage("msg.project.packaging.vuldoc.instructions" , new String[]{installLink, webLink, isInfo, vulDocInfoLink});
			}
		} else {
			String vulDocInfoLink = "<a target='_blank' href='http://collab.lge.com/main/display/SWSEC/%5B6%5D+Getting+Help+and+Support' style='color:blue;'>SW Security Governance Team</a>";
			vulDocMsg = getMessage("msg.project.packaging.vuldoc.info" , new String[]{vulDocInfoLink});
		}
		
		return vulDocMsg;
	}
	
	@SuppressWarnings("unchecked")
	public static void setDeduplicatedMessageInfo(Map<String, Object> result) {
		Map<String, String> customInfoMsg = new HashMap<>();
		
		if (result.containsKey("infoData")) {
			Map<String, String> validDataMap = (Map<String, String>) result.get("validData");
			Map<String, String> diffDataMap = (Map<String, String>) result.get("diffData");
			Map<String, String> infoDataMap = (Map<String, String>) result.get("infoData");
			
			if (validDataMap != null && diffDataMap != null) {
				for (String key : infoDataMap.keySet()) {
					if (key.startsWith("binaryName")) {
						customInfoMsg.put(key, infoDataMap.get(key));
						continue;
					}
					if (!customInfoMsg.containsKey(key) && !validDataMap.containsKey(key) && !diffDataMap.containsKey(key)) {
						customInfoMsg.put(key, infoDataMap.get(key));
					}
				}
			} else if (validDataMap != null) {
				for (String key : infoDataMap.keySet()) {
					if (key.startsWith("binaryName")) {
						customInfoMsg.put(key, infoDataMap.get(key));
						continue;
					}
					if (!customInfoMsg.containsKey(key) && !validDataMap.containsKey(key)) {
						customInfoMsg.put(key, infoDataMap.get(key));
					}
				}
			} else if (diffDataMap != null) {
				for (String key : infoDataMap.keySet()) {
					if (!customInfoMsg.containsKey(key) && key.startsWith("binaryName")) {
						customInfoMsg.put(key, infoDataMap.get(key));
						continue;
					}
					if (!customInfoMsg.containsKey(key) && !diffDataMap.containsKey(key)) {
						customInfoMsg.put(key, infoDataMap.get(key));
					}
				}
			} else {
				customInfoMsg.putAll(infoDataMap);
			}
		}
		
		result.put("infoData", customInfoMsg);
	}
	
	public static Object copyObject(Object obj, String gubn) {
		if (gubn.equals("OM")) {
			OssMaster bean = (OssMaster) obj;
			
			OssMaster copiedBean = new OssMaster();
			copiedBean.setOssId(bean.getOssId());
			copiedBean.setOssName(bean.getOssName());
			copiedBean.setOssVersion(bean.getOssVersion());
			copiedBean.setOssType(bean.getOssType());
			copiedBean.setLicenseName(bean.getLicenseName());
			copiedBean.setOssLicenses(bean.getOssLicenses());
			copiedBean.setDetectedLicenses(bean.getDetectedLicenses());
			copiedBean.setOssNickname(bean.getOssNickname());
			copiedBean.setOssNicknames(bean.getOssNicknames());
			copiedBean.setDownloadLocation(bean.getDownloadLocation());
			copiedBean.setDownloadLocations(bean.getDownloadLocations());
			copiedBean.setHomepage(bean.getHomepage());
			copiedBean.setObligation(bean.getObligation());

			if (!isEmpty(bean.getLicenseDiv())) {
				copiedBean.setMultiLicenseFlag(bean.getLicenseDiv());
				copiedBean.setLicenseDiv(bean.getLicenseDiv());
			}
			
			if (!isEmpty(bean.getLicenseType())) {
				copiedBean.setLicenseType(bean.getLicenseType());
			}
			
			if (!isEmpty(bean.getObligationType())) {
				copiedBean.setObligationType(bean.getObligationType());
			}
			
			if (!isEmpty(bean.getModifiedDate())) {
				copiedBean.setModifiedDate(bean.getModifiedDate());
			}
			
			if (!isEmpty(bean.getModifier())) {
				copiedBean.setModifier(bean.getModifier());
			}
			
			if (!isEmpty(bean.getCreatedDate())) {
				copiedBean.setCreatedDate(bean.getCreatedDate());
			}
			
			if (!isEmpty(bean.getCreator())) {
				copiedBean.setCreator(bean.getCreator());
			}

			copiedBean.setAttribution(bean.getAttribution());
			copiedBean.setSummaryDescription(bean.getSummaryDescription());
			copiedBean.setCopyright(bean.getCopyright());
			
			return copiedBean;
		} else {
			return obj;
		}
	}

	public static String getMessageForDisabled(String msgCode, String status, String step) {
		if (step.equals("identification")) {
			return getMessage(msgCode , new String[]{CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, status)});
		} else {
			return getMessage(msgCode , new String[]{CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, status)});
		}
	}
	
	public static String getCustomMessage(String msgCode, String contents) {
		return getCustomMessage(msgCode, contents, false);
	}
	
	public static String getCustomMessage(String msgCode, String contents, boolean msgPropertyFlag) {
		if (msgPropertyFlag) {
			return getMessage(msgCode, new String[]{getMessage(contents)});
		} else {
			return getMessage(msgCode, new String[]{contents});
		}
	}

	public static void addSystemLogRecords(String prjId, String loginUserName) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info("reset {} / {} / {}", prjId, loginUserName, sdf.format(System.currentTimeMillis()));
	}
	
	public static String changeDataToTableFormat(String target, String msg, List<String> data) {
		String rtnString = "";
		String comment = "";
		if (!isEmpty(msg)) {
			comment = "<p><b>" + msg + "</b></p>";
		}
		String contents = 	"<div class=\"custom-layout card-body p-0\">";
		if (isEmpty(msg) && !target.equals("nick")) {
			if (target.equals("oss")) {
				contents += "<b>Open Source</b>";
			} else {
				contents += "<b>License</b>";
			}
		}
				contents += "	<table class=\"table table-bordered\">" +
							"		<thead>";
		if (target.equals("oss") || target.equals("nick")) {
				contents += "			<tr>" +
							"		    	<th class=\"text-center\">Before</th>" +
							"               <th class=\"text-center\">After</th>" +
							"			</tr>" +
							"		</thead>" +
							"		<tbody>";
			
			for (String key : data) {
				String[] changeInfos = key.split("[|]");
				String beforeValue = changeInfos[0];
				String afterValue = changeInfos[1];
				String value = "		<tr>" +
								"			<td stlye=\"padding-left:0.75rem;\">"+ beforeValue.trim() + "</td>" +
								"			<td>"+ afterValue.trim() + " </td>" +
								"		</tr>";
				contents += value;
			}
		} else {
				contents += "			<tr>" +
							"		    	<th class=\"text-center\">OSS</th>" +
							"               <th class=\"text-center\">Before</th>" +
							"               <th class=\"text-center\">After</th>" +
							"			</tr>" +
							"		</thead>" +
							"		<tbody>";

			for (String key : data) {
				String[] comments = key.split("[|]");
				String ossInfo = comments[0];
				String beforeValue = comments[1];
				String afterValue = comments[2];
				
				String value = "		<tr>" +
								"			<td stlye=\"padding-left:0.75rem;\">"+ ossInfo.trim() + " </td>" +
								"			<td>"+ beforeValue.trim() + "</td>" +
								"			<td>"+ afterValue.trim() + "</td>" +
								"		</tr>";
				contents += value;
			}
		}
		
			contents += 	"		</tbody>" +
							"	</table>" +
							"</div>";
		rtnString += comment + contents;
		return rtnString;
	}
	
	public static String getCommentForChangeNickname(String comment, List<String> beforeNicknames, List<String> afterNicknames) {
		String customComment = comment;
		
		if (CollectionUtils.isEmpty(beforeNicknames) && !CollectionUtils.isEmpty(afterNicknames)) {
			if (!isEmpty(customComment)) {
				customComment += "<br>";
			}
			customComment += "[Nickname Added] " + String.join(",", afterNicknames);
		} else if (!CollectionUtils.isEmpty(beforeNicknames) && CollectionUtils.isEmpty(afterNicknames)) {
			if (!isEmpty(customComment)) {
				customComment += "<br>";
			}
			customComment += "[Nickname Deleted] " + String.join(",", beforeNicknames);
		} else {
			List<String> nonDuplicateListForAfter = afterNicknames.stream().filter(a -> beforeNicknames.stream().noneMatch(Predicate.isEqual(a))).collect(Collectors.toList());
			List<String> nonDuplicateListForBefore = beforeNicknames.stream().filter(b -> afterNicknames.stream().noneMatch(Predicate.isEqual(b))).collect(Collectors.toList());
			
			if (beforeNicknames.size() == afterNicknames.size()) {
				if (!CollectionUtils.isEmpty(nonDuplicateListForAfter) && !CollectionUtils.isEmpty(nonDuplicateListForBefore)) {
					customComment += "[Nickname Changed]<br>";
					List<String> data = new ArrayList<>();
					for (int i=0; i < nonDuplicateListForAfter.size(); i++) {
						data.add(nonDuplicateListForBefore.get(i) + "|" + nonDuplicateListForAfter.get(i));
					}
					customComment += CommonFunction.changeDataToTableFormat("nick", "", data);
				}
			} else {
				if (!CollectionUtils.isEmpty(nonDuplicateListForAfter)) {
					if (!isEmpty(customComment)) {
						customComment += "<br>";
					}
					customComment += "[Nickname Added] " + String.join(",", nonDuplicateListForAfter);
				}
				if (!CollectionUtils.isEmpty(nonDuplicateListForBefore)) {
					if (!isEmpty(customComment)) {
						customComment += "<br>";
					}
					customComment += "[Nickname Deleted] " + String.join(",", nonDuplicateListForBefore);
				}
			}
		}
		
		return customComment;
	}

	public static void redirectLogoutSingleSignOn(HttpServletResponse response) {
		String redirectUrl = UriComponentsBuilder.fromUriString(CommonFunction.emptyCheckProperty("sso.server.url", "") + "/logout")
											.queryParam("scope", "email profile openid")
											.queryParam("client_id", CommonFunction.emptyCheckProperty("sso.server.client.id", ""))
											.queryParam("post_logout_redirect_uri", CommonFunction.emptyCheckProperty("server.domain", "") + "/login")
											.build().toUriString();
		try {
			response.sendRedirect(redirectUrl);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static OssMaster getOssVulnerabilityInfo(String ossName, String ossVersion) {
		OssMaster ossMaster = new OssMaster();
		if (CoCodeManager.OSS_INFO_UPPER.containsKey((ossName + "_" + ossVersion).toUpperCase())) {
			ossMaster = CoCodeManager.OSS_INFO_UPPER.get((ossName + "_" + ossVersion).toUpperCase());
			String[] ossNicknameList = ossService.getOssNickNameListByOssName(ossName);
			if (ossNicknameList != null && ossNicknameList.length > 0) {
				ossMaster.setOssNicknames(ossNicknameList);
			}
			if (!isEmpty(ossMaster.getIncludeCpe()) && CoConstDef.FLAG_YES.equals(avoidNull(ossMaster.getInCpeMatchFlag()))) {
				List<String> includeCpeList = new ArrayList<>();
				for (String includeCpe : ossMaster.getIncludeCpe().split(",")) {
					String[] splitIncludeCpe = includeCpe.split(":");
					if (splitIncludeCpe.length > 2 && splitIncludeCpe.length == 13) {
						includeCpeList.add(splitIncludeCpe[3] + ":" + splitIncludeCpe[4]);
					} else {
						includeCpeList.add(includeCpe);
					}
				}
				if (!includeCpeList.isEmpty()) {
					ossMaster.setIncludeCpes(includeCpeList.toArray(new String[includeCpeList.size()]));
				}
			}
			if (!isEmpty(ossMaster.getExcludeCpe())) {
				List<String> excludeCpeList = new ArrayList<>();
				for (String excludeCpe : ossMaster.getExcludeCpe().split(",")) {
					String[] splitExcludeCpe = excludeCpe.split(":");
					if (splitExcludeCpe.length > 2 && splitExcludeCpe.length == 13) {
						excludeCpeList.add(splitExcludeCpe[3] + ":" + splitExcludeCpe[4]);
					} else {
						excludeCpeList.add(excludeCpe);
					}
				}
				if (!excludeCpeList.isEmpty()) {
					ossMaster.setExcludeCpes(excludeCpeList.toArray(new String[excludeCpeList.size()]));
				}
			}
			if (!isEmpty(ossMaster.getOssVersionAlias())) {
				String[] ossVersionAliases = Arrays.stream(ossMaster.getOssVersionAlias().split(",")).map(String::trim).toArray(String[]::new);
				ossMaster.setOssVersionAliases(ossVersionAliases);
			}
		} else {
			ossMaster.setOssName(ossName);
			ossMaster.setOssVersion(ossVersion);
		}
		ossMaster = ossService.getOssVulnerabilityInfo(ossMaster);
		return ossMaster;
	}
}

