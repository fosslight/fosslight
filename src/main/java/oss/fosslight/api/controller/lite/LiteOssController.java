package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.api.dto.GetOSSDetailsDto;
import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.common.Url;
import oss.fosslight.service.ApiOssService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteOssController {
    @Autowired
    ApiOssService ossService;

    @GetMapping("/oss")
    public @ResponseBody ResponseEntity<ListOssDto.Result> getPage(
            @ModelAttribute ListOssDto.Request ossQuery
    ) {
        try {
            var map = ossService.listOss(ossQuery);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/oss/{id}")
    public @ResponseBody ResponseEntity<GetOSSDetailsDto.Result> getOss(
            @PathVariable("id") String id
    ) {
        try {
            var result = ossService.getOss(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
