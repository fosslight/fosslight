/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.File;
import java.util.HashMap;

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

	@Autowired ProjectService projectService;
	@Autowired FileService fileService;
	@Autowired CommentService commentService;
	
	@PostMapping(value =SPDXDOWNLOAD.SPDX_POST)
	public @ResponseBody ResponseEntity<Object> getExcelPost(@RequestBody HashMap<String, Object> map,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		String downloadId = null;
		
		try {
			String spdxType = (String)map.get("type");
			String prjId = (String)map.get("parameter");
			Project prjBean = projectService.getProjectBasicInfo(prjId);
			String templatePath = CommonFunction.propertyFlagCheck("checkflag", CoConstDef.FLAG_YES)
					? CommonFunction.emptyCheckProperty("export.template.path", "/template")
					: "template";
					
			if("spdxRdf".equals(spdxType)) {
				if(!isEmpty(prjBean.getSpdxRdfFileId())) {
					downloadId = prjBean.getSpdxRdfFileId();
				} else {
					String sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", prjId, templatePath);
					T2File sheetFile = fileService.selectFileInfo(sheetFileId);
					String sheetFullPath = sheetFile.getLogiPath();
					
					if(!sheetFullPath.endsWith("/")) {
						sheetFullPath += "/";
					}
					
					sheetFullPath += sheetFile.getLogiNm();
					String rdfFullPath = sheetFile.getLogiPath();
					
					if(!rdfFullPath.endsWith("/")) {
						rdfFullPath += "/";
					}
					
					rdfFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm())+".rdf";
					
					SPDXUtil2.spreadsheetToRDF(prjId, sheetFullPath, rdfFullPath);
					
					downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm())+".rdf", 
							FilenameUtils.getBaseName(sheetFile.getLogiNm())+".rdf");
					
					try {
						File spdxRdfFile = new File(rdfFullPath);
						
						if(spdxRdfFile.exists() && spdxRdfFile.length() <= 0) {
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
			} else if("spdxTag".equals(spdxType)) {
				if(!isEmpty(prjBean.getSpdxTagFileId())) {
					downloadId = prjBean.getSpdxTagFileId();
				} else {
					String sheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", prjId, templatePath);
					T2File sheetFile = fileService.selectFileInfo(sheetFileId);
					String sheetFullPath = sheetFile.getLogiPath();
					
					if(!sheetFullPath.endsWith("/")) {
						sheetFullPath += "/";
					}
					
					sheetFullPath += sheetFile.getLogiNm();
					
					String tagFullPath = sheetFile.getLogiPath();
					
					if(!tagFullPath.endsWith("/")) {
						tagFullPath += "/";
					}
					
					tagFullPath += FilenameUtils.getBaseName(sheetFile.getLogiNm())+".tag";
					
					SPDXUtil2.spreadsheetToTAG(prjId, sheetFullPath, tagFullPath);
					
					downloadId = fileService.registFileDownload(sheetFile.getLogiPath(), FilenameUtils.getBaseName(sheetFile.getOrigNm())+".tag", 
							FilenameUtils.getBaseName(sheetFile.getLogiNm())+".tag");
					
					try {
						File spdxTafFile = new File(tagFullPath);
						
						if(spdxTafFile.exists() && spdxTafFile.length() <= 0) {
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
			} else if("spdx".equals(spdxType)){
				if(!isEmpty(prjBean.getSpdxSheetFileId())) {
					downloadId = prjBean.getSpdxSheetFileId();
				} else {
					downloadId = ExcelDownLoadUtil.getExcelDownloadId("spdx", prjId, templatePath);
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
		
		if(!filePath.endsWith("/")) {
			filePath += "/";
		}
		
		filePath += fileInfo.getLogiNm();
		
		return excelToResponseEntity(filePath, fileInfo.getOrigNm());
	}
}
