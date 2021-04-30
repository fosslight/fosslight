/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.T2Users;

@Mapper
public interface T2UserMapper {
	public List<T2Users> getAllUsers(T2Users member);										// 사용자 목록을 가져옵니다.
	
	public T2Users getUser(T2Users member);													// 특정 사용자 정보를 가져옵니다.
	
	public List<T2Users> getAuthorityUsers(String role);									// 특정 권한 사용자 목록을 가져옵니다.
	
	public T2Users getPassword(T2Users member);
	
	public int selectDuplicateId(String userId);											// ID중복여부 체크
	
	public int insertUsers(T2Users member);													// 사용자 등록
	
	public int selectUserTotalCount(T2Users member);										// 리스트 총개수
	
	public int changeEnabled(T2Users member);												// 사이트 사용여부 변경
	
	public int updateUsers(T2Users member);													// 사용자 수정
	
	public List<T2Users> selectUserList();
	
	public List<T2Users> selectParamUserList(T2Users t2Users);
	
	public String selectAuthority(String userId);
	
	public void insertAuthority(T2Users vo);
	
	public void updateAuthorities(T2Users t2Users);
	
	public String checkDuplicateId(T2Users vo);
	
	public List<T2Users> selectReviwer(@Param("adminYn") String adminYn, @Param("ldapFlag") String ldapFlag);
	
	public List<T2Users> selectAllUsersDivision();
	
	public List<T2Users> selectCheckEmail(String email);
	
	public void updateProjectWatcherUserInfo(T2Users bean);
	
	public void updateBatWatcherUserInfo(T2Users bean);
	
	public void updatePartnerWatcherUserInfo(T2Users bean);
	
	public List<T2Users> getUserListByName(String name);
	
	public int procToken(T2Users bean);

	public int existUserIdOrEmail(String userId);
}