package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.api.dto.GetLicenseDetailsDto;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.common.Url;
import oss.fosslight.controller.LicenseController;
import oss.fosslight.repository.ApiLicenseMapper;
import oss.fosslight.service.ApiLicenseService;
import oss.fosslight.service.ApiOssService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteLicenseController {
    @Autowired
    ApiLicenseService apiLicenseService;

    @Autowired
    ApiLicenseMapper apiLicenseMapper;

    @GetMapping("/licenses")
    public @ResponseBody ResponseEntity<ListLicenseDto.Result> getPage(
            @ModelAttribute ListLicenseDto.Request licenseRequest
    ) {
        try {
            var result = apiLicenseService.listLicenses(licenseRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/licenses/{id}")
    public @ResponseBody ResponseEntity<GetLicenseDetailsDto.Result> getLicense(
            @PathVariable("id")String id
    ) {
        try {
            var result = apiLicenseService.getLicense(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/licenses/autocomplete")
    public @ResponseBody ResponseEntity<List<String>> getAutocompleteCandidates(
            @RequestParam("query") String query
    ) {
        try {
            var result = apiLicenseMapper.getLicenseAutocompleteCandidates(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
