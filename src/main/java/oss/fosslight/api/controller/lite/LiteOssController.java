package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.common.Url;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.service.OssService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteOssController {
    @Autowired
    OssService ossService;

    private class SortParams {
        String column;
        String direction;
    }

    @GetMapping("/oss")
    public @ResponseBody ResponseEntity<Map<String, Object>> getPage(
            @RequestParam int page,
            @RequestParam int countPerPage,
            @RequestParam(required = false) String homepageUrl,
//            OssQueryDTO query
            OssMaster ossQuery,
            SortParams sort
    ) {
        var resultMap = new HashMap<String, Object>();
        var homepage = homepageUrl.replaceFirst("^((http|https)://)?(www\\.)?", "");
        homepage = homepage.replaceFirst("/$", "");

        ossQuery.setCurPage(page);
        ossQuery.setPageListSize(countPerPage);
        ossQuery.setHomepage(homepage);
        ossQuery.setSidx(sort.column);
        ossQuery.setSord(sort.direction);

        resultMap.put("result", "test success");
        resultMap.put("page", page);
        resultMap.put("name", ossQuery.getOssName());

        try {
            var map = ossService.getOssMasterList(ossQuery);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

        return ResponseEntity.ok(resultMap);
    }
}
