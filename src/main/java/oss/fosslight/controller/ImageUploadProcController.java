/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.IMAGE_UPLOAD;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.service.FileService;

@Controller
public class ImageUploadProcController extends CoTopComponent {
	@Autowired FileService fileService;

	@PostMapping(value=IMAGE_UPLOAD.UPLOAD)
	public void imageUpload (
			HttpServletRequest req, HttpServletResponse res) throws IOException{
		PrintWriter printWriter = null;	
		String callback = req.getParameter("CKEditorFuncNum");
		T2File fileInfo = new T2File();
		fileInfo.setGubn(CoConstDef.FILE_GUBUN_EDITOR_IMAGE);
		List<UploadFile> fList = fileService.uploadFile(req, fileInfo);
		
		if (fList != null && !fList.isEmpty()) {
			for (UploadFile f : fList) {
				if (f.isUploadSucc()) {
					res.setCharacterEncoding("utf-8");
					res.setContentType("text/html;charset=utf-8");
					printWriter = res.getWriter();
					String _host = req.getScheme() + "://" + req.getServerName();
					
					if (req.getServerPort() > 0) {
						int _port = req.getServerPort();
						
						if (80 != _port && 443 != _port) {
							_host += ":" + String.valueOf(_port);
						}
					}
					
					String fileUrl = _host + "/download/" + f.getRegistSeq() + "/" + f.getFileName();
					printWriter.println("<script type='text/javascript'>window.parent.CKEDITOR.tools.callFunction("
							+ callback + ",'" + fileUrl + "','Success'" + ")</script>");
					printWriter.flush();
					
					break;
				}
			}
		}
	}

	@PostMapping(value=IMAGE_UPLOAD.UPLOAD2)
	public ResponseEntity<Object> imageUpload2 (
			HttpServletRequest req, HttpServletResponse res) throws IOException{
		T2File fileInfo = new T2File();
		fileInfo.setGubn(CoConstDef.FILE_GUBUN_EDITOR_IMAGE);
		List<UploadFile> fList = fileService.uploadFile(req, fileInfo);
		Map<String, Object> resultMap = new HashMap<>();
		
		if (fList != null && !fList.isEmpty()) {
			for (UploadFile f : fList) {
				if (f.isUploadSucc()) {
					resultMap.put("uploaded", 1);
					resultMap.put("fileName", f.getOriginalFilename());
					String _host = req.getScheme() + "://" + req.getServerName();
					
					if (req.getServerPort() > 0) {
						int _port = req.getServerPort();
						
						if (80 != _port && 443 != _port) {
							_host += ":" + String.valueOf(_port);
						}
					}
					
					resultMap.put("url",  _host + "/imageView/" + f.getFileName());
					
					break;
				}
			}
		}
		
		if (resultMap.isEmpty()) {
			resultMap.put("uploaded", 0);
		}
		
		return makeJsonResponseHeader(resultMap);
	}

}