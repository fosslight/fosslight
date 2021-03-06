/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Functions;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
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
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.FileService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.VerificationService;
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
	@Autowired OssService ossService;
	@Autowired VerificationService verificationService;
	@Autowired FileService fileService;
	
	// Mapper
	@Autowired ProjectMapper projectMapper;
	@Autowired T2UserMapper userMapper;
	@Autowired PartnerMapper partnerMapper;
	
	@Override
	@Cacheable(value="autocompleteProjectCache", key="{#root.methodName, #project?.creator, #project?.identificationStatus}")
	public List<Project> getProjectNameList(Project project) {
		return projectMapper.getProjectNameList(project);
	}
	
	@Override
	@Cacheable(value="autocompleteProjectCache", key="{#root.methodName, #project?.creator}")
	public List<Project> getProjectModelNameList() {
		return projectMapper.getProjectModelNameList();
	}
	
	@Override
	public String getReviewerList(String adminYn) {
		String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
		List<T2Users> userList = userMapper.selectReviwer(adminYn, ldapFlag);
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
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Project> list = null;

		try {
			// OSS NAME?????? ???????????? ?????? NICK NAME??? ??????????????? ??????
			if(!isEmpty(project.getOssName()) && CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(project.getOssName().toUpperCase())) {
				String[] nickNames = ossService.getOssNickNameListByOssName(project.getOssName());
				
				if(nickNames != null && nickNames.length > 0) {
					project.setOssNickNames(nickNames);
				}
			}
			
			int records = projectMapper.selectProjectTotalCount(project);
			project.setTotListSize(records);
			String ossId = project.getOssId();

			if (!StringUtil.isEmpty(ossId)) {
				list = projectMapper.selectUnlimitedOssComponentBomList(project);
			} else {
				if(CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_NO)) {
					project.setIdentificationSubStatusBat(CoConstDef.FLAG_NO);
				}
				
				list = projectMapper.selectProjectList(project);
				
				if(list != null) {
					// ??????????????????
					for(Project bean : list) {
						// DISTRIBUTION Android Flag
						String androidFlag = CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equalsIgnoreCase(avoidNull(bean.getNoticeType())) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO;
						bean.setAndroidFlag(androidFlag);
						
						if(CoConstDef.FLAG_YES.equals(androidFlag)) {
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
						String distributionStatus = CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROCESS.equals(bean.getDestributionStatus()) 
														? CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROGRESS : bean.getDestributionStatus();
						bean.setDestributionStatus(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_STATUS, distributionStatus));
						// DIVISION
						bean.setDivision(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, bean.getDivision()));
						
						OssMaster nvdMaxScoreInfo = projectMapper.findIdentificationMaxNvdInfo(bean.getPrjId(), null);
						
						if(nvdMaxScoreInfo != null) {
							bean.setCveId(nvdMaxScoreInfo.getCveId());
							bean.setCvssScore(nvdMaxScoreInfo.getCvssScore());
							bean.setVulnYn(nvdMaxScoreInfo.getVulnYn());
						}
					}
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
	
	@Override
	public Project getProjectDetail(Project project) {
		// master
		project = projectMapper.selectProjectMaster(project);
		
		// ????????? ????????? ??????????????? ?????? Default??? ????????????.
		Map<String, Object> NoticeInfo = projectMapper.getNoticeType(project.getPrjId());
		if(NoticeInfo == null) {
			NoticeInfo = new HashMap<>();
		}
		project.setNoticeType(avoidNull((String) NoticeInfo.get("noticeType"), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));
		project.setNoticeTypeEtc(avoidNull((String) NoticeInfo.get("noticeTypeEtc")));
		project.setAndroidFlag(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(avoidNull(project.getNoticeType())) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
		
		// watcher
		List<Project> watcherList = projectMapper.selectWatchersList(project);
		project.setWatcherList(watcherList);

		// file
		project.setCsvFile(projectMapper.selectCsvFile(project));
		project.setAndroidCsvFile(projectMapper.selectAndroidCsvFile(project));
		project.setAndroidNoticeFile(projectMapper.selectAndroidNoticeFile(project));
		project.setAndroidResultFile(projectMapper.selectAndroidResultFile(project));
		
		if(!isEmpty(project.getBinCsvFileId())) {
			project.setBinCsvFile(projectMapper.selectFileInfoById(project.getBinCsvFileId()));
		}
		
		if(!isEmpty(project.getBinBinaryFileId())) {
			project.setBinBinaryFile(projectMapper.selectFileInfoById(project.getBinBinaryFileId()));
		}

		//  button(??????/??????/??????) view ??????
		if(CommonFunction.isAdmin()) {
			project.setViewOnlyFlag(CoConstDef.FLAG_NO);
		} else {
			project.setViewOnlyFlag(projectMapper.selectViewOnlyFlag(project));
		}
		
		int resultCnt = projectMapper.getOssAnalysisDataCnt(project);
		
		if(resultCnt > 0) {
			Project analysisStatus = projectMapper.getOssAnalysisData(project);
			project.setAnalysisStartDate(analysisStatus.getAnalysisStartDate());
			project.setOssAnalysisStatus(analysisStatus.getOssAnalysisStatus());
		}
		
		return project;
	}
	
	@Override
	public Map<String, Object> getIdentificationGridList(ProjectIdentification identification) {
		return getIdentificationGridList(identification, false);
	}
	
	@Override
	public Map<String, Object> getIdentificationGridList(ProjectIdentification identification, boolean multiUIFlag) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<ProjectIdentification> list = null;
		List<OssComponentsLicense> listLicense = null;
		
		identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		
		if(CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST != null && !CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST.isEmpty()) {
			identification.setRoleOutLicenseIdList(CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST);
		}
		
		boolean isLoadFromProject = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefPrjId());
		
		if(isLoadFromProject) {
			identification.setReferenceId(identification.getRefPrjId());
		}
		
		boolean isApplyFromBat = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefBatId());
		
		if(isApplyFromBat) {
			identification.setReferenceId(identification.getRefBatId());
		}

		// bom ??????
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(identification.getReferenceDiv())) {
			Map<String, String> obligationTypeMergeMap = new HashMap<>();
			String reqMergeFlag = identification.getMerge();
			
			list = projectMapper.selectBomList(identification);
			
			// bom merge ????????? ????????????, ??????????????? ?????? ??????, ????????? ???????????? ?????? ????????? ????????????.
			// need check??? ???????????? ???????????? ??????
			if(CoConstDef.FLAG_YES.equals(reqMergeFlag) && list != null && !list.isEmpty()) {
				identification.setMerge(CoConstDef.FLAG_NO);
				List<ProjectIdentification> bomBeforeList = projectMapper.selectBomList(identification);
				
				if(bomBeforeList != null) {
					for(ProjectIdentification _orgIdentificationBean : bomBeforeList) {
						obligationTypeMergeMap.put(_orgIdentificationBean.getRefComponentId(), _orgIdentificationBean.getObligationType());
					}
				}
			}
			
			Map<String, ProjectIdentification> batMergeSrcMap = new HashMap<>();
			Map<String, ProjectIdentification> batMergePartnerMap = new HashMap<>();
			
			for (ProjectIdentification ll : list) {
				ll.setLicenseId(CommonFunction.removeDuplicateStringToken(ll.getLicenseId(), ","));
				ll.setLicenseName(CommonFunction.removeDuplicateStringToken(ll.getLicenseName(), ","));
  				ll.setCopyrightText(ll.getCopyrightText());
				ll.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
				
				listLicense = projectMapper.selectBomLicense(ll);
				ll.setOssComponentsLicenseList(listLicense);
				ll.setObligationLicense(CoConstDef.FLAG_YES.equals(ll.getAdminCheckYn()) ? CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK : CommonFunction.checkObligationSelectedLicense(listLicense));
				
				if(CoConstDef.FLAG_YES.equals(reqMergeFlag)) {
					if(obligationTypeMergeMap.containsKey(ll.getComponentId())) {
						ll.setObligationType(obligationTypeMergeMap.get(ll.getComponentId()));
					} else {
						ll.setObligationType(ll.getObligationLicense());
					}
				}
				
				// grouping ??? file path??? br tag??? ??????
				ll.setFilePath(CommonFunction.lineReplaceToBR(ll.getFilePath()));
				
				if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(ll.getReferenceDiv())) {
					if(!batMergeSrcMap.containsKey(ll.getOssName().toUpperCase())) {
						batMergeSrcMap.put(ll.getOssName().toUpperCase(), ll);
					} else if(StringUtil.compareTo(ll.getOssVersion(), batMergeSrcMap.get(ll.getOssName().toUpperCase()).getOssVersion()) > 0) {
						batMergeSrcMap.replace(ll.getOssName().toUpperCase(), ll);
					}
				} else if(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(ll.getReferenceDiv())) {
					if(!batMergePartnerMap.containsKey(ll.getOssName().toUpperCase())) {
						batMergePartnerMap.put(ll.getOssName().toUpperCase(), ll);
					} else if (StringUtil.compareTo(ll.getOssVersion(), batMergePartnerMap.get(ll.getOssName().toUpperCase()).getOssVersion()) > 0) {
						batMergePartnerMap.replace(ll.getOssName().toUpperCase(), ll);
					}
				}
				
				// oss Name??? ????????????, oss Version??? ???????????? ?????? case?????? ?????? ??????????????? ??????
				if(isEmpty(ll.getCveId()) 
						&& isEmpty(ll.getOssVersion()) 
						&& !isEmpty(ll.getCvssScoreMax())
						&& !("-".equals(ll.getOssName()))){ 
					String[] cvssScoreMax = ll.getCvssScoreMax().split("\\@");
					ll.setCvssScore(cvssScoreMax[0]);
					ll.setCveId(cvssScoreMax[1]);
				}
			}
			
			// bat merget
			// bat ?????? ?????? ????????? oss version??? ???????????? ??????, src ?????? 3rd party??? ????????? oss ??? ???????????? ??????
			// bat ?????? ????????? src ?????? 3rd party??? merge ??????.
			List<ProjectIdentification> _list = new ArrayList<>();
			List<String> adminCheckList = new ArrayList<>();
			List<ProjectIdentification> groupList = null;
			Map<String, List<ProjectIdentification>> srcSameLicenseMap = new HashMap<>();
			List<String> egnoreList = new ArrayList<>();
			
			for (ProjectIdentification ll : list) {
				// ?????? ????????? oss??? ??????
				if(egnoreList.contains(ll.getComponentId())) {
					continue;
				}
				
				int addIdx = -1;
				
				if(!isEmpty(ll.getOssName())) {
					String mergeKey = ll.getOssName().toUpperCase();
					// main oss??? ???????????? bat oss??? version??? ???????????? ?????? ?????? ??????
					if(CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(ll.getReferenceDiv()) && isEmpty(ll.getMergePreDiv()) && isEmpty(ll.getOssVersion())) {
						ProjectIdentification refBean = null;
						
						if( batMergeSrcMap.containsKey(mergeKey)) {
							// bat => src
							refBean = batMergeSrcMap.get(mergeKey);
							
							// ????????? license??? ????????????, bat??? ????????? license??? src??? ??????????????? (????????? ????????? ????????????)
							// continue?????? ?????? loop?????? merge
							if(!CommonFunction.isSameLicense(refBean.getOssComponentsLicenseList(), ll.getOssComponentsLicenseList())) {
								String ossNameAndVersion = findBatOssOtherVersionWithLicense(ll, refBean, list);
								
								if(!isEmpty(ossNameAndVersion)) {
									List<ProjectIdentification> _batList = null;
									
									if(srcSameLicenseMap.containsKey(ossNameAndVersion)) {
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
							
							// ?????? ??????
							addIdx = findOssAppendIndex(CoConstDef.CD_DTL_COMPONENT_ID_SRC, refBean.getComponentId(), list);
							
							if(addIdx > -1) {
								addIdx = addIdx +1;
								ll.setGroupingColumn(refBean.getOssName() + refBean.getOssVersion());
							}					
						} else if( batMergePartnerMap.containsKey(mergeKey)) {
							// 3rd => bat
							refBean = batMergePartnerMap.get(mergeKey);
							
							// 3rd??? ?????? ???????????? ?????? ?????? ?????? oss list??? ??????
							ll.setGroupingColumn(refBean.getOssName() + refBean.getOssVersion());
							ll.setOssName(refBean.getOssName());
							ll.setOssVersion(refBean.getOssVersion());
							ll.setOssId(refBean.getOssId());
							
							// bin ??? ????????? ????????? 3rd??? ????????? row?????? ?????? ?????????.
							// DOWNLOAD_LOCATION
							if(isEmpty(ll.getDownloadLocation())) {
								ll.setDownloadLocation(refBean.getDownloadLocation());
							}
							
							// HOMEPAGE
							if(isEmpty(ll.getHomepage())) {
								ll.setHomepage(refBean.getHomepage());
							}
							
							// license ??????
							ll.setOssComponentsLicenseList(refBean.getOssComponentsLicenseList());
							ll.setLicenseName(refBean.getLicenseName());
							ll.setCopyrightText(refBean.getCopyrightText());
							
							ll.setObligationLicense(refBean.getObligationLicense());
							ll.setObligationType(refBean.getObligationType());
							
							// 3rd party??? ??????????????? ?????? ?????? ?????????, ???????????? ???????????? ????????? ?????????, ?????? ????????? ???????????? list ????????? ??????
							groupList = findOssGroupList(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, batMergePartnerMap.get(mergeKey).getOssName(), batMergePartnerMap.get(mergeKey).getOssVersion(), list);
							
							if(groupList != null && !groupList.isEmpty()) {
								for(ProjectIdentification _groupBean : groupList) {
									egnoreList.add(_groupBean.getComponentId());
								}
							}
						}
					} 
					// 3rd party??? ??????, bat??? ????????? oss??? ?????? ????????? ?????? (??????)
					else if(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(ll.getReferenceDiv()) && isEmpty(ll.getMergePreDiv()) ) {
						if(existsBatOSS(ll.getOssName(), list)) {
							continue;
						}
					}
				}
				// License Restriction ??????
				ll.setRestriction(CommonFunction.setLicenseRestrictionListById(ll.getLicenseId()));

				if(addIdx > -1) {
					if(addIdx > _list.size() -1) {
						_list.add(ll);
					} else {
						_list.add(addIdx, ll);
					}
				} else {
					_list.add(ll);
					
					if(groupList != null && !groupList.isEmpty()) {
						_list.addAll(groupList);
					}
				}
				
				if(CoConstDef.FLAG_YES.equals(ll.getAdminCheckYn())) {
					adminCheckList.add(ll.getComponentId());
				}
			}
			
			// src oss????????? bat??? merge??? ??? ?????? ????????? oss??? ?????? version ??? ?????????????????? ????????? bat??? ???????????? ??????
			if(!srcSameLicenseMap.isEmpty()) {
				List<ProjectIdentification> _tmp = new ArrayList<>();
				
				for(ProjectIdentification bean : _list) {
					_tmp.add(bean);
					String _key = bean.getOssName() + "-" + avoidNull(bean.getOssVersion());
					
					if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(bean.getReferenceDiv()) && srcSameLicenseMap.containsKey(_key)) {
						for(ProjectIdentification _mergeBean : srcSameLicenseMap.get(_key)) {
							_mergeBean.setOssId(bean.getOssId());
							_mergeBean.setOssName(bean.getOssName());
							_mergeBean.setOssVersion(bean.getOssVersion());
							_mergeBean.setOssComponentsLicenseList(bean.getOssComponentsLicenseList());
							_mergeBean.setGroupingColumn(bean.getGroupingColumn()); // ?????? ??????
							
							_tmp.add(_mergeBean);
						}
					}
				}
				
				_list = _tmp;
			}

			map.put("rows", _list);
			
			if(adminCheckList.size() > 0) {
				map.put("adminCheckList", adminCheckList);
			}
		} else { // bom ??? ?????? ?????????
			// bat oss list??? ??????
			// ?????? ????????? ????????????.
			if(CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(identification.getReferenceDiv()) 
					|| CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(identification.getReferenceDiv())
					|| CoConstDef.CD_DTL_COMPONENT_BAT.equals(identification.getReferenceDiv())) {
				identification.setSortAndroidFlag(CoConstDef.FLAG_YES);
			}
			
			HashMap<String, Object> subMap = new HashMap<String, Object>();
			
			// src, bin, bin(android) ??? ????????? comment??? ????????????.
			if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(identification.getReferenceDiv()) 
					|| CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(identification.getReferenceDiv())
					|| CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(identification.getReferenceDiv())) {
				identification.setIncCommentsFlag(CoConstDef.FLAG_YES);
			}
			
			list = projectMapper.selectIdentificationGridList(identification);
			
			if(list != null && !list.isEmpty()) {
				for(ProjectIdentification project : list){
					String _test = project.getOssName().trim() + "_" + project.getOssVersion().trim();
					String _test2 = project.getOssName().trim() + "_" + project.getOssVersion().trim() + ".0";
					String licenseDiv = "";
					
					if(CoCodeManager.OSS_INFO_UPPER.containsKey(_test.toUpperCase())){
						licenseDiv = CoCodeManager.OSS_INFO_UPPER.get(_test.toUpperCase()).getLicenseDiv(); 
					} else if(CoCodeManager.OSS_INFO_UPPER.containsKey(_test2.toUpperCase())){
						licenseDiv = CoCodeManager.OSS_INFO_UPPER.get(_test2.toUpperCase()).getLicenseDiv();
					}
					
					project.setLicenseDiv(licenseDiv);
					
					// oss Name??? ????????????, oss Version??? ???????????? ?????? case?????? ?????? ??????????????? ??????
					if(isEmpty(project.getCveId()) 
							&& isEmpty(project.getOssVersion()) 
							&& !isEmpty(project.getCvssScoreMax())
							&& !("-".equals(project.getOssName()))){ 
						String[] cvssScoreMax = project.getCvssScoreMax().split("\\@");
						project.setCvssScore(cvssScoreMax[0]);
						project.setCveId(cvssScoreMax[1]);
					}
				}
				
				ProjectIdentification param = new ProjectIdentification();
				OssMaster ossParam = new OssMaster();
				
				// components license ????????? ????????? ????????????
				for(ProjectIdentification bean : list) {
					param.addComponentIdList(bean.getComponentId());
					
					if(!isEmpty(bean.getOssId())) {
						ossParam.addOssIdList(bean.getOssId());
					}
				}
				
				// oss id??? oss master??? ???????????? ?????? ???????????? ????????? ??????
				Map<String, OssMaster> componentOssInfoMap = null;
				
				if(ossParam.getOssIdList() != null && !ossParam.getOssIdList().isEmpty()) {
					componentOssInfoMap = ossService.getBasicOssInfoListById(ossParam);
				}
				
				List<ProjectIdentification> licenseList = projectMapper.identificationSubGrid(param);
				
				for(ProjectIdentification licenseBean : licenseList) {
					for(ProjectIdentification bean : list) {
						if(licenseBean.getComponentId().equals(bean.getComponentId())) {
							// ???????????? ?????? ????????????
							licenseBean.setEditable(CoConstDef.FLAG_YES);
							bean.addComponentLicenseList(licenseBean);
							
							break;
						}
					}
				}
				
				// license ?????? ??????
				for(ProjectIdentification bean : list) {
					if(bean.getComponentLicenseList() != null) {
						//String licenseText = "";
						String licenseCopy = "";
						
						// multi dual ??????????????? ??????, main row??? ???????????? license ????????? OSS List??? ??????????????? ??????????????? ???????????? ????????????.
						// ossId??? ?????? ????????? ??????????????? subGrid??? ????????? ??? ??????
						// ??????????????? ?????? ????????? ?????????, subgrid ?????? ???????????? ????????? ??????????????? oss ??? ???????????? ?????? ??????????????? ???????????? ??????
						if(componentOssInfoMap == null) {
							componentOssInfoMap = new HashMap<>();
						}
						
						OssMaster ossBean = componentOssInfoMap.get(bean.getOssId());
						
						// Restriction ????????? ?????? (BOM ??? ??????) License Id??? ?????? ???????????? ??????
						String strLicenseIds = "";
						
						if(ossBean != null
								&& CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())
								&& ossBean.getOssLicenses() != null && !ossBean.getOssLicenses().isEmpty()) {
							for(OssLicense ossLicenseBean : ossBean.getOssLicenses()) {
								
								if(!isEmpty(ossLicenseBean.getOssCopyright())) {
									licenseCopy += (!isEmpty(licenseCopy) ? "\r\n" : "") + ossLicenseBean.getOssCopyright();
								}
								
								//?????? ?????? ??????
								for(ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
									// license index ?????? ???????????? ?????????
									// multi dual ????????? ??????, ????????? ??????????????? ?????? ?????? ??? ??? ?????? ??????
									if(ossLicenseBean.getLicenseId().equals(licenseBean.getLicenseId()) 
											&& ossLicenseBean.getOssLicenseIdx().equals(licenseBean.getRnum())) {
										licenseBean.setEditable(CoConstDef.FLAG_NO);
										break;
									}
								}
								
								// Restriction ????????? ?????? license id ??????
								if(!isEmpty(ossLicenseBean.getLicenseId())) {
									strLicenseIds += (!isEmpty(strLicenseIds) ? "," : "") + ossLicenseBean.getLicenseId();
								}
								
							}
							
							// ????????? ????????? ???????????? ???????????? ????????? ????????? ??????
							if(bean.getComponentLicenseList() != null && bean.getComponentLicenseList().size() == 1 && bean.getComponentLicenseList().get(0) != null) {
								bean.setLicenseName(bean.getComponentLicenseList().get(0).getLicenseName());
							} else if(CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())) {
								bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
							} else {
								// TODO - ?????? ???????????? ?????? ??????.. multiUIFlag
								bean.setLicenseName(CommonFunction.makeLicenseExpression(ossBean.getOssLicenses()));
							}
						} else {
							for(ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
								if(!isEmpty(licenseBean.getCopyrightText())) {
									licenseCopy += (!isEmpty(licenseCopy) ? "\r\n" : "") + licenseBean.getCopyrightText();
								}

								// Restriction ????????? ?????? license id ??????
								if(!isEmpty(licenseBean.getLicenseId())) {
									strLicenseIds += (!isEmpty(strLicenseIds) ? "," : "") + licenseBean.getLicenseId();
								}
							}
							
							bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
						}
						

						// Restriction ??????
						if(!isEmpty(strLicenseIds)) {
							bean.setRestriction(CommonFunction.setLicenseRestrictionListById(strLicenseIds));
						}
						
						// 3rd party??? ?????? obligation ????????? ?????? license??? ?????? obligation ????????? ??????
						if(CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(identification.getReferenceDiv())) {
							bean.setObligationLicense(CommonFunction.checkObligationSelectedLicense2(bean.getComponentLicenseList()));
							
							if(isEmpty(bean.getObligationType())) {
								bean.setObligationType(bean.getObligationLicense());
							}
							
							// need check ??? ??????
							if(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(bean.getObligationLicense())) {
								if(CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType())) {
									bean.setNotify(CoConstDef.FLAG_YES);
									bean.setSource(CoConstDef.FLAG_NO);
								} else if(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) {
									bean.setNotify(CoConstDef.FLAG_YES);
									bean.setSource(CoConstDef.FLAG_YES);
								} else if(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK_SELECTED.equals(bean.getObligationType())) {
									bean.setNotify(CoConstDef.FLAG_NO);
									bean.setSource(CoConstDef.FLAG_NO);
								} else {
									bean.setNotify("");
									bean.setSource("");
								}
							}
						}
						
						// subGrid??? Item ????????? ?????? ????????? map?????? ????????????.
						// ????????? component_id??? key??? ????????????.
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					} else {
						bean.setLicenseName("");
						
						subMap.put(bean.getGridId(), new ArrayList<ProjectIdentification>());
					}
					
					// bat ?????? ????????? ??????
					if(!isEmpty(bean.getBatPercentage()) || !isEmpty(bean.getBatStringMatchPercentage())) {
						String batPercentageStr = avoidNull(bean.getBatStringMatchPercentage());
						
						if(CoConstDef.BAT_MATCHED_STR.equalsIgnoreCase(avoidNull(bean.getBatPercentage()).trim())) {
							batPercentageStr = CoConstDef.BAT_MATCHED_STR;
						} else if(!isEmpty(bean.getBatPercentage())) {
							batPercentageStr += " (" + bean.getBatPercentage() + ")";
						}

						bean.setBatStrPlus(batPercentageStr);
						
						// Change bat grid result percentage (UI) Number only
						if(!isEmpty(bean.getBatStringMatchPercentage())) {
							bean.setBatStringMatchPercentageFloat(bean.getBatStringMatchPercentage().replace("%", "").trim());
						}
						
					}
					
					// License Restriction ??????
					bean.setRestriction(CommonFunction.setLicenseRestrictionList(bean.getComponentLicenseList()));
					
					// comments??? null??? ?????? grid?????? ????????? ???????????? ?????? ?????????, ???????????? ????????????.
					bean.setComments(avoidNull(bean.getComments()));
				}
				

				// ?????? ?????????????????? load??? ?????? component id ?????????
				if(isLoadFromProject || isApplyFromBat) {
					subMap = new HashMap<>();

					// refproject id + "p" + componentid ??? component_id??? ????????? ??????, 
					// license ??? ?????? ???????????? component_id + ?????? license grid_id??? component_license_id ????????? ??????
					for(ProjectIdentification bean : list) {
						if(isLoadFromProject) {
							bean.setRefPrjId(identification.getRefPrjId());
						} else if(isApplyFromBat) {
							bean.setRefBatId(identification.getRefBatId());
						}
						
						String _compId = CoConstDef.GRID_NEWROW_DEFAULT_PREFIX;
						
						if(isLoadFromProject) {
							_compId += identification.getRefPrjId();
						} else if (isApplyFromBat) {
							_compId += identification.getRefBatId();
						}
						
						_compId += "p" + bean.getComponentId();
						
						bean.setComponentId("");
						bean.setGridId(_compId);

						if(bean.getComponentLicenseList()!=null){
							for(ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
								licenseBean.setComponentId("");
								licenseBean.setGridId(_compId + "-"+ licenseBean.getComponentLicenseId());
							}
						}
						
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					}
				}
			}
			
			// ???????????? data ??? ????????? ?????? append ??????.
			{
				if(identification.getMainDataGridList() != null) {
					for(ProjectIdentification bean : identification.getMainDataGridList()) {
						if(bean.getComponentLicenseList() == null || bean.getComponentLicenseList().isEmpty()){
							//????????????????????? ??????
							if(CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())){
								for (List<ProjectIdentification> comLicenseList : identification.getSubDataGridList()) {
									for (ProjectIdentification comLicense : comLicenseList) {
										if(bean.getComponentId().equals(comLicense.getComponentId())){
											bean.addComponentLicenseList(comLicense);
										}
									}
								}
							} else { //???????????????????????????
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

					for(ProjectIdentification bean : identification.getMainDataGridList()) {
						list.add(0, bean);
						
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					}
				}
			}
			
			// exclude row??? ????????? (?????? ???????????????)
			List<ProjectIdentification> newSortList = new ArrayList<>();
			List<ProjectIdentification> excludeList = new ArrayList<>();
			
			for(ProjectIdentification bean : list) {
				if(CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
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
	
	/**
	 * BAT??? ????????? OSS, license??? ????????? SRC ????????? ????????? ??????
	 * @param ll
	 * @param refBean
	 * @param list
	 * @return
	 */
	private String findBatOssOtherVersionWithLicense(ProjectIdentification ll, ProjectIdentification refBean, List<ProjectIdentification> list) {
		for(ProjectIdentification bean : list) {
			if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(bean.getReferenceDiv()) 
					&& bean.getOssName().equals(refBean.getOssName())
					&& !bean.getOssVersion().equals(refBean.getOssVersion())
					&& bean.getOssName().equals(ll.getOssName())) {
				if(CommonFunction.isSameLicense(bean.getOssComponentsLicenseList(), ll.getOssComponentsLicenseList())) {
					return bean.getOssName() + "-" + avoidNull(bean.getOssVersion());
				}
			}
		}
		
		return null;
	}
	
	private int findOssAppendIndex(String type, String componentId, List<ProjectIdentification> list) {
		int idx = 0;
		
		for(ProjectIdentification bean : list) {
			if(type.equals(bean.getReferenceDiv()) && componentId.equals(bean.getComponentId())) {
				return idx;
			}
			
			idx++;
		}
		
		return -1;
	}
	
	private List<ProjectIdentification> findOssGroupList(String type, String ossName, String ossVersion, List<ProjectIdentification> list) {
		String targetGroup = avoidNull(ossName) + avoidNull(ossVersion);
		List<ProjectIdentification> groupList = new ArrayList<>();
		
		for(ProjectIdentification bean : list) {
			if(type.equals(bean.getReferenceDiv()) && targetGroup.equalsIgnoreCase(bean.getGroupingColumn())) {
				groupList.add(bean);
			}
		}
		
		return groupList;
	}
	
	private boolean existsBatOSS(String ossName, List<ProjectIdentification> list) {
		for(ProjectIdentification bean : list) {
			if(CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(bean.getReferenceDiv()) && isEmpty(bean.getMergePreDiv()) && isEmpty(bean.getOssVersion())
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
		map.put("rows", modelList); // export, project ??????????????? ????????? ???????????? ?????? ????????? ?????? rows??? ??????
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
			notice = verificationService.selectOssNoticeOne(prjId);
			
			if(isEmpty(notice.getCompanyNameFull()) 
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
				// OSS_NOTICE??? OSS_NOTICE_NEW??? ????????? ???????????? default setting
				notice.setEditNoticeYn(CoConstDef.FLAG_NO);
				notice.setEditCompanyYn(CoConstDef.FLAG_YES);
				notice.setEditDistributionSiteUrlYn(CoConstDef.FLAG_YES);
				notice.setEditEmailYn(CoConstDef.FLAG_YES);
				notice.setHideOssVersionYn(CoConstDef.FLAG_NO);
				notice.setEditAppendedYn(CoConstDef.FLAG_NO);
				notice.setPrjId(project.getPrjId());
				
				String distributeType = avoidNull(project.getDistributeTarget(), CoConstDef.CD_DISTRIBUTE_SITE_SKS); // LGE, NA => LGE??? ??????, SKS => SKS??? ?????????.
				String distributeCode = CoConstDef.CD_DISTRIBUTE_SITE_SKS.equals(distributeType) ? CoConstDef.CD_NOTICE_DEFAULT_SKS : CoConstDef.CD_NOTICE_DEFAULT;
				
				if(isEmpty(notice.getCompanyNameFull())) {
					notice.setCompanyNameFull(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_FULLNAME));
				}
				
				if(isEmpty(notice.getDistributionSiteUrl())) {
					notice.setDistributionSiteUrl(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_DISTRIBUTE_SITE));
				}
				
				if(isEmpty(notice.getEmail())) {
					notice.setEmail(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_EMAIL));
				}
				
				if(isEmpty(notice.getAppended())){
					notice.setAppended(CoCodeManager.getCodeExpString(distributeCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_APPENDED));
				}
			} else if(CoConstDef.FLAG_YES.equals(notice.getEditNoticeYn())
					&& CoConstDef.CD_NOTICE_TYPE_GENERAL.equals(notice.getNoticeType())) {
				
			} else {
				if(!isEmpty(notice.getCompanyNameFull())){
					notice.setEditCompanyYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if(!isEmpty(notice.getDistributionSiteUrl())){
					notice.setEditDistributionSiteUrlYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if(!isEmpty(notice.getEmail())){
					notice.setEditEmailYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if(!isEmpty(notice.getAppended())){
					notice.setEditAppendedYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
				
				if(CoConstDef.CD_NOTICE_TYPE_GENERAL_WITHOUT_OSS_VERSION.equals(project.getNoticeType())){
					notice.setHideOssVersionYn(CoConstDef.FLAG_YES);
					notice.setEditNoticeYn(CoConstDef.FLAG_YES);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return notice;
	}
	
	@Override
	public Project getProjectBasicInfo(String prjId) {
		Project param = new Project();
		param.setPrjId(prjId);
		
		return projectMapper.selectProjectMaster2(param);
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
		
		if(Project.class.equals(param.getClass())) {
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
		//copy ???
		if("true".equals(project.getCopy())){
			String oldId = project.getPrjId();
			project.setPrjId(null);
			project.setCopyPrjId(oldId);
			project.setPublicYn(avoidNull(project.getPublicYn(), CoConstDef.FLAG_YES));
			// project master
			projectMapper.insertProjectMaster(project);
			
			String prjId = project.getPrjId();

			// notice type (packaging?????? project ??????????????? ??????????????? ??????????????????
			// verificationMapper??? insert??? ?????????, ???????????? ????????????????????? notice type??? ???????????? ????????? ????????? project mapper??? ??????
			OssNotice noticeParam = new OssNotice();
			noticeParam.setPrjId(project.getPrjId());
			noticeParam.setNoticeType(avoidNull(project.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));
			
			if(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(project.getNoticeType())) {
				noticeParam.setNoticeTypeEtc(project.getNoticeTypeEtc());
			}
			
			projectMapper.makeOssNotice(noticeParam);

			// project model insert
			if (project.getModelList().size() > 0) {
				for(Project modelBean : project.getModelList()) {
					modelBean.setPrjId(project.getPrjId());
					modelBean.setModelName(modelBean.getModelName().trim().toUpperCase().replaceAll("\t", ""));
					
					// copy ??? ??????????????? ??????, ??????????????? ?????? ????????? ????????????.
					// ????????? ????????? ?????? ????????? ????????? ?????? ??????
					if(CoConstDef.FLAG_YES.equals(modelBean.getDelYn())) {
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

			if (project.getWatchers()!= null) { // Project ?????? ????????? ???????????? watcher ??????
				ArrayList<Map<String, String>> divisionList = new ArrayList<Map<String, String>>();
				ArrayList<Map<String, String>> emailList = new ArrayList<Map<String, String>>();
				String[] arr;
				
				for (String watcher : project.getWatchers()) {
					Map<String, String> m = new HashMap<String, String>();
					arr = watcher.split("\\/");
					
					if(!"Email".equals(arr[1])){
						project.setPrjDivision(arr[0]);
						
						if(arr.length > 1){
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
					
					if(watcherList.size() == 0){
						projectMapper.insertProjectWatcher(project);
					}
				}
				
				project.setDivisionList(divisionList);
				project.setEmailList(emailList);
				
				projectMapper.deleteProjectWatcher(project);
			}
			
			//????????? ???????????? ????????? ????????? ??????
			// Identification ?????? ????????? Copy??????.
			// upload??? ????????? ?????? ????????? ?????? ????????? ????????? ???????????? ???????????????. ??????????????? ???????????? ?????? ????????????.
			project.setPrjId(oldId);
			Project orgBean = projectMapper.selectProjectMaster2(project);
			project.setPrjId(prjId);
			
			{
				Project newBean = new Project();
				newBean.setPrjId(prjId);
				newBean.setModifier(newBean.getLoginUserName());
				
				if(!isEmpty(orgBean.getIdentificationStatus())) {
					newBean.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
				}
				
				newBean.setIdentificationSubStatusPartner(orgBean.getIdentificationSubStatusPartner());
				newBean.setIdentificationSubStatusSrc(orgBean.getIdentificationSubStatusSrc());
				newBean.setIdentificationSubStatusBin(orgBean.getIdentificationSubStatusBin());
				newBean.setIdentificationSubStatusAndroid(orgBean.getIdentificationSubStatusAndroid());
				newBean.setIdentificationSubStatusBat(orgBean.getIdentificationSubStatusBat());
				
				// distribute target??? ????????? ??????, ????????? ??????????????? ???????????? ?????????.
				if(!CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(project.getDistributeTarget()) 
						&& avoidNull(project.getDistributeTarget()).equals(orgBean.getDistributeTarget())) {
					newBean.setDistributeMasterCategory(orgBean.getDistributeMasterCategory());
					newBean.setDistributeName(orgBean.getDistributeName());
					newBean.setDistributeSoftwareType(orgBean.getDistributeSoftwareType());
				}
				
				if(!avoidNull(project.getDistributionType()).equals(orgBean.getDistributionType())) {}
				
				if(!isEmpty(orgBean.getSrcAndroidCsvFileId())) {
					newBean.setSrcAndroidCsvFileId(fileService.copyFileInfo(orgBean.getSrcAndroidCsvFileId()));
				}
				
				if(!isEmpty(orgBean.getSrcAndroidNoticeFileId())) {
					newBean.setSrcAndroidNoticeFileId(fileService.copyFileInfo(orgBean.getSrcAndroidNoticeFileId()));
				}
				
				if(!isEmpty(orgBean.getSrcAndroidResultFileId())) {
					newBean.setSrcAndroidResultFileId(fileService.copyFileInfo(orgBean.getSrcAndroidResultFileId()));
				}
				
				// copy ??? distributeName ??? ?????? ?????? ????????? ????????? ??????
				newBean.setCopyFlag(CoConstDef.FLAG_YES);
				
				projectMapper.updateProjectMaster(newBean);
			}
			
			//identification-components
			project.setOldId(oldId);
			List<ProjectIdentification> components = projectMapper.selectOssComponentsList(project);
			List<OssComponentsLicense> licenses;
			int componentIdx = 1;
			
			for (ProjectIdentification bean : components){
				bean.setReferenceId(prjId);
				bean.setReportFileId(null);
				licenses = projectMapper.selectOssComponentsLicenseList(bean);
				bean.setComponentIdx(String.valueOf(componentIdx++));
				projectMapper.insertOssComponents(bean);
				
				for (OssComponentsLicense licenseBean : licenses){
					licenseBean.setComponentId(bean.getComponentId());
					
					projectMapper.insertOssComponentsLicense(licenseBean);
				}
			}
			
			// Project - 3rd ?????? ?????? ??????
			List<PartnerMaster> partnerList = partnerMapper.selectThirdPartyMapList(oldId);
			for (PartnerMaster bean : partnerList) {
				bean.setPrjId(prjId);
				
				partnerMapper.insertPartnerMapList(bean);
			}
		} else {
			boolean isNew = isEmpty(project.getPrjId());
			
			// ?????? ???????????? ?????????, Android model  ??? ??????, 3rd, src, bin Tab???  N/A ????????????.
			if(isNew && "A".equals(CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE, project.getDistributionType())) ) {
				 project.setIdentificationSubStatusPartner(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusSrc(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusBin(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusBat(CoConstDef.FLAG_NO);
			}
			
			// admin??? ???????????? creator??? ???????????? ?????????.
			if(!CommonFunction.isAdmin()) {
				project.setCreator(null);
			}
			
			project.setPublicYn(avoidNull(project.getPublicYn(), CoConstDef.FLAG_YES));
			
			// if complete value equals 'Y', set
			if(!isNew) {
				Project prjBean = projectMapper.selectProjectMaster(project);
				
				if(prjBean != null) {
					if(CoConstDef.FLAG_YES.equals(prjBean.getCompleteYn())) {
						project.setCompleteYn(CoConstDef.FLAG_YES);
					}
				}
			}
			// project master
			projectMapper.insertProjectMaster(project);
			
			OssNotice noticeParam = new OssNotice();
			noticeParam.setPrjId(project.getPrjId());
			noticeParam.setNoticeType(avoidNull(project.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));
			
			if(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(project.getNoticeType())) {
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
			
			if("CONF".equals(result.getVerificationStatus()) 
					&& isEmpty(result.getDestributionStatus())
					&& CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(project.getDistributeTarget())) {
				projectMapper.updateProjectDistributionStatus(project.getPrjId(), CoConstDef.CD_DTL_DISTRIBUTE_NA);
			} else if("CONF".equals(result.getVerificationStatus()) 
					&& CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(result.getDestributionStatus())
					&& !CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(project.getDistributeTarget())) {
				projectMapper.updateProjectDistributionStatus(project.getPrjId(), null);
			}
			
			if(isNew) {
				// project watcher insert
				ArrayList<Map<String, String>> divisionList = new ArrayList<Map<String, String>>();
				ArrayList<Map<String, String>> emailList = new ArrayList<Map<String, String>>();

				if (project.getWatchers()!= null) {
					String[] arr;
					
					for (String watcher : project.getWatchers()) {
						Map<String, String> m = new HashMap<String, String>();
						arr = watcher.split("\\/");
						
						if(!"Email".equals(arr[1])){
							project.setPrjDivision(arr[0]);
							
							if(arr.length > 1){
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
						
						if(watcherList.size() == 0){
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

	@Transactional
	@Override
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void deleteProject(Project project) {
		projectMapper.deleteProjectMaster(project);
	}

	@Override
	public Map<String, Object> getOssIdLicenses(ProjectIdentification identification) {
		HashMap<String, Object> map = new HashMap<String, Object>();

		try {
			ProjectIdentification prjOssMaster = projectMapper.getOssId(identification);
			List<ProjectIdentification> Licenselist = projectMapper.getLicenses(prjOssMaster);
			
			if(Licenselist.size() != 0){
				//excludeYn ???????????????
				Licenselist = CommonFunction.makeLicenseExcludeYn(Licenselist);
				// multi/dual license??? ?????? ????????? ????????? ??????
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

		ProjectIdentification components = new ProjectIdentification();
		components.setReferenceId(project.getPrjId());
		components.setReferenceDiv(project.getReferenceDiv());
		
		List<OssComponents> componentsLicense = projectMapper.selectComponentId(components);
		
		for(OssComponents oc : componentsLicense) {
			projectMapper.deleteOssComponentsLicense(oc);
		}
		
		projectMapper.deleteOssComponents(components);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void registComponentsThird(String prjId, String identificationSubStatusPartner, List<OssComponents> ossComponentsList, List<PartnerMaster> thirdPartyList) {		
		// ???????????? ????????? ??????
		Project prjBasicInfo = new Project();
		prjBasicInfo.setPrjId(prjId);
		prjBasicInfo = projectMapper.selectProjectMaster(prjBasicInfo);
		
		// ???????????? ?????? ?????? ??????
		{
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(prjId);
			projectSubStatus.setIdentificationSubStatusPartner((ossComponentsList.isEmpty() && thirdPartyList.isEmpty()) ? "X" : CoConstDef.FLAG_YES);
			
			// ?????? ??????????????? ?????? ??????
			// row count??? 0????????? ???????????? ??????????????? ???????????? progress ????????? ??????????????????
			if(isEmpty(prjBasicInfo.getIdentificationStatus())) {
				projectSubStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			}
			
			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			projectMapper.updateProjectMaster(projectSubStatus);
		}
		
		OssComponents compParam = new OssComponents();
		compParam.setReferenceId(prjId);
		Map<String, ProjectIdentification> orgOssMap = new HashMap<>();
		
		// ?????? ?????? oss ?????? ??????
		{
			Map<String, Object> _map = getIdentificationThird(compParam);
			List<ProjectIdentification> rows = (List<ProjectIdentification>) _map.get("rows");
			
			if(rows != null) {
				for(ProjectIdentification bean : rows) {
					orgOssMap.put(bean.getComponentId(), bean);
				}
			}
		}
		
		List<OssComponents> updateList = new ArrayList<>();
		List<OssComponents> insertFromPrjList = new ArrayList<>();
		List<OssComponents> insertFromPartnerList = new ArrayList<>();
		List<String> deleteList = new ArrayList<>();
		
		// ???????????? ?????? 1) update, 2) delete, 3)insert from project , 4) insert from 3rd 
		for(OssComponents bean : ossComponentsList) {
			// ????????? ???????????? ?????? ????????? update
			if(orgOssMap.containsKey(avoidNull(bean.getComponentId()))) {
				// exclude ????????? ??????
				updateList.add(bean);
				deleteList.add(bean.getComponentId());
			} else {
				if(!isEmpty(bean.getRefPrjId())) {
					// ?????? ???????????????, ref ???????????? id??? ????????? 
					// componentsid + prjid + exclude ????????? ??????
					insertFromPrjList.add(bean);
				} else {
					// ?????? ???????????????, ref partner id??? ????????? (??????????????? ?????? ????????? else?????????)
					//componentsid + partnerid + exclude ????????? ??????
					insertFromPartnerList.add(bean);
				}
			}
		}
		
		// 1) update
		for(OssComponents bean : updateList) {
			projectMapper.updatePartnerOssList(bean);
		}
		
		// 2) delete
		if(orgOssMap != null && !orgOssMap.isEmpty()) {
			OssComponents param = new OssComponents();
			param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
			param.setReferenceId(prjId);
			param.setOssComponentsIdList(deleteList);
			List<String> deleteComponentIds = projectMapper.getDeleteOssComponentsLicenseIds(param);
			param.setOssComponentsIdList(deleteComponentIds);
			
			if(deleteComponentIds.size() > 0){
				projectMapper.deleteOssComponentsLicenseWithIds(param);
				// deleteOssComponentsWithIds??? not in ?????? delete, deleteOssComponentsWithIds2??? in ?????? delete???.
				projectMapper.deleteOssComponentsWithIds2(param); 
			}
		}
		
		// 3) insert from project
		for(OssComponents bean : insertFromPrjList) {
			bean.setComponentId(bean.getRefComponentId());
			bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
			bean.setReferenceId(prjId);
			
			projectMapper.insertOssComponentsCopy(bean);
			projectMapper.insertOssComponentsLicenseCopy(bean);
			
		}
		
		// 4) insert from 3rd part
		for(OssComponents bean : insertFromPartnerList) {
			bean.setComponentId(bean.getRefComponentId());
			bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
			bean.setReferenceId(prjId);
			
			projectMapper.insertOssComponentsCopy(bean);
			projectMapper.insertOssComponentsLicenseCopy(bean);
		}
		
		Map<String, PartnerMaster> thirdPartyMap = new HashMap<>();
		
		// ?????? ?????? 3rd Map ?????? ??????
		{
			Map<String, Object> _map = getThirdPartyMap(prjId);
			
			if(_map != null) {
				for(PartnerMaster bean : (List<PartnerMaster>) _map.get("rows")) {
					thirdPartyMap.put(bean.getPartnerId(), bean);
				}
			}
		}
		
		List<PartnerMaster> thirdPartyUpdateList = new ArrayList<>();
		List<PartnerMaster> thirdPartyInsertList = new ArrayList<>();
		List<String> thirdPartyDeleteList = new ArrayList<>();
		
		for(PartnerMaster bean : thirdPartyList) {
			// ????????? ???????????? ?????? ????????? update
			if(thirdPartyMap.containsKey(avoidNull(bean.getPartnerId()))) {
				thirdPartyUpdateList.add(bean);
			} else {
				thirdPartyInsertList.add(bean);
			}
		}
		
		Map<String, PartnerMaster> deleteCheckMap = new HashMap<>();
		
		// 1) update
		for(PartnerMaster bean : thirdPartyUpdateList) {
			deleteCheckMap.put(bean.getPartnerId(), bean);
		}
				
		// 3) insert
		for(PartnerMaster bean : thirdPartyInsertList) {
			deleteCheckMap.put(bean.getPartnerId(), bean);
			bean.setPrjId(prjId);
			
			partnerMapper.insertPartnerMapList(bean);
		}
		
		for(String s : thirdPartyMap.keySet()) {
			if(!deleteCheckMap.containsKey(s)) {
				thirdPartyDeleteList.add(s);
			}
		}
		
		// 2) delete
		if(!thirdPartyDeleteList.isEmpty()) {
			PartnerMaster param = new PartnerMaster();
			param.setPrjId(prjId);
			param.setThirdPartyPartnerIdList(thirdPartyDeleteList);
			
			partnerMapper.deletePartnerMapList(param);
		}
	}

	/* 
	 * ???????????? ????????? (Name, Version) ?????? ??????
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
	@Transactional
	public void registSrcOss(List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense, Project project, String refDiv) {
		// ????????? ????????? ???????????? ????????? SRC ????????????????????? N?????? N ?????? null
		if(ossComponent.size()==0){
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(project.getPrjId());
			
			if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
				if(!StringUtil.isEmpty(project.getIdentificationSubStatusSrc())){
					projectSubStatus.setIdentificationSubStatusSrc(project.getIdentificationSubStatusSrc());
				} else {
					projectSubStatus.setIdentificationSubStatusSrc("X");
				}
			} else if(CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(refDiv)) {
				if(!StringUtil.isEmpty(project.getIdentificationSubStatusBin())){
					projectSubStatus.setIdentificationSubStatusBin(project.getIdentificationSubStatusBin());
				} else {
					projectSubStatus.setIdentificationSubStatusBin("X");
				}
			} else {
				if(!StringUtil.isEmpty(project.getIdentificationSubStatusAndroid())){
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
		if(project.getCsvFile() != null && project.getCsvFile().size() > 0) {
			deleteUploadFile(project, refDiv);
		}
		
		// ?????? ??????
		if(!isEmpty(project.getSrcCsvFileId()) || !isEmpty(project.getSrcAndroidCsvFileId()) || !isEmpty(project.getSrcAndroidNoticeFileId()) || !isEmpty(project.getBinCsvFileId()) || !isEmpty(project.getBinBinaryFileId())){
			projectMapper.updateFileId(project);
			
			if(project.getCsvFileSeq() != null) {
				for (int i = 0; i < project.getCsvFileSeq().size(); i++) {
					projectMapper.updateFileBySeq(project.getCsvFileSeq().get(i));
				}				
			}
		}
		
		// bin android ??? ?????? ?????? ?????????????????? load??? ????????? save??? ??????, notice html??? result text ????????? ????????????.
		if(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv) && CoConstDef.FLAG_YES.equals(project.getLoadFromAndroidProjectFlag())) {
			if(isEmpty(project.getSrcAndroidResultFileId())) {
				project.setSrcAndroidResultFileId(null);
			}
			
			projectMapper.updateAndroidNoticeFileInfoWithLoadFromProject(project);
		}
	}
	
	private void deleteUploadFile(Project project, String refDiv) {
		Project prjFileCheck = projectMapper.getProjectBasicInfo(project);
		boolean fileDeleteCheckFlag = false;
		
		if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
			if(project.getCsvFileSeq().size() == 0 && !isEmpty(prjFileCheck.getSrcCsvFileId())) {
				project.setSrcCsvFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
		} else if(CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(refDiv)) {
			if(isEmpty(project.getBinCsvFileId()) && !isEmpty(prjFileCheck.getBinCsvFileId())) {
				project.setBinCsvFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
			if(isEmpty(project.getBinBinaryFileId()) && !isEmpty(prjFileCheck.getBinBinaryFileId())) {
				project.setBinBinaryFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
		} else {
			if(isEmpty(project.getSrcAndroidCsvFileId()) && !isEmpty(prjFileCheck.getSrcAndroidCsvFileId())) {
				project.setSrcAndroidCsvFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
			if(isEmpty(project.getSrcAndroidNoticeFileId()) && !isEmpty(prjFileCheck.getSrcAndroidNoticeFileId())) {
				project.setSrcAndroidNoticeFileFlag(CoConstDef.FLAG_YES);
				fileDeleteCheckFlag = true;
			}
		}
		
		if(project.getCsvFile() != null && project.getCsvFile().size() > 0) {
			for (int i = 0; i < project.getCsvFile().size(); i++) {
				projectMapper.deleteFileBySeq(project.getCsvFile().get(i));
				fileService.deletePhysicalFile(project.getCsvFile().get(i), "Identification");
			}
		}
		
		if(fileDeleteCheckFlag) projectMapper.updateFileId2(project);
	}
	
	@Override
	@Transactional
	public void registOss(List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense, String refId, String refDiv) {
		updateOssComponentList(new Project(), refDiv, refId, ossComponent, ossComponentLicense);
	}
	
	@Transactional
	private void updateOssComponentList(Project project, String refDiv, String refId, List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense) {
		// ???????????? ????????? ???????????? ?????????
		ProjectIdentification prj = new ProjectIdentification();
		
		if(isEmpty(refId)) {
			refId = project.getPrjId();
		}
		
		prj.setReferenceId(refId);
		prj.setReferenceDiv(refDiv);
		List<OssComponents> componentId = projectMapper.selectComponentId(prj);
		
		for (int i = 0; i < componentId.size(); i++) {
			projectMapper.deleteOssComponentsLicense(componentId.get(i));
		}
		
		if(!CoConstDef.CD_DTL_COMPONENT_BAT.equals(refDiv)) {
			if(!ossComponent.isEmpty()) {
				Project projectStatus = new Project();
				projectStatus.setPrjId(refId);
				projectStatus = projectMapper.selectProjectMaster(projectStatus);
				
				// ?????? ???????????? PROG 
				if (StringUtil.isEmpty(projectStatus.getIdentificationStatus())) {
					projectStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
				}
				
				if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
					// ???????????? ????????? SRC ????????????????????? N ?????? N ?????? Y
					if(!StringUtil.isEmpty(project.getIdentificationSubStatusSrc())){
						projectStatus.setIdentificationSubStatusSrc(project.getIdentificationSubStatusSrc());
					} else {
						projectStatus.setIdentificationSubStatusSrc(CoConstDef.FLAG_YES);
					}
				} else if(CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(refDiv)) {
					// ???????????? ????????? SRC ????????????????????? N ?????? N ?????? Y
					if(!StringUtil.isEmpty(project.getIdentificationSubStatusBin())){
						projectStatus.setIdentificationSubStatusBin(project.getIdentificationSubStatusBin());
					} else {
						projectStatus.setIdentificationSubStatusBin(CoConstDef.FLAG_YES);
					}
				} else {
					// ???????????? ????????? SRC ????????????????????? N ?????? N ?????? Y
					if(!StringUtil.isEmpty(project.getIdentificationSubStatusAndroid())){
						projectStatus.setIdentificationSubStatusAndroid(project.getIdentificationSubStatusAndroid());
					} else {
						projectStatus.setIdentificationSubStatusAndroid(CoConstDef.FLAG_YES);
					}
				}
	
				projectStatus.setModifier(projectStatus.getLoginUserName());
				projectMapper.updateProjectMaster(projectStatus);
			}
		}

		project.setReferenceDiv(refDiv);
		project.setReferenceId(refId);
		
		int ossComponentIdx = projectMapper.selectOssComponentMaxIdx(project);
		
		//deleteRows
		List<String> deleteRows = new ArrayList<String>();
		
		// ???????????? ??????	
		for (int i = 0; i < ossComponent.size(); i++) {
			// SRC STATUS ??????
			
			ProjectIdentification ossBean = ossComponent.get(i);
			
			// oss_id??? ?????? ?????????. (oss name??? oss id??? ???????????? ?????? ????????? ?????? ??? ??????)
			ossBean = CommonFunction.findOssIdAndName(ossBean);
			
			String downloadLocationUrl = ossBean.getDownloadLocation();
			String homepageUrl = ossBean.getHomepage();
			
			if(!isEmpty(downloadLocationUrl)) {
				if(downloadLocationUrl.endsWith("/")) {
					ossBean.setDownloadLocation(downloadLocationUrl.substring(0, downloadLocationUrl.length()-1));
				}
			}
			
			if(!isEmpty(homepageUrl)) {
				if(homepageUrl.endsWith("/")) {
					ossBean.setHomepage(homepageUrl.substring(0, homepageUrl.length()-1));
				}
			}
			
			//update
			if(!StringUtil.contains(ossBean.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
				//ossComponents ??????
				// android project??? ??????, bom ????????? ?????? ?????? ?????????, bom save?????? ???????????? obligation type??? ????????? ??????????????????.
				if(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv)) {
					List<OssComponentsLicense> _list = new ArrayList<>();
					
					if(CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())) {
						for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
							for (ProjectIdentification comLicense : comLicenseList) {
								if(ossBean.getComponentId().equals(comLicense.getComponentId())){
									// multi license oss??? license??? ????????? ??????, license ?????? ???????????? ?????? ????????? ??????
									if(isEmpty(comLicense.getLicenseName()) && isEmpty(comLicense.getLicenseText()) && isEmpty(comLicense.getOssCopyright())) {
										continue;
									}
									
									_list.add(CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI));
								}
							}
						}
					} else {
						_list.add(CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE));
					}
					
					ossBean.setObligationType(CommonFunction.checkObligationSelectedLicense(_list));
					ossBean.setBomWithAndroidFlag(CoConstDef.FLAG_YES);
				}
				
				projectMapper.updateSrcOssList(ossBean);
				deleteRows.add(ossBean.getComponentId());
				
				//????????????????????? ??????
				if(CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())){
					List<String> duplicateLicense = new ArrayList<String>();
					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(ossBean.getComponentId().equals(comLicense.getComponentId())){
								// multi license oss??? license??? ????????? ??????, license ?????? ???????????? ?????? ????????? ??????
								if((isEmpty(comLicense.getLicenseName()) 
										&& isEmpty(comLicense.getLicenseText()) 
										&& isEmpty(comLicense.getOssCopyright())) 
									|| duplicateLicense.contains(comLicense.getLicenseId())) {
									continue;
								}
								
								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
								duplicateLicense.add(comLicense.getLicenseId());
								
								// ???????????? ??????
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else { //???????????????????????????
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
					// ???????????? ??????
					projectMapper.registComponentLicense(license);
				}
			} else { //insert
				//ossComponents ??????
				String exComponentId = ossBean.getGridId();
				ossBean.setReferenceId(refId);
				ossBean.setReferenceDiv(refDiv);
				
				// android project??? ??????, bom ????????? ?????? ?????? ?????????, bom save?????? ???????????? obligation type??? ????????? ??????????????????.
				if(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv)) {
					List<OssComponentsLicense> _list = new ArrayList<>();
					
					if(CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())) {

						for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
							for (ProjectIdentification comLicense : comLicenseList) {
								String gridId = comLicense.getGridId();
								
								if(isEmpty(gridId)) {
									continue;
								}
								
								gridId = gridId.split("-")[0];
								
								if(exComponentId.equals(comLicense.getComponentId())
										|| exComponentId.equals(gridId)){
									_list.add(CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI));
								}
							}
						}
					} else {
						_list.add(CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE));
					}
					
					ossBean.setObligationType(CommonFunction.checkObligationSelectedLicense(_list));
					ossBean.setBomWithAndroidFlag(CoConstDef.FLAG_YES);
				}
				
				// insert??? ?????? max idx??? select ?????? 
				ossBean.setComponentIdx(Integer.toString(ossComponentIdx++));
				projectMapper.insertSrcOssList(ossBean);
				deleteRows.add(ossBean.getComponentId());
				
				//????????????????????? ??????
				if(CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())){
					List<String> duplicateLicense = new ArrayList<String>();
					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							String gridId = comLicense.getGridId();
							
							if(isEmpty(gridId)) {
								continue;
							}
							
							gridId = gridId.split("-")[0];
							
							if(exComponentId.equals(comLicense.getComponentId()) || exComponentId.equals(gridId)){
								// multi license oss??? license??? ????????? ??????, license ?????? ???????????? ?????? ????????? ??????
								if((isEmpty(comLicense.getLicenseName()) 
									&& isEmpty(comLicense.getLicenseText()) 
									&& isEmpty(comLicense.getOssCopyright())) 
										|| duplicateLicense.contains(comLicense.getLicenseName())) {
									continue;
								}
								
								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
								// ???????????? ID ??????
								license.setComponentId(ossBean.getComponentId());
								duplicateLicense.add(comLicense.getLicenseName()); 
								
								// ???????????? ??????
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else { // ???????????????????????????
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
					// ???????????? ??????
					projectMapper.registComponentLicense(license);
				}
			}
		}
		
		// delete
		OssComponents param = new OssComponents();
		param.setReferenceDiv(refDiv);
		param.setReferenceId(refId);
		param.setOssComponentsIdList(deleteRows);
		
		projectMapper.deleteOssComponentsWithIds(param);
	}
	
	@Transactional
	private void addOssComponentByBinaryInfo(List<OssComponents> componentList, Map<String, List<Map<String, Object>>> binaryRegInfoMap) {
		for(OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			String componentId = bean.getComponentId();
			
			if(isEmpty(binaryName)) {
				continue;
			}
			
			if(!binaryRegInfoMap.containsKey(binaryName)) {
				continue;
			}
			
			List<Map<String, Object>> binaryInfoList = (List<Map<String, Object>>) binaryRegInfoMap.get(binaryName);
			
			boolean addOssComponentFlag = false;
			for(Map<String, Object> binaryInfo : binaryInfoList) {
				if(binaryInfo.containsKey("ossName")) {
					Map<String, OssMaster> ossInfo = CoCodeManager.OSS_INFO_UPPER;
					String ossName = (String) binaryInfo.get("ossName");
					String ossVersion = "";
					String _binaryLicenseStr = "";
					
					if(binaryInfo.containsKey("ossVersion")) {
						ossVersion = (String) binaryInfo.get("ossVersion");
					}
					
					if(binaryInfo.containsKey("license")) {
						_binaryLicenseStr = (String) binaryInfo.get("license");
					}
					
					
					String key = ossName + "_" + ossVersion;
					
					OssMaster ossBean = ossInfo.get(key.toUpperCase());
					boolean isEmptyOss = (ossBean == null || "-".equals(ossName));
					
					// update??? ??????
					// ossmaster => projectIdentification ?????? ??????
					ProjectIdentification updateBean = new ProjectIdentification();
					
					if(ossBean != null) {
						if(!isEmptyOss) {
							updateBean.setOssId(ossBean.getOssId());
						}
						
						updateBean.setOssName(isEmptyOss ? "-" : ossBean.getOssNameTemp()); // nick name?????? ???????????? ????????? ???????????????, ??????????????? ??????(temp)
						updateBean.setOssVersion(isEmptyOss ? ossVersion : ossBean.getOssVersion());
						updateBean.setDownloadLocation(isEmptyOss ? "" : ossBean.getDownloadLocation());
						updateBean.setHomepage(isEmptyOss ? "" : ossBean.getHomepage());
						updateBean.setCopyrightText(isEmptyOss ? "" : ossBean.getCopyright());
					} else {
						updateBean.setOssId(null);
						updateBean.setOssName(ossName); // nick name?????? ???????????? ????????? ???????????????, ??????????????? ??????(temp)
						updateBean.setOssVersion(ossVersion);
						updateBean.setDownloadLocation(null);
						updateBean.setHomepage(null);
						updateBean.setCopyrightText(null);
					}
					
					// ????????? ??????
					updateBean.setFilePath(bean.getFilePath()); 
					updateBean.setExcludeYn(bean.getExcludeYn());
					updateBean.setBinaryName(bean.getBinaryName());
					updateBean.setBinaryNotice(bean.getBinaryNotice());
					updateBean.setCustomBinaryYn(bean.getCustomBinaryYn());
					
					// ????????? binary??? ????????? ???????????? OSS??? ????????? ??????, ?????? ????????? ?????????????????? ??????????????? ?????? ????????????.
					if(addOssComponentFlag) {
						updateBean.setReferenceId(bean.getReferenceId());
						updateBean.setReferenceDiv(bean.getReferenceDiv());
						
						projectMapper.insertOssComponents(updateBean);
						
						componentId = updateBean.getComponentId();
					} else {
						updateBean.setComponentId(componentId);
						projectMapper.updateSrcOssList(updateBean);
						addOssComponentFlag = true;
						
						// ?????? license ??????
						projectMapper.deleteOssComponentsLicense(bean);
					}
						
					List<String> selectedLicenseIdList = new ArrayList<>();
					
					if(!isEmpty(_binaryLicenseStr)) {
						for(String _licenseName : _binaryLicenseStr.split(",")) {
							if(isEmpty(_licenseName)) {
								continue;
							}
							
							_licenseName = _licenseName.trim();
							
							String _licenseId = CommonFunction.getLicenseIdByName(_licenseName);
							
							if(!isEmpty(_licenseId)) {
								selectedLicenseIdList.add(_licenseId);
							}
						}
					}
					
					List<OssComponentsLicense> updateLicenseList = new ArrayList<>();
						
					// oss name??? ???????????? ????????????, OSS List??? ????????? ????????? ???????????? ??????
					if(!isEmptyOss) {
						boolean hasSelectedLicense = false;
						
						for(OssLicense license : ossBean.getOssLicenses()) {
							OssComponentsLicense componentLicense = new OssComponentsLicense();
							
							componentLicense.setComponentId(componentId);
							componentLicense.setLicenseId(license.getLicenseId());
							componentLicense.setLicenseName(license.getLicenseName());
							// license text ????????? ????????????
							
							if(selectedLicenseIdList.contains(componentLicense.getLicenseId())) {
								hasSelectedLicense = true;
								componentLicense.setExcludeYn(CoConstDef.FLAG_NO);
							}
							
							updateLicenseList.add(componentLicense);
						}
						
						for(OssComponentsLicense license : updateLicenseList) {
							if(hasSelectedLicense) {
								license.setExcludeYn(avoidNull(license.getExcludeYn(), CoConstDef.FLAG_YES));
							} else {
								license.setExcludeYn(CoConstDef.FLAG_NO);
							}
							
							projectMapper.insertOssComponentsLicense(license);
						}
					} else {
						// oss name??? ?????????????????? ???????????? binary db??? ????????? license ????????? ????????? ????????????.
						// ????????? ????????? license??? ???????????? ???????????? ?????? ??? ??????
						OssComponentsLicense license = new OssComponentsLicense();
						license.setExcludeYn(CoConstDef.FLAG_NO);
						license.setComponentId(componentId);
						
						// binary db??? ????????? license ????????? license master??? ???????????? ????????? master ????????? ??????
						if(!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
							LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_binaryLicenseStr.toUpperCase().trim());
							license.setLicenseId(licenseMaster.getLicenseId());
							license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName()));
						} else {
							// ???????????? ?????? ?????????, license name??? ????????????.
							// ????????? ????????? ????????? ????????? ??? ??????
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
		for(OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			
			if(binaryName.indexOf("/") > -1) {
				binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
			}
			
			if(isEmpty(binaryName)) {
				continue;
			}
			
			// ???????????? ????????? oss??? ????????? ???????????? ??????
			if(!isEmpty(bean.getOssName())) {
				continue;
			}
			
			if(!binaryRegInfoMap.containsKey(binaryName)) {
				continue;
			}
			
			List<Map<String, Object>> binaryInfoList = binaryRegInfoMap.get(binaryName);
			
			boolean addOssComponentFlag = false;
			
			for(Map<String, Object> binaryInfo : binaryInfoList) {
				if(binaryInfo.containsKey("ossName")) {
					Map<String, OssMaster> ossInfo = CoCodeManager.OSS_INFO_UPPER;
					String ossName = (String) binaryInfo.get("ossName");
					String ossVersion = "";
					String _binaryLicenseStr = "";
					
					if(binaryInfo.containsKey("ossVersion")) {
						ossVersion = (String) binaryInfo.get("ossVersion");
					}
					
					if(binaryInfo.containsKey("license")) {
						_binaryLicenseStr = (String) binaryInfo.get("license");
					}
					
					String key = ossName + "_" + ossVersion;
					
					if("-".equals(ossName) || ossInfo.containsKey(key.toUpperCase())) {
						// oss name + version ??? ???????????? oss ??? ????????????, update ??????.
						boolean isEmptyOss = "-".equals(ossName);
						OssMaster ossBean = ossInfo.get(key.toUpperCase());
						
						// update??? ??????
						// ossmaster => projectIdentification ?????? ??????
						ProjectIdentification updateBean = new ProjectIdentification();
						
						if(!isEmptyOss) {
							updateBean.setOssId(ossBean.getOssId());
						}
						
						updateBean.setOssName(isEmptyOss ? "-" : ossBean.getOssNameTemp()); // nick name?????? ???????????? ????????? ???????????????, ??????????????? ??????(temp)
						updateBean.setOssVersion(isEmptyOss ? ossVersion : ossBean.getOssVersion());
						updateBean.setDownloadLocation(isEmptyOss ? "" : ossBean.getDownloadLocation());
						updateBean.setHomepage(isEmptyOss ? "" : ossBean.getHomepage());
						updateBean.setCopyrightText(isEmptyOss ? "" : ossBean.getCopyright());
						
						// ????????? ??????
						updateBean.setFilePath(bean.getFilePath()); 
						updateBean.setExcludeYn(bean.getExcludeYn());
						updateBean.setBinaryName(bean.getBinaryName());
						updateBean.setBinaryNotice(bean.getBinaryNotice());
						updateBean.setCustomBinaryYn(bean.getCustomBinaryYn());
						
						// ????????? binary??? ????????? ???????????? OSS??? ????????? ??????, ?????? ????????? ?????????????????? ??????????????? ?????? ????????????.
						if(addOssComponentFlag) {
							updateBean.setReferenceId(bean.getReferenceId());
							updateBean.setReferenceDiv(bean.getReferenceDiv());
							
							projectMapper.insertOssComponents(updateBean);
						} else {
							updateBean.setComponentId(bean.getComponentId());
							
							projectMapper.updateSrcOssList(updateBean);
							addOssComponentFlag = true;
							
							// ?????? license ??????
							projectMapper.deleteOssComponentsLicense(bean);
						}
						

						List<String> selectedLicenseIdList = new ArrayList<>();
						
						if(!isEmpty(_binaryLicenseStr)) {
							for(String _licenseName : _binaryLicenseStr.split(",")) {
								if(isEmpty(_licenseName)) {
									continue;
								}
								
								_licenseName = _licenseName.trim();
								String _licenseId = CommonFunction.getLicenseIdByName(_licenseName);
								
								if(!isEmpty(_licenseId)) {
									selectedLicenseIdList.add(_licenseId);
								}
							}
						}
						List<OssComponentsLicense> updateLicenseList = new ArrayList<>();
						
						// oss name??? ???????????? ????????????, OSS List??? ????????? ????????? ???????????? ??????
						if(!isEmptyOss) {
							boolean hasSelectedLicense = false;
							
							for(OssLicense license : ossBean.getOssLicenses()) {
								OssComponentsLicense componentLicense = new OssComponentsLicense();
								componentLicense.setComponentId(bean.getComponentId());
								componentLicense.setLicenseId(license.getLicenseId());
								componentLicense.setLicenseName(license.getLicenseName());
								
								// license text ????????? ????????????
								if(selectedLicenseIdList.contains(componentLicense.getLicenseId())) {
									hasSelectedLicense = true;
									componentLicense.setExcludeYn(CoConstDef.FLAG_NO);
								}
								
								updateLicenseList.add(componentLicense);
							}
							
							for(OssComponentsLicense license : updateLicenseList) {
								if(hasSelectedLicense) {
									license.setExcludeYn(avoidNull(license.getExcludeYn(), CoConstDef.FLAG_YES));
								} else {
									license.setExcludeYn(CoConstDef.FLAG_NO);
								}
								
								projectMapper.insertOssComponentsLicense(license);
							}
						} else {
							// oss name??? ?????????????????? ???????????? binary db??? ????????? license ????????? ????????? ????????????.
							// ????????? ????????? license??? ???????????? ???????????? ?????? ??? ??????
							OssComponentsLicense license = new OssComponentsLicense();
							license.setExcludeYn(CoConstDef.FLAG_NO);
							license.setComponentId(addOssComponentFlag ? updateBean.getComponentId() : bean.getComponentId());
							
							// binary db??? ????????? license ????????? license master??? ???????????? ????????? master ????????? ??????
							if(!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
								LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_binaryLicenseStr.toUpperCase().trim());
								license.setLicenseId(licenseMaster.getLicenseId());
								license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName()));
							} else {
								// ???????????? ?????? ?????????, license name??? ????????????.
								// ????????? ????????? ????????? ????????? ??? ??????
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
	public Map<String, List<String>> nickNameValid(List<ProjectIdentification> ossComponentList, List<List<ProjectIdentification>> ossComponentLicenseList) {
		List<String> ossNickNameCheckResult = new ArrayList<>();
		List<String> licenseNickNameCheckResult = new ArrayList<>();
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		List<String> ossCheckParam = new ArrayList<>();
		List<String> licenseCheckParam = new ArrayList<>();
		
		for(ProjectIdentification bean : ossComponentList) {
			String _ossName = avoidNull(bean.getOssName()).trim();
			int isAdminCheck = projectMapper.selectAdminCheckCnt(bean);
			
			if(!isEmpty(_ossName) && !"-".equals(_ossName) && !ossCheckParam.contains(_ossName) && isAdminCheck < 1) {
				ossCheckParam.add(_ossName);
			}
			
			if(CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())) {
				// ????????? ??? ?????? ??????
			} else {
				String _licenseName = avoidNull(bean.getLicenseName()).trim();
				if(!isEmpty(_licenseName) && !licenseCheckParam.contains(_licenseName)) {
					licenseCheckParam.add(_licenseName);
				}
			}
		}

		// multi license??? ?????? nickname check?????? ??????
		for (List<ProjectIdentification> licenseList : ossComponentLicenseList) {
			for (ProjectIdentification licenseBean : licenseList) {
				String _licenseName = avoidNull(licenseBean.getLicenseName()).trim();
				
				if(!isEmpty(_licenseName) && !licenseCheckParam.contains(_licenseName)) {
					licenseCheckParam.add(_licenseName);
				}
			}
		}
		
		List<OssMaster> ossNickNameList = null;
		
		if(!ossCheckParam.isEmpty()) {
			OssMaster param = new OssMaster();
			param.setOssNames(ossCheckParam.toArray(new String[ossCheckParam.size()]));
			ossNickNameList = projectMapper.checkOssNickName(param);
			
			if(ossNickNameList != null) {
				for(OssMaster bean : ossNickNameList) {
					String _disp = bean.getOssNickname() + " => " + bean.getOssName();
					
					if(!ossNickNameCheckResult.contains(_disp)) {
						ossNickNameCheckResult.add(_disp);
					}
				}
			}
		}
		
		if(!licenseCheckParam.isEmpty()) {
			for(String licenseName : licenseCheckParam) {
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
					if(licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
						for(String s : licenseMaster.getLicenseNicknameList()) {
							if(licenseName.equalsIgnoreCase(s)) {
								String disp = licenseName + " => " + avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseNameTemp());
								
								if(!licenseNickNameCheckResult.contains(disp)) {
									licenseNickNameCheckResult.add(disp);
									
									break;
								}
							}
						}
						
					}
				}
			}
		}
		
		result.put("OSS", ossNickNameCheckResult);
		result.put("LICENSE", licenseNickNameCheckResult);
		
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void registBom(String prjId, String merge, List<ProjectIdentification> projectIdentification) {
		// ???????????? ??????
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(prjId);
		identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
		identification.setMerge(merge);
		identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		identification.setSaveBomFlag(CoConstDef.FLAG_YES); // file path ??? groupping ?????? ??????, ????????? data ??????
		List<OssComponents> componentId = projectMapper.selectComponentId(identification);
		
		// ?????? bom ????????? ?????? ?????????????????? ?????? ????????????.
		if(componentId.size() > 0){
			for (int i = 0; i < componentId.size(); i++) {
				projectMapper.deleteOssComponentsLicense(componentId.get(i));
			}
			
			projectMapper.deleteOssComponents(identification);
		}
		
		Map<String, Object> mergeListMap = getIdentificationGridList(identification);
		
		if(mergeListMap != null && mergeListMap.get("rows") != null) {
			for(ProjectIdentification bean : (List<ProjectIdentification>)mergeListMap.get("rows")) {
				
				if((isEmpty(bean.getOssName()) || "-".equals(bean.getOssName())) // ossName??? ??????????????? '-' ?????? license??? multi license????????? bom??? merge?????? ??????. 
					&& bean.getOssComponentsLicenseList().size() > 1) {
					continue;
				}
				
				bean.setRefDiv(bean.getReferenceDiv());
				bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				bean.setRefComponentId(bean.getComponentId());
				bean.setAdminCheckYn(CoConstDef.FLAG_NO);
				bean.setPreObligationType(bean.getObligationType());
				
				// ????????? ????????? ??????
				for (ProjectIdentification gridData : projectIdentification) {
					// merge ?????? (src/bat/3rd) ??????
					if(gridData.getRefComponentId().contains(bean.getRefComponentId())){
						bean.setMergePreDiv(gridData.getMergePreDiv());
						
						// BOM??? ??????????????? obligation??? ?????? ????????? ??????
						// needs check??? ????????? ???????????? ???????????????.
						if(CoConstDef.FLAG_YES.equals(gridData.getAdminCheckYn())) {
							bean.setAdminCheckYn(gridData.getAdminCheckYn());
							
							if(CoConstDef.FLAG_YES.equals(gridData.getSource())) {
								bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
							} else if(CoConstDef.FLAG_YES.equals(gridData.getNotify())) {
								bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
							} else if(CoConstDef.FLAG_NO.equals(gridData.getNotify()) && CoConstDef.FLAG_NO.equals(gridData.getSource())) {
								bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK_SELECTED);
							}
						}
						
						break;
					}
				}
				
				bean = CommonFunction.findOssIdAndName(bean);
				
				// ???????????? ????????? ?????????
				projectMapper.registBomComponents(bean);
				List<OssComponentsLicense> licenseList = CommonFunction.findOssLicenseIdAndName(bean.getOssId(), bean.getOssComponentsLicenseList());
				
				for(OssComponentsLicense licenseBean : licenseList) {
					licenseBean.setComponentId(bean.getComponentId());
					
					projectMapper.registComponentLicense(licenseBean);
				}
			}
		}
			
		// identification ????????? ?????? ?????? ???????????? ??????
		Project _tempPrjInfo = new Project();
		_tempPrjInfo.setPrjId(prjId);
		_tempPrjInfo = projectMapper.selectProjectMaster2(_tempPrjInfo);
		
		if (isEmpty(_tempPrjInfo.getIdentificationStatus())) {
			_tempPrjInfo.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			
			projectMapper.updateIdentifcationProgress(_tempPrjInfo);
		}
	}
	
	@Override
	public void checkProjectReviewer(Project project) {
		Project param = new Project();
		param.setPrjId(project.getPrjId());
		param = projectMapper.selectProjectMaster2(param);
		
		//review ????????? ????????? reviewer??? ???????????? ?????? ?????? ??????, reviewer??? ???????????? ??????.
		if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getIdentificationStatus())) {
			if(isEmpty(param.getReviewer())) {
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
	
	@Override
	@Transactional
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public Map<String, Object> updateProjectStatus(Project project) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		
		String commentDiv = isEmpty(project.getReferenceDiv()) ? CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS
				: project.getReferenceDiv();
		
		String userComment = project.getUserComment();
		String statusCode = project.getIdentificationStatus();
		
		if(isEmpty(statusCode)) {
			statusCode = project.getVerificationStatus();
		}
		
		String status = CoCodeManager.getCodeExpString(CoConstDef.CD_IDENTIFICATION_STATUS, statusCode);
		String mailType = null;
		log.info("statusCode : " + statusCode + "/  status : " + status);
		
		log.debug("PARAM: " + "identificationStatus="+project.getIdentificationStatus());
		log.debug("PARAM: " + "completeYn="+project.getCompleteYn());
		
		// Identification confirm??? validation check ??????
		if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(project.getIdentificationStatus())) {
			boolean isAndroidModel = false;
			boolean isNetworkRestriction = false;
			boolean hasSourceOss = false;
			boolean hasNotificationOss = false; 
			
			Map<String, Object> map = null;
			
			// confirm ??? ?????? DB Data??? ???????????? ????????????.
			ProjectIdentification param = new ProjectIdentification();
			param.setReferenceId(project.getPrjId());
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
//					return makeJsonResponseHeader(vr.getValidMessageMap());
					resultMap.put("validMap", vr.getValidMessageMap());
					return resultMap;
				}
				
				String networkRedistribution = CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, CoConstDef.CD_LICENSE_NETWORK_RESTRICTION);
				
				for(ProjectIdentification _projectBean : (List<ProjectIdentification>) map.get("rows")) {
					if(hasSourceOss && hasNotificationOss && isNetworkRestriction) {
						break;
					}
					
					if(!hasNotificationOss) {
						if(!CoConstDef.FLAG_YES.equals(_projectBean.getExcludeYn()) && ("10".equals(_projectBean.getObligationType()) || "11".equals(_projectBean.getObligationType()) )) {
							hasNotificationOss = true;
						}
					}
					
					if(!hasSourceOss) {
						if("11".equals(_projectBean.getObligationType())){
							hasSourceOss = true;
						}
					}
					
					if(!isNetworkRestriction) {
						if(_projectBean.getRestriction().toUpperCase().contains(networkRedistribution.toUpperCase())) {
							isNetworkRestriction = true;
						}
					}
				}
			}

			Project prjInfo = null;
			
			{
				// ANDROID PROJECT??? ??????
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
						isAndroidModel = true;
						T2CoProjectValidator pv = new T2CoProjectValidator();
						pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);

						pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
						pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
						T2CoValidationResult vr = pv.validate(new HashMap<>());
						
						// return validator result
						if (!vr.isValid()) {
//							return makeJsonResponseHeader(false, getMessage("msg.project.android.valid"));
							resultMap.put("androidMessage", getMessage("msg.project.android.valid"));
							return resultMap;
						}
					}
				}
			}
			
			if(CoConstDef.FLAG_YES.equals(prjInfo.getNetworkServerType())) {
				if(!isNetworkRestriction) {
					project.setSkipPackageFlag(CoConstDef.FLAG_YES);
					project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
					project.setDestributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				}
			} else {
				if (isAndroidModel) {
					project.setAndroidFlag(CoConstDef.FLAG_YES);
				} else if (!hasNotificationOss) {
					// Android model??? ???????????? bom ????????? ?????? ??????
					// package, distribute??? N/A ????????????.
					project.setSkipPackageFlag(CoConstDef.FLAG_YES);
					project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
					project.setDestributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				}
			}
			
			if(CoConstDef.CD_NOTICE_TYPE_NA.equals(prjInfo.getNoticeType())) {
				if(!hasSourceOss) {
					project.setSkipPackageFlag(CoConstDef.FLAG_YES);
					project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
					project.setDestributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				}
			}
			
			project.setModifier(project.getLoginUserName());
			updateProjectIdentificationConfirm(project);
			
			// network server ????????? notice ?????? ????????? ?????? ??????
			if( hasNotificationOss
					&& CoConstDef.FLAG_NO.equals(avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE,
							prjInfo.getDistributionType())).trim().toUpperCase())
					&& verificationService.checkNetworkServer(prjInfo.getPrjId()) ) {
				project.setSkipPackageFlag(CoConstDef.FLAG_YES);
				project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA);
				project.setDestributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
				updateIdentificationConfirmSkipPackaing(project);
				
				hasNotificationOss = false;
			}

			// permissive?????? ??????????????????, notice type??? ????????? ??????, ?????? packaging review?????????
			// ????????????.
			if ((!isAndroidModel && !hasNotificationOss) 
					|| (CoConstDef.FLAG_YES.equals(prjInfo.getNetworkServerType()) && !isNetworkRestriction) // Network service Only : yes ????????? network restriction??? ?????? case 
					|| (CoConstDef.CD_NOTICE_TYPE_NA.equals(prjInfo.getNoticeType()) && !hasSourceOss)) { // OSS Notice??? N/A????????? packaging??? ?????? ?????? ??????
				// do nothing
				mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY;
			} else {
				String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF));
				userComment = avoidNull(userComment) + "<br />" + _tempComment;
				mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF;
			}
		} else if (!isEmpty(project.getCompleteYn())) {
			// project complete ???
			updateProjectMaster(project);
			
			String _tempComment = "";
			
			if(CoConstDef.FLAG_YES.equals(project.getCompleteYn())) {
				_tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_COMPLETED));
				userComment =  avoidNull(userComment) + "<br />" + _tempComment;
			}
			
			// complete log ??????
			commentDiv = CoConstDef.CD_DTL_COMMENT_PROJECT_HIS;
			status = CoConstDef.FLAG_YES.equals(project.getCompleteYn()) ? "Completed" : "Reopened";
			
			// complete mail ??????
			mailType = CoConstDef.FLAG_YES.equals(project.getCompleteYn()) ? CoConstDef.CD_MAIL_TYPE_PROJECT_COMPLETED : CoConstDef.CD_MAIL_TYPE_PROJECT_REOPENED;
		} else if(!isEmpty(project.getDropYn())){
			// project drop ???
			updateProjectMaster(project);
			
			String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_DROPPED));
				userComment = avoidNull(userComment) + "<br />" + _tempComment;
			
			// complete log ??????
			commentDiv = CoConstDef.CD_DTL_COMMENT_PROJECT_HIS;
			status = "Dropped";
			
			// complete mail ??????
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
				
				// Admin ???????????? ?????? ????????? ????????? request review ??????????????? ??????
				if(CommonFunction.isAdmin()) {
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
			
			// ???????????? reject?????? ????????? validation check ???????????? ??????
			if(!ignoreValidation) {
				// Identification Reqeust review??? ??????, ?????? ?????? ?????? ??????
				Map<String, Object> map = null;
				// confirm ??? ?????? DB Data??? ???????????? ????????????.
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
//							return makeJsonResponseHeader(false, null, vr.getValidMessageMap(), vr.getDiffMessageMap());
							resultMap.put("diffMap", vr.getDiffMessageMap());
						}
//						return makeJsonResponseHeader(false, null, vr.getValidMessageMap());
						resultMap.put("validMap", vr.getValidMessageMap());
						return resultMap;
					}
				}
				
				Project prjInfo = null;
				
				{
					// ANDROID PROJECT??? ??????
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
			
			if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getIdentificationStatus())) {
				checkProjectReviewer(project);
			}
			
			projectMapper.updateProjectMaster(project);
			
			if(!isEmpty(project.getVerificationStatus())) {
				verificationService.updateProjectAllowDownloadBitFlag(project);
			}
		}
		
		resultMap.put("mailType", mailType);
		resultMap.put("userComment", userComment);
		resultMap.put("commentDiv", commentDiv);
		resultMap.put("status", status);
		
		return resultMap;
	}
	
	@Override
	@Transactional
	public void updateProjectIdentificationConfirm(Project project) {
		// oss id ??????
		projectMapper.updateComponentsOssId(project);
		// downlaod location, homepage??? master ????????? ??????
		projectMapper.updateComponentsOssInfo(project);
		// license id ??????
		projectMapper.updateComponentsLicenseId(project);
		// license id ?????? ????????? license text, copyright ????????? confirm ????????? oss master??? ???????????? ?????? ?????? ????????? ???????????? ??????.
		projectMapper.updateComponentsLicenseInfo(project);
		// ?????? ?????? (packaging, distribute ????????? ????????? ?????? ????????? ?????? service??? ???????????? ?????? confirm??? ????????? ????????? ??????)
		projectMapper.updateIdentificationConfirm(project);
		
		// Identification confirm??? OSS??? ???????????? ?????? Tab??? ???????????? N/A ??????
		projectMapper.updateProjectStatusWithComplete(project);
		
		// packaging ?????? ????????? reference_div = "50" ??? ????????? ??? ????????? confirm ???????????? ?????? notice ?????? data??? ?????? 50????????? copy??????.
		// ????????? ?????????  data??? ?????? ??? ?????? ????????? ?????? ??? ??????
		{
			// ????????? ???????????? ?????? data??? ???????????? ?????? path????????? ?????? ??????????????? ????????? verify ?????? ????????? ????????????.
			List<OssComponents> oldPackagingList = verificationService.getVerifyOssList(project);
			Map<String, OssComponents> oldPackageInfoMap = new HashMap<>();
			
			if(oldPackagingList != null && !oldPackagingList.isEmpty()) {
				// key value ????????????
				// key = ref + oss name + oss version + license name
				for(OssComponents oldBean : oldPackagingList) {
					if(!isEmpty(oldBean.getFilePath())) {
						String key = oldBean.getReferenceDiv() + "|" + oldBean.getOssId() + "|" + oldBean.getLicenseName();
						
						oldPackageInfoMap.put(key, oldBean);
					}
				}
			}
			
			// 1) packaging components delete ??????
			ProjectIdentification delParam = new ProjectIdentification();
			delParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PACKAGING);
			delParam.setReferenceId(project.getPrjId());
			List<OssComponents> componentId = projectMapper.selectComponentId(delParam);
			
			for (int i = 0; i < componentId.size(); i++) {
				projectMapper.deleteOssComponentsLicense(componentId.get(i));
			}
			
			projectMapper.deleteOssComponents(delParam);
			
			// 2) get bom list
			ProjectIdentification bomParam = new ProjectIdentification();
			bomParam.setReferenceId(project.getPrjId());
			bomParam.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
			bomParam.setMerge(CoConstDef.FLAG_NO);
			bomParam.setNoticeFlag(CoConstDef.FLAG_YES); // notice ????????? ????????????. (obligation type??? 10, 11)
			bomParam.setSaveBomFlag(CoConstDef.FLAG_YES);
			bomParam.setBomWithAndroidFlag(project.getAndroidFlag()); // android Project
			List<ProjectIdentification> bomList = projectMapper.selectBomList(bomParam);
			
			// ?????? ????????? ?????? ?????? data??? component id ??? ????????????.
			List<String> groupingList = new ArrayList<>(); // ???????????? row (??????) ??? ?????????
			List<String> componentList = new ArrayList<>();
			
			if(bomList != null) {
				for(ProjectIdentification bean : bomList) {
					if(groupingList.contains(bean.getGroupingColumn())) {
						continue;
					}
					
					if(CoConstDef.FLAG_YES.equals(bean.getAdminCheckYn()) && ("11".equals(bean.getObligationType()) || "10".equals(bean.getObligationType()))) {
						componentList.add(bean.getComponentId()+"-"+bean.getAdminCheckYn());
					}else {
						componentList.add(bean.getComponentId());
					}
				}
			}
			
			// ??????????????? ????????? ?????? ?????? ?????? ??????
			if(!componentList.isEmpty()) {
				for(String refComponentId : componentList) {
					OssComponents copyParam = new OssComponents();
					copyParam.setReferenceId(project.getPrjId());
					copyParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PACKAGING);
					copyParam.setExcludeYn(CoConstDef.FLAG_NO);
					copyParam.setAndroidFlag(project.getAndroidFlag());
					
					if(refComponentId.contains("-")) {
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
			
			if(oldPackageInfoMap != null && !oldPackageInfoMap.isEmpty()) {
				List<OssComponents> afterPackagingList = verificationService.getVerifyOssList(project);
				
				if(afterPackagingList != null && !afterPackagingList.isEmpty()) {
					// key value ????????????
					// key = ref + oss name + oss version + license name
					for(OssComponents newBean : afterPackagingList) {
						String key = newBean.getReferenceDiv() + "|" + newBean.getOssId() + "|" + newBean.getLicenseName();
						
						if(oldPackageInfoMap.containsKey(key)) {
							newBean.setFilePath(oldPackageInfoMap.get(key).getFilePath());
							projectMapper.updateFilePath(newBean);
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
		
		if(CoConstDef.FLAG_YES.equals(project.getCompleteYn())) {
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
		
		if("ROLE_USER".equals(partnerMaster.getLoginUserRole())){
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
		
		if(list != null) {
			for(Project bean : list) {
				// distribution type code ??????
				if(!isEmpty(bean.getDistributionType())) {
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
		// ???????????? ????????? ???????????? ?????????
		ProjectIdentification deleteparam = new ProjectIdentification();
		deleteparam.setReferenceId(prjId);
		deleteparam.setReferenceDiv(prjYn?CoConstDef.CD_DTL_COMPONENT_ID_BAT:CoConstDef.CD_DTL_COMPONENT_PARTNER_BAT); // 3rd ??????
		List<OssComponents> componentsId = projectMapper.selectComponentId(deleteparam);
		
		for(int j = 0; j < componentsId.size(); j++){
			projectMapper.deleteOssComponentsLicense(componentsId.get(j));
		}
		
		// // ????????? ????????? ???????????? ????????? BAT ????????????????????? N?????? N ?????? null
		if(ossComponents.size()==0 && prjYn){
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(prjId);
			
			if(!StringUtil.isEmpty(identificationSubStatusBat)){
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
		
		// ???????????? ??????
		for (int i = 0; i < ossComponents.size(); i++) {
			// BAT STATUS ??????
			if(i==0 && prjYn){
				Project projectStatus = new Project();
				projectStatus.setPrjId(prjId);
				projectStatus = projectMapper.selectProjectMaster(projectStatus);
				
				// ?????? ???????????? PROG
				if (StringUtil.isEmpty(projectStatus.getIdentificationStatus())) {
					projectStatus.setIdentificationStatus("PROG");
				}
				
				// ???????????? ????????? BAT ????????????????????? N ?????? N ?????? Y
				if(!StringUtil.isEmpty(identificationSubStatusBat)) {
					projectStatus.setIdentificationSubStatusBat(identificationSubStatusBat);
				} else {
					projectStatus.setIdentificationSubStatusBat(CoConstDef.FLAG_YES);
				}
				
				projectStatus.setModifier(projectStatus.getLoginUserName());
				projectStatus.setModifiedDate(projectStatus.getCreatedDate());
				
				projectMapper.updateProjectMaster(projectStatus);
			}
			
			//update
			if(!StringUtil.contains(ossComponents.get(i).getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
				//ossComponents ??????
				projectMapper.updateSrcOssList(ossComponents.get(i));
				deleteRows.add(ossComponents.get(i).getComponentId());
				
				//????????????????????? ??????
				if("M".equals(ossComponents.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentsLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(ossComponents.get(i).getComponentId().equals(comLicense.getComponentId())){
								OssComponentsLicense license = new OssComponentsLicense();
								// ???????????? ID ??????
								license.setComponentId(comLicense.getComponentId());
								
								// ???????????? ID ??????
								if (StringUtil.isEmpty(comLicense.getLicenseId())) {
									license.setLicenseId(CommonFunction.getLicenseIdByName(comLicense.getLicenseName()));
								} else {
									license.setLicenseId(comLicense.getLicenseId());
								}
								
								// ?????? ??????
								license.setLicenseName(comLicense.getLicenseName());
								license.setLicenseText(comLicense.getLicenseText());
								license.setCopyrightText(comLicense.getCopyrightText());
								license.setExcludeYn(avoidNull(comLicense.getExcludeYn(), CoConstDef.FLAG_NO));
								
								// ???????????? ??????
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else{ // ???????????????????????????
					OssComponentsLicense license = new OssComponentsLicense();
					// ???????????? ID ??????
					license.setComponentId(ossComponents.get(i).getComponentId());
					
					// ???????????? ID ??????
					if (StringUtil.isEmpty(ossComponents.get(i).getLicenseId())) {
						license.setLicenseId(CommonFunction.getLicenseIdByName(ossComponents.get(i).getLicenseName()));
					} else {
						license.setLicenseId(ossComponents.get(i).getLicenseId());
					}
					
					// ?????? ??????
					license.setLicenseName(ossComponents.get(i).getLicenseName());
					license.setLicenseText(ossComponents.get(i).getLicenseText());
					license.setCopyrightText(ossComponents.get(i).getCopyrightText());
					license.setExcludeYn(CoConstDef.FLAG_NO);
					
					// ???????????? ??????
					projectMapper.registComponentLicense(license);
				}
			} else { //insert
				//ossComponents ??????
				String exComponentId = ossComponents.get(i).getGridId();
				ossComponents.get(i).setReferenceId(prjId);
				ossComponents.get(i).setReferenceDiv(prjYn?"12":"20"); // 3rd ??????
				ossComponents.get(i).setComponentIdx(Integer.toString(ossComponentIdx++));
				projectMapper.insertSrcOssList(ossComponents.get(i));
				deleteRows.add(ossComponents.get(i).getComponentId());
				
				//????????????????????? ??????
				if("M".equals(ossComponents.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentsLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(exComponentId.equals(comLicense.getComponentId())){
								OssComponentsLicense license = new OssComponentsLicense();
								// ???????????? ID ??????
								license.setComponentId(projectMapper.selectLastComponent());
								
								// ???????????? ID ??????
								if (StringUtil.isEmpty(comLicense.getLicenseId())) {
									license.setLicenseId(CommonFunction.getLicenseIdByName(comLicense.getLicenseName()));
								} else {
									license.setLicenseId(comLicense.getLicenseId());
								}
								
								// ?????? ??????
								license.setLicenseName(comLicense.getLicenseName());
								license.setLicenseText(comLicense.getLicenseText());
								license.setCopyrightText(comLicense.getCopyrightText());
								license.setExcludeYn(avoidNull(comLicense.getExcludeYn(), CoConstDef.FLAG_NO));
								
								// ???????????? ??????
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else { //???????????????????????????
					OssComponentsLicense license = new OssComponentsLicense();
					// ???????????? ID ??????
					license.setComponentId(projectMapper.selectLastComponent());
					
					// ???????????? ID ??????
					if (StringUtil.isEmpty(ossComponents.get(i).getLicenseId())) {
						license.setLicenseId(CommonFunction.getLicenseIdByName(ossComponents.get(i).getLicenseName()));
					} else {
						license.setLicenseId(ossComponents.get(i).getLicenseId());
					}
					
					// ?????? ??????
					license.setLicenseName(ossComponents.get(i).getLicenseName());
					license.setLicenseText(ossComponents.get(i).getLicenseText());
					license.setCopyrightText(ossComponents.get(i).getCopyrightText());
					license.setExcludeYn(CoConstDef.FLAG_NO);
					
					// ???????????? ??????
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
		
		ossComponents.setReferenceDiv(avoidNull(ossComponents.getReferenceDiv(), CoConstDef.CD_DTL_COMPONENT_PARTNER));
		List<OssComponents> list = projectMapper.getPartnerOssList(ossComponents);
		
		for(OssComponents oc : list){
			if(CoConstDef.FLAG_YES.equals(oc.getExcludeYn())){
				ProjectIdentification PI = new ProjectIdentification();
				PI.setComponentId(oc.getComponentId());
				List<ProjectIdentification> subGridData = projectMapper.identificationSubGrid(PI);
				if(!subGridData.isEmpty()) {
					PI = subGridData.get(0);
					
					oc.setLicenseName(PI.getLicenseName());
					oc.setLicenseText(PI.getLicenseText());
					oc.setCopyrightText(PI.getCopyrightText());
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
		List<ProjectIdentification> result = new ArrayList<>();
		
		List<OssComponentsLicense> license = null;
		String licenseId = "";
		String licenseName = "";
		String licenseText = "";
		String copyrightText = "";
		
		// ?????? ???????????? ??????
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getComponentId() != null) {
				// ???????????? ???????????? ??????
				list.get(i).setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
				license = projectMapper.selectBomLicense(list.get(i));
				
				for (int j = 0; j < license.size(); j++){
					if(j == 0) {
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
				
				// oss Name??? ????????????, oss Version??? ???????????? ?????? case?????? ?????? ??????????????? ??????
				if(isEmpty(list.get(i).getCveId()) 
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
		
		for(ProjectIdentification oc : list){
			if(CoConstDef.FLAG_YES.equals(oc.getExcludeYn())){
				ProjectIdentification PI = new ProjectIdentification();
				PI.setComponentId(oc.getComponentId());
				List<ProjectIdentification> subGridData = projectMapper.identificationSubGrid(PI);
				if(!subGridData.isEmpty()) {
					PI = subGridData.get(0);
					
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
			// Identification, 3rd Party ??? OSS Table ?????? ?????? ?????? - Restriction ??????
			map.put("rows", CommonFunction.identificationSortByValidInfo(list, vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false, true));
			
			if(!vr.isValid()) {
				map.put("validData", vr.getValidMessageMap());
			}
			
			if(!vr.isDiff()) {
				map.put("diffData", vr.getDiffMessageMap());
			}
			
			if(vr.hasInfo()) {
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
		}
		
		resultMap.put("reportData", reportData);
		
		if(!validMap.isEmpty()) {
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
		
		// ?????? DB??? ???????????? ?????? ????????? CLIENT?????? ????????? ????????? SORT ??? ??? ???????????? ????????????.
		// TRIM, ??????????????? ????????????, ?????? ????????? ????????????.
		List<String> dbInfoList = projectMapper.checkChangedIdentification(prjId);
		
		Project projectInfo = new Project();
		projectInfo.setPrjId(prjId);
		projectInfo = projectMapper.selectProjectMaster2(projectInfo);
		
		List<String> dbPartnerList = new ArrayList<>();
		List<String> dbSrcList = new ArrayList<>();
		List<String> dbBinList = new ArrayList<>();

		List<String> partnerList = null;
		List<String> srcList = null;
		List<String> binList = null;

		// LICENSE NAME?????? ????????? SHORT IDENTIFIER??? ????????? ?????? ????????? ???????????? ?????? ????????? ?????? ????????? ???????????? ??????
		List<String> partnerList2 = null;
		List<String> srcList2 = null;
		List<String> binList2 = null;

		boolean partnerUseFlag = CoConstDef.FLAG_NO.equals(applicableParty);
		boolean srcUseFlag = CoConstDef.FLAG_NO.equals(applicableSrc);
		boolean binUseFlag = CoConstDef.FLAG_NO.equals(applicableBin);
		boolean dbPartnerUseFlag = CoConstDef.FLAG_NO.equals(projectInfo.getIdentificationSubStatusPartner());
		boolean dbSrcUseFlag = CoConstDef.FLAG_NO.equals(projectInfo.getIdentificationSubStatusSrc());
		boolean dbBinUseFlag = CoConstDef.FLAG_NO.equals(projectInfo.getIdentificationSubStatusBin());
		
		if(partnerUseFlag != dbPartnerUseFlag) {
			return getMessage("msg.project.checke.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)});
		} else if(srcUseFlag != dbSrcUseFlag) {
			return getMessage("msg.project.checke.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_SRC)});
		} else if(binUseFlag != dbBinUseFlag) {
			return getMessage("msg.project.checke.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_BIN)});
		}
		
 		if(dbInfoList != null && !dbInfoList.isEmpty()) {
 			for(String key : dbInfoList) {
 				if(!dbPartnerUseFlag && key.startsWith(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)) {
 					// 3rd??? ?????? ??????????????? ???????????? key??? ???????????? ????????? ???????????? ????????? ??????
 					if(!dbPartnerList.contains(key)) {
 						dbPartnerList.add(key);
 					}
 				} else if(!dbSrcUseFlag && key.startsWith(CoConstDef.CD_DTL_COMPONENT_ID_SRC)) {
 					dbSrcList.add(key);
 				} else if(!dbBinUseFlag && key.startsWith(CoConstDef.CD_DTL_COMPONENT_ID_BIN)) {
 					dbBinList.add(key);
 				}
 			}
		}
 		
		// ???????????? ?????? key??? convert
		// 3rd party??? ????????? ????????? ??????????????? ????????? ???????????? ????????? ???????????? ????????????.
 		if(!partnerUseFlag && partyData != null) {
 			partnerList = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, partyData, null);
 			partnerList2 = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, partyData, null, true);
 		}
 		
 		if(!srcUseFlag && srcData != null && srcSubData != null) {
 			srcList = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_SRC, srcData, srcSubData);
 			srcList2 = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_SRC, srcData, srcSubData, true);
 		}
 		
 		if(!binUseFlag && binData != null && binSubData != null) {
 			binList = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_BIN, binData, binSubData);
 			binList2 = makeCompareKey(CoConstDef.CD_DTL_COMPONENT_ID_BIN, binData, binSubData, true);
 		}
 		
 		if(partnerList == null) {
 			partnerList = new ArrayList<>();
 			partnerList2 = new ArrayList<>();
 		}
 		
 		if(srcList == null) {
 			srcList = new ArrayList<>();
 			srcList2 = new ArrayList<>();
 		}
 		
 		if(binList == null) {
 			binList = new ArrayList<>();
 			binList2 = new ArrayList<>();
 		}
 		
 		// 1) ?????? ?????? 
 		// 2) ????????? ???????????? ????????? sort??? text ??????
 		if(partnerList.size() != dbPartnerList.size() || !compareList(partnerList, partnerList2, dbPartnerList)) {
 			return getMessage("msg.project.checke.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)});
 		} else if(srcList.size() != dbSrcList.size() || !compareList(srcList, srcList2, dbSrcList)) {
 			return getMessage("msg.project.checke.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_SRC)});
 		} else if(binList.size() != dbBinList.size() || !compareList(binList, binList2, dbBinList)) {
 			return getMessage("msg.project.checke.changed", new String[]{CoCodeManager.getCodeString(CoConstDef.CD_COMPONENT_DIVISION, CoConstDef.CD_DTL_COMPONENT_ID_BIN)});
 		}
 		
 		return null;
	}
	
	private boolean compareList(List<String> list, List<String> list2, List<String> dbList) {
		if(list.size() != dbList.size()) {
			return false;
		}
		
		Collections.sort(list);
		Collections.sort(list2);
		Collections.sort(dbList);
		
		for(int i=0; i<list.size(); i++) {
			if(!list.get(i).equalsIgnoreCase(dbList.get(i)) && !list2.get(i).equalsIgnoreCase(dbList.get(i))) {
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
		
		if(data != null) {
			for(ProjectIdentification bean : data) {
				if(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(type)) {
					String key = type;
					key += "|" + avoidNull(bean.getOssName()).trim();
					key += "|" + avoidNull(bean.getOssVersion()).trim();
					key += "|" + avoidNull(bean.getRefPartnerId()).trim();
					key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
					key = key.toUpperCase();
					
					if(!list.contains(key)) {
						list.add(key);
					}
				} else if (subData != null) {
					if(CoConstDef.LICENSE_DIV_SINGLE.equals(avoidNull(bean.getLicenseDiv(), CoConstDef.LICENSE_DIV_SINGLE))) {
						String key = type;
						key += "|" + avoidNull(bean.getOssName()).trim();
						key += "|" + avoidNull(bean.getOssVersion()).trim();
						key += "|" + avoidNull(convertLicenseShortName(bean.getLicenseName(), convertShortLicenseName)).trim();
						key += "|" + avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO);
						key += "|" + CoConstDef.FLAG_NO;
						key = key.toUpperCase();
						
						list.add(key);
					} else {
						if(bean.getComponentLicenseList() != null) {
							for(ProjectIdentification license : bean.getComponentLicenseList()) {
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
							key += "|" + CoConstDef.FLAG_NO;
							key = key.toUpperCase();
							
							list.add(key);
						}						
					}
				}
			}
		}
		
		return list;
	}
	
	/**
	 * ???????????? ????????? ???????????? short identifier??? ?????? ???????????????, ????????????(?????? ?????????)?????????, short identifier??? ???????????? ?????? ????????? short identifier??? ????????????.
	 * ?????? ????????? ????????? ??????
	 * @param licenseName
	 * @param convertShortLicenseName
	 * @return
	 */
	private String convertLicenseShortName(String licenseName, boolean convertShortLicenseName) {
		if(convertShortLicenseName && !isEmpty(licenseName)) {
			if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
				LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
				
				if(master != null) {
					// ?????? ???????????? ?????? short identifier ?????? ?????? ????????? ??????
					if(licenseName.equals(master.getShortIdentifier())) {
						licenseName = master.getLicenseNameTemp();
					} else if(!isEmpty(master.getShortIdentifier())) {
						// ?????? ???????????? ?????? ?????? ?????? (?????? ?????????) ??? ?????? short identifier??? ??????
						licenseName = master.getShortIdentifier();
					}
				}
			}
		}
		
		return avoidNull(licenseName).trim();
	}
	
//	private List<ProjectIdentification> findLicense(String gridId, List<List<ProjectIdentification>> componentLicenseList) {
//		List<ProjectIdentification> licenseList = new ArrayList<>();
//		if(componentLicenseList != null && !componentLicenseList.isEmpty()) {
//			boolean breakFlag = false;
//			for(List<ProjectIdentification> list : componentLicenseList) {
//				for(ProjectIdentification bean : list) {
//					String key = gridId;
//					if(bean.getGridId().startsWith(key)) {
//						licenseList.add(bean);
//						breakFlag = true;
//					}
//				}
//				if(breakFlag) {
//					break;
//				}
//			}
//		}
//		return licenseList;
//	}
	
	@Override
	public Map<String, Map<String, String>> getProjectDownloadExpandInfo(Project param) {
		Map<String, Map<String, String>> resultMap = new HashMap<>();
		
		if(param.getPrjIdList() != null && !param.getPrjIdList().isEmpty()) {
			List<Map<String, String>> list = projectMapper.getProjectDownloadExpandInfo(param);
			
			if(list != null) {
				for(Map<String, String> map : list) {
					resultMap.put(String.valueOf(map.get("PRJ_ID")), map);
				}
			}
		}
		
		return resultMap;
	}

	@Override
	public void cancelFileDel(Project project) {
		if(project.getCsvFile() != null) {
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
		
		for(ProjectIdentification bean : ossComponentList) {
			String _ossName = avoidNull(bean.getOssName()).trim();
			int isAdminCheck = projectMapper.selectAdminCheckCnt(bean);
			
			if(!isEmpty(_ossName) && !"-".equals(_ossName) && !ossCheckParam.contains(_ossName) && isAdminCheck < 1) {
				ossCheckParam.add(_ossName);
			}
		}
		
		if(!ossCheckParam.isEmpty()) {
			OssMaster param = new OssMaster();
			param.setOssNames(ossCheckParam.toArray(new String[ossCheckParam.size()]));
			ossNickNameList = projectMapper.checkOssNickName(param);
			
			if(ossNickNameList != null) {
				for(OssMaster bean : ossNickNameList) {
					ossNickNameConvertMap.put(bean.getOssNickname().toUpperCase(), bean);
				}
			}
		}

		for(ProjectIdentification bean : ossComponentList) {
			if(ossNickNameConvertMap.containsKey(avoidNull(bean.getOssName()).trim().toUpperCase())) {
				bean.setOssName(ossNickNameConvertMap.get(avoidNull(bean.getOssName()).trim().toUpperCase()).getOssName());
			}
			
			// license nickname ??????
			if(CoConstDef.LICENSE_DIV_SINGLE.equals(bean.getLicenseDiv())) {
				String _licenseName = avoidNull(bean.getLicenseName()).trim();
				
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(_licenseName.toUpperCase())) {
					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_licenseName.toUpperCase());
					
					if(licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
						for(String s : licenseMaster.getLicenseNicknameList()) {
							if(_licenseName.equalsIgnoreCase(s)) {
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
		if(ossComponentLicenseList != null) {
			for(List<ProjectIdentification> licenseList : ossComponentLicenseList) {
				for (ProjectIdentification licenseBean : licenseList) {
					String _licenseName = avoidNull(licenseBean.getLicenseName()).trim();
					if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(_licenseName.toUpperCase())) {
						LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_licenseName.toUpperCase());
						
						if(licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
							for(String s : licenseMaster.getLicenseNicknameList()) {
								if(_licenseName.equalsIgnoreCase(s)) {
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
	public void addWatcher(Project project) {
		if(!isEmpty(project.getPrjEmail())) {
			if(projectMapper.existsWatcherByEmail(project) == 0) { // ?????? ????????? watcher ??????
				projectMapper.insertWatcher(project); // watcher ??????
				
				// email ??????
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
			if(projectMapper.existsWatcherByUser(project) == 0) { // ?????? ????????? watcher ??????
				projectMapper.insertWatcher(project); // watcher ??????
			}
		}
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
		
		if(i > 0){
			result = true;
		}	
		
		return result;
	}

	/**
	 * ??????????????? ???????????? 3rd party ?????? ??????
	 */
	@Override
	public String getPartnerFormatName(String partnerId) {
		if(!isEmpty(partnerId)) {
			PartnerMaster param = new PartnerMaster();
			param.setPartnerId(partnerId);
			param = partnerMapper.selectPartnerMaster(param);
			
			if(param != null) {
				return "("+partnerId+") "+ avoidNull(param.getPartnerName());
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
			for(Project bean : project.getModelList()) {
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

		if(i > 0){
			projectMapper.deleteAddList(project);
			
			result = true;
		}	
		
		return result;
	}

	@Override
	public void insertAddList(List<Project> project) {
		for(Project p : project){
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
		boolean ossNameEmptyFlag = false;
		
		for(ProjectIdentification info : gridData) {
			if(isEmpty(groupColumn)) {
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
			}
			
			if("-".equals(groupColumn)) {
				if("NA".equals(info.getLicenseType())) {
					ossNameEmptyFlag = true;
				}
			}
			
			if(groupColumn.equals(info.getOssName() + "-" + info.getOssVersion()) // ?????? groupColumn?????? ???????????? ??????
					&& !("-".equals(info.getOssName()) 
					&& !"NA".equals(info.getLicenseType()))
					&& !ossNameEmptyFlag) { // ???, OSS Name: - ?????????, License Type: Proprietary??? ?????? ?????? Row??? ????????? ??????.
				tempData.add(info);
			} else { // ?????? grouping
				setMergeData(tempData, resultGridData);
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
				tempData.clear();
				tempData.add(info);
			}
			
			ossNameEmptyFlag = false; // ?????????
		}
		
		setMergeData(tempData, resultGridData); // bom data??? loop??? ???????????? tempData??? ?????? ????????? ?????? ?????? merge??? ???.
		
		return resultGridData;
	}
	
	public static void setMergeData(List<ProjectIdentification> tempData, List<ProjectIdentification> resultGridData){
		if(tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<ProjectIdentification>() {
				@Override
				public int compare(ProjectIdentification o1, ProjectIdentification o2) {
					if(o1.getLicenseName().length() >= o2.getLicenseName().length()) { // license name??? ????????? bomList???????????? ?????? ????????? ?????????. license name??? ????????? ????????????
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			ProjectIdentification rtnBean = null;
			
			for(ProjectIdentification temp : tempData) {
				if(rtnBean == null) {
					rtnBean = temp;
					continue;
				}
				
				String key = temp.getOssName() + "-" + temp.getLicenseType();
				
				if("--NA".equals(key)) {
					if(!rtnBean.getLicenseName().contains(temp.getLicenseName())) {
						resultGridData.add(rtnBean);
						rtnBean = temp;
						continue;
					}
				}
				
				// ????????? oss name??? version??? ?????? license ????????? ?????????????????? merge ???.
				for(String licenseName : temp.getLicenseName().split(",")) {
					boolean equalFlag = false;
					
					for(String rtnLicenseName : rtnBean.getLicenseName().split(",")) {
						if(rtnLicenseName.equals(licenseName)) {
							equalFlag = true;
							break;
						}
					}
					
					if(!equalFlag) {
						rtnBean.setLicenseName(rtnBean.getLicenseName() + "," + licenseName);
					}
				}
				
				List<OssComponentsLicense> rtnComponentLicenseList = new ArrayList<OssComponentsLicense>();
				
				for(OssComponentsLicense list : temp.getOssComponentsLicenseList()) {
					int equalsItemList = (int) rtnBean.getOssComponentsLicenseList()
														.stream()
														.filter(e -> list.getLicenseName().equals(e.getLicenseName())) // ????????? licenseName??? filter
														.collect(Collectors.toList()) // return??? list?????????
														.size(); // ?????? list??? size
					
					if(equalsItemList == 0) {
						rtnComponentLicenseList.add(list);
					}
				}
				
				rtnBean.getOssComponentsLicenseList().addAll(rtnComponentLicenseList);
				
				if(!rtnBean.getRefComponentId().contains(temp.getRefComponentId())) {
					rtnBean.setRefComponentId(rtnBean.getRefComponentId() + "," + temp.getRefComponentId());
				}
				
				if(!rtnBean.getRefDiv().contains(temp.getRefDiv())) {
					rtnBean.setRefDiv(rtnBean.getRefDiv() + "," + temp.getRefDiv());
				}
				
				if(!isEmpty(temp.getRestriction())) {
					for(String restriction : temp.getRestriction().split("\\n")) {
						if(!rtnBean.getRestriction().contains(restriction)) {
							rtnBean.setRestriction(rtnBean.getRestriction() + "\\n" + restriction);
						}
					}
				}
				
				// ?????? tab??? ?????? Data??? ????????????, ?????? tab??? ?????? Data??? ?????? ???????????? ?????? ?????? ????????? ???????????? data??? ?????????. (?????? downloadLocation, homepage, copyrightText)
				if(isEmpty(rtnBean.getDownloadLocation())) { 
					if(!isEmpty(temp.getDownloadLocation())) {
						rtnBean.setDownloadLocation(temp.getDownloadLocation());
					}
				}
				
				if(isEmpty(rtnBean.getHomepage())) {
					if(!isEmpty(temp.getHomepage())) {
						rtnBean.setHomepage(temp.getHomepage());
					}
				}
				
				if(isEmpty(rtnBean.getCopyrightText())) {
					if(!isEmpty(temp.getCopyrightText())) {
						rtnBean.setCopyrightText(temp.getCopyrightText());
					}
				}
				
				if(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(temp.getObligationType())){
					rtnBean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
					rtnBean.setObligationLicense(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
					rtnBean.setPreObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
				} else if(CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(temp.getObligationType())
						&& ("").equals(avoidNull(rtnBean.getObligationType(), ""))){
					rtnBean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
					rtnBean.setObligationLicense(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
					rtnBean.setPreObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
				}
			}
			
			resultGridData.add(rtnBean);
		}
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
		
		if(mainData != null) {
			emptyBinaryPathCnt = mainData.stream()
											.filter(c -> isEmpty(c.getBinaryName()))
											.collect(Collectors.toList())
											.size();
		}
		
		if(validData != null) {
			errCnt = validData.keySet().stream()
								.filter(c -> c.toUpperCase().contains("OSS_NAME") 
												|| c.toUpperCase().contains("OSS_VERSION") 
												|| c.toUpperCase().contains("LICENSE_NAME"))
								.collect(Collectors.toList())
								.size();
		}
		
		if(diffData != null) {
			Map<String, Object> diffDataMap = new HashMap<String, Object>();
			for(String key : diffData.keySet()) {
				if(key.toUpperCase().contains("LICENSENAME")) {
					String diffMsg = (String) diffData.get(key);
					if(!diffMsg.contains("Declared")) {
						diffDataMap.put(key, diffData.get(key));
					}
				} else {
					diffDataMap.put(key, diffData.get(key));
				}
			}
			
			if(!diffDataMap.isEmpty()) {
				diffCnt = diffDataMap.keySet()
						.stream()
						.filter(c -> c.toUpperCase().contains("OSSNAME") 
										|| c.toUpperCase().contains("OSSVERSION") 
										|| c.toUpperCase().contains("LICENSENAME"))
						.collect(Collectors.toList())
						.size();
			}
		}
		
		// OSS Name, OSS Version, License??? Warning message(?????????, ?????????)??? ?????? Row ?????? Binary Name??? ????????? Row
		if(emptyBinaryPathCnt > 0 || errCnt > 0 || diffCnt > 0) {
			validMsg = "You can download NOTICE only if there is no warning message in OSS Name, OSS Version, License or Binary Name is not null.";
		}
		
		// ????????? Binary??? ?????? ??????(= ?????? ????????? ???????????? Row??? ?????? ??????)
		if(mainData.size() == 0 || diffData.size() == 0) {
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
		
		if(dir.exists()) {  // ?????? ?????? ??????( html file??? ?????????)
			for(File item : dir.listFiles()) {
				if(!item.isDirectory() && item.getName().contains(".html")){
					item.delete();
				} else if(item.isDirectory()) {
					CommonFunction.removeAll(item); // ????????????, file ?????? ??????
				}
			}
		}
		
		String LicensesfilePath = filePath + "/needtoadd-notice/LICENSES";
		dir = new File(LicensesfilePath);
		dir.mkdirs();
		
		Map<String, List<ProjectIdentification>> mergedBinaryData = getMergedBinaryData(paramMap);
		List<String> ObligationNoticeLicenseList = new ArrayList<String>();
		
		for(List<ProjectIdentification> bean : mergedBinaryData.values()) { // Licenses proc
			for(ProjectIdentification p : bean) {
				for(String licenseName : p.getLicenseName().split(",")) {
					if(!ObligationNoticeLicenseList.contains(licenseName)) {
						LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
						
						if(CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn())) {
							String LICENSEFileName = avoidNull(licenseBean.getShortIdentifier(), "");
							
							if(isEmpty(LICENSEFileName)) {
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
		
		for(String binaryPath : mergedBinaryData.keySet()) { // Binary proc
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("templateURL", CoCodeManager.getCodeExpString(CoConstDef.CD_NOTICE_DEFAULT, CoConstDef.CD_DTL_SUPPLMENT_NOTICE_TXT_TEMPLATE));
			model.put("noticeData", mergedBinaryData.get(binaryPath));
			
			String contents = CommonFunction.VelocityTemplateToString(model);
			
			for(String path : binaryPath.split(",")) {
				String fileName = "";
				String binaryFilePath = binaryDirPath;
				
				if(path.contains("/")) {
					File f = new File(binaryFilePath + "/" + path);
					fileName = f.getName();
					File parentFile = f.getParentFile();
					if(parentFile != null) {
						binaryFilePath = parentFile.toString();
						f = new File(binaryFilePath);
						f.mkdirs(); // path????????? directory ??????
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
		
		if(fileDir.exists()) {
			for(File f : fileDir.listFiles()) {
				if(f.getName().contains(".html")) {
					if(f.delete()) {
						log.debug(filePath + "/" + f.getName() + " is delete success.");
					} else {
						log.debug(filePath + "/" + f.getName() + " is delete failed.");
					}
				}
			}
		}
		
		if(FileUtil.writeFile(filePath, fileName, contents)) {
			// ?????? ??????
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
		
		for(ProjectIdentification bean : mainData) {
			List<ProjectIdentification> _list = new ArrayList<>();
			String _oldKey = null;
			String key = bean.getOssName() + "|" + bean.getOssVersion() + "|" + bean.getLicenseName();
			String licenseName = "";
			
			if(binaryMergeData.containsKey(bean.getBinaryName())) { // binaryPath ???????????? merge
				_list = binaryMergeData.get(bean.getBinaryName());
				_oldKey = binaryOssKeyMap.get(bean.getBinaryName());
				
				if(_list != null) {
					for(ProjectIdentification license : bean.getComponentLicenseList()) {
						LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
						license.setLicenseText(avoidNull(licenseBean.getLicenseText()));
						license.setAttribution(avoidNull(licenseBean.getAttribution()));
						license.setObligationType(CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn()) ? CoConstDef.CD_DTL_OBLIGATION_NOTICE : "");
						
						if(CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn())) {
							if(!isEmpty(licenseName)) {
								licenseName += ",";
							}
							
							licenseName += license.getLicenseName();
						}
					}
					
					OssMaster ossBean = CoCodeManager.OSS_INFO_UPPER.get((bean.getOssName() +"_"+ avoidNull(bean.getOssVersion())).toUpperCase());
					bean.setAttribution(avoidNull(ossBean.getAttribution()));
					bean.setLicenseName(licenseName);
					
					_list.add(bean);
					
					binaryMergeData.replace(bean.getBinaryName(), _list);
					String str = CommonFunction.mergedString(_oldKey, key, _oldKey.compareTo(key), ",");
					binaryOssKeyMap.put(bean.getBinaryName(), str);
				}
			} else {
				for(ProjectIdentification license : bean.getComponentLicenseList()) {
					LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
					license.setLicenseText(avoidNull(licenseBean.getLicenseText()));
					license.setAttribution(avoidNull(licenseBean.getAttribution()));
					license.setObligationType(CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn()) ? CoConstDef.CD_DTL_OBLIGATION_NOTICE : "");

					if(CoConstDef.FLAG_YES.equals(licenseBean.getObligationNotificationYn())) {
						if(!isEmpty(licenseName)) {
							licenseName += ",";
						}
						
						licenseName += license.getLicenseName();
					}
				}
				
				OssMaster ossBean = CoCodeManager.OSS_INFO_UPPER.get((bean.getOssName() +"_"+ avoidNull(bean.getOssVersion())).toUpperCase());
				
				bean.setAttribution(ossBean != null ? avoidNull(ossBean.getAttribution())  : "");
				
				bean.setLicenseName(licenseName);
				
				_list.add(bean);
				
				binaryMergeData.put(bean.getBinaryName(), _list);
				binaryOssKeyMap.put(bean.getBinaryName(), key);
			}
		}
		
		Map<String, List<ProjectIdentification>> resultData = new HashMap<String, List<ProjectIdentification>>();
		
		List<String> valuesList = binaryOssKeyMap.values().stream().distinct().collect(Collectors.toList());
		
		Map<String, String> collect = valuesList.stream()
										.collect(Collectors.toMap(Functions.identity(), v -> binaryOssKeyMap.entrySet().stream()
												.filter(entry -> Objects.equals(v, entry.getValue()))
												.map(Map.Entry<String, String>::getKey)
												.reduce("", (s1, s2) -> isEmpty(s1) ? s2 : s1 + "," + s2)));
		
		for(String binaryNameList : collect.values()) {
			for(String binaryName : binaryMergeData.keySet()) {
				int matchCnt = Arrays.asList(binaryNameList.split(",")).stream().filter(s -> s.equals(binaryName)).collect(Collectors.toList()).size();
				
				if(matchCnt > 0) {
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
		
		// bom compare ?????? ?????? ?????? ?????? ?????????????????? ????????? ???????????? ????????? ?
		// bflist loop -> aflist??? ossname & oss version??? ?????? ???????????? ????????? ?????????.
		// aflist loop -> bflist??? ossname & oss version??? ?????? ???????????? ????????? ?????????.
		// ?????? delete / change / add ?????? ?????????.
		
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
		
		// status > add
		int addchk = 0;
		for(ProjectIdentification after : filteredAfterBomList) {
			String ossName = after.getOssName();
			int addTargetCnt = filteredBeforeBomList.stream().filter(before -> (before.getOssName()).equals(ossName)).collect(Collectors.toList()).size();
			
			if(addTargetCnt == 0) {
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
		for(ProjectIdentification before : filteredBeforeBomList) {
			String ossName = before.getOssName();
			List<ProjectIdentification> afterList = filteredAfterBomList.stream().filter(after -> (after.getOssName()).equals(ossName)).collect(Collectors.toList());
			
			if(afterList.size() == 0) {
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
		return param.getOssName().toLowerCase() + "|" + avoidNull(param.getOssVersion(), "") + "|" + param.getLicenseName();
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
			}else {
				return ossNameVersion + " / " + ossNameVersion2;
			}
		}else {
			String licenseNmArr1[] = ossLicenseName.split("/");
			
			int chk = 0;
			for (int i=0; i<count; i++) {
				if (splitOssNameVersion[i].trim().equals(ossNameVersion2)) {
					List<String> licenseNmChk1 = Arrays.asList(licenseNmArr1[i].split(","));
					List<String> licenseNmChk2 = Arrays.asList(ossLicenseName2.split(","));
					
					Set<String> set = new LinkedHashSet<>(licenseNmChk1);
					set.addAll(licenseNmChk2);
					
					List<String> mergeList = new ArrayList<>(set);
					
					if (mergeList.size() > 0) {
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
			
			if (chk > 0) {
				String strMerge = "";
				for (int i=0; i<licenseNmArr1.length; i++) {
					strMerge += licenseNmArr1[i];
					if (i<licenseNmArr1.length-1) {
						strMerge += " / ";
					}
				}
				return strMerge;
			}else {
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
			
			// ?????? ??????????????? ?????? ??????
			// row count??? 0????????? ???????????? ??????????????? ???????????? progress ????????? ??????????????????
			if(isEmpty(project.getIdentificationStatus())) {
				projectSubStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			}
			
			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			projectMapper.updateProjectMaster(projectSubStatus);
		}
		
		OssComponents component = new OssComponents();
		component.setReferenceId(project.getRefPartnerId());
		component.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		
		// select partner Data
		Map<String, Object> resultMap = getPartnerOssList(component);
		List<OssComponents> partnerList = (List<OssComponents>) resultMap.get("rows");
		
		// Identification > 3rd Party Tab Insert
		for(OssComponents bean : partnerList) {
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
		
		for(ProjectIdentification pi : bomList) {
			List<OssComponentsLicense> licenseList = projectMapper.selectBomLicense(pi);
			pi.setReferenceId(project.getPrjId());
			// ???????????? ????????? ?????????
			projectMapper.registBomComponents(pi);
			
			for(OssComponentsLicense licenseBean : licenseList) {
				licenseBean.setComponentId(pi.getComponentId());
				projectMapper.registComponentLicense(licenseBean);
			}
		}
	}

	@Override
	public List<String> getPackageFileList(Project project, String filePath) {
		Project prj = getProjectBasicInfo(project.getCopyPrjId());
		List<String> fileSeqs = new ArrayList<>();
		
		if(!isEmpty(prj.getPackageFileId())) {
			List<UploadFile> uploadFile = fileService.setReusePackagingFile(prj.getPackageFileId());
			
			HashMap<String, Object> fileMap = new HashMap<>();
			fileMap.put("prjId", project.getPrjId());
			fileMap.put("refPrjId", project.getCopyPrjId());
			fileMap.put("refFileSeq", prj.getPackageFileId());
			fileMap.put("fileSeq", uploadFile.get(0).getRegistSeq());
			boolean reuseCheck = verificationService.setReusePackagingFile(fileMap);
			
			if(reuseCheck) {
				fileSeqs.add(uploadFile.get(0).getRegistSeq());
			}
			
			if(!isEmpty(prj.getPackageFileId2())) {
				List<UploadFile> file2 = fileService.setReusePackagingFile(prj.getPackageFileId2());
				fileMap.put("refFileSeq", prj.getPackageFileId2());
				fileMap.put("fileSeq", file2.get(0).getRegistSeq());
				reuseCheck = verificationService.setReusePackagingFile(fileMap);
				
				if(reuseCheck) {
					fileSeqs.add(file2.get(0).getRegistSeq());
				}
			}
			
			if(!isEmpty(prj.getPackageFileId3())) {
				List<UploadFile> file3 = fileService.setReusePackagingFile(prj.getPackageFileId3());
				fileMap.put("refFileSeq", prj.getPackageFileId3());
				fileMap.put("fileSeq", file3.get(0).getRegistSeq());
				reuseCheck = verificationService.setReusePackagingFile(fileMap);
				
				if(reuseCheck) {
					fileSeqs.add(file3.get(0).getRegistSeq());
				}
			}
		}
		
		return fileSeqs;
	}

	@Override
	public List<ProjectIdentification> selectIdentificationGridList(ProjectIdentification identification) {
		return projectMapper.selectIdentificationGridList(identification);
	}

	@Override
	public void updateCopyConfirmStatusProjectStatus(Project project) {
		projectMapper.updateCopyConfirmStatusProjectStatus(project);
	}

	@Override
	public void copySrcAndroidNoticeFile(Project project) {
		Project prj = getProjectBasicInfo(project.getCopyPrjId());
		
		if(!isEmpty(prj.getSrcAndroidNoticeFileId())) {
			String fileId = fileService.copyPhysicalFile(prj.getSrcAndroidNoticeFileId());
			if(!isEmpty(fileId)) {
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
		
		for(String prjId : project.getPrjIds()) {
			param.setPrjId(prjId);
			Project beforeBean = getProjectDetail(param);
			
			if(!beforeBean.getDivision().equals(division)) {
				Project afterBean = getProjectDetail(param);
				afterBean.setDivision(division);
				
				projectMapper.updateProjectDivision(afterBean);
				
				comment = "?????? Project ??? Division ( " + CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, beforeBean.getDivision()) 
						+ " => " + CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, afterBean.getDivision())
						+ " ) ??? ?????????????????????.";
				
				afterBean.setUserComment(comment);
				
				Map<String, List<Project>> modelMap = getModelList(prjId);
				beforeBean.setModelList((List<Project>) modelMap.get("currentModelList"));
				afterBean.setModelList((List<Project>) modelMap.get("currentModelList"));
				
				beforeBeanList.add(beforeBean);
				afterBeanList.add(afterBean);
			}
		}
		
		if(!beforeBeanList.isEmpty()) {
			updateProjectDivMap.put("before", beforeBeanList);
		}
		
		if(!afterBeanList.isEmpty()) {
			updateProjectDivMap.put("after", afterBeanList);
		}
		
		return updateProjectDivMap;
	}
}
