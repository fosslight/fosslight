/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.advice;

public class CProjectNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    public CProjectNotAvailableException(String msg, Throwable t) {
        super(msg, t);
    }

    public CProjectNotAvailableException(String msg) {
        super(msg);
    }

    public CProjectNotAvailableException() {
        super();
    }
}
