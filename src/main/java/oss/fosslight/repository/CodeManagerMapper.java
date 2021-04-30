/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.CodeBean;
import oss.fosslight.domain.CodeDtlBean;

@Mapper
public interface CodeManagerMapper {

	public List<CodeBean> getCodeList(CodeBean vo) throws Exception;
	
	public int getCodeListTotCnt(CodeBean vo) throws Exception;
	
	public CodeBean getCodeInfo(CodeBean vo) throws Exception;
	
	public List<CodeDtlBean> getCodeDtlList(CodeBean vo) throws Exception;
	
	public void insertCode(CodeBean vo) throws Exception;
	
	public int updateCode(CodeBean vo) throws Exception;
	
	public void insertCodeDtl(CodeDtlBean vo) throws Exception;
	
	public int deleteCode(CodeBean vo) throws Exception;
	
	public List<CodeDtlBean> getCodeListAll();
}