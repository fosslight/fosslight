/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.OssMaster;

@Mapper
public interface NvdMapper {
	// 사용중인 메타 데이터 조회
	public List<HashMap<String, Object>> selectUseMetaData(HashMap<String, Object> param);
	
	// 메타 데이터 신규 등록
	public void insertNewMetaData(HashMap<String, Object> param);
	
	// 메타 데이터 사용유무 변경 
	public void updateUseYN(HashMap<String, Object> param);
	
	// 메타 데이터 Wait Job 데이터 조회
	public List<HashMap<String, Object>> selectWaitJobData(HashMap<String, Object> param);
	
	// 메타 데이터 Delete Job 데이터 조회
	public List<HashMap<String, Object>> selectDeleteJobData(HashMap<String, Object> param);
	
	// 메타 데이터 Job 상태 변경
	public void updateJobStatus(HashMap<String, Object> param);
	
	// CVE 데이터 삭제
	public void deleteCveData(HashMap<String, Object> param);
	
	// SW 취약점 리스트 데이터 삭제
	public void deleteVulnSwData(HashMap<String, Object> param);
	
	// CPE 데이터 조회
	public HashMap<String, Object> selectOneCpeDicData(HashMap<String, Object> param);
	
	// CPE 데이터 등록
	public void insertCpeDicData(HashMap<String, Object> param);
	
	// CPE Dictionary 데이터 삭제
	public void deleteCpeDicData();
	
	// SW 취약점 개수 조회
	public int selectVulnSwCnt(HashMap<String, Object> param);
	
	// CVE 정보 조회
	public HashMap<String, Object> selectOneCveInfo(HashMap<String, Object> param);
	
	// SW 취약점 정보 조회
	public HashMap<String, Object> selectOneVulnSwInfo(HashMap<String, Object> param);
	
	// CVE 정보 등록
	public void insertCveInfo(HashMap<String, Object> param);
	
	// SW 취약점 정보 등록
	public void insertVulnSwInfo(HashMap<String, Object> param); 
	
	// OSS 배치 타겟 초기화
	public void updateOssBatTarget();
	
	// OSS 배치 타겟 조회
	public List<Map<String, Object>> selectOssBatTarget();
	
	// OSS 취약점 정보 설정
	public void updateOssVulnInfo(Map<String, Object> param);
	
	// OSS 취약점 유무 설정
	public void updateOssVulnYn(HashMap<String, Object> param);
	
	// 메타 데이터 Job Run Timeout
	public void updateJobRunTimeout(HashMap<String, Object> param);
	
	// Local에 저장된 입력 데이터 조회 - 첫 NVD 데이터 입력 시 사용
	public List<HashMap<String, Object>> selectLocalJobData(HashMap<String, Object> param);
	
	public List<String> selectUsedVulnerabilityOssProject(OssMaster ossBean);
	
	public void deleteNvdDataTemp();
	
	public void deleteNvdData();
	
	public int insertNvdDataTemp();
	
	public int copyNvdDataFromTemp();
	
	public void deleteNvdDataScore();
	
	public int insertNvdDataScore();
	
	public List<Map<String, Object>> selectOssBatTargetWithOutVersion();
	
	public int updateOssRecheckVulnFlag();
	
	public HashMap<String, Object> selectOneCveInfoV3(Map<String, Object> cveInfo);
	
	public void insertCveInfoV3(Map<String, Object> cveInfo);
	
	public void deleteCveDataV3(Map<String, Object> cveInfo);
	
	public void deleteNvdDataV3(Map<String, Object> cveInfo);
	
	public void insertNvdDataV3(Map<String, String> ossInfo);
	
	public void deleteNvdDataTempV3();

	public int insertNvdDataTempV3();
	
	public void deleteNvdDataScoreV3();
	
	public void insertNvdDataScoreV3();
	
	public List<Map<String, Object>> selectOssBatTargetV3();
	
	public List<Map<String, Object>> selectOssBatTargetWithOutVersionV3();
	
	public Map<String, Object> selectNvdInfo(OssMaster ossBean);
	
	public Map<String, Object> selectNvdInfoWithOutVer(OssMaster ossBean);
	
	public void updateOssVulnInfoNew(OssMaster ossBean);
	
	public void insertNvdOssHis(OssMaster ossBean);
	
	public List<Map<String, Object>> selectOssBatTargetV3test();
	
	public List<Map<String, Object>> selectOssBatTargetWithOutVersionV3test();
	
	public int selectNvdTotalCount();
	
	public int deleteOssVulnInfo(String ossId);
	
	public int selectNickNameMgrtNvdDataScoreV3();
	
	public int insertNickNameMgrtNvdDataScoreV3();
	
	public int selectMaxCvssScoreNvdDataScoreV3();
	
	public int insertMaxCvssScoreNvdDataScoreV3();
	
	public int ossNameNickNameCvssScoreDiffCnt();
	
	public int ossNameToNickNameMgrtCvssScore();
	
	public int nickNameToOssNameMgrtCvssScore();
	
	public void truncateCpeMatchNames();
	
	public void truncateCpeMatch();
	
	public void insertCpeMatchData(Map<String, Object> matchInfo);
	
	public void insertCpeMatchNameData(Map<String, Object> matchInfo);
	
	public List<String> selectNvdMatchList(Map<String, String> _matchNameParams);
	
	public void insertBulkCpeMatchData(List<Map<String, Object>> insertDataList);

	public void insertBulkCpeMatchNameData(List<Map<String, Object>> insertDataList);
	
	public void insertBulkNvdDataV3(Object object);
	
	public void nvdBulkDataCleanCVE();
	
	public void nvdBulkDataCleanData();
}
