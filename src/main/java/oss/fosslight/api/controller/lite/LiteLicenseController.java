package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.api.dto.GetLicenseDetailsDto;
import oss.fosslight.api.dto.LicenseDto;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.common.Url;
import oss.fosslight.repository.ApiLicenseMapper;
import oss.fosslight.service.ApiLicenseService;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/licenses/export/excel")
    public @ResponseBody ResponseEntity<String> getExport(
            @ModelAttribute ListLicenseDto.Request licenseRequest
    ) {
        try {
            var id = apiLicenseService.getLicenseExcel(licenseRequest);
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/licenses/{id}")
    public @ResponseBody ResponseEntity<GetLicenseDetailsDto.Result> getLicense(
            @PathVariable String id
    ) {
        try {
            var result = apiLicenseService.getLicense(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/licenses/candidates/all")
    public @ResponseBody ResponseEntity<List<List<String>>> getAutocompleteCandidates() {
        try {
            var result = apiLicenseMapper.getLicenseAutocompleteCandidates()
                    .stream().map(license ->
                            List.of(license.getLicenseName(), license.getLicenseIdentifier())
                    ).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
