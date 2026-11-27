/*
 * Copyright (c) 2026 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class SpdxSpreadsheetTemplateTest {
	private static final String TEMPLATE_PATH = "src/main/resources/template";

	@TempDir
	Path tempDir;

	@Test
	void opensXlsxPreservingTemplateMetadataRowsAndStyles() throws Exception {
		try (InputStream input = Files.newInputStream(Path.of(TEMPLATE_PATH, SPDXUtil2.SPREADSHEET_TEMPLATE));
				Workbook original = WorkbookFactory.create(input);
				Workbook workbook = SPDXUtil2.openSpreadsheetTemplate(TEMPLATE_PATH)) {
			assertThat(workbook).isInstanceOf(XSSFWorkbook.class);
			assertThat(workbook.getNumberOfSheets()).isEqualTo(9);
			Row document = workbook.getSheet("Document Info").getRow(1);
			assertThat(document.getCell(0).getStringCellValue()).isEqualTo("2.3.0");
			assertThat(document.getCell(1).getStringCellValue()).isEqualTo("SPDX-2.3");
			assertThat(document.getCell(2).getStringCellValue()).isEqualTo("CC0-1.0");
			assertThat(document.getCell(3).getStringCellValue()).isEqualTo("SPDXRef-DOCUMENT");
			assertThat(new DataFormatter().formatCellValue(document.getCell(4))).isEqualTo("3.29");
			Row header = workbook.getSheet("Package Info").getRow(0);
			assertThat(header.getCell(18).getStringCellValue()).isEqualTo("Description");
			assertThat(header.getCell(22).getStringCellValue()).isEqualTo("Primary Purpose");
			assertThat(header.getCell(23).getStringCellValue()).isEqualTo("Release Date");
			assertThat(header.getCell(24).getStringCellValue()).isEqualTo("Built Date");
			assertThat(header.getCell(25).getStringCellValue()).isEqualTo("Valid Until Date");
			for (Sheet sheet : workbook) {
				Sheet originalSheet = original.getSheet(sheet.getSheetName());
				assertThat(sheet.getPhysicalNumberOfRows())
						.as(sheet.getSheetName())
						.isEqualTo(originalSheet.getPhysicalNumberOfRows());
				for (Row row : originalSheet) {
					for (Cell cell : row) {
						Cell loaded = sheet.getRow(row.getRowNum()).getCell(cell.getColumnIndex());
						assertThat(new DataFormatter().formatCellValue(loaded))
								.isEqualTo(new DataFormatter().formatCellValue(cell));
						assertThat(loaded.getCellStyle().getIndex()).isEqualTo(cell.getCellStyle().getIndex());
					}
				}
			}
		}
	}

	@Test
	void doesNotOverwriteMetadataOrRemoveExistingRows() throws Exception {
		try (Workbook workbook = SPDXUtil2.openSpreadsheetTemplate(TEMPLATE_PATH);
				OutputStream output = Files.newOutputStream(tempDir.resolve(SPDXUtil2.SPREADSHEET_TEMPLATE))) {
			workbook.getSheet("Document Info").getRow(1).getCell(4).setCellValue("3.18");
			workbook.getSheet("Package Info").createRow(10).createCell(0).setCellValue("retained-package");
			workbook.write(output);
		}
		try (Workbook workbook = SPDXUtil2.openSpreadsheetTemplate(tempDir.toString())) {
			assertThat(workbook.getSheet("Document Info").getRow(1).getCell(4).getStringCellValue()).isEqualTo("3.18");
			assertThat(workbook.getSheet("Package Info").getRow(10).getCell(0).getStringCellValue()).isEqualTo("retained-package");
		}
	}

	@Test
	void failsWhenConfiguredTemplateIsMissing() {
		assertThatThrownBy(() -> SPDXUtil2.openSpreadsheetTemplate(tempDir.toString()))
				.isInstanceOf(java.io.IOException.class)
				.hasMessageContaining(SPDXUtil2.SPREADSHEET_TEMPLATE);
	}

	@ParameterizedTest
	@ValueSource(strings = {"json", "rdf", "tag", "spdx", "yaml"})
	void converts23XlsxToSupportedFormatsWithoutExamplePackages(String extension) throws Exception {
		Path spreadsheet = tempDir.resolve("document.xlsx");
		try (Workbook workbook = SPDXUtil2.openSpreadsheetTemplate(TEMPLATE_PATH);
				OutputStream output = Files.newOutputStream(spreadsheet)) {
			Row document = workbook.getSheet("Document Info").getRow(1);
			document.createCell(5).setCellValue("test-project");
			document.createCell(6).setCellValue("https://example.org/spdx/test-project");
			document.createCell(10).setCellValue("Tool: FOSSLight");
			Cell created = document.createCell(11);
			created.setCellValue(Date.from(Instant.parse("2026-11-26T00:00:00Z")));
			CellStyle dateStyle = workbook.createCellStyle();
			dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
			created.setCellStyle(dateStyle);
			Row pkg = workbook.getSheet("Package Info").createRow(1);
			pkg.createCell(0).setCellValue("test-package");
			pkg.createCell(1).setCellValue("SPDXRef-Package-test");
			pkg.createCell(2).setCellValue("1.0");
			pkg.createCell(7).setCellValue("NOASSERTION");
			pkg.createCell(12).setCellValue("MIT");
			pkg.createCell(13).setCellValue("MIT");
			pkg.createCell(16).setCellValue("NOASSERTION");
			pkg.createCell(20).setCellValue(false);
			pkg.createCell(22).setCellValue("LIBRARY");
			Row relationship = workbook.getSheet("Relationships").createRow(1);
			relationship.createCell(0).setCellValue("SPDXRef-DOCUMENT");
			relationship.createCell(1).setCellValue("DESCRIBES");
			relationship.createCell(2).setCellValue("SPDXRef-Package-test");
			workbook.write(output);
		}
		try (Workbook written = WorkbookFactory.create(spreadsheet.toFile())) {
			assertThat(written).isInstanceOf(XSSFWorkbook.class);
		}
		Path converted = tempDir.resolve("document." + extension);
		SPDXUtil2.convert("test", spreadsheet.toString(), converted.toString());
		assertThat(Files.size(converted)).isPositive();
		Path json = converted;
		if (!"json".equals(extension)) {
			json = tempDir.resolve("roundtrip.json");
			SPDXUtil2.convert("test", converted.toString(), json.toString());
		}
		JsonNode result = new ObjectMapper().readTree(json.toFile());
		assertThat(result.path("spdxVersion").asText()).isEqualTo("SPDX-2.3");
		assertThat(result.path("creationInfo").path("licenseListVersion").asText()).isEqualTo("3.29");
		assertThat(result.path("packages").size()).isEqualTo(1);
		JsonNode pkg = result.path("packages").get(0);
		assertThat(pkg.path("name").asText()).isEqualTo("test-package");
		assertThat(pkg.path("primaryPackagePurpose").asText()).isEqualTo("LIBRARY");
		assertThat(pkg.path("filesAnalyzed").asBoolean()).isFalse();
		assertThat(pkg.has("licenseInfoFromFiles")).isFalse();
		assertThat(result.path("hasExtractedLicensingInfos").size()).isZero();
		assertThat(result.path("files").size()).isZero();
	}
}
