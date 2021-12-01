/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.ComBean;
import oss.fosslight.util.StringUtil;

@Slf4j
public abstract class T2CoValidator extends CoTopComponent {
    /** The Constant DEFAULT_REQUIRED_MARKER. */
    private final static String DEFAULT_REQUIRED_MARKER = "<span style=\"color:#f00\">*</span>";
    
    /** The rule map. */
    private Map<String, Map<String, String>> ruleMap = null;
    
    /** The message config map. */
    private Map<String, String> messageConfigMap = null;
    
    /** The hint. */
    private List<String> hint = new ArrayList<String>(1);
    
    /** The ignore. */
    private List<String> ignore = new ArrayList<String>(1);
    
    /** The custom marker. */
    private String customMarker = null;
    
    protected T2CoValidator(){
        T2CoValidationConfig conf = T2CoValidationConfig.getInstance();
        ruleMap = conf.getRuleMap();
        messageConfigMap = conf.getMessageConfigMap();
    }
    
    public void setHint(String keys){
        if(keys != null){
            hint = Arrays.asList(keys.split("\\s*,\\s*"));
            log.trace("HINT:" + hint);
        }
    }
    
    public void setIgnore(String keys){
        if(keys != null){
            ignore = Arrays.asList(keys.split("\\s*,\\s*"));
            log.trace("IGNORE:" + ignore);
        }
    }
    
    public void setRequiredMarker(String maker) {
        customMarker = maker;
    }


	protected String basicValidation(String inputValue, Map<String, String> rule){
    	return basicValidation(inputValue, rule, false);
    }
	
    protected String basicValidation(String inputValue, Map<String, String> rule, boolean ignoreRequired){
        String errCd = null;
        int limitLength = getLimitLength(rule);
        String[] inputs = nvl(inputValue).split("\\t");

        for(String input:inputs){
            if(!ignoreRequired && isRequired(rule) && isEmpty(input) ){
                //필수체크
                errCd = "REQUIRED";
            }else if( limitLength < safeLength(input) ){
                //길이체크
                errCd = "LENGTH";
            }else if(
                !isEmpty(input) &&
                !input.matches(rule.get("FORMAT"))
            ){
                //형식체크
                errCd = "FORMAT";
            }

            if(errCd != null){
                break;
            }
        }
        
        return errCd;
    }
    
    private void printDebugLog(String inputValue, String errCd, String ruleKey, String ruleKeySeq){
        if(ruleMap != null) {
            if(errCd != null) {
                Map<String, String> rule = getRule(ruleKey);
                
                log.trace("basic validation for [" + ruleKey + "] ----------------");
                log.trace( (isRequired(rule) ? "required" : "optional") + ", limit " + getLimitString(ruleKey) + ", format " + rule.get("FORMAT"));
                log.trace(ruleKeySeq + "=" + inputValue + "[error code:" + errCd + "]");
            }
        } else {
            log.debug("ruleMap is null.");
        }
    }

    public T2CoValidationResult validateRequest(Map<String, String> map, ServletRequest request){
    	return validateRequest(map, request, null);
    }
    
    public T2CoValidationResult validateRequest(Map<String, String> map, ServletRequest request, Map<String, String> keyPreMap){
        Map<String, String> reqMap = new HashMap<String, String>();
        saveRequest(request, reqMap, keyPreMap);
        T2CoValidationResult vr = validate(reqMap);
        
        if(map != null){
            map.putAll(reqMap);
        }
        
        // key prefix 처리 추가
        if(keyPreMap != null && !keyPreMap.isEmpty() && !vr.getErrorCodeMap().isEmpty()) {
        	// Data Key 복원
        	Map<String, String> errMap = vr.getErrorCodeMap();
        	Map<String, String> replaceErrMap = new HashMap<>();

        	for(String key : errMap.keySet()) {
        		if(key.indexOf("@") > -1) {
        			String paramName = key.substring(key.indexOf("@")+1);
        			String restoreKey = key.replaceAll(keyPreMap.get(paramName)+"@", "");
        			
        			replaceErrMap.put(restoreKey, errMap.get(key));
        		} else {
        			replaceErrMap.put(key, errMap.get(key));
        		}
        	}
        	
        	vr.setErrorCodeMap(replaceErrMap);
        }
        
        return vr;
    }
    
    public T2CoValidationResult validateRequest(ServletRequest request){
        return validateRequest(null, request);
    }
    

	public T2CoValidationResult validateObject(Map<String, String> map, Object source){
		return validateObject(map, source, null);
	}
	
    @SuppressWarnings("unchecked")
	public T2CoValidationResult validateObject(Map<String, String> map, Object source, String keyPre){
    	Map<String, String> reqMap = null;
    	List<String> gridKeys = null;
    	
    	if(source instanceof List<?>) {
    		reqMap = new HashMap<>();
    		gridKeys = new ArrayList<>();
        	int seqSuffix = 1;
        	String _gridKey;
        	
        	for (Object sourceVO : (List<Object>)source) {
        		_gridKey = avoidNull(((ComBean)sourceVO).getGridId(), ((ComBean)sourceVO).getNo());
        		
        		if(!isEmpty(_gridKey)) {
        			gridKeys.add(_gridKey); 
        		}
        		
        		reqMap.putAll(ConverObjectToMap(sourceVO, seqSuffix++, keyPre));
    		}
    	} else {
            reqMap = ConverObjectToMap(source, -1, keyPre);
    	}
    	
    	if(map != null) {
    		reqMap.putAll(map);
    	}
    	
        T2CoValidationResult vr = validate(reqMap);
        
        if(!isEmpty(keyPre)) {
			//STEP. 
		    //keyPre 변수가 있을시
		    //validation_properties의 property 중 Prefix@ 형태를 제거한후
		    //에러맵을 교체한다.
        	Map<String, String> errMap = vr.getErrorCodeMap();
        	Map<String, String> replaceErrMap = new HashMap<>();
        	
        	for(String key : errMap.keySet()) {
        		replaceErrMap.put(key.replaceAll(keyPre+"@", ""), errMap.get(key));
        	}
        	
        	vr.setErrorCodeMap(replaceErrMap);
        }
        
        // 시퀀스 번호를 grid key 값으로 치환한다.
        if(gridKeys != null && !gridKeys.isEmpty() && !vr.getErrorCodeMap().isEmpty()) {
        	Map<String, String> errMap = vr.getErrorCodeMap();
        	Map<String, String> addErrMap = new HashMap<>();
        	
        	 for(String key : errMap.keySet()) {
        		 if(key.indexOf(".") > -1) {
        			 String seqStr = key.substring(key.indexOf(".") +1);
        			 int seq = StringUtil.string2integer(seqStr);
        			 
        			 if(gridKeys.size() >= seq) {
        				 addErrMap.put(key.replace("." + seqStr, "." + gridKeys.get(seq -1)), errMap.get(key));
        			 }
        		 }else{
        			 addErrMap.put(key, errMap.get(key));
        		 }
        	 }
        	 
        	 //Grid에 대한 에러맵이 존재할 경우 결과에 격납
        	 if(!addErrMap.isEmpty()) {
        		 vr.setErrorCodeMap(addErrMap);
        	 }
        }
        
        return vr;
    }
    
	public T2CoValidationResult validateObject(Object sourceVO) {
    	return validateObject(null, sourceVO);
    }
	
	public Map<String, String> ConverObjectToMap(Object obj, int seq, String keyPre){
		
		Map<String, String> resultMap = new HashMap<>();
		Method[] methods = obj.getClass().getMethods();
		
		for(Method method : methods){
			// is getter check
			if (!(!method.getName().startsWith("get") || method.getParameterTypes().length != 0
					|| void.class.equals(method.getReturnType()) 
					|| !(String.class.equals(method.getReturnType()) || String[].class.equals(method.getReturnType())))) {
				Object valueObj = invokeGetter(method.getName(), obj);
				
				if(valueObj != null) {
					String key = StringUtil.convertToUnderScore(method.getName().substring(3)).toUpperCase();
					
					if(seq > 0) {
						key += "." + Integer.toString(seq);
					}
					
					if(valueObj instanceof String[]) {
						String tempVal = "";
						
						for(String strVal : (String[]) valueObj) {
							if(!isEmpty(tempVal)) {
								tempVal += "\t";
							}
							
							tempVal += strVal;
						}
						
						valueObj = tempVal;
					}
					
					if(!isEmpty(keyPre)) {
						key = keyPre + "@" + key;
					}
					
					resultMap.put(key, (String) valueObj);
				}
			}
		}
		
		return resultMap;
	}
	
	private Object invokeGetter(String name, Object obj) {
		PropertyDescriptor objPropertyDescriptor;
		Object variableValue = null;
		
		try {
			if(name.startsWith("get")) {
				objPropertyDescriptor = new PropertyDescriptor((String)name.substring(3), obj.getClass());
				variableValue = objPropertyDescriptor.getReadMethod() != null ? objPropertyDescriptor.getReadMethod().invoke(obj) : null;
			}
		} catch (IntrospectionException e) {
			//if(log.isDebugEnabled()) {e.printStackTrace();}
		} catch (IllegalAccessException e) {
			//if(log.isDebugEnabled()) {e.printStackTrace();}
		} catch (IllegalArgumentException e) {
			//if(log.isDebugEnabled()) {e.printStackTrace();}
		} catch (InvocationTargetException e) {
			//if(log.isDebugEnabled()) {e.printStackTrace();}
		}
		return variableValue;
	}
	
    public T2CoValidationResult validate(Map<String, String> map){
        checkStatus();

        //hint에서 설정한 없을 수도 있지만, 반드시 있어야하는 key에 대해서 시스템 오류를 장지하기 위해 임의 추가
        {
            Iterator<String> itr = hint.iterator();
            
            while(itr.hasNext()){
                String mustHave = (String)itr.next();
                
                if(!map.containsKey(mustHave)){
                    map.put(mustHave, "");
                }
            }
        }

        //검증
        T2CoValidationResult vr = new T2CoValidationResult(ruleMap, messageConfigMap);
        Map<String, String> errMap = new HashMap<String, String>();  // error message
        Map<String, String> diffMap = new HashMap<String, String>(); // warning message
        Map<String, String> infoMap = new HashMap<String, String>(); // gray로 표시 추가정보 표시용도

        vr.setErrorCodeMap(errMap);
        vr.setDataMap(map);
        vr.setDiffCodeMap(diffMap);
        vr.setInfoMap(infoMap);

        try{
            Iterator<String> itr = ruleMap.keySet().iterator();
            
            while(itr.hasNext()){
                String ruleKey = (String)itr.next();
                boolean doValidate = false;
                Map<String, String> rule = getRule(ruleKey);
                List<String> seqSuffix = new ArrayList<String>();
                
                if("TRUE".equalsIgnoreCase(rule.get("USE_SEQUENCE"))) {
                    for(int i = 1; map.containsKey(ruleKey + "." + i); i++) {
                        seqSuffix.add("." + i);
                    }
                } else {
                    seqSuffix.add("");
                }

                Iterator<String> itr2 = seqSuffix.iterator();
                
                while(itr2.hasNext()){
                    String ruleKeySeq = ruleKey + (String)itr2.next(); // 연번 이없는 경우는 ruleKey 와 같음
                    
                    if(map.containsKey(ruleKeySeq)) {
                        doValidate = true;
                    } else if(rule.containsKey("COMPOSITE")) {
                        StringBuffer buf = new StringBuffer();
                        String[] comp = parseExp(rule.get("COMPOSITE"));
                        boolean hasInputKey = false;
                        boolean hasInputValue = false;
                        
                        for(int i = 0; i < comp.length; i++){
                            if(comp[i].matches("'.*'")){
                                buf.append(comp[i].substring(1,comp[i].length() - 1));
                            }else{
                                if(map.containsKey(comp[i])){
                                    doValidate = true;
                                    hasInputKey = true;
                                    String s = map.get(comp[i]);
                                    hasInputValue = hasInputValue || !isEmpty(s);
                                    
                                    buf.append(s);
                                }
                            }
                        }
                        
                        if(hasInputKey){
                            map.put(ruleKeySeq, hasInputValue ? buf.toString() : "");// COMPSITE로 설정한 값을 추가
                        }
                    }

                    doValidate = (doValidate || this.hint.contains(ruleKey)) && !this.ignore.contains(ruleKey);

                    if(doValidate){
                        String errCd = basicValidation(map.get(ruleKeySeq), rule);

                        if(log.isDebugEnabled()){
                            printDebugLog(map.get(ruleKeySeq), errCd, ruleKey, ruleKeySeq);
                        }

                        if(errCd != null){
                            errCd = ruleKey + "." + errCd;
                            errMap.put(ruleKeySeq, errCd);
                        }else{
                            vr.validate(ruleKeySeq);
                        }
                    }
                }
            }
        } catch(Exception e) {
        	log.error(e.getMessage());
        }

        customValidation(map, errMap, diffMap, infoMap);
        
        return vr;
    }
    
    protected static int safeLength(String str){
        return str == null ? 0 : str.length();
    }
    
    static int getLimitLength(Map<String, String> rule){
        int maxLength = 0;
        
        try {
            maxLength = Integer.parseInt((String)rule.get("LENGTH"));
            
            if(maxLength < 0) {
            	throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalArgumentException("rule map has invalid value at 'LENGTH' of " + rule.get("KEY"));
        }
        
        return maxLength == 0 ? Integer.MAX_VALUE : maxLength;
    }

    public void saveRequest(ServletRequest request, Map<String, String> map){
    	saveRequest(request, map, null);
    }
    
    public void saveRequest(ServletRequest request, Map<String, String> map, Map<String, String> keyPreMap){
        Enumeration<?> names = request.getParameterNames();
        
        while( names.hasMoreElements() ){
        	boolean ignore = false;
            String name = (String) names.nextElement();
            String value = "";
            
            if(request.getParameterValues(name).length > 1){
                String[] vals = request.getParameterValues(name);
                Map<String,String> rule = getRule(CoConstDef.VALIDATION_USE_CAMELCASE ? StringUtil.convertToUnderScore(name).toUpperCase() : name);
                
                if(rule != null && "TRUE".equalsIgnoreCase(rule.get("USE_SEQUENCE"))) {
                    for(int i = 0; i < vals.length; i++){
                        value = vals[i];
                        
                    	if(i == 0) {
                    		if(CoConstDef.VALIDATION_USE_CAMELCASE) {
                    			name = StringUtil.convertToUnderScore(name).toUpperCase();
                    		}
                    		
                    		String _val = "";
                    		
                    		for(int subIdx = 0; subIdx < vals.length; subIdx++){
                    			_val = (subIdx == 0 ? "" : value + "\t") + vals[subIdx];
                    		}
                    		
                    		map.put( convertPrefixKey(name, keyPreMap), treatment(baseTreatment(_val)) );
                    	}
                    	
                    	map.put(  convertPrefixKey(name, keyPreMap) + "." + Integer.toString(i+1), treatment(baseTreatment(value)) );
                    }    
                    
                    ignore = true;
                } else {
                    for(int i = 0; i < vals.length; i++){
                        value = (i == 0 ? "" : value + "\t") + vals[i];
                    }                	
                }
            }else{
                value = request.getParameter(name);
            }
            
            if(!ignore) {
            	if(CoConstDef.VALIDATION_USE_CAMELCASE) {
            		name = StringUtil.convertToUnderScore(name).toUpperCase();
            	}
            	
            	map.put( convertPrefixKey(name, keyPreMap), treatment(baseTreatment(value)) );
            }
        }
        
        return;
    }

    private String convertPrefixKey(String name, Map<String, String> keyPreMap) {
    	if(keyPreMap != null && keyPreMap.containsKey(name)) {
    		return keyPreMap.get(name) + "@" + name;
    	}
    	
		return name;
	}
    
    public boolean isRequired(String key){
        checkStatus();
        
        if(key == null) {
        	throw new NullPointerException("key name is null.");
        }
        
        return ruleMap.containsKey(key) && isRequired(getRule(key));
    }
    
    private boolean isRequired(Map<String, String> rule){
        return rule.get("REQUIRED").toUpperCase().equals("TRUE");
    }
    
    public String getLimitString(String key){
        int limitLength = getLimitLength(getRule(key));
        return limitLength == Integer.MAX_VALUE ? "" : Integer.toString(limitLength);
    }
    
    public String getCustomMessage(String key){
    	if(messageConfigMap.containsKey(key)) {
    		return messageConfigMap.get(key);
    	}
    	
    	log.error("Can not find validation message config key : " + key);
    	return "Error";
    }
    
    private void checkStatus(){
        if(this.ruleMap == null){
            throw new IllegalStateException("No Validation Rule.");
        }
        
        return;
    }
    
    public Map<String, String> getRequiredItemMap(){
        if (!isEmpty(customMarker)) {
            return getRequiredItemMap(customMarker);
        } else {
            return getRequiredItemMap(DEFAULT_REQUIRED_MARKER);
        }
    }
    
    public Map<String, String> getRequiredItemMap(String marker){
        checkStatus();
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> itr = ruleMap.keySet().iterator();
        
        while(itr.hasNext()){
            String key = (String) itr.next();
            map.put(key, isRequired(key) ? marker : "");
        }
        
        return map;
    }
    
    public Map<String, String> getLimitLengthMap(){
        checkStatus();
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> itr = ruleMap.keySet().iterator();
        
        while(itr.hasNext()){
            String key = (String) itr.next();
            map.put(key, getLimitString(key));
        }
        
        return map;
    }
    
    private static String[] parseExp(String str){
        str = str.replaceAll("((?:'.*?[^']|')'|[^'\\+])\\s*\\+\\s*", "$1\n").replaceAll("''(.)", "'$1");
        
        return str.split("\\n");
    }
    
    public void arrangeMapByRule(Map<String,String> map){
        checkStatus();
        
      //확인 입력 등 기록 되지 않은 값 복원
        {
            Iterator<String> itr = map.keySet().iterator();
            Map<String,String> addition = new HashMap<String, String>();
            
            while(itr.hasNext()){
                String key = (String)itr.next();
                Map<String,String> rule = getRule(key);

                if(ruleMap.containsKey(key) && rule.get("COPY_WHEN_INIT") != null){
                    String copyTo = rule.get("COPY_WHEN_INIT");
                    
                    if(!map.containsKey(copyTo)){
                        addition.put(copyTo, map.get(key));
                    }
                }
            }
            map.putAll(addition);
        }
        
        // COMPOSITE 로 지정된 분할 된 값 의 복원 
        {
            Iterator<String> itr = map.keySet().iterator();
            Map<String,String> addition = new HashMap<String, String>();
            
            while(itr.hasNext()){
                String key = (String)itr.next();
                Map<String,String> rule = getRule(key);

                if(rule.containsKey("COMPOSITE")){
                    String val = map.get(key);
                    String[] comp = parseExp(rule.get("COMPOSITE"));
                    String[] split = null;
                    
                    if(rule.containsKey("SPLIT_WHEN_INIT")){
                        split = parseExp(rule.get("SPLIT_WHEN_INIT"));
                        
                        if(comp.length != split.length){
                            throw new IllegalArgumentException("inconsistency of COMPOSITE and SPLIT_WHEN_INIT of rule map.");
                        }
                    }
                    for(int i = 0; i < comp.length; i++){
                        if(isEmpty(val)) {
                            if(!comp[i].matches("'.*'")){
                                addition.put(comp[i], "");
                            }
                        } else {
                            if(comp.length - 1 <= i) {
                                //last element
                                if(!comp[i].matches("'.*'")) {
                                    addition.put(comp[i], val);
                                }
                            } else {
                                //has next
                                if(!comp[i].matches("'.*'")) {

                                    if(comp[i + 1].matches("'.*'")) {
                                        // 다음 COMPOSITE 요소 가 리터럴 이라면, 그 리터럴 직전까지 ( 없으면 끝까지 ) 를 취득
                                        String literal = comp[i + 1].substring(1, comp[i + 1].length() - 1);
                                        int p = val.indexOf(literal) < 0 ? val.length() : val.indexOf(literal);
                                        addition.put(comp[i], val.substring(0, p));
                                        val = val.substring(p);
                                    } else {
                                        // 다음 COMPOSITE 요소 가 변수 라면 지정 이 있으면 그 길이 없으면 끝까지 취득
                                        int limit = val.length();//값의 최대치를 초기화
                                        
                                        if(split != null){
                                            try{
                                                limit = Integer.parseInt(split[i].replaceFirst(".+\\{(\\d+)\\}$", "$1"));
                                            }catch(Exception e){
                                                throw new IllegalArgumentException("invalid argument for SPLIT_WHEN_INIT of rule map.");
                                            }
                                        }
                                        
                                        int p = limit < val.length() ? limit : val.length();
                                        addition.put(comp[i], val.substring(0, p));
                                        val = val.substring(p);
                                    }
                                } else {
                                    val = val.substring(comp[i].length() - 2);//2 means ' * 2
                                }
                            }
                        }
                    }
                }
            }
            
            map.putAll(addition);
        }
    }
    
    protected Map<String,String> getRule(String key){
        checkStatus();

        return ruleMap.containsKey(key) ? ruleMap.get(key) : new HashMap<String, String>();
    }
    
    private String baseTreatment(String s){
        if(s == null){
            return null;
        }
        
        return s.replaceAll("\\t", " ")
		            .replaceFirst("^[\\u0020\\u3000]*", "")
		            .replaceFirst("[\\u0020\\u3000]*$", "");
    }
    
    protected abstract void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap);
    
    public abstract void setAppendix(String key, Object obj);
    
    protected abstract String treatment(String paramvalue);

    protected String checkBasicError(String basicKey, String val) {
		return checkBasicError(basicKey, null, val, false);
	}

    protected String checkBasicError(String basicKey, String val, boolean ignoreRequired) {
		return checkBasicError(basicKey, null, val, ignoreRequired);
	}
    
    protected String checkBasicError(String basicKey, String gridKey, String val) {
		return checkBasicError(basicKey, gridKey, val, false);
	}
    
    protected String checkBasicError(String basicKey, String gridKey, String val, boolean ignoreRequired) {
		String errCd = "";
		Map<String, String> ruleMap = getRule(basicKey);
		
		if(ruleMap != null && !ruleMap.isEmpty()) {
			errCd = basicValidation(val, ruleMap, ignoreRequired);
			
			if(!isEmpty(errCd)) {
				errCd = basicKey + "." + errCd;
			}
		}
		
		return errCd;
	}
}