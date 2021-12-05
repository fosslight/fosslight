/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.DependencyType;
import oss.fosslight.common.ExternalLicenseServiceType;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.AutoFillOssInfoService;
import oss.fosslight.service.CommentService;
import oss.fosslight.util.CryptUtil;
import oss.fosslight.util.StringUtil;
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
	@Autowired MessageSource messageSource;

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
	public List<ProjectIdentification> checkOssLicense(List<ProjectIdentification> ossList){
		List<ProjectIdentification> result = new ArrayList<>();

		// oss name,과 download location이 동일한 oss의 componentId를 묶어서 List<ProjectIdentification>을 만듬
		ossList = ossList.stream()
				.collect(Collectors.groupingBy(oss -> oss.getOssName() + "-" + oss.getDownloadLocation() + "-" + oss.getOssVersion() + "-" + oss.getLicenseName()))
				.values().stream()
				.map(list -> {
					ProjectIdentification uniqueOss = list.stream().distinct().findFirst().get();
					List<String> componentIds = list.stream().map(oss -> oss.getComponentId()).distinct().collect(Collectors.toList());
					uniqueOss.setComponentIdList(componentIds);
					return uniqueOss;
				})
				.collect(Collectors.toList());

		for(ProjectIdentification oss : ossList) {
			List<ProjectIdentification> prjOssLicenses;
			String downloadLocation = oss.getDownloadLocation();
			String ossVersion = oss.getOssVersion();
			String currentLicense = oss.getLicenseName();
			String checkedLicense = "";

			// Search Priority 1. find by oss name and oss version
			prjOssLicenses = projectMapper.getOssFindByNameAndVersion(oss);
			checkedLicense = combineOssLicenses(prjOssLicenses);

			if (!checkedLicense.isEmpty() && !currentLicense.equals(checkedLicense)) {
				String evidence = getMessage("check.evidence.exist.nameAndVersion");
				oss.setCheckOssList("Y");
				oss.setCheckLicense(checkedLicense);
				oss.setCheckedEvidence(evidence);
				oss.setCheckedEvidenceType("DB");
				result.add(oss);
				continue;
			}

			if(downloadLocation.isEmpty()) {
				continue;
			}

			// Search Priority 2. find by oss download location and version
			prjOssLicenses = projectMapper.getOssFindByVersionAndDownloadLocation(oss);
			checkedLicense = combineOssLicenses(prjOssLicenses);

			if (!checkedLicense.isEmpty() && !currentLicense.equals(checkedLicense)) {
				String evidence = getMessage("check.evidence.exist.downloadLocationAndVersion");
				oss.setCheckOssList("Y");
				oss.setCheckLicense(checkedLicense);
				oss.setCheckedEvidence(evidence);
				oss.setCheckedEvidenceType("DB");
				result.add(oss);
				continue;
			}
			
			// Search Priority 3. find by oss download location
			prjOssLicenses = projectMapper.getOssFindByDownloadLocation(oss).stream()
					.filter(CommonFunction.distinctByKeys(
							ProjectIdentification::getOssName,
							ProjectIdentification::getLicenseName
					))
					.collect(Collectors.toList());
			checkedLicense = combineOssLicenses(prjOssLicenses);

			if (!checkedLicense.isEmpty() && !currentLicense.equals(checkedLicense) && !isEachOssVersionDiff(prjOssLicenses)) {
				String evidence = getMessage("check.evidence.exist.downloadLocation");
				oss.setCheckOssList("Y");
				oss.setCheckLicense(checkedLicense);
				oss.setCheckedEvidence(evidence);
				oss.setCheckedEvidenceType("DB");
				result.add(oss);
				continue;
			}

			// Search Priority 4. find by Clearly Defined And Github API
			DependencyType dependencyType = DependencyType.downloadLocationToType(downloadLocation);

			if(dependencyType.equals(DependencyType.UNSUPPORTED)) {
				continue;
			}

			// Search Priority 4-1. Github API : empty oss version and download location
			if(ossVersion.isEmpty() && ExternalLicenseServiceType.GITHUB.hasDependencyType(dependencyType)) {
				Matcher matcher = dependencyType.getPattern().matcher(downloadLocation);
				String owner = "", repo = "";

				while(matcher.find()) {
					owner = matcher.group(3);
					repo = matcher.group(4);
				}

				String requestUri = ExternalLicenseServiceType.githubLicenseRequestUri(owner, repo);
				checkedLicense = requestGithubLicenseApi(requestUri);

				if(!currentLicense.equals(checkedLicense) && !checkedLicense.equals("NOASSERTION") && !checkedLicense.equals("NONE")) {
					String evidence = getMessage("check.evidence.github.downloadLocation");
					oss.setCheckOssList("Y");
					oss.setCheckLicense(checkedLicense);
					oss.setCheckedEvidence(evidence);
					oss.setCheckedEvidenceType("GH");
					result.add(oss);
					continue;
				}
			}

			// Search Priority 4-2. Clearly Defined : oss version and download location
			if(!ossVersion.isEmpty() && ExternalLicenseServiceType.CLEARLY_DEFINED.hasDependencyType(dependencyType)) {
				Matcher matcher = dependencyType.getPattern().matcher(downloadLocation);
				String type = dependencyType.getType();
				String provider = dependencyType.getProvider();
				String revision = ossVersion;
				String namespace = "", name = "";

				while(matcher.find()) {
					if(dependencyType.equals(DependencyType.MAVEN_CENTRAL) || dependencyType.equals(DependencyType.MAVEN_GOOGLE)) {
						namespace = matcher.group(3);
						name = matcher.group(4);
					} else {
						namespace = "-";
						name = matcher.group(3);
					}
				}

				String requestUri = ExternalLicenseServiceType.clearlyDefinedLicenseRequestUri(type, provider, namespace, name, revision);
				checkedLicense = requestClearlyDefinedLicenseApi(requestUri);

				if(!currentLicense.equals(checkedLicense) && !checkedLicense.equals("NOASSERTION") && !checkedLicense.equals("NONE")) {
					String evidence = getMessage("check.evidence.clearlyDefined.downloadLocationAndVersion");
					oss.setCheckOssList("Y");
					oss.setCheckLicense(checkedLicense);
					oss.setCheckedEvidence(evidence);
					oss.setCheckedEvidenceType("CD");
					result.add(oss);
					continue;
				}
			}
		}

		// TODO : Request at once when using external API(Clearly Defined, Github API)



		/* grouping same oss */
		final Comparator<ProjectIdentification> comp = (p1, p2) -> p1.getDownloadLocation().compareTo(p2.getDownloadLocation());
		
		// oss name, oss version, oss license, checked license는 unique하게 출력
		List<ProjectIdentification> sortedData = result.stream()
				.filter(CommonFunction.distinctByKeys(
						ProjectIdentification::getOssName,
						ProjectIdentification::getOssVersion,
						ProjectIdentification::getLicenseName,
						ProjectIdentification::getCheckLicense
				))
				.sorted(comp)
				.collect(Collectors.toList());
		
		// oss name, oss version, oss license와 checked license가 unique하지 않다면 중복된 data의 downloadlocation을 전부 합쳐서 출력함. 
		for(ProjectIdentification p : sortedData) {
			String downloadLocation = result.stream()
					.filter(e -> e.getOssName().equals(p.getOssName()))
					.filter(e -> e.getOssVersion().equals(p.getOssVersion()))
					.filter(e -> e.getLicenseName().equals(p.getLicenseName()))
					.filter(e -> e.getCheckLicense().equals(p.getCheckLicense()))
					.map(e -> e.getDownloadLocation())
					.collect(Collectors.joining(","));

			List<String> componentIds = result.stream()
					.filter(e -> e.getOssName().equals(p.getOssName()))
					.filter(e -> e.getOssVersion().equals(p.getOssVersion()))
					.filter(e -> e.getLicenseName().equals(p.getLicenseName()))
					.filter(e -> e.getCheckLicense().equals(p.getCheckLicense()))
					.map(e -> e.getComponentIdList())
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
			
			p.setDownloadLocation(downloadLocation);
			p.setComponentIdList(componentIds);
		}

		return sortedData;
	}

	private String requestClearlyDefinedLicenseApi(String requestUri) {
		String checkedLicense;
		try {
			Mono<Object> mono = requestClearlyDefinedLicense(requestUri);
			Map<String, Object> ossInfo = (Map<String, Object>) mono.block();
			Map<String, String> licenseInfo = (Map<String, String>) ossInfo.get("licensed");
			checkedLicense = licenseInfo.get("declared");
		} catch (Exception e) {
			log.error("Clearly Defined -> " + requestUri + " : " + e.getMessage());
			checkedLicense = "NONE";
		}
		return checkedLicense;
	}

	private String requestGithubLicenseApi(String requestUri) {
		String checkedLicense;
		try {
			Mono<Object> mono = requestGithubLicense(requestUri);
			Map<String, Object> ossInfo = (Map<String, Object>) mono.block();
			Map<String, String> licenseInfo = (Map<String, String>) ossInfo.get("license");
			checkedLicense = licenseInfo.get("spdx_id");
		} catch (Exception e) {
			log.error("Github Request -> " + requestUri + " : " + e.getMessage());
			checkedLicense = "NONE";
		}
		return checkedLicense;
	}

	private Boolean isEachOssVersionDiff(List<ProjectIdentification> prjOssMasters) {
		return prjOssMasters.stream().allMatch(oss -> oss.getVersionDiffFlag().equals("Y"));
	}

	private String combineOssLicenses(List<ProjectIdentification> prjOssMasters) {
		String checkLicense = "";
		List<ProjectIdentification> licenses;

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

	@Override
	public ParallelFlux<Object> getGithubLicenses(List<String> locations) {
		return Flux.fromIterable(locations)
			.parallel()
			.runOn(Schedulers.elastic())
			.flatMap(this::requestGithubLicense);
	}

	@Override
	public Mono<Object> requestGithubLicense(String location) {
		String githubToken = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_SERVICE_SETTING, CoConstDef.CD_DTL_GITHUB_TOKEN);

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

	@Transactional
	@Override
	public Map<String, Object> saveOssCheckLicense(ProjectIdentification paramBean, String targetName) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int updateCnt = 0;

			List<String> componentIds = paramBean.getComponentIdList();

			switch(targetName.toUpperCase()) {
				case CoConstDef.CD_CHECK_OSS_NAME_SELF:
					for(String componentId : componentIds) {
						String[] gridId = componentId.split("-");
						paramBean.setGridId(gridId[0]+"-"+gridId[1]);
						paramBean.setComponentId(gridId[2]);

						if(paramBean.getOssVersion() == null) paramBean.setOssVersion("");
						
						updateCnt += ossMapper.updateOssCheckLicenseBySelfCheck(paramBean);
					}
					
					break;
				case CoConstDef.CD_CHECK_OSS_NAME_IDENTIFICATION:
					for(String componentId : componentIds) {
						paramBean.setComponentId(componentId);
						updateCnt += ossMapper.updateOssCheckLicense(paramBean);
					}

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