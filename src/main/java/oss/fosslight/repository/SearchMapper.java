/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface SearchMapper {

    String selectProjectSearchFilter(String userId);

    String selectOssSearchFilter(String userId);

    String selectLicenseSearchFilter(String userId);

    String selectSearchFilter(@Param(value = "type") String type, @Param(value = "userId") String userId);

    void upsertSearchFilter(@Param(value = "jsonfilter") String jsonfilter, @Param(value = "userId") String userId, @Param(value = "type") String type);

	String selectPartnerSearchFilter(String userId);
	String selectSelfCheckSearchFilter(String userId);
    String selectUserColumns(Map<String, Object> params);
    void insertUserColumns(Map<String, Object> params);
}
