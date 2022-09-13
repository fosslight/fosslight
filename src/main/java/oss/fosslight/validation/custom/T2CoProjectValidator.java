/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation.custom;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidator;

@Slf4j
public class T2CoProjectValidator extends T2CoValidator {
	private ProjectService projectService = (ProjectService) getWebappContext().getBean(ProjectService.class);
	private T2UserService userService = (T2UserService) getWebappContext().getBean(T2UserService.class);
	private List<ProjectIdentification> ossComponetList = null;
	private List<List<ProjectIdentification>> ossComponentLicenseList = null;
	private Map<String, List<ProjectIdentification>> ossComponentLicenseListMap = null;
	@SuppressWarnings("unused")
	private List<String> noticeBinaryList = null;
	private List<String> existsResultBinaryNameList = null;

	// validation check level (일반사용자 or Admin)
	private boolean checkForAdmin = false;
	// 처리 구분(화면별)
	private String PROC_TYPE = null;
	private int LEVEL = -1;
	private boolean ignoreExcludeDataFlag = false;
	private String projectId = null;

	public final String PROC_TYPE_BASICINFO = "BASIC";
	public final String PROC_TYPE_IDENTIFICATION_PARTNER = "PARTNER";
	public final String PROC_TYPE_IDENTIFICATION_SOURCE = "SRC";
	public final String PROC_TYPE_IDENTIFICATION_BAT = "BAT";
	public final String PROC_TYPE_IDENTIFICATION_BIN = "BIN";
	public final String PROC_TYPE_IDENTIFICATION_ANDROID = "ANDROID";
	public final String PROC_TYPE_IDENTIFICATION_BOM = "BOM";
	public final String PROC_TYPE_IDENTIFICATION_BOM_MERGE = "BOM_MERGE";
	public final String PROC_TYPE_PACKAGING = "PACKAGING";
	public final String PROC_TYPE_DISTRBUTE = "DISTRIBUTE";
	public final String PROC_TYPE_VERIFIY = "VERIFY";

	public final int VALID_LEVEL_BASIC = 1;
	public final int VALID_LEVEL_REQUEST = 2;
	
	@Override
	protected void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		// 기본적인 유효성 체크만 필요한 경우
		if (VALID_LEVEL_BASIC == LEVEL) {
			if (!isEmpty(PROC_TYPE)) {
				validateIdentificationBasic(map, errMap);
			}
		} else if (VALID_LEVEL_REQUEST == LEVEL) {
			if (!isEmpty(PROC_TYPE)) {
				if(PROC_TYPE_IDENTIFICATION_PARTNER.equals(PROC_TYPE)) {
					validatePartnerRequest(map, errMap, diffMap);
				} else {
					validateIdentificationRequest(map, errMap, diffMap);
				}
			}
		} else {
			if (!isEmpty(PROC_TYPE)) {
				switch (PROC_TYPE) {
					case PROC_TYPE_BASICINFO: // 프로젝트 기본정보 validation
						validateProjectBasicInfo(map, errMap);
						
						break;
					case PROC_TYPE_IDENTIFICATION_PARTNER: // 프로젝트 Identification
															// 3rd용 유효성 체크
						validateProjectPartner(map, errMap, diffMap, infoMap);
						
						break;
					case PROC_TYPE_IDENTIFICATION_SOURCE: // 프로젝트 Identification // src용 유효성 체크
					case PROC_TYPE_IDENTIFICATION_BIN: // 프로젝트 Identification src용 // 유효성 체크
					case PROC_TYPE_IDENTIFICATION_ANDROID: // 프로젝트 Identification // src용 유효성 체크
						validateProjectSrc(map, errMap, diffMap, infoMap);
						
						break;
					case PROC_TYPE_IDENTIFICATION_BAT: // 프로젝트 Identification bat용 // 유효성 체크
						validateProjectBat(map, errMap, diffMap, infoMap);
						
						break;
					case PROC_TYPE_PACKAGING: // 프로젝트 Identification Packaging용 유효성 // 체크
						validateProjectPackaging(map, errMap);
						
						break;
					case PROC_TYPE_DISTRBUTE: // 프로젝트 Identification Packaging용 유효성 // 체크
						validateProjectDistribute(map, errMap);
						
						break;
					case PROC_TYPE_VERIFIY:
						validateProjectVerify(map, errMap);
						
						break;
					case PROC_TYPE_IDENTIFICATION_BOM_MERGE:
						validateProjectBomMerge(map, errMap, diffMap);
						
						break;
				}
			}
		}
	}
	
	private void validatePartnerRequest(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap) {
		//ossComponetList==> grid 데이터(사용자 등록 데이터)
		if(ossComponetList != null) {
			// validation case에 따라 필요한 정보를 추출한다.
			List<String> ossNameList = new ArrayList<>();
			
			for(ProjectIdentification bean : ossComponetList) {
				if(!isEmpty(bean.getOssName()) && !ossNameList.contains(avoidNull(bean.getOssName()))) {
					ossNameList.add(avoidNull(bean.getOssName()));
				}
			}
			
			Map<String, OssMaster> ossInfoByName = null;
			ossInfoByName = CoCodeManager.OSS_INFO_UPPER;

			for(ProjectIdentification bean : ossComponetList) {
				if(CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				String basicKey = "";
				String gridKey = "";
				
				if("-".equals(bean.getOssName())) {
					// license check
					{
						basicKey = "LICENSE_NAME";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// 기본체크
						String errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());
						
						if(!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getComponentId(), errCd);
						} else if(isEmpty(bean.getLicenseName())) {
							errMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
						} else if(!CoCodeManager.LICENSE_INFO_UPPER.containsKey(bean.getLicenseName().toUpperCase())) {
							diffMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
						}
					}
					
					// FILE PATH
					{
						basicKey = "FILE_PATH";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// FILE_PATH의 경우 basic validator에서 형식, 길이 체크만 한다.
						// basic validator의 체크 순서가 필수부터 체크하기 때문에, 필수체크를 무시하는 파라미터 플래그를 추가
						String errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
						
						if(!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getComponentId(), errCd);
						}
					}
					
					continue;
				}
				
				String checkKey = bean.getOssName().trim() +"_"+ avoidNull(bean.getOssVersion()).trim();
				checkKey = checkKey.toUpperCase();
				OssMaster ossBean = ossInfoByName.get(checkKey);

				List<ProjectIdentification> licenseList = null;
				
				if(ossComponentLicenseListMap != null
						&& ossComponentLicenseListMap.containsKey(bean.getGridId())) {
					licenseList = ossComponentLicenseListMap.get(bean.getGridId());
				}

				basicKey = "OSS_NAME";
				gridKey = StringUtil.convertToCamelCase(basicKey);
				String errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), true);
				
				if(!isEmpty(errCd)) {
					errMap.put(basicKey + "." + bean.getComponentId(), errCd);
				}
				
				if(isEmpty(bean.getOssName()) && !checkNonPermissiveLicense(licenseList)) {
					errMap.put("OSS_NAME."+bean.getComponentId(), "OSS_NAME.REQUIRED");
				}
				
				// oss 등록 여부 체크
				if(!isEmpty(bean.getOssName()) && !diffMap.containsKey("OSS_NAME." + bean.getComponentId())) {
					String licenseText = "";
					
					if(ossBean != null) {
						licenseText = CommonFunction.makeLicenseExpressionMsgType(ossBean.getOssLicenses(), true); // msgType return
					}
					
					if(!ossInfoByName.containsKey(checkKey)) {
						if(checkNonVersionOss(ossInfoByName, bean.getOssName())) {
							// oss는 등록되어 있지만, 해당 version은 없는 경우
							diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
						} else {
							diffMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.UNCONFIRMED");
						}
					}
					// license 등록 여부 (등록되어 있는 오픈소스이나 사용자가 입력한 라이선스는 포함하고 있지 않은 경우 & Detected License도 미 포함인 경우)
					else if(!hasOssLicense2(ossBean, licenseList)) {
						diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
					}
					// Declared License가 미포함인 경우
					else if(!hasOssLicense2(ossBean, licenseList, false)) {
						diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
					}
					//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
					else if(hasOssLicenseTypeProject(ossBean, licenseList)) {
						diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
					}
				}
				
				// 관리되지 않은 라이선스가 포함되어 있는 경우
				if(licenseList != null) {
					for(ProjectIdentification license : licenseList) {
						if(CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
							continue;
						}
						
						if(isEmpty(license.getLicenseName())) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
							break;
						}
						
						if(!CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())
								&& !ossInfoByName.containsKey(checkKey)) {
							if(CommonFunction.isAdmin() && !errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							} else if(!diffMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							}
							break;
						}
					}
				}
				
				if(!errMap.containsKey("LICENSE_NAME." + bean.getComponentId()) && licenseList != null) {
					boolean hasSelected = false;
					
					for(ProjectIdentification license : licenseList) {
						if(!CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
							hasSelected = true;
							
							break;
						}
					}
					
					if(!hasSelected) {
						errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.NOLICENSE");
					}
					else if(licenseList != null // bom merge licese 정보를 이용해서 dual license 중복 여부를 확인한다. // oss list에 등록되어 있고, dual license를 가지는 oss에 대해서만 체크
							&& !CoConstDef.FLAG_YES.equals(bean.getExcludeYn())
							&& ossInfoByName.containsKey(checkKey) 
							&& CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv()) 
							&& CoConstDef.FLAG_YES.equals(ossBean.getDualLicenseFlag()) ) {
						if(checkOROperation(licenseList, ossBean)) {
							errMap.put("LICENSE_NAME."  + bean.getComponentId(), "LICENSE_NAME.INCLUDE_DUAL_OPERATE");
						}
					}
				}
			}
		}
	}

	private boolean checkNonPermissiveLicense(List<ProjectIdentification> licenseList) {
		if(licenseList != null) {
			for (ProjectIdentification license : licenseList) {
				if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					continue;
				}
				
				if (!isEmpty(license.getLicenseName())
						&& CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
					
					if (master != null 
							&& !CoConstDef.FLAG_YES.equals(master.getObligationDisclosingSrcYn())
							&& !CoConstDef.FLAG_YES.equals(master.getObligationNotificationYn())) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	@SuppressWarnings("unchecked")
	private void validateIdentificationRequest(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap) {
		// ossComponetList==> grid 데이터(사용자 등록 데이터)
		if (ossComponetList != null) {
			// validation case에 따라 필요한 정보를 추출한다.
			List<String> ossNameList = new ArrayList<>();
			
			for (ProjectIdentification bean : ossComponetList) {
				if (!isEmpty(bean.getOssName()) && !ossNameList.contains(avoidNull(bean.getOssName()))) {
					ossNameList.add(avoidNull(bean.getOssName()));
				}
			}

			Map<String, OssMaster> ossInfoByName = null;
			ossInfoByName = CoCodeManager.OSS_INFO_UPPER;

			for (ProjectIdentification bean : ossComponetList) {
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				String basicKey = "";
				String gridKey = "";

				if ("-".equals(bean.getOssName())) {
					// license check
					{
						basicKey = "LICENSE_NAME";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// 기본체크
						String errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getComponentId(), errCd);
						} else if (isEmpty(bean.getLicenseName())) {
							errMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
						} else if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(bean.getLicenseName().toUpperCase())) {
							diffMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
						}
					}
					// FILE PATH
					{
						basicKey = "FILE_PATH";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// FILE_PATH의 경우 basic validator에서 형식, 길이 체크만 한다.
						// basic validator의 체크 순서가 필수부터 체크하기 때문에, 필수체크를 무시하는
						// 파라미터 플래그를 추가
						String errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getComponentId(), errCd);
						}
					}
					
					continue;
				}
				
				String checkKey = bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
				checkKey = checkKey.toUpperCase();
				OssMaster ossBean = ossInfoByName.get(checkKey);
				basicKey = "OSS_NAME";
				gridKey = StringUtil.convertToCamelCase(basicKey);
				String errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), CommonFunction.isIgnoreLicense(bean.getLicenseName()));
				
				if (!isEmpty(errCd)) {
					errMap.put(basicKey + "." + bean.getComponentId(), errCd);
				}

				// oss 등록 여부 체크
				if (!CommonFunction.isIgnoreLicense(bean.getLicenseName())) {
					// oss 등록 여부 체크
					 if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
							&& !ossInfoByName.containsKey(checkKey)) {
						if (checkNonVersionOss(ossInfoByName, bean.getOssName())) {
							// oss는 등록되어 있지만, 해당 version은 없는 경우
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							} else {
								diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							}
						} else {
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.UNCONFIRMED");
							} else {
								diffMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.UNCONFIRMED");
							}
						}
					}
					// license 등록 여부 (등록되어 있는 오픈소스이나 사용자가 입력한 라이선스는 포함하고 있지 않은
					// 경우)
					else if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
						String licenseText = "";
						
						if(ossBean != null) {
							licenseText = CommonFunction.makeLicenseExpressionMsgType(ossBean.getOssLicenses(), true); // msgType return
						}
						
						if (bean.getOssComponentsLicenseList() != null
								&& !bean.getOssComponentsLicenseList().isEmpty()) {
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense(ossBean, bean.getOssComponentsLicenseList())) {
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
							// Declared License를 사용하지 않는 case
							else if(!hasOssLicense(ossBean, bean.getOssComponentsLicenseList(), false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeComponents(ossBean, bean.getOssComponentsLicenseList())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						} else if (ossComponentLicenseListMap != null
								&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
							List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
							
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense2(ossBean, licenseList)) {
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							} 
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense2(ossBean, licenseList, false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeProject(ossBean, licenseList)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						}
					}
				} else {
					if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
							&& !ossInfoByName.containsKey(checkKey)) {
						if (checkNonVersionOss(ossInfoByName, bean.getComponentId())) {
							// oss는 등록되어 있지만, 해당 version은 없는 경우
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							} else {
								diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							}
						}
					}
					else if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
							&& ossInfoByName.containsKey(checkKey)
							) {
						String licenseText = "";
						
						if(ossBean != null) {
							licenseText = CommonFunction.makeLicenseExpressionMsgType(ossBean.getOssLicenses(), true); // msgType return
						}
						
						if (bean.getOssComponentsLicenseList() != null
								&& !bean.getOssComponentsLicenseList().isEmpty()) {
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense(ossBean, bean.getOssComponentsLicenseList())) {
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							} 
							// Declared License를 사용하지 않는 case
							else if(!hasOssLicense(ossBean, bean.getOssComponentsLicenseList(), false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeComponents(ossBean, bean.getOssComponentsLicenseList())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						} else if (ossComponentLicenseListMap != null
								&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
							List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
							
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense2(ossBean, licenseList)) {								
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							} 
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense2(ossBean, licenseList, false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeProject(ossBean, licenseList)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						}
					}
				}

				// 관리되지 않은 라이선스가 포함되어 있는 경우
				if (bean.getOssComponentsLicenseList() != null) {
					for (OssComponentsLicense license : bean.getOssComponentsLicenseList()) {
						if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
							continue;
						}
						
						if (isEmpty(license.getLicenseName())) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
							
							break;
						}
						
						if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())
								&& !ossInfoByName.containsKey(checkKey)) {
							if (CommonFunction.isAdmin()
									&& !errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							} else if (!diffMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							}
							
							break;
						}
					}
				}
				
				if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())
						&& bean.getOssComponentsLicenseList() != null) {
					boolean hasSelected = false;
					
					for (OssComponentsLicense license : bean.getOssComponentsLicenseList()) {
						if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
							hasSelected = true;
							break;
						}
					}

					if (!hasSelected) {
						errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.NOLICENSE");
					}
					// bom merge licese 정보를 이용해서 dual license 중복 여부를 확인한다.
					// oss list에 등록되어 있고, dual license를 가지는 oss에 대해서만 체크
					else if (PROC_TYPE_IDENTIFICATION_BOM_MERGE.equals(PROC_TYPE) && !isEmpty(bean.getRefComponentId())
							&& !CoConstDef.FLAG_YES.equals(bean.getExcludeYn()) && ossInfoByName.containsKey(checkKey)
							&& CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())
							&& CoConstDef.FLAG_YES.equals(ossBean.getDualLicenseFlag())) {
						// 참조 대상 source 에서 현재 설정된 정보를 취득한다.
						ProjectIdentification param = new ProjectIdentification();
						param.setComponentId(bean.getRefComponentId());
						Map<String, Object> checkLicenseInfo = projectService.identificationSubGrid(param);
						
						if (checkLicenseInfo != null && checkLicenseInfo.containsKey("rows")
								&& checkOROperation((List<ProjectIdentification>) checkLicenseInfo.get("rows"), ossBean)) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.INCLUDE_DUAL_OPERATE");
						}
					} else if (PROC_TYPE_IDENTIFICATION_PARTNER.equals(PROC_TYPE)
							&& !CoConstDef.FLAG_YES.equals(bean.getExcludeYn()) && ossInfoByName.containsKey(checkKey)
							&& CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv())
							&& CoConstDef.FLAG_YES.equals(ossBean.getDualLicenseFlag())) {
						List<ProjectIdentification> licenseList = findLicense(bean.getGridId());
						if (licenseList != null && !licenseList.isEmpty() && checkOROperation(licenseList, ossBean)) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.INCLUDE_DUAL_OPERATE");
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void validateIdentificationBasic(Map<String, String> map, Map<String, String> errMap) {
		Map<String, OssMaster> ossInfo = null;
		
		// dataMap을 사용하지 않고, request정보를 직접 참조
		if (ossComponetList != null) {
			String basicKey = "";
			String gridKey = "";
			String errKey = "";
			List<ProjectIdentification> licenseList = null;
			// 설정된 oss 정보를 DB에서 취득한다.
			OssMaster ossParam = new OssMaster();
			ossParam.setOssNames(getOssNames());
			
			if (ossParam.getOssNames() != null && ossParam.getOssNames().length > 0) {
				ossInfo = CoCodeManager.OSS_INFO_UPPER;
			}

			if (ossInfo == null) {
				ossInfo = new HashMap<>();
			}

			for (ProjectIdentification bean : ossComponetList) {
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}

				// oss name
				{
					basicKey = "OSS_NAME";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					String errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), true);
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					}
				}
				// oss version
				{
					basicKey = "OSS_VERSION";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					String errCd = checkBasicError(basicKey, gridKey, bean.getOssVersion(), true);
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					}
				}
				// License
				{
					basicKey = "LICENSE_NAME";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					String errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName(), true);
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					}
				}
				// download location
				// homepage

				// V-DIFF 및 편집중인 상태에서 oss 의 라이선스가 변경되어 oss는 multi이나, 라이선스가 하나만
				// 등록되는 현상 대응
				if (!isEmpty(bean.getOssName())) {
					String checkKey = bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
					checkKey = checkKey.toUpperCase();
					licenseList = findLicense(bean.getGridId());
					
					if (licenseList != null && ossInfo.containsKey(checkKey)) {
						OssMaster master = ossInfo.get(checkKey);
						if (CoConstDef.LICENSE_DIV_MULTI.equals(master.getLicenseDiv())) {
							// 멀티 라이선스로 등록된 오픈소스에 싱글 라이선스를 적용한 경우
							if (licenseList == null || licenseList.size() < master.getOssLicenses().size()) {
								// oss master 가 single에서 multi로 변경된 경우,
								// save하면서 multi로 등록한다.
							}
						} else {
							// 싱글 라이선스로 등록된 오픈소스에 멀티 라이선스를 적용한 경우
						}
					} else {
						// 등록되지 않은 오픈소스에 멀티/듀얼 라이선스를 적용한 경우
					}
				}
				
				{
					basicKey = "COPYRIGHT";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					
					try {
						if(!isEmpty(bean.getCopyrightText())) {
							String copyrightText = new String(bean.getCopyrightText().getBytes("UTF-8"), "UTF-8");
							Pattern pattern = Pattern.compile("[\uD83C-\uDBFF\uDC00-\uDFFF]+");
							Matcher matcher = pattern.matcher(copyrightText);
							List<String> matchList = new ArrayList<String>();
							
							while (matcher.find()) {
								String group = matcher.group().replaceAll("-", "");
								
								if(!isEmpty(matcher.group()) && group.length() > 0) {
									matchList.add(group);
								}
							}
							
							if(matchList.size() > 0) {
								String key = isEmpty(bean.getBinaryName()) ? bean.getFilePath() : bean.getBinaryName();
								key += " | " + bean.getOssName() + " | " + bean.getOssVersion( )+ " | " + bean.getLicenseName();
								
								errMap.put(basicKey + "." + key, "COPYRIGHT.INCLUDE_IMOJI");
							}
						}
					} catch (UnsupportedEncodingException e) {
						log.error(e.getMessage());
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateProjectBomMerge(Map<String, String> map, Map<String, String> errMap,
			Map<String, String> diffMap) {
		// ossComponetList==> grid 데이터(사용자 등록 데이터)
		if (ossComponetList != null) {

			// validation case에 따라 필요한 정보를 추출한다.
			List<String> ossNameList = new ArrayList<>();
			
			for (ProjectIdentification bean : ossComponetList) {
				if (!isEmpty(bean.getOssName()) && !ossNameList.contains(avoidNull(bean.getOssName()))) {
					ossNameList.add(avoidNull(bean.getOssName()));
				}
			}

			Map<String, OssMaster> ossInfoByName = null;
			ossInfoByName = CoCodeManager.OSS_INFO_UPPER;

			for (ProjectIdentification bean : ossComponetList) {
				boolean diffMapLicense = false;
				
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				String basicKey = "";
				String gridKey = "";

				if ("-".equals(bean.getOssName())) {
					// license check
					{
						basicKey = "LICENSE_NAME";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// 기본체크
						String errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getComponentId(), errCd);
						} else if (isEmpty(bean.getLicenseName())) {
							errMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
						} else if (!CommonFunction.checkLicense(bean.getLicenseName())) {
							if (CommonFunction.isAdmin()) {
								errMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							} else {
								diffMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
								diffMapLicense = true;
							}
						}
					}
					
					// FILE PATH
					{

						basicKey = "FILE_PATH";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// FILE_PATH의 경우 basic validator에서 형식, 길이 체크만 한다.
						// basic validator의 체크 순서가 필수부터 체크하기 때문에, 필수체크를 무시하는
						// 파라미터 플래그를 추가
						String errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getComponentId(), errCd);
						}
					}
					
					continue;
				}
				
				String checkKey = avoidNull(bean.getOssName(), "").trim() + "_" + avoidNull(bean.getOssVersion(), "").trim();
				checkKey = checkKey.toUpperCase();
				OssMaster checkOSSMaster = ossInfoByName.get(checkKey);
				basicKey = "OSS_NAME";
				gridKey = StringUtil.convertToCamelCase(basicKey);
				String errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), CommonFunction.isIgnoreLicense(bean.getLicenseName()));
				
				if (!isEmpty(errCd)) {
					errMap.put(basicKey + "." + bean.getComponentId(), errCd);
				}

				// oss 등록 여부 체크
				if (!CommonFunction.isIgnoreLicense(bean.getLicenseName())) {
					// oss 등록 여부 체크
					 if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
							&& !ossInfoByName.containsKey(checkKey)) {
						if (checkNonVersionOss(ossInfoByName, bean.getOssName())) {
							// oss는 등록되어 있지만, 해당 version은 없는 경우
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							} else {
								diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							}
						} else {
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.UNCONFIRMED");
							} else {
								diffMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.UNCONFIRMED");
							}
						}
					}
					// license 등록 여부 (등록되어 있는 오픈소스이나 사용자가 입력한 라이선스는 포함하고 있지 않은
					// 경우)
					else if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
						String licenseText = "";
						
						if(checkOSSMaster != null) {
							licenseText = CommonFunction.makeLicenseExpressionMsgType(checkOSSMaster.getOssLicenses(), true); // msgType return
						}
						
						if (bean.getOssComponentsLicenseList() != null
								&& !bean.getOssComponentsLicenseList().isEmpty()
								&& !isEmpty(bean.getLicenseName())) {
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList())) {
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
									diffMapLicense = true;
								}
							} 
							// Declared License를 사용하지 않는 case
							else if(!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList(), false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								diffMapLicense = true;
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeComponents(checkOSSMaster, bean.getOssComponentsLicenseList())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						} else if (ossComponentLicenseListMap != null
								&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
							List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
							
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense2(checkOSSMaster, licenseList)) {						
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
							// Declared License를 사용하지 않는 case
							else if(!hasOssLicense2(checkOSSMaster, licenseList, false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								diffMapLicense = true;
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeProject(checkOSSMaster, licenseList)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						}
					}
				} else {
					if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
							&& !ossInfoByName.containsKey(checkKey)) {
						if (checkNonVersionOss(ossInfoByName, bean.getOssName())) {
							// oss는 등록되어 있지만, 해당 version은 없는 경우
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							} else {
								diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
							}
						}
					}
					else if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
							&& ossInfoByName.containsKey(checkKey)
							) {
						String licenseText = "";
						
						if(checkOSSMaster != null) {
							licenseText = CommonFunction.makeLicenseExpressionMsgType(checkOSSMaster.getOssLicenses(), true); // msgType return
						}
						
						if (bean.getOssComponentsLicenseList() != null
								&& !bean.getOssComponentsLicenseList().isEmpty()) {
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList())) {
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList(), false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								diffMapLicense = true;
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeComponents(checkOSSMaster, bean.getOssComponentsLicenseList())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						} else if (ossComponentLicenseListMap != null
								&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
							List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
							
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense2(checkOSSMaster, licenseList)) {
								if (CommonFunction.isAdmin()) {
									errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								} else {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense2(checkOSSMaster, licenseList, false)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								diffMapLicense = true;
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeProject(checkOSSMaster, licenseList)) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						}
					}
				}

				// 관리되지 않은 라이선스가 포함되어 있는 경우
				if (bean.getOssComponentsLicenseList() != null) {
					for (OssComponentsLicense license : bean.getOssComponentsLicenseList()) {
						if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
							continue;
						}
						
						if (isEmpty(license.getLicenseName())) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
							
							break;
						}
						
						if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())
								&& !ossInfoByName.containsKey(checkKey)) {
							if (CommonFunction.isAdmin()) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							} else {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
								diffMapLicense = true;
							}
							
							break;
						}
					}
				}
				
				if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())
						&& bean.getOssComponentsLicenseList() != null) {
					boolean hasSelected = false;
					
					for (OssComponentsLicense license : bean.getOssComponentsLicenseList()) {
						if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
							hasSelected = true;
							
							break;
						}
					}

					if (!hasSelected) {
						errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.NOLICENSE");
					}
					// bom merge licese 정보를 이용해서 dual license 중복 여부를 확인한다. 
					// oss list에 등록되어 있고, dual license를 가지는 oss에 대해서만 체크
					else if (!isEmpty(bean.getRefComponentId()) && !CoConstDef.FLAG_YES.equals(bean.getExcludeYn())
							&& ossInfoByName.containsKey(checkKey)
							&& CoConstDef.LICENSE_DIV_MULTI.equals(checkOSSMaster.getLicenseDiv())
							&& CoConstDef.FLAG_YES.equals(checkOSSMaster.getDualLicenseFlag())) {
						// 참조 대상 source 에서 현재 설정된 정보를 취득한다.
						ProjectIdentification param = new ProjectIdentification();
						param.setComponentId(bean.getRefComponentId());
						Map<String, Object> checkLicenseInfo = projectService.identificationSubGrid(param);
						
						if (checkLicenseInfo != null && checkLicenseInfo.containsKey("rows")
								&& checkOROperation((List<ProjectIdentification>) checkLicenseInfo.get("rows"), checkOSSMaster)) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.INCLUDE_DUAL_OPERATE");
							
							if(diffMapLicense) { // 일반사용자의 경우 error message 우선순위가 높은 대상들이 diff message로 출력하기 때문에 중복등록 방지를 해야함.
								diffMap.remove("LICENSE_NAME." + bean.getComponentId());
							}
						}
					}
				}

				if (ossInfoByName.containsKey(checkKey)) {
					// oss Download_location 체크
					if (!errMap.containsKey("DOWNLOAD_LOCATION." + bean.getComponentId())
							&& !diffMap.containsKey("DOWNLOAD_LOCATION." + bean.getComponentId())
							&& !isEmpty(bean.getDownloadLocation())) {
						if (checkOssData(checkOSSMaster, bean.getDownloadLocation(), "DOWNLOAD")) {
							diffMap.put("DOWNLOAD_LOCATION." + bean.getComponentId(), "DOWNLOAD_LOCATION.DIFFERENT");
						}
					}

					// oss Homepage 체크
					if (!errMap.containsKey("HOMEPAGE." + bean.getComponentId()) 
							&& !diffMap.containsKey("HOMEPAGE." + bean.getComponentId()) 
							&& !isEmpty(bean.getHomepage())) {
						if (checkOssData(checkOSSMaster, bean.getHomepage(), "HOMEPAGE")) {
							diffMap.put("HOMEPAGE." + bean.getComponentId(), "HOMEPAGE.DIFFERENT");
						}
					}
				}
			}
		}
	}

	private boolean checkOssData(OssMaster ossMaster, String val, String kind) {
		String getData = "";
		String getData2 = "";
		
		// null point exception 발생으로 일단 return 추가
		if (ossMaster == null || isEmpty(val)) {
			return false;
		}

		String[] splitCheckVal = val.split(",");
		
		switch (kind) {
		case "DOWNLOAD":
			getData = ossMaster.getDownloadLocation();
			getData2 = ossMaster.getDownloadLocationGroup();
			if(getData.contains(",") && isEmpty(getData2)) {
				ossMaster.setDownloadLocationGroup(getData);
				getData2 = getData;
			}
			break;
		case "HOMEPAGE":
			getData = ossMaster.getHomepage();
			break;
		case "COPYRIGHT":
			getData = avoidNull(ossMaster.getCopyright(), "").trim();
			break;
		case "LICENSE":
			getData = ossMaster.getOssLicenseText();
			break;
		default:
			break;
		}

		List<String> checkOssNameUrl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_NAME_URL);
		boolean splitFlag = false;
		
		for(String checkVal : splitCheckVal) {
			checkVal = linkPatternCompile(checkOssNameUrl, checkVal);
			splitFlag = checkVal.split("//").length == 2 ? true : false;
			
			if (!isEmpty(getData) && !kind.equals("DOWNLOAD")) {
				if(kind.equals("HOMEPAGE")) {
					if((checkVal.startsWith("http://") || checkVal.startsWith("https://")) && splitFlag) {
						checkVal = checkVal.split("//")[1];
					}
					
					if(checkVal.startsWith("www.")) {
						checkVal = checkVal.substring(5, checkVal.length());
					}
					
					if(getData.contains(";")) {
						getData = getData.split(";")[0];
					}
					
					getData = linkPatternCompile(checkOssNameUrl, getData);
					
					if(getData.startsWith("http://") || getData.startsWith("https://")) {
						getData = getData.split("//")[1];
					}
					
					if(getData.startsWith("www.")) {
						getData = getData.substring(5, getData.length());
					}
				}
				
				if(!getData.toUpperCase().equals(checkVal.toUpperCase())) {
					return true;
				}
			}
			
			if(kind.equals("DOWNLOAD") && !isEmpty(getData2)){
				if((checkVal.startsWith("http://") || checkVal.startsWith("https://")) && splitFlag) {
					checkVal = checkVal.split("//")[1];
				}
				
				if(checkVal.startsWith("www.")) {
					checkVal = checkVal.substring(5, checkVal.length());
				}
				
				boolean chkFlag = false;
				
				for(String downloadLocation : getData2.split(",")){
					downloadLocation = linkPatternCompile(checkOssNameUrl, downloadLocation);
					
					if(downloadLocation.startsWith("http://") || downloadLocation.startsWith("https://")) {
						downloadLocation = downloadLocation.split("//")[1];
					}
					
					if(downloadLocation.startsWith("www.")) {
						downloadLocation = downloadLocation.substring(5, downloadLocation.length());
					}
					
					if(downloadLocation.toUpperCase().equals(checkVal.toUpperCase())) {
						chkFlag = true;
						break;
					}
				}
				
				if(!chkFlag) {
					return true;
				}
			}else if(kind.equals("DOWNLOAD") && !isEmpty(getData) && isEmpty(getData2)){
				if((checkVal.startsWith("http://") || checkVal.startsWith("https://")) && splitFlag) {
					checkVal = checkVal.split("//")[1];
				}
				
				if(checkVal.startsWith("www.")) {
					checkVal = checkVal.substring(5, checkVal.length());
				}
				
				getData = linkPatternCompile(checkOssNameUrl, getData);
				
				if(getData.startsWith("http://") || getData.startsWith("https://")) {
					getData = getData.split("//")[1];
				}
				
				if(getData.startsWith("www.")) {
					getData = getData.substring(5, getData.length());
				}
				
				if(!getData.toUpperCase().equals(checkVal.toUpperCase())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private String linkPatternCompile(List<String> checkOssNameUrl, String checkVal) {
		int urlSearchSeq = -1;
		int seq = 0;
		
		for(String url : checkOssNameUrl) {
			if(urlSearchSeq == -1 && checkVal.contains(url)) {
				urlSearchSeq = seq;
				break;
			}
			seq++;
		}
		
		Pattern p = null;
		
		if(checkVal.startsWith("git://")) {
			checkVal = checkVal.replace("git://", "https://");
		} else if(checkVal.startsWith("ftp://")) {
			checkVal = checkVal.replace("ftp://", "https://");
		} else if(checkVal.startsWith("svn://")) {
			checkVal = checkVal.replace("svn://", "https://");
		} else if(checkVal.startsWith("git@")) {
			checkVal = checkVal.replace("git@", "https://");
		}
		
		if(checkVal.contains(".git")) {
			if(checkVal.endsWith(".git")) {
				checkVal = checkVal.substring(0, checkVal.length()-4);
			} else {
				if(checkVal.contains("#")) {
					checkVal = checkVal.substring(0, checkVal.indexOf("#"));
					checkVal = checkVal.substring(0, checkVal.length()-4);
				}
			}
		}
		
		String[] downloadlocationUrlSplit = checkVal.split("/");
		if(downloadlocationUrlSplit[downloadlocationUrlSplit.length-1].indexOf("#") > -1) {
			checkVal = checkVal.substring(0, checkVal.indexOf("#"));
		}
		
		if( urlSearchSeq > -1 ) {
			switch(urlSearchSeq) {
				case 0: // github
					p = Pattern.compile("((http|https)://github.com/([^/]+)/([^/]+))");
				
					break;
				case 1: // npm
				case 6: // npm
					if(checkVal.contains("/package/@")) {
						p = Pattern.compile("((http|https)://www.npmjs.(org|com)/package/([^/]+)/([^/]+))");
					}else {
						p = Pattern.compile("((http|https)://www.npmjs.(org|com)/package/([^/]+))");
					}
				
					break;
				case 2: // pypi
					p = Pattern.compile("((http|https)://pypi.org/project/([^/]+))");
				
					break;
				case 3: // maven
					p = Pattern.compile("((http|https)://mvnrepository.com/artifact/([^/]+)/([^/]+))");
					break;
				case 4: // pub
					p = Pattern.compile("((http|https)://pub.dev/packages/([^/]+))");
					break;
				case 5: // cocoapods
					p = Pattern.compile("((http|https)://cocoapods.org/pods/([^/]+))");
					break;
				default:
					break;
			}
		
			Matcher m = p.matcher(checkVal);
		
			while(m.find()) {
				checkVal = m.group(0);
			}
		}
		
		return checkVal;
	}

	private boolean hasOssLicense(OssMaster ossMaster, List<OssComponentsLicense> list) {
		return hasOssLicense(ossMaster, list, true);
	}
	
	private boolean hasOssLicense(OssMaster ossMaster, List<OssComponentsLicense> list, boolean detectedLicenseCheck) {
		if (ossMaster == null) {
			return true;
		}
		
		// license nick name을 포함한 라이선스 명 list를 구성
		List<String> checkLicenseNameList = new ArrayList<>(); // declared License
		List<String> detectedLicenseList = ossMaster.getDetectedLicenses(); // detected License
		
		if(detectedLicenseList == null) {
			detectedLicenseList = new ArrayList<>();
		}
		
		for (OssLicense license : ossMaster.getOssLicenses()) {
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
				LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
				checkLicenseNameList.add(_temp.getLicenseName());
				
				if (!isEmpty(_temp.getShortIdentifier())) {
					checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
				}
				
				if(!isEmpty(_temp.getLicenseNameTemp())) {
					checkLicenseNameList.add(_temp.getLicenseNameTemp().toUpperCase());
				}
				
				// nick name이 등록되어 있다면 닉네임도 포함시킨다.
				if (_temp.getLicenseNicknameList() != null) {
					for (String s : _temp.getLicenseNicknameList()) {
						if (!isEmpty(s)) {
							checkLicenseNameList.add(s.toUpperCase());
						}
					}
				}
			}
		}
		
		if(detectedLicenseCheck) {
			if(detectedLicenseList != null) {
				for(String licenseName : detectedLicenseList) {
					if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
						LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
						checkLicenseNameList.add(_temp.getLicenseName().toUpperCase());
						if (!isEmpty(_temp.getShortIdentifier())) {
							checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
						}
						if(!isEmpty(_temp.getLicenseNameTemp())) {
							checkLicenseNameList.add(_temp.getLicenseNameTemp().toUpperCase());
						}
						// nick name이 등록되어 있다면 닉네임도 포함시킨다.
						if (_temp.getLicenseNicknameList() != null) {
							for (String s : _temp.getLicenseNicknameList()) {
								if (!isEmpty(s)) {
									checkLicenseNameList.add(s.toUpperCase());
								}
							}
						}
					}
				}
			}
		} else {
			detectedLicenseList = detectedLicenseList.stream().map(dl -> dl.toUpperCase()).collect(Collectors.toList());
		}
		
		boolean declaredLicenseEmptyCheck = true;
		
		for (OssComponentsLicense license : list) {
			// 포함되어 있지 않은 라이선스가 하나라도 존재한다면 false
			String licenseName = avoidNull(license.getLicenseName()).trim().toUpperCase();
			
			if(!detectedLicenseCheck && detectedLicenseList.contains(licenseName)) {
				continue;
			}
			
			if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())
				&& !checkLicenseNameList.contains(licenseName)) {
				return false;
			} else {
				declaredLicenseEmptyCheck = false;
			}
		}
		
		if(declaredLicenseEmptyCheck) {
			return false;
		}
		
		return true;
	}
	
	private boolean hasOssLicense(OssMaster ossMaster, String LicenseName, String exclude) {
		return hasOssLicense(ossMaster, LicenseName, exclude, true);
	}
	
	private boolean hasOssLicense(OssMaster ossMaster, String LicenseName, String exclude, boolean detectedLicenseCheck) {
		if (ossMaster == null) {
			return true;
		}
		// license nick name을 포함한 라이선스 명 list를 구성
		List<String> checkLicenseNameList = new ArrayList<>();
		List<String> detectedLicenseList = ossMaster.getDetectedLicenses();
		String[] LicenseNames = LicenseName.split("AND|OR|\\,");
		
		if(detectedLicenseList == null) {
			detectedLicenseList = new ArrayList<>();
		}
		
		for (OssLicense license : ossMaster.getOssLicenses()) {
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
				LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
				checkLicenseNameList.add(_temp.getLicenseName());
				
				if (!isEmpty(_temp.getShortIdentifier())) {
					checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
				}
				
				if(!isEmpty(_temp.getLicenseNameTemp())) {
					checkLicenseNameList.add(_temp.getLicenseNameTemp().toUpperCase());
				}
				
				// nick name이 등록되어 있다면 닉네임도 포함시킨다.
				if (_temp.getLicenseNicknameList() != null) {
					for (String s : _temp.getLicenseNicknameList()) {
						if (!isEmpty(s)) {
							checkLicenseNameList.add(s.toUpperCase());
						}
					}
				}
			}
		}
		if(detectedLicenseCheck) {
			if(detectedLicenseList != null) {
				for(String licenseName : detectedLicenseList) {
					if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
						LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
						checkLicenseNameList.add(_temp.getLicenseName().toUpperCase());
						if (!isEmpty(_temp.getShortIdentifier())) {
							checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
						}
						if(!isEmpty(_temp.getLicenseNameTemp())) {
							checkLicenseNameList.add(_temp.getLicenseNameTemp().toUpperCase());
						}
						// nick name이 등록되어 있다면 닉네임도 포함시킨다.
						if (_temp.getLicenseNicknameList() != null) {
							for (String s : _temp.getLicenseNicknameList()) {
								if (!isEmpty(s)) {
									checkLicenseNameList.add(s.toUpperCase());
								}
							}
						}
					}
				}
			}
		}
		
		// 포함되어 있지 않은 라이선스가 하나라도 존재한다면 false
		for(String LicenseNm : LicenseNames){
			if(!detectedLicenseCheck && detectedLicenseList.contains(LicenseNm)) {
				continue;
			}
			
			if (!CoConstDef.FLAG_YES.equals(exclude)
					&& !checkLicenseNameList.contains(avoidNull(LicenseNm).trim().toUpperCase())) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean hasOssLicense2(OssMaster ossMaster, List<ProjectIdentification> list){
		return hasOssLicense2(ossMaster, list, true);
	}
	
	private boolean hasOssLicense2(OssMaster ossMaster, List<ProjectIdentification> list, boolean detectedLicenseCheck) {
		// license nick name을 포함한 라이선스 명 list를 구성
		List<String> checkLicenseNameList = new ArrayList<>(); // declared License
		List<String> detectedLicenseList = ossMaster.getDetectedLicenses(); // detected License
		
		if(detectedLicenseList == null) {
			detectedLicenseList = new ArrayList<>();
		}
		
		if (ossMaster != null) {
			for (OssLicense license : ossMaster.getOssLicenses()) {
				if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
					LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
					checkLicenseNameList.add(_temp.getLicenseName().toUpperCase());
					
					if (!isEmpty(_temp.getShortIdentifier())) {
						checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
					}
					
					if(!isEmpty(_temp.getLicenseNameTemp())) {
						checkLicenseNameList.add(_temp.getLicenseNameTemp().toUpperCase());
					}
					
					// nick name이 등록되어 있다면 닉네임도 포함시킨다.
					if (_temp.getLicenseNicknameList() != null) {
						for (String s : _temp.getLicenseNicknameList()) {
							if (!isEmpty(s)) {
								checkLicenseNameList.add(s.toUpperCase());
							}
						}
					}
				}
			}
			
			if(detectedLicenseCheck) {
				if(detectedLicenseList != null) {
					for(String licenseName : detectedLicenseList) {
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
							LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
							checkLicenseNameList.add(_temp.getLicenseName().toUpperCase());
							if (!isEmpty(_temp.getShortIdentifier())) {
								checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
							}
							if(!isEmpty(_temp.getLicenseNameTemp())) {
								checkLicenseNameList.add(_temp.getLicenseNameTemp().toUpperCase());
							}
							// nick name이 등록되어 있다면 닉네임도 포함시킨다.
							if (_temp.getLicenseNicknameList() != null) {
								for (String s : _temp.getLicenseNicknameList()) {
									if (!isEmpty(s)) {
										checkLicenseNameList.add(s.toUpperCase());
									}
								}
							}
						}
					}
				}
			} else {
				detectedLicenseList = detectedLicenseList.stream().map(dl -> dl.toUpperCase()).collect(Collectors.toList());
			}
		}

		if (list != null) {
			boolean declaredLicenseEmptyCheck = true; // detected License만 사용할 경우 check
			for (ProjectIdentification license : list) {
				// 포함되어 있지 않은 라이선스가 하나라도 존재한다면 false
				String licenseName = avoidNull(license.getLicenseName()).trim().toUpperCase();
				
				if(!detectedLicenseCheck && detectedLicenseList.contains(licenseName)) {
					continue;
				}
				
				if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())
					&& !checkLicenseNameList.contains(licenseName)) {
					return false;
				} else {
					declaredLicenseEmptyCheck = false;
				}
			}
			
			if(declaredLicenseEmptyCheck) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean checkNonVersionOss(Map<String, OssMaster> ossInfoByName, String s) {
		return CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(s.toUpperCase());
	}

	private void validateProjectVerify(Map<String, String> map, Map<String, String> errMap) {
		// Check, if you apply for exceptional notice file 체크인 경우 basic
		// validator 결과를 무시한다.
		for(String str : map.keySet()){
			if(str.contains("PACKAGING@")){
				if(!errMap.containsKey(str) && isEmpty(map.get(str))) {
					errMap.put(str, str+".REQUIRED");
				}
			}
		}
	}
	
	private void validateProjectDistribute(Map<String, String> map, Map<String, String> errMap) {
		// TODO Auto-generated method stub
	}
	
	private void validateProjectPackaging(Map<String, String> map, Map<String, String> errMap) {
		// TODO Auto-generated method stub
	}
	
	private void validateProjectBat(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		validateProjectGrid(map, errMap, diffMap, infoMap);
	}
	
	private void validateProjectSrc(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		validateProjectGrid(map, errMap, diffMap, infoMap);
	}
	 
	private void validateProjectPartner(Map<String, String> map, Map<String, String> errMap,
			Map<String, String> diffMap, Map<String, String> infoMap) {
		validateProjectGrid(map, errMap, diffMap, infoMap);
	}
	
	private void validateProjectBasicInfo(Map<String, String> map, Map<String, String> errMap) {
		String targetName = "";
		String targetNameSub = "";
		// PRJ_NAME, PRJ_VERSION, OSS_NOTICE_DUE_DATE
		Project prj = new Project();
		String prjId = map.get("PRJ_ID");
		String prjName = map.get("PRJ_NAME");
		String prjVersion = map.get("PRJ_VERSION");
		//String prjDate = map.get("OSS_NOTICE_DUE_DATE");
		
		// -- 프로젝트 기본정보 유효성 체크 start --------------------------------
		// 1. 신규인경우 프로젝트명 유니크 체크 -> 수정인 경우에도 체크
		targetName = "PRJ_NAME";
		
		if (!errMap.containsKey(targetName)) {
			prj.setPrjId(prjId);
			prj.setPrjName(prjName);
			prj.setPrjVersion(prjVersion);
			boolean exist = projectService.existProjectData(prj);
			
			if (exist) {
				errMap.put(targetName, targetName + ".DUPLICATED");
			}
		}

		// 2. oss type
		targetName = "OS_TYPE";
		targetNameSub = "OS_TYPE_ETC";
		
		if (!errMap.containsKey(targetName) && !errMap.containsKey(targetNameSub)) {			
			if (CoConstDef.COMMON_SELECTED_ETC.equals(map.get(targetName)) && isEmpty(map.get(targetNameSub))) {
				errMap.put(targetNameSub, targetNameSub + ".REQUIRED");
			}
		}
		
		// 3. oss Notice
		targetName = "NOTICE_TYPE";
		targetNameSub = "NOTICE_TYPE_ETC";
		
		if (!errMap.containsKey(targetName) && !errMap.containsKey(targetNameSub)) {
			if (CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(map.get(targetName)) && isEmpty(map.get(targetNameSub))) {
				errMap.put(targetNameSub, targetNameSub + ".REQUIRED");
			}
		}
		
		// 4. MODEL INPOMATION CHECK
		targetName = "MODEL_NAME";
		targetNameSub = "CATEGORY";
		
		if (map.containsKey(targetName + ".1")) {
			// 중목체크
			// 동일한 카테고리와 모델이 존재하는지 체크
			List<String> modelKeyList = new ArrayList<>();
			
			for (int i = 1; map.containsKey(targetName + "." + i); i++) {
				String _seqkey = targetName + "." + i;
				String _seqKeySub = targetNameSub + "." + i;
				
				if (!errMap.containsKey(_seqkey) && !errMap.containsKey(_seqKeySub)) {
					String _key = map.get(_seqkey) + "_" + map.get(_seqKeySub);
					
					if (modelKeyList.contains(_key)) {
						errMap.put(_seqkey, targetName + ".DUPLICATED"); // 중목
					} else {
						modelKeyList.add(_key);
					}
				}
			}
		}
		
		// 5. priority
		targetName = "PRIORITY";
		
		if (!errMap.containsKey(targetName)) {
			String priority = CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, map.get(targetName));
			
			if (isEmpty(priority)) {
				errMap.put(targetName, targetName + ".REQUIRED");
			}
		}
		
		if (CommonFunction.isAdmin()) {
			targetName = "CREATOR_NM";
			
			if (map.containsKey(targetName) && !isEmpty(map.get("PRJ_ID")) && !"true".equals(map.get("COPY"))) {
				if (isEmpty(map.get(targetName))) {
					errMap.put(targetName, targetName + ".REQUIRED");
				} else if (!map.get(targetName).equals(map.get("CREATOR"))) {
					// 퇴사자인 경우는 에러 처리하지 않음
					if (!isEmpty(map.get("CREATOR")) && userService.isLeavedMember(map.get("CREATOR"))) {

					} else {
						errMap.put(targetName, targetName + ".NOTFOUND");
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void validateProjectGrid(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {

		Map<String, OssMaster> ossInfo = null;
		
		// dataMap을 사용하지 않고, request정보를 직접 참조
		if (ossComponetList != null) {
			String basicKey = "";
			String gridKey = "";
			String errKey = "";
			List<ProjectIdentification> licenseList = null;

			// 설정된 oss 정보를 DB에서 취득한다.
			OssMaster ossParam = new OssMaster();
			ossParam.setOssNames(getOssNames());
			
			if (ossParam.getOssNames() != null && ossParam.getOssNames().length > 0) {
				ossInfo = CoCodeManager.OSS_INFO_UPPER;
			}

			if (ossInfo == null) {
				ossInfo = new HashMap<>();
			}

			// checkBasicError : REQUIRED, LENGTH, FORMAT 만 체크!
			for (ProjectIdentification bean : ossComponetList) {
				boolean hasError = false;
				boolean hasMultiError = false; // multi license용
				
				String checkKey = bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
				checkKey = checkKey.toUpperCase();
				OssMaster ossmaster = ossInfo.get(checkKey);
				
				// exclude=Y 상태인 경우 체크하지 않음
				if (!ignoreExcludeDataFlag && CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					// exclude 상태에서도 체크 되어야하는 case
					// bin android case only
					if (existsResultBinaryNameList != null && !isEmpty(bean.getBinaryName())) {
						if (existsResultBinaryNameList.contains(bean.getBinaryName())) {
							errMap.put("BINARY_NAME." + bean.getGridId(), "BINARY_NAME.RESULTTXT_EXISTS");
						}
					}

					continue;
				}

				// nullpoint
				bean.setOssName(avoidNull(bean.getOssName()));
				licenseList = findLicense(bean.getGridId());

				// oss 를 설정하지 않고, class 파일을 추가하는 경우, oss Name 에 하이픈을 입력한다.
				// class 파일을 설정한 경우, 라이선스 유무와 파일패스 설정 여부만 확인한다.
				if ("-".equals(avoidNull(bean.getOssName()).trim())) {
					// license check
					{
						basicKey = "LICENSE_NAME";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// 기본체크
						String errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getGridId(), errCd);
						} else if (isEmpty(bean.getLicenseName())) {
							errMap.put(basicKey + "." + bean.getGridId(), "LICENSE_NAME.REQUIRED");
						} else if (!CommonFunction.checkLicense(bean.getLicenseName())) {
							if (CommonFunction.isAdmin()) {
								errMap.put(basicKey + "." + bean.getGridId(), "LICENSE_NAME.UNCONFIRMED");
							} else {
								diffMap.put(basicKey + "." + bean.getGridId(), "LICENSE_NAME.UNCONFIRMED");
							}
						} else if(bean.getComponentLicenseList() != null) {
							if(bean.getComponentLicenseList().size() > 1) {
								errMap.put(basicKey + "." + bean.getGridId(), "LICENSE_NAME.INCLUDE_MULTI_OPERATE");
							}
						}
					}
					
					// FILE PATH
					{

						basicKey = "FILE_PATH";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// FILE_PATH의 경우 basic validator에서 형식, 길이 체크만 한다.
						// basic validator의 체크 순서가 필수부터 체크하기 때문에, 필수체크를 무시하는
						// 파라미터 플래그를 추가
						String errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getGridId(), errCd);
						}
						// OSS가 DB에 존재하고, 선택된 라이선스(멀티인 경우 복수)의 oblication이 소스코드를
						// 공개해야하는 경우, 필수 체크
						else if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(bean.getRefDiv())
								&& isEmpty(bean.getFilePath())) {
							errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".REQUIRED");
						}
					}
					
					// Notice는 OSS와 상관없이, 
					if (PROC_TYPE_IDENTIFICATION_ANDROID.equals(PROC_TYPE)) {
						basicKey = "BINARY_NAME";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						errKey = basicKey + "." + bean.getGridId();
						
						if (!errMap.containsKey(errKey)) {
							// 길이, 형식 체크만 한다.
							String errCd = checkBasicError(basicKey, gridKey, bean.getBinaryName(), true);
							
							if (!isEmpty(errCd)) {
								errMap.put(errKey, errCd);
							} else if (isEmpty(bean.getBinaryName())) {
								errMap.put(errKey, basicKey + ".REQUIRED");
							}

						}
						
						basicKey = "BINARY_NOTICE";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						errKey = basicKey + "." + bean.getGridId();
						// nullpoint 대응
						bean.setBinaryNotice(avoidNull(bean.getBinaryNotice()));
						
						if (!diffMap.containsKey(errKey)) {
							// 길이, 형식 체크만 한다.
							String errCd = checkBasicError(basicKey, gridKey, bean.getBinaryNotice(), true);
							
							if (!isEmpty(errCd)) {
								diffMap.put(errKey, errCd);
							} else if (("ok".equalsIgnoreCase(bean.getBinaryNotice())
									|| "ok(NA)".equalsIgnoreCase(bean.getBinaryNotice()))
									&& !checkUsedPermissive(bean, licenseList)) {
								diffMap.put(errKey, basicKey + ".NOTICE_FIND");
							} else if (("nok".equalsIgnoreCase(bean.getBinaryNotice())
									|| "nok(NA)".equalsIgnoreCase(bean.getBinaryNotice()))
									&& checkUsedPermissive(bean, licenseList)) {
								diffMap.put(errKey, basicKey + ".NOTICE_PERMISSIVE");
							}
						}
					}
					
					continue;
				}

				// 1) oss name
				{
					basicKey = "OSS_NAME";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					// 기본체크
					String errCd = checkBasicError(basicKey, gridKey, bean.getOssName(),
							CommonFunction.isIgnoreLicense(bean.getLicenseName()));
							
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					} else {
						// OSS NAME에 대해서 추가적으로 해야할 것이 있다면
					}
					
					if((isEmpty(bean.getOssName()) || "-".equals(bean.getOssName())) && bean.getComponentLicenseList() != null) {
						if(bean.getComponentLicenseList().size() > 1) {
							basicKey = "LICENSE_NAME";
							gridKey = StringUtil.convertToCamelCase(basicKey);
							
							errMap.put(basicKey + "." + bean.getGridId(), "LICENSE_NAME.INCLUDE_MULTI_OPERATE");
						}
					}
					
					OssMaster ossBean = CoCodeManager.OSS_INFO_UPPER.get(checkKey);
					if(ossBean != null) {
						if(CoConstDef.FLAG_YES.equals(ossBean.getDeactivateFlag())){
							if (CommonFunction.isAdmin()) {
								errMap.put(basicKey + "." + bean.getGridId(), "OSS_NAME.DEACTIVATED");
							} else {
								diffMap.put(basicKey + "." + bean.getGridId(), "OSS_NAME.DEACTIVATED");
							}
						}
					} else {
						OssMaster om = null;
						
						for(String key : CoCodeManager.OSS_INFO_UPPER.keySet()) {
							if(key.contains(bean.getOssName().toUpperCase())) {
								om = CoCodeManager.OSS_INFO_UPPER.get(key);
								break;
							}
						}
						
						if(om != null) {
							if(CoConstDef.FLAG_YES.equals(om.getDeactivateFlag())){
								if (CommonFunction.isAdmin()) {
									errMap.put(basicKey + "." + bean.getGridId(), "OSS_NAME.DEACTIVATED");
								} else {
									diffMap.put(basicKey + "." + bean.getGridId(), "OSS_NAME.DEACTIVATED");
								}
							}
						}
					}
				}

				// 2) OSS VERSION
				{
					basicKey = "OSS_VERSION";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					// 기본체크
					String errCd = checkBasicError(basicKey, gridKey, bean.getOssVersion());
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					} else {
						// OSS VERSION에 대해서 추가적으로 해야할 것이 있다면
					}
				}

				// 3) DOWNLOAD LOCATION
				{
					basicKey = "DOWNLOAD_LOCATION";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					// 기본체크
					String errCd = checkBasicError(basicKey, gridKey, bean.getDownloadLocation());
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					} else {
						// OSS VERSION에 대해서 추가적으로 해야할 것이 있다면
					}
				}
				// 4) HOMEPAGE
				{
					basicKey = "HOMEPAGE";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					// 기본체크
					String errCd = checkBasicError(basicKey, gridKey, bean.getHomepage());
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					} else {
						// OSS VERSION에 대해서 추가적으로 해야할 것이 있다면
					}
				}

				// 이후부터는 license와 관련됨

				if (licenseList == null || licenseList.isEmpty() || licenseList.size() < 2) {
					hasError = false; // 초기화

					// subGrid가 없을 경우, 싱글라이선스로 간주
					{
						basicKey = "LICENSE_NAME";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// 5-1. license name의 basic validator
						// 기본체크
						String errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getGridId(), errCd);
							hasError = true;
						}
					}
				} else {
					// Multi 또는 dual 라이선스의 경우
					hasMultiError = false; // 초기화
					List<ProjectIdentification> unExcludeLicenseList = new ArrayList<>();
					
					for (ProjectIdentification licenseBean : licenseList) {
						hasError = false; // 초기화

						// exclude=Y 상태인 경우 체크하지 않음
						if (!CoConstDef.FLAG_YES.equals(licenseBean.getExcludeYn())) {
							unExcludeLicenseList.add(licenseBean);
							
							{
								basicKey = "LICENSE_NAME";
								gridKey = StringUtil.convertToCamelCase(basicKey);
								// 5-1. license name의 basic validator
								// 기본체크
								String errCd = checkBasicError(basicKey, gridKey, licenseBean.getLicenseName());
								
								if (!isEmpty(errCd)) {
									errMap.put(basicKey + "." + licenseBean.getGridId(), errCd);
									hasMultiError = hasError = true;
								}
							}
						}
					}

					// exclude를 포함하여 체크해야할 validator
					if (!hasMultiError) {
						basicKey = "LICENSE_NAME";
						
						// 모두 exlucde가 체크되어 선택된 라이선스가 없을 경우
						if (unExcludeLicenseList.isEmpty()) {
							// header row의 라이선스에 에러 표시
							errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".NOLICENSE");
						} else if (checkOROperation(licenseList, ossmaster)) {
							// OR 조건이 두개이상 선택된 경우
							errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".INCLUDE_DUAL_OPERATE");
						}
					}
				}

				// 6) path 정보
				{
					basicKey = "FILE_PATH";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					// FILE_PATH의 경우 basic validator에서 형식, 길이 체크만 한다.
					// basic validator의 체크 순서가 필수부터 체크하기 때문에, 필수체크를 무시하는 파라미터
					// 플래그를 추가
					String errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getGridId(), errCd);
					}
					// OSS가 DB에 존재하고, 선택된 라이선스(멀티인 경우 복수)의 oblication이 소스코드를
					// 공개해야하는 경우, 필수 체크
					else if (PROC_TYPE_IDENTIFICATION_ANDROID.equals(PROC_TYPE) && isEmpty(bean.getFilePath())) {
						// 17.02.21 yuns bin(android)의 경우 무조건 필수체크로 변경
						errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".REQUIRED");
					}
					// 170524 add yun SRC의 path 정보에 복수개의 path를 선언하는 경우, 에러는 아니지만
					// 다른 색상으로 표시
					else if (diffMap != null && PROC_TYPE_IDENTIFICATION_SOURCE.equals(PROC_TYPE)
							&& (avoidNull(bean.getFilePath()).indexOf("\r\n") > -1
									|| avoidNull(bean.getFilePath()).indexOf("\n") > -1)) {
						diffMap.put(basicKey + "." + bean.getGridId(), basicKey + ".FORMAT");
					}
				}

				// binary name (Android Model만)
				// TODO Android Model과 일반 Model이 같이 있는 경우, 어떻게 구분할 것인가? 일단
				// binary name과 notice 칼럼으로 판단
				if (PROC_TYPE_IDENTIFICATION_ANDROID.equals(PROC_TYPE)) {
					basicKey = "BINARY_NAME";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					errKey = basicKey + "." + bean.getGridId();
					
					if (!errMap.containsKey(errKey)) {
						// 길이, 형식 체크만 한다.
						String errCd = checkBasicError(basicKey, gridKey, bean.getBinaryName(), true);
						if (!isEmpty(errCd)) {
							errMap.put(errKey, errCd);
						} else if (isEmpty(bean.getBinaryName())) {
							errMap.put(errKey, basicKey + ".REQUIRED");
						}

					}
					
					basicKey = "BINARY_NOTICE";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					errKey = basicKey + "." + bean.getGridId();
					// nullpoint 대응
					bean.setBinaryNotice(avoidNull(bean.getBinaryNotice()));
					
					if (!diffMap.containsKey(errKey)) {
						// 길이, 형식 체크만 한다.
						String errCd = checkBasicError(basicKey, gridKey, bean.getBinaryNotice(), true);
						
						if (!isEmpty(errCd)) {
							diffMap.put(errKey, errCd);
						}
						else if (("ok".equalsIgnoreCase(bean.getBinaryNotice())
								|| "ok(NA)".equalsIgnoreCase(bean.getBinaryNotice()))
								&& !checkUsedPermissive(bean, licenseList)) {
							diffMap.put(errKey, basicKey + ".NOTICE_FIND");
						} else if (("nok".equalsIgnoreCase(bean.getBinaryNotice())
								|| "nok(NA)".equalsIgnoreCase(bean.getBinaryNotice()))
								&& checkUsedPermissive(bean, licenseList)) {
							diffMap.put(errKey, basicKey + ".NOTICE_PERMISSIVE");
						}
					}
				}

				// Admin용 validation
				// admin review시 confirm을 위한 추가 validation
				if (!isEmpty(bean.getLicenseName())) {
					if (bean.getOssComponentsLicenseList() != null) {
						for (OssComponentsLicense license : bean.getOssComponentsLicenseList()) {
							if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
								continue;
							}
							
							String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
							if (!CoCodeManager.LICENSE_INFO_UPPER
									.containsKey(avoidNull(license.getLicenseName()).toUpperCase())
									&& !ossInfo.containsKey(checkKey)) {
								if (CommonFunction.isAdmin()) {
									errMap.put(LICENSE_KEY, "LICENSE_NAME.UNCONFIRMED");
								} else {
									if(!errMap.containsKey(LICENSE_KEY)) {
										diffMap.put(LICENSE_KEY, "LICENSE_NAME.UNCONFIRMED");
									}
								}
								
								break;
							}
						}
					} else if (ossComponentLicenseListMap != null
							&& ossComponentLicenseListMap.containsKey(bean.getGridId())) {
						for (ProjectIdentification license : ossComponentLicenseListMap.get(bean.getGridId())) {
							if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
								continue;
							}
							
							String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
							if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(license.getLicenseName()).toUpperCase())
									&& !ossInfo.containsKey(checkKey)) {
								if (CommonFunction.isAdmin()) {
									errMap.put(LICENSE_KEY, "LICENSE_NAME.UNCONFIRMED");
								} else {
									if(!errMap.containsKey(LICENSE_KEY)) {
										diffMap.put(LICENSE_KEY, "LICENSE_NAME.UNCONFIRMED");
									}
								}
								
								break;
							}
						}
					}
				}
				
				// LGE license인 경우는 OSS name과 version을 입력하지 않아도 된다.
				if (!CommonFunction.isIgnoreLicense(bean.getLicenseName())) {
					// oss 등록 여부 체크
					if (!errMap.containsKey("OSS_NAME." + bean.getGridId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getGridId())
							&& !ossInfo.containsKey(checkKey)) {
						if (checkNonVersionOss(ossInfo, bean.getOssName())) {
							// oss는 등록되어 있지만, 해당 version은 없는 경우
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_VERSION." + bean.getGridId(), "OSS_VERSION.UNCONFIRMED");
							} else {
								diffMap.put("OSS_VERSION." + bean.getGridId(), "OSS_VERSION.UNCONFIRMED");
							}
						} else {
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_NAME." + bean.getGridId(), "OSS_NAME.UNCONFIRMED");
							} else {
								diffMap.put("OSS_NAME." + bean.getGridId(), "OSS_NAME.UNCONFIRMED");
							}
						}
					}
					// license 등록 여부 (등록되어 있는 오픈소스이나 사용자가 입력한 라이선스는 포함하고 있지 않은
					// 경우)
					else if (!errMap.containsKey("OSS_NAME." + bean.getGridId())
							&& !errMap.containsKey("LICENSE_NAME." + bean.getGridId())) {
						String licenseText = "";
						
						if(ossmaster != null) {
							licenseText = CommonFunction.makeLicenseExpressionMsgType(ossmaster.getOssLicenses(), true); // msgType return
						}
						
						if (bean.getOssComponentsLicenseList() != null
								&& !bean.getOssComponentsLicenseList().isEmpty()) {
							String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
							
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList())) {
								if (CommonFunction.isAdmin()) {
									errMap.put(LICENSE_KEY, "Declared : " + licenseText);
								} else {
									if(!errMap.containsKey(LICENSE_KEY)) {
										diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
									}
								}
							}
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList(), false)) {
								if(!errMap.containsKey(LICENSE_KEY)) {
									diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
								}
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeComponents(ossmaster, bean.getOssComponentsLicenseList())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						} else if (ossComponentLicenseListMap != null
								&& ossComponentLicenseListMap.containsKey(bean.getGridId())) {
							List<ProjectIdentification> useLicenseList = ossComponentLicenseListMap.get(bean.getGridId());
							String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
							
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense2(ossmaster, useLicenseList)) {								
								if (CommonFunction.isAdmin()) {
									errMap.put(LICENSE_KEY, "Declared : " + licenseText);
								} else {
									if(!errMap.containsKey(LICENSE_KEY)) {
										diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
									}
								}
							}
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense2(ossmaster, useLicenseList, false)) {
								if(!errMap.containsKey(LICENSE_KEY)) {
									diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
								}
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeProject(ossmaster, useLicenseList)) {
								diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
							}
						} else if (ossComponentLicenseListMap == null) {
							if(PROC_TYPE_IDENTIFICATION_PARTNER.equals(PROC_TYPE)) {
								// Declared & Detected License를 전부 사용하지 않는 case
								if (!hasOssLicense(ossmaster, bean.getLicenseName(), bean.getExcludeYn())) {
									String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
									
									if (CommonFunction.isAdmin()) {
										errMap.put(LICENSE_KEY, "Declared : " + licenseText);
									} else {
										if(!errMap.containsKey(LICENSE_KEY)) {
											diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
										}
									}
								} 
								// Declared License를 사용하지 않는 case
								else if (!hasOssLicense(ossmaster, bean.getLicenseName(), bean.getExcludeYn(), false)) {
									String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
									
									if(!errMap.containsKey(LICENSE_KEY)) {
										diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
									}
								}
								//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
								else if(hasOssLicenseTypeSingle(ossmaster, bean.getLicenseName())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
						}
					}
				} else {
					if (!errMap.containsKey("OSS_NAME." + bean.getGridId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getGridId())
							&& !ossInfo.containsKey(checkKey)) {
						if (checkNonVersionOss(ossInfo, bean.getOssName())) {
							// oss는 등록되어 있지만, 해당 version은 없는 경우
							if (CommonFunction.isAdmin()) {
								errMap.put("OSS_VERSION." + bean.getGridId(), "OSS_VERSION.UNCONFIRMED");
							} else {
								diffMap.put("OSS_VERSION." + bean.getGridId(), "OSS_VERSION.UNCONFIRMED");
							}
						}
					}
					else if (!errMap.containsKey("OSS_NAME." + bean.getGridId())
							&& !errMap.containsKey("OSS_VERSION." + bean.getGridId())
							&& ossInfo.containsKey(checkKey)
							) {
						String licenseText = "";
						
						if(ossmaster != null) {
							licenseText = CommonFunction.makeLicenseExpressionMsgType(ossmaster.getOssLicenses(), true); // msgType return
						}
						
						if (bean.getOssComponentsLicenseList() != null
								&& !bean.getOssComponentsLicenseList().isEmpty()) {
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList())) {
								String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
								
								if (CommonFunction.isAdmin()) {
									errMap.put(LICENSE_KEY, "Declared : " + licenseText);
								} else {
									if(!errMap.containsKey(LICENSE_KEY)) {
										diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
									}
								}
							}
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList(), false)) {
								String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
								
								if(!errMap.containsKey(LICENSE_KEY)) {
									diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
								}
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeComponents(ossmaster, bean.getOssComponentsLicenseList())) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							}
						} else if (ossComponentLicenseListMap != null
								&& ossComponentLicenseListMap.containsKey(bean.getGridId())) {
							List<ProjectIdentification> useLicenseList = ossComponentLicenseListMap.get(bean.getGridId());
							String LICENSE_KEY = "LICENSE_NAME." + bean.getGridId();
							
							// Declared & Detected License를 전부 사용하지 않는 case
							if (!hasOssLicense2(ossmaster, useLicenseList)) {
								if (CommonFunction.isAdmin()) {
									errMap.put(LICENSE_KEY, "Declared : " + licenseText);
								} else {
									if(!errMap.containsKey(LICENSE_KEY)) {
										diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
									}
								}
							} 
							// Declared License를 사용하지 않는 case
							else if (!hasOssLicense2(ossmaster, useLicenseList, false)) {								
								if(!errMap.containsKey(LICENSE_KEY)) {
									diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
								}
							}
							//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
							else if(hasOssLicenseTypeProject(ossmaster, useLicenseList)) {
								diffMap.put(LICENSE_KEY, "Declared : " + licenseText);
							}
						}
					}
				}

				if (ossInfo.containsKey(checkKey)) {
					// oss Download_location 체크
					if (!diffMap.containsKey("DOWNLOAD_LOCATION." + bean.getGridId())
							&& !isEmpty(bean.getDownloadLocation())) {
						if (checkOssData(ossInfo.get(checkKey), bean.getDownloadLocation(), "DOWNLOAD")) {
							diffMap.put("DOWNLOAD_LOCATION." + bean.getGridId(), "DOWNLOAD_LOCATION.DIFFERENT");
						}
					}

					// oss Homepage 체크
					if (!diffMap.containsKey("HOMEPAGE." + bean.getGridId()) && !isEmpty(bean.getHomepage())) {
						if (checkOssData(ossInfo.get(checkKey), bean.getHomepage(), "HOMEPAGE")) {
							diffMap.put("HOMEPAGE." + bean.getGridId(), "HOMEPAGE.DIFFERENT");
						}
					}
				}
				
				// exception 처리
				// LICENSE_NAME.UNCONFIRMED 의 경우, notice warning message 미표시
				if(PROC_TYPE_IDENTIFICATION_ANDROID.equals(PROC_TYPE)) {
					String chkKey1 = "LICENSE_NAME." + bean.getGridId();
					String chkKey2 = "BINARY_NOTICE." + bean.getGridId();
					
					if(errMap.containsKey(chkKey1) && (errMap.containsKey(chkKey2) || diffMap.containsKey(chkKey2))) {
						String _diffCode1 = errMap.get(chkKey1);
						
						if("LICENSE_NAME.UNCONFIRMED".equals(_diffCode1)) {
							if(errMap.containsKey(chkKey2)) {
								errMap.remove(chkKey2);
							}
							
							if(diffMap.containsKey(chkKey2)) {
								diffMap.remove(chkKey2);
							}
						}
					}
					if(diffMap.containsKey(chkKey1) && (errMap.containsKey(chkKey2) || diffMap.containsKey(chkKey2))) {
						String _diffCode1 = diffMap.get(chkKey1);
						
						if("LICENSE_NAME.UNCONFIRMED".equals(_diffCode1)) {
							if(errMap.containsKey(chkKey2)) {
								errMap.remove(chkKey2);
							}
							
							if(diffMap.containsKey(chkKey2)) {
								diffMap.remove(chkKey2);
							}
						}
					}
				}

			} // end of loop
		}
	}

	private String makeBinaryOssName(String ossName, String ossVersion) {
		String rtn = avoidNull(ossName, "-");
		
		if(!isEmpty(ossVersion)) {
			rtn += " " + ossVersion;
		}
		
		return rtn;
	}
	
	private boolean compareLicenseWithLicenseNameSort(ProjectIdentification bean, Map<String, Object> _temp) {
		String selectedLicenses = "";
		
		if (bean.getOssComponentsLicenseList() != null) {
			for (OssComponentsLicense license : bean.getOssComponentsLicenseList()) {
				if (CoConstDef.FLAG_YES.equals(license.getExcludeYn()) || isEmpty(license.getLicenseName())) {
					continue;
				}

				if (!isEmpty(selectedLicenses)) {
					selectedLicenses += ",";
				}
				
				if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().trim().toUpperCase())) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER
							.get(license.getLicenseName().trim().toUpperCase());
					selectedLicenses += avoidNull(master.getShortIdentifier(), master.getLicenseNameTemp());
				} else {
					selectedLicenses += license.getLicenseName();
				}
			}
		} else if (ossComponentLicenseListMap != null && ossComponentLicenseListMap.containsKey(bean.getGridId())) {
			for (ProjectIdentification license : ossComponentLicenseListMap.get(bean.getGridId())) {
				if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					continue;
				}

				if (!isEmpty(selectedLicenses)) {
					selectedLicenses += ",";
				}
				
				if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().trim().toUpperCase())) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER
							.get(license.getLicenseName().trim().toUpperCase());
					selectedLicenses += avoidNull(master.getShortIdentifier(), master.getLicenseNameTemp());
				} else {
					selectedLicenses += license.getLicenseName();
				}
			}
		}

		String regLicenses = "";
		String license = _temp.containsKey("license") ? (String) _temp.get("license") : "";
		
		for (String s : avoidNull(license).split(",")) {
			if (isEmpty(s)) {
				continue;
			}
			
			if (!isEmpty(regLicenses)) {
				regLicenses += ",";
			}
			
			s = s.trim();
			
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(s.toUpperCase())) {
				LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(s.toUpperCase());
				regLicenses += avoidNull(master.getShortIdentifier(), master.getLicenseNameTemp());
			} else {
				regLicenses += s;
			}
		}

		// 콤마구분 형태의 동일한 문자 객체를 sorting 하여 비교 한다.
		List<String> compare1 = Arrays.asList(selectedLicenses.trim().toUpperCase().split(","));
		List<String> compare2 = Arrays.asList(avoidNull(regLicenses).toUpperCase().split(","));
		Collections.sort(compare1);
		Collections.sort(compare2);
		String diff1 = "";
		String diff2 = "";
		
		for (String s : compare1) {
			if (!isEmpty(diff1)) {
				diff1 += ",";
			}
			
			diff1 += s;
		}
		for (String s : compare2) {
			if (!isEmpty(diff2)) {
				diff2 += ",";
			}
			
			diff2 += s;
		}

		return diff1.equalsIgnoreCase(diff2);
	}

	private boolean checkUsedPermissive(ProjectIdentification bean, List<ProjectIdentification> licenseList) {
		if (!isEmpty(bean.getOssName()) && licenseList != null) {
			for (ProjectIdentification license : licenseList) {
				if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					continue;
				}
				
				if (!isEmpty(license.getLicenseName())
						&& CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
					
					if (master != null && (CoConstDef.FLAG_YES.equals(master.getObligationDisclosingSrcYn())
							|| CoConstDef.FLAG_YES.equals(master.getObligationNotificationYn()))) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean checkOROperation(List<ProjectIdentification> licenseList, OssMaster ossInfo) {
		if(ossInfo != null) {
			String licenseGroup = CommonFunction.makeLicenseExpression(ossInfo.getOssLicenses());
//			String[] licenseGroupSplit = licenseGroup.split("OR");
			
			List<String> andCombLicenseList = new ArrayList<>();
			for(OssLicense bean : ossInfo.getOssLicenses()) {
				if(andCombLicenseList.isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
					andCombLicenseList.add(bean.getLicenseName());
					
					continue;
				}
				
				int seq = andCombLicenseList.size()-1;
				String licenseName = andCombLicenseList.get(seq);
				licenseName += " AND " + bean.getLicenseName();
				andCombLicenseList.set(seq, licenseName);
			}
			
			Map<String, Object> result = new HashMap<String, Object>();
			boolean returnFlag = false;
			
			for(ProjectIdentification iden : licenseList) {
				if(!licenseGroup.contains(iden.getLicenseName())) {
					returnFlag = true;
					break;
				}
			}
			
			if(returnFlag || licenseList.size() == 1) { // OSS에 등록된 license를 사용하지 않았거나, license가 1개만 들록된 경우 dual check를 하지 않음.
				return false;
			}
			
			for(String licenseName : andCombLicenseList) {
				for(ProjectIdentification iden : licenseList) {
					if(!licenseName.trim().contains(iden.getLicenseName().trim()) && CoConstDef.FLAG_NO.equals(iden.getExcludeYn())) {
						result.put(licenseName, false);
						break;
					}
				}
			}
			
			return andCombLicenseList.size() == result.size() ? true : false; // group의 size와 존재하지 않은 값 check size가 동일하면 true
		}

		return false;
	}
	
	private boolean hasOssLicenseTypeComponents(OssMaster ossInfo, List<OssComponentsLicense> licenseList) {
		List<String> licenseNameList = licenseList.stream()
													.map(ocl -> CoCodeManager.LICENSE_INFO.containsKey(ocl.getLicenseName())
																		? avoidNull(CoCodeManager.LICENSE_INFO.get(ocl.getLicenseName()).getShortIdentifier()
																				, CoCodeManager.LICENSE_INFO.get(ocl.getLicenseName()).getLicenseName())
																		: ocl.getLicenseName())
													.collect(Collectors.toCollection(ArrayList::new));
		
		return hasOssLicenseType(ossInfo, licenseNameList);
	}
	
	private boolean hasOssLicenseTypeProject(OssMaster ossInfo, List<ProjectIdentification> licenseList) {
		List<String> licenseNameList = licenseList.stream()
													.map(pi -> CoCodeManager.LICENSE_INFO.containsKey(pi.getLicenseName()) 
																		? avoidNull(CoCodeManager.LICENSE_INFO.get(pi.getLicenseName()).getShortIdentifier()
																				, CoCodeManager.LICENSE_INFO.get(pi.getLicenseName()).getLicenseName())
																		: pi.getLicenseName())
													.collect(Collectors.toCollection(ArrayList::new));
		
		return hasOssLicenseType(ossInfo, licenseNameList);
	}
	
	private boolean hasOssLicenseTypeSingle(OssMaster ossInfo, String licenseName) {List<String> licenseNameList = new ArrayList<String>();
		String[] licenseNameSplit = licenseName.split(",");
		for (int i=0; i < licenseNameSplit.length; i++) {
			licenseName = avoidNull(CoCodeManager.LICENSE_INFO.get(licenseNameSplit[i]).getShortIdentifier(), licenseNameSplit[i]);
			licenseNameList.add(licenseName);
		}
		
		return hasOssLicenseType(ossInfo, licenseNameList);
	}
	
	private boolean hasOssLicenseType(OssMaster ossInfo, List<String> licenseNameList) {
		if(ossInfo != null) {
			// License가 permissive로만 구성되어 있는지 check 함.
			List<OssLicense> permissiveCheck = ossInfo.getOssLicenses()
														.stream()
														.filter(ol -> !CoConstDef.CD_LICENSE_TYPE_PMS.equals(ol.getLicenseType()))
														.collect(Collectors.toList());
			
			// 전체가 permissive로 이루어져 있으므로 message를 출력하지 않음.
			if(permissiveCheck.size() == 0) { 
				return false;
			}
			
			
			List<OssLicense> ossLicenses = ossInfo.getOssLicenses().stream().filter(ol -> "OR".equals(ol.getOssLicenseComb())).collect(Collectors.toList());
			
			// Single License이거나 AND로만 구성된 Multi License -> Group을 나눌 필요가 없음.
			if(ossLicenses.size() == 0) {				
				// permissive가 아닌 licenseType이면서 사용자가 입력한 License Name중에 없는 License가 존재할 경우 message를 출력함.
				ossLicenses = ossInfo.getOssLicenses()
											.stream()
											.filter(ol -> !CoConstDef.CD_LICENSE_TYPE_PMS.equals(ol.getLicenseType()) 
															&& !licenseNameList.contains(ol.getLicenseName()))
											.collect(Collectors.toList());
			
				if(ossLicenses.size() > 0) {
					return true;
				}
			} 
			// Multi License(AND, OR 전부 포함한 case) -> Group을 나누어 각각 check를 함.
			else {
				List<List<OssLicense>> groupList = new ArrayList<>();
				List<OssLicense> olList = new ArrayList<>();
				
				// OR Group별로 분리
				for(OssLicense bean : ossInfo.getOssLicenses()) {
					if(olList.isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
						olList = new ArrayList<>();
						olList.add(bean);
						
						groupList.add(olList);
						
						continue;
					}
					
					int seq = groupList.size()-1;
					olList = groupList.get(seq);
					olList.add(bean);
					
					groupList.set(seq, olList);
				}
				boolean errorFlag = true;
				for(List<OssLicense> list : groupList) {
					List<OssLicense> checkList = list.stream().filter(c -> licenseNameList.contains(c.getLicenseName())).collect(Collectors.toList());
					// 현재 그룹 내에 사용된 license name이 존재하는지 check함.
					if(checkList.size() == 0) {
						continue; // 그룹내에 사용한 license name이 없을 경우 continue
					}
					
					list = list.stream()
							   .filter(ol -> !CoConstDef.CD_LICENSE_TYPE_PMS.equals(ol.getLicenseType()) 
									   			&& !licenseNameList.contains(ol.getLicenseName()))
							   .collect(Collectors.toList());
					
					if(list.size() == 0) {
						errorFlag = false;
						break;
					}
				}
				
				if(errorFlag) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private String[] getOssNames() {
		List<String> names = new ArrayList<>();
		
		for (ProjectIdentification bean : ossComponetList) {
			if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn()) || isEmpty(bean.getOssName())
					|| names.contains(bean.getOssName().trim())) {
				continue;
			}
			
			names.add(bean.getOssName().trim());
		}

		return names.toArray(new String[names.size()]);
	}

	private List<ProjectIdentification> findLicense(String gridId) {
		List<ProjectIdentification> licenseList = new ArrayList<>();
		
		if (ossComponentLicenseList != null && !ossComponentLicenseList.isEmpty()) {
			boolean breakFlag = false;
			
			for (List<ProjectIdentification> list : ossComponentLicenseList) {
				for (ProjectIdentification bean : list) {
					String key = gridId + "-";
					
					if (avoidNull(bean.getGridId()).startsWith(key)) {
						licenseList.add(bean);
						breakFlag = true;
					}
				}

				if (breakFlag) {
					break;
				}
			}
		} else if (ossComponentLicenseListMap != null && ossComponentLicenseListMap.containsKey(gridId)) {
			licenseList = ossComponentLicenseListMap.get(gridId);
		}
		
		return licenseList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setAppendix(String key, Object obj) {
		if (!isEmpty(key)) {
			switch (key) {
				case "mainList":
				case "bomList":
					ossComponetList = CommonFunction.replaceOssVersionNA((List<ProjectIdentification>) obj);
					
					break;
				case "subList":
					ossComponentLicenseList = (List<List<ProjectIdentification>>) obj;
					
					break;
				case "subListMap":
					ossComponentLicenseListMap = (Map<String, List<ProjectIdentification>>) obj;
					
					break;
				case "noticeBinaryList":
					noticeBinaryList = (List<String>) obj;
					
					break;
				case "existsResultBinaryName":
					existsResultBinaryNameList = (List<String>) obj;
					
					break;
				case "projectId":
					projectId = (String) obj;
					
					break;
				default:
					break;
			}
		}
	}

	@Override
	protected String treatment(String paramvalue) {
		return paramvalue;
	}
	
	public void setProcType(String type) {
		PROC_TYPE = type;
	}

	public void setValidLevel(int level) {
		LEVEL = level;
	}

	public void setIgnoreExcludeData(boolean ignore) {
		ignoreExcludeDataFlag = ignore;
	}

	public boolean isCheckForAdmin() {
		return checkForAdmin;
	}

	public void setCheckForAdmin(boolean checkForAdmin) {
		this.checkForAdmin = checkForAdmin;
	}

}
