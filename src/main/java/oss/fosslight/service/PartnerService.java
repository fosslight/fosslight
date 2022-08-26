/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import oss.fosslight.config.HistoryConfig;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2Users;

public interface PartnerService extends HistoryConfig{
	public Map<String, Object> getPartnerMasterList(PartnerMaster partnerMaster);
	
	public Map<String, Object> getPartnerStatusList(PartnerMaster partnerMaster);

	public int updateReviewer(PartnerMaster vo);

	public List<PartnerMaster> getPartnerNameList(PartnerMaster partnerMaster);

	public void registPartnerMaster(PartnerMaster partnerMaster, List<ProjectIdentification> ossComponents, List<List<ProjectIdentification>> ossComponentsLicense);

	public PartnerMaster getPartnerMasterOne(PartnerMaster partnerMaster);

	public void deletePartnerMaster(PartnerMaster partnerMaster);

	public List<T2Users> getUserList(T2Users t2Users);

	public void changeStatus(PartnerMaster partnerMaster);

	public List<PartnerMaster> getPartnerSwNmList(PartnerMaster partnerMaster);

	public List<PartnerMaster> getPartnerSwVerList(PartnerMaster partnerMaster);

	public void updatePartnerConfirm(PartnerMaster partnerMaster);
	
	public List<PartnerMaster> getPartnerDuplication(PartnerMaster partnerMaster);
	
	public String checkViewOnly(String partnerId);

	public void addWatcher(PartnerMaster project);

	public void removeWatcher(PartnerMaster project);

	public List<PartnerMaster> copyWatcher(PartnerMaster project);
	
	public List<String> getInvateWatcherList(String prjId);

	public void updatePublicYn(PartnerMaster partnerMaster);
	
	public boolean existsWatcher(PartnerMaster project);
	
	public Map<String, Object> getPartnerValidationList(PartnerMaster partnerMaster);
	
	public Map<String, Object> getFilterdList(Map<String, Object> paramMap);

	public Map<String, Object> getIdentificationGridList(ProjectIdentification identification);

	public int updateDivision(String partnerId, String division);

	public Map<String, List<PartnerMaster>> updatePartnerDivision(PartnerMaster partnerMaster);

	public void updateDescription(PartnerMaster partnerMaster);
}