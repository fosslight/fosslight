/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.USER;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.CodeService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2BasicValidator;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoAdminValidator;

@Controller
@Slf4j
public class UserController extends CoTopComponent {
	@Autowired T2UserService userService;
	@Autowired CodeService codeService;
	
	@GetMapping(value=USER.LIST, produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		return USER.LIST_JSP;
	}
	
	@GetMapping(value=USER.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> userList(
			HttpServletRequest req
			, HttpServletResponse res
			, @ModelAttribute T2Users t2User
			, Model model){
		
		return makeJsonResponseHeader(userService.getUserList(t2User));
	}
	
	/**
	 * [API] 유저 저장 
	 */
	@PostMapping(value=USER.SAVE_AJAX,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public @ResponseBody ResponseEntity<Object> userSave(
			@ModelAttribute T2Users vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		
		// validation check
		T2CoValidationResult vResult = validate(req);
		
		if(!vResult.isValid()) {
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		Map<String, String> dataMap = vResult.getDataMap();
		Map<String, String> validResultMap = vResult.getValidDataMap();

		vo.setCreatedDateCurrentTime();
		vo.setCreator(vo.getUserId());
		vo.setModifier(vo.getUserId());
		vo.setEmail((String) dataMap.get("EMAIL"));
		
		String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
		
		if(!CoConstDef.FLAG_YES.equals(ldapFlag)) {
			vo.setPassword(encodePassword((String) validResultMap.get("USER_PW")));
		}
		
		// 선택된 division이 없을경우 N/A로 선택됨.
		if(isEmpty(vo.getDivision())){
			vo.setDivision(CoConstDef.CD_USER_DIVISION_EMPTY);
		}
		
		userService.addNewUsers(vo);
		
		return makeJsonResponseHeader();
	}
	
	/**
	 * [API] 유저 수정 
	 */
	@PostMapping(value=USER.MOD_AJAX,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public @ResponseBody ResponseEntity<Object> userMod(
			@RequestBody List<T2Users> vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		userService.modUser(vo);
		
		return makeJsonResponseHeader(map);
	}
	
	@PostMapping(value = USER.DIVISION_LIST)
	public @ResponseBody ResponseEntity<Object> getDivisionList(@RequestBody T2CodeDtl t2CodeDtl, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		List<T2CodeDtl> list = null;
		
		try {
			list = codeService.getCodeDetailList(t2CodeDtl);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(list);
	}

	@GetMapping(value = USER.CHECK_EMAIL)
	public @ResponseBody ResponseEntity<Object> checkEmail(HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, String> resMap = new HashMap<>();
		String email = req.getParameter("email");
		List<T2Users> list = userService.checkEmail(email);
		
		if(list.size() > 0){
			resMap.put("isValid", "true");
			resMap.put("userId", list.get(0).getUserId());
			resMap.put("division", list.get(0).getDivision());
			resMap.put("divisionName", list.get(0).getDivisionName());
		}else{
			resMap.put("isValid", "false");
		}
		
		return makeJsonResponseHeader(resMap);
	}
	
	@GetMapping(value = USER.AUTOCOMPLETE_CRAETOR_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteCreatorAjax(T2Users t2Users, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		t2Users.setSortField("userName");
		t2Users.setSortOrder("asc");
		
		List<T2Users> list = userService.getAllUsers(t2Users);
		
		return makeJsonResponseHeader(list);
	}
	
	@GetMapping(value=USER.AUTOCOMPLETE_REVIEWER_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteReviewerAjax(T2Users t2Users, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		List<T2Users> list = userService.getReviwer();
		
		return makeJsonResponseHeader(list);
	}
	
	@GetMapping(value=USER.AUTOCOMPLETE_CREATOR_DIVISION_AJAX)
	public @ResponseBody ResponseEntity<Object> autoCompleteCreatorDivisionAjax(T2Users t2Users, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		List<T2Users> list = userService.getAllUsersDivision();
		
		return makeJsonResponseHeader(list);
	}
	
	@PostMapping(value=USER.CHANGE_PASSWORD)
	public  @ResponseBody ResponseEntity<Object> changePassword(
			@RequestBody T2Users bean
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		Map<String, Object> resMap = new HashMap<String, Object>();
		String userId = avoidNull(bean.getUserId(), loginUserName());
		String userPw = avoidNull(bean.getPassword(), bean.getUserId());
		T2Users userInfo = new T2Users();
		
		try {
			userInfo.setUserId(userId);
			userInfo = userService.getUser(userInfo);
			userInfo.setPassword(encodePassword(userPw)); // password encoding
			userInfo.setModifier(loginUserName());
			
			int updateCnt = userService.updateUsers(userInfo);
			
			if(updateCnt == 1) {
				resMap.put("resCd", "10");
			} else {
				resMap.put("resCd", "20");
				resMap.put("resCd", "password change Failure.");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			resMap.put("resCd", "20");
			resMap.put("resCd", "password change Failure.");
		}
		
		return makeJsonResponseHeader(resMap);
	}
	
	@PostMapping(value=USER.UPDATE_USERNAME_DIVISION)
	public  @ResponseBody ResponseEntity<Object> updateUserNameDivision(
			@RequestBody Map<String, String> params
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		T2Users userInfo = new T2Users();
		try {
			params.put("USER_NAME", params.get("userName").trim());
			params.put("DIVISION", params.get("division").trim());
			if(params.get("password") != null) {
				params.put("PASSWORD", params.get("password").trim());
			}
			T2CoAdminValidator validator = new T2CoAdminValidator();
			T2CoValidationResult vr = validator.validate(params);
			if(!vr.isValid()) {
				return makeJsonResponseHeader(false,  CommonFunction.makeValidMsgTohtml(vr.getValidMessageMap()), vr.getValidMessageMap());
			}
			userInfo.setUserId(loginUserName());
			userInfo.setModifier(loginUserName());
			userInfo.setUserName(params.get("USER_NAME"));
			userInfo.setDivision(params.get("DIVISION"));
			
			String passwd = params.get("PASSWORD");
			if(!StringUtil.isEmpty(passwd)) {
				userInfo.setPassword(encodePassword(passwd)); // password encoding	
			}
			
			userService.updateUserNameDivision(userInfo);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, getMessage("msg.common.valid2"));
		}
		return makeJsonResponseHeader();
	}
	
	@PostMapping(value=USER.TOKEN_PROC)
	public @ResponseBody ResponseEntity<Object> tokenProc(
			@RequestBody HashMap<String, Object> map
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model
			, @PathVariable String procType) throws Exception{
		String mainData = (String) map.get("mainData");
		boolean isSuccess = false;
		
		// Proc token Data
		Type collectionType = new TypeToken<T2Users>() {}.getType();
		T2Users userData = new T2Users();
		userData = (T2Users) fromJson(mainData, collectionType);
		userData.setTokenType(procType);
		
		isSuccess = userService.procToken(userData);
		
		if(isSuccess) {
			// email 발송
			try {
				String emailType = null;
				switch(procType.toUpperCase()) {
					case CoConstDef.CD_TOKEN_CREATE_TYPE:
						emailType = CoConstDef.CD_MAIL_TOKEN_CREATE_TYPE;
						
						break;
					case CoConstDef.CD_TOKEN_DELETE_TYPE:
						emailType = CoConstDef.CD_MAIL_TOKEN_DELETE_TYPE;
						
						break;
					default:
						break;
				}
				
				if(!isEmpty(emailType)) {
					CoMail mailBean = new CoMail(emailType);
					mailBean.setParamUserId(userData.getUserId());
					mailBean.setToIds(new String[] { userData.getUserId() });
					mailBean.setCcIds(new String[] { loginUserName() });
					
					CoMailManager.getInstance().sendMail(mailBean);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		Map<String, Object> resMap = new HashMap<>();
		
		resMap.put("isValid", isSuccess);
		
		return makeJsonResponseHeader(resMap);
	}
}
