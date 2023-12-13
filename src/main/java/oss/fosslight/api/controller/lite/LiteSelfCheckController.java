package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.ListSelfCheckDto;
import oss.fosslight.api.dto.ListSelfCheckOssDto;
import oss.fosslight.common.Url;
import oss.fosslight.domain.Project;
import oss.fosslight.service.ApiSelfCheckService;
import oss.fosslight.service.SelfCheckService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteSelfCheckController extends CoTopComponent {
    @Autowired
    private SelfCheckService selfCheckService;

    @Autowired
    private ApiSelfCheckService apiSelfCheckService;

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
        try {
            var result = apiSelfCheckService.listSelfCheckOss(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }

    }

}
