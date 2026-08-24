/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.advice;

/**
 * Thrown when a supplement notice file could not be generated because the
 * project does not contain valid binary components (or other business-rule
 * failure). Mapped to HTTP 422 Unprocessable Entity by
 * {@link ApiV2ExceptionAdvice}.
 */
public class CSupplementNoticeGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CSupplementNoticeGenerationException(String msg, Throwable t) {
        super(msg, t);
    }

    public CSupplementNoticeGenerationException(String msg) {
        super(msg);
    }

    public CSupplementNoticeGenerationException() {
        super();
    }
}
