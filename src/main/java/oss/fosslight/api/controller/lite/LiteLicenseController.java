package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.api.dto.ListOssDto;
import oss.fosslight.common.Url;
import oss.fosslight.controller.LicenseController;
import oss.fosslight.service.ApiLicenseService;
import oss.fosslight.service.ApiOssService;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteLicenseController {
    @Autowired
    ApiLicenseService apiLicenseService;

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
}
