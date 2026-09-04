/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.doc.example;

public final class PartnerApiExampleConstants {
    private PartnerApiExampleConstants() {
    }

    public static final String PARTNER_LIST_SEARCH_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"list\": [\n"
                    + "    {\n"
                    + "      \"partnerId\": \"1\",\n"
                    + "      \"partnerName\": \"Example Supplier\",\n"
                    + "      \"softwareName\": \"Example SDK\",\n"
                    + "      \"softwareVersion\": \"2.5.0\",\n"
                    + "      \"status\": \"Confirm\",\n"
                    + "      \"division\": \"HE Division\",\n"
                    + "      \"creator\": \"user01\",\n"
                    + "      \"created\": \"2026-08-01 09:00:00\",\n"
                    + "      \"modifier\": \"user02\",\n"
                    + "      \"modified\": \"2026-08-20 14:30:00\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"totalCount\": 1\n"
                    + "}";

    public static final String PARTNER_SBOM_JSON_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"contents\": [\n"
                    + "    {\n"
                    + "      \"ossName\": \"sample-oss\",\n"
                    + "      \"ossVersion\": \"1.0.0\",\n"
                    + "      \"license\": [\"Apache-2.0\"],\n"
                    + "      \"download location\": \"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\",\n"
                    + "      \"homepage\": \"https://example.org/sample-oss\",\n"
                    + "      \"Vulnerability\": \"7.5\",\n"
                    + "      \"source\": \"NVD\",\n"
                    + "      \"licenseType\": \"Permissive\"\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
}
