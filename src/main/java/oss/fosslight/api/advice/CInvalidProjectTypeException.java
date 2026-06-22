/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

public class CInvalidProjectTypeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    public CInvalidProjectTypeException(String msg, Throwable t) {
        super(msg, t);
    }

    public CInvalidProjectTypeException(String msg) {
        super(msg);
    }

    public CInvalidProjectTypeException() {
        super();
    }
}
