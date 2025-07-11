/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

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
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.ProjectIdentificationTree;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.CacheService;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.VerificationService;
import oss.fosslight.service.VulnerabilityService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationConfig;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Service
@Slf4j
public class ProjectServiceImpl extends CoTopComponent implements ProjectService{
	// Service
	@Autowired private OssService ossService;
	@Autowired private VerificationService verificationService;
	@Autowired private FileService fileService;
	@Autowired private VulnerabilityService vulnerabilityService;
	@Autowired private CommentService commentService;
	
	// Mapper
	@Autowired private ProjectMapper projectMapper;
	@Autowired private T2UserMapper userMapper;
	@Autowired private PartnerMapper partnerMapper;
	@Autowired private CodeMapper codeMapper;
	@Autowired private CacheService cacheService;

	@Autowired Environment env;
	
	private String JDBC_DRIVER;
	private String DB_URL;
	private String USERNAME;
	private String PASSWORD;
	
	@Autowired private SqlSessionFactory sqlSessionFactory;
	
	@PostConstruct
	public void setResourcePathPrefix(){
		JDBC_DRIVER = env.getRequiredProperty("spring.datasource.driver-class-name");
		DB_URL = env.getRequiredProperty("spring.datasource.url");
		if (!DB_URL.startsWith("jdbc:mariadb")) DB_URL = "jdbc:mariadb://" + DB_URL;
		USERNAME = env.getRequiredProperty("spring.datasource.username");
		PASSWORD = env.getRequiredProperty("spring.datasource.password");
	}
	
	@Override
	@Cacheable(value="autocompleteProjectCache", key="{#root.methodName}")
	public List<Map<String, String>> getProjectNameList(Project project) {
		List<Project> prjNameList = projectMapper.getProjectNameList(project);
		List<Map<String, String>> prjNameMapList = prjNameList.stream().map(e -> {
														Map<String, String> map = new HashMap<>();
														map.put("prjName", e.getPrjName());
														return map;
													}).collect(Collectors.toList());
		return prjNameMapList;
	}
	
	@Override
	@Cacheable(value="autocompleteProjectCache", key="{#root.methodName, #project?.creator, #project?.identificationStatus}")
	public List<Project> getProjectIdList(Project project) {
		return projectMapper.getProjectIdList(project);
	}
	
	@Override
	@Cacheable(value="autocompleteProjectCache", key="{#root.methodName, #project?.creator}")
	public List<Project> getProjectModelNameList() {
		return projectMapper.getProjectModelNameList();
	}
	
	@Override
	public String getReviewerList(String adminYn) {
		String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
		List<T2Users> userList = userMapper.selectReviwer(ldapFlag);
		StringBuilder sb = new StringBuilder();
		sb.append( " : " +  ";");
		
		for (T2Users user : userList) {
			user.getUserId();
			sb.append(user.getUserId() + ":" + user.getUserName() + ";");
		}
		
		return sb.toString();
	}
	
	@Override
	public String getAdminUserList() {
		List<T2Users> userList = userMapper.selectAdminUser();
		StringBuilder sb = new StringBuilder();
		sb.append( " : " +  ";");
		
		for (T2Users user : userList) {
			user.getUserId();
			sb.append(user.getUserId() + ":" + user.getUserName() + ";");
		}
		
		return sb.toString();
	}
	
	
	@Override
	public Map<String, Object> getProjectList(Project project) {
		HashMap<String, Object> map = new HashMap<>();
		List<Project> list = null;
		String standardScore = CoCodeManager.getCodeExpString(CoConstDef.CD_VULNERABILITY_MAILING_SCORE, CoConstDef.CD_VULNERABILITY_MAILING_SCORE_STANDARD);
		
		try {
			// OSS NAME으로 검색하는 경우 NICK NAME을 포함하도록 추가
			if (!isEmpty(project.getOssName()) && CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(project.getOssName().toUpperCase())) {
				String[] nickNames = ossService.getOssNickNameListByOssName(project.getOssName());
				
				if (nickNames != null && nickNames.length > 0) {
					project.setOssNickNames(nickNames);
				}
			}
			
			int records = projectMapper.selectProjectTotalCount(project);
			project.setTotListSize(records);
			String ossId = project.getOssId();

			if (!StringUtil.isEmpty(ossId)) {
				list = projectMapper.selectUnlimitedOssComponentBomList(project);
			} else {
				if (CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_NO)) {
					project.setIdentificationSubStatusBat(CoConstDef.FLAG_NO);
				}
				
				list = projectMapper.selectProjectList(project);
				
				if (list != null) {
					boolean isNumberic = false;
					if (!isEmpty(project.getPrjIdName())) {
						isNumberic = project.getPrjIdName().chars().allMatch(Character::isDigit);
					}
					if (isNumberic) {
						List<Project> filteredList = list.stream().filter(e -> e.getPrjId().equalsIgnoreCase(project.getPrjName())).collect(Collectors.toList());
						if (filteredList != null && !filteredList.isEmpty()) {
							List<Project> sortedList = new ArrayList<>();
							sortedList.addAll(filteredList);
							sortedList.addAll(list.stream().filter(e -> !e.getPrjId().equalsIgnoreCase(project.getPrjName())).collect(Collectors.toList()));
							list = sortedList;
						}
					}
					
					ProjectIdentification identification = new ProjectIdentification();
					identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
					identification.setMerge(CoConstDef.FLAG_NO);
					
					// 코드변환처리
					list.forEach(bean -> {
						bean.setStandardScore(Float.valueOf(standardScore));
						// DISTRIBUTION Android Flag
						String androidFlag = CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equalsIgnoreCase(avoidNull(bean.getNoticeType())) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO;
						bean.setAndroidFlag(androidFlag);
						
						if (CoConstDef.FLAG_YES.equals(androidFlag)) {
							bean.setNoticeTypeEtc(CommonFunction.tabTitleSubStr(CoConstDef.CD_PLATFORM_GENERATED, bean.getNoticeTypeEtc()));
						}
						
						// DISTRIBUTION_TYPE
						bean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, bean.getDistributionType()));
						// Project Status
						bean.setStatus(CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_STATUS, bean.getStatus()));
						// Project priority 
						bean.setPriority(CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, bean.getPriority()));
						// Identification Status
						bean.setIdentificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getIdentificationStatus()));
						// Verification Status
						bean.setVerificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getVerificationStatus()));
						// Distribute Status
						String distributionStatus = CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROCESS.equals(bean.getDistributionStatus()) 
														? CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROGRESS : bean.getDistributionStatus();
						bean.setDistributionStatus(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_STATUS, distributionStatus));
						// DIVISION
						bean.setDivision(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, bean.getDivision()));
						
						List<String> permissionCheckList = CommonFunction.checkUserPermissions("", new String[] {bean.getPrjId()}, "project");
						if (permissionCheckList != null) {
							if (bean.getPublicYn().equals(CoConstDef.FLAG_NO)
									&& !CommonFunction.isAdmin() 
									&& !permissionCheckList.contains(loginUserName())) {
								bean.setPermission(0);
								bean.setStatusPermission(0);
							} else {
								if (!CommonFunction.isAdmin() && !permissionCheckList.contains(loginUserName())) {
									bean.setStatusPermission(0);
								} else {
									bean.setStatusPermission(1);
								}
								bean.setPermission(1);
							}
						}
						
						if (!CoConstDef.FLAG_YES.equals(androidFlag)) {
							bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
						} else {
							bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
						}
					});
				}
			}

			map.put("page", project.getCurPage());
			map.put("total", project.getTotBlockSize());
			map.put("records", records);
			map.put("rows", list);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return map;
	}
	
	private void checkIfVulnerabilityResolutionIsFixed(Project bean) {
		String fixedCvssScore = "";
		String notFixedCvssScore = "";
		int fixedCheckCnt = 0;
		List<OssComponents> securityList = projectMapper.selectVulnerabilityResolutionSecurityList(bean);
		
		if (securityList != null && !securityList.isEmpty()) {
			int emptyVersionCnt = securityList.stream().filter(e -> isEmpty(e.getOssVersion())).collect(Collectors.toList()).size();
			int securityListCnt = securityList.stream().filter(e -> !isEmpty(e.getOssVersion()) && Float.valueOf(e.getCvssScore()) >= bean.getStandardScore()).collect(Collectors.toList()).size();
			
			for (OssComponents oc : securityList) {
				if (oc.getVulnerabilityResolution().equalsIgnoreCase("Fixed")) {
					fixedCheckCnt++;
					continue;
				} else {
					if (!isEmpty(notFixedCvssScore)) {
						if (new BigDecimal(oc.getCvssScore()).compareTo(new BigDecimal(notFixedCvssScore)) > 0) {
							notFixedCvssScore = oc.getCvssScore();
						}
					} else {
						notFixedCvssScore = oc.getCvssScore();
					}
				}
				
				if (!oc.getVulnerabilityResolution().equalsIgnoreCase("Fixed")) {
					if (isEmpty(fixedCvssScore)) {
						fixedCvssScore = oc.getCvssScore();
					} else {
						if (new BigDecimal(oc.getCvssScore()).compareTo(new BigDecimal(fixedCvssScore)) > 0) {
							fixedCvssScore = oc.getCvssScore();
						}
					}
				}
			}
			
			if (emptyVersionCnt == securityList.size()) {
				bean.setSecCode("notFixed");
				bean.setCvssScore(notFixedCvssScore);
			} else {
				if (fixedCheckCnt > 0 && fixedCheckCnt == securityListCnt) {
					bean.setSecCode("Fixed");
					bean.setCvssScore(fixedCvssScore);
				} else {
					bean.setSecCode("notFixed");
					bean.setCvssScore(notFixedCvssScore);
				}
			}
		}
		
		bean.setCvssScore(avoidNull(bean.getCvssScore(), CoConstDef.FLAG_NO));
		bean.setSecCode(avoidNull(bean.getSecCode(), CoConstDef.FLAG_NO));
	}

	@Override
	public Project getProjectDetail(Project project) {
		String standardScore = CoCodeManager.getCodeExpString(CoConstDef.CD_VULNERABILITY_MAILING_SCORE, CoConstDef.CD_VULNERABILITY_MAILING_SCORE_STANDARD);
		// master
		project = projectMapper.selectProjectMaster(project.getPrjId());
		
		// 이전에 생성된 프로젝트를 위해 Default를 설정한다.
		Map<String, Object> NoticeInfo = projectMapper.getNoticeType(project.getPrjId());
		if (NoticeInfo == null) {
			NoticeInfo = new HashMap<>();
		}
		project.setNoticeType(avoidNull((String) NoticeInfo.get("noticeType"), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));
		project.setNoticeTypeEtc(avoidNull((String) NoticeInfo.get("noticeTypeEtc")));
		project.setAndroidFlag(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(avoidNull(project.getNoticeType())) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
		// watcher
		List<Project> watcherList = projectMapper.selectWatchersList(project);
		if (!CollectionUtils.isEmpty(watcherList)) {
			T2Users param = new T2Users();
			for (Project wat : watcherList) {
				if (!isEmpty(wat.getPrjUserId())) {
					param.setUserId(wat.getPrjUserId());
					T2Users userInfo = userMapper.getUser(param);
					if (userInfo != null) {
						wat.setPrjDivision(userInfo.getDivision());
						wat.setPrjUserName(userInfo.getUserName());
						String codeNm = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, userInfo.getDivision());
						if (!isEmpty(codeNm)) {
							wat.setPrjDivisionName(codeNm);
						} else {
							wat.setPrjDivisionName(null);
						}
					}
				}
			}
			project.setWatcherList(watcherList);
		}

		// file
		if (!isEmpty(project.getSrcCsvFileId())) {
			project.setCsvFile(projectMapper.selectCsvFile(project.getSrcCsvFileId()));
		}
		
		if (!isEmpty(project.getDepCsvFileId())) {
			project.setDepCsvFile(projectMapper.selectCsvFile(project.getDepCsvFileId()));
		}
		
		if (!isEmpty(project.getBinCsvFileId())) {
			project.setBinCsvFile(projectMapper.selectCsvFile(project.getBinCsvFileId()));
		}
		
		if (!isEmpty(project.getBinBinaryFileId())) {
			project.setBinBinaryFile(projectMapper.selectFileInfoById(project.getBinBinaryFileId()));
		}
		
		project.setAndroidCsvFile(projectMapper.selectAndroidCsvFile(project));
		project.setAndroidNoticeFile(projectMapper.selectAndroidNoticeFile(project));
		
		if (!isEmpty(project.getSrcAndroidResultFileId())) {
			project.setAndroidResultFile(projectMapper.selectAndroidResultFile(project));
		}
		
		if (!isEmpty(project.getScrtCsvFileId())) {
			project.setScrtCsvFile(projectMapper.selectCsvFile(project.getScrtCsvFileId()));
		}
		
		//  button(삭제/복사/저장) view 여부
		if (CommonFunction.isAdmin()) {
			project.setViewOnlyFlag(CoConstDef.FLAG_NO);
		} else {
			project.setViewOnlyFlag(projectMapper.selectViewOnlyFlag(project));
		}
		
		int resultCnt = projectMapper.getOssAnalysisDataCnt(project);
		
		if (resultCnt > 0) {
			Project analysisStatus = projectMapper.getOssAnalysisData(project);
			project.setAnalysisStartDate(analysisStatus.getAnalysisStartDate());
			project.setOssAnalysisStatus(analysisStatus.getOssAnalysisStatus());
		}
		
		if (project.getAndroidFlag().equals(CoConstDef.FLAG_YES)) {
			project.setStandardScore(Float.valueOf(standardScore));
			project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
		} else {
			if (!project.getIdentificationSubStatusBom().equals("0")) {
				project.setStandardScore(Float.valueOf(standardScore));
				project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
			}
		}
		
		List<String> permissionCheckList = CommonFunction.checkUserPermissions("", new String[] {project.getPrjId()}, "project");
		if (permissionCheckList != null) {
			if (avoidNull(project.getPublicYn()).equals(CoConstDef.FLAG_NO)
					&& !CommonFunction.isAdmin() 
					&& !permissionCheckList.contains(loginUserName())) {
				project.setPermission(0);
				project.setStatusPermission(0);
			} else {
				if (!CommonFunction.isAdmin() && !permissionCheckList.contains(loginUserName())) {
					project.setStatusPermission(0);
				} else {
					project.setStatusPermission(1);
				}
				project.setPermission(1);
			}
		}
		
		project.setStandardScore(null);
		return project;
	}
	
	@Override
	public Map<String, Object> getIdentificationGridList(ProjectIdentification identification) {
		return getIdentificationGridList(identification, false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getIdentificationGridList(ProjectIdentification identification, boolean multiUIFlag) {
		final HashMap<String, Object> map = new HashMap<>();
		List<ProjectIdentification> list = null;
		final Map<String, OssMaster> ossInfoMap = CoCodeManager.OSS_INFO_UPPER;
		final List<String> inCpeMatchCheckList = ossInfoMap.values().stream().filter(e -> CoConstDef.FLAG_YES.equals(e.getInCpeMatchFlag())).map(OssMaster::getCveId).distinct().collect(Collectors.toList());
		
		identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		
		if (CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST != null && !CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST.isEmpty()) {
			identification.setRoleOutLicenseIdList(CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST);
		}
		
		final boolean isLoadFromProject = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefPrjId());
		
		if (isLoadFromProject) {
			identification.setReferenceId(identification.getRefPrjId());
		}
		
		final boolean isApplyFromBat = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefBatId());
		
		if (isApplyFromBat) {
			identification.setReferenceId(identification.getRefBatId());
		}

		// bom 일시
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(identification.getReferenceDiv()) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(identification.getReferenceDiv()) || CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(identification.getReferenceDiv())) {
			Map<String, String> obligationTypeMergeMap = new HashMap<>();
			final String reqMergeFlag = identification.getMerge();
			
			if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(identification.getReferenceDiv())) {
				list = projectMapper.selectBomList(identification);
				final Comparator<ProjectIdentification> compare = Comparator
						.comparing(ProjectIdentification::getLicenseTypeIdx)
						.thenComparing(ProjectIdentification::getOssName, Comparator.nullsFirst(Comparator.naturalOrder()))
						.thenComparing(ProjectIdentification::getOssVersion, Comparator.reverseOrder())
						.thenComparing(ProjectIdentification::getDownloadLocation, Comparator.reverseOrder())
						.thenComparing(ProjectIdentification::getLicenseName, Comparator.nullsFirst(Comparator.naturalOrder()))
						.thenComparing(ProjectIdentification::getHomepage, Comparator.naturalOrder())
						.thenComparing(ProjectIdentification::getMergeOrder);
				list.sort(compare);
				
				// For loading 3rd Party ID
				ProjectIdentification thirdPartyOssListParam = new ProjectIdentification();
				thirdPartyOssListParam.setReferenceId(identification.getReferenceId());
				thirdPartyOssListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);

				List<ProjectIdentification> thirdList = null;
				Map<String, Object> thirdlistMap = getIdentificationGridList(thirdPartyOssListParam);
				Map<String, String> thirdPartyNameListByOssMap = new HashMap<>();
				final String keySeparater = ":::";
				if (thirdlistMap != null && (thirdlistMap.containsKey("mainData") || thirdlistMap.containsKey("rows"))) {
					thirdList = (List<ProjectIdentification>) thirdlistMap.get(thirdlistMap.containsKey("mainData") ? "mainData" : "rows");
					if (thirdList != null) {
						thirdList.stream().filter(bean -> CoConstDef.FLAG_NO.equals(avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO))).forEach(bean -> {
							String value = bean.getRefPartnerId();
							if (value != null) {
								String rowOssName = avoidNull(bean.getOssName()).toUpperCase();
								String rowOssVersion = avoidNull(bean.getOssVersion()).toUpperCase();
								String strKey = rowOssName + keySeparater + rowOssVersion + keySeparater;
								if (isEmpty(rowOssName) || "-".equals(rowOssName)) {
									String rowOssLicense = avoidNull(bean.getLicenseName()).toUpperCase();
									strKey = rowOssName + keySeparater + rowOssVersion + keySeparater + rowOssLicense;
								}
								thirdPartyNameListByOssMap.put(strKey, thirdPartyNameListByOssMap.containsKey(strKey) ? thirdPartyNameListByOssMap.get(strKey) + "," + value : value);
							}
						});
					}
				}
				
				// bom merge 버튼을 클릭하고, 표시대상이 있을 경우, 기존에 저장되어 있는 내용을 취득한다.
				// need check의 저장값을 유지하기 위함
				if (CoConstDef.FLAG_YES.equals(reqMergeFlag) && list != null && !list.isEmpty()) {
					identification.setMerge(CoConstDef.FLAG_NO);
					List<ProjectIdentification> bomBeforeList = projectMapper.selectBomList(identification);
					bomBeforeList.sort(compare);
					
					if (bomBeforeList != null) {
						bomBeforeList.forEach(_orgIdentificationBean -> {
							obligationTypeMergeMap.put(_orgIdentificationBean.getRefComponentId(), _orgIdentificationBean.getObligationType());
						});
					}
				}
				
				Map<String, ProjectIdentification> mergeDepMap = new HashMap<>();
				Map<String, ProjectIdentification> batMergeSrcMap = new HashMap<>();
				Map<String, ProjectIdentification> batMergePartnerMap = new HashMap<>();
				
				Map<String, List<OssComponentsLicense>> bomLicenseMap = new HashMap<>();
				List<OssComponentsLicense> bomLicenseList = projectMapper.selectBomLicenseList(identification);
				
				bomLicenseList.forEach(ocl -> {
					String key = ocl.getComponentId();
					List<OssComponentsLicense> bomLicenses = null;
					if (bomLicenseMap.containsKey(key)) {
						bomLicenses = bomLicenseMap.get(ocl.getComponentId());
					} else {
						bomLicenses = new ArrayList<>();
					}
					bomLicenses.add(ocl);
					bomLicenseMap.put(key, bomLicenses);
				});
				
				Map<String, Object> ossInfoCheckMap = new HashMap<>();
 				list.forEach(ll -> {
 					String key = ll.getOssName() + "_" + avoidNull(ll.getOssVersion(), "-");
 					if (!isEmpty(ll.getOssName()) && !ll.getOssName().equals("-") && !CoConstDef.FLAG_YES.equals(avoidNull(ll.getExcludeYn())) && !ossInfoCheckMap.containsKey(key)) {
 						ossInfoCheckMap.put(key, "");
 					}
 					
					ll.setLicenseId(CommonFunction.removeDuplicateStringToken(ll.getLicenseId(), ","));
					ll.setLicenseName(CommonFunction.removeDuplicateStringToken(ll.getLicenseName(), ","));
	  				ll.setCopyrightText(ll.getCopyrightText());
					ll.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);

					if (bomLicenseMap.containsKey(ll.getComponentId())) {
						ll.setOssComponentsLicenseList(bomLicenseMap.get(ll.getComponentId()));
						ll.setObligationLicense(CoConstDef.FLAG_YES.equals(ll.getAdminCheckYn()) ? CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK : CommonFunction.checkObligationSelectedLicense(bomLicenseMap.get(ll.getComponentId())));
					}

					if (!isEmpty(ll.getLicenseId())) {
						boolean licenseTextFlag = false;
						for (String licenseId : ll.getLicenseId().split(",")) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_BY_ID.get(licenseId);
							if (licenseMaster != null && !isEmpty(licenseMaster.getLicenseText())) {
								licenseTextFlag = true;
								break;
							}
						}
						if (!licenseTextFlag) {
							ll.setNotAdminCheck(CoConstDef.FLAG_YES);
						} else {
							ll.setNotAdminCheck(CoConstDef.FLAG_NO);
						}
					}
					
					if (CoConstDef.FLAG_YES.equals(reqMergeFlag)) {
						if (obligationTypeMergeMap.containsKey(ll.getComponentId())) {
							ll.setObligationType(obligationTypeMergeMap.get(ll.getComponentId()));
						} else {
							ll.setObligationType(ll.getObligationLicense());
						}
					}
					
					// grouping 된 file path를 br tag로 변경
					ll.setFilePath(CommonFunction.lineReplaceToBR(ll.getFilePath()));
					
					if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(ll.getReferenceDiv())) {
						if (!batMergeSrcMap.containsKey(ll.getOssName().toUpperCase())) {
							batMergeSrcMap.put(ll.getOssName().toUpperCase(), ll);
						} else if (StringUtil.compareTo(ll.getOssVersion(), batMergeSrcMap.get(ll.getOssName().toUpperCase()).getOssVersion()) > 0) {
							batMergeSrcMap.replace(ll.getOssName().toUpperCase(), ll);
						}
					} else if (CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(ll.getReferenceDiv())) {
						if (!batMergePartnerMap.containsKey(ll.getOssName().toUpperCase())) {
							batMergePartnerMap.put(ll.getOssName().toUpperCase(), ll);
						} else if (StringUtil.compareTo(ll.getOssVersion(), batMergePartnerMap.get(ll.getOssName().toUpperCase()).getOssVersion()) > 0) {
							batMergePartnerMap.replace(ll.getOssName().toUpperCase(), ll);
						}
					}
					
//					String key = (ll.getOssName() + "_" + avoidNull(ll.getOssVersion())).toUpperCase();
//					boolean setCveInfoFlag = false;
//					
//					if (ll.getCvssScoreMax() != null) {
//						String cveId = ll.getCvssScoreMax().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax());
//					}
//					if (ll.getCvssScoreMax1() != null) {
//						String cveId = ll.getCvssScoreMax1().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax1());
//					}
//					if (ll.getCvssScoreMax2() != null) {
//						String cveId = ll.getCvssScoreMax2().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax2());
//					}
//					if (ll.getCvssScoreMax3() != null) {
//						String cveId = ll.getCvssScoreMax3().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax3());
//					}
//					if (cvssScoreMaxList != null && !cvssScoreMaxList.isEmpty()) {
//						if (cvssScoreMaxList.size() > 1) {
//							Collections.sort(cvssScoreMaxList, new Comparator<String>() {
//								@Override
//								public int compare(String o1, String o2) {
//									if (new BigDecimal(o1.split("\\@")[3]).compareTo(new BigDecimal(o2.split("\\@")[3])) > 0) {
//										return -1;
//									}else {
//										return 1;
//									}
//								}
//							});
//						}
//						
//						String[] cveData = cvssScoreMaxList.get(0).split("\\@");
//						ll.setCvssScore(cveData[3]);
//						ll.setCveId(cveData[4]);
//						ll.setVulnYn(CoConstDef.FLAG_YES);
//					} else {
//						String conversionCveInfo = CommonFunction.getConversionCveInfo(ll.getReferenceId(), ossInfoMap, ll, null, cvssScoreMaxList, true);
//						if (conversionCveInfo != null) {
//							String[] conversionCveData = conversionCveInfo.split("\\@");
//							ll.setCvssScore(conversionCveData[3]);
//							ll.setCveId(conversionCveData[4]);
//							ll.setVulnYn(CoConstDef.FLAG_YES);
//						} else {
//							setCveInfoFlag = true;
//						}
//					}
//					
//					cvssScoreMaxList.clear();
//					
//					if (!isEmpty(ll.getOssName()) && ossInfoMap.containsKey(key)) {
//						OssMaster om = ossInfoMap.get(key);
//						if (CoConstDef.FLAG_YES.equals(avoidNull(om.getInCpeMatchFlag())) || setCveInfoFlag) {
//							String cveId = om.getCveId();
//							String cvssScore = om.getCvssScore();
//							String _cvssScore = ll.getCvssScore();
//							
//							if (!isEmpty(cvssScore) && !isEmpty(cveId)) {
//								if (!isEmpty(_cvssScore)) {
//									if (new BigDecimal(cvssScore).compareTo(new BigDecimal(_cvssScore)) > 0) {
//										ll.setCvssScore(cvssScore);
//										ll.setCveId(cveId);
//										ll.setVulnYn(CoConstDef.FLAG_YES);
//									}
//								} else {
//									ll.setCvssScore(cvssScore);
//									ll.setCveId(cveId);
//									ll.setVulnYn(CoConstDef.FLAG_YES);
//								}
//							}
//						}
//					}
					
					if (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(ll.getRefDiv())) {
						String _key = ll.getOssName() + "-" + avoidNull(ll.getOssVersion());
						mergeDepMap.put(_key, ll);
					}
				});
				
 				Map<String, OssMaster> vulnerabilityInfoMap = new HashMap<>();
 				if (!ossInfoCheckMap.isEmpty()) {
 					for (String key : ossInfoCheckMap.keySet()) {
 						String ossName = key.split("_")[0];
 						String ossVersion = key.split("_")[1];
 						if (ossVersion.equals("-")) {
 							ossVersion = "";
 						}
 						OssMaster om = CommonFunction.getOssVulnerabilityInfo(ossName, ossVersion);
 						if (om != null && !isEmpty(om.getCvssScore())) {
 							vulnerabilityInfoMap.put((ossName + "_" + ossVersion).toUpperCase(), om);
 						}
 					}
 					
 					ossInfoCheckMap.clear();
 				}
 				
				// bat merget
				// bat 분석 결과 중에서 oss version이 명시되지 않고, src 또는 3rd party에 동일한 oss 가 존재하는 경우
				// bat 분석 결과를 src 또는 3rd party에 merge 한다.
				List<ProjectIdentification> _list = new ArrayList<>();
				List<String> adminCheckList = new ArrayList<>();
				List<ProjectIdentification> groupList = null;
				Map<String, List<ProjectIdentification>> srcSameLicenseMap = new HashMap<>();
				List<String> egnoreList = new ArrayList<>();
				
				for (ProjectIdentification ll : list) {
					
					// 이미 추가된 oss의 경우
					if (egnoreList.contains(ll.getComponentId())) {
						continue;
					}
					
					int addIdx = -1;
					String ossRestriction = "";
					
					if (!isEmpty(ll.getOssName())) {
						String key = (ll.getOssName() + "_" + avoidNull(ll.getOssVersion())).toUpperCase();
						if (ossInfoMap.containsKey(key)) {
							ossRestriction = ossInfoMap.get(key).getRestriction();
						}
						if (vulnerabilityInfoMap.containsKey(key)) {
							OssMaster om = vulnerabilityInfoMap.get(key);
							ll.setCveId(om.getCveId());
							ll.setCvssScore(om.getCvssScore());
							ll.setVulnYn(CoConstDef.FLAG_YES);
						}
						String mergeKey = ll.getOssName().toUpperCase();
						// main oss로 표시되는 bat oss의 version이 명시되어 있지 않은 경우
						if (CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(ll.getReferenceDiv()) && isEmpty(ll.getMergePreDiv()) && isEmpty(ll.getOssVersion())) {
							ProjectIdentification refBean = null;
							
							if ( batMergeSrcMap.containsKey(mergeKey)) {
								// bat => src
								refBean = batMergeSrcMap.get(mergeKey);
								
								// 설정된 license가 상이하고, bat와 동일한 license가 src에 존재한다면 (최상위 버전이 아닌경우)
								// continue하고 다음 loop에서 merge
								if (!CommonFunction.isSameLicense(refBean.getOssComponentsLicenseList(), ll.getOssComponentsLicenseList())) {
									String ossNameAndVersion = findBatOssOtherVersionWithLicense(ll, refBean, list);
									
									if (!isEmpty(ossNameAndVersion)) {
										List<ProjectIdentification> _batList = null;
										
										if (srcSameLicenseMap.containsKey(ossNameAndVersion)) {
											_batList = srcSameLicenseMap.get(ossNameAndVersion);
											_batList.add(ll);
											srcSameLicenseMap.replace(ossNameAndVersion, _batList);
										} else {
											_batList = new ArrayList<>();
											_batList.add(ll);
											srcSameLicenseMap.put(ossNameAndVersion, _batList);
										}
										
										continue;
									}
								}
								
								ll.setOssId(refBean.getOssId());
								ll.setOssName(refBean.getOssName());
								ll.setOssVersion(refBean.getOssVersion());
								ll.setOssComponentsLicenseList(refBean.getOssComponentsLicenseList());
								
								// 순서 정렬
								addIdx = findOssAppendIndex(CoConstDef.CD_DTL_COMPONENT_ID_SRC, refBean.getComponentId(), list);
								
								if (addIdx > -1) {
									addIdx = addIdx +1;
									ll.setGroupingColumn(refBean.getOssName() + refBean.getOssVersion());
								}					
							} else if ( batMergePartnerMap.containsKey(mergeKey)) {
								// 3rd => bat
								refBean = batMergePartnerMap.get(mergeKey);

								// 3rd에 같은 그룹으로 묶여 있는 모든 oss list를 취득
								ll.setGroupingColumn(refBean.getOssName() + refBean.getOssVersion());
								ll.setOssName(refBean.getOssName());
								ll.setOssVersion(refBean.getOssVersion());
								ll.setOssId(refBean.getOssId());
								
								// bin 에 누락된 정보를 3rd의 첫번재 row에서 채워 넣는다.
								// DOWNLOAD_LOCATION
								if (isEmpty(ll.getDownloadLocation())) {
									ll.setDownloadLocation(refBean.getDownloadLocation());
								}
								
								// HOMEPAGE
								if (isEmpty(ll.getHomepage())) {
									ll.setHomepage(refBean.getHomepage());
								}
								
								// license 정보
								ll.setOssComponentsLicenseList(refBean.getOssComponentsLicenseList());
								ll.setLicenseName(refBean.getLicenseName());
								ll.setCopyrightText(refBean.getCopyrightText());
								
								ll.setObligationLicense(refBean.getObligationLicense());
								ll.setObligationType(refBean.getObligationType());
								
								// 3rd party의 우선순위가 가장 낮기 때문에, 복수건을 취득하는 경우는 없지만, 기능 확장을 고려해서 list 형으로 반환
								groupList = findOssGroupList(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, batMergePartnerMap.get(mergeKey).getOssName(), batMergePartnerMap.get(mergeKey).getOssVersion(), list);
								
								if (groupList != null && !groupList.isEmpty()) {
									for (ProjectIdentification _groupBean : groupList) {
										egnoreList.add(_groupBean.getComponentId());
									}
								}
							}
						} 
						// 3rd party의 경우, bat에 동일한 oss가 없을 경우만 추가 (정렬)
						else if (CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(ll.getReferenceDiv()) && isEmpty(ll.getMergePreDiv()) ) {
							if (existsBatOSS(ll.getOssName(), list)) {
								continue;
							}
						}
					}

					// Set 3rd Party IDs
					String rowOssName = avoidNull(ll.getOssName()).toUpperCase();
					String rowOssVersion = avoidNull(ll.getOssVersion()).toUpperCase();
					String strKey = rowOssName + keySeparater + rowOssVersion + keySeparater;
					if (isEmpty(rowOssName) || rowOssName.equals("-")) {
						String rowOssLicense = avoidNull(ll.getLicenseName()).toUpperCase();
						strKey = rowOssName + keySeparater + rowOssVersion + keySeparater + rowOssLicense;
					}
					if (thirdPartyNameListByOssMap.containsKey(strKey)) {
						ll.setRefPartnerId(thirdPartyNameListByOssMap.get(strKey));
					}
					// License Restriction 저장
					ll.setRestriction(CommonFunction.setLicenseRestrictionListById(ll.getLicenseId(), ossRestriction));
					
					if (addIdx > -1) {
						if (addIdx > _list.size() -1) {
							_list.add(ll);
						} else {
							_list.add(addIdx, ll);
						}
					} else {
						_list.add(ll);
						
						if (groupList != null && !groupList.isEmpty()) {
							_list.addAll(groupList);
						}
					}
					
					if (CoConstDef.FLAG_YES.equals(ll.getAdminCheckYn())) {
						adminCheckList.add(ll.getComponentId());
					}
				}
				
				vulnerabilityInfoMap.clear();
				
				// src oss중에서 bat와 merge할 수 있는 동일한 oss에 최신 version 외 라이선스까지 동일한 bat가 존재하는 경우
				if (!srcSameLicenseMap.isEmpty()) {
					List<ProjectIdentification> _tmp = new ArrayList<>();
					
					for (ProjectIdentification bean : _list) {
						_tmp.add(bean);
						String _key = bean.getOssName() + "-" + avoidNull(bean.getOssVersion());
						
						if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(bean.getReferenceDiv()) && srcSameLicenseMap.containsKey(_key)) {
							for (ProjectIdentification _mergeBean : srcSameLicenseMap.get(_key)) {
								_mergeBean.setOssId(bean.getOssId());
								_mergeBean.setOssName(bean.getOssName());
								_mergeBean.setOssVersion(bean.getOssVersion());
								_mergeBean.setOssComponentsLicenseList(bean.getOssComponentsLicenseList());
								_mergeBean.setGroupingColumn(bean.getGroupingColumn()); // 순서 정렬
								
								_tmp.add(_mergeBean);
							}
						}
					}
					
					_list = _tmp;
				}
				map.put("rows", _list);

				if (adminCheckList.size() > 0) {
					map.put("adminCheckList", adminCheckList);
				}
			} else {
				list = projectMapper.selectOtherBomList(identification);
				final Comparator<ProjectIdentification> compare = Comparator
						.comparing(ProjectIdentification::getLicenseTypeIdx)
						.thenComparing(ProjectIdentification::getOssName, Comparator.nullsFirst(Comparator.naturalOrder()))
						.thenComparing(ProjectIdentification::getOssVersion, Comparator.reverseOrder())
						.thenComparing(ProjectIdentification::getDownloadLocation, Comparator.reverseOrder())
						.thenComparing(ProjectIdentification::getLicenseName, Comparator.nullsFirst(Comparator.naturalOrder()))
						.thenComparing(ProjectIdentification::getHomepage, Comparator.naturalOrder());
				list.sort(compare);
				
				Map<String, Object> ossInfoCheckMap = new HashMap<>();
				list.forEach(ll -> {
 					String key = ll.getOssName() + "_" + avoidNull(ll.getOssVersion(), "-");
 					if (!isEmpty(ll.getOssName()) && !ll.getOssName().equals("-") && !CoConstDef.FLAG_YES.equals(avoidNull(ll.getExcludeYn())) && !ossInfoCheckMap.containsKey(key)) {
 						ossInfoCheckMap.put(key, "");
 					}
				});
				
				Map<String, OssMaster> vulnerabilityInfoMap = new HashMap<>();
 				if (!ossInfoCheckMap.isEmpty()) {
 					for (String key : ossInfoCheckMap.keySet()) {
 						String ossName = key.split("_")[0];
 						String ossVersion = key.split("_")[1];
 						if (ossVersion.equals("-")) {
 							ossVersion = "";
 						}
 						OssMaster om = CommonFunction.getOssVulnerabilityInfo(ossName, ossVersion);
 						if (om != null && !isEmpty(om.getCvssScore())) {
 							vulnerabilityInfoMap.put((ossName + "_" + ossVersion).toUpperCase(), om);
 						}
 					}
 					
 					ossInfoCheckMap.clear();
 				}
				
				// convert max score
				List<String> cvssScoreMaxList = new ArrayList<>();
				List<String> adminCheckList = new ArrayList<>();
				
				Map<String, List<OssComponentsLicense>> bomLicenseMap = new HashMap<>();
				List<OssComponentsLicense> bomLicenseList = projectMapper.selectBomLicenseList(identification);
				
				bomLicenseList.forEach(ocl -> {
					String key = ocl.getComponentId();
					List<OssComponentsLicense> bomLicenses = null;
					if (bomLicenseMap.containsKey(key)) {
						bomLicenses = bomLicenseMap.get(ocl.getComponentId());
					} else {
						bomLicenses = new ArrayList<>();
					}
					bomLicenses.add(ocl);
					bomLicenseMap.put(key, bomLicenses);
				});
				
				list.forEach(ll -> {
					String ossRestriction = "";
					String key = (ll.getOssName() + "_" + avoidNull(ll.getOssVersion())).toUpperCase();
					
					if (ossInfoMap.containsKey(key)) {
						ossRestriction = ossInfoMap.get(key).getRestriction();
					}
					
					if (vulnerabilityInfoMap.containsKey(key)) {
						OssMaster om = vulnerabilityInfoMap.get(key);
						ll.setCveId(om.getCveId());
						ll.setCvssScore(om.getCvssScore());
						ll.setVulnYn(CoConstDef.FLAG_YES);
					}
					
					ll.setLicenseId(CommonFunction.removeDuplicateStringToken(ll.getLicenseId(), ","));
					ll.setLicenseName(CommonFunction.removeDuplicateStringToken(ll.getLicenseName(), ","));
	  				ll.setCopyrightText(ll.getCopyrightText());
					ll.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);

					if (bomLicenseMap.containsKey(ll.getComponentId())) {
						ll.setOssComponentsLicenseList(bomLicenseMap.get(ll.getComponentId()));
						ll.setObligationLicense(CoConstDef.FLAG_YES.equals(ll.getAdminCheckYn()) ? CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK : CommonFunction.checkObligationSelectedLicense(bomLicenseMap.get(ll.getComponentId())));
					}
					
					if (!isEmpty(ll.getLicenseId())) {
						boolean licenseTextFlag = false;
						for (String licenseId : ll.getLicenseId().split(",")) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_BY_ID.get(licenseId);
							if (licenseMaster != null && !isEmpty(licenseMaster.getLicenseText())) {
								licenseTextFlag = true;
								break;
							}
						}
						if (!licenseTextFlag) {
							ll.setNotAdminCheck(CoConstDef.FLAG_YES);
						} else {
							ll.setNotAdminCheck(CoConstDef.FLAG_NO);
						}
					}
					
					if (CoConstDef.FLAG_YES.equals(reqMergeFlag)) {
						if (obligationTypeMergeMap.containsKey(ll.getComponentId())) {
							ll.setObligationType(obligationTypeMergeMap.get(ll.getComponentId()));
						} else {
							ll.setObligationType(ll.getObligationLicense());
						}
					}
					
					// grouping 된 file path를 br tag로 변경
					ll.setFilePath(CommonFunction.lineReplaceToBR(ll.getFilePath()));
					
					// License Restriction 저장
					ll.setRestriction(CommonFunction.setLicenseRestrictionListById(ll.getLicenseId(), ossRestriction));
					
//					boolean setCveInfoFlag = false;
//					
//					if (ll.getCvssScoreMax() != null) {
//						String cveId = ll.getCvssScoreMax().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax());
//					}
//					if (ll.getCvssScoreMax1() != null) {
//						String cveId = ll.getCvssScoreMax1().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax1());
//					}
//					if (ll.getCvssScoreMax2() != null) {
//						String cveId = ll.getCvssScoreMax2().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax2());
//					}
//					if (ll.getCvssScoreMax3() != null) {
//						String cveId = ll.getCvssScoreMax3().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) cvssScoreMaxList.add(ll.getCvssScoreMax3());
//					}
//					if (cvssScoreMaxList != null && !cvssScoreMaxList.isEmpty()) {
//						if (cvssScoreMaxList.size() > 1) {
//							Collections.sort(cvssScoreMaxList, new Comparator<String>() {
//								@Override
//								public int compare(String o1, String o2) {
//									if (new BigDecimal(o1.split("\\@")[3]).compareTo(new BigDecimal(o2.split("\\@")[3])) > 0) {
//										return -1;
//									}else {
//										return 1;
//									}
//								}
//							});
//						}
//						
//						String[] cveData = cvssScoreMaxList.get(0).split("\\@");
//						ll.setCvssScore(cveData[3]);
//						ll.setCveId(cveData[4]);
//						ll.setVulnYn(CoConstDef.FLAG_YES);
//					} else {
//						String conversionCveInfo = CommonFunction.getConversionCveInfo(ll.getReferenceId(), ossInfoMap, ll, null, cvssScoreMaxList, true);
//						if (conversionCveInfo != null) {
//							String[] conversionCveData = conversionCveInfo.split("\\@");
//							ll.setCvssScore(conversionCveData[3]);
//							ll.setCveId(conversionCveData[4]);
//							ll.setVulnYn(CoConstDef.FLAG_YES);
//						} else {
//							setCveInfoFlag = true;
//						}
//					}
//					
//					cvssScoreMaxList.clear();
//					
//					if (ossInfoMap.containsKey(key)) {
//						OssMaster om = ossInfoMap.get(key);
//						if (CoConstDef.FLAG_YES.equals(avoidNull(om.getInCpeMatchFlag())) || setCveInfoFlag) {
//							String cveId = om.getCveId();
//							String cvssScore = om.getCvssScore();
//							if (!isEmpty(cvssScore) && !isEmpty(cveId)) {
//								ll.setCvssScore(cvssScore);
//								ll.setCveId(cveId);
//								ll.setVulnYn(CoConstDef.FLAG_YES);
//							}
//						}
//					}
					
					if (CoConstDef.FLAG_YES.equals(ll.getAdminCheckYn())) {
						adminCheckList.add(ll.getComponentId());
					}
				});
				
				vulnerabilityInfoMap.clear();
				
				map.put("rows", list);

				if (adminCheckList.size() > 0) {
					map.put("adminCheckList", adminCheckList);
				}
			}
		} else { // bom 외 서브 그리드
			// bat oss list를 대상
			// 정렬 순서를 변경한다.
			if (CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(identification.getReferenceDiv()) 
					|| CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(identification.getReferenceDiv())
					|| CoConstDef.CD_DTL_COMPONENT_BAT.equals(identification.getReferenceDiv())) {
				identification.setSortAndroidFlag(CoConstDef.FLAG_YES);
			}
			
			String loadToListComment = "";
			if (!isEmpty(identification.getRefPrjId()) &&
					(CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(identification.getReferenceDiv()) 
							|| CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(identification.getReferenceDiv()) 
							|| CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(identification.getReferenceDiv()))) {
				loadToListComment = "(From Prj " + identification.getRefPrjId() + ")";
			}
			
			HashMap<String, Object> subMap = new HashMap<String, Object>();
			
			// src, bin, bin(android) 의 경우만 comment를 포함한다.
			if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(identification.getReferenceDiv()) 
					|| CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(identification.getReferenceDiv())
					|| CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(identification.getReferenceDiv())
					|| CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(identification.getReferenceDiv())) {
				identification.setIncCommentsFlag(CoConstDef.FLAG_YES);
			}
			
			list = projectMapper.selectIdentificationGridList(identification);
			list.sort(Comparator.comparing(ProjectIdentification::getComponentId));
			
			if (list != null && !list.isEmpty()) {
				Map<String, Object> ossInfoCheckMap = new HashMap<>();
				
				for (ProjectIdentification project : list){
					String _test = project.getOssName().trim() + "_" + project.getOssVersion().trim();
					String _test2 = project.getOssName().trim() + "_" + project.getOssVersion().trim() + ".0";
					String licenseDiv = "";
					
					if (CoCodeManager.OSS_INFO_UPPER.containsKey(_test.toUpperCase())){
						licenseDiv = CoCodeManager.OSS_INFO_UPPER.get(_test.toUpperCase()).getLicenseDiv(); 
					} else if (CoCodeManager.OSS_INFO_UPPER.containsKey(_test2.toUpperCase())){
						licenseDiv = CoCodeManager.OSS_INFO_UPPER.get(_test2.toUpperCase()).getLicenseDiv();
					}
					
					project.setLicenseDiv(licenseDiv);
					
					String key = project.getOssName() + "_" + avoidNull(project.getOssVersion(), "-");
 					if (!isEmpty(project.getOssName()) && !project.getOssName().equals("-") && !CoConstDef.FLAG_YES.equals(avoidNull(project.getExcludeYn())) && !ossInfoCheckMap.containsKey(key)) {
 						ossInfoCheckMap.put(key, "");
 					}
					
					String comments = "";
					if (!isEmpty(loadToListComment)) {
						comments = loadToListComment;
						if (!isEmpty(project.getComments())) {
							comments += " " + project.getComments();
						}
					} else {
						if (!isEmpty(project.getComments())) comments += " " + project.getComments();
					}
					
					if (!isEmpty(comments)) {
						project.setComments(comments);
					}
					
					if (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(identification.getReferenceDiv())) {
						project.setDependencies(avoidNull(project.getDependencies()));
					}
				}
				
				Map<String, OssMaster> vulnerabilityInfoMap = new HashMap<>();
 				if (!ossInfoCheckMap.isEmpty()) {
 					for (String key : ossInfoCheckMap.keySet()) {
 						String ossName = key.split("_")[0];
 						String ossVersion = key.split("_")[1];
 						if (ossVersion.equals("-")) {
 							ossVersion = "";
 						}
 						OssMaster om = CommonFunction.getOssVulnerabilityInfo(ossName, ossVersion);
 						if (om != null && !isEmpty(om.getCvssScore())) {
 							vulnerabilityInfoMap.put((ossName + "_" + ossVersion).toUpperCase(), om);
 						}
 					}
 					
 					ossInfoCheckMap.clear();
 				}
				
				ProjectIdentification param = new ProjectIdentification();
				OssMaster ossParam = new OssMaster();
				
				// components license 정보를 한번에 가져온다
				for (ProjectIdentification bean : list) {
					param.addComponentIdList(bean.getComponentId());
					
					if (!isEmpty(bean.getOssId())) {
						ossParam.addOssIdList(bean.getOssId());
					}
					
					String key = (bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase();
					if (vulnerabilityInfoMap.containsKey(key)) {
						OssMaster om = vulnerabilityInfoMap.get(key);
						bean.setCveId(om.getCveId());
						bean.setCvssScore(om.getCvssScore());
						bean.setVulnYn(CoConstDef.FLAG_YES);
					}
//					boolean setCveInfoFlag = false;
//					
//					if (bean.getCvssScoreMax() != null) {
//						String cveId = bean.getCvssScoreMax().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) {
//							cvssScoreMaxList.add(bean.getCvssScoreMax());
//						}
//					}
//					if (bean.getCvssScoreMax1() != null) {
//						String cveId = bean.getCvssScoreMax1().split("\\@")[4];
//						if (!inCpeMatchCheckList.contains(cveId)) {
//							cvssScoreMaxList.add(bean.getCvssScoreMax1());
//						}
//					}
//					if (cvssScoreMaxList != null && !cvssScoreMaxList.isEmpty()) {
//						if (cvssScoreMaxList.size() > 1) {
//							Collections.sort(cvssScoreMaxList, new Comparator<String>() {
//								@Override
//								public int compare(String o1, String o2) {
//									if (new BigDecimal(o1.split("\\@")[3]).compareTo(new BigDecimal(o2.split("\\@")[3])) > 0) {
//										return -1;
//									}else {
//										return 1;
//									}
//								}
//							});
//						}
//						
//						String[] cveData = cvssScoreMaxList.get(0).split("\\@");
//						bean.setCvssScore(cveData[3]);
//						bean.setCveId(cveData[4]);
//						bean.setVulnYn(CoConstDef.FLAG_YES);
//					}
//					
//					cvssScoreMaxList.clear();
//					
//					if (ossInfoMap.containsKey(key)) {
//						OssMaster om = ossInfoMap.get(key);
//						if (CoConstDef.FLAG_YES.equals(avoidNull(om.getInCpeMatchFlag())) || setCveInfoFlag) {
//							String cveId = om.getCveId();
//							String cvssScore = om.getCvssScore();
//							String _cvssScore = bean.getCvssScore();
//							
//							if (!isEmpty(cvssScore) && !isEmpty(cveId)) {
//								if (!isEmpty(_cvssScore)) {
//									if (new BigDecimal(cvssScore).compareTo(new BigDecimal(_cvssScore)) > 0) {
//										bean.setCvssScore(cvssScore);
//										bean.setCveId(cveId);
//										bean.setVulnYn(CoConstDef.FLAG_YES);
//									}
//								} else {
//									bean.setCvssScore(cvssScore);
//									bean.setCveId(cveId);
//									bean.setVulnYn(CoConstDef.FLAG_YES);
//								}
//							}
//						}
//					}
				}
				
				vulnerabilityInfoMap.clear();
				
				// oss id로 oss master에 등록되어 있는 라이선스 정보를 취득
				Map<String, OssMaster> componentOssInfoMap = null;
				
				if (ossParam.getOssIdList() != null && !ossParam.getOssIdList().isEmpty()) {
					componentOssInfoMap = ossService.getBasicOssInfoListById(ossParam);
				}
				
				Map<String, List<ProjectIdentification>> licenseMap = new HashMap<>();
				List<ProjectIdentification> licenseList = projectMapper.identificationSubGrid(param);
				
				for (ProjectIdentification ocl : licenseList) {
					String key = ocl.getComponentId();
					List<ProjectIdentification> bomLicenses = null;
					if (licenseMap.containsKey(key)) {
						bomLicenses = licenseMap.get(ocl.getComponentId());
					} else {
						bomLicenses = new ArrayList<>();
					}
					ocl.setEditable(CoConstDef.FLAG_YES);
					bomLicenses.add(ocl);
					licenseMap.put(key, bomLicenses);
				}
				
				for (ProjectIdentification bean : list) {
					if (licenseMap.containsKey(bean.getComponentId())) {
						bean.setComponentLicenseList(licenseMap.get(bean.getComponentId()));
					}
				}
				
				licenseMap = null;
				
//				for (ProjectIdentification licenseBean : licenseList) {
//					for (ProjectIdentification bean : list) {
//						if (licenseBean.getComponentId().equals(bean.getComponentId())) {
//							// 수정가능 여부 초기설정
//							licenseBean.setEditable(CoConstDef.FLAG_YES);
//							bean.addComponentLicenseList(licenseBean);
//							
//							break;
//						}
//					}
//				}
				
				// license 정보 등록
				for (ProjectIdentification bean : list) {
					if (bean.getComponentLicenseList() != null) {
						//String licenseText = "";
						String licenseCopy = "";
						
						String ossRestriction = "";
						String key = (bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase();
						if (ossInfoMap.containsKey(key)) {
							ossRestriction = ossInfoMap.get(key).getRestriction();
						}
						
						// multi dual 라이선스의 경우, main row에 표시되는 license 정보는 OSS List에 등록되어진 라이선스를 기준으로 표시한다.
						// ossId가 없는 경우는 기본적으로 subGrid로 등록될 수 없다
						// 이짓거리를 하는 두번째 이유는, subgrid 에서 사용자가 추가한 라이선스와 oss 에 등록되어 있는 라이선스를 구분하기 위함
						if (componentOssInfoMap == null) {
							componentOssInfoMap = new HashMap<>();
						}
						
						OssMaster ossBean = componentOssInfoMap.get(bean.getOssId());
						
						// Restriction 대응을 위해 (BOM 외 추가) License Id를 콤마 구분으로 격납
						String strLicenseIds = "";
						
						if (ossBean != null
								&& CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())
								&& ossBean.getOssLicenses() != null && !ossBean.getOssLicenses().isEmpty()) {
							for (OssLicense ossLicenseBean : ossBean.getOssLicenses()) {
								
								if (!isEmpty(ossLicenseBean.getOssCopyright())) {
									licenseCopy += (!isEmpty(licenseCopy) ? "\r\n" : "") + ossLicenseBean.getOssCopyright();
								}
								
								//삭제 불가 처리
								for (ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
									// license index 까지 비교하는 이유는
									// multi dual 혼용인 경우, 동일한 라이선스가 두번 등록 될 수 있기 때문
									if (ossLicenseBean.getLicenseId().equals(licenseBean.getLicenseId()) 
											&& ossLicenseBean.getOssLicenseIdx().equals(licenseBean.getRnum())) {
										licenseBean.setEditable(CoConstDef.FLAG_NO);
										break;
									}
								}
								
								// Restriction 설정을 위해 license id 격납
								if (!isEmpty(ossLicenseBean.getLicenseId())) {
									strLicenseIds += (!isEmpty(strLicenseIds) ? "," : "") + ossLicenseBean.getLicenseId();
								}
								
							}
							
							// 어짜피 여기서 설정하는 라이선스 이름은 의미가 없음
							if (bean.getComponentLicenseList() != null && bean.getComponentLicenseList().size() == 1 && bean.getComponentLicenseList().get(0) != null) {
								bean.setLicenseName(bean.getComponentLicenseList().get(0).getLicenseName());
							} else if (CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())) {
								bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
							} else {
								// TODO - 여기 수정하면 될거 같음.. multiUIFlag
								bean.setLicenseName(CommonFunction.makeLicenseExpression(ossBean.getOssLicenses()));
							}
						} else {
							for (ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
								if (!isEmpty(licenseBean.getCopyrightText())) {
									licenseCopy += (!isEmpty(licenseCopy) ? "\r\n" : "") + licenseBean.getCopyrightText();
								}

								// Restriction 설정을 위해 license id 격납
								if (!isEmpty(licenseBean.getLicenseId())) {
									strLicenseIds += (!isEmpty(strLicenseIds) ? "," : "") + licenseBean.getLicenseId();
								}
							}
							
							bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
						}
						

						// Restriction 설정
						if (!isEmpty(strLicenseIds) || !isEmpty(ossRestriction)) {
							bean.setRestriction(CommonFunction.setLicenseRestrictionListById(strLicenseIds, ossRestriction));
						}
						
						// customBinary 설정
						if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(identification.getReferenceDiv())
								|| CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(identification.getReferenceDiv())) {
							bean.setCustomBinaryYn(CoConstDef.FLAG_YES);
						}
						
						// 3rd party의 경우 obligation 처리를 위해 license에 따른 obligation 설정을 추가
						if (CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(identification.getReferenceDiv())) {
							bean.setObligationLicense(CommonFunction.checkObligationSelectedLicense2(bean.getComponentLicenseList()));
							
							if (isEmpty(bean.getObligationType())) {
								bean.setObligationType(bean.getObligationLicense());
							}
							
							// need check 인 경우
							if (CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(bean.getObligationLicense())) {
								if (CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType())) {
									bean.setNotify(CoConstDef.FLAG_YES);
									bean.setSource(CoConstDef.FLAG_NO);
								} else if (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) {
									bean.setNotify(CoConstDef.FLAG_YES);
									bean.setSource(CoConstDef.FLAG_YES);
								} else if (CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK_SELECTED.equals(bean.getObligationType())) {
									bean.setNotify(CoConstDef.FLAG_NO);
									bean.setSource(CoConstDef.FLAG_NO);
								} else {
									bean.setNotify("");
									bean.setSource("");
								}
							}
						}
						
						// subGrid의 Item 추출을 위해 별도의 map으로 구성한다.
						// 부몬의 component_id를 key로 관리한다.
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					} else {
						bean.setLicenseName("");
						
						subMap.put(bean.getGridId(), new ArrayList<ProjectIdentification>());
					}
					
					// bat 분석 결과의 경우
					if (!isEmpty(bean.getBatPercentage()) || !isEmpty(bean.getBatStringMatchPercentage())) {
						String batPercentageStr = avoidNull(bean.getBatStringMatchPercentage());
						
						if (CoConstDef.BAT_MATCHED_STR.equalsIgnoreCase(avoidNull(bean.getBatPercentage()).trim())) {
							batPercentageStr = CoConstDef.BAT_MATCHED_STR;
						} else if (!isEmpty(bean.getBatPercentage())) {
							batPercentageStr += " (" + bean.getBatPercentage() + ")";
						}

						bean.setBatStrPlus(batPercentageStr);
						
						// Change bat grid result percentage (UI) Number only
						if (!isEmpty(bean.getBatStringMatchPercentage())) {
							bean.setBatStringMatchPercentageFloat(bean.getBatStringMatchPercentage().replace("%", "").trim());
						}
						
					}
					
					// License Restriction 저장
					bean.setRestriction(CommonFunction.setLicenseRestrictionList(bean.getComponentLicenseList()));
					
					// comments가 null인 경우 grid에서 수정시 저정되지 않기 때문에, 공백으로 치환한다.
					bean.setComments(avoidNull(bean.getComments()));
				}
				

				// 다른 프로젝트에서 load한 경우 component id 초기화
				if (isLoadFromProject || isApplyFromBat) {
					subMap = new HashMap<>();

					// refproject id + "p" + componentid 로 component_id를 재생성 하고, 
					// license 의 경우 재성생한 component_id + 기존 license grid_id의 component_license_id 부분을 결합
					for (ProjectIdentification bean : list) {
						if (isLoadFromProject) {
							bean.setRefPrjId(identification.getRefPrjId());
						} else if (isApplyFromBat) {
							bean.setRefBatId(identification.getRefBatId());
						}
						
						String _compId = CoConstDef.GRID_NEWROW_DEFAULT_PREFIX;
						
						if (isLoadFromProject) {
							_compId += identification.getRefPrjId();
						} else if (isApplyFromBat) {
							_compId += identification.getRefBatId();
						}
						
						_compId += "p" + bean.getComponentId();
						
						bean.setComponentId("");
						bean.setGridId(_compId);

						if (bean.getComponentLicenseList()!=null){
							for (ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
								licenseBean.setComponentId("");
								licenseBean.setGridId(_compId + "-"+ licenseBean.getComponentLicenseId());
							}
						}
						
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					}
				}
			}
			
			// 편집중인 data 가 존재할 경우 append 한다.
			{
				if (identification.getMainDataGridList() != null) {
					for (ProjectIdentification bean : identification.getMainDataGridList()) {
						if (bean.getComponentLicenseList() == null || bean.getComponentLicenseList().isEmpty()){
							//멀티라이센스일 경우
							if (CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())){
								for (List<ProjectIdentification> comLicenseList : identification.getSubDataGridList()) {
									for (ProjectIdentification comLicense : comLicenseList) {
										if (bean.getComponentId().equals(comLicense.getComponentId())){
											bean.addComponentLicenseList(comLicense);
										}
									}
								}
							} else { //싱글라이센스일경우
								ProjectIdentification license = new ProjectIdentification();
								license.setComponentId(bean.getComponentId());
								license.setLicenseId(bean.getLicenseId());
								license.setLicenseName(bean.getLicenseName());
								license.setLicenseText(bean.getLicenseText());
								license.setCopyrightText(bean.getCopyrightText());
								license.setExcludeYn(bean.getExcludeYn());
								bean.addComponentLicenseList(license);
							}
						}
					}

					for (ProjectIdentification bean : identification.getMainDataGridList()) {
						list.add(0, bean);
						
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					}
				}
			}
			
			// exclude row의 재정렬 (가장 마지막으로)
			List<ProjectIdentification> newSortList = new ArrayList<>();
			List<ProjectIdentification> excludeList = new ArrayList<>();
			
			for (ProjectIdentification bean : list) {
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					excludeList.add(bean);
				} else {
					newSortList.add(bean);
				}
			}
			
			newSortList.addAll(excludeList);
			list = newSortList;
			
			map.put("subData", subMap);
			map.put("mainData", list);
		}
		
		return map;
	}

	private String findAddedOssCopyright(String ossId, String licenseId, String ossCopyright) {
		if (!isEmpty(ossId) && !isEmpty(licenseId)) {
			OssMaster bean = CoCodeManager.OSS_INFO_BY_ID.get(ossId);
			if (bean != null) {
				for (OssLicense license : bean.getOssLicenses()) {
					if (licenseId.equals(license.getLicenseId()) && !isEmpty(license.getOssCopyright())) {
						return license.getOssCopyright();
					}
				}
			}
		}

		return ossCopyright;
	}
	
	/**
	 * BAT와 동일한 OSS, license를 가지는 SRC 정보가 있는지 확인
	 * @param ll
	 * @param refBean
	 * @param list
	 * @return
	 */
	private String findBatOssOtherVersionWithLicense(ProjectIdentification ll, ProjectIdentification refBean, List<ProjectIdentification> list) {
		for (ProjectIdentification bean : list) {
			if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(bean.getReferenceDiv()) 
					&& bean.getOssName().equals(refBean.getOssName())
					&& !bean.getOssVersion().equals(refBean.getOssVersion())
					&& bean.getOssName().equals(ll.getOssName())) {
				if (CommonFunction.isSameLicense(bean.getOssComponentsLicenseList(), ll.getOssComponentsLicenseList())) {
					return bean.getOssName() + "-" + avoidNull(bean.getOssVersion());
				}
			}
		}
		
		return null;
	}
	
	private int findOssAppendIndex(String type, String componentId, List<ProjectIdentification> list) {
		int idx = 0;
		
		for (ProjectIdentification bean : list) {
			if (type.equals(bean.getReferenceDiv()) && componentId.equals(bean.getComponentId())) {
				return idx;
			}
			
			idx++;
		}
		
		return -1;
	}
	
	private List<ProjectIdentification> findOssGroupList(String type, String ossName, String ossVersion, List<ProjectIdentification> list) {
		String targetGroup = avoidNull(ossName) + avoidNull(ossVersion);
		List<ProjectIdentification> groupList = new ArrayList<>();
		
		for (ProjectIdentification bean : list) {
			if (type.equals(bean.getReferenceDiv()) && targetGroup.equalsIgnoreCase(bean.getGroupingColumn())) {
				groupList.add(bean);
			}
		}
		
		return groupList;
	}
	
	private boolean existsBatOSS(String ossName, List<ProjectIdentification> list) {
		for (ProjectIdentification bean : list) {
			if (CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(bean.getReferenceDiv()) && isEmpty(bean.getMergePreDiv()) && isEmpty(bean.getOssVersion())
					&& ossName.equalsIgnoreCase(bean.getOssName())) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Map<String, String> getCategoryCodeToJson(String code) {
		Map<String, String> map = new LinkedHashMap<>();
		
		try {
			List<String> list = projectMapper.selectCategoryCode(code);
			String[] arr;
			
			for (String str : list) {
				arr = str.split("\\|");
				map.put(arr[0] + "|" + arr[1], arr[2]);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return map;
	}
	
	@Override
	public Map<String, List<Project>> getModelList(String prjId) {
		List<Project> modelList = projectMapper.selectModelList(prjId);
		
		HashMap<String, List<Project>> map = new HashMap<>();
		map.put("currentModelList", modelList);
		map.put("rows", modelList); // export, project 기본정보등 기존에 참조하고 있던 소스를 위해 rows를 유지
		map.put("delModelList", projectMapper.selectDeleteModelList(prjId));
		
		return map;
	}
	
	@Override
	@Cacheable(value="autocompleteCache", key="#root.methodName")
	public List<ProjectIdentification> getOssNames(ProjectIdentification identification) {
		return projectMapper.getOssNames(identification);
	}
	
	@Override
	public List<ProjectIdentification> getOssVersions(String ossName) {
		return projectMapper.getOssVersions(ossName);
	}
	
	@Override
	public OssNotice setCheckNotice(Project project) {
		OssNotice notice = new OssNotice();
		
		try {
			String prjId = project.getPrjId();
			notice = verificationService.selectOssNoticeOne2(prjId);
			
			String distributeType = avoidNull(project.getDistributeTarget(), CoConstDef.CD_DISTRIBUTE_SITE_SKS); // LGE, NA => LGE로 표기, SKS => SKS로 표기함.
			String distributeCode = CoConstDef.CD_DISTRIBUTE_SITE_SKS.equals(distributeType) ? CoConstDef.CD_NOTICE_DEFAULT_SKS : CoConstDef.CD_NOTICE_DEFAULT;
			
			if (isEmpty(notice.getCompanyNameFull()) 
					&& isEmpty(notice.getDistributionSiteUrl()) 
					&& isEmpty(notice.getEmail())
					&& isEmpty(notice.getAppended())
					&& CoConstDef.FLAG_NO.equals(notice.getEditNoticeYn())
					&& CoConstDef.FLAG_NO.equals(notice.getEditCompanyYn())
					&& CoConstDef.FLAG_NO.equals(notice.getEditDistributionSiteUrlYn())
					&& CoConstDef.FLAG_NO.equals(notice.getEditEmailYn())
					&& CoConstDef.FLAG_NO.equals(notice.getHideOssVersionYn())
					&& CoConstDef.FLAG_NO.equals(notice.getEditAppendedYn())
					&& (CoConstDef.CD_NOTICE_TYPE_GENERAL.equals(notice.getNoticeType()) 
							|| CoConstDef.CD_NOTICE_TYPE_NA.equals(notice.getNoticeType()))){
				// OSS_NOTICE와 OSS_NOTICE_NEW에 정보가 없을경우 default setting
				notice.setEditNoticeYn(CoConstDef.FLAG_NO);
				notice.setEditCompanyYn(CoConstDef.FLAG_YES);
				notice.setEditDistributionSiteUrlYn(CoConstDef.FLAG_YES);
				notice.setEditEmailYn(CoConstDef.FLAG_YES);
				notice.setHideOssVersionYn(CoConstDef.FLAG_NO);
				notice.setEditAppendedYn(CoConstDef.FLAG_NO);
				notice.setPrjId(project.getPrjId());
				
				if (isEmpty(notice.getCompanyNameFull())) {
					notice.setCompanyNameFull(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_FULLNAME));
				}
				
				if (isEmpty(notice.getDistributionSiteUrl())) {
					notice.setDistributionSiteUrl(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_DISTRIBUTE_SITE));
				}
				
				if (isEmpty(notice.getEmail())) {
					notice.setEmail(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_EMAIL));
				}
				
				if (isEmpty(notice.getAppended())){
					notice.setAppended(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_APPENDED));
				}
				
				notice.setNoticeFileFormat(new String[]{"chkAllowDownloadNoticeHTML"});
			} else if (CoConstDef.FLAG_YES.equals(notice.getEditNoticeYn())
					&& CoConstDef.CD_NOTICE_TYPE_GENERAL.equals(notice.getNoticeType())) {
				
			} else {
				if (!isEmpty(notice.getCompanyNameFull())){
					notice.setEditCompanyYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if (!isEmpty(notice.getDistributionSiteUrl())){
					notice.setEditDistributionSiteUrlYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if (!isEmpty(notice.getEmail())){
					notice.setEditEmailYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if (!isEmpty(notice.getAppended())){
					notice.setEditAppendedYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if (CoConstDef.CD_NOTICE_TYPE_GENERAL_WITHOUT_OSS_VERSION.equals(project.getNoticeType())){
					notice.setHideOssVersionYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
			}
			
			notice.setDefaultCompanyNameFull(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_FULLNAME));
			notice.setDefaultDistributionSiteUrl(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_DISTRIBUTE_SITE));
			notice.setDefaultEmail(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_EMAIL));
		} catch (Exception e) {e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		
		return notice;
	}
	
	@Override
	public Project getProjectBasicInfo(String prjId) {
		return projectMapper.selectProjectMaster2(prjId);
	}
	
	@Transactional
	@Override
	public void registVerifyContents(Project project) {
		projectMapper.updateVerifyContents(project);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public History work(Object param) {
		History h = new History();
		
		if (Project.class.equals(param.getClass())) {
			Project vo = (Project) param;
			Project prj = getProjectDetail(vo);
			prj.setModelList(projectMapper.selectModelList(prj.getPrjId()));
			h.sethKey(prj.getPrjId());
			h.sethTitle(prj.getPrjName());
			h.sethType(CoConstDef.EVENT_CODE_PROJECT);
			h.setModifier(prj.getLoginUserName());
			h.setModifiedDate(prj.getModifiedDate());
			h.sethComment("");
			h.sethData(prj);
		} else {
			List<ProjectIdentification> vo = (List<ProjectIdentification>) param;
			List<ProjectIdentification> data = getBomListExcel(vo.get(0));
			Project prj = new Project();
			prj.setPrjId(data.get(0).getReferenceId());
			prj = getProjectDetail(prj);
			
			h.sethKey(prj.getPrjId());
			h.sethTitle(prj.getPrjName());
			h.sethType(CoConstDef.EVENT_CODE_BOM);
			h.setModifier(prj.getLoginUserName());
			h.setModifiedDate(prj.getModifiedDate());
			h.sethComment("");
			h.sethData(data);
		}
		
		return h;
	}
	
	@Override
	public String getDivision(Project project) {
		String result = "";

		result = projectMapper.getDivision(project);

		return result;
	}
	
	@Override
	public void updateReviewer(Project project) {
		projectMapper.updateReviewer(project);
	}

	@Transactional
	@Override
	public void updateReject(Project project) {
		for (String prjId : project.getPrjIds()) {
			project.setPrjId(prjId);
			
			projectMapper.updateReject(project);
		}
	}	

	@Override
	public String getCategoryCode(String code, String gubun) {
		StringBuilder sb = new StringBuilder();

		try {
			List<String> list = projectMapper.selectCategoryCode(code);
			String[] arr;
			
			for (String str : list) {
				arr = str.split("\\|");
				
				if (CoConstDef.FLAG_YES.equals(gubun)) {
					sb.append("<option value=\"" + arr[1] + "\">" + arr[2] + "</option>");
				} else {
					sb.append(arr[1] + ":" + arr[2] + ";");
				}
			}
			
			return sb.toString();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return sb.toString();
	}


	@Transactional
	@Override
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void registProject(Project project) {
		//copy 건
		if ("true".equals(project.getCopy())) {
			String oldId = project.getPrjId();
			project.setPrjId(null);
			project.setCopyPrjId(oldId);
			project.setPublicYn(avoidNull(project.getPublicYn(), CoConstDef.FLAG_YES));
			// project master
			projectMapper.insertProjectMaster(project);
			
			String prjId = project.getPrjId();

			// notice type (packaging에서 project 기본정보로 이동되면서 추가해줘야함
			// verificationMapper에 insert가 있지만, 프로젝트 기본정보에서는 notice type만 결정하기 때문에 별도로 project mapper에 추가
			OssNotice noticeParam = new OssNotice();
			noticeParam.setPrjId(project.getPrjId());
			noticeParam.setNoticeType(avoidNull(project.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));
			
			if (CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(project.getNoticeType())) {
				noticeParam.setNoticeTypeEtc(project.getNoticeTypeEtc());
			}
			
			projectMapper.makeOssNotice(noticeParam);

			// project model insert
			if (project.getModelList().size() > 0) {
				for (Project modelBean : project.getModelList()) {
					modelBean.setPrjId(project.getPrjId());
					modelBean.setModelName(modelBean.getModelName().trim().toUpperCase().replaceAll("\t", ""));
					
					// copy 한 프로젝트의 경우, 배포사이트 연동 정보는 삭제한다.
					// 삭제된 이력이 있는 모델은 추가할 필요 없음
					if (CoConstDef.FLAG_YES.equals(modelBean.getDelYn())) {
						continue;
					}
					
					modelBean.setModifier(null);
					modelBean.setModifiedDate(null);
					modelBean.setOsddSyncYn(null);
					modelBean.setOsddSyncTime(null);
					
					projectMapper.insertProjectModel(modelBean);
				}
			}
			

			// project watcher insert
			if (project.getWatchers()!= null) { // Project 신규 등록과 동일하게 watcher 추가
				ArrayList<Map<String, String>> divisionList = new ArrayList<Map<String, String>>();
				ArrayList<Map<String, String>> emailList = new ArrayList<Map<String, String>>();
				String[] arr;
				
				for (String watcher : project.getWatchers()) {
					Map<String, String> m = new HashMap<String, String>();
					arr = watcher.split("\\/");
					
					if (!"Email".equals(arr[1])){
						project.setPrjDivision(arr[0]);
						
						if (arr.length > 1){
							project.setPrjUserId(arr[1]);
						}else{
							project.setPrjUserId("");
						}
						
						project.setPrjEmail("");

						m.put("division", project.getPrjDivision());
						m.put("userId", project.getPrjUserId());
						divisionList.add(m);
					} else {
						project.setPrjDivision("");
						project.setPrjUserId("");
						project.setPrjEmail(arr[0]);

						m.put("email", project.getPrjEmail());
						emailList.add(m);
					}

					List<Project> watcherList = projectMapper.selectWatchersCheck(project);
					
					if (watcherList.size() == 0){
						projectMapper.insertProjectWatcher(project);
					}
				}
				
				project.setDivisionList(divisionList);
				project.setEmailList(emailList);
				
				projectMapper.deleteProjectWatcher(project);
			}
			
			{
				projectMapper.copyProjectAddList(project);
			}
			
			//나머지 프로젝트 마스터 테이블 카피
			// Identification 관련 정보만 Copy한다.
			// upload한 파일이 있는 경우는 파일 순번을 새롭게 취득하여 재등록한다. 물리파일은 복사하지 않고 공유한다.
			project.setPrjId(oldId);
			Project orgBean = projectMapper.selectProjectMaster2(oldId);
			project.setPrjId(prjId);
			
			{
				Project newBean = new Project();
				newBean.setPrjId(prjId);
				newBean.setModifier(newBean.getLoginUserName());
				
				if (!isEmpty(orgBean.getIdentificationStatus())) {
					newBean.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
				}
				
				newBean.setIdentificationSubStatusPartner(orgBean.getIdentificationSubStatusPartner());
				newBean.setIdentificationSubStatusDep(orgBean.getIdentificationSubStatusDep());
				newBean.setIdentificationSubStatusSrc(orgBean.getIdentificationSubStatusSrc());
				newBean.setIdentificationSubStatusBin(orgBean.getIdentificationSubStatusBin());
				newBean.setIdentificationSubStatusAndroid(orgBean.getIdentificationSubStatusAndroid());
				newBean.setIdentificationSubStatusBat(orgBean.getIdentificationSubStatusBat());
				
				// distribute target이 변경된 경우, 마스터 카테고리는 복사하지 않는다.
				if (!CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(project.getDistributeTarget()) 
						&& avoidNull(project.getDistributeTarget()).equals(orgBean.getDistributeTarget())) {
					newBean.setDistributeMasterCategory(orgBean.getDistributeMasterCategory());
					newBean.setDistributeName(orgBean.getDistributeName());
					newBean.setDistributeSoftwareType(orgBean.getDistributeSoftwareType());
				}
				
				if (!isEmpty(orgBean.getSrcCsvFileId())) {
					newBean.setSrcCsvFileId(fileService.copyPhysicalFile(orgBean.getSrcCsvFileId(), null, true));
				}
				
				if (!isEmpty(orgBean.getDepCsvFileId())) {
					newBean.setDepCsvFileId(fileService.copyPhysicalFile(orgBean.getDepCsvFileId(), null, true));
				}

				if (!isEmpty(orgBean.getBinCsvFileId())) {
					newBean.setBinCsvFileId(fileService.copyPhysicalFile(orgBean.getBinCsvFileId(), null, true));
				}
				
				if (!isEmpty(orgBean.getSrcAndroidCsvFileId())) {
					newBean.setSrcAndroidCsvFileId(fileService.copyPhysicalFile(orgBean.getSrcAndroidCsvFileId(), null, true));
				}
				
				if (!isEmpty(orgBean.getSrcAndroidNoticeFileId())) {
					newBean.setSrcAndroidNoticeFileId(fileService.copyPhysicalFile(orgBean.getSrcAndroidNoticeFileId(), null, true));
				}
				
				if (!isEmpty(orgBean.getSrcAndroidResultFileId())) {
					newBean.setSrcAndroidResultFileId(fileService.copyFileInfo(orgBean.getSrcAndroidResultFileId()));
				}
				
				// copy 시 distributeName 은 복사 되지 않도록 플래그 추가
				newBean.setCopyFlag(CoConstDef.FLAG_YES);
				
				if (CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(avoidNull(orgBean.getNoticeType()))) {
					newBean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
				}
				
				projectMapper.updateProjectMaster(newBean);
			}
		} else {
			boolean isNew = isEmpty(project.getPrjId());
			
			// 신규 프로젝트 생성시, Android model  의 경우, 3rd, src, bin Tab을  N/A 처리한다.
			if (isNew && "A".equals(CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE, project.getDistributionType())) ) {
				 project.setIdentificationSubStatusPartner(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusSrc(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusBin(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusBat(CoConstDef.FLAG_NO);
			}
			
			// admin이 아니라면 creator를 변경하지 않는다.
			if (!CommonFunction.isAdmin()) {
				project.setCreator(null);
			}
			
			project.setPublicYn(avoidNull(project.getPublicYn(), CoConstDef.FLAG_YES));
			project.setSecMailYn(avoidNull(project.getSecMailYn(), CoConstDef.FLAG_YES));
			if(project.getSecMailYn().equals("Y")) {
				project.setSecMailDesc("");
			} else {
				project.setSecMailDesc(avoidNull(project.getSecMailDesc()));
			}
			project.setSecPerson(avoidNull(project.getSecPerson()));
			// if complete value equals 'Y', set
			if (!isNew) {
				final Project prjBean = projectMapper.selectProjectMaster(project.getPrjId());
				
				if (prjBean != null) {
					if (CoConstDef.FLAG_YES.equals(prjBean.getCompleteYn())) {
						project.setCompleteYn(CoConstDef.FLAG_YES);
					}
				}
			}
			// project master
			projectMapper.insertProjectMaster(project);
			
			OssNotice noticeParam = new OssNotice();
			noticeParam.setPrjId(project.getPrjId());
			noticeParam.setNoticeType(avoidNull(project.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));
			
			if (CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(project.getNoticeType())) {
				noticeParam.setNoticeTypeEtc(project.getNoticeTypeEtc());
			}
			
			projectMapper.makeOssNotice(noticeParam);

			// delete model, watcher
			projectMapper.deleteProjectModel(project);

			// project model insert
			if (project.getModelList().size() > 0) {
				for (int i = 0; i < project.getModelList().size(); i++) {
					project.getModelList().get(i).setPrjId(project.getPrjId());
					projectMapper.insertProjectModel(project.getModelList().get(i));
				}
			}
			
			Project result = projectMapper.getProjectBasicInfo(project);
			
			if ("CONF".equals(result.getVerificationStatus()) 
					&& isEmpty(result.getDistributionStatus())
					&& CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(project.getDistributeTarget())) {
				projectMapper.updateProjectDistributionStatus(project.getPrjId(), CoConstDef.CD_DTL_DISTRIBUTE_NA);
			} else if ("CONF".equals(result.getVerificationStatus()) 
					&& CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(result.getDistributionStatus())
					&& !CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(project.getDistributeTarget())) {
				String distributionType = codeMapper.getCodeDetail(CoConstDef.CD_DISTRIBUTION_TYPE, result.getDistributionType()).getCdDtlExp();
				if ("T".equalsIgnoreCase(avoidNull(distributionType))
						|| (CoConstDef.FLAG_NO.equalsIgnoreCase(avoidNull(distributionType)) && verificationService.checkNetworkServer(result.getPrjId()))) {
					projectMapper.updateProjectDistributionStatus(project.getPrjId(), CoConstDef.CD_DTL_DISTRIBUTE_NA);
				} else {
					projectMapper.updateProjectDistributionStatus(project.getPrjId(), null);
				}
			}
			
			if (isNew) {
				// project watcher insert
				ArrayList<Map<String, String>> divisionList = new ArrayList<Map<String, String>>();
				ArrayList<Map<String, String>> emailList = new ArrayList<Map<String, String>>();

				if (project.getWatchers()!= null) {
					String[] arr;
					
					for (String watcher : project.getWatchers()) {
						Map<String, String> m = new HashMap<String, String>();
						arr = watcher.split("\\/");
						
						if (!"Email".equals(arr[1])){
							project.setPrjDivision(arr[0]);
							
							if (arr.length > 1){
								project.setPrjUserId(arr[1]);
							}else{
								project.setPrjUserId("");
							}
							
							project.setPrjEmail("");

							m.put("division", project.getPrjDivision());
							m.put("userId", project.getPrjUserId());
							divisionList.add(m);
						} else {
							project.setPrjDivision("");
							project.setPrjUserId("");
							project.setPrjEmail(arr[0]);

							m.put("email", project.getPrjEmail());
							emailList.add(m);
						}

						List<Project> watcherList = projectMapper.selectWatchersCheck(project);
						
						if (watcherList.size() == 0){
							projectMapper.insertProjectWatcher(project);
						}
					}
				}
				
				project.setDivisionList(divisionList);
				project.setEmailList(emailList);
				
				projectMapper.deleteProjectWatcher(project);
			}
		}
	}

	@Override
	public void copyOssComponentList(Project project, boolean isBom) {
		String copyPrjId = project.getCopyPrjId();
		String prjId = project.getPrjId();
		project.setOldId(copyPrjId);
		
		List<ProjectIdentification> ossComponents = projectMapper.selectOssComponentsList(project);
		
		final List<ProjectIdentification> insertOssComponentList = new ArrayList<>();
		final List<OssComponentsLicense> insertOssComponentLicenseList = new ArrayList<>();
		Map<String, List<OssComponentsLicense>> refComponentIdLicenseMap = new HashMap<>();
		List<String> ossComponentsIdList = new ArrayList<>();
		int ossComponentIdx = 1;
		
		for (ProjectIdentification ossBean : ossComponents) {
			ossBean.setReferenceId(prjId);
			ossBean.setReportFileId(null);
			ossBean.setComponentIdx(Integer.toString(ossComponentIdx++));
			ossBean.setCopyrightText(ossBean.getCopyright());
			ossComponentsIdList.add(ossBean.getComponentId());
			insertOssComponentList.add(ossBean);
		}
		
		OssComponents param = new OssComponents();
		param.setOssComponentsIdList(ossComponentsIdList);
		List<OssComponentsLicense> ossComponentsIdLicenseList = projectMapper.selectOssComponentsIdLicenseList(param);
		ossComponentsIdLicenseList.forEach(ocl -> {
			String key = ocl.getComponentId();
			List<OssComponentsLicense> refComponentIdLicenses = null;
			if (refComponentIdLicenseMap.containsKey(key)) {
				refComponentIdLicenses = refComponentIdLicenseMap.get(ocl.getComponentId());
			} else {
				refComponentIdLicenses = new ArrayList<>();
			}
			refComponentIdLicenses.add(ocl);
			refComponentIdLicenseMap.put(key, refComponentIdLicenses);
		});
		
		for (ProjectIdentification bean : insertOssComponentList) {
			if (refComponentIdLicenseMap.containsKey(bean.getComponentId())) {
				bean.setOssComponentsLicenseList(refComponentIdLicenseMap.get(bean.getComponentId()));
			}
		}
		
		if (!insertOssComponentList.isEmpty()) {
            try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                ProjectMapper mapper = sqlSession.getMapper(ProjectMapper.class);
                int saveCnt = 0;
                for (ProjectIdentification bean : insertOssComponentList) {
                	if (isBom) {
                		mapper.registBomComponents(bean);
                	} else {
                		mapper.insertSrcOssList(bean);
                	}
                    if(saveCnt++ == 1000) {
                        sqlSession.flushStatements();
                        saveCnt = 0;
                    }
                }
                
                if (saveCnt > 0) {
                    sqlSession.flushStatements();
                }
                insertOssComponentLicenseList.addAll(getInsertOssComponentLicenseList(insertOssComponentList));
                
                saveCnt = 0;
                for (OssComponentsLicense bean : insertOssComponentLicenseList) {
                    mapper.registComponentLicense(bean);
                    if (saveCnt++ == 1000) {
                        sqlSession.flushStatements();
                        saveCnt = 0;
                    }
                }
                if (saveCnt > 0) {
                    sqlSession.flushStatements();
                }
                sqlSession.commit();
            }
            
            insertOssComponentList.clear();
            insertOssComponentLicenseList.clear();
            refComponentIdLicenseMap.clear();
            ossComponentsIdList.clear();
        }
		
		if (!isBom) {
			final List<PartnerMaster> insertPartnerMapList = new ArrayList<>();
			List<PartnerMaster> partnerList = partnerMapper.selectThirdPartyMapList(copyPrjId);
			
			if (!CollectionUtils.isEmpty(partnerList)) {
				for (PartnerMaster bean : partnerList) {
					bean.setPrjId(prjId);
					insertPartnerMapList.add(bean);	
				}
			}
			
			if (!insertPartnerMapList.isEmpty()) {
				try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
					PartnerMapper mapper = sqlSession.getMapper(PartnerMapper.class);
	                int saveCnt = 0;
	                for (PartnerMaster bean : insertPartnerMapList) {
	                    mapper.insertPartnerMapList(bean);
	                    if(saveCnt++ == 1000) {
	                        sqlSession.flushStatements();
	                        saveCnt = 0;
	                    }
	                }
	                if (saveCnt > 0) {
	                    sqlSession.flushStatements();
	                }
	                sqlSession.commit();
				}
				
				insertPartnerMapList.clear();
			}
		}
	}
	
	@Transactional
	@Override
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void deleteProject(Project project) {
		if (projectMapper.checkProjectDistributeHis(project) > 0) {
			projectMapper.deleteProjectDistributeHis(project);
		}
		projectMapper.deleteProjectModel(project);
		projectMapper.deleteProjectWatcher(project);
		projectMapper.deleteStatisticsMostUsedInfo(project);
		projectMapper.deleteAddList(project);
		projectMapper.deleteOssNotice(project.getPrjId());
		projectMapper.resetOssComponentsAndLicense(project.getPrjId(), null);
		projectMapper.deleteProjectMaster(project);
	}
	
	@Override
	@Transactional
	public void deleteProjectRefFiles(Project projectInfo) {
		// delete identification files
		deleteFiles(projectInfo.getCsvFile());
		deleteFiles(projectInfo.getAndroidCsvFile());
		deleteFiles(projectInfo.getAndroidNoticeFile());
		deleteFiles(projectInfo.getAndroidResultFile());
		deleteFiles(projectInfo.getBinCsvFile());
		deleteFiles(projectInfo.getBinBinaryFile());
	}
	
	private void deleteFiles(List<T2File> list) {
		if(list != null) {
			for(T2File fileInfo : list) {
				projectMapper.updateDeleteYNByFileSeq(fileInfo);
				projectMapper.deleteFileBySeq(fileInfo);
				fileService.deletePhysicalFile(fileInfo, null);
			}
		}
	}

	@Override
	public Map<String, Object> getOssIdLicenses(ProjectIdentification identification) {
		HashMap<String, Object> map = new HashMap<String, Object>();

		try {
			ProjectIdentification prjOssMaster = projectMapper.getOssId(identification);
			List<ProjectIdentification> Licenselist = projectMapper.getLicenses(prjOssMaster);
			
			if (Licenselist.size() != 0){
				//excludeYn 체크해주기
				Licenselist = CommonFunction.makeLicenseExcludeYn(Licenselist);
				// multi/dual license인 경우 연산식 표기로 변경
				prjOssMaster.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(Licenselist, ","));
			}
			
			map.put("prjOssMaster", prjOssMaster);
			map.put("prjLicense", Licenselist);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return map;
	}
	
	@Override
	public void updateSubStatus(Project project) {
		projectMapper.updateProjectSubStatus(project);

//		ProjectIdentification components = new ProjectIdentification();
//		components.setReferenceId(project.getPrjId());
//		components.setReferenceDiv(project.getReferenceDiv());
//		
//		List<OssComponents> componentsLicense = projectMapper.selectComponentId(components);
//		
//		for (OssComponents oc : componentsLicense) {
//			projectMapper.deleteOssComponentsLicense(oc);
//		}
//		
//		projectMapper.deleteOssComponents(components);
		projectMapper.resetOssComponentsAndLicense(project.getPrjId(), project.getReferenceDiv());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void registComponentsThird(String prjId, String identificationSubStatusPartner, List<OssComponents> ossComponentsList, List<PartnerMaster> thirdPartyList) {
		// 프로젝트 정보를 취득
		Project prjBasicInfo = projectMapper.selectProjectMaster(prjId);
		
		// 프로젝트 상태 정보 변경
		{
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(prjId);
			projectSubStatus.setIdentificationSubStatusPartner((ossComponentsList.isEmpty() && thirdPartyList.isEmpty()) ? "X" : CoConstDef.FLAG_YES);
			
			// 최초 저장시에만 상태 변경
			// row count가 0이어도 사용자가 한번이라도 저장하면 progress 상태로 인지되어야함
			if (isEmpty(prjBasicInfo.getIdentificationStatus())) {
				projectSubStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			}
			
			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			projectMapper.updateProjectMaster(projectSubStatus);
		}
		
		ossComponentsList = convertOssNickName3rd(ossComponentsList);
		
		OssComponents compParam = new OssComponents();
		compParam.setReferenceId(prjId);
		Map<String, ProjectIdentification> orgOssMap = new HashMap<>();
		
		// 기존 등록 oss 정보 취득
		{
			Map<String, Object> _map = getIdentificationThird(compParam);
			List<ProjectIdentification> rows = (List<ProjectIdentification>) _map.get("rows");
			
			if (rows != null) {
				for (ProjectIdentification bean : rows) {
					orgOssMap.put(bean.getComponentId(), bean);
				}
			}
		}
		
		List<OssComponents> updateList = new ArrayList<>();
		List<OssComponents> insertFromPrjList = new ArrayList<>();
		List<ProjectIdentification> insertOssComponentList = new ArrayList<>();
		
		List<String> refComponentIdList = new ArrayList<>();
		Map<String, ProjectIdentification> refOssComponentsMap = new HashMap<>();
		Map<String, List<OssComponentsLicense>> refComponentIdLicenseMap = new HashMap<>();
		final List<OssComponentsLicense> insertOssComponentLicenseList = new ArrayList<>();
		List<String> deleteList = new ArrayList<>();
		
		// 순서대로 등록 1) update, 2) delete, 3)insert from project , 4) insert from 3rd 
		for (OssComponents bean : ossComponentsList) {
			// 기존에 등록되어 있는 경우는 update
			if (orgOssMap.containsKey(avoidNull(bean.getComponentId()))) {
				// exclude 여부만 사용
				updateList.add(bean);
				deleteList.add(bean.getComponentId());
			} else {
				refComponentIdList.add(bean.getRefComponentId());
				insertFromPrjList.add(bean);
			}
		}
		
		if (!refComponentIdList.isEmpty()) {
			OssComponents param = new OssComponents();
			param.setOssComponentsIdList(refComponentIdList);
			List<ProjectIdentification> refOssComponentList = projectMapper.selectOssComponentsThirdCopy(param);
			refOssComponentList.forEach(oc -> {
				refOssComponentsMap.put(oc.getComponentId(), oc);
			});
			
			List<OssComponentsLicense> refComponentIdLicenseList = projectMapper.selectOssComponentsIdLicenseList(param);
			refComponentIdLicenseList.forEach(ocl -> {
				String key = ocl.getComponentId();
				List<OssComponentsLicense> refComponentIdLicenses = null;
				if (refComponentIdLicenseMap.containsKey(key)) {
					refComponentIdLicenses = refComponentIdLicenseMap.get(ocl.getComponentId());
				} else {
					refComponentIdLicenses = new ArrayList<>();
				}
				refComponentIdLicenses.add(ocl);
				refComponentIdLicenseMap.put(key, refComponentIdLicenses);
			});
		}
		
		if (!CollectionUtils.isEmpty(insertFromPrjList)) {
			for (OssComponents bean : insertFromPrjList) {
				if (refOssComponentsMap.containsKey(bean.getRefComponentId())) {
					ProjectIdentification pi = refOssComponentsMap.get(bean.getRefComponentId());
					pi.setOssName(bean.getOssName());
					pi.setReferenceId(prjId);
					pi.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
					pi.setExcludeYn(bean.getExcludeYn());
					pi.setRefPartnerId(bean.getRefPartnerId());
					pi.setRefPrjId(bean.getRefPrjId());
					insertOssComponentList.add(pi);
					
					if (refComponentIdLicenseMap.containsKey(bean.getRefComponentId())) {
						pi.setOssComponentsLicenseList(refComponentIdLicenseMap.get(bean.getRefComponentId()));
					}
				}
			}
		}
		
		Map<String, PartnerMaster> thirdPartyMap = new HashMap<>();
		
		// 기존 등록 3rd Map 정보 취득
		{
			Map<String, Object> _map = getThirdPartyMap(prjId);
			
			if (_map != null) {
				for (PartnerMaster bean : (List<PartnerMaster>) _map.get("rows")) {
					thirdPartyMap.put(bean.getPartnerId(), bean);
				}
			}
		}
		
		List<PartnerMaster> thirdPartyUpdateList = new ArrayList<>();
		List<PartnerMaster> thirdPartyInsertList = new ArrayList<>();
		List<String> thirdPartyDeleteList = new ArrayList<>();
		Map<String, PartnerMaster> deleteCheckMap = new HashMap<>();
		
		for (PartnerMaster bean : thirdPartyList) {
			// 기존에 등록되어 있는 경우는 update
			if (thirdPartyMap.containsKey(avoidNull(bean.getPartnerId()))) {
				thirdPartyUpdateList.add(bean);
			} else {
				thirdPartyInsertList.add(bean);
				deleteCheckMap.put(bean.getPartnerId(), bean);
			}
		}
		
		for (PartnerMaster bean : thirdPartyUpdateList) {
			deleteCheckMap.put(bean.getPartnerId(), bean);
		}
		
		for (String s : thirdPartyMap.keySet()) {
			if (!deleteCheckMap.containsKey(s)) {
				thirdPartyDeleteList.add(s);
			}
		}
		
		registOssComponentsThird(prjId, prjBasicInfo, updateList, deleteList, insertOssComponentList, insertOssComponentLicenseList, thirdPartyInsertList, thirdPartyDeleteList);
	}

	private void registOssComponentsThird(String prjId, Project prjBasicInfo, List<OssComponents> updateList, List<String> deleteList, List<ProjectIdentification> insertOssComponentList,
			List<OssComponentsLicense> insertOssComponentLicenseList, List<PartnerMaster> thirdPartyInsertList, List<String> thirdPartyDeleteList) {
		
		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
			ProjectMapper mapper = sqlSession.getMapper(ProjectMapper.class);
			PartnerMapper pMapper = sqlSession.getMapper(PartnerMapper.class);
			
			int cnt = 0;
			// 1) update
			for (OssComponents bean : updateList) {
				mapper.updatePartnerOssList(bean);
				if (cnt++ == 1000) {
					sqlSession.flushStatements();
					cnt = 0;
				}
			}
			if (cnt > 0) {
                sqlSession.flushStatements();
            }
			
			cnt = 0;
			// 2) delete
			OssComponents param = new OssComponents();
			param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
			param.setReferenceId(prjId);
			param.setOssComponentsIdList(deleteList);
			List<String> deleteComponentIds = mapper.getDeleteOssComponentsLicenseIds(param);
			param.setOssComponentsIdList(deleteComponentIds);
			
			if (deleteComponentIds.size() > 0){
				mapper.deleteOssComponentsLicenseWithIds(param);
				mapper.deleteOssComponentsWithIds2(param);
				sqlSession.flushStatements();
			}
			
			if (!CollectionUtils.isEmpty(insertOssComponentList)) {
				prjBasicInfo.setReferenceId(prjId);
				prjBasicInfo.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
				int ossComponentIdx = mapper.selectOssComponentMaxIdx(prjBasicInfo);
				
				for (ProjectIdentification bean : insertOssComponentList) {
					bean.setComponentIdx(String.valueOf(ossComponentIdx++));
					mapper.insertSrcOssList(bean);
					if (cnt++ == 1000) {
						sqlSession.flushStatements();
						cnt = 0;
					}
				}
				if (cnt > 0) {
	                sqlSession.flushStatements();
	            }
				insertOssComponentLicenseList.addAll(getInsertOssComponentLicenseList(insertOssComponentList));
			}
			
			cnt = 0;
            for (OssComponentsLicense bean : insertOssComponentLicenseList) {
                mapper.registComponentLicense(bean);
                if (cnt++ == 1000) {
                    sqlSession.flushStatements();
                    cnt = 0;
                }
            }
            if (cnt > 0) {
                sqlSession.flushStatements();
            }
            
			cnt = 0;
			for (PartnerMaster bean : thirdPartyInsertList) {
				bean.setPrjId(prjId);
				
				pMapper.insertPartnerMapList(bean);
				if (cnt++ == 1000) {
					sqlSession.flushStatements();
					cnt = 0;
				}
			}
			if (cnt > 0) {
                sqlSession.flushStatements();
            }
			
			cnt = 0;
			if (!thirdPartyDeleteList.isEmpty()) {
				PartnerMaster thirdPartyDeleteParam = new PartnerMaster();
				thirdPartyDeleteParam.setPrjId(prjId);
				thirdPartyDeleteParam.setThirdPartyPartnerIdList(thirdPartyDeleteList);
				
				pMapper.deletePartnerMapList(thirdPartyDeleteParam);
				sqlSession.flushStatements();
			}
			
			sqlSession.commit();
		}
		
		updateList.clear();
		insertOssComponentList.clear();
		insertOssComponentLicenseList.clear();
		thirdPartyInsertList.clear();
		thirdPartyDeleteList.clear();
	}

	private List<OssComponents> convertOssNickName3rd(List<OssComponents> ossComponents) {
		List<String> ossCheckParam = new ArrayList<>();
		List<OssMaster> ossNickNameList = null;
		Map<String, OssMaster> ossNickNameConvertMap = new HashMap<>();
		
		for (OssComponents bean : ossComponents) {
			String _ossName = avoidNull(bean.getOssName()).trim();
			
			if (!isEmpty(_ossName) && !"-".equals(_ossName) && !ossCheckParam.contains(_ossName)) {
				ossCheckParam.add(_ossName);
			}
		}
		
		if (!ossCheckParam.isEmpty()) {
			OssMaster param = new OssMaster();
			param.setOssNames(ossCheckParam.toArray(new String[ossCheckParam.size()]));
			ossNickNameList = projectMapper.checkOssNickName(param);
			
			if (ossNickNameList != null) {
				for (OssMaster bean : ossNickNameList) {
					ossNickNameConvertMap.put(bean.getOssNickname().toUpperCase(), bean);
				}
			}
		}

		for (OssComponents bean : ossComponents) {
			if (ossNickNameConvertMap.containsKey(avoidNull(bean.getOssName()).trim().toUpperCase())) {
				bean.setOssName(ossNickNameConvertMap.get(avoidNull(bean.getOssName()).trim().toUpperCase()).getOssName());
			}
			
			// license nickname 체크
			if (!avoidNull(bean.getLicenseName()).contains(",")) {
				String _licenseName = avoidNull(bean.getLicenseName()).trim();
				
				if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(_licenseName.toUpperCase())) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_licenseName.toUpperCase());
					
					if (licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
						for (String s : licenseMaster.getLicenseNicknameList()) {
							if (_licenseName.equalsIgnoreCase(s)) {
								bean.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp()));
								
								break;
							}
						}
					}
				}
			}
		}
		
		return ossComponents;
	}
	
	/* 
	 * 프로젝트 데이터 (Name, Version) 중복 체크
	 */
	@Override
	public boolean existProjectData(Project project) {
		int result = projectMapper.selectDuplicatedProject(project);
		return result > 0 ? true : false;
	}
	
	@Override
	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project) {
		registSrcOss(ossComponent, ossComponentLicense, project, CoConstDef.CD_DTL_COMPONENT_ID_SRC);
	}
	
	@Override
	public void registDepOss(List<ProjectIdentification> ossComponent,	List<List<ProjectIdentification>> ossComponentLicense, Project project) {
		registSrcOss(ossComponent, ossComponentLicense, project, CoConstDef.CD_DTL_COMPONENT_ID_DEP);
	}

	@Override
	public void registBinOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project) {
		registSrcOss(ossComponent, ossComponentLicense, project, CoConstDef.CD_DTL_COMPONENT_ID_BIN);
	}


	@Override
	@Transactional
	public void registSrcOss(List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense, Project project, String refDiv) {
		// 한건도 없을시 프로젝트 마스터 SRC 사용가능여부가 N이면 N 그외 null
		if (ossComponent.size()==0){
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(project.getPrjId());
			
			if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
				if (!StringUtil.isEmpty(project.getIdentificationSubStatusSrc())){
					projectSubStatus.setIdentificationSubStatusSrc(project.getIdentificationSubStatusSrc());
				} else {
					projectSubStatus.setIdentificationSubStatusSrc("X");
				}
			} else if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(refDiv)) {
				if (!StringUtil.isEmpty(project.getIdentificationSubStatusBin())){
					projectSubStatus.setIdentificationSubStatusBin(project.getIdentificationSubStatusBin());
				} else {
					projectSubStatus.setIdentificationSubStatusBin("X");
				}
			} else {
				if (!StringUtil.isEmpty(project.getIdentificationSubStatusAndroid())){
					projectSubStatus.setIdentificationSubStatusAndroid(project.getIdentificationSubStatusAndroid());
				} else {
					projectSubStatus.setIdentificationSubStatusAndroid("X");
				}
			}

			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			
			projectSubStatus.setReferenceDiv(refDiv);
			projectMapper.updateProjectMaster(projectSubStatus);
		}
		
		ossComponent = convertOssNickName(ossComponent);
		ossComponentLicense = convertLicenseNickName(ossComponentLicense);
		String refId = project.getReferenceId();
		
		updateOssComponentList(project, refDiv, refId, ossComponent, ossComponentLicense);

		// delete file
		if (project.getCsvFile() != null && project.getCsvFile().size() > 0) {
			deleteUploadFile(project, refDiv);
		}
		
		// 파일 등록
		if (!isEmpty(project.getDepCsvFileId()) || !isEmpty(project.getSrcCsvFileId()) || !isEmpty(project.getSrcAndroidCsvFileId()) || !isEmpty(project.getSrcAndroidNoticeFileId()) || !isEmpty(project.getBinCsvFileId()) || !isEmpty(project.getBinBinaryFileId())){
			projectMapper.updateFileId(project);
			
			if (project.getCsvFileSeq() != null) {
				for (int i = 0; i < project.getCsvFileSeq().size(); i++) {
					projectMapper.updateFileBySeq(project.getCsvFileSeq().get(i));
				}				
			}
		}
		
		// bin android 의 경우 다른 프로젝트에서 load한 정보를 save할 경우, notice html과 result text 정보를 변경한다.
		if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv) && CoConstDef.FLAG_YES.equals(project.getLoadFromAndroidProjectFlag())) {
			if (isEmpty(project.getSrcAndroidResultFileId())) {
				project.setSrcAndroidResultFileId(null);
			}
			
			projectMapper.updateAndroidNoticeFileInfoWithLoadFromProject(project);
		}
	}

	public void updateFileId(Project project) {
		projectMapper.updateFileId(project);
	}
	
	private void deleteUploadFile(Project project, String refDiv) {
		Project prjFileCheck = projectMapper.getProjectBasicInfo(project);
		boolean fileDeleteCheckFlag = false;
		
		if(CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(refDiv)) {
			if(isEmpty(project.getDepCsvFileId()) && !isEmpty(prjFileCheck.getDepCsvFileId())) {
				project.setDepCsvFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
			if (project.getCsvFileSeq().size() == 0 && !isEmpty(prjFileCheck.getSrcCsvFileId())) {
				project.setSrcCsvFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(refDiv)) {
			if (isEmpty(project.getBinCsvFileId()) && !isEmpty(prjFileCheck.getBinCsvFileId())) {
				project.setBinCsvFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
			if (isEmpty(project.getBinBinaryFileId()) && !isEmpty(prjFileCheck.getBinBinaryFileId())) {
				project.setBinBinaryFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
		} else {
			if (isEmpty(project.getSrcAndroidCsvFileId()) && !isEmpty(prjFileCheck.getSrcAndroidCsvFileId())) {
				project.setSrcAndroidCsvFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
			if (isEmpty(project.getSrcAndroidNoticeFileId()) && !isEmpty(prjFileCheck.getSrcAndroidNoticeFileId())) {
				project.setSrcAndroidNoticeFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
			if(isEmpty(project.getSrcAndroidNoticeXmlId()) && !isEmpty(prjFileCheck.getSrcAndroidNoticeXmlId())) {
				project.setSrcAndroidNoticeXmlFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
		}
		
		if (project.getCsvFile() != null && project.getCsvFile().size() > 0) {
			for (int i = 0; i < project.getCsvFile().size(); i++) {
				projectMapper.updateDeleteYNByFileSeq(project.getCsvFile().get(i));
				fileService.deletePhysicalFile(project.getCsvFile().get(i), "Identification");
				projectMapper.deleteFileBySeq(project.getCsvFile().get(i));
			}
		}
		
		if (fileDeleteCheckFlag) {
			projectMapper.updateFileId2(project);
		}
	}
	
	@Override
	@Transactional
	public void registOss(List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense, String refId, String refDiv) {
		updateOssComponentList(new Project(), refDiv, refId, ossComponent, ossComponentLicense);
	}

	@Override
	@Transactional
	public void updateOssComponentList(Project project, String refDiv, String refId, List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense) {
		
		if(StringUtil.isEmpty(refId)) {
			refId = project.getPrjId();
		}
		
		if (!CoConstDef.CD_DTL_COMPONENT_BAT.equals(refDiv) && !CollectionUtils.isEmpty(ossComponent)) {
			Project projectStatus = projectMapper.selectProjectMaster(refId);
			
			// 최초 상태이면 PROG 
			if (StringUtil.isEmpty(projectStatus.getIdentificationStatus())) {
				projectStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			}
			
			if (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(refDiv)) {
				if(!StringUtil.isEmpty(project.getIdentificationSubStatusDep())){
					projectStatus.setIdentificationSubStatusDep(project.getIdentificationSubStatusDep());
				} else {
					projectStatus.setIdentificationSubStatusDep(CoConstDef.FLAG_YES);
				}
			} else if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
				// 프로젝트 마스터 SRC 사용가능여부가 N 이면 N 그외 Y
				if(!StringUtil.isEmpty(project.getIdentificationSubStatusSrc())){
					projectStatus.setIdentificationSubStatusSrc(project.getIdentificationSubStatusSrc());
				} else {
					projectStatus.setIdentificationSubStatusSrc(CoConstDef.FLAG_YES);
				}
			} else if(CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(refDiv)) {
				// 프로젝트 마스터 SRC 사용가능여부가 N 이면 N 그외 Y
				if(!StringUtil.isEmpty(project.getIdentificationSubStatusBin())){
					projectStatus.setIdentificationSubStatusBin(project.getIdentificationSubStatusBin());
				} else {
					projectStatus.setIdentificationSubStatusBin(CoConstDef.FLAG_YES);
				}
			} else {
				// 프로젝트 마스터 SRC 사용가능여부가 N 이면 N 그외 Y
				if(!StringUtil.isEmpty(project.getIdentificationSubStatusAndroid())){
					projectStatus.setIdentificationSubStatusAndroid(project.getIdentificationSubStatusAndroid());
				} else {
					projectStatus.setIdentificationSubStatusAndroid(CoConstDef.FLAG_YES);
				}
			}

			projectStatus.setModifier(projectStatus.getLoginUserName());
			projectMapper.updateProjectMaster(projectStatus);
		}

		project.setReferenceDiv(refDiv);
		project.setReferenceId(refId);
		
		int ossComponentIdx = projectMapper.selectOssComponentMaxIdx(project);
		// Delete project all components and license
		projectMapper.resetOssComponentsAndLicense(refId, refDiv);
		
		// 컴포넌트 등록
		if (CollectionUtils.isEmpty(ossComponent)) {
			return;
		}
		
		final Map<String, List<OssComponentsLicense>> componentMultiLicenseMap = makeComponentMultiLicenseMap(ossComponentLicense);
		String componentId;
		String downloadLocationUrl;
		String homepageUrl;
		final List<ProjectIdentification> insertOssComponentListWithComponentId = new ArrayList<>();
		final List<ProjectIdentification> insertOssComponentList = new ArrayList<>();
		final List<OssComponentsLicense> insertOssComponentLicenseList = new ArrayList<>();
		for(ProjectIdentification ossBean : ossComponent) {
			// oss_id를 다시 찾는다. (oss name과 oss id가 일치하지 않는 경우가 있을 수 있음)
			ossBean = CommonFunction.findOssIdAndName(ossBean);
			if (isEmpty(ossBean.getOssId())) {
				ossBean.setOssId(null);
			}
			downloadLocationUrl = ossBean.getDownloadLocation();
			if (!StringUtil.isEmpty(downloadLocationUrl) && downloadLocationUrl.endsWith("/")) {
				ossBean.setDownloadLocation(downloadLocationUrl.substring(0, downloadLocationUrl.length()-1));
			} else if (StringUtil.isEmpty(downloadLocationUrl)) {
				ossBean.setDownloadLocation("");
			}
			homepageUrl = ossBean.getHomepage();
			if (!StringUtil.isEmpty(homepageUrl) && homepageUrl.endsWith("/")) {
				ossBean.setHomepage(homepageUrl.substring(0, homepageUrl.length()-1));
			} else if (StringUtil.isEmpty(homepageUrl)) {
				ossBean.setHomepage("");
			}
			if (avoidNull(ossBean.getTlsh()).equalsIgnoreCase("TNULL")) {
				ossBean.setTlsh("0");
			}
			componentId = StringUtil.avoidNull(ossBean.getComponentId(), ossBean.getGridId());
			
			// set component licnese
			if(componentMultiLicenseMap.containsKey(componentId)) {
				ossBean.setOssComponentsLicenseList(componentMultiLicenseMap.get(componentId));
			} else {
				ossBean.addOssComponentsLicense(CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE));
			}
			if (!isEmpty(ossBean.getCopyrightText())) {
				ossBean.setCopyrightText(StringUtils.trimWhitespace(ossBean.getCopyrightText()));
			}
			// android project의 경우, bom 처리를 하지 않기 때문에, bom save에서 처리하는 obligation type을 여기서 설정해야한다.
			if(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv)) {
				ossBean.setObligationType(CommonFunction.checkObligationSelectedLicense(ossBean.getOssComponentsLicenseList()));
				ossBean.setBomWithAndroidFlag(CoConstDef.FLAG_YES);
			}
			
			ossBean.setReferenceId(refId);
			ossBean.setReferenceDiv(refDiv);
			if (StringUtil.isEmpty(ossBean.getComponentIdx())) {
				ossBean.setComponentIdx(Integer.toString(++ossComponentIdx));
			}
			
			// 신규인 경우만 compoentId를 생성
			// 업데이트되는 경우 기존 componentId를 그대로 사용한다.(화면에 표시되는 ID로 커뮤니케이션 이력 유지) 
			if(!componentId.contains(CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
				insertOssComponentListWithComponentId.add(ossBean);
			} else {
				insertOssComponentList.add(ossBean);
			}
		}
		
		if (!insertOssComponentList.isEmpty() || !insertOssComponentListWithComponentId.isEmpty()) {
            try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                ProjectMapper mapper = sqlSession.getMapper(ProjectMapper.class);
                int saveCnt = 0;
                for (ProjectIdentification bean : insertOssComponentListWithComponentId) {
                    mapper.insertSrcOssList(bean);
                    if(saveCnt++ == 1000) {
                        sqlSession.flushStatements();
                        saveCnt = 0;
                    }
                }
                for (ProjectIdentification bean : insertOssComponentList) {
                    mapper.insertSrcOssList(bean);
                    if(saveCnt++ == 1000) {
                        sqlSession.flushStatements();
                        saveCnt = 0;
                    }
                }
                
                if (saveCnt > 0) {
                    sqlSession.flushStatements();
                }
                insertOssComponentLicenseList.addAll(getInsertOssComponentLicenseList(insertOssComponentListWithComponentId));
                insertOssComponentLicenseList.addAll(getInsertOssComponentLicenseList(insertOssComponentList));
                
                saveCnt = 0;
                for (OssComponentsLicense bean : insertOssComponentLicenseList) {
                    mapper.registComponentLicense(bean);
                    if (saveCnt++ == 1000) {
                        sqlSession.flushStatements();
                        saveCnt = 0;
                    }
                }
                if (saveCnt > 0) {
                    sqlSession.flushStatements();
                }
                sqlSession.commit();
            }
            
            insertOssComponentListWithComponentId.clear();
            insertOssComponentList.clear();
            insertOssComponentLicenseList.clear();
        }
		
//	
//		
//		
//		// 컴포넌트 마스터 라이센스 지우기
//		ProjectIdentification prj = new ProjectIdentification();
//		
//		if (isEmpty(refId)) {
//			refId = project.getPrjId();
//		}
//		
//		prj.setReferenceId(refId);
//		prj.setReferenceDiv(refDiv);
//		List<OssComponents> componentId = projectMapper.selectComponentId(prj);
//		
//		for (int i = 0; i < componentId.size(); i++) {
//			projectMapper.deleteOssComponentsLicense(componentId.get(i));
//		}
//		
//		if (!CoConstDef.CD_DTL_COMPONENT_BAT.equals(refDiv)) {
//			if (!ossComponent.isEmpty()) {
//				Project projectStatus = projectMapper.selectProjectMaster(refId);
//				
//				// 최초 상태이면 PROG 
//				if (StringUtil.isEmpty(projectStatus.getIdentificationStatus())) {
//					projectStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
//				}
//				
//				if (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(refDiv)) {
//					if(!StringUtil.isEmpty(project.getIdentificationSubStatusDep())){
//						projectStatus.setIdentificationSubStatusDep(project.getIdentificationSubStatusDep());
//					} else {
//						projectStatus.setIdentificationSubStatusDep(CoConstDef.FLAG_YES);
//					}
//				} else if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
//					// 프로젝트 마스터 SRC 사용가능여부가 N 이면 N 그외 Y
//					if (!StringUtil.isEmpty(project.getIdentificationSubStatusSrc())){
//						projectStatus.setIdentificationSubStatusSrc(project.getIdentificationSubStatusSrc());
//					} else {
//						projectStatus.setIdentificationSubStatusSrc(CoConstDef.FLAG_YES);
//					}
//				} else if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(refDiv)) {
//					// 프로젝트 마스터 SRC 사용가능여부가 N 이면 N 그외 Y
//					if (!StringUtil.isEmpty(project.getIdentificationSubStatusBin())){
//						projectStatus.setIdentificationSubStatusBin(project.getIdentificationSubStatusBin());
//					} else {
//						projectStatus.setIdentificationSubStatusBin(CoConstDef.FLAG_YES);
//					}
//				} else {
//					// 프로젝트 마스터 SRC 사용가능여부가 N 이면 N 그외 Y
//					if (!StringUtil.isEmpty(project.getIdentificationSubStatusAndroid())){
//						projectStatus.setIdentificationSubStatusAndroid(project.getIdentificationSubStatusAndroid());
//					} else {
//						projectStatus.setIdentificationSubStatusAndroid(CoConstDef.FLAG_YES);
//					}
//				}
//	
//				projectStatus.setModifier(projectStatus.getLoginUserName());
//				projectMapper.updateProjectMaster(projectStatus);
//			}
//		}
//
//		project.setReferenceDiv(refDiv);
//		project.setReferenceId(refId);
//		
//		int ossComponentIdx = projectMapper.selectOssComponentMaxIdx(project);
//		
//		//deleteRows
//		List<String> deleteRows = new ArrayList<String>();
//		
//		// 컴포넌트 등록	
//		for (int i = 0; i < ossComponent.size(); i++) {
//			// SRC STATUS 등록
//			
//			ProjectIdentification ossBean = ossComponent.get(i);
//			
//			// oss_id를 다시 찾는다. (oss name과 oss id가 일치하지 않는 경우가 있을 수 있음)
//			ossBean = CommonFunction.findOssIdAndName(ossBean);
//			if (isEmpty(ossBean.getOssId())) {
//				ossBean.setOssId(null);
//			}
//			
//			String downloadLocationUrl = ossBean.getDownloadLocation();
//			String homepageUrl = ossBean.getHomepage();
//			
//			if (!isEmpty(downloadLocationUrl)) {
//				if (downloadLocationUrl.endsWith("/")) {
//					ossBean.setDownloadLocation(downloadLocationUrl.substring(0, downloadLocationUrl.length()-1));
//				}
//			}
//			
//			if (!isEmpty(homepageUrl)) {
//				if (homepageUrl.endsWith("/")) {
//					ossBean.setHomepage(homepageUrl.substring(0, homepageUrl.length()-1));
//				}
//			}
//			
//			//update
//			if (!ossBean.getGridId().contains(CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
//				//ossComponents 등록
//				// android project의 경우, bom 처리를 하지 않기 때문에, bom save에서 처리하는 obligation type을 여기서 설정해야한다.
//				if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv)) {
//					List<OssComponentsLicense> _list = new ArrayList<>();
//					
//					if (CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())) {
//						for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
//							for (ProjectIdentification comLicense : comLicenseList) {
//								if (ossBean.getComponentId().equals(comLicense.getComponentId())){
//									// multi license oss에 license를 추가한 경우, license 명을 입력하지 않은 경우는 무시
//									if (isEmpty(comLicense.getLicenseName()) && isEmpty(comLicense.getLicenseText()) && isEmpty(comLicense.getOssCopyright())) {
//										continue;
//									}
//									
//									_list.add(CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI));
//								}
//							}
//						}
//					} else {
//						_list.add(CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE));
//					}
//					
//					ossBean.setObligationType(CommonFunction.checkObligationSelectedLicense(_list));
//					ossBean.setBomWithAndroidFlag(CoConstDef.FLAG_YES);
//				}
//				
//				projectMapper.updateSrcOssList(ossBean);
//				deleteRows.add(ossBean.getComponentId());
//				
//				//멀티라이센스일 경우
//				if (CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())){
//					List<String> duplicateLicense = new ArrayList<String>();
//					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
//						for (ProjectIdentification comLicense : comLicenseList) {
//							if (ossBean.getComponentId().equals(comLicense.getComponentId())){
//								if (!isEmpty(comLicense.getLicenseId()) && duplicateLicense.contains(comLicense.getLicenseId())) {
//									continue;
//								}
//								
//								// multi license oss에 license를 추가한 경우, license 명을 입력하지 않은 경우는 무시
//								if ((isEmpty(comLicense.getLicenseName()) 
//										&& isEmpty(comLicense.getLicenseText()) 
//										&& isEmpty(comLicense.getOssCopyright()))) {
//									OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
//									projectMapper.registComponentLicense(license);
//									break;
//								}
//								
//								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
//								duplicateLicense.add(comLicense.getLicenseId());
//								
//								// 라이센스 등록
//								projectMapper.registComponentLicense(license);
//							}
//						}
//					}
//				} else { //싱글라이센스일경우
//					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
//					// 라이센스 등록
//					projectMapper.registComponentLicense(license);
//				}
//			} else { //insert
//				//ossComponents 등록
//				String exComponentId = ossBean.getGridId();
//				ossBean.setReferenceId(refId);
//				ossBean.setReferenceDiv(refDiv);
//				
//				// android project의 경우, bom 처리를 하지 않기 때문에, bom save에서 처리하는 obligation type을 여기서 설정해야한다.
//				if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv)) {
//					List<OssComponentsLicense> _list = new ArrayList<>();
//					
//					if (CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())) {
//
//						for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
//							for (ProjectIdentification comLicense : comLicenseList) {
//								String gridId = comLicense.getGridId();
//								
//								if (isEmpty(gridId)) {
//									continue;
//								}
//								
//								gridId = gridId.split("-")[0];
//								
//								if (exComponentId.equals(comLicense.getComponentId())
//										|| exComponentId.equals(gridId)){
//									_list.add(CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI));
//								}
//							}
//						}
//					} else {
//						_list.add(CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE));
//					}
//					
//					ossBean.setObligationType(CommonFunction.checkObligationSelectedLicense(_list));
//					ossBean.setBomWithAndroidFlag(CoConstDef.FLAG_YES);
//				}
//				
//				// insert시 매번 max idx를 select 하면 
//				ossBean.setComponentIdx(Integer.toString(ossComponentIdx++));
//				projectMapper.insertSrcOssList(ossBean);
//				deleteRows.add(ossBean.getComponentId());
//				
//				//멀티라이센스일 경우
//				if (CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())){
//					List<String> duplicateLicense = new ArrayList<String>();
//					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
//						for (ProjectIdentification comLicense : comLicenseList) {
//							String gridId = comLicense.getGridId();
//							
//							if (isEmpty(gridId)) {
//								continue;
//							}
//							
//							gridId = gridId.split("-")[0];
//							
//							if (exComponentId.equals(comLicense.getComponentId()) || exComponentId.equals(gridId)){
//								if (!isEmpty(comLicense.getLicenseId()) && duplicateLicense.contains(comLicense.getLicenseId())) {
//									continue;
//								}
//								
//								// multi license oss에 license를 추가한 경우, license 명을 입력하지 않은 경우는 무시
//								if ((isEmpty(comLicense.getLicenseName()) 
//									&& isEmpty(comLicense.getLicenseText()) 
//									&& isEmpty(comLicense.getOssCopyright()))) {
//									OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
//									projectMapper.registComponentLicense(license);
//									break;
//								}
//								
//								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
//								// 컴포넌트 ID 설정
//								license.setComponentId(ossBean.getComponentId());
//								duplicateLicense.add(comLicense.getLicenseName()); 
//								
//								// 라이센스 등록
//								projectMapper.registComponentLicense(license);
//							}
//						}
//					}
//				} else { // 싱글라이센스일경우
//					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
//					// 라이센스 등록
//					projectMapper.registComponentLicense(license);
//				}
//			}
//		}
//		
//		// delete
//		OssComponents param = new OssComponents();
//		param.setReferenceDiv(refDiv);
//		param.setReferenceId(refId);
//		param.setOssComponentsIdList(deleteRows);
//		
//		projectMapper.deleteOssComponentsWithIds(param);
	}
	
	/**
	 * make component license list
	 * @param ossComponentList
	 * @return
	 */
	private List<OssComponentsLicense> getInsertOssComponentLicenseList(List<ProjectIdentification> ossComponentList) {
		final List<OssComponentsLicense> insertOssComponentLicenseList = new ArrayList<>();
		ossComponentList.stream()
		.filter(regComponet -> !CollectionUtils.isEmpty(regComponet.getOssComponentsLicenseList()))
		.forEach(regComponet -> regComponet.getOssComponentsLicenseList().forEach(_license -> {
			_license.setComponentId(regComponet.getComponentId());
			insertOssComponentLicenseList.add(_license);
		}));
		return insertOssComponentLicenseList;
	}

	/**
	 * make component Multi license Map List
	 * @param ossComponentLicense
	 * @return
	 */
	private Map<String, List<OssComponentsLicense>> makeComponentMultiLicenseMap(List<List<ProjectIdentification>> ossComponentLicense) {
		final Map<String, List<OssComponentsLicense>> componentMultiLicenseMap = new HashMap<>();
		List<OssComponentsLicense> licenseList;
		String _componentId;
		String _licenseId;
		for (List<ProjectIdentification> compLicenseList : ossComponentLicense) {
			for (ProjectIdentification compLicense : compLicenseList) {
				_componentId = isEmpty(compLicense.getComponentId()) ? compLicense.getGridId() : compLicense.getComponentId();
				_licenseId = compLicense.getLicenseId();
				if(componentMultiLicenseMap.containsKey(_componentId)) {
					licenseList = componentMultiLicenseMap.get(_componentId);
				} else {
					licenseList = new ArrayList<>();
				}
				
				// multi license oss에 license 명을 입력하지 않은 경우는 Single license
				if((StringUtil.isEmpty(compLicense.getLicenseName()) 
						&& StringUtil.isEmpty(compLicense.getLicenseText()) 
						&& StringUtil.isEmpty(compLicense.getOssCopyright()))) {
					break;
				}
				
				// check duplicate license id
				boolean isDuplicatedLicenseId = false;
				for(OssComponentsLicense _license : licenseList) {
					if(!StringUtil.isEmpty(_licenseId) && StringUtil.avoidNull(_license.getLicenseId(), "").equals(_licenseId)) {
						isDuplicatedLicenseId = true;
						break;
					}
				}
				if(isDuplicatedLicenseId) {
					continue;
				}

				licenseList.add(CommonFunction.reMakeLicenseBean(compLicense, CoConstDef.LICENSE_DIV_MULTI));
				componentMultiLicenseMap.put(_componentId, licenseList);
			}
		}
		return componentMultiLicenseMap;
	}
	
	@Transactional
	private void addOssComponentByBinaryInfo(List<OssComponents> componentList, Map<String, List<Map<String, Object>>> binaryRegInfoMap) {
		for (OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			String componentId = bean.getComponentId();
			
			if (isEmpty(binaryName)) {
				continue;
			}
			
			if (!binaryRegInfoMap.containsKey(binaryName)) {
				continue;
			}
			
			List<Map<String, Object>> binaryInfoList = (List<Map<String, Object>>) binaryRegInfoMap.get(binaryName);
			
			boolean addOssComponentFlag = false;
			for (Map<String, Object> binaryInfo : binaryInfoList) {
				if (binaryInfo.containsKey("ossName")) {
					Map<String, OssMaster> ossInfo = CoCodeManager.OSS_INFO_UPPER;
					String ossName = (String) binaryInfo.get("ossName");
					String ossVersion = "";
					String _binaryLicenseStr = "";
					
					if (binaryInfo.containsKey("ossVersion")) {
						ossVersion = (String) binaryInfo.get("ossVersion");
					}
					
					if (binaryInfo.containsKey("license")) {
						_binaryLicenseStr = (String) binaryInfo.get("license");
					}
					
					
					String key = ossName + "_" + ossVersion;
					
					OssMaster ossBean = ossInfo.get(key.toUpperCase());
					boolean isEmptyOss = (ossBean == null || "-".equals(ossName));
					
					// update를 위해
					// ossmaster => projectIdentification 으로 변한
					ProjectIdentification updateBean = new ProjectIdentification();
					
					if (ossBean != null) {
						if (!isEmptyOss) {
							updateBean.setOssId(ossBean.getOssId());
						}
						
						updateBean.setOssName(isEmptyOss ? "-" : ossBean.getOssNameTemp()); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
						updateBean.setOssVersion(isEmptyOss ? ossVersion : ossBean.getOssVersion());
						updateBean.setDownloadLocation(isEmptyOss ? "" : ossBean.getDownloadLocation());
						updateBean.setHomepage(isEmptyOss ? "" : ossBean.getHomepage());
						updateBean.setCopyrightText(isEmptyOss ? "" : ossBean.getCopyright());
					} else {
						updateBean.setOssId(null);
						updateBean.setOssName(ossName); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
						updateBean.setOssVersion(ossVersion);
						updateBean.setDownloadLocation(null);
						updateBean.setHomepage(null);
						updateBean.setCopyrightText(null);
					}
					
					// 기존값 유지
					updateBean.setFilePath(bean.getFilePath()); 
					updateBean.setExcludeYn(bean.getExcludeYn());
					updateBean.setBinaryName(bean.getBinaryName());
					updateBean.setBinaryNotice(bean.getBinaryNotice());
					updateBean.setCustomBinaryYn(bean.getCustomBinaryYn());
					
					// 하나의 binary에 대해서 여러개의 OSS가 적용된 경우, 최초 한번만 업데이트하고 이후부터는 신규 등록한다.
					if (addOssComponentFlag) {
						updateBean.setReferenceId(bean.getReferenceId());
						updateBean.setReferenceDiv(bean.getReferenceDiv());
						
						projectMapper.insertOssComponents(updateBean);
						
						componentId = updateBean.getComponentId();
					} else {
						updateBean.setComponentId(componentId);
						projectMapper.updateSrcOssList(updateBean);
						addOssComponentFlag = true;
						
						// 기존 license 삭제
						projectMapper.deleteOssComponentsLicense(bean);
					}
						
					List<String> selectedLicenseIdList = new ArrayList<>();
					
					if (!isEmpty(_binaryLicenseStr)) {
						for (String _licenseName : _binaryLicenseStr.split(",")) {
							if (isEmpty(_licenseName)) {
								continue;
							}
							
							_licenseName = _licenseName.trim();
							
							String _licenseId = CommonFunction.getLicenseIdByName(_licenseName);
							
							if (!isEmpty(_licenseId)) {
								selectedLicenseIdList.add(_licenseId);
							}
						}
					}
					
					List<OssComponentsLicense> updateLicenseList = new ArrayList<>();
						
					// oss name이 하이픈이 아니라면, OSS List에 등록된 정보를 기준으로 취합
					if (!isEmptyOss) {
						boolean hasSelectedLicense = false;
						
						for (OssLicense license : ossBean.getOssLicenses()) {
							OssComponentsLicense componentLicense = new OssComponentsLicense();
							
							componentLicense.setComponentId(componentId);
							componentLicense.setLicenseId(license.getLicenseId());
							componentLicense.setLicenseName(license.getLicenseName());
							// license text 설정은 불필요함
							
							if (selectedLicenseIdList.contains(componentLicense.getLicenseId())) {
								hasSelectedLicense = true;
								componentLicense.setExcludeYn(CoConstDef.FLAG_NO);
							}
							
							updateLicenseList.add(componentLicense);
						}
						
						for (OssComponentsLicense license : updateLicenseList) {
							if (hasSelectedLicense) {
								license.setExcludeYn(avoidNull(license.getExcludeYn(), CoConstDef.FLAG_YES));
							} else {
								license.setExcludeYn(CoConstDef.FLAG_NO);
							}
							
							projectMapper.insertOssComponentsLicense(license);
						}
					} else {
						// oss name이 하이픈이라면 라이선스 binary db에 등록된 license 정보를 그대로 등록한다.
						// 이러한 경우는 license가 복수개로 등록되어 있을 수 없음
						OssComponentsLicense license = new OssComponentsLicense();
						license.setExcludeYn(CoConstDef.FLAG_NO);
						license.setComponentId(componentId);
						
						// binary db에 등록된 license 정보가 license master에 등록되어 있다면 master 정보를 사용
						if (!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_binaryLicenseStr.toUpperCase().trim());
							license.setLicenseId(licenseMaster.getLicenseId());
							license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName()));
						} else {
							// 등록되어 있지 않다면, license name만 등록한다.
							// 이러한 경우는 사실항 존재할 수 없음
							license.setLicenseName(avoidNull(_binaryLicenseStr));
						}
						
						projectMapper.insertOssComponentsLicense(license);
					}
				}
			}
		}
	}
	
	@Transactional
	private void addOssComponentByBinaryInfoAndroid(List<OssComponents> componentList, Map<String, List<Map<String, Object>>> binaryRegInfoMap) {
		for (OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			
			if (binaryName.indexOf("/") > -1) {
				binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
			}
			
			if (isEmpty(binaryName)) {
				continue;
			}
			
			// 사용자가 입력한 oss가 있으면 설정하지 않음
			if (!isEmpty(bean.getOssName())) {
				continue;
			}
			
			if (!binaryRegInfoMap.containsKey(binaryName)) {
				continue;
			}
			
			List<Map<String, Object>> binaryInfoList = binaryRegInfoMap.get(binaryName);
			
			boolean addOssComponentFlag = false;
			
			for (Map<String, Object> binaryInfo : binaryInfoList) {
				if (binaryInfo.containsKey("ossName")) {
					Map<String, OssMaster> ossInfo = CoCodeManager.OSS_INFO_UPPER;
					String ossName = (String) binaryInfo.get("ossName");
					String ossVersion = "";
					String _binaryLicenseStr = "";
					
					if (binaryInfo.containsKey("ossVersion")) {
						ossVersion = (String) binaryInfo.get("ossVersion");
					}
					
					if (binaryInfo.containsKey("license")) {
						_binaryLicenseStr = (String) binaryInfo.get("license");
					}
					
					String key = ossName + "_" + ossVersion;
					
					if ("-".equals(ossName) || ossInfo.containsKey(key.toUpperCase())) {
						// oss name + version 이 일치하는 oss 가 존재하면, update 한다.
						boolean isEmptyOss = "-".equals(ossName);
						OssMaster ossBean = ossInfo.get(key.toUpperCase());
						
						// update를 위해
						// ossmaster => projectIdentification 으로 변한
						ProjectIdentification updateBean = new ProjectIdentification();
						
						if (!isEmptyOss) {
							updateBean.setOssId(ossBean.getOssId());
						}
						
						updateBean.setOssName(isEmptyOss ? "-" : ossBean.getOssNameTemp()); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
						updateBean.setOssVersion(isEmptyOss ? ossVersion : ossBean.getOssVersion());
						updateBean.setDownloadLocation(isEmptyOss ? "" : ossBean.getDownloadLocation());
						updateBean.setHomepage(isEmptyOss ? "" : ossBean.getHomepage());
						updateBean.setCopyrightText(isEmptyOss ? "" : ossBean.getCopyright());
						
						// 기존값 유지
						updateBean.setFilePath(bean.getFilePath()); 
						updateBean.setExcludeYn(bean.getExcludeYn());
						updateBean.setBinaryName(bean.getBinaryName());
						updateBean.setBinaryNotice(bean.getBinaryNotice());
						updateBean.setCustomBinaryYn(bean.getCustomBinaryYn());
						
						// 하나의 binary에 대해서 여러개의 OSS가 적용된 경우, 최초 한번만 업데이트하고 이후부터는 신규 등록한다.
						if (addOssComponentFlag) {
							updateBean.setReferenceId(bean.getReferenceId());
							updateBean.setReferenceDiv(bean.getReferenceDiv());
							
							projectMapper.insertOssComponents(updateBean);
						} else {
							updateBean.setComponentId(bean.getComponentId());
							
							projectMapper.updateSrcOssList(updateBean);
							addOssComponentFlag = true;
							
							// 기존 license 삭제
							projectMapper.deleteOssComponentsLicense(bean);
						}
						

						List<String> selectedLicenseIdList = new ArrayList<>();
						
						if (!isEmpty(_binaryLicenseStr)) {
							for (String _licenseName : _binaryLicenseStr.split(",")) {
								if (isEmpty(_licenseName)) {
									continue;
								}
								
								_licenseName = _licenseName.trim();
								String _licenseId = CommonFunction.getLicenseIdByName(_licenseName);
								
								if (!isEmpty(_licenseId)) {
									selectedLicenseIdList.add(_licenseId);
								}
							}
						}
						List<OssComponentsLicense> updateLicenseList = new ArrayList<>();
						
						// oss name이 하이픈이 아니라면, OSS List에 등록된 정보를 기준으로 취합
						if (!isEmptyOss) {
							boolean hasSelectedLicense = false;
							
							for (OssLicense license : ossBean.getOssLicenses()) {
								OssComponentsLicense componentLicense = new OssComponentsLicense();
								componentLicense.setComponentId(bean.getComponentId());
								componentLicense.setLicenseId(license.getLicenseId());
								componentLicense.setLicenseName(license.getLicenseName());
								
								// license text 설정은 불필요함
								if (selectedLicenseIdList.contains(componentLicense.getLicenseId())) {
									hasSelectedLicense = true;
									componentLicense.setExcludeYn(CoConstDef.FLAG_NO);
								}
								
								updateLicenseList.add(componentLicense);
							}
							
							for (OssComponentsLicense license : updateLicenseList) {
								if (hasSelectedLicense) {
									license.setExcludeYn(avoidNull(license.getExcludeYn(), CoConstDef.FLAG_YES));
								} else {
									license.setExcludeYn(CoConstDef.FLAG_NO);
								}
								
								projectMapper.insertOssComponentsLicense(license);
							}
						} else {
							// oss name이 하이픈이라면 라이선스 binary db에 등록된 license 정보를 그대로 등록한다.
							// 이러한 경우는 license가 복수개로 등록되어 있을 수 없음
							OssComponentsLicense license = new OssComponentsLicense();
							license.setExcludeYn(CoConstDef.FLAG_NO);
							license.setComponentId(addOssComponentFlag ? updateBean.getComponentId() : bean.getComponentId());
							
							// binary db에 등록된 license 정보가 license master에 등록되어 있다면 master 정보를 사용
							if (!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
								LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_binaryLicenseStr.toUpperCase().trim());
								license.setLicenseId(licenseMaster.getLicenseId());
								license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName()));
							} else {
								// 등록되어 있지 않다면, license name만 등록한다.
								// 이러한 경우는 사실항 존재할 수 없음
								license.setLicenseName(avoidNull(_binaryLicenseStr));
							}
							
							projectMapper.insertOssComponentsLicense(license);
						}				
					}
				}
			}
		}
	}

	@Override
	public Map<String, List<String>> nickNameValid(String prjId, List<ProjectIdentification> ossComponentList, List<List<ProjectIdentification>> ossComponentLicenseList) {
		List<String> ossNickNameCheckResult = new ArrayList<>();
		List<String> licenseNickNameCheckResult = new ArrayList<>();
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		List<String> ossCheckParam = new ArrayList<>();
		List<String> licenseCheckParam = new ArrayList<>();
		
		List<ProjectIdentification> adminCheckList = projectMapper.selectAdminCheckList(prjId);
		List<String> refComponentIdList = new ArrayList<>();
		List<String> ossNameVersionList = new ArrayList<>();
		Map<String, String> ossComponentInfo = new HashMap<>();
		
		for (ProjectIdentification pi : adminCheckList) {
			refComponentIdList.add(pi.getRefComponentId());
			if (!pi.getOssName().isEmpty() && !pi.getOssName().equals("")) {
				String ossName = CoCodeManager.OSS_INFO_UPPER_NAMES.get(pi.getOssName().toUpperCase());
				ossNameVersionList.add((ossName + "_" + avoidNull(pi.getOssVersion())).toUpperCase());
			}
		}
		
		for (ProjectIdentification bean : ossComponentList) {
			String _ossName = avoidNull(bean.getOssName()).trim();
			String _ossVersion = avoidNull(bean.getOssVersion().trim(), "N/A");
			String _componentId = avoidNull(bean.getComponentId(), bean.getGridId());
			ossComponentInfo.put(_componentId, _ossName + " (" + _ossVersion + ")");
			
			if (!isEmpty(_ossName) && !"-".equals(_ossName)) {
				boolean adminCheckFlag = false;
				if (refComponentIdList.contains(bean.getComponentId()) || ossNameVersionList.contains((_ossName + "_" + avoidNull(bean.getOssVersion())).toUpperCase())) {
					adminCheckFlag = true;
				}
				if (!ossCheckParam.contains(_ossName) && !adminCheckFlag) {
					ossCheckParam.add(_ossName);
				}
			}
			
			if (CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())) {
				// 여기서 할 필요 없음
			} else {
				String _licenseName = avoidNull(bean.getLicenseName()).trim();
				if (!isEmpty(_licenseName) && !licenseCheckParam.contains(_licenseName)) {
					licenseCheckParam.add(_componentId + "|" + _licenseName);
				}
			}
		}

		// multi license의 경우 nickname check대상 추출
		for (List<ProjectIdentification> licenseList : ossComponentLicenseList) {
			for (ProjectIdentification licenseBean : licenseList) {
				String _componentId = avoidNull(licenseBean.getComponentId(), licenseBean.getGridId());
				String _licenseName = avoidNull(licenseBean.getLicenseName()).trim();
				
				if (!isEmpty(_licenseName) && !licenseCheckParam.contains(_licenseName)) {
					licenseCheckParam.add(_componentId + "|" + _licenseName);
				}
			}
		}
		
		List<OssMaster> ossNickNameList = null;
		
		if (!ossCheckParam.isEmpty()) {
			OssMaster param = new OssMaster();
			param.setOssNames(ossCheckParam.toArray(new String[ossCheckParam.size()]));
			ossNickNameList = projectMapper.checkOssNickName(param);
			
			if (ossNickNameList != null) {
				for (OssMaster bean : ossNickNameList) {
					String _disp = bean.getOssNickname() + "|" + bean.getOssName();
					if (!ossNickNameCheckResult.contains(_disp)) {
						ossNickNameCheckResult.add(_disp);
					}
				}
			}
		}
		
		if (!licenseCheckParam.isEmpty()) {
			Map<String, Map<String, String>> licenseCheckMap = new HashMap<>();
			
			for (String checkParam : licenseCheckParam) {
				String componentId = checkParam.split("[|]")[0];
				String licenseName = checkParam.split("[|]")[1];
				String ossInfo = ossComponentInfo.get(componentId);
				
				if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
					if (licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
						for (String s : licenseMaster.getLicenseNicknameList()) {
							if (licenseName.equalsIgnoreCase(s)) {
								Map<String, String> checkMap = null;
								if (licenseCheckMap.containsKey(ossInfo)) {
									checkMap = licenseCheckMap.get(ossInfo);
								} else {
									checkMap = new HashMap<>();
								}
								checkMap.put(licenseName, avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp()));
								licenseCheckMap.put(ossInfo, checkMap);
							}
						}
					}
				}
			}
			
			if (!licenseCheckMap.isEmpty()) {
				for (String ossInfo : licenseCheckMap.keySet()) {
					Map<String, String> licenseMap = licenseCheckMap.get(ossInfo);
					String nicknames = "";
					String changeNames = "";
					
					for (String nickname : licenseMap.keySet()) {
						nicknames += nickname + ",";
						changeNames += licenseMap.get(nickname) + ",";
					}
					nicknames = nicknames.substring(0, nicknames.length()-1);
					changeNames = changeNames.substring(0, changeNames.length()-1);
					
					String disp = ossInfo + "|" + nicknames + "|" + changeNames;
					if (!licenseNickNameCheckResult.contains(disp)) {
						licenseNickNameCheckResult.add(disp);
					}
				}
			}
		}
		
		result.put("OSS", ossNickNameCheckResult);
		result.put("LICENSE", licenseNickNameCheckResult);
		
		return result;
	}

	@Override
	public void registBom(String prjId, String merge, List<ProjectIdentification> projectIdentification, List<ProjectIdentification> checkGridBomList) {
		registBom(prjId, merge, projectIdentification, checkGridBomList, null, false, false);
	}
	
	@Override
	public void registBom(String prjId, String merge, List<ProjectIdentification> projectIdentification, List<ProjectIdentification> checkGridBomList, String copyPrjId, boolean isCopyConfirm, boolean isAndroid) {
		registBom(prjId, merge, projectIdentification, checkGridBomList, copyPrjId, isCopyConfirm, isAndroid, false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void registBom(String prjId, String merge, List<ProjectIdentification> projectIdentification, List<ProjectIdentification> checkGridBomList, String copyPrjId, boolean isCopyConfirm, boolean isAndroid, boolean isPartner) {
		Map<String, OssMaster> ossInfoMap = CoCodeManager.OSS_INFO_UPPER;
		List<ProjectIdentification> includeVulnInfoNewBomList = new ArrayList<>();
		List<ProjectIdentification> includeVulnInfoOldBomList = new ArrayList<>();
		List<String> cvssScoreMaxList = new ArrayList<>();
				
		// 컴포넌트 삭제
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(prjId);
		if (!isAndroid) {
			if (!isPartner) {
				identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
			} else {
				identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
			}
		} else {
			identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM);
		}
		identification.setMerge(CoConstDef.FLAG_NO);
		
		// 기존 bom data get
		List<ProjectIdentification> bomList = null;
		if (!isAndroid && !isPartner) {
			bomList = projectMapper.selectBomList(identification);
		} else {
			bomList = projectMapper.selectOtherBomList(identification);
		}
		
		List<String> adminCheckComponentIds = new ArrayList<>();
		List<String> removeAdminCheckComponentIds = new ArrayList<>();
		for (ProjectIdentification bomGridData : checkGridBomList) {
			for (String refComponentId : bomGridData.getRefComponentId().split(",")) {
				removeAdminCheckComponentIds.add(refComponentId.trim());
			}
		}
		
		if (bomList != null && !bomList.isEmpty()) {
			for (ProjectIdentification pi : bomList) {
				if (pi.getAdminCheckYn().equals(CoConstDef.FLAG_YES)) {
					adminCheckComponentIds.add(pi.getRefComponentId());
				}
				
				String key = (pi.getOssName() + "_" + avoidNull(pi.getOssVersion())).toUpperCase();
				boolean setCveInfoFlag = false;
				if (ossInfoMap.containsKey(key)) {
					OssMaster om = ossInfoMap.get(key);
					if (CoConstDef.FLAG_YES.equals(avoidNull(om.getInCpeMatchFlag()))) {
						String cveId = om.getCveId();
						String cvssScore = om.getCvssScore();
						if (!isEmpty(cvssScore) && !isEmpty(cveId)) {
							if (new BigDecimal(cvssScore).compareTo(new BigDecimal("8.0")) > -1) {
								includeVulnInfoOldBomList.add(pi);
							}
						} else {
							setCveInfoFlag = true;
						}
					} else {
						setCveInfoFlag = true;
					}
				}
				
				if (setCveInfoFlag) {
					// convert max score
					if (pi.getCvssScoreMax() != null) {
						cvssScoreMaxList.add(pi.getCvssScoreMax());
					}
					if (pi.getCvssScoreMax1() != null) {
						cvssScoreMaxList.add(pi.getCvssScoreMax1());
					}
					
					String conversionCveInfo = CommonFunction.getConversionCveInfo(pi.getReferenceId(), ossInfoMap, pi, null, cvssScoreMaxList, false);
					if (conversionCveInfo != null) {
						String[] conversionCveInfoSplit = conversionCveInfo.split("\\@");
						if (new BigDecimal(conversionCveInfoSplit[3]).compareTo(new BigDecimal("8.0")) > -1) {
							includeVulnInfoOldBomList.add(pi);
						}
					}
					
					cvssScoreMaxList.clear();
				}
			}
		}
		
		if (!removeAdminCheckComponentIds.isEmpty()) adminCheckComponentIds.removeAll(removeAdminCheckComponentIds);
		List<OssComponents> componentId = projectMapper.selectComponentId(identification);
		
		// 기존 bom 정보를 모두 물리삭제하고 다시 등록한다.
		if (componentId.size() > 0){
			projectMapper.resetOssComponentsAndLicense(identification.getReferenceId(), identification.getReferenceDiv());
		}
		
		identification.setMerge(merge);
		identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		identification.setSaveBomFlag(CoConstDef.FLAG_YES); // file path 를 groupping 하지 않고, 개별로 data 등록
		Map<String, Object> mergeListMap = getIdentificationGridList(identification);
		List<ProjectIdentification> bomComponentsList = new ArrayList<>();
		
		if (mergeListMap != null && mergeListMap.get("rows") != null) {
			if (isCopyConfirm) {
				ProjectIdentification param = new ProjectIdentification();
				param.setReferenceId(copyPrjId);
				param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				param.setMerge(CoConstDef.FLAG_NO);
				Map<String, Object> copyTargetbomList = getIdentificationGridList(param);
				
				if (copyTargetbomList != null && copyTargetbomList.get("rows") != null && projectIdentification.isEmpty()) {
					projectIdentification = (List<ProjectIdentification>) copyTargetbomList.get("rows");
				}
			}
			
			for (ProjectIdentification bean : (List<ProjectIdentification>)mergeListMap.get("rows")) {
				bean.setRefDiv(bean.getReferenceDiv());
				if (!isAndroid) {
					if (!isPartner) {
						bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
					} else {
						bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
					}
				} else {
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM);
				}
				bean.setRefComponentId(bean.getComponentId());
				if (adminCheckComponentIds.contains(bean.getRefComponentId())) {
					bean.setAdminCheckYn(CoConstDef.FLAG_YES);
				} else {
					bean.setAdminCheckYn(CoConstDef.FLAG_NO);
				}
				bean.setPreObligationType(bean.getObligationType());
				
				String copyCheckKey = bean.getRefComponentId();
				if (isCopyConfirm) {
					copyCheckKey = (bean.getRefDiv() + "_" + bean.getOssName() + "_" + bean.getOssVersion() + "_" + bean.getLicenseName()).toUpperCase();
				}
				
				// 그리드 데이터 넣기
				for (ProjectIdentification gridData : projectIdentification) {
					String copyCheckKey2 = gridData.getRefComponentId();
					if (isCopyConfirm) {
						copyCheckKey2 = (gridData.getRefDiv() + "_" + gridData.getOssName() + "_" + gridData.getOssVersion() + "_" + gridData.getLicenseName()).toUpperCase();
					}
					
					// merge 결과 (src/bat/3rd) 일시
					if (copyCheckKey2.contains(copyCheckKey)){
						bean.setMergePreDiv(gridData.getMergePreDiv());
						
						// BOM에 초기표시된 obligation을 초기 값으로 설정
						// needs check의 경우만 화면에서 입력받는다.
						if (CoConstDef.FLAG_YES.equals(gridData.getAdminCheckYn())) {
							bean.setAdminCheckYn(gridData.getAdminCheckYn());
							
							if (isCopyConfirm) {
								bean.setPreObligationType(gridData.getPreObligationType());
								bean.setObligationType(gridData.getObligationType());
							} else {
								if (CoConstDef.FLAG_NO.equals(gridData.getNotify()) && CoConstDef.FLAG_YES.equals(gridData.getSource())) {
									bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY);
								} else if (CoConstDef.FLAG_YES.equals(gridData.getSource())) {
									bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
								} else if (CoConstDef.FLAG_YES.equals(gridData.getNotify())) {
									bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
								} else if (CoConstDef.FLAG_NO.equals(gridData.getNotify()) && CoConstDef.FLAG_NO.equals(gridData.getSource())) {
									bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK_SELECTED);
								}
							}
							
							bean.setDownloadLocation(gridData.getDownloadLocation());
							bean.setHomepage(gridData.getHomepage());
							bean.setCopyrightText(gridData.getCopyrightText());
						}
						
						break;
					}
				}
				
				bean = CommonFunction.findOssIdAndName(bean);
				
				String key = (bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase();
				boolean setCveInfoFlag = false;
				if (ossInfoMap.containsKey(key)) {
					OssMaster om = ossInfoMap.get(key);
					if (CoConstDef.FLAG_YES.equals(avoidNull(om.getInCpeMatchFlag()))) {
						String cveId = om.getCveId();
						String cvssScore = om.getCvssScore();
						if (!isEmpty(cvssScore) && !isEmpty(cveId)) {
							if (new BigDecimal(cvssScore).compareTo(new BigDecimal("8.0")) > -1) {
								includeVulnInfoOldBomList.add(bean);
							}
						} else {
							setCveInfoFlag = true;
						}
					} else {
						setCveInfoFlag = true;
					}
				}
				
				if (setCveInfoFlag) {
					// convert max score
					if (bean.getCvssScoreMax() != null) {
						cvssScoreMaxList.add(bean.getCvssScoreMax());
					}
					if (bean.getCvssScoreMax1() != null) {
						cvssScoreMaxList.add(bean.getCvssScoreMax1());
					}
					
					String conversionCveInfo = CommonFunction.getConversionCveInfo(bean.getReferenceId(), ossInfoMap, bean, null, cvssScoreMaxList, false);
					if (conversionCveInfo != null) {
						String[] conversionCveInfoSplit = conversionCveInfo.split("\\@");
						if (new BigDecimal(conversionCveInfoSplit[3]).compareTo(new BigDecimal("8.0")) > -1) {
							includeVulnInfoNewBomList.add(bean);
						}
					}
					
					cvssScoreMaxList.clear();
				}
				
				if(!isEmpty(bean.getCopyrightText())) {
					String[] copyrights = bean.getCopyrightText().split("\\|");
					String copyrightText  = Arrays.stream(copyrights).distinct().collect(Collectors.joining("\n"));
					bean.setCopyrightText(copyrightText);
				}
				
				// 컴포넌트 마스터 인서트
				// projectMapper.registBomComponents(bean);
				List<OssComponentsLicense> licenseList = CommonFunction.findOssLicenseIdAndName(bean.getOssId(), bean.getOssComponentsLicenseList());
				bean.setOssComponentsLicenseList(licenseList);
				bomComponentsList.add(bean);
				
//				for (OssComponentsLicense licenseBean : licenseList) {
//					licenseBean.setComponentId(bean.getComponentId());
//					projectMapper.registComponentLicense(licenseBean);
//				}
			}
		}
		
		if (!CollectionUtils.isEmpty(bomComponentsList)) {
			registBomComponents(bomComponentsList);
		}
		
		if (!isPartner) {
			// identification 대상이 없이 처음 저장하는 경우
			final Project _tempPrjInfo = projectMapper.selectProjectMaster2(prjId);
			
			if (isEmpty(_tempPrjInfo.getIdentificationStatus())) {
				_tempPrjInfo.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
				
				projectMapper.updateIdentifcationProgress(_tempPrjInfo);
			}
		}
		
		// add or delete data containing vulnerability information among oss information
		String securityComment = "";
		List<ProjectIdentification> duplicatedNewVulnInfoList = null;
		List<ProjectIdentification> duplicatedOldVulnInfoList = null;
		
		if (!includeVulnInfoNewBomList.isEmpty()) {
			duplicatedNewVulnInfoList = includeVulnInfoNewBomList.stream().filter(CommonFunction.distinctByKey(p -> p.getOssName()+p.getOssVersion())).collect(Collectors.toList());
		}
		if (!includeVulnInfoOldBomList.isEmpty()) {
			duplicatedOldVulnInfoList = includeVulnInfoOldBomList.stream().filter(CommonFunction.distinctByKey(p -> p.getOssName()+p.getOssVersion())).collect(Collectors.toList());
		}
		if (duplicatedNewVulnInfoList != null && duplicatedOldVulnInfoList != null) {
			List<ProjectIdentification> filteredAddVulnDataList = includeVulnInfoNewBomList
					.stream()
					.filter(bfList-> 
					includeVulnInfoOldBomList
									.stream()
									.filter(afList -> 
											(bfList.getOssName() + "||" + bfList.getOssVersion()).equalsIgnoreCase(afList.getOssName() + "||" + afList.getOssVersion())
											).collect(Collectors.toList()).size() == 0
							).collect(Collectors.toList());
							
			List<ProjectIdentification> filteredDelVulnDataList = includeVulnInfoOldBomList
					.stream()
					.filter(bfList-> 
					includeVulnInfoNewBomList
									.stream()
									.filter(afList -> 
											(bfList.getOssName() + "||" + bfList.getOssVersion()).equalsIgnoreCase(afList.getOssName() + "||" + afList.getOssVersion())
											).collect(Collectors.toList()).size() == 0
							).collect(Collectors.toList());
			
			if (filteredAddVulnDataList != null && !filteredAddVulnDataList.isEmpty()) {
				securityComment += "<p><strong>Added vulnerabilities from Identification</strong>";
				for (ProjectIdentification pi : filteredAddVulnDataList) {
					securityComment += "<br />" + pi.getOssName() + " (" + avoidNull(pi.getOssVersion(), "N/A") + ")";
				}
			}
			
			if (filteredDelVulnDataList != null && !filteredDelVulnDataList.isEmpty()) {
				if (!securityComment.isEmpty()) securityComment += "<br /><br />";
				securityComment += "<p><strong>Deleted vulnerabilities from Identification</strong>";
				for (ProjectIdentification pi : filteredDelVulnDataList) {
					securityComment += "<br />" + pi.getOssName() + " (" + avoidNull(pi.getOssVersion(), "N/A") + ")";
				}				
			}
		} else if (duplicatedNewVulnInfoList != null) {
			securityComment += "<p><strong>Added vulnerabilities from Identification</strong>";
			for (ProjectIdentification pi : duplicatedNewVulnInfoList) {
				securityComment += "<br />" + pi.getOssName() + " (" + avoidNull(pi.getOssVersion(), "N/A") + ")";
			}
		} else if (duplicatedOldVulnInfoList != null) {
			securityComment += "<p><strong>Deleted vulnerabilities from Identification</strong>";
			for (ProjectIdentification pi : duplicatedOldVulnInfoList) {
				securityComment += "<br />" + pi.getOssName() + " (" + avoidNull(pi.getOssVersion(), "N/A") + ")";
			}
		}
		
		if (!isEmpty(securityComment)) {
			securityComment += "</p>";
			
			CommentsHistory commHisBean = new CommentsHistory();
			commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_SECURITY_HIS);
			commHisBean.setReferenceId(prjId);
			commHisBean.setContents(securityComment);
			
			commentService.registComment(commHisBean, false);
		}
	}
	
	private void registBomComponents(List<ProjectIdentification> bomComponentsList) {
		List<OssComponentsLicense> bomComponentsLicenseList = new ArrayList<>();
		
		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            ProjectMapper mapper = sqlSession.getMapper(ProjectMapper.class);
            int saveCnt = 0;
            for (ProjectIdentification bean : bomComponentsList) {
                mapper.registBomComponents(bean);
                if(saveCnt++ == 1000) {
                    sqlSession.flushStatements();
                    saveCnt = 0;
                }
            }
            
            if (saveCnt > 0) {
                sqlSession.flushStatements();
            }
            
            bomComponentsLicenseList.addAll(getInsertOssComponentLicenseList(bomComponentsList));
            
            saveCnt = 0;
            for (OssComponentsLicense bean : bomComponentsLicenseList) {
                mapper.registComponentLicense(bean);
                if (saveCnt++ == 1000) {
                    sqlSession.flushStatements();
                    saveCnt = 0;
                }
            }
            if (saveCnt > 0) {
                sqlSession.flushStatements();
            }
            sqlSession.commit();
        }
		
		bomComponentsList.clear();
		bomComponentsLicenseList.clear();
	}

	@Override
	public void checkProjectReviewer(Project project) {
		Project param = projectMapper.selectProjectMaster2(project.getPrjId());
		
		//review 상태로 변경시 reviewer가 설정되어 있지 않은 경우, reviewer도 업데이트 한다.
		if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getIdentificationStatus())) {
			if (isEmpty(param.getReviewer())) {
				param.setModifier(param.getLoginUserName());
				param.setReviewer(param.getLoginUserName());
				
				projectMapper.updateReviewer(param);
				
				try {
					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_ADD);
					mailBean.setToIds(new String[] {param.getLoginUserName()});
					mailBean.setParamPrjId(project.getPrjId());
					
					CoMailManager.getInstance().sendMail(mailBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public Map<String, Object> updateProjectStatus(Project project, boolean isCopyConfirm, boolean isVerificationConfirm) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		
		String commentDiv = isEmpty(project.getReferenceDiv()) ? CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS
				: project.getReferenceDiv();
		
		String userComment = project.getUserComment();
		String statusCode = project.getIdentificationStatus();
		
		if (isEmpty(statusCode)) {
			statusCode = project.getVerificationStatus();
		}
		
		String status = CoCodeManager.getCodeExpString(CoConstDef.CD_IDENTIFICATION_STATUS, statusCode);
		String mailType = null;
		log.info("statusCode : " + statusCode + "/  status : " + status);
		
		log.debug("PARAM: " + "identificationStatus="+project.getIdentificationStatus());
		log.debug("PARAM: " + "completeYn="+project.getCompleteYn());
		
		// Identification confirm시 validation check 수행
		if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(project.getIdentificationStatus())) {
			boolean isAndroidModel = false;
			boolean isNetworkRestriction = false;
			boolean hasSourceOss = false;
			boolean hasNotificationOss = false; 
			
			Map<String, Object> map = null;
			
			Project prjParam = new Project();
			prjParam.setPrjId(project.getPrjId());
			Project prjInfo = getProjectDetail(prjParam);
			
			// confirm 시 다시 DB Data를 가져와서 체크한다.
			ProjectIdentification param = new ProjectIdentification();
			param.setReferenceId(project.getPrjId());
			
			if (CoConstDef.FLAG_YES.equals(prjInfo.getAndroidFlag())
					&& !CoConstDef.FLAG_NO.equals(prjInfo.getIdentificationSubStatusAndroid())
					&& !CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA.equals(prjInfo.getIdentificationSubStatusAndroid())) {
				project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM);
				param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
				map = getIdentificationGridList(param);

				if (map != null && map.containsKey("mainData")
						&& !((List<ProjectIdentification>) map.get("mainData")).isEmpty()) {
					isAndroidModel = true;
					T2CoProjectValidator pv = new T2CoProjectValidator();
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);

					pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
					pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
					T2CoValidationResult vr = pv.validate(new HashMap<>());
					
					// return validator result
					if (!vr.isValid()) {
//						return makeJsonResponseHeader(false, getMessage("msg.project.android.valid"));
						resultMap.put("androidMessage", getMessage("msg.project.android.valid"));
						return resultMap;
					}
				}
			} else {
				project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				param.setMerge(CoConstDef.FLAG_NO);
				map = getIdentificationGridList(param);
				
				if (map != null && map.containsKey("rows") && !((List<ProjectIdentification>) map.get("rows")).isEmpty()) {
					T2CoProjectValidator pv = new T2CoProjectValidator();
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BOM_MERGE);

					pv.setAppendix("bomList", (List<ProjectIdentification>) map.get("rows"));

					T2CoValidationResult vr = pv.validate(new HashMap<>());
					
					// return validator result
					if (!vr.isValid() && !vr.isAdminCheck((List<String>) map.get("adminCheckList"))) {
//						return makeJsonResponseHeader(vr.getValidMessageMap());
						resultMap.put("validMap", vr.getValidMessageMap());
						return resultMap;
					}
					
					String networkRedistribution = CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, CoConstDef.CD_LICENSE_NETWORK_RESTRICTION);
					
					for (ProjectIdentification _projectBean : (List<ProjectIdentification>) map.get("rows")) {
						if (hasSourceOss && hasNotificationOss && isNetworkRestriction) {
							break;
						}
						
						if (!hasNotificationOss) {
							if (!CoConstDef.FLAG_YES.equals(_projectBean.getExcludeYn()) && ("10".equals(_projectBean.getObligationType()) || "11".equals(_projectBean.getObligationType()) || "12".equals(_projectBean.getObligationType()) )) {
								hasNotificationOss = true;
							}
						}
						
						if (!hasSourceOss) {
							if ("11".equals(_projectBean.getObligationType()) || "12".equals(_projectBean.getObligationType())){
								hasSourceOss = true;
							}
						}
						
						if (!isNetworkRestriction) {
							if (("10".equals(_projectBean.getObligationType()) || "11".equals(_projectBean.getObligationType()) || "12".equals(_projectBean.getObligationType())) && _projectBean.getRestriction().toUpperCase().contains(networkRedistribution.toUpperCase())) {
								isNetworkRestriction = true;
							}
						}
					}
				}
			}
			
			if (CoConstDef.FLAG_YES.equals(prjInfo.getNetworkServerType())) {
				if (!isNetworkRestriction) {
					project.setSkipPackageFlag(CoConstDef.FLAG_YES);
					project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
					project.setDistributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				}
			} else {
				if (isAndroidModel) {
					project.setAndroidFlag(CoConstDef.FLAG_YES);
				} else if (!hasNotificationOss) {
					// Android model이 아니면서 bom 대상이 없는 경우
					// package, distribute를 N/A 처리한다.
					project.setSkipPackageFlag(CoConstDef.FLAG_YES);
					project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
					project.setDistributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				}
			}
			
			if (CoConstDef.CD_NOTICE_TYPE_NA.equals(prjInfo.getNoticeType())) {
				if (!hasSourceOss) {
					project.setSkipPackageFlag(CoConstDef.FLAG_YES);
					project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
					project.setDistributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				}
			}
			
			project.setModifier(project.getLoginUserName());
			updateProjectIdentificationConfirm(project, isCopyConfirm, isVerificationConfirm);
			
			// network server 이면서 notice 생성 대상이 없을 경우
			if ( hasNotificationOss
					&& CoConstDef.FLAG_NO.equals(avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE,
							prjInfo.getDistributionType())).trim().toUpperCase())
					&& verificationService.checkNetworkServer(prjInfo.getPrjId()) ) {
				project.setSkipPackageFlag(CoConstDef.FLAG_YES);
				project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
				project.setDistributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				updateIdentificationConfirmSkipPackaing(project);
				
				hasNotificationOss = false;
			}

			// permissive로만 이루어져있고, notice type이 기본인 경우, 바로 packaging review상태로
			// 변경한다.
			if ((!isAndroidModel && !hasNotificationOss) 
					|| (CoConstDef.FLAG_YES.equals(prjInfo.getNetworkServerType()) && !isNetworkRestriction) // Network service Only : yes 이지만 network restriction이 없는 case 
					|| (CoConstDef.CD_NOTICE_TYPE_NA.equals(prjInfo.getNoticeType()) && !hasSourceOss)) { // OSS Notice가 N/A이면서 packaging이 필요 없는 경우
				// do nothing
				mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY;
				if (CoConstDef.FLAG_YES.equals(prjInfo.getNetworkServerType()) && !isNetworkRestriction) {
					if (!isEmpty(avoidNull(userComment))) {
						userComment = avoidNull(userComment) + avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_NETWORK_SERVICE_ONLY));
					} else {
						userComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_NETWORK_SERVICE_ONLY));
					}
				} else {
					if (!isEmpty(avoidNull(userComment))) {
						userComment = avoidNull(userComment) + avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY));
					} else {
						userComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY));
					}
				}
			} else {
				String _tempComment;
				if(isAndroidModel) {
					mailType = CoConstDef.CD_MAIL_TYPE_BIN_PROJECT_IDENTIFICATION_CONF;
					_tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_BIN_PROJECT_IDENTIFICATION_CONF));
				} else {
					mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF;
					_tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF));
				}
				userComment = avoidNull(userComment) + "<br />" + _tempComment;
			}
			verificationService.getReviewReportPdfFile(prjInfo.getPrjId());
		} else if (!isEmpty(project.getCompleteYn())) {
			// project complete 시
			updateProjectMaster(project);
			
			String _tempComment = "";
			
			if (CoConstDef.FLAG_YES.equals(project.getCompleteYn())) {
				_tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_COMPLETED));
				userComment =  avoidNull(userComment) + "<br />" + _tempComment;
			}
			
			// complete log 추가
			commentDiv = CoConstDef.CD_DTL_COMMENT_PROJECT_HIS;
			status = CoConstDef.FLAG_YES.equals(project.getCompleteYn()) ? "Completed" : "Reopened";
			
			// complete mail 발송
			mailType = CoConstDef.FLAG_YES.equals(project.getCompleteYn()) ? CoConstDef.CD_MAIL_TYPE_PROJECT_COMPLETED : CoConstDef.CD_MAIL_TYPE_PROJECT_REOPENED;
		} else if (!isEmpty(project.getDropYn())){
			// project drop 시
			updateProjectMaster(project);
			
			String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_DROPPED));
				userComment = avoidNull(userComment) + "<br />" + _tempComment;
			
			// complete log 추가
			commentDiv = CoConstDef.CD_DTL_COMMENT_PROJECT_HIS;
			status = "Dropped";
			
			// complete mail 발송
			mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_DROPPED;
		} else {
			boolean ignoreValidation = CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getIdentificationStatus()) 
					|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(project.getVerificationStatus()) 
					|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getVerificationStatus());
			boolean isIdentificationReject = false;
			Project beforeInfo = getProjectDetail(project);
			
			// Identification
			// default -> request
			if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(project.getIdentificationStatus())
					&& !CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(beforeInfo.getIdentificationStatus())) {
				mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REQ_REVIEW;
				
				// Admin 사용자의 경우 오류가 있어도 request review 가능하도록 수정
				if (CommonFunction.isAdmin()) {
					ignoreValidation = true;
				}
			} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS.equals(project.getIdentificationStatus())) {
				ignoreValidation = true;
				isIdentificationReject = true;
				
				if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(beforeInfo.getIdentificationStatus())) {
					// self reject
					mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_SELF_REJECT;
				} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW
						.equals(beforeInfo.getIdentificationStatus())) {
					// reject by review
					mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REJECT;
				} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM
						.equals(beforeInfo.getIdentificationStatus())) {
					// confirm to review
					mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CANCELED_CONF;
				}
			} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(project.getVerificationStatus()) // Packaging
					&& !CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(beforeInfo.getVerificationStatus())) {
				mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REQ_REVIEW;
				//ignoreValidation = true;
			} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS.equals(project.getVerificationStatus())) {
				ignoreValidation = true;
				
				if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(beforeInfo.getVerificationStatus())) {
					// self reject
					mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_SELF_REJECT;
				} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(beforeInfo.getVerificationStatus())) {
					// review -> reject
					mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REJECT;
				} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(beforeInfo.getVerificationStatus())) {
					mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CANCELED_CONF;
				}
			}
			
			// 사용자가 reject하는 경우는 validation check 수행하지 않음
			if (!ignoreValidation) {
				// Identification Reqeust review인 경우, 필수 항목 체크 추가
				Map<String, Object> map = null;
				// confirm 시 다시 DB Data를 가져와서 체크한다.
				ProjectIdentification param = new ProjectIdentification();
				param.setReferenceId(project.getPrjId());
				param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				param.setMerge(CoConstDef.FLAG_NO);
				map = getIdentificationGridList(param);
				
				if (map != null && map.containsKey("rows") && !((List<ProjectIdentification>) map.get("rows")).isEmpty()) {
					T2CoProjectValidator pv = new T2CoProjectValidator();
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BOM_MERGE);
					pv.setValidLevel(pv.VALID_LEVEL_REQUEST);
					pv.setAppendix("bomList", (List<ProjectIdentification>) map.get("rows"));

					T2CoValidationResult vr = pv.validate(new HashMap<>());
					// return validator result
					if (!vr.isValid()) {
						if (!vr.isDiff()) {
							resultMap.put("diffMap", vr.getDiffMessageMap(true));
						}
						resultMap.put("validMap", vr.getValidMessageMap());
						return resultMap;
					}
				}
				
				Project prjInfo = null;
				
				{
					// ANDROID PROJECT인 경우
					Project prjParam = new Project();
					prjParam.setPrjId(project.getPrjId());
					prjInfo = getProjectDetail(prjParam);
					
					if (CoConstDef.FLAG_YES.equals(prjInfo.getAndroidFlag())
							&& !CoConstDef.FLAG_NO.equals(prjInfo.getIdentificationSubStatusAndroid())
							&& !CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA.equals(prjInfo.getIdentificationSubStatusAndroid())) {
						param = new ProjectIdentification();
						param.setReferenceId(project.getPrjId());
						param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
						map = getIdentificationGridList(param);

						if (map != null && map.containsKey("mainData")
								&& !((List<ProjectIdentification>) map.get("mainData")).isEmpty()) {
							T2CoProjectValidator pv = new T2CoProjectValidator();
							pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);

							pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
							pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
							T2CoValidationResult vr = pv.validate(new HashMap<>());
							
							// return validator result
							if (!vr.isValid()) {
//								return makeJsonResponseHeader(false, getMessage("msg.project.android.valid"));
								resultMap.put("androidMessage", getMessage("msg.project.android.valid"));
								return resultMap;
							}
						}
					}
				}			
			}
			
			project.setModifier(project.getLoginUserName());
			project.setModifiedDate(project.getCreatedDate());
			
			if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getIdentificationStatus())) {
				checkProjectReviewer(project);
			}
			
			projectMapper.updateProjectMaster(project);
			
			if (!isEmpty(project.getVerificationStatus())) {
				OssNotice ossNotice = null;
				if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getVerificationStatus())) {
					ossNotice = verificationService.selectOssNoticeOne(project.getPrjId());
					if (ossNotice != null) {
						ossNotice.setEditNoticeYn(project.getEditNoticeYn());
						if (CoConstDef.FLAG_YES.equals(avoidNull(project.getWithoutVerifyYn()))) {
							ossNotice.setWithoutVerifyYn(CoConstDef.FLAG_YES);
						}
						verificationService.registOssNotice(ossNotice);
					}
				} else if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS.equals(project.getVerificationStatus())) {
					ossNotice = new OssNotice();
					ossNotice.setPrjId(project.getPrjId());
					ossNotice.setWithoutVerifyYn(null);
					projectMapper.updateWithoutVerifyYn(ossNotice);
				}
				
				if (project.getNoticeFileFormat() != null) {
					List<String> noticeFileFormatList = Arrays.asList(project.getNoticeFileFormat());
					setNoticeFileFormat(project, noticeFileFormatList);
				}
				
				verificationService.updateProjectAllowDownloadBitFlag(project);
			}
		}
		
		resultMap.put("mailType", mailType);
		resultMap.put("userComment", userComment);
		resultMap.put("commentDiv", commentDiv);
		resultMap.put("status", status);
		
		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void updateProjectIdentificationConfirm(Project project, boolean isCopyConfirm, boolean isVerificationConfirm) {
		Map<String, Object> map = null;
		ProjectIdentification param = new ProjectIdentification();
		param.setReferenceId(project.getPrjId());
		param.setReferenceDiv(project.getReferenceDiv());
		param.setMerge(CoConstDef.FLAG_NO);
		map = getIdentificationGridList(param);
		if (map != null && map.containsKey("rows") && !((List<ProjectIdentification>) map.get("rows")).isEmpty()) {
			((List<ProjectIdentification>) map.get("rows")).forEach(bean -> {
				String ossCopyright = findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright());
				OssMaster oss = CoCodeManager.OSS_INFO_BY_ID.get(bean.getOssId());
				if(oss != null) {
					bean.setCopyrightText(avoidNull(oss.getCopyright()));
				}
				if (!isEmpty(ossCopyright)) {
					String addCopyright = avoidNull(bean.getCopyrightText());
					if (!isEmpty(bean.getCopyrightText())) {
						addCopyright += "\n";
					}
					addCopyright += ossCopyright;
					bean.setCopyrightText(addCopyright);
				}
				projectMapper.updateComponentsCopyrightInfo(bean);
			});
		}

		// oss id 등록
		projectMapper.updateComponentsOssId(project);
		// downlaod location, homepage등 master 정보로 치환
		projectMapper.updateComponentsOssInfo(project);
		// license id 등록
		projectMapper.updateComponentsLicenseId(project);
		// license id 등록 이후에 license text, copyright 정보를 confirm 시점에 oss master에 등록되어 있는 기준 정보로 업데이트 한다.
		projectMapper.updateComponentsLicenseInfo(project);
		// 상태 변경 (packaging, distribute 초기와 작업이 있기 때문에 공통 service를 사용하지 않고 confirm인 경우만 별도로 추가)
		projectMapper.updateIdentificationConfirm(project);
		
		// Identification confirm시 OSS가 등록되지 않은 Tab에 대해서는 N/A 처리
		projectMapper.updateProjectStatusWithComplete(project);
		
		// packaging 이루 부터는 reference_div = "50" 만 참조할 수 있도록 confirm 단계에서 대상 notice 대상 data를 모두 50번으로 copy한다.
		// 기존에 등록된  data가 있을 수 있기 때문에 삭제 후 등록
		{
			// 기존에 등록되어 있는 data를 삭제하기 전에 path정보를 다시 매핑해주기 위해서 verify 대상 정보를 취득한다.
			List<OssComponents> oldPackagingList = verificationService.getVerifyOssList(project);
			Map<String, OssComponents> oldPackageInfoMap = new HashMap<>();
			
			if (oldPackagingList != null && !oldPackagingList.isEmpty()) {
				// key value 형식으로
				// key = ref + oss name + oss version + license name
				for (OssComponents oldBean : oldPackagingList) {
					if (!isEmpty(oldBean.getFilePath())) {
						String key = oldBean.getReferenceDiv() + "|" + oldBean.getOssId() + "|" + oldBean.getLicenseName();
						
						oldPackageInfoMap.put(key, oldBean);
					}
				}
			}
			
			// 1) packaging components delete 처리
//			ProjectIdentification delParam = new ProjectIdentification();
//			delParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PACKAGING);
//			delParam.setReferenceId(project.getPrjId());
//			List<OssComponents> componentId = projectMapper.selectComponentId(delParam);
//			
//			for (int i = 0; i < componentId.size(); i++) {
//				projectMapper.deleteOssComponentsLicense(componentId.get(i));
//			}
//			
//			projectMapper.deleteOssComponents(delParam);
			projectMapper.resetOssComponentsAndLicense(project.getPrjId(), CoConstDef.CD_DTL_COMPONENT_PACKAGING);
			
			// 2) get bom list
			ProjectIdentification bomParam = new ProjectIdentification();
			bomParam.setReferenceId(project.getPrjId());
			bomParam.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
			bomParam.setMerge(CoConstDef.FLAG_NO);
			bomParam.setNoticeFlag(CoConstDef.FLAG_YES); // notice 대상만 추출한다. (obligation type이 10, 11)
			bomParam.setSaveBomFlag(CoConstDef.FLAG_YES);
			bomParam.setBomWithAndroidFlag(project.getAndroidFlag()); // android Project
			
			List<ProjectIdentification> bomList = projectMapper.selectBomList(bomParam);
			Comparator<ProjectIdentification> compare = Comparator
					.comparing(ProjectIdentification::getLicenseTypeIdx)
					.thenComparing(ProjectIdentification::getOssName, Comparator.nullsFirst(Comparator.naturalOrder()))
					.thenComparing(ProjectIdentification::getOssVersion, (str1, str2) -> str2.compareTo(str1))
					.thenComparing(ProjectIdentification::getDownloadLocation, Comparator.reverseOrder())
					.thenComparing(ProjectIdentification::getLicenseName, Comparator.nullsFirst(Comparator.naturalOrder()))
					.thenComparing(ProjectIdentification::getHomepage, Comparator.naturalOrder())
					.thenComparing(ProjectIdentification::getMergeOrder);
			bomList.sort(compare);
			
			// 일괄 등록을 위해 대상 data의 component id 만 추출한다.
			List<String> groupingList = new ArrayList<>(); // 불필요한 row (중복) 는 미등록
			List<String> componentList = new ArrayList<>();
			
			if (bomList != null) {
				for (ProjectIdentification bean : bomList) {
					if (groupingList.contains(bean.getGroupingColumn())) {
						continue;
					}
					
					if (CoConstDef.FLAG_YES.equals(bean.getAdminCheckYn()) 
							&& (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType()) 
									|| CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType()) 
									|| CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY.equals(bean.getObligationType()))) {
						componentList.add(bean.getComponentId()+"-"+bean.getAdminCheckYn());
					}else {
						componentList.add(bean.getComponentId());
					}
				}
			}
			
			// 안드로이드 모델인 경우 없을 수도 있음
			if (!componentList.isEmpty()) {
				for (String refComponentId : componentList) {
					OssComponents copyParam = new OssComponents();
					copyParam.setReferenceId(project.getPrjId());
					copyParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PACKAGING);
					copyParam.setExcludeYn(CoConstDef.FLAG_NO);
					copyParam.setAndroidFlag(project.getAndroidFlag());
					
					if (refComponentId.contains("-")) {
						String[] ids = refComponentId.split("-");
						copyParam.setRefComponentId(ids[0]);
						copyParam.setAdminCheckYn(ids[1]);
					}else {
						copyParam.setRefComponentId(refComponentId);
					}
					
					projectMapper.insertOssComponentsCopy(copyParam);
					projectMapper.insertOssComponentsLicenseCopy(copyParam);
				}
			}
			
			if (oldPackageInfoMap != null && !oldPackageInfoMap.isEmpty()) {
				List<OssComponents> afterPackagingList = verificationService.getVerifyOssList(project);
				
				if (afterPackagingList != null && !afterPackagingList.isEmpty()) {
					// key value 형식으로
					// key = ref + oss name + oss version + license name
					for (OssComponents newBean : afterPackagingList) {
						String key = newBean.getReferenceDiv() + "|" + newBean.getOssId() + "|" + newBean.getLicenseName();
						
						if (oldPackageInfoMap.containsKey(key)) {
							newBean.setFilePath(oldPackageInfoMap.get(key).getFilePath());
							projectMapper.updateFilePath(newBean);
						}
					}
				}
			}
			
			if (isCopyConfirm) {
				Project paramBean = new Project();
				paramBean.setPrjId(project.getCopyPrjId());
				
				Map<String, OssComponents> copiedPackageInfoMap = new HashMap<>();
				List<OssComponents> copiedPackagingList = verificationService.getVerifyOssList(paramBean);
				
				if (copiedPackagingList != null && !copiedPackagingList.isEmpty()) {
					for (OssComponents copyBean : copiedPackagingList) {
						if (!isEmpty(copyBean.getFilePath())) {
							String key = copyBean.getReferenceDiv() + "|" + copyBean.getOssId() + "|" + copyBean.getLicenseName();
							
							copiedPackageInfoMap.put(key, copyBean);
						}
					}
				}
				
				List<OssComponents> afterPackagingList = verificationService.getVerifyOssList(project);
				
				if (afterPackagingList != null && !afterPackagingList.isEmpty()) {
					for (OssComponents newBean : afterPackagingList) {
						String key = newBean.getReferenceDiv() + "|" + newBean.getOssId() + "|" + newBean.getLicenseName();
						
						if (copiedPackageInfoMap.containsKey(key)) {
							newBean.setFilePath(copiedPackageInfoMap.get(key).getFilePath());
							if (isVerificationConfirm) {
								newBean.setVerifyFileCount(copiedPackageInfoMap.get(key).getVerifyFileCount());
								projectMapper.updateFilePathWithFileCount(newBean);
							} else {
								projectMapper.updateFilePath(newBean);
							}
						}
					}
				}
			}
			
			// StatisticsMostUsed > OssInfo INSERT
			projectMapper.insertStatisticsMostUsedOssInfo(project);
			
			// StatisticsMostUsed > LicenseInfo INSERT
			projectMapper.insertStatisticsMostUsedLicenseInfo(project);
		}
	}


	@Override
	public void updateIdentificationConfirmSkipPackaing(Project project) {
		projectMapper.updateIdentificationConfirm(project);
	}

	@Override
	public void updateProjectMaster(Project project) {
		projectMapper.updateProjectMaster(project);
		
		if (CoConstDef.FLAG_YES.equals(project.getCompleteYn())) {
			projectMapper.updateProjectStatusWithComplete(project);
		}
	}

	@Override
	public List<Project> getModelListExcel(Project project) {
		String prjId = project.getPrjId();
		List<Project> modelList = projectMapper.selectModelList(prjId);
		
		return modelList;
	}

	@Transactional
	@Override
	public void registReadmeContent(Project project) {
		projectMapper.updateReadmeContent(project);
	}

	@Override
	public Map<String, Object> getPartnerList(PartnerMaster partnerMaster) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		int records = 0;
		List<PartnerMaster> list = new ArrayList<PartnerMaster>();
		partnerMaster.setStatus("CONF");
		
		if ("ROLE_USER".equals(partnerMaster.getLoginUserRole())){
			records = partnerMapper.selectPartnerMasterTotalCountUser(partnerMaster);
			partnerMaster.setTotListSize(records);
			list = partnerMapper.selectPartnerListUser(partnerMaster);
		}else{
			records = partnerMapper.selectPartnerMasterTotalCount(partnerMaster);
			partnerMaster.setTotListSize(records);
			list = partnerMapper.selectPartnerList(partnerMaster);
		}
		
		map.put("page", partnerMaster.getCurPage());
		map.put("total", partnerMaster.getTotBlockSize());
		map.put("records", records);
		map.put("rows", list);
		
		return map; 
	}
	
	@Override
	public Map<String, Object> getIdentificationProject(Project project) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Project> list = null;

		project.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
		int records = projectMapper.selectProjectTotalCount(project);
		project.setTotListSize(records);
		
		String ossId = project.getOssId();

		if (!StringUtil.isEmpty(ossId)) {
			list = projectMapper.selectUnlimitedOssComponentBomList(project);
		} else {
			list = projectMapper.selectProjectList(project);
		}
		
		if (list != null) {
			for (Project bean : list) {
				// distribution type code 변환
				if (!isEmpty(bean.getDistributionType())) {
					bean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, bean.getDistributionType()));
				}
			}
		}

		map.put("page", project.getCurPage());
		map.put("total", project.getTotBlockSize());
		map.put("records", records);
		map.put("rows", list);
	
		return map;
	}

	@Override
	@Transactional
	public void registComponentsBat(String prjId, String identificationSubStatusBat, List<ProjectIdentification> ossComponents,
			List<List<ProjectIdentification>> ossComponentsLicense, boolean prjYn) {
		// 컴포넌트 마스터 라이센스 지우기
		ProjectIdentification deleteparam = new ProjectIdentification();
		deleteparam.setReferenceId(prjId);
		deleteparam.setReferenceDiv(prjYn?CoConstDef.CD_DTL_COMPONENT_ID_BAT:CoConstDef.CD_DTL_COMPONENT_PARTNER_BAT); // 3rd 추가
		List<OssComponents> componentsId = projectMapper.selectComponentId(deleteparam);
		
		for (int j = 0; j < componentsId.size(); j++){
			projectMapper.deleteOssComponentsLicense(componentsId.get(j));
		}
		
		// // 한건도 없을시 프로젝트 마스터 BAT 사용가능여부가 N이면 N 그외 null
		if (ossComponents.size()==0 && prjYn){
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(prjId);
			
			if (!StringUtil.isEmpty(identificationSubStatusBat)){
				projectSubStatus.setIdentificationSubStatusBat(identificationSubStatusBat);
			}else{
				projectSubStatus.setIdentificationSubStatusBat("X");
			}			
			
			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			projectSubStatus.setModifiedDate(projectSubStatus.getCreatedDate());
			
			projectMapper.updateProjectMaster(projectSubStatus);
		}
		
		//deleteRows
		List<String> deleteRows = new ArrayList<String>();

		Project prjParam = new Project();
		prjParam.setReferenceDiv(prjYn?"12":"20");
		prjParam.setReferenceId(prjId);
		int ossComponentIdx = projectMapper.selectOssComponentMaxIdx(prjParam);
		
		// 컴포넌트 등록
		for (int i = 0; i < ossComponents.size(); i++) {
			ProjectIdentification ossBean = ossComponents.get(i);
			
			// oss_id를 다시 찾는다. (oss name과 oss id가 일치하지 않는 경우가 있을 수 있음)
			ossBean = CommonFunction.findOssIdAndName(ossBean);
			if (isEmpty(ossBean.getOssId())) {
				ossBean.setOssId(null);
			}
			
			// BAT STATUS 등록
			if (i==0 && prjYn){
				Project projectStatus = projectMapper.selectProjectMaster(prjId);
				
				// 최초 상태이면 PROG
				if (StringUtil.isEmpty(projectStatus.getIdentificationStatus())) {
					projectStatus.setIdentificationStatus("PROG");
				}
				
				// 프로젝트 마스터 BAT 사용가능여부가 N 이면 N 그외 Y
				if (!StringUtil.isEmpty(identificationSubStatusBat)) {
					projectStatus.setIdentificationSubStatusBat(identificationSubStatusBat);
				} else {
					projectStatus.setIdentificationSubStatusBat(CoConstDef.FLAG_YES);
				}
				
				projectStatus.setModifier(projectStatus.getLoginUserName());
				projectStatus.setModifiedDate(projectStatus.getCreatedDate());
				
				projectMapper.updateProjectMaster(projectStatus);
			}
			
			//update
			if (!StringUtil.contains(ossBean.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
				//ossComponents 등록
				projectMapper.updateSrcOssList(ossBean);
				deleteRows.add(ossBean.getComponentId());
				
				//멀티라이센스일 경우
				if ("M".equals(ossBean.getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentsLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if (ossBean.getComponentId().equals(comLicense.getComponentId())){
								OssComponentsLicense license = new OssComponentsLicense();
								// 컴포넌트 ID 설정
								license.setComponentId(comLicense.getComponentId());
								
								// 라이센스 ID 설정
								if (StringUtil.isEmpty(comLicense.getLicenseId())) {
									license.setLicenseId(CommonFunction.getLicenseIdByName(comLicense.getLicenseName()));
								} else {
									license.setLicenseId(comLicense.getLicenseId());
								}
								
								// 기타 설정
								license.setLicenseName(comLicense.getLicenseName());
								license.setLicenseText(comLicense.getLicenseText());
								license.setCopyrightText(comLicense.getCopyrightText());
								license.setExcludeYn(avoidNull(comLicense.getExcludeYn(), CoConstDef.FLAG_NO));
								
								// 라이센스 등록
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else{ // 싱글라이센스일경우
					OssComponentsLicense license = new OssComponentsLicense();
					// 컴포넌트 ID 설정
					license.setComponentId(ossBean.getComponentId());
					
					// 라이센스 ID 설정
					if (StringUtil.isEmpty(ossBean.getLicenseId())) {
						license.setLicenseId(CommonFunction.getLicenseIdByName(ossBean.getLicenseName()));
					} else {
						license.setLicenseId(ossBean.getLicenseId());
					}
					
					// 기타 설정
					license.setLicenseName(ossBean.getLicenseName());
					license.setLicenseText(ossBean.getLicenseText());
					license.setCopyrightText(ossBean.getCopyrightText());
					license.setExcludeYn(CoConstDef.FLAG_NO);
					
					// 라이센스 등록
					projectMapper.registComponentLicense(license);
				}
			} else { //insert
				//ossComponents 등록
				String exComponentId = ossBean.getGridId();
				ossBean.setReferenceId(prjId);
				ossBean.setReferenceDiv(prjYn?"12":"20"); // 3rd 추가
				ossBean.setComponentIdx(Integer.toString(ossComponentIdx++));
				projectMapper.insertSrcOssList(ossBean);
				deleteRows.add(ossBean.getComponentId());
				
				//멀티라이센스일 경우
				if ("M".equals(ossBean.getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentsLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if (exComponentId.equals(comLicense.getComponentId())){
								OssComponentsLicense license = new OssComponentsLicense();
								// 컴포넌트 ID 설정
								license.setComponentId(projectMapper.selectLastComponent());
								
								// 라이센스 ID 설정
								if (StringUtil.isEmpty(comLicense.getLicenseId())) {
									license.setLicenseId(CommonFunction.getLicenseIdByName(comLicense.getLicenseName()));
								} else {
									license.setLicenseId(comLicense.getLicenseId());
								}
								
								// 기타 설정
								license.setLicenseName(comLicense.getLicenseName());
								license.setLicenseText(comLicense.getLicenseText());
								license.setCopyrightText(comLicense.getCopyrightText());
								license.setExcludeYn(avoidNull(comLicense.getExcludeYn(), CoConstDef.FLAG_NO));
								
								// 라이센스 등록
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else { //싱글라이센스일경우
					OssComponentsLicense license = new OssComponentsLicense();
					// 컴포넌트 ID 설정
					license.setComponentId(projectMapper.selectLastComponent());
					
					// 라이센스 ID 설정
					if (StringUtil.isEmpty(ossBean.getLicenseId())) {
						license.setLicenseId(CommonFunction.getLicenseIdByName(ossBean.getLicenseName()));
					} else {
						license.setLicenseId(ossBean.getLicenseId());
					}
					
					// 기타 설정
					license.setLicenseName(ossBean.getLicenseName());
					license.setLicenseText(ossBean.getLicenseText());
					license.setCopyrightText(ossBean.getCopyrightText());
					license.setExcludeYn(CoConstDef.FLAG_NO);
					
					// 라이센스 등록
					projectMapper.registComponentLicense(license);
				}
			}
		}
		
		//delete
		OssComponents param = new OssComponents();
		param.setReferenceDiv(prjYn ? CoConstDef.CD_DTL_COMPONENT_ID_BAT : CoConstDef.CD_DTL_COMPONENT_PARTNER);
		param.setReferenceId(prjId);
		param.setOssComponentsIdList(deleteRows);
		
		projectMapper.deleteOssComponentsWithIds(param);
	}

	@Override
	public Map<String, Object> getPartnerOssList(OssComponents ossComponents) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		String referenceId = ossComponents.getReferenceId();
		
		ossComponents.setReferenceDiv(avoidNull(ossComponents.getReferenceDiv(), CoConstDef.CD_DTL_COMPONENT_PARTNER));
		List<OssComponents> list = projectMapper.getPartnerOssList(ossComponents);
		ProjectIdentification param = new ProjectIdentification();
		
		for (OssComponents oc : list){
			if (!isEmpty(referenceId) && isEmpty(oc.getRefPartnerId())) {
				oc.setRefPartnerId(referenceId);
			}
			if (CoConstDef.FLAG_YES.equals(oc.getExcludeYn())){
				param.addComponentIdList(oc.getComponentId());
			}
		}
		
		Map<String, List<ProjectIdentification>> licenseMap = new HashMap<>();
		if (param.getComponentIdList() != null && !param.getComponentIdList().isEmpty()) {
			List<ProjectIdentification> subGridData = projectMapper.identificationSubGrid(param);
			for (ProjectIdentification ocl : subGridData) {
				String key = ocl.getComponentId();
				List<ProjectIdentification> thridLicenses = null;
				if (licenseMap.containsKey(key)) {
					thridLicenses = licenseMap.get(ocl.getComponentId());
				} else {
					thridLicenses = new ArrayList<>();
				}
				ocl.setEditable(CoConstDef.FLAG_YES);
				thridLicenses.add(ocl);
				licenseMap.put(key, thridLicenses);
			}
			
			for (OssComponents oc : list){
				if (licenseMap.containsKey(oc.getComponentId())) {
					List<ProjectIdentification> licenseList = licenseMap.get(oc.getComponentId());
					oc.setLicenseName(licenseList.get(0).getLicenseName());
					oc.setLicenseText(licenseList.get(0).getLicenseText());
					oc.setCopyrightText(licenseList.get(0).getCopyrightText());
				}
			}
		}
		
		map.put("rows", list);
		
		return map; 
	}
	
	@Override
	public List<ProjectIdentification> getBomListExcel(ProjectIdentification bom) {
		bom.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		List<ProjectIdentification> list = projectMapper.selectBomList(bom);
		Comparator<ProjectIdentification> compare = Comparator
				.comparing(ProjectIdentification::getLicenseTypeIdx)
				.thenComparing(ProjectIdentification::getOssName, Comparator.nullsFirst(Comparator.naturalOrder()))
				.thenComparing(ProjectIdentification::getOssVersion, (str1, str2) -> str2.compareTo(str1))
				.thenComparing(ProjectIdentification::getDownloadLocation, Comparator.reverseOrder())
				.thenComparing(ProjectIdentification::getLicenseName, Comparator.nullsFirst(Comparator.naturalOrder()))
				.thenComparing(ProjectIdentification::getHomepage, Comparator.naturalOrder())
				.thenComparing(ProjectIdentification::getMergeOrder);
		list.sort(compare);
		
		Map<String, List<OssComponentsLicense>> bomLicenseMap = new HashMap<>();
		List<OssComponentsLicense> bomLicenseList = projectMapper.selectBomLicenseList(bom);
		for (OssComponentsLicense ocl : bomLicenseList) {
			String key = ocl.getComponentId();
			List<OssComponentsLicense> bomLicenses = null;
			if (bomLicenseMap.containsKey(key)) {
				bomLicenses = bomLicenseMap.get(ocl.getComponentId());
			} else {
				bomLicenses = new ArrayList<>();
			}
			bomLicenses.add(ocl);
			bomLicenseMap.put(key, bomLicenses);
		}
		
		List<ProjectIdentification> result = new ArrayList<>();
		
		List<OssComponentsLicense> license = null;
		String licenseId = "";
		String licenseName = "";
		String licenseText = "";
		String copyrightText = "";
		
		// 대상 컴포넌트 추출
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getComponentId() != null) {
				// 컴포넌트 라이센스 조회
				list.get(i).setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
				if (bomLicenseMap.containsKey(list.get(i).getComponentId())) {
					license = bomLicenseMap.get(list.get(i).getComponentId());
					
					for (int j = 0; j < license.size(); j++){
						if (j == 0) {
							licenseId = license.get(j).getLicenseId();
							licenseName = license.get(j).getLicenseName();
							licenseText = license.get(j).getLicenseText();
							copyrightText = license.get(j).getCopyrightText();
						} else {
							licenseId = licenseId + ","+license.get(j).getLicenseId();
							licenseName = licenseName + ","+license.get(j).getLicenseName();
							licenseText = licenseText + ","+license.get(j).getLicenseText();
							copyrightText = copyrightText + ","+license.get(j).getCopyrightText();
						}
					}
					list.get(i).setLicenseId(licenseId);
					list.get(i).setLicenseName(licenseName);
					list.get(i).setLicenseText(licenseText);
					list.get(i).setCopyrightText(copyrightText);
				}
				
//				license = projectMapper.selectBomLicense(list.get(i));
//				for (int j = 0; j < license.size(); j++){
//					if (j == 0) {
//						licenseId = license.get(j).getLicenseId();
//						licenseName = license.get(j).getLicenseName();
//						licenseText = license.get(j).getLicenseText();
//						copyrightText = license.get(j).getCopyrightText();
//					} else {
//						licenseId = licenseId + ","+license.get(j).getLicenseId();
//						licenseName = licenseName + ","+license.get(j).getLicenseName();
//						licenseText = licenseText + ","+license.get(j).getLicenseText();
//						copyrightText = copyrightText + ","+license.get(j).getCopyrightText();
//					}
//				}
//				
//				list.get(i).setLicenseId(licenseId);
//				list.get(i).setLicenseName(licenseName);
//				list.get(i).setLicenseText(licenseText);
//				list.get(i).setCopyrightText(copyrightText);
				
				// oss Name은 작성하고, oss Version은 작성하지 않은 case경우 해당 분기문에서 처리
				if (isEmpty(list.get(i).getCveId()) 
						&& isEmpty(list.get(i).getOssVersion()) 
						&& !isEmpty(list.get(i).getCvssScoreMax())
						&& !("-".equals(list.get(i).getOssName()))) { 
					String[] cvssScoreMax = list.get(i).getCvssScoreMax().split("\\@");
					list.get(i).setCvssScore(cvssScoreMax[0]);
					list.get(i).setCveId(cvssScoreMax[1]);
				}
				
				result.add(list.get(i));
			}
		}
		
		return result;
	}
	
	@Override
	public Map<String, Object> getIdentificationProjectSearch(ProjectIdentification projectIdentification) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		Project project = new Project();
		project.setSrcAndroidNoticeFileId(projectIdentification.getAndroidNoticeFileId());
		project.setSrcAndroidResultFileId(projectIdentification.getAndroidResultFileId());
		
		List<ProjectIdentification> list = projectMapper.getIdentificationProjectSearch(projectIdentification);
		
		project.setAndroidNoticeFile(projectMapper.selectAndroidNoticeFile(project));
		project.setAndroidResultFile(projectMapper.selectAndroidResultFile(project));
		
		map.put("rows", list);
		map.put("project", project);
		
		return map; 
	}

	@Override
	public Map<String, Object> getIdentificationThird(OssComponents ossComponents) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		ossComponents.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
		List<ProjectIdentification> list = projectMapper.getPartnerOssListValidation(ossComponents);
		List<String> componentIdList = new ArrayList<>();
		Map<String, ProjectIdentification> componentIdLicenseMap = new HashMap<>();
		
		for (ProjectIdentification oc : list) {
			if (CoConstDef.FLAG_YES.equals(oc.getExcludeYn())) {
				componentIdList.add(oc.getComponentId());
			}
		}
		
		if (!componentIdList.isEmpty()) {
			ProjectIdentification pi = new ProjectIdentification();
			pi.setComponentIdList(componentIdList);
			List<ProjectIdentification> subGridData = projectMapper.identificationSubGrid(pi);
			subGridData.forEach(projectIdentification -> {
				String key = projectIdentification.getComponentId();
				if (!componentIdLicenseMap.containsKey(key)) {
					componentIdLicenseMap.put(key, projectIdentification);
				}
			});
			
			for (ProjectIdentification oc : list) {
				if (componentIdLicenseMap.containsKey(oc.getComponentId())) {
					ProjectIdentification PI = componentIdLicenseMap.get(oc.getComponentId());
					oc.setLicenseName(PI.getLicenseName());
					oc.setLicenseText(PI.getLicenseText());
					oc.setCopyrightText(PI.getCopyrightText());
				}
			}
		}
		
		T2CoProjectValidator pv = new T2CoProjectValidator();
		
		if (list != null) {
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_PARTNER);

			// main grid
			pv.setAppendix("mainList", list);

			T2CoValidationResult vr = pv.validate(new HashMap<>());
			// return validator result
			// Identification, 3rd Party 의 OSS Table 정렬 순위 변경 - Restriction 추가
			map.put("rows", CommonFunction.identificationSortByValidInfo(list, vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false, true));
			
			if (!vr.isValid()) {
				map.put("validData", vr.getValidMessageMap());
			}
			
			if (!vr.isDiff()) {
				map.put("diffData", vr.getDiffMessageMap());
			}
			
			if (vr.hasInfo()) {
				map.put("infoData", vr.getInfoMessageMap());
			}
		}
		
		return map; 
	}

	@Override
	@Cacheable(value="autocompleteProjectCache", key="{#root.methodName, #project?.creator}")
	public List<Project> getProjectVersionList(Project project) {
		return projectMapper.getProjectVersionList(project);
	}
	
	@Override
	public List<Project> getProjectDivisionList(Project project) {
		return projectMapper.getProjectDivisionList(project);
	}
	
	@Override
	public Map<String, Object> applySrcAndroidModel(List<ProjectIdentification> reportData, List<String> noticeBinaryList) throws IOException {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, String> validMap = new HashMap<>();

		if (noticeBinaryList != null) {
			for (ProjectIdentification bean : reportData) {
				String binaryNm = bean.getBinaryName();
				String binaryNameWithoutPath = binaryNm;

				if (binaryNm.indexOf("/") > -1) {
					if (!binaryNm.endsWith("/")) {
						binaryNameWithoutPath = binaryNm.substring(binaryNm.lastIndexOf("/") + 1);
					}
				}
				bean.setBinaryNotice("nok");
				if (noticeBinaryList.contains(binaryNameWithoutPath)) {
					bean.setBinaryNotice("ok");
				} else {
					try {
						ArrayList<String> apex_name_to_search = new ArrayList<String>();
						Pattern pattern = Pattern.compile("apex/([^/]+)/");
						Matcher matcher = pattern.matcher(binaryNm);
						while (matcher.find()) {
							String apex_name = matcher.group(1);
							if (!apex_name.isEmpty()) {
								apex_name_to_search.add(apex_name + ".apex");
								apex_name_to_search.add(apex_name + ".capex");
								apex_name_to_search.add(apex_name + "_compressed.apex");
								apex_name_to_search.add(apex_name + "-uncompressed.apex");
							}
							break;
						}
						for (String apex_search : apex_name_to_search) {
							if (noticeBinaryList.contains(apex_search)) {
								bean.setBinaryNotice("ok");
								break;
							}
						}
					} catch (Exception e) {
						log.debug(e.getMessage());
					}
				}
			}
		} else {
			for (ProjectIdentification bean : reportData) {
				bean.setBinaryNotice("nok");
			}
		}
		
		resultMap.put("reportData", reportData);
		
		if (!validMap.isEmpty()) {
			resultMap.put("validData", validMap);
		}
		
		return resultMap;
	}

	@Override
	public List<UploadFile> selectAndroidFileDetail(Project project) {
		List<T2File> file = projectMapper.selectAndroidCsvFile(project);
		UploadFile param = new UploadFile();
		param.setFileName(file.get(0).getLogiNm());
		
		List<UploadFile> result = new ArrayList<UploadFile>();
		result.add(param);
		
		return result;
	}

	@Override
	public Map<String, Object> getOssIdCheck(ProjectIdentification projectIdentification) {
		ProjectIdentification ossIdInfo = projectMapper.getOssId(projectIdentification);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ossIdInfo", ossIdInfo);
		
		return map;
	}

	@Override
	public String checkChangedIdentification(String prjId, List<ProjectIdentification> partyData,
			List<ProjectIdentification> srcData, List<List<ProjectIdentification>> srcSubData,
			List<ProjectIdentification> binData, List<List<ProjectIdentification>> binSubData,
			String applicableParty, String applicableSrc, String applicableBin) {
		
		// 현재 DB에 등록되어 있는 정보와 CLIENT에서 넘어온 정보를 SORT 한 후 문자열로 비교한다.
		// TRIM, 대소문자를 무시하고, 주요 칼럼만 비교한다.
		List<String> dbInfoList = projectMapper.checkChangedIdentification(prjId);
		
		Project projectInfo = projectMapper.selectProjectMaster2(prjId);
		
		List<String> dbPartnerList = new ArrayList<>();
		List<String> dbSrcList = new ArrayList<>();
		List<String> dbBinList = new ArrayList<>();

		List<String> partnerList = null;
		List<String> srcList = null;
		List<String> binList = null;

		// LICENSE NAME으로 비교시 SHORT IDENTIFIER로 설정된 경우 에러로 판단하는 오류 대응을 위해 두가지 패턴으로 체크
		List<String> partnerList2 = null;
		List<String> srcList2 = null;
		List<String> binList2 = null;

		boolean partnerUseFlag = CoConstDef.FLAG_NO.equals(applicableParty);
		boolean srcUseFlag = CoConstDef.FLAG_NO.equals(applicableSrc);
		boolean binUseFlag = CoConstDef.FLAG_NO.equals(applicableBin);
		boolean dbPartnerUseFlag = CoConstDef.FLAG_NO.equals(projectInfo.getIdentificationSubStatusPartner());
		boolean dbSrcUseFlag = CoConstDef.FLAG_NO.equals(projectInfo.getIdentificationSubStatusSrc());
		boolean dbBinUseFlag = CoConstDef.FLAG_NO.equals(projectInfo.getIdentificationSubStatusBin());
		
		if (partnerUseFlag != dbPartnerUseFlag) {
			return getMessage("msg.project.check.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)});
		} else if (srcUseFlag != dbSrcUseFlag) {
			return getMessage("msg.project.check.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_SRC)});
		} else if (binUseFlag != dbBinUseFlag) {
			return getMessage("msg.project.check.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_BIN)});
		}
		
 		if (dbInfoList != null && !dbInfoList.isEmpty()) {
 			for (String key : dbInfoList) {
 				key = key.toUpperCase();
 				if (!dbPartnerUseFlag && key.startsWith(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)) {
 					// 3rd의 경우 라이선스를 무시하고 key를 생성하기 때문에 중복되는 경우가 있음
 					if (!dbPartnerList.contains(key)) {
 						dbPartnerList.add(key);
 					}
 				} else if (!dbSrcUseFlag && key.startsWith(CoConstDef.CD_DTL_COMPONENT_ID_SRC)) {
 					dbSrcList.add(key);
 				} else if (!dbBinUseFlag && key.startsWith(CoConstDef.CD_DTL_COMPONENT_ID_BIN)) {
 					dbBinList.add(key);
 				}
 			}
		}
 		
		// 화면정보 비교 key로 convert
		// 3rd party의 경우는 편집이 불가능하기 때문에 라이선스 정보를 제외하고 비교한다.
 		if (!partnerUseFlag && partyData != null) {
 			partnerList = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, partyData, null);
 			partnerList2 = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, partyData, null, true);
 		}
 		
 		if (!srcUseFlag && srcData != null && srcSubData != null) {
 			srcList = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_SRC, srcData, srcSubData);
 			srcList2 = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_SRC, srcData, srcSubData, true);
 		}
 		
 		if (!binUseFlag && binData != null && binSubData != null) {
 			binList = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_BIN, binData, binSubData);
 			binList2 = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_BIN, binData, binSubData, true);
 		}
 		
 		if (partnerList == null) {
 			partnerList = new ArrayList<>();
 			partnerList2 = new ArrayList<>();
 		}
 		
 		if (srcList == null) {
 			srcList = new ArrayList<>();
 			srcList2 = new ArrayList<>();
 		}
 		
 		if (binList == null) {
 			binList = new ArrayList<>();
 			binList2 = new ArrayList<>();
 		}
 		
 		// 1) 건수 비교 
 		// 2) 건수가 동일하기 때문에 sort후 text 비교
 		if (partnerList.size() != dbPartnerList.size() || !compareList(partnerList, partnerList2, dbPartnerList)) {
 			return getMessage("msg.project.check.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)});
 		} else if (srcList.size() != dbSrcList.size() || !compareList(srcList, srcList2, dbSrcList)) {
 			return getMessage("msg.project.check.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_SRC)});
 		} else if (binList.size() != dbBinList.size() || !compareList(binList, binList2, dbBinList)) {
 			return getMessage("msg.project.check.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_BIN)});
 		}
 		
 		return null;
	}
	
	private boolean compareList(List<String> list, List<String> list2, List<String> dbList) {
		if (list.size() != dbList.size()) {
			return false;
		}
		
		Collections.sort(list);
		Collections.sort(list2);
		Collections.sort(dbList);
		
		for (int i=0; i<list.size(); i++) {
			if (!list.get(i).equalsIgnoreCase(dbList.get(i)) && !list2.get(i).equalsIgnoreCase(dbList.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	private List<String> makeCompareKey(String type, List<ProjectIdentification> data, List<List<ProjectIdentification>> subData) {
		return makeCompareKey(type, data, subData, false);
	}
	
	private List<String> makeCompareKey(String type, List<ProjectIdentification> data, List<List<ProjectIdentification>> subData, boolean convertShortLicenseName) {
		List<String> list = new ArrayList<>();
		
		if (data != null) {
			for (ProjectIdentification bean : data) {
				if (CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(type)) {
					String key = type;
					key += "|" + avoidNull(bean.getOssName()).trim();
					key += "|" + avoidNull(bean.getOssVersion()).trim();
					key += "|" + avoidNull(bean.getRefPartnerId()).trim();
					key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
					key = key.toUpperCase();
					
					if (!list.contains(key)) {
						list.add(key);
					}
				} else if (subData != null) {
//					if (CoConstDef.LICENSE_DIV_SINGLE.equals(avoidNull(bean.getLicenseDiv(), CoConstDef.LICENSE_DIV_SINGLE))) {
//						String key = type;
//						key += "|" + avoidNull(bean.getOssName()).trim();
//						key += "|" + avoidNull(bean.getOssVersion()).trim();
//						key += "|" + avoidNull(convertLicenseShortName(bean.getLicenseName(), convertShortLicenseName)).trim();
//						key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
//						key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
//						key = key.toUpperCase();
//					
//						list.add(key);
//					} else {
//					
//					}
				
					if (bean.getComponentLicenseList() != null) {
						for (ProjectIdentification license : bean.getComponentLicenseList()) {
							String key = type;
							key += "|" + avoidNull(bean.getOssName()).trim();
							key += "|" + avoidNull(bean.getOssVersion()).trim();
							key += "|" + avoidNull(convertLicenseShortName(license.getLicenseName(), convertShortLicenseName)).trim();
							key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
							key += "|" + avoidNull(license.getExcludeYn(), CoConstDef.FLAG_NO);
							key = key.toUpperCase();
							
							list.add(key);
						}
					} else {
						String key = type;
						key += "|" + avoidNull(bean.getOssName()).trim();
						key += "|" + avoidNull(bean.getOssVersion()).trim();
						key += "|" + avoidNull("").trim();
						key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
						key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
						key = key.toUpperCase();
						
						list.add(key);
					}
				}
			}
		}
		
		return list;
	}
	
	/**
	 * 라이선스 명칭을 기준으로 short identifier인 경우 정식명칭을, 정식명칭(또는 닉네임)이면서, short identifier가 설정되어 있는 경우는 short identifier를 반환한다.
	 * 그외 경우는 그대로 반환
	 * @param licenseName
	 * @param convertShortLicenseName
	 * @return
	 */
	private String convertLicenseShortName(String licenseName, boolean convertShortLicenseName) {
		if (convertShortLicenseName && !isEmpty(licenseName)) {
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
				LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
				
				if (master != null) {
					// 현재 라이선스 명이 short identifier 이면 정식 명칭을 반환
					if (licenseName.equals(master.getShortIdentifier())) {
						licenseName = master.getLicenseNameTemp();
					} else if (!isEmpty(master.getShortIdentifier())) {
						// 현재 라이선스 명이 정식 명칭 (또는 닉네임) 인 경우 short identifier를 반환
						licenseName = master.getShortIdentifier();
					}
				}
			}
		}
		
		return avoidNull(licenseName).trim();
	}
	
//	private List<ProjectIdentification> findLicense(String gridId, List<List<ProjectIdentification>> componentLicenseList) {
//		List<ProjectIdentification> licenseList = new ArrayList<>();
//		if (componentLicenseList != null && !componentLicenseList.isEmpty()) {
//			boolean breakFlag = false;
//			for (List<ProjectIdentification> list : componentLicenseList) {
//				for (ProjectIdentification bean : list) {
//					String key = gridId;
//					if (bean.getGridId().startsWith(key)) {
//						licenseList.add(bean);
//						breakFlag = true;
//					}
//				}
//				if (breakFlag) {
//					break;
//				}
//			}
//		}
//		return licenseList;
//	}
	
	@Override
	public Map<String, Map<String, String>> getProjectDownloadExpandInfo(Project param) {
		Map<String, Map<String, String>> resultMap = new HashMap<>();
		
		if (param.getPrjIdList() != null && !param.getPrjIdList().isEmpty()) {
			List<Map<String, String>> list = projectMapper.getProjectDownloadExpandInfo(param);
			
			if (list != null) {
				for (Map<String, String> map : list) {
					resultMap.put(String.valueOf(map.get("PRJ_ID")), map);
				}
			}
		}
		
		return resultMap;
	}

	@Override
	public void cancelFileDel(Project project) {
		if (project.getCsvFile() != null) {
			for (int i = 0; i < project.getCsvFile().size(); i++) {
				projectMapper.deleteFileBySeq(project.getCsvFile().get(i));
			}				
		}
	}

	@Override
	public List<OssComponents> selectOssComponentsListByComponentIds(OssComponents param) {
		return projectMapper.selectOssComponentsListByComponentIds(param);
	}

	@Override
	public Map<String, Object> getFileInfo(ProjectIdentification identification) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		Project project = new Project();
		project.setSrcAndroidCsvFileId(identification.getAndroidCsvFileId());
		project.setSrcAndroidNoticeFileId(identification.getAndroidNoticeFileId());
		project.setSrcAndroidResultFileId(identification.getAndroidResultFileId());
		
		project.setAndroidCsvFile(projectMapper.selectAndroidCsvFile(project));
		project.setAndroidNoticeFile(projectMapper.selectAndroidNoticeFile(project));
		project.setAndroidResultFile(projectMapper.selectAndroidResultFile(project));
		
		map.put("project", project);
		
		return map; 
	}

	@Override
	public Map<String, Object> get3rdMapList(Project project) {
		List<PartnerMaster> list = new ArrayList<PartnerMaster>();
		list = partnerMapper.select3rdMapList(project);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("rows", list);
		
		return map; 
	}

	private Map<String, Object> getThirdPartyMap(String prjId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		List<PartnerMaster> list = partnerMapper.selectThirdPartyMapList(prjId);
		
		map.put("rows", list);
		return map; 
	}

	@Override
	public List<Project> getWatcherList(Project project) {
		List<Project> watcherList = projectMapper.selectWatchersList(project);
		
		return watcherList;
	}

	/**
	 * oss Nickname convert
	 * @param ossComponentList
	 * @return
	 */
	@Override
	public List<ProjectIdentification> convertOssNickName(List<ProjectIdentification> ossComponentList) {
		List<String> ossCheckParam = new ArrayList<>();
		List<OssMaster> ossNickNameList = null;
		Map<String, OssMaster> ossNickNameConvertMap = new HashMap<>();
		
		for (ProjectIdentification bean : ossComponentList) {
			String _ossName = avoidNull(bean.getOssName()).trim();
			int isAdminCheck = projectMapper.selectAdminCheckCnt(bean);
			
			if (!isEmpty(_ossName) && !"-".equals(_ossName) && !ossCheckParam.contains(_ossName) && isAdminCheck < 1) {
				ossCheckParam.add(_ossName);
			}
		}
		
		if (!ossCheckParam.isEmpty()) {
			OssMaster param = new OssMaster();
			param.setOssNames(ossCheckParam.toArray(new String[ossCheckParam.size()]));
			ossNickNameList = projectMapper.checkOssNickName(param);
			
			if (ossNickNameList != null) {
				for (OssMaster bean : ossNickNameList) {
					ossNickNameConvertMap.put(bean.getOssNickname().toUpperCase(), bean);
				}
			}
		}

		for (ProjectIdentification bean : ossComponentList) {
			if (ossNickNameConvertMap.containsKey(avoidNull(bean.getOssName()).trim().toUpperCase())) {
				bean.setOssName(ossNickNameConvertMap.get(avoidNull(bean.getOssName()).trim().toUpperCase()).getOssName());
			}
			
			// license nickname 체크
			if (CoConstDef.LICENSE_DIV_SINGLE.equals(bean.getLicenseDiv())) {
				String _licenseName = avoidNull(bean.getLicenseName()).trim();
				
				if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(_licenseName.toUpperCase())) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_licenseName.toUpperCase());
					
					if (licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
						for (String s : licenseMaster.getLicenseNicknameList()) {
							if (_licenseName.equalsIgnoreCase(s)) {
								bean.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp()));
								
								break;
							}
						}
					}
				}
			}
		}
		
		return ossComponentList;
	}

	@Override
	public List<List<ProjectIdentification>> convertLicenseNickName(
			List<List<ProjectIdentification>> ossComponentLicenseList) {
		if (ossComponentLicenseList != null) {
			for (List<ProjectIdentification> licenseList : ossComponentLicenseList) {
				for (ProjectIdentification licenseBean : licenseList) {
					String _licenseName = avoidNull(licenseBean.getLicenseName()).trim();
					if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(_licenseName.toUpperCase())) {
						LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_licenseName.toUpperCase());
						
						if (licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
							for (String s : licenseMaster.getLicenseNicknameList()) {
								if (_licenseName.equalsIgnoreCase(s)) {
									licenseBean.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp()));
									
									break;
								}
							}
						}
					}
				}
			}
		}
		
		return ossComponentLicenseList;
	}

	@Override
	public String addWatcher(Project project) {
		String addWatcher = "";
		
		if (!isEmpty(project.getPrjEmail())) {
			if (projectMapper.existsWatcherByEmail(project) == 0) { // 이미 추가된 watcher 체크
				projectMapper.insertWatcher(project); // watcher 추가
				
				// email 발송
				try {
					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_INVATED);
					mailBean.setParamPrjId(project.getPrjId());
					mailBean.setParamUserId(project.getLoginUserName());
					mailBean.setParamEmail(project.getPrjEmail());
					
					CoMailManager.getInstance().sendMail(mailBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} else {
			if (projectMapper.existsWatcherByUser(project) == 0) { // 이미 추가된 watcher 체크
				if (projectMapper.existsWatcherByUserDivistion(project) > 0) { // 이미 추가된 watcher 의 user division 이 변경된 경우
					projectMapper.updateWatcherDivision(project);
				} else {
					projectMapper.insertWatcher(project); // watcher 추가
				}
			}
		}
		
		if (!isEmpty(project.getPrjUserId())) {
			T2Users userInfo = new T2Users();
			userInfo.setUserId(project.getPrjUserId());
			userInfo = userMapper.getUser(userInfo);
			addWatcher = project.getPrjDivision() + "/" + userInfo.getUserId();
		} else {
			addWatcher = project.getPrjEmail();
		}
		
		return addWatcher;
	}

	@Override
	public void removeWatcher(Project project) {
		projectMapper.removeWatcher(project);		
	}

	@Override
	public List<Project> copyWatcher(Project project) {
		return projectMapper.copyWatcher(project);
	}

	@Override
	public boolean existsWatcher(Project project) {
		boolean result = false;
		int i = projectMapper.existsWatcher(project);
		
		if (i > 0){
			result = true;
		}	
		
		return result;
	}

	/**
	 * 분석결과서 다운로드 3rd party 명칭 반환
	 */
	@Override
	public String getPartnerFormatName(String partnerId, boolean onlyName) {
		if (!isEmpty(partnerId)) {
			PartnerMaster param = new PartnerMaster();
			param.setPartnerId(partnerId);
			param = partnerMapper.selectPartnerMaster(param);

			if (param != null) {
				if (onlyName) {
					return avoidNull(param.getPartnerName());
				} else {
					return "(" + partnerId + ") " + avoidNull(param.getPartnerName());
				}
			}
		}
		return "";
	}

	// project model update
	@Override
	public void insertProjectModel(Project project) {
		projectMapper.updateDistributeTarget(project);
		projectMapper.deleteProjectModel(project);
		if (project.getModelList().size() > 0) {
			for (Project bean : project.getModelList()) {
				bean.setPrjId(project.getPrjId());
				bean.setModelName(bean.getModelName().trim().toUpperCase().replaceAll("\t", ""));
				projectMapper.insertProjectModel(bean);
			}
		}
	}

	@Override
	public void updatePublicYn(Project project) {
		projectMapper.updatePublicYn(project);
	}

	@Override
	public Map<String, Object> getProjectToAddList(OssComponents ossComponents) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<PartnerMaster> list = partnerMapper.getProjectToAddList(ossComponents);
		
		map.put("rows", list);
		
		return map;
	}

	@Override
	public Map<String, Object> getAddList(Project project) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		List<Project> list = projectMapper.selectAddList(project);
		map.put("rows", list);
		
		return map; 
	}

	@Override
	public boolean existsAddList(Project project) {
		boolean result = false;
		
		int i = projectMapper.existsAddList(project);

		if (i > 0){
			projectMapper.deleteAddList(project);
			
			result = true;
		}	
		
		return result;
	}

	@Override
	public void insertAddList(List<Project> project) {
		int idx = 1;
		for (Project p : project){
			p.setPrjRefIdx(idx++);
			projectMapper.insertAddList(p);
		}
	}
	
	@Override
	public Map<String, Object> identificationSubGrid(ProjectIdentification identification) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			List<ProjectIdentification> list = projectMapper.identificationSubGrid(identification);
			
			map.put("rows", list);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return map;
	}

	@Override
	public List<ProjectIdentification> setMergeGridData(List<ProjectIdentification> gridData) {
		List<ProjectIdentification> tempData = new ArrayList<ProjectIdentification>();
		List<ProjectIdentification> resultGridData = new ArrayList<ProjectIdentification>();
		String groupColumn = "";
		
		for (ProjectIdentification info : gridData) {
			if (isEmpty(groupColumn)) {
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
			}
			if (groupColumn.equals(info.getOssName() + "-" + info.getOssVersion())) {
				// 같은 groupColumn이면 데이터를 쌓음
				tempData.add(info);
			} else { // 다른 grouping
				setMergeData(tempData, resultGridData);
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
				tempData.clear();
				tempData.add(info);
			}
		}
		
		setMergeData(tempData, resultGridData); // bom data의 loop가 끝났지만 tempData에 값이 있다면 해당 값도 merge를 함.
		
		return resultGridData;
	}
	
	public static void setMergeData(List<ProjectIdentification> tempData, List<ProjectIdentification> resultGridData){
		if (tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<ProjectIdentification>() {
				@Override
				public int compare(ProjectIdentification o1, ProjectIdentification o2) {
					if (o1.getLicenseName().length() >= o2.getLicenseName().length()) { // license name이 같으면 bomList조회해온 순서 그대로 유지함. license name이 다르면 순서변경
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			ProjectIdentification rtnBean = null;
			
			for (ProjectIdentification temp : tempData) {
				if (rtnBean == null) {
					rtnBean = temp;
					if (!isEmpty(rtnBean.getCopyrightText())) {
						List<String> rtnBeanCopyrights = Arrays.asList(rtnBean.getCopyrightText().split("\\n"));
						String mergedCopyrightText = rtnBeanCopyrights.stream().distinct().collect(Collectors.joining("\n"));
						rtnBean.setCopyrightText(mergedCopyrightText);
					}
					continue;
				}
				
				if ("-".equals(temp.getOssName())) {
					boolean licenseSameFlag = false;
					List<String> tempLicenses = Arrays.asList(temp.getLicenseName().split(","));
					List<String> rtnBeanLicenses = Arrays.asList(rtnBean.getLicenseName().split(","));
					List<String> matchLicenses = tempLicenses.stream().filter(e -> rtnBeanLicenses.stream().anyMatch(Predicate.isEqual(e))).collect(Collectors.toList());
					
					if (!CollectionUtils.isEmpty(matchLicenses) && matchLicenses.size() == tempLicenses.size()) {
						licenseSameFlag = true;
					}
					
					if (licenseSameFlag && temp.getDownloadLocation().equalsIgnoreCase(rtnBean.getDownloadLocation()) && temp.getHomepage().equalsIgnoreCase(rtnBean.getHomepage())) {
					} else {
						resultGridData.add(rtnBean);
						rtnBean = temp;
						continue;
					}
				}
				
				// 동일한 oss name과 version일 경우 license 정보를 중복제거하여 merge 함.
				for (String licenseName : temp.getLicenseName().split(",")) {
					boolean equalFlag = false;
					
					for (String rtnLicenseName : rtnBean.getLicenseName().split(",")) {
						if (rtnLicenseName.equals(licenseName)) {
							equalFlag = true;
							break;
						}
					}
					
					if (!equalFlag) {
						rtnBean.setLicenseName(rtnBean.getLicenseName() + "," + licenseName);
					}
				}
				
				if (!isEmpty(temp.getCopyrightText())) {
					List<String> mergedCopyrights = new ArrayList<>();
					if (!isEmpty(rtnBean.getCopyrightText())) {
						mergedCopyrights.addAll(Arrays.asList(rtnBean.getCopyrightText().split("\\n")));
					}
					if (!isEmpty(rtnBean.getCopyrightText())) {
						mergedCopyrights.addAll(Arrays.asList(temp.getCopyrightText().split("\\n")));
					}
					if (mergedCopyrights != null && !mergedCopyrights.isEmpty()) {
						String mergedCopyrightText = mergedCopyrights.stream().distinct().collect(Collectors.joining("\n"));
						rtnBean.setCopyrightText(mergedCopyrightText);
					}
				}
				
				List<OssComponentsLicense> rtnComponentLicenseList = new ArrayList<OssComponentsLicense>();
				
				if (!CollectionUtils.isEmpty(temp.getOssComponentsLicenseList())) {
					for (OssComponentsLicense list : temp.getOssComponentsLicenseList()) {
						int equalsItemList = (int) rtnBean.getOssComponentsLicenseList()
															.stream()
															.filter(e -> list.getLicenseName().equals(e.getLicenseName())) // 동일한 licenseName을 filter
															.collect(Collectors.toList()) // return을 list로변환
															.size(); // 해당 list의 size
						
						if (equalsItemList == 0) {
							rtnComponentLicenseList.add(list);
						}
					}
					
					rtnBean.getOssComponentsLicenseList().addAll(rtnComponentLicenseList);
				}
				
				if (!rtnBean.getRefComponentId().contains(temp.getRefComponentId())) {
					rtnBean.setRefComponentId(rtnBean.getRefComponentId() + "," + temp.getRefComponentId());
				}
				
				if (!rtnBean.getRefDiv().contains(temp.getRefDiv())) {
					rtnBean.setRefDiv(rtnBean.getRefDiv() + "," + temp.getRefDiv());
				}

				if (!isEmpty(temp.getRestriction())) {
					for (String restriction : temp.getRestriction().split("\\n")) {
						if (!isEmpty(restriction) && !rtnBean.getRestriction().contains(restriction)) {
							if (!isEmpty(rtnBean.getRestriction())) {
								rtnBean.setRestriction(rtnBean.getRestriction() + "\\n" + restriction);
							} else {
								rtnBean.setRestriction(restriction);
							}
						}
					}
				}
				
				// 특정 tab에 해당 Data가 공란이고, 다른 tab에 해당 Data가 값이 작성되어 있을 경우 첫번째 발견되는 data를 넣어줌. (대상 downloadLocation, homepage, copyrightText)
				if (isEmpty(rtnBean.getDownloadLocation())) { 
					if (!isEmpty(temp.getDownloadLocation())) {
						rtnBean.setDownloadLocation(temp.getDownloadLocation());
					}
				}
				
				if (isEmpty(rtnBean.getHomepage())) {
					if (!isEmpty(temp.getHomepage())) {
						rtnBean.setHomepage(temp.getHomepage());
					}
				}
				
				if (isEmpty(rtnBean.getCopyrightText())) {
					if (!isEmpty(temp.getCopyrightText())) {
						rtnBean.setCopyrightText(temp.getCopyrightText());
					}
				}
				
				if (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(temp.getObligationType())){
					rtnBean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
					rtnBean.setObligationLicense(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
					rtnBean.setPreObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
				} else if (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY.equals(temp.getObligationType())){
					rtnBean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY);
					rtnBean.setObligationLicense(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY);
					rtnBean.setPreObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY);
				}else if (CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(temp.getObligationType())
						&& ("").equals(avoidNull(rtnBean.getObligationType(), ""))){
					rtnBean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
					rtnBean.setObligationLicense(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
					rtnBean.setPreObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
				}
				
				if (isEmpty(rtnBean.getDependencies())) {
					if (!isEmpty(temp.getDependencies())) {
						rtnBean.setDependencies(temp.getDependencies());
					}
				}
			}
			
			resultGridData.add(rtnBean);
		}
	}
	
	@Override
	public List<ProjectIdentification> setMergeGridDataByAndroid(List<ProjectIdentification> gridData) {
		List<ProjectIdentification> resultGridData = null;
		Map<String, ProjectIdentification> resultGridDataMap = new HashMap<>();
		String groupKey = "";
		
		for (ProjectIdentification grid : gridData) {
			groupKey = (grid.getOssName() + "_" + avoidNull(grid.getOssVersion())).toUpperCase();
			
			if (!resultGridDataMap.containsKey(groupKey)) {
				resultGridDataMap.put(groupKey, grid);
			} else {
				ProjectIdentification bean = resultGridDataMap.get(groupKey);
				for (String licenseName : grid.getLicenseName().split(",")) {
					boolean equalFlag = false;
					
					for (String rtnLicenseName : bean.getLicenseName().split(",")) {
						if (rtnLicenseName.trim().equals(licenseName.trim())) {
							equalFlag = true;
							break;
						}
					}
					
					if (!equalFlag) {
						bean.setLicenseName((bean.getLicenseName() + "," + licenseName).trim());
					}
				}
				resultGridDataMap.put(groupKey, bean);
			}
		}
		
		if (!resultGridDataMap.isEmpty()) {
			resultGridData = new ArrayList<>(resultGridDataMap.values());
		}
		
		return resultGridData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String checkValidData(Map<String, Object> map) {
		List<ProjectIdentification> mainData = (List<ProjectIdentification>) map.get("mainData");
		Map<String, Object> validData = (Map<String, Object>) map.get("validData");
		Map<String, Object> diffData = (Map<String, Object>) map.get("diffData");
		String validMsg = null;
		
		int emptyBinaryPathCnt = 0;
		int errCnt = 0;
		int diffCnt = 0;
		
		if (mainData != null) {
			emptyBinaryPathCnt = mainData.stream()
											.filter(c -> isEmpty(c.getBinaryName()) && !CoConstDef.FLAG_YES.equals(avoidNull(c.getExcludeYn(), CoConstDef.FLAG_NO)))
											.collect(Collectors.toList())
											.size();
		}
		
		if (validData != null) {
			errCnt = validData.keySet().stream()
								.filter(c -> c.toUpperCase().contains("OSS_NAME") 
												|| c.toUpperCase().contains("OSS_VERSION") 
												|| c.toUpperCase().contains("LICENSE_NAME"))
								.collect(Collectors.toList())
								.size();
		}
		
		if (diffData != null) {
			Map<String, Object> diffDataMap = new HashMap<String, Object>();
			for (String key : diffData.keySet()) {
				if (key.toUpperCase().contains("LICENSENAME")) {
					String diffMsg = (String) diffData.get(key);
					if (!diffMsg.contains("Declared")) {
						diffDataMap.put(key, diffData.get(key));
					}
				} else {
					diffDataMap.put(key, diffData.get(key));
				}
			}
			
			if (!diffDataMap.isEmpty()) {
				diffCnt = diffDataMap.keySet()
						.stream()
						.filter(c -> c.toUpperCase().contains("OSSNAME") 
										|| c.toUpperCase().contains("OSSVERSION") 
										|| c.toUpperCase().contains("LICENSENAME"))
						.collect(Collectors.toList())
						.size();
			}
		}
		
		// OSS Name, OSS Version, License에 Warning message(빨간색, 파란색)가 있는 Row 또는 Binary Name이 공란인 Row
		if (emptyBinaryPathCnt > 0 || errCnt > 0 ) {
			validMsg = "You can download NOTICE only if there is no warning message in OSS Name, OSS Version, License or Binary Name is not null.";
		}
		
		// 출력할 Binary가 없는 경우(= 출력 조건에 해당하는 Row가 없는 경우)
		if (mainData.size() == 0 || diffData.size() == 0) {
			validMsg = "There is no binary that meets the conditions for creating NOTICE.";
		}
		
		return validMsg;
	}
	
	@Override
	public String makeNoticeFileContents(Map<String, Object> paramMap) {
		Map<String, List<ProjectIdentification>> resultData = getMergedBinaryData(paramMap);
		Map<String, Object> model = new HashMap<String, Object>();
		
		model.put("templateURL", CoCodeManager.getCodeExpString(CoConstDef.CD_NOTICE_DEFAULT, CoConstDef.CD_DTL_SUPPLMENT_NOTICE_HTML_TEMPLATE));
		model.put("noticeData", resultData);
		
		return CommonFunction.VelocityTemplateToString(model);
	}
	
	@Override
	public String makeZipFileId(Map<String, Object> paramMap, Project project) {
		String fileId = "";
		String filePath = CommonFunction.emptyCheckProperty("common.public_supplement_notice_path", "/supplement_notice") + "/" + project.getPrjId();
		File dir = new File(filePath);
		
		if (dir.exists()) {  // 전체 삭제 예쩡( html file은 제외함)
			for (File item : dir.listFiles()) {
				if (!item.isDirectory() && item.getName().contains(".html")){
					item.delete();
				} else if (item.isDirectory()) {
					CommonFunction.removeAll(item); // 하위폴더, file 전체 삭제
				}
			}
		}
		
		String LicensesfilePath = filePath + "/needtoadd-notice/LICENSES";
		dir = new File(LicensesfilePath);
		dir.mkdirs();
		
		Map<String, List<ProjectIdentification>> mergedBinaryData = getMergedBinaryData(paramMap);
		List<String> ObligationNoticeLicenseList = new ArrayList<String>();
		
		for (List<ProjectIdentification> bean : mergedBinaryData.values()) { // Licenses proc
			for (ProjectIdentification p : bean) {
				for (String licenseName : p.getLicenseName().split(",")) {
					if (!ObligationNoticeLicenseList.contains(licenseName)) {
						LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
						
						if (CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn())) {
							String LICENSEFileName = avoidNull(licenseBean.getShortIdentifier(), "");
							
							if (isEmpty(LICENSEFileName)) {
								LICENSEFileName = "LicenseRef-"+licenseBean.getLicenseName().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-");
							}
							
							FileUtil.writeFile(LicensesfilePath, LICENSEFileName+".txt", licenseBean.getLicenseText());
							ObligationNoticeLicenseList.add(licenseName);
						}
					}
				}
			}
		}
		
		String binaryDirPath = filePath + "/needtoadd-notice";
		
		for (String binaryPath : mergedBinaryData.keySet()) { // Binary proc
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("templateURL", CoCodeManager.getCodeExpString(CoConstDef.CD_NOTICE_DEFAULT, CoConstDef.CD_DTL_SUPPLMENT_NOTICE_TXT_TEMPLATE));
			model.put("noticeData", mergedBinaryData.get(binaryPath));
			
			String contents = CommonFunction.VelocityTemplateToString(model);
			contents = HtmlUtils.htmlUnescape(contents);
			for (String path : binaryPath.split(",")) {
				String fileName = "";
				String binaryFilePath = binaryDirPath;
				
				if (path.contains("/")) {
					File f = new File(binaryFilePath + "/" + path);
					fileName = f.getName();
					File parentFile = f.getParentFile();
					if (parentFile != null) {
						binaryFilePath = parentFile.toString();
						f = new File(binaryFilePath);
						f.mkdirs(); // path전체의 directory 생성
					} else {
						fileName = path;	
					}
				} else {
					fileName = path;
				}
				
				FileUtil.writeFile(binaryFilePath, fileName, contents);
			}
		}
		
		try {
			String zipFileName = "needtoadd-notice_"+CommonFunction.getCurrentDateTime("yyMMdd")+".zip";
			FileUtil.zip(binaryDirPath, filePath, zipFileName, null);
			fileId = fileService.registFileDownload(filePath, zipFileName, zipFileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return fileId;
	}
	
	@Override
	public String makeSupplementFileId(String contents, Project project) {
		String fileName = CommonFunction.getNoticeFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), "needtoadd-notice",  DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN), "html");
		String filePath = CommonFunction.emptyCheckProperty("common.public_supplement_notice_path", "/supplement_notice") + "/" + project.getPrjId();
		String fileId = "";
		
		File fileDir = new File(filePath);
		
		if (fileDir.exists()) {
			for (File f : fileDir.listFiles()) {
				if (f.getName().contains(".html")) {
					if (f.delete()) {
						log.debug(filePath + "/" + f.getName() + " is delete success.");
					} else {
						log.debug(filePath + "/" + f.getName() + " is delete failed.");
					}
				}
			}
		}
		
		if (FileUtil.writeFile(filePath, fileName, contents)) {
			// 파일 등록
			fileId = fileService.registFileDownload(filePath, fileName, fileName);
		}
		
		return fileId;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, List<ProjectIdentification>> getMergedBinaryData(Map<String, Object> paramMap){
		List<ProjectIdentification> mainData = (List<ProjectIdentification>) paramMap.get("mainData");
		Map<String, Object> diffData = (Map<String, Object>) paramMap.get("diffData");
		String ruleMsg = (String) T2CoValidationConfig.getInstance().getRuleAllMap().get("BINARY_NOTICE.NOTICE_PERMISSIVE.MSG");
		
		List<String> gridIdList = diffData.entrySet()
											.stream()
											.filter(c -> ((String) c.getValue()).equals(ruleMsg))
											.map(c -> ((String) c.getKey()).split("\\.")[1])
											.collect(Collectors.toList());
		
		mainData = mainData.stream()
							.filter(c -> gridIdList.contains(c.getGridId()))
							.sorted(Comparator.comparing((ProjectIdentification p) -> p.getBinaryName()))
							.collect(Collectors.toList());
		
		Map<String, List<ProjectIdentification>> binaryMergeData = new HashMap<String, List<ProjectIdentification>>();
		Map<String, String> binaryOssKeyMap = new HashMap<String, String>();
		List<String> mergeOssCheckList = new ArrayList<>();
		Map<String, List<String>> mergeLicenseCheckList = new HashMap<>();
		
		for (ProjectIdentification bean : mainData) {
			boolean licenseDuplicateFlag = false;
			List<ProjectIdentification> _list = new ArrayList<>();
			String _oldKey = null;
			String key = bean.getOssName() + "|" + bean.getOssVersion() + "|" + bean.getLicenseName();
			String licenseName = "";
			
			String _key = (bean.getBinaryName() + "|" + bean.getOssName() + "|" + licenseName).toUpperCase();
			if (!mergeOssCheckList.contains(_key)) {
				mergeOssCheckList.add(_key);
			} else {
				continue;
			}
			
			List<String> duplicateCheckList = new ArrayList<>();
			List<ProjectIdentification> deduplicateComponentLicenseList = new ArrayList<>();
			
			if (binaryMergeData.containsKey(bean.getBinaryName())) { // binaryPath 기준으로 merge
				_list = binaryMergeData.get(bean.getBinaryName());
				_oldKey = binaryOssKeyMap.get(bean.getBinaryName());
				
				if (_list != null) {
					for (ProjectIdentification license : bean.getComponentLicenseList()) {
						if (!duplicateCheckList.contains(license.getLicenseName())) {
							duplicateCheckList.add(license.getLicenseName());
							deduplicateComponentLicenseList.add(license);
						}
						
						LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
						if (licenseBean != null) {
							license.setLicenseText(avoidNull(licenseBean.getLicenseText()));
							license.setAttribution(avoidNull(licenseBean.getAttribution()));
							license.setObligationType(CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn()) ? CoConstDef.CD_DTL_OBLIGATION_NOTICE : "");
							
							if (CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn())) {
								if (!isEmpty(licenseName)) {
									licenseName += ",";
								}
								
								licenseName += license.getLicenseName();
							}
						}
					}
					
					if (mergeLicenseCheckList.containsKey(bean.getBinaryName())) {
						List<String> licenseList = mergeLicenseCheckList.get(bean.getBinaryName());
						if (licenseList.contains(licenseName)) {
							licenseDuplicateFlag = true;
						} else {
							licenseList.add(licenseName);
							mergeLicenseCheckList.put(bean.getBinaryName(), licenseList);
						}
					}
					
					OssMaster ossBean = CoCodeManager.OSS_INFO_UPPER.get((bean.getOssName() +"_"+ avoidNull(bean.getOssVersion())).toUpperCase());
					bean.setAttribution(ossBean != null ? avoidNull(ossBean.getAttribution())  : "");
					bean.setLicenseName(licenseName);
					if (!licenseDuplicateFlag) {
						bean.setDeduplicatedComponentLicenseList(deduplicateComponentLicenseList);
					}
					
					_list.add(bean);
					
					binaryMergeData.replace(bean.getBinaryName(), _list);
					String str = CommonFunction.mergedString(_oldKey, key, _oldKey.compareTo(key), ",");
					binaryOssKeyMap.put(bean.getBinaryName(), str);
				}
			} else {
				for (ProjectIdentification license : bean.getComponentLicenseList()) {
					if (!duplicateCheckList.contains(license.getLicenseName())) {
						duplicateCheckList.add(license.getLicenseName());
						deduplicateComponentLicenseList.add(license);
					}
					
					LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
					if (licenseBean != null) {
						license.setLicenseText(avoidNull(licenseBean.getLicenseText()));
						license.setAttribution(avoidNull(licenseBean.getAttribution()));
						license.setObligationType(CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn()) ? CoConstDef.CD_DTL_OBLIGATION_NOTICE : "");

						if (CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn())) {
							if (!isEmpty(licenseName)) {
								licenseName += ",";
							}
							
							licenseName += license.getLicenseName();
						}
					}
				}
				
				List<String> licenseList = new ArrayList<>();
				licenseList.add(licenseName);
				mergeLicenseCheckList.put(bean.getBinaryName(), licenseList);
				
				OssMaster ossBean = CoCodeManager.OSS_INFO_UPPER.get((bean.getOssName() +"_"+ avoidNull(bean.getOssVersion())).toUpperCase());
				
				bean.setAttribution(ossBean != null ? avoidNull(ossBean.getAttribution())  : "");
				bean.setLicenseName(licenseName);
				bean.setDeduplicatedComponentLicenseList(deduplicateComponentLicenseList);
				
				_list.add(bean);
				
				binaryMergeData.put(bean.getBinaryName(), _list);
				binaryOssKeyMap.put(bean.getBinaryName(), key);
			}
		}
		
		Map<String, List<ProjectIdentification>> resultData = new HashMap<String, List<ProjectIdentification>>();
		
		List<String> valuesList = binaryOssKeyMap.values().stream().distinct().collect(Collectors.toList());
		
		Map<String, String> collect = valuesList.stream()
										.collect(Collectors.toMap(Function.identity(), v -> binaryOssKeyMap.entrySet().stream()
												.filter(entry -> Objects.equals(v, entry.getValue()))
												.map(Map.Entry<String, String>::getKey)
												.reduce("", (s1, s2) -> isEmpty(s1) ? s2 : s1 + "," + s2)));
		
		for (String binaryNameList : collect.values()) {
			for (String binaryName : binaryMergeData.keySet()) {
				int matchCnt = Arrays.asList(binaryNameList.split(",")).stream().filter(s -> s.equals(binaryName)).collect(Collectors.toList()).size();
				
				if (matchCnt > 0) {
					resultData.put(binaryNameList, binaryMergeData.get(binaryName));
					
					break;
				}
			}
		}
		
		return resultData;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, String>> getBomCompare(List<ProjectIdentification> beforeBomList, List<ProjectIdentification> afterBomList, String flag)
			throws Exception {
		Map<String, Object> addList = new HashMap<String, Object>();
		Map<String, Object> deleteList = new HashMap<String, Object>();
		
		List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
		
		// bom compare 대상 추출 하기 전에 완전일치하는 대상은 제외하고 처리함 ?
		// bflist loop -> aflist와 ossname & oss version이 완전 일치하는 대상은 제거함.
		// aflist loop -> bflist와 ossname & oss version이 완전 일치하는 대상은 제거함.
		// 이후 delete / change / add 대상 추출함.
		
		List<ProjectIdentification> filteredBeforeBomList = beforeBomList
				.stream()
				.filter(bfList-> 
						afterBomList
								.stream()
								.filter(afList -> 
										(bfList.getOssName() + "||" + bfList.getOssVersion() + "||" + getLicenseNameSort(bfList.getLicenseName().trim()))
										.equalsIgnoreCase(afList.getOssName() + "||" + afList.getOssVersion() + "||" + getLicenseNameSort(afList.getLicenseName().trim()))
										).collect(Collectors.toList()).size() == 0
						).collect(Collectors.toList());
//		filteredBeforeBomList = filteredBeforeBomList.stream().filter(e -> !isEmpty(e.getOssName()) && !e.getOssName().equals("-")).collect(Collectors.toList());
		
		List<ProjectIdentification> filteredAfterBomList = afterBomList
				.stream()
				.filter(afList-> 
						beforeBomList
								.stream()
								.filter(bfList -> 
										(afList.getOssName() + "||" + afList.getOssVersion() + "||" + getLicenseNameSort(afList.getLicenseName().trim()))
										.equalsIgnoreCase(bfList.getOssName() + "||" + bfList.getOssVersion() + "||" + getLicenseNameSort(bfList.getLicenseName().trim()))
										).collect(Collectors.toList()).size() == 0
						).collect(Collectors.toList());
//		filteredAfterBomList = filteredAfterBomList.stream().filter(e -> !isEmpty(e.getOssName()) && !e.getOssName().equals("-")).collect(Collectors.toList());
		
		// status > add
		int addchk = 0;
		for (ProjectIdentification after : filteredAfterBomList) {
			String ossName = after.getOssName();
			int addTargetCnt = filteredBeforeBomList.stream().filter(before -> (before.getOssName()).equals(ossName)).collect(Collectors.toList()).size();
			
			if (addTargetCnt == 0) {
				Map<String, String> addMap = new HashMap<String, String>();
				
				String afterossname = after.getOssName();
				String version = avoidNull(after.getOssVersion(), "");
				
				if (!version.equals("")) {
					afterossname += " (" + version + ")";
				}
				
				String afterlicense = after.getLicenseName();
				
				if (flag.equals("list")) {
					afterossname = changeStyle("add", afterossname);
					afterlicense = changeStyle("add", getLicenseNameSort(afterlicense.trim()));
				}
				
				addMap.put("status", "add");
				addMap.put("beforeossname", "");
				addMap.put("beforelicense", "");
				addMap.put("afterossname", afterossname);
				addMap.put("afterlicense", afterlicense);
				addList.put(getCompareKey(after), addMap);
				
				addchk++;
			}	
		}
		
		if (addchk > 0) {
			for (String key : addList.keySet()) {
				returnList.add((Map<String, String>) addList.get(key));
			}
		}
				
		// status > delete
		int deletechk = 0;
		for (ProjectIdentification before : filteredBeforeBomList) {
			String ossName = before.getOssName();
			List<ProjectIdentification> afterList = filteredAfterBomList.stream().filter(after -> (after.getOssName()).equals(ossName)).collect(Collectors.toList());
			
			if (afterList.size() == 0) {
				Map<String, String> deleteMap = new HashMap<String, String>();
				
				String beforeossname = before.getOssName();
				String version = avoidNull(before.getOssVersion(), "");
				
				if (!version.equals("")) {
					beforeossname += " (" + version + ")";
				}
				
				String beforelicense = before.getLicenseName();
				
				if (flag.equals("list")) {
					beforeossname = changeStyle("delete", beforeossname);
					beforelicense = changeStyle("delete", getLicenseNameSort(beforelicense.trim()));
				}
				
				deleteMap.put("status", "delete");
				deleteMap.put("beforeossname", beforeossname);
				deleteMap.put("beforelicense", beforelicense);
				deleteMap.put("afterossname", "");
				deleteMap.put("afterlicense", "");
				deleteList.put(getCompareKey(before), deleteMap);
				
				deletechk++;
			}
		}
		
		if (deletechk > 0) {
			for (String key : deleteList.keySet()) {
				returnList.add((Map<String, String>) deleteList.get(key));
			}
		}
		
		// status > change
		int changechk = 0;
		if (filteredBeforeBomList.size() > 0 && filteredAfterBomList.size() > 0) {
			List<ProjectIdentification> beforeBomResult = new ArrayList<ProjectIdentification>();
			
			int chk = 0;
			
			for (ProjectIdentification beforeBomCheckVO : filteredBeforeBomList) {
				ProjectIdentification addBeforeBomVO = new ProjectIdentification();
				addBeforeBomVO.setOssName(beforeBomCheckVO.getOssName());
				if (avoidNull(beforeBomCheckVO.getOssVersion(), "").equals("")) {
					addBeforeBomVO.setOssVersion(beforeBomCheckVO.getOssName());
				}else {
					addBeforeBomVO.setOssVersion(beforeBomCheckVO.getOssName() + "("+ beforeBomCheckVO.getOssVersion() + ")");
				}
				addBeforeBomVO.setLicenseName(getLicenseNameSort(avoidNull(beforeBomCheckVO.getLicenseName().trim(), "N/A")));
									
				if (beforeBomResult.size() > 0) {
					for (int j=0; j<beforeBomResult.size(); j++) {
						ProjectIdentification piVO = beforeBomResult.get(j);
						if (addBeforeBomVO.getOssName().equals(piVO.getOssName())) {
							ProjectIdentification changePIVO = new ProjectIdentification();
							changePIVO.setOssName(piVO.getOssName());
							changePIVO.setOssVersion(ossCheck("ossName", piVO.getOssVersion(), piVO.getLicenseName(), addBeforeBomVO.getOssVersion(), addBeforeBomVO.getLicenseName()));
							changePIVO.setLicenseName(ossCheck("licenseName", piVO.getOssVersion(), piVO.getLicenseName(), addBeforeBomVO.getOssVersion(), addBeforeBomVO.getLicenseName()));
							beforeBomResult.add(changePIVO);
							beforeBomResult.remove(j);
							chk++;
						}
					}
				}
				
				if (chk == 0) {
					beforeBomResult.add(addBeforeBomVO);
				}
				chk = 0;
			}
			
			List<ProjectIdentification> afterBomResult = new ArrayList<ProjectIdentification>();
			
			for (ProjectIdentification afterBomCheckVO : filteredAfterBomList) {
				ProjectIdentification addAfterBomVO = new ProjectIdentification();
				addAfterBomVO.setOssName(afterBomCheckVO.getOssName());
				if (avoidNull(afterBomCheckVO.getOssVersion(), "").equals("")) {
					addAfterBomVO.setOssVersion(afterBomCheckVO.getOssName());
				}else {
					addAfterBomVO.setOssVersion(afterBomCheckVO.getOssName() + "(" + avoidNull(afterBomCheckVO.getOssVersion(), "N/A") + ")");
				}
				addAfterBomVO.setLicenseName(getLicenseNameSort(avoidNull(afterBomCheckVO.getLicenseName().trim(), "N/A")));
				
				if (afterBomResult.size() > 0) {
					for (int j=0; j<afterBomResult.size(); j++) {
						ProjectIdentification piVO = afterBomResult.get(j);
						if (addAfterBomVO.getOssName().equals(piVO.getOssName())) {
							ProjectIdentification changePIVO = new ProjectIdentification();
							changePIVO.setOssName(addAfterBomVO.getOssName());
							changePIVO.setOssVersion(ossCheck("ossName", piVO.getOssVersion(), piVO.getLicenseName(), addAfterBomVO.getOssVersion(), addAfterBomVO.getLicenseName()));
							changePIVO.setLicenseName(ossCheck("licenseName", piVO.getOssVersion(), piVO.getLicenseName(), addAfterBomVO.getOssVersion(), addAfterBomVO.getLicenseName()));
							afterBomResult.add(changePIVO);
							afterBomResult.remove(j);
							chk++;
						}
					}
				}
				
				if (chk == 0) {
					afterBomResult.add(addAfterBomVO);
				}
				chk = 0;
			}
			
			for (ProjectIdentification beforeResult : beforeBomResult) {
				for (ProjectIdentification afterResult : afterBomResult) {
					if (beforeResult.getOssName().equals(afterResult.getOssName())) {
						if (!beforeResult.getOssVersion().equals(afterResult.getOssVersion()) || !beforeResult.getLicenseName().equals(afterResult.getLicenseName())) {
							Map<String, String> map = new HashMap<String, String>();
							map.put("status", "change");
							if (flag.equals("list")) {
								map.put("beforeossname", changeStyle("change", beforeResult.getOssVersion()));
								map.put("beforelicense", changeStyle("change", beforeResult.getLicenseName()));
								map.put("afterossname", changeStyle("change", afterResult.getOssVersion()));
								map.put("afterlicense", changeStyle("change", afterResult.getLicenseName()));
							}else {
								map.put("beforeossname", beforeResult.getOssVersion());
								map.put("beforelicense", beforeResult.getLicenseName());
								map.put("afterossname", afterResult.getOssVersion());
								map.put("afterlicense", afterResult.getLicenseName());
							}
							
							returnList.add(map);
							
							changechk++;
						}
					}
				}
			}
		}
		
		if (addchk == 0 && deletechk == 0 && changechk == 0) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("status", "Same");
			returnList.add(map);
		}
		
		return returnList;
	}


	private String getCompareKey(ProjectIdentification param) {
		return (param.getOssName() + "|" + avoidNull(param.getOssVersion(), "") + "|" + param.getLicenseName()).toLowerCase();
	}
	
	private static String changeStyle(String flag, String text) {
		String returnVal = "";
		
		if (flag.equals("delete")){
			returnVal = "<span style=\"background-color:#FFCCCC;\">" + text + "</span>";
		}else if (flag.equals("change") || flag.equals("add")){
			returnVal = "<span style=\"background-color:yellow\">" + text + "</span>";
		}

		return returnVal;
	}
	
	private String getLicenseNameSort(String licenseName) {
		String sortedValue = "";
		
		String splitLicenseNm[] = licenseName.split(",");
		Arrays.sort(splitLicenseNm);
		
		for (int i=0; i< splitLicenseNm.length; i++) {
			sortedValue += splitLicenseNm[i];
			if (i<splitLicenseNm.length-1) {
				sortedValue += ", ";
			}
		}
		
		return sortedValue;
	}
	
	private String ossCheck(String flag, String ossNameVersion, String ossLicenseName, String ossNameVersion2, String ossLicenseName2) {
String splitOssNameVersion[] = ossNameVersion.split("/");
		
		int count = splitOssNameVersion.length;
		
		if (flag.equals("ossName")) {
			int cnt = 0;
			
			for (int i=0; i<count; i++) {
				if (splitOssNameVersion[i].trim().equals(ossNameVersion2)) {
					cnt++;
				}
			}
			
			if (cnt > 0) {
				return ossNameVersion;
			} else {
				return ossNameVersion + " / " + ossNameVersion2;
			}
		} else {
			String licenseNmArr1[] = ossLicenseName.split(" / ");
			int chk = 0;
			boolean chkFlag = false;
			
			if (count == licenseNmArr1.length) {
				chkFlag = true;
				
				for (int i=0; i<count; i++) {
					if (splitOssNameVersion[i].trim().equals(ossNameVersion2)) {
						List<String> mergeList = new ArrayList<>();
						List<String> licenseNmChk1 = Arrays.asList(licenseNmArr1[i].split(","));
						List<String> licenseNmChk2 = Arrays.asList(ossLicenseName2.split(","));
						
						for (String chk1 : licenseNmChk1) {
							boolean equalsFlag = false;
							
							for (String chk2 : licenseNmChk2) {
								if (chk1.trim().equalsIgnoreCase(chk2.trim())) {
									equalsFlag = true;
									break;
								}
							}
							
							if (!equalsFlag) {
								for (String chk2 : licenseNmChk2) {
									mergeList.add(chk2.trim());
								}
								mergeList.add(chk1.trim());
							}
						}
						
						if (mergeList.size() > 0) {
							mergeList = mergeList.stream().distinct().collect(Collectors.toList());
							
							String str = "";
							for (int j=0; j<mergeList.size(); j++) {
								str += mergeList.get(j);
								if (j < mergeList.size()-1) {
									str += ", ";
								}
							}
							licenseNmArr1[i] = str;
							chk++;
						}
					}
				}
			}
			
			if (chk > 0 || chkFlag) {
				String strMerge = "";
				for (int i=0; i<licenseNmArr1.length; i++) {
					strMerge += licenseNmArr1[i];
					if (i<licenseNmArr1.length-1) {
						strMerge += " / ";
					}
				}
				
				int cnt = 0;
				for (int i=0; i<count; i++) {
					if (splitOssNameVersion[i].trim().equals(ossNameVersion2)) {
						cnt++;
					}
				}
				
				if (cnt == 0) {
					return strMerge + " / " + ossLicenseName2;
				} else {
					return strMerge;
				}
			} else {
				return ossLicenseName + " / " + ossLicenseName2;
			}
		}
	}

	@Override
	public void deleteStatisticsMostUsedInfo(Project project) {
		projectMapper.deleteStatisticsMostUsedInfo(project);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void addPartnerData(Project project) {
		{
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(project.getPrjId());
			projectSubStatus.setIdentificationSubStatusPartner(CoConstDef.FLAG_YES);
			
			// 최초 저장시에만 상태 변경
			// row count가 0이어도 사용자가 한번이라도 저장하면 progress 상태로 인지되어야함
			if (isEmpty(project.getIdentificationStatus())) {
				projectSubStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			}
			
			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			projectMapper.updateProjectMaster(projectSubStatus);
		}
		
		OssComponents component = new OssComponents();
		component.setReferenceId(project.getRefPartnerId());
		component.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
		
		// select partner Data
		Map<String, Object> resultMap = getPartnerOssList(component);
		List<OssComponents> partnerList = (List<OssComponents>) resultMap.get("rows");
		
		partnerList = convertOssNickName3rd(partnerList);
		
		// Identification > 3rd Party Tab Insert
		for (OssComponents bean : partnerList) {
			bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
			bean.setReferenceId(project.getPrjId());
			bean.setRefPartnerId(project.getRefPartnerId());
			
			projectMapper.insertOssComponentsCopy(bean);
			projectMapper.insertOssComponentsLicenseCopy(bean);
		}
		
		PartnerMaster partnerBean = new PartnerMaster();
		partnerBean.setPrjId(project.getPrjId());
		partnerBean.setPartnerId(project.getRefPartnerId());
		
		// project - partner Map Insert
		partnerMapper.insertPartnerMapList(partnerBean);
	}

	@Override
	public void insertCopyConfirmStatusBomList(Project project, ProjectIdentification identification) {
		List<ProjectIdentification> bomList = projectMapper.selectBomList(identification);
		Comparator<ProjectIdentification> compare = Comparator
				.comparing(ProjectIdentification::getLicenseTypeIdx)
				.thenComparing(ProjectIdentification::getOssName, Comparator.nullsFirst(Comparator.naturalOrder()))
				.thenComparing(ProjectIdentification::getOssVersion, (str1, str2) -> str2.compareTo(str1))
				.thenComparing(ProjectIdentification::getDownloadLocation, Comparator.reverseOrder())
				.thenComparing(ProjectIdentification::getLicenseName, Comparator.nullsFirst(Comparator.naturalOrder()))
				.thenComparing(ProjectIdentification::getHomepage, Comparator.naturalOrder())
				.thenComparing(ProjectIdentification::getMergeOrder);
		bomList.sort(compare);
		
		Map<String, List<OssComponentsLicense>> bomLicenseMap = new HashMap<>();
		List<OssComponentsLicense> bomLicenseList = projectMapper.selectBomLicenseList(identification);
		for (OssComponentsLicense ocl : bomLicenseList) {
			String key = ocl.getComponentId();
			List<OssComponentsLicense> bomLicenses = null;
			if (bomLicenseMap.containsKey(key)) {
				bomLicenses = bomLicenseMap.get(ocl.getComponentId());
			} else {
				bomLicenses = new ArrayList<>();
			}
			bomLicenses.add(ocl);
			bomLicenseMap.put(key, bomLicenses);
		}
		
		for (ProjectIdentification pi : bomList) {
			List<OssComponentsLicense> licenseList = null;
			pi.setReferenceId(project.getPrjId());
			
			if (bomLicenseMap.containsKey(pi.getComponentId())) {
				licenseList = bomLicenseMap.get(pi.getComponentId());
			}
			
			// 컴포넌트 마스터 인서트
			projectMapper.registBomComponents(pi);
			
			if (licenseList != null) {
				for (OssComponentsLicense licenseBean : licenseList) {
					licenseBean.setComponentId(pi.getComponentId());
					projectMapper.registComponentLicense(licenseBean);
				}
			}
			
		}
	}

	@Override
	public List<String> getPackageFileList(Project project, String filePath) {
		Project prj = getProjectBasicInfo(project.getCopyPrjId());
		List<String> fileSeqs = new ArrayList<>();
		
		if (!isEmpty(prj.getPackageFileId())) {
			List<UploadFile> uploadFile = fileService.setReusePackagingFile(prj.getPackageFileId());
			
			HashMap<String, Object> fileMap = new HashMap<>();
			fileMap.put("prjId", project.getPrjId());
			fileMap.put("refPrjId", project.getCopyPrjId());
			fileMap.put("refFileSeq", prj.getPackageFileId());
			fileMap.put("fileSeq", uploadFile.get(0).getRegistSeq());
			boolean reuseCheck = verificationService.setReusePackagingFile(fileMap);
			
			if (reuseCheck) {
				fileSeqs.add(uploadFile.get(0).getRegistSeq());
			}
			
			if (!isEmpty(prj.getPackageFileId2())) {
				List<UploadFile> file2 = fileService.setReusePackagingFile(prj.getPackageFileId2());
				fileMap.put("refFileSeq", prj.getPackageFileId2());
				fileMap.put("fileSeq", file2.get(0).getRegistSeq());
				reuseCheck = verificationService.setReusePackagingFile(fileMap);
				
				if (reuseCheck) {
					fileSeqs.add(file2.get(0).getRegistSeq());
				}
			}
			
			if (!isEmpty(prj.getPackageFileId3())) {
				List<UploadFile> file3 = fileService.setReusePackagingFile(prj.getPackageFileId3());
				fileMap.put("refFileSeq", prj.getPackageFileId3());
				fileMap.put("fileSeq", file3.get(0).getRegistSeq());
				reuseCheck = verificationService.setReusePackagingFile(fileMap);
				
				if (reuseCheck) {
					fileSeqs.add(file3.get(0).getRegistSeq());
				}
			}
			
			if (!isEmpty(prj.getPackageFileId4())) {
				List<UploadFile> file4 = fileService.setReusePackagingFile(prj.getPackageFileId4());
				fileMap.put("refFileSeq", prj.getPackageFileId4());
				fileMap.put("fileSeq", file4.get(0).getRegistSeq());
				reuseCheck = verificationService.setReusePackagingFile(fileMap);
				
				if (reuseCheck) {
					fileSeqs.add(file4.get(0).getRegistSeq());
				}
			}
			
			if (!isEmpty(prj.getPackageFileId5())) {
				List<UploadFile> file5 = fileService.setReusePackagingFile(prj.getPackageFileId5());
				fileMap.put("refFileSeq", prj.getPackageFileId5());
				fileMap.put("fileSeq", file5.get(0).getRegistSeq());
				reuseCheck = verificationService.setReusePackagingFile(fileMap);
				
				if (reuseCheck) {
					fileSeqs.add(file5.get(0).getRegistSeq());
				}
			}
		}
		
		return fileSeqs;
	}

	@Override
	public List<ProjectIdentification> selectIdentificationGridList(ProjectIdentification identification) {
		List<ProjectIdentification> list = projectMapper.selectIdentificationGridList(identification);
		list.sort(Comparator.comparing(ProjectIdentification::getComponentId));
		
		return list;
	}

	@Override
	public void updateCopyConfirmStatusProjectStatus(Project project) {
		projectMapper.updateCopyConfirmStatusProjectStatus(project);
	}

	@Override
	public void copySrcAndroidNoticeFile(Project project) {
		Project prj = getProjectBasicInfo(project.getCopyPrjId());
		
		if (!isEmpty(prj.getSrcAndroidNoticeFileId())) {
			String fileId = fileService.copyPhysicalFile(prj.getSrcAndroidNoticeFileId(), null, true);
			if (!isEmpty(fileId)) {
				project.setSrcAndroidNoticeFileId(fileId);
				projectMapper.updateFileId(project);
			}
		}
	}

	@Override
	public Map<String, List<Project>> updateProjectDivision(Project project) {
		Map<String, List<Project>> updateProjectDivMap = new HashMap<>();
		List<Project> beforeBeanList = new ArrayList<>();
		List<Project> afterBeanList = new ArrayList<>();
		
		Project param = new Project();
		String division = project.getPrjDivision();
		String comment = "";
		
		for (String prjId : project.getPrjIds()) {
			param.setPrjId(prjId);
			Project beforeBean = getProjectDetail(param);
			
			if (!avoidNull(beforeBean.getDivision(), "").equals(division)) {
				Project afterBean = getProjectDetail(param);
				afterBean.setDivision(division);
				
				projectMapper.updateProjectDivision(afterBean);
				
				comment = CommonFunction.getDiffItemComment(beforeBean, afterBean);
				
				afterBean.setUserComment(comment);
				
				Map<String, List<Project>> modelMap = getModelList(prjId);
				beforeBean.setModelList((List<Project>) modelMap.get("currentModelList"));
				afterBean.setModelList((List<Project>) modelMap.get("currentModelList"));
				
				if (afterBean.getWatcherList() != null && !afterBean.getWatcherList().isEmpty()) {
					List<String> prjWatchers = afterBean.getWatcherList().stream().map(e -> e.getPrjDivision() + "/" + e.getPrjUserId()).collect(Collectors.toList());
					afterBean.setWatchers(prjWatchers.toArray(new String[prjWatchers.size()]));
				}
				
				beforeBeanList.add(beforeBean);
				afterBeanList.add(afterBean);
			}
		}
		
		if (!beforeBeanList.isEmpty()) {
			updateProjectDivMap.put("before", beforeBeanList);
		}
		
		if (!afterBeanList.isEmpty()) {
			updateProjectDivMap.put("after", afterBeanList);
		}
		
		return updateProjectDivMap;
	}

	@Override
	public void updateComment(Project project) {
		projectMapper.updateComment(project);
	}

	@Override
	public String checkOssNicknameList(ProjectIdentification identification) {
		String referenceDivString = "";
		List<ProjectIdentification> bomList = projectMapper.selectBomList(identification);
		Comparator<ProjectIdentification> compare = Comparator
				.comparing(ProjectIdentification::getLicenseTypeIdx)
				.thenComparing(ProjectIdentification::getOssName, Comparator.nullsFirst(Comparator.naturalOrder()))
				.thenComparing(ProjectIdentification::getOssVersion, (str1, str2) -> str2.compareTo(str1))
				.thenComparing(ProjectIdentification::getDownloadLocation, Comparator.reverseOrder())
				.thenComparing(ProjectIdentification::getLicenseName, Comparator.nullsFirst(Comparator.naturalOrder()))
				.thenComparing(ProjectIdentification::getHomepage, Comparator.naturalOrder())
				.thenComparing(ProjectIdentification::getMergeOrder);
		bomList.sort(compare);
		
		if (bomList != null) {
			List<ProjectIdentification> checkList = bomList.stream()
																.filter(obj -> {
																	String ossName = (avoidNull(obj.getOssName())).toUpperCase();
																	String compareOssName = (avoidNull(CoCodeManager.OSS_INFO_UPPER_NAMES.get(obj.getOssName().toUpperCase()))).toUpperCase();
																	String referenceDiv = obj.getReferenceDiv();
																	return CoConstDef.FLAG_NO.equals(obj.getAdminCheckYn()) && !isEmpty(compareOssName) && !ossName.equals(compareOssName) && !CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(referenceDiv);
																}).collect(Collectors.toList());
			
			if (checkList.size() > 0) {
				for (ProjectIdentification row : checkList) {
					if (CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(row.getOssName().toUpperCase())) {
						switch(row.getReferenceDiv()) {
						case CoConstDef.CD_DTL_COMPONENT_ID_PARTNER : 
							if (!referenceDivString.contains("3rd")) {
								referenceDivString += "3rd" + ","; 
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_SRC : 
							if (!referenceDivString.contains("SRC")) {
								referenceDivString += "SRC" + ","; 
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_BIN : 
							if (!referenceDivString.contains("BIN")) {
								referenceDivString += "BIN" + ","; 
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID : 
							if (!referenceDivString.contains("ANDROID")) {
								referenceDivString += "ANDROID" + ","; 
							}
							break;
						}
					}
				}
				
				referenceDivString = referenceDivString.substring(0, referenceDivString.length()-1);
				return getMessage("msg.project.error.bom.usenickname", new String[]{referenceDivString});
			}
		}
		
		return null;
	}

	@Override
	public void deleteUploadFile(Project project) {
		Project prjInfo = projectMapper.getProjectBasicInfo(project);
		boolean fileDeleteCheckFlag = false;
		String physicalFilePath = "";
		T2File fileInfo = null;
		
		if (project.getReferenceDiv().equals(CoConstDef.CD_DTL_COMPONENT_ID_DEP) || project.getReferenceDiv().equals(CoConstDef.CD_DTL_COMPONENT_ID_SRC) 
				|| project.getReferenceDiv().equals(CoConstDef.CD_DTL_COMPONENT_ID_BIN) || project.getReferenceDiv().equals(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID)) {
			physicalFilePath = "Identification";
		}
		
		if (project.getCsvFile() != null) {
			for (int i = 0; i < project.getCsvFile().size(); i++) {
				if (i == 0) {
					fileInfo = fileService.selectFileInfo(project.getCsvFile().get(i).getFileSeq());
				}
				projectMapper.updateDeleteYNByFileSeq(project.getCsvFile().get(i));
				projectMapper.deleteFileBySeq(project.getCsvFile().get(i));
				fileService.deletePhysicalFile(project.getCsvFile().get(i), physicalFilePath);
			}
		}
		
		if (fileInfo != null) {
			T2File fileInfo2 = fileService.selectFileInfoById(fileInfo.getFileId());
			switch (project.getReferenceDiv()) {
			case CoConstDef.CD_DTL_COMPONENT_ID_DEP: 
				if (fileInfo2 == null && prjInfo.getDepCsvFileId() != null) {
					if (prjInfo.getDepCsvFileId().equals(fileInfo.getFileId())) {
						project.setDepCsvFileFlag(CoConstDef.FLAG_YES);
						fileDeleteCheckFlag = true;
					}
				}
				break;
			case CoConstDef.CD_DTL_COMPONENT_ID_SRC: 
				if (fileInfo2 == null && prjInfo.getSrcCsvFileId() != null) {
					if (prjInfo.getSrcCsvFileId().equals(fileInfo.getFileId())) {
						project.setSrcCsvFileFlag(CoConstDef.FLAG_YES);
						fileDeleteCheckFlag = true;
					}
				}
				break;
			case CoConstDef.CD_DTL_COMPONENT_ID_BIN: 
				if (fileInfo2 == null && prjInfo.getBinCsvFileId() != null) {
					if (prjInfo.getBinCsvFileId().equals(fileInfo.getFileId())) {
						project.setBinCsvFileFlag(CoConstDef.FLAG_YES);
						fileDeleteCheckFlag = true;
					}
				}
				
				if (fileInfo2 == null && prjInfo.getBinBinaryFileId() != null) {
					if (prjInfo.getBinBinaryFileId().equals(fileInfo.getFileId())) {
						project.setBinBinaryFileFlag(CoConstDef.FLAG_YES);
						fileDeleteCheckFlag = true;
					}
				}
				break;
			case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID: 
				if (fileInfo2 == null && prjInfo.getSrcAndroidCsvFileId() != null) {
					if (prjInfo.getSrcAndroidCsvFileId().equals(fileInfo.getFileId())) {
						project.setSrcAndroidCsvFileFlag(CoConstDef.FLAG_YES);
						fileDeleteCheckFlag = true;
					}
				}
				break;
			default :
				break;
			}
		}
		
		if (fileDeleteCheckFlag) {
			projectMapper.updateFileId2(project);
		}
	}
	
	@Override
	public Map<String, Object> getSecurityGridList(Project project) {
		Map<String, Object> rtnMap = new HashMap<>();
		List<OssComponents> totalList = new ArrayList<>();
		List<OssComponents> fullDiscoveredList = new ArrayList<>();
		Map<String, Object> securityGridMap = new HashMap<>();
		List<String> deduplicatedkey = new ArrayList<>();
		List<String> caseWithoutVersionKey = new ArrayList<>();
		List<String> checkOssNameList = new ArrayList<>();
		List<ProjectIdentification> list = null;
		List<ProjectIdentification> fullList = null;
		
		OssComponents oc = null;
		OssComponents bean = null;
		boolean activateFlag;
		String ossVersion = "";
		String vulnerabilityLink = "";
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(project.getPrjId());
		identification.setStandardScore(Float.valueOf(CoCodeManager.getCodeExpString(CoConstDef.CD_SECURITY_VULNERABILITY_SCORE, CoConstDef.CD_SECURITY_VULNERABILITY_DETAIL_SCORE)));
		
		Project prjInfo = projectMapper.selectProjectMaster2(project.getPrjId());
		if (!prjInfo.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
			identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
		} else {
			identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
		}
		
		list = projectMapper.selectSecurityListForProject(identification);
		identification.setStandardScore(Float.valueOf("0.1"));
		fullList = projectMapper.selectSecurityListForProject(identification);
		
		if (fullList != null && !fullList.isEmpty()) {
			List<OssComponents> securityDatalist = projectMapper.getSecurityDataList(identification);
			if (securityDatalist != null && !securityDatalist.isEmpty()) {
				for (OssComponents oss : securityDatalist) {
					String key = (oss.getOssName() + "_" + oss.getOssVersion() + "_" + oss.getCveId() + "_" + oss.getCvssScore()).toUpperCase();
					securityGridMap.put(key, oss);
				}
			}
			
			int gridIdx = 1;
			for (ProjectIdentification pi : fullList) {
				activateFlag = false;
				if (isEmpty(pi.getOssVersion())) {
					activateFlag = true;
					String keyWithoutVersion = (pi.getOssName() + "_" + pi.getOssVersion()).toUpperCase();
					if (!caseWithoutVersionKey.contains(keyWithoutVersion)) {
						caseWithoutVersionKey.add(keyWithoutVersion);
					} else {
						continue;
					}
				} 
					
				String key = (pi.getOssName() + "_" + pi.getOssVersion() + "_" + pi.getCveId() + "_" + pi.getCvssScore()).toUpperCase();
				
				if (!deduplicatedkey.contains(key)) {
					deduplicatedkey.add(key);
					
					if (securityGridMap.containsKey(key)) {
						bean = (OssComponents) securityGridMap.get(key);
					}
					
					if (activateFlag) {
						checkOssNameList.add(pi.getOssName());
						vulnerabilityLink = CommonFunction.getProperty("server.domain");
						vulnerabilityLink += "/vulnerability/vulnpopup?ossName=" + pi.getOssName() + "&ossVersion=" + ossVersion;
					} else {
						vulnerabilityLink = "https://nvd.nist.gov/vuln/detail/" + pi.getCveId();
					}
					
					oc = new OssComponents();
					oc.setGridId("jqg_sec_" + project.getPrjId() + "_" + String.valueOf(gridIdx));
					oc.setOssName(pi.getOssName());
					oc.setOssVersion(pi.getOssVersion());
					
					if (!activateFlag) {
						oc.setCveId(pi.getCveId());
						oc.setCvssScore(pi.getCvssScore());
						oc.setPublDate(pi.getPublDate());
					}
					
					oc.setActivateFlag(activateFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
					oc.setVulnerabilityLink(vulnerabilityLink);
					oc.setVulnerabilityResolution("Unresolved");
					
					if (bean != null) {
						oc.setVulnerabilityResolution(bean.getVulnerabilityResolution());
						oc.setSecurityComments(bean.getSecurityComments());
					}
							
					fullDiscoveredList.add(oc);
					
					bean = null;
					gridIdx++;
				}
			}
			
			if (list != null && !list.isEmpty()) {
				gridIdx = 1;
				caseWithoutVersionKey.clear();
				deduplicatedkey.clear();
				
				for (ProjectIdentification pi : list) {
					activateFlag = false;
					
					if (isEmpty(pi.getOssVersion())) {
						activateFlag = true;
						String keyWithoutVersion = (pi.getOssName() + "_" + pi.getOssVersion()).toUpperCase();
						if (!caseWithoutVersionKey.contains(keyWithoutVersion)) {
							caseWithoutVersionKey.add(keyWithoutVersion);
						} else {
							continue;
						}
					}
					
					String key = (pi.getOssName() + "_" + pi.getOssVersion() + "_" + pi.getCveId() + "_" + pi.getCvssScore()).toUpperCase();
					
					if (!deduplicatedkey.contains(key)) {
						deduplicatedkey.add(key);
						
						if (securityGridMap.containsKey(key)) {
							bean = (OssComponents) securityGridMap.get(key);
						}
						
						if (activateFlag) {
							checkOssNameList.add(pi.getOssName());
							vulnerabilityLink = CommonFunction.getProperty("server.domain");
							vulnerabilityLink += "/vulnerability/vulnpopup?ossName=" + pi.getOssName() + "&ossVersion=" + ossVersion;
						} else {
							vulnerabilityLink = "https://nvd.nist.gov/vuln/detail/" + pi.getCveId();
						}
						
						oc = new OssComponents();
						oc.setGridId("jqg_sec_" + project.getPrjId() + "_" + String.valueOf(gridIdx));
						oc.setOssId(pi.getOssId());
						oc.setOssName(pi.getOssName());
						oc.setOssVersion(pi.getOssVersion());
						
						if (!activateFlag) {
							oc.setCveId(pi.getCveId());
							oc.setCvssScore(pi.getCvssScore());
							oc.setPublDate(pi.getPublDate());
						}
						
						oc.setActivateFlag(activateFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
						oc.setVulnerabilityLink(vulnerabilityLink);
						oc.setVulnerabilityResolution("Unresolved");
						
						if (bean != null) {
							oc.setVulnerabilityResolution(bean.getVulnerabilityResolution());
							oc.setSecurityComments(bean.getSecurityComments());
						}
						
						totalList.add(oc);
						
						bean = null;
						gridIdx++;
					}
				}
			}
		}
		
		checkOssNameList = checkOssNameList.stream().distinct().collect(Collectors.toList());
		String warningMsg = getMessage("msg.project.security.check.version");
		boolean checkDataFlag = false;
		
		if (!checkOssNameList.isEmpty()) {
			checkDataFlag = true;
			warningMsg += "<br/><br/>";
			for (int i=0; i < checkOssNameList.size(); i++) {
				warningMsg += "- " + checkOssNameList.get(i);
				if (i < checkOssNameList.size()-1) {
					warningMsg += "<br/>";
				}
			}
		}
		
		if (checkDataFlag) {
			rtnMap.put("msg", warningMsg);
		}
		
		rtnMap.put("totalList", totalList);
		rtnMap.put("fullDiscoveredList", fullDiscoveredList);
		
		return rtnMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void registSecurity(Project project, String tabName, List<OssComponents> ossComponents) {
		Map<String, OssComponents> securityGridMap = new HashMap<>();
		List<OssComponents> deleteDataList = new ArrayList<>();
		
		String prjId = project.getPrjId();
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(prjId);
		identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
		identification.setMerge(CoConstDef.FLAG_NO);
		
		Map<String, Object> bomObj = getIdentificationGridList(identification);
		List<ProjectIdentification> bomList = (List<ProjectIdentification>) bomObj.get("rows");
		
		for (ProjectIdentification pi : bomList) {
			if (pi.getOssName().equals("-")) continue;
			List<OssComponents> securityDatalist = projectMapper.getSecurityDataList(pi);
			if (securityDatalist != null) {
				for (OssComponents oss : securityDatalist) {
					String ossVersion = oss.getOssVersion();
					if (ossVersion.equals("-")) {
						ossVersion = "";
					}
					String key = (oss.getOssName() + "_" + ossVersion + "_" + oss.getCveId()).toUpperCase();
					securityGridMap.put(key, oss);
				}
			}
		}
		
		for (OssComponents oc : ossComponents) {
			oc.setReferenceId(prjId);
			String ossVersion = oc.getOssVersion();
			if (ossVersion.equals("-")) {
				ossVersion = "";
			}
			String key = (oc.getOssName() + "_" + ossVersion + "_" + oc.getCveId()).toUpperCase();
			if (securityGridMap.containsKey(key)) {
				deleteDataList.add(oc);
			}
		}
		
		if (!deleteDataList.isEmpty() || (ossComponents != null && !ossComponents.isEmpty())) {
			try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
				 ProjectMapper mapper = sqlSession.getMapper(ProjectMapper.class);
				 
				 int cnt = 0;
				 for (OssComponents bean : deleteDataList) {
					 mapper.deleteSecurityData(bean);
					 if (cnt++ == 1000) {
						 sqlSession.flushStatements();
						 cnt = 0;
					 }
				 }
				 
				 if (cnt > 0) sqlSession.flushStatements();
				 
				 cnt = 0;
				 for (OssComponents bean : ossComponents) {
					 mapper.insertSecurityData(bean);
					 if (cnt++ == 1000) {
						 sqlSession.flushStatements();
						 cnt = 0;
					 }
				 }
				 
				 if (cnt > 0) {
					 sqlSession.flushStatements();
				 }
				 
				 sqlSession.commit();
			}
		}
		
		if (!isEmpty(project.getScrtCsvFileId())) {
			projectMapper.updateFileId(project);
			
			if (project.getCsvFileSeq() != null) {
				for (int i = 0; i < project.getCsvFileSeq().size(); i++) {
					projectMapper.updateFileBySeq(project.getCsvFileSeq().get(i));
				}				
			}
		}
	}
	
	@Override
	public Map<String, Object> getExportDataForSBOMInfo(OssNotice ossNotice) {
		Map<String, Object> model = new HashMap<String, Object>();
		
		String prjName = "";
		String prjVersion = "";
		String prjId = "";
		String distributeSite = "";
		int dashSeq = 0;
		
		Project project = new Project();
		project.setPrjId(ossNotice.getPrjId());
		
		project = projectMapper.getProjectBasicInfo(project);
		
		if (project != null){
			if (isEmpty(prjName)) {
				prjName = project.getPrjName();
			}
			
			if (isEmpty(prjId)) {
				prjId = project.getPrjId();
			}
			
			if (isEmpty(prjVersion)) {
				prjVersion = project.getPrjVersion();
			}
			
			if (isEmpty(distributeSite)) {
				distributeSite = project.getDistributeTarget();
			}
		}
		
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(ossNotice.getPrjId());
		identification.setReferenceDiv(!CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(ossNotice.getRefDiv()) ? CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM : CoConstDef.CD_DTL_COMPONENT_ID_DEP);
		
		List<OssComponents> ossComponentList = projectMapper.selectOssComponentsSbomList(identification);
		
		// TYPE별 구분
		Map<String, OssComponents> noticeInfo = new HashMap<>();
		Map<String, OssComponents> srcInfo = new HashMap<>();
		Map<String, OssComponents> notObligationInfo = new HashMap<>();
		
		OssMaster om = new OssMaster();
		OssComponents ossComponent;
		
		for (OssComponents bean : ossComponentList) {
			String componentKey = "";
			if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(identification.getReferenceDiv())) {
				if (isEmpty(bean.getOssName()) || isEmpty(bean.getLicenseName())) {
					continue;
				}
				componentKey = (bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
				if ("-".equals(bean.getOssName())) {
					componentKey += dashSeq++;
				}
			} else {
				if (isEmpty(bean.getOssName()) || isEmpty(bean.getLicenseName()) || isEmpty(bean.getPackageUrl())) {
					continue;
				}
				componentKey = bean.getPackageUrl().toUpperCase();
			}
			
			om.setOssNames(new String[] {bean.getOssName()});
			List<OssMaster> ossList = projectMapper.checkOssNickName(om);
			if (ossList != null && !ossList.isEmpty()) {
				om = ossList.get(0);
				OssMaster ossInfo = CoCodeManager.OSS_INFO_BY_ID.get(om.getOssId());
				if (ossInfo != null) {
					String copyright = ossInfo.getCopyright();
					String homepage = ossInfo.getHomepage();
					
					if (isEmpty(bean.getCopyrightText()) && !isEmpty(copyright)) {
						bean.setCopyrightText(copyright);
					}
					
					if (isEmpty(bean.getHomepage()) && !isEmpty(homepage)) {
						bean.setHomepage(homepage);
					}
				}
			}
			
			// type
			boolean isDisclosure = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType()) || CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY.equals(bean.getObligationType());
			boolean isNotice = CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType());
			
			if (CoConstDef.CD_DTL_NOTICE_TYPE_ACCOMPANIED.equals(ossNotice.getNoticeType())) {
				isDisclosure = true;
			}
			
			boolean addDisclosure = isDisclosure && srcInfo.containsKey(componentKey);
			boolean addNotice = !isDisclosure && noticeInfo.containsKey(componentKey);
			boolean addNotObligation = notObligationInfo.containsKey(componentKey);
			
			if (addDisclosure) {
				ossComponent = srcInfo.get(componentKey);
			} else if (addNotice) {
				ossComponent = noticeInfo.get(componentKey);
			} else if (addNotObligation) {
				ossComponent = notObligationInfo.get(componentKey);
			} else {
				ossComponent = bean;
			}
			
			// 라이선스 정보 생성
			OssComponentsLicense license = new OssComponentsLicense();
			license.setLicenseId(bean.getLicenseId());
			license.setLicenseName(bean.getLicenseName());
			license.setLicenseText(bean.getLicenseText());
			license.setAttribution(bean.getAttribution());
			
			// 하나의 oss에 대해서 동일한 LICENSE가 복수 표시되는 현상 
			// 일단 여기서 막는다. (쿼리가 잘못된 건지, DATA가 꼬이는건지 모르겠음)
			if (!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
				ossComponent.addOssComponentsLicense(license);
				
				if (CoConstDef.FLAG_NO.equals(ossComponent.getAdminCheckYn())) {
					bean.setOssCopyright(findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright()));
					
					// multi license 추가 copyright
					if (!isEmpty(bean.getOssCopyright())) {
						String addCopyright = avoidNull(ossComponent.getCopyrightText());
						
						if (!isEmpty(ossComponent.getCopyrightText())) {
							addCopyright += "\r\n";
						}
						 
						addCopyright += bean.getOssCopyright();
						ossComponent.setCopyrightText(addCopyright);
					}
				}
			}
			
			if (isDisclosure) {
				if (addDisclosure) {
					srcInfo.replace(componentKey, ossComponent);
				} else {
					srcInfo.put(componentKey, ossComponent);
				}
			} else if (isNotice) {
				if (addNotice) {
					noticeInfo.replace(componentKey, ossComponent);
				} else {
					noticeInfo.put(componentKey, ossComponent);
				}
			} else {
				if (addNotObligation) {
					notObligationInfo.replace(componentKey, ossComponent);
				} else {
					notObligationInfo.put(componentKey, ossComponent);
				}
			}
		}
		
		List<OssComponents> addOssComponentList = projectMapper.selectOssComponentsListClassAppend(identification);
		
		if (addOssComponentList != null) {
			for (OssComponents bean : addOssComponentList) {
				String componentKey = "";
				if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(identification.getReferenceDiv())) {
					if (isEmpty(bean.getLicenseName())) {
						continue;
					}
					componentKey = (bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
					if ("-".equals(bean.getOssName())) {
						componentKey += dashSeq++;
					}
				} else {
					if (isEmpty(bean.getLicenseName()) || isEmpty(bean.getPackageUrl())) {
						continue;
					}
					componentKey = bean.getPackageUrl().toUpperCase();
				}
				
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());
				
				if (!checkLicenseDuplicated(bean.getOssComponentsLicense(), license)) {
					bean.addOssComponentsLicense(license);
				}
				
				boolean disclosureFlag = false;
				boolean noticeFlag = false;
				
				switch(CommonFunction.checkObligationSelectedLicense(bean.getOssComponentsLicense())){
					case CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE: 
						srcInfo.put(componentKey, bean);
						disclosureFlag = true;
						break;
					case CoConstDef.CD_DTL_OBLIGATION_NOTICE: 
						noticeInfo.put(componentKey, bean);
						noticeFlag = true;
						break;
				}
				
				if (!disclosureFlag && !noticeFlag) {
					notObligationInfo.put(componentKey, bean);
				}
			}
		}
		
		boolean isTextNotice = "text".equals(ossNotice.getFileType());
		
		Map<String, String> ossAttributionMap = new HashMap<>();
		// 개행처리 및 velocity용 list 생성
		List<OssComponents> noticeList = new ArrayList<>();
		
		for (OssComponents bean : noticeInfo.values()) {
			if (isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getOssAttribution()))));
			}

			if (!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if (!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			noticeList.add(bean);
		}
		
		Collections.sort(noticeList, new Comparator<OssComponents>() {
			@Override
			public int compare(OssComponents oc1, OssComponents oc2) {
				return oc1.getOssName().toUpperCase().compareTo(oc2.getOssName().toUpperCase());
			}
		});
		
		List<OssComponents> srcList = new ArrayList<>();
		
		for (OssComponents bean : srcInfo.values()) {
			if (isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getOssAttribution()))));
			}
			

			if (!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if (!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			srcList.add(bean);
		}
		
		Collections.sort(srcList, new Comparator<OssComponents>() {
			@Override
			public int compare(OssComponents oc1, OssComponents oc2) {
				return oc1.getOssName().toUpperCase().compareTo(oc2.getOssName().toUpperCase());
			}
		});
		
		List<OssComponents> notObligationList = new ArrayList<>();
		
		for (OssComponents bean : notObligationInfo.values()) {
			if (isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getOssAttribution()))));
			}
			

			if (!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if (!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			notObligationList.add(bean);
		}
		
		Collections.sort(notObligationList, new Comparator<OssComponents>() {
			@Override
			public int compare(OssComponents oc1, OssComponents oc2) {
				return oc1.getOssName().toUpperCase().compareTo(oc2.getOssName().toUpperCase());
			}
		});
		
		model.put("noticeObligationList", noticeList);
		model.put("disclosureObligationList", srcList);
		model.put("notObligationList", notObligationList);
		model.put("addOssComponentList", addOssComponentList);
		
		return model;
	}

	private boolean checkLicenseDuplicated(List<OssComponentsLicense> ossComponentsLicense, OssComponentsLicense license) {
		if (ossComponentsLicense != null) {
			if (!isEmpty(license.getLicenseId())) {
				for (OssComponentsLicense bean : ossComponentsLicense) {
					if (bean.getLicenseId().equals(license.getLicenseId())) {
						return true;
					}
				}
			} else if (isEmpty(license.getLicenseId()) && !isEmpty(license.getLicenseName())) {
				for (OssComponentsLicense bean : ossComponentsLicense) {
					if (bean.getLicenseName().equals(license.getLicenseName())) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkReqEntrySecurity(Project project, String tabMenu) {
		boolean reqEntryFlag = false;
		Map<String, Object> result = getSecurityGridList(project);
		
		switch (tabMenu) {
		case "fixed" : 
			if (result.containsKey("fixedList")) {
				List<OssComponents> fixedList = (List<OssComponents>) result.get("fixedList");
				fixedList = fixedList.stream().filter(e -> e.getOssVersion().isEmpty()
															|| (e.getVulnerabilityResolution().equals("Fixed") && isEmpty(avoidNull(e.getSecurityPatchLink())))
															|| (!e.getVulnerabilityResolution().equals("Fixed") && !e.getVulnerabilityResolution().equals("Unresolved") && isEmpty(avoidNull(e.getSecurityComments())))
															|| e.getVulnerabilityResolution().equals("Unresolved")).collect(Collectors.toList());
				if (!fixedList.isEmpty()) {
					reqEntryFlag = true;
				}
			}
			break;
		case "notfixed" : 
			if (result.containsKey("notFixedList")) {
				List<OssComponents> notFixedList = (List<OssComponents>) result.get("notFixedList");
				notFixedList = notFixedList.stream().filter(e -> isEmpty(avoidNull(e.getOssVersion()))
															|| (e.getVulnerabilityResolution().equals("Fixed") && isEmpty(avoidNull(e.getSecurityPatchLink())))
															|| (!e.getVulnerabilityResolution().equals("Fixed") && !e.getVulnerabilityResolution().equals("Unresolved") && isEmpty(avoidNull(e.getSecurityComments())))
															|| e.getVulnerabilityResolution().equals("Unresolved")).collect(Collectors.toList());
				if (!notFixedList.isEmpty()) {
					reqEntryFlag = true;
				}
			}
			break;
		default : 
			if (result.containsKey("totalList")) {
				List<OssComponents> totalList = (List<OssComponents>) result.get("totalList");
				List<OssComponents> filteredTotalList = totalList.stream().filter(e -> e.getOssVersion().isEmpty()
															|| (e.getVulnerabilityResolution().equals("Fixed") && isEmpty(avoidNull(e.getSecurityPatchLink())))
															|| (!e.getVulnerabilityResolution().equals("Fixed") && !e.getVulnerabilityResolution().equals("Unresolved") && isEmpty(avoidNull(e.getSecurityComments())))
															|| e.getVulnerabilityResolution().equals("Unresolved")).collect(Collectors.toList());
				if (!filteredTotalList.isEmpty()) {
					reqEntryFlag = true;
				}
			}
			break;
		}
		
		return reqEntryFlag;
	}

	@Override
	public void copySecurityDataForProject(Project project, Project bean) {
		boolean copyFlag = projectMapper.copySecurityDataForProjectCnt(project) > 0 ? true : false;
		if (copyFlag) {
			projectMapper.copySecurityDataForProject(project);
		}
		
		project.setCvssScoreMax(bean.getCvssScoreMax());
		project.setVulnerabilityResolution(bean.getVulnerabilityResolution());
		projectMapper.updateProjectForSecurity(project);
	}
	
	@Override
	public Map<String, Object> checkSelectDownloadFile(Project project) {
		Map<String, Object> resMap = new HashMap<>();
		List<String> overMaxLengthOssList = new ArrayList<>();
		boolean emptyCheckFlag = false;
		
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(project.getReferenceDiv()) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(project.getReferenceDiv())) {
			List<ProjectIdentification> list = projectMapper.checkSelectDownloadFileForBOM(project);
			if (list != null) {
				for (ProjectIdentification bean : list) {
					if (!isEmpty(bean.getCopyright()) && bean.getCopyright().length() > 32767 && !isEmpty(bean.getOssName())) {
						String key = bean.getOssName() + " (" + avoidNull(bean.getOssVersion(), "N/A") + ")";
						if (!overMaxLengthOssList.contains(key)) {
							overMaxLengthOssList.add(key);
						}
					}
					if (!bean.getLicenseTypeIdx().equals("1")) {
						continue;
					}
					if (isEmpty(bean.getOssName()) || isEmpty(bean.getLicenseName())) {
						emptyCheckFlag = true;
					}
				}
			}
		} else {
			List<OssComponents> list = projectMapper.checkSelectDownloadFile(project);
			if (list != null) {
				for (OssComponents oss : list) {
					if (!isEmpty(oss.getCopyrightText()) && oss.getCopyrightText().length() > 32767 && !isEmpty(oss.getOssName())) {
						String key = oss.getOssName() + " (" + avoidNull(oss.getOssVersion(), "N/A") + ")";
						if (!overMaxLengthOssList.contains(key)) {
							overMaxLengthOssList.add(key);
						}
					}
					if (isEmpty(oss.getOssName()) || isEmpty(oss.getLicenseName())) {
						emptyCheckFlag = true;
					}
				}
			}
		}
		
		if (emptyCheckFlag) {
			resMap.put("isValid", false);
		} else {
			resMap.put("isValid", true);
		}
		if (!overMaxLengthOssList.isEmpty()) {
			resMap.put("overMaxLengthOssList", overMaxLengthOssList);
		}
		
		return resMap;
	}

	@Override
	public List<OssComponents> getDependenciesDataList(Project project) {
		return projectMapper.getDependenciesDataList(project);
	}

	@Override
	public void setNoticeFileFormat(Project project, List<String> noticeFileFormatList) {
		if (noticeFileFormatList.contains("chkAllowDownloadNoticeHTML")) {
			project.setAllowDownloadNoticeHTMLYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadNoticeHTMLYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadNoticeText")) {
			project.setAllowDownloadNoticeTextYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadNoticeTextYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadSimpleHTML")) {
			project.setAllowDownloadSimpleHTMLYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadSimpleHTMLYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadSimpleText")) {
			project.setAllowDownloadSimpleTextYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadSimpleTextYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadSPDXSheet")) {
			project.setAllowDownloadSPDXSheetYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadSPDXSheetYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadSPDXRdf")) {
			project.setAllowDownloadSPDXRdfYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadSPDXRdfYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadSPDXTag")) {
			project.setAllowDownloadSPDXTagYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadSPDXTagYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadSPDXJson")) {
			project.setAllowDownloadSPDXJsonYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadSPDXJsonYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadSPDXYaml")) {
			project.setAllowDownloadSPDXYamlYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadSPDXYamlYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadCDXJson")) {
			project.setAllowDownloadCDXJsonYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadCDXJsonYn(CoConstDef.FLAG_NO);
		}
		if (noticeFileFormatList.contains("chkAllowDownloadCDXXml")) {
			project.setAllowDownloadCDXXmlYn(CoConstDef.FLAG_YES);
		} else {
			project.setAllowDownloadCDXXmlYn(CoConstDef.FLAG_NO);
		}
	}

	@Override
	public void updateSecurityDataForProject(String prjId) {
		// check project notice
		Project project = new Project();
		project.setPrjId(prjId);
		project = getProjectDetail(project);
		project.setStandardScore(Float.valueOf(CoCodeManager.getCodeExpString(CoConstDef.CD_VULNERABILITY_MAILING_SCORE, CoConstDef.CD_VULNERABILITY_MAILING_SCORE_STANDARD)));
		
		if (CoConstDef.FLAG_NO.equals(project.getAndroidFlag())) {
			project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
		} else {
			project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
		}
		
		// set vulnerability resolution
		checkIfVulnerabilityResolutionIsFixed(project);
		
		if (!CoConstDef.FLAG_NO.equals(project.getSecCode())) {
			if (project.getSecCode().equals("Fixed")) {
				project.setVulnerabilityResolution("Resolved");
				if (CoConstDef.FLAG_NO.equals(project.getCvssScore())) {
					project.setCvssScoreMax("N/A");
				} else {
					project.setCvssScoreMax(project.getCvssScore());
				}
			} else {
				if (!project.getCvssScore().equals("0") && !project.getCvssScore().equals("N")) {
					if (new BigDecimal(project.getCvssScore()).compareTo(new BigDecimal(project.getStandardScore())) > 0) {
						project.setVulnerabilityResolution("Need to resolve");
					} else {
						project.setVulnerabilityResolution("Discovered");
					}
					project.setCvssScoreMax(project.getCvssScore());
				} else {
					project.setVulnerabilityResolution("Discovered");
					project.setCvssScoreMax("N/A");
				}
			}
		} else {
			project.setVulnerabilityResolution("Discovered");
			if (!project.getCvssScore().equals("0") && !project.getCvssScore().equals("N")) {
				project.setCvssScoreMax(project.getCvssScore());
			} else {
				project.setCvssScoreMax("N/A");
			}
		}
		
		// update security data for project
		projectMapper.updateProjectForSecurity(project);
	}

	@Override
	public void updatePreparedStatement(List<ProjectIdentification> updateOssComponentList, List<ProjectIdentification> insertOssComponentList, List<OssComponentsLicense> insertOssComponentLicenseList, List<String> deleteRows) {
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		
		String query = "";
		
		try{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
			conn.setAutoCommit(false);
			
			int idx = 1;
			if (updateOssComponentList != null && updateOssComponentList.size() > 0) {
				query = "UPDATE OSS_COMPONENTS SET OSS_ID = ?, OSS_NAME = TRIM(?), OSS_VERSION = TRIM(REPLACE(?, 'N/A','')), DOWNLOAD_LOCATION = ?, HOMEPAGE = ?, FILE_PATH = ?, EXCLUDE_YN = ?, BINARY_NAME = ?, BINARY_NOTICE = ?, CUSTOM_BINARY_YN = ?, COPYRIGHT = ?,"
						+ " OBLIGATION_TYPE = ?, COMMENTS = ?, DEPENDENCIES = ?, REF_OSS_NAME = ?, PACKAGE_URL = ?"
						+ " WHERE COMPONENT_ID = ?;";
				stmt = conn.prepareStatement(query);
				
				for (ProjectIdentification item : updateOssComponentList) {
					stmt.setString(1, item.getOssId());
					stmt.setString(2, item.getOssName());
					stmt.setString(3, item.getOssVersion());
					stmt.setString(4, item.getDownloadLocation());
					stmt.setString(5, item.getHomepage());
					stmt.setString(6, item.getFilePath());
					stmt.setString(7, item.getExcludeYn());
					stmt.setString(8, item.getBinaryName());
					stmt.setString(9, item.getBinaryNotice());
					stmt.setString(10, item.getCustomBinaryYn());
					stmt.setString(11, item.getCopyrightText());
					stmt.setString(12, isEmpty(item.getObligationType()) ? null : item.getObligationType());
					stmt.setString(13, item.getComments());
					stmt.setString(14, isEmpty(item.getDependencies()) ? null : item.getDependencies());
					stmt.setString(15, isEmpty(item.getRefOssName()) ? null : item.getRefOssName());
					stmt.setString(16, isEmpty(item.getPackageUrl()) ? null : item.getPackageUrl());
					stmt.setString(17, item.getComponentId());
					stmt.addBatch();
					stmt.clearParameters();
					
					if ((idx % 1000) == 0) {
						stmt.executeBatch();
						stmt.clearBatch();
			            conn.commit();
					}
					
					idx++;
				}

				stmt.executeBatch() ;
				conn.commit();
			}
			
			Map<String, String> componentIdMap = new HashMap<>();
			Map<String, String> gridMap = new HashMap<>();
			
			if (insertOssComponentList != null && insertOssComponentList.size() > 0) {
				query = "INSERT INTO OSS_COMPONENTS (REFERENCE_ID, REFERENCE_DIV, COMPONENT_IDX, OSS_ID, OSS_NAME, OSS_VERSION, DOWNLOAD_LOCATION, HOMEPAGE, FILE_PATH, EXCLUDE_YN, COPYRIGHT, BINARY_NAME, BINARY_SIZE, BINARY_NOTICE, CUSTOM_BINARY_YN, REF_PARTNER_ID"
						+ ", REF_PRJ_ID, REF_BAT_ID, REF_COMPONENT_ID, REPORT_FILE_ID, BAT_STRING_MATCH_PERCENTAGE, BAT_PERCENTAGE, BAT_SCORE, OBLIGATION_TYPE, COMMENTS, DEPENDENCIES, REF_OSS_NAME, TLSH, CHECK_SUM, PACKAGE_URL) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
				stmt2 = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
				
				idx = 1;
				int rsIdx = 1;
				
				for (ProjectIdentification item : insertOssComponentList) {
					stmt2.setString(1, item.getReferenceId());
					stmt2.setString(2, item.getReferenceDiv());
					stmt2.setString(3, item.getComponentIdx());
					stmt2.setString(4, isEmpty(item.getOssId()) ? null : item.getOssId());
					stmt2.setString(5, item.getOssName().trim());
					stmt2.setString(6, item.getOssVersion().equals("N/A") ? "" : item.getOssVersion().trim());
					stmt2.setString(7, item.getDownloadLocation());
					stmt2.setString(8, item.getHomepage());
					stmt2.setString(9, item.getFilePath());
					stmt2.setString(10, item.getExcludeYn());
					stmt2.setString(11, item.getCopyrightText());
					stmt2.setString(12, isEmpty(item.getBinaryName()) ? null : item.getBinaryName());
					stmt2.setString(13, isEmpty(item.getBinarySize()) ? null : item.getBinarySize());
					stmt2.setString(14, isEmpty(item.getBinaryNotice()) ? null : item.getBinaryNotice());
					stmt2.setString(15, isEmpty(item.getCustomBinaryYn()) ? null : item.getCustomBinaryYn());
					stmt2.setString(16, isEmpty(item.getRefPartnerId()) ? null : item.getRefPartnerId());
					stmt2.setString(17, isEmpty(item.getRefPrjId()) ? null : item.getRefPrjId());
					stmt2.setString(18, isEmpty(item.getRefBatId()) ? null : item.getRefBatId());
					stmt2.setString(19, isEmpty(item.getRefComponentId()) ? null : item.getRefComponentId());
					stmt2.setString(20, isEmpty(item.getReportFileId()) ? null : item.getReportFileId());
					stmt2.setString(21, isEmpty(item.getBatStringMatchPercentage()) ? null : item.getBatStringMatchPercentage());
					stmt2.setString(22, isEmpty(item.getBatPercentage()) ? null : item.getBatPercentage());
					stmt2.setString(23, isEmpty(item.getBatScore()) ? null : item.getBatScore());
					stmt2.setString(24, isEmpty(item.getObligationType()) ? null : item.getObligationType());
					stmt2.setString(25, isEmpty(item.getComments()) ? null : item.getComments());
					stmt2.setString(26, isEmpty(item.getDependencies()) ? null : item.getDependencies());
					stmt2.setString(27, isEmpty(item.getRefOssName()) ? null : item.getRefOssName());
					stmt2.setString(28, isEmpty(item.getTlsh()) ? null : item.getTlsh());
					stmt2.setString(29, isEmpty(item.getCheckSum()) ? null : item.getCheckSum());
					stmt2.setString(30, isEmpty(item.getPackageUrl()) ? null : item.getPackageUrl());
					stmt2.addBatch();
					gridMap.put(String.valueOf(idx), item.getGridId());
					
					if ((idx % 1000) == 0) {
						stmt2.executeBatch();
						ResultSet rs = stmt2.getGeneratedKeys();
						while (rs.next()) {
							int componentId = rs.getInt(1);
							componentIdMap.put(String.valueOf(rsIdx), String.valueOf(componentId));
							deleteRows.add(String.valueOf(componentId));
							rsIdx++;
						}
						
						stmt2.clearParameters();
						stmt2.clearBatch();
			            conn.commit();
					}
					
					idx++;
				}

				stmt2.executeBatch();
				
				ResultSet rs = stmt2.getGeneratedKeys();
				while (rs.next()) {
					int componentId = rs.getInt(1);
					componentIdMap.put(String.valueOf(rsIdx), String.valueOf(componentId));
					deleteRows.add(String.valueOf(componentId));
					rsIdx++;
				}
				
				stmt2.clearParameters();
				stmt2.clearBatch();
				
				conn.commit();
			}

			
			if (insertOssComponentLicenseList != null && insertOssComponentLicenseList.size() > 0) {
				Map<String, String> ossComponentIdMap = new HashMap<>();
				for (String key : gridMap.keySet()) {
					ossComponentIdMap.put(gridMap.get(key), componentIdMap.get(key));
				}
				
				query = "INSERT INTO OSS_COMPONENTS_LICENSE (COMPONENT_ID, LICENSE_ID, LICENSE_NAME, COPYRIGHT_TEXT, EXCLUDE_YN) VALUES (?,?,?,?,?);";
				stmt3 = conn.prepareStatement(query);
				
				idx = 1;
				for (OssComponentsLicense item : insertOssComponentLicenseList) {
					String componentId = item.getComponentId();
					if (componentId.contains(CoConstDef.GRID_NEWROW_DEFAULT_PREFIX) && ossComponentIdMap.containsKey(componentId)) {
						componentId = ossComponentIdMap.get(componentId);
					}
					
					stmt3.setString(1, componentId);
					stmt3.setString(2, item.getLicenseId());
					stmt3.setString(3, item.getLicenseName().trim());
					stmt3.setString(4, item.getCopyrightText());
					stmt3.setString(5, item.getExcludeYn());
					stmt3.addBatch();
					stmt3.clearParameters();
					
					if ((idx % 1000) == 0) {
						stmt3.executeBatch();
						stmt3.clearBatch();
			            conn.commit();
					}
					
					idx++;
				}

				stmt3.executeBatch() ;
				conn.commit();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try{
				if (stmt != null) {
					stmt.close();
				}
			} catch(SQLException se) {}
			
			try{
				if (stmt2 != null) {
					stmt2.close();
				}
			} catch(SQLException se) {}
			
			try{
				if (stmt3 != null) {
					stmt3.close();
				}
			} catch(SQLException se) {}
			
			if (conn != null) {
				try {
					conn.rollback();
					conn.close();
				} catch (Exception e2) {
					log.error(e2.getMessage(), e2);
				}
			}
		} finally {
			try{
				if (stmt != null) {
					stmt.close();
				}
			} catch(SQLException e) {}
			
			try {
				if (stmt2 != null) {
					stmt2.close();
				}
			} catch(SQLException e) {}
			
			try{
				if (stmt3 != null) {
					stmt3.close();
				}
			} catch(SQLException e) {}
			
			try{
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {}
		}
	}
	
	public void deletePreparedStatement(List<OssComponents> componentIdList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String query = "DELETE FROM OSS_COMPONENTS_LICENSE WHERE COMPONENT_ID = ?";
		
		try{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
			conn.setAutoCommit(false);
			
			stmt = conn.prepareStatement(query);
			
			int idx = 1;
			for (OssComponents item : componentIdList) {
				stmt.setString(1, item.getComponentId());
				stmt.addBatch();
				stmt.clearParameters();
				
				if ((idx % 1000) == 0) {
					stmt.executeBatch();
					stmt.clearBatch();
					conn.commit();
				}
				
				idx++;
			}

			stmt.executeBatch() ;
			conn.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try{
				if (stmt != null) {
					stmt.close();
				}
			} catch(SQLException e1) {}
			
			if (conn != null) {
				try {
					conn.rollback();
					conn.close();
				} catch (Exception e2) {
					log.error(e2.getMessage(), e2);
				}
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch(SQLException e) {}
			
			try{
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setLoadToList(Map<String, Object> map, String prjId) {
		List<OssComponents> rows = (List<OssComponents>) map.get("rows");
		int idx = 1;
		
		for (OssComponents row : rows) {
			row.setGridId("jqg" + String.valueOf(idx));
			row.setComponentId(null);
			row.setRefPartnerId(row.getReferenceId());
			row.setReferenceId(prjId);
			row.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
			
			idx++;
		}
		
		map.put("rows", rows);
	}
	
	@Override
	public Map<String, Object> changeProjectStatus(Project project) {
		Map<String, Object> resultMap = new HashMap<>();
		String resCd = "";
		
		try {
			resultMap = updateProjectStatus(project, false, false);
			if (resultMap.containsKey("androidMessage") || resultMap.containsKey("diffMap") || resultMap.containsKey("validMap")) {
				resCd = "20";
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			resCd = "20";
		}
		
		resultMap.put("resCd", resCd);
		return resultMap;
	}

	@Override
	public Map<String, Object> getDependencyTreeList(List<ProjectIdentification> ossComponents) {
		List<ProjectIdentificationTree> rtnDepTreeList = new LinkedList<>();
		List<ProjectIdentificationTree> depTreeList = new ArrayList<>();
		Map<String, String> packageUrlInfo = new HashMap<>();
		
		int length = 8;
		String treeId = "";
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		
		for (ProjectIdentification bean : ossComponents) {
			if (!isEmpty(bean.getPackageUrl())) {
				packageUrlInfo.put(bean.getPackageUrl(), !isEmpty(bean.getDependencies()) ? bean.getDependencies() : "");
			}
		}
		
		int level = 0;
		for (ProjectIdentification bean : ossComponents) {
			if (!isEmpty(bean.getPackageUrl()) && avoidNull(bean.getComments()).trim().contains("direct")) {
				for (int i = 0; i < length; i++) {
					int index = random.nextInt(characters.length());
					sb.append(characters.charAt(index));        
				}
				treeId = sb.toString();
				sb = new StringBuilder();
				
				String dependencies = !isEmpty(bean.getDependencies()) ? bean.getDependencies() : "";
				ProjectIdentificationTree tree = new ProjectIdentificationTree(treeId, "", String.valueOf(level), bean.getPackageUrl(), dependencies, avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO));
				if (!isEmpty(dependencies)) {
					tree.setExistDependency(true);
					depTreeList.add(tree);
				} else {
					tree.setExistDependency(false);
				}
				rtnDepTreeList.add(tree);
			}
		}
		
		collectDependencyTreeData(level, depTreeList, rtnDepTreeList, packageUrlInfo);
		Map<String, Object> rtnDepTreeMap = generateDependencyTreeMap(rtnDepTreeList);
		return rtnDepTreeMap;
	}

	private void collectDependencyTreeData(int level, List<ProjectIdentificationTree> depTreeList, List<ProjectIdentificationTree> rtnDepTreeList, Map<String, String> packageUrlInfo) {
		int lvl = level+1;
		int length = 8;
		String treeId = "";
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		List<ProjectIdentificationTree> addDepTreeList = new ArrayList<>();
		
		for (ProjectIdentificationTree depTree : depTreeList) {
			if (!isEmpty(depTree.getDependencies())) {
				for (String packageUrl : depTree.getDependencies().split(",")) {
					for (int i = 0; i < length; i++) {
						int index = random.nextInt(characters.length());
						sb.append(characters.charAt(index));        
					}
					treeId = sb.toString();
					sb = new StringBuilder();
					
					String dependencies = !isEmpty(packageUrlInfo.get(packageUrl)) ? packageUrlInfo.get(packageUrl) : ""; 
					ProjectIdentificationTree tree = new ProjectIdentificationTree(treeId, depTree.getTreeId(), String.valueOf(lvl), packageUrl, dependencies, depTree.getExcludeYn());
					if (!isEmpty(dependencies)) {
						tree.setExistDependency(true);
						if (lvl <= 4) {
							addDepTreeList.add(tree);
						} else {
							tree.setPackageUrl(tree.getPackageUrl() + " ...");
						}
					} else {
						tree.setExistDependency(false);
					}
					rtnDepTreeList.add(tree);
				}
			}
		}
		
		if (lvl <= 4) {
			collectDependencyTreeData(lvl, addDepTreeList, rtnDepTreeList, packageUrlInfo);
		}
	}
	
	private Map<String, Object> generateDependencyTreeMap(List<ProjectIdentificationTree> rtnDepTreeList) {
		Map<String, Object> rtnTreeMap = new HashMap<>();
		if (!CollectionUtils.isEmpty(rtnDepTreeList)) {
			List<Integer> levelList = rtnDepTreeList.stream().distinct().map(e -> Integer.parseInt(e.getLevel())).collect(Collectors.toList());
			Collections.sort(levelList, Collections.reverseOrder());
			int maxLvl = levelList.get(0);
			
			rtnTreeMap.put("maxLvl", maxLvl);
			for (int i=0; i<=maxLvl; i++) {
				String lvl = String.valueOf(i);
				List<ProjectIdentificationTree> lvlList = rtnDepTreeList.stream().filter(e -> e.getLevel().equals(lvl)).collect(Collectors.toList());
				rtnTreeMap.put("lvl_" + lvl , lvlList);
			}
		}
		
		return rtnTreeMap;
	}
}
