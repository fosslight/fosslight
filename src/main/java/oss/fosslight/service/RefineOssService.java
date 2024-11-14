package oss.fosslight.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURL.StandardTypes;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.repository.RefineOssMapper;
import oss.fosslight.util.StringUtil;

@Service
@Slf4j
public class RefineOssService {
	
	@Autowired private SqlSessionFactory sqlSessionFactory;
	
	private static final String FIELD_DOWNLOAD_LOCATION = "DOWNLOAD_LOCATION";
	private static final String FIELD_PURL = "PURL";
	private static final String FIELD_OSS_COMMON_ID = "OSS_COMMON_ID";
	private static final String FIELD_OSS_NAME = "OSS_NAME";
	
	private static final String RESULT_KEY_TOTALCNT = "reFineTotalCnt";
	private static final String RESULT_KEY_ITEMS = "reFineItems";
	
	private static final String LOG_FORMAT_INPROGRESS = "IN PROGRESS {}/{}";
	private static final String LOG_FORMAT_METHOD_START = "Start refine OSS : {}";
	private static final String LOG_FORMAT_METHOD_END = "End refine OSS : {}. reFineTotalCnt:{}";
	
	private static final int PROC_CHUNK_SIZE = 2000;
	
	@Transactional
	public Map<String, Object> refineDownloadLocation(String ossName, String refineType, boolean doUpdateFlag) {
		
		final Map<String, Object> resultMap = new HashMap<>();
		try(SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
			
			switch (refineType.toUpperCase()) {
			case "1.REMOVE DUPLICATED DOWNLOAD LOCATION":
				// 하나의 OSS 내 중복 Download location 제거
				resultMap.put("REMOVE-DUPLICATED-DOWNLOAD-LOCATION", removeDuplicatedUrl(sqlSession, doUpdateFlag, ossName));
				break;
			case "3.REMOVE DUPLICATED PURL":
				// 하나의 OSS 내 중복 PURL 제거
				resultMap.put("REMOVE-DUPLICATED-PURL", removeDuplicatedPurl(sqlSession, doUpdateFlag, ossName));
				break;
			case "4.REORDER GITHUB PRIORITY":
				// github.com이 포함된 경우 우선순위 최우선으로 변경
				resultMap.put("REORDER-GITHUB-PRIORITY", reorderGithubPriority(sqlSession, doUpdateFlag, ossName));
				break;
			case "2.PUT PURL":
				// purl 설정
				resultMap.put("PUT-PURL", trySetPurl(sqlSession, doUpdateFlag, ossName));
				break;
			case "CHECK ON ERROR FOR PURL GENERATION":
				// purl 설정
				doUpdateFlag = false;
				resultMap.put("CHECK-ON-ERROR-FOR-PURL-GENERATION", preChecFailedMakePurl(sqlSession, ossName));
				break;
			case "5.REFINE ALL":
				resultMap.put("REMOVE-DUPLICATED-DOWNLOAD-LOCATION", removeDuplicatedUrl(sqlSession, doUpdateFlag, ossName));
				resultMap.put("PUT-PURL", trySetPurl(sqlSession, doUpdateFlag, ossName));
				resultMap.put("REMOVE-DUPLICATED-PURL", removeDuplicatedPurl(sqlSession, doUpdateFlag, ossName));
				resultMap.put("REORDER-GITHUB-PRIORITY", reorderGithubPriority(sqlSession, doUpdateFlag, ossName));
				break;
			default:
				break;
			}

			sqlSession.flushStatements();
			if(doUpdateFlag) {
				// 이조건은 동작하지 않음, commit이 호출되지 않아도 Exception이 발생하지 않으면 auto commit됨 주의
				sqlSession.commit();
			}
		}
		return resultMap;
	}

	/**
	 * Purl 생성 로직 오류 확인 및 download location url을 잘못 입력한 경우 등, purl 생성시 exception이 발생하는 download url list를 반환한다. 
	 * @param sqlSession
	 * @param schOssName
	 * @return
	 */
	private Object preChecFailedMakePurl(SqlSession sqlSession, String schOssName) {
		log.info(LOG_FORMAT_METHOD_START, "preChecFailedMakePurl");
		final Map<String, Object> resultMap = new HashMap<>();
		final Map<String, List<String>> reFineItems = new HashMap<>();
		
		final RefineOssMapper refineOssMapper = sqlSession.getMapper(RefineOssMapper.class);
		int itemTotalCnt = refineOssMapper.getRefineOssTotalCnt(schOssName, "unsetPurl");
		List<String> refinedItemList;
		List<Map<String, String>> ossDownloadLocationList;
		String downloadLocation;
		int reFineTotalCnt = 0;
		if (itemTotalCnt > 0) {
			if(itemTotalCnt < PROC_CHUNK_SIZE) {
				itemTotalCnt = PROC_CHUNK_SIZE;
			}
			
			for(int limitIndex = 0; limitIndex < itemTotalCnt/PROC_CHUNK_SIZE; limitIndex++) {
				final List<Map<String, Object>> ossCommonList = refineOssMapper.selectRefineOssCommonList(schOssName, "unsetPurl", limitIndex*PROC_CHUNK_SIZE, PROC_CHUNK_SIZE);
				String ossCommonId;
				String ossName;
				for(Map<String, Object> ossCommonInfo : ossCommonList) {
					refinedItemList = new ArrayList<>();
					ossCommonId = Integer.toString((int)ossCommonInfo.get(FIELD_OSS_COMMON_ID));
					ossName = (String) ossCommonInfo.get(FIELD_OSS_NAME);
					ossDownloadLocationList = refineOssMapper.selectOssDownloadLocationList(ossCommonId);

					if(!CollectionUtils.isEmpty(ossDownloadLocationList)) {
						for(Map<String, String> n : ossDownloadLocationList) {
							if(StringUtil.isEmpty(n.get(FIELD_PURL))) {
								downloadLocation = n.get(FIELD_DOWNLOAD_LOCATION);
								try {
									generatePurlByDownloadLocation(downloadLocation);
								} catch (Exception e) {
									log.error("failed to generate purl download location : {}, {}", downloadLocation, e.getMessage(), e);
									refinedItemList.add(MessageFormat.format("{0}:{1}", downloadLocation, e.getMessage()));
								}
								
							}
						}

						if(!CollectionUtils.isEmpty(refinedItemList)) {
							reFineTotalCnt++;
							reFineItems.put(ossName, refinedItemList);							
						}						
					}
				}

				sqlSession.flushStatements();
				log.info(LOG_FORMAT_INPROGRESS, (limitIndex*PROC_CHUNK_SIZE) + PROC_CHUNK_SIZE, itemTotalCnt);
			}
		}

		resultMap.put(RESULT_KEY_TOTALCNT, reFineTotalCnt);
		resultMap.put(RESULT_KEY_ITEMS, reFineItems);
		log.info(LOG_FORMAT_METHOD_END, "preChecFailedMakePurl", reFineTotalCnt);
		return resultMap;
	}

	/**
	 * PURL 설정
	 * @param sqlSession
	 * @param doUpdateFlag 
	 * @param schOssName
	 * @return
	 */
	private Map<String, Object> trySetPurl(SqlSession sqlSession, boolean doUpdateFlag, String schOssName) {
		log.info(LOG_FORMAT_METHOD_START, "trySetPurl");
		final Map<String, Object> resultMap = new HashMap<>();
		final Map<String, List<String>> reFineItems = new HashMap<>();
		
		final RefineOssMapper refineOssMapper = sqlSession.getMapper(RefineOssMapper.class);
		int itemTotalCnt = refineOssMapper.getRefineOssTotalCnt(schOssName, "unsetPurl");
		List<String> refinedItemList;
		List<Map<String, String>> ossDownloadLocationList;
		String downloadLocation;
		String purl;
		int reFineTotalCnt = 0;
		if (itemTotalCnt > 0) {
			if(itemTotalCnt < PROC_CHUNK_SIZE) {
				itemTotalCnt = PROC_CHUNK_SIZE;
			}
			
			for(int limitIndex = 0; limitIndex < itemTotalCnt/PROC_CHUNK_SIZE; limitIndex++) {
				final List<Map<String, Object>> ossCommonList = refineOssMapper.selectRefineOssCommonList(schOssName, "unsetPurl", limitIndex*PROC_CHUNK_SIZE, PROC_CHUNK_SIZE);
				String ossCommonId;
				String ossName;
				for(Map<String, Object> ossCommonInfo : ossCommonList) {
					refinedItemList = new ArrayList<>();
					ossCommonId = Integer.toString((int)ossCommonInfo.get(FIELD_OSS_COMMON_ID));
					ossName = (String) ossCommonInfo.get(FIELD_OSS_NAME);
					ossDownloadLocationList = refineOssMapper.selectOssDownloadLocationList(ossCommonId);

					if(!CollectionUtils.isEmpty(ossDownloadLocationList)) {
						for(Map<String, String> n : ossDownloadLocationList) {
							if(StringUtil.isEmpty(n.get(FIELD_PURL))) {
								downloadLocation = n.get(FIELD_DOWNLOAD_LOCATION);
								try {
									purl = generatePurlByDownloadLocation(downloadLocation);
									if(!StringUtil.isEmpty(purl)) {
										n.put(FIELD_PURL, purl);
										refinedItemList.add(MessageFormat.format("{0}:{1}", downloadLocation, purl));
									}
								} catch (Exception e) {
									log.error("failed to generate purl download location : {}, {}", downloadLocation, e.getMessage());
								}
							}
						}

						if(!CollectionUtils.isEmpty(refinedItemList)) {
							if(doUpdateFlag) {
								refineOssMapper.deleteOssDownloadLocation(ossCommonId);
								refineOssMapper.insertOssDownloadLocation(ossCommonId, ossDownloadLocationList);
							}
							reFineTotalCnt++;
							reFineItems.put(ossName, refinedItemList);							
						}						
					}
				}

				sqlSession.flushStatements();
				log.info(LOG_FORMAT_INPROGRESS, (limitIndex*PROC_CHUNK_SIZE) + PROC_CHUNK_SIZE, itemTotalCnt);
			}
		}

		resultMap.put(RESULT_KEY_TOTALCNT, reFineTotalCnt);
		resultMap.put(RESULT_KEY_ITEMS, reFineItems);
		log.info(LOG_FORMAT_METHOD_END, "trySetPurl", reFineTotalCnt);
		return resultMap;
	}

	/**
	 * gitHub url 이 포함된 경우 정려룬서 최우선으로 변경
	 * @param sqlSession
	 * @param doUpdateFlag 
	 * @param schOssName
	 * @return
	 */
	private Map<String, Object> reorderGithubPriority(SqlSession sqlSession, boolean doUpdateFlag, String schOssName) {
		log.info(LOG_FORMAT_METHOD_START, "reorderGithubPriority");
		final Map<String, Object> resultMap = new HashMap<>();
		final Map<String, List<String>> reFineItems = new HashMap<>();
		
		final RefineOssMapper refineOssMapper = sqlSession.getMapper(RefineOssMapper.class);
		int itemTotalCnt = refineOssMapper.getRefineOssTotalCnt(schOssName, "reorderGithubUrl");
		List<String> refinedItemList;
		List<Map<String, String>> ossDownloadLocationList;
		int reFineTotalCnt = 0;
		if (itemTotalCnt > 0) {
			if(itemTotalCnt < PROC_CHUNK_SIZE) {
				itemTotalCnt = PROC_CHUNK_SIZE;
			}
			
			for(int limitIndex = 0; limitIndex < itemTotalCnt/PROC_CHUNK_SIZE; limitIndex++) {
				final List<Map<String, Object>> ossCommonList = refineOssMapper.selectRefineOssCommonList(schOssName, "reorderGithubUrl", limitIndex*PROC_CHUNK_SIZE, PROC_CHUNK_SIZE);
				String ossCommonId;
				String ossName;
				for(Map<String, Object> ossCommonInfo : ossCommonList) {
					refinedItemList = new ArrayList<>();
					ossCommonId = Integer.toString((int)ossCommonInfo.get(FIELD_OSS_COMMON_ID));
					ossName = (String) ossCommonInfo.get(FIELD_OSS_NAME);
					ossDownloadLocationList = refineOssMapper.selectOssDownloadLocationList(ossCommonId);

					// find github.com url item
					int githubUrlItemIndex = 0;
					Map<String, String> githubUrlItem = null;
					if(!CollectionUtils.isEmpty(ossDownloadLocationList)) {
						for(Map<String, String> n : ossDownloadLocationList) {
							if(n.get(FIELD_DOWNLOAD_LOCATION).contains("github.com")) {
								githubUrlItem = n;
								break;
							}
							githubUrlItemIndex++;
						}
						
						// remove and insert first github.com url item
						if(githubUrlItem != null) {
							ossDownloadLocationList.remove(githubUrlItemIndex);
							ossDownloadLocationList.add(0, githubUrlItem);
							refinedItemList.add(githubUrlItem.get(FIELD_DOWNLOAD_LOCATION));
							
							if(doUpdateFlag) {
								refineOssMapper.deleteOssDownloadLocation(ossCommonId);
								refineOssMapper.insertOssDownloadLocation(ossCommonId, ossDownloadLocationList);
							}
							reFineTotalCnt++;
							reFineItems.put(ossName, refinedItemList);
						}
						
					}
				}

				sqlSession.flushStatements();
				log.info(LOG_FORMAT_INPROGRESS, (limitIndex*PROC_CHUNK_SIZE) + PROC_CHUNK_SIZE, itemTotalCnt);
			}
		}

		resultMap.put(RESULT_KEY_TOTALCNT, reFineTotalCnt);
		resultMap.put(RESULT_KEY_ITEMS, reFineItems);
		log.info(LOG_FORMAT_METHOD_END, "reorderGithubPriority", reFineTotalCnt);
		return resultMap;
	}

	/**
	 * 하나의 OSS에 대해서 중복된 PURL이 존재하는 경우 가장 짧은 download location을 제외하고 PURL이 동일한 row를 삭제처리 한다.
	 * @param doUpdateFlag 
	 */
	private Map<String, Object> removeDuplicatedPurl(SqlSession sqlSession, boolean doUpdateFlag, String schOssName) {
		log.info(LOG_FORMAT_METHOD_START, "removeDuplicatedPurl");
		final Map<String, Object> resultMap = new HashMap<>();
		final Map<String, List<String>> reFineItems = new HashMap<>();
		
		final RefineOssMapper refineOssMapper = sqlSession.getMapper(RefineOssMapper.class);
		
		int reFineTotalCnt = 0;
		int itemTotalCnt = refineOssMapper.getRefineOssTotalCnt(schOssName, null);
		List<String> refinedItemList;
		List<Map<String, String>> ossDownloadLocationList;
		if (itemTotalCnt > 0) {
			if(itemTotalCnt < PROC_CHUNK_SIZE) {
				itemTotalCnt = PROC_CHUNK_SIZE;
			}
			for(int limitIndex = 0; limitIndex < itemTotalCnt/PROC_CHUNK_SIZE; limitIndex++) {
				final List<Map<String, Object>> ossCommonList = refineOssMapper.selectRefineOssCommonList(schOssName, null, limitIndex*PROC_CHUNK_SIZE, PROC_CHUNK_SIZE);
				String ossCommonId;
				String ossName;
				Map<String, List<Map<String, String>>> duplPurlGroupMap;
				for(Map<String, Object> ossCommonInfo : ossCommonList) {
					refinedItemList = new ArrayList<>();
					ossCommonId = Integer.toString((int)ossCommonInfo.get(FIELD_OSS_COMMON_ID));
					ossName = (String) ossCommonInfo.get(FIELD_OSS_NAME);
					ossDownloadLocationList = refineOssMapper.selectOssDownloadLocationList(ossCommonId);
					
					// 만약 oss_common table에만 존재하고, download_location table에 등록되어 있지 않은 경우 (Data Migration 누락 Case)
					if(CollectionUtils.isEmpty(ossDownloadLocationList)) {
//						if(!StringUtil.isEmpty(ossCommonInfo.get("downloadLocation"))) {
//							Map<String, String> itemMap = new HashMap<>();
//							itemMap.put("downloadLocation", ossCommonInfo.get("downloadLocation"));
//							checkedOssDownloadLocationList.add(itemMap);
//						}
					} else {
						duplPurlGroupMap = new HashMap<>();
						List<Map<String, String>> duplcatedList;
						for(Map<String, String> n : ossDownloadLocationList) {
							
							// Purl이 동일한 download location List
							if(!duplPurlGroupMap.containsKey(n.get(FIELD_PURL))) {
								duplcatedList = findSamePurlData(n.get(FIELD_PURL), ossDownloadLocationList);
								// 자신을 포함하여 동이한 purl이 2개이상인 경우만 중복으로 판단. download location 길이가 더 짧은 data는 삭제대상이됨
								if(!CollectionUtils.isEmpty(duplcatedList) && duplcatedList.size() > 1) {
									duplPurlGroupMap.put(n.get(FIELD_PURL), duplcatedList);
								}
							}
						}
						
						if(!duplPurlGroupMap.isEmpty()) {
							String key;
							List<Map<String, String>> value;
							Map<String, String> shortDownloadlocationItem;
						    for (Map.Entry<String, List<Map<String, String>>> elem : duplPurlGroupMap.entrySet()) {
						        key = elem.getKey();
						        value = elem.getValue();
						        // 동일한 purl에 대해서 download location이 가장 짧은 item을 구한다.
//						        value.forEach(v -> log.info(v.get(FIELD_DOWNLOAD_LOCATION)));
						        value.sort((o1, o2) -> (o1.get(FIELD_DOWNLOAD_LOCATION)).trim().length() - (o2.get(FIELD_DOWNLOAD_LOCATION)).trim().length());
//						        value.forEach(v -> log.info(v.get(FIELD_DOWNLOAD_LOCATION)));
						        shortDownloadlocationItem = value.get(0);
						        value.remove(0);
						        
						        int deleteIdx;
						        for(int i=0; i<value.size(); i++) {
							        deleteIdx = findSamePurlDeleteTargetIndex(key, shortDownloadlocationItem.get(FIELD_DOWNLOAD_LOCATION), ossDownloadLocationList);
							        if(deleteIdx > -1) {
							        	refinedItemList.add(MessageFormat.format("{0}:{1}", ossDownloadLocationList.get(deleteIdx).get(FIELD_DOWNLOAD_LOCATION), ossDownloadLocationList.get(deleteIdx).get(FIELD_PURL)));
							        	ossDownloadLocationList.remove(deleteIdx);
							        }							        	
						        }
						    }

						    if(doUpdateFlag) {
						    	refineOssMapper.deleteOssDownloadLocation(ossCommonId);
						    	refineOssMapper.insertOssDownloadLocation(ossCommonId, ossDownloadLocationList);
						    }
							reFineTotalCnt++;
							reFineItems.put(ossName, refinedItemList);
						}
					}
				}

				sqlSession.flushStatements();
				log.info(LOG_FORMAT_INPROGRESS, (limitIndex*PROC_CHUNK_SIZE) + PROC_CHUNK_SIZE, itemTotalCnt);
			}
		}
		
		resultMap.put(RESULT_KEY_TOTALCNT, reFineTotalCnt);
		resultMap.put(RESULT_KEY_ITEMS, reFineItems);
		log.info(LOG_FORMAT_METHOD_END, "removeDuplicatedPurl", reFineTotalCnt);
		return resultMap;
	}
	
	/**
	 * list내에서 동일한 purl이 존재하는 경우 해당 index를 반환한다.
	 * @param purl
	 * @param downloadlocation
	 * @param ossDownloadLocationList
	 * @return
	 */
	private int findSamePurlDeleteTargetIndex(String purl, String downloadlocation, List<Map<String, String>> ossDownloadLocationList) {
		int idx = -1;
		for(Map<String, String> item : ossDownloadLocationList) {
			idx++;
			if(purl.equals(item.get(FIELD_PURL)) && !downloadlocation.equals(item.get(FIELD_DOWNLOAD_LOCATION))) {
				return idx;
			}
			
		}
		return -1;
	}

	/**
	 * list내에서 동일한 PURL정보를 가지는 item list를 반환한다.
	 * @param purl
	 * @param ossDownloadLocationList
	 * @return
	 */
	private List<Map<String, String>> findSamePurlData(String purl, List<Map<String, String>> ossDownloadLocationList) {
		if(StringUtil.isEmpty(purl)) {
			return Collections.emptyList();
		}
		
		final List<Map<String, String>> resultList = new ArrayList<>();
		ossDownloadLocationList.stream().filter(n -> purl.equals(n.get(FIELD_PURL))).forEach(resultList::add);
		return resultList;
	}

	/**
	 * 각 OSS에 중복 설정된 download location을 삭제한다. (http, https 프로토콜만 다른 경우도 중복으로 판단)
	 * @param sqlSession
	 * @param doUpdateFlag 
	 * @param schOssName
	 * @return
	 */
	private Map<String, Object> removeDuplicatedUrl(SqlSession sqlSession, boolean doUpdateFlag, String schOssName) {
		log.info(LOG_FORMAT_METHOD_START, "removeDuplicatedUrl");
		final Map<String, Object> resultMap = new HashMap<>();
		final Map<String, List<String>> reFineItems = new HashMap<>();
		
		final RefineOssMapper refineOssMapper = sqlSession.getMapper(RefineOssMapper.class);
		
		int reFineTotalCnt = 0;
		int itemTotalCnt = refineOssMapper.getRefineOssTotalCnt(schOssName, null);
		List<String> refinedItemList;
		List<Map<String, String>> ossDownloadLocationList;
		List<Map<String, String>> checkedOssDownloadLocationList;
		if (itemTotalCnt > 0) {
			if(itemTotalCnt < PROC_CHUNK_SIZE) {
				itemTotalCnt = PROC_CHUNK_SIZE;
			}
			for(int limitIndex = 0; limitIndex < itemTotalCnt/PROC_CHUNK_SIZE; limitIndex++) {
				final List<Map<String, Object>> ossCommonList = refineOssMapper.selectRefineOssCommonList(schOssName, null, limitIndex*PROC_CHUNK_SIZE, PROC_CHUNK_SIZE);
				String ossCommonId;
				String ossName;
				for(Map<String, Object> ossCommonInfo : ossCommonList) {
					refinedItemList = new ArrayList<>();
					ossCommonId = Integer.toString((int)ossCommonInfo.get(FIELD_OSS_COMMON_ID));
					ossName = (String) ossCommonInfo.get(FIELD_OSS_NAME);
					ossDownloadLocationList = refineOssMapper.selectOssDownloadLocationList(ossCommonId);
					// check exist duplication download url
					checkedOssDownloadLocationList = new ArrayList<>();
					
					// 만약 oss_common table에만 존재하고, download_location table에 등록되어 있지 않은 경우 (Data Migration 누락 Case)
					if(CollectionUtils.isEmpty(ossDownloadLocationList)) {
//						if(!StringUtil.isEmpty(ossCommonInfo.get("downloadLocation"))) {
//							Map<String, String> itemMap = new HashMap<>();
//							itemMap.put("downloadLocation", ossCommonInfo.get("downloadLocation"));
//							checkedOssDownloadLocationList.add(itemMap);
//						}
					} else {
						for(Map<String, String> n : ossDownloadLocationList) {
							if(!checkDuplicationUrl(checkedOssDownloadLocationList,ossDownloadLocationList, n)) {
								checkedOssDownloadLocationList.add(n);
							} else {
								// 중복으로 판단된 경우
								refinedItemList.add(n.get(FIELD_DOWNLOAD_LOCATION));
							}
						}
					}
					
					if(checkedOssDownloadLocationList.size() != ossDownloadLocationList.size()) {
						if(doUpdateFlag) {
							refineOssMapper.deleteOssDownloadLocation(ossCommonId);
							refineOssMapper.insertOssDownloadLocation(ossCommonId, checkedOssDownloadLocationList);
						}
						reFineTotalCnt++;
						reFineItems.put(ossName, refinedItemList);
					}
				}

				sqlSession.flushStatements();
				log.info(LOG_FORMAT_INPROGRESS, (limitIndex*PROC_CHUNK_SIZE) + PROC_CHUNK_SIZE, itemTotalCnt);
			}
		}
		
		resultMap.put(RESULT_KEY_TOTALCNT, reFineTotalCnt);
		resultMap.put(RESULT_KEY_ITEMS, reFineItems);
		log.info(LOG_FORMAT_METHOD_END, "removeDuplicatedUrl", reFineTotalCnt);
		return resultMap;
	}

	/**
	 * list에 동일한 downloadlocation 존재여부 반환
	 * @param list
	 * @param orgList
	 * @param target
	 * @return
	 */
	private boolean checkDuplicationUrl(List<Map<String, String>> list, List<Map<String, String>> orgList, Map<String, String> target) {
		final String downloadLocationUrl = target.get(FIELD_DOWNLOAD_LOCATION);
		
		// http:// 프로토콜인 경우, https:// 가 중복 존재하는 경우가 있기 때문에 http를 제외하기 위해 이미 존재하는 것으로 판단한다.
		if(downloadLocationUrl.startsWith("http://")) {
			final String uri = downloadLocationUrl.substring("http://".length());
			String downloadUrl;
			for(Map<String, String> item : orgList) {
				downloadUrl = item.get(FIELD_DOWNLOAD_LOCATION);
				if(downloadUrl.startsWith("https://") && uri.equalsIgnoreCase(downloadUrl.substring("https://".length()))) {
					return true;
				}
			}
		}
		
		// 이미 추가되어 있는 경우(중복)
		return list.stream().filter(item -> downloadLocationUrl.equalsIgnoreCase(item.get(FIELD_DOWNLOAD_LOCATION))).findFirst().map(item -> true).orElse(false);
	}
	
	/**
	 * download location url에서 purl을 생성하여 반환한다.
	 * @param downloadLocation
	 * @return
	 * @throws MalformedPackageURLException
	 */
	private String generatePurlByDownloadLocation(String downloadLocation) throws MalformedPackageURLException {
		final List<String> checkPurl = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_DOWNLOADLOCAION_PURL);
		String purlString = "";
		int urlSearchSeq = -1;
		int seq = 0;

		if (!StringUtil.isEmpty(downloadLocation)) {
			String subPath = "";
			
			for (String url : checkPurl) {
				if (urlSearchSeq == -1 && downloadLocation.contains(url)) {
					urlSearchSeq = seq;
					break;
				}
				seq++;
			}
			
			downloadLocation = downloadLocation.split("://")[1];
			if (downloadLocation.startsWith("www.")) {
				downloadLocation = downloadLocation.substring(4, downloadLocation.length());
			}
			if (downloadLocation.contains(";")) {
				downloadLocation = downloadLocation.split("[;]")[0];
			}
			// delete port number
			if (downloadLocation.contains(":")) {
				int colonIdx = downloadLocation.indexOf(":");
				int slashIdx = downloadLocation.indexOf("/", colonIdx);
				if (slashIdx > -1 && slashIdx > colonIdx && downloadLocation.substring(colonIdx+1, slashIdx).chars().allMatch(Character::isDigit)) {
					downloadLocation = downloadLocation.substring(0, colonIdx) + downloadLocation.substring(slashIdx, downloadLocation.length());
				}
			}
			
			if (downloadLocation.contains(".git")) {
				if (downloadLocation.endsWith(".git")) {
					downloadLocation = downloadLocation.substring(0, downloadLocation.length()-4);
				} else {
					if (downloadLocation.contains("#")) {
						downloadLocation = downloadLocation.substring(0, downloadLocation.indexOf("#"));
						if (downloadLocation.endsWith(".git")) {
							downloadLocation = downloadLocation.substring(0, downloadLocation.length()-4);
						}
					}
				}
			}
			
			if (downloadLocation.contains("#")) {
				if (urlSearchSeq == 9) {
					String[] splitDownloadLocation = downloadLocation.split("[#]");
					subPath = splitDownloadLocation[1];
				}
				downloadLocation = downloadLocation.substring(0, downloadLocation.indexOf("#"));
			}
			
			if (downloadLocation.contains("@")) {
				if (urlSearchSeq == 9) downloadLocation = downloadLocation.substring(0, downloadLocation.indexOf("@"));
			}
			
			if (downloadLocation.endsWith("/")) downloadLocation = downloadLocation.substring(0, downloadLocation.length()-1);
			
			if (urlSearchSeq > -1) {
				Pattern p = generatePatternPurl(urlSearchSeq, downloadLocation);
				Matcher m = p.matcher(downloadLocation);
				
				while (m.find()) {
					downloadLocation = m.group(0);
				}
			}
			
			PackageURL purl = null;
			if (urlSearchSeq == -1) {
				if (downloadLocation.contains("+")) downloadLocation = downloadLocation.substring(0, downloadLocation.indexOf("+"));
				purlString = "link:" + downloadLocation;
			} else if (urlSearchSeq == 10) {
				if (downloadLocation.contains("+")) downloadLocation = downloadLocation.substring(0, downloadLocation.indexOf("+")-1);
				purlString = "link:" + downloadLocation;
			} else {
				String[] splitDownloadLocation = downloadLocation.split("/");
				boolean addFlag = false;
				String namespace = "/";
				
				switch(urlSearchSeq) {
					case 0: // github
						purl = new PackageURL(StandardTypes.GITHUB, splitDownloadLocation[1], splitDownloadLocation[2], null, null, null);
						break;
					case 1: // npm
						if (downloadLocation.contains("/package/@")) addFlag = true;
						purl = new PackageURL(StandardTypes.NPM, null, splitDownloadLocation[2], null, null, null);
						break;
					case 2: // npm
						if (downloadLocation.contains("/@")) addFlag = true;
						purl = new PackageURL(StandardTypes.NPM, null, splitDownloadLocation[1], null, null, null);
						break;
					case 3: // pypi
					case 4: // pypi
						purl = new PackageURL(StandardTypes.PYPI, null, splitDownloadLocation[2].replaceAll("_", "-"), null, null, null);
						break;
					case 5: // maven
					case 6: // maven
						purl = new PackageURL(StandardTypes.MAVEN, splitDownloadLocation[2], splitDownloadLocation[3], null, null, null);
						break;
					case 7: // cocoapod
						purl = new PackageURL("cocoapods", null, splitDownloadLocation[2], null, null, null);
						break;
					case 8: // gem
						purl = new PackageURL(StandardTypes.GEM, null, splitDownloadLocation[2], null, null, null);
						break;
					case 9: // go
						int idx = 0;
						for (String data : splitDownloadLocation) {
							if (idx > 1) {
								namespace += data + "/";
							}
							idx++;
						}
						namespace = namespace.substring(0, namespace.length()-1);
						purl = new PackageURL(StandardTypes.GOLANG, splitDownloadLocation[1]);
						
						break;
					case 11:
						purl = new PackageURL("pub", null, splitDownloadLocation[2], null, null, null);
						break;
					default:
						break;
				}
				
				if (purl != null) {
					purlString = purl.toString();
					if (urlSearchSeq == 9) {
						purlString += namespace + subPath;
					} else {
						if (addFlag) {
							if (urlSearchSeq == 1) {
								if (splitDownloadLocation.length > 3) purlString += "/" + splitDownloadLocation[3];
							} else {
								if (splitDownloadLocation.length > 2) purlString += "/" + splitDownloadLocation[2];
							}
						}
					}
				}
			}
		}
		
		return purlString;
	}
	
	/**
	 * PURL생성 가능여부 판단을 위핸 패턴 정의
	 * @param urlSearchSeq
	 * @param downloadlocationUrl
	 * @return
	 */
	private Pattern generatePatternPurl(int urlSearchSeq, String downloadlocationUrl) {
		Pattern p = null;
		switch(urlSearchSeq) {
			case 0: // github
				p = Pattern.compile("((http|https)://github.com/([^/]+)/([^/]+))");

				break;
			case 1: // npm
				if (downloadlocationUrl.contains("/package/@")) {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+)/([^/]+))");
				}else {
					p = Pattern.compile("((http|https)://npmjs.(org|com)/package/([^/]+))");
				}
			case 2: // npm
				if (downloadlocationUrl.contains("/@")) {
					p = Pattern.compile("((http|https)://registry.npmjs.(org|com)/([^/]+)/([^/]+))");
				}else {
					p = Pattern.compile("((http|https)://registry.npmjs.(org|com)/([^/]+))");
				}
				break;
			case 3: // pypi
				p = Pattern.compile("((http|https)://pypi.python.org/project/([^/]+))");
				break;
			case 4: // pypi
				p = Pattern.compile("((http|https)://pypi.org/project/([^/]+))");
				break;
			case 5: // maven
				p = Pattern.compile("((http|https)://mvnrepository.com/artifact/([^/]+)/([^/]+))");
				break;
			case 6: // maven
				p = Pattern.compile("((http|https)://repo.maven.apache.org/maven2/([^/]+)/([^/]+))");
				break;
			case 7: // cocoapod
				p = Pattern.compile("((http|https)://cocoapods.org/pods/([^/]+))");
				break;
			case 8: // gem
				p = Pattern.compile("((http|https)://rubygems.org/gems/([^/]+))");
				break;
			case 9: // go
				p = Pattern.compile("((http|https)://pkg.go.dev/([^@]+)@?v?([^/]+))");
				break;
			case 10:
				p = Pattern.compile("((http|https)://android.googlesource.com/platform/(.*))");
				break;
			case 11:
				p = Pattern.compile("((http|https)://pub.dev/packages/([^/]+))");
				break;
			default:
				p = Pattern.compile("(.*)");
				break;
		}
		return p;
	}

}
