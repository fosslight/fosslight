/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.EntAnalysis;
import oss.fosslight.domain.EnterpriseIntegrationBean;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2Users;
import oss.fosslight.repository.EnterpriseIntegrationMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.EnterpriseIntegrationService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.T2UserService;

@Service
@Slf4j
public class EnterpriseIntegrationServiceImpl extends CoTopComponent implements EnterpriseIntegrationService {
	@Autowired ProjectService projectService;
	@Autowired OssService ossService;
	@Autowired CommentService commentService;
	@Autowired T2UserService userService;
	
	@Autowired private EnterpriseIntegrationMapper enterpriseIntegrationMapper;
	
	@Value("${ent.analysis.service.url:}") private String enterpriseIntegrationUrl;
	
	@Value("${server.port:}") private String port;
	
	private final RestTemplate enterpriseApiRestTemplate;
	
	public EnterpriseIntegrationServiceImpl(@Qualifier("internalApiRestTemplate") RestTemplate internalApiRestTemplate) {
        this.enterpriseApiRestTemplate = internalApiRestTemplate;
    }
	
	@Override
	public ResponseEntity<?> executeEnterpriseAnalysis(String url, String token, String prjId, boolean isResponseRequired) {
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        if (!isEmpty(token)) {
	        	headers.add("Cookie", "X-FOSS-AUTH-TOKEN=" + token);
	        } else if (!isResponseRequired) {
	        	ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    Cookie[] cookies = request.getCookies();
                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            if ("X-FOSS-AUTH-TOKEN".equals(cookie.getName())) {
                            	token = cookie.getValue();
                                break;
                            }
                        }
                    }
                    
                    if (!isEmpty(token)) {
                        headers.add("Cookie", "X-FOSS-AUTH-TOKEN=" + token);
                        log.info("[executeEnterpriseAnalysis] Successfully attached X-FOSS-AUTH-TOKEN to request header.");
                    } else {
                        log.warn("[executeEnterpriseAnalysis Warning] X-FOSS-AUTH-TOKEN not found in current request context.");
                    }
                }
	        }
	        
	        Map<String, String> requestBody = new HashMap<>();
	        requestBody.put("prjId", prjId);
	        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
	        
	        if (isResponseRequired) {
	        	// Define the response type according to the expected JSON structure
	            ParameterizedTypeReference<Map<String, Map<String, Object>>> responseType = new ParameterizedTypeReference<Map<String, Map<String, Object>>>(){};
	            
	            // Exchange and return the entire ResponseEntity instead of just the body
	            ResponseEntity<Map<String, Map<String, Object>>> response = enterpriseApiRestTemplate.exchange(url, HttpMethod.POST, entity, responseType);
	            return response;
	        } else {
	        	if (!isEmpty(token)) {
	        		CompletableFuture.runAsync(() -> {
                        try {
                            ResponseEntity<String> response = enterpriseApiRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                            log.info("[executeEnterpriseAnalysis] Async response received. Status: {}", response.getStatusCode());
                        } catch (RestClientException e) {
                            log.error("[executeEnterpriseAnalysis Error] Failed during async request: {} / Error: {}", url, e.getMessage());
                        }
                    });
	        	}
	        	return null;
	        }
		} catch (HttpClientErrorException e) {
	        // Catch 4xx/5xx errors thrown by RestTemplate and return them as a ResponseEntity containing the error body
	        log.error("[executeEnterpriseAnalysis API Error] Status: {} / Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
	        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
	        
	    } catch (Exception e) {
	        log.error("[executeEnterpriseAnalysis Exception] {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
	    }
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void processAnalysisDataInterface(Object object, T2Users userInfo) {
		// Receive data by calling the Enterprise API
		if (isEmpty(enterpriseIntegrationUrl)) {
			return;
		}
		
		// Group the received data based on Project ID
    	Map<String, Map<String, Object>> resultMap = (Map<String, Map<String, Object>>) object;
    	if (MapUtils.isEmpty(resultMap)) {
            return;
        }
    	
    	String prjId = resultMap.keySet().iterator().next();
    	Map<String, Object> analysisDataMap = resultMap.get(prjId);
    	String companyName = String.valueOf(analysisDataMap.getOrDefault("companyName", ""));
    	String targetPrjId = "";
    	Map<String, Object> preResult = null;
    	Integer jobSeq = null;
    	
    	try {
            preResult = preProcessEnterpriseAnalysis(prjId, analysisDataMap);
            if (MapUtils.isNotEmpty(preResult)) {
            	jobSeq = (Integer) preResult.get("jobSeq");
                targetPrjId = postProcessEnterpriseAnalysis(preResult, prjId, companyName, userInfo.getUserId());
            }
            
            if (isEmpty(targetPrjId)) {
                throw new RuntimeException("Failed to postProcessEnterpriseAnalysis: targetPrjId is empty.");
            }
            
            log.info("[Success] postProcessEntAnalysis ENT_{} processed successfully.", prjId);
            
            projectService.initAutoReview(targetPrjId, userInfo.getUserId(), jobSeq);
        } catch (Exception e) {
            log.error("[Error/Rollback] Failed to process analysis data for ENT_{}. Reason: {}", prjId, e.getMessage(), e);
            throw e; 
        }
    	
    	try {
    		if (!preResult.containsKey("skipCoReviewer")) {
    			String autoReviewUrl = String.format("http://127.0.0.1:%s/autoReview/autoReviewStart/%s/true/true/true", port, targetPrjId);
                executeEnterpriseAnalysis(autoReviewUrl, null, prjId, false);
    		}
            log.info("[Final Success] Auto Review initiated for Project {}", targetPrjId);
		} catch (Exception e) {
			log.error("[Final Error] Failed to initiate Auto Review: {}", e.getMessage(), e);
		}
    	
    	try {
    		if (preResult.containsKey("chunkMap")) {
            	Map<String, OssMaster> ossInfo = (Map<String, OssMaster>) preResult.get("chunkMap");
            	syncOssInfoByChunk(ossInfo);
            }
    		if (jobSeq != null && preResult.containsKey("skipCoReviewer")) {
    			executeAnalysisUpdate(String.valueOf(jobSeq), null, null, true);
    		}
    	} catch (Exception e) {
    		log.error("[Final Error] Failed to initiate Auto Review: {}", e.getMessage(), e);
    	}
	}

	private Map<String, Object> preProcessEnterpriseAnalysis(String analysisKey, Map<String, Object> analysisData) {
		if (MapUtils.isEmpty(analysisData)) {
			return null;
		}

		Map<String, Object> result = new HashMap<>();
		
		try {
	    	String division = String.valueOf(analysisData.getOrDefault("division", ""));
	    	ObjectMapper mapper = new ObjectMapper();
		    List<ProjectIdentification> ossComponents = mapper.convertValue(analysisData.get("ossList"), new TypeReference<List<ProjectIdentification>>() {});
		    
		    if (isEmpty(division) || CollectionUtils.isEmpty(ossComponents)) {
		    	return null;
		    }
	    	
	    	if (CollectionUtils.isNotEmpty(ossComponents)) {
	    		// Saves data to ENT_ANALYSIS_JOB
	    		int size = ossComponents.size();
		        String referenceId = "ENT_" + analysisKey;
		        EntAnalysis entAnalysis = new EntAnalysis(referenceId, division, String.valueOf(size));
		        enterpriseIntegrationMapper.insertEnterpriseIntegrationJob(entAnalysis);
		        
		        Integer jobSeq = entAnalysis.getJobSeq();

		    	// Saves data to ENT_ANALYSIS_JOB_DETAILS
		        if (jobSeq != null) {
		            for (ProjectIdentification bean : ossComponents) {
		                bean.setReferenceId(referenceId);
		                bean.setJobSeq(jobSeq);
		            }
		            enterpriseIntegrationMapper.insertEnterpriseIntegrationJobDetails(ossComponents);
		        }
		        
		        // Check if the data is registered in the OSS Info
		        int limitCount = Integer.parseInt(String.valueOf(analysisData.get("limitCount")));
		        int analysisDataCount = enterpriseIntegrationMapper.getEnterpriseAnalysisCountByDivision(division);
		        int rejectedReviewCount = 0;
		        int allowedReviewCount = 0;
		        
                Map<String, OssMaster> chunkMap = new HashMap<>();
                List<ProjectIdentification> requiredAnalysisOssList = new ArrayList<>();
                
                for (ProjectIdentification bean : ossComponents) {
                	if (analysisDataCount == limitCount) {
                		break;
                	}
                	
                    String key = (bean.getOssName() + "_" + avoidNull(bean.getOssVersion())).toUpperCase();
                    if (CoCodeManager.OSS_INFO_UPPER.containsKey(key)) {
                    	OssMaster ossMaster = CoCodeManager.OSS_INFO_UPPER.get(key);
                    	if (ossMaster != null) {
                    		enterpriseIntegrationMapper.updateEnterpriseIntegrationJobOssId(jobSeq, ossMaster.getOssId(), ossMaster.getOssName(), ossMaster.getOssVersion());
                    		chunkMap.put(key, ossMaster);
                    	} else {
                    		requiredAnalysisOssList.add(bean);
                    	}
                    } else {
                    	requiredAnalysisOssList.add(bean);
                    }
                    
                    analysisDataCount++;
                    allowedReviewCount++;
                }
                
                if (allowedReviewCount != size) {
                	rejectedReviewCount = size - allowedReviewCount;
                } 
                
		        result.put("jobSeq", jobSeq);
	            result.put("referenceId", referenceId);
	            result.put("ossComponents", requiredAnalysisOssList);
	            if (rejectedReviewCount > 0) {
	            	result.put("limitCount", limitCount);
		            result.put("rejectedReviewCount", rejectedReviewCount);
	            }
	            if (MapUtils.isNotEmpty(chunkMap)) {
	            	result.put("chunkMap", chunkMap);
	            }
	            if (CollectionUtils.isEmpty(requiredAnalysisOssList)) {
	            	result.put("skipCoReviewer", CoConstDef.FLAG_YES);
	            }
		    }
	    } catch (Exception e) {
	        log.error("Parsing or DB Error for key {}: {}", analysisKey, e.getMessage());
	    }
	    
	    return result;
	}

	@SuppressWarnings("unchecked")
	private String postProcessEnterpriseAnalysis(Map<String, Object> preResult, String prjId, String companyName, String userId) {
		// Create a new project
		int jobSeq = (Integer) preResult.get("jobSeq");
	    String targetPrjId = "";
	    
	    try {
	    	targetPrjId = createProjectFromEnterpriseTargets(preResult, prjId, companyName, userId);
	    } catch (Exception e) {
	    	log.error("[Error] Failed to postProcessEntAnalysis: ENT_{}. Reason: {}", prjId, e.getMessage(), e);
	    	return null;
	    }
		
	    Project project = new Project();
	    project.setPrjId(targetPrjId);
	    
		String referenceId = (String) preResult.get("referenceId");
	    List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) preResult.get("ossComponents");
	    
	    // Data mapping for reflecting Identification SRC tab
	    for (ProjectIdentification bean : ossComponents) {
	        bean.setReferenceId(targetPrjId);
	        bean.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_SRC);
	        bean.setJobSeq(jobSeq);
	    }

	    // Saves data to Identification SRC tab
	    List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);
	    projectService.registSrcOss(ossComponents, ossComponentsLicense, project);

	    // Saves data to Identification SBOM tab
	    projectService.registBom(targetPrjId, CoConstDef.FLAG_YES, new ArrayList<>(), new ArrayList<>());
	    
	    log.info("[PostProcess Success] JobSeq: {}, ReferenceId: ENT_{}, targetPrjId: {}", preResult.get("jobSeq"), referenceId, targetPrjId);
	    
	    return targetPrjId;
	}

	private String createProjectFromEnterpriseTargets(Map<String, Object> preResult, String prjId, String companyName, String userId) {
		String prjName = "Enterprise_" + prjId + "_" + companyName;
		String prjVersion;
		Double maxVersion = projectService.selectMaxProjectVersion(prjName);

		if (maxVersion == null) {
		    prjVersion = "1.0";
		} else {
		    BigDecimal currentVer = BigDecimal.valueOf(maxVersion);
		    prjVersion = currentVer.add(new BigDecimal("0.1")).setScale(1, RoundingMode.HALF_UP).toString();
		}
		
		Project project = new Project();
		project.setPrjName(prjName);
		project.setPrjVersion(prjVersion);
		project.setPriority(CoConstDef.CD_PRIORITY_P2);
		project.setOsType(CoCodeManager.getCodeDtlsRegardlessUseYn(CoConstDef.CD_OS_TYPE).get(0).getCdDtlNo());
		project.setDistributionType(CoConstDef.CD_NOTICE_TYPE_GENERAL);
		project.setDistributeTarget(CoConstDef.CD_DTL_DISTRIBUTE_NA);
		project.setNetworkServerType(CoConstDef.FLAG_NO);
		project.setModelList(new ArrayList<>());
		project.setCreator(userId);
		project.setLoginUserName(userId);
		
		projectService.registProject(project);
		
		if (preResult.containsKey("rejectedReviewCount")) {
			String comment = String.format("<p>limit exceeded: %s allowed, %s requests were rejected.</p>", preResult.get("limitCount"), preResult.get("rejectedReviewCount"));
			CommentsHistory commentsHistory = new CommentsHistory();
			commentsHistory.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
			commentsHistory.setReferenceId(project.getPrjId());
			commentsHistory.setContents(comment);
			commentsHistory.setLoginUserName(userId);
			commentService.registComment(commentsHistory);
		}
		
		return project.getPrjId();
	}

	@Override
	@Transactional
	public void executeAnalysisUpdate(String jobSeq, String ossId, String prjId, boolean isResponseRequired) {
		try {
			Map<String, Object> paramMap = new HashMap<>();
			if (isResponseRequired) {
				int updatedCount = enterpriseIntegrationMapper.updateEnterpriseIntegrationJob(jobSeq);
				log.info("Successfully updated {} rows in ENT_ANALYSIS_JOB.", updatedCount);
				
				if (updatedCount > 0) {
					List<EnterpriseIntegrationBean> updatedData = enterpriseIntegrationMapper.getUpdatedEnterpriseIntegrationJobs(jobSeq);
	                if (CollectionUtils.isNotEmpty(updatedData)) {
	                	paramMap.put("jobData", updatedData);
	                }
				} else {
	                log.warn("No rows were updated. Skipping external API call.");
	            }
				
				log.info("[executeAnalysisUpdate isResponseRequired] {} {}", paramMap, isResponseRequired);
			} else if (isEmpty(prjId)) {
				int jobSeqInt = Integer.parseInt(jobSeq);
	            // Update success and failure counts for the analysis job
	            enterpriseIntegrationMapper.updateEnterpriseIntegrationJobCount(jobSeqInt);
	            EnterpriseIntegrationBean enterpriseIntegrationBean = enterpriseIntegrationMapper.getEnterpriseIntegrationJob(jobSeqInt);
	            if (enterpriseIntegrationBean != null) {
	            	paramMap.put("updateJobData", enterpriseIntegrationBean);
	            }
			}
			
			if (!isEmpty(ossId)) {
				OssMaster ossMaster = new OssMaster();
				ossMaster.setOssId(ossId);
				
            	OssMaster bean = ossService.getOssMasterOne(ossMaster);
            	if (bean != null) {
            		if (!isEmpty(bean.getCreator()) && isEmpty(bean.getModifier())) {
            			bean.setModifier(bean.getCreator());
            		}
            		paramMap.put("ossInfo", bean);
            		if (bean.getOssLicenses() != null) {
            			List<LicenseMaster> licenseMasterList = new ArrayList<>();
            			for (OssLicense license : bean.getOssLicenses()) {
            				if (!isEmpty(license.getLicenseId())) {
            					LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_BY_ID.get(license.getLicenseId());
            					if (licenseMaster != null) {
            						String todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            						if (licenseMaster.getCreatedDate().startsWith(todayStr)) {
            							licenseMasterList.add(licenseMaster);
            		                }
            					}
            				}
            			}
            			if (CollectionUtils.isNotEmpty(licenseMasterList)) {
            				paramMap.put("licenseInfo", licenseMasterList);
            			}
            		}
            	}
            }
            if (MapUtils.isNotEmpty(paramMap)) {
            	sendEntAnalysisDataToExternalDomain(paramMap);
            }
            log.info("Data transfer to external domain completed successfully.");
		} catch (DataAccessException e) {
            // Error during Database operation
            log.error("Database error occurred while updating ENT_ANALYSIS_JOB: {}", e.getMessage());
            throw new RuntimeException("Failed to update database records.", e);

        } catch (RestClientException e) {
            // Error during API call (External domain)
            log.error("External API communication error: {}", e.getMessage());
            throw new RuntimeException("Failed to transfer data to the external domain.", e);

        } catch (Exception e) {
            // Unexpected system error
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("Unexpected system failure during synchronization.", e);
        }
	}

	private void sendEntAnalysisDataToExternalDomain(Map<String, Object> paramMap) {
		String targetUrl = enterpriseIntegrationUrl + "/api/v2/coReviewer/ossAnalysis/jobs/receive";
		try {
			HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(paramMap, headers);
            ResponseEntity<String> response = enterpriseApiRestTemplate.exchange(targetUrl, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("External server returned an error status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error during API execution in sendEntAnalysisDataToExternalDomain: {}", e.getMessage());
            throw e;
        }
	}

	@Override
	public void syncLicenseToEnterpriseInterface() {
		if (isEmpty(enterpriseIntegrationUrl)) {
			return;
		}
		
		try {
			log.info("Starting sync License Info To Enterprise Interface");
			CoCodeManager.getInstance().refreshLicenseInfo();
			
			if (MapUtils.isEmpty(CoCodeManager.LICENSE_INFO_BY_ID)) {
				log.warn("No License Info data found to sync");
	            return;
			}
			
			String todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			Map<String, LicenseMaster> modifiedLicenseInfo = CoCodeManager.LICENSE_INFO_BY_ID.entrySet().stream()
																		    .filter(entry -> entry.getValue() != null && entry.getValue().getModifiedDate() != null)
																		    .filter(entry -> entry.getValue().getModifiedDate().startsWith(todayStr))
																		    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			if (MapUtils.isNotEmpty(modifiedLicenseInfo)) {
				syncLicenseInfoByChunk(modifiedLicenseInfo);
			} else {
				log.info("No modified license data found to sync. Skipping process.");
			}
		} catch (Exception e) {
			clearSyncToEnterprise("license");
			log.error("Failed to sync License Info to Enterprise: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void syncOssToEnterpriseInterface() {
		try {
			log.info("Starting sync Oss Info To Enterprise Interface");
			CoCodeManager.getInstance().refreshOssInfo();
			
			if (MapUtils.isEmpty(CoCodeManager.OSS_INFO_BY_ID)) {
				log.warn("No OSS Info data found to sync");
	            return;
			}
			
			String todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			Map<String, OssMaster> modifiedOssInfo = CoCodeManager.OSS_INFO_BY_ID.entrySet().stream()
																    .filter(entry -> entry.getValue() != null && entry.getValue().getModifiedDate() != null)
																    .filter(entry -> entry.getValue().getModifiedDate().startsWith(todayStr))
																    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			if (MapUtils.isNotEmpty(modifiedOssInfo)) {
				syncOssInfoByChunk(modifiedOssInfo);
			} else {
				log.info("No modified OSS info found to sync. Skipping process.");
			}
		} catch (Exception e) {
			clearSyncToEnterprise("ossAnalysis");
			log.error("Failed to sync OSS Info to Enterprise: " + e.getMessage(), e);
		}
	}

	private void syncOssInfoByChunk(Map<String, OssMaster> ossInfo) throws Exception {
		int batchSize = 2000;
        int totalSize = ossInfo.size();
        
        Map<String, OssMaster> chunkMap = new HashMap<>(batchSize);
        int currentCount = 0;
        int sentCount = 0;
		
        log.info("Total data to sync: {} items. Starting memory-safe streaming...", totalSize);
        
        clearSyncToEnterprise("ossAnalysis");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        for (Map.Entry<String, OssMaster> entry : ossInfo.entrySet()) {
        	OssMaster ossMaster = entry.getValue();
        	ossMaster.setSyncFlag(CoConstDef.FLAG_YES);
        	
        	chunkMap.put(entry.getKey(), ossMaster);
            currentCount++;

            if (currentCount == batchSize || (sentCount + currentCount) == totalSize) {
            	String jsonBody = objectMapper.writeValueAsString(chunkMap);
            	HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            	sendChunkDataToEnterprise("ossAnalysis", entity);
                
                sentCount += currentCount;
                log.info("Sent chunk successfully: {} / {} items processed.", sentCount, totalSize);
                
                chunkMap.clear();
                currentCount = 0;
            }
		}
        
        completeSyncToEnterprise("ossAnalysis", true);
        log.info("OSS Info sync to Enterprise completed successfully");
	}

	private void syncLicenseInfoByChunk(Map<String, LicenseMaster> licenseInfo) throws Exception {
		int batchSize = 2000;
        int totalSize = licenseInfo.size();
        
        Map<String, LicenseMaster> chunkMap = new HashMap<>(batchSize);
        int currentCount = 0;
        int sentCount = 0;
        
        log.info("Total license data to sync: {} items. Starting memory-safe streaming...", totalSize);
        
        clearSyncToEnterprise("license");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        for (Map.Entry<String, LicenseMaster> entry : licenseInfo.entrySet()) {
        	chunkMap.put(entry.getKey(), entry.getValue());
            currentCount++;

            if (currentCount == batchSize || (sentCount + currentCount) == totalSize) {
            	String jsonBody = objectMapper.writeValueAsString(chunkMap);
            	
            	HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            	sendChunkDataToEnterprise("license", entity);
                
                sentCount += currentCount;
                log.info("Sent chunk successfully: {} / {} items processed.", sentCount, totalSize);
                
                chunkMap.clear();
                currentCount = 0;
            }
		}
        
        completeSyncToEnterprise("license", false);
        log.info("License Info sync to Enterprise completed successfully.");
	}
	
	private void clearSyncToEnterprise(String category) {
		String targetUrl = enterpriseIntegrationUrl + "/api/v2/coReviewer/" + category + "/sync/clear";
		log.info("Sending request to clear sync API: {}", targetUrl);
		
		try {
			ResponseEntity<String> response = enterpriseApiRestTemplate.postForEntity(targetUrl, null, String.class);
	        if (!response.getStatusCode().is2xxSuccessful()) {
	            throw new RuntimeException("External server returned an error status during preparation: " + response.getStatusCode());
	        }
	        log.info("Sync preparation completed on Enterprise server. Response: {}", response.getBody());
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to prepare sync to Enterprise: " + e.getMessage(), e);
	    }
	}

	private void sendChunkDataToEnterprise(String category, HttpEntity<String> entity) {
		String targetUrl = enterpriseIntegrationUrl + "/api/v2/coReviewer/" + category + "/sync/chunk";
		try {
            ResponseEntity<String> response = enterpriseApiRestTemplate.exchange(targetUrl, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("External server returned an error status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error during API execution in sendEntAnalysisDataToExternalDomain: {}", e.getMessage());
            throw e;
        }
	}
	
	private void completeSyncToEnterprise(String category, boolean includeData) {
		String targetUrl = enterpriseIntegrationUrl + "/api/v2/coReviewer/" + category + "/sync/complete";
        log.info("Sending request to complete sync API: {}", targetUrl);
        try {
        	HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity;
            
            if (includeData) {
                List<EnterpriseIntegrationBean> enterpriseIntegrationJobs = enterpriseIntegrationMapper.getEnterpriseIntegrationJobs();
                entity = new HttpEntity<>(enterpriseIntegrationJobs, headers);
            } else {
                entity = new HttpEntity<>(headers); 
            }
            
            ResponseEntity<String> response = enterpriseApiRestTemplate.exchange(targetUrl, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
            	throw new RuntimeException("External server returned an error status during completion: " + response.getStatusCode());
            }
            log.info("Sync transaction completed on Enterprise server. Response: {}", response.getBody());
        } catch (Exception e) {
        	log.error("Error during API execution in completeSyncToEnterprise: {}", e.getMessage(), e);
            throw e;
        }
	}

	@Override
	public String getEnterpriseAnalysisInfo(String prjId) {
		return enterpriseIntegrationMapper.getEnterpriseAnalysisInfo(prjId);
	}
}
