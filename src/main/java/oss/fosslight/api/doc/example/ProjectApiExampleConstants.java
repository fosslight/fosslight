/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.doc.example;

public final class ProjectApiExampleConstants {
    private ProjectApiExampleConstants() {
    }

    public static final String PROJECT_LIST_SEARCH_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"list\": [\n"
                    + "    {\n"
                    + "      \"prjId\": \"6304\",\n"
                    + "      \"prjName\": \"FOSSLight TV Platform\",\n"
                    + "      \"prjVersion\": \"2026.08\",\n"
                    + "      \"status\": \"Complete\",\n"
                    + "      \"identificationStatus\": \"Confirm\",\n"
                    + "      \"verificationStatus\": \"Confirm\",\n"
                    + "      \"division\": \"LGE\",\n"
                    + "      \"creator\": \"user01\",\n"
                    + "      \"created\": \"2026-08-01 09:00:00\",\n"
                    + "      \"modifier\": \"user02\",\n"
                    + "      \"modified\": \"2026-08-25 10:30:00\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"totalCount\": 1\n"
                    + "}";

    public static final String PROJECT_MODEL_LIST_SEARCH_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"records\": 1,\n"
                    + "  \"contents\": [\n"
                    + "    {\n"
                    + "      \"prjId\": \"6304\",\n"
                    + "      \"distributionName\": \"FOSSLight 2026\",\n"
                    + "      \"modelList\": [\n"
                    + "        {\n"
                    + "          \"modelName\": \"AIRCON\",\n"
                    + "          \"category\": \"Appliances > Air Conditioner\",\n"
                    + "          \"releaseDate\": \"20260831\",\n"
                    + "          \"status\": \"Complete\"\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    public static final String PROJECT_SBOM_JSON_SUCCESS_EXAMPLE =
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

    public static final String PROJECT_SBOM_COMPARE_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"contents\": {\n"
                    + "    \"add\": [\n"
                    + "      {\n"
                    + "        \"name\": \"new-oss\",\n"
                    + "        \"version\": \"2.0.0\"\n"
                    + "      }\n"
                    + "    ],\n"
                    + "    \"delete\": [\n"
                    + "      {\n"
                    + "        \"name\": \"old-oss\",\n"
                    + "        \"version\": \"1.0.0\"\n"
                    + "      }\n"
                    + "    ],\n"
                    + "    \"change\": [\n"
                    + "      {\n"
                    + "        \"name\": \"sample-oss\",\n"
                    + "        \"prev\": [\n"
                    + "          {\n"
                    + "            \"version\": \"1.0.0\"\n"
                    + "          }\n"
                    + "        ],\n"
                    + "        \"now\": [\n"
                    + "          {\n"
                    + "            \"version\": \"1.1.0\"\n"
                    + "          }\n"
                    + "        ]\n"
                    + "      }\n"
                    + "    ]\n"
                    + "  }\n"
                    + "}";
}
