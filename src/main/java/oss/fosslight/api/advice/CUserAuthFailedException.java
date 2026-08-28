/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

public class CUserAuthFailedException extends RuntimeException {
    private String errorCode;
	private static final long serialVersionUID = 1L;
    
    public CUserAuthFailedException(String msg, Throwable t) {
        super(msg, t);
    }

    public CUserAuthFailedException(String errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }
    
    public CUserAuthFailedException(String msg) {
        super(msg);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
