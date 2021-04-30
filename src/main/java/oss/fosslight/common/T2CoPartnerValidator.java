/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.util.StringUtil;

public class T2CoPartnerValidator extends T2CoValidator {
	private List<ProjectIdentification> ossComponetList = null;
	private List<List<ProjectIdentification>> ossComponentLicenseList = null;
	private String PROC_TYPE = null;
	public final String PROC_TYPE_IDENTIFICATION_SOURCE = "SRC";
	public final String PROC_TYPE_IDENTIFICATION_CONFIRM = "CONF";

	@SuppressWarnings("unused")
	@Override
	protected void customValidation(Map<String, String> map, Map<String, String> errMap, Map<String, String> diffMap, Map<String, String> infoMap) {
		if (PROC_TYPE_IDENTIFICATION_CONFIRM.equals(PROC_TYPE)) {
			validComfirmProcess(map, errMap);
		} else {
			Map<String, OssMaster> ossInfo = null;
			
			// dataMap을 사용하지 않고, request정보를 직접 참조
			if (ossComponetList != null) {
				String basicKey = "";
				String gridKey = "";
				List<ProjectIdentification> licenseList = null;
				OssMaster ossParam = new OssMaster(); // 설정된 oss 정보를 DB에서 취득한다.
				ossParam.setOssNames(getOssNames());
				
				if (ossParam.getOssNames() != null && ossParam.getOssNames().length > 0) {
					ossInfo = CoCodeManager.OSS_INFO_UPPER;
				}

				// checkBasicError : REQUIRED, LENGTH, FORMAT 만 체크!
				for (ProjectIdentification bean : ossComponetList) {
					boolean hasError = false;
					boolean hasMultiError = false; // multi license용

					// exclude=Y 상태인 경우 체크하지 않음
					if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
						continue;
					}

					// 1) oss name
					{
						basicKey = "OSS_NAME";
						gridKey = StringUtil.convertToCamelCase(basicKey);
						// 기본체크
						String errCd = checkBasicError(basicKey, gridKey, bean.getOssName());
						
						if (!isEmpty(errCd)) {
							errMap.put(basicKey + "." + bean.getGridId(), errCd);
						} else {
							// OSS NAME에 대해서 추가적으로 해야할 것이 있다면
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
					licenseList = findLicense(bean.getGridId());

					if (licenseList.isEmpty()) {
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
								{
									basicKey = "LICENSE_TEXT";
									gridKey = StringUtil.convertToCamelCase(basicKey);
									// 5-2 license text의 basic validator
									// 기본체크
									String errCd = checkBasicError(basicKey, gridKey, licenseBean.getLicenseText(), true);
									
									if (!isEmpty(errCd)) {
										errMap.put(basicKey + "." + licenseBean.getGridId(), errCd);
										hasMultiError = hasError = true;
									}
								}
								// 5-3. 라이선스 정보가 db에 없고, license text가 공백인 경우
								if (!hasError && !CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseBean.getLicenseName().toUpperCase())
										&& isEmpty(licenseBean.getLicenseText())) {
									errMap.put(basicKey + "." + licenseBean.getGridId(), "LICENSE_TEXT.REQUIRED");
									hasMultiError = hasError = true;
								}
								// 5-4. 라이선스가 DB에 존재하고, REQ_LICENSE_TEXT_YN='Y'
								// 인 경우, license text 필수 체크
								// MIT like, BSD like 대응
								if (!hasError && CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseBean.getLicenseName().toUpperCase())
										&& CoConstDef.FLAG_YES.equals(CoCodeManager.LICENSE_INFO_UPPER
												.get(licenseBean.getLicenseName().toUpperCase()).getReqLicenseTextYn())) {
									errMap.put(basicKey + "." + licenseBean.getGridId(), "LICENSE_TEXT.REQUIRED");
									hasMultiError = hasError = true;
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
							} else if (checkOROperation(licenseList)) {
								// OR 조건이 두개이상 선택된 경우
								errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".INCLUDE_DUAL_OPERATE");
							} else if (checkANDOperation(licenseList)) {
								// AND OR 연산식에 부합하지 않는 경우
								// 순서대로 첫번재 exclude 되지 않은 licenes를 기준으로 and 조건
								// 라이선스중에 (다음 OR를 만나기전) exclude된 라이선스가 있는지
								errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".SATISFY");
							}
						}
					}

					// 6) path 정보
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
						else if (!hasError && isEmpty(bean.getFilePath())) {

							if (licenseList.isEmpty()) {
								if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(bean.getLicenseName().toUpperCase())
										&& CoConstDef.FLAG_YES.equals(CoCodeManager.LICENSE_INFO_UPPER
												.get(bean.getLicenseName().toUpperCase()).getObligationDisclosingSrcYn())) {
									errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".REQUIRED");
								}
							} else {
								// license name에 error가 없을 경우만
								if (!errMap.containsKey("LICENSE_NAME." + bean.getGridId())) {
									// 선택한 라이선스 중에 하나라도 소스공개 의무를 가질 경우 에러
									for (ProjectIdentification licenseBean : licenseList) {
										if (!CoConstDef.FLAG_YES.equals(licenseBean.getExcludeYn())
												&& CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseBean.getLicenseName().toUpperCase())
												&& CoConstDef.FLAG_YES.equals(
														CoCodeManager.LICENSE_INFO_UPPER.get(licenseBean.getLicenseName().toUpperCase())
																.getObligationDisclosingSrcYn())) {
											errMap.put(basicKey + "." + bean.getGridId(), basicKey + ".REQUIRED");
											
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void validComfirmProcess(Map<String, String> map, Map<String, String> errMap) {
		// TODO Auto-generated method stub		
	}
	
	private boolean checkANDOperation(List<ProjectIdentification> licenseList) {
		// OR 조건으로 각 list로 구분한다.
		List<List<ProjectIdentification>> andCombLicenseList = new ArrayList<>();
		
		for (ProjectIdentification bean : licenseList) {
			if (bean.getOssLicenseComb().isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
				andCombLicenseList.add(new ArrayList<>());
			}
			
			andCombLicenseList.get(andCombLicenseList.size() - 1).add(bean);
		}

		for (List<ProjectIdentification> list : andCombLicenseList) {
			for (ProjectIdentification andLicense : list) {
				if (!CoConstDef.FLAG_YES.equals(andLicense.getExcludeYn())) {
					return hasExclude(list, true);
				}
			}
		}
		
		return false;
	}

	private boolean hasExclude(List<ProjectIdentification> list, boolean exclude) {
		for (ProjectIdentification bean : list) {
			if (exclude) {
				if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					return true;
				}
			} else {
				if (!CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean checkOROperation(List<ProjectIdentification> licenseList) {
		// OR 조건으로 각 list로 구분한다.
		List<List<ProjectIdentification>> andCombLicenseList = new ArrayList<>();
		for (ProjectIdentification bean : licenseList) {
			if (andCombLicenseList.isEmpty() || "OR".equals(bean.getOssLicenseComb())) {
				andCombLicenseList.add(new ArrayList<>());
			}
			
			andCombLicenseList.get(andCombLicenseList.size() - 1).add(bean);
		}

		boolean hasSelectedLicense = false;
		
		for (List<ProjectIdentification> list : andCombLicenseList) {
			boolean hasSelected = hasExclude(list, false);
			
			if (hasSelectedLicense && hasSelected) {
				return true;
			}
			
			if (hasSelected) {
				hasSelectedLicense = hasSelected;
			}
		}

		return false;
	}

	private String[] getOssNames() {
		List<String> names = new ArrayList<>();
		
		for (ProjectIdentification bean : ossComponetList) {
			if (CoConstDef.FLAG_YES.equals(bean.getExcludeYn()) || isEmpty(bean.getOssName()) || names.contains(bean.getOssName().trim())) {
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
					
					if (bean.getGridId().startsWith(key)) {
						licenseList.add(bean);
						breakFlag = true;
					}
				}

				if (breakFlag) {
					break;
				}
			}
		}
		
		return licenseList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setAppendix(String key, Object obj) {
		if ("mainList".equals(key)) {
			ossComponetList = (List<ProjectIdentification>) obj;
		} else if ("subList".equals(key)) {
			ossComponentLicenseList = (List<List<ProjectIdentification>>) obj;
		}
	}

	@Override
	protected String treatment(String paramvalue) {
		return paramvalue;
	}
	
	public void setProcType(String type) {
		PROC_TYPE = type;
	}
}
