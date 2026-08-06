/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.OssMaster;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Download location → PURL tests against a live FOSSLight DB.
 * Loads code table 913 via {@link CoCodeManager} (same path as production).
 * Runs only when MariaDB is reachable (skipped otherwise).
 * Cases: {@code classpath:purl/download-location-purl-cases.json}
 * (all types; JSON {@code unimplementedTypes} soft-pass when actual ≠ expected).
 * <p>
 * Override JDBC with {@code spring.datasource.url|username|password}
 * system properties or {@code SPRING_DATASOURCE_*} env vars.
 */
@EnabledIf("oss.fosslight.service.PurlTestSupport#isDatabaseAvailable")
@SpringBootTest
@WithMockUser(username = "user", roles = {"USER"})
class OssServicePurlDbTest {

	@Autowired
	private OssService ossService;

	@BeforeAll
	static void logMode() {
		PurlTestSupport.clearResults();
		System.out.println("[OssServicePurlDbTest] mode=database (CoCodeManager from T2_CODE_DTL)");
	}

	@AfterAll
	static void printResultTable() {
		PurlTestSupport.printResultTable("database");
	}

	@Test
	@DisplayName("code table 913 should be loaded from DB")
	void codeTable913ShouldBeLoadedFromDb() {
		List<String> prefixes = CoCodeManager.getCodeNames(CoConstDef.CD_CHECK_OSS_DOWNLOADLOCAION_PURL);
		assertThat(prefixes)
				.as("CD_CHECK_OSS_DOWNLOADLOCAION_PURL (913) must be present in DB")
				.isNotEmpty();
		assertThat(prefixes.get(0)).isEqualTo("github.com");
	}

	@ParameterizedTest(name = "[{index}] {3}: {0} → {2}")
	@MethodSource("downloadLocationToPurlCases")
	@DisplayName("should generate purl from download location (DB)")
	void shouldGeneratePurlFromDownloadLocation(String downloadLocation, String ossName, String expectedPurl, String type) {
		OssMaster ossMaster = new OssMaster();
		ossMaster.setOssName(ossName);
		ossMaster.setDownloadLocation(downloadLocation);

		String actual = ossService.getPurlByDownloadLocation(ossMaster);
		PurlTestSupport.assertPurl(type, downloadLocation, expectedPurl, actual);
	}

	@Test
	@DisplayName("should return empty string When download location is empty (DB)")
	void shouldReturnEmptyWhenDownloadLocationIsEmpty() {
		OssMaster ossMaster = new OssMaster();
		ossMaster.setOssName("sample");
		ossMaster.setDownloadLocation("");

		assertThat(ossService.getPurlByDownloadLocation(ossMaster)).isEmpty();
	}

	static Stream<Arguments> downloadLocationToPurlCases() throws Exception {
		return PurlTestSupport.downloadLocationToPurlCases();
	}
}
