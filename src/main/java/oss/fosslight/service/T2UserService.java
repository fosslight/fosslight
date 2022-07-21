/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import oss.fosslight.domain.T2Users;

@Service
public interface T2UserService extends UserDetailsService {
	public List<T2Users> getAllUsers(T2Users t2Users);
	public T2Users getUser(T2Users user);
	public List<T2Users> getAuthorityUsers(String authority);
	public T2Users getUserAndAuthorities(T2Users user);
	public JsonObject selectDuplicateId(String userId);
	public int addNewUsers(T2Users t2Users);
	public int changEnabled(T2Users t2Users);
	public int updateUsers(T2Users t2Users);
	public int modifyUserRoles(T2Users member);
	public String getPassword(T2Users user);
	public Map<String,Object> getUserList(T2Users vo);
	public void setUser(T2Users vo) throws Exception;
	public void modUser(List<T2Users> vo);

	public List<T2Users> getUserListExcel() throws Exception;

	public boolean checkDuplicateId(T2Users vo);

	public List<T2Users> getReviwer();

	public List<T2Users> selectAllUsers();
	
	public List<T2Users> getAllUsersDivision();

	public boolean checkAdAccounts(Map<String, String> userInfo, String idKey, String pwKey, String filter);

	public List<T2Users> checkEmail(String email);

	public List<T2Users> getUserListByName(String name);
	
	public T2Users getLoginUserInfo();

	public boolean isLeavedMember(String userId);

	public T2Users checkApiUserAuth(String _token);
	
	public boolean procToken(T2Users vo);
	
	public boolean checkPassword(String rawPassword, T2Users bean);
	
	public boolean existUserIdOrEmail(String userId);
	public int updateUserNameDivision(T2Users userInfo);
	public String[] checkUserInfo(T2Users userInfo) ;
}