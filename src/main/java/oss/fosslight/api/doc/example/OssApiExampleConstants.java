/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.doc.example;

public final class OssApiExampleConstants {
    private OssApiExampleConstants() {
    }

    public static final String OSS_LIST_SEARCH_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"list\": [\n"
                    + "    {\n"
                    + "      \"ossId\": \"1\",\n"
                    + "      \"ossType\": \"100\",\n"
                    + "      \"ossTypeMap\": {\n"
                    + "        \"Multi\": \"Y\",\n"
                    + "        \"Dual\": \"N\",\n"
                    + "        \"V-Diff\": \"N\"\n"
                    + "      },\n"
                    + "      \"ossName\": \"sample-oss\",\n"
                    + "      \"ossVersion\": \"1.0.0\",\n"
                    + "      \"licenseName\": \"Apache-2.0\",\n"
                    + "      \"licenseType\": \"PMS\",\n"
                    + "      \"downloadUrl\": \"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\",\n"
                    + "      \"downloadUrls\": [\"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\"],\n"
                    + "      \"homepageUrl\": \"https://example.org/sample-oss\",\n"
                    + "      \"description\": \"Sample open source component\",\n"
                    + "      \"cveId\": \"CVE-2026-1234\",\n"
                    + "      \"cvssScore\": \"7.5\",\n"
                    + "      \"creator\": \"user01\",\n"
                    + "      \"created\": \"2026-08-01 09:00:00\",\n"
                    + "      \"modifier\": \"user02\",\n"
                    + "      \"modified\": \"2026-08-20 14:30:00\",\n"
                    + "      \"obligations\": [\"Y\", \"N\"],\n"
                    + "      \"obligationTypeMap\": {\n"
                    + "        \"Notice\": \"Y\",\n"
                    + "        \"Source\": \"N\"\n"
                    + "      },\n"
                    + "      \"copyright\": \"Copyright 2026 Example Authors\",\n"
                    + "      \"nicknames\": \"sample|sample-lib\",\n"
                    + "      \"nicknameList\": [\"sample\", \"sample-lib\"],\n"
                    + "      \"attribution\": \"This product includes sample-oss.\",\n"
                    + "      \"exclude\": false\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"totalCount\": 1\n"
                    + "}";

    public static final String LICENSE_LIST_SEARCH_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"list\": [\n"
                    + "    {\n"
                    + "      \"licenseId\": \"1\",\n"
                    + "      \"licenseName\": \"Apache License 2.0\",\n"
                    + "      \"licenseType\": \"PMS\",\n"
                    + "      \"ossId\": \"1\",\n"
                    + "      \"licenseText\": \"Apache License Version 2.0\",\n"
                    + "      \"licenseIdentifier\": \"Apache-2.0\",\n"
                    + "      \"homepageUrl\": \"https://www.apache.org/licenses/LICENSE-2.0\",\n"
                    + "      \"description\": \"Apache License, Version 2.0\",\n"
                    + "      \"creator\": \"user01\",\n"
                    + "      \"modifier\": \"user02\",\n"
                    + "      \"created\": \"2026-08-01 09:00:00\",\n"
                    + "      \"modified\": \"2026-08-20 14:30:00\",\n"
                    + "      \"restrictions\": [\"Patent Grant\"],\n"
                    + "      \"obligations\": [\"Y\", \"N\"],\n"
                    + "      \"attribution\": \"This product includes Apache License 2.0.\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"totalCount\": 1\n"
                    + "}";

    public static final String OSS_DOWNLOAD_LOCATION_REFINE_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"UPDATE-DOWNLOAD-LOCATION-FORMAT\": {\n"
                    + "    \"reFineTotalCnt\": 1,\n"
                    + "    \"reFineItems\": {\n"
                    + "      \"sample-oss_1.0.0\": [\n"
                    + "        \"https://github.com/example/sample-oss/archive/v1.0.0.tar.gz\"\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  }\n"
                    + "}";
}
