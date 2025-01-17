/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation;

import java.util.Map;

public abstract class T2BasicValidator extends T2CoValidator{
	protected String PROC_MODE;
	
    @Override
	public void setAppendix(String key, Object obj){
    	boolean condition = !isEmpty(key) && obj != null && "PROC_MODE".equals(key);
		if (condition) {
			this.PROC_MODE = (String) obj;
		}
    }
    
    @Override
	protected String treatment(String paramvalue){
        return paramvalue == null ? null : paramvalue;
    }
    
    protected void customValidation(Map<String, String> map, Map<String, String> errMap){
        return;
    }

}