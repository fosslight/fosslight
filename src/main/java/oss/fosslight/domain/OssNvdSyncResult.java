/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class OssNvdSyncResult extends ComBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Set<String> prjMailSet;
	private Set<String> reCalcPrjMailSet;
	private Set<String> removeReCalcPrjMailSet;
	private Set<String> notUsedOssSet;
	private Map<String, OssMaster> reCalcOssInfoMap;
	private Map<String, Map<String, OssMaster>> reCalcCommentsOssInfoMap;
	private Map<String, List<Map<String, Object>>> discoveredCommentsOssInfoMap;
	private List<Map<String, Object>> nvdInfoDiffVendorList;
	
	public OssNvdSyncResult(Set<String> prjMailSet, Set<String> reCalcPrjMailSet, Set<String> removeReCalcPrjMailSet, Set<String> notUsedOssSet, Map<String, OssMaster> reCalcOssInfoMap
					, Map<String, Map<String, OssMaster>> reCalcCommentsOssInfoMap, Map<String, List<Map<String, Object>>> discoveredCommentsOssInfoMap, List<Map<String, Object>> nvdInfoDiffVendorList) {
		this.prjMailSet = prjMailSet;
		this.reCalcPrjMailSet = reCalcPrjMailSet;
		this.removeReCalcPrjMailSet = removeReCalcPrjMailSet;
		this.notUsedOssSet = notUsedOssSet;
		this.reCalcOssInfoMap = reCalcOssInfoMap;
		this.reCalcCommentsOssInfoMap = reCalcCommentsOssInfoMap;
		this.discoveredCommentsOssInfoMap = discoveredCommentsOssInfoMap;
		this.nvdInfoDiffVendorList = nvdInfoDiffVendorList;
	}
}
