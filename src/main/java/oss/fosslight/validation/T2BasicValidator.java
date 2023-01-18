/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation;

import java.util.Map;

public abstract class T2BasicValidator extends T2CoValidator{
	protected String PROC_MODE;
	
    public void setAppendix(String key, Object obj){
    	if (!isEmpty(key) && obj != null) {
    		if ("PROC_MODE".equals(key)) {
    			this.PROC_MODE = (String) obj;
    		}
    	}
    }
    
    protected String treatment(String paramvalue){
        if (paramvalue == null) {
          return null;
        }

        return paramvalue;
    }
    
    protected void customValidation(Map<String, String> map, Map<String, String> errMap){
        return;
    }

}