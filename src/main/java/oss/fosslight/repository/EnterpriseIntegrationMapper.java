/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import oss.fosslight.domain.EntAnalysis;
import oss.fosslight.domain.EnterpriseIntegrationBean;
import oss.fosslight.domain.ProjectIdentification;


@Mapper
public interface EnterpriseIntegrationMapper {
	void insertEnterpriseIntegrationJob(EntAnalysis entAnalysis);

	void insertEnterpriseIntegrationJobDetails(@Param("list") List<ProjectIdentification> list);

	int updateEnterpriseIntegrationJob(@Param("jobSeq") String jobSeq);

	List<EnterpriseIntegrationBean> getUpdatedEnterpriseIntegrationJobs(@Param("jobSeq") String jobSeq);
	
	List<EnterpriseIntegrationBean> getEnterpriseIntegrationJobs();

	String getEnterpriseAnalysisInfo(@Param("prjId") String prjId);

	void updateEnterpriseIntegrationJobCount(int jobSeq);

	EnterpriseIntegrationBean getEnterpriseIntegrationJob(int jobSeq);

	int getEnterpriseAnalysisCountByDivision(@Param("division") String division);

	void updateEnterpriseIntegrationJobOssId(@Param("jobSeq") int jobSeq, @Param("ossId") String ossId, @Param("ossName") String ossName, @Param("ossVersion") String ossVersion);
}