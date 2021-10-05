/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.LICENSE;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.History;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.LicenseService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoLicenseValidator;

@Controller
@Slf4j
public class LicenseController extends CoTopComponent{
	@Autowired LicenseService licenseService;
	@Autowired HistoryService historyService;
	@Autowired CommentService commentService;
	
	private final String SESSION_KEY_SEARCH = "SESSION_KEY_LICENSE_LIST";
	
	@GetMapping(value=LICENSE.LIST)
	public String list(HttpServletRequest req, HttpServletResponse res, Model model) throws Exception{
		LicenseMaster searchBean = null;
		
		if(!CoConstDef.FLAG_YES.equals(req.getParameter("gnbF"))) {
			deleteSession(SESSION_KEY_SEARCH);
			
			searchBean = new LicenseMaster();
		} else if(getSessionObject(SESSION_KEY_SEARCH) != null) {
			searchBean = (LicenseMaster) getSessionObject(SESSION_KEY_SEARCH);
		}
		
		if(getSessionObject("defaultLoadYn") != null) {
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
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		licenseMaster.setCurPage(page);
		licenseMaster.setPageListSize(rows);
		licenseMaster.setSortField(sidx);
		licenseMaster.setSortOrder(sord);

		if("search".equals(req.getParameter("act"))) {
			// 검색 조건 저장
			putSessionObject(SESSION_KEY_SEARCH, licenseMaster);
		} else if(getSessionObject(SESSION_KEY_SEARCH) != null) {
			licenseMaster = (LicenseMaster) getSessionObject(SESSION_KEY_SEARCH);
		}
		
		Map<String, Object> map = null;
		
		try {
			if(isEmpty(licenseMaster.getLicenseNameAllSearchFlag())) {
				licenseMaster.setLicenseNameAllSearchFlag(CoConstDef.FLAG_NO);
			}
			
			licenseMaster.setTotListSize(licenseService.selectLicenseMasterTotalCount(licenseMaster));
			map = licenseService.getLicenseMasterList(licenseMaster);

		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		
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
		
		if(licenseMaster != null) {
			licenseMaster.setDomain(CommonFunction.getDomain(req));
			licenseMaster.setInternalUrl(CommonFunction.makeLicenseInternalUrl(licenseMaster, distributionFlag));

			if(!"ROLE_ADMIN".equals(loginUserRole())) {
				// html link 형식으로 변환
				licenseMaster.setDescription(CommonFunction.makeHtmlLinkTagWithText(licenseMaster.getDescription()));
			}
			
			model.addAttribute("licenseInfo", licenseMaster);
		}
		
		model.addAttribute("detail", toJson(licenseMaster));
		
		if("ROLE_ADMIN".equals(loginUserRole())) {
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
		
		if(!vr.isValid()) {
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
		
		licenseService.deleteLicenseMaster(licenseMaster);
		
		putSessionObject("defaultLoadYn", true); // 화면 로드 시 default로 리스트 조회 여부 flag 
		CoCodeManager.getInstance().refreshLicenseInfo();
		
		try {
			CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_LICENSE_DELETE);
			mailBean.setParamLicenseId(licenseMaster.getLicenseId());
			mailBean.setComment(licenseMaster.getComment());
			
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
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
		String resCd="00";	//비정상
		String result="";
		LicenseMaster beforeBean = null;
		LicenseMaster afterBean = null;
		
		HashMap<String,Object> resMap = new HashMap<>(); 
		String licenseId = licenseMaster.getLicenseId();
		boolean isNew = isEmpty(licenseId);
		boolean isChangeName = false;
		boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
		List<OssMaster> typeChangeOssIdList = null;
		
		if(!isNew) {
			beforeBean =  licenseService.getLicenseMasterOne(licenseMaster);
		}
		
		// webpages이 n건일때 0번째 값은 oss Master로 저장.
		String[] webpages = licenseMaster.getWebpages();
		if(webpages != null){
			if(webpages.length >= 1){
				for(String url : webpages){
					if(!isEmpty(url)){
						licenseMaster.setWebpage(url); // 등록된 url 중 공백을 제외한 나머지에서 첫번째 url을 만나게 되면 등록을 함.
						break;
					}
				}
			}
		} else if(webpages == null){
			licenseMaster.setWebpage("");
		}
		
		result = licenseService.registLicenseMaster(licenseMaster);
		
		if(!isNew) {
			afterBean =  licenseService.getLicenseMasterOne(licenseMaster);

			// licnese type이 변경된 경우, 해당 라이선스를 사용하는 oss의 license type을 재확인 한다.
			if(!avoidNull(beforeBean.getLicenseType()).equals(avoidNull(afterBean.getLicenseType()))) {
				typeChangeOssIdList = licenseService.updateOssLicenseType(result);
			}
			
			isChangeName = !beforeBean.getLicenseName().equalsIgnoreCase(afterBean.getLicenseName());
		}
		
		// 싱글톤 정보 refresh
		resCd="10";		//정상
		putSessionObject("defaultLoadYn", true); // 화면 로드 시 default로 리스트 조회 여부 flag 
		
		try{
			History h = licenseService.work(licenseMaster);
			h.sethAction(isEmpty(licenseId) ? CoConstDef.ACTION_CODE_INSERT : CoConstDef.ACTION_CODE_UPDATE);
			historyService.storeData(h);
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		
		// osdd 연동
		boolean successType = false;
		
		try {			
			if(isNew) {
				successType = licenseService.distributeLicense(result, distributionFlag);
			} else if(beforeBean != null && afterBean != null) {
				if(!avoidNull(beforeBean.getLicenseName()).equals(afterBean.getLicenseName())
						|| !avoidNull(beforeBean.getShortIdentifier()).equals(afterBean.getShortIdentifier())
						|| !avoidNull(beforeBean.getLicenseText()).equals(afterBean.getLicenseText())) {
					successType = licenseService.distributeLicense(result, distributionFlag);
				} else {
					successType = true; // license > update 일 경우에 internal url 생성과 상관없는 항목이 update될 경우는 comment를 남기지 않음.
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			CoMail mailBean = new CoMail(isNew ? CoConstDef.CD_MAIL_TYPE_LICENSE_REGIST : isChangeName ? CoConstDef.CD_MAIL_TYPE_LICENSE_RENAME : CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE);
			mailBean.setParamLicenseId(result);
			String comment = licenseMaster.getComment();
			
			if(!successType) { // internal url 생성 실패시 comment 남김
				comment += (isEmpty(comment) ? "" : "<br>") + "[Error] An error occurred when creating an internal URL file.";
			}
			
			mailBean.setComment(comment);
			
			if(!isNew) {
				
				mailBean.setParamOssList(typeChangeOssIdList);
				
				// code convert
				if(beforeBean != null) {
					beforeBean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, beforeBean.getLicenseType()));
					beforeBean.setObligation(CommonFunction.makeLicenseObligationStr(beforeBean.getObligationNotificationYn(), beforeBean.getObligationDisclosingSrcYn(), beforeBean.getObligationNeedsCheckYn()));
					beforeBean.setModifiedDate(DateUtil.dateFormatConvert(beforeBean.getModifiedDate(), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
					beforeBean.setModifier(CoMailManager.getInstance().makeUserNameFormat(beforeBean.getModifier()));
					beforeBean.setDescription(CommonFunction.lineReplaceToBR(beforeBean.getDescription()));
					beforeBean.setLicenseText(CommonFunction.lineReplaceToBR(beforeBean.getLicenseText()));
					beforeBean.setAttribution(CommonFunction.lineReplaceToBR(beforeBean.getAttribution()));
					beforeBean.setWebpage(licenseService.webPageStringFormat(beforeBean.getWebpages()));
				}
				
				if(afterBean != null) {
					afterBean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, afterBean.getLicenseType()));
					afterBean.setObligation(CommonFunction.makeLicenseObligationStr(afterBean.getObligationNotificationYn(), afterBean.getObligationDisclosingSrcYn(), afterBean.getObligationNeedsCheckYn()));
					afterBean.setModifiedDate(DateUtil.dateFormatConvert(afterBean.getModifiedDate(), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
					afterBean.setModifier(CoMailManager.getInstance().makeUserNameFormat(afterBean.getModifier()));
					afterBean.setDescription(CommonFunction.lineReplaceToBR(afterBean.getDescription()));
					afterBean.setLicenseText(CommonFunction.lineReplaceToBR(afterBean.getLicenseText()));
					afterBean.setAttribution(CommonFunction.lineReplaceToBR(afterBean.getAttribution()));
					afterBean.setWebpage(licenseService.webPageStringFormat(afterBean.getWebpages()));
				}
				
				mailBean.setCompareDataBefore(beforeBean);
				mailBean.setCompareDataAfter(afterBean);
			}
			
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try{
			// RESTRICTION(2) => Network Redistribution
			String netWorkRestriction = CoConstDef.CD_LICENSE_NETWORK_RESTRICTION;
			
			if(isNew) {
				if(licenseMaster.getRestriction().contains(netWorkRestriction)){
					licenseService.registNetworkServerLicense(licenseMaster.getLicenseId(), "NEW");
				}
			} else {
				String type = "";
				
				if(beforeBean.getRestriction().contains(netWorkRestriction) && afterBean.getRestriction().contains(netWorkRestriction)){
					type = "";
				}else if(beforeBean.getRestriction().contains(netWorkRestriction) && !afterBean.getRestriction().contains(netWorkRestriction)){
					type = "DEL";
				}else if(!beforeBean.getRestriction().contains(netWorkRestriction) && afterBean.getRestriction().contains(netWorkRestriction)){
					type = "INS";
				}
				
				licenseService.registNetworkServerLicense(licenseMaster.getLicenseId(), type);
			}
		} catch (Exception e){
			
		}
		
		resMap.put("resCd", resCd);
		resMap.put("licenseId", result);
		
		
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
			if(!isEmpty(licenseMaster.getLicenseName()) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseMaster.getLicenseName().trim().toUpperCase())) {
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
}
