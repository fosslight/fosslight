/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.File;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.tools.SpdxConverter;

public class SPDXUtil2 {
	static final Logger logger = LoggerFactory.getLogger("DEFAULT_LOG");

	public static void convert(String prjId, String inputFilePath, String outputFilePath) throws Exception {
		// 기존 파일 변환 결과 파일이 존재하는 경우 삭제
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
}
