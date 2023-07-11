/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.*;
import oss.fosslight.common.Url.LICENSE;
import oss.fosslight.domain.*;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.LicenseService;
import oss.fosslight.service.SearchService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoLicenseValidator;

import static oss.fosslight.common.CoConstDef.*;

@Controller
@Slf4j
public class LicenseController extends CoTopComponent{
	@Autowired LicenseService licenseService;
	@Autowired HistoryService historyService;
	@Autowired CommentService commentService;
	@Autowired SearchService searchService;
	
	private final String SESSION_KEY_SEARCH = "SESSION_KEY_LICENSE_LIST";
	
	@GetMapping(value=LICENSE.LIST)
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		LicenseMaster searchBean = null;
		
		if (!CoConstDef.FLAG_YES.equals(req.getParameter("gnbF"))) {
			deleteSession(SESSION_KEY_SEARCH);
			searchBean = searchService.getLicenseSearchFilter(loginUserName());
			if (searchBean == null) {
				searchBean = new LicenseMaster();
			}
		} else if (getSessionObject(SESSION_KEY_SEARCH) != null) {
			searchBean = (LicenseMaster) getSessionObject(SESSION_KEY_SEARCH);
		}
		
		if (getSessionObject("defaultLoadYn") != null) {
			model.addAttribute("defaultLoadYn", CoConstDef.FLAG_YES);
			
			deleteSession("defaultLoadYn");
		}
		
		model.addAttribute("searchBean", searchBean);
		
		return LICENSE.LIST_JSP;
	}
	
	@GetMapping(value=LICENSE.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(
			LicenseMaster licenseMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		
		if ("Y".equals(req.getParameter("ignoreSearchFlag"))) {
			return makeJsonResponseHeader(new HashMap<String, Object>());
		}
		
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		licenseMaster.setCurPage(page);
		licenseMaster.setPageListSize(rows);
		licenseMaster.setSortField(sidx);
		licenseMaster.setSortOrder(sord);

		if ("search".equals(req.getParameter("act"))) {
			// 검색 조건 저장
			putSessionObject(SESSION_KEY_SEARCH, licenseMaster);
		} else if (getSessionObject(SESSION_KEY_SEARCH) != null) {
			licenseMaster = (LicenseMaster) getSessionObject(SESSION_KEY_SEARCH);
		}
		
		Map<String, Object> map = null;
		
		try {
			if (isEmpty(licenseMaster.getLicenseNameAllSearchFlag())) {
				licenseMaster.setLicenseNameAllSearchFlag(CoConstDef.FLAG_NO);
			}
			
			licenseMaster.setTotListSize(licenseService.selectLicenseMasterTotalCount(licenseMaster));
			map = licenseService.getLicenseMasterList(licenseMaster);

		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		CustomXssFilter.licenseMasterFilter((List<LicenseMaster>) map.get("rows"));
		return makeJsonResponseHeader(map);
	}
	
	@GetMapping(value=LICENSE.EDIT)
	public String edit(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		return LICENSE.EDIT_JSP;
	}
	
	@GetMapping(value=LICENSE.EDIT_ID)
	public String edit(@PathVariable String licenseId, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		LicenseMaster licenseMaster = new LicenseMaster(licenseId);
		licenseMaster = licenseService.getLicenseMasterOne(licenseMaster);
		boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
		
		if (licenseMaster != null) {
			licenseMaster.setDomain(CommonFunction.getDomain(req));
			licenseMaster.setInternalUrl(CommonFunction.makeLicenseInternalUrl(licenseMaster, distributionFlag));

			if (!"ROLE_ADMIN".equals(loginUserRole())) {
				// html link 형식으로 변환
				licenseMaster.setDescription(CommonFunction.makeHtmlLinkTagWithText(licenseMaster.getDescription()));
			}
			model.addAttribute("licenseInfo", licenseMaster);
		}
		
		model.addAttribute("detail", toJson(licenseMaster));
		
		if ("ROLE_ADMIN".equals(loginUserRole())) {
			return LICENSE.EDIT_JSP;
		} else {
			return LICENSE.LICENSE_VIEW_JSP;
		}
	}
	
	@PostMapping(value=LICENSE.VALIDATION)
	public @ResponseBody ResponseEntity<Object> validation(
			@ModelAttribute LicenseMaster licenseMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		// validation check
		T2CoLicenseValidator lv = new T2CoLicenseValidator();
		T2CoValidationResult vResult = lv.validateRequest(req);

		return makeJsonResponseHeader(vResult.getValidMessageMap());
	}
	
	@PostMapping(value=LICENSE.DEL_AJAX)
	public @ResponseBody ResponseEntity<Object> delAjax(
			@ModelAttribute LicenseMaster licenseMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		T2CoLicenseValidator lv = new T2CoLicenseValidator();
		lv.setProcType(lv.PROC_TYPE_DELETE);
		lv.setAppendix("licenseId", licenseMaster.getLicenseId());
		T2CoValidationResult vr = lv.validate(new HashMap<>());
		
		if (!vr.isValid()) {
			return makeJsonResponseHeader(false, vr.getValidMessage("LICENSE_NAME"));
		}
		
		LicenseMaster beforeBean =  licenseService.getLicenseMasterOne(licenseMaster);
		
		try {
			History h = licenseService.work(licenseMaster);
			h.sethAction(CoConstDef.ACTION_CODE_DELETE);
			historyService.storeData(h);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		try {
			CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_LICENSE_DELETE);
			mailBean.setParamLicenseId(licenseMaster.getLicenseId());
			mailBean.setComment(licenseMaster.getComment());
			
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		licenseService.deleteLicenseMaster(licenseMaster);
		
		putSessionObject("defaultLoadYn", true); // 화면 로드 시 default로 리스트 조회 여부 flag 
		CoCodeManager.getInstance().refreshLicenseInfo();
		
		try {
			boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
			
			licenseService.deleteDistributeLicense(beforeBean, distributionFlag);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader();
	}

	@PostMapping(value=LICENSE.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> saveAjax(
			@ModelAttribute LicenseMaster licenseMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){

		//License Save
		Map<String, Object> resMap = licenseService.saveLicense(licenseMaster);

		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value=LICENSE.DELETE_COMMENT)
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
		
		if (!vResult.isValid()){
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		try{
			commentService.deleteComment(commentsHistory);
		} catch (Exception e){
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(vResult.getValidMessageMap());
	}
	
	@PostMapping(value=LICENSE.SAVE_COMMENT)
	public @ResponseBody ResponseEntity<Object> saveComment(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		T2CoValidationResult vResult = null;
		
		try {
			// validation check
			vResult = validate(req);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if (!vResult.isValid()) {
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		CommentsHistory result = null;
		
		try {

			result = commentService.registComment(commentsHistory);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(result);
	}
	
	@GetMapping(value=LICENSE.LICENSE_TEXT)
	public @ResponseBody ResponseEntity<Object> getLicenseText(
			LicenseMaster licenseMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		try{
			if (!isEmpty(licenseMaster.getLicenseName()) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseMaster.getLicenseName().trim().toUpperCase())) {
				return makeJsonResponseHeader(true, CommonFunction.lineReplaceToBR(avoidNull(CoCodeManager.LICENSE_INFO_UPPER.get(licenseMaster.getLicenseName().trim().toUpperCase()).getLicenseText())));
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		
		return makeJsonResponseHeader(false, null);
	}
	
	@GetMapping(value=LICENSE.AUTOCOMPLETE_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteAjax(
			LicenseMaster licenseMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		
		List<LicenseMaster> list = licenseService.getLicenseNameList();
		CustomXssFilter.licenseMasterFilter(list);
		return makeJsonResponseHeader(list);
	}	
	
	@PostMapping(value=LICENSE.LICENSE_ID)
	public @ResponseBody ResponseEntity<Object> getLicenseId(HttpServletRequest req, HttpServletResponse res, 
			@RequestParam(value="licenseName", required=true)String licenseName) {
		Map<String, String> map = new HashMap<String, String>();
		
		LicenseMaster lm = new LicenseMaster();
		lm.setLicenseName(licenseName.trim());
		
		lm = licenseService.getLicenseId(lm);
		map.put("licenseId", lm.getLicenseId());
		
		return makeJsonResponseHeader(map);
	}

	/**LicenseBulkReg UI*/
	@GetMapping(value = LICENSE.LICENSE_BULK_REG, produces = "text/html; charset=utf-8")
	public String LicenseBulkRegPage(HttpServletRequest req, HttpServletResponse res, Model model) {

		return LICENSE.LICENSE_BULK_REG_JSP;
	}

	/**
	 * LicenseBulkReg Save Post
	 * */
	@PostMapping(value = Url.LICENSE.BULK_REG_AJAX)
	public @ResponseBody
	ResponseEntity<Object> saveAjaxJson(
			@RequestBody List<LicenseMaster> licenseMasters
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) {
		List<Map<String, Object>> licenseDataMapList = new ArrayList<>();
		Map<String, Object> resMap = new HashMap<>();

		if (licenseMasters.isEmpty()) {
			//When the licenseMaster List delivered from the client is empty (if the upload button is pressed without uploading the file)
			resMap.put("res", false);
			return makeJsonResponseHeader(resMap);
		}

		for (LicenseMaster license : licenseMasters) {
			Map<String, Object> licenseDataMap = new HashMap<>();

			if (license.getLicenseType().equalsIgnoreCase("Permissive")) {
				license.setLicenseType(CD_LICENSE_TYPE_PMS);
			} else if (license.getLicenseType().equalsIgnoreCase("Weak Copyleft")) {
				license.setLicenseType(CD_LICENSE_TYPE_WCP);
			} else if (license.getLicenseType().equalsIgnoreCase("Copyleft")) {
				license.setLicenseType(CD_LICENSE_TYPE_CP);
			} else if (license.getLicenseType().equalsIgnoreCase("Proprietary")) {
				license.setLicenseType(CD_LICENSE_TYPE_NA);
			} else if (license.getLicenseType().equalsIgnoreCase("Proprietary Free")) {
				license.setLicenseType(CD_LICENSE_TYPE_PF);
			} else {
				license.setLicenseType("");
			}

			/**
			 * Set
			 * HotYn, notice, source
			 * */
			license.setRestriction("");
			license.setHotYn(CoConstDef.FLAG_NO);
			if (license.getObligationNotificationYn().equalsIgnoreCase("o")) {
				license.setObligationNotificationYn(CoConstDef.FLAG_YES);
			} else {
				license.setObligationNotificationYn(null);
			}
			if (license.getObligationDisclosingSrcYn().equalsIgnoreCase("o")) {
				license.setObligationDisclosingSrcYn(CoConstDef.FLAG_YES);
			} else {
				license.setObligationDisclosingSrcYn(null);
			}

			/**
			 * Check
			 * LicenseName NPE
			 * LicenseName COMMA
			 * LicenseName DB Check
			 * */
			if (Objects.isNull(license.getLicenseName()) || StringUtil.isBlank(license.getLicenseName())) {
				log.debug("License name is required.");
				licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (Required missing)");
				licenseDataMapList.add(licenseDataMap);
				continue;
			} else if (license.getLicenseName().contains(CoConstDef.CD_COMMA_CHAR)) {
				log.debug("License name contains COMMA.");
				licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (License name contains COMMA)");
				licenseDataMapList.add(licenseDataMap);
				continue;
			} else {
				LicenseMaster licenseForCheckName = new LicenseMaster();
				licenseForCheckName.setLicenseName(license.getLicenseName());
				LicenseMaster resultByName = licenseService.checkExistsLicense(licenseForCheckName);
				if (resultByName != null) {
					log.debug("Same License already exists.");
					licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (license already exist)");
					licenseDataMapList.add(licenseDataMap);
					continue;
				}
			}

			/**
			 * Check
			 * LicenseText NPE
			 * */
			String licenseText = license.getLicenseText();
			if (licenseText == null || licenseText.isEmpty()) {
				log.debug("licenseText is null:" + license.getLicenseName());
				licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (Required missing)");
				licenseDataMapList.add(licenseDataMap);
				continue;
			}

			/**
			 * Check
			 * LicenseType NPE
			 * */
			String licenseType = license.getLicenseType();
			if (licenseType == null || licenseType.isEmpty()) {
				log.debug("licenseType is null:" + license.getLicenseName());
				licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (Required missing)");
				licenseDataMapList.add(licenseDataMap);
				continue;
			}

			/**
			 * Check
			 * SHORT_IDENTIFIER COMMA
			 * SHORT_IDENTIFIER LicenseName
			 */
			if (license.getShortIdentifier().contains(CoConstDef.CD_COMMA_CHAR)) {
				log.debug("SPDX contains COMMA.");
				licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (SPDX contains COMMA)");
				licenseDataMapList.add(licenseDataMap);
				continue;
			} else if (license.getShortIdentifier().equalsIgnoreCase(avoidNull(license.getLicenseName()))) {
				log.debug("SPDX equals with LicenseName.");
				licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (SPDX sames with LicenseName.)");
				licenseDataMapList.add(licenseDataMap);
				continue;
			} else if(!license.getShortIdentifier().equals("")) {
				LicenseMaster licenseForCheckSPDX = new LicenseMaster(license.getLicenseId());
				licenseForCheckSPDX.setLicenseName(license.getShortIdentifier());
				LicenseMaster resultBySPDX = licenseService.checkExistsLicense(licenseForCheckSPDX);
				if (resultBySPDX != null) {
					log.debug("SPDX already exits");
					licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (SPDX already exits)");
					licenseDataMapList.add(licenseDataMap);
					continue;
				}
			}

			/**
			 * Check
			 * LicenseNickname Input Duplication
			 * LicenseNickname contains COMMA
			 * LicenseNickname DB Check
			 */
			List<String> nickNameList = new ArrayList<>();
			Boolean checkLicenseNickname = true;
			for (String nickname : license.getLicenseNicknames()) {

				if (nickNameList.contains(nickname.toUpperCase())) {
					log.debug("Input nickname is overlapped.");
					licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (Input nickname is overlapped.)");
					licenseDataMapList.add(licenseDataMap);
					checkLicenseNickname = false;
					break;
				} else {
					nickNameList.add(nickname.toUpperCase());

					if (nickname.contains(CoConstDef.CD_COMMA_CHAR)) {
						log.debug("LicenseNickname contains COMMA.");
						licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (LicenseNickname contains COMMA)");
						licenseDataMapList.add(licenseDataMap);
						checkLicenseNickname = false;
						break;
					} else if (nickname.equalsIgnoreCase(avoidNull(license.getLicenseName()))
							|| nickname.equalsIgnoreCase(avoidNull(license.getShortIdentifier()))) {
						log.debug("Nickname equals with LicenseName or SPDX");
						licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (Nickname equals with LicenseName or SPDX)");
						licenseDataMapList.add(licenseDataMap);
						checkLicenseNickname = false;
						break;
					} else {
						LicenseMaster licenseForCheckNickname = new LicenseMaster(license.getLicenseId());
						licenseForCheckNickname.setLicenseName(nickname);
						LicenseMaster resultByNickname = licenseService.checkExistsLicense(licenseForCheckNickname);
						if (resultByNickname != null) {
							log.debug("LicenseNickname already exits");
							licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), false, "X (LicenseNickname already exits)");
							licenseDataMapList.add(licenseDataMap);
							checkLicenseNickname = false;
							break;
						}
					}
				}
			}
			if (checkLicenseNickname == false) {
				continue;
			}

			Map<String, Object> result = licenseService.saveLicense(license);
			if (result.get("resCd").equals("10")) {
				licenseDataMap = licenseService.getLicenseDataMap(license.getGridId(), true, "O");
				licenseDataMapList.add(licenseDataMap);
			}
		}
		resMap.put("res", true);
		resMap.put("value", licenseDataMapList);
		return makeJsonResponseHeader(resMap);
	}

	/**
	 * Validate Bulk Reg
	 * */
	@PostMapping(value=Url.LICENSE.BULK_VALIDATION)
	public @ResponseBody ResponseEntity<Object> bulkValidation(
			@RequestBody List<LicenseMaster> licenseMasters){
		Map<String, Object> resMap = new HashMap<>();

		T2CoLicenseValidator validator = new T2CoLicenseValidator();
		validator.setAppendix("licenseList", licenseMasters);
		validator.setVALIDATION_TYPE(validator.VALID_LICNESELIST_BULK);
		T2CoValidationResult vr = validator.validate(new HashMap<>());

		resMap.put("validData", vr.getValidMessageMap());
		return makeJsonResponseHeader(resMap);
	}

	/**LicenseBulkReg Upload Post*/
	@ResponseBody
	@PostMapping(value = Url.LICENSE.CSV_FILE)
	public ResponseEntity<Object> csvFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request,
										  HttpServletResponse res, Model model) throws Exception {
		List<Object> limitCheckFiles = new ArrayList<>();
		List<UploadFile> list = new ArrayList<UploadFile>();
		List<LicenseMaster> licenseList = new ArrayList<>();
		Iterator<String> fileNames = req.getFileNames();
		List<Map<String, Object>> licenseWithStatusList = new ArrayList<>();
		Map<String, Object> resMap = new HashMap<>();

		while (fileNames.hasNext()) {
			UploadFile uploadFile = new UploadFile();
			MultipartFile multipart = req.getFile(fileNames.next());
			uploadFile.setSize(multipart.getSize());
			list.add(uploadFile);
		}

		limitCheckFiles = CommonFunction.checkXlsxFileLimit(list);
		resMap.put("limitCheck", limitCheckFiles);
		licenseList = ExcelUtil.readLicenseList(req, CommonFunction.emptyCheckProperty("upload.path", "/upload"));

		if (licenseList != null) {
			Map<String, Object> ossWithStatus;
			for (int i = 0; i < licenseList.size(); i++) {
				ossWithStatus = new HashMap<>();
				ossWithStatus.put("license", licenseList.get(i));
				ossWithStatus.put("status", "Ready");
				licenseWithStatusList.add(ossWithStatus);
			}
		} else {
			resMap.put("res", false);
			return makeJsonResponseHeader(resMap);
		}

		resMap.put("res", true);
		resMap.put("value", licenseWithStatusList);
		return makeJsonResponseHeader(resMap);
	}
}
