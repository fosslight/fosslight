/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.BinaryMaster;
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
import oss.fosslight.domain.UploadFile;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.SelfCheckMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.FileService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.VerificationService;
import oss.fosslight.util.StringUtil;

@Service
@Slf4j
public class SelfCheckServiceImpl extends CoTopComponent implements SelfCheckService {
	//SERVICE
	@Autowired FileService fileService;
	@Autowired OssService ossService;
	@Autowired VerificationService verificationService;
	
	//MAPPER
	@Autowired ProjectMapper projectMapper;
	@Autowired T2UserMapper userMapper;
	@Autowired PartnerMapper partnerMapper;
	@Autowired FileMapper fileMapper;
	@Autowired SelfCheckMapper selfCheckMapper;
	@Autowired LicenseMapper licenseMapper;
	
	@Override
	public Map<String, Object> getProjectList(Project project) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Project> list = null;

		try {
			int records = selfCheckMapper.selectProjectTotalCount(project);
			project.setTotListSize(records);

			String ossId = project.getOssId();

			if (!StringUtil.isEmpty(ossId)) {
				list = selfCheckMapper.selectUnlimitedOssComponentBomList(project);
			} else {
				list = selfCheckMapper.selectProjectList(project);
				
				if(list != null) {
					// 코드변환처리
					for(Project bean : list) {
						// DISTRIBUTION Android Flag
						// 이슈로 인해 android project가 좀더 세분화 되어 android project기준이 변경됨. 단 selfcheck에서는 해당 기준이 필요 없음으로 판단이 되어 주석처리함.
						// DISTRIBUTION_TYPE
						bean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, bean.getDistributionType()));
						// Project Status
						bean.setStatus( CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_STATUS, bean.getStatus()));
						// Identification Status
						bean.setIdentificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getIdentificationStatus()));
						// Verification Status
						bean.setVerificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getVerificationStatus()));
						// Distribute Status
						bean.setDestributionStatus(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_STATUS, bean.getDestributionStatus()));
						// DIVISION
						bean.setDivision(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, bean.getDivision()));
						
						//OS_TYPE
						if("999".equals(bean.getOsType())){
							bean.setOsType(bean.getOsTypeEtc());
						}else{
							bean.setOsType(CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, bean.getOsType()));
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
		project = selfCheckMapper.selectProjectMaster(project);
		
		project.setDestributionName(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, project.getDistributionType()));
		//OS_TYPE
		if(!"999".equals(project.getOsType())){
			project.setOsTypeEtc(CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, project.getOsType()));
		}

		// watcher
		List<Project> watcherList = selfCheckMapper.selectWatchersList(project);
		project.setWatcherList(watcherList);

		// file
		project.setCsvFile(selfCheckMapper.selectCsvFile(project));
		
		return project;
	}
	
	@Override
	public Map<String, Object> getIdentificationGridList(ProjectIdentification identification) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<ProjectIdentification> list = null;
		identification.setRoleOutLicense(CoCodeManager.CD_ROLE_OUT_LICENSE);
		
		boolean isLoadFromProject = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefPrjId());
		
		if(isLoadFromProject) {
			identification.setReferenceId(identification.getRefPrjId());
		}
		
		boolean isApplyFromBat = isEmpty(identification.getReferenceId()) && !isEmpty(identification.getRefBatId());
		
		if(isApplyFromBat) {
			identification.setReferenceId(identification.getRefBatId());
		}
			
		HashMap<String, Object> subMap = new HashMap<String, Object>();
			
		list = selfCheckMapper.selectIdentificationGridList(identification);
		if(list != null && !list.isEmpty()) {
			
			ProjectIdentification param = new ProjectIdentification();
			param.setReferenceDiv(identification.getReferenceDiv());
			param.setReferenceId(identification.getReferenceId());
			OssMaster ossParam = new OssMaster();
			
			// components license 정보를 한번에 가져온다
			for(ProjectIdentification bean : list) {
				param.addComponentIdList(bean.getComponentId());
				
				if(!isEmpty(bean.getOssId())) {
					ossParam.addOssIdList(bean.getOssId());
				}
				
				// oss Name은 작성하고, oss Version은 작성하지 않은 case경우 해당 분기문에서 처리
				if(isEmpty(bean.getCveId()) 
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
			
			if(ossParam.getOssIdList() != null && !ossParam.getOssIdList().isEmpty()) {
				componentOssInfoMap = ossService.getBasicOssInfoListById(ossParam);
			}
				
			List<ProjectIdentification> licenseList = selfCheckMapper.identificationSubGrid(param);
			
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
				if(bean.getComponentLicenseList()!=null){
					String licenseCopy = "";
						
					// multi dual 라이선스의 경우, main row에 표시되는 license 정보는 OSS List에 등록되어진 라이선스를 기준으로 표시한다.
					// ossId가 없는 경우는 기본적으로 subGrid로 등록될 수 없다
					// 이짓거리를 하는 두번째 이유는, subgrid 에서 사용자가 추가한 라이선스와 oss 에 등록되어 있는 라이선스를 구분하기 위함
					if(componentOssInfoMap == null) {
						componentOssInfoMap = new HashMap<>();
					}
					
					OssMaster ossBean = componentOssInfoMap.get(bean.getOssId());
						
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
						}
						
						bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
					} else {
						// license text는 표시하지 않기 때문에 설정할 필요는 없음
						for(ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
							if(!isEmpty(licenseBean.getCopyrightText())) {
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

		return map;
	}
	
	@Transactional
	@Override
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void registProject(Project project) {
		boolean isNew = isEmpty(project.getPrjId());
			
		// admin이 아니라면 creator를 변경하지 않는다.
		if(!CommonFunction.isAdmin()) {
			project.setCreator(null);
		}
			
		// project master
		selfCheckMapper.insertProjectMaster(project);

		// project watcher insert
		ArrayList<Map<String, String>> divisionList = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, String>> emailList = new ArrayList<Map<String, String>>();

		if(isNew) {
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
					}else{
						project.setPrjDivision("");
						project.setPrjUserId("");
						project.setPrjEmail(arr[0]);
						
						m.put("email", project.getPrjEmail());
						
						emailList.add(m);
					}
					
					List<Project> watcherList = selfCheckMapper.selectWatchersCheck(project);
					
					if(watcherList.size() == 0){
						selfCheckMapper.insertProjectWatcher(project);						
					}
				}
			}
			
			project.setDivisionList(divisionList);
			project.setEmailList(emailList);
			
			selfCheckMapper.deleteProjectWatcher(project);
		}
	}
	
	@Transactional
	@Override
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void deleteProject(Project project) {
		// project master
		selfCheckMapper.deleteProjectMaster(project);
	}
	
	@Override
	@Transactional
	public void registSrcOss(List<ProjectIdentification> ossComponent, List<List<ProjectIdentification>> ossComponentLicense, Project project) {
		registSrcOss(ossComponent, ossComponentLicense, project, CoConstDef.CD_DTL_SELF_COMPONENT_ID);
	}
	
	@Override
	@Transactional
	public void registSrcOss(List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense, Project project, String refDiv) {
		// 컴포넌트 마스터 라이센스 지우기
		ProjectIdentification prj = new ProjectIdentification();
		prj.setReferenceId(project.getPrjId());
		prj.setReferenceDiv(refDiv);
		List<OssComponents> componentId = selfCheckMapper.selectComponentId(prj);
		
		for (int i = 0; i < componentId.size(); i++) {
			selfCheckMapper.deleteOssComponentsLicense(componentId.get(i));
		}
				
		// 한건도 없을시 프로젝트 마스터 SRC 사용가능여부가 N이면 N 그외 null
		if(ossComponent.size()==0){
			Project projectSubStatus = new Project();
			projectSubStatus.setPrjId(project.getPrjId());
			projectSubStatus.setModifier(projectSubStatus.getLoginUserName());
			projectSubStatus.setReferenceDiv(refDiv);
			
			selfCheckMapper.updateProjectMaster(projectSubStatus);
		}
		
		ossComponent = convertOssNickName(ossComponent);
		ossComponentLicense = convertLicenseNickName(ossComponentLicense);
		
		//deleteRows
		List<String> deleteRows = new ArrayList<String>();

		// 컴포넌트 등록	
		for (int i = 0; i < ossComponent.size(); i++) {
			String downloadLocation = ossComponent.get(i).getDownloadLocation();
			
			if(downloadLocation.endsWith("/")) {
				ossComponent.get(i).setDownloadLocation(downloadLocation.substring(0, downloadLocation.length()-1));
			}
			
			//update
			if(!StringUtil.contains(ossComponent.get(i).getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
				//ossComponents 등록
				selfCheckMapper.updateSrcOssList(ossComponent.get(i));
				
				deleteRows.add(ossComponent.get(i).getComponentIdx());
				
				//멀티라이센스일 경우
				if(CoConstDef.LICENSE_DIV_MULTI.equals(ossComponent.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(ossComponent.get(i).getComponentId().equals(comLicense.getComponentId())){
								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
								
								// 라이센스 등록
								selfCheckMapper.registComponentLicense(license);
							}
						}
					}
				} else { //싱글라이센스일경우
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossComponent.get(i), CoConstDef.LICENSE_DIV_SINGLE);
					// 라이센스 등록
					selfCheckMapper.registComponentLicense(license);
				}
			} else { // insert
				//ossComponents 등록
				String exComponentId = ossComponent.get(i).getGridId();
				//component_idx key
				String componentIdx = selfCheckMapper.selectComponentIdx(prj);
				ossComponent.get(i).setReferenceId(project.getPrjId());
				ossComponent.get(i).setReferenceDiv(refDiv);
				ossComponent.get(i).setComponentIdx(componentIdx);
				String _componentId = ossComponent.get(i).getReferenceId() + "-" + ossComponent.get(i).getReferenceDiv() + "-" + ossComponent.get(i).getComponentIdx();
				
				selfCheckMapper.insertSrcOssList(ossComponent.get(i));
				deleteRows.add(componentIdx);
				
				//멀티라이센스일 경우
				if(CoConstDef.LICENSE_DIV_MULTI.equals(ossComponent.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							// null point
							if(isEmpty(comLicense.getGridId())) {
								continue;
							}
							
							String gridId = comLicense.getGridId().split("-")[0];
							
							if(exComponentId.equals(comLicense.getComponentId())
									|| exComponentId.equals(gridId)){
								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
								// 컴포넌트 ID 설정
								license.setComponentId(_componentId);
								
								selfCheckMapper.registComponentLicense(license);
							}
						}
					}
				} else { //싱글라이센스일경우
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossComponent.get(i), CoConstDef.LICENSE_DIV_SINGLE);
					// 라이센스 등록
					license.setComponentId(_componentId);
					
					selfCheckMapper.registComponentLicense(license);
				}
			}
		}
		
		{
			Project _ossidUpdateParam = new Project();
			_ossidUpdateParam.setPrjId(project.getPrjId());
			_ossidUpdateParam.setReferenceDiv(refDiv);
			selfCheckMapper.updateComponentsOssId(_ossidUpdateParam);
			
			// license id 등록
			selfCheckMapper.updateComponentsLicenseId(_ossidUpdateParam);
		}
		
		//delete
		OssComponents param = new OssComponents();
		param.setReferenceDiv(refDiv);
		param.setReferenceId(project.getPrjId());
		param.setOssComponentsIdList(deleteRows);
		
		selfCheckMapper.deleteOssComponentsWithIds(param);
		
		// 파일 등록
		if(!isEmpty(project.getSrcCsvFileId()) || !isEmpty(project.getSrcAndroidCsvFileId()) || !isEmpty(project.getSrcAndroidNoticeFileId()) || !isEmpty(project.getBinCsvFileId()) || !isEmpty(project.getBinBinaryFileId())){
			selfCheckMapper.updateFileId(project);
			
			if(project.getCsvFile() != null) {
				for (int i = 0; i < project.getCsvFile().size(); i++) {
					selfCheckMapper.deleteFileBySeq(project.getCsvFile().get(i));
				}		
				
			}
			if(project.getCsvAddFileSeq() != null) {
				for (int i = 0; i < project.getCsvAddFileSeq().size(); i++) {
					selfCheckMapper.updateFileBySeq(project.getCsvAddFileSeq().get(i));
				}				
			}
		}
		
		{
			// vulnerability max score를 저장
			double max_cvss_score = 0;
			String max_vuln_ossName = null;
			String max_vuln_ossVersion = null;
			List<ProjectIdentification> _ossList = selfCheckMapper.selectIdentificationGridList(prj);
			
			if(_ossList != null) {
				for(ProjectIdentification targetBean : _ossList) {
					if(targetBean != null && !CoConstDef.FLAG_YES.equals(targetBean.getExcludeYn()) && !isEmpty(targetBean.getCvssScore())) {
						double _currentSccore = Double.parseDouble(targetBean.getCvssScore());
						
						if(Double.compare(_currentSccore, max_cvss_score) > 0) {
							max_cvss_score = _currentSccore;
							max_vuln_ossName = targetBean.getOssName();
							max_vuln_ossVersion = targetBean.getOssVersion();
						}
					}
				}
			}
			
			Project vnlnUpdBean = null;
			
			if(!isEmpty(max_vuln_ossName)) {
				vnlnUpdBean = new Project();
				vnlnUpdBean.setOssName(max_vuln_ossName);
				vnlnUpdBean.setOssVersion(avoidNull(max_vuln_ossVersion));
				vnlnUpdBean = selfCheckMapper.getMaxVulnByOssName(vnlnUpdBean);				
			}

			if(vnlnUpdBean == null) {
				vnlnUpdBean = new Project();
			}
			
			vnlnUpdBean.setUpdVuln(CoConstDef.FLAG_YES);
			vnlnUpdBean.setPrjId(project.getPrjId());
			vnlnUpdBean.setModifier(vnlnUpdBean.getLoginUserName());
			
			selfCheckMapper.updateProjectMaster(vnlnUpdBean);
		}
	}
	
	private List<ProjectIdentification> convertOssNickName(List<ProjectIdentification> ossComponentList) {
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
	
	private List<List<ProjectIdentification>> convertLicenseNickName(
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
	public List<Vulnerability> getAllVulnListWithProject(String prjId) {
		// 성능 이슈로 nickname 조건을 분리하여 2번 쿼리후 java 에서 merge 하는 방식으로 변경
		List<Vulnerability> list = new ArrayList<>();
		Map<String, Vulnerability> duplCheck = new HashMap<>();
		Project param = new Project();
		param.setPrjId(prjId);
		
		List<String> vulnList = selfCheckMapper.getAllVulnList(param);
		List<Vulnerability> list1 = selfCheckMapper.getAllVulnListWithProject(param);
			
		if(list1 != null) {
			for(Vulnerability bean : list1) {
				if(vulnList.contains(bean.getProduct())) {
					String key = avoidNull(avoidNull(bean.getVendor()) + "_" + avoidNull(bean.getProduct()) + "_" + avoidNull(bean.getVersion()) + "_" + bean.getCveId());
					
					if(!duplCheck.containsKey(key)) {
						duplCheck.put(key, bean);
					}
				}
			}
		}

		List<Vulnerability> list2 = selfCheckMapper.getAllVulnListWithProjectByNickName(param);
		
		if(list2 != null) {
			for(Vulnerability bean : list2) {
				String key = avoidNull(avoidNull(bean.getVendor()) + "_" + avoidNull(bean.getProduct()) + "_" + avoidNull(bean.getVersion()) + "_" + bean.getCveId());
				if(!duplCheck.containsKey(key)) {
					duplCheck.put(key, bean);
				}
			}
		}
		
		// sort by key
		Map<String, Vulnerability> sortMap = new TreeMap<>(duplCheck);

		list.addAll(sortMap.values());
		
		return list;
	}
	
	@Override
	public void addWatcher(Project project) {
		if(!isEmpty(project.getPrjEmail())) {
			// 이미 추가된 watcher 체크
			if(selfCheckMapper.existsWatcherByEmail(project) == 0) {
				// watcher 추가
				selfCheckMapper.insertWatcher(project);
				
				// email 발송
				try {
					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_SELFCHECK_PROJECT_WATCHER_INVATED);
					mailBean.setParamPrjId(project.getPrjId());
					mailBean.setParamUserId(project.getLoginUserName());
					mailBean.setParamEmail(project.getPrjEmail());
					
					CoMailManager.getInstance().sendMail(mailBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} else {
			// 이미 추가된 watcher 체크
			if(selfCheckMapper.existsWatcherByUser(project) == 0) {
				// watcher 추가
				selfCheckMapper.insertWatcher(project);
			}
		}
	}
	
	@Override
	public void removeWatcher(Project project) {
		selfCheckMapper.removeWatcher(project);		
	}
	
	@Override
	public List<Project> copyWatcher(Project project) {
		return selfCheckMapper.copyWatcher(project);
	}
	
	@Override
	public boolean existsWatcher(Project project) {
		boolean result = false;
		
		int i = selfCheckMapper.existsWatcher(project);
		
		if(i > 0){
			result = true;
		}	
		
		return result;
	}
	
	@Override
	public History work(Object param) {
		return null;
	}

	@Override
	public String getUserList() {
		return null;
	}

	@Override
	public String getCategoryCode(String code, String gubun) {
		return null;
	}

	@Override
	public List<ProjectIdentification> getOssNames(ProjectIdentification identification) {
		return null;
	}

	@Override
	public List<ProjectIdentification> getOssVersions(String ossName) {
		return null;
	}

	@Override
	public Map<String, Object> getOssIdLicenses(ProjectIdentification identification) {
		return null;
	}

	@Override
	public String getDivision(Project project) {
		return null;
	}

	@Override
	public List<Project> getProjectNameList(Project project) {
		return null;
	}

	@Override
	public List<Map<String, Object>> getCategoryCodeToJson(String code) {
		return null;
	}

	@Override
	public boolean existProjectData(Project project) {
		return false;
	}

	@Override
	public Map<String, List<String>> nickNameValid(List<ProjectIdentification> ossComponent,
			List<List<ProjectIdentification>> ossComponentLicense) {
		return null;
	}

	@Override
	public List<Project> getProjectListExcel(Project project) {
		return null;
	}

	@Override
	public Map<String, Object> identificationSubGrid(ProjectIdentification identification) {
		return null;
	}

	@Override
	public Map<String, Object> getPartnerList(PartnerMaster partnerMaster) {
		return null;
	}

	@Override
	public Map<String, Object> getIdentificationProject(Project project) {
		return null;
	}

	@Override
	public Map<String, Object> getPartnerOssList(OssComponents ossComponents) {
		return null;
	}

	@Override
	public Map<String, Object> getIdentificationProjectSearch(ProjectIdentification projectIdentification) {
		return null;
	}

	@Override
	public String getReviewerList() {
		return null;
	}

	@Override
	public Map<String, Object> getIdentificationThird(OssComponents ossComponents) {
		return null;
	}

	@Override
	public List<Project> getProjectCreator() {
		return null;
	}

	@Override
	public List<Project> getProjectReviwer() {
		return null;
	}
	
	@Override
	public Project selectProjectDetailExcel(String parameter) {
		return null;
	}

	@Override
	public List<ProjectIdentification> getProjectReportExcelList(ProjectIdentification identification) {
		return null;
	}

	@Override
	public List<Project> getProjectVersionList(Project project) {
		return null;
	}

	@Override
	public void registPackageFile(List<UploadFile> list, String prjId) {
		
	}

	@Override
	public HashMap<String, Object> applySrcAndroidModel(Project project) {
		return null;
	}

	@Override
	public void updateSubStatus(Project project) {
		
	}

	@Override
	public List<UploadFile> selectAndroidFileDetail(Project project) {
		return null;
	}

	@Override
	public LicenseMaster getLicenseMaster(LicenseMaster license) {
		return null;
	}

	@Override
	public Map<String, Object> getOssIdCheck(ProjectIdentification projectIdentification) {
		return null;
	}

	@Override
	public Map<String, Object> applySrcAndroidModel(List<ProjectIdentification> list, List<String> noticeBinaryList)
			throws IOException {
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getProjectDownloadExpandInfo(Project param) {
		return null;
	}

	@Override
	public Project getProjectBasicInfo(String prjId) {
		return null;
	}

	@Override
	public boolean isPermissiveOnlyAndGeneralNotice(String prjId, boolean isAndroidModel) {
		return false;
	}

	@Override
	public void cancelFileDel(Project project) {}

	@Override
	public List<OssComponents> selectOssComponentsListByComponentIds(OssComponents param) {
		return null;
	}

	@Override
	public Map<String, Object> registBatWithFileUploadByProject(MultipartHttpServletRequest req, T2File file,
			BinaryMaster binary) {
		return null;
	}

	@Override
	public void updateProjectMaster(Project project) {}

	@Override
	public Map<String, Object> getFileInfo(ProjectIdentification identification) {
		return null;
	}

	@Override
	public Map<String, Object> registBatWithFileUploadByProjectByUrl(HttpServletRequest request, T2File file,
			BinaryMaster binary, Map<Object, Object> map) {
		return null;
	}

	@Override
	public Map<String, Object> get3rdMapList(Project project) {
		return null;
	}

	@Override
	public Map<String, Object> getThirdPartyMap(String prjId) {
		return null;
	}

	@Override
	public void updateWithoutVerifyYn(OssNotice ossNotice) {}
}
