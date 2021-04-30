/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.T2SecuredResourcesRole;

@Mapper
public interface T2SecuredResourcesRoleMapper {
	public int insertSecuredResourcesRoleTypeList(List<T2SecuredResourcesRole> securedResourcesRoles);
	
	public int deleteSecuredResourcesRoleByAuthority(String authority);
	
	public int deleteSecuredResourcesRoleByResourceId(String resourceId);
}