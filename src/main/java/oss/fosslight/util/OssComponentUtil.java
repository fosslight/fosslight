/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;

public class OssComponentUtil {
	
	private static OssComponentUtil instance;
	
	private boolean USE_AUTOCOMPLETE_LICENSE_TEXT = "true".equalsIgnoreCase(CommonFunction.getProperty("use.autocomplete.licensetext"));
	
	public static OssComponentUtil getInstance () {
		if (instance == null) {
			instance = new OssComponentUtil();
		}
		
		return instance;
	}
	
	public void makeOssComponent(List<OssComponents> selectedData) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		makeOssComponent(selectedData, false);
	}
	
	public void makeOssComponent(List<OssComponents> selectedData, boolean replaceLikeLicense) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		makeOssComponent(selectedData, replaceLikeLicense, false);
	}
	
	public void makeOssComponent(List<OssComponents> selectedData, boolean replaceLikeLicense, boolean replaceDownloadHomepage) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		// DB 확인 된 OSS list
		List<OssComponents> fillList = new ArrayList<>();
		// DB 에 등록되지 않은 oss list
		List<OssComponents> newList = new ArrayList<>();
		
		List<String> ossNameList = new ArrayList<>();
		
		// ossName 정보만 취득
		for (OssComponents bean : selectedData) {
			if (!StringUtil.isEmpty(bean.getOssName()) && !ossNameList.contains(bean.getOssName())) {
				ossNameList.add(bean.getOssName());
			}
		}
		
		// Oss Master 정보 취득
		OssMaster param = new OssMaster();
		String[] ossNames = new String[ossNameList.size()];
		param.setOssNames(ossNameList.toArray(ossNames));
		
		Map<String, OssMaster> regData = CoCodeManager.OSS_INFO_UPPER;
		Map<String, String> ossNameInfo = CoCodeManager.OSS_INFO_UPPER_NAMES;
		
		// DB 정보를 기준으로 Merge
		for (OssComponents bean : selectedData) {
			String key = (bean.getOssName() + "_" + StringUtil.nvl(bean.getOssVersion())).toUpperCase();
			
			// 분석결과서 업로드시 OSS VERSION을 N/A로 사용자가 지정한 경우, OSS VERSION이 공백인 OSS가 존재함에도 불구하고, 다른 OSS로 인식하여 멀티라이선스로 적용되지 않는 현상
			// N/A인 경우 공백이 VERSION이 존재하는지 체크하고 존해할 경우 자동 치환한다.
			if ("AWS IoT Device SDK for C".equals(bean.getOssName())) {
				System.out.println();
			}
			
			if (regData.containsKey(key)) {
				// 사용자가 입력한 oss 가 db에 존재한다면
				// DB + 입력정보
				fillList.addAll(mergeOssAndLicense(regData.get(key), bean, replaceDownloadHomepage));				
			} else if ("N/A".equalsIgnoreCase(bean.getOssVersion()) && regData.containsKey((bean.getOssName() + "_").toUpperCase()))  {
				bean.setOssVersion("");
				fillList.addAll(mergeOssAndLicense(regData.get((bean.getOssName() + "_").toUpperCase()), bean, replaceDownloadHomepage));	
			} else {
				// DB에서 확인되지 않은 OSS
				// 라이선스별로 row 등록
				for (OssComponentsLicense userLicense : bean.getOssComponentsLicense()) {
					List<OssComponentsLicense> _list = new ArrayList<>();
					_list.add(fillLicenseInfo(userLicense));
					OssComponents newBean = (OssComponents) BeanUtils.cloneBean(bean);
					
					newBean.setOssComponentsLicense(_list);
					newList.add(newBean);
				}
			}
		}
		
 		if (!newList.isEmpty()) {
			fillList.addAll(newList);
		}
		
		if (!fillList.isEmpty()) {
			selectedData.clear();
			selectedData.addAll(fillList);
		}
	}
	
	private List<OssComponents> mergeOssAndLicense(OssMaster master, OssComponents ossComponent, boolean replaceDownloadHomepage) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		// 1. OSS Master를 기준으로 Merge 한다.
		
		// 사용자가 입력한 라이선스가 oss 에 포함되어 있지 않을 경우
		// oss 가 multi or dual 라이선스인 경우는 해당 oss의 subgrid 하단에 추가
		// oss 가 single 라이선스인 경우는 해당 master 정보를 사용하지 않는다.
		
		List<OssComponents> list = new ArrayList<>();

		setOssBasicInfo(ossComponent, master, replaceDownloadHomepage);
		
		// 멀티라이선스인 경우
		if (CoConstDef.LICENSE_DIV_MULTI.equals(master.getLicenseDiv())) {
			mergeOssLicenseInfo(ossComponent, master);
			
			list.add(ossComponent);
		} else if (CoConstDef.LICENSE_DIV_SINGLE.equals(master.getLicenseDiv()) && ossComponent.getOssComponentsLicense() != null && ossComponent.getOssComponentsLicense().size() > 1) {
			// 싱글로 등록되어 있는 oss인데, 라이선스를 복수개 등록한 경우 별개의 row로 분할
			// 사용자가 입력한 라이선스별로 별도의 row로 구성
			for (OssComponentsLicense comLicense : ossComponent.getOssComponentsLicense()) {
				OssComponents newBean = (OssComponents) BeanUtils.cloneBean(ossComponent);
				newBean.setOssComponentsLicense(null);
				newBean.addOssComponentsLicense(comLicense);
				
				mergeOssLicenseInfo(newBean, master);
				
				list.add(newBean);
			}
		} else { // 싱글라이선스인 경우
			mergeOssLicenseInfo(ossComponent, master);
			
			list.add(ossComponent);
		}
		
		return list;
	}
	
	private void setOssBasicInfo(OssComponents ossComponent, OssMaster master, boolean replaceDownloadHomepage) {
		if (StringUtil.isEmpty(ossComponent.getOssId())) {
			ossComponent.setOssId(master.getOssId());
		}
		
		if (!replaceDownloadHomepage) {
			if (StringUtil.isEmpty(ossComponent.getDownloadLocation())) {
				ossComponent.setDownloadLocation(master.getDownloadLocation());
			}
			
			if (StringUtil.isEmpty(ossComponent.getHomepage())) {
				ossComponent.setHomepage(master.getHomepage());
			}
		}

		if (StringUtil.isEmpty(ossComponent.getCopyrightText())) {
			ossComponent.setCopyrightText(master.getCopyright());
		}
		
		// AND OR 연산표현식으로 변경
		ossComponent.setLicenseName(CommonFunction.makeLicenseExpression(master.getOssLicenses()));
	}
	
	private void mergeOssLicenseInfo(OssComponents ossComponent, OssMaster master) {
		// 2. oss license merge
		List<OssComponentsLicense> subList = new ArrayList<>();
		List<String> selectedLicenseList = new ArrayList<>();

		if (CoConstDef.LICENSE_DIV_MULTI.equals(master.getLicenseDiv())) {
			for (OssLicense subBean : master.getOssLicenses()) {
				OssComponentsLicense componentLicenseBean = findOssLicense(subBean, ossComponent);
				
				// 등록되어있는 license인 경우
				if (componentLicenseBean != null && !selectedLicenseList.contains(componentLicenseBean.getLicenseId())) {
					selectedLicenseList.add(componentLicenseBean.getLicenseId());
					componentLicenseBean.setExcludeYn(CoConstDef.FLAG_NO);
				} else {
					componentLicenseBean = new OssComponentsLicense();
					// full name -> short Identification
					componentLicenseBean.setLicenseName(CommonFunction.getShortIdentify(subBean.getLicenseName()));
					componentLicenseBean.setExcludeYn(CoConstDef.FLAG_YES);
				}

				componentLicenseBean.setLicenseId(subBean.getLicenseId());
				componentLicenseBean.setLicensetype(subBean.getLicenseType());
				componentLicenseBean.setOssLicenseComb(subBean.getOssLicenseComb());
				
				if (USE_AUTOCOMPLETE_LICENSE_TEXT && StringUtil.isEmpty(componentLicenseBean.getLicenseText())) {
					componentLicenseBean.setLicenseText(subBean.getOssLicenseText());
				}
				
				if (StringUtil.isEmpty(componentLicenseBean.getCopyrightText())) {
					componentLicenseBean.setCopyrightText(subBean.getOssCopyright());
				}
				
				subList.add(componentLicenseBean);
			}
			
			// DB에 등록되지 않은 라이선스가 포함된 경우 마지막 row로 추가
			subList.addAll(getCustomLicense(master, ossComponent));
		} else {
			OssComponentsLicense componentLicenseBean = findOssLicense(master.getOssLicenses().get(0), ossComponent);
			
			// 등록되어있는 license인 경우
			if (componentLicenseBean != null && !selectedLicenseList.contains(componentLicenseBean.getLicenseId())) {
				selectedLicenseList.add(componentLicenseBean.getLicenseId());
				
				// DB에 등록되지 않은 라이선스가 포함된 경우 마지막 row로 추가
				subList.add(fillLicenseInfo(componentLicenseBean));
			} else {
				// ossmaster DB에 등록되지 않은 라이선스가 포함된 경우 마지막 row로 추가
				subList.addAll(getCustomLicense(master, ossComponent));
			}
		}
		
		// permissve 기준에 따라 exclude 처리
		excludedLicense(selectedLicenseList, subList);
		
		ossComponent.setOssComponentsLicense(subList);
	}
	
	private OssComponentsLicense fillLicenseInfo(OssComponentsLicense componentLicenseBean) {
		if (CoCodeManager.LICENSE_INFO.containsKey(componentLicenseBean.getLicenseName())) {
			LicenseMaster license = CoCodeManager.LICENSE_INFO.get(componentLicenseBean.getLicenseName());
			
			if (StringUtil.isEmpty(componentLicenseBean.getLicenseId())) {
				componentLicenseBean.setLicenseId(license.getLicenseId());
			}
			
			if (USE_AUTOCOMPLETE_LICENSE_TEXT && StringUtil.isEmpty(componentLicenseBean.getLicenseText())) {
				componentLicenseBean.setLicenseText(license.getLicenseText());
			}
		}
		
		return componentLicenseBean;
	}
	
	private List<OssComponentsLicense> getCustomLicense(OssMaster master, OssComponents ossComponent) {
		// DB에 등록되지 않은 customer가 입력한 라이선스가 있을 경우, 해당 OSS의 마지막 row로 추가
		List<OssComponentsLicense> returnList = new ArrayList<>();
		List<String> ossMasterLicenseIdList = new ArrayList<>();
		
		for (OssLicense liBean : master.getOssLicenses()) {
			ossMasterLicenseIdList.add(liBean.getLicenseId());
		}
		
		for (OssComponentsLicense bean : ossComponent.getOssComponentsLicense()) {
			boolean addflag = true;
			
			// 닉네임, short identify 모두 비교
			if (CoCodeManager.LICENSE_INFO.containsKey(bean.getLicenseName())) {
				String selectedLicenseId = CoCodeManager.LICENSE_INFO.get(bean.getLicenseName()).getLicenseId();
				
				if (ossMasterLicenseIdList.contains(selectedLicenseId)) {
					addflag = false;
				}
			} else {
				// 미등록 라이선스
			}
			
			if (addflag && !StringUtil.isEmpty(bean.getLicenseName())) {
				bean.setEditable(CoConstDef.FLAG_YES);
				returnList.add(fillLicenseInfo(bean));
			}
		}
		
		return returnList;
	}
	
	private OssComponentsLicense findOssLicense(OssLicense dbData, OssComponents userDataList) {
		for (OssComponentsLicense bean : userDataList.getOssComponentsLicense()) {
			String selectedLicenseId = "";
			
			// 대소문자 구분으로 인한 문제로, 만약 일치하는 라이선스가 있을 경우
			// 사용자 입력 라이선스 명을 DB 명으로 변경한다.
			String inputLicenseName = bean.getLicenseName().trim();
			
			if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(inputLicenseName.toUpperCase())) {
				bean.setLicenseName(CoCodeManager.LICENSE_INFO_UPPER.get(inputLicenseName.toUpperCase()).getLicenseName());
			}
			
			// 확인 가능한 라이선스 이면
			if (CoCodeManager.LICENSE_INFO.containsKey(bean.getLicenseName())) {
				LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO.get(bean.getLicenseName());

				List<String> compareList = new ArrayList<>();
				compareList.add(licenseMaster.getLicenseName());
				
				if (!StringUtil.isEmpty(licenseMaster.getShortIdentifier())) {
					compareList.add(licenseMaster.getShortIdentifier());
				}
				
				if (licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
					compareList.addAll(licenseMaster.getLicenseNicknameList());
				}
				
				if (compareList.contains(bean.getLicenseName())) {
					selectedLicenseId = licenseMaster.getLicenseId();
				}
			}
			
			// 라이선스가 라이선스 마스터에 등록되어 있어야하고, oss에 포함되는 라이선스이어야만 반환
			if (selectedLicenseId.equals(dbData.getLicenseId())) {
				bean.setLicenseId(dbData.getLicenseId());
				
				return bean;
			}
		}
		
		return null;
	}
	
	private void excludedLicense(List<String> selectedLicenseList, List<OssComponentsLicense> allList) {
		// 1. 사용자가 선택 입력한 라이선스를 제외하고 모두 exclude 처리
		boolean isfirst = true;
		List<String> isSelectedDuplicatedLicenseCheckList = new ArrayList<>();
		
		for (OssComponentsLicense bean : allList) {
			boolean onSelected = CoConstDef.FLAG_YES.equals(bean.getEditable()) || selectedLicenseList.contains(bean.getLicenseId());
			
			// license id가 없다는 것은 사용자가 등록되지 않은 라이선스를 입력한 경우 => license master에 존재하는 경우 license id를 설정하기 때문에, Editable flag로 판단한다.
			// 복수개의 라이선스가 설정되어 있고(OSS는 DB에 존재한다는 뜻), 처음이 아닌경우 OR 조건으로 추가한다.
			if ( CoConstDef.FLAG_YES.equals(bean.getEditable())) {
				if (!isfirst && StringUtil.isEmpty(bean.getOssLicenseComb())) {
					bean.setOssLicenseComb("OR");
				}
			}
			
			if (!onSelected || isSelectedDuplicatedLicenseCheckList.contains(bean.getLicenseId()) || CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
				bean.setExcludeYn(CoConstDef.FLAG_YES);
			} else {
				if (null != bean.getLicenseId()) {
					isSelectedDuplicatedLicenseCheckList.add(bean.getLicenseId());
				}
				
				bean.setExcludeYn(CoConstDef.FLAG_NO);
			}
			
			isfirst = false;
		}
	}
}
