/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.T2Roles;

@Mapper
public interface T2RolesMapper {
	public List<T2Roles> getAllRoles();
	
	public List<T2Roles> getAllRolesAndSecuredResourcesRole();
	
	public T2Roles getRoles(T2Roles roles);
	
	public T2Roles getRolesAndSecuredResourcesRole(T2Roles roles);
	
	public int updateRoles(T2Roles roles);
	
	public int insertRoles(T2Roles roles);
	
	public int deleteRolesByAuthority(String authority);
}