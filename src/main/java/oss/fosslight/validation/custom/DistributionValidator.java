/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oss.fosslight.domain.Project;
import oss.fosslight.validation.T2CoValidator;


public class DistributionValidator extends T2CoValidator {
	
	private Project dataBean = null;
	private String validType = "";
	
	@Override
	protected void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		if(dataBean != null) {
			// basic validation
			String basicKey = "";
			boolean isMod = !isEmpty(dataBean.getDistributeDeployTime());
			
			// distributeName
			{
				basicKey = "DISTRIBUTE_NAME";
				String errCd = checkBasicError(basicKey, dataBean.getDistributeName(), isMod);
				
				if(!isEmpty(errCd)) {
					errMap.put(basicKey, errCd);
				}
			}
			
			// master category
			{
				basicKey = "DISTRIBUTE_MASTER_CATEGORY";
				String errCd = checkBasicError(basicKey, dataBean.getDistributeMasterCategory(), isMod);
				
				if(!isEmpty(errCd)) {
					errMap.put(basicKey, errCd);
				}
			}
			
			// model info
			if(dataBean.getModelList() != null && !dataBean.getModelList().isEmpty()) {
				// 중복체크
				List<String> modelDuplCheckList = new ArrayList<>();
				List<String> dupKeyList = new ArrayList<>();
				
				for(Project bean : dataBean.getModelList()) {
					String key = avoidNull(bean.getCategory()) + "|" + avoidNull(bean.getModelName()).trim().toUpperCase();
					
					if(dupKeyList.contains(key)) {
						modelDuplCheckList.add(key);
					} else {
						dupKeyList.add(key);
					}
				}
				
				for(Project bean : dataBean.getModelList()) {
					// model name
					basicKey = "MODEL_NAME";
					
					String errCd = checkBasicError(basicKey, bean.getModelName());
					if(!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					} else if(modelDuplCheckList.contains(avoidNull(bean.getCategory()) + "|" + avoidNull(bean.getModelName()).trim().toUpperCase())){
						errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".DUPLICATED");
					} else if(avoidNull(bean.getModelName()).trim().length() < 2) {
						errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".MINLENGTH");
					}
					
					basicKey = "RELEASE_DATE";
					errCd = checkBasicError(basicKey, bean.getReleaseDate());
					
					if(!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					}
				}
			} else if("Distribution".equalsIgnoreCase(validType)) {
				errMap.put("VALID_MSG_MODEL_LIST", "MODEL_NAME.REQUIRED_INFO");
			}
			
		}
	}

	@Override
	protected String treatment(String paramvalue) {
		return paramvalue;
	}

	@Override
	public void setAppendix(String key, Object obj) {
		if("project".equals(key)) {
			this.dataBean = (Project) obj;
		}
 	}
	
	public void setValidType(String vType) {
		this.validType = vType;
	}

}
