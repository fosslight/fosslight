package oss.fosslight.service;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import oss.fosslight.common.CommonFunction;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.NvdDataMapper;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;

@Service("NvdDataService")
public class NvdDataService {
	final static Logger log = LoggerFactory.getLogger("SCHEDULER_LOG");
	
	private final String NVD_DATA_FILE_NAME_CPEMATCH = "nvdcpematch-1.0";
	private final String NVD_DATA_FILE_NAME_NVDCVE = "nvdcve-1.1-modified";
	
	private final String NVD_META_URL = "https://nvd.nist.gov/feeds/json/cpematch/1.0/";
	private final String NVD_CVE_URL = "https://nvd.nist.gov/feeds/json/cve/1.1/";
	
	@Autowired NvdDataMapper nvdDataMapper;
	@Autowired CodeMapper codeMapper;
	@Autowired Environment env;
	@Autowired SqlSessionFactory sqlSessionFactory;
	
	public String executeNvdDataSync() throws IOException {
		
		// GET Nvd Meta Data
		// 작업등록
		try {
			boolean fileCheck = nvdMetaCheckJob(NVD_DATA_FILE_NAME_CPEMATCH, "MATCH");
			if(!fileCheck) fileCheck = nvdMetaRetryCheckJob(NVD_DATA_FILE_NAME_CPEMATCH, "MATCH", fileCheck, 0);
			
			if(fileCheck) {
				nvdFeedDataDownloadJob(NVD_DATA_FILE_NAME_CPEMATCH);
				nvdMetaDataSyncJob();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "91";
		}
		
		try {
			// initialize NVD Data Feed
			// check initialize flag
			if("Y".equalsIgnoreCase(codeMapper.getCodeDtlNm("990", "100")) ) {
				codeMapper.updateCodeDtlNm("990", "100", "N");

				// delete all NVD Data and Max Score
				resetNvdFeedData();
				
				// Put NVD Data feed from CPE2002 ~ current date year
				initNvdDataFeed();
				
			}
		} catch (Exception e) {
			log.error("Failed to NVD Data initiallize", e);
		}

		try {
			boolean fileCheck = nvdMetaCheckJob(NVD_DATA_FILE_NAME_NVDCVE, "CVE");
			if(!fileCheck) fileCheck = nvdMetaRetryCheckJob(NVD_DATA_FILE_NAME_NVDCVE, "CVE", fileCheck, 0);
			
			if(fileCheck) {
				nvdFeedDataDownloadJob(NVD_DATA_FILE_NAME_NVDCVE);
				nvdCveDataSyncJob();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "92";
		}
		
		return "00";
	}
	

	@Transactional
	private String nvdCveDataSyncJob() throws JsonParseException, JsonMappingException, IOException {
		String resCd = "00";

		// 1. Wait Job 데이터 조회
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("fileType", "CVE");
		List<HashMap<String, Object>> waitList = nvdDataMapper.selectWaitJobData(param);
		
		boolean updateFlag = false;
		// 2. Json File -> DB Insert
		for(Map<String, Object> wMetaMap : waitList){

			param.put("fileNm", wMetaMap.get("fileNm"));
			param.put("modiDate", wMetaMap.get("modiDate"));
			param.put("jobStatus", "G");
			// JobStatus가 W인 대상이 작업이 들어갔다면 G로 변경을 하여 추후에 다시 loop 돌지 않도록 처리함.
			nvdDataMapper.updateJobStatus(param);
			
			String NVD_CVE_PATH = env.getProperty("root.dir");
			if(StringUtil.isEmpty(NVD_CVE_PATH)) {
				NVD_CVE_PATH = new FileSystemResource("").getFile().getAbsolutePath();
			}
			NVD_CVE_PATH = Paths.get(NVD_CVE_PATH, "nvd/cve").toString();

			updateFlag = updateNvdData(NVD_CVE_PATH, (String) wMetaMap.get("fileNm"));
			
			param.put("jobStatus", "C");
			nvdDataMapper.updateJobStatus(param);
		
		}
		
		// NVD 관련 Data 중복표시(cpe_nm의 마이너 버전이 포함됨), 및 성능 개선을 위해 별도의 테이블을 추가
		// truncate and insert 처리
		// 위험성 : truncate는 transaction 으로 관리 할 수 없다. truncate이후에 insert 실패시 data 없을 수 있음
		
		if(updateFlag){
			// temp table에 data insert 이후, real table로 copy
			nvdDataMapper.deleteNvdDataTempV3();
			
			int cnt = nvdDataMapper.getProducVerCnt();
			for(int idx = 0; idx < cnt; ) {
				List<Map<String, Object>> itemList = nvdDataMapper.getProducVerList(idx, 1000);
				List<Map<String, Object>> params = new ArrayList<>();
				for(Map<String, Object> item : itemList) {
					params.add(nvdDataMapper.getMaxScoreProductVer((String)item.get("PRODUCT"), (String)item.get("VERSION")));
				}
				nvdDataMapper.insertNvdDataListTempV3(params);
				
				idx = idx+1000;
			}

			nvdDataMapper.deleteNvdDataScoreV3();
			nvdDataMapper.insertNvdDataScoreV3();
			nvdDataMapper.deleteNvdDataTempV3();
		}
		
		int nickNameMgrCnt = nvdDataMapper.selectNickNameMgrtNvdDataScoreV3();
		if(nickNameMgrCnt > 0) {
			log.info("Nickname Migration Count : " + nickNameMgrCnt);
			// OSS_NICKNAME 기준으로 NVD_DATA_SCORE_V3에 NICKNAME을 추가함.
			nvdDataMapper.insertNickNameMgrtNvdDataScoreV3();
		}else{
			log.info("Nickname Migration Count : 0");
		}
		
		int MaxCvssScoreCnt = nvdDataMapper.selectMaxCvssScoreNvdDataScoreV3();
		if(MaxCvssScoreCnt > 0) {
			log.info("MaxCvssScore Added Count : " + MaxCvssScoreCnt);
			// NVD_DATA_SCORE_V3에서 CVSS_SCORE MAX값을 기준으로 PRODUCT에서 VERSION이 없는 DATA를 추가함.
			nvdDataMapper.insertMaxCvssScoreNvdDataScoreV3();
		}else{
			log.info("MaxCvssScore Added Count : 0");
		}
		
		int diffCvssScoreCnt = nvdDataMapper.ossNameNickNameCvssScoreDiffCnt();
		if(diffCvssScoreCnt > 0){
			log.info("ossName <-> NickName cvssScore Diff Count : " + diffCvssScoreCnt);
			// ossName과 NickName의 nvd data가 동일한 version일 경우에 cvss score만 다를때 더 높은 값으로 변경을 함. 변경시 CVE ID 도 같이 변경을 함.
			nvdDataMapper.ossNameToNickNameMgrtCvssScore();
			nvdDataMapper.nickNameToOssNameMgrtCvssScore();
		}else{
			log.info("ossName <-> NickName cvssScore Diff Count : 0");
		}
		
		return resCd;
	}
	
	
	@SuppressWarnings("unchecked")
	private boolean updateNvdData(String cpeFileRootPath, String cpeFileName) throws JsonParseException, JsonMappingException, IOException {

		boolean updateFlag = false;
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(  new File(cpeFileRootPath + File.separator + cpeFileName + ".json") , new TypeReference<Map<String, Object>>() { });
		
		if(map.containsKey("CVE_Items")) {
			List<Map<String, Object>> cveItems = (List<Map<String, Object>>) map.get("CVE_Items");
			for(Map<String, Object> cveItem : cveItems) {
				
				Map<String, Object> cveInfo = cveDatajsonReader(cveItem);
				if(cveInfo == null || cveInfo.isEmpty()) {
					continue;
				}
				
				String cveId = (String) cveInfo.get("cveId");
				Map<String, Object> comapare = nvdDataMapper.selectOneCveInfoV3(cveInfo);
				
				List<Map<String, String>> ossList = null;
				
				ossList = new ArrayList<>();
				// 전체 cpe match 정보에서 vulnerable 가 false인 경우는 제외한다.
				// 적용대상 cpe match list
				List<Map<String, Object>> cpe_match_all = (List<Map<String, Object>>) cveInfo.get("cpe_match_all");
				for (Map<String, Object> cpe_match_data : cpe_match_all) {
					// 정보에서 Version Range 조건을 고려하여 Cpe match 정보로 부터 최종적요으로 적용할 모든 대상 cpe23uri를 취득한다.
					// Version Range 조건 취득
					// 검색 조건 설정
					Map<String, String> _matchNameParams = new HashMap<>();
					_matchNameParams.put("cpe23Uri", (String) cpe_match_data.get("cpe23Uri"));
					if (cpe_match_data.containsKey("versionStartIncluding")) {
						_matchNameParams.put("versionStartIncluding",
								(String) cpe_match_data.get("versionStartIncluding"));
					}
					if (cpe_match_data.containsKey("versionEndIncluding")) {
						_matchNameParams.put("versionEndIncluding",
								(String) cpe_match_data.get("versionEndIncluding"));
					}
					if (cpe_match_data.containsKey("versionStartExcluding")) {
						_matchNameParams.put("versionStartExcluding",
								(String) cpe_match_data.get("versionStartExcluding"));
					}
					if (cpe_match_data.containsKey("versionEndExcluding")) {
						_matchNameParams.put("versionEndExcluding",
								(String) cpe_match_data.get("versionEndExcluding"));
					}

					List<String> matchNames = nvdDataMapper.selectNvdMatchList(_matchNameParams);

					// 만약 cpe match에서 cpe23uri로 조회된 결과가 없을 경우 해당 cpe23uri 만 설정한다.
					if (matchNames == null) {
						matchNames = new ArrayList<>();
					}

					if (matchNames.isEmpty()) {
						matchNames.add((String) cpe_match_data.get("cpe23Uri"));
					}

					// cpe23uri에서 product, version 정보를 추출한다.
					for (String cpe23uri : matchNames) {
						String[] cpeInfoArr = cpe23uri.split(":");

						Map<String, String> _productInfo = new HashMap<>();
						_productInfo.put("cveId", cveId);
						_productInfo.put("product", cpeInfoArr[4].replaceAll("_", " "));
						_productInfo.put("version", cpeInfoArr[5]);

						ossList.add(_productInfo);
					}
				}
				
				// 신규등록
				if(comapare == null){
					nvdDataMapper.insertCveInfoV3(cveInfo);
					
					if(!ossList.isEmpty()) {
						List<Map<String, String>> insertDataList = new ArrayList<>();
						for(Map<String, String> item : ossList){
							insertDataList.add(item);
							if( (insertDataList.size() % 1000) == 0 ) {
								nvdDataMapper.insertBulkNvdDataV3(insertDataList);
								insertDataList = new ArrayList<>();
							}
						}
						// 미등록 data가 존재하는 경우 나머지를 등록한다.
						if( !insertDataList.isEmpty() ) {
							nvdDataMapper.insertBulkNvdDataV3(insertDataList);
						}
					}
					
					updateFlag = true;
				}
				// 변경(delete > insert)
				else if(!DateUtil.equals((Date)comapare.get("modiDate"), (Date) cveInfo.get("modiDate"))
						|| !((Float)comapare.get("cvssScore")).equals(Float.valueOf((String)cveInfo.get("cvssScore"))) ){
					// 기존 데이터 삭제
					nvdDataMapper.deleteCveDataV3(cveInfo);
					nvdDataMapper.deleteNvdDataV3(cveInfo);
					// 변경 데이터 등록
					nvdDataMapper.insertCveInfoV3(cveInfo);
					if(!ossList.isEmpty()) {
						List<Map<String, String>> insertDataList = new ArrayList<>();
						for(Map<String, String> item : ossList){
							insertDataList.add(item);
							if( (insertDataList.size() % 1000) == 0 ) {
								nvdDataMapper.insertBulkNvdDataV3(insertDataList);
								insertDataList = new ArrayList<>();
							}
						}
						// 미등록 data가 존재하는 경우 나머지를 등록한다.
						if( !insertDataList.isEmpty() ) {
							nvdDataMapper.insertBulkNvdDataV3(insertDataList);
						}
					}
				} else if (DateUtil.equals((Date)comapare.get("modiDate"), (Date) cveInfo.get("modiDate"))
						&& ((Float)comapare.get("cvssScore")).equals(Float.valueOf((String)cveInfo.get("cvssScore")))) {
					// NVD_CVE_V3는 변경 대상이 아니지만 NVD_DATA_V3에 적용 될 대상이 존재 한 경우
					
					if(!ossList.isEmpty()) {
						List<Map<String, String>> insertDataList = new ArrayList<>();
						for(Map<String, String> item : ossList){
							insertDataList.add(item);
							if( (insertDataList.size() % 1000) == 0 ) {
								nvdDataMapper.insertBulkNvdDataV3(insertDataList);
								insertDataList = new ArrayList<>();
							}
						}
						// 미등록 data가 존재하는 경우 나머지를 등록한다.
						if( !insertDataList.isEmpty() ) {
							nvdDataMapper.insertBulkNvdDataV3(insertDataList);
						}
					}
				}
				updateFlag = true;
			}
		}
		
	
		return updateFlag;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> cveDatajsonReader(Map<String, Object> cveItem) throws JsonParseException, JsonMappingException, IOException {	

		String publishedDate = (String) cveItem.get("publishedDate");
		String lastModifiedDate = (String) cveItem.get("lastModifiedDate");
		
		Map<String, Object> cveInfo = (Map<String, Object>) cveItem.get("cve");
		
		Map<String, Object> cveDataInfo = (Map<String, Object>) cveInfo.get("CVE_data_meta");
		String cveId = (String) cveDataInfo.get("ID");
		// impact
		Map<String, Object> impact = (Map<String, Object>) cveItem.get("impact");
		// CVSS V3가 없는 경우 V2 Score를 사용
		if(!impact.containsKey("baseMetricV3") && !impact.containsKey("baseMetricV2")) {
			return null;
		}
		
		String baseScore = null;
		String baseMetric = "V3";
		if(impact.containsKey("baseMetricV3")) {
			Map<String, Object> baseMetricV3 = (Map<String, Object>) impact.get("baseMetricV3");
			Map<String, Object> cvssV3 = (Map<String, Object>) baseMetricV3.get("cvssV3");
			baseScore = String.valueOf(cvssV3.get("baseScore"));						
		} else {
			Map<String, Object> baseMetricV2 = (Map<String, Object>) impact.get("baseMetricV2");
			Map<String, Object> cvssV2 = (Map<String, Object>) baseMetricV2.get("cvssV2");
			baseScore = String.valueOf(cvssV2.get("baseScore"));
			baseMetric = "V2";
		}

		
		List<Map<String, String>> ossList = new ArrayList<>();
		List<Map<String, Object>> cpe_match_all = new ArrayList<>();
	
		Map<String, Object> configurationsInfo = (Map<String, Object>) cveItem.get("configurations");
		List<Map<String, Object>> configurations_nodes = (List<Map<String, Object>>) configurationsInfo.get("nodes");
		
		// 정의된 모든 cpe match 정보
		for(Map<String, Object> node_data : configurations_nodes) {
			// children cpe_match 대신 children 노드가 존재하는 경우, children 노드 하위에서 cpe_match 정보를 취득한다.
			if(node_data.containsKey("cpe_match")) {
				cpe_match_all.addAll((List<Map<String, Object>>) node_data.get("cpe_match"));
			}
			
			if(node_data.containsKey("children")) {
				List<Map<String, Object>> children = (List<Map<String, Object>>) node_data.get("children");
				// 스키마 구조상으로는 children 하위 노드에서 다시 operator AND 조건이 발생할 수 있지만, 실제 Data 존재하지 않았기 때문에
				// 재귀처리는 생략하고 1Depth 까지만 찾는다.
				for(Map<String, Object> children_data : children) {
					cpe_match_all.addAll((List<Map<String, Object>>) children_data.get("cpe_match"));
				}
			}
			
		}
		
		Map<String, Object> description = (Map<String, Object>) cveInfo.get("description");
		List<Map<String, Object>> description_datas = (List<Map<String, Object>>) description.get("description_data");
		String descriptionStr = "";
		for(Map<String, Object> description_data : description_datas) {
			if(!StringUtil.isEmpty(descriptionStr)) {
				descriptionStr += "\n";
			}
			descriptionStr += description_data.get("value");
		}
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("ossList", ossList);
		resultMap.put("cveId", cveId);
		resultMap.put("publDate", DateUtil.convertStringToTimestamp((publishedDate), "yyyy-MM-dd'T'HH:mmZ"));
		resultMap.put("modiDate", DateUtil.convertStringToTimestamp((lastModifiedDate), "yyyy-MM-dd'T'HH:mmZ"));
		resultMap.put("cvssScore", baseScore);
		resultMap.put("summary", descriptionStr);
		resultMap.put("baseMetric", baseMetric);
		resultMap.put("cpe_match_all", cpe_match_all);
	
		return resultMap;
	}

	private boolean nvdMetaCheckJob(String FILE_NM, String FILE_TYPE) throws IOException {
		HashMap<String, String> metaInfo = nvdMetaData(FILE_NM);
		if(metaInfo != null){
			// 1. 사용중인 메타 데이터 조회
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("fileType", FILE_TYPE);
			param.put("fileNm", FILE_NM);
			List<HashMap<String, Object>> useList = nvdDataMapper.selectUseMetaData(param);
			// 2. 메타 데이터를 비교한다.
			// 2.1. 신규 파일면 메타 데이터를 신규 등록한다.
			if( useList.size() == 0 || !metaInfo.get("modiDate").equals(useList.get(0).get("modiDate")) ){
				param.put("modiDate", metaInfo.get("modiDate"));
				param.put("size", Integer.parseInt(metaInfo.get("size")));
				param.put("zipSize",Integer.parseInt(metaInfo.get("zipSize")));
				param.put("gzSize", Integer.parseInt(metaInfo.get("gzSize")));
				param.put("sha256", metaInfo.get("sha256"));
				nvdDataMapper.insertNewMetaData(param);
				return true;
			}
			// 2.2. 변경된 파일이면 메타 데이터를 삭제한다.
			if( useList.size() > 0 && !metaInfo.get("modiDate").equals(useList.get(0).get("modiDate")) ){
				param.put("fileNm", FILE_NM);
				param.put("modiDate", useList.get(0).get("modiDate"));
				param.put("useYn", "N");
				nvdDataMapper.updateUseYN(param);
				nvdDataMapper.updateJobStatus(param);
				return true;
			}
		} else {
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("fileType", FILE_TYPE);
			param.put("fileNm", FILE_NM);
			nvdDataMapper.insertErrorMetaData(param);
		}
		
		return false;
	}
	
	private boolean nvdMetaRetryCheckJob(String FILE_NM, String FILE_TYPE, boolean fileCheck, int cnt) {
		int maxCnt = 3;
		
		log.debug("5초후 재시도합니다.");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		
		try {
			fileCheck = nvdMetaCheckJob(FILE_NM, FILE_TYPE);
		} catch (IOException ioe) {
			log.error(ioe.getMessage(), ioe);
		}
		
		if(!fileCheck && cnt < maxCnt) {
			fileCheck = nvdMetaRetryCheckJob(FILE_NM, FILE_TYPE, fileCheck, ++cnt);
		}
		
		return fileCheck;
	}
	
	/**
	 * Nvd feed data download job.
	 *
	 * @param FILE_NAME the file name
	 * @throws Exception the exception
	 */
	private void nvdFeedDataDownloadJob(String FILE_NAME) throws Exception  {
		String NVD_CVE_PATH = env.getProperty("root.dir");
		if(StringUtil.isEmpty(NVD_CVE_PATH)) {
			NVD_CVE_PATH = new FileSystemResource("").getFile().getAbsolutePath();
		}
		String NVD_DATA_BACKUP_PATH = NVD_CVE_PATH;
		NVD_CVE_PATH = Paths.get(NVD_CVE_PATH, "nvd/cve").toString();
		NVD_DATA_BACKUP_PATH = Paths.get(NVD_DATA_BACKUP_PATH, "nvd/backup").toString();
		try {
			FileUtil.backupRawData(NVD_CVE_PATH + File.separator + FILE_NAME + ".json.zip", NVD_DATA_BACKUP_PATH);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		
		String downloadUrl = (NVD_DATA_FILE_NAME_CPEMATCH.equals(FILE_NAME) ? NVD_META_URL : NVD_CVE_URL) + FILE_NAME + ".json.zip";
		try {
			FileUtil.downloadFile(downloadUrl, NVD_CVE_PATH);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			Thread.sleep(3000);
			log.info("Retry downloading the NVD data file. FILE_NAME : " + FILE_NAME);
			if(Paths.get(NVD_CVE_PATH, FILE_NAME + ".json.zip").toFile().exists()) {
				try {
					Paths.get(NVD_CVE_PATH, FILE_NAME + ".json.zip").toFile().delete();
				} catch (Exception e2) {}
			}
			FileUtil.downloadFile(downloadUrl, NVD_CVE_PATH);
		}
		FileUtil.decompress(NVD_CVE_PATH + File.separator + FILE_NAME + ".json.zip", NVD_CVE_PATH);		// 압축해제
	}
	
	
	@SuppressWarnings("unchecked")
	public String nvdMetaDataSyncJob() throws JsonParseException, JsonMappingException, IOException {
		String resCd = "00";
		int batchSize = 100;

		// 1. Wait Job 데이터 조회
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("fileType", "MATCH");
		List<HashMap<String, Object>> waitList = nvdDataMapper.selectWaitJobData(param);

		
		String NVD_CVE_PATH = env.getProperty("root.dir");
		if(StringUtil.isEmpty(NVD_CVE_PATH)) {
			NVD_CVE_PATH = new FileSystemResource("").getFile().getAbsolutePath();
		}
		NVD_CVE_PATH = Paths.get(NVD_CVE_PATH, "nvd/cve").toString();
		
		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {

			NvdDataMapper mapper = sqlSession.getMapper(NvdDataMapper.class);

			// 2. Json File -> DB Insert
			for (Map<String, Object> wMetaMap : waitList) {
				param.put("fileNm", wMetaMap.get("fileNm"));
				param.put("modiDate", wMetaMap.get("modiDate"));
				param.put("jobStatus", "G");
				// JobStatus가 W인 대상이 작업이 들어갔다면 G로 변경을 하여 추후에 다시 loop 돌지 않도록 처리함.
				nvdDataMapper.updateJobStatus(param);

				try {

					Map<String, Object> dataMap = new ObjectMapper().readValue(new File(
							NVD_CVE_PATH + File.separator + ((String) wMetaMap.get("fileNm")).toLowerCase() + ".json"),
							new TypeReference<Map<String, Object>>() {
							});

					if (dataMap.containsKey("matches")) {
						List<Map<String, Object>> matchItems = (List<Map<String, Object>>) dataMap.get("matches");
						int seq = 1;
						int totSize = matchItems.size();
//						List<Map<String, Object>> cpeMetaList = new ArrayList<>();
						List<Map<String, Object>> cpeNameList = new ArrayList<>();
						

						// 조건이 변경되거나, 삭제되는 경우를 고려하여 전체 데이터를 삭제하고 다시 등록한다.
						// truncate table
						mapper.truncateCpeMatchNames();
						mapper.truncateCpeMatch();
						sqlSession.flushStatements();
						
						for (Map<String, Object> matchItem : matchItems) {
							// cpe23uri (key)
							String cpe23Uri = (String) matchItem.get("cpe23Uri");
							// version range conditions
							String versionStartIncluding = null;
							String versionEndIncluding = null;
							String versionStartExcluding = null;
							String versionEndExcluding = null;

							if (matchItem.containsKey("versionStartIncluding")) {
								versionStartIncluding = (String) matchItem.get("versionStartIncluding");
							}
							if (matchItem.containsKey("versionEndIncluding")) {
								versionEndIncluding = (String) matchItem.get("versionEndIncluding");
							}
							if (matchItem.containsKey("versionStartExcluding")) {
								versionStartExcluding = (String) matchItem.get("versionStartExcluding");
							}
							if (matchItem.containsKey("versionEndExcluding")) {
								versionEndExcluding = (String) matchItem.get("versionEndExcluding");
							}

							Map<String, Object> cpeMetaMap = new HashMap<>();
							cpeMetaMap.put("cpeSeq", seq);
							cpeMetaMap.put("cpe23Uri", cpe23Uri);
							cpeMetaMap.put("versionStartIncluding", versionStartIncluding);
							cpeMetaMap.put("versionEndIncluding", versionEndIncluding);
							cpeMetaMap.put("versionStartExcluding", versionStartExcluding);
							cpeMetaMap.put("versionEndExcluding", versionEndExcluding);

//								cpeMetaList.add(cpeMetaMap);
							mapper.insertCpeMatchData(cpeMetaMap);

							// CPE Names
								if(matchItem.containsKey("cpe_name")) {
									int nameIdx = 0;
									for(Map<String, Object> cpe_name : (List<Map<String, Object>>) matchItem.get("cpe_name")) {
										cpe_name.put("cpeSeq", seq);
										cpe_name.put("idx", nameIdx);
										cpeNameList.add(cpe_name);
										if(cpeNameList.size() >= 1000) {
											mapper.insertBulkCpeMatchNameData(cpeNameList);
											cpeNameList = new ArrayList<>();
										}
										nameIdx++;
									}
								}
								
								if (!cpeNameList.isEmpty()) {
									mapper.insertBulkCpeMatchNameData(cpeNameList);
									cpeNameList = new ArrayList<>();
								}
								

							if(seq % batchSize == 0 || seq == totSize) {
								List<BatchResult> batResults = sqlSession.flushStatements();
								batResults.clear();
							}

							seq++;
						}

					}

					sqlSession.commit();

				} catch (Exception e) {
					log.error(e.getMessage());
					sqlSession.rollback();
				} finally {
					sqlSession.close();
				}

				param.put("jobStatus", "C");
				nvdDataMapper.updateJobStatus(param);
			}
		}
		
		return resCd;
	}
	
	private HashMap<String, String> nvdMetaData(String FILE_NM) throws IOException{
		HashMap<String, String> metaInfo = null;
		HttpURLConnection con = null;
		Scanner s = null;
		try {
			URL url = new URL( (NVD_DATA_FILE_NAME_CPEMATCH.equals(FILE_NM) ? NVD_META_URL : NVD_CVE_URL) +FILE_NM+".meta");
			con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("HEAD");
			if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
				s = new Scanner(url.openConnection().getInputStream());
				metaInfo = new HashMap<String, String>();
				while(s.hasNext()){
					String txt = s.next();
					String[] d = txt.split(":");
					String k = d[0].equals("lastModifiedDate") ? "modiDate" : d[0];
					String v = txt.substring(d[0].length()+1, txt.length());
					metaInfo.put(k, v);
				}
			}else{
				log.warn("connection error : " + CommonFunction.httpCodePrint(con.getResponseCode()) + " - " + con.getResponseCode());
			}
		} finally {
			if(s != null) {
				try {s.close();} catch (Exception e) {}
			}
			if(con != null) {
				try {con.disconnect();} catch (Exception e) {}
			}
		}
		
		return metaInfo;
	}
	
	@Transactional
	private void initNvdDataFeed() throws Exception {

		int nvdBeginDateYear = 2002;
		int currentDateYear = StringUtil.string2integer(DateUtil.getCurrentDateAsString("yyyy")) ;
		for(int year = nvdBeginDateYear; year <= currentDateYear; year ++) {
			nvdFeedDataDownloadJob("nvdcve-1.1-" + year);
		}
		
		String NVD_CVE_PATH = env.getProperty("root.dir");
		if(StringUtil.isEmpty(NVD_CVE_PATH)) {
			NVD_CVE_PATH = new FileSystemResource("").getFile().getAbsolutePath();
		}
		NVD_CVE_PATH = Paths.get(NVD_CVE_PATH, "nvd/cve").toString();
		for(int year = nvdBeginDateYear; year <= currentDateYear; year ++) {
			updateNvdData(NVD_CVE_PATH, "nvdcve-1.1-" + year);
		}
		
	}


	/**
	 * Reset ALL NVD Feed Data 
	 */
	private void resetNvdFeedData() {
		nvdDataMapper.resetCveDataV3();
		nvdDataMapper.resetNvdDataV3();
	}
}
