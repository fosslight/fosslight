/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;

import oss.fosslight.domain.T2SecuredResources;

public interface T2SecuredResourcesService {
	public List<T2SecuredResources> getAllSecuredResources();
	
	public List<T2SecuredResources> getAllSecuredResourcesAndSecuredResourcesRoles();
	
	public T2SecuredResources getSecuredResources(T2SecuredResources securedResources);
	
	public T2SecuredResources getSecuredResources(String resourceId);
	
	public T2SecuredResources getSecuredResourcesAndSecuredResourcesRoles(String resourceId);
	
	public int modifySecuredResource(T2SecuredResources securedResource);
	
	public int registSecuredResource(T2SecuredResources securedResource);
	
	public int deleteSecuredResourcesAndSecuredResourcesRole(String[] resourceIds);
}
