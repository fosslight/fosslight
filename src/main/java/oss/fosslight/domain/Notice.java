/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Notice extends ComBean implements Serializable {
	@Serial
	private static final long serialVersionUID = 3499915410560019672L;
	
	private String seq;
	private String title;
	private String notice;
	private String replaceNotice;
	private String sDate;
	private String eDate;
	private String publishYn;	
}
