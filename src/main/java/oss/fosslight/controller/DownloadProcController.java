/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.DOWNLOAD;
import oss.fosslight.domain.T2File;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.service.FileService;

@Controller
public class DownloadProcController extends CoTopComponent {
	@Autowired FileService fileService;
	
	@Autowired FileMapper fileMapper;
	
	@GetMapping(value=DOWNLOAD.SEQ_FNAME)
	public ResponseEntity<FileSystemResource> downloadComponent (
			@PathVariable("seq") final String seq, 
			@PathVariable("fName") final String fName,
			HttpServletRequest req, HttpServletResponse res, Model model) throws IOException{
		ResponseEntity<FileSystemResource> responseEntity = null;
		
		T2File fileVo = new T2File();
		fileVo.setFileSeq(seq);
		fileVo = fileMapper.getFileInfo(fileVo);
		
		if (fileVo == null){
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Long.toString(0));
			responseEntity = new ResponseEntity<FileSystemResource>(null, responseHeaders, HttpStatus.NOT_FOUND);
		} else {
			String origNm = fileVo.getOrigNm();
			String logiNm = fileVo.getLogiNm();
			String downName = "";
			
			// 파일명이 같을 경우만 다운로드되도록 한다.
			if (logiNm.equals(fName)){
				//파일 인코딩
				String browser = req.getHeader("User-Agent");
				
		        if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Chrome")){
		            downName = URLEncoder.encode(origNm,"UTF-8").replaceAll("\\+", "%20");
		        } else {
		            downName = new String(origNm.getBytes("UTF-8"), "ISO-8859-1");
		        }
		        
		        String logiPath = fileVo.getLogiPath();
		        String fullLogiPath = null;
		        
		        if (logiPath.substring(logiPath.length()-1).equals("/")){
		        	fullLogiPath = fileVo.getLogiPath() + fileVo.getLogiNm();
		        } else {
		        	fullLogiPath = fileVo.getLogiPath() + "/" + fileVo.getLogiNm();
		        }
			    
				java.io.File file = new java.io.File(fullLogiPath);
			    FileSystemResource fileSystemResource = new FileSystemResource(file);
			    HttpHeaders responseHeaders = new HttpHeaders();
			    
			    responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
			    responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + downName + ";filename*= UTF-8''" + downName);
			    responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Long.toString(fileSystemResource.contentLength()));
			    responseEntity = new ResponseEntity<FileSystemResource>(fileSystemResource, responseHeaders, HttpStatus.OK);
			} else { // 파일명이 다를 경우 404를 리턴한다.
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Long.toString(0));
				responseEntity = new ResponseEntity<FileSystemResource>(null, responseHeaders, HttpStatus.NOT_FOUND);
			}
		}
		
		return responseEntity;
	}
	
	@GetMapping(value={DOWNLOAD.BATGUIREPORT_ID_CHECKSUM})
	public ResponseEntity<FileSystemResource> downloadBatGuiReport (
			@PathVariable("batId") final String batId, 
			@PathVariable("checkSum") final String checkSum,
			HttpServletRequest req, HttpServletResponse res, Model model) throws IOException{
		ResponseEntity<FileSystemResource> responseEntity = null;
		String baseImageUrl = "http://" + req.getServerName();
		
		if (req.getServerPort() != 80) {
			baseImageUrl += ":" + Integer.toString(req.getServerPort());
		}
		
		baseImageUrl += "/imageView/guiReport/" + batId + "/";
		// bat reposrt path
		String fileName = checkSum + "-guireport";
		String filePath  = CommonFunction.emptyCheckProperty("vat.root.path", CommonFunction.getProperty("root.dir") + "/batsystem/");
		filePath = filePath + "out/" + batId + "_dir" + "/reports/" + fileName + ".html";
		File file = new File(filePath);
		
		// 파일명이 같을 경우만 다운로드되도록 한다.
		if (file.exists() && file.isFile()){
			fileName+=".html";
		    String encodedFilename = URLEncoder.encode(fileName,"UTF-8").replace("+", "%20");
		    File tempFile = Paths.get(CommonFunction.emptyCheckProperty("image.temp.path", "/imagetemp")).resolve("BAT").resolve(batId).resolve(file.getName()).toFile();
		   
		    try {
		    	if (!tempFile.exists()) {
		    		String content = FileUtils.readFileToString(file, "UTF-8");
		    		content = content.replaceAll("img src=\"../images/", "img src=\""+baseImageUrl+"");
		    		
		    		FileUtils.writeStringToFile(tempFile, content, "UTF-8");
		    	}
		    } catch (IOException e) {
		    	
		    }
		    
		    FileSystemResource fileSystemResource = new FileSystemResource(tempFile);
		    HttpHeaders responseHeaders = new HttpHeaders();
		    
		    responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
		    responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodedFilename + ";filename*= UTF-8''" + encodedFilename);
		    responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Long.toString(fileSystemResource.contentLength()));
		    
		    responseEntity = new ResponseEntity<FileSystemResource>(fileSystemResource, responseHeaders, HttpStatus.OK);
		} else { // 파일명이 다를 경우 404를 리턴한다.
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add(HttpHeaders.CONTENT_LENGTH, Long.toString(0));
			responseEntity = new ResponseEntity<FileSystemResource>(null, responseHeaders, HttpStatus.NOT_FOUND);
		}
	
		return responseEntity;
	}
}
