/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.security.Key;
import java.util.UUID;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
	private Key key;
	
	public JwtUtil(String secret){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
	
	public String createToken(String id, String email) {
        String token = Jwts.builder()
                .claim("userId", id)
                .claim("email", email)
                .claim("uuid", UUID.randomUUID())
                .signWith(key)
                //.signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return token;
    }
}
