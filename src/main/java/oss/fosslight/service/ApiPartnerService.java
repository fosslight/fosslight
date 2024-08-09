/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface ApiPartnerService {
	Map<String, Object> getPartnerMasterList(Map<String, Object> paramMap);
	boolean existPartnertCnt(Map<String, Object> paramMap);
	boolean existLdapUserToEmail(String email);
	boolean existsWatcherByEmail(String partnerId, String email);
	void insertWatcher(Map<String, Object> paramMap);

	public Map<String, Object> getExportJson (String partnerId);
}
