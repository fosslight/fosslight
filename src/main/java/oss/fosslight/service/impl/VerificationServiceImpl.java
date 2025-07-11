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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
import oss.fosslight.domain.ProjectIdentification;
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
import oss.fosslight.util.PdfUtil;
import oss.fosslight.util.SPDXUtil2;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

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
	private static String REVIEW_REPORT_PATH = CommonFunction.emptyCheckProperty("reviewReport.path", "/reviewReport");
	private static String FOSSLIGHT_BINARY_SCANNER_PATH = CommonFunction.emptyCheckProperty("verify.fosslight.binary.scanner.path", "/fosslight_binary");
	
	@Override
	public Map<String, Object> getVerificationOne(Project project) {
		// 1. Verification정보
		// 2. Comment 정보
		HashMap<String, Object> map = new HashMap<String, Object>();
		Project prj = projectMapper.selectProjectMaster(project.getPrjId());

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
	public OssNotice selectOssNoticeOne2(String prjId) {
		Project project = new Project();
		project.setPrjId(prjId);
		
		return verificationMapper.selectOssNoticeOne2(project);
	}
	
	@Override
	public List<OssComponents> getVerifyOssList(Project projectMaster) {
		List<OssComponents> componentList = verificationMapper.selectVerifyOssList(projectMaster);
		if (!CollectionUtils.isEmpty(componentList) && CoConstDef.FLAG_YES.equals(avoidNull(projectMaster.getNetworkServerFlag()))) {
			List<OssComponents> collateOssComponentList = null;
			for (OssComponents ossComponent : componentList) {
				String restrictionStr = ossComponent.getRestriction();
				if (!isEmpty(restrictionStr)) {
					String[] restrictions = restrictionStr.split(",");
					for (String restriction : restrictions) {
						if (CoConstDef.CD_LICENSE_NETWORK_RESTRICTION.equals(restriction.trim())) {
							if (collateOssComponentList == null) {
								collateOssComponentList = new ArrayList<>();
							}
							collateOssComponentList.add(ossComponent);
							break;
						}
					}
				}
			}
			componentList = collateOssComponentList;
		}
		
		if (componentList != null && !componentList.isEmpty() && componentList.get(0) == null) {
			componentList = new ArrayList<>();
		}
		
		return componentList;
	}

	@Override
	public boolean getChangedPackageFile(String prjId, List<String> fileSeqs) {
		String packageFileId = fileSeqs.get(0);
		String packageFileId2 = fileSeqs.size() > 1 ? fileSeqs.get(1) : null;
		String packageFileId3 = fileSeqs.size() > 2 ? fileSeqs.get(2) : null;
		String packageFileId4 = fileSeqs.size() > 3 ? fileSeqs.get(3) : null;
		String packageFileId5 = fileSeqs.size() > 4 ? fileSeqs.get(4) : null;
		
		int result = verificationMapper.checkPackagingFileId(prjId, packageFileId, packageFileId2, packageFileId3, packageFileId4, packageFileId5);

		if (result > 0){
			return false;
		} else {
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
			List<String> fileTypeSeqs =	(List<String>) map.get("fileTypeSeqs");
			String prjId = (String) map.get("prjId");
			String deleteFlag = (String) map.get("deleteFlag");
			String verifyFlag = (String) map.get("statusVerifyYn");
			String deleteFiles = (String) map.get("deleteFiles");
			String deleteComment = "";
			String uploadComment = "";
			
			// verify 버튼 클릭시 file path를 저장한다.
			if (gridComponentIds != null && !gridComponentIds.isEmpty()) {
				int idx = 0;
				
				for (String s : gridComponentIds){
					OssComponents param = new OssComponents();
					param.setComponentId(s);
					param.setFilePath(gridFilePaths.get(idx++));
					
					if (verifyFlag.equals(CoConstDef.FLAG_YES)){
						param.setVerifyFileCount("");
						
						verificationMapper.updateVerifyFileCount(param);
					}
					
					verificationMapper.updateVerifyFilePath(param);
				}
			}
			
			if (!isEmpty(prjId)) {
				Project prjParam = new Project();
				prjParam.setPrjId(prjId);
				ArrayList<String> newPackagingFileIdList = new ArrayList<String>();
				newPackagingFileIdList.add(fileSeqs.size() > 0 ? fileSeqs.get(0) : null);
				newPackagingFileIdList.add(fileSeqs.size() > 1 ? fileSeqs.get(1) : null);
				newPackagingFileIdList.add(fileSeqs.size() > 2 ? fileSeqs.get(2) : null);
				newPackagingFileIdList.add(fileSeqs.size() > 3 ? fileSeqs.get(3) : null);
				newPackagingFileIdList.add(fileSeqs.size() > 4 ? fileSeqs.get(4) : null);
				prjParam.setPackageFileId(newPackagingFileIdList.get(0));
				prjParam.setPackageFileId2(newPackagingFileIdList.get(1));
				prjParam.setPackageFileId3(newPackagingFileIdList.get(2));
				prjParam.setPackageFileId4(newPackagingFileIdList.get(3));
				prjParam.setPackageFileId5(newPackagingFileIdList.get(4));
				
				ArrayList<String> newPackagingFileTypeList = new ArrayList<String>();
				newPackagingFileTypeList.add(fileTypeSeqs.size() > 0 ? fileTypeSeqs.get(0) : null);
				newPackagingFileTypeList.add(fileTypeSeqs.size() > 1 ? fileTypeSeqs.get(1) : null);
				newPackagingFileTypeList.add(fileTypeSeqs.size() > 2 ? fileTypeSeqs.get(2) : null);
				newPackagingFileTypeList.add(fileTypeSeqs.size() > 3 ? fileTypeSeqs.get(3) : null);
				newPackagingFileTypeList.add(fileTypeSeqs.size() > 4 ? fileTypeSeqs.get(4) : null);
				prjParam.setPackageFileType1(newPackagingFileTypeList.get(0));
				prjParam.setPackageFileType2(newPackagingFileTypeList.get(1));
				prjParam.setPackageFileType3(newPackagingFileTypeList.get(2));
				prjParam.setPackageFileType4(newPackagingFileTypeList.get(3));
				prjParam.setPackageFileType5(newPackagingFileTypeList.get(4));
				
				if (deleteFiles.equals(CoConstDef.FLAG_YES)){
					prjParam.setStatusVerifyYn(CoConstDef.FLAG_NO);
				}			
				
				List<T2File> deletePhysicalFileList = new ArrayList<>();
				
				// packaging File comment
				try {
					Project project = projectMapper.selectProjectMaster(prjParam.getPrjId());
					ArrayList<String> origPackagingFileIdList = new ArrayList<String>();
					origPackagingFileIdList.add(project.getPackageFileId());
					origPackagingFileIdList.add(project.getPackageFileId2());
					origPackagingFileIdList.add(project.getPackageFileId3());
					origPackagingFileIdList.add(project.getPackageFileId4());
					origPackagingFileIdList.add(project.getPackageFileId5());
					
					int idx = 0;
					
					for (String fileId : origPackagingFileIdList){
						T2File fileInfo = new T2File();
						
						if (!isEmpty(fileId) && !fileId.equals(newPackagingFileIdList.get(idx))){
							fileInfo.setFileSeq(fileId);
							fileInfo = fileMapper.getFileInfo(fileInfo);
							deleteComment += "Packaging file, "+fileInfo.getOrigNm()+", was deleted by "+loginUserName()+". <br>";

							log.info("[Prj " + prjId + "] fileSeq(" +  fileInfo.getFileSeq() + ") " + deleteComment);

							if (verificationMapper.countSameLogiFile(fileInfo) == 1) {
								deletePhysicalFileList.add(fileInfo);
							}
							//delete logic path
							verificationMapper.deletePackagingFileInfo(fileInfo);
							verificationMapper.deleteReuseFileInfo(fileInfo);

						}
						
						if (!isEmpty(newPackagingFileIdList.get(idx)) && !newPackagingFileIdList.get(idx).equals(fileId)){
							fileInfo.setFileSeq(newPackagingFileIdList.get(idx));
							fileInfo = fileMapper.getFileInfo(fileInfo);
							oss.fosslight.domain.File result = verificationMapper.selectVerificationFile(newPackagingFileIdList.get(idx));
							
							if (CoConstDef.FLAG_YES.equals(result.getReuseFlag())){
								uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was loaded from Project ID: "+result.getRefPrjId()+" by "+loginUserName()+". <br>";
								log.info("[Prj " + prjId + "] fileSeq(" +  fileInfo.getFileSeq() + ") " + uploadComment);
							}else{
								uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was uploaded by "+loginUserName()+". <br>";
								log.info("[Prj " + prjId + "] fileSeq(" +  fileInfo.getFileSeq() + ") " + uploadComment);
							}
						}
						
						idx++;
					}
					
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
					commHisBean.setReferenceId(prjId);
					commHisBean.setContents(deleteComment+uploadComment);
					
					commentService.registComment(commHisBean, false);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				
//				verificationMapper.updatePackagingReuseMap(prjParam);
				verificationMapper.updatePackageFile(prjParam);
				
				// delete physical file
				for (T2File delFile : deletePhysicalFileList){
					fileService.deletePhysicalFile(delFile, "VERIFY");
					log.info("[Prj " + prjId + "] "+ "Remove physical file for " + delFile.getOrigNm() + " : "
							+ delFile.getLogiPath() + "/" + delFile.getLogiNm());
				}
				
				if (CoConstDef.FLAG_YES.equals(deleteFlag)){
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
		if (CoConstDef.CD_NOTICE_TYPE_NA.equals(prjInfo.getNoticeType())) {
			return true;
		}
		
		prjInfo.setUseCustomNoticeYn(!isEmpty(contents) ? CoConstDef.FLAG_YES : CoConstDef.FLAG_NO);
		contents = avoidNull(contents, getNoticeHtml(ossNotice));
		
		if ("binAndroid".equals(contents)) {
			return getAndroidNoticeVelocityTemplateFile(prjInfo); // file Content 옮기는 기능에서 files.copy로 변경
		} else {
			return getNoticeVelocityTemplateFile(contents, prjInfo);	
		}
	}

	@Override
	public boolean getReviewReportPdfFile(String prjId) throws IOException {
		return getReviewReportPdfFile(prjId, null);
	}

	@Override
	public boolean getReviewReportPdfFile(String prjId, String contents) throws IOException {
		Project prjInfo = projectService.getProjectBasicInfo(prjId);

		try {
			contents = avoidNull(contents, PdfUtil.getInstance().getReviewReportHtml(prjId));
			if(contents == null) {
				Project projectParam = new Project();
				projectParam.setPrjId(prjId);
				projectParam.setReviewReportFileId(null);
				verificationMapper.updateReviewReportFileInfo(projectParam);
				return false;
			}
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}

		if ("binAndroid".equals(contents)) {
			return getAndroidNoticeVelocityTemplateFile(prjInfo); // file Content 옮기는 기능에서 files.copy로 변경
		} else {
			return getReviewReportVelocityTemplateFile(contents, prjInfo);
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
			
			if (isEmpty(project.getSrcAndroidNoticeXmlId()) && !isEmpty(project.getSrcAndroidNoticeFileId())) {
				baseFile = fileMapper.selectFileInfoById(project.getSrcAndroidNoticeFileId());
				basePath = CommonFunction.emptyCheckProperty("upload.path", "/upload") + "/" + baseFile.getLogiNm();
			} else {
				baseFile = fileMapper.selectFileInfoById(project.getSrcAndroidNoticeXmlId());
				basePath = baseFile.getLogiPath() + "/" + baseFile.getLogiNm();
			}
			
			// 이전에 생성된 파일은 모두 삭제한다.
			Path rootPath = Paths.get(filePath);
			
			if (rootPath.toFile().exists()) {
				for (String _fName : rootPath.toFile().list()) {
					Files.deleteIfExists(rootPath.resolve(_fName));
					
					T2File file = new T2File();
					file.setLogiNm(_fName);
					file.setLogiPath(filePath);
					
					int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(file);
					
					if (returnSuccess > 0){
						log.debug(filePath + "/" + _fName + " is delete success.");
					}else{
						log.debug(filePath + "/" + _fName + " is delete failed.");
					}
				}
			}
			
			String fileName = CommonFunction.getNoticeFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), "html");
			
			if (oss.fosslight.util.FileUtil.copyFile(basePath, filePath, fileName)) {
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
			if (rootPath.toFile().exists()) {
				for (String _fName : rootPath.toFile().list()) {
					Files.deleteIfExists(rootPath.resolve(_fName));

					T2File file = new T2File();
					file.setLogiNm(_fName);
					file.setLogiPath(filePath);

					int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(file);

					if (returnSuccess > 0) {
						log.debug(filePath + "/" + _fName + " is delete success.");
					} else {
						log.debug(filePath + "/" + _fName + " is delete failed.");
					}
				}
			}			
			
			String fileName = CommonFunction.getNoticeFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), "html");
			
			if (oss.fosslight.util.FileUtil.writeFile(filePath, fileName, contents)) {
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

	public boolean getReviewReportVelocityTemplateFile(String contents, Project project) {
		boolean procResult = true;

		try {
			// file path and name 설정
			// 파일 path : <upload_home>/reviewReport/
			// 파일명 : 임시: 프로젝트ID_yyyyMMdd\
			String filePath = REVIEW_REPORT_PATH + "/" + project.getPrjId();

			// 이전에 생성된 pdf 파일은 모두 삭제한다.
			Path rootPath = Paths.get(filePath);
			if (rootPath.toFile().exists()) {
				for (String _fName : rootPath.toFile().list()) {
					String[] fNameList = _fName.split("\\.");
					if (fNameList[fNameList.length - 1].equals("pdf")) {
						Files.deleteIfExists(rootPath.resolve(_fName));

						T2File file = new T2File();
						file.setLogiNm(_fName);
						file.setLogiPath(filePath);

						int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(file);

						if (returnSuccess > 0){
							log.debug(filePath + "/" + _fName + " is delete success.");
							fileService.deletePhysicalFile(file, "verify");
						}else{
							log.debug(filePath + "/" + _fName + " is delete failed.");
						}
					}
				}
			}

			String fileName = CommonFunction.getReviewReportFileName(project.getPrjId(), project.getPrjName(), project.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), ".pdf");

			if (oss.fosslight.util.FileUtil.writeReviewReportFile(filePath, fileName, contents)) {
				// 파일 등록
				String FileSeq = fileService.registFileWithFileName(filePath, fileName);

				// project 정보 업데이트
				Project projectParam = new Project();
				projectParam.setPrjId(project.getPrjId());
				projectParam.setReviewReportFileId(FileSeq);

				verificationMapper.updateReviewReportFileInfo(projectParam);
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
		
		if (CoConstDef.FLAG_YES.equals(ossNotice.getPreviewOnly()) && !isEmpty(androidNoticeContents)) {
			return androidNoticeContents;
		} else {
			if (!isEmpty(androidNoticeContents)) {
				return "binAndroid"; 
			} else {
				ossNotice.setNetworkServerFlag(prjInfo.getNetworkServerType());

				// Convert Map to Apache Velocity Template
				return CommonFunction.VelocityTemplateToString(getNoticeHtmlInfo(ossNotice, true));
			}
		}
	}
	
	private String getAndroidNotice(Project prjInfo) throws IOException {
		// distribution type이 Android 이면서
		// Android Build Image이외의 OSS List가 포함된 경우는 병합
		// android build image만 사용된 경우는 notice.html을 반환한다.
		// 이슈로 인해 android project 기준이 변경이 되었으며 NoticeType이 20인 경우는 전부 Android Project형태를 띄고 있도록 변경이 되었음.
		Map<String, Object> NoticeInfo = projectMapper.getNoticeType(prjInfo.getPrjId());
		
		if (prjInfo != null 
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
				
				if (prjInfo != null && CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(prjInfo.getVerificationStatus())) {
					doUpdate = false;
				}
			}
			
			File chk_list_file = new File(VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list_"+packagingFileIdx);
			
			if (!chk_list_file.exists()) {
				isChangedPackageFile = true;
			}
			
			file.setFileSeq(fileSeq);
			file = fileMapper.getFileInfo(file);
			filePath = file.getLogiPath()+"/"+file.getLogiNm();
			
			log.debug("VERIFY TARGET FILE : " + filePath);
			
			if (packagingFileIdx == 1 && isChangedPackageFile) {
				ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "find " + VERIFY_PATH_OUTPUT + " -maxdepth 1 -name "+prjId+" -type d -exec rm -rf {} \\;"});
			}
			
			String exceptionWordsPatten = "proprietary\\|commercial";
			if (checkExceptionWordsList != null && !checkExceptionWordsList.isEmpty()) {
				exceptionWordsPatten = "";
				
				for (String s : checkExceptionWordsList) {
					if (!isEmpty(exceptionWordsPatten)) {
						exceptionWordsPatten += "\\|";
					}
					
					exceptionWordsPatten += s;
				}
			}
			
			log.info("VERIFY prjName : " + prjInfo.getPrjName());
			log.info("VERIFY OrigNm : " + file.getOrigNm());
			String projectNm = (prjInfo.getPrjName()).replace(" ", "@@");
			
			if (!isEmpty(prjInfo.getPrjVersion())){
				projectNm +="_"+(prjInfo.getPrjVersion()).replace(" ", "@@");
			}
			
			projectNm +="_"+Integer.toString(packagingFileIdx)+"("+(file.getOrigNm()).replace(" ", "@@")+")";
			
			String commandStr = VERIFY_BIN_PATH+"/verify "+filePath+" "+prjId+" "+exceptionWordsPatten+" "+projectNm+" "+packagingFileIdx+" "+VERIFY_HOME_PATH+" "+FOSSLIGHT_BINARY_SCANNER_PATH ;
			log.info("VERIFY COMMAND : " + commandStr);

			log.info("VERIFY START : " + prjId);
			
			if (isChangedPackageFile){ // packageFile을 변경하지 않고 다시 verify할 경우 아래 shellCommander는 중복 동작 하지 않음.
				ShellCommander.shellCommandWaitFor(commandStr);
			}
			
			log.info("VERIFY END : " + prjId);
			
			//STEP 2 : Verify 진행후 특정 위치의 파일리스트 출력
			//STEP 3 : 결과 문자열 리스트값을 배열로 변환 		
			String chk_list_file_path = null;
			
			if (packagingFileIdx == 1) {
				chk_list_file_path = VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list_1";
			} else {
				chk_list_file_path = VERIFY_PATH_OUTPUT+"/"+prjId+"/verify_chk_list";
			}
			
			String verify_chk_list = CommonFunction.getStringFromFile(chk_list_file_path).replaceAll(VERIFY_PATH_DECOMP +"/"+ prjId + "/", "");
			if (verify_chk_list.contains(VERIFY_PATH_DECOMP +"/"+ prjId)) {
				verify_chk_list = verify_chk_list.replaceAll(VERIFY_PATH_DECOMP +"/"+ prjId, "");
			}
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
			
			if (rePath.indexOf(".tar") > -1){
				rePath = rePath.substring(0, rePath.lastIndexOf(".tar"));
			}
			if (rePath.indexOf(".zip") > -1){
				rePath = rePath.substring(0, rePath.lastIndexOf(".zip"));
			}
			String decompressionDirName = "/" + rePath;
			
			String packageFileName = rePath;
			String decompressionRootPath = "";
			List<String> collectDataDeCompResultList = new ArrayList<>();
			String firstPathName = "";
			
			// 사용자 입력과 packaging 파일의 디렉토리 정보 비교를 위해
			// 분석 결과를 격납 (dir or file n	ame : count)
			Map<String, Integer> deCompResultMap = new HashMap<>();
			Map<String, Integer> deCompResultFileMap = new HashMap<>();
			Map<String, Integer> secondDeCompResultMap = new HashMap<>();
			Map<String, Integer> secondDeCompResultFileMap = new HashMap<>();
			List<String> readmePathList = new ArrayList<String>();
			if (result != null) {
				boolean isFirst = true;
				
				for (String s : result) {
					if (s.contains("?")) s = s.replaceAll("[?]", "0x3F");
					
					if (!isEmpty(s) && !(s.contains("(") && s.contains(")"))) {
						// packaging file name의 경우 Path로 인식하지 못하도록 처리함.

						boolean isFile = s.endsWith("*");
						s = s.replace(VERIFY_PATH_DECOMP +"/" + prjId + "/", "");
						s = s.replaceAll("//", "/");
						
						if (s.startsWith("/")) {
							s = s.substring(1);
						}
						
						if (s.startsWith(packageFileName)) {
							String removePackageFileName = s.replace(packageFileName, "");
							if (removePackageFileName.startsWith("/")) {
								removePackageFileName = removePackageFileName.substring(1);
							}
							if (!deCompResultMap.containsKey(removePackageFileName)) collectDataDeCompResultList.add(removePackageFileName);
						} else {
							String addPackageFileName = packageFileName + "/" + s;
							if (!deCompResultMap.containsKey(addPackageFileName)) collectDataDeCompResultList.add(addPackageFileName);
						}
						
						if (!isEmpty(firstPathName) && !packageFileName.equals(firstPathName)) {
							if (s.startsWith(firstPathName)) {
								String removeFirstPathName = s.replace(firstPathName, "");
								if (removeFirstPathName.startsWith("/")) {
									removeFirstPathName = removeFirstPathName.substring(1);
								}
								if (!deCompResultMap.containsKey(removeFirstPathName)) {
									collectDataDeCompResultList.add(removeFirstPathName);
								}
							}
						}
						
						if (s.endsWith("*")) {
							s = s.substring(0, s.length()-1);
						}
						
						if (s.endsWith("/")) {
							s = s.substring(0, s.length() -1);
						}
						
						if (isFirst) {
							if (!isFile) {
								// 첫번째 path를 압축을 푼 처번째 dir로 사용
								decompressionRootPath = s;
								
								isFirst = false;
							}
						}
						
						int cnt = 0;
						boolean isNotDir = false;
						
						//파일 path인 경우, 상위 dir의 파일 count를 +1 한다.
						if (isFile){
							String _dir = s;
							
							if (s.toUpperCase().indexOf("README") > -1) {
								readmePathList.add(s);
							}
							
							if (s.indexOf("/") > -1) {
								_dir = s.substring(0, s.lastIndexOf("/"));
								if (isEmpty(firstPathName) && !isEmpty(s)) {
									firstPathName = _dir;
								}
							}
							
//							if (deCompResultMap.containsKey(_dir)) {
//								cnt = deCompResultMap.get(_dir);
//							}
//							
//							cnt++;
							
							deCompResultMap.put(_dir, cnt);
							
							if (!_dir.equalsIgnoreCase(s)) {
								isNotDir = true;
							}
						} else {
							deCompResultMap.put(s, 0);
						}
						
						if (isNotDir) {
							int fileCnt = 0;
							
							if (deCompResultFileMap.containsKey(s)) {
								fileCnt = deCompResultFileMap.get(s);
							}
							
							fileCnt++;
							
							deCompResultFileMap.put(s, fileCnt);
						}
					}
				}
				
				result = null;
			}
			
			if (collectDataDeCompResultList != null && !collectDataDeCompResultList.isEmpty()) {
				for (String s : collectDataDeCompResultList) {
					boolean isFile = s.endsWith("*");
					
					if (s.startsWith("/")) {
						s = s.substring(1);
					}
					
					if (s.endsWith("*")) {
						s = s.substring(0, s.length()-1);
					}
					
					if (s.endsWith("/")) {
						s = s.substring(0, s.length() -1);
					}
					
					int cnt = 0;
					boolean isNotDir = false;
					
					if (!deCompResultMap.containsKey(s)) {
						if (isFile){
							String _dir = s;
							
							if (s.toUpperCase().indexOf("README") > -1) {
								continue;
							}
							
							if (s.indexOf("/") > -1) {
								_dir = s.substring(0, s.lastIndexOf("/"));
							}
							
//							if (secondDeCompResultMap.containsKey(_dir)) {
//								cnt = secondDeCompResultMap.get(_dir);
//							}
//							
//							cnt++;
							
							secondDeCompResultMap.put(_dir, cnt);
							
							if (!_dir.equalsIgnoreCase(s)) {
								isNotDir = true;
							}
						} else {
							secondDeCompResultMap.put(s, 0);
						}
						
						if (isNotDir) {
							int fileCnt = 0;
							
							if (secondDeCompResultFileMap.containsKey(s)) {
								fileCnt = secondDeCompResultFileMap.get(s);
							}
							
							fileCnt++;
							
							secondDeCompResultFileMap.put(s, fileCnt);
						}
					}
				}
				
				collectDataDeCompResultList = null;
			}
			
			if (!secondDeCompResultMap.isEmpty()) {
				for (String path : secondDeCompResultMap.keySet()) {
					if (!deCompResultMap.containsKey(path)) {
						deCompResultMap.put(path, secondDeCompResultMap.get(path));
					}
				}
			}
			
			secondDeCompResultMap.clear();
			
			if (!secondDeCompResultFileMap.isEmpty()) {
				for (String path : secondDeCompResultFileMap.keySet()) {
					if (!deCompResultFileMap.containsKey(path)) {
						deCompResultFileMap.put(path, secondDeCompResultFileMap.get(path));
					}
				}
			}
			
			secondDeCompResultFileMap.clear();
			
			List<String> paths = sortByValue(deCompResultFileMap);
			
			for (String path : paths){
				deCompResultMap = setAddFileCount(deCompResultMap, path, deCompResultFileMap.get(path));
			}
			
			deCompResultMap.putAll(deCompResultFileMap);
			
			deCompResultFileMap.clear();
			
			paths = null;
			
			// 결과 file path에 대해서 4가지 허용 패턴으로 검사한다.
//			Map<String, Integer> checkResultMap = new HashMap<>();
//			List<String> pathCheckList1 = new ArrayList<>();
//			List<String> pathCheckList2 = new ArrayList<>();
//			List<String> pathCheckList3 = new ArrayList<>();
//			List<String> pathCheckList4 = new ArrayList<>();
//			
//			List<String> pathCheckList11 = new ArrayList<>();
//			List<String> pathCheckList21 = new ArrayList<>();
//			List<String> pathCheckList31 = new ArrayList<>();
//			List<String> pathCheckList41 = new ArrayList<>();
//			
//			List<String> pathCheckList12 = new ArrayList<>();
//			List<String> pathCheckList22 = new ArrayList<>();
//			List<String> pathCheckList32 = new ArrayList<>();
//			List<String> pathCheckList42 = new ArrayList<>();
//			
//			List<String> pathCheckList13 = new ArrayList<>();
//			List<String> pathCheckList23 = new ArrayList<>();
//			List<String> pathCheckList33 = new ArrayList<>();
//			List<String> pathCheckList43 = new ArrayList<>();
//			
//			List<String> pathCheckList14 = new ArrayList<>();
//			List<String> pathCheckList24 = new ArrayList<>();
//			List<String> pathCheckList34 = new ArrayList<>();
//			List<String> pathCheckList44 = new ArrayList<>();
//			
//			List<String> pathCheckList15 = new ArrayList<>();
//			List<String> pathCheckList25 = new ArrayList<>();
//			List<String> pathCheckList35 = new ArrayList<>();
//			List<String> pathCheckList45 = new ArrayList<>();
//
//			List<String> pathCheckList16 = new ArrayList<>();
//			List<String> pathCheckList26 = new ArrayList<>();
//			List<String> pathCheckList36 = new ArrayList<>();
//			List<String> pathCheckList46 = new ArrayList<>();
			
//			for (String path : deCompResultMap.keySet()) {
//				pathCheckList1.add(path);
//				pathCheckList2.add("/" + path);
//				pathCheckList3.add(path + "/");
//				pathCheckList4.add("/"+path + "/");

//				String replaceFilePath = path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
//				
//				if (replaceFilePath.startsWith("/")) {
//					replaceFilePath = replaceFilePath.substring(1);
//				}
//				
//				if (replaceFilePath.endsWith("/")) {
//					replaceFilePath = replaceFilePath.substring(0, replaceFilePath.length()-1);
//				}
//				
//				pathCheckList11.add(replaceFilePath);
//				pathCheckList21.add("/" + replaceFilePath);
//				pathCheckList31.add(replaceFilePath + "/");
//				pathCheckList41.add("/"+replaceFilePath + "/");
//				
//				String addRootDir = decompressionDirName + "/" + path;
//				
//				if (addRootDir.startsWith("/")) {
//					addRootDir = addRootDir.substring(1);
//				}
//				
//				if (addRootDir.endsWith("/")) {
//					addRootDir = addRootDir.substring(0, addRootDir.length()-1);
//				}
//				
//				pathCheckList12.add(addRootDir);
//				pathCheckList22.add("/" + addRootDir);
//				pathCheckList32.add(addRootDir + "/");
//				pathCheckList42.add("/"+addRootDir + "/");
//				
//				String addRootDirReplaceFilePath = decompressionDirName + "/" + path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
//				
//				if (addRootDirReplaceFilePath.startsWith("/")) {
//					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(1);
//				}
//				
//				if (addRootDirReplaceFilePath.endsWith("/")) {
//					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(0, addRootDirReplaceFilePath.length());
//				}
//				
//				pathCheckList13.add(addRootDirReplaceFilePath);
//				pathCheckList23.add("/" + addRootDirReplaceFilePath);
//				pathCheckList33.add(addRootDirReplaceFilePath + "/");
//				pathCheckList43.add("/"+addRootDirReplaceFilePath + "/");
//
//				String replaceRootDir = path.replaceFirst(packageFileName, "").replaceAll("//", "/");
//				if (replaceRootDir.startsWith("/")) {
//					replaceRootDir = replaceRootDir.substring(1);
//				}
//				
//				if (replaceRootDir.endsWith("/")) {
//					replaceRootDir = replaceRootDir.substring(0, replaceRootDir.length()-1);
//				}
//				
//				pathCheckList14.add(replaceRootDir);
//				pathCheckList24.add("/" + replaceRootDir);
//				pathCheckList34.add(replaceRootDir + "/");
//				pathCheckList44.add("/"+replaceRootDir + "/");
//				
//				String replaceRootDirReplaceFilePath = replaceRootDir;
//				
//				if (replaceRootDirReplaceFilePath.endsWith("*")) {
//					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
//				}
//				
//				if (replaceRootDirReplaceFilePath.endsWith("/")) {
//					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
//				}
//				
//				pathCheckList15.add(replaceRootDirReplaceFilePath);
//				pathCheckList25.add("/" + replaceRootDirReplaceFilePath);
//				pathCheckList35.add(replaceRootDirReplaceFilePath + "/");
//				pathCheckList45.add("/"+replaceRootDirReplaceFilePath + "/");
//				
//				String replaceDecomFileRootDir = path.replaceFirst(decompressionRootPath, "").replaceAll("//", "/");
//				
//				if (replaceDecomFileRootDir.startsWith("/")) {
//					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(1);
//				}
//				
//				if (replaceDecomFileRootDir.endsWith("/")) {
//					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(0, replaceDecomFileRootDir.length()-1);
//				}
//				
//				pathCheckList16.add(replaceDecomFileRootDir);
//				pathCheckList26.add("/" + replaceDecomFileRootDir);
//				pathCheckList36.add(replaceDecomFileRootDir + "/");
//				pathCheckList46.add("/"+replaceDecomFileRootDir + "/");
//			}
			
			// 통합 Map 에 모든 허용 패턴을 저장
			Map<String, Integer> checkResultMap = new HashMap<>();
			
			for (String s : deCompResultMap.keySet()) {
				String path = s;
				String path2 = "/" + path;
				String path3 = path + "/";
				String path4 = "/"+ path + "/";
				
				String replaceFilePath = path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
				
				if (replaceFilePath.startsWith("/")) {
					replaceFilePath = replaceFilePath.substring(1);
				}
				
				if (replaceFilePath.endsWith("/")) {
					replaceFilePath = replaceFilePath.substring(0, replaceFilePath.length()-1);
				}
				
				String path5 = replaceFilePath;
				String path6 = "/" + replaceFilePath;
				String path7 = replaceFilePath + "/";
				String path8 = "/"+ replaceFilePath + "/";
				
				String addRootDir = decompressionDirName + "/" + path;
				
				if (addRootDir.startsWith("/")) {
					addRootDir = addRootDir.substring(1);
				}
				
				if (addRootDir.endsWith("/")) {
					addRootDir = addRootDir.substring(0, addRootDir.length()-1);
				}
				
				String path9 = addRootDir;
				String path10 = "/" + addRootDir;
				String path11 = addRootDir + "/";
				String path12 = "/" + addRootDir + "/";
				
				String addRootDirReplaceFilePath = decompressionDirName + "/" + path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
				
				if (addRootDirReplaceFilePath.startsWith("/")) {
					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(1);
				}
				
				if (addRootDirReplaceFilePath.endsWith("/")) {
					addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(0, addRootDirReplaceFilePath.length());
				}
				
				String path13 = addRootDirReplaceFilePath;
				String path14 = "/" + addRootDirReplaceFilePath;
				String path15 = addRootDirReplaceFilePath + "/";
				String path16 = "/" + addRootDirReplaceFilePath + "/";

				String replaceRootDir = path.replaceFirst(packageFileName, "").replaceAll("//", "/");
				if (replaceRootDir.startsWith("/")) {
					replaceRootDir = replaceRootDir.substring(1);
				}
				
				if (replaceRootDir.endsWith("/")) {
					replaceRootDir = replaceRootDir.substring(0, replaceRootDir.length()-1);
				}
				
				String path17 = replaceRootDir;
				String path18 = "/" + replaceRootDir;
				String path19 = replaceRootDir + "/";
				String path20 = "/"+replaceRootDir + "/";
				
				String replaceRootDirReplaceFilePath = replaceRootDir;
				
				if (replaceRootDirReplaceFilePath.endsWith("*")) {
					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
				}
				
				if (replaceRootDirReplaceFilePath.endsWith("/")) {
					replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
				}
				
				String path21 = replaceRootDirReplaceFilePath;
				String path22 = "/" + replaceRootDirReplaceFilePath;
				String path23 = replaceRootDirReplaceFilePath + "/";
				String path24 = "/"+replaceRootDirReplaceFilePath + "/";
				
				String replaceDecomFileRootDir = path.replaceFirst(decompressionRootPath, "").replaceAll("//", "/");
				
				if (replaceDecomFileRootDir.startsWith("/")) {
					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(1);
				}
				
				if (replaceDecomFileRootDir.endsWith("/")) {
					replaceDecomFileRootDir = replaceDecomFileRootDir.substring(0, replaceDecomFileRootDir.length()-1);
				}
				
				String path25 = replaceDecomFileRootDir;
				String path26 = "/" + replaceDecomFileRootDir;
				String path27 = replaceDecomFileRootDir + "/";
			    String path28 = "/"+replaceDecomFileRootDir + "/";
				
				checkResultMap.put(s, deCompResultMap.containsKey(s) ? deCompResultMap.get(s) : 0);
				checkResultMap.put(path2, deCompResultMap.containsKey(path2) ? deCompResultMap.get(path2) : 0);
				checkResultMap.put(path3, deCompResultMap.containsKey(path3) ? deCompResultMap.get(path3) : 0);
				checkResultMap.put(path4, deCompResultMap.containsKey(path4) ? deCompResultMap.get(path4) : 0);

				checkResultMap.put(path5, deCompResultMap.containsKey(path5) ? deCompResultMap.get(path5) : 0);
				checkResultMap.put(path6, deCompResultMap.containsKey(path6) ? deCompResultMap.get(path6) : 0);
				checkResultMap.put(path7, deCompResultMap.containsKey(path7) ? deCompResultMap.get(path7) : 0);
				checkResultMap.put(path8, deCompResultMap.containsKey(path8) ? deCompResultMap.get(path8) : 0);

				checkResultMap.put(path9, deCompResultMap.containsKey(path9) ? deCompResultMap.get(path9) : 0);
				checkResultMap.put(path10, deCompResultMap.containsKey(path10) ? deCompResultMap.get(path10) : 0);
				checkResultMap.put(path11, deCompResultMap.containsKey(path11) ? deCompResultMap.get(path11) : 0);
				checkResultMap.put(path12, deCompResultMap.containsKey(path12) ? deCompResultMap.get(path12) : 0);

				checkResultMap.put(path13, deCompResultMap.containsKey(path13) ? deCompResultMap.get(path13) : 0);
				checkResultMap.put(path14, deCompResultMap.containsKey(path14) ? deCompResultMap.get(path14) : 0);
				checkResultMap.put(path15, deCompResultMap.containsKey(path15) ? deCompResultMap.get(path15) : 0);
				checkResultMap.put(path16, deCompResultMap.containsKey(path16) ? deCompResultMap.get(path16) : 0);

				checkResultMap.put(path17, deCompResultMap.containsKey(path17) ? deCompResultMap.get(path17) : 0);
				checkResultMap.put(path18, deCompResultMap.containsKey(path18) ? deCompResultMap.get(path18) : 0);
				checkResultMap.put(path19, deCompResultMap.containsKey(path19) ? deCompResultMap.get(path19) : 0);
				checkResultMap.put(path20, deCompResultMap.containsKey(path20) ? deCompResultMap.get(path20) : 0);

				checkResultMap.put(path21, deCompResultMap.containsKey(path21) ? deCompResultMap.get(path21) : 0);
				checkResultMap.put(path22, deCompResultMap.containsKey(path22) ? deCompResultMap.get(path22) : 0);
				checkResultMap.put(path23, deCompResultMap.containsKey(path23) ? deCompResultMap.get(path23) : 0);
				checkResultMap.put(path24, deCompResultMap.containsKey(path24) ? deCompResultMap.get(path24) : 0);

				String _tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(path25), path25);
				checkResultMap.put(path25, deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
				_tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(path26), path26);
				checkResultMap.put(path26, deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
				_tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(path27), path27);
				checkResultMap.put(path27, deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
				_tmp = addDecompressionRootPath(decompressionRootPath, deCompResultMap.containsKey(path28), path28);
				checkResultMap.put(path28, deCompResultMap.containsKey(_tmp) ? deCompResultMap.get(_tmp) : 0);
			}

			deCompResultMap.clear();
			
			int gridIdx = 0;
			ArrayList<String> gValidIdxlist = new ArrayList<>();
			ArrayList<String> checkSourcePathlist = new ArrayList<>();
			ArrayList<String> emptySourcePathlist = new ArrayList<>();
			HashMap<String,Object> gFileCountMap = new HashMap<>();
			boolean separatorErrFlag = false;
			
			log.info("VERIFY Path Check START -----------------");
			
			for (String gridPath : gridFilePaths){
				if (isEmpty(gridPath)) {
					emptySourcePathlist.add(gridComponentIds.get(gridIdx));
					continue;
				}
				
				if (gridPath.contains("?")) {
					gridPath = gridPath.replaceAll("[?]", "0x3F");
				}
				
				if (!separatorErrFlag) {
					separatorErrFlag = gridPath.contains("\\") ? true : false;
				}
				
				if (gridPath.equals("/")) {
					checkSourcePathlist.add(gridComponentIds.get(gridIdx));
				} else {
					//사용자가 * 입력했을때
					if (!gridPath.trim().equals("/*") && !gridPath.trim().equals("/")){
						if (gridPath.endsWith("*")) {
							gridPath = gridPath.substring(0, gridPath.length()-1);
						}
						if (gridPath.startsWith(".")) {
							gridPath = gridPath.substring(1, gridPath.length());
						}
						// 앞뒤 path구분 제거
						if (gridPath.endsWith("/")) {
							gridPath = gridPath.substring(0, gridPath.length()-1);
						}
						if (gridPath.startsWith("/")) {
							gridPath = gridPath.substring(1);
						}
						
						int gFileCount = 0;
						
						/*
						 * SUB_STEP 1. verify 결과 배열을 받아온 grid filepath와 비교하여 실제로 그 path가 존재하는지 확인후 
						 * 존재하지 않을 경우 grid index 저장 
						 */
						boolean resultFlag = false;
						if (checkResultMap.containsKey(gridPath)) {
							resultFlag = true;
							gFileCount = checkResultMap.get(gridPath);
						}
						
						if (!resultFlag) {//path가 존재하지않을 때
							gValidIdxlist.add(gridComponentIds.get(gridIdx));
						} else {//path가 존재할 때
							// file을 직접 비교하는 경우 count되지 않기 때문에, 1로 고정
							// resultFlag == true 인경우는 존재하기 해당 path or file 대상이 존재한다는 의미이기 때문에 0이 될 수 없다.
							if (gFileCount == 0) {
								gFileCount = 1;
							}
							gFileCountMap.put(gridComponentIds.get(gridIdx), Integer.toString(gFileCount));
						}
					} else {
						gFileCountMap.put(gridComponentIds.get(gridIdx), Integer.toString(allFileCount));
					}
				}
				
				gridIdx++;
			}
			
			checkResultMap.clear();
			
			log.info("VERIFY Path Check END -----------------");
			
			//STEP 4 : README 파일 존재 유무 확인(README 여러개 일경우도 생각해야함 ---차후)
			
			// depth가 낮은 readme 파일을 구하기 위해 sort
			if (packagingFileIdx == 1){ // packageFile에서 readMe File은 첫번째 file에서만 찾음.
//				List<String> sortList = new ArrayList<>(deCompResultMap.keySet());
				Collections.sort(readmePathList, new Comparator<String>() {

					@Override
					public int compare(String arg1, String arg2) {
						if (arg1.split("\\/").length > arg2.split("\\/").length) {
							return 1;
						} else if (arg1.split("\\/").length < arg2.split("\\/").length) {
							return -1;
						} else {
							return arg1.compareTo(arg2);
						}
					};
				});
				
//				String lastReadmeFilePath = "";
				for (String r : readmePathList) {
					String _upperPath = avoidNull(r).toUpperCase();
					
					if (_upperPath.endsWith("/")) {
						continue;
					}
					
//					String _currentReadmeFilePath = _upperPath.indexOf("/") < 0 ? _upperPath : _upperPath.substring(0,_upperPath.lastIndexOf("/"));
//					
//					if (!lastReadmeFilePath.equals(_currentReadmeFilePath)) {
//						if (!isEmpty(readmePath)) {
//							break;
//						}
//						
//						lastReadmeFilePath = _currentReadmeFilePath;
//					}
					
					if (_upperPath.indexOf("/") > -1) {
						_upperPath = _upperPath.substring(_upperPath.lastIndexOf("/"), _upperPath.length());
					}
					
					if (_upperPath.indexOf("README") > -1){
						String _readmePath = r.replaceAll("\\n", "");
						
						int afterDepthCnt = StringUtils.countMatches(_readmePath, "/");
						int beforeDepthCnt = StringUtils.countMatches(readmePath, "/");
						if (isEmpty(readmePath) || beforeDepthCnt > afterDepthCnt) {
							readmePath = _readmePath;
						}
					}
				}
			}

			
			String readmeFileName = "";
			//STEP 6 : README 파일 내용 출력
			if (!StringUtil.isEmpty(readmePath)){
				if (readmePath.indexOf("*") > -1){
					readmePath = readmePath.substring(0, readmePath.length()-1);
				}
				
				readmeFileName = readmePath;
				
				if (readmeFileName.indexOf("/") > -1) {
					readmeFileName = readmeFileName.substring(readmeFileName.lastIndexOf("/") + 1);
				}
				
				if (readmePath.indexOf(" ") > -1) {
					log.info("do space replase ok");
					
					readmePath = readmePath.replaceAll(" ", "*");
				}
				
				log.info("readmePath : " + readmePath);
				log.info("readmeFileName : " + readmeFileName);
				log.info("VERIFY Copy Readme file START -----------------");
				log.info("VERIFY README MV PATH : " + VERIFY_PATH_DECOMP +"/" + prjId +"/" + readmePath);
				
				if (isChangedPackageFile){
					ShellCommander.shellCommandWaitFor(new String[]{"/bin/bash", "-c", "cp "+VERIFY_PATH_DECOMP +"/" + prjId +"/" + readmePath+ " " + VERIFY_PATH_OUTPUT +"/" + prjId +"/"});
				}
				
				log.info("VERIFY Copy Readme file END -----------------");
			}
			
			//STEP 7 : README 파일 내용 DB 에 저장
			if (doUpdate && packagingFileIdx == 1) {
				log.debug("VERIFY readme 등록");
				
				project.setPrjId(prjId);
				project.setReadmeFileName(readmeFileName);
				project.setReadmeYn(StringUtil.isEmpty(readmeFileName) ? CoConstDef.FLAG_NO : CoConstDef.FLAG_YES);
			
				projectService.registReadmeContent(project);
				
				log.debug("VERIFY readme 등록 완료");
			}
			
			//STEP 8 : Verify 동작 후 Except File Result DB 저장
			log.info("VERIFY Read exceptFileContent file START -----------------");
			File targetFile = new File(VERIFY_PATH_OUTPUT +"/"+prjId+"/except_file_result_" + packagingFileIdx);
			if (targetFile.exists()) {
				if (packagingFileIdx == 1) {
					File copiedFile = new File(VERIFY_PATH_OUTPUT +"/"+prjId+"/except_file_result");
					Files.copy(targetFile.toPath(), copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					FileUtil.addFileContents(VERIFY_PATH_OUTPUT +"/"+prjId+"/except_file_result", VERIFY_PATH_OUTPUT +"/"+prjId+"/except_file_result_" + packagingFileIdx);
				}
			}
			
			exceptFileContent = CommonFunction.getStringFromFile(VERIFY_PATH_OUTPUT +"/"+prjId+"/except_file_result", VERIFY_PATH_DECOMP +"/" + prjId +"/", checkExceptionWordsList, checkExceptionIgnoreWorksList);
			
			log.info("VERIFY Read exceptFileContent file END -----------------");
			
			// 2017.03.23 yuns contents 용량이 너무 커서 DB로 관리하지 않음 (flag만 처리, empty여부로 체크하기 때문에 내용이 있을 경우 "Y" 만 등록
			project.setExceptFileContent(!isEmpty(exceptFileContent) ? CoConstDef.FLAG_YES : "");
			project.setVerifyFileContent(!isEmpty(verify_chk_list) ? CoConstDef.FLAG_YES : "");
			
			if (doUpdate) {
				projectService.registVerifyContents(project);
			}
			
			log.debug("VERIFY 파일내용 등록 완료");
			
			// 서버 디렉토리를 replace한 내용으로 새로운 파일로 다시 쓴다.
			if (!isEmpty(exceptFileContent)) {
				log.info("VERIFY writeFile exceptFileContent file START -----------------");
				FileUtil.writeFile(VERIFY_PATH_OUTPUT +"/" + prjId, CoConstDef.PACKAGING_VERIFY_FILENAME_PROPRIETARY, exceptFileContent.replaceAll(VERIFY_PATH_DECOMP +"/" + prjId +"/", ""));
				
				log.info("VERIFY writeFile exceptFileContent file END -----------------");
			}
			
			if (!isEmpty(verify_chk_list)) {
				log.info("VERIFY writeFile verify_chk_list file START -----------------");
				
				FileUtil.writeFile(VERIFY_PATH_OUTPUT +"/" + prjId, CoConstDef.PACKAGING_VERIFY_FILENAME_FILE_LIST, verify_chk_list.replaceAll(VERIFY_PATH_DECOMP +"/" + prjId +"/", ""));
				
				log.info("VERIFY writeFile verify_chk_list file END -----------------");
			}

			log.info("VERIFY Read fosslight_binary result file START -----------------");
			String binaryFile = VERIFY_PATH_OUTPUT +"/" + prjId + "/binary_" + packagingFileIdx;
			File f = new File(binaryFile);
			if (f.exists()) {
				boolean isExistFile = false;
				for (File bFile : f.listFiles()){
					if (bFile.exists() &&f.listFiles().length == 1) {
						isExistFile = true;
						if (packagingFileIdx == 1) {
							File copiedFile = new File(VERIFY_PATH_OUTPUT + "/" + prjId + "/verify_binary_list");
							Files.copy(bFile.toPath(), copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} else {
							FileUtil.addFileContents(VERIFY_PATH_OUTPUT +"/"+prjId+"/verify_binary_list", bFile.getPath());
						}
					}
				}

				if (isExistFile) {
					String verify_binary_list = CommonFunction.getStringFromFile(VERIFY_PATH_OUTPUT +"/"+prjId+"/verify_binary_list").replaceAll(VERIFY_PATH_DECOMP +"/"+ prjId + "/", "");
					FileUtil.writeFile(VERIFY_PATH_OUTPUT +"/" + prjId, CoConstDef.PACKAGING_VERIFY_FILENAME_BINARY_LIST, verify_binary_list);
					project.setBinaryFileYn(CoConstDef.FLAG_YES);
				} else {
					project.setBinaryFileYn("");
				}
			} else {
				project.setBinaryFileYn("");
			}

			if (doUpdate) {
				projectService.registVerifyContents(project);
			}

			log.info("VERIFY Read fosslight_binary result file END -----------------");
			
			resCd="10";
			if (separatorErrFlag) {
				resMsg = getMessage("verify.path.error");
			} else {
				resMsg= getMessage(gValidIdxlist.isEmpty() ? "msg.common.success" : "msg.common.valid");
			}
			
			resMap.put("verifyValid", gValidIdxlist);
			resMap.put("verifyCheckSourcePath", checkSourcePathlist);
			resMap.put("verifyEmptySourcePath", emptySourcePathlist);
			resMap.put("verifyValidMsg", "path not found.");
			resMap.put("verifyCheckSourcePathMsg", getMessage("msg.package.check.source.code.path"));
			resMap.put("verifyEmptySourcePathMsg", "Required");
			resMap.put("fileCounts", gFileCountMap);
			resMap.put("verifyReadme", readmeFileName);
			resMap.put("verifyCheckList", !isEmpty(verify_chk_list) ? CoConstDef.FLAG_YES : "");
			resMap.put("verifyProprietary", !isEmpty(exceptFileContent) ? CoConstDef.FLAG_YES : "");
			resMap.put("verifyBinary", project.getBinaryFileYn().equals(CoConstDef.FLAG_YES) ? CoConstDef.FLAG_YES : "");
			
			//path not found.가 1건이라도 있으면 status_verify_yn의 flag는 N으로 저장함.
			// packagingFileId, filePath는 1번만 저장하며, gValidIdxlist의 값때문에 마지막 fileSeq일때 저장함.
			if (doUpdate && packagingFileIdx == fileSeqs.size()) {
				// verify 버튼 클릭시 file path를 저장한다.
				if (gridComponentIds != null && !gridComponentIds.isEmpty()) {
					int seq = 0;
					
					for (String s : gridComponentIds){
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
					prjParam.setPackageFileType1("1");
					prjParam.setPackageFileId2(fileSeqs.size() >= 2 ? fileSeqs.get(1) : null);
					prjParam.setPackageFileType2(fileSeqs.size() >= 2 ? "1" : null);
					prjParam.setPackageFileId3(fileSeqs.size() >= 3 ? fileSeqs.get(2) : null);
					prjParam.setPackageFileType3(fileSeqs.size() >= 3 ? "1" : null);
					prjParam.setPackageFileId4(fileSeqs.size() >= 4 ? fileSeqs.get(3) : null);
					prjParam.setPackageFileType4(fileSeqs.size() >= 4 ? "1" : null);
					prjParam.setPackageFileId5(fileSeqs.size() >= 5 ? fileSeqs.get(4) : null);
					prjParam.setPackageFileType5(fileSeqs.size() >= 5 ? "1" : null);

					if (!isEmpty(prjInfo.getDistributionStatus())){
						prjParam.setStatusVerifyYn("C");
					} else {
						prjParam.setStatusVerifyYn(CoConstDef.FLAG_YES);
					}
					
					verificationMapper.updatePackageFile(prjParam);
				}
				
				
				CommentsHistory commHisBean = new CommentsHistory();
				commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
				commHisBean.setReferenceId(prjId); commHisBean.setContents(packagingComment);
				  
				commentService.registComment(commHisBean, false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			resCd="20";
			resMsg="process failed. (server error)";
		} finally {
			try {
				if (isChangedPackageFile){
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
	
	private int checkGridPath(String gridPath, Iterator<String> deCompResultMapKeys, Map<String, Integer> deCompResultMap, String decompressionDirName, String packageFileName, String decompressionRootPath, String firstPathName) {
		List<String> checkPathList = new ArrayList<>();
		String matchPath = "";
		int fileCount = 0;
		
		while (deCompResultMapKeys.hasNext()) {
			boolean matchFlag = false;
			
			String path = deCompResultMapKeys.next();
			checkPathList.add(path);
			checkPathList.add("/" + path);
			checkPathList.add(path + "/");
			checkPathList.add("/"+ path + "/");
			
			String replaceFilePath = path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
			
			if (replaceFilePath.startsWith("/")) {
				replaceFilePath = replaceFilePath.substring(1);
			}
			
			if (replaceFilePath.endsWith("/")) {
				replaceFilePath = replaceFilePath.substring(0, replaceFilePath.length()-1);
			}
			
			checkPathList.add(replaceFilePath);
			checkPathList.add("/" + replaceFilePath);
			checkPathList.add(replaceFilePath + "/");
			checkPathList.add("/"+ replaceFilePath + "/");
			
			String addRootDir = decompressionDirName + "/" + path;
			
			if (addRootDir.startsWith("/")) {
				addRootDir = addRootDir.substring(1);
			}
			
			if (addRootDir.endsWith("/")) {
				addRootDir = addRootDir.substring(0, addRootDir.length()-1);
			}
			
			checkPathList.add(addRootDir);
			checkPathList.add("/" + addRootDir);
			checkPathList.add(addRootDir + "/");
			checkPathList.add("/"+ addRootDir + "/");
			
			String addRootDirReplaceFilePath = decompressionDirName + "/" + path.substring(0, path.endsWith("*") ? path.length()-1 : path.length());
			
			if (addRootDirReplaceFilePath.startsWith("/")) {
				addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(1);
			}
			
			if (addRootDirReplaceFilePath.endsWith("/")) {
				addRootDirReplaceFilePath = addRootDirReplaceFilePath.substring(0, addRootDirReplaceFilePath.length());
			}
			
			checkPathList.add(addRootDirReplaceFilePath);
			checkPathList.add("/" + addRootDirReplaceFilePath);
			checkPathList.add(addRootDirReplaceFilePath + "/");
			checkPathList.add("/"+ addRootDirReplaceFilePath + "/");

			String firstPathDir = path.replaceFirst(firstPathName, "").replaceAll("//", "/");
			if (firstPathDir.startsWith("/")) {
				firstPathDir = firstPathDir.substring(1);
			}
			
			if (firstPathDir.endsWith("/")) {
				firstPathDir = firstPathDir.substring(0, firstPathDir.length()-1);
			}
			
			checkPathList.add(firstPathDir);
			checkPathList.add("/" + firstPathDir);
			checkPathList.add(firstPathDir + "/");
			checkPathList.add("/"+ firstPathDir + "/");
			
			String replaceRootDir = path.replaceFirst(packageFileName, "").replaceAll("//", "/");
			if (replaceRootDir.startsWith("/")) {
				replaceRootDir = replaceRootDir.substring(1);
			}
			
			if (replaceRootDir.endsWith("/")) {
				replaceRootDir = replaceRootDir.substring(0, replaceRootDir.length()-1);
			}
			
			checkPathList.add(replaceRootDir);
			checkPathList.add("/" + replaceRootDir);
			checkPathList.add(replaceRootDir + "/");
			checkPathList.add("/"+ replaceRootDir + "/");
			
			String replaceRootDirReplaceFilePath = replaceRootDir;
			
			if (replaceRootDirReplaceFilePath.endsWith("*")) {
				replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
			}
			
			if (replaceRootDirReplaceFilePath.endsWith("/")) {
				replaceRootDirReplaceFilePath = replaceRootDirReplaceFilePath.substring(0, replaceRootDirReplaceFilePath.length()-1);
			}
			
			checkPathList.add(replaceRootDirReplaceFilePath);
			checkPathList.add("/" + replaceRootDirReplaceFilePath);
			checkPathList.add(replaceRootDirReplaceFilePath + "/");
			checkPathList.add("/"+ replaceRootDirReplaceFilePath + "/");
			
			String replaceDecomFileRootDir = path.replaceFirst(decompressionRootPath, "").replaceAll("//", "/");
			
			if (replaceDecomFileRootDir.startsWith("/")) {
				replaceDecomFileRootDir = replaceDecomFileRootDir.substring(1);
			}
			
			if (replaceDecomFileRootDir.endsWith("/")) {
				replaceDecomFileRootDir = replaceDecomFileRootDir.substring(0, replaceDecomFileRootDir.length()-1);
			}
			
			checkPathList.add(replaceDecomFileRootDir);
			checkPathList.add("/" + replaceDecomFileRootDir);
			checkPathList.add(replaceDecomFileRootDir + "/");
			checkPathList.add("/"+ replaceDecomFileRootDir + "/");
			
			checkPathList.add(decompressionRootPath + "/" + replaceDecomFileRootDir);
			checkPathList.add(decompressionRootPath + "/" + replaceDecomFileRootDir + "/");
			
			if (checkPathList != null && !checkPathList.isEmpty()) checkPathList = checkPathList.stream().distinct().collect(Collectors.toList());
			
			for (String checkPath : checkPathList) {
				if (checkPath.equalsIgnoreCase(gridPath)) {
					matchPath = checkPath;
					matchFlag = true;
					break;
				}
			}
			
			checkPathList.clear();
			if (matchFlag) break;
		}
		
		if (!isEmpty(matchPath) && deCompResultMap.containsKey(matchPath)) {
			fileCount = deCompResultMap.get(matchPath);
		}
		
		return fileCount;
	}

	private String addDecompressionRootPath(String path, boolean flag, String val) {
		return flag ? val : path + "/" + val;
	}

	@Override
	public void updateVerifyFileCount(Map<String,Object> fileCounts) {
		for (String componentId : fileCounts.keySet()){
			OssComponents param = new OssComponents();
			param.setComponentId(componentId);
			param.setVerifyFileCount((String) fileCounts.get(componentId));
			
			verificationMapper.updateVerifyFileCount(param);
		}
	}
	
	@Override
	public void updateVerifyFileCountReset(List<String> fileCounts) {
		for (String componentId : fileCounts){
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
		List<OssComponents> ossComponentList = verificationMapper.selectVerificationNotice(ossNotice);
		
		List<OssComponents> collateOssComponentList = null;
		if (!CollectionUtils.isEmpty(ossComponentList)) {
			for (OssComponents ossComponent : ossComponentList) {
				String restrictionStr = ossComponent.getRestriction();
				if (!isEmpty(restrictionStr)) {
					String[] restrictions = restrictionStr.split(",");
					for (String restriction : restrictions) {
						if (!isEmpty(restriction) && CoConstDef.CD_LICENSE_NETWORK_RESTRICTION.equals(restriction.trim())) {
							if (collateOssComponentList == null) {
								collateOssComponentList = new ArrayList<>();
							}
							collateOssComponentList.add(ossComponent);
							break;
						}
					}
				}
			}
		}
		
		return CollectionUtils.isEmpty(collateOssComponentList);
	}
	
	@Transactional
	@CacheEvict(value="autocompleteProjectCache", allEntries=true)
	private void updateProjectStatus(Project project) {
		//다운로드 허용 플래그
		project.setAllowDownloadBitFlag(allowDownloadMultiFlagToBitFlag(project));
		
		// 프로젝트 상태 변경
		projectMapper.updateProjectMaster(project);
	}
	
	@Override
	@Transactional
	public void updateStatusWithConfirm(Project project, OssNotice ossNotice, boolean copyConfirmFlag) throws Exception {
		if (copyConfirmFlag) {
			projectMapper.updateConfirmCopyVerificationDistributionStatus(project);
		} else {
			updateProjectStatus(project);
		}
		
		boolean makeZipFile = false;
		String spdxComment = "";
		
		// html simple
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleHTMLYn())) {
			ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES);
			ossNotice.setFileType("html");
			project.setSimpleHtmlFileId(getNoticeTextFileForPreview(ossNotice, true));
			makeZipFile = true;
		}

		// text
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadNoticeTextYn())) {
			ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_NO);
			ossNotice.setFileType("text");
			project.setNoticeTextFileId(getNoticeTextFileForPreview(ossNotice, true));
			makeZipFile = true;
		}
		
		// text simple
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleTextYn())) {
			ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES);
			ossNotice.setFileType("text");
			project.setSimpleTextFileId(getNoticeTextFileForPreview(ossNotice, true));
			makeZipFile = true;
		}
		
		// SPDX
		String spdxSheetFileId = null;
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXSheetYn())) {
			Map<String, String> data = new HashMap<>(); data.put("prjId", project.getPrjId());
			String dataStr = toJson(data);
			spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, EXPORT_TEMPLATE_PATH);
			if (!isEmpty(spdxSheetFileId)) {
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
		
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXRdfYn())) {
			if (isEmpty(spdxSheetFileId)) {
				Map<String, String> data = new HashMap<>(); data.put("prjId", project.getPrjId());
				String dataStr = toJson(data);
				spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, EXPORT_TEMPLATE_PATH);
			}
			
			if (!isEmpty(spdxSheetFileId)) {
				T2File spdxFileInfo = fileService.selectFileInfo(spdxSheetFileId);
				String sheetFullPath = spdxFileInfo.getLogiPath();
				
				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}
				
				sheetFullPath += spdxFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(spdxFileInfo.getLogiNm())+".rdf";
				String resultFileName = FilenameUtils.getBaseName(spdxFileInfo.getOrigNm())+".rdf";
				String tagFullPath = spdxFileInfo.getLogiPath();
				
				if (!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}
				
				tagFullPath += targetFileName;
				SPDXUtil2.convert(project.getPrjId(), sheetFullPath, tagFullPath);
				File spdxRdfFile = new File(tagFullPath);
				
				if (spdxRdfFile.exists() && spdxRdfFile.length() <= 0) {
					if (!isEmpty(spdxComment)) {
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
		
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXTagYn())) {
			if (isEmpty(spdxSheetFileId)) {
				Map<String, String> data = new HashMap<>(); data.put("prjId", project.getPrjId());
				String dataStr = toJson(data);
				spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, EXPORT_TEMPLATE_PATH);
			}
			
			if (!isEmpty(spdxSheetFileId)) {
				T2File spdxFileInfo = fileService.selectFileInfo(spdxSheetFileId);
				
				String sheetFullPath = spdxFileInfo.getLogiPath();
				
				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}
				
				sheetFullPath += spdxFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(spdxFileInfo.getLogiNm())+".tag";
				String resultFileName = FilenameUtils.getBaseName(spdxFileInfo.getOrigNm())+".tag";
				String tagFullPath = spdxFileInfo.getLogiPath();
				
				if (!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}
				
				tagFullPath += targetFileName;
				SPDXUtil2.convert(project.getPrjId(), sheetFullPath, tagFullPath);
				
				File spdxTafFile = new File(tagFullPath);
				
				if (spdxTafFile.exists() && spdxTafFile.length() <= 0) {
					if (!isEmpty(spdxComment)) {
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

		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXJsonYn())) {
			if (isEmpty(spdxSheetFileId)) {
				Map<String, String> data = new HashMap<>(); data.put("prjId", project.getPrjId());
				String dataStr = toJson(data);
				spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, EXPORT_TEMPLATE_PATH);
			}

			if (!isEmpty(spdxSheetFileId)) {
				T2File spdxFileInfo = fileService.selectFileInfo(spdxSheetFileId);
				String sheetFullPath = spdxFileInfo.getLogiPath();

				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}

				sheetFullPath += spdxFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(spdxFileInfo.getLogiNm())+".json";
				String resultFileName = FilenameUtils.getBaseName(spdxFileInfo.getOrigNm())+".json";
				String tagFullPath = spdxFileInfo.getLogiPath();

				if (!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}

				tagFullPath += targetFileName;
				SPDXUtil2.convert(project.getPrjId(), sheetFullPath, tagFullPath);
				File spdxJsonFile = new File(tagFullPath);

				if (spdxJsonFile.exists() && spdxJsonFile.length() <= 0) {
					if (!isEmpty(spdxComment)) {
						spdxComment += "<br>";
					}

					spdxComment += getMessage("spdx.json.failure");
				}

				String filePath = NOTICE_PATH + "/" + project.getPrjId();
				FileUtil.moveTo(tagFullPath, filePath, resultFileName);
				project.setSpdxJsonFileId(fileService.registFileDownload(filePath, resultFileName, resultFileName));

				makeZipFile = true;
			}
		}

		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXYamlYn())) {
			if (isEmpty(spdxSheetFileId)) {
				Map<String, String> data = new HashMap<>(); data.put("prjId", project.getPrjId());
				String dataStr = toJson(data);
				spdxSheetFileId = ExcelDownLoadUtil.getExcelDownloadId("spdx", dataStr, EXPORT_TEMPLATE_PATH);
			}

			if (!isEmpty(spdxSheetFileId)) {
				T2File spdxFileInfo = fileService.selectFileInfo(spdxSheetFileId);
				String sheetFullPath = spdxFileInfo.getLogiPath();

				if (!sheetFullPath.endsWith("/")) {
					sheetFullPath += "/";
				}

				sheetFullPath += spdxFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(spdxFileInfo.getLogiNm())+".yaml";
				String resultFileName = FilenameUtils.getBaseName(spdxFileInfo.getOrigNm())+".yaml";
				String tagFullPath = spdxFileInfo.getLogiPath();

				if (!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}

				tagFullPath += targetFileName;
				SPDXUtil2.convert(project.getPrjId(), sheetFullPath, tagFullPath);
				File spdxYamlFile = new File(tagFullPath);

				if (spdxYamlFile.exists() && spdxYamlFile.length() <= 0) {
					if (!isEmpty(spdxComment)) {
						spdxComment += "<br>";
					}

					spdxComment += getMessage("spdx.yaml.failure");
				}

				String filePath = NOTICE_PATH + "/" + project.getPrjId();
				FileUtil.moveTo(tagFullPath, filePath, resultFileName);
				project.setSpdxYamlFileId(fileService.registFileDownload(filePath, resultFileName, resultFileName));

				makeZipFile = true;
			}
		}
		
		// cycloneDX
		String cdxJsonFileId = null;
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadCDXJsonYn())) {
			if (isEmpty(cdxJsonFileId)) {
				cdxJsonFileId = ExcelDownLoadUtil.getExcelDownloadId("cycloneDXJson", project.getPrjId(), EXPORT_TEMPLATE_PATH, "verify");
			}
			
			if (!isEmpty(cdxJsonFileId)) {
				T2File jsonFileInfo = fileService.selectFileInfo(cdxJsonFileId);
				String jsonFullPath = jsonFileInfo.getLogiPath();
				
				if (!jsonFullPath.endsWith("/")) {
					jsonFullPath += "/";
				}
				
				jsonFullPath += jsonFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(jsonFileInfo.getLogiNm())+".json";
				String resultFileName = FilenameUtils.getBaseName(jsonFileInfo.getOrigNm())+".json";
				String tagFullPath = jsonFileInfo.getLogiPath();
				
				if (!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}
				
				tagFullPath += targetFileName;
				File cdxJsonFile = new File(tagFullPath);
				
				if (cdxJsonFile.exists() && cdxJsonFile.length() <= 0) {
					if (!isEmpty(spdxComment)) {
						spdxComment += "<br>";
					}
					
					spdxComment += getMessage("cyclonedx.json.failure"); 
				}
				
				String filePath = NOTICE_PATH + "/" + project.getPrjId();
				FileUtil.moveTo(tagFullPath, filePath, resultFileName);
				project.setCdxJsonFileId(fileService.registFileDownload(filePath, resultFileName, resultFileName));
				
				makeZipFile = true;
			}
		}
		
		String cdxXmlFileId = null;
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadCDXXmlYn())) {
			if (isEmpty(cdxXmlFileId)) {
				cdxXmlFileId = ExcelDownLoadUtil.getExcelDownloadId("cycloneDXXml", project.getPrjId(), EXPORT_TEMPLATE_PATH, "verify");
			}
			
			if (!isEmpty(cdxXmlFileId)) {
				T2File xmlFileInfo = fileService.selectFileInfo(cdxXmlFileId);
				String xmlFullPath = xmlFileInfo.getLogiPath();
				
				if (!xmlFullPath.endsWith("/")) {
					xmlFullPath += "/";
				}
				
				xmlFullPath += xmlFileInfo.getLogiNm();
				String targetFileName = FilenameUtils.getBaseName(xmlFileInfo.getLogiNm())+".xml";
				String resultFileName = FilenameUtils.getBaseName(xmlFileInfo.getOrigNm())+".xml";
				String tagFullPath = xmlFileInfo.getLogiPath();
				
				if (!tagFullPath.endsWith("/")) {
					tagFullPath += "/";
				}
				
				tagFullPath += targetFileName;
				File cdxXmlFile = new File(tagFullPath);
				
				if (cdxXmlFile.exists() && cdxXmlFile.length() <= 0) {
					if (!isEmpty(spdxComment)) {
						spdxComment += "<br>";
					}
					
					spdxComment += getMessage("cyclonedx.xml.failure"); 
				}
				
				String filePath = NOTICE_PATH + "/" + project.getPrjId();
				FileUtil.moveTo(tagFullPath, filePath, resultFileName);
				project.setCdxXmlFileId(fileService.registFileDownload(filePath, resultFileName, resultFileName));
				
				makeZipFile = true;
			}
		}
		
		// zip파일 생성
		if (makeZipFile) {
			String noticeRootDir = NOTICE_PATH;
			ossNotice.setFileType(".zip");
			Project prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
			String zipFileName = CommonFunction.getNoticeFileName(prjInfo.getPrjId(), prjInfo.getPrjName(), prjInfo.getPrjVersion(), CommonFunction.getCurrentDateTime("yyMMdd"), ".zip");
			FileUtil.zip(noticeRootDir + "/" + project.getPrjId(), noticeRootDir, zipFileName, "OSS Notice");
			
			String zipFileId = fileService.registFileDownload(noticeRootDir, zipFileName, zipFileName);
			project.setZipFileId(zipFileId);
		}
		
		verificationMapper.updateNoticeFileInfoEtc(project); // file info update
		
		if (!isEmpty(spdxComment)) { // spdx failure => comment regist
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
		Project prjBean = projectMapper.selectProjectMaster2(prjId);
		List<String> packageFileIds = new ArrayList<String>();
		
		if (!isEmpty(prjBean.getPackageFileId())) {
			packageFileIds.add(prjBean.getPackageFileId());
		}
		
		if (!isEmpty(prjBean.getPackageFileId2())) {
			packageFileIds.add(prjBean.getPackageFileId2());
		}
		
		if (!isEmpty(prjBean.getPackageFileId3())) {
			packageFileIds.add(prjBean.getPackageFileId3());
		}
		
		int fileSeq = 1;
		
		for (String packageFileId : packageFileIds){
			T2File packageFileInfo = new T2File();
			packageFileInfo.setFileSeq(packageFileId);
			packageFileInfo = fileMapper.getFileInfo(packageFileInfo);
			
			if (packageFileInfo != null) {
				// Packaging > Confirm시 Packaging 파일명 변경 건
				String paramSeq = (packageFileIds.size() > 1 ? Integer.toString(fileSeq++) : ""); 
				String chgFileName = getPackageFileName(prjBean.getPrjName(), prjBean.getPrjVersion(), packageFileInfo.getOrigNm(), paramSeq);
				
				packageFileInfo.setOrigNm(chgFileName);
				
				fileMapper.upateOrgFileName(packageFileInfo);
			}
		}
	}

	@Override
	public String changePackageFileNameCombine(String prjId) {

		String contents = "";
		// 프로젝트 기본정보 취득
		Project prjBean = projectMapper.selectProjectMaster2(prjId);
		List<String> packageFileIds = new ArrayList<String>();

		if (!isEmpty(prjBean.getPackageFileId())) {
			packageFileIds.add(prjBean.getPackageFileId());
		}

		if (!isEmpty(prjBean.getPackageFileId2())) {
			packageFileIds.add(prjBean.getPackageFileId2());
		}

		if (!isEmpty(prjBean.getPackageFileId3())) {
			packageFileIds.add(prjBean.getPackageFileId3());
		}
		
		if (!isEmpty(prjBean.getPackageFileId4())) {
			packageFileIds.add(prjBean.getPackageFileId4());
		}
		
		if (!isEmpty(prjBean.getPackageFileId5())) {
			packageFileIds.add(prjBean.getPackageFileId5());
		}

		int fileSeq = 1;

		for (String packageFileId : packageFileIds){
			T2File packageFileInfo = new T2File();
			packageFileInfo.setFileSeq(packageFileId);
			packageFileInfo = fileMapper.getFileInfo(packageFileInfo);

			if (packageFileInfo != null) {
				String orgFileName = packageFileInfo.getOrigNm();
				// Packaging > Confirm시 Packaging 파일명 변경 건
				String paramSeq = (packageFileIds.size() > 1 ? Integer.toString(fileSeq++) : "");
				String chgFileName = getPackageFileName(prjBean.getPrjName(), prjBean.getPrjVersion(), packageFileInfo.getOrigNm(), paramSeq);

				packageFileInfo.setOrigNm(chgFileName);

				fileMapper.upateOrgFileName(packageFileInfo);

				contents += "<p>Changed File Name (\""+orgFileName+"\") to \""+chgFileName+"\" </p> ";
			}
		}
		return contents;
	}
	
	private String getPackageFileName(String prjName, String prjVersion, String orgFileName, String fileSeq) {
		String fileName = prjName;
		
		if (!isEmpty(prjVersion)) {
			fileName += "_" + prjVersion;
		}
		
		if (!isEmpty(fileSeq)){
			fileName += "_" + fileSeq;
		}
		
		// file명에 사용할 수 없는 특수문자 체크
		if (!FileUtil.isValidFileName(fileName)) {
			fileName = FileUtil.makeValidFileName(fileName, "_");
		}
		
		String fileExt = FilenameUtils.getExtension(orgFileName);
		
		if (orgFileName.toLowerCase().endsWith(".tgz.gz")) {
			fileExt = "tgz.gz";
		} else if (orgFileName.toLowerCase().endsWith(".tar.bz2")) {
			fileExt = "tar.bz2";
		} else if (orgFileName.toLowerCase().endsWith(".tar.gz")) {
			fileExt = "tar.gz";
		}
		
		if (fileExt.startsWith(".")) {
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
			Project project = projectMapper.selectProjectMaster(ossNotice.getPrjId());
			
			Map<String, Object> result = projectMapper.getNoticeType(ossNotice.getPrjId());
			
			// android project는 notice를 사용하지 않음.
			if (!CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equalsIgnoreCase(avoidNull((String) result.get("noticeType")))) {
				if (CoConstDef.FLAG_YES.equals(ossNotice.getEditNoticeYn())){
					verificationMapper.insertOssNotice(ossNotice);
				}else if (CoConstDef.FLAG_NO.equals(ossNotice.getEditNoticeYn())){
					verificationMapper.updateOssNotice(ossNotice);
				}
			}
			
			projectMapper.updateWithoutVerifyYn(ossNotice);
			
			
			if (isEmpty(project.getVerificationStatus())){
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
		
		if (fileType == "text"){
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
			if (isConfirm && CoConstDef.FLAG_YES.equals(simpleFlag) && CoConstDef.FLAG_YES.equals(project.getUseCustomNoticeYn())) {
				// 이미 생성된 고지문구 파일의 내용을 가져온다.
				T2File defaultNoticeFileInfo = fileService.selectFileInfo(project.getNoticeFileId());
				
				if (defaultNoticeFileInfo != null) {
					File noticeFile = new File(defaultNoticeFileInfo.getLogiPath() + "/" + defaultNoticeFileInfo.getLogiNm());
					if (noticeFile.exists()) {
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

		if (FileUtil.writeFile(filePath, fileName, contents)) {
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
			
			if (records > 0){
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
			if (!isEmpty(project.getPrjId())){
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

		Project project = projectMapper.selectProjectMaster(prjId);
		
		oss.fosslight.domain.File noticeFile = null;
		
		if (!isEmpty(project.getZipFileId())) {
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
	public ResponseEntity<FileSystemResource> getReviewReport(String prjId,
			String rESOURCE_PUBLIC_DOWNLOAD_REVIEW_REPORT_FILE_PATH_PREFIX) throws IOException {
		String fileName = "";
		String filePath = rESOURCE_PUBLIC_DOWNLOAD_REVIEW_REPORT_FILE_PATH_PREFIX;

		Project project = projectMapper.selectProjectMaster(prjId);

		oss.fosslight.domain.File reviewReportFile = null;

		if (!isEmpty(project.getReviewReportFileId())) {
			reviewReportFile = verificationMapper.selectVerificationFile(project.getReviewReportFileId());
			fileName =  reviewReportFile.getOrigNm();
			filePath += File.separator+prjId+File.separator+fileName;
		}

		return reviewReportToResponseEntity(filePath, fileName);
	};

	@Override
	public Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice) {
		return getNoticeHtmlInfo(ossNotice, false);
	}
	
	@Override
	public Map<String, Object> getNoticeHtmlInfo(OssNotice ossNotice, boolean isProtocol) {
		Map<String, Object> model = new HashMap<String, Object>();
		
		String noticeType = "";
		String prjName = "";
		String prjVersion = "";
		String prjId = "";
		String distributeSite = "";
		int dashSeq = 0;
		boolean hideOssVersionFlag = CoConstDef.FLAG_YES.equals(ossNotice.getHideOssVersionYn());
		
		// NETWORK SERVER 여부를 체크한다.
		
		Project project = new Project();
		project.setPrjId(ossNotice.getPrjId());
		
		project = projectMapper.getProjectBasicInfo(project);
		
		if (project != null){
			if (isEmpty(prjName)) {
				prjName = project.getPrjName();
			}
			
			if (isEmpty(prjId)) {
				prjId = project.getPrjId();
			}
			
			if (isEmpty(prjVersion)) {
				prjVersion = project.getPrjVersion();
			}
			
			if (isEmpty(distributeSite)) {
				distributeSite = project.getDistributeTarget();
			}
		}
		
		List<OssComponents> ossComponentList = verificationMapper.selectVerificationNotice(ossNotice);
		if (CoConstDef.FLAG_YES.equals(avoidNull(ossNotice.getNetworkServerFlag()))) {
			List<OssComponents> collateOssComponentList = null;
			if (!CollectionUtils.isEmpty(ossComponentList)) {
				for (OssComponents ossComponent : ossComponentList) {
					String restrictionStr = ossComponent.getRestriction();
					if (!isEmpty(restrictionStr)) {
						String[] restrictions = restrictionStr.split(",");
						for (String restriction : restrictions) {
							if (CoConstDef.CD_LICENSE_NETWORK_RESTRICTION.equals(restriction.trim())) {
								if (collateOssComponentList == null) {
									collateOssComponentList = new ArrayList<>();
								}
								collateOssComponentList.add(ossComponent);
								break;
							}
						}
					}
				}
				ossComponentList = collateOssComponentList;
			}
		}
		
		// TYPE별 구분
		Map<String, OssComponents> noticeInfo = new HashMap<>();
		Map<String, OssComponents> srcInfo = new HashMap<>();
		Map<String, OssComponentsLicense> licenseInfo = new HashMap<>();
		Map<String, List<String>> componentCopyright = new HashMap<>();
		Map<String, List<String>> componentAttribution = new HashMap<>();
		
		OssComponents ossComponent;
		
		for (OssComponents bean : ossComponentList) {
			OssComponents oc = verificationMapper.checkOssNickName2(bean);
			if (oc != null) {
				OssMaster om = CoCodeManager.OSS_INFO_BY_ID.get(oc.getOssId());
				if (om != null) {
					String copyright = om.getCopyright();
					String homepage = om.getHomepage();
					
					if (isEmpty(bean.getCopyrightText()) && !isEmpty(copyright)) {
						bean.setCopyrightText(copyright);
					}
					
					if (isEmpty(bean.getHomepage()) && !isEmpty(homepage)) {
						bean.setHomepage(homepage);
					}
				}
			}
			
			String componentKey = (hideOssVersionFlag
									? bean.getOssName() 
									: bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
			
			if ("-".equals(bean.getOssName())) {
				componentKey += dashSeq++;
			}
			
			// type
			boolean isDisclosure = CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType()) || CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE_ONLY.equals(bean.getObligationType());
			// 2017.05.16 add by yuns start
			// obligation을 특정할 수 없는 oss도 bom에 merge 되도록 수정하면서, identification confirm시 refDiv가 '50'(고지대상)에 obligation을 특정할 수 없는 oss도 포함되어 등록되어
			// confirm 처리에서 obligation이 고지의무가 있거나 소스코드 공개의무가 있는 경우만 '50'으로 copy되도록 수정하였으나, 여기서 한번도 필터링함
			boolean isNotice = CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType());
			
			if (!isDisclosure && !isNotice) {
				continue;
			}
			
			// 2017.07.05
			// Accompanied with source code 의 경우
			// 소스공개여부와 상관없이 모두 소스공개가 필요한 oss table에 표시
			if (CoConstDef.CD_DTL_NOTICE_TYPE_ACCOMPANIED.equals(ossNotice.getNoticeType())) {
				isDisclosure = true;
			}
			
			// 2017.05.16 add by yuns end
			boolean addDisclosure = isDisclosure && srcInfo.containsKey(componentKey);
			boolean addNotice = !isDisclosure && noticeInfo.containsKey(componentKey);
			
			
			if (addDisclosure) {
				ossComponent = srcInfo.get(componentKey);
			} else if (addNotice) {
				ossComponent = noticeInfo.get(componentKey);
			} else {
				ossComponent = bean;
			}
			
			if (hideOssVersionFlag) {
				
				List<String> copyrightList = componentCopyright.containsKey(componentKey) 
						? (List<String>) componentCopyright.get(componentKey) 
						: new ArrayList<>();
						
				List<String> attributionList = componentAttribution.containsKey(componentKey) 
						? (List<String>) componentAttribution.get(componentKey) 
						: new ArrayList<>();
						
				if (!isEmpty(bean.getCopyrightText())) {
					for (String copyright : bean.getCopyrightText().split("\n")) {
						copyrightList.add(copyright);
					}
				}
				
				if (!isEmpty(bean.getOssAttribution())) {
					attributionList.add(bean.getOssAttribution());
				}

				// 라이선스 정보 생성
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());

				if (!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
					ossComponent.addOssComponentsLicense(license);
				}
				
				if (CoConstDef.FLAG_NO.equals(bean.getAdminCheckYn())) {
					String ossCopyright = findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright());
					
					// multi license 추가 copyright
					if (!isEmpty(ossCopyright)) {
						for (String copyright : ossCopyright.split("\n")) {
							copyrightList.add(copyright);
						}
					}
				}
				
				// 중복제거
				copyrightList = copyrightList.stream()
												.filter(CommonFunction.distinctByKey(c -> avoidNull(c).trim().toUpperCase()))
												.collect(Collectors.toList()); 
				ossComponent.setCopyrightText(String.join("\r\n", copyrightList));
				componentCopyright.put(componentKey, copyrightList);
				
				attributionList = attributionList.stream()
													.filter(CommonFunction.distinctByKey(a -> avoidNull(a).trim().toUpperCase()))
													.collect(Collectors.toList()); 
				ossComponent.setOssAttribution(String.join("\r\n", attributionList));
				componentAttribution.put(componentKey, attributionList);
				
				if (isDisclosure) {
					if (addDisclosure) {
						srcInfo.replace(componentKey, ossComponent);
					} else {
						srcInfo.put(componentKey, ossComponent);
					}
				} else {
					if (addNotice) {
						noticeInfo.replace(componentKey, ossComponent);
					} else {
						noticeInfo.put(componentKey, ossComponent);
					}
				}
				
				if (!licenseInfo.containsKey(license.getLicenseName())) {
					licenseInfo.put(license.getLicenseName(), license);
				}
			} else {
				
				// 라이선스 정보 생성
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());
				
				// 하나의 oss에 대해서 동일한 LICENSE가 복수 표시되는 현상 
				// 일단 여기서 막는다. (쿼리가 잘못된 건지, DATA가 꼬이는건지 모르겠음)
				if (!checkLicenseDuplicated(ossComponent.getOssComponentsLicense(), license)) {
					ossComponent.addOssComponentsLicense(license);
					
					// OSS의 Copyright text를 수정하였음에도 Packaging > Notice Preview에 업데이트 안 됨.
					// MULTI LICENSE를 가지는 oss의 개별로 추가된 copyright의 경우, Identification Confirm시에 DB에 업데이트한 정보를 기준으로 추출되기 때문에, preview 단계에서 오류가 발견되어 수정하여도 반영되지 않는다
					// verification단계에서의 oss_component_license는 oss_license의 license등록 순번을 가지고 있지 않기 때문에 (exclude된 license는 이관하지 않음)
					// 여기서 oss id와 license id를 이용하여 찾는다.
					// 동이한 라이선스를 or 구분으로 여러번 정의한 경우 문제가 될 수 있으나, 동일한 oss의 동일한 license의 경우 같은 copyright를 추가한다는 전제하에 적용함 (이부분에서 추가적인 이슉가 발생할 경우 대응방법이 복잡해짐)
					if (CoConstDef.FLAG_NO.equals(ossComponent.getAdminCheckYn())) {
						bean.setOssCopyright(findAddedOssCopyright(bean.getOssId(), bean.getLicenseId(), bean.getOssCopyright()));
						
						// multi license 추가 copyright
						if (!isEmpty(bean.getOssCopyright())) {
							String addCopyright = avoidNull(ossComponent.getCopyrightText());
							
							if (!isEmpty(ossComponent.getCopyrightText())) {
								addCopyright += "\r\n";
							}
							 
							addCopyright += bean.getOssCopyright();
							ossComponent.setCopyrightText(addCopyright);
						}
					}
				}
				
				if (isDisclosure) {
					if (addDisclosure) {
						srcInfo.replace(componentKey, ossComponent);
					} else {
						srcInfo.put(componentKey, ossComponent);
					}
				} else {
					if (addNotice) {
						noticeInfo.replace(componentKey, ossComponent);
					} else {
						noticeInfo.put(componentKey, ossComponent);
					}
				}
				
				if (!licenseInfo.containsKey(license.getLicenseName())) {
					licenseInfo.put(license.getLicenseName(), license);
				}
			}
		}
		
		// copyleft에 존재할 경우 notice에서는 출력하지 않고 copyleft로 merge함.
		if (hideOssVersionFlag) {
			Map<String, OssComponents> hideOssVersionMergeNoticeInfo = new HashMap<>();
			Set<String> noticeKeyList = noticeInfo.keySet();
			
			for (String key : noticeKeyList) {
				if (!srcInfo.containsKey(key)) {
					hideOssVersionMergeNoticeInfo.put(key, noticeInfo.get(key));
				}
			}
			
			noticeInfo = hideOssVersionMergeNoticeInfo;
		}
		
		// CLASS 파일만 등록한 경우 라이선스 정보만 추가한다.
		// OSS NAME을 하이픈 ('-') 으로 등록한 경우 (고지문구에 라이선스만 추가)
		List<OssComponents> addOssComponentList = verificationMapper.selectVerificationNoticeClassAppend(ossNotice);
		
		if (addOssComponentList != null) {
			List<String> checkKeyInfo = new ArrayList<>();
			
			for (OssComponents bean : addOssComponentList) {
				String componentKey = (hideOssVersionFlag
											? bean.getOssName() 
											: bean.getOssName() + "|" + bean.getOssVersion()).toUpperCase();
				
				String checkKey = (hideOssVersionFlag
										? bean.getOssName() + "|" + bean.getLicenseName()
										: bean.getOssName() + "|" + bean.getOssVersion() + "|" + bean.getLicenseName()).toUpperCase();
				
				if (checkKeyInfo.contains(checkKey)) {
					continue;
				}
				
				if ("-".equals(bean.getOssName())) {
					componentKey += dashSeq++;
				}
				
				OssComponentsLicense license = new OssComponentsLicense();
				license.setLicenseId(bean.getLicenseId());
				license.setLicenseName(bean.getLicenseName());
				license.setLicenseText(bean.getLicenseText());
				license.setAttribution(bean.getAttribution());
				bean.addOssComponentsLicense(license);
				
				if (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())
						|| CoConstDef.CD_DTL_NOTICE_TYPE_ACCOMPANIED.equals(ossNotice.getNoticeType())) { // Accompanied with source code 의 경우 source 공개 의무
					srcInfo.put(componentKey, bean);
				} else {
					noticeInfo.put(componentKey, bean);
				}
				
				if (!licenseInfo.containsKey(license.getLicenseName())) {
					licenseInfo.put(componentKey, license);
				}
				
				checkKeyInfo.add(checkKey);
			}
		}
		
		boolean isTextNotice = "text".equals(ossNotice.getFileType());
		
		Map<String, String> ossAttributionMap = new HashMap<>();
		// 개행처리 및 velocity용 list 생성
		List<OssComponents> noticeList = new ArrayList<>();
		
		for (OssComponents bean : noticeInfo.values()) {
			if (isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getOssAttribution()))));
			}

			if (!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if (!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			if (isProtocol && !isEmpty(bean.getHomepage()) && !bean.getHomepage().contains("://")) bean.setHomepage("http://" + bean.getHomepage());
			
			noticeList.add(bean);
		}
		
		Collections.sort(noticeList, new Comparator<OssComponents>() {
			@Override
			public int compare(OssComponents oc1, OssComponents oc2) {
				return oc1.getOssName().toUpperCase().compareTo(oc2.getOssName().toUpperCase());
			}
		});
		
		List<OssComponents> srcList = new ArrayList<>();
		
		for (OssComponents bean : srcInfo.values()) {
			if (isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getOssAttribution()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getLicenseText()))));
				bean.setOssAttribution(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getOssAttribution()))));
			}
			

			if (!isEmpty(bean.getOssAttribution()) && !ossAttributionMap.containsKey(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()))) {
				ossAttributionMap.put(avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion()), avoidNull(bean.getOssName(), "") + "__" + bean.getOssAttribution());
			}
			
			if (!isEmpty(bean.getOssName())) {
				bean.setOssName(StringUtil.replaceHtmlEscape(bean.getOssName()));
			}
			
			if (isProtocol && !isEmpty(bean.getHomepage()) && !bean.getHomepage().contains("://")) {
				bean.setHomepage("//" + bean.getHomepage());
			}
			
			srcList.add(bean);
		}
		
		Collections.sort(srcList, new Comparator<OssComponents>() {
			@Override
			public int compare(OssComponents oc1, OssComponents oc2) {
				return oc1.getOssName().toUpperCase().compareTo(oc2.getOssName().toUpperCase());
			}
		});
		
		List<OssComponentsLicense> licenseList = new ArrayList<>();
		List<OssComponentsLicense> licenseListUrls = new ArrayList<>(); //simple version용
		List<OssComponentsLicense> attributionList = new ArrayList<>();
		List<String> ossAttributionList = new ArrayList<>();
		
		// 정렬
		TreeMap<String, OssComponentsLicense> licenseTreeMap = new TreeMap<>( licenseInfo );
		
		for (OssComponentsLicense bean : licenseTreeMap.values()) {
			if (isTextNotice) {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.unescapeHtml4(avoidNull(bean.getLicenseText()))));
			} else {
				bean.setCopyrightText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getCopyrightText()))));
				bean.setLicenseText(CommonFunction.lineReplaceToBR(StringEscapeUtils.escapeHtml4(avoidNull(bean.getLicenseText()))));
			}
			
			// 배포사이트 license text url
			licenseList.add(bean);
			
			if (CoConstDef.FLAG_YES.equals(ossNotice.getSimpleNoticeFlag())) {
				LicenseMaster licenseBean = CoCodeManager.LICENSE_INFO_BY_ID.get(bean.getLicenseId());
				
				if (licenseBean != null) {
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

			if (!isEmpty(bean.getAttribution())) {
				bean.setAttribution(CommonFunction.lineReplaceToBR(avoidNull(bean.getAttribution())));
				attributionList.add(bean);
			}
		}
		
		TreeMap<String, String> ossAttributionTreeMap = new TreeMap<>( ossAttributionMap );
		ossAttributionList.addAll(ossAttributionTreeMap.values());
		
		// 배포 사이트 구분에 따라 참조 코드가 달라짐
		String noticeInfoCode = CoConstDef.CD_NOTICE_DEFAULT;

		noticeType = avoidNull(ossNotice.getNoticeType(), CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL);
		
		String companyNameFull = ossNotice.getCompanyNameFull();
		String distributionSiteUrl = ossNotice.getDistributionSiteUrl();
		String email = ossNotice.getEmail();
		String noticeAppendType = ossNotice.getNoticeAppendType();
		String appendedContentsTEXT = ossNotice.getAppendedTEXT();
		String appendedContents = "";;
		
		if (avoidNull(noticeAppendType).equals("F") && !isEmpty(project.getNoticeAppendFileId())) {
			List<T2File> appendFileInfoList = verificationMapper.selectNoticeAppendFile(project.getNoticeAppendFileId());
			if (!CollectionUtils.isEmpty(appendFileInfoList)) {
				String customAppendedContents = "";
				for (int i=0; i<appendFileInfoList.size(); i++) {
					if (appendFileInfoList.get(i).getExt().equalsIgnoreCase("txt")) {
						if ("text".equals(ossNotice.getFileType())){
							customAppendedContents += CommonFunction.getStringFromFile(appendFileInfoList.get(i).getLogiPath() + "/" + appendFileInfoList.get(i).getLogiNm(), false);
							if (i < appendFileInfoList.size()-1) {
								customAppendedContents += "<br>_________________________________________________________________________________________________________________________<br><br>";
							}
						} else {
							customAppendedContents += "<p class='bdTop'>" + CommonFunction.getStringFromFile(appendFileInfoList.get(i).getLogiPath() + "/" + appendFileInfoList.get(i).getLogiNm(), false) + "</p>";
						}
					} else {
						if ("text".equals(ossNotice.getFileType())){
							customAppendedContents += CommonFunction.getStringFromFile(appendFileInfoList.get(i).getLogiPath() + "/" + appendFileInfoList.get(i).getLogiNm());
							if (i < appendFileInfoList.size()-1) {
								customAppendedContents += "<br>_________________________________________________________________________________________________________________________<br><br>";
							}
						} else {
							customAppendedContents += "<p class='bdTop'>" + CommonFunction.getStringFromFile(appendFileInfoList.get(i).getLogiPath() + "/" + appendFileInfoList.get(i).getLogiNm()) + "</p>";
						}
					}
				}
				appendedContents = customAppendedContents;
			}
		} else {
			appendedContents = "<p class='bdTop'>" + ossNotice.getAppended() + "</p>";
		}
		
		if (!isEmpty(distributionSiteUrl) && !(distributionSiteUrl.startsWith("http://") || distributionSiteUrl.startsWith("https://") || distributionSiteUrl.startsWith("ftp://"))) {
			distributionSiteUrl = "http://" + distributionSiteUrl;
		}
		
		String noticeTitle = CommonFunction.getNoticeFileName(prjId, prjName, prjVersion, CommonFunction.getCurrentDateTime("yyMMdd"), ossNotice.getFileType());
		String noticeFileName = "";
		if (noticeTitle.endsWith(".txt")) {
			noticeFileName = noticeTitle.substring(0, noticeTitle.length()-4);
		} else {
			noticeFileName = noticeTitle;
		}
		
		model.put("noticeType", noticeType);
		model.put("noticeTitle", noticeTitle);
		model.put("noticeFileName", noticeFileName);
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
		if (CoConstDef.FLAG_YES.equals(ossNotice.getSimpleNoticeFlag())) {
			model.put("licenseListUrls", licenseListUrls);
		} else {
			model.put("licenseList", licenseList);
		}
		
		model.put("attributionList", attributionList.isEmpty() ? null : attributionList);
		model.put("ossAttributionList", ossAttributionList.isEmpty() ? null : ossAttributionList);
		
		if ("text".equals(ossNotice.getFileType())){
			if (!isEmpty(appendedContentsTEXT)) {
				model.put("appended", avoidNull(appendedContentsTEXT, "").replaceAll("&nbsp;", " "));
			} else {
				model.put("appended", avoidNull(appendedContents, "").replaceAll("&nbsp;", " "));
			}
		} else {
			model.put("appended", appendedContents);
		}

		if ("text".equals(ossNotice.getFileType())){
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
		if (ossComponentsLicense != null) {
			for (OssComponentsLicense bean : ossComponentsLicense) {
				if (bean.getLicenseId().equals(license.getLicenseId())) {
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
		if (!isEmpty(ossId) && !isEmpty(licenseId)) {
			OssMaster bean = CoCodeManager.OSS_INFO_BY_ID.get(ossId);
			if (bean != null) {
				for (OssLicense license : bean.getOssLicenses()) {
					if (licenseId.equals(license.getLicenseId()) && !isEmpty(license.getOssCopyright())) {
						return license.getOssCopyright();
					}
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
			
			if (deCompResultMap.get(url.substring(0, url.lastIndexOf("/"))) != null){
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
		
		for (OssComponents info : gridData) {
			if (isEmpty(groupColumn)) {
				groupColumn = info.getOssName() + "-" + info.getOssVersion();
			}
						
			if (groupColumn.equals(info.getOssName() + "-" + info.getOssVersion()) // 같은 groupColumn이면 데이터를 쌓음
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
		if (tempData.size() > 0) {
			Collections.sort(tempData, new Comparator<OssComponents>() {
				@Override
				public int compare(OssComponents o1, OssComponents o2) {
					if (o1.getLicenseName().length() >= o2.getLicenseName().length()) {
						return 1;
					}else {
						return -1;
					}
				}
			});
			
			OssComponents rtnBean = null;
			
			for (OssComponents temp : tempData) {
				if (rtnBean == null) {
					rtnBean = temp;
					
					continue;
				}
				
				String key = temp.getOssName() + "-" + temp.getLicenseType();
				
				if ("--NA".equals(key)) {
					if (!rtnBean.getLicenseName().contains(temp.getLicenseName())) {
						resultGridData.add(rtnBean);
						rtnBean = temp;
						
						continue;
					}
				}
				
				for (String licenseName : temp.getLicenseName().split(",")) {
					boolean equalFlag = false;
					
					for (String rtnLicenseName : rtnBean.getLicenseName().split(",")) {
						if (rtnLicenseName.equals(licenseName)) {
							equalFlag = true;
							
							break;
						}
					}
					
					if (!equalFlag) {
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
		
		if (fileSeq.equals("6")) {
			prjParam.setNoticeAppendFileId(registFileId);
			verificationMapper.updateNoticeAppendFile(prjParam);
		} else {
			Project project = projectMapper.selectProjectMaster(prjParam.getPrjId());
			
			if (fileSeq.equals("1")) {
				prjParam.setPackageFileId(registFileId);
				prjParam.setPackageFileId2(project.getPackageFileId2() != null ? project.getPackageFileId2() : null);
				prjParam.setPackageFileId3(project.getPackageFileId3() != null ? project.getPackageFileId3() : null);
				prjParam.setPackageFileId4(project.getPackageFileId4() != null ? project.getPackageFileId4() : null);
				prjParam.setPackageFileId5(project.getPackageFileId5() != null ? project.getPackageFileId5() : null);
			} else if (fileSeq.equals("2")) {
				prjParam.setPackageFileId(project.getPackageFileId() != null ? project.getPackageFileId() : null);
				prjParam.setPackageFileId2(registFileId);
				prjParam.setPackageFileId3(project.getPackageFileId3() != null ? project.getPackageFileId3() : null);
				prjParam.setPackageFileId4(project.getPackageFileId4() != null ? project.getPackageFileId4() : null);
				prjParam.setPackageFileId5(project.getPackageFileId5() != null ? project.getPackageFileId5() : null);
			} else if (fileSeq.equals("3")) {
				prjParam.setPackageFileId(project.getPackageFileId() != null ? project.getPackageFileId() : null);
				prjParam.setPackageFileId2(project.getPackageFileId2() != null ? project.getPackageFileId2() : null);
				prjParam.setPackageFileId3(registFileId);
				prjParam.setPackageFileId4(project.getPackageFileId4() != null ? project.getPackageFileId4() : null);
				prjParam.setPackageFileId5(project.getPackageFileId5() != null ? project.getPackageFileId5() : null);
			} else if (fileSeq.equals("4")) {
				prjParam.setPackageFileId(project.getPackageFileId() != null ? project.getPackageFileId() : null);
				prjParam.setPackageFileId2(project.getPackageFileId2() != null ? project.getPackageFileId2() : null);
				prjParam.setPackageFileId3(project.getPackageFileId3() != null ? project.getPackageFileId3() : null);
				prjParam.setPackageFileId4(registFileId);
				prjParam.setPackageFileId5(project.getPackageFileId5() != null ? project.getPackageFileId5() : null);
			} else {
				prjParam.setPackageFileId(project.getPackageFileId() != null ? project.getPackageFileId() : null);
				prjParam.setPackageFileId2(project.getPackageFileId2() != null ? project.getPackageFileId2() : null);
				prjParam.setPackageFileId3(project.getPackageFileId3() != null ? project.getPackageFileId3() : null);
				prjParam.setPackageFileId4(project.getPackageFileId4() != null ? project.getPackageFileId4() : null);
				prjParam.setPackageFileId5(registFileId);
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
			if (prjParam.getPackageFileId4() != null) {
				fileSeqs.add(prjParam.getPackageFileId4()); 
			}
			if (prjParam.getPackageFileId5() != null) {
				fileSeqs.add(prjParam.getPackageFileId5()); 
			}
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("prjId", prjId);
			map.put("fileSeqs", fileSeqs);
			
			String packagingComment = "";
			
			try {
				packagingComment = fileService.setClearFiles(map);
			} catch(Exception e) {
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
				
				commentService.registComment(commHisBean, false);
			}
		}
	}
	
	@Override
	public void updateProjectAllowDownloadBitFlag(Project project) {
		project.setAllowDownloadBitFlag(allowDownloadMultiFlagToBitFlag(project));
		
		projectMapper.updateProjectAllowDownloadBitFlag(project);
	}

	public int allowDownloadMultiFlagToBitFlag(Project project) {
		int bitFlag = 1;
		
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadNoticeHTMLYn())) {
			bitFlag |= CoConstDef.FLAG_A;
		}
			
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadNoticeTextYn())) {
			bitFlag |= CoConstDef.FLAG_B;
		}
		
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleHTMLYn())) {
			bitFlag |= CoConstDef.FLAG_C;
		}
			
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSimpleTextYn())) {
			bitFlag |= CoConstDef.FLAG_D;
		}
			
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXSheetYn())) {
			bitFlag |= CoConstDef.FLAG_E;
		}
			
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXRdfYn())) {
			bitFlag |= CoConstDef.FLAG_F;
		}
			
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXTagYn())) {
			bitFlag |= CoConstDef.FLAG_G;
		}

		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXJsonYn())) {
			bitFlag |= CoConstDef.FLAG_H;
		}

		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadSPDXYamlYn())) {
			bitFlag |= CoConstDef.FLAG_I;
		}
		
		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadCDXJsonYn())) {
			bitFlag |= CoConstDef.FLAG_J;
		}

		if (CoConstDef.FLAG_YES.equals(project.getAllowDownloadCDXXmlYn())) {
			bitFlag |= CoConstDef.FLAG_K;
		}
		
		return bitFlag;
	}
	
	@Override
	public void registOssNoticeConfirmStatus(OssNotice ossNotice) {
		try{
			Project project = projectMapper.selectProjectMaster(ossNotice.getPrjId());
			
			// android project는 notice를 사용하지 않음.
			if (!CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equalsIgnoreCase(project.getNoticeType())) {
				verificationMapper.insertOssNotice(ossNotice);
			}
			
			if (isEmpty(project.getVerificationStatus())){
				verificationMapper.updateVerificationStatusProgress(ossNotice);
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void deleteFile(Map<Object, Object> map) {
		String delFileSeq = "";
		String prjId = (String) map.get("prjId");
		
		if (map.containsKey("delFileId")) {
			delFileSeq = (String) map.get("delFileId");
			List<T2File> fileList = fileService.getFileInfoList(delFileSeq);
			if (!CollectionUtils.isEmpty(fileList)) {
				for (T2File file : fileList) {
					fileMapper.updateFileDelYn(new String[] {file.getFileSeq()});
					fileService.deletePhysicalFile(file, "VERIFY");
				}
			}
		} else {
			delFileSeq = (String) map.get("delFileSeq");
			T2File file = fileService.selectFileInfo(delFileSeq);
			// delete logical file
			fileMapper.updateFileDelYn(new String[] {delFileSeq});
			// delete physical file
			fileService.deletePhysicalFile(file, "VERIFY");
		}
		
		// update file id
		Project project = new Project();
		project.setPrjId(prjId);
		Project prjInfo = projectMapper.getProjectBasicInfo(project);
		List<T2File> noticeAppendFile = verificationMapper.selectNoticeAppendFile(prjInfo.getNoticeAppendFileId());
		if (CollectionUtils.isEmpty(noticeAppendFile)) {
			project.setNoticeAppendFileId(null);
			verificationMapper.updateNoticeAppendFile(project);
		}
	}

	@Override
	public void updateFileWhenVerificationCopyConfirm(Project project, Project copyProject, List<String> packageFileSeqList) throws IOException {
		Project param = new Project();
		param.setPrjId(project.getPrjId());
		
		// update package file seq
		int fileSize = packageFileSeqList.size();
		switch (fileSize) {
			case 1 : param.setPackageFileId(packageFileSeqList.get(0));
					param.setPackageFileType1(copyProject.getPackageFileType1());
				break;
			case 2 : param.setPackageFileId(packageFileSeqList.get(0)); param.setPackageFileId2(packageFileSeqList.get(1));
					param.setPackageFileType1(copyProject.getPackageFileType1()); param.setPackageFileType2(copyProject.getPackageFileType2());
				break;
			case 3 : param.setPackageFileId(packageFileSeqList.get(0)); param.setPackageFileId2(packageFileSeqList.get(1)); param.setPackageFileId3(packageFileSeqList.get(2));
					param.setPackageFileType1(copyProject.getPackageFileType1()); param.setPackageFileType2(copyProject.getPackageFileType2());
					param.setPackageFileType3(copyProject.getPackageFileType3());
				break;
			case 4 : param.setPackageFileId(packageFileSeqList.get(0)); param.setPackageFileId2(packageFileSeqList.get(1)); param.setPackageFileId3(packageFileSeqList.get(2)); param.setPackageFileId4(packageFileSeqList.get(3));
					param.setPackageFileType1(copyProject.getPackageFileType1()); param.setPackageFileType2(copyProject.getPackageFileType2());
					param.setPackageFileType3(copyProject.getPackageFileType3()); param.setPackageFileType4(copyProject.getPackageFileType4());
				break;
			default : param.setPackageFileId(packageFileSeqList.get(0)); param.setPackageFileId2(packageFileSeqList.get(1)); param.setPackageFileId3(packageFileSeqList.get(2)); param.setPackageFileId4(packageFileSeqList.get(3)); param.setPackageFileId5(packageFileSeqList.get(4));
					param.setPackageFileType1(copyProject.getPackageFileType1()); param.setPackageFileType2(copyProject.getPackageFileType2());
					param.setPackageFileType3(copyProject.getPackageFileType3()); param.setPackageFileType4(copyProject.getPackageFileType4());
					param.setPackageFileType5(copyProject.getPackageFileType5());
				break;
		}
		
		verificationMapper.updatePackageFile(param);
		
		// update verify result file
		param.setReadmeContent(copyProject.getReadmeContent());
		param.setReadmeFileName(copyProject.getReadmeFileName());
		param.setReadmeYn(copyProject.getReadmeYn());
		param.setExceptFileContent(copyProject.getExceptFileContent());
		param.setVerifyFileContent(copyProject.getVerifyFileContent());
		param.setBinaryFileYn(copyProject.getBinaryFileYn());
		
		boolean isCopyDir = !isEmpty(param.getReadmeContent()) || !isEmpty(param.getExceptFileContent()) || !isEmpty(param.getVerifyFileContent()) || !isEmpty(param.getBinaryFileYn());
		if (isCopyDir) {
			File srcDir = new File(VERIFY_PATH_OUTPUT + "/" + copyProject.getPrjId());
			File destDir = new File(VERIFY_PATH_OUTPUT + "/" + param.getPrjId());
			FileUtils.copyDirectory(srcDir, destDir);
			
			projectService.registReadmeContent(param);
			projectService.registVerifyContents(param);
		}
	}

	@Override
	public Map<String, Object> checkNoticeHtmlInfo(OssNotice ossNotice) {
		Map<String, Object> rtnMap = new HashMap<>();
		List<String> rtnList = new ArrayList<>();
		
		String referenceDiv = !CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(ossNotice.getNoticeType()) ? CoConstDef.CD_DTL_COMPONENT_ID_BOM : CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM;
		ProjectIdentification identification = new ProjectIdentification();
		identification.setReferenceId(ossNotice.getPrjId());
		identification.setReferenceDiv(referenceDiv);
		identification.setMerge(CoConstDef.FLAG_NO);
		
		Map<String, Object> map = projectService.getIdentificationGridList(identification, true);
		if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(referenceDiv)) {
			map.replace("rows", projectService.setMergeGridData((List<ProjectIdentification>) map.get("rows")));
		}
		
		List<ProjectIdentification> bomList = (List<ProjectIdentification>) map.get("rows");
		T2CoProjectValidator pv = new T2CoProjectValidator();
		pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BOM_MERGE);
		pv.setAppendix("bomList", bomList);
		
		T2CoValidationResult vr = pv.validate(new HashMap<>());
		boolean isValid = true;
		if (!vr.isValid()) {
			Map<String, String> customErrorMap = new HashMap<>();
			for (String key : vr.getValidMessageMap().keySet()) {
				if (!key.contains(".")) {
					continue;
				}
				String message = vr.getValidMessageMap().get(key);
				if (message.equalsIgnoreCase("New open source") || message.equalsIgnoreCase("New version")) {
					isValid = false;
					String componentId = key.split("[.]")[1];
					customErrorMap.put(componentId, message);
				}
			}
			if (!isValid) {
				for (ProjectIdentification pi : bomList) {
					if (customErrorMap.containsKey(pi.getComponentId())) {
						String ossInfo = pi.getOssName() + " (" + avoidNull(pi.getOssVersion(), "N/A") + ")";
						if (!rtnList.contains(ossInfo)) {
							rtnList.add(ossInfo);
						}
					}
				}
			}
		}
		
		rtnMap.put("isValid", isValid);
		rtnMap.put("data", rtnList);
		return rtnMap;
	}

	@Override
	public String getNoticeAppendInfo(String prjId) {
		return verificationMapper.selectNoticeAppendInfo(prjId);
	}
}
