/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;

/**
 * The Class OssMaster.
 *
 * @author Administrator
 */
/**
 * @author Administrator
 *
 */
public class ProjectDistributeHis extends ComBean implements Serializable{

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 702862084569729284L;
	
	/** The prj id. */
	private String prjId;
	
	/** The act type. */
	private String actType;
	
	/** The act cont. */
	private String actCont;
	
	/**
	 * Gets the prj id.
	 *
	 * @return the prj id
	 */
	public String getPrjId() {
		return prjId;
	}
	
	/**
	 * Sets the prj id.
	 *
	 * @param prjId the new prj id
	 */
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	
	/**
	 * Gets the act type.
	 *
	 * @return the act type
	 */
	public String getActType() {
		return actType;
	}
	
	/**
	 * Sets the act type.
	 *
	 * @param actType the new act type
	 */
	public void setActType(String actType) {
		this.actType = actType;
	}
	
	/**
	 * Gets the act cont.
	 *
	 * @return the act cont
	 */
	public String getActCont() {
		return actCont;
	}
	
	/**
	 * Sets the act cont.
	 *
	 * @param actCont the new act cont
	 */
	public void setActCont(String actCont) {
		this.actCont = actCont;
	}
	
	
}
