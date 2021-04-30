/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import oss.fosslight.domain.T2Roles;

public interface T2RolesService {
	public List<T2Roles> getAllRoles();

	public List<T2Roles> getAllRolesAndSecuredResourcesRole();
	
	public T2Roles getRoles(T2Roles role);
	
	public T2Roles getRolesAndSecuredResourcesRole(T2Roles roles);
	
	@Transactional
	public int modifyRolesAndSecuredResourcesRole(T2Roles roles);
	
	@Transactional
	public int registRolesAndSecuredResourcesRole(T2Roles roles);
	
	@Transactional
	public int deleteRolesAndSecuredResourcesRole(String[] roleIds);
}
