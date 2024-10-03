/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

import java.io.Serial;

public class CSigninFailedException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;
    
    public CSigninFailedException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CSigninFailedException(String msg) {
        super(msg);
    }
    
    public CSigninFailedException() {
        super();
    }
}
