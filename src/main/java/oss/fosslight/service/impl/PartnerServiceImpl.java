/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.PartnerWatcher;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.validation.custom.T2CoProjectValidator;
import oss.fosslight.repository.CommentMapper;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.CacheService;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.VulnerabilityService;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;

@Service
@Slf4j
public class PartnerServiceImpl extends CoTopComponent implements PartnerService {
	// Service
	@Autowired private ProjectService projectService;
	@Autowired private FileService fileService;
	@Autowired private OssService ossService;
	@Autowired VulnerabilityService vulnerabilityService;
	@Autowired CommentService commentService;

	// Mapper
	@Autowired private PartnerMapper partnerMapper;
	@Autowired private T2UserMapper userMapper;
	@Autowired private CommentMapper commentMapper;
	@Autowired private FileMapper fileMapper;
	@Autowired private ProjectMapper projectMapper;
	@Autowired private CacheService cacheService;
	
	@Autowired private SqlSessionFactory sqlSessionFactory;
	
	@Override
	@Cacheable(value="autocompletePartnerCache", key="{#root.methodName, #partnerMaster?.creator, #partnerMaster?.status}")
	public List<PartnerMaster> getPartnerNameList(PartnerMaster partnerMaster) {
		return partnerMapper.getPartnerNameList(partnerMaster);
	}
	
	@Override
	public PartnerMaster getPartnerMasterOne(PartnerMaster partnerMaster) {
		PartnerMaster result = new PartnerMaster();
		//파트너 마스터
		result = partnerMapper.selectPartnerMaster(partnerMaster);
		if (result != null) {
			result.setCommentText(avoidNull(commentMapper.getContent(result.getComment())));
			if (!isEmpty(result.getDocumentsFileId())) {
				result.setDocumentsFile(partnerMapper.selectDocumentsFile(result.getDocumentsFileId()));
			}
			if (!isEmpty(result.getConfirmationFileId())) {
				T2File confirmationFile = fileService.selectFileInfo(result.getConfirmationFileId());
				if (confirmationFile != null) {
					List<T2File> confirmationFileList = new ArrayList<>();
					confirmationFileList.add(confirmationFile);
					result.setConfirmationFile(confirmationFileList);
				}
			}
			if (!isEmpty(result.getOssFileId())) {
				T2File ossFile = fileService.selectFileInfo(result.getOssFileId());
				if (ossFile != null) {
					List<T2File> ossFileList = new ArrayList<>();
					ossFileList.add(ossFile);
					result.setOssFile(ossFileList);
				}
			}
		}
		
		//파트너 와쳐
		List<PartnerWatcher> watcher = partnerMapper.selectPartnerWatcher(partnerMaster);
		
		if (result != null) {
			if (!CollectionUtils.isEmpty(watcher)) {
				T2Users param = new T2Users();
				for (PartnerWatcher wat : watcher) {
					if (!isEmpty(wat.getParUserId())) {
						param.setUserId(wat.getParUserId());
						T2Users userInfo = userMapper.getUser(param);
						if (userInfo != null) {
							wat.setParDivision(userInfo.getDivision());
							wat.setParUserName(userInfo.getUserName());
							String codeNm = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, userInfo.getDivision());
							if (!isEmpty(codeNm)) {
								wat.setParDivisionName(codeNm);
							} else {
								wat.setParDivisionName(null);
							}
						}
					}
				}
				result.setPartnerWatcher(watcher);
			}
			String partnerId = "3rd_" + result.getPartnerId();
			int resultCnt = partnerMapper.getOssAnalysisDataCnt(partnerId);
			
			if (resultCnt > 0) {
				PartnerMaster analysisStatus = partnerMapper.getOssAnalysisData(partnerId);
				
				result.setAnalysisStartDate(analysisStatus.getAnalysisStartDate());
				result.setOssAnalysisStatus(analysisStatus.getOssAnalysisStatus());
			}
		}
		
		return result;
	}
	
	@Override
	@Transactional
	public Map<String, Object> getPartnerMasterList(PartnerMaster partnerMaster) {
		HashMap<String, Object> map = new HashMap<>();
		
		int records = partnerMapper.selectPartnerMasterTotalCount(partnerMaster);
		partnerMaster.setTotListSize(records);
		List<PartnerMaster> list = partnerMapper.selectPartnerList(partnerMaster);
		
		if (list != null) {
			boolean isNumberic = false;
			if (!isEmpty(partnerMaster.getPartnerName())) {
				isNumberic = partnerMaster.getPartnerName().chars().allMatch(Character::isDigit);
			}
			if (isNumberic) {
				List<PartnerMaster> filteredList = list.stream().filter(e -> e.getPartnerId().equalsIgnoreCase(partnerMaster.getPartnerName())).collect(Collectors.toList());
				if (filteredList != null && !filteredList.isEmpty()) {
					List<PartnerMaster> sortedList = new ArrayList<>();
					sortedList.addAll(filteredList);
					sortedList.addAll(list.stream().filter(e -> !e.getPartnerId().equalsIgnoreCase(partnerMaster.getPartnerName())).collect(Collectors.toList()));
					list = sortedList;
				}
			}
			
			list.forEach(bean -> {
				String conversionCveInfo = cacheService.findIdentificationMaxNvdInfo(bean.getPartnerId(), CoConstDef.CD_DTL_COMPONENT_PARTNER);
				if (conversionCveInfo != null) {
					String[] conversionCveData = conversionCveInfo.split("\\@");
					bean.setCvssScore(conversionCveData[3]);
					bean.setCveId(conversionCveData[4]);
					bean.setVulnYn(CoConstDef.FLAG_YES);
				}
			});
		}
		
		map.put("page", partnerMaster.getCurPage());
		map.put("total", partnerMaster.getTotBlockSize());
		map.put("records", records);
		map.put("rows", list);
		
		return map; 
	}
	
	@Override
	@Transactional
	public Map<String, Object> getPartnerStatusList(PartnerMaster partnerMaster) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		int records = 0;
		List<PartnerMaster> list = new ArrayList<PartnerMaster>();
		records = partnerMapper.selectPartnerStatusTotalCountUser(partnerMaster);
		
		partnerMaster.setTotListSize(records);
		list = partnerMapper.selectPartnerStatusUser(partnerMaster);

		map.put("page", partnerMaster.getCurPage());
		map.put("total", partnerMaster.getTotBlockSize());
		map.put("records", records);
		map.put("rows", list);
		
		return map; 
	}
	
	@Override
	@Cacheable(value="autocompletePartnerCache", key="{#root.methodName, #partnerMaster?.creator, #partnerMaster?.status}")
	public List<PartnerMaster> getPartnerSwNmList(PartnerMaster partnerMaster) {
		List<PartnerMaster> partnerList = partnerMapper.getPartnerSwNmList(partnerMaster);
		return partnerList;
	}
	
	@Override
	public List<T2Users> getUserList(T2Users t2Users) {
		List<T2Users> result = new ArrayList<T2Users>();
		
		result = partnerMapper.getUserList(t2Users);
		
		return result;
	}
	
	@Override
	@CacheEvict(value="autocompletePartnerCache", allEntries=true)
	public int updateReviewer(PartnerMaster vo) {
		int result = 0;
		
		result = partnerMapper.updateReviewer(vo);
		
		return result;
	}
	
	@Override
	public History work(Object param) {
		History h = new History();
		PartnerMaster vo = (PartnerMaster) param;
		PartnerMaster data = getPartnerMasterOne(vo);
		
		h.sethKey(data.getPartnerId());
		h.sethTitle(data.getPartnerName());
		h.sethType(CoConstDef.EVENT_CODE_PARTNER);
		h.setModifier(data.getCreator());
		h.setModifiedDate(data.getCreatedDate());
		h.sethComment("");
		h.sethData(data);
		
		return h;
	}
	
	@Override
	public List<PartnerMaster> getPartnerDuplication(PartnerMaster partner) {
		return partnerMapper.selectPartnerDuplication(partner);	
	}

	@Override
	@Transactional
	@CacheEvict(value="autocompletePartnerCache", allEntries=true)
	public void registPartnerMaster(PartnerMaster partnerMaster) {
		//파트너 마스터 테이블
		if (partnerMaster.getPartnerId() != null) {
			// admin이 아니라면 creator를 변경하지 않는다.
			if (!CommonFunction.isAdmin()) {
				partnerMaster.setCreator(null);
			} else {
				if (!isEmpty(partnerMaster.getCreator())) {
					List<T2Users> user = userMapper.getUserListByName(partnerMaster.getCreatorName());
					if (user != null && !user.isEmpty()) {
						String creator = user.get(0).getUserId();
						if (!partnerMaster.getCreator().equalsIgnoreCase(creator)) {
							partnerMaster.setCreator(creator);
						}
					}
				}
			}
		} else {
			partnerMaster.setCreator(partnerMaster.getLoginUserName());
		}
		
		boolean isNew = isEmpty(partnerMaster.getPartnerId());
		
		partnerMaster.setModifier(partnerMaster.getLoginUserName());
		partnerMaster.setCreatedDateCurrentTime();
		partnerMaster.setModifiedDate(partnerMaster.getCreatedDate());
		partnerMaster.setPublicYn(avoidNull(partnerMaster.getPublicYn(), CoConstDef.FLAG_YES));
		
		partnerMapper.registPartnerMaster(partnerMaster);
		
		String[] delDocumentsFile = partnerMaster.getDelDocumentsFile().split(",");
		
		if (delDocumentsFile.length > 0){
			for (String fileSeq : delDocumentsFile){
				if (!isEmpty(fileSeq)) {
					T2File delFile = new T2File();
					delFile.setFileSeq(fileSeq);

					partnerMapper.deleteFileBySeq(delFile);
					fileService.deletePhysicalFile(delFile, CoConstDef.CD_CHECK_OSS_PARTNER);
				}
			}
		}
		
		PartnerMaster beforePartner =  partnerMapper.selectPartnerMaster(partnerMaster);
		
		if (beforePartner != null) {
			if (!isEmpty(beforePartner.getConfirmationFileId())) {
				if (partnerMaster.getConfirmationFileId() == null || !partnerMaster.getConfirmationFileId().equals(beforePartner.getConfirmationFileId())) {
					T2File delFile = new T2File();
					delFile.setFileSeq(beforePartner.getConfirmationFileId());
					partnerMapper.deleteFileBySeq(delFile);
					fileService.deletePhysicalFile(delFile, CoConstDef.CD_CHECK_OSS_PARTNER);
				}
			}
		}
		
		// delete and insert
		if (isNew) {
			// partner watcher insert
			ArrayList<Map<String, String>> divisionList = new ArrayList<Map<String, String>>();
			ArrayList<Map<String, String>> emailList = new ArrayList<Map<String, String>>();

			if (partnerMaster.getWatchers()!= null) {
				String[] arr;
				
				for (String watcher : partnerMaster.getWatchers()) {
					Map<String, String> m = new HashMap<String, String>();
					arr = watcher.split("\\/");

					if (!"Email".equals(arr[1])) {
						partnerMaster.setParDivision(arr[0]);
						if (arr.length > 1){
							partnerMaster.setParUserId(arr[1]);
						}else{
							partnerMaster.setParUserId("");
						}
						partnerMaster.setParEmail("");

						m.put("division", partnerMaster.getParDivision());
						m.put("userId", partnerMaster.getParUserId());
						
						divisionList.add(m);
					} else {
						partnerMaster.setParDivision("");
						partnerMaster.setParUserId("");
						partnerMaster.setParEmail(arr[0]);

						m.put("email", partnerMaster.getParEmail());
						
						emailList.add(m);
					}

					List<PartnerMaster> watcherList = partnerMapper.selectWatchersCheck(partnerMaster);
					
					if (watcherList.size() == 0){
						partnerMapper.registPartnerWatcher(partnerMaster);
					}
				}
			}
			
			partnerMaster.setDivisionList(divisionList);
			partnerMaster.setEmailList(emailList);
			
			partnerMapper.deleteWatcher(partnerMaster);
		}
	}

	@Override
	@Transactional
	public void registOss(PartnerMaster partnerMaster, List<ProjectIdentification> ossComponents, List<List<ProjectIdentification>> ossComponentsLicense) {
		String refId = partnerMaster.getPartnerId();
		String refDiv = CoConstDef.CD_DTL_COMPONENT_PARTNER;
		
		PartnerMaster partnerInfo = new PartnerMaster();
		partnerInfo.setPartnerId(refId);
		partnerInfo = getPartnerMasterOne(partnerInfo);
		
		ossComponents =  projectService.convertOssNickName(ossComponents);
		ossComponentsLicense = projectService.convertLicenseNickName(ossComponentsLicense);
		
		for (ProjectIdentification bean : ossComponents) {
			bean.setObligationType(bean.getObligationLicense());
			bean.setBomWithAndroidFlag(CoConstDef.FLAG_YES);
			
			if (CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(bean.getObligationLicense())) {
				if (CoConstDef.FLAG_NO.equals(bean.getNotify()) && CoConstDef.FLAG_YES.equals(bean.getSource())) {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY);
				} else if (CoConstDef.FLAG_YES.equals(bean.getSource())) {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
				} else if (CoConstDef.FLAG_YES.equals(bean.getNotify())) {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
				} else if (CoConstDef.FLAG_NO.equals(bean.getNotify()) && CoConstDef.FLAG_NO.equals(bean.getSource())) {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK_SELECTED);
				} else {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK);
				}
			}
		}
		
		//oss insert
		Project prjParam = new Project();
		prjParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		prjParam.setReferenceId(partnerMaster.getPartnerId());
		int ossComponentIdx = projectMapper.selectOssComponentMaxIdx(prjParam);
		
		// Delete project all components and license
		projectMapper.resetOssComponentsAndLicense(refId, refDiv);

		final Map<String, List<OssComponentsLicense>> componentMultiLicenseMap = makeComponentMultiLicenseMap(ossComponentsLicense);
		String componentId;
		String downloadLocationUrl;
		String homepageUrl;
		final List<ProjectIdentification> insertOssComponentListWithComponentId = new ArrayList<>();
		final List<ProjectIdentification> insertOssComponentList = new ArrayList<>();
		final List<OssComponentsLicense> insertOssComponentLicenseList = new ArrayList<>();
		
		// 컴포넌트 등록
		for (int i = 0; i < ossComponents.size(); i++) {
			ProjectIdentification bean = ossComponents.get(i);
			
			bean.setReferenceId(refId);
			bean.setReferenceDiv(refDiv);
			
			// oss_id를 다시 찾는다. (oss name과 oss id가 일치하지 않는 경우가 있을 수 있음)
			bean = CommonFunction.findOssIdAndName(bean);
			if (isEmpty(bean.getOssId())) {
				bean.setOssId(null);
			}
			
			downloadLocationUrl = bean.getDownloadLocation();
			if (!StringUtil.isEmpty(downloadLocationUrl) && downloadLocationUrl.endsWith("/")) {
				bean.setDownloadLocation(downloadLocationUrl.substring(0, downloadLocationUrl.length()-1));
			} else if (StringUtil.isEmpty(downloadLocationUrl)) {
				bean.setDownloadLocation("");
			}
			homepageUrl = bean.getHomepage();
			if (!StringUtil.isEmpty(homepageUrl) && homepageUrl.endsWith("/")) {
				bean.setHomepage(homepageUrl.substring(0, homepageUrl.length()-1));
			} else if (StringUtil.isEmpty(homepageUrl)) {
				bean.setHomepage("");
			}
			
			componentId = StringUtil.avoidNull(bean.getComponentId(), bean.getGridId());
			
			// set component licnese
			if (componentMultiLicenseMap.containsKey(componentId)) {
				bean.setOssComponentsLicenseList(componentMultiLicenseMap.get(componentId));
			} else {
				bean.addOssComponentsLicense(CommonFunction.reMakeLicenseBean(bean, CoConstDef.LICENSE_DIV_SINGLE));
			}
			if (!isEmpty(bean.getCopyrightText())) {
				bean.setCopyrightText(StringUtils.trimWhitespace(bean.getCopyrightText()));
			}
			if (StringUtil.isEmpty(bean.getComponentIdx())) {
				bean.setComponentIdx(Integer.toString(ossComponentIdx++));
			}
			
			if (!StringUtil.contains(bean.getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
				insertOssComponentListWithComponentId.add(bean);
			} else {
				insertOssComponentList.add(bean);
			}
		}
		
		if (!insertOssComponentList.isEmpty() || !insertOssComponentListWithComponentId.isEmpty()) {
			try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
				ProjectMapper mapper = sqlSession.getMapper(ProjectMapper.class);
				int saveCnt = 0;
		      	for (ProjectIdentification bean : insertOssComponentListWithComponentId) {
		        	mapper.insertSrcOssList(bean);
		           	if (saveCnt++ == 1000) {
		            	sqlSession.flushStatements();
		              	saveCnt = 0;
		          	}
		      	}
		      	for (ProjectIdentification bean : insertOssComponentList) {
		          	mapper.insertSrcOssList(bean);
		         	if (saveCnt++ == 1000) {
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
		
		{
			partnerMapper.updateOssFileId(partnerMaster);
			if ((isEmpty(partnerMaster.getOssFileId()) && !isEmpty(partnerInfo.getOssFileId()))
					|| (!isEmpty(partnerMaster.getOssFileId()) && !isEmpty(partnerInfo.getOssFileId()) && !partnerMaster.getOssFileId().equals(partnerInfo.getOssFileId()))) {
				deleteFiles(partnerInfo.getOssFile());
			}
		}
		
		{
			PartnerMaster _ossidUpdateParam = new PartnerMaster();
			_ossidUpdateParam.setPartnerId(partnerMaster.getPartnerId());
			partnerMapper.updateComponentsOssId(_ossidUpdateParam);
		}
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
	
	@Transactional
	@Override
	@CacheEvict(value="autocompletePartnerCache", allEntries=true)
	public void deletePartnerMaster(PartnerMaster partnerMaster) {
		// Delete partner all components and license
		projectMapper.resetOssComponentsAndLicense(partnerMaster.getPartnerId(), null);
		//partnerWatcher
		partnerMapper.deleteWatcher(partnerMaster);
		//partnerMaster
		partnerMapper.deleteMaster(partnerMaster);
	}
	
	@Override
	@Transactional
	public void updatePartnerConfirm(PartnerMaster partnerMaster) {
		// oss id 등록
		Project project = new Project();
		project.setPrjId(partnerMaster.getPartnerId());
		project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		
		projectMapper.updateComponentsOssId(project);
		// license id 등록
		projectMapper.updateComponentsLicenseId(project);
		projectMapper.updateComponentsLicenseInfo(project);
		
		// 상태 변경
		changeStatus(partnerMaster, false);	
	}
	
	@Override
	@CacheEvict(value="autocompletePartnerCache", allEntries=true)
	public void changeStatus(PartnerMaster partnerMaster, boolean isCoReviewer) {
		CoMail mailBean = null;
		

		if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(partnerMaster.getStatus())) {
			PartnerMaster orgPartnerMaster = partnerMapper.selectPartnerMaster(partnerMaster);
			if (isEmpty(orgPartnerMaster.getReviewer()) && !isCoReviewer) {
				partnerMaster.setReviewer(partnerMaster.getLoginUserName());
				mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED);
				mailBean.setToIds(new String[] {partnerMaster.getLoginUserName()});
				mailBean.setParamPartnerId(partnerMaster.getPartnerId());
			}
		}
		
		partnerMapper.changeStatus(partnerMaster);
		
		if (mailBean != null) {
			try {
				CoMailManager.getInstance().sendMail(mailBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	@Cacheable(value="autocompletePartnerCache", key="{#root.methodName, #partnerMaster?.creator, #partnerMaster?.status}")
	public List<PartnerMaster> getPartnerSwVerList(PartnerMaster partnerMaster) {
		return partnerMapper.getPartnerSwVerList(partnerMaster);
	}

	@Override
	public String checkViewOnly(String partnerId) {
		String rtnFlag = CoConstDef.FLAG_NO;
		
		if (!CommonFunction.isAdmin()) {
			PartnerMaster param = new PartnerMaster();
			param.setPartnerId(partnerId);
			
			if (partnerMapper.checkWatcherAuth(param) == 0) {
				rtnFlag = CoConstDef.FLAG_YES;
			}
		}
		
		return rtnFlag;
	}

	@Override
	public void addWatcher(PartnerMaster project) {
		if (!isEmpty(project.getParEmail())) {
			// 이미 추가된 watcher 체크
			if (partnerMapper.existsWatcherByEmail(project) == 0) {
				// watcher 추가
				partnerMapper.insertWatcher(project);
				
				// email 발송
				try {
					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_INVATED);
					mailBean.setParamPartnerId(project.getPartnerId());
					mailBean.setParamUserId(project.getLoginUserName());
					mailBean.setParamEmail(project.getParEmail());
					
					CoMailManager.getInstance().sendMail(mailBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} else {
			// 이미 추가된 watcher 체크
			if (partnerMapper.existsWatcherByUser(project) == 0) {
				// watcher 추가
				if (partnerMapper.existsWatcherByUserDivistion(project) > 0) {
					partnerMapper.updateWatcherDivision(project);
				} else {
					partnerMapper.insertWatcher(project);
				}
			}
		}
	}
	@Override
	public void removeWatcher(PartnerMaster project) {
		partnerMapper.removeWatcher(project);		
	}

	@Override
	public List<PartnerMaster> copyWatcher(PartnerMaster project){
		return partnerMapper.copyWatcher(project);
	}

	@Override
	public List<String> getInvateWatcherList(String prjId) {
		return partnerMapper.getInvateWatcherList(prjId);
	}

	@Override
	public void updatePublicYn(PartnerMaster partner) {
		partnerMapper.updatePublicYn(partner);
	}
	
	@Override
	public boolean existsWatcher(PartnerMaster project) {
		boolean result = false;
		
		int i = partnerMapper.existsWatcher(project);
		
		if (i > 0){
			result = true;
		}	
		
		return result;
	}
	
	@Override
	public Map<String, Object> getPartnerValidationList(PartnerMaster partnerMaster) {
		return getPartnerValidationList(partnerMaster, false);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getPartnerValidationList(PartnerMaster partnerMaster, boolean coReview){
		ProjectIdentification prjBean = new ProjectIdentification();
		
		prjBean.setReferenceId(partnerMaster.getPartnerId());
		prjBean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
		prjBean.setMerge(CoConstDef.FLAG_NO);
		Map<String, Object> partnerList = projectService.getIdentificationGridList(prjBean, true);
		
		PartnerMaster partnerInfo = new PartnerMaster();
		T2CoProjectValidator pv = new T2CoProjectValidator();
		
		if (prjBean.getPartnerId() == null){
			partnerInfo.setPartnerId(prjBean.getReferenceId());
		}else{
			partnerInfo.setPartnerId(prjBean.getPartnerId());
		}
		
		partnerInfo = getPartnerMasterOne(partnerInfo);

		if (partnerInfo != null) {
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BOM_MERGE);

			pv.setAppendix("bomList", (List<ProjectIdentification>) partnerList.get("rows"));
			
			if ((CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(partnerInfo.getStatus())
					|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(partnerInfo.getStatus()))
					&& (CommonFunction.isAdmin() || coReview)) {
				pv.setCheckForAdmin(true);
			}

			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				partnerList.replace("rows", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) partnerList.get("rows"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false, true));
				if (!vr.isValid()) {
					partnerList.put("validData", vr.getValidMessageMap());
				}
				if (!vr.isDiff()) {
					partnerList.put("diffData", vr.getDiffMessageMap(true));
				}
				if (vr.hasInfo()) {
					partnerList.put("infoData", vr.getInfoMessageMap());
				}
			}
		}
		
		return partnerList;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getFilterdList(Map<String, Object> paramMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();

		List<ProjectIdentification> mainData = (List<ProjectIdentification>) paramMap.get("rows");
		Map<String, String> errorMap = (Map<String, String>) paramMap.get("validData");
		List<String> duplicateList = new ArrayList<>();
		List<String> componentIdList = new ArrayList<>();
		
		if (errorMap != null) {
			for (ProjectIdentification prjBean : mainData) {
				String checkKey = prjBean.getOssName() + "_" + prjBean.getOssVersion();
				// 중복된 oss Info는 제외함.
				if (duplicateList.contains(checkKey)) {
					continue;
				}
				
				String componentId = prjBean.getComponentId();
				String ossNameErrorMsg = errorMap.containsKey("ossName."+componentId) ? errorMap.get("ossName."+componentId) : "";
				String ossVersionErrorMsg = errorMap.containsKey("ossVersion."+componentId) ? errorMap.get("ossVersion."+componentId) : "";
				
				if (ossNameErrorMsg.indexOf("New") > -1
						|| ossVersionErrorMsg.indexOf("New") > -1) {
					duplicateList.add(checkKey);
					componentIdList.add(componentId);
				}
			}
		}
		
		resultMap.put("componentIdList", componentIdList);
		resultMap.put("isValid", true);
		
		return resultMap;
	}

	public Map<String, Object> getIdentificationGridList(ProjectIdentification identification) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<ProjectIdentification> list = null;
		identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		
		boolean isLoadFromProject = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefPrjId());
		
		if (isLoadFromProject) {
			identification.setReferenceId(identification.getRefPrjId());
		}
		
		boolean isApplyFromBat = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefBatId());
		
		if (isApplyFromBat) {
			identification.setReferenceId(identification.getRefBatId());
		}
			
		HashMap<String, Object> subMap = new HashMap<String, Object>();
			
		list = projectMapper.selectIdentificationGridList(identification);
		list.sort(Comparator.comparing(ProjectIdentification::getComponentId));
		
		if (list != null && !list.isEmpty()) {

			ProjectIdentification param = new ProjectIdentification();
			param.setReferenceDiv(identification.getReferenceDiv());
			param.setReferenceId(identification.getReferenceId());
			OssMaster ossParam = new OssMaster();
			
			// components license 정보를 한번에 가져온다
			for (ProjectIdentification bean : list) {
				param.addComponentIdList(bean.getComponentId());
				
				if (!isEmpty(bean.getOssId())) {
					ossParam.addOssIdList(bean.getOssId());
				}
				
				// oss Name은 작성하고, oss Version은 작성하지 않은 case경우 해당 분기문에서 처리
				if (isEmpty(bean.getCveId()) 
						&& isEmpty(bean.getOssVersion()) 
						&& !isEmpty(bean.getCvssScoreMax())
						&& !("-".equals(bean.getOssName()))){ 
					String[] cvssScoreMax = bean.getCvssScoreMax().split("\\@");
					bean.setCvssScore(cvssScoreMax[0]);
					bean.setCveId(cvssScoreMax[1]);
				}
			}
				
			// oss id로 oss master에 등록되어 있는 라이선스 정보를 취득
			Map<String, OssMaster> componentOssInfoMap = null;
			
			if (ossParam.getOssIdList() != null && !ossParam.getOssIdList().isEmpty()) {
				componentOssInfoMap = ossService.getBasicOssInfoListById(ossParam);
			}
				
			List<ProjectIdentification> licenseList = projectMapper.identificationSubGrid(param);
			
			for (ProjectIdentification licenseBean : licenseList) {
				for (ProjectIdentification bean : list) {
					if (licenseBean.getComponentId().equals(bean.getComponentId())) {
						// 수정가능 여부 초기설정
						licenseBean.setEditable(CoConstDef.FLAG_YES);
						bean.addComponentLicenseList(licenseBean);
						break;
					}
				}
			}

			// license 정보 등록
			for (ProjectIdentification bean : list) {
				if (bean.getComponentLicenseList()!=null){
					String licenseCopy = "";
						
					// multi dual 라이선스의 경우, main row에 표시되는 license 정보는 OSS List에 등록되어진 라이선스를 기준으로 표시한다.
					// ossId가 없는 경우는 기본적으로 subGrid로 등록될 수 없다
					// 이짓거리를 하는 두번째 이유는, subgrid 에서 사용자가 추가한 라이선스와 oss 에 등록되어 있는 라이선스를 구분하기 위함
					if (componentOssInfoMap == null) {
						componentOssInfoMap = new HashMap<>();
					}
					
					OssMaster ossBean = componentOssInfoMap.get(bean.getOssId());
						
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
						}
						
						bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
					} else {
						// license text는 표시하지 않기 때문에 설정할 필요는 없음
						for (ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
							if (!isEmpty(licenseBean.getCopyrightText())) {
								licenseCopy += (!isEmpty(licenseCopy) ? "\r\n" : "") + licenseBean.getCopyrightText();
							}
						}
						
						bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
					}
					
					bean.setLicenseNameExistsYn(CommonFunction.existsLicenseName(bean.getComponentLicenseList()) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
					bean.setLicenseUserGuideStr(CommonFunction.checkLicenseUserGuide(bean.getComponentLicenseList()));
					bean.setLicenseUserGuideYn(isEmpty(bean.getLicenseUserGuideStr()) ? CoConstDef.FLAG_NO : CoConstDef.FLAG_YES);
					bean.setRestriction(CommonFunction.setLicenseRestrictionList(bean.getComponentLicenseList()));
					
					// subGrid의 Item 추출을 위해 별도의 map으로 구성한다.
					// 부몬의 component_id를 key로 관리한다.
					subMap.put(bean.getGridId(), bean.getComponentLicenseList());
				}	
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

		return map;
	}

	@Override
	public int updateDivision(String partnerId, String division) {
		return partnerMapper.updateDivision(partnerId, division);
	}

	@Override
	public Map<String, List<PartnerMaster>> updatePartnerDivision(PartnerMaster partnerMaster) {
		Map<String, List<PartnerMaster>> updatePartnerDivMap = new HashMap<>();
		List<PartnerMaster> beforeBeanList = new ArrayList<>();
		List<PartnerMaster> afterBeanList = new ArrayList<>();
		
		PartnerMaster param = new PartnerMaster();
		String division = partnerMaster.getParDivision();
		String comment = "";
		
		for (String partnerId : partnerMaster.getPartnerIds()) {
			param.setPartnerId(partnerId);
			PartnerMaster beforeBean = getPartnerMasterOne(param);
			
			if (!avoidNull(beforeBean.getDivision(), "").equals(division)) {
				PartnerMaster afterBean = getPartnerMasterOne(param);
				afterBean.setDivision(division);
				
				partnerMapper.updateDivision(partnerId, division);
				
				comment = CommonFunction.getDiffItemCommentPartner(beforeBean, afterBean);
				
				afterBean.setUserComment(comment);
				
				beforeBeanList.add(beforeBean);
				afterBeanList.add(afterBean);
			}
		}
		
		if (!beforeBeanList.isEmpty()) {
			updatePartnerDivMap.put("before", beforeBeanList);
		}
		
		if (!afterBeanList.isEmpty()) {
			updatePartnerDivMap.put("after", afterBeanList);
		}
		
		return updatePartnerDivMap;
	}

	@Override
	public void updateDescription(PartnerMaster partnerMaster){
		partnerMapper.updateDescription(partnerMaster);
	}

	@Override
	@Cacheable(value="autocompletePartnerCache", key="{#root.methodName, #partnerMaster?.creator, #partnerMaster?.status}")
	public List<PartnerMaster> getPartnerIdList(PartnerMaster partnerMaster) {
		return partnerMapper.getPartnerIdList(partnerMaster);
	}

	@Override
	public Map<String, Object> getExportDataForSbomInfo(PartnerMaster partner) {
		Map<String, Object> model = new HashMap<String, Object>();
		int dashSeq = 0;
		
		List<OssComponents> ossComponentList = partnerMapper.selectOssComponentsSbomList(partner);
		
		// TYPE별 구분
		Map<String, OssComponents> noticeInfo = new HashMap<>();
		Map<String, OssComponents> srcInfo = new HashMap<>();
		Map<String, OssComponents> notObligationInfo = new HashMap<>();
		
		OssComponents ossComponent;
		String ossInfoUpperKey = "";
		
		for (OssComponents bean : ossComponentList) {
			if (isEmpty(bean.getOssName()) || isEmpty(bean.getLicenseName())) continue;
			
			ossInfoUpperKey = (bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase();
			if (CoCodeManager.OSS_INFO_UPPER.containsKey(ossInfoUpperKey) && isEmpty(bean.getHomepage())) {
				bean.setHomepage(CoCodeManager.OSS_INFO_UPPER.get(ossInfoUpperKey).getHomepage());
			}
			
			String componentKey = (bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
			if ("-".equals(bean.getOssName())) {
				componentKey += dashSeq++;
			}
			
			boolean isDisclosure = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType()) || CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY.equals(bean.getObligationType());
			boolean isNotice = CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType());
			
			boolean addDisclosure = srcInfo.containsKey(componentKey);
			boolean addNotice = noticeInfo.containsKey(componentKey);
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
			
			OssComponentsLicense license = new OssComponentsLicense();
			license.setLicenseId(bean.getLicenseId());
			license.setLicenseName(bean.getLicenseName());
			license.setLicenseText(bean.getLicenseText());
			license.setAttribution(bean.getAttribution());
			
			if (!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
				ossComponent.addOssComponentsLicense(license);
				bean.setOssCopyright(findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright()));
				
				if (!isEmpty(bean.getOssCopyright())) {
					String addCopyright = avoidNull(ossComponent.getCopyrightText());
					
					if (!isEmpty(ossComponent.getCopyrightText())) {
						addCopyright += "\r\n";
					}
					 
					addCopyright += bean.getOssCopyright();
					ossComponent.setCopyrightText(addCopyright);
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
		
		List<OssComponents> addOssComponentList = partnerMapper.selectOssComponentsSbomListClassAppend(partner);
		
		if (addOssComponentList != null) {
			ossComponent = null;
			Map<String, List<String>> addOssComponentCopyright = new HashMap<>();
			
			for (OssComponents bean : addOssComponentList) {
				if (isEmpty(bean.getLicenseName())) continue;
				
				String componentKey = (bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
				
				List<String> copyrightList = addOssComponentCopyright.containsKey(componentKey) 
						? (List<String>) addOssComponentCopyright.get(componentKey) 
						: new ArrayList<>();
				
				if (!isEmpty(bean.getCopyrightText())) {
					for (String copyright : bean.getCopyrightText().split("\n")) {
						copyrightList.add(copyright);
					}
				}
				
				boolean addSrcInfo = srcInfo.containsKey(componentKey);
				boolean addNoticeInfo = noticeInfo.containsKey(componentKey);
				boolean addNotObligationInfo = notObligationInfo.containsKey(componentKey);
				
				if (addSrcInfo) {
					ossComponent = srcInfo.get(componentKey);
				} else if (addNoticeInfo) {
					ossComponent = noticeInfo.get(componentKey);
				} else if (addNotObligationInfo) {
					ossComponent = notObligationInfo.get(componentKey);
				} else {
					ossComponent = bean;
				}
				
				if ("-".equals(bean.getOssName()) || !CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(bean.getOssName().toUpperCase())
						|| !CoCodeManager.OSS_INFO_UPPER.containsKey((bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase())) {
					if (!isEmpty(bean.getDownloadLocation()) && isEmpty(bean.getHomepage())) {
						ossComponent.setHomepage(bean.getDownloadLocation());
					}
				}
				
				if ("-".equals(bean.getOssName())) {
					componentKey += dashSeq++;
				}
				
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());
				
				if (!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
					ossComponent.addOssComponentsLicense(license);
				}
				
				copyrightList = copyrightList.stream()
												.filter(CommonFunction.distinctByKey(c -> avoidNull(c).trim().toUpperCase()))
												.collect(Collectors.toList()); 
				ossComponent.setCopyrightText(String.join("\r\n", copyrightList));
				addOssComponentCopyright.put(componentKey, copyrightList);
				
				boolean disclosureFlag = false;
				boolean noticeFlag = false;
				
				switch(CommonFunction.checkObligationSelectedLicense(ossComponent.getOssComponentsLicense())){
					case CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE: 
						srcInfo.put(componentKey, ossComponent);
						disclosureFlag = true;
						break;
					case CoConstDef.CD_DTL_OBLIGATION_NOTICE: 
						noticeInfo.put(componentKey, ossComponent);
						noticeFlag = true;
						break;
				}
				
				if (!disclosureFlag && !noticeFlag) {
					notObligationInfo.put(componentKey, ossComponent);
				}
			}
		}
		
		boolean isTextNotice = true;
		
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

	@Override
	public Map<String, Object> checkSelectDownloadFile(PartnerMaster partnerMaster) {
		Map<String, Object> resMap = new HashMap<>();
		boolean emptyCheckFlag = false;
		
		List<OssComponents> list = partnerMapper.checkSelectDownloadFile(partnerMaster);
		for (OssComponents oss : list) {
			if (isEmpty(oss.getOssName()) || isEmpty(oss.getLicenseName())) {
				emptyCheckFlag = true;
				break;
			}
		}
		
		if (emptyCheckFlag) {
			resMap.put("isValid", false);
		} else {
			resMap.put("isValid", true);
		}
		
		return resMap;
	}

	@Override
	public List<OssComponents> getSecurityGridList(boolean isDemo, PartnerMaster partnerMaster) {
		List<OssComponents> needToResolveList = new ArrayList<>();
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(partnerMaster.getPartnerId());
		identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
		identification.setStandardScore(Float.valueOf(CoCodeManager.getCodeExpString(CoConstDef.CD_SECURITY_VULNERABILITY_SCORE, CoConstDef.CD_SECURITY_VULNERABILITY_DETAIL_SCORE)));
		
		List<String> deduplicatedkey = new ArrayList<>();
		List<String> caseWithoutVersionKey = new ArrayList<>();
		boolean activateFlag;
		OssComponents oc = null;
		String ossVersion = "";
		String vulnerabilityLink = "";
		int gridIdx = 1;
		
		List<ProjectIdentification> list = projectMapper.selectSecurityListForProject(identification);
		Map<String, List<Map<String, Object>>> cpeInfoMap = new HashMap<>();
		Map<String, String> patchLinkMap = new HashMap<>();
		
		if (!isDemo) {
			List<Map<String, Object>> cpeInfoList = projectMapper.getCpeInfoAndRangeForProject(identification);
			for (Map<String, Object> cpeInfo : cpeInfoList) {
				String key = ((String) cpeInfo.get("cveId") + "_" + (String) cpeInfo.get("product")).toUpperCase();
				String key2 = (String) cpeInfo.get("cveId");
				String patchLink = (String) cpeInfo.getOrDefault("patchLink", "");
				
				List<Map<String, Object>> cpeInfoMapList = null;
				if (cpeInfoMap.containsKey(key)) {
					cpeInfoMapList = cpeInfoMap.get(key);
				} else {
					cpeInfoMapList = new ArrayList<>();
				}
				cpeInfoMapList.add(cpeInfo);
				cpeInfoMap.put(key, cpeInfoMapList);
				
				if (!patchLinkMap.containsKey(key2) && !isEmpty(patchLink)) {
					patchLinkMap.put(key2, patchLink);
				}
			}
		}
		
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
			String key2 = (pi.getCveId() + "_" + pi.getOssName().replaceAll(" ", "_")).toUpperCase();
			if (!deduplicatedkey.contains(key)) {
				deduplicatedkey.add(key);
				
				if (activateFlag) {
					vulnerabilityLink = CommonFunction.getProperty("server.domain");
					vulnerabilityLink += "/vulnerability/vulnpopup?ossName=" + pi.getOssName() + "&ossVersion=" + ossVersion;
				} else {
					vulnerabilityLink = "https://nvd.nist.gov/vuln/detail/" + pi.getCveId();
				}
				
				oc = new OssComponents();
				oc.setGridId("jqg_sec_" + partnerMaster.getPartnerId() + "_" + String.valueOf(gridIdx));
				oc.setOssName(pi.getOssName());
				oc.setOssVersion(pi.getOssVersion());
				oc.setCvssScore(pi.getCvssScore());
				
				if (!activateFlag) {
					oc.setCveId(pi.getCveId());
					oc.setPublDate(pi.getPublDate());
				}
				
				oc.setActivateFlag(activateFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
				oc.setVulnerabilityLink(vulnerabilityLink);
				oc.setVulnerabilityResolution("");
				
				if (!activateFlag) {
					if (!isDemo && cpeInfoMap.containsKey(key2)) {
						List<Map<String, Object>> matchCpeInfoList = cpeInfoMap.get(key2);
						String criteria = "";
						String verStartEndRange = "";
						String checkUrl = "";
						
						boolean emptyFlag = false;
						for (Map<String, Object> cpeInfo : matchCpeInfoList) {
							Map<String, Object> paramMap = new HashMap<>();
							paramMap = cpeInfo;
							if (!paramMap.containsKey("verStartInc")) {
								paramMap.put("verStartInc", "");
							}
							if (!paramMap.containsKey("verEndInc")) {
								paramMap.put("verEndInc", "");
							}
							if (!paramMap.containsKey("verStartExc")) {
								paramMap.put("verStartExc", "");
							}
							if (!paramMap.containsKey("verEndExc")) {
								paramMap.put("verEndExc", "");
							}
							if (!vulnerabilityService.getCpeMatchForCpeInfoCnt(paramMap)) {
								continue;
							}
							
							if (cpeInfo.containsKey("criteria")) {
								String cpeInfoCriteria = (String) cpeInfo.get("criteria");
								String[] url = cpeInfoCriteria.split(":");
								if (!emptyFlag) checkUrl = cpeInfoCriteria;
								if (!criteria.contains(cpeInfoCriteria) && url[5].equals("*") || url[5].equals(oc.getOssVersion())) {
									criteria += cpeInfoCriteria + ",";
								}
							}
							if (cpeInfo.containsKey("verStartInc")) {
								verStartEndRange += "From (including) : " + (String) cpeInfo.get("verStartInc")+",";
							}
							if (cpeInfo.containsKey("verEndInc")) {
								verStartEndRange += "Up to (including) : " + (String) cpeInfo.get("verEndInc")+",";
							}
							if (cpeInfo.containsKey("verStartExc")) {
								verStartEndRange += "From (excluding) : " + (String) cpeInfo.get("verStartExc")+",";
							}
							if (cpeInfo.containsKey("verEndExc")) {
								verStartEndRange += "Up to (excluding) : " + (String) cpeInfo.get("verEndExc")+",";
							}
							
							emptyFlag = true;
						}
						
						if (!isEmpty(criteria)) {
							criteria = criteria.substring(0, criteria.length()-1);
							oc.setCpeName(criteria);
						} else {
							if (!isEmpty(checkUrl)) {
								String[] url = checkUrl.split(":");
								String changeUrl = "";
								int i = 0;
								for (String urlData : url) {
									if (i == 5) {
										changeUrl += "*:";
									} else {
										changeUrl += urlData + ":";
									}
									i++;
								}
								changeUrl = changeUrl.substring(0, changeUrl.length()-1);
								oc.setCpeName(changeUrl);
							}
						}
						
						if (!isEmpty(verStartEndRange)) {
							verStartEndRange = verStartEndRange.substring(0, verStartEndRange.length()-1);
							oc.setVerStartEndRange(verStartEndRange);
						} else {
							oc.setVerStartEndRange("N/A");
						}
					}
					
					if (patchLinkMap.containsKey(pi.getCveId())) {
						oc.setOfficialPatchLink(patchLinkMap.get(pi.getCveId()));
					} else {
						oc.setOfficialPatchLink("N/A");
					}
					
					oc.setSecurityPatchLink("N/A");
				}
				
				needToResolveList.add(oc);
			}
		}
		
		return needToResolveList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registOssWhenRegistPartner(PartnerMaster partnerMaster) {
		String resCd = "00";
		String readType = CoConstDef.CD_CHECK_OSS_PARTNER;
		String fileSeq = partnerMaster.getOssFileId();
		List<String> sheetNums = new ArrayList<>();
		for (String sheetNum : partnerMaster.getOssFileSheetNo().split(",")) {
			sheetNums.add(sheetNum);
		}
		List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
		List<OssComponents> reportData = new ArrayList<OssComponents>();
		List<String> errMsgList = new ArrayList<>();
		Map<String, String> emptyErrMsg = new HashMap<>();
		
		try {
			ExcelUtil.readReport(readType, true, sheetNums.toArray(new String[sheetNums.size()]), fileSeq, reportData, errMsgList, emptyErrMsg);
			Map<String, Object> resultMap = CommonFunction.makeGridDataFromReport(null, null, null, reportData, fileSeq, readType);
			if (resultMap.containsKey("mainData")) {
				ossComponents = (List<ProjectIdentification>) resultMap.get("mainData");
				if (!CollectionUtils.isEmpty(ossComponents)) {
					List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
					ossComponentsLicense = CommonFunction.mergeGridAndSession(
							CommonFunction.makeSessionKey(loginUserName(),CoConstDef.CD_DTL_COMPONENT_PARTNER, partnerMaster.getPartnerId()), ossComponents, ossComponentsLicense,
							CommonFunction.makeSessionReportKey(loginUserName(),CoConstDef.CD_DTL_COMPONENT_PARTNER, partnerMaster.getPartnerId()));
					
					Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(ossComponents, ossComponentsLicense);
					ossComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
					ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
					registOss(partnerMaster, ossComponents, ossComponentsLicense);
					resCd="10";
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		if ("10".equals(resCd)) {
			String prjId = partnerMaster.getPartnerId();
			try {
				if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, partnerMaster.getOssFileId())) != null) {
					String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(
							loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE,
							partnerMaster.getOssFileId()), true);
					
					if (!isEmpty(changedLicenseName)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setContents(changedLicenseName);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void deletePartnerRefFiles(PartnerMaster partnerMaster) {
		deleteFiles(partnerMaster.getOssFile());
		deleteFiles(partnerMaster.getConfirmationFile());
		deleteFiles(partnerMaster.getDocumentsFile());
	}
	
	private void deleteFiles(List<T2File> list) {
		if (list != null) {
			for (T2File fileInfo : list) {
				partnerMapper.deleteFileBySeq(fileInfo);
				fileService.deletePhysicalFile(fileInfo, CoConstDef.CD_CHECK_OSS_PARTNER);
			}
		}
	}
}
