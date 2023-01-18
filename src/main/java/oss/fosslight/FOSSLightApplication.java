/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;

@SpringBootApplication
@MapperScan(basePackages = CoConstDef.MAPPER_PACKAGE)
@Slf4j
public class FOSSLightApplication implements CommandLineRunner  {
	
	@Resource private Environment env; /** The env. */
	
	public static void main(String[] args) {
		SpringApplication.run(FOSSLightApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		boolean checkFlag = false;
		
		if (env.containsProperty("secret.key")) {
			List<String> argsList = Arrays.asList(args)
											.stream()
											.filter(c -> c.indexOf("secretKey") > -1)
											.collect(Collectors.toList());
			
			if (argsList != null && argsList.size() > 0) {
				String checkSecretKey = String.join("", argsList).split("=")[1];
				String secretKey = env.getProperty("secret.key");
				
				checkFlag = new BCryptPasswordEncoder().matches(checkSecretKey, secretKey);
			}
		}
		
		System.setProperty("checkFlag", checkFlag ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		Runtime runtime = Runtime.getRuntime();

		final NumberFormat format = NumberFormat.getInstance();

		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		final long mb = 1024 * 1024;
		final String mega = " MB";

		log.info("========================== Memory Info ==========================");
		log.info("Free memory: " + format.format(freeMemory / mb) + mega);
		log.info("Allocated memory: " + format.format(allocatedMemory / mb) + mega);
		log.info("Max memory: " + format.format(maxMemory / mb) + mega);
		log.info("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / mb) + mega);
		log.info("=================================================================\n");
	}
}
