/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.validation.custom;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.BinaryData;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.service.BinaryDataService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidator;

@Slf4j
public class T2CoProjectValidator extends T2CoValidator {
	private final SelfCheckService selfcheckService = getWebappContext().getBean(SelfCheckService.class);
	private final ProjectService projectService = getWebappContext().getBean(ProjectService.class);
	private final OssService ossService = getWebappContext().getBean(OssService.class);
	private final T2UserService userService = getWebappContext().getBean(T2UserService.class);
	private final FileService fileService = getWebappContext().getBean(FileService.class);
	private final BinaryDataService binaryDataService = getWebappContext().getBean(BinaryDataService.class);
	private List<ProjectIdentification> ossComponetList = null;
	private List<List<ProjectIdentification>> ossComponentLicenseList = null;
	private Map<String, List<ProjectIdentification>> ossComponentLicenseListMap = null;
	@SuppressWarnings("unused")
	private List<String> noticeBinaryList = null;
	private List<String> existsResultBinaryNameList = null;
	private List<OssComponents> ossComponetSecurityList = null;

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
	public final String PROC_TYPE_SELFCHECK = "SELF";
	public final String PROC_TYPE_IDENTIFICATION_BOM = "BOM";
	public final String PROC_TYPE_IDENTIFICATION_BOM_MERGE = "BOM_MERGE";
	public final String PROC_TYPE_PACKAGING = "PACKAGING";
	public final String PROC_TYPE_DISTRBUTE = "DISTRIBUTE";
	public final String PROC_TYPE_VERIFIY = "VERIFY";
	public final String PROC_TYPE_SECURITY = "SECURITY";

	public final int VALID_LEVEL_BASIC = 1;
	public final int VALID_LEVEL_REQUEST = 2;
	
	@Override
	protected void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		// 기본적인 유효성 체크만 필요한 경우
		if (VALID_LEVEL_BASIC == LEVEL) {
			if (!isEmpty(PROC_TYPE)) {
				if (PROC_TYPE.equals(PROC_TYPE_SECURITY)) {
					validateSecurityBasic(map, errMap);
				} else {
					validateIdentificationBasic(map, errMap);
				}
			}
		} else if (VALID_LEVEL_REQUEST == LEVEL) {
			if (!isEmpty(PROC_TYPE)) {
				if (PROC_TYPE_IDENTIFICATION_PARTNER.equals(PROC_TYPE)) {
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
						validateProjectBomMerge(map, errMap, diffMap, infoMap);
						
						break;
					case PROC_TYPE_SELFCHECK :
						validateSelfCheck(map, errMap);

						break;
				}
			}
		}
	}
	
	private void validateSecurityBasic(Map<String, String> map, Map<String, String> errMap) {
		if (CollectionUtils.isEmpty(ossComponetSecurityList)) {
			return;
		}
		String basicKey;
		String gridKey;
		String errCd;
		for (OssComponents bean : ossComponetSecurityList) {
			{
				basicKey = "SECURITY_OSS_VERSION";
				gridKey = StringUtil.convertToCamelCase(basicKey);
				errCd = checkBasicError(basicKey, gridKey, bean.getOssVersion(), false);
				if (!isEmpty(errCd)) {
					errMap.put("OSS_VERSION" + "." + bean.getGridId(), errCd);
				}
			}
		}
	}

	private void validatePartnerRequest(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap) {
		//ossComponetList==> grid 데이터(사용자 등록 데이터)
		if (CollectionUtils.isEmpty(ossComponetList)) {
			return;
		}
		boolean isAdmin = CommonFunction.isAdmin();
		// validation case에 따라 필요한 정보를 추출한다.
//		List<String> ossNameList = new ArrayList<>();
//		for (ProjectIdentification bean : ossComponetList) {
//			if (!isEmpty(bean.getOssName()) && !ossNameList.contains(avoidNull(bean.getOssName()))) {
//				ossNameList.add(avoidNull(bean.getOssName()));
//			}
//		}
		Map<String, OssMaster> ossInfoByName = null;
		ossInfoByName = CoCodeManager.OSS_INFO_UPPER;
		String basicKey;
		String gridKey;
		String errCd;
		for (ProjectIdentification bean : ossComponetList) {
			if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
				continue;
			}
			
			
			if ("-".equals(bean.getOssName())) {
				// license check
				{
					basicKey = "LICENSE_NAME";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					// 기본체크
					errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());
					
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
					// basic validator의 체크 순서가 필수부터 체크하기 때문에, 필수체크를 무시하는 파라미터 플래그를 추가
					errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getComponentId(), errCd);
					}
				}
				
				continue;
			}
			
			String checkKey = bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
			checkKey = checkKey.toUpperCase();
			if (!ossInfoByName.containsKey(checkKey) && !isEmpty(bean.getRefOssName())) {
				checkKey = bean.getRefOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
				checkKey = checkKey.toUpperCase();
			}
			OssMaster ossBean = ossInfoByName.get(checkKey);

			List<ProjectIdentification> licenseList = null;
			
			if (ossComponentLicenseListMap != null
					&& ossComponentLicenseListMap.containsKey(bean.getGridId())) {
				licenseList = ossComponentLicenseListMap.get(bean.getGridId());
			}

			basicKey = "OSS_NAME";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), true);
			
			if (!isEmpty(errCd)) {
				errMap.put(basicKey + "." + bean.getComponentId(), errCd);
			}
			
			if (isEmpty(bean.getOssName()) && !checkNonPermissiveLicense(licenseList)) {
				errMap.put("OSS_NAME."+bean.getComponentId(), "OSS_NAME.REQUIRED");
			}
			
			// oss 등록 여부 체크
			if (!isEmpty(bean.getOssName()) && !diffMap.containsKey("OSS_NAME." + bean.getComponentId())) {
				String licenseText = "";
				
				if (ossBean != null) {
					licenseText = CommonFunction.makeLicenseExpressionMsgType(ossBean.getOssLicenses(), true); // msgType return
				}
				
				if (!ossInfoByName.containsKey(checkKey)) {
					if (checkNonVersionOss(ossInfoByName, bean.getOssName())) {
						// oss는 등록되어 있지만, 해당 version은 없는 경우
						diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
					} else {
						diffMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.UNCONFIRMED");
					}
				}
				// license 등록 여부 (등록되어 있는 오픈소스이나 사용자가 입력한 라이선스는 포함하고 있지 않은 경우 & Detected License도 미 포함인 경우)
				else if (!hasOssLicense2(ossBean, licenseList)) {
					diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
				}
				// Declared License가 미포함인 경우
				else if (!hasOssLicense2(ossBean, licenseList, false)) {
					diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
				}
				//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
				else if (hasOssLicenseTypeProject(ossBean, licenseList)) {
					diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
				}
			}
			
			// 관리되지 않은 라이선스가 포함되어 있는 경우
			if (licenseList != null) {
				for (ProjectIdentification license : licenseList) {
					if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
						continue;
					}
					
					if (isEmpty(license.getLicenseName())) {
						errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
						break;
					}
					
					if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())
							&& !ossInfoByName.containsKey(checkKey)) {
						if (isAdmin && !errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
						} else if (!diffMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|LICENSE_NAME.UNCONFIRMED");
						}
						break;
					}
				}
			}
			
			if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId()) && licenseList != null) {
				boolean hasSelected = false;
				
				for (ProjectIdentification license : licenseList) {
					if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
						hasSelected = true;
						
						break;
					}
				}
				
				if (!hasSelected) {
					errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.NOLICENSE");
				}
				else if (licenseList != null // bom merge licese 정보를 이용해서 dual license 중복 여부를 확인한다. // oss list에 등록되어 있고, dual license를 가지는 oss에 대해서만 체크
						&& !CoConstDef.FLAG_YES.equals(bean.getExcludeYn())
						&& ossInfoByName.containsKey(checkKey) 
						&& CoConstDef.LICENSE_DIV_MULTI.equals(ossBean.getLicenseDiv()) 
						&& CoConstDef.FLAG_YES.equals(ossBean.getDualLicenseFlag()) ) {
					if (checkOROperation(licenseList, ossBean)) {
						errMap.put("LICENSE_NAME."  + bean.getComponentId(), "LICENSE_NAME.INCLUDE_DUAL_OPERATE");
					}
				}
			}
		}
	}

	private boolean checkNonPermissiveLicense(List<ProjectIdentification> licenseList) {
		if (licenseList != null) {
			for (ProjectIdentification license : licenseList) {
				if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
					continue;
				}
				
				if (!isEmpty(license.getLicenseName())
						&& CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
					
					if (master != null 
							&& !CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationDisclosingSrcYn()))
							&& !CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationNotificationYn()))) {
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
		if (CollectionUtils.isEmpty(ossComponetList)) {
			return;
		}
		
		boolean isAdmin = CommonFunction.isAdmin();
		// validation case에 따라 필요한 정보를 추출한다.
//		List<String> ossNameList = new ArrayList<>();
//		for (ProjectIdentification bean : ossComponetList) {
//			if (!isEmpty(bean.getOssName()) && !ossNameList.contains(avoidNull(bean.getOssName()))) {
//				ossNameList.add(avoidNull(bean.getOssName()));
//			}
//		}
		Map<String, OssMaster> ossInfoByName = null;
		ossInfoByName = CoCodeManager.OSS_INFO_UPPER;
		String basicKey;
		String gridKey;
		String errCd;
		for (ProjectIdentification bean : ossComponetList) {
			if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
				continue;
			}
			

			if ("-".equals(bean.getOssName())) {
				// license check
				{
					basicKey = "LICENSE_NAME";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					// 기본체크
					errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());
					
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
					errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getComponentId(), errCd);
					}
				}
				
				continue;
			}
			
			String checkKey = bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
			checkKey = checkKey.toUpperCase();
			if (!ossInfoByName.containsKey(checkKey) && !isEmpty(bean.getRefOssName())) {
				checkKey = bean.getRefOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
				checkKey = checkKey.toUpperCase();
			}
			OssMaster ossBean = ossInfoByName.get(checkKey);
			basicKey = "OSS_NAME";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), CommonFunction.isIgnoreLicense(bean.getLicenseName()));
			
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
						if (isAdmin) {
							errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
						} else {
							diffMap.put("OSS_VERSION." + bean.getComponentId(), "errLv|OSS_VERSION.UNCONFIRMED");
						}
					} else {
						if (isAdmin) {
							errMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.UNCONFIRMED");
						} else {
							diffMap.put("OSS_NAME." + bean.getComponentId(), "errLv|OSS_NAME.UNCONFIRMED");
						}
					}
				}
				// license 등록 여부 (등록되어 있는 오픈소스이나 사용자가 입력한 라이선스는 포함하고 있지 않은
				// 경우)
				else if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
						&& !errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
					String licenseText = "";
					
					if (ossBean != null) {
						licenseText = CommonFunction.makeLicenseExpressionMsgType(ossBean.getOssLicenses(), true); // msgType return
					}
					
					if (bean.getOssComponentsLicenseList() != null
							&& !bean.getOssComponentsLicenseList().isEmpty()) {
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense(ossBean, bean.getOssComponentsLicenseList())) {
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
							}
						}
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense(ossBean, bean.getOssComponentsLicenseList(), false)) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeComponents(ossBean, bean.getOssComponentsLicenseList())) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
						}
					} else if (ossComponentLicenseListMap != null
							&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
						List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
						
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense2(ossBean, licenseList)) {
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
							}
						} 
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense2(ossBean, licenseList, false)) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeProject(ossBean, licenseList)) {
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
						if (isAdmin) {
							errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
						} else {
							diffMap.put("OSS_VERSION." + bean.getComponentId(), "errLv|OSS_VERSION.UNCONFIRMED");
						}
					}
				}
				else if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
						&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
						&& ossInfoByName.containsKey(checkKey)
						) {
					String licenseText = "";
					
					if (ossBean != null) {
						licenseText = CommonFunction.makeLicenseExpressionMsgType(ossBean.getOssLicenses(), true); // msgType return
					}
					
					if (bean.getOssComponentsLicenseList() != null
							&& !bean.getOssComponentsLicenseList().isEmpty()) {
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense(ossBean, bean.getOssComponentsLicenseList())) {
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
							}
						} 
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense(ossBean, bean.getOssComponentsLicenseList(), false)) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeComponents(ossBean, bean.getOssComponentsLicenseList())) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
						}
					} else if (ossComponentLicenseListMap != null
							&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
						List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
						
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense2(ossBean, licenseList)) {								
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
							}
						} 
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense2(ossBean, licenseList, false)) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeProject(ossBean, licenseList)) {
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
						if (isAdmin
								&& !errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
						} else if (!diffMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|LICENSE_NAME.UNCONFIRMED");
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
	
	@SuppressWarnings("unused")
	private void validateIdentificationBasic(Map<String, String> map, Map<String, String> errMap) {
		Map<String, OssMaster> ossInfo = null;
		
		// dataMap을 사용하지 않고, request정보를 직접 참조
		if (CollectionUtils.isEmpty(ossComponetList)) {
			return;
		}
		String basicKey;
		String gridKey;
		String errKey;
		String errCd;
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
				errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), true);
				
				if (!isEmpty(errCd)) {
					errMap.put(basicKey + "." + bean.getGridId(), errCd);
				}
			}
			// oss version
			{
				basicKey = "OSS_VERSION";
				gridKey = StringUtil.convertToCamelCase(basicKey);
				errCd = checkBasicError(basicKey, gridKey, bean.getOssVersion(), true);
				
				if (!isEmpty(errCd)) {
					errMap.put(basicKey + "." + bean.getGridId(), errCd);
				}
			}
			// License
			{
				basicKey = "LICENSE_NAME";
				gridKey = StringUtil.convertToCamelCase(basicKey);
				errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName(), true);
				
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
				if (!ossInfo.containsKey(checkKey) && !isEmpty(bean.getRefOssName())) {
					checkKey = bean.getRefOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
					checkKey = checkKey.toUpperCase();
				}
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
					if (!isEmpty(bean.getCopyrightText())) {
						String copyrightText = new String(bean.getCopyrightText().getBytes("UTF-8"), "UTF-8");
						Pattern pattern = Pattern.compile("[\uD83C-\uDBFF\uDC00-\uDFFF]+");
						Matcher matcher = pattern.matcher(copyrightText);
						List<String> matchList = new ArrayList<String>();
						
						while (matcher.find()) {
							String group = matcher.group().replaceAll("-", "");
							
							if (!isEmpty(matcher.group()) && group.length() > 0) {
								matchList.add(group);
							}
						}
						
						if (matchList.size() > 0) {
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
	
	@SuppressWarnings("unchecked")
	private void validateProjectBomMerge(Map<String, String> map, Map<String, String> errMap,
			Map<String, String> diffMap, Map<String, String> infoMap) {
		// ossComponetList==> grid 데이터(사용자 등록 데이터)
		if (CollectionUtils.isEmpty(ossComponetList)) {
			return;
		}
		boolean isAdmin = CommonFunction.isAdmin();
		// validation case에 따라 필요한 정보를 추출한다.
//		List<String> ossNameList = new ArrayList<>();
//		for (ProjectIdentification bean : ossComponetList) {
//			if (!isEmpty(bean.getOssName()) && !ossNameList.contains(avoidNull(bean.getOssName()))) {
//				ossNameList.add(avoidNull(bean.getOssName()));
//			}
//		}
		Map<String, OssMaster> ossInfoByName = null;
		ossInfoByName = CoCodeManager.OSS_INFO_UPPER;
		// check deactivate oss info
		List<String> deactivateOssList = ossService.getDeactivateOssList();
		deactivateOssList.replaceAll(String::toUpperCase);

		String basicKey;
		String gridKey;
		String errCd;
		for (ProjectIdentification bean : ossComponetList) {
			boolean diffMapLicense = false;
			
			if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
				continue;
			}

			if ("-".equals(bean.getOssName())) {
				// license check
				{
					basicKey = "LICENSE_NAME";
					gridKey = StringUtil.convertToCamelCase(basicKey);
					if (bean.getLicenseName().split(",").length > 1) {
						errMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.INCLUDE_MULTI_OPERATE");
					} else {
						// 기본체크
						errCd = checkBasicError(basicKey, gridKey, bean.getLicenseName());

						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getComponentId(), errCd);
						} else if (isEmpty(bean.getLicenseName())) {
							errMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.REQUIRED");
						} else if (!CommonFunction.checkLicense(bean.getLicenseName())) {
							if (isAdmin) {
								errMap.put(basicKey + "." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							} else {
								diffMap.put(basicKey + "." + bean.getComponentId(), "errLv|LICENSE_NAME.UNCONFIRMED");
								diffMapLicense = true;
							}
						} else if (CommonFunction.checkLicense(bean.getLicenseName())) {
							LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER
									.get(bean.getLicenseName().trim().toUpperCase());
							if (master != null && CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationDisclosingSrcYn()))) {
								diffMap.put("OSS_NAME." + bean.getComponentId(), "OSS_NAME.REQUIRED2");
							}
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
					errCd = checkBasicError(basicKey, gridKey, bean.getFilePath(), true);
					
					if (!isEmpty(errCd)) {
						errMap.put(basicKey + "." + bean.getComponentId(), errCd);
					}
				}
				
				continue;
			}
			
			String checkKey = bean.getOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
			checkKey = checkKey.toUpperCase();
			if (!ossInfoByName.containsKey(checkKey) && !isEmpty(bean.getRefOssName())) {
				checkKey = bean.getRefOssName().trim() + "_" + avoidNull(bean.getOssVersion()).trim();
				checkKey = checkKey.toUpperCase();
			}
			OssMaster checkOSSMaster = ossInfoByName.get(checkKey);
			basicKey = "OSS_NAME";
			gridKey = StringUtil.convertToCamelCase(basicKey);
			errCd = checkBasicError(basicKey, gridKey, bean.getOssName(), CommonFunction.isIgnoreLicense(bean.getLicenseName()));
			
			if (!isEmpty(errCd)) {
				errMap.put(basicKey + "." + bean.getComponentId(), errCd);
			}

			if(checkOSSMaster != null) {
				if(CoConstDef.FLAG_YES.equals(checkOSSMaster.getDeactivateFlag())){
					if (isAdmin) {
						errMap.put(basicKey + "." + bean.getComponentId(), "OSS_NAME.DEACTIVATED");
					} else {
						diffMap.put(basicKey + "." + bean.getComponentId(), "OSS_NAME.DEACTIVATED");
					}
				}
			} else {
				boolean deactivateFlag = false;
				
				if(!isEmpty(bean.getOssName())) {
					if(deactivateOssList.contains(bean.getOssName().toUpperCase())) {
						deactivateFlag = true;
					}
					
					if(deactivateFlag) {
						if (isAdmin) {
							errMap.put(basicKey + "." + bean.getComponentId(), "OSS_NAME.DEACTIVATED");
						} else {
							diffMap.put(basicKey + "." + bean.getComponentId(), "OSS_NAME.DEACTIVATED");
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
						if (isAdmin) {
							errMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
						} else {
							diffMap.put("LICENSE_NAME." + bean.getComponentId(), "LICENSE_NAME.UNCONFIRMED");
							diffMapLicense = true;
						}
						
						break;
					}
				}
			}
			
			// oss 등록 여부 체크
			if (!CommonFunction.isIgnoreLicense(bean.getLicenseName())) {
				// oss 등록 여부 체크
				 if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
						&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
						&& !ossInfoByName.containsKey(checkKey)) {
					if (checkNonVersionOss(ossInfoByName, bean.getOssName())) {
						// oss는 등록되어 있지만, 해당 version은 없는 경우
						if (isAdmin) {
							errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
						} else {
							diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
						}
					} else {
						if (isAdmin) {
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
					
					if (checkOSSMaster != null) {
						licenseText = CommonFunction.makeLicenseExpressionMsgType(checkOSSMaster.getOssLicenses(), true); // msgType return
					}
					
					if (bean.getOssComponentsLicenseList() != null
							&& !bean.getOssComponentsLicenseList().isEmpty()
							&& !isEmpty(bean.getLicenseName())) {
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList())) {
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
						} 
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList(), false)) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeComponents(checkOSSMaster, bean.getOssComponentsLicenseList())) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
						}
					} else if (ossComponentLicenseListMap != null
							&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
						List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
						
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense2(checkOSSMaster, licenseList)) {						
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
						}
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense2(checkOSSMaster, licenseList, false)) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
							diffMapLicense = true;
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeProject(checkOSSMaster, licenseList)) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
						}
					}
				}
			} else {
				if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
						&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
						&& !ossInfoByName.containsKey(checkKey)) {
					if (checkNonVersionOss(ossInfoByName, bean.getOssName())) {
						// oss는 등록되어 있지만, 해당 version은 없는 경우
						if (isAdmin) {
							errMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.UNCONFIRMED");
						} else {
							diffMap.put("OSS_VERSION." + bean.getComponentId(), "errLv|OSS_VERSION.UNCONFIRMED");
						}
					}
				}
				else if (!errMap.containsKey("OSS_NAME." + bean.getComponentId())
						&& !errMap.containsKey("OSS_VERSION." + bean.getComponentId())
						&& ossInfoByName.containsKey(checkKey)
						) {
					String licenseText = "";
					
					if (checkOSSMaster != null) {
						licenseText = CommonFunction.makeLicenseExpressionMsgType(checkOSSMaster.getOssLicenses(), true); // msgType return
					}
					
					if (bean.getOssComponentsLicenseList() != null
							&& !bean.getOssComponentsLicenseList().isEmpty()) {
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList())) {
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
						}
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense(checkOSSMaster, bean.getOssComponentsLicenseList(), false)) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
							diffMapLicense = true;
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeComponents(checkOSSMaster, bean.getOssComponentsLicenseList())) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
						}
					} else if (ossComponentLicenseListMap != null
							&& ossComponentLicenseListMap.containsKey(bean.getComponentId())) {
						List<ProjectIdentification> licenseList = ossComponentLicenseListMap.get(bean.getComponentId());
						
						// Declared & Detected License를 전부 사용하지 않는 case
						if (!hasOssLicense2(checkOSSMaster, licenseList)) {
							if (isAdmin) {
								errMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "errLv|Declared : " + licenseText);
									diffMapLicense = true;
								}
							}
						}
						// Declared License를 사용하지 않는 case
						else if (!hasOssLicense2(checkOSSMaster, licenseList, false)) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
							diffMapLicense = true;
						}
						//Declared License 중 Permissive가 아닌 type(Copyleft, weak copyleft, Proprietary, Proprietary Free)의 License가 누락된 경우
						else if (hasOssLicenseTypeProject(checkOSSMaster, licenseList)) {
							if (isAdmin) {
								diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
							} else {
								if (!errMap.containsKey("LICENSE_NAME." + bean.getComponentId())) {
									diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Declared : " + licenseText);
								}
							}
						}
					}
				}
			}

			{ // ADD OSS_VERSION REQUIRED MSG
				if (!isEmpty(bean.getOssName()) 
						&& !bean.getOssName().equals("-") 
						&& isEmpty(bean.getOssVersion())) {
					if (!errMap.containsKey("OSS_VERSION." + bean.getComponentId())) {
						if (ossService.checkOssVersionDiff(bean.getOssName()) > 0) {
							diffMap.put("OSS_VERSION." + bean.getComponentId(), "OSS_VERSION.REQUIRED");
						}
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
						
						if (diffMapLicense) { // 일반사용자의 경우 error message 우선순위가 높은 대상들이 diff message로 출력하기 때문에 중복등록 방지를 해야함.
							diffMap.remove("LICENSE_NAME." + bean.getComponentId());
						}
					}
				}
			}

			// oss Download_location 체크
			if (CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(bean.getOssName().toUpperCase())) {
				String checkOssName = CoCodeManager.OSS_INFO_UPPER_NAMES.get(bean.getOssName().toUpperCase());
				OssMaster ossBean = null;
				
				for (String mapKey : ossInfoByName.keySet()) {
			    	int lastUnderscoreIndex = mapKey.lastIndexOf("_");
			    	if (lastUnderscoreIndex != -1) {
			    		String extractedName = mapKey.substring(0, lastUnderscoreIndex);
			    		if (extractedName.equalsIgnoreCase(checkOssName)) {
			                ossBean = ossInfoByName.get(mapKey);
			                break;
			            }
			    	}
			    }
			    
			    if (!errMap.containsKey("DOWNLOAD_LOCATION." + bean.getComponentId()) && !diffMap.containsKey("DOWNLOAD_LOCATION." + bean.getComponentId()) && !isEmpty(bean.getDownloadLocation())) {
					if (checkOssData(ossBean, bean.getDownloadLocation(), "PURL")) {
						diffMap.put("DOWNLOAD_LOCATION." + bean.getComponentId(), "DOWNLOAD_LOCATION.DIFFERENT");
					}
				}
			}
			
			if (ossInfoByName.containsKey(checkKey)) {
				if (!diffMap.containsKey("LICENSE_NAME." + bean.getComponentId()) && !errMap.containsKey("LICENSE_NAME." + bean.getComponentId()) && !isEmpty(bean.getLicenseName())) {
					String licenseText = CommonFunction.makeRecommendedLicenseString(checkOSSMaster, bean);
					if (!isEmpty(licenseText)) {
						diffMap.put("LICENSE_NAME." + bean.getComponentId(), "Recommended : " + licenseText );
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

		String[] splitCheckVal = null;
		if ("COPYRIGHT".equals(kind)) {
			splitCheckVal = new String[] {val};
		} else {
			splitCheckVal = val.split(",");
		}
		
		switch (kind) {
			case "DOWNLOAD":
				getData = ossMaster.getDownloadLocation();
				getData2 = ossMaster.getDownloadLocationGroup();
				if (getData.contains(",") && isEmpty(getData2)) {
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
			case "PURL":
				getData = ossMaster.getPurl();
			default:
				break;
		}

		List<String> checkOssNameUrl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_NAME_URL);
		OssMaster param = new OssMaster();
		boolean splitFlag = false;
		
		for (String checkVal : splitCheckVal) {
			if ("COPYRIGHT".equals(kind)) {
				if (getData.equalsIgnoreCase(checkVal)) {
					return true;
				}
			} else if ("PURL".equals(kind)) {
				if (!isEmpty(getData)) {
					param.setOssName(ossMaster.getOssName());
					param.setDownloadLocation(checkVal);
					String purlStr = ossService.getPurlByDownloadLocation(param);
					if (!isEmpty(purlStr)) {
						boolean chkFlag = false;
						for (String purl : getData.split(",")) {
							if (purlStr.equalsIgnoreCase(purl.trim())) {
								chkFlag = true;
								break;
							}
						}
						if (!chkFlag) {
							return true;
						}
					}
				}
			} else {
				checkVal = linkPatternCompile(checkOssNameUrl, checkVal);
				splitFlag = checkVal.split("//").length == 2 ? true : false;
				
				if (!isEmpty(getData) && !"DOWNLOAD".equals(kind)) {
					if ("HOMEPAGE".equals(kind)) {
						if ((checkVal.startsWith("http://") || checkVal.startsWith("https://")) && splitFlag) {
							checkVal = checkVal.split("//")[1];
						}
						
						if (checkVal.startsWith("www.")) {
							checkVal = checkVal.substring(5, checkVal.length());
						}
						
						if (getData.contains(";")) {
							getData = getData.split(";")[0];
						}
						
						getData = linkPatternCompile(checkOssNameUrl, getData);
						
						if (getData.startsWith("http://") || getData.startsWith("https://")) {
							getData = getData.split("//")[1];
						}
						
						if (getData.startsWith("www.")) {
							getData = getData.substring(5, getData.length());
						}
					}
					
					if (!getData.equalsIgnoreCase(checkVal)) {
						return true;
					}
				}
				
				if ("DOWNLOAD".equals(kind) && !isEmpty(getData2)){
					if ((checkVal.startsWith("http://") || checkVal.startsWith("https://")) && splitFlag) {
						checkVal = checkVal.split("//")[1];
					}
					
					if (checkVal.startsWith("www.")) {
						checkVal = checkVal.substring(5, checkVal.length());
					}
					
					boolean chkFlag = false;
					
					for (String downloadLocation : getData2.split(",")){
						downloadLocation = linkPatternCompile(checkOssNameUrl, downloadLocation);
						
						if (downloadLocation.startsWith("http://") || downloadLocation.startsWith("https://")) {
							downloadLocation = downloadLocation.split("//")[1];
						}
						
						if (downloadLocation.startsWith("www.")) {
							downloadLocation = downloadLocation.substring(5, downloadLocation.length());
						}
						
						if (downloadLocation.equalsIgnoreCase(checkVal)) {
							chkFlag = true;
							break;
						}
					}
					
					if (!chkFlag) {
						return true;
					}
				} else if ("DOWNLOAD".equals(kind) && !isEmpty(getData) && isEmpty(getData2)){
					if ((checkVal.startsWith("http://") || checkVal.startsWith("https://")) && splitFlag) {
						checkVal = checkVal.split("//")[1];
					}
					
					if (checkVal.startsWith("www.")) {
						checkVal = checkVal.substring(5, checkVal.length());
					}
					
					getData = linkPatternCompile(checkOssNameUrl, getData);
					
					if (getData.startsWith("http://") || getData.startsWith("https://")) {
						getData = getData.split("//")[1];
					}
					
					if (getData.startsWith("www.")) {
						getData = getData.substring(5, getData.length());
					}
					
					if (!getData.equalsIgnoreCase(checkVal)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private String linkPatternCompile(List<String> checkOssNameUrl, String checkVal) {
		int urlSearchSeq = -1;
		int seq = 0;
		
		for (String url : checkOssNameUrl) {
			if (urlSearchSeq == -1 && checkVal.contains(url)) {
				urlSearchSeq = seq;
				break;
			}
			seq++;
		}
		
		Pattern p = null;
		
		if (checkVal.startsWith("git://")) {
			checkVal = checkVal.replace("git://", "https://");
		} else if (checkVal.startsWith("ftp://")) {
			checkVal = checkVal.replace("ftp://", "https://");
		} else if (checkVal.startsWith("svn://")) {
			checkVal = checkVal.replace("svn://", "https://");
		} else if (checkVal.startsWith("git@")) {
			checkVal = checkVal.replace("git@", "https://");
		}
		
		if (checkVal.contains(".git")) {
			if (checkVal.endsWith(".git")) {
				checkVal = checkVal.substring(0, checkVal.length()-4);
			} else {
				if (checkVal.contains("#")) {
					checkVal = checkVal.substring(0, checkVal.indexOf("#"));
					checkVal = checkVal.substring(0, checkVal.length()-4);
				}
			}
		}
		
		String[] downloadlocationUrlSplit = checkVal.split("/");
		if (downloadlocationUrlSplit[downloadlocationUrlSplit.length-1].indexOf("#") > -1) {
			checkVal = checkVal.substring(0, checkVal.indexOf("#"));
		}
		
		if ( urlSearchSeq > -1 ) {
			switch(urlSearchSeq) {
				case 0: // github
					if (checkVal.contains("www.")) {
						checkVal = checkVal.replace("www.", "");
					}
					p = Pattern.compile("((http|https)://github.com/([^/]+)/([^/]+))");
				
					break;
				case 1: // npm
				case 6: // npm
					if (checkVal.contains("/package/@")) {
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
				case 7:
					p = Pattern.compile("((http|https)://android.googlesource.com/platform/(.*))");
					break;
				case 8:
					p = Pattern.compile("((http|https)://www.nuget.org/packages/([^/]+))");
					break;
				case 9:
					p = Pattern.compile("((http|https)://stackoverflow.com/revisions/([^/]+)/([^/]+))");
					break;
				default:
					p = Pattern.compile("(.*)");
					break;
			}
		
			Matcher m = p.matcher(checkVal);
		
			while (m.find()) {
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
		
		if (detectedLicenseList == null) {
			detectedLicenseList = new ArrayList<>();
		}
		
		for (OssLicense license : ossMaster.getOssLicenses()) {
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
				LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
				checkLicenseNameList.add(_temp.getLicenseName());
				
				if (!isEmpty(_temp.getShortIdentifier())) {
					checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
				}
				
				if (!isEmpty(_temp.getLicenseNameTemp())) {
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
		
		if (detectedLicenseCheck) {
			if (detectedLicenseList != null) {
				for (String licenseName : detectedLicenseList) {
					if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
						LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
						checkLicenseNameList.add(_temp.getLicenseName().toUpperCase());
						if (!isEmpty(_temp.getShortIdentifier())) {
							checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
						}
						if (!isEmpty(_temp.getLicenseNameTemp())) {
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
			
			if (!detectedLicenseCheck && detectedLicenseList.contains(licenseName) && !checkLicenseNameList.contains(licenseName)) {
				continue;
			}
			
			if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())
				&& !checkLicenseNameList.contains(licenseName)) {
				return false;
			} else {
				declaredLicenseEmptyCheck = false;
			}
		}
		
		if (declaredLicenseEmptyCheck) {
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
		
		if (detectedLicenseList == null) {
			detectedLicenseList = new ArrayList<>();
		}
		
		for (OssLicense license : ossMaster.getOssLicenses()) {
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(license.getLicenseName().toUpperCase())) {
				LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
				checkLicenseNameList.add(_temp.getLicenseName());
				
				if (!isEmpty(_temp.getShortIdentifier())) {
					checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
				}
				
				if (!isEmpty(_temp.getLicenseNameTemp())) {
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
		if (detectedLicenseCheck) {
			if (detectedLicenseList != null) {
				for (String licenseName : detectedLicenseList) {
					if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
						LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
						checkLicenseNameList.add(_temp.getLicenseName().toUpperCase());
						if (!isEmpty(_temp.getShortIdentifier())) {
							checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
						}
						if (!isEmpty(_temp.getLicenseNameTemp())) {
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
		for (String LicenseNm : LicenseNames){
			if (!detectedLicenseCheck && detectedLicenseList.contains(LicenseNm)) {
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
		List<String> detectedLicenseList = ossMaster != null ? ossMaster.getDetectedLicenses() : null; // detected License
		
		if (detectedLicenseList == null) {
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
					
					if (!isEmpty(_temp.getLicenseNameTemp())) {
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
			
			if (detectedLicenseCheck) {
				if (detectedLicenseList != null) {
					for (String licenseName : detectedLicenseList) {
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
							LicenseMaster _temp = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
							checkLicenseNameList.add(_temp.getLicenseName().toUpperCase());
							if (!isEmpty(_temp.getShortIdentifier())) {
								checkLicenseNameList.add(_temp.getShortIdentifier().toUpperCase());
							}
							if (!isEmpty(_temp.getLicenseNameTemp())) {
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
				
				if (!detectedLicenseCheck && detectedLicenseList.contains(licenseName) && !checkLicenseNameList.contains(licenseName)) {
					continue;
				}
				
				if (!CoConstDef.FLAG_YES.equals(license.getExcludeYn())
					&& !checkLicenseNameList.contains(licenseName)) {
					return false;
				} else {
					declaredLicenseEmptyCheck = false;
				}
			}
			
			if (declaredLicenseEmptyCheck) {
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
		for (String str : map.keySet()){
			if (str.contains("PACKAGING@")){
				if (!errMap.containsKey(str) && isEmpty(map.get(str))) {
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

	private void validateSelfCheck(Map<String, String> map, Map<String, String> errMap){
		String targetName = "";
		Project prj = new Project();
		String prjId = map.get("PRJ_ID");
		String prjName = map.get("PRJ_NAME");
		String prjVersion = map.get("PRJ_VERSION");
		targetName = "PRJ_NAME";
		if (!errMap.containsKey(targetName)) {
			prj.setPrjId(prjId);
			prj.setPrjName(prjName);
			prj.setPrjVersion(prjVersion);
			boolean exist = selfcheckService.existProjectData(prj);
			if (exist) {
				errMap.put(targetName, targetName + ".DUPLICATED");
			}
		}
	}
	
	private void validateProjectBasicInfo(Map<String, String> map, Map<String, String> errMap) {
		String targetName = "";
		String targetNameSub = "";
		// PRJ_NAME, PRJ_VERSION, OSS_NOTICE_DUE_DATE
		Project prj = new Project();
		String prjId = map.get("PRJ_ID");
		String prjName = map.get("PRJ_NAME");
		String prjVersion = map.get("PRJ_VERSION");
		String networkServerType = map.get("NETWORK_SERVER_TYPE");
		boolean isAdmin = CommonFunction.isAdmin();
		//String prjDate = map.get("OSS_NOTICE_DUE_DATE");
		String secMailYn = map.get("SEC_MAIL_YN");
		String secMailDec = map.get("SEC_MAIL_DESC");
		
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
		
		// 6. network server type
		targetName = "NETWORK_SERVER_TYPE";
		if (!errMap.containsKey(targetName)) {
			if (isEmpty(networkServerType)) errMap.put(targetName, targetName + ".REQUIRED");
		}

		targetName = "SECMAIL_DESC";
		if(!errMap.containsKey(targetName)) {
			if(isEmpty(secMailDec) && secMailYn.equals("N")) errMap.put(targetName, targetName + ".REQUIRED");
		}
		
		if (isAdmin) {
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
		if (CollectionUtils.isEmpty(ossComponetList)) {
			return;
		}
		boolean isAdmin = CommonFunction.isAdmin() || isCheckForAdmin();
		String basicKey;
		String gridKey;
		String errKey;
		String errCd;
		String LICENSE_KEY;
//		List<ProjectIdentification> licenseList = null;
		// 설정된 oss 정보를 DB에서 취득한다.
		OssMaster ossParam = new OssMaster();
		ossParam.setOssNames(getOssNames());
		if (ossParam.getOssNames() != null && ossParam.getOssNames().length > 0) {
			ossInfo = CoCodeManager.OSS_INFO_UPPER;
		}
		if (ossInfo == null) {
			ossInfo = new HashMap<>();
		}
		// check deactivate oss info
		List<String> deactivateOssList = ossService.getDeactivateOssList();
		deactivateOssList.replaceAll(String::toUpperCase);
		Map<String, String[]> checkSumInfoMap = new HashMap<>();
		
		final boolean isAndroid = PROC_TYPE_IDENTIFICATION_ANDROID.equals(PROC_TYPE);
		final boolean isPartner = PROC_TYPE_IDENTIFICATION_PARTNER.equals(PROC_TYPE);
		final boolean isSource = PROC_TYPE_IDENTIFICATION_SOURCE.equals(PROC_TYPE);

		final Map<String, OssMaster> ossNameOnlyMap = new HashMap<>(ossInfo.size());

		for (Map.Entry<String, OssMaster> entry : ossInfo.entrySet()) {
		    String mapKey = entry.getKey();

		    int idx = mapKey.lastIndexOf('_');
		    if (idx > 0) {
		        String ossName = mapKey.substring(0, idx).toUpperCase();
		        ossNameOnlyMap.putIfAbsent(ossName, entry.getValue());
		    }
		}

		final Map<String, List<ProjectIdentification>> licenseCache = new HashMap<>();
		final Map<String, Boolean> permissiveCache = new HashMap<>();

		final Set<String> deactivateUpperSet = new HashSet<>(deactivateOssList.size());
		for (String deactivate : deactivateOssList) {
		    deactivateUpperSet.add(avoidNull(deactivate).toUpperCase());
		}
		
		// checkBasicError : REQUIRED, LENGTH, FORMAT 만 체크!
		for (ProjectIdentification bean : ossComponetList) {
			// -----------------------------------------------------------------------------
		    // COMMON VALUE CACHE
		    // -----------------------------------------------------------------------------

		    final String gridId = avoidNull(bean.getGridId());
		    final String ossName = avoidNull(bean.getOssName()).trim();
		    final String ossVersion = avoidNull(bean.getOssVersion()).trim();
		    final String ossNameUpper = ossName.toUpperCase();
		    final String ossVersionUpper = ossVersion.toUpperCase();
		    final String binaryName = avoidNull(bean.getBinaryName());
		    final String binaryNotice = avoidNull(bean.getBinaryNotice());
		    final String checkSum = avoidNull(bean.getCheckSum());
		    final String tlsh = avoidNull(bean.getTlsh(), "0");
		    final String filePath = avoidNull(bean.getFilePath());
		    final String licenseName = avoidNull(bean.getLicenseName());
		    final String downloadLocation = avoidNull(bean.getDownloadLocation());
		    final String refOssName = avoidNull(bean.getRefOssName()).trim();
		    final String refOssNameUpper = refOssName.toUpperCase();
		    final String checkKey = ossNameUpper + "_" + ossVersionUpper;
		    final String licenseErrKey = "LICENSE_NAME." + gridId;
			
		    // -----------------------------------------------------------------------------
		    // CHECKSUM
		    // -----------------------------------------------------------------------------

		    if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(bean.getReferenceDiv()) || CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(bean.getReferenceDiv())) {
		        if (!binaryName.isEmpty() && !checkSum.isEmpty()) {
		            checkSumInfoMap.put(binaryName, new String[]{checkSum, tlsh});
		        }
		    } else if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(bean.getReferenceDiv())) {
		        if (!binaryName.isEmpty() && !checkSum.isEmpty() && !tlsh.isEmpty()) {
		            if (!(binaryName.endsWith("/") || binaryName.endsWith("\\"))) {
		                checkSumInfoMap.put(binaryName, new String[]{checkSum, tlsh, filePath});
		            }
		        }
		    }
		    
		    // -----------------------------------------------------------------------------
		    // OSS MASTER
		    // -----------------------------------------------------------------------------

		    OssMaster ossmaster = ossInfo.get(checkKey);
		    if (ossmaster == null && !refOssNameUpper.isEmpty()) {
		        ossmaster = ossInfo.get(refOssNameUpper + "_" + ossVersionUpper);
		    }
		    
		    // -----------------------------------------------------------------------------
		    // EXCLUDE CHECK
		    // -----------------------------------------------------------------------------

		    if (!ignoreExcludeDataFlag && CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
		        if (existsResultBinaryNameList != null && !binaryName.isEmpty() && existsResultBinaryNameList.contains(binaryName)) {
		            errMap.put("BINARY_NAME." + gridId, "BINARY_NAME.RESULTTXT_EXISTS");
		        }
		        continue;
		    }
		    
		    // -----------------------------------------------------------------------------
		    // LICENSE CACHE
		    // -----------------------------------------------------------------------------

		    final List<ProjectIdentification> licenseList = licenseCache.computeIfAbsent(gridId, k -> {
												                List<ProjectIdentification> result = findLicense(k);
												                return result != null ? result : Collections.emptyList();
												            });
		    
		    // -----------------------------------------------------------------------------
		    // "-" OSS
		    // -----------------------------------------------------------------------------

		    if ("-".equals(ossName)) {

		        // -----------------------------------------------------------------------------
		        // LICENSE
		        // -----------------------------------------------------------------------------

		        if (!licenseName.isEmpty() && licenseName.split(",").length > 1) {
		            errMap.put(licenseErrKey, "LICENSE_NAME.INCLUDE_MULTI_OPERATE");
		        } else {
		            errCd = checkBasicError("LICENSE_NAME", "licenseName", licenseName);
		            if (!isEmpty(errCd)) {
		                errMap.put(licenseErrKey, errCd);
		            } else if (isEmpty(licenseName)) {
		                errMap.put(licenseErrKey, "LICENSE_NAME.REQUIRED");
		            } else if (!CommonFunction.checkLicense(licenseName)) {
		                if (isAdmin) {
		                    errMap.put(licenseErrKey, "LICENSE_NAME.UNCONFIRMED");
		                } else {
		                    diffMap.put(licenseErrKey, "errLv|LICENSE_NAME.UNCONFIRMED");
		                }
		            } else if (bean.getComponentLicenseList() != null) {
						if (bean.getComponentLicenseList().size() > 1) {
							errMap.put(licenseErrKey, "LICENSE_NAME.INCLUDE_MULTI_OPERATE");
						}
					} else if (CommonFunction.checkLicense(licenseName)) {
		                LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
		                if (master != null && CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationDisclosingSrcYn()))) {
		                    diffMap.put("OSS_NAME." + gridId, "OSS_NAME.REQUIRED2");
		                }
		            }
		        }

		        // -----------------------------------------------------------------------------
		        // FILE PATH
		        // -----------------------------------------------------------------------------

		        {
		            errCd = checkBasicError("FILE_PATH", "filePath", filePath, true);
		            if (!isEmpty(errCd)) {
		                errMap.put("FILE_PATH." + gridId, errCd);
		            } else if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(bean.getRefDiv()) && filePath.isEmpty()) {
		                errMap.put("FILE_PATH." + gridId, "FILE_PATH.REQUIRED");
		            }
		        }

		        // -----------------------------------------------------------------------------
		        // ANDROID
		        // -----------------------------------------------------------------------------

		        if (isAndroid) {
		            // BINARY NAME
		            {
		            	errKey = "BINARY_NAME." + gridId;
		            	if (!errMap.containsKey(errKey)) {
		            		errCd = checkBasicError("BINARY_NAME", "binaryName", binaryName, true);
			                if (!isEmpty(errCd)) {
			                    errMap.put(errKey, errCd);
			                } else if (binaryName.isEmpty()) {
			                    errMap.put(errKey, "BINARY_NAME.REQUIRED");
			                }
		            	}
		            }

		            // BINARY NOTICE
		            {
		            	errKey = "BINARY_NOTICE." + gridId;
		                errCd = checkBasicError("BINARY_NOTICE", "binaryNotice", binaryNotice, true);
		                bean.setBinaryNotice(binaryNotice);
		                if (!diffMap.containsKey(errKey)) {
		                	String permissiveKey = checkKey + "|" + licenseName;
		                	boolean permissive = permissiveCache.computeIfAbsent(permissiveKey, k -> checkUsedPermissive(bean, licenseList));
			                if (!isEmpty(errCd)) {
			                    diffMap.put("BINARY_NOTICE." + gridId, errCd);
			                } else if (("ok".equalsIgnoreCase(binaryNotice) || "ok(NA)".equalsIgnoreCase(binaryNotice)) && !permissive) {
			                    diffMap.put("BINARY_NOTICE." + gridId, "BINARY_NOTICE.NOTICE_FIND");
			                } else if (("nok".equalsIgnoreCase(binaryNotice) || "nok(NA)".equalsIgnoreCase(binaryNotice)) && permissive) {
			                    diffMap.put("BINARY_NOTICE." + gridId, "BINARY_NOTICE.NOTICE_PERMISSIVE");
			                }
		                }
		            }
		        }

		        continue;
		    }

		    // -----------------------------------------------------------------------------
		    // OSS NAME VALIDATION
		    // -----------------------------------------------------------------------------

		    {
		        errCd = checkBasicError("OSS_NAME", "ossName", ossName, CommonFunction.isIgnoreLicense(licenseName));
		        if (!isEmpty(errCd)) {
		            errMap.put("OSS_NAME." + gridId, errCd);
		        }

		        // multi license with empty or "-"
		        if ((ossName.isEmpty() || "-".equals(ossName)) && bean.getComponentLicenseList() != null && bean.getComponentLicenseList().size() > 1) {
		            errMap.put(licenseErrKey, "LICENSE_NAME.INCLUDE_MULTI_OPERATE");
		        }

		        // deactivate
		        if (ossmaster != null) {
		            if (CoConstDef.FLAG_YES.equals(ossmaster.getDeactivateFlag())) {
		                if (isAdmin) {
		                    errMap.put("OSS_NAME." + gridId, "OSS_NAME.DEACTIVATED");
		                } else {
		                    diffMap.put("OSS_NAME." + gridId, "OSS_NAME.DEACTIVATED");
		                }
		            }
		        } else if (!ossName.isEmpty() && deactivateUpperSet.contains(ossNameUpper)) {
		            if (isAdmin) {
		                errMap.put("OSS_NAME." + gridId, "OSS_NAME.DEACTIVATED");
		            } else {
		                diffMap.put("OSS_NAME." + gridId, "OSS_NAME.DEACTIVATED");
		            }
		        }
		    }
		    
		    // -----------------------------------------------------------------------------
		    // OSS VERSION
		    // -----------------------------------------------------------------------------

		    {
		        errCd = checkBasicError("OSS_VERSION", "ossVersion", ossVersion);
		        if (!isEmpty(errCd)) {
		            errMap.put("OSS_VERSION." + gridId, errCd);
		        }
		    }
		    
		    // -----------------------------------------------------------------------------
		    // DOWNLOAD LOCATION
		    // -----------------------------------------------------------------------------

		    {
		        errCd = checkBasicError("DOWNLOAD_LOCATION", "downloadLocation", downloadLocation);
		        if (!isEmpty(errCd)) {
		            errMap.put("DOWNLOAD_LOCATION." + gridId, errCd);
		        }
		    }
		    
		    // -----------------------------------------------------------------------------
		    // HOMEPAGE
		    // -----------------------------------------------------------------------------

		    {
		        errCd = checkBasicError("HOMEPAGE", "homepage", bean.getHomepage());
		        if (!isEmpty(errCd)) {
		            errMap.put("HOMEPAGE." + gridId, errCd);
		        }
		    }
			
		    // -----------------------------------------------------------------------------
		    // LICENSE VALIDATION
		    // -----------------------------------------------------------------------------

		    if (licenseList.isEmpty() || licenseList.size() < 2) {
		        // -----------------------------------------------------------------------------
		        // SINGLE LICENSE
		        // -----------------------------------------------------------------------------

		        {
		            errCd = checkBasicError("LICENSE_NAME", "licenseName", licenseName);
		            if (!isEmpty(errCd)) {
		                errMap.put(licenseErrKey, errCd);
		            }
		        }
		    } else {
		        // -----------------------------------------------------------------------------
		        // MULTI LICENSE
		        // -----------------------------------------------------------------------------

		        boolean hasMultiError = false;
		        List<ProjectIdentification> unExcludeLicenseList = new ArrayList<>();

		        for (ProjectIdentification licenseBean : licenseList) {
		            if (CoConstDef.FLAG_YES.equals(licenseBean.getExcludeYn())) {
		                continue;
		            }

		            unExcludeLicenseList.add(licenseBean);
		            String subLicenseName = avoidNull(licenseBean.getLicenseName());
		            errCd = checkBasicError("LICENSE_NAME", "licenseName", subLicenseName);

		            if (!isEmpty(errCd)) {
		                errMap.put("LICENSE_NAME." + licenseBean.getGridId(), errCd);
		                hasMultiError = true;
		            }
		        }

		        if (!hasMultiError) {
		            if (unExcludeLicenseList.isEmpty()) {
		                errMap.put(licenseErrKey, "LICENSE_NAME.NOLICENSE");
		            } else if (checkOROperation(licenseList, ossmaster)) {
		                errMap.put(licenseErrKey, "LICENSE_NAME.INCLUDE_DUAL_OPERATE");
		            }
		        }
		    }

			 // -----------------------------------------------------------------------------
			 // LICENSE UNCONFIRMED CHECK
			 // -----------------------------------------------------------------------------
	
			 if (!licenseName.isEmpty()) {
			     if (bean.getOssComponentsLicenseList() != null) {
			         for (OssComponentsLicense license : bean.getOssComponentsLicenseList()) {
			             if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
			                 continue;
			             }
	
			             String upperLicense = avoidNull(license.getLicenseName()).toUpperCase();
			             if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(upperLicense) && !ossInfo.containsKey(checkKey)) {
			                 if (isAdmin) {
			                     errMap.put(licenseErrKey, "LICENSE_NAME.UNCONFIRMED");
			                 } else if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "LICENSE_NAME.UNCONFIRMED");
			                 }
	
			                 break;
			             }
			         }
			     } else if (ossComponentLicenseListMap != null && ossComponentLicenseListMap.containsKey(gridId)) {
			         for (ProjectIdentification license : ossComponentLicenseListMap.get(gridId)) {
			             if (CoConstDef.FLAG_YES.equals(license.getExcludeYn())) {
			                 continue;
			             }
	
			             String upperLicense = avoidNull(license.getLicenseName()).toUpperCase();
			             if (!CoCodeManager.LICENSE_INFO_UPPER.containsKey(upperLicense) && !ossInfo.containsKey(checkKey)) {
			                 if (isAdmin) {
			                     errMap.put(licenseErrKey, "LICENSE_NAME.UNCONFIRMED");
			                 } else if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "LICENSE_NAME.UNCONFIRMED");
			                 }
			                 break;
			             }
			         }
			     }
			 }
	
			 // -----------------------------------------------------------------------------
			 // OSS / LICENSE MATCH VALIDATION
			 // -----------------------------------------------------------------------------
	
			 if (!CommonFunction.isIgnoreLicense(licenseName)) {
			     // -----------------------------------------------------------------------------
			     // OSS 존재 여부
			     // -----------------------------------------------------------------------------
	
			     if (!errMap.containsKey("OSS_NAME." + gridId) && !errMap.containsKey("OSS_VERSION." + gridId) && !ossInfo.containsKey(checkKey)) {
			         if (checkNonVersionOss(ossInfo, ossName)) {
			             if (isAdmin) {
			                 errMap.put("OSS_VERSION." + gridId, "OSS_VERSION.UNCONFIRMED");
			             } else {
			                 diffMap.put("OSS_VERSION." + gridId, "OSS_VERSION.UNCONFIRMED");
			             }
			         } else {
			             if (isAdmin) {
			                 errMap.put("OSS_NAME." + gridId, "OSS_NAME.UNCONFIRMED");
			             } else {
			                 diffMap.put("OSS_NAME." + gridId, "OSS_NAME.UNCONFIRMED");
			             }
			         }
			     }
	
			     // -----------------------------------------------------------------------------
			     // OSS는 존재하지만 입력한 license가 declared/detected 와 다른 경우
			     // -----------------------------------------------------------------------------
			     else if (!errMap.containsKey("OSS_NAME." + gridId) && !errMap.containsKey(licenseErrKey)) {
			         String licenseText = "";
	
			         if (ossmaster != null) {
			             licenseText = CommonFunction.makeLicenseExpressionMsgType(ossmaster.getOssLicenses(), true);
			         }
	
			         if (bean.getOssComponentsLicenseList() != null && !bean.getOssComponentsLicenseList().isEmpty()) {
			             // Declared + Detected 전체 불일치
			             if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList())) {
			                 if (isAdmin) {
			                     errMap.put(licenseErrKey, "Declared : " + licenseText);
			                 } else if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "errLv|Declared : " + licenseText);
			                 }
			             }
			             // Declared 불일치
			             else if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList(), false)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			             // Non-permissive 누락
			             else if (hasOssLicenseTypeComponents(ossmaster, bean.getOssComponentsLicenseList())) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			         } else if (ossComponentLicenseListMap != null && ossComponentLicenseListMap.containsKey(gridId)) {
			             List<ProjectIdentification> useLicenseList = ossComponentLicenseListMap.get(gridId);
			             // Declared + Detected 전체 불일치
			             if (!hasOssLicense2(ossmaster, useLicenseList)) {
			                 if (isAdmin) {
			                     errMap.put(licenseErrKey, "Declared : " + licenseText);
			                 } else if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "errLv|Declared : " + licenseText);
			                 }
			             }
			             // Declared 불일치
			             else if (!hasOssLicense2(ossmaster, useLicenseList, false)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			             // Non-permissive 누락
			             else if (hasOssLicenseTypeProject(ossmaster, useLicenseList)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			         } else if (ossComponentLicenseListMap == null && isPartner) {
			             // Declared + Detected 전체 불일치
			             if (!hasOssLicense(ossmaster, licenseName, bean.getExcludeYn())) {
			                 if (isAdmin) {
			                     errMap.put(licenseErrKey, "Declared : " + licenseText);
			                 } else if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "errLv|Declared : " + licenseText);
			                 }
			             }
			             // Declared 불일치
			             else if (!hasOssLicense(ossmaster, licenseName, bean.getExcludeYn(), false)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			             // Non-permissive 누락
			             else if (hasOssLicenseTypeSingle(ossmaster, licenseName)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			         }
			     }
			 } else {
			     if (!errMap.containsKey("OSS_NAME." + gridId) && !errMap.containsKey("OSS_VERSION." + gridId) && !ossInfo.containsKey(checkKey)) {
			         if (checkNonVersionOss(ossInfo, ossName)) {
			             if (isAdmin) {
			                 errMap.put("OSS_VERSION." + gridId, "OSS_VERSION.UNCONFIRMED");
			             } else {
			                 diffMap.put("OSS_VERSION." + gridId, "OSS_VERSION.UNCONFIRMED");
			             }
			         }
			     } else if (!errMap.containsKey("OSS_NAME." + gridId) && !errMap.containsKey("OSS_VERSION." + gridId) && ossInfo.containsKey(checkKey)) {
			         String licenseText = "";
			         if (ossmaster != null) {
			             licenseText = CommonFunction.makeLicenseExpressionMsgType(ossmaster.getOssLicenses(), true);
			         }
	
			         if (CollectionUtils.isNotEmpty(bean.getOssComponentsLicenseList())) {
			             if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList())) {
			                 if (isAdmin) {
			                     errMap.put(licenseErrKey, "Declared : " + licenseText);
			                 } else if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "errLv|Declared : " + licenseText);
			                 }
			             } else if (!hasOssLicense(ossmaster, bean.getOssComponentsLicenseList(), false)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             } else if (hasOssLicenseTypeComponents(ossmaster, bean.getOssComponentsLicenseList())) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			         } else if (ossComponentLicenseListMap != null && ossComponentLicenseListMap.containsKey(gridId)) {
			             List<ProjectIdentification> useLicenseList = ossComponentLicenseListMap.get(gridId);
			             if (!hasOssLicense2(ossmaster, useLicenseList)) {
			                 if (isAdmin) {
			                     errMap.put(licenseErrKey, "Declared : " + licenseText);
			                 } else if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "errLv|Declared : " + licenseText);
			                 }
			             } else if (!hasOssLicense2(ossmaster, useLicenseList, false)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             } else if (hasOssLicenseTypeProject(ossmaster, useLicenseList)) {
			                 if (!errMap.containsKey(licenseErrKey)) {
			                     diffMap.put(licenseErrKey, "Declared : " + licenseText);
			                 }
			             }
			         }
			     }
			 }

			 // -------------------------------------------------------------------------
			 // FILE PATH
			 // -------------------------------------------------------------------------

			 {
				 errCd = checkBasicError("FILE_PATH", "filePath", filePath, true);
				 if (!isEmpty(errCd)) {
					 errMap.put("FILE_PATH." + gridId, errCd);
				 } else if (isAndroid && filePath.isEmpty()) {
					 errMap.put("FILE_PATH." + gridId, "FILE_PATH.REQUIRED");
				 } else if (diffMap != null && isSource && (filePath.contains("\r\n") || filePath.contains("\n"))) {
					 diffMap.put("FILE_PATH." + gridId, "FILE_PATH.FORMAT");
				 }
			 }
			 
			 // -------------------------------------------------------------------------
			 // ANDROID
			 // -------------------------------------------------------------------------

			 if (isAndroid) {
				 // -------------------------------------------------------------------------
				 // BINARY NAME
				 // -------------------------------------------------------------------------
				 {
					 errKey = "BINARY_NAME." + gridId;
					 if (!errMap.containsKey(errKey)) {
						 errCd = checkBasicError("BINARY_NAME", "binaryName", binaryName, true);
						 if (!isEmpty(errCd)) {
							 errMap.put("BINARY_NAME." + gridId, errCd);
						 } else if (binaryName.isEmpty()) {
							 errMap.put("BINARY_NAME." + gridId, "BINARY_NAME.REQUIRED");
						 }
					 }
				 }

				 // -------------------------------------------------------------------------
				 // BINARY NOTICE
				 // -------------------------------------------------------------------------
				 {
					 errKey = "BINARY_NOTICE." + gridId;
					 if (!diffMap.containsKey(errKey)) {
						 errCd = checkBasicError("BINARY_NOTICE", "binaryNotice", binaryNotice, true);
						 String permissiveKey = checkKey + "|" + licenseName;
						 boolean permissive = permissiveCache.computeIfAbsent(permissiveKey, k -> checkUsedPermissive(bean, licenseList));
						 if (!isEmpty(errCd)) {
							 diffMap.put("BINARY_NOTICE." + gridId, errCd);
						 } else if (("ok".equalsIgnoreCase(binaryNotice) || "ok(NA)".equalsIgnoreCase(binaryNotice)) && !permissive) {
							 diffMap.put("BINARY_NOTICE." + gridId, "BINARY_NOTICE.NOTICE_FIND");
						 } else if (("nok".equalsIgnoreCase(binaryNotice) || "nok(NA)".equalsIgnoreCase(binaryNotice)) && permissive) {
							 diffMap.put("BINARY_NOTICE." + gridId, "BINARY_NOTICE.NOTICE_PERMISSIVE");
						 }
					 }
				 }
			 }
			 
			 // -------------------------------------------------------------------------
			 // DOWNLOAD LOCATION CHECK
			 // -------------------------------------------------------------------------
			 if (CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossNameUpper)) {
				 String checkOssName = CoCodeManager.OSS_INFO_UPPER_NAMES.get(ossNameUpper);
				 OssMaster ossBean = ossNameOnlyMap.get(checkOssName.toUpperCase());
				 if (ossBean != null && !diffMap.containsKey("DOWNLOAD_LOCATION." + gridId) && !downloadLocation.isEmpty() && checkOssData(ossBean, downloadLocation, "PURL")) {
					 diffMap.put("DOWNLOAD_LOCATION." + gridId, "DOWNLOAD_LOCATION.DIFFERENT");
				 }
			 }
			 
			 // -------------------------------------------------------------------------
			 // RECOMMENDED LICENSE
			 // -------------------------------------------------------------------------
			 if (ossmaster != null && !licenseName.isEmpty() && !diffMap.containsKey(licenseErrKey) && !errMap.containsKey(licenseErrKey)) {
				 String recommended = CommonFunction.makeRecommendedLicenseString(ossmaster, bean);
			     if (!isEmpty(recommended)) {
			    	 diffMap.put(licenseErrKey, "Recommended : " + recommended);
			     }
			 }
			 
			 // -------------------------------------------------------------------------
			 // NOTICE EXCEPTION
			 // -------------------------------------------------------------------------
			 if (isAndroid) {
				 String noticeKey = "BINARY_NOTICE." + gridId;
			     if (errMap.containsKey(licenseErrKey) && (errMap.containsKey(noticeKey) || diffMap.containsKey(noticeKey))) {
			    	 String errCode = errMap.get(licenseErrKey);
			    	 if ("LICENSE_NAME.UNCONFIRMED".equals(errCode)) {
			    		 errMap.remove(noticeKey);
			    		 diffMap.remove(noticeKey);
			    	 }
			     }
			     if (diffMap.containsKey(licenseErrKey) && (errMap.containsKey(noticeKey) || diffMap.containsKey(noticeKey))) {
			    	 String diffCode = diffMap.get(licenseErrKey);
				     if ("LICENSE_NAME.UNCONFIRMED".equals(diffCode)) {
				    	 errMap.remove(noticeKey);
				         diffMap.remove(noticeKey);
				     }
			     }
			 }

			 // -------------------------------------------------------------------------
			 // ADD OSS_VERSION REQUIRED MSG
			 // -------------------------------------------------------------------------
			 if (!ossName.isEmpty() && !"-".equals(ossName) && ossVersion.isEmpty()) {
				 if (!errMap.containsKey("OSS_VERSION." + gridId)) {
					 OssMaster noVersionMaster = CoCodeManager.OSS_INFO_UPPER.get((ossName + "_").toUpperCase());
			         if (noVersionMaster != null && avoidNull(noVersionMaster.getOssType()).contains("V")) {
			        	 diffMap.put("OSS_VERSION." + gridId,"OSS_VERSION.REQUIRED");
			         }
				 }
			 }
		} // end of loop
		Map<String, List<BinaryData>> checkBinaryInfoMap = new HashMap<>();
		if ((PROC_TYPE_IDENTIFICATION_ANDROID.equals(PROC_TYPE) || PROC_TYPE_IDENTIFICATION_BIN.equals(PROC_TYPE) || PROC_TYPE_IDENTIFICATION_PARTNER.equals(PROC_TYPE)) && !isEmpty(projectId)) {
			Project projectInfo = projectService.getProjectBasicInfo(projectId);
			checkBinaryInfoMap = binaryDataService.getBinaryListFromBinaryDB(PROC_TYPE_IDENTIFICATION_ANDROID.equals(PROC_TYPE), projectInfo, checkSumInfoMap);
		}
		if(checkBinaryInfoMap != null) {
			String errMessageFormatSame = "Same {0}";
			String errMessageFormatSimilar = "Similar {0}";
			String errMessageFormatModified = "Modified ";
			
			for (ProjectIdentification bean : ossComponetList) {
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					continue;
				}
				
				basicKey = "BINARY_NAME";
				gridKey = StringUtil.convertToCamelCase(basicKey);
				errKey = basicKey + "." + bean.getGridId();
				
				if (!diffMap.containsKey(errKey)) {
					String binaryName = avoidNull(bean.getBinaryName());
					
					if (isEmpty(binaryName)) {
						continue;
					}

					List<BinaryData> _batList = checkBinaryInfoMap.get(binaryName);
					if (_batList != null && !_batList.isEmpty()) {
						String msgParam = "";
						String msgParamSame = ""; // 동일한 binary가 존재하는 경우
						String msgParamLicense = "";

						// oss name version이 동일한게 있으면 pass
						boolean hasBatOssSameTlsh = false;

						// 먼저 사용자 입력 정보와 동일한 binary 정보가 db에 존재하는 경우 message를
						// 표시하지 않는다
						boolean doCheck = true;
						boolean tlshDistanceOver = true;
						
						for (BinaryData _temp : _batList) {
							// 동일한 binary가 없는 경우 유사한 binary 체크
							if (avoidNull(bean.getOssName(), "-").equalsIgnoreCase(_temp.getOssName())
									&& avoidNull(bean.getOssVersion()).equalsIgnoreCase(_temp.getOssVersion())
									&& compareLicenseWithLicenseNameSort(bean, _temp, false)) {
								doCheck = false;
							}
							
							if(_temp.getTlshDistance() > 120){
								tlshDistanceOver = false;
							}
						}
						
						if (doCheck && tlshDistanceOver) {
							BinaryData _temp = null;
							for (BinaryData _temp2 : _batList) {
								if (_temp2.getTlshDistance() == 0) {
									hasBatOssSameTlsh = true;
									_temp = _temp2;
									break;
								}
							}
							
							if(_temp == null) {
								_temp = _batList.get(0);
							}
							
							boolean isSameLicense = compareLicenseWithLicenseNameSort(bean, _temp, false);
							boolean isIncludeLicense = compareLicenseWithLicenseNameSort(bean, _temp, true);
							boolean isSameOssName = avoidNull(bean.getOssName(), "-").equalsIgnoreCase(_temp.getOssName());
							boolean isSameOssVersion = avoidNull(bean.getOssVersion()).equalsIgnoreCase(_temp.getOssVersion());
							//2) OSS NAME + VERSION 등일하나 LICENSE 만 다른 경우
							if(isSameOssName
									&& isSameOssVersion
									&& !isSameLicense) {
								// Same binary : / <License>
								if (!isIncludeLicense) {
									diffMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + " /"+_temp.getLicense())); // message를
								} else {
									infoMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + " /"+_temp.getLicense()));
								}
							} 
							// 3) OSS NAME LICENSE는 동일하나  VERSION 이 다른경우
							else if(isSameOssName && isSameLicense && !isSameOssVersion) {
								// Same binary : <OSS Name> <OSS Version>
								infoMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + makeBinaryOssName(_temp.getOssName(), _temp.getOssVersion())+" /"));
							}
							// 4) OSS NAME 은 동일하나 VERSION 과 LICENSE 가 다른 경우 
							else if(isSameOssName && !isSameOssVersion && !isSameLicense) {
								// Same binary : <OSS Name> <OSS Version> / <License>
								if (!isIncludeLicense) {
									diffMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + makeBinaryOssName(_temp.getOssName(), _temp.getOssVersion()) + " / "+_temp.getLicense()));
								} else {
									infoMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + makeBinaryOssName(_temp.getOssName(), _temp.getOssVersion()) + " / "+_temp.getLicense()));
								}
							}
							// 6) OSS NAME은 다르나, LICENSE 는 동일
							else if(!isSameOssName && isSameLicense) {
								// Same binary : <OSS Name> <OSS Version> /
								infoMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + makeBinaryOssName(_temp.getOssName(), _temp.getOssVersion()) + " /"));
							} 
							// 7) OSS NAME LICENSE 모두 다른 경우
							else {
								// Same binary : <OSS Name> <OSS Version> / <License>
								if (!isIncludeLicense) {
									diffMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + makeBinaryOssName(_temp.getOssName(), _temp.getOssVersion()) + " / "+_temp.getLicense()));
								} else {
									infoMap.put(errKey, MessageFormat.format(hasBatOssSameTlsh ? errMessageFormatSame : errMessageFormatSimilar, (hasBatOssSameTlsh ? ":" : " ("+_temp.getTlshDistance()+") :") + makeBinaryOssName(_temp.getOssName(), _temp.getOssVersion()) + " / "+_temp.getLicense()));
								}
							}
						} else {
							// oss name + version + license 까지 동일하면 match info Matched message 표시
							infoMap.put(errKey, basicKey + ".MATCHED");
						}
					}
					// 기 등록된 binary 정보가 없으면
					else if (checkBinaryInfoMap.containsKey(binaryName)) {
						infoMap.put(errKey, basicKey + ".NEW_BINARY_OSS");
					}
				}
			}
		}
	}

	private boolean compareLicenseWithLicenseNameSort(ProjectIdentification bean, BinaryData _temp, boolean isInclude) {

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
		for (String s : avoidNull(_temp.getLicense()).split(",")) {
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

		if (!isInclude) {
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
		} else {
			if (!CollectionUtils.isEmpty(compare1)) {
				if (CollectionUtils.isEmpty(compare2)) {
					return false;
				} else {
					List<String> includeLicenseCheckList = new ArrayList<>(compare1);
					includeLicenseCheckList.removeAll(compare2);
					if (includeLicenseCheckList.size() != compare1.size()) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		}
	}
	
	private String makeBinaryOssName(String ossName, String ossVersion) {
		String rtn = avoidNull(ossName, "-");
		
		if (!isEmpty(ossVersion)) {
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
						&& CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(license.getLicenseName()).toUpperCase())) {
					LicenseMaster master = CoCodeManager.LICENSE_INFO_UPPER.get(license.getLicenseName().toUpperCase());
					
					if (master != null && (CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationDisclosingSrcYn()))
							|| CoConstDef.FLAG_YES.equals(avoidNull(master.getObligationNotificationYn())))) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean checkOROperation(List<ProjectIdentification> licenseList, OssMaster ossInfo) {
		if (ossInfo != null) {
			String licenseGroup = CommonFunction.makeLicenseExpression(ossInfo.getOssLicenses());
//			String[] licenseGroupSplit = licenseGroup.split("OR");
			
			List<String> andCombLicenseList = new ArrayList<>();
			for (OssLicense bean : ossInfo.getOssLicenses()) {
				if (andCombLicenseList.isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
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
			
			for (ProjectIdentification iden : licenseList) {
				if (!licenseGroup.contains(iden.getLicenseName())) {
					returnFlag = true;
					break;
				}
			}
			
			if (returnFlag || licenseList.size() == 1) { // OSS에 등록된 license를 사용하지 않았거나, license가 1개만 들록된 경우 dual check를 하지 않음.
				return false;
			}
			
			for (String licenseName : andCombLicenseList) {
				for (ProjectIdentification iden : licenseList) {
					if (!licenseName.trim().contains(iden.getLicenseName().trim()) && CoConstDef.FLAG_NO.equals(iden.getExcludeYn())) {
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
		if (ossInfo != null) {
			// License가 permissive로만 구성되어 있는지 check 함.
			List<OssLicense> permissiveCheck = ossInfo.getOssLicenses()
														.stream()
														.filter(ol -> !CoConstDef.CD_LICENSE_TYPE_PMS.equals(ol.getLicenseType()))
														.collect(Collectors.toList());
			
			// 전체가 permissive로 이루어져 있으므로 message를 출력하지 않음.
			if (permissiveCheck.size() == 0) { 
				return false;
			}
			
			
			List<OssLicense> ossLicenses = ossInfo.getOssLicenses().stream().filter(ol -> "OR".equals(ol.getOssLicenseComb())).collect(Collectors.toList());
			
			// Single License이거나 AND로만 구성된 Multi License -> Group을 나눌 필요가 없음.
			if (ossLicenses.size() == 0) {				
				// permissive가 아닌 licenseType이면서 사용자가 입력한 License Name중에 없는 License가 존재할 경우 message를 출력함.
				ossLicenses = ossInfo.getOssLicenses()
											.stream()
											.filter(ol -> !CoConstDef.CD_LICENSE_TYPE_PMS.equals(ol.getLicenseType()) 
															&& !licenseNameList.contains(ol.getLicenseName()))
											.collect(Collectors.toList());
			
				if (ossLicenses.size() > 0) {
					return true;
				}
			} 
			// Multi License(AND, OR 전부 포함한 case) -> Group을 나누어 각각 check를 함.
			else {
				List<List<OssLicense>> groupList = new ArrayList<>();
				List<OssLicense> olList = new ArrayList<>();
				
				// OR Group별로 분리
				for (OssLicense bean : ossInfo.getOssLicenses()) {
					if (olList.isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
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
				for (List<OssLicense> list : groupList) {
					List<OssLicense> checkList = list.stream().filter(c -> licenseNameList.contains(c.getLicenseName())).collect(Collectors.toList());
					// 현재 그룹 내에 사용된 license name이 존재하는지 check함.
					if (checkList.size() == 0) {
						continue; // 그룹내에 사용한 license name이 없을 경우 continue
					}
					
					list = list.stream()
							   .filter(ol -> !CoConstDef.CD_LICENSE_TYPE_PMS.equals(ol.getLicenseType()) 
									   			&& !licenseNameList.contains(ol.getLicenseName()))
							   .collect(Collectors.toList());
					
					if (list.size() == 0) {
						errorFlag = false;
						break;
					}
				}
				
				if (errorFlag) {
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
				case "totalList":
				case "fullDiscoveredList":
					ossComponetSecurityList = (List<OssComponents>) obj;
				break;
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
