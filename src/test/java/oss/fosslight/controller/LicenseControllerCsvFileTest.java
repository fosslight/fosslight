/*
Copyright (c) 2026
SPDX-License-Identifier: AGPL-3.0-only
*/
package oss.fosslight.controller;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import oss.fosslight.common.Url;
import oss.fosslight.common.CoConstDef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "user", roles = "USER")
class LicenseControllerCsvFileTest {
    private static final Path uploadDirectory = createUploadDirectory();

    private static Path createUploadDirectory() {
        try {
            // Keep uploads on the checkout's filesystem, including Windows checkouts on another drive.
            Path parent = Files.createDirectories(Paths.get("build", "tmp"));
            return Files.createTempDirectory(parent, "license-csv-upload-").toAbsolutePath();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @AfterAll
    static void deleteUploads() throws IOException {
        FileSystemUtils.deleteRecursively(uploadDirectory);
    }

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void uploadPath(DynamicPropertyRegistry registry) {
        // readLicenseList prepends '.' to upload.path and appends the filename directly.
        registry.add("upload.path", () -> "/" + Paths.get("").toAbsolutePath()
                .relativize(uploadDirectory.toAbsolutePath()) + "/");
    }

    @ParameterizedTest
    @ValueSource(strings = {"xls", "XLS", "Xls", "xlsx", "XLSX", "Xlsx"})
    void returnsParsedLicensesInOrderWithReadyStatus(String extension) throws Exception {
        mockMvc.perform(upload(workbook("licenses", extension, "LICENSE NAME", "MIT", "Apache-2.0")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.res").value(true))
                .andExpect(jsonPath("$.limitCheck").isEmpty())
                .andExpect(jsonPath("$.value.length()").value(2))
                .andExpect(jsonPath("$.value[0].status").value("Ready"))
                .andExpect(jsonPath("$.value[0].license.licenseName").value("MIT"))
                .andExpect(jsonPath("$.value[0].license.shortIdentifier").value("MIT"))
                .andExpect(jsonPath("$.value[1].status").value("Ready"))
                .andExpect(jsonPath("$.value[1].license.licenseName").value("Apache-2.0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"xls", "XLS", "Xls", "xlsx", "XLSX", "Xlsx"})
    void rejectsUnrecognizedHeadersWithoutReadyRows(String extension) throws Exception {
        mockMvc.perform(upload(workbook("invalid", extension, "UNKNOWN COLUMN", "MIT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(false))
                .andExpect(jsonPath("$.limitCheck").isEmpty())
                .andExpect(jsonPath("$.value").doesNotExist());
    }

    @Test
    void rejectsUnsupportedExtension() throws Exception {
        mockMvc.perform(upload(new MockMultipartFile("licenses", "licenses.txt", "text/plain",
                        "LICENSE NAME\nMIT".getBytes(java.nio.charset.StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(false))
                .andExpect(jsonPath("$.limitCheck").isEmpty())
                .andExpect(jsonPath("$.value").doesNotExist());
    }

    @ParameterizedTest
    @MethodSource("workbookSizeCases")
    void reportsWorkbookSizeLimitWithoutLosingParsedRows(String extension, long size) throws Exception {
        MockMultipartFile source = workbook("sized", extension, "LICENSE NAME", "MIT");
        // Override only metadata so testing the 15 MiB boundary does not allocate a huge fixture.
        MockMultipartFile file = new MockMultipartFile("sized", "sized." + extension,
                source.getContentType(), source.getBytes()) {
            @Override
            public long getSize() {
                return size;
            }
        };
        mockMvc.perform(upload(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(true))
                .andExpect(jsonPath("$.value[0].license.licenseName").value("MIT"))
                .andExpect(jsonPath("$.value[0].status").value("Ready"))
                .andExpect(size > CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT
                        ? jsonPath("$.limitCheck[0]").value("FILE_SIZE_LIMIT_OVER")
                        : jsonPath("$.limitCheck").isEmpty());
    }

    @Test
    void acceptsHeaderOnlyWorkbookWithNoReadyRows() throws Exception {
        mockMvc.perform(upload(workbook("empty", "xlsx", "LICENSE NAME")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(true))
                .andExpect(jsonPath("$.value").isEmpty());
    }

    @Test
    void combinesLicensesFromMultipleUploadParts() throws Exception {
        mockMvc.perform(upload(workbook("first", "xls", "LICENSE NAME", "MIT"))
                        .file(workbook("second", "xlsx", "LICENSE NAME", "BSD-3-Clause")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(true))
                .andExpect(jsonPath("$.value.length()").value(2))
                .andExpect(jsonPath("$.value[0].license.licenseName").value("MIT"))
                .andExpect(jsonPath("$.value[1].license.licenseName").value("BSD-3-Clause"))
                .andExpect(jsonPath("$.value[1].status").value("Ready"));
    }

    @Test
    void rejectsWholeUploadWhenALaterPartHasInvalidHeaders() throws Exception {
        mockMvc.perform(upload(workbook("first", "xlsx", "LICENSE NAME", "MIT"))
                        .file(workbook("second", "xls", "UNKNOWN COLUMN", "BSD-3-Clause")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(false))
                .andExpect(jsonPath("$.value").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"xls", "XLS", "Xls", "xlsx", "XLSX", "Xlsx"})
    void preservesRowsAfterGaps(String extension) throws Exception {
        mockMvc.perform(upload(sparseWorkbook(extension, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(true))
                .andExpect(jsonPath("$.value.length()").value(2))
                .andExpect(jsonPath("$.value[0].license.licenseName").value("MIT"))
                .andExpect(jsonPath("$.value[1].license.licenseName").value("BSD-3-Clause"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"xls", "XLS", "Xls", "xlsx", "XLSX", "Xlsx"})
    void skipsMissingLicenseNameWithoutShiftingIdentifier(String extension) throws Exception {
        mockMvc.perform(upload(sparseWorkbook(extension, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value(true))
                .andExpect(jsonPath("$.value.length()").value(1))
                .andExpect(jsonPath("$.value[0].license.licenseName").value("BSD-3-Clause"))
                .andExpect(jsonPath("$.value[0].license.shortIdentifier").value("BSD-3-Clause"));
    }

    private MockMultipartFile sparseWorkbook(String extension, boolean missingName) throws Exception {
        try (Workbook workbook = "xls".equalsIgnoreCase(extension) ? new HSSFWorkbook() : new XSSFWorkbook();
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Licenses");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("LICENSE NAME");
            header.createCell(1).setCellValue("SPDX SHORT IDENTIFIER");
            Row first = sheet.createRow(missingName ? 1 : 2);
            if (!missingName) first.createCell(0).setCellValue("MIT");
            first.createCell(1).setCellValue("Apache-2.0");
            if (!missingName) sheet.createRow(3); // Defined but empty row, in addition to absent row 1.
            Row last = sheet.createRow(missingName ? 2 : 5);
            last.createCell(0).setCellValue("BSD-3-Clause");
            last.createCell(1).setCellValue("BSD-3-Clause");
            workbook.write(bytes);
            return new MockMultipartFile("sparse", "sparse." + extension,
                    "application/octet-stream", bytes.toByteArray());
        }
    }

    @ParameterizedTest
    @MethodSource("headerCases")
    void preservesActualHeaderColumns(String extension, int nameColumn, int identifierColumn,
                                     boolean missingName) throws Exception {
        byte[] data;
        try (Workbook book = "xls".equalsIgnoreCase(extension) ? new HSSFWorkbook() : new XSSFWorkbook();
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            Sheet sheet = book.createSheet("Licenses");
            Row header = sheet.createRow(0);
            header.createCell(nameColumn).setCellValue("LICENSE NAME");
            header.createCell(identifierColumn).setCellValue("SPDX SHORT IDENTIFIER");
            Row row = sheet.createRow(1);
            if (!missingName) row.createCell(nameColumn).setCellValue("Example License");
            row.createCell(identifierColumn).setCellValue("MIT");
            book.write(bytes);
            data = bytes.toByteArray();
        }
        org.springframework.test.web.servlet.ResultActions result = mockMvc.perform(upload(
                new MockMultipartFile("headers", "headers." + extension, "application/octet-stream", data)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.res").value(true))
                .andExpect(jsonPath("$.value.length()").value(missingName ? 0 : 1));
        if (!missingName) {
            result.andExpect(jsonPath("$.value[0].license.licenseName").value("Example License"))
                    .andExpect(jsonPath("$.value[0].license.shortIdentifier").value("MIT"))
                    .andExpect(jsonPath("$.value[0].status").value("Ready"));
        }
    }

    static Stream<Arguments> headerCases() {
        return Stream.of("xls", "XLS", "Xls", "xlsx", "XLSX", "Xlsx")
                .flatMap(ext -> Stream.of(Arguments.of(ext, 1, 2, false),
                        Arguments.of(ext, 0, 2, false), Arguments.of(ext, 1, 2, true),
                        Arguments.of(ext, 2, 0, true)));
    }

    static Stream<Arguments> workbookSizeCases() {
        return Stream.of("xls", "XLS", "Xls", "xlsx", "XLSX", "Xlsx")
                .flatMap(extension -> Stream.of(
                        Arguments.of(extension, (long) CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT),
                        Arguments.of(extension, CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT + 1L)));
    }

    private MockMultipartHttpServletRequestBuilder upload(MockMultipartFile file) {
        MockMultipartHttpServletRequestBuilder request = multipart(Url.LICENSE.CSV_FILE).file(file);
        request.header("User-Agent", "Mozilla/5.0");
        return request;
    }

    private MockMultipartFile workbook(String name, String extension, String header, String... licenses)
            throws Exception {
        try (Workbook workbook = "xls".equalsIgnoreCase(extension) ? new HSSFWorkbook() : new XSSFWorkbook();
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Licenses");
            sheet.createRow(0).createCell(0).setCellValue(header);
            // Only valid fixtures include another recognized header.
            if ("LICENSE NAME".equals(header)) {
                sheet.getRow(0).createCell(1).setCellValue("SPDX SHORT IDENTIFIER");
            }
            for (int i = 0; i < licenses.length; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(licenses[i]);
                row.createCell(1).setCellValue(licenses[i]);
            }
            workbook.write(bytes);
            return new MockMultipartFile(name, name + "." + extension,
                    "application/octet-stream", bytes.toByteArray());
        }
    }
}
