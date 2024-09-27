/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oss.fosslight.domain.T2Code;
import oss.fosslight.domain.T2CodeDtl;

public interface CodeService {
	Map<String, Object> getCodeList(T2Code vo) throws Exception;
	ArrayList<T2CodeDtl> getCodeDetailList(T2CodeDtl vo) throws Exception;
	ArrayList<T2CodeDtl> getCodeDetailList(T2CodeDtl vo, boolean notDisplayFlag) throws Exception;
	void setCode(T2Code vo) throws Exception;
	void setCodeDetails(List<T2CodeDtl> dtlList, String cdNo) throws Exception;
	public boolean isExists(T2Code vo);
	List<T2Code> getcodeList(T2Code t2Code);
	List<T2Code> getcodeNmList(T2Code t2Code);
}
