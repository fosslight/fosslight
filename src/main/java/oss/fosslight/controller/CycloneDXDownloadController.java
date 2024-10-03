/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.File;
import java.util.HashMap;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.CYCLONEDXDOWNLOAD;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.ExcelDownLoadUtil;

@Controller
@Slf4j
public class CycloneDXDownloadController extends CoTopComponent {
	@Resource private Environment env;
	private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;
	@PostConstruct
	public void setResourcePathPrefix(){
		RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	}

	@Autowired ProjectService projectService;
	@Autowired FileService fileService;
	@Autowired CommentService commentService;
	
	@PostMapping(value =CYCLONEDXDOWNLOAD.CYCLONEDX_POST)
	public @ResponseBody ResponseEntity<Object> getCycloneDXPost(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String downloadId = null;
		String rtnMsg = "";
		
		try {
			String type = (String)map.get("type");
			String prjId = (String)map.get("prjId");
			String dataStr = "";
			if (map.containsKey("dataStr")) {
				dataStr = (String)map.get("dataStr");;
			}
			Project prjBean = null;
			
			boolean partnerIdCheckFlag = prjId.startsWith("3rd_") ? true : false;
			if (!partnerIdCheckFlag) {
				prjBean = projectService.getProjectBasicInfo(prjId);
			}
			
			if ("cycloneDXJson".equals(type)) {
				if (!partnerIdCheckFlag && (prjBean != null && !isEmpty(prjBean.getCdxJsonFileId()))) {
					downloadId = prjBean.getCdxJsonFileId();
				} else {
					String fileId = ExcelDownLoadUtil.getExcelDownloadId(type, prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX, !isEmpty(dataStr) ? "verify" : "bom");
					
					T2File jsonFile = fileService.selectFileInfo(fileId);
					String jsonFullPath = jsonFile.getLogiPath();
					
					if (!jsonFullPath.endsWith("/")) {
						jsonFullPath += "/";
					}
					
					jsonFullPath += jsonFile.getLogiNm();
					
					try {
						File cycloneDXJsonFile = new File(jsonFullPath);
						if (cycloneDXJsonFile.exists() && cycloneDXJsonFile.length() > 0) {
							downloadId = fileId;
						} else {
							rtnMsg = getMessage("cyclonedx.json.failure");
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} else if ("cycloneDXXml".equals(type)) {
				if (!partnerIdCheckFlag && (prjBean != null && !isEmpty(prjBean.getCdxXmlFileId()))) {
					downloadId = prjBean.getCdxXmlFileId();
				} else {
					String fileId = ExcelDownLoadUtil.getExcelDownloadId(type, prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX, !isEmpty(dataStr) ? "verify" : "bom");
					
					T2File xmlFile = fileService.selectFileInfo(fileId);
					String xmlFullPath = xmlFile.getLogiPath();
					
					if (!xmlFullPath.endsWith("/")) {
						xmlFullPath += "/";
					}
					
					xmlFullPath += xmlFile.getLogiNm();
					
					try {
						File cycloneDXXmlFile = new File(xmlFullPath);
						if (cycloneDXXmlFile.exists() && cycloneDXXmlFile.length() > 0) {
							downloadId = fileId;
						} else {
							rtnMsg = getMessage("cyclonedx.xml.failure");
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} else {
				log.error("not match type...");
				
				return makeJsonResponseHeader(false, "not match type...");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			return makeJsonResponseHeader(false, e.getMessage());
		}

		if (downloadId != null) {
			return makeJsonResponseHeader(downloadId);
		} else {
			return makeJsonResponseHeader(false, rtnMsg);
		}
	}
	
	@ResponseBody
	@GetMapping(value = CYCLONEDXDOWNLOAD.FILE)
	public ResponseEntity<FileSystemResource> getFile (
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		T2File fileInfo = fileService.selectFileInfo(req.getParameter("id"));
		String filePath = fileInfo.getLogiPath();
		
		if (!filePath.endsWith("/")) {
			filePath += "/";
		}
		
		filePath += fileInfo.getLogiNm();
		
		return excelToResponseEntity(filePath, fileInfo.getOrigNm());
	}
}
