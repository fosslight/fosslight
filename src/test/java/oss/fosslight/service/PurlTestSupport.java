/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service;

import com.google.gson.Gson;
import org.junit.jupiter.params.provider.Arguments;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Shared helpers for download-location → PURL tests.
 * Cases and unimplemented types: {@code classpath:purl/download-location-purl-cases.json}.
 */
final class PurlTestSupport {

	static final String CASES_RESOURCE = "purl/download-location-purl-cases.json";

	private static final String DEFAULT_JDBC_URL = "jdbc:mariadb://127.0.0.1:3306/fosslight";
	private static final String DEFAULT_USER = "fosslight";
	private static final String DEFAULT_PASSWORD = "fosslight";

	private static final int COL_TYPE = 12;
	private static final int COL_URL = 48;
	private static final int COL_PURL = 52;

	private static Boolean databaseAvailable;
	private static PurlCaseFile caseFile;
	private static final List<CaseResult> RESULTS = Collections.synchronizedList(new ArrayList<>());

	private PurlTestSupport() {
	}

	static void clearResults() {
		RESULTS.clear();
	}

	static boolean isUnimplementedType(String type) {
		try {
			return unimplementedTypes().contains(type);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Record result and assert. Types in JSON {@code unimplementedTypes} always pass
	 * the assertion even when actual differs from expected (gap still shown in the table).
	 */
	static void assertPurl(String type, String downloadLocation, String expectedPurl, String actualPurl) {
		boolean matches = Objects.equals(expectedPurl, actualPurl);
		boolean unimplemented = isUnimplementedType(type);
		boolean testPass = matches || unimplemented;
		RESULTS.add(new CaseResult(type, downloadLocation, expectedPurl, actualPurl, matches, unimplemented, testPass));
		if (!unimplemented) {
			org.assertj.core.api.Assertions.assertThat(actualPurl)
					.as("type=%s downloadLocation=%s", type, downloadLocation)
					.isEqualTo(expectedPurl);
		}
	}

	/** Print a console summary table of recorded parameterized cases. Long values keep the prefix. */
	static void printResultTable(String mode) {
		System.out.println();
		System.out.println("========== PURL download-location results (" + mode + ") ==========");
		if (RESULTS.isEmpty()) {
			System.out.println("(no parameterized cases recorded)");
			System.out.println("=================================================================");
			return;
		}

		String header = String.format(
				"| %3s | %-4s | %-13s | %-" + COL_TYPE + "s | %-" + COL_URL + "s | %-" + COL_PURL + "s | %-" + COL_PURL + "s |",
				"#", "OK", "Gap", "type", "downloadLocation", "expectedPurl", "actualPurl");
		String sep = "-".repeat(header.length());
		System.out.println(sep);
		System.out.println(header);
		System.out.println(sep);

		int passExact = 0;
		int passUnimplementedGap = 0;
		int failMismatch = 0;
		int i = 1;
		for (CaseResult r : RESULTS) {
			String gap = gapLabel(r);
			if (r.matches) {
				passExact++;
			} else if (r.unimplemented) {
				passUnimplementedGap++;
			} else {
				failMismatch++;
			}
			System.out.printf(
					"| %3d | %-4s | %-13s | %-" + COL_TYPE + "s | %-" + COL_URL + "s | %-" + COL_PURL + "s | %-" + COL_PURL + "s |%n",
					i++,
					r.testPass ? "PASS" : "FAIL",
					gap,
					truncate(r.type, COL_TYPE),
					truncate(r.downloadLocation, COL_URL),
					truncate(r.expectedPurl, COL_PURL),
					truncate(r.actualPurl, COL_PURL));
		}
		System.out.println(sep);
		System.out.printf(
				"summary: total=%d  PASS=%d (exact=%d, unimplemented-gap=%d)  FAIL=%d (mismatch)%n",
				RESULTS.size(),
				passExact + passUnimplementedGap,
				passExact,
				passUnimplementedGap,
				failMismatch);
		System.out.println("=================================================================");
		System.out.println();
	}

	private static String gapLabel(CaseResult r) {
		if (r.matches) {
			return "";
		}
		return r.unimplemented ? "unimplemented" : "mismatch";
	}

	/** Keep the start of {@code value}; drop the suffix when longer than {@code max}. */
	private static String truncate(String value, int max) {
		if (value == null) {
			return "";
		}
		if (value.length() <= max) {
			return value;
		}
		if (max <= 3) {
			return value.substring(0, max);
		}
		return value.substring(0, max - 3) + "...";
	}

	/** True when JDBC to FOSSLight DB succeeds (cached per JVM). */
	public static boolean isDatabaseAvailable() {
		if (databaseAvailable == null) {
			databaseAvailable = probeDatabase();
		}
		return databaseAvailable;
	}

	/** Types listed under {@code unimplementedTypes} in the JSON resource. */
	static Set<String> unimplementedTypes() throws Exception {
		return new HashSet<>(loadCaseFile().unimplementedTypesOrEmpty());
	}

	/** All JSON cases (including unimplemented types). */
	static Stream<Arguments> downloadLocationToPurlCases() throws Exception {
		return loadCases().stream()
				.map(c -> arguments(c.downloadLocation, c.ossName, c.expectedPurl, c.type));
	}

	static List<PurlCase> loadCases() throws Exception {
		List<PurlCase> cases = loadCaseFile().cases;
		if (cases == null || cases.isEmpty()) {
			throw new IllegalStateException("No cases found in " + CASES_RESOURCE);
		}
		return cases;
	}

	private static PurlCaseFile loadCaseFile() throws Exception {
		if (caseFile != null) {
			return caseFile;
		}
		InputStream in = PurlTestSupport.class.getClassLoader().getResourceAsStream(CASES_RESOURCE);
		if (in == null) {
			throw new IllegalStateException("Missing test resource: " + CASES_RESOURCE);
		}
		try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			caseFile = new Gson().fromJson(reader, PurlCaseFile.class);
			if (caseFile == null) {
				throw new IllegalStateException("Failed to parse " + CASES_RESOURCE);
			}
			return caseFile;
		}
	}

	private static boolean probeDatabase() {
		String url = firstNonBlank(
				System.getProperty("spring.datasource.url"),
				System.getenv("SPRING_DATASOURCE_URL"),
				DEFAULT_JDBC_URL);
		String user = firstNonBlank(
				System.getProperty("spring.datasource.username"),
				System.getenv("SPRING_DATASOURCE_USERNAME"),
				DEFAULT_USER);
		String password = firstNonBlank(
				System.getProperty("spring.datasource.password"),
				System.getenv("SPRING_DATASOURCE_PASSWORD"),
				DEFAULT_PASSWORD);

		try {
			Class.forName("org.mariadb.jdbc.Driver");
			try (Connection conn = DriverManager.getConnection(url, user, password)) {
				boolean ok = conn.isValid(3);
				if (ok) {
					System.out.println("[OssServicePurlDbTest] DB reachable: " + url);
				}
				return ok;
			}
		} catch (Exception e) {
			System.out.println("[OssServicePurlDbTest] DB unavailable (" + url + "): " + e.getMessage());
			return false;
		}
	}

	private static String firstNonBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String v : values) {
			if (v != null && !v.isBlank()) {
				return v;
			}
		}
		return null;
	}

	static class PurlCaseFile {
		List<String> unimplementedTypes;
		List<PurlCase> cases;

		List<String> unimplementedTypesOrEmpty() {
			return unimplementedTypes != null ? unimplementedTypes : Collections.emptyList();
		}
	}

	static class PurlCase {
		String type;
		String downloadLocation;
		String ossName;
		String expectedPurl;
	}

	private static final class CaseResult {
		final String type;
		final String downloadLocation;
		final String expectedPurl;
		final String actualPurl;
		final boolean matches;
		final boolean unimplemented;
		final boolean testPass;

		CaseResult(String type, String downloadLocation, String expectedPurl, String actualPurl,
				boolean matches, boolean unimplemented, boolean testPass) {
			this.type = type;
			this.downloadLocation = downloadLocation;
			this.expectedPurl = expectedPurl;
			this.actualPurl = actualPurl;
			this.matches = matches;
			this.unimplemented = unimplemented;
			this.testPass = testPass;
		}
	}
}
