package oss.fosslight.api.controller.lite;

import io.swagger.models.License;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.*;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.repository.*;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/lite")
public class LiteDashboardController extends CoTopComponent {
    @Autowired
    ApiOssMapper apiOssMapper;

    @Autowired
    ApiLicenseMapper apiLicenseMapper;

    @Autowired
    ApiVulnerabilityMapper apiVulnerabilityMapper;

    @GetMapping("/dashboard/search")
    public @ResponseBody ResponseEntity<SearchAllDto.Response> searchAll(
            @RequestParam("query") String query
    ) {
        final int LIMIT = 10;
        try {
            var ossQuery = ListOssDto.Request.builder()
                    .ossName(query)
                    .build();
            ossQuery.setLimit(LIMIT);
            var ossSearchResults = apiOssMapper.selectOssList(ossQuery);
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
        try {
            var ossSearchResults = apiOssMapper.selectRecentOss();
            var licenseSearchResults = apiLicenseMapper.selectRecentLicenses();
            var vulnerabilitySearchResults = apiVulnerabilityMapper.selectRecentVulnerabilities();
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
