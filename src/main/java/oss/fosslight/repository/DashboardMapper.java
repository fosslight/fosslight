/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Project;

@Mapper
public interface DashboardMapper {
    List<Project> selectDashboardJobsList(Map<String, Object> param);

    int selectDashboardJobsTotalCount(Map<String, Object> param);

    List<CommentsHistory> selectDashboardCommentsList(Map<String, Object> param);

    int selectDashboardCommentsTotalCount(Map<String, Object> param);
    
    List<OssMaster> selectDashboardOssList(OssMaster ossMaster);

    int selectDashboardOssTotalCount(OssMaster ossMaster);
    
    List<LicenseMaster> selectDashboardLicenseList(LicenseMaster licenseMaster);

    int selectDashboardLicenseTotalCount(LicenseMaster licenseMaster);
    
    void readConfirmAll(CommentsHistory commentsHistory);

	List<Map<String, Object>> selectProgProjectCnt(Map<String, Object> paramMap);

	List<Map<String, Object>> getDiscoveredEmlList(Map<String, Object> paramMap);

	String getDiscoveredEmlMessage(HashMap<String, Object> param);
}
