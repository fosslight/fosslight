package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import oss.fosslight.api.dto.GetUserInfoDto;
import oss.fosslight.api.dto.ListVulnerabilityDto;
import oss.fosslight.common.Url;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.ApiVulnerabilityService;
import oss.fosslight.service.T2UserService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteUserController {
    @Autowired
    ApiVulnerabilityService vulnerabilityService;

    @Autowired
    T2UserService userService;

    @GetMapping("/me")
    public @ResponseBody ResponseEntity<GetUserInfoDto.Result> getMeInfo() {
        try {
            SecurityContext sec = SecurityContextHolder.getContext();
            AbstractAuthenticationToken auth = (AbstractAuthenticationToken)sec.getAuthentication();
            T2Users user = new T2Users();
            user.setUserId(auth.getName());
            T2Users userInfo = userService.getUserAndAuthorities(user);
            var result = GetUserInfoDto.Result.builder()
                    .username(userInfo.getUserName())
                    .email(userInfo.getEmail())
                    .build();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
