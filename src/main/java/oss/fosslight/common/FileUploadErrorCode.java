/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.common;

/**
 * Error codes returned by the project report upload endpoints.
 */
public enum FileUploadErrorCode {
	FILE_EMPTY("FILE_EMPTY", "msg.file.upload.empty", "The file name or uploaded file is empty."),
	FILE_SIZE_ZERO("FILE_SIZE_ZERO", "msg.file.upload.size.zero", "The uploaded file is empty."),
	FILE_SIZE_LIMIT_OVER("FILE_SIZE_LIMIT_OVER", "msg.file.upload.size.limit", "The uploaded file exceeds the allowed size."),
	UNSUPPORTED_FILE_EXTENSION("UNSUPPORTED_FILE", "msg.file.upload.unsupported.extension", "The file extension is not supported."),

	SPDX_INVALID_FORMAT("SPDX_INVALID_FORMAT", "msg.file.upload.spdx.invalid.format", "The file is not a valid SPDX document."),
	SPDX_MISSING_VERSION("SPDX_MISSING_VERSION", "msg.file.upload.spdx.missing.version", "The SPDXVersion field is missing."),
	SPDX_YAML_PARSE_ERROR("SPDX_YAML_PARSE_ERROR", "msg.file.upload.spdx.yaml.parse", "The SPDX YAML file could not be parsed."),
	SPDX_JSON_PARSE_ERROR("SPDX_JSON_PARSE_ERROR", "msg.file.upload.spdx.json.parse", "The SPDX JSON file could not be parsed."),
	SPDX_RDF_PARSE_ERROR("SPDX_RDF_PARSE_ERROR", "msg.file.upload.spdx.rdf.parse", "The SPDX RDF/XML file could not be parsed."),
	SPDX_TAG_VALUE_PARSE_ERROR("SPDX_TAG_VALUE_PARSE_ERROR", "msg.file.upload.spdx.tag.parse", "The SPDX Tag-Value file could not be parsed."),
	SPDX_CONVERSION_FAILED("SPDX_CONVERSION_FAILED", "msg.file.upload.spdx.conversion", "The SPDX file could not be converted to the FOSSLight format."),
	SPDX_NO_PACKAGE_INFO("SPDX_NO_PACKAGE_INFO", "msg.file.upload.spdx.no.package", "The SPDX document does not contain package information."),

	CDX_INVALID_FORMAT("CDX_INVALID_FORMAT", "msg.file.upload.cdx.invalid.format", "The file is not a valid CycloneDX document."),
	CDX_JSON_PARSE_ERROR("CDX_JSON_PARSE_ERROR", "msg.file.upload.cdx.json.parse", "The CycloneDX JSON file could not be parsed."),
	CDX_XML_PARSE_ERROR("CDX_XML_PARSE_ERROR", "msg.file.upload.cdx.xml.parse", "The CycloneDX XML file could not be parsed."),
	CDX_MISSING_BOM_FORMAT("CDX_MISSING_BOM_FORMAT", "msg.file.upload.cdx.missing.bom.format", "The CycloneDX bomFormat field is missing or invalid."),
	CDX_CONVERSION_FAILED("CDX_CONVERSION_FAILED", "msg.file.upload.cdx.conversion", "The CycloneDX file could not be converted to the FOSSLight format."),
	CDX_NO_COMPONENTS("CDX_NO_COMPONENTS", "msg.file.upload.cdx.no.components", "The CycloneDX document does not contain components."),

	EXCEL_INVALID_FORMAT("EXCEL_INVALID_FORMAT", "msg.file.upload.excel.invalid.format", "The uploaded file is not a valid Excel file."),
	EXCEL_PASSWORD_PROTECTED("EXCEL_PASSWORD_PROTECTED", "msg.file.upload.excel.password", "The Excel file is password protected."),
	EXCEL_CORRUPTED("EXCEL_CORRUPTED", "msg.file.upload.excel.corrupted", "The Excel file is corrupted."),
	EXCEL_NO_SHEET("EXCEL_NO_SHEET", "msg.file.upload.excel.no.sheet", "The Excel file does not contain a worksheet."),
	EXCEL_MISSING_HEADER("EXCEL_MISSING_HEADER", "msg.file.upload.excel.missing.header", "The required Excel header is missing."),
	EXCEL_DUPLICATE_HEADER("EXCEL_DUPLICATE_HEADER", "msg.file.upload.excel.duplicate.header", "The Excel file contains duplicate headers."),
	EXCEL_EMPTY_SHEET("EXCEL_EMPTY_SHEET", "msg.file.upload.excel.empty.sheet", "The Excel worksheet is empty."),
	EXCEL_REQUIRED_COLUMN_MISSING("EXCEL_REQUIRED_COLUMN_MISSING", "msg.file.upload.excel.required.column", "A required Excel column is missing."),

	CSV_INVALID_DELIMITER("CSV_INVALID_DELIMITER", "msg.file.upload.csv.delimiter", "The CSV file must use tab delimiters."),
	CSV_ENCODING_ERROR("CSV_ENCODING_ERROR", "msg.file.upload.csv.encoding", "The CSV file encoding could not be read."),

	FILE_COPY_FAILED("FILE_COPY_FAILED", "msg.file.upload.copy.failed", "The uploaded file could not be copied."),
	FILE_CONVERSION_FAILED("FILE_CONVERSION_FAILED", "msg.file.upload.conversion.failed", "The uploaded file could not be converted."),
	FILE_SAVE_FAILED("FILE_SAVE_FAILED", "msg.file.upload.save.failed", "The uploaded file could not be saved."),
	TEMP_FILE_CREATE_FAILED("TEMP_FILE_CREATE_FAILED", "msg.file.upload.temp.failed", "A temporary file could not be created."),
	SYSTEM_ERROR("SYSTEM_ERROR", "msg.file.upload.system.error", "An unexpected error occurred while processing the upload.");

	private final String code;
	private final String messageKey;
	private final String defaultMessage;

	FileUploadErrorCode(String code, String messageKey, String defaultMessage) {
		this.code = code;
		this.messageKey = messageKey;
		this.defaultMessage = defaultMessage;
	}

	public String getCode() {
		return code;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	public static FileUploadErrorCode fromCode(String code) {
		if (code == null) {
			return null;
		}

		for (FileUploadErrorCode errorCode : values()) {
			if (errorCode.code.equals(code)) {
				return errorCode;
			}
		}
		return null;
	}
}
