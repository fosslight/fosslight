/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

public class CProjectNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private ProjectPermissionType permissionType;

    public CProjectNotAvailableException(String msg, ProjectPermissionType permissionType) {
        super(msg);
        this.permissionType = permissionType;
    }

    public CProjectNotAvailableException(String msg, Throwable t) {
        super(msg, t);
        this.permissionType = ProjectPermissionType.EDIT;
    }

    public CProjectNotAvailableException(String msg) {
        super(msg);
        this.permissionType = ProjectPermissionType.EDIT;
    }

    public CProjectNotAvailableException() {
        super();
        this.permissionType = ProjectPermissionType.EDIT;
    }

	public ProjectPermissionType getPermissionType() {
		return permissionType;
	}
}
