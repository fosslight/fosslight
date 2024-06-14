/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */
package oss.fosslight.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.BinaryAnalysisResult;
import oss.fosslight.domain.BinaryData;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.repository.BinaryDataMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.util.TlshUtil;


@Service
@Slf4j
public class BinaryDataService  extends CoTopComponent {

	@Autowired private BinaryDataMapper binaryDataMapper;
	@Autowired private FileService fileService;
	@Autowired private ProjectMapper projectMapper;
	@Autowired private PartnerMapper partnerMapper;
	@Autowired AutoIdService autoIDService;
	
	public Map<String, Object> getBinaryList(String page, String rows , BinaryData vo) {
		int records = 0;
		HashMap<String, Object> map = new HashMap<String, Object>();

		String filterCondition = CommonFunction.getFilterToString(vo.getFilters());
		vo.setFilterCondition(filterCondition);

		if(!StringUtil.isEmpty(vo.getFileName()) && vo.getFileName().indexOf("/") > -1) {
			String[] splitFileName = vo.getFileName().split("/");
			vo.setFileName(splitFileName[splitFileName.length-1]);
		}
		
		records = binaryDataMapper.countBinaryList(vo);
		vo.setTotListSize(records);

		if(CoConstDef.FLAG_NO.equals(vo.getBinaryPopupFlag())) {
			vo.setCurPage(Integer.parseInt( StringUtil.isNotEmpty(page) ? page : "1"));
			vo.setPageListSize(Integer.parseInt( StringUtil.isNotEmpty(rows) ? rows : "20"));
		}
		
		List<BinaryData> list = binaryDataMapper.getBinaryList(vo);
		for(BinaryData item : list) {
			item.setDownloadlocation(CommonFunction.getOssDownloadLocation(item.getOssName(), item.getOssVersion()));
		}
		
		if(CoConstDef.FLAG_NO.equals(vo.getBinaryPopupFlag())) {
			map.put("page", vo.getCurPage());
			map.put("total", vo.getTotBlockSize());
		}
		map.put("records", records);
		map.put("rows", list);
		
		return map;
	}
	
	@Transactional
	@CacheEvict(value={"tlshFindOssInfoCache", "tlshDistanceCache"}, allEntries=true)
	public void setBinaryDataModify(BinaryData bean) {
//		Connection conn = null;
//		PreparedStatement stmt = null;
//		String SQL_UPDATE_LGBAT = "UPDATE lgematching SET ossname = ?, ossversion = ?, license = ? , parentname = ?, pathname = ?, sourcepath = ?,platformname=?,platformversion=?, updatedate = CURRENT_TIMESTAMP WHERE concat( filename , '-' , checksum, '-', COALESCE(ossname,''), '-', COALESCE(ossversion,''), '-', COALESCE(REPLACE(license, ',','|'),'') ) = ?";
//		String SQL_DELETE_LGBAT = "DELETE FROM lgematching WHERE concat( filename , '-' , checksum, '-', COALESCE(ossname,''), '-', COALESCE(ossversion,''), '-', COALESCE(REPLACE(license, ',','|'),'') ) = ?";
		
		boolean isDeleteMode = CoConstDef.GRID_OPERATION_DELETE.equals(bean.getOper());
		List<BinaryData> historyList = new ArrayList<>();
		
		if(!StringUtil.isEmpty(bean.getId()) && StringUtil.isEmpty(bean.getBatId())) {
			bean.setBatId(bean.getId());
		}
		
		if(StringUtil.isEmpty(bean.getBatId())) {
			bean.setBatId(bean.getFileName() + "-" + bean.getCheckSum() + "-" + avoidNull(bean.getOssName()) + "-" + avoidNull(bean.getOssVersion()) + "-" + avoidNull(bean.getLicense()));
		}
		
		// 변경 이력을 남기기 위해 삭제인 경우 기존 정보를 취득한다.
		 if(isDeleteMode) {
			 BinaryData searchParam = new BinaryData();
			for(String id : bean.getId().split(",")) {
				if(!StringUtil.isEmpty(id)) {
					searchParam.setBatId(id);
					Map<String, Object> searchResultMap = getBinaryList(null, null, searchParam);
					if(searchResultMap.get("rows") != null) {
						historyList.addAll( (List<BinaryData>) searchResultMap.get("rows"));
					}
				}
			}
		 }
		
		if("N/A".equals(bean.getOssVersion())) {
			bean.setOssVersion("");
		}
		
		if(isDeleteMode) {
			 BinaryData searchParam = new BinaryData();
			for(String id : bean.getId().split(",")) {
				if(!StringUtil.isEmpty(id)) {
					searchParam.setBatId(id);
					binaryDataMapper.deleteBinaryData(id);
				}
			}
		} else {
			binaryDataMapper.updateBinaryData(bean);
			historyList.add(bean);
		}
		
		// 변경 이력
		if(!historyList.isEmpty()) {
			try {
				String actionId = isEmpty(bean.getActionId()) ? DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN) : bean.getActionId();
				// bat vo => BinaryAnalysisResult
				for(BinaryData hisVo : historyList) {
					hisVo.setActionId(actionId);
					hisVo.setActionType(isDeleteMode ? "DELETE" : "MODIFY");
					binaryDataMapper.insertBinaryDataLog(hisVo);
				}				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	@Transactional
	public void setBinaryDataListModify(List<BinaryData> list) {
		String actionId = DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN);
		
		for(BinaryData bean : list) {
			bean.setActionId(actionId);
			setBinaryDataModify(bean);
		}
	}
	

	private List<BinaryData> getBinaryDbLogBean(String type, List<BinaryData> checkList) {
		if(checkList != null) {
			for(BinaryData item : checkList) {
				item.setActionType(type);
			}
		}
		return checkList;
	}

	public Map<String, Object> getExistBinaryName (BinaryData vo){
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		int totalCnt = binaryDataMapper.countExistsBinaryName(vo.getFileName());
		map.put("isValid", totalCnt > 0);
		return map;
	}
	
	private boolean checkTlshDistance(String tlsh1, String tlsh2) {
		log.debug("check tlsh distance : tlsh1 = " + avoidNull(tlsh1) +", tlsh2 = " + avoidNull(tlsh2));
		if(!isEmpty(tlsh1) && !isEmpty(tlsh2)) {
			int rtn = TlshUtil.compareTlshDistance(tlsh1,tlsh2);
			log.debug("distance result = " + rtn);
			return rtn > -1 && rtn <= 120;
		}
		return false;
	}
	
	
	public void insertBatConfirmBinOssWithChecksum(String prjName, String platformName, String platformVer, String fileId, List<ProjectIdentification> list) {
		T2File binaryTextFile = new T2File();
		
		if(!isEmpty(fileId) && list != null && !list.isEmpty()) {
			binaryTextFile = fileService.selectFileInfoById(fileId);
		}
		
		insertBatConfirmBinOssWithChecksum(null, prjName, platformName, platformVer, binaryTextFile, list);
	}
	
	public void insertBatConfirmBinOssWithChecksum(String gubn, String prjName, String platformName, String platformVer, T2File binaryTextFile, List<ProjectIdentification> list) {
		Map<String, String[]> checksumInfoMap = new HashMap<>();
		Map<String, String[]> checksumInfoMapByBinaryPath = new HashMap<>();
		
		for (ProjectIdentification bean : list) {
			if (isEmpty(bean.getBinaryName()) || isEmpty(bean.getCheckSum())) {
				continue;
			}
			
			String binaryName = bean.getBinaryName();
			if (binaryName.endsWith("/") || binaryName.endsWith("\\")) {
				continue;
			}
			if (binaryName.indexOf("/") > -1) {
				binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
			}
			if (binaryName.indexOf("\\") > -1) {
				binaryName = binaryName.substring(binaryName.lastIndexOf("\\") + 1);
			}
			if (isEmpty(binaryName)) {
				continue;
			}
			
			checksumInfoMapByBinaryPath.put(bean.getBinaryName(), new String[]{bean.getCheckSum(), avoidNull(bean.getTlsh(), "0")});
			checksumInfoMap.put(binaryName, new String[]{bean.getCheckSum(), avoidNull(bean.getTlsh(), "0")});
		}
		
		if (checksumInfoMap.isEmpty() && checksumInfoMapByBinaryPath.isEmpty()) {
			//bin binary.txt 파일 정보를 추출한다.
			if(binaryTextFile != null) {
				String _contents = avoidNull(CommonFunction.getStringFromFile(binaryTextFile.getLogiPath() + "/" + binaryTextFile.getLogiNm()));
//				Binary	sha256sum	tlsh
//				./tlsh_unittest	404b0e9fad52eecd8c342779e8cddff9385d7c094a9e3a29142aa4ef3e2398ea	46B3DE93D364FEAFDA28FBFC598A78D9C4C5A0522DF00A4B65461F9A00CE1D06B453ED
//				./rand_tags	40d2efb42d536f0a1afb2cbcac8e0b38994a8caa3d50d95fa663a852ea655725	B9B3CD93D364FEAFDA28FAFC598978D9C4CAD4122DF00A5B65021F9A04CD2D06B453ED
//				./tlsh_version	ee2a667560664b50a13414db878ef31a826f627b11bce658d3b74421140d6253	39021FC7A3E1CAAFCC9822BD085F077532B3D4B2436383120A0AA7741F41BD91F59999
//				./simple_unittest	79a24b0c4dbcef50e1f59dd078e913d06d0ad5115a459a9ddd743b909a8edf89	90939A93D364FE9FEA28EAFC598978D9C4C994132DF00A5B65421F9A40CE1D03B453EE
				
				boolean isHeader = true;
				for(String line : _contents.split(System.lineSeparator())) {
					if(isHeader) {
						isHeader = false;
						continue;
					}
					
					// binary name을 key로 sha256sum과 tlsh 를 격납한다.
					String[] data = line.split("\t", -1);
					if(data == null || data.length !=3
							|| isEmpty(data[0]) || isEmpty(data[1])/* || isEmpty(data[2])*/) {
						log.warn("unexpected format Bin binary text ("+prjName+") : " + line);
						continue;
					}
					String binaryName = data[0].trim();
					if(binaryName.endsWith("/") || binaryName.endsWith("\\")) {
						log.warn("unexpected format Bin binary text (is not file) ("+prjName+") : " + line);
						continue;
					}
					if(binaryName.indexOf("/") > -1) {
						binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
					}
					if(binaryName.indexOf("\\") > -1) {
						binaryName = binaryName.substring(binaryName.lastIndexOf("\\") + 1);
					}
					if(isEmpty(binaryName)) {
						continue;
					}
					
					checksumInfoMapByBinaryPath.put(data[0].trim(), new String[]{data[1], avoidNull(data[2], "0")});
					checksumInfoMap.put(binaryName, new String[]{data[1], avoidNull(data[2], "0")});
				}
			}
		}
		
		// checksum 정보가 존재하는 경우
		// bat db 등록을 위해 oss list에서 checksum 정보에 존재하는 oss 정보를 격납한다.
		Map<String, BinaryAnalysisResult> batInfoMap = new HashMap<>();
		if(!checksumInfoMap.isEmpty() || !checksumInfoMapByBinaryPath.isEmpty()) {
			for(ProjectIdentification bean : list) {
				if(CoConstDef.FLAG_YES.equals(bean.getExcludeYn()) || isEmpty(bean.getBinaryName()) || bean.getBinaryName().endsWith("/") || bean.getBinaryName().endsWith("\\")) {
					continue;
				}
				String[] checkSumData = null;
				String binaryName = bean.getBinaryName();
				if(binaryName.indexOf("/") > -1) {
					binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
				}
				
				if(binaryName.indexOf("\\") > -1) {
					binaryName = binaryName.substring(binaryName.lastIndexOf("\\") + 1);
				}
				
				if(checksumInfoMapByBinaryPath.containsKey(bean.getBinaryName())) { // full path로 검색해서 있다면 해당 정보를 담는다.
					checkSumData = checksumInfoMapByBinaryPath.get(bean.getBinaryName());
				}
				
				if(ArrayUtils.isEmpty(checkSumData) && checksumInfoMap.containsKey(binaryName)) { // full path로 검색해서 존재하지 않는 정보라면 binary name으로 검색을 하고 누적된 제일 마지막 정보를 담는다.
					checkSumData = checksumInfoMap.get(binaryName);
				}
				
				if(!ArrayUtils.isEmpty(checkSumData)){
					BinaryAnalysisResult _updateBean = new BinaryAnalysisResult();
					_updateBean.setBinaryName(binaryName);
					_updateBean.setFilePath(bean.getBinaryName());
					_updateBean.setCheckSum(checkSumData[0]);
					_updateBean.setTlsh(checkSumData[1]);
					_updateBean.setOssName(bean.getOssName());
					_updateBean.setOssVersion(bean.getOssVersion());
					_updateBean.setLicense(CommonFunction.getSelectedLicenseString(bean.getComponentLicenseList()));
					_updateBean.setParentname(prjName);
					
					_updateBean.setPlatformname(platformName);
					_updateBean.setPlatformversion(platformVer);
					
					_updateBean.setDownloadLocation(bean.getDownloadLocation());
					_updateBean.setPrjId(bean.getReferenceId());
					
					batInfoMap.put(_updateBean.getBinaryName() + "|" + _updateBean.getCheckSum()+ "|" +avoidNull(_updateBean.getOssName())+ "|" +avoidNull(_updateBean.getOssVersion())+ "|" +avoidNull(_updateBean.getLicense()), _updateBean);
				}
			}
		}
		
		// db 등록
		if(!batInfoMap.isEmpty()) {
			try {
				mergeBinaryOssInfo(batInfoMap);
				
				String mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_BINARY_DATA_COMMIT;
				
				if(!isEmpty(gubn)) {
					mailType = CoConstDef.CD_MAIL_TYPE_PARTNER_BINARY_DATA_COMMIT;
				}
				
				String comment = "Data is added to the Binary DB. : Success";
				CoMail mailBean = new CoMail(mailType);
				
				if(!isEmpty(gubn)) {
					mailBean.setParamPartnerId(batInfoMap.get(batInfoMap.keySet().toArray()[0]).getPrjId());
				} else {
					mailBean.setParamPrjId(batInfoMap.get(batInfoMap.keySet().toArray()[0]).getPrjId());
				}
				
				mailBean.setComment(comment);
				mailBean.setBinaryCommitResult("Success");
				//CoMailManager.getInstance().sendMail(mailBean);
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				String mailType = CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_BINARY_DATA_COMMIT;
				
				if(!isEmpty(gubn)) {
					mailType = CoConstDef.CD_MAIL_TYPE_PARTNER_BINARY_DATA_COMMIT;
				}
				
				String comment = "Data is added to the Binary DB. : Failed";
				CoMail mailBean = new CoMail(mailType);
				
				if(!isEmpty(gubn)) {
					mailBean.setParamPartnerId(batInfoMap.get(batInfoMap.keySet().toArray()[0]).getPrjId());
				} else {
					mailBean.setParamPrjId(batInfoMap.get(batInfoMap.keySet().toArray()[0]).getPrjId());
				}
				
				mailBean.setComment(comment);
				mailBean.setBinaryCommitResult("Failed");
				//CoMailManager.getInstance().sendMail(mailBean);
			}
		}
	}
	
	
	@Transactional
	private void mergeBinaryOssInfo(Map<String, BinaryAnalysisResult> batInfoMap) {

		List<BinaryData> historyList = new ArrayList<>();
		String actionId = DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN);
		
		// tlsh distance가 120이하이고, OSS / Version이 동일한 경우
		Map<String, String> binaryList = new HashMap<String, String>();
		List<String> duplicatedList = new ArrayList<>();
		
		for(BinaryAnalysisResult bean : batInfoMap.values()) {
			
			/** 
			 * OSC-1257 - BIN(Android), BIN > Binary DB 저장할 때 중복 Data 저장됨
			 * binary name + checksum값이 동일하고 path name만 다를 경우 duplicatedList에 추가하여 insert를 제한함.(최초에 발견된 path name만 insert함.)
			 * binary name + checksum 동일하면서 oss name과 license name이 다를 경우는 정상적으로 등록됨.
			 */
			String binaryKey =  bean.getBinaryName() + "_" + bean.getCheckSum();
			
			if(isEmpty((String) binaryList.get(binaryKey))) {
				binaryList.put(binaryKey, bean.getFilePath());
			} else {
				String pathData = binaryList.get(binaryKey);
				
				if(!pathData.equals(bean.getFilePath())) {
					duplicatedList.add(bean.getFilePath());
				}
			}
			
			// OSC-673
			// OSC-1056 binary name + checksum 이 동일하면 동일한 바이너리 임, 삭제하고 최신화
			bean.setOssName(avoidNull(bean.getOssName(), "-"));
			
			
			List<BinaryData> checkList = binaryDataMapper.getBinaryListWithNameAndChecksum(bean.getBinaryName(), bean.getCheckSum());
			if(checkList != null && checkList.size() > 0) {
				
				// For change history, change target information is acquired before updating.
				historyList.addAll(getBinaryDbLogBean("DELETE", checkList));
				
				// delete old data
				binaryDataMapper.deleteBinaryListWithNameAndChecksum(bean.getBinaryName(), bean.getCheckSum());
				
			}

			// Update if tlsh distance is within 120
			if(!"0".equals(bean.getTlsh())) {
				List<BinaryData> tlshList = binaryDataMapper.getBinaryTlshListWithoutChecksum(bean.getBinaryName(), bean.getCheckSum());
				if(tlshList != null) {
					for(BinaryData item : tlshList) {
						String _tlsh = item.getTlshCheckSum();
						String _checksum = item.getCheckSum();
						String _ossname = avoidNull(item.getOssName(), "-");
						String _ossversion = item.getOssVersion();
						
						if(checkTlshDistance(bean.getTlsh(), _tlsh)) {
							//log.debug("update tish distance under 120. binary name : " + bean.getBinaryName() + " , checksum : " + _checksum);
							
							// oss name 이 다르면 현행화 대상
							// oss name 과 version 이 같으면 현행화 (동일한 바이너리에 대해서 라이선스 정보를 현행화)
							// oss name은 같으나 version이 다른 경우 신규 추가만
							if(!bean.getOssName().equalsIgnoreCase(_ossname) 
									|| (bean.getOssName().equalsIgnoreCase(_ossname) && avoidNull(bean.getOssVersion()).equalsIgnoreCase(avoidNull(_ossversion)))) {
								
								// OSC-1056 oss name이 다르거나, 동일한 oss 인 경우 (oss name + version이 동일) 삭제하지 않고, tlsh 를 0으로 업데이트하여 기존에 등록된  binary정보가 변경되지 않도록 함 (checksum이 동일한 바리너리를 우선해서 찾기 때문에 tlsh가 달라져도 동일한 binary의 경우 무관함)
								item.setActionType("UPDATE");
								//item.setTlsh("0");// TLSH_CHECK_SUM
								historyList.add(item);
								
								binaryDataMapper.updateTlshCheckSumToZero(bean.getBinaryName(), _checksum, _ossname, _ossversion);
							}
						}
					}
				}
			}
			
		}
		
		for(BinaryAnalysisResult bean : batInfoMap.values()) {

			// binary Name과 binary Path이 전부 동일한 값이외의 값들은 전부 무시함. ** 동일한 checksum값이라고 하더라도 path정보가 다르다면 무시.
			if(duplicatedList.contains(bean.getFilePath())) { 
				continue;
			}
			
			BinaryData item = new BinaryData();
			item.setFileName(bean.getBinaryName());
			item.setPathName(bean.getFilePath());
			item.setSourcePath(bean.getSourcePath());
			item.setCheckSum(bean.getCheckSum());
			item.setTlshCheckSum(bean.getTlsh());
			item.setOssName(avoidNull(bean.getOssName(), "-"));
			item.setOssVersion(bean.getOssVersion());
			item.setLicense(bean.getLicense());
			item.setParentName(bean.getParentname());
			item.setPlatformName(bean.getPlatformname());
			item.setPlatformVersion(bean.getPlatformversion());
			
			binaryDataMapper.insertBinaryData(item);
			item.setActionType("INSERT");
			historyList.add(item);
			
		}
		
		try{
			// commit 성공하면 이력 등록
			for(BinaryData tempBean : historyList) {
				tempBean.setActionId(actionId);
				binaryDataMapper.insertBinaryDataLog(tempBean);
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
			
	}

	public void insertBatConfirmAndroidBinOssWithChecksum(String prjName, String platformName, String platformVer,
			String fileId, List<ProjectIdentification> list) {
		if(!isEmpty(fileId) && list != null && !list.isEmpty()) {
			log.debug("Start insertBatConfirmAndroidBinOssWithChecksum");
			//bin binary.txt 파일 정보를 추출한다.
			T2File binaryTextFile = fileService.selectFileInfoById(fileId);
			if(binaryTextFile != null) {
				Map<String, Object> readFileData = CommonFunction.getAndroidResultFileInfo(binaryTextFile, new ArrayList<String>());
				
				if(readFileData != null && readFileData.containsKey("addCheckList")) {
					Map<String, String[]> checksumInfoMap = new HashMap<>();
					Map<String, String[]> checksumInfoMapByBinaryPath = new HashMap<>();
					List<OssComponents> _list = (List<OssComponents>) readFileData.get("addCheckList");
					for(OssComponents bean : _list) {
						
						// binary name을 key로 sha256sum과 tlsh 를 격납한다.
						if(isEmpty(bean.getBinaryName()) || isEmpty(bean.getCheckSum()) || isEmpty(bean.getTlsh())) {
							continue;
						}
						String binaryName = bean.getBinaryName();
						if(binaryName.endsWith("/") || bean.getBinaryName().endsWith("\\")) {
							continue;
						}
						if(binaryName.indexOf("/") > -1) {
							binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
						}
						if(binaryName.indexOf("\\") > -1) {
							binaryName = binaryName.substring(binaryName.lastIndexOf("\\") + 1);
						}
						if(isEmpty(binaryName)) {
							continue;
						}
						
						checksumInfoMapByBinaryPath.put(bean.getBinaryName(), new String[]{bean.getCheckSum(), bean.getTlsh(), avoidNull(bean.getSourceCodePath())});
						checksumInfoMap.put(binaryName, new String[]{bean.getCheckSum(), bean.getTlsh(), avoidNull(bean.getSourceCodePath())});
					}
					
					// checksum 정보가 존재하는 경우
					// bat db 등록을 위해 oss list에서 checksum 정보에 존재하는 oss 정보를 격납한다.
					Map<String, BinaryAnalysisResult> batInfoMap = new HashMap<>();
					if(!checksumInfoMap.isEmpty() || !checksumInfoMapByBinaryPath.isEmpty()) {
						for(ProjectIdentification bean : list) {
							if(CoConstDef.FLAG_YES.equals(bean.getExcludeYn()) || isEmpty(bean.getBinaryName()) || bean.getBinaryName().endsWith("/") || bean.getBinaryName().endsWith("\\")) {
								continue;
							}
							
							String[] checkSumData = null;
							String binaryName = bean.getBinaryName();
							if(binaryName.indexOf("/") > -1) {
								binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
							}
							
							if(binaryName.indexOf("\\") > -1) {
								binaryName = binaryName.substring(binaryName.lastIndexOf("\\") + 1);
							}
							
							if(checksumInfoMapByBinaryPath.containsKey(bean.getBinaryName())) { // full path로 검색해서 있다면 해당 정보를 담는다.
								checkSumData = checksumInfoMapByBinaryPath.get(bean.getBinaryName());
							}
							
							if(ArrayUtils.isEmpty(checkSumData) && checksumInfoMap.containsKey(binaryName)) { // full path로 검색해서 존재하지 않는 정보라면 binary name으로 검색을 하고 누적된 제일 마지막 정보를 담는다.
								checkSumData = checksumInfoMap.get(binaryName);
							}
							
							if(!ArrayUtils.isEmpty(checkSumData)){
								BinaryAnalysisResult _updateBean = new BinaryAnalysisResult();
								_updateBean.setBinaryName(binaryName);
								_updateBean.setFilePath(bean.getBinaryName());
								_updateBean.setCheckSum(checkSumData[0]);
								_updateBean.setTlsh(checkSumData[1]);
								_updateBean.setSourcePath(checkSumData[2]);
								_updateBean.setOssName(bean.getOssName());
								_updateBean.setOssVersion(bean.getOssVersion());
								_updateBean.setLicense(CommonFunction.getSelectedLicenseString(bean.getComponentLicenseList()));
								_updateBean.setParentname(prjName);
								
								_updateBean.setPlatformname(platformName);
								_updateBean.setPlatformversion(platformVer);
								
								_updateBean.setDownloadLocation(bean.getDownloadLocation());
								_updateBean.setPrjId(bean.getReferenceId());
								
								// 하나의 binary에 여러개의 OSS가 등록되어 있는 경우 + OSS Name 이 없고 (하이픈), 라이선스만 다른 경우 key 중복으로 등록되지 않는 현상을 위해, 라이선스도 포함
								// OSC-806
								
								batInfoMap.put(_updateBean.getBinaryName() + "|" + _updateBean.getCheckSum()+ "|" +avoidNull(_updateBean.getOssName())+ "|" +avoidNull(_updateBean.getOssVersion())+ "|" +avoidNull(_updateBean.getLicense()), _updateBean);
							}
						}
					} else {
						log.debug("insertBatConfirmAndroidBinOssWithChecksum : checksumInfoMap is empty");
					}
					
					// db 등록
					if(!batInfoMap.isEmpty()) {
						mergeBinaryOssInfo(batInfoMap);
					} else {
						log.debug("insertBatConfirmAndroidBinOssWithChecksum : batInfoMap is empty");
					}
				}
			} else {
				log.debug("insertBatConfirmAndroidBinOssWithChecksum : cannot find file android result.txt");
			}

			log.debug("End insertBatConfirmAndroidBinOssWithChecksum");
		}
		
	}

	@Transactional
	public void autoIdentificationWithBinryTextFile(Project project) {
		if(!isEmpty(project.getBinBinaryFileId())) {
			// oss name과 license가 설정되지 않은 oss coponent를 찾는다.
			// component_id와 binary name만 반환한다.
			List<OssComponents> componentList = projectMapper.findBinAutoIdentificationWithBinaryText(project.getPrjId());
			
			if(componentList != null && !componentList.isEmpty()) {
				// DB에서 binary로 매핑되는 정보를 취득한다.
				Map<String, String[]> mappingData = loadBinaryText(project.getBinBinaryFileId(), true);
				
				if(mappingData != null && !mappingData.isEmpty()) {
					
					Map<String, List<BinaryData>> binaryRegInfoMap = new HashMap<>();
					
					for(OssComponents bean : componentList) {
						String binaryName = avoidNull(bean.getBinaryName());
						if(isEmpty(binaryName)) {
							continue;
						}
						
						// 업로드한 binary파일과 grid에 등록한 binaryname이 동일한 data인 경우, bat DB에서 oss 정보를 찾는다.
						String checkBinaryName = "";
						String[] tlshData = null;
						if(mappingData.containsKey(binaryName)) {
							tlshData = mappingData.get(binaryName);
							checkBinaryName = binaryName;
							
							if(checkBinaryName.indexOf("/") > -1) {
								checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("/") + 1);
							}
							
							if(checkBinaryName.indexOf("\\") > -1) {
								checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("\\") + 1);
							}
						}
						
						if(isEmpty(checkBinaryName) && ArrayUtils.isEmpty(tlshData)){
							checkBinaryName = binaryName;
							
							if(checkBinaryName.indexOf("/") > -1) {
								checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("/") + 1);
							}
							
							if(checkBinaryName.indexOf("\\") > -1) {
								checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("\\") + 1);
							}
							
							if(mappingData.containsKey(checkBinaryName)) {
								tlshData = mappingData.get(checkBinaryName);
							}
						}
						
						if(!isEmpty(checkBinaryName) && !ArrayUtils.isEmpty(tlshData)){
							List<BinaryData> binaryInfoList = autoIDService.findOssInfoWithBinaryName(checkBinaryName, tlshData[0], tlshData[1]);
							if(binaryInfoList != null) {
								binaryRegInfoMap.put(binaryName, binaryInfoList);
							}
						}		
					}
					// binary 정보를 찾는 시간이 오래걸릴 수 있기 때문에 틑랜젝션 이슈를 최소화 하기 위해 일단 binary 정보를 찾는 처리만 먼저 수행하고 트랜젝션을 분리하여 등록 한다.
					addOssComponentByBinaryInfo(componentList, binaryRegInfoMap);
				}
			}
		}
	}
	
	private Map<String, String[]> loadBinaryText(String fileId, boolean pathFlag) {

		Map<String, String[]> loadData = new HashMap<>();
		T2File binaryTextFile = fileService.selectFileInfoById(fileId);
		
		if(binaryTextFile != null && binaryTextFile.getExt().equals("txt")) {
			log.info("loadBinaryText : " + binaryTextFile.getLogiNm());
			String _contents = avoidNull(CommonFunction.getStringFromFile(binaryTextFile.getLogiPath() + "/" + binaryTextFile.getLogiNm()));
			
			if(!isEmpty(_contents)) {
				boolean isHeader = true;
				int idx_checkSum = -1;
				int idx_tlsh = -1;
				int idx_binaryName = -1;
				for(String line : _contents.split(System.lineSeparator())) {
					if(isHeader) {
						String[] hdata = line.split("\t", -1);
						// header 정보가 없을 경우 처리 중단
						if(hdata == null || isEmpty(line)) {
							log.warn("Header row is empty :" + binaryTextFile.getLogiNm());
							break;
						}
						
						int idx = 0;
						for(String s : hdata) {
							if(idx_binaryName < 0 && (s.trim().equalsIgnoreCase("Binary Name") || s.trim().equalsIgnoreCase("Binary"))) {
								idx_binaryName = idx;
							} else if(idx_checkSum < 0 && (s.trim().equalsIgnoreCase("checksum") || s.trim().equalsIgnoreCase("sha1sum"))) {
								idx_checkSum = idx;
							} else if(idx_tlsh < 0 && (s.trim().equalsIgnoreCase("tlsh"))) {
								idx_tlsh = idx;
							}
							idx ++;
						}
						isHeader = false;
						continue;
					}
					
					String binaryName, checkSum, tlsh;
					String[] data = line.split("\t", -1);
					
					if(idx_binaryName > -1 && idx_checkSum > -1 && idx_tlsh > -1) {
						if(data == null) {
							log.warn("unexpected format Bin binary text : " + line);
							continue;
						} else if(data.length < idx_binaryName + 1) {
							log.warn("unexpected format Bin binary text (idx_binaryName index): " + line); continue;
						} else if(data.length < idx_checkSum + 1) {
							log.warn("unexpected format Bin binary text (idx_checkSum index): " + line); continue;
						} else if(data.length < idx_tlsh + 1) {
							log.warn("unexpected format Bin binary text (idx_tlsh index): " + line); continue;
						}
						binaryName = data[idx_binaryName].trim();
						checkSum = data[idx_checkSum].trim();
						tlsh = data[idx_tlsh].trim();
						
						// OSC-1028 tils가 추출되지 않은 경우 공백
						if(isEmpty(binaryName) || isEmpty(checkSum)/* || isEmpty(tlsh)*/) {
							log.warn("unexpected format Bin binary text : " + line);
							continue;
						}
					} else {
						// binary name을 key로 sha256sum과 tlsh 를 격납한다.
						if(data == null || data.length !=3
								|| isEmpty(data[0]) || isEmpty(data[1])/* || isEmpty(data[2])*/) {
							log.warn("unexpected format Bin binary text : " + line);
							continue;
						}
						
						binaryName = data[0].trim();
						if(binaryName.endsWith("/") || binaryName.endsWith("\\")) {
							log.warn("unexpected format Bin binary text (is not file) : " + line);
							continue;
						}
						
						checkSum = data[1].trim();
						tlsh = data[2].trim();
					}

					if(!pathFlag){
						if(binaryName.indexOf("/") > -1) {
							binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
						}
						if(binaryName.indexOf("\\") > -1) {
							binaryName = binaryName.substring(binaryName.lastIndexOf("\\") + 1);
						}
					}
					if(isEmpty(binaryName)) {
						continue;
					}
					
					loadData.put(binaryName, new String[]{checkSum, avoidNull(tlsh, "0")});
				}					
			}
		}
		return loadData;
	}
	
	@Transactional
	private void addOssComponentByBinaryInfo(List<OssComponents> componentList,
			Map<String, List<BinaryData>> binaryRegInfoMap) {

		for(OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			String componentId = bean.getComponentId();
			if(isEmpty(binaryName)) {
				continue;
			}
			
			if(!binaryRegInfoMap.containsKey(binaryName)) {
				continue;
			}
			
			List<BinaryData> binaryInfoList = binaryRegInfoMap.get(binaryName);
			
			boolean addOssComponentFlag = false;
			for(BinaryData binaryInfo : binaryInfoList) {

				if(!isEmpty(binaryInfo.getOssName())) {
					Map<String, OssMaster> ossInfo = CoCodeManager.OSS_INFO_UPPER;
					
					String key = binaryInfo.getOssName() + "_" + avoidNull(binaryInfo.getOssVersion());
					String _binaryLicenseStr = binaryInfo.getLicense();
					
					// binary db에 하이픈으로 등록된 case로 고려해야함
					//if(ossInfo.containsKey(key.toUpperCase()) || "-".equals(binaryInfo.getOssName()) ) {
						// oss name + version 이 일치하는 oss 가 존재하면, update 한다.

						OssMaster ossBean = ossInfo.get(key.toUpperCase());
						boolean isEmptyOss = (ossBean == null || "-".equals(binaryInfo.getOssName()));
						
						// update를 위해
						// ossmaster => projectIdentification 으로 변한
						ProjectIdentification updateBean = new ProjectIdentification();
						if(ossBean != null) {
							if(!isEmptyOss) {
								updateBean.setOssId(ossBean.getOssId());
							}
							updateBean.setOssName(isEmptyOss ? "-" : ossBean.getOssNameTemp()); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
							updateBean.setOssVersion(isEmptyOss ? avoidNull(binaryInfo.getOssVersion()) : ossBean.getOssVersion());
							
							updateBean.setDownloadLocation(isEmptyOss ? "" : ossBean.getDownloadLocation());
							updateBean.setHomepage(isEmptyOss ? "" : ossBean.getHomepage());
							updateBean.setCopyrightText(isEmptyOss ? "" : ossBean.getCopyright());
						} else {
							updateBean.setOssId(null);
							updateBean.setOssName(binaryInfo.getOssName()); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
							updateBean.setOssVersion(avoidNull(binaryInfo.getOssVersion()));
							updateBean.setDownloadLocation(null);
							updateBean.setHomepage(null);
							updateBean.setCopyrightText(null);
						}
						// 기존값 유지
						updateBean.setFilePath(bean.getFilePath()); 
						updateBean.setExcludeYn(bean.getExcludeYn());
						updateBean.setBinaryName(bean.getBinaryName());
						updateBean.setBinaryNotice(bean.getBinaryNotice());
						updateBean.setCustomBinaryYn(bean.getCustomBinaryYn());
						
						// 하나의 binary에 대해서 여러개의 OSS가 적용된 경우, 최초 한번만 업데이트하고 이후부터는 신규 등록한다.
						if(addOssComponentFlag) {
							updateBean.setReferenceId(bean.getReferenceId());
							updateBean.setReferenceDiv(bean.getReferenceDiv());
							projectMapper.insertOssComponents(updateBean);
							componentId = updateBean.getComponentId();
						} else {
							updateBean.setComponentId(componentId);
							projectMapper.updateSrcOssList(updateBean);
							addOssComponentFlag = true;
							// 기존 license 삭제
							projectMapper.deleteOssComponentsLicense(bean);
						}
						
						List<String> selectedLicenseIdList = new ArrayList<>();
						if(!isEmpty(_binaryLicenseStr)) {
							for(String _licenseName : _binaryLicenseStr.split(",")) {
								if(isEmpty(_licenseName)) {
									continue;
								}
								_licenseName = _licenseName.trim();
								String _licenseId = CommonFunction.getLicenseIdByName(_licenseName);
								if(!isEmpty(_licenseId)) {
									selectedLicenseIdList.add(_licenseId);
								}
							}
						}
						List<OssComponentsLicense> updateLicenseList = new ArrayList<>();
						//List<ProjectIdentification> updateLicenseDefaultList = new ArrayList<>();
						
						// oss name이 하이픈이 아니라면, OSS List에 등록된 정보를 기준으로 취합
						if(!isEmptyOss) {
							boolean hasSelectedLicense = false;
							for(OssLicense license : ossBean.getOssLicenses()) {
								OssComponentsLicense componentLicense = new OssComponentsLicense();
								
								componentLicense.setComponentId(componentId);
								componentLicense.setLicenseId(license.getLicenseId());
								componentLicense.setLicenseName(license.getLicenseName());
								// license text 설정은 불필요함
								
								if(selectedLicenseIdList.contains(componentLicense.getLicenseId())) {
									hasSelectedLicense = true;
									componentLicense.setExcludeYn(CoConstDef.FLAG_NO);
								}
								updateLicenseList.add(componentLicense);
							}
							
							for(OssComponentsLicense license : updateLicenseList) {
								if(hasSelectedLicense) {
									license.setExcludeYn(avoidNull(license.getExcludeYn(), CoConstDef.FLAG_YES));
								} else {
									license.setExcludeYn(CoConstDef.FLAG_NO);
								}
								projectMapper.insertOssComponentsLicense(license);
							}
						}
						// oss name이 하이픈이라면 라이선스 binary db에 등록된 license 정보를 그대로 등록한다.
						// 이러한 경우는 license가 복수개로 등록되어 있을 수 없음
						else {
							OssComponentsLicense license = new OssComponentsLicense();
							license.setExcludeYn(CoConstDef.FLAG_NO);
							license.setComponentId(componentId);
							// binary db에 등록된 license 정보가 license master에 등록되어 있다면 master 정보를 사용
							if(!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
								LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_binaryLicenseStr.toUpperCase().trim());
								license.setLicenseId(licenseMaster.getLicenseId());
								license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName()));
							}
							// 등록되어 있지 않다면, license name만 등록한다.
							// 이러한 경우는 사실항 존재할 수 없음
							else {
								license.setLicenseName(avoidNull(_binaryLicenseStr));
							}
							projectMapper.insertOssComponentsLicense(license);
						}
					//}
				}
			}
			
		}
	}

	

	@Transactional
	private void addOssComponentByBinaryInfoPartner(List<OssComponents> componentList,
			Map<String, List<BinaryData>> binaryRegInfoMap) {

		for(OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			String componentId = bean.getComponentId();
			if(isEmpty(binaryName)) {
				continue;
			}
			
			if(!binaryRegInfoMap.containsKey(binaryName)) {
				continue;
			}
			
			List<BinaryData> binaryInfoList = binaryRegInfoMap.get(binaryName);
			
			boolean addOssComponentFlag = false;
			for(BinaryData binaryInfo : binaryInfoList) {

				if(!isEmpty(binaryInfo.getOssName())) {
					Map<String, OssMaster> ossInfo = CoCodeManager.OSS_INFO_UPPER;
					
					String key = binaryInfo.getOssName() + "_" + avoidNull(binaryInfo.getOssVersion());
					String _binaryLicenseStr = binaryInfo.getLicense();
					
					// binary db에 하이픈으로 등록된 case로 고려해야함
					//if(ossInfo.containsKey(key.toUpperCase()) || "-".equals(binaryInfo.getOssName()) ) {
						// oss name + version 이 일치하는 oss 가 존재하면, update 한다.

						OssMaster ossBean = ossInfo.get(key.toUpperCase());
						boolean isEmptyOss = (ossBean == null || "-".equals(binaryInfo.getOssName()));
						
						// update를 위해
						// ossmaster => projectIdentification 으로 변한
						ProjectIdentification updateBean = new ProjectIdentification();
						if(ossBean != null) {
							if(!isEmptyOss) {
								updateBean.setOssId(ossBean.getOssId());
							}
							updateBean.setOssName(isEmptyOss ? "-" : ossBean.getOssNameTemp()); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
							updateBean.setOssVersion(isEmptyOss ? avoidNull(binaryInfo.getOssVersion()) : ossBean.getOssVersion());
							
							updateBean.setDownloadLocation(isEmptyOss ? "" : ossBean.getDownloadLocation());
							updateBean.setHomepage(isEmptyOss ? "" : ossBean.getHomepage());
							updateBean.setCopyrightText(isEmptyOss ? "" : ossBean.getCopyright());
						} else {
							updateBean.setOssId(null);
							updateBean.setOssName(binaryInfo.getOssName()); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
							updateBean.setOssVersion(avoidNull(binaryInfo.getOssVersion()));
							updateBean.setDownloadLocation(null);
							updateBean.setHomepage(null);
							updateBean.setCopyrightText(null);
						}
						// 기존값 유지
						updateBean.setFilePath(bean.getFilePath()); 
						updateBean.setExcludeYn(bean.getExcludeYn());
						updateBean.setBinaryName(bean.getBinaryName());
						updateBean.setBinaryNotice(bean.getBinaryNotice());
						updateBean.setCustomBinaryYn(bean.getCustomBinaryYn());
						
						// 하나의 binary에 대해서 여러개의 OSS가 적용된 경우, 최초 한번만 업데이트하고 이후부터는 신규 등록한다.
						if(addOssComponentFlag) {
							updateBean.setReferenceId(bean.getReferenceId());
							updateBean.setReferenceDiv(bean.getReferenceDiv());
							partnerMapper.insertBinaryOssComponents(updateBean);
							componentId = updateBean.getComponentId();
						} else {
							updateBean.setComponentId(componentId);
							partnerMapper.updateOssList(updateBean);
							addOssComponentFlag = true;
							// 기존 license 삭제
							partnerMapper.deleteOssComponentsLicense(bean);
						}
						
						List<String> selectedLicenseIdList = new ArrayList<>();
						if(!isEmpty(_binaryLicenseStr)) {
							for(String _licenseName : _binaryLicenseStr.split(",")) {
								if(isEmpty(_licenseName)) {
									continue;
								}
								_licenseName = _licenseName.trim();
								String _licenseId = CommonFunction.getLicenseIdByName(_licenseName);
								if(!isEmpty(_licenseId)) {
									selectedLicenseIdList.add(_licenseId);
								}
							}
						}
						List<OssComponentsLicense> updateLicenseList = new ArrayList<>();
						//List<ProjectIdentification> updateLicenseDefaultList = new ArrayList<>();
						
						// oss name이 하이픈이 아니라면, OSS List에 등록된 정보를 기준으로 취합
						if(!isEmptyOss) {
							boolean hasSelectedLicense = false;
							for(OssLicense license : ossBean.getOssLicenses()) {
								OssComponentsLicense componentLicense = new OssComponentsLicense();
								
								componentLicense.setComponentId(componentId);
								componentLicense.setLicenseId(license.getLicenseId());
								componentLicense.setLicenseName(license.getLicenseName());
								// license text 설정은 불필요함
								
								if(selectedLicenseIdList.contains(componentLicense.getLicenseId())) {
									hasSelectedLicense = true;
									componentLicense.setExcludeYn(CoConstDef.FLAG_NO);
								}
								updateLicenseList.add(componentLicense);
							}
							
							for(OssComponentsLicense license : updateLicenseList) {
								if(hasSelectedLicense) {
									license.setExcludeYn(avoidNull(license.getExcludeYn(), CoConstDef.FLAG_YES));
								} else {
									license.setExcludeYn(CoConstDef.FLAG_NO);
								}
								partnerMapper.insertOssComponentsLicense(license);
							}
						}
						// oss name이 하이픈이라면 라이선스 binary db에 등록된 license 정보를 그대로 등록한다.
						// 이러한 경우는 license가 복수개로 등록되어 있을 수 없음
						else {
							OssComponentsLicense license = new OssComponentsLicense();
							license.setExcludeYn(CoConstDef.FLAG_NO);
							license.setComponentId(componentId);
							// binary db에 등록된 license 정보가 license master에 등록되어 있다면 master 정보를 사용
							if(!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
								LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_binaryLicenseStr.toUpperCase().trim());
								license.setLicenseId(licenseMaster.getLicenseId());
								license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName()));
							}
							// 등록되어 있지 않다면, license name만 등록한다.
							// 이러한 경우는 사실항 존재할 수 없음
							else {
								license.setLicenseName(avoidNull(_binaryLicenseStr));
							}
							partnerMapper.insertOssComponentsLicense(license);
						}
					//}
				}
			}
			
		}
	}
	
	@Transactional
	public void autoIdentificationWithAndroidResultTextFile(Project project) {
		if(!isEmpty(project.getSrcAndroidResultFileId())) {
			// oss name과 license가 설정되지 않은 oss coponent를 찾는다.
			// component_id와 binary name만 반환한다.
			//List<OssComponents> componentList = projectMapper.findBinAutoIdentificationWithBinaryText(project.getPrjId());
			List<OssComponents> componentList = projectMapper.findBinAutoIdentificationWithResultText(project.getPrjId());
			
			if(componentList != null && !componentList.isEmpty()) {
				// 미설정 대상이 있는 경우 업로드 파일에서 동일한 binary를 찾는다.
				
				// DB에서 binary로 매핑되는 정보를 취득한다.
				//Map<String, String[]> mappingData = loadBinaryText(project.getBinBinaryFileId());
				T2File binaryTextFile = fileService.selectFileInfoById(project.getSrcAndroidResultFileId());
				if(binaryTextFile != null) {
					Map<String, Object> readFileData = CommonFunction.getAndroidResultFileInfo(binaryTextFile, new ArrayList<String>());
					if(readFileData != null && readFileData.containsKey("addCheckList")) {
						
						@SuppressWarnings("unchecked")
						List<OssComponents> binaryList = (List<OssComponents>) readFileData.get("addCheckList");

						Map<String, String[]> mappingData = new HashMap<>(); // 1:checksum, 2:tlsh
						if(binaryList != null && !binaryList.isEmpty()) {
							for(OssComponents _tmpBean : binaryList) {
								if(!isEmpty(_tmpBean.getCheckSum()) && !isEmpty(_tmpBean.getBinaryName()) && !isEmpty(_tmpBean.getTlsh())) {
									String _binaryName = _tmpBean.getBinaryName();
									if(_binaryName.indexOf("/") > -1) {
										_binaryName = _binaryName.substring(_binaryName.lastIndexOf("/") + 1);
									}
									if(isEmpty(_binaryName)) {
										continue;
									}
									
									mappingData.put(_binaryName, new String[]{_tmpBean.getCheckSum(), _tmpBean.getTlsh()});
								}
							}
						}
						
						if(!mappingData.isEmpty()) {
							Map<String, List<BinaryData>> binaryRegInfoMap = new HashMap<>();
							
							for(OssComponents bean : componentList) {
								String binaryName = avoidNull(bean.getBinaryName());
								if(binaryName.indexOf("/") > -1) {
									binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
								}
								if(isEmpty(binaryName)) {
									continue;
								}
								
								// 사용자가 입력한 oss가 있으면 설정하지 않음
								if(!isEmpty(bean.getOssName())) {
									continue;
								}
								
								// 업로드한 binary파일과 grid에 등록한 binaryname이 동일한 data인 경우, bat DB에서 oss 정보를 찾는다.
								if(mappingData.containsKey(binaryName)) {
									String[] tlshData = mappingData.get(binaryName);
									List<BinaryData> binaryInfoList = autoIDService.findOssInfoWithBinaryName(binaryName, tlshData[0], tlshData[1]);
									
									if(binaryInfoList != null) {
										binaryRegInfoMap.put(binaryName, binaryInfoList);
									}
								}

							}

							// 2019.04.03 ryan.yun binary 정보를 찾는 시간이 오래걸릴 수 있기 때문에 틑랜젝션 이슈를 최소화 하기 위해 일단 binary 정보를 찾는 처리만 먼저 수행하고 트랜젝션을 분리하여 등록 한다.
							
							addOssComponentByBinaryInfoAndroid(componentList, binaryRegInfoMap);
							
						}
					}
				}
			}
		}
	}
	
	@Transactional
	private void addOssComponentByBinaryInfoAndroid(List<OssComponents> componentList, Map<String, List<BinaryData>> binaryRegInfoMap) {

		for(OssComponents bean : componentList) {
			String binaryName = avoidNull(bean.getBinaryName());
			if(binaryName.indexOf("/") > -1) {
				binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
			}
			if(isEmpty(binaryName)) {
				continue;
			}
			
			// 사용자가 입력한 oss가 있으면 설정하지 않음
			if(!isEmpty(bean.getOssName())) {
				continue;
			}
			
			if(!binaryRegInfoMap.containsKey(binaryName)) {
				continue;
			}
			
			List<BinaryData> binaryInfoList = binaryRegInfoMap.get(binaryName);
			
			boolean addOssComponentFlag = false;
			for(BinaryData binaryInfo : binaryInfoList) {
				if(!isEmpty(binaryInfo.getOssName())) {
					Map<String, OssMaster> ossInfo = CoCodeManager.OSS_INFO_UPPER;
					
					String key = binaryInfo.getOssName() + "_" + avoidNull(binaryInfo.getOssVersion());
					String _binaryLicenseStr = binaryInfo.getLicense();
					if("-".equals(binaryInfo.getOssName()) || ossInfo.containsKey(key.toUpperCase())) {
						// oss name + version 이 일치하는 oss 가 존재하면, update 한다.
						boolean isEmptyOss = "-".equals(binaryInfo.getOssName());
						OssMaster ossBean = ossInfo.get(key.toUpperCase());
						
						// update를 위해
						// ossmaster => projectIdentification 으로 변한
						ProjectIdentification updateBean = new ProjectIdentification();
						if(!isEmptyOss) {
							updateBean.setOssId(ossBean.getOssId());
						}
						updateBean.setOssName(isEmptyOss ? "-" : ossBean.getOssNameTemp()); // nick name으로 일치하는 경우도 있기때문에, 원본이름을 설정(temp)
						updateBean.setOssVersion(isEmptyOss ? avoidNull(binaryInfo.getOssVersion()) : ossBean.getOssVersion());
						
						updateBean.setDownloadLocation(isEmptyOss ? "" : ossBean.getDownloadLocation());
						updateBean.setHomepage(isEmptyOss ? "" : ossBean.getHomepage());
						updateBean.setCopyrightText(isEmptyOss ? "" : ossBean.getCopyright());
						
						// 기존값 유지
						updateBean.setFilePath(bean.getFilePath()); 
						updateBean.setExcludeYn(bean.getExcludeYn());
						updateBean.setBinaryName(bean.getBinaryName());
						updateBean.setBinaryNotice(bean.getBinaryNotice());
						updateBean.setCustomBinaryYn(bean.getCustomBinaryYn());
						
						// 하나의 binary에 대해서 여러개의 OSS가 적용된 경우, 최초 한번만 업데이트하고 이후부터는 신규 등록한다.
						if(addOssComponentFlag) {
							updateBean.setReferenceId(bean.getReferenceId());
							updateBean.setReferenceDiv(bean.getReferenceDiv());
							projectMapper.insertOssComponents(updateBean);
						} else {
							updateBean.setComponentId(bean.getComponentId());
							projectMapper.updateSrcOssList(updateBean);
							addOssComponentFlag = true;
							// 기존 license 삭제
							projectMapper.deleteOssComponentsLicense(bean);
						}
						

						List<String> selectedLicenseIdList = new ArrayList<>();
						if(!isEmpty(_binaryLicenseStr)) {
							for(String _licenseName : _binaryLicenseStr.split(",")) {
								if(isEmpty(_licenseName)) {
									continue;
								}
								_licenseName = _licenseName.trim();
								String _licenseId = CommonFunction.getLicenseIdByName(_licenseName);
								if(!isEmpty(_licenseId)) {
									selectedLicenseIdList.add(_licenseId);
								}
							}
						}
						List<OssComponentsLicense> updateLicenseList = new ArrayList<>();
						//List<ProjectIdentification> updateLicenseDefaultList = new ArrayList<>();
						
						// oss name이 하이픈이 아니라면, OSS List에 등록된 정보를 기준으로 취합
						if(!isEmptyOss) {
							boolean hasSelectedLicense = false;
							for(OssLicense license : ossBean.getOssLicenses()) {
								OssComponentsLicense componentLicense = new OssComponentsLicense();
								
								componentLicense.setComponentId(bean.getComponentId());
								componentLicense.setLicenseId(license.getLicenseId());
								componentLicense.setLicenseName(license.getLicenseName());
								// license text 설정은 불필요함
								
								if(selectedLicenseIdList.contains(componentLicense.getLicenseId())) {
									hasSelectedLicense = true;
									componentLicense.setExcludeYn(CoConstDef.FLAG_NO);
								}
								updateLicenseList.add(componentLicense);
							}
							
							for(OssComponentsLicense license : updateLicenseList) {
								if(hasSelectedLicense) {
									license.setExcludeYn(avoidNull(license.getExcludeYn(), CoConstDef.FLAG_YES));
								} else {
									license.setExcludeYn(CoConstDef.FLAG_NO);
								}
								projectMapper.insertOssComponentsLicense(license);
							}
						}
						// oss name이 하이픈이라면 라이선스 binary db에 등록된 license 정보를 그대로 등록한다.
						// 이러한 경우는 license가 복수개로 등록되어 있을 수 없음
						else {
							OssComponentsLicense license = new OssComponentsLicense();
							license.setExcludeYn(CoConstDef.FLAG_NO);
							license.setComponentId(addOssComponentFlag ? updateBean.getComponentId() : bean.getComponentId());
							// binary db에 등록된 license 정보가 license master에 등록되어 있다면 master 정보를 사용
							if(!isEmpty(_binaryLicenseStr) && CoCodeManager.LICENSE_INFO_UPPER.containsKey(_binaryLicenseStr.toUpperCase().trim())) {
								LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(_binaryLicenseStr.toUpperCase().trim());
								license.setLicenseId(licenseMaster.getLicenseId());
								license.setLicenseName(avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName()));
							}
							// 등록되어 있지 않다면, license name만 등록한다.
							// 이러한 경우는 사실항 존재할 수 없음
							else {
								license.setLicenseName(avoidNull(_binaryLicenseStr));
							}
							projectMapper.insertOssComponentsLicense(license);
						}				
					}
				}
			}
		}
	}

	public void autoIdentificationWithBinryTextFilePartner(PartnerMaster partner) {

		// oss name과 license가 설정되지 않은 oss coponent를 찾는다.
		// component_id와 binary name만 반환한다.
		List<OssComponents> componentList = partnerMapper.findBinAutoIdentificationWithBinaryText(partner.getPartnerId());
		
		if(componentList != null && !componentList.isEmpty()) {
			// 미설정 대상이 있는 경우 업로드 파일에서 동일한 binary를 찾는다.
			
			
			// DB에서 binary로 매핑되는 정보를 취득한다.
			Map<String, String[]> mappingData = loadBinaryText(fileService.selectFileInfo(partner.getBinaryFileId()).getFileId(), true);
			
			if(mappingData != null && !mappingData.isEmpty()) {
				
				Map<String, List<BinaryData>> binaryRegInfoMap = new HashMap<>();
				
				for(OssComponents bean : componentList) {
					String binaryName = avoidNull(bean.getBinaryName());
					/*
					if(binaryName.indexOf("/") > -1) {
						binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
					}
					*/
					if(isEmpty(binaryName)) {
						continue;
					}
					
					// 업로드한 binary파일과 grid에 등록한 binaryname이 동일한 data인 경우, bat DB에서 oss 정보를 찾는다.
					String checkBinaryName = "";
					String[] tlshData = null;
					if(mappingData.containsKey(binaryName)) {
						tlshData = mappingData.get(binaryName);
						checkBinaryName = binaryName;
						
						if(checkBinaryName.indexOf("/") > -1) {
							checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("/") + 1);
						}
						
						if(checkBinaryName.indexOf("\\") > -1) {
							checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("\\") + 1);
						}
					}
					
					if(isEmpty(checkBinaryName) && ArrayUtils.isEmpty(tlshData)){
						checkBinaryName = binaryName;
						
						if(checkBinaryName.indexOf("/") > -1) {
							checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("/") + 1);
						}
						
						if(checkBinaryName.indexOf("\\") > -1) {
							checkBinaryName = checkBinaryName.substring(checkBinaryName.lastIndexOf("\\") + 1);
						}
						
						if(mappingData.containsKey(checkBinaryName)) {
							tlshData = mappingData.get(checkBinaryName);
						}
					}
					
					if(!isEmpty(checkBinaryName) && !ArrayUtils.isEmpty(tlshData)){
						List<BinaryData> binaryInfoList = autoIDService.findOssInfoWithBinaryName(checkBinaryName, tlshData[0], tlshData[1]);
						if(binaryInfoList != null) {
							binaryRegInfoMap.put(binaryName, binaryInfoList);
						}
					}		
				}
				// binary 정보를 찾는 시간이 오래걸릴 수 있기 때문에 틑랜젝션 이슈를 최소화 하기 위해 일단 binary 정보를 찾는 처리만 먼저 수행하고 트랜젝션을 분리하여 등록 한다.
				addOssComponentByBinaryInfoPartner(componentList, binaryRegInfoMap);
			}
		}
			
	}

	public Map<String, List<BinaryData>> getBinaryListFromBinaryDB(boolean isAndroid, Project projectInfo, Map<String, String[]> checkSumInfoMap) {
		Map<String, List<BinaryData>> listMap = new HashMap<>();
		
		if(projectInfo != null ) {
			if(isAndroid) {
				if ((checkSumInfoMap == null || checkSumInfoMap.isEmpty()) && !isEmpty(projectInfo.getSrcAndroidResultFileId())) {

					T2File binaryTextFile = fileService.selectFileInfoById(projectInfo.getSrcAndroidResultFileId());
					
					if(binaryTextFile != null) {
						Map<String, Object> readFileData = CommonFunction.getAndroidResultFileInfo(binaryTextFile, new ArrayList<String>());
						
						if(readFileData != null && readFileData.containsKey("addCheckList")) {
							List<OssComponents> _loadlist = (List<OssComponents>) readFileData.get("addCheckList");
							
							for(OssComponents bean : _loadlist) {
								// binary name을 key로 sha256sum과 tlsh 를 격납한다.
								if(isEmpty(bean.getBinaryName()) || isEmpty(bean.getCheckSum()) || isEmpty(bean.getTlsh())) {
									continue;
								}
								
								String binaryName = bean.getBinaryName();
								
								if(binaryName.endsWith("/") || binaryName.endsWith("\\")) {
									continue;
								}
								
								if(isEmpty(binaryName)) {
									continue;
								}

								checkSumInfoMap.put(bean.getBinaryName(), new String[]{bean.getCheckSum(), bean.getTlsh(), avoidNull(bean.getSourceCodePath())});
							}
						}
					}
				}
				
				if (checkSumInfoMap != null && !checkSumInfoMap.isEmpty()) {
					try {
						for(String binaryname : checkSumInfoMap.keySet()) {
							String[] datas = checkSumInfoMap.get(binaryname);
							
							List<BinaryData> _list = autoIDService.findOssInfoWithBinaryName(binaryname, datas[0], datas[1]);
						
							if(listMap.containsKey(binaryname) && _list != null) {
								List<BinaryData> resultList = listMap.get(binaryname);
								if(resultList != null){
									resultList.addAll(_list);
									listMap.put(binaryname, resultList);
								}
								
							} else {
								listMap.put(binaryname, _list);
							}
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}	
				}
			} else {
				if ((checkSumInfoMap == null || checkSumInfoMap.isEmpty()) &&!isEmpty(projectInfo.getBinBinaryFileId())) {
					T2File binaryTextFile = fileService.selectFileInfoById(projectInfo.getBinBinaryFileId());
					
					checkSumInfoMap = loadBinaryText(binaryTextFile, true);
				}
				
				if (checkSumInfoMap != null && !checkSumInfoMap.isEmpty()) {
					if (checkSumInfoMap != null && !checkSumInfoMap.isEmpty()) {
						try {
							for(String binaryname : checkSumInfoMap.keySet()) {
								String[] datas = checkSumInfoMap.get(binaryname);
								
								List<BinaryData> _list = autoIDService.findOssInfoWithBinaryName(binaryname, datas[0], datas[1]);
								
								if(listMap.containsKey(binaryname) && _list != null) {
									List<BinaryData> resultList = listMap.get(binaryname);
									if(resultList != null){
										resultList.addAll(_list);
										listMap.put(binaryname, resultList);
									}
								} else {
									listMap.put(binaryname, _list);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
		}
		
		return listMap;
	}
	
	private static Map<String, String[]> loadBinaryText(T2File binaryTextFile, boolean pathFlag) {
		Map<String, String[]> loadData = new HashMap<>();
		
		if(binaryTextFile != null && binaryTextFile.getExt().equals("txt")) {
			log.info("loadBinaryText : " + binaryTextFile.getLogiNm());
			
			String _contents = avoidNull(CommonFunction.getStringFromFile(binaryTextFile.getLogiPath() + "/" + binaryTextFile.getLogiNm()));
			
			if(!isEmpty(_contents)) {
				boolean isHeader = true;
				int idx_checkSum = -1;
				int idx_tlsh = -1;
				int idx_binaryName = -1;
				
				for(String line : _contents.split(System.lineSeparator())) {
					if(isHeader) {
						String[] hdata = line.split("\t", -1);
						
						// header 정보가 없을 경우 처리 중단
						if(hdata == null || isEmpty(line)) {
							log.warn("Header row is empty :" + binaryTextFile.getLogiNm());
							
							break;
						}
						
						int idx = 0;
						
						for(String s : hdata) {
							if(idx_binaryName < 0 && (s.trim().equalsIgnoreCase("Binary Name") || s.trim().equalsIgnoreCase("Binary"))) {
								idx_binaryName = idx;
							} else if(idx_checkSum < 0 && (s.trim().equalsIgnoreCase("checksum") || s.trim().equalsIgnoreCase("sha1sum"))) {
								idx_checkSum = idx;
							} else if(idx_tlsh < 0 && (s.trim().equalsIgnoreCase("tlsh"))) {
								idx_tlsh = idx;
							}
							
							idx ++;
						}
						
						isHeader = false;
						
						continue;
					}
					
					String binaryName, checkSum, tlsh;
					String[] data = line.split("\t", -1);
					
					if(idx_binaryName > -1 && idx_checkSum > -1 && idx_tlsh > -1) {
						if(data == null) {
							log.warn("unexpected format Bin binary text : " + line);
							
							continue;
						} else if(data.length < idx_binaryName + 1) {
							log.warn("unexpected format Bin binary text (idx_binaryName index): " + line); 
							
							continue;
						} else if(data.length < idx_checkSum + 1) {
							log.warn("unexpected format Bin binary text (idx_checkSum index): " + line); 
							
							continue;
						} else if(data.length < idx_tlsh + 1) {
							log.warn("unexpected format Bin binary text (idx_tlsh index): " + line); 
							
							continue;
						}
						
						binaryName = data[idx_binaryName].trim();
						checkSum = data[idx_checkSum].trim();
						tlsh = data[idx_tlsh].trim();
						
						// tils가 추출되지 않은 경우 공백
						if(isEmpty(binaryName) || isEmpty(checkSum)) {
							log.warn("unexpected format Bin binary text : " + line);
							
							continue;
						}
					} else {
						// binary name을 key로 sha256sum과 tlsh 를 격납한다.
						if(data == null || data.length !=3 || isEmpty(data[0]) || isEmpty(data[1])) {
							log.warn("unexpected format Bin binary text : " + line);
							
							continue;
						}
						
						binaryName = data[0].trim();
						
						if(binaryName.endsWith("/") || binaryName.endsWith("\\")) {
							log.warn("unexpected format Bin binary text (is not file) : " + line);
							
							continue;
						}
						
						checkSum = data[1].trim();
						tlsh = data[2].trim();
					}

					if(!pathFlag){
						if(binaryName.indexOf("/") > -1) {
							binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1);
						}
						
						if(binaryName.indexOf("\\") > -1) {
							binaryName = binaryName.substring(binaryName.lastIndexOf("\\") + 1);
						}
					}
					
					if(isEmpty(binaryName)) {
						continue;
					}
					
					loadData.put(binaryName, new String[]{checkSum, avoidNull(tlsh, "0")});
				}					
			}
		}
		
		return loadData;
	}
}
