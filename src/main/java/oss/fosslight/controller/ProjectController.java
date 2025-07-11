/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.PROJECT;
import oss.fosslight.common.CustomXssFilter;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.service.BinaryDataService;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SearchService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.service.VerificationService;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.OssComponentUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.util.YamlUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Controller
@Slf4j
public class ProjectController extends CoTopComponent {
	/** The file service. */
	@Autowired FileService fileService;
	
	/** The partner service. */
	@Autowired PartnerService partnerService;
	
	/** The project service. */
	@Autowired ProjectService projectService;
	
	/** The user service. */
	@Autowired T2UserService userService;
	
	/** The comment service. */
	@Autowired CommentService commentService;
	
	/** The history service. */
	@Autowired HistoryService historyService;
	
	@Autowired VerificationService verificationService;
	
	@Autowired CodeMapper codeMapper;
	
	@Autowired ResponseService responseService;
	@Autowired SearchService searchService;
	
	@Autowired private BinaryDataService binaryDataService;
	
	/** The env. */
	@Resource
	private Environment env;
	
	/** The session key search. */
	private final String SESSION_KEY_SEARCH = "SESSION_KEY_PROJECT_LIST";
	
	/**
	 * [화면] 프로젝트 목록 조회.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the string
	 */
	@GetMapping(value = PROJECT.LIST, produces = "text/html; charset=utf-8")
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) {
		T2Users param = new T2Users();
		param.setSortField("userName");
		param.setSortOrder("asc");
		// 여기까지 고정

		// 전체 사용자 리스트
		model.addAttribute("creator", userService.getAllUsers(param));
		model.addAttribute("reviewer", userService.getReviwer());
		
		Project searchBean = null;
		Object _param =  getSessionObject(CoConstDef.SESSION_KEY_PREFIX_DEFAULT_SEARCHVALUE + "OSSLISTMORE", true);
		Object _param2 =  getSessionObject(CoConstDef.SESSION_KEY_PREFIX_DEFAULT_SEARCHVALUE + "PARTNERLISTMORE", true);
		
		if (_param != null) {
			String defaultSearchOssId = (String) _param;
			searchBean = new Project();
			
			if (!isEmpty(defaultSearchOssId)) {
				deleteSession(SESSION_KEY_SEARCH);
				OssMaster ossBean = CoCodeManager.OSS_INFO_BY_ID.get(defaultSearchOssId);
				
				if (ossBean != null) {
					searchBean.setOssName(ossBean.getOssName());
					searchBean.setOssVersion(ossBean.getOssVersion());
				}
			}
		} else if (_param2 != null) {
			String defaultSearchRefPartnerName = (String) _param2;
			searchBean = new Project();
			
			if (!isEmpty(defaultSearchRefPartnerName)) {
				deleteSession(SESSION_KEY_SEARCH);
				
				searchBean.setRefPartnerName(defaultSearchRefPartnerName);
			}
		} else {
			if (!CoConstDef.FLAG_YES.equals(req.getParameter("gnbF")) || getSessionObject(SESSION_KEY_SEARCH) == null) {
				deleteSession(SESSION_KEY_SEARCH);
				
				searchBean = searchService.getProjectSearchFilter(loginUserName());
				if (searchBean == null) {
					searchBean = new Project();
				}
			} else if (getSessionObject(SESSION_KEY_SEARCH) != null) {
				searchBean = (Project) getSessionObject(SESSION_KEY_SEARCH);
			}
		}

		model.addAttribute("searchBean", searchBean);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		
		return "project/list";
	}
	
	/**
	 * Auto complete ajax.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.AUTOCOMPLETE_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteAjax(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		project.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		List<Map<String, String>> list = projectService.getProjectNameList(project);
		CustomXssFilter.nameFilter(list);
		return makeJsonResponseHeader(list);
	}
	
	@GetMapping(value = PROJECT.AUTOCOMPLETE_ID_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteIdAjax(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		project.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		List<Project> list = projectService.getProjectIdList(project);
		return makeJsonResponseHeader(list);
	}
	
	/**
	 * Auto complete version ajax.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.AUTOCOMPLETE_VERSION_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteVersionAjax(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		project.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());
		
		return makeJsonResponseHeader(projectService.getProjectVersionList(project));
	}
	
	/**
	 * Auto complete Division ajax.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.AUTOCOMPLETE_DIVISION_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteDivisonAjax(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		project.setCreator(CommonFunction.isAdmin() ? "ADMIN" : loginUserName());

		return makeJsonResponseHeader(projectService.getProjectDivisionList(project));
	}
	/**
	 * Auto complete model ajax.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.AUTOCOMPLETE_MODEL_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteModelAjax(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		return makeJsonResponseHeader(projectService.getProjectModelNameList());
	}
	
	/**
	 * Gets the user id list.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the user id list
	 */
	@GetMapping(value = PROJECT.USER_ID_LIST)
	public @ResponseBody String getUserIdList(HttpServletRequest req, HttpServletResponse res, Model model) {
		String reviewerFlag = req.getParameter("reviewerFlag");
		String userIdList = "";
		
		if (CoConstDef.FLAG_YES.equals(reviewerFlag)) {
			userIdList = projectService.getReviewerList(CoConstDef.FLAG_YES);
		} else {
			userIdList = projectService.getAdminUserList();
		}
		
		return userIdList;
	}
	
	/**
	 * [API] 프로젝트 목록 조회.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.LIST_AJAX)
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

		if (project.getStatuses() != null) {
			String statuses = project.getStatuses();
			if (!isEmpty(statuses)){
				String[] arrStatuses = statuses.split(",");
				project.setArrStatuses(arrStatuses);
			}
		}
		
		project.setPublicYn(isEmpty(project.getPublicYn())?CoConstDef.FLAG_YES:project.getPublicYn());
		
		if ("search".equals(req.getParameter("act"))) {
			// 검색 조건 저장
			putSessionObject(SESSION_KEY_SEARCH, project);
		} else if (getSessionObject(SESSION_KEY_SEARCH) != null) {
			project = (Project) getSessionObject(SESSION_KEY_SEARCH);
		}

		Map<String, Object> map = projectService.getProjectList(project);

		@SuppressWarnings("unchecked")
		List<Project> list = (List<Project>) map.get("rows");
		
		CustomXssFilter.projectFilter(list);
		return makeJsonResponseHeader(map);
	}
	
	/**
	 * [화면] 프로젝트 상세.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = { PROJECT.EDIT }, method = RequestMethod.GET, produces = "text/html; charset=utf-8")
	public String edit(HttpServletRequest req, HttpServletResponse res, Model model) {
		Project project = new Project();
//		project.setNoticeType(CoConstDef.CD_GENERAL_MODEL);
		project.setPriority(CoConstDef.CD_PRIORITY_P2);
		
		Object _param =  getSessionObject(CoConstDef.SESSION_KEY_PREFIX_DEFAULT_SEARCHVALUE + "PARTNER", true);
		
		if (_param != null) {
			String partnerKey = (String) _param;
			
			if (!isEmpty(partnerKey)) {
				deleteSession(SESSION_KEY_SEARCH);
				
				String[] partnerArr = partnerKey.split("\\|\\|");
				String refPartnerId = partnerArr[0];
				String partnerName = partnerArr[1];
				String softwareName = partnerArr[2];
				
				project.setRefPartnerId(refPartnerId); // refPartnerId
				
				String comment = "Copied from [3rd-" + refPartnerId + "] " + partnerName + " (" + softwareName.replace("[]", "/") + ")";
				project.setComment(comment);
				model.addAttribute("createThird", comment);
			}
		}
		
		model.addAttribute("project", project);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		
		return "project/edit";
	}
	
	/**
	 * [API] 프로젝트 상세 조회.
	 *
	 * @param prjId the prj id
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = { PROJECT.EDIT_ID }, method = { RequestMethod.GET, RequestMethod.POST }, produces = "text/html; charset=utf-8")
	public String edit(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) {
		Project project = new Project();
		project.setPrjId(prjId);
		project.setActType(CoConstDef.FLAG_NO);
		project = projectService.getProjectDetail(project);
		
		CommentsHistory comHisBean = new CommentsHistory();
		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_USER);
		comHisBean.setReferenceId(project.getPrjId());
		
		project.setUserComment(commentService.getUserComment(comHisBean));

		model.addAttribute("project", project);
		model.addAttribute("detail", project);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		
		List<String> permissionCheckList = null;
		boolean permissionFlag = false;
		
		if (!CommonFunction.isAdmin()) {
			project.setPrjIds(new String[] {prjId});
			permissionCheckList = CommonFunction.checkUserPermissions("", project.getPrjIds(), "project");
			if (permissionCheckList.contains(loginUserName())) {
				permissionFlag = true;
			}

		}
		
		if (project.getPublicYn().equals(CoConstDef.FLAG_NO)
				&& !CommonFunction.isAdmin()
				&& !permissionFlag) {
			model.addAttribute("projectPermission", CoConstDef.FLAG_NO);
			
			return "project/view";
		} else {
			if (!CommonFunction.isAdmin() && !permissionFlag) {
				List<T2Users> userList = userService.selectAllUsers();
				
				if (userList != null) {
					model.addAttribute("userWithDivisionList", userList);
				}
				
				return "project/view";
			} else {
				return "project/edit";
			}
		}
	}
	
	@RequestMapping(value = { PROJECT.EDIT_DIV_ID }, method = { RequestMethod.GET, RequestMethod.POST }, produces = "text/html; charset=utf-8")
	public String editDiv(@PathVariable String prjId, @PathVariable String initDiv, HttpServletRequest req, HttpServletResponse res, Model model) {
		Project project = new Project();
		project.setPrjId(prjId);
		project.setActType(CoConstDef.FLAG_NO);
		project = projectService.getProjectDetail(project);
		
		CommentsHistory comHisBean = new CommentsHistory();
		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_USER);
		comHisBean.setReferenceId(project.getPrjId());
		
		project.setUserComment(commentService.getUserComment(comHisBean));

		model.addAttribute("project", project);
		model.addAttribute("detail", project);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("initDiv", initDiv);
		
		List<String> permissionCheckList = null;
		boolean permissionFlag = false;
		
		if (!CommonFunction.isAdmin()) {
			project.setPrjIds(new String[] {prjId});
			permissionCheckList = CommonFunction.checkUserPermissions("", project.getPrjIds(), "project");
			if (permissionCheckList.contains(loginUserName())) {
				permissionFlag = true;
			}

		}
		
		if (project.getPublicYn().equals(CoConstDef.FLAG_NO)
				&& !CommonFunction.isAdmin()
				&& !permissionFlag) {
			model.addAttribute("projectPermission", CoConstDef.FLAG_NO);
			
			return "project/view";
		} else {
			if (!CommonFunction.isAdmin() && !permissionFlag) {
				List<T2Users> userList = userService.selectAllUsers();
				
				if (userList != null) {
					model.addAttribute("userWithDivisionList", userList);
				}
				
				return "project/view";
			} else {
				return "project/edit";
			}
		}
	}
	
	@RequestMapping(value = { PROJECT.VIEW_ID }, method = { RequestMethod.GET, RequestMethod.POST }, produces = "text/html; charset=utf-8")
	public String view(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) throws IOException {
		Project project = new Project();
		project.setPrjId(prjId);
		
		try {
			project = projectService.getProjectDetail(project);
			
			if (CoConstDef.FLAG_YES.equals(project.getUseYn())) {
				CommentsHistory comHisBean = new CommentsHistory();
				comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_USER);
				comHisBean.setReferenceId(prjId);
				
				project.setUserComment(commentService.getUserComment(comHisBean));
				
				model.addAttribute("project", project);
				model.addAttribute("detail", toJson(project));
				model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
				model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
				model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
				
				if (CommonFunction.isAdmin()) {
					List<T2Users> userList = userService.selectAllUsers();
					
					if (userList != null) {
						model.addAttribute("userWithDivisionList", userList);
					}
				}
			} else {
				model.addAttribute("message", "Reqeusted URL is for a deleted Project. Please contact the creator or watcher of the project.");
			}
		} catch (Exception e) {
			model.addAttribute("message", "Reqeusted URL contains Project ID that doesn't exist. Please check the Project ID again.");
		}
		
		return "project/view";
	}
	
	/**
	 * [API] Identification 공통 메인 조회.
	 * Identification 각 Tab에 저장되어있는 SRC / BIN / BIN(Android) / BOM 정보를 반환한다.
	 *
	 * @param identification the identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @param prjId the prj id
	 * @param code the code
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.IDENTIFICATION_GRID_ID_CD)
	public @ResponseBody ResponseEntity<Object> srcMainGridAjax(@ModelAttribute ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId,
			@PathVariable String code) {
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(code)) {
			String merge = req.getParameter("merge");
			identification.setMerge(merge);
		}
		
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PARTNER,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_DEP,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BAT,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_BAT,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), identification.getReferenceDiv(),
				identification.getReferenceId()));
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			result = getOssComponentDataInfo(identification, code);	
		} catch (Exception e) {
			log.error(e.getMessage(), e); // Identification에서 data를 호출할 때 문제가 발생한다면 error는 여기서 전부 모이게 됨.
		}
		
		if (result != null) {
			CommonFunction.setDeduplicatedMessageInfo(result);
			
			if (CoConstDef.CD_DTL_COMPONENT_BAT.equals(code) && isEmpty(identification.getReferenceId())
					&& !isEmpty(identification.getRefBatId())) {
				code = CoConstDef.CD_DTL_COMPONENT_ID_BAT;
			}
			
			putSessionObject(CommonFunction.makeSessionKey(loginUserName(), code, prjId), result);
		}
		
		return makeJsonResponseHeader(result);
	}
	
	/**
	 * Gets the oss component data info.
	 *
	 * @param identification the identification
	 * @param code the code
	 * @return the oss component data info
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getOssComponentDataInfo(ProjectIdentification identification, String code) {

		if (isEmpty(identification.getReferenceDiv())) {
			identification.setReferenceDiv(code);
		}

		if (!isEmpty(identification.getMainData())) {
			Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {
			}.getType();
			identification.setMainDataGridList(
					(List<ProjectIdentification>) fromJson(identification.getMainData(), collectionType2));
		}

		if (!isEmpty(identification.getSubData())) {
			Type collectionType3 = new TypeToken<List<List<ProjectIdentification>>>() {
			}.getType();
			identification.setSubDataGridList(
					(List<List<ProjectIdentification>>) fromJson(identification.getSubData(), collectionType3));
		}

		// bat apply 여부 flag
		boolean isBatResult = !isEmpty(identification.getRefBatId());
		boolean isSortOnBom = false;
		
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(code)) {
			if (!isEmpty(identification.getSidxOrg())) {
				String[] _sortIdxs = identification.getSidxOrg().split(",");
				
				if (_sortIdxs.length == 2) {
					if (!isEmpty(_sortIdxs[1].trim())) {
						isSortOnBom = true;
					}
					
					identification.setSortField(_sortIdxs[1].trim());
					identification.setSortOrder(identification.getSord());
				}
			}
			
			String filterCondition = CommonFunction.getFilterToString(identification.getFilters());
			
			if (!isEmpty(filterCondition)) {
				identification.setFilterCondition(filterCondition);
			}
		}
		
		Map<String, Object> map = projectService.getIdentificationGridList(identification, true);

		T2CoProjectValidator pv = new T2CoProjectValidator();
		
		if ((CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(code) || CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(code) || CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(code)) && map != null) {
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
			
			if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(code)) {
				pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BIN);
			}
			
			pv.setAppendix("projectId", avoidNull(identification.getReferenceId()));
			
			pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				map.replace("mainData", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) map.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false));
				
				if (!vr.isValid()) {
					map.put("validData", vr.getValidMessageMap());
				}
				
				if (!vr.isDiff()) {
					map.put("diffData", vr.getDiffMessageMap(true));
				}
				
				if (vr.hasInfo()) {
					map.put("infoData", vr.getInfoMessageMap());
				}
			}
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(code) && map != null) {
			List<String> noticeBinaryList = null;
			List<String> existsBinaryName = null;
			
			if (!isEmpty(identification.getReferenceId())) {
				// 다른 project에서 load하는 경우
				if (CoConstDef.FLAG_YES.equals(identification.getLoadFromAndroidProjectFlag())) {
					if (!isEmpty(identification.getAndroidNoticeFileId())) {
						log.info("identification.getAndroidNoticeFileId() : OK");
						noticeBinaryList = CommonFunction.getNoticeBinaryList(fileService.selectFileInfoById(identification.getAndroidNoticeFileId()));
					}
					
					if (!isEmpty(identification.getAndroidResultFileId())) {
						List<String> removedCheckList = null;
						List<OssComponents> addCheckList = null;
						log.info("identification.getAndroidResultFileId() : OK");
						existsBinaryName = CommonFunction.getExistsBinaryNames(
								fileService.selectFileInfoById(identification.getAndroidResultFileId()));

						List<String> _checkExistsBinaryName = new ArrayList<>();
						List<ProjectIdentification> _list = (List<ProjectIdentification>) map.get("mainData");
						
						for (ProjectIdentification bean : _list) {
							if (!isEmpty(bean.getBinaryName())) {
								_checkExistsBinaryName.add(bean.getBinaryName());
							}
						}
						
						T2File resultFileInfo = fileService.selectFileInfoById(identification.getAndroidResultFileId());
						Map<String, Object> _resultFileInfoMap = CommonFunction.getAndroidResultFileInfo(resultFileInfo,
								_checkExistsBinaryName);
						
						if (_resultFileInfoMap.containsKey("removedCheckList")) {
							removedCheckList = (List<String>) _resultFileInfoMap.get("removedCheckList");
						}
						
						if (_resultFileInfoMap.containsKey("addCheckList")) {
							addCheckList = (List<OssComponents>) _resultFileInfoMap.get("addCheckList");
						}

						if (removedCheckList != null) {
							for (ProjectIdentification bean : _list) {
								if (removedCheckList.contains(bean.getBinaryName())) {
									bean.setExcludeYn(CoConstDef.FLAG_YES);
								}
							}
						}

						if (addCheckList != null) {
							// ossComponent에서 ProjectIdentification으로 변환
							try {
								OssComponentUtil.getInstance().makeOssComponent(addCheckList, true);
							} catch (IllegalAccessException | InstantiationException | InvocationTargetException
									| NoSuchMethodException e) {
								log.error(e.getMessage());
							}

							for (OssComponents bean : addCheckList) {
								ProjectIdentification _tempBean = new ProjectIdentification();
								_tempBean.setBinaryName(bean.getBinaryName());
								_tempBean.setFilePath(bean.getFilePath());
								_tempBean.setBinaryNotice(bean.getBinaryNotice());
								_tempBean.setOssName(bean.getOssName());
								_tempBean.setOssVersion(bean.getOssVersion());
								_tempBean.setLicenseName(bean.getLicenseName());
								_tempBean.setDownloadLocation(bean.getDownloadLocation());
								_tempBean.setHomepage(bean.getHomepage());
								_tempBean.setCopyrightText(bean.getCopyrightText());
								_tempBean.setExcludeYn(bean.getExcludeYn());
								_tempBean.setEditable(CoConstDef.FLAG_YES);

								_list.add(_tempBean);
							}
						}

						map.replace("mainData", _list);
					}
				} else {
					Project prjInfo = projectService.getProjectBasicInfo(identification.getReferenceId());
					
					if (prjInfo != null) {
						if (!isEmpty(prjInfo.getSrcAndroidNoticeXmlId())) {
							noticeBinaryList = CommonFunction.getNoticeBinaryList(fileService.selectFileInfoById(prjInfo.getSrcAndroidNoticeXmlId()));
						}
						
						if (isEmpty(prjInfo.getSrcAndroidNoticeXmlId()) && !isEmpty(prjInfo.getSrcAndroidNoticeFileId())) {
							noticeBinaryList = CommonFunction.getNoticeBinaryList(fileService.selectFileInfoById(prjInfo.getSrcAndroidNoticeFileId()));
						}

						if (!isEmpty(prjInfo.getSrcAndroidResultFileId())) {
							existsBinaryName = CommonFunction.getExistsBinaryNames(fileService.selectFileInfoById(prjInfo.getSrcAndroidResultFileId()));
						}
					}
				}
			}

			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);
			pv.setAppendix("projectId", avoidNull(identification.getReferenceId()));
			pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));

			if (noticeBinaryList != null) {
				pv.setAppendix("noticeBinaryList", noticeBinaryList);
			}
			
			if (existsBinaryName != null) {
				pv.setAppendix("existsResultBinaryName", existsBinaryName);
			}
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				map.replace("mainData", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) map.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false, true));
				if (!vr.isValid()) {
					map.put("validData", vr.getValidMessageMap());
				}
				if (!vr.isDiff()) {
					map.put("diffData", vr.getDiffMessageMap(true));
				}
				if (vr.hasInfo()) {
					map.put("infoData", vr.getInfoMessageMap());
				}
			}
		} else if ((CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(code) || CoConstDef.CD_DTL_COMPONENT_BAT.equals(code) || CoConstDef.CD_DTL_COMPONENT_PARTNER_BAT.equals(code))
				&& map != null) {
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BAT);
			pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				if (!CoConstDef.CD_DTL_COMPONENT_BAT.equals(code)){
					map.replace("mainData", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) map.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false));
				}
				
				if (!vr.isValid()) {
					map.put("validData", vr.getValidMessageMap());
				}
				
				if (!vr.isDiff()) {
					map.put("diffData", vr.getDiffMessageMap(true));
				}
				
				if (vr.hasInfo()) {
					map.put("infoData", vr.getInfoMessageMap());
				}
			}
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(code) && map != null && map.containsKey("rows")
				&& !((List<ProjectIdentification>) map.get("rows")).isEmpty()) {
			map.replace("rows", projectService.setMergeGridData((List<ProjectIdentification>) map.get("rows")));
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BOM_MERGE);

			pv.setAppendix("bomList", (List<ProjectIdentification>) map.get("rows"));

			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				if (!isSortOnBom) {
					map.replace("rows", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) map.get("rows"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false, true));
				}
				
				if (!vr.isValid()) {
					map.put("validData", vr.getValidMessageMap());
				}
				
				if (!vr.isDiff()) {
					map.put("diffData", vr.getDiffMessageMap(true));
				}
				
				if (vr.hasInfo()) {
					map.put("infoData", vr.getInfoMessageMap());
				}
			} else {
				map.replace("rows", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) map.get("rows"), null, null, null, false, true));
			}
		} else if (CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(code)) {
			PartnerMaster partnerInfo = new PartnerMaster();
			
			if (identification.getPartnerId() == null){
				partnerInfo.setPartnerId(identification.getReferenceId());
			}else{
				partnerInfo.setPartnerId(identification.getPartnerId());
			}
			
			partnerInfo = partnerService.getPartnerMasterOne(partnerInfo);

			if (partnerInfo != null) {
				pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_PARTNER);

				// main grid
				pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
				// sub grid
				pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
				
				if ((CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST.equals(partnerInfo.getStatus())
						|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equals(partnerInfo.getStatus()))
						&& CommonFunction.isAdmin()) {
					pv.setCheckForAdmin(true);
				}

				T2CoValidationResult vr = pv.validate(new HashMap<>());
				
				if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
					map.replace("mainData", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) map.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false, true));
					if (!vr.isValid()) {
						map.put("validData", vr.getValidMessageMap());
					}
					if (!vr.isDiff()) {
						map.put("diffData", vr.getDiffMessageMap(true));
					}
					if (vr.hasInfo()) {
						map.put("infoData", vr.getInfoMessageMap());
					}
				}
			}
		} else if (isBatResult) {
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BAT);
			// main grid
			pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());

			if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				map.replace("mainData", CommonFunction.identificationSortByValidInfo((List<ProjectIdentification>) map.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false));
				if (!vr.isValid()) {
					map.put("validData", vr.getValidMessageMap());
				}
				if (!vr.isDiff()) {
					map.put("diffData", vr.getDiffMessageMap(true));
				}
				if (vr.hasInfo()) {
					map.put("infoData", vr.getInfoMessageMap());
				}
			}
		}

		return map;
	}
	
	/**
	 * [API] 프로젝트 상세 모델 목록 조회.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.MODELLIST_AJAX)
	public @ResponseBody ResponseEntity<Object> modellistAjax(@ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, List<Project>> map = projectService.getModelList(project.getPrjId());
		
		if (map != null && !map.isEmpty() && CoConstDef.FLAG_YES.equals(project.getCopy())) {
			List<Project> list = map.get("currentModelList");
			
			if (list != null) {
				List<Project> list2 = new ArrayList<>();
				
				for (Project modelBean : list) {
					modelBean.setReleaseDate(null);
					modelBean.setModifier(null);
					modelBean.setModifiedDate(null);
					modelBean.setOsddSyncYn(null);
					modelBean.setOsddSyncTime(null);
					list2.add(modelBean);
				}
				
				map.put("currentModelList", list2);
			}
		}
		
		return makeJsonResponseHeader(map);
	}
	
	/**
	 * [API] 카테고리 코드 조회.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the category code to json
	 */
	@GetMapping(value = PROJECT.CATEGORY_CODE_TOJSON)
	public @ResponseBody ResponseEntity<Object> getCategoryCodeToJson(HttpServletRequest req, HttpServletResponse res,
			Model model) {
		String code = avoidNull(req.getParameter("code"), CoConstDef.CD_MODEL_TYPE);
		
		return makeJsonResponseHeader(projectService.getCategoryCodeToJson(code));
	}
	
	/**
	 * [API] Identification OssNames 조회.
	 *
	 * @param identification the identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the oss names
	 */
	@GetMapping(value = PROJECT.OSS_NAMES)
	public @ResponseBody ResponseEntity<Object> getOssNames(ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		List<ProjectIdentification> list = projectService.getOssNames(identification);
		
		return makeJsonResponseHeader(list);
	}
	
	/**
	 * [API] Identification OssVersions 조회.
	 *
	 * @param identification the identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the oss versions
	 */
	@GetMapping(value = PROJECT.OSS_VERSIONS)
	public @ResponseBody ResponseEntity<Object> getOssVersions(ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String ossName = req.getParameter("ossName");
		List<ProjectIdentification> list = projectService.getOssVersions(ossName);

		return makeJsonResponseHeader(list);
	}
	
	/**
	 * [API] 프로젝트 목록 Reviewer 저장.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.UPDATE_REVIEWER)
	public @ResponseBody ResponseEntity<Object> updateReviewer(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Project orgProject = projectService.getProjectBasicInfo(project.getPrjId());
		String beforeReviewer = orgProject.getReviewer();
		project.setModifier(project.getLoginUserName());
		project.setModifiedDate(project.getCreatedDate());

		if (!isEmpty(project.getReviewer()) && project.getReviewer().equals(orgProject.getReviewer())) {
			return makeJsonResponseHeader();
		}

		projectService.updateReviewer(project);
		
		try {
			History h = projectService.work(project);
			h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
			historyService.storeData(h);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			if (!isEmpty(project.getReviewer())) {
				String mailType = isEmpty(beforeReviewer) ? CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_ADD : CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_CHANGED;
				
				CoMail mailBean = new CoMail(mailType);
				mailBean.setToIds(new String[] { orgProject.getReviewer(), project.getReviewer() }); // change reviewer 변경 전후 reviewer 전부에게 mail 전송
				mailBean.setParamPrjId(project.getPrjId());
				CoMailManager.getInstance().sendMail(mailBean);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * [API] 프로젝트 목록 Reject 저장.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.UPDATE_REJECT)
	public @ResponseBody ResponseEntity<Object> updateReject(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		try {
			project.setModifier(project.getLoginUserName());
			project.setModifiedDate(project.getCreatedDate());
			projectService.updateReject(project);
			
			for (String prjId : project.getPrjIds()) {
				project.setPrjId(prjId);
				History h = projectService.work(project);
				h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
				project = (Project) h.gethData();
				h.sethEtc(project.etcStr());
				historyService.storeData(h);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * [API] 카테고리 코드 조회.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the category code
	 */
	@GetMapping(value = PROJECT.CATEGORY_CODE)
	public @ResponseBody String getCategoryCode(HttpServletRequest req, HttpServletResponse res, Model model) {
		String code = req.getParameter("code");
		String gubun = req.getParameter("gubun");

		return projectService.getCategoryCode(code, gubun);
	}
	
	/**
	 * [API] Comment Update.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.UPDATE_COMMENT)
	public @ResponseBody ResponseEntity<Object> updateComment(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		try {
			projectService.updateComment(project);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return makeJsonResponseHeader();
	}

	/**
	 * [API] 프로젝트 저장.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(@ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		String jsonString = project.getPrjModelJson();
		Type collectionType = new TypeToken<List<Project>>() {}.getType();
		List<Project> list = (List<Project>) fromJson(jsonString, collectionType);
		project.setModelList(list);
		
		if (project.getWatchers() == null) {
			String[] watcherArr = req.getParameterValues("watchers");
			
			if (watcherArr == null) {
				String watcherArrTemp = req.getParameter("watchers");
				
				if (watcherArrTemp != null) {
					String[] tempWatcherArr = {watcherArrTemp};
					watcherArr = tempWatcherArr;
				}
			}
			
			project.setWatchers(watcherArr);			
		}

		Boolean isNew = StringUtil.isEmpty(project.getPrjId());
		Project beforeBean = null;
		Project afterBean = null;
		String copy = req.getParameter("copy");
		String confirmStatusCopy = req.getParameter("confirmStatusCopy");
		String creatorIdByName = null;
		String secIdByName = null;

		if (CommonFunction.isAdmin() && !isNew && !"true".equals(copy)) {
			if (!isEmpty(project.getCreatorNm())) {
				List<T2Users> userList = userService.getUserListByName(project.getCreatorNm());
				
				if (userList != null) {
					for (T2Users _bean : userList) {
						if (_bean.getUserId().equals(project.getCreator())) {
							creatorIdByName = _bean.getUserId();
							
							if (!creatorIdByName.equals(project.getCreator())) {
								project.setCreator(_bean.getUserId());
							}
							
							break;
						}
					}
				}
			}

			if (!isEmpty(project.getSecPersonNm())) {
				List<T2Users> userList = userService.getUserListByName(project.getSecPersonNm());

				if (userList != null) {
					for (T2Users _bean : userList) {
						if (_bean.getUserId().equals(project.getSecPerson())) {
							secIdByName = _bean.getUserId();

							if (!secIdByName.equals(project.getSecPerson())) {
								project.setSecPerson(_bean.getUserId());
							}

							break;
						}
					}
				}
			}
		}

		// validation check
		T2CoProjectValidator pv = new T2CoProjectValidator();
		pv.setProcType(pv.PROC_TYPE_BASICINFO);
		Map<String, String> reqMap = new HashMap<>();
		pv.saveRequest(req, reqMap);
		
		if (!isEmpty(creatorIdByName)) {
			reqMap.put("CREATOR_NM", creatorIdByName);
		}
		
		if ("true".equals(copy) && reqMap.containsKey("PRJ_ID")) {
			reqMap.put("PRJ_ID", "");
		}
		
		T2CoValidationResult vr = pv.validateObject(reqMap, list);

		if (!vr.isValid()) {
			return makeJsonResponseHeader(vr.getValidMessageMap());
		}

		project.setCopy(copy);
		
		try {
			History h = new History();

			if (isNew) {
				projectService.registProject(project);
				h = projectService.work(project);
				h.sethAction(CoConstDef.ACTION_CODE_INSERT);
			} else {
				beforeBean = projectService.getProjectBasicInfo(project.getPrjId());
				beforeBean.setModelList(projectService.getModelListExcel(project));
				beforeBean.setWatcherList(projectService.getWatcherList(project));
				projectService.registProject(project);
				if (copy.equals("true")) {
					projectService.copyOssComponentList(project, false);
					if (project.getNoticeType() != null && project.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
						projectService.copySrcAndroidNoticeFile(project);
					}
				}
				afterBean = projectService.getProjectBasicInfo(project.getPrjId());
				afterBean.setModelList(list);
				afterBean.setWatchers(project.getWatchers());
				h = projectService.work(project);
				h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
			}
			
			historyService.storeData(h);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}

		Map<String, String> lastResult = new HashMap<>();
		lastResult.put("prjId", project.getPrjId());
		String flag = "false";
		
		if (!isNew && afterBean.getModelList() != null && afterBean.getModelList().size() > 0) {
			if (beforeBean.getModelList() == null || beforeBean.getModelList().size() == 0) {
				flag = "true";
			} else {
				for (int i=0; i < afterBean.getModelList().size(); i++){
					int cnt = 0;
					boolean ck_flag = false;
					String after = afterBean.getModelList().get(i).getCategory()+"|"+afterBean.getModelList().get(i).getModelName()+"|"+afterBean.getModelList().get(i).getReleaseDate();
					
					for (int j=0; j < beforeBean.getModelList().size(); j++){
						String before = beforeBean.getModelList().get(j).getCategory()+"|"+beforeBean.getModelList().get(j).getModelName()+"|"+beforeBean.getModelList().get(j).getReleaseDate();
						
						if (avoidNull(after).equals(before)){
							cnt++;
						}
						
						if (!avoidNull(after).equals(before) && cnt == 0){
							ck_flag = true;
						}
					}
					
					if (cnt == 0 && ck_flag == true){
						flag = "true";
					}
				}
			}
		}
		
		lastResult.put("isAdd", flag);
		String userComment = project.getUserComment();
		if (!isEmpty(userComment) && !isEmpty(project.getPrjId())) {
			CommentsHistory commentsHistory = new CommentsHistory();
			commentsHistory.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
			commentsHistory.setReferenceId(project.getPrjId());
			commentsHistory.setContents(userComment);
			
			commentService.registComment(commentsHistory);
		}

		if (!isNew) {
			try {
				String mailType = "true".equals(copy)?CoConstDef.CD_MAIL_TYPE_PROJECT_COPIED:CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED;
				String diffDivisionComment = "";
				
				if (CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED.equals(mailType) && !beforeBean.getDivision().equals(afterBean.getDivision())){
					diffDivisionComment = CommonFunction.getDiffItemComment(beforeBean, afterBean);
				}
				
				CoMail mailBean = new CoMail(mailType);
				mailBean.setParamPrjId(project.getPrjId());
				mailBean.setCompareDataBefore(beforeBean);
				mailBean.setCompareDataAfter(afterBean);

//				if ("true".equals(copy)) {
//					String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_COPIED));
//					userComment = avoidNull(userComment) + "<br />" + _tempComment;
//				}
				mailBean.setComment(userComment);
				
				CoMailManager.getInstance().sendMail(mailBean);
				
				if (CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED.equals(mailType)){
					String diffItemComment = CommonFunction.getDiffItemComment(beforeBean, afterBean, true);
					
					try {
						if (!isEmpty(diffItemComment) || !isEmpty(diffDivisionComment)) {
							diffItemComment += diffDivisionComment;
							CommentsHistory commHisBean = new CommentsHistory();
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
							commHisBean.setReferenceId(project.getPrjId());
							commHisBean.setContents(diffItemComment);
							commHisBean.setStatus("Changed");
							commentService.registComment(commHisBean);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				} else if (CoConstDef.CD_MAIL_TYPE_PROJECT_COPIED.equals(mailType)){
					String linkUrl = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/project/shareUrl/" + beforeBean.getPrjId();
					String _s = "<a href='" + linkUrl + "' class='urlLink2' target='_blank'>PRJ-" + beforeBean.getPrjId() + "</a>";
					String initMessage = "<p>";
					if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(beforeBean.getIdentificationStatus())) {
						initMessage += "Copy with confirm status from [" + _s + "] " + beforeBean.getPrjName();
					} else {
						initMessage += "Copied from [" + _s + "] " + beforeBean.getPrjName();
						initMessage += avoidNull(beforeBean.getPrjVersion()) + "_Copied";
					}
					initMessage += "</p>";
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
					commHisBean.setReferenceId(project.getPrjId());
					commHisBean.setContents(initMessage);
					commHisBean.setStatus("Copied");
					commentService.registComment(commHisBean);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			try {
				String initMessage = "<p>Project Created</p>";
				if (!isEmpty(userComment)) {
					initMessage += userComment;
				}
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
				commHisBean.setReferenceId(project.getPrjId());
				commHisBean.setContents(initMessage);
				commHisBean.setStatus("Created");
				commentService.registComment(commHisBean);
				
				CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED);
				mailBean.setParamPrjId(project.getPrjId());

				String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED));
				userComment = avoidNull(userComment) + "<br />" + _tempComment;
				mailBean.setComment(userComment);
				
				CoMailManager.getInstance().sendMail(mailBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		if ((isNew || "true".equals(copy)) && !isEmpty(project.getPrjId()) && project.getWatchers() != null && project.getWatchers().length > 0) {
			List<String> mailList = new ArrayList<>(); 
			
			if (!isNew && !"true".equals(copy)) {
				String[] arr;
				List<String> emailList = new ArrayList<>();
				List<String> emailList2 = new ArrayList<>();
				
				for (String watcher : project.getWatchers()) {
					arr = watcher.split("\\/");
					
					if ("Email".equals(arr[1])){
						if (!emailList.contains(arr[0])) {
							emailList.add(arr[0]);
						}
					}
				}
			
				if (beforeBean.getWatcherList() != null && !beforeBean.getWatcherList().isEmpty()) {
					for (Project _prj : beforeBean.getWatcherList()) {
						emailList2.add(_prj.getPrjEmail());
					}
				}
				
				for (String s : emailList) {
					if (!emailList2.contains(s)) {
						// 신규 추가 이면
						mailList.add(s);
					}
				}
			} else {
				List<Project> _prjList = projectService.getWatcherList(project);
				
				if (_prjList != null) {
					for (Project _prj : _prjList) {
						if (!StringUtil.isEmptyTrimmed(_prj.getPrjEmail())) {
							String _mailAddr = _prj.getPrjEmail().trim();
							if (!mailList.contains(_mailAddr)) {
								mailList.add(_mailAddr);
							}
						}
					}
				}
			}
			
			if (!mailList.isEmpty()) {
				for (String addr : mailList) {
					try {
						CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_INVATED);
						mailBean.setParamPrjId(project.getPrjId());
						mailBean.setParamUserId(project.getLoginUserName());
						mailBean.setParamEmail(addr);
						
						CoMailManager.getInstance().sendMail(mailBean);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
		
		try {
			// 1. 신규 생성 & partner id가 존재함. -> 이미 완료된 시점
			if (isNew && !isEmpty(project.getRefPartnerId())) {
				// 2. RefPartnerId 기준으로 OSS Component & OSS Component License Data -> Identification > 3rd Party에 copy함.
				// 3. PROJECT_PARTNER_MAP 생성
				// -> Identification > 3rd party save처리와 동일한 상태
				projectService.addPartnerData(project);
				
				// 4. merge and save 처리
				projectService.registBom(project.getPrjId(), CoConstDef.FLAG_YES, new ArrayList<>(), new ArrayList<>()); // 신규생성이기 때문에 default Data가 없음.
				projectService.updateSecurityDataForProject(project.getPrjId());
				
				// 5. validation check로 project status를 정리함.
				ProjectIdentification identification = new ProjectIdentification();
				identification.setReferenceId(project.getPrjId());
				identification.setMerge(CoConstDef.FLAG_NO);
				Map<String, Object> result = getOssComponentDataInfo(identification, CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				
				// 6. mail & comment를 남김.(status가 정리된 내용까지 전부 포함.)
				Project prjBean = new Project();
				prjBean.setPrjId(project.getPrjId());
				prjBean.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST);
				try {
					Map<String, Object> resultMap = projectService.updateProjectStatus(project, false, false);
					updateProjectNotification(project, resultMap);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				
				if (!result.containsKey("validData")) {
					// review Start에서는 status말고 변경되는 정보가 없어서 우선은 pass함.					
					prjBean.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
					prjBean.setIgnoreBinaryDbFlag(CoConstDef.FLAG_NO);
					
					try {
						Map<String, Object> resultMap = projectService.updateProjectStatus(prjBean, true, false);
						updateProjectNotification(prjBean, resultMap);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		if (copy.equals("true")) {
			if (!isEmpty(project.getCopyPrjId())) {
				if (!project.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
					project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				} else {
					project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM);
				}
				projectService.copyOssComponentList(project, true);
			}
			
			if (!confirmStatusCopy.equals("false")) {
				Map<String, Object> copyConfirmStatusResultMap = updateCopyConfirmStatus(req, project, confirmStatusCopy, userComment);
				if (copyConfirmStatusResultMap.get("result").equals("true")) {
					lastResult.put("confirmCopyStatusSuccess", "true");
				} else {
					String falseStep = "";
					
					if (copyConfirmStatusResultMap.get("step").equals("verificationProgress")) {
						falseStep = "verification";
						project.setIdentificationStatus(null);
						project.setVerificationStatus(CoConstDef.CD_DTL_PROJECT_STATUS_PROGRESS);
					} else {
						falseStep = "identification";
						project.setIdentificationStatus(CoConstDef.CD_DTL_PROJECT_STATUS_PROGRESS);
						project.setVerificationStatus(null);
					} 
					
					Project prj = projectService.getProjectBasicInfo(project.getPrjId());
					if (falseStep.equals("verification") && !isEmpty(prj.getVerificationStatus())) {
						project.setVerificationStatus(prj.getVerificationStatus());
					}
					
					projectService.updateCopyConfirmStatusProjectStatus(project);
					
					lastResult.put("confirmCopyStatusSuccess", "false");
					if (!confirmStatusCopy.equals("IdentificationProg")) {
						lastResult.put("confirmCopyStatusFail", falseStep);
					}
				}
			}
		}
		
		return makeJsonResponseHeader(true, null, lastResult);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> updateCopyConfirmStatus(HttpServletRequest req, Project project, String confirmStatusCopy, String userComment) {
		log.info("copyConfirmStatus Start >>> " + project.getPrjId());
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		boolean identificationStatusRequest = false;
		boolean identificationStatusConfirm = false;
		boolean isVerificationConfirm = false;
		
		if (!project.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
			ProjectIdentification identification = new ProjectIdentification();
			identification.setReferenceId(project.getCopyPrjId());
			identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
			identification.setMerge(CoConstDef.FLAG_NO);
			Map<String, Object> result = getOssComponentDataInfo(identification, CoConstDef.CD_DTL_COMPONENT_ID_BOM);
			projectService.insertCopyConfirmStatusBomList(project, identification);
			
			if (result.containsKey("validData") && (confirmStatusCopy.equals("IdentificationConf") || confirmStatusCopy.equals("verificationConf"))) {
				log.error("copyConfirmStatus error >>> bom validation");
				returnMap.put("result", "false");
				returnMap.put("step", "IdentificationProgress");
				return returnMap;
			}
			
			if (!result.containsKey("validData") && !confirmStatusCopy.equals("IdentificationProg")) {
				project.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST);
				
				try {
					Map<String, Object> resultMap = projectService.updateProjectStatus(project, false, false);
					resultMap.put("userComment", "");
					updateProjectNotification(project, resultMap);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					returnMap.put("result", "false");
					returnMap.put("step", "identificationProgress");
					return returnMap;
				} finally {
					identificationStatusRequest = true;
				}
			}
		} else {
			ProjectIdentification param = new ProjectIdentification();
			param.setReferenceId(project.getPrjId());
			param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
			Map<String, Object> map = null;
			map = projectService.getIdentificationGridList(param);
			
			if (map != null && map.containsKey("mainData")
					&& !((List<ProjectIdentification>) map.get("mainData")).isEmpty()) {
				T2CoProjectValidator pv = new T2CoProjectValidator();
				pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);

				pv.setAppendix("mainList", (List<ProjectIdentification>) map.get("mainData"));
				pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) map.get("subData"));
				T2CoValidationResult vr = pv.validate(new HashMap<>());
				
				if (!vr.isValid()) {
					log.error("copyConfirmStatus error >>> mainData validation");
					returnMap.put("result", "false");
					returnMap.put("step", "identificationProgress");
					return returnMap;
				}
			}else {
				returnMap.put("result", "false");
				returnMap.put("step", "identificationProgress");
				return returnMap;
			}
			
			if (isEmpty(projectService.getProjectBasicInfo(project.getPrjId()).getSrcAndroidNoticeFileId())) {
				log.error("copyConfirmStatus error > androidNoticeFile empty");
				returnMap.put("result", "false");
				returnMap.put("step", "identificationProgress");
				return returnMap;
			}
			
			if (!confirmStatusCopy.equals("IdentificationProg")) {
				project.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST);
				
				try {
					Map<String, Object> resultMap = projectService.updateProjectStatus(project, false, false);
					resultMap.put("userComment", "");
					updateProjectNotification(project, resultMap);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					returnMap.put("result", "false");
					returnMap.put("step", "identificationProgress");
					return returnMap;
				} finally {
					identificationStatusRequest = true;
				}
			}
		}
		
		Project prjInfo = projectService.getProjectBasicInfo(project.getPrjId());
		Project copyPrjInfo = projectService.getProjectBasicInfo(project.getCopyPrjId());
		boolean networkServerType = project.getNetworkServerType().equals(copyPrjInfo.getNetworkServerType());
		
		if (identificationStatusRequest) {
			project.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
			project.setIgnoreBinaryDbFlag(CoConstDef.FLAG_NO);
			if (confirmStatusCopy.equals("verificationConf") && networkServerType) {
				isVerificationConfirm = true;
			}
			try {
				Map<String, Object> resultMap = projectService.updateProjectStatus(project, true, isVerificationConfirm);
				resultMap.put("userComment", "");
				updateProjectNotification(project, resultMap);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				returnMap.put("result", "false");
				returnMap.put("step", "identificationProgress");
				return returnMap;
			} finally {
				identificationStatusConfirm = true;
			}
		}
		
		if (identificationStatusConfirm && confirmStatusCopy.equals("verificationConf")) {
			if (!networkServerType) {
				returnMap.put("result", "false");
				returnMap.put("step", "verificationProgress");
				return returnMap;
			}
			
			String filePath = CommonFunction.emptyCheckProperty("packaging.path", "/upload/packaging");
			List<String> fileSeqsList = projectService.getPackageFileList(project, filePath);
			
			if (fileSeqsList.size() > 0) {
				try {
					verificationService.updateFileWhenVerificationCopyConfirm(prjInfo, copyPrjInfo, fileSeqsList);
				} catch (IOException e1) {
					log.error(e1.getMessage(), e1);
					returnMap.put("result", "false");
					returnMap.put("step", "verificationProgress");
					return returnMap;
				}
			}
			
			OssNotice ossNotice = verificationService.selectOssNoticeOne(project.getCopyPrjId());
			ossNotice.setPrjId(project.getPrjId());
			ossNotice.setNoticeType(avoidNull(project.getNoticeType(), avoidNull(copyPrjInfo.getNoticeType(), CoConstDef.CD_NOTICE_TYPE_GENERAL)));
			verificationService.registOssNoticeConfirmStatus(ossNotice);
			
			// download flag
			prjInfo.setAllowDownloadNoticeHTMLYn(copyPrjInfo.getAllowDownloadNoticeHTMLYn());
			prjInfo.setAllowDownloadNoticeTextYn(copyPrjInfo.getAllowDownloadNoticeTextYn());
			prjInfo.setAllowDownloadSimpleHTMLYn(copyPrjInfo.getAllowDownloadSimpleHTMLYn());
			prjInfo.setAllowDownloadSimpleTextYn(copyPrjInfo.getAllowDownloadSimpleTextYn());
			prjInfo.setAllowDownloadSPDXSheetYn(copyPrjInfo.getAllowDownloadSPDXSheetYn());
			prjInfo.setAllowDownloadSPDXRdfYn(copyPrjInfo.getAllowDownloadSPDXRdfYn());
			prjInfo.setAllowDownloadSPDXTagYn(copyPrjInfo.getAllowDownloadSPDXTagYn());
			prjInfo.setAllowDownloadSPDXJsonYn(copyPrjInfo.getAllowDownloadSPDXJsonYn());
			prjInfo.setAllowDownloadSPDXYamlYn(copyPrjInfo.getAllowDownloadSPDXYamlYn());
			
			verificationService.updateProjectAllowDownloadBitFlag(prjInfo);
			
			boolean ignoreMailSend = false;

			prjInfo.setIdentificationStatus(null);
			prjInfo.setCompleteYn(null);
			prjInfo.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REQUEST);
			prjInfo.setDistributionStatus(null);
			prjInfo.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
			
			try {
				Map<String, Object> resultMap = projectService.updateProjectStatus(prjInfo, false, false);
				resultMap.put("userComment", "");
				updateProjectNotification(prjInfo, resultMap);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				returnMap.put("result", "false");
				returnMap.put("step", "verificationProgress");
				return returnMap;
			}
			
			prjInfo.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
			prjInfo.setDistributionStatus(null);
			prjInfo.setModifier(loginUserName());

			try {
				verificationService.updateStatusWithConfirm(prjInfo, ossNotice, true);
			} catch(Exception e) {
				log.error(e.getMessage(), e);
				returnMap.put("result", "false");
				returnMap.put("step", "verificationProgress");
				return returnMap;
			}
			
			try {
				String packagingFileComment = verificationService.changePackageFileNameCombine(ossNotice.getPrjId());;
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
				commHisBean.setReferenceId(project.getPrjId());
				commHisBean.setContents(packagingFileComment);
				commHisBean.setStatus(CoCodeManager.getCodeExpString(CoConstDef.CD_IDENTIFICATION_STATUS, CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM));
				
				commentService.registComment(commHisBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (prjInfo != null) {
				String noticeUserComment = ossNotice.getUserComment();
				try {
					String mailTemplateCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF;
					String mailContentCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF;
					if (ignoreMailSend) {
						mailTemplateCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY;
						mailContentCode = mailTemplateCode;
					} else if (CoConstDef.FLAG_YES.equals(prjInfo.getAndroidFlag())) {
						mailTemplateCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY;
					}
					CoMail mailBean = new CoMail(mailTemplateCode);
					mailBean.setParamPrjId(ossNotice.getPrjId());
					String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, mailContentCode));
					noticeUserComment = avoidNull(noticeUserComment) + "<br />" + _tempComment;

					if (!isEmpty(noticeUserComment)) {
						mailBean.setComment(noticeUserComment);
					}
					CoMailManager.getInstance().sendMail(mailBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		returnMap.put("result", "true");
		return returnMap;
	}

	/**
	 * [API] 프로젝트 삭제.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.DEL_AJAX)
	public @ResponseBody ResponseEntity<Object> delAjax(@ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {

		Project projectInfo = projectService.getProjectDetail(project);
		
		try {
			History h = new History();
			h = projectService.work(project);
			h.sethAction(CoConstDef.ACTION_CODE_DELETE);
			historyService.storeData(h);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_DELETED);
			mailBean.setParamPrjId(project.getPrjId());
			
			if (!isEmpty(project.getUserComment())) {
				mailBean.setComment(project.getUserComment());
			}
			
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		String rtnFlag = "11"; // default error
		HashMap<String, Object> resMap = new HashMap<>();
		
		try {
			projectService.deleteProject(project);
			rtnFlag = "10"; // Success
			try {
				// Delete project ref files
				projectService.deleteProjectRefFiles(projectInfo);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		resMap.put("resCd", rtnFlag);
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value = PROJECT.MULTI_DEL_AJAX)
	public @ResponseBody ResponseEntity<Object> multiDelAjax(@ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {

		String rtnFlag = "11"; // default error
		HashMap<String, Object> resMap = new HashMap<>();
		List<String> noDelPrjIds = new ArrayList<>();
		
		for (String prjId : project.getPrjIds()) {
			Project param = new Project();
			param.setPrjId(prjId);
			param.setUserComment(project.getUserComment());
			
			Project projectInfo = projectService.getProjectDetail(param);
			if (CoConstDef.CD_DTL_PROJECT_STATUS_COMPLETE.equalsIgnoreCase(projectInfo.getStatus()) || CoConstDef.CD_DTL_PROJECT_STATUS_FINAL_REVIEW.equalsIgnoreCase(projectInfo.getStatus())) {
				rtnFlag = "10";
				noDelPrjIds.add(prjId);
				continue;
			}
			
			try {
				History h = new History();
				h = projectService.work(projectInfo);
				h.sethAction(CoConstDef.ACTION_CODE_DELETE);
				historyService.storeData(h);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			try {
				CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_DELETED);
				mailBean.setParamPrjId(param.getPrjId());
				
				if (!isEmpty(param.getUserComment())) {
					mailBean.setComment(param.getUserComment());
				}
				
				CoMailManager.getInstance().sendMail(mailBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			try {
				projectService.deleteProject(param);
				rtnFlag = "10"; // Success
				try {
					// Delete project ref files
					projectService.deleteProjectRefFiles(projectInfo);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		resMap.put("resCd", rtnFlag);
		if (!noDelPrjIds.isEmpty()) {
			resMap.put("noDelPrjIds", noDelPrjIds);
		}
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value = PROJECT.IDENTIFICATION_ID_DIV_DELETE)
	public @ResponseBody ResponseEntity<Object> delIdentificaitonDiv(@ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse res, Model model, @PathVariable String prjId, @PathVariable String initDiv) {
		String rtnFlag = "10";
		HashMap<String, Object> resMap = new HashMap<>();
		
		project.setPrjId(prjId);
		project.setActType(CoConstDef.FLAG_NO);
		Project projectInfo = projectService.getProjectDetail(project);
		
		if (CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(initDiv)) {
			// delete project_parter_map
			
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(initDiv)) {
			// delete upload file
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(initDiv)) {
			// delete upload file
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(initDiv)) {
			
		} else if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(initDiv)) {
			
		}
		
		resMap.put("resCd", rtnFlag);
		
		return makeJsonResponseHeader(resMap);
	}
	
	/**
	 * [API] Identification OssId Licenses 조회.
	 *
	 * @param identification the identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the oss id licenses
	 */
	@GetMapping(value = PROJECT.OSS_ID_LICENSES)
	public @ResponseBody ResponseEntity<Object> getOssIdLicenses(ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String ossName = req.getParameter("ossName");
		String ossVersion = req.getParameter("ossVersion");

		identification.setOssName(ossName);
		identification.setOssVersion(ossVersion);

		Map<String, Object> map = projectService.getOssIdLicenses(identification);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Save 3 rd.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_3RD)
	public @ResponseBody ResponseEntity<Object> save3rd(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String prjId = (String) map.get("referenceId");
		String identificationSubStatusPartner = (String) map.get("identificationSubStatusPartner");
		String mainGrid = (String) map.get("mainData");
		String thirdPartyGrid = (String) map.get("thirdPartyData");
		String resetFlag = (String) map.get("resetFlag");
		
		// 메인그리드
		Type collectionType = new TypeToken<List<OssComponents>>() {}.getType();
		List<OssComponents> ossComponents = new ArrayList<>();
		ossComponents = (List<OssComponents>) fromJson(mainGrid, collectionType);
		
		Type collectionType1 = new TypeToken<List<PartnerMaster>>() {}.getType();
		List<PartnerMaster> thirdPartyList = new ArrayList<>();
		thirdPartyList = (List<PartnerMaster>) fromJson(thirdPartyGrid, collectionType1);
		
		Project project = new Project();
		
		// 서브그리드
		projectService.registComponentsThird(prjId, identificationSubStatusPartner, ossComponents, thirdPartyList);
		
		if (CoConstDef.FLAG_NO.equals(identificationSubStatusPartner)) {
			project.setPrjId(prjId);
			project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
			project.setIdentificationSubStatusPartner(identificationSubStatusPartner);
			
			// 상태값 변경
			projectService.updateSubStatus(project);
		}
		
		try {
			if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)) != null) {
				String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
				CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_PARTNER), true);
							
				if (!isEmpty(changedLicenseName)) {
					CommentsHistory commentHisBean = new CommentsHistory();
					commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
					commentHisBean.setReferenceId(prjId);
					commentHisBean.setExpansion1("3rd party");
					commentHisBean.setContents(changedLicenseName);
					commentService.registComment(commentHisBean, false);
				}
			}
			
			if (CoConstDef.FLAG_YES.equals(avoidNull(resetFlag))) {
				CommentsHistory commentHisBean = new CommentsHistory();
				commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
				commentHisBean.setReferenceId(prjId);
				commentHisBean.setExpansion1("3rd party");
				commentHisBean.setContents("reset all data in 3rd party");
				commentService.registComment(commentHisBean, false);
				
				CommonFunction.addSystemLogRecords(prjId, loginUserName());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
					
		try {
			project.setPrjId(prjId);
			History h = new History();
			h = projectService.work(project);
			h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
			project = (Project) h.gethData();
			h.sethEtc(project.etcStr());
			historyService.storeData(h);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return makeJsonResponseHeader(true, "success", projectService.getProjectDetail(project).getIdentificationStatus());
	}
	
	/**
	 * Save bin.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_BIN)
	public @ResponseBody ResponseEntity<Object> saveBin(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		try {
			// default validation
			boolean isValid = true;
			// last response map
			Map<String, String> resMap = new HashMap<>();
			// default 00:java error check code, 10:success code
			String resCd = "00";
			String prjId = (String) map.get("prjId");
			String csvFileId = (String) map.get("binCsvFileId");
			String binaryFileId = (String) map.get("binBinaryFileId");
			String delFileString = (String) map.get("csvDelFileIds");
			String identificationSubStatusBin = (String) map.get("identificationSubStatusBin");
			String mainDataString = (String) map.get("mainData");
			String binAddListDataString = (String) map.get("binAddListData");
			
			Type collectionType = new TypeToken<List<T2File>>() {}.getType();
			List<T2File> delFile = new ArrayList<T2File>(); delFile = (List<T2File>) fromJson(delFileString, collectionType);

			Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {
			}.getType();
			List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
			ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);
			
			List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
			
			Type collectionType4 = new TypeToken<List<Project>>() {
			}.getType();
			List<Project> binAddList = new ArrayList<Project>();
			binAddList = (List<Project>) fromJson(binAddListDataString, collectionType4);
			
			if (CoConstDef.FLAG_NO.equals(identificationSubStatusBin)) {
				Project project = new Project();
				project.setIdentificationSubStatusBin(identificationSubStatusBin);
				project.setPrjId(prjId);
				project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BIN);
				projectService.updateSubStatus(project);
			} else {
				
				/*
				 * 중간저장 대응을 위해 save시에는 validation check 를 수행하지 않는다. 문제가 생길경우, 꼭
				 * 필요한 체크를 별도의 type으로 추가해야함 // grid validation only
				 * customValidation check!! // 모든 체크 파라미터를 customValidation에 코딩
				 * 해야한다. T2CoProjectValidator pv = new T2CoProjectValidator();
				 * pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE); // main
				 * grid pv.setAppendix("mainList", ossComponents); // sub grid
				 * pv.setAppendix("subList", ossComponentsLicense);
				 * 
				 * // basic validator는 무시, validate를 호출하여 custom validator를
				 * 수행한다. T2CoValidationResult vr = pv.validate(new HashMap<>());
				 * 
				 * // return validator result if (!vr.isValid()) { return
				 * makeJsonResponseHeader(vr.getValidMessageMap()); }
				 */
				try {
					T2CoProjectValidator pv = new T2CoProjectValidator();
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BIN);
					pv.setValidLevel(pv.VALID_LEVEL_BASIC);
					pv.setAppendix("mainList", ossComponents); // sub grid
					pv.setAppendix("subList", ossComponentsLicense);
					T2CoValidationResult vr = pv.validate(new HashMap<>());
					
					if (!vr.isValid()) {
						if (CommonFunction.booleanValidationFormatForValidMsg(vr.getValidMessageMap(), true)) {
							return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap(), ossComponents), vr.getValidMessageMap());
						} else if (CommonFunction.booleanValidationFormatForValidMsg(vr.getValidMessageMap(), false)) {
							List<ProjectIdentification> exceedingMaxLengthList = CommonFunction.getItemsExceedingMaxLength(vr.getValidMessageMap(), ossComponents);
							if (!CollectionUtils.isEmpty(exceedingMaxLengthList)) {
								return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), exceedingMaxLengthList);
							} else {
								return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
							}
						} else {
							return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					return makeJsonResponseHeader(false, e.getMessage());
				}

				Project project = new Project();
				project.setPrjId(prjId);
				project.setBinCsvFileId(csvFileId);
				project.setBinBinaryFileId(binaryFileId);

				project.setCsvFile(delFile);
				project.setIdentificationSubStatusBin(identificationSubStatusBin);
				
				Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(ossComponents, ossComponentsLicense);
				ossComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
				ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
				
				String changeExclude = "";
				String changeAdded = "";
				// result txt가 존재하는 경우에 한해
				// result.txt에 있으나 OSS Report에 없는 경우 => load되는 OSS List에 해당 Binary를 추가. 팝업을 띄우고 Comment로 추가된 binary목록을 남김.
				// result.txt에 있으나 OSS Report에서 exclude 처리된 경우 => Exclude체크 된 것을 유지. 2번의 Comment내용과 함께 팝업에도 뜨고 Comment로 exclude되어있음을 남김
				
				// binary.txt 파일이 변경된 경우 최초 한번만 수행
				Project beforeProjectIfno = projectService.getProjectBasicInfo(prjId);
				if (!isEmpty(binaryFileId) && beforeProjectIfno != null && !binaryFileId.equals(beforeProjectIfno.getBinBinaryFileId())) {
					List<String> binaryTxtList = CommonFunction.getBinaryListBinBinaryTxt(fileService.selectFileInfoById(binaryFileId));
					
					if (binaryTxtList != null && !binaryTxtList.isEmpty()) {
						// 현재 osslist의 binary 목록을 격납
						Map<String, ProjectIdentification> componentBinaryList = new HashMap<>();
						
						for (ProjectIdentification bean : ossComponents) {
							if (!isEmpty(bean.getBinaryName())) {
								if (componentBinaryList.containsKey(bean.getBinaryName())) {
									ProjectIdentification identification = componentBinaryList.get(bean.getBinaryName());
									if (bean.getExcludeYn().equals(CoConstDef.FLAG_NO)) {
										componentBinaryList.put(bean.getBinaryName(), bean);
									} else if (identification.getExcludeYn().equals(CoConstDef.FLAG_NO)) {
										componentBinaryList.put(identification.getBinaryName(), identification);
									}
								} else {
									componentBinaryList.put(bean.getBinaryName(), bean);
								}
							}
						}
						
						List<ProjectIdentification> addComponentList = Lists.newArrayList();
						
						// 존재여부 확인
						for (String binaryNameTxt : binaryTxtList) {
							if (!componentBinaryList.containsKey(binaryNameTxt)) {
								// add 해야할 list
								ProjectIdentification bean = new ProjectIdentification();
								// 화면에서 추가한 것 처럼 jqg로 시작하는 component id를 임시로 설정한다.
								bean.setGridId("jqg_"+binaryFileId+"_"+addComponentList.size());
								bean.setBinaryName(binaryNameTxt);
								addComponentList.add(bean);
								
								changeAdded += "<br> - " + binaryNameTxt;
							} else { // exclude처리된 경우
								ProjectIdentification bean = componentBinaryList.get(binaryNameTxt);
								if (bean != null && CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
									changeExclude += "<br>" + binaryNameTxt;
								}
							}
						}
						
						if (addComponentList != null && !addComponentList.isEmpty()) {
							ossComponents.addAll(addComponentList);
						}
					}
				}

				projectService.registSrcOss(ossComponents, ossComponentsLicense, project, CoConstDef.CD_DTL_COMPONENT_ID_BIN);
				binaryDataService.autoIdentificationWithBinryTextFile(project);
				
				if (!isEmpty(changeExclude) || !isEmpty(changeAdded)) {
					String changedByResultTxt = "";
					
					if (!isEmpty(changeAdded)) {
						changedByResultTxt += "<b>The following binaries were added to OSS List automatically because they exist in the fosslight_binary.txt.</b><br>";
						changedByResultTxt += changeAdded;
					}
					
					if (!isEmpty(changeExclude)) {
						if (!isEmpty(changedByResultTxt)) {
							changedByResultTxt += "<br><br>";
						}
						changedByResultTxt += "<b>The following binaries are written to the OSS report as excluded, but they are in the fosslight_binary.txt. Make sure it is not included in the final firmware.</b><br>";
						changedByResultTxt += changeExclude;
					}
					
					resMap.put("changeBySystemNotice", changedByResultTxt);
					CommentsHistory commentHisBean = new CommentsHistory();
					commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
					commentHisBean.setReferenceId(prjId);
					commentHisBean.setExpansion1("BIN");
					commentHisBean.setContents(changedByResultTxt);
					commentService.registComment(commentHisBean, false);
				}
				
				// 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
				try {
					if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
							CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId)) != null) {
						String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(
								loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId), true);
						
						if (!isEmpty(changedLicenseName)) {
							CommentsHistory commentHisBean = new CommentsHistory();
							commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commentHisBean.setReferenceId(prjId);
							commentHisBean.setExpansion1("BIN");
							commentHisBean.setContents(changedLicenseName);
							commentService.registComment(commentHisBean, false);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				// oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
				try {
					if (getSessionObject(
							CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId,
									CoConstDef.CD_DTL_COMPONENT_ID_BIN)) != null) {
						String changedLicenseName = (String) getSessionObject(
								CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED,
										prjId, CoConstDef.CD_DTL_COMPONENT_ID_BIN),
								true);
						
						if (!isEmpty(changedLicenseName)) {
							CommentsHistory commentHisBean = new CommentsHistory();
							commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commentHisBean.setReferenceId(prjId);
							commentHisBean.setExpansion1("BIN");
							commentHisBean.setContents(changedLicenseName);
							commentService.registComment(commentHisBean, false);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				
				try {
					if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId)) != null) {
						String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId), true);
						
						if (!isEmpty(chagedOssVersion)) {
							CommentsHistory commentHisBean = new CommentsHistory();
							commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commentHisBean.setReferenceId(prjId);
							commentHisBean.setExpansion1("BIN");
							commentHisBean.setContents(chagedOssVersion);
							commentService.registComment(commentHisBean, false);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				try {
					History h = new History();
					h = projectService.work(project);
					h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
					project = (Project) h.gethData();
					h.sethEtc(project.etcStr());
					historyService.storeData(h);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			Project project = new Project();
			project.setPrjId(prjId);
			project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BIN);
			projectService.existsAddList(project);
			projectService.insertAddList(binAddList);
			
			// session 삭제
			deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_BIN, prjId));
			deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN, prjId));

			// success code set 10
			resCd = "10";
			resMap.put("isValid", String.valueOf(isValid));
			resMap.put("resCd", resCd);
			resMap.put("resultData", projectService.getProjectDetail(project).getIdentificationStatus());
			
			return makeJsonResponseHeader(resMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			return makeJsonResponseHeader(false, null);
		}
	}

	/**
	 * Save dep.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_DEP)
	public @ResponseBody ResponseEntity<Object> saveDep(@RequestBody HashMap<String, Object> map,
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
		String identificationSubStatusDep = (String) map.get("identificationSubStatusDep");
		String mainDataString = (String) map.get("mainData");
		String depAddListDataString = (String) map.get("depAddListData");

		Type collectionType = new TypeToken<List<T2File>>() {}.getType();
		List<T2File> delFile = new ArrayList<T2File>();
		delFile = (List<T2File>) fromJson(delFileString, collectionType);
		List<T2File> addFile = new ArrayList<T2File>();
		addFile = (List<T2File>) fromJson(FileSeqs, collectionType);
		Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
		ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);

		List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);

		Type collectionType4 = new TypeToken<List<Project>>() {}.getType();
		List<Project> depAddList = new ArrayList<Project>();
		depAddList = (List<Project>) fromJson(depAddListDataString, collectionType4);

		if (CoConstDef.FLAG_NO.equals(identificationSubStatusDep)) {
			Project project = new Project();
			project.setIdentificationSubStatusDep(identificationSubStatusDep);
			project.setPrjId(prjId);
			project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_DEP);
			projectService.updateSubStatus(project);
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
			//pv.setIgnore("LICENSE_NAME"); // 예외처리를 추가함.
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
			pv.setValidLevel(pv.VALID_LEVEL_BASIC);
			pv.setAppendix("mainList", ossComponents); // sub grid
			pv.setAppendix("subList", ossComponentsLicense);

			T2CoValidationResult vr = pv.validate(new HashMap<>());

			if (!vr.isValid()) {
				if (CommonFunction.booleanValidationFormatForValidMsg(vr.getValidMessageMap(), true)) {
					return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap(), ossComponents), vr.getValidMessageMap());
				} else if (CommonFunction.booleanValidationFormatForValidMsg(vr.getValidMessageMap(), false)) {
					List<ProjectIdentification> exceedingMaxLengthList = CommonFunction.getItemsExceedingMaxLength(vr.getValidMessageMap(), ossComponents);
					if (!CollectionUtils.isEmpty(exceedingMaxLengthList)) {
						return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), exceedingMaxLengthList);
					} else {
						return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
					}
				} else {
					return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
				}
			}

			Project project = new Project();
			project.setPrjId(prjId);
			project.setDepCsvFileId(csvFileId);
			project.setCsvFile(delFile);
			project.setCsvFileSeq(addFile);
			project.setIdentificationSubStatusDep(identificationSubStatusDep);

			Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(ossComponents, ossComponentsLicense);
			ossComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
			ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");

			projectService.registDepOss(ossComponents, ossComponentsLicense, project);

			// 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
			if (addFile != null) {
				for (T2File _file : addFile) {
					if (!isEmpty(_file.getFileSeq())) {
						try {
							if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
									CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, _file.getFileSeq())) != null) {
								String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(
										loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE,
										_file.getFileSeq()), true);

								if (!isEmpty(changedLicenseName)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("DEP");
									commentHisBean.setContents(changedLicenseName);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}

						try {
							if(getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, _file.getFileSeq())) != null) {
								String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, _file.getFileSeq()), true);

								if(!isEmpty(chagedOssVersion)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("DEP");
									commentHisBean.setContents(chagedOssVersion);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}

			try {
				History h = new History();
				h = projectService.work(project);
				h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
				project = (Project) h.gethData();
				h.sethEtc(project.etcStr());
				historyService.storeData(h);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		Project project = new Project();
		project.setPrjId(prjId);
		project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_DEP);
		projectService.existsAddList(project);
		projectService.insertAddList(depAddList);

		// 정상처리된 경우 세션 삭제
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_DEP, prjId));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_DEP, prjId));

		// success code set 10
		resCd = "10";
		resMap.put("isValid", String.valueOf(isValid));
		resMap.put("resCd", resCd);
		resMap.put("resultData", projectService.getProjectDetail(project).getIdentificationStatus());

		return makeJsonResponseHeader(resMap);
	}

	/**
	 * Save src.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_SRC)
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
		String srcAddListDataString = (String) map.get("srcAddListData");
		
		Type collectionType = new TypeToken<List<T2File>>() {}.getType();
		List<T2File> delFile = new ArrayList<T2File>();
		delFile = (List<T2File>) fromJson(delFileString, collectionType);
		List<T2File> addFile = new ArrayList<T2File>();
		addFile = (List<T2File>) fromJson(FileSeqs, collectionType);
		Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
		ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);
		
		List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
		
		Type collectionType4 = new TypeToken<List<Project>>() {}.getType();
		List<Project> srcAddList = new ArrayList<Project>();
		srcAddList = (List<Project>) fromJson(srcAddListDataString, collectionType4);
		
		if (CoConstDef.FLAG_NO.equals(identificationSubStatusSrc)) {
			Project project = new Project();
			project.setIdentificationSubStatusSrc(identificationSubStatusSrc);
			project.setPrjId(prjId);
			project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_SRC);
			projectService.updateSubStatus(project);
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
			 * // return validator result if (!vr.isValid()) { return
			 * makeJsonResponseHeader(vr.getValidMessageMap()); }
			 */
			// save 시 가장 기본적인 유효성 체크만 진행 (길이, 형식 체크)
			T2CoProjectValidator pv = new T2CoProjectValidator();
			//pv.setIgnore("LICENSE_NAME"); // 예외처리를 추가함.
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
			pv.setValidLevel(pv.VALID_LEVEL_BASIC);
			pv.setAppendix("mainList", ossComponents); // sub grid
			pv.setAppendix("subList", ossComponentsLicense);
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if (!vr.isValid()) {
				if (CommonFunction.booleanValidationFormatForValidMsg(vr.getValidMessageMap(), true)) {
					return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap(), ossComponents), vr.getValidMessageMap());
				} else if (CommonFunction.booleanValidationFormatForValidMsg(vr.getValidMessageMap(), false)) {
					List<ProjectIdentification> exceedingMaxLengthList = CommonFunction.getItemsExceedingMaxLength(vr.getValidMessageMap(), ossComponents);
					if (!CollectionUtils.isEmpty(exceedingMaxLengthList)) {
						return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), exceedingMaxLengthList);
					} else {
						return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
					}
				} else {
					return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
				}
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
			
			projectService.registSrcOss(ossComponents, ossComponentsLicense, project);
			
			// 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
			if (addFile != null) {
				for (T2File _file : addFile) {
					if (!isEmpty(_file.getFileSeq())) {
						try {
							if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
									CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, _file.getFileSeq())) != null) {
								String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(
										loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE,
										_file.getFileSeq()), true);
								
								if (!isEmpty(changedLicenseName)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("SRC");
									commentHisBean.setContents(changedLicenseName);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
						
						try {
							if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, _file.getFileSeq())) != null) {
								String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, _file.getFileSeq()), true);
								
								if (!isEmpty(chagedOssVersion)) {
									CommentsHistory commentHisBean = new CommentsHistory();
									commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentHisBean.setReferenceId(prjId);
									commentHisBean.setExpansion1("SRC");
									commentHisBean.setContents(chagedOssVersion);
									commentService.registComment(commentHisBean, false);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}

			// oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
			try {
				if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_SRC)) != null) {
					String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
							CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_SRC), true);
					
					if (!isEmpty(changedLicenseName)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setExpansion1("SRC");
						commentHisBean.setContents(changedLicenseName);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			try {
				History h = new History();
				h = projectService.work(project);
				h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
				project = (Project) h.gethData();
				h.sethEtc(project.etcStr());
				historyService.storeData(h);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		Project project = new Project();
		project.setPrjId(prjId);
		project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_SRC);
		projectService.existsAddList(project);
		projectService.insertAddList(srcAddList);
		
		// 정상처리된 경우 세션 삭제
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));

		// success code set 10
		resCd = "10";
		resMap.put("isValid", String.valueOf(isValid));
		resMap.put("resCd", resCd);
		resMap.put("resultData", projectService.getProjectDetail(project).getIdentificationStatus());
		
		return makeJsonResponseHeader(resMap);
	}
	
	/**
	 * Nick name valid.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @param code the code
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.NICKNAME_CD)
	public @ResponseBody ResponseEntity<Object> nickNameValid(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String code) {
		Map<String, List<String>> result = null;

		String mainDataString = (String) map.get("mainData");
		String prjId = (String) map.get("prjId");

		Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> ossComponent = new ArrayList<ProjectIdentification>();
		ossComponent = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);
		
		List<List<ProjectIdentification>> ossComponentLicense = null;
		if (code.equals(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER)) {
			ossComponentLicense = CommonFunction.setOssComponentLicense(ossComponent, true);
		} else {
			ossComponentLicense = CommonFunction.setOssComponentLicense(ossComponent, false, true);
		}
		
		ossComponentLicense = CommonFunction.mergeGridAndSession(CommonFunction.makeSessionKey(loginUserName(), code, prjId), ossComponent, ossComponentLicense, CommonFunction.makeSessionReportKey(loginUserName(), code, prjId));
		result = projectService.nickNameValid(prjId, ossComponent, ossComponentLicense);

		StringBuffer resultSb = new StringBuffer();
		if (result != null) {
			boolean hasOssNick = false;
			boolean hasLicenseNick = false;
			List<String> ossNickList = result.get("OSS");
			List<String> licenseNickList = result.get("LICENSE");

			if (!CollectionUtils.isEmpty(ossNickList) || !CollectionUtils.isEmpty(licenseNickList)) {
				resultSb.append("<p><b>" + getMessage("msg.oss.changed.by.system") + "</b></p>");
				if (!CollectionUtils.isEmpty(ossNickList)) {
					resultSb.append(CommonFunction.changeDataToTableFormat("oss", "", ossNickList));
				}

				if (!CollectionUtils.isEmpty(licenseNickList)) {
					if (!CollectionUtils.isEmpty(ossNickList)) {
						resultSb.append("<br>");
					}
					resultSb.append(CommonFunction.changeDataToTableFormat("license", "", licenseNickList));
				}

				putSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, code), resultSb.toString());

				return makeJsonResponseHeader(true, resultSb.toString());
			}
		}

		return makeJsonResponseHeader();
	}
	
	/**
	 * Save bin android.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	@PostMapping(value = PROJECT.SAVE_BINANDROID)
	public @ResponseBody ResponseEntity<Object> saveBinAndroid(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		// default validation
		boolean isValid = true;
		// last response map
		Map<String, String> resMap = new HashMap<>();
		// default 00:java error check code, 10:success code
		String resCd = "00";
		String prjId = (String) map.get("prjId");
		String androidCsvFileId = (String) map.get("androidCsvFileId");
		String androidNoticeFileId = (String) map.get("androidNoticeFileId");
		String androidNoticeXmlId = (String) map.get("androidNoticeXmlId");
		String androidResultFileId = (String) map.get("androidResultFileId");
		String identificationSubStatusAndroid = (String) map.get("identificationSubStatusAndroid");
		String loadFromAndroidProjectFlag = (String) map.get("loadFromAndroidProjectFlag");
		String mainDataString = (String) map.get("mainData");

		Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {
		}.getType();
		List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
		ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);
		
		List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
		
		try {
			String fileId = null;
			
			if (!isEmpty(androidNoticeXmlId)) {
				fileId = androidNoticeXmlId;
			}
			
			if (isEmpty(androidNoticeXmlId) 
					&& !isEmpty(androidNoticeFileId)) {
				fileId = androidNoticeFileId;
			}
			
			T2File noticeFile = fileService.selectFileInfoById(fileId);
			List<String> noticeBinaryList = CommonFunction.getAndroidNoticeBinaryList(
					FileUtils.readFileToString(new File(noticeFile.getLogiPath() + "/" + noticeFile.getLogiNm())));
			
			Map<String, Object> syncMap = projectService.applySrcAndroidModel(ossComponents, noticeBinaryList);
			ossComponents = (List<ProjectIdentification>) syncMap.get("reportData");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		Project prj = new Project();
		prj.setPrjId(prjId);
		
		if (CoConstDef.FLAG_NO.equals(identificationSubStatusAndroid)) {
			prj.setIdentificationSubStatusAndroid(identificationSubStatusAndroid);
			prj.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
			projectService.updateSubStatus(prj);
		} else {
			T2CoProjectValidator pv = new T2CoProjectValidator();
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);
			pv.setValidLevel(pv.VALID_LEVEL_BASIC);
			pv.setAppendix("mainList", ossComponents); // sub grid
			pv.setAppendix("subList", ossComponentsLicense);
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			
			if (!vr.isValid()) {
				return makeJsonResponseHeader(false, CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
			}

			Project project = new Project();
			project.setPrjId(prjId);
			project.setSrcAndroidCsvFileId(androidCsvFileId);
			project.setSrcAndroidNoticeFileId(androidNoticeFileId);
			project.setSrcAndroidNoticeXmlId(androidNoticeXmlId);
			project.setSrcAndroidResultFileId(androidResultFileId);
			project.setIdentificationSubStatusAndroid(identificationSubStatusAndroid);
			
			if (CoConstDef.FLAG_YES.equals(loadFromAndroidProjectFlag)) {
				project.setLoadFromAndroidProjectFlag(loadFromAndroidProjectFlag);
			}

			projectService.registSrcOss(ossComponents, ossComponentsLicense, project,
					CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
			
			binaryDataService.autoIdentificationWithAndroidResultTextFile(project);
			
			try {
				if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, androidResultFileId)) != null) {
					String changedByResultTxt = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
							CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, androidResultFileId), true);
					
					if (!isEmpty(changedByResultTxt)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setExpansion1("BIN(Android)");
						commentHisBean.setContents(changedByResultTxt);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			// 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
			try {
				if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, androidCsvFileId)) != null) {
					String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
							CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, androidCsvFileId), true);
					
					if (!isEmpty(changedLicenseName)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setExpansion1("BIN(Android)");
						commentHisBean.setContents(changedLicenseName);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			// oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
			try {
				if (getSessionObject(
						CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId,
								CoConstDef.CD_DTL_COMPONENT_ID_ANDROID)) != null) {
					String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
							CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID), true);
					
					if (!isEmpty(changedLicenseName)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setExpansion1("BIN(Android)");
						commentHisBean.setContents(changedLicenseName);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			try {
				if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, androidCsvFileId)) != null) {
					String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, androidCsvFileId), true);
					if (!isEmpty(chagedOssVersion)) {
						CommentsHistory commentHisBean = new CommentsHistory();
						commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
						commentHisBean.setReferenceId(prjId);
						commentHisBean.setExpansion1("BIN(Android)");
						commentHisBean.setContents(chagedOssVersion);
						commentService.registComment(commentHisBean, false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			try {
				History h = new History();
				h = projectService.work(project);
				h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
				project = (Project) h.gethData();
				h.sethEtc(project.etcStr());
				historyService.storeData(h);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		// session 정보 삭제
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, prjId));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID, prjId));
		
		// success code set 10
		resCd = "10";
		resMap.put("isValid", String.valueOf(isValid));
		resMap.put("resCd", resCd);
		resMap.put("resultData", projectService.getProjectDetail(prj).getIdentificationStatus());
		
		return makeJsonResponseHeader(resMap);
	}
	
	/**
	 * Save bom.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_BOM)
	public @ResponseBody ResponseEntity<Object> saveBom(@RequestBody Map<String, Object> map, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		String prjId = (String) map.get("referenceId");
		String merge = (String) map.get("merge");
		String gridString = (String) map.get("gridData");
		String checkGridString = (String) map.get("checkGridData");
		
		// bom에서 admin check선택한 data
		Type collectionType = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> projectIdentification = new ArrayList<>();
		projectIdentification = (List<ProjectIdentification>) fromJson(gridString, collectionType);
		List<ProjectIdentification> checkGridBomList = new ArrayList<>();
		checkGridBomList = (List<ProjectIdentification>) fromJson(checkGridString, collectionType);
		projectService.registBom(prjId, merge, projectIdentification, checkGridBomList);
		projectService.updateSecurityDataForProject(prjId);
		Map<String, String> resMap = new HashMap<>();
		
		try {
			Project param = new Project();
			param.setPrjId(prjId);
			Project pDat = projectService.getProjectDetail(param);
			resMap.put("identificationStatus", pDat.getIdentificationStatus());
			History h = projectService.work(pDat);
			h.sethAction(CoConstDef.ACTION_CODE_NEEDED);
			historyService.storeData(h); // 메일로 보낼 데이터를 History에 저장합니다. -> h.gethData()로 확인 가능
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader(resMap);
	}
	
	/**
	 * [API] 프로젝트 상태 변경.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@PostMapping(value = PROJECT.UPDATE_PROJECT_STATUS)
	public @ResponseBody ResponseEntity<Object> updateProjectStatus(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		log.info("URI: "+ "/project/updateProjectStatus");
		log.debug("PARAM: " + "useCustomNoticeYn="+project.getUseCustomNoticeYn());
		/* 2018-07-27 choye 추가 */
		String resCd = "";
		String resMsg = "";

		if (project.getPrjId() == null || "".equals(project.getPrjId())) {
			return makeJsonResponseHeader(false, "prjId is empty", null);
		}
		
		if (project.getDelOsdd()!=null && (project.getDelOsdd().equals(CoConstDef.FLAG_YES) || project.getDelOsdd().equals(CoConstDef.FLAG_NO))) {
			project.setCompleteYn(CoConstDef.FLAG_NO);
			project.setStatusRequestYn(CoConstDef.FLAG_NO);
			project.setCommId(null);
			project.setUserComment(project.getUserComment());
			project.setIgnoreUserCommentReg(CoConstDef.FLAG_YES);
			
			resMsg = getMessage("msg.distribute.reset");
			if ("10".equals(resCd)){
				
			}	
			else if ("20".equals(resCd)){
				resMsg="server error.";
			} else{
				resMsg = resCd;
			}
			
			if (!resCd.equals("10")) {
				return makeJsonResponseHeader(false, resMsg, null);
			}
		}
		
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			 resultMap = projectService.updateProjectStatus(project, false, false);
			 
			 if (resultMap.containsKey("androidMessage")) {
				 return makeJsonResponseHeader(false, getMessage("msg.project.android.valid"));
			 }
			 
			 if (resultMap.containsKey("diffMap")) {
				 return makeJsonResponseHeader(false, null, (Map<String, Object>) resultMap.get("validMap"), (Map<String, Object>) resultMap.get("diffMap"));
			 }
			 
			 if (resultMap.containsKey("validMap")) {
				 return makeJsonResponseHeader(false, null, (Map<String, Object>) resultMap.get("validMap"));
			 }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		updateProjectNotification(project, resultMap);
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * [화면] Identification 화면.
	 *
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the string
	 * @throws Exception the exception
	 */
	@GetMapping(value = PROJECT.IDENTIFICATION_ID_DIV, produces = "text/html; charset=utf-8")
	public String identification(HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId,
			@PathVariable String initDiv) throws Exception {
		Project project = new Project();
		project.setPrjId(prjId);
		project.setActType(CoConstDef.FLAG_NO);
		Project projectMaster = projectService.getProjectDetail(project);
		
		boolean partnerFlag = CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES);
		CommentsHistory comHisBean = new CommentsHistory();
		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICATION_USER);
		comHisBean.setReferenceId(projectMaster.getPrjId());
		projectMaster.setUserComment(commentService.getUserComment(comHisBean));
		projectMaster.setNoticeTypeEtc(CommonFunction.tabTitleSubStr(CoConstDef.CD_PLATFORM_GENERATED, projectMaster.getNoticeTypeEtc()));
		
		// 프로젝트 정보
		model.addAttribute("project", projectMaster);
		// android model이면서 bom화면을 표시하려고하는 경우, android bin tab index로 치환한다.
		
		if (!"4".equals(initDiv) && CoConstDef.FLAG_YES.equals(projectMaster.getAndroidFlag())) {
			initDiv = "4";
		}
		
		if (!partnerFlag && "0".equals(initDiv)) {
			initDiv = "2";
		}
		
		String isNew = CoConstDef.FLAG_NO;
		if (CoConstDef.FLAG_YES.equals(projectMaster.getAndroidFlag())) {
			if (isEmpty(projectMaster.getIdentificationSubStatusAndroid())) {
				isNew = CoConstDef.FLAG_YES;
			}
		} else {
			if (isEmpty(projectMaster.getIdentificationSubStatusPartner())
					&& isEmpty(projectMaster.getIdentificationSubStatusDep())
					&& isEmpty(projectMaster.getIdentificationSubStatusSrc())
					&& isEmpty(projectMaster.getIdentificationSubStatusBin())
					&& isEmpty(projectMaster.getIdentificationSubStatusBom())) {
				isNew = CoConstDef.FLAG_YES;
			}
		}
		
		model.addAttribute("editMode", isNew);
		model.addAttribute("initDiv", initDiv);
		model.addAttribute("autoAnalysisFlag", CommonFunction.propertyFlagCheck("autoanalysis.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", partnerFlag);
		model.addAttribute("newPrjFlag", isEmpty(projectMaster.getIdentificationStatus()) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
		
		Map<String, Object> btnShowMap = new HashMap<>();
		CommonFunction.getDisplayIdentificationBtn(avoidNull(project.getIdentificationStatus()), avoidNull(project.getViewOnlyFlag()), btnShowMap);
		model.addAttribute("display", btnShowMap);
		
		if (initDiv.equals("4")) {
			return "project/identification-android";
		} else {
			return "project/identification";
		}
	}
	
	@PostMapping(value = PROJECT.IDENTIFICATION_ID_DIV_MODE, produces = "text/html; charset=utf-8")
	public String identification(HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId,
			@PathVariable String initDiv, @PathVariable String mode) throws Exception {
		Project project = new Project();
		project.setPrjId(prjId);
		Project projectMaster = projectService.getProjectDetail(project);
		
		// 프로젝트 정보
		model.addAttribute("project", projectMaster);
		model.addAttribute("initDiv", initDiv);
		model.addAttribute("mode", mode);
		
		if (initDiv.equalsIgnoreCase("third")) {
			if (mode.equals("view")) {
				return "project/fragments/view :: partyFragments";
			} else {
				return "project/fragments/edit :: partyFragments";
			}
		} else if (initDiv.equalsIgnoreCase("dep")) {
			if (mode.equals("view")) {
				return "project/fragments/view :: depFragments";
			} else {
				return "project/fragments/edit :: depFragments";
			}
		} else if (initDiv.equalsIgnoreCase("src")) {
			if (mode.equals("view")) {
				return "project/fragments/view :: srcFragments";
			} else {
				return "project/fragments/edit :: srcFragments";
			}
		} else if (initDiv.equalsIgnoreCase("bin")) {
			if (mode.equals("view")) {
				return "project/fragments/view :: binFragments";
			} else {
				return "project/fragments/edit :: binFragments";
			}
		} else if (initDiv.equalsIgnoreCase("android")) {
			if (mode.equals("view")) {
				return "project/fragments/view :: binandroidFragments";
			} else {
				return "project/fragments/edit :: binandroidFragments";
			}
		} else {
			if (mode.equals("view")) {
				return "project/fragments/view :: batFragments";
			} else {
				return "project/fragments/edit :: batFragments";
			}
		}
	}
	
	/**
	 * Src main grid ajax post.
	 *
	 * @param identification the identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.IDENTIFICATION_GRID_POST)
	public @ResponseBody ResponseEntity<Object> srcMainGridAjaxPost(@RequestBody ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		Map<String, Object> result = getOssComponentDataInfo(identification, identification.getReferenceDiv());
		if (result != null) CommonFunction.setDeduplicatedMessageInfo(result);
		return makeJsonResponseHeader(result);
	}
	
	/**
	 * [API] Identification 공통 메인 조회.
	 * Identification 각 Tab에 저장되어있는 SRC / BIN / BIN(Android) / BOM 정보를 반환한다.
	 *
	 * @param identification the identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @param prjId the prj id
	 * @param code the code
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.IDENTIFICATION_MERGED_GRID_ID_CD)
	public @ResponseBody ResponseEntity<Object> srcMainGridMergedAjax(@RequestBody ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId,
			@PathVariable String code) {
		// session key 초기화
		// referenceId에 해당하는 모든 report (분석결과서) 세션 정보를 초기화 한다.
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PARTNER,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BAT,
				identification.getReferenceId()));
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_BAT,
				identification.getReferenceId()));
		
		// 요청한 reference의 이전 정보도 삭제
		deleteSession(CommonFunction.makeSessionKey(loginUserName(), identification.getReferenceDiv(),
				identification.getReferenceId()));
		Map<String, Object> result = getOssComponentDataInfo(identification, code);
		
		if (result != null) {
			CommonFunction.setDeduplicatedMessageInfo(result);
			// Project Identification에서 BAT Apply 인 경우 (BAT List 와
			// Identification의 BAT 를 구분
			if (CoConstDef.CD_DTL_COMPONENT_BAT.equals(code) && isEmpty(identification.getReferenceId())
					&& !isEmpty(identification.getRefBatId())) {
				code = CoConstDef.CD_DTL_COMPONENT_ID_BAT;
			}
			
			putSessionObject(CommonFunction.makeSessionKey(loginUserName(), code, prjId), result);
		}
		
		return makeJsonResponseHeader(result);
	}
	
	/**
	 * identification project.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @param code the code
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.IDENTIFICATION_CD)
	public @ResponseBody ResponseEntity<Object> identificationProject(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model, @PathVariable String code) {
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		project.setCurPage(page);
		project.setPageListSize(rows);
		project.setSortField(sidx);
		project.setSortOrder(sord);
		project.setCode(code);

		Map<String, Object> map = projectService.getIdentificationProject(project);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * List ajax.
	 *
	 * @param ossComponents the oss components
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@RequestMapping(value = PROJECT.TRD_OSS)
	public @ResponseBody ResponseEntity<Object> listAjax(OssComponents ossComponents, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		ossComponents.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
		Map<String, Object> map = projectService.getPartnerOssList(ossComponents);
		projectService.setLoadToList(map, ossComponents.getReferenceId());
		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Third project.
	 *
	 * @param projectIdentification the project identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @param code the code
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.IDENTIFICATION_PROJECT_SEARCH_CD)
	public @ResponseBody ResponseEntity<Object> thirdProject(ProjectIdentification projectIdentification,
			HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String code) {
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		projectIdentification.setCurPage(page);
		projectIdentification.setPageListSize(rows);
		projectIdentification.setSortField(sidx);
		projectIdentification.setSortOrder(sord);
		projectIdentification.setReferenceDiv(code);
		
		Map<String, Object> map = projectService.getIdentificationProjectSearch(projectIdentification);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Project Identification > 3rd oss list 취득.
	 *
	 * @param ossComponents the oss components
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@GetMapping(value = PROJECT.IDENTIFICATION_THIRD)
	public @ResponseBody ResponseEntity<Object> identificationThird(OssComponents ossComponents, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, Object> map = projectService.getIdentificationThird(ossComponents);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Android sheet name.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.ANDROID_SHEET_NAME)
	public @ResponseBody ResponseEntity<Object> androidSheetName(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		List<UploadFile> list = new ArrayList<UploadFile>();
		list = projectService.selectAndroidFileDetail(project);
		List<Object> sheetNameList = null;
		
		try {
			sheetNameList = ExcelUtil.getSheetNames(list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return makeJsonResponseHeader(sheetNameList);
	}
	
	/**
	 * Gets the oss id check.
	 *
	 * @param projectIdentification the project identification
	 * @param model the model
	 * @return the oss id check
	 */
	@GetMapping(value = PROJECT.OSS_ID_CHECK)
	public @ResponseBody ResponseEntity<Object> getOssIdCheck(
			@ModelAttribute ProjectIdentification projectIdentification, Model model) {
		Map<String, Object> map = projectService.getOssIdCheck(projectIdentification);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Gets the check change data.
	 *
	 * @param map the map
	 * @return the check change data
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.CHECK_CHANGE_DATA)
	public @ResponseBody ResponseEntity<Object> getCheckChangeData(@RequestBody HashMap<String, Object> map) {
		String prjId = (String) map.get("referenceId");
		String partyGrid = (String) map.get("partyGrid");
		String srcMainGrid = (String) map.get("srcMainGrid");
		String binMainGrid = (String) map.get("binMainGrid");
		String status = (String) map.get("status");

		// party
		Type partyType = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> partyData = new ArrayList<ProjectIdentification>();
		partyData = (List<ProjectIdentification>) fromJson(partyGrid, partyType);

		// src
		Type srcType = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> srcData = new ArrayList<ProjectIdentification>();
		srcData = (List<ProjectIdentification>) fromJson(srcMainGrid, srcType);
		if (!CollectionUtils.isEmpty(srcData)) {
			srcData.sort(Comparator.comparing(ProjectIdentification::getComponentId));
		}
		
		List<List<ProjectIdentification>> srcSubData = CommonFunction.setOssComponentLicense(srcData);
		
		if(srcSubData != null && !srcSubData.isEmpty()) {
			srcSubData = CommonFunction.mergeGridAndSession(
					CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId), srcData,
					srcSubData,
					CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));
		}		// bin
		Type binType = new TypeToken<List<ProjectIdentification>>() {
		}.getType();
		List<ProjectIdentification> binData = new ArrayList<ProjectIdentification>();
		binData = (List<ProjectIdentification>) fromJson(binMainGrid, binType);
		if (!CollectionUtils.isEmpty(binData)) {
			binData.sort(Comparator.comparing(ProjectIdentification::getComponentId));
		}
		
		List<List<ProjectIdentification>> binSubData = CommonFunction.setOssComponentLicense(binData);
		
		if(binSubData != null && !binSubData.isEmpty()) {
			binSubData = CommonFunction.mergeGridAndSession(
					CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_BIN, prjId), binData,
					binSubData,
					CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN, prjId));
		}
		// 체크 서비스 호출
		String errMsg = projectService.checkChangedIdentification(prjId, partyData, srcData, srcSubData, binData,
				binSubData, (String) map.get("applicableParty"), (String) map.get("applicableSrc"),
				(String) map.get("applicableBin"));
		
		if (!isEmpty(errMsg)) {
			return makeJsonResponseHeader(false, errMsg);
		}

		if (!isEmpty(status) && CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(status.toUpperCase())){
			ProjectIdentification identification = new ProjectIdentification();
			identification.setReferenceId(prjId);
			identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
			identification.setMerge(CoConstDef.FLAG_YES);
			errMsg = projectService.checkOssNicknameList(identification);
			if (!isEmpty(errMsg)) {
				return makeJsonResponseHeader(false, errMsg);
			}
		}
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Calcel file del src.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.CANCEL_FILE_DEL_SRC)
	public @ResponseBody ResponseEntity<Object> cancelFileDelSrc(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		// default validation
		boolean isValid = true;
		// last response map
		Map<String, String> resMap = new HashMap<>();
		// default 00:java error check code, 10:success code
		String resCd = "00";
		String delFileString = (String) map.get("csvDelFileIds");

		Type collectionType = new TypeToken<List<T2File>>() {}.getType();
		List<T2File> delFile = new ArrayList<T2File>();
		delFile = (List<T2File>) fromJson(delFileString, collectionType);

		if (delFile.size() > 0) {
			try {
				Project project = new Project();
				project.setCsvFile(delFile);
				projectService.cancelFileDel(project);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			// success code set 10
			resCd = "10";
			resMap.put("isValid", String.valueOf(isValid));
			resMap.put("resCd", resCd);
		}

		return makeJsonResponseHeader(resMap);
	}
	
	/**
	 * Gets the file info.
	 *
	 * @param identification the identification
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the file info
	 */
	@GetMapping(value = PROJECT.FILE_INFO)
	public @ResponseBody ResponseEntity<Object> getFileInfo(@ModelAttribute ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		Map<String, Object> result = projectService.getFileInfo(identification);

		return makeJsonResponseHeader(result);
	}
	
	/**
	 * Gets the 3 rd map.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the 3 rd map
	 */
	@GetMapping(value = PROJECT.TRD_MAP)
	public @ResponseBody ResponseEntity<Object> get3rdMap(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, Object> map = null;
		
		try {
			map = projectService.get3rdMapList(project);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Adds the watcher.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.ADD_WATCHER)
	public @ResponseBody ResponseEntity<Object> addWatcher(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// addWatcher로 email을 등록할 경우 ldap search로 존재하는 사용자의 email인지 check가 필요함.
			String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
			if (CoConstDef.FLAG_YES.equals(ldapFlag) && !isEmpty(project.getPrjEmail())) {
				Map<String, String> userInfo = new HashMap<>();
				userInfo.put("USER_ID", CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_ID));
				userInfo.put("USER_PW", CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_PW));
				
				String filter = project.getPrjEmail();
				
				boolean isAuthenticated = userService.checkAdAccounts(userInfo, "USER_ID", "USER_PW", filter);
				
				if (!isAuthenticated) {
					throw new Exception("add Watcher Failure");
				}
				
				String email = (String) userInfo.get("EMAIL");
				project.setPrjEmail(email);
				
				// 사용자가 입력한 domain과 ldap search를 통해 확인된 domain이 다를 수 있기때문에 ldap search에서 확인된 domain을 우선적으로 처리함.
				resultMap.put("email", email);
			}
			
			if (!isEmpty(project.getPrjUserId()) || !isEmpty(project.getPrjEmail())) {
				String addWatcher = projectService.addWatcher(project);
				resultMap.put("isValid", "true");
				resultMap.put("addWatcher", addWatcher);
			} else {
				return makeJsonResponseHeader(false, null);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader(resultMap);
	}
	
	/**
	 * Adds the watchers.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.ADD_WATCHERS)
	public @ResponseBody ResponseEntity<Object> addWatchers(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
			Project param = new Project();
			
			for (Map<String, String> changeWatcher : project.getChangeWatcherList()) {
				String prjEmail = changeWatcher.get("prjEmail");
				
				if (CoConstDef.FLAG_YES.equals(ldapFlag) && !isEmpty(prjEmail)) {
					Map<String, String> userInfo = new HashMap<>();
					userInfo.put("USER_ID", CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_ID));
					userInfo.put("USER_PW", CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_PW));
					
					boolean isAuthenticated = userService.checkAdAccounts(userInfo, "USER_ID", "USER_PW", prjEmail);
					
					if (!isAuthenticated) {
						throw new Exception("add Watcher Failure");
					}
				}
				
				param.setPrjUserId(changeWatcher.get("prjUserId"));
				param.setPrjDivision(changeWatcher.get("prjDivision"));
				param.setPrjEmail(prjEmail);
				
				for (String prjId : project.getPrjIds()) {
					if (!isEmpty(param.getPrjUserId()) || !isEmpty(param.getPrjEmail())) {
						param.setPrjId(prjId);
						projectService.addWatcher(param);
					}
				}
			}
			
			resultMap.put("isValid", "true");
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader(resultMap);
	}
	
	/**
	 * Removes the watcher.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.REMOVE_WATCHER)
	public @ResponseBody ResponseEntity<Object> removeWatcher(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		try {
			if (!isEmpty(project.getPrjUserId()) || !isEmpty(project.getPrjEmail())) {
				projectService.removeWatcher(project);
			} else {
				return makeJsonResponseHeader(false, null);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Removes the watcher.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.REMOVE_WATCHERS)
	public @ResponseBody ResponseEntity<Object> removeWatchers(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			Project param = new Project();
			
			for (Map<String, String> changeWatcher : project.getChangeWatcherList()) {
				String prjUserId = changeWatcher.get("prjUserId");
				String prjEmail = changeWatcher.get("prjEmail");
				
				if (!isEmpty(prjUserId) || !isEmpty(prjEmail)) {
					param.setPrjUserId(prjUserId);
					param.setPrjDivision(changeWatcher.get("prjDivision"));
					param.setPrjEmail(prjEmail);
					
					for (String prjId : project.getPrjIds()) {
						param.setPrjId(prjId);
						projectService.removeWatcher(param);
					}
				}
			}
			
			resultMap.put("isValid", "true");
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader(resultMap);
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
	@PostMapping(value = PROJECT.COPY_WATCHER)
	public @ResponseBody ResponseEntity<Object> copyWatcher(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		HashMap<String, Object> resMap = new HashMap<>();
		
		try {			
			if (!isEmpty(project.getListKind()) && !isEmpty(project.getListId()) ) {
				List<Project> result = projectService.copyWatcher(project);
				
				if (result != null) {
					for (Project bean : result) {
						if (!isEmpty(bean.getPrjDivision())) {
							bean.setPrjDivisionName(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, bean.getPrjDivision()));
						}
					}
					
					if (isEmpty(project.getCopyWatcherLocation()) && !isEmpty(project.getPrjId())) {
						boolean existProjectWatcher = projectService.existsWatcher(project);
						
						for (Project pm : result) {
							pm.setPrjId(project.getPrjId());
							
							if (existProjectWatcher) {
								projectService.addWatcher(pm);
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
	
	/**
	 * saveModelAjax
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_MODEL_AJAX)
	public @ResponseBody ResponseEntity<Object> saveModelAjax(@ModelAttribute Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		try {
			String jsonString = project.getPrjModelJson();
			Type collectionType = new TypeToken<List<Project>>() {}.getType();
			List<Project> list = (List<Project>) fromJson(jsonString, collectionType);
			project.setModelList(list);
			projectService.insertProjectModel(project);
		} catch (Exception e) {
			return makeJsonResponseHeader(false, null);
		}
		
		return makeJsonResponseHeader();
	}
	
	@GetMapping(value = PROJECT.PROJECT_TO_ADD_LIST)
	public @ResponseBody ResponseEntity<Object> projectToAddList(@ModelAttribute OssComponents ossComponents, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, Object> map = projectService.getProjectToAddList(ossComponents);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Gets the 3 rd map.
	 *
	 * @param project the project
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the 3 rd map
	 */
	@GetMapping(value = PROJECT.ADD_LIST)
	public @ResponseBody ResponseEntity<Object> getSrcAddList(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, Object> map = null;
		
		try {
			map = projectService.getAddList(project);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Gets the partner list.
	 *
	 * @param partnerMaster the partner master
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the partner list
	 */
	@GetMapping(value = PROJECT.PARTNER_LIST)
	public @ResponseBody ResponseEntity<Object> getPartnerList(PartnerMaster partnerMaster, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, Object> map = null;
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		partnerMaster.setCurPage(page);
		partnerMaster.setPageListSize(rows);
		partnerMaster.setSortField(sidx);
		partnerMaster.setSortOrder(sord);
		
		try {
			map = projectService.getPartnerList(partnerMaster);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return makeJsonResponseHeader(map);
	}
	
	/**
	 * Csv file.
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
	@PostMapping(value = PROJECT.CSV_FILE)	
	public String csvFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {
		ArrayList<Object> resultList = new ArrayList<Object>();
		// 파일등록
		List<UploadFile> list = new ArrayList<UploadFile>();
		String fileId = req.getParameter("registFileId");
		file.setTabGubn(req.getParameter("tabNm"));
		log.info("tabNm ==> " + req.getParameter("tabNm"));

		Map<String, MultipartFile> fileMap = req.getFileMap();
		String fileExtension = StringUtils.getFilenameExtension(fileMap.get("myfile").getOriginalFilename());
		
		// 파일 등록
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

			if (fileExtension.equals("csv")) {
				resultList = CommonFunction.checkCsvFileLimit(list);
			} else {
				resultList = CommonFunction.checkXlsxFileLimit(list);
			}
			
			if (resultList.size() > 0) {
				return toJson(resultList);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if (fileExtension.equals("csv")) {
			resultList.add(list);
			resultList.add("SRC");
			resultList.add("CSV_FILE");
			return toJson(resultList);
		} else {
			// sheet이름
			List<Object> sheetNameList = null;

			try {
				sheetNameList = ExcelUtil.getSheetNames(list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
			} catch (Exception e) {
				log.error(e.getMessage());
			}

			Boolean isSpdxSpreadsheet = false;
			for (Object sheet : sheetNameList) {
				String sheetName = sheet.toString();
				if (sheetName.contains("Package Info") || sheetName.contains("Per File Info")) {
					isSpdxSpreadsheet = true;
				}
			}

			if (isSpdxSpreadsheet){
				resultList.add(list);
				resultList.add(sheetNameList);
				resultList.add("SPDX_SPREADSHEET_FILE");
			}
			else {
				resultList.add(list);
				resultList.add(sheetNameList);
				resultList.add("EXCEL_FILE");
			}
			// 결과값 resultList에 담기
			return toJson(resultList);
		}
	}
	
	/**
	 * Bin csv file.
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
	@PostMapping(value = PROJECT.BIN_CSV_FILE)
	public String binCsvFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {

		String fileType = req.getParameter("fileType");
		ArrayList<Object> resultList = new ArrayList<Object>();
		// 파일등록
		List<UploadFile> list = new ArrayList<UploadFile>();
		String fileId = req.getParameter("registFileId");

		Map<String, MultipartFile> fileMap = req.getFileMap();
		String fileExtension = StringUtils.getFilenameExtension(fileMap.get("myfile").getOriginalFilename());
		
		// 파일 등록
		try {
			if (req.getContentType() != null && req.getContentType().toLowerCase().indexOf("multipart/form-data") > -1) {
				file.setCreator(loginUserName());
				if (fileId == null) {
					list = fileService.uploadFile(req, file);
				} else {
					list = fileService.uploadFile(req, file, null, fileId);
				}
			}

			if (fileExtension.equals("csv")) {
				resultList = CommonFunction.checkCsvFileLimit(list);
			} else {
				resultList = CommonFunction.checkXlsxFileLimit(list);
			}
			
			if (resultList.size() > 0) {
				return toJson(resultList);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if ("text".equals(fileType)) {
			resultList.add(list);
			resultList.add(fileType);
			resultList.add("TEXT_FILE");

			return toJson(resultList);
		} else if (fileExtension.equals("csv")) {
			resultList.add(list);
			resultList.add("BIN");
			resultList.add("CSV_FILE");

			return toJson(resultList);
		} else {
			// sheet이름
			List<Object> sheetNameList = null;

			try {
				sheetNameList = ExcelUtil.getSheetNames(list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			
			Boolean isSpdxSpreadsheet = false;
			for (Object sheet : sheetNameList) {
				String sheetName = sheet.toString();
				if (sheetName.contains("Package Info") || sheetName.contains("Per File Info")) {
					isSpdxSpreadsheet = true;
				}
			}
			
			if (isSpdxSpreadsheet){
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
	 * Gets the sheet data.
	 *
	 * @param map the map
	 * @param req            the req
	 * @param res            the res
	 * @param model            the model
	 * @return the sheet data
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SHEET_DATA)
	public @ResponseBody ResponseEntity<Object> getSheetData(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		log.info("Report Read Start");

		try {
			String prjId = (String) map.get("prjId");
			String fileSeq = (String) map.get("fileSeq");
			List<String> sheetNums = (List<String>) map.get("sheetNums");
			String mainDataString = (String) map.get("mainData");
			String subDataString = (String) map.get("subData");
			String readType = (String) map.get("readType");

			List<String> sheetList = new ArrayList<>();
			for (String s : sheetNums) {
				if (!sheetList.contains(s)) {
					sheetList.add(s);
				}
			}
			// 편집중인 Data 격납
			Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {
			}.getType();
			List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
			ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);

			Type collectionType3 = new TypeToken<List<List<ProjectIdentification>>>() {
			}.getType();
			List<List<ProjectIdentification>> ossComponentsLicense = new ArrayList<>();
			ossComponentsLicense = (List<List<ProjectIdentification>>) fromJson(subDataString, collectionType3);

			String errMsg = "";
			List<OssComponents> reportData = new ArrayList<OssComponents>();
			List<String> errMsgList = new ArrayList<>();
			Map<String, String> emptyErrMsg = new HashMap<>();
			try {
				if (!ExcelUtil.readReport(readType, true, sheetList.toArray(new String[sheetList.size()]), fileSeq,
						reportData, errMsgList, emptyErrMsg)) {
					// error 처리
					for (String s : errMsgList) {
						if (isEmpty(s)) {
							continue;
						}
						if (!isEmpty(errMsg)) {
							errMsg += "<br/>";
						}
						errMsg += s;
					}
					return makeJsonResponseHeader(isEmpty(errMsg), errMsg);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return makeJsonResponseHeader(false, e.getMessage());
			}
			
			for (String s : errMsgList) {
				if (isEmpty(s)) {
					continue;
				}
				if (!isEmpty(errMsg)) {
					errMsg += "<br/>";
				}
				errMsg += s;
			}
			
			if (isEmpty(errMsg) && !emptyErrMsg.isEmpty()) {
				errMsg = emptyErrMsg.get("emptyErrMsg");
			}
			
			Map<String, Object> resultMap = CommonFunction.makeGridDataFromReport(ossComponents, ossComponentsLicense, null, reportData, fileSeq, readType);
			
			String sessionKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC;
			if ("bin".equals(readType)) {
				sessionKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN;
			} else if ("android".equals(readType)) {
				sessionKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID;
			} else if ("partner".equals(readType)) {
				sessionKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_PARTNER;
			} else if ("self".equals(readType)) {
				sessionKey = CoConstDef.SESSION_KEY_UPLOAD_REPORT_SELFT_PROJECT;
			}
			
			// license name이 변경된 내용이 있을 경우 사용자 표시
			String systemChangeHisStr = "";
			if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
					CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, fileSeq)) != null) {
				systemChangeHisStr = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, fileSeq));
			}

			if (resultMap.containsKey("versionChangedList")) {
				List<String> versionChangedList = (List<String>) resultMap.get("versionChangedList");
				if (!versionChangedList.isEmpty()) {
					String versionChangedStr = "<b>The following open source version below has been changed to a registered version</b><br><br>";
					for (String s : versionChangedList) {
						versionChangedStr += "<br>" + s;
					}
					
					putSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, fileSeq), versionChangedStr);

					if (!isEmpty(systemChangeHisStr)) {
						systemChangeHisStr += "<br><br>" + versionChangedStr;
					}
				}
			}
			
			resultMap.put("systemChangeHisStr", systemChangeHisStr);
			
			if (!putSessionObject(CommonFunction.makeSessionKey(loginUserName(), sessionKey, prjId), resultMap)) {
				return makeJsonResponseHeader(false, null);
			}
			
			T2CoProjectValidator pv = new T2CoProjectValidator();
			pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);

			pv.setAppendix("mainList", (List<ProjectIdentification>) resultMap.get("mainData"));
			// sub grid
			pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) resultMap.get("subData"));

			T2CoValidationResult vr = pv.validate(new HashMap<>());
			if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
				resultMap.replace("mainData", CommonFunction.identificationSortByValidInfo(
						(List<ProjectIdentification>) resultMap.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false));
				return makeJsonResponseHeader(true, errMsg, resultMap, vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap());
			}
			
			return makeJsonResponseHeader(true, errMsg, resultMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}

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
	@PostMapping(value =PROJECT.SEND_COMMENT)
	public @ResponseBody ResponseEntity<Object> sendComment(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		switch (avoidNull(commentsHistory.getReferenceDiv())) {
			case CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS:
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_ADDED_COMMENT);
				break;
			case CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_HIS:
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_ADDED_COMMENT);
				break;
			case CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS:
			case CoConstDef.CD_DTL_COMMENT_SECURITY_HIS:
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_ADDED_COMMENT);
				break;
			case CoConstDef.CD_DTL_COMMENT_PROJECT_HIS:
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_ADDED_COMMENT);
				break;
			case CoConstDef.CD_DTL_COMMENT_LICENSE:
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_LICENSE_ADDED_COMMENT);
				commentsHistory.setParameter(CommonFunction.getDomain(req));
				break;
			case CoConstDef.CD_DTL_COMMENT_OSS:
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_OSS_ADDED_COMMENT);
				break;
			case CoConstDef.CD_DTL_COMMENT_PARTNER_HIS:
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PARTER_ADDED_COMMENT);
				break;
			default:
				break;
		}
		
		commentService.registComment(commentsHistory);
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Copy.
	 *
	 * @param prjId the prj id
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = PROJECT.COPY_ID, method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "text/html; charset=utf-8")
	public String copy(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) {
		Project project = new Project();
		project.setPrjId(prjId);

		project = projectService.getProjectDetail(project);
		
		String comment = "";
		String identificationStatus = project.getIdentificationStatus();
		String verificationStatus = project.getVerificationStatus();
		boolean isPrjVersion = avoidNull(project.getPrjVersion()).equals("") ? false : true;
		
		project.setPrjName(project.getPrjName());
		project.setPrjVersion(avoidNull(project.getPrjVersion()) + "_Copied");
		
		if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(identificationStatus)) {
			T2Users user = new T2Users();
			user.setUserId(loginUserName());
			
			project.setIdentificationStatusConfFlag(CoConstDef.FLAG_YES);
			project.setPriority(CoConstDef.CD_PRIORITY_P2);
			project.setReviewerName("");
			project.setPrjUserName(userService.getUser(user).getUserName());

			comment = "Copy with confirm status from [PRJ-" + prjId + "] " + project.getPrjName();
			
			if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(verificationStatus)) {
				project.setVerificationStatusConfFlag(CoConstDef.FLAG_YES);
			}
		} else {
			comment = "Copied from [PRJ-" + prjId + "] " + project.getPrjName();
			if (isPrjVersion) {
				comment += "_";
			}
			comment += project.getPrjVersion();
		}
		
		project.setComment(comment);
		project.setDistributeDeployTime(null);
		project.setDistributeDeployYn(null);
		project.setDistributeDeployModelYn(null);
		project.setVerificationStatus(null);
		project.setDistributionStatus(null);
		project.setCopyFlag(CoConstDef.FLAG_YES);
		project.setCompleteYn(CoConstDef.FLAG_NO);
		
		T2Users user = userService.getLoginUserInfo();
		if (user != null && user.getDivision() != null) {
			project.setDivision(user.getDivision());
		}
		
		model.addAttribute("project", project);
		model.addAttribute("copy", project);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
		model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		
		return "project/edit";
	}
	
	/**
	 * Android file.
	 *
	 * @param file the file
	 * @param req the req
	 * @param request the request
	 * @param res the res
	 * @param model the model
	 * @return the string
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@PostMapping(value = PROJECT.ANDROID_FILE)
	public String androidFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {
		String fileType = req.getParameter("fileType");
		ArrayList<Object> resultList = new ArrayList<Object>();
		// 파일등록거
		List<UploadFile> list = new ArrayList<UploadFile>();
		String fileId = req.getParameter("registFileId");
		String prjId = req.getParameter("prjId");
		Project prj = new Project();
		prj.setPrjId(prjId);
		
		// 파일 이름
		int count = 0;
		Iterator<String> fileNames = req.getFileNames();
		String extType = "";
		
		while (fileNames.hasNext()) {
			MultipartFile multipart = req.getFile(fileNames.next());
			String fileName = multipart.getOriginalFilename();
			String[] ext = StringUtil.split(fileName, ".");
			extType = ext[ext.length - 1];
			String type = "";
			
			if ("csv".equals(fileType)) {
				type = "13";
			} else if ("notice".equals(fileType)) {
				type = "14";
			} else {
				type = "19";
			}
			
			String codeExt[] = StringUtil.split(codeMapper.selectExtType(type), ",");
			
			for (int i = 0; i < codeExt.length; i++) {
				if (codeExt[i].equals(extType)) {
					count++;
				}
			}
		}
		
		if (count != 1) {
			String codeExt[] = StringUtil.split(codeMapper.selectExtType(CoConstDef.CD_ANDROID_NOTICE_XML), ",");
			
			for (int i = 0; i < codeExt.length; i++) {
				if (codeExt[i].equals(extType)) {
					count++;
				}
			}
			
			if (count != 1) {
				resultList = null;
			} else {
				// 파일 등록
				if (req.getContentType() != null
						&& req.getContentType().toLowerCase().indexOf("multipart/form-data") > -1) {
					file.setCreator(loginUserName());
					Map<String, Object> rtnMap = fileService.uploadNoticeXMLFile(req, file, fileId, prjId);
					if (rtnMap.containsKey("msg")) {
						return toJson((String) rtnMap.get("msg"));
					} else {
						list = (List<UploadFile>) rtnMap.get("file");
					}
				}
				
				if (!CollectionUtils.isEmpty(list)) {
					prj.setSrcAndroidNoticeXmlId(list.get(0).getRegistFileId());
					prj.setSrcAndroidNoticeFileId(list.get(1).getRegistFileId());
				}

				resultList.add(list);
				resultList.add(fileType);
			}
		} else {
			// 파일 등록
			if (req.getContentType() != null
					&& req.getContentType().toLowerCase().indexOf("multipart/form-data") > -1) {
				file.setCreator(loginUserName());
				
				if (isEmpty(fileId)) {
					list = fileService.uploadFile(req, file);
				} else {
					list = fileService.uploadFile(req, file, null, fileId);
				}
				
				resultList = CommonFunction.checkXlsxFileLimit(list);
				
				if (resultList.size() > 0) {
					return toJson(resultList);
				}
				if ("csv".equals(fileType)) {
					prj.setSrcAndroidCsvFileId(list.get(0).getRegistFileId());
				} else if ("notice".equals(fileType)) {
					prj.setSrcAndroidNoticeFileId(list.get(0).getRegistFileId());
				} else {
					prj.setSrcAndroidResultFileId(list.get(0).getRegistFileId());
				}
			}
			
			resultList.add(list);
			resultList.add(fileType);

		}
		projectService.updateFileId(prj);

		// 결과값 resultList에 담기
		return toJson(resultList);
	}
	
	/**
	 * Android apply.
	 *
	 * @param map the map
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	@PostMapping(value = PROJECT.ANDROID_APPLY)
	public @ResponseBody ResponseEntity<Object> androidApply(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String prjId = (String) map.get("prjId");
		String fileSeq = (String) map.get("androidFileSeq");
		String noticeFileSeq = (String) map.get("androidNoticeFileSeq");
		String resultFileSeq = (String) map.get("androidResultFileSeq");
		List<String> sheetNums = (List<String>) map.get("sheetNums");
		String mainDataString = (String) map.get("mainData");
		String subDataString = (String) map.get("subData");

		try {
			List<String> sheetList = new ArrayList<>();
			
			for (String s : sheetNums) {
				if (!sheetList.contains(s)) {
					sheetList.add(s);
				}
			}
			
			// 편집중인 Data 격납
			Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {
			}.getType();
			List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
			ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType2);

			Type collectionType3 = new TypeToken<List<List<ProjectIdentification>>>() {
			}.getType();
			List<List<ProjectIdentification>> ossComponentsLicense = new ArrayList<>();
			ossComponentsLicense = (List<List<ProjectIdentification>>) fromJson(subDataString, collectionType3);

			String errMsg = "";
			List<OssComponents> reportData = new ArrayList<OssComponents>();
			List<String> errMsgList = new ArrayList<>();
			Map<String, Object> checkHeaderSheetName = new HashMap<String, Object>();
			
			try {
				// 1) build image를 기준으로 oss data mapping (공통)
				if (!ExcelUtil.readAndroidBuildImage("", true, sheetList.toArray(new String[sheetList.size()]), fileSeq,
						resultFileSeq, reportData, errMsgList, checkHeaderSheetName)) {
					// error 처리
					for (String s : errMsgList) {
						if (isEmpty(s)) {
							continue;
						}
						if (!isEmpty(errMsg)) {
							errMsg += "<br/>";
						}
						errMsg += s;
					}
					return makeJsonResponseHeader(false, errMsg);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return makeJsonResponseHeader(false, e.getMessage());
			}
			
			for (String s : errMsgList) {
				if (isEmpty(s)) {
					continue;
				}
				if (!isEmpty(errMsg)) {
					errMsg += "<br/>";
				}
				errMsg += s;
			}

			// result.text에 의해 변경된 내용이 있을 경우 사용자 표시
			// validator에서 session에 격납한다.
			String resultTextChangeHisStr = "";
			
			if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
					CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileSeq)) != null) {
				resultTextChangeHisStr = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileSeq));
			}
			
			// license name이 변경된 내용이 있을 경우 사용자 표시
			String licenseNameChangeHisStr = "";
			
			if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
					CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, fileSeq)) != null) {
				licenseNameChangeHisStr = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
						CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, fileSeq));
			}

			// notice html과 비교 분석
			Map<String, Object> noticeCheckResultMap = null;
			List<String> noticeBinaryList = null;
			List<String> versionChangedList = null;
			
			try {
				T2File noticeFile = fileService.selectFileInfoById(noticeFileSeq);
				String noticeFileName = noticeFile.getLogiNm();
                String fullName = noticeFile.getLogiPath() + "/" + noticeFileName;
                
                if ("xml".equalsIgnoreCase(FilenameUtils.getExtension(noticeFileName))) {
                    noticeBinaryList = CommonFunction.getAndroidNoticeBinaryXmlList(fullName);
                } else {
                    noticeBinaryList = CommonFunction.getAndroidNoticeBinaryList(FileUtils.readFileToString(
                            new File(fullName)));
                }
                
                Map<String, Object> convertObj = CommonFunction.convertToProjectIdentificationList(reportData, fileSeq);
				noticeCheckResultMap = projectService.applySrcAndroidModel((List<ProjectIdentification>)convertObj.get("resultList"), noticeBinaryList);
				
				if (convertObj.containsKey("versionChangeList")) {
					versionChangedList = (List<String>) convertObj.get("versionChangeList");
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return makeJsonResponseHeader(false, "Failed to Notice file parsing");
			}

			// validation

			Map<String, Object> resultMap = CommonFunction.mergeGridAndReport(ossComponents, ossComponentsLicense,
					(List<ProjectIdentification>) noticeCheckResultMap.get("reportData"), "BINADROID");

			String systemChangeHisStr = "";
			
			if (!isEmpty(resultTextChangeHisStr)) {
				systemChangeHisStr = resultTextChangeHisStr;
			}
			
			if (!isEmpty(licenseNameChangeHisStr)) {
				if (!isEmpty(systemChangeHisStr)) {
					systemChangeHisStr += "<br><br>";
				}
				
				systemChangeHisStr += licenseNameChangeHisStr;
			}
			
			if (versionChangedList != null) {
				String versionChangedStr = "<b>The following open source version below has been changed to a registered version</b><br><br>";
				
				for (String s : versionChangedList) {
					versionChangedStr += "<br>" + s;
				}
				
				putSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, fileSeq), versionChangedStr);
				
				if (!isEmpty(systemChangeHisStr)) {
					systemChangeHisStr += "<br><br>";
				}
				systemChangeHisStr += versionChangedStr;
			}
			
			resultMap.put("changehisLicenseName", systemChangeHisStr);

			if (!putSessionObject(CommonFunction.makeSessionKey(loginUserName(),
					CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID, prjId), resultMap)) {
				return makeJsonResponseHeader(false, null);
			}

			{
				T2CoProjectValidator pv = new T2CoProjectValidator();
				pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);

				pv.setAppendix("mainList", (List<ProjectIdentification>) resultMap.get("mainData"));
				// sub grid
				pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) resultMap.get("subData"));
				pv.setAppendix("noticeBinaryList", noticeBinaryList);
				
				T2CoValidationResult vr = pv.validate(new HashMap<>());

				if (!vr.isValid() || !vr.isDiff() || vr.hasInfo()) {
					resultMap.replace("mainData", CommonFunction.identificationSortByValidInfo(
							(List<ProjectIdentification>) resultMap.get("mainData"), vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false));
					return makeJsonResponseHeader(true, errMsg, resultMap, vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap());
				}
			}
			return makeJsonResponseHeader(true, errMsg, resultMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// session 삭제
			deleteSession(CommonFunction.makeSessionKey(loginUserName(),
					CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID, prjId));
			return makeJsonResponseHeader(false, e.getMessage());
		}

	}
	
	@GetMapping(value = PROJECT.PARTNER_OSS_FROM_PROJECT)
	public @ResponseBody ResponseEntity<Object> partnerOssFromProject(OssComponents ossComponents, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		ossComponents.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
		Map<String, Object> map = projectService.getPartnerOssList(ossComponents);

		return makeJsonResponseHeader(map);
	}
	
	/**
	 *
	 * @param commentsHistory the comments history
	 * @param req the req
	 * @param res the res
	 * @param model the model
	 * @return the response entity
	 */
	@PostMapping(value = PROJECT.COMMENTS_SAVE)
	public @ResponseBody ResponseEntity<Object> commentsSave(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String resCd = "";
		String resMsg= "";
		Project project = new Project();
		project.setPrjId(commentsHistory.getReferenceId());
		Project projectDetail = projectService.getProjectDetail(project);
		if (avoidNull(projectDetail.getCompleteYn()).equals(CoConstDef.FLAG_NO)) {
			//향후 메세지 처리 확인 할것
			return makeJsonResponseHeader(false, "The status is not Complete. Check the status value.", null);
		} else {
			String commentsMode = commentsHistory.getCommentsMode();
			if (commentsMode.equals("reject")) {
				project.setCompleteYn(CoConstDef.FLAG_NO);
				project.setStatusRequestYn(CoConstDef.FLAG_NO);
				project.setCommId(null);
				project.setUserComment(commentsHistory.getContents());
				project.setIgnoreUserCommentReg(CoConstDef.FLAG_YES);
				
				resMsg = getMessage("msg.distribute.reset");
				if ("10".equals(resCd)){
				}	
				else if ("20".equals(resCd)){
					resMsg="server error.";
				}
				else{
					resMsg = resCd;
				}
				if (!resCd.equals("10")) {
					return makeJsonResponseHeader(false, resMsg, null);
				}
				
			} else{
				commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT);
				commentsHistory.setStatus("Request to Open");
				commentService.registComment(commentsHistory);
				project.setCommId(commentsHistory.getCommId());
				project.setStatusRequestYn(CoConstDef.FLAG_YES);
			}
			projectService.updateProjectMaster(project);
		}
		return makeJsonResponseHeader();
	}
	
	/**
	 * @param commentsHistory
	 * @param req
	 * @param res
	 * @param model
	 * @return
	 */
	@PostMapping(value = PROJECT.COMMENTS_IGNORE)
	public @ResponseBody ResponseEntity<Object> commentsIgnore(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		Project project = new Project();
		project.setPrjId(commentsHistory.getReferenceId());
		Project projectDetail = projectService.getProjectDetail(project);
		
		if (projectDetail.getCompleteYn().equals(CoConstDef.FLAG_NO)) {
			//향후 메세지 처리 확인 할것
			return makeJsonResponseHeader(false, "The status is not Complete. Check the status value.", null);
		}else {
			project.setCompleteYn(CoConstDef.FLAG_YES);
			project.setStatusRequestYn(CoConstDef.FLAG_NO);
			project.setCommId(null);
			commentsHistory.setStatus("Request to Open(Ignore)");
			commentsHistory.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
			commentService.registComment(commentsHistory);
			projectService.updateProjectMaster(project);
		}
		
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
	@PostMapping(value = PROJECT.SAVE_COMMENT)
	public @ResponseBody ResponseEntity<Object> saveComment(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		commentService.registComment(commentsHistory);
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * Model file.
	 *
	 * @param file the file
	 * @param req the req
	 * @param request the request
	 * @param res the res
	 * @param model the model
	 * @return the string
	 * @throws Exception the exception
	 */
	@PostMapping(value = PROJECT.MODEL_FILE)
	public @ResponseBody ResponseEntity<Object> modelFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {
		// 엑셀 분석
		Map<String, List<Project>> modelList = ExcelUtil.getModelList(req, CommonFunction.emptyCheckProperty("upload.path", "/upload"),
				request.getParameter("distributionTarget"), request.getParameter("prjId"), request.getParameter("modelListAppendFlag"), request.getParameter("modelSeq"));
		
		return makeJsonResponseHeader(modelList);
	}
	
	@PostMapping(value = PROJECT.SUPPLEMENT_NOTICE_FILE)
	public @ResponseBody ResponseEntity<Object> getSupplementNoticeFile(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String fileId = null;
		String prjId = (String) map.get("referenceId");
		String zipFlag = (String) map.get("zipFlag");
		
		try {
			Project project = new Project();
			project.setPrjId(prjId); 
			Project projectDetail = projectService.getProjectDetail(project);
			
			ProjectIdentification identification = new ProjectIdentification();
			identification.setReferenceId(prjId);
			identification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
			Map<String, Object> result = getOssComponentDataInfo(identification, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
			
			String validMsg = projectService.checkValidData(result);
			
			if (isEmpty(validMsg)) {
				if (CoConstDef.FLAG_YES.equals(zipFlag)) {
					fileId = projectService.makeZipFileId(result, projectDetail);
				} else {
					try {
						String contents = projectService.makeNoticeFileContents(result);
						fileId = projectService.makeSupplementFileId(contents, projectDetail);
					} catch(Exception e) {
						log.error(e.getMessage());
					}
				}
			} else {
				return makeJsonResponseHeader(false, validMsg);
			}
			
			if (isEmpty(fileId)){
				return makeJsonResponseHeader(false, "overflow");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}

		return makeJsonResponseHeader(fileId);
	}
	
	// 20210715_BOM COMPARE FUNC MOVE (LgeProjectController > ProjectController) >>>
	@GetMapping(value=PROJECT.BOM_COMPARE, produces = "text/html; charset=utf-8")
	public String bomCompare(@PathVariable String beforePrjId, 
			@PathVariable String afterPrjId, 
			HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		
		if (beforePrjId.equals("0000")) {
			model.addAttribute("beforePrjId", "");
		}else {
			model.addAttribute("beforePrjId", beforePrjId);
		}
		
		if (afterPrjId.equals("0000")) {
			model.addAttribute("afterPrjId", "");
		}else {
			model.addAttribute("afterPrjId", afterPrjId);
		}
		
		return "project/bomCompare";
	}
			
	@SuppressWarnings("unchecked")
	@GetMapping(value=PROJECT.BOM_COMPARE_LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> bomCompareList(
			@RequestParam("beforePrjId") String beforePrjId, @RequestParam("afterPrjId") String afterPrjId) throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			Project beforePrjInfo = projectService.getProjectBasicInfo(beforePrjId);
			String beforeReferenceDiv = "";
			
			ProjectIdentification beforeIdentification = new ProjectIdentification();
			beforeIdentification.setReferenceId(beforePrjId);
			
			if(!beforePrjInfo.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
				beforeReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_BOM;
				beforeIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				beforeIdentification.setMerge("N");
			} else {
				beforeReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_ANDROID;
				beforeIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
			}
			
			Project afterPrjInfo = projectService.getProjectBasicInfo(afterPrjId);
			String afterReferenceDiv = "";
			
			ProjectIdentification AfterIdentification = new ProjectIdentification();
			AfterIdentification.setReferenceId(afterPrjId);
			
			if(!afterPrjInfo.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
				afterReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_BOM;
				AfterIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				AfterIdentification.setMerge("N");
			} else {
				afterReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_ANDROID;
				AfterIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
			}
			
			Map<String, Object> beforeBom = new HashMap<String, Object>();
			Map<String, Object> afterBom = new HashMap<String, Object>();
			List<ProjectIdentification> beforeBomList = null;
			List<ProjectIdentification> afterBomList = null;
			boolean beforeDataFlag = false;
			boolean afterDataFlag = false;
			
			beforeBom = getOssComponentDataInfo(beforeIdentification, beforeReferenceDiv);
			if (beforeReferenceDiv.equals(CoConstDef.CD_DTL_COMPONENT_ID_BOM)) {
				if (!beforeBom.containsKey("rows") || (List<ProjectIdentification>) beforeBom.get("rows") == null) {
					beforeDataFlag = true;
				} else {
					beforeBomList = (List<ProjectIdentification>) beforeBom.get("rows");
				}
			} else {
				if (!beforeBom.containsKey("mainData") || (List<ProjectIdentification>) beforeBom.get("mainData") == null) {
					beforeDataFlag = true;
				} else {
					beforeBomList = projectService.setMergeGridDataByAndroid((List<ProjectIdentification>) beforeBom.get("mainData"));
				}
			}
			if (beforeDataFlag || beforeBomList == null) {
				return makeJsonResponseHeader(false, "1");
			}
			
			afterBom = getOssComponentDataInfo(AfterIdentification, afterReferenceDiv);
			if (afterReferenceDiv.equals(CoConstDef.CD_DTL_COMPONENT_ID_BOM)) {
				if (!afterBom.containsKey("rows") || (List<ProjectIdentification>) afterBom.get("rows") == null) {
					afterDataFlag = true;
				} else {
					afterBomList = (List<ProjectIdentification>) afterBom.get("rows");
				}
			} else {
				if (!afterBom.containsKey("mainData") || (List<ProjectIdentification>) afterBom.get("mainData") == null) {
					afterDataFlag = true;
				} else {
					afterBomList = projectService.setMergeGridDataByAndroid((List<ProjectIdentification>) afterBom.get("mainData"));
				}
			}
			if (afterDataFlag || afterBomList == null) {
				return makeJsonResponseHeader(false, "1");
			}
			
			String beforePrjInfoString = beforePrjId + " - " + beforePrjInfo.getPrjName();
			if (!isEmpty(beforePrjInfo.getPrjVersion())) {
				beforePrjInfoString += " (" + beforePrjInfo.getPrjVersion() + ")";
			}
			String afterPrjInfoString = afterPrjId + " - " + afterPrjInfo.getPrjName();
			if (!isEmpty(afterPrjInfo.getPrjVersion())) {
				afterPrjInfoString += " (" + afterPrjInfo.getPrjVersion() + ")";
			}
			
			resultMap.put("beforePrjInfo", beforePrjInfoString);
			resultMap.put("afterPrjInfo", afterPrjInfoString);
			resultMap.put("contents", projectService.getBomCompare(beforeBomList, afterBomList, "list"));
			return makeJsonResponseHeader(true, "0" , resultMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, "1");
		}
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value=PROJECT.PROJECT_CHANGE_VIEW)
	public String getProjectChangeView(@RequestBody Map<String, Object> map, @PathVariable String code, HttpServletRequest req, HttpServletResponse res, Model model) {
		if (code.equals("status")) {
//			Map<String, String> map = new HashMap<String, String>();
//			Project prjBean = projectService.getProjectDetail(project);
//			String distributionStatus = avoidNull(prjBean.getDistributionStatus()).toUpperCase();
//			
//			map.put("projectStatus", avoidNull(prjBean.getStatus()).toUpperCase());
//			map.put("identificationStatus", avoidNull(prjBean.getIdentificationStatus()).toUpperCase());
//			map.put("verificationStatus", avoidNull(prjBean.getVerificationStatus()).toUpperCase());
//			map.put("distributionStatus", distributionStatus);
//			map.put("distributeDeployYn", prjBean.getDistributeDeployYn());
//			map.put("distributeDeployTime", prjBean.getDistributeDeployTime());
//			map.put("completeFlag", avoidNull(prjBean.getCompleteYn(), CoConstDef.FLAG_NO));
//			map.put("dropFlag", avoidNull(prjBean.getDropYn(), CoConstDef.FLAG_NO));
//			map.put("commId", avoidNull(prjBean.getCommId(), ""));
//			map.put("viewOnlyFlag", avoidNull(prjBean.getViewOnlyFlag(), CoConstDef.FLAG_NO));
//			
//			if (distributionStatus.equals("PROC")) {
//				code = "false";
//			}
//			
			model.addAttribute("permissionPrjIds", String.join(",", (List<String>) map.get("permissionPrjIds")));
			model.addAttribute("notPermissionPrjIds", String.join(",", (List<String>) map.get("notPermissionPrjIds")));
		} else if (code.equals("watcher")) {
			model.addAttribute("partnerFlag", CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES));
			model.addAttribute("batFlag", CommonFunction.propertyFlagCheck("menu.bat.use.flag", CoConstDef.FLAG_YES));
		}
		model.addAttribute("code", code);
		return "project/view/projectChangeView";
	}
	
	@PostMapping(value=PROJECT.PROJECT_STATUS)
	public @ResponseBody ResponseEntity<Object> getProjectStatus(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, String> map = new HashMap<String, String>();
		Project prjBean = projectService.getProjectDetail(project);
		
		map.put("projectStatus", avoidNull(prjBean.getStatus()));
		map.put("identificationStatus", avoidNull(prjBean.getIdentificationStatus()));
		map.put("verificationStatus", avoidNull(prjBean.getVerificationStatus()));
		map.put("distributionStatus", avoidNull(prjBean.getDistributionStatus()));
		map.put("distributeDeployYn", avoidNull(prjBean.getDistributeDeployYn()));
		map.put("distributeDeployTime", avoidNull(prjBean.getDistributeDeployTime()));
		map.put("completeFlag", avoidNull(prjBean.getCompleteYn(), CoConstDef.FLAG_NO));
		map.put("dropFlag", avoidNull(prjBean.getDropYn(), CoConstDef.FLAG_NO));
		map.put("commId", avoidNull(prjBean.getCommId(), ""));
		map.put("viewOnlyFlag", avoidNull(prjBean.getViewOnlyFlag(), CoConstDef.FLAG_NO));
		map.put("statusRequestYn", avoidNull(prjBean.getStatusRequestYn(), CoConstDef.FLAG_NO));
		if (!isEmpty(prjBean.getCvssScoreMax())) {
			map.put("cvssScoreMax", prjBean.getCvssScoreMax());
		}
		if (!isEmpty(prjBean.getVulnerabilityResolution())) {
			map.put("vulnerabilityResolution", prjBean.getVulnerabilityResolution());
		}
		
		return makeJsonResponseHeader(map);
	}
	
	@PostMapping(value=PROJECT.MAKE_YAML)
	public @ResponseBody ResponseEntity<Object> makeYaml(
			@RequestBody Project project
			, @PathVariable String code
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		String yamlFileId = "";
		
		try {
			// identification Tab Code / partnerName 필수 값.
			yamlFileId = YamlUtil.makeYaml(code, toJson(project));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
		
		return makeJsonResponseHeader(yamlFileId);
	}
	
	@PostMapping(value=PROJECT.PROJECT_DIVISION)
	public @ResponseBody ResponseEntity<Object> updateProjectDivision(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		List<String> permissionCheckList = null;
		
		if (!CommonFunction.isAdmin()) {
			permissionCheckList = CommonFunction.checkUserPermissions(loginUserName(), project.getPrjIds(), "project");
		}
		
		if (permissionCheckList == null || permissionCheckList.isEmpty()){
			Map<String, List<Project>> updatePrjDivision = projectService.updateProjectDivision(project);
			
			if (updatePrjDivision.containsKey("before") && updatePrjDivision.containsKey("after")) {
				List<Project> beforePrjList = (List<Project>) updatePrjDivision.get("before");
				List<Project> afterPrjList = (List<Project>) updatePrjDivision.get("after");
				
				if ((beforePrjList != null && !beforePrjList.isEmpty()) 
						&& (afterPrjList != null && !afterPrjList.isEmpty())
						&& beforePrjList.size() == afterPrjList.size()) {
					
					for (int i=0; i<beforePrjList.size(); i++) {
						try {
							String mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED;
							CoMail mailBean = new CoMail(mailType);
							mailBean.setParamPrjId(afterPrjList.get(i).getPrjId());
							mailBean.setCompareDataBefore(beforePrjList.get(i));
							mailBean.setCompareDataAfter(afterPrjList.get(i));
							mailBean.setToIdsCheckDivision(true);
							
							CoMailManager.getInstance().sendMail(mailBean);
							
							try {
								CommentsHistory commentsHistory = new CommentsHistory();
								commentsHistory.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
								commentsHistory.setReferenceId(afterPrjList.get(i).getPrjId());
								commentsHistory.setContents(afterPrjList.get(i).getUserComment());
								commentsHistory.setStatus("Changed");
								
								commentService.registComment(commentsHistory, false);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
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
	
	public void updateProjectNotification(Project project, Map<String, Object> resultMap) {
		if (resultMap != null){
			String mailType = (String) resultMap.get("mailType");
			String userComment = (String) resultMap.get("userComment");
			String commentDiv = (String) resultMap.get("commentDiv");
			String status = (String) resultMap.get("status");
			
			try {
				History h = new History();
				h = projectService.work(project);
				h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
				project = (Project) h.gethData();
				h.sethEtc(project.etcStr());
				historyService.storeData(h);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			if (!isEmpty(mailType)) {
				try {
					CoMail mailBean = new CoMail(mailType);
					mailBean.setParamPrjId(project.getPrjId());
					mailBean.setComment(userComment);
					
					CoMailManager.getInstance().sendMail(mailBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			if (!isEmpty(avoidNull(userComment).trim())) {
				try {
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(commentDiv);
					commHisBean.setReferenceId(project.getPrjId());
					commHisBean.setContents(userComment);
					commHisBean.setStatus(status);
					log.info(status + " 상태 comment 저장!!");
					commentService.registComment(commHisBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else if (!isEmpty(status)) {
				try {
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(commentDiv);
					commHisBean.setReferenceId(project.getPrjId());
					commHisBean.setContents(userComment);
					commHisBean.setStatus(status);
					log.info("comment empty, " + status + " 상태 comment 저장!!");
					commentService.registComment(commHisBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	

	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.PROJECT_BINARY_DB_SAVE)
	public @ResponseBody ResponseEntity<Object> binaryDBSave(@RequestBody Project project,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		Project prjInfo = projectService.getProjectDetail(project);
		
		boolean booleanFlag = false;
		
		if(project.getReferenceDiv().equals(CoConstDef.CD_DTL_COMPONENT_ID_BOM)) {
			if(prjInfo != null && !isEmpty(prjInfo.getBinBinaryFileId()) && prjInfo.getIdentificationSubStatusBin() != null && !(CoConstDef.FLAG_NO.equals(prjInfo.getIdentificationSubStatusBin())) 
					&& !(CoConstDef.FLAG_YES.equals(project.getIgnoreBinaryDbFlag()))) {
				try {

					ProjectIdentification parambin = new ProjectIdentification();
					parambin.setReferenceId(prjInfo.getPrjId());
					parambin.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BIN);
					Map<String, Object> mapbin = projectService.getIdentificationGridList(parambin);
					
					// platform 정보
					String platformName = "";
					String platformVer = "";
					
					if(CoConstDef.COMMON_SELECTED_ETC.equals(prjInfo.getOsType()) && !isEmpty(prjInfo.getOsTypeEtc())) {
						platformName = CommonFunction.getPlatformName(prjInfo.getOsTypeEtc());
						platformVer = CommonFunction.getPlatformVersion(prjInfo.getOsTypeEtc());
					} else {
						String _temp = CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, prjInfo.getOsType());
						platformName = CommonFunction.getPlatformName(_temp);
						platformVer = CommonFunction.getPlatformVersion(_temp);
					}
					
					String _prjName = "[" + prjInfo.getPrjId() + "]" + prjInfo.getPrjName();
					if(!isEmpty(prjInfo.getPrjVersion())) {
						_prjName += "_" + prjInfo.getPrjVersion();
					}
					
					binaryDataService.insertBatConfirmBinOssWithChecksum(_prjName, platformName, platformVer, prjInfo.getBinBinaryFileId(), (List<ProjectIdentification>) mapbin.get("mainData"));
					
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				
				} finally {
					booleanFlag = true;
				}
			}else {
				booleanFlag = false;
			}
		}
		
		if(project.getReferenceDiv().equals(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID)) {
			if(prjInfo != null && !isEmpty(prjInfo.getSrcAndroidResultFileId()) && prjInfo.getIdentificationSubStatusAndroid() != null && !(CoConstDef.FLAG_NO.equals(prjInfo.getIdentificationSubStatusAndroid()))
					&& !(CoConstDef.FLAG_YES.equals(project.getIgnoreBinaryDbFlag()))) {
				try {

					ProjectIdentification paramandroid = new ProjectIdentification();
					paramandroid.setReferenceId(prjInfo.getPrjId());
					paramandroid.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
					Map<String, Object> mapandroid = projectService.getIdentificationGridList(paramandroid);
					
					// platform 정보
					String platformName = "";
					String platformVer = "";
					if(CoConstDef.COMMON_SELECTED_ETC.equals(prjInfo.getOsType()) && !isEmpty(prjInfo.getOsTypeEtc())) {
						platformName = CommonFunction.getPlatformName(prjInfo.getOsTypeEtc());
						platformVer = CommonFunction.getPlatformVersion(prjInfo.getOsTypeEtc());
					} else {
						String _temp = CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, prjInfo.getOsType());
						platformName = CommonFunction.getPlatformName(_temp);
						platformVer = CommonFunction.getPlatformVersion(_temp);
					}
					
					String _prjName = "[" + prjInfo.getPrjId() + "]" + prjInfo.getPrjName();
					if(!isEmpty(prjInfo.getPrjVersion())) {
						_prjName += "_" + prjInfo.getPrjVersion();
					}
					
					binaryDataService.insertBatConfirmAndroidBinOssWithChecksum(_prjName, platformName, platformVer, prjInfo.getSrcAndroidResultFileId(), (List<ProjectIdentification>) mapandroid.get("mainData"));
				
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				
				} finally {
					booleanFlag = true;
				}
			}else {
				booleanFlag = false;
			}
		}
		
		return makeJsonResponseHeader(booleanFlag, null);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.DELETE_FILES)
	public @ResponseBody ResponseEntity<Object> deleteFiles (@RequestBody HashMap<String, Object> map, 
			HttpServletRequest req, HttpServletResponse res, Model model) {
		// default validation
		boolean isValid = true;
		// last response map
		Map<String, String> resMap = new HashMap<>();
		// default 00:java error check code, 10:success code
		String resCd = "00";
		String delFileString = (String) map.get("csvDelFileIds");
		String prjId = (String) map.get("prjId");
		String referenceDiv = (String) map.get("referenceDiv");

		Type collectionType = new TypeToken<List<T2File>>() {}.getType();
		List<T2File> delFile = new ArrayList<T2File>();
		delFile = (List<T2File>) fromJson(delFileString, collectionType);

		if (delFile.size() > 0) {
			try {
				Project project = new Project();
				project.setCsvFile(delFile);
				project.setPrjId(prjId);
				project.setReferenceDiv(referenceDiv);
				projectService.deleteUploadFile(project);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			// success code set 10
			resCd = "10";
			resMap.put("isValid", String.valueOf(isValid));
			resMap.put("resCd", resCd);
		}

		return makeJsonResponseHeader(resMap);
	}

	@PostMapping(value = PROJECT.CHECK_REQ_ENTRY_SECURITY)
	public @ResponseBody ResponseEntity<Object> checkReqEntrySecurity(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String prjId = (String)map.get("prjId");
		String tabMenu = (String)map.get("tabMenu");
		
		Project param = new Project();
		param.setPrjId(prjId);
		
		boolean reqEntryFlag = projectService.checkReqEntrySecurity(param, tabMenu);
		if (!reqEntryFlag) {
			return makeJsonResponseHeader();
		} else {
			return makeJsonResponseHeader(false, "checkReqEntry");
		}
	}
	
	@PostMapping(value=PROJECT.SEC_BULK_EDIT_POPUP)
	public String securityBulkEditPopup(HttpServletRequest req, HttpServletResponse res, Model model){
		return "project/view/secBulkEditView";
	}
	
	@GetMapping(value = PROJECT.SECURITY)
	public String security(@ModelAttribute ProjectIdentification identification, HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId) {
		Project project = new Project();
		project.setPrjId(prjId);
		
		Project projectMaster = projectService.getProjectDetail(project);
		
		if (!StringUtil.isEmpty(projectMaster.getCreator())){
			projectMaster.setPrjDivision(projectService.getDivision(projectMaster));	
		}
		
		CommentsHistory comHisBean = new CommentsHistory();
		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_USER);
		comHisBean.setReferenceId(projectMaster.getPrjId());
		projectMaster.setUserComment(commentService.getUserComment(comHisBean));
		
		model.addAttribute("project", projectMaster);
		
		return "project/security";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = PROJECT.SECURITY_GRID)
	public @ResponseBody ResponseEntity<Object> srcSecurityGridAjax(@ModelAttribute ProjectIdentification identification,
			HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId, @PathVariable String code) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		
		Project project = new Project();
		project.setPrjId(prjId);
		
		try {
			result = projectService.getSecurityGridList(project);
			rtnMap.put("totalGridData", (List<OssComponents>) result.get("totalList"));
			rtnMap.put("fullDiscoveredGridData", (List<OssComponents>) result.get("fullDiscoveredList"));
			
			T2CoProjectValidator pv = new T2CoProjectValidator();
			pv.setProcType(pv.PROC_TYPE_SECURITY);
			pv.setValidLevel(pv.VALID_LEVEL_BASIC);
			
			for (int i = 0; i < 2; i++) {
				if (i == 0) {
					pv.setAppendix("totalList", (List<OssComponents>) result.get("totalList"));
				} else {
					pv.setAppendix("fullDiscoveredList", (List<OssComponents>) result.get("fullDiscoveredList"));
				}
				T2CoValidationResult vr = pv.validate(new HashMap<>());
				if (!vr.isValid()) {
					if (i == 0) {
						rtnMap.put("totalValidData", vr.getValidMessageMap());
					} else {
						rtnMap.put("fullDiscoveredValidData", vr.getValidMessageMap());
					}
				}
			}
			
			if (result.containsKey("msg")) {
				rtnMap.put("msg", result.get("msg"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader(rtnMap);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SAVE_SECURITY)
	public @ResponseBody ResponseEntity<Object> saveSecurity(@RequestBody Map<String, Object> map, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		String prjId = (String) map.get("referenceId");
		String tabName = (String) map.get("targetName");
		String scrtCsvFileId = (String) map.get("scrtCsvFileId");
		String scrtCsvFiles = (String) map.get("scrtCsvFiles");
		String gridString = (String) map.get("gridData");
		
		Type collectionType = new TypeToken<List<T2File>>() {}.getType();
		List<T2File> addFile = new ArrayList<T2File>();
		addFile = (List<T2File>) fromJson(scrtCsvFiles, collectionType);
		collectionType = new TypeToken<List<OssComponents>>() {}.getType();
		List<OssComponents> ossComponents = new ArrayList<>();
		ossComponents = (List<OssComponents>) fromJson(gridString, collectionType);
		
		Map<String, String> resMap = new HashMap<>();
		Project project = new Project();
		project.setPrjId(prjId);
		project.setScrtCsvFileId(scrtCsvFileId);
		project.setCsvFileSeq(addFile);
		
		try {
			projectService.registSecurity(project, tabName, ossComponents);
			projectService.updateSecurityDataForProject(prjId);
			Project param = new Project();
			param.setPrjId(prjId);
			Project pDat = projectService.getProjectDetail(param);
			resMap.put("identificationStatus", pDat.getIdentificationStatus());
			History h = projectService.work(pDat);
			h.sethAction(CoConstDef.ACTION_CODE_NEEDED);
			historyService.storeData(h); // 메일로 보낼 데이터를 History에 저장합니다. -> h.gethData()로 확인 가능
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			resMap.put("isValid", "false");
		} finally {
			resMap.put("isValid", "true");
		}
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value = PROJECT.CHECK_SELECT_DOWNLOAD_FILE)
	public @ResponseBody ResponseEntity<Object> checkSelectDownloadFile(@RequestBody HashMap<String, Object> map, @PathVariable String code, HttpServletRequest req, HttpServletResponse res) {
		Map<String, Object> resMap = new HashMap<>();
		String prjId = (String) map.get("prjId");
		Project project = new Project();
		project.setPrjId(prjId);
		project.setReferenceDiv(code);
		
		resMap = projectService.checkSelectDownloadFile(project);
		return makeJsonResponseHeader(resMap);
	}
	
	@GetMapping(value = PROJECT.SHARE_URL)
	public void shareUrl(HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String prjId) throws IOException {
		Project project = new Project();
		project.setPrjId(prjId);
		
		try {
			project = projectService.getProjectDetail(project);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		if (CoConstDef.FLAG_NO.equals(project.getViewOnlyFlag()) && project.getStatusPermission() == 1) {
			res.sendRedirect(req.getContextPath() + "/index?id=" + project.getPrjId() + "&menu=prj&view=false");
		} else {
			res.sendRedirect(req.getContextPath() + "/index?id=" + project.getPrjId() + "&menu=prj&view=true");
		}
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = PROJECT.SECURITY_SHEET_DATA)
	public @ResponseBody ResponseEntity<Object> getSecuritySheetData(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		log.info("Security Report Read Start");

		try {
			String fileSeq = (String) map.get("fileSeq");
			List<String> sheetNums = (List<String>) map.get("sheetNums");
			String mainDataString = (String) map.get("mainData");
			String readType = (String) map.get("readType");

			List<String> sheetList = new ArrayList<>();
			for (String s : sheetNums) {
				if (!sheetList.contains(s)) {
					sheetList.add(s);
				}
			}
			
			Type collectionType2 = new TypeToken<List<OssComponents>>() {
			}.getType();
			List<OssComponents> ossComponents = new ArrayList<OssComponents>();
			ossComponents = (List<OssComponents>) fromJson(mainDataString, collectionType2);
			
			String errMsg = "";
			List<OssComponents> reportData = new ArrayList<OssComponents>();
			List<String> errMsgList = new ArrayList<>();
			Map<String, String> emptyErrMsg = new HashMap<>();
			try {
				if (!ExcelUtil.readReport(readType, true, sheetList.toArray(new String[sheetList.size()]), fileSeq, reportData, errMsgList, emptyErrMsg)) {
					// error 처리
					for (String s : errMsgList) {
						if (isEmpty(s)) {
							continue;
						}
						if (!isEmpty(errMsg)) {
							errMsg += "<br/>";
						}
						errMsg += s;
					}
					
					return makeJsonResponseHeader(isEmpty(errMsg), errMsg);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return makeJsonResponseHeader(false, e.getMessage());
			}
			
			for (String s : errMsgList) {
				if (isEmpty(s)) {
					continue;
				}
				if (!isEmpty(errMsg)) {
					errMsg += "<br/>";
				}
				errMsg += s;
			}
			
			if (isEmpty(errMsg) && !emptyErrMsg.isEmpty()) {
				errMsg = emptyErrMsg.get("emptyErrMsg");
			}
			
			Map<String, Object> resultMap = CommonFunction.makeSecurityGridDataFromReport(ossComponents, reportData, fileSeq, readType);
			
			T2CoProjectValidator pv = new T2CoProjectValidator();
			pv.setProcType(pv.PROC_TYPE_SECURITY);
			pv.setValidLevel(pv.VALID_LEVEL_BASIC);
			pv.setAppendix("totalList", (List<OssComponents>) resultMap.get("totalGridData"));
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			if (!vr.isValid()) {
				resultMap.put("totalValidData", vr.getValidMessageMap());
			}
			
			resultMap.put("isValid", true);
			resultMap.put("validMsg", errMsg);
			return makeJsonResponseHeader(resultMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}
	}
	
	@PostMapping(value=PROJECT.CHANGE_PROJECT_STATUS)
	public @ResponseBody ResponseEntity<Object> changeProjectStatus(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		String resCd = "";
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		Project prjBean = projectService.getProjectDetail(project);
		
		if (prjBean != null) {
			boolean notChangeFlag = false;
			String prjId = project.getPrjId();
			String changeCode = project.getCode();
			boolean completeFlag = CoConstDef.FLAG_YES.equalsIgnoreCase(avoidNull(prjBean.getCompleteYn(), CoConstDef.FLAG_NO)) ? true : false;
			boolean dropFlag = CoConstDef.FLAG_YES.equalsIgnoreCase(avoidNull(prjBean.getDropYn(), CoConstDef.FLAG_NO)) ? true : false;
			
			if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_REVIEW.equalsIgnoreCase(avoidNull(prjBean.getStatus()))) {
				notChangeFlag = true;
				rtnMap.put("notChangePrjId", prjId);
			} else {
				if (changeCode.equals("1")) {
					if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS.equalsIgnoreCase(avoidNull(prjBean.getStatus()))) {
						if (isEmpty(avoidNull(prjBean.getIdentificationStatus())) || CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS.equalsIgnoreCase(avoidNull(prjBean.getIdentificationStatus()))) {
							notChangeFlag = true;
							rtnMap.put("notChangePrjId", prjId);
						}
					}
				} else if (changeCode.equals("2")) {
					if (!dropFlag && !completeFlag) {
					} else {
						notChangeFlag = true;
						rtnMap.put("notChangePrjId", prjId);
					}
				} else if (changeCode.equals("4")) {
					if (CommonFunction.isAdmin() && CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(avoidNull(prjBean.getIdentificationStatus()))
							&& (isEmpty(avoidNull(prjBean.getVerificationStatus())) 
									|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS.equalsIgnoreCase(avoidNull(prjBean.getVerificationStatus()))
									|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equalsIgnoreCase(avoidNull(prjBean.getVerificationStatus()))
									|| CoConstDef.CD_DTL_IDENTIFICATION_STATUS_NA.equalsIgnoreCase(avoidNull(prjBean.getVerificationStatus())))
							&& !completeFlag) {
					} else {
						notChangeFlag = true;
						rtnMap.put("notChangePrjId", prjId);
					}
				}
			}
			
			if (!notChangeFlag) {
				Project param = new Project();
				param.setPrjId(prjId);
				param.setUserComment(project.getUserComment());
				
				try {
					switch (changeCode) {
						case "1" :
							param.setIdentificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_PROGRESS);
							if (CommonFunction.isAdmin()) {
								if (CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(avoidNull(prjBean.getIdentificationStatus()))) {
									if (completeFlag) {
										param.setCompleteYn(CoConstDef.FLAG_NO);
									}
								}
								
								if (dropFlag) {
									param.setCompleteYn(CoConstDef.FLAG_NO);
								}
								
								rtnMap = projectService.changeProjectStatus(param);
							} else {
								if (completeFlag) {
									CommentsHistory commentsHistory = new CommentsHistory();
									commentsHistory.setReferenceId(prjId);
									commentsHistory.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
									commentsHistory.setCommId(prjBean.getCommId());
									commentsHistory.setCommentsMode(!isEmpty(prjBean.getCommId()) ? CoConstDef.ACTION_CODE_UPDATE : CoConstDef.ACTION_CODE_INSERT);
									commentsHistory.setContents(param.getUserComment());
									commentsHistory.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT);
									commentsHistory.setStatus("Request to Open");
									commentService.registComment(commentsHistory);
									
									param.setCommId(commentsHistory.getCommId());
									param.setStatusRequestYn(CoConstDef.FLAG_YES);
									projectService.updateProjectMaster(param);
								} else {
									if (dropFlag) {
										param.setCompleteYn(CoConstDef.FLAG_NO);
									}
									
									rtnMap = projectService.changeProjectStatus(param);
								}
							}
							break;
						case "2" :
							param.setDropYn(CoConstDef.FLAG_YES);
							rtnMap = projectService.changeProjectStatus(param);
							
							break;
						default :
							param.setCompleteYn(CoConstDef.FLAG_YES);
							rtnMap = projectService.changeProjectStatus(param);
							
							break;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					resCd = "20";
				}
				
				resCd = (String) rtnMap.get("resCd");
				
				if (isEmpty(resCd) || resCd.equals("10")) {
					updateProjectNotification(project, rtnMap);
					rtnMap.clear();
				} else {
					rtnMap.clear();
					rtnMap.put("notChangePrjId", prjId);
				}
			}
		}
		
		return makeJsonResponseHeader(rtnMap);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value=PROJECT.DEPENDENCY_TREE_POPUP)
	public String dependencyTreePopup(@RequestParam Map<String, Object> map, HttpServletRequest req, HttpServletResponse res, Model model) {
		String mainDataString = (String) map.get("rows");
		Type collectionType = new TypeToken<List<ProjectIdentification>>() {}.getType();
		List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
		ossComponents = (List<ProjectIdentification>) fromJson(mainDataString, collectionType);
		Map<String, Object> dependencyTreeMap = projectService.getDependencyTreeList(ossComponents);
		model.addAttribute("dependencyTreeMap", dependencyTreeMap);
		return "project/fragments/dependencyTreePopup";
	}

}
