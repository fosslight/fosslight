/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;
import java.util.List;

public interface ApiCommonService {
	public void mergeDivision(String from, String to) throws Exception;

	Map<String, Object> addDivision(String detailName, String detailDescription) throws Exception;

	/**
	 * Updates CD_DTL_NM and/or CD_DTL_EXP for a user division code (CD_NO=200).
	 * {@code null} arguments are left unchanged; at least one of {@code cdDtlNm} or {@code cdDtlExp} must be non-null.
	 *
	 * @return result map with success when updated; {@code null} when CD_DTL_NO does not exist
	 */
	Map<String, Object> updateDivision(String cdDtlNo, String cdDtlNm, String cdDtlExp) throws Exception;

	List<Map<String, Object>> getDivisionList() throws Exception;

	boolean existsActiveDivision(String cdDtlNo) throws Exception;
}
