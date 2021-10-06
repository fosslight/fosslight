/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.config;

import com.google.common.net.HttpHeaders;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

	@Autowired Environment env;

	@Bean
	public WebClient getWebClient() {
		final int connectTimeOut = env.getProperty("rest.template.connect-timeout", Integer.class);
		final int readTimeOut = env.getProperty("rest.template.read-timeout", Integer.class);
		final int writeTimeOut = env.getProperty("rest.template.write-timeout", Integer.class);

		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(client ->
						client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut)
								.doOnConnected(conn -> conn
										.addHandlerLast(new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS))
										.addHandlerLast(new WriteTimeoutHandler(writeTimeOut, TimeUnit.MILLISECONDS))
								)
				);

		ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient.wiretap(true));

		return WebClient.builder()
				.clientConnector(connector)
				.defaultHeaders(httpHeaders -> {
					httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
					httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
				})
				.build();
	}
}