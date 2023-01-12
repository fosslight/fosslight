/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.service.FileService;
import oss.fosslight.util.CompressUtil;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.StringUtil;

@Service("fileService")
@Slf4j
@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
public class FileServiceImpl extends CoTopComponent implements FileService {
	
	@Autowired FileMapper fileMapper;
	@Autowired VerificationMapper verificationMapper;
	@Autowired ProjectMapper projectMapper;
	
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile) {
		return uploadFile(req, registFile, null);
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName) {
		return uploadFile(req,registFile,inputFileName,null);
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String oldFileId) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		MultipartHttpServletRequest multipartRequest = null;

		/** S: 파일 업로드 **/
		try {
			multipartRequest = (MultipartHttpServletRequest)req;
		} catch(Exception e) {
			log.debug("error : " + e.getMessage());
			return result;
		}
		
		java.util.Iterator<String> fileNames = multipartRequest.getFileNames();

		boolean sw = true;
		String fileId = "";
		
		if (oldFileId==null || "0".equals(oldFileId) || "".equals(oldFileId)){
			fileId = fileMapper.getFileId();
			if (fileId == null){
				fileId = "1";
			}
		}else{
			fileId = oldFileId;
		}
		
		int indexNum = 0;
		
		while (fileNames.hasNext()){
			UploadFile upFile = new UploadFile();					
			boolean uploadSucc = true;
			String fileName = fileNames.next();		//input name
			
			if (inputFileName != null){
				String inputFileNameRe = inputFileName.replace("##]", "");
				int st = fileName.indexOf("[");
				int en = fileName.indexOf("]");
				
				try{
					indexNum = Integer.parseInt(fileName.substring(st+1, en));
				}catch(Exception e){
					log.error("[##] NumberFormat Exception : " + e.getMessage());
				}
				
				boolean isInput = fileName.startsWith(inputFileNameRe);
				
				if (!isInput || inputFileName == null){
					continue;
				}
			}
			
			sw=false;
			
			MultipartFile mFile = multipartRequest.getFile(fileName);
			
			if (isEmpty(mFile.getOriginalFilename())) {
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
				uploadFilePath = appEnv.getProperty("upload.path", "/upload");
				uploadThumbFilePath = appEnv.getProperty("image.path", "/image");
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
						if (file.getParentFile().mkdirs()){ //경로에 해당하는 디렉토리들을 생성

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
			
			result.add(upFile);
		}
		
		if (sw){
			result = null;
		}
		
		return result;
	}
	
	//파일 DB 등록
	public String registFile(T2File file) {
		int result = fileMapper.insertFile(file);
		
		if (result <= 0){
			return null;
		}
		
		return file.getFileSeq();
	}

	@Override
	public T2File selectFileInfo(String fileSeq) {
		T2File file = fileMapper.selectFileInfo(fileSeq);
		
		return file;
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, boolean useRandomPath, String filePath) {
		return uploadFile(req, registFile, inputFileName, "", useRandomPath, filePath);
	}


	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String oldFileId, boolean useRandomPath, String filePath){
		return uploadFile(req, registFile, inputFileName, "", useRandomPath, filePath, false);
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String oldFileId, boolean useRandomPath, String filePath, boolean isOrigFile) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		MultipartHttpServletRequest multipartRequest = null;
		
		try {
			// request가 multipartRequest가 아닐 경우.
			multipartRequest = (MultipartHttpServletRequest)req;
		} catch(Exception e) {
			log.error("Request IS NOT a type of MultipartRequest : " + e.getMessage());
			return result;
		}
		
		// request에서 파일명들을 가져온다.
		java.util.Iterator<String> fileNames = multipartRequest.getFileNames();
		
		boolean sw = true;
		String fileId = "";
		
		//구 fileId가 존제 한다면 구 fileId에 넣어준다
		if (!isEmpty(oldFileId)){
			fileId = oldFileId;
		} else {
			fileId = avoidNull(fileMapper.getFileId(), "1");
		}
		
		log.debug("Target fileId : " + fileId);

		int indexNum = 0;
		
		while (fileNames.hasNext()){
			UploadFile upFile = new UploadFile();
			registFile.setCreator(registFile.getCreator());
			boolean uploadSucc = true;
			String fileName = fileNames.next();		//input name
			
			if (inputFileName != null){
				String inputFileNameRe = inputFileName.replace("##]", "");
				int st = fileName.indexOf("[");
				int en = fileName.indexOf("]");
				
				try {
					indexNum = Integer.parseInt(fileName.substring(st+1, en));
				} catch(Exception e) {
					log.error("[##] NumberFormat Exception : " + e.getMessage());
				}
				
				log.debug("indexNum : " + indexNum + ", fileName : " + fileName);
				
				boolean isInput = fileName.startsWith(inputFileNameRe);
				
				if (!isInput || inputFileName == null){
					continue;
				}
			}
			
			sw=false;
			
			MultipartFile mFile = multipartRequest.getFile(fileName);

			if (isEmpty(mFile.getOriginalFilename())) {
				throw new RuntimeException("File Name is empty");
			}
			
			if (mFile.getSize() <= 0) {
				throw new RuntimeException("File Size is 0");
			}
			
			String originalFileName = avoidNull(registFile.getBeforeOrigNm(), mFile.getOriginalFilename());	//Original File name

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
			
			try{
				/* 파일 저장 경로 설정 hk-cho */
				// 1) parameter로 filePath가 넘어오지 않았을 경우 Property에 있는 경로에 저장
				if (StringUtil.isEmpty(filePath)) {
					uploadFilePath = appEnv.getProperty("upload.path", "/upload");
					uploadThumbFilePath = appEnv.getProperty("image.path", "/image");
				} else { // 2) parameter로 filePath가 넘어왔을 경우 넘어온 filePath에 저장
					uploadFilePath = filePath;
					uploadThumbFilePath = filePath + "/" + "thumb";
				}
				
				log.debug("uploadFilePath : " + uploadFilePath);
			} catch(Exception e) {
				log.error("Wrong file upload path(get properties) : " + e.getMessage());
			}
			
			/* 랜덤 파일명 사용 여부 hk-cho */
			String phyFileNm = originalFileName;						// 서버에 저장될 물리 파일명
			String thumbPhyFileNm = phyFileNm+"_thumb."+fileExt;		// 서버에 저장될 thumb 물리 파일명
			
			if (useRandomPath){
				UUID randomUUID = UUID.randomUUID();
				phyFileNm = randomUUID+"."+fileExt;
				thumbPhyFileNm = randomUUID+"_thumb."+fileExt;
			}
			
			/** Return Setting **/
			upFile.setOriginalFilename(originalFileName);
			upFile.setInputName(fileName);
			upFile.setSize(mFile.getSize());
			upFile.setFilePath(uploadFilePath);
			upFile.setFileExt(fileExt);
			upFile.setFileName(phyFileNm);
			upFile.setIndexNum(indexNum);
			upFile.setRegistFileId(fileId);
			
			try {
				upFile.setContentType(mFile.getContentType());
			} catch (Exception e) {}
			
			/** DB Regist Setting **/
			registFile.setFileId(fileId);
			registFile.setOrigNm(originalFileName);
			registFile.setLogiNm(phyFileNm);
			registFile.setLogiPath(uploadFilePath);
			registFile.setLogiThumbNm(thumbPhyFileNm);
			registFile.setLogiThumbPath(uploadThumbFilePath);
			registFile.setExt(fileExt);
			registFile.setSize(mFile.getSize()+"");

			try {
				registFile.setContentType(mFile.getContentType());
			} catch (Exception e) {}
			
			if (mFile.getSize()!=0){ //File Null Check
				new File(filePath).mkdirs();
				
				if (isOrigFile) {
					uploadSucc = FileUtil.transferTo(mFile, new File(filePath + "/" + originalFileName));
				} else {
					uploadSucc = FileUtil.transferTo(mFile, new File(filePath + "/" + phyFileNm));
				}
				
				if (uploadSucc){
					try {
						String regiSeq = registFile(registFile);
						
						upFile.setRegistSeq(regiSeq); //새로추가된 SEQ(primaryKey a.i)
						upFile.setRegistFileId(fileId); //FileId(group개념의 컬럼)
						upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
					} catch(Exception e) {
						log.error("file regist error : " + e.getMessage());
						uploadSucc=false;
					}
				}
				
				upFile.setUploadSucc(uploadSucc);
			}
			
			result.add(upFile);
		}
		
		if (sw){
			result = null;
		}
		
		return result;
	}

	@Override
	public String registFileWithFileName(String filePath, String fileName) {
		T2File fileInfo = new T2File();
		fileInfo.setFileId(fileMapper.getFileId());
		fileInfo.setOrigNm(fileName);
		fileInfo.setLogiNm(fileName);
		fileInfo.setLogiPath(filePath);
		fileInfo.setExt(FilenameUtils.getExtension(fileName));
		
		try {
			if (fileName.toLowerCase().endsWith(".tgz.gz")) {
				fileInfo.setExt("tgz.gz");
			} else if (fileName.toLowerCase().endsWith(".tar.bz2")) {
				fileInfo.setExt("tar.bz2");
			} else if (fileName.toLowerCase().endsWith(".tar.gz")) {
				fileInfo.setExt("tar.gz");
			}
		} catch (Exception e) {
			//TODO: handle exception
			log.error("file regist error : " + e.getMessage());
		}

		fileInfo.setSize("1");
		
		return registFile(fileInfo);
	}

	@Override
	public String registFileDownload(String filePath, String fileName, String logiFileName) {
		T2File fileInfo = new T2File();
		fileInfo.setFileId(fileMapper.getFileId());
		fileInfo.setGubn(CoConstDef.FILE_GUBUN_FILE_DOWNLOAD);
		fileInfo.setOrigNm(fileName);
		fileInfo.setLogiNm(logiFileName);
		fileInfo.setLogiPath(filePath);
		fileInfo.setExt(FilenameUtils.getExtension(fileName));
		
		try {
			if (avoidNull(fileName.toLowerCase()).endsWith(".tgz.gz")) {
				fileInfo.setExt("tgz.gz");
			} else if (avoidNull(fileName.toLowerCase()).endsWith(".tar.bz2")) {
				fileInfo.setExt("tar.bz2");
			} else if (avoidNull(fileName.toLowerCase()).endsWith(".tar.gz")) {
				fileInfo.setExt("tar.gz");
			}
		} catch (Exception e) {
			//TODO: handle exception
			log.error("file regist error : " + e.getMessage());
		}
		
		fileInfo.setSize("1");
		
		return registFile(fileInfo);
	}
	
	@Override
	public T2File selectFileInfoById(String fileId) {
		return fileMapper.selectFileInfoById(fileId);
	}

	@Override
	public String copyFileInfo(String orgFileId) {
		T2File fileInfo = new T2File();
		fileInfo.setFileId(fileMapper.getFileId());
		fileInfo.setOrgFileId(orgFileId);
		
		fileMapper.copyFileInfo(fileInfo);
		
		return fileInfo.getFileId();
	}

	@Override
	public T2File selectFileInfoByLogiName(T2File bean) {
		return fileMapper.selectFileInfoByName(bean);
	}
	
	//wgetUrl 파일 upload
	@Override
	public List<UploadFile> uploadWgetFile(HttpServletRequest req, T2File registFile, Map<Object, Object> map, boolean isOrigFile) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		log.debug("<-------- uploadWgetFile Start------->");
		/** S: 파일 업로드 **/
		String url = (String) map.get("wgetUrl");
		String filePath = (String) map.get("filePath");
		String prjId = (String) map.get("prjId");
		String uploadFilePath = "";
		String uploadThumbFilePath = "";
		boolean uploadSucc = true;
		UploadFile upFile = new UploadFile();
		
		if (StringUtil.isEmpty(filePath)){
			try {
				uploadFilePath = appEnv.getProperty("packaging.path", "/upload/packaging") + "/" + prjId;
				uploadThumbFilePath = appEnv.getProperty("packaging.path", "/upload/packaging") + "/" + prjId + "/thumb";
				
				File dir = new File(uploadFilePath);
				
				if (!dir.exists()){
					dir.mkdirs();
				}
			} catch(Exception e) {
				log.error("file upload path(get properties) : " + e.getMessage());
			}
		} else {
			uploadFilePath = filePath;
			uploadThumbFilePath = filePath + File.separator + "thumb";
			File dir = new File(filePath);
			 
	        if (!dir.exists()) { //폴더 없으면 폴더 생성
	            dir.mkdirs();
	        }
		}
		
		int ShellCommanderResult = 9;
		int indexNum = 0;
		// Url의 index 다음 문자열 부터 분리후 저장
		String originalFileName = avoidNull(url).trim();
		
		if (originalFileName.indexOf("/") > -1) {
			originalFileName = originalFileName.substring(originalFileName.lastIndexOf('/') + 1);
		}
		
		int i = originalFileName.lastIndexOf('.'); 
	    // 마지막 .부터 나머지 문자열을 f에 저장
		String fileName = originalFileName.substring(0,i);		//input name
		String fileExt = FilenameUtils.getExtension(originalFileName);
		
		if (originalFileName.toLowerCase().endsWith(".tgz.gz")) {
			fileExt = "tgz.gz";
		} else if (originalFileName.toLowerCase().endsWith(".tar.bz2")) {
			fileExt = "tar.bz2";
		} else if (originalFileName.toLowerCase().endsWith(".tar.gz")) {
			fileExt = "tar.gz";
		}
		
		UUID randomUUID = UUID.randomUUID();
		
		log.info("WGET STart");
		log.info("WGET URL : " + url);
		log.info("WGET FileName : " + originalFileName);
		log.info("WGET Save as File Name :" + randomUUID+"."+fileExt);
		
		//주소에서 파일 가져오기
		// 네트워크 상황에 따라서 대용량 파일을 정상적으로 다운로드 받지 못하는 현상이 발생하여 (유추) NIO 방식으로 변경함
		ReadableByteChannel readChannel = null;
		FileChannel writeChannel = null;
		FileOutputStream fileOS = null;
		
		try {
			readChannel = Channels.newChannel(new URL(url.replaceAll("\\s", "%20")).openStream());
			
			if (isOrigFile) {
				fileOS = new FileOutputStream(uploadFilePath+"/"+fileName+"."+fileExt);
			} else {
				fileOS = new FileOutputStream(uploadFilePath+"/"+randomUUID+"."+fileExt);
			}
			  
			writeChannel = fileOS.getChannel(); 
			writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
			ShellCommanderResult = 0;
		} catch (Exception e) {
			log.error(e.getMessage());
			ShellCommanderResult = -1;
		} finally {
			if (writeChannel != null) {
				try {
					writeChannel.close();
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
				}
			}
			
			if (fileOS != null) {
				try {
					fileOS.close();
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
				}
			}
			
			if (readChannel != null) {
				try {
					readChannel.close();
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
				}
			}
		}
		
		if (ShellCommanderResult == 0){
			File getfile = new File(uploadFilePath+"/"+randomUUID+"."+fileExt);
			long fileSize = getfile.length();
			
			String fileId = "";
			//fileId
			fileId = avoidNull(fileMapper.getFileId(), "1");
				
			/** Return Setting **/
			upFile.setOriginalFilename(originalFileName);
			upFile.setInputName(fileName);
			upFile.setSize(fileSize);
			upFile.setFilePath(uploadFilePath);
			upFile.setFileName(randomUUID+"."+fileExt);
			upFile.setIndexNum(indexNum);
			upFile.setRegistFileId(fileId);
			
			try {
				upFile.setContentType(fileExt);
			} catch (Exception e) {}
				
			/** DB Regist Setting **/
			registFile.setFileId(fileId);
			registFile.setOrigNm(originalFileName);
			registFile.setLogiNm(randomUUID+"."+fileExt);
			registFile.setLogiPath(uploadFilePath);
			registFile.setLogiThumbNm(randomUUID+"_thumb."+fileExt);
			registFile.setLogiThumbPath(uploadThumbFilePath);
			registFile.setExt(fileExt);
			registFile.setSize(fileSize+"");
			
			try {
				registFile.setContentType(fileExt);
			} catch (Exception e) {}
			
			upFile.setRegistSeq(registFile(registFile));
			upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
			
		} else {
			uploadSucc = false;
		}
		
		upFile.setUploadSucc(uploadSucc);
		upFile.setWgetResult(ShellCommanderResult);
		result.add(upFile);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String setClearFiles(Map<Object, Object> map) {
		String deleteComment = "";
		String uploadComment = "";
		String prjId = (String) map.get("prjId");
		List<String> fileSeqs =	(List<String>)map.get("fileSeqs");
		List<T2File> uploadFileInfos = new ArrayList<T2File>();
		File file = null;
		
		Project prjParam = new Project();
		prjParam.setPrjId(prjId);
		ArrayList<String> newPackagingFileIdList = new ArrayList<String>();
		newPackagingFileIdList.add(fileSeqs.size() > 0 ? fileSeqs.get(0) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 1 ? fileSeqs.get(1) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 2 ? fileSeqs.get(2) : null);
		prjParam.setPackageFileId(newPackagingFileIdList.get(0));
		prjParam.setPackageFileId2(newPackagingFileIdList.get(1));
		prjParam.setPackageFileId3(newPackagingFileIdList.get(2));
		
		for (String fileSeq : fileSeqs){
			T2File paramT2File = new T2File();
			
			paramT2File.setFileSeq(fileSeq);
			uploadFileInfos.add(fileMapper.getFileInfo(paramT2File));
		}
						
		String publicUrl = appEnv.getProperty("upload.path", "/upload");
		String packagingUrl = appEnv.getProperty("packaging.path", "/upload/packaging") + "/" + prjId;
		List<T2File> result = fileMapper.selectPackagingFileInfo(prjId); // verify한 file을 select함.

		if (result.size() > 0){
			for (T2File res : result){
				String rtnFilePath = res.getLogiPath();
				String rtnFileName = res.getLogiNm();
				String rtnFileSeq = res.getFileSeq();
				
				if (publicUrl.equals(rtnFilePath)){
					// select한 filePath가 upload Dir 일 경우 해당 파일만 삭제함.
					file = new File(rtnFilePath + "/" + rtnFileName);
					
					for (String fileSeq : fileSeqs) {
						if (file.exists() && !rtnFileSeq.equals(fileSeq)){
							int reuseCnt = fileMapper.getPackgingReuseCnt(rtnFileName);
								
							if (reuseCnt == 0){
								T2File delFile = new T2File();
								delFile.setFileSeq(rtnFileSeq);
								delFile.setGubn("A");
								int returnSuccess = fileMapper.updateFileDelYnKessan(delFile);
								
								if (returnSuccess > 0) {
									if (file.delete()){
										log.debug(rtnFilePath + "/" + rtnFileName + " is delete success.");
									} else {
										log.debug(rtnFilePath + "/" + rtnFileName + " is delete failed.");
									}
								}
							}
						}
					}
				}
			}
			
			deleteFiles(packagingUrl, uploadFileInfos, prjId); // 'upload/packaging/#{prjId}' 의 Directory가 있는지 체크 후 삭제 처리함.( 현재등록한 file을 제외한 나머지를 삭세처리 )
		} else {
			deleteFiles(packagingUrl, uploadFileInfos, prjId); // verify 한 file이 없을경우 packagingUrl도 같이 검사하여 delete를 함.
		}
		
		// packaging File comment
		try {
			Project project = projectMapper.selectProjectMaster(prjParam);
			ArrayList<String> origPackagingFileIdList = new ArrayList<String>();
			origPackagingFileIdList.add(project.getPackageFileId());
			origPackagingFileIdList.add(project.getPackageFileId2());
			origPackagingFileIdList.add(project.getPackageFileId3());
			
			int idx = 0;
			
			for (String fileId : origPackagingFileIdList){
				T2File fileInfo = new T2File();
				
				if (!isEmpty(fileId) && !fileId.equals(newPackagingFileIdList.get(idx))){
					//fileInfo.setFileSeq(fileId);
					fileInfo = fileMapper.selectFileInfo(fileId);
					deleteComment += "Packaging file, "+fileInfo.getOrigNm()+", was deleted by "+loginUserName()+". <br>";
				}
				
				if (!isEmpty(newPackagingFileIdList.get(idx)) && !newPackagingFileIdList.get(idx).equals(fileId)){
					//fileInfo.setFileSeq(newPackagingFileIdList.get(idx));
					fileInfo = fileMapper.selectFileInfo(newPackagingFileIdList.get(idx));
					oss.fosslight.domain.File resultFile = verificationMapper.selectVerificationFile(newPackagingFileIdList.get(idx));
					
					if (CoConstDef.FLAG_YES.equals(resultFile.getReuseFlag())){
						uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was loaded from Project ID: "+resultFile.getRefPrjId()+" by "+loginUserName()+". <br>";
					} else {
						uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was uploaded by "+loginUserName()+". <br>";
					}
				}
				
				idx++;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}		
		
		verificationMapper.updatePackagingReuseMap(prjParam);
		
		return deleteComment + uploadComment;
	}

	@Override
	public void deleteFiles(String url, List<T2File> uploadFileInfos, String prjId) {
		File file = new File(url);
		ArrayList<String> LogiNms = new ArrayList<String>();
		ArrayList<String> reuseNms = new ArrayList<String>();
		
		for (T2File uploadFileInfo : uploadFileInfos){
			LogiNms.add(uploadFileInfo.getLogiNm());
		}
		
		// 현재 proejct Packaging File 중 재사용중인 packaging File 이 있다면 제거 불가
		List<T2File> reusePackaging = fileMapper.getReusePackagingInfo();
		
		for (T2File reuse : reusePackaging){
			reuseNms.add(reuse.getLogiNm());
		}
		
		if (file.exists()){
			for (File f : file.listFiles()){
				String fileNm = f.getName();
				
				if (!LogiNms.contains(fileNm)){
					T2File delFile = new T2File();
					delFile.setLogiPath(url);
					delFile.setLogiNm(f.getName());
					
					int returnSuccess = fileMapper.updateReuseChkFileDelYnByFilePathNm(delFile);
					
					if (returnSuccess > 0 && !reuseNms.contains(fileNm)){
						if (f.delete()){
							log.debug(url + "/" + f.getName() + " is delete success.");
						}else{
							log.debug(url + "/" + f.getName() + " is delete failed.");
						}
					}
				}
			}
		}
		
		// 재사용을 했었던 file중 다른 project에서도 재사용을 하지 않은 file 있는지 확인하고 재사용을 안한다면 file 삭제 / 추후 reuse하는 다른 project에서도 reuseFlag가 N이 되면 지우는 case이므로 log는 남기지 않음.
		List<T2File> reusePackagingFileList = fileMapper.getPackgingReuseCntToList(prjId);
		
		for (T2File reusePackagingFile : reusePackagingFileList){ // reuseCnt가 0인 값만 불러오고 삭제처리 후 hidden flag를 Y로 변경 그리고 재검색시 조회 불가상태로 만듦.
			File reuseFile = new File(reusePackagingFile.getLogiPath());
			
			if (reuseFile.exists()){
				for (File f : reuseFile.listFiles()){
					if (reusePackagingFile.getLogiNm().equals(f.getName())){
						T2File delFile = new T2File();
						delFile.setLogiPath(reusePackagingFile.getLogiPath());
						delFile.setLogiNm(f.getName());
						int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(delFile);
						String[] refPrjId = reusePackagingFile.getLogiPath().split("/");
						
						fileMapper.setReusePackagingFileHidden(refPrjId[refPrjId.length-1], reusePackagingFile.getLogiPath(), f.getName());
						
						if (returnSuccess > 0){
							if (f.delete()){
								log.debug(url + "/" + f.getName() + " is delete success.");
							}else{
								log.debug(url + "/" + f.getName() + " is delete failed.");
							}
						}
					}
				}
			}
		}
	}

	@Override
	public List<UploadFile> setReusePackagingFile(String refFileSeq) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		UploadFile upFile = new UploadFile();
		
		String FileId = fileMapper.getFileId();
		
		T2File file = selectFileInfo(refFileSeq);
		file.setFileId(FileId);
		file.setCreator(loginUserName());
		
		fileMapper.insertFile(file);
		
		file = selectFileInfoById(FileId);
		
		upFile.setOriginalFilename(file.getOrigNm());
		upFile.setSize(Long.parseLong(file.getSize()));
		upFile.setFilePath(file.getLogiPath());
		upFile.setFileName(file.getLogiNm());
		upFile.setContentType(file.getExt());
		upFile.setRegistSeq(file.getFileSeq());
		upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
		
		result.add(upFile);
		
		return result;
	}

	@Override
	public List<UploadFile> uploadNoticeXMLFile(HttpServletRequest req, T2File registFile, String oldFileId, String prjId) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		MultipartHttpServletRequest multipartRequest = null;

		/** S: 파일 업로드 **/
		try {
			multipartRequest = (MultipartHttpServletRequest)req;
		} catch(Exception e) {
			log.debug("error : " + e.getMessage());
			
			return result;
		}
		
		java.util.Iterator<String> fileNames = multipartRequest.getFileNames();

		boolean sw = true;
		String fileId = "";
		
		//구 fileId가 존제 한다면 구 fileId에 넣어준다
		if (oldFileId==null || "0".equals(oldFileId) || "".equals(oldFileId)){
			fileId = fileMapper.getFileId();
			if (fileId == null){
				fileId = "1";
			}
		} else {
			fileId = oldFileId;
		}
		
		int indexNum = 0;
		
		while (fileNames.hasNext()){
			UploadFile upFile = new UploadFile();
					
			boolean uploadSucc = true;
			String fileName = fileNames.next();		//input name
			
			sw = false;
			
			MultipartFile mFile = multipartRequest.getFile(fileName);
			
			if (isEmpty(mFile.getOriginalFilename())) {
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
			
			try{
				uploadFilePath = appEnv.getProperty("android.upload.path", "/upload/android_notice") + "/" + prjId;
				uploadThumbFilePath = appEnv.getProperty("image.path", "/image") + "/" + prjId + "/thumb";
			}catch(Exception e){
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
						if (file.getParentFile().mkdirs()){ //경로에 해당하는 디렉토리들을 생성
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
			result.add(upFile);
			
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
						registConvertHTML.setLogiThumbNm(convertHTMLFile.getName().replace(".html", "_thumb.html"));
						registConvertHTML.setLogiThumbPath(uploadFilePath + "/" + randomUUID + "/thumb");
						registConvertHTML.setExt("html");
						registConvertHTML.setSize(Long.toString(convertHTMLFileSize));
						registFile.setContentType("text/html");
						convertNoticeFile.setRegistSeq(registFile(registConvertHTML));
						convertNoticeFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
						
						result.add(convertNoticeFile);
					}
				}
			} catch (Throwable e) {
				log.debug(e.getMessage());
			}
		}
		
		if (sw){
			result = null;
		}
		
		return result;
	}

	@Override
	public void deletePhysicalFile(T2File file, String flag) {
		String filePath = "";
		
		if (flag.equals("VERIFY")) {
			filePath = file.getLogiPath() + "/" + file.getLogiNm();
		}else {
			T2File T2file = fileMapper.getFileInfo2(file);
			filePath = T2file.getLogiPath() + "/" + T2file.getLogiNm();
		}
		
		try {
			FileOutputStream to = new FileOutputStream(filePath);
			to.flush();
   	 		to.close();
   	 		
   	 		File LogiFile = new File(filePath);	
   	 		if (LogiFile.exists()){
   	 			LogiFile.delete();
   	 		}
		} catch(Exception e) {
			log.info(e.getMessage(), e);
		}
	}

	@Override
	public String copyPhysicalFile(String fileId) {
		boolean fileCopyFlag = false;
		String newFileId = fileMapper.getFileId();
		List<T2File> orgFileInfoList = fileMapper.getFileInfoList(fileId);
		
		for (T2File orgFile : orgFileInfoList) {
			String baseFile = orgFile.getLogiPath() + "/" + orgFile.getLogiNm();
			
			UUID randomUUID = UUID.randomUUID();
			String copyFileName = randomUUID + "." + orgFile.getExt();
			String newFile = orgFile.getLogiPath();
			
			if (FileUtil.copyFile(baseFile, newFile, copyFileName)) {
				T2File fileInfo = new T2File();
				fileInfo.setFileId(newFileId);
				fileInfo.setFileSeq(orgFile.getFileSeq());
				fileInfo.setLogiNm(copyFileName);
				
				fileMapper.insertCopyPhysicalFileInfo(fileInfo);
				fileCopyFlag = true;
			}else {
				fileCopyFlag = false;
			}
			
			if (!fileCopyFlag) {
				newFileId = null;
				log.error("physical file copy error");
				break;
			}
		}
		
		return newFileId;
	}
}