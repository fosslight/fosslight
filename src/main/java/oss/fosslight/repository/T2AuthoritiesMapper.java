/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.T2Authorities;
import oss.fosslight.domain.T2Users;

@Mapper
public interface T2AuthoritiesMapper {
	public int insertAuthorities(T2Authorities t2Authorities);
	
	public List<T2Authorities> selectAuthoritiesByUser(T2Users veticaMember);
	
	public int deleteAuthoritiesByUserId(T2Users veticaMember);
}