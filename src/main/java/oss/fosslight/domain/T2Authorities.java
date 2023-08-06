/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class T2Authorities implements Serializable{
	private static final long serialVersionUID = 1L;
    public static final String ROLE_USER = "ROLE_USER";
    private static final Set<String> ROLE_WRITABLE_AUTHORITIES =
        Set.of(
            "ROLE_VIEWER",
            "ROLE_ADMIN",
            "ROLE_WATCHER",
            "ROLE_COMM",
            "ROLE_SYSTEM"
        );

	private String userId;																	// 사용자 Id
	private String authority;																// 권한 아이디
	private String[] userIds;																//사용자 아이디들
	private String[] authoritys;															// 권한 아이디들

    public boolean isWritable() {
        return ROLE_WRITABLE_AUTHORITIES.contains(this.authority);
    }
}
