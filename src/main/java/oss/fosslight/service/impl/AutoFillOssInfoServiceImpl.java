/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.CoConstDef.DependencyType;
import oss.fosslight.common.CoConstDef.ExternalService;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.AutoFillOssInfoService;
import oss.fosslight.service.CommentService;
import oss.fosslight.util.CryptUtil;
import oss.fosslight.validation.T2CoValidationConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class AutoFillOssInfoServiceImpl extends CoTopComponent implements AutoFillOssInfoService {
	// Service
	@Autowired CommentService commentService;
	@Autowired WebClient webClient;

	// Mapper
	@Autowired OssMapper ossMapper;
	@Autowired ProjectMapper projectMapper;

    @Override
	public List<ProjectIdentification> checkOssLicenseData(List<ProjectIdentification> componentData, Map<String, String> validMap, Map<String, String> diffMap){
		List<ProjectIdentification> resultData = new ArrayList<ProjectIdentification>();
		Map<String, Object> ruleMap = T2CoValidationConfig.getInstance().getRuleAllMap();

		if(validMap != null) {
			for(String key : validMap.keySet()) {
				if(key.toUpperCase().startsWith("LICENSENAME")
						&& (validMap.get(key).equals(ruleMap.get("LICENSE_NAME.UNCONFIRMED.MSG"))
						|| validMap.get(key).equals(ruleMap.get("LICENSE_NAME.REQUIRED.MSG")))
						|| validMap.get(key).startsWith("Declared")) {
					resultData.addAll((List<ProjectIdentification>) componentData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList()));
				}

				if(key.toUpperCase().startsWith("OSSVERSION") && validMap.get(key).equals(ruleMap.get("OSS_VERSION.UNCONFIRMED.MSG"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList()));
				}

				if(key.toUpperCase().startsWith("OSSNAME") && validMap.get(key).equals(ruleMap.get("OSS_NAME.UNCONFIRMED.MSG"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList()));
				}
			}
		}

		if(diffMap != null) {
			for(String key : diffMap.keySet()) {
				if(key.toUpperCase().startsWith("LICENSENAME") && diffMap.get(key).equals(ruleMap.get("LICENSE_NAME.UNCONFIRMED.MSG"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList()));
				}

				if(key.toUpperCase().startsWith("OSSVERSION") && diffMap.get(key).equals(ruleMap.get("OSS_VERSION.UNCONFIRMED.MSG"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList()));
				}

				if(key.toUpperCase().startsWith("DOWNLOADLOCATION") && diffMap.get(key).equals(ruleMap.get("DOWNLOAD_LOCATION.DIFFERENT.MSG"))) {
					int duplicateRow = (int) resultData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList())
							.size();

					if(duplicateRow == 0) {
						resultData.addAll((List<ProjectIdentification>) componentData
								.stream()
								.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
								.collect(Collectors.toList()));
					}
				}
			}
		}

		return resultData;
	}

	@Override
	public List<ProjectIdentification> checkOssLicense(List<ProjectIdentification> list){
		List<ProjectIdentification> result = new ArrayList<ProjectIdentification>();

		list = list.stream().filter(CommonFunction.distinctByKey(p -> p.getOssName()+"-"+p.getDownloadLocation()+"-"+p.getOssVersion()+"-"+p.getLicenseName())).collect(Collectors.toList());

		for(ProjectIdentification bean : list) {
			List<ProjectIdentification> prjOssMasters = new ArrayList<>();
			String downloadLocation = bean.getDownloadLocation();
			String currentLicense = bean.getLicenseName();
			String ossVersion = bean.getOssVersion();
			String checkLicense = "";

			// Search Priority 1. find by oss name and oss version
			prjOssMasters = projectMapper.getOssFindByNameAndVersion(bean);
			checkLicense = makeCheckLicenseExpression(prjOssMasters);
			
			if (!isEmpty(checkLicense) && !currentLicense.equals(checkLicense)) {
				bean.setCheckLicense(checkLicense);
				result.add(bean);
				continue;
			}

			if(isEmpty(downloadLocation)) {
				continue;
			}

			// Search Priority 2. find by oss download location and version
			prjOssMasters = projectMapper.getOssFindByVersionAndDownloadLocation(bean);
			checkLicense = makeCheckLicenseExpression(prjOssMasters);

			if (!isEmpty(checkLicense) && !currentLicense.equals(checkLicense)) {
				bean.setCheckLicense(checkLicense);
				result.add(bean);
				continue;
			}
			
			// Search Priority 3. find by oss download location
			prjOssMasters = projectMapper.getOssFindByDownloadLocation(bean);
			checkLicense = makeCheckLicenseExpression(prjOssMasters);

			Boolean versionDiff = prjOssMasters.stream().allMatch(oss -> {
				return oss.getVersionDiffFlag().equals("Y");
			});

			if (!isEmpty(checkLicense) && versionDiff == false && !currentLicense.equals(checkLicense)) {
				bean.setCheckLicense(checkLicense);
				result.add(bean);
				continue;
			}

			// Search Priority 4. find by Clearly Defined And Github API
			DependencyType dependencyType = DependencyType.downloadLocationToType(bean.getDownloadLocation());

			if(dependencyType.equals(DependencyType.UNSUPPORTED)) {
				continue;
			}

			// Search Priority 4-1. Github API : empty oss version and download location
			if(ossVersion.isEmpty() && ExternalService.GITHUB_LICENSE_API.hasDependencyType(dependencyType)) {
				Matcher matcher = dependencyType.getPattern().matcher(bean.getDownloadLocation());
				String owner = "", repo = "";

				while(matcher.find()) {
					owner = matcher.group(3);
					repo = matcher.group(4);
				}

				try {
					String requestUri = ExternalService.githubLicenseRequestUri(owner, repo);
					Mono<Object> mono = requestGithubLicense(requestUri);
					Map<String, Object> ossInfo = (Map<String, Object>) mono.block();
					Map<String, String> licenseInfo = (Map<String, String>) ossInfo.get("license");
					checkLicense = licenseInfo.get("spdx_id");
				} catch (Exception e) {
					log.error("Github -> " + downloadLocation + " : " + e.getMessage());
					checkLicense = "NONE";
				}

				if(!currentLicense.equals(checkLicense) && !checkLicense.equals("NOASSERTION") && !checkLicense.equals("NONE")) {
					bean.setCheckLicense(checkLicense);
					result.add(bean);
					continue;
				}
			}

			// Search Priority 4-2. Clearly Defined : oss version and download location
			if(!ossVersion.isEmpty() && ExternalService.CLEARLY_DEFINED_DEFINITIONS_API.hasDependencyType(dependencyType)) {
				Matcher matcher = dependencyType.getPattern().matcher(downloadLocation);
				String type = dependencyType.getType();
				String provider = dependencyType.getProvider();
				String revision = ossVersion;
				String namespace = "-", name = "";

				while(matcher.find()) {
					if(dependencyType.getIsNameSpaceRequired()) {
						namespace = matcher.group(3);
						name = matcher.group(4);
					} else {
						name = matcher.group(3);
					}
				}

				if(!dependencyType.getIsNameSpaceRequired()) {
					namespace = "-";
				}

				try {
					String requestUri = ExternalService.clearlyDefinedLicenseRequestUri(type, provider, namespace, name, revision);
					Mono<Object> mono = requestClearlyDefinedLicense(requestUri);
					Map<String, Object> ossInfo = (Map<String, Object>) mono.block();
					Map<String, String> licenseInfo = (Map<String, String>) ossInfo.get("licensed");
					checkLicense = licenseInfo.get("declared");
				} catch (Exception e) {
					log.error("Clearly Defined -> " + downloadLocation + " : " + e.getMessage());
					checkLicense = "NONE";
				}

				if(!currentLicense.equals(checkLicense) && !checkLicense.equals("NOASSERTION") && !checkLicense.equals("NONE")) {
					bean.setCheckLicense(checkLicense);
					result.add(bean);
					continue;
				}
			}
		}

		// Request at once when using external API(Clearly Defined, Github API)

		return result;
	}

	@Override
	public ParallelFlux<Object> getGithubLicenses(List<String> locations) {
		return Flux.fromIterable(locations)
			.parallel()
			.runOn(Schedulers.elastic())
			.flatMap(this::requestGithubLicense);
	}

	@Override
	public Mono<Object> requestGithubLicense(String location) {

		String githubToken = "";
		try {
			githubToken = CryptUtil.decryptAES256(CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_SERVICE_SETTING, CoConstDef.CD_DTL_GITHUB_TOKEN), CoConstDef.ENCRYPT_DEFAULT_SALT_KEY);
		} catch(Exception e) {
			log.error(e.getMessage());		
		}

		return webClient.get()
			.uri(location)
			.header("Authorization", "token " + githubToken)
			.retrieve()
			.bodyToMono(Object.class);
	}

	@Override
	public ParallelFlux<Object> getClearlyDefinedLicenses(List<String> locations) {
		return Flux.fromIterable(locations)
				.parallel()
				.runOn(Schedulers.elastic())
				.flatMap(this::requestClearlyDefinedLicense);
	}

	@Override
	public Mono<Object> requestClearlyDefinedLicense(String location) {
		return webClient.get()
				.uri(location)
				.retrieve()
				.bodyToMono(Object.class);
	}

	private String makeCheckLicenseExpression(List<ProjectIdentification> prjOssMasters) {
		String checkLicense = "";
		List<ProjectIdentification> licenses = new ArrayList<>();;

		for(ProjectIdentification prjOssMaster : prjOssMasters) {

			if(!isEmpty(checkLicense)) {
				checkLicense += "|";
			}

			licenses = projectMapper.getLicenses(prjOssMaster);
			checkLicense += makeLicenseExpression(licenses);
		}
		return checkLicense;
	}

	private String makeLicenseExpression(List<ProjectIdentification> licenses) {
		String license = "";

		if(licenses.size() != 0){
			licenses = CommonFunction.makeLicenseExcludeYn(licenses);
			license = CommonFunction.makeLicenseExpressionIdentify(licenses, ",");
		}

		return license;
	}

	@Transactional
	@Override
	public Map<String, Object> saveOssCheckLicense(ProjectIdentification paramBean, String targetName) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int updateCnt = 0;

			switch(targetName.toUpperCase()) {
				case CoConstDef.CD_CHECK_OSS_NAME_SELF:
					String[] gridId = paramBean.getGridId().split("-");
					paramBean.setGridId(gridId[0]+"-"+gridId[1]);

					if(paramBean.getOssVersion() == null) paramBean.setOssVersion("");
					
					updateCnt = ossMapper.updateOssCheckLicenseBySelfCheck(paramBean);
					
					break;
				case CoConstDef.CD_CHECK_OSS_NAME_IDENTIFICATION:
					updateCnt = ossMapper.updateOssCheckLicense(paramBean);

					if(updateCnt >= 1) {
						String commentId = paramBean.getReferenceId();
						String checkOssLicenseComment = "";
						String changeOssLicenseInfo = "<p>" + paramBean.getLicenseName() + " => " + paramBean.getCheckLicense() + "</p>";
						CommentsHistory commentInfo = null;

						if(isEmpty(commentId)) {
							checkOssLicenseComment  = "<p><b>The following open source and license names will be changed to names registered on the system for efficient management.</b></p>";
							checkOssLicenseComment += "<p><b>Opensource Names</b></p>";
							checkOssLicenseComment += changeOssLicenseInfo;
							CommentsHistory commHisBean = new CommentsHistory();
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commHisBean.setReferenceId(paramBean.getRefPrjId());
							commHisBean.setContents(checkOssLicenseComment);
							commentInfo = commentService.registComment(commHisBean, false);
						} else {
							commentInfo = (CommentsHistory) commentService.getCommnetInfo(commentId).get("info");

							if(commentInfo != null) {
								if(!isEmpty(commentInfo.getContents())) {
									checkOssLicenseComment  = commentInfo.getContents();
									checkOssLicenseComment += changeOssLicenseInfo;
									commentInfo.setContents(checkOssLicenseComment);

									commentService.updateComment(commentInfo, false);
								}
							}
						}

						if(commentInfo != null) {
							map.put("commentId", commentInfo.getCommId());
						}
					}

					break;
			}

			if(updateCnt >= 1) {
				map.put("isValid", true);
				map.put("returnType", "Success");
			} else {
				throw new Exception("update Cnt가 비정상적인 값임.");
			}
		} catch (Exception e) {
			log.error(e.getMessage());

			map.put("isValid", false);
			map.put("returnType", "");
		}

		return map;
	}
}