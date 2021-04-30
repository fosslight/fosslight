/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.CoMail;

@Mapper
public interface SentMailMapper {
	int selectSentMailTotalCount(CoMail vo); 
	
	List<CoMail> selectSentMailList(CoMail vo);
}
