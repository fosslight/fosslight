/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.util.StringUtil;

@Configuration
@ComponentScan(value=AppConstBean.APP_COMPONENT_SCAN_PACKAGE)
@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
@Slf4j
public class AppConfig {
	
	@Bean(name={"mailSender"})
	public JavaMailSenderImpl mailSender(){
		JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
		String smtpUseFlag = CommonFunction.getProperty("mail.smtp.useflag");
		
		if(CoConstDef.FLAG_YES.equals(smtpUseFlag)) {
			try {
				final String	MAIL_SERVICE_HOST		= CommonFunction.getProperty("mail.smtp.host");
				final int		MAIL_SERVICE_PORT		= Integer.parseInt(StringUtil.avoidNull(CommonFunction.getProperty("mail.smtp.port"), "25"));
				final String	MAIL_SERVICE_ENCODING	= StringUtil.avoidNull(CommonFunction.getProperty("mail.smtp.encoding"), "UTF-8");
				final String	MAIL_SERVICE_USERNAME	= CommonFunction.getProperty("mail.smtp.username");
				final String	MAIL_SERVICE_PASSWORD	= CommonFunction.getProperty("mail.smtp.password");
				final boolean checkFlag = CommonFunction.propertyFlagCheck("checkFlag", CoConstDef.FLAG_YES);
				
				final Properties MAIL_SERVICE_PROP = new Properties() {
					private static final long serialVersionUID = 1L;
					{
						if(checkFlag) {
							setProperty("mail.smtp.host", MAIL_SERVICE_HOST);
							setProperty("mail.smtp.user", MAIL_SERVICE_USERNAME);
							setProperty("mail.smtp.port", String.valueOf(MAIL_SERVICE_PORT));
						} else {
							setProperty("mail.smtp.auth", "true");
							setProperty("mail.smtp.starttls.enable", "true");
							setProperty("mail.smtp.ssl.trust", MAIL_SERVICE_HOST);
						}
					}
				};
				
				mailSenderImpl.setHost(MAIL_SERVICE_HOST);
				mailSenderImpl.setPort(MAIL_SERVICE_PORT);
				mailSenderImpl.setDefaultEncoding(MAIL_SERVICE_ENCODING);
				mailSenderImpl.setJavaMailProperties(MAIL_SERVICE_PROP);
				
				if(!checkFlag) {
					mailSenderImpl.setUsername(MAIL_SERVICE_USERNAME);
					mailSenderImpl.setPassword(MAIL_SERVICE_PASSWORD);
				}
				
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		return mailSenderImpl;
	}
	
    @Bean
    public VelocityEngine getVelocityEngine() throws VelocityException, IOException {
    	VelocityEngine factory = new VelocityEngine();
        Properties props = new Properties();
        props.put("resource.loader", "class");
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.put("input.encoding", AppConstBean.APP_ENCODING);
        props.put("output.encoding", AppConstBean.APP_ENCODING);
        props.put("response.encoding", AppConstBean.APP_ENCODING);
 
        factory.init(props);
        
        return factory;
    }
}
