/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.EXCELDOWNLOAD;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.Statistics;
import oss.fosslight.domain.T2File;
import oss.fosslight.service.FileService;
import oss.fosslight.util.ExcelDownLoadUtil;

@Controller
@Slf4j
public class ExcelDownloadController extends CoTopComponent {
	@Resource private Environment env;
	private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;
	@PostConstruct
	public void setResourcePathPrefix(){
		RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	}
	
	@Autowired FileService fileService;

	@PostMapping(value = EXCELDOWNLOAD.EXCEL_POST)
	public @ResponseBody ResponseEntity<Object> getExcelPost(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String downloadId = null;
		
		try {
			if (map.containsKey("extParam")) {
				downloadId = ExcelDownLoadUtil.getExcelDownloadId((String)map.get("type"), (String)map.get("parameter"), RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX, (String)map.get("extParam"));				
			} else {
				downloadId = ExcelDownLoadUtil.getExcelDownloadId((String)map.get("type"), (String)map.get("parameter"), RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
			}
			
			if (isEmpty(downloadId)){
				return makeJsonResponseHeader(false, "overflow");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}

		return makeJsonResponseHeader(downloadId);
	}
	
	@PostMapping(value = EXCELDOWNLOAD.EXCEL_POST_OSS)
	public @ResponseBody ResponseEntity<Object> getExcelPostOss(@ModelAttribute OssMaster ossMaster,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String downloadId = null;
				
		try {
			downloadId = ExcelDownLoadUtil.getExcelDownloadIdOss("OSS", ossMaster , RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
			
			if (isEmpty(downloadId)){
				return makeJsonResponseHeader(false, "overflow");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}

		return makeJsonResponseHeader(downloadId);
	}
	
	
	@ResponseBody
	@GetMapping(value = EXCELDOWNLOAD.FILE,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public ResponseEntity<FileSystemResource> getFile (
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		T2File fileInfo = fileService.selectFileInfo(req.getParameter("id"));
		
		return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
	}
	
	@GetMapping(value = EXCELDOWNLOAD.CHART_EXCEL)
	public @ResponseBody ResponseEntity<Object> getChartExcel(@ModelAttribute Statistics params,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		
		String downloadId = null;
		
		try {
			downloadId = ExcelDownLoadUtil.getChartExcel(params, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
			
			if (isEmpty(downloadId)){
				return makeJsonResponseHeader(false, "overflow");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}

		return makeJsonResponseHeader(downloadId);
	}
}
