/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.controller.v2;

import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.advice.CProjectNotAvailableException;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url;
import oss.fosslight.common.Url.APIV2;
import oss.fosslight.domain.*;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.NoticeMapper;
import oss.fosslight.service.*;
import oss.fosslight.util.ExcelDownLoadUtil;
import oss.fosslight.util.ExcelUtil;
import oss.fosslight.api.validator.ValuesAllowed;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.lang.reflect.Type;
import java.util.*;

@Api(tags = {"03. Project"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v2")
@Validated
public class ApiProjectV2Controller extends CoTopComponent {
    private static final String KEY_ERROR_MESSAGE = "errorMessage";
    private static final String KEY_VALID_ERROR = "validError";


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


    @ApiOperation(value = "프로젝트 목록 조회", notes = "조회 권한이 있는 프로젝트를 조건과 페이지 정보로 검색합니다. 조회 결과가 없으면 빈 list와 totalCount 0을 반환합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공", response = Map.class,
                    examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"list\":[],\"totalCount\":0}"))),
            @ApiResponse(code = 400, message = "잘못된 요청 - page 또는 countPerPage가 1 미만", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Input value=0. page must be larger than 1\"}"))),
            @ApiResponse(code = 401, message = "인증 실패 - 사용자 또는 TOKEN 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_SEARCH})
    public ResponseEntity<Map<String, Object>> selectProjectList(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project Name", required = false) @RequestParam(required = false) String prjName,
            @ApiParam(value = "Project Name exact match (Y: true, N: false)", allowableValues = "Y,N", required = false) @RequestParam(required = false, defaultValue = "N") String prjNameExactYn,
            @ApiParam(value = "project ID List", required = false) @RequestParam(required = false) String[] prjIdList,
            @ApiParam(value = "Division (\"Check the input value with /api/v2/codes\")", required = false) @RequestParam(required = false) String division,
            @ApiParam(value = "Model Name", required = false) @RequestParam(required = false) String modelName,
            @ApiParam(value = "Model Name exact match (Y: true, N: false)", allowableValues = "Y,N", required = false) @RequestParam(required = false, defaultValue = "N") String modelNameExactYn,
            @ApiParam(value = "Create Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String createDate,
            @ApiParam(value = "Status (PROG:progress, REQ:Request, REV:Review, COMP:Complete, DROP:Drop)", required = false, allowableValues = "PROG,REQ,REV,COMP,DROP") @RequestParam(required = false) String status,
            @ApiParam(value = "Update Date (Format: fromDate-toDate > yyyymmdd-yyyymmdd)", required = false) @RequestParam(required = false) String updateDate,
            @ApiParam(value = "Creator", required = false) @RequestParam(required = false) String creator,
            @ApiParam(value = "OSS Name", required = false) @RequestParam(required = false) String ossName,
            @ApiParam(value = "OSS Version", required = false) @RequestParam(required = false) String ossVersion,
            @ApiParam(value = "Count Per Page (max: 1000)", required = false)
            @Min(value = 1, message = "Input value=${validatedValue}. countPerPage must be larger than {value}") @RequestParam(required = false, defaultValue = "1000") int countPerPage,
            @ApiParam(value = "Page", required = false)
            @Min(value = 1, message = "Input value=${validatedValue}. page must be larger than {value}") @RequestParam(required = false, defaultValue = "1") int page) {

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        CommonFunction.splitDate(createDate, paramMap, "-", "createDate");
        CommonFunction.splitDate(updateDate, paramMap, "-", "updateDate");

//			paramMap.put("userRole", userInfo.getAuthority());
        paramMap.put("creator", creator);
        paramMap.put("userId", userInfo.getUserId());
        paramMap.put("userRole", userRole(userInfo));
        paramMap.put("division", division);
        paramMap.put("modelName", modelName);
        paramMap.put("modelNameExactYn", modelNameExactYn);
        paramMap.put("status", status);
        paramMap.put("prjIdList", prjIdList);
        paramMap.put("prjName", prjName);
        paramMap.put("prjNameExactYn", prjNameExactYn);
        paramMap.put("countPerPage", countPerPage);
        paramMap.put("offset", (page - 1) * countPerPage);
        if (!isEmpty(ossName)) {
            paramMap.put("ossName", ossName);
        }
        if (!isEmpty(ossVersion)) {
            paramMap.put("ossVersion", ossVersion);
        }

        resultMap = apiProjectService.selectProjectList(paramMap);

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "프로젝트 모델 목록 조회", notes = "prjIdList에 지정한 프로젝트별 모델 목록과 배포명을 조회합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"records\":0,\"contents\":[]}"))),
            @ApiResponse(code = 400, message = "잘못된 요청 - prjIdList 누락", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"msg\":\"'prjIdList' parameter is missing or misspelled\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_MODEL_SEARCH})
    public ResponseEntity<Map<String, Object>> selectModelList(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "project ID List", required = true) @RequestParam(required = true) String[] prjIdList) {

        // 사용자 인증
        userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("prjIdList", prjIdList);
        resultMap = apiProjectService.selectModelList(paramMap);

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "프로젝트 모델 목록 수정", notes = "모델명|카테고리|출시일(yyyyMMdd) 형식의 목록으로 프로젝트 모델을 교체합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "수정 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "잘못된 요청 - modelListToUpdate 누락", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"msg\":\"'modelListToUpdate' parameter is missing or misspelled\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "모델 데이터 생성 실패 또는 서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_MODEL_UPDATE})
    public ResponseEntity<Map<String, Object>> updateModelList(
            @ApiParam(hidden = true) @RequestHeader String authorization,
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

        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(prjId);
        }

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

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(value = "파일로 프로젝트 모델 목록 수정", notes = "스프레드시트의 모델 목록으로 프로젝트 모델을 교체합니다. 파일명에 xls가 포함되어야 하며 최대 크기는 15MB입니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "수정 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "잘못된 요청 - 파일 누락 또는 파싱 실패\n\n* `The parameter is invalid.`\n* `Error while parsing given file`", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Error while parsing given file\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 413, message = "파일 형식 오류 또는 15MB 이상", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"File size exceeded. (Max size: 15MB for oss report, 4GB for packaging file)\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_MODEL_UPDATE_UPLOAD_FILE})
    public ResponseEntity<Map<String, Object>> updateModelListUploadFile(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Model List (Spread sheet)", required = false) @RequestPart(required = false) MultipartFile modelReport) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, List<Project>> modelList = null;

        if (modelReport == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
                throw new CProjectNotAvailableException(prjId);
            }
            try {
                Project project = projectService.getProjectBasicInfo(prjId);
                if (modelReport != null) {
                    if (modelReport.getOriginalFilename().contains("xls") // Allowed file extension: xls, xlsx, xlsm
                            && CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT > modelReport.getSize()) { // Max file size :15MB
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
            }
        }

        return responseService.errorResponse(HttpStatus.BAD_REQUEST);
//		return responseService.getFailResult(errorCode, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, errorCode));
    }

    @ApiOperation(value = "프로젝트 생성", notes = "프로젝트 기본 정보와 Notice 설정을 등록하고 생성된 프로젝트 ID를 반환합니다. 코드 값은 /api/v2/codes에서 확인할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "생성 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"prjId\":\"123\"}"))),
            @ApiResponse(code = 400, message = "필수값 누락, 코드값 오류 또는 중복 프로젝트\n\n* `Valid OS type code is required.`\n* `Valid distribution type code is invalid.`\n* `Valid distribution site type code is invalid.`\n* `Network server type parameter must be either Y or N.`\n* `Notice type code is invalid.`\n* `Must select 'noticeTypeEtc' code for Platform-generated type`\n* `noticeTypeEtc code is invalid.`\n* `Priority code is invalid`\n* `Project '{name} ({version})' already exists`", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Valid OS type code is required.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_CREATE})
    public ResponseEntity<Map<String, Object>> createProject(
            @ApiParam(hidden = true) @RequestHeader String authorization,
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
        Map<String, Object> result = new HashMap<String, Object>();

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
                if (!CoCodeManager.checkValidCodeDtl(CoConstDef.CD_NOTICE_TYPE, noticeType)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Notice type code is invalid.");
                }
            }

            if (CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(noticeType)) {
                if (isEmpty(noticeTypeEtc)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Must select 'noticeTypeEtc' code for Platform-generated type");
                }
                if (!CoCodeManager.checkValidCodeDtl(CoConstDef.CD_PLATFORM_GENERATED, noticeTypeEtc)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "noticeTypeEtc code is invalid.");
                }
            } else {
                noticeTypeEtc = "";
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
            paramMap.put("noticeType", avoidNull(noticeType, CoConstDef.CD_DTL_NOTICE_TYPE_GENERAL));
            paramMap.put("noticeTypeEtc", noticeTypeEtc);


            result = apiProjectService.createProject(paramMap);

            if (result == null || result.isEmpty()) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                        String.format("Project '%s%s' already exists", prjName, avoidNull(" (" + prjVersion + ")", "")));
            }

            String resultPrjId = (String) result.get("prjId");

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

            try {
                String initMessage = "<p>Project Created via API</p>";
                if (!isEmpty(userComment)) {
                    initMessage += userComment;
                }

                CommentsHistory commentHisBean = new CommentsHistory();
                commentHisBean.setLoginUserName(userInfo.getUserId());
                commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
                commentHisBean.setReferenceId(resultPrjId);
                commentHisBean.setContents(initMessage);
                commentHisBean.setStatus("created");
                commentService.registComment(commentHisBean);

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
    }


    @ApiOperation(value = "프로젝트 SBOM 파일 다운로드", notes = "프로젝트 BOM을 스프레드시트 파일로 생성하여 다운로드합니다. saveFlag가 Y이면 생성 결과를 프로젝트에 저장합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "파일 다운로드 성공", response = FileSystemResource.class),
            @ApiResponse(code = 400, message = "잘못된 요청 - 필수 format 누락 또는 허용값 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"error\":\"Bad Request\",\"msg\":\"'format' parameter is missing or misspelled\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "파일 생성 또는 서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_BOM_DOWNLOAD})
    public ResponseEntity<FileSystemResource> getPrjBomDownload(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Save Flag (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "saveFlag", values = {"Y", "N"}) @RequestParam(required = false, defaultValue = "Y") String saveFlag,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet")
            @ValuesAllowed(propName = "format", values = {"Spreadsheet"}) @RequestParam String format) throws Exception {
        return getPrjBomDownloadInternal(authorization, prjId, saveFlag, format);
    }

    @ApiOperation(value = "프로젝트 BOM 파일 다운로드 (Deprecated)", notes = "이전 경로입니다. /projects/{id}/sbom/file 사용을 권장합니다.", hidden = true)
    @ApiResponses({
            @ApiResponse(code = 200, message = "파일 다운로드 성공", response = FileSystemResource.class),
            @ApiResponse(code = 400, message = "잘못된 요청", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The parameter is invalid.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {"/projects/{id}/bom/file"})
    public ResponseEntity<FileSystemResource> getPrjBomDownloadDeprecated(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Save Flag (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "saveFlag", values = {"Y", "N"}) @RequestParam(required = false, defaultValue = "Y") String saveFlag,
            @ApiParam(value = "Format", allowableValues = "Spreadsheet")
            @ValuesAllowed(propName = "format", values = {"Spreadsheet"}) @RequestParam String format) throws Exception {
        return getPrjBomDownloadInternal(authorization, prjId, saveFlag, format);
    }

    private ResponseEntity<FileSystemResource> getPrjBomDownloadInternal(String authorization, String prjId, String saveFlag, String format) throws Exception {
        log.info("Project Bom Download as File :: " + prjId + " :: " + saveFlag + " :: " + format);

        // 사용자 인증
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(prjId);
        }

        String downloadId = "";
        T2File fileInfo = new T2File();
        String type = "";

        if (CoConstDef.FLAG_YES.equals(saveFlag)) {
            apiProjectService.registBom(prjId, saveFlag, userInfo.getUserId());
            projectService.updateSecurityDataForProject(prjId);
        }

        Project project = new Project();
        project.setPrjId(prjId);
        Project projectMaster = projectService.getProjectDetail(project);

        if (projectMaster.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
            type = "binAndroidBom";
        } else {
            type = "bom";
        }
        downloadId = ExcelDownLoadUtil.getExcelDownloadId(type, prjId, RESOURCE_PUBLIC_DOWNLOAD_EXCEL_PATH_PREFIX);
        fileInfo = fileService.selectFileInfo(downloadId);

        return excelToResponseEntity(fileInfo.getLogiPath() + fileInfo.getLogiNm(), fileInfo.getOrigNm());
    }

    @ApiOperation(value = "프로젝트 SBOM JSON 조회", notes = "프로젝트 BOM을 OSS 이름별 JSON으로 반환합니다. saveFlag가 Y이면 생성 결과를 프로젝트에 저장합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공. 프로젝트가 조회되지 않으면 빈 객체 반환", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "잘못된 요청 - saveFlag 허용값 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"getPrjBomAsJson.saveFlag: Input value='X'. 'saveFlag' field value should be from list of [Y, N]\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_BOM_JSON})
    public ResponseEntity<Map<String, Object>> getPrjBomAsJson(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Save Flag (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "saveFlag", values = {"Y", "N"}) @RequestParam(required = false) String saveFlag) {
        return getPrjBomAsJsonInternal(authorization, prjId, saveFlag);
    }

    @ApiOperation(value = "프로젝트 BOM JSON 조회 (Deprecated)", notes = "이전 경로입니다. /projects/{id}/sbom/json-data 사용을 권장합니다.", hidden = true)
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "잘못된 요청", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The parameter is invalid.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {"/projects/{id}/bom/json-data"})
    public ResponseEntity<Map<String, Object>> getPrjBomAsJsonDeprecated(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Save Flag (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "saveFlag", values = {"Y", "N"}) @RequestParam(required = false) String saveFlag) {
        return getPrjBomAsJsonInternal(authorization, prjId, saveFlag);
    }

    private ResponseEntity<Map<String, Object>> getPrjBomAsJsonInternal(String authorization, String prjId, String saveFlag) {
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(prjId);
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();

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
            if (CoConstDef.FLAG_YES.equals(saveFlag)) {
                apiProjectService.registBom(prjId, saveFlag, userInfo.getUserId());
                projectService.updateSecurityDataForProject(prjId);
            }
        }
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "프로젝트 SBOM 비교", notes = "두 프로젝트의 BOM을 비교하여 추가·삭제·변경 항목을 contents에 반환합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "비교 성공. 같은 프로젝트이면 status가 same", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"contents\":{\"status\":\"same\"}}"))),
            @ApiResponse(code = 400, message = "비교할 BOM 데이터 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The parameter is invalid.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_BOM_COMPARE})
    public ResponseEntity<Map<String, Object>> getPrjBomCompare(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Before Project id", required = true) @PathVariable(name = "id", required = true) String beforePrjId,
            @ApiParam(value = "After Project id", required = true) @PathVariable(name = "compareId", required = true) String afterPrjId) {
        return getPrjBomCompareInternal(authorization, beforePrjId, afterPrjId);
    }

    @ApiOperation(value = "프로젝트 BOM 비교 (Deprecated)", notes = "이전 경로입니다. /projects/{id}/sbom/compare-with/{compareId} 사용을 권장합니다.", hidden = true)
    @ApiResponses({
            @ApiResponse(code = 200, message = "비교 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"contents\":{\"status\":\"same\"}}"))),
            @ApiResponse(code = 400, message = "비교할 BOM 데이터 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The parameter is invalid.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {"/projects/{id}/bom/compare-with/{compareId}"})
    public ResponseEntity<Map<String, Object>> getPrjBomCompareDeprecated(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Before Project id", required = true) @PathVariable(name = "id", required = true) String beforePrjId,
            @ApiParam(value = "After Project id", required = true) @PathVariable(name = "compareId", required = true) String afterPrjId) {
        return getPrjBomCompareInternal(authorization, beforePrjId, afterPrjId);
    }

    private ResponseEntity<Map<String, Object>> getPrjBomCompareInternal(String authorization, String beforePrjId, String afterPrjId) {
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

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

                Project beforePrjInfo = apiProjectService.getProjectBasicInfo(beforePrjId);
                if (!CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(beforePrjInfo.getNoticeType())) {
                    beforeBomList = apiProjectService.getBomList(beforePrjId);
                } else {
                    apiProjectService.getIdentificationGridList(beforePrjId, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, null, null, beforeBomList);
                    beforeBomList = apiProjectService.setMergeGridData(beforeBomList);
                }

                Project afterPrjInfo = apiProjectService.getProjectBasicInfo(afterPrjId);
                if (!CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED.equals(afterPrjInfo.getNoticeType())) {
                    afterBomList = apiProjectService.getBomList(afterPrjId);
                } else {
                    apiProjectService.getIdentificationGridList(afterPrjId, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, null, null, afterBomList);
                    afterBomList = apiProjectService.setMergeGridData(afterBomList);
                }

                if (beforeBomList.isEmpty() || afterBomList.isEmpty()) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST);
                }

                resultMap.put("contents", apiProjectService.getBomCompare(beforeBomList, afterBomList));

                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            } else {
                paramMap.clear();
                paramMap.put("status", "not exist project");
                resultMap.put("contents", paramMap);
                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            }
    }

    @ApiOperation(value = "Identification 탭 초기화", notes = "지정한 dep, src, bin 또는 전체(all) OSS 컴포넌트 데이터를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "초기화 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "잘못된 tab_name", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"identificationReset.tabName: Input value='invalid'. 'tabName' field value should be from list of [dep, src, bin, all]\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_IDENTIFICATION_RESET})
    public ResponseEntity<Map<String, Object>> identificationReset(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Upload Target Tab Name (Valid Input: dep, src, bin, all)", required = true, allowableValues = "dep, src, bin, all")
            @ValuesAllowed(propName = "tabName", values = {"dep", "src", "bin", "all"}) @PathVariable(name = "tab_name") String tabName
    ) {
        T2Users userInfo = userService.checkApiUserAuth(authorization);
        log.info(String.format("/api/v2/projects/%s/%s/reset called by %s", prjId, tabName, userInfo.getUserId()));
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        if (!apiProjectService.checkUserAvailableToEditProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", prjId));
        }

        tabName = tabName.toUpperCase();

        Type collectionType2 = new TypeToken<List<ProjectIdentification>>() {
        }.getType();
        List<ProjectIdentification> ossComponents = new ArrayList<ProjectIdentification>();
        ossComponents = (List<ProjectIdentification>) fromJson("[]", collectionType2);
        List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(ossComponents);

        Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(ossComponents, ossComponentsLicense);
        ossComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
        ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");

        Project project = new Project();
        project.setPrjId(prjId);
        Project projectMaster = projectService.getProjectDetail(project);
        project.setCsvFileSeq(new ArrayList<T2File>());

        switch (tabName) {
            case "ALL":
                apiProjectService.processResetTab("DEP", projectMaster, ossComponents, ossComponentsLicense);
                apiProjectService.processResetTab("SRC", projectMaster, ossComponents, ossComponentsLicense);
                apiProjectService.processResetTab("BIN", projectMaster, ossComponents, ossComponentsLicense);
                break;
            case "DEP":
            case "SRC":
            case "BIN":
                apiProjectService.processResetTab(tabName, projectMaster, ossComponents, ossComponentsLicense);
                break;
        }
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Identification OSS Report 업로드", notes = "지정한 dep, src 또는 bin 탭에 OSS Report를 업로드합니다. resetFlag로 기존 데이터 교체 여부를 정하고, sbomSave가 Y이면 SBOM도 갱신합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "업로드 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"success\":true,\"addedCount\":1}"))),
            @ApiResponse(code = 400, message = "파일·탭·데이터 오류\n\n* `ossReport is required.`\n* 지원하지 않는 파일 확장자 메시지\n* `The tab you are trying to upload is not active. Check Project Distribution Type`\n* `There is no data to load.`\n* `There is an error in the data written in the file.`\n* 파일 분석 중 생성된 상세 오류 메시지", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is no data to load.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))),
            @ApiResponse(code = 413, message = "파일 크기 초과 (15MB 이상)", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"File size exceeded. (Max size: 15MB for oss report, 4GB for packaging file)\"}"))),
            @ApiResponse(code = 500, message = "파일 처리 또는 서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_OSS_REPORT})
    public ResponseEntity<Map<String, Object>> ossReportAll(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id") @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Upload Target Tab Name (Valid Input: dep, src, bin)", required = true, allowableValues = "dep, src, bin")
            @ValuesAllowed(propName = "tabName", values = {"dep", "src", "bin"}) @PathVariable(name = "tab_name") String tabName,
            @ApiParam(value = "OSS Report", required = true) @RequestPart(required = true) MultipartFile ossReport,
            @ApiParam(value = "Comment") @RequestParam(name = "comment", required = false) String comment,
            @ApiParam(value = "Reset Flag (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "resetFlag", values = {"Y", "N"}) @RequestParam(required = false, defaultValue = "Y") String resetFlag,
            @ApiParam(value = "Sheet Names") @RequestParam(name = "sheet_names", required = false) String sheetNames,
            @ApiParam(value = "SBOM save (YES : Y, NO : N)", allowableValues = "Y,N")
            @ValuesAllowed(propName = "SBOM save", values = {"Y", "N"}) @RequestParam(required = false, defaultValue = "Y") String sbomSave,
            @ApiParam(value = "BOM save (YES : Y, NO : N)", allowableValues = "Y,N", hidden = true)
            @ValuesAllowed(propName = "BOM save", values = {"Y", "N"}) @RequestParam(required = false) String bomSave) {


        T2Users userInfo = userService.checkApiUserAuth(authorization);
        log.info(String.format("/api/v2/projects/%s/%s/reports called by %s", prjId, tabName, userInfo.getUserId()));
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        tabName = tabName.toUpperCase();

        // bomSave 파라미터가 있으면 sbomSave로 사용
        if (!isEmpty(bomSave)) {
            sbomSave = bomSave;
        }

        if (!apiProjectService.checkUserAvailableToEditProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", prjId));
        }

        String oldFileId = "";
//            if (CoConstDef.FLAG_NO.equals(avoidNull(resetFlag))) {
//                Map<String, Object> prjInfo = apiProjectService.selectProjectMaster(prjId);
//                if (prjInfo.get(tabName.toLowerCase() + "CsvFileId") != null) {
//                    oldFileId = String.valueOf((int) prjInfo.get(tabName.toLowerCase() + "CsvFileId"));
//                }
//            }

            Project prjInfo = apiProjectService.selectProjectMaster(prjId);

            // UI와 동일한 방식: IDENTIFICATION_CSV_FILE_ID (모든 탭이 공유하는 FILE_ID)
            String identificationCsvFileId = prjInfo.getIdentificationCsvFileId();
            if (!isEmpty(identificationCsvFileId)) {
                oldFileId = identificationCsvFileId;
            }


            List<ProjectIdentification> ossComponents = new ArrayList<>();
            List<List<ProjectIdentification>> ossComponentsLicense = null;
            String changeExclude = "";
            String changeAdded = "";
            Project project = new Project();
            UploadFile ossReportBean = null;
            UploadFile binaryTxtBean = null;

            if (ossReport == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "ossReport is required.");
            }
            String originalFilename = ossReport.getOriginalFilename();
            int extensionIndex = originalFilename == null ? -1 : originalFilename.lastIndexOf('.');
            String fileExtension = extensionIndex > -1 ? StringUtils.lowerCase(originalFilename.substring(extensionIndex + 1)) : "";
            if (!projectService.isAllowedProjectReportExtension(fileExtension)) {
                List<String> fileExtList = projectService.getAllowedProjectReportExtensions();
                String msg = getMessage("msg.project.packaging.upload.fileextension", new String[]{String.join(",", fileExtList)});
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, msg);
            }
            if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= ossReport.getSize()) {
                return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
            }

            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("prjId", prjIdList);
            paramMap.put("distributionType", "normal");

            try {
                boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap); // 잘못된  project에 oss report를 upload하려고 할 경우 ex) src -> bin Android
                if (!checkDistributionTypeFlag) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE) + " Check Project Distribution Type");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            UploadFile bean = null;
            if (!isEmpty(oldFileId)) {
                bean = apiFileService.uploadFileWithCreator(ossReport, userInfo.getUserId(), oldFileId);
            } else {
                bean = apiFileService.uploadFileWithCreator(ossReport, userInfo.getUserId(), null); // file 등록 처리 이후 upload된 file정보를 return함.
            }

            // get Excel Sheet name starts with SRC
            List<String> sheet = null;
            boolean sheetNamesEmptyFlag = isEmpty(sheetNames) ? true : false;

            try {
                if (sheetNamesEmptyFlag) {
                    sheet = ExcelUtil.getSheetNoStartsWith(tabName, Arrays.asList(bean), CommonFunction.emptyCheckProperty("upload.path", "/upload"));
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
            int addedCount = 0;
            if (sheetNamesEmptyFlag) {
                result = apiProjectService.getSheetData(bean, prjId, tabName.toUpperCase(), sheet != null ? sheet.toArray(new String[sheet.size()]) : ArrayUtils.EMPTY_STRING_ARRAY);
                resultMap = apiProjectService.getProcessSheetData(result, prjId, resetFlag, bean.getRegistFileId(), userInfo.getUserId(), comment, tabName, tabName, sheetNamesEmptyFlag, false, 0, true, addedCount);
                addedCount += Integer.parseInt(String.valueOf(resultMap.getOrDefault("addedCount", 0)));
            } else {
                int sheetLength = sheetNames.split(",").length;
                int sheetIdx = 0;
                for (String sheetNm : sheetNames.split(",")) {
                    if (isEmpty(sheetNm.trim())) {
                        continue;
                    }
                    result = apiProjectService.getSheetData(bean, prjId, sheetNm.trim(), sheet != null ? sheet.toArray(new String[sheet.size()]) : ArrayUtils.EMPTY_STRING_ARRAY, true);
                    resultMap = apiProjectService.getProcessSheetData(result, prjId, resetFlag, bean.getRegistFileId(), userInfo.getUserId(), comment, tabName, sheetNm.trim(), sheetNamesEmptyFlag, sheetLength > 1 ? true : false, sheetIdx, sheetIdx == sheetLength-1, addedCount);
                    addedCount += Integer.parseInt(String.valueOf(resultMap.getOrDefault("addedCount", 0)));
                    resultMap.put("addedCount", addedCount);
                    sheetIdx++;
                    if (resultMap.containsKey(KEY_ERROR_MESSAGE) || resultMap.containsKey(KEY_VALID_ERROR)) {
                        break;
                    }
                }
            }

            if (CoConstDef.FLAG_YES.equals(sbomSave)) {
                apiProjectService.registBom(prjId, CoConstDef.FLAG_YES, userInfo.getUserId());
                projectService.updateSecurityDataForProject(prjId);
            }

        if (!resultMap.containsKey(KEY_ERROR_MESSAGE) && !resultMap.containsKey(KEY_VALID_ERROR)) {
                // 정상처리된 경우 세션 삭제
                switch (tabName) {
                    case "DEP":
                        deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_DEP, prjId));
                        deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_DEP, prjId));
                        break;
                    case "SRC":
                        deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId));
                        deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));
                        break;
                    case "BIN":
                        deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_BIN, prjId));
                        deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN, prjId));
                        break;
                }

                resultMap.put("success", true);
                return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } else {
            if (resultMap.containsKey(CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_DATA_EMPTY_MESSAGE));
            } else if (resultMap.containsKey(KEY_VALID_ERROR)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_DATA_VALIDERROR_MESSAGE));
            } else if (resultMap.containsKey(KEY_ERROR_MESSAGE)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, (String) resultMap.get(KEY_ERROR_MESSAGE));
            } else {
                return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(
            value = "다중 시트 Identification OSS Report 업로드",
            notes = "### Multi-sheet OSS Report Upload\n\n" +
                    "단일 엑셀 파일에서 여러 시트를 읽어 dep/src/bin 탭의 OSS 컴포넌트를 한번에 등록합니다.\n\n" +
                    "#### 요청 파라미터\n\n" +
                    "* **ossReport** - 업로드할 엑셀 파일 (dep, src, bin 시트 포함)\n" +
                    "  - 지원 형식: .xlsx, .xls, .csv 등 리포트 파일 및 CycloneDX, SPDX 양식\n" +
                    "  - 최대 용량: 15MB\n" +
                    "  - 각 시트에는 OSS 이름, 버전, 라이선스 등의 컬럼 포함\n\n" +
                    "* **tabSheetMapping** - 탭과 시트명 매핑 JSON (필수)\n" +
                    "  - 형식: `{\"탭명\": [\"시트명1\", \"시트명2\", ...]}`\n" +
                    "  - 탭명 허용값: `src`, `dep`, `bin` (소문자)\n" +
                    "  - 로드하지 않을 탭은 JSON에서 생략하면 됨 (예: src만 로드 → `{\"src\":[\"시트명\"]}`)\n" +
                    "  - 예시: `{\"src\":[\"SRC_FL_Source\"],\"dep\":[\"DEP_FL_Dependency\"],\"bin\":[\"BIN_FL_Binary\"]}`\n\n" +
                    "* **comment** - 업로드 시 추가 설명 (선택)\n\n" +
                    "* **id** - Report파일을 업로드할 Project ID\n\n"
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "성공",
                    response = Map.class,
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"success\":true,\"uploaded\":[],\"error\":[]}"))
            ),
            @ApiResponse(
                    code = 400,
                    message = "파일·매핑·시트 데이터 오류\n\n" +
                            "**가능한 오류 메시지:**\n\n" +
                            "* `ossReport is required.` - 파일이 업로드되지 않음\n" +
                            "* 지원하지 않는 파일 확장자 메시지\n" +
                            "* `Invalid tabSheetMapping JSON format.` - JSON 형식이 잘못됨\n" +
                            "* `tabSheetMapping is required and must not be empty.` - tabSheetMapping이 비어있음\n" +
                            "* `Invalid tab name in tabSheetMapping: {tab} (allowed: dep, src, bin)` - 지원하지 않는 탭명\n" +
                            "* `Tab '{tab}' cannot have an empty sheet list. Either provide sheet names or omit this tab from the request.` - 탭에 시트명이 없음\n" +
                            "* `File information not found.` - 파일 저장 실패\n" +
                            "* `'{sheet}' sheet not found in the uploaded file.` - 시트를 찾을 수 없음\n" +
                            "* 시트 읽기 중 생성된 상세 오류 메시지\n",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"msg\":\"ossReport is required.\"}"))
            ),
            @ApiResponse(
                    code = 413,
                    message = "파일 크기 초과",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json",
                            value = "{\"msg\":\"File size exceeded. (Max size: 15MB for oss report, 4GB for packaging file)\"}"))
            ),
            @ApiResponse(
                    code = 401,
                    message = "인증 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))
            ),
            @ApiResponse(
                    code = 403,
                    message = "프로젝트 수정 권한 없음",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))
            ),
            @ApiResponse(
                    code = 500,
                    message = "서버 내부 오류 또는 파일 ID 생성 실패",
                    examples = @Example(value = @ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}"))
            )
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_UPLOAD_OSS_REPORT}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadReport(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "OSS Report (one excel file containing dep/src/bin sheets)", required = true) @RequestPart(required = true) MultipartFile ossReport,
            @ApiParam(value = "Comment") @RequestParam(name = "comment", required = false) String comment,
            @ApiParam(
                    value = "Tab to Sheet Names mapping JSON\n" +
                            "(Example: {\"src\":[\"SRC_FL_Source\"],\"dep\":[\"DEP_FL_Dependency\"],\"bin\":[\"BIN_FL_Binary\"]})",
                    required = true
            )
            @RequestParam(required = true) String tabSheetMapping) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        log.info(String.format("/api/v2/projects/%s/reports (multi) called by %s", prjId, userInfo.getUserId()));
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (!apiProjectService.checkUserAvailableToEditProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", prjId));
        }

        // 1. Parse tabSheetMapping JSON
        Map<String, List<String>> tabSheetMap = null;
            try {
                Type mapType = new TypeToken<Map<String, List<String>>>() {
                }.getType();
                tabSheetMap = (Map<String, List<String>>) fromJson(tabSheetMapping, mapType);
            } catch (Exception e) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Invalid tabSheetMapping JSON format.");
            }

            if (MapUtils.isEmpty(tabSheetMap)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "tabSheetMapping is required and must not be empty.");
            }

            // Validate tab keys
            for (String tab : tabSheetMap.keySet()) {
                if (!"dep".equalsIgnoreCase(tab) && !"src".equalsIgnoreCase(tab) && !"bin".equalsIgnoreCase(tab)) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Invalid tab name in tabSheetMapping: " + tab + " (allowed: dep, src, bin)");
                }
                if (CollectionUtils.isEmpty(tabSheetMap.get(tab))) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                            "Tab '" + tab + "' cannot have an empty sheet list. Either provide sheet names or omit this tab from the request.");
                }
            }

            // 2. Validate uploaded file
            if (ossReport == null) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "ossReport is required.");
            }
            String originalFilename = ossReport.getOriginalFilename();
            int extensionIndex = originalFilename == null ? -1 : originalFilename.lastIndexOf('.');
            String fileExtension = extensionIndex > -1 ? StringUtils.lowerCase(originalFilename.substring(extensionIndex + 1)) : "";
            if (!projectService.isAllowedProjectReportExtension(fileExtension)) {
                List<String> fileExtList = projectService.getAllowedProjectReportExtensions();
                String msg = getMessage("msg.project.packaging.upload.fileextension", new String[]{String.join(",", fileExtList)});
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, msg);
            }
            if (CoConstDef.CD_XLSX_UPLOAD_FILE_SIZE_LIMIT <= ossReport.getSize()) {
                return responseService.errorResponse(HttpStatus.PAYLOAD_TOO_LARGE, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE));
            }

            // 3. Distribution type check
            Map<String, Object> paramMap = new HashMap<>();
            List<String> prjIdList = new ArrayList<String>();
            prjIdList.add(prjId);
            paramMap.put("prjId", prjIdList);
            paramMap.put("distributionType", "normal");

            try {
                boolean checkDistributionTypeFlag = apiProjectService.checkDistributionType(paramMap);
                if (!checkDistributionTypeFlag) {
                    return responseService.errorResponse(HttpStatus.BAD_REQUEST, CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UPLOAD_TARGET_ERROR_MESSAGE) + " Check Project Distribution Type");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            // 4. Upload file (reuse old file id if any)
            // UI와 동일하게: 프로젝트의 현재 FILE_ID를 재사용하거나 새로 생성
            String oldFileId = "";
            Project prjInfo = apiProjectService.selectProjectMaster(prjId);
            
            // UI와 동일한 방식: IDENTIFICATION_CSV_FILE_ID (모든 탭이 공유하는 FILE_ID)
            String identificationCsvFileId = prjInfo.getIdentificationCsvFileId();
            if (!isEmpty(identificationCsvFileId)) {
                oldFileId = identificationCsvFileId;
            }

            UploadFile uploadFile;
            if (!isEmpty(oldFileId)) {
                uploadFile = apiFileService.uploadFileWithCreator(ossReport, userInfo.getUserId(), oldFileId);
            } else {
                uploadFile = apiFileService.uploadFileWithCreator(ossReport, userInfo.getUserId(), null);
            }

            if (uploadFile == null) {
                resultMap.put(KEY_ERROR_MESSAGE, "File information not found.");
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, (String) resultMap.get(KEY_ERROR_MESSAGE));
            }

            String registFileId = uploadFile.getRegistFileId();

            T2File registFile = fileService.selectFileInfoById(registFileId);

            String uploadFileSeq = registFile.getFileSeq();
            String uploadFileNm = registFile.getOrigNm();

            List<Map<String, Object>> uploadedList = new ArrayList<>();
            List<Map<String, Object>> errorList = new ArrayList<>();

            // 5. Save sheet data with sheet name
            Map<String, List<OssComponents>> reportDataMap = new HashMap<>();
            Map<String, String> errMsgListMap = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : tabSheetMap.entrySet()) {
                String tabName = entry.getKey().toUpperCase();
                List<String> targetSheetNames = entry.getValue();

//                int sheetIdx = 0;
//                int targetSheetCount = targetSheetNames.size();
                for (String targetSheetName : targetSheetNames) {
                    if (isEmpty(targetSheetName.trim())) {
                        continue;
                    }

                    // get sheet numbers that match the sheet name (exact match)
                    List<UploadFile> list = new ArrayList<UploadFile>();
                    list.add(uploadFile);
                    String[] sheetNumberArray = ArrayUtils.EMPTY_STRING_ARRAY;
                    try {
                        List<Object> excelSheets = ExcelUtil.getSheetNames(list, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
                        for (Object obj : excelSheets) {
                            Map<String, Object> sheetMap = (Map<String, Object>) obj;
                            if (sheetMap.containsKey("name")
                                    && sheetMap.get("name") instanceof String
                                    && targetSheetName.trim().equalsIgnoreCase(((String) sheetMap.get("name")).trim())) {
                                sheetNumberArray = new String[]{(String) sheetMap.get("no")};
                                break;
                            }
                        }
                        if (sheetNumberArray.length == 0) {
                            errMsgListMap.put(targetSheetName, "'" + targetSheetName.trim() + "' sheet not found in the uploaded file.");
                            continue;
                        }
                        Map<String, Object> sheetDataResult = apiProjectService.getSheetOriginalData(uploadFile, tabName, sheetNumberArray, true);


                        String errMsg = "";
                        String errorMessage = (String) sheetDataResult.get(KEY_ERROR_MESSAGE);
                        if (!isEmpty(errorMessage)) {
                            errMsg += errorMessage;
                        }
                        String emptyErrMsg = (String) sheetDataResult.get("emptyErrMsg");
                        if (!isEmpty(emptyErrMsg)) {
                            errMsg += emptyErrMsg;
                        }
                        if (!errMsg.isEmpty()) {
                            errMsgListMap.put(targetSheetName, errMsg);
                        } else {
                            reportDataMap.put(targetSheetName, (List<OssComponents>) sheetDataResult.get("reportData"));
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        errMsgListMap.put(targetSheetName, "Unexpected error occurred while reading the sheet '" + targetSheetName.trim() + "'.");
                    }

                }
            }

            boolean isDepLoaded = false;
            boolean isSrcLoaded = false;
            boolean isBinLoaded = false;

            int depComponentCount = 0;
            int srcComponentCount = 0;
            int binComponentCount = 0;

            List<ProjectIdentification> depOssComponents = new ArrayList<ProjectIdentification>();
            List<ProjectIdentification> srcOssComponents = new ArrayList<ProjectIdentification>();
            List<ProjectIdentification> binOssComponents = new ArrayList<ProjectIdentification>();
            List<String> versionChangedList = new ArrayList<>();

            // 6. Aggregate sheet data based on each tab
            for (Map.Entry<String, List<String>> entry : tabSheetMap.entrySet()) {
                String tabName = entry.getKey().toUpperCase();
                List<String> targetSheetNames = entry.getValue();

                if (MapUtils.isEmpty(reportDataMap)) {
                    //TODO: exception 처리
                }

                // loop all targetSheet in the tab
                for (String targetSheetName : targetSheetNames) {
                    if (isEmpty(targetSheetName.trim())) {
                        continue;
                    }
                    if (errMsgListMap.get(targetSheetName) != null) {
                        Map<String, Object> errorItem = new LinkedHashMap<>();
                        errorItem.put("name", targetSheetName.trim());
                        errorItem.put("reason", errMsgListMap.get(targetSheetName));
                        errorItem.put("tab", tabName);
                        errorList.add(errorItem);
                    }

                    int uploadedItemCount = 0;

                    if (tabName.toUpperCase().equals("DEP")) {
                        isDepLoaded = true;
                        Map<String, Object> gridDataMap = CommonFunction.makeGridDataFromReport(
                                null, null, null,
                                reportDataMap.get(targetSheetName), uploadFileSeq, "dep");

                        List<ProjectIdentification> reportData = (List<ProjectIdentification>) gridDataMap.get("mainData");
                        if (CollectionUtils.isNotEmpty(reportData)) {
                            for (ProjectIdentification oc : reportData) {
                                String comments = oc.getComments();
                                if (!isEmpty(comments)) {
                                    comments = comments + "\n";
                                }
                                comments += "(From " + uploadFileNm + ")";
                                oc.setComments(comments);
                                oc.setRefLoadedVal(uploadFileSeq);
                            }
                            uploadedItemCount = reportData.size();
                            depComponentCount += reportData.size();
                            depOssComponents.addAll(reportData);
                        }
                        if (MapUtils.isNotEmpty(gridDataMap) && gridDataMap.containsKey("versionChangedList")) {
                            if (versionChangedList == null) {
                                versionChangedList = new ArrayList<>();
                            }
                            versionChangedList.addAll((List<String>) gridDataMap.get("versionChangedList"));
                        }
                    } else if (tabName.toUpperCase().equals("SRC")) {
                        isSrcLoaded = true;
                        Map<String, Object> gridDataMap = CommonFunction.makeGridDataFromReport(
                                null, null, null,
                                reportDataMap.get(targetSheetName), uploadFileSeq, "src");

                        List<ProjectIdentification> reportData = (List<ProjectIdentification>) gridDataMap.get("mainData");
                        if (CollectionUtils.isNotEmpty(reportData)) {
                            for (ProjectIdentification oc : reportData) {
                                String comments = oc.getComments();
                                if (!isEmpty(comments)) {
                                    comments = comments + "\n";
                                }
                                comments += "(From " + uploadFileNm + ")";
                                oc.setComments(comments);
                                oc.setRefLoadedVal(uploadFileSeq);
                            }
                            uploadedItemCount = reportData.size();
                            srcComponentCount += reportData.size();
                            srcOssComponents.addAll(reportData);
                        }
                        if (MapUtils.isNotEmpty(gridDataMap) && gridDataMap.containsKey("versionChangedList")) {
                            if (versionChangedList == null) {
                                versionChangedList = new ArrayList<>();
                            }
                            versionChangedList.addAll((List<String>) gridDataMap.get("versionChangedList"));
                        }
                    } else if (tabName.toUpperCase().equals("BIN")) {
                        isBinLoaded = true;
                        Map<String, Object> gridDataMap = CommonFunction.makeGridDataFromReport(
                                null, null, null,
                                reportDataMap.get(targetSheetName), uploadFileSeq, "bin");

                        List<ProjectIdentification> reportData = (List<ProjectIdentification>) gridDataMap.get("mainData");
                        if (CollectionUtils.isNotEmpty(reportData)) {
                            for (ProjectIdentification oc : reportData) {
                                String comments = oc.getComments();
                                if (!isEmpty(comments)) {
                                    comments = comments + "\n";
                                }
                                comments += "(From " + uploadFileNm + ")";
                                oc.setComments(comments);
                                oc.setRefLoadedVal(uploadFileSeq);
                            }
                            uploadedItemCount = reportData.size();
                            binComponentCount += reportData.size();
                            binOssComponents.addAll(reportData);
                        }
                        if (MapUtils.isNotEmpty(gridDataMap) && gridDataMap.containsKey("versionChangedList")) {
                            if (versionChangedList == null) {
                                versionChangedList = new ArrayList<>();
                            }
                            versionChangedList.addAll((List<String>) gridDataMap.get("versionChangedList"));
                        }
                    }

                    if (reportDataMap.containsKey(targetSheetName)) {
                        Map<String, Object> uploadedItem = new LinkedHashMap<>();
                        uploadedItem.put("sheet_name", targetSheetName.trim());
                        uploadedItem.put("count", uploadedItemCount);
                        uploadedItem.put("tab", tabName);
                        uploadedList.add(uploadedItem);
                    }

                }
            }

            String versionChangedStr = "";
            if (versionChangedList != null) {
                versionChangedStr = "<b>The following open source version below has been changed to a registered version</b><br><br>";
                for (String s : versionChangedList) {
                    versionChangedStr += "<br>" + s;
                }
            }
            Project project = projectService.getProjectBasicInfo(prjId);
            project.setLoginUserName(userInfo.getUserId());
            project.setModifier(userInfo.getUserId());
            if (isDepLoaded) {
                // Prepend existing DEP components from DB so new data is appended
                List<ProjectIdentification> existingDep = new ArrayList<>();
                List<List<ProjectIdentification>> existingDepLicense = new ArrayList<>();
                apiProjectService.getIdentificationGridList(prjId, CoConstDef.CD_DTL_COMPONENT_ID_DEP, existingDep, existingDepLicense, null);
                existingDep.addAll(depOssComponents);
                depOssComponents = existingDep;

                List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(depOssComponents);
                Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(depOssComponents, ossComponentsLicense);
                depOssComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
                ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
                projectService.registCommentWithNickNameValid(prjId, depOssComponents, ossComponentsLicense, CoConstDef.CD_DTL_COMPONENT_ID_DEP, userInfo.getUserId());
                projectService.registDepOss(depOssComponents, ossComponentsLicense, project, true);
            }
            if (isSrcLoaded) {
                // Prepend existing SRC components from DB so new data is appended
                List<ProjectIdentification> existingSrc = new ArrayList<>();
                List<List<ProjectIdentification>> existingSrcLicense = new ArrayList<>();
                apiProjectService.getIdentificationGridList(prjId, CoConstDef.CD_DTL_COMPONENT_ID_SRC, existingSrc, existingSrcLicense, null);
                existingSrc.addAll(srcOssComponents);
                srcOssComponents = existingSrc;

                List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(srcOssComponents);
                Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(srcOssComponents, ossComponentsLicense);
                srcOssComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
                ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
                projectService.registCommentWithNickNameValid(prjId, srcOssComponents, ossComponentsLicense, CoConstDef.CD_DTL_COMPONENT_ID_SRC, userInfo.getUserId());
                projectService.registSrcOss(srcOssComponents, ossComponentsLicense, project, CoConstDef.CD_DTL_COMPONENT_ID_SRC, true);
            }
            if (isBinLoaded) {
                // Prepend existing BIN components from DB so new data is appended
                List<ProjectIdentification> existingBin = new ArrayList<>();
                List<List<ProjectIdentification>> existingBinLicense = new ArrayList<>();
                apiProjectService.getIdentificationGridList(prjId, CoConstDef.CD_DTL_COMPONENT_ID_BIN, existingBin, existingBinLicense, null);
                existingBin.addAll(binOssComponents);
                binOssComponents = existingBin;

                List<List<ProjectIdentification>> ossComponentsLicense = CommonFunction.setOssComponentLicense(binOssComponents);
                Map<String, Object> remakeComponentsMap = CommonFunction.remakeMutiLicenseComponents(binOssComponents, ossComponentsLicense);
                binOssComponents = (List<ProjectIdentification>) remakeComponentsMap.get("mainList");
                ossComponentsLicense = (List<List<ProjectIdentification>>) remakeComponentsMap.get("subList");
                projectService.registCommentWithNickNameValid(prjId, binOssComponents, ossComponentsLicense, CoConstDef.CD_DTL_COMPONENT_ID_BIN, userInfo.getUserId());
                projectService.registSrcOss(binOssComponents, ossComponentsLicense, project, CoConstDef.CD_DTL_COMPONENT_ID_BIN, true);
            }
            if (registFile != null) {
                projectService.setFileAddList(registFile, project, CoConstDef.CD_DTL_COMPONENT_ID_BOM,
                        depComponentCount, srcComponentCount, binComponentCount, isDepLoaded, isSrcLoaded, isBinLoaded);
            }

            // session cleanup per tab
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_DEP, prjId));
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_DEP, prjId));
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_SRC, prjId));
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_SRC, prjId));
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.CD_DTL_COMPONENT_ID_BIN, prjId));
            deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_PROJECT_BIN, prjId));

            // 7. SBOM save
            apiProjectService.registBom(prjId, CoConstDef.FLAG_YES, userInfo.getUserId());
            projectService.updateSecurityDataForProject(prjId);

            String uploadLogComment = "OSS Report Uploaded (by API)";

            CommentsHistory commentHisBean = new CommentsHistory();
            commentHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS);
            commentHisBean.setReferenceId(prjId);
            commentHisBean.setContents((comment == null ? "" : comment + "<br>") + uploadLogComment);
            commentHisBean.setLoginUserName(userInfo.getUserId());
            commentService.registComment(commentHisBean, false);


            try {
                History h = new History();
                h = projectService.work(project);
                h.sethAction(CoConstDef.ACTION_CODE_UPDATE);
                h.setLoginUserName(userInfo.getUserId());
                project = (Project) h.gethData();
                h.sethEtc(project.etcStr());
                historyService.storeData(h);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            resultMap.put("success", true);
            resultMap.put("uploaded", uploadedList);
            resultMap.put("error", errorList);

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Packaging 파일 업로드", notes = "프로젝트 Verification에 Packaging 파일을 추가합니다. 프로젝트당 최대 3개이며 verifyFlag가 Y이면 업로드 후 검증도 실행합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "업로드 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "파일 누락 또는 업로드 가능 개수 초과\n\n* Multipart 파일 파라미터 누락 메시지\n* `Up to 3 packaging files can be uploaded.`", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Up to 3 packaging files can be uploaded.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))),
            @ApiResponse(code = 500, message = "업로드·검증 또는 서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PACKAGE_UPLOAD})
    public ResponseEntity<Map<String, Object>> ossUploadPackage(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Package FIle", required = true) @RequestPart(required = true) MultipartFile packageFile,
            @ApiParam(value = "Verify when file is uploaded (YES : Y, NO : N)", allowableValues = "Y,N") @RequestParam(required = false, defaultValue = "N") String verifyFlag) {

        Map<String, Object> resultMap = new HashMap<String, Object>(); // 성공, 실패에 대한 정보를 return하기 위한 map;

        T2Users userInfo = userService.checkApiUserAuth(authorization); // token이 정상적인 값인지 확인
        if (!apiProjectService.checkUserAvailableToEditProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", prjId));
        }

        String errorMsg = "";
        String afterFileSeq = "";
        boolean uploadFlag = false;

        Map<String, Object> check_result = apiProjectService.selectVerificationCheck(prjId);
        String useYn = (String) check_result.get("useYn");
        String packageFileSeq = (String) check_result.get("packageFileSeq").toString();

        if (CoConstDef.CD_OPEN_API_PACKAGE_FILE_LIMIT < Integer.parseInt(packageFileSeq)) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Up to 3 packaging files can be uploaded.");
        }

        String filePath = CommonFunction.emptyCheckProperty("packaging.path", "/upload/packaging") + "/" + prjId;
        UploadFile packageFileBean = apiFileService.uploadFile(packageFile, filePath); // packagingFile 등록
        afterFileSeq = packageFileBean.getRegistSeq();

        if (CoConstDef.FLAG_YES.equals(useYn) && CoConstDef.CD_OPEN_API_PACKAGE_FILE_LIMIT >= Integer.parseInt(packageFileSeq)) {
            // packaging File comment
            String uploadComment = "Packaging file, " + packageFileBean.getOriginalFilename() + ", was uploaded by " + userInfo.getUserId() + ". <br>";

            Map<String, Object> paramMap = new HashMap<String, Object>();
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
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
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
                return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "프로젝트 Editor 추가", notes = "프로젝트에 수정 권한을 가진 사용자를 한 명 이상 추가합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "추가 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "Editor ID 목록 누락", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Editor ID list is required.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))),
            @ApiResponse(code = 404, message = "추가할 사용자 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"User not found in FOSSLight Hub. User ID: user01\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_ADD_EDITOR})
    public ResponseEntity<Map<String, Object>> addPrjEditor(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project Id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Editor Id", required = true) @RequestParam(required = true) String[] idList) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", prjId));
        }

        if (idList == null) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Editor ID list is required.");
        }

        for (String id : idList) {
            T2Users targetUser = new T2Users();
            targetUser.setUserId(id);
            T2Users existingUser = userService.getUser(targetUser);
            if (existingUser == null) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND, "User not found in FOSSLight Hub. User ID: " + id);
            }
            Map<String, Object> param = new HashMap<>();
            param.put("prjId", prjId);
            param.put("division", existingUser.getDivision());
            param.put("userId", existingUser.getUserId());
            apiProjectService.insertWatcher(param);
        }
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @ApiOperation(value = "Security 담당자 지정", notes = "프로젝트의 Security responsible person을 사용자 ID로 지정합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "지정 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Security responsible person added successfully\",\"userId\":\"user01\",\"userName\":\"홍길동\"}"))),
            @ApiResponse(code = 400, message = "User ID 누락", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"User ID is required.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))),
            @ApiResponse(code = 404, message = "사용자 또는 프로젝트 없음\n\n* `User not found in FOSSLight Hub. User ID: {id}`\n* `Project not found. Project ID: {id}`", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"User not found in FOSSLight Hub. User ID: user01\"}"))),
            @ApiResponse(code = 500, message = "담당자 지정 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Failed to add security responsible person: database error\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_ADD_SECURITY_PERSON})
    public ResponseEntity<Map<String, Object>> addSecurityPerson(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project Id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "User ID", required = true) @RequestParam(required = true) String userId) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", prjId));
        }

        try {
            if (StringUtils.isEmpty(userId)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "User ID is required.");
            }

            // T2_Users 테이블에서 사용자 존재 여부 확인
            T2Users targetUser = new T2Users();
            targetUser.setUserId(userId);
            T2Users existingUser = userService.getUser(targetUser);

            if (existingUser == null) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND, "User not found in FOSSLight Hub. User ID: " + userId);
            }

            // 기존 프로젝트 정보 조회
            Project project = projectService.getProjectBasicInfo(prjId);
            Project beforeProject = projectService.getProjectBasicInfo(prjId);
            Project afterProject = null;
            if (project == null) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND, "Project not found. Project ID: " + prjId);
            }

            // Security responsible person 설정
            project.setSecPerson(userId);
            project.setSecPersonNm(existingUser.getUserName());

            // 프로젝트 업데이트
            projectService.updateSecurityPerson(project);

            afterProject = projectService.getProjectBasicInfo(prjId);
            String diffComment = CommonFunction.getDiffItemComment(beforeProject, afterProject, true);

            try {
                CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED);
                mailBean.setParamPrjId(project.getPrjId());
                mailBean.setCompareDataBefore(beforeProject);
                mailBean.setCompareDataAfter(afterProject);
                mailBean.setLoginUserName(userInfo.getUserId());


                if (!isEmpty(diffComment)) {
                    CommentsHistory commHisBean = new CommentsHistory();
                    commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
                    commHisBean.setReferenceId(project.getPrjId());
                    commHisBean.setContents(diffComment);
                    commHisBean.setStatus("Changed");
                    commentService.registComment(commHisBean);
                }
                CoMailManager.getInstance().sendMail(mailBean);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            resultMap.put("msg", "Security responsible person added successfully");
            resultMap.put("userId", userId);
            resultMap.put("userName", existingUser.getUserName());

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error adding security responsible person: " + e.getMessage(), e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add security responsible person: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Security Mail 설정", notes = "프로젝트 Security Mail 사용 여부를 설정합니다. N인 경우 secMailDesc가 필수입니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "설정 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Security enable setting updated successfully\",\"secMailYn\":\"Y\",\"secMailDesc\":\"Enable\"}"))),
            @ApiResponse(code = 400, message = "Security Mail 파라미터 오류\n\n* `Security Enable (secMailYn) is required.`\n* `Security Enable (secMailYn) must be Y or N.`\n* `Security Description (secMailDesc) is required when Security Enable is N.`", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Security Enable (secMailYn) must be Y or N.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))),
            @ApiResponse(code = 404, message = "프로젝트 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Project not found. Project ID: 123\"}"))),
            @ApiResponse(code = 500, message = "설정 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Failed to set security enable: database error\"}")))
    })
    @PostMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_SET_SECURITY_MAIL})
    public ResponseEntity<Map<String, Object>> setSecurityMail(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Project Id", required = true) @PathVariable(name = "id") String prjId,
            @ApiParam(value = "Security Enable (Y: Enable, N: Disable)", required = true, allowableValues = "Y,N") @RequestParam(required = true) String secMailYn,
            @ApiParam(value = "Security Description (Required when secMailYn is N)", required = false) @RequestParam(required = false) String secMailDesc) {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<>();

        userService.changeSession(userInfo.getUserId());

        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", prjId));
        }

        try {
            if (StringUtils.isEmpty(secMailYn)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Security Enable (secMailYn) is required.");
            }

            if (!secMailYn.equals("Y") && !secMailYn.equals("N")) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Security Enable (secMailYn) must be Y or N.");
            }

            // Security Enable이 N인 경우 설명이 필수
            if (secMailYn.equals("N") && StringUtils.isEmpty(secMailDesc)) {
                return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Security Description (secMailDesc) is required when Security Enable is N.");
            }

            // 기존 프로젝트 정보 조회
            Project project = projectService.getProjectBasicInfo(prjId);
            Project beforeProject = projectService.getProjectBasicInfo(prjId);
            Project afterProject = null;
            if (project == null) {
                return responseService.errorResponse(HttpStatus.NOT_FOUND, "Project not found. Project ID: " + prjId);
            }

            // Security enable 설정
            Project param = new Project();
            param.setPrjId(prjId);
            param.setSecMailYn(secMailYn);
            if (secMailYn.equals("N")) {
                param.setSecMailDesc(secMailDesc);
            } else {
                param.setSecMailDesc("Enable");
            }

            // 프로젝트 업데이트
            projectService.updateProjectMaster(param);

            afterProject = projectService.getProjectBasicInfo(prjId);
            String diffComment = CommonFunction.getDiffItemComment(beforeProject, afterProject, true);

            try {
                CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED);
                mailBean.setParamPrjId(project.getPrjId());
                mailBean.setCompareDataBefore(beforeProject);
                mailBean.setCompareDataAfter(afterProject);
                mailBean.setLoginUserName(userInfo.getUserId());

                if (!isEmpty(diffComment)) {
                    CommentsHistory commHisBean = new CommentsHistory();
                    commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
                    commHisBean.setReferenceId(project.getPrjId());
                    commHisBean.setContents(diffComment);
                    commHisBean.setStatus("Changed");
                    commentService.registComment(commHisBean);
                }
                CoMailManager.getInstance().sendMail(mailBean);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            resultMap.put("msg", "Security enable setting updated successfully");
            resultMap.put("secMailYn", secMailYn);
            resultMap.put("secMailDesc", project.getSecMailDesc());

            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error setting security enable: " + e.getMessage(), e);
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to set security enable: " + e.getMessage());
        }
    }

    @ApiOperation(value = "프로젝트 Notice 다운로드", notes = "발행된 프로젝트 Notice HTML 파일을 다운로드합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "파일 다운로드 성공", response = FileSystemResource.class),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 404, message = "발행된 Notice 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Notice has not been published for given project.\"}"))),
            @ApiResponse(code = 500, message = "파일 처리 또는 서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @GetMapping(value = {APIV2.FOSSLIGHT_API_PROJECT_GET_NOTICE})
    public ResponseEntity getProjectNotice(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "project ID", required = false) @PathVariable(required = true, name = "id") String prjId,
            HttpServletRequest req
    ) throws Exception {

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(prjId);
        }

        OssNotice ossNotice = verificationService.selectOssNoticeOne(prjId);

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
    }


    @ApiOperation(value = "다른 프로젝트 OSS 불러오기", notes = "프로젝트 ID 또는 이름으로 원본 프로젝트를 찾아 대상 프로젝트의 dep, src 또는 bin 탭으로 OSS 컴포넌트를 불러옵니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "불러오기 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{}"))),
            @ApiResponse(code = 400, message = "ID·검색조건·탭 오류 또는 불러올 데이터 없음\n\n* `targetPrjId is not in the correct format`\n* `the prjIdToLoad is missing`\n* `prjIdToLoad is not in the correct format`\n* `Please enter other prjIdToLoad that is different from targetPrjId`\n* `the prjNameToLoad is missing`\n* 서비스에서 반환한 상세 오류 메시지", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"the prjIdToLoad is missing\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123. Check Permission or Project Status\"}"))),
            @ApiResponse(code = 500, message = "서버 내부 오류", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @PostMapping(value = {Url.APIV2.FOSSLIGHT_API_OSS_LOAD})
    public ResponseEntity<Map<String, Object>> ossLoad(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Target Project ID", required = true) @PathVariable(name = "id") String targetPrjId,
            @ApiParam(value = "Load Target Tab Name (Valid Input: dep, src, bin)", required = true, allowableValues = "dep, src, bin")
            @ValuesAllowed(propName = "tabName", values = {"dep", "src", "bin"}) @PathVariable(name = "tab_name") String tabName,
            @ApiParam(value = "Search Condition (Project ID : id, Project Name : name)", required = true, allowableValues = "id,name")
            @ValuesAllowed(propName = "searchCondition", values = {"id", "name"}) @RequestParam(required = true) String searchCondition,
            @ApiParam(value = "Project ID to Load") @RequestParam(required = false) String prjIdToLoad,
            @ApiParam(value = "Project Name to Load") @RequestParam(required = false) String prjNameToLoad,
            @ApiParam(value = "Project Version to Load") @RequestParam(required = false) String prjVersionToLoad,
            @ApiParam(value = "Reset Flag (YES : Y, NO : N)", allowableValues = "Y, N")
            @ValuesAllowed(propName = "resetFlag", values = {"Y", "N"}) @RequestParam(required = false, defaultValue = "Y") String resetFlag) {

        log.error("/api/v2/oss_load called:" + targetPrjId);

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String errorMsgCode = CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE;

        if (!apiProjectService.checkUserAvailableToEditProject(userInfo, targetPrjId)) {
            throw new CProjectNotAvailableException(String.format("%s. Check Permission or Project Status", targetPrjId));
        }

        Map<String, Object> paramMap = new HashMap<>();

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

            switch (tabName) {
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
    }

    @ApiOperation(value = "프로젝트 삭제", notes = "프로젝트 생성자, Editor 또는 관리자가 프로젝트를 삭제합니다. 일반 사용자는 배포 중이거나 배포 완료된 프로젝트를 삭제할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "삭제 성공", response = Map.class, examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"123 is deleted\"}"))),
            @ApiResponse(code = 400, message = "삭제 권한 없음 또는 배포 프로젝트\n\n* `Cannot delete project.`\n* `Cannot delete distributed project.`", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Cannot delete distributed project.\"}"))),
            @ApiResponse(code = 401, message = "인증 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"There is an error in the TOKEN value.\"}"))),
            @ApiResponse(code = 403, message = "프로젝트 수정 권한 없음", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"The user does not have edit permissions for Project 123\"}"))),
            @ApiResponse(code = 500, message = "프로젝트 또는 참조 파일 삭제 실패", examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"msg\":\"Unknown error.\"}")))
    })
    @DeleteMapping(value = {Url.APIV2.FOSSLIGHT_API_PROJECT_BY_ID})
    public ResponseEntity<Map<String, Object>> deleteProject(
            @ApiParam(hidden = true) @RequestHeader String authorization,
            @ApiParam(value = "Target Project ID", required = true) @PathVariable(name = "id") String prjId
    ) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        T2Users userInfo = userService.checkApiUserAuth(authorization);
        if (!apiProjectService.checkUserHasProject(userInfo, prjId)) {
            throw new CProjectNotAvailableException(prjId);
        }

        Project project = new Project();
        project.setPrjId(prjId);
        Project projectInfo = projectService.getProjectDetail(project);

        boolean hasPermission = false;
        List<String> permissionCheckUserList = new ArrayList<>();
        if (!isEmpty(projectInfo.getCreator())) {
            permissionCheckUserList.add(projectInfo.getCreator());
        }
        if (CollectionUtils.isNotEmpty(projectInfo.getWatcherList())) {
            for (Project watcher : projectInfo.getWatcherList()) {
                if (!permissionCheckUserList.contains(watcher.getPrjUserId())) {
                    permissionCheckUserList.add(watcher.getPrjUserId());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(permissionCheckUserList)) {
            for (String checkUserId : permissionCheckUserList) {
                if (checkUserId.equalsIgnoreCase(userInfo.getUserId())) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!userInfo.getAuthority().equalsIgnoreCase("ROLE_ADMIN") && !hasPermission) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Cannot delete project.");
        } else if (!userInfo.getAuthority().equalsIgnoreCase("ROLE_ADMIN") && hasPermission
                && (Objects.equals(projectInfo.getDistributionStatus(), CoConstDef.CD_DTL_DISTRIBUTE_STATUS_DEPLOIDED) || Objects.equals(projectInfo.getDistributionStatus(), CoConstDef.CD_DTL_DISTRIBUTE_STATUS_PROCESS))) {
            return responseService.errorResponse(HttpStatus.BAD_REQUEST, "Cannot delete distributed project.");
        }

        try {
            History h = new History();
            h = projectService.work(project);
            h.sethAction(CoConstDef.ACTION_CODE_DELETE);
            h.setModifier(userInfo.getUserId());
            historyService.storeData(h);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_DELETED);
            mailBean.setLoginUserName(userInfo.getUserId());
            mailBean.setLoginUserRole(userInfo.getAuthority());
            mailBean.setParamPrjId(project.getPrjId());
            mailBean.setParamPrjInfo(projectInfo);

            if (!isEmpty(project.getUserComment())) {
                mailBean.setComment(project.getUserComment());
            }

            CoMailManager.getInstance().sendMail(mailBean);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            projectService.deleteProject(project);

            try {
                // Delete project ref files
                projectService.deleteProjectRefFiles(projectInfo);
                resultMap.put("msg", prjId + " is deleted");
                return new ResponseEntity<>(resultMap, HttpStatus.OK);
            } catch (Exception e) {
                log.error(e.getMessage());
                resultMap.put("msg", "Error occurs during remove ref files. Please report this issue");
                return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultMap.put("msg", "Error occurs during remove ref files. Please report this issue");
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
