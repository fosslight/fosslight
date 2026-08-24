/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.advice;

/**
 * Thrown when a project referenced by an API call does not exist.
 * Mapped to HTTP 404 Not Found by {@link ApiV2ExceptionAdvice}.
 */
public class CProjectNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CProjectNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    public CProjectNotFoundException(String msg) {
        super(msg);
    }

    public CProjectNotFoundException() {
        super();
    }
}
