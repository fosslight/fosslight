/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value=HttpStatus.NOT_FOUND)
public class ColumnMissingException extends RuntimeException {

	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 1L;
	private String message;
 
	public ColumnMissingException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
}
