/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.ShellCommander;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;
import oss.fosslight.repository.CommentMapper;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.VerificationService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.SPDXUtil2;
import oss.fosslight.util.StringUtil;

@Service
@Slf4j
public class VerificationServiceImpl extends CoTopComponent implements VerificationService {
	// Service
	@Autowired CommentService commentService;
	@Autowired ProjectService projectService;
	@Autowired FileService fileService;
	
	// Mapper
	@Autowired ProjectMapper projectMapper;
	@Autowired CommentMapper commentMapper;
	@Autowired VerificationMapper verificationMapper;
	@Autowired FileMapper fileMapper;
	
	private static String VERIFY_HOME_PATH = CommonFunction.emptyCheckProperty("verify.home.path", "/verify");
	private static String VERIFY_BIN_PATH = CommonFunction.emptyCheckProperty("verify.bin.path", "/verify");
	private static String VERIFY_PATH_DECOMP = CommonFunction.emptyCheckProperty("verify.decompress.path", "/verify/decompression");
	private static String VERIFY_PATH_OUTPUT = CommonFunction.emptyCheckProperty("verify.output.path", "/verify/output");
	private static String NOTICE_PATH = CommonFunction.emptyCheckProperty("notice.path", "/notice");
	private static String EXPORT_TEMPLATE_PATH = CommonFunction.emptyCheckProperty("export.template.path", "/template");
	
	@Override
	public Map<String, Object> getVerificationOne(Project project) {
		// 1. Verification정보
		// 2. Comment 정보
		HashMap<String, Object> map = new HashMap<String, Object>();
		Project prj = projectMapper.selectProjectMaster(project);

		String comment = prj != null ? prj.getComment() : null;
		String content = commentMapper.getContent(comment);
		OssNotice ossNotice = verificationMapper.selectOssNoticeOne(project);

		map.put("data", prj);
		map.put("commentText", content);
		map.put("notice", ossNotice);

		return map;
	}
	
	@Override
	public OssNotice selectOssNoticeOne(String prjId) {
		Project project = new Project();
		project.setPrjId(prjId);
		
		return verificationMapper.selectOssNoticeOne(project);
	}
	
	@Override
	public List<OssComponents> getVerifyOssList(Project projectMaster) {
		List<OssComponents> componentList = verificationMapper.selectVerifyOssList(projectMaster);
		
		if(componentList != null && !componentList.isEmpty() && componentList.get(0) == null) {
			componentList = new ArrayList<>();
		}
		
		return componentList;
	}

	@Override
	public boolean getChangedPackageFile(String prjId, List<String> fileSeqs) {
		String packageFileId = fileSeqs.get(0);
		String packageFileId2 = fileSeqs.size() > 1 ? fileSeqs.get(1) : null;
		String packageFileId3 = fileSeqs.size() > 2 ? fileSeqs.get(2) : null;
		
		int result = verificationMapper.checkPackagingFileId(prjId,packageFileId, packageFileId2, packageFileId3);
		
		if(result > 0){
			return false;
		}else{
			return true;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void savePath(Map<Object, Object> map) {
		try {
			List<String> gridComponentIds =	(List<String>)map.get("gridComponentIds");
			List<String> gridFilePaths =	(List<String>)map.get("gridFilePaths");
			List<String> fileSeqs =	(List<String>) map.get("fileSeqs");
			String prjId = (String) map.get("prjId");
			String deleteFlag = (String) map.get("deleteFlag");
			String verifyFlag = (String) map.get("statusVerifyYn");
			String deleteFiles = (String) map.get("deleteFiles");
			String deleteComment = "";
			String uploadComment = "";
			
			// verify 버튼 클릭시 file path를 저장한다.
			if(gridComponentIds != null && !gridComponentIds.isEmpty()) {
				int idx = 0;
				
				for(String s : gridComponentIds){
					OssComponents param = new OssComponents();
					param.setComponentId(s);
					param.setFilePath(gridFilePaths.get(idx++));
					
					if(verifyFlag.equals(CoConstDef.FLAG_YES)){
						param.setVerifyFileCount("");
						
						verificationMapper.updateVerifyFileCount(param);
					}
					
					verificationMapper.updateVerifyFilePath(param);
				}
			}
			
			if(!isEmpty(prjId)) {
				Project prjParam = new Project();
				prjParam.setPrjId(prjId);
				ArrayList<String> newPackagingFileIdList = new ArrayList<String>();
				newPackagingFileIdList.add(fileSeqs.size() > 0 ? fileSeqs.get(0) : null);
				newPackagingFileIdList.add(fileSeqs.size() > 1 ? fileSeqs.get(1) : null);
				newPackagingFileIdList.add(fileSeqs.size() > 2 ? fileSeqs.get(2) : null);
				prjParam.setPackageFileId(newPackagingFileIdList.get(0));
				prjParam.setPackageFileId2(newPackagingFileIdList.get(1));
				prjParam.setPackageFileId3(newPackagingFileIdList.get(2));
				
				if(deleteFiles.equals(CoConstDef.FLAG_YES)){
					prjParam.setStatusVerifyYn(CoConstDef.FLAG_NO);
				}			
				
				// packaging File comment
				try {
					Project project = projectMapper.selectProjectMaster(prjParam);
					ArrayList<String> origPackagingFileIdList = new ArrayList<String>();
					origPackagingFileIdList.add(project.getPackageFileId());
					origPackagingFileIdList.add(project.getPackageFileId2());
					origPackagingFileIdList.add(project.getPackageFileId3());
					
					int idx = 0;
					
					for(String fileId : origPackagingFileIdList){
						T2File fileInfo = new T2File();
						
						if(!isEmpty(fileId) && !fileId.equals(newPackagingFileIdList.get(idx))){
							fileInfo.setFileSeq(fileId);
							fileInfo = fileMapper.getFileInfo(fileInfo);
							deleteComment += "Packaging file, "+fileInfo.getOrigNm()+", was deleted by "+loginUserName()+". <br>";
						}
						
						if(!isEmpty(newPackagingFileIdList.get(idx)) && !newPackagingFileIdList.get(idx).equals(fileId)){
							fileInfo.setFileSeq(newPackagingFileIdList.get(idx));
							fileInfo = fileMapper.getFileInfo(fileInfo);
							oss.fosslight.domain.File result = verificationMapper.selectVerificationFile(newPackagingFileIdList.get(idx));
							
							if(CoConstDef.FLAG_YES.equals(result.getReuseFlag())){
								uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was loaded from Project ID: "+result.getRefPrjId()+" by "+loginUserName()+". <br>";
							}else{
								uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was uploaded by "+loginUserName()+". <br>";
							}
						}
						
						idx++;
					}
					
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
					commHisBean.setReferenceId(prjId);
					commHisBean.setContents(deleteComment+uploadComment);
					
					commentService.registComment(commHisBean);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				
				verificationMapper.updatePackagingReuseMap(prjParam);
				verificationMapper.updatePackageFile(prjParam);
				
				if(CoConstDef.FLAG_YES.equals(deleteFlag)){
					projectMapper.updateReadmeContent(prjParam); // README Clear
					projectMapper.updateVerifyContents(prjParam); // File List, Banned List Clear
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@Override
	public boolean getNoticeHtmlFile(OssNotice ossNotice) throws IOException {
		return getNoticeHtmlFile(ossNotice, null);
	}
	
	@Override
	public boolean getNoticeHtmlFile(OssNotice ossNotice, String contents) throws IOException {
		Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
		
		// OSS Notice가 N/A이면 고지문을 생성하지 않는다.
		if(CoConstDef.CD_NOTICE_TYPE_NA.equals(prjInfo.getNoticeType())) {
			return true;
		}
		
		prjInfo.setUseCustomNoticeYn(!isEmpty(contents) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
		contents = avoidNull(contents, getNoticeHtml(ossNotice));
		
		if("binAndroid".equals(contents)) {
			return getAndroidNoticeVelocityTemplateFile(prjInfo); // file Content 옮기는 기능에서 files.copy로 변경
		} else {
			return getNoticeVelocityTemplateFile(contents, prjInfo);	
		}
	}
	
	private boolean getAndroidNoticeVelocityTemplateFile(Project project) {
		boolean procResult = true;
		try {
			// file path and name 설정
			// 파일 path : <upload_home>/notice/
			// 파일명 : 임시: 프로젝트ID_yyyyMMdd\
			String filePath = NOTICE_PATH + "/" + project.getPrjId();
			T2File baseFile = null;
			String basePath = null;
			
			if(isEmpty(project.getSrcAndroidNoticeXmlId()) && !isEmpty(project.getSrcAndroidNoticeFileId())) {
				baseFile = fileMapper.selectFileInfoById(project.getSrcAndroidNoticeFileId());
				basePath = CommonFunction.emptyCheckProperty("upload.path", "/upload") + "/" + baseFile.getLogiNm();
			} else {
				baseFile = fileMapper.selectFileInfoById(project.getSrcAndroidNoticeXmlId());
				basePath = baseFile.getLogiPath() + "/" + baseFile.getLogiNm();
			}
			
			// 이전에 생성된 파일은 모두 삭제한다.
			Path rootPath = Paths.get(filePath);
			
			if(rootPath.toFile().exists()) {
				for(String _fName : rootPath.toFile().list()) {
					Files.deleteIfExists(rootPath.resolve(_fName));
					
					T2File file = new T2File();
					file.setLogiNm(_fName);
					file.setLogiPath(filePath);
					
					int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(file);
					
					if(returnSuccess > 0){
						log.debug(filePath + "/" + _fName + " is delete success.");
					}else{
						log.debug(filePath + "/" + _fName + " is delete failed.");
					}
				}
			}
			
			String fileName = CommonFunction.getNoticeFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), "html");
			
			if(oss.fosslight.util.FileUtil.copyFile(basePath, filePath, fileName)) {
				// 파일 등록
				String FileSeq = fileService.registFileWithFileName(filePath, fileName);
				
				// project 정보 업데이트
				Project projectParam = new Project();
				projectParam.setPrjId(project.getPrjId());
				projectParam.setNoticeFileId(FileSeq);
				projectParam.setUseCustomNoticeYn(StringUtil.nvl(project.getUseCustomNoticeYn(),CoConstDef.FLAG_NO));
				
				verificationMapper.updateNoticeFileInfo(projectParam);
			} else {
				procResult = false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			procResult = false;
		}
		
		return procResult;
	}
	
	private boolean getNoticeVelocityTemplateFile(String contents, Project project) {
		boolean procResult = true;
		
		try {
			// file path and name 설정
			// 파일 path : <upload_home>/notice/
			// 파일명 : 임시: 프로젝트ID_yyyyMMdd\
			
			String filePath = NOTICE_PATH + "/" + project.getPrjId();
			// 이전에 생성된 파일은 모두 삭제한다.
			Path rootPath = Paths.get(filePath);
			if(rootPath.toFile().exists()) {
				for(String _fName : rootPath.toFile().list()) {
					Files.deleteIfExists(rootPath.resolve(_fName));
					
					T2File file = new T2File();
					file.setLogiNm(_fName);
					file.setLogiPath(filePath);
					
					int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(file);
					
					if(returnSuccess > 0){
						log.debug(filePath + "/" + _fName + " is delete success.");
					}else{
						log.debug(filePath + "/" + _fName + " is delete failed.");
					}
				}
			}			
			
			String fileName = CommonFunction.getNoticeFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), "html");
			
			if(oss.fosslight.util.FileUtil.writhFile(filePath, fileName, contents)) {
				// 파일 등록
				String FileSeq = fileService.registFileWithFileName(filePath, fileName);
				
				// project 정보 업데이트
				Project projectParam = new Project();
				projectParam.setPrjId(project.getPrjId());
				projectParam.setNoticeFileId(FileSeq);
				projectParam.setUseCustomNoticeYn(StringUtil.nvl(project.getUseCustomNoticeYn(),CoConstDef.FLAG_NO));
				
				verificationMapper.updateNoticeFileInfo(projectParam);
			} else {
				procResult = false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			procResult = false;
		}
		
		return procResult;
	}
	
	@Override
	public String getNoticeHtml(OssNotice ossNotice) throws IOException {
		Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
		String androidNoticeContents = getAndroidNotice(prjInfo);
		
		if(CoConstDef.FLAG_YES.equals(ossNotice.getPreviewOnly()) && !isEmpty(androidNoticeContents)) {
			return androidNoticeContents;
		} else {
			if(!isEmpty(androidNoticeContents)) {
				return "binAndroid"; 
			} else {
				ossNotice.setNetworkServerFlag(prjInfo.getNetworkServerType());

				// Convert Map to Apache Velocity Template
				return CommonFunction.VelocityTemplateToString(getNoticeHtmlInfo(ossNotice));
			}
		}
	}
	
	private String getAndroidNotice(Project prjInfo) throws IOException {
		// distribution type이 Android 이면서
		// Android Build Image이외의 OSS List가 포함된 경우는 병합
		// android build image만 사용된 경우는 notice.html을 반환한다.
		// 이슈로 인해 android project 기준이 변경이 되었으며 NoticeType이 20인 경우는 전부 Android Project형태를 띄고 있도록 변경이 되었음.
		Map<String, Object> NoticeInfo = projectMapper.getNoticeType(prjInfo.getPrjId());
		
		if(prjInfo != null 
				&& CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(avoidNull((String) NoticeInfo.get("noticeType"), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL)) 
				&& !isEmpty(prjInfo.getSrcAndroidNoticeFileId())) {
			T2File androidFile = fileService.selectFileInfoById(prjInfo.getSrcAndroidNoticeFileId());
			
			return CommonFunction.getStringFromFile(androidFile.getLogiPath() + "/" + androidFile.getLogiNm());
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
//	@Transactional // verify 수행시간이 50초이상인 경우(SELECT @@innodb_lock_wait_timeout;) 에러가 발생하는 것을 의심되어 Transactional 속성을 삭제함
	@Override
	public Map<String, Object> processVerification(Map<Object, Object> map, T2File file, Project project) {
		HashMap<String, Object> resMap = new HashMap<>();
		String resCd = "00";
		String resMsg = "";
		
		log.info("VERIFY START PROJECT ID : " + (String)map.get("prjId"));
		
		int allFileCount = 0;
		String[] result	=	null;
		String readmePath =	"";
		String exceptFileContent = "";
		String prjId =	(String)map.get("prjId");
		String fileSeq =	(String)map.get("fileSeq");
		int packagingFileIdx = (int)map.get("packagingFileIdx");
		List<String> fileSeqs =	(List<String>)map.get("fileSeqs");
		String filePath =	"";
		List<String> gridFilePaths =	(List<String>)map.get("gridFilePaths");
		List<String> gridComponentIds =	(List<String>)map.get("gridComponentIds");
		boolean isChangedPackageFile = (boolean)map.get("isChangedPackageFile");
		String packagingComment = (String)map.get("packagingComment");
		
		List<String> checkExceptionWordsList = CoCodeManager.getCodeNames(CoConstDef.CD_VERIFY_EXCEPTION_WORDS);
		List<String> checkExceptionIgnoreWorksList = CoCodeManager.getCodeNames(CoConstDef.CD_VERIFY_IGNORE_WORDS);
		
		Project prjInfo = null;
		boolean doUpdate = true;
		
		try {
			// 프로젝트 정보 취득 (verification status 에 따라 DB update 여부를 결정
			{
				prjInfo = projectService.getProjectBasicInfo(prjId);
				
				if(prjInfo != null && CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(prjInfo.getVerificationStatus())) {
					doUpdate = false;
				}
			}
			
			File chk_list_file = new File(VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list_"+packagingFileIdx);
			
			if(!chk_list_file.exists()) {
				isChangedPackageFile = true;
			}
			
			file.setFileSeq(fileSeq);
			file = fileMapper.getFileInfo(file);
			filePath = file.getLogiPath()+"/"+file.getLogiNm();
			
			log.debug("VERIFY TARGET FILE : " + filePath);
			
			if(packagingFileIdx == 1 && isChangedPackageFile) {
				ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "find " + VERIFY_PATH_OUTPUT + " -maxdepth 1 -name "+prjId+" -type d -exec rm -rf {} \\;"});
			}
			
			String exceptionWordsPatten = "proprietary\\|commercial";
			if(checkExceptionWordsList != null && !checkExceptionWordsList.isEmpty()) {
				exceptionWordsPatten = "";
				
				for(String s : checkExceptionWordsList) {
					if(!isEmpty(exceptionWordsPatten)) {
						exceptionWordsPatten += "\\|";
					}
					
					exceptionWordsPatten += s;
				}
			}
			
			log.info("VERIFY prjName : " + prjInfo.getPrjName());
			log.info("VERIFY OrigNm : " + file.getOrigNm());
			String projectNm = (prjInfo.getPrjName()).replace(" ", "@@");
			
			if(!isEmpty(prjInfo.getPrjVersion())){
				projectNm +="_"+(prjInfo.getPrjVersion()).replace(" ", "@@");
			}
			
			projectNm +="_"+Integer.toString(packagingFileIdx)+"("+(file.getOrigNm()).replace(" ", "@@")+")";
			
			String commandStr = VERIFY_BIN_PATH+"/verify "+filePath+" "+prjId+" "+exceptionWordsPatten+" "+projectNm+" "+packagingFileIdx+" "+VERIFY_HOME_PATH;
			log.info("VERIFY COMMAND : " + commandStr);

			log.info("VERIFY START : " + prjId);
			
			if(isChangedPackageFile){ // packageFile을 변경하지 않고 다시 verify할 경우 아래 shellCommander는 중복 동작 하지 않음.
				ShellCommander.shellCommandWaitFor(commandStr);
			}
			
			log.info("VERIFY END : " + prjId);
			
			//STEP 2 : Verify 진행후 특정 위치의 파일리스트 출력
			//STEP 3 : 결과 문자열 리스트값을 배열로 변환 		
			String chk_list_file_path = null;
			
			if(packagingFileIdx == 1) {
				chk_list_file_path = VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list_1";
			} else {
				chk_list_file_path = VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list";
			}
			
			String verify_chk_list = CommonFunction.getStringFromFile(chk_list_file_path).replaceAll(VERIFY_PATH_DECOMP +"/"+ prjId + "/", "");
			log.info("VERIFY Read verify_chk_list END : " + prjId);
			
			result = verify_chk_list.split(System.lineSeparator());
			allFileCount = StringUtils.countMatches(verify_chk_list, "*");
			
			// 압축 해제한 디렉토리를 포함하여 상호비교 하기 위해 제외할 디렉토리 명 추출
			// 두번째 디렉토리 까지 치환할 문자열 추출
			
			log.debug("file.getOrigNm() : " + file.getOrigNm());
			log.debug("file.getExt() : " + file.getExt());
			
			String tempFileOrgName = file.getOrigNm();
			String tempFileOrgExt = file.getExt();
			
			log.debug("lastIndexOf(file.getExt()) : " + tempFileOrgName.lastIndexOf(tempFileOrgExt));
			
			String rePath = FilenameUtils.removeExtension(tempFileOrgName);
			
			log.debug("rePath : " + rePath);
			
			if(rePath.indexOf(".tar") > -1){
				rePath = rePath.substring(0, rePath.lastIndexOf(".tar"));
			}
			
			String decompressionDirName = "/" + rePath;
			
			String packageFileName = rePath;
			String decompressionRootPath = "";
			
			// 사용자 입력과 packaging 파일의 디렉토리 정보 비교를 위해
			// 분석 결과를 격납 (dir or file n	ame : count)
			Map<String, Integer> deCompResultMap = new HashMap<>();
			List<String> readmePathList = new ArrayList<String>();
			if(result != null) {
				boolean isFirst = true;
				
				for(String s : result) {
					if(!isEmpty(s) && !(s.contains("(") && s.contains(")"))) {
						// packaging file name의 경우 Path로 인식하지 못하도록 처리함.

						boolean isFile = s.endsWith("*");
						s = s.replace(VERIFY_PATH_DECOMP +"/" + prjId + "/", "");
						s = s.replaceAll("//", "/");
						
						if(s.startsWith("/")) {
							s = s.substring(1);
						}
						
						if(s.endsWith("*")) {
							s = s.substring(0, s.length()-1);
						}
						
						if(s.endsWith("/")) {
							s = s.substring(0, s.length() -1);
						}
						
						if(isFirst) {
							// 첫번째 path를 압축을 푼 처번째 dir로 사용
							decompressionRootPath = s;
							
							isFirst = false;
						}
						
						int cnt = 0;
						
						//파일 path인 경우, 상위 dir의 파일 count를 +1 한다.
						if(isFile){
							String _dir = s;
							
							if(s.toUpperCase().indexOf("README") > -1) {
								readmePathList.add(s);
							}
							
							if(s.indexOf("/") > -1) {
								_dir = s.substring(0, s.lastIndexOf("/"));
							}
							
							if(deCompResultMap.containsKey(_dir)) {
								cnt = deCompResultMap.get(_dir);
							}
							
							cnt++;
							
							deCompResultMap.put(_dir, cnt);
						}
						
						deCompResultMap.put(s, 0);
					}
				}
			}
			
			List<String> paths = sortByValue(deCompResultMap);
			
			for(String path : paths){
				if(deCompResultMap.get(path) != null){
					deCompResultMap = setAddFileCount(deCompResultMap, path, (int)deCompResultMap.get(path));
				}
			}
			
			// 결과 file path에 대해서 4가지 허용 패턴으로 검사한다.
			Map<String, Integer> checkResultMap = new HashMap<>();
			List<String> pathCheckList1 = new ArrayList<>();
			List<String> pathCheckList2 = new ArrayList<>();
			List<String> pathCheckList3 = new ArrayList<>();
			List<String> pathCheckList4 = new ArrayList<>();
			
			List<String> pathCheckList11 = new ArrayList<>();
			List<String> pathCheckList21 = new ArrayList<>();
			List<String> pathCheckList31 = new ArrayList<>();
			List<String> pathCheckList41 = new ArrayList<>();
			
			List<String> pathCheckList12 = new ArrayList<>();
			List<String> pathCheckList22 = new ArrayList<>();
			List<String> pathCheckList32 = new ArrayList<>();
			List<String> pathCheckList42 = new ArrayList<>();
			
			List<String> pathCheckList13 = new ArrayList<>();
			List<String> pathCheckList23 = new ArrayList<>();
			List<String> pathCheckList33 = new ArrayList<>();
			List<String> pathCheckList43 = new ArrayList<>();
			
			List<String> pathCheckList14 = new ArrayList<>();
			List<String> pathCheckList24 = new ArrayList<>();
			List<String> pathCheckList34 = new ArrayList<>();
			List<String> pathCheckList44 = new ArrayList<>();
			
			List<String> pathCheckList15 = new ArrayList<>();
			List<String> pathCheckList25 = new ArrayList<>();
			List<String> pathCheckList35 = new ArrayList<>();
			List<String> pathCheckList45 = new ArrayList<>();

			List<String> pathCheckList16 = new ArrayList<>();
			List<String> pathCheckList26 = new ArrayList<>();
			List<String> pathCheckList36 = new ArrayList<>();
			List<String> pathCheckList46 = new ArrayList<>();
			
			for (String path : deCompResultMap.keySet()) {
				pathCheckList1.add(path);
				pathCheckList2.add("/" + path);
				pathCheckList3.add(path + "/");
				pathCheckList4.add("/"+path + "/");

				String replaceFilePath = path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
				
				if(replaceFilePath.startsWith("/")) {
					replaceFilePath = replaceFilePath.substring(1);
				}
				
				if(replaceFilePath.endsWith("/")) {
					replaceFilePath = replaceFilePath.substring(0, replaceFilePath.length()-1);
				}
				
				pathCheckList11.add(replaceFilePath);
				pathCheckList21.add("/" + replaceFilePath);
				pathCheckList31.add(replaceFilePath + "/");
				pathCheckList41.add("/"+replaceFilePath + "/");
				
				String addRootDir = decompressionDirName + "/" + path;
				
				if(addRootDir.startsWith("/")) {
					addRootDir = addRootDir.substring(1);
				}
				
				if(addRootDir.endsWith("/")) {
					addRootDir = addRootDir.substring(0, addRootDir.length()-1);
				}
				
				pathCheckList12.add(addRootDir);
				pathCheckList22.add("/" + addRootDir);
				pathCheckList32.add(addRootDir + "/");
				pathCheckList42.add("/"+addRootDir + "/");
				
				String addRootDirReplaceFilePath = decompressionDirName + "/" + path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
				
				if(addRootDirReplaceFilePath.startsWith("/")) {
					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(1);
				}
				
				if(addRootDirReplaceFilePath.endsWith("/")) {
					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(0, addRootDirReplaceFilePath.length());
				}
				
				pathCheckList13.add(addRootDirReplaceFilePath);
				pathCheckList23.add("/" + addRootDirReplaceFilePath);
				pathCheckList33.add(addRootDirReplaceFilePath + "/");
				pathCheckList43.add("/"+addRootDirReplaceFilePath + "/");

				String replaceRootDir = path.replaceFirst(packageFileName, "").replaceAll("//", "/");
				if(replaceRootDir.startsWith("/")) {
					replaceRootDir = replaceRootDir.substring(1);
				}
				
				if(replaceRootDir.endsWith("/")) {
					replaceRootDir = replaceRootDir.substring(0, replaceRootDir.length()-1);
				}
				
				pathCheckList14.add(replaceRootDir);
				pathCheckList24.add("/" + replaceRootDir);
				pathCheckList34.add(replaceRootDir + "/");
				pathCheckList44.add("/"+replaceRootDir + "/");
				
				String replaceRootDirReplaceFilePath = replaceRootDir;
				
				if(replaceRootDirReplaceFilePath.endsWith("*")) {
					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
				}
				
				if(replaceRootDirReplaceFilePath.endsWith("/")) {
					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
				}
				
				pathCheckList15.add(replaceRootDirReplaceFilePath);
				pathCheckList25.add("/" + replaceRootDirReplaceFilePath);
				pathCheckList35.add(replaceRootDirReplaceFilePath + "/");
				pathCheckList45.add("/"+replaceRootDirReplaceFilePath + "/");
				
				String replaceDecomFileRootDir = path.replaceFirst(decompressionRootPath, "").replaceAll("//", "/");
				
				if(replaceDecomFileRootDir.startsWith("/")) {
					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(1);
				}
				
				if(replaceDecomFileRootDir.endsWith("/")) {
					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(0, replaceDecomFileRootDir.length()-1);
				}
				
				pathCheckList16.add(replaceDecomFileRootDir);
				pathCheckList26.add("/" + replaceDecomFileRootDir);
				pathCheckList36.add(replaceDecomFileRootDir + "/");
				pathCheckList46.add("/"+replaceDecomFileRootDir + "/");
			}
			
			// 통합 Map 에 모든 허용 패턴을 저장
			int idx = 0;
			
			for(String s : pathCheckList1) {
				checkResultMap.put(s, deCompResultMap.containsKey(s) ? deCompResultMap.get(s) : 0);
				checkResultMap.put(pathCheckList2.get(idx), deCompResultMap.containsKey(pathCheckList2.get(idx)) ? deCompResultMap.get(pathCheckList2.get(idx)) : 0);
				checkResultMap.put(pathCheckList3.get(idx), deCompResultMap.containsKey(pathCheckList3.get(idx)) ? deCompResultMap.get(pathCheckList3.get(idx)) : 0);
				checkResultMap.put(pathCheckList4.get(idx), deCompResultMap.containsKey(pathCheckList4.get(idx)) ? deCompResultMap.get(pathCheckList4.get(idx)) : 0);

				checkResultMap.put(pathCheckList11.get(idx), deCompResultMap.containsKey(pathCheckList11.get(idx)) ? deCompResultMap.get(pathCheckList11.get(idx)) : 0);
				checkResultMap.put(pathCheckList21.get(idx), deCompResultMap.containsKey(pathCheckList21.get(idx)) ? deCompResultMap.get(pathCheckList21.get(idx)) : 0);
				checkResultMap.put(pathCheckList31.get(idx), deCompResultMap.containsKey(pathCheckList31.get(idx)) ? deCompResultMap.get(pathCheckList31.get(idx)) : 0);
				checkResultMap.put(pathCheckList41.get(idx), deCompResultMap.containsKey(pathCheckList41.get(idx)) ? deCompResultMap.get(pathCheckList41.get(idx)) : 0);

				checkResultMap.put(pathCheckList12.get(idx), deCompResultMap.containsKey(pathCheckList12.get(idx)) ? deCompResultMap.get(pathCheckList12.get(idx)) : 0);
				checkResultMap.put(pathCheckList22.get(idx), deCompResultMap.containsKey(pathCheckList22.get(idx)) ? deCompResultMap.get(pathCheckList22.get(idx)) : 0);
				checkResultMap.put(pathCheckList32.get(idx), deCompResultMap.containsKey(pathCheckList32.get(idx)) ? deCompResultMap.get(pathCheckList32.get(idx)) : 0);
				checkResultMap.put(pathCheckList42.get(idx), deCompResultMap.containsKey(pathCheckList42.get(idx)) ? deCompResultMap.get(pathCheckList42.get(idx)) : 0);

				checkResultMap.put(pathCheckList13.get(idx), deCompResultMap.containsKey(pathCheckList13.get(idx)) ? deCompResultMap.get(pathCheckList13.get(idx)) : 0);
				checkResultMap.put(pathCheckList23.get(idx), deCompResultMap.containsKey(pathCheckList23.get(idx)) ? deCompResultMap.get(pathCheckList23.get(idx)) : 0);
				checkResultMap.put(pathCheckList33.get(idx), deCompResultMap.containsKey(pathCheckList33.get(idx)) ? deCompResultMap.get(pathCheckList33.get(idx)) : 0);
				checkResultMap.put(pathCheckList43.get(idx), deCompResultMap.containsKey(pathCheckList43.get(idx)) ? deCompResultMap.get(pathCheckList43.get(idx)) : 0);

				checkResultMap.put(pathCheckList14.get(idx), deCompResultMap.containsKey(pathCheckList14.get(idx)) ? deCompResultMap.get(pathCheckList14.get(idx)) : 0);
				checkResultMap.put(pathCheckList24.get(idx), deCompResultMap.containsKey(pathCheckList24.get(idx)) ? deCompResultMap.get(pathCheckList24.get(idx)) : 0);
				checkResultMap.put(pathCheckList34.get(idx), deCompResultMap.containsKey(pathCheckList34.get(idx)) ? deCompResultMap.get(pathCheckList34.get(idx)) : 0);
				checkResultMap.put(pathCheckList44.get(idx), deCompResultMap.containsKey(pathCheckList44.get(idx)) ? deCompResultMap.get(pathCheckList44.get(idx)) : 0);

				checkResultMap.put(pathCheckList15.get(idx), deCompResultMap.containsKey(pathCheckList15.get(idx)) ? deCompResultMap.get(pathCheckList15.get(idx)) : 0);
				checkResultMap.put(pathCheckList25.get(idx), deCompResultMap.containsKey(pathCheckList25.get(idx)) ? deCompResultMap.get(pathCheckList25.get(idx)) : 0);
				checkResultMap.put(pathCheckList35.get(idx), deCompResultMap.containsKey(pathCheckList35.get(idx)) ? deCompResultMap.get(pathCheckList35.get(idx)) : 0);
				checkResultMap.put(pathCheckList45.get(idx), deCompResultMap.containsKey(pathCheckList45.get(idx)) ? deCompResultMap.get(pathCheckList45.get(idx)) : 0);

				checkResultMap.put(pathCheckList16.get(idx), deCompResultMap.containsKey(pathCheckList16.get(idx)) ? deCompResultMap.get(pathCheckList16.get(idx)) : 0);
				checkResultMap.put(pathCheckList26.get(idx), deCompResultMap.containsKey(pathCheckList26.get(idx)) ? deCompResultMap.get(pathCheckList26.get(idx)) : 0);
				checkResultMap.put(pathCheckList36.get(idx), deCompResultMap.containsKey(pathCheckList36.get(idx)) ? deCompResultMap.get(pathCheckList36.get(idx)) : 0);
				checkResultMap.put(pathCheckList46.get(idx), deCompResultMap.containsKey(pathCheckList46.get(idx)) ? deCompResultMap.get(pathCheckList46.get(idx)) : 0);

				idx ++;
			}

			
			int gridIdx = 0;
			ArrayList<String> gValidIdxlist = new ArrayList<>();
			HashMap<String,Object> gFileCountMap = new HashMap<>();
			boolean separatorErrFlag = false;
			
			log.info("VERIFY Path Check START -----------------");
			
			for(String gridPath : gridFilePaths){
				if(!separatorErrFlag) {
					separatorErrFlag = gridPath.contains("\\") ? true : false;
				}
				
				//사용자가 * 입력했을때
				if(!gridPath.trim().equals("/*") && !gridPath.trim().equals("/")){
					if(gridPath.endsWith("*")) {
						gridPath = gridPath.substring(0, gridPath.length()-1);
					}
					if(gridPath.startsWith(".")) {
						gridPath = gridPath.substring(1, gridPath.length());
					}
					// 앞뒤 path구분 제거
					if(gridPath.endsWith("/")) {
						gridPath = gridPath.substring(0, gridPath.length()-1);
					}
					if(gridPath.startsWith("/")) {
						gridPath = gridPath.substring(1);
					}
					
					int gFileCount = 0;
					
					/*
					 * SUB_STEP 1. verify 결과 배열을 받아온 grid filepath와 비교하여 실제로 그 path가 존재하는지 확인후 
					 * 존재하지 않을 경우 grid index 저장 
					 */
					boolean resultFlag = false;
					
					if(checkResultMap.containsKey(gridPath)) {
						resultFlag = true;
						gFileCount = checkResultMap.get(gridPath);
					}
					
					if(!resultFlag) {//path가 존재하지않을 때
						gValidIdxlist.add(gridComponentIds.get(gridIdx));
					} else {//path가 존재할 때
						// file을 직접 비교하는 경우 count되지 않기 때문에, 1로 고정
						// resultFlag == true 인경우는 존재하기 해당 path or file 대상이 존재한다는 의미이기 때문에 0이 될 수 없다.
						if(gFileCount == 0) {
							gFileCount = 1;
						}
						gFileCountMap.put(gridComponentIds.get(gridIdx), Integer.toString(gFileCount));
					}
				} else {
					gFileCountMap.put(gridComponentIds.get(gridIdx), Integer.toString(allFileCount));
				}
				
				gridIdx++;
			}
			
			log.info("VERIFY Path Check END -----------------");
			
			//STEP 4 : README 파일 존재 유무 확인(README 여러개 일경우도 생각해야함 ---차후)
			
			// depth가 낮은 readme 파일을 구하기 위해 sort
			if(packagingFileIdx == 1){ // packageFile에서 readMe File은 첫번째 file에서만 찾음.
//				List<String> sortList = new ArrayList<>(deCompResultMap.keySet());
				Collections.sort(readmePathList, new Comparator<String>() {

					@Override
					public int compare(String arg1, String arg2) {
						if(arg1.split("\\/").length > arg2.split("\\/").length) {
							return 1;
						} else if(arg1.split("\\/").length < arg2.split("\\/").length) {
							return -1;
						} else {
							return arg1.compareTo(arg2);
						}
					};
				});
				
//				String lastReadmeFilePath = "";
				for(String r : readmePathList) {
					String _upperPath = avoidNull(r).toUpperCase();
					
					if(_upperPath.endsWith("/")) {
						continue;
					}
					
//					String _currentReadmeFilePath = _upperPath.indexOf("/") < 0 ? _upperPath : _upperPath.substring(0,_upperPath.lastIndexOf("/"));
//					
//					if(!lastReadmeFilePath.equals(_currentReadmeFilePath)) {
//						if(!isEmpty(readmePath)) {
//							break;
//						}
//						
//						lastReadmeFilePath = _currentReadmeFilePath;
//					}
					
					if(_upperPath.indexOf("/") > -1) {
						_upperPath = _upperPath.substring(_upperPath.lastIndexOf("/"), _upperPath.length());
					}
					
					if(_upperPath.indexOf("README") > -1){
						String _readmePath = r.replaceAll("\\n", "");
						
						int afterDepthCnt = StringUtils.countMatches(_readmePath, "/");
						int beforeDepthCnt = StringUtils.countMatches(readmePath, "/");
						if(isEmpty(readmePath) || beforeDepthCnt > afterDepthCnt) {
							readmePath = _readmePath;
						}
					}
				}
			}

			
			String readmeFileName = "";
			//STEP 6 : README 파일 내용 출력
			if(!StringUtil.isEmpty(readmePath)){
				if(readmePath.indexOf("*") > -1){
					readmePath = readmePath.substring(0, readmePath.length()-1);
				}
				
				readmeFileName = readmePath;
				
				if(readmeFileName.indexOf("/") > -1) {
					readmeFileName = readmeFileName.substring(readmeFileName.lastIndexOf("/") + 1);
				}
				
				if(readmePath.indexOf(" ") > -1) {
					log.info("do space replase ok");
					
					readmePath = readmePath.replaceAll(" ", "*");
				}
				
				log.info("readmePath : " + readmePath);
				log.info("readmeFileName : " + readmeFileName);
				log.info("VERIFY Copy Readme file START -----------------");
				log.info("VERIFY README MV PATH : " + VERIFY_PATH_DECOMP +"/" + prjId +"/" + readmePath);
				
				if(isChangedPackageFile){
					ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "cp "+VERIFY_PATH_DECOMP +"/" + prjId +"/" + readmePath+ " " + VERIFY_PATH_OUTPUT +"/" + prjId +"/"});
				}
				
				log.info("VERIFY Copy Readme file END -----------------");
			}
			
			//STEP 7 : README 파일 내용 DB 에 저장
			if(doUpdate && packagingFileIdx == 1) {
				log.debug("VERIFY readme 등록");
				
				project.setPrjId(prjId);
				project.setReadmeFileName(readmeFileName);
				project.setReadmeYn(StringUtil.isEmpty(readmeFileName) ? CoConstDef.FLAG_NO : CoConstDef.FLAG_YES);
			
				projectService.registReadmeContent(project);
				
				log.debug("VERIFY readme 등록 완료");
			}
			
			//STEP 8 : Verify 동작 후 Except File Result DB 저장
			log.info("VERIFY Read exceptFileContent file START -----------------");
			
			exceptFileContent = CommonFunction.getStringFromFile(VERIFY_PATH_OUTPUT +"/"+prjId+"/except_file_result", VERIFY_PATH_DECOMP +"/" + prjId +"/", checkExceptionWordsList, checkExceptionIgnoreWorksList);
			
			log.info("VERIFY Read exceptFileContent file END -----------------");
			
			// 2017.03.23 yuns contents 용량이 너무 커서 DB로 관리하지 않음 (flag만 처리, empty여부로 체크하기 때문에 내용이 있을 경우 "Y" 만 등록
			project.setExceptFileContent(!isEmpty(exceptFileContent) ? CoConstDef.FLAG_YES : "");
			project.setVerifyFileContent(!isEmpty(verify_chk_list) ? CoConstDef.FLAG_YES : "");
			
			if(doUpdate) {
				projectService.registVerifyContents(project);
			}
			
			log.debug("VERIFY 파일내용 등록 완료");
			
			// 서버 디렉토리를 replace한 내용으로 새로운 파일로 다시 쓴다.
			if(!isEmpty(exceptFileContent)) {
				log.info("VERIFY writhFile exceptFileContent file START -----------------");
				
				FileUtil.writhFile(VERIFY_PATH_OUTPUT +"/" + prjId, CoConstDef.PACKAGING_VERIFY_FILENAME_PROPRIETARY, exceptFileContent.replaceAll(VERIFY_PATH_DECOMP +"/" + prjId +"/", ""));
				
				log.info("VERIFY writhFile exceptFileContent file END -----------------");
			}
			
			if(!isEmpty(verify_chk_list)) {
				log.info("VERIFY writhFile verify_chk_list file START -----------------");
				
				FileUtil.writhFile(VERIFY_PATH_OUTPUT +"/" + prjId, CoConstDef.PACKAGING_VERIFY_FILENAME_FILE_LIST, verify_chk_list.replaceAll(VERIFY_PATH_DECOMP +"/" + prjId +"/", ""));
				
				log.info("VERIFY writhFile verify_chk_list file END -----------------");
			}
			
			resCd="10";
			if(separatorErrFlag) {
				resMsg = getMessage("verify.path.error");
			} else {
				resMsg= getMessage(gValidIdxlist.isEmpty() ? "msg.common.success" : "msg.common.valid");
			}
			
			resMap.put("verifyValid", gValidIdxlist);
			resMap.put("verifyValidMsg", "path not found.");
			resMap.put("fileCounts", gFileCountMap);
			resMap.put("verifyReadme", readmeFileName);
			resMap.put("verifyCheckList", !isEmpty(verify_chk_list) ? CoConstDef.FLAG_YES : "");
			resMap.put("verifyProprietary", !isEmpty(exceptFileContent) ? CoConstDef.FLAG_YES : "");
			
			//path not found.가 1건이라도 있으면 status_verify_yn의 flag는 N으로 저장함.
			// packagingFileId, filePath는 1번만 저장하며, gValidIdxlist의 값때문에 마지막 fileSeq일때 저장함.
			if(doUpdate && packagingFileIdx == fileSeqs.size()) {
				// verify 버튼 클릭시 file path를 저장한다.
				if(gridComponentIds != null && !gridComponentIds.isEmpty()) {
					int seq = 0;
					
					for(String s : gridComponentIds){
						OssComponents param = new OssComponents();
						param.setComponentId(s);
						param.setFilePath(gridFilePaths.get(seq++));
						
						verificationMapper.updateVerifyFilePath(param);
					}
				}
				
				{						
					Project prjParam = new Project();
					prjParam.setPrjId(prjId);
					prjParam.setPackageFileId(fileSeqs.get(0));
					prjParam.setPackageFileId2(fileSeqs.size() >= 2 ? fileSeqs.get(1) : null);
					prjParam.setPackageFileId3(fileSeqs.size() >= 3 ? fileSeqs.get(2) : null);

					if(!isEmpty(prjInfo.getDestributionStatus())){
						prjParam.setStatusVerifyYn("C");
					} else {
						prjParam.setStatusVerifyYn(CoConstDef.FLAG_YES);
					}
					
					verificationMapper.updatePackageFile(prjParam);
				}
				
				
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
				commHisBean.setReferenceId(prjId); commHisBean.setContents(packagingComment);
				  
				commentService.registComment(commHisBean);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			resCd="20";
			resMsg="process failed. (server error)";
		} finally {
			try {
				if(isChangedPackageFile){
					ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "find " + VERIFY_PATH_DECOMP + " -maxdepth 1 -name "+prjId+" -type d -exec rm -rf {} \\;"});
				}
				
				log.info("VERIFY delete decomp file END -----------------");
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
			}
		}
		
		resMap.put("resCd", resCd);
		resMap.put("resMsg", resMsg);
		
		log.debug("verify 처리 완료 resCd : " + resCd);
		log.debug("verify 처리 완료 resMsg : " + resMsg);
		
		return resMap;
	}
	
	@Override
	public void updateVerifyFileCount(HashMap<String,Object> fileCounts) {
		for(String componentId : fileCounts.keySet()){
			OssComponents param = new OssComponents();
			param.setComponentId(componentId);
			param.setVerifyFileCount((String) fileCounts.get(componentId));
			
			verificationMapper.updateVerifyFileCount(param);
		}
	}
	
	@Override
	public void updateVerifyFileCount(ArrayList<String> fileCounts) {
		for(String componentId : fileCounts){
			OssComponents param = new OssComponents();
			param.setComponentId(componentId);
			param.setVerifyFileCount(" ");
			
			verificationMapper.updateVerifyFileCount(param);
		}
	}
	
	/**
	 * APSL 또는 AGPL 라이선스를 사용하는 oss를 포함하고 있는 경우 false
	 * notice를 만들 필요가 없는 경우 true를 반환
	 */
	@Override
	public boolean checkNetworkServer(String prjId) {
		OssNotice ossNotice = new OssNotice();
		ossNotice.setPrjId(prjId);
		ossNotice.setNetworkServerFlag(CoConstDef.FLAG_YES);
		List<OssComponents> ossComponentList = verificationMapper.selectVerificationNotice(ossNotice);
		
		return ossComponentList == null || ossComponentList.isEmpty();
	}
	
	@Override
	@Transactional
	public void updateStatusWithConfirm(Project project, OssNotice ossNotice) throws Exception {
		projectService.updateProjectStatus(project);
		
		boolean makeZipFile = false;
		String spdxComment = "";
		
		// html simple
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleHTMLYn())) {
			ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES);
			ossNotice.setFileType("html");
			project.setSimpleHtmlFileId(getNoticeTextFileForPreview(ossNotice, true));
			makeZipFile = true;
		}

		// text
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadNoticeTextYn())) {
			ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_NO);
			ossNotice.setFileType("text");
			project.setNoticeTextFileId(getNoticeTextFileForPreview(ossNotice, true));
			makeZipFile = true;
		}
		
		// text simple
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleTextYn())) {
			ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES);
			ossNotice.setFileType("text");
			project.setSimpleTextFileId(getNoticeTextFileForPreview(ossNotice, true));
			makeZipFile = true;
		}
		
		// SPDX
		String spdxSheetFileId = null;
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXSheetYn())) {
			spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", project.getPrjId(), EXPORT_TEMPLATE_PATH);
			if(!isEmpty(spdxSheetFileId)) {
				T2File spdxFileInfo = fileService.selectFileInfo(spdxSheetFileId);
				Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
				String fileName = "spdx_" + CommonFunction.getNoticeFileName(prjInfo.getPrjId(), prjInfo.getPrjName(), prjInfo.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), "");
				fileName += "."+FilenameUtils.getExtension(spdxFileInfo.getOrigNm());
				String filePath = NOTICE_PATH + "/" + prjInfo.getPrjId();
				FileUtil.moveTo(spdxFileInfo.getLogiPath() + "/" + spdxFileInfo.getLogiNm(), filePath, fileName);
				project.setSpdxSheetFileId(fileService.registFileDownload(filePath, fileName, fileName));
				spdxSheetFileId = ossNotice.getSpdxSheetFileId();
				makeZipFile = true;
			}

		}
		
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXRdfYn())) {
			if(isEmpty(spdxSheetFileId)) {
				spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", project.getPrjId(), EXPORT_TEMPLATE_PATH);
			}
			
			if(!isEmpty(spdxSheetFileId)) {
				T2File spdxFileInfo = fileService.selectFileInfo(spdxSheetFileId);
				String sheetFullPath = spdxFileInfo.getLogiPath();
				
				if(!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}
				
				sheetFullPath += spdxFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(spdxFileInfo.getLogiNm())+".rdf";
				String resultFileName = FilenameUtils.getBaseName(spdxFileInfo.getOrigNm())+".rdf";
				String tagFullPath = spdxFileInfo.getLogiPath();
				
				if(!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}
				
				tagFullPath += targetFileName;
				SPDXUtil2.spreadsheetToRDF(project.getPrjId(), sheetFullPath, tagFullPath);
				File spdxRdfFile = new File(tagFullPath);
				
				if(spdxRdfFile.exists() && spdxRdfFile.length() <= 0) {
					if(!isEmpty(spdxComment)) {
						spdxComment += "<br>";
					}
					
					spdxComment += getMessage("spdx.rdf.failure"); 
				}
				
				String filePath = NOTICE_PATH + "/" + project.getPrjId();
				FileUtil.moveTo(tagFullPath, filePath, resultFileName);
				project.setSpdxRdfFileId(fileService.registFileDownload(filePath, resultFileName, resultFileName));
				
				makeZipFile = true;
			}
		}
		
		if(CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXTagYn())) {
			if(isEmpty(spdxSheetFileId)) {
				spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", project.getPrjId(), EXPORT_TEMPLATE_PATH);
			}
			
			if(!isEmpty(spdxSheetFileId)) {
				T2File spdxFileInfo = fileService.selectFileInfo(spdxSheetFileId);
				
				String sheetFullPath = spdxFileInfo.getLogiPath();
				
				if(!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}
				
				sheetFullPath += spdxFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(spdxFileInfo.getLogiNm())+".tag";
				String resultFileName = FilenameUtils.getBaseName(spdxFileInfo.getOrigNm())+".tag";
				String tagFullPath = spdxFileInfo.getLogiPath();
				
				if(!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}
				
				tagFullPath += targetFileName;
				SPDXUtil2.spreadsheetToTAG(project.getPrjId(), sheetFullPath, tagFullPath);
				
				File spdxTafFile = new File(tagFullPath);
				
				if(spdxTafFile.exists() && spdxTafFile.length() <= 0) {
					if(!isEmpty(spdxComment)) {
						spdxComment += "<br>";
					}
					
					spdxComment += getMessage("spdx.tag.failure"); 
				}
				
				String filePath = NOTICE_PATH + "/" + project.getPrjId();
				FileUtil.moveTo(tagFullPath, filePath, resultFileName);
				project.setSpdxTagFileId(fileService.registFileDownload(filePath, resultFileName, resultFileName));
				
				makeZipFile = true;
			}
		}
		
		// zip파일 생성
		if(makeZipFile) {
			String noticeRootDir = NOTICE_PATH;
			ossNotice.setFileType(".zip");
			Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
			String zipFileName = CommonFunction.getNoticeFileName(prjInfo.getPrjId(), prjInfo.getPrjName(), prjInfo.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), ".zip");
			FileUtil.zip(noticeRootDir + "/" + project.getPrjId(), noticeRootDir, zipFileName, "OSS Notice");
			
			String zipFileId = fileService.registFileDownload(noticeRootDir, zipFileName, zipFileName);
			project.setZipFileId(zipFileId);
		}
		
		verificationMapper.updateNoticeFileInfoEtc(project); // file info update
		
		if(!isEmpty(spdxComment)) { // spdx failure => comment regist
			try {
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
				commHisBean.setReferenceId(project.getPrjId());
				commHisBean.setContents(spdxComment);
				commHisBean.setStatus(CoCodeManager.getCodeExpString(CoConstDef.CD_IDENTIFICATION_STATUS, CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM));
				
				commentService.registComment(commHisBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void changePackageFileNameDistributeFormat(String prjId) {
		// 프로젝트 기본정보 취득
		Project prjBean = new Project();
		prjBean.setPrjId(prjId);
		prjBean = projectMapper.selectProjectMaster2(prjBean);
		List<String> packageFileIds = new ArrayList<String>();
		
		if(!isEmpty(prjBean.getPackageFileId())) {
			packageFileIds.add(prjBean.getPackageFileId());
		}
		
		if(!isEmpty(prjBean.getPackageFileId2())) {
			packageFileIds.add(prjBean.getPackageFileId2());
		}
		
		if(!isEmpty(prjBean.getPackageFileId3())) {
			packageFileIds.add(prjBean.getPackageFileId3());
		}
		
		int fileSeq = 1;
		
		for(String packageFileId : packageFileIds){
			T2File packageFileInfo = new T2File();
			packageFileInfo.setFileSeq(packageFileId);
			packageFileInfo = fileMapper.getFileInfo(packageFileInfo);
			
			if(packageFileInfo != null) {
				String orgFileName = packageFileInfo.getOrigNm();
				// Packaging > Confirm시 Packaging 파일명 변경 건
				String paramSeq = (packageFileIds.size() > 1 ? Integer.toString(fileSeq++) : ""); 
				String chgFileName = getPackageFileName(prjBean.getPrjName(), prjBean.getPrjVersion(), packageFileInfo.getOrigNm(), paramSeq);
				
				packageFileInfo.setOrigNm(chgFileName);
				
				fileMapper.upateOrgFileName(packageFileInfo);
				
				// 이력을 남긴다.
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
				commHisBean.setReferenceId(prjId);
				commHisBean.setContents("Changed File Name (\""+orgFileName+"\") to \""+chgFileName+"\"  ");
				
				commentService.registComment(commHisBean);
			}
		}
	}
	
	private String getPackageFileName(String prjName, String prjVersion, String orgFileName, String fileSeq) {
		String fileName = prjName;
		
		if(!isEmpty(prjVersion)) {
			fileName += "_" + prjVersion;
		}
		
		if(!isEmpty(fileSeq)){
			fileName += "_" + fileSeq;
		}
		
		// file명에 사용할 수 없는 특수문자 체크
		if(!FileUtil.isValidFileName(fileName)) {
			fileName = FileUtil.makeValidFileName(fileName, "_");
		}
		
		String fileExt = FilenameUtils.getExtension(orgFileName);
		
		if(orgFileName.toLowerCase().endsWith(".tgz.gz")) {
			fileExt = "tgz.gz";
		} else if(orgFileName.toLowerCase().endsWith(".tar.bz2")) {
			fileExt = "tar.bz2";
		} else if(orgFileName.toLowerCase().endsWith(".tar.gz")) {
			fileExt = "tar.gz";
		}
		
		if(fileExt.startsWith(".")) {
			fileExt = fileExt.substring(1);
		}
		
		return fileName + "." + fileExt;
	}
	
	@Override
	@Transactional
	public Map<String, Object> registOssNotice(OssNotice ossNotice) {
		HashMap<String, Object> resMap = new HashMap<>();
		String resCd="00";
		String resMsg="none.";
		
		try{
			Project project = new Project();
			project.setPrjId(ossNotice.getPrjId());
			project = projectMapper.selectProjectMaster(project);
			
			Map<String, Object> result = projectMapper.getNoticeType(ossNotice.getPrjId());
			
			// android project는 notice를 사용하지 않음.
			if(!CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equalsIgnoreCase(avoidNull((String) result.get("noticeType")))) {
				if(CoConstDef.FLAG_YES.equals(ossNotice.getEditNoticeYn())){
					verificationMapper.insertOssNotice(ossNotice);
				}else if(CoConstDef.FLAG_NO.equals(ossNotice.getEditNoticeYn())){
					verificationMapper.updateOssNotice(ossNotice);
				}
			}
			
			projectMapper.updateWithoutVerifyYn(ossNotice);
			
			
			if(isEmpty(project.getVerificationStatus())){
				verificationMapper.updateVerificationStatusProgress(ossNotice);
			}
			
			resCd="10";
			resMsg="process success.";
		}catch(Exception e){
			log.error(e.getMessage(), e);
			
			resMsg="backend exception error.";
		}
		
		resMap.put("resCd", resCd);
		resMap.put("resMsg", resMsg);
		
		return resMap;
	}
	
	@Override
	public String getNoticeHtmlFileForPreview(OssNotice ossNotice) throws IOException {
		Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
		
		return getNoticeVelocityTemplateFileForPreview(getNoticeHtml(ossNotice), prjInfo, ossNotice.getFileType(), ossNotice.getSimpleNoticeFlag());	
	}
	
	private String getNoticeVelocityTemplateFileForPreview(String contents, Project project, String fileType, String simpleFlag) throws IOException {
		return getNoticeVelocityTemplateFileForPreview(contents, project, fileType, simpleFlag, false);
	}
	private String getNoticeVelocityTemplateFileForPreview(String contents, Project project, String fileType, String simpleFlag, boolean isConfirm) throws IOException {
		String fileId = "";
		String filePath = "";
		String fileName = "";
		// Text 형식 OSS 고지문 생성 시 개행문자 변경
		// System.getProperty("line.separator") => "\n" => "\r\n" 변경 
		String line = "\r\n";
		
		if(fileType == "text"){
			fileId = "";
			filePath = NOTICE_PATH + ( isConfirm ? "/" : "/preview/") + project.getPrjId();
			fileName = (CoConstDef.FLAG_YES.equals(simpleFlag) ? "simple_" : "") + CommonFunction.getNoticeFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), ( isConfirm ? CommonFunction.getCurrentDateTime("yyMMdd") : DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN) ), fileType);
			contents = contents.replace("\n", line).replace(",)", ")").replace("<br>", line).replace("&copy;", "©").replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&#39;", "\'");
		} else {
			fileId = "";
			filePath = NOTICE_PATH + ( isConfirm ? "/" : "/preview/") + project.getPrjId();
			fileName = (CoConstDef.FLAG_YES.equals(simpleFlag) ? "simple_" : "") + CommonFunction.getNoticeFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), ( isConfirm ? CommonFunction.getCurrentDateTime("yyMMdd") : DateUtil.getCurrentDateTime(DateUtil.DATE_HMS_PATTERN) ), fileType);
			
			// custom edit를 사용하고, packaging confirm 인 경우 이면서 simple인 경우
			// license text 부분만 다시 변경한다.
			if(isConfirm && CoConstDef.FLAG_YES.equals(simpleFlag) && CoConstDef.FLAG_YES.equals(project.getUseCustomNoticeYn())) {
				// 이미 생성된 고지문구 파일의 내용을 가져온다.
				T2File defaultNoticeFileInfo = fileService.selectFileInfo(project.getNoticeFileId());
				
				if(defaultNoticeFileInfo != null) {
					File noticeFile = new File(defaultNoticeFileInfo.getLogiPath() + "/" + defaultNoticeFileInfo.getLogiNm());
					if(noticeFile.exists()) {
						Document doc = Jsoup.parse(noticeFile, "UTF8");
						Document doc2 = Jsoup.parse(contents);
						
						doc.select("body > p.bdTop.license").remove();
						doc.select("body").append(doc2.select("body > p.bdTop.license").toString());
						doc.select("body").append(doc2.select("body > ul.bdTop2.license").toString());
						
						contents = doc.toString();
					}
				}
			}
		}

		if(FileUtil.writhFile(filePath, fileName, contents)) {
			// 파일 등록
			fileId = fileService.registFileDownload(filePath, fileName, fileName);
		}
		
		return fileId;
	}
	
	@Override
	public String getNoticeTextFileForPreview(OssNotice ossNotice) throws IOException {
		Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
		
		return getNoticeVelocityTemplateFileForPreview(getNoticeHtml(ossNotice), prjInfo, ossNotice.getFileType(), ossNotice.getSimpleNoticeFlag());	
	}
	
	@Override
	public String getNoticeTextFileForPreview(OssNotice ossNotice, boolean isConfirm) throws IOException {
		Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
		
		return getNoticeVelocityTemplateFileForPreview(getNoticeHtml(ossNotice), prjInfo, ossNotice.getFileType(), ossNotice.getSimpleNoticeFlag(), isConfirm);	
	}
	
	@Override
	public Map<String, Object> getReuseProject(Project project) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Project> list = null;

		try {
			int records = projectMapper.selectReuseProjectTotalCount(project);
			project.setTotListSize(records);
			
			if(records > 0){
				list = projectMapper.selectReuseProject(project);
			}
			
			map.put("page", project.getCurPage());
			map.put("total", project.getTotBlockSize());
			map.put("records", records);
			map.put("rows", list);

		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return map;
	}
	
	@Override
	public Map<String, Object> getReuseProjectPackagingFile(Project project) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<T2File> list = null;

		try {
			if(!isEmpty(project.getPrjId())){
				list = projectMapper.selectReusePackagingFileList(project.getPrjId());
			}
			
			map.put("rows", list);
			map.put("prjId", project.getPrjId());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return map;
	}

	@Override
	public boolean setReusePackagingFile(Map<String, Object> map) {
		String prjId = (String) map.get("prjId");
		String fileSeq = (String) map.get("fileSeq");
		String refPrjId = (String) map.get("refPrjId");
		String refFileSeq = (String) map.get("refFileSeq");
		
		 int result = verificationMapper.setPackagingReuseMap(prjId, fileSeq, refPrjId, refFileSeq);
		 
		 return result > 0 ? true : false;
	}
	
	@Override
	public ResponseEntity<FileSystemResource> getPackage(String prjId, String path) throws IOException{
		T2File fileInfo = verificationMapper.selectPackageFileName(prjId, "");
		
		String filePath = fileInfo.getLogiPath()+"/"+fileInfo.getLogiNm();
		
		return noticeToResponseEntity(filePath, fileInfo.getOrigNm());
	}
	
	@Override
	public ResponseEntity<FileSystemResource> getPackageMulti(String prjId, String path, String fileIdx) throws IOException{
		T2File fileInfo = verificationMapper.selectPackageFileName(prjId, fileIdx);
		
		String filePath = fileInfo.getLogiPath()+"/"+fileInfo.getLogiNm();
		
		return noticeToResponseEntity(filePath, fileInfo.getOrigNm());
	}
	
	@Override
	public ResponseEntity<FileSystemResource> getNotice(String prjId,
			String rESOURCE_PUBLIC_DOWNLOAD_NOTICE_FILE_PATH_PREFIX) throws IOException {
		String fileName = "";
		String filePath = rESOURCE_PUBLIC_DOWNLOAD_NOTICE_FILE_PATH_PREFIX;

		Project project = new Project();
		project.setPrjId(prjId);
		project = projectMapper.selectProjectMaster(project);
		
		oss.fosslight.domain.File noticeFile = null;
		
		if(!isEmpty(project.getZipFileId())) {
			noticeFile = verificationMapper.selectVerificationFile(project.getZipFileId());
			fileName =  noticeFile.getOrigNm();
			filePath += File.separator+fileName;
		} else {
			noticeFile = verificationMapper.selectVerificationFile(project.getNoticeFileId());
			fileName =  noticeFile.getOrigNm();
			filePath += File.separator+prjId+File.separator+fileName;
		}
					
		return noticeToResponseEntity(filePath, fileName);
	}

	@Override
	public Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice) {
		Map<String, Object> model = new HashMap<String, Object>();
		
		String noticeType = "";
		String prjName = "";
		String prjVersion = "";
		String prjId = "";
		String distributeSite = "";
		int dashSeq = 0;
		
		// NETWORK SERVER 여부를 체크한다.
		
		Project project = new Project();
		project.setPrjId(ossNotice.getPrjId());
		
		project = projectMapper.getProjectBasicInfo(project);
		
		if(project != null){
			if(isEmpty(prjName)) {
				prjName = project.getPrjName();
			}
			
			if(isEmpty(prjId)) {
				prjId = project.getPrjId();
			}
			
			if(isEmpty(prjVersion)) {
				prjVersion = project.getPrjVersion();
			}
			
			if(isEmpty(distributeSite)) {
				distributeSite = project.getDistributeTarget();
			}
		}
		
		List<OssComponents> ossComponentList = verificationMapper.selectVerificationNotice(ossNotice);
		
		// TYPE별 구분
		Map<String, OssComponents> noticeInfo = new LinkedHashMap<>();
		Map<String, OssComponents> srcInfo = new LinkedHashMap<>();
		Map<String, OssComponentsLicense> licenseInfo = new LinkedHashMap<>();
		
		OssComponents ossComponent;
		
		for(OssComponents bean : ossComponentList) {
			String componentKey = (bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
			
			if("-".equals(bean.getOssName())) {
				componentKey += dashSeq++;
			}
			
			// type
			boolean isDisclosure = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType());
			// 2017.05.16 add by yuns start
			// obligation을 특정할 수 없는 oss도 bom에 merge 되도록 수정하면서, identification confirm시 refDiv가 '50'(고지대상)에 obligation을 특정할 수 없는 oss도 포함되어 등록되어
			// confirm 처리에서 obligation이 고지의무가 있거나 소스코드 공개의무가 있는 경우만 '50'으로 copy되도록 수정하였으나, 여기서 한번도 필터링함
			boolean isNotice = CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType());
			
			if(!isDisclosure && !isNotice) {
				continue;
			}
			
			// 2017.07.05
			// Accompanied with source code 의 경우
			// 소스공개여부와 상관없이 모두 소스공개가 필요한 oss table에 표시
			if(CoConstDef.CD_DTL_NOTICE_TYPE_ACCOMPANIED.equals(ossNotice.getNoticeType())) {
				isDisclosure = true;
			}
			
			// 2017.05.16 add by yuns end
			boolean addDisclosure = isDisclosure && srcInfo.containsKey(componentKey);
			boolean addNotice = !isDisclosure && noticeInfo.containsKey(componentKey);
			
			if(addDisclosure) {
				ossComponent = srcInfo.get(componentKey);
			} else if(addNotice) {
				ossComponent = noticeInfo.get(componentKey);
			} else {
				ossComponent = bean;
			}
					
			// 라이선스 정보 생성
			OssComponentsLicense license = new OssComponentsLicense();
			license.setLicenseId(bean.getLicenseId());
			license.setLicenseName(bean.getLicenseName());
			license.setLicenseText(bean.getLicenseText());
			license.setAttribution(bean.getAttribution());
			// 하나의 oss에 대해서 동일한 LICENSE가 복수 표시되는 현상 
			// 일단 여기서 막는다. (쿼리가 잘못된 건지, DATA가 꼬이는건지 모르겠음)
			if(!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
				ossComponent.addOssComponentsLicense(license);
				// OSS의 Copyright text를 수정하였음에도 Packaging > Notice Preview에 업데이트 안 됨.
				// MULTI LICENSE를 가지는 oss의 개별로 추가된 copyright의 경우, Identification Confirm시에 DB에 업데이트한 정보를 기준으로 추출되기 때문에, preview 단계에서 오류가 발견되어 수정하여도 반영되지 않는다
				// verification단계에서의 oss_component_license는 oss_license의 license등록 순번을 가지고 있지 않기 때문에 (exclude된 license는 이관하지 않음)
				// 여기서 oss id와 license id를 이용하여 찾는다.
				// 동이한 라이선스를 or 구분으로 여러번 정의한 경우 문제가 될 수 있으나, 동일한 oss의 동일한 license의 경우 같은 copyright를 추가한다는 전제하에 적용함 (이부분에서 추가적인 이슉가 발생할 경우 대응방법이 복잡해짐)
				 if(CoConstDef.FLAG_NO.equals(ossComponent.getAdminCheckYn())) {
					 bean.setOssCopyright(findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright()));
				
					 // multi license 추가 copyright
					 if(!isEmpty(bean.getOssCopyright())) {
						 String addCopyright = avoidNull(ossComponent.getCopyrightText());
						
						 if(!isEmpty(ossComponent.getCopyrightText())) {
							 addCopyright += "\r\n";
						 }
						
						 addCopyright += bean.getOssCopyright();
						 ossComponent.setCopyrightText(addCopyright);
					 }
				 }
			}
			
			if(isDisclosure) {
				if(addDisclosure) {
					srcInfo.replace(componentKey, ossComponent);
				} else {
					srcInfo.put(componentKey, ossComponent);
				}
			} else {
				if(addNotice) {
					noticeInfo.replace(componentKey, ossComponent);
				} else {
					noticeInfo.put(componentKey, ossComponent);
				}
			}
			
			if(!licenseInfo.containsKey(license.getLicenseName())) {
				licenseInfo.put(license.getLicenseName(), license);
			}
		}
		
		// CLASS 파일만 등록한 경우 라이선스 정보만 추가한다.
		// OSS NAME을 하이픈 ('-') 으로 등록한 경우 (고지문구에 라이선스만 추가)
		List<OssComponents> addOssComponentList = verificationMapper.selectVerificationNoticeClassAppend(ossNotice);
		
		if(addOssComponentList != null) {
			for(OssComponents bean : addOssComponentList) {
				String componentKey = (bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
				
				if("-".equals(bean.getOssName())) {
					componentKey += dashSeq++;
				}
				
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());
				bean.addOssComponentsLicense(license);
				
				if(CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())
						|| CoConstDef.CD_DTL_NOTICE_TYPE_ACCOMPANIED.equals(ossNotice.getNoticeType())) { // Accompanied with source code 의 경우 source 공개 의무
					srcInfo.put(componentKey, bean);
				} else {
					noticeInfo.put(componentKey, bean);
				}
				
				if(!licenseInfo.containsKey(license.getLicenseName())) {
					licenseInfo.put(componentKey, license);
				}
			}
		}
		
		boolean isTextNotice = "text".equals(ossNotice.getFileType());
		
		Map<String, String> ossAttributionMap = new HashMap<>();
		// 개행처리 및 velocity용 list 생성
		List<OssComponents> noticeList = new ArrayList<>();
		
		for(OssComponents bean : noticeInfo.values()) {
			if(isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getOssAttribution()))));
			}

			if(!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if(!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			// oss 없이 라이선스만 확인한 경우, 고지문구 oss tag가 link로 생성되지 않도록 downloadlocation을 초기화
			if(isEmpty(bean.getOssName()) || "-".equals(bean.getOssName())) {
				bean.setDownloadLocation("");
			}
			
			noticeList.add(bean);
		}
		List<OssComponents> srcList = new ArrayList<>();
		
		for(OssComponents bean : srcInfo.values()) {
			if(isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getOssAttribution()))));
			}
			

			if(!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if(!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			// oss 없이 라이선스만 확인한 경우, 고지문구 oss tag가 link로 생성되지 않도록 downloadlocation을 초기화
			if(isEmpty(bean.getOssName()) || "-".equals(bean.getOssName())) {
				bean.setDownloadLocation("");
			}
			
			srcList.add(bean);
		}
		
		List<OssComponentsLicense> licenseList = new ArrayList<>();
		List<OssComponentsLicense> licenseListUrls = new ArrayList<>(); //simple version용
		List<OssComponentsLicense> attributionList = new ArrayList<>();
		List<String> ossAttributionList = new ArrayList<>();
		
		// 정렬
		TreeMap<String, OssComponentsLicense> licenseTreeMap = new TreeMap<>( licenseInfo );
		
		for(OssComponentsLicense bean : licenseTreeMap.values()) {
			if(isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml(avoidNull(bean.getLicenseText()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getLicenseText()))));
			}
			
			// 배포사이트 license text url
			licenseList.add(bean);
			
			if(CoConstDef.FLAG_YES.equals(ossNotice.getSimpleNoticeFlag())) {
				LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_BY_ID.get(bean.getLicenseId());
				
				if(licenseBean != null) {
//					String simpleLicenseFileName = !isEmpty(licenseBean.getShortIdentifier()) ? licenseBean.getShortIdentifier() : licenseBean.getLicenseNameTemp();
//					String distributeUrl = CoCodeManager.getCodeExpString(CoConstDef.CD_DISTRIBUTE_CODE, CoConstDef.CD_DTL_DISTRIBUTE_LGE);
//					simpleLicenseFileName = simpleLicenseFileName.replaceAll(" ", "_").replaceAll("/", "_") + ".html";
//					distributeUrl += "/license/" + simpleLicenseFileName;
					boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
					licenseBean.setDomain(ossNotice.getDomain());
					
					bean.setWebpage(CommonFunction.makeLicenseInternalUrl(licenseBean, distributionFlag));
					licenseListUrls.add(bean);
				}
			}

			if(!isEmpty(bean.getAttribution())) {
				bean.setAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml(avoidNull(bean.getAttribution()))));
				attributionList.add(bean);
			}
		}

		TreeMap<String, String> ossAttributionTreeMap = new TreeMap<>( ossAttributionMap );
		ossAttributionList.addAll(ossAttributionTreeMap.values());
		
		// 배포 사이트 구분에 따라 참조 코드가 달라짐
		String noticeInfoCode = CoConstDef.CD_DTL_DISTRIBUTE_SKS.equals(avoidNull(distributeSite, CoConstDef.CD_DTL_DISTRIBUTE_LGE)) ? CoConstDef.CD_NOTICE_DEFAULT_SKS : CoConstDef.CD_NOTICE_DEFAULT;

		noticeType = avoidNull(ossNotice.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL);
		
		String companyNameFull = ossNotice.getCompanyNameFull();
		String distributionSiteUrl = ossNotice.getDistributionSiteUrl();
		String email = ossNotice.getEmail();
		String appendedContentsTEXT = ossNotice.getAppendedTEXT();
		String appendedContents = ossNotice.getAppended();
		
		if(!isEmpty(distributionSiteUrl) && !(distributionSiteUrl.startsWith("http://") || distributionSiteUrl.startsWith("https://") || distributionSiteUrl.startsWith("ftp://"))) {
			distributionSiteUrl = "http://" + distributionSiteUrl;
		}
		model.put("noticeType", noticeType);
		model.put("noticeTitle", CommonFunction.getNoticeFileName(prjId, prjName, prjVersion, CommonFunction.getCurrentDateTime("yyMMdd"), ossNotice.getFileType()));
		model.put("companyNameFull", companyNameFull);
		model.put("distributionSiteUrl", distributionSiteUrl);
		model.put("email", email);
		model.put("noticeObligationSize", noticeList.size());
		model.put("disclosureObligationSize", srcList.size());
		model.put("noticeObligationList", noticeList);
		model.put("disclosureObligationList", srcList);
		/* ui 개선버전으로 신규 추가된 flag */
		model.put("editNoticeYn", ossNotice.getEditNoticeYn());
		model.put("editCompanyYn", ossNotice.getEditCompanyYn());
		model.put("editDistributionSiteUrlYn", ossNotice.getEditDistributionSiteUrlYn());
		model.put("editEmailYn", ossNotice.getEditEmailYn());
		model.put("hideOssVersionYn", ossNotice.getHideOssVersionYn());
		model.put("editAppendedYn", ossNotice.getEditAppendedYn());
		
		/*//ui 개선버전으로 신규 추가된 flag */
		if(CoConstDef.FLAG_YES.equals(ossNotice.getSimpleNoticeFlag())) {
			model.put("licenseListUrls", licenseListUrls);
		} else {
			model.put("licenseList", licenseList);
		}
		
		model.put("attributionList", attributionList.isEmpty() ? null : attributionList);
		model.put("ossAttributionList", ossAttributionList.isEmpty() ? null : ossAttributionList);
		
		if("text".equals(ossNotice.getFileType())){
			model.put("appended", avoidNull(appendedContentsTEXT, "").replaceAll("&nbsp;", " "));
		} else {
			model.put("appended", appendedContents);
		}

		if("text".equals(ossNotice.getFileType())){
			model.put("templateURL", CoCodeManager.getCodeExpString(noticeInfoCode, CoConstDef.CD_DTL_NOTICE_TEXT_TEMPLATE));
		} else {
			model.put("templateURL", CoCodeManager.getCodeExpString(noticeInfoCode, CoConstDef.CD_DTL_NOTICE_DEFAULT_TEMPLATE));
		}

		model.put("addOssComponentList", addOssComponentList);
		model.put("isSimpleNotice", avoidNull(ossNotice.getIsSimpleNotice(), CoConstDef.FLAG_NO));
		
		return model;
	}
	
	private boolean checkLicenseDuplicated(List<OssComponentsLicense> ossComponentsLicense,
			OssComponentsLicense license) {
		if(ossComponentsLicense != null) {
			for(OssComponentsLicense bean : ossComponentsLicense) {
				if(bean.getLicenseId().equals(license.getLicenseId())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * multi license oss의 경우에 해당하며, oss master에 추가된 license별 copyright정보를 반환한다.
	 * @param ossId
	 * @param licenseId
	 * @param ossCopyright
	 * @return
	 */
	private String findAddedOssCopyright(String ossId, String licenseId, String ossCopyright) {
		if(!isEmpty(ossId) && !isEmpty(licenseId)) {
			OssMaster bean = CoCodeManager.OSS_INFO_BY_ID.get(ossId);
			
			for(OssLicense license : bean.getOssLicenses()) {
				if(licenseId.equals(license.getLicenseId()) && !isEmpty(license.getOssCopyright())) {
					return license.getOssCopyright();
				}
			}
		}
		
		return ossCopyright;
	}
	
	@Override
	public Map<String, Integer> setAddFileCount(Map<String, Integer> deCompResultMap, String url, int fileCnt) {
		try {
			url = url.substring(0, url.lastIndexOf("/"));
			
			int pFileCnt = deCompResultMap.get(url);
			deCompResultMap.put(url, fileCnt + pFileCnt);
			
			if(deCompResultMap.get(url.substring(0, url.lastIndexOf("/"))) != null){
				setAddFileCount(deCompResultMap, url, fileCnt);
			}
			
			return deCompResultMap;
		} catch (Exception e) {
			return deCompResultMap;
		}	
	}

	@Override
	public List<String> sortByValue(Map<String, Integer> map) throws Exception {
        List<String> list = new ArrayList<String>();
        list.addAll(map.keySet());
         
        Collections.sort(list, new Comparator<Object>(){
            public int compare(Object o1,Object o2){
                int o1_depth = o1.toString().split("/").length;
                int o2_depth = o2.toString().split("/").length;
                
                return o1_depth-o2_depth;
            }
        });
        
        return list;
    }

	@Override
	public List<OssComponents> setMergeGridData(List<OssComponents> gridData) {
		List<OssComponents> tempData = new ArrayList<OssComponents>();
		List<OssComponents> resultGridData = new ArrayList<OssComponents>();
		
		String groupColumn = "";

		final Comparator<OssComponents> comp = Comparator.comparing((OssComponents o) -> o.getOssName()+"|"+o.getOssVersion());
		gridData = gridData.stream().sorted(comp).collect(Collectors.toList());
		
		for(OssComponents info : gridData) {
			if(isEmpty(groupColumn)) {
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
			}
						
			if(groupColumn.equals(info.getOssName() + "-" + info.getOssVersion()) // 같은 groupColumn이면 데이터를 쌓음
					&& !("-".equals(info.getOssName()) 
					&& !"NA".equals(info.getLicenseType()))) { // 단, OSS Name: - 이면서, License Type: Proprietary이 아닌 경우 Row를 합치지 않음.
				tempData.add(info);
			} else { // 다른 grouping
				setMergeData(tempData, resultGridData);
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
				tempData.clear();
				
				tempData.add(info);
			}
		}
		
		setMergeData(tempData, resultGridData);
		
		return resultGridData;
	}	
	
	public static void setMergeData(List<OssComponents> tempData, List<OssComponents> resultGridData){
		if(tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<OssComponents>() {
				@Override
				public int compare(OssComponents o1, OssComponents o2) {
					if(o1.getLicenseName().length() >= o2.getLicenseName().length()) {
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			OssComponents rtnBean = null;
			
			for(OssComponents temp : tempData) {
				if(rtnBean == null) {
					rtnBean = temp;
					
					continue;
				}
				
				String key = temp.getOssName() + "-" + temp.getLicenseType();
				
				if("--NA".equals(key)) {
					if(!rtnBean.getLicenseName().contains(temp.getLicenseName())) {
						resultGridData.add(rtnBean);
						rtnBean = temp;
						
						continue;
					}
				}
				
				for(String licenseName : temp.getLicenseName().split(",")) {
					boolean equalFlag = false;
					
					for(String rtnLicenseName : rtnBean.getLicenseName().split(",")) {
						if(rtnLicenseName.equals(licenseName)) {
							equalFlag = true;
							
							break;
						}
					}
					
					if(!equalFlag) {
						rtnBean.setLicenseName(rtnBean.getLicenseName() + "," + licenseName);
					}
				}
			}
			
			resultGridData.add(rtnBean);
		}
	}

	@Override
	public void setUploadFileSave(String prjId, String fileSeq, String registFileId) throws Exception {
		Project prjParam = new Project(); 
		prjParam.setPrjId(prjId);
		  
		Project project = projectMapper.selectProjectMaster(prjParam);
		
		if (fileSeq.equals("1")) {
			prjParam.setPackageFileId(registFileId);
			prjParam.setPackageFileId2(project.getPackageFileId2() != null ? project.getPackageFileId2() : null);
			prjParam.setPackageFileId3(project.getPackageFileId3() != null ? project.getPackageFileId3() : null);
		}else if (fileSeq.equals("2")) {
			prjParam.setPackageFileId(project.getPackageFileId() != null ? project.getPackageFileId() : null);
			prjParam.setPackageFileId2(registFileId);
			prjParam.setPackageFileId3(project.getPackageFileId3() != null ? project.getPackageFileId3() : null);
		}else {
			prjParam.setPackageFileId(project.getPackageFileId() != null ? project.getPackageFileId() : null);
			prjParam.setPackageFileId2(project.getPackageFileId2() != null ? project.getPackageFileId2() : null);
			prjParam.setPackageFileId3(registFileId);
		}
				
		List<String> fileSeqs = new ArrayList<String>(); 
		if (prjParam.getPackageFileId() != null) {
			fileSeqs.add(prjParam.getPackageFileId()); 
		}  
		if (prjParam.getPackageFileId2() != null) {
			fileSeqs.add(prjParam.getPackageFileId2()); 
		} 
		if (prjParam.getPackageFileId3() != null) {
			fileSeqs.add(prjParam.getPackageFileId3()); 
		}
					
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("prjId", prjId);
		map.put("fileSeqs", fileSeqs);
		
		String packagingComment = "";
		
		try {
			packagingComment = fileService.setClearFiles(map);
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		prjParam.setStatusVerifyYn("N");
		// project_master packageFileId update
		verificationMapper.updatePackageFile(prjParam);
		
		// commentHistory regist
		if (!packagingComment.equals("")) {
			CommentsHistory commHisBean = new CommentsHistory();
			commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
			commHisBean.setReferenceId(prjId); 
			commHisBean.setContents(packagingComment);
			
			commentService.registComment(commHisBean);
		}
	}
}
