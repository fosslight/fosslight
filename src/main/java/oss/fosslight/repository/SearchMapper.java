/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchMapper {

    String selectProjectSearchFilter(String userId);

    String selectOssSearchFilter(String userId);

    String selectLicenseSearchFilter(String userId);

    String selectSearchFilter(String type, String userId);

    void upsertSearchFilter(String jsonfilter, String userId, String type);

}
