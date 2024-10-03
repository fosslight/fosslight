/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class Bat.
 */
@Getter @Setter
public class Bat  extends ComBean implements Serializable {

	/** The Constant serialVersionUID. */
	@Serial
	private static final long serialVersionUID = 9018255826730935058L;

	/** The id. */
	private String id;
	
	/** The bat id. */
	private String batId;
	
	/** The filename. */
	private String filename    ;
	
	/** The pathname. */
	private String pathname    ;
	private String sourcepath    ;
	
	/** The checksum. */
	private String checksum    ;
	
	/** The tlshchecksum. */
	private String tlshchecksum;
	
	/** The ossname. */
	private String ossname     ;
	
	/** The ossversion. */
	private String ossversion  ;
	
	/** The license. */
	private String license     ;
	
	/** The parentname. */
	private String parentname  ;
	
	/** The platformname. */
	private String platformname;
	
	/** The platformversion. */
	private String platformversion;
	
	/** The updatedate. */
	private String updatedate;

	/** The sch start date. */
	private String schStartDate;
	
	/** The sch end date. */
	private String schEndDate;
	
	private String downloadlocation;
	
	private String equalFlag = "N";
	
	private String comment;
	
	private String parameter;
	
	private String binaryPopupFlag = "N";
	
}
