/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.Notice;

@Mapper
public interface NoticeMapper {
	public List<Notice> selectNoticeList(Notice vo) throws Exception;
	
	public int selectNoticeTotalCount(Notice vo) throws Exception;
	
	public void insertNotice(Notice vo) throws Exception;
	
	public void updateNotice(Notice vo) throws Exception;
	
	public void deleteNotice(Notice vo) throws Exception;
	
	public int selectPublishedNoticeCount(Notice vo) throws Exception;
	
	public List<Notice> selectPublishedNotice(Notice vo) throws Exception;
}
