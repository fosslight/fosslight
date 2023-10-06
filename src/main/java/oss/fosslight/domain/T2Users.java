/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.List;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;


/**
 * The Class T2Users.
 */
public class T2Users extends ComBean implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6658712585854634962L;
	
	/** The user id. */
	private String userId; // 사용자 ID
	
	/** The user name. */
	private String userName; // 사용자 이름(닉네임)
	
	/** The password. */
	private String password; // 암호
	
	/** The email. */
	private String email;
	
	/** The division. */
	private String division;

	/** The Number of rows displayed per page. */
	private Integer numbersOfRowPerPage;

	/** The enabled. */
	private String enabled; // 사용 구분
	
	/** The Default Tab. 2018-08-14 choye 추가 */
	private String defaultTab; // 로그인후 tab정보
	private String defaultTabAnchor; // 로그인후 tab정보

	/** The Default Locale. */
	private String defaultLocale;

	/** The authorities list. */
	private List<T2Authorities> authoritiesList; // 사용자 권한 목록
	
	/** The authority. */
	private String authority;			//해당 권한 Role
	
	/** The viewerAuthority. */
	private String viewerAuthority;			// view 권한 Role
	
	/** The adminAuthority. */
	private String adminAuthority;			// admin 권한 Role

	private String useYn;
	
	private String divisionName;
	
	private String tokenType;
	
	private String token;
	
	private String expireDate;
	
	private String expireFlag;
	
	private String modifiedFlag;

	public String getModifiedFlag() {
		return modifiedFlag;
	}

	public void setModifiedFlag(String modifiedFlag) {
		this.modifiedFlag = modifiedFlag;
	}

	public String getExpireFlag() {
		return expireFlag;
	}

	public void setExpireFlag(String expireFlag) {
		this.expireFlag = expireFlag;
	}

	public T2Users() {}
	
	public T2Users(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param userName the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the division.
	 *
	 * @return the division
	 */
	public String getDivision() {
		return division;
	}

	/**
	 * Sets the division.
	 *
	 * @param division the new division
	 */
	public void setDivision(String division) {
		this.division = division;
	}

	/**
	 * Gets the numbersOfRowPerPage.
	 *
	 * @return numbersOfRowPerPage the new numbersOfRowPerPage
	 */
	public Integer getNumbersOfRowPerPage() {
		return numbersOfRowPerPage;
	}

	/**
	 * Sets the division.
	 *
	 * @param numbersOfRowPerPage the new numbersOfRowPerPage
	 */
	public void setNumbersOfRowPerPage(Integer numbersOfRowPerPage) {
		this.numbersOfRowPerPage = numbersOfRowPerPage;
	}

	/**
	 * Gets the enabled.
	 *
	 * @return the enabled
	 */
	public String getEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled.
	 *
	 * @param enabled the new enabled
	 */
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	/**
	 * Gets the defaultLocale.
	 *
	 * @return the defaultLocale
	 */
	public String getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * Sets the defaultLocale.
	 *
	 * @param defaultLocale the new defaultLocale
	 */
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	/**
	 * Gets the defaultTab. 2018-08-14 choye 추가
	 *
	 * @return the defaultTab
	 */
	public String getDefaultTab() {
		return defaultTab;
	}

	/**
	 * Sets the defaultTab. 2018-08-14 choye 추가
	 *
	 * @param defaultTab the new defaultTab
	 */
	public void setDefaultTab(String defaultTab) {
		this.defaultTab = defaultTab;
	}

	/**
	 * Gets the authorities list.
	 *
	 * @return the authorities list
	 */
	public List<T2Authorities> getAuthoritiesList() {
		return authoritiesList;
	}

	/**
	 * Sets the authorities list.
	 *
	 * @param authoritiesList the new authorities list
	 */
	public void setAuthoritiesList(List<T2Authorities> authoritiesList) {
		this.authoritiesList = authoritiesList;
	}

	/**
	 * Gets the authority.
	 *
	 * @return the authority
	 */
	public String getAuthority() {
		return authority;
	}

	/**
	 * Sets the authority.
	 *
	 * @param authority the new authority
	 */
	public void setAuthority(String authority) {
		this.authority = authority;
	}

	/**
	 * Gets the viewerAuthority.
	 *
	 * @return the viewerAuthority
	 */
	public String getViewerAuthority() {
		return viewerAuthority;
	}

	/**
	 * Sets the viewerAuthority.
	 *
	 * @param viewerAuthority the new viewerAuthority
	 */
	public void setViewerAuthority(String viewerAuthority) {
		this.viewerAuthority = viewerAuthority;
	}

	/**
	 * Gets the adminAuthority.
	 *
	 * @return the adminAuthority
	 */
	public String getAdminAuthority() {
		return adminAuthority;
	}
	
	/**
	 * Sets the adminAuthority.
	 *
	 * @param adminAuthority the new adminAuthority
	 */
	public void setAdminAuthority(String adminAuthority) {
		this.adminAuthority = adminAuthority;
	}
	
	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public String getDefaultTabAnchor() {
		if (isEmpty(defaultTabAnchor) && !isEmpty(defaultTab)) {
			String _temp = "";
			for (String s : defaultTab.split(",")) {
				String _anchor = CoCodeManager.getCodeExpString(CoConstDef.CD_DEFAULT_TAB, s);
				if (!isEmpty(_anchor)) {
					if (!isEmpty(_temp)) {
						_temp += ",";
					}
					_temp += _anchor;
				}
			}
			
			if (!isEmpty(_temp)) {
				return _temp;
			}
		}
		
		for (String[] dtlCd : CoCodeManager.getValues(CoConstDef.CD_DEFAULT_TAB)) {
			defaultTab = dtlCd[0]; // detail No
			defaultTabAnchor = dtlCd[3]; // detail Description
			
			break;
		}
		
		return defaultTabAnchor;
	}

	public void setDefaultTabAnchor(String defaultTabAnchor) {
		this.defaultTabAnchor = defaultTabAnchor;
	}
	
	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}
}
