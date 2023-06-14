/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.File;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import oss.fosslight.common.Url.SPDXDOWNLOAD;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.SPDXUtil2;

@Controller
@Slf4j
public class SPDXDownloadController extends CoTopComponent {
	@Resource private Environment env;
	private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;
	@PostConstruct
	public void setResourcePathPrefix(){
		RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	}

	@Autowired ProjectService projectService;
	@Autowired FileService fileService;
	@Autowired CommentService commentService;
	
	@PostMapping(value =SPDXDOWNLOAD.SPDX_POST)
	public @ResponseBody ResponseEntity<Object> getExcelPost(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String downloadId = null;
		
		try {
			String spdxType = (String)map.get("type");
			String dataStr = (String)map.get("dataStr");
			String prjId = (String)map.get("prjId");
			Project prjBean = null;
			
			boolean partnerIdCheckFlag = prjId.startsWith("3rd_") ? true : false;
			if (!partnerIdCheckFlag) {
				prjBean = projectService.getProjectBasicInfo(prjId);
			}
			
			if ("spdxRdf".equals(spdxType)) {
				if (!partnerIdCheckFlag && (prjBean != null && !isEmpty(prjBean.getSpdxRdfFileId()))) {
					downloadId = prjBean.getSpdxRdfFileId();
				} else {
					String sheetFileId = "";
					if (dataStr.equals("spdx_sbom")) {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_sbom", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					} else {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					}
					 
					T2File sheetFile = fileService.selectFileInfo(sheetFileId);
					String sheetFullPath = sheetFile.getLogiPath();
					
					if (!sheetFullPath.endsWith("/")) {
						sheetFullPath += "/";
					}
					
					sheetFullPath += sheetFile.getLogiNm();
					String rdfFullPath = sheetFile.getLogiPath();
					
					if (!rdfFullPath.endsWith("/")) {
						rdfFullPath += "/";
					}
					
					rdfFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm())+".rdf";
					
					SPDXUtil2.convert(prjId, sheetFullPath, rdfFullPath);
					
					downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm())+".rdf", 
							FilenameUtils.getBaseName(sheetFile.getLogiNm())+".rdf");
					
					try {
						File spdxRdfFile = new File(rdfFullPath);
						
						if (spdxRdfFile.exists() && spdxRdfFile.length() <= 0) {
							CommentsHistory commHisBean = new CommentsHistory();
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
							commHisBean.setReferenceId(prjId);
							commHisBean.setContents(getMessage("spdx.rdf.failure"));
							commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
							
							commentService.registComment(commHisBean);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} else if ("spdxTag".equals(spdxType)) {
				if (!partnerIdCheckFlag && (prjBean != null && !isEmpty(prjBean.getSpdxTagFileId()))) {
					downloadId = prjBean.getSpdxTagFileId();
				} else {
					String sheetFileId = "";
					if (dataStr.equals("spdx_sbom")) {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_sbom", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					} else {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					}
					T2File sheetFile = fileService.selectFileInfo(sheetFileId);
					String sheetFullPath = sheetFile.getLogiPath();
					
					if (!sheetFullPath.endsWith("/")) {
						sheetFullPath += "/";
					}
					
					sheetFullPath += sheetFile.getLogiNm();
					
					String tagFullPath = sheetFile.getLogiPath();
					
					if (!tagFullPath.endsWith("/")) {
						tagFullPath += "/";
					}
					
					tagFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm())+".tag";
					
					SPDXUtil2.convert(prjId, sheetFullPath, tagFullPath);
					
					downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm())+".tag", 
							FilenameUtils.getBaseName(sheetFile.getLogiNm())+".tag");
					
					try {
						File spdxTafFile = new File(tagFullPath);
						
						if (spdxTafFile.exists() && spdxTafFile.length() <= 0) {
							CommentsHistory commHisBean = new CommentsHistory();
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
							commHisBean.setReferenceId(prjId);
							commHisBean.setContents(getMessage("spdx.tag.failure"));
							commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
							commentService.registComment(commHisBean);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} else if ("spdxJson".equals(spdxType)) {
				if (!partnerIdCheckFlag && (prjBean != null && !isEmpty(prjBean.getSpdxJsonFileId()))) {
					downloadId = prjBean.getSpdxJsonFileId();
				} else {
					String sheetFileId = "";
					if (dataStr.equals("spdx_sbom")) {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_sbom", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					} else {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					}
					T2File sheetFile = fileService.selectFileInfo(sheetFileId);
					String sheetFullPath = sheetFile.getLogiPath();

					if (!sheetFullPath.endsWith("/")) {
						sheetFullPath += "/";
					}

					sheetFullPath += sheetFile.getLogiNm();
					String jsonFullPath = sheetFile.getLogiPath();

					if (!jsonFullPath.endsWith("/")) {
						jsonFullPath += "/";
					}

					jsonFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".json";

					SPDXUtil2.convert(prjId, sheetFullPath, jsonFullPath);

					downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm()) + ".json",
							FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".json");

					try {
						File spdxJsonFile = new File(jsonFullPath);

						if (spdxJsonFile.exists() && spdxJsonFile.length() <= 0) {
							CommentsHistory commHisBean = new CommentsHistory();
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
							commHisBean.setReferenceId(prjId);
							commHisBean.setContents(getMessage("spdx.json.failure"));
							commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
							commentService.registComment(commHisBean);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} else if ("spdxYaml".equals(spdxType)) {
				if (!partnerIdCheckFlag && (prjBean != null && !isEmpty(prjBean.getSpdxYamlFileId()))) {
					downloadId = prjBean.getSpdxYamlFileId();
				} else {
					String sheetFileId = "";
					if (dataStr.equals("spdx_sbom")) {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_sbom", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					} else {
						sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					}
					T2File sheetFile = fileService.selectFileInfo(sheetFileId);
					String sheetFullPath = sheetFile.getLogiPath();

					if (!sheetFullPath.endsWith("/")) {
						sheetFullPath += "/";
					}

					sheetFullPath += sheetFile.getLogiNm();
					String yamlFullPath = sheetFile.getLogiPath();

					if (!yamlFullPath.endsWith("/")) {
						yamlFullPath += "/";
					}

					yamlFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".yaml";

					SPDXUtil2.convert(prjId, sheetFullPath, yamlFullPath);

					downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm()) + ".yaml",
							FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".yaml");

					try {
						File spdxYamlFile = new File(yamlFullPath);

						if (spdxYamlFile.exists() && spdxYamlFile.length() <= 0) {
							CommentsHistory commHisBean = new CommentsHistory();
							commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
							commHisBean.setReferenceId(prjId);
							commHisBean.setContents(getMessage("spdx.yaml.failure"));
							commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
							commentService.registComment(commHisBean);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} else if ("spdx".equals(spdxType)){
				if (!partnerIdCheckFlag && (prjBean != null && !isEmpty(prjBean.getSpdxSheetFileId()))) {
					downloadId = prjBean.getSpdxSheetFileId();
				} else {
					if (dataStr.equals("spdx_sbom")) {
						downloadId = ExcelDownLoadUtil.getExcelDownloadId("spdx_sbom", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
					} else {
						downloadId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
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

		return makeJsonResponseHeader(downloadId);
	}
	
	@ResponseBody
	@GetMapping(value = SPDXDOWNLOAD.FILE)
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
	
	@PostMapping(value =SPDXDOWNLOAD.SELFCHECK_SPDX_POST)
	public @ResponseBody ResponseEntity<Object> getSelfcheckSpdxPost(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String downloadId = null;
		
		try {
			String spdxType = (String)map.get("type");
			String dataStr = (String)map.get("dataStr");
			String prjId = (String)map.get("prjId");
			
			if ("spdxRdf".equals(spdxType)) {
				String sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_self", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
				T2File sheetFile = fileService.selectFileInfo(sheetFileId);
				String sheetFullPath = sheetFile.getLogiPath();
				
				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}
				
				sheetFullPath += sheetFile.getLogiNm();
				String rdfFullPath = sheetFile.getLogiPath();
				
				if (!rdfFullPath.endsWith("/")) {
					rdfFullPath += "/";
				}
				
				rdfFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm())+".rdf";
				
				SPDXUtil2.convert(prjId, sheetFullPath, rdfFullPath);
				
				downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm())+".rdf", 
						FilenameUtils.getBaseName(sheetFile.getLogiNm())+".rdf");
				
				try {
					File spdxRdfFile = new File(rdfFullPath);
					
					if (spdxRdfFile.exists() && spdxRdfFile.length() <= 0) {
						CommentsHistory commHisBean = new CommentsHistory();
						commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
						commHisBean.setReferenceId(prjId);
						commHisBean.setContents(getMessage("spdx.rdf.failure"));
						commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
						
						commentService.registComment(commHisBean);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else if ("spdxTag".equals(spdxType)) {
				String sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_self", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
				T2File sheetFile = fileService.selectFileInfo(sheetFileId);
				String sheetFullPath = sheetFile.getLogiPath();
				
				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}
				
				sheetFullPath += sheetFile.getLogiNm();
				
				String tagFullPath = sheetFile.getLogiPath();
				
				if (!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}
				
				tagFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm())+".tag";
				
				SPDXUtil2.convert(prjId, sheetFullPath, tagFullPath);
				
				downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm())+".tag", 
						FilenameUtils.getBaseName(sheetFile.getLogiNm())+".tag");
				
				try {
					File spdxTafFile = new File(tagFullPath);
					
					if (spdxTafFile.exists() && spdxTafFile.length() <= 0) {
						CommentsHistory commHisBean = new CommentsHistory();
						commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
						commHisBean.setReferenceId(prjId);
						commHisBean.setContents(getMessage("spdx.tag.failure"));
						commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
						commentService.registComment(commHisBean);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else if ("spdxJson".equals(spdxType)) {
				String sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_self", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
				T2File sheetFile = fileService.selectFileInfo(sheetFileId);
				String sheetFullPath = sheetFile.getLogiPath();

				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}

				sheetFullPath += sheetFile.getLogiNm();
				String jsonFullPath = sheetFile.getLogiPath();

				if (!jsonFullPath.endsWith("/")) {
					jsonFullPath += "/";
				}

				jsonFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".json";

				SPDXUtil2.convert(prjId, sheetFullPath, jsonFullPath);

				downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm()) + ".json",
						FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".json");

				try {
					File spdxJsonFile = new File(jsonFullPath);

					if (spdxJsonFile.exists() && spdxJsonFile.length() <= 0) {
						CommentsHistory commHisBean = new CommentsHistory();
						commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
						commHisBean.setReferenceId(prjId);
						commHisBean.setContents(getMessage("spdx.json.failure"));
						commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
						commentService.registComment(commHisBean);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else if ("spdxYaml".equals(spdxType)) {
				String sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx_self", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
				T2File sheetFile = fileService.selectFileInfo(sheetFileId);
				String sheetFullPath = sheetFile.getLogiPath();

				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}

				sheetFullPath += sheetFile.getLogiNm();
				String yamlFullPath = sheetFile.getLogiPath();

				if (!yamlFullPath.endsWith("/")) {
					yamlFullPath += "/";
				}

				yamlFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".yaml";

				SPDXUtil2.convert(prjId, sheetFullPath, yamlFullPath);

				downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm()) + ".yaml",
						FilenameUtils.getBaseName(sheetFile.getLogiNm()) + ".yaml");

				try {
					File spdxYamlFile = new File(yamlFullPath);

					if (spdxYamlFile.exists() && spdxYamlFile.length() <= 0) {
						CommentsHistory commHisBean = new CommentsHistory();
						commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
						commHisBean.setReferenceId(prjId);
						commHisBean.setContents(getMessage("spdx.yaml.failure"));
						commHisBean.setStatus(null); // 일반적인 comment에는 status를 넣지 않음.
						commentService.registComment(commHisBean);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else if ("spdx".equals(spdxType)){
				downloadId = ExcelDownLoadUtil.getExcelDownloadId("spdx_self", dataStr, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
			} else {
				log.error("not match type...");
				
				return makeJsonResponseHeader(false, "not match type...");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			return makeJsonResponseHeader(false, e.getMessage());
		}

		return makeJsonResponseHeader(downloadId);
	}
}
