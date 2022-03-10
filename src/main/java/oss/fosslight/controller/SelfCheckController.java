/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.util.MimeTypeUtils;
import org.springframework.core.io.FileSystemResource;

import com.google.gson.reflect.TypeToken;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.SELF_CHECK;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.MailService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SearchService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;
import oss.fosslight.service.VerificationService;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.domain.File;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.History;

@Controller
public class SelfCheckController extends CoTopComponent {
	@Autowired ProjectService projectService;
	@Autowired FileService fileService;
	@Autowired CommentService commentService;
	@Autowired HistoryService historyService;
	@Autowired MailService mailService;
	@Autowired T2UserService userService;
	@Autowired SelfCheckService selfCheckService;
	@Autowired VerificationService verificationService;
	@Autowired VerificationMapper verificationMapper;
	@Autowired ProjectMapper projectMapper;
	@Autowired CodeMapper codeMapper;
	@Autowired SearchService searchService;
	
	private final String SESSION_KEY_SEARCH = "SESSION_KEY_SELFCHECK_LIST";
	
	/**
	 * [화면] self-check 목록 조회
	 */
	@GetMapping(value = SELF_CHECK.LIST, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) {
		T2Users param = new T2Users();
		param.setSortField("userName");
		param.setSortOrder("asc");
		
		model.addAttribute("creator", userService.getAllUsers(param));
		model.addAttribute("reviewer", userService.getReviwer());
		
		Project searchBean = null;
		
		if(!CoConstDef.FLAG_YES.equals(req.getParameter("gnbF"))) {
			deleteSession(SESSION_KEY_SEARCH);
			
			searchBean = searchService.getSelfCheckSearchFilter(loginUserName());
			if(searchBean == null) {
				searchBean = new Project();
			}
		} else if(getSessionObject(SESSION_KEY_SEARCH) != null) {
			searchBean = (Project) getSessionObject(SESSION_KEY_SEARCH);
		}
		
		model.addAttribute("searchBean", searchBean);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		
		return SELF_CHECK.LIST_JSP;
	}

	/**
	 * [화면] 프로젝트 상세
	 */
	@GetMapping(value = SELF_CHECK.EDIT, produces = "text/html; charset=utf-8")
	public String edit(HttpServletRequest req, HttpServletResponse res, Model model) {
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));

		return SELF_CHECK.EDIT_JSP;
	}
	
	/**
	 * [API] 프로젝트 상세 조회 Edit Page
	 */
	@RequestMapping(value = SELF_CHECK.EDIT_ID, method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "text/html; charset=utf-8")
	public String edit(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) {
		Project project = new Project();
		project.setPrjId(prjId);
		project = selfCheckService.getProjectDetail(project);
		T2Users user = userService.getLoginUserInfo();
		project.setPrjEmail(user.getEmail());

		model.addAttribute("project", project);
		model.addAttribute("detail", toJson(project));
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));
		
		// Admin인 경우 Creator 를 변경할 수 있도록 사용자 정보를 반환한다.
		if(CommonFunction.isAdmin()) {
			List<T2Users> userList = userService.selectAllUsers();
			
			if(userList != null) {
				model.addAttribute("userWithDivisionList", userList);
			}
		}
		
		return SELF_CHECK.EDIT_JSP;
	}
	
	/**
	 * [API] 프로젝트 상세 조회 View Page
	 */
	@RequestMapping(value = SELF_CHECK.VIEW_ID, method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "text/html; charset=utf-8")
	public String view(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) {
		Project project = new Project();
		project.setPrjId(prjId);
		
		try {
			project = selfCheckService.getProjectDetail(project);
			
			if(CoConstDef.FLAG_YES.equals(project.getUseYn())) {
				model.addAttribute("project", project);
				model.addAttribute("detail", toJson(project));
				model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
				model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
				model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
				model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));
				
				// Admin인 경우 Creator 를 변경할 수 있도록 사용자 정보를 반환한다.
				if(CommonFunction.isAdmin()) {
					List<T2Users> userList = userService.selectAllUsers();
					
					if(userList != null) {
						model.addAttribute("userWithDivisionList", userList);
					}
				}
			} else {
				model.addAttribute("message", "Reqeusted URL is for a deleted Self-Check Project. Please contact the creator or watcher of the Self-Check Project.");
			}
		} catch (Exception e) {
			model.addAttribute("message", "Reqeusted URL contains Self-Check Project ID that doesn't exist. Please check the Self-Check Project ID again.");
		}
		
		
		return SELF_CHECK.VIEW_JSP;
	}
	
	/**
	 * [API] 프로젝트 목록 조회
	 */
	@GetMapping(value = SELF_CHECK.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		project.setCurPage(page);
		project.setPageListSize(rows);
		project.setSortField(sidx);
		project.setSortOrder(sord);
		
		if("search".equals(req.getParameter("act"))){
			// 검색 조건 저장
			putSessionObject(SESSION_KEY_SEARCH, project);
		}else if(getSessionObject(SESSION_KEY_SEARCH) != null){
			project = (Project) getSessionObject(SESSION_KEY_SEARCH);
		}

		Map<String, Object> map = selfCheckService.getProjectList(project);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * [API] Identification 공통 메인 조회
	 */
	@GetMapping(value = SELF_CHECK.OSSGRID_ID_CD)
	public @ResponseBody ResponseEntity<Object> srcMainGridAjax(@ModelAttribute ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId, @PathVariable String code) {
		// 요청한 reference의 이전 정보도 삭제
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), identification.getReferenceDiv(),
				identification.getReferenceId()));
		Map<String, Object> result = getOssComponentDataInfo(identification, code);
		
		if (result != null) {
			putSessionObject(CommonFunction.makeSessionKey(loginUserName(), code, prjId), result);
		}
		
		return makeJsonResponseHeader(result);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getOssComponentDataInfo(ProjectIdentification identification, String code) {
		if (isEmpty(identification.getReferenceDiv())) {
			identification.setReferenceDiv(code);
		}

		if (!isEmpty(identification.getMainData())) {
			Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {}.getType();
			identification.setMainDataGridList((List<ProjectIdentification>) fromJson(identification.getMainData(), collectionType2));
		}

		if (!isEmpty(identification.getSubData())) {
			Type collectionType3 = new TypeToken<List<List<ProjectIdentification>>>() {}.getType();
			identification.setSubDataGridList((List<List<ProjectIdentification>>) fromJson(identification.getSubData(), collectionType3));
		}

		Map<String, Object> map = selfCheckService.getIdentificationGridList(identification);

		T2CoProjectValidator pv = new T2CoProjectValidator();
		
		if (map != null) {
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
			pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			// set self check obligation message
			List<ProjectIdentification> mainDataList = CommonFunction.identificationUnclearObligationCheck((List<ProjectIdentification>) map.get("mainData"), vr.getErrorCodeMap(), vr.getWarningCodeMap());
			
			if (!vr.isValid()) {
				Map<String, String> validMap = vr.getValidMessageMap();
				map.put("validData", validMap);
				map.replace("mainData", CommonFunction
						.identificationSortByValidInfo(mainDataList, validMap, vr.getDiffMessageMap(), vr.getInfoMessageMap(), true, true));
			} else {
				map.replace("mainData", CommonFunction
						.identificationSortByValidInfo(mainDataList, null, null, null, true, true));
			}
			
			if(!vr.isDiff()){
				Map<String, String> diffMap = vr.getDiffMessageMap();
				map.put("diffData", diffMap);
			}
		} 

		return map;
	}
	
	@GetMapping(value=SELF_CHECK.VIEW_AJAX, produces = "text/html; charset=utf-8")
	public String ossDetailView(HttpServletRequest req, HttpServletResponse res, @ModelAttribute Project bean, Model model){
		Project project = selfCheckService.getProjectDetail(bean);
		
		if(project != null) {
			model.addAttribute("project", project);
		} else {
			model.addAttribute("project", new Project());
		}
		
		return SELF_CHECK.VIEW_AJAX_JSP;
	}
	
	@GetMapping(value=SELF_CHECK.LICENSE_POPUP, produces = "text/html; charset=utf-8")
	public String viewLicensePopup(HttpServletRequest req, HttpServletResponse res, @ModelAttribute LicenseMaster bean, Model model){
		model.addAttribute("licenseInfo", bean);
		
		List<LicenseMaster> resultList = new ArrayList<LicenseMaster>();
		
		if(!isEmpty(bean.getLicenseName())) {
			for(String s : bean.getLicenseName().split(",")) {
				if(isEmpty(s)) {
					continue;
				}
				
				s = s.toUpperCase().trim();
				
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(s)) {
					resultList.add(CoCodeManager.LICENSE_INFO_UPPER.get(s));
				}
			}
		}

		model.addAttribute("isValid", !resultList.isEmpty());
		
		return SELF_CHECK.LICENSE_POPUP_JSP;
	}
	
	@PostMapping(value=SELF_CHECK.SEND_COMMENT)
	public @ResponseBody ResponseEntity<Object> sendComment(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		commentsHistory.setMailType(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS.equals(commentsHistory.getReferenceDiv())
				? CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_ADDED_COMMENT
				: CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_ADDED_COMMENT);
		
		commentService.registComment(commentsHistory);
		
		return makeJsonResponseHeader();
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = SELF_CHECK.SAVE_SRC)
	public @ResponseBody ResponseEntity<Object> saveSrc(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		// default validation
		boolean isValid = true;
		// last response map
		Map<String, String> resMap = new HashMap<>();
		// default 00:java error check code, 10:success code
		String resCd = "00";

		String prjId = (String) map.get("prjId");
		String csvFileId = (String) map.get("csvFileId");
		String delFileString = (String) map.get("csvDelFileIds");
		String FileSeqs = (String) map.get("csvFileSeqs");
		String identificationSubStatusSrc = (String) map.get("identificationSubStatusSrc");
		String mainDataString = (String) map.get("mainData");

		Type collectionType = new TypeToken<List<T2File>>() {}.getType();
		List<T2File> delFile = new ArrayList<T2File>();
		delFile = (List<T2File>) fromJson(delFileString, collectionType);
		
		List<T2File> addFile = new ArrayList<T2File>();
		addFile = (List<T2File>) fromJson(FileSeqs, collectionType);
		
		Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
		ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);
		ossComponents = CommonFunction.removeDuplicateLicense(ossComponents);
		List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
		
		if (CoConstDef.FLAG_NO.equals(identificationSubStatusSrc)) {
			Project project = new Project();
			project.setIdentificationSubStatusSrc(identificationSubStatusSrc);
			project.setPrjId(prjId);
		} else {
			/*
			 * 중간저장 대응을 위해 save시에는 validation check 를 수행하지 않는다. 문제가 생길경우, 꼭 필요한
			 * 체크를 별도의 type으로 추가해야함 // grid validation only customValidation
			 * check!! // 모든 체크 파라미터를 customValidation에 코딩 해야한다.
			 * T2CoProjectValidator pv = new T2CoProjectValidator();
			 * pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE); // main grid
			 * pv.setAppendix("mainList", ossComponents); // sub grid
			 * pv.setAppendix("subList", ossComponentsLicense);
			 * 
			 * // basic validator는 무시, validate를 호출하여 custom validator를 수행한다.
			 * T2CoValidationResult vr = pv.validate(new HashMap<>());
			 * 
			 * // return validator result if(!vr.isValid()) { return
			 * makeJsonResponseHeader(vr.getValidMessageMap()); }
			 */
			
			// save 시 가장 기본적인 유효성 체크만 진행 (길이, 형식 체크)
			T2CoProjectValidator pv = new T2CoProjectValidator();
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
			pv.setValidLevel(pv.VALID_LEVEL_BASIC);
			pv.setAppendix("mainList", ossComponents); // sub grid
			pv.setAppendix("subList", ossComponentsLicense);
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if(!vr.isValid()) {
				return makeJsonResponseHeader(vr.getValidMessageMap());
			}

			Project project = new Project();
			project.setPrjId(prjId);
			project.setSrcCsvFileId(csvFileId);
			project.setCsvFile(delFile);
			project.setCsvFileSeq(addFile);
			project.setIdentificationSubStatusSrc(identificationSubStatusSrc);
			
			Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(ossComponents, ossComponentsLicense);
			ossComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
			ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
			
			selfCheckService.registSrcOss(ossComponents, ossComponentsLicense, project);
		}

		// 정상처리된 경우 세션 삭제
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_SELF_COMPONENT_ID, prjId));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_SELFT_PROJECT, prjId));

		// success code set 10
		resCd = "10";
		resMap.put("isValid", String.valueOf(isValid));
		resMap.put("resCd", resCd);
		
		return makeJsonResponseHeader(resMap);
	}
	
	/**
	 * [API] 프로젝트 저장
	 */
	@PostMapping(value = SELF_CHECK.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(@ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		project.setWatchers(req.getParameterValues("watchers"));

		Boolean isNew = StringUtil.isEmpty(project.getPrjId());
		String copy = req.getParameter("copy");
		String creatorIdByName = null;
		
		if(CommonFunction.isAdmin() && !isNew && !"true".equals(copy)) {
			if(!isEmpty(project.getCreatorNm())) {
				List<T2Users> userList = userService.getUserListByName(project.getCreatorNm());
				
				if(userList != null) {
					for(T2Users _bean : userList) {
						if(_bean.getUserId().equals(project.getCreator())) {
							creatorIdByName = _bean.getUserId();
							
							if(!creatorIdByName.equals(project.getCreator())) {
								project.setCreator(_bean.getUserId());
							}
							
							break;
						}
					}
				}
			}
		}

		project.setCopy(copy);
		
		selfCheckService.registProject(project);

		Map<String, String> lastResult = new HashMap<>();
		lastResult.put("prjId", project.getPrjId());
		
		return makeJsonResponseHeader(true, null, lastResult);
	}
	
	/**
	 * [API] 프로젝트 삭제
	 */
	@PostMapping(value = SELF_CHECK.DEL_AJAX)
	public @ResponseBody ResponseEntity<Object> delAjax(@ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		selfCheckService.deleteProject(project);
		
		HashMap<String, Object> resMap = new HashMap<>();
		resMap.put("resCd", "10");
		
		return makeJsonResponseHeader(resMap);
	}
	@GetMapping(value=SELF_CHECK.LICENSE_USERGUIDE_HTML_NM, produces = "text/html; charset=utf-8")
	public @ResponseBody String getLicenseUserGuideHtml(@PathVariable String licenseName, HttpServletRequest req, HttpServletResponse res, Model model){
		LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(licenseName).toUpperCase());
		
		if(license != null) {
			return CommonFunction.makeHtmlLinkTagWithText(license.getDescription());
		}
		
		return "";
	}
	

	@PostMapping(value = SELF_CHECK.ADD_WATCHER)
	public @ResponseBody ResponseEntity<Object> addWatcher(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		try {
			if(!isEmpty(project.getPrjUserId()) || !isEmpty(project.getPrjEmail())) {
				selfCheckService.addWatcher(project);
			} else {
				return makeJsonResponseHeader(false, null);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader();
	}
	
	@PostMapping(value = SELF_CHECK.REMOVE_WATCHER)
	public @ResponseBody ResponseEntity<Object> removeWatcher(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		try {
			if(!isEmpty(project.getPrjUserId()) || !isEmpty(project.getPrjEmail())) {
				selfCheckService.removeWatcher(project);
			} else {
				return makeJsonResponseHeader(false, null);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Copys the watcher.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = SELF_CHECK.COPY_WATCHER)
	public @ResponseBody ResponseEntity<Object> copyWatcher(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
			HashMap<String, Object> resMap = new HashMap<>();
		try {			
			if(!isEmpty(project.getListKind()) && !isEmpty(project.getListId()) ) {
				
				List<Project> result = selfCheckService.copyWatcher(project);
				
				if(result != null) {
					for(Project bean : result) {
						if(!StringUtils.isEmpty(bean.getPrjDivision())) {
							bean.setPrjDivisionName(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, bean.getPrjDivision()));
						}
					}
					
					if(!isEmpty(project.getPrjId())) {
						boolean existSelfCheckWatcher = selfCheckService.existsWatcher(project);
						
						for(Project pm : result) {
							pm.setPrjId(project.getPrjId());
							
							if(existSelfCheckWatcher) {
								selfCheckService.addWatcher(pm);
							}
						}					
					}
				}

				resMap.put("copyWatcher", result);
			} else {
				return makeJsonResponseHeader(false, null);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader(resMap);
	}
	
	
	@GetMapping(value=SELF_CHECK.LICENSE_DATA)
	public @ResponseBody ResponseEntity<Object> getLicenseData(HttpServletRequest req
			, HttpServletResponse res, @ModelAttribute LicenseMaster bean, Model model){
		Map<String, List<LicenseMaster>> resultMap = new HashMap<>();
		List<LicenseMaster> resultList = new ArrayList<LicenseMaster>();
		
		if(!isEmpty(bean.getLicenseName())) {
			for(String s : bean.getLicenseName().split(",")) {
				if(isEmpty(s)) {
					continue;
				}
				
				s = s.toUpperCase().trim();
				
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(s)) {
					resultList.add(CoCodeManager.LICENSE_INFO_UPPER.get(s));
				}
			}
		}
		resultMap.put("licenseList", resultList);
		
		return makeJsonResponseHeader(resultMap);
	}

	@GetMapping(value = SELF_CHECK.VERIFICATION_PAGE_ID, produces = "text/html; charset=utf-8")
	public String list(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) {
		
		Project project = new Project();
		project.setPrjId(prjId);
		
		Project projectMaster = selfCheckService.getProjectDetail(project);
		
		if(!StringUtil.isEmpty(projectMaster.getCreator())){
			projectMaster.setPrjDivision(projectService.getDivision(projectMaster));	
		}
		
		CommentsHistory comHisBean = new CommentsHistory();
		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_USER);
		comHisBean.setReferenceId(projectMaster.getPrjId());
		projectMaster.setUserComment(commentService.getUserComment(comHisBean));
		project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);

		OssNotice _noticeInfo = selfCheckService.setCheckNotice(projectMaster);

		if(_noticeInfo != null && CoConstDef.FLAG_NO.equals(projectMaster.getUseCustomNoticeYn())) {
			// Notice Type: Accompanied with source code인 경우 Default Company Name, Email 세팅
			model.addAttribute("ossNotice", _noticeInfo);
		}

		projectMaster.setPrjId(_noticeInfo.getPrjId());
		projectMaster.setAllowDownloadNoticeHTMLYn("Y");
		projectMaster.setAllowDownloadNoticeTextYn("N");
		projectMaster.setAllowDownloadSimpleHTMLYn("N");
		projectMaster.setAllowDownloadSimpleTextYn("N");
		projectMaster.setAllowDownloadSPDXSheetYn("N");
		projectMaster.setAllowDownloadSPDXRdfYn("N");
		projectMaster.setAllowDownloadSPDXTagYn("N");
		
		//프로젝트 정보
		model.addAttribute("project", projectMaster);
		List<OssComponents> list = selfCheckService.getVerifyOssList(projectMaster);
		list = verificationService.setMergeGridData(list);
		List<LicenseMaster> userGuideLicenseList = new ArrayList<>();
		List<String> duplLicenseCheckList = new ArrayList<>(); // 중목제거용
		
		// 사용중인 라이선스에 user guide가 설정되어 있는 경우 체크
		if(list != null && !list.isEmpty()) {
			for(OssComponents bean : list) {
				// multi license의 경우 콤마 구분으로 반환됨
				if(!isEmpty(bean.getLicenseName())) {
					LicenseMaster licenseBean;
					for(String license : bean.getLicenseName().split(",", -1)) {
						licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
						if(licenseBean != null && !isEmptyWithLineSeparator(licenseBean.getDescription()) 
								&& !duplLicenseCheckList.contains(licenseBean.getLicenseId())
								&& CoConstDef.FLAG_YES.equals(licenseBean.getObligationDisclosingSrcYn())) {
							userGuideLicenseList.add(licenseBean);
							duplLicenseCheckList.add(licenseBean.getLicenseId());
						}
					}
				}
			}
		}
		
		List<File> files = new ArrayList<File>();
		files.add(verificationMapper.selectVerificationFile(projectMaster.getPackageFileId()));
		files.add(verificationMapper.selectVerificationFile(projectMaster.getPackageFileId2()));
		files.add(verificationMapper.selectVerificationFile(projectMaster.getPackageFileId3()));
		
		model.addAttribute("verify", toJson(selfCheckService.getVerificationOne(project)));
		model.addAttribute("ossList", toJson(list));
		model.addAttribute("files", files);
		
		model.addAttribute("userGuideLicenseList", userGuideLicenseList);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		
		return SELF_CHECK.PAGE_JSP;
	}

	@GetMapping(value = SELF_CHECK.VERIFICATION_PAGE_DIV_ID, produces = "text/html; charset=utf-8")
	public String list2(@PathVariable String prjId, @PathVariable String initDiv, HttpServletRequest req, HttpServletResponse res, Model model) {
		
		Project project = new Project();
		project.setPrjId(prjId);
		
		Project projectMaster = selfCheckService.getProjectDetail(project);
		
		if(!StringUtil.isEmpty(projectMaster.getCreator())){
			projectMaster.setPrjDivision(selfCheckService.getDivision(projectMaster));
		}
		
		CommentsHistory comHisBean = new CommentsHistory();
		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_USER);
		comHisBean.setReferenceId(projectMaster.getPrjId());
		projectMaster.setUserComment(commentService.getUserComment(comHisBean));
		
		//프로젝트 정보
		model.addAttribute("project", projectMaster);
		
		OssNotice _noticeInfo = selfCheckService.setCheckNotice(projectMaster);
				
		if(_noticeInfo != null && CoConstDef.FLAG_NO.equals(projectMaster.getUseCustomNoticeYn())) {
			// Notice Type: Accompanied with source code인 경우 Default Company Name, Email 세팅
			model.addAttribute("ossNotice", _noticeInfo);
		}
		
		List<OssComponents> list = selfCheckService.getVerifyOssList(projectMaster);
		
		List<LicenseMaster> userGuideLicenseList = new ArrayList<>();
		// 중목제거용
		List<String> duplLicenseCheckList = new ArrayList<>();
		
		// 사용중인 라이선스에 user guide가 설정되어 있는 경우 체크
		if(list != null && !list.isEmpty()) {
			for(OssComponents bean : list) {
				// multi license의 경우 콤마 구분으로 반환됨
				if(!isEmpty(bean.getLicenseName())) {
					LicenseMaster licenseBean;
					for(String license : bean.getLicenseName().split(",", -1)) {
						licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
						if(licenseBean != null && !isEmptyWithLineSeparator(licenseBean.getDescription()) 
								&& !duplLicenseCheckList.contains(licenseBean.getLicenseId())) {
							userGuideLicenseList.add(licenseBean);
							duplLicenseCheckList.add(licenseBean.getLicenseId());
						}
					}
					
				}
			}
		}
		
		List<File> files = new ArrayList<File>();
		files.add(verificationMapper.selectVerificationFile(projectMaster.getPackageFileId()));
		files.add(verificationMapper.selectVerificationFile(projectMaster.getPackageFileId2()));
		files.add(verificationMapper.selectVerificationFile(projectMaster.getPackageFileId3()));
		
		model.addAttribute("verify", toJson(selfCheckService.getVerificationOne(project)));
		model.addAttribute("ossList", toJson(list));
		model.addAttribute("files", files);
		model.addAttribute("initDiv", initDiv);
		
		model.addAttribute("userGuideLicenseList", userGuideLicenseList);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		
		return SELF_CHECK.PAGE_JSP;
	}

	@PostMapping(value = SELF_CHECK.VERIFICATION_NOTICE_AJAX)
	public @ResponseBody ResponseEntity<Object>  getNoticeHtml(HttpServletRequest req,HttpServletResponse res, Model model,	//
			@RequestParam(value="confirm", defaultValue="")String confirm, //
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			@RequestParam(value="allowDownloadNoticeHTMLYn", defaultValue="")String allowDownloadNoticeHTMLYn, //
			@RequestParam(value="allowDownloadNoticeTextYn", defaultValue="")String allowDownloadNoticeTextYn, //
			@RequestParam(value="allowDownloadSimpleHTMLYn", defaultValue="")String allowDownloadSimpleHTMLYn, //
			@RequestParam(value="allowDownloadSimpleTextYn", defaultValue="")String allowDownloadSimpleTextYn, //
			@RequestParam(value="allowDownloadSPDXSheetYn", defaultValue="")String allowDownloadSPDXSheetYn, //
			@RequestParam(value="allowDownloadSPDXRdfYn", defaultValue="")String allowDownloadSPDXRdfYn, //
			@RequestParam(value="allowDownloadSPDXTagYn", defaultValue="")String allowDownloadSPDXTagYn, //
			@RequestParam(value="allowDownloadSPDXJsonYn", defaultValue="")String allowDownloadSPDXJsonYn, //
			@RequestParam(value="allowDownloadSPDXYamlYn", defaultValue="")String allowDownloadSPDXYamlYn, //
			OssNotice ossNotice	//
			) throws IOException {
		
		String resultHtml = "";
		
		try {
			ossNotice.setDomain(CommonFunction.getDomain(req)); // domain Setting
			
			Project prjMasterInfo = selfCheckService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			
			if("conf".equals(confirm)){
				boolean ignoreMailSend = false;

				String userComment = ossNotice.getUserComment();

				//파일 만들기
				if(isEmpty(noticeFileId) || !CoConstDef.FLAG_YES.equals(useCustomNoticeYn)) {
					if(!selfCheckService.getNoticeHtmlFile(ossNotice)) {
						return makeJsonResponseHeader(false, getMessage("msg.common.valid2"));
					}
				}
				
				// reject 이후에 다시 confirm 하는 경우 이전의 package 파일 삭제 처리 필요 여부를 위해
				// source code obligation 체크를 한다.
				boolean needResetPackageFile = false;
				
				{
					Project projectMaster = selfCheckService.getProjectBasicInfo(ossNotice.getPrjId());
					if(!isEmpty(projectMaster.getPackageFileId())) {
						List<OssComponents> list = selfCheckService.getVerifyOssList(projectMaster);
						needResetPackageFile = list.isEmpty();
					}
				}

				Project prjInfo = null;
				//프로젝트 상태 변경
				Project project = new Project();
				project.setPrjId(ossNotice.getPrjId());
				//다운로드 허용 플래그
				project.setAllowDownloadNoticeHTMLYn(allowDownloadNoticeHTMLYn);
				project.setAllowDownloadNoticeTextYn(allowDownloadNoticeTextYn);
				project.setAllowDownloadSimpleHTMLYn(allowDownloadSimpleHTMLYn);
				project.setAllowDownloadSimpleTextYn(allowDownloadSimpleTextYn);
				project.setAllowDownloadSPDXSheetYn(allowDownloadSPDXSheetYn);
				project.setAllowDownloadSPDXRdfYn(allowDownloadSPDXRdfYn);
				project.setAllowDownloadSPDXTagYn(allowDownloadSPDXTagYn);
				project.setAllowDownloadSPDXJsonYn(allowDownloadSPDXJsonYn);
				project.setAllowDownloadSPDXYamlYn(allowDownloadSPDXYamlYn);
				
				project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
				
				// 프로젝트 기본정보의 distribution type이 verify까지만인 경우, distribute status를 N/A 처리한다.
				{
					prjInfo = selfCheckService.getProjectBasicInfo(ossNotice.getPrjId());
					
					if("T".equals(avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE, prjInfo.getDistributionType())).trim().toUpperCase())
							|| (CoConstDef.FLAG_NO.equals(avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTION_TYPE, prjInfo.getDistributionType())).trim().toUpperCase()))
							|| CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(prjInfo.getDistributeTarget()) // 배포사이트 사용안함으로 설정한 경우
							) {
						project.setDestributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
						ignoreMailSend = true;
					} else if(!CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(prjInfo.getDistributeTarget())
							&& CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA.equals(prjInfo.getDestributionStatus())) {
						project.setResetDistributionStatus(CoConstDef.FLAG_YES);
					}
					
					if(!isEmpty(prjInfo.getDestributionStatus()) 
							&& CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(confirm.toUpperCase())) {
						project.setChangedNoticeYn(CoConstDef.FLAG_YES);
					}
				}
				project.setModifier(project.getLoginUserName());
				
				// 기존에 등록한 package file의 삭제
				if(needResetPackageFile) {
					project.setNeedPackageFileReset(CoConstDef.FLAG_YES);
				}
				
				selfCheckService.updateStatusWithConfirm(project, ossNotice);
				
				try {
					History h = new History();
					h = selfCheckService.work(project);
					h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
					project = (Project) h.gethData();
					h.sethEtc(project.etcStr());
					
					historyService.storeData(h);
				} catch (Exception e) {

				}
				
				try {
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
					commHisBean.setReferenceId(project.getPrjId());
					commHisBean.setContents(userComment);
					commHisBean.setStatus(CoCodeManager.getCodeExpString(CoConstDef.CD_IDENTIFICATION_STATUS, CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM));
					
					commentService.registComment(commHisBean);
				} catch (Exception e) {

				}
								
				if(prjInfo != null && !CoConstDef.FLAG_YES.equals(prjInfo.getAndroidFlag()) && !ignoreMailSend) {
					try {
						CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF);
						mailBean.setParamPrjId(ossNotice.getPrjId());
						
						if(!isEmpty(ossNotice.getUserComment())) {
							mailBean.setComment(ossNotice.getUserComment());
						}
						
						CoMailManager.getInstance().sendMail(mailBean);
					} catch (Exception e) {

					}
				} else if(prjInfo != null) {
					// android 또는 distributin과 관련 없는 경우, packaging이 완료되었음만 공지한다.
					try {
						CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY);
						mailBean.setParamPrjId(ossNotice.getPrjId());
						
						if(!isEmpty(ossNotice.getUserComment())) {
							mailBean.setComment(ossNotice.getUserComment());
						}
						
						CoMailManager.getInstance().sendMail(mailBean);
					} catch (Exception e) {
						
					}
				}
				
				// package file 이 존재하는 경우 파일명을 변경한다.
				selfCheckService.changePackageFileNameDistributeFormat(ossNotice.getPrjId());
			} else { // preview 인 경우
				// 저장된 고지문구가 없을 경우
				if(isEmpty(noticeFileId) || !CoConstDef.FLAG_YES.equals(useCustomNoticeYn)) {
					resultHtml = selfCheckService.getNoticeHtml(ossNotice);
				} else { // 저장된 고지문구가 있을 경우
					T2File fileInfo = fileService.selectFileInfo(noticeFileId);
					resultHtml = CommonFunction.getStringFromFile(fileInfo.getLogiPath() + "/" + fileInfo.getLogiNm());
					// 파일을 못찾을 경우 예외처리
					if(isEmpty(resultHtml)) {
						resultHtml = selfCheckService.getNoticeHtml(ossNotice);
					}
				}
			}			
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		return makeJsonResponseHeader(true, null, resultHtml);
	}

	@PostMapping(value = SELF_CHECK.VERIFICATION_MAKE_NOTICE_PREVIEW)
	public @ResponseBody ResponseEntity<Object>  makeNoticePreview(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {

		String downloadId = null;
		ossNotice.setFileType("html");
		
		try {
			Project prjMasterInfo = selfCheckService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			
			if(isEmpty(noticeFileId)) {
				downloadId = selfCheckService.getNoticeHtmlFileForPreview(ossNotice);
			} else {
				downloadId = noticeFileId;
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}

	@ResponseBody
	@GetMapping(value = SELF_CHECK.VERIFICATION_DOWNLOAD_NOTICE_PREVIEW,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public ResponseEntity<FileSystemResource> downloadNoticePreview (
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{

		T2File fileInfo = fileService.selectFileInfo(req.getParameter("id"));
		
		return noticeToResponseEntity(fileInfo.getLogiPath() + "/" + fileInfo.getLogiNm(), fileInfo.getOrigNm());
	}

	@PostMapping(value = SELF_CHECK.VERIFICATION_MAKE_NOTICE_TEXT)
	public @ResponseBody ResponseEntity<Object>  makeNoticeText(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {
		
		String downloadId = null;
		ossNotice.setFileType("text");
		
		try {
			Project prjMasterInfo = selfCheckService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			
			if(!isEmpty(prjMasterInfo.getNoticeTextFileId())) {
				downloadId = prjMasterInfo.getNoticeTextFileId();
			} else {
				downloadId = selfCheckService.getNoticeTextFileForPreview(ossNotice);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}

	@PostMapping(value = SELF_CHECK.VERIFICATION_MAKE_NOTICE_SIMPLE)
	public @ResponseBody ResponseEntity<Object>  makeNoticeSimple(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {
		
		String downloadId = null;
		ossNotice.setFileType("html");
		ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES); // simple templete
		ossNotice.setDomain(CommonFunction.getDomain(req)); // domain Setting
		
		try {
			Project prjMasterInfo = selfCheckService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			
			if(!isEmpty(prjMasterInfo.getSimpleHtmlFileId())) {
				downloadId = prjMasterInfo.getSimpleHtmlFileId();
			} else {
				downloadId = selfCheckService.getNoticeTextFileForPreview(ossNotice);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}

	@PostMapping(value = SELF_CHECK.VERIFICATION_MAKE_NOTICE_TEXT_SIMPLE)
	public @ResponseBody ResponseEntity<Object>  makeNoticeTextSimple(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {
		
		String downloadId = null;
		ossNotice.setFileType("text");
		ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES); // simple templete
		
		try {
			Project prjMasterInfo = selfCheckService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			
			if(!isEmpty(prjMasterInfo.getSimpleTextFileId())) {
				downloadId = prjMasterInfo.getSimpleTextFileId();
			} else {
				downloadId = selfCheckService.getNoticeTextFileForPreview(ossNotice);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}

}
