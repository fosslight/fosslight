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
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	public Map<String, Object> getProjectList(Project project) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Project> list = null;

		try {
			// OSS NAME으로 검색하는 경우 NICK NAME을 포함하도록 추가
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
					// 코드변환처리
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
						
						OssMaster nvdMaxScoreInfo = projectMapper.findIdentificationMaxNvdInfo(bean.getPrjId());
						
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
			e.printStackTrace();
		}

		return map;
	}
	
	@Override
	public Project getProjectDetail(Project project) {
		// master
		project = projectMapper.selectProjectMaster(project);
		
		// 이전에 생성된 프로젝트를 위해 Default를 설정한다.
		Map<String, Object> NoticeInfo = projectMapper.getNoticeType(project.getPrjId());
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

		//  button(삭제/복사/저장) view 여부
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

		// bom 일시
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(identification.getReferenceDiv())) {
			Map<String, String> obligationTypeMergeMap = new HashMap<>();
			String reqMergeFlag = identification.getMerge();
			
			list = projectMapper.selectBomList(identification);
			
			// bom merge 버튼을 클릭하고, 표시대상이 있을 경우, 기존에 저장되어 있는 내용을 취득한다.
			// need check의 저장값을 유지하기 위함
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
				
				// grouping 된 file path를 br tag로 변경
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
				
				// oss Name은 작성하고, oss Version은 작성하지 않은 case경우 해당 분기문에서 처리
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
			// bat 분석 결과 중에서 oss version이 명시되지 않고, src 또는 3rd party에 동일한 oss 가 존재하는 경우
			// bat 분석 결과를 src 또는 3rd party에 merge 한다.
			List<ProjectIdentification> _list = new ArrayList<>();
			List<String> adminCheckList = new ArrayList<>();
			List<ProjectIdentification> groupList = null;
			Map<String, List<ProjectIdentification>> srcSameLicenseMap = new HashMap<>();
			List<String> egnoreList = new ArrayList<>();
			
			for (ProjectIdentification ll : list) {
				// 이미 추가된 oss의 경우
				if(egnoreList.contains(ll.getComponentId())) {
					continue;
				}
				
				int addIdx = -1;
				
				if(!isEmpty(ll.getOssName())) {
					String mergeKey = ll.getOssName().toUpperCase();
					// main oss로 표시되는 bat oss의 version이 명시되어 있지 않은 경우
					if(CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(ll.getReferenceDiv()) && isEmpty(ll.getMergePreDiv()) && isEmpty(ll.getOssVersion())) {
						ProjectIdentification refBean = null;
						
						if( batMergeSrcMap.containsKey(mergeKey)) {
							// bat => src
							refBean = batMergeSrcMap.get(mergeKey);
							
							// 설정된 license가 상이하고, bat와 동일한 license가 src에 존재한다면 (최상위 버전이 아닌경우)
							// continue하고 다음 loop에서 merge
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
							
							// 순서 정렬
							addIdx = findOssAppendIndex(CoConstDef.CD_DTL_COMPONENT_ID_SRC, refBean.getComponentId(), list);
							
							if(addIdx > -1) {
								addIdx = addIdx +1;
								ll.setGroupingColumn(refBean.getOssName() + refBean.getOssVersion());
							}					
						} else if( batMergePartnerMap.containsKey(mergeKey)) {
							// 3rd => bat
							refBean = batMergePartnerMap.get(mergeKey);
							
							// 3rd에 같은 그룹으로 묶여 있는 모든 oss list를 취득
							ll.setGroupingColumn(refBean.getOssName() + refBean.getOssVersion());
							ll.setOssName(refBean.getOssName());
							ll.setOssVersion(refBean.getOssVersion());
							ll.setOssId(refBean.getOssId());
							
							// bin 에 누락된 정보를 3rd의 첫번재 row에서 채워 넣는다.
							// DOWNLOAD_LOCATION
							if(isEmpty(ll.getDownloadLocation())) {
								ll.setDownloadLocation(refBean.getDownloadLocation());
							}
							
							// HOMEPAGE
							if(isEmpty(ll.getHomepage())) {
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
							
							if(groupList != null && !groupList.isEmpty()) {
								for(ProjectIdentification _groupBean : groupList) {
									egnoreList.add(_groupBean.getComponentId());
								}
							}
						}
					} 
					// 3rd party의 경우, bat에 동일한 oss가 없을 경우만 추가 (정렬)
					else if(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(ll.getReferenceDiv()) && isEmpty(ll.getMergePreDiv()) ) {
						if(existsBatOSS(ll.getOssName(), list)) {
							continue;
						}
					}
				}
				// License Restriction 저장
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
			
			// src oss중에서 bat와 merge할 수 있는 동일한 oss에 최신 version 외 라이선스까지 동일한 bat가 존재하는 경우
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
							_mergeBean.setGroupingColumn(bean.getGroupingColumn()); // 순서 정렬
							
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
		} else { // bom 외 서브 그리드
			// bat oss list를 대상
			// 정렬 순서를 변경한다.
			if(CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(identification.getReferenceDiv()) 
					|| CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(identification.getReferenceDiv())
					|| CoConstDef.CD_DTL_COMPONENT_BAT.equals(identification.getReferenceDiv())) {
				identification.setSortAndroidFlag(CoConstDef.FLAG_YES);
			}
			
			HashMap<String, Object> subMap = new HashMap<String, Object>();
			
			// src, bin, bin(android) 의 경우만 comment를 포함한다.
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
					
					// oss Name은 작성하고, oss Version은 작성하지 않은 case경우 해당 분기문에서 처리
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
				
				// components license 정보를 한번에 가져온다
				for(ProjectIdentification bean : list) {
					param.addComponentIdList(bean.getComponentId());
					
					if(!isEmpty(bean.getOssId())) {
						ossParam.addOssIdList(bean.getOssId());
					}
				}
				
				// oss id로 oss master에 등록되어 있는 라이선스 정보를 취득
				Map<String, OssMaster> componentOssInfoMap = null;
				
				if(ossParam.getOssIdList() != null && !ossParam.getOssIdList().isEmpty()) {
					componentOssInfoMap = ossService.getBasicOssInfoListById(ossParam);
				}
				
				List<ProjectIdentification> licenseList = projectMapper.identificationSubGrid(param);
				
				for(ProjectIdentification licenseBean : licenseList) {
					for(ProjectIdentification bean : list) {
						if(licenseBean.getComponentId().equals(bean.getComponentId())) {
							// 수정가능 여부 초기설정
							licenseBean.setEditable(CoConstDef.FLAG_YES);
							bean.addComponentLicenseList(licenseBean);
							
							break;
						}
					}
				}
				
				// license 정보 등록
				for(ProjectIdentification bean : list) {
					if(bean.getComponentLicenseList() != null) {
						//String licenseText = "";
						String licenseCopy = "";
						
						// multi dual 라이선스의 경우, main row에 표시되는 license 정보는 OSS List에 등록되어진 라이선스를 기준으로 표시한다.
						// ossId가 없는 경우는 기본적으로 subGrid로 등록될 수 없다
						// 이짓거리를 하는 두번째 이유는, subgrid 에서 사용자가 추가한 라이선스와 oss 에 등록되어 있는 라이선스를 구분하기 위함
						if(componentOssInfoMap == null) {
							componentOssInfoMap = new HashMap<>();
						}
						
						OssMaster ossBean = componentOssInfoMap.get(bean.getOssId());
						
						// Restriction 대응을 위해 (BOM 외 추가) License Id를 콤마 구분으로 격납
						String strLicenseIds = "";
						
						if(ossBean != null
								&& CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())
								&& ossBean.getOssLicenses() != null && !ossBean.getOssLicenses().isEmpty()) {
							for(OssLicense ossLicenseBean : ossBean.getOssLicenses()) {
								
								if(!isEmpty(ossLicenseBean.getOssCopyright())) {
									licenseCopy += (!isEmpty(licenseCopy) ? "\r\n" : "") + ossLicenseBean.getOssCopyright();
								}
								
								//삭제 불가 처리
								for(ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
									// license index 까지 비교하는 이유는
									// multi dual 혼용인 경우, 동일한 라이선스가 두번 등록 될 수 있기 때문
									if(ossLicenseBean.getLicenseId().equals(licenseBean.getLicenseId()) 
											&& ossLicenseBean.getOssLicenseIdx().equals(licenseBean.getRnum())) {
										licenseBean.setEditable(CoConstDef.FLAG_NO);
										break;
									}
								}
								
								// Restriction 설정을 위해 license id 격납
								if(!isEmpty(ossLicenseBean.getLicenseId())) {
									strLicenseIds += (!isEmpty(strLicenseIds) ? "," : "") + ossLicenseBean.getLicenseId();
								}
								
							}
							
							// 어짜피 여기서 설정하는 라이선스 이름은 의미가 없음
							if(bean.getComponentLicenseList() != null && bean.getComponentLicenseList().size() == 1 && bean.getComponentLicenseList().get(0) != null) {
								bean.setLicenseName(bean.getComponentLicenseList().get(0).getLicenseName());
							} else if(multiUIFlag) {
								bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
							} else {
								// TODO - 여기 수정하면 될거 같음.. multiUIFlag
								bean.setLicenseName(CommonFunction.makeLicenseExpression(ossBean.getOssLicenses()));
							}
						} else {
							for(ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
								if(!isEmpty(licenseBean.getCopyrightText())) {
									licenseCopy += (!isEmpty(licenseCopy) ? "\r\n" : "") + licenseBean.getCopyrightText();
								}

								// Restriction 설정을 위해 license id 격납
								if(!isEmpty(licenseBean.getLicenseId())) {
									strLicenseIds += (!isEmpty(strLicenseIds) ? "," : "") + licenseBean.getLicenseId();
								}
							}
							
							bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
						}
						

						// Restriction 설정
						if(!isEmpty(strLicenseIds)) {
							bean.setRestriction(CommonFunction.setLicenseRestrictionListById(strLicenseIds));
						}
						
						// 3rd party의 경우 obligation 처리를 위해 license에 따른 obligation 설정을 추가
						if(CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(identification.getReferenceDiv())) {
							bean.setObligationLicense(CommonFunction.checkObligationSelectedLicense2(bean.getComponentLicenseList()));
							
							if(isEmpty(bean.getObligationType())) {
								bean.setObligationType(bean.getObligationLicense());
							}
							
							// need check 인 경우
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
						
						// subGrid의 Item 추출을 위해 별도의 map으로 구성한다.
						// 부몬의 component_id를 key로 관리한다.
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					} else {
						bean.setLicenseName("");
						
						subMap.put(bean.getGridId(), new ArrayList<ProjectIdentification>());
					}
					
					// bat 분석 결과의 경우
					if(!isEmpty(bean.getBatPercentage()) || !isEmpty(bean.getBatStringMatchPercentage())) {
						String batPercentageStr = avoidNull(bean.getBatStringMatchPercentage());
						
						if(CoConstDef.BAT_MATCHED_STR.equalsIgnoreCase(avoidNull(bean.getBatPercentage()).trim())) {
							batPercentageStr = CoConstDef.BAT_MATCHED_STR;
						} else if(!isEmpty(bean.getBatPercentage())) {
							batPercentageStr += " (" + bean.getBatPercentage() + ")";
						}

						bean.setBatStrPlus(batPercentageStr);
					}
					
					// License Restriction 저장
					bean.setRestriction(CommonFunction.setLicenseRestrictionList(bean.getComponentLicenseList()));
					
					// comments가 null인 경우 grid에서 수정시 저정되지 않기 때문에, 공백으로 치환한다.
					bean.setComments(avoidNull(bean.getComments()));
				}
				

				// 다른 프로젝트에서 load한 경우 component id 초기화
				if(isLoadFromProject || isApplyFromBat) {
					subMap = new HashMap<>();

					// refproject id + "p" + componentid 로 component_id를 재생성 하고, 
					// license 의 경우 재성생한 component_id + 기존 license grid_id의 component_license_id 부분을 결합
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
			
			// 편집중인 data 가 존재할 경우 append 한다.
			{
				if(identification.getMainDataGridList() != null) {
					for(ProjectIdentification bean : identification.getMainDataGridList()) {
						if(bean.getComponentLicenseList() == null || bean.getComponentLicenseList().isEmpty()){
							//멀티라이센스일 경우
							if(CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())){
								for (List<ProjectIdentification> comLicenseList : identification.getSubDataGridList()) {
									for (ProjectIdentification comLicense : comLicenseList) {
										if(bean.getComponentId().equals(comLicense.getComponentId())){
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

					for(ProjectIdentification bean : identification.getMainDataGridList()) {
						list.add(0, bean);
						
						subMap.put(bean.getGridId(), bean.getComponentLicenseList());
					}
				}
			}
			
			// exclude row의 재정렬 (가장 마지막으로)
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
	 * BAT와 동일한 OSS, license를 가지는 SRC 정보가 있는지 확인
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
				// OSS_NOTICE와 OSS_NOTICE_NEW에 정보가 없을경우 default setting
				notice.setEditNoticeYn(CoConstDef.FLAG_NO);
				notice.setEditCompanyYn(CoConstDef.FLAG_YES);
				notice.setEditDistributionSiteUrlYn(CoConstDef.FLAG_YES);
				notice.setEditEmailYn(CoConstDef.FLAG_YES);
				notice.setHideOssVersionYn(CoConstDef.FLAG_NO);
				notice.setEditAppendedYn(CoConstDef.FLAG_NO);
				notice.setPrjId(project.getPrjId());
				
				String distributeType = avoidNull(project.getDistributeTarget(), CoConstDef.CD_DISTRIBUTE_SITE_SKS); // LGE, NA => LGE로 표기, SKS => SKS로 표기함.
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
			e.printStackTrace();
		}

		return sb.toString();
	}


	@Transactional
	@Override
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void registProject(Project project) {
		//copy 건
		if("true".equals(project.getCopy())){
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
			
			if(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(project.getNoticeType())) {
				noticeParam.setNoticeTypeEtc(project.getNoticeTypeEtc());
			}
			
			projectMapper.makeOssNotice(noticeParam);

			// project model insert
			if (project.getModelList().size() > 0) {
				for(Project modelBean : project.getModelList()) {
					modelBean.setPrjId(project.getPrjId());
					modelBean.setModelName(modelBean.getModelName().trim().toUpperCase().replaceAll("\t", ""));
					
					// copy 한 프로젝트의 경우, 배포사이트 연동 정보는 삭제한다.
					// 삭제된 이력이 있는 모델은 추가할 필요 없음
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

			if (project.getWatchers()!= null) { // Project 신규 등록과 동일하게 watcher 추가
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
			
			//나머지 프로젝트 마스터 테이블 카피
			// Identification 관련 정보만 Copy한다.
			// upload한 파일이 있는 경우는 파일 순번을 새롭게 취득하여 재등록한다. 물리파일은 복사하지 않고 공유한다.
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
				
				// distribute target이 변경된 경우, 마스터 카테고리는 복사하지 않는다.
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
				
				// copy 시 distributeName 은 복사 되지 않도록 플래그 추가
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
			
			// Project - 3rd 매핑 정보 복사
			List<PartnerMaster> partnerList = partnerMapper.selectThirdPartyMapList(oldId);
			for (PartnerMaster bean : partnerList) {
				bean.setPrjId(prjId);
				
				partnerMapper.insertPartnerMapList(bean);
			}
		} else {
			boolean isNew = isEmpty(project.getPrjId());
			
			// 신규 프로젝트 생성시, Android model  의 경우, 3rd, src, bin Tab을  N/A 처리한다.
			if(isNew && "A".equals(CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE, project.getDistributionType())) ) {
				 project.setIdentificationSubStatusPartner(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusSrc(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusBin(CoConstDef.FLAG_NO);
				 project.setIdentificationSubStatusBat(CoConstDef.FLAG_NO);
			}
			
			// admin이 아니라면 creator를 변경하지 않는다.
			if(!CommonFunction.isAdmin()) {
				project.setCreator(null);
			}
			
			project.setPublicYn(avoidNull(project.getPublicYn(), CoConstDef.FLAG_YES));
			
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
			if (!CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(project.getDistributeTarget()) 
					&& project.getModelList().size() > 0) {
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
				//excludeYn 체크해주기
				Licenselist = CommonFunction.makeLicenseExcludeYn(Licenselist);
				// multi/dual license인 경우 연산식 표기로 변경
				prjOssMaster.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(Licenselist, ","));
			}
			
			map.put("prjOssMaster", prjOssMaster);
			map.put("prjLicense", Licenselist);
		} catch (Exception e) {
			e.printStackTrace();
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
		// 프로젝트 정보를 취득
		Project prjBasicInfo = new Project();
		prjBasicInfo.setPrjId(prjId);
		prjBasicInfo = projectMapper.selectProjectMaster(prjBasicInfo);
		
		// 프로젝트 상태 정보 변경
		{
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(prjId);
			projectSubStatus.setIdentificationSubStatusPartner((ossComponentsList.isEmpty() && thirdPartyList.isEmpty()) ? "X" : CoConstDef.FLAG_YES);
			
			// 최초 저장시에만 상태 변경
			// row count가 0이어도 사용자가 한번이라도 저장하면 progress 상태로 인지되어야함
			if(isEmpty(prjBasicInfo.getIdentificationStatus())) {
				projectSubStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			}
			
			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			projectMapper.updateProjectMaster(projectSubStatus);
		}
		
		OssComponents compParam = new OssComponents();
		compParam.setReferenceId(prjId);
		Map<String, ProjectIdentification> orgOssMap = new HashMap<>();
		
		// 기존 등록 oss 정보 취득
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
		
		// 순서대로 등록 1) update, 2) delete, 3)insert from project , 4) insert from 3rd 
		for(OssComponents bean : ossComponentsList) {
			// 기존에 등록되어 있는 경우는 update
			if(orgOssMap.containsKey(avoidNull(bean.getComponentId()))) {
				// exclude 여부만 사용
				updateList.add(bean);
				deleteList.add(bean.getComponentId());
			} else {
				if(!isEmpty(bean.getRefPrjId())) {
					// 신규 추가이면서, ref 프로젝트 id가 있으면 
					// componentsid + prjid + exclude 여부만 사용
					insertFromPrjList.add(bean);
				} else {
					// 신규 추가이면서, ref partner id가 있으면 (개별등록은 없기 때문에 else로처리)
					//componentsid + partnerid + exclude 여부만 사용
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
				// deleteOssComponentsWithIds는 not in 값을 delete, deleteOssComponentsWithIds2는 in 값을 delete함.
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
		
		// 기존 등록 3rd Map 정보 취득
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
			// 기존에 등록되어 있는 경우는 update
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
	@Transactional
	public void registSrcOss(List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense, Project project, String refDiv) {
		// 한건도 없을시 프로젝트 마스터 SRC 사용가능여부가 N이면 N 그외 null
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

		// 파일 등록
		if(!isEmpty(project.getSrcCsvFileId()) || !isEmpty(project.getSrcAndroidCsvFileId()) || !isEmpty(project.getSrcAndroidNoticeFileId()) || !isEmpty(project.getBinCsvFileId()) || !isEmpty(project.getBinBinaryFileId())){
			projectMapper.updateFileId(project);
			
			if(project.getCsvFile() != null) {
				for (int i = 0; i < project.getCsvFile().size(); i++) {
					projectMapper.deleteFileBySeq(project.getCsvFile().get(i));
				}				
			}
			
			if(project.getCsvAddFileSeq() != null) {
				for (int i = 0; i < project.getCsvAddFileSeq().size(); i++) {
					projectMapper.updateFileBySeq(project.getCsvAddFileSeq().get(i));
				}				
			}
		}
		
		// bin android 의 경우 다른 프로젝트에서 load한 정보를 save할 경우, notice html과 result text 정보를 변경한다.
		if(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv) && CoConstDef.FLAG_YES.equals(project.getLoadFromAndroidProjectFlag())) {
			if(isEmpty(project.getSrcAndroidResultFileId())) {
				project.setSrcAndroidResultFileId(null);
			}
			
			projectMapper.updateAndroidNoticeFileInfoWithLoadFromProject(project);
		}
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
		// 컴포넌트 마스터 라이센스 지우기
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
				
				// 최초 상태이면 PROG 
				if (StringUtil.isEmpty(projectStatus.getIdentificationStatus())) {
					projectStatus.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
				}
				
				if(CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(refDiv)) {
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
		}

		project.setReferenceDiv(refDiv);
		project.setReferenceId(refId);
		
		int ossComponentIdx = projectMapper.selectOssComponentMaxIdx(project);
		
		//deleteRows
		List<String> deleteRows = new ArrayList<String>();
		
		// 컴포넌트 등록	
		for (int i = 0; i < ossComponent.size(); i++) {
			// SRC STATUS 등록
			
			ProjectIdentification ossBean = ossComponent.get(i);
			
			// oss_id를 다시 찾는다. (oss name과 oss id가 일치하지 않는 경우가 있을 수 있음)
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
				//ossComponents 등록
				// android project의 경우, bom 처리를 하지 않기 때문에, bom save에서 처리하는 obligation type을 여기서 설정해야한다.
				if(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(refDiv)) {
					List<OssComponentsLicense> _list = new ArrayList<>();
					
					if(CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())) {
						for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
							for (ProjectIdentification comLicense : comLicenseList) {
								if(ossBean.getComponentId().equals(comLicense.getComponentId())){
									// multi license oss에 license를 추가한 경우, license 명을 입력하지 않은 경우는 무시
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
				
				//멀티라이센스일 경우
				if(CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())){
					List<String> duplicateLicense = new ArrayList<String>();
					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(ossBean.getComponentId().equals(comLicense.getComponentId())){
								// multi license oss에 license를 추가한 경우, license 명을 입력하지 않은 경우는 무시
								if((isEmpty(comLicense.getLicenseName()) 
										&& isEmpty(comLicense.getLicenseText()) 
										&& isEmpty(comLicense.getOssCopyright())) 
									|| duplicateLicense.contains(comLicense.getLicenseId())) {
									continue;
								}
								
								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
								duplicateLicense.add(comLicense.getLicenseId());
								
								// 라이센스 등록
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else { //싱글라이센스일경우
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
					// 라이센스 등록
					projectMapper.registComponentLicense(license);
				}
			} else { //insert
				//ossComponents 등록
				String exComponentId = ossBean.getGridId();
				ossBean.setReferenceId(refId);
				ossBean.setReferenceDiv(refDiv);
				
				// android project의 경우, bom 처리를 하지 않기 때문에, bom save에서 처리하는 obligation type을 여기서 설정해야한다.
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
				
				// insert시 매번 max idx를 select 하면 
				ossBean.setComponentIdx(Integer.toString(ossComponentIdx++));
				projectMapper.insertSrcOssList(ossBean);
				deleteRows.add(ossBean.getComponentId());
				
				//멀티라이센스일 경우
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
								// multi license oss에 license를 추가한 경우, license 명을 입력하지 않은 경우는 무시
								if((isEmpty(comLicense.getLicenseName()) 
									&& isEmpty(comLicense.getLicenseText()) 
									&& isEmpty(comLicense.getOssCopyright())) 
										|| duplicateLicense.contains(comLicense.getLicenseName())) {
									continue;
								}
								
								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
								// 컴포넌트 ID 설정
								license.setComponentId(ossBean.getComponentId());
								duplicateLicense.add(comLicense.getLicenseName()); 
								
								// 라이센스 등록
								projectMapper.registComponentLicense(license);
							}
						}
					}
				} else { // 싱글라이센스일경우
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossBean, CoConstDef.LICENSE_DIV_SINGLE);
					// 라이센스 등록
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
					
					// update를 위해
					// ossmaster => projectIdentification 으로 변한
					ProjectIdentification updateBean = new ProjectIdentification();
					
					if(ossBean != null) {
						if(!isEmptyOss) {
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
					if(addOssComponentFlag) {
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
						
					// oss name이 하이픈이 아니라면, OSS List에 등록된 정보를 기준으로 취합
					if(!isEmptyOss) {
						boolean hasSelectedLicense = false;
						
						for(OssLicense license : ossBean.getOssLicenses()) {
							OssComponentsLicense componentLicense = new OssComponentsLicense();
							
							componentLicense.setComponentId(componentId);
							componentLicense.setLicenseId(license.getLicenseId());
							componentLicense.setLicenseName(license.getLicenseName());
							// license text 설정은 불필요함
							
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
						// oss name이 하이픈이라면 라이선스 binary db에 등록된 license 정보를 그대로 등록한다.
						// 이러한 경우는 license가 복수개로 등록되어 있을 수 없음
						OssComponentsLicense license = new OssComponentsLicense();
						license.setExcludeYn(CoConstDef.FLAG_NO);
						license.setComponentId(componentId);
						
						// binary db에 등록된 license 정보가 license master에 등록되어 있다면 master 정보를 사용
						if(!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
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
		for(OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			
			if(binaryName.indexOf("/") > -1) {
				binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
			}
			
			if(isEmpty(binaryName)) {
				continue;
			}
			
			// 사용자가 입력한 oss가 있으면 설정하지 않음
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
						// oss name + version 이 일치하는 oss 가 존재하면, update 한다.
						boolean isEmptyOss = "-".equals(ossName);
						OssMaster ossBean = ossInfo.get(key.toUpperCase());
						
						// update를 위해
						// ossmaster => projectIdentification 으로 변한
						ProjectIdentification updateBean = new ProjectIdentification();
						
						if(!isEmptyOss) {
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
						if(addOssComponentFlag) {
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
						
						// oss name이 하이픈이 아니라면, OSS List에 등록된 정보를 기준으로 취합
						if(!isEmptyOss) {
							boolean hasSelectedLicense = false;
							
							for(OssLicense license : ossBean.getOssLicenses()) {
								OssComponentsLicense componentLicense = new OssComponentsLicense();
								componentLicense.setComponentId(bean.getComponentId());
								componentLicense.setLicenseId(license.getLicenseId());
								componentLicense.setLicenseName(license.getLicenseName());
								
								// license text 설정은 불필요함
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
							// oss name이 하이픈이라면 라이선스 binary db에 등록된 license 정보를 그대로 등록한다.
							// 이러한 경우는 license가 복수개로 등록되어 있을 수 없음
							OssComponentsLicense license = new OssComponentsLicense();
							license.setExcludeYn(CoConstDef.FLAG_NO);
							license.setComponentId(addOssComponentFlag ? updateBean.getComponentId() : bean.getComponentId());
							
							// binary db에 등록된 license 정보가 license master에 등록되어 있다면 master 정보를 사용
							if(!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
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
	public Map<String, List<String>> nickNameValid(List<ProjectIdentification> ossComponentList, List<List<ProjectIdentification>> ossComponentLicenseList) {
		List<String> ossNickNameCheckResult = new ArrayList<>();
		List<String> licenseNickNameCheckResult = new ArrayList<>();
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		List<String> ossCheckParam = new ArrayList<>();
		List<String> licenseCheckParam = new ArrayList<>();
		
		for(ProjectIdentification bean : ossComponentList) {
			String _ossName = avoidNull(bean.getOssName()).trim();
			
			if(!isEmpty(_ossName) && !"-".equals(_ossName) && !ossCheckParam.contains(_ossName)) {
				ossCheckParam.add(_ossName);
			}
			
			if(CoConstDef.LICENSE_DIV_MULTI.equals(bean.getLicenseDiv())) {
				// 여기서 할 필요 없음
			} else {
				String _licenseName = avoidNull(bean.getLicenseName()).trim();
				if(!isEmpty(_licenseName) && !licenseCheckParam.contains(_licenseName)) {
					licenseCheckParam.add(_licenseName);
				}
			}
		}

		// multi license의 경우 nickname check대상 추출
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
		// 컴포넌트 삭제
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(prjId);
		identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
		identification.setMerge(merge);
		identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		identification.setSaveBomFlag(CoConstDef.FLAG_YES); // file path 를 groupping 하지 않고, 개별로 data 등록
		List<OssComponents> componentId = projectMapper.selectComponentId(identification);
		
		// 기존 bom 정보를 모두 물리삭제하고 다시 등록한다.
		if(componentId.size() > 0){
			for (int i = 0; i < componentId.size(); i++) {
				projectMapper.deleteOssComponentsLicense(componentId.get(i));
			}
			
			projectMapper.deleteOssComponents(identification);
		}
		
		Map<String, Object> mergeListMap = getIdentificationGridList(identification);
		
		if(mergeListMap != null && mergeListMap.get("rows") != null) {
			for(ProjectIdentification bean : (List<ProjectIdentification>)mergeListMap.get("rows")) {
				
				if((isEmpty(bean.getOssName()) || "-".equals(bean.getOssName())) // ossName이 공란이거나 '-' 일때 license가 multi license일때는 bom에 merge하지 않음. 
					&& bean.getOssComponentsLicenseList().size() > 1) {
					continue;
				}
				
				bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				bean.setRefComponentId(bean.getComponentId());
				bean.setAdminCheckYn(CoConstDef.FLAG_NO);
				bean.setPreObligationType(bean.getObligationType());
				
				// 그리드 데이터 넣기
				for (ProjectIdentification gridData : projectIdentification) {
					// merge 결과 (src/bat/3rd) 일시
					if(gridData.getRefComponentId().contains(bean.getRefComponentId())){
						bean.setMergePreDiv(gridData.getMergePreDiv());
						
						// BOM에 초기표시된 obligation을 초기 값으로 설정
						// needs check의 경우만 화면에서 입력받는다.
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
				
				// 컴포넌트 마스터 인서트
				projectMapper.registBomComponents(bean);
				List<OssComponentsLicense> licenseList = CommonFunction.findOssLicenseIdAndName(bean.getOssId(), bean.getOssComponentsLicenseList());
				
				for(OssComponentsLicense licenseBean : licenseList) {
					licenseBean.setComponentId(bean.getComponentId());
					
					projectMapper.registComponentLicense(licenseBean);
				}
			}
		}
			
		// identification 대상이 없이 처음 저장하는 경우
		Project _tempPrjInfo = new Project();
		_tempPrjInfo.setPrjId(prjId);
		_tempPrjInfo = projectMapper.selectProjectMaster2(_tempPrjInfo);
		
		if (isEmpty(_tempPrjInfo.getIdentificationStatus())) {
			_tempPrjInfo.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
			
			projectMapper.updateIdentifcationProgress(_tempPrjInfo);
		}
	}

	@Override
	@Transactional
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void updateProjectStatus(Project project) {
		CoMail mailBean = null;
		//review 상태로 변경시 reviewer가 설정되어 있지 않은 경우, reviewer도 업데이트 한다.
		if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(project.getIdentificationStatus())) {
			Project param = new Project();
			param.setPrjId(project.getPrjId());
			param = projectMapper.selectProjectMaster2(param);
			
			if(isEmpty(param.getReviewer())) {
				param.setModifier(param.getLoginUserName());
				param.setReviewer(param.getLoginUserName());
				
				projectMapper.updateReviewer(param);
				
				mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_ADD);
				mailBean.setToIds(new String[] {param.getLoginUserName()});
				mailBean.setParamPrjId(project.getPrjId());
			}
		}
		
		// 프로젝트 완료처리
		if(CoConstDef.FLAG_YES.equals(project.getCompleteYn())) {
			// Identification N/A 처리
			// identification sub status
			// distribution status
			Project _param = new Project();
			_param.setPrjId(project.getPrjId());
			
			projectMapper.updateProjectStatusWithComplete(_param);
		} else {
			//다운로드 허용 플래그
			project.setAllowDownloadBitFlag(allowDownloadMultiFlagToBitFlag(project));
			
			// 프로젝트 상태 변경
			projectMapper.updateProjectMaster(project);
		}
		
		if(mailBean != null) {
			try {
				CoMailManager.getInstance().sendMail(mailBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	@Transactional
	public void updateProjectIdentificationConfirm(Project project) {
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
			
			if(oldPackagingList != null && !oldPackagingList.isEmpty()) {
				// key value 형식으로
				// key = ref + oss name + oss version + license name
				for(OssComponents oldBean : oldPackagingList) {
					if(!isEmpty(oldBean.getFilePath())) {
						String key = oldBean.getReferenceDiv() + "|" + oldBean.getOssId() + "|" + oldBean.getLicenseName();
						
						oldPackageInfoMap.put(key, oldBean);
					}
				}
			}
			
			// 1) packaging components delete 처리
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
			bomParam.setNoticeFlag(CoConstDef.FLAG_YES); // notice 대상만 추출한다. (obligation type이 10, 11)
			bomParam.setSaveBomFlag(CoConstDef.FLAG_YES);
			bomParam.setBomWithAndroidFlag(project.getAndroidFlag()); // android Project
			List<ProjectIdentification> bomList = projectMapper.selectBomList(bomParam);
			
			// 일괄 등록을 위해 대상 data의 component id 만 추출한다.
			List<String> groupingList = new ArrayList<>(); // 불필요한 row (중복) 는 미등록
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
			
			// 안드로이드 모델인 경우 없을 수도 있음
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
					// key value 형식으로
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
		}
	}


	@Override
	public void updateIdentificationConfirmSkipPackaing(Project project) {
		projectMapper.updateIdentificationConfirm(project);
	}

	@Override
	public void updateProjectMaster(Project project) {
		projectMapper.updateProjectMaster(project);
		
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
				// distribution type code 변환
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
		// 컴포넌트 마스터 라이센스 지우기
		ProjectIdentification deleteparam = new ProjectIdentification();
		deleteparam.setReferenceId(prjId);
		deleteparam.setReferenceDiv(prjYn?CoConstDef.CD_DTL_COMPONENT_ID_BAT:CoConstDef.CD_DTL_COMPONENT_PARTNER_BAT); // 3rd 추가
		List<OssComponents> componentsId = projectMapper.selectComponentId(deleteparam);
		
		for(int j = 0; j < componentsId.size(); j++){
			projectMapper.deleteOssComponentsLicense(componentsId.get(j));
		}
		
		// // 한건도 없을시 프로젝트 마스터 BAT 사용가능여부가 N이면 N 그외 null
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
		
		// 컴포넌트 등록
		for (int i = 0; i < ossComponents.size(); i++) {
			// BAT STATUS 등록
			if(i==0 && prjYn){
				Project projectStatus = new Project();
				projectStatus.setPrjId(prjId);
				projectStatus = projectMapper.selectProjectMaster(projectStatus);
				
				// 최초 상태이면 PROG
				if (StringUtil.isEmpty(projectStatus.getIdentificationStatus())) {
					projectStatus.setIdentificationStatus("PROG");
				}
				
				// 프로젝트 마스터 BAT 사용가능여부가 N 이면 N 그외 Y
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
				//ossComponents 등록
				projectMapper.updateSrcOssList(ossComponents.get(i));
				deleteRows.add(ossComponents.get(i).getComponentId());
				
				//멀티라이센스일 경우
				if("M".equals(ossComponents.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentsLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(ossComponents.get(i).getComponentId().equals(comLicense.getComponentId())){
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
					license.setComponentId(ossComponents.get(i).getComponentId());
					
					// 라이센스 ID 설정
					if (StringUtil.isEmpty(ossComponents.get(i).getLicenseId())) {
						license.setLicenseId(CommonFunction.getLicenseIdByName(ossComponents.get(i).getLicenseName()));
					} else {
						license.setLicenseId(ossComponents.get(i).getLicenseId());
					}
					
					// 기타 설정
					license.setLicenseName(ossComponents.get(i).getLicenseName());
					license.setLicenseText(ossComponents.get(i).getLicenseText());
					license.setCopyrightText(ossComponents.get(i).getCopyrightText());
					license.setExcludeYn(CoConstDef.FLAG_NO);
					
					// 라이센스 등록
					projectMapper.registComponentLicense(license);
				}
			} else { //insert
				//ossComponents 등록
				String exComponentId = ossComponents.get(i).getGridId();
				ossComponents.get(i).setReferenceId(prjId);
				ossComponents.get(i).setReferenceDiv(prjYn?"12":"20"); // 3rd 추가
				ossComponents.get(i).setComponentIdx(Integer.toString(ossComponentIdx++));
				projectMapper.insertSrcOssList(ossComponents.get(i));
				deleteRows.add(ossComponents.get(i).getComponentId());
				
				//멀티라이센스일 경우
				if("M".equals(ossComponents.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentsLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(exComponentId.equals(comLicense.getComponentId())){
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
					if (StringUtil.isEmpty(ossComponents.get(i).getLicenseId())) {
						license.setLicenseId(CommonFunction.getLicenseIdByName(ossComponents.get(i).getLicenseName()));
					} else {
						license.setLicenseId(ossComponents.get(i).getLicenseId());
					}
					
					// 기타 설정
					license.setLicenseName(ossComponents.get(i).getLicenseName());
					license.setLicenseText(ossComponents.get(i).getLicenseText());
					license.setCopyrightText(ossComponents.get(i).getCopyrightText());
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
		
		ossComponents.setReferenceDiv(avoidNull(ossComponents.getReferenceDiv(), CoConstDef.CD_DTL_COMPONENT_PARTNER));
		List<OssComponents> list = projectMapper.getPartnerOssList(ossComponents);
		
		for(OssComponents oc : list){
			if(CoConstDef.FLAG_YES.equals(oc.getExcludeYn())){
				ProjectIdentification PI = new ProjectIdentification();
				PI.setComponentId(oc.getComponentId());
				List<ProjectIdentification> subGridData = projectMapper.identificationSubGrid(PI);
				PI = subGridData.get(0);
				
				oc.setLicenseName(PI.getLicenseName());
				oc.setLicenseText(PI.getLicenseText());
				oc.setCopyrightText(PI.getCopyrightText());
			}
		}
		
		map.put("rows", list);
		
		return map; 
	}

	@Override
	public void updateProjectAllowDownloadBitFlag(Project project) {
		project.setAllowDownloadBitFlag(allowDownloadMultiFlagToBitFlag(project));
		
		projectMapper.updateProjectAllowDownloadBitFlag(project);
	}

	public int allowDownloadMultiFlagToBitFlag(Project project) {
		int bitFlag = 1;
		
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadNoticeHTMLYn())) {
			bitFlag |= CoConstDef.FLAG_A;
		}
			
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadNoticeTextYn())) {
			bitFlag |= CoConstDef.FLAG_B;
		}
		
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleHTMLYn())) {
			bitFlag |= CoConstDef.FLAG_C;
		}
			
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleTextYn())) {
			bitFlag |= CoConstDef.FLAG_D;
		}
			
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXSheetYn())) {
			bitFlag |= CoConstDef.FLAG_E;
		}
			
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXRdfYn())) {
			bitFlag |= CoConstDef.FLAG_F;
		}
			
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXTagYn())) {
			bitFlag |= CoConstDef.FLAG_G;
		}	
		
		return bitFlag;
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
		
		// 대상 컴포넌트 추출
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getComponentId() != null) {
				// 컴포넌트 라이센스 조회
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
				
				// oss Name은 작성하고, oss Version은 작성하지 않은 case경우 해당 분기문에서 처리
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
				PI = subGridData.get(0);
				
				oc.setLicenseName(PI.getLicenseName());
				oc.setLicenseText(PI.getLicenseText());
				oc.setCopyrightText(PI.getCopyrightText());
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
		
		// notice.html 파일이 있으면 notice.html내 해당 binary가 존재하는지 여부를 체크 csv내의 결과와 다를 경우 warning
		if(noticeBinaryList != null) {
			for(ProjectIdentification bean : reportData) {
				// 사용자 입력 정보를 무시 
				bean.setBinaryNotice(noticeBinaryList.contains(bean.getBinaryName()) ? "ok" : "nok");
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
		
		// 현재 DB에 등록되어 있는 정보와 CLIENT에서 넘어온 정보를 SORT 한 후 문자열로 비교한다.
		// TRIM, 대소문자를 무시하고, 주요 칼럼만 비교한다.
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
 					// 3rd의 경우 라이선스를 무시하고 key를 생성하기 때문에 중복되는 경우가 있음
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
 		
		// 화면정보 비교 key로 convert
		// 3rd party의 경우는 편집이 불가능하기 때문에 라이선스 정보를 제외하고 비교한다.
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
 		
 		// 1) 건수 비교 
 		// 2) 건수가 동일하기 때문에 sort후 text 비교
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
	 * 라이선스 명칭을 기준으로 short identifier인 경우 정식명칭을, 정식명칭(또는 닉네임)이면서, short identifier가 설정되어 있는 경우는 short identifier를 반환한다.
	 * 그외 경우는 그대로 반환
	 * @param licenseName
	 * @param convertShortLicenseName
	 * @return
	 */
	private String convertLicenseShortName(String licenseName, boolean convertShortLicenseName) {
		if(convertShortLicenseName && !isEmpty(licenseName)) {
			if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
				LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
				
				if(master != null) {
					// 현재 라이선스 명이 short identifier 이면 정식 명칭을 반환
					if(licenseName.equals(master.getShortIdentifier())) {
						licenseName = master.getLicenseNameTemp();
					} else if(!isEmpty(master.getShortIdentifier())) {
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
			
			if(!isEmpty(_ossName) && !"-".equals(_ossName) && !ossCheckParam.contains(_ossName)) {
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
			
			// license nickname 체크
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
			if(projectMapper.existsWatcherByEmail(project) == 0) { // 이미 추가된 watcher 체크
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
			if(projectMapper.existsWatcherByUser(project) == 0) { // 이미 추가된 watcher 체크
				projectMapper.insertWatcher(project); // watcher 추가
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
	 * 분석결과서 다운로드 3rd party 명칭 반환
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
			
			if(groupColumn.equals(info.getOssName() + "-" + info.getOssVersion()) // 같은 groupColumn이면 데이터를 쌓음
					&& !("-".equals(info.getOssName()) 
					&& !"NA".equals(info.getLicenseType()))
					&& !ossNameEmptyFlag) { // 단, OSS Name: - 이면서, License Type: Proprietary이 아닌 경우 Row를 합치지 않음.
				tempData.add(info);
			} else { // 다른 grouping
				setMergeData(tempData, resultGridData);
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
				tempData.clear();
				tempData.add(info);
			}
			
			ossNameEmptyFlag = false; // 초기화
		}
		
		setMergeData(tempData, resultGridData); // bom data의 loop가 끝났지만 tempData에 값이 있다면 해당 값도 merge를 함.
		
		return resultGridData;
	}
	
	public static void setMergeData(List<ProjectIdentification> tempData, List<ProjectIdentification> resultGridData){
		if(tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<ProjectIdentification>() {
				@Override
				public int compare(ProjectIdentification o1, ProjectIdentification o2) {
					if(o1.getLicenseName().length() >= o2.getLicenseName().length()) { // license name이 같으면 bomList조회해온 순서 그대로 유지함. license name이 다르면 순서변경
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
				
				// 동일한 oss name과 version일 경우 license 정보를 중복제거하여 merge 함.
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
														.filter(e -> list.getLicenseName().equals(e.getLicenseName())) // 동일한 licenseName을 filter
														.collect(Collectors.toList()) // return을 list로변환
														.size(); // 해당 list의 size
					
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
				
				// 특정 tab에 해당 Data가 공란이고, 다른 tab에 해당 Data가 값이 작성되어 있을 경우 첫번째 발견되는 data를 넣어줌. (대상 downloadLocation, homepage, copyrightText)
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
						&& CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(rtnBean.getObligationType())){
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
			diffCnt = diffData.keySet()
								.stream()
								.filter(c -> c.toUpperCase().contains("OSSNAME") 
												|| c.toUpperCase().contains("OSSVERSION") 
												|| c.toUpperCase().contains("LICENSENAME"))
								.collect(Collectors.toList())
								.size();
		}
		
		// OSS Name, OSS Version, License에 Warning message(빨간색, 파란색)가 있는 Row 또는 Binary Name이 공란인 Row
		if(emptyBinaryPathCnt > 0 || errCnt > 0 || diffCnt > 0) {
			validMsg = "You can download NOTICE only if there is no warning message in OSS Name, OSS Version, License or Binary Name is not null.";
		}
		
		// 출력할 Binary가 없는 경우(= 출력 조건에 해당하는 Row가 없는 경우)
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
		String filePath = CommonFunction.emptyCheckProperty("supplement.notice.path", "/supplement_notice") + "/" + project.getPrjId();
		File dir = new File(filePath);
		
		if(dir.exists()) {  // 전체 삭제 예쩡( html file은 제외함)
			for(File item : dir.listFiles()) {
				if(!item.isDirectory() && item.getName().contains(".html")){
					item.delete();
				} else if(item.isDirectory()) {
					CommonFunction.removeAll(item); // 하위폴더, file 전체 삭제
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
							
							FileUtil.writhFile(LicensesfilePath, LICENSEFileName+".txt", licenseBean.getLicenseText());
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
					binaryFilePath = f.getParentFile().toString();
					
					f = new File(binaryFilePath);
					f.mkdirs(); // path전체의 directory 생성
				} else {
					fileName = path;
				}
				
				FileUtil.writhFile(binaryFilePath, fileName, contents);
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
		String filePath = CommonFunction.emptyCheckProperty("supplement.notice.path", "/supplement_notice") + "/" + project.getPrjId();
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
		
		if(FileUtil.writhFile(filePath, fileName, contents)) {
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
		
		for(ProjectIdentification bean : mainData) {
			List<ProjectIdentification> _list = new ArrayList<>();
			String _oldKey = null;
			String key = bean.getOssName() + "|" + bean.getOssVersion() + "|" + bean.getLicenseName();
			String licenseName = "";
			
			if(binaryMergeData.containsKey(bean.getBinaryName())) { // binaryPath 기준으로 merge
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

	// 20210616_BOM COMPARE FUNC ADD
	@Override
	public boolean existProjectCnt(Project project) throws Exception {
		String ossReportFlag = project.getOssReportFlag();
		List<String> prjIdList = (List<String>) project.getWatcherListInfo();
		
		if(prjIdList != null) {
			if(isEmpty(ossReportFlag)) {
				ossReportFlag = CoConstDef.FLAG_NO;
				
				project.setOssReportFlag(ossReportFlag);
			}
			
			int records = projectMapper.selectProjectCount(project);
			
			return records == prjIdList.size() ? true : false;
		}
		
		return false;
	}

	@Override
	public List<ProjectIdentification> getBomList(String prjId) throws Exception {
		Project project = new Project();
		
		// get bom list
		ProjectIdentification bomParam = new ProjectIdentification();
		bomParam.setReferenceId(prjId);
		bomParam.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		bomParam.setMerge(CoConstDef.FLAG_NO);
		bomParam.setNoticeFlag(CoConstDef.FLAG_NO); // notice 대상만 추출한다. (obligation type이 10, 11)
		bomParam.setSaveBomFlag(CoConstDef.FLAG_YES);
		bomParam.setBomWithAndroidFlag(project.getAndroidFlag()); // android Project
		
		if(CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST != null && !CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST.isEmpty()) {
			bomParam.setRoleOutLicenseIdList(CoCodeManager.CD_ROLE_OUT_LICENSE_ID_LIST); // android Project
		}
		
		List<ProjectIdentification> bomList = projectMapper.selectBomList(bomParam);
		List<ProjectIdentification> chkedBomList = new ArrayList<ProjectIdentification>();
		
		if (bomList.size() > 0) {
			for(ProjectIdentification li : bomList) {
				String licenseId = CommonFunction.removeDuplicateStringToken(avoidNull(li.getLicenseId()), ",");
				String licenseName = CommonFunction.removeDuplicateStringToken(li.getLicenseName(), ",");
				List<OssComponentsLicense> listLicense = projectMapper.selectBomLicense(li);
				
				li.setLicenseId(licenseId);
				li.setLicenseName(licenseName);
				li.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
				li.setOssComponentsLicenseList(listLicense);
				
				chkedBomList.add(li);
			}
		}
		
		return setMergeGridData(chkedBomList);
	}

	@Override
	public Object getBomCompare(List<ProjectIdentification> beforeBomList, List<ProjectIdentification> afterBomList)
			throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> addList = new HashMap<String, Object>();
		Map<String, Object> deleteList = new HashMap<String, Object>();
		Map<String, Object> changeList = new HashMap<String, Object>();
		
		for(ProjectIdentification before : beforeBomList) {
			String ossName = before.getOssName();
			List<ProjectIdentification> afterList = afterBomList.stream().filter(after -> (after.getOssName()).equals(ossName)).collect(Collectors.toList());
			
			if(afterList.size() == 0) {
				Map<String, Object> deleteMap = new HashMap<String, Object>();
				
				deleteMap.put("name", before.getOssName());
				deleteMap.put("version", avoidNull(before.getOssVersion(), ""));
				deleteMap.put("license", Arrays.asList((before.getLicenseName()).split(",")));
				deleteList.put(getCompareKey(before), deleteMap);
			} else {
				if(!(afterList.get(0).getOssVersion()).equals(before.getOssVersion())
						 || !(afterList.get(0).getLicenseName()).equals(before.getLicenseName())) {
					Map<String, Object> changeMap = new HashMap<String, Object>();
					
					changeMap.put("name", (String) before.getOssName());
					changeMap.put("prev_version", avoidNull(before.getOssVersion(), ""));
					changeMap.put("prev_license", Arrays.asList((before.getLicenseName()).split(",")));
					changeMap.put("now_version", avoidNull(afterList.get(0).getOssVersion(), ""));
					changeMap.put("now_license", Arrays.asList((afterList.get(0).getLicenseName()).split(",")));
					
					changeList.put(getCompareKey(before), changeMap);
				}
			}
		}
		
		for(ProjectIdentification after : afterBomList) {
			String ossName = after.getOssName();
			int addTargetCnt = beforeBomList.stream().filter(before -> (before.getOssName()).equals(ossName)).collect(Collectors.toList()).size();
			
			if(addTargetCnt == 0) {
				Map<String, Object> addMap = new HashMap<String, Object>();
				
				addMap.put("name", after.getOssName());
				addMap.put("version", avoidNull(after.getOssVersion(), ""));
				addMap.put("license", Arrays.asList((after.getLicenseName()).split(",")));
				
				addList.put(getCompareKey(after), addMap);
			}
		}
		
		// add, delete, change가 값이없으면 완전일치한 project로 판단. 
		resultMap.put("add", 	addList.values());
		resultMap.put("delete", deleteList.values());
		resultMap.put("change", changeList.values());
		
		return resultMap;
	}
	
	private String getCompareKey(ProjectIdentification param) {
		return param.getOssName() + "|" + avoidNull(param.getOssVersion(), "") + "|" + param.getLicenseName();
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> getBomCompareList(String flag, Map<String, Object> compareMap) throws Exception{
		List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
		
		List<Map<String, Object>> addList = (List<Map<String, Object>>) compareMap.get("add");
		
		if (addList.size() > 0) {
			for (int i=0; i<addList.size(); i++) {
				Map<String, String> returnMap = new HashMap<String, String>();

				returnMap.put("status", "add");
				List<String> licenseArr = (List<String>) addList.get(i).get("license");
				String license = "";
				for (int j=0; j<licenseArr.size(); j++) {
					license += licenseArr.get(j).toString();
					if (j<licenseArr.size()-1) {
						license += ", ";
					}
				}
				returnMap.put("beforeossname", "");
				returnMap.put("beforelicense", "");
				
				String afterossname = "";
				afterossname += addList.get(i).get("name").toString();
				
				if (addList.get(i).get("version").toString().equals("") || addList.get(i).get("version") == null) {
				}else {
					afterossname += " (" + addList.get(i).get("version").toString() + ")";
				}
				if (flag.equals("list")) {
					returnMap.put("afterossname", changeStyle("add", afterossname));
					returnMap.put("afterlicense", changeStyle("add", license));
				}else {
					returnMap.put("afterossname", afterossname);
					returnMap.put("afterlicense", license);
				}
				returnList.add(returnMap);
			}
		}
		
		List<Map<String, Object>> deleteList = (List<Map<String, Object>>) compareMap.get("delete");
		
		if (deleteList.size() > 0) {
			for (int i=0; i<deleteList.size(); i++) {
				Map<String, String> returnMap = new HashMap<String, String>();

				returnMap.put("status", "delete");
				List<String> licenseArr = (List<String>) deleteList.get(i).get("license");
				String license = "";
				for (int j=0; j<licenseArr.size(); j++) {
					license += licenseArr.get(j).toString();
					if (j<licenseArr.size()-1) {
						license += ", ";
					}
				}
				
				String beforeossname = "";
				
				beforeossname += deleteList.get(i).get("name").toString();
				
				if (deleteList.get(i).get("version").toString().equals("") || deleteList.get(i).get("version") == null) {
				}else {
					beforeossname += " (" + deleteList.get(i).get("version").toString() + ")";
				}
				if (flag.equals("list")) {
					returnMap.put("beforeossname", changeStyle("delete", beforeossname));
					returnMap.put("beforelicense", changeStyle("delete", license));
				}else {
					returnMap.put("beforeossname", beforeossname);
					returnMap.put("beforelicense", license);
				}
				
				returnMap.put("afterossname", "");
				returnMap.put("afterlicense", "");
				
				returnList.add(returnMap);
			}
		}
		
		List<Map<String, Object>> changeList = (List<Map<String, Object>>) compareMap.get("change");
		
		if (changeList.size() > 0) {
			for (int i=0; i<changeList.size(); i++) {
				Map<String, String> returnMap = new HashMap<String, String>();

				returnMap.put("status", "change");
				
				List<String> prev_licenseArr = (List<String>) changeList.get(i).get("prev_license");
				List<String> now_licenseArr = (List<String>) changeList.get(i).get("now_license");
				
				String prev_license = "";
				String now_license = "";
				
				for (int j=0; j<prev_licenseArr.size(); j++) {
					prev_license += prev_licenseArr.get(j).toString();
					if (j<prev_licenseArr.size()-1) {
						prev_license += ", ";
					}
				}
				
				for (int a=0; a<now_licenseArr.size(); a++) {
					int chk = 0;
					for (int b=0; b<prev_licenseArr.size(); b++) {
						if (now_licenseArr.get(a).toString().equals(prev_licenseArr.get(b).toString())) {
							chk++;
						}
					}
					
					if (chk > 0) {
						now_license += now_licenseArr.get(a).toString();
					}else {
						if (flag.equals("list")) {
							now_license += changeStyle("change", now_licenseArr.get(a).toString());
						}else {
							now_license += now_licenseArr.get(a).toString();
						}
					}
					
					if (a<now_licenseArr.size()-1) {
						now_license += ", ";
					}
				}
				
				returnMap.put("beforelicense", prev_license);
				returnMap.put("afterlicense", now_license);
				
				String beforeossname = "";
				String afterossname = "";
				
				beforeossname += changeList.get(i).get("name").toString();
				afterossname += changeList.get(i).get("name").toString();
				
				if (changeList.get(i).get("prev_version").toString().equals("") || changeList.get(i).get("prev_version") == null) {
				}else {
					beforeossname += " (" + changeList.get(i).get("prev_version").toString() + ")";
				}
				
				if (changeList.get(i).get("now_version").toString().equals("") || changeList.get(i).get("now_version") == null) {
				}else {
					if (beforeossname.contains(changeList.get(i).get("now_version").toString())) {
						afterossname += " (" + changeList.get(i).get("now_version").toString() + ")";
					}else{
						if (flag.equals("list")) {
							afterossname += " (" + changeStyle("change", changeList.get(i).get("now_version").toString()) + ")";
						}else {
							afterossname += " (" + changeList.get(i).get("now_version").toString() + ")";
						}
					}
				}
				
				returnMap.put("beforeossname", beforeossname);
				returnMap.put("afterossname", afterossname);
				
				returnList.add(returnMap);
			}
		}
		
		return returnList;
	}

	private static String changeStyle(String flag, String license) {
		String returnVal = "";
		
		if (flag.equals("delete")){
			returnVal = "<span style=\"background-color:red;text-decoration:line-through;\">" + license + "</span>";
		}else if (flag.equals("change") || flag.equals("add")){
			returnVal = "<span style=\"background-color:yellow\">" + license + "</span>";
		}

		return returnVal;
	}
}
