/*
 * Copyright (c) 2021 Suram Kim
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service;

import java.util.Map;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.Vulnerability;

public interface SearchService extends HistoryConfig {

    void saveProjectSearchFilter(Project project, String userId);

    Project getProjectSearchFilter(String userId);

    void saveLicenseSearchFilter(LicenseMaster licenseMaster, String userId);

    LicenseMaster getLicenseSearchFilter(String userId);

    void saveOssSearchFilter(OssMaster ossMaster, String userId);

    OssMaster getOssSearchFilter(String userId);

    void saveSelfCheckSearchFilter(Project project, String userId);

    Project getSelfCheckSearchFilter(String userId);

    void savePartnerSearchFilter(PartnerMaster partnerMaster, String userId);

    void saveVulnerabilitySearchFilter(Vulnerability vulnerability, String userId);

    PartnerMaster getPartnerSearchFilter(String userId);

    Object getSearchFilter(String type, String userId);

	void saveSearchFilter(Map<String, Object> params, String userId);

}
