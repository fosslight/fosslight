/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.T2Code;
import oss.fosslight.domain.T2CodeDtl;

@Mapper
public interface CodeMapper {
	public List<T2Code> selectCodeList(T2Code vo) throws Exception;
	
	public ArrayList<T2CodeDtl> selectCodeDetailList(T2CodeDtl vo) throws Exception;
	
	public void insertCode(T2Code vo) throws Exception;
	
	public void updateCode(T2Code vo) throws Exception;
	
	public void deleteCode(T2Code vo) throws Exception;
	
	public void deleteCodeDetailAll(T2Code vo) throws Exception;
	
	public void insertCodeDetail(T2CodeDtl vo) throws Exception;
	
	public void updateCodeDetail(T2CodeDtl vo) throws Exception;
	
	public void deleteCodeDetail(T2CodeDtl vo) throws Exception;
	
	public int selectCodeTotalCount(T2Code vo);
	
	public List<T2Code> getCodeList(T2Code t2Code);
	
	public List<T2Code> getCodeNmList(T2Code t2Code);
	
	public String selectExtType(String string);
	
	public List<String> getCategoryList(String categoryCd);
	
	public void saveConfiguration(T2Code code);

	/**
	 * Gets the Detail Code Name.
	 *
	 * @param cdNo the cd no
	 * @param cdDtlNo the cd dtl no
	 * @return the code dtl nm
	 */
	public String getCodeDtlNm(@Param("cdNo") String cdNo, @Param("cdDtlNo") String cdDtlNo);
	
	/**
	 * Update Detail Code Name.
	 *
	 * @param cdNo the cd no
	 * @param cdDtlNo the cd dtl no
	 * @param cdDtlNm the cd dtl nm
	 * @return update result int
	 */
	public int updateCodeDtlNm(@Param("cdNo") String cdNo, @Param("cdDtlNo") String cdDtlNo, @Param("cdDtlNm") String cdDtlNm);
}
