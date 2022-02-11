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
	
	/** The default search type. */
	private String defaultSearchType;
	
	
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
	 * @param defaultTab the new default tab
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
	 * @param defaultLocale the new default locale
	 */
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	/**
	 * Gets the default search type.
	 *
	 * @return the default search type
	 */
	public String getDefaultSearchType() {
		return defaultSearchType;
	}

	/**
	 * Sets the default search type.
	 *
	 * @param defaultSearchType the new default search type
	 */
	public void setDefaultSearchType(String defaultSearchType) {
		this.defaultSearchType = defaultSearchType;
	}
	
	
}
