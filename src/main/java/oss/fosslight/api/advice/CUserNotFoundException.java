/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

import java.io.Serial;

public class CUserNotFoundException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;
    
    public CUserNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CUserNotFoundException(String msg) {
        super(msg);
    }
    
    public CUserNotFoundException() {
        super();
    }
}
