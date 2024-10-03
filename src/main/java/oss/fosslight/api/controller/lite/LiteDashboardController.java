package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.ListLicenseDto;
import oss.fosslight.api.dto.ListVulnerabilityDto;
import oss.fosslight.api.dto.SearchAllDto;
import oss.fosslight.repository.ApiLicenseMapper;
import oss.fosslight.repository.ApiVulnerabilityMapper;
import oss.fosslight.service.ApiOssService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/lite")
public class LiteDashboardController extends CoTopComponent {
    @Autowired
    ApiLicenseMapper apiLicenseMapper;

    @Autowired
    ApiVulnerabilityMapper apiVulnerabilityMapper;

    @Autowired
    ApiOssService apiOssService;

    @GetMapping("/dashboard/search")
    public @ResponseBody ResponseEntity<SearchAllDto.Response> searchAll(
            @RequestParam String query
    ) {
        final int LIMIT = 5;
        try {
            var ossSearchResults = apiOssService.listNameSearchResult(query, LIMIT);
            var licenseQuery = ListLicenseDto.Request.builder()
                    .licenseName(query)
                    .build();
            licenseQuery.setLimit(LIMIT);
            var licenseSearchResults = apiLicenseMapper.selectLicenseList(licenseQuery);
            var vulnerabilityQuery = ListVulnerabilityDto.Request.builder()
                    .cveId(query)
                    .build();
            vulnerabilityQuery.setLimit(LIMIT);
            var vulnerabilitySearchResults = apiVulnerabilityMapper.selectVulnerabilityList(vulnerabilityQuery);
            return ResponseEntity.ok(SearchAllDto.Response.builder()
                    .oss(ossSearchResults)
                    .licenses(licenseSearchResults)
                    .vulnerabilities(vulnerabilitySearchResults)
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard")
    public @ResponseBody ResponseEntity<SearchAllDto.Response> dashboard(
    ) {
        final int LIMIT = 5;
        try {
            var ossSearchResults = apiOssService.listRecentOss(LIMIT);
            var licenseSearchResults = apiLicenseMapper.selectRecentLicenses(LIMIT);
            var vulnerabilitySearchResults = apiVulnerabilityMapper.selectRecentVulnerabilities(LIMIT);
            return ResponseEntity.ok(SearchAllDto.Response.builder()
                    .oss(ossSearchResults)
                    .licenses(licenseSearchResults)
                    .vulnerabilities(vulnerabilitySearchResults)
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
