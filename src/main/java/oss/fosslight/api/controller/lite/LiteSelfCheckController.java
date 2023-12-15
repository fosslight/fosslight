package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.*;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.service.ApiSelfCheckService;
import oss.fosslight.service.ApiVerificationService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.SelfCheckService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteSelfCheckController extends CoTopComponent {
    @Autowired
    private SelfCheckService selfCheckService;

    @Autowired
    private ApiSelfCheckService apiSelfCheckService;

    @Autowired
    private ApiVerificationService apiVerificationService;

    @Autowired
    private CodeMapper codeMapper;

    @Autowired
    private FileService fileService;

    @GetMapping("/selfchecks")
    public @ResponseBody ResponseEntity<ListSelfCheckDto.Result> list(
            @ModelAttribute ListSelfCheckDto.Request request
    ) {
        try {
            var result = apiSelfCheckService.listSelfChecks(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/selfchecks/delete")
    public @ResponseBody ResponseEntity<Object> delete(
            @ModelAttribute Project project,
            HttpServletRequest req,
            HttpServletResponse res,
            Model model
    ) {
        selfCheckService.deleteProject(project);

        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("resCd", "10");

        return makeJsonResponseHeader(resMap);
    }


    @GetMapping("/selfchecks/{id}/list-oss")
    public @ResponseBody ResponseEntity<ListSelfCheckOssDto.Result> listOss(
            @PathVariable("id") String id
    ) {
        var projectQuery = new Project();
        projectQuery.setPrjId(id);
        try {
            var project = selfCheckService.getProjectDetail(projectQuery);
            var files = project.getCsvFile();
            var fileList = files.stream()
                    .filter(file -> "N".equals(file.getDelYn()))
                    .map(this::getFileInfoMap)
                    .collect(Collectors.toList());
            var ossList = apiSelfCheckService.listSelfCheckOss(id);
            var fileId = project.getSrcCsvFileId();
            var result = ListSelfCheckOssDto.Result.builder()
                    .oss(ossList)
                    .files(fileList)
                    .fileId(fileId == null ? "" : fileId)
                    .build();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/selfchecks/{id}")
    public @ResponseBody ResponseEntity<GetSelfCheckDetailsDto.Result> selfCheckDetail(
            @PathVariable("id") String id
    ) {
        try {
            var result = apiSelfCheckService.getSelfCheck(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/selfchecks/{id}/packages")
    public @ResponseBody ResponseEntity<ListSelfCheckVerifyOssDto.Result> selfCheckPackage(
            @PathVariable("id") String id
    ) {
        try {
            var result = apiVerificationService.getVerifyOssList(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/selfchecks/{id}/packages/files")
    public @ResponseBody ResponseEntity<SelfCheckFileListDto.Response> packageFiles(
            @PathVariable("id") String id
    ) {
        try {
            var packages = apiSelfCheckService.listSelfCheckPackages(id);
            var list = packages.stream()
                    .map(this::getFileInfoMap)
                    .collect(Collectors.toList());
            var result = SelfCheckFileListDto.Response.builder()
                    .files(list)
                    .build();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/selfchecks/{id}/packages/files")
    public @ResponseBody ResponseEntity<Object> registerFile(
            @PathVariable("id") String id,
            MultipartHttpServletRequest req
    ) {
        var fileMap = req.getFileMap();
        var multipartFile = fileMap.get("selfCheckPackageFile");
        T2File file = new T2File();
        String filePath = CommonFunction.emptyCheckProperty(
                "selfcheck.packaging.path", "/upload/selfcheck/packaging"
        ) + "/" + id;

        String fileExtension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());

        var allowedExtension = Arrays.stream(codeMapper.getCodeDetail(
                CoConstDef.CD_FILE_ACCEPT,
                CoConstDef.CD_DTL_COMPONENT_ID_DEP
        ).getCdDtlExp().split(",")).toArray();

        if (!Arrays.asList(allowedExtension).contains(fileExtension)) {
            String msg = getMessage("msg.project.packaging.upload.fileextension", allowedExtension);
            return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
        }

        file.setCreator(loginUserName());

        List<UploadFile> uploadedList = fileService.uploadFile(
                req, file, null, true, filePath);

        try {
            var fileList = apiVerificationService.saveUploadedFile(id, uploadedList)
                    .stream().map(this::getFileInfoMap)
                    .collect(Collectors.toList());
            var result = SelfCheckFileListDto.Response.builder()
                    .files(fileList)
                    .build();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/selfchecks/{id}/packages/files")
    public @ResponseBody ResponseEntity<SelfCheckFileDeleteDto.Response> deleteFile(
            @ModelAttribute SelfCheckFileDeleteDto.Request request,
            @PathVariable("id") String id
    ) {
        var fileId = request.getFileId();
        try {
            var list = apiVerificationService.deleteSelfCheckPackageFile(id, fileId)
                    .stream().map(this::getFileInfoMap)
                    .collect(Collectors.toList());
            var result = SelfCheckFileDeleteDto.Response.builder()
                    .files(list)
                    .build();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private FileDto getFileInfoMap(T2File file) {
        return FileDto.builder()
                .orgNm(file.getOrigNm())
                .created(file.getCreatedDate())
                .fileId(file.getFileId())
                .logiNm(file.getLogiNm())
                .fileSeq(file.getFileSeq())
                .build();
    }
}
