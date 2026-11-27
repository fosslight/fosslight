/*
 * Copyright (c) 2026 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import oss.fosslight.CoTopComponent;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.service.impl.FileServiceImpl;

class FileServiceSpdxSpreadsheetTest {
	@TempDir
	Path tempDir;
	private Environment originalEnvironment;

	@BeforeEach
	void configureTemplatePath() {
		originalEnvironment = (Environment) ReflectionTestUtils.getField(CoTopComponent.class, "appEnv");
		ReflectionTestUtils.setField(CoTopComponent.class, "appEnv",
				new MockEnvironment().withProperty("export.template.path", "src/main/resources/template"));
	}

	@AfterEach
	void restoreEnvironment() {
		ReflectionTestUtils.setField(CoTopComponent.class, "appEnv", originalEnvironment);
	}

	@Test
	void yamlUploadUses23XlsxWithoutExampleRows() throws Exception {
		Path input = tempDir.resolve("document.yaml");
		Files.writeString(input, "spdxVersion: SPDX-2.3\npackages:\n"
				+ "  - SPDXID: SPDXRef-Package-test\n    name: test-package\n"
				+ "    versionInfo: '1.0'\n    licenseDeclared: MIT\n    filesAnalyzed: false\n");
		Path output = tempDir.resolve("document.xlsx");
		Boolean converted = ReflectionTestUtils.invokeMethod(new FileServiceImpl(),
				"convertSpdxYamlToXlsUsingTree", input, output);
		assertThat(converted).isTrue();
		assertConvertedWorkbook(output, 12);
	}

	@Test
	void cycloneDxUploadUsesSame23XlsxTemplateWithoutExampleRows() throws Exception {
		Path input = tempDir.resolve("document.json");
		String json = "{\"bomFormat\":\"CycloneDX\",\"specVersion\":\"1.5\",\"version\":1,"
				+ "\"components\":[{\"type\":\"library\",\"bom-ref\":\"test\",\"name\":\"test-package\","
				+ "\"version\":\"1.0\",\"licenses\":[{\"license\":{\"id\":\"MIT\"}}]}]}";
		Files.writeString(input, json);
		Path output = tempDir.resolve("document.xlsx");
		Boolean converted = ReflectionTestUtils.invokeMethod(new FileServiceImpl(),
				"convertCdxToExcel", input.toFile(), json, output.toString(), new UploadFile());
		assertThat(converted).isTrue();
		assertConvertedWorkbook(output, 13);
	}

	private void assertConvertedWorkbook(Path output, int licenseColumn) throws Exception {
		try (Workbook workbook = WorkbookFactory.create(output.toFile())) {
			assertThat(workbook).isInstanceOf(XSSFWorkbook.class);
			assertThat(workbook.getSheet("Document Info").getRow(1).getCell(1).getStringCellValue())
					.isEqualTo("SPDX-2.3");
			assertThat(workbook.getSheet("Package Info").getRow(1).getCell(0).getStringCellValue())
					.isEqualTo("test-package");
			assertThat(workbook.getSheet("Package Info").getRow(1).getCell(licenseColumn).getStringCellValue())
					.isEqualTo("MIT");
			assertThat(workbook.getSheet("Package Info").getRow(1).getCell(20).getStringCellValue())
					.isEqualTo("FALSE");
			for (String sheet : new String[] {"Extracted License Info", "Per File Info", "External Refs"}) {
				for (Row row : workbook.getSheet(sheet)) {
					if (row.getRowNum() > 0) {
						row.forEach(cell -> assertThat(new DataFormatter().formatCellValue(cell)).as(sheet).isEmpty());
					}
				}
			}
		}
	}
}
