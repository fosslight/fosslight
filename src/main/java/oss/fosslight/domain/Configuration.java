/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;


/**
 * The Class Configuration.
 */
public class Configuration extends ComBean implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4882283606058177605L;

	/** The Default Tab. */
	private String defaultTab;

	/** The Default Locale. */
	private String defaultLocale;
	
	/**
	 * Gets the Default Tab.
	 *
	 * @return the Default Tab
	 */
	public String getDefaultTab() {
		return defaultTab;
	}

	/**
	 * Sets the Default Tab.
	 *
	 * @param Default Tab the new Default Tab
	 */
	public void setDefaultTab(String defaultTab) {
		this.defaultTab = defaultTab;
	}

	/**
	 * Gets the Default Locale.
	 *
	 * @return the Default Locale
	 */
	public String getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * Sets the Default Locale.
	 *
	 * @param Default Locale the new Default Locale
	 */
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}
	
}
