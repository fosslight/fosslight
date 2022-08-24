/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import java.util.*;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.T2Authorities;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;

@Component
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {
	@Autowired T2UserService userService;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String user_id = (String)authentication.getPrincipal();     
        String user_pw = (String)authentication.getCredentials();
        boolean loginSuccess = false;
        String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
        List<String> customAccounts = Arrays.asList(CommonFunction.emptyCheckProperty("custom.accounts", "").split(","));
        
        if(CoConstDef.FLAG_YES.equals(ldapFlag) && !customAccounts.contains(user_id)) {
        	loginSuccess = checkByADUser(user_id, user_pw);
        } else {
        	loginSuccess = checkSystemUser(user_id, user_pw);
        }
        
        if(loginSuccess) {
            List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
            T2Users user = new T2Users();
            user.setUserId(user_id);
            T2Users getUser = userService.getUserAndAuthorities(user);
            
            for(T2Authorities auth : getUser.getAuthoritiesList()) {
            	roles.add(new SimpleGrantedAuthority(auth.getAuthority()));
            }
            
            return new UsernamePasswordAuthenticationToken(user_id, user_pw, roles);          
        } else {
            throw new BadCredentialsException("Bad credentials");
        }
	}
	
	private boolean checkSystemUser(String user_id, String user_pw) {
		T2Users param = new T2Users();
		param.setUserId(user_id);
		
		return userService.checkPassword(user_pw, param);
	}
	
	private boolean checkByADUser(String user_id, String user_pw) {
		boolean isAuthenticated = false;

		if (StringUtil.isNotEmpty(user_pw)) {
			
			// 사용자 가입여부 체크
			if(!userService.existUserIdOrEmail(user_id)){
				Map<String, String> userInfo = new HashMap<>();
				T2Users vo = new T2Users();
				vo.setUserId(user_id);
				vo.setCreatedDateCurrentTime();
				vo.setCreator(user_id);
				vo.setModifier(user_id);


				String[] info = userService.checkUserInfo(vo);
				String userName = info[0];
				String userEmail = info[1];
				if(StringUtil.isEmptyTrimmed(userEmail) || StringUtil.isEmptyTrimmed(userName)) {
					log.debug("Cannot find Ldap user information : " + vo.getUserId());
					return false;
				}
				vo.setEmail(userEmail);
				vo.setUserName(userName);
				vo.setDivision(CoConstDef.CD_USER_DIVISION_EMPTY);

				userService.addNewUsers(vo);
			}
			
			String ldapDomain = CoCodeManager.getCodeExpString(CoConstDef.CD_LOGIN_SETTING, CoConstDef.CD_LDAP_DOMAIN);
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, CoConstDef.AD_LDAP_LOGIN.INITIAL_CONTEXT_FACTORY.getValue());
			properties.put(Context.PROVIDER_URL, CoConstDef.AD_LDAP_LOGIN.LDAP_SERVER_URL.getValue());
			properties.put(Context.SECURITY_AUTHENTICATION, "simple");
			properties.put(Context.SECURITY_PRINCIPAL, user_id+ldapDomain);
			properties.put(Context.SECURITY_CREDENTIALS, user_pw);

			DirContext con = null;
			try {
				con = new InitialDirContext(properties);
				isAuthenticated = true;
			}catch (NamingException e) {
				log.warn("LDAP NamingException userId : " + user_id + " ERROR Message :" + e.getMessage());
			} finally {
				if(con != null) {
					try {
						con.close();
					} catch (Exception e) {}
				}
			}
		}
		return isAuthenticated;
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}
