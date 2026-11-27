/*
 * Copyright (c) 2026 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package oss.fosslight.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.CoCode;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.service.VerificationService;

class SpdxSpreadsheetExportTest {
	private static final String TEMPLATE_PATH = "src/main/resources/template";
	private final Map<Class<?>, Map<String, Object>> originalFields = new LinkedHashMap<>();
	private Path outputDirectory;
	private FileService fileService;
	private ProjectService projectService;
	private SelfCheckService selfCheckService;

	@BeforeEach
	void configureExportServices() throws Exception {
		outputDirectory = Files.createDirectories(Path.of("build", "spdx-export-tests", UUID.randomUUID().toString()));
		replaceStatic(CoTopComponent.class, "appEnv", new MockEnvironment()
				.withProperty("export.template.path", TEMPLATE_PATH)
				.withProperty("server.domain", "https://example.org/"));
		WebApplicationContext context = mock(WebApplicationContext.class);
		when(context.getBean(any(Class.class))).thenAnswer(invocation -> mock((Class<?>) invocation.getArgument(0)));
		replaceStatic(CoTopComponent.class, "applicationContext", context);
		CoCode exportCode = mock(CoCode.class);
		when(exportCode.getCdDtlExp(CoConstDef.CD_MAX_ROW_COUNT)).thenReturn("1000");
		HashMap<String, CoCode> codes = new HashMap<>();
		codes.put(CoConstDef.CD_EXCEL_DOWNLOAD, exportCode);
		replaceStatic(CoCodeManager.class, "codes", codes);

		LicenseMaster license = new LicenseMaster();
		license.setLicenseId("1");
		license.setLicenseName("MIT");
		license.setShortIdentifier("MIT");
		replaceStatic(CoCodeManager.class, "LICENSE_INFO", new HashMap<>(Map.of("MIT", license)));
		replaceStatic(CoCodeManager.class, "LICENSE_INFO_UPPER", new HashMap<>(Map.of("MIT", license)));
		OssLicense declaredLicense = new OssLicense();
		declaredLicense.setLicenseName("MIT");
		OssMaster oss = new OssMaster();
		oss.setOssLicenses(List.of(declaredLicense));
		replaceStatic(CoCodeManager.class, "OSS_INFO_UPPER", new HashMap<>(Map.of("GENERATED-PACKAGE_1.0", oss)));

		projectService = mock(ProjectService.class);
		selfCheckService = mock(SelfCheckService.class);
		VerificationService verificationService = mock(VerificationService.class);
		T2UserService userService = mock(T2UserService.class);
		fileService = mock(FileService.class);
		replaceStatic(ExcelDownLoadUtil.class, "projectService", projectService);
		replaceStatic(ExcelDownLoadUtil.class, "selfCheckService", selfCheckService);
		replaceStatic(ExcelDownLoadUtil.class, "verificationService", verificationService);
		replaceStatic(ExcelDownLoadUtil.class, "userService", userService);
		replaceStatic(ExcelDownLoadUtil.class, "fileService", fileService);
		replaceStatic(ExcelDownLoadUtil.class, "writepath", outputDirectory.toString());
		replaceStatic(ExcelDownLoadUtil.class, "downloadpath", TEMPLATE_PATH);

		Project project = new Project();
		project.setPrjId("123");
		project.setPrjName("generated-project");
		project.setPrjVersion("1.0");
		project.setCreator("exporter");
		project.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
		when(projectService.getProjectDetail(any(Project.class))).thenReturn(project);
		when(selfCheckService.getProjectDetail(any(Project.class))).thenReturn(project);
		when(projectService.getDependenciesDataList(any(Project.class))).thenReturn(List.of());
		T2Users user = new T2Users();
		user.setEmail("exporter@example.org");
		when(userService.getUser(any(T2Users.class))).thenReturn(user);

		OssComponentsLicense usedLicense = new OssComponentsLicense();
		usedLicense.setLicenseName("MIT");
		OssComponents component = new OssComponents();
		component.setComponentId("201");
		component.setOssId("101");
		component.setOssName("generated-package");
		component.setOssVersion("1.0");
		component.setLicenseName("MIT");
		component.setOssComponentsLicense(new ArrayList<>(List.of(usedLicense)));
		component.setDownloadLocation("https://example.org/generated-package");
		component.setCopyrightText("Copyright 2026 Example");
		component.setOssAttribution("");
		Map<String, Object> packageInfo = new HashMap<>();
		packageInfo.put("noticeObligationList", new ArrayList<>(List.of(component)));
		packageInfo.put("disclosureObligationList", new ArrayList<>());
		packageInfo.put("notObligationList", new ArrayList<>());
		packageInfo.put("addOssComponentList", new ArrayList<>());
		when(verificationService.getNoticeHtmlInfo(any(OssNotice.class))).thenReturn(packageInfo);
		when(projectService.getExportDataForSBOMInfo(any(OssNotice.class))).thenReturn(packageInfo);
		when(selfCheckService.getExportDataForSBOMInfo(any(OssNotice.class))).thenReturn(packageInfo);
		when(verificationService.setMergeGridData(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
		when(selfCheckService.setMergeGridData(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
		when(fileService.registFileDownload(anyString(), anyString(), anyString())).thenReturn("generated-id");
	}

	@AfterEach
	void restoreStaticFieldsAndDeleteOutput() throws IOException {
		originalFields.forEach((type, fields) ->
				fields.forEach((name, value) -> ReflectionTestUtils.setField(type, name, value)));
		if (outputDirectory != null) {
			try (Stream<Path> paths = Files.walk(outputDirectory)) {
				for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
					Files.deleteIfExists(path);
				}
			}
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"spdx", "spdx_sbom", "spdx_self"})
	void exportsOnlyGeneratedPackagesAs23Xlsx(String exportType) throws Exception {
		String fileId = ExcelDownLoadUtil.getExcelDownloadId(exportType, dataFor(exportType), TEMPLATE_PATH);
		assertThat(fileId).isEqualTo("generated-id");
		ArgumentCaptor<String> directory = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> originalName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> logicalName = ArgumentCaptor.forClass(String.class);
		verify(fileService).registFileDownload(directory.capture(), originalName.capture(), logicalName.capture());
		assertThat(originalName.getValue()).isEqualTo("SPDXRdf-generated-project-1.0.xlsx");
		assertThat(logicalName.getValue()).endsWith(".xlsx");
		Path spreadsheet = Path.of(directory.getValue(), logicalName.getValue());
		try (Workbook workbook = WorkbookFactory.create(spreadsheet.toFile())) {
			assertThat(workbook).isInstanceOf(XSSFWorkbook.class);
			Row document = workbook.getSheet("Document Info").getRow(1);
			assertThat(document.getCell(0).getStringCellValue()).isEqualTo("2.3.0");
			assertThat(document.getCell(1).getStringCellValue()).isEqualTo("SPDX-2.3");
			assertThat(document.getCell(2).getStringCellValue()).isEqualTo("CC0-1.0");
			assertThat(document.getCell(3).getStringCellValue()).isEqualTo("SPDXRef-DOCUMENT");
			assertThat(new DataFormatter().formatCellValue(document.getCell(4))).isEqualTo("3.29");
			assertThat(new DataFormatter().formatCellValue(document.getCell(7))).isEmpty();
			assertThat(document.getCell(11).getCellType()).isEqualTo(CellType.NUMERIC);
			assertThat(DateUtil.isCellDateFormatted(document.getCell(11))).isTrue();
			Row pkg = workbook.getSheet("Package Info").getRow(1);
			assertThat(pkg.getCell(0).getStringCellValue()).isEqualTo("generated-package");
			assertThat(new DataFormatter().formatCellValue(pkg.getCell(14))).isEmpty();
			assertThat(pkg.getCell(20).getStringCellValue()).isEqualTo("false");
			for (String emptySheet : List.of("External Refs", "Extracted License Info", "Per File Info",
					"Annotations", "Snippets", "Reviewers")) {
				for (Row row : workbook.getSheet(emptySheet)) {
					if (row.getRowNum() > 0) {
						row.forEach(cell -> assertThat(new DataFormatter().formatCellValue(cell)).as(emptySheet).isEmpty());
					}
				}
			}
		}

		Path json = outputDirectory.resolve("converted.json");
		SPDXUtil2.convert("123", spreadsheet.toString(), json.toString());
		JsonNode result = new ObjectMapper().readTree(json.toFile());
		assertThat(result.path("spdxVersion").asText()).isEqualTo("SPDX-2.3");
		assertThat(result.path("dataLicense").asText()).isEqualTo("CC0-1.0");
		assertThat(result.path("SPDXID").asText()).isEqualTo("SPDXRef-DOCUMENT");
		assertThat(result.path("name").asText()).isEqualTo("generated-project-1.0");
		assertThat(result.path("documentNamespace").asText())
				.startsWith("https://example.org/SPDXRef-generated-project-1.0-");
		assertThat(result.path("creationInfo").path("licenseListVersion").asText()).isEqualTo("3.29");
		assertThat(result.path("creationInfo").path("created").asText()).isNotBlank();
		assertThat(result.path("creationInfo").path("creators").toString())
				.contains("Person: exporter (exporter@example.org)",
						"Organization: FOSSLight (opensource@fosslight.org)", "Tool: SPDXTools-2.3.0");
		assertThat(result.path("packages").size()).isEqualTo(1);
		JsonNode pkg = result.path("packages").get(0);
		assertThat(pkg.path("name").asText()).isEqualTo("generated-package");
		assertThat(pkg.path("licenseDeclared").asText()).isEqualTo("MIT");
		assertThat(pkg.path("licenseConcluded").asText()).isEqualTo("MIT");
		assertThat(pkg.path("filesAnalyzed").asBoolean()).isFalse();
		assertThat(pkg.has("licenseInfoFromFiles")).isFalse();
		assertThat(result.path("files").size()).isZero();
		assertThat(result.path("hasExtractedLicensingInfos").size()).isZero();
		assertThat(result.path("relationships").size()).isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(strings = {"spdx", "spdx_sbom", "spdx_self"})
	void missingTemplateDoesNotRegisterPartialExport(String exportType) {
		assertThatThrownBy(() -> ExcelDownLoadUtil.getExcelDownloadId(exportType, dataFor(exportType),
				outputDirectory.resolve("missing-template").toString()))
				.isInstanceOf(IOException.class)
				.hasMessageContaining(SPDXUtil2.SPREADSHEET_TEMPLATE);
		verifyNoInteractions(fileService);
	}

	@ParameterizedTest
	@ValueSource(strings = {"spdx", "spdx_sbom", "spdx_self"})
	void processingFailureDoesNotRegisterPartialExport(String exportType) {
		IllegalStateException failure = new IllegalStateException("Project unavailable");
		when(projectService.getProjectDetail(any(Project.class))).thenThrow(failure);
		when(selfCheckService.getProjectDetail(any(Project.class))).thenThrow(failure);
		assertThatThrownBy(() -> ExcelDownLoadUtil.getExcelDownloadId(exportType, dataFor(exportType), TEMPLATE_PATH))
				.isInstanceOf(IOException.class)
				.hasCause(failure);
		verifyNoInteractions(fileService);
	}

	private void replaceStatic(Class<?> type, String name, Object value) {
		originalFields.computeIfAbsent(type, unused -> new LinkedHashMap<>())
				.put(name, ReflectionTestUtils.getField(type, name));
		ReflectionTestUtils.setField(type, name, value);
	}

	private String dataFor(String exportType) {
		return "spdx_sbom".equals(exportType) ? "123" : "{\"prjId\":\"123\"}";
	}
}
