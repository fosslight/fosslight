/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.OS;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.*;
import oss.fosslight.lge.validation.custom.T2CoOssValidator;
import oss.fosslight.repository.ApiOssMapper;
import oss.fosslight.service.*;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ApiOssServiceImpl extends CoTopComponent implements ApiOssService{
	/** The api oss mapper. */
	@Autowired ApiOssMapper apiOssMapper;
	@Autowired OssService ossService;
	@Autowired HistoryService historyService;

	@Autowired CoReviewerService coReviewerService;
	@Autowired T2UserService userService;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public List<Map<String, Object>> getOssInfo(Map<String, Object> paramMap) {
		String rtnOssName = apiOssMapper.getOssName((String) paramMap.get("ossName"));
		
		if (!StringUtil.isEmpty(rtnOssName)) {
			paramMap.replace("ossName", rtnOssName);
		}
		
		return apiOssMapper.getOssInfo(paramMap);
	}

	@Override
	public List<Map<String, Object>> getOssInfoByDownloadLocation(String downloadLocation) {
		return apiOssMapper.getOssInfoByDownloadLocation(downloadLocation);
	}

	@Override
	public List<Map<String, Object>> getLicenseInfo(String licenseName) {
		return apiOssMapper.getLicenseInfo(licenseName);
	}
	
	
	public String[] getOssNickNameListByOssName(String ossName) {
		List<String> nickList = null;
		if (!StringUtil.isEmpty(ossName)) {
			nickList =  apiOssMapper.selectOssNicknameList(ossName);
			if (nickList != null) {
				nickList = nickList.stream()
									.filter(CommonFunction.distinctByKey(nick -> nick.trim().toUpperCase()))
									.collect(Collectors.toList());
			}
		}
		
		nickList = (nickList != null ? nickList : Collections.emptyList());
		return nickList.toArray(new String[nickList.size()]);
	}

	private Map<String, Object> saveOssAnalysisData2 (OssMaster ossMaster, OssAnalysis analysisBean) {
		Map<String, Object> result = new HashMap<>();
		if(CoCodeManager.OSS_INFO_UPPER.containsKey( (ossMaster.getOssName() + "_" + avoidNull(ossMaster.getOssVersion())).toUpperCase() )) {
			result.put("isValid", false);
			result.put("errMsg", ossMaster.getOssName() + "(" + ossMaster.getOssVersion() + ") is already existed.");
			return  result;
		}
		boolean isNewVersion = CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(ossMaster.getOssName().toUpperCase());

		String resultOssId = "";
		try {
			resultOssId = ossService.registOssMaster(ossMaster);
			analysisBean.setComponentId(analysisBean.getGroupId());
			analysisBean.setReferenceOssId(resultOssId);
			ossService.updateAnalysisComplete(analysisBean); // auto-analysis 완료처리
			result.put("isValid", true);
			result.put("ossId", resultOssId);
			result.put("ossVersion", ossMaster.getOssVersion());
		} catch (Exception e) {
			result.put("isValid", false);
			result.put("errMsg", e.getMessage());
			return  result;
		}

		CoCodeManager.getInstance().refreshOssInfo(); // 등록된 oss info 갱신

		History h = ossService.work(ossMaster);
		h.sethAction(CoConstDef.ACTION_CODE_INSERT);
		historyService.storeData(h);

		// history 저장 성공 후 메일 발송
		try {
			CoMail mailBean = new CoMail(isNewVersion ? CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION : CoConstDef.CD_MAIL_TYPE_OSS_REGIST);
			mailBean.setParamOssId(resultOssId);

			mailBean.setComment(ossMaster.getComment());

			if(isNewVersion && !isEmpty(ossMaster.getOssName())) {
				ossMaster.setExistOssNickNames(ossService.getOssNickNameListByOssName(ossMaster.getOssName()));
				mailBean.setParamOssInfo(ossMaster);
			}
			CoMailManager.getInstance().sendMail(mailBean);
		} catch (Exception e) {
			//log.error(e.getMessage(), e);
			return result;
		}

		return result;

	}

	private OssAnalysis changeFormatUserData(Map<String, String> userDataMap) {

		//{'oss_name_origin': 'sample55', 'version_origin': '3.0', 'license_origin': 'nan', 'dn_loc_origin': 'https://github.com/fosslight/fosslight_util',
		//'homepage_origin': 'nan', 'oss_name_ort': 'sample55', 'url_type': 'github', 'dn_loc_ort': 'https://github.com/fosslight/fosslight_util',
		// 'dn_loc_result': 'https://github.com/fosslight/fosslight_util', 'dn_loc_redirect_origin': '', 'download_ret': True, 'oss_version_download': '3.0', 'scanner_ret': True}

		for (String key : userDataMap.keySet()) {
			if (userDataMap.get(key).equals("nan")) {
				userDataMap.put(key, "");
			}
		}

		OssAnalysis ossAnalysis = new OssAnalysis();
		ossAnalysis.setOssName(userDataMap.get("oss_name_origin"));
		ossAnalysis.setOssVersion(versionFormatter(userDataMap.get("version_origin")));
		ossAnalysis.setLicenseName(userDataMap.get("license_origin"));
		ossAnalysis.setDownloadLocation(userDataMap.get("dn_loc_origin"));
		ossAnalysis.setHomepage(userDataMap.get("homepage_origin"));
		return ossAnalysis;
	}

	private OssAnalysis changeFormatAnalysisResult(List<String> stringResult) {
		//output_col_idx = {'id':0, 'result':1, 'oss_name':2, 'nickname':3, 'oss_version':4, 'license':5, 'concluded':6, 'main_l':7, 'scancode_l': 8,
		// 'need_review_l':9, 'dn_loc':10, 'homepage':11, 'copyright':12, 'comment':13}
		OssAnalysis ossAnalysis = new OssAnalysis();
		ossAnalysis.setGridId(stringResult.get(0));
		ossAnalysis.setGroupId(stringResult.get(0));
		ossAnalysis.setResult(stringResult.get(1));
		ossAnalysis.setOssName(stringResult.get(2));
		ossAnalysis.setOssNickname(stringResult.get(3));
		ossAnalysis.setOssVersion(versionFormatter(stringResult.get(4)));
		ossAnalysis.setLicenseName(stringResult.get(5));
		ossAnalysis.setConcludedLicense(stringResult.get(6));
		ossAnalysis.setAskalonoLicense(stringResult.get(7).replaceAll("\\(\\d+\\)", ""));
		ossAnalysis.setScancodeLicense(stringResult.get(8).replaceAll("\\(\\d+\\)", ""));
		ossAnalysis.setNeedReviewLicenseAskalono(stringResult.get(9));
		ossAnalysis.setDownloadLocation(stringResult.get(10));
		ossAnalysis.setHomepage(stringResult.get(11));
		ossAnalysis.setOssCopyright(stringResult.get(12));
		ossAnalysis.setComment(stringResult.get(13));

		return ossAnalysis;
	}
	private OssMaster returnNewestOSS (String name) {
		OssMaster param = new OssMaster();
		if(CoCodeManager.OSS_INFO_UPPER_NAMES.containsKey(name.toUpperCase())) {
			param.setOssName(CoCodeManager.OSS_INFO_UPPER_NAMES.get(name.toUpperCase()));
			List<OssMaster> ossList = ossService.getOssListByName(param);
			if (ossList != null) {
				int deactivateCnt = ossList.stream().filter(e -> e.getDeactivateFlag().equals(CoConstDef.FLAG_YES)).collect(Collectors.toList()).size();
				if (deactivateCnt > 0) {
					return null;
				}
				final Comparator<OssMaster> comp = Comparator.comparing((OssMaster o) -> o.getModifiedDate()).reversed();
				ossList = ossList.stream().sorted(comp).collect(Collectors.toList());

				return CoCodeManager.OSS_INFO_BY_ID.get(ossList.get(0).getOssId());
			}
		}
		return null;
	}
	private OssMaster ossNameInfoList (OssAnalysis resultData) {
		return returnNewestOSS(resultData.getOssName());
	}

	private List<OssMaster> nickNameInfoList (OssAnalysis resultData) {
		List<OssMaster> ossAnalysisByNickList = new ArrayList<>(); // 자동 분석 결과 OSS Nickname으로 oss name / oss nickname을 갖는 OSS

		for(String nick : resultData.getOssNickname().split(",")) {
			OssMaster om = returnNewestOSS(nick);
			if(om == null) continue;
			if (ossAnalysisByNickList.size() > 0) {
				String checkDuplicateOssName = om.getOssName();
				int checkDuplicateCnt = ossAnalysisByNickList.stream().filter(e -> e.getOssName().equalsIgnoreCase(checkDuplicateOssName)).collect(Collectors.toList()).size();
				if (checkDuplicateCnt > 0) {
					continue;
				}
			}
			ossAnalysisByNickList.add(om);
		}
		return ossAnalysisByNickList;
	}

	private String versionFormatter(String version) {
		String result = version;
		if(version.matches("^(v|V)(\\d|\\.)+")) {
			result = version.replaceAll("^(v|V)[.]", "").replaceAll("^(v|V)","");
		}
		return result;
	}

	private List<String> changeUpperCase(List<String> list){
		List<String> result = new ArrayList<>();
		for(String s : list) {
			if(!s.equals("")) {
				result.add(s.toUpperCase());
			}
		}
		return result;
	}

	private Map<String, Object> checkNewestVersion(OssMaster om, OssAnalysis oa) {
		Map<String, Object> returnValue = new HashMap<>();
		//List<String> declared = new ArrayList<>(Arrays.asList(om.getDeclaredLicense().split(",")));
		List<String> declared = new ArrayList<>();
		for(OssLicense ol : om.getOssLicenses()) {
			declared.add(ol.getLicenseName().toUpperCase());
		}
		List<String> detected = changeUpperCase(new ArrayList<>(Arrays.asList(om.getDetectedLicense().split(","))));
		List<String> scan = changeUpperCase(new ArrayList<>(Arrays.asList(oa.getScancodeLicense().split(","))));
		List<String> needReview = changeUpperCase(new ArrayList<>(Arrays.asList(oa.getNeedReviewLicenseAskalono().split(","))));
		if(!declared.isEmpty()) {
			for(String s : declared) {
				if(!scan.contains(s)) {
					returnValue.put("isValid", false);
					returnValue.put("errMsg", s + " : declared license of latest version is not included in autoanalysis result.");
					return returnValue;
				} else {
					scan.remove(s);
				}
			}
		}
		if(!detected.isEmpty()) {
			for(String s : detected) {
				if(!scan.contains(s)) {
					returnValue.put("isValid", false);
					returnValue.put("errMsg", s + " : detected license of latest version is not included in autoanalysis result.");
					return returnValue;
				} else {
					scan.remove(s);
				}
			}
		}

		//Map<String, Object> resMap = new HashMap<String, Object>();
		if(!scan.isEmpty()) {
			for(String s : scan) {
				if(needReview.contains(s)){
					returnValue.put("isValid", false);
					returnValue.put("errMsg", "There are unsaved licenses that require review : " + s);
					return returnValue;
				}
			}
		}
		returnValue.put("isValid", true);
		return returnValue;
	}

	@Override
	public Map<String , Object> finishOss( String prjId){
		Map<String, Object> returnValue = new HashMap<>();
			coReviewerProcess(prjId);
			returnValue.put("isValid", true);
			return returnValue;
	}
	@Override
	public Map<String, Object> registAnalysisOss(List<String> stringResult, Map<String, String> userDataMap, String prjId, String _token) throws ExecutionException, InterruptedException {

		Map<String, Object> resultMap = new HashMap<>();
		Future<Map<String, Object>> future = executorService.submit(new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap = singleRegistAnalysisOss(stringResult, userDataMap, prjId, _token);
				return resultMap;
			}
		});

		resultMap = future.get();
		return resultMap;
	}

	public Map<String, Object> singleRegistAnalysisOss(List<String> stringResult, Map<String, String> userDataMap, String prjId, String _token) {
		//log.info(userDataMap.get("oss_name_origin"));
		Map<String, Object> returnValue = new HashMap<>();
		Map<String, Object> condition = new HashMap<>();
		userService.checkApiUserAuthAndSetSession(_token);
		OssAnalysis resultData = changeFormatAnalysisResult(stringResult);
		OssAnalysis userData = changeFormatUserData(userDataMap);
		userData.setPrjId(prjId);
		userData.setGridId(stringResult.get(0));
		userData.setComponentId(stringResult.get(0));
		boolean isNick = false;
		boolean isName = false;
		boolean needcheck = false;
		boolean nameSameURL = false;
		String infoStr = "";

		condition = checkConditionAnalysisResult(resultData);
		if(!(boolean) condition.get("isValid")){
			log.error(userDataMap.get("oss_name_origin") + " fail condition");
			return condition;
		} else {
			condition = checkConditionLicense(resultData);
			if(!(boolean) condition.get("isValid")) {
				needcheck = true;
			}

			List<OssMaster> ossAnalysisByNickList = nickNameInfoList(resultData);
			OssMaster ossAnalysisByOssName = null;
			if(ossAnalysisByNickList != null && !ossAnalysisByNickList.isEmpty()) {
				for(OssMaster om : ossAnalysisByNickList) {
					if(om.getOssName().toUpperCase().equals(resultData.getOssName().toUpperCase())) {
						ossAnalysisByOssName = om;
						isName = true;
						ossAnalysisByNickList.remove(om);
						break;
					}
				}
				if(ossAnalysisByNickList.size() == 1) {
					isNick = true;
				} else {
					if(ossAnalysisByNickList.size() > 1 ){
						returnValue.put("isValid", false);
						returnValue.put("errMsg", "Detected multiple newest oss by nicknames");
						return returnValue;
					}
				}
			}

			if(!isName) {
				ossAnalysisByOssName = ossNameInfoList(resultData);
				if(ossAnalysisByOssName != null && !ossAnalysisByNickList.contains(ossAnalysisByOssName)) {
					isName = true;
				}
			}

			if(isName) {
				nameSameURL = hasSameURL(ossAnalysisByOssName.getOssName(),resultData);
			}

			OssMaster saveData = null;

			Map<String, Object> versionReturn = new HashMap<>();
			versionReturn.put("isValid", true);

			if(isNick) {
				if(needcheck) {
					versionReturn = checkNewestVersion(ossAnalysisByNickList.get(0), resultData);
				}

				if((boolean) versionReturn.get("isValid")) {
					saveData = ossAnalysisByNickList.get(0);

				} else {
					return versionReturn;
				}
			}

			if(isName) {
				if(saveData == null) {
					if(needcheck) {
						versionReturn = checkNewestVersion(ossAnalysisByOssName, resultData);
					}
					if((boolean) versionReturn.get("isValid")) {
						if(nameSameURL) {
							saveData = ossAnalysisByOssName;
						}
					} else {
						return versionReturn;
					}
				} else {
					infoStr += ossAnalysisByOssName.getOssName() + "(" + ossAnalysisByOssName.getOssVersion() + ") should be checked.<br>";
				}
			}

			if(saveData == null) {
				if(isName || isNick) { //nick 혹은 oss가 있는 경우
					List<String> resultNames = new ArrayList<>();
					resultNames.add(resultData.getOssName());
					for(String nick : resultData.getOssNickname().split(",")) {
						resultNames.add(nick);
					}

					List<String> detectedNames = new ArrayList<>();
					if(ossAnalysisByNickList != null && !ossAnalysisByNickList.isEmpty()) {
						detectedNames.add(ossAnalysisByNickList.get(0).getOssName());
						for(String nick : ossAnalysisByNickList.get(0).getOssNicknames()) {
							detectedNames.add(nick);
						}
					}
					if(ossAnalysisByOssName != null) {
						detectedNames.add(ossAnalysisByOssName.getOssName());
						for(String nick : ossAnalysisByOssName.getOssNicknames()) {
							detectedNames.add(nick);
						}
					}

					List<String> finalName = new ArrayList<>();
					for(String name : resultNames) {
						for(String dname : detectedNames) {
							if(name.equals(dname)) {
								finalName.add(dname);
								break;
							}
						}
					}

					for(String name : finalName) {
						resultNames.remove(name);
					}

					if(resultNames.contains(userDataMap.get("oss_nickname_github"))) {
						resultData.setOssName(userDataMap.get("oss_nickname_github"));
						resultNames.remove(userDataMap.get("oss_nickname_github"));
					} else {
						resultData.setOssName(resultNames.get(0));
						resultNames.remove(0);
					}
					resultData.setOssNickname(String.join(",", resultNames));
				}

				if(isEmpty(resultData.getLicenseName())){
					resultData.setLicenseName((String) condition.get("license"));
				}
				saveData = changeOssMaster(resultData);
				if(saveData == null) {
					returnValue.put("isValid", false);
					returnValue.put("errMsg", "fail to change analysis result to oss master");
					return returnValue;
				}
				saveData.setHomepage(userData.getHomepage());
			} else {
				String[] mergeNickname = mergeNickname(resultData, saveData.getOssNicknames());
				saveData.setOssNicknames(mergeNickname);
				saveData.setOssNickname(String.join(",",Arrays.asList(mergeNickname)));
			}

			String oss_version_download = userDataMap.get("oss_version_download");
			if(userDataMap.get("find_version").equals("")) {
				if(oss_version_download.equals("master") || oss_version_download.equals("main") || oss_version_download.equals("develop")) {
					if(avoidNull(saveData.getVersionDiffFlag()).equals("Y")) {
						returnValue.put("isValid", false);
						returnValue.put("errMsg", userData.getOssName() + " is version diff oss.");
						return returnValue;
					}
					saveData.setOssVersion("");
				} else {
					saveData.setOssVersion(oss_version_download);
				}

				if(!userData.getOssVersion().equals("")) {
					infoStr += "OSS version is saved as branch name - user input : " + userData.getOssVersion() + " anlaysis result : " + resultData.getOssVersion() + " saved : " + saveData.getOssVersion()+ "<br>";
				}
			} else {
				saveData.setOssVersion(resultData.getOssVersion());
				if(!saveData.getOssVersion().equals(versionFormatter(userData.getOssVersion()))){
					infoStr += "OSS Version is different - user input : " + userData.getOssVersion() + " saved : " + saveData.getOssVersion() + "<br>";
				}
			}

			String com = CommonFunction.lineReplaceToBR(resultData.getComment());
			saveData.setComment(com.replaceAll("\\\\n", "<br>").replaceAll("\\\\<br>","<br>"));
			saveData.setGridId(resultData.getGridId());
			saveData.setOssId("");
			returnValue = saveOssAnalysisData2(saveData, resultData);
			returnValue.put("infoMsg", infoStr);
		}
		log.info(userDataMap.get("oss_name_origin") + " success condition");
		return returnValue;
	}

	private String[] mergeNickname(OssAnalysis bean, String[] newestNickName) {
		List<String> nicknameList = new ArrayList<>();
		if(newestNickName.length != 0) {
			if(!isEmpty(bean.getOssNickname())) {
				nicknameList.addAll(Arrays.asList(bean.getOssNickname().split(",")));
				nicknameList = nicknameList.stream().filter(e -> !e.equalsIgnoreCase(bean.getOssName())).collect(Collectors.toList());
			}
			nicknameList.addAll(Arrays.asList(newestNickName));
			nicknameList = nicknameList.stream().sorted().filter(CommonFunction.distinctByKey(p -> p.trim().toUpperCase())).collect(Collectors.toList());
		} else {
			nicknameList = Arrays.asList(bean.getOssNickname());
		}
		return nicknameList.stream().toArray(String[]::new);
	}
	private boolean hasSameURL(String ossName, OssAnalysis ossAnalysis) {
		List<String> dlList = apiOssMapper.getDownloadLocationListByOssName(ossName);
		dlList = dlList.stream().distinct().collect(Collectors.toList());
		String[] dlSplit = ossAnalysis.getDownloadLocation().split("[,]");
		for(String dl : dlSplit) {
			if(dlList.contains(dl)) {
				return true;
			}
		}

		List<String> hpList = apiOssMapper.getHomepageListByOssName(ossName);
		hpList = hpList.stream().distinct().collect(Collectors.toList());
		if(hpList.contains(ossAnalysis.getHomepage())){
			return true;
		}
		return false;
	}


	private OssMaster changeOssMaster(OssAnalysis analysisBean) {
		try {
			// validator
			T2CoOssValidator validator = new T2CoOssValidator();
			validator.setAppendix("ossAnalysis", analysisBean);
			validator.setVALIDATION_TYPE(validator.VALID_OSSANALYSIS);
			T2CoValidationResult vr = validator.validate(new HashMap<>());

			if(!vr.isValid()) {
				return null;
			}
		} catch (Exception e) {
			return null;
		}

		OssMaster resultData = new OssMaster();

		if(!isEmpty(analysisBean.getOssName())) {
			resultData.setOssName(analysisBean.getOssName());
			resultData.setOssNameTemp(analysisBean.getOssName());
		}

		if(!isEmpty(analysisBean.getOssVersion())) {
			resultData.setOssVersion(analysisBean.getOssVersion());
		}

		resultData.setGridId(analysisBean.getGridId());
		resultData.setLicenseDiv(CoConstDef.LICENSE_DIV_SINGLE); // default
		// multi license 대응
		List<OssLicense> ossLicenseList = new ArrayList<>();
		int licenseIdx = 0;

		if(!isEmpty(analysisBean.getLicenseName())) {
//			for(String s : analysisBean.getLicenseName().toUpperCase().split(" OR ")) {
			// 순서가 중요
			String orGroupStr = analysisBean.getLicenseName().replaceAll("\\(", " ").replaceAll("\\)", " ");
//				boolean groupFirst = true;
			for(String s2 : orGroupStr.split(",")) {
				LicenseMaster license = CoCodeManager.LICENSE_INFO_UPPER.get(s2.trim().toUpperCase());
				OssLicense licenseBean = new OssLicense();

				if(license != null) {
					licenseBean.setOssLicenseIdx(String.valueOf(licenseIdx++));
					licenseBean.setLicenseId(license.getLicenseId());
					licenseBean.setLicenseName(license.getLicenseNameTemp());
					licenseBean.setOssLicenseComb("AND");
				} else {
					licenseBean.setOssLicenseIdx(String.valueOf(licenseIdx++));
					licenseBean.setLicenseId("");
					licenseBean.setLicenseName(s2);
					licenseBean.setOssLicenseComb("AND");
				}

				ossLicenseList.add(licenseBean);
//					groupFirst = false;
			}
//			}

			resultData.setLicenseName(analysisBean.getLicenseName());
			resultData.setOssLicenses(ossLicenseList);
		} else {
			resultData.setLicenseName("");
		}

		if(ossLicenseList.size() > 1) {
			resultData.setLicenseDiv(CoConstDef.LICENSE_DIV_MULTI);
		}

		if(!isEmpty(analysisBean.getOssNickname())) {
			// trim 처리는 registOssMaster 내에서 처리한다.
			resultData.setOssNickname(analysisBean.getOssNickname());
			resultData.setOssNicknames(analysisBean.getOssNickname().split(","));
		}

		if(!isEmpty(analysisBean.getDownloadLocation())){
			List<String> duplicateDownloadLocation = new ArrayList<>();
			String result = "";
			boolean isFirst = true;

			for(String url : analysisBean.getDownloadLocation().split(",")) {
				if(duplicateDownloadLocation.contains(url)) {
					continue;
				}

				if(!isEmpty(result)) {
					result += ",";
				}

				if(url.endsWith("/")) {
					result += url.substring(0, url.length()-1);
				} else {
					result += url;
				}

				if(isFirst) {
					resultData.setDownloadLocation(result);
					isFirst = false;
				}
			}

			resultData.setDownloadLocations(result.split(","));

		} else {
			resultData.setDownloadLocation("");
		}

		if(!isEmpty(analysisBean.getHomepage())) {
			if(analysisBean.getHomepage().endsWith("/")) {
				String homepage = analysisBean.getHomepage();
				resultData.setHomepage(homepage.substring(0, homepage.length()-1));
			} else {
				resultData.setHomepage(analysisBean.getHomepage());
			}
		} else {
			resultData.setHomepage("");
		}

		resultData.setCopyright(analysisBean.getOssCopyright());
		resultData.setSummaryDescription(analysisBean.getSummaryDescription());
		// editor를 이용하지 않고, textarea로 등록된 코멘트의 경우 br 태그로 변경
		String com = CommonFunction.lineReplaceToBR(analysisBean.getComment());

		resultData.setComment(com.replaceAll("\\\\n", "<br>").replaceAll("\\\\<br>","<br>"));
		resultData.setAddNicknameYn(CoConstDef.FLAG_YES); //nickname을 clear&insert 하지 않고, 중복제거를 한 나머지 nickname에 대해서는 add함.



		return resultData;
	}

	private Map<String, Object> checkConditionLicense (OssAnalysis resultData) {
		Map<String, Object> returnValue = new HashMap<>();
		returnValue.put("isValid", true);
		if(!isEmpty(resultData.getConcludedLicense())) { //취합 정보 license가 있음
			if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(resultData.getConcludedLicense().toUpperCase())) { // confirmed license
				returnValue.put("license",resultData.getConcludedLicense());
			} else {
				returnValue.put("isValid", false);
				returnValue.put("errMsg", "concluded license is unconfirmed license.");
				return returnValue;
			}
		} else {
			if(!isEmpty(resultData.getScancodeLicense()) && isEmpty(resultData.getAskalonoLicense())) {
				String[] scan = resultData.getScancodeLicense().split(",");
				if(scan.length == 1 && CoCodeManager.LICENSE_INFO_UPPER.containsKey(scan[0].toUpperCase())) {
					returnValue.put("license", scan[0]);
				}
			} else {
				returnValue.put("isValid", false);
				returnValue.put("errMsg", "concluded license == null or #license text/#full scan > 1");
				return returnValue;
			}
		}
		return returnValue;
	}


	private Map<String, Object> checkConditionAnalysisResult (OssAnalysis resultData) {
		Map<String, Object> returnValue = new HashMap<>();
		returnValue.put("isValid", true);
		if(resultData.getResult().toUpperCase().equals("TRUE")) { // 자동분석결과 TRUE
			if(isEmpty(resultData.getScancodeLicense())){ // 검출된 license 없음
				returnValue.put("isValid", false);
				returnValue.put("errMsg", "auto analysis result license is empty");
			}
		} else { // 자동분석 결과 FALSE
			returnValue.put("isValid", false);
			returnValue.put("errMsg", "auto analysis result is FALSE");
		}
		return returnValue;
	}

	public void coReviewerProcess(String prjId) {
		//log.info("Start Last Step --- ");
		String targetName = CoConstDef.CD_CHECK_OSS_IDENTIFICATION;
		if(prjId.contains("3rd_")){
			prjId = prjId.substring(4);
			targetName = CoConstDef.CD_CHECK_OSS_PARTNER;
		}

		coReviewerService.checkOssName(prjId, targetName);
		coReviewerService.checkLicense(prjId, targetName);
	}
}