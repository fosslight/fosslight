package oss.fosslight.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.NvdDataMapper;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.StringUtil;

@Service("NvdDataService")
@Slf4j
public class NvdDataService extends CoTopComponent {
	static final Logger schlog = LoggerFactory.getLogger("SCHEDULER_LOG");
	
	private final String NVD_META_REST_URL = "https://services.nvd.nist.gov/rest/json/cpematch/2.0";
	private final String NVD_CVE_REST_URL = "https://services.nvd.nist.gov/rest/json/cves/2.0";
	private final String NVD_API_KEY = CommonFunction.getProperty("nvd.nist.gov.api.key");

	private final int BATCH_SIZE = 1000;
	private static final String NVD_REST_BASE_URL = "https://services.nvd.nist.gov";
	private static final String NVD_REST_CPE_MATCH_URL = "/rest/json/cpematch/2.0";
	private static final String NVD_REST_CPE_URL = "/rest/json/cves/2.0";
	private static final int API_MATCH_CHUNK_SIZE = 150; // 500;
	private static final int API_CPE_CHUNK_SIZE = 1000; // 2000;
	
	private String lastModStartDate;
	private String lastModEndDate;

	boolean initializeFlag = false;

	@Autowired NvdDataMapper nvdDataMapper;
	@Autowired CodeMapper codeMapper;
	@Autowired Environment env;
	@Autowired SqlSessionFactory sqlSessionFactory;
	
	public String executeNvdDataSync() throws IOException {
		
		// check initialize flag
		if ("Y".equalsIgnoreCase(codeMapper.getCodeDtlNm("990", "100")) ) {
			initializeFlag = true;
		}

		if (!initializeFlag) {
			Date today = new Date();
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		    sdformat.setTimeZone(TimeZone.getTimeZone("UTC"));

			Calendar cal = Calendar.getInstance();
		    cal.setTime(today);
		    cal.add(Calendar.HOUR, -1);

		    String endTime = sdformat.format(cal.getTime());

			Calendar mon = Calendar.getInstance();
			mon.add(Calendar.DATE, -7); // Set start time (1 weeks ago from current time)
//		    mon.add(Calendar.MONTH, -1);
		    String startTime = sdformat.format(mon.getTime());

			lastModStartDate = startTime + "%2B01:00";
			lastModEndDate = endTime + "%2B01:00";
		}

		try {
			Map<String, Object> rtnMap = nvdMetaDataApiCheckJob(NVD_META_REST_URL, 1, 0);
			if (rtnMap.containsKey("checkUrlFlag") && !(boolean) rtnMap.get("checkUrlFlag")) {
				rtnMap = nvdMetaDataApiJob(NVD_META_REST_URL, rtnMap);
				if (!(boolean) rtnMap.get("connectionFlag")) {
					schlog.info("nvd meta api connection error");
					return "91";
				}
			}
		} catch (Exception e) {
			schlog.error(e.getMessage(), e);
			return "91";
		}
    
		try {
			Map<String, Object> rtnMap = nvdMetaDataApiCheckJob(NVD_CVE_REST_URL, 1, 0);
			if (rtnMap.containsKey("checkUrlFlag") && !(boolean) rtnMap.get("checkUrlFlag")) {
				rtnMap = nvdCveDataApiJob(NVD_CVE_REST_URL, rtnMap);
				if (!(boolean) rtnMap.get("connectionFlag")) {
					schlog.info("nvd meta api connection error");
					return "91";
				}
			}
		} catch (Exception e) {
			schlog.error(e.getMessage(), e);
			return "92";
		}
	    
	    if (initializeFlag) {
			codeMapper.updateCodeDtlNm("990", "100", "N");
			initializeFlag = false;
		}
		
		try {
			nvdCveDataSyncJob(true);
		} catch (Exception e) {
			schlog.error(e.getMessage(), e);
			return "93";
		}
		
		return "00";
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Map<String, Object> nvdCveDataApiJob(String restApiUrl, Map<String, Object> rtnMap) {
		final int totalResults = (int) rtnMap.get("totalResults");
		final String urlConnTimestamp = (String) rtnMap.get("timestamp");
		final String format = (String) rtnMap.get("format");
		final String fileNm = restApiUrl.replace("https://services.nvd.nist.gov", "");
		
		Map<String, Object> responseMap = new HashMap<>();
		boolean httpsUrlConnectionFlag = false;

		List<Map<String, Object>> vulnerabilities = null;
		Map<String, Object> cveInfo = null;
		List<Map<String, Object>> cpe_match_all = null;
		List<String> cpe_match_running_on_with = null;
		List<Map<String, Object>> cvePatchList = null;
		schlog.info("nvdCveDataApiJob start");

		nvdDataMapper.createTableNvdCveV3Temp();
		nvdDataMapper.createTableNvdDataV3Temp();
		nvdDataMapper.createTableConfigurationsTemp();
		nvdDataMapper.createTablePatchLinkTemp();
		
		nvdDataMapper.truncateNvdCveV3Temp();
		nvdDataMapper.truncateNvdDataV3Temp();
		nvdDataMapper.truncateNvdDataConfigurationsTemp();
		nvdDataMapper.truncateNvdDataPatchLinkTemp();
		
		int totalCnt = totalResults;
		if(totalCnt < API_CPE_CHUNK_SIZE) {
			totalCnt = API_CPE_CHUNK_SIZE;
		}
		
		String cveId = null;
		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH,false)) {
			final NvdDataMapper mapper = sqlSession.getMapper(NvdDataMapper.class);
			
			for(int limitIndex = 0; limitIndex <= totalCnt/API_CPE_CHUNK_SIZE; limitIndex++) {
				schlog.info("getNvdCpeMatchData in progress {}/{}", API_CPE_CHUNK_SIZE*limitIndex, totalCnt);
				for (int i = 0; i < 5; i++) {
					responseMap = getDataForRestApiConnection(NVD_REST_BASE_URL + NVD_REST_CPE_URL, API_CPE_CHUNK_SIZE, API_CPE_CHUNK_SIZE * limitIndex, 1);
					httpsUrlConnectionFlag = (boolean) responseMap.get("connectionFlag");
					if (httpsUrlConnectionFlag) {
						break;
					}
				}
        
				if (httpsUrlConnectionFlag) {
					if (responseMap.containsKey("vulnerabilities")) {
						vulnerabilities = (List<Map<String, Object>>) responseMap.get("vulnerabilities");
						if (vulnerabilities != null) {
							for (Map<String, Object> vulnerability : vulnerabilities) {
								cveInfo = restApiCveDatajsonReader(vulnerability);
								if (cveInfo == null || cveInfo.isEmpty()) {
									continue;
								}
                
								cveId = (String) cveInfo.get("cveId");
								Map<String, Object> existCveInfo = mapper.selectOneCveInfoV3(cveInfo);
								if (!MapUtils.isEmpty(existCveInfo)) {
									String baseMetric = (String) cveInfo.get("baseMetric");
									String existBaseMetric = (String) existCveInfo.get("baseMetric");
									Map<String, Object> param = new HashMap<>();
									param.put("cveId", cveInfo.get("cveId"));
									boolean updateFlag = false;
									if (StringUtil.isEmpty(existBaseMetric) && !StringUtil.isEmpty(baseMetric)) {
										param.put("baseMetric", baseMetric);
										param.put("cvssScore", Float.parseFloat((String) cveInfo.get("cvssScore")));
										param.put("summary", (String) cveInfo.get("summary"));
										param.put("modiDate", cveInfo.get("modiDate"));
										updateFlag = true;
									} else if (!StringUtil.isEmpty(baseMetric) && !StringUtil.isEmpty(existBaseMetric)) {
										if (baseMetric.equals(existBaseMetric)) {
											if (new BigDecimal(cveInfo.get("cvssScore").toString()).compareTo(new BigDecimal(existCveInfo.get("cvssScore").toString())) > 0
													|| new BigDecimal(cveInfo.get("cvssScore").toString()).compareTo(new BigDecimal(existCveInfo.get("cvssScore").toString())) < 0) {
												param.put("baseMetric", baseMetric);
												param.put("cvssScore", Float.parseFloat((String) cveInfo.get("cvssScore")));
												param.put("summary", (String) cveInfo.get("summary"));
												param.put("modiDate", cveInfo.get("modiDate"));
												updateFlag = true;
											}
										} else {
											param.put("baseMetric", baseMetric);
											param.put("cvssScore", Float.parseFloat((String) cveInfo.get("cvssScore")));
											param.put("summary", (String) cveInfo.get("summary"));
											param.put("modiDate", cveInfo.get("modiDate"));
											updateFlag = true;
										}
									}
									if (updateFlag) {
										mapper.updateCveInfoV3(param);
									}
								} else {
									mapper.insertCveInfoV3Temp(cveInfo);
								}
								
								cpe_match_all = (List<Map<String, Object>>) cveInfo.get("cpe_match_all");
								
								if (cpe_match_all == null || cpe_match_all.isEmpty()) {
									continue;
								}
								
								List<String> matchCriteriaIdDuplicatedList = new ArrayList<>();

								for (Map<String, Object> cpe_match_data : cpe_match_all) {
									// 정보에서 Version Range 조건을 고려하여 Cpe match 정보로 부터 최종적요으로 적용할 모든 대상 cpe23uri를 취득한다.
									// Version Range 조건 취득
									// 검색 조건 설정
//								Map<String, String> _matchNameParams = new HashMap<>();
									final String criteria = (String) cpe_match_data.get("criteria");
									final String matchCriteriaId = (String) cpe_match_data.get("matchCriteriaId");
//								_matchNameParams.put("cpe23Uri", criteria);
//								_matchNameParams.put("matchCriteriaId", matchCriteriaId);
									

									// configurations의 operator (AND/OR)에 따라 동일한 CVE ID에 matchCriteriaId 가 여러번 등장할 수 있음
									// FL-Hub에서는 operator 조건을 확인하지 않기 때문에, CVEID + matchCriteriaId를 PK로 사용하는 경우 중복 오류가 발생할 수 있음
									// ex) https://services.nvd.nist.gov/rest/json/cves/2.0?cveId=CVE-2001-1104
									if (matchCriteriaIdDuplicatedList.contains(cveId + "-" + matchCriteriaId)) {
										continue;
									}
									matchCriteriaIdDuplicatedList.add(cveId + "-" + matchCriteriaId);
									
									String versionStartIncluding = null;
									String versionEndIncluding = null;
									String versionStartExcluding = null;
									String versionEndExcluding = null;

									if (cpe_match_data.containsKey("versionStartIncluding")) {
										versionStartIncluding = (String) cpe_match_data.get("versionStartIncluding");
//										_matchNameParams.put("versionStartIncluding", versionStartIncluding);
									}
									if (cpe_match_data.containsKey("versionEndIncluding")) {
										versionEndIncluding = (String) cpe_match_data.get("versionEndIncluding");
//										_matchNameParams.put("versionEndIncluding", versionEndIncluding);
									}
									if (cpe_match_data.containsKey("versionStartExcluding")) {
										versionStartExcluding = (String) cpe_match_data.get("versionStartExcluding");
//										_matchNameParams.put("versionStartExcluding", versionStartExcluding);
									}
									if (cpe_match_data.containsKey("versionEndExcluding")) {
										versionEndExcluding = (String) cpe_match_data.get("versionEndExcluding");
//										_matchNameParams.put("versionEndExcluding", versionEndExcluding);
									}

//									matchNames = nvdDataMapper.selectNvdMatchList(_matchNameParams);
//
//									// 만약 cpe match에서 cpe23uri로 조회된 결과가 없을 경우 해당 cpe23uri 만 설정한다.
//									if (matchNames == null || matchNames.isEmpty()) {
//										log.warn("unmatchNames matchCriteriaId : {}, criteria : {}" , matchCriteriaId, criteria);
//										// api 호출 여부 확인
//										continue;
//									}
//
//									// cpe23uri에서 product, version 정보를 추출한다.
//									List<Map<String, String>> nvdDataV3TempList = new ArrayList<>();
//									matchNames.forEach(cpe23uri -> {
//										final String[] cpeInfoArr = cpe23uri.split(":");
//
//										final Map<String, String> _productInfo = new HashMap<>();
//										_productInfo.put("cveId", cveId);
//										_productInfo.put("vendor", cpeInfoArr[3]);
//										_productInfo.put("product", cpeInfoArr[4]);
//										_productInfo.put("version", cpeInfoArr[5]);
//										nvdDataV3TempList.add(_productInfo);
//									});
//									
//									if(!CollectionUtils.isEmpty(nvdDataV3TempList)) {
//										mapper.insertBulkNvdDataV3Temp(nvdDataV3TempList);
//									}
									
									final Map<String, String> configurationInsertParam = new HashMap<>();
									configurationInsertParam.put("cveId", cveId);
									configurationInsertParam.put("matchCriteriaId", matchCriteriaId);
									configurationInsertParam.put("criteria", criteria);
									final String[] criteriaArr = unescapedColon(criteria);
									configurationInsertParam.put("vendor", criteriaArr[3]);
									configurationInsertParam.put("product", criteriaArr[4]);
									configurationInsertParam.put("version", criteriaArr[5]);
									if (versionStartIncluding != null) {
										configurationInsertParam.put("versionStartIncluding", versionStartIncluding);
									}
									if (versionEndIncluding != null) {
										configurationInsertParam.put("versionEndIncluding", versionEndIncluding);
									}
									if (versionStartExcluding != null) {
										configurationInsertParam.put("versionStartExcluding", versionStartExcluding);
									}
									if (versionEndExcluding != null) {
										configurationInsertParam.put("versionEndExcluding", versionEndExcluding);
									}

									mapper.insertNvdDataConfigurationsTemp(configurationInsertParam);
								}

								cpe_match_running_on_with = (List<String>) cveInfo.get("cpe_match_running_on_with");
								if (!CollectionUtils.isEmpty(cpe_match_running_on_with)) {
									for (String key : cpe_match_running_on_with) {
										String vulnerableKey = key.split("[|]")[0];
										String notVulnerableCriteria = key.split("[|]")[1];
										
										final Map<String, Object> notVulnInsertParam = new HashMap<>();
										final String[] criteriaArr = notVulnerableCriteria.split(":");
										
										notVulnInsertParam.put("matchCriteriaId", vulnerableKey);
										notVulnInsertParam.put("cveId", cveId);
										notVulnInsertParam.put("vendor", criteriaArr[3]);
										notVulnInsertParam.put("product", criteriaArr[4]);
										notVulnInsertParam.put("relMatchCriteria", notVulnerableCriteria);
										
										mapper.insertNvdDataRunningOnWithTemp(notVulnInsertParam);
									}
									
									cpe_match_running_on_with = null;
								}
								
								cvePatchList = (List<Map<String, Object>>) cveInfo.get("cvePatchList");
								if (!CollectionUtils.isEmpty(cvePatchList)) {
									for (Map<String, Object> cvePatchInfo : cvePatchList) {
										final Map<String, Object> _patchInfo = new HashMap<>();
										_patchInfo.put("cveId", cveId);
										_patchInfo.put("patchLink", cvePatchInfo.get("url"));
										_patchInfo.put("publDate", cveInfo.get("publDate"));
										mapper.insertNvdDataPatchLinkTemp(_patchInfo);
									}
									cvePatchList = null;
								}
								
								mapper.insertNvdDataV3Temp(cveId);
							}

							sqlSession.flushStatements();

						}
					}
				} else {
					schlog.info("Failed to get data for index: {}/{}. Skipping to next chunk.", API_MATCH_CHUNK_SIZE*limitIndex, totalCnt);
				}
			}

			sqlSession.flushStatements();
			sqlSession.commit();
		}

		if (httpsUrlConnectionFlag) {
			// configuration data delete & insert
			if (initializeFlag) {
				nvdDataMapper.resetCveDataV3();
				nvdDataMapper.resetNvdDataV3();
				nvdDataMapper.truncateNvdDataConfigurations();
				nvdDataMapper.truncateNvdDataPatchLink();
				nvdDataMapper.truncateNvdDataRunningOnWith();
			} else {
				// 최신정보 재등록을 위한 Copy 대상 data 삭제
				nvdDataMapper.deleteNvdCveV3ExistingInTemp();
				nvdDataMapper.deleteNvdDataV3ExistingInTemp();
				nvdDataMapper.deleteNvdDataConfigurationsExistingInTemp();
				nvdDataMapper.deleteNvdDataPatchLinkExistingInTemp();
				nvdDataMapper.deleteNvdDataRunningOnWithExistingInTemp();
			}
			nvdDataMapper.copyNvdCveV3FromTemp();
			nvdDataMapper.copyNvdDataV3FromTemp();
			nvdDataMapper.copyNvdDataConfigurationsFromTemp();
			nvdDataMapper.copyNvdDataPatchLinkFromTemp();
			nvdDataMapper.copyNvdDataRunningOnWithFromTemp();

			nvdDataMapper.truncateNvdCveV3Temp();
			nvdDataMapper.truncateNvdDataV3Temp();
			nvdDataMapper.truncateNvdDataConfigurationsTemp();
			nvdDataMapper.truncateNvdDataPatchLinkTemp();
			nvdDataMapper.truncateNvdDataRunningOnWithTemp();
			
			int vendorProductNvdDataV3Cnt = nvdDataMapper.selectVendorProductNvdDataV3Cnt();
			if (vendorProductNvdDataV3Cnt > 0) {
				schlog.info("Vendor Product Nvd Data V3 Update Count : {}", vendorProductNvdDataV3Cnt);
				nvdDataMapper.updateVendorProductNvdDataV3();
			}

			// success data insert > nvd_meta table
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("fileNm", fileNm);
			param.put("fileType", format);
			param.put("modiDate", urlConnTimestamp);

			nvdDataMapper.insertNewMetaDataUrlConnection(param);
			schlog.info("nvdCveDataApiJob end");
		}

		responseMap.put("connectionFlag", httpsUrlConnectionFlag);
		return responseMap;
	}

	private String[] unescapedColon(String criteria) {
		String placeholder = "__ESCAPED_COLON__";
        String temp = criteria.replaceAll("\\\\:", placeholder);

        String[] parts = temp.split(":");

        List<String> result = new ArrayList<>();
        for (String part : parts) {
            result.add(part.replace(placeholder, ":"));
        }
		return result.toArray(new String[result.size()]);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> restApiCveDatajsonReader(Map<String, Object> vulnerability) {
		final Map<String, Object> resultMap = new HashMap<>();

		final Map<String, Object> cveItem = (Map<String, Object>) vulnerability.get("cve");
		final String publishedDate = (String) cveItem.get("published");
		final String lastModifiedDate = (String) cveItem.get("lastModified");
		final String cveId = (String) cveItem.get("id");
		
		// summary
		final List<Map<String, Object>> descriptions = (List<Map<String, Object>>) cveItem.get("descriptions");
		String descriptionStr = "";
		if (!CollectionUtils.isEmpty(descriptions)) {
			List<String> enValues = descriptions.stream().filter(map -> "en".equals(map.get("lang"))).map(map -> String.valueOf(map.get("value"))).collect(Collectors.toList());
		    if (!enValues.isEmpty()) {
		        descriptionStr = String.join("\n", enValues);
		    } else {
		        Object firstValue = descriptions.get(0).get("value");
		        descriptionStr = (firstValue != null) ? firstValue.toString() : "";
		    }
		}
		
		// metrics
		final Map<String, Object> metrics = (Map<String, Object>) cveItem.get("metrics");
		// CVSS V3가 없는 경우 V2 Score를 사용
		String baseScore = "0";
		String baseMetric = "";
		if (metrics.containsKey("cvssMetricV40")) {
			List<Map<String, Object>> cvssMetricV4 = (List<Map<String, Object>>) metrics.get("cvssMetricV40");
			if (!CollectionUtils.isEmpty(cvssMetricV4)) {
				Map<String, Object> cvssData = readToCvssMetric(cvssMetricV4);
				if (!MapUtils.isEmpty(cvssData) && cvssData.containsKey("baseScore")) {
					baseScore = String.valueOf(cvssData.get("baseScore"));
					baseMetric = "V4";	
				}
			}
		}
		if (baseMetric.equals("") && metrics.containsKey("cvssMetricV31")) {
			List<Map<String, Object>> cvssMetricV3 = (List<Map<String, Object>>) metrics.get("cvssMetricV31");
			if (!CollectionUtils.isEmpty(cvssMetricV3)) {
				Map<String, Object> cvssData = readToCvssMetric(cvssMetricV3);
				if (!MapUtils.isEmpty(cvssData) && cvssData.containsKey("baseScore")) {
					baseScore = String.valueOf(cvssData.get("baseScore"));
					baseMetric = "V3";	
				}
			}
		}
		if (baseMetric.equals("") && metrics.containsKey("cvssMetricV30")){
			List<Map<String, Object>> cvssMetricV30 = (List<Map<String, Object>>) metrics.get("cvssMetricV30");
			if (!CollectionUtils.isEmpty(cvssMetricV30)) {
				Map<String, Object> cvssData = readToCvssMetric(cvssMetricV30);
				if (!MapUtils.isEmpty(cvssData) && cvssData.containsKey("baseScore")) {
					baseScore = String.valueOf(cvssData.get("baseScore"));
					baseMetric = "V3";
				}
			}
		}
		if (baseMetric.equals("") && metrics.containsKey("cvssMetricV2")){
			List<Map<String, Object>> cvssMetricV2 = (List<Map<String, Object>>) metrics.get("cvssMetricV2");
			if (!CollectionUtils.isEmpty(cvssMetricV2)) {
				Map<String, Object> cvssData = readToCvssMetric(cvssMetricV2);
				if (!MapUtils.isEmpty(cvssData) && cvssData.containsKey("baseScore")) {
					baseScore = String.valueOf(cvssData.get("baseScore"));
					baseMetric = "V2";
				}
			}
		}
		
		List<Map<String, String>> ossList = new ArrayList<>();
		List<Map<String, Object>> cpe_match_all = new ArrayList<>();
		List<String> cpe_match_running_on_with_list = new ArrayList<>();
		
		if (cveItem.containsKey("configurations")) {
			for (Map<String, Object> configurations : (List<Map<String, Object>>) cveItem.get("configurations")) {
				List<String> vulnMatchCrtrIdList = new ArrayList<>();
				List<String> notVulnMatchCriteriaList = new ArrayList<>();
				
				if (configurations.containsKey("nodes")) {
					// 정의된 모든 cpe match 정보
					for (Map<String, Object> node_data : (List<Map<String, Object>>) configurations.get("nodes")) {
						// children cpe_match 대신 children 노드가 존재하는 경우, children 노드 하위에서 cpe_match 정보를 취득한다.
						if (node_data.containsKey("cpeMatch")) {
							List<Map<String, Object>> cpeMatchList = (List<Map<String, Object>>) node_data.get("cpeMatch");
							for (Map<String, Object> cpeMatch : cpeMatchList) {
								String matchCriteriaId = (String) cpeMatch.get("matchCriteriaId");
								if ((boolean) cpeMatch.get("vulnerable")) {
									vulnMatchCrtrIdList.add(matchCriteriaId);
									cpe_match_all.add(cpeMatch);
								} else {
									notVulnMatchCriteriaList.add((String) cpeMatch.get("criteria"));
								}
							}
						}
					}
					
					if (!CollectionUtils.isEmpty(notVulnMatchCriteriaList)) {
						for (String vulnMatchCrtrId : vulnMatchCrtrIdList) {
							for (String notVulnMatchCriteria : notVulnMatchCriteriaList) {
								String key = vulnMatchCrtrId + "|" + notVulnMatchCriteria;
								if (!cpe_match_running_on_with_list.contains(key)) {
									cpe_match_running_on_with_list.add(key);
								}
							}
						}
					}
				}
			}
		}
		
		final List<Map<String, Object>> cvePatchList = new ArrayList<>();
		if (cveItem.containsKey("references")) {
			for (Map<String, Object> references : (List<Map<String, Object>>) cveItem.get("references")) {
				if (references.containsKey("tags")) {
					boolean checkPatchLinkFlag = false;
					for (String tag : (List<String>) references.get("tags")) {
						tag = tag.toUpperCase();
						if ("PATCH".equals(tag) || "MITIGATION".equals(tag) || "RELEASE NOTES".equals(tag)) {
							checkPatchLinkFlag = true;
							break;
						}
					}
					if (checkPatchLinkFlag) {
						cvePatchList.add(references);
					}
				}
			}
		}
		
		resultMap.put("ossList", ossList);
		resultMap.put("cveId", cveId);
		resultMap.put("publDate", DateUtil.convertStringToTimestamp((publishedDate), "yyyy-MM-dd'T'HH:mm:ss.SSS"));
		resultMap.put("modiDate", DateUtil.convertStringToTimestamp((lastModifiedDate), "yyyy-MM-dd'T'HH:mm:ss.SSS"));
		resultMap.put("cvssScore", baseScore);
		resultMap.put("summary", descriptionStr);
		resultMap.put("baseMetric", baseMetric);
		resultMap.put("cpe_match_all", cpe_match_all);
		resultMap.put("cpe_match_running_on_with", cpe_match_running_on_with_list);
		resultMap.put("cvePatchList", cvePatchList);
    
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> readToCvssMetric(List<Map<String, Object>> cvssMetricList) {
		Map<String, Object> cvssData = null;
		Map<String, Object> cvssObj = null;
		if (cvssMetricList.size() > 1) {
			Map<String, Object> firstObj = null;
			Map<String, Object> firstData = null;
			Map<String, Object> secondObj = null;
			Map<String, Object> secondData = null;
			
			for (Map<String, Object> cvssMetric : cvssMetricList) {
				String source = String.valueOf(cvssMetric.get("source"));
				String type = String.valueOf(cvssMetric.get("type"));
				if (source.equalsIgnoreCase("nvd@nist.gov")) {
					firstObj = cvssMetric;
					firstData = (Map<String, Object>) firstObj.get("cvssData");
				}
				if (type.equalsIgnoreCase("Primary")) {
					secondObj = cvssMetric;
					secondData = (Map<String, Object>) secondObj.get("cvssData");
				}
			}
			
			if (firstObj != null) {
				cvssObj = firstObj;
				cvssData = firstData;
			} else if (secondObj != null) {
				cvssObj = secondObj;
				cvssData = secondData;
			}
			
			if (cvssObj == null) {
				cvssObj = (Map<String, Object>) cvssMetricList.get(0);
				cvssData = (Map<String, Object>) cvssObj.get("cvssData");
			}
		} else {
			cvssObj = (Map<String, Object>) cvssMetricList.get(0);
			cvssData = (Map<String, Object>) cvssObj.get("cvssData");
		}
		return cvssData;
	}
	
	private Map<String, Object> nvdMetaDataApiCheckJob(String restApiUrl, int resultsPerPage, int startIndex) throws IOException {
		Map<String, Object> responseMap = new HashMap<>();
		String fileNm = restApiUrl.replace("https://services.nvd.nist.gov", "");

		try {
			for (int i=0; i<5; i++) {
				responseMap = getDataForRestApiConnection(restApiUrl, resultsPerPage, startIndex, i);
				if ((boolean) responseMap.get("connectionFlag")) {
					break;
				}
			}

			if ((boolean) responseMap.get("connectionFlag")) {
				// check url connection timestamp
				HashMap<String, Object> param = new HashMap<String, Object>();
				param.put("fileType", (String) responseMap.get("format"));
				param.put("fileNm", fileNm);
				param.put("modiDate", (String) responseMap.get("timestamp"));
				responseMap.put("checkUrlFlag", nvdDataMapper.selectUseMetaDataUrlConnection(param).size() > 0 ? true : false);
			}
		} catch (Exception e) {
			schlog.error(e.getMessage());
			responseMap.put("connectionFlag", false);
		}

		return responseMap;
	}

	/**
	 * CPEMATCH Data API Job
	 * @param restApiUrl
	 * @param rtnMap
	 * @return
	 * @throws SSLException 
	 * @throws JsonProcessingException 
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public Map<String, Object> nvdMetaDataApiJob(String restApiUrl, Map<String, Object> rtnMap) throws JsonProcessingException, SSLException {
		schlog.info("nvdMetaDataApiJob start");
		
		final int totalResults = (int) rtnMap.get("totalResults");
		final String urlConnTimestamp = (String) rtnMap.get("timestamp");
		final String format = (String) rtnMap.get("format");
		final String fileNm = restApiUrl.replace("https://services.nvd.nist.gov", "");
		
		Map<String, Object> responseMap = new HashMap<>();
		
		List<Map<String, Object>> matchStrings = null;
		boolean httpsUrlConnectionFlag = false;
		responseMap.put("totalResults", totalResults);
		
		int totalCnt = totalResults;
		if(totalCnt < API_MATCH_CHUNK_SIZE) {
			totalCnt = API_MATCH_CHUNK_SIZE;
		}
		
		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
			NvdDataMapper mapper = sqlSession.getMapper(NvdDataMapper.class);

			nvdDataMapper.createTableCpeMatchTemp();
			nvdDataMapper.createTableCpeMatchNameTemp();
			nvdDataMapper.truncateCpeMatchTemp();
			nvdDataMapper.truncateCpeMatchNameTemp();
			
			for(int limitIndex = 0; limitIndex <= totalCnt/API_MATCH_CHUNK_SIZE; limitIndex++) {
				schlog.info("getNvdCpeMatchData in progress {}/{}", API_MATCH_CHUNK_SIZE*limitIndex, totalCnt);
				
				for (int i = 0; i < 5; i++) {
					responseMap = getDataForRestApiConnection(NVD_REST_BASE_URL + NVD_REST_CPE_MATCH_URL, API_MATCH_CHUNK_SIZE, API_MATCH_CHUNK_SIZE * limitIndex, 1);
					httpsUrlConnectionFlag = (boolean) responseMap.get("connectionFlag");
					if (httpsUrlConnectionFlag) {
						break;
					}
				}
				
//				String searchUrl = MessageFormat.format(NVD_REST_CPE_MATCH_URL + "?resultsPerPage={0}&startIndex={1}", API_MATCH_CHUNK_SIZE, API_MATCH_CHUNK_SIZE*limitIndex);
//				if (!initializeFlag) {
//					searchUrl += "&lastModStartDate=" + lastModStartDate + "&lastModEndDate=" + lastModEndDate;
//				}
//				responseMap = getNvdData(searchUrl);
//				httpsUrlConnectionFlag = responseMap != null && responseMap.containsKey("matchStrings");
				
				if (httpsUrlConnectionFlag) {
					if (responseMap.containsKey("matchStrings")) {
						matchStrings = (List<Map<String, Object>>) responseMap.get("matchStrings");
						if (matchStrings != null) {
              
							for (Map<String, Object> matchStringObj : matchStrings) {
								if (matchStringObj.containsKey("matchString")) {
									Map<String, Object> matchString = (Map<String, Object>) matchStringObj.get("matchString");
									String matchCriteriaId = (String) matchString.get("matchCriteriaId");
									String cpe23Uri = (String) matchString.get("criteria");

									Map<String, Object> cpeMatchMap = new HashMap<>();
									cpeMatchMap.put("matchCriteriaId", matchCriteriaId);
									cpeMatchMap.put("cpe23Uri", cpe23Uri);
									
									if (matchString.containsKey("versionStartIncluding")) {
										cpeMatchMap.put("versionStartIncluding", (String) matchString.get("versionStartIncluding"));
									} else {
										cpeMatchMap.put("versionStartIncluding", null);
									}
									if (matchString.containsKey("versionEndIncluding")) {
										cpeMatchMap.put("versionEndIncluding", (String) matchString.get("versionEndIncluding"));
									} else {
										cpeMatchMap.put("versionEndIncluding", null);
									}
									if (matchString.containsKey("versionStartExcluding")) {
										cpeMatchMap.put("versionStartExcluding", (String) matchString.get("versionStartExcluding"));
									} else {
										cpeMatchMap.put("versionStartExcluding", null);
									}
									if (matchString.containsKey("versionEndExcluding")) {
										cpeMatchMap.put("versionEndExcluding", (String) matchString.get("versionEndExcluding"));
									} else {
										cpeMatchMap.put("versionEndExcluding", null);
									}
                  
									mapper.insertCpeMatchData(cpeMatchMap);
									
									if (matchString.containsKey("matches")) {
										List<Map<String, Object>> matches = (List<Map<String, Object>>) matchString.get("matches");
										
										int nameIdx = 0;
										List<Map<String, Object>> matchNameList = new ArrayList<>();
										for (Map<String, Object> match : matches) {
											if (match.containsKey("cpeName")) {
												Map<String, Object> cpeMatchNamesMap = new HashMap<>();
												cpeMatchNamesMap.put("matchCriteriaId", matchCriteriaId);
												cpeMatchNamesMap.put("idx", nameIdx);
												cpeMatchNamesMap.put("cpe23Uri", match.get("cpeName"));
												matchNameList.add(cpeMatchNamesMap);
												nameIdx++;
											}
										}
										if (!CollectionUtils.isEmpty(matchNameList)) {
											mapper.insertBulkCpeMatchNameData(matchNameList);
										}
									} else {
										// matche range가 제공되지 않을 수 있음, 아마도 CVSS Version이 2.0 인 경우 제공되지 않는 Case가 있는듯
										// https://services.nvd.nist.gov/rest/json/cpematch/2.0?cveId=CVE-2000-0564
										// 이후 처리에서 matchCriteriaId 에 해당하는 version range를 조회를 일관된 방법으로 처리할 수 있도록
										// matchString의 criteria 를 matches의 cpeName 와 동일하게 처리한다.
										Map<String, Object> cpeMatchNamesMap = new HashMap<>();
										cpeMatchNamesMap.put("matchCriteriaId", matchCriteriaId);
										cpeMatchNamesMap.put("idx", 0);
										cpeMatchNamesMap.put("cpe23Uri", cpe23Uri);
										
										mapper.insertCpeMatchNameData(cpeMatchNamesMap);
									}
									sqlSession.flushStatements();
								}
							}
						}
					}
				} else {
					schlog.info("Failed to get data for index: {}/{}. Skipping to next chunk.", API_MATCH_CHUNK_SIZE*limitIndex, totalCnt);
				}
			}

			sqlSession.flushStatements();
			sqlSession.commit();
		}
			
		schlog.info("httpsUrlConnectionFlag : {}", httpsUrlConnectionFlag);
		if (httpsUrlConnectionFlag && totalResults > 0) {
			if (initializeFlag) {
				nvdDataMapper.truncateCpeMatch();
				nvdDataMapper.truncateCpeMatchNames();
			} else {
				// 최신정보 재등록을 위한 Copy 대상 data 삭제
				// delete CpeMatch and CpeMatchNames
				nvdDataMapper.deleteNvdDataMatchExistingInTemp();
			}

			nvdDataMapper.copyNvdDataMatchFromTemp();
			nvdDataMapper.copyNvdDataMatchNameFromTemp();
			nvdDataMapper.truncateCpeMatchTemp();
			nvdDataMapper.truncateCpeMatchNameTemp();

			// success data insert > nvd_meta table
			HashMap<String, Object> param = new HashMap<>();
			param.put("fileNm", fileNm);
			param.put("fileType", format);
			param.put("modiDate", urlConnTimestamp);

			nvdDataMapper.insertNewMetaDataUrlConnection(param);
			schlog.info("nvdMetaDataApiJob end");
		}

		responseMap.put("connectionFlag", httpsUrlConnectionFlag);
		return responseMap;
	}

	private Map<String, Object> getDataForRestApiConnection(String restApiUrl, int resultsPerPage, int startIndex, int cnt) {
		HttpsURLConnection httpsURLConnection = null;
		Map<String, Object> rtnMap = new HashMap<>();
		String urlString = restApiUrl + "?resultsPerPage=" + resultsPerPage + "&startIndex=" + startIndex;
		if (!initializeFlag) {
			urlString += "&lastModStartDate=" + lastModStartDate + "&lastModEndDate=" + lastModEndDate;
		}

		boolean connectionFlag = true;

		try {
			URL url = new URL(urlString);
			ignoreSsl();
			httpsURLConnection = (HttpsURLConnection) url.openConnection();
			httpsURLConnection.setRequestMethod("GET");
			httpsURLConnection.addRequestProperty("x-api-key", NVD_API_KEY);
			httpsURLConnection.setConnectTimeout(15000);
			httpsURLConnection.setReadTimeout(180000);

			int responseCode = httpsURLConnection.getResponseCode();

	        if (responseCode != HttpURLConnection.HTTP_OK) {
	        	schlog.error("NVD API response error. status={}",responseCode);

	        	connectionFlag = false;
	        	rtnMap.put("connectionFlag", connectionFlag);
	        	return rtnMap;
	        }
			
	        String responseBody;

	        try (
	            InputStream is = httpsURLConnection.getInputStream();
	            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
	            BufferedReader br = new BufferedReader(isr)
	        ) {
	            StringBuilder sb = new StringBuilder(1024 * 1024);
	            String line;
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	            responseBody = sb.toString();
	        }

	        schlog.info("[NVD] startIndex={}, responseLength={}", startIndex, responseBody.length());

	        Map<String, Object> convertMap = getFromJSONObjectToMap(responseBody);
	        if (convertMap != null) {
	            rtnMap = convertMap;
	            connectionFlag = true;
	        } else {
	            connectionFlag = false;
	        }
	        
//			if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
//				Map<String, Object> convertMap = getFromJSONObjectToMap(bufferedReader);
//				if (convertMap != null) {
//					connectionFlag = true;
//					rtnMap = convertMap;
//				} else {
//					connectionFlag = false;
//				}
//			} else {
//				schlog.error("httpsURLConnection error : " + HttpStatus.valueOf(httpsURLConnection.getResponseCode()));
//				connectionFlag = false;
//			}
		} catch (Exception e) {
			schlog.error("[NVD] API connection error. startIndex={}", startIndex, e);
			connectionFlag = false;
		} finally {
			if (httpsURLConnection != null) {
				httpsURLConnection.disconnect();
			}
			if (connectionFlag) {
				try {
					Thread.sleep(1000 * 8);
				} catch (InterruptedException e) {
					schlog.error(e.getMessage());
				}
			} else {
				try {
					schlog.warn("[NVD] Retry after 30 sec. startIndex={}", startIndex);
					Thread.sleep(1000 * 30);
				} catch (InterruptedException e) {
					schlog.error(e.getMessage());
				}
			}
		}

		rtnMap.put("connectionFlag", connectionFlag);
		return rtnMap;
	}

	private Map<String, Object> getFromJSONObjectToMap(String json) {
		if (isEmpty(json)) {
			return null;
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
        	int length = json.length();
            String tail = json.substring(Math.max(0, length - 2000));
            log.error("NVD JSON parsing error (Jackson): {}", e.getMessage());
            log.error("NVD response length={}", length);
            log.error("NVD response tail={}", tail);
        	return null;
        }
	}

	@Transactional
	public String nvdCveDataSyncJob(boolean restApiFlag) throws IOException {
		String resCd = "00";
		log.info("Start CVE Data Sync Job");
//		// 1. Wait Job 데이터 조회
//		HashMap<String, Object> param = new HashMap<String, Object>();
//		param.put("fileType", "CVE");
//		List<HashMap<String, Object>> waitList = nvdDataMapper.selectWaitJobData(param);
//
//		boolean updateFlag = restApiFlag;
//		// 2. Json File -> DB Insert
//		for (Map<String, Object> wMetaMap : waitList){
//
//			param.put("fileNm", wMetaMap.get("fileNm"));
//			param.put("modiDate", wMetaMap.get("modiDate"));
//			param.put("jobStatus", "G");
//			// JobStatus가 W인 대상이 작업이 들어갔다면 G로 변경을 하여 추후에 다시 loop 돌지 않도록 처리함.
//			nvdDataMapper.updateJobStatus(param);
//
//			String NVD_CVE_PATH = env.getProperty("root.dir");
//			if (StringUtil.isEmpty(NVD_CVE_PATH)) {
//				NVD_CVE_PATH = new FileSystemResource("").getFile().getAbsolutePath();
//			}
//			NVD_CVE_PATH = Paths.get(NVD_CVE_PATH, "nvd/cve").toString();
//
//			updateFlag = updateNvdData(NVD_CVE_PATH, (String) wMetaMap.get("fileNm"));
//
//			param.put("jobStatus", "C");
//			nvdDataMapper.updateJobStatus(param);
//
//		}

		// NVD 관련 Data 중복표시(cpe_nm의 마이너 버전이 포함됨), 및 성능 개선을 위해 별도의 테이블을 추가
		// truncate and insert 처리
		// 위험성 : truncate는 transaction 으로 관리 할 수 없다. truncate이후에 insert 실패시 data 없을 수 있음

		if (restApiFlag){
			// temp table에 data insert 이후, real table로 copy
			nvdDataMapper.createTableNvdDavaScoreV3Temp();
			nvdDataMapper.deleteNvdDataScoreV3Temp();
			
			AtomicInteger processedCount = new AtomicInteger(0);
	        nvdDataMapper.streamDistinctProductList(new ResultHandler<String>() {
	            @Override
	            public void handleResult(ResultContext<? extends String> resultContext) {
	                String product = resultContext.getResultObject();
	                
	                if (product != null && !product.trim().isEmpty()) {
	                    nvdDataMapper.insertNvdDataScoreV3TempByProduct(product);
	                    
	                    int currentCount = processedCount.incrementAndGet();
	                    if (currentCount % 1000 == 0) {
	                    	schlog.info("Progress: {} products processed...", currentCount);
	                    }
	                }
	            }
	        });
	        schlog.info("Product split-loading completed. (Total products: {} items)", processedCount.get());

			nvdDataMapper.deleteNvdDataScoreV3();
			nvdDataMapper.insertNvdDataScoreV3();
			nvdDataMapper.deleteNvdDataScoreV3Temp();

			schlog.info("End CVE Data Sync Job");
		}

		int nickNameMgrCnt = nvdDataMapper.selectNickNameMgrtNvdDataScoreV3();
		if (nickNameMgrCnt > 0) {
			schlog.info("Nickname Migration Count : " + nickNameMgrCnt);
			// OSS_NICKNAME 기준으로 NVD_DATA_SCORE_V3에 NICKNAME을 추가함.
			nvdDataMapper.insertNickNameMgrtNvdDataScoreV3();
		} else{
			schlog.info("Nickname Migration Count : 0");
		}

		int MaxCvssScoreCnt = nvdDataMapper.selectMaxCvssScoreNvdDataScoreV3();
		if (MaxCvssScoreCnt > 0) {
			schlog.info("MaxCvssScore Added Count : " + MaxCvssScoreCnt);
			// NVD_DATA_SCORE_V3에서 CVSS_SCORE MAX값을 기준으로 PRODUCT에서 VERSION이 없는 DATA를 추가함.
			nvdDataMapper.insertMaxCvssScoreNvdDataScoreV3();
		} else {
			schlog.info("MaxCvssScore Added Count : 0");
		}

//		int diffCvssScoreCnt = nvdDataMapper.ossNameNickNameCvssScoreDiffCnt();
//		if (diffCvssScoreCnt > 0){
//			schlog.info("NickName -> ossName cvssScore Diff Count : " + diffCvssScoreCnt);
//			nvdDataMapper.ossNameToNickNameMgrtCvssScore();
//		} else{
//			schlog.info("NickName -> ossName cvssScore Diff Count : 0");
//		}
//
//		int ossNameToNickDiffCvssScoreCnt = nvdDataMapper.ossNameToNickMgrtCvssScoreDiffCnt();
//		if (ossNameToNickDiffCvssScoreCnt > 0){
//			schlog.info("ossName -> NickName cvssScore Diff Count : " + ossNameToNickDiffCvssScoreCnt);
//			nvdDataMapper.nickNameToOssNameMgrtCvssScore();
//		} else{
//			schlog.info("ossName -> NickName cvssScore Diff Count : 0");
//		}

		int vendorProductNvdDataScoreV3Cnt = nvdDataMapper.selectVendorProductNvdDataScoreV3Cnt();
		if (vendorProductNvdDataScoreV3Cnt > 0) {
			schlog.info("Vendor Product NVD Data Score V3 Update Count : " + vendorProductNvdDataScoreV3Cnt);
			nvdDataMapper.updateVendorProductNvdDataScoreV3();
		}

		return resCd;
	}
  
	private void ignoreSsl() {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
	    		return true;
	    	}
		};
		try {
			trustAllHttpsCertificates();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	private static void trustAllHttpsCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

	static class miTM implements TrustManager,X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }
}
