/*
 * Copyright (c) 2021 Dongmin Kang
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import oss.fosslight.CoTopComponent;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.service.FileService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@PropertySources(value = {@PropertySource(value= AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
public class CsvUtil extends CoTopComponent {
	// Service
	private static FileService fileService 		= (FileService) 		getWebappContext().getBean(FileService.class);

	public static Workbook csvFileToExcelWorkbook(File file, String readType) throws IOException{
		CSVParser parser = new CSVParserBuilder()
				.withSeparator('\t')
				.build();

		CSVReader csvReader = new CSVReaderBuilder(new FileReader(file))
				.withCSVParser(parser)
				.build();

		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet(readType);

		int rowIdx = 0;

		String [] nextLine;
		while ((nextLine = csvReader.readNext()) != null) {
			int cellIdx = 0;
			Row row = sheet.createRow(rowIdx);

			for (String token : nextLine) {
				Cell cell = row.createCell(cellIdx); cellIdx++;
				cell.setCellValue(token);
			}

			rowIdx++;
		}

		return wb;
	}
}
