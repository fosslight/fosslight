/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.ProcessGuide;

@Mapper
public interface ProcessGuideMapper {	
	public List<ProcessGuide> selectProcessGuideList(ProcessGuide vo) throws Exception;
	
	public int selectProcessGuideTotalCount(ProcessGuide vo) throws Exception;
	
	public void updateProcessGuide(ProcessGuide vo) throws Exception;
	
	public ProcessGuide selectProcessGuide(ProcessGuide vo) throws Exception;
	
	public int selectProcessGuideCount(ProcessGuide vo) throws Exception;
}
