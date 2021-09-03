/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.OSS;
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
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoOssValidator;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Controller
@Slf4j
public class OssController extends CoTopComponent{
	private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;
	@PostConstruct
	public void setResourcePathPrefix(){
		RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	}
	
	@Autowired OssService ossService;
	@Autowired HistoryService historyService;
	@Autowired CommentService commentService;
	@Autowired ProjectMapper projectMapper;
	@Autowired PartnerMapper partnerMapper;
	@Autowired SelfCheckService selfCheckService;
	@Autowired ProjectService projectService;
	
	private final String SESSION_KEY_SEARCH = "SESSION_KEY_OSS_LIST";
	
	@GetMapping(value={OSS.LIST}, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res, Model model){
		OssMaster searchBean = null;
		
		if(!CoConstDef.FLAG_YES.equals(req.getParameter("gnbF"))) {
			deleteSession(SESSION_KEY_SEARCH);
			
			searchBean = new OssMaster();
		} else if(getSessionObject(SESSION_KEY_SEARCH) != null) {
			searchBean = (OssMaster) getSessionObject(SESSION_KEY_SEARCH);
			
			if(searchBean != null && searchBean.getCopyrights() != null && searchBean.getCopyrights().length > 0) {
				String _str = "";
				
				for(String s : searchBean.getCopyrights()) {
					if(!isEmpty(_str)) {
						_str += "\r\n";
					}
					_str += s;
				}
				
				searchBean.setCopyright(_str);
			}
		}
		
		if(req.getParameter("ossName") != null) { // OSS List 로드 시 Default로 ossName 검색조건에 추가
			if(searchBean == null) {
				searchBean = new OssMaster();
			}
			
			searchBean.setOssName(req.getParameter("ossName"));
		}
		
		if(getSessionObject("defaultLoadYn") != null) {
			model.addAttribute("defaultLoadYn", CoConstDef.FLAG_YES);
			
			deleteSession("defaultLoadYn");
		}
		
		model.addAttribute("searchBean", searchBean);
		
		return OSS.LIST_JSP;
	}
	
	@GetMapping(value=OSS.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(
			OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		
		if(sidx != null) {
			sidx  = sidx.split("[,]")[1].trim();
		}
		
		ossMaster.setSidx(sidx);
		
		ossMaster.setCurPage(page);
		ossMaster.setPageListSize(rows);
		
		if("search".equals(req.getParameter("act"))) {
			// 검색 조건 저장
			putSessionObject(SESSION_KEY_SEARCH, ossMaster);
		} else if(getSessionObject(SESSION_KEY_SEARCH) != null) {
			ossMaster = (OssMaster) getSessionObject(SESSION_KEY_SEARCH);
		}
		
		Map<String, Object> map = null;
		ossMaster.setSearchFlag(CoConstDef.FLAG_YES);
		ossMaster.setSearchFlag(CoConstDef.FLAG_YES); // 화면 검색일 경우 "Y" export시 "N"
		
		try {
			map = ossService.getOssMasterList(ossMaster);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader(map);
	}
	
	@GetMapping(value=OSS.AUTOCOMPLETE_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteAjax(
			OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		return makeJsonResponseHeader(ossService.getOssNameList());
	}
	
	@GetMapping(value={OSS.EDIT}, produces = "text/html; charset=utf-8")
	public String edit(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		if(getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NEWOSS_DEFAULT_DATA, null)) != null) {
			OssMaster bean = (OssMaster) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NEWOSS_DEFAULT_DATA, null), true);
			
			model.addAttribute("ossName", avoidNull(bean.getOssName()));
			model.addAttribute("ossVersion", avoidNull(bean.getOssVersion()));
			model.addAttribute("downloadLocation", avoidNull(bean.getDownloadLocation()));
			model.addAttribute("homepage", avoidNull(bean.getHomepage()));
			
			if(bean.getLicenseName().contains(",")) {
				List<OssLicense> licenseList = new ArrayList<OssLicense>();
				HashMap<String, Object> map = new HashMap<String, Object>();
				int idx = 1;
				
				for(String licenseNm : bean.getLicenseName().split(",")) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO.get(licenseNm);
					OssLicense result = new OssLicense();
					
					result.setOssLicenseIdx(""+idx);
					result.setOssLicenseComb(idx == 1 ? "" : "AND");
					
					if(master != null) {
						String shortIDentifier = isEmpty(master.getShortIdentifier()) ? master.getLicenseName() : master.getShortIdentifier();
						result.setLicenseId(master.getLicenseId());
						result.setLicenseName(master.getLicenseName());
						result.setLicenseNameEx(shortIDentifier);
						result.setObligation(master.getObligation());
						result.setObligationChecks(master.getObligationChecks());
					} else {
						result.setLicenseName(licenseNm);
						result.setLicenseNameEx(licenseNm);
					}
					
					licenseList.add(result);
					
					idx++;
				}
				
				map.put("rows", licenseList);
				model.addAttribute("list", toJson(map));
			} else {
				model.addAttribute("licenseName", avoidNull(bean.getLicenseName()));
			}
			
			model.addAttribute("copyright", avoidNull(bean.getCopyright()));
			model.addAttribute("ossType", avoidNull(bean.getOssType()));
			// nick name 체크
			// oss name으로 nickname이 존재할 경우 초기 표시되어야함
			// 표시되지 않은 상태에서 save시 기존 nickname이 모두 삭제됨
			model.addAttribute("ossNickList", toJson(ossService.getOssNickNameListByOssName(bean.getOssName())));
			
			List<String> downloadLocationList = new ArrayList<>();
			downloadLocationList.add(avoidNull(bean.getDownloadLocation()));
			model.addAttribute("downloadLocationList", toJson(downloadLocationList));
		} else {
			// 신규 등록시에도 ossNickList 은 필수(empty array를 설정)
			List<String> nickList = new ArrayList<>();
			model.addAttribute("ossNickList", toJson(nickList.toArray(new String[nickList.size()])));
			
			List<String> downloadLocationList = new ArrayList<>();
			model.addAttribute("downloadLocationList", toJson(downloadLocationList.toArray(new String[downloadLocationList.size()])));
		}
		
		return OSS.EDIT_JSP;
	}
	
	@GetMapping(value={OSS.EDIT_ID}, produces = "text/html; charset=utf-8")
	public String edit(@PathVariable String ossId, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		OssMaster ossMaster = new OssMaster(ossId);
		Map<String, Object> map = ossService.getOssLicenseList(ossMaster);
		ossMaster = ossService.getOssMasterOne(ossMaster);
		
		if(ossMaster.getOssVersion() == null) {
			ossMaster.setOssVersion("");
		}
		
		model.addAttribute("list", toJson(map));
		model.addAttribute("detail", toJson(ossMaster));
		model.addAttribute("ossId", ossMaster.getOssId());
		
		// 참조 프로젝트 목록 조회
		boolean projectListFlag = CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES);
		
		if(projectListFlag) {
			// 성능이슈로 이전 son system의 프로젝트 조회와 신규 FOSSLight System 프로젝트 조회를 union 하지 않고 각각 조회한다.
			List<OssComponents> components = projectMapper.selectOssRefPrjList1(ossMaster);
			
			// 참조 partner 목록 조회
			List<PartnerMaster> componentsPartner = new ArrayList<PartnerMaster>();
			
			boolean partnerFlag = CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES);
			
			if(partnerFlag) {
				componentsPartner = partnerMapper.selectOssRefPartnerList(ossMaster);
			}
			
			model.addAttribute("components", toJson(components));
			model.addAttribute("componentsPartner", toJson(componentsPartner));
		}
		
		model.addAttribute("projectListFlag", projectListFlag);
		
		Vulnerability vulnParam = new Vulnerability();
		vulnParam.setOssId(ossId);
		List<Vulnerability> vulnInfoList = ossService.getOssVulnerabilityList(vulnParam);
		
		if(vulnInfoList != null && !vulnInfoList.isEmpty()) {
			model.addAttribute("vulnInfoList", toJson(vulnInfoList));
		}
		
		List<String> nickList = new ArrayList<>();
		model.addAttribute("ossNickList", toJson(nickList.toArray(new String[nickList.size()])));
		
		List<String> downloadLocationList = new ArrayList<>();
		model.addAttribute("downloadLocationList", toJson(downloadLocationList.toArray(new String[downloadLocationList.size()])));
		
		return CommonFunction.isAdmin() ? OSS.EDIT_JSP : OSS.VIEW_JSP;
	}
	
	@GetMapping(value={OSS.POPUPLIST_ID}, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> getPopupList(@PathVariable String ossId, OssMaster ossMaster, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		
		ossMaster.setCurPage(page);
		ossMaster.setPageListSize(rows);
		
		Map<String, Object> map = null;
		
		try{
			map = ossService.getOssPopupList(ossMaster);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return makeJsonResponseHeader(map);
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value={OSS.COPY_ID}, produces = "text/html; charset=utf-8")
	public String copy(@PathVariable String ossId, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		OssMaster ossMaster = new OssMaster(ossId);
		String _version = req.getParameter("ossVersion");
		boolean isVersionup = false;
		
		if(_version != null) {
			isVersionup = true;
		}
		
		Map<String, Object> map = ossService.getOssLicenseList(ossMaster);
		ossMaster = ossService.getOssMasterOne(ossMaster);
		
		if(isVersionup) {
			ossMaster.setOssVersion(_version);
		} else {
			ossMaster.setOssName(ossMaster.getOssName() + "_Copied");
		}
		
		ossMaster.setOssLicenses((List<OssLicense>) map.get("rows"));
		ossMaster.setOssId(null);
		model.addAttribute("copyData", toJson(ossMaster));
		List<String> nickList = new ArrayList<>();
		model.addAttribute("ossNickList", toJson(nickList.toArray(new String[nickList.size()])));
		
		List<String> downloadLocationList = new ArrayList<>();
		model.addAttribute("downloadLocationList", toJson(downloadLocationList.toArray(new String[downloadLocationList.size()])));
		
		return OSS.EDIT_JSP;
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value=OSS.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(
			@ModelAttribute OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		String resCd = "00";
		String result = null;
		HashMap<String, Object> resMap = new HashMap<>();
		
		/*Json String -> Json Object*/
		String jsonString = ossMaster.getOssLicensesJson();
		Type collectionType = new TypeToken<List<OssLicense>>(){}.getType();
		List<OssLicense> list = checkLicenseId((List<OssLicense>) fromJson(jsonString, collectionType));
		
		ossMaster.setOssLicenses(list);
		String action = "";
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
		
		if(downloadLocations != null){
			if(downloadLocations.length >= 1){
				for(String url : downloadLocations){
					if(!isEmpty(url)){
						ossMaster.setDownloadLocation(url); // 등록된 url 중 공백을 제외한 나머지에서 첫번째 url을 만나게 되면 등록을 함.
						
						break;
					}
				}
			}
		} else if(downloadLocations == null){
			ossMaster.setDownloadLocation("");
		}
		
		if(!ossMaster.getComment().startsWith("<p>")) {
			ossMaster.setComment(CommonFunction.lineReplaceToBR(ossMaster.getComment())); 
		}
		
		try{
			History h = new History();
			
			// OSS 수정
			if(!isNew){
				beforeBean = ossService.getOssInfo(ossId, true);
				result = ossService.registOssMaster(ossMaster);
				CoCodeManager.getInstance().refreshOssInfo();
				h = ossService.work(ossMaster);
				action = CoConstDef.ACTION_CODE_UPDATE;
				afterBean = ossService.getOssInfo(ossId, true);
				
				if(!beforeBean.getOssName().equalsIgnoreCase(afterBean.getOssName())) {
					isChangedName = true;
				}
				
				String beforeDeactivateFlag = avoidNull(beforeBean.getDeactivateFlag(), CoConstDef.FLAG_NO);
				String afterDeactivateFlag = avoidNull(afterBean.getDeactivateFlag(), CoConstDef.FLAG_NO);
				
				if(CoConstDef.FLAG_NO.equals(beforeDeactivateFlag) 
						&& CoConstDef.FLAG_YES.equals(afterDeactivateFlag)) {
					isDeactivateFlag = true;
				}
				
				if(CoConstDef.FLAG_YES.equals(beforeDeactivateFlag) 
						&& CoConstDef.FLAG_NO.equals(afterDeactivateFlag)) {
					isActivateFlag = true;
				}
			} else{ // OSS 등록
				// 기존에 동일한 이름으로 등록되어 있는 OSS Name인 지 확인
				isNewVersion = CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossMaster.getOssName().toUpperCase());
				
				result = ossService.registOssMaster(ossMaster);
				CoCodeManager.getInstance().refreshOssInfo();
				ossId = result;
				
				h = ossService.work(ossMaster);
				action = CoConstDef.ACTION_CODE_INSERT;
			}

			h.sethAction(action);
			historyService.storeData(h);
			
			// history 저장 성공 후 메일 발송
			try {
				String mailType = "";
				
				if(isNew) {
					mailType = isNewVersion 
								? CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION 
								: CoConstDef.CD_MAIL_TYPE_OSS_REGIST;	
				} else {
					mailType = isChangedName 
								? CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME 
								: CoConstDef.CD_MAIL_TYPE_OSS_UPDATE;
					
					if(isDeactivateFlag) { 
						mailType = CoConstDef.CD_MAIL_TYPE_OSS_DEACTIVATED;
					}
					
					if(isActivateFlag) { 
						mailType = CoConstDef.CD_MAIL_TYPE_OSS_ACTIVATED;
					}
				}
				
				CoMail mailBean = new CoMail(mailType);
				mailBean.setParamOssId(ossId);
				mailBean.setComment(ossMaster.getComment());
				
				if(!isNew && !isDeactivateFlag) {
					mailBean.setCompareDataBefore(beforeBean);
					mailBean.setCompareDataAfter(afterBean);
				}
				
				CoMailManager.getInstance().sendMail(mailBean);				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			resCd="10";
			
			putSessionObject("defaultLoadYn", true); // 화면 로드 시 default로 리스트 조회 여부 flag
		} catch(Exception e) {
			log.error("OSS " + action + "Failed.", e);
			log.error(e.getMessage(), e);
		}
		
		resMap.put("resCd", resCd);
		resMap.put("ossId", result);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value={OSS.DEL_AJAX})
	public @ResponseBody ResponseEntity<Object> delAjax(
			@ModelAttribute OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		String resCd="00";
		HashMap<String, Object> resMap = new HashMap<>();
		// mail 발송을 위해 삭제전 data 취득
		OssMaster ossMailInfo = ossService.getOssInfo(ossMaster.getOssId(), true);
		
		if(ossMailInfo.getOssNicknames() != null) {
			ossMailInfo.setOssNickname(CommonFunction.arrayToString(ossMailInfo.getOssNicknames(), "<br>"));	
		}
		
		// 삭제처리
		ossService.deleteOssMaster(ossMaster);

		CoCodeManager.getInstance().refreshOssInfo();
		resCd="10";
		
		putSessionObject("defaultLoadYn", true); // 화면 로드 시 default로 리스트 조회 여부 flag
		
		try {
			History h = ossService.work(ossMaster);
			
			h.sethAction(CoConstDef.ACTION_CODE_DELETE);
			historyService.storeData(h);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_OSS_DELETE);
			mailBean.setParamOssId(ossMaster.getOssId());
			mailBean.setComment(ossMaster.getComment());
			mailBean.setParamOssInfo(ossMailInfo);
			
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		resMap.put("resCd", resCd);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value=OSS.DEL_OSS_VERSION_MERGE_AJAX)
	public @ResponseBody ResponseEntity<Object> delOssWithVersionMeregeAjax(
			@ModelAttribute OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		String resCd="00";
		HashMap<String, Object> resMap = new HashMap<>();

		try {
			ossService.deleteOssWithVersionMerege(ossMaster);
			resCd = "10";
			
			putSessionObject("defaultLoadYn", true); // 화면 로드 시 default로 리스트 조회 여부 flag
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		resMap.put("resCd", resCd);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value=OSS.VALIDATION)
	public @ResponseBody ResponseEntity<Object> validation(
			@ModelAttribute OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		/*Json String -> Json Object*/
		String jsonString = ossMaster.getOssLicensesJson();
		Type collectionType = new TypeToken<List<OssLicense>>(){}.getType();
		@SuppressWarnings("unchecked")
		List<OssLicense> list = checkLicenseId((List<OssLicense>) fromJson(jsonString, collectionType) );
		
		ossMaster.setOssLicenses(list);

		// validator
		T2CoOssValidator validator = new T2CoOssValidator();
		Map<String, String> reqMap = new HashMap<>();
		// form data set(with request)
		validator.saveRequest(req, reqMap);
		validator.setAppendix("ossMaster", ossMaster);
		validator.setIgnore("LICENSE_TYPE");
		
		T2CoValidationResult vr = validator.validateObject(reqMap, list, "OSS"); 
		
		if(!vr.isValid()) {
			return makeJsonResponseHeader(vr.getValidMessageMap());
		}
		
		if(isEmpty(ossMaster.getOssId())) {
			OssMaster checkOssInfo = ossService.getOssInfo(ossMaster.getOssId(), ossMaster.getOssName(), false);
			
			if(CoConstDef.FLAG_YES.equals(checkOssInfo.getDeactivateFlag())) {
				return makeJsonResponseHeader(false, "deactivate");
			} else {
				// 신규 등록인 경우 nick name 체크(변경된 사항이 있는 경우, 삭제는 불가)
				String[] _mergeNicknames = ossService.checkNickNameRegOss(ossMaster.getOssName(), ossMaster.getOssNicknames());
				
				// 삭제는 불가능하기 때문에, 건수가 다르면 기존에 등록된 닉네임이 있다는 의미
				// null을 반환하지는 않는다.
				if(_mergeNicknames.length > 0) {
					return makeJsonResponseHeader(false, null, _mergeNicknames);
				}
			}
		} else {
			// 수정이면서 oss name이 변경되었고, 변경하려고 하는 oss에 nick name이 등록되어 있는 경우, 사용자 confirm 필요
			// 변경전 정보를 취득
			OssMaster orgBean = ossService.getOssInfo(ossMaster.getOssId(), false);
			
			if(CoConstDef.FLAG_YES.equals(ossMaster.getDeactivateFlag()) 
					&& CoConstDef.FLAG_YES.equals(orgBean.getDeactivateFlag())) {
				return makeJsonResponseHeader(false, "deactivate");
			}
			
			// oss name 변경 여부 확인
			if(orgBean != null && !ossMaster.getOssName().equalsIgnoreCase(orgBean.getOssNameTemp())) {
				List<String> orgNickList = new ArrayList<>();
				List<String> delNickList = new ArrayList<>();
				// oss name이 변경 되었고, 변경 후 oss name이 이미 등록되어 있는 경우
				Map<String, List<String>> diffMap = new HashMap<>();
				String[] orgNicks = ossService.getOssNickNameListByOssName(ossMaster.getOssName());
				
				if(orgNicks != null && orgNicks.length > 0) {
					for(String s : orgNicks) {
						orgNickList.add(s);
					}
				}
				
				// 기존 oss name에 물려있는 nick name이 존재하는 경우, nick name은 무시됨
				// 변경 후 oss name에 nick name이 존재하는 경우 이관됨
				if(ossMaster.getOssNicknames() != null) {
					for(String _nick : ossMaster.getOssNicknames()) {
						if(!isEmpty(_nick)) {
							// 삭제되는 oss
							if(orgNickList.isEmpty() || !orgNickList.contains(_nick)) {
								delNickList.add(_nick);
							}
						}
					}
				}
				
				// 변경하려는 oss name에 nick name이 등록되어 있는 상태이면 merge 해야함
				if(!orgNickList.isEmpty()) {
					boolean isChange = !delNickList.isEmpty();
					
					if(!isChange) {
						// 대상 nick name과 현재 oss name이 동일하면 pass
						List<String> currNickNameList = new ArrayList<>();
						
						if(ossMaster.getOssNicknames() != null) {
							for(String _nick : ossMaster.getOssNicknames()) {
								if(!isEmpty(_nick)) {
									currNickNameList.add(_nick);
								}
							}
						}
						
						if(orgNickList.size() != currNickNameList.size()) {
							isChange = true;
						}
						
						for(String s : orgNickList) {
							if(!currNickNameList.contains(s)) {
								isChange = true;
								break;
							}
						}
					}
					
					if(isChange) {
						diffMap.put("addNickArr", orgNickList);
						
						if(!delNickList.isEmpty()) {
							diffMap.put("delNickArr", delNickList);
						}
						return makeJsonResponseHeader(false, "hasDelNick", diffMap);
					}					
				}
			}
		}
		
		return makeJsonResponseHeader();
	}
	
	private List<OssLicense> checkLicenseId(List<OssLicense> list) {
		// license name만 있고 id는 없는 경우를 우해 license id를 찾는다.
		// validation에서 license id는 필수로 되어 있기때문
		if(list != null) {
			for(OssLicense bean : list) {
				if(isEmpty(bean.getLicenseId()) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(bean.getLicenseNameEx().toUpperCase())) {
					bean.setLicenseId(CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseNameEx().toUpperCase()).getLicenseId());
					
					if(isEmpty(bean.getLicenseName())) {
						bean.setLicenseName(CoCodeManager.LICENSE_INFO_UPPER.get(bean.getLicenseNameEx().toUpperCase()).getLicenseName());
					}
				}
			}
		}
		
		return list;
	}
	
	@GetMapping(value = OSS.CHECK_EXIST_OSS_CONF)
	public @ResponseBody String checkExistOssConf(HttpServletRequest req, HttpServletResponse res,
			Model model, @RequestParam(value="ossId", required=true)String ossId) {
		return ossService.checkExistOssConf(ossId);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = OSS.CHECK_VD_DIFF)
	public @ResponseBody ResponseEntity<Object> checkVdiff(@RequestBody HashMap<String, Object> map, HttpServletRequest req, HttpServletResponse res,
			Model model) {
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put("ossId", (String) map.get("ossId"));
		reqMap.put("ossName", (String) map.get("ossName"));
		
		Type collectionType = new TypeToken<List<OssLicense>>(){}.getType();
		List<OssLicense> list = checkLicenseId((List<OssLicense>) fromJson((String) map.get("license"), collectionType));
		
		reqMap.put("license", list);
		
		HashMap<String, Object> resMap = new HashMap<>();
		resMap.put("vFlag", ossService.checkVdiff(reqMap));
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value = OSS.SAVE_COMMENT)
	public @ResponseBody ResponseEntity<Object> saveComment(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		T2CoValidationResult vResult = null;
		
		try {
			// validation check
			vResult = validate(req);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!vResult.isValid()) {
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		CommentsHistory result = null;
		
		try {
			result = commentService.registComment(commentsHistory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return makeJsonResponseHeader(result);
	}
	
	@PostMapping(value=OSS.DELTE_COMMENT)
	public @ResponseBody ResponseEntity<Object> deleteComment(
			@ModelAttribute CommentsHistory commentsHistory
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		T2CoValidationResult vResult = null;
		
		try {
			//validation check
			 vResult = validate(req);	
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(!vResult.isValid()) {
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		try {
			commentService.deleteComment(commentsHistory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return makeJsonResponseHeader(vResult.getValidMessageMap());
	}
	
	@GetMapping(value=OSS.OSS_MERGE_CHECK_LIST, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> ossMergeCheckList(@PathVariable String ossId, @PathVariable String newOssId, OssMaster ossMaster, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		Map<String, Object> map = ossService.ossMergeCheckList(ossMaster);
		
		return makeJsonResponseHeader(map);
	}
	
	@PostMapping(value=OSS.URL_DUPLICATE_VALIDATION)
	public @ResponseBody ResponseEntity<Object> urlDuplicateValidation(
			@ModelAttribute OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		// validator
		T2CoOssValidator validator = new T2CoOssValidator();
		validator.setAppendix("ossMaster", ossMaster);
		String validType = ossMaster.getValidationType();
		
		if(validator.VALID_DOWNLOADLOCATION.equals(validType)){
			validator.setVALIDATION_TYPE(validator.VALID_DOWNLOADLOCATION);
		} else if(validator.VALID_DOWNLOADLOCATIONS.equals(validType)){
			validator.setVALIDATION_TYPE(validator.VALID_DOWNLOADLOCATIONS);
		}else if(validator.VALID_HOMEPAGE.equals(validType)){
			validator.setVALIDATION_TYPE(validator.VALID_HOMEPAGE);
		}
		
		T2CoValidationResult vr = validator.validateObject(ossMaster);
		
		if(!vr.isDiff()) {
			return makeJsonResponseHeader(true, null, null, null, vr.getDiffMessageMap());
		}
		
		return makeJsonResponseHeader();
	}
	
	@PostMapping(value=OSS.SAVE_SESSION_OSS_INFO)
	public @ResponseBody ResponseEntity<Object> saveSessionOssInfo(
			@ModelAttribute OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		OssMaster orgMaster = null;
		
		if(!isEmpty(ossMaster.getOssName())) {
			orgMaster = ossService.getLastModifiedOssInfoByName(ossMaster);
		}
		
		if(orgMaster != null && !isEmpty(orgMaster.getOssId())) {
			return makeJsonResponseHeader(true, orgMaster.getOssId());
		} else {
			try {
				if(!putSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NEWOSS_DEFAULT_DATA, null), ossMaster)) {
					log.error("failed save session oss edit (new) "); 
					return makeJsonResponseHeader(false, null);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return makeJsonResponseHeader(false, null);
			}
			
			return makeJsonResponseHeader();
		}
	}
	
	@GetMapping(value = OSS.OSS_LIST_BY_NAME)
	public @ResponseBody ResponseEntity<Object> getOssIdCheck(
			@ModelAttribute OssMaster bean, Model model) {
		Map<String, List<OssMaster>> resultMap = new HashMap<>();
		resultMap.put("ossList", ossService.getOssListByName(bean));
		
		return makeJsonResponseHeader(resultMap);
	}
	
	@GetMapping(value=OSS.OSS_BULK_REG, produces = "text/html; charset=utf-8")
	public String ossBulkRegPage(HttpServletRequest req, HttpServletResponse res, @ModelAttribute Project bean, Model model){
		// oss list (oss name으로만)
		model.addAttribute("projectInfo", bean);
		
		return OSS.OSS_BULK_REG_JSP;
	}
	
	@GetMapping(value = OSS.OSS_BULK_REG_AJAX, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> getOssBulkRegAjax(
			HttpServletRequest req, 
			HttpServletResponse res,
			@ModelAttribute ProjectIdentification paramBean, 
			Model model) {
		
		Map<String, OssMaster> resultMap = new HashMap<>();
		Map<String, OssMaster> resultMapVer = new HashMap<>();
		List<String> dupCheckList = new ArrayList<>();

		// 일괄등록 대상 Identification의 모든 components 정보를 취득한다.
		paramBean.setBulkRegistYn(CoConstDef.FLAG_YES);
		List<ProjectIdentification> componentList = projectMapper.selectIdentificationGridList(paramBean);
		int gridIdSeq = 0;
		
		if(componentList != null) {
			// 미등록 OSS List 정보를 추출한다.
			for(ProjectIdentification bean : componentList) {
				if(isEmpty(bean.getOssName()) || "-".equals(bean.getOssName())) {
					continue;
				}
				
				String chkKey = (bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim()).toUpperCase();
				
				// 중복체크
				if(dupCheckList.contains(chkKey)) {
					continue;
				}
				
				// exclude 제외
				if(CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				if(!CoCodeManager.OSS_INFO_UPPER.containsKey(chkKey)) {
					// licnese 정보 취득
					ProjectIdentification licenseParam = new ProjectIdentification();
					licenseParam.setComponentId(bean.getComponentId());
					List<ProjectIdentification> licenseList = projectMapper.identificationSubGrid(licenseParam);
					
					String licenseName = CommonFunction.makeLicenseExpressionIdentify(licenseList);
					
					if(CommonFunction.isIgnoreLicense(licenseName)){
						continue;
					}
					
					// OSS Master 형으로 변경
					OssMaster ossBean = new OssMaster();
					ossBean.setOssName(bean.getOssName().trim());
					ossBean.setOssVersion(avoidNull(bean.getOssVersion()).trim());
					ossBean.setLicenseName(licenseName);
					ossBean.setDownloadLocation(bean.getDownloadLocation());
					ossBean.setHomepage(bean.getHomepage());
					ossBean.setCopyright(bean.getCopyrightText());
					
					// OSS Name이 등록되어 있는 경우, NickName을 치환한다.
					if(CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossBean.getOssName().toUpperCase())) {
						ossBean.setOssName(CoCodeManager.OSS_INFO_UPPER_NAMES.get(ossBean.getOssName().toUpperCase()));
						
						// 등록되어 있는 oss name인 경우 마지막으로 생성한 oss를 기준으로 사용자 입력정보를 무시하고 DB에 등록된 정보를 설정한다.
						OssMaster lastCreatedOssBean = ossService.getLastModifiedOssInfoByName(ossBean);
						if(lastCreatedOssBean != null && !isEmpty(lastCreatedOssBean.getOssId())) {
							OssMaster masterBean = CoCodeManager.OSS_INFO_BY_ID.get(lastCreatedOssBean.getOssId());
							ossBean.setLicenseName(CommonFunction.makeLicenseExpression(masterBean.getOssLicenses()));
							String downloadLocation = StringUtil.isEmpty(lastCreatedOssBean.getDownloadLocationGroup()) ? lastCreatedOssBean.getDownloadLocation() : lastCreatedOssBean.getDownloadLocationGroup();
							ossBean.setDownloadLocation(downloadLocation);
							ossBean.setHomepage(masterBean.getHomepage());
							ossBean.setCopyright(masterBean.getCopyright());
							ossBean.setSummaryDescription(masterBean.getSummaryDescription()); // OSS bulk registration > Summary Description란 추가
							
							// nickname 정보를 설정한다.
							if(masterBean.getOssNicknames() != null) {
								ossBean.setOssNickname(CommonFunction.arrayToString(masterBean.getOssNicknames(), ","));
							}
						}
						
						ossBean.setRegType("VER");
					} else {
						ossBean.setRegType("OSS");
					}
					
					ossBean.setGridId("jqg_" + gridIdSeq++);
					
					// default 정렬
					if(CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossBean.getOssName().toUpperCase())) {
						resultMapVer.put(chkKey, ossBean);
					} else {
						resultMap.put(chkKey, ossBean);
					}

					dupCheckList.add(chkKey);
					
					// nick name 을 정식명칭으로 변경한 case도 중복체크에 포함한다.
					String chkKey2 = (bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim()).toUpperCase();
					
					if(!chkKey.equals(chkKey2)) {
						dupCheckList.add(chkKey2);
					}
				}
			}
		}
		
		List<OssMaster> list = null;
		
		if(!resultMap.isEmpty()) {
			list = new ArrayList<>(resultMap.values()) ;
		}
		
		if(!resultMapVer.isEmpty()) {
			if(list == null) {
				list = new ArrayList<>(resultMapVer.values()) ;
			} else {
				list.addAll(new ArrayList<>(resultMapVer.values()));
			}
		}
		
		if(list == null) {
			list = new ArrayList<>();
		}

		T2CoOssValidator validator = new T2CoOssValidator();
		validator.setAppendix("ossList", list);
		validator.setVALIDATION_TYPE(validator.VALID_OSSLIST_BULK);
		T2CoValidationResult vr = validator.validate(new HashMap<>());
		Map<String, String> validMapResult = vr.getValidMessageMap();
		Map<String, String> diffMapResult = vr.getDiffMessageMap();
		
		HashMap<String, Object> resMap = new HashMap<>();
		resMap.put("ossList", list);
		resMap.put("validMapResult", validMapResult);
		resMap.put("diffMapResult", diffMapResult);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value=OSS.SAVE_OSS_BULK_REG)
	public @ResponseBody ResponseEntity<Object> saveOssBulkReg(
			@RequestBody OssMaster ossMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		// 이미등록되어 있는 OSS의 경우 Skip한다.
		if(CoCodeManager.OSS_INFO_UPPER.containsKey( (ossMaster.getOssName() + "_" + avoidNull(ossMaster.getOssVersion())).toUpperCase() )) {
			return makeJsonResponseHeader(true, "Skip");
		}
		
		// validator
		T2CoOssValidator validator = new T2CoOssValidator();
		validator.setAppendix("ossMaster", ossMaster);
		validator.setVALIDATION_TYPE(validator.VALID_OSS_BULK);
		T2CoValidationResult vr = validator.validate(new HashMap<>()); 
		Map<String, String> validMapResult = vr.getValidMessageMap();
		Map<String, String> diffMapResult = vr.getDiffMessageMap();
		
		HashMap<String, Object> resMap = new HashMap<>();
		resMap.put("validMapResult", validMapResult);
		resMap.put("diffMapResult", diffMapResult);
		
		if(!vr.isValid()) {
			return makeJsonResponseHeader(resMap);
		}
		
		// 기존에 동일한 이름으로 등록되어 있는 OSS Name인 지 확인
		boolean isNewVersion = CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossMaster.getOssName().toUpperCase());
		
		ossMaster.setLicenseDiv(CoConstDef.LICENSE_DIV_SINGLE); // default
		// multi license 대응
		List<OssLicense> ossLicenseList = new ArrayList<>();
		int licenseIdx = 0;
		
		for(String s : ossMaster.getLicenseName().toUpperCase().split(" OR ")) {
			// 순서가 중요
			String orGroupStr = s.replaceAll("\\(", " ").replaceAll("\\)", " ");
			boolean groupFirst = true;
			
			for(String s2 : orGroupStr.split(" AND ")) {
				LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(s2.trim().toUpperCase());
				OssLicense licenseBean = new OssLicense();
				licenseBean.setOssLicenseIdx(String.valueOf(licenseIdx++));
				licenseBean.setLicenseId(license.getLicenseId());
				licenseBean.setLicenseName(license.getLicenseNameTemp());
				licenseBean.setOssLicenseComb(groupFirst ? "OR" : "AND");
				ossLicenseList.add(licenseBean);
				groupFirst = false;
			}
		}
		
		ossMaster.setOssLicenses(ossLicenseList);
		
		if(ossLicenseList.size() > 1) {
			ossMaster.setLicenseDiv(CoConstDef.LICENSE_DIV_MULTI);
		}
		
		// nick Name을 Array형으로 변경해줌
		if(!isEmpty(ossMaster.getOssNickname())) {
			// trim 처리는 registOssMaster 내에서 처리한다.
			ossMaster.setOssNicknames(ossMaster.getOssNickname().split(","));
		}
		
		if(!isEmpty(ossMaster.getDownloadLocation())){
			String result = "";
			
			for(String url : ossMaster.getDownloadLocation().split(",")) {
				if(!isEmpty(result)) {
					result += ",";
				}
				
				if(url.endsWith("/")) {
					result += url.substring(0, url.length()-1);
				}else {
					result += url;
				}
			}
			
			ossMaster.setDownloadLocations(result.split(","));
			ossMaster.setDownloadLocation(result);
		}
		
		if(!isEmpty(ossMaster.getHomepage())) {
			if(ossMaster.getHomepage().endsWith("/")) {
				String homepage = ossMaster.getHomepage();
				ossMaster.setHomepage(homepage.substring(0, homepage.length()-1));
			}
		}
		
		// editor를 이용하지 않고, textarea로 등록된 코멘트의 경우 br 태그로 변경
		ossMaster.setComment(CommonFunction.lineReplaceToBR(ossMaster.getComment()) );
		ossMaster.setAddNicknameYn(CoConstDef.FLAG_YES); //nickname을 clear&insert 하지 않고, 중복제거를 한 나머지 nickname에 대해서는 add함.
		String resultOssId = ossService.registOssMaster(ossMaster);
		CoCodeManager.getInstance().refreshOssInfo();
		
		History h = ossService.work(ossMaster);
		h.sethAction(CoConstDef.ACTION_CODE_INSERT);
		historyService.storeData(h);
		
		// history 저장 성공 후 메일 발송
		try {
			CoMail mailBean = new CoMail(isNewVersion ? CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION : CoConstDef.CD_MAIL_TYPE_OSS_REGIST);
			mailBean.setParamOssId(resultOssId);
			mailBean.setComment(ossMaster.getComment());
			
			CoMailManager.getInstance().sendMail(mailBean);				
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader(true, "Success", diffMapResult);
	}
	
	@GetMapping(value=OSS.OSS_POPUP, produces = "text/html; charset=utf-8")
	public String viewOssPopup(HttpServletRequest req, HttpServletResponse res, @ModelAttribute OssMaster bean, Model model){
		// oss list (oss name으로만)
		model.addAttribute("ossInfo", bean);
		
		return OSS.OSS_POPUP_JSP;
	}
	
	@GetMapping(value=OSS.OSS_DETAIL_VIEW_AJAX, produces = "text/html; charset=utf-8")
	public String ossDetailView(HttpServletRequest req, HttpServletResponse res, @ModelAttribute OssMaster bean, Model model){
		List<OssMaster> ossList = ossService.getOssListByName(bean);
		
		if(ossList != null && !ossList.isEmpty()) {
			OssMaster _bean = ossList.get(0);
			_bean.setOssName(StringUtil.replaceHtmlEscape(_bean.getOssName()));
			
			model.addAttribute("ossInfo", ossList.get(0));
		} else {
			model.addAttribute("ossInfo", new OssMaster());
		}
		
		return OSS.OSS_DETAILS_VIEW_AJAX_JSP;
	}
	
	@GetMapping(value = OSS.CHECK_EXISTS_OSS_BY_NAME)
	public @ResponseBody ResponseEntity<Object> checkExistsOssByname(
			@ModelAttribute OssMaster bean, Model model) {
		return makeJsonResponseHeader(ossService.checkExistsOssByname(bean) > 0, "unconfirmed oss");
	}
	
	@GetMapping(value=OSS.CHECK_OSS_NAME, produces = "text/html; charset=utf-8")
	public String checkOssName(HttpServletRequest req, HttpServletResponse res, @ModelAttribute Project bean, Model model){
		// oss list (oss name으로만)
		model.addAttribute("projectInfo", bean);
		
		return OSS.CHECK_OSS_NAME_JSP;
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = OSS.CHECK_OSS_NAME_AJAX, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> getCheckOssNameAjax(
			HttpServletRequest req, 
			HttpServletResponse res,
			@ModelAttribute ProjectIdentification paramBean, 
			Model model,
			@PathVariable String targetName) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> map = null;
		List<ProjectIdentification> result = new ArrayList<ProjectIdentification>();
		
		switch(targetName.toUpperCase()) {
			case CoConstDef.CD_CHECK_OSS_NAME_SELF:
				map = selfCheckService.getIdentificationGridList(paramBean);
				
				break;
			case CoConstDef.CD_CHECK_OSS_NAME_IDENTIFICATION:
				map = projectService.getIdentificationGridList(paramBean);
				
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
				result.addAll(ossService.checkOssNameData(mainData, validMap, null));
				resMap.put("validMap", validMap);
			}
			
			if(!vr.isDiff()){
				Map<String, String> diffMap = vr.getDiffMessageMap();
				result.addAll(ossService.checkOssNameData(mainData, null, diffMap));
				resMap.put("diffMap", diffMap);
			}
		}
		
		if(result.size() > 0) {
			result = ossService.checkOssName(result);
			resMap.put("list", result);
		}
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value=OSS.SAVE_OSS_CHECK_NAME)
	public @ResponseBody ResponseEntity<Object> saveOssCheckName(
			@RequestBody ProjectIdentification paramBean
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model
			, @PathVariable String targetName){
		Map<String, Object> map = ossService.saveOssCheckName(paramBean, targetName);
		
		return makeJsonResponseHeader(map);
	}
	
	@PostMapping(value=OSS.SAVE_OSS_NICKNAME)
	public @ResponseBody ResponseEntity<Object> saveOssNickname(
			@RequestBody ProjectIdentification paramBean
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		OssMaster beforeBean = ossService.getOssInfo(null, paramBean.getCheckName(), true);
		
		Map<String, Object> map = ossService.saveOssNickname(paramBean);
		boolean updatedFlag = (boolean) map.get("isValid");
		
		if(updatedFlag) {
			CoCodeManager.getInstance().refreshOssInfo();
			String action = CoConstDef.ACTION_CODE_UPDATE;
			OssMaster afterBean = ossService.getOssInfo(null, paramBean.getCheckName(), true);

			History h = ossService.work(afterBean);
			h.sethAction(action);
			historyService.storeData(h);
			
			// history 저장 성공 후 메일 발송
			try {
				CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_ADDNICKNAME_UPDATE);
				mailBean.setParamOssId(afterBean.getOssId());
				mailBean.setComment(afterBean.getComment());
				mailBean.setCompareDataBefore(beforeBean);
				mailBean.setCompareDataAfter(afterBean);
				
				CoMailManager.getInstance().sendMail(mailBean);				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return makeJsonResponseHeader((boolean) map.get("isValid"), (String) map.get("returnType"));
	}
	
	@PostMapping(value=OSS.SAVE_OSS_ANALYSIS_LIST)
	public @ResponseBody ResponseEntity<Object> saveOssAnalysisList(
			@RequestBody OssMaster ossBean
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model
			, @PathVariable String targetName){
		Map<String, Object> map = ossService.saveOssAnalysisList(ossBean, targetName.toUpperCase());
		
		return makeJsonResponseHeader((boolean) map.get("isValid"), (String) map.get("returnType"));
	}
	
	@RequestMapping(value=OSS.OSS_AUTO_ANALYSIS, method = {RequestMethod.POST, RequestMethod.GET}, produces = "text/html; charset=utf-8")
	public String ossAutoRegist(HttpServletRequest req, HttpServletResponse res, @ModelAttribute Project bean, Model model){
		// oss list (oss name으로만)
		model.addAttribute("projectInfo", bean);
		
		return OSS.OSS_AUTO_ANALYSIS_JSP;
	}
	
	@GetMapping(value=OSS.AUTO_ANALYSIS_LIST)
	public @ResponseBody ResponseEntity<Object> getAutoAnalysisList(
			HttpServletRequest req, 
			HttpServletResponse res,
			@ModelAttribute OssMaster ossBean, 
			Model model) {
		Map<String, Object> result = ossService.getOssAnalysisList(ossBean);
		
		return makeJsonResponseHeader(result);
	}
	
	@PostMapping(value=OSS.START_ANALYSIS)
	public @ResponseBody ResponseEntity<Object> startAnalysis(
			@RequestBody OssMaster ossBean
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		String donwloadId = null;
		Map<String, Object> result = null;
		
		try {
			donwloadId = ExcelDownLoadUtil.getExcelDownloadId("autoAnalysis", ossBean.getPrjId(), RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
			
			if(!isEmpty(donwloadId)) {
				result = ossService.startAnalysis(ossBean.getPrjId(), donwloadId);
				return makeJsonResponseHeader(result);
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		result = new HashMap<String, Object>();
		result.put("isValid", false);
		result.put("returnMsg", "Failure");
		
		return makeJsonResponseHeader(result);
	}
	
	@GetMapping(value=OSS.ANALYSIS_RESULT_LIST)
	public @ResponseBody ResponseEntity<Object> getAnalysisResultList(
			HttpServletRequest req, 
			HttpServletResponse res,
			@ModelAttribute OssMaster ossMaster, 
			Model model) {
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		
		if(sidx != null) {
			sidx  = sidx.split("[,]")[1].trim();
		}
		
		ossMaster.setSidx(sidx);
		
		if(page == 0) {
			page = ossService.getAnalysisListPage(rows, ossMaster.getPrjId());
		}
		
		ossMaster.setCurPage(page);
		ossMaster.setPageListSize(rows);
		
		Map<String, Object> result = ossService.getOssAnalysisList(ossMaster);
		
		String analysisResultListPath = CommonFunction.emptyCheckProperty("autoanalysis.output.path", "/autoanalysis/out/dev") + "/" + ossMaster.getPrjId() + "/result";
		
		result.put("analysisResultListPath", analysisResultListPath);
		
		result = ExcelUtil.getAnalysisResultList(result);
		
		return makeJsonResponseHeader(result);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value=OSS.SET_SESSION_ANALYSIS_RESULT_DATA)
	public @ResponseBody ResponseEntity<Object> setSessionAnalysisResultData(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestBody HashMap<String, Object> map, 
			Model model){
		// oss list (oss name으로만)
		String groupId = (String) map.get("groupId");
		String dataString = (String) map.get("dataString");
		Type typeAnalysis = new TypeToken<List<OssAnalysis>>() {}.getType();
		List<OssAnalysis> analysisResultData = new ArrayList<OssAnalysis>();
		analysisResultData = (List<OssAnalysis>) fromJson(dataString, typeAnalysis);
		String sessionKey = CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_ANALYSIS_RESULT_DATA, groupId);
		
		if(getSessionObject(sessionKey) != null) {
			deleteSession(sessionKey);
		}
		
		return makeJsonResponseHeader(putSessionObject(sessionKey, analysisResultData));
	}
	
	@GetMapping(value=OSS.ANALYSIS_RESULT_DETAIL_ID)
	public String analysisResultDetail(HttpServletRequest req, HttpServletResponse res, @PathVariable String groupId, Model model){
		model.addAttribute("groupId", groupId);
		
		return OSS.ANALYSIS_RESULT_DETAIL_JSP;
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value=OSS.SESSION_ANALYSIS_RESULT_DATA)
	public @ResponseBody ResponseEntity<Object> getSessionAnalysisResultData(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestBody OssAnalysis analysisBean, 
			Model model){
		Map<String, Object> result = new HashMap<String, Object>();
		
		String sessionKey = CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_ANALYSIS_RESULT_DATA, analysisBean.getGroupId());
		List<OssAnalysis> detailData = (List<OssAnalysis>) getSessionObject(sessionKey);
		
		if(detailData != null) {
			result.put("isValid", true);
			result.put("detailData", detailData);
			result.put("cloneLicenseData", new OssMaster());
			
			CommonFunction.getAnalysisValidation(result, detailData);
		} else {
			result.put("isValid", false);
		}
		
		return makeJsonResponseHeader(result);
	}
	
	@PostMapping(value=OSS.UPDATE_ANALYSIS_COMPLETE)
	public @ResponseBody ResponseEntity<Object> updateAnalysisComplete(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestBody OssAnalysis analysisBean, 
			Model model){
		Map<String, Object> result = null;
		
		try {
			result = ossService.updateAnalysisComplete(analysisBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			result.put("isValid", false);
			result.put("returnMsg", "Complete Failure");
		}
		
		return makeJsonResponseHeader(result);
	}
	
	@PostMapping(value=OSS.SAVE_OSS_ANALYSIS_DATA)
	public @ResponseBody ResponseEntity<Object> saveOssAnalysisData(
			@RequestBody OssAnalysis analysisBean
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		// 이미등록되어 있는 OSS의 경우 Skip한다.
		if(CoCodeManager.OSS_INFO_UPPER.containsKey( (analysisBean.getOssName() + "_" + avoidNull(analysisBean.getOssVersion())).toUpperCase() )) {
			return makeJsonResponseHeader(false, "Skip");
		}
		
		// analysis Data -> OssMaster 변환
		OssMaster resultData = new OssMaster();
		
		if(!isEmpty(analysisBean.getOssName())) {
			resultData.setOssName(analysisBean.getOssName());
		}
		
		if(!isEmpty(analysisBean.getOssVersion())) {
			resultData.setOssVersion(analysisBean.getOssVersion());
		}
		
		// 기존에 동일한 이름으로 등록되어 있는 OSS Name인 지 확인
		boolean isNewVersion = CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(analysisBean.getOssName().toUpperCase());
		
		resultData.setGridId(analysisBean.getGridId());
		resultData.setLicenseDiv(CoConstDef.LICENSE_DIV_SINGLE); // default
		// multi license 대응
		List<OssLicense> ossLicenseList = new ArrayList<>();
		int licenseIdx = 0;
		
		if(!isEmpty(analysisBean.getLicenseName())) {
			for(String s : analysisBean.getLicenseName().toUpperCase().split(" OR ")) {
				// 순서가 중요
				String orGroupStr = s.replaceAll("\\(", " ").replaceAll("\\)", " ");
				boolean groupFirst = true;
				for(String s2 : orGroupStr.split(" AND ")) {
					LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(s2.trim().toUpperCase());
					OssLicense licenseBean = new OssLicense();
					
					if(license != null) {
						licenseBean.setOssLicenseIdx(String.valueOf(licenseIdx++));
						licenseBean.setLicenseId(license.getLicenseId());
						licenseBean.setLicenseName(license.getLicenseNameTemp());
						licenseBean.setOssLicenseComb(groupFirst ? "OR" : "AND");
					} else {
						licenseBean.setOssLicenseIdx(String.valueOf(licenseIdx++));
						licenseBean.setLicenseId("");
						licenseBean.setLicenseName(s2);
						licenseBean.setOssLicenseComb(groupFirst ? "OR" : "AND");
					}
					
					ossLicenseList.add(licenseBean);
					groupFirst = false;
				}
			}
			
			resultData.setLicenseName(analysisBean.getLicenseName());
			resultData.setOssLicenses(ossLicenseList);
		} else {
			resultData.setLicenseName("");
		}
		
		if(ossLicenseList.size() > 1) {
			resultData.setLicenseDiv(CoConstDef.LICENSE_DIV_MULTI);
		}
		
		// nick Name을 Array형으로 변경해줌
		if(!isEmpty(analysisBean.getOssNickname())) {
			// trim 처리는 registOssMaster 내에서 처리한다.
			resultData.setOssNickname(analysisBean.getOssNickname());
			resultData.setOssNicknames(analysisBean.getOssNickname().split(","));
		}
		
		if(!isEmpty(analysisBean.getDownloadLocation())){
			String result = "";
			
			for(String url : analysisBean.getDownloadLocation().split(",")) {
				if(!isEmpty(result)) {
					result += ",";
				}
				
				if(url.endsWith("/")) {
					result += url.substring(0, url.length()-1);
				} else {
					result += url;
				}
			}
			
			resultData.setDownloadLocations(result.split(","));
			resultData.setDownloadLocation(result);
		} else {
			resultData.setDownloadLocation("");
		}
		
		if(!isEmpty(analysisBean.getHomepage())) {
			if(analysisBean.getHomepage().endsWith("/")) {
				String homepage = analysisBean.getHomepage();
				resultData.setHomepage(homepage.substring(0, homepage.length()-1));
			} else {
				resultData.setHomepage(analysisBean.getHomepage());
			}
		} else {
			resultData.setHomepage("");
		}
		
		resultData.setCopyright(analysisBean.getOssCopyright());
		resultData.setSummaryDescription(analysisBean.getSummaryDescription());
		// editor를 이용하지 않고, textarea로 등록된 코멘트의 경우 br 태그로 변경
		resultData.setComment(CommonFunction.lineReplaceToBR(analysisBean.getComment()) );
		resultData.setAddNicknameYn(CoConstDef.FLAG_YES); //nickname을 clear&insert 하지 않고, 중복제거를 한 나머지 nickname에 대해서는 add함.
		
		HashMap<String, Object> resMap = new HashMap<>();
		
		try {
			// validator
			T2CoOssValidator validator = new T2CoOssValidator();
			validator.setAppendix("ossAnalysis", analysisBean);
			validator.setVALIDATION_TYPE(validator.VALID_OSSANALYSIS);
			T2CoValidationResult vr = validator.validate(new HashMap<>()); 
			Map<String, String> validMapResult = vr.getValidMessageMap();
			Map<String, String> diffMapResult = vr.getDiffMessageMap();
			
			resMap.put("validMapResult", validMapResult);
			resMap.put("diffMapResult", diffMapResult);
			
			if(!vr.isValid()) {
				return makeJsonResponseHeader(false, "Fail", resMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			return makeJsonResponseHeader(false, "Fail");
		}
		
		Map<String, Object> result = null;
		String resultOssId = "";
		
		try {
			resultOssId = ossService.registOssMaster(resultData); // oss 정보 등록
			
			analysisBean.setComponentId(analysisBean.getGroupId());
			analysisBean.setReferenceOssId(resultOssId);
			result = ossService.updateAnalysisComplete(analysisBean); // auto-analysis 완료처리
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			
			return makeJsonResponseHeader(false, "Fail");
		}
		
		CoCodeManager.getInstance().refreshOssInfo(); // 등록된 oss info 갱신
		
		History h = ossService.work(resultData);
		h.sethAction(CoConstDef.ACTION_CODE_INSERT);
		historyService.storeData(h);
		
		// history 저장 성공 후 메일 발송
		try {
			CoMail mailBean = new CoMail(isNewVersion ? CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION : CoConstDef.CD_MAIL_TYPE_OSS_REGIST);
			mailBean.setParamOssId(resultOssId);

			mailBean.setComment(resultData.getComment());
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader(result);
	}
}