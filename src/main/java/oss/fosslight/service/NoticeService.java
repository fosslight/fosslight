/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.Map;

import oss.fosslight.domain.Notice;

public interface NoticeService {
	Map<String, Object> getNoticeList(Notice vo) throws Exception;
	
	public void setNotice(Notice vo) throws Exception;
	
	Map<String, Object> getPublishedNotice(Notice vo) throws Exception;
}
