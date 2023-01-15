/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */
package oss.fosslight.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.BinaryData;
import oss.fosslight.repository.BinaryDataMapper;
import oss.fosslight.util.TlshUtil;

@Service
public class AutoIdService {
	
	@Autowired private BinaryDataMapper binaryDataMapper;
	
	@Cacheable(value="tlshFindOssInfoCache", key="{#root.methodName, #binaryName, #checkSum, #tish}")
	public List<BinaryData> findOssInfoWithBinaryName(String binaryName, String checkSum, String tlsh) {
		
		List<BinaryData> dbDataList = binaryDataMapper.getBinaryListWithNameAndChecksum(binaryName, checkSum);
		// check sum이 동일한 정보가 존재하는 경우는 모두 반환한다.
		if(dbDataList != null && !dbDataList.isEmpty()) {
			for(BinaryData item : dbDataList) {
				item.setDownloadlocation(CommonFunction.getOssDownloadLocation(item.getOssName(), item.getOssVersion()));
			}
			return dbDataList;
		}
		
		// checksum이 동일한 binary 정보가 없는경우, distance를 비교한다.
		
		dbDataList = binaryDataMapper.selectBinaryDataListWithBinaryName(binaryName);
		
		if(!dbDataList.isEmpty()) {
			for(BinaryData item : dbDataList) {
				item.setDownloadlocation(CommonFunction.getOssDownloadLocation(item.getOssName(), item.getOssVersion()));
			}
			
			List<BinaryData> list = new ArrayList<>();
			// tlsh distance를 비교하여 근접값을 찾는다.
			BinaryData binaryData = findCloseBatOssWithTlshDistance(binaryName, tlsh, dbDataList);
			if(binaryData != null) {
				list.add(binaryData);
				return list;
			}
		}
		
		return null;
	}
	
	/**
	 * tlsh distance가 가장 가까운 binary 정보를 찾는다.
	 * @param binaryName
	 * @param tish
	 * @param dbDataList
	 * @return
	 */
	private BinaryData findCloseBatOssWithTlshDistance(String binaryName, String tlsh, List<BinaryData> dbDataList) {
		
		int currentDistance = 999999;
		BinaryData currentBinaryBean = null;
		
		for(BinaryData bean : dbDataList) {
			
			if("0".equals(tlsh)) {
				if("0".equals(bean.getTlshCheckSum())) {
					return bean;
				}
			} else {
				int _distance = TlshUtil.compareTlshDistance(tlsh, bean.getTlshCheckSum());
				if(_distance <= 120 && _distance > -1 && _distance < currentDistance) {
					currentDistance = _distance;
					
					currentBinaryBean = bean;
				}
			}
		}
		if(currentDistance <= 120) {
			return currentBinaryBean;
		}
		return null;
	}
}
