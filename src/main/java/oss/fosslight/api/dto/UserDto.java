package oss.fosslight.api.dto;

import lombok.Data;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.T2Users;

@Data
public class UserDto {
    String userName;
    String userId;

    public UserDto(T2Users user) {
        userName = user.getUserName();
        userId = user.getUserId();
    }
}
