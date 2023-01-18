package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.BinaryData;

@Mapper
public interface BinaryDataMapper {

	int countBinaryList(BinaryData vo);

	List<BinaryData> getBinaryList(BinaryData vo);

	int deleteBinaryData(String bianryDataId);

	int updateBinaryData(BinaryData bean);

	int insertBinaryData(BinaryData bean);
	
	int insertBinaryDataLog(BinaryData logBean);

	int countExistsBinaryName(String fileName);
	
	List<BinaryData> getBinaryListWithNameAndChecksum(@Param("fileName")String fileName, @Param("checkSum")String checkSum);
	
	int deleteBinaryListWithNameAndChecksum(@Param("fileName")String fileName, @Param("checkSum")String checkSum);
	
	List<BinaryData> getBinaryTlshListWithoutChecksum(@Param("fileName")String fileName, @Param("checkSum")String checkSum);
	
	int updateTlshCheckSumToZero(@Param("fileName")String fileName, @Param("checkSum")String checkSum, @Param("ossName")String ossName, @Param("ossVersion")String ossVersion);

	List<BinaryData> selectBinaryDataListWithBinaryName(String binaryName);

}