/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import oss.fosslight.common.CoConstDef;

@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_VALIDATION_PROPERTIES)})
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {    
    /** The jwt token provider. */
    private final JwtTokenProvider jwtTokenProvider;
    
    /** The unauthorized handler. */
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    
    @Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		return http.csrf(AbstractHttpConfigurer::disable).exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(jwtAccessDeniedHandler))
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeRequests(
						authorize -> authorize.antMatchers(CoConstDef.STATIC_RESOURCES_URL_PATTERNS).permitAll()
						.antMatchers(CoConstDef.PERMIT_UTL_PATTERNS).permitAll()
						.anyRequest().authenticated())
				.formLogin(FormLoginConfigurer::disable)
				.addFilterBefore(new JwtAuthenticationFilter(this.jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
				.build();
	}
    
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
