package oss.fosslight.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Configuration;
@Configuration("NvdDataMapper")
@Mapper
public interface NvdDataMapper {
	
	String getCodeString(@Param(value = "cdNo") String cdNo, @Param(value = "cdDtlNo") String cdDtlNo);
	String getCodeExp(@Param(value = "cdNo") String cdNo, @Param(value = "cdDtlNo") String cdDtlNo);

	List<HashMap<String, Object>> selectUseMetaData(HashMap<String, Object> params);

	int insertNewMetaData(HashMap<String, Object> params);
	
	int insertErrorMetaData(HashMap<String, Object> params);

	int updateUseYN(HashMap<String, Object> params);

	int updateJobStatus(HashMap<String, Object> params);
	List<HashMap<String, Object>> selectWaitJobData(HashMap<String, Object> params);
	void truncateCpeMatchNames();
	void truncateCpeMatch();
	void insertBulkCpeMatchData(List<Map<String, Object>> params);
	void insertBulkCpeMatchNameData(List<Map<String, Object>> params);
	Map<String, Object> selectOneCveInfoV3(Map<String, Object> params);
	List<String> selectNvdMatchList(Map<String, String> params);
	void insertCveInfoV3(Map<String, Object> params);
	void insertNvdDataV3(Map<String, String> params);
	void insertBulkNvdDataV3(List<Map<String, String>> params);
	void insertNvdDataV3Temp(Map<String, Object> params);
	void deleteCveDataV3(Map<String, Object> params);
	void deleteNvdDataV3(Map<String, Object> params);
	void deleteNvdDataTempV3();
	int getProducVerCnt();
	List<Map<String, Object>> getProducVerList(@Param(value = "pageIdx")int pageIdx, @Param(value = "pageCnt")int pageCnt);
	public Map<String, Object> getMaxScoreProductVer(@Param(value = "ossName")String ossName, @Param(value = "ossVersion")String ossVersion, @Param(value = "vendor") String vendor);
	void insertNvdDataListTempV3(List<Map<String, Object>> params);
	void deleteNvdDataScoreV3();
	void insertNvdDataScoreV3();
	int selectNickNameMgrtNvdDataScoreV3();
	void insertNickNameMgrtNvdDataScoreV3();
	int selectMaxCvssScoreNvdDataScoreV3();
	void insertMaxCvssScoreNvdDataScoreV3();
	int ossNameNickNameCvssScoreDiffCnt();
	int ossNameToNickMgrtCvssScoreDiffCnt();
	void ossNameToNickNameMgrtCvssScore();
	void nickNameToOssNameMgrtCvssScore();
	void insertCpeMatchData(Map<String, Object> params);
	void insertCpeMatchNameData(Map<String, Object> params);
	void resetCveDataV3();
	void resetNvdDataV3();
	void createTableCpeMatchTemp();
	void createTableCpeMatchNameTemp();
	void truncateCpeMatchTemp();
	void truncateCpeMatchNameTemp();
	void copyNvdDataMatchFromTemp();
	void copyNvdDataMatchNameFromTemp();
	int selectVendorProductNvdDataV3Cnt();
	void updateVendorProductNvdDataV3();
	int selectVendorProductNvdDataScoreV3Cnt();
	void updateVendorProductNvdDataScoreV3();
	void insertNewMetaDataUrlConnection(HashMap<String, Object> param);
	List<Map<String, Object>> selectUseMetaDataUrlConnection(HashMap<String, Object> param);
	void insertNvdDataPatchLink(Map<String, Object> param);
	void deleteNvdDataPatchLink(@Param(value = "cveId") String cveId);
	int selectNvdCpeMatch(Map<String, Object> param);
	int selectNvdCpeMatchTemp(Map<String, Object> param);
	void deleteNvdCpeMatch(Map<String, Object> param);
	void deleteNvdCpeMatchTemp(Map<String, Object> param);
	void deleteNvdCpeMatchNames(@Param(value = "matchCriteriaId") String matchCriteriaId);
	void insertNvdDataConfigurationsTemp(Map<String, String> param);
	void truncateNvdDataConfigurations();
	void copyNvdDataConfigurationsFromTemp();
	void truncateNvdDataConfigurationsTemp();
	void deleteNvdDataConfigurations(Map<String, String> param);
}
