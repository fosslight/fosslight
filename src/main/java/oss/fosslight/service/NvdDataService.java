package oss.fosslight.service;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.annotation.PostConstruct;

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
	
	private final int BATCH_SIZE = 1000;
	
	private String JDBC_DRIVER;  
	private String DB_URL;
	private String USERNAME;
	private String PASSWORD;
	
	@Autowired NvdDataMapper nvdDataMapper;
	@Autowired CodeMapper codeMapper;
	@Autowired Environment env;
	@Autowired SqlSessionFactory sqlSessionFactory;
	
	@PostConstruct
	public void setResourcePathPrefix(){
		JDBC_DRIVER = env.getRequiredProperty("spring.datasource.driver-class-name");
		DB_URL = env.getRequiredProperty("spring.datasource.url");
		USERNAME = env.getRequiredProperty("spring.datasource.username");
		PASSWORD = env.getRequiredProperty("spring.datasource.password");
	}
	
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
//				resetNvdFeedData();
				
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
		log.info("Start CVE Data Sync Job");
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
			String insertQuery = "INSERT INTO NVD_DATA_TEMP_V3 (PRODUCT, VERSION, VENDOR, CVE_ID, CVSS_SCORE, VULN_SUMMARY, MODI_DATE ) VALUES (?,?,?,?,?,?,?) "
					+ "ON DUPLICATE KEY UPDATE PRODUCT = values(PRODUCT), VERSION = values(VERSION), VENDOR = values(VENDOR), CVE_ID = values(CVE_ID), CVSS_SCORE = values(CVSS_SCORE), VULN_SUMMARY = values(VULN_SUMMARY), MODI_DATE = values(MODI_DATE)";
			
			String selectQuery = "SELECT NVD.PRODUCT, NVD.VERSION, GROUP_CONCAT(DISTINCT(NVD.VENDOR)) AS VENDOR FROM (SELECT T2.* FROM (SELECT @ROWNUM:=@ROWNUM+1 AS SEQ, T1.PRODUCT FROM ("
					+ "SELECT PRODUCT FROM NVD_DATA_V3 GROUP BY PRODUCT ORDER BY PRODUCT ) T1, (SELECT @ROWNUM:=0) AS R ) T2 WHERE T2.SEQ BETWEEN ? AND ? ) NVD_PRODUCT JOIN NVD_DATA_V3 NVD ON NVD_PRODUCT.PRODUCT = NVD.PRODUCT GROUP BY NVD.PRODUCT, NVD.VERSION";
			
			String selectQuery2 = "SELECT T2.PRODUCT, T2.VERSION, T1.CVE_ID, T1.CVSS_SCORE, T1.VULN_SUMMARY, T1.MODI_DATE FROM NVD_CVE_V3 T1, NVD_DATA_V3 T2 WHERE T1.CVE_ID = T2.CVE_ID AND T2.PRODUCT = ? AND T2.VERSION = ? "
					+ "AND T1.CVSS_SCORE = (SELECT MAX(CVSS_SCORE) AS CVSS_SCORE  FROM NVD_CVE_V3 WHERE CVE_ID IN (SELECT CVE_ID FROM NVD_DATA_V3 WHERE PRODUCT = ? AND VERSION = ?)) ORDER BY CVE_ID DESC LIMIT 1";
			// temp table에 data insert 이후, real table로 copy
			nvdDataMapper.deleteNvdDataTempV3();
			
			int cnt = nvdDataMapper.getProducVerCnt();
			List<Map<String, Object>> itemList = new ArrayList<>();
			List<Map<String, Object>> params = new ArrayList<>();
			int endIdx = BATCH_SIZE;
						
			Connection conn = null;
			Connection conn1 = null;
			Connection conn2 = null;
			
			PreparedStatement getProductStmt = null;
			PreparedStatement getMaxScoreProductVerStmt = null;
			PreparedStatement insertStmt = null;
			
			try {
				Class.forName(JDBC_DRIVER);
				
				conn = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
				conn.setAutoCommit(false);
				
				conn1 = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
				conn1.setAutoCommit(false);
				
				conn2 = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
				conn2.setAutoCommit(false);
				
				getProductStmt = conn.prepareStatement(selectQuery);
				getMaxScoreProductVerStmt = conn1.prepareStatement(selectQuery2);
				insertStmt = conn2.prepareStatement(insertQuery);
				
				for(int idx = 0; idx < cnt; ) {
					if(endIdx >= cnt) endIdx = cnt;
					preparedStatementGetProductList(conn2, itemList, params, getProductStmt, getMaxScoreProductVerStmt, insertStmt, idx, endIdx);
					
					idx = idx+BATCH_SIZE;
					endIdx = idx+BATCH_SIZE;
					
					if(idx % 10000 == 0) {
						log.info("NVD_DATA_TEMP_V3 process : " + idx + " / " + cnt);
					}
				}
				log.info("NVD_DATA_TEMP_V3 process : " + cnt + " / " + cnt);
			} catch(Exception e) {
				log.error(e.getMessage(), e);
				
				try{
					if(insertStmt!=null)
						insertStmt.close();
				}catch(SQLException e1){}
				try{
					if(getMaxScoreProductVerStmt!=null)
						getMaxScoreProductVerStmt.close();
				}catch(SQLException e1){}
				try{
					if(getMaxScoreProductVerStmt!=null)
						getMaxScoreProductVerStmt.close();
				}catch(SQLException e1){}
				try{
					if(conn2!=null)
						conn2.rollback();
						conn2.close();
				}catch(SQLException e1){}
				try{
					if(conn1!=null)
						conn1.close();
				}catch(SQLException e1){}
				try{
					if(conn!=null)
						conn.close();
				}catch(SQLException e1){}
			} finally {
				try{
					if(insertStmt!=null)
						insertStmt.close();
				}catch(SQLException e1){}
				try{
					if(getMaxScoreProductVerStmt!=null)
						getMaxScoreProductVerStmt.close();
				}catch(SQLException e1){}
				try{
					if(getMaxScoreProductVerStmt!=null)
						getMaxScoreProductVerStmt.close();
				}catch(SQLException e1){}
				try{
					if(conn2!=null)
						conn2.close();
				}catch(SQLException e1){}
				try{
					if(conn1!=null)
						conn1.close();
				}catch(SQLException e1){}
				try{
					if(conn!=null)
						conn.close();
				}catch(SQLException e1){}
			}
			
			nvdDataMapper.deleteNvdDataScoreV3();
			nvdDataMapper.insertNvdDataScoreV3();
			nvdDataMapper.deleteNvdDataTempV3();

			log.info("End CVE Data Sync Job");
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
	
	private void preparedStatementGetProductList(Connection conn2, List<Map<String, Object>> itemList, List<Map<String, Object>> params, 
			PreparedStatement getProductStmt, PreparedStatement getMaxScoreProductVerStmt, PreparedStatement insertStmt, int batchIdx, int batchEndIdx) {
		ResultSet rs = null;
		Map<String, Object> itemMap = null;
		
		try {
			getProductStmt.setInt(1, batchIdx);
			getProductStmt.setInt(2, batchEndIdx);
			
			rs = getProductStmt.executeQuery();
			
			getProductStmt.clearParameters();
			
			while (rs.next()) {
				itemMap = new HashMap<>();
				itemMap.put("ossName", rs.getString(1));
				itemMap.put("ossVersion", rs.getString(2));
				itemMap.put("vendor", rs.getString(3));
				
				itemList.add(itemMap);
				
				if(itemList.size() % BATCH_SIZE == 0) {
					preparedStatementGetMaxScoreProductVer(conn2, getMaxScoreProductVerStmt, insertStmt, itemList, params);
					itemList.clear();
				}
			}
			
			if(itemList.size() > 0) {
				preparedStatementGetMaxScoreProductVer(conn2, getMaxScoreProductVerStmt, insertStmt, itemList, params);
			}
		} catch(Exception e) {
			itemList.clear();
			log.error(e.getMessage(), e);
			try{
				if(rs!=null)
					rs.close();
			}catch(SQLException e1){}
		} finally {
			itemList.clear();
			try{
				if(rs!=null)
					rs.close();
			}catch(SQLException e1){}
		}
	}
	
	private void preparedStatementGetMaxScoreProductVer(Connection conn2, PreparedStatement getMaxScoreProductVerStmt, PreparedStatement insertStmt, List<Map<String, Object>> itemList, List<Map<String, Object>> params) {
		ResultSet getMaxScoreProductVerRs = null;
		Map<String, Object> paramMap = null;
		
		try {
			String vendor = "";
			List<String> vendorList = new ArrayList<>();
			
			for(Map<String, Object> item : itemList) {
				vendor = (String) item.get("vendor");
				for(String chk : vendor.split(",")) {
					if(!StringUtil.isEmpty(chk)) {
						vendorList.add(chk);
					}
				}
				
				getMaxScoreProductVerStmt.setString(1, (String) item.get("ossName"));
				getMaxScoreProductVerStmt.setString(2, (String) item.get("ossVersion"));
				getMaxScoreProductVerStmt.setString(3, (String) item.get("ossName"));
				getMaxScoreProductVerStmt.setString(4, (String) item.get("ossVersion"));
				
				getMaxScoreProductVerRs = getMaxScoreProductVerStmt.executeQuery();
				
				getMaxScoreProductVerStmt.clearParameters();
				
				if(vendorList.size() > 0) {
					while (getMaxScoreProductVerRs.next()) {
						for(String vn : vendorList) {
							paramMap = new HashMap<>();
							paramMap.put("PRODUCT", getMaxScoreProductVerRs.getString(1));
							paramMap.put("VERSION", getMaxScoreProductVerRs.getString(2));
							paramMap.put("VENDOR", vn);
							paramMap.put("CVE_ID", getMaxScoreProductVerRs.getString(3));
							paramMap.put("CVSS_SCORE", getMaxScoreProductVerRs.getFloat(4));
							paramMap.put("VULN_SUMMARY", getMaxScoreProductVerRs.getString(5));
							paramMap.put("MODI_DATE", getMaxScoreProductVerRs.getTimestamp(6));
							
							params.add(paramMap);
							
							if(params.size() % BATCH_SIZE == 0) {
								preparedStatementInsertNvdDataListTempV3(conn2, insertStmt, params);
								params.clear();
							}
						}
					}
				} else {
					while (getMaxScoreProductVerRs.next()) {
						paramMap = new HashMap<>();
						paramMap.put("PRODUCT", getMaxScoreProductVerRs.getString(1));
						paramMap.put("VERSION", getMaxScoreProductVerRs.getString(2));
						paramMap.put("VENDOR", "");
						paramMap.put("CVE_ID", getMaxScoreProductVerRs.getString(3));
						paramMap.put("CVSS_SCORE", getMaxScoreProductVerRs.getFloat(4));
						paramMap.put("VULN_SUMMARY", getMaxScoreProductVerRs.getString(5));
						paramMap.put("MODI_DATE", getMaxScoreProductVerRs.getTimestamp(6));
						
						params.add(paramMap);
						
						if(params.size() % BATCH_SIZE == 0) {
							preparedStatementInsertNvdDataListTempV3(conn2, insertStmt, params);
							params.clear();
						}
					}
				}
				
				vendorList.clear();
			}
			
			if(params.size() > 0) {
				preparedStatementInsertNvdDataListTempV3(conn2, insertStmt, params);
				params.clear();
			}
		} catch(Exception e) {
			params.clear();
			log.error(e.getMessage(), e);
			try{
				if(getMaxScoreProductVerRs!=null)
					getMaxScoreProductVerRs.close();
			}catch(SQLException e1){}
			try{
				if(getMaxScoreProductVerStmt!=null)
					getMaxScoreProductVerStmt.close();
			}catch(SQLException e1){}
		} finally {
			params.clear();
			try{
				if(getMaxScoreProductVerRs!=null)
					getMaxScoreProductVerRs.close();
			}catch(SQLException e){}
		}
	}
	
	private void preparedStatementInsertNvdDataListTempV3(Connection conn2, PreparedStatement insertStmt, List<Map<String, Object>> params) {
		try{
			for(Map<String, Object> item : params) {
				insertStmt.setString(1, (String) item.get("PRODUCT"));
				insertStmt.setString(2, (String) item.get("VERSION"));
				insertStmt.setString(3, (String) item.get("VENDOR"));
				insertStmt.setString(4, (String) item.get("CVE_ID"));
				insertStmt.setFloat(5, (float) item.get("CVSS_SCORE"));
				insertStmt.setString(6, (String) item.get("VULN_SUMMARY"));
				insertStmt.setTimestamp(7, Timestamp.valueOf(item.get("MODI_DATE").toString()));
				insertStmt.addBatch();
				insertStmt.clearParameters();
			}

			// 커밋되지 못한 나머지 구문에 대하여 커밋
			insertStmt.executeBatch() ;
			conn2.commit();
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean updateNvdData(String cpeFileRootPath, String cpeFileName) throws JsonParseException, JsonMappingException, IOException {

		boolean updateFlag = false;
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(  new File(cpeFileRootPath + File.separator + cpeFileName + ".json") , new TypeReference<Map<String, Object>>() { });
		
		if(map.containsKey("CVE_Items")) {
			List<Map<String, Object>> cveItems = (List<Map<String, Object>>) map.get("CVE_Items");
			Map<String, Object> cveInfo = null;
			Map<String, Object> comapare = null;
			List<Map<String, String>> ossList = new ArrayList<>();
			List<Map<String, String>> insertDataList = new ArrayList<>();
			
			List<Map<String, Object>> cpe_match_all = null;
			
			for(Map<String, Object> cveItem : cveItems) {
				
				cveInfo = cveDatajsonReader(cveItem);
				if(cveInfo == null || cveInfo.isEmpty()) {
					continue;
				}
				
				String cveId = (String) cveInfo.get("cveId");
				
				// 전체 cpe match 정보에서 vulnerable 가 false인 경우는 제외한다.
				// 적용대상 cpe match list
				List<String> matchNames = null;
				cpe_match_all = (List<Map<String, Object>>) cveInfo.get("cpe_match_all");
				
//				if(cpe_match_all.isEmpty()) {
//					log.info("REJECTED CVE " + cveId);
//				}
				
				ossList.clear();
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

					matchNames = nvdDataMapper.selectNvdMatchList(_matchNameParams);

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
						_productInfo.put("vendor", cpeInfoArr[3]);
						_productInfo.put("product", cpeInfoArr[4]);
						_productInfo.put("version", cpeInfoArr[5]);

						ossList.add(_productInfo);
					}
				}

				comapare = nvdDataMapper.selectOneCveInfoV3(cveInfo);
				// 신규등록
				if(comapare == null){
					nvdDataMapper.insertCveInfoV3(cveInfo);
					
					if(!ossList.isEmpty()) {
						insertDataList.addAll(ossList);
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
						insertDataList.addAll(ossList);
					}
				} else if (DateUtil.equals((Date)comapare.get("modiDate"), (Date) cveInfo.get("modiDate"))
						&& ((Float)comapare.get("cvssScore")).equals(Float.valueOf((String)cveInfo.get("cvssScore")))) {
					// NVD_CVE_V3는 변경 대상이 아니지만 NVD_DATA_V3에 적용 될 대상이 존재 한 경우
					
					if(!ossList.isEmpty()) {
						insertDataList.addAll(ossList);
					}
				}
				
				if(insertDataList.size() % BATCH_SIZE == 0) {
					prepareStatementUpdateNvdData(insertDataList);
					insertDataList.clear();
				}
			}
			
			if(insertDataList.size() > 0) {
				prepareStatementUpdateNvdData(insertDataList);
			}
		}
	
		return updateFlag;
	}

	private void prepareStatementUpdateNvdData(List<Map<String, String>> insertDataList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
			conn.setAutoCommit(false);
			
			String SQL_INSERT = "INSERT INTO NVD_DATA_V3 (CVE_ID, PRODUCT, VERSION, VENDOR) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE CVE_ID = values(cve_id), PRODUCT = values(product), VERSION = values(version), VENDOR = values(vendor)";
			stmt = conn.prepareStatement(SQL_INSERT);
			
			int seq = 1;
			
			for(Map<String, String> item : insertDataList){
				stmt.setString(1, (String) item.get("cveId"));
				stmt.setString(2, (String) item.get("product"));
				stmt.setString(3, (String) item.get("version"));
				stmt.setString(4, (String) item.get("vendor"));
				stmt.addBatch();
				stmt.clearParameters();
				
				if((seq % BATCH_SIZE) == 0 ) {
					stmt.executeBatch();
					stmt.clearBatch();
					conn.commit();
				}
				
				seq++;
			}
			
			stmt.executeBatch();
			conn.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if(conn != null) {
				try {
					conn.rollback();
				} catch (Exception e2) {
					log.error(e2.getMessage(), e2);
				}
			}
		} finally {
			try{
				if(stmt!=null)
					stmt.close();
			}catch(SQLException e){}
			
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException e){}
		}
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

//		if(!impact.containsKey("baseMetricV3") && !impact.containsKey("baseMetricV2")) {
//			// REJECT
//			return null;
//		}
		
		// CVSS V3가 없는 경우 V2 Score를 사용
		String baseScore = "0";
		String baseMetric = "";
		if(impact.containsKey("baseMetricV3")) {
			Map<String, Object> baseMetricV3 = (Map<String, Object>) impact.get("baseMetricV3");
			Map<String, Object> cvssV3 = (Map<String, Object>) baseMetricV3.get("cvssV3");
			baseScore = String.valueOf(cvssV3.get("baseScore"));
			baseMetric = "V3";					
		} else if(impact.containsKey("baseMetricV2")){
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
		
		log.warn("5초후 재시도합니다.");
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
			Thread.sleep(1000 * 30);
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
	public String nvdMetaDataSyncJob() throws Exception {
		String resCd = "00";

		// 1. Wait Job 데이터 조회
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("fileType", "MATCH");
		List<HashMap<String, Object>> waitList = nvdDataMapper.selectWaitJobData(param);

		
		String NVD_CVE_PATH = env.getProperty("root.dir");
		if(StringUtil.isEmpty(NVD_CVE_PATH)) {
			NVD_CVE_PATH = new FileSystemResource("").getFile().getAbsolutePath();
		}
		NVD_CVE_PATH = Paths.get(NVD_CVE_PATH, "nvd/cve").toString();

		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;

		String SQL_INSERT = "INSERT INTO NVD_CPE_MATCH_TEMP (SEQ, CPE23URI, VER_START_INC, VER_END_INC, VER_START_EXC, VER_END_EXC) VALUES (?,?,?,?,?,?)";
		String SQL_INSERT2 = "INSERT INTO NVD_CPE_MATCH_NAMES_TEMP (SEQ, IDX, CPE23URI) VALUES (?,?,?)";
		
		// 2. Json File -> DB Insert
		for (Map<String, Object> wMetaMap : waitList) {
			param.put("fileNm", wMetaMap.get("fileNm"));
			param.put("modiDate", wMetaMap.get("modiDate"));
			param.put("jobStatus", "G");
			// JobStatus가 W인 대상이 작업이 들어갔다면 G로 변경을 하여 추후에 다시 loop 돌지 않도록 처리함.
			
			log.info("Start NVD Meta Job");
			
			nvdDataMapper.updateJobStatus(param);

			int totSize = 0;
			try {
				log.info("Read NVD Meta Data, fileName: " + wMetaMap.get("fileNm"));
				Map<String, Object> dataMap = new ObjectMapper().readValue(new File(
						NVD_CVE_PATH + File.separator + ((String) wMetaMap.get("fileNm")).toLowerCase() + ".json"),
						new TypeReference<Map<String, Object>>() {
						});

				if (dataMap.containsKey("matches")) {
					List<Map<String, Object>> matchItems = (List<Map<String, Object>>) dataMap.get("matches");
					int seq = 1;
					totSize = matchItems.size();
					log.info("Find NVD Meta matches item : " + totSize);
					
					// 조건이 변경되거나, 삭제되는 경우를 고려하여 전체 데이터를 삭제하고 다시 등록한다.
					// truncate table
					nvdDataMapper.createTableCpeMatchTemp();
					nvdDataMapper.createTableCpeMatchNameTemp();
					nvdDataMapper.truncateCpeMatchTemp();
					nvdDataMapper.truncateCpeMatchNameTemp();
					
					try {
						Class.forName(JDBC_DRIVER);
						conn = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
						conn.setAutoCommit(false);
						stmt = conn.prepareStatement(SQL_INSERT);
						stmt2 = conn.prepareStatement(SQL_INSERT2);
						
						int seq2 = 1;
						
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

							stmt.setInt(1, seq);
							stmt.setString(2, cpe23Uri);
							stmt.setString(3, versionStartIncluding);
							stmt.setString(4, versionEndIncluding);
							stmt.setString(5, versionStartExcluding);
							stmt.setString(6, versionEndExcluding);
							stmt.addBatch();
							stmt.clearParameters();

							// CPE Names
							if(matchItem.containsKey("cpe_name")) {
								int nameIdx = 0;
								for(Map<String, Object> cpe_name : (List<Map<String, Object>>) matchItem.get("cpe_name")) {
									stmt2.setInt(1, seq);
									stmt2.setInt(2, nameIdx);
									stmt2.setString(3, (String) cpe_name.get("cpe23Uri"));
									stmt2.addBatch();
									stmt2.clearParameters();
									
									if(seq2 % BATCH_SIZE == 0) {
										stmt2.executeBatch(); // Batch 실행
										stmt2.clearBatch(); // Batch 초기화
					                    conn.commit(); // 커밋
									}
									nameIdx++;
									seq2++;
								}
							}
							
							if(seq % BATCH_SIZE == 0) {
								stmt.executeBatch(); // Batch 실행
			                    stmt.clearBatch(); // Batch 초기화
			                    conn.commit(); // 커밋
								log.info("In progress : " + seq + "/" + totSize);
							}

							seq++;
						}
						
						stmt.executeBatch(); // Batch 실행
						stmt2.executeBatch(); // Batch 실행
						conn.commit();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						if(conn != null) {
							try {
								conn.rollback();
							} catch (Exception e2) {
								log.error(e2.getMessage(), e2);
							}
						}
					} finally {
						try{
							if(stmt!=null)
								stmt.close();
						}catch(SQLException e){}
						try{
							if(stmt2!=null)
								stmt2.close();
						}catch(SQLException e){}
						try{
							if(conn!=null)
								conn.close();
						}catch(SQLException e){}
					}
				}

				if(totSize > 0) {
					nvdDataMapper.truncateCpeMatch();
					nvdDataMapper.truncateCpeMatchNames();
					nvdDataMapper.copyNvdDataMatchFromTemp();
					nvdDataMapper.copyNvdDataMatchNameFromTemp();
				}

				nvdDataMapper.truncateCpeMatchTemp();
				nvdDataMapper.truncateCpeMatchNameTemp();
				
				param.put("jobStatus", "C");
				nvdDataMapper.updateJobStatus(param);
				
				log.info("End NVD Meta Job");
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw e;
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
				log.warn("connection error : " + CommonFunction.httpCodePrint(con.getResponseCode()) + " - " + con.getResponseCode() + ", file name:" + FILE_NM);
			}
		} catch(UnknownHostException e) {
			log.error("unknownHost connection error : " + CommonFunction.httpCodePrint(con.getResponseCode()) + " - " + con.getResponseCode() + ", file name:" + FILE_NM);
			return metaInfo;
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
