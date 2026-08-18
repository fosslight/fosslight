/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.service.*;

@Component
@Slf4j
public class EnterpriseSchedulerWorkerTask extends CoTopComponent {
	@Autowired EnterpriseIntegrationService enterpriseIntegrationService;
	
	@Scheduled(cron="0 30 22 * * ?")
	public void syncOssAndLicenseToEnterprise() {
		log.info("syncOssAndLicenseToEnterprise start");
		
		try {
			enterpriseIntegrationService.syncLicenseToEnterpriseInterface();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			enterpriseIntegrationService.syncOssToEnterpriseInterface();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		log.info("syncOssAndLicenseToEnterprise end");
	}
}
