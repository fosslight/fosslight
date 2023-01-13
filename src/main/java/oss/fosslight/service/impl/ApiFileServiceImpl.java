/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.service.ApiFileService;
import oss.fosslight.util.CompressUtil;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;

@Service("ApiFileService")
@Slf4j
public class ApiFileServiceImpl implements ApiFileService {
	@Autowired FileMapper fileMapper;
	
	@Override
	public UploadFile uploadFile(MultipartFile mFile){
		return uploadFile(mFile, null);
	}
	
	@Override
	public UploadFile uploadFile(MultipartFile mFile, String filePath){
		boolean uploadSucc = false;
		String fileId = StringUtil.avoidNull(fileMapper.getFileId(), "1");//max+1 file Id 가져옴 20160524 ms-kwon
		int indexNum = 0;
		UploadFile upFile = new UploadFile();
		T2File registFile = new T2File();
		
		if (StringUtil.isEmpty(mFile.getOriginalFilename())) {
			throw new RuntimeException("File Name is empty");
		}
		
		if (mFile.getSize() <= 0) {
			throw new RuntimeException("File Size is 0");
		}
		
		String originalFileName = mFile.getOriginalFilename();	//Original File name

		// originalFileName에 경로가 포함되어 있는 경우 처리
		log.debug("File upload OriginalFileName : " + originalFileName);
		
		if (originalFileName.indexOf("/") > -1) {
			originalFileName = originalFileName.substring(originalFileName.lastIndexOf("/") + 1);
			
			log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
		}
		if (originalFileName.indexOf("\\") > -1) {
			originalFileName = originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
			
			log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
		}
		
		String fileExt = FilenameUtils.getExtension(originalFileName);
		
		if (originalFileName.toLowerCase().endsWith(".tgz.gz")) {
			fileExt = "tgz.gz";
		} else if (originalFileName.toLowerCase().endsWith(".tar.bz2")) {
			fileExt = "tar.bz2";
		} else if (originalFileName.toLowerCase().endsWith(".tar.gz")) {
			fileExt = "tar.gz";
		}
		
		String uploadFilePath = "";
		String uploadThumbFilePath = "";
		
		try {
			if (StringUtil.isEmpty(filePath)) {
				uploadFilePath = CommonFunction.emptyCheckProperty("upload.path", "/upload");
				uploadThumbFilePath = CommonFunction.emptyCheckProperty("image.path", "/image");
			} else {
				uploadFilePath = filePath;
				uploadThumbFilePath = filePath + "/" + "thumb";
				
				File packagingFile = new File(filePath);
				
				if (!packagingFile.exists()) {
					packagingFile.mkdirs();
				}
			}
		} catch(Exception e) {
			log.error("file upload path(get properties) : " + e.getMessage());
		}
		
		UUID randomUUID = UUID.randomUUID();
		File file = new File(uploadFilePath+"/"+randomUUID+"."+fileExt);

		/** Return Setting **/
		upFile.setOriginalFilename(originalFileName);
		upFile.setInputName("");
		upFile.setSize(mFile.getSize());
		upFile.setFilePath(uploadFilePath);
		upFile.setFileName(randomUUID+"."+fileExt);
		upFile.setFileExt(fileExt);
		upFile.setIndexNum(indexNum);
		upFile.setRegistFileId(fileId);
		
		try {
			upFile.setContentType(mFile.getContentType());
		} catch (Exception e) {}
		
		/** DB Regist Setting **/
		registFile.setFileId(fileId);
		registFile.setOrigNm(originalFileName);
		registFile.setLogiNm(randomUUID+"."+fileExt);
		registFile.setLogiPath(uploadFilePath);
		registFile.setLogiThumbNm(randomUUID+"_thumb."+fileExt);
		registFile.setLogiThumbPath(uploadThumbFilePath);
		registFile.setExt(fileExt);
		registFile.setSize(mFile.getSize()+"");
		
		try {
			registFile.setContentType(mFile.getContentType());
		} catch (Exception e) {}
		
		upFile.setRegistSeq(registFile(registFile));
		upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
		
		if (mFile.getSize()!=0) { //File Null Check
			if (! file.exists()) { //경로상에 파일이 존재하지 않을 경우
				try {
					if (file.getParentFile() != null && file.getParentFile().mkdirs()) { //경로에 해당하는 디렉토리들을 생성
						boolean upSucc = file.createNewFile(); //이후 파일 생성
						
						if (!upSucc) {
							uploadSucc=false;
						}
					}
				}
				catch (IOException e) {
					log.error("file upload create error : " + e.getMessage());
					
					uploadSucc=false;
				}
			}
			
			uploadSucc = FileUtil.transferTo(mFile, file);
			
			upFile.setUploadSucc(uploadSucc);
		}
		
		return upFile;
	}
	
	@Override
	public Map<String, UploadFile> uploadNoticeXMLFile(MultipartFile mFile, String prjId) {
		Map<String, UploadFile> result = new HashMap<String, UploadFile>();
		String fileId = StringUtil.avoidNull(fileMapper.getFileId(), "1");		
		int indexNum = 0;
		UploadFile upFile = new UploadFile();
		T2File registFile = new T2File();
		boolean uploadSucc = true;
		String fileName = mFile.getOriginalFilename();
		
		if (StringUtil.isEmpty(mFile.getOriginalFilename())) {
			throw new RuntimeException("File Name is empty");
		}
		
		if (mFile.getSize() <= 0) {
			throw new RuntimeException("File Size is 0");
		}
		
		String originalFileName = mFile.getOriginalFilename();	//Original File name

		// originalFileName에 경로가 포함되어 있는 경우 처리
		log.debug("File upload OriginalFileName : " + originalFileName);
		
		if (originalFileName.indexOf("/") > -1) {
			originalFileName = originalFileName.substring(originalFileName.lastIndexOf("/") + 1);
			
			log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
		}
		if (originalFileName.indexOf("\\") > -1) {
			originalFileName = originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
			
			log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
		}
		
		String fileExt = FilenameUtils.getExtension(originalFileName);
		
		if (originalFileName.toLowerCase().endsWith(".tgz.gz")) {
			fileExt = "tgz.gz";
		} else if (originalFileName.toLowerCase().endsWith(".tar.bz2")) {
			fileExt = "tar.bz2";
		} else if (originalFileName.toLowerCase().endsWith(".tar.gz")) {
			fileExt = "tar.gz";
		}
		
		String uploadFilePath = "";
		String uploadThumbFilePath = "";
		
		try {
			uploadFilePath = CommonFunction.emptyCheckProperty("android.upload.path", "/upload/android_notice")+ "/" + prjId;
			uploadThumbFilePath = CommonFunction.emptyCheckProperty("image.path", "/image")+ "/" + prjId + "/thumb";
		} catch(Exception e) {
			log.error("file upload path(get properties) : " + e.getMessage());
		}
		
		UUID randomUUID = UUID.randomUUID();
		File file = new File(uploadFilePath+"/"+randomUUID+"."+fileExt);

		/** Return Setting **/
		upFile.setOriginalFilename(originalFileName);
		upFile.setInputName(fileName);
		upFile.setSize(mFile.getSize());
		upFile.setFilePath(uploadFilePath);
		upFile.setFileName(randomUUID+"."+fileExt);
		upFile.setFileExt(fileExt);
		upFile.setIndexNum(indexNum);
		upFile.setRegistFileId(fileId);
		
		try {
			upFile.setContentType(mFile.getContentType());
		} catch (Exception e) {}
		
		/** DB Regist Setting **/
		registFile.setFileId(fileId);
		registFile.setOrigNm(originalFileName);
		registFile.setLogiNm(randomUUID+"."+fileExt);
		registFile.setLogiPath(uploadFilePath);
		registFile.setLogiThumbNm(randomUUID+"_thumb."+fileExt);
		registFile.setLogiThumbPath(uploadThumbFilePath);
		registFile.setExt(fileExt);
		registFile.setSize(mFile.getSize()+"");
		
		try {
			registFile.setContentType(mFile.getContentType());
		} catch (Exception e) {}
		
		upFile.setRegistSeq(registFile(registFile));
		upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
		
		if (mFile.getSize()!=0){ //File Null Check
			if (! file.exists()){ //경로상에 파일이 존재하지 않을 경우
				try {
					if (file.getParentFile() != null && file.getParentFile().mkdirs()){ //경로에 해당하는 디렉토리들을 생성
							boolean upSucc = file.createNewFile(); //이후 파일 생성
							if (!upSucc){
								uploadSucc=false;
							}
						}
				}
				catch (IOException e) {
					log.error("file upload create error : " + e.getMessage());
					uploadSucc=false;
				}
			}
			
			uploadSucc = FileUtil.transferTo(mFile, file);
			
			upFile.setUploadSucc(uploadSucc);
			
		}
		
		result.put("noticeXML",  upFile); // zip, xml, tar.gz file
		
		try {
			File convertHTMLFile = null;
			
			if ("XML".equals(fileExt.toUpperCase())) {
				convertHTMLFile = CommonFunction.convertXMLToHTML(file, false);
			} else if ("ZIP".equals(fileExt.toUpperCase())) {
				FileUtil.decompress(uploadFilePath + "/" + file.getName(), uploadFilePath + "/" + randomUUID);
				convertHTMLFile = CommonFunction.convertXMLToHTML(new File(uploadFilePath + "/" + randomUUID), true);
			} else if ("TAR.GZ".equals(fileExt.toUpperCase())) {
				CompressUtil.decompressTarGZ(file, uploadFilePath + "/" + randomUUID);
				convertHTMLFile = CommonFunction.convertXMLToHTML(new File(uploadFilePath + "/" + randomUUID), true);
			}
			
			if (convertHTMLFile != null) {
				long convertHTMLFileSize = convertHTMLFile.length();
				
				if (convertHTMLFileSize > 0){
					UploadFile convertNoticeFile = new UploadFile();
					String convertFileId = fileMapper.getFileId();
					String convertNoticeFileName = "Notice-"+prjId+"_"+DateUtil.getCurrentDateTime(DateUtil.DATE_PATTERN)+".html";
					
					convertNoticeFile.setOriginalFilename(convertNoticeFileName);
					convertNoticeFile.setInputName(convertNoticeFileName);
					convertNoticeFile.setSize(convertHTMLFileSize);
					convertNoticeFile.setFilePath(uploadFilePath + "/" + randomUUID);
					convertNoticeFile.setFileName(convertHTMLFile.getName());
					convertNoticeFile.setFileExt("html");
					convertNoticeFile.setIndexNum(indexNum+1);
					convertNoticeFile.setRegistFileId(convertFileId);
					convertNoticeFile.setContentType("text/html");
					
					/** DB Regist Setting **/
					T2File registConvertHTML = new T2File();
					registConvertHTML.setFileId(convertFileId);
					registConvertHTML.setOrigNm(convertNoticeFileName);
					registConvertHTML.setLogiNm(convertHTMLFile.getName());
					registConvertHTML.setLogiPath(uploadFilePath + "/" + randomUUID);
					registConvertHTML.setLogiThumbNm(StringUtil.avoidNull(convertHTMLFile.getName(), "").replace(".html", "_thumb.html"));
					registConvertHTML.setLogiThumbPath(uploadFilePath + "/" + randomUUID + "/thumb");
					registConvertHTML.setExt("html");
					registConvertHTML.setSize(Long.toString(convertHTMLFileSize));
					registFile.setContentType("text/html");
					convertNoticeFile.setRegistSeq(registFile(registConvertHTML));
					convertNoticeFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
					
					result.put("noticeHTML", convertNoticeFile);
				}
			}
			
			return result;
		} catch (Throwable e) {
			log.debug(e.getMessage());
		}
		
		return null; // 변환 실패한 case
	}
	
	//파일 DB 등록
	public String registFile(T2File file) {
		int result = fileMapper.insertFile(file);
		
		if (result<=0){
			return null;
		}
		
		return file.getFileSeq();
	}
}
