/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class BinaryData.
 */
@Getter @Setter
public class BinaryData  extends ComBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The id. */
	private String id;
	
	/** The bat id. */
	private String batId;
	
	/** The filename. */
	private String fileName;
	
	/** The pathname. */
	private String pathName;
	private String sourcePath;
	
	/** The checksum. */
	private String checkSum;
	
	/** The tlshchecksum. */
	private String tlshCheckSum;
	
	/** The ossname. */
	private String ossName;
	
	/** The ossversion. */
	private String ossVersion;
	
	/** The license. */
	private String license;
	
	/** The parentname. */
	private String parentName;
	
	/** The platformname. */
	private String platformName;
	
	/** The platformversion. */
	private String platformVersion;
	
	/** The updatedate. */
	private String updateDate;

	/** The sch start date. */
	private String schStartDate;
	
	/** The sch end date. */
	private String schEndDate;
	
	private String downloadlocation;
	
	private String equalFlag = "N";
	
	private String comment;
	
	private String parameter;
	
	private String binaryPopupFlag = "N";
	
	private String actionId;
	
	private String actionType;
}
