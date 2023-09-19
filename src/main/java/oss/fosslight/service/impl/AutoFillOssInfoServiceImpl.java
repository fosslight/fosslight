/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.service.impl;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.DependencyType;
import oss.fosslight.common.ExternalLicenseServiceType;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.SelfCheckMapper;
import oss.fosslight.service.AutoFillOssInfoService;
import oss.fosslight.service.CommentService;
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
	@Autowired SelfCheckMapper selfCheckMapper;
	@Autowired PartnerMapper partnerMapper;

    @Override
	public List<ProjectIdentification> checkOssLicenseData(List<ProjectIdentification> componentData, Map<String, String> validMap, Map<String, String> diffMap){
		List<ProjectIdentification> resultData = new ArrayList<ProjectIdentification>();
		Map<String, Object> ruleMap = T2CoValidationConfig.getInstance().getRuleAllMap();

		if (validMap != null) {
			for (String key : validMap.keySet()) {
				if (key.toUpperCase().startsWith("LICENSENAME")
						&& (validMap.get(key).equals(ruleMap.get("LICENSE_NAME.UNCONFIRMED.MSG"))
						|| validMap.get(key).equals(ruleMap.get("LICENSE_NAME.REQUIRED.MSG"))
						|| validMap.get(key).equals(ruleMap.get("LICENSE_NAME.NOLICENSE.MSG"))
						|| validMap.get(key).equals(ruleMap.get("LICENSE_NAME.INCLUDE_MULTI_OPERATE.MSG"))
						|| validMap.get(key).equals(ruleMap.get("LICENSE_NAME.INCLUDE_DUAL_OPERATE.MSG"))
						|| validMap.get(key).startsWith("Declared"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList()));
				}
			}
		}

		if (diffMap != null) {
			for (String key : diffMap.keySet()) {
				if (key.toUpperCase().startsWith("LICENSENAME") && 
						(diffMap.get(key).equals(ruleMap.get("LICENSE_NAME.UNCONFIRMED.MSG"))
								|| diffMap.get(key).startsWith("Declared")
								|| diffMap.get(key).startsWith("Recommended"))) {
					resultData.addAll((List<ProjectIdentification>) componentData
							.stream()
							.filter(e -> key.split("\\.")[1].equals(e.getComponentId())) // 동일한 componentId을 filter
							.collect(Collectors.toList()));
				}
			}
		}

		return resultData;
	}

	@SuppressWarnings("unused")
	@Override
	public Map<String, Object> checkOssLicense(List<ProjectIdentification> ossList){
		Map<String, Object> resMap = new HashMap<>();
		List<ProjectIdentification> result = new ArrayList<>();
		List<String> errors = new ArrayList<>();

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

		// external api health check
		boolean isGitHubApiHealth = false;
		boolean isClearlyDefinedApiHealth = false;

		try {
			isGitHubApiHealth = isGitHubApiHealth();
		} catch (HttpServerErrorException e) {
			errors.add(e.getStatusText());
		}

		try {
			isClearlyDefinedApiHealth = isClearlyDefinedApiHealth();
		} catch (HttpServerErrorException e) {
			errors.add(e.getStatusText());
		}

		List<String> checkedLicenseList;
		
		for (ProjectIdentification oss : ossList) {
			checkedLicenseList = null;
			
			List<ProjectIdentification> prjOssLicenses;
			String downloadLocation = oss.getDownloadLocation();
			String ossVersion = oss.getOssVersion();
			String currentLicense = getLicenseNameSort(oss.getLicenseName());
			String checkedLicense = "";
			String checkedLicense1 = "";
			String checkedLicense2 = "";
			String checkedLicense3 = "";
			
			// Search Priority 1. find by oss name and oss version
			prjOssLicenses = projectMapper.getOssFindByNameAndVersion(oss);
			checkedLicense1 = combineOssLicenses(prjOssLicenses, currentLicense);

			if (!downloadLocation.isEmpty()) {
				oss.setDownloadLocation(URLDecoder.decode(oss.getDownloadLocation()));
				if (oss.getDownloadLocation().contains(";")) {
					oss.setDownloadLocation(oss.getDownloadLocation().split(";")[0]);
				}
				
				if (oss.getDownloadLocation().startsWith("git@")){
					oss.setDownloadLocation(oss.getDownloadLocation().split("@")[1]);
				}
				
				if (oss.getDownloadLocation().startsWith("http://") 
						|| oss.getDownloadLocation().startsWith("https://")
						|| oss.getDownloadLocation().startsWith("git://")
						|| oss.getDownloadLocation().startsWith("ftp://")
						|| oss.getDownloadLocation().startsWith("svn://")) {
					oss.setDownloadLocation(oss.getDownloadLocation().split("//")[1]);
				}
				
				if (oss.getDownloadLocation().startsWith("www.")) {
					oss.setDownloadLocation(oss.getDownloadLocation().substring(5, oss.getDownloadLocation().length()));
				}
				
				if (oss.getDownloadLocation().contains(".git")) {
					if (oss.getDownloadLocation().endsWith(".git")) {
						oss.setDownloadLocation(oss.getDownloadLocation().substring(0, oss.getDownloadLocation().length()-4));
					} else {
						if (oss.getDownloadLocation().contains("#")) {
							oss.setDownloadLocation(oss.getDownloadLocation().substring(0, oss.getDownloadLocation().indexOf("#")));
							oss.setDownloadLocation(oss.getDownloadLocation().substring(0, oss.getDownloadLocation().length()-4));
						}
					}
				}
				
				String[] downloadlocationUrlSplit = oss.getDownloadLocation().split("/");
				if (downloadlocationUrlSplit[downloadlocationUrlSplit.length-1].indexOf("#") > -1) {
					oss.setDownloadLocation(oss.getDownloadLocation().substring(0, oss.getDownloadLocation().indexOf("#")));
				}
				
				// Search Priority 2. find by oss download location and version
				prjOssLicenses = projectMapper.getOssFindByVersionAndDownloadLocation(oss);
				checkedLicense2 = combineOssLicenses(prjOssLicenses, currentLicense);

				// Search Priority 3. find by oss download location
				prjOssLicenses = projectMapper.getOssFindByDownloadLocation(oss).stream()
						.filter(CommonFunction.distinctByKeys(
								ProjectIdentification::getOssName,
								ProjectIdentification::getLicenseName
						))
						.collect(Collectors.toList());
				checkedLicense3 = combineOssLicenses(prjOssLicenses, currentLicense);
			}

			oss.setDownloadLocation(downloadLocation);
			
			if (!isEmpty(checkedLicense1)) {
				if (!currentLicense.equals(checkedLicense1)) {
					String evidence = getMessage("check.evidence.exist.nameAndVersion");
					oss.setCheckOssList("Y");
					oss.setCheckLicense(checkedLicense1);
					oss.setCheckedEvidence(evidence);
					oss.setCheckedEvidenceType("DB");
					result.add(oss);
				}
				continue;
			}
			
			if (!isEmpty(checkedLicense2)) {
				if (!currentLicense.equals(checkedLicense2)) {
					String evidence = getMessage("check.evidence.exist.downloadLocationAndVersion");
					oss.setCheckOssList("Y");
					oss.setCheckLicense(checkedLicense2);
					oss.setCheckedEvidence(evidence);
					oss.setCheckedEvidenceType("DB");
					oss.setDownloadLocation(downloadLocation);
					result.add(oss);
				}
				continue;
			}
			
			if (!isEmpty(checkedLicense3)) {
				if (!currentLicense.equals(checkedLicense3)) {
					String evidence = getMessage("check.evidence.exist.downloadLocation");
					oss.setCheckOssList("Y");
					oss.setCheckLicense(checkedLicense3);
					oss.setCheckedEvidence(evidence);
					oss.setCheckedEvidenceType("DB");
					oss.setDownloadLocation(downloadLocation);
					result.add(oss);
				}
				continue;
			}

			// Search Priority 4. find by Clearly Defined And Github API
			DependencyType dependencyType = DependencyType.downloadLocationToType(downloadLocation);

			if (dependencyType.equals(DependencyType.UNSUPPORTED) || !isExternalServiceEnable()) {
				continue;
			}

			// Search Priority 4-1. Github API : empty oss version and download location
			if (ossVersion.isEmpty() && ExternalLicenseServiceType.GITHUB.hasDependencyType(dependencyType) && isGitHubApiHealth) {
				Matcher matcher = dependencyType.getPattern().matcher(downloadLocation);
				String owner = "", repo = "";

				while (matcher.find()) {
					owner = matcher.group(3);
					repo = matcher.group(4);
				}

				String requestUri = ExternalLicenseServiceType.githubLicenseRequestUri(owner, repo);
				checkedLicense = avoidNull(requestGithubLicenseApi(requestUri));

				if (!currentLicense.equals(checkedLicense) && !checkedLicense.equals("NOASSERTION") && !checkedLicense.equals("NONE") && !checkedLicense.isEmpty()) {
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
			if (!ossVersion.isEmpty() && ExternalLicenseServiceType.CLEARLY_DEFINED.hasDependencyType(dependencyType) && isClearlyDefinedApiHealth) {
				Matcher matcher = dependencyType.getPattern().matcher(downloadLocation);
				String type = dependencyType.getType();
				String provider = dependencyType.getProvider();
				String revision = ossVersion;
				String namespace = "", name = "";

				while (matcher.find()) {
					if (dependencyType.equals(DependencyType.MAVEN_CENTRAL) || dependencyType.equals(DependencyType.MAVEN_GOOGLE)) {
						namespace = matcher.group(3);
						name = matcher.group(4);
					} else {
						namespace = "-";
						name = matcher.group(3);
					}
				}

				String requestUri = ExternalLicenseServiceType.clearlyDefinedLicenseRequestUri(type, provider, namespace, name, revision);
				checkedLicense = avoidNull(requestClearlyDefinedLicenseApi(requestUri));

				if (!currentLicense.equals(checkedLicense) && !checkedLicense.equals("NOASSERTION") && !checkedLicense.equals("NONE") && !checkedLicense.isEmpty()) {
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
		for (ProjectIdentification p : sortedData) {
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

		resMap.put("checkedData", sortedData);

		if (errors.size() > 0) {
			resMap.put("error", getMessage("external.service.connect.fail", new Object[]{errors}));
		}

		return resMap;
	}

	private String getLicenseNameSort(String licenseName) {
    	String sortedValue = "";
		
		String splitLicenseNm[] = licenseName.split(",");
		Arrays.sort(splitLicenseNm);
		
		for (int i=0; i< splitLicenseNm.length; i++) {
			sortedValue += splitLicenseNm[i];
			if (i<splitLicenseNm.length-1) {
				sortedValue += ",";
			}
		}
		
		return sortedValue;
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

	private Boolean isExternalServiceEnable() {
		return "Y".equalsIgnoreCase(CommonFunction.getProperty("external.service.useflag"));
	}

	private boolean isGitHubApiHealth() throws HttpServerErrorException {
		try {
			requestGithubLicense("https://api.github.com/").block();
		} catch(HttpServerErrorException e) {
			String message = "GitHub ";
			if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
				message += getMessage("api.token.invalid");
				throw new HttpServerErrorException(e.getStatusCode(), message);
			}
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
				message += getMessage("api.server.connect.limit");
				throw new HttpServerErrorException(e.getStatusCode(), message);
			}
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				message += getMessage("api.server.connect.fail");
				throw new HttpServerErrorException(e.getStatusCode(), message);
			}
		}
		return true;
	}

	private boolean isClearlyDefinedApiHealth() throws HttpServerErrorException {
		Map<String, Object> res = new HashMap<>();

		try {
			res = (Map<String, Object>) requestClearlyDefinedLicense("https://api.clearlydefined.io/").block();
		} catch(HttpServerErrorException e) {
			String message = "ClearlyDefined ";
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
				message += getMessage("api.server.connect.limit");
				throw new HttpServerErrorException(e.getStatusCode(), message);
			}
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				message += getMessage("api.server.connect.fail");
				throw new HttpServerErrorException(e.getStatusCode(), message);
			}
		}
		return res.get("status").equals("OK");
	}

	private String combineOssLicenses(List<ProjectIdentification> prjOssMasters, String currentLicense) {
		String checkLicense = "";
		List<ProjectIdentification> licenses;
		prjOssMasters = prjOssMasters.stream().filter(CommonFunction.distinctByKey(p -> p.getOssId())).collect(Collectors.toList());
		
		for (ProjectIdentification prjOssMaster : prjOssMasters) {

			if (!isEmpty(checkLicense)) {
				checkLicense += "|";
			}

			licenses = projectMapper.getLicenses(prjOssMaster);
			checkLicense += makeLicenseExpression(licenses, currentLicense);
		}
		return checkLicense;
	}

	private String makeLicenseExpression(List<ProjectIdentification> licenses, String currentLicense) {
		String license = "";

		if (licenses.size() != 0){
			licenses = CommonFunction.makeLicensePermissiveList(licenses, currentLicense);
			licenses = CommonFunction.makeLicenseExcludeYn(licenses);
			licenses.sort(Comparator.comparing(ProjectIdentification::getLicenseName));
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
		String githubToken = CommonFunction.getProperty("external.service.github.token");

		return webClient.get()
			.uri(location)
			.header("Authorization", "token " + githubToken)
			.exchange()
			.flatMap(response -> {
				HttpStatus statusCode = response.statusCode();
				if (statusCode.is4xxClientError()) {
					return Mono.error(new HttpServerErrorException(statusCode));
				}else if (statusCode.is5xxServerError()) {
					return Mono.error(new HttpServerErrorException(statusCode));
				}
				return Mono.just(response);
			})
			.retry (1)
			.flatMap(response -> response.bodyToMono(Object.class));
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
                .exchange()
				.flatMap(response -> {
					HttpStatus statusCode = response.statusCode();
					if (statusCode.is4xxClientError()) {
						return Mono.error(new HttpServerErrorException(statusCode));
					}else if (statusCode.is5xxServerError()) {
						return Mono.error(new HttpServerErrorException(statusCode));
					}
					return Mono.just(response);
				})
				.retry (1)
				.flatMap(response -> response.bodyToMono(Object.class));
	}

	@Transactional
	@Override
	public Map<String, Object> saveOssCheckLicense(ProjectIdentification paramBean, String targetName) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int updateCnt = 0;

			List<String> componentIds = paramBean.getComponentIdList();

			for (String componentId : componentIds) {
				OssComponents oc = new OssComponents();
				oc.setComponentId(componentId);
				switch(targetName.toUpperCase()) {
					case CoConstDef.CD_CHECK_OSS_SELF:
						selfCheckMapper.deleteOssComponentsLicense(oc);
						break;
					case CoConstDef.CD_CHECK_OSS_PARTNER:
						partnerMapper.deleteOssComponentsLicense(oc);
						break;
					case CoConstDef.CD_CHECK_OSS_IDENTIFICATION:
						projectMapper.deleteOssComponentsLicense(oc);
						break;
				}
								
				String[] checkLicense = paramBean.getCheckLicense().split(",");
				String licenseDev = checkLicense.length > 1 ? CoConstDef.LICENSE_DIV_MULTI : CoConstDef.LICENSE_DIV_SINGLE;
				
				for (String licenseName : checkLicense) {
					ProjectIdentification comLicense = new ProjectIdentification();
					comLicense.setComponentId(componentId);
					comLicense.setLicenseName(licenseName);
					OssComponentsLicense license = CommonFunction.reMakeLicenseBean(comLicense, licenseDev);
					switch(targetName.toUpperCase()) {
						case CoConstDef.CD_CHECK_OSS_SELF:
							selfCheckMapper.registComponentLicense(license);
							break;
						case CoConstDef.CD_CHECK_OSS_PARTNER:
							partnerMapper.insertOssComponentsLicense(license);
							break;
						case CoConstDef.CD_CHECK_OSS_IDENTIFICATION:
							projectMapper.registComponentLicense(license);
							break;
					}
					
					updateCnt++;
				}
			}
			
			if (CoConstDef.CD_CHECK_OSS_PARTNER.equals(targetName.toUpperCase())
					|| CoConstDef.CD_CHECK_OSS_IDENTIFICATION.equals(targetName.toUpperCase())) {
				if (updateCnt >= 1) {
					String commentId = CoConstDef.CD_CHECK_OSS_PARTNER.equals(targetName.toUpperCase()) ? paramBean.getRefPrjId() : paramBean.getReferenceId();
					String checkOssLicenseComment = "";
					String changeOssLicenseInfo = "<p>" + paramBean.getOssName();

					if (!paramBean.getOssVersion().isEmpty()) {
						changeOssLicenseInfo += " (" + paramBean.getOssVersion() + ") ";
					} else {
						changeOssLicenseInfo += " ";
					}

					changeOssLicenseInfo += paramBean.getDownloadLocation() + " "
							+ paramBean.getLicenseName() + " => " + paramBean.getCheckLicense() + "</p>";
					CommentsHistory commentInfo = null;

					if (isEmpty(commentId)) {
						checkOssLicenseComment  = "<p><b>The following Licenses were modified by \"Check License\"</b></p>";
						checkOssLicenseComment += changeOssLicenseInfo;
						CommentsHistory commHisBean = new CommentsHistory();
						
						if (CoConstDef.CD_CHECK_OSS_PARTNER.equals(targetName.toUpperCase())) {
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_HIS);
							commHisBean.setReferenceId(paramBean.getReferenceId());
						}else {
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
							commHisBean.setReferenceId(paramBean.getRefPrjId());
						}
						
						commHisBean.setContents(checkOssLicenseComment);
						commentInfo = commentService.registComment(commHisBean, false);
					} else {
						commentInfo = (CommentsHistory) commentService.getCommnetInfo(commentId).get("info");

						if (commentInfo != null) {
							if (!isEmpty(commentInfo.getContents())) {
								checkOssLicenseComment  = commentInfo.getContents();
								checkOssLicenseComment += changeOssLicenseInfo;
								commentInfo.setContents(checkOssLicenseComment);

								commentService.updateComment(commentInfo, false);
							}
						}
					}

					if (commentInfo != null) {
						map.put("commentId", commentInfo.getCommId());
					}
				}
			}
			
			if (updateCnt >= 1) {
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