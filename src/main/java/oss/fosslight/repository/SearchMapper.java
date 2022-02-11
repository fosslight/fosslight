/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SearchMapper {

    String selectProjectSearchFilter(String userId);

    String selectOssSearchFilter(String userId);

    String selectLicenseSearchFilter(String userId);

    String selectSearchFilter(@Param(value = "type") String type, @Param(value = "userId") String userId);

    void upsertSearchFilter(@Param(value = "jsonfilter") String jsonfilter, @Param(value = "userId") String userId, @Param(value = "type") String type);

	String selectPartnerSearchFilter(String userId);
	String selectSelfCheckSearchFilter(String userId);

}
