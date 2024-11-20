/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

public class CUserHasNoPermissionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    public CUserHasNoPermissionException(String msg, Throwable t) {
        super(msg, t);
    }

    public CUserHasNoPermissionException(String msg) {
        super(msg);
    }

    public CUserHasNoPermissionException() {
        super();
    }
}
