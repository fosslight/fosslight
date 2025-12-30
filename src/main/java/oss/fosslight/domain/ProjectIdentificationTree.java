/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

public class ProjectIdentificationTree implements Serializable {
	
	private static final long serialVersionUID = -8851678159383549493L;

	private String level;
	
	private String treeId;
	
	private String parentTreeId;
	
	private String packageUrl;
	
	private String dependencies;
	
	private String excludeYn;
	
	private boolean existDependency;
	
	public ProjectIdentificationTree(String treeId, String parentTreeId, String level, String packageUrl, String dependencies, String excludeYn) {
		this.treeId = treeId;
		this.parentTreeId = parentTreeId;
		this.level = level;
		this.packageUrl = packageUrl;
		this.dependencies = dependencies;
		this.excludeYn = excludeYn;
	}

	public String getTreeId() {
		return treeId;
	}

	public String getParentTreeId() {
		return parentTreeId;
	}

	public String getLevel() {
		return level;
	}
	
	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}
	
	public String getPackageUrl() {
		return packageUrl;
	}
	
	public String getDependencies() {
		return dependencies;
	}

	public String getExcludeYn() {
		return excludeYn;
	}

	public void setExcludeYn(String excludeYn) {
		this.excludeYn = excludeYn;
	}

	public boolean isExistDependency() {
		return existDependency;
	}

	public void setExistDependency(boolean existDependency) {
		this.existDependency = existDependency;
	}
}
