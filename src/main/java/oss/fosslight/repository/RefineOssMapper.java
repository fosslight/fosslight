package oss.fosslight.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefineOssMapper {

	int getRefineOssTotalCnt(@Param("ossName")String ossName, @Param("refineType")String refineType);

	List<Map<String, Object>> selectRefineOssCommonList(@Param("ossName")String ossName, @Param("refineType")String refineType, @Param("startIndex")int startIndex, @Param("pageListSize")int pageListSize);

	List<Map<String, String>> selectOssDownloadLocationList(String ossCommonId);

	void insertOssDownloadLocation(@Param("ossCommonId")String ossCommonId, @Param("list")List<Map<String, String>> list);

	int deleteOssDownloadLocation(String ossCommonId);
	
}