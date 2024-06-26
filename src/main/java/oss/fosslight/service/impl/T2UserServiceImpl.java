/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.api.advice.CSigninFailedException;
import oss.fosslight.api.advice.CUserNotFoundException;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2Authorities;
import oss.fosslight.domain.T2Roles;
import oss.fosslight.domain.T2Users;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.T2AuthoritiesMapper;
import oss.fosslight.repository.T2RolesMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.FileService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.JwtUtil;
import oss.fosslight.util.StringUtil;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * The Class T2UserServiceImpl.
 */
@Service("userService")
@Slf4j
public class T2UserServiceImpl implements T2UserService {
	
	@Autowired Environment env;
	
	// Service
	@Autowired FileService fileService;
	
	// Mapper
	@Autowired T2UserMapper userMapper;
	@Autowired T2AuthoritiesMapper authMapper;
	@Autowired T2RolesMapper roleMapper;
	@Autowired ProjectMapper projectMapper;
	@Autowired PartnerMapper partnerMapper;
	
	@Override
	public List<T2Users> getAllUsers(T2Users t2Users) {
		return userMapper.getAllUsers(t2Users);
	}
	
	@Override
	public T2Users getUser(T2Users user) {
		return userMapper.getUser(user);
	}
	
	@Override
	public T2Users getUserAndAuthorities(T2Users user) {
		user = userMapper.getUser(user);
		user.setAuthoritiesList(authMapper.selectAuthoritiesByUser(user));

		return user;
	}
	
	@Override
	public JsonObject selectDuplicateId(String userId) {
		int resultCount = userMapper.selectDuplicateId(userId);
		String dupl = resultCount == 0 ? "ok" : "no";

		JsonObject result = new JsonObject();
		result.addProperty("result", dupl);
		
		return result;
	}
	
	@Transactional
	@Override
	public int addNewUsers(T2Users t2Users) {
		int result = 0;
		t2Users.setCreatedDateCurrentTime();
		// 1. 사용자 등록
		result = userMapper.insertUsers(t2Users);

		// 2. 사용자 기본 권한 부여
		// 2-1. 기본 권한이 Table에 등록되어있는지 확인하여 없을 경우 Insert한다.
//		AppConstBean.SECURITY_ROLE_DEFAULT
		T2Roles defaultRole = new T2Roles();
		defaultRole.setAuthority(AppConstBean.SECURITY_ROLE_DEFAULT);
		defaultRole = roleMapper.getRoles(defaultRole);
		
		if (defaultRole== null){
			defaultRole = new T2Roles();
			defaultRole.setupDefaultRoleData();
			
			roleMapper.insertRoles(defaultRole);
		}
		// 2-2. 사용자에 기본 권한을 등록해준다.
		T2Authorities t2Authorities = new T2Authorities();
		t2Authorities.setUserId(t2Users.getUserId());
		t2Authorities.setAuthority(AppConstBean.SECURITY_ROLE_DEFAULT);

		authMapper.insertAuthorities(t2Authorities);
		
		// watcher invated 여부 체크
		try {
			
			List<Project> prjList = null;
			List<PartnerMaster> partnerList = null;
//			List<Map<String, Object>> batList = null;
			
			if (CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES)) {
				// project watcher 초대여부
				prjList = projectMapper.getWatcherListByEmail(t2Users.getEmail());
				userMapper.updateProjectWatcherUserInfo(t2Users);		
			}
			
			if (CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES)) {
				partnerList = partnerMapper.getWatcherListByEmail(t2Users.getEmail());
				userMapper.updatePartnerWatcherUserInfo(t2Users);
			}
			
			if (prjList != null) {
				// 진행중인 프로젝트에 대해서 creator에세 메일을 발송
				for (Project bean : prjList) {
					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_REGISTED);
					mailBean.setParamPrjId(bean.getPrjId());
					mailBean.setParamUserId(t2Users.getUserId());
					
					CoMailManager.getInstance().sendMail(mailBean);
				}
			}
			
			if (partnerList != null) {
				// 진행중인 프로젝트에 대해서 creator에세 메일을 발송
				for (PartnerMaster bean : partnerList) {
					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_REGISTED);
					mailBean.setParamPartnerId(bean.getPartnerId());
					mailBean.setParamUserId(t2Users.getUserId());
					
					CoMailManager.getInstance().sendMail(mailBean);
				}
			}
			
//			if (batList != null) {
//				// 진행중인 프로젝트에 대해서 creator에세 메일을 발송
//				for (Map<String, Object> bean : batList) {
//					String batId = (String) bean.get("baId");
//					CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_BAT_WATCHER_REGISTED);
//					mailBean.setParamBatId(batId);
//					mailBean.setParamUserId(t2Users.getUserId());
//					
//					CoMailManager.getInstance().sendMail(mailBean);
//				}
//			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return result;
	}
	
	@Override
	public int changEnabled(T2Users t2Users) {
		String enabled = t2Users.getEnabled() == "true" ? "1" : "2";
		t2Users.setEnabled(enabled);
		// 사용여부 변경
		return userMapper.changeEnabled(t2Users);
	}
	
	@Override
	public int updateUsers(T2Users t2Users) {
		return userMapper.updateUsers(t2Users);
	}
	
	@Override
	public int modifyUserRoles(T2Users t2Users) {
		// 0. 사용자 ID가 없을 경우 0 리턴
		if (null == t2Users.getUserId() || t2Users.getUserId().trim().equals("")) {
			return 0;
		}
		
		// 1. 사용자의 모든 역할 삭제
		authMapper.deleteAuthoritiesByUserId(t2Users);
		
		// 2. 모든 사용자가 가지고 있는 기본 권한 추가
		T2Roles defaultRole = new T2Roles();
		defaultRole.setupDefaultRoleData();
		T2Authorities authority = new T2Authorities();
		authority.setAuthority(defaultRole.getAuthority());
		authority.setUserId(t2Users.getUserId());
		
		authMapper.insertAuthorities(authority);
				
		// 3. 사용자의 역할 1건씩 등록
		List<T2Authorities> userAuthorityList = t2Users.getAuthoritiesList();
		
		if (null != userAuthorityList){
			for (T2Authorities authorityItem: userAuthorityList){
				authorityItem.setUserId(t2Users.getUserId());
				authMapper.insertAuthorities(authorityItem);
			}
		}
		
		return 1;
	}
	
	@SuppressWarnings("serial")
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return new UserDetails() {
			@Override
			public boolean isEnabled() {
				return false;
			}
			
			@Override
			public boolean isCredentialsNonExpired() {
				return false;
			}
			
			@Override
			public boolean isAccountNonLocked() {
				return false;
			}
			
			@Override
			public boolean isAccountNonExpired() {
				return false;
			}
			
			@Override
			public String getUsername() {
				return null;
			}
			
			@Override
			public String getPassword() {
				return null;
			}
			
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}
		};
	}

	@Override
	public String getPassword(T2Users user) {
		T2Users result = userMapper.getPassword(user);
		
		if (result != null) {
			return result.getPassword();
		}
		
		return null;
	}
	
	/**
	 * 유저 목록 조회
	 */
	@Override
	public Map<String,Object> getUserList(T2Users t2Users){
		HashMap<String, Object> map = new HashMap<String, Object>();
		int records = userMapper.selectUserTotalCount(t2Users);
		t2Users.setCurPage(t2Users.getPage());
		t2Users.setPageListSize(t2Users.getRows());
		t2Users.setTotListSize(records);
		List<T2Users> userList = new ArrayList<T2Users>();
		
		userList = userMapper.selectParamUserList(t2Users);
		
		map.put("page", t2Users.getPage());
		map.put("total", t2Users.getTotBlockSize());
		map.put("records", records);
		map.put("rows", userList);
		
		return map;
	}

	/**
	 * 유저 저장
	 */
	@Override
	@Transactional
	public void setUser(T2Users vo) throws Exception {
		vo.setCreator(vo.getUserId());
		vo.setModifier(vo.getUserId());
		
		userMapper.insertUsers(vo);
		userMapper.insertAuthority(vo);
	}

	@Override
	public void modUser(List<T2Users> vo) {
		for (int i = 0;i<vo.size();i++) {
			vo.get(i).setModifier(vo.get(i).getUserId());
			vo.get(i).setPassword("");
			if (vo.get(i).getDivision() != null && vo.get(i).getDivision().trim().equals("")){
				vo.get(i).setDivision(CoConstDef.CD_USER_DIVISION_EMPTY);
			}
			
			userMapper.updateUsers(vo.get(i));	
			
			if ("V".equals(vo.get(i).getAuthority())) {
				vo.get(i).setAuthority("ROLE_ADMIN");
				userMapper.updateAuthorities(vo.get(i));
			} else {
				vo.get(i).setAuthority("ROLE_USER");
				userMapper.updateAuthorities(vo.get(i));
			}
			
			// statisticsMostUsed > div_no value update
			userMapper.updateStatisticsMostUsedInfo(vo.get(i));
		}		
	}
	
	@Override
	public List<T2Users> getAuthorityUsers(String authority) {
		return userMapper.getAuthorityUsers(authority);
	}

	@Override
	public List<T2Users> getUserListExcel() throws Exception {
		List<T2Users> result = userMapper.selectUserList();
		
		for (int i = 0; i< result.size(); i++){
			String userId = result.get(i).getUserId();
			String userAuth = userMapper.selectAuthority(userId);
			
			if ("ROLE_ADMIN".equals(userAuth)){
				userAuth = "V";
			} else {
				userAuth = "";
			}
			
			result.get(i).setAuthority(userAuth);
		}
		
		return result;
	}

	@Override
	public boolean checkDuplicateId(T2Users vo) {
		String duplicate = userMapper.checkDuplicateId(vo);
		boolean result;
		
		if ("DUPLICATE".equals(duplicate)) {
			result = true;
		} else {
			result = false;
		}
		
		return result;
	}

	@Override
	public List<T2Users> getReviwer() {
		String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
		return userMapper.selectReviwer(CoConstDef.FLAG_YES, ldapFlag);
	}

	@Override
	public List<T2Users> selectAllUsers() {
		return userMapper.selectUserList();
	}
	
	@Override
	public List<T2Users> getAllUsersDivision() {
		return  userMapper.selectAllUsersDivision();
	}

	@Override
	public boolean checkAdAccounts(Map<String, String> userInfo, String idKey, String pwKey, String filter) {
		boolean isAuthenticated = false;
		
		String userId = (String) userInfo.get(idKey);
		String userPw = (String) userInfo.get(pwKey);
		
		String ldapDomain = CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN);
		try {
			LdapContextSource contextSource = new LdapContextSource();
			contextSource.setUrl(CoConstDef.AD_LDAP_LOGIN.LDAP_SERVER_URL.getValue());
			contextSource.setBase("OU=LGE Users,DC=LGE,DC=NET");
			contextSource.setUserDn(userId+ldapDomain);
			contextSource.setPassword(userPw);
			CommonFunction.setSslWithCert();
			contextSource.afterPropertiesSet();
			
			if (StringUtil.isEmpty(filter)) {
				filter = userId;
			}
			
			LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
			ldapTemplate.afterPropertiesSet();
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<String[]> result = ldapTemplate.search(query().where("mail").is(filter), new AttributesMapper() {
				public Object mapFromAttributes(Attributes attrs) throws NamingException {
					return new String[]{(String)attrs.get("mail").get(), (String)attrs.get("displayname").get()};
				}
			});
			if(result != null && result.size() > 0) {
				isAuthenticated = true;
				for(int i=0;i<result.size();i++) {
					String email = result.get(i)[0];
					if (!StringUtil.isEmpty(email)) {
						userInfo.put("EMAIL", email);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return isAuthenticated;
	}
	
	@Override
	public List<T2Users> checkEmail(String email) {
		return  userMapper.selectCheckEmail(email);
	}

	@Override
	public List<T2Users> getUserListByName(String name) {
		return userMapper.getUserListByName(name);
	}

	/**
	 * Gets the login user info.
	 *
	 * @return the login user info
	 */
	@Override
	public T2Users getLoginUserInfo() {
		T2Users bean = new T2Users();
		bean.setUserId(bean.getLoginUserName());
		
		return userMapper.getUser(bean);
	}

	@Override
	public boolean isLeavedMember(String userId) {
		T2Users param = new T2Users();
		param.setUserId(userId);
		T2Users bean = userMapper.getUser(param);
		
		return bean == null || CoConstDef.FLAG_NO.equals(bean.getUseYn());
	}

	/**
	 * Check api user auth.
	 * API 요청시 사용자 ID, 패스워드 인증 처리
	 * 인증성공인 경우 사용자 정보를 반환한다.
	 * 인증실패인 경우 결과를 return 하지 않고 Exception처리한다.
	 *
	 * @param id the id
	 * @param password the password
	 * @return the t 2 users
	 */
	@Override
	public T2Users checkApiUserAuth(String _token) {
		T2Users params = new T2Users();
		params.setToken(_token);
		params = getUser(params); // 등록된 token 여부 확인
		
		if (params == null) {
			// 미등록 token
			throw new CUserNotFoundException();
		}
		
		// Token 인증
		if (checkToken(params, _token)) { // 추출된 USER 정보로 동일한 token이 생성이 되는지 확인.
            return getUserAndAuthorities(params);
        } else {
            throw new CSigninFailedException();
        }
	}

	@Override
	public T2Users changeSession(String userId) {
		T2Users params = new T2Users();
		params.setUserId(userId);
		params = getUser(params);

		if (params == null) {
			throw new CUserNotFoundException();
		}

		return setSession(params);
	}

	private T2Users setSession(T2Users params) {
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		T2Users getUser = getUserAndAuthorities(params);
		for (T2Authorities auth : getUser.getAuthoritiesList()) {
			roles.add(new SimpleGrantedAuthority(auth.getAuthority()));
		}

		Authentication authentication = new UsernamePasswordAuthenticationToken(params.getUserId(), null, roles);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return getUser;
	}
	
	@Override
	public T2Users changeSession(String userId) {
		T2Users params = new T2Users();
		params.setUserId(userId);
		params = getUser(params);

		if (params == null) {
			throw new CUserNotFoundException();
		}

		return setSession(params);
	}

	private T2Users setSession(T2Users params) {
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		T2Users getUser = getUserAndAuthorities(params);
		for (T2Authorities auth : getUser.getAuthoritiesList()) {
			roles.add(new SimpleGrantedAuthority(auth.getAuthority()));
		}

		Authentication authentication = new UsernamePasswordAuthenticationToken(params.getUserId(), null, roles);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return getUser;
	}
	
	/**
	 * @param T2Users
	 * @return success Flag
	 * */
	@Override
	@Transactional
	public boolean procToken(T2Users vo) {
		try {
			if (CoConstDef.CD_TOKEN_CREATE_TYPE.equals(vo.getTokenType())) {
				if (StringUtil.isEmpty(vo.getExpireDate())) {
					vo.setExpireDate(CoConstDef.CD_TOKEN_END_DATE);
				}
				
				String expireDate = CommonFunction.removeSpecialChar(vo.getExpireDate(), 8);
				
				JwtUtil jwt = new JwtUtil(env.getProperty("token.secret.key") + expireDate);
				String tokenKey = jwt.createToken(vo.getUserId(), vo.getEmail());
				vo.setToken(tokenKey);
			}
			
			if (!StringUtil.isEmpty(vo.getToken())) {
				int successCnt = userMapper.procToken(vo);
				
				return successCnt != 1 ? false : true;
			}
			
			return false;
		} catch (Exception e) {
			log.error(e.getMessage());
			
			return false;
		}
	}
	
	private boolean checkToken(T2Users vo, String _token) {
		String expireDate = CommonFunction.removeSpecialChar(vo.getExpireDate(), 8);
		JwtUtil jwt = new JwtUtil(env.getProperty("token.secret.key") + expireDate);
		String tokenKey = jwt.createToken(vo.getUserId(), vo.getEmail());
		
		return _token.equals(tokenKey);
	}
	
	public boolean checkPassword(String rawPassword, T2Users bean) {
		T2Users userInfo = userMapper.getPassword(bean);
		if (userInfo == null) {
			return false;
		}
		String encPassword = userInfo.getPassword();
		return rawPassword.equals(encPassword) || new BCryptPasswordEncoder().matches(rawPassword, encPassword);
	}

	@Override
	public boolean existUserIdOrEmail(String userId) {
		return userMapper.existUserIdOrEmail(userId) > 0;
	}

	@Override
	public int updateUserNameDivision(T2Users userInfo) {
		HashMap<String, Object> info = new HashMap<String, Object>();
		info.put("sessUserInfo", userInfo);
		SecurityContext sec = SecurityContextHolder.getContext();
		AbstractAuthenticationToken auth = (AbstractAuthenticationToken)sec.getAuthentication();
		auth.setDetails(info);
		return userMapper.updateUserNameDivision(userInfo);
	}

	private Hashtable<String, String> makeLdapProperty() {
		String LDAP_SEARCH_DOMAIN = CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN);
		String LDAP_SEARCH_ID = CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_ID);
		String LDAP_SEARCH_PW = CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_PW);

		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, CoConstDef.AD_LDAP_LOGIN.INITIAL_CONTEXT_FACTORY.getValue());
		properties.put(Context.PROVIDER_URL, CoConstDef.AD_LDAP_LOGIN.LDAP_SERVER_URL.getValue());
		properties.put(Context.SECURITY_AUTHENTICATION, "simple");
		properties.put(Context.SECURITY_PRINCIPAL, LDAP_SEARCH_ID + LDAP_SEARCH_DOMAIN);
		properties.put(Context.SECURITY_CREDENTIALS, LDAP_SEARCH_PW);

		return properties;
	}

	private LdapContextSource makeLdapContextSource() {
		String LDAP_SEARCH_DOMAIN = CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN);
		String LDAP_SEARCH_ID = CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_ID);
		String LDAP_SEARCH_PW = CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_PW);
		
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl(CoConstDef.AD_LDAP_LOGIN.LDAP_SERVER_URL.getValue());
		contextSource.setBase("OU=LGE Users,DC=LGE,DC=NET");
		contextSource.setUserDn(LDAP_SEARCH_ID+LDAP_SEARCH_DOMAIN);
		contextSource.setPassword(LDAP_SEARCH_PW);
		
		return contextSource;
	}
	
	public String[] checkUserInfo(T2Users userInfo) {
		return checkUserInfo(userInfo, makeLdapContextSource());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String[] checkUserInfo(T2Users userInfo, LdapContextSource contextSource) {
		String[] result = new String[3];
		String userId = !StringUtil.isEmptyTrimmed(userInfo.getUserId()) ? userInfo.getUserId() : userInfo.getCreator();
		String ldapSearchID = CoCodeManager.getCodeExpString(CoConstDef.CD_LDAP_SEARCH_INFO, CoConstDef.CD_DTL_LDAP_SEARCH_ID);

		try {
			CommonFunction.setSslWithCert();
			contextSource.afterPropertiesSet();
			
			LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
			ldapTemplate.afterPropertiesSet();
			
			if(ldapTemplate.authenticate("", String.format("(cn=%s)", ldapSearchID), contextSource.getPassword())) {
				List<String[]> searchResult = ldapTemplate.search(query().where("cn").is(userId), new AttributesMapper() {
					public Object mapFromAttributes(Attributes attrs) throws NamingException {
						return new String[]{(String)attrs.get("mail").get(), (String)attrs.get("displayname").get()};
					}
				});
				if(searchResult != null && searchResult.size() > 0) {
					int cnt = 1;
					for(int i=0;i<searchResult.size();i++) {
						String email = searchResult.get(i)[0];
						String displayName = searchResult.get(i)[1];
						
						if (StringUtil.isEmptyTrimmed(displayName)) {
							result[0] = email.split("@")[0];
						} else{
							result[0] = displayName.replaceAll("\\("+email+"\\)", "").trim();
						}
						
						if (!StringUtil.isEmptyTrimmed(userInfo.getEmail())) {
							if (email.equals(userInfo.getEmail().trim())) {
								result[1] = email;
								result[2] = String.valueOf(cnt);
								break;
							} else {
								result[1] = "";
								result[2] = String.valueOf(cnt);
							}
						} else {
							result[1] = email;
							result[2] = String.valueOf(cnt++);
						}
					}
				}
			}
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return null;
		}
	}

	@Override
	public T2Users checkApiUserAuthAndSetSession(String _token) {
		T2Users params = new T2Users();
		params.setToken(_token);
		params = getUser(params);
		
		if (params == null) {
			throw new CUserNotFoundException();
		}
		
		if (checkToken(params, _token)) {
			List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
			T2Users getUser = getUserAndAuthorities(params);
			for (T2Authorities auth : getUser.getAuthoritiesList()) {
            	roles.add(new SimpleGrantedAuthority(auth.getAuthority()));
            }
			
			Authentication authentication = new UsernamePasswordAuthenticationToken(params.getUserId(), null, roles);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			return getUser;
        } else {
            throw new CSigninFailedException();
        }
	}

	@Override
	public boolean isAdmin(String _token) {
		T2Users params = new T2Users();
		params.setToken(_token);
		params = getUser(params);

		if (params == null) {
			throw new CUserNotFoundException();
		}

		if (params.getAuthority().equals("ROLE_ADMIN")) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkByADUser(String user_id, String user_pw, Map<String, Object> rtnMap) {
		String rtnEmail = "";
		if (rtnMap.containsKey("email")) {
			rtnEmail = (String) rtnMap.get("email");
		}
		
		List<String[]> searchResult = null;
		
		if (StringUtil.isNotEmpty(user_pw)) {
			String ldapDomain = CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN);

			try {
				LdapContextSource contextSource = new LdapContextSource();
				contextSource.setUrl(CoConstDef.AD_LDAP_LOGIN.LDAP_SERVER_URL.getValue());
				contextSource.setBase("OU=LGE Users, DC=LGE, DC=NET");
				contextSource.setUserDn(user_id+ldapDomain);
				contextSource.setPassword(user_pw);
				CommonFunction.setSslWithCert();
				contextSource.afterPropertiesSet();

				LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
				ldapTemplate.afterPropertiesSet();
				
				if (ldapTemplate.authenticate("", String.format("(cn=%s)", user_id), user_pw)) {
					searchResult = ldapTemplate.search(query().where("cn").is(user_id), new AttributesMapper() {
						public Object mapFromAttributes(Attributes attrs) throws NamingException {
							return new String[]{(String)attrs.get("mail").get(), (String)attrs.get("displayname").get()};
						}
					});
						
					rtnMap.put("isAuthenticated", true);
				} else {
					rtnMap.put("isAuthenticated", false);
					return rtnMap;
				}
			} catch (Exception e) {
				log.error("ERROR Message :" + e.getMessage());
				rtnMap.put("isAuthenticated", false);
				return rtnMap;
			}
			
			// 사용자 가입여부 체크
			if (!existUserIdOrEmail(user_id)){
				String userName = "";
				String userEmail = "";
				String userEmailCnt = "";
				
				if (searchResult != null) {
					int cnt = 1;
					for(int i=0;i<searchResult.size();i++) {
						String[] userInfo = searchResult.get(i);
						String email = userInfo[0];
						String displayName = userInfo[1];
						
						if (StringUtil.isEmptyTrimmed(displayName)) {
							userName = email.split("@")[0];
						} else{
							userName = displayName.replaceAll("\\("+email+"\\)", "").trim();
						}
						
						if (!StringUtil.isEmptyTrimmed(rtnEmail)) {
							if (email.equals(rtnEmail.trim())) {
								userEmail = email;
								userEmailCnt = String.valueOf(cnt);
								break;
							} else {
								userEmail= "";
								userEmailCnt = String.valueOf(cnt);
							}
						} else {
							userEmail = email;
							userEmailCnt = String.valueOf(cnt++);
						}
					}
				}
				
				if (StringUtil.isEmptyTrimmed(userEmail) || StringUtil.isEmptyTrimmed(userName)) {
					log.info("Cannot find Ldap user information : " + user_id);
					rtnMap.put("isAuthenticated", false);
					return rtnMap;
				}
				
				if (Integer.parseInt(userEmailCnt) > 1 && StringUtil.isNotEmpty(userEmail)) {
					log.info("ldap user email duplicate : " + user_id);
					rtnMap.put("isAuthenticated", false);
					rtnMap.put("msg", "enter email");
					
					return rtnMap;
				}
				
				T2Users vo = new T2Users();
				vo.setUserId(user_id);
				vo.setCreatedDateCurrentTime();
				vo.setCreator(user_id);
				vo.setModifier(user_id);
				vo.setEmail(rtnEmail);
				vo.setEmail(userEmail);
				vo.setUserName(userName);
				vo.setDivision(CoConstDef.CD_USER_DIVISION_EMPTY);

				addNewUsers(vo);
			}
		}
		return rtnMap;
	}

	@Override
	public boolean checkSystemUser(String userId, String rawPassword) {
		T2Users param = new T2Users();
		param.setUserId(userId);
		
		return checkPassword(rawPassword, param);
	}
}
