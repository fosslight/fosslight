/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURL.StandardTypes;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssAnalysis;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.repository.VulnerabilityMapper;
import oss.fosslight.service.*;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationConfig;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Service
@Slf4j
public class OssServiceImpl extends CoTopComponent implements OssService {
	// Service
	@Autowired CommentService commentService;
	@Autowired HistoryService historyService;
	@Autowired ProjectService projectService;
	@Autowired PartnerService partnerService;
	@Autowired VerificationService verificationService;
	@Autowired SelfCheckService selfCheckService;
	@Autowired AutoFillOssInfoService autoFillOssInfoService;

	// Mapper
	@Autowired OssMapper ossMapper;
	@Autowired T2UserMapper userMapper;
	@Autowired FileMapper fileMapper;
	@Autowired ProjectMapper projectMapper;
	@Autowired PartnerMapper partnerMapper;
	@Autowired VulnerabilityMapper vulnerabilityMapper;
	@Autowired CodeMapper codeMapper;
	
	@Override
	public Map<String,Object> getOssMasterList(OssMaster ossMaster) {
		// 기간 검색 조건
		if (!isEmpty(ossMaster.getcEndDate())) {
			ossMaster.setcEndDate(DateUtil.addDaysYYYYMMDD(ossMaster.getcEndDate(), 1));
		}
		
		if (!isEmpty(ossMaster.getmEndDate())) {
			ossMaster.setmEndDate(DateUtil.addDaysYYYYMMDD(ossMaster.getmEndDate(), 1));
		}
		
		if (isEmpty(ossMaster.getOssNameAllSearchFlag())) {
			ossMaster.setOssNameAllSearchFlag(CoConstDef.FLAG_NO);
		}
		
		if (isEmpty(ossMaster.getLicenseNameAllSearchFlag())) {
			ossMaster.setLicenseNameAllSearchFlag(CoConstDef.FLAG_NO);
		}

		if (isEmpty(ossMaster.getDetectedLicenseNameAllSearchFlag())) {
			ossMaster.setDetectedLicenseNameAllSearchFlag(CoConstDef.FLAG_NO);
		}
		
		if (isEmpty(ossMaster.getHomepageAllSearchFlag())) {
			ossMaster.setHomepageAllSearchFlag(CoConstDef.FLAG_NO);
		}
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		ossMaster.setCveIdText();
		int records = ossMapper.selectOssMasterTotalCount(ossMaster);
		ossMaster.setTotListSize(records);
		
		ArrayList<OssMaster> newList = new ArrayList<>();
		List<OssMaster> list = ossMapper.selectOssList(ossMaster);
		
		String orgOssName = ossMaster.getOssName();
		List<String> multiOssList = ossMapper.selectMultiOssList(ossMaster);
		multiOssList.replaceAll(String::toUpperCase);
		
		for (OssMaster oss : list){
			if (multiOssList.contains(oss.getOssName().toUpperCase())) {
				ossMaster.setOssId(oss.getOssId());
				ossMaster.setOssName(oss.getOssName());
				List<OssMaster> subList = ossMapper.selectOssSubList(ossMaster);
				
				newList.addAll(subList);
			} else {
				newList.add(oss);
			}
		}
		
		ossMaster.setOssName(orgOssName);
		
		// license name 처리
		if (newList != null && !newList.isEmpty()) {
			OssMaster param = new OssMaster();
			
			for (OssMaster bean : newList) {
				param.addOssIdList(bean.getOssId());
			}

			List<OssLicense> licenseList = ossMapper.selectOssLicenseList(param);
			
			for (OssLicense licenseBean : licenseList) {
				for (OssMaster bean : newList) {
					if (licenseBean.getOssId().equals(bean.getOssId())) {
						bean.addOssLicense(licenseBean);
						break;
					}
				}
			}

			for (OssMaster bean : newList) {
				if (bean.getOssLicenses() != null && !bean.getOssLicenses().isEmpty()) {
					bean.setLicenseName(CommonFunction.makeLicenseExpression(bean.getOssLicenses()));
					if (!CollectionUtils.isEmpty(bean.getOssLicenses())) {
						String ossLicenseText = "";
						for (OssLicense licenseBean : bean.getOssLicenses()) {
							ossLicenseText += licenseBean.getLicenseName() + ",";
						}
						ossLicenseText = ossLicenseText.substring(0, ossLicenseText.length()-1);
						bean.setOssLicenseText(ossLicenseText);
					}
				}
				
				// group by key 설정 grid 상에서 대소문자 구분되어 대문자로 모두 치화하여 그룹핑
				bean.setGroupKey(bean.getOssName().toUpperCase());
				
				// NICK NAME ICON 표시
				if (CoConstDef.FLAG_YES.equals(ossMaster.getSearchFlag())) {
					bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
					
//					if (!isEmpty(bean.getOssNickname())) {
//						bean.setOssName("<span class=\"badge badge-warning\">Nick</span>&nbsp;" + bean.getOssName());
//					}
				}

				List<OssMaster> ossDetectedLicense = ossMapper.selectOssDetectedLicenseList(bean);
				if (ossDetectedLicense != null && !ossDetectedLicense.isEmpty()) {
					StringBuilder sb = new StringBuilder(); // 초기화
					for (OssMaster licenseInfo : ossDetectedLicense) {
						sb.append(licenseInfo.getLicenseName()).append(",");
					}
					bean.setDetectedLicense(String.valueOf(sb));
				}
				
				String restriction = bean.getRestriction();
				if (!isEmpty(restriction)) {
					List<String> restrictionList = new ArrayList<>();
					for (String res : restriction.split(",")) {
						if (!isEmpty(res) && !restrictionList.contains(res)) {
							restrictionList.add(res);
						}
					}
					if (!restrictionList.isEmpty()) {
						bean.setRestriction(CommonFunction.setLicenseRestrictionListById(null, restrictionList.stream().distinct().collect(Collectors.joining(","))));
					} else {
						bean.setRestriction("");
					}
				} else {
					bean.setRestriction("");
				}
			}
		}
		
		map.put("page", ossMaster.getCurPage());
		map.put("total", ossMaster.getTotBlockSize());
		map.put("records", records);
		map.put("rows", newList);
		
		return map; 
	}
	
	@Override
	@Cacheable(value="autocompleteCache", key="#root.methodName")
	public List<Map<String, String>> getOssNameList() {
		List<OssMaster> ossNameList = ossMapper.selectOssNameList();
		List<Map<String, String>> ossNameMapList = ossNameList.stream().map(e -> {
														Map<String, String> map = new HashMap<>();
														map.put("ossName", e.getOssName());
														return map;
													}).collect(Collectors.toList());
		return ossNameMapList;
	}
	
	@Override
	public String[] getOssNickNameListByOssName(String ossName) {
		List<String> nickList = new ArrayList<>();
		
		if (!isEmpty(ossName)) {
			OssMaster param = new OssMaster();
			param.setOssName(ossName);
			List<OssMaster> list =  ossMapper.selectOssNicknameList(param);
			
			if (list != null) {
				for (OssMaster bean : list) {
					if (!isEmpty(bean.getOssNickname()) && !nickList.contains(bean.getOssNickname())) {
						nickList.add(bean.getOssNickname());
					}
				}
			}
		}
		
		return nickList.toArray(new String[nickList.size()]);
	}
	
	@Override
	public Map<String, Object> getOssLicenseList(OssMaster ossMaster) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<OssLicense> list = ossMapper.selectOssLicenseList(ossMaster);

		if (list != null) {
			for (OssLicense license : list) {
				if (!CommonFunction.isAdmin() && !isEmpty(license.getOssCopyright())) {
					license.setOssCopyright(CommonFunction.lineReplaceToBR( StringUtil.replaceHtmlEscape( license.getOssCopyright() )));
				}
				
				if (!isEmpty(license.getRestriction())) {
					List<String> restrictionList = Arrays.asList(license.getRestriction().split(","));
					String restrictionStr = "";
					for (String restriction : restrictionList) {
						if (isEmpty(restriction)) continue;

						if (!isEmpty(restrictionStr)) {
							restrictionStr += ",";
						}
						restrictionStr += CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, restriction);
					}
					if (!isEmpty(restrictionStr)) license.setRestriction(restrictionStr);
				}
			}
		}
		
		map.put("rows", list);
		
		return map;
	}
	
	@Override
	public List<Vulnerability> getOssVulnerabilityList(Vulnerability vulnParam) {
		return ossMapper.getOssVulnerabilityList(vulnParam);
	}
	
	@Override
	public OssMaster getOssMasterOne(OssMaster ossMaster) {
		ossMaster = ossMapper.selectOssOne(ossMaster);
		List<OssMaster> ossNicknameList = ossMapper.selectOssNicknameList(ossMaster);
		List<OssMaster> ossDownloadLocation = ossMapper.selectOssDownloadLocationList(ossMaster);
		List<OssLicense> ossLicenses = ossMapper.selectOssLicenseList(ossMaster); // declared License
		List<OssMaster> ossDetectedLicense = ossMapper.selectOssDetectedLicenseList(ossMaster); // detected License
		List<String> includeCpeList = ossMapper.selectOssIncludeCpeList(ossMaster);
		List<String> excludeCpeList = ossMapper.selectOssExcludeCpeList(ossMaster);
		List<String> ossVersionAliasList = ossMapper.selectOssVersionAliases(ossMaster);
		
		if (includeCpeList != null && !includeCpeList.isEmpty()) {
			ossMaster.setIncludeCpes(includeCpeList.toArray(new String[includeCpeList.size()]));
		}
		if (excludeCpeList != null && !excludeCpeList.isEmpty()) {
			ossMaster.setExcludeCpes(excludeCpeList.toArray(new String[excludeCpeList.size()]));
		}
		if (ossVersionAliasList != null && !ossVersionAliasList.isEmpty()) {
			ossMaster.setOssVersionAliases(ossVersionAliasList.toArray(new String[ossVersionAliasList.size()]));
		}
		
		String totLicenseTxt = CommonFunction.makeLicenseExpression(ossLicenses);
		ossMaster.setTotLicenseTxt(totLicenseTxt);
		
		StringBuilder sb = new StringBuilder();
		
		for (OssMaster ossNickname : ossNicknameList){
			sb.append(ossNickname.getOssNickname()).append(",");
		}
		
		String[] ossNicknames = new String(sb).split("[,]");
		
		sb = new StringBuilder(); // 초기화
		
		if (ossDownloadLocation != null && !ossDownloadLocation.isEmpty()) {
			for (OssMaster location : ossDownloadLocation) {
				if (!isEmpty(location.getPurl())) {
					sb.append(location.getDownloadLocation() + "|" + location.getPurl()).append(",");
				} else {
					String purl = getPurlByDownloadLocation(location);
					if (!isEmpty(purl)) {
						sb.append(location.getDownloadLocation() + "|" + purl).append(",");
					} else {
						sb.append(location.getDownloadLocation());
					}
				}
			}
		} else {
			if (!isEmpty(ossMaster.getDownloadLocation())) {
				String purl = getPurlByDownloadLocation(ossMaster);
				if (!isEmpty(purl)) {
					sb.append(ossMaster.getDownloadLocation() + "|" + purl).append(",");
				} else {
					sb.append(ossMaster.getDownloadLocation());
				}
			}
		}
		
		String[] ossDownloadLocations = new String(sb).split("[,]");
		
		sb = new StringBuilder(); // 초기화
		
		for (OssMaster licenseInfo : ossDetectedLicense) {
			sb.append(licenseInfo.getLicenseName()).append(",");
		}
		
		String[] detectedLicenses = new String(sb).split("[,]");
		
		ossMaster.setOssNicknames(ossNicknames);
		ossMaster.setDownloadLocations(ossDownloadLocations);
		ossMaster.setOssLicenses(ossLicenses);
		ossMaster.setDetectedLicenses(Arrays.asList(detectedLicenses));
		
		if (ossMaster.getRestriction() != null) {
			T2CodeDtl t2CodeDtl = new T2CodeDtl();
			List<T2CodeDtl> t2CodeDtlList = new ArrayList<>(); 
			t2CodeDtl.setCdNo(CoConstDef.CD_LICENSE_RESTRICTION);
			try {
				t2CodeDtlList = codeMapper.selectCodeDetailList(t2CodeDtl);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			List<String> restrictionList = Arrays.asList(ossMaster.getRestriction().split(","));
			List<String> restrictionCdNoList = new ArrayList<>();
			String restrictionStr = "";
			
			for (T2CodeDtl item: t2CodeDtlList){
				if (restrictionList.contains(item.getCdDtlNo())) {
					restrictionStr += (!isEmpty(restrictionStr) ? ", " : "") + item.getCdDtlNm();
					restrictionCdNoList.add(item.getCdDtlNo());
				}
			}
			
			ossMaster.setRestriction(restrictionStr);
			ossMaster.setRestrictionCdNoList(restrictionCdNoList);
		}
		
		return ossMaster;
	}
	
	@Override
	public Map<String, Object> getOssPopupList(OssMaster ossMaster) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		if (!isEmpty(ossMaster.getOssId()) && CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getOssId()) != null) {
			ossMaster.setOssName(CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getOssId()).getOssName());
			
			int records = ossMapper.selectOssPopupTotalCount(ossMaster);
			ossMaster.setTotListSize(records);
			List<OssMaster> list = ossMapper.selectOssPopupList(ossMaster);
			
			map.put("page", ossMaster.getCurPage());
			map.put("total", ossMaster.getTotBlockSize());
			map.put("records", records);
			map.put("rows", list);
		}
		
		return map;
	}
	
	@Override
	public OssMaster getOssInfo(String ossId, boolean isMailFormat) {
		return getOssInfo(ossId, null, isMailFormat);
	}
	
	@Override
	public OssMaster getOssInfo(String ossId, String ossName, boolean isMailFormat) {
		OssMaster param = new OssMaster();
		Map<String, OssMaster> map = new HashMap<String, OssMaster>();
		
		if (!isEmpty(ossId)) {
			param.addOssIdList(ossId);
			map = isMailFormat ? getBasicOssInfoListByIdOnTime(param) : getBasicOssInfoListById(param);
		}
		
		if (!isEmpty(ossName)) {
			param.setOssName(ossName);
			map = getNewestOssInfoOnTime(param);
		}
		
		if (map != null) {
			// nickname 정보 취득 
			for (OssMaster bean : map.values()) {
				param.setOssCommonId(bean.getOssCommonId());
				param.setOssName(bean.getOssName());
				
				List<OssMaster> nickNameList = ossMapper.selectOssNicknameList(param);
				if (nickNameList != null && !nickNameList.isEmpty()) {
					List<String> nickNames = new ArrayList<>();
					
					for (OssMaster nickNameBean : nickNameList) {
						if (!isEmpty(nickNameBean.getOssNickname())) {
							nickNames.add(nickNameBean.getOssNickname());
						}
					}
					
					bean.setOssNicknames(nickNames.toArray(new String[nickNames.size()]));
				}
				
				List<OssMaster> detectedLicenseList = ossMapper.selectOssDetectedLicenseList(bean);
				if (detectedLicenseList.size() > 0) {
					String detectedLicense = "";
					for (OssMaster ossBean : detectedLicenseList) {
						if (!isEmpty(detectedLicense)) {
							detectedLicense += ", ";
						}
						
						detectedLicense += ossBean.getLicenseName();
					}
					
					bean.addDetectedLicense(detectedLicense);
				}
				
				if (!isEmpty(bean.getDownloadLocation())) {
					bean.setDownloadLocations(bean.getDownloadLocation().split(","));
					if (isEmpty(bean.getPurl())) {
						StringBuilder sb = new StringBuilder();
						OssMaster ossMaster = new OssMaster();
						
						for (String downloadLocation : bean.getDownloadLocations()) {
							ossMaster.setDownloadLocation(downloadLocation);
							String purl = generatePurlByDownloadLocation(bean);
							sb.append(purl).append(",");
						}
						
						bean.setPurl(sb.toString());
					}
				}
				
				if (isMailFormat) {
					bean.setLicenseName(CommonFunction.makeLicenseExpression(bean.getOssLicenses()));
					bean.setOssLicenses(CommonFunction.changeLicenseNameToShort(bean.getOssLicenses()));
					
					// code 변경
					if (!isEmpty(bean.getLicenseDiv())) {
						// multi license 표시 여부 판단을 위해서 코드표시명 변환 이전의 값이 필요함
						bean.setMultiLicenseFlag(bean.getLicenseDiv());
						bean.setLicenseDiv(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_DIV, bean.getLicenseDiv()));
					}
					
					if (!isEmpty(bean.getLicenseType())) {
						bean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, bean.getLicenseType()));
					}
					
					if (!isEmpty(bean.getObligationType())) {
						bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, bean.getObligationType()));
					}
					
					// 날짜 형식
					if (!isEmpty(bean.getModifiedDate())) {
						bean.setModifiedDate(DateUtil.dateFormatConvert(bean.getModifiedDate(), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
					}
					
					if (!isEmpty(bean.getModifier())) {
						bean.setModifier(CoMailManager.getInstance().makeUserNameFormat(bean.getModifier()));
					}
					
					if (!isEmpty(bean.getCreatedDate())) {
						bean.setCreatedDate(DateUtil.dateFormatConvert(bean.getCreatedDate(), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
					}
					
					if (!isEmpty(bean.getCreator())) {
						bean.setCreator(CoMailManager.getInstance().makeUserNameFormat(bean.getCreator()));
					}

					bean.setAttribution(CommonFunction.lineReplaceToBR(bean.getAttribution()));
					bean.setSummaryDescription(CommonFunction.lineReplaceToBR(bean.getSummaryDescription()));
					bean.setCopyright(CommonFunction.lineReplaceToBR(bean.getCopyright()));
					bean.setImportantNotes(CommonFunction.lineReplaceToBR(bean.getImportantNotes()));
				}
				
				return bean;
			}
		}
		
		return null;
	}
	
	private Map<String, OssMaster> getBasicOssInfoListByIdOnTime(OssMaster ossMaster) {
		List<OssMaster> list = ossMapper.getBasicOssInfoListById(ossMaster);
		
		return makeBasicOssInfoMap(list, true, false);
	}
	
	private Map<String, OssMaster> makeBasicOssInfoMap(List<OssMaster> list, boolean useId, boolean useUpperKey) {
		Map<String, OssMaster> map = new HashMap<>();
		
		for (OssMaster bean : list) {
			OssMaster targetBean = null;
			String key = useId ? bean.getOssId() : bean.getOssName() +"_"+ avoidNull(bean.getOssVersion());
			
			if (useUpperKey) {
				key = key.toUpperCase();
			}
			
			if (map.containsKey(key)) {
				targetBean = map.get(key);
			} else {
				targetBean = bean;
			}
			
			OssLicense subBean = new OssLicense();
			subBean.setOssId(bean.getOssId());
			subBean.setLicenseId(bean.getLicenseId());
			subBean.setLicenseName(bean.getLicenseName());
			subBean.setLicenseType(bean.getLicenseType());
			subBean.setOssLicenseIdx(bean.getOssLicenseIdx());
			subBean.setOssLicenseComb(bean.getOssLicenseComb());
			subBean.setOssLicenseText(bean.getOssLicenseText());
			subBean.setOssCopyright(bean.getOssCopyright());
			
			// oss의 license type을 license의 license type 적용 이후에 set
			bean.setLicenseType(bean.getOssLicenseType());
			
			targetBean.addOssLicense(subBean);
			
			if (map.containsKey(key)) {
				map.replace(key, targetBean);
			} else {
				map.put(key, targetBean);
			}
		}
		
		return map;
	}
	
	@Override
	public History work(Object param) {
		History h = new History();
		OssMaster vo = (OssMaster) param;
		OssMaster data = getOssMasterOne(vo);
		data.setComment(vo.getComment());
		
		h.sethKey(vo.getOssId());
		h.sethTitle(vo.getOssName());
		h.sethType(CoConstDef.EVENT_CODE_OSS);
		h.setModifier(vo.getLoginUserName());
		h.setModifiedDate(vo.getCreatedDate());
		h.sethComment("");
		h.sethData(data);
		
		return h;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void deleteOssWithVersionMerege(OssMaster ossMaster) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		String chagedOssName = CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getNewOssId()).getOssName();
		String chagedOssCommonId = CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getNewOssId()).getOssCommonId();
		String beforOssName = CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getOssId()).getOssName();
		String beforOssCommonId = CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getOssId()).getOssCommonId();

		// 동일한 oss에서 이동하는 경우, nick name을 별도로 등록하지 않음
		OssMaster beforeBean = getOssInfo(ossMaster.getOssId(), false);
		
		// 삭제대상 OSS 목록 취득
		Map<String, Object> rowMap = ossMergeCheckList(ossMaster);
		List<OssMaster> rowList = (List<OssMaster>) rowMap.get("rows");
		
		List<Map<String, OssMaster>> mailBeanList = new ArrayList<>();
		
		// 메일 발송을 위한 data 취득( 메일 형식을 위해 다시 DB Select )
		for (OssMaster bean : rowList) {
			if (!isEmpty(bean.getMergeStr())) {
				// 실제로 삭제처리는 중복되는 OSS Version만
				if ("Duplicated".equalsIgnoreCase(bean.getMergeStr())) {
					// mail 발송을 위해 삭제전 data 취득
					Map<String, OssMaster> mailDiffMap = new HashMap<>();
					OssMaster tempBean1 = getOssInfo(bean.getDelOssId(), true);
					
					List<String> ossNickNameList = new ArrayList<String>();

					if (tempBean1.getOssNicknames() != null) {
						tempBean1.setOssNickname(CommonFunction.arrayToString(tempBean1.getOssNicknames(), "<br>"));

						for (String nickName : Arrays.asList(tempBean1.getOssNicknames())) {
							ossNickNameList.add(nickName);
						}
					}
					
					tempBean1.setOssId(bean.getDelOssId());
					mailDiffMap.put("before", tempBean1);
					
					OssMaster tempBean2 = getOssInfo(bean.getOssId(), true);
					
					if (tempBean2.getOssNicknames() != null) {
						for (String nickName : Arrays.asList(tempBean2.getOssNicknames())){
							ossNickNameList.add(nickName);
						}
					}
					
					ossNickNameList.add(beforOssName);
					tempBean2.setOssNickname(CommonFunction.arrayToString(ossNickNameList.toArray(new String[ossNickNameList.size()]), "<br>"));
					tempBean2.setOssNicknames(ossNickNameList.toArray(new String[ossNickNameList.size()]));
					tempBean2.setOssName(chagedOssName);
					mailDiffMap.put("after", tempBean2);
					
					mailBeanList.add(mailDiffMap);
				} else {
					// 실제로 삭제되는 것은 아님
					// 이름만 변경해서 비교 메일 발송
					Map<String, OssMaster> mailDiffMap = new HashMap<>();
					OssMaster tempBean1 = getOssInfo(bean.getOssId(), true);
					
					if (tempBean1.getOssNicknames() != null) {
						tempBean1.setOssNickname(CommonFunction.arrayToString(tempBean1.getOssNicknames(), "<br>"));
					}
					
					tempBean1.setOssId(bean.getOssId());
					mailDiffMap.put("before", tempBean1);

					List<String> ossNickNameList = new ArrayList<String>();

					OssMaster tempBean2 = (OssMaster) CommonFunction.copyObject(tempBean1, "OM");
					OssMaster beforeBean1 = getOssInfo(ossMaster.getNewOssId(), false);
					
					if (beforeBean1.getOssNicknames() != null) {
						for (String nickName : Arrays.asList(beforeBean1.getOssNicknames())) {
							ossNickNameList.add(nickName);
						}
					}

					if (tempBean2.getOssNicknames() != null) {
						for (String nickName : Arrays.asList(tempBean2.getOssNicknames())){
							ossNickNameList.add(nickName);
						}
					}
					
					ossNickNameList.add(beforOssName);
					tempBean2.setOssNickname(CommonFunction.arrayToString(ossNickNameList.toArray(new String[ossNickNameList.size()]), "<br>"));
					tempBean2.setOssNicknames(ossNickNameList.toArray(new String[ossNickNameList.size()]));
					tempBean2.setOssName(chagedOssName);
					mailDiffMap.put("after", tempBean2);
					
					mailBeanList.add(mailDiffMap);
				}
			}
		}

		// 삭제 처리
		for (OssMaster bean : rowList) {
			if (!isEmpty(bean.getMergeStr())) {
				boolean isDel = false;
				
				OssMaster mergeBean = new OssMaster();
				History h = null;
				
				// 신규 version의 등록이 필요한 경우
				if ("Added".equalsIgnoreCase(bean.getMergeStr())) {
					CommentsHistory historyBean = new CommentsHistory();
					historyBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_OSS);
					historyBean.setReferenceId(bean.getOssId());
					historyBean.setContents("OSS 일괄 이관 처리에 의해 OSS Name이 변경되었습니다. <br/>" + "Before OSS Name : " + bean.getOssName() + "<br/>" + avoidNull(ossMaster.getComment()));
//					bean.setOssName(chagedOssName);
//					bean.setNewOssId(bean.getOssId()); // 삭제하지 않고 이름만 변경해서 재사용한다.
					bean.setOssCommonId(chagedOssCommonId);
					
//					ossMapper.changeOssNameByDelete(bean);
					ossMapper.changeOssCommonNameByDelete(bean);
					
					// Version Flag Setting
					updateLicenseDivDetail(bean);
					
					commentService.registComment(historyBean);

					mergeBean.setMergeOssId(bean.getOssId());
				} else {
					// Duplicated
					bean.setNewOssId(bean.getOssId());
					bean.setOssId(bean.getDelOssId());
					
					h = work(bean);
					
					CommentsHistory historyBean = new CommentsHistory();
					historyBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_OSS);
					historyBean.setReferenceId(bean.getDelOssId());
					historyBean.setContents("OSS 일괄 이관 처리에 의해 "+bean.getOssName()+" 으로 이관되었습니다.<br/>" + avoidNull(ossMaster.getComment()));
					
					commentService.registComment(historyBean);
					
					historyBean.setReferenceId(bean.getOssId());
					historyBean.setContents("OSS 일괄 이관 처리에 의해 "+beforOssName+" 과 병합되었습니다.<br/>" + avoidNull(ossMaster.getComment()));
					
					commentService.registComment(historyBean);
					
					ossMapper.deleteOssLicenseFlag(bean.getOssId());
					ossMapper.deleteOssLicense(bean);
					ossMapper.deleteOssMaster(bean);
					
					mergeBean.setMergeOssId(bean.getNewOssId());

					isDel = true;
				}
				
				//1. 기존 OssId를 사용중인 프로젝트의 OssId , Version 를 새로운 OssId로 교체				
				if (isDel && h != null) {
					try{
						h.sethAction(CoConstDef.ACTION_CODE_DELETE);
						historyService.storeData(h);
					}catch(Exception e){
						log.error(e.getMessage(), e);
					}
				}

				mergeBean.setOssId(bean.getOssId());
				mergeBean.setOssName(beforeBean.getOssName());
				mergeBean.setMergeOssName(chagedOssName);
				mergeBean.setOssVersion(bean.getOssVersion());
				mergeBean.setMergeOssVersion(bean.getOssVersion());
				mergeBean.setRegistMergeFlag("N");
				ossNameMerge(mergeBean, chagedOssName, beforOssName);
			}
		}
		
		OssMaster deleteParam = new OssMaster();
//		deleteNickParam.setOssName(beforOssName);
		deleteParam.setOssCommonId(beforOssCommonId);
		
		ossMapper.deleteOssNickname(deleteParam);
		
		// nick name merge
		// 일단 삭제된 oss name을 nickname으로 추가한다.
		OssMaster mergeParam  = new OssMaster();
//		nickMergeParam.setOssName(chagedOssName);
		mergeParam.setOssCommonId(chagedOssCommonId);
		mergeParam.setOssNickname(beforOssName);
		ossMapper.mergeOssNickname2(mergeParam);
		
		if (beforeBean.getOssNicknames() != null) {
			for (String nickName : beforeBean.getOssNicknames()) {
				mergeParam.setOssNickname(nickName);
				
				ossMapper.mergeOssNickname2(mergeParam);
			}
		}
		
		// download location merge
		if (beforeBean.getDownloadLocations() != null) {
			List<String> mergeDownloadLocationList = new ArrayList<>();
			for (String downloadLocation : beforeBean.getDownloadLocations()) {
				mergeDownloadLocationList.add(downloadLocation);
			}
			
			int ossDlIdx = 1;
			
			List<OssMaster> afterBeanList = ossMapper.selectOssDownloadLocationList(mergeParam);
			if (afterBeanList != null && !afterBeanList.isEmpty()) {
				ossDlIdx += afterBeanList.size();
				List<String> afterDownloadLocationList = afterBeanList.stream().map(e -> e.getDownloadLocation()).collect(Collectors.toList());
				mergeDownloadLocationList.removeAll(afterDownloadLocationList);
			}
			
			if (mergeDownloadLocationList != null && !mergeDownloadLocationList.isEmpty()) {
				for (String mergeDownloadLocation : mergeDownloadLocationList) {
					mergeParam.setDownloadLocation(mergeDownloadLocation);
					String purl = generatePurlByDownloadLocation(mergeParam);
					mergeParam.setPurl(purl);
					mergeParam.setOssDlIdx(ossDlIdx);
					ossMapper.insertOssDownloadLocation(mergeParam);
					ossDlIdx++;
				}
			}
		}
		
		ossMapper.deleteOssDownloadLocation(deleteParam);
		ossMapper.deleteOssCommonMaster(deleteParam);
		
		CoCodeManager.getInstance().refreshOssInfo();

		for (Map<String, OssMaster> mailInfoMap : mailBeanList) {
			// 삭제대상 OSS를 사용중인 프로젝트의 목록을 코멘트로 남긴다.
			try {
				String templateComemnt = makeTemplateComment(ossMaster.getComment(), mailInfoMap.get("before"), mailInfoMap.get("after"));
				
				// 사용중인 프로젝트가 없는 경우는 코멘트를 추가작으로 남기지 않는다.
				if (!isEmpty(templateComemnt)) {
					CommentsHistory historyBean = new CommentsHistory();
					historyBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_OSS);
					historyBean.setReferenceId(mailInfoMap.get("after").getOssId());
					historyBean.setContents(templateComemnt);
					
					commentService.registComment(historyBean);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			try {
				CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_OSS_RENAME);
				mailBean.setCompareDataBefore(mailInfoMap.get("before"));
				
				OssMaster afterOssMaster = (OssMaster) mailInfoMap.get("after");
				afterOssMaster.setModifiedDate(DateUtil.getCurrentDateTime());
				afterOssMaster.setModifier(CoMailManager.getInstance().makeUserNameFormat(loginUserName()));
				
				mailBean.setCompareDataAfter(afterOssMaster);
				mailBean.setParamOssId(mailInfoMap.get("before").getOssId());
				mailBean.setComment(ossMaster.getComment());
				
				CoMailManager.getInstance().sendMail(mailBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}	
		}
	}
	
	private void ossNameMerge(OssMaster ossMaster, String changedOssName, String beforeOssName) {
		List<String> changeOssInfoList = new ArrayList<>();
		String before = beforeOssName + " (" + avoidNull(ossMaster.getOssVersion(), "N/A") + ")";
		String after = changedOssName + " (" + avoidNull(ossMaster.getMergeOssVersion(), "N/A") + ")";
		changeOssInfoList.add(before + "|" + after);
		String contents = CommonFunction.changeDataToTableFormat("oss", CommonFunction.getCustomMessage("msg.common.change.name", "OSS Name"), changeOssInfoList);

		// 3rdParty == 'CONF'
		List<PartnerMaster> confirmPartnerList = ossMapper.getOssNameMergePartnerList(ossMaster);

		if (confirmPartnerList.size() > 0) {
			ossMaster.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
			ossMapper.mergeOssName(ossMaster);

			for (PartnerMaster pm : confirmPartnerList) {
				// partner Comment Regist
				CommentsHistory historyBean = new CommentsHistory();
				historyBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_HIS);
				historyBean.setReferenceId(pm.getPartnerId());
				historyBean.setStatus("OSS Name Changed");
				historyBean.setContents(contents);

				commentService.registComment(historyBean);
			}
		}

		// Identification == 'CONF', verification
		List<Project> confirmProjectList = ossMapper.getOssNameMergeProjectList(ossMaster);

		if (confirmProjectList.size() > 0) {
			ossMaster.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
			ossMapper.mergeOssName(ossMaster);

			for (Project prj : confirmProjectList) {
				// Project > Identification comment regist
				CommentsHistory historyBean = new CommentsHistory();
				historyBean.setReferenceId(prj.getPrjId());
				historyBean.setStatus("OSS Name Changed");
				historyBean.setContents(contents);
				historyBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
				commentService.registComment(historyBean);
			}
		}
	}

	private String makeTemplateComment(String comment, OssMaster ossMasterBefore, OssMaster ossMasterAfter) {
		Map<String, Object> convertDataMap = new HashMap<>();
		// 사용중인 프로젝트가 있는 경우
		// 메일 발송시 사용사는 쿼리와 동일
		List<Project> prjList = ossMapper.getOssChangeForUserList(ossMasterBefore);
		
		if (prjList != null && !prjList.isEmpty()) {
			for (Project prjBean : prjList) {
				prjBean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, prjBean.getDistributionType()));
				prjBean.setCreator(makeUserNameFormatWithDivision(prjBean.getCreator()));
				prjBean.setCreatedDate(CommonFunction.formatDateSimple(prjBean.getCreatedDate()));
				prjBean.setReviewer(makeUserNameFormatWithDivision(prjBean.getReviewer()));
			}
			
			convertDataMap.put("projectList", prjList);
			convertDataMap.put("comment", comment);
			convertDataMap.put("modifierNm", makeUserNameFormat(loginUserName()));
			convertDataMap.put("ossBeforeNm", makeOssNameFormat(ossMasterBefore));
			convertDataMap.put("ossAftereNm", makeOssNameFormat(ossMasterAfter));
			convertDataMap.put("templateURL", "/comment/ossRenamed.html");
			
			return CommonFunction.VelocityTemplateToString(convertDataMap);
		}
		
		return null;
	}
	
	private String makeUserNameFormatWithDivision(String userId) {
		String rtnVal = "";
		T2Users userParam = new T2Users();
		userParam.setUserId(userId);
		
		T2Users userInfo = userMapper.getUser(userParam);
		
		if (userInfo != null && !isEmpty(userInfo.getUserName())) {
			if (!isEmpty(userInfo.getDivision())) {
				String _division = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, userInfo.getDivision());
				
				if (!isEmpty(_division)) {
					rtnVal += _division + " ";
				}
			}
			
			rtnVal += userInfo.getUserName() + "(" + userId + ")";
		}
		
		return isEmpty(rtnVal) ? userId : rtnVal;
	}
	
	private String makeUserNameFormat(String userId) {
		String rtn = userId;
		T2Users param = new T2Users();
		param.setUserId(userId);
		T2Users userInfo = userMapper.getUser(param);
		
		if (userInfo != null) {
			rtn = avoidNull(userInfo.getUserName());
			rtn += "(" + avoidNull(userInfo.getUserId()) + ")";
		}
		
		return rtn;
	}
	
	private String makeOssNameFormat(OssMaster bean) {
		String rtnVal = "";
		
		if (bean != null) {
			rtnVal = avoidNull(bean.getOssName());
			if (!isEmpty(bean.getOssVersion())) {
				rtnVal += " (" + bean.getOssVersion() + ")";
			}
		}
		
		return rtnVal;
	}
	
	@Override
	public String[] checkNickNameRegOss(String ossName, String[] ossNicknames) {
		List<String> rtnList = new ArrayList<>();
		List<String> currntList = null;
		
		if (ossNicknames != null && ossNicknames.length > 0) {
			currntList = Arrays.asList(ossNicknames);
		}
		
		if (currntList == null) {
			currntList = new ArrayList<>();
		}
		
		if (!isEmpty(ossName)) {
			// oss name으로 등록된 nick name이 있는지 확인
			List<String> _nickNames = ossMapper.checkNickNameRegOss(ossName);
			
			if (_nickNames != null && !_nickNames.isEmpty()) {
				for (String s : _nickNames) {
					if (!isEmpty(s) && !currntList.contains(s)) {
						rtnList.add(s);
					}
				}
			}
		}
		
		return rtnList.toArray(new String[rtnList.size()]);
	}

	@Override
	public String checkExistOssConf(String ossId) {
		int resultCnt = 0;
		
		boolean projectFlag = CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES);
        boolean partnerFlag = CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES);
        
		if (projectFlag) {
			resultCnt += ossMapper.checkExistOssConfProject(CoCodeManager.OSS_INFO_BY_ID.get(ossId));
		}
		
		if (partnerFlag) {
			resultCnt += ossMapper.checkExistOssConfPartner(CoCodeManager.OSS_INFO_BY_ID.get(ossId));
		}
		
		return Integer.toString(resultCnt);
		
	}

	@Transactional
	@Override
//	@CacheEvict(value="autocompleteCache", allEntries=true)
	public String registOssMaster(OssMaster ossMaster) {
		try {
			if (isEmpty(ossMaster.getOssCommonId())) {
				OssMaster bean = ossMapper.checkExistsOssname(ossMaster);
				if (bean != null) {
					ossMaster.setOssCommonId(bean.getOssCommonId());
				}
			}
			
			String[] ossNicknames = ossMaster.getOssNicknames();
			String ossId = ossMaster.getOssId();
			boolean isNew = StringUtil.isEmpty(ossId);
			OssMaster orgMasterInfo = null;

			if (StringUtil.isEmpty(ossId)){
				ossMaster.setCreator(ossMaster.getLoginUserName());
			} else {
				orgMasterInfo = new OssMaster();
				orgMasterInfo.setOssId(ossId);
				orgMasterInfo = getOssMasterOne(orgMasterInfo);
			}
			
			// oss name 또는 version이 변경된 경우만 vulnerability recheck 대상으로 업데이트 한다.
			boolean vulnRecheck = false;
			OssMaster beforeOssInfo = null;
			
			// 변경전 oss name에 해당하는 oss_id 목록을 찾는다.
			if (!isNew) {
				OssMaster _orgBean = getOssInfo(ossId, false);
				
				if (_orgBean != null && !isEmpty(_orgBean.getOssName())) {
					if (!avoidNull(ossMaster.getOssName()).trim().equalsIgnoreCase(_orgBean.getOssName()) 
							|| !avoidNull(ossMaster.getOssVersion()).trim().equalsIgnoreCase(avoidNull(_orgBean.getOssVersion()).trim())) {
						vulnRecheck = true;
					}
					if (!avoidNull(ossMaster.getOssName()).trim().equalsIgnoreCase(_orgBean.getOssName())) {
						// 변경 전 oss name 에 등록된 nickname 삭제
						_orgBean.setOssId(null);
						List<OssMaster> beforeOssNameList = ossMapper.getOssListByName(_orgBean);
						if (beforeOssNameList != null) {
							int ossIdCnt = beforeOssNameList.stream().map(e -> e.getOssId()).distinct().collect(Collectors.toList()).size();
							if (ossIdCnt == 1) {
								ossMapper.deleteOssNickname(_orgBean);
							}
							
							List<OssMaster> filteredBeforeOssInfoList = beforeOssNameList.stream().filter(e -> !e.getOssId().equals(ossMaster.getOssId())).collect(Collectors.toList());
							if (filteredBeforeOssInfoList != null && !filteredBeforeOssInfoList.isEmpty()) {
								beforeOssInfo = filteredBeforeOssInfoList.get(0);
							}
						}
					}
				}
			}
			
			if (vulnRecheck) {
				ossMaster.setVulnRecheck(CoConstDef.FLAG_YES);
			}
			if (!isEmpty(ossMaster.getCopyright())) {
				ossMaster.setCopyright(StringUtils.trimWhitespace(ossMaster.getCopyright()));
			}
			
			ossMaster.setModifier(ossMaster.getLoginUserName());
			
			// trim처리
			ossMaster.setOssName(avoidNull(ossMaster.getOssName()).trim());
			
			checkOssLicenseAndObligation(ossMaster);
			
			ossMapper.insertCommonOssMaster(ossMaster);
			ossMapper.insertOssMaster(ossMaster);
			ossMapper.deleteOssLicense(ossMaster); // Declared, Detected License Delete 처리
			
			// v-Diff 체크를 위해 license list를 생성
			List<OssLicense> list = ossMaster.getOssLicenses();
			int ossLicenseDeclaredIdx = 0;
			String licenseId = ""; 
			
			for (OssLicense license : list) {
				ossLicenseDeclaredIdx++;
				licenseId = CommonFunction.getLicenseIdByName(license.getLicenseName());
				
				OssMaster om = new OssMaster(
					  Integer.toString(ossLicenseDeclaredIdx)
					, ossMaster.getOssId()
					, licenseId
					, license.getLicenseName()
					, ossLicenseDeclaredIdx == 1 ? "" : license.getOssLicenseComb()//ossLicenseIdx가 1일때 Comb 입력안함
//					, license.getOssLicenseText()
					, license.getOssCopyright()
					, ossMaster.getLicenseDiv()
				);
				
				ossMapper.insertOssLicenseDeclared(om);
			}
			
			// Detected License Insert / 20210806_Distinct Add
			List<String> detectedLicenses = ossMaster.getDetectedLicenses().stream().distinct().collect(Collectors.toList());
			
			if (detectedLicenses != null) {
				int ossLicenseDetectedIdx = 0;
				
				for (String detectedLicense : detectedLicenses) {			
					if (!isEmpty(detectedLicense)) {
						LicenseMaster detectedLicenseInfo = CoCodeManager.LICENSE_INFO_UPPER.get(detectedLicense.toUpperCase().trim());
						
						if (detectedLicenseInfo != null) {
							OssMaster om = new OssMaster(
									  ossMaster.getOssId() // ossId
									, detectedLicenseInfo.getLicenseId() // licenseId
									, Integer.toString(++ossLicenseDetectedIdx) // ossLicenseIdx
							);
							
							ossMapper.insertOssLicenseDetected(om);
						}
					}
				}
			}
			
			if (ossMaster.getOssLicenses() != null) {
				for (OssLicense license : ossMaster.getOssLicenses()) {
					// v-Diff check를 위해 만약 license id가 param Bean에 없는 경우, license id를 등록한다.
					if (isEmpty(license.getLicenseId())) {
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
							license.setLicenseId(CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase()).getLicenseId());
						}
					}
				}
			}
			
			/*
			 * 1. 라이센스 닉네임 삭제 
			 * 2. 라이센스 닉네임 재등록
			 */
			if (CoConstDef.FLAG_YES.equals(ossMaster.getAddNicknameYn())) { //nickname을 clear&insert 하지 않고, 중복제거를 한 나머지 nickname에 대해서는 add함.
				if (ossNicknames != null){
					List<OssMaster> ossNicknameList = ossMapper.selectOssNicknameList(ossMaster);
					
					for (String nickName : ossNicknames){
						if (!isEmpty(nickName)) {
							int duplicateCnt = ossNicknameList.stream().filter(o -> nickName.toUpperCase().equals(o.getOssNickname().toUpperCase())).collect(Collectors.toList()).size();
							
							if (duplicateCnt == 0) {
								OssMaster ossBean = new OssMaster();
//								ossBean.setOssName(ossMaster.getOssName());
								ossBean.setOssCommonId(ossMaster.getOssCommonId());
								ossBean.setOssNickname(nickName.trim());
								
								ossMapper.insertOssNickname(ossBean);
							}
						}
					}
				}
			} else { // nickname => clear&insert
				if (ossNicknames != null) {
					ossMapper.deleteOssNickname(ossMaster);
					
					for (String nickName : ossNicknames){
						if (!isEmpty(nickName)) {
							ossMaster.setOssNickname(nickName.trim());
							ossMapper.insertOssNickname(ossMaster);
						}
					}
				} else {
					ossMapper.deleteOssNickname(ossMaster);
				}
			}
			
			//코멘트 등록
			if (!isEmpty(avoidNull(ossMaster.getComment()).trim())) {
				CommentsHistory param = new CommentsHistory();
				param.setReferenceId(ossMaster.getOssId());
				param.setReferenceDiv(avoidNull(ossMaster.getReferenceDiv(), CoConstDef.CD_DTL_COMMENT_OSS));
				param.setContents(ossMaster.getComment());
				
				commentService.registComment(param);
			}
			
			if (isNew || vulnRecheck) {
				List<String> ossNameArr = new ArrayList<>();
				ossNameArr.add(ossMaster.getOssName().trim());
				
				if (ossMaster.getOssName().contains(" ")) {
					ossNameArr.add(ossMaster.getOssName().replaceAll(" ", "_"));
				}
				
				if (ossNicknames != null) {
					for (String s : ossNicknames) {
						if (!isEmpty(s)) {
							ossNameArr.add(s.trim());
						}
					}
				}
				
				OssMaster nvdParam = new OssMaster();
				nvdParam.setOssNames(ossNameArr.toArray(new String[ossNameArr.size()]));
				OssMaster nvdData = null;
				
				if (!isEmpty(ossMaster.getOssVersion())) {
					nvdParam.setOssVersion(ossMaster.getOssVersion());
					nvdData = ossMapper.getNvdDataByOssName(nvdParam);
				} else {
					nvdData = ossMapper.getNvdDataByOssNameWithoutVer(nvdParam);
				}
				
				if (nvdData != null) {
					ossMaster.setCvssScore(nvdData.getCvssScore());
					ossMaster.setCveId(nvdData.getCveId());
					ossMaster.setVulnDate(nvdData.getVulnDate());
					ossMaster.setVulnYn(CoConstDef.FLAG_YES);
					
					ossMapper.updateNvdData(ossMaster);
				} else if (!isNew) {
					OssMaster _orgBean = getOssInfo(ossId, false);
					if (_orgBean != null && (CoConstDef.FLAG_YES.equals(_orgBean.getVulnYn()) || !isEmpty(_orgBean.getCvssScore()))) {
						ossMaster.setVulnYn(CoConstDef.FLAG_NO);
						
						ossMapper.updateNvdData(ossMaster);
					}
				}
			}
			
			// Version Flag Setting
			updateLicenseDivDetail(ossMaster);
			if (beforeOssInfo != null) {
				updateLicenseDivDetail(beforeOssInfo);
			}
			
			// download location이 여러건일 경우를 대비해 table을 별도로 관리함.
			registOssDownloadLocation(ossMaster);
			
			// oss version alias
			registOssVersionAlias(ossMaster);
			
			// include Cpe, exclude Cpe 
			registCpeInfo(ossMaster);
			
			// Deactivate Flag Setting
			if (isEmpty(ossMaster.getDeactivateFlag())) {
				ossMaster.setDeactivateFlag(CoConstDef.FLAG_NO);
			}

			ossMapper.setDeactivateFlag(ossMaster);
			
			// updated oss info, oss components > ossName update
			if (!isNew && orgMasterInfo != null){
				if (!orgMasterInfo.getOssName().equals(ossMaster.getOssName())) {
					// oss name merge
					ossMaster.setRegistMergeFlag("Y");
					ossMaster.setMergeOssName(ossMaster.getOssName());
					ossMaster.setOssName(orgMasterInfo.getOssName());
					ossMaster.setMergeOssVersion(ossMaster.getOssVersion());
					ossMaster.setOssVersion(orgMasterInfo.getOssVersion());
					ossNameMerge(ossMaster, ossMaster.getMergeOssName(), orgMasterInfo.getOssName());
				}
			}
		} catch (RuntimeException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		} catch (Exception ex2) {
			log.error(ex2.getMessage(), ex2);
		} 
		
		return ossMaster.getOssId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registOssDownloadLocation(OssMaster ossMaster) {
		if (ossMapper.existsOssDownloadLocation(ossMaster) > 0){
			ossMapper.deleteOssDownloadLocation(ossMaster);
		}
		
		String purlJsonString = ossMaster.getPurlJson();
		Map<String, String> purlMap = null;
		if (!isEmpty(purlJsonString)) {
			Type collectionType = new TypeToken<Map<String, String>>() {}.getType();
			purlMap = (Map<String, String>) fromJson(purlJsonString, collectionType);
		}
		
		int idx = 0;
		
		String[] downloadLocations = ossMaster.getDownloadLocations();
		
		OssMaster master = new OssMaster();
		
		if (downloadLocations != null){
			List<String> purls = new ArrayList<>();
			
			for (String url : downloadLocations){
				if (!isEmpty(url)){ // 공백의 downloadLocation은 save하지 않음.
					master.setOssCommonId(ossMaster.getOssCommonId());
					master.setDownloadLocation(url);
					master.setOssDlIdx(++idx);
					
					String purlString = "";
					if (purlMap != null) {
						if (purlMap.containsKey(url)) {
							purlString = purlMap.get(url);
						} else {
							purlString = generatePurlByDownloadLocation(master);
						}
					} else {
						purlString = generatePurlByDownloadLocation(master);
					}
					
					if (isEmpty(purlString)) {
						purlString = generatePurlByDownloadLocation(master);
					}
					
					purls.add(purlString);
					master.setPurl(purlString);
					ossMapper.insertOssDownloadLocation(master);
				}
			}
			
			if (!purls.isEmpty()) {
				ossMaster.setPurl(String.join(",", purls));
			}
		}
	}
	
	@Override
	public Map<String, Object> ossMergeCheckList(OssMaster ossMaster) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<OssMaster> list1 = ossMapper.getBasicOssListByName(CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getOssId()).getOssName());
		List<OssMaster> list2 = ossMapper.getBasicOssListByName(CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getNewOssId()).getOssName());
		
		Map<String, OssMaster> mergeMap = new HashMap<>();
		
		// 이관 대상 OSS 정보를 먼저 격납한다.
		for (OssMaster bean : list2) {
			bean.setLicenseName(CommonFunction.makeLicenseExpression(CoCodeManager.OSS_INFO_BY_ID.get(bean.getOssId()).getOssLicenses()));
			bean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, bean.getLicenseType()));
			bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, bean.getObligationType()));
			bean.setMergeStr("");
			mergeMap.put(avoidNull(bean.getOssVersion(), "N/A").toUpperCase(), bean);
		}
		
		// 삭제 대상 OSS Version이 이관 대상 OSS 에 존재하는지 확인 
		for (OssMaster bean : list1) {
			bean.setLicenseName(CommonFunction.makeLicenseExpression(CoCodeManager.OSS_INFO_BY_ID.get(bean.getOssId()).getOssLicenses()));
			bean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, bean.getLicenseType()));
			bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, bean.getObligationType()));
			
			String verKey = avoidNull(bean.getOssVersion(), "N/A").toUpperCase();
			
			// 이미 존재한다면
			if (mergeMap.containsKey(verKey)) {
				mergeMap.get(verKey).setMergeStr("Duplicated");
				mergeMap.get(verKey).setDelOssId(bean.getOssId());
			} else {
				bean.setMergeStr("Added");
				mergeMap.put(verKey, bean);
			}
		}
		
		Map<String, OssMaster> treeMap = new TreeMap<>(mergeMap);
		
		map.put("rows", new ArrayList<>(treeMap.values()));
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
//	@CacheEvict(value="autocompleteCache", allEntries=true)
	public void deleteOssMaster(OssMaster ossMaster) {

		OssMaster beforeBean = getOssInfo(ossMaster.getOssId(), false);
		
		if (isEmpty(ossMaster.getOssName())) {
			ossMaster.setOssName(beforeBean.getOssName());
		}
		
		boolean ossCommonDeleteFlag = false;
		
		// 바로 삭제 일 경우( identification 상태가 conf인 프로젝트가 없을시 )
		if (CoConstDef.FLAG_NO.equals(avoidNull(ossMaster.getNewOssId(), CoConstDef.FLAG_NO))) {
			//3. 기존의 Oss 삭제		
			//ossMapper.deleteOssNickname(ossMaster);
			
			// 닉네임은 이름으로 매핑되기 때문에, 삭제후에 신규 추가시 자동으로 설정되는 문제가 있음
			// 삭제하는 oss 이름으로 공유하는 닉네임이 더이상 없을 경우, 닉네임도 삭제하도록 추가
			if (ossMapper.checkHasAnotherVersion(ossMaster) == 0) {
				ossCommonDeleteFlag = true;
				ossMapper.deleteOssNickname(ossMaster);
			}
			
			ossMapper.deleteOssLicense(ossMaster);
			ossMapper.deleteOssLicenseDeclaredSync(ossMaster);
			ossMapper.deleteOssLicenseDetectedSync(ossMaster);
			
			updateLicenseDivDetail(ossMaster);
			
			if (ossCommonDeleteFlag) {
				ossMapper.deleteOssDownloadLocation(ossMaster);
			}
			ossMapper.deleteOssMaster(ossMaster);
			if (ossCommonDeleteFlag) {
				ossMapper.deleteOssCommonMaster(ossMaster);
			}
		} else {
			// 동일한 oss에서 이동하는 경우, nick name을 별도로 등록하지 않음
			OssMaster afterBean = getOssInfo(ossMaster.getNewOssId(), false);
			
			if (!beforeBean.getOssName().toUpperCase().equals(afterBean.getOssName().toUpperCase())) {
				//2. 기존 Oss 의 Name 과 Nickname을 현재 선택한 Oss의 Nickname 에 병합
				ArrayList<String> nickNamesArray = new ArrayList<>();
				Map<String, Object> map = ossMapper.selectOssNameMap(ossMaster);
				String ossName = (String)map.get("ossName");
				List<Map<String, String>> list = (List<Map<String, String>>) map.get("nicknameList");
				
				nickNamesArray.add(ossName);
				
				for (Map<String, String> nickMap : list){
					nickNamesArray.addAll(new ArrayList<String>(nickMap.values()));
				}
				
				for (String nickname : nickNamesArray){
					ossMaster.setOssNickname(nickname);
					ossMapper.mergeOssNickname(ossMaster);
				}
			}
			
			if (ossMapper.checkHasAnotherVersion(ossMaster) == 0) {
				ossCommonDeleteFlag = true;
				ossMapper.deleteOssNickname(ossMaster);
			}
			
			ossMapper.deleteOssLicense(ossMaster);
			ossMapper.deleteOssLicenseDeclaredSync(ossMaster);
			ossMapper.deleteOssLicenseDetectedSync(ossMaster);
			ossMapper.deleteOssLicenseFlag(ossMaster.getOssId());
			
			updateLicenseDivDetail(ossMaster);
			
			if (ossCommonDeleteFlag) {
				ossMapper.deleteOssDownloadLocation(ossMaster);
			}
			ossMapper.deleteOssMaster(ossMaster);
			if (ossCommonDeleteFlag) {
				ossMapper.deleteOssCommonMaster(ossMaster);
			}
		}
	}

	@Override
	public OssMaster checkExistsOss(OssMaster param) {
		OssMaster bean = ossMapper.checkExistsOss(param);
		
		if (bean != null && !isEmpty(bean.getOssId())) {
			return bean;
		}
		
		return null;
	}

	@Override
	public OssMaster checkExistsOssNickname(OssMaster param) {
		OssMaster bean1 = ossMapper.checkExistsOssname(param);
		
		if (bean1 != null && !isEmpty(bean1.getOssId())) {
			return bean1;
		}
		
		OssMaster bean2 = ossMapper.checkExistsOssNickname(param);
		
		if (bean2 != null && !isEmpty(bean2.getOssId())) {
			return bean2;
		}
		
		if (!isEmpty(param.getOssId())) {
			OssMaster bean3 = ossMapper.checkExistsOssNickname2(param);
			
			if (bean3 != null) {
				return bean3;
			}
			
		}
		
		return null;
	}

	@Override
	public OssMaster checkExistsOssNickname2(OssMaster param) {
		OssMaster bean2 = ossMapper.checkExistsOssNickname(param);
		
		if (bean2 != null && !isEmpty(bean2.getOssId())) {
			return bean2;
		}
		
		return null;
	}

	@Override
	public Map<String, OssMaster> getBasicOssInfoListById(OssMaster ossMaster) {
		Map<String, OssMaster> resultMap = new HashMap<>();
		
		if (CoCodeManager.OSS_INFO_BY_ID != null && ossMaster.getOssIdList() != null && !ossMaster.getOssIdList().isEmpty()) {
			for (String ossId : ossMaster.getOssIdList()) {
				if (CoCodeManager.OSS_INFO_BY_ID.containsKey(ossId)) {
					resultMap.put(ossId, CoCodeManager.OSS_INFO_BY_ID.get(ossId));
				}
			}
		}
		
		return resultMap;
	}

	@Override
	public List<OssMaster> getOssListByName(OssMaster bean) {
		List<OssMaster> list = ossMapper.getOssListByName(bean);
		
		if (list == null) {
			list = new ArrayList<>();
		}
		
		// oss id로 취합(라이선스 정보)
		List<OssMaster> newList = new ArrayList<>();
		Map<String, OssMaster> remakeMap = new HashMap<>();
		OssMaster currentBean = null;
		for (OssMaster ossBean : list) {
			// name + version
			if (!isEmpty(ossBean.getOssVersion())) {
				ossBean.setOssNameVerStr(ossBean.getOssName() + " (" + ossBean.getOssVersion() + ")");
			} else {
				ossBean.setOssNameVerStr(ossBean.getOssName());
			}
			
			if (remakeMap.containsKey(ossBean.getOssId())) {
				currentBean = remakeMap.get(ossBean.getOssId());
			} else {
				currentBean = ossBean;
				
				if (!isEmpty(currentBean.getOssNickname())) {
					currentBean.setOssNickname(currentBean.getOssNickname().replaceAll("\\|", "<br>"));
				}
				
				currentBean.setCopyright(CommonFunction.lineReplaceToBR(ossBean.getCopyright()));
				currentBean.setSummaryDescription(CommonFunction.lineReplaceToBR(ossBean.getSummaryDescription()));
				currentBean.setImportantNotes(CommonFunction.lineReplaceToBR(ossBean.getImportantNotes()));
				
				String detectedLicense = avoidNull(ossBean.getDetectedLicense());
				List<String> detectedLicenseList = Arrays.asList(detectedLicense.split(","));
				String resultDectedLicense = "";
				String resultLicenseText = "";
				
				if (!isEmpty(detectedLicense)) {
					for (String dl : detectedLicenseList) {
						LicenseMaster licenseMaster = null;
						
						if (CoCodeManager.LICENSE_INFO.containsKey(dl)) {
							licenseMaster = CoCodeManager.LICENSE_INFO.get(dl);
						}
						
						if (licenseMaster != null) {
							resultDectedLicense += "<a href='javascript:void(0);' class='urlLink'  onclick='showLicenseText(" + avoidNull(licenseMaster.getLicenseId()) + ");' >" + dl + "</a>";
							resultLicenseText += "<div id='license_"+ avoidNull(licenseMaster.getLicenseId())+"' class='classLicenseText' style='display: none;'>"+CommonFunction.lineReplaceToBR(avoidNull(licenseMaster.getLicenseText()))+"</div>";
						}
					}
					
					ossBean.setDetectedLicense(resultDectedLicense + resultLicenseText);
				}
			}
			
			OssLicense licenseBean = new OssLicense();
			licenseBean.setLicenseId(ossBean.getLicenseId());
			licenseBean.setOssLicenseIdx(ossBean.getOssLicenseIdx());
			licenseBean.setLicenseName(ossBean.getLicenseName());
			licenseBean.setOssLicenseComb(ossBean.getOssLicenseComb());
			licenseBean.setOssLicenseText(CommonFunction.lineReplaceToBR(ossBean.getOssLicenseText()));
			licenseBean.setOssCopyright(CommonFunction.lineReplaceToBR(ossBean.getOssCopyright()));
			
			currentBean.addOssLicense(licenseBean);

			if (remakeMap.containsKey(ossBean.getOssId())) {
				remakeMap.replace(ossBean.getOssId(), currentBean);
			} else {
				remakeMap.put(ossBean.getOssId(), currentBean);
			}
		}
		
		for (OssMaster ossBean : remakeMap.values()) {
			ossBean.setLicenseName(CommonFunction.makeLicenseExpression(ossBean.getOssLicenses(), !isEmpty(bean.getOssId())));
			newList.add(ossBean);
		}
		
		return newList;
	}
	
	@Override
	public void updateLicenseDivDetail(OssMaster master) {
		if (master != null && !isEmpty(master.getOssId())) {
			boolean multiLicenseFlag = false;
			boolean dualLicenseFlag = false;
			boolean vDiffFlag = false;

			// Update multi / dual flag
			OssMaster updateParam = new OssMaster();
			
			// version 에 따라 라이선스가 달라지는지 체크 (v-diff)
			OssMaster param = new OssMaster();
			List<String> ossNameList = new ArrayList<>();
			ossNameList.add(master.getOssName());
			String[] ossNames = new String[ossNameList.size()];
			param.setOssNames(ossNameList.toArray(ossNames));
			// oss name 또는 nick name으로 참조 가능한 oss 이름만으로 검색한 db 정보
			Map<String, OssMaster> ossMap = getBasicOssInfoList(param);
			// size가 1개인 경우는 처리할 필요 없음
			List<String> ossIdListByName = new ArrayList<>();
			ossIdListByName.add(master.getOssId());
			
			if (ossMap != null && ossMap.size() > 1) {
				// 비교대상 key룰 먼저 설정
				List<List<OssLicense>> andCombLicenseListStandard = new ArrayList<>();
				List<List<OssLicense>> andCombLicenseListCompare = new ArrayList<>();
				
				int idx = 0;
				
				for (OssMaster _bean : ossMap.values()) {
					if (!isEmpty(_bean.getOssId())) {
						for (OssLicense license : _bean.getOssLicenses()) {
							if ("AND".equalsIgnoreCase(license.getOssLicenseComb())) {
								multiLicenseFlag = true;
							}
							
							if ("OR".equalsIgnoreCase(license.getOssLicenseComb())) {
								dualLicenseFlag = true;
							}
						}
						
						updateParam.setOssId(_bean.getOssId());
						updateParam.setMultiLicenseFlag(multiLicenseFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
						updateParam.setDualLicenseFlag(dualLicenseFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
						
						ossMapper.updateOssLicenseFlag(updateParam);
						
						updateParam = new OssMaster();
						multiLicenseFlag = false;
						dualLicenseFlag = false;
						
						ossIdListByName.add(_bean.getOssId());
						
						if (idx == 0) {
							andCombLicenseListStandard = makeLicenseKeyList(_bean.getOssLicenses());
						}else {
							if (!vDiffFlag && _bean.getOssLicenses() != null) {
								andCombLicenseListCompare = makeLicenseKeyList(_bean.getOssLicenses());
								
								if (andCombLicenseListStandard.size() != andCombLicenseListCompare.size()) {
									vDiffFlag = true;
								}else {
									if (!checkLicenseListVersionDiff(andCombLicenseListStandard, andCombLicenseListCompare)) {
										vDiffFlag = true;
									}
								}
								
								andCombLicenseListCompare = new ArrayList<>();
							}
						}
						
						idx++;
					}
				}
			} else {
				String ossId = "";
				
				if (ossMap != null) {
					for (OssMaster _bean : ossMap.values()) {
						if (!isEmpty(_bean.getOssId())) {
							ossId = _bean.getOssId();
							
							for (OssLicense license : _bean.getOssLicenses()) {
								if ("AND".equalsIgnoreCase(license.getOssLicenseComb())) {
									multiLicenseFlag = true;
								}
								
								if ("OR".equalsIgnoreCase(license.getOssLicenseComb())) {
									dualLicenseFlag = true;
								}
							}
							
							ossIdListByName.add(_bean.getOssId());
						}
					}
				}else {
					if (CoConstDef.LICENSE_DIV_MULTI.equals(master.getLicenseDiv())) {
						ossId = master.getOssId();
						
						for (OssLicense license : master.getOssLicenses()) {
							if ("AND".equalsIgnoreCase(license.getOssLicenseComb())) {
								multiLicenseFlag = true;
							}
											
							if ("OR".equalsIgnoreCase(license.getOssLicenseComb())) {
								dualLicenseFlag = true;
							}
						}
					}
				}
				
				if (ossId.isEmpty()) {
					ossId = master.getOssId();
				}
				
				updateParam.setOssId(ossId);
				updateParam.setMultiLicenseFlag(multiLicenseFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
				updateParam.setDualLicenseFlag(dualLicenseFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
				
				ossMapper.updateOssLicenseFlag(updateParam);
			}
			
			ossIdListByName = ossIdListByName.stream().distinct().collect(Collectors.toList());
			updateParam.setOssIdList(ossIdListByName);
			updateParam.setVersionDiffFlag(vDiffFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
			
			ossMapper.updateOssLicenseVDiffFlag(updateParam);
		}
	}

	private boolean checkLicenseListVersionDiff(List<List<OssLicense>> andCombLicenseListStandard, List<List<OssLicense>> andCombLicenseListCompare) {
		List<String> standardKey = new ArrayList<>();
		List<String> compareKey = new ArrayList<>();
		
		String licenseId = "";
		
		for (List<OssLicense> standardList : andCombLicenseListStandard) {
			standardList.sort(Comparator.comparing(OssLicense::getLicenseId));
			
			for (OssLicense ol : standardList) {
				licenseId += ol.getLicenseId() + ",";
			}
			
			standardKey.add(licenseId.substring(0, licenseId.length()-1));
			licenseId = "";
		}
		
		for (List<OssLicense> compareList : andCombLicenseListCompare) {
			compareList.sort(Comparator.comparing(OssLicense::getLicenseId));
			
			for (OssLicense ol : compareList) {
				licenseId += ol.getLicenseId() + ",";
			}
			
			compareKey.add(licenseId.substring(0, licenseId.length()-1));
			licenseId = "";
		}
		
		return standardKey.containsAll(compareKey);
	}

	@Override
	public OssMaster getLastModifiedOssInfoByName(OssMaster bean) {
		return ossMapper.getLastModifiedOssInfoByName(bean);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkVdiff(Map<String, Object> reqMap) {
		Map<String, Object> rtnMap = new HashMap<>();
		boolean vDiffFlag = false;
		// version 에 따라 라이선스가 달라지는지 체크 (v-diff)
		OssMaster param = new OssMaster();
		String ossId = avoidNull((String) reqMap.get("ossId"));
		String ossName = (String) reqMap.get("ossName");
		String ossVersion = "";
		if (reqMap.containsKey("ossVersion")) ossVersion = (String) reqMap.get("ossVersion");
		List<OssLicense> license = (List<OssLicense>) reqMap.get("license");
		String[] ossNames = new String[1];
		ossNames[0] = ossName;
		param.setOssNames(ossNames);

		// oss name 또는 nick name으로 참조 가능한 oss 이름만으로 검색한 db 정보
		Map<String, OssMaster> ossMap = getBasicOssInfoList(param);

		// size가 1개인 경우는 처리할 필요 없음
		List<String> ossIdListByName = new ArrayList<>();
		ossIdListByName.add(ossId);
		
		if (ossMap != null && !ossMap.isEmpty()) {
			// 비교대상 key룰 먼저 설정
			String _key = makeLicenseIdKeyStr(license);
			
			for (OssMaster _bean : ossMap.values()) {
				if (!isEmpty(_bean.getOssId())) {
					ossIdListByName.add(_bean.getOssId());
					if (_bean.getOssLicenses() != null && !ossId.equals(_bean.getOssId())) {
						if (!_key.equals(makeLicenseIdKeyStr(_bean.getOssLicenses()))) {
							vDiffFlag = true;
							break;
						}
					}
				}
			}
		}
		
		rtnMap.put("vFlag", vDiffFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
		
		if (vDiffFlag && !isEmpty(ossVersion)) {
			if (ossMapper.checkOssVersionDiff(ossName) == 0) {
				List<String> firstVersionDiffList = new ArrayList<>();
				for (String key : ossMap.keySet()) {
					OssMaster om = ossMap.get(key);
					firstVersionDiffList.add(om.getOssName() + " (" + om.getOssVersion() + ")|" + CommonFunction.makeLicenseExpression(om.getOssLicenses()));
				}
				rtnMap.put("resultData", firstVersionDiffList);
			}
		}
		
		return rtnMap;
	}
	
	private List<List<OssLicense>> makeLicenseKeyList(List<OssLicense> list) {
		List<List<OssLicense>> andCombLicenseList = new ArrayList<>();
		
		for (OssLicense license : list) {
			if (andCombLicenseList.isEmpty() || "OR".equals(license.getOssLicenseComb())) {
				andCombLicenseList.add(new ArrayList<>());
			}
			
			andCombLicenseList.get(andCombLicenseList.size()-1).add(license);
		}
		
		return andCombLicenseList;
	}
	
	private String makeLicenseIdKeyStr(List<OssLicense> list) {
		String rtnVal = "";
		List<String> licenseIdList = new ArrayList<>();
		
		if (list != null) {
			for (OssLicense bean : list) {
				licenseIdList.add(bean.getLicenseId());
			}
		}
		
		Collections.sort(licenseIdList);
		
		for (String s : licenseIdList) {
			if (!isEmpty(rtnVal)) {
				rtnVal += "-";
			}
			
			rtnVal += s;
		}

		return rtnVal;
	}
	
	@Override
	public void checkOssLicenseAndObligation(OssMaster ossMaster) {
		List<List<OssLicense>> orLicenseList = new ArrayList<>();
		String currentType = null;
		String currentObligation = null;
		boolean isFirst = true;
		List<OssLicense> andLicenseList = new ArrayList<>();

		for (OssLicense license : ossMaster.getOssLicenses()) {
			LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
			master = master != null ? master : new LicenseMaster();
			if (!isEmpty(master.getLicenseId()) && !license.getLicenseId().equals(master.getLicenseId())) {
				license.setLicenseId(master.getLicenseId());
			}
			license.setLicenseType(master.getLicenseType());
			
			// obligation 설정
			if (CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationNeedsCheckYn()))) {
				license.setObligation(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK);
			} else if (CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationDisclosingSrcYn()))) {
				license.setObligation(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
			} else if (CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationNotificationYn()))) {
				license.setObligation(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
			}
			
			if (!isFirst && "OR".equalsIgnoreCase(license.getOssLicenseComb()) ) {
				if (!andLicenseList.isEmpty()) {
					orLicenseList.add(andLicenseList);
					andLicenseList = new ArrayList<>();
					andLicenseList.add(license);
				}
				
				andLicenseList = new ArrayList<>();
				andLicenseList.add(license);
			} else {
				andLicenseList.add(license);
			}
			
			isFirst = false;
		}

		if (!andLicenseList.isEmpty()) {
			orLicenseList.add(andLicenseList);
		}
		
		//  or인 경우는 Permissive한 걸로, and인 경우는 Copyleft 가 강한 것으로 표시
		for (List<OssLicense> andlicenseGroup : orLicenseList) {
			OssLicense permissiveLicense = CommonFunction.getLicensePermissiveTypeLicense(andlicenseGroup);
			
			if (permissiveLicense != null) {
				switch (permissiveLicense.getLicenseType()) {
					case CoConstDef.CD_LICENSE_TYPE_PMS:
						currentType = CoConstDef.CD_LICENSE_TYPE_PMS;
						
						currentObligation = CommonFunction.getObligationTypeWithAndLicense(andlicenseGroup);
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_WCP:
						if (!CoConstDef.CD_LICENSE_TYPE_PMS.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_WCP;
							
							currentObligation = CommonFunction.getObligationTypeWithAndLicense(andlicenseGroup);
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_CP:
						if (!CoConstDef.CD_LICENSE_TYPE_PMS.equals(currentType)
								&& !CoConstDef.CD_LICENSE_TYPE_WCP.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_CP;
							
							currentObligation = CommonFunction.getObligationTypeWithAndLicense(andlicenseGroup);
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_PF:
						if (!CoConstDef.CD_LICENSE_TYPE_PMS.equals(currentType)
								&& !CoConstDef.CD_LICENSE_TYPE_WCP.equals(currentType)
								&& !CoConstDef.CD_LICENSE_TYPE_CP.equals(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_PF;
							
							currentObligation = CommonFunction.getObligationTypeWithAndLicense(andlicenseGroup);
						}
						
						break;
					case CoConstDef.CD_LICENSE_TYPE_NA:
						if (isEmpty(currentType)) {
							currentType = CoConstDef.CD_LICENSE_TYPE_NA;
							
							currentObligation = CommonFunction.getObligationTypeWithAndLicense(andlicenseGroup);
						}
						
						break;
					default:
						break;
				}
			}
		}
		
		ossMaster.setLicenseType(currentType);
		ossMaster.setObligationType(currentObligation);
	}

	@Override
	public void updateLicenseTypeAndObligation(OssMaster ossBean) {
		ossMapper.updateLicenseTypeAndObligation(ossBean);
	}

	@Override
	public Map<String, Object> checkExistsOssDownloadLocation(OssMaster ossMaster) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		if (!isEmpty(ossMaster.getDownloadLocation())) {
			List<String> checkOssNameUrl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_NAME_URL);
			int urlSearchSeq = -1;
			int seq = 0;
			
			try {
				if (ossMaster.getDownloadLocation().contains(";")) {
					ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().split(";")[0]);
				}
				
				for (String url : checkOssNameUrl) {
					if (urlSearchSeq == -1 && ossMaster.getDownloadLocation().contains(url)) {
						urlSearchSeq = seq;
						break;
					}
					seq++;
				}
				
				String downloadLocationUrl = "";
				
				if (ossMaster.getDownloadLocation().startsWith("git://")) {
					ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().replace("git://", "https://"));
				} else if (ossMaster.getDownloadLocation().startsWith("ftp://")) {
					ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().replace("ftp://", "https://"));
				} else if (ossMaster.getDownloadLocation().startsWith("svn://")) {
					ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().replace("svn://", "https://"));
				} else if (ossMaster.getDownloadLocation().startsWith("git@")) {
					ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().replace("git@", "https://"));
				}
				
				if (ossMaster.getDownloadLocation().contains(".git")) {
					if (ossMaster.getDownloadLocation().endsWith(".git")) {
						ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().substring(0, ossMaster.getDownloadLocation().length()-4));
					} else {
						if (ossMaster.getDownloadLocation().contains("#")) {
							ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().substring(0, ossMaster.getDownloadLocation().indexOf("#")));
							ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().substring(0, ossMaster.getDownloadLocation().length()-4));
						}
					}
				}
				
				String[] downloadlocationUrlSplit = ossMaster.getDownloadLocation().split("/");
				if (downloadlocationUrlSplit[downloadlocationUrlSplit.length-1].indexOf("#") > -1) {
					ossMaster.setDownloadLocation(ossMaster.getDownloadLocation().substring(0, ossMaster.getDownloadLocation().indexOf("#")));
				}
				
				if (!ossMaster.getDownloadLocation().startsWith("http://") && !ossMaster.getDownloadLocation().startsWith("https://")) {
					if (ossMaster.getDownloadLocation().contains("//")) {
						ossMaster.setDownloadLocation("https://" + ossMaster.getDownloadLocation().split("//")[1]);
					} else {
						ossMaster.setDownloadLocation("https://" + ossMaster.getDownloadLocation());
					}
				}
				
				if ( urlSearchSeq > -1 ) {
					Pattern p = generatePattern(urlSearchSeq, ossMaster.getDownloadLocation());
					Matcher m = p.matcher(ossMaster.getDownloadLocation());
					
					while (m.find()) {
						ossMaster.setDownloadLocation(m.group(0));
					}
				}	
					
				if (ossMaster.getDownloadLocation().startsWith("http://") 
						|| ossMaster.getDownloadLocation().startsWith("https://")) {
					downloadLocationUrl = ossMaster.getDownloadLocation().split("//")[1];
				}
				
				if (downloadLocationUrl.startsWith("www.")) {
					downloadLocationUrl = downloadLocationUrl.substring(5, downloadLocationUrl.length());
				}
				
				if (!isEmpty(downloadLocationUrl)) {
					ossMaster.setDownloadLocation(downloadLocationUrl);
				}
				returnMap.put("downloadLocation", ossMapper.checkExistsOssDownloadLocation(ossMaster));
				
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		return returnMap;
	}

	@Override
	public Map<String, Object> checkExistsOssHomepage(OssMaster ossMaster) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		if (!isEmpty(ossMaster.getHomepage())) {
			List<String> checkOssNameUrl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_NAME_URL);
			int urlSearchSeq = -1;
			int seq = 0;
			
			try {
				if (ossMaster.getHomepage().contains(";")) {
					ossMaster.setHomepage(ossMaster.getHomepage().split(";")[0]);
				}
				
				for (String url : checkOssNameUrl) {
					if (urlSearchSeq == -1 && ossMaster.getHomepage().contains(url)) {
						urlSearchSeq = seq;
						break;
					}
					seq++;
				}
				
				String homepageUrl = "";
				
				if (ossMaster.getHomepage().startsWith("git://")) {
					ossMaster.setHomepage(ossMaster.getHomepage().replace("git://", "https://"));
				} else if (ossMaster.getHomepage().startsWith("ftp://")) {
					ossMaster.setHomepage(ossMaster.getHomepage().replace("ftp://", "https://"));
				} else if (ossMaster.getHomepage().startsWith("svn://")) {
					ossMaster.setHomepage(ossMaster.getHomepage().replace("svn://", "https://"));
				} else if (ossMaster.getHomepage().startsWith("git@")) {
					ossMaster.setHomepage(ossMaster.getHomepage().replace("git@", "https://"));
				}
				
				if (ossMaster.getHomepage().contains(".git")) {
					if (ossMaster.getHomepage().endsWith(".git")) {
						ossMaster.setHomepage(ossMaster.getHomepage().substring(0, ossMaster.getHomepage().length()-4));
					} else {
						if (ossMaster.getHomepage().contains("#")) {
							ossMaster.setHomepage(ossMaster.getHomepage().substring(0, ossMaster.getHomepage().indexOf("#")));
							ossMaster.setHomepage(ossMaster.getHomepage().substring(0, ossMaster.getHomepage().length()-4));
						}
					}
				}
				
				String[] homepageUrlSplit = ossMaster.getHomepage().split("/");
				if (homepageUrlSplit[homepageUrlSplit.length-1].indexOf("#") > -1) {
					ossMaster.setHomepage(ossMaster.getHomepage().substring(0, ossMaster.getHomepage().indexOf("#")));
				}
				
				if (!ossMaster.getHomepage().startsWith("http://") && !ossMaster.getHomepage().startsWith("https://")) {
					if (ossMaster.getHomepage().contains("//")) {
						ossMaster.setHomepage("https://" + ossMaster.getHomepage().split("//")[1]);
					} else {
						ossMaster.setHomepage("https://" + ossMaster.getHomepage());
					}
				}
				
				if ( urlSearchSeq > -1 ) {
					Pattern p = generatePattern(urlSearchSeq, ossMaster.getHomepage());
					Matcher m = p.matcher(ossMaster.getHomepage());
					
					while (m.find()) {
						ossMaster.setHomepage(m.group(0));
					}
				}	
					
				if (ossMaster.getHomepage().startsWith("http://") 
						|| ossMaster.getHomepage().startsWith("https://")) {
					homepageUrl = ossMaster.getHomepage().split("//")[1];
				}
				
				if (homepageUrl.startsWith("www.")) {
					homepageUrl = homepageUrl.substring(5, homepageUrl.length());
				}
				
				if (!isEmpty(homepageUrl)) {
					ossMaster.setHomepage(homepageUrl);
				}
				returnMap.put("homepage", ossMapper.checkExistsOssHomepage(ossMaster));
				
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		return returnMap;
	}

	@Override
	public Map<String, Object> checkExistsOssDownloadLocationWithOssName(OssMaster param) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("downloadLocation", ossMapper.checkExistsOssDownloadLocationWithOssName(param));
		
		return returnMap;
	}

	@Override
	public Map<String, Object> checkExistsOssHomepageWithOssName(OssMaster param) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("homepage", ossMapper.checkExistsOssHomepageWithOssName(param));
		
		return returnMap;
	}

	@Override
	public Map<String, OssMaster> getBasicOssInfoList(OssMaster ossMaster) {
		return getBasicOssInfoList(ossMaster, false);
	}

	@Override
	public Map<String, OssMaster> getBasicOssInfoList(OssMaster ossMaster, boolean useUpperKey) {
		List<OssMaster> list = ossMapper.getBasicOssInfoList(ossMaster);
		
		return makeBasicOssInfoMap(list, false, useUpperKey);
	}	
	
	@Override
	public int checkExistsOssByname(OssMaster bean) {
		return ossMapper.checkExistsOssByname(bean);
	}

	private ProjectIdentification generateCheckOSSName(ProjectIdentification bean, Pattern p, List<String> androidPlatformList, String downloadlocationUrl) {
		String checkName = "";
		boolean isValid = false;
		Matcher ossNameMatcher = p.matcher("https://" + downloadlocationUrl);
		String[] android = null;
		while (ossNameMatcher.find()) {
			for (String list : androidPlatformList){
				if (ossNameMatcher.group(3).equalsIgnoreCase(list)){
					isValid = true;
					android = list.replaceAll("^platform/","").split("/");
					break;
				}
			}
			if(!isValid) {
				android = ossNameMatcher.group(3).replaceAll("^platform/","").split("/");
				bean.setCheckOssList("I");
			}
			checkName = "android-";
			for (String name : android) {
				checkName += name + "-";
			}
			checkName = checkName.substring(0, checkName.length()-1);
		}
		bean.setCheckName(checkName);
		return bean;
	}

	private String generateCheckOSSName(int urlSearchSeq, String downloadlocationUrl, Pattern p) {
		String checkName = "";
		String customDownloadlocationUrl = "";
		if (downloadlocationUrl.contains("?")) {
			customDownloadlocationUrl = downloadlocationUrl.split("[?]")[0];
		} else {
			customDownloadlocationUrl = downloadlocationUrl;
		}

		if(urlSearchSeq == 12 && downloadlocationUrl.contains("/-/")){
			customDownloadlocationUrl = downloadlocationUrl.split("/-/")[0];
		}

		Matcher ossNameMatcher = p.matcher("https://" + customDownloadlocationUrl);
		while (ossNameMatcher.find()){
			switch(urlSearchSeq) {
				case 0: // github
					checkName = ossNameMatcher.group(3) + "-" + ossNameMatcher.group(4);
					break;
				case 1: // npm
				case 6: // npm
					checkName = "npm:" + ossNameMatcher.group(4);
					if (checkName.contains(":@")) {
						checkName += "/" + ossNameMatcher.group(5);
					}
					break;
				case 2: // pypi
					checkName = "pypi:" + ossNameMatcher.group(3);
					break;
				case 3: // maven
					checkName = ossNameMatcher.group(3) + ":" + ossNameMatcher.group(4);
					break;
				case 4: // pub
					checkName = "pub:" + ossNameMatcher.group(3);
					break;
				case 5: // cocoapods
					checkName = "cocoapods:" + ossNameMatcher.group(3);
					break;
				case 8:
					checkName = "nuget:" + ossNameMatcher.group(3);
					break;
				case 9:
					checkName = "stackoverflow-" + ossNameMatcher.group(3);
					break;
				case 11:
					checkName = "cargo:" + ossNameMatcher.group(3);
					break;
				case 12 :
					ArrayList<String> name = new ArrayList<>();
					name.add("codelinaro");
					for(String nick : ossNameMatcher.group(5).split("/")) {
						name.add(nick);
					}
					checkName = String.join("-", name);
					break;
				case 13 :
					checkName = "go:" + ossNameMatcher.group(3).split("@")[0];
					break;
				default:
					break;
			}
		}
		return checkName;
	}

	private String appendCheckOssName(List<OssMaster> ossNameList, Map<String, String> ossInfoNames, String checkOssName) {
		List<String> checkName = new ArrayList<>();

		if(ossNameList != null) {
			for (OssMaster ossBean : ossNameList) {
				if(ossBean != null) {
					for(String name : ossBean.getOssName().split(",")){
						checkName.add(name);
					}
				}
			}
		}
		
		if (ossInfoNames.containsKey(checkOssName.toUpperCase())) {
			String ossNameTemp = ossInfoNames.get(checkOssName.toUpperCase());
			checkName.add(ossNameTemp);
		}
		
		return checkName.stream().distinct().map(v->v.toString()).collect(Collectors.joining("|"));
	}

	private Pattern generatePattern(int urlSearchSeq, String downloadlocationUrl) {
		Pattern p = null;

		switch(urlSearchSeq) {
			case 0: // github
				if (downloadlocationUrl.contains("www.")) {
					downloadlocationUrl = downloadlocationUrl.replace("www.", "");
				}
				p = Pattern.compile("((http|https)://github.com/([^/]+)/([^/]+))");

				break;
			case 1: // npm
			case 6: // npm
				if (downloadlocationUrl.contains("/package/@")) {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+)/([^/]+))");
				}else {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+))");
				}
				break;
			case 2: // pypi
				p = Pattern.compile("((http|https)://pypi.org/project/([^/]+))");
				break;
			case 3: // maven
				p = Pattern.compile("((http|https)://mvnrepository.com/artifact/([^/]+)/([^/]+))");
				break;
			case 4: // pub
				p = Pattern.compile("((http|https)://pub.dev/packages/([^/]+))");
				break;
			case 5: // cocoapods
				p = Pattern.compile("((http|https)://cocoapods.org/pods/([^/]+))");
				break;
			case 7:
				p = Pattern.compile("((http|https)://android.googlesource.com/(.*))");
				break;
			case 8:
				p = Pattern.compile("((http|https)://nuget.org/packages/([^/]+))");
				break;
			case 9:
				p = Pattern.compile("((http|https)://stackoverflow.com/revisions/([^/]+)/([^/]+))");
				break;
			case 11:
				p = Pattern.compile("((http|https)://crates.io/crates/([^/]+))");
				break;
			case 12 :
				p = Pattern.compile("((http|https)://git.codelinaro.org/([^/]+)/([^/]+)/(.*))");
				break;
			case 13:
				p = Pattern.compile("((http|https)://pkg.go.dev/(.*))");
				break;
			default:
				p = Pattern.compile("(.*)");
				break;
		}
		return p;
	}


	private ProjectIdentification downloadlocationFormatter(ProjectIdentification bean, int urlSearchSeq) {
		if (urlSearchSeq == 0) {
			if (bean.getDownloadLocation().startsWith("git://")) {
				bean.setDownloadLocation(bean.getDownloadLocation().replace("git://", "https://"));
			}
			if (bean.getDownloadLocation().startsWith("git@")) {
				bean.setDownloadLocation(bean.getDownloadLocation().replace("git@", "https://"));
			}
			if (bean.getDownloadLocation().contains(".git")) {
				if (bean.getDownloadLocation().endsWith(".git")) {
					bean.setDownloadLocation(bean.getDownloadLocation().substring(0, bean.getDownloadLocation().length()-4));
				} else {
					if (bean.getDownloadLocation().contains("#")) {
						bean.setDownloadLocation(bean.getDownloadLocation().substring(0, bean.getDownloadLocation().indexOf("#")));
						bean.setDownloadLocation(bean.getDownloadLocation().substring(0, bean.getDownloadLocation().length()-4));
					}
				}
			}
		}
		String downloadlocationUrl = bean.getDownloadLocation();

		String[] downloadlocationUrlSplit = downloadlocationUrl.split("/");
		if (downloadlocationUrlSplit[downloadlocationUrlSplit.length-1].indexOf("#") > -1) {
			downloadlocationUrl = downloadlocationUrl.substring(0, downloadlocationUrl.indexOf("#"));
		}

		Pattern p = generatePattern(urlSearchSeq, downloadlocationUrl);

		Matcher m = p.matcher(downloadlocationUrl);

		while (m.find()) {
			bean.setDownloadLocation(m.group(0));
		}

		if (bean.getDownloadLocation().startsWith("http://")
				|| bean.getDownloadLocation().startsWith("https://")
				|| bean.getDownloadLocation().startsWith("git://")
				|| bean.getDownloadLocation().startsWith("ftp://")
				|| bean.getDownloadLocation().startsWith("svn://")) {
			downloadlocationUrl = bean.getDownloadLocation().split("//")[1];
		}

		if (downloadlocationUrl.startsWith("www.")) {
			downloadlocationUrl = downloadlocationUrl.substring(4, downloadlocationUrl.length());
		}

		bean.setDownloadLocation(downloadlocationUrl);
		return bean;
	}

	private List<String> getAndroidPlatformList(){
		List<String> list = new ArrayList<String>();
		Connection conn = Jsoup.connect("https://android.googlesource.com/");
		try{
			Document document = conn.get();
			Elements parsingDiv = document.getElementsByClass("RepoList-itemName");
			for (Element element : parsingDiv) {
				if (!element.text().equals("Name")){
					list.add(element.text());
				}
			}
		} catch(IOException e){

		}
		return list;
	}

	@Override
	public Map<String, Object> getCheckOssNameAjax(ProjectIdentification paramBean, String targetName) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> map = null;
		List<ProjectIdentification> result = new ArrayList<ProjectIdentification>();

		switch(targetName.toUpperCase()) {
			case CoConstDef.CD_CHECK_OSS_SELF:
				map = selfCheckService.getIdentificationGridList(paramBean);
				break;
			case CoConstDef.CD_CHECK_OSS_IDENTIFICATION:
				map = projectService.getIdentificationGridList(paramBean);
				break;
			case CoConstDef.CD_CHECK_OSS_PARTNER:
				map = partnerService.getIdentificationGridList(paramBean);
				break;
		}

		// 중간 저장을 기능 대응을 위해 save시 유효성 체크를 data load시로 일괄 변경
		if (map != null) {
			T2CoProjectValidator pv = new T2CoProjectValidator();

			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
			List<ProjectIdentification> mainData = (List<ProjectIdentification>) map.get("mainData");
			pv.setAppendix("mainList", mainData);
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));

			T2CoValidationResult vr = pv.validate(new HashMap<>());

			if (!vr.isValid()) {
				Map<String, String> validMap = vr.getValidMessageMap();
				result.addAll(checkOssNameData(mainData, validMap, null));
				resMap.put("validMap", validMap);
			}

			if(!vr.isDiff()){
				Map<String, String> diffMap = vr.getDiffMessageMap();
				result.addAll(checkOssNameData(mainData, null, diffMap));
				resMap.put("diffMap", diffMap);
			}

			result.addAll(checkOssNameData(mainData, null, null));
		}

		if(result.size() > 0) {
			result = checkOssName(result);
			List<ProjectIdentification> valid = new ArrayList<ProjectIdentification>();
			List<ProjectIdentification> invalid = new ArrayList<ProjectIdentification>();
			for(ProjectIdentification prj : result){
				if(prj.getCheckOssList().equals("I")){
					invalid.add(prj);
				} else{
					valid.add(prj);
				}
			}
			resMap.put("list", Stream.concat(valid.stream(), invalid.stream()).collect(Collectors.toList()));
		}
		return resMap;
	}

	@Override
	public Map<String, Object> getCheckOssLicenseAjax(ProjectIdentification paramBean, String targetName) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> map = null;
		List<ProjectIdentification> result = new ArrayList<ProjectIdentification>();

		switch(targetName.toUpperCase()) {
			case CoConstDef.CD_CHECK_OSS_SELF:
				map = selfCheckService.getIdentificationGridList(paramBean);

				break;
			case CoConstDef.CD_CHECK_OSS_IDENTIFICATION:
				map = projectService.getIdentificationGridList(paramBean);

				break;

			case CoConstDef.CD_CHECK_OSS_PARTNER:
				map = partnerService.getIdentificationGridList(paramBean);

				break;
		}

		// intermediate storage function correspondence : validation check when loading data
		if (map != null) {
			T2CoProjectValidator pv = new T2CoProjectValidator();

			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
			List<ProjectIdentification> mainData = (List<ProjectIdentification>) map.get("mainData");
			pv.setAppendix("mainList", mainData);
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));

			T2CoValidationResult vr = pv.validate(new HashMap<>());

			if (!vr.isValid()) {
				Map<String, String> validMap = vr.getValidMessageMap();
				result.addAll(autoFillOssInfoService.checkOssLicenseData(mainData, validMap, null));
				resMap.put("validMap", validMap);
			}

			if (!vr.isDiff()){
				Map<String, String> diffMap = vr.getDiffMessageMap();
				result.addAll(autoFillOssInfoService.checkOssLicenseData(mainData, null, diffMap));
				resMap.put("diffMap", diffMap);
			}
		}

		if (result.size() > 0) {
			Map<String, Object> data = autoFillOssInfoService.checkOssLicense(result);
			resMap.put("list", data.get("checkedData"));
			resMap.put("error", data.get("error"));
		}
		return resMap;
	}

	@Override
	public List<ProjectIdentification> checkOssName(List<ProjectIdentification> list){
		List<ProjectIdentification> result = new ArrayList<ProjectIdentification>();
		List<String> checkOssNameUrl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_NAME_URL);
		List<String> packageManagerUrl = new ArrayList<>();
		for(String code : CoCodeManager.getCodes(CoConstDef.CD_CHECK_OSS_NAME_URL)) {
			if(avoidNull(CoCodeManager.getSubCodeNo(CoConstDef.CD_CHECK_OSS_NAME_URL, code)).equals("1")) {
				packageManagerUrl.add(CoCodeManager.getCodeString(CoConstDef.CD_CHECK_OSS_NAME_URL, code));
			}
		}

		int urlSearchSeq = -1;
		List<String> androidPlatformList = getAndroidPlatformList();
		Map<String, String> ossInfoNames = CoCodeManager.OSS_INFO_UPPER_NAMES;

		// oss name과 download location이 동일한 oss의 componentId를 묶어서 List<ProjectIdentification>을 만듬
		list = list.stream()
				.collect(Collectors.groupingBy(oss -> oss.getOssName() + "-" + oss.getDownloadLocation()))
				.values().stream()
				.map(ossList -> {
					ProjectIdentification uniqueOss = ossList.stream().distinct().findFirst().get();
					List<String> componentIds = ossList.stream().map(oss -> oss.getComponentId()).distinct().collect(Collectors.toList());
					uniqueOss.setComponentIdList(componentIds);
					return uniqueOss;
				})
				.collect(Collectors.toList());

		for (ProjectIdentification bean : list) {
			int seq = 0;
			urlSearchSeq = -1;

			for(String url : packageManagerUrl) {
				if (!isEmpty(bean.getHomepage()) && bean.getHomepage().contains(url)) {
					bean.setDownloadLocation(bean.getHomepage());
				}
			}

			if (isEmpty(bean.getDownloadLocation())) {
				continue;
			}

			try {
				boolean semicolonFlag = false;
				String semicolonStr = "";
				String downloadLocation = bean.getDownloadLocation();

				if (bean.getDownloadLocation().contains(";")) {
					semicolonFlag = true;
					int idx = 0;
					for (String smc : bean.getDownloadLocation().split(";")) {
						if (idx > 0) {
							semicolonStr += smc + ";";
						}
						idx++;
					}
					semicolonStr = semicolonStr.substring(0, semicolonStr.length()-1);
					bean.setDownloadLocation(bean.getDownloadLocation().split(";")[0]);
				}

				for (String url : checkOssNameUrl) {
					if (urlSearchSeq == -1 && bean.getDownloadLocation().contains(url)) {
						urlSearchSeq = seq;
						break;
					}
					seq++;
				}

				if ( urlSearchSeq > -1 ) {
					if(urlSearchSeq == 10) { //pythonhosted
						String name[] =  bean.getDownloadLocation().split("/");
						bean.setDownloadLocation(checkOssNameUrl.get(2) + name[name.length-2]);
					}

					bean = downloadlocationFormatter(bean, urlSearchSeq);
					String downloadlocationUrl = bean.getDownloadLocation();
					
					if (urlSearchSeq == 7) {
						if (downloadlocationUrl.contains("+")) {
							downloadlocationUrl = downloadlocationUrl.split("[+]")[0];
							downloadlocationUrl = downloadlocationUrl.substring(0, downloadlocationUrl.lastIndexOf("/"));
						}
					}
					
					downloadlocationUrl = URLDecoder.decode(downloadlocationUrl);
					
					Pattern p = generatePattern(urlSearchSeq, downloadlocationUrl);
					int cnt = ossMapper.checkOssNameUrl2Cnt(bean);
					if (cnt == 0) {
						bean.setOssNickName(generateCheckOSSName(urlSearchSeq, downloadlocationUrl, p));
						String checkName = appendCheckOssName(ossMapper.checkOssNameTotal(bean), ossInfoNames, bean.getOssNickName());
						if (!isEmpty(checkName)) {
							bean.setCheckOssList("Y");
							bean.setRecommendedNickname(bean.getOssNickName());
						} else {
							if (urlSearchSeq == 7) {
								generateCheckOSSName(bean, p, androidPlatformList, downloadlocationUrl);
								checkName = bean.getCheckName();
							} else if (urlSearchSeq == 3 || urlSearchSeq == 5){
								checkName = generateCheckOSSName(urlSearchSeq, downloadlocationUrl, p);
							} else {
								String redirectlocationUrl = "";
								try {
									URL checkUrl = new URL("https://" + downloadlocationUrl);
									HttpURLConnection oc = (HttpURLConnection) checkUrl.openConnection();
									oc.setUseCaches(false);
									oc.setConnectTimeout(1500);
									if (200 == oc.getResponseCode()) {
										ProjectIdentification url = new ProjectIdentification();
										url.setDownloadLocation(oc.getURL().toString());
										url = downloadlocationFormatter(url, urlSearchSeq);
										if (url.getDownloadLocation().equals(downloadlocationUrl) || url.getDownloadLocation().equals(downloadlocationUrl + "/")) {
											checkName = generateCheckOSSName(urlSearchSeq, downloadlocationUrl, p);
										} else {
											if (oc.getURL().toString().indexOf("//") > -1) {
												redirectlocationUrl = oc.getURL().toString().split("//")[1];
											}
											bean.setDownloadLocation(redirectlocationUrl);
											bean.setOssNickName(generateCheckOSSName(urlSearchSeq, redirectlocationUrl, p));
											checkName = appendCheckOssName(ossMapper.checkOssNameTotal(bean), ossInfoNames, bean.getOssNickName());
											if (!isEmpty(checkName)) {
												bean.setCheckOssList("Y");
												bean.setRecommendedNickname(bean.getOssNickName() + "|" + generateCheckOSSName(urlSearchSeq, redirectlocationUrl, p));
											} else {
												checkName = generateCheckOSSName(urlSearchSeq, redirectlocationUrl, p);
											}
											bean.setRedirectLocation(redirectlocationUrl);
										}
									} else {
										checkName = generateCheckOSSName(urlSearchSeq, downloadlocationUrl, p);
										bean.setCheckOssList("I");
									}
								} catch (IOException e) {
									checkName = generateCheckOSSName(urlSearchSeq, downloadlocationUrl, p);
									bean.setCheckOssList("I");
								}
							}
						}

						if (!isEmpty(checkName)){
							bean.setCheckName(checkName);
							bean.setDownloadLocation(downloadLocation);
							if (!bean.getOssName().equals(bean.getCheckName())) {
								result.add(bean);
							}
						}
					}
				} else {
					String downloadlocationUrl = "";
					
					if (semicolonFlag) {
						downloadlocationUrl = bean.getDownloadLocation().split(";")[0];
					} else {
						downloadlocationUrl = bean.getDownloadLocation();
					}
					
					if (downloadlocationUrl.startsWith("git@")) {
						downloadlocationUrl = downloadlocationUrl.replace("git@", "");
					}
					
					if (downloadlocationUrl.startsWith("http://") 
							|| downloadlocationUrl.startsWith("https://")
							|| downloadlocationUrl.startsWith("git://")
							|| downloadlocationUrl.startsWith("ftp://")
							|| downloadlocationUrl.startsWith("svn://")) {
						downloadlocationUrl = downloadlocationUrl.split("//")[1];
					}
					
					if (downloadlocationUrl.startsWith("www.")) {
						downloadlocationUrl = downloadlocationUrl.substring(4, downloadlocationUrl.length());
					}
					
					if (downloadlocationUrl.contains(".git")) {
						if (downloadlocationUrl.endsWith(".git")) {
							downloadlocationUrl = downloadlocationUrl.substring(0, downloadlocationUrl.length()-4);
						} else {
							if (downloadlocationUrl.contains("#")) {
								downloadlocationUrl = downloadlocationUrl.substring(0, downloadlocationUrl.indexOf("#"));
								downloadlocationUrl = downloadlocationUrl.substring(0, downloadlocationUrl.length()-4);
							}
						}
					}
					
					String[] downloadlocationUrlSplit = downloadlocationUrl.split("/");
					if (downloadlocationUrlSplit[downloadlocationUrlSplit.length-1].indexOf("#") > -1) {
						downloadlocationUrl = downloadlocationUrl.substring(0, downloadlocationUrl.indexOf("#"));
					}
					
					bean.setDownloadLocation(downloadlocationUrl);
					
					int cnt = ossMapper.checkOssNameUrl2Cnt(bean);
					
					if (cnt == 0) {
						List<OssMaster> ossNameList = ossMapper.checkOssNameUrl2(bean);
						String checkName = "";
						
						if (ossNameList != null && !ossNameList.isEmpty()) {
							for (OssMaster ossBean : ossNameList) {
								if (!isEmpty(checkName)) {
									checkName += "|";
								}
								
								checkName += ossBean.getOssName();
							}
						}
						
						if (!isEmpty(checkName)) {
							bean.setCheckOssList("Y");
							bean.setCheckName(checkName);
							if (!bean.getOssName().equals(bean.getCheckName())) {
								bean.setDownloadLocation(downloadLocation);
								result.add(bean);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		final Comparator<ProjectIdentification> comp = (p1, p2) -> Integer.compare(StringUtil.countMatches(p1.getCheckName(), ","), StringUtil.countMatches(p2.getCheckName(), ","));
		
		// oss name과 registered oss name은 unique하게 출력
		List<ProjectIdentification> sortedData = result.stream().filter(CommonFunction.distinctByKey(p -> p.getOssName()+p.getCheckName())).sorted(comp).collect(Collectors.toList());
		
		// oss name과 registered oss name이 unique하지 않다면 중복된 data의 downloadlocation을 전부 합쳐서 출력함. 
		for (ProjectIdentification p : sortedData) {
			String downloadLocation = result.stream()
											.filter(e -> (e.getOssName()+e.getCheckName()).equals(p.getOssName()+p.getCheckName()))
											.map(e -> e.getDownloadLocation())
											.distinct()
											.collect(Collectors.joining(","));

			List<String> componentIds = result.stream()
											.filter(e -> (e.getOssName()+e.getCheckName()).equals(p.getOssName()+p.getCheckName()))
											.map(e -> e.getComponentIdList())
											.flatMap(Collection::stream)
											.distinct()
											.collect(Collectors.toList());

			p.setDownloadLocation(downloadLocation);
			p.setComponentIdList(componentIds);
		}
		
		return sortedData;
	}
	
	@Transactional
	@Override
	public Map<String, Object> saveOssCheckName(List<ProjectIdentification> paramBeanList, String targetName) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<String> checkOssNameUrl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_NAME_URL);
			List<String> changeOssNameInfoList = new ArrayList<>();
			List<String> successIdList = new ArrayList<>();
			List<String> failIdList = new ArrayList<>();
			String referenceId = "";
			String referenceDiv = "";
			String commentId = "";
			
			for (ProjectIdentification paramBean : paramBeanList) {
				String rowId = paramBean.getGridId();
				String[] downloadLocations = paramBean.getDownloadLocation().split("<br>");
				
				// If there are multiple download paths > add
				if (downloadLocations.length > 0) {
					int updateCnt;
					int urlSearchSeq;
					int seq;

					for (String downloadLocation : downloadLocations) {
						updateCnt = 0;
						urlSearchSeq = -1;
						seq = 0;

						paramBean.setDownloadLocation(downloadLocation);

						for (String url : checkOssNameUrl) {
							if (urlSearchSeq == -1 && downloadLocation.contains(url)) {
								urlSearchSeq = seq;

								break;
							}

							seq++;
						}

						if ( urlSearchSeq > -1 ) {
							Pattern p = generatePattern(urlSearchSeq, downloadLocation);
							Matcher m = p.matcher(downloadLocation);
							while (m.find()) {
								paramBean.setDownloadLocation(m.group(0));
							}
						}

						List<String> componentIds = paramBean.getComponentIdList();
						switch(targetName.toUpperCase()) {
							case CoConstDef.CD_CHECK_OSS_SELF:
								for (String componentId : componentIds) {
									String[] gridId = componentId.split("-");
									paramBean.setGridId(gridId[0]+"-"+gridId[1]);
									paramBean.setComponentId(gridId[2]);
									updateCnt += ossMapper.updateOssCheckNameBySelfCheck(paramBean);
								}
								
								break;
							case CoConstDef.CD_CHECK_OSS_PARTNER:
								for (String componentId : componentIds) {
									paramBean.setComponentId(componentId);
									updateCnt += ossMapper.updateOssCheckNameByPartner(paramBean);
								}

								if (updateCnt >= 1) {
									commentId = paramBean.getRefPrjId();
									changeOssNameInfoList.add(avoidNull(paramBean.getOssName(), "N/A") + "|" + paramBean.getCheckName());
									
									if (isEmpty(commentId)) {
										referenceId = paramBean.getReferenceId();
										referenceDiv = CoConstDef.CD_DTL_COMMENT_PARTNER_HIS;
									}
								}

								break;
							case CoConstDef.CD_CHECK_OSS_IDENTIFICATION:
								for (String componentId : componentIds) {
									paramBean.setComponentId(componentId);
									updateCnt += ossMapper.updateOssCheckName(paramBean);
								}
								
								if (updateCnt >= 1) {
									commentId = paramBean.getReferenceId();
									changeOssNameInfoList.add(avoidNull(paramBean.getOssName(), "N/A") + "|" + paramBean.getCheckName());

									if (isEmpty(commentId)) {
										referenceId = paramBean.getRefPrjId();
										referenceDiv = CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS;
									}
								}

								break;
						}

						if (updateCnt >= 1) {
							successIdList.add(rowId);
						} else {
							failIdList.add(rowId);
						}
					}
				}
			}
			
			map.put("isValid", true);
			map.put("returnType", "Success");
			
			if (!CollectionUtils.isEmpty(changeOssNameInfoList)) {
				String checkOssNameComment = CommonFunction.changeDataToTableFormat("oss", CommonFunction.getCustomMessage("msg.common.change.name", "OSS Name"), changeOssNameInfoList);
				CommentsHistory commentInfo = null;
				
				if (isEmpty(commentId) && !isEmpty(referenceId) && !isEmpty(referenceDiv)) {
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(referenceDiv);
					commHisBean.setReferenceId(referenceId);
					commHisBean.setContents(checkOssNameComment);
					commHisBean.setStatus("pre-review > open source");
					commentInfo = commentService.registComment(commHisBean, false);
				} else {
					commentInfo = (CommentsHistory) commentService.getCommnetInfo(commentId).get("info");

					if (commentInfo != null) {
						if (!isEmpty(commentInfo.getContents())) {
							String contents = commentInfo.getContents();
							contents += checkOssNameComment;
							commentInfo.setContents(contents);
							commentInfo.setStatus("pre-review > open source");
							commentService.updateComment(commentInfo, false);
						}
					}
				}
				
				if (commentInfo != null) {
					map.put("commentId", commentInfo.getCommId());
				}
				if (!successIdList.isEmpty()) {
					map.put("successIds", successIdList);
				}
				if (!failIdList.isEmpty()) {
					map.put("failIds", failIdList);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			
			map.put("isValid", false);
			map.put("returnType", "");
		}
		
		return map;
	}

	@Transactional
	@Override
//	@CacheEvict(value="autocompleteCache", allEntries=true)
	public Map<String, Object> saveOss(OssMaster ossMaster) {
		String resCd = "00";
		String result = null;
		HashMap<String, Object> resMap = new HashMap<>();

		/*Json String -> Json Object*/
		String jsonString = ossMaster.getOssLicensesJson();
		if (!isEmpty(jsonString)) {
			Type collectionType = new TypeToken<List<OssLicense>>() {
			}.getType();
			List<OssLicense> list = checkLicenseId((List<OssLicense>) fromJson(jsonString, collectionType));
			ossMaster.setOssLicenses(list);
		}

		String action = "";
		String ossCommonId = ossMaster.getOssCommonId();
		String ossId = ossMaster.getOssId();
		boolean isNew = StringUtil.isEmpty(ossId);
		boolean isNewVersion = false; // 새로운 version을 등록
		boolean isChangedName = false;
		boolean isDeactivateFlag = false;
		boolean isActivateFlag = false;
		OssMaster beforeBean = null;
		OssMaster afterBean = null;

		// downloadLocations이 n건일때 0번째 값은 oss Master로 저장.
		String[] downloadLocations = ossMaster.getDownloadLocations();
		if (downloadLocations != null) {
			if (downloadLocations.length >= 1) {
				for (String url : downloadLocations) {
					if (!isEmpty(url)) {
						ossMaster.setDownloadLocation(url); // 등록된 url 중 공백을 제외한 나머지에서 첫번째 url을 만나게 되면 등록을 함.
						break;
					}
				}
			}
		} else {
			ossMaster.setDownloadLocation("");
		}

		if (!isEmpty(ossMaster.getHomepage())) {
			if (ossMaster.getHomepage().endsWith("/")) {
				String homepage = ossMaster.getHomepage();
				ossMaster.setHomepage(homepage.substring(0, homepage.length() - 1));
			} else {
				ossMaster.setHomepage(ossMaster.getHomepage());
			}
		} else {
			ossMaster.setHomepage("");
		}

		if (!isEmpty(ossMaster.getComment()) && !ossMaster.getComment().startsWith("<p>")) {
			ossMaster.setComment(CommonFunction.lineReplaceToBR(ossMaster.getComment()));
		}

		Map<String, List<OssMaster>> updateOssNameVersionDiffMergeObject = null;
		try {
			History h;
			if (CoConstDef.FLAG_YES.equals(avoidNull(ossMaster.getOssCopyFlag()))) {
				ossMaster.setOssId(null);
				isNew = true;
			}
			// OSS 수정
			if (!isNew) {
				beforeBean = getOssInfo(ossId, true);
				
				boolean updateNvdFlag = false;
				List<String> nicknames = null;
				String nickChangeComment = "";
				if (beforeBean.getOssNicknames() != null) {
					nicknames = Arrays.asList(beforeBean.getOssNicknames());
				}
				List<String> includeCpeList = ossMapper.selectOssIncludeCpeList(ossMaster);
				List<String> excludeCpeList = ossMapper.selectOssExcludeCpeList(ossMaster);
				
				List<String> newIncludeCpes = new ArrayList<>();
				if (ossMaster.getIncludeCpes() != null) {
					newIncludeCpes = Arrays.asList(ossMaster.getIncludeCpes());
				}
				List<String> newExcludeCpes = new ArrayList<>();
				if (ossMaster.getExcludeCpes() != null) {
					newExcludeCpes = Arrays.asList(ossMaster.getExcludeCpes());
				}
				List<String> newNicknames = null;
				if (ossMaster.getOssNicknames() != null) {
					newNicknames = Arrays.asList(ossMaster.getOssNicknames());
				}
				if (!CollectionUtils.isEmpty(nicknames) || !CollectionUtils.isEmpty(newNicknames)) {
					nickChangeComment = CommonFunction.getCommentForChangeNickname("", nicknames, newNicknames);
				}
				if (!Objects.equals(includeCpeList, newIncludeCpes) || !Objects.equals(excludeCpeList, newExcludeCpes) || !Objects.equals(nicknames, newNicknames)
						|| !avoidNull(ossMaster.getOssName()).equals(avoidNull(beforeBean.getOssName())) || !ossMaster.getOssVersion().equals(beforeBean.getOssVersion())) {
					updateNvdFlag = true;
				}
				if (CoConstDef.FLAG_YES.equals(ossMaster.getRenameFlag())) {
					updateOssNameVersionDiffMergeObject = updateOssNameVersionDiff(ossMaster);
				} else {
					result = registOssMaster(ossMaster);
				}
				if (!isEmpty(nickChangeComment)) {
					CommentsHistory commentsParam = new CommentsHistory();
					commentsParam.setReferenceId(ossMaster.getOssId());
					commentsParam.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_OSS_COMMON);
					commentsParam.setContents(nickChangeComment);
					commentService.registComment(commentsParam);
				}
//				if (updateNvdFlag) {
//					updateVulnInfoByOssMaster(ossMaster, true);
//				}
				
				CoCodeManager.getInstance().refreshOssInfo();
				action = CoConstDef.ACTION_CODE_UPDATE;
				afterBean = getOssInfo(ossId, true);

				if (CoConstDef.FLAG_YES.equals(ossMaster.getRenameFlag())) {
					if (updateOssNameVersionDiffMergeObject != null) {
						List<OssMaster> diffOssVersionMergeList = updateOssNameVersionDiffMergeObject.get("after");
						if (diffOssVersionMergeList != null && diffOssVersionMergeList.size() > 0) {
							afterBean.setOssNickname(diffOssVersionMergeList.get(0).getOssNickname());
							afterBean.setOssNicknames(diffOssVersionMergeList.get(0).getOssNicknames());
						}
					}
				}

				if (!beforeBean.getOssName().equalsIgnoreCase(afterBean.getOssName())) {
					isChangedName = true;
				}
				
				String beforeDeactivateFlag = avoidNull(beforeBean.getDeactivateFlag(), CoConstDef.FLAG_NO);
				String afterDeactivateFlag = avoidNull(afterBean.getDeactivateFlag(), CoConstDef.FLAG_NO);

				if (CoConstDef.FLAG_NO.equals(beforeDeactivateFlag)
						&& CoConstDef.FLAG_YES.equals(afterDeactivateFlag)) {
					isDeactivateFlag = true;
				}

				if (CoConstDef.FLAG_YES.equals(beforeDeactivateFlag)
						&& CoConstDef.FLAG_NO.equals(afterDeactivateFlag)) {
					isActivateFlag = true;
				}
			} else { // OSS 등록
				// 기존에 동일한 이름으로 등록되어 있는 OSS Name인 지 확인
				isNewVersion = CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossMaster.getOssName().toUpperCase());
				if (isNewVersion) {
					setExistedOssInfo(ossMaster);
				} else {
					ossMaster.setOssCommonId(null);
				}
				ossId = registOssMaster(ossMaster);
				
				// update vulnerability info
//				updateVulnInfoByOssMaster(ossMaster, false);
				
				CoCodeManager.getInstance().refreshOssInfo();
				action = CoConstDef.ACTION_CODE_INSERT;
			}

			if (!CoConstDef.FLAG_YES.equals(ossMaster.getRenameFlag())) {
				h = work(ossMaster);
				h.sethAction(action);
				historyService.storeData(h);
			}
			
			resCd = "10";
		} catch (RuntimeException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		} catch (Exception e) {
			log.error("OSS " + action + "Failed.", e);
			log.error(e.getMessage(), e);
		} finally {
			resMap.put("ossMaster", ossMaster);
			resMap.put("ossId", ossId);
			resMap.put("isNew", isNew);
			resMap.put("isNewVersion", isNewVersion);
			resMap.put("isChangedName", isChangedName);
			resMap.put("isDeactivateFlag", isDeactivateFlag);
			resMap.put("isActivateFlag", isActivateFlag);
			if (beforeBean != null) {
				resMap.put("beforeBean", beforeBean);
			}
			if (afterBean != null) {
				resMap.put("afterBean", afterBean);
			}
			if (updateOssNameVersionDiffMergeObject != null) {
				resMap.put("updateOssNameVersionDiffMergeObject", updateOssNameVersionDiffMergeObject);
			}
		}
		resMap.put("resCd", resCd);
		return resMap;
	}

	private void updateVulnInfoByOssMaster(OssMaster ossMaster, boolean delFlag) {
		if (delFlag) {
			vulnerabilityMapper.deleteOssVulnInfo(ossMaster.getOssId());
		}
		
		List<String> includeCpeList = null;
		if (ossMaster.getIncludeCpes() != null) {
			includeCpeList = new ArrayList<>(Arrays.asList(ossMaster.getIncludeCpes()));
		}
		List<String> excludeCpeList = null;
		if (ossMaster.getExcludeCpes() != null) {
			excludeCpeList = new ArrayList<>(Arrays.asList(ossMaster.getExcludeCpes()));
		}
		List<String> ossVersionAlias = new ArrayList<>();
		ossVersionAlias.add(isEmpty(ossMaster.getOssVersion()) ? "-" : ossMaster.getOssVersion());
		if (ossMaster.getOssVersionAliases() != null) {
			ossVersionAlias.addAll(Arrays.asList(ossMaster.getOssVersionAliases()));
		}
		boolean isNoVersion = isEmpty(ossMaster.getOssVersion()) ? true : false;
		
		List<String> includeCpeEnvironmentList = new ArrayList<>();
		List<String> excludeCpeEnvironmentList = new ArrayList<>();
		
		List<Map<String, Object>> includeVendorProductInfoList = null;
		List<Map<String, Object>> excludeVendorProductInfoList = null;
		OssMaster param = new OssMaster();
		param.setOssVersionAliases(ossVersionAlias.toArray(new String[ossVersionAlias.size()]));
		
		if (includeCpeList != null && !includeCpeList.isEmpty()) {
			generateIncludeCpeParam(param, includeCpeList, includeCpeEnvironmentList);
			includeVendorProductInfoList = vulnerabilityMapper.selectVendorProductByIncludeCpeInfo(param);
		}
		
		if (excludeCpeList != null && !excludeCpeList.isEmpty()) {
			generateExcludeCpeParam(param, excludeCpeList, excludeCpeEnvironmentList);
			excludeVendorProductInfoList = vulnerabilityMapper.selectVendorProductByExcludeCpeInfo(param);
		}
		
		List<Map<String, Object>> filteredVendorProductInfoList = new ArrayList<>();
		
		if (includeVendorProductInfoList != null && !includeVendorProductInfoList.isEmpty()) {
			if (excludeVendorProductInfoList != null && !excludeVendorProductInfoList.isEmpty()) {
				generateIncludeCpeMatchList(includeVendorProductInfoList, excludeVendorProductInfoList, includeCpeEnvironmentList, filteredVendorProductInfoList, isNoVersion);
			} else {
				generateIncludeCpeMatchList(includeVendorProductInfoList, null, includeCpeEnvironmentList, filteredVendorProductInfoList, isNoVersion);
			}
		}
		
		Map<String, Object> maxScoreVulnMap = null;
		if (filteredVendorProductInfoList != null && !filteredVendorProductInfoList.isEmpty()) {
			Collections.sort(filteredVendorProductInfoList, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					if (new BigDecimal(o1.get("CVSS_SCORE").toString()).compareTo(new BigDecimal(o2.get("CVSS_SCORE").toString())) > 0) {
						return -1;
					}else {
						return 1;
					}
				}
			});
			
			maxScoreVulnMap = filteredVendorProductInfoList.get(0);
			ossMaster.setInCpeMatchFlag(CoConstDef.FLAG_YES);
		} else {
			List<String> inCpeMatchList = ossMapper.selectIncludeCpeMatchOssInfo();
			
			if (!isEmpty(ossMaster.getOssVersion())) {
				List<Map<String, Object>> nvdInfoList = vulnerabilityMapper.selectNvdInfo(ossMaster);
				if (nvdInfoList != null && !nvdInfoList.isEmpty()) {
					List<Map<String, Object>> filteredNvdInfoList = new ArrayList<>();
					for (Map<String, Object> nvdInfo : nvdInfoList) {
						String cveId = (String) nvdInfo.get("CVE_ID");
						if (!inCpeMatchList.contains(cveId)) {
							filteredNvdInfoList.add(nvdInfo);
						}
					}
					
					if (filteredNvdInfoList != null && !filteredNvdInfoList.isEmpty()) {
						Collections.sort(filteredNvdInfoList, new Comparator<Map<String, Object>>() {
							@Override
							public int compare(Map<String, Object> o1, Map<String, Object> o2) {
								if (new BigDecimal(o1.get("CVSS_SCORE").toString()).compareTo(new BigDecimal(o2.get("CVSS_SCORE").toString())) > 0) {
									return -1;
								}else {
									return 1;
								}
							}
						});
						
						maxScoreVulnMap = filteredNvdInfoList.get(0);
						ossMaster.setInCpeMatchFlag(CoConstDef.FLAG_YES);
					}
				}
			} else {
				List<Map<String, Object>> nvdInfoList = vulnerabilityMapper.selectNvdInfoWithOutVer(ossMaster);
				if (nvdInfoList != null && !nvdInfoList.isEmpty()) {
					Map<String, Object> nvdInfo = nvdInfoList.get(0);
					String cveId = (String) nvdInfo.get("CVE_ID");
					if (!inCpeMatchList.contains(cveId)) {
						maxScoreVulnMap = nvdInfo;
					}
				}
			}
		}
		
		if (maxScoreVulnMap != null) {
			ossMaster.setCveId((String) maxScoreVulnMap.get("CVE_ID"));
			Float cvssScore = (Float) maxScoreVulnMap.get("CVSS_SCORE");
			ossMaster.setCvssScore(String.valueOf(cvssScore));
			vulnerabilityMapper.updateOssVulnInfoNew(ossMaster);
		}
	}

	private void generateIncludeCpeMatchList(List<Map<String, Object>> includeVendorProductInfoList, List<Map<String, Object>> excludeVendorProductInfoList
			, List<String> includeCpeEnvironmentList, List<Map<String, Object>> filteredVendorProductInfoList, boolean isNoVersion) {
		List<String> filteredKeyList = null;
		boolean excludeListFlag = false;
		
		if (excludeVendorProductInfoList != null) {
			excludeListFlag = true;
			filteredKeyList = new ArrayList<>();
			for (Map<String, Object> map : excludeVendorProductInfoList) {
				String version = isNoVersion ? "-" : (String) map.get("VERSION");
				filteredKeyList.add(((String) map.get("PRODUCT") + "|" + version + "|" + (String) map.get("VENDOR") + "|" + (String) map.get("CVE_ID")).toUpperCase());
			}
		}

		for (Map<String, Object> includeVendorProductInfo : includeVendorProductInfoList) {
			if (excludeListFlag) {
				String version = isNoVersion ? "-" : (String)includeVendorProductInfo.get("VERSION");
				String key = (String)includeVendorProductInfo.get("PRODUCT") + "|" + version + "|" + (String)includeVendorProductInfo.get("VENDOR") + "|" + (String)includeVendorProductInfo.get("CVE_ID");
				if (!filteredKeyList.contains(key.toUpperCase())) {
					checkIncludeCpeEnvironment(includeVendorProductInfo, includeCpeEnvironmentList, filteredVendorProductInfoList);
				}
			} else {
				checkIncludeCpeEnvironment(includeVendorProductInfo, includeCpeEnvironmentList, filteredVendorProductInfoList);
			}
		}
	}

	private void checkIncludeCpeEnvironment(Map<String, Object> includeVendorProductInfo, List<String> includeCpeEnvironmentList, List<Map<String, Object>> filteredVendorProductInfoList) {
		String criteria = (String) includeVendorProductInfo.get("CRITERIA");
		String[] criteriaSplit = criteria.split("[:]");
		
		int cpeIdx = 0;
		String criteriaEnvironment = "";
		
		for (String cpe : criteriaSplit) {
			if (cpeIdx != 5) {
				criteriaEnvironment += cpe;
				if (cpeIdx != criteriaSplit.length-1) criteriaEnvironment += ":";
			}
			cpeIdx++;
		}
		
		if (includeCpeEnvironmentList != null && !includeCpeEnvironmentList.isEmpty()) {
			if (includeCpeEnvironmentList.contains(criteriaEnvironment)) {
				filteredVendorProductInfoList.add(includeVendorProductInfo);
			}
		} else {
			filteredVendorProductInfoList.add(includeVendorProductInfo);
		}
	}

	private void generateExcludeCpeParam(OssMaster ossMaster, List<String> excludeCpeList, List<String> excludeCpeEnvironmentList) {
		List<String> excludeCpes = new ArrayList<>();
		String excludeCpeString = "\"";
		int idx = 0;
		for (String excludeCpe : excludeCpeList) {
			int index = 0;
			index = excludeCpe.indexOf("*");
			
			if (excludeCpe.startsWith("cpe:2.3:a") || excludeCpe.startsWith("cpe:2.3:h")) {
				int stringLength = excludeCpe.length();
				int colonLength = excludeCpe.replace(":", "").length();
				if (stringLength - colonLength == 12) {
					String[] excludeCpeSplit = excludeCpe.split("[:]");
					int cpeIdx = 0;
					String excludeCpeEnvironment = "";
					for (String cpe : excludeCpeSplit) {
						if (cpeIdx != 5) {
							excludeCpeEnvironment += cpe;
							if (cpeIdx != excludeCpeSplit.length-1) excludeCpeEnvironment += ":";
						}
						cpeIdx++;
					}
					excludeCpeEnvironmentList.add(excludeCpeEnvironment);
				}
			}
			
			if (index > -1) excludeCpe = excludeCpe.substring(0, index-1);
			if (excludeCpe.startsWith("cpe:2.3:a")) excludeCpe = excludeCpe.replace("cpe:2.3:a", "");
			if (excludeCpe.startsWith("cpe:2.3:h")) excludeCpe = excludeCpe.replace("cpe:2.3:h", "");
			excludeCpeString += excludeCpe;
			excludeCpes.add(excludeCpe);
			if (idx != excludeCpeList.size()-1) {
				excludeCpeString += "\" \"";
			}
			idx++;
		}
		excludeCpeString += "\"";
		ossMaster.setExcludeCpe(excludeCpeString);
		ossMaster.setExcludeCpes(excludeCpes.toArray(new String[excludeCpes.size()]));
	}

	private void generateIncludeCpeParam(OssMaster ossMaster, List<String> includeCpeList, List<String> includeCpeEnvironmentList) {
		ossMaster.setExcludeCpe(null);
		ossMaster.setExcludeCpes(null);
		
		List<String> includeCpes = new ArrayList<>();
		String includeCpeString = "\"";
		
		int idx = 0;
		for (String includeCpe : includeCpeList) {
			int index = 0;
			index = includeCpe.indexOf("*");
			
			if (includeCpe.startsWith("cpe:2.3:a") || includeCpe.startsWith("cpe:2.3:h")) {
				int stringLength = includeCpe.length();
				int colonLength = includeCpe.replace(":", "").length();
				if (stringLength - colonLength == 12) {
					String[] includeCpeSplit = includeCpe.split("[:]");
					int cpeIdx = 0;
					String includeCpeEnvironment = "";
					for (String cpe : includeCpeSplit) {
						if (cpeIdx != 5) {
							includeCpeEnvironment += cpe;
							if (cpeIdx != includeCpeSplit.length-1) includeCpeEnvironment += ":";
						}
						cpeIdx++;
					}
					includeCpeEnvironmentList.add(includeCpeEnvironment);
				}
			}
			
			if (index > -1) includeCpe = includeCpe.substring(0, index-1);
			if (includeCpe.startsWith("cpe:2.3:a:")) includeCpe = includeCpe.replace("cpe:2.3:a:", "");
			if (includeCpe.startsWith("cpe:2.3:h:")) includeCpe = includeCpe.replace("cpe:2.3:h:", "");
			includeCpeString += includeCpe;
			includeCpes.add(includeCpe);
			if (idx != includeCpeList.size()-1) {
				includeCpeString += "\" \"";
			}
			idx++;
		}
		includeCpeString += "\"";
		ossMaster.setIncludeCpe(includeCpeString);
		ossMaster.setIncludeCpes(includeCpes.toArray(new String[includeCpes.size()]));
	}

	@Transactional
	@Override
	public Map<String, Object> saveOssNickname(ProjectIdentification paramBean) {
		Map<String, Object> map = new HashMap<String, Object>();
		OssMaster ossMaster = new OssMaster();
		ossMaster.setOssName(paramBean.getCheckName());
		ossMaster.setOssNickname(paramBean.getOssName());
		
		try {
			if (isEmpty(ossMaster.getOssNickname())) {
				throw new Exception(ossMaster.getOssName() + " -> NickName field is required.");
			}
			
			List<String> ossNameList = new ArrayList<>();
			ossNameList.add(paramBean.getOssName());
			String[] ossNames = new String[ossNameList.size()];
			ossMaster.setOssNames(ossNameList.toArray(ossNames));
			
			Map<String, OssMaster> ossMap = getBasicOssInfoList(ossMaster);
			
			if (ossMap == null || ossMap.isEmpty()) {
				OssMaster bean = ossMapper.checkExistsOssname(ossMaster);
				if (bean != null) {
					ossMaster.setOssCommonId(bean.getOssCommonId());
				} else {
					throw new Exception(ossMaster.getOssName() + " -> OSS Name registered in OSS list.");
				}
			} else {
				throw new Exception(paramBean.getOssName() + " -> OSS Name registered in OSS list.");
			}
			
			int insertCnt = ossMapper.insertOssNickname(ossMaster);
			
			if (insertCnt == 1) {
				map.put("isValid", true);
				map.put("returnType", "Success");
			} else {
				throw new Exception("update Cnt가 비정상적인 값임.");
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			
			map.put("isValid", false);
			map.put("returnType", e.getMessage());
		}
		
		return map;
	}

	public List<OssLicense> checkLicenseId(List<OssLicense> list) {
		// license name만 있고 id는 없는 경우를 우해 license id를 찾는다.
		// validation에서 license id는 필수로 되어 있기때문
		if (list != null) {
			for (OssLicense bean : list) {
				if (isEmpty(bean.getLicenseId()) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(bean.getLicenseNameEx().toUpperCase())) {
					bean.setLicenseId(CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseNameEx().toUpperCase()).getLicenseId());

					if (isEmpty(bean.getLicenseName())) {
						bean.setLicenseName(CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseNameEx().toUpperCase()).getLicenseName());
					}
				}
			}
		}

		return list;
	}

	@Transactional
	@Override
	public Map<String, Object> saveOssAnalysisList(OssMaster ossBean, String key) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			switch(key) {
				case "VIEW":
					String prjId = ossBean.getPrjId();
					if (CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(ossBean.getReferenceDiv())) {
						prjId = "3rd_" + prjId;
					}
					
					int analysisListCnt = ossMapper.ossAnalysisListCnt(prjId, ossBean.getStartAnalysisFlag(), ossBean.getCsvComponentIdList());
					
					if (analysisListCnt > 0) {
						ossMapper.deleteOssAnalysisList(prjId);
					}
					
					if (ossBean.getComponentIdList().size() > 0) {
						int insertCnt = ossMapper.insertOssAnalysisList(ossBean);
						
						if (insertCnt <= 0) {
							result.put("isValid", false);
							result.put("returnType", "Failed");
						}
					}
					
					break;
				case "POPUP":
					int updateCnt = ossMapper.updateOssAnalysisList(ossBean);
					
					if (updateCnt != 1) {
						result.put("isValid", false);
						result.put("returnType", "Failed");
					}
					
					break;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		if (result.keySet().size() == 0) {
			result.put("isValid", true);
			result.put("returnType", "Success");
		}
		
		return result;
	}
	
	@Override
	public Map<String, Object> getOssAnalysisList(OssMaster ossMaster) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<OssAnalysis> list = null;
		String prjId = ossMaster.getPrjId();
		
		if (CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(ossMaster.getReferenceDiv())) {
			ossMaster.setPrjId("3rd_" + prjId);
		}
		
		if (CoConstDef.FLAG_YES.equals(ossMaster.getStartAnalysisFlag())) {
			int records = ossMapper.ossAnalysisListCnt(ossMaster.getPrjId(), ossMaster.getStartAnalysisFlag(), ossMaster.getCsvComponentIdList());
			ossMaster.setTotListSize(records);
			
			list = ossMapper.selectOssAnalysisList(ossMaster);
			
			result.put("page", ossMaster.getCurPage());
			result.put("total", ossMaster.getTotBlockSize());
			result.put("records", records);
		}
		
		if (!CoConstDef.FLAG_YES.equals(ossMaster.getStartAnalysisFlag())) {
			list = ossMapper.selectOssAnalysisList(ossMaster);
			CommonFunction.getAnalysisValidation(result, list);
		}
		
		result.put("rows", list);
		
		return result;
	}
	
	@Override
	public int getAnalysisListPage(int rows, String prjId) {
		try {
			return ossMapper.getAnalysisListPage(rows, prjId);
		} catch (Exception e) {
			return 1;
		}
	}
	
	@Override
	public Map<String, Object> startAnalysis(String prjId, String fileSeq, String userName){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		T2File fileInfo = new T2File();
		
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		BufferedReader error = null;
		Process process = null;
		boolean isProd = "REAL".equals(avoidNull(CommonFunction.getProperty("server.mode")));
		
		try {			
			oss_auto_analysis_log.info("ANALYSIS START PRJ ID : "+prjId+" ANALYSIS file ID : " + fileSeq);
			
			fileInfo = fileMapper.selectFileInfo(fileSeq);

			String loginUserName = userName != null ? userName : loginUserName();
			T2Users user = new T2Users();
			user.setUserId(loginUserName);
			user = userMapper.getUser(user);
			String EMAIL_VAL = user.getEmail();

			String analysisCommand = MessageFormat.format(CommonFunction.getProperty("autoanalysis.ssh.command"), (isProd ? "live" : "dev"), prjId, fileInfo.getLogiNm(), EMAIL_VAL, (isProd ? 0 : 1), 1);
			
			ProcessBuilder builder = new ProcessBuilder( "/bin/bash", "-c", analysisCommand );
			
			builder.redirectErrorStream(true);
		
			process = builder.start();
			oss_auto_analysis_log.info("ANALYSIS Process PRJ ID : " + prjId + " command : " + analysisCommand);
			
			is = process.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			
			int count = 0;
			int interval = 1000; // 1 sec
			int idleTime = Integer.parseInt(CoCodeManager.getCodeExpString(CoConstDef.CD_AUTO_ANALYSIS, CoConstDef.CD_IDLE_TIME));

			Project prjInfo = new Project();
			prjInfo.setPrjId(prjId);

			// script가 success일때 status를 progress로 변경함.
			setOssAnalysisStatus(prjId);

			prjInfo = projectMapper.getOssAnalysisData(prjInfo);

			resultMap.put("isValid", true);
			resultMap.put("returnMsg", "Success");
			resultMap.put("prjInfo", prjInfo);

			Thread.sleep(interval);
			
			/*while (!Thread.currentThread().isInterrupted()) {
				if (count > idleTime) {
					oss_auto_analysis_log.info("ANALYSIS TIMEOUT PRJ ID : " + prjId);
					resultMap.put("isValid", false);
					resultMap.put("returnMsg", "OSS auto analysis has not been completed yet.");
					
					break;
				}
				
				String result = br.readLine();
				oss_auto_analysis_log.info("OSS AUTO ANALYSIS READLINE : " + result);
				
				if (result != null && result.toLowerCase().indexOf("start download oss") > -1) {
					oss_auto_analysis_log.info("ANALYSIS START SUCCESS PRJ ID : " + prjId);
					Project prjInfo = new Project();
					prjInfo.setPrjId(prjId);
					
					// script가 success일때 status를 progress로 변경함.
					OssMaster ossBean = new OssMaster();
					ossBean.setPrjId(prjId);
					ossBean.setCreator(loginUserName());
					ossMapper.setOssAnalysisStatus(ossBean);
					
					prjInfo = projectMapper.getOssAnalysisData(prjInfo);
					
					resultMap.put("isValid", true);
					resultMap.put("returnMsg", "Success");
					resultMap.put("prjInfo", prjInfo);
					
					break;
				}
				
				count++;
				
				Thread.sleep(interval);
			}*/
			// 스크립트 종료
		} catch(NullPointerException npe) {
			oss_auto_analysis_log.error("ANALYSIS ERR PRJ ID : " + prjId);
			oss_auto_analysis_log.error(npe.getMessage(), npe);
			
			resultMap.replace("isValid", false);
			resultMap.replace("returnMsg", "script Error");
		} catch (Exception e) {
			oss_auto_analysis_log.error("ANALYSIS ERR PRJ ID : " + prjId);
			oss_auto_analysis_log.error(e.getMessage(), e);
			
			resultMap.replace("isValid", false);
			resultMap.replace("returnMsg", "OSS auto analysis has not been completed yet.");
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e2) {}
			}
			
			if (error != null) {
				try {
					error.close();
				} catch (Exception e2) {}
			}
			
			if (isr != null) {
				try {
					isr.close();
				} catch (Exception e2) {}
			}
			
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {}
			}
			
			if (error != null) {
				try {
					error.close();
				} catch (Exception e2) {}
			}
			
			try {
				if (process != null) {
					oss_auto_analysis_log.info("Do OSS ANALYSIS Process Destry");
					process.destroy();
				}
			} catch (Exception e2) {
				oss_auto_analysis_log.error(e2.getMessage(), e2);
			}
		}
		
		return resultMap;
	}

	@Override
	public OssAnalysis getNewestOssInfo2(OssAnalysis bean) {
		OssAnalysis ossNewistData = ossMapper.getNewestOssInfo2(bean);

		if (ossNewistData != null) {
			if (!isEmpty(ossNewistData.getDownloadLocationGroup())) {
				String url = "";

				String[] downloadLocationList = ossNewistData.getDownloadLocationGroup().split(",");
				// master table에 download location이 n건인 경우에 대해 중복제거를 추가함.
				String duplicateRemoveUrl =  String.join(",", Arrays.asList(downloadLocationList)
						.stream()
						.filter(CommonFunction.distinctByKey(p -> p))
						.collect(Collectors.toList()));

				if (!isEmpty(duplicateRemoveUrl)) {
					url = duplicateRemoveUrl;
				}

				ossNewistData.setDownloadLocation(url);
			}
		}

		return ossNewistData;
	}
	
	@Override
	public OssAnalysis getNewestOssInfo(OssAnalysis bean) {		
		OssAnalysis ossNewistData = ossMapper.getNewestOssInfo(bean);
		
		if (ossNewistData != null) {
			if (!isEmpty(ossNewistData.getDownloadLocationGroup())) {
				String url = "";
				
				String[] downloadLocationList = ossNewistData.getDownloadLocationGroup().split(",");
				// master table에 download location이 n건인 경우에 대해 중복제거를 추가함.
				String duplicateRemoveUrl =  String.join(",", Arrays.asList(downloadLocationList)
														.stream()
														.filter(CommonFunction.distinctByKey(p -> p))
														.collect(Collectors.toList()));
				
				if (!isEmpty(duplicateRemoveUrl)) {
					url = duplicateRemoveUrl;
				}
				
				ossNewistData.setDownloadLocation(url);
			}
			
			if (ossNewistData.getLicenseName() != null && !ossNewistData.getLicenseName().contains(" OR ")) {
				ProjectIdentification prjOssMaster = new ProjectIdentification();
				prjOssMaster.setOssId(ossNewistData.getOssId());
				List<ProjectIdentification> Licenselist = projectMapper.getLicenses(prjOssMaster);
				if (Licenselist.size() != 0){
					Licenselist = CommonFunction.makeLicenseExcludeYn(Licenselist);
					ossNewistData.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(Licenselist, ","));
				}
			}
			
			ossNewistData.setOssId(null);
		}
		
		return ossNewistData;
	}
	
	@Override
	public Map<String,Object> updateAnalysisComplete(OssAnalysis bean) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
	
		int updateCnt = ossMapper.updateAnalysisComplete(bean);
		
		if (updateCnt == 1) {
			resultMap.put("isValid", true);
			resultMap.put("returnMsg", "Success");
		} else {
			throw new Exception("Complete Failure");
		}
		
		return resultMap;
	}

	@Override
	public OssAnalysis getAutoAnalysisSuccessOssInfo(String referenceOssId) {
		return ossMapper.getAutoAnalysisSuccessOssInfo(referenceOssId);
	}
	
	@Override
	public List<ProjectIdentification> checkOssNameData(List<ProjectIdentification> componentData, Map<String, String> validMap, Map<String, String> diffMap){
		List<ProjectIdentification> resultData = new ArrayList<ProjectIdentification>();
		Map<String, Object> ruleMap = T2CoValidationConfig.getInstance().getRuleAllMap();
		
		if (validMap != null) {
			for (String key : validMap.keySet()) {
				if (key.toUpperCase().startsWith("OSSNAME") 
						&& (validMap.get(key).equals(ruleMap.get("OSS_NAME.UNCONFIRMED.MSG"))
								|| validMap.get(key).equals(ruleMap.get("OSS_NAME.DEACTIVATED.MSG"))
								|| validMap.get(key).equals(ruleMap.get("OSS_NAME.REQUIRED.MSG")))) {
					resultData.addAll((List<ProjectIdentification>) componentData
																	.stream()
																	.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
																	.collect(Collectors.toList()));
				}
				
				if (key.toUpperCase().startsWith("OSSVERSION") && validMap.get(key).equals(ruleMap.get("OSS_VERSION.UNCONFIRMED.MSG"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
																	.stream()
																	.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
																	.collect(Collectors.toList()));
				}
			}
		}
		
		if (diffMap != null) {
			for (String key : diffMap.keySet()) {
				if (key.toUpperCase().startsWith("OSSNAME") && diffMap.get(key).equals(ruleMap.get("OSS_NAME.UNCONFIRMED.MSG"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
																	.stream()
																	.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
																	.collect(Collectors.toList()));
				}
				
				if (key.toUpperCase().startsWith("OSSVERSION") && diffMap.get(key).equals(ruleMap.get("OSS_VERSION.UNCONFIRMED.MSG"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
																	.stream()
																	.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
																	.collect(Collectors.toList()));
				}
				
				if (key.toUpperCase().startsWith("DOWNLOADLOCATION") && diffMap.get(key).equals(ruleMap.get("DOWNLOAD_LOCATION.DIFFERENT.MSG"))) {
					int duplicateRow = (int) resultData
												.stream()
												.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
												.collect(Collectors.toList())
												.size();
					
					if (duplicateRow == 0) {
						resultData.addAll((List<ProjectIdentification>) componentData
																		.stream()
																		.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
																		.collect(Collectors.toList()));
					}
				}
			}
		}
		
		if (validMap == null && diffMap == null) {
			resultData.addAll(componentData.stream().filter(e -> e.getOssName().equals("-")).collect(Collectors.toList()));
		}
		
		return resultData;
	}
	
	private Map<String, OssMaster> getNewestOssInfoOnTime(OssMaster ossMaster) {
		List<OssMaster> list = ossMapper.getNewestOssInfoByOssMaster(ossMaster);
		return makeBasicOssInfoMap(list, true, false);
	}
	
	@Override
	public List<OssMaster> getOssListBySync(OssMaster bean) {
		List<OssMaster> list = ossMapper.getOssListByName(bean);
		
		if (list == null) {
			list = new ArrayList<>();
		}
		
		// oss id로 취합(라이선스 정보)
		List<OssMaster> newList = new ArrayList<>();
		Map<String, OssMaster> remakeMap = new HashMap<>();
		OssMaster currentBean = null;
		for (OssMaster ossBean : list) {
			// name + version
			if (!isEmpty(ossBean.getOssVersion())) {
				ossBean.setOssNameVerStr(ossBean.getOssName() + " (" + ossBean.getOssVersion() + ")");
			} else {
				ossBean.setOssNameVerStr(ossBean.getOssName());
			}
			
			if (remakeMap.containsKey(ossBean.getOssId())) {
				currentBean = remakeMap.get(ossBean.getOssId());
			} else {
				currentBean = ossBean;
				
				if (!isEmpty(currentBean.getOssNickname())) {
					currentBean.setOssNickname(currentBean.getOssNickname().replaceAll("\\|", "<br>"));
				}
				
				currentBean.setCopyright(CommonFunction.lineReplaceToBR(ossBean.getCopyright()));
				currentBean.setSummaryDescription(CommonFunction.lineReplaceToBR(ossBean.getSummaryDescription()));
				currentBean.setAttribution(CommonFunction.lineReplaceToBR(ossBean.getAttribution()));
			}
			
			List<OssMaster> ossDownloadLocation = ossMapper.selectOssDownloadLocationList(ossBean);
			if (!CollectionUtils.isEmpty(ossDownloadLocation)) {
				StringBuilder sb = new StringBuilder();
						
				for (OssMaster location : ossDownloadLocation) {
					sb.append(location.getDownloadLocation()).append(",");
				}
							
				String[] ossDownloadLocations = new String(sb).split("[,]");
				currentBean.setDownloadLocations(ossDownloadLocations);
			}
			
			OssLicense licenseBean = new OssLicense();
			licenseBean.setLicenseId(ossBean.getLicenseId());
			licenseBean.setOssLicenseIdx(ossBean.getOssLicenseIdx());
			licenseBean.setLicenseName(ossBean.getLicenseName());
			licenseBean.setOssLicenseComb(ossBean.getOssLicenseComb());
			licenseBean.setOssLicenseText(CommonFunction.lineReplaceToBR(ossBean.getOssLicenseText()));
			licenseBean.setOssCopyright(CommonFunction.lineReplaceToBR(ossBean.getOssCopyright()));
			
			currentBean.addOssLicense(licenseBean);

			if (remakeMap.containsKey(ossBean.getOssId())) {
				remakeMap.replace(ossBean.getOssId(), currentBean);
			} else {
				remakeMap.put(ossBean.getOssId(), currentBean);
			}
			
			if (ossBean.getRestriction() != null) {
				T2CodeDtl t2CodeDtl = new T2CodeDtl();
				List<T2CodeDtl> t2CodeDtlList = new ArrayList<>(); 
				t2CodeDtl.setCdNo(CoConstDef.CD_LICENSE_RESTRICTION);
				try {
					t2CodeDtlList = codeMapper.selectCodeDetailList(t2CodeDtl);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				List<String> restrictionList = Arrays.asList(ossBean.getRestriction().split(","));
				List<String> restrictionCdNoList = new ArrayList<>();
				String restrictionStr = "";
				
				for (T2CodeDtl item: t2CodeDtlList){
					if (restrictionList.contains(item.getCdDtlNo())) {
						restrictionStr += (!isEmpty(restrictionStr) ? ", " : "") + item.getCdDtlNm();
						restrictionCdNoList.add(item.getCdDtlNo());
					}
				}
				
				ossBean.setRestriction(restrictionStr);
				ossBean.setRestrictionCdNoList(restrictionCdNoList);
			}
		}
		
		for (OssMaster ossBean : remakeMap.values()) {
			ossBean.setLicenseName(CommonFunction.makeLicenseExpression(ossBean.getOssLicenses()));
			newList.add(ossBean);
		}
		
		return newList;
	}

	@Override
	public List<String> getOssListSyncCheck(List<OssMaster> selectOssList, List<OssMaster> standardOssList) {
		OssMaster standardOss = standardOssList.get(0);
		OssMaster selectOss = selectOssList.get(0);
		
		List<String> checkList = new ArrayList<String>();
				
		if (!Arrays.equals(standardOss.getOssLicenses().toArray(), selectOss.getOssLicenses().toArray())) {
			if (!standardOss.getLicenseName().equals(selectOss.getLicenseName())) {
				checkList.add("Declared License");
			}
		}
		if (!Arrays.equals(standardOss.getDetectedLicenses().toArray(), selectOss.getDetectedLicenses().toArray())) {
			if (!standardOss.getDetectedLicense().equals(selectOss.getDetectedLicense())) {
				checkList.add("Detected License");
			}
		}
		if (!avoidNull(standardOss.getCopyright(), "").equals(avoidNull(selectOss.getCopyright(), ""))) {
			checkList.add("Copyright");
		}
		if (standardOss.getDownloadLocations() != null) {
			if (selectOss.getDownloadLocations() != null) {
				if (!Arrays.equals(Arrays.asList(standardOss.getDownloadLocations()).toArray(), Arrays.asList(selectOss.getDownloadLocations()).toArray())) {
					checkList.add("Download Location");
				}
			} else {
				checkList.add("Download Location");
			}
		} else {
			if (selectOss.getDownloadLocations() != null) {
				checkList.add("Download Location");
			}
		}
		if (!avoidNull(standardOss.getHomepage(), "").equals(avoidNull(selectOss.getHomepage(), ""))) {
			checkList.add("Home Page");
		}
		if (!avoidNull(standardOss.getSummaryDescription(), "").equals(avoidNull(selectOss.getSummaryDescription(), ""))) {
			checkList.add("Summary Description");
		}
		if (!avoidNull(standardOss.getAttribution(), "").equals(avoidNull(selectOss.getAttribution(), ""))) {
			checkList.add("Attribution");
		}
		
		return checkList;
	}

	@Override
	public void syncOssMaster(OssMaster ossMaster, boolean declaredLicenseCheckFlag, boolean detectedLicenseCheckFlag, boolean restrictionCheckFlag) {
		ossMaster.setModifier(ossMaster.getLoginUserName());
		checkOssLicenseAndObligation(ossMaster);

		ossMapper.updateOssMasterSync(ossMaster);

		if (declaredLicenseCheckFlag) {
			ossMapper.deleteOssLicenseDeclaredSync(ossMaster);

			List<OssLicense> list = ossMaster.getOssLicenses();
			int ossLicenseDeclaredIdx = 0;
			String licenseId = "";
			
			for (OssLicense license : list) {
				ossLicenseDeclaredIdx++;
				licenseId = CommonFunction.getLicenseIdByName(license.getLicenseName());
				
				OssMaster om = new OssMaster(
					  Integer.toString(ossLicenseDeclaredIdx)
					, ossMaster.getOssId()
					, licenseId
					, license.getLicenseName()
					, ossLicenseDeclaredIdx == 1 ? "" : license.getOssLicenseComb()
//					, license.getOssLicenseText()
					, license.getOssCopyright()
					, ossMaster.getLicenseDiv()
				);

				ossMapper.insertOssLicenseDeclared(om);
			}

			updateLicenseDivDetail(ossMaster);
		}

		if (detectedLicenseCheckFlag) {
			ossMapper.deleteOssLicenseDetectedSync(ossMaster);

			List<String> detectedLicenses = ossMaster.getDetectedLicenses().stream().distinct().collect(Collectors.toList());

			if (detectedLicenses != null) {
				int ossLicenseDetectedIdx = 0;

				for (String detectedLicense : detectedLicenses) {
					if (!isEmpty(detectedLicense)) {
						LicenseMaster detectedLicenseInfo = CoCodeManager.LICENSE_INFO_UPPER.get(detectedLicense.toUpperCase());

						OssMaster om = new OssMaster(
							  ossMaster.getOssId() // ossId
							, detectedLicenseInfo.getLicenseId() // licenseId
							, Integer.toString(++ossLicenseDetectedIdx) // ossLicenseIdx
						);

						ossMapper.insertOssLicenseDetected(om);
					}
				}
			}
		}

		if (!isEmpty(avoidNull(ossMaster.getComment()).trim())) {
			CommentsHistory param = new CommentsHistory();
			param.setReferenceId(ossMaster.getOssId());
			param.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_OSS);
			param.setContents(ossMaster.getComment());

			commentService.registComment(param);
		}

		// Deactivate Flag Setting
		if (isEmpty(ossMaster.getDeactivateFlag())) {
			ossMaster.setDeactivateFlag(CoConstDef.FLAG_NO);
		}

		ossMapper.setDeactivateFlag(ossMaster);
	}

	@Override
	public OssMaster makeEmailSendFormat(OssMaster bean) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		for (String downloadLocation : bean.getDownloadLocations()) {
			if (downloadLocation.contains("|")) {
				sb.append(downloadLocation.split("[|]")[0]).append(",");
				sb1.append(downloadLocation.split("[|]")[1]).append(",");
			} else {
				sb.append(downloadLocation);
			}
		}
		String[] ossDownloadLocations = new String(sb).split("[,]");
		bean.setDownloadLocations(ossDownloadLocations);
		bean.setPurl(sb1.toString());
		
		bean.setLicenseName(CommonFunction.makeLicenseExpression(bean.getOssLicenses()));
		bean.setOssLicenses(CommonFunction.changeLicenseNameToShort(bean.getOssLicenses()));

		if (!isEmpty(bean.getLicenseDiv())) {
			bean.setMultiLicenseFlag(bean.getLicenseDiv());
			bean.setLicenseDiv(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_DIV, bean.getLicenseDiv()));
		}

		if (!isEmpty(bean.getLicenseType())) {
			bean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, bean.getLicenseType()));
		}

		if (!isEmpty(bean.getObligationType())) {
			bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, bean.getObligationType()));
		}

		if (!isEmpty(bean.getModifiedDate())) {
			bean.setModifiedDate(DateUtil.dateFormatConvert(bean.getModifiedDate(), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
		}

		if (!isEmpty(bean.getModifier())) {
			bean.setModifier(CoMailManager.getInstance().makeUserNameFormat(bean.getModifier()));
		}

		bean.setAttribution(CommonFunction.lineReplaceToBR(bean.getAttribution()));
		bean.setSummaryDescription(CommonFunction.lineReplaceToBR(bean.getSummaryDescription()));
		bean.setCopyright(CommonFunction.lineReplaceToBR(bean.getCopyright()));

		return bean;
	}

	@Override
	public String checkOssVersionDiff(OssMaster ossMaster) {
		boolean ossVersion_Flag = false;
		boolean isNew = StringUtil.isEmpty(ossMaster.getOssId());
		
		if (!isNew) {
			OssMaster beforeOss = CoCodeManager.OSS_INFO_BY_ID.get(ossMaster.getOssId());
			
			List<String> ossNameList = new ArrayList<>();
			ossNameList.add(beforeOss.getOssName().trim());
			String[] ossNames = new String[ossNameList.size()];
			beforeOss.setOssNames(ossNameList.toArray(ossNames));
			
			Map<String, OssMaster> beforeOssMap = getBasicOssInfoList(beforeOss);
			
			ossNameList = new ArrayList<>();
			ossNameList.add(ossMaster.getOssName().trim());
			ossNames = new String[ossNameList.size()];
			ossMaster.setOssNames(ossNameList.toArray(ossNames));
			
			Map<String, OssMaster> afterOssMap = getBasicOssInfoList(ossMaster);
									
			if ((afterOssMap == null || afterOssMap.isEmpty()) && beforeOssMap.size() > 1) {
				ossVersion_Flag = true;
			}
			
			return ossVersion_Flag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO;
		
		}else {
			return ossVersion_Flag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO;
		}
	}

	@Transactional
	@Override
	public Map<String, List<OssMaster>> updateOssNameVersionDiff(OssMaster ossMaster) {
		String beforeOssName = getOssInfo(ossMaster.getOssId(), false).getOssName();
		String afterOssName = ossMaster.getOssName();
		
		Map<String, List<OssMaster>> ossNameVersionDiffMergeObject = new HashMap<>();
		List<OssMaster> beforeOssNameVersionBeanList = new ArrayList<OssMaster>();
		List<OssMaster> afterOssNameVersionBeanList = new ArrayList<OssMaster>();
		List<String> beforeOssNameVersionOssIdList = new ArrayList<String>();
		OssMaster beforeOssNameVersionBean = null;
		OssMaster afterOssNameVersionBean = null;
		
		List<String> beforeOssNameList = new ArrayList<>();
		beforeOssNameList.add(beforeOssName);
		String[] beforeOssNames = new String[beforeOssNameList.size()];
		ossMaster.setOssNames(beforeOssNameList.toArray(beforeOssNames));
		
		Map<String, OssMaster> beforeOssMap = getBasicOssInfoList(ossMaster);
		
		History history;
		String comment = ossMaster.getComment();
		List<String> changeOssNameList = new ArrayList<>();
		
		for (OssMaster om : beforeOssMap.values()) {
			if (!ossMaster.getOssVersion().equals(om.getOssVersion())) {
				beforeOssNameVersionOssIdList.add(om.getOssId());
				beforeOssNameVersionBean = getOssInfo(om.getOssId(), true);
				beforeOssNameVersionBeanList.add(beforeOssNameVersionBean);
			}
			
			if (!beforeOssName.equals(afterOssName)) {
				om.setOssName(afterOssName);
				ossMapper.changeOssNameByDelete(om);
				
				history = work(om);
				history.sethAction(CoConstDef.ACTION_CODE_UPDATE);
				historyService.storeData(history);
				
				String before = beforeOssName + " (" + avoidNull(om.getOssVersion(), "N/A") + ")";
				String after = afterOssName + " (" + avoidNull(om.getOssVersion(), "N/A") + ")";
				changeOssNameList.add(before + "|" + after);
				String contents = CommonFunction.changeDataToTableFormat("oss", CommonFunction.getCustomMessage("msg.common.change.name", "OSS Name"), changeOssNameList);
				contents = comment + contents;
				
				CommentsHistory commentsHistory = new CommentsHistory();
				commentsHistory.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_OSS);
				commentsHistory.setReferenceId(om.getOssId());
				commentsHistory.setContents(contents);
				commentService.registComment(commentsHistory, false);
			}
			
			changeOssNameList.clear();
		}
		
		ossMaster.setOssName(beforeOssName);
		
		if (ossMaster.getOssNicknames() != null) {
			ossMapper.deleteOssNickname(ossMaster);
			
			ossMaster.setOssName(afterOssName);
			for (String nickName : ossMaster.getOssNicknames()){
				if (!isEmpty(nickName)) {
					ossMaster.setOssNickname(nickName.trim());
					ossMapper.insertOssNickname(ossMaster);
				}
			}
		} else {
			ossMapper.deleteOssNickname(ossMaster);
		}
		
		CoCodeManager.getInstance().refreshOssInfo();
		
		for (String ossId : beforeOssNameVersionOssIdList) {
			afterOssNameVersionBean = getOssInfo(ossId, true);
			afterOssNameVersionBeanList.add(afterOssNameVersionBean);
		}
		
		if (beforeOssNameVersionBeanList != null && !beforeOssNameVersionBeanList.isEmpty()) {
			beforeOssNameVersionBeanList = beforeOssNameVersionBeanList.stream()
										.sorted(Comparator.comparing(OssMaster::getOssVersion, Comparator.nullsLast(Comparator.naturalOrder())))
										.collect(Collectors.toList());
			afterOssNameVersionBeanList = afterOssNameVersionBeanList.stream()
										.sorted(Comparator.comparing(OssMaster::getOssVersion, Comparator.nullsLast(Comparator.naturalOrder())))
										.collect(Collectors.toList());
		}
		
		ossNameVersionDiffMergeObject.put("before", beforeOssNameVersionBeanList);
		ossNameVersionDiffMergeObject.put("after", afterOssNameVersionBeanList);
		
		if (!beforeOssName.equals(afterOssName)) {
			OssMaster param = new OssMaster();
			param.setOssName(beforeOssName);
			param.setOssVersion(null);
			
			// 3rdParty == 'CONF'
			List<PartnerMaster> confirmPartnerList = ossMapper.getOssNameMergePartnerList(param);

			if (confirmPartnerList.size() > 0) {
				param.setMergeOssName(afterOssName);
				param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
				
				for (PartnerMaster pm : confirmPartnerList) {
					param.setPrjId(pm.getPartnerId());
					
					List<OssComponents> confirmOssComponentsList = ossMapper.getConfirmOssComponentsList(param);
					
					for (OssComponents oc : confirmOssComponentsList) {
						param.setOssVersion(oc.getOssVersion());
						try {
							ossMapper.updateOssComponents(param);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						try {
							List<String> changeOssInfoList = new ArrayList<>();
							String before = beforeOssName + " (" + avoidNull(oc.getOssVersion(), "N/A") + ")";
							String after = afterOssName + " (" + avoidNull(oc.getOssVersion(), "N/A") + ")";
							changeOssInfoList.add(before + "|" + after);
							String contents = CommonFunction.changeDataToTableFormat("oss", CommonFunction.getCustomMessage("msg.common.change.name", "OSS Name"), changeOssInfoList);
							
							// partner Comment Regist
							CommentsHistory historyBean = new CommentsHistory();
							historyBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_HIS);
							historyBean.setReferenceId(pm.getPartnerId());
							historyBean.setStatus("OSS Name Changed");
							historyBean.setContents(contents);

							commentService.registComment(historyBean);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
			
			param.setOssVersion(null);
			// Identification == 'CONF', verification
			List<Project> confirmProjectList = ossMapper.getOssNameMergeProjectList(param);

			if (confirmProjectList.size() > 0) {
				param.setMergeOssName(afterOssName);
				param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_SRC);
				
				for (Project prj : confirmProjectList) {
					param.setPrjId(prj.getPrjId());
					
					List<OssComponents> confirmOssComponentsList = ossMapper.getConfirmOssComponentsList(param);
					
					for (OssComponents oc : confirmOssComponentsList) {
						param.setOssVersion(oc.getOssVersion());
						try {
							ossMapper.updateOssComponents(param);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						try {
							List<String> changeOssInfoList = new ArrayList<>();
							String before = beforeOssName + " (" + avoidNull(oc.getOssVersion(), "N/A") + ")";
							String after = afterOssName + " (" + avoidNull(oc.getOssVersion(), "N/A") + ")";
							changeOssInfoList.add(before + "|" + after);
							String contents = CommonFunction.changeDataToTableFormat("oss", CommonFunction.getCustomMessage("msg.common.change.name", "OSS Name"), changeOssInfoList);
							
							// Project > Identification comment regist
							CommentsHistory historyBean = new CommentsHistory();
							historyBean.setReferenceId(prj.getPrjId());
							historyBean.setStatus("OSS Name Changed");
							historyBean.setContents(contents);
							historyBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commentService.registComment(historyBean);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
		}
		
		return ossNameVersionDiffMergeObject;
	}

	@Override
	public OssMaster getSaveSesstionOssInfoByName(OssMaster ossMaster) {
		return ossMapper.getSaveSesstionOssInfoByName(ossMaster);
	}

	@Override
	public List<Vulnerability> getOssVulnerabilityList2(OssMaster ossMaster) {
		if (avoidNull(ossMaster.getCveId()).isEmpty() && avoidNull(ossMaster.getCvssScore()).isEmpty()) {
			return null;
		}
		
		List<Vulnerability> list = null;
		List<Vulnerability> convertList = new ArrayList<>();
		boolean inCpeMatchFlag = CoConstDef.FLAG_YES.equals(avoidNull(ossMaster.getInCpeMatchFlag())) ? true : false;
		
		String[] nicknameList = null;
		List<String> dashOssNameList = new ArrayList<>();
		List<String> convertNameList = null;
		boolean convertFlag = false;
		
		try {
			OssMaster param = new OssMaster();
			param.setOssName(ossMaster.getOssName());
			param.setOssVersion(ossMaster.getOssVersion());
			param.setOssNicknames(ossMaster.getOssNicknames());
			
			boolean isNoVersion = isEmpty(param.getOssVersion()) || param.getOssVersion().equals("-")? true : false;
			List<String> includeCpeEnvironmentList = new ArrayList<>();
			List<String> excludeCpeEnvironmentList = new ArrayList<>();
			
			List<String> includeCpeList = null;
			if (ossMaster.getIncludeCpes() != null) {
				includeCpeList = new ArrayList<>(Arrays.asList(ossMaster.getIncludeCpes()));
			}
			List<String> excludeCpeList = null;
			if (ossMaster.getExcludeCpes() != null) {
				excludeCpeList = new ArrayList<>(Arrays.asList(ossMaster.getExcludeCpes()));
			}
			List<String> ossVersionAliasWithColon = new ArrayList<>();
			List<String> ossVersionAliasWithoutColon = new ArrayList<>();
			
			if (ossMaster.getOssVersionAliases() != null) {
				for (String alias : ossMaster.getOssVersionAliases()) {
					if (alias.contains(":")) {
						ossVersionAliasWithColon.add(alias);
					} else {
						ossVersionAliasWithoutColon.add(alias);
					}
				}
				param.setOssVersionAliasWithColon(ossVersionAliasWithColon.toArray(new String[ossVersionAliasWithColon.size()]));
			}
			ossVersionAliasWithoutColon.add(isEmpty(ossMaster.getOssVersion()) ? "-" : ossMaster.getOssVersion());
			param.setOssVersionAliases(ossVersionAliasWithoutColon.toArray(new String[ossVersionAliasWithoutColon.size()]));
			
			if (inCpeMatchFlag) {
				List<Map<String, Object>> includeVendorProductInfoList = null;
				List<Map<String, Object>> excludeVendorProductInfoList = null;
				
				if (includeCpeList != null && !includeCpeList.isEmpty()) {
					generateIncludeCpeParam(param, includeCpeList, includeCpeEnvironmentList);
					includeVendorProductInfoList = vulnerabilityMapper.selectVendorProductByIncludeCpeInfo(param);
				}
				
				if (excludeCpeList != null && !excludeCpeList.isEmpty()) {
					generateExcludeCpeParam(param, excludeCpeList, excludeCpeEnvironmentList);
					excludeVendorProductInfoList = vulnerabilityMapper.selectVendorProductByExcludeCpeInfo(param);
				}
				
				List<Map<String, Object>> filteredVendorProductInfoList = new ArrayList<>();
				
				if (includeVendorProductInfoList != null && !includeVendorProductInfoList.isEmpty()) {
					if (excludeVendorProductInfoList != null && !excludeVendorProductInfoList.isEmpty()) {
						generateIncludeCpeMatchList(includeVendorProductInfoList, excludeVendorProductInfoList, includeCpeEnvironmentList, filteredVendorProductInfoList, isNoVersion);
					} else {
						generateIncludeCpeMatchList(includeVendorProductInfoList, null, includeCpeEnvironmentList, filteredVendorProductInfoList, isNoVersion);
					}
				}
				
				if (filteredVendorProductInfoList != null && !filteredVendorProductInfoList.isEmpty()) {
					Collections.sort(filteredVendorProductInfoList, new Comparator<Map<String, Object>>() {
						@Override
						public int compare(Map<String, Object> o1, Map<String, Object> o2) {
							if (new BigDecimal(o1.get("CVSS_SCORE").toString()).compareTo(new BigDecimal(o2.get("CVSS_SCORE").toString())) > 0) {
								return -1;
							}else {
								return 1;
							}
						}
					});
					
					list = new ArrayList<>();
					
					for (Map<String, Object> maxScoreVulnMap : filteredVendorProductInfoList) {
						Vulnerability vuln = new Vulnerability();
						vuln.setProduct((String) maxScoreVulnMap.get("PRODUCT"));
						vuln.setCveId((String) maxScoreVulnMap.get("CVE_ID"));
						vuln.setCvssScore(String.valueOf((Float) maxScoreVulnMap.get("CVSS_SCORE")));
						vuln.setVulnSummary((String) maxScoreVulnMap.get("VULN_SUMMARY"));
						vuln.setModiDate(String.valueOf((Timestamp) maxScoreVulnMap.get("MODI_DATE")));
						list.add(vuln);
					}
				}
				
				List<Vulnerability> list2 = vulnDataForNotIncludeCpeMatch(convertFlag, ossMaster, nicknameList, convertNameList, dashOssNameList, param);
				if (list2 != null && !list2.isEmpty()) {
					if (list == null) {
						list = new ArrayList<>();
					}
					list.addAll(list2);
				}
			} else {
				list = vulnDataForNotIncludeCpeMatch(convertFlag, ossMaster, nicknameList, convertNameList, dashOssNameList, param);
			}
			
			if (list != null) {
				if (!CoConstDef.FLAG_YES.equals(avoidNull(param.getVulnerabilityCheckFlag()))) {
					list = checkVulnData(list, ossMaster.getOssNicknames());
				}
				list = list.stream().filter(CommonFunction.distinctByKey(e -> e.getCveId())).collect(Collectors.toList());
				int idx = 1;
				for (Vulnerability vuln : list) {
					if (idx > 5) {
						break;
					}
					convertList.add(vuln);
					idx++;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		ossMaster.setOssNameTemp(null);
		if (convertFlag) {
			ossMaster.setOssNicknames(nicknameList);
		}
		
		if (ossMaster.getOssVersion().equals("-")) {
			ossMaster.setOssVersion("");
		}
		
		return convertList;
	}

	private List<Vulnerability> vulnDataForNotIncludeCpeMatch(Boolean convertFlag, OssMaster ossMaster, String[] nicknameList, List<String> convertNameList, List<String> dashOssNameList, OssMaster param) {
		List<Vulnerability> list = new ArrayList<>();
		
		if ("N/A".equals(ossMaster.getOssVersion()) || isEmpty(ossMaster.getOssVersion())) {
			param.setOssVersion("-");
		}
		
		if (ossMaster.getOssName().contains(" ")) {
			param.setOssNameTemp(ossMaster.getOssName().replaceAll(" ", "_"));
		}
		
		if (ossMaster.getOssName().contains("-")) {
			dashOssNameList.add(ossMaster.getOssName());
		}
		
		nicknameList = getOssNickNameListByOssName(ossMaster.getOssName());
		
		for (String nick : nicknameList) {
			if (nick.contains("-")) {
				dashOssNameList.add(nick);
			}
			if (nick.contains(" ")) {
				if (!convertFlag) {
					convertNameList = new ArrayList<>();
					convertFlag = true;
				}
				convertNameList.add(nick.replaceAll(" ", "_"));
			}
		}
		
		if (convertNameList != null) {
			convertNameList.addAll(Arrays.asList(nicknameList));
			param.setOssNicknames(convertNameList.toArray(new String[convertNameList.size()]));
		} else {
			param.setOssNicknames(nicknameList);
		}
		
		if (dashOssNameList.size() > 0) {
			param.setDashOssNameList(dashOssNameList.toArray(new String[dashOssNameList.size()]));
		}
		
		list = ossMapper.getOssVulnerabilityList2(param);
		
		if (ossMaster.getOssVersionAliases() != null) {
			for (String ossVersionAlias : ossMaster.getOssVersionAliases()) {
				param.setOssVersion(ossVersionAlias);
				List<Vulnerability> list2 = ossMapper.getOssVulnerabilityList2(param);
				if (list2 != null && !list2.isEmpty()) {
					list.addAll(list2);
				}
			}
		}
		
		if (list != null && !list.isEmpty()) {
			list = list.stream().filter(CommonFunction.distinctByKey(e -> e.getCveId())).collect(Collectors.toList());
			
			List<String> includeCpeList = ossMapper.notExistsOssIncludeCpeListByOssCommonId(ossMaster);
			List<Vulnerability> customList = new ArrayList<>();
			
			for (Vulnerability vuln : list) {
				if (!includeCpeList.contains(vuln.getCriteria())) {
					customList.add(vuln);
				}
			}
			
			list = customList;
		}
		
		if (ossMaster.getExcludeCpes() != null) {
			List<String> excludeCpeList = Arrays.asList(ossMaster.getExcludeCpes());
			List<Vulnerability> customList = new ArrayList<>();
			
			for (Vulnerability vuln : list) {
				if (!excludeCpeList.contains(vuln.getCriteria())) {
					customList.add(vuln);
				}
			}
			
			list = customList;
		}
		
		return list;
	}

	private List<Vulnerability> checkVulnData(List<Vulnerability> list, String[] nicknameList) {
		List<Vulnerability> result = new ArrayList<Vulnerability>();
		
		for (Vulnerability bean : list) {
			bean.setOssNameAllSearchFlag(CoConstDef.FLAG_YES);
			if (nicknameList != null) {
				bean.setOssNicknames(nicknameList);
			}
			int vulnCnt = vulnerabilityMapper.checkVulnDataCnt(bean);
			if (vulnCnt > 0) {
				result.add(bean);
			}
		}
		
		return result;
	}

	@Override
	public List<String> getOssNicknameListWithoutOwn(OssMaster ossMaster, List<String> checkList, List<String> duplicatedList) {
		if (checkList != null && checkList.size() > 0) {
			List<OssMaster> ossNameCheckList = ossMapper.selectOssNameList();
			ossNameCheckList = ossNameCheckList.stream()
							.filter(e -> checkList.stream().anyMatch(Predicate.isEqual(e.getOssName())))
							.collect(Collectors.toList());
						
			List<OssMaster> ossNicknameCheckList = ossMapper.selectOssNicknameListWithoutOwn(ossMaster);
			ossNicknameCheckList = ossNicknameCheckList.stream()
								.filter(e -> checkList.stream().anyMatch(Predicate.isEqual(e.getOssNickname())))
								.collect(Collectors.toList());
			
			if (ossNameCheckList != null && ossNameCheckList.size() > 0) {
				for (OssMaster om : ossNameCheckList) {
					if (duplicatedList.isEmpty() || !duplicatedList.contains(om.getOssName())) {
						duplicatedList.add(om.getOssName());
					}
				}
			}
			
			if (ossNicknameCheckList != null && ossNicknameCheckList.size() > 0) {
				for (OssMaster om : ossNicknameCheckList) {
					if (duplicatedList.isEmpty() || !duplicatedList.contains(om.getOssNickname())) {
						duplicatedList.add(om.getOssNickname());
					}
				}
			}
		}
		
		return duplicatedList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> sendMailForSaveOss(Map<String, Object> resMap) {
		OssMaster ossMaster = (OssMaster) resMap.get("ossMaster");
		String ossId = (String) resMap.get("ossId");
		boolean isNew = (boolean) resMap.get("isNew");
		boolean isNewVersion = (boolean) resMap.get("isNewVersion");
		boolean isChangedName = (boolean) resMap.get("isChangedName");
		boolean isDeactivateFlag = (boolean) resMap.get("isDeactivateFlag");
		boolean isActivateFlag = (boolean) resMap.get("isActivateFlag");
		
		Map<String, List<OssMaster>> updateOssNameVersionDiffMergeObject = null;
		if (resMap.containsKey("updateOssNameVersionDiffMergeObject")) {
			updateOssNameVersionDiffMergeObject = (Map<String, List<OssMaster>>) resMap.get("updateOssNameVersionDiffMergeObject");
		}
		
		String mailType = "";
		
		if (CoConstDef.FLAG_YES.equals(ossMaster.getRenameFlag())) {
			mailType = CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME;
		} else if (isNew) {
			mailType = isNewVersion
					? CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION
					: CoConstDef.CD_MAIL_TYPE_OSS_REGIST;
		} else {
			mailType = isChangedName
					? CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME
					: CoConstDef.CD_MAIL_TYPE_OSS_UPDATE;

			if (isDeactivateFlag) {
				mailType = CoConstDef.CD_MAIL_TYPE_OSS_DEACTIVATED;
			}

			if (isActivateFlag) {
				mailType = CoConstDef.CD_MAIL_TYPE_OSS_ACTIVATED;
			}
		}
		try {
			CoMail mailBean = new CoMail(mailType);
			mailBean.setParamOssId(ossId);
			mailBean.setComment(ossMaster.getComment());
			mailBean.setParamReferenceDiv(ossMaster.getReferenceDiv());

			if (!isNew && !isDeactivateFlag) {
				mailBean.setCompareDataBefore((OssMaster) resMap.get("beforeBean"));
				mailBean.setCompareDataAfter((OssMaster) resMap.get("afterBean"));
			} else if (isNewVersion) {
				mailBean.setParamOssInfo(ossMaster);
			}
			
			if (CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION.equals(mailType)
					|| CoConstDef.CD_MAIL_TYPE_OSS_UPDATE.equals(mailType)
					|| CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME.equals(mailType)) {
				setVdiffInfoForSentMail(ossMaster.getOssName(), mailBean);
			}
			
			CoMailManager.getInstance().sendMail(mailBean);

		} catch (Exception e) {
			log.error("Failed to send mail:" + e.getMessage());
		}

		if (!isNew && CoConstDef.FLAG_YES.equals(ossMaster.getRenameFlag())) {
			if (updateOssNameVersionDiffMergeObject != null) {
				List<OssMaster> beforeOssNameVersionMergeList = updateOssNameVersionDiffMergeObject.get("before");
				List<OssMaster> afterOssNameVersionMergeList = updateOssNameVersionDiffMergeObject.get("after");

				if (afterOssNameVersionMergeList != null) {
					for (int i = 0; i < afterOssNameVersionMergeList.size(); i++) {
						try {
							mailType = CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME;
							CoMail mailBean = new CoMail(mailType);
							mailBean.setComment(ossMaster.getComment());
							mailBean.setParamOssId(afterOssNameVersionMergeList.get(i).getOssId());
							mailBean.setCompareDataBefore(beforeOssNameVersionMergeList.get(i));
							mailBean.setCompareDataAfter(afterOssNameVersionMergeList.get(i));
							CoMailManager.getInstance().sendMail(mailBean);
						} catch (Exception e) {
							log.error("Failed to send mail:" + e.getMessage());
						}
					}
				}
			}
		}
		
		putSessionObject("defaultLoadYn", true); // 화면 로드 시 default로 리스트 조회 여부 flag
		resMap.put("ossId", ossId);
		return resMap;
	}

	public void setVdiffInfoForSentMail(String ossName, CoMail mailBean) {
		List<Map<String, Object>> resultData  = new ArrayList<>();
		boolean vDiffFlag = ossMapper.checkOssVersionDiff(ossName) > 0 ? true : false;

		if(vDiffFlag) {
			OssMaster param = new OssMaster();
			param.setOssNames(new String[] {ossName});
			Map<String, OssMaster> ossMap = getBasicOssInfoList(param);

			for (String key : ossMap.keySet()) {
				OssMaster om = ossMap.get(key);
				Map<String, Object> contentMap = new HashMap<>();
				contentMap.put("ossNameInfo", om.getOssName() + " (" + om.getOssVersion() + ")");
				contentMap.put("licenseInfo", CommonFunction.makeLicenseExpression(om.getOssLicenses()));
				resultData.add(contentMap);
			}
			
			if (resultData != null && !resultData.isEmpty()) {
				mailBean.setParamList(resultData);
			} else {
				mailBean.setParamList(new ArrayList<>());
			}
		}
	}
	
	@Override
	public List<String> getDeactivateOssList() {
		return ossMapper.getDeactivateOssList();
	}

	@Override
	public Map<String, Object> getOssDataMap(String gridId, boolean status, String msg) {
		Map<String, Object> ossDataMap = new HashMap<>();
		ossDataMap.put("gridId", gridId);
		ossDataMap.put("status", status);
		ossDataMap.put("msg", msg);

		return ossDataMap;
	}
	
		@Transactional
	@Override
	public Map<String, Object> saveOssURLNickname(ProjectIdentification paramBean) {
		Map<String, Object> map = new HashMap<String, Object>();
		OssMaster ossMaster = getOssInfo(null, paramBean.getCheckName(), true);
		if (ossMaster != null) {
			List<String> checkOssNameUrl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_NAME_URL);
			int urlSearchSeq = -1;
			int seq = 0;
			for (String url : checkOssNameUrl) {
				if (urlSearchSeq == -1 && paramBean.getDownloadLocation().contains(url)) {
					urlSearchSeq = seq;
					break;
				}
				seq++;
			}
			ProjectIdentification bean;

			try {
				for (String recommendedNickname : paramBean.getRecommendedNickname().split("\\|")) {
					if (!isEmpty(recommendedNickname)) {
						ossMaster.setOssNickname(recommendedNickname);
						ossMapper.mergeOssNickname2(ossMaster);
					}
				}

				if (urlSearchSeq > -1) {
					bean = downloadlocationFormatter(paramBean, urlSearchSeq);
					String downloadLocation = bean.getDownloadLocation();
					String redirectLocation = bean.getRedirectLocation();
					bean.setOssName(paramBean.getCheckName());

					for (int i = 0; i < 2; i++) {
						if (i == 0) {
							bean.setDownloadLocation(downloadLocation);
						} else {
							bean.setDownloadLocation(redirectLocation);
						}

						if (ossMapper.checkOssNameUrl2Cnt(bean) == 0) {
							Map<String, Object> data = ossMapper.getRecentlyModifiedOss(ossMaster);
							ossMaster.setOssCommonId(String.valueOf(data.get("OSS_COMMON_ID")));
							int cnt = Integer.parseInt(String.valueOf(data.get("CNT"))) + 1;
							ossMaster.setOssDlIdx(cnt);
							ossMaster.setDownloadLocation("https://" + bean.getDownloadLocation());
							ossMapper.insertOssDownloadLocation(ossMaster);
						}
					}
				}
				map.put("isValid", true);
				map.put("returnType", "Success");
			} catch (Exception e) {
				log.error(e.getMessage());
				map.put("isValid", false);
				map.put("returnType", e.getMessage());
			}
		} else {
			map.put("isValid", false);
		}
		return map;
	}

	@Override
	public List<String> selectVulnInfoForOss(OssMaster ossMaster) {
		return ossMapper.selectVulnInfoForOss(ossMaster);
	}

	@Override
	public List<String> checkExistsVendorProductMatchOss(OssMaster ossMaster) {
		return ossMapper.checkExistsVendorProductMatchOss(ossMaster);
	}

	@Override
	public int checkOssVersionDiff(String ossName) {
		return ossMapper.checkOssVersionDiff(ossName);
	}

	@Override
	public boolean checkOssTypeForAnalysisResult(OssAnalysis ossAnalysis) {
		boolean vDiffFlag = false;
		OssMaster param = new OssMaster();
		List<String> ossNameList = new ArrayList<>();
		String ossName = ossAnalysis.getOssName();
		String ossNameTemp = "";
		boolean ossNameFlag = false;
		
		if (CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossName.toUpperCase())) {
			ossNameTemp = CoCodeManager.OSS_INFO_UPPER_NAMES.get(ossName.toUpperCase());
			if (!isEmpty(ossNameTemp) && ossName.equalsIgnoreCase(ossNameTemp)) {
				ossNameFlag = true;
			}
		}
		
		if (ossNameFlag) {
			ossNameList.add(ossName);
		} else {
			ossNameList.add(ossNameTemp);
		}
		
		String[] ossNames = new String[ossNameList.size()];
		param.setOssNames(ossNameList.toArray(ossNames));
		Map<String, OssMaster> ossMap = getBasicOssInfoList(param);
		
		if (ossMap != null && ossMap.size() > 1) {
			List<List<OssLicense>> andCombLicenseListStandard = new ArrayList<>();
			List<List<OssLicense>> andCombLicenseListCompare = new ArrayList<>();
			
			int idx = 0;
			
			for (OssMaster _bean : ossMap.values()) {
				if (!isEmpty(_bean.getOssId())) {
					if (idx == 0) {
						andCombLicenseListStandard = makeLicenseKeyList(_bean.getOssLicenses());
					}else {
						if (!vDiffFlag && _bean.getOssLicenses() != null) {
							andCombLicenseListCompare = makeLicenseKeyList(_bean.getOssLicenses());
							
							if (andCombLicenseListStandard.size() != andCombLicenseListCompare.size()) {
								vDiffFlag = true;
								break;
							}else {
								if (!checkLicenseListVersionDiff(andCombLicenseListStandard, andCombLicenseListCompare)) {
									vDiffFlag = true;
									break;
								}
							}
							
							andCombLicenseListCompare = new ArrayList<>();
						}
					}
					
					idx++;
				}
			}
		}
		
		return vDiffFlag;
	}
	
	@Override
	public String getOssAnalysisStatus(String prjId) {
		String status = ossMapper.getOssAnalysisStatus(prjId);
		return status;
	}

	@Override
	public void deleteOssAnalysis(String prjId) {
		ossMapper.deleteOssAnalysis(prjId);
	}

	@Override
	public void registCpeInfo(OssMaster ossMaster) {
		if (ossMapper.existsOssIncludeCpe(ossMaster) > 0){
			ossMapper.deleteOssIncludeCpe(ossMaster);
		}
		if (ossMapper.existsOssExcludeCpe(ossMaster) > 0){
			ossMapper.deleteOssExcludeCpe(ossMaster);
		}
		
		String[] includeCpes = ossMaster.getIncludeCpes();
		
		OssMaster master = new OssMaster();
		if (includeCpes != null){
			for (String includeCpe : includeCpes){
				if (!isEmpty(includeCpe)){
					master.setOssCommonId(ossMaster.getOssCommonId());
					master.setIncludeCpe(includeCpe);;
					
					ossMapper.insertOssIncludeCpe(master);
				}
			}
		}
		
		String[] excludeCpes = ossMaster.getExcludeCpes();
		
		if (excludeCpes != null){
			for (String excludeCpe : excludeCpes){
				if (!isEmpty(excludeCpe)){
					master.setOssCommonId(ossMaster.getOssCommonId());
					master.setExcludeCpe(excludeCpe);;
					
					ossMapper.insertOssExcludeCpe(master);
				}
			}
		}
	}

	@Override
	public void registOssVersionAlias(OssMaster ossMaster) {
		if (ossMapper.existsOssVersionAlias(ossMaster) > 0){
			ossMapper.deleteOssVersionAlias(ossMaster);
		}
		
		String[] ossVersionAliases = ossMaster.getOssVersionAliases();
		if (ossVersionAliases != null) {
			for (String ossVersionAlias : ossVersionAliases) {
				if (!isEmpty(ossVersionAlias)) {
					ossMaster.setOssVersionAlias(ossVersionAlias);
					ossMapper.insertOssVersionAlias(ossMaster);
				}
			}
		}
	}

	@Override
	public String getPurlByDownloadLocation(OssMaster ossMaster) {
		return generatePurlByDownloadLocation(ossMaster);
	}

	private String generatePurlByDownloadLocation(OssMaster ossMaster) {
		List<String> checkPurl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_DOWNLOADLOCAION_PURL);
		String purlString = "";
		int urlSearchSeq = -1;
		int seq = 0;
		
		String downloadLocation = ossMaster.getDownloadLocation();
		if (!isEmpty(downloadLocation)) {
			String subPath = "";
			
			for (String url : checkPurl) {
				if (urlSearchSeq == -1 && downloadLocation.contains(url)) {
					urlSearchSeq = seq;
					break;
				}
				seq++;
			}
			
			if (downloadLocation.contains("://")) {
				downloadLocation = downloadLocation.split("//")[1];
			}
			if (downloadLocation.startsWith("www.")) {
				downloadLocation = downloadLocation.substring(4, downloadLocation.length());
			}
			if (downloadLocation.contains(";")) {
				downloadLocation = downloadLocation.split("[;]")[0];
			}
			// delete port number
			if (downloadLocation.contains(":")) {
				int colonIdx = downloadLocation.indexOf(":");
				int slashIdx = downloadLocation.indexOf("/", colonIdx);
				if (slashIdx > -1 && slashIdx > colonIdx && downloadLocation.substring(colonIdx+1, slashIdx).chars().allMatch(Character::isDigit)) {
					downloadLocation = downloadLocation.substring(0, colonIdx) + downloadLocation.substring(slashIdx, downloadLocation.length());
				}
			}
			
			if (downloadLocation.contains(".git") && downloadLocation.endsWith(".git")) {
				downloadLocation = downloadLocation.substring(0, downloadLocation.length()-4);
			}
			
			if (urlSearchSeq == 16) {
				if (downloadLocation.contains("#")) {
					String[] splitDownloadLocation = downloadLocation.split("[#]");
					subPath = splitDownloadLocation[1];
					downloadLocation = splitDownloadLocation[0];
				}
				if (downloadLocation.contains("@")) {
					downloadLocation = downloadLocation.substring(0, downloadLocation.indexOf("@"));
				}
			}
			
			if (downloadLocation.endsWith("/")) {
				downloadLocation = downloadLocation.substring(0, downloadLocation.length()-1);
			}
			
			if (urlSearchSeq > -1) {
				Pattern p = generatePatternPurl(urlSearchSeq, downloadLocation);
				Matcher m = p.matcher(downloadLocation);
				
				while (m.find()) {
					downloadLocation = m.group(0);
				}
			}
			
			PackageURL purl = null;
			if (urlSearchSeq == -1 || urlSearchSeq == 16) {
				purlString = "link:" + downloadLocation;
			} else {
				String[] splitDownloadLocation = downloadLocation.split("/");
				boolean addFlag = false;
				String namespace = "/";
				
				boolean errorFlag = false;
				try {
					switch(urlSearchSeq) {
						case 0: // github
							purl = new PackageURL(StandardTypes.GITHUB, splitDownloadLocation[1], splitDownloadLocation[2], null, null, null);
							break;
						case 1: // npm
						case 2: // npm
						case 3: // npm
							if (downloadLocation.contains("/package/@")) {
								addFlag = true;
							}
							purl = new PackageURL(StandardTypes.NPM, null, splitDownloadLocation[2], null, null, null);
							break;
						case 4: // npm
						case 5: // npm
						case 6: // npm
						case 7: // npm
						case 8: // npm
							if (downloadLocation.contains("/@")) {
								addFlag = true;
							}
							purl = new PackageURL(StandardTypes.NPM, null, splitDownloadLocation[1], null, null, null);
							break;
						case 9: // pypi
						case 10: // pypi
							purl = new PackageURL(StandardTypes.PYPI, null, splitDownloadLocation[2].replaceAll("_", "-"), null, null, null);
							break;
						case 11: // maven
						case 12: // maven
							purl = new PackageURL(StandardTypes.MAVEN, splitDownloadLocation[2], splitDownloadLocation[3], null, null, null);
							break;
						case 13: // cocoapod
							purl = new PackageURL("cocoapods", null, splitDownloadLocation[2], null, null, null);
							break;
						case 14: // gem
							purl = new PackageURL(StandardTypes.GEM, null, splitDownloadLocation[2], null, null, null);
							break;
						case 15: // go
							int idx = 0;
							for (String data : splitDownloadLocation) {
								if (idx > 1) {
									namespace += data + "/";
								}
								idx++;
							}
							namespace = namespace.substring(0, namespace.length()-1);
							purl = new PackageURL(StandardTypes.GOLANG, splitDownloadLocation[1]);
							
							break;
						case 17:
							purl = new PackageURL("pub", null, splitDownloadLocation[2], null, null, null);
							break;
						default:
							break;
					}
				} catch (Exception e) {
					errorFlag = true;
					log.error("failed to generate purl download location : {}, link generate purl {}", downloadLocation, "link:" + downloadLocation);
				}
				
				if (errorFlag) {
					purlString = "link:" + downloadLocation;
				} else {
					if (purl != null) {
						purlString = purl.toString();
						if (urlSearchSeq == 15) {
							purlString += namespace + subPath;
						} else {
							if (addFlag) {
								if (urlSearchSeq == 1) {
									if (splitDownloadLocation.length > 3) {
										purlString += "/" + splitDownloadLocation[3];
									}
								} else {
									if (splitDownloadLocation.length > 2) {
										purlString += "/" + splitDownloadLocation[2];
									}
								}
							}
						}
					}
				}
			}
		}
		
		return purlString;
	}
	
	private Pattern generatePatternPurl(int urlSearchSeq, String downloadlocationUrl) {
		Pattern p = null;
		switch(urlSearchSeq) {
			case 0: // github
				p = Pattern.compile("((http|https)://github.com/([^/]+)/([^/]+))");

				break;
			case 1: // npm
			case 2: // npm
			case 3: // npm
				if (downloadlocationUrl.contains("/package/@")) {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+)/([^/]+))");
				} else {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+))");
				}
			case 4: // npm
				if (downloadlocationUrl.contains("/@")) {
					p = Pattern.compile("((http|https)://registry.npmjs.(org|com)/([^/]+)/([^/]+))");
				} else {
					p = Pattern.compile("((http|https)://registry.npmjs.(org|com)/([^/]+))");
				}
				break;
			case 5: // npm
			case 6: // npm
			case 7: // npm
			case 8: // npm
				p = Pattern.compile("((http|https)://npmjs.(org|com)/([^/]+))");
				break;
			case 9: // pypi
				p = Pattern.compile("((http|https)://pypi.python.org/project/([^/]+))");
				break;
			case 10: // pypi
				p = Pattern.compile("((http|https)://pypi.org/project/([^/]+))");
				break;
			case 11: // maven
				p = Pattern.compile("((http|https)://mvnrepository.com/artifact/([^/]+)/([^/]+))");
				break;
			case 12: // maven
				p = Pattern.compile("((http|https)://repo.maven.apache.org/maven2/([^/]+)/([^/]+))");
				break;
			case 13: // cocoapod
				p = Pattern.compile("((http|https)://cocoapods.org/pods/([^/]+))");
				break;
			case 14: // gem
				p = Pattern.compile("((http|https)://rubygems.org/gems/([^/]+))");
				break;
			case 15: // go
				p = Pattern.compile("((http|https)://pkg.go.dev/([^@]+)@?v?([^/]+))");
				break;
			case 16:
				p = Pattern.compile("((http|https)://android.googlesource.com/(.*))");
				break;
			case 17:
				p = Pattern.compile("((http|https)://pub.dev/packages/([^/]+))");
				break;
			default:
				p = Pattern.compile("(.*)");
				break;
		}
		return p;
	}

	@Override
	public void setOssAnalysisStatus(String prjId) {
		OssMaster ossBean = new OssMaster();
		ossBean.setPrjId(prjId);
		ossBean.setCreator(loginUserName());
		ossMapper.setOssAnalysisStatus(ossBean);
	}

	@Override
	public void setExistedOssInfo(OssMaster ossMaster) {
		ossMaster.setExistOssNickNames(getOssNickNameListByOssName(ossMaster.getOssName()));
		OssMaster ossBean = getOssInfo(null, ossMaster.getOssName(), true);
		if (ossBean != null) {
			ossMaster.setOssCommonId(ossBean.getOssCommonId());
			ossMaster.setExistIncludeCpes(ossBean.getIncludeCpe() != null ? ossBean.getIncludeCpe().split(",") : null);
			ossMaster.setExistExcludeCpes(ossBean.getExcludeCpe() != null ? ossBean.getExcludeCpe().split(",") : null);
			ossMaster.setExistArrRestriction(ossBean.getRestriction() != null ? ossBean.getRestriction().split(",") : null);
			ossMaster.setExistDownloadLocations(ossBean.getDownloadLocations());
			ossMaster.setExistPurls(!isEmpty(ossBean.getPurl()) ? ossBean.getPurl().split(",") : null);
			ossMaster.setExistHomepage(ossBean.getHomepage());
			ossMaster.setExistSummaryDescription(ossBean.getSummaryDescription());
			ossMaster.setExistImportantNotes(ossBean.getImportantNotes());
		}
	}

	@Override
	public OssMaster getOssVulnerabilityInfo(OssMaster ossMaster) {
		return ossMapper.getOssVulnerabilityInfo(ossMaster);
	}
}
