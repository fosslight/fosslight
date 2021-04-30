/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.T2Code;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.service.CodeService;

@Service
public class CodeServiceImpl extends CoTopComponent implements CodeService {
	@Autowired CodeMapper codeMapper;

	/**
	 * 코드 목록 조회
	 */
	@Override
	public Map<String, Object> getCodeList(T2Code vo) throws Exception {
		Map<String, Object> map = null;
		int records = codeMapper.selectCodeTotalCount(vo);
		
		if(records > 0) {
			vo.setTotListSize(records);
			map = getGridPagerMap(vo);
			map.put("rows", codeMapper.selectCodeList(vo));
		}
		
		return map == null ? new HashMap<String, Object>() : map;
	}

	/**
	 * 코드상세 목록 조회
	 */
	@Override
	public ArrayList<T2CodeDtl> getCodeDetailList(T2CodeDtl vo) throws Exception {
		ArrayList<T2CodeDtl> codeDetailList = codeMapper.selectCodeDetailList(vo);
		
		if(codeDetailList.size() > 0){
			for(int i=0; i<codeDetailList.size(); i++){
				codeDetailList.get(i).setCdDtlNoOrign(codeDetailList.get(i).getCdDtlNo());
			}
		}
			
		return codeDetailList;
	}

	/**
	 * 코드 저장
	 */
	@CacheEvict(value="autocompleteCache", allEntries=true)
	@Transactional
	@Override
	public void setCode(T2Code vo) throws Exception {
		// 추가
		if(CoConstDef.GRID_OPERATION_ADD.equals(vo.getOper())) {
			codeMapper.insertCode(vo);
		} else if(CoConstDef.GRID_OPERATION_EDIT.equals(vo.getOper())) { // 수정
			codeMapper.updateCode(vo);
		} else { // 삭제
			codeMapper.deleteCodeDetailAll(vo);
			codeMapper.deleteCode(vo);
		}
	}

	@Override
	public boolean isExists(T2Code vo) {
		return codeMapper.selectCodeTotalCount(vo) > 0;
	}

	@Override
	@CacheEvict(value="autocompleteCache", allEntries=true)
	@Transactional
	public void setCodeDetails(List<T2CodeDtl> dtlList, String cdNo) throws Exception {
		// 1. 코드 상세 전체 삭제
		T2Code codeVo = new T2Code();
		codeVo.setCdNo(cdNo);
		
		codeMapper.deleteCodeDetailAll(codeVo);
		
		// 2. 전체 상세 코드 재등록(추가 / 변경 / 삭제)
		for (T2CodeDtl vo : dtlList) {
			codeMapper.insertCodeDetail(vo);
		}
	}

	@Override
	@Cacheable(value="autocompleteCache", key="#root.methodName")
	public List<T2Code> getcodeList(T2Code t2Code) {
		List<T2Code> result = codeMapper.getCodeList(t2Code);
		
		return result;
	}

	@Override
	@Cacheable(value="autocompleteCache", key="#root.methodName")
	public List<T2Code> getcodeNmList(T2Code t2Code) {
		List<T2Code> result = codeMapper.getCodeNmList(t2Code);
		
		return result;
	}

}