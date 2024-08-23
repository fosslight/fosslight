/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.*;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.NoticeMapper;
import oss.fosslight.service.*;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.util.StringUtil;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.api.validator.ValuesAllowed;
import oss.fosslight.validation.custom.T2CoProjectValidator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Api(tags = {"3. Project"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
@Validated
public class ApiProjectV2Controller extends CoTopComponent {

    @Resource
    private Environment env;
    private String RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX;

    @PostConstruct
    public void setResourcePathPrefix() {
        RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX = CommonFunction.emptyCheckProperty("export.template.path", "/template");
    }

    private boolean ldapCheckFlag = CoConstDef.FLAG_YES.equals(avoidNull(CommonFunction.getProperty("ldap.check.flag"))) ? true : false;

    private final RestResponseService responseService;

    private final T2UserService userService;

    private final ApiProjectService apiProjectService;

    private final FileService fileService;

    private final ApiFileService apiFileService;

    private final CommentService commentService;

    private final ProjectService projectService;

    private final HistoryService historyService;

    private final VerificationService verificationService;

    private final CodeMapper codeMapper;

    private final NoticeMapper noticeMapper;

    protected static final Logger log = LoggerFactory.getLogger("DEFAULT_LOG");

    @ApiOperation(value = "Search Project List", notes = "Project 정보 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_SEARCH})
    public ResponseEntity<Map<String, Object>> selectProjectList(
            @RequestHeader String authorization,
            @ApiParam(value = "Project Name", required = false) @RequestParam(required = false) String prjName,
            @ApiParam(value = "Project Name exact match (Y: true, N: false)", allowableValues = "Y,N", required = false) @RequestParam(required = false, defaultValue = "N") String prjNameExactYn,
            @ApiParam(value = "project ID List", required = false) @RequestParam(required = false) String[] prjIdList,
            @ApiParam(value = "Division (\"Check the input value with /api/v2/codes\")", required = false) @RequestParam(required = false) String division,
            @ApiParam(value = "Model Name", required = false) @RequestParam(required = false) String modelName,
            @ApiParam(value = "Create Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String createDate,
            @ApiParam(value = "Status (PROG:progress, REQ:Request, REV:Review, COMP:Complete, DROP:Drop)", required = false, allowableValues = "PROG,REQ,REV,COMP,DROP") @RequestParam(required = false) String status,
            @ApiParam(value = "Update Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String updateDate,
            @ApiParam(value = "Creator", required = false) @RequestParam(required = false) String creator) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        try {
            CommonFunction.splitDate(createDate, paramMap, "-", "createDate");
            CommonFunction.splitDate(updateDate, paramMap, "-", "updateDate");

//			paramMap.put("userRole", userInfo.getAuthority());
            paramMap.put("creator", creator);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("division", division);
            paramMap.put("modelName", modelName);
            paramMap.put("status", status);
            paramMap.put("prjIdList", prjIdList);
            paramMap.put("prjName", prjName);
            paramMap.put("prjNameExactYn", prjNameExactYn);

            try {
                resultMap = apiProjectService.selectProjectList(paramMap);
            } catch (Exception e) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Search Project List", notes = "Project 정보 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_MODEL_SEARCH})
    public ResponseEntity<Map<String, Object>> selectModelList(
            @RequestHeader String authorization,
            @ApiParam(value = "project ID List", required = true) @RequestParam(required = true) String[] prjIdList) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            try {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("prjIdList", prjIdList);

                resultMap = apiProjectService.selectModelList(paramMap);
            } catch (Exception e) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Update model list of project", notes = "Basic Information > Model list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_MODEL_UPDATE})
    public ResponseEntity<Map<String, Object>> updateModelList(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(required = true, name = "id") String prjId,
            @ApiParam(
                    value = "Model List, in format of: ${MODEL_NAME}|${CATEGORY}|${yyyyMMdd} (ex. MODEL_NAME|ETC > Etc|20220428)",
                    required = true
            )
            @RequestParam(required = true)
            String[] modelListToUpdate
    ) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, List<Project>> modelList = null;

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("ossReportFlag", CoConstDef.FLAG_NO);
            paramMap.put("readOnly", CoConstDef.FLAG_NO);

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
            if (searchFlag) {
                Project project = projectService.getProjectBasicInfo(prjId);
                if (modelListToUpdate != null) {
                    List<String[]> models = new ArrayList<>();
                    for (String strModel : modelListToUpdate) {
                        String[] model = strModel.replaceAll("\"", "").split("\\|");
                        if (model.length > 2) {
                            models.add(model);
                        }
                    }
                    if (models.size() > 0) {
                        modelList = ExcelUtil.readModelFromList(models, prjId, CoConstDef.FLAG_YES, "0", project.getDistributeTarget());
                    }
                }

                if (modelList != null) {
                    project.setModelList(modelList.get("currentModelList"));
                    projectService.insertProjectModel(project);
                    return new ResponseEntity<>(resultMap, HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
//			errorCode = CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE;
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(value = "Update model list of project with file", notes = "Basic Information > Model list with file")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_MODEL_UPDATE_UPLOAD_FILE})
    public ResponseEntity<Map<String, Object>> updateModelListUploadFile(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Model List (Spread sheet)", required = false) @RequestPart(required = false) MultipartFile modelReport) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, List<Project>> modelList = null;

        if (modelReport == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                List<String> prjIdList = new ArrayList<String>();
                prjIdList.add(prjId);
                paramMap.put("userId", userInfo.getUserId());
                paramMap.put("userRole", userRole(userInfo));
                paramMap.put("prjId", prjIdList);
                paramMap.put("ossReportFlag", CoConstDef.FLAG_NO);
                paramMap.put("readOnly", CoConstDef.FLAG_NO);

                boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
                if (!searchFlag) {
                    return responseService.errorResponse(HttpStatus.FORBIDDEN);
                }
                Project project = projectService.getProjectBasicInfo(prjId);
                if (modelReport != null) {
                    if (modelReport.getOriginalFilename().contains("xls") // Allowed file extension: xls, xlsx, xlsm
                            && CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > modelReport.getSize()) { // Max file size :5MB
                        modelList = ExcelUtil.getModelList(modelReport, CommonFunction.emptyCheckProperty("upload.path", "/upload"),
                                project.getDistributeTarget(), prjId, CoConstDef.FLAG_YES, "0");
                    } else {
                        return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE);
                    }
                }

                if (modelList != null) {
                    project.setModelList(modelList.get("currentModelList"));
                    projectService.insertProjectModel(project);
                    return ResponseEntity.ok(resultMap);
                }
            } catch (IndexOutOfBoundsException e) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Error while parsing given file");
            } catch (Exception e) {
                log.error(e.getMessage());
                return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return responseService.errorResponse(HttpStatus.BAD_REQUEST);
//		return responseService.getFailResult(errorCode, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, errorCode));
    }

    @ApiOperation(value = "Create Project", notes = "project 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_CREATE})
    public ResponseEntity<Map<String, Object>> createProject(
            @RequestHeader String authorization,
            @ApiParam(value = "Project Name (Duplicate not allowed)", required = true) @RequestParam(required = true) String prjName,
            @ApiParam(value = "Project Version", required = false) @RequestParam(required = false) String prjVersion,
            @ApiParam(value = "OS Type (\"Check the input value with /api/v2/codes\")", required = true) @RequestParam(required = true) String osType,
            @ApiParam(value = "OS Type etc", required = false) @RequestParam(required = false) String osTypeEtc,
            @ApiParam(value = "Distribution Type (\"Check the input value with /api/v2/codes\")", required = false) @RequestParam(required = false) String distributionType,
            @ApiParam(value = "Distribution Site (\"Check the input value with /api/v2/codes\")", required = false) @RequestParam(required = false) String distributionSite,
            @ApiParam(value = "Network Service (YES : Y, NO : N)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String networkServerType,
            @ApiParam(value = "OSS Notice (\"Check the input value with /api/v2/codes\")", required = false) @RequestParam(required = false) String noticeType,
            @ApiParam(value = "Notice Platform (\"Check the input value with /api/v2/codes\")", required = false) @RequestParam(required = false) String noticeTypeEtc,
            @ApiParam(value = "Priority (\"Check the input value with /api/v2/codes\")", required = false) @RequestParam(required = false) String priority,
            @ApiParam(value = "Visible to everyone? (YES : Y, NO : N)", required = false, allowableValues = "Y,N") @RequestParam(required = false, defaultValue = "Y") String publicYn,
            @ApiParam(value = "User Comment", required = false) @RequestParam(required = false) String userComment,
            @ApiParam(value = "Additional Information", required = false) @RequestParam(required = false) String additionalInformation) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        int createCnt = apiProjectService.getCreateProjectCnt(userInfo.getUserId());
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            if (CoConstDef.CD_OPEN_API_CREATE_PROJECT_LIMIT <= createCnt) {
                return responseService.errorResponse(HttpStatus.TOO_MANY_REQUESTS,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_CREATE_OVERFLOW_MESSAGE));
            }
            Map<String, Object> paramMap = new HashMap<String, Object>();

            String osTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, osType);

            if (isEmpty(osTypeStr)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Valid OS type code is required.");
            }

            if (!isEmpty(distributionType)) {
                String distributionTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, distributionType);

                if (isEmpty(distributionTypeStr)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Valid distribution type code is invalid.");
                }
            } else {
                distributionType = CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL;
            }

            if (!isEmpty(distributionSite)) {
                String distributionSiteStr = CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_CODE, distributionSite);

                if (isEmpty(distributionSiteStr)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Valid distribution site type code is invalid.");
                }
            } else {
                distributionSite = CoConstDef.CD_DTL_DISTRIBUTE_LGE;
            }


            if (!isEmpty(networkServerType)) {
                if (!CoConstDef.FLAG_YES.equals(networkServerType)
                        && !CoConstDef.FLAG_NO.equals(networkServerType)) { // NETWORK Service Only는 Y / N만 선택 가능함.

                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, String.format("Network server type parameter must be either %s or %s.", CoConstDef.FLAG_YES, CoConstDef.FLAG_NO));
                }
            } else {
                networkServerType = CoConstDef.FLAG_NO;
            }

            if (isEmpty(noticeType)) {
                if (!isEmpty(noticeTypeEtc)) {
                    String noticeTypeEtcStr = CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, noticeTypeEtc);

                    if (!isEmpty(noticeTypeEtcStr)) {
                        noticeType = CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED;
                    } else if (isEmpty(noticeTypeEtcStr)) {
                        return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Notice type etc code is invalid.");
                    }
                } else {
                    noticeType = CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL;
                }
            } else if (!isEmpty(noticeType)) {
                String noticeTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, noticeType);

                if (isEmpty(noticeTypeStr)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Notice type code is invalid.");
                }
            }

            if (!CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(noticeType)) {
                noticeTypeEtc = "";
            } else if (CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(noticeType) && isEmpty(noticeTypeEtc)) {
                noticeTypeEtc = CoConstDef.CD_DTL_DEFAULT_PLATFORM;
            } else if (!isEmpty(noticeTypeEtc)) {
                String noticeTypeEtcStr = CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, noticeTypeEtc);

                if (!isEmpty(noticeTypeEtcStr)) {
                    noticeTypeEtc = noticeTypeEtcStr;
                } else if (isEmpty(noticeTypeEtcStr)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Notice type etc code is invalid.");
                }
            }

            if (!isEmpty(priority)) {
                String priorityStr = CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, priority);

                if (isEmpty(priorityStr)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Priority code is invalid");
                }
            } else {
                priority = CoConstDef.CD_PRIORITY_P2;
            }

            paramMap.put("prjName", prjName);
            paramMap.put("prjVersion", avoidNull(prjVersion, ""));
            paramMap.put("osType", osType);
            paramMap.put("osTypeEtc", osTypeEtc);
            paramMap.put("distributionType", distributionType);
            paramMap.put("distributionSite", distributionSite);
            paramMap.put("networkServerType", networkServerType);
            paramMap.put("priority", priority);
            paramMap.put("loginUserName", userInfo.getUserId());
            paramMap.put("publicYn", publicYn);
            paramMap.put("comment", avoidNull(additionalInformation, ""));

            result = apiProjectService.createProject(paramMap);

            String resultPrjId = (String) result.get("prjId");

            Map<String, Object> noticeParamMap = new HashMap<String, Object>();
            noticeParamMap.put("prjId", resultPrjId);
            noticeParamMap.put("noticeType", noticeType);
            noticeParamMap.put("noticeTypeEtc", noticeTypeEtc);

            int resultCnt = apiProjectService.makeOssNotice(noticeParamMap);

            if (isEmpty(resultPrjId) || resultCnt <= 0) {
                return responseService.errorResponse(HttpStatus.CONFLICT,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_CREATE_PROJECT_DUPLICATE_MESSAGE));
            }
            try {
                History h = new History();
                Project project = new Project();
                project.setPrjId(resultPrjId);
                h = projectService.work(project);
                h.setModifier(userInfo.getUserId());
                h.sethAction(CoConstDef.ACTION_CODE_INSERT);

                historyService.storeData(h);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            if (userComment != null) {
                CommentsHistory commentHisBean = new CommentsHistory();
                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_USER);
                commentHisBean.setReferenceId(resultPrjId);
                commentHisBean.setExpansion1("SRC");
                commentHisBean.setContents(userComment);
                commentService.registComment(commentHisBean, false);
            }

            try {
                CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED);
                mailBean.setParamPrjId(resultPrjId);
                String _tempComment = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED));
                userComment = avoidNull(userComment) + "<br />" + _tempComment;
                mailBean.setComment(userComment);
                mailBean.setLoginUserName(userInfo.getUserId());
                mailBean.setLoginUserRole(userInfo.getAuthority());
                CoMailManager.getInstance().sendMail(mailBean);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "Project Bom Download as File", notes = "Project > Bom tab download as file")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_BOM_DOWNLOAD})
    public ResponseEntity<FileSystemResource> getPrjBomDownload(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Save Flag (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "saveFlag", values={"Y","N"}) @RequestParam(required = false) String saveFlag,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet")
            @ValuesAllowed(propName = "format", values = { "Spreadsheet"}) @RequestParam String format){

        log.info("Project Bom Download as File :: " + prjId + " :: " + saveFlag + " :: " + format);

        // 사용자 인증
        String downloadId = "";
        T2File fileInfo = new T2File();

        try {
            T2Users userInfo = userService.checkApiUserAuth(authorization);
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("ossReportFlag", CoConstDef.FLAG_NO);
            paramMap.put("distributionType", "normal");

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap);

            if (searchFlag) {
                if ("Y".equals(saveFlag)) {
//                    apiProjectService.registBom(prjId, mergeSaveFlag);
                    projectService.registBom(prjId, saveFlag, new ArrayList<>(), new ArrayList<>());
                }
                downloadId = ExcelDownLoadUtil.getExcelDownloadId("bom", prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
                fileInfo = fileService.selectFileInfo(downloadId);
            }

            return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @ApiOperation(value = "Get Project Bom Tab As Json", notes = "Project > Get Bom tab data as json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_BOM_JSON})
    public ResponseEntity<Map<String, Object>> getPrjBomAsJson(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Save Flag (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "saveFlag", values={"Y","N"}) @RequestParam(required = false) String saveFlag){

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);

            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("distributionType", "normal");

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap);

            if (searchFlag) {
                resultMap = apiProjectService.getBomExportJson(prjId);
                if ("Y".equals(saveFlag)) {
                    projectService.registBom(prjId, saveFlag, new ArrayList<>(), new ArrayList<>());
                }
            }
            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Project Bom Compare", notes = "Project > Bom tab Compare")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_BOM_COMPARE})
    public ResponseEntity<Map<String, Object>> getPrjBomCompare(
            @RequestHeader String authorization,
            @ApiParam(value = "Before Project id", required = true) @PathVariable(name = "id", required = true) String beforePrjId,
            @ApiParam(value = "After Project id", required = true) @PathVariable(name = "compareId", required = true) String afterPrjId) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

        try {

            Map<String, Object> paramMap = new HashMap<>();

            if (!isEmpty(beforePrjId) && beforePrjId.equals(afterPrjId)) {
                paramMap.put("status", "same");
                resultMap.put("contents", paramMap);
                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            }

            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(beforePrjId);
            prjIdList.add(afterPrjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("distributionType", "normal");

            int records = apiProjectService.existProjectCntBomCompare(paramMap);

            if (records > 0) {
				List<Map<String, Object>> beforeBomList = new ArrayList<>();
				List<Map<String, Object>> afterBomList = new ArrayList<>();

				Map<String, Object> beforePrjInfo = apiProjectService.getProjectBasicInfo(beforePrjId);
				if (!((String) beforePrjInfo.get("noticeType")).equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
					beforeBomList = apiProjectService.getBomList(beforePrjId);
				} else {
					apiProjectService.getIdentificationGridList(beforePrjId, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, null, null, beforeBomList);
					beforeBomList = apiProjectService.setMergeGridData(beforeBomList);
				}

				Map<String, Object> afterPrjInfo = apiProjectService.getProjectBasicInfo(afterPrjId);
				if (!((String) afterPrjInfo.get("noticeType")).equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
					afterBomList = apiProjectService.getBomList(afterPrjId);
				} else {
					apiProjectService.getIdentificationGridList(afterPrjId, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, null, null, afterBomList);
					afterBomList = apiProjectService.setMergeGridData(afterBomList);
				}

				if (beforeBomList.isEmpty() || afterBomList.isEmpty()) {
					throw new Exception();
				}

                resultMap.put("contents", apiProjectService.getBomCompare(beforeBomList, afterBomList));

                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            } else {
                paramMap.clear();
                paramMap.put("status", "not exist project");
                resultMap.put("contents", paramMap);
                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            }
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Identification OSS Report", notes = "Identification > src > oss report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_OSS_REPORT_SRC})
    public ResponseEntity<Map<String, Object>> ossReportSrc(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "OSS Report > sheetName : all sheets starting with 'SRC'", required = false) @RequestPart(required = false) MultipartFile ossReport,
            @ApiParam(value = "Comment", required = false) @RequestParam(required = false) String comment,
            @ApiParam(value = "Reset Flag (YES : Y, NO : N, Default : Y)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String resetFlag,
            @ApiParam(value = "Sheet Names", required = false) @RequestParam(required = false) String sheetNames) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("ossReportFlag", CoConstDef.FLAG_YES);
            paramMap.put("readOnly", CoConstDef.FLAG_NO);
            paramMap.put("distributionType", "normal");

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.

            if (!searchFlag) {
                return responseService.errorResponse(HttpStatus.FORBIDDEN); // TODO: permission 맞나?
            }

            String oldFileId = "";
            if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
                Map<String, Object> prjInfo = apiProjectService.selectProjectMaster(prjId);
                if (prjInfo.get("srcCsvFileId") != null) {
                    oldFileId = String.valueOf((int) prjInfo.get("srcCsvFileId"));
                }
            }

            if (ossReport == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "ossReport is required.");
            }
            if (!ossReport.getOriginalFilename().contains("xls")) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST);
            }
            if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= ossReport.getSize()) {
                return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
            }

            boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap); // 잘못된  project에 oss report를 upload하려고 할 경우 ex) src -> bin Android
            if (!checkDistributionTypeFlag) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Target project invalid");
            }

            UploadFile bean = null;
            if (!isEmpty(oldFileId)) {
                bean = apiFileService.uploadFile(ossReport, null, oldFileId);
            } else {
                bean = apiFileService.uploadFile(ossReport); // file 등록 처리 이후 upload된 file정보를 return함.
            }

            // get Excel Sheet name starts with SRC
            List<String> sheet = null;
            boolean sheetNamesEmptyFlag = isEmpty(sheetNames) ? true : false;

            try {
                if (sheetNamesEmptyFlag) {
                    sheet = ExcelUtil.getSheetNoStartsWith("SRC", Arrays.asList(bean),
                            CommonFunction.emptyCheckProperty("upload.path", "/upload"));
                } else {
                    List<UploadFile> list = new ArrayList<UploadFile>();
                    list.add(bean);

                    List<Object> sheets = ExcelUtil.getSheetNames(list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
                    boolean createListFlag = false;
                    for (Object obj : sheets) {
                        Map<String, Object> sheetMap = (Map<String, Object>) obj;
                        if (sheetMap.containsKey("name")) {
                            if (!createListFlag) {
                                sheet = new ArrayList<>();
                                createListFlag = true;
                            }
                            sheet.add((String) sheetMap.get("name"));
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            Map<String, Object> result = null;
            if (sheetNamesEmptyFlag) {
                result = apiProjectService.getSheetData(bean, prjId, "SRC", sheet != null ? sheet.toArray(new String[sheet.size()]) : ArrayUtils.EMPTY_STRING_ARRAY);
                resultMap = apiProjectService.getProcessSheetData(result, prjId, resetFlag, bean.getRegistFileId(), userInfo.getUserId(), comment, "SRC", "SRC", sheetNamesEmptyFlag, false, 0);
            } else {
                int sheetLength = sheetNames.split(",").length;
                int sheetIdx = 0;
                for (String sheetNm : sheetNames.split(",")) {
                    if (isEmpty(sheetNm.trim())) continue;
                    result = apiProjectService.getSheetData(bean, prjId, sheetNm.trim(), sheet != null ? sheet.toArray(new String[sheet.size()]) : ArrayUtils.EMPTY_STRING_ARRAY, true);
                    resultMap = apiProjectService.getProcessSheetData(result, prjId, resetFlag, bean.getRegistFileId(), userInfo.getUserId(), comment, "SRC", sheetNm.trim(), sheetNamesEmptyFlag, sheetLength > 1 ? true : false, sheetIdx);
                    sheetIdx++;
                    if (!resultMap.isEmpty()) {
                        break;
                    }
                }
            }

            if (resultMap.isEmpty()) {
                // 정상처리된 경우 세션 삭제
                deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_DEP, prjId));
                deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_DEP, prjId));
                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            } else {
                if (resultMap.containsKey(CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE)) {

                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE));
                } else if (resultMap.containsKey("validError")) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE));
                } else {
                    return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
                }
            }
        } catch (Exception e) {
            // TODO: used to be BAD REQUEST, add case when user's input is wrong
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Identification OSS Report", notes = "Identification > bin > oss report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_OSS_REPORT_BIN})
    public ResponseEntity<Map<String, Object>> ossReportBin(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "OSS Report > sheetName : 'BIN'", required = false) @RequestPart(required = false) MultipartFile ossReport,
            @ApiParam(value = "Binary.txt", required = false) @RequestPart(required = false) MultipartFile binaryTxt,
            @ApiParam(value = "Comment", required = false) @RequestParam(required = false) String comment,
            @ApiParam(value = "Reset Flag (YES : Y, NO : N, Default : Y)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String resetFlag,
            @ApiParam(value = "Sheet Names", required = false) @RequestParam(required = false) String sheetNames) {

        T2Users userInfo = userService.checkApiUserAuth(authorization); // token이 정상적인 값인지 확인
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("loginUserName", userInfo.getUserName());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("ossReportFlag", CoConstDef.FLAG_YES);
            paramMap.put("distributionType", "normal");
            paramMap.put("readOnly", CoConstDef.FLAG_NO);

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.

            if (!searchFlag) {
                return responseService.errorResponse(HttpStatus.FORBIDDEN);
            }
            List<ProjectIdentification> ossComponents = new ArrayList<>();
            List<List<ProjectIdentification>> ossComponentsLicense = null;
            String changeExclude = "";
            String changeAdded = "";
            Project project = new Project();
            UploadFile ossReportBean = null;
            UploadFile binaryTxtBean = null;

            String oldFileId = "";
            if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
                Map<String, Object> prjInfo = apiProjectService.selectProjectMaster(prjId);
                if (prjInfo.get("binCsvFileId") != null) {
                    oldFileId = String.valueOf((int) prjInfo.get("binCsvFileId"));
                }
            }

            if (ossReport != null) {
                if (!ossReport.getOriginalFilename().contains("xls")) { // 확장자 xls, xlsx, xlsm 허용
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE));
                } else if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= ossReport.getSize()) { // file size 5MB 이하만 허용.
                    return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
                }

                boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap); // 잘못된  project에 oss report를 upload하려고 할 경우 ex) bin -> bin Android
                if (!checkDistributionTypeFlag) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE));
                }

                if (!isEmpty(oldFileId)) {
                    ossReportBean = apiFileService.uploadFile(ossReport, null, oldFileId);
                } else {
                    ossReportBean = apiFileService.uploadFile(ossReport);
                }

                List<String> sheet = null;
                boolean sheetNamesEmptyFlag = isEmpty(sheetNames) ? true : false;
                try {
                    if (sheetNamesEmptyFlag) {
                        sheet = ExcelUtil.getSheetNoStartsWith("BIN", Arrays.asList(ossReportBean), CommonFunction.emptyCheckProperty("upload.path", "/upload"));
                    } else {
                        List<UploadFile> list = new ArrayList<UploadFile>();
                        list.add(ossReportBean);

                        List<Object> sheets = ExcelUtil.getSheetNames(list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
                        boolean createListFlag = false;
                        for (Object obj : sheets) {
                            Map<String, Object> sheetMap = (Map<String, Object>) obj;
                            if (sheetMap.containsKey("name")) {
                                if (!createListFlag) {
                                    sheet = new ArrayList<>();
                                    createListFlag = true;
                                }
                                sheet.add((String) sheetMap.get("name"));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                Map<String, Object> result = null;
                boolean errorFlag = false;
                String errorMsg = "";
                String sheetName = "";

                if (sheetNamesEmptyFlag) {
                    sheetName = "BIN";
                    result = apiProjectService.getSheetData(ossReportBean, prjId, sheetName, sheet != null ? sheet.toArray(new String[sheet.size()]) : ArrayUtils.EMPTY_STRING_ARRAY);
                    if (result.containsKey("errorMsg")) {
                        errorMsg = (String) result.get("errorMsg");
                    }
                } else {
                    for (String sheetNm : sheetNames.split(",")) {
                        sheetName = sheetNm.trim();
                        if (isEmpty(sheetName)) continue;
                        Map<String, Object> rtnMap = apiProjectService.getSheetData(ossReportBean, prjId, sheetName, sheet != null ? sheet.toArray(new String[sheet.size()]) : ArrayUtils.EMPTY_STRING_ARRAY, true);
                        String rtnErrorMsg = "";
                        if (rtnMap.containsKey("errorMsg")) {
                            rtnErrorMsg = (String) rtnMap.get("errorMsg");
                            if (!isEmpty(rtnErrorMsg) && rtnErrorMsg.toUpperCase().startsWith("THERE ARE NO OSS LISTED")) {
                                errorMsg = rtnErrorMsg;
                                break;
                            }
                        }

                        List<ProjectIdentification> rtnOssComponents = (List<ProjectIdentification>) rtnMap.get("ossComponents");
                        rtnOssComponents = (rtnOssComponents != null ? rtnOssComponents : new ArrayList<>());
                        if (rtnOssComponents.isEmpty()) {
                            errorFlag = true;
                            break;
                        } else {
                            ossComponents.addAll(rtnOssComponents);
                        }

                        List<List<ProjectIdentification>> rtnOssComponentsLicense = (List<List<ProjectIdentification>>) rtnMap.get("ossComponentLicense");
                        if (rtnOssComponentsLicense != null) {
                            if (ossComponentsLicense == null) {
                                ossComponentsLicense = new ArrayList<>();
                            }
                            ossComponentsLicense.addAll(rtnOssComponentsLicense);
                        }
                    }
                }

                if (!isEmpty(errorMsg) && errorMsg.toUpperCase().startsWith("THERE ARE NO OSS LISTED")) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST
                            , CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE));
                }

                if (!isEmpty(errorMsg)) {
                    resultMap.put("errorMessage", errorMsg);
                }

                if (sheetNamesEmptyFlag) {
                    ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
                    ossComponents = (ossComponents != null ? ossComponents : new ArrayList<>());
                    ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");

                    if (ossComponents.isEmpty()) {
                        return responseService.errorResponse(HttpStatus.BAD_REQUEST, getMessage("api.upload.file.sheet.no.match", new String[]{"BIN*"}));
                    }
                } else {
                    if (errorFlag) {
                        return responseService.errorResponse(HttpStatus.BAD_REQUEST, getMessage("api.upload.file.sheet.no.match", new String[]{sheetName + "*"}));
                    }
                }

                project.setBinCsvFileId(ossReportBean.getRegistFileId()); // set file id
            }

            if (binaryTxt != null) {
                if (!binaryTxt.getOriginalFilename().contains("txt")) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE));
                }
                binaryTxtBean = apiFileService.uploadFile(binaryTxt); // file 등록 처리 이후 upload된 file정보를 return함.
                String binaryFileId = binaryTxtBean.getRegistFileId();
                List<String> binaryTxtList = CommonFunction.getBinaryListBinBinaryTxt(fileService.selectFileInfoById(binaryFileId));

                if (binaryTxtList != null && !binaryTxtList.isEmpty()) {
                    // 현재 osslist의 binary 목록을 격납
                    Map<String, ProjectIdentification> componentBinaryList = new HashMap<>();
                    for (ProjectIdentification bean : ossComponents) {
                        if (bean != null && !isEmpty(bean.getBinaryName())) {
                            componentBinaryList.put(bean.getBinaryName(), bean);
                        }
                    }
                    List<ProjectIdentification> addComponentList = Lists.newArrayList();
                    // 존재여부 확인
                    for (String binaryNameTxt : binaryTxtList) {
                        if (!componentBinaryList.containsKey(binaryNameTxt)) {
                            // add 해야할 list
                            ProjectIdentification bean = new ProjectIdentification();
                            // 화면에서 추가한 것 처럼 jqg로 시작하는 component id를 임시로 설정한다.
                            bean.setGridId("jqg_" + binaryFileId + "_" + addComponentList.size());
                            bean.setBinaryName(binaryNameTxt);
                            addComponentList.add(bean);

                            changeAdded += "<br> - " + binaryNameTxt;
                        }
                        // exclude처리된 경우
                        else {
                            ProjectIdentification bean = componentBinaryList.get(binaryNameTxt);
                            if (bean != null && CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
                                changeExclude += "<br>" + binaryNameTxt;
                            }
                        }
                    }
                    if (addComponentList != null && !addComponentList.isEmpty()) {
                        ossComponents.addAll(addComponentList);
                    }
                }

                project.setBinBinaryFileId(binaryFileId);
            }

            if (ossReportBean == null && binaryTxtBean == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE));
            } else {
                T2CoProjectValidator pv = new T2CoProjectValidator();
                pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BIN);
                pv.setValidLevel(pv.VALID_LEVEL_BASIC);
                pv.setAppendix("mainList", ossComponents); // sub grid
                pv.setAppendix("subList", ossComponentsLicense);
                T2CoValidationResult vr = pv.validate(new HashMap<>());

                if (!vr.isValid()) {
                    return responseService.errorResponse(HttpStatus.UNPROCESSABLE_ENTITY,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE));
                } else {
                    List<ProjectIdentification> ossComponentList = new ArrayList<>();
                    List<List<ProjectIdentification>> ossComponentsLicenseList = new ArrayList<>();

                    if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
                        apiProjectService.getIdentificationGridList(prjId, CoConstDef.CD_DTL_COMPONENT_ID_BIN, ossComponentList, ossComponentsLicenseList, null);
                    }

                    ossComponentList.addAll(ossComponents);
                    ossComponentsLicenseList.addAll(ossComponentsLicense);

                    project.setPrjId(prjId);
                    projectService.registSrcOss(ossComponentList, ossComponentsLicenseList, project, CoConstDef.CD_DTL_COMPONENT_ID_BIN); // bin tab

                    String csvFileId = project.getBinCsvFileId();

                    if (!isEmpty(changeExclude) || !isEmpty(changeAdded)) {
                        String changedByResultTxt = "";

                        if (!isEmpty(changeAdded)) {
                            changedByResultTxt += "<b>The following binaries were added to OSS List automatically because they exist in the binary.txt.</b><br>";
                            changedByResultTxt += changeAdded;
                        }
                        if (!isEmpty(changeExclude)) {
                            if (!isEmpty(changedByResultTxt)) {
                                changedByResultTxt += "<br><br>";
                            }
                            changedByResultTxt += "<b>The following binaries are written to the OSS report as excluded, but they are in the binary.txt. Make sure it is not included in the final firmware.</b><br>";
                            changedByResultTxt += changeExclude;
                        }

                        CommentsHistory commentHisBean = new CommentsHistory();
                        commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                        commentHisBean.setReferenceId(prjId);
                        commentHisBean.setExpansion1("BIN");
                        commentHisBean.setContents(changedByResultTxt);
                        commentService.registComment(commentHisBean, false);
                    }

                    // 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
                    try {
                        if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
                                CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId)) != null) {
                            String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(
                                    loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId), true);
                            if (!isEmpty(changedLicenseName)) {
                                CommentsHistory commentHisBean = new CommentsHistory();
                                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                                commentHisBean.setReferenceId(prjId);
                                commentHisBean.setExpansion1("BIN");
                                commentHisBean.setContents(changedLicenseName);
                                commentService.registComment(commentHisBean, false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    // oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
                    try {
                        if (getSessionObject(
                                CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId,
                                        CoConstDef.CD_DTL_COMPONENT_ID_BIN)) != null) {
                            String changedLicenseName = (String) getSessionObject(
                                    CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED,
                                            prjId, CoConstDef.CD_DTL_COMPONENT_ID_BIN),
                                    true);
                            if (!isEmpty(changedLicenseName)) {
                                CommentsHistory commentHisBean = new CommentsHistory();
                                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                                commentHisBean.setReferenceId(prjId);
                                commentHisBean.setExpansion1("BIN");
                                commentHisBean.setContents(changedLicenseName);
                                commentService.registComment(commentHisBean, false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    try {
                        if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId)) != null) {
                            String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId), true);
                            if (!isEmpty(chagedOssVersion)) {
                                CommentsHistory commentHisBean = new CommentsHistory();
                                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                                commentHisBean.setReferenceId(prjId);
                                commentHisBean.setExpansion1("BIN");
                                commentHisBean.setContents(chagedOssVersion);
                                commentService.registComment(commentHisBean, false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    if (comment != null) {
                        CommentsHistory commentHisBean = new CommentsHistory();
                        commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                        commentHisBean.setReferenceId(prjId);
                        commentHisBean.setExpansion1("BIN");
                        commentHisBean.setContents(comment);
                        commentService.registComment(commentHisBean, false);
                    }

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

                    // session 삭제
                    deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_BIN, prjId));
                    deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN,
                            prjId));
                }
            }
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Identification OSS Report", notes = "Identification > android > oss report", hidden = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_OSS_REPORT_ANDROID})
    public ResponseEntity<Map<String, Object>> ossReportAndroid(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "OSS Report > sheetName : 'BIN (Android)'", required = true) @RequestPart(required = true) MultipartFile ossReport,
            @ApiParam(value = "NOTICE.html", required = true) @RequestPart(required = true) MultipartFile noticeHtml,
            @ApiParam(value = "result.txt", required = false) @RequestPart(required = false) MultipartFile resultTxt,
            @ApiParam(value = "Comment", required = false) @RequestParam(required = false) String comment) {

        T2Users userInfo = userService.checkApiUserAuth(authorization); // token이 정상적인 값인지 확인
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("ossReportFlag", CoConstDef.FLAG_YES);
            paramMap.put("distributionType", "android");
            paramMap.put("readOnly", CoConstDef.FLAG_NO);

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.
            UploadFile ossReportBean = null;

            if (!searchFlag) {
                return responseService.errorResponse(HttpStatus.FORBIDDEN,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
            }
            if (!ossReport.getOriginalFilename().contains("xls")) { // 확장자 xls, xlsx, xlsm 허용
                return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_EXT_UNSUPPORT_MESSAGE));
            } else if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= ossReport.getSize()) { // file size 5MB 이하만 허용.
                return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
            } else {
                boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap); // 잘못된  project에 oss report를 upload하려고 할 경우 ex) bin Android -> bin
                if (!checkDistributionTypeFlag) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE));
                }

                ossReportBean = apiFileService.uploadFile(ossReport); // ossReport 등록
            }

            String noticeHtmlFileName = noticeHtml.getOriginalFilename();
            String codeExt[] = StringUtil.split(codeMapper.selectExtType(CoConstDef.CD_ANDROID_NOTICE_XML), ",");
            int count = 0;

            for (int i = 0; i < codeExt.length; i++) {
                if (noticeHtmlFileName.endsWith(codeExt[i])) {
                    count++;
                }
                ;
            }

            UploadFile noticeHtmlBean = null;
            UploadFile noticeXMLBean = null;
            if (count == 1) {
                Map<String, UploadFile> Files = apiFileService.uploadNoticeXMLFile(noticeHtml, prjId);

                noticeHtmlBean = Files.get("noticeHTML");
                noticeXMLBean = Files.get("noticeXML");
            } else if (count == 0) {
                noticeHtmlBean = apiFileService.uploadFile(noticeHtml); // noticeHtml 등록
            } else {
                noticeHtmlBean = null;
            }

            UploadFile resultTxtBean = null;

            if (resultTxt != null) {
                resultTxtBean = apiFileService.uploadFile(resultTxt); // resultTxt 등록
            }

            Map<String, Object> result = apiProjectService.readAndroidBuildImage(ossReportBean, noticeHtmlBean, resultTxtBean);

            List<ProjectIdentification> ossComponents = (List<ProjectIdentification>) result.get("ossComponents");
            List<List<ProjectIdentification>> ossComponentsLicense = (List<List<ProjectIdentification>>) result.get("ossComponentLicense");

            if (ossReportBean == null && noticeHtmlBean == null && resultTxtBean == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_NOTEXISTS_MESSAGE));
            } else {
                T2CoProjectValidator pv = new T2CoProjectValidator();
                pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);
                pv.setValidLevel(pv.VALID_LEVEL_BASIC);
                pv.setAppendix("mainList", ossComponents); // sub grid
                pv.setAppendix("subList", ossComponentsLicense);
                T2CoValidationResult vr = pv.validate(new HashMap<>());

                if (!vr.isValid()) {
                    return responseService.errorResponse(HttpStatus.UNPROCESSABLE_ENTITY,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE));
                } else {
                    Project project = new Project(); // detail project vo
                    project.setPrjId(prjId);
                    project.setSrcAndroidCsvFileId(ossReportBean.getRegistFileId());
                    project.setSrcAndroidNoticeFileId(noticeXMLBean != null ? noticeXMLBean.getRegistFileId() : noticeHtmlBean.getRegistFileId());
                    project.setSrcAndroidNoticeXmlId(noticeHtmlBean != null ? noticeHtmlBean.getRegistFileId() : "");
                    project.setSrcAndroidResultFileId(resultTxtBean != null ? resultTxtBean.getRegistFileId() : "");

                    projectService.registSrcOss(ossComponents, ossComponentsLicense, project,
                            CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);

                    String csvFileId = project.getSrcAndroidCsvFileId();
                    String resultFileId = project.getSrcAndroidResultFileId();

                    try {
                        if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
                                CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileId)) != null) {
                            String changedByResultTxt = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
                                    CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileId), true);
                            if (!isEmpty(changedByResultTxt)) {
                                CommentsHistory commentHisBean = new CommentsHistory();
                                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                                commentHisBean.setReferenceId(prjId);
                                commentHisBean.setExpansion1("BIN(Android)");
                                commentHisBean.setContents(changedByResultTxt);
                                commentService.registComment(commentHisBean, false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    // 분석결과서 업로드시 라이선스명(닉네임)이 변경된 사항이 있으면 이력으로 등록한다.
                    try {
                        if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
                                CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId)) != null) {
                            String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
                                    CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, csvFileId), true);
                            if (!isEmpty(changedLicenseName)) {
                                CommentsHistory commentHisBean = new CommentsHistory();
                                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                                commentHisBean.setReferenceId(prjId);
                                commentHisBean.setExpansion1("BIN(Android)");
                                commentHisBean.setContents(changedLicenseName);
                                commentService.registComment(commentHisBean, false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    // oss name이 nick name으로 등록되어 있는 경우, 자동치환된 Data를 comment his에 등록
                    try {
                        if (getSessionObject(
                                CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId,
                                        CoConstDef.CD_DTL_COMPONENT_ID_ANDROID)) != null) {
                            String changedLicenseName = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(),
                                            CoConstDef.SESSION_KEY_NICKNAME_CHANGED, prjId, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID),
                                    true);
                            if (!isEmpty(changedLicenseName)) {
                                CommentsHistory commentHisBean = new CommentsHistory();
                                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                                commentHisBean.setReferenceId(prjId);
                                commentHisBean.setExpansion1("BIN(Android)");
                                commentHisBean.setContents(changedLicenseName);
                                commentService.registComment(commentHisBean, false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    try {
                        if (getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId)) != null) {
                            String chagedOssVersion = (String) getSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_OSS_VERSION_CHANGED, csvFileId), true);
                            if (!isEmpty(chagedOssVersion)) {
                                CommentsHistory commentHisBean = new CommentsHistory();
                                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                                commentHisBean.setReferenceId(prjId);
                                commentHisBean.setExpansion1("BIN(Android)");
                                commentHisBean.setContents(chagedOssVersion);
                                commentService.registComment(commentHisBean, false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    if (comment != null) {
                        CommentsHistory commentHisBean = new CommentsHistory();
                        commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
                        commentHisBean.setReferenceId(prjId);
                        commentHisBean.setExpansion1("BIN(Android)");
                        commentHisBean.setContents(comment);
                        commentService.registComment(commentHisBean, false);
                    }

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
                }

                // session 정보 삭제
                deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, prjId));
                deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_ANDROID,
                        prjId));

            }
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Verification Package File Upload", notes = "Verification > Package File Upload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PACKAGE_UPLOAD})
    public ResponseEntity<Map<String, Object>> ossUploadPackage(
            @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Package FIle", required = true) @RequestPart(required = true) MultipartFile packageFile,
            @ApiParam(value = "Verify when file is uploaded (YES : Y, NO : N)", required = false, allowableValues = "Y,N") @RequestParam(required = false) String verifyFlag) {

        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        T2Users userInfo = userService.checkApiUserAuth(authorization); // token이 정상적인 값인지 확인
        Map<String, Object> paramMap = new HashMap<>();
        List<String> prjIdList = new ArrayList<String>();
        prjIdList.add(prjId);
        paramMap.put("userId", userInfo.getUserId());
        paramMap.put("userRole", userRole(userInfo));
        paramMap.put("prjId", prjIdList);
        paramMap.put("readOnly", CoConstDef.FLAG_NO);

        boolean searchFlag = apiProjectService.existProjectCnt(paramMap);
        String errorMsg = "";
        String afterFileSeq = "";
        boolean uploadFlag = false;

        if (searchFlag) {
            Map<String, Object> result = apiProjectService.selectVerificationCheck(prjId);
            String useYn = (String) result.get("useYn");
            String packageFileSeq = (String) result.get("packageFileSeq").toString();

            if (CoConstDef.CD_OPEN_API_PACKAGE_FILE_LIMIT < Integer.parseInt(packageFileSeq)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Up to 3 packaging files can be uploaded.");
            }

            String filePath = CommonFunction.emptyCheckProperty("packaging.path", "/upload/packaging") + "/" + prjId;
            UploadFile packageFileBean = apiFileService.uploadFile(packageFile, filePath); // packagingFile 등록
            afterFileSeq = packageFileBean.getRegistSeq();

            if (CoConstDef.FLAG_YES.equals(useYn) && CoConstDef.CD_OPEN_API_PACKAGE_FILE_LIMIT >= Integer.parseInt(packageFileSeq)) {
                // packaging File comment
                String uploadComment = "Packaging file, " + packageFileBean.getOriginalFilename() + ", was uploaded by " + userInfo.getUserId() + ". <br>";

                paramMap = new HashMap<String, Object>();
                paramMap.put("prjId", prjId);
                paramMap.put("packageFileId", packageFileBean.getRegistSeq());
                paramMap.put("packageFileSeq", packageFileSeq);

                apiProjectService.updatePackageFile(paramMap);
                CommentsHistory commHisBean = new CommentsHistory();
                commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS);
                commHisBean.setReferenceId(prjId);
                commHisBean.setContents(uploadComment);
                commentService.registComment(commHisBean);

                errorMsg = null; // 정상적으로 처리됨.
                uploadFlag = true;
            } else {
                if (!CoConstDef.FLAG_YES.equals(useYn)) {
                    errorMsg = "delete project"; // 삭제된 project
                }
            }
        } else {
            errorMsg = "Authrization Issue"; // 해당 사용자 권한으로는 등록할 project가 없음
        }

        try {
            String emailType = isEmpty(errorMsg) ? CoConstDef.CD_MAIL_PACKAGING_UPLOAD_SUCCESS : CoConstDef.CD_MAIL_PACKAGING_UPLOAD_FAILURE;

            CoMail mailBean = new CoMail(emailType);
            mailBean.setParamPrjId(prjId);
            mailBean.setParamExpansion1(packageFile.getOriginalFilename()); // packaging file name
            mailBean.setParamExpansion2(errorMsg);                            // error message
            mailBean.setToIds(new String[]{userInfo.getUserId()});
            mailBean.setLoginUserName(userInfo.getUserId());
            mailBean.setLoginUserRole(userInfo.getAuthority());

            CoMailManager.getInstance().sendMail(mailBean);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // after upload complete, verify
        if ("Y".equals(verifyFlag) && uploadFlag) {
            try {
                Map<String, Object> file = new HashMap<>();

                List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = new HashMap<String, Object>();
                Map<String, Object> resMap = null;

                List<String> fileSeqs = apiProjectService.getPackageFileList(prjId);

                map.put("prjId", prjId);
                map.put("fileSeqs", fileSeqs);

                String packagingComment = apiProjectService.setClearFiles(map);
                map.put("packagingComment", packagingComment);

                Map<String, Object> project = new HashMap<>();
                project.put("prjId", prjId);

                List<Map<String, Object>> list = apiProjectService.getVerifyOssList(project);
                list = apiProjectService.serMergeGridData(list);

                List<String> filePaths = new ArrayList<String>();
                List<String> componentsList = new ArrayList<String>();

                for (Map<String, Object> ossComponents : list) {
                    componentsList.add(Integer.toString((int) ossComponents.get("componentId")));
                    filePaths.add((String) ossComponents.get("filePath"));
                }

                map.put("gridFilePaths", filePaths);
                map.put("gridComponentIds", componentsList);

                boolean isChangedPackageFile = apiProjectService.getChangedPackageFile(prjId, fileSeqs);
                int seq = 1;

                map.put("packagingFileIdx", seq);
                map.put("isChangedPackageFile", isChangedPackageFile);

                for (String fileSeq : fileSeqs) {
                    map.put("fileSeq", fileSeq);
                    map.put("packagingFileIdx", seq++);
                    map.put("isChangedPackageFile", isChangedPackageFile);
                    result.add(apiProjectService.processVerification(map, file, project));
                }

                resMap = result.get(0);

                if (fileSeqs.size() > 1) {
                    resMap.put("verifyValid", result.get(result.size() - 1).get("verifyValid"));
                    resMap.put("fileCounts", result.get(result.size() - 1).get("fileCounts"));
                }

                apiProjectService.updateVerifyFileCount((ArrayList<String>) resMap.get("verifyValid"));
                apiProjectService.updateVerifyFileCount((HashMap<String, Object>) resMap.get("fileCounts"));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "Project Add Watcher", notes = "Project Add Watcher")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_ADD_WATCHER})
    public ResponseEntity<Map<String, Object>> addPrjWatcher(
            @RequestHeader String authorization,
            @ApiParam(value = "Project Id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Watcher Email", required = true) @RequestParam(required = true) String[] emailList) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("ossReportFlag", CoConstDef.FLAG_NO);
            paramMap.put("readOnly", CoConstDef.FLAG_NO);

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap);
            if (!searchFlag) {
                return responseService.errorResponse(HttpStatus.FORBIDDEN,
                        CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE));
            }
            if (emailList == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Email list is required.");
            }
            for (String email : emailList) {
                boolean ldapCheck = true;
                if (CoConstDef.FLAG_YES.equals(avoidNull(CommonFunction.getProperty("ldap.check.flag")))) {
                    apiProjectService.existLdapUserToEmail(email);
                }
                if (!ldapCheck) {
                    return responseService.errorResponse(HttpStatus.NOT_FOUND,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_USER_NOTFOUND_MESSAGE));
                }
                boolean watcherFlag = apiProjectService.existsWatcherByEmail(prjId, email);
                if (!watcherFlag) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
                }
                Map<String, Object> param = new HashMap<>();
                param.put("prjId", prjId);
                param.put("division", "");
                param.put("userId", "");
                param.put("prjEmail", email);
                apiProjectService.insertWatcher(param);
            }

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                    CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE));
        }
    }

    @ApiOperation(value = "Project get Notice", notes = "Project Get")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_GET_NOTICE})
    public ResponseEntity getSelfcheckReport(
            @RequestHeader String authorization,
            @ApiParam(value = "project ID", required = false) @PathVariable(required = true, name = "id") String prjId,
            HttpServletRequest req
    ) {
        try {
            var ossNotice = verificationService.selectOssNoticeOne(prjId);

            if (ossNotice == null) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND, "Notice has not been published for given project.");
            }
            var downloadId = verificationService.getNoticeHtmlFileForPreview(ossNotice);

            T2File fileInfo = fileService.selectFileInfo(downloadId);
            String filePath = fileInfo.getLogiPath();

            if (!filePath.endsWith("/")) {
                filePath += "/";
            }

            filePath += fileInfo.getLogiNm();

            return excelToResponseEntity(filePath, fileInfo.getOrigNm());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @ApiOperation(value = "Load Searched Project Oss to Bin", notes = "Project > Identification > BIN")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "_token", value = "token", required = true, dataType = "String", paramType = "header") })
    @PostMapping(value = { Url.APIV2.FOSSLIGHT_API_OSS_LOAD })
    public ResponseEntity<Map<String, Object>> ossLoad(@RequestHeader String _token,
                                                       @ApiParam(value = "Target Project ID", required = true) @PathVariable(name = "id") String targetPrjId,
                                                       @ApiParam(value = "Load Target Tab Name (Valid Input: dep, src, bin)", required = true, allowableValues = "dep, src, bin")
                                    @ValuesAllowed(propName = "tabName", values = {"dep", "src", "bin"}) @PathVariable(name = "tab_name") String tabName,
                                                       @ApiParam(value = "Search Condition (Project ID : id, Project Name : name)", required = true, allowableValues = "id,name")
                                    @ValuesAllowed(propName = "searchCondition", values = {"id", "name"})@RequestParam(required = true) String searchCondition,
                                                       @ApiParam(value = "Project ID to Load") @RequestParam(required = false) String prjIdToLoad,
                                                       @ApiParam(value = "Project Name to Load") @RequestParam(required = false) String prjNameToLoad,
                                                       @ApiParam(value = "Project Version to Load") @RequestParam(required = false) String prjVersionToLoad,
                                                       @ApiParam(value = "Reset Flag (YES : Y, NO : N, Default : Y)", defaultValue = "Y", allowableValues = "Y, N")
                                    @ValuesAllowed(propName = "resetFlag", values = {"Y", "N"})@RequestParam(required = false) String resetFlag) {

        log.error("/api/v2/oss_load called:" + targetPrjId);

        T2Users userInfo = userService.checkApiUserAuth(_token);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String errorMsgCode = CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE;

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(targetPrjId);
            paramMap.put("userId", userInfo.getUserId());
            paramMap.put("loginUserName", userInfo.getUserName());
            paramMap.put("userRole", userRole(userInfo));
            paramMap.put("prjId", prjIdList);
            paramMap.put("ossReportFlag", CoConstDef.FLAG_YES);
            paramMap.put("distributionType", "normal");
            paramMap.put("readOnly", CoConstDef.FLAG_NO);

            boolean searchFlag = apiProjectService.existProjectCnt(paramMap); // 조회가 안된다면 권한이 없는 project id를 입력함.

            if (!searchFlag) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND,
                        String.format("Project %s not exist or User doesn't have permission for the project", targetPrjId));
            }

            paramMap.clear();

            // Parameter validation check:
            if (!StringUtils.isEmpty(targetPrjId) && !targetPrjId.chars().allMatch(Character::isDigit)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "targetPrjId is not in the correct format");
            }

            paramMap.put("targetPrjId", targetPrjId);
            paramMap.put("resetFlag", CoConstDef.FLAG_YES.equals(StringUtils.isEmpty(resetFlag) ? "Y" : resetFlag));

            switch (searchCondition) {
                case "id":
                    // Check if project ID is entered
                    if (StringUtils.isEmpty(prjIdToLoad)) {
                        return responseService.errorResponse(HttpStatus.BAD_REQUEST, "the prjIdToLoad is missing");
                    }

                    if (!StringUtils.isEmpty(prjIdToLoad) && !prjIdToLoad.chars().allMatch(Character::isDigit)) {
                        return responseService.errorResponse(HttpStatus.BAD_REQUEST, "prjIdToLoad is not in the correct format");
                    }

                    // Check for duplication of targetPrjId with prjIdToLoad
                    if (targetPrjId.equals(prjIdToLoad)) {
                        return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Please enter other prjIdToLoad that is different from targetPrjId");
                    }
                    paramMap.put("prjIdToLoad", prjIdToLoad);
                    break;

                case "name":
                    // Check if project name is entered
                    if (StringUtils.isEmpty(prjNameToLoad)) {
                        return responseService.errorResponse(HttpStatus.BAD_REQUEST, "the prjNameToLoad is missing");
                    }

                    paramMap.put("prjNameToLoad", prjNameToLoad);
                    paramMap.put("prjVersionToLoad", prjVersionToLoad);
                    break;

                default:
                    break;
            }

            switch(tabName) {
                case "dep":
                    resultMap = apiProjectService.registProjectOssComponent(paramMap, CoConstDef.CD_DTL_COMPONENT_ID_DEP);
                    break;
                case "src":
                    resultMap = apiProjectService.registProjectOssComponent(paramMap, CoConstDef.CD_DTL_COMPONENT_ID_SRC);
                    break;
                case "bin":
                    resultMap = apiProjectService.registProjectOssComponent(paramMap, CoConstDef.CD_DTL_COMPONENT_ID_BIN);
                    break;
            }

            // Check if resultMap contains a "msg" key and return failure result if it does
            if (errorMsgCode.equals(resultMap.get("code"))) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, (String) resultMap.get("msg"));
            }

            return new ResponseEntity<>(resultMap, HttpStatus.OK);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

