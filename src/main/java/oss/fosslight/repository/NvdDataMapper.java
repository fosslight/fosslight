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
	
	void truncateCpeMatchNames();
	void truncateCpeMatch();
	void insertBulkCpeMatchNameData(List<Map<String, Object>> params);
	Map<String, Object> selectOneCveInfoV3(Map<String, Object> params);
	void insertCveInfoV3(Map<String, Object> params);
	void insertNvdDataV3(Map<String, String> params);
	void insertBulkNvdDataV3Temp(List<Map<String, String>> params);
	void insertNvdDataScoreV3Temp(Map<String, Object> params);
	void deleteCveDataV3(Map<String, Object> params);
	void deleteNvdDataV3(Map<String, Object> params);
	void deleteNvdDataScoreV3Temp();
	int getProducVerCnt();
	List<Map<String, Object>> getProducVerList(@Param(value = "pageIdx")int pageIdx, @Param(value = "pageCnt")int pageCnt);
	Map<String, Object> getMaxScoreProductVer(@Param(value = "ossName")String ossName, @Param(value = "ossVersion")String ossVersion, @Param(value = "vendor") String vendor);
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
	void insertNvdDataPatchLinkTemp(Map<String, Object> param);
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
	void deleteNvdDataMatchExistingInTemp();
	void createTableConfigurationsTemp();
	void createTablePatchLinkTemp();
	void truncateNvdDataPatchLinkTemp();
	void truncateNvdDataPatchLink();
	void copyNvdDataPatchLinkFromTemp();
	void deleteNvdDataConfigurationsExistingInTemp();
	void deleteNvdDataPatchLinkExistingInTemp();
	void insertCveInfoV3Temp(Map<String, Object> cveInfo);
	void createTableNvdCveV3Temp();
	void truncateNvdCveV3Temp();
	void copyNvdCveV3FromTemp();
	
	void createTableNvdDavaScoreV3Temp();
	int insertNvdDataV3Temp(String cveId);
	void deleteNvdCveV3ExistingInTemp();
	void deleteNvdDataV3ExistingInTemp();
	void copyNvdDataV3FromTemp();
	void createTableNvdDataV3Temp();
	void truncateNvdDataV3Temp();
	
}
