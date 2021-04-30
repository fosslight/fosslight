/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.T2SecuredResources;

@Mapper
public interface T2SecuredResourcesMapper {
	public List<T2SecuredResources> getAllSecuredResources();
	
	public List<T2SecuredResources> getAllSecuredResourcesAndSecuredResourcesRoles();
	
	public T2SecuredResources getSecuredResources(T2SecuredResources securedResources);
	
	public T2SecuredResources getSecuredResourcesAndSecuredResourcesRoles(T2SecuredResources securedResources);
	
	public int updateSecuredResources(T2SecuredResources securedResource);
	
	public int insertSecuredResource(T2SecuredResources securedResource);
	
	public int deleteSecuredResourceByResourceId(String resourceId);
}