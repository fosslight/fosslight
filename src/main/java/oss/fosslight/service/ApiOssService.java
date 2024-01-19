/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface ApiOssService {
	public List<Map<String, Object>> getOssInfo(Map<String, Object> paramMap);
	
	public List<Map<String, Object>> getOssInfoByDownloadLocation(String downloadLocation);
	
	public List<Map<String, Object>> getLicenseInfo(String licenseName);
	
	String[] getOssNickNameListByOssName(String ossName);

	public Map<String, Object> registAnalysisOss(List<String> stringResult, Map<String, String> userDataMap, String prjId, String _token) throws ExecutionException, InterruptedException;

	public void coReviewerProcess(String prjId);

	public Map<String, Object> finishOss(String prjId);
}
