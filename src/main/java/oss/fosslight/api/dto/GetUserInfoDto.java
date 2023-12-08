package oss.fosslight.api.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import oss.fosslight.domain.Vulnerability;

import java.util.List;
import java.util.stream.Collectors;

public class GetUserInfoDto {
    @Builder(toBuilder = true)
    public static class Result {
        String username;
        String email;
    }
}
