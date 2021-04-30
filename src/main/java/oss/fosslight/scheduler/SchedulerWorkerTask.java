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
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.MailService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.impl.VulnerabilityServiceImpl;
import oss.fosslight.service.vulnerability.NvdDataService;
import oss.fosslight.util.FileUtil;

@Slf4j
public class SchedulerWorkerTask {
	@Autowired Environment env;
	@Autowired MailService mailService;
	@Autowired OssService ossService;
	@Autowired CommentService commentService;
	@Autowired VulnerabilityServiceImpl vulnerabilityService;
	@Autowired NvdDataService nvdService;
	
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
		Path copyPath = Paths.get(internalUrlDirPath);
		
		if(!new File(internalUrlDirPath+"/"+fileNm).exists()) {
			try (InputStream is = new ClassPathResource("/template/"+fileNm).getInputStream()) {
				if(!Files.exists(copyPath)) {
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
	@Scheduled(cron="0 0 1 * * ?")
	//@Scheduled(fixedDelay=1000)
	public void nvdDataIfJob() {
		String resCd = "";
		try {
			resCd = nvdService.executeNvdDataSync();
			
			if(resCd == "00") {
				vulnerabilityService.doSyncOSSNvdInfo();
				log.info("nvdDataIfJob end");
			} else {
				log.error("executeNvdDataSync - resCd : " + resCd);
			}
		} catch (IOException ioe) {
			log.error(ioe.getMessage() + " (resCd : " + resCd + ")", ioe);
		}
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
