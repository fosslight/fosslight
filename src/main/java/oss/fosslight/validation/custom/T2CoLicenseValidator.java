/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation.custom;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.service.LicenseService;
import oss.fosslight.validation.T2CoValidator;

public class T2CoLicenseValidator extends T2CoValidator {
	
	private LicenseService 	licenseService 	= (LicenseService) getWebappContext().getBean(LicenseService.class);
	private String licenseId = null;

	private String PROC_TYPE = null;
	public final String PROC_TYPE_DELETE				 		= "DEL";
	
	@Override
	protected void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		if (PROC_TYPE_DELETE.equals(PROC_TYPE)) {
			List<OssMaster> result = licenseService.checkExistsUsedOss(licenseId);
			
			if (result != null && !result.isEmpty()) {
				String errMsg = MessageFormat.format(getCustomMessage("LICENSE_NAME.OSSUSED"), result.size());
				int ossCnt = 1;
				
				for (OssMaster bean : result) {
					if (ossCnt > 40) {
						errMsg += "<br>...";
						
						break;
					}
					
					errMsg += "<br>" + MessageFormat.format(getCustomMessage("LICENSE_NAME.OSSUSEDEXP"), bean.getOssId(), bean.getOssId(), bean.getOssName() + (!isEmpty(bean.getOssVersion()) ? " " : "") + avoidNull(bean.getOssVersion()));
					ossCnt ++;
				}
				
				errMap.put("LICENSE_NAME", errMsg);
			}
		} else {
			String targetName = "";
			String licenseId = avoidNull(map.get("LICENSE_ID"));
			
			// 1. license name 
			targetName = "LICENSE_NAME";
			
			if (!errMap.containsKey(targetName) && map.containsKey(targetName) && !isEmpty(map.get(targetName))) {
				if (map.get(targetName).contains(CoConstDef.CD_COMMA_CHAR)) {
					errMap.put(targetName, targetName + ".NOTALLOWED_CHAR");
				}else {
					// DB체크만
					LicenseMaster param = new LicenseMaster(licenseId);
					param.setLicenseName(map.get(targetName));
					LicenseMaster result = licenseService.checkExistsLicense(param);
					
					if (result != null) {
						errMap.put(targetName, MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), map.get(targetName), result.getLicenseId(), result.getLicenseId(), result.getLicenseName()));
					}
				}
			}
			
			// 2. SHORT_IDENTIFIER
			targetName = "SHORT_IDENTIFIER";
			
			if (!errMap.containsKey(targetName) && map.containsKey(targetName) && !isEmpty(map.get(targetName))) {
				// 2-1 license name 과 같은 경우
				if (map.get(targetName).contains(CoConstDef.CD_COMMA_CHAR)) {
					errMap.put(targetName, targetName + ".NOTALLOWED_CHAR");
				} else if (map.get(targetName).equalsIgnoreCase(avoidNull(map.get("LICENSE_NAME")))) {
					errMap.put(targetName, targetName + ".SAME");
				} else {
					// 2-2 db 에 존재하는 경우
					LicenseMaster param = new LicenseMaster(licenseId);
					param.setLicenseName(map.get(targetName));
					LicenseMaster result = licenseService.checkExistsLicense(param);
					
					if (result != null) {
						errMap.put(targetName, MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), map.get(targetName), result.getLicenseId(), result.getLicenseId(), result.getLicenseName()));
					}
				}
			}
			
			// 3. NICK NAME
			targetName = "LICENSE_NICKNAMES";
			
			if (!errMap.containsKey(targetName) && (map.containsKey(targetName + ".1") || map.containsKey(targetName) )) {
	        	List<String> nickNameKeyList = new ArrayList<>();
	        	
	        	if (!map.containsKey(targetName + ".1")) {
	        		checkLicenseNickName(errMap, map, nickNameKeyList, targetName, licenseId);
	        	} else {
	            	for (int i = 1; map.containsKey(targetName + "." + i); i++){
	            		String _seqkey = targetName + "." + i;
	            		checkLicenseNickName(errMap, map, nickNameKeyList, _seqkey, licenseId);
	            	}
	        	}
			}
		}
	}

	private void checkLicenseNickName(Map<String, String> errMap, Map<String, String> map, List<String> nickNameKeyList, String _seqkey, String licenseId) {
		String targetName = "LICENSE_NICKNAMES";
		
		if (isEmpty(map.get(_seqkey))) {
			return;
		}
		if (!errMap.containsKey(_seqkey)) {
			// 3-1 동일한 닉네임을 입력한 경우
			if (nickNameKeyList.contains(map.get(_seqkey).toUpperCase())) {
				errMap.put(_seqkey, targetName + ".SAME");
			} else {
				String nickName = map.get(_seqkey).toUpperCase(); 
				nickNameKeyList.add(nickName);
				
				if (nickName.contains(CoConstDef.CD_COMMA_CHAR)) {
					errMap.put(_seqkey, targetName + ".NOTALLOWED_CHAR");
				} else if (map.get(_seqkey).equalsIgnoreCase(avoidNull(map.get("LICENSE_NAME")))
						|| map.get(_seqkey).equalsIgnoreCase(avoidNull(map.get("SHORT_IDENTIFIER")))) { // 3-2 license name 또는 SHORT_IDENTIFIER 과 같은 경우
					errMap.put(_seqkey, targetName + ".SAME");
				} else {
					// 3-4 db 에 존재하는 경우
					LicenseMaster param = new LicenseMaster(licenseId);
					param.setLicenseName(map.get(_seqkey));
					LicenseMaster result = licenseService.checkExistsLicense(param);
					
					if (result != null) {
						errMap.put(_seqkey, MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), map.get(_seqkey), result.getLicenseId(), result.getLicenseId(), result.getLicenseName()));
					}
				}
			}
		}		
	}
	
	public void setProcType(String type) {
		PROC_TYPE = type;
	}

	@Override
	public void setAppendix(String key, Object obj) {
		if ("licenseId".equals(key)) {
			this.licenseId = (String) obj;
		}
	}

	@Override
	protected String treatment(String paramvalue) {
		return paramvalue;
	}
	

}
