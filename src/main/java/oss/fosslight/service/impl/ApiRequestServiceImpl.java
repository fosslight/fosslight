/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.service.ApiRequestService;

@Service
@Slf4j
public class ApiRequestServiceImpl extends CoTopComponent implements ApiRequestService {
	private static String OSORI_API_URL = CommonFunction.emptyCheckProperty("osori.server.url") + ":15443/api/v2/user/oss";
	private static String OSORI_API_DETAIL_URL = CommonFunction.emptyCheckProperty("osori.server.url") + ":13443/osori/ossDetail.do";
	
	private final Map<String, String> redirectCache = new ConcurrentHashMap<>();
	private final ExecutorService redirectExecutor = Executors.newFixedThreadPool(50);
	private final ExecutorService osoriExecutor = Executors.newFixedThreadPool(10);
	private static final int CONNECT_TIMEOUT = 1500;
    private static final int READ_TIMEOUT = 1500;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private RestTemplate sharedRestTemplate;
    private static final UriComponents OSORI_API_TEMPLATE = UriComponentsBuilder.fromHttpUrl(OSORI_API_URL)
													            .queryParam("equalFlag", CoConstDef.FLAG_NO)
													            .queryParam("page", "0")
													            .queryParam("size", "20")
													            .queryParam("sort", "name")
													            .queryParam("downloadLocation", "{downloadLocation}")
													            .build();
    
	@PostConstruct
	public void setResourcePathPrefix(){
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    	connectionManager.setMaxTotal(100);
    	connectionManager.setDefaultMaxPerRoute(50);
    	
    	CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
    	
    	HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.sharedRestTemplate = new RestTemplate(factory);
	}

	@PreDestroy
	public void shutdownExecutor() {
	    redirectExecutor.shutdown();
	    try {
	        redirectExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
	        osoriExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
	    } catch (InterruptedException e) {
	        redirectExecutor.shutdownNow();
	        osoriExecutor.shutdownNow();
	        Thread.currentThread().interrupt();
	    }
	}
	
	@Override
	public void requestRedirectUrl(List<ProjectIdentification> result, List<ProjectIdentification> redirectTargets, List<ProjectIdentification> osoriTargets, Map<String, String> ossInfoNames, Map<String, Set<String>> urlToNameMap) {
		List<Future<Void>> futures = new ArrayList<>();
		for (ProjectIdentification bean : redirectTargets) {
			futures.add(redirectExecutor.submit(() -> {
		        Pattern p = generatePattern(bean.getUrlSearchSeq(), bean.getDownloadLocation());

		        String redirectlocationUrl = resolveRedirectUrl(bean.getDownloadLocation());

		        ProjectIdentification url = new ProjectIdentification();
		        url.setDownloadLocation(redirectlocationUrl);
		        url = downloadlocationFormatter(url, bean.getUrlSearchSeq());

		        String checkName;
		        if (url.getDownloadLocation().equals(bean.getDownloadLocation()) || url.getDownloadLocation().equals(bean.getDownloadLocation() + "/")) {
		            checkName = generateCheckOSSName(bean.getUrlSearchSeq(), bean.getDownloadLocation(), p);
		        } else {
		            bean.setDownloadLocation(redirectlocationUrl);
		            bean.setOssNickName(generateCheckOSSName(bean.getUrlSearchSeq(), redirectlocationUrl, p));
		            Set<String> names = urlToNameMap.get(bean.getDownloadLocation());
		            if (names != null) {
		            	checkName = appendCheckOssName(names, ossInfoNames, bean.getOssNickName());

			            if (!isEmpty(checkName)) {
			                bean.setCheckOssList("Y");
			                bean.setRecommendedNickname(bean.getOssNickName() + "|" + generateCheckOSSName(bean.getUrlSearchSeq(), redirectlocationUrl, p));
			            } else {
			                checkName = generateCheckOSSName(bean.getUrlSearchSeq(), redirectlocationUrl, p);
			            }

			            bean.setRedirectLocation(redirectlocationUrl);
		            } else {
		            	checkName = "";
		            }
		        }

		        if (!isEmpty(checkName)) {
		            bean.setCheckName(checkName);
		            bean.setDownloadLocation(bean.getOriginalDownloadLocation());
		            if (!bean.getOssName().equals(bean.getCheckName())) {
		                bean.setCheckedEvidence(getMessage("check.evidence.exist.downloadLocation"));
		                bean.setCheckedEvidenceType("DB");
		                synchronized(result) {
		                    result.add(bean);
		                }
		            }
		        } else {
		        	synchronized(osoriTargets) {
		        		osoriTargets.add(bean);
	                }
		        }

		        return null;
		    }));
		}
		
		for (Future<Void> f : futures) {
		    try {
		        f.get();
		    } catch (Exception e) {
		        log.error("[Redirect processing failed] ", e);
		    }
		}
	}

	@Override
	public void requestOsoriUrl(List<ProjectIdentification> result, List<ProjectIdentification> osoriTargets) {
		List<Future<?>> osoriFutures = new ArrayList<>();
	    for (ProjectIdentification bean : osoriTargets) {
	        osoriFutures.add(osoriExecutor.submit(() -> {
	            processOsoriApi(bean, result);
	        }));
	    }
	    
	    for (Future<?> f : osoriFutures) {
		    try {
		        f.get();
		    } catch (Exception e) {
		        log.error("[Osori processing failed] ", e);
		    }
		}
	}
	
	private Pattern generatePattern(int urlSearchSeq, String downloadlocationUrl) {
		Pattern p = null;

		switch(urlSearchSeq) {
			case 0: // github
				if (downloadlocationUrl.contains("www.")) {
					downloadlocationUrl = downloadlocationUrl.replace("www.", "");
				}
				p = Pattern.compile("((http|https)://github.com/([^/]+)/([^/]+))");

				break;
			case 1: // npm
			case 6: // npm
				if (downloadlocationUrl.contains("/package/@")) {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+)/([^/]+))");
				}else {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+))");
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
				p = Pattern.compile("((http|https)://android.googlesource.com/(.*))");
				break;
			case 8:
				p = Pattern.compile("((http|https)://nuget.org/packages/([^/]+))");
				break;
			case 9:
				p = Pattern.compile("((http|https)://stackoverflow.com/revisions/([^/]+)/([^/]+))");
				break;
			case 11:
				p = Pattern.compile("((http|https)://crates.io/crates/([^/]+))");
				break;
			case 12 :
				p = Pattern.compile("((http|https)://git.codelinaro.org/([^/]+)/([^/]+)/(.*))");
				break;
			case 13:
				p = Pattern.compile("((http|https)://pkg.go.dev/(.*))");
				break;
			default:
				p = Pattern.compile("(.*)");
				break;
		}
		return p;
	}
	
	private String resolveRedirectUrl(String downloadLocation) {
        String cached = redirectCache.get(downloadLocation);
        if (cached != null) {
            return cached;
        }

        String finalUrl = downloadLocation;

        try {
            URL url = new URL("https://" + downloadLocation);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            int code = conn.getResponseCode();
            if (code >= 200 && code < 400) {
                finalUrl = conn.getURL().toString();
                if (finalUrl.contains("//")) {
                    finalUrl = finalUrl.split("//")[1];
                }
            }
        } catch (IOException e) {
            finalUrl = downloadLocation;
        }

        redirectCache.put(downloadLocation, finalUrl);
        return finalUrl;
	}
	
	private ProjectIdentification downloadlocationFormatter(ProjectIdentification bean, int urlSearchSeq) {
		if (urlSearchSeq == 0) {
			if (bean.getDownloadLocation().startsWith("git://")) {
				bean.setDownloadLocation(bean.getDownloadLocation().replace("git://", "https://"));
			}
			if (bean.getDownloadLocation().startsWith("git@")) {
				bean.setDownloadLocation(bean.getDownloadLocation().replace("git@", "https://"));
			}
			if (bean.getDownloadLocation().contains(".git")) {
				if (bean.getDownloadLocation().endsWith(".git")) {
					bean.setDownloadLocation(bean.getDownloadLocation().substring(0, bean.getDownloadLocation().length()-4));
				} else {
					if (bean.getDownloadLocation().contains("#")) {
						bean.setDownloadLocation(bean.getDownloadLocation().substring(0, bean.getDownloadLocation().indexOf("#")));
						bean.setDownloadLocation(bean.getDownloadLocation().substring(0, bean.getDownloadLocation().length()-4));
					}
				}
			}
		}
		String downloadlocationUrl = bean.getDownloadLocation();

		String[] downloadlocationUrlSplit = downloadlocationUrl.split("/");
		if (downloadlocationUrlSplit[downloadlocationUrlSplit.length-1].indexOf("#") > -1) {
			downloadlocationUrl = downloadlocationUrl.substring(0, downloadlocationUrl.indexOf("#"));
		}

		Pattern p = generatePattern(urlSearchSeq, downloadlocationUrl);

		Matcher m = p.matcher(downloadlocationUrl);

		while (m.find()) {
			bean.setDownloadLocation(m.group(0));
		}

		if (bean.getDownloadLocation().startsWith("http://")
				|| bean.getDownloadLocation().startsWith("https://")
				|| bean.getDownloadLocation().startsWith("git://")
				|| bean.getDownloadLocation().startsWith("ftp://")
				|| bean.getDownloadLocation().startsWith("svn://")) {
			downloadlocationUrl = bean.getDownloadLocation().split("//")[1];
		}

		if (downloadlocationUrl.startsWith("www.")) {
			downloadlocationUrl = downloadlocationUrl.substring(4, downloadlocationUrl.length());
		}

		bean.setDownloadLocation(downloadlocationUrl);
		return bean;
	}
	
	private String generateCheckOSSName(int urlSearchSeq, String downloadlocationUrl, Pattern p) {
		String checkName = "";
		String customDownloadlocationUrl = "";
		if (downloadlocationUrl.contains("?")) {
			customDownloadlocationUrl = downloadlocationUrl.split("[?]")[0];
		} else {
			customDownloadlocationUrl = downloadlocationUrl;
		}

		if(urlSearchSeq == 12 && downloadlocationUrl.contains("/-/")){
			customDownloadlocationUrl = downloadlocationUrl.split("/-/")[0];
		}

		Matcher ossNameMatcher = p.matcher("https://" + customDownloadlocationUrl);
		while (ossNameMatcher.find()){
			switch(urlSearchSeq) {
				case 0: // github
					checkName = ossNameMatcher.group(3) + "-" + ossNameMatcher.group(4);
					break;
				case 1: // npm
				case 6: // npm
					checkName = "npm:" + ossNameMatcher.group(4);
					if (checkName.contains(":@")) {
						checkName += "/" + ossNameMatcher.group(5);
					}
					break;
				case 2: // pypi
					checkName = "pypi:" + ossNameMatcher.group(3);
					break;
				case 3: // maven
					checkName = ossNameMatcher.group(3) + ":" + ossNameMatcher.group(4);
					break;
				case 4: // pub
					checkName = "pub:" + ossNameMatcher.group(3);
					break;
				case 5: // cocoapods
					checkName = "cocoapods:" + ossNameMatcher.group(3);
					break;
				case 8:
					checkName = "nuget:" + ossNameMatcher.group(3);
					break;
				case 9:
					checkName = "stackoverflow-" + ossNameMatcher.group(3);
					break;
				case 11:
					checkName = "cargo:" + ossNameMatcher.group(3);
					break;
				case 12 :
					ArrayList<String> name = new ArrayList<>();
					name.add("codelinaro");
					for(String nick : ossNameMatcher.group(5).split("/")) {
						name.add(nick);
					}
					checkName = String.join("-", name);
					break;
				case 13 :
					checkName = "go:" + ossNameMatcher.group(3).split("@")[0];
					break;
				default:
					break;
			}
		}
		return checkName;
	}
	
	private String appendCheckOssName(Set<String> ossNameList, Map<String, String> ossInfoNames, String checkOssName) {
		List<String> checkName = new ArrayList<>();

		if (ossInfoNames.containsKey(checkOssName.toUpperCase())) {
			String ossNameTemp = ossInfoNames.get(checkOssName.toUpperCase());
			checkName.add(ossNameTemp);
		}

		if (ossNameList != null && checkName.size() == 0) {
			for (String ossName : ossNameList) {
				if (!ossName.equalsIgnoreCase(checkOssName)) {
					checkName.add(ossName);
				}
			}
		}
		
		return checkName.stream().distinct().map(v->v.toString()).collect(Collectors.joining("|"));
	}
	
	@SuppressWarnings("unchecked")
	private void processOsoriApi(ProjectIdentification bean, List<ProjectIdentification> result) {
		Map<String, String> resMap = requestOsoriApi(bean.getOriginalDownloadLocation());
	    if (!MapUtils.isEmpty(resMap) && resMap.get("oss_master") != null) {
	        List<Map<String, String>> ossMasterList = OBJECT_MAPPER.convertValue(resMap.get("oss_master"), List.class);
	        if (!CollectionUtils.isEmpty(ossMasterList)) {
	            for (Map<String, String> ossMaster : ossMasterList) {
	                if (ossMaster.containsKey("name") && !bean.getOssName().equals(ossMaster.get("name"))) {
	                    bean.setCheckOssList(CoConstDef.FLAG_YES);
	                    bean.setCheckName(ossMaster.get("name"));
	                    bean.setDownloadLocation(bean.getOriginalDownloadLocation());
	                    bean.setCheckedEvidence(getMessage("check.evidence.osori.downloadLocation"));
	                    bean.setCheckedEvidenceType("OSR");
	                    bean.setLinkToPopup(OSORI_API_DETAIL_URL + "?nowPage=0&id=" + String.valueOf(ossMaster.get("oss_master_id")));
	                    result.add(bean);
	                    break;
	                }
	            }
	        }
	    }
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> requestOsoriApi(String downloadLocation) {
		try {
			URI uri = OSORI_API_TEMPLATE.expand(downloadLocation).encode().toUri();
			ResponseEntity<String> response = sharedRestTemplate.getForEntity(uri, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
	            Map<String, Object> map = OBJECT_MAPPER.readValue(response.getBody(), Map.class);
	            return (Map<String, String>) map.get("messageList");
	        }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
		
		return null;
	}
}