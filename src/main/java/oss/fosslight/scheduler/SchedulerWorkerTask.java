/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.scheduler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.MailService;
import oss.fosslight.service.NvdDataService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.impl.VulnerabilityServiceImpl;
import oss.fosslight.util.FileUtil;

import oss.fosslight.repository.ProjectMapper;

@Component
public class SchedulerWorkerTask extends CoTopComponent {
	final static Logger log = LoggerFactory.getLogger("SCHEDULER_LOG");
	
	@Autowired Environment env;
	@Autowired MailService mailService;
	@Autowired OssService ossService;
	@Autowired CommentService commentService;
	@Autowired VulnerabilityServiceImpl vulnerabilityService;
	@Autowired NvdDataService nvdService;
	@Autowired ProjectService projectService;
	@Autowired ProjectMapper projectMapper;
	boolean serverLoadFlag = false; 
	boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
	
	@PostConstruct
	public void init() {
		serverLoadFlag = true;
		makeInternalLicense();
	}
	
	public void makeInternalLicense() {
		String internalUrlDirPath = CommonFunction.appendProperty("root.dir", "internal.url.dir.path");
		String fileNm = "internalLicense.zip";
		Path copyPath = Path.of(internalUrlDirPath);
		
		if (!new File(internalUrlDirPath+"/"+fileNm).exists()) {
			try (InputStream is = new ClassPathResource("/template/"+fileNm).getInputStream()) {
				if (!Files.exists(copyPath)) {
					Files.createDirectories(copyPath);
				}
				
			    Files.copy(is, copyPath.resolve(fileNm));
			    FileUtil.decompress(internalUrlDirPath+"/"+fileNm, internalUrlDirPath);
			} catch (IOException ioe) {
				log.debug(ioe.getMessage());
			} catch (Throwable e) {
				log.debug(e.getMessage());
			}
		}
	}
	
	// 새벽 12시 스케줄 - CPE Dictionary, CVE Update Data Sync 
	@Scheduled(cron="${nvd.scheduled.cron.value}")
//	@Scheduled(fixedDelay=1000)
	public void nvdDataIfJob() {
		log.info("nvdDataIfJob start");
		
		String resCd = "";
		try {
			resCd = nvdService.executeNvdDataSync();
			
			if (resCd == "00") {
				vulnerabilityService.doSyncOSSNvdInfo();
			} else {
				log.error("executeNvdDataSync - resCd : " + resCd);
			}
		} catch (IOException ioe) {
			log.error(ioe.getMessage() + " (resCd : " + resCd + ")", ioe);
		}
		
		log.info("nvdDataIfJob end");
		
//		List<String> prjIdList = projectMapper.selectProjectForSecurity();
//		if (prjIdList != null && !prjIdList.isEmpty()) {
//			log.info("security data update start");
//			for (String prjId : prjIdList) {
//				projectService.updateSecurityDataForProject(prjId);
//			}
//			log.info("security data update end");
//		}
	}
	
	// 0분 부터 5분 단위 스케줄 - 30분이 지난 메일은 삭제한다.
	//@Scheduled(cron="0 5,10,15,20,25,30,35,40,45,50,55 * * * *")
	public void sendMailRunTimeout(){
		mailService.sendMailRunTimeout();
	}
	
	// 0분 부터 5분 단위 스케줄 - 30분이 지난 메일은 삭제한다.
	//@Scheduled(cron="0 5,10,15,20,25,30,35,40,45,50,55 * * * *")
	public void sendTempMail() {
		mailService.sendTempMail();
	}
}
