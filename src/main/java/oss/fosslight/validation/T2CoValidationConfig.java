/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import oss.fosslight.CoTopComponent;
import oss.fosslight.config.AppConstBean;

@Component("t2CoValidationConfig")
@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_VALIDATION_PROPERTIES)})
public class T2CoValidationConfig extends CoTopComponent {
    private static T2CoValidationConfig instance = null;
    
    private static Map<String, Map<String, String>> ruleMap = null;
    
    private static Map<String, Object> ruleAllMap = null;
    
    private static Map<String, String> messageConfigMap = null;
    
    private static Environment env;
	
	@SuppressWarnings("static-access")
	@Resource
	public void setEnvironment(Environment env) throws IOException {
		this.env = env;
		init();
	}

    private static final String RULE_TYPE_DEF = "LENGTH, REQUIRED, FORMAT, COMPOSITE, SPLIT_WHEN_INIT, COPY_WHEN_INIT, USE_SEQUENCE";
    
    public T2CoValidationConfig(){}
    
    private void init() throws IOException{
        load();
        
        instance = new T2CoValidationConfig();
    }
    
    public static T2CoValidationConfig getInstance(){
        if (instance == null){
            throw new IllegalStateException("not initialized");
        }
        
        return instance;
    }
    
    public Map<String, Map<String,String>> getRuleMap(){
        return ruleMap;
    }
    
    public Map<String, Object> getRuleAllMap(){
        return ruleAllMap;
    }
    
    public Map<String, String> getMessageConfigMap(){
        return messageConfigMap;
    }
    
    @SuppressWarnings({ "static-access", "unchecked" })
	private synchronized void load() throws IOException{

        Map<String, Map<String, String>> ruleMap = new HashMap<>();
        Map<String, String> messageConfigMap = new HashMap<>();
        List<String> ruleTypeDef = Arrays.asList(RULE_TYPE_DEF.split("\\s*,\\s*"));

        Map<String, Object> map = new HashMap<>();
        if (env instanceof ConfigurableEnvironment environment) {
        	for (org.springframework.core.env.PropertySource<?> propertySource : environment.getPropertySources()) {
                if (propertySource instanceof CompositePropertySource cps 
                		&& propertySource.getName().indexOf("validation") > 0) {
                	Iterator<org.springframework.core.env.PropertySource<?>> itr = cps.getPropertySources().iterator();
                	while (itr.hasNext()) {
                		Map<? extends String, ? extends Object> property = (Map<? extends String, ? extends Object>) itr.next().getSource();
                		map.putAll(property);
					}
                }
    		}
        }
        
        for (Object oKey : map.keySet()){
            String key = (String)oKey;
            
            if (!key.contains(".") ){
                continue;
            }
            
            String val = (String) map.get(key);

            while (val.matches(".*\\$\\{[\\w\\d_]+\\}.*")){
                String refKey = val.replaceFirst(".*\\$\\{([\\w\\d]+)\\}.*", "$1");
                String refVal = ((String) map.get(key)).replaceAll("\\\\", "\\\\\\\\");//escape 재처리
                val = val.replaceAll("\\$\\{" + refKey + "\\}", refVal);
            }

            //Map에 값추가
            if (key.endsWith(".MSG")){
                String ruleKey = key.substring(0, key.length() - 4);//.MSG제거
                messageConfigMap.put(ruleKey.toUpperCase(), val);
            }else{
                String ruleKey = key.replaceFirst("(.*?[^\\\\])\\..*", "$1");
                String ruleType = key.substring(ruleKey.length() + 1).toUpperCase();
                ruleKey = ruleKey.replaceAll("\\\\\\.", "\\.");//.을 escape 해서 제거

                if (ruleTypeDef.contains(ruleType)){
                    Map<String, String> rule = ruleMap.get(ruleKey);
                    
                    if (rule == null){
                        rule = new HashMap<>();
                        ruleMap.put(ruleKey.toUpperCase(), rule);
                    }
                    
                    rule.put(ruleType, val);
                } else {
                    //미정의된 rule type(형식 오류)의 경우
                    throw new IllegalArgumentException("undefined rule-type found in properties:" + ruleType);
                }
            }
        }

        this.ruleMap = ruleMap;
        this.messageConfigMap = messageConfigMap;
        this.ruleAllMap = map;
    }
    
    public void reload() throws IOException{
        if (instance != null){
        	instance.load();
        }else{
            throw new IllegalStateException("not initialized.");
        }
    }
}
