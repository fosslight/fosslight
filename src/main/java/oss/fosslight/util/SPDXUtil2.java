/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.File;
import java.nio.file.Paths;
import java.security.Permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.tools.SpdxConverter;

public class SPDXUtil2 {
	static final Logger logger = LoggerFactory.getLogger("DEFAULT_LOG");

	public static void convert(String prjId, String inputFilePath, String outputFilePath) throws Exception {
		File inputFile = Paths.get(outputFilePath).toFile();
		inputFile.deleteOnExit();

		logger.debug("SPDX format convert ("+prjId+") :" + inputFilePath + " => " + outputFilePath);
		try {
			SpdxConverter.convert(inputFilePath, outputFilePath);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			throw e;
		}
	}
	
	@SuppressWarnings("removal")
	public static void convert2(String prjId, String inputFilePath, String outputFilePath) throws Exception {
        File outputFile = Paths.get(outputFilePath).toFile();
        if (outputFile.exists()) {
            outputFile.delete();
        }

        logger.debug("SPDX format convert (" + prjId + ") :" + inputFilePath + " => " + outputFilePath);

        SecurityManager originalSecurityManager = System.getSecurityManager();
        
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    // Allow all other permissions.
                }
                @Override
                public void checkExit(int status) {
                    // Throws an intentional SecurityException when tools-java calls System.exit to intercept and block the JVM shutdown, keeping Tomcat running.
                    throw new SecurityException("Intercepted System.exit(" + status + ") from SPDX Library.");
                }
            });

            String[] args = new String[]{"Convert", inputFilePath, outputFilePath};
            org.spdx.tools.Main.main(args);
        } catch (SecurityException se) {
            logger.warn("[SPDX-Bypass] System.exit blocked successfully. Tomcat is safe. Reason: " + se.getMessage());
        } catch (Exception e) {
            logger.error("Standard SPDX Error: " + e.getMessage());
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }

        if (outputFile.exists() && outputFile.length() > 0) {
            logger.info("SPDX Custom Conversion Successfully Generated Excel File: " + outputFilePath);
        } else {
            throw new Exception("Error converting SPDX file: Output file was not generated due to formatting issues.");
        }
    }
}
