/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import com.google.common.collect.Lists;
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

@Component
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
        
        if(dt.length() >= 8) {
            if(CoConstDef.DISP_FORMAT_DATE_TAG_SIMPLE.equals(separator)) {
                // yyyyMMdd
                dt = dt.substring(0, 8);
            } else if(dt.length() >= 14) {
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
        if(array != null) {
            return Arrays.asList(array).contains(item);
        }
        
        return false;
    }
    
    public static String arrayToString(String[] arr, String sep) {
        String tmpStr = CoConstDef.EMPTY_STRING;
        if(isEmpty(sep)) {
            sep = ","; // default value
        }
        if(arr != null) {
            for(String s : arr) {
                tmpStr += (isEmpty(tmpStr) ? "" : sep) + s.trim();
            }
        }
        return tmpStr;
    }
    
    public static String arrayToStringForSql(String[] arr) {
        String tmpStr = CoConstDef.EMPTY_STRING;
        if(arr != null) {
            for(String s : arr) {
                tmpStr += (isEmpty(tmpStr) ? "" : ",") + "\'" + avoidNull(s) + "\'";
            }
        }
        return tmpStr;
    }
    
    public static String arrayToString(List<String> list) {
    	if(list == null || list.isEmpty()) {
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
		if(uploadFile != null && uploadFile.getSize() > 0){
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
		
		if(s.length() == 12) {
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
	
	public static String genOptionUsers(String authority) {
		List<T2Users> users = t2UserService.getAuthorityUsers(authority);
		
        StringBuffer stringbuffer = new StringBuffer();
        
        for(int k = users.size(), j = 0; j < k; j++) {
            T2Users user = users.get(j);
            stringbuffer.append("    <option value='").append(user.getUserId()).append('\'');
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
	    	if(!authorities.isEmpty()){
	    		for (GrantedAuthority authority : authorities) {
	    			result = (authority.getAuthority()).replaceFirst(AppConstBean.SECURITY_ROLE_PREFIX, "");
	    			break;
				}
	    	}
    	} catch(Exception e){}
    	
    	if (auth != null && "ROLE_ADMIN".equalsIgnoreCase(result) && auth.isAuthenticated()) { 
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
	    	if(!authorities.isEmpty()){
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
		
		if(list == null || list.isEmpty()) {
			return "";
		}
		
		List<OssLicense> licenseList = new ArrayList<>();
		
		// 필요한 정보만 재설정한다.
		for(ProjectIdentification bean : list) {
			OssLicense license = new OssLicense();
			license.setOssLicenseComb(bean.getOssLicenseComb());
			license.setLicenseName(bean.getLicenseName());
			
			if(!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())){
				license.setLicenseName(license.getLicenseName());
				licenseList.add(license);
			}
		}
		
		if(isEmpty(delimiter)) {
			return makeLicenseExpression(licenseList);
		}else {
			return makeLicenseString(licenseList, delimiter);
		}
	}
	public static String makeLicenseString(List<OssLicense> list, String delimiter) {
		String result = "";
		for(OssLicense bean : list) {
			if(!isEmpty(result)) {
				result += delimiter;
			}
			
			String licenseName = bean.getLicenseName();
			if(CoCodeManager.LICENSE_INFO.containsKey(licenseName)) {
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
		for(OssLicense bean : list) { 
			if(!isFirstLicense && "OR".equals(bean.getOssLicenseComb())) {
				if(!andLicenseList.isEmpty()) {
					licenseNameList.add(andLicenseList);
					andLicenseList = new ArrayList<>();
					andLicenseList.add(bean.getLicenseName());
				}
			} else {
				andLicenseList.add(bean.getLicenseName());
			}
			isFirstLicense = false;
		}
		
		if(!andLicenseList.isEmpty()) {
			licenseNameList.add(andLicenseList);
		}
		
		// 연삭식 적용 및 Short Identifier 로 변경
		if(!licenseNameList.isEmpty()) {
			for(List<String> combList : licenseNameList) {
				if(!isEmpty(rtnVal)) {
					rtnVal += " OR ";
				}
				String andStr = "";
				for(String s : combList) {
					if(!isEmpty(andStr)) {
						if(msgType) { 
							andStr += ", ";
						} else {
							andStr += " AND ";
						}
					}
					
					LicenseMaster licenseMaster = null;
					
					if(CoCodeManager.LICENSE_INFO.containsKey(s)) {
						s = avoidNull(CoCodeManager.LICENSE_INFO.get(s).getShortIdentifier(), s);
						licenseMaster = CoCodeManager.LICENSE_INFO.get(s);
					}
					
					if(htmlLinkType && licenseMaster != null) {
						andStr += "<a href='javascript:void(0);' class='urlLink'  onclick='showLicenseText(" + licenseMaster.getLicenseId() + ");' >" + s + "</a>" ;
					} else {
						if(spdxConvert) {
							// identifier가 없는 경우 라이선스 이름을 spdx 연동 용으로 변경한다.
							s = licenseStrToSPDXLicenseFormat(s);
						}
						andStr += s;
					}
				}
				
				rtnVal += licenseNameList.size() > 1 ? ( combList.size() != 1 && !msgType ? ("(" + andStr + ")") :  andStr ) : andStr;
			}
		}
		
		return rtnVal;
	}

	public static String licenseStrToSPDXLicenseFormat(String licenseStr) {
		if(CoCodeManager.LICENSE_INFO.containsKey(licenseStr) && isEmpty(CoCodeManager.LICENSE_INFO.get(licenseStr).getShortIdentifier())) {
			licenseStr = "LicenseRef-" + licenseStr;
		}

		licenseStr = licenseStr.replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-");
		return licenseStr;
	}

	public static String makeLicenseFromFiles(OssMaster _ossBean, boolean booleanflag) {
		List<String> resultList = new ArrayList<>(); // declared License
		List<String> detectedLicenseList = _ossBean.getDetectedLicenses(); // detected License
		
		if (_ossBean != null) {
			for (OssLicense license : _ossBean.getOssLicenses()) {
				String licenseName = license.getLicenseName();
				if (booleanflag) {
					licenseName = avoidNull(CoCodeManager.LICENSE_INFO.get(licenseName).getShortIdentifier(), "LicenseRef-" + licenseName);
					licenseName = licenseName.replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-");
				}
				
				if(!resultList.contains(licenseName)) {
					resultList.add(licenseName);
				}
			}

			if(detectedLicenseList != null) {
				for(String licenseName : detectedLicenseList) {
					if (booleanflag) {
						licenseName = avoidNull(CoCodeManager.LICENSE_INFO.get(licenseName).getShortIdentifier(), "LicenseRef-" + licenseName);
						licenseName = licenseName.replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-");
					}
					
					if(!resultList.contains(licenseName)) {
						resultList.add(licenseName);
					}
				}
			}
		}
		
		return String.join(",", resultList);
	}
	
	public static List<ProjectIdentification> makeLicenseExcludeYn(List<ProjectIdentification> list){
		// OR 조건으로 각 list로 구분한다.
		List<List<ProjectIdentification>> andCombLicenseList = new ArrayList<>();
		
		for(ProjectIdentification bean : list) {
			if(andCombLicenseList.isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
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
		
		for(List<ProjectIdentification> andList : andCombLicenseList) {
			// 그룹별 퍼미션 취득
			switch (getLicensePermissive(andList)) {
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					finalPermissive = CoConstDef.CD_LICENSE_TYPE_PMS;
					selectedIdx = idx;
					permissiveListPSM.add(selectedIdx);
				
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if(!CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive)) {
						finalPermissive = CoConstDef.CD_LICENSE_TYPE_WCP;
						selectedIdx = idx;
						permissiveListWCP.add(selectedIdx);
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_CP:
					if(!CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive)
							&& !CoConstDef.CD_LICENSE_TYPE_WCP.equals(finalPermissive)) {
						finalPermissive = CoConstDef.CD_LICENSE_TYPE_CP;
						selectedIdx = idx;
						permissiveListCP.add(selectedIdx);
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PF:
					if(!CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive)
							&& !CoConstDef.CD_LICENSE_TYPE_WCP.equals(finalPermissive)
							&& !CoConstDef.CD_LICENSE_TYPE_CP.equals(finalPermissive)) {
						finalPermissive = CoConstDef.CD_LICENSE_TYPE_PF;
						selectedIdx = idx;
						permissiveListPF.add(selectedIdx);
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_NA:
					if(isEmpty(finalPermissive)) {
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
		
		for(List<ProjectIdentification> andList : andCombLicenseList) {
			boolean _break = false;
			for(ProjectIdentification bean : andList) {
				if(hasSelected) {
					bean.setExcludeYn(CoConstDef.FLAG_YES);
				} else {
					List<Integer> containsList = CoConstDef.CD_LICENSE_TYPE_PMS.equals(finalPermissive) ? 
								permissiveListPSM : CoConstDef.CD_LICENSE_TYPE_WCP.equals(finalPermissive) ? 
									permissiveListWCP : CoConstDef.CD_LICENSE_TYPE_CP.equals(finalPermissive) ? 
											permissiveListCP : CoConstDef.CD_LICENSE_TYPE_PF.equals(finalPermissive) ? 
													permissiveListPF : CoConstDef.CD_LICENSE_TYPE_NA.equals(finalPermissive) ? 
															permissiveListNA : new ArrayList<>();

					if(containsList.contains(idx)) {
						bean.setExcludeYn(CoConstDef.FLAG_NO);
						_break = true;
					} else {
						bean.setExcludeYn(CoConstDef.FLAG_YES);
					}
					//bean.setExcludeYn(containsList.contains(idx) ? CoConstDef.FLAG_NO : CoConstDef.FLAG_YES);
				}
				resultList.add(bean);
			}
			
			if(_break) {
				hasSelected = true;
			}
			
			idx++;
		}
		
		return resultList;
	}
	
	private static String getLicensePermissive(List<ProjectIdentification> andList) {
		String rtnVal = "";
		
		for(ProjectIdentification bean : andList) {
			// 가장 Strong한 라이선스부터 case
			switch (bean.getLicenseType()) {
				case CoConstDef.CD_LICENSE_TYPE_NA:
					rtnVal = CoConstDef.CD_LICENSE_TYPE_NA;
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PF:
					if(!CoConstDef.CD_LICENSE_TYPE_NA.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_PF;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_CP:
					if(!CoConstDef.CD_LICENSE_TYPE_NA.equals(rtnVal)
							&& !CoConstDef.CD_LICENSE_TYPE_PF.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_CP;
					}
				
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if(!CoConstDef.CD_LICENSE_TYPE_NA.equals(rtnVal)
							&& !CoConstDef.CD_LICENSE_TYPE_PF.equals(rtnVal)
							&& !CoConstDef.CD_LICENSE_TYPE_CP.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_WCP;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					if(isEmpty(rtnVal)) {
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
		
		for(OssLicense bean : andList) {
			// 가장 Strong한 라이선스부터 case
			switch (bean.getLicenseType()) {
				case CoConstDef.CD_LICENSE_TYPE_CP:
					rtnVal = CoConstDef.CD_LICENSE_TYPE_CP;
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if(!CoConstDef.CD_LICENSE_TYPE_CP.equals(rtnVal)) {
						rtnVal = CoConstDef.CD_LICENSE_TYPE_WCP;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					if(isEmpty(rtnVal)) {
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
		
		if(andList != null) {
			for(OssLicense bean : andList) {
				// 가장 Strong한 라이선스부터 case
				switch (bean.getLicenseType()) {
					case CoConstDef.CD_LICENSE_TYPE_NA:
						currentType = CoConstDef.CD_LICENSE_TYPE_NA;
						rtnVal = bean;
						breakFlag = true;
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_PF:
						if(!CoConstDef.CD_LICENSE_TYPE_NA.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_PF;
							rtnVal = bean;
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_CP:
						if(!CoConstDef.CD_LICENSE_TYPE_PF.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_CP;
							rtnVal = bean;
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_WCP:
						if(!CoConstDef.CD_LICENSE_TYPE_CP.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_WCP;
							rtnVal = bean;
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_PMS:
						if(isEmpty(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_PMS;
							rtnVal = bean;
						}
						
						break;
					default:
						break;
				}
				
				if(breakFlag) {
					break;
				}
			}
		}
		
		return rtnVal;
	}

	
	public static String getStringFromFile(String path) {
		StringBuffer sb = new StringBuffer();
		File file = new File(path);
		
		if(file.exists() && file.isFile()) {
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
	            while( (temp = br.readLine()) != null) {
	            	sb.append(temp).append(System.lineSeparator());
	            }
	        } catch (FileNotFoundException e) {
	        	log.error(e.getMessage(), e);
	             
	        } catch (Exception e) {
	             log.error(e.getMessage(), e);
	        } finally {
	        	if(fis != null) {
	        		try {fis.close();} catch (IOException e) {}
	        	}
	        	if(isr != null) {
	        		try {isr.close();} catch (IOException e) {}
	        	}
	        	if(br != null) {
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
		if(file.exists() && file.isFile()) {
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
	            while( (temp = br.readLine()) != null) {
	            	if(duplicationCheckList.contains(temp.trim())) {
	            		continue;
	            	}
	            	if(checkExceptionWords(temp, ignorePathStr, checkList, ignoreList)) {
	            		sb.append(temp).append(System.lineSeparator());
	            	}
	            	duplicationCheckList.add(temp.trim());
	            }
	        } catch (FileNotFoundException e) {
	        	log.error(e.getMessage(), e);
	             
	        } catch (Exception e) {
	             log.error(e.getMessage(), e);
	        } finally {
	        	if(fis != null) {
	        		try {fis.close();} catch (IOException e) {}
	        	}
	        	if(isr != null) {
	        		try {isr.close();} catch (IOException e) {}
	        	}
	        	if(br != null) {
	        		try {br.close();} catch (IOException e) {}
	        	}
	        }
			
		} else {
			log.error("파일이 존재하지 않습니다. path : " + path);
		}
		
		file = null;
		
		return sb.toString();
	}
	
	private static boolean checkExceptionWords(String line, String ignorePathStr, List<String> checkList, List<String> ignoreList) {
		line = line.replaceAll(ignorePathStr, "");
		line = line.toUpperCase();
		
		for(String s : ignoreList) {
			if(line.replaceAll(" ", "").replaceAll("\t", "").contains(s.toUpperCase().replaceAll(" ", "").replaceAll("\t", ""))) {
				return false;
			}
		}
		
		for(String s : checkList) {
			if(line.contains(s.toUpperCase())) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String getFileEncode(String filePath) {
		String encoding = null;
		java.io.FileInputStream fis = null;
		
		try {
			byte[] buf = new byte[4096];
		    String fileName = filePath;
		    fis = new java.io.FileInputStream(fileName);

		    UniversalDetector detector = new UniversalDetector(null);

		    int nread;
		    
		    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
		      detector.handleData(buf, 0, nread);
		    }
		    
		    detector.dataEnd();

		    encoding = detector.getDetectedCharset();
		    detector.reset();			
		} catch (Exception e) {
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (Exception e2) {
				}
			}
		}
		
	    return encoding;
	}
	
	public static String sortLicenseName(List<OssLicense> ossLicenses) {
		List<String> l = new ArrayList<>();
		
		if(ossLicenses != null && !ossLicenses.isEmpty()){
			for(OssLicense bean : ossLicenses) {
				l.add(avoidNull(bean.getLicenseId()));
			}			
		}
		
		Collections.sort(l);
		
		String rtn = "";
		for(String s : l) {
			rtn += (!isEmpty(s) ? "|" : "") + s;
		}
		
		return rtn;
	}
	
	public static String getShortIdentify(String licenseName) {
		if(CoCodeManager.LICENSE_INFO.containsKey(licenseName)) {
			LicenseMaster bean = CoCodeManager.LICENSE_INFO.get(licenseName);
			
			return !isEmpty(bean.getShortIdentifier()) ? bean.getShortIdentifier() : licenseName;
		}
		
		return licenseName;
	}
	
	public static String getLicenseIdByName(String licenseName) {
		if(!isEmpty(licenseName)) {
			licenseName = licenseName.trim().toUpperCase();
			
			if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName)) {
				return CoCodeManager.LICENSE_INFO_UPPER.get(licenseName).getLicenseId();
			}
		}

		return null;
	}
	
	public static String getLicenseNameById(String licenseId, String licenseName) {
		if(!isEmpty(licenseId)) {
			if(CoCodeManager.LICENSE_INFO_BY_ID.containsKey(licenseId)) {
				LicenseMaster licenseInfo = CoCodeManager.LICENSE_INFO_BY_ID.get(licenseId);
				if(licenseInfo != null) {
					// short identify를 최우선
					if(!isEmpty(licenseInfo.getShortIdentifier())) {
						return licenseInfo.getShortIdentifier();
					}
					
					if(!isEmpty(licenseInfo.getLicenseNameTemp())) {
						// 정식 명칭으로 변환
						return licenseInfo.getLicenseNameTemp();
					}
				}
			}
		}

		return licenseName;
	}
	
	public static List<OssLicense> changeLicenseNameToShort(List<OssLicense> list) {
		if(list != null) {
			for(OssLicense bean : list) {
				if(CoCodeManager.LICENSE_INFO.containsKey(bean.getLicenseName()) && !isEmpty(CoCodeManager.LICENSE_INFO.get(bean.getLicenseName()).getShortIdentifier())) {
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
		
		for(OssComponents bean : reportData) {
			gridBean = new ProjectIdentification();
			gridBean.setGridId(CoConstDef.GRID_NEWROW_DEFAULT_PREFIX+fileSeq +"f"+keyCnt++);
			gridBean.setComponentId(bean.getComponentId());
			gridBean.setReferenceId(bean.getReferenceId());
			gridBean.setReferenceDiv(bean.getReferenceDiv());
			gridBean.setOssId(bean.getOssId());
			gridBean.setOssName(bean.getOssName());
			// oss version이 정수형인 경우 분석결과서 서식에 따라, ".0" 이 사라지는 경우를 위해 
			// 기 등록된 oss에 존재하는 경우 자동으로 .0을 채워줌
			if(!isEmpty(bean.getOssName()) && !isEmpty(bean.getOssVersion()) && StringUtil.isNumeric(bean.getOssVersion())) {
				String _test = bean.getOssName().trim() + "_" + bean.getOssVersion().trim();
				String _test2 = bean.getOssName().trim() + "_" + bean.getOssVersion().trim() + ".0";
				if(!CoCodeManager.OSS_INFO_UPPER.containsKey(_test.toUpperCase()) 
						&& CoCodeManager.OSS_INFO_UPPER.containsKey(_test2.toUpperCase())) {
					if(!versionChangeCheckList.contains(bean.getOssName() + "_" + bean.getOssVersion())) {
						versionChangeCheckList.add(bean.getOssName() + "_" + bean.getOssVersion());
						versionChangeList.add(bean.getOssName() + " : " + bean.getOssVersion() + " => " + bean.getOssVersion().trim() + ".0");
					}
					
					bean.setOssVersion(bean.getOssVersion().trim() + ".0");
				}
			}
			
			gridBean.setOssVersion(bean.getOssVersion());
			gridBean.setDownloadLocation(bean.getDownloadLocation());
			gridBean.setHomepage(bean.getHomepage());
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
			
			if(!isEmpty(bean.getOssNickName())) {
				gridBean.setOssNickName(bean.getOssNickName());
			}
			
			// license 
			int licenseIdx = 1;
			if(bean.getOssComponentsLicense() != null) {
				String licenseText = "";
				
				for(OssComponentsLicense license : bean.getOssComponentsLicense()) {
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
					

					if(!isEmpty(license.getLicenseText())) {
						licenseText += (!isEmpty(licenseText) ? "\r\n" : "") + license.getLicenseText();
					}				
				}
				
				gridBean.setLicenseText(licenseText);
				
				if(CoConstDef.LICENSE_DIV_MULTI.equals(gridBean.getLicenseDiv())) {
					gridBean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(gridBean.getComponentLicenseList(), ","));
				} else {
					gridBean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(gridBean.getComponentLicenseList()));
				}
				
			}
			
			resultList.add(gridBean);
		}
	
		if(!versionChangeList.isEmpty()) {
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
		for(ProjectIdentification bean : mainGridList) {
			if(CoConstDef.LICENSE_DIV_SINGLE.equals(bean.getLicenseDiv())) {
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
		
		if(ossComponentsLicense != null) {
			for(List<ProjectIdentification> licenseList : ossComponentsLicense) {
				String gridId = licenseList.get(0).getGridId();
				String key = licenseList.get(0).getGridId().split(gridId.indexOf("-") > -1 ? "-" : "_")[0];
				subMap.put(key, licenseList);
			}
		}
		
		if(addOssComponents != null) {
			String key = "";
			for(ProjectIdentification bean : addOssComponents) {
				String key2 = bean.getBinaryName()+ "-" + bean.getOssName() + "-" + bean.getOssVersion();
				if(key.equals(key2)) {
					ProjectIdentification result = mainGridList.get(mainGridList.size()-1);
					result.setLicenseName(result.getLicenseName() + "," + bean.getLicenseName());
					
					mainGridList.set(mainGridList.size()-1, result);
				}else {
					bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
					
					mainGridList.add(bean);
					key = key2;
				}
				
				if(bean.getComponentLicenseList() != null) {
					subMap.put(bean.getGridId(), bean.getComponentLicenseList());
				}
			}

		}
		
		if(reportData != null) {
			Map<String, Object> convertObj = convertToProjectIdentificationList(reportData, fileSeq);
			List<ProjectIdentification> _list = (List<ProjectIdentification>) convertObj.get("resultList");
			
			if(_list != null) {
				Map<String, ProjectIdentification> sortMap = new TreeMap<String, ProjectIdentification>();
				
				for(ProjectIdentification gridBean : _list) { // 중복제거 및 정렬
					String key = "";
					if("BIN".equals(readType.toUpperCase()) || "BINANDROID".equals(readType.toUpperCase()) || "PARTNER".equals(readType.toUpperCase())) {
						if(!isEmpty(gridBean.getFilePath()) && isEmpty(gridBean.getBinaryName())) {
							gridBean.setBinaryName(gridBean.getFilePath());
						}
						
						key = gridBean.getBinaryName() + "-" + gridBean.getOssName() + "-" + gridBean.getOssVersion() + "-" + gridBean.getLicenseName() + "-" + gridBean.getExcludeYn();
					}else {
						if(isEmpty(gridBean.getFilePath()) && !isEmpty(gridBean.getBinaryName())) {
							gridBean.setFilePath(gridBean.getBinaryName());
						}
						
						key = gridBean.getFilePath() + "-" + gridBean.getOssName() + "-" + gridBean.getOssVersion() + "-" + gridBean.getLicenseName() + "-" + gridBean.getExcludeYn();
					}
					
					if(!sortMap.keySet().contains(key)) {
						List<ProjectIdentification> resultLicenseList = new ArrayList<ProjectIdentification>();
						List<String> duplicateLicense = new ArrayList<String>();
						
						if(gridBean.getComponentLicenseList() != null) {
							for(ProjectIdentification licenseList : gridBean.getComponentLicenseList()) {
								if(licenseList.getLicenseName().contains(",")){
									ProjectIdentification copyLicenseList = null;
									String[] licenses = licenseList.getLicenseName().split(",");
									for(String li : licenses) {
										if(!duplicateLicense.contains(StringUtil.trim(li).toUpperCase())) {
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
									if(!duplicateLicense.contains(licenseList.getLicenseName()) && CoConstDef.FLAG_NO.equals(licenseList.getExcludeYn())) {
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
				for(ProjectIdentification gridBean : sortMap.values()) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO.get(gridBean.getLicenseName());
					String ossName = isEmpty(gridBean.getOssName()) ? "-" : gridBean.getOssName();
					String key2 = "";
					String key4 = ossName + "-" + (licenseMaster != null ? licenseMaster.getLicenseType() : "");
					String gridId = "";
					
					if("BIN".equals(readType.toUpperCase()) || "BINANDROID".equals(readType.toUpperCase()) || "PARTNER".equals(readType.toUpperCase())) {
						key2 = gridBean.getBinaryName() + "-" + ossName + "-" + gridBean.getOssVersion() + "-" + gridBean.getExcludeYn();
					}else {
						key2 = gridBean.getFilePath() + "-" + ossName + "-" + gridBean.getOssVersion() + "-" + gridBean.getExcludeYn();
					}
					
					if(!"-".equals(ossName) 
							&& key.equals(key2)
							&& !"--NA".equals(key3)
							&& !"--NA".equals(key4)) { // 단, OSS Name: - 이면서, License Type: Proprietary이 아닌 경우 Row를 합치지 않음.
						ProjectIdentification result = mainGridList.get(mainGridList.size()-1);
						
						if(!result.getLicenseName().contains(StringUtil.trim(gridBean.getLicenseName()))) {
							String resultLicenseName = StringUtil.trim(result.getLicenseName());
							String licenseNameList = StringUtil.trim(gridBean.getLicenseName());
							if(licenseNameList.contains(",")) {
								for(String licensename : licenseNameList.split(",")) {
									if(!resultLicenseName.contains(licensename) && !isEmpty(licensename)) {
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
					
					if(gridBean.getComponentLicenseList() != null) {
						if(isEmpty(gridId)) {
							subMap.put(gridBean.getGridId(), gridBean.getComponentLicenseList());
						}else {
							List<ProjectIdentification> result = (List<ProjectIdentification>) subMap.get(gridId);
							for(ProjectIdentification subData : gridBean.getComponentLicenseList()) {
								for(ProjectIdentification resultData : result) {
									if(!resultData.getLicenseName().equals(resultData.getLicenseName())) {
										result.add(subData);
										break;
									}
								}
							}
							if(result.size() > 0) {
								subMap.replace(gridId, result);
							}else {
								subMap.replace(gridId, gridBean.getComponentLicenseList());
							}
						}
					}
				}
			}
			
			// OSC-486
			// version 정보가 자동으로 변경된 Data 체크
			if(convertObj.containsKey("versionChangeList")) {
				resultMap.put("versionChangedList",convertObj.get("versionChangeList"));
			}
						
		}
		resultMap.put("subData", subMap);
		resultMap.put("mainData", mainGridList);

		return resultMap;
	}
	
	public static String makeLicenseObligationStr(String obligationChecks) {
		String rtnStr = "";
		
		if(avoidNull(obligationChecks).length() == 3) {
			// 우선순위가 높은 순으로
			if(CoConstDef.FLAG_YES.equals(obligationChecks.substring(2))) {
				rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK);
			} else if(CoConstDef.FLAG_YES.equals(obligationChecks.substring(1, 2))) {
				rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
			} else if(CoConstDef.FLAG_YES.equals(obligationChecks.substring(0, 1))) {
				rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_NOTICE);
			}
		}
		
		return rtnStr;
	}

	public static String makeLicenseObligationStr(String notice, String disclose, String needsCheck) {
		return makeLicenseObligationStr(avoidNull(notice, CoConstDef.FLAG_NO) + avoidNull(disclose, CoConstDef.FLAG_NO) + avoidNull(needsCheck, CoConstDef.FLAG_NO));
	}
	
	public static String makeCategoryFormat(String distributeTarget, String mainCategoryCd, String subCategoryCode) {
		String categoryCode = CoConstDef.CD_DTL_DISTRIBUTE_SKS.equals(avoidNull(distributeTarget, CoConstDef.CD_DTL_DISTRIBUTE_LGE)) ? CoConstDef.CD_MODEL_TYPE2 : CoConstDef.CD_MODEL_TYPE;
		
		return  CoCodeManager.getCodeString(categoryCode, mainCategoryCd) + " > " + CoCodeManager.getCodeString(CoCodeManager.getSubCodeNo(categoryCode, mainCategoryCd), subCategoryCode);
	}
	
	public static String makeCategoryFormat(String distributeTarget, String categoryCd) {
		if(!isEmpty(categoryCd) && categoryCd.length() == 6) {
			return makeCategoryFormat(distributeTarget, categoryCd.substring(0,3), categoryCd.substring(3));
		}
		
		return "";
	}

	public static boolean isSameLicense(List<OssComponentsLicense> list1,
			List<OssComponentsLicense> list2) {
		if(list1 == null && list2 == null) {
			return false;
		}
		
		if((list1 == null && list2 != null) || (list1 != null && list2 == null)) {
			return false;
		}
		
		if(list1.size() != list2.size()) {
			return false;
		}
		
		List<String> licenseNames = new ArrayList<>();
		
		for(OssComponentsLicense bean : list1) {
			LicenseMaster liMaster = CoCodeManager.LICENSE_INFO.get(bean.getLicenseName());
			if(liMaster == null) {
				licenseNames.add(avoidNull( bean.getLicenseName()));
			} else {
				licenseNames.add(liMaster.getLicenseName());
				if(!isEmpty(liMaster.getShortIdentifier())) {
					licenseNames.add(liMaster.getShortIdentifier());
				}
				if(liMaster.getLicenseNicknameList() != null && !liMaster.getLicenseNicknameList().isEmpty()) {
					licenseNames.addAll(liMaster.getLicenseNicknameList());
				}
			}
		}
		
		for(OssComponentsLicense bean : list2) {
			if(!licenseNames.contains(bean.getLicenseName())) {
				return false;
			}
		}
		
		return true;
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
		Map<String, List<String>> infoBiMap = new HashMap<>(); // info level (new bianry)
		Map<String, List<String>> infoModifyMap = new HashMap<>(); // info level (modified = new + tlsh > 120)
		Map<String, List<String>> infoOnlyMap = new HashMap<>(); // info level
		Map<String, String> hideObligationIdList = new HashMap<>();
		
		if(RestrictionFlag) {
			for(ProjectIdentification p : list) {
				if(!StringUtil.isEmpty(p.getRestriction()) && CoConstDef.FLAG_NO.equals(p.getExcludeYn())) {
					restrictionMap.put(avoidNull(p.getGridId(), p.getComponentId()), new ArrayList<String>());
				}
			}
		}
		
		if(validMap != null && !validMap.isEmpty()) {
			for(String errKey : validMap.keySet()) {
				if(errKey.indexOf(".") > -1) {
					String _key = errKey.substring(errKey.indexOf(".") + 1, errKey.length());
					if(errorMap.containsKey(_key)) {
						List<String> _list = errorMap.get(_key);
						_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
						errorMap.replace(_key, _list);
					} else {
						List<String> _list = new ArrayList<>();
						_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
						errorMap.put(_key, _list);
					}
					/*
					if(hideObligation) {
						if(hideObligationColumns.contains(errKey.substring(0, errKey.indexOf(".")).toUpperCase())) {
//							hideObligationIdList.put(_key, validMap.get(errKey));
							hideObligationIdList.put(_key, getMessage("msg.project.obligation.unclear"));
						}
					}
					*/
				}
			}			
		}

		
		// warning message 가 포함되어 있는 경우 정렬 (우선순위 3)
		if(validDiffMap != null && !validDiffMap.isEmpty()) {
			for(String errKey : validDiffMap.keySet()) {
				if(errKey.indexOf(".") > -1) {
					String _key = errKey.substring(errKey.indexOf(".") + 1, errKey.length());
					/*
					if(hideObligation) {
						if(hideObligationColumns.contains(errKey.substring(0, errKey.indexOf(".")).toUpperCase())) {
//							hideObligationIdList.put(_key, validDiffMap.get(errKey));
							hideObligationIdList.put(_key, getMessage("msg.project.obligation.unclear"));
						}
					}
					*/
					
					if(warningMap.containsKey(_key)) {
						List<String> _list = warningMap.get(_key);
						_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
						warningMap.replace(_key, _list);
					} else {
						List<String> _list = new ArrayList<>();
						_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
						warningMap.put(_key, _list);
					}
				}
			}
		}
		
		// info message가 포함되어 있는 경우 정렬 (우선순위 6)
		if(validInfoMap != null && !validInfoMap.isEmpty()) {
			for(String errKey : validInfoMap.keySet()) {
				if(errKey.indexOf(".") > -1) {
					String _key = errKey.substring(errKey.indexOf(".") + 1, errKey.length());
					
					if(hideObligation) {
						if(hideObligationColumns.contains(errKey.substring(0, errKey.indexOf(".")).toUpperCase())) {
//							hideObligationIdList.put(_key, validInfoMap.get(errKey));
							hideObligationIdList.put(_key, getMessage("msg.project.obligation.unclear"));
						}
					}
					
					// info level message 중에서 new binary message는 info level 상단으로 정렬하기 위해 우선순위 5로 구분하여 격납함
					if(errKey.startsWith("binaryName.") && "NEW".equalsIgnoreCase(validInfoMap.get(errKey))) {
						if(infoBiMap.containsKey(_key)) {
							List<String> _list = infoBiMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoBiMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoBiMap.put(_key, _list);
						}
						
					} else if(errKey.startsWith("binaryName.") && validInfoMap.get(errKey).toUpperCase().startsWith("MODIFIED")) {
						if(infoModifyMap.containsKey(_key)) {
							List<String> _list = infoModifyMap.get(_key);
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoModifyMap.replace(_key, _list);
						} else {
							List<String> _list = new ArrayList<>();
							_list.add(errKey.substring(0, errKey.indexOf(".")).toUpperCase());
							infoModifyMap.put(_key, _list);
						}
					} else {
						
						if(infoOnlyMap.containsKey(_key)) {
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
		for(ProjectIdentification bean : list) {
			
			// self check case only
			if(hideObligation) {
				if(hideObligationIdList.containsKey(bean.getGridId())) {
					bean.setObligationGrayFlag(CoConstDef.FLAG_YES);
					bean.setObligationMsg(hideObligationIdList.get(bean.getGridId()));
				} 

				// Need Check license인 경우
				else if(hasNeedCheckLicense(bean, true)) {
					bean.setObligationGrayFlag(CoConstDef.FLAG_YES);
					bean.setObligationMsg(getMessage("msg.info.selfcheck.include.needcheck.licnese"));
				}
				// 선택된 license로 다시 계산해야함 obligation type
				else if(CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())) {
					bean.setObligationType(CommonFunction.getObligationTypeWithSelectedLicense(bean));
				}
			}
			
			if(	!(!isEmpty(bean.getGroupingColumn()) && currentGroup.equals(bean.getGroupingColumn()))) {
				// 0(1) : error level
				if(errorMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(errorMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "0"), bean);
				} else if(checkMultiLicenseError(errorMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(errorMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(errorMap.get(_id), _id, "0"), bean);
				} else if(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(bean.getObligationLicense())) {
					sortMap.put(makeValidSortKey(new ArrayList<String>(), avoidNull(bean.getGridId(), bean.getComponentId()) , "0", bean.getObligationLicense()), bean);
				}
				// 2 : Restriction
				else if(restrictionMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(restrictionMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "2"), bean);
				}
				// 3 : warning level
				else if(warningMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(warningMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "3"), bean);
				} else if(checkMultiLicenseError(warningMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(warningMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(warningMap.get(_id), _id, "3"), bean);
				}
				// 5 : info level (new binary)
				else if(infoBiMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(infoBiMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "5"), bean);
				} else if(checkMultiLicenseError(infoBiMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(infoBiMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(infoBiMap.get(_id), _id, "5"), bean);
				}
				// 6 : info level (Modified = new + tlsh > 120)
				else if(infoModifyMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					sortMap.put(makeValidSortKey(infoModifyMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "6"), bean);
				} else if(checkMultiLicenseError(infoModifyMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(infoBiMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(infoModifyMap.get(_id), _id, "6"), bean);
				}
				// 7 : info level
				else if(infoOnlyMap.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
					//sortList.add(bean);
					sortMap.put(makeValidSortKey(infoOnlyMap.get(avoidNull(bean.getGridId(), bean.getComponentId())), avoidNull(bean.getGridId(), bean.getComponentId()) , "7"), bean);
				} else if(checkMultiLicenseError(infoOnlyMap, bean.getComponentLicenseList()) != null) {
					String _id = checkMultiLicenseError(infoOnlyMap, bean.getComponentLicenseList());
					sortMap.put(makeValidSortKey(infoOnlyMap.get(_id), _id, "7"), bean);
				}
				else {
					sortListOk.add(bean);
				}
			} else {
				sortListOk.add(bean);
			}
		}
		
		
		// validation 위치별 재정렬
		// treemap을 이용하여 오름차순으로 정렬한다.
		TreeMap<String,ProjectIdentification> tm = new TreeMap<String,ProjectIdentification>(sortMap);
		for(String key : tm.keySet()) {
			sortList.add(tm.get(key));
		}

		// subGrid에서 오류가 있는 row
		if(!sortList2.isEmpty()) {
			sortList.addAll(sortList2);
		}
		
		// 오류가 없는 row
		if(!sortListOk.isEmpty()) {
			sortList.addAll(sortListOk);
		}
		
		return sortList;
	}
	
	private static String getObligationTypeWithSelectedLicense(ProjectIdentification identificationBean) {
		if(identificationBean != null && identificationBean.getComponentLicenseList() != null && !identificationBean.getComponentLicenseList().isEmpty()) {

			// oss master의 obligation과는 달리, exclude 이외의 license는 모두 선택중인 license로 판단한다. (AND)
			List<OssLicense> checkForObligationLicenseList = new ArrayList<>();
			
			for(ProjectIdentification bean : identificationBean.getComponentLicenseList()) {
				if(CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(bean.getLicenseName()).toUpperCase());
				
				if(master != null) {
					OssLicense license = new OssLicense();
					license.setLicenseName(master.getLicenseNameTemp());
					license.setLicenseId(master.getLicenseId());
					license.setLicenseType(master.getLicenseType());
					
					checkForObligationLicenseList.add(license);
				}
			}
			
			if(!checkForObligationLicenseList.isEmpty()) {
				OssLicense obligationLicense = CommonFunction.getLicensePermissiveTypeLicense(checkForObligationLicenseList);
				
				if(obligationLicense != null) {
					// 선택한 license 중에서 가장 Strong한 라이선스의 obligation 설정에 따라 표시
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(obligationLicense.getLicenseName()).toUpperCase());
					
					if(master != null) {
						if(CoConstDef.FLAG_YES.equals(master.getObligationNeedsCheckYn())) {
							return CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK;
						} else if(CoConstDef.FLAG_YES.equals(master.getObligationDisclosingSrcYn())) {
							return CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
						} else if(CoConstDef.FLAG_YES.equals(master.getObligationNotificationYn())) {
							return CoConstDef.CD_DTL_OBLIGATION_NOTICE;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private static boolean hasNeedCheckLicense(ProjectIdentification bean, boolean withSelectedLicense) {
		if(bean != null && bean.getComponentLicenseList() != null && !bean.getComponentLicenseList().isEmpty()) {
			for(ProjectIdentification license : bean.getComponentLicenseList()) {
				if(withSelectedLicense && CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					continue;
				}
				
				if(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(license.getObligationType())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static String makeValidSortKey(List<String> list, String _id, String prevSortKey, String obligationType) {
		String rtn = "";
		// compareArray 순서대로 정렬된다.
		// 정의되지 않은 경우는 정렬대상에서 제외
		String[] compareArray = new String[]{"NOTICE", "BINARYNOTICE", "BINARYNAME", "OSSNAME", "OSSVERSION", "LICENSENAME", "LICENSETEXT", "FILEPATH", "DOWNLOADLOCATION", "HOMEPAGE"};
		Map<String, String> compareSortMap = new LinkedHashMap<>(); // [Identification] Auto ID message있는 Row 정렬 필요.
		int sortIdx = 0;
		
		for(String key : compareArray) {
			compareSortMap.put(key, StringUtil.leftPad(Integer.toString(sortIdx++), 2, "0") );
		}
		
		//String preSortKey = useNextSort ? "2" : "0";
		String preSortKey = avoidNull(prevSortKey, "0");
		boolean hasErrorCode = false;
		
		for(String key : compareSortMap.keySet()) {
			rtn += preSortKey + (list.contains(key) ? compareSortMap.get(key) : "99");
			
			if(list.contains(key)) {
				hasErrorCode = true;
			}
		}
		
		// error code가 없고, obligation type이 need check 인 경우, error 와 warning 사이에 표시한다.
		if(!isEmpty(obligationType) && CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(obligationType) && !hasErrorCode) {
			preSortKey = "1";
			rtn = "";
			
			for(String key : compareSortMap.keySet()) {
				rtn += preSortKey + (list.contains(key) ? compareSortMap.get(key) : "99");
			}
		}
		
		rtn += "_" + _id;
		
		return rtn;
	}
	
	private static String makeValidSortKey(List<String> list, String _id, String prevSortKey) {
		return makeValidSortKey(list, _id, prevSortKey, null);
	}

	private static String checkMultiLicenseError(Map<String, List<String>> addedIdList, List<ProjectIdentification> list) {
		if(list != null) {
			for(ProjectIdentification bean : list) {
				if(addedIdList.containsKey(avoidNull(bean.getGridId(), bean.getComponentId()))) {
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
		
		for (Element em : binaryEmList) {
			noticeBinaryList.add(em.text());
			
			if (em.text().startsWith("/")) {
				noticeBinaryList.add(em.text().substring(1));
			} else {
				noticeBinaryList.add("/" + em.text());
			}
			
			// path 정보를 무시하고 binary 파일명만 추가 (binary file은 사전에 중복 제거되어 유니크하다)
			if(em.text().indexOf("/") > -1) {
				noticeBinaryList.add(em.text().substring(em.text().lastIndexOf("/")));
				noticeBinaryList.add(em.text().substring(em.text().lastIndexOf("/") + 1));
			} else {
				if(!noticeBinaryList.contains( ("/" + em.text()) )) {
					noticeBinaryList.add("/" + em.text());
				}
			}
		}
		
		binaryEmList = doc.select("div.file-list");
		
		for (Element em : binaryEmList) {
			String _fileStr = (em.html()).replaceAll("<br>", "\n").replaceAll("<br />", "\n").replaceAll("<br/>", "\n").replaceAll("\r\n", "\n");
			
			for(String s : _fileStr.split("\n", -1)) {
				if(isEmpty(s)) {
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
				if(!s.endsWith("/") && s.indexOf("/") > -1) {
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
		
		for(Element el : fileList) {
			if(el.childNodeSize() == 0) {
				continue;
			}
			Node node = el.childNode(0);
            String nodeValue = node.toString();
            
            nodeValue = StringUtil.avoidNull(nodeValue, "").replace("\n", "");
            if(StringUtil.isEmpty(nodeValue)) {
            	continue;
            }
            noticeBinaryList.add(nodeValue);
            
            if (nodeValue.startsWith("/")) {
                noticeBinaryList.add(nodeValue.substring(1));
            } else {
                noticeBinaryList.add("/" + nodeValue);
            }
            
            // path 정보를 무시하고 binary 파일명만 추가 (binary file은 사전에 중복 제거되어 유니크하다)
            if(nodeValue.indexOf("/") > -1) {
                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/")));
                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/") + 1));
            } else {
                if(!noticeBinaryList.contains( ("/" + nodeValue) )) {
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
//            if(nodeValue.indexOf("/") > -1) {
//                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/")));
//                noticeBinaryList.add(nodeValue.substring(nodeValue.lastIndexOf("/") + 1));
//            } else {
//                if(!noticeBinaryList.contains( ("/" + nodeValue) )) {
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
		
		if(licenseList != null) {
			for(OssComponentsLicense bean : licenseList) {
				if(!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					// 확인 가능한 라이선스 중에서만 obligation 대상으로 한다.
					if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName()).toUpperCase())) {
						LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().toUpperCase());
						
						if(CoConstDef.FLAG_YES.equals(license.getObligationNeedsCheckYn())) {
							return CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK;
						} else if(CoConstDef.FLAG_YES.equals(license.getObligationDisclosingSrcYn())) {
							rtnVal = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
						} else if(isEmpty(rtnVal) && CoConstDef.FLAG_YES.equals(license.getObligationNotificationYn())) {
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
		
		if(licenseList != null) {
			for(ProjectIdentification bean : licenseList) {
				if(!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					// 확인 가능한 라이선스 중에서만 obligation 대상으로 한다.
					if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName()).toUpperCase())) {
						LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().toUpperCase());
						
						if(CoConstDef.FLAG_YES.equals(license.getObligationNeedsCheckYn())) {
							return CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK;
						} else if(CoConstDef.FLAG_YES.equals(license.getObligationDisclosingSrcYn())) {
							rtnVal = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
						} else if(isEmpty(rtnVal) && CoConstDef.FLAG_YES.equals(license.getObligationNotificationYn())) {
							rtnVal = CoConstDef.CD_DTL_OBLIGATION_NOTICE;
						}
					}
				}
			}
		}
		
		return rtnVal;
	}

	public static String convertUrlLinkFormat(String url) {
		if(!isEmpty(url) && url.startsWith("www")) {
			return "http://" + url;
		}
		
		return url;
	}

	public static String makeOssTypeStr(String ossType) {
		String rtn = "";
		
		if(!isEmpty(ossType)) {
			ossType = ossType.toUpperCase();
			
			if(ossType.indexOf("M") > -1) {
				if(!isEmpty(rtn)) {
					rtn += ", ";
				}
				rtn += "Multi";
			}
			
			if(ossType.indexOf("D") > -1) {
				if(!isEmpty(rtn)) {
					rtn += ", ";
				}
				rtn += "Dual";
			}
			
			if(ossType.indexOf("V") > -1) {
				if(!isEmpty(rtn)) {
					rtn += ", ";
				}
				rtn += "v-Diff";
			}
		}
		
		return rtn;
	}
	
	public static String getMsApplicationContentType(String fileName) {
		String type = "";
		
		if(!isEmpty(fileName)) {
			type = "application/octet-stream";
		}
		
		return type;
	}

	public static String lineReplaceToBR(String s) {
		return avoidNull(s).replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>");
	}

	public static String brReplaceToLine(String s) {
		return avoidNull(s).replaceAll("\r\n", "\n").replaceAll("<br>", "\n").replaceAll("<br />", "\n").replaceAll("<br/>", "\n").replaceAll("<br \\>", "\n").replaceAll("<br\\>", "\n");
	}

	public static boolean isIgnoreLicense(String licenseName) {
		boolean result = false;
		
		if(!isEmpty(CoCodeManager.CD_ROLE_OUT_LICENSE)) {
			for(String license : avoidNull(licenseName).split(",")) {
				for(String s : CoCodeManager.CD_ROLE_OUT_LICENSE.split("\\|")) {
					if(s.trim().equalsIgnoreCase(license)) {
						result = true;
						break;
					}
				}
			}
		}
		
		// license type이 NA가 아닌 라이선스가 포함되어 있거나, Unconfirmed license인 경우 false
		if(result) {
			for(String license : avoidNull(licenseName).split(",")) {
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.toUpperCase())) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
					if(!"NA".equalsIgnoreCase(licenseMaster.getLicenseType())) {
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
		if(!isEmpty(s)) {
			try {
				return CommonFunction.lineReplaceToBR(HtmlUtils.htmlEscape(brReplaceToLine(s)));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return s;
	}
	
	public static boolean isIgnoreLicense(List<OssComponentsLicense> list) {
		if(list != null && !list.isEmpty()) {
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
		
		if(!isEmpty(reportkey)) {
			sessionObjReport = getSessionObject(reportkey);
		}
		
		if( (sessionObj != null || sessionObjReport != null) && ossComponents != null) {
			// session에 merge할 정보가 존재하면, 사용자 편집 정보와, session 정보를 모두  Map 형태로 변환하여 상위 grid정보를 기준으로 merge 한다.
			Map<String, List<ProjectIdentification>> subGridInfo = new HashMap<>();
			Map<String, Object> sessionMap = null;
			Map<String, Object> sessionMapReport = null;
			
			if(sessionObj != null) {
				sessionMap = (Map<String, Object>) sessionObj;
			}
			
			if(sessionObjReport != null) {
				sessionMapReport = (Map<String, Object>) sessionObjReport;
			}
			
			if( (sessionMap != null && sessionMap.containsKey("subData"))  || (sessionMapReport != null && sessionMapReport.containsKey("subData"))) {
				Map<String, List<ProjectIdentification>> subGridSessionInfo = null;
				Map<String, List<ProjectIdentification>> subGridSessionInfoReport = null;
				
				if(sessionMap != null) {
					subGridSessionInfo = (Map<String, List<ProjectIdentification>>) sessionMap.get("subData");
				}
				
				if(sessionMapReport != null) {
					subGridSessionInfoReport = (Map<String, List<ProjectIdentification>>) sessionMapReport.get("subData");
				}
				
				if(ossComponentsLicense != null) {
					for(List<ProjectIdentification> subTables : ossComponentsLicense) {
						String _key = subTables.get(0).getComponentId();
						
						// 최초저장이면 component id는 설정되어 있지 않음
						// 이런경우는 grid id에서 "-" 앞자리를 키로 가져야함
						if(isEmpty(_key)) {
							_key = subTables.get(0).getGridId().split("-")[0];
						}
						
						subGridInfo.put(_key, subTables);
					}
				}

				if(subGridSessionInfo != null && !subGridSessionInfo.isEmpty()) {
					for(ProjectIdentification bean : ossComponents) {
						// subGrid key에 주의 해야함 grid id로 매핑
						if(!duplicateCheckList.contains(bean.getGridId()) && !subGridInfo.containsKey(bean.getGridId()) && subGridSessionInfo.containsKey(bean.getGridId())) {
							duplicateCheckList.add(bean.getGridId());
							
							// multi => single로 변경한 경우 대응
							// subGridInfo 에 해당 grid id 로 값이 없다는 것인 single을 의미한다.
							if(CoConstDef.LICENSE_DIV_SINGLE.equals(bean.getLicenseDiv()) && subGridSessionInfo.get(bean.getGridId()) != null && subGridSessionInfo.get(bean.getGridId()).size() > 1) {
								
							} else {
								ossComponentsLicense.add(subGridSessionInfo.get(bean.getGridId()));
							}
						}
					}					
				}
				
				if(subGridSessionInfoReport != null && !subGridSessionInfoReport.isEmpty()) {
					for(ProjectIdentification bean : ossComponents) {
						// subGrid key에 주의 해야함 grid id로 매핑
						if(!duplicateCheckList.contains(bean.getGridId()) && !subGridInfo.containsKey(bean.getGridId()) && subGridSessionInfoReport.containsKey(bean.getGridId())) {
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
		
		if(!isEmpty(code)) {
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
		
		if(!isEmpty(reportKey)) {
			return makeSessionKey(loginUserName(), reportKey, prjId);
		}
		
		return null;
	}

	public static OssComponentsLicense reMakeLicenseBean(ProjectIdentification comLicense, String licenseDiv) {
		OssComponentsLicense license = new OssComponentsLicense();
		// 컴포넌트 ID 설정
		license.setComponentId(comLicense.getComponentId());
		
		// 라이센스 ID 설정
		// 2017-11-13 yuns 라이선스 ID 및 이름은 DB를 기준으로 재설정함
		String licenseName = comLicense.getLicenseName();
		
		if(!isEmpty(licenseName)) {
			licenseName = licenseName.replaceAll("\\<.*\\>", ""); // 시점이 맞지않아서 licenseName에 valid Message가 포함되어 저장할 경우 제거 함.
		}
		
		license.setLicenseId(CommonFunction.getLicenseIdByName(licenseName));
		license.setLicenseName(CommonFunction.getLicenseNameById(license.getLicenseId(), licenseName));
		
		// 기타 설정
		//license.setLicenseName(comLicense.getLicenseName());
		license.setLicenseText(comLicense.getLicenseText());
		license.setCopyrightText(comLicense.getCopyrightText());
		
		//TODO - License ExcludeYn값에 대해서 재점검해야 함. 
		if(CoConstDef.LICENSE_DIV_SINGLE.equals(licenseDiv)) {
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
		
		for(OssLicense bean : list) {
			ProjectIdentification liBean = new ProjectIdentification();
			liBean.setLicenseId(bean.getLicenseId());
			liBean.setOssLicenseComb(bean.getOssLicenseComb());
			liBean.setLicenseName(bean.getLicenseName());
			liBean.setLicenseText(selectedLicenseBean.getLicenseText());
			liBean.setComponentId(selectedLicenseBean.getComponentId());
			liBean.setGridId(selectedLicenseBean.getGridId());
			
			if(bean.getLicenseName().toUpperCase().trim().equals(selectedLicenseName.toUpperCase().trim())) {
				boolean isDup = false;
				
				for(String licenseName : licenseNameList){
					if(licenseName.toUpperCase().trim().equals(bean.getLicenseName().toUpperCase().trim())){
						isDup = true;
					}
				}
				
				if(isDup){
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
		
		if(!hasLicense) {
			selectedLicenseBean.setOssLicenseComb("OR");
			licenseList.add(selectedLicenseBean);
		}
		
		return licenseList;
	}
	
	public static List<ProjectIdentification> reMakeLicenseComponentListMultiToSingle(List<ProjectIdentification> list) {
		if(list != null && !list.isEmpty()) {
			List<ProjectIdentification> newLicenseList = new ArrayList<>();
			
			for(ProjectIdentification bean : list) {
				if(!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					if(!isEmpty(bean.getOssLicenseComb())) {
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
			    
			    if("xml".equalsIgnoreCase(FilenameUtils.getExtension(noticeFileName))) {
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
			
			for(String s : resultFileContents) {
				if(isFirst) {
					isFirst = false;
					
					if(!isEmpty(s)) {
						String[] resultCols = s.split("\t", -1);
						int idx = 0;
						
						for(String idxStr : resultCols) {
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
				
				if(isEmpty(s)) {
					continue;
				}
				
				if(avoidNull(s).startsWith("<Removed>")) {
					log.debug("Removed : " + s);
				} else {
					//0 binary , 1 directory, 5 license
					if(idxBinaryName > -1){
						String[] resultCols = s.split("\t", -1);
						
						if(!isEmpty(resultCols[idxBinaryName])) {
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
							
							if(idxDirectory > -1 && resultCols.length >= idxDirectory +1) {
								addComponent.setSourceCodePath(resultCols[idxDirectory]);
							}
							
							if(idxNoticeHtml > -1 && resultCols.length >= idxNoticeHtml +1) {
								addComponent.setBinaryNotice(resultCols[idxNoticeHtml]);
							}
							
							if(idxOssName > -1 && resultCols.length >= idxOssName +1) {
								addComponent.setOssName(resultCols[idxOssName]);
							}
							
							if(idxOssVersion > -1 && resultCols.length >= idxOssVersion +1) {
								addComponent.setOssVersion(resultCols[idxOssVersion]);
							}
							
							if(idxLicense > -1 && resultCols.length >= idxLicense +1) {
								OssComponentsLicense license = new OssComponentsLicense();
								license.setLicenseName(resultCols[idxLicense]);
								addComponent.addOssComponentsLicense(license);
							}
							
							if(idxTlsh> -1 && resultCols.length >= idxTlsh +1) {
								String _tlsh = avoidNull(resultCols[idxTlsh]);
								if("-".equals(_tlsh)) {
									_tlsh = "0";
								}
								addComponent.setTlsh(_tlsh);
							}
							
							if(idxChecksum > -1 && resultCols.length >= idxChecksum +1) {
								addComponent.setCheckSum(resultCols[idxChecksum]);
							}
							
							if(!existsBinaryName.contains(resultCols[idxBinaryName])) {
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
		if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(licenseName).toUpperCase())) {
			return avoidNull(CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(licenseName).toUpperCase()).getWebpage());
		}
		
		return "";
	}
	
	public static String getSelectedLicenseString(List<ProjectIdentification> list) {
		String rtn = "";
		
		if(list != null) {
			for(ProjectIdentification license : list) {
				if(!CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					if(!isEmpty(rtn)) {
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
                
                if(property1 == null) {
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
                
                if(property1 == null) {
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
                
                if(property1 == null) {
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
		if(list != null) {
			for(ProjectIdentification bean : list) {
				// license detail 아이콘 표시 여부를 위해 여기서 license id를 검증한다.
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName().toUpperCase().trim()))) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static String makeLicenseInternalUrl(LicenseMaster licenseMaster, boolean distributionFlag) {
		if(licenseMaster != null) {
			String filePath = appEnv.getProperty("internal.url.dir.path");
			String licenseName = !isEmpty(licenseMaster.getShortIdentifier()) ? licenseMaster.getShortIdentifier() : !isEmpty(licenseMaster.getLicenseNameTemp()) ? licenseMaster.getLicenseNameTemp() : licenseMaster.getLicenseName();
			
			if(!isEmpty(licenseName)) {
				String fileName = licenseName.replaceAll(" ", "_").replaceAll("/", "_") + ".html";
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
		if(!isEmpty(s)) {
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
		if(componentLicenseList != null) {
			String guideStr = "";
			
			for(ProjectIdentification bean : componentLicenseList) {
				if(CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				if(CoCodeManager.LICENSE_USER_GUIDE_LIST.contains(avoidNull(bean.getLicenseName()).toUpperCase())) {
					LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().trim().toUpperCase());
					
					if(license != null && !isEmpty(license.getDescription())) {
						if(!isEmpty(guideStr)) {
							guideStr += "<br>";
						}
						
						guideStr += license.getDescription();
					}
					
				}
			}
			
			if(!isEmpty(guideStr)) {
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
		
		if(validMessageMap != null) {
			for(String key : validMessageMap.keySet()) {
				if("isValid".equalsIgnoreCase(key)) {
					continue;
				}
				String msg = removeLineSeparator(validMessageMap.get(key));
				if(key.indexOf(".") > -1) {
					rtnStr += "<br />" + key.substring(0, key.indexOf(".")) + " : " + msg;
				} else {
					rtnStr += "<br />" + key + " : " + msg;
				}
			}
		}
		
		return rtnStr;
	}

	public static String makeValidMsgTohtml(Map<String, String> validMessageMap, List<ProjectIdentification> ossComponents) {
		List<String> rtnStrList = new ArrayList<>();
		String rtnStr = getMessage("msg.oss.check.ossName.format");
		
		if(validMessageMap != null) {
			for(String key : validMessageMap.keySet()) {
				if(rtnStrList.size() == 10) {
					break;
				}
				
				for(ProjectIdentification pi : ossComponents) {
					if(pi.getGridId().equals(key.substring(key.indexOf(".") + 1, key.length()))) {
						String replaceString = "";
						if(pi.getOssName().contains(",")) {
							replaceString = pi.getOssName().replace(",", "&#44;");
						}else if(pi.getOssName().contains("<")) {
							replaceString = pi.getOssName().replace("<", "&#60;");
						}else if(pi.getOssName().contains(">")) {
							replaceString = pi.getOssName().replace(">", "&#62;");
						}
						if(!isEmpty(replaceString) && !rtnStrList.contains(replaceString)) {
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
	
	public static Boolean booleanOssNameFormatForValidMsg(Map<String, String> validMessageMap) {
		boolean validFlag = false;
		
		if(validMessageMap != null) {
			for(String key : validMessageMap.keySet()) {
				if("isValid".equalsIgnoreCase(key)) {
					continue;
				}
				
				String msg = removeLineSeparator(validMessageMap.get(key));
				if(key.indexOf(".") > -1) {
					if(msg.contains("Formatting") && key.substring(0, key.indexOf(".")).equals("ossName")) {
						validFlag = true;
						break;
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
		
		if(ossComponents != null && ossComponentsLicense != null) {
			if(ossComponentsLicense != null && !ossComponentsLicense.isEmpty()) {
				for(List<ProjectIdentification> list : ossComponentsLicense) {
					for(ProjectIdentification licenseBean : list) {
						String componentId = isEmpty(licenseBean.getComponentId()) ? licenseBean.getGridId().split("-")[0] : licenseBean.getComponentId();
						
						if(!isEmpty(componentId)) {
							licenseMap.put(componentId, list);
							
							break;
						}
					}
				}
			}
			
			for(ProjectIdentification bean : ossComponents) {
				//if(!"-".equals(bean.getOssName()) && !isEmpty(bean.getOssName()) && !StringUtil.contains(bean.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX) && !isEmpty(bean.getComponentId())) {
				if(!"-".equals(bean.getOssName()) && !isEmpty(bean.getOssName()) ) {
					String key = bean.getOssName().toUpperCase().trim() + "_" + avoidNull(bean.getOssVersion()).trim().toUpperCase();
					String componentId = isEmpty(bean.getComponentId()) ? bean.getGridId().split("-")[0] : bean.getComponentId();
					
					if(CoCodeManager.OSS_INFO_UPPER.containsKey(key)) {
						OssMaster ossMaster = CoCodeManager.OSS_INFO_UPPER.get(key);
						
						if(CoConstDef.LICENSE_DIV_MULTI.equals(ossMaster.getLicenseDiv())) {
							//boolean isDiff = false;
							List<ProjectIdentification> licenseList = new ArrayList<>();
							
							if(licenseMap.containsKey(componentId)) {
								licenseList = licenseMap.get(componentId);
							}
							
							// licnese list를 찾는다. 지금은 사용하지 않으나, multi => single로 변경한 경우 사용예정
							// 해당 license 정보를 제외한 list를 만든다.
							
							if(CoConstDef.LICENSE_DIV_SINGLE.equals(avoidNull(bean.getLicenseDiv(), CoConstDef.LICENSE_DIV_SINGLE))) {
								// oss master는 multi license이고, component는 single license로 정의된 경우
								// multi license의 첫번째 license를 선택한 상태로 적용한다.
								// oss master에 해당 license가 없는 경우 마지막에 or 조건으로 추가한다.
								
								ProjectIdentification paramBean = bean;
								// 새로만든 license list를 추가한다.						
								
								if(StringUtil.contains(bean.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
									licenseMap.put(bean.getGridId(), CommonFunction.reMakeLicenseComponentList(ossMaster.getOssLicenses(), paramBean));
								}else{
									licenseMap.replace(componentId, CommonFunction.reMakeLicenseComponentList(ossMaster.getOssLicenses(), paramBean));
								}
								
								bean.setLicenseDiv(CoConstDef.LICENSE_DIV_MULTI);
								isChanged = true;
							}
						} else if(CoConstDef.LICENSE_DIV_SINGLE.equals(ossMaster.getLicenseDiv()) 
								&& CoConstDef.LICENSE_DIV_MULTI.equals(avoidNull(bean.getLicenseDiv(), CoConstDef.LICENSE_DIV_SINGLE))) {
							
						}
					}					
				}
			}
		}
		
		if(!ossComponentsAddList.isEmpty()) {
			ossComponents.addAll(ossComponentsAddList);
		}
		
		resultMap.put("mainList", ossComponents);
		
		if(isChanged) {
			List<List<ProjectIdentification>> newOssComponentsLicense = new ArrayList<>();
			
			for(List<ProjectIdentification> _tmp : licenseMap.values()) {
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
		
		if(!isEmpty(schKeyword)) {
			for(String s : schKeyword.split(" ")) {
				if(!isEmpty(s)) {
					if(sb.length() > 0) {
						sb.append(" AND ");
					}
					
					sb.append(field + " LIKE '%"+s+"%'");
				}
			}
		}
		
		if(sb.length() > 0) {
			sb.insert(0, " (");
			sb.append(") ");
		}
		
		return sb.toString();
	}

	public static void setOssDownloadLocation(List<Map<String, Object>> binaryList) {
		for(Map<String, Object> binaryMap : binaryList) {
			String ossName = "";
			String ossVersion = "";
			String downloadlocation = "";
			
			if(binaryMap.containsKey("ossName")) {
				ossName = (String) binaryMap.get("ossName");
				
				if(binaryMap.containsKey("ossVersion")) {
					ossVersion = (String) binaryMap.get("ossVersion");
				}
				
				String key = (ossName + "_" + avoidNull(ossVersion)).toUpperCase();
				if(CoCodeManager.OSS_INFO_UPPER.containsKey(key)){
					downloadlocation = avoidNull(CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(ossVersion)).toUpperCase() ).getDownloadLocation());
				}
			}
			
			binaryMap.put("downloadlocation", downloadlocation);
		}
	}

	public static String getPlatformName(String str) {
		if(!isEmpty(str)) {
			String arr[]  = str.trim().split(" ");
			
			if(arr.length > 1) {
				String version = arr[arr.length -1];
				
				if(StringUtil.isFormattedString(version, "[\\.0-9]+")) {
					String _temp = "";
					
					for(int i=0; i<arr.length -1; i++) {
						if(!isEmpty(_temp)) {
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
		if(!isEmpty(str)) {
			String arr[] = str.trim().split(" ");
			
			if(arr.length > 1) {
				String version = arr[arr.length -1];
				
				if(StringUtil.isFormattedString(version, "[\\.0-9]+")) {
					return version.trim();
				}
			}
		}
		
		return "";
	}
	
	@SuppressWarnings("unused")
	public static String getObligationTypeWithAndLicense(List<OssLicense> andlicenseGroup) {
		if(andlicenseGroup != null) {
			boolean sourceCode = false;
			boolean notice = false;
			boolean needcheck = false;
			
			for(OssLicense bean : andlicenseGroup) {
				LicenseMaster license = CoCodeManager.LICENSE_INFO_BY_ID.get(bean.getLicenseId());
				
				if(license != null) {
					if(CoConstDef.FLAG_YES.equals(license.getObligationDisclosingSrcYn())) {
						sourceCode = true;
					}
					
					if(CoConstDef.FLAG_YES.equals(license.getObligationNotificationYn())) {
						notice = true;
					}
				}
			}
			
			if(sourceCode) {
				return CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE;
			} else if(notice) {
				return CoConstDef.CD_DTL_OBLIGATION_NOTICE;
			}
		}
		
		return "";
	}

	@SuppressWarnings("unused")
	public static List<String> getBinaryListBinBinaryTxt(T2File binaryTextFile) {
		List<String> binaryList = Lists.newArrayList();
		
		if(binaryTextFile != null) {
			try {
				List<String> resultFileContents =FileUtils.readLines(new File(binaryTextFile.getLogiPath() + "/" + binaryTextFile.getLogiNm()), "UTF-8");
				boolean isFirst = true;
				
				int idxBinaryName = -1;
				int idxTlsh = -1;
				int idxChecksum = -1;
				
				for(String s : resultFileContents) {
					if(isFirst) {
						isFirst = false;
						
						if(!isEmpty(s)) {
							String[] resultCols = s.split("\t", -1);
							int idx = 0;
							
							for(String idxStr : resultCols) {
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
					
					if(isEmpty(s)) {
						continue;
					}

					//0 binary , 1 directory, 5 license
					if(idxBinaryName > -1){
						String[] resultCols = s.split("\t", -1);
						
						if(!isEmpty(resultCols[idxBinaryName])) {
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
		
		if(componentLicenseList != null) {
			String restrictionStr = "";
			
			for(ProjectIdentification bean : componentLicenseList) {
				LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseName().trim().toUpperCase());
				
				if(license != null && !isEmpty(license.getRestriction()) && !CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					restrictionStr += (isEmpty(restrictionStr)?"":",") + license.getRestriction(); 
				}
			}
			
			String restrictionArr[] = restrictionStr.split(",");
			List<String> restrictionList = new ArrayList<>();
			List<String> distinctList = new ArrayList<String>();
			
			// String 배열 -> String 리스트
			for(int i = 0 ; i < restrictionArr.length ; i++){
				restrictionList.add(restrictionArr[i]);
			}
			// 중복 제거
            for(String str : restrictionList){
                if (!distinctList.contains(str)) {
                	distinctList.add(str);
                }
            }
            
            for(String str : distinctList){
            	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
            }
		}
		
		return returnStr;
	}
	
	@SuppressWarnings("unused")
	public static String setLicenseRestrictionList(String restrictionStr) {
		String returnStr = "";
		
		if(!isEmpty(restrictionStr)) {
			String restrictionArr[] = restrictionStr.split(",");
			List<String> restrictionList = new ArrayList<>();
			List<String> distinctList = new ArrayList<String>();
			
			// String 배열 -> String 리스트
			for(int i = 0 ; i < restrictionArr.length ; i++){
				restrictionList.add(restrictionArr[i]);
			}
			
            for(String str : restrictionList){
            	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
            }
		}
		
		return returnStr;
	}
	
	public static String setLicenseRestrictionListById(String licenseIdStr) {
		String returnStr = "";
		
		if(licenseIdStr != null) {
			String restrictionStr = "";
			String licenseIdArr[] = licenseIdStr.split(",");
			
			for(int i = 0 ; i < licenseIdArr.length ; i++) {
				LicenseMaster license = CoCodeManager.LICENSE_INFO_BY_ID.get(licenseIdArr[i]);
				if(license != null && !isEmpty(license.getRestriction())) {
					restrictionStr += (isEmpty(restrictionStr)?"":",") + license.getRestriction(); 
				}
			}
			
			String restrictionArr[] = restrictionStr.split(",");
			List<String> restrictionList = new ArrayList<>();
			List<String> distinctList = new ArrayList<String>();
			
			// String 배열 -> String 리스트
			for(int i = 0 ; i < restrictionArr.length ; i++){
				restrictionList.add(restrictionArr[i]);
			}
			
			// 중복 제거
            for(String str : restrictionList){
                if (!distinctList.contains(str)) {
                	distinctList.add(str);
                }
            }
            
            for(String str : distinctList){
            	returnStr += (isEmpty(returnStr)?"":"\n") + CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, str.trim().toUpperCase());
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
		if(ambiguousInfo == null) {
			ambiguousInfo = new HashMap<>();
		}
		
		String filterCondition = "";
		String[] dateField = {"creationDate", "publDate", "modiDate", "regDt"};
		
		if(exceptionMap == null) {
			exceptionMap = new HashMap<>();
		}
		
		exceptionMap.put("copyrighttext", "copyright");
		List<String> numberFormatColumns = new ArrayList<>();
		numberFormatColumns.add("CVSS_SCORE");
		
		if(!isEmpty(filters)) {
			Type collectionType1 = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> filtersMap = (Map<String, Object>) fromJson(filters, collectionType1);
			
			if(filters != null && filtersMap.containsKey("rules")) {
				for(Map<String, String> ruleMap : (List<LinkedTreeMap<String, String>>)filtersMap.get("rules")) {
					String field = ruleMap.get("field");
					String op =  ruleMap.get("op");
					String data = ruleMap.get("data");
					String startCondition = "";
					String endCondition = "";
					boolean isNumberFormat = numberFormatColumns.contains(StringUtil.convertToUnderScore(field).toUpperCase());
					
					if(!isEmpty(field) && !isEmpty(data)) {
						for( String key : exceptionMap.keySet() ){ 
							if(field.equalsIgnoreCase(key)) {
								field = exceptionMap.get(key);
							}
						}
						
						boolean dateB = false;
						
						for(String dateF : dateField) {
							if(field.equalsIgnoreCase(dateF)) {
								dateB = true;
							}
						}
						
						switch(op) {
							case "eq":
								if(!isNumberFormat) {
									if(upperFlag){
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
								if(!isNumberFormat) {
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
								if(upperFlag){
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
								if(!isNumberFormat) {
									startCondition = " < '";
									endCondition = "'";
								} else {
									startCondition = " < ";
								}
								
								break;
							case "le":
								if(!isNumberFormat) {
									startCondition = " <= '";
									endCondition = "'";
								} else {
									startCondition = " <= ";
								}
								
								break;
							case "gt":
								if(!isNumberFormat) {
									startCondition = " > '";
									endCondition = "'";
								} else {
									startCondition = " > ";
								}
								
								break;
							case "ge":
								if(!isNumberFormat) {
									startCondition = " >= '";
									endCondition = "'";
								} else {
									startCondition = " >= ";
								}
								
								break;
							default:
								if(dateB) {
									startCondition = " = '";
									endCondition = "'";
								}else{
									startCondition = " LIKE '%";
									endCondition = "%'";
								}
								
								break;
						}
						
						if(!upperFlag){
							field = StringUtil.convertToUnderScore(field).toUpperCase();
						}
						
						if(ambiguousInfo.containsKey(field) && !isEmpty(ambiguousInfo.get(field))) {
							field = ambiguousInfo.get(field) + "." + field;
						}
						
						// 예외조건
						if(dateB) {
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
		if(bean != null && !isEmpty(bean.getOssName())) {
			if("N/A".equals(bean.getOssVersion())) {
				bean.setOssVersion("");
			}
			
			String findKey = (bean.getOssName().trim() +"_" + avoidNull(bean.getOssVersion()).trim()).toUpperCase();
			OssMaster masterBean = CoCodeManager.OSS_INFO_UPPER.get(findKey);
			
			if(masterBean != null) {
				bean.setOssId(masterBean.getOssId());
				bean.setOssName(masterBean.getOssName());
			}
		}
		
		return bean;
	}
	
	public static OssComponentsLicense findLicenseIdAndName(OssComponentsLicense bean) {
		return null;
	}

	public static List<OssComponentsLicense> findOssLicenseIdAndName(String ossId,
			List<OssComponentsLicense> ossComponentsLicenseList) {
		// 먼저 license Id는 찾을수 있으면 모두 설정한다.
		if(ossComponentsLicenseList != null) {
			for(OssComponentsLicense licenseBean : ossComponentsLicenseList) {
				LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(licenseBean.getLicenseName(), "").trim().toUpperCase());
				
				if(licenseMaster != null) {
					licenseBean.setLicenseId(licenseMaster.getLicenseId());
					licenseBean.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(),licenseMaster.getLicenseName()));
					
					// OSS_LICENSE 에 설정값이 있는 경우 우선으로 설정하지만, 없는 경우 license master를 기준으로 설정하기 때문에 일단 여기서는 master기준으로 설정
					licenseBean.setLicenseText(licenseMaster.getLicenseText());
				}
			}
		}
		
		// oss에서 추가설정한 license text 및 oss copyright 설정
		if(!isEmpty(ossId)) {
			OssMaster ossMaster = CoCodeManager.OSS_INFO_BY_ID.get(ossId);
			
			if(ossMaster != null) {
				// oss master에 등록된 licnese순서 기준으로 찾는다 (기본적으로 size가 일치 해야함, multi dual license의 경우)
				int idx=0;
				
				for(OssLicense ossLicense : ossMaster.getOssLicenses()) {
					if(ossComponentsLicenseList != null 
							&&ossComponentsLicenseList.size() >= idx+1 
							&& ossLicense.getLicenseId().equals(ossComponentsLicenseList.get(idx).getLicenseId())) {
						// license text
						// oss_license에 존재하는 경우만 설정
						if(!isEmpty(ossLicense.getOssLicenseText())) {
							ossComponentsLicenseList.get(idx).setLicenseText(ossLicense.getOssLicenseText());
						}
						
						// oss copyright
						if(!isEmpty(ossLicense.getOssCopyright())) {
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
		List<List<ProjectIdentification>> ossComponentLicense = new ArrayList<List<ProjectIdentification>>(); 
		
		for(ProjectIdentification pi : ossComponent) {
			String convertLicenseName = pi.getLicenseName();
			
			if(convertLicenseName.contains("\n")) { // 사용자가 oss-report를 통해 license 정보를 입력할 경우 개행이 있을 case가 존재하여 추가함. 
				pi.setLicenseName(convertLicenseName.replace("\n", " "));
			}
			
			if(StringUtil.isEmpty(pi.getComponentId()) && StringUtil.contains(pi.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)) {
				List<ProjectIdentification> licenseList = new ArrayList<ProjectIdentification>();
				
				if(pi.getLicenseName().contains(",")) {
					List<String> licenseNames = Arrays.asList(pi.getLicenseName().split(","));
					pi.setLicenseDiv("M");
					
					for(String nm : licenseNames) {
						ProjectIdentification license = new ProjectIdentification();
						nm = nm.trim(); // license 정보를 입력할때 license 정보 trim 처리함.
						
						license.setGridId(pi.getGridId());
						license.setExcludeYn(CoConstDef.FLAG_NO);
						
						if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(nm.toUpperCase())) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(nm.toUpperCase());
							
							license.setLicenseId(licenseMaster.getLicenseId());
							license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp()));
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
				
				if(pi.getLicenseName().contains(",")) {
					List<String> licenseNames = Arrays.asList(pi.getLicenseName().split(","));
					pi.setLicenseDiv("M");
					
					for(String nm : licenseNames) {
						ProjectIdentification license = new ProjectIdentification();
						nm = nm.trim(); // license 정보를 입력할때 license 정보 trim 처리함.
						
						license.setComponentId(pi.getComponentId());
						license.setGridId(pi.getGridId()+"-");
						license.setExcludeYn(CoConstDef.FLAG_NO);
						
						if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(nm.toUpperCase())) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(nm.toUpperCase());
							
							license.setLicenseId(licenseMaster.getLicenseId());
							license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp()));
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
		
		for(String name : result) {
			if(!CoCodeManager.LICENSE_INFO_UPPER.containsKey(name.toUpperCase())) {
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
		
		for(OssAnalysis bean : analysisResultList) {
			OssAnalysis userData = analysisList
										.stream()
										.filter(e -> e.getComponentId().equals(bean.getGridId()))
										.collect(Collectors.toList()).get(0); // 사용자 입력 정보
			
			userData.setGroupId(userData.getGridId()); // groupId는 user가 입력한 row의 grid Id임.
			
			if(CoConstDef.FLAG_YES.equals(userData.getCompleteYn()) && !isEmpty(userData.getReferenceOssId())) {
				
				OssAnalysis successOssInfo = ossService.getAutoAnalysisSuccessOssInfo(userData.getReferenceOssId());
				
				if(!isEmpty(successOssInfo.getDownloadLocationGroup())) {
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
			
			userData.setTitle("사용자 작성 정보");
			
			if(bean.getResult().toUpperCase().equals("TRUE")) {
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
				String comment  = bean.getComment();
				
				String askalonoLicense = bean.getAskalonoLicense().replaceAll("\\(\\d+\\)", "");
				String scancodeLicense = bean.getScancodeLicense().replaceAll("\\(\\d+\\)", "");
				
				String duplicateNickname = bean.getOssNickname();
				
				if(ossNameCnt == 0 && ossVersionCnt > 0) { // ossVersion 대상
					// 사용자 작성정보의 oss name이 취합정보의 nickname에 들어가는 case를 방지함.
					if(!userData.getOssName().toUpperCase().equals(bean.getOssName().toUpperCase())) {
						duplicateNickname = String.join(",", Arrays.asList(duplicateNickname.split(","))
								.stream()
								.filter(n -> !n.equals(userData.getOssName()))
								.collect(Collectors.toList()));
					}
				}
				
				OssAnalysis totalAnalysis = new OssAnalysis(userData.getGridId(), bean.getOssName(), bean.getOssVersion(), duplicateNickname
						, avoidNull(bean.getConcludedLicense(), null), copyright, bean.getDownloadLocation()
						, userData.getHomepage(), null, comment, bean.getResult(), "취합정보"); // 취합정보
				OssAnalysis askalono = new OssAnalysis(userData.getGridId(), bean.getOssName(), bean.getOssVersion(), duplicateNickname
						, askalonoLicense, null, bean.getDownloadLocation()
						, userData.getHomepage(), null, comment, bean.getResult(), "License text파일 분석 결과"); // License text 정보
				OssAnalysis scancode = new OssAnalysis(userData.getGridId(), bean.getOssName(), bean.getOssVersion(), duplicateNickname
						, scancodeLicense, copyright, bean.getDownloadLocation()
						, userData.getHomepage(), null, comment, bean.getResult(), "Scancode 분석 결과"); // scancode 정보

				userData.setResult("true");
				
				if(ossNameCnt == 0 && ossVersionCnt > 0) { // ossVersion 대상
					totalAnalysis.setGridId(""+gridSeq++);
					askalono.setGridId(""+gridSeq++);
					scancode.setGridId(""+gridSeq++);
					OssAnalysis newestOssInfo = null;
					OssAnalysis totalNewestOssInfo = null;
					
					try {
						newestOssInfo = ossService.getNewestOssInfo(userData); // 사용자 정보의 ossName기준 최신 등록정보
						if(newestOssInfo != null) {
							newestOssInfo.setGridId(""+gridSeq++);
							newestOssInfo.setOssVersion(userData.getOssVersion());
							newestOssInfo.setComment(comment);
						}
						
						if(userData.getOssName().toUpperCase().equals(totalAnalysis.getOssName().toUpperCase())) {
							String newestMergeNickName = "";
							if(newestOssInfo != null) {
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
							
							if(totalNewestOssInfo != null) {
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
					
					changeAnalysisResultList.add(totalAnalysis); // seq 1 : 취합 정보
					
					if(totalNewestOssInfo != null) {
						changeAnalysisResultList.add(totalNewestOssInfo); // seq 2 : 취합정보 최신등록 정보
					}
					
					if(newestOssInfo != null) {
						changeAnalysisResultList.add(newestOssInfo); // seq 3 : 최신등록 정보
					}
					
					changeAnalysisResultList.add(askalono);		 // seq 4 : askalono 정보
					changeAnalysisResultList.add(scancode);		 // seq 5 : scancode 정보
					changeAnalysisResultList.add(userData);		 // seq 6 : 사용자 입력 정보
				} else { // ossName 대상
					totalAnalysis.setGridId(""+gridSeq++);
					
					OssAnalysis totalNewestOssInfo = ossService.getNewestOssInfo(totalAnalysis); // 사용자 정보의 ossName기준 최신 등록정보
					
					if(totalNewestOssInfo != null) {
						totalNewestOssInfo.setGridId(""+gridSeq++);
						totalNewestOssInfo.setOssVersion(userData.getOssVersion());
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
					
					changeAnalysisResultList.add(totalAnalysis); // seq 1 : 취합 정보
					
					if(totalNewestOssInfo != null) {
						changeAnalysisResultList.add(totalNewestOssInfo); // seq 2 : 취합정보 최신등록 정보
					}
					
					changeAnalysisResultList.add(userData);		 // seq 3 : 사용자 입력 정보
					changeAnalysisResultList.add(askalono);		 // seq 4 : askalono 정보
					changeAnalysisResultList.add(scancode);		 // seq 5 : scancode 정보
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
				
				if(ossNameCnt == 0 && ossVersionCnt > 0){
					try {
						OssAnalysis newestOssInfo = ossService.getNewestOssInfo(userData); // 사용자 정보의 ossName기준 최신 등록정보
						
						newestOssInfo.setGridId(""+gridSeq++);
						newestOssInfo.setOssVersion(userData.getOssVersion());
						
						changeAnalysisResultList.add(newestOssInfo); // seq 2 : 최신등록 정보
					} catch (Exception newestException) {
						log.error(newestException.getMessage());
					}
				}
			}
		}
		
		getAnalysisValidation(map, changeAnalysisResultList);
		map.replace("rows", changeAnalysisResultList);
		map.remove("analysisList"); // 분석결과 Data에서는 필요없는 data이므로 제거.
	}
	
	public static ArrayList<Object> checkXlsxFileLimit(List<UploadFile> list){
		ArrayList<Object> result = new ArrayList<Object>();
		
		for(UploadFile f : list) {
			if(f.getSize() > CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT && f.getFileExt().contains("xls")){
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
		
		for(UploadFile f : list) {
			if(f.getSize() > CoConstDef.CD_CSV_UPLOAD_FILE_SIZE_LIMIT && f.getFileExt().contains("csv")){
				result.add("FILE_SIZE_LIMIT_OVER");
				result.add("The file exceeded 5MB.<br>Please delete the blank row or unnecessary data, and then upload it.");
				nextCheck = true;
				break;
			}
			
			if(!nextCheck) {
				boolean tabGubnBoolean = false;
				String commaData = "";
				Scanner sc = null;
				
				try {
					sc = new Scanner(new FileInputStream(new File(f.getFilePath() + "/" + f.getFileName())));
					while(sc.hasNext()) {
						String readLine = sc.nextLine();
						if(!isEmpty(readLine) && !readLine.contains("\t")) {
							tabGubnBoolean = true;
							break;
						}
					}
				} catch (Exception e) {
					log.info(e.getMessage(), e);
				} finally {
					sc.close();
					
					if(tabGubnBoolean) {
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
		
		if(!isDir) {
			if(XmlFile.exists()) {
				String Path = XmlFile.getPath().split("\\.")[0];
				outPath = new File(Path);
				outPath.mkdirs(); // xml file은 upload/android_notice/projectId/* 에 올려두기 때문에 dir를 생성해야 함.
				
				xmlFiles.add(XmlFile);
			}
		} else {
			if(XmlFile.exists()) {
				for(File f : XmlFile.listFiles()) {
					xmlFiles.add((File) f);
				}
			}
			
			outPath = XmlFile;
		}
		
		UUID randomUUID = UUID.randomUUID();
		File outFile = new File(outPath + "/" + randomUUID + ".html");
		LicenseHtmlGeneratorFromXml.generateHtml(xmlFiles, outFile);
		
		return outFile;
	}
	
	public static String mergeNickname(OssAnalysis bean, String newestNickName) {
		String mergeNickname = newestNickName;
		
		if(!isEmpty(mergeNickname)) {
			List<String> nicknameList = new ArrayList<String>();
			
			if(!isEmpty(bean.getOssNickname())) { // nickname이 빈값이 있을 경우 담지 않음.
				nicknameList.addAll(Arrays.asList(bean.getOssNickname().split(",")));
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
	
	public static String VelocityTemplateToString(Map<String, Object> model) {		
		VelocityContext context = new VelocityContext();
		Writer writer = new StringWriter();
		VelocityEngine ve = new VelocityEngine();
		Properties props = new Properties();
		
		for(String key : model.keySet()) {
			if(!"templateURL".equals(key)) {
				context.put(key, model.get(key));
			}
		}
		
	    props.put("resource.loader", "class");
	    props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    props.put("input.encoding", "UTF-8");
	    
		ve.init(props);

		// Core Logic: Velocity engine decides which template should be loaded according to the model's template URL
		// Refer to the 'template' directory for further information.
		try {
			Template template = ve.getTemplate((String) model.get("templateURL"));
			template.merge(context, writer);
			
			return writer.toString();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return "";
	}
	
	public static String getNoticeFileName(String prjId, String prjName, String prjVersion, String dateStr, String fileType) {
		return getNoticeFileName(prjId, prjName, prjVersion, null, dateStr, fileType);
	}
	
	public static String getNoticeFileName(String prjId, String prjName, String prjVersion, String fileName, String dateStr, String fileType) {
		if(isEmpty(fileName)) {
			fileName = "OSSNotice-";
			fileName += prjId + "_" + prjName;
			
			if(!isEmpty(prjVersion)) {
				fileName += "_" + prjVersion;
			}
		}
		
		// file명에 사용할 수 없는 특수문자 체크
		if(!FileUtil.isValidFileName(fileName)) {
			fileName = FileUtil.makeValidFileName(fileName, "_");
		}
		
		fileName += "_" + dateStr;
		
		if(isEmpty(fileType) || fileType == "html"){
			fileName += ".html";
		}else if(fileType == "text"){
			fileName += ".txt";
		} else {
			fileName += fileType;
		}
		
		return fileName;
	}
	
	public static String mergedString(String fromStr, String toStr, int compareTo, String separator) {
		if(compareTo >= 0){
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
		
		if(limit > 0) {
			target = target.substring(0, limit);
		}
		
		return target;
	}
	
	// 구분자 문자열 순서대로 중복 제거 
	public static String removeDuplicateStringToken(String str, String token){ 
		String removeDubString = ""; 
		
		if(!StringUtil.isEmpty(str)){
			String[] arr = StringUtil.split(str, token.trim());
			LinkedHashSet<String> ls = new LinkedHashSet<>();
			
			for(int i=0; i<arr.length; i++){
				ls.add(arr[i]);
			}
			
			Iterator<String> it = ls.iterator();
			
			while(it.hasNext()){
				removeDubString +=it.next()+token.trim();
			}
			
			if(!StringUtil.isEmpty(removeDubString)){
				removeDubString=removeDubString.substring(0, removeDubString.lastIndexOf(token.trim()));
			}
		}
		
		return removeDubString;		
	}
	
	public static void splitDate(String str, Map<String, Object> paramMap, String separator, String prefix) {
		if(!isEmpty(str)) {
			String[] strArr = str.split(separator);
			
			paramMap.put(prefix+"From", strArr[0]);
			paramMap.put(prefix+"To", strArr[1]);
		}
	}
	
	public static String tabTitleSubStr(String code, String value) {
		String tabData = CoCodeManager.getCodeString(code, value);
		
		if(tabData.length() > 12) {
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
		
		if(booleanFlag) {
			// Project Name
			if(!beforeBean.getPrjName().equals(afterBean.getPrjName())) {
				comment += "<p><strong>Project Name</strong><br />";
				comment += "Before : " + beforeBean.getPrjName() + "<br />";
				comment += "After : " + afterBean.getPrjName() + "<br /></p>";
			}

			// Project Version
			if(!beforeBean.getPrjVersion().equals(afterBean.getPrjVersion())) {
				comment += "<p><strong>Project Version</strong><br />";
				comment += "Before : " + beforeBean.getPrjVersion() + "<br />";
				comment += "After : " + afterBean.getPrjVersion() + "<br /></p>";
			}
			
			// Operating System
			if(!beforeBean.getOsType().equals(afterBean.getOsType())) {
				comment += "<p><strong>Operating System</strong><br />";
				comment += "Before : " + beforeBean.getOsType() + "<br />";
				comment += "After : " + afterBean.getOsType() + "<br /></p>";
			}
			
			// Distribution Type
			if(!beforeBean.getDistributionType().equals(afterBean.getDistributionType())) {
				comment += "<p><strong>Distribution Type</strong><br />";
				comment += "Before : " + beforeBean.getDistributionType() + "<br />";
				comment += "After : " + afterBean.getDistributionType() + "<br /></p>";
			}
			
			
			if(!beforeBean.getNetworkServerType().equals(afterBean.getNetworkServerType())) {
				comment += "<p><strong>Network Service only?</strong><br />";
				comment += "Before : " + beforeBean.getNetworkServerType() + "<br />";
				comment += "After : " + afterBean.getNetworkServerType() + "<br /></p>";
			}
			
			if(!beforeBean.getDistributeTarget().equals(afterBean.getDistributeTarget())) {
				comment += "<p><strong>Distribution Site</strong><br />";
				comment += "Before : " + beforeBean.getDistributeTarget() + "<br />";
				comment += "After : " + afterBean.getDistributeTarget() + "<br /></p>";
			}
			
			if(!beforeBean.getNoticeType().equals(afterBean.getNoticeType())) {
				comment += "<p><strong>OSS Notice</strong><br />";
				comment += "Before : " + beforeBean.getNoticeType() + "<br />";
				comment += "After : " + afterBean.getNoticeType() + "<br /></p>";
			}
			
			if(!beforeBean.getPriority().equals(afterBean.getPriority())) {
				comment += "<p><strong>Priority</strong><br />";
				comment += "Before : " + beforeBean.getPriority() + "<br />";
				comment += "After : " + afterBean.getPriority() + "</p>";
			}
			
			String before = beforeBean.getComment().replaceAll("(\r\n|\r|\n|\n\r)", "");
			String after = afterBean.getComment().replaceAll("(\r\n|\r|\n|\n\r)", "");
			if(!before.equals(after)) {
				comment += "<p><strong>Additional Information</strong><br />";
				comment += "Before : " + beforeBean.getComment() + "<br />";
				comment += "After : " + afterBean.getComment() + "</p>";
			}
		} else {
			// Project Division
			if(!beforeBean.getDivision().equals(afterBean.getDivision())) {
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
		
		if(map.containsKey("blockSize")) {
			blockSize = Integer.parseInt((String) map.get("blockSize"));
		}
		
		int totBlockSize = totListSize/pageListSize < 1 ? 1 : totListSize%pageListSize==0?totListSize/pageListSize:(totListSize/pageListSize)+1;

		int startIndex = (curPage-1)*pageListSize;
		
		int totBlockPage = (totBlockSize / blockSize);
		if(totBlockSize != blockSize) totBlockPage++;
		
		int blockPage = ((curPage-1) / blockSize) + 1;
		
		int blockStart = ((blockPage-1) * blockSize) + 1;
		int blockEnd = blockStart+blockSize-1;
		if(blockEnd > totBlockSize) blockEnd = totBlockSize;
		
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
		if(StringUtil.isEmpty(jdbcUrl)) {
			return jdbcUrl;
		}
		if(!jdbcUrl.startsWith("jdbc:")) {
			jdbcUrl = "jdbc:mysql://" + jdbcUrl;
		}
		if(!jdbcUrl.contains("serverTimezone")) {
			jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "serverTimezone=UTC";
		}
		if(!jdbcUrl.contains("autoReconnect")) {
			jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "autoReconnect=true";
		}
		return jdbcUrl;
	}
	
	public static String getOssDownloadLocation(String ossName, String ossVersion) {
		if(!isEmpty(ossName) && CoCodeManager.OSS_INFO_UPPER.containsKey( (ossName + "_" + avoidNull(ossVersion)).toUpperCase() )) {
			return avoidNull( CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(ossVersion)).toUpperCase() ).getDownloadLocation() );
		}
		return "";
	}
	
	public static String httpCodePrint(int code){
		String res = code + "";
		switch(code){
			case HttpURLConnection.HTTP_ACCEPTED: 		  res = "HTTP_ACCEPTED"; break;
			case HttpURLConnection.HTTP_BAD_GATEWAY: 	  res = "HTTP_BAD_GATEWAY"; break;
			case HttpURLConnection.HTTP_BAD_METHOD: 	  res = "HTTP_BAD_METHOD"; break;
			case HttpURLConnection.HTTP_BAD_REQUEST: 	  res = "HTTP_BAD_REQUEST"; break;
			case HttpURLConnection.HTTP_CLIENT_TIMEOUT:   res = "HTTP_CLIENT_TIMEOUT"; break;
			case HttpURLConnection.HTTP_CONFLICT: 		  res = "HTTP_CONFLICT"; break;
			case HttpURLConnection.HTTP_CREATED: 		  res = "HTTP_CREATED"; break;
			case HttpURLConnection.HTTP_ENTITY_TOO_LARGE: res = "HTTP_ENTITY_TOO_LARGE"; break;
			case HttpURLConnection.HTTP_FORBIDDEN: 		  res = "HTTP_FORBIDDEN"; break;
			case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:  res = "HTTP_GATEWAY_TIMEOUT"; break;
			case HttpURLConnection.HTTP_GONE: 			  res = "HTTP_GONE"; break;
			case HttpURLConnection.HTTP_INTERNAL_ERROR:   res = "HTTP_INTERNAL_ERROR"; break;
			case HttpURLConnection.HTTP_LENGTH_REQUIRED:  res = "HTTP_LENGTH_REQUIRED"; break;
			case HttpURLConnection.HTTP_MOVED_PERM: 	  res = "HTTP_MOVED_PERM"; break;
			case HttpURLConnection.HTTP_MOVED_TEMP: 	  res = "HTTP_MOVED_TEMP"; break;
			case HttpURLConnection.HTTP_MULT_CHOICE: 	  res = "HTTP_MULT_CHOICE"; break;
			case HttpURLConnection.HTTP_NO_CONTENT: 	  res = "HTTP_NO_CONTENT"; break;
			case HttpURLConnection.HTTP_NOT_ACCEPTABLE:   res = "HTTP_NOT_ACCEPTABLE"; break;
			case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:res = "HTTP_NOT_AUTHORITATIVE"; break;
			case HttpURLConnection.HTTP_NOT_FOUND: 		  res = "HTTP_NOT_FOUND"; break;
			case HttpURLConnection.HTTP_NOT_IMPLEMENTED:  res = "HTTP_NOT_IMPLEMENTED"; break;
			case HttpURLConnection.HTTP_NOT_MODIFIED: 	  res = "HTTP_NOT_MODIFIED"; break;
			case HttpURLConnection.HTTP_OK: 			  res = "HTTP_OK"; break;
			case HttpURLConnection.HTTP_PARTIAL: 		  res = "HTTP_PARTIAL"; break;
			case HttpURLConnection.HTTP_PAYMENT_REQUIRED: res = "HTTP_PAYMENT_REQUIRED"; break;
			case HttpURLConnection.HTTP_PRECON_FAILED: 	  res = "HTTP_PRECON_FAILED"; break;
			case HttpURLConnection.HTTP_PROXY_AUTH: 	  res = "HTTP_PROXY_AUTH"; break;
			case HttpURLConnection.HTTP_REQ_TOO_LONG: 	  res = "HTTP_REQ_TOO_LONG"; break;
			case HttpURLConnection.HTTP_RESET: 			  res = "HTTP_RESET"; break;
			case HttpURLConnection.HTTP_SEE_OTHER: 		  res = "HTTP_SEE_OTHER"; break;
			case HttpURLConnection.HTTP_UNAUTHORIZED: 	  res = "HTTP_UNAUTHORIZED"; break;
			case HttpURLConnection.HTTP_UNAVAILABLE: 	  res = "HTTP_UNAVAILABLE"; break;
			case HttpURLConnection.HTTP_UNSUPPORTED_TYPE: res = "HTTP_UNSUPPORTED_TYPE"; break;
			case HttpURLConnection.HTTP_USE_PROXY: 		  res = "HTTP_USE_PROXY"; break;
			case HttpURLConnection.HTTP_VERSION: 		  res = "HTTP_VERSION"; break;
			default: break;	
		}
		return res;
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
			if(pri.getLicenseName().contains(",")) {
				List<String> licenseNameList = Arrays.asList(pri.getLicenseName().split(","));
				List<String> licenseNameNicknameCheckList = new ArrayList<>();
				List<String> duplicateList = new ArrayList<>();
				List<Integer> indexList = new ArrayList<>();
				
				for(int j=0; j<licenseNameList.size(); j++) {
	                String licenseName = licenseNameList.get(j).trim();
	                if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
	                	LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
	                    if(licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
	                    	boolean flag = false;
	                    	for(String s : licenseMaster.getLicenseNicknameList()) {
	                    		if(licenseName.equalsIgnoreCase(s)) {
	                    			String disp = avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp());
	                    			licenseNameNicknameCheckList.add(disp);
	                    			flag = true;
	                    			break;
	                    		}
	                    	}
	                    	
	                    	if(!flag) {
	                    		licenseNameNicknameCheckList.add(licenseName);
	                    	}
	                    }else {
	                    	licenseNameNicknameCheckList.add(licenseName);
	                    }
	                }else {
	                	licenseNameNicknameCheckList.add(licenseName);
	                }
				}
				
				for(int i=0; i<licenseNameNicknameCheckList.size(); i++) {
					if(!duplicateList.contains(licenseNameNicknameCheckList.get(i))) {
						duplicateList.add(licenseNameNicknameCheckList.get(i));
						indexList.add(i);
					}
				}
				
				duplicateList = new ArrayList<>();
				for(int i=0; i<indexList.size(); i++) {
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
		
		if(errorCodeMap != null) {
			for(String key : errorCodeMap.keySet()) {
				if(key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_LIST.contains(errorCodeMap.get(key))) {
					unclearObligationList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
				if(key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_NOTLICENSE_LIST.contains(errorCodeMap.get(key))) {
					unclearObligationNotLicenseList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
			}
		}
		if(warningCodeMap != null) {
			for(String key : warningCodeMap.keySet()) {
				if(key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_LIST.contains(warningCodeMap.get(key))) {
					unclearObligationList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
				if(key.indexOf(".") > -1 && UNCLEAR_OBLIGATION_CODE_NOTLICENSE_LIST.contains(warningCodeMap.get(key))) {
					unclearObligationNotLicenseList.add(key.substring(key.indexOf(".") + 1, key.length()));
				}
			}
		}
		
		if(list != null) {
			for(ProjectIdentification bean : list) {
				if("-".equals(bean.getOssName())  
						&& !unclearObligationList.contains(bean.getGridId()) 
						&& isEmpty(bean.getObligationType()) 
						&& !checkIncludeUnconfirmedLicense(bean.getComponentLicenseList())) {
					String obligationType = CommonFunction.getObligationTypeWithSelectedLicense(bean);
					if(!isEmpty(obligationType)) {
						bean.setObligationType(obligationType);
						continue;
					}
				}
				
				// Check unconfirmed license, the actual message is (Declared : ~~), so check again registered license.
				if(unclearObligationList.contains(bean.getGridId())
						|| (!isEmpty(bean.getObligationType()) && (checkIncludeUnconfirmedLicense(bean.getComponentLicenseList()) || checkIncludeNotDeclaredLicense(bean.getOssName(), bean.getOssVersion(), bean.getComponentLicenseList())))) {
					bean.setObligationGrayFlag(CoConstDef.FLAG_YES);
					bean.setObligationMsg(getMessage("msg.project.obligation.unclear"));
				}
				 
				if(unclearObligationNotLicenseList.contains(bean.getGridId())) {
					if(isEmpty(bean.getObligationType())){
						String obligationType = CommonFunction.getObligationTypeWithSelectedLicense(bean);
						if(!isEmpty(obligationType)) {
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
		
		List<String> licenseNameList = getAllAvailableLicenseUpperCaseName(ossName, ossVer);
		for(ProjectIdentification license : licenseList) {
			if(!licenseNameList.contains(license.getLicenseName().toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	private static List<String> getAllAvailableLicenseUpperCaseName(String ossName, String ossVer) {
		String key = (avoidNull(ossName).trim() + "_" + avoidNull(ossVer).trim()).toUpperCase();
		List<String> licenseNameList = new ArrayList<>();
		if(CoCodeManager.OSS_INFO_UPPER.containsKey(key)) {
			OssMaster bean = CoCodeManager.OSS_INFO_UPPER.get(key);
			for(OssLicense _temp : Optional.ofNullable(bean.getOssLicenses()).orElse(new ArrayList<>())) {
				licenseNameList.add(_temp.getLicenseName().toUpperCase());
				LicenseMaster _license = CoCodeManager.LICENSE_INFO_BY_ID.get(_temp.getLicenseId());
				if(_license != null) {
					for(String _nick : Optional.ofNullable(_license.getLicenseNicknameList()).orElse(new ArrayList<>())) {
						licenseNameList.add(_nick.toUpperCase());
					}
				}
			}
			
			for(String _temp : Optional.ofNullable(bean.getDetectedLicenses()).orElse(new ArrayList<>())) {
				licenseNameList.add(_temp.toUpperCase());
				LicenseMaster _license = CoCodeManager.LICENSE_INFO_UPPER.get(_temp.toUpperCase());
				if(_license != null) {
					for(String _nick : Optional.ofNullable(_license.getLicenseNicknameList()).orElse(new ArrayList<>())) {
						licenseNameList.add(_nick.toUpperCase());
					}
				}
			}
		}
		return licenseNameList;
	}

	private static boolean checkIncludeUnconfirmedLicense(List<ProjectIdentification> licenseList) {
		if(licenseList != null) {
			for(ProjectIdentification bean : licenseList) {
				if(!isEmpty(bean.getLicenseName()) && !CoCodeManager.LICENSE_INFO_UPPER.containsKey(bean.getLicenseName().toUpperCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<ProjectIdentification> makeLicensePermissiveList(List<ProjectIdentification> licenses, String currentLicense) {
		List<List<ProjectIdentification>> andCombLicenseList = new ArrayList<>();
		
		for(ProjectIdentification bean : licenses) {
			if(andCombLicenseList.size() == 0 || "OR".equals(bean.getOssLicenseComb())) {
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
		
		for(List<ProjectIdentification> andList : andCombLicenseList) {
			switch(getLicensePermissive(andList)) {
				case CoConstDef.CD_LICENSE_TYPE_PMS:
					if(pmsCnt == 0) {
						licenseList = andList;
						pmsCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_WCP:
					if(pmsCnt == 0 && wcpCnt == 0) {
						licenseList = andList;
						wcpCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_CP:
					if(pmsCnt == 0 && wcpCnt == 0 && cpCnt == 0) {
						licenseList = andList;
						cpCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_PF:
					if(pmsCnt == 0 && wcpCnt == 0 && cpCnt == 0 && pfCnt == 0) {
						licenseList = andList;
						pfCnt++;
					}
					
					break;
				case CoConstDef.CD_LICENSE_TYPE_NA:
					if(pmsCnt == 0 && wcpCnt == 0 && cpCnt == 0 && pfCnt == 0 && naCnt == 0) {
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
			for(int i=0; i<prjIds.length; i++) {
				userIdList = new ArrayList<>();
				param.setPrjId(prjIds[i]);
				Project bean = projectService.getProjectDetail(param);
				
				userIdList.add(bean.getCreator());
				if(bean.getWatcherList() != null) {
					for(String watcher : bean.getWatcherList().stream().map(e -> e.getPrjUserId()).collect(Collectors.toList())) {
						userIdList.add(watcher);
					}
				}
				
				userIdList = userIdList.stream().distinct().collect(Collectors.toList());
				if(!userIdList.contains(userId)) {
					notPermissionList.add(prjIds[i]);
				}
			}
			break;
		
		case "partner":
			PartnerMaster partner = new PartnerMaster();
			for(int i=0; i<prjIds.length; i++) {
				userIdList = new ArrayList<>();
				partner.setPartnerId(prjIds[i]);
				PartnerMaster bean = partnerService.getPartnerMasterOne(partner);
				
				userIdList.add(bean.getCreator());
				if(bean.getPartnerWatcher() != null) {
					for(String watcher : bean.getPartnerWatcher().stream().map(e -> e.getUserId()).collect(Collectors.toList())) {
						userIdList.add(watcher);
					}
				}
				
				userIdList = userIdList.stream().distinct().collect(Collectors.toList());
				if(!userIdList.contains(userId)) {
					notPermissionList.add(prjIds[i]);
				}
			}
			break;
		}
		
		Collections.sort(notPermissionList);
		return notPermissionList;
	}
}
