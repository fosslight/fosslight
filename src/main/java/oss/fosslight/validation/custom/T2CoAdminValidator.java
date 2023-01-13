/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.T2Code;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.CodeService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.validation.T2BasicValidator;

public class T2CoAdminValidator extends T2BasicValidator {
	private CodeService codeService = (CodeService) getWebappContext().getBean(CodeService.class);
	private T2UserService userService = (T2UserService) getWebappContext().getBean(T2UserService.class);
	
    @Override
    protected void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
    	
        // 1) First, execute Default Check
        super.customValidation(map, errMap);
        
        // 2) Seconds
        String targetKey = "CD_NO";
        String targetKey2 = "";
        
        // 코드 관리
        if (CoConstDef.GRID_OPERATION_ADD.equals(map.get("OPER")) && map.containsKey(targetKey) && !errMap.containsKey(targetKey)) {
        	// 중목체크
        	T2Code vo = new T2Code();
        	vo.setCdNo(map.get(targetKey));
        	
        	if (codeService.isExists(vo)) {
        		errMap.put(targetKey, targetKey + ".DUPLICATED");
        	}
        }
        
        // 코드 상세 등록 /수정
        targetKey = "CD_DTL_NO";
        
        if (map.containsKey("CD_DTL_NO.1")) {
        	// 중목체크
        	List<String> cdDtlList = new ArrayList<>();
        	for (int i = 1; map.containsKey(targetKey + "." + i); i++){
        		String _seqkey = targetKey + "." + i;
        		
        		if (!errMap.containsKey(_seqkey)) {
            		String val = map.get(_seqkey);
            		
            		if (cdDtlList.contains(val)) {
            			// 중목
            			errMap.put(_seqkey, targetKey + ".DUPLICATED");
            		} else {
            			cdDtlList.add(val);
            		}
        		}
            }
        }
        
        // 사용자 등록 AD 계정 유효성 체크
        targetKey = "USER_ID";
        targetKey2 = "USER_PW";
        
        if (map.containsKey(targetKey) && map.containsKey(targetKey2) && !errMap.containsKey(targetKey) && !errMap.containsKey(targetKey2)) {
        	// 기 등록 여부 체크
        	T2Users _param = new T2Users();
        	_param.setUserId(map.get(targetKey));
        	
        	if (!isEmpty(_param.getUserId()) && userService.checkDuplicateId(_param)) {
        		errMap.put(targetKey, targetKey+".DUPLICATED");
        	}
        	
        	String ldapFlag = CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_LDAP_USED_FLAG);
        	 
        	if (CoConstDef.FLAG_YES.equals(ldapFlag)) { // configuration에서 LDAP을 선택시만 check함.
	        	if (!isEmpty(map.get(targetKey2))) {
	        		if (!userService.checkAdAccounts(map, targetKey, targetKey2, null)) {
	        			if (map.containsKey("EMAIL") && !errMap.containsKey("EMAIL")) {
	        				if (!userService.checkAdAccounts(map, "EMAIL", targetKey2, null)) {
	        					errMap.put(targetKey2, targetKey2+".AUTH");
	        				}
	        			} else {
	        				errMap.put(targetKey2, targetKey2+".AUTH");
	        			}
	        		}
	        	}
        	}
        }
        
        targetKey = "EMAIL";
        
        if (map.containsKey(targetKey) && !errMap.containsKey(targetKey)) {
        	// email 등록 여부 체크
        	String email = (String) map.get(targetKey);
        	
        	if (!isEmpty(email)) {
        		List<T2Users> duplicateEmailList = userService.checkEmail(email);
        		if (duplicateEmailList.size() > 0) {
        			errMap.put(targetKey, targetKey+".DUPLICATED");
        		}
        	}
        }
        
        return;
    }
}
