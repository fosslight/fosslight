/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.CommentMapper;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.SelfCheckMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.VerificationService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;


@Service
@Slf4j
public class SelfCheckServiceImpl extends CoTopComponent implements SelfCheckService {
	//SERVICE
	@Autowired FileService fileService;
	@Autowired OssService ossService;
	@Autowired VerificationService verificationService;
	@Autowired CommentService commentService;	

	//MAPPER
	@Autowired ProjectMapper projectMapper;
	@Autowired T2UserMapper userMapper;
	@Autowired PartnerMapper partnerMapper;
	@Autowired FileMapper fileMapper;
	@Autowired SelfCheckMapper selfCheckMapper;
	@Autowired LicenseMapper licenseMapper;
	@Autowired CommentMapper commentMapper;
	

	private static String NOTICE_PATH = CommonFunction.emptyCheckProperty("selfcheck.notice.path", "/selfcheck/notice");
	private static String EXPORT_TEMPLATE_PATH = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	
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
					// ??????????????????
					for(Project bean : list) {
						// DISTRIBUTION Android Flag
						// ????????? ?????? android project??? ?????? ????????? ?????? android project????????? ?????????. ??? selfcheck????????? ?????? ????????? ?????? ???????????? ????????? ?????? ???????????????.
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
			log.debug(e.getMessage());
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
		List<String> unconfirmedLicenseList = new ArrayList<>();
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
			
			// components license ????????? ????????? ????????????
			for(ProjectIdentification bean : list) {
				param.addComponentIdList(bean.getComponentId());
				
				if(!isEmpty(bean.getOssId())) {
					ossParam.addOssIdList(bean.getOssId());
				}
				
				// oss Name??? ????????????, oss Version??? ???????????? ?????? case?????? ?????? ??????????????? ??????
				if(isEmpty(bean.getCveId()) 
						&& isEmpty(bean.getOssVersion()) 
						&& !isEmpty(bean.getCvssScoreMax())
						&& !("-".equals(bean.getOssName()))){ 
					String[] cvssScoreMax = bean.getCvssScoreMax().split("\\@");
					bean.setCvssScore(cvssScoreMax[0]);
					bean.setCveId(cvssScoreMax[1]);
				}
			}
				
			// oss id??? oss master??? ???????????? ?????? ???????????? ????????? ??????
			Map<String, OssMaster> componentOssInfoMap = null;
			
			if(ossParam.getOssIdList() != null && !ossParam.getOssIdList().isEmpty()) {
				componentOssInfoMap = ossService.getBasicOssInfoListById(ossParam);
			}
				
			List<ProjectIdentification> licenseList = selfCheckMapper.identificationSubGrid(param);
			
			for(ProjectIdentification licenseBean : licenseList) {
				for(ProjectIdentification bean : list) {
					if(licenseBean.getComponentId().equals(bean.getComponentId())) {
						// ???????????? ?????? ????????????
						licenseBean.setEditable(CoConstDef.FLAG_YES);
						bean.addComponentLicenseList(licenseBean);
						
						if(!unconfirmedLicenseList.contains(avoidNull(licenseBean.getLicenseName()))) {
							if(CoConstDef.FLAG_NO.equals(bean.getExcludeYn()) 
									&& isEmpty(licenseBean.getLicenseId())) {
								unconfirmedLicenseList.add(licenseBean.getLicenseName());
							}
						}
						
						break;
					}
				}
			}

			// license ?????? ??????
			for(ProjectIdentification bean : list) {
				if(bean.getComponentLicenseList()!=null){
					String licenseCopy = "";
						
					// multi dual ??????????????? ??????, main row??? ???????????? license ????????? OSS List??? ??????????????? ??????????????? ???????????? ????????????.
					// ossId??? ?????? ????????? ??????????????? subGrid??? ????????? ??? ??????
					// ??????????????? ?????? ????????? ?????????, subgrid ?????? ???????????? ????????? ??????????????? oss ??? ???????????? ?????? ??????????????? ???????????? ??????
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
						}
						
						bean.setLicenseName(CommonFunction.makeLicenseExpressionIdentify(bean.getComponentLicenseList(), ","));
					} else {
						// license text??? ???????????? ?????? ????????? ????????? ????????? ??????
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
					
					// subGrid??? Item ????????? ?????? ????????? map?????? ????????????.
					// ????????? component_id??? key??? ????????????.
					subMap.put(bean.getGridId(), bean.getComponentLicenseList());
				}	
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
		map.put("unconfirmedLicenseList", unconfirmedLicenseList);

		return map;
	}
	
	@Transactional
	@Override
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	public void registProject(Project project) {
		boolean isNew = isEmpty(project.getPrjId());
			
		// admin??? ???????????? creator??? ???????????? ?????????.
		if(!CommonFunction.isAdmin()) {
			project.setCreator(null);
		}
			
		// project master
		selfCheckMapper.insertProjectMaster(project);

		// oss notice
		OssNotice noticeParam = new OssNotice();
		noticeParam.setPrjId(project.getPrjId());
		noticeParam.setNoticeType(avoidNull(project.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));

		if(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(project.getNoticeType())) {
			noticeParam.setNoticeTypeEtc(project.getNoticeTypeEtc());
		}

		selfCheckMapper.makeOssNotice(noticeParam);

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
		// ???????????? ????????? ???????????? ?????????
		ProjectIdentification prj = new ProjectIdentification();
		prj.setReferenceId(project.getPrjId());
		prj.setReferenceDiv(refDiv);
		List<OssComponents> componentId = selfCheckMapper.selectComponentId(prj);
		
		for (int i = 0; i < componentId.size(); i++) {
			selfCheckMapper.deleteOssComponentsLicense(componentId.get(i));
		}
				
		// ????????? ????????? ???????????? ????????? SRC ????????????????????? N?????? N ?????? null
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

		// ???????????? ??????	
		for (int i = 0; i < ossComponent.size(); i++) {
			String downloadLocation = ossComponent.get(i).getDownloadLocation();
			
			if(downloadLocation.endsWith("/")) {
				ossComponent.get(i).setDownloadLocation(downloadLocation.substring(0, downloadLocation.length()-1));
			}
			
			//update
			if(!StringUtil.contains(ossComponent.get(i).getGridId(), CoConstDef.GRID_NEWROW_DEFAULT_PREFIX)){
				//ossComponents ??????
				selfCheckMapper.updateSrcOssList(ossComponent.get(i));
				
				deleteRows.add(ossComponent.get(i).getComponentIdx());
				
				//????????????????????? ??????
				if(CoConstDef.LICENSE_DIV_MULTI.equals(ossComponent.get(i).getLicenseDiv())){
					for (List<ProjectIdentification> comLicenseList : ossComponentLicense) {
						for (ProjectIdentification comLicense : comLicenseList) {
							if(ossComponent.get(i).getComponentId().equals(comLicense.getComponentId())){
								OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, CoConstDef.LICENSE_DIV_MULTI);
								
								// ???????????? ??????
								selfCheckMapper.registComponentLicense(license);
							}
						}
					}
				} else { //???????????????????????????
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossComponent.get(i), CoConstDef.LICENSE_DIV_SINGLE);
					// ???????????? ??????
					selfCheckMapper.registComponentLicense(license);
				}
			} else { // insert
				//ossComponents ??????
				String exComponentId = ossComponent.get(i).getGridId();
				//component_idx key
				String componentIdx = selfCheckMapper.selectComponentIdx(prj);
				ossComponent.get(i).setReferenceId(project.getPrjId());
				ossComponent.get(i).setReferenceDiv(refDiv);
				ossComponent.get(i).setComponentIdx(componentIdx);
				String _componentId = ossComponent.get(i).getReferenceId() + "-" + ossComponent.get(i).getReferenceDiv() + "-" + ossComponent.get(i).getComponentIdx();
				
				OssMaster ossInfo = CoCodeManager.OSS_INFO_UPPER.get((ossComponent.get(i).getOssName() +"_"+ avoidNull(ossComponent.get(i).getOssVersion())).toUpperCase());
				if(ossInfo != null) {
					ossComponent.get(i).setObligationType(ossInfo.getObligationType());
				}
				
				selfCheckMapper.insertSrcOssList(ossComponent.get(i));
				deleteRows.add(componentIdx);
				
				//????????????????????? ??????
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
								// ???????????? ID ??????
								license.setComponentId(_componentId);
								
								selfCheckMapper.registComponentLicense(license);
							}
						}
					}
				} else { //???????????????????????????
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(ossComponent.get(i), CoConstDef.LICENSE_DIV_SINGLE);
					// ???????????? ??????
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
			
			// license id ??????
			selfCheckMapper.updateComponentsLicenseId(_ossidUpdateParam);
		}
		
		//delete
		OssComponents param = new OssComponents();
		param.setReferenceDiv(refDiv);
		param.setReferenceId(project.getPrjId());
		param.setOssComponentsIdList(deleteRows);
		
		selfCheckMapper.deleteOssComponentsWithIds(param);
		
		// delete file
		if(project.getCsvFile() != null && project.getCsvFile().size() > 0) {
			for (int i = 0; i < project.getCsvFile().size(); i++) {
				selfCheckMapper.deleteFileBySeq(project.getCsvFile().get(i));
				fileService.deletePhysicalFile(project.getCsvFile().get(i), "SELF");
			}
		}
		
		// ?????? ??????
		if(!isEmpty(project.getSrcCsvFileId())){
			selfCheckMapper.updateFileId(project);
			
			if(project.getCsvFileSeq() != null) {
				for (int i = 0; i < project.getCsvFileSeq().size(); i++) {
					selfCheckMapper.updateFileBySeq(project.getCsvFileSeq().get(i));
				}				
			}
		}
		
		{
			// vulnerability max score??? ??????
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
			
			Project vnlnUpdBean = new Project();
			
			if(!isEmpty(max_vuln_ossName)) {
				vnlnUpdBean.setOssName(max_vuln_ossName);
				vnlnUpdBean.setOssVersion(avoidNull(max_vuln_ossVersion));
				vnlnUpdBean = selfCheckMapper.getMaxVulnByOssName(vnlnUpdBean);				
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
		// ?????? ????????? nickname ????????? ???????????? 2??? ????????? java ?????? merge ?????? ???????????? ??????
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
			// ?????? ????????? watcher ??????
			if(selfCheckMapper.existsWatcherByEmail(project) == 0) {
				// watcher ??????
				selfCheckMapper.insertWatcher(project);
				
				// email ??????
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
			// ?????? ????????? watcher ??????
			if(selfCheckMapper.existsWatcherByUser(project) == 0) {
				// watcher ??????
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
	public OssNotice setCheckNotice(Project project) {
		OssNotice notice = new OssNotice();
		
		try {
			String prjId = project.getPrjId();
			notice = selectOssNoticeOne(prjId);
			
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
	public OssNotice selectOssNoticeOne(String prjId) {
		Project project = new Project();
		project.setPrjId(prjId);
		
		return selfCheckMapper.selectOssNoticeOne(project);
	}

	@Override
	public List<OssComponents> getVerifyOssList(Project projectMaster) {
		List<OssComponents> componentList = selfCheckMapper.selectVerifyOssList(projectMaster);
		if(componentList != null && !componentList.isEmpty() && componentList.get(0) == null) {
			componentList = new ArrayList<>();
		}
		
		return componentList;
	}

	@Override
	public Map<String, Object> getVerificationOne(Project project) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		Project prj = selfCheckMapper.selectProjectMaster(project);

		String comment = prj != null ? prj.getComment() : null;
		String content = commentMapper.getContent(comment);
		OssNotice ossNotice = selfCheckMapper.selectOssNoticeOne(project);

		map.put("data", prj);
		map.put("commentText", content);
		map.put("notice", ossNotice);

		return map;
	}

	@Override
	public Project getProjectBasicInfo(String prjId) {
		Project param = new Project();
		param.setPrjId(prjId);
		
		return selfCheckMapper.selectProjectMaster2(param);
	}

	@Override
	public boolean getNoticeHtmlFile(OssNotice ossNotice) throws IOException {
		return getNoticeHtmlFile(ossNotice, null);
	}
	
	@Override
	public boolean getNoticeHtmlFile(OssNotice ossNotice, String contents) throws IOException {
		Project prjInfo = getProjectBasicInfo(ossNotice.getPrjId());
		
		// OSS Notice??? N/A?????? ???????????? ???????????? ?????????.
		if(CoConstDef.CD_NOTICE_TYPE_NA.equals(prjInfo.getNoticeType())) {
			return true;
		}
		
		prjInfo.setUseCustomNoticeYn(!isEmpty(contents) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
		contents = avoidNull(contents, getNoticeHtml(ossNotice));
		
		return getNoticeVelocityTemplateFile(contents, prjInfo);
	}

	@Override
	public String getNoticeHtml(OssNotice ossNotice) throws IOException {	
		// Convert Map to Apache Velocity Template
		return CommonFunction.VelocityTemplateToString(getNoticeHtmlInfo(ossNotice));		
	}

	private boolean getNoticeVelocityTemplateFile(String contents, Project project) {
		boolean procResult = true;
		
		try {
			// file path and name ??????
			// ?????? path : <upload_home>/notice/
			// ????????? : ??????: ????????????ID_yyyyMMdd\
			
			String filePath = NOTICE_PATH + "/" + project.getPrjId();
			// ????????? ????????? ????????? ?????? ????????????.
			Path rootPath = Paths.get(filePath);
			if(rootPath.toFile().exists()) {
				for(String _fName : rootPath.toFile().list()) {
					Files.deleteIfExists(rootPath.resolve(_fName));
					
					T2File file = new T2File();
					file.setLogiNm(_fName);
					file.setLogiPath(filePath);
					
					int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(file);
					
					if(returnSuccess > 0){
						log.debug(filePath + "/" + _fName + " is delete success.");
					}else{
						log.debug(filePath + "/" + _fName + " is delete failed.");
					}
				}
			}			
			
			String fileName = CommonFunction.getNoticeFileName("Self-Check_"+project.getPrjId(), project.getPrjName(), project.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), "html");
			
			if(oss.fosslight.util.FileUtil.writeFile(filePath, fileName, contents)) {
				// ?????? ??????
				String FileSeq = fileService.registFileWithFileName(filePath, fileName);
				
				// project ?????? ????????????
				Project projectParam = new Project();
				projectParam.setPrjId(project.getPrjId());
				projectParam.setNoticeFileId(FileSeq);
				projectParam.setUseCustomNoticeYn(StringUtil.nvl(project.getUseCustomNoticeYn(),CoConstDef.FLAG_NO));
				
				selfCheckMapper.updateNoticeFileInfo(projectParam);
			} else {
				procResult = false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			procResult = false;
		}
		
		return procResult;
	}

	public Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice) {
		Map<String, Object> model = new HashMap<String, Object>();
		
		String noticeType = "";
		String prjName = "";
		String prjVersion = "";
		String prjId = "";
		String distributeSite = "";
		int dashSeq = 0;
		boolean hideOssVersionFlag = CoConstDef.FLAG_YES.equals(ossNotice.getHideOssVersionYn());
		
		// NETWORK SERVER ????????? ????????????.
		
		Project project = new Project();
		project.setPrjId(ossNotice.getPrjId());
		
		project = selfCheckMapper.getProjectBasicInfo(project);
		
		if(project != null){
			if(isEmpty(prjName)) {
				prjName = project.getPrjName();
			}
			
			if(isEmpty(prjId)) {
				prjId = project.getPrjId();
			}
			
			if(isEmpty(prjVersion)) {
				prjVersion = project.getPrjVersion();
			}
			
			if(isEmpty(distributeSite)) {
				distributeSite = project.getDistributeTarget();
			}
		}
		
		List<OssComponents> ossComponentList = selfCheckMapper.selectVerificationNotice(ossNotice);
		
		// TYPE??? ??????
		Map<String, OssComponents> noticeInfo = new HashMap<>();
		Map<String, OssComponents> srcInfo = new HashMap<>();
		Map<String, OssComponentsLicense> licenseInfo = new HashMap<>();
		Map<String, List<String>> componentCopyright = new HashMap<>();
		Map<String, List<String>> componentAttribution = new HashMap<>();
		
		OssComponents ossComponent;
		String ossInfoUpperKey = "";
		
		for(OssComponents bean : ossComponentList) {
			ossInfoUpperKey = (bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase();
			if(CoCodeManager.OSS_INFO_UPPER.containsKey(ossInfoUpperKey) && isEmpty(bean.getHomepage())) {
				bean.setHomepage(CoCodeManager.OSS_INFO_UPPER.get(ossInfoUpperKey).getHomepage());
			}
			
			String componentKey = (hideOssVersionFlag
									? bean.getOssName() 
									: bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
			
			if("-".equals(bean.getOssName())) {
				componentKey += dashSeq++;
			}
			
			// type
			boolean isDisclosure = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType());
			// 2017.05.16 add by yuns start
			// obligation??? ????????? ??? ?????? oss??? bom??? merge ????????? ???????????????, identification confirm??? refDiv??? '50'(????????????)??? obligation??? ????????? ??? ?????? oss??? ???????????? ????????????
			// confirm ???????????? obligation??? ??????????????? ????????? ???????????? ??????????????? ?????? ????????? '50'?????? copy????????? ??????????????????, ????????? ????????? ????????????
			boolean isNotice = CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType());
			
			if(!isDisclosure && !isNotice) {
				continue;
			}
			
			// 2017.07.05
			// Accompanied with source code ??? ??????
			// ????????????????????? ???????????? ?????? ??????????????? ????????? oss table??? ??????
			if(CoConstDef.CD_DTL_NOTICE_TYPE_ACCOMPANIED.equals(ossNotice.getNoticeType())) {
				isDisclosure = true;
			}
			
			// 2017.05.16 add by yuns end
			boolean addDisclosure = isDisclosure && srcInfo.containsKey(componentKey);
			boolean addNotice = !isDisclosure && noticeInfo.containsKey(componentKey);
			
			if(addDisclosure) {
				ossComponent = srcInfo.get(componentKey);
			} else if(addNotice) {
				ossComponent = noticeInfo.get(componentKey);
			} else {
				ossComponent = bean;
			}
			
			if(hideOssVersionFlag) {
				List<String> copyrightList = componentCopyright.containsKey(componentKey) 
						? (List<String>) componentCopyright.get(componentKey) 
						: new ArrayList<>();
						
				List<String> attributionList = componentAttribution.containsKey(componentKey) 
						? (List<String>) componentAttribution.get(componentKey) 
						: new ArrayList<>();
						
				if(!isEmpty(bean.getCopyrightText())) {
					for(String copyright : bean.getCopyrightText().split("\n")) {
						copyrightList.add(copyright);
					}
				}
				
				if(!isEmpty(bean.getOssAttribution())) {
					attributionList.add(bean.getOssAttribution());
				}

				// ???????????? ?????? ??????
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());

				if(!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
					ossComponent.addOssComponentsLicense(license);
				}
				

				String ossCopyright = findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright());
				
				// multi license ?????? copyright
				if(!isEmpty(ossCopyright)) {
					for(String copyright : ossCopyright.split("\n")) {
						copyrightList.add(copyright);
					}
				}
				
				
				// ????????????
				copyrightList = copyrightList.stream()
												.filter(CommonFunction.distinctByKey(c -> avoidNull(c).trim().toUpperCase()))
												.collect(Collectors.toList()); 
				ossComponent.setCopyrightText(String.join("\r\n", copyrightList));
				componentCopyright.put(componentKey, copyrightList);
				
				attributionList = attributionList.stream()
													.filter(CommonFunction.distinctByKey(a -> avoidNull(a).trim().toUpperCase()))
													.collect(Collectors.toList()); 
				ossComponent.setOssAttribution(String.join("\r\n", attributionList));
				componentAttribution.put(componentKey, attributionList);
				
				if(isDisclosure) {
					if(addDisclosure) {
						srcInfo.replace(componentKey, ossComponent);
					} else {
						srcInfo.put(componentKey, ossComponent);
					}
				} else {
					if(addNotice) {
						noticeInfo.replace(componentKey, ossComponent);
					} else {
						noticeInfo.put(componentKey, ossComponent);
					}
				}
				
				if(!licenseInfo.containsKey(license.getLicenseName())) {
					licenseInfo.put(license.getLicenseName(), license);
				}
			} else {
				
				// ???????????? ?????? ??????
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());
				
				// ????????? oss??? ????????? ????????? LICENSE??? ?????? ???????????? ?????? 
				// ?????? ????????? ?????????. (????????? ????????? ??????, DATA??? ??????????????? ????????????)
				if(!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
					ossComponent.addOssComponentsLicense(license);
					
					// OSS??? Copyright text??? ????????????????????? Packaging > Notice Preview??? ???????????? ??? ???.
					// MULTI LICENSE??? ????????? oss??? ????????? ????????? copyright??? ??????, Identification Confirm?????? DB??? ??????????????? ????????? ???????????? ???????????? ?????????, preview ???????????? ????????? ???????????? ??????????????? ???????????? ?????????
					// verification??????????????? oss_component_license??? oss_license??? license?????? ????????? ????????? ?????? ?????? ????????? (exclude??? license??? ???????????? ??????)
					// ????????? oss id??? license id??? ???????????? ?????????.
					// ????????? ??????????????? or ???????????? ????????? ????????? ?????? ????????? ??? ??? ?????????, ????????? oss??? ????????? license??? ?????? ?????? copyright??? ??????????????? ???????????? ????????? (??????????????? ???????????? ????????? ????????? ?????? ??????????????? ????????????)
					bean.setOssCopyright(findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright()));
					
					// multi license ?????? copyright
					if(!isEmpty(bean.getOssCopyright())) {
						String addCopyright = avoidNull(ossComponent.getCopyrightText());
						
						if(!isEmpty(ossComponent.getCopyrightText())) {
							addCopyright += "\r\n";
						}
						 
						addCopyright += bean.getOssCopyright();
						ossComponent.setCopyrightText(addCopyright);
					}
				}
				
				if(isDisclosure) {
					if(addDisclosure) {
						srcInfo.replace(componentKey, ossComponent);
					} else {
						srcInfo.put(componentKey, ossComponent);
					}
				} else {
					if(addNotice) {
						noticeInfo.replace(componentKey, ossComponent);
					} else {
						noticeInfo.put(componentKey, ossComponent);
					}
				}
				
				if(!licenseInfo.containsKey(license.getLicenseName())) {
					licenseInfo.put(license.getLicenseName(), license);
				}
			}
		}
		
		// copyleft??? ????????? ?????? notice????????? ???????????? ?????? copyleft??? merge???.
		Set<String> noticeKeyList = noticeInfo.keySet();
		if(hideOssVersionFlag) {
			for(String key : noticeKeyList) {
				if(srcInfo.containsKey(key)) {
					noticeInfo.remove(key);
				}
			}
		}
		
		// CLASS ????????? ????????? ?????? ???????????? ????????? ????????????.
		// OSS NAME??? ????????? ('-') ?????? ????????? ?????? (??????????????? ??????????????? ??????)
		List<OssComponents> addOssComponentList = selfCheckMapper.selectVerificationNoticeClassAppend(ossNotice);
		
		if(addOssComponentList != null) {
			for(OssComponents bean : addOssComponentList) {
				if("-".equals(bean.getOssName()) || !CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(bean.getOssName().toUpperCase())
						|| !CoCodeManager.OSS_INFO_UPPER.containsKey((bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase())) {
					if(!isEmpty(bean.getDownloadLocation()) && isEmpty(bean.getHomepage())) {
						bean.setHomepage(bean.getDownloadLocation());
					}
				}
				
				String componentKey = (hideOssVersionFlag
											? bean.getOssName() 
											: bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
				
				if("-".equals(bean.getOssName())) {
					componentKey += dashSeq++;
				}
				
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());
				bean.addOssComponentsLicense(license);
				
				if(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())
						|| CoConstDef.CD_DTL_NOTICE_TYPE_ACCOMPANIED.equals(ossNotice.getNoticeType())
						|| hideOssVersionFlag) { // Accompanied with source code ??? ?????? source ?????? ??????
					srcInfo.put(componentKey, bean);
				} else {
					noticeInfo.put(componentKey, bean);
				}
				
				if(!licenseInfo.containsKey(license.getLicenseName())) {
					licenseInfo.put(componentKey, license);
				}
			}
		}
		
		boolean isTextNotice = "text".equals(ossNotice.getFileType());
		
		Map<String, String> ossAttributionMap = new HashMap<>();
		// ???????????? ??? velocity??? list ??????
		List<OssComponents> noticeList = new ArrayList<>();
		
		for(OssComponents bean : noticeInfo.values()) {
			if(isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getOssAttribution()))));
			}

			if(!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if(!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			noticeList.add(bean);
		}
		List<OssComponents> srcList = new ArrayList<>();
		
		for(OssComponents bean : srcInfo.values()) {
			if(isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getOssAttribution()))));
			}
			

			if(!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if(!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			srcList.add(bean);
		}
		
		List<OssComponentsLicense> licenseList = new ArrayList<>();
		List<OssComponentsLicense> licenseListUrls = new ArrayList<>(); //simple version???
		List<OssComponentsLicense> attributionList = new ArrayList<>();
		List<String> ossAttributionList = new ArrayList<>();
		
		// ??????
		TreeMap<String, OssComponentsLicense> licenseTreeMap = new TreeMap<>( licenseInfo );
		
		for(OssComponentsLicense bean : licenseTreeMap.values()) {
			if(isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getLicenseText()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getLicenseText()))));
			}
			
			// ??????????????? license text url
			licenseList.add(bean);
			
			if(CoConstDef.FLAG_YES.equals(ossNotice.getSimpleNoticeFlag())) {
				LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_BY_ID.get(bean.getLicenseId());
				
				if(licenseBean != null) {
//					String simpleLicenseFileName = !isEmpty(licenseBean.getShortIdentifier()) ? licenseBean.getShortIdentifier() : licenseBean.getLicenseNameTemp();
//					String distributeUrl = CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTE_CODE, CoConstDef.CD_DTL_DISTRIBUTE_LGE);
//					simpleLicenseFileName = simpleLicenseFileName.replaceAll(" ", "_").replaceAll("/", "_") + ".html";
//					distributeUrl += "/license/" + simpleLicenseFileName;
					boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
					licenseBean.setDomain(ossNotice.getDomain());
					
					bean.setWebpage(CommonFunction.makeLicenseInternalUrl(licenseBean, distributionFlag));
					licenseListUrls.add(bean);
				}
			}

			if(!isEmpty(bean.getAttribution())) {
				bean.setAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getAttribution()))));
				attributionList.add(bean);
			}
		}

		TreeMap<String, String> ossAttributionTreeMap = new TreeMap<>( ossAttributionMap );
		ossAttributionList.addAll(ossAttributionTreeMap.values());
		
		// ?????? ????????? ????????? ?????? ?????? ????????? ?????????
		String noticeInfoCode = CoConstDef.CD_DTL_DISTRIBUTE_SKS.equals(avoidNull(distributeSite, CoConstDef.CD_DTL_DISTRIBUTE_LGE)) ? CoConstDef.CD_NOTICE_DEFAULT_SKS : CoConstDef.CD_NOTICE_DEFAULT;

		noticeType = avoidNull(ossNotice.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL);
		
		String companyNameFull = ossNotice.getCompanyNameFull();
		String distributionSiteUrl = ossNotice.getDistributionSiteUrl();
		String email = ossNotice.getEmail();
		String appendedContentsTEXT = ossNotice.getAppendedTEXT();
		String appendedContents = ossNotice.getAppended();
		
		if(!isEmpty(distributionSiteUrl) && !(distributionSiteUrl.startsWith("http://") || distributionSiteUrl.startsWith("https://") || distributionSiteUrl.startsWith("ftp://"))) {
			distributionSiteUrl = "http://" + distributionSiteUrl;
		}
		model.put("noticeType", noticeType);
		model.put("noticeTitle", CommonFunction.getNoticeFileName("Self-Check_"+prjId, prjName, prjVersion, CommonFunction.getCurrentDateTime("yyMMdd"), ossNotice.getFileType()));
		model.put("companyNameFull", companyNameFull);
		model.put("distributionSiteUrl", distributionSiteUrl);
		model.put("email", email);
		model.put("noticeObligationSize", noticeList.size());
		model.put("disclosureObligationSize", srcList.size());
		model.put("noticeObligationList", noticeList);
		model.put("disclosureObligationList", srcList);
		/* ui ?????????????????? ?????? ????????? flag */
		model.put("editNoticeYn", ossNotice.getEditNoticeYn());
		model.put("editCompanyYn", ossNotice.getEditCompanyYn());
		model.put("editDistributionSiteUrlYn", ossNotice.getEditDistributionSiteUrlYn());
		model.put("editEmailYn", ossNotice.getEditEmailYn());
		model.put("hideOssVersionYn", ossNotice.getHideOssVersionYn());
		model.put("editAppendedYn", ossNotice.getEditAppendedYn());
		
		/*//ui ?????????????????? ?????? ????????? flag */
		if(CoConstDef.FLAG_YES.equals(ossNotice.getSimpleNoticeFlag())) {
			model.put("licenseListUrls", licenseListUrls);
		} else {
			model.put("licenseList", licenseList);
		}
		
		model.put("attributionList", attributionList.isEmpty() ? null : attributionList);
		model.put("ossAttributionList", ossAttributionList.isEmpty() ? null : ossAttributionList);
		
		if("text".equals(ossNotice.getFileType())){
			model.put("appended", avoidNull(appendedContentsTEXT, "").replaceAll("&nbsp;", " "));
		} else {
			model.put("appended", appendedContents);
		}

		if("text".equals(ossNotice.getFileType())){
			model.put("templateURL", CoCodeManager.getCodeExpString(noticeInfoCode, CoConstDef.CD_DTL_SELFCHECK_NOTICE_TEXT_TEMPLATE));
		} else {
			model.put("templateURL", CoCodeManager.getCodeExpString(noticeInfoCode, CoConstDef.CD_DTL_SELFCHECK_NOTICE_DEFAULT_TEMPLATE));
		}

		model.put("addOssComponentList", addOssComponentList);
		model.put("isSimpleNotice", avoidNull(ossNotice.getIsSimpleNotice(), CoConstDef.FLAG_NO));
		
		return model;
	}

	private boolean checkLicenseDuplicated(List<OssComponentsLicense> ossComponentsLicense,
			OssComponentsLicense license) {
		if(ossComponentsLicense != null) {
			for(OssComponentsLicense bean : ossComponentsLicense) {
				if(bean.getLicenseId().equals(license.getLicenseId())) {
					return true;
				}
			}
		}
		
		return false;
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

		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXJsonYn())) {
			bitFlag |= CoConstDef.FLAG_H;
		}

		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXYamlYn())) {
			bitFlag |= CoConstDef.FLAG_I;
		}
		
		return bitFlag;
	}

	private String findAddedOssCopyright(String ossId, String licenseId, String ossCopyright) {
		if(!isEmpty(ossId) && !isEmpty(licenseId)) {
			OssMaster bean = CoCodeManager.OSS_INFO_BY_ID.get(ossId);
			if (bean != null) {
				for(OssLicense license : bean.getOssLicenses()) {
					if(licenseId.equals(license.getLicenseId()) && !isEmpty(license.getOssCopyright())) {
						return license.getOssCopyright();
					}
				}
			}
		}
		
		return ossCopyright;
	}

	@Override
	public String getNoticeHtmlFileForPreview(OssNotice ossNotice) throws IOException {
		Project prjInfo = getProjectBasicInfo(ossNotice.getPrjId());
		
		return getNoticeVelocityTemplateFileForPreview(getNoticeHtml(ossNotice), prjInfo, ossNotice.getFileType(), ossNotice.getSimpleNoticeFlag());	
	}
	
	private String getNoticeVelocityTemplateFileForPreview(String contents, Project project, String fileType, String simpleFlag) throws IOException {
		return getNoticeVelocityTemplateFileForPreview(contents, project, fileType, simpleFlag, false);
	}
	
	private String getNoticeVelocityTemplateFileForPreview(String contents, Project project, String fileType, String simpleFlag, boolean isConfirm) throws IOException {
		String fileId = "";
		String filePath = "";
		String fileName = "";
		// Text ?????? OSS ????????? ?????? ??? ???????????? ??????
		// System.getProperty("line.separator") => "\n" => "\r\n" ?????? 
		String line = "\r\n";
		if(fileType == "text"){
			fileId = "";
			filePath = NOTICE_PATH + ( isConfirm ? "/" : "/preview/") + project.getPrjId();
			fileName = (CoConstDef.FLAG_YES.equals(simpleFlag) ? "simple_" : "") + CommonFunction.getNoticeFileName("Self-Check_"+project.getPrjId(), project.getPrjName(), project.getPrjVersion(), ( isConfirm ? CommonFunction.getCurrentDateTime("yyMMdd") : DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN) ), fileType);
			contents = contents.replace("\n", line).replace(",)", ")").replace("<br>", line).replace("&copy;", "??").replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&#39;", "\'");
		} else {
			fileId = "";
			filePath = NOTICE_PATH + ( isConfirm ? "/" : "/preview/") + project.getPrjId();
			fileName = (CoConstDef.FLAG_YES.equals(simpleFlag) ? "simple_" : "") + CommonFunction.getNoticeFileName("Self-Check_"+project.getPrjId(), project.getPrjName(), project.getPrjVersion(), ( isConfirm ? CommonFunction.getCurrentDateTime("yyMMdd") : DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN) ), fileType);
			
			// custom edit??? ????????????, packaging confirm ??? ?????? ????????? simple??? ??????
			// license text ????????? ?????? ????????????.
			if(isConfirm && CoConstDef.FLAG_YES.equals(simpleFlag) && CoConstDef.FLAG_YES.equals(project.getUseCustomNoticeYn())) {
				// ?????? ????????? ???????????? ????????? ????????? ????????????.
				T2File defaultNoticeFileInfo = fileService.selectFileInfo(project.getNoticeFileId());
				
				if(defaultNoticeFileInfo != null) {
					File noticeFile = new File(defaultNoticeFileInfo.getLogiPath() + "/" + defaultNoticeFileInfo.getLogiNm());
					if(noticeFile.exists()) {
						Document doc = Jsoup.parse(noticeFile, "UTF8");
						Document doc2 = Jsoup.parse(contents);
						
						doc.select("body > p.bdTop.license").remove();
						doc.select("body").append(doc2.select("body > p.bdTop.license").toString());
						doc.select("body").append(doc2.select("body > ul.bdTop2.license").toString());
						
						contents = doc.toString();
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
	
	@Override
	public String getNoticeTextFileForPreview(OssNotice ossNotice) throws IOException {
		Project prjInfo = getProjectBasicInfo(ossNotice.getPrjId());
		
		return getNoticeVelocityTemplateFileForPreview(getNoticeHtml(ossNotice), prjInfo, ossNotice.getFileType(), ossNotice.getSimpleNoticeFlag());	
	}
	
	@Override
	public String getNoticeTextFileForPreview(OssNotice ossNotice, boolean isConfirm) throws IOException {
		Project prjInfo = getProjectBasicInfo(ossNotice.getPrjId());
		
		return getNoticeVelocityTemplateFileForPreview(getNoticeHtml(ossNotice), prjInfo, ossNotice.getFileType(), ossNotice.getSimpleNoticeFlag(), isConfirm);	
	}
	
	@Override
	@Transactional
	public void registOssNotice(OssNotice ossNotice) throws Exception {
		if(CoConstDef.FLAG_YES.equals(ossNotice.getEditNoticeYn())){
			selfCheckMapper.insertOssNotice(ossNotice);
		} else {
			selfCheckMapper.updateOssNotice(ossNotice);
		}
	}
	
	@Override
	public List<OssComponents> setMergeGridData(List<OssComponents> gridData) {
		List<OssComponents> tempData = new ArrayList<OssComponents>();
		List<OssComponents> resultGridData = new ArrayList<OssComponents>();
		
		String groupColumn = "";

		final Comparator<OssComponents> comp = Comparator.comparing((OssComponents o) -> o.getOssName()+"|"+o.getOssVersion());
		gridData = gridData.stream().sorted(comp).collect(Collectors.toList());
		
		for(OssComponents info : gridData) {
			if(isEmpty(groupColumn)) {
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
			}
						
			if(groupColumn.equals(info.getOssName() + "-" + info.getOssVersion()) // ?????? groupColumn?????? ???????????? ??????
					&& !("-".equals(info.getOssName()) 
					&& !"NA".equals(info.getLicenseType()))) { // ???, OSS Name: - ?????????, License Type: Proprietary??? ?????? ?????? Row??? ????????? ??????.
				tempData.add(info);
			} else { // ?????? grouping
				setMergeData(tempData, resultGridData);
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
				tempData.clear();
				
				tempData.add(info);
			}
		}
		
		setMergeData(tempData, resultGridData);
		
		return resultGridData;
	}	
	
	public static void setMergeData(List<OssComponents> tempData, List<OssComponents> resultGridData){
		if(tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<OssComponents>() {
				@Override
				public int compare(OssComponents o1, OssComponents o2) {
					if(o1.getLicenseName().length() >= o2.getLicenseName().length()) {
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			OssComponents rtnBean = null;
			
			for(OssComponents temp : tempData) {
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
			}
			
			resultGridData.add(rtnBean);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public History work(Object param) {
		// ?????????
		return new History();
	}
}
