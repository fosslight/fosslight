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
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.PARTNER;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SearchService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.YamlUtil;
import oss.fosslight.validation.T2CoValidationConfig;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;


@Controller
@Slf4j
public class PartnerController extends CoTopComponent{
	@Autowired PartnerService partnerService;
	@Autowired T2UserService userService;
	@Autowired CommentService commentService;
	@Autowired HistoryService historyService;
	@Autowired ProjectService projectService;
	@Autowired FileService fileService;
	
	@Autowired PartnerMapper partnerMapper;
	@Autowired FileMapper fileMapper;
	@Autowired ProjectMapper projectMapper;
	@Autowired SearchService searchService;
	
	/** The session key search. */
	private final String SESSION_KEY_SEARCH = "SESSION_KEY_PARTNER_LIST";
	
	/** The env. */
	@Resource private Environment env;
	
	/** The resource public upload excel path prefix. */
	private String RESOURCE_PUBLIC_UPLOAD_EXCEL_PATH_PREFIX;
	
	/** The resource public excel template path prefix. */
	private String RESOURCE_PUBLIC_EXCEL_TEMPLATE_PATH_PREFIX;
	
	/**
	 * Sets the resource path prefix.
	 */
	@PostConstruct
	public void setResourcePathPrefix(){
		RESOURCE_PUBLIC_UPLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("upload.path", "/upload");
		RESOURCE_PUBLIC_EXCEL_TEMPLATE_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	}
	
	@GetMapping(value=PARTNER.LIST, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res, Model model){
		T2Users param = new T2Users();
		param.setSortField("userName");
		param.setSortOrder("asc");
		model.addAttribute("creator", userService.getAllUsers(param));
		model.addAttribute("reviewer",userService.getReviwer());
		
		PartnerMaster searchBean = null;
		Object _param =  getSessionObject(CoConstDef.SESSION_KEY_PREFIX_DEFAULT_SEARCHVALUE + "OSSLISTMORE", true);
		
		if(_param != null) {
			String defaultSearchOssId = (String) _param;
			searchBean = new PartnerMaster();
			
			if(!isEmpty(defaultSearchOssId)) {
				deleteSession(SESSION_KEY_SEARCH);
				
				OssMaster ossBean = CoCodeManager.OSS_INFO_BY_ID.get(defaultSearchOssId);
				
				if(ossBean != null) {
					searchBean.setOssName(ossBean.getOssName());
					searchBean.setOssVersion(ossBean.getOssVersion());
				}
			}
		} else {
			if(!CoConstDef.FLAG_YES.equals(req.getParameter("gnbF"))) {
				deleteSession(SESSION_KEY_SEARCH);
				
				searchBean = searchService.getPartnerSearchFilter(loginUserName());
				if(searchBean == null) {
					searchBean = new PartnerMaster();
				}
			} else if(getSessionObject(SESSION_KEY_SEARCH) != null) {
				searchBean = (PartnerMaster) getSessionObject(SESSION_KEY_SEARCH);
			}	
		}
		
		model.addAttribute("searchBean", searchBean);
		
		return PARTNER.LIST_JSP;
		
		
	}
	
	@GetMapping(value=PARTNER.EDIT, produces = "text/html; charset=utf-8")
	public String edit(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("autoAnalysisFlag", CommonFunction.propertyFlagCheck("autoanalysis.use.flag", CoConstDef.FLAG_YES));
		
		return PARTNER.EDIT_JSP;
	}
	
	@GetMapping(value=PARTNER.EDIT_ID, produces = "text/html; charset=utf-8")
	public String edit(@PathVariable String partnerId, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		PartnerMaster partnerMaster = new PartnerMaster();
		partnerMaster.setPartnerId(partnerId);
		partnerMaster = partnerService.getPartnerMasterOne(partnerMaster);
		
		T2File confirmationFile = new T2File();
		confirmationFile.setFileSeq(partnerMaster.getConfirmationFileId());
		confirmationFile = fileMapper.getFileInfo(confirmationFile);
		
		partnerMaster.setViewOnlyFlag(partnerService.checkViewOnly(partnerId));
		
		T2File ossFile = new T2File();
		ossFile.setFileSeq(partnerMaster.getOssFileId());
		ossFile = fileMapper.getFileInfo(ossFile);
		
		String binaryFileId = partnerMaster.getBinaryFileId();
		T2File binaryFile = new T2File();
		
		if(!isEmpty(binaryFileId)){
			binaryFile.setFileSeq(binaryFileId);
			binaryFile = fileMapper.getFileInfo(binaryFile);
			
			model.addAttribute("binaryFile", binaryFile);
		}
		
		CommentsHistory comHisBean = new CommentsHistory();
		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_USER);
		comHisBean.setReferenceId(partnerMaster.getPartnerId());
		partnerMaster.setUserComment(commentService.getUserComment(comHisBean));
		partnerMaster.setDocumentsFile(partnerMapper.selectDocumentsFile(partnerMaster.getDocumentsFileId()));
		partnerMaster.setDocumentsFileCnt(partnerMapper.selectDocumentsFileCnt(partnerMaster.getDocumentsFileId()));
		
		List<Project> prjList = projectMapper.selectPartnerRefPrjList(partnerMaster);
		
		if(prjList.size() > 0) {
			model.addAttribute("prjList", toJson(prjList));
		}
		
		model.addAttribute("detail", partnerMaster);
		model.addAttribute("detailJson", toJson(partnerMaster));
		model.addAttribute("confirmationFile", confirmationFile);
		model.addAttribute("ossFile", ossFile);
		model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("checkFlag", CommonFunction.propertyFlagCheck("checkFlag", CoConstDef.FLAG_YES));
		model.addAttribute("autoAnalysisFlag", CommonFunction.propertyFlagCheck("autoanalysis.use.flag", CoConstDef.FLAG_YES));
		
		return PARTNER.EDIT_JSP;
	}
	
	
	@GetMapping(value=PARTNER.VIEW_ID, produces = "text/html; charset=utf-8")
	public String view(@PathVariable String partnerId, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		PartnerMaster partnerMaster = new PartnerMaster();
		partnerMaster.setPartnerId(partnerId);
		
		try {
			partnerMaster = partnerService.getPartnerMasterOne(partnerMaster);
			
			if(CoConstDef.FLAG_YES.equals(partnerMaster.getUseYn())) {
				T2File confirmationFile = new T2File();
				confirmationFile.setFileSeq(partnerMaster.getConfirmationFileId());
				confirmationFile = fileMapper.getFileInfo(confirmationFile);
				
				partnerMaster.setViewOnlyFlag(partnerService.checkViewOnly(partnerId));
				
				T2File ossFile = new T2File();
				ossFile.setFileSeq(partnerMaster.getOssFileId());
				ossFile = fileMapper.getFileInfo(ossFile);
				
				CommentsHistory comHisBean = new CommentsHistory();
				comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_USER);
				comHisBean.setReferenceId(partnerMaster.getPartnerId());
				partnerMaster.setUserComment(commentService.getUserComment(comHisBean));
				partnerMaster.setDocumentsFile(partnerMapper.selectDocumentsFile(partnerMaster.getDocumentsFileId()));
				partnerMaster.setDocumentsFileCnt(partnerMapper.selectDocumentsFileCnt(partnerMaster.getDocumentsFileId()));
				
				model.addAttribute("detail", partnerMaster);
				model.addAttribute("detailJson", toJson(partnerMaster));
				model.addAttribute("confirmationFile", confirmationFile);
				model.addAttribute("ossFile", ossFile);
				model.addAttribute("projectFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
				model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
				model.addAttribute("checkFlag", CommonFunction.propertyFlagCheck("checkFlag", CoConstDef.FLAG_YES));
				model.addAttribute("autoAnalysisFlag", CommonFunction.propertyFlagCheck("autoanalysis.use.flag", CoConstDef.FLAG_YES));
			} else {
				model.addAttribute("message", "Reqeusted URL is for a deleted 3rd Party Software. Please contact the creator or watcher of the 3rd Party Software.");
			}
		} catch (Exception e) {
			model.addAttribute("message", "Reqeusted URL contains 3rd Party Software ID that doesn't exist. Please check the 3rd Party Software ID again.");
		}
		
		return PARTNER.VIEW_JSP;
	}
	
	@GetMapping(value=PARTNER.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		partnerMaster.setCurPage(page);
		partnerMaster.setPageListSize(rows);
		partnerMaster.setSortField(sidx);
		partnerMaster.setSortOrder(sord);
		
		if(partnerMaster.getStatus() != null) {
			String statuses = partnerMaster.getStatus();
			if(!isEmpty(statuses)) {
				String[] arrStatuses = statuses.split(",");
				partnerMaster.setArrStatuses(arrStatuses);
			}
		}
		
		if("search".equals(req.getParameter("act"))){
			// 검색 조건 저장
			// save search condition
			putSessionObject(SESSION_KEY_SEARCH, partnerMaster);
		}else if(getSessionObject(SESSION_KEY_SEARCH) != null){
			partnerMaster = (PartnerMaster) getSessionObject(SESSION_KEY_SEARCH);
		}
		
		Map<String, Object> map = null;
		try{
			map = partnerService.getPartnerMasterList(partnerMaster);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(map);
	}
	
	@GetMapping(value=PARTNER.AUTOCOMPLETE_CONF_NM_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteConfAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		partnerMaster.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		partnerMaster.setStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
		
		return makeJsonResponseHeader(partnerService.getPartnerNameList(partnerMaster));
	}
	
	@GetMapping(value=PARTNER.AUTOCOMPLETE_NM_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		partnerMaster.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		
		return makeJsonResponseHeader(partnerService.getPartnerNameList(partnerMaster));
	}
	
	@GetMapping(value=PARTNER.AUTOCOMPLETE_CONF_SW_NM_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteConfSwNmAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		partnerMaster.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		partnerMaster.setStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
		
		return makeJsonResponseHeader(partnerService.getPartnerSwNmList(partnerMaster));
	}
	
	@GetMapping(value=PARTNER.AUTOCOMPLETE_SW_NM_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteSwNmAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		partnerMaster.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		
		return makeJsonResponseHeader(partnerService.getPartnerSwNmList(partnerMaster));
	}
	
	@GetMapping(value=PARTNER.USER_LIST)
	public @ResponseBody ResponseEntity<Object> getUserList(
			T2Users t2Users
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		List<T2Users> result = null;
		
		try{
			result = partnerService.getUserList(t2Users);	
		}catch(Exception e){
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(result);
	}
	
	@PostMapping(value = PARTNER.UPDATE_REVIEWER)
	public @ResponseBody ResponseEntity<Object> partnerMod(
			@RequestBody PartnerMaster vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		PartnerMaster orgPartnerMaster = null;
		
		if(!isEmpty(vo.getReviewer())) {
			orgPartnerMaster = partnerService.getPartnerMasterOne(vo);
		}
		
		int result = partnerService.updateReviewer(vo);
		
		try {
			History h = partnerService.work(vo);
			h.sethAction(CoConstDef.ACTION_CODE_UPDATE);				
			historyService.storeData(h);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			if(orgPartnerMaster != null) {
				if(orgPartnerMaster != null && !vo.getReviewer().equals(orgPartnerMaster.getReviewer())) {
					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED);
					mailBean.setParamPartnerId(vo.getPartnerId());
					mailBean.setToIds(new String[]{vo.getReviewer()});
					CoMailManager.getInstance().sendMail(mailBean);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader(result);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value=PARTNER.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(
			@RequestBody PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		// default validation
		boolean isValid = true;
		// last response map
		Map<String, String> resMap = new HashMap<>();
		// default 00:java error check code, 10:success code
		String resCd = "00";
		
		//3rd party name, software name, software version Duplication Check
		List<PartnerMaster> result = null;
		result = partnerService.getPartnerDuplication(partnerMaster);
		Map<String, String> dupMap = new HashMap<String, String>();
		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String, Object> ruleMap = T2CoValidationConfig.getInstance().getRuleAllMap();
		String msg = "";
	        
		if(result.size() > 0){

			if(!isEmpty(partnerMaster.getPartnerName()) && partnerMaster.getPartnerName().equals(result.get(0).getPartnerName())){
				msg = (String) ruleMap.get("PARTNER_NAME.DUPLICATED.MSG");
				dupMap.put("partnerName", msg);
			}

	        
			retMap.put("isValid", "false");
			retMap.put("resCd", "99"); // overlap flag
			retMap.put("dupData", dupMap);
			return makeJsonResponseHeader(retMap);
		}
				
		String mainGrid = partnerMaster.getOssComponentsStr();
		
		Type collectionType = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
		// ossVersion N/A => "" 치환 / 3rd Party > e2fsprogs를 Row에 추가 시 Save 불가
		// ossVersion N/A => "" replace / 3rd Party > cannot save when adding e2fsprogs to row
		ossComponents = CommonFunction.replaceOssVersionNA((List<ProjectIdentification>) fromJson(mainGrid, collectionType));
		ossComponents = CommonFunction.removeDuplicateLicense(ossComponents);
		List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
		
		ossComponentsLicense = CommonFunction.mergeGridAndSession(
				CommonFunction.makeSessionKey(loginUserName(),CoConstDef.CD_DTL_COMPONENT_PARTNER, partnerMaster.getPartnerId()), ossComponents, ossComponentsLicense,
				CommonFunction.makeSessionReportKey(loginUserName(),CoConstDef.CD_DTL_COMPONENT_PARTNER, partnerMaster.getPartnerId()));
		
		T2CoProjectValidator pv = new T2CoProjectValidator(); // validation proceeded with t2coProject
		pv.setIgnore("OSS_NAME");
		pv.setIgnore("OSS_VERSION");
		pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_PARTNER);
		pv.setValidLevel(pv.VALID_LEVEL_BASIC);
		// main grid
		pv.setAppendix("mainList", ossComponents);
		
		T2CoValidationResult vr = pv.validateObject(partnerMaster); 
		
		// return validator result
		if(!vr.isValid()) {
			return makeJsonResponseHeader(false,  CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
		}
		
		Boolean isNew = isEmpty(partnerMaster.getPartnerId());
		
		Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(ossComponents, ossComponentsLicense);
		ossComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
		ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
		
		try{
			History h = new History();
			
			if(!isNew) {
				h = partnerService.work(partnerMaster);
				partnerService.registPartnerMaster(partnerMaster, ossComponents, ossComponentsLicense);
				h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
			} else {
				partnerService.registPartnerMaster(partnerMaster, ossComponents, ossComponentsLicense);
				h = partnerService.work(partnerMaster);
				h.sethAction(CoConstDef.ACTION_CODE_INSERT);
			}
			
			historyService.storeData(h);
			resCd="10";
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		if("10".equals(resCd) && !isEmpty(partnerMaster.getPartnerId())) {
			String prjId = partnerMaster.getPartnerId();
			
			// send invate mail
			if(isNew) {
				List<String> partnerInvateWatcherList = partnerService.getInvateWatcherList(prjId);
				
				if(partnerInvateWatcherList != null && !partnerInvateWatcherList.isEmpty()) {
					for(String _email : partnerInvateWatcherList) {
						try {
							CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_INVATED);
							mailBean.setParamPartnerId(prjId);
							mailBean.setParamUserId(partnerMaster.getLoginUserName());
							mailBean.setParamEmail(_email);
							
							CoMailManager.getInstance().sendMail(mailBean);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
			
			// 분석 결과어 업로시 nickname 변경된 사항
			// In the case of nickname is changed when uploading to analysis result
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
			
			try {
				if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_PARTNER)) != null) {
					String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
							CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_PARTNER), true);
					if (!isEmpty(changedLicenseName)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_HIS);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setContents(changedLicenseName);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			try {
				if(getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, partnerMaster.getOssFileId())) != null) {
					String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, partnerMaster.getOssFileId()), true);
					
					if(!isEmpty(chagedOssVersion)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setContents(chagedOssVersion);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		if(!isEmpty(partnerMaster.getPartnerId())) {
			resMap.put("partnerId", partnerMaster.getPartnerId());
		}
		
		resMap.put("isValid", String.valueOf(isValid));
		resMap.put("resCd", resCd);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value=PARTNER.CHANGE_DIVISION_AJAX)
	public @ResponseBody ResponseEntity<Object> saveBasicInfoOnConfirmAjax(
			@RequestBody HashMap<String, Object> map
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		
		String partnerId = (String)map.get("partnerId");
		String division = (String)map.get("division");
		try {
			partnerService.updateDivision(partnerId, division);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader();
	}
	
	@PostMapping(value=PARTNER.DEL_AJAX)
	public @ResponseBody ResponseEntity<Object> delAjax(
			@ModelAttribute PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		HashMap<String, Object> resMap = new HashMap<>();
		String resCd = "00";
		
		try{
			History h = partnerService.work(partnerMaster);
			partnerService.deletePartnerMaster(partnerMaster);
			h.sethAction(CoConstDef.ACTION_CODE_DELETE);	
			historyService.storeData(h);
			
			resCd="10";
			
		} catch (Exception e){
			log.error(e.getMessage());
		}
		
		if("10".equals(resCd)) {
			try {
				CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_DELETED);
				mailBean.setParamPartnerId(partnerMaster.getPartnerId());
				if(!isEmpty(partnerMaster.getUserComment())) {
					mailBean.setComment(partnerMaster.getUserComment());
				}
				CoMailManager.getInstance().sendMail(mailBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		resMap.put("resCd", resCd);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@GetMapping(value=PARTNER.AUTOCOMPLETE_SW_VER_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteSwVerAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		partnerMaster.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		
		return makeJsonResponseHeader(partnerService.getPartnerSwVerList(partnerMaster));
	}
	
	@GetMapping(value=PARTNER.AUTOCOMPLETE_CONF_SW_VER_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteConfSwVerAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		partnerMaster.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		partnerMaster.setStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
		
		return makeJsonResponseHeader(partnerService.getPartnerSwVerList(partnerMaster));
	}
	
	@PostMapping(value = PARTNER.ADD_WATCHER)
	public @ResponseBody ResponseEntity<Object> addWatcher(@RequestBody PartnerMaster project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// addWatcher로 email을 등록할 경우 ldap search로 존재하는 사용자의 email인지 check가 필요함.
			String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
			if(CoConstDef.FLAG_YES.equals(ldapFlag) && !isEmpty(project.getParEmail())) {
				Map<String, String> userInfo = new HashMap<>();
				userInfo.put("USER_ID", CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_ID));
				userInfo.put("USER_PW", CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_PW));
				
				String userId = project.getParEmail().split("@")[0];
				String filter = "(cn=" + userId + ")";
				
				boolean isAuthenticated = userService.checkAdAccounts(userInfo, "USER_ID", "USER_PW", filter);
				
				if(!isAuthenticated) {
					throw new Exception("add Watcher Failure");
				}
				
				String email = (String) userInfo.get("EMAIL");
				project.setParEmail(email);

				// 사용자가 입력한 domain과 ldap search를 통해 확인된 domain이 다를 수 있기때문에 ldap search에서 확인된 domain을 우선적으로 처리함.
				resultMap.put("email", email);
			}
						
			if(!isEmpty(project.getParUserId()) || !isEmpty(project.getParEmail())) {
				partnerService.addWatcher(project);
				resultMap.put("isValid", "true");
			} else {
				return makeJsonResponseHeader(false, null);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader(resultMap);
	}
	
	@PostMapping(value = PARTNER.REMOVE_WATCHER)
	public @ResponseBody ResponseEntity<Object> removeWatcher(@RequestBody PartnerMaster project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		try {
			if(!isEmpty(project.getParUserId()) || !isEmpty(project.getParEmail())) {
				partnerService.removeWatcher(project);
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
	@PostMapping(value = PARTNER.COPY_WATCHER)
	public @ResponseBody ResponseEntity<Object> copyWatcher(@RequestBody PartnerMaster project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
			HashMap<String, Object> resMap = new HashMap<>();
		try {			
			if(!isEmpty(project.getListKind()) && !isEmpty(project.getListId()) ) {
				
				List<PartnerMaster> result = partnerService.copyWatcher(project);
				
				if(result != null) {

					for(PartnerMaster pm : result) {
						if(!StringUtils.isEmpty(pm.getDivision())) {
							pm.setParDivision(pm.getDivision());
							pm.setParDivisionName(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, pm.getDivision()));
						}
					}
					
					if(!isEmpty(project.getPartnerId())) {
						boolean existPartnerWatcher = partnerService.existsWatcher(project);
						
						for(PartnerMaster pm : result) {
							pm.setPartnerId(project.getPartnerId());
							
							if(existPartnerWatcher)
								partnerService.addWatcher(pm);
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
	
	/**
	 * update the publicYn.
	 *
	 * @param PartnerMaster the PartnerMaster
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PARTNER.UPDATE_PUBLIC_YN)
	public @ResponseBody ResponseEntity<Object> updatePublicYn(@RequestBody PartnerMaster partner,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		try {
			partnerService.updatePublicYn(partner);
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Change status.
	 *
	 * @param partnerMaster the partner master
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value=PARTNER.CHANGE_STATUS)
	public @ResponseBody ResponseEntity<Object> changeStatus(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		CoMail mailbean = null;
		
		String commentDiv = CoConstDef.CD_DTL_COMMENT_PARTNER_HIS;
		String userComment = partnerMaster.getUserComment();
		String statusCode = partnerMaster.getStatus();
		String status = CoCodeManager.getCodeExpString(CoConstDef.CD_IDENTIFICATION_STATUS, statusCode);
		
		if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(partnerMaster.getStatus())) {
			ProjectIdentification _param = new ProjectIdentification();
			_param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
			_param.setReferenceId(partnerMaster.getPartnerId());
			Map<String, Object> map = projectService.getIdentificationGridList(_param);
			
			T2CoProjectValidator pv = new T2CoProjectValidator();
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_PARTNER);

			// main grid
			pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
			//pv.setCheckForAdmin(true);
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			// return validator result
			if(!vr.isValid()) {
				return makeJsonResponseHeader(vr.getValidMessageMap());
			}
			
			partnerService.updatePartnerConfirm(partnerMaster);
			
			try {
				mailbean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_CONF);
				mailbean.setParamPartnerId(partnerMaster.getPartnerId());

				String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PARTER_CONF));

				if(!isEmpty(userComment)) {
					userComment = avoidNull(userComment) + "<br />" + _tempComment;
					mailbean.setComment(userComment);
				} else{
					mailbean.setComment(_tempComment);
				}
				
				CoMailManager.getInstance().sendMail(mailbean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(partnerMaster.getStatus())) {
				ProjectIdentification _param = new ProjectIdentification();
				_param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
				_param.setReferenceId(partnerMaster.getPartnerId());
				
				Map<String, Object> map = projectService.getIdentificationGridList(_param);
				
				T2CoProjectValidator pv = new T2CoProjectValidator();
				pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_PARTNER);
				pv.setValidLevel(pv.VALID_LEVEL_REQUEST);
				// main grid
				pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
				// sub grid
				pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
				
				T2CoValidationResult vr = pv.validate(new HashMap<>());
				
				// return validator result
				if(!vr.isValid()) {
					if (!vr.isDiff()) {
						return makeJsonResponseHeader(false, "", "", vr.getValidMessageMap(), vr.getDiffMessageMap());
					} else {
						return makeJsonResponseHeader(false, "", "", vr.getValidMessageMap());
					}
				}
			}
			
			PartnerMaster _param2 = new PartnerMaster();
			_param2.setPartnerId(partnerMaster.getPartnerId());
			PartnerMaster orgInfo = partnerService.getPartnerMasterOne(_param2);
			
			partnerService.changeStatus(partnerMaster);
			
			try {
				if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(partnerMaster.getStatus())) {
					mailbean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_REQ_REVIEW);
				} else if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS.equals(partnerMaster.getStatus())) {
					if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(orgInfo.getStatus())) {
						// confirm -> reject
						mailbean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_CANCELED_CONF);
					} else if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(orgInfo.getStatus())) {
						// review -> reject
						mailbean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_REJECT);
					} else if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(orgInfo.getStatus())) {
						// self reject
						mailbean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_SELF_REJECT);
					}
				}
				
				if(mailbean != null) {
					mailbean.setParamPartnerId(partnerMaster.getPartnerId());
					
					if(!isEmpty(userComment)) {
						mailbean.setComment(userComment);
					}
					
					CoMailManager.getInstance().sendMail(mailbean);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		if (!isEmpty(avoidNull(userComment).trim())) {
			try {
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(commentDiv);
				commHisBean.setReferenceId(partnerMaster.getPartnerId());
				commHisBean.setContents(userComment);
				commHisBean.setStatus(status);
				commentService.registComment(commHisBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else if(!isEmpty(status)) {
			try {
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(commentDiv);
				commHisBean.setReferenceId(partnerMaster.getPartnerId());
				commHisBean.setContents(userComment);
				commHisBean.setStatus(status);
				commentService.registComment(commHisBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return makeJsonResponseHeader(null);
	}
	
	/**
	 * Oss file.
	 *
	 * @param file the file
	 * @param req the req
	 * @param request the request
	 * @param res the res
	 * @param model the model
	 * @return the string
	 * @throws Exception the exception
	 */
	@ResponseBody
	@PostMapping(value=PARTNER.OSS_FILE)
	public String ossFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request, HttpServletResponse res, Model model) throws Exception{
		String excel = req.getParameter("excel");
		List<UploadFile> list = new ArrayList<UploadFile>();
		ArrayList<Object> resultList = new ArrayList<Object>();

		Map<String, MultipartFile> fileMap = req.getFileMap();
		String fileExtension = StringUtils.getFilenameExtension(fileMap.get("myfile").getOriginalFilename());
		
		if (req.getContentType() != null && req.getContentType().toLowerCase().indexOf("multipart/form-data") > -1 ) {
			file.setCreator(loginUserName());
			list = fileService.uploadFile(req, file);

			if(fileExtension.equals("csv")) {
				resultList = CommonFunction.checkCsvFileLimit(list);
			} else {
				resultList = CommonFunction.checkXlsxFileLimit(list);
			}
			
			if(resultList.size() > 0) {
				return toJson(resultList);
			}
		}

		if(fileExtension.equals("csv")) {
			resultList.add(list);
			resultList.add("SRC");
			resultList.add("CSV_FILE");

			return toJson(resultList);
		} else {
			// sheet name
			List<Object> sheetNameList = null;
			Boolean isSpdxSpreadsheet = false;
			
			try {
				if(CoConstDef.FLAG_YES.equals(excel)){
					if(list != null && !list.isEmpty() && CoCodeManager.getCodeExpString(CoConstDef.CD_FILE_ACCEPT, "22").contains(list.get(0).getFileExt())) {

						sheetNameList = ExcelUtil.getSheetNames(list, RESOURCE_PUBLIC_UPLOAD_EXCEL_PATH_PREFIX);
					}
				}
			
				for(Object sheet : sheetNameList) {
					String sheetName = sheet.toString();
					if(sheetName.contains("Package Info") || sheetName.contains("Per File Info")) {
						isSpdxSpreadsheet = true;
					}
				}
			} catch(Exception e) {
				log.error(e.getMessage(), e);
			}

			if(isSpdxSpreadsheet){
				resultList.add(list);
				resultList.add(sheetNameList);
				resultList.add("SPDX_SPREADSHEET_FILE");
			}
			else {
				resultList.add(list);
				resultList.add(sheetNameList);
				resultList.add("EXCEL_FILE");
			}

			return toJson(resultList);
		}
	}
	
	/**
	 * documents File
	 *
	 * @param file the file
	 * @param req the req
	 * @param request the request
	 * @param res the res
	 * @param model the model
	 * @return the string
	 * @throws Exception the exception
	 */
	@ResponseBody
	@PostMapping(value = PARTNER.DOCUMENT_FILE)
	public String documentsFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {
		// file registration
		List<UploadFile> list = new ArrayList<UploadFile>();
		String fileId = req.getParameter("registFileId");
		
		// file registration
		try {
			if (req.getContentType() != null
					&& req.getContentType().toLowerCase().indexOf("multipart/form-data") > -1) {
				file.setCreator(loginUserName());
				if (fileId == null) {
					list = fileService.uploadFile(req, file);
				} else {
					list = fileService.uploadFile(req, file, null, fileId);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		// 결과값 resultList에 담기
		// Put the result value in resultList
		return toJson(list);
	}
	
	/**
	 * Send comment.
	 *
	 * @param commentsHistory the comments history
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value=PARTNER.SEND_COMMENT)
	public @ResponseBody ResponseEntity<Object> sendComment(
			@ModelAttribute CommentsHistory commentsHistory
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PARTER_ADDED_COMMENT);
		commentService.registComment(commentsHistory);
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Save comment.
	 *
	 * @param commentsHistory the comments history
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value=PARTNER.SAVE_COMMENT)
	public @ResponseBody ResponseEntity<Object> saveComment(
			@ModelAttribute CommentsHistory commentsHistory
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		commentService.registComment(commentsHistory);
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Delete comment.
	 *
	 * @param commentsHistory the comments history
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value=PARTNER.DELETE_COMMENT)
	public @ResponseBody ResponseEntity<Object> deleteComment(
			@ModelAttribute CommentsHistory commentsHistory
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		T2CoValidationResult vResult = null;
		
		try{
			//validation check
			 vResult = validate(req);	
		}catch(Exception e){
			log.error(e.getMessage());
		}
		
		if(!vResult.isValid()){
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		try{
			commentService.deleteComment(commentsHistory);
		} catch (Exception e){
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(vResult.getValidMessageMap());
	}
	
	/**
	 * Gets the comment list.
	 *
	 * @param commentsHistory the comments history
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the comment list
	 */
	@GetMapping(value=PARTNER.COMMENT_LIST)
	public @ResponseBody ResponseEntity<Object> getCommentList(
			CommentsHistory commentsHistory
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		commentsHistory.setReferenceDiv("20");
		List<CommentsHistory> result = null;
		
		try{
			 result = commentService.getCommentList(commentsHistory);	
		}catch(Exception e){
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(result);
	}
	
	/**
	 * Sample download.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 * @throws Exception the exception
	 */
	@ResponseBody
	@GetMapping(value = PARTNER.SAMPLEDOWNLOAD)
	public ResponseEntity<FileSystemResource> sampleDownload (
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		String fileName = req.getParameter("fileName");
		String logiPath = req.getParameter("logiPath");
				
		return excelToResponseEntity(RESOURCE_PUBLIC_EXCEL_TEMPLATE_PATH_PREFIX + logiPath, fileName);
	}
	
	
	@PostMapping(value = PARTNER.FILTERED_LIST)
	public @ResponseBody ResponseEntity<Object> getfilteredList(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		
		Map<String, Object> resultMap = partnerService.getPartnerValidationList(partnerMaster);
		
		try{
			resultMap = partnerService.getFilterdList(resultMap);
			
			return makeJsonResponseHeader(resultMap);
		}catch(Exception e){
			return makeJsonResponseHeader(false, "filterList Error");
		}
	}
	
	@GetMapping(value=PARTNER.CHECK_STATUS)
	public @ResponseBody ResponseEntity<Object> checkStatus(@PathVariable String partnerId, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		PartnerMaster partnerMaster = new PartnerMaster();
		partnerMaster.setPartnerId(partnerId);
		partnerMaster = partnerService.getPartnerMasterOne(partnerMaster);
		
		resultMap.put("status", partnerMaster.getStatus());
		
		return makeJsonResponseHeader(resultMap);
	}
	
	
	@PostMapping(value=PARTNER.MAKE_YAML)
	public @ResponseBody ResponseEntity<Object> makeYaml(
			@RequestBody PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		String yamlFileId = "";
		
		try {
			// partnerId / partnerName 필수 값.
			yamlFileId = YamlUtil.makeYaml(CoConstDef.CD_DTL_COMPONENT_PARTNER, toJson(partnerMaster));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
		
		return makeJsonResponseHeader(yamlFileId);
	}
	
	@PostMapping(value=PARTNER.PARTNER_DIVISION)
	public @ResponseBody ResponseEntity<Object> updatePartnerDivision(
			@RequestBody PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		List<String> permissionCheckList = null;
		
		if(!CommonFunction.isAdmin()) {
			CommonFunction.setPartnerService(partnerService);
			permissionCheckList = CommonFunction.checkUserPermissions(loginUserName(), partnerMaster.getPartnerIds(), "partner");
		}
		
		if(permissionCheckList == null || permissionCheckList.isEmpty()){
			Map<String, List<PartnerMaster>> updatePartnerDivision = partnerService.updatePartnerDivision(partnerMaster);	
			
			if(updatePartnerDivision.containsKey("before") && updatePartnerDivision.containsKey("after")) {
				List<PartnerMaster> beforePartnerList = (List<PartnerMaster>) updatePartnerDivision.get("before");
				List<PartnerMaster> afterPartnerList = (List<PartnerMaster>) updatePartnerDivision.get("after");
				
				if((beforePartnerList != null && !beforePartnerList.isEmpty()) 
						&& (afterPartnerList != null && !afterPartnerList.isEmpty())
						&& beforePartnerList.size() == afterPartnerList.size()) {
					
					for(int i=0; i<beforePartnerList.size(); i++) {
						try {
							CommentsHistory commentsHistory = new CommentsHistory();
							commentsHistory.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
							commentsHistory.setReferenceId(afterPartnerList.get(i).getPartnerId());
							commentsHistory.setContents(afterPartnerList.get(i).getUserComment());
							
							commentService.registComment(commentsHistory, false);
						} catch(Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
		} else {
			return makeJsonResponseHeader(false, null, permissionCheckList);
		}
		
		return makeJsonResponseHeader();
	}

	@PostMapping(value = PARTNER.UPDATE_DESCRIPTION)
	public @ResponseBody ResponseEntity<Object> updateDescription(
			@RequestBody PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		try {
			partnerService.updateDescription(partnerMaster);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return makeJsonResponseHeader();
	}
}
