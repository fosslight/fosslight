/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.reflect.TypeToken;

import io.jsonwebtoken.lang.Arrays;
import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.VERIFICATION;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.File;
import oss.fosslight.domain.History;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.HistoryService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.VerificationService;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@Controller
@Slf4j
public class VerificationController extends CoTopComponent {
	@Autowired ProjectService projectService;
	@Autowired CommentService commentService;
	@Autowired VerificationService verificationService;
	@Autowired FileService fileService;
	@Autowired HistoryService historyService;
	
	@Autowired VerificationMapper verificationMapper;
	@Autowired CodeMapper codeMapper;
	
	@GetMapping(value = VERIFICATION.PAGE_ID, produces = "text/html; charset=utf-8")
	public String list(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) {
		log.info("URI: "+ "/project/verification/"+prjId);
		Project project = new Project();
		project.setPrjId(prjId);
		
		Project projectMaster = projectService.getProjectDetail(project);
		projectMaster.setVulDocInfo(CommonFunction.getMessageForVulDOC(req, "info"));
		projectMaster.setVulDocInst(CommonFunction.getMessageForVulDOC(req, "inst").replace("<br />", " "));
		
		if (!StringUtil.isEmpty(projectMaster.getCreator())){
			projectMaster.setPrjDivision(projectService.getDivision(projectMaster));	
		}
		
//		CommentsHistory comHisBean = new CommentsHistory();
//		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_USER);
//		comHisBean.setReferenceId(projectMaster.getPrjId());
//		projectMaster.setUserComment(commentService.getUserComment(comHisBean));
		
		OssNotice _noticeInfo = projectService.setCheckNotice(projectMaster);
		
		if (_noticeInfo != null) {
			// Notice Type: Accompanied with source code인 경우 Default Company Name, Email 세팅
			model.addAttribute("ossNotice", _noticeInfo);
		}
		
		List<OssComponents> list = null;
		try {
			list = verificationService.getVerifyOssList(projectMaster);
			if (list != null) list = verificationService.setMergeGridData(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<LicenseMaster> userGuideLicenseList = new ArrayList<>();
		List<String> duplLicenseCheckList = new ArrayList<>(); // 중목제거용
		
		// 사용중인 라이선스에 user guide가 설정되어 있는 경우 체크
		if (list != null && !list.isEmpty()) {
			for (OssComponents bean : list) {
				// multi license의 경우 콤마 구분으로 반환됨
				if (!isEmpty(bean.getLicenseName())) {
					LicenseMaster licenseBean;
					for (String license : bean.getLicenseName().split(",", -1)) {
						licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
						if (licenseBean != null && !isEmptyWithLineSeparator(licenseBean.getDescription()) 
								&& !duplLicenseCheckList.contains(licenseBean.getLicenseId())
								&& CoConstDef.FLAG_YES.equals(avoidNull(licenseBean.getObligationDisclosingSrcYn()))) {
							userGuideLicenseList.add(licenseBean);
							duplLicenseCheckList.add(licenseBean.getLicenseId());
						}
					}
				}
			}
		}
		
		boolean existsFileFlag = false;
		List<File> files = new ArrayList<File>();
		File packageFile = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId());
		File packageFile2 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId2());
		File packageFile3 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId3());
		File packageFile4 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId4());
		File packageFile5 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId5());
		List<T2File> noticeAppendFile = verificationMapper.selectNoticeAppendFile(projectMaster.getNoticeAppendFileId());
		
		if (noticeAppendFile != null) {
			model.addAttribute("noticeAppendFile", noticeAppendFile);
		}
		
		if (packageFile != null) {
			if (!isEmpty(packageFile.getRefPrjId())) {
				projectMaster.setReuseRefPrjId1(packageFile.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile2 != null) {
			if (!isEmpty(packageFile2.getRefPrjId())) {
				projectMaster.setReuseRefPrjId2(packageFile2.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile3 != null) {
			if (!isEmpty(packageFile3.getRefPrjId())) {
				projectMaster.setReuseRefPrjId3(packageFile3.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile4 != null) {
			if (!isEmpty(packageFile4.getRefPrjId())) {
				projectMaster.setReuseRefPrjId4(packageFile4.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile5 != null) {
			if (!isEmpty(packageFile5.getRefPrjId())) {
				projectMaster.setReuseRefPrjId5(packageFile5.getRefPrjId());
			}
			existsFileFlag = true;
		}
		files.add(packageFile);
		files.add(packageFile2);
		files.add(packageFile3);
		files.add(packageFile4);
		files.add(packageFile5);
		
		//프로젝트 정보
		model.addAttribute("project", projectMaster);
		model.addAttribute("verify", verificationService.getVerificationOne(project));
		model.addAttribute("ossList", list);
		model.addAttribute("files", files);
		model.addAttribute("existsFileFlag", existsFileFlag);
		model.addAttribute("userGuideLicenseList", userGuideLicenseList);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		
		return "project/verification";
	}
	
	@GetMapping(value = VERIFICATION.PAGE_DIV_ID, produces = "text/html; charset=utf-8")
	public String list2(@PathVariable String prjId, @PathVariable String initDiv, HttpServletRequest req, HttpServletResponse res, Model model) {
		log.info("URI: "+ "/project/verification/"+prjId);
		
		Project project = new Project();
		project.setPrjId(prjId);
		
		Project projectMaster = projectService.getProjectDetail(project);
		if (!StringUtil.isEmpty(projectMaster.getCreator())){
			projectMaster.setPrjDivision(projectService.getDivision(projectMaster));	
		}
		
//		CommentsHistory comHisBean = new CommentsHistory();
//		comHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_USER);
//		comHisBean.setReferenceId(projectMaster.getPrjId());
//		projectMaster.setUserComment(commentService.getUserComment(comHisBean));
		
		//프로젝트 정보
		model.addAttribute("project", projectMaster);
		
		OssNotice _noticeInfo = projectService.setCheckNotice(projectMaster);
		
		if (_noticeInfo != null) {
			// Notice Type: Accompanied with source code인 경우 Default Company Name, Email 세팅
			model.addAttribute("ossNotice", _noticeInfo);
		}
		
		List<OssComponents> list = null;
		try {
			list = verificationService.getVerifyOssList(projectMaster);
			if (list != null) list = verificationService.setMergeGridData(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<LicenseMaster> userGuideLicenseList = new ArrayList<>();
		// 중목제거용
		List<String> duplLicenseCheckList = new ArrayList<>();
		
		// 사용중인 라이선스에 user guide가 설정되어 있는 경우 체크
		if (list != null && !list.isEmpty()) {
			for (OssComponents bean : list) {
				// multi license의 경우 콤마 구분으로 반환됨
				if (!isEmpty(bean.getLicenseName())) {
					LicenseMaster licenseBean;
					for (String license : bean.getLicenseName().split(",", -1)) {
						licenseBean = CoCodeManager.LICENSE_INFO_UPPER.get(license.toUpperCase());
						if (licenseBean != null && !isEmptyWithLineSeparator(licenseBean.getDescription()) 
								&& !duplLicenseCheckList.contains(licenseBean.getLicenseId())
								&& CoConstDef.FLAG_YES.equals(avoidNull(licenseBean.getObligationDisclosingSrcYn()))) {
							userGuideLicenseList.add(licenseBean);
							duplLicenseCheckList.add(licenseBean.getLicenseId());
						}
					}
				}
			}
		}
		
		boolean existsFileFlag = false;
		List<File> files = new ArrayList<File>();

		File packageFile = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId());
		File packageFile2 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId2());
		File packageFile3 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId3());
		File packageFile4 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId4());
		File packageFile5 = verificationMapper.selectVerificationFile(projectMaster.getPackageFileId5());
		List<T2File> noticeAppendFile = verificationMapper.selectNoticeAppendFile(projectMaster.getNoticeAppendFileId());
		
		if (noticeAppendFile != null) {
			model.addAttribute("noticeAppendFile", noticeAppendFile);
		}
		
		if (packageFile != null) {
			if (!isEmpty(packageFile.getRefPrjId())) {
				projectMaster.setReuseRefPrjId1(packageFile.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile2 != null) {
			if (!isEmpty(packageFile2.getRefPrjId())) {
				projectMaster.setReuseRefPrjId2(packageFile2.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile3 != null) {
			if (!isEmpty(packageFile3.getRefPrjId())) {
				projectMaster.setReuseRefPrjId3(packageFile3.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile4 != null) {
			if (!isEmpty(packageFile4.getRefPrjId())) {
				projectMaster.setReuseRefPrjId4(packageFile4.getRefPrjId());
			}
			existsFileFlag = true;
		}
		if (packageFile5 != null) {
			if (!isEmpty(packageFile5.getRefPrjId())) {
				projectMaster.setReuseRefPrjId5(packageFile5.getRefPrjId());
			}
			existsFileFlag = true;
		}
		files.add(packageFile);
		files.add(packageFile2);
		files.add(packageFile3);
		files.add(packageFile4);
		files.add(packageFile5);
		
		model.addAttribute("verify", verificationService.getVerificationOne(project));
		model.addAttribute("ossList", list);
		model.addAttribute("files", files);
		model.addAttribute("initDiv", initDiv);
		model.addAttribute("existsFileFlag", existsFileFlag);
		model.addAttribute("userGuideLicenseList", userGuideLicenseList);
		model.addAttribute("distributionFlag", CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES));
		
		return "project/verification";
	}
	
	@GetMapping(value = VERIFICATION.NOTICE_APPEND_INFO)
	public @ResponseBody ResponseEntity<Object> appendInfo(@PathVariable String prjId, HttpServletRequest req, HttpServletResponse res, Model model) {
		Map<String, Object> rtnMap = new HashMap<>();
		String appended = verificationService.getNoticeAppendInfo(prjId);
		if (!isEmpty(appended)) {
			rtnMap.put("isValid", true);
			rtnMap.put("appended", appended);
		} else {
			rtnMap.put("isValid", false);
		}
		return makeJsonResponseHeader(rtnMap);
	}
	
	@ResponseBody
	@PostMapping(value=VERIFICATION.REGIST_FILE)
	public String registFile(T2File file, MultipartHttpServletRequest req, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {
		log.info("URI: "+ "/project/verification/registFile");
		
		String permission = StringUtil.isEmpty(req.getParameter("permission")) ? null : req.getParameter("permission");
		ArrayList<Object> resultList = new ArrayList<Object>();
		
		if (permission.equals("1")) {
			//파일 등록
			List<UploadFile> list = new ArrayList<UploadFile>();
			boolean appendFileFlag = false;
			
			String fileId = StringUtil.isEmpty(req.getParameter("fileId")) ? null : req.getParameter("fileId");
			String prjId = req.getParameter("prjId");
			String filePath = CommonFunction.emptyCheckProperty("packaging.path", "/upload/packaging") + "/" + prjId;

			Map<String, MultipartFile> fileMap = req.getFileMap();
			String fileExtension = StringUtils.getFilenameExtension(fileMap.get("myfile").getOriginalFilename());
			String fileSeq = StringUtil.isEmpty(req.getParameter("fileSeq")) ? null : req.getParameter("fileSeq");
			
			//파일 등록
			if (req.getContentType() != null && req.getContentType().toLowerCase().indexOf("multipart/form-data") > -1 ) {
				file.setCreator(loginUserName());

				//파일 확장자 체크
				String codeExp = "";
				switch (fileSeq) {
					case "6": 
						codeExp = codeMapper.getCodeDetail("120", "41").getCdDtlExp();
						appendFileFlag = true;
						break;
					default : 
						codeExp = codeMapper.getCodeDetail("120", "16").getCdDtlExp();
						break;
				}
				
				String[] exts = codeExp.split(",");
				boolean fileExtCheck = false;
				for (String s : exts) {
					if (s.equals(fileExtension)) {
						fileExtCheck = true;
					}
				}

				if(!fileExtCheck) {
					resultList.add("UNSUPPORTED_FILE");
					String msg = getMessage("msg.project.packaging.upload.fileextension" , new String[]{codeExp});
					resultList.add(msg);
					return toJson(resultList);
				}

				if (appendFileFlag) {
					list = fileService.uploadFile(req, file, null, fileId, true, filePath, false);
				} else {
					list = fileService.uploadFile(req, file, null, fileId, true, filePath);
				}
			}
			
			//결과값 resultList에 담기
			resultList.add(list);
			
			// 20210625_fileUpload 시 projectMaster table save_START
			String registFileId = list.get(0).getRegistSeq();
			if (appendFileFlag) {
				registFileId = list.get(0).getRegistFileId();
			}
			verificationService.setUploadFileSave(prjId, fileSeq, registFileId);
			// 20210625_fileUpload 시 projectMaster table save_END
			
			return toJson(resultList);
		} else {
			resultList.add("NOT_PERMISSION");
			String msg = getMessage("msg.project.check.division.permissions" , new String[]{"file upload"});
			resultList.add(msg);
			return toJson(resultList);
		}
	}
	
	@PostMapping(value=VERIFICATION.UPLOAD_VERIFICATION,  produces = {
				MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
				MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public @ResponseBody ResponseEntity<Object> uploadVerification(File file, MultipartHttpServletRequest req, HttpServletRequest request, HttpServletResponse res, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/uploadVerification");
		Project projectMaster = new Project();
		projectMaster.setPrjId(req.getParameter("prjId"));
		projectMaster.setNoticeType(verificationService.selectOssNoticeOne2(projectMaster.getPrjId()).getNoticeType());
		List<OssComponents> list = verificationService.getVerifyOssList(projectMaster);
		list = verificationService.setMergeGridData(list);
		
		//엑셀 분석
		List<OssComponents> verificationList = ExcelUtil.getVerificationList(req, list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
		
		if (verificationList == null) {
			return makeJsonResponseHeader(false, "");
		}
		
		return makeJsonResponseHeader(true, "", verificationList);
	}
	
	@SuppressWarnings("unchecked")
	@ResponseBody
	@PostMapping(value=VERIFICATION.VERIFY)
	public String procVerify(@RequestBody Map<Object, Object> map, T2File file, Project project,
			HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		log.info("URI: "+ "/project/verification/verify");
		
		Map<String, Object> resMap = null;
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		
		List<String> fileSeqs =	(List<String>) map.get("fileSeqs");
		String prjId = (String) map.get("prjId");
		
		try {
			
			log.info("start verify prjId:" + prjId + ", fileSeqs:" + fileSeqs.toString());
			
			String packagingComment = fileService.setClearFiles(map);
			map.put("packagingComment", packagingComment);
			
			boolean isChangedPackageFile = verificationService.getChangedPackageFile(prjId, fileSeqs);
			int seq = 1;
			
			for (String fileSeq : fileSeqs){
				map.put("fileSeq", fileSeq);
				map.put("packagingFileIdx", seq++);
				map.put("isChangedPackageFile", isChangedPackageFile);
				result.add(verificationService.processVerification(map, file, project));
			}
			
			resMap = result.get(0);
			
			Map<String, Object> fileCountsMap = new HashMap<>();
			List<String> verifyValidChkList = new ArrayList<>();
			
			if (fileSeqs.size() > 1){
				for (Map<String, Object> resultMap : result) {
					if (resultMap.containsKey("fileCounts")) {
						Map<String, Object> fileCountMap = (Map<String, Object>) resultMap.get("fileCounts");
						for (String key : fileCountMap.keySet()) {
							fileCountsMap.put(key, fileCountMap.get(key));
						}
					}
				}
				
				for (Map<String, Object> resultMap : result) {
					if (resultMap.containsKey("verifyValid")) {
						List<String> verifyValidList = (List<String>) resultMap.get("verifyValid");
						for (String verifyValid : verifyValidList) {
							if (!fileCountsMap.containsKey(verifyValid)) {
								verifyValidChkList.add(verifyValid);
							}
						}
					}
				}
				
				verifyValidChkList = verifyValidChkList.stream().distinct().collect(Collectors.toList());
			} else {
				fileCountsMap = (Map<String, Object>) resMap.get("fileCounts");
				
				if (resMap.containsKey("verifyValid")) {
					List<String> verifyValidList = (List<String>) resMap.get("verifyValid");
					for (String verifyValid : verifyValidList) {
						if (!fileCountsMap.containsKey(verifyValid)) {
							verifyValidChkList.add(verifyValid);
						}
					}
				}
			}
			
			verificationService.updateVerifyFileCount(fileCountsMap);
			verificationService.updateVerifyFileCountReset(verifyValidChkList);
			
			resMap.put("verifyValid", verifyValidChkList);
			resMap.put("fileCounts", fileCountsMap);
		} catch (Exception e) {
			log.error("failed to verify project id:" + prjId, e);
			
			resMap = new HashMap<String, Object>();
			resMap.put("resCd", "99");
			resMap.put("resMsg", "Error : " + ( (StringUtil.isEmpty(e.getMessage()) || "null".equalsIgnoreCase(e.getMessage())) ? getMessage("msg.common.valid2") : e.getMessage()));
		}
		
		return toJson(resMap);
	}
	
	@PostMapping(value=VERIFICATION.SAVE_PATH,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public @ResponseBody ResponseEntity<Object> savePath(@RequestBody Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/savePath");
		
		try {
			verificationService.savePath(map);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader();
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = VERIFICATION.NOTICE_AJAX)
	public @ResponseBody ResponseEntity<Object>  getNoticeHtml(HttpServletRequest req,HttpServletResponse res, Model model,	//
			@RequestParam(value="confirm", defaultValue="")String confirm, //
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			@RequestParam(value="allowDownloadNoticeHTMLYn", defaultValue="")String allowDownloadNoticeHTMLYn, //
			@RequestParam(value="allowDownloadNoticeTextYn", defaultValue="")String allowDownloadNoticeTextYn, //
			@RequestParam(value="allowDownloadSimpleHTMLYn", defaultValue="")String allowDownloadSimpleHTMLYn, //
			@RequestParam(value="allowDownloadSimpleTextYn", defaultValue="")String allowDownloadSimpleTextYn, //
			@RequestParam(value="allowDownloadSPDXSheetYn", defaultValue="")String allowDownloadSPDXSheetYn, //
			@RequestParam(value="allowDownloadSPDXRdfYn", defaultValue="")String allowDownloadSPDXRdfYn, //
			@RequestParam(value="allowDownloadSPDXTagYn", defaultValue="")String allowDownloadSPDXTagYn, //
			@RequestParam(value="allowDownloadSPDXJsonYn", defaultValue="")String allowDownloadSPDXJsonYn, //
			@RequestParam(value="allowDownloadSPDXYamlYn", defaultValue="")String allowDownloadSPDXYamlYn, //
			@RequestParam(value="allowDownloadCDXJsonYn", defaultValue="")String allowDownloadCDXJsonYn, //
			@RequestParam(value="allowDownloadCDXXmlYn", defaultValue="")String allowDownloadCDXXmlYn, //
			OssNotice ossNotice	//
			) throws IOException {
		log.info("URI: "+ "/project/verification/noticeAjax");
		log.debug("PARAM: "+ "confirm="+confirm);
		log.debug("PARAM: "+ "prjId="+ossNotice.getPrjId());
		log.debug("PARAM: "+ "useCustomNoticeYn="+useCustomNoticeYn);
		
		String resultHtml = "";
		
		try {
			ossNotice.setDomain(CommonFunction.getDomain(req)); // domain Setting
			
			Project prjMasterInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			log.debug("PARAM: "+ "noticeFileId="+noticeFileId);
			
			if ("conf".equals(confirm)){
				boolean ignoreMailSend = false;
				
				Map<String, Object> checkMap = verificationService.checkNoticeHtmlInfo(ossNotice);
				if (!(boolean) checkMap.get("isValid")) {
					return makeJsonResponseHeader((boolean) checkMap.get("isValid"), null, (List<String>) checkMap.get("data"));
				}
				
				String userComment = ossNotice.getUserComment();

				//파일 만들기
				if (isEmpty(noticeFileId) || !CoConstDef.FLAG_YES.equals(useCustomNoticeYn)) {
					if (!verificationService.getNoticeHtmlFile(ossNotice)) {
						return makeJsonResponseHeader(false, getMessage("msg.common.valid2"));
					}
				}
				
				// reject 이후에 다시 confirm 하는 경우 이전의 package 파일 삭제 처리 필요 여부를 위해
				// source code obligation 체크를 한다.
				boolean needResetPackageFile = false;
				
				{
					if (!isEmpty(prjMasterInfo.getPackageFileId())) {
						List<OssComponents> list = verificationService.getVerifyOssList(prjMasterInfo);
						needResetPackageFile = list.isEmpty();
					}
				}

				Project prjInfo = null;
				//프로젝트 상태 변경
				Project project = new Project();
				project.setPrjId(ossNotice.getPrjId());
				//다운로드 허용 플래그
				List<String> noticeFileFormatList = Arrays.asList(ossNotice.getNoticeFileFormat());
				projectService.setNoticeFileFormat(project, noticeFileFormatList);
				
//				project.setAllowDownloadNoticeHTMLYn(allowDownloadNoticeHTMLYn);
//				project.setAllowDownloadNoticeTextYn(allowDownloadNoticeTextYn);
//				project.setAllowDownloadSimpleHTMLYn(allowDownloadSimpleHTMLYn);
//				project.setAllowDownloadSimpleTextYn(allowDownloadSimpleTextYn);
//				project.setAllowDownloadSPDXSheetYn(allowDownloadSPDXSheetYn);
//				project.setAllowDownloadSPDXRdfYn(allowDownloadSPDXRdfYn);
//				project.setAllowDownloadSPDXTagYn(allowDownloadSPDXTagYn);
//				project.setAllowDownloadSPDXJsonYn(allowDownloadSPDXJsonYn);
//				project.setAllowDownloadSPDXYamlYn(allowDownloadSPDXYamlYn);
//				project.setAllowDownloadCDXJsonYn(allowDownloadCDXJsonYn);
//				project.setAllowDownloadCDXXmlYn(allowDownloadCDXXmlYn);
				
				project.setVerificationStatus(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM);
				
				// 프로젝트 기본정보의 distribution type이 verify까지만인 경우, distribute status를 N/A 처리한다.
				{
					prjInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
					String distributionType = codeMapper.getCodeDetail(CoConstDef.CD_DISTRIBUTION_TYPE, prjInfo.getDistributionType()).getCdDtlExp();
					if ("T".equalsIgnoreCase(avoidNull(distributionType))
							|| (CoConstDef.FLAG_NO.equalsIgnoreCase(avoidNull(distributionType)) && verificationService.checkNetworkServer(ossNotice.getPrjId()))
							|| CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(prjInfo.getDistributeTarget()) // 배포사이트 사용안함으로 설정한 경우
							) {
						project.setDistributionStatus(CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA);
						ignoreMailSend = true;
					} else if (!CoConstDef.CD_DTL_DISTRIBUTE_NA.equals(prjInfo.getDistributeTarget())
							&& CoConstDef.CD_DTL_DISTRIBUTE_STATUS_NA.equals(prjInfo.getDistributionStatus())) {
						project.setResetDistributionStatus(CoConstDef.FLAG_YES);
					}
					
					if (!isEmpty(prjInfo.getDistributionStatus()) 
							&& CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(confirm.toUpperCase())) {
						project.setChangedNoticeYn(CoConstDef.FLAG_YES);
					}
				}
				project.setModifier(project.getLoginUserName());
				
				// 기존에 등록한 package file의 삭제
				if (needResetPackageFile) {
					project.setNeedPackageFileReset(CoConstDef.FLAG_YES);
				}
				
				verificationService.updateStatusWithConfirm(project, ossNotice, false);

				try {
					History h = new History();
					h = projectService.work(project);
					h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
					project = (Project) h.gethData();
					h.sethEtc(project.etcStr());
					
					historyService.storeData(h);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				userComment += verificationService.changePackageFileNameCombine(ossNotice.getPrjId());
				try {
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
					commHisBean.setReferenceId(project.getPrjId());
					commHisBean.setContents(userComment);
					commHisBean.setStatus(CoCodeManager.getCodeExpString(CoConstDef.CD_IDENTIFICATION_STATUS, CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM));
					
					commentService.registComment(commHisBean);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				if (prjInfo != null) {
					try {
						String mailTemplateCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF;
						String mailContentCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF;
						if (ignoreMailSend) {
							mailTemplateCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY;
							mailContentCode = mailTemplateCode;
						} else if (CoConstDef.FLAG_YES.equals(prjInfo.getAndroidFlag())) {
							mailTemplateCode = CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY;
						}
						CoMail mailBean = new CoMail(mailTemplateCode);
						mailBean.setParamPrjId(ossNotice.getPrjId());
						String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, mailContentCode));
						userComment = avoidNull(userComment) + "<br />" + _tempComment;

						if (!isEmpty(userComment)) {
							mailBean.setComment(userComment);
						}

						CoMailManager.getInstance().sendMail(mailBean);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}

			} else { // preview 인 경우
				// 저장된 고지문구가 없을 경우
				if (isEmpty(noticeFileId) || !CoConstDef.FLAG_YES.equals(useCustomNoticeYn)) {
					resultHtml = verificationService.getNoticeHtml(ossNotice);
				} else { // 저장된 고지문구가 있을 경우
					T2File fileInfo = fileService.selectFileInfo(noticeFileId);
					resultHtml = CommonFunction.getStringFromFile(fileInfo.getLogiPath() + "/" + fileInfo.getLogiNm());
					// 파일을 못찾을 경우 예외처리
					if (isEmpty(resultHtml)) {
						resultHtml = verificationService.getNoticeHtml(ossNotice);
					}
				}
			}			
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(true, null, resultHtml);
	}

	@ResponseBody
	@GetMapping(value = VERIFICATION.DOWNLOAD_FILE,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public ResponseEntity<FileSystemResource> downloadFile (
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/downloadFile");
		
		String prjId = req.getParameter("prjId");
		String fileName = req.getParameter("fileNm");
		String filePath = CommonFunction.emptyCheckProperty("verify.output.path", "/verify")+"/"+prjId+"/"+fileName;

		return noticeToResponseEntity(filePath, fileName);
	}
	
	@ResponseBody
	@PostMapping(value=VERIFICATION.WGET_URL,  produces = {
					MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
					MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public String wgetUrl(@RequestBody Map<Object, Object> map, T2File file, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {
		log.info("URI: "+ "/project/verification/wgetUrl");
		
		//파일 등록
		List<UploadFile> list = new ArrayList<UploadFile>();
		ArrayList<Object> resultList = new ArrayList<Object>();
		String fileId = "";

		file.setCreator(loginUserName());
		map.put("filePath", "");
		
		if (StringUtil.isEmpty(fileId)){
			list = fileService.uploadWgetFile(request, file, map, false);
		}
		
		log.debug("WgetResult ==>"+list.get(0).getWgetResult());
		
		//결과값 resultList에 담기
		resultList.add(list);
		
		return toJson(resultList);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value=VERIFICATION.SAVE_AJAX)
	public @ResponseBody ResponseEntity<Object> registVerify(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			@RequestParam(value="allowDownloadNoticeHTMLYn", defaultValue="")String allowDownloadNoticeHTMLYn, //
			@RequestParam(value="allowDownloadNoticeTextYn", defaultValue="")String allowDownloadNoticeTextYn, //
			@RequestParam(value="allowDownloadSimpleHTMLYn", defaultValue="")String allowDownloadSimpleHTMLYn, //
			@RequestParam(value="allowDownloadSimpleTextYn", defaultValue="")String allowDownloadSimpleTextYn, //
			@RequestParam(value="allowDownloadSPDXSheetYn", defaultValue="")String allowDownloadSPDXSheetYn, //
			@RequestParam(value="allowDownloadSPDXRdfYn", defaultValue="")String allowDownloadSPDXRdfYn, //
			@RequestParam(value="allowDownloadSPDXTagYn", defaultValue="")String allowDownloadSPDXTagYn, //
			@RequestParam(value="allowDownloadSPDXJsonYn", defaultValue="")String allowDownloadSPDXJsonYn, //
			@RequestParam(value="allowDownloadSPDXYamlYn", defaultValue="")String allowDownloadSPDXYamlYn, //
			@RequestParam(value="allowDownloadCDXJsonYn", defaultValue="")String allowDownloadCDXJsonYn, //
			@RequestParam(value="allowDownloadCDXXmlYn", defaultValue="")String allowDownloadCDXXmlYn, //
			HttpServletResponse res, Model model) throws Exception {
		log.info("URI: "+ "/project/verification/saveAjax");
		
		T2CoValidationResult vResult = null;
		
		/*Json String -> Json Object*/
		String jsonString = ossNotice.getPackageJson();
		Type collectionType = new TypeToken<List<OssComponents>>(){}.getType();
		List<OssComponents> list = (List<OssComponents>) fromJson(jsonString, collectionType);
		ossNotice.setOssComponents(list);
		
		T2CoProjectValidator pv = new T2CoProjectValidator();
		pv.setProcType(pv.PROC_TYPE_VERIFIY);
		Map<String, String> reqMap = new HashMap<>();
		Map<String, String> keyPreMap = new HashMap<>();
		
		keyPreMap.put("COMPANY_NAME_FULL"			, "PACKAGING");
		keyPreMap.put("COMPANY_NAME_SHORT"			, "PACKAGING");
		keyPreMap.put("DISTRIBUTION_SITE_URL"		, "PACKAGING");
		keyPreMap.put("EMAIL"						, "PACKAGING");
		keyPreMap.put("USE_COMPANY_NAME_TITLE"		, "PACKAGING");
		keyPreMap.put("DISTRIBUTED_OTHER_COMPANY"	, "PACKAGING");
		keyPreMap.put("MERGED_OTHER_OSS_NOTICE"		, "PACKAGING");
		keyPreMap.put("ACCOMPANIED_SOURCE_CODE"		, "PACKAGING");
		
		vResult = pv.validateRequest(reqMap, req, keyPreMap);
		
		
		if (!vResult.isValid()){
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		verificationService.registOssNotice(ossNotice);
		
		Project project = new Project();
		project.setPrjId(ossNotice.getPrjId());
		
		// notice 수정시에만 변경값을 저장, 수정안한 defualt에는 allowDownloadNoticeHTMLYn만 값 입력
		List<String> noticeFileFormatList = Arrays.asList(ossNotice.getNoticeFileFormat());
		projectService.setNoticeFileFormat(project, noticeFileFormatList);
		
		verificationService.updateProjectAllowDownloadBitFlag(project);
		
		return makeJsonResponseHeader(vResult.getValidMessageMap());
	}
	
	@PostMapping(value = VERIFICATION.SAVE_NOTICE_AJAX)
	public @ResponseBody ResponseEntity<Object>  saveNoticeHtml(HttpServletRequest req,HttpServletResponse res, Model model,	//
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			@RequestParam(value="noticeHtml", defaultValue="")String noticeHtml, //
			OssNotice ossNotice	//
			) throws IOException {
		log.info("URI: "+ "/project/verification/saveNoticeAjax");
		log.debug("PARAM: "+ "prjId="+ossNotice.getPrjId());
		log.debug("PARAM: "+ "useCustomNoticeYn="+useCustomNoticeYn);
		
		if (!verificationService.getNoticeHtmlFile(ossNotice, noticeHtml) || !CoConstDef.FLAG_YES.equals(useCustomNoticeYn)) {
			return makeJsonResponseHeader(false, "Notice Registration Failed");
		}
		
		return makeJsonResponseHeader();
	}
	
	@PostMapping(value = VERIFICATION.MAKE_NOTICE_PREVIEW)
	public @ResponseBody ResponseEntity<Object>  makeNoticePreview(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {
		log.info("URI: "+ "/project/verification/makeNoticePreview");
		
		String downloadId = null;
		ossNotice.setFileType("html");
		
		try {	
			Project prjMasterInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			log.debug("PARAM: "+ "noticeFileId="+noticeFileId);
			log.debug("PARAM: "+ "useCustomNoticeYn="+useCustomNoticeYn);
			
			if (isEmpty(noticeFileId)) {
				downloadId = verificationService.getNoticeHtmlFileForPreview(ossNotice);
			} else {
				downloadId = noticeFileId;
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}
	
	@ResponseBody
	@GetMapping(value = VERIFICATION.DOWNLOAD_NOTICE_PREVIEW,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public ResponseEntity<FileSystemResource> downloadNoticePreview (
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/downloadNoticePreview");
		
		T2File fileInfo = fileService.selectFileInfo(req.getParameter("id"));
		
		return noticeToResponseEntity(fileInfo.getLogiPath() + "/" + fileInfo.getLogiNm(), fileInfo.getOrigNm());
	}
	
	@PostMapping(value = VERIFICATION.MAKE_NOTICE_TEXT)
	public @ResponseBody ResponseEntity<Object>  makeNoticeText(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {
		log.info("URI: "+ "/project/verification/makeNoticeText");
		
		String downloadId = null;
		ossNotice.setFileType("text");
		
		try {
			Project prjMasterInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			log.debug("PARAM: "+ "noticeFileId="+noticeFileId);
			log.debug("PARAM: "+ "useCustomNoticeYn="+useCustomNoticeYn);
			
			if (!isEmpty(prjMasterInfo.getNoticeTextFileId())) {
				downloadId = prjMasterInfo.getNoticeTextFileId();
			} else {
				downloadId = verificationService.getNoticeTextFileForPreview(ossNotice);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}
	
	@PostMapping(value = VERIFICATION.MAKE_NOTICE_SIMPLE)
	public @ResponseBody ResponseEntity<Object>  makeNoticeSimple(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {
		log.info("URI: "+ "/project/verification/makeNoticeSimple");
		
		String downloadId = null;
		ossNotice.setFileType("html");
		ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES); // simple templete
		ossNotice.setDomain(CommonFunction.getDomain(req)); // domain Setting
		
		try {
			Project prjMasterInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			log.debug("PARAM: "+ "noticeFileId="+noticeFileId);
			log.debug("PARAM: "+ "useCustomNoticeYn="+useCustomNoticeYn);
			
			if (!isEmpty(prjMasterInfo.getSimpleHtmlFileId())) {
				downloadId = prjMasterInfo.getSimpleHtmlFileId();
			} else {
				downloadId = verificationService.getNoticeTextFileForPreview(ossNotice);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}
	
	@PostMapping(value = VERIFICATION.MAKE_NOTICE_TEXT_SIMPLE)
	public @ResponseBody ResponseEntity<Object>  makeNoticeTextSimple(OssNotice ossNotice, HttpServletRequest req,
			@RequestParam(value="useCustomNoticeYn", defaultValue="")String useCustomNoticeYn, //
			HttpServletResponse res, Model model) throws IOException {
		log.info("URI: "+ "/project/verification/makeNoticeTextSimple");
		
		String downloadId = null;
		ossNotice.setFileType("text");
		ossNotice.setSimpleNoticeFlag(CoConstDef.FLAG_YES); // simple templete
		
		try {
			Project prjMasterInfo = projectService.getProjectBasicInfo(ossNotice.getPrjId());
			String noticeFileId = prjMasterInfo.getNoticeFileId();
			log.debug("PARAM: "+ "noticeFileId="+noticeFileId);
			log.debug("PARAM: "+ "useCustomNoticeYn="+useCustomNoticeYn);
			
			if (!isEmpty(prjMasterInfo.getSimpleTextFileId())) {
				downloadId = prjMasterInfo.getSimpleTextFileId();
			} else {
				downloadId = verificationService.getNoticeTextFileForPreview(ossNotice);
			}
		} catch (Exception e) {
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader(downloadId);
	}
	
	@GetMapping(value = VERIFICATION.REUSE_PROJECT_SEARCH)
	public @ResponseBody ResponseEntity<Object> reuseProjectSearch(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		
		project.setCurPage(page);
		project.setPageListSize(rows);
		project.setSortField(sidx);
		project.setSortOrder(sord);

		Map<String, Object> map = verificationService.getReuseProject(project);

		return makeJsonResponseHeader(map);
	}
	
	@GetMapping(value = VERIFICATION.REUSE_PROJECT_PACKAGING_SEARCH)
	public @ResponseBody ResponseEntity<Object> reuseProjectPackagingSearch(Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		Map<String, Object> map = verificationService.getReuseProjectPackagingFile(project);

		return makeJsonResponseHeader(map);
	}
	
	@PostMapping(value=VERIFICATION.REUSE_PACKAGING_FILE, produces = "text/html; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> reusePackagingFile(@RequestBody HashMap<String, Object> map, HttpServletRequest request,
			HttpServletResponse res, Model model) throws Exception {
		log.info("URI: "+ "/project/verification/reusePackagingFile");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String refFileSeq = (String) map.get("refFileSeq");
		List<UploadFile> file = fileService.setReusePackagingFile(refFileSeq);
		
		map.put("fileSeq", file.get(0).getRegistSeq());
		
		boolean result = verificationService.setReusePackagingFile(map);
		
		if (result) {
			resultMap.put("file", file);
			
			return makeJsonResponseHeader(resultMap);
		} else {
			resultMap.put("file", "false");
			
			return makeJsonResponseHeader(map);
		}
	}
	
	@ResponseBody
	@GetMapping(value = VERIFICATION.DOWNLOAD_PACKAGE,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public ResponseEntity<FileSystemResource> downloadPackage(
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/downloadPackage");
		
		ResponseEntity<FileSystemResource> result = null;
		String prjId = req.getParameter("prjId");
		
		try {
			result = verificationService.getPackage(prjId, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return result;
	}
	
	@ResponseBody
	@GetMapping(value = {VERIFICATION.DOWNLOAD_PACKAGING_MULTI})
	public ResponseEntity<FileSystemResource> downloadPackageMulti(
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/downloadPackage");
		
		ResponseEntity<FileSystemResource> result = null;
		String prjId = req.getParameter("prjId");
		String fileIdx = req.getParameter("fileIdx");
		
		try {
			result = verificationService.getPackageMulti(prjId, CommonFunction.emptyCheckProperty("upload.path", "/upload"), fileIdx);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return result;
	}
	
	@ResponseBody
	@GetMapping(value = VERIFICATION.DOWNLOAD_NOTICE,  produces = {
			MimeTypeUtils.TEXT_HTML_VALUE+"; charset=utf-8", 
			MimeTypeUtils.APPLICATION_JSON_VALUE+"; charset=utf-8"})
	public ResponseEntity<FileSystemResource> downloadNotice(
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/downloadNotice");
		
		ResponseEntity<FileSystemResource> result = null;
		String prjId = req.getParameter("prjId");
		result = verificationService.getNotice(prjId, CommonFunction.emptyCheckProperty("notice.path", "/notice"));

		return result;
	}

	@ResponseBody
	@GetMapping(value = VERIFICATION.DOWNLOAD_REVIEW_REPORT)
	public ResponseEntity<FileSystemResource> downloadReviewReport(
			HttpServletRequest req,
			HttpServletResponse res, Model model) throws Exception {
		log.info("URI: " + "/project/verification/downloadReviewReport");

		ResponseEntity<FileSystemResource> result = null;
		String prjId = req.getParameter("prjId");
		result = verificationService.getReviewReport(prjId, CommonFunction.emptyCheckProperty("reviewReport.path", "/reviewReport"));

		return result;
	}
	
	@PostMapping(value=VERIFICATION.SEND_COMMENT)
	public @ResponseBody ResponseEntity<Object> sendComment(
			@ModelAttribute CommentsHistory commentsHistory
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		log.info("URI: "+ "/project/verification/sendComment");
		
		T2CoValidationResult vResult = null;
		
		try{
			//validation check
			 vResult = validate(req);	
		}catch(Exception e){
			log.error(e.getMessage());
		}
		
		if (!vResult.isValid()){
			return makeJsonResponseHeader(vResult.getValidMessageMap());
		}
		
		try{
			commentService.deleteComment(commentsHistory);
			
			commentsHistory.setCommId("");
			
			commentService.registComment(commentsHistory);
			verificationMapper.deleteComment(commentsHistory);
		} catch (Exception e){
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(vResult.getValidMessageMap());
	}
	
	@PostMapping(value=VERIFICATION.DELETE_FILE)
	public @ResponseBody ResponseEntity<Object> deleteFile (@RequestBody Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/deleteFile");
		
		try {
			verificationService.deleteFile(map);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		return makeJsonResponseHeader();
	}

	@PostMapping(value=VERIFICATION.DEFAULT_NOTICE_INFO)
	public @ResponseBody ResponseEntity<Object> defaultNoticeInfo (@RequestBody Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response, Model model) throws Exception{
		log.info("URI: "+ "/project/verification/getDefaultNoticeInfo");
		
		OssNotice _noticeInfo = null;
		
		try {
			Project param = new Project();
			param.setPrjId((String) map.get("prjId"));
			_noticeInfo = projectService.setCheckNotice(param);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return makeJsonResponseHeader(false, e.getMessage());
		}
		
		if (_noticeInfo != null) {
			return makeJsonResponseHeader(true, "true", _noticeInfo);
		} else {
			return makeJsonResponseHeader(false, "false");
		}
	}
}
