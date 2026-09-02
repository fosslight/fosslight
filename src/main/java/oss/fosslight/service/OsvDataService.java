package oss.fosslight.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.OsvDataMapper;
import us.springett.cvss.Cvss;
import us.springett.cvss.Score;

@Service("OsvDataService")
@Slf4j
public class OsvDataService extends CoTopComponent {
	static final Logger schlog = LoggerFactory.getLogger("SCHEDULER_LOG");

	@Autowired
	CodeMapper codeMapper;
	@Autowired
	OsvDataMapper osvDataMapper;
	@Autowired
	SqlSessionFactory sqlSessionFactory;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
    private OsvDataService self;

	private final RestTemplate osvDataApiRestTemplate;
	private static final int BATCH_SIZE = 1000;

	private static final List<String> SEVERITY_KEYS = Arrays.asList("severity", "score", "base_score", "cvss_score", "cvss", "urgency", "raw_severity", "priority");

	private static final Pattern REVISION_PATTERN = Pattern.compile("-r\\d+");

	public OsvDataService(@Qualifier("osvDataApiRestTemplate") RestTemplate osvDataApiRestTemplate) {
		this.osvDataApiRestTemplate = osvDataApiRestTemplate;
	}

	public String executeOsvDataSync() throws IOException {
		boolean initializeFlag = false;
		if (CoConstDef.FLAG_YES.equalsIgnoreCase(codeMapper.getCodeDtlNm("990", "100"))) {
			initializeFlag = true;
		}
		try {
			schlog.info("[OSV] Start Total/Full Data Synchronization.");
			osvDataTotalBulkJob(initializeFlag, osvDataApiRestTemplate, BATCH_SIZE);
		} catch (Exception e) {
			schlog.error(e.getMessage(), e);
			return "99";
		}
		return "00";
	}

	/**
	 * [TOTAL SYNC] Process full database reload via intermediate staging tables
	 * (_TEMP)
	 */
	private void osvDataTotalBulkJob(boolean initializeFlag, RestTemplate restTemplate, int batchSize) {
		schlog.info("[OSV] Starting full OSV snapshot download.");

		String downloadUrl = "https://storage.googleapis.com/osv-vulnerabilities/all.zip";

		final int MAX_RETRY = 5;
		int retryCount = 0;
		boolean success = false;

		while (!success && retryCount < MAX_RETRY) {
	        try {
	            schlog.info("[OSV] Download attempt {}/{} started.", retryCount + 1, MAX_RETRY);

	            restTemplate.execute(downloadUrl, HttpMethod.GET,
	                request -> {
	                    request.getHeaders().set("Accept-Encoding", "identity");
	                    request.getHeaders().set("Connection", "keep-alive");
	                },
	                response -> {
	                    if (!response.getStatusCode().is2xxSuccessful()) {
	                        throw new IOException("HTTP Status : " + response.getStatusCode());
	                    }

	                    schlog.info("[OSV] Download connection established. Processing snapshot stream directly.");

	                    try (
	                        InputStream is = response.getBody();
	                        ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8);
	                        SqlSession sqlSession =
	                            sqlSessionFactory.openSession(ExecutorType.BATCH)
	                    ) {

	                        int count = 0;
	                        int jsonCount = 0;

	                        ZipEntry entry;

	                        byte[] buffer = new byte[1024 * 1024];

	                        while ((entry = zis.getNextEntry()) != null) {

	                            try {
	                                if (entry.isDirectory() || !entry.getName().endsWith(".json")) {
	                                    continue;
	                                }

	                                jsonCount++;

	                                ByteArrayOutputStream bos = new ByteArrayOutputStream();

	                                int len;
	                                while ((len = zis.read(buffer)) != -1) {
	                                    bos.write(buffer, 0, len);
	                                }

	                                String jsonContent = bos.toString("UTF-8");

	                                try {
	                                    boolean isProcess = processAndQueueOsvRecord(sqlSession, jsonContent, entry.getName(), initializeFlag);
	                                    if (isProcess) {
	                                        count++;
	                                    }
	                                } catch (Exception ex) {
	                                    log.error("[OSV] Failed to process JSON file [{}]: {}", entry.getName(), ex.getMessage(), ex);
	                                }

	                                jsonContent = null;
	                                bos = null;

	                                if (count > 0 && count % batchSize == 0) {
	                                    sqlSession.flushStatements();
	                                    schlog.info("[OSV] Batch flushed. Processed {} records.", count);
	                                }

	                            } finally {
	                                zis.closeEntry();
	                            }
	                        }

	                        sqlSession.flushStatements();
	                        schlog.info("[OSV] Final batch flushed. Total JSON files: {}, processed records: {}", jsonCount, count);

	                        sqlSession.commit();
	                        schlog.info("[OSV] Snapshot processing completed successfully. Total records processed: {}", count);
	                    }

	                    return null;
	                }
	            );

	            success = true;
	            schlog.info("[OSV] Full OSV snapshot processing completed successfully.");
	        } catch (Exception e) {
	            retryCount++;
	            schlog.error("[OSV] Download attempt {}/{} failed.", retryCount, MAX_RETRY, e);

	            if (retryCount >= MAX_RETRY) {
	                schlog.error("[OSV] Maximum retry count ({}) exceeded. OSV snapshot processing aborted.", MAX_RETRY, e);
	                break;
	            }

	            try {
	                schlog.warn("[OSV] Retrying download in 30 seconds...");
	                Thread.sleep(1000L * 30);
	            } catch (InterruptedException ie) {
	                Thread.currentThread().interrupt();
	                schlog.warn("[OSV] Retry wait interrupted. Job terminated.");

	                return;
	            }
	        }
	    }

		if (success) {
			try {
				self.refreshOsvSearchMaster();
			} catch (Exception e) {
				log.error("[OSV] Failed to refresh OSV search master.", e);
			}
		} else {
			schlog.error("[OSV] OSV snapshot processing failed. Search master refresh skipped.");
		}

		schlog.info("[OSV] OSV bulk download job finished.");
	}

	@Transactional
	public void refreshOsvSearchMaster() {
		schlog.info("[OSV] Refresh Osv Search Master");

		osvDataMapper.setGroupConcatMaxLen();

		// Range Temp
		osvDataMapper.dropOsvRangeTemp();
		osvDataMapper.createOsvRangeTemp();
		osvDataMapper.createIndexOsvRangeTemp();

		// Version Temp
		osvDataMapper.dropOsvVersionTemp();
		osvDataMapper.createOsvVersionTemp();
		osvDataMapper.createIndexOsvVersionTemp();

		// Severity Temp
		osvDataMapper.dropOsvSeverityTemp();
		osvDataMapper.createOsvSeverityTemp();
		osvDataMapper.createIndexOsvSeverityTemp();

		// Pre Merge Temp
		osvDataMapper.dropOsvPreMergeTemp();
		osvDataMapper.createOsvPreMergeTemp();
		osvDataMapper.createIndexOsvPreMergeTemp();

		// Search Master Temp
		osvDataMapper.dropOsvSearchMasterTemp();
		osvDataMapper.createOsvSearchMasterTemp();
		osvDataMapper.insertOsvSearchMasterData();
		osvDataMapper.updateOsvSearchMasterSeverityDefault();

		// Indexes
		osvDataMapper.createIndexMasterId();
		osvDataMapper.createIndexMasterWithdrawn();
		osvDataMapper.createIndexMasterNameP1();
		osvDataMapper.createIndexMasterNameP2();
		osvDataMapper.createIndexMasterNameP3();
		osvDataMapper.createIndexMasterVersionP1Yn();
		osvDataMapper.createIndexMasterVersionP2Yn();
		osvDataMapper.createIndexMasterVersionP3Yn();
		osvDataMapper.createIndexMasterSevType();
		osvDataMapper.createIndexMasterAffectedVersion();

		// Swap & Cleanup
		osvDataMapper.dropOsvSearchMaster();
		osvDataMapper.renameOsvSearchMasterTemp();
		osvDataMapper.dropOsvRangeTemp();
		osvDataMapper.dropOsvVersionTemp();
		osvDataMapper.dropOsvSeverityTemp();
		osvDataMapper.dropOsvPreMergeTemp();

		schlog.info("[OSV] OSV search master refresh completed.");
	}

	private boolean processAndQueueOsvRecord(SqlSession sqlSession, String jsonContent, String fileName, boolean initializeFlag) throws Exception {
		JsonNode root = objectMapper.readTree(jsonContent);
		String osvId = root.get("id").asText();
		Instant jsonModiInstant = root.has("modified") ? Instant.parse(root.get("modified").asText()) : null;

		boolean isUpdate = false;
		Vulnerability bean = sqlSession.selectOne("oss.fosslight.repository.OsvDataMapper.selectOsvVulnerabilityInfo", osvId);
		if (bean != null) {
			Timestamp dbModiDate = Timestamp.valueOf(bean.getModiDate());
			Instant dbModiInstant = dbModiDate != null ? dbModiDate.toInstant() : null;
			if (jsonModiInstant != null) {
				if (!jsonModiInstant.equals(dbModiInstant)) {
					isUpdate = true;
				} else {
					return true;
				}
			}
		}

		Set<String> insertedOsvSeveritySet = new HashSet<>();
		Set<String> versionSet = new HashSet<>();

		// Parse and Map Main Vulnerability Properties (OSV_VULNERABILITIES)
		Map<String, Object> vulnParam = new HashMap<>();
		vulnParam.put("osvId", osvId);
		vulnParam.put("schemaVersion", root.path("schema_version").asText(null));
		vulnParam.put("summary", root.path("summary").asText(null));
		vulnParam.put("details", root.path("details").asText(null));
		vulnParam.put("publDate", root.has("published") ? Timestamp.from(Instant.parse(root.get("published").asText())) : null);
		vulnParam.put("modiDate", root.has("modified") ? Timestamp.from(jsonModiInstant) : null);
		vulnParam.put("withdrawn", root.has("withdrawn") ? Timestamp.from(Instant.parse(root.get("withdrawn").asText())) : null);
		vulnParam.put("rawJson", jsonContent);

		if (!isUpdate) {
			sqlSession.insert("oss.fosslight.repository.OsvDataMapper.insertOsvVulnerability", vulnParam);
		} else {
			vulnParam.put("id", bean.getId());
			sqlSession.update("oss.fosslight.repository.OsvDataMapper.updateOsvVulnerability", vulnParam);
		}
		sqlSession.flushStatements();

		int id;
		if (isUpdate) {
			id = Integer.parseInt(bean.getId());
		} else {
			Object idObj = vulnParam.get("id");
			if (!(idObj instanceof Number)) {
				schlog.info("[insertOsvVulnerability failed] {} | {}", osvId, fileName);
				return false;
			}
			id = ((Number) idObj).intValue();
		}

		if (isUpdate) {
			sqlSession.delete("oss.fosslight.repository.OsvDataMapper.deleteOsvReferences", osvId);
			sqlSession.delete("oss.fosslight.repository.OsvDataMapper.deleteOsvAffectedPackageRange", id);
			sqlSession.delete("oss.fosslight.repository.OsvDataMapper.deleteOsvAffectedPackageVersion", id);
			sqlSession.delete("oss.fosslight.repository.OsvDataMapper.deleteOsvAffectedPackage", id);
			sqlSession.delete("oss.fosslight.repository.OsvDataMapper.deleteOsvAlias", id);
			sqlSession.delete("oss.fosslight.repository.OsvDataMapper.deleteOsvSeverity", id);
		}

		// Map and Queue Alternate IDs (OSV_ALIASES)
		if (root.has("aliases")) {
			for (JsonNode aliasNode : root.get("aliases")) {
				if (aliasNode == null) {
					continue;
				}
				Map<String, Object> aliasParam = Map.of("id", id, "aliasId", aliasNode.asText());
				sqlSession.insert("oss.fosslight.repository.OsvDataMapper.insertOsvAlias", aliasParam);
			}
		}

		// Map and Queue Severity Vectors (OSV_SEVERITIES)
		if (root.has("severity")) {
			for (JsonNode sevNode : root.get("severity")) {
				if (sevNode == null) {
					continue;
				}
				insertOsvSeverity(sqlSession, insertedOsvSeveritySet, id, "*", "*", "GLOBAL", sevNode.path("type").asText(), sevNode.path("score").asText());
			}
		}

		if (root.has("database_specific")) {
			JsonNode db = root.get("database_specific");
			if (db != null) {
				String severify = extractSeverityFromSpecific(db);
				if (severify != null) {
					insertOsvSeverity(sqlSession, insertedOsvSeveritySet, id, "*", "*", "GLOBAL_DB", "N/A", severify);
				}
			}
		}

		// Map and Queue Affected Packages along with Nested Metadata
		if (root.has("affected")) {
			int packageIdx = 1;

			for (JsonNode affectedNode : root.get("affected")) {
				String ecosystem = "";
				String packageName = "";
				String purl = "";

				if (affectedNode.has("package")) {
					JsonNode pkgNode = affectedNode.get("package");
					ecosystem = pkgNode.path("ecosystem").asText();
					packageName = pkgNode.path("name").asText();
					purl = pkgNode.has("purl") ? pkgNode.path("purl").asText() : "";
				}

				Map<String, Object> pkgParam = new HashMap<>();
				pkgParam.put("id", id);
				pkgParam.put("packageIdx", packageIdx);
				pkgParam.put("ecosystem", ecosystem);
				pkgParam.put("packageName", packageName);
				pkgParam.put("purl", purl);

				sqlSession.insert("oss.fosslight.repository.OsvDataMapper.insertOsvAffectedPackage", pkgParam);

				if (affectedNode.has("severity")) {
					for (JsonNode sevNode : affectedNode.get("severity")) {
						if (sevNode == null) {
							continue;
						}
						insertOsvSeverity(sqlSession, insertedOsvSeveritySet, id, packageName, ecosystem, "PACKAGE", sevNode.path("type").asText(), sevNode.path("score").asText());
					}
				}

				// Fallback Mechanism: Store Ecosystem-Specific Severity if standard top-level
				// score is missing
				if (affectedNode.has("ecosystem_specific")) {
					JsonNode ecoSpec = affectedNode.get("ecosystem_specific");
					if (ecoSpec != null) {
						String severify = extractSeverityFromSpecific(ecoSpec);
						if (severify != null) {
							insertOsvSeverity(sqlSession, insertedOsvSeveritySet, id, packageName, ecosystem, "PACKAGE_ECO", "N/A", severify);
						}
					}
				}

				if (affectedNode.has("database_specific")) {
					JsonNode db = affectedNode.get("database_specific");
					if (db != null) {
						String severify = extractSeverityFromSpecific(db);
						if (severify != null) {
							insertOsvSeverity(sqlSession, insertedOsvSeveritySet, id, packageName, ecosystem, "PACKAGE_DB", "N/A", severify);
						}
					}
				}

				// Map and Queue Exact-Match Versions (OSV_AFFECTED_PACKAGES_VERSIONS)
				if (affectedNode.has("versions")) {
					int versionIdx = 1;
					for (JsonNode verNode : affectedNode.get("versions")) {
						String version = verNode.asText();
						String key = String.valueOf(id) + "|" + String.valueOf(packageIdx) + "|" + String.valueOf(versionIdx);

						if (!versionSet.add(key)) {
							continue;
						}

						Map<String, Object> verParam = Map.of("id", id, "packageIdx", packageIdx, "versionIdx", versionIdx, "version", version);
						sqlSession.insert("oss.fosslight.repository.OsvDataMapper.insertOsvAffectedPackageVersion", verParam);
						versionIdx++;
					}
				}

				// Map and Queue State Interval Ranges (OSV_PACKAGE_RANGES)
				if (affectedNode.has("ranges")) {
					int rangeIdx = 1;
					for (JsonNode rangeNode : affectedNode.get("ranges")) {
						Map<String, Object> rangeParam = new HashMap<>();
						rangeParam.put("id", id);
						rangeParam.put("packageIdx", packageIdx);
						rangeParam.put("rangeIdx", rangeIdx);
						rangeParam.put("rangeType", rangeNode.get("type").asText());
						rangeParam.put("repo", rangeNode.has("repo") ? rangeNode.get("repo").asText() : null);

						String introduced = "0";
						String fixed = null;
						String lastAffected = null;
						String limitVersion = "*";

						// Track internal state change events
						if (rangeNode.has("events")) {
							for (JsonNode eventNode : rangeNode.get("events")) {
								if (eventNode.has("introduced")) {
									introduced = eventNode.get("introduced").asText();
								}
								if (eventNode.has("fixed")) {
									fixed = eventNode.get("fixed").asText();
								}
								if (eventNode.has("last_affected")) {
									lastAffected = eventNode.get("last_affected").asText();
								}
								if (eventNode.has("limit")) {
									limitVersion = eventNode.get("limit").asText();
								}
							}
						}

						rangeParam.put("introduced", introduced);
						rangeParam.put("fixed", fixed);
						rangeParam.put("lastAffected", lastAffected);
						rangeParam.put("limitVersion", limitVersion);

						sqlSession.insert("oss.fosslight.repository.OsvDataMapper.insertOsvAffectedPackageRange", rangeParam);
						rangeIdx++;

						if (rangeNode.has("database_specific")) {
							JsonNode db = rangeNode.get("database_specific");
							if (db != null) {
								String severify = extractSeverityFromSpecific(db);
								if (severify != null) {
									insertOsvSeverity(sqlSession, insertedOsvSeveritySet, id, packageName, ecosystem, "PACKAGE_RANGE_DB", "N/A", severify);
								}
							}
						}
					}
				}

				packageIdx++;
			}
		}

		if (root.has("references")) {
			int referenceIdx = 1;
			for (JsonNode refNode : root.get("references")) {
				String type = refNode.path("type").asText(null);
				if (!"FIX".equalsIgnoreCase(type)) {
					continue;
				}

				String patchLink = refNode.path("url").asText(null);
				Map<String, Object> verParam = Map.of("osvId", osvId, "refIdx", referenceIdx++, "type", type, "patchLink", patchLink);

				sqlSession.insert("oss.fosslight.repository.OsvDataMapper.insertOsvReferences", verParam);
			}
		}

		return true;
	}

	private String extractSeverityFromSpecific(JsonNode node) {
		for (String key : SEVERITY_KEYS) {
			JsonNode value = node.get(key);

			if (value == null || value.isNull()) {
				continue;
			}

			if (value.isValueNode()) {
				return value.asText();
			}

			if (value.has("score")) {
				return value.get("score").asText();
			}

			if (value.has("value")) {
				return value.get("value").asText();
			}
		}
		return null;
	}

	private void insertOsvSeverity(SqlSession sqlSession, Set<String> insertedOsvSeveritySet, int id, String packageName, String ecosystem, String source, String type, String score) {
		if (isEmpty(type) || isEmpty(score)) {
			return;
		}

		String key = String.valueOf(id) + "|" + avoidNull(packageName) + "|" + avoidNull(ecosystem) + "|" + source + "|" + type;
		if (!insertedOsvSeveritySet.add(key)) {
			return;
		}

		String finalScore = score;
		if (isCvssType(type, score)) {
			try {
				finalScore = calculateCvssScore(score);
				// Skip insertion if calculation fails or returns null for unparseable vectors
	            if (finalScore == null) {
	                return;
	            }
			} catch (Exception e) {
		        // Log the exception and exit the flow when calculation fails
		        schlog.error("Failed to calculate CVSS score for vector: {}", score, e);
		        return;
		    }
		} else {
			return;
		}

		Map<String, Object> param = new HashMap<>();
		param.put("id", id);
		param.put("packageName", packageName);
		param.put("ecosystem", ecosystem);
		param.put("source", source);
		param.put("type", type);
		param.put("score", finalScore);

		sqlSession.insert("oss.fosslight.repository.OsvDataMapper.insertOsvSeverity", param);
	}

	private boolean isCvssType(String type, String score) {
		if (type == null || score == null) {
			return false;
		}
		String upperType = type.toUpperCase();
		return upperType.contains("CVSS") || score.toUpperCase().startsWith("CVSS:");
	}

	private String calculateCvssScore(String vectorString) {
		if (isEmpty(vectorString)) {
	        return null;
	    }
		
		String trimmed = vectorString.trim();
		
		// Pre-check and fix malformed vectors BEFORE calling the external parser
	    String targetVector = fixMalformedVector(trimmed);
	    if (targetVector == null) {
	        schlog.warn("Skipping unparseable or invalid CVSS vector string: [{}]", trimmed);
	        return null;
	    }
		
		try {
			// Try parsing with the standard vector string
	        Cvss cvss = Cvss.fromVector(trimmed);
	        if (cvss != null) {
	            Score cvssScore = cvss.calculateScore();
	            if (!targetVector.equals(trimmed)) {
	                schlog.warn("Successfully recovered malformed CVSS vector from [{}] to [{}]", trimmed, targetVector);
	            }
	            return String.valueOf(cvssScore.getBaseScore());
	        }
	    } catch (Exception e) {
	    	// Catch any remaining unexpected parsing exceptions to prevent batch interruption
	        schlog.warn("Failed to parse CVSS vector even after correction: [{}]. Error: {}", trimmed, e.getMessage());
	        return null;
	    }
		
	    return null;
	}

	private String fixMalformedVector(String vector) {
		if (isEmpty(vector)) {
	        return null;
	    }
	    
		String trimmed = vector.trim();
		
	    // Filter out invalid or non-standard vector formats (e.g., "None", too short strings)
		if (trimmed.toUpperCase().contains("NONE") || trimmed.length() < 5) {
	        return null;
	    }
	    
	    // Case: Declared as CVSS:3.x but contains v2 specific metric ('Au:'), convert to CVSS 2.0
		if (trimmed.toUpperCase().startsWith("CVSS:3") && trimmed.contains("Au:")) {
	        return trimmed.replaceFirst("(?i)CVSS:3\\.[01]", "CVSS:2.0");
	    }
		// Case: CVSS v3.x vector containing 'Au:' without strict prefix or mixed format
	    if (trimmed.contains("Au:") && !trimmed.toUpperCase().startsWith("CVSS:2")) {
	        // If it has a wrong prefix or missing prefix, ensure it's treated as CVSS 2.0
	        int slashIdx = trimmed.indexOf('/');
	        if (slashIdx != -1) {
	            return "CVSS:2.0" + trimmed.substring(slashIdx);
	        } else {
	            return "CVSS:2.0/" + trimmed;
	        }
	    }
	    
	    // Case: Missing CVSS prefix entirely
	    if (!trimmed.toUpperCase().startsWith("CVSS:")) {
	        if (trimmed.contains("Au:")) {
	            return "CVSS:2.0/" + trimmed;
	        } else {
	            return "CVSS:3.1/" + trimmed;
	        }
	    }
	    
	    return trimmed;
	}
	
	// 메모리 이슈 대응
	// String.format 대체을 위핸 key 클래스 추가
	private static final class VulnGroupKey {
	    private final String ossName;
	    private final String ossVersion;
	    private final String cveId;

	    public VulnGroupKey(String ossName, String ossVersion, String cveId) {
	        this.ossName = ossName;
	        this.ossVersion = ossVersion;
	        this.cveId = cveId;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        VulnGroupKey that = (VulnGroupKey) o;
	        return Objects.equals(ossName, that.ossName) &&
	               Objects.equals(ossVersion, that.ossVersion) &&
	               Objects.equals(cveId, that.cveId);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(ossName, ossVersion, cveId);
	    }
	}

	// 매모리 이슈 대응
	private static final Pattern COMPONENT_PATTERN = Pattern.compile("([\\[\\(])([^,\\])]+),\\s*([^,\\])]+)([\\]\\)])");
	private static final class FinalGroupKey {
	    private final String ossName;
	    private final String ossVersion;
	    private final String cveId;

	    public FinalGroupKey(String ossName, String ossVersion, String cveId) {
	        // 생성 시점에 단 한 번만 정제하여 힙 메모리 낭비 방지
	        this.ossName = ossName != null ? ossName.toUpperCase().trim() : "";
	        this.ossVersion = ossVersion != null ? ossVersion.toUpperCase().trim() : "";
	        this.cveId = cveId != null ? cveId.toUpperCase().trim() : "";
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        FinalGroupKey that = (FinalGroupKey) o;
	        return Objects.equals(ossName, that.ossName) &&
	               Objects.equals(ossVersion, that.ossVersion) &&
	               Objects.equals(cveId, that.cveId);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(ossName, ossVersion, cveId);
	    }
	}
	
	public List<OssComponents> getSecurityVulnerabilityList(Map<String, Object> securityGridMap, ProjectIdentification identification, String prjId, int securityIdx, boolean isSecurity) {
	    List<OssComponents> osvVulnerabilityList = new ArrayList<>();
	    List<Vulnerability> osvVulnList = osvDataMapper.selectOsvSecurityListForProject(identification);
	    
	    if (CollectionUtils.isEmpty(osvVulnList)) {
	        return osvVulnerabilityList; 
	    }

	    // [개선 3] 스트림 맵 생성을 제거하고, 필요한 경우에만 최소한의 공간을 가진 맵을 반복문으로 직접 생성 (메모리 절약)
	    Map<String, Vulnerability> osvVulnerabilityMap = new HashMap<>((int)(osvVulnList.size() / 0.75) + 1);
	    for (Vulnerability v : osvVulnList) {
	        if (v.getCveId() != null) {
	            osvVulnerabilityMap.put(v.getCveId(), v);
	        }
	    }

	    // 최적화된 하위 메서드 호출
	    List<Vulnerability> processedVulnerabilityList = filterByNamePriority(null, osvVulnList);
	    List<Vulnerability> osvResultList = filterByVersionPriority(processedVulnerabilityList, osvVulnerabilityMap, null, null, true);

	    // [개선 4] 스트림 내부 정렬 구조를 제거하고 단일 반복문 내 대소 비교(Map.compute) 구조로 변환
	    Map<FinalGroupKey, Vulnerability> finalMap = new LinkedHashMap<>();
	    
	    for (Vulnerability item : osvResultList) {
	        if (item == null || isEmpty(item.getCveId())) {
	            continue;
	        }

	        FinalGroupKey key = new FinalGroupKey(item.getOssName(), item.getOssVersion(), item.getCveId());

	        finalMap.compute(key, (k, existing) -> {
	            if (existing == null) {
	            	return item;
	            }

	            // 기존 로직의 정렬(sorted) 기준을 1:1 비교 연산으로 완벽히 대체
	            boolean validA = CommonFunction.isBigDecimal(existing.getCvssScore());
	            boolean validB = CommonFunction.isBigDecimal(item.getCvssScore());

	            if (validA && validB) {
	                // 두 점수 모두 유효할 경우: 내림차순(높은 점수가 우선)
	                BigDecimal scoreA = new BigDecimal(existing.getCvssScore());
	                BigDecimal scoreB = new BigDecimal(item.getCvssScore());
	                int scoreCompare = scoreB.compareTo(scoreA); // B가 크면 양수 -> item 우선
	                
	                if (scoreCompare != 0) {
	                    return scoreCompare > 0 ? item : existing;
	                }
	            } else if (validA) {
	                return existing; // 기존 데이터만 유효하면 기존 데이터 유지
	            } else if (validB) {
	                return item; // 새로운 데이터만 유효하면 새로운 데이터 선택
	            }

	            // 둘 다 유효하지 않거나 점수가 같다면: priority 오름차순(낮은 숫자가 우선)
	            return existing.getPriority() <= item.getPriority() ? existing : item;
	        });
	    }

	    List<Vulnerability> finalFilteredVulnList = new ArrayList<>(finalMap.values());
	    // [개선 1] 루프 진입 전, 단 한 번만 맵의 상태를 체크하여 CPU 오버헤드 제거
	    boolean hasGridMap = MapUtils.isNotEmpty(securityGridMap);

	    // [개선 2] 문자열 결합 속도 향상을 위해 StringBuilder 인스턴스 하나를 재사용
	    StringBuilder gridIdBuilder = new StringBuilder(64);
	    String gridIdPrefix = "jqg_sec_" + prjId + "_";

	    for (Vulnerability osvVulnInfo : finalFilteredVulnList) {
	        boolean activateFlag = isEmpty(osvVulnInfo.getOssVersion());
	        
	        // !isSecurity 일 때는 activateFlag가 true(버전 없음)이면 건너뜀
	        if (!isSecurity && activateFlag) {
	            continue;
	        }

	        // [개선 3] StringBuilder를 활용한 그리드 ID 결합 효율화 (힙 오버헤드 대폭 감소)
	        gridIdBuilder.setLength(0);
	        gridIdBuilder.append(gridIdPrefix).append(securityIdx++);

	        // 공통 객체 생성 및 기본 필드 매핑 (isSecurity 여부와 관계없이 중복 분리)
	        OssComponents ossComponents = new OssComponents();
	        ossComponents.setGridId(gridIdBuilder.toString());
	        ossComponents.setOssName(osvVulnInfo.getOssName());
	        ossComponents.setOssVersion(osvVulnInfo.getOssVersion());
	        ossComponents.setCvssScore(osvVulnInfo.getCvssScore());
	        ossComponents.setCveId(osvVulnInfo.getCveId());
	        ossComponents.setPublDate(osvVulnInfo.getPublDate());
	        ossComponents.setModiDate(osvVulnInfo.getModiDate());
	        ossComponents.setVulnSummary(osvVulnInfo.getVulnSummary());
	        ossComponents.setActivateFlag(CoConstDef.FLAG_NO);
	        ossComponents.setGroupKeyId(osvVulnInfo.getGroupKeyId());

	        // [개선 4] generateKey가 일으키는 문자열 폭탄을 제어하기 위해 맵 조회 키 생성 규칙 최적화
	        String securityGridMapKey = (!activateFlag) 
	            ? generateKey(osvVulnInfo.getOssName(), osvVulnInfo.getOssVersion(), osvVulnInfo.getCveId(), osvVulnInfo.getCvssScore())
	            : generateKey(osvVulnInfo.getOssName(), osvVulnInfo.getOssVersion(), osvVulnInfo.getCvssScore(), null);

	        // 해상도(Resolution) 판단 공통 비즈니스 로직
	        if (!activateFlag) {
	            boolean hasPatch = !isEmpty(osvVulnInfo.getPatchLink());
	            boolean isValidScore = CommonFunction.isBigDecimal(ossComponents.getCvssScore());
	            
	            if (isSecurity && hasPatch) {
	                ossComponents.setOfficialPatchLink(osvVulnInfo.getPatchLink());
	            } else if (isSecurity) {
	                ossComponents.setOfficialPatchLink("N/A");
	            }
	            
	            if (isValidScore) {
	                ossComponents.setVulnerabilityResolution(hasPatch ? "Unresolved" : "Deferred (Not Available)");
	            } else {
	                ossComponents.setVulnerabilityResolution("");
	            }
	            
	            if (isSecurity) {
	                ossComponents.setSecurityPatchLink("N/A");
	            }
	        } else {
	            ossComponents.setVulnerabilityResolution("");
	        }

	        // 바깥으로 뺀 플래그를 활용하여 맵 매핑 조건 분기 성능 개선
	        if (hasGridMap) {
	            OssComponents bean = (OssComponents) securityGridMap.get(securityGridMapKey);
	            if (bean != null) {
	                if (isSecurity) {
	                    ossComponents.setSecurityComments(bean.getSecurityComments());
	                }
	                if (!activateFlag) {
	                    ossComponents.setVulnerabilityResolution(bean.getVulnerabilityResolution());
	                    if (isSecurity) {
	                        if (!isEmpty(bean.getSecurityPatchLink()) || ("Fixed".equals(ossComponents.getVulnerabilityResolution()) && isEmpty(bean.getSecurityPatchLink()))) {
	                            ossComponents.setSecurityPatchLink(bean.getSecurityPatchLink());
	                        }
	                    }
	                }
	            }
	        } else {
	            if (!isEmpty(osvVulnInfo.getVulnerabilityResolution())) {
	                ossComponents.setVulnerabilityResolution(osvVulnInfo.getVulnerabilityResolution());
	            }
	        }

	        // 보안 전용 부가 정보 세팅 (isSecurity 전용 로직)
	        if (isSecurity) {
	            ossComponents.setVulnerabilityLink("https://osv.dev/vulnerability/" + osvVulnInfo.getCveId());
	            
	            if (!isEmpty(osvVulnInfo.getAffectedVersion())) {
	                // 상단 전역 static final로 컴파일해둔 COMPONENT_PATTERN 재사용
	                String verStartEndRange = convertOsvToSimpleFormat(osvVulnInfo.getAffectedVersion(), COMPONENT_PATTERN);
	                if (!isEmpty(verStartEndRange)) {
	                    ossComponents.setVerStartEndRange(verStartEndRange);
	                }
	            }
	            if (!isEmpty(osvVulnInfo.getAliasId())) {
	                ossComponents.setAliasIds(osvVulnInfo.getAliasId());
	            }
	        }

	        osvVulnerabilityList.add(ossComponents);
	    }

	    return osvVulnerabilityList;
	}
	
//	public List<OssComponents> getSecurityVulnerabilityList(Map<String, Object> securityGridMap, ProjectIdentification identification, String prjId, int securityIdx, boolean isSecurity) {
//		List<OssComponents> osvVulnerabilityList = new ArrayList<>();
//		List<Vulnerability> osvVulnList = osvDataMapper.selectOsvSecurityListForProject(identification);
//		if (CollectionUtils.isNotEmpty(osvVulnList)) {
//			Map<String, Vulnerability> osvVulnerabilityMap = osvVulnList.stream().collect(Collectors.toMap(Vulnerability::getCveId, vulnerability -> vulnerability, (existing, replacement) -> replacement));
//			List<Vulnerability> processedVulnerabilityList = filterByNamePriority(null, osvVulnList);
//			List<Vulnerability> osvResultList = filterByVersionPriority(processedVulnerabilityList, osvVulnerabilityMap, null, null, true);
//
//			Map<String, Vulnerability> finalMap = osvResultList.stream()
//															.filter(v -> v != null && !isEmpty(v.getCveId()))
//														    .collect(Collectors.groupingBy(
//														        item -> String.format("%s_%s_%s", 
//														            !isEmpty(item.getOssName()) ? item.getOssName().toUpperCase().trim() : "", 
//														            !isEmpty(item.getOssVersion()) ? item.getOssVersion().toUpperCase().trim() : "", 
//														            item.getCveId().toUpperCase().trim()
//														        ),
//														        LinkedHashMap::new,
//														        Collectors.collectingAndThen(
//														            Collectors.toList(),
//														            itemList -> {
//														                // If there is only 1 item in the group, return it as-is
//														                if (itemList.size() == 1) {
//														                    return itemList.get(0);
//														                }
//														                
//														                // Sort the entire group:
//														                // Valid CVSS scores come first, sorted in descending order (highest score first)
//														                // If scores are invalid or equal, sort by priority in ascending order
//														                return itemList.stream()
//														                    .sorted((a, b) -> {
//														                    	boolean validA = CommonFunction.isBigDecimal(a.getCvssScore());
//														                        boolean validB = CommonFunction.isBigDecimal(b.getCvssScore());
//														                        
//														                        // Both have valid scores: compare scores descending (Max first)
//														                        if (validA && validB) {
//														                            int scoreCompare = new java.math.BigDecimal(b.getCvssScore()).compareTo(new java.math.BigDecimal(a.getCvssScore()));
//														                            if (scoreCompare != 0) {
//														                                return scoreCompare;
//														                            }
//														                        }
//														                        
//														                        // Only A has a valid score -> A comes first (-1)
//														                        if (validA && !validB) {
//														                            return -1;
//														                        }
//														                        // Only B has a valid score -> B comes first (1)
//														                        if (!validA && validB) {
//														                            return 1;
//														                        }
//														                        
//														                        // Neither has a valid score (or both invalid): compare by priority
//														                        return Integer.compare(a.getPriority(), b.getPriority());
//														                    })
//														                    .findFirst()
//														                    .orElse(itemList.get(0));
//														            }
//														        )
//														    ));
//			List<Vulnerability> finalFilteredVulnList = new ArrayList<>(finalMap.values());
//			
//			OssComponents ossComponents = null;
//			Pattern pattern = Pattern.compile("([\\[\\(])([^,\\])]+),\\s*([^,\\])]+)([\\]\\)])");
//			
//			// Populate OssComponents objects using the finalized list and add them to the
//			// result list
//			if (isSecurity) {
//				for (Vulnerability osvVulnInfo : finalFilteredVulnList) {
//					boolean activateFlag = isEmpty(osvVulnInfo.getOssVersion());
//					String securityGridMapKey = "";
//					if (!activateFlag) {
//						securityGridMapKey = generateKey(osvVulnInfo.getOssName(), osvVulnInfo.getOssVersion(), osvVulnInfo.getCveId(), osvVulnInfo.getCvssScore());
//					} else {
//						securityGridMapKey = generateKey(osvVulnInfo.getOssName(), osvVulnInfo.getOssVersion(), osvVulnInfo.getCvssScore(), null);
//					}
//
//					ossComponents = new OssComponents();
//					ossComponents.setGridId("jqg_sec_" + prjId + "_" + String.valueOf(securityIdx++));
//					ossComponents.setOssName(osvVulnInfo.getOssName());
//					ossComponents.setOssVersion(osvVulnInfo.getOssVersion());
//					ossComponents.setCvssScore(osvVulnInfo.getCvssScore());
//					ossComponents.setCveId(osvVulnInfo.getCveId());
//					ossComponents.setPublDate(osvVulnInfo.getPublDate());
//					ossComponents.setModiDate(osvVulnInfo.getModiDate());
//					ossComponents.setVulnSummary(osvVulnInfo.getVulnSummary());
//					ossComponents.setActivateFlag(CoConstDef.FLAG_NO);
//					ossComponents.setVulnerabilityLink("https://osv.dev/vulnerability/" + osvVulnInfo.getCveId());
//					ossComponents.setGroupKeyId(osvVulnInfo.getGroupKeyId());
//
//					if (!isEmpty(osvVulnInfo.getAffectedVersion())) {
//						String verStartEndRange = convertOsvToSimpleFormat(osvVulnInfo.getAffectedVersion(), pattern);
//						if (!isEmpty(verStartEndRange)) {
//							ossComponents.setVerStartEndRange(verStartEndRange);
//						}
//					}
//
//					if (!activateFlag) {
//						if (!isEmpty(osvVulnInfo.getPatchLink())) {
//							ossComponents.setOfficialPatchLink(osvVulnInfo.getPatchLink());
//							if (CommonFunction.isBigDecimal(ossComponents.getCvssScore())) {
//								ossComponents.setVulnerabilityResolution("Unresolved");
//							} else {
//								ossComponents.setVulnerabilityResolution("");
//							}
//						} else {
//							ossComponents.setOfficialPatchLink("N/A");
//							if (CommonFunction.isBigDecimal(ossComponents.getCvssScore())) {
//								ossComponents.setVulnerabilityResolution("Deferred (Not Available)");
//							} else {
//								ossComponents.setVulnerabilityResolution("");
//							}
//						}
//						ossComponents.setSecurityPatchLink("N/A");
//					} else {
//						ossComponents.setVulnerabilityResolution("");
//					}
//
//					if (MapUtils.isNotEmpty(securityGridMap)) {
//						OssComponents bean = (OssComponents) securityGridMap.get(securityGridMapKey);
//						if (bean != null) {
//							ossComponents.setSecurityComments(bean.getSecurityComments());
//							if (!activateFlag) {
//								ossComponents.setVulnerabilityResolution(bean.getVulnerabilityResolution());
//								if (!isEmpty(bean.getSecurityPatchLink()) || (ossComponents.getVulnerabilityResolution().equals("Fixed") && isEmpty(bean.getSecurityPatchLink()))) {
//									ossComponents.setSecurityPatchLink(bean.getSecurityPatchLink());
//								}
//							}
//						}
//					} else {
//						if (!isEmpty(osvVulnInfo.getVulnerabilityResolution())) {
//							ossComponents.setVulnerabilityResolution(osvVulnInfo.getVulnerabilityResolution());
//						}
//					}
//
//					if (!isEmpty(osvVulnInfo.getAliasId())) {
//						ossComponents.setAliasIds(osvVulnInfo.getAliasId());
//					}
//
//					osvVulnerabilityList.add(ossComponents);
//				}
//			} else {
//				for (Vulnerability osvVulnInfo : finalFilteredVulnList) {
//					boolean activateFlag = isEmpty(osvVulnInfo.getOssVersion());
//					if (activateFlag) {
//						continue;
//					}
//					
//					String securityGridMapKey = generateKey(osvVulnInfo.getOssName(), osvVulnInfo.getOssVersion(), osvVulnInfo.getCveId(), osvVulnInfo.getCvssScore());
//					
//					ossComponents = new OssComponents();
//					ossComponents.setGridId("jqg_sec_" + prjId + "_" + String.valueOf(securityIdx++));
//					ossComponents.setOssName(osvVulnInfo.getOssName());
//					ossComponents.setOssVersion(osvVulnInfo.getOssVersion());
//					ossComponents.setCvssScore(osvVulnInfo.getCvssScore());
//					ossComponents.setCveId(osvVulnInfo.getCveId());
//					ossComponents.setPublDate(osvVulnInfo.getPublDate());
//					ossComponents.setModiDate(osvVulnInfo.getModiDate());
//					ossComponents.setVulnSummary(osvVulnInfo.getVulnSummary());
//					ossComponents.setActivateFlag(CoConstDef.FLAG_NO);
//					ossComponents.setGroupKeyId(osvVulnInfo.getGroupKeyId());
//					
//					if (!activateFlag) {
//						if (!isEmpty(osvVulnInfo.getPatchLink())) {
//							if (CommonFunction.isBigDecimal(ossComponents.getCvssScore())) {
//								ossComponents.setVulnerabilityResolution("Unresolved");
//							} else {
//								ossComponents.setVulnerabilityResolution("");
//							}
//						} else {
//							if (CommonFunction.isBigDecimal(ossComponents.getCvssScore())) {
//								ossComponents.setVulnerabilityResolution("Deferred (Not Available)");
//							} else {
//								ossComponents.setVulnerabilityResolution("");
//							}
//						}
//					} else {
//						ossComponents.setVulnerabilityResolution("");
//					}
//
//					if (MapUtils.isNotEmpty(securityGridMap)) {
//						OssComponents bean = (OssComponents) securityGridMap.get(securityGridMapKey);
//						if (bean != null) {
//							if (!activateFlag) {
//								ossComponents.setVulnerabilityResolution(bean.getVulnerabilityResolution());
//							}
//						}
//					} else {
//						if (!isEmpty(osvVulnInfo.getVulnerabilityResolution())) {
//							ossComponents.setVulnerabilityResolution(osvVulnInfo.getVulnerabilityResolution());
//						}
//					}
//					osvVulnerabilityList.add(ossComponents);
//				}
//			}
//		}
//		return osvVulnerabilityList;
//	}

	private String generateKey(String ossName, String ossVersion, String cveId, String cvssScore) {
		if (!isEmpty(cvssScore)) {
			return String.format("%s_%s_%s_%s", String.valueOf(ossName), String.valueOf(ossVersion), String.valueOf(cveId), String.valueOf(cvssScore));
		} else {
			return String.format("%s_%s_%s", String.valueOf(ossName), String.valueOf(ossVersion), String.valueOf(cveId));
		}
	}

	private String convertOsvToSimpleFormat(String inputRanges, Pattern pattern) {
		Matcher matcher = pattern.matcher(inputRanges);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String startBracket = matcher.group(1);
			String startVal = matcher.group(2).trim();
			String endVal = matcher.group(3).trim();
			String endBracket = matcher.group(4);

			if ("[".equals(startBracket)) {
				result.append("#").append("From (including) : ").append(startVal).append("|");
			}

			if (")".equals(endBracket)) {
				result.append("Up to (excluding) : ").append(endVal).append("|");
			}
		}

		return result.toString();
	}

	public List<Vulnerability> fetchOsvVulnerabilityData(OssMaster ossMaster, List<Vulnerability> combinedList) {
		if (CollectionUtils.isEmpty(combinedList)) {
			combinedList = new ArrayList<>();
		}
		
		List<Vulnerability> fetchOsvVulnerabilityList = new ArrayList<>();
		Map<String, Object> paramMap = prepareParamMap(ossMaster);
		List<Vulnerability> osvVulnerabilityList = findByNamePriority(ossMaster, paramMap);
		Map<String, Vulnerability> osvVulnerabilityMap = osvVulnerabilityList.stream().collect(Collectors.toMap(Vulnerability::getCveId, vulnerability -> vulnerability, (existing, replacement) -> replacement));
		List<Vulnerability> processedVulnerabilityList = filterByNamePriority(ossMaster, osvVulnerabilityList);
		List<Vulnerability> osvResultList = filterByVersionPriority(processedVulnerabilityList, osvVulnerabilityMap, ossMaster.getOssVersion(), ossMaster.getOssVersionAliases(), false);
		boolean emptyVersion = isEmpty(ossMaster.getOssVersion()) ? true : false;

		if (CollectionUtils.isNotEmpty(osvResultList)) {
			for (Vulnerability osv : osvResultList) {
				Vulnerability match = null;
				for (Vulnerability existing : combinedList) {
					if (isIdMatched(existing.getId(), osv.getId())) {
						match = existing;
						break;
					}
				}

				if (match != null) {
					match.setSource(match.getSource() + "," + osv.getSource());
					String mergedId = mergeAndFormatIds(match.getId(), osv.getId(), osv.getAliasId());
					match.setId(mergedId);
					if (mergedId.contains("(")) {
						int openIdx = mergedId.indexOf('(');
						int closeIdx = mergedId.lastIndexOf(')');

						if (openIdx != -1 && closeIdx != -1 && closeIdx > openIdx) {
							String mainId = mergedId.substring(0, openIdx).trim();
							String aliasValue = mergedId.substring(openIdx + 1, closeIdx).trim();

							match.setId(mainId);
							match.setAliasId(aliasValue);
						}
					} else {
						match.setAliasId(null);
					}
					if (emptyVersion && CoConstDef.FLAG_NO.equals(match.getSearchVersionP3Yn())) {
						continue;
					}
					fetchOsvVulnerabilityList.add(match);
				} else {
					String rawId = osv.getId();
					if (rawId.contains("(")) {
						int openIdx = rawId.indexOf('(');
						int closeIdx = rawId.lastIndexOf(')');

						if (openIdx != -1 && closeIdx != -1 && closeIdx > openIdx) {
							String mainId = rawId.substring(0, openIdx).trim();
							String aliasValue = rawId.substring(openIdx + 1, closeIdx).trim();

							osv.setId(mainId);
							osv.setAliasId(aliasValue);
						}
					}
					if (emptyVersion && CoConstDef.FLAG_NO.equals(osv.getSearchVersionP3Yn())) {
						continue;
					}
					fetchOsvVulnerabilityList.add(osv);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(fetchOsvVulnerabilityList)) {
			Map<String, Vulnerability> existingMap = combinedList.stream()
														.filter(v -> v != null && !isEmpty(v.getCveId()))
														.collect(Collectors.toMap(
																item -> String.format("%s_%s_%s",
																		!isEmpty(item.getOssName()) ? item.getOssName().toUpperCase().trim() : "",
																		!isEmpty(item.getOssVersion()) ? item.getOssVersion().toUpperCase().trim() : "",
																		item.getCveId().toUpperCase().trim()),
																item -> item, (existing, replacement) -> existing
														));

			// Iterate through fetchOsvVulnerabilityList and process accordingly
			for (var osvItem : fetchOsvVulnerabilityList) {
				if (osvItem == null || isEmpty(osvItem.getCveId())) {
					continue;
				}

				// Generate a unique key for the OSV item
				String key = String.format("%s_%s_%s",
						!isEmpty(osvItem.getOssName()) ? osvItem.getOssName().toUpperCase().trim() : "",
						!isEmpty(osvItem.getOssVersion()) ? osvItem.getOssVersion().toUpperCase().trim() : "",
						osvItem.getCveId().toUpperCase().trim());

				// Check if the same key exists in combinedList (via the map)
				if (existingMap.containsKey(key)) {
					// If it matches, set the aliasId into the existing object in combinedList
					existingMap.get(key).setAliasId(osvItem.getAliasId());
				} else {
					// If it does not exist, add the OSV item to combinedList and register it in the
					combinedList.add(osvItem);
					existingMap.put(key, osvItem);
				}
			}
		}
		
		if (CollectionUtils.isNotEmpty(combinedList)) {
			combinedList = combinedList.stream()
							    .filter(v -> v != null && !isEmpty(v.getCveId()))
							    .collect(Collectors.groupingBy(
							        item -> String.format("%s_%s_%s", 
							            !isEmpty(item.getProduct()) ? item.getProduct().toUpperCase().trim() : "", 
							            !isEmpty(item.getVersion()) ? item.getVersion().toUpperCase().trim() : "", 
							            item.getCveId().toUpperCase().trim()
							        ),
							        LinkedHashMap::new,
							        Collectors.collectingAndThen(
							            Collectors.toList(),
							            itemList -> {
							                // If there is only 1 item in the group, return it as-is
							                if (itemList.size() == 1) {
							                    return itemList.get(0);
							                }
							                
							                // Sort the entire group:
							                // Valid CVSS scores come first, sorted in descending order (highest score first)
							                // If scores are invalid or equal, sort by priority in ascending order
							                return itemList.stream()
							                    .sorted((a, b) -> {
							                    	boolean validA = CommonFunction.isBigDecimal(a.getCvssScore());
							                        boolean validB = CommonFunction.isBigDecimal(b.getCvssScore());
							                        
							                        // Both have valid scores: compare scores descending (Max first)
							                        if (validA && validB) {
							                            int scoreCompare = new java.math.BigDecimal(b.getCvssScore()).compareTo(new java.math.BigDecimal(a.getCvssScore()));
							                            if (scoreCompare != 0) {
							                                return scoreCompare;
							                            }
							                        }
							                        
							                        // Only A has a valid score -> A comes first (-1)
							                        if (validA && !validB) {
							                            return -1;
							                        }
							                        // Only B has a valid score -> B comes first (1)
							                        if (!validA && validB) {
							                            return 1;
							                        }
							                        
							                        // Neither has a valid score (or both invalid): compare by priority
							                        return Integer.compare(a.getPriority(), b.getPriority());
							                    })
							                    .findFirst()
							                    .orElse(itemList.get(0));
							            }
							        )
							    ))
							    .values()
							    .stream()
							    .collect(Collectors.toList());
		}
		
		return combinedList;
	}

	private boolean isIdMatched(String id1, String id2) {
		if (id1 == null || id2 == null) {
			return false;
		}
		if (id1.equals(id2)) {
			return true;
		}
		return id2.contains(id1) || id1.contains(id2);
	}

	private String mergeAndFormatIds(String id1, String id2, String aliasId) {
		if (isEmpty(id1) && isEmpty(id2) && isEmpty(aliasId)) {
			return id1 != null ? id1 : (id2 != null ? id2 : "");
		}

		Set<String> allTokens = new LinkedHashSet<>();

		extractTokens(id1, allTokens);
		extractTokens(id2, allTokens);

		if (aliasId != null && !aliasId.trim().isEmpty()) {
			for (String alias : aliasId.split(",")) {
				String trimmedAlias = alias.trim();
				if (!trimmedAlias.isEmpty()) {
					allTokens.add(trimmedAlias);
				}
			}
		}

		if (allTokens.isEmpty()) {
			return "";
		}

		List<String> sortedTokens = new ArrayList<>(allTokens);
		sortedTokens.sort((a, b) -> {
			int r1 = getPriorityScore(a);
			int r2 = getPriorityScore(b);
			if (r1 != r2) {
				return Integer.compare(r1, r2);
			}
			return a.compareTo(b);
		});

		String mainId = sortedTokens.get(0);
		if (sortedTokens.size() > 1) {
			String subIds = String.join(", ", sortedTokens.subList(1, sortedTokens.size()));
			return mainId + " (" + subIds + ")";
		}
		return mainId;
	}

	private int getPriorityScore(String id) {
		if (id == null) {
			return 3;
		}
		String upperId = id.toUpperCase();
		if (upperId.startsWith("CVE-")) {
			return 1;
		} else if (upperId.startsWith("GHSA-")) {
			return 2;
		}
		return 3;
	}

	private List<Vulnerability> filterByNamePriority(OssMaster ossMaster, List<Vulnerability> osvVulnerabilityList) {
		if (CollectionUtils.isEmpty(osvVulnerabilityList)) {
			return Collections.emptyList();
		}

		// Initial duplicate removal from the raw OSV list
		
		// 메모리 이슈 대응
		// Stream 연산과 무분별한 힙 객체 생성을 제거하고 단일 루프로 초기 중복 제거
//		Map<String, Vulnerability> uniqueMap = osvVulnerabilityList.stream()
//				.collect(Collectors.groupingBy(
//						item -> String.format("%s_%s_%s", !isEmpty(item.getOssName()) ? item.getOssName() : "", !isEmpty(item.getOssVersion()) ? item.getOssVersion() : "", item.getCveId()),
//						LinkedHashMap::new, Collectors.collectingAndThen(Collectors.toList(), list -> {
//							if (list.size() == 1) {
//								return list.get(0);
//							}
//
//							return list.stream().sorted((a, b) -> {
//								boolean validA = CommonFunction.isBigDecimal(a.getCvssScore());
//								boolean validB = CommonFunction.isBigDecimal(b.getCvssScore());
//
//								if (validA && !validB) {
//									return -1;
//								}
//								if (!validA && validB) {
//									return 1;
//								}
//
//								return Integer.compare(a.getPriority(), b.getPriority());
//							}).findFirst().orElse(list.get(0));
//						})));
	    Map<VulnGroupKey, Vulnerability> uniqueMap = new LinkedHashMap<>();
	    for (Vulnerability item : osvVulnerabilityList) {
	        VulnGroupKey key = new VulnGroupKey(
	            !isEmpty(item.getOssName()) ? item.getOssName() : "",
	            !isEmpty(item.getOssVersion()) ? item.getOssVersion() : "",
	            item.getCveId()
	        );
	        
	        uniqueMap.compute(key, (k, existing) -> {
	            if (existing == null) return item;
	            
	            // 우선순위 비교 (스트림 내부 정렬을 단일 비교 연산으로 변경하여 메모리 절약)
	            boolean validA = CommonFunction.isBigDecimal(existing.getCvssScore());
	            boolean validB = CommonFunction.isBigDecimal(item.getCvssScore());
	            if (validA && !validB) return existing;
	            if (!validA && validB) return item;
	            
	            return existing.getPriority() <= item.getPriority() ? existing : item;
	        });
	    }

//		List<Vulnerability> rawFilteredList = new ArrayList<>(uniqueMap.values());
//		// Temporary list to store items that pass version validation
//		Map<String, Vulnerability> mergedOsvMap = new LinkedHashMap<>();
//		mergedOsvMap = CommonFunction.filterAndMergeOsvVulnerabilities(ossMaster, rawFilteredList);
//		List<Vulnerability> finalFilteredVulnList = new ArrayList<>(mergedOsvMap.values());
//
//		Map<String, Vulnerability> mergedMap = new LinkedHashMap<>();
//		for (Vulnerability v : finalFilteredVulnList) {
//			Set<String> currentTokens = new LinkedHashSet<>();
//			if (v.getId() != null) {
//				extractTokens(v.getId(), currentTokens);
//			}
//			if (v.getAliasId() != null) {
//				for (String alias : v.getAliasId().split(",")) {
//					if (!alias.trim().isEmpty()) {
//						currentTokens.add(alias.trim());
//					}
//				}
//			}
//
//			if (currentTokens.isEmpty()) {
//				continue;
//			}
//
//			// Sort by priority (CVE -> GHSA -> Others)
//			List<String> sortedTokens = new ArrayList<>(currentTokens);
//			sortedTokens.sort((a, b) -> {
//				int r1 = getPriorityScore(a);
//				int r2 = getPriorityScore(b);
//				if (r1 != r2) {
//					return Integer.compare(r1, r2);
//				}
//				return a.compareTo(b);
//			});
//
//			String bestId = sortedTokens.get(0);
//
//			// Merge items within OSV if their tokens intersect
//			boolean merged = false;
//			for (Map.Entry<String, Vulnerability> entry : mergedMap.entrySet()) {
//				Vulnerability existing = entry.getValue();
//				Set<String> existingTokens = new LinkedHashSet<>();
//				extractTokens(existing.getId(), existingTokens);
//				if (existing.getAliasId() != null) {
//					for (String a : existing.getAliasId().split(",")) {
//						if (!a.trim().isEmpty()) {
//							existingTokens.add(a.trim());
//						}
//					}
//				}
//
//				boolean hasIntersection = currentTokens.stream().anyMatch(existingTokens::contains);
//				if (hasIntersection) {
//					String combinedFormattedId = mergeAndFormatIds(existing.getId(), v.getId(), v.getAliasId());
//					existing.setId(combinedFormattedId);
//
//					if (v.getSource() != null && !existing.getSource().contains(v.getSource())) {
//						existing.setSource(existing.getSource() + "," + v.getSource());
//					}
//
//					merged = true;
//					break;
//				}
//			}
//
//			if (!merged) {
//				v.setId(mergeAndFormatIds(v.getId(), null, v.getAliasId()));
//				mergedMap.put(bestId, v);
//			}
//		}
//
//		return new ArrayList<>(mergedMap.values());
	    
	    // 매모리 이슈 대응
	    // 중간 변환용 ArrayList 생성을 최소화한 코드로 변경
	    Map<String, Vulnerability> mergedOsvMap = CommonFunction.filterAndMergeOsvVulnerabilities(ossMaster, new ArrayList<>(uniqueMap.values()));
	    
	    // 매모리 이슈 대응
	    // 이중 루프 대체를 위한 역색인(Inverted Index) 맵
	    List<Vulnerability> resultList = new ArrayList<>();
	    Map<String, Vulnerability> tokenToVulnMap = new HashMap<>(); // 토큰 -> 취약점 객체 매핑 매개체

	    for (Vulnerability v : mergedOsvMap.values()) {
	        Set<String> currentTokens = new LinkedHashSet<>();
	        if (v.getId() != null) {
	            extractTokens(v.getId(), currentTokens);
	        }
	        if (v.getAliasId() != null) {
	            for (String alias : v.getAliasId().split(",")) {
	                String trimmed = alias.trim();
	                if (!trimmed.isEmpty()) {
	                    currentTokens.add(trimmed);
	                }
	            }
	        }

	        if (currentTokens.isEmpty()) {
	            continue;
	        }

	        // 역색인 맵을 활용하여 기존 등록된 취약점 중 교집합이 있는지 O(1) 단위로 확인
	        Vulnerability targetExisting = null;
	        for (String token : currentTokens) {
	            if (tokenToVulnMap.containsKey(token)) {
	                targetExisting = tokenToVulnMap.get(token);
	                break; // 토큰이 하나라도 겹치면 해당 객체를 병합 대상으로 선정
	            }
	        }

	        if (targetExisting != null) {
	            // [병합] 기존에 존재하던 객체에 식별자 및 소스 누적 수정
	            String combinedFormattedId = mergeAndFormatIds(targetExisting.getId(), v.getId(), v.getAliasId());
	            targetExisting.setId(combinedFormattedId);
	            
	            if (v.getSource() != null && !targetExisting.getSource().contains(v.getSource())) {
	                targetExisting.setSource(targetExisting.getSource() + "," + v.getSource());
	            }
	            
	            // 새로 파싱된 토큰들도 기존 객체를 가리키도록 역색인 관계 누적 업데이트
	            for (String token : currentTokens) {
	                tokenToVulnMap.putIfAbsent(token, targetExisting);
	            }
	        } else {
	            // [신규 등록] 중복 토큰이 없다면 최종 리스트에 추가 후 역색인 등록
	            v.setId(mergeAndFormatIds(v.getId(), null, v.getAliasId()));
	            resultList.add(v);
	            
	            for (String token : currentTokens) {
	                tokenToVulnMap.put(token, v);
	            }
	        }
	    }

	    return resultList;
	}
	
	// 매모리 이슈 대응
	private void extractTokens(String rawId, Set<String> tokenSet) {
	    if (rawId == null) {
	        return;
	    }

	    int openIdx = rawId.indexOf('(');
	    int closeIdx = rawId.lastIndexOf(')');

	    if (openIdx != -1 && closeIdx != -1 && closeIdx > openIdx) {
	        // 1. 괄호 앞 기본 ID 추출 (trim 대상 공백 확인 후 substring 최소화)
	        String main = rawId.substring(0, openIdx).trim();
	        if (!main.isEmpty()) {
	            tokenSet.add(main);
	        }

	        // 2. 괄호 내부 문자열 파싱 (split 대체하여 배열 객체 생성 방지)
	        int start = openIdx + 1;
	        while (start < closeIdx) {
	            int nextComma = rawId.indexOf(',', start);
	            int end = (nextComma == -1 || nextComma > closeIdx) ? closeIdx : nextComma;
	            
	            // 공백을 수동으로 건너뛰어 trim() 호출 및 임시 객체 생성 최소화
	            while (start < end && rawId.charAt(start) <= ' ') {
	                start++;
	            }
	            int actualEnd = end;
	            while (actualEnd > start && rawId.charAt(actualEnd - 1) <= ' ') {
	                actualEnd--;
	            }
	            
	            if (actualEnd > start) {
	                tokenSet.add(rawId.substring(start, actualEnd));
	            }
	            
	            if (nextComma == -1) {
	                break;
	            }
	            start = nextComma + 1;
	        }
	    } else {
	        String trimmed = rawId.trim();
	        if (!trimmed.isEmpty()) {
	            tokenSet.add(trimmed);
	        }
	    }
	}


//	private void extractTokens(String rawId, Set<String> tokenSet) {
//		if (rawId == null) {
//			return;
//		}
//
//		int openIdx = rawId.indexOf('(');
//		int closeIdx = rawId.lastIndexOf(')');
//
//		if (openIdx != -1 && closeIdx != -1 && closeIdx > openIdx) {
//			String main = rawId.substring(0, openIdx).trim();
//			if (!main.isEmpty()) {
//				tokenSet.add(main);
//			}
//
//			String inside = rawId.substring(openIdx + 1, closeIdx);
//			for (String sub : inside.split(",")) {
//				String trimmed = sub.trim();
//				if (!trimmed.isEmpty()) {
//					tokenSet.add(trimmed);
//				}
//			}
//		} else {
//			String trimmed = rawId.trim();
//			if (!trimmed.isEmpty()) {
//				tokenSet.add(trimmed);
//			}
//		}
//	}

	private Map<String, Object> prepareParamMap(OssMaster ossMaster) {
		Map<String, Object> param = new HashMap<>();
		String[] purls = ossMaster.getPurls();
		param.put("ossName", ossMaster.getOssName());

		if (!isEmpty(ossMaster.getOssVersion())) {
			param.put("ossVersion", ossMaster.getOssVersion());
		}
		if (ossMaster.getOssNicknames() != null && ossMaster.getOssNicknames().length > 0) {
			param.put("ossNicknames", ossMaster.getOssNicknames());
		}
		if (purls != null && purls.length > 0 && purls[0] != null) {
			param.put("purls", purls);
		}
		if (ossMaster.getOssVersionAliases() != null) {
			param.put("ossVersionAliases", ossMaster.getOssVersionAliases());
		}

		return param;
	}

	private List<Vulnerability> findByNamePriority(OssMaster ossMaster, Map<String, Object> paramMap) {
		List<Vulnerability> result = new ArrayList<>();
		if (paramMap.containsKey("ossName")) {
			List<Vulnerability> priority1List = osvDataMapper.selectOsvVulnerabilityListByUniqueNick(paramMap);
			if (CollectionUtils.isNotEmpty(priority1List)) {
				result.addAll(priority1List);
			}
		}

		if (paramMap.containsKey("purls")) {
			List<Vulnerability> priority2List = osvDataMapper.selectOsvVulnerabilityListByPurl(paramMap);
			if (CollectionUtils.isNotEmpty(priority2List)) {
				result.addAll(priority2List);
			}
		}

		if (paramMap.containsKey("ossName")) {
			List<Vulnerability> priority3List = osvDataMapper.selectOsvVulnerabilityListByPackageName(paramMap);
			if (CollectionUtils.isNotEmpty(priority3List)) {
				result.addAll(priority3List);
			}
		}

		if (CollectionUtils.isNotEmpty(result)) {
			String ossName = ossMaster.getOssName();
			String ossVersion = avoidNull(ossMaster.getOssVersion(), "");
			result.forEach(v -> {
				v.setOssName(ossName);
				v.setProduct(ossName);
				v.setOssVersion(ossVersion);
				v.setVersion(ossVersion);
				v.setCvssScore(v.getSeverity());
			});
		}
		return result;
	}
	
	// 매모리 이슈 대응
	/**
	 * 중복되던 필드 복사 및 스코어 세팅 로직을 하나로 묶어 캡슐화
	 */
	private void processVulnerabilityData(Vulnerability osvVulnerability, Map<String, Vulnerability> osvVulnerabilityMap) {
	    Vulnerability bean = osvVulnerabilityMap.get(osvVulnerability.getCveId());
	    if (bean != null) {
	        copyVulnerabilityFields(bean, osvVulnerability);
	    }
	    osvVulnerability.setCvssScore(osvVulnerability.getSeverity());
	}
	private List<Vulnerability> filterByVersionPriority(List<Vulnerability> osvVulnerabilityList, Map<String, Vulnerability> osvVulnerabilityMap, String targetVersion, String[] aliases, boolean isSecurity) {
	    if (CollectionUtils.isEmpty(osvVulnerabilityList)) {
	        return Collections.emptyList();
	    }

	    List<Vulnerability> resultList = new ArrayList<>();
	    String currentTargetVersion;
	    Set<String> seenVulnerabilities = new HashSet<>();
	    
	    for (Vulnerability osvVulnerability : osvVulnerabilityList) {
	        String uniqueKey = osvVulnerability.getOssName() + "|" + osvVulnerability.getOssVersion() + "|" + osvVulnerability.getCveId();
	        if (seenVulnerabilities.contains(uniqueKey)) {
	            continue;
	        }
	        
	        currentTargetVersion = isSecurity ? osvVulnerability.getOssVersion() : targetVersion;
	        
	        // 1순위 검증: Exact Match
	        if (!isEmpty(osvVulnerability.getSearchVersionP1())) {
	        	if (isExactVersionMatch(currentTargetVersion, aliases, osvVulnerability.getSearchVersionP1())) {
		        	seenVulnerabilities.add(uniqueKey);
	                processVulnerabilityData(osvVulnerability, osvVulnerabilityMap);
	                resultList.add(osvVulnerability);
	                continue;
	            }
	        }

	        // 2순위 검증: Range Match
	        if (!isEmpty(osvVulnerability.getSearchVersionP2())) {
	        	boolean matched = isVersionInRange(currentTargetVersion, osvVulnerability.getSearchVersionP2(), osvVulnerability.getAffectedVersion());
	            if (!matched && aliases != null) {
	                for (String alias : aliases) {
	                    if (isVersionInRange(alias, osvVulnerability.getSearchVersionP2(), osvVulnerability.getAffectedVersion())) {
	                        matched = true;
	                        break;
	                    }
	                }
	            }

	            if (matched) {
	            	seenVulnerabilities.add(uniqueKey);
	                processVulnerabilityData(osvVulnerability, osvVulnerabilityMap);
	                resultList.add(osvVulnerability);
	                continue;
	            }
	        }

	        // 3순위 검증: Empty Target Version
	        if (isEmpty(currentTargetVersion) && CoConstDef.FLAG_YES.equals(osvVulnerability.getSearchVersionP3Yn())) {
	        	seenVulnerabilities.add(uniqueKey);
	            processVulnerabilityData(osvVulnerability, osvVulnerabilityMap);
	            resultList.add(osvVulnerability);
	        }
	    }
	    
	    return resultList;
	}
//	private List<Vulnerability> filterByVersionPriority(List<Vulnerability> osvVulnerabilityList, Map<String, Vulnerability> osvVulnerabilityMap, String targetVersion, String[] aliases, boolean isSecurity) {
//		List<Vulnerability> versionP1 = new ArrayList<>();
//		List<Vulnerability> versionP2 = new ArrayList<>();
//		List<Vulnerability> versionP3 = new ArrayList<>();
//
//		for (Vulnerability osvVulnerability : osvVulnerabilityList) {
//			if (isSecurity) {
//				targetVersion = osvVulnerability.getOssVersion();
//			}
//			
//			Vulnerability bean = osvVulnerabilityMap.get(osvVulnerability.getCveId());
//			if (bean != null) {
//				copyVulnerabilityFields(bean, osvVulnerability);
//			}
//			
//			osvVulnerability.setCvssScore(osvVulnerability.getSeverity());
//
//			if (CoConstDef.FLAG_YES.equals(osvVulnerability.getSearchVersionP1Yn())) {
//				if (isExactVersionMatch(targetVersion, aliases, osvVulnerability.getSearchVersionP1())) {
//					versionP1.add(osvVulnerability);
//					continue;
//				}
//			}
//
//			if (CoConstDef.FLAG_YES.equals(osvVulnerability.getSearchVersionP2Yn())) {
//				boolean matched = isVersionInRange(targetVersion, osvVulnerability.getSearchVersionP2());
//				if (!matched && aliases != null) {
//					for (String alias : aliases) {
//						if (isVersionInRange(alias, osvVulnerability.getSearchVersionP2())) {
//							matched = true;
//							break;
//						}
//					}
//				}
//
//				if (matched) {
//					versionP2.add(osvVulnerability);
//					continue;
//				}
//			}
//
//			if (isEmpty(targetVersion) && CoConstDef.FLAG_YES.equals(osvVulnerability.getSearchVersionP3Yn())) {
//				versionP3.add(osvVulnerability);
//			}
//		}
//
//		if (!versionP1.isEmpty()) {
//			return versionP1;
//		}
//
//		if (!versionP2.isEmpty()) {
//			return versionP2;
//		}
//
//		if (!versionP3.isEmpty()) {
//			return versionP3;
//		}
//
//		return Collections.emptyList();
//	}

	private boolean isExactVersionMatch(String targetVersion, String[] aliases, String csvVersions) {
		Set<String> versionSet = new HashSet<>(Arrays.asList(csvVersions.split(",")));
		if (versionSet.contains(targetVersion)) {
			return true;
		}
		if (aliases != null) {
			for (String alias : aliases) {
				if (versionSet.contains(alias)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isVersionInRange(String targetVersion, String rangeRaw, String affectedVersion) {
		if (rangeRaw == null || rangeRaw.isEmpty() || "-".equals(rangeRaw)) {
			return false;
		}

	    String[] orRanges = rangeRaw.split("\\|");
	    String[] affectedVersions = !isEmpty(affectedVersion) ? affectedVersion.split("\\s*,\\s*(?=\\[|\\()") : new String[0];

	    for (int i = 0; i < orRanges.length; i++) {
	        String[] parts = orRanges[i].split("~");
	        if (parts.length < 2) {
	        	continue;
	        }

	        String start = parts[0].trim();
	        String end = parts[1].trim();

	        int startCompare = compareVersion(targetVersion, start);
	        int endCompare = compareVersion(targetVersion, end);

	        boolean endExclusive = i < affectedVersions.length && affectedVersions[i].trim().endsWith(")");
	        boolean endMatched = endExclusive ? endCompare < 0 : endCompare <= 0;

	        if (startCompare >= 0 && endMatched) {
	            return true;
	        }
	    }

	    return false;
	}

	// TODO 메모리 이슈 가능성 검토 필요
	private int compareVersion(String v1, String v2) {
		if ("0".equals(v1) || "0".equals(v2)) {
			if ("0".equals(v1) && "0".equals(v2)) {
				return 0;
			}
			return "0".equals(v1) ? -1 : 1;
		}

		String cleanV1 = REVISION_PATTERN.matcher(v1).replaceAll("");
		String cleanV2 = REVISION_PATTERN.matcher(v2).replaceAll("");

		String[] vals1 = cleanV1.split("\\.");
		String[] vals2 = cleanV2.split("\\.");
		int i = 0;

		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}

		if (i < vals1.length && i < vals2.length) {
			try {
				int num1 = Integer.parseInt(vals1[i].replaceAll("[^0-9]", ""));
				int num2 = Integer.parseInt(vals2[i].replaceAll("[^0-9]", ""));
				return Integer.compare(num1, num2);
			} catch (NumberFormatException e) {
				return vals1[i].compareTo(vals2[i]);
			}
		}
		return Integer.compare(vals1.length, vals2.length);
	}

	private void copyVulnerabilityFields(Vulnerability source, Vulnerability target) {
		target.setSource(source.getSource());
		target.setComponent(source.getComponent());
		target.setAffectedVersion(source.getAffectedVersion());
		target.setCvssScore(isEmpty(source.getCvssScore()) ? source.getSeverity() : source.getCvssScore());
		target.setSeverity(isEmpty(source.getSeverity()) ? source.getCvssScore() : source.getSeverity());
		target.setModiDate(source.getModiDate());
		target.setPublDate(source.getPublDate());
		target.setSummary(source.getSummary());
		target.setWithdrawnYn(source.getWithdrawnYn());
		target.setSearchNameP1(source.getSearchNameP1());
		target.setSearchNameP2(source.getSearchNameP2());
		target.setSearchNameP3(source.getSearchNameP3());
		target.setSearchVersionP1Yn(source.getSearchVersionP1Yn());
		target.setSearchVersionP1(source.getSearchVersionP1());
		target.setSearchVersionP2Yn(source.getSearchVersionP2Yn());
		target.setSearchVersionP2(source.getSearchVersionP2());
		target.setSearchVersionP3Yn(source.getSearchVersionP3Yn());
		target.setPriority(source.getPriority());
	}
}
