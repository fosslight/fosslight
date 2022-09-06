/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SelfCheckService;

@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
@Slf4j
public class YamlUtil extends CoTopComponent {
	private static String writepath = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	private static FileService fileService = (FileService)	getWebappContext().getBean(FileService.class);
	private static ProjectService projectService = (ProjectService) getWebappContext().getBean(ProjectService.class);
	private static SelfCheckService selfCheckService = (SelfCheckService) getWebappContext().getBean(SelfCheckService.class);
	
	public static String makeYaml(String type, String dataStr) throws Exception {
		String downloadFileId = "";
		
		switch(type) {
			case CoConstDef.CD_DTL_COMPONENT_ID_SRC: 		// OSS Name, OSS version, License, Download location, Homepage, Copyright,Exclude 가 동일한 경우 Merge. > Source Name or Path, Binary Name, Binary Name or Source Path, Comment 
			case CoConstDef.CD_DTL_COMPONENT_ID_BIN:		// OSS Name, OSS version, License, Download location, Homepage, Copyright,Exclude 가 동일한 경우 Merge. > Source Name or Path, Binary Name, Binary Name or Source Path, Comment 
			case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID:	// Source Path, OSS Name, OSS version, License, Download location, Homepage, Copyright, Exclude 가 동일한 경우 Merge > Binary Name, Comment
			case CoConstDef.CD_DTL_COMPONENT_ID_BOM:		// Merge 없음
				downloadFileId = makeYamlIdentification(dataStr, type);
				
				break;
			case CoConstDef.CD_DTL_COMPONENT_PARTNER:		// OSS Name, OSS version, License, Download location, Homepage, Copyright,Exclude 가 동일한 경우 Merge. > Source Name or Path, Binary Name, Binary Name or Source Path, Comment 
				downloadFileId = makeYamlPartner(dataStr, type);
				
				break;
			default:
				// self-check								// OSS Name, OSS version, License, Download location, Copyright,Exclude 가 동일한 경우 Merge. > Binary Name or Source Path
				downloadFileId = makeYamlSelfCheck(dataStr, type);
				break;
		}
		
		return downloadFileId;
	}
	
	private static String makeYamlIdentification(String dataStr, String type) throws Exception {
		Type projectType = new TypeToken<Project>(){}.getType();
		Project projectBean = (Project) fromJson(dataStr, projectType);
		
		ProjectIdentification _param = new ProjectIdentification();
		_param.setReferenceDiv(type);
		_param.setReferenceId(projectBean.getPrjId());
		
		if(CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type)) {
			_param.setMerge(CoConstDef.FLAG_NO);
		}
		
		Map<String, Object> map = projectService.getIdentificationGridList(_param, true);
		
		List<ProjectIdentification> list = (List<ProjectIdentification>) map.get(CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type) ? "rows" : "mainData");
		String jsonStr = "";
		
		if(list != null) {
			if(CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type)) {
				jsonStr = toJson(checkYamlFormat(projectService.setMergeGridData(list), type));
			} else {
				jsonStr = toJson(checkYamlFormat(setMergeData(list, type)));
			}
		}
		
		// fosslight_sbom_info_[date]_prj-[ID].yaml
		String fileName = "fosslight_sbom_info_" + CommonFunction.getCurrentDateTime() + "_prj-" + projectBean.getPrjId();
		return makeYamlFileId(fileName, convertJSON2YAML(jsonStr));
	}
	
	private static String makeYamlPartner(String dataStr, String typeCode)  throws Exception {
		Type partnerType = new TypeToken<PartnerMaster>(){}.getType();
		PartnerMaster partnerBean = (PartnerMaster) fromJson(dataStr, partnerType);
		
		// partner > OSS List
		ProjectIdentification _param = new ProjectIdentification();
		_param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
		_param.setReferenceId(partnerBean.getPartnerId());
		
		Map<String, Object> map = projectService.getIdentificationGridList(_param, true);
		
		List<ProjectIdentification> list = new ArrayList<>();
		list = (List<ProjectIdentification>) map.get("mainData");
		
		String jsonStr = "";
		if(list != null) {
			jsonStr = toJson(checkYamlFormat(setMergeData(list, typeCode)));
		}
		
		// fosslight_sbom_info_[date]_3rd-[ID].yaml
		String fileName = "fosslight_sbom_info_" + CommonFunction.getCurrentDateTime() + "_3rd-" + partnerBean.getPartnerId();
		return makeYamlFileId(fileName, convertJSON2YAML(jsonStr));
	}
	
	private static String makeYamlSelfCheck(String dataStr, String typeCode) throws Exception {
		Type projectType = new TypeToken<Project>(){}.getType();
		Project projectBean = (Project) fromJson(dataStr, projectType);
		
		// partner > OSS List
		ProjectIdentification _param = new ProjectIdentification();
		_param.setReferenceDiv("10");
		_param.setReferenceId(projectBean.getPrjId());
		
		Map<String, Object> map = selfCheckService.getIdentificationGridList(_param);
		
		List<ProjectIdentification> list = new ArrayList<>();
		list = (List<ProjectIdentification>) map.get("mainData");
		
		String jsonStr = "";
		if(list != null) {
			jsonStr = toJson(checkYamlFormat(setMergeData(list, typeCode)));
		}
		
		// fosslight_sbom_info_[date]_self-[ID].yaml
		String fileName = "fosslight_sbom_info_" + CommonFunction.getCurrentDateTime() + "_self-" + projectBean.getPrjId();
		return makeYamlFileId(fileName, convertJSON2YAML(jsonStr));
	}
	
	private static LinkedHashMap<String, List<Map<String, Object>>> checkYamlFormat(List<ProjectIdentification> list) {
		return checkYamlFormat(list, "");
	}
	
	public static LinkedHashMap<String, List<Map<String, Object>>> checkYamlFormat(List<ProjectIdentification> list, String typeCode) {
		LinkedHashMap<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
		
		for(ProjectIdentification bean : list) {
			List<Map<String, Object>> ossNameResult = new ArrayList<>();
			LinkedHashMap<String, Object> yamlFormat = new LinkedHashMap<>();
				
			if(!isEmpty(bean.getOssVersion())) {
				yamlFormat.put("version", bean.getOssVersion());
			} else{
				yamlFormat.put("version", "");
			}

			if(isEmpty(typeCode)) {
				if(!isEmpty(bean.getBinaryName())) {
					String binaryNameStr = bean.getBinaryName();
					yamlFormat.put("source name or path", binaryNameStr.contains("\n") ? binaryNameStr.split("\n") : binaryNameStr);
				} else if(!isEmpty(bean.getFilePath())) {
					String filePathStr = bean.getFilePath();
					yamlFormat.put("source name or path", filePathStr.contains("\n") ? filePathStr.split("\n") : filePathStr);
				}
			}

			String licenseNameStr = bean.getLicenseName();
			yamlFormat.put("license",	licenseNameStr.contains(",") ? licenseNameStr.split(",") : licenseNameStr);

			if(!isEmpty(bean.getDownloadLocation())) {
				yamlFormat.put("download location", bean.getDownloadLocation());
			}

			if(!isEmpty(bean.getHomepage())) {
				yamlFormat.put("homepage", 	bean.getHomepage());
			}

			if(!isEmpty(bean.getCopyrightText())) {
				String copyrightStr = bean.getCopyrightText();
				yamlFormat.put("copyright text", copyrightStr.contains("\n") ? Arrays.asList(copyrightStr.split("\n")) : copyrightStr);
			}
			
			if(CoConstDef.FLAG_YES.equals(avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO))) {
				yamlFormat.put("exclude", true);
			}
			
			if(!isEmpty(bean.getComments())) {
				yamlFormat.put("comment", bean.getComments());
			}
			String ossNameStr = bean.getOssName();
			if(ossNameStr.length() == 0) {
				ossNameStr = "-";
			}
			if(result.containsKey(ossNameStr)) {
				ossNameResult = result.get(ossNameStr);
			}
			ossNameResult.add(yamlFormat);
			result.put(ossNameStr, ossNameResult);
		}
		return result;
	}

	public static String convertJSON2YAML(String jsonStr) {
		return convertJSON2YAML(jsonStr, "");
	}
	
	public static String convertJSON2YAML(String jsonStr, String suffix) {
		String yamlStr = "";
		
		try {
			if(!isEmpty(jsonStr)) {
				JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonStr);
		        // save it as YAML
				yamlStr = new YAMLMapper().writeValueAsString(jsonNodeTree);
				yamlStr = yamlStr.replaceAll("---","").trim();
				// 접두사 존재시 추가
				if(!isEmpty(suffix)) {
					yamlStr = suffix + yamlStr;
				}
			}
		} catch (Exception e) {
			yamlStr = "Failure";
		}
		
		return yamlStr;
	}
	
	public static String convertYAML2JSON(String yamlStr) {
		String jsonStr = "";
		
		try {
			if(!isEmpty(yamlStr)) {
			
				ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
			    Object obj = yamlReader.readValue(yamlStr, Object.class);
	
			    ObjectMapper jsonWriter = new ObjectMapper();
			    jsonStr = jsonWriter.writeValueAsString(obj);
			}
		} catch (Exception e) {
			jsonStr = "Failure";
		}
		
		return jsonStr;
	}
	
	private static String makeYamlFileId(String targetName, String yamlStr) throws IOException {
		UUID randomUUID = UUID.randomUUID();
		String fileName = CommonFunction.replaceSlashToUnderline(targetName);
		String logiFileName = fileName + "_" + randomUUID+".yaml";
		String filePath = writepath+"/download/";
		
		FileOutputStream outFile = null;
		
		try {
			if(!Files.exists(Paths.get(filePath))) {
				Files.createDirectories(Paths.get(filePath));
			}
			outFile = new FileOutputStream(filePath + logiFileName);
			FileUtil.writeFile(filePath, logiFileName, yamlStr);
			
			// db 등록
			return fileService.registFileDownload(filePath, fileName + ".yaml", logiFileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if(outFile != null) {
				try {
					outFile.close();
				} catch (Exception e2) {}
			}
		}
		
		return null;
	}
	
	private static List<ProjectIdentification> setMergeData(List<ProjectIdentification> list, String typeCode) {
		List<ProjectIdentification> tempData = new ArrayList<ProjectIdentification>();
		List<ProjectIdentification> resultGridData = new ArrayList<ProjectIdentification>();
		
		String groupColumn = "";
		boolean ossNameEmptyFlag = false;
		
		Collections.sort(list, new Comparator<ProjectIdentification>() {
			@Override
			public int compare(ProjectIdentification before, ProjectIdentification after) {
				String key = makeMergeKey(before, typeCode);
				String key2 = makeMergeKey(after, typeCode);
				
				return key.compareTo(key2);
			}
		});
		
		for(ProjectIdentification info : list) {
			if(isEmpty(groupColumn)) {
				groupColumn = makeMergeKey(info, typeCode);
			}
			
			if("-".equals(groupColumn)) {
				if("NA".equals(info.getLicenseType())) {
					ossNameEmptyFlag = true;
				}
			}
			
			if(groupColumn.equals(makeMergeKey(info, typeCode)) // 같은 groupColumn이면 데이터를 쌓음
					&& !ossNameEmptyFlag) { // 단, OSS Name: - 이면서, License Type: Proprietary이 아닌 경우 Row를 합치지 않음.
				tempData.add(info);
			} else { // 다른 grouping
				setMergeData(tempData, resultGridData);
				groupColumn = makeMergeKey(info, typeCode);
				tempData.clear();
				tempData.add(info);
			}
			
			ossNameEmptyFlag = false; // 초기화
		}
		
		setMergeData(tempData, resultGridData); // bom data의 loop가 끝났지만 tempData에 값이 있다면 해당 값도 merge를 함.
		
		return resultGridData;
	}
	
	public static void setMergeData(List<ProjectIdentification> tempData, List<ProjectIdentification> resultGridData){
		if(tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<ProjectIdentification>() {
				@Override
				public int compare(ProjectIdentification o1, ProjectIdentification o2) {
					if(o1.getLicenseName().length() >= o2.getLicenseName().length()) { // license name이 같으면 bomList조회해온 순서 그대로 유지함. license name이 다르면 순서변경
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			ProjectIdentification rtnBean = null;
			
			for(ProjectIdentification temp : tempData) {
				if(rtnBean == null) {
					rtnBean = temp;
					continue;
				}
				
				String key = temp.getOssName() + "-" + temp.getLicenseType();
				
				if("--NA".equals(key)) {
					if(!rtnBean.getLicenseName().contains(temp.getLicenseName())) {
						resultGridData.add(rtnBean);
						rtnBean = temp;
						continue;
					}
				}
				
				// 동일한 oss name과 version일 경우 license 정보를 중복제거하여 merge 함.
				for(String licenseName : temp.getLicenseName().split(",")) {
					boolean equalFlag = false;
					
					for(String rtnLicenseName : rtnBean.getLicenseName().split(",")) {
						if(rtnLicenseName.equals(licenseName)) {
							equalFlag = true;
							break;
						}
					}
					
					if(!equalFlag) {
						rtnBean.setLicenseName(rtnBean.getLicenseName() + "," + licenseName);
					}
				}
				
				// 3RD Tab, SRC Tab, BIN Tab >  Source Name or Path, Binary Name, Binary Name or Source Path, Comment
				// BINANDROID Tab > Binary Name, Comment
				// SELFCHECK > Binary Name or Source Path
				
				// binaryName > merge & distinct				
				String binaryNameStr = avoidNull(rtnBean.getBinaryName()) + "\n" + avoidNull(temp.getBinaryName());
				rtnBean.setBinaryName(String.join("\n", Arrays.asList(binaryNameStr.split("\n")).stream()
																								.filter(CommonFunction.distinctByKey(p -> p.trim().toUpperCase()))
																								.collect(Collectors.toList())));
				
				// filePath > merge & distinct
				String filePathStr = avoidNull(rtnBean.getFilePath()) + "\n" + avoidNull(temp.getFilePath());
				rtnBean.setFilePath(String.join("\n", Arrays.asList(filePathStr.split("\n")).stream()
																								.filter(CommonFunction.distinctByKey(p -> p.trim().toUpperCase()))
																								.collect(Collectors.toList())));
				
				// comment > merge & distinct
				String commentsStr = avoidNull(rtnBean.getComments()) + "\n" + avoidNull(temp.getComments());
				rtnBean.setComments(String.join("\n", Arrays.asList(commentsStr.split("\n")).stream()
																								.filter(CommonFunction.distinctByKey(p -> p.trim().toUpperCase()))
																								.collect(Collectors.toList())));
			}
			
			resultGridData.add(rtnBean);
		}
	}
	
	private static String makeMergeKey(ProjectIdentification bean, String typeCode) {
		String mergeKey = "";
		
		// 3RD Tab, SRC Tab, BIN Tab > OSS Name, OSS version, License, Download location, Homepage, Copyright,Exclude 가 동일한 경우 Merge.
		// BINANDROID Tab > Source Path, OSS Name, OSS version, License, Download location, Homepage, Copyright, Exclude 가 동일한 경우 Merge.
		// SELFCHECK > OSS Name, OSS version, License, Download location, Copyright,Exclude 가 동일한 경우 Merge.
		switch(typeCode) {
			case CoConstDef.CD_DTL_COMPONENT_PARTNER:
			case CoConstDef.CD_DTL_COMPONENT_ID_SRC:
			case CoConstDef.CD_DTL_COMPONENT_ID_BIN:
				mergeKey = bean.getOssName() + "_" + bean.getOssVersion() + "_" + bean.getLicenseName() + "_" + bean.getDownloadLocation() + "_" + bean.getHomepage() + "_" + bean.getCopyrightText() + "_" + bean.getExcludeYn();
				
				break;
			case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID:
				mergeKey = bean.getBinaryName() + "_" + bean.getOssName() + "_" + bean.getOssVersion() + "_" + bean.getLicenseName() + "_" + bean.getDownloadLocation() + "_" + bean.getHomepage() + "_" + bean.getCopyrightText() + "_" + bean.getExcludeYn();
	
				break;
			default: // selfcheck
				mergeKey = bean.getOssName() + "_" + bean.getOssVersion() + "_" + bean.getLicenseName() + "_" + bean.getDownloadLocation() + "_" + bean.getCopyrightText() + "_" + bean.getExcludeYn();
				
				break;
		}
		
		return mergeKey;
	}
}
