/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
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
import oss.fosslight.service.FileService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;

@Service
@Slf4j
public class PartnerServiceImpl extends CoTopComponent implements PartnerService {
	// Service
	@Autowired ProjectService projectService;
	@Autowired FileService fileService;
	
	// Mapper
	@Autowired PartnerMapper partnerMapper;
	@Autowired CommentMapper commentMapper;
	@Autowired FileMapper fileMapper;
	@Autowired ProjectMapper projectMapper;
	
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
		result.setCommentText(commentMapper.getContent(result.getComment()));
		//파트너 와쳐
		List<PartnerWatcher> watcher = partnerMapper.selectPartnerWatcher(partnerMaster);
		
		if(watcher != null){
			result.setPartnerWatcher(watcher);	
		}
		String partnerId = "3rd_" + result.getPartnerId();
		int resultCnt = partnerMapper.getOssAnalysisDataCnt(partnerId);
		
		if(resultCnt > 0) {
			PartnerMaster analysisStatus = partnerMapper.getOssAnalysisData(partnerId);
			
			result.setAnalysisStartDate(analysisStatus.getAnalysisStartDate());
			result.setOssAnalysisStatus(analysisStatus.getOssAnalysisStatus());
		}
		
		return result;
	}
	
	@Override
	@Transactional
	public Map<String, Object> getPartnerMasterList(PartnerMaster partnerMaster) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		int records = 0;
		List<PartnerMaster> list = new ArrayList<PartnerMaster>();
		
		if("ROLE_USER".equals(partnerMaster.getLoginUserRole())) {
			records = partnerMapper.selectPartnerMasterTotalCountUser(partnerMaster);
			partnerMaster.setTotListSize(records);
			list = partnerMapper.selectPartnerListUser(partnerMaster);
		} else {
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
	public void registPartnerMaster(PartnerMaster partnerMaster, List<ProjectIdentification> ossComponents, List<List<ProjectIdentification>> ossComponentsLicense) {
		PartnerMaster beforePartner =  partnerMapper.selectPartnerMaster(partnerMaster);
		
		//파트너 마스터 테이블
		if(partnerMaster.getPartnerId() != null) {
			// admin이 아니라면 creator를 변경하지 않는다.
			if(!CommonFunction.isAdmin()) {
				partnerMaster.setCreator(null);
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
		
		if(delDocumentsFile.length > 0){
			for(String fileSeq : delDocumentsFile){
				if(!isEmpty(fileSeq)) {
					T2File delFile = new T2File();
					delFile.setFileSeq(fileSeq);
					delFile.setGubn("A");
					
					fileMapper.updateFileDelYnKessan(delFile);
					fileService.deletePhysicalFile(delFile, "PARTNER");
				}
			}
		}
		
		if(!isEmpty(beforePartner.getConfirmationFileId())) {
			if(partnerMaster.getConfirmationFileId() == null || !partnerMaster.getConfirmationFileId().equals(beforePartner.getConfirmationFileId())) {
				T2File delFile = new T2File();
				delFile.setFileSeq(beforePartner.getConfirmationFileId());
				fileService.deletePhysicalFile(delFile, "PARTNER");
			}
		}
		
		if(!isEmpty(beforePartner.getOssFileId())) {
			if(partnerMaster.getOssFileId() == null || !partnerMaster.getOssFileId().equals(beforePartner.getOssFileId())) {
				T2File delFile = new T2File();
				delFile.setFileSeq(beforePartner.getOssFileId());
				fileService.deletePhysicalFile(delFile, "PARTNER");
			}
		}
		
		//delete License
		ProjectIdentification deleteparam = new ProjectIdentification();
		deleteparam.setReferenceId(partnerMaster.getPartnerId());
		deleteparam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		List<OssComponents> componentsId = projectMapper.selectComponentId(deleteparam);
		
		for(int j = 0; j < componentsId.size(); j++){
			projectMapper.deleteOssComponentsLicense(componentsId.get(j));
		}
		
		ossComponents =  projectService.convertOssNickName(ossComponents);
		ossComponentsLicense = projectService.convertLicenseNickName(ossComponentsLicense);
		
		for(ProjectIdentification bean : ossComponents) {
			bean.setObligationType(bean.getObligationLicense());
			bean.setBomWithAndroidFlag(CoConstDef.FLAG_YES);
			
			if(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(bean.getObligationLicense())) {
				if(CoConstDef.FLAG_YES.equals(bean.getSource())) {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
				} else if(CoConstDef.FLAG_YES.equals(bean.getNotify())) {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
				} else if(CoConstDef.FLAG_NO.equals(bean.getNotify()) && CoConstDef.FLAG_NO.equals(bean.getSource())) {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK_SELECTED);
				} else {
					bean.setObligationType(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK);
				}
			}
		}
		
		//oss insert
		//deleteRows
		List<String> deleteRows = new ArrayList<String>();
		
		Project prjParam = new Project();
		prjParam.setReferenceDiv("20");
		prjParam.setReferenceId(partnerMaster.getPartnerId());
		int ossComponentIdx = projectMapper.selectOssComponentMaxIdx(prjParam);

		// 컴포넌트 등록
		for (int i = 0; i < ossComponents.size(); i++) {
			String downloadLocationUrl = ossComponents.get(i).getDownloadLocation();
			String homepageUrl = ossComponents.get(i).getHomepage();
			
			if(!isEmpty(downloadLocationUrl)) {
				if(downloadLocationUrl.endsWith("/")) {
					ossComponents.get(i).setDownloadLocation(downloadLocationUrl.substring(0, downloadLocationUrl.length()-1));
				}
			}
			
			if(!isEmpty(homepageUrl)) {
				if(homepageUrl.endsWith("/")) {
					ossComponents.get(i).setHomepage(homepageUrl.substring(0, homepageUrl.length()-1));
				}
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
				} else { //싱글라이센스일경우
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
				ossComponents.get(i).setReferenceId(partnerMaster.getPartnerId());
				ossComponents.get(i).setReferenceDiv("20");
				ossComponents.get(i).setComponentIdx(Integer.toString(ossComponentIdx++));
				projectMapper.insertSrcOssList(ossComponents.get(i));
				
				deleteRows.add(ossComponents.get(i).getComponentId());
				
				//멀티라이센스일 경우
				if("M".equals(ossComponents.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentsLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							String gridId = comLicense.getGridId();
							
							if(isEmpty(gridId)) {
								continue;
							}
							
							gridId = gridId.split("-")[0];
							
							if(exComponentId.equals(comLicense.getComponentId())
									|| exComponentId.equals(gridId)){
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
								license.setExcludeYn(comLicense.getExcludeYn());
								
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
		
		{
			PartnerMaster _ossidUpdateParam = new PartnerMaster();
			_ossidUpdateParam.setPartnerId(partnerMaster.getPartnerId());
			partnerMapper.updateComponentsOssId(_ossidUpdateParam);
		}
		
		//delete
		OssComponents param = new OssComponents();
		param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		param.setReferenceId(partnerMaster.getPartnerId());
		param.setOssComponentsIdList(deleteRows);
		
		projectMapper.deleteOssComponentsWithIds(param);
		
		// delete and insert
		if(isNew) {
			// partner watcher insert
			ArrayList<Map<String, String>> divisionList = new ArrayList<Map<String, String>>();
			ArrayList<Map<String, String>> emailList = new ArrayList<Map<String, String>>();

			if (partnerMaster.getWatchers()!= null) {
				String[] arr;
				
				for (String watcher : partnerMaster.getWatchers()) {
					Map<String, String> m = new HashMap<String, String>();
					arr = watcher.split("\\/");

					if(!"Email".equals(arr[1])) {
						partnerMaster.setParDivision(arr[0]);
						if(arr.length > 1){
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
					
					if(watcherList.size() == 0){
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
	@CacheEvict(value="autocompletePartnerCache", allEntries=true)
	public void deletePartnerMaster(PartnerMaster partnerMaster) {
		//partnerMaster
		partnerMapper.deleteMaster(partnerMaster);
		//partnerWatcher
		partnerMapper.deleteWatcher(partnerMaster);
		//partnerComment
		CommentsHistory param = new CommentsHistory();
		param.setReferenceId(partnerMaster.getPartnerId());
		param.setReferenceDiv("20");
		commentMapper.deleteCommentByReferenceId(param);
		//ossComponents
		List<OssComponents> componentId = partnerMapper.selectComponentId(partnerMaster.getPartnerId());
		
		for(int i = 0; i<componentId.size(); i++){
			partnerMapper.deleteOssComponentsLicense(componentId.get(i));
		}
		
		partnerMapper.deleteOssComponents(partnerMaster.getPartnerId());
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
		changeStatus(partnerMaster);		
	}
	
	@Override
	@CacheEvict(value="autocompletePartnerCache", allEntries=true)
	public void changeStatus(PartnerMaster partnerMaster) {
		CoMail mailBean = null;
		
		if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(partnerMaster.getStatus())) {
			PartnerMaster orgPartnerMaster = partnerMapper.selectPartnerMaster(partnerMaster);
			if(isEmpty(orgPartnerMaster.getReviewer())) {
				partnerMaster.setReviewer(partnerMaster.getLoginUserName());
				mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED);
				mailBean.setToIds(new String[] {partnerMaster.getLoginUserName()});
				mailBean.setParamPartnerId(partnerMaster.getPartnerId());
			}
		}
		
		partnerMapper.changeStatus(partnerMaster);
		
		if(mailBean != null) {
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
		
		if(!CommonFunction.isAdmin()) {
			PartnerMaster param = new PartnerMaster();
			param.setPartnerId(partnerId);
			
			if(partnerMapper.checkWatcherAuth(param) == 0) {
				rtnFlag = CoConstDef.FLAG_YES;
			}
		}
		
		return rtnFlag;
	}

	@Override
	public void addWatcher(PartnerMaster project) {
		if(!isEmpty(project.getParEmail())) {
			// 이미 추가된 watcher 체크
			if(partnerMapper.existsWatcherByEmail(project) == 0) {
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
			if(partnerMapper.existsWatcherByUser(project) == 0) {
				// watcher 추가
				partnerMapper.insertWatcher(project);
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
		
		if(i > 0){
			result = true;
		}	
		
		return result;
	}
	
	@Override
	public Map<String, Object> getPartnerValidationList(PartnerMaster partnerMaster){
		ProjectIdentification prjBean = new ProjectIdentification();
		
		prjBean.setReferenceId(partnerMaster.getPartnerId());
		prjBean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		Map<String, Object> partnerList = projectService.getIdentificationGridList(prjBean, true);
		
		PartnerMaster partnerInfo = new PartnerMaster();
		T2CoProjectValidator pv = new T2CoProjectValidator();
		
		if(prjBean.getPartnerId() == null){
			partnerInfo.setPartnerId(prjBean.getReferenceId());
		}else{
			partnerInfo.setPartnerId(prjBean.getPartnerId());
		}
		
		partnerInfo = getPartnerMasterOne(partnerInfo);

		if (partnerInfo != null) {
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_PARTNER);

			// main grid
			pv.setAppendix("mainList", (List<ProjectIdentification>) partnerList.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) partnerList.get("subData"));
			
			if ((CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(partnerInfo.getStatus())
					|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(partnerInfo.getStatus()))
					&& CommonFunction.isAdmin()) {
				pv.setCheckForAdmin(true);
			}

			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if(!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				partnerList.replace("mainData", CommonFunction.identificationSortByValidInfo(
						(List<ProjectIdentification>) partnerList.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false, true));
				if(!vr.isValid()) {
					partnerList.put("validData", vr.getValidMessageMap());
				}
				if(!vr.isDiff()) {
					partnerList.put("diffData", vr.getDiffMessageMap());
				}
				if(vr.hasInfo()) {
					partnerList.put("infoData", vr.getInfoMessageMap());
				}
			}
		}
		
		return partnerList;
	}
	
	public Map<String, Object> getFilterdList(Map<String, Object> paramMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();

		List<ProjectIdentification> mainData = (List<ProjectIdentification>) paramMap.get("mainData");
		Map<String, String> errorMap = (Map<String, String>) paramMap.get("validData");
		List<String> duplicateList = new ArrayList<>();
		List<String> componentIdList = new ArrayList<>();
		
		if(errorMap != null) {
			for(ProjectIdentification prjBean : mainData) {
				String checkKey = prjBean.getOssName() + "_" + prjBean.getOssVersion();
				// 중복된 oss Info는 제외함.
				if(duplicateList.contains(checkKey)) {
					continue;
				}
				
				String componentId = prjBean.getComponentId();
				String ossNameErrorMsg = errorMap.containsKey("ossName."+componentId) ? errorMap.get("ossName."+componentId) : "";
				String ossVersionErrorMsg = errorMap.containsKey("ossVersion."+componentId) ? errorMap.get("ossVersion."+componentId) : "";
				
				if(ossNameErrorMsg.indexOf("Unconfirmed") > -1 
						|| ossVersionErrorMsg.indexOf("Unconfirmed") > -1) {
					duplicateList.add(checkKey);
					componentIdList.add(componentId);
				}
			}
		}
		
		resultMap.put("componentIdList", componentIdList);
		resultMap.put("isValid", true);
		
		return resultMap;
	}


}
