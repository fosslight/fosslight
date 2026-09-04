/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.doc.example;

public final class BatApiExampleConstants {
    private BatApiExampleConstants() {
    }

    public static final String BINARY_INFO_SEARCH_SUCCESS_EXAMPLE =
            "{\n"
                    + "  \"content\": [\n"
                    + "    {\n"
                    + "      \"binaryFileName\": \"bash\",\n"
                    + "      \"path\": \"/bin/bash\",\n"
                    + "      \"ossName\": \"bash\",\n"
                    + "      \"ossVersion\": \"5.2.21\",\n"
                    + "      \"license\": \"GPL-3.0-or-later\",\n"
                    + "      \"checksum\": \"a1b2c3d4e5f6\",\n"
                    + "      \"sourceCodePath\": \"/usr/src/bash\",\n"
                    + "      \"platformName\": \"Linux\",\n"
                    + "      \"platformVersion\": \"6.1\"\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
}
