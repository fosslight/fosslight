/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
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
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.LicenseService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;


@Service("licenseService")
@Slf4j
public class LicenseServiceImpl extends CoTopComponent implements LicenseService {
	// Service
	@Autowired T2UserService userService;
	@Autowired OssService ossService;
	@Autowired CommentService commentService;
	
	//Mapper
	@Autowired LicenseMapper licenseMapper;
	@Autowired CodeMapper codeMapper;
	
	
	@Override
	public History work(Object param) {
		History h = new History();
		LicenseMaster vo = (LicenseMaster) param;
		LicenseMaster data = getLicenseMasterOne(vo);
		data.setComment(vo.getComment());
		
		h.sethKey(vo.getLicenseId());
		h.sethTitle(vo.getLicenseName());
		h.sethType(CoConstDef.EVENT_CODE_LICENSE);
		h.setModifier(vo.getLoginUserName());
		h.setModifiedDate(vo.getCreatedDate());
		h.sethComment("");
		h.sethData(data);
		
		return h;
	}

	@Override
	public int selectLicenseMasterTotalCount(LicenseMaster licenseMaster) {
		return licenseMapper.selectLicenseMasterTotalCount(licenseMaster);
	}
	
	@Override
	public Map<String,Object> getLicenseMasterList(LicenseMaster licenseMaster) {
		
		// obligation type 검색 조건 추가
		if(!isEmpty(licenseMaster.getObligationType())) {
			switch (licenseMaster.getObligationType()) {
				case CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK:
					licenseMaster.setObligationNeedsCheckYn(CoConstDef.FLAG_YES);
					
					break;
				case CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE:
					licenseMaster.setObligationDisclosingSrcYn(CoConstDef.FLAG_YES);
					
					break;
				case CoConstDef.CD_DTL_OBLIGATION_NOTICE:
					licenseMaster.setObligationNotificationYn(CoConstDef.FLAG_YES);
					
					break;
				case CoConstDef.CD_DTL_OBLIGATION_NONE:
					// "NONE"의 경우는 쿼리 조건절에 직접 등록
					break;
				default:
					break;
			}
		}

		if(licenseMaster.getRestrictions() != null) {
			String restrictions = licenseMaster.getRestrictions();
			
			if(!isEmpty(restrictions)){
				String[] arrRestrictions = restrictions.split(",");
				
				licenseMaster.setArrRestriction(arrRestrictions);
			}
		}
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		int records = licenseMapper.selectLicenseMasterTotalCount(licenseMaster);
		licenseMaster.setTotListSize(records);
		
		List<LicenseMaster> list = licenseMapper.selectLicenseList(licenseMaster);
		
		for(LicenseMaster item : list){
			if(!isEmpty(item.getRestriction())){
				item.setRestriction(CommonFunction.setLicenseRestrictionList(item.getRestriction()));
			}
		}
		
		map.put("page", licenseMaster.getCurPage());
		map.put("total", licenseMaster.getTotBlockSize());
		map.put("records", records);
		map.put("rows", list);
		
		return map; 
	}
	
	@Override
	public LicenseMaster getLicenseMasterOne(LicenseMaster licenseMaster) {
		licenseMaster = licenseMapper.selectLicenseOne(licenseMaster);
		List<LicenseMaster> licenseNicknameList = licenseMapper.selectLicenseNicknameList(licenseMaster);
		List<String> nickNames = new ArrayList<>();
		
		for(LicenseMaster bean : licenseNicknameList) {
			nickNames.add(bean.getLicenseNickname());
		}
		
		// 일반 user 화면 일 경우 restriction을 full name으로 화면 출력
		// admin 화면 일 경우 restriction code를 사용하여 체크박스로 구성
		if(!"ROLE_ADMIN".equals(loginUserRole())) {
			if(licenseMaster.getRestriction() != null) {
				T2CodeDtl t2CodeDtl = new T2CodeDtl();
				List<T2CodeDtl> t2CodeDtlList = new ArrayList<>(); 
				t2CodeDtl.setCdNo(CoConstDef.CD_LICENSE_RESTRICTION);
				try {
					t2CodeDtlList = codeMapper.selectCodeDetailList(t2CodeDtl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				List<String> restrictionList = Arrays.asList(licenseMaster.getRestriction().split(","));
				String restrictionStr = "";
				
				for(T2CodeDtl item: t2CodeDtlList){
					if(restrictionList.contains(item.getCdDtlNo())) {
						restrictionStr += (!isEmpty(restrictionStr) ? ", " : "") + item.getCdDtlNm();
					}
				}
				
				licenseMaster.setRestriction(restrictionStr);
			}
		}
		
		licenseMaster.setLicenseNicknames(nickNames.toArray(new String[nickNames.size()]));
		
		return licenseMaster;
	}
	
	@Override
	public List<OssMaster> checkExistsUsedOss(String licenseId) {
		return licenseMapper.checkExistsUsedOss(licenseId);
	}
	
	@Override
	public LicenseMaster checkExistsLicense(LicenseMaster param) {
		LicenseMaster bean = licenseMapper.checkExistsLicense(param);
		
		if(bean != null && !isEmpty(bean.getLicenseId())) {
			return bean;
		}
		
		return null;
	}
	
	@Override
	public List<LicenseMaster> getLicenseMasterListExcel(LicenseMaster license) {
		List<LicenseMaster> result = licenseMapper.selectLicenseList(license);
		
		if(result != null) {
			for(LicenseMaster bean : result) {
				if(CoCodeManager.LICENSE_INFO_BY_ID.containsKey(bean.getLicenseId())) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO_BY_ID.get(bean.getLicenseId());
					
					bean.setLicenseNicknameList(master.getLicenseNicknameList());
					bean.setRestrictionStr(master.getRestrictionStr());
				}
			}
		}
		
		return result;
	}
	
	@Transactional
	@Override
	@CacheEvict(value="autocompleteCache", allEntries=true)
	public int deleteLicenseMaster(LicenseMaster licenseMaster) {
		licenseMapper.deleteLicenseNickname(licenseMaster);
		
		return licenseMapper.deleteLicenseMaster(licenseMaster);
	}
	
	@Override
	public void deleteDistributeLicense(LicenseMaster bean, boolean distributionFlag) {
		if(bean != null && !isEmpty(bean.getLicenseName())) {
			String fileName = bean.getShortIdentifier();
			String result = null;
			
			if(isEmpty(fileName)) {
				fileName = bean.getLicenseName();
			}
			
			result = "";
			
			log.info("OSDD license update result : " + avoidNull(result));
		}
		
		if(bean.getRestriction().contains(CoConstDef.CD_LICENSE_NETWORK_RESTRICTION)){
			registNetworkServerLicense(bean.getLicenseId(), "DEL");
		}
	}
	
	@Override
	public void registNetworkServerLicense(String licenseId, String type) {
		String CD_DTL_NO = licenseMapper.existNetworkServerLicense(licenseId);
		
		switch(type){
			case "NEW":
				licenseMapper.insertNetworkServerLicense(licenseId);
				
				break;
			case "INS":
				if(isEmpty(CD_DTL_NO)){
					licenseMapper.insertNetworkServerLicense(licenseId);
				}
				
				break;
			case "DEL":
				if(!isEmpty(CD_DTL_NO)){
					licenseMapper.deleteNetworkServerLicense(licenseId);
				}
				
				break;
			default:
				break;
		}
	}
	
	@Override
	public List<OssMaster> updateOssLicenseType(String licenseId) {
		// license type이 변경된 경우 oss의 license type이 함께 변경되어야함
		// 기존 license 를 사용하는 프로젝트 목록을 메일 내용에 포함하던 것을, license type이 변경된 oss를 포함하는 프로젝트 목록을 표시하는 것으로 변경
		List<OssMaster> changedLicenseTypeOssList = new ArrayList<>();
		
		// 해당 license를 사용하는 oss 목록 취득
		List<String> ossList = licenseMapper.getOssListWithLicenseForTypeCheck(licenseId);
		
		// 최종적으로 라이선스 타입이 변경된 oss 목록 산출
		if(ossList != null) {
			for(String ossId : ossList) {
				OssMaster ossBean = ossService.getOssInfo(ossId, false);
				
				String orgOssLicenseType = ossBean.getLicenseType();
				String orgOssObligationType = ossBean.getObligationType();
				
				// license type 변경 여부 체크
				ossService.checkOssLicenseAndObligation(ossBean);
				
				if(!orgOssLicenseType.equals(ossBean.getLicenseType())) {
					// oss의 license type과 obligation을 변경한다.
					ossService.updateLicenseTypeAndObligation(ossBean);
					
					ossBean.setOrgLicenseType(orgOssLicenseType);
					ossBean.setOrgObligationType(orgOssObligationType);
					changedLicenseTypeOssList.add(ossBean);
				}				
			}
		}
		
		return changedLicenseTypeOssList;
	}

	@Transactional
	@Override
	@CacheEvict(value="autocompleteCache", allEntries=true)
	public String registLicenseMaster(LicenseMaster licenseMaster) {
		String[] licenseNicknames = licenseMaster.getLicenseNicknames();
		String licenseId = licenseMaster.getLicenseId();
		
		if(StringUtil.isEmpty(licenseId)){
			licenseMaster.setCreator(licenseMaster.getLoginUserName());
		}
		
		licenseMaster.setModifier(licenseMaster.getLoginUserName());
		// trim 처리
		licenseMaster.setLicenseName(licenseMaster.getLicenseName().trim());
		
		if(StringUtil.isEmpty(licenseId)){
			licenseMapper.insertLicenseMaster(licenseMaster);
		} else {
			licenseMapper.updateLicenseMaster(licenseMaster);
		}
		
		/*
		 * 1. 라이센스 닉네임 삭제 
		 * 2. 라이센스 닉네임 재등록
		 */
		licenseMapper.deleteLicenseNickname(licenseMaster);
		
		if(licenseNicknames != null){
			for(String nickName : licenseNicknames){
				if(!isEmpty(nickName)) {
					licenseMaster.setLicenseNickname(nickName.trim());
					licenseMapper.insertLicenseNickname(licenseMaster);
				}
			}
		}
		
		//코멘트 등록
		if(!isEmpty(licenseMaster.getComment())) {
			CommentsHistory param = new CommentsHistory();
			param.setReferenceId(licenseMaster.getLicenseId());
			param.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_LICENSE);
			param.setContents(licenseMaster.getComment());
			
			commentService.registComment(param);
		}
		
		// code refresh
		CoCodeManager.getInstance().refreshLicenseInfo();

		return licenseMaster.getLicenseId();
	}
	
	@Override
	public boolean distributeLicense(String licenseId, boolean distributionFlag) {
		boolean SuccessType = false;
		LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_BY_ID.get(licenseId);
		
		if(licenseBean == null) {
			log.error("LICENSE INFO IS NULL, LICENSE ID = " + avoidNull(licenseId));
		} else {
			String contents = makeLicenseInfoHtml(licenseBean);
			String fileName = !isEmpty(licenseBean.getShortIdentifier()) ? licenseBean.getShortIdentifier() : licenseBean.getLicenseNameTemp();
			fileName = fileName.replaceAll(" ", "_").replaceAll("/", "_") + ".html";
			
			if(!isEmpty(contents)) {
				String filePath = CommonFunction.appendProperty("root.dir", "internal.url.dir.path");
				FileUtil.writhFile(filePath, fileName, contents);
				
				SuccessType = true;
			} else {
				log.error("OSDD DISTRIBUTE LCIENSE info is null");
				SuccessType = false;
			}
		}
		
		return SuccessType;
	}
	
	private String makeLicenseInfoHtml(LicenseMaster bean) {
		Map<String, Object> model = new HashMap<>();
		model.put("licenseName", bean.getLicenseNameTemp());
		
		if(!isEmpty(bean.getShortIdentifier())) {
			model.put("shortIdentifier", bean.getShortIdentifier());
		}
		
		if(!isEmpty(bean.getWebpage())) {
			model.put("webPage", bean.getWebpage());
		}
		
		model.put("licenseText", CommonFunction.lineReplaceToBR(bean.getLicenseText()));
		model.put("templateURL", "/template/notice/license.html");
		
		return CommonFunction.VelocityTemplateToString(model);
	}
	
	@Override
	@Cacheable(value="autocompleteCache", key="#root.methodName")
	public List<LicenseMaster> getLicenseNameList() {
		List<LicenseMaster> list = licenseMapper.selectLicenseNameList();
		
		if(list != null) {
			for(LicenseMaster bean : list) {
				if(CoConstDef.FLAG_YES.equals(bean.getObligationNeedsCheckYn())) {
					bean.setObligationCode(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK);
				} else if(CoConstDef.FLAG_YES.equals(bean.getObligationDisclosingSrcYn())) {
					bean.setObligationCode(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE);
				} else if(CoConstDef.FLAG_YES.equals(bean.getObligationNotificationYn())) {
					bean.setObligationCode(CoConstDef.CD_DTL_OBLIGATION_NOTICE);
				}
			}
		}
		
		return list;
	}
	
	@Override
	public void sendLicenseTypeChangedMail(String licenseId, LicenseMaster beforeBean, LicenseMaster afterBean, String comment) {
		// OSS를 참조하고 있는 프로젝트 관련자에게 메일 발송
		// 동일메일 수신을 방지하기위해 변경 내용 + 프로젝트 리스트 형식으로 발송하기 위해, 사용자 개별 발송한다.
		// 관련자들 모두 취득
		List<Project> prjList = licenseMapper.getLicenseChangeForUserList(licenseId);
		
		if(prjList != null) {
			// 수신자 대상 추출 (프로젝트 정보를 함께 넘긴다)
			Map<String, Map<String, Project>> sendInfoMap = new HashMap<>();
			
			for(Project bean : prjList) {
				Map<String, Project> _info = null;
				
				if(!isEmpty(bean.getCreator())) {
					_info = sendInfoMap.get(bean.getCreator());
					
					if(_info == null) {
						_info = new HashMap<>();
					}
					
					if(!_info.containsKey(bean.getPrjId())) {
						_info.put(bean.getPrjId(), bean);
						if(sendInfoMap.containsKey(bean.getCreator())) {
							sendInfoMap.replace(bean.getCreator(), _info);
						} else {
							sendInfoMap.put(bean.getCreator(), _info);
						}
					}
				} 
				if(!isEmpty(bean.getReviewer())) {
					_info = sendInfoMap.get(bean.getReviewer());
					
					if(_info == null) {
						_info = new HashMap<>();
					}
					
					if(!_info.containsKey(bean.getPrjId())) {
						_info.put(bean.getPrjId(), bean);
						if(sendInfoMap.containsKey(bean.getReviewer())) {
							sendInfoMap.replace(bean.getReviewer(), _info);
						} else {
							sendInfoMap.put(bean.getReviewer(), _info);
						}
					}
				} 
				if(!isEmpty(bean.getPrjUserId())) {
					_info = sendInfoMap.get(bean.getPrjUserId());
					
					if(_info == null) {
						_info = new HashMap<>();
					}
					
					if(!_info.containsKey(bean.getPrjId())) {
						_info.put(bean.getPrjId(), bean);
						if(sendInfoMap.containsKey(bean.getPrjUserId())) {
							sendInfoMap.replace(bean.getPrjUserId(), _info);
						} else {
							sendInfoMap.put(bean.getPrjUserId(), _info);
						}
					}
				}
			}
			
			// sendInfoMap 건수 (사람수) 만큼 발송
			for(String userId : sendInfoMap.keySet()) {
				CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE_TYPE);
				mailBean.setToIds(new String[]{userId});
				List<Project> tempList = new ArrayList<>();
				Map<String, Project> prjInfo = sendInfoMap.get(userId);
				
				for(String prjId : prjInfo.keySet()) {
					tempList.add(prjInfo.get(prjId));
				}
				
				mailBean.setParamPrjList(tempList);
				mailBean.setComment(comment);
				mailBean.setCompareDataBefore(beforeBean);
				mailBean.setCompareDataAfter(afterBean);
				
				CoMailManager.getInstance().sendMail(mailBean);	
			}
		}	
	}
}
