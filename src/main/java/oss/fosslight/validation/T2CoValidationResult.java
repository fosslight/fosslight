/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.util.StringUtil;

public class T2CoValidationResult {
    /** The err map. */
    private Map<String,String> errMap = null;
    
    /** The rule map. */
    private Map<String,Map<String,String>> ruleMap = null;
    
    /** The message map. */
    private Map<String,String> messageMap = null;
    
    /** The data map. */
    private Map<String,String> dataMap = null;
    
    /** The valid data map. */
    private Map<String,String> validDataMap = null;
    
    /** The default format. */
    private final String DEFAULT_FORMAT = "<div class=\"errorMsg\">{0}</div>";
    
    /** The format. */
    private String format = DEFAULT_FORMAT;
    
    /** The diff map. */
    private Map<String,String> diffMap = null;
    private Map<String,String> infoMap = null;
    
    public T2CoValidationResult(Map<String, Map<String,String>> ruleMap, Map<String, String> messageMap){
        this.ruleMap = ruleMap;
        this.messageMap = messageMap;
        this.validDataMap =  new T2CoValidMap<>();
    }
    
    public void setErrorCodeMap(Map<String, String> map){
        errMap = map;
    }
    
    public Map<String, String> getErrorCodeMap(){
        if (errMap == null){
            return new HashMap<String, String>();
        }else{
            return errMap;
        }
    }
    
    public Map<String, String> getWarningCodeMap(){
        if (diffMap == null){
            return new HashMap<String, String>();
        }else{
            return diffMap;
        }
    }
    
    public Map<String, String> getInfoCodeMap(){
        if (infoMap == null){
            return new HashMap<String, String>();
        }else{
            return infoMap;
        }
    }
    
    public void setDataMap(Map<String,String> map){
        dataMap = map;
    }
    
    public Map<String, String> getDataMap(){
        return dataMap == null ? new HashMap<String,String>() : dataMap;
    }
    
    void validate(String key){
        if (dataMap == null) {
          throw new IllegalStateException("no dataMap");
        }
        
        if (dataMap.containsKey(key)) {
          validDataMap.put(key, dataMap.get(key));
        } else {
          throw new IllegalArgumentException("invalid key. Entry not found in dataMap.");
        }
    }
    
    public Map<String, String> getValidDataMap(){
        if (isValid()){
            return validDataMap;
        }
        
        return new T2CoValidMap<>();
    }
    
    public boolean isValid(){
        if (errMap == null) {
          throw new IllegalStateException("Not validated yet.");
        }
        
        return errMap.isEmpty();
    }
    
    public void setFormat(String str){
        format = str;
    }
    
    public String getValidMessage(String key){
        boolean printErrCd = false;
        
        if (errMap == null || !errMap.containsKey(key)){
            return "";
        }else if (messageMap == null || !messageMap.containsKey(errMap.get(key))){
            return errMap.get(key);
        }else{
            String errCd = errMap.get(key);
            String msg;
            
            if (errCd.endsWith(".LENGTH")){
                Map<String,String> rule = ruleMap.get(key);

                // key에 해당ㅇ하는 rule이 없을 경우
                if (rule == null) {
                    // root key를 기준으로(例 : CARD.1 → CARD)、시쿼스 키를 찾는다
                    String rootKey = key.replaceFirst("\\.\\d+", "");
                    rule = ruleMap.get(rootKey);
                    
                    if (rule == null && key.indexOf(".") > -1) {
                    	rootKey = key.substring(0, key.indexOf("."));
                    	rule = ruleMap.get(rootKey);
                    }
                }
                
                msg = MessageFormat.format(messageMap.get(errCd), T2CoValidator.getLimitLength(rule));
            }else{
                msg = messageMap.get(errCd);
            }
            
            return printErrCd ? (errMap.get(key) + " " + msg) : msg;
        }
    }
    
    public String format(String str){
        if (str == null || "".equals(str)) {
        	return "";
        }
        
        return MessageFormat.format(format, str);
    }
    
    public String getFormattedMessage(String key){
        return format(getValidMessage(key));
    }
    
    public Map<String, String> getFormattedMessageMap(){
        if (errMap == null) {
        	return new HashMap<>();
        }

        Map<String, String> formatted = new HashMap<>();
        Iterator<String> itr = errMap.keySet().iterator();
        
        while (itr.hasNext()){
            String key = (String)itr.next();
            String camelKey = key;
            
            if (CoConstDef.VALIDATION_USE_CAMELCASE) {
            	camelKey = StringUtil.convertToCamelCase(camelKey);
            }
            
            formatted.put(camelKey, getFormattedMessage(key));
            
            if (!StringUtil.isEmpty(getFormattedMessage(key))) {
            	formatted.put(camelKey+"Style", "style=\"background:#FEF8F8;border:1px solid #FF0000;\"");
            }
        }
        
        formatted.put("isValid", isValid() ? "true" : "false");
        
        return formatted;
    }
    
    public Map<String, String> getValidMessageMap() {
    	Map<String, String> messageMap = new HashMap<>();
        Iterator<String> itr = errMap.keySet().iterator();
        
        while (itr.hasNext()){
        	String key = (String)itr.next();
            String camelKey = key;
            
            if (CoConstDef.VALIDATION_USE_CAMELCASE) {
            	if (camelKey.indexOf(".") > -1) {
            		String _name = StringUtil.convertToCamelCase(camelKey.substring(0, camelKey.indexOf(".")));
            		String _rowId = camelKey.substring(camelKey.indexOf("."));
            		camelKey = _name + _rowId;
            	} else {
                	camelKey = StringUtil.convertToCamelCase(camelKey);
            	}
            }
            
        	messageMap.put(camelKey, getValidMessage(key));
        }

        messageMap.put("isValid", isValid() ? "true" : "false");
        
    	return messageMap;
    }
    
    public Map<String, String> getDiffMessageMap() {
    	Map<String, String> messageMap = new HashMap<>();
        Iterator<String> itr = diffMap.keySet().iterator();
        
        while (itr.hasNext()){
        	String key = (String)itr.next();
            String camelKey = key;
            
            if (CoConstDef.VALIDATION_USE_CAMELCASE) {
            	if (camelKey.indexOf(".") > -1) {
            		String _name = StringUtil.convertToCamelCase(camelKey.substring(0, camelKey.indexOf(".")));
            		String _rowId = camelKey.substring(camelKey.indexOf("."));
            		camelKey = _name + _rowId;
            	} else {
                	camelKey = StringUtil.convertToCamelCase(camelKey);
            	}
            }
            
        	messageMap.put(camelKey, getDiffMessage(key));
        }

        messageMap.put("isDiff", isDiff() ? "true" : "false");        
    	return messageMap;
    }
    
    public boolean isDiff(){
    	if (diffMap == null) {
    		throw new IllegalStateException("Not validated yet.");
    	}
    	
        return diffMap.isEmpty();
    }
    
    public String getDiffMessage(String key){
        boolean printErrCd = false;
        
        if (diffMap == null || !diffMap.containsKey(key)){
            return "";
        }else if (messageMap == null || !messageMap.containsKey(diffMap.get(key))){
            return diffMap.get(key);
        }else{
            String errCd = diffMap.get(key);
            String msg;
            
            if (errCd.endsWith(".LENGTH")){
                Map<String,String> rule = ruleMap.get(key);
                
                // key에 해당ㅇ하는 rule이 없을 경우
                if (rule == null) {
                    // root key를 기준으로(例 : CARD.1 → CARD)、시쿼스 키를 찾는다
                    String rootKey = key.replaceFirst("\\.\\d+", "");
                    rule = ruleMap.get(rootKey);
                    
                    if (rule == null && key.indexOf(".") > -1) {
                    	rootKey = key.substring(0, key.indexOf("."));
                    	rule = ruleMap.get(rootKey);
                    }
                }

                msg = MessageFormat.format(messageMap.get(errCd), T2CoValidator.getLimitLength(rule));
            }else{
                msg = messageMap.get(errCd);
            }
            
            return printErrCd ? (diffMap.get(key) + " " + msg) : msg;
        }
    }
    
    public Map<String, String> getInfoMessageMap() {
    	Map<String, String> messageMap = new HashMap<>();
        Iterator<String> itr = infoMap.keySet().iterator();
        
        while (itr.hasNext()){
        	String key = (String)itr.next();
            String camelKey = key;
            
            if (CoConstDef.VALIDATION_USE_CAMELCASE) {
            	camelKey = StringUtil.convertToCamelCase(camelKey);
            }
            
        	messageMap.put(camelKey, getInfoMessage(key));
        }

        messageMap.put("hasInfo", hasInfo() ? "true" : "false");
        
    	return messageMap;
    }
    
    public boolean hasInfo(){
    	if (infoMap == null) {
    		throw new IllegalStateException("Not validated yet.");
    	}
    	
        return !infoMap.isEmpty();
    }
    
    public String getInfoMessage(String key){
        boolean printErrCd = false;
        
        if (infoMap == null || !infoMap.containsKey(key)){
            return "";
        }else if (messageMap == null || !messageMap.containsKey(infoMap.get(key))){
            return infoMap.get(key);
        }else{
            String errCd = infoMap.get(key);
            String msg;
            
            if (errCd.endsWith(".LENGTH")){
                Map<String,String> rule = ruleMap.get(key);
                
                // key에 해당ㅇ하는 rule이 없을 경우
                if (rule == null) {
                    // root key를 기준으로(例 : CARD.1 → CARD)、시쿼스 키를 찾는다
                    String rootKey = key.replaceFirst("\\.\\d+", "");
                    rule = ruleMap.get(rootKey);
                    
                    if (rule == null && key.indexOf(".") > -1) {
                    	rootKey = key.substring(0, key.indexOf("."));
                    	rule = ruleMap.get(rootKey);
                    }
                }

                msg = MessageFormat.format(messageMap.get(errCd), T2CoValidator.getLimitLength(rule));
            }else{
                msg = messageMap.get(errCd);
            }
            
            return printErrCd ? (infoMap.get(key) + " " + msg) : msg;
        }
    }
    
    public void setDiffCodeMap(Map<String, String> map){
        diffMap = map;
    }
    
    public void setInfoMap(Map<String, String> map) {
    	infoMap = map;
    }
    
    public boolean isAdminCheck(List<String> adminCheckList) {
    	Iterator<String> itr = errMap.keySet().iterator();
    	boolean result = true;
  
		while (itr.hasNext()){
			String key = (String)itr.next();
			String componentId = key.split("\\.")[1];
			
			if (adminCheckList.indexOf(componentId) == -1) {
				result = false;
			}
		}
  
		return result;
    }
}