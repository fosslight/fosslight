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
import org.springframework.stereotype.Service;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.domain.History;
import oss.fosslight.repository.HistoryMapper;
import oss.fosslight.service.HistoryService;

@Service
public class HistoryServiceImpl extends CoTopComponent implements HistoryService {
	@Autowired HistoryMapper hisotryMapper;
	
	@Override
	public void storeData(History history) {
		history.sethData(toJson(history.gethData()));
		hisotryMapper.insertHistoryData(history);
	}
	
	@Override
	public History getData(History history){
		return hisotryMapper.selectOneHistoryData(history);
	}
	
	@Override
	public Map<String, Object> getList(History history) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		history.setTotListSize(hisotryMapper.selectHistoryDataTotalCount(history));
		List<History> hList = hisotryMapper.selectHistoryData(history);
		
		map.put("pageInfo", history);	// page: curPage, total: blockEnd, records: totListSize
		map.put("rows", hList);
		 
		return map;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getAsToBeHistoryDataByGrid(History history) {
		History beData = hisotryMapper.selectOneHistoryData(history);
		History asData = hisotryMapper.selectOneHistoryBeforeData(beData);
		
		Map<String, Object> asMap =  asData != null ? ((Map<String, Object>)fromJson((String) asData.gethData(), Map.class)) : null;
		Map<String, Object> beMap =  beData != null ? ((Map<String, Object>)fromJson((String) beData.gethData(), Map.class)) : null;
		
		List<Map<String, Object>> mTbl = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sTblList = new ArrayList<Map<String, Object>>();
		
		// Data : dtlCd
		// Param : String CD_NO
        // Result : Vector [ String CD_DTL_NO, String CD_DTL_NM(Key), String CD_SUB_NO(Ref Entity) ] 
		int cnt = 1;
		String cdNm = beData.gethType(); 
		
		for(String[] dtlCd : CoCodeManager.getValues(cdNm)){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			
			// EXP : TYPE(String, Code, Array, Object) | NAME | CD_NO
			String[] inf = CoCodeManager.getCodeExpString(cdNm, dtlCd[0]).split("\\|");
			
			dataMap.put("no", cnt++);
			dataMap.put("name", inf[1]);
			Object asD_ = getDataForType(cdNm, dtlCd, asMap);
			Object beD_ = getDataForType(cdNm, dtlCd, beMap);
			
			// main data
			if(asD_.getClass().equals(String.class) && beD_.getClass().equals(String.class)){
				dataMap.put("as", asD_);
				dataMap.put("be", beD_);
			} else { // sub list data
				dataMap.put("as", "-");
				dataMap.put("be", "-");
				Map<String, Object> sTblMap = new HashMap<String, Object>();
				sTblMap.put("type", ((Map<String, Object>)asD_).get("type"));		
				sTblMap.put("colNames", ((Map<String, Object>)asD_).get("colNames"));
				sTblMap.put("subAsList", ((Map<String, Object>)asD_).get("subList"));
				sTblMap.put("subBeList", ((Map<String, Object>)beD_).get("subList"));
				
				sTblList.add(sTblMap);
			}
			
			mTbl.add(dataMap);
		}
		
		Map<String, Object> asToBeMap = new HashMap<String, Object>();
		asToBeMap.put("main", mTbl);
		asToBeMap.put("sub", sTblList);
		asToBeMap.put("asModifier", asData != null ? asData.getModifier() : "");
		asToBeMap.put("beModifier", beData != null ? beData.getModifier() : "");
		asToBeMap.put("asModifiedDate", asData != null ? asData.getModifiedDate() : "");
		asToBeMap.put("beModifiedDate", beData != null ? beData.getModifiedDate() : "");
		
		return asToBeMap;
	}
	
	// History Sub Method
	@SuppressWarnings("unchecked")
	public Object getDataForType(String cdNm, String[] dtlCd, Map<String, Object> dMap){
		Object ret = null;
		// EXP : TYPE(String, Code, Array, Object) | NAME | CD_NO
		String[] inf = CoCodeManager.getCodeExpString(cdNm, dtlCd[0]).split("\\|");
		
		if(inf[0].equals("String")) {
			ret = dMap != null ? escapeSql(nvl((String)dMap.get(dtlCd[1]), "")) : "";
		} else if(inf[0].equals("Code")) {
			ret = dMap != null ? nvl(CoCodeManager.getCodeString(inf[2], (String)dMap.get(dtlCd[1])), inf[2]) : "";
		} else if(inf[0].equals("Array") && dtlCd[2] == null) {
			List<String> asArr = dMap != null ? (ArrayList<String>)dMap.get(dtlCd[1]) : null;
			ret = asArr != null && asArr.size() > 0 ? String.join(", ", asArr) : "";
		} else if(inf[0].equals("Array") && dtlCd[2] != null) {
			String sCdNm = dtlCd[2]; // CD_SUB_NO(Ref Entity) 
			HashMap<String, Object> sTblMap = new HashMap<String, Object>();
			
			// colNames 생성
			List<Map<String, Object>> colNames = new ArrayList<Map<String, Object>>(); 
			// Param : String CD_NO
	        // Result : Vector&String [ String CD_DTL_NO, String CD_SUB_NO, String CD_DTL_NM, String CD_DTL_NM, String CD_DTL_EXP, String CD_ORDER ]
			HashMap<String, Object> colInfo =  new HashMap<String, Object>();
			colInfo.put("key", "no");
			colInfo.put("name", "no");
			colInfo.put("order", "0");
			colNames.add(colInfo);
			
			for(String[] v : CoCodeManager.getAllValues(sCdNm)){
				 colInfo =  new HashMap<String, Object>();
				 colInfo.put("key", v[2]);
				 colInfo.put("name", v[4].split("\\|")[1]);
				 colInfo.put("order", v[5]);
				 
				 colNames.add(colInfo);
			}
			
			sTblMap.put("type", inf[1]);		// model name
			sTblMap.put("colNames", colNames);	// list columns
			
			// colModel 생성
			List<Map<String, Object>> subList = new ArrayList<Map<String, Object>>();
			
			if(dMap != null && dMap.get(dtlCd[1]) != null){
				// sub list 생성
				int sCnt = 1;
				
				for(Map<String, Object> sMap : (List<Map<String, Object>>) dMap.get(dtlCd[1])){
					Map<String, Object> sDataMap = new HashMap<String, Object>();
					sDataMap.put("no", sCnt++);
					
					// row data 생성
					for(String[] sDtlCd : CoCodeManager.getValues(sCdNm)){
						String[] sInf = CoCodeManager.getCodeExpString(sCdNm, sDtlCd[0]).split("\\|");	// EXP : TYPE(String, Code, Array, Object) | NAME | CD_NO
						
						if(sInf[0].equals("String")){
							sDataMap.put(sDtlCd[1], escapeSql(nvl((String)sMap.get(sDtlCd[1]))) );
						}else if(sInf[0].equals("Code")){
							sDataMap.put(sDtlCd[1], CoCodeManager.getCodeString(sInf[1], (String)sMap.get(sDtlCd[1])) );
						}	
					}
					
					subList.add(sDataMap);
				}
			}
			
			sTblMap.put("subList", subList);
			
			ret = sTblMap;
		}
		
		return ret;
	}
		
	public String escapeSql(String str){
		return str == null ? "null" : String.format("%s", str.replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\""));
	}
}
