/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

public class CUserAuthFailedException extends RuntimeException {
	private final String message;
	private static final long serialVersionUID = 1L;
    
    public CUserAuthFailedException(String msg, Throwable t) {
        this.message = msg;
    }
    
    public CUserAuthFailedException(String msg) {
        this.message = msg;
    }
    
    public String getMessage() {
    	return message;
    }
}
