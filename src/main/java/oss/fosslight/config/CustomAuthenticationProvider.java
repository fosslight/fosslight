///*
// * Copyright (c) 2021 LG Electronics Inc.
// * SPDX-License-Identifier: AGPL-3.0-only 
// */
//
//package oss.fosslight.config;
//
//import static org.springframework.ldap.query.LdapQueryBuilder.query;
//
//import java.util.*;
//
//import javax.naming.NamingException;
//import javax.naming.directory.Attributes;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.ldap.core.AttributesMapper;
//import org.springframework.ldap.core.LdapTemplate;
//import org.springframework.ldap.core.support.LdapContextSource;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.stereotype.Component;
//
//import lombok.extern.slf4j.Slf4j;
//import oss.fosslight.common.CoCodeManager;
//import oss.fosslight.common.CoConstDef;
//import oss.fosslight.common.CommonFunction;
//import oss.fosslight.domain.T2Authorities;
//import oss.fosslight.domain.T2Users;
//import oss.fosslight.service.T2UserService;
//import oss.fosslight.util.StringUtil;
//
//@Component
//@Slf4j
//public class CustomAuthenticationProvider implements AuthenticationProvider {
//	@Autowired T2UserService userService;
//	
//	@Override
//	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        String user_id = (String)authentication.getPrincipal();     
//        String user_pw = (String)authentication.getCredentials();
//        
//        CustomWebAuthenticationDetails customWebAuthenticationDetails = (CustomWebAuthenticationDetails)authentication.getDetails();
//        String email = customWebAuthenticationDetails.getEmail();
//        
//        boolean loginSuccess = false;
//        Map<String, Object> rtnMap = new HashMap<>();
//        rtnMap.put("email", email);
//        
//        String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
//        List<String> customAccounts = Arrays.asList(CommonFunction.emptyCheckProperty("custom.accounts", "").split(","));
//        
//        if (CoConstDef.FLAG_YES.equals(ldapFlag) && !customAccounts.contains(user_id)) {
//        	rtnMap = checkByADUser(user_id, user_pw, rtnMap);
//        	loginSuccess = (boolean) rtnMap.get("isAuthenticated");
//        } else {
//        	loginSuccess = checkSystemUser(user_id, user_pw);
//        }
//        
//        if (loginSuccess) {
//            List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
//            T2Users user = new T2Users();
//            user.setUserId(user_id);
//            T2Users getUser = userService.getUserAndAuthorities(user);
//            
//            for (T2Authorities auth : getUser.getAuthoritiesList()) {
//            	roles.add(new SimpleGrantedAuthority(auth.getAuthority()));
//            }
//            
//            return new UsernamePasswordAuthenticationToken(user_id, user_pw, roles);          
//        } else {
//        	if (rtnMap.containsKey("msg")) {
//        		throw new BadCredentialsException((String) rtnMap.get("msg"));
//        	} else {
//                throw new BadCredentialsException("Bad credentials");
//        	}
//        }
//	}
//	
//	private boolean checkSystemUser(String user_id, String user_pw) {
//		T2Users param = new T2Users();
//		param.setUserId(user_id);
//		
//		return userService.checkPassword(user_pw, param);
//	}
//	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	private Map<String, Object> checkByADUser(String user_id, String user_pw, Map<String, Object> rtnMap) {
//		String rtnEmail = (String) rtnMap.get("email");
//		List<String[]> searchResult = null;
//		
//		if (StringUtil.isNotEmpty(user_pw)) {
//			String ldapDomain = CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN);
//
//			try {
//				LdapContextSource contextSource = new LdapContextSource();
//				contextSource.setUrl(CoConstDef.AD_LDAP_LOGIN.LDAP_SERVER_URL.getValue());
//				contextSource.setBase("OU=LGE Users, DC=LGE, DC=NET");
//				contextSource.setUserDn(user_id+ldapDomain);
//				contextSource.setPassword(user_pw);
//				CommonFunction.setSslWithCert();
//				contextSource.afterPropertiesSet();
//
//				LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
//				ldapTemplate.afterPropertiesSet();
//				
//				if (ldapTemplate.authenticate("", String.format("(cn=%s)", user_id), user_pw)) {
//					searchResult = ldapTemplate.search(query().where("cn").is(user_id), new AttributesMapper() {
//						public Object mapFromAttributes(Attributes attrs) throws NamingException {
//							return new String[]{(String)attrs.get("mail").get(), (String)attrs.get("displayname").get()};
//						}
//					});
//					
//					rtnMap.put("isAuthenticated", true);
//				} else {
//					rtnMap.put("isAuthenticated", false);
//					return rtnMap;
//				}
//			} catch (Exception e) {
//				log.warn("ERROR Message :" + e.getMessage());
//				rtnMap.put("isAuthenticated", false);
//				return rtnMap;
//			}
//			
//			// 사용자 가입여부 체크
//			if (!userService.existUserIdOrEmail(user_id)){
//				String userName = "";
//				String userEmail = "";
//				String userEmailCnt = "";
//				
//				if (searchResult != null) {
//					int cnt = 1;
//					for(int i=0;i<searchResult.size();i++) {
//						String email = searchResult.get(i)[0];
//						String displayName = searchResult.get(i)[1];
//						
//						if (StringUtil.isEmptyTrimmed(displayName)) {
//							userName = email.split("@")[0];
//						} else{
//							userName = displayName.replaceAll("\\("+email+"\\)", "").trim();
//						}
//						
//						if (!StringUtil.isEmptyTrimmed(rtnEmail)) {
//							if (email.equals(rtnEmail.trim())) {
//								userEmail = email;
//								userEmailCnt = String.valueOf(cnt);
//								break;
//							} else {
//								userEmail= "";
//								userEmailCnt = String.valueOf(cnt);
//							}
//						} else {
//							userEmail = email;
//							userEmailCnt = String.valueOf(cnt++);
//						}
//					}
//				}
//				
//				if (StringUtil.isEmptyTrimmed(userEmail) || StringUtil.isEmptyTrimmed(userName)) {
//					log.debug("Cannot find Ldap user information : " + user_id);
//					rtnMap.put("isAuthenticated", false);
//					return rtnMap;
//				}
//				
//				if (Integer.parseInt(userEmailCnt) > 1 && StringUtil.isNotEmpty(userEmail)) {
//					log.debug("ldap user email duplicate : " + user_id);
//					rtnMap.put("isAuthenticated", false);
//					rtnMap.put("msg", "enter email");
//					
//					return rtnMap;
//				}
//				
//				T2Users vo = new T2Users();
//				vo.setUserId(user_id);
//				vo.setCreatedDateCurrentTime();
//				vo.setCreator(user_id);
//				vo.setModifier(user_id);
//				vo.setEmail(rtnEmail);
//				vo.setEmail(userEmail);
//				vo.setUserName(userName);
//				vo.setDivision(CoConstDef.CD_USER_DIVISION_EMPTY);
//
//				userService.addNewUsers(vo);
//			}
//		}
//		return rtnMap;
//	}
//	
//	@Override
//	public boolean supports(Class<?> authentication) {
//		return true;
//	}
//
//}
