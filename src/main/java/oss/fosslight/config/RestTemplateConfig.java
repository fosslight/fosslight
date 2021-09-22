/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Autowired Environment env;

	@Bean
	public HttpClient httpClient() {
		return HttpClientBuilder.create()
				.setMaxConnTotal(env.getProperty("rest.template.max-connection-total", Integer.class))
				.setMaxConnPerRoute(env.getProperty("rest.template.connection-per-route", Integer.class))
				.build();
	}

	@Bean
	public HttpComponentsClientHttpRequestFactory httpRequestFactory(HttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectTimeout(env.getProperty("rest.template.connect-timeout", Integer.class));
		httpRequestFactory.setReadTimeout(env.getProperty("rest.template.read-timeout", Integer.class));
		httpRequestFactory.setHttpClient(httpClient);
		return httpRequestFactory;
	}

	@Bean
	public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory httpRequestFactory) {
		return new RestTemplate(httpRequestFactory);
	}
}
