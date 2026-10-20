/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Equivalence check between the previous SQL normalize predicate and the new
 * Java-side URL variant expansion used by selectBulkOssInfoByUrls.
 */
public class BulkOssInfoByUrlsEquivalenceTest {

	private static final String[] PREFIXES = {
			"",
			"www.",
			"http://",
			"https://",
			"http://www.",
			"https://www.",
			"git://",
			"git://www.",
			"ftp://",
			"ftp://www.",
			"svn://",
			"svn://www.",
			"ssh://",
			"ssh://www."
	};

	private static final String[] SCHEMES = {
			"http", "https", "git", "ftp", "svn", "ssh"
	};

	/** Same logic as OssServiceImpl.expandDownloadLocationVariants */
	static Set<String> expandDownloadLocationVariants(Collection<String> normalizedUrls) {
		Set<String> variants = new LinkedHashSet<>();
		if (normalizedUrls == null || normalizedUrls.isEmpty()) {
			return variants;
		}
		for (String url : normalizedUrls) {
			if (url == null || url.isEmpty()) {
				continue;
			}
			for (String prefix : PREFIXES) {
				variants.add(prefix + url);
			}
		}
		return variants;
	}

	/** Mirror of previous MyBatis SQL normalize expression. */
	static String sqlNormalize(String url) {
		if (url == null) {
			return "";
		}
		int locate = url.indexOf("://");
		String stripped;
		if (locate > 0) {
			stripped = url.substring(locate + 3);
		} else {
			stripped = url;
		}
		if (stripped.startsWith("www.")) {
			stripped = stripped.substring(4);
		}
		return stripped;
	}

	@Test
	void expandedVariantsMatchPreviousSqlNormalizeForKnownSchemes() {
		List<String> inputs = Arrays.asList(
				"github.com/fosslight/fosslight",
				"npmjs.com/package/lodash",
				"pypi.org/project/requests",
				"example.com/path/to/oss");

		for (String normalized : inputs) {
			Set<String> inputSet = new LinkedHashSet<>();
			inputSet.add(normalized);
			Set<String> variants = expandDownloadLocationVariants(inputSet);

			List<String> dbCorpus = new ArrayList<>();
			dbCorpus.add(normalized);
			dbCorpus.add("www." + normalized);
			for (String scheme : SCHEMES) {
				dbCorpus.add(scheme + "://" + normalized);
				dbCorpus.add(scheme + "://www." + normalized);
			}
			dbCorpus.add(normalized + "/");
			dbCorpus.add("https://" + normalized + "/");
			dbCorpus.add("https://www.evil.com/" + normalized);
			dbCorpus.add("github.com/other/repo");

			for (String db : dbCorpus) {
				boolean oldMatch = inputSet.contains(sqlNormalize(db));
				boolean newMatch = variants.contains(db);
				assertEquals(oldMatch, newMatch,
						() -> "input=" + normalized + " db=" + db + " old=" + oldMatch + " new=" + newMatch);
			}
		}
	}

	@Test
	void everyExpandedPrefixRoundTripsThroughSqlNormalize() {
		String normalized = "github.com/fosslight/fosslight";
		Set<String> variants = expandDownloadLocationVariants(Arrays.asList(normalized));
		for (String prefix : PREFIXES) {
			String db = prefix + normalized;
			assertTrue(variants.contains(db));
			assertEquals(normalized, sqlNormalize(db));
		}
	}
}
