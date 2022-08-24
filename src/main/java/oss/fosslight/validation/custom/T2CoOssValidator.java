/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation.custom;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.domain.OssAnalysis;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.service.OssService;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidator;

public class T2CoOssValidator extends T2CoValidator {	
	private OssService 		ossService		= (OssService) getWebappContext().getBean(OssService.class);
	private OssMaster ossMaster = null;
	private OssAnalysis ossAnalysis = null;
	private List<OssMaster> ossList = null;
	private List<OssAnalysis> analysisList = null;

	private String VALID_TYPE = null;

	public final String VALID_DOWNLOADLOCATION = "DOWNLOADLOCATION";
	public final String VALID_DOWNLOADLOCATIONS = "DOWNLOADLOCATIONS";
	public final String VALID_HOMEPAGE = "HOMEPAGE";
	public final String VALID_OSS_BULK = "OSS_BULK";
	public final String VALID_OSSLIST_BULK = "OSSLIST_BULK";
	public final String VALID_OSSANALYSIS = "OSSANALYSIS";
	public final String VALID_OSSANALYSIS_LIST = "OSSANALYSISLIST";
	
	@SuppressWarnings("unchecked")
	@Override
	protected void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		String targetName = "";
		String ossId = avoidNull(map.get("OSS_ID"));
		// oss 일괄등록 validator

		if(VALID_TYPE == VALID_OSSLIST_BULK) {
			if(ossList != null) {
				for(OssMaster bean : ossList) {
					ossValidate(bean, map, errMap, diffMap, infoMap, true);
				}
			}
		} else if(VALID_TYPE == VALID_OSS_BULK) {
			ossValidate(ossMaster, map, errMap, diffMap, infoMap, false);
		} else if(VALID_TYPE == VALID_OSSANALYSIS) {
			ossAnalysisValidate(ossAnalysis, map, errMap, diffMap, infoMap, false);
		} else if(VALID_TYPE == VALID_OSSANALYSIS_LIST) {
			if(analysisList != null) {
				for(OssAnalysis bean : analysisList) {
					ossAnalysisValidate(bean, map, errMap, diffMap, infoMap, true);
				}
			}
		} else if(VALID_TYPE == VALID_DOWNLOADLOCATION){ // DOWNLOAD LOCATION URL을 중복으로 작성한 경우
			targetName = "DOWNLOAD_LOCATION";
			
			if(isEmpty(ossId) && !errMap.containsKey(targetName) && map.containsKey(targetName) && !isEmpty(map.get(targetName))) {
				OssMaster param = new OssMaster();
				param.setDownloadLocation(map.get(targetName));
				param.setOssName((String) map.get("OSS_NAME"));
				Map<String, Object> paramMap = ossService.checkExistsOssDownloadLocation(param);
				List<OssMaster> downLoadLocationList = (List<OssMaster>) paramMap.get("downloadLocation");
				
				if(!downLoadLocationList.isEmpty()) {
					String sortOrder = downLoadLocationList.get(0).getSOrder();
					String msg = "";
					
					if("1".equals(sortOrder)) {
						msg = MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), "");
					} else {
						msg = MessageFormat.format(getCustomMessage(targetName + ".PARTIAL_DUPLICATED"), "");
					}
					
					for(int i = 0 ; i < downLoadLocationList.size() ; i++) {
						if("1".equals(sortOrder) && !"1".equals(downLoadLocationList.get(i).getSOrder())) {
							msg += "<br>" + MessageFormat.format(getCustomMessage(targetName + ".PARTIAL_DUPLICATED"), "");
							msg += MessageFormat.format(getCustomMessage(targetName + ".LIST_LINK"), downLoadLocationList.get(i).getOssName(), downLoadLocationList.get(i).getOssName());
						} else {
							msg += (i==0?"":",") + MessageFormat.format(getCustomMessage(targetName + ".LIST_LINK"), downLoadLocationList.get(i).getOssName(), downLoadLocationList.get(i).getOssName());
						}
						sortOrder = downLoadLocationList.get(i).getSOrder();
					}
					
					diffMap.put(targetName, msg);
				}
			}
		} else if(VALID_TYPE == VALID_DOWNLOADLOCATIONS){
			targetName = "DOWNLOAD_LOCATIONS";
			
			if(isEmpty(ossId) && !errMap.containsKey(targetName) && map.containsKey(targetName) && !isEmpty(map.get(targetName))) {
				String[] downloadLocations = map.get(targetName).split("\t");
				targetName = "DOWNLOAD_LOCATION";
				List<String> result = new ArrayList<String>();
				
				for(String downloadLocation : downloadLocations){
					OssMaster param = new OssMaster();
					param.setDownloadLocation(downloadLocation);
					param.setOssName((String) map.get("OSS_NAME"));
					Map<String, Object> paramMap = ossService.checkExistsOssDownloadLocation(param);
					List<OssMaster> downLoadLocationList = (List<OssMaster>) paramMap.get("downloadLocation");
					
					if(!downLoadLocationList.isEmpty()) {
						String sortOrder = downLoadLocationList.get(0).getSOrder();
						String msg = "";
						
						if("1".equals(sortOrder)) {
							msg = MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), "");
						} else {
							msg = MessageFormat.format(getCustomMessage(targetName + ".PARTIAL_DUPLICATED"), "");
						}
						
						for(int i = 0 ; i < downLoadLocationList.size() ; i++) {
							if("1".equals(sortOrder) && !"1".equals(downLoadLocationList.get(i).getSOrder())) {
								msg += "<br>" + MessageFormat.format(getCustomMessage(targetName + ".PARTIAL_DUPLICATED"), "");
								msg += MessageFormat.format(getCustomMessage(targetName + ".LIST_LINK"), downLoadLocationList.get(i).getOssName(), downLoadLocationList.get(i).getOssName());
							} else {
								msg += (i==0?"":",") + MessageFormat.format(getCustomMessage(targetName + ".LIST_LINK"), downLoadLocationList.get(i).getOssName(), downLoadLocationList.get(i).getOssName());
							}
							sortOrder = downLoadLocationList.get(i).getSOrder();
						}
						
						result.add(downloadLocation+"@@"+msg);
					}
				}
				
				if(result.size() > 0){
					diffMap.put("DOWNLOAD_LOCATIONS", StringUtils.join(result, "||"));
				}
			}
		} else if(VALID_TYPE == VALID_HOMEPAGE){ // HOMEPAGE URL을 중복으로 작성한 경우
			targetName = "HOMEPAGE";
			if(isEmpty(ossId) && !errMap.containsKey(targetName) && map.containsKey(targetName) && !isEmpty(map.get(targetName))) {
				OssMaster param = new OssMaster();
				param.setHomepage(map.get(targetName));
				param.setOssName((String) map.get("OSS_NAME"));
				Map<String, Object> paramMap = ossService.checkExistsOssHomepage(param);
				List<OssMaster> homepageList = (List<OssMaster>) paramMap.get("homepage");
				
				if(!homepageList.isEmpty()) {
					String sortOrder = homepageList.get(0).getSOrder();
					String msg = "";
					
					if("1".equals(sortOrder)) {
						msg = MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), "");
					} else {
						msg = MessageFormat.format(getCustomMessage(targetName + ".PARTIAL_DUPLICATED"), "");
					}
					
					for(int i = 0 ; i < homepageList.size() ; i++) {
						if("1".equals(sortOrder) && !"1".equals(homepageList.get(i).getSOrder())) {
							msg += "<br>" + MessageFormat.format(getCustomMessage(targetName + ".PARTIAL_DUPLICATED"), "");
							msg += MessageFormat.format(getCustomMessage(targetName + ".LIST_LINK"), homepageList.get(i).getOssName(), homepageList.get(i).getOssName());
						} else {
							msg += (i==0?"":",") + MessageFormat.format(getCustomMessage(targetName + ".LIST_LINK"), homepageList.get(i).getOssName(), homepageList.get(i).getOssName());
						}
						sortOrder = homepageList.get(i).getSOrder();
					}
					
					diffMap.put(targetName, msg);
				}
			}
		} else {
			// 1. version
			targetName = "OSS_VERSION";
			
			if(!errMap.containsKey(targetName) && map.containsKey(targetName)) {
				// DB체크만
				OssMaster param = new OssMaster(ossId);
				param.setOssVersion(map.get(targetName));
				param.setOssName(map.get("OSS_NAME"));
				OssMaster result = ossService.checkExistsOss(param);
				
				if(result != null) {
					errMap.put(targetName, MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), map.get(targetName), result.getOssId(), result.getOssId(), result.getOssName()));
				}
			}
			
			// 2. NICK NAME
			targetName = "OSS_NICKNAMES";
			
			if(!errMap.containsKey(targetName) && (map.containsKey(targetName + ".1") || map.containsKey(targetName)) ) {
	        	List<String> nickNameKeyList = new ArrayList<>();
	        	
	        	if(!map.containsKey(targetName + ".1") && map.containsKey(targetName)) {
	        		String _seqkey = targetName;
	        		
	        		if(!errMap.containsKey(_seqkey) && !StringUtil.isEmpty(map.get(_seqkey))) {
	        			// 3-1 동일한 닉네임을 입력한 경우
	        			if(nickNameKeyList.contains(map.get(_seqkey).trim().toUpperCase())) {
	        				errMap.put(_seqkey, targetName + ".SAME");
	        			} else {
	        				nickNameKeyList.add(map.get(_seqkey).trim().toUpperCase());
	        				
	        				// 3-2 oss name 과 같은 경우
	        				if(map.get(_seqkey).trim().equalsIgnoreCase(avoidNull(map.get("OSS_NAME")).trim())) {
	        					errMap.put(_seqkey, targetName + ".SAME");
	        				// 3-3 db 에 존재하는 경우
	        				} else {
	        					OssMaster param = new OssMaster(ossId);
	        					param.setOssName(map.get(_seqkey).trim());
	        					
	        					if(!isEmpty(map.get("OSS_NAME"))) {
	        						param.setOssNameTemp(map.get("OSS_NAME"));
	        					}
	        					
	        					OssMaster result = ossService.checkExistsOssNickname(param);
	        					
	        					if(result != null) {
	        						errMap.put(_seqkey, MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), map.get(_seqkey), result.getOssId(), result.getOssId(), result.getOssName()));
	        					}
	        				}
	        			}
	        		}
	        	}else{
		        	for(int i = 1; map.containsKey(targetName + "." + i); i++){
		        		String _seqkey = targetName + "." + i;
		        		
		        		if(!errMap.containsKey(_seqkey) && !StringUtil.isEmpty(map.get(_seqkey))) {
		        			// 3-1 동일한 닉네임을 입력한 경우
		        			if(nickNameKeyList.contains(map.get(_seqkey).trim().toUpperCase())) {
		        				errMap.put(_seqkey, targetName + ".SAME");
		        			} else {
		        				nickNameKeyList.add(map.get(_seqkey).trim().toUpperCase());
		        				
		        				// 3-2 oss name 과 같은 경우
		        				if(map.get(_seqkey).trim().equalsIgnoreCase(avoidNull(map.get("OSS_NAME")).trim())) {
		        					errMap.put(_seqkey, targetName + ".SAME");
		        				// 3-3 db 에 존재하는 경우
		        				} else {
		        					OssMaster param = new OssMaster(ossId);
		        					param.setOssName(map.get(_seqkey).trim());
		        					
		        					if(!isEmpty(map.get("OSS_NAME"))) {
		        						param.setOssNameTemp(map.get("OSS_NAME"));
		        					}
		        					
		        					OssMaster result = ossService.checkExistsOssNickname(param);
		        					
		        					if(result != null) {
		        						errMap.put(_seqkey, MessageFormat.format(getCustomMessage(targetName + ".DUPLICATED"), map.get(_seqkey), result.getOssId(), result.getOssId(), result.getOssName()));
		        					}
		        				}
		        			}
		        		}
		        	}
	        	}
			}
			
			// 17.02.15 yuns
			// OSS NAME이 이미 등록되어 있는 경우 체크 추가
			targetName = "OSS_NAME";
			
			if(!errMap.containsKey(targetName) && map.containsKey(targetName) && !isEmpty(map.get(targetName))) {
				OssMaster param = new OssMaster(ossId);
				param.setOssName(map.get(targetName));
				OssMaster result = ossService.checkExistsOssNickname2(param);
				
				if(result != null) {
					errMap.put(targetName, MessageFormat.format(getCustomMessage(targetName + ".DUPLICATEDNICK"), map.get(targetName), result.getOssId(), result.getOssId(), result.getOssName()));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void ossValidate(OssMaster ossBean, Map<String, String> map, Map<String, String> errMap,
			Map<String, String> diffMap, Map<String, String> infoMap, boolean useGridSeq) {
		if(ossBean != null) {
			// OSS Name
			// 이미등록된 경우 체크
			String basicKey = "OSS_NAME";
			String gridKey = StringUtil.convertToCamelCase(basicKey);
			String errCd = checkBasicError(basicKey, gridKey, ossBean.getOssName(), false);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), errCd);
			} else {
				// 초기표시인 경우, Identification과 동일
				if(useGridSeq) {
					if(!CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossBean.getOssName().toUpperCase())) {
						errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".UNCONFIRMED");
					}
				} else {
					OssMaster param = new OssMaster();
					param.setOssName(ossBean.getOssName());
					OssMaster result = ossService.checkExistsOssNickname2(param);
					
					if(result != null) {
						errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".DUPLICATEDNICK2");
					}
				}

			}
			
			// OSS Version
			basicKey = "OSS_VERSION";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, ossBean.getOssVersion(), true);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), errCd);
			} else {
				// OSS Name에 에러가 있는 경우 version의 유효성 체크할 필요 없음
				if(!hasError(errMap, "OSS_NAME", ossBean.getGridId(), useGridSeq)) {
					if(useGridSeq) {
						if(!CoCodeManager.OSS_INFO_UPPER.containsKey( (ossBean.getOssName() + "_" + ossBean.getOssVersion()).toUpperCase() )) {
							errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".UNCONFIRMED");
						}
					} else {
						// DB체크만
						// 필요한가? 어짜피 skip하면됨 일단은 체크
						OssMaster param = new OssMaster();
						param.setOssVersion(ossBean.getOssVersion());
						param.setOssName(ossBean.getOssName());
						OssMaster result = ossService.checkExistsOss(param);
						
						if(result != null) {
							errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".DUPLICATED2");
						}
					}
				}
			}
			
			// license
			basicKey = "LICENSE_NAME";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, ossBean.getLicenseName(), false);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), errCd);
			} else {
				boolean breakPoint = false;
				String chkStr = ossBean.getLicenseName().replaceAll("\\(", " ").replaceAll("\\)", " ").toUpperCase();
				
				for(String s1 : chkStr.split(" OR ")) {
					for(String s2 : s1.split(" AND ")) {
						if(!CoCodeManager.LICENSE_INFO_UPPER.containsKey(s2.trim().toUpperCase())) {
							errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".UNCONFIRMED");
							breakPoint = true;
							
							break;
						}
					}
					if(breakPoint) {
						break;
					}
				}
				
				// error가 없다면, 괄호안에 OR 조건이 포함되어 있는지 체크
				// 괄호 안에는 OR Operator가 존재할 수 없음 (AND만)
				if(!breakPoint) {
					Pattern p = Pattern.compile("\\(([^()]*)\\)");
					Matcher m = p.matcher(ossBean.getLicenseName());
					
					while (m.find()) {
						if(m.group().toUpperCase().indexOf(" OR ") > -1) {
							errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".ORCONDITIONS");
							
							break;
						}
					}	
				}
			}
			
			// nickname 체크
			basicKey = "OSS_NICKNAME";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			
			if(!isEmpty(ossBean.getOssNickname())) {
				List<String> nickList = Arrays.asList(ossBean.getOssNickname().split(","));
				String upperOssName = ossBean.getOssName().trim().toUpperCase();
				List<String> dupCheckList = new ArrayList<>();
				
				for(String s : nickList) {
					if(isEmpty(s)) {
						continue;
					}
					
					String upperNick = s.trim().toUpperCase();
					
					// oss name과 동일한 경우
					if(upperNick.equalsIgnoreCase(upperOssName)) {
						errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".DUPLICATED");
					} else if(dupCheckList.contains(upperNick)) {
						// 동일한 nick name을 두번이상 설정한 경우
						errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".DUPLICATED");
					} else {
						// 다른 oss 에서 이미 사용하고 있는 경우
						OssMaster param = new OssMaster();
						param.setOssName(upperNick);
						param.setOssNameTemp(upperOssName);
						OssMaster result = ossService.checkExistsOssNickname(param);
						
						if(result != null) {
							errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".SAME");
						}
					}
					
					dupCheckList.add(upperNick);
				}
			}
		
			// downloadlocation
			basicKey = "DOWNLOAD_LOCATION";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			String[] downloadLocation = ossBean.getDownloadLocation().split(",");
			
			for(String location : downloadLocation){
				errCd = checkBasicError(basicKey, gridKey, location.trim(), true);
				
				if(!isEmpty(errCd)){
					break;
				}
			}
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), errCd);
			} else if(!isEmpty(ossBean.getDownloadLocation())){
				for(String location : downloadLocation){
					OssMaster param = new OssMaster();
					
					// version up 인 경우, 해당 oss name을 제외하고 검증
					if(!isEmpty(ossBean.getOssName()) && CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossBean.getOssName().trim().toUpperCase())) {
						param.setOssName(CoCodeManager.OSS_INFO_UPPER_NAMES.get(ossBean.getOssName().trim().toUpperCase()));
					}
					
					param.setDownloadLocation(location.trim());
					Map<String, Object> paramMap = ossService.checkExistsOssDownloadLocation(param);
					List<OssMaster> list = (List<OssMaster>) paramMap.get("downloadLocation");
					
					if(list != null && !list.isEmpty()) {
						String sortOrder = list.get(0).getSOrder();
						if("1".equals(sortOrder)) {
							diffMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".DUPLICATED");
						} else {
							diffMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".PARTIAL_DUPLICATED");
						}
						break;
					}
				}
			}
			
			// homepage
			basicKey = "HOMEPAGE";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, ossBean.getHomepage().trim(), true);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), errCd);
			} else if(!isEmpty(ossBean.getHomepage())) {
				OssMaster param = new OssMaster();
				
				// version up 인 경우, 해당 oss name을 제외하고 검증
				if(!isEmpty(ossBean.getOssName()) && CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossBean.getOssName().trim().toUpperCase())) {
					param.setOssName(CoCodeManager.OSS_INFO_UPPER_NAMES.get(ossBean.getOssName().trim().toUpperCase()));
				}
				
				param.setHomepage(ossBean.getHomepage().trim());
				Map<String, Object> paramMap = ossService.checkExistsOssHomepage(param);
				List<OssMaster> list = (List<OssMaster>) paramMap.get("homepage");
				
				if(list != null && !list.isEmpty()) {
					String sortOrder = list.get(0).getSOrder();
					
					if("1".equals(sortOrder)) {
						diffMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".DUPLICATED");
					} else {
						diffMap.put(basicKey + (useGridSeq ? "."+ossBean.getGridId() : ""), basicKey+".PARTIAL_DUPLICATED");
					}
				}
			}				
		}		
	}

	@SuppressWarnings("unchecked")
	private void ossAnalysisValidate(OssAnalysis analysisBean, Map<String, String> map, Map<String, String> errMap,
			Map<String, String> diffMap, Map<String, String> infoMap, boolean useGridSeq) {
		if(analysisBean != null) {
			// OSS Name
			// 이미등록된 경우 체크
			String basicKey = "OSS_NAME";
			String gridKey = StringUtil.convertToCamelCase(basicKey);
			String errCd = checkBasicError(basicKey, gridKey, analysisBean.getOssName(), false);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), errCd);
			} else {
				// 초기표시인 경우, Identification과 동일
				if(useGridSeq) {
					if(!CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(analysisBean.getOssName().toUpperCase())) {
						errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".UNCONFIRMED");
					}
				} else {
					OssMaster param = new OssMaster();
					param.setOssName(analysisBean.getOssName());
					OssMaster result = ossService.checkExistsOssNickname2(param);
					
					if(result != null) {
						errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".DUPLICATEDNICK2");
					}
				}
			}
			
			// OSS Version
			basicKey = "OSS_VERSION";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, analysisBean.getOssVersion(), true);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), errCd);
			} else {
				// OSS Name에 에러가 있는 경우 version의 유효성 체크할 필요 없음
				if(!hasError(errMap, "OSS_NAME", analysisBean.getGridId(), useGridSeq)) {
					if(useGridSeq) {
						if(!CoCodeManager.OSS_INFO_UPPER.containsKey( (analysisBean.getOssName() + "_" + avoidNull(analysisBean.getOssVersion())).toUpperCase() )) {
							errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".UNCONFIRMED");
						}
					} else {
						// DB체크만
						// 필요한가? 어짜피 skip하면됨 일단은 체크
						OssMaster param = new OssMaster();
						param.setOssVersion(analysisBean.getOssVersion());
						param.setOssName(analysisBean.getOssName());
						OssMaster result = ossService.checkExistsOss(param);
						
						if(result != null) {
							errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".DUPLICATED2");
						}
					}
				}
			}
			
			// license
			basicKey = "LICENSE_NAME";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, analysisBean.getLicenseName(), false);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), errCd);
			} else {
				boolean breakPoint = false;
				
				for(String s2 : analysisBean.getLicenseName().split(",")) {
					if(!CoCodeManager.LICENSE_INFO_UPPER.containsKey(s2.trim().toUpperCase())) {
						errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".UNCONFIRMED");
						breakPoint = true;
						
						break;
					}
				}
								
				// error가 없다면, 괄호안에 OR 조건이 포함되어 있는지 체크
				// 괄호 안에는 OR Operator가 존재할 수 없음 (AND만)
				if(!breakPoint) {
					Pattern p = Pattern.compile("\\(([^()]*)\\)");
					Matcher m = p.matcher(analysisBean.getLicenseName());
					
					while (m.find()) {
						if(m.group().toUpperCase().indexOf(" OR ") > -1) {
							errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".ORCONDITIONS");
							
							break;
						}
					}	
				}
			}
			
			// nickname 체크
			basicKey = "OSS_NICKNAME";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			
			if(!isEmpty(analysisBean.getOssNickname())) {
				List<String> nickList = Arrays.asList(analysisBean.getOssNickname().split(","));
				
				String upperOssName = analysisBean.getOssName().trim().toUpperCase();
				List<String> dupCheckList = new ArrayList<>();
				
				for(String s : nickList) {
					if(isEmpty(s)) {
						continue;
					}
					
					String upperNick = s.trim().toUpperCase();
					
					// oss name과 동일한 경우
					if(upperNick.equalsIgnoreCase(upperOssName)) {
						errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".DUPLICATED");
					} else if(dupCheckList.contains(upperNick)) {
						// 동일한 nick name을 두번이상 설정한 경우
						errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".DUPLICATED");
					} else {
						// 다른 oss 에서 이미 사용하고 있는 경우
						OssMaster param = new OssMaster();
						param.setOssName(upperNick);
						param.setOssNameTemp(upperOssName);
						OssMaster result = ossService.checkExistsOssNickname(param);
						
						if(result != null) {
							errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".SAME");
						}
					}
					
					dupCheckList.add(upperNick);
				}
			}
		
			// downloadlocation
			basicKey = "DOWNLOAD_LOCATION";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			String[] downloadLocation = analysisBean.getDownloadLocation().split(",");
			
			for(String location : downloadLocation){
				errCd = checkBasicError(basicKey, gridKey, location.trim(), true);
				
				if(!isEmpty(errCd)){
					break;
				}
			}
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), errCd);
			} else if(!isEmpty(analysisBean.getDownloadLocation())){
				for(String location : downloadLocation){
					OssMaster param = new OssMaster();
					
					// version up 인 경우, 해당 oss name을 제외하고 검증
					if(!isEmpty(analysisBean.getOssName()) && CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(analysisBean.getOssName().trim().toUpperCase())) {
						param.setOssName(CoCodeManager.OSS_INFO_UPPER_NAMES.get(analysisBean.getOssName().trim().toUpperCase()));
					}
					
					param.setDownloadLocation(location.trim());
					Map<String, Object> paramMap = ossService.checkExistsOssDownloadLocation(param);
					List<OssMaster> list = (List<OssMaster>) paramMap.get("downloadLocation");
					
					if(list != null && !list.isEmpty()) {
						String sortOrder = list.get(0).getSOrder();
						
						if("1".equals(sortOrder)) {
							diffMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".DUPLICATED");
						} else {
							diffMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".PARTIAL_DUPLICATED");
						}
						
						break;
					}
				}
			}
			
			// homepage
			basicKey = "HOMEPAGE";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, analysisBean.getHomepage().trim(), true);
			
			if(!isEmpty(errCd)) {
				errMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), errCd);
			} else if(!isEmpty(analysisBean.getHomepage())) {
				OssMaster param = new OssMaster();
				
				// version up 인 경우, 해당 oss name을 제외하고 검증
				if(!isEmpty(analysisBean.getOssName()) && CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(analysisBean.getOssName().trim().toUpperCase())) {
					param.setOssName(CoCodeManager.OSS_INFO_UPPER_NAMES.get(analysisBean.getOssName().trim().toUpperCase()));
				}
				
				param.setHomepage(analysisBean.getHomepage().trim());
				Map<String, Object> paramMap = ossService.checkExistsOssHomepage(param);
				List<OssMaster> list = (List<OssMaster>) paramMap.get("homepage");
				
				if(list != null && !list.isEmpty()) {
					String sortOrder = list.get(0).getSOrder();
					
					if("1".equals(sortOrder)) {
						diffMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".DUPLICATED");
					} else {
						diffMap.put(basicKey + (useGridSeq ? "."+analysisBean.getGridId() : ""), basicKey+".PARTIAL_DUPLICATED");
					}
				}
			}	
		}
	}


	private boolean hasError(Map<String, String> errMap, String basicKey, String gridId, boolean useGridSeq) {
		return errMap.containsKey(basicKey + (useGridSeq ? "."+gridId : ""));
	}


	@SuppressWarnings("unchecked")
	@Override
	public void setAppendix(String key, Object obj) {
		if("ossMaster".equals(key)) {
			this.ossMaster = (OssMaster) obj;
		} else if ("ossList".equals(key)) {
			this.ossList = (List<OssMaster>) obj;
		} else if ("ossAnalysis".equals(key)) {
			this.ossAnalysis = (OssAnalysis) obj;
		} else if ("analysisList".equals(key)) {
			this.analysisList = (List<OssAnalysis>) obj;
		}
	}

	@Override
	protected String treatment(String paramvalue) {
		return paramvalue;
	}

	public void setVALIDATION_TYPE(String vALID_TYPE) {
		VALID_TYPE = vALID_TYPE;
	}

}
