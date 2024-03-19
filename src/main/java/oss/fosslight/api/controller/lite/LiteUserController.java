package oss.fosslight.api.controller.lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import oss.fosslight.api.dto.GetUserInfoDto;
import oss.fosslight.api.dto.UserDto;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url;
import oss.fosslight.domain.T2Users;
import oss.fosslight.repository.ApiCodeMapper;
import oss.fosslight.service.ApiVulnerabilityService;
import oss.fosslight.service.T2UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = Url.API_LITE.PATH)
public class LiteUserController {
    @Autowired
    ApiVulnerabilityService vulnerabilityService;

    @Autowired
    T2UserService userService;

    @Autowired
    ApiCodeMapper codeMapper;

    @GetMapping("/me")
    public @ResponseBody ResponseEntity<GetUserInfoDto.Result> getMeInfo() {
        try {
            var userInfo = userService.getLoginUserInfo();
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

    @GetMapping("/users")
    public @ResponseBody ResponseEntity<List<UserDto>> getUserList() {
        try {
            var users = userService.getAllUsers(new T2Users())
                    .stream().map(UserDto::new).collect(Collectors.toList());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/divisions")
    public @ResponseBody ResponseEntity<List<Map<String, Object>>> getDivisions() {
        try {
            var codes = codeMapper.selectCodeList(CoConstDef.CD_USER_DIVISION, null);
            return ResponseEntity.ok(codes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
