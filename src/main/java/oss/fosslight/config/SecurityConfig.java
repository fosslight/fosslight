/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;

@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_VALIDATION_PROPERTIES)})
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {    
	@Autowired private T2UserService userService;
	
	@Autowired private CustomAuthenticationProvider customAuthenticationProvider;
	@Autowired private CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(customAuthenticationProvider);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		// replay 어택을 막기 위한 csrf 토큰의 생성을 비활성화(disabled) 처리
		.csrf().disable()
		// 'X-Frame-Options' to 'DENY' 대응
		.headers().frameOptions().disable().and()
		.authorizeRequests().antMatchers(Url.USER.SAVE_AJAX).permitAll().and() // 사용자가입 요청처리 예외
		.authorizeRequests().antMatchers(Url.USER.RESET_PASSWORD).permitAll().and() // 비밀번호 초기화 요청처리 예외
		.authorizeRequests().antMatchers("/*" + Url.USER.SAVE_AJAX).permitAll().and() // 사용자가입 요청처리 예외
		.authorizeRequests().antMatchers(Url.VULNERABILITY.VULN_POPUP).permitAll().and() // vulnerability popup 화면 예외
		.authorizeRequests().antMatchers(Url.API.PATH+"/**").permitAll().and()
		.authorizeRequests().antMatchers(Url.NOTICE.PUBLISHED_NOTICE).permitAll().and() // 공지사항 조회 요청처리 예외

		// 요청에 대한 권한 매핑
		.authorizeRequests().anyRequest().authenticated()		// 모든 요청에 대해 권한 확인이 필요

		// set login page url
		.and()
		.formLogin()
			.permitAll()
			.loginPage(AppConstBean.SECURITY_LOGIN_PAGE)
			.usernameParameter(AppConstBean.SECURITY_USERNAME_PARAMETER)
			.passwordParameter(AppConstBean.SECURITY_PASSWORD_PARAMETER)
			.authenticationDetailsSource(customWebAuthenticationDetailsSource)
			.defaultSuccessUrl(AppConstBean.SECURITY_DEFAULT_SUCCESS_URL)
            .loginProcessingUrl(AppConstBean.SECURITY_LOGIN_PROCESSING_URL)
            .failureHandler(failureHandler())
            .successHandler(successHandler())
            .permitAll()
            .and()
         .logout()
	         .logoutUrl(AppConstBean.SECURITY_LOGOUT_URL)
	         .logoutSuccessHandler(logoutSuccessHandler())
	         .deleteCookies("JSESSIONID")
	         .invalidateHttpSession(true) // is Default True
		;
	}
	
	public LogoutSuccessHandler logoutSuccessHandler() {
		LogoutSuccessHandler successHandler = new CustomLogoutSuccessHandler();
		
        return successHandler;
    }
	
	public CustomAuthenticationFailureHandler failureHandler() {
		return new CustomAuthenticationFailureHandler();
	}
	public CustomAuthenticationSuccessHandler successHandler() {
        CustomAuthenticationSuccessHandler successHandler = new CustomAuthenticationSuccessHandler(userService);
        
        return successHandler;
    }
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		// Security Debug
//		web.debug(true);
		
		web
			.ignoring()
				// static 리소스 경로는 webSecurity 검사 제외
				.antMatchers( CoConstDef.STATIC_RESOURCES_URL_PATTERNS )
				.antMatchers( CoConstDef.HEALTH_CHECK_URL )
		;

		web.ignoring().antMatchers("/v2/api-docs", "/swagger-resources/**",
                "/swagger-ui.html", "/webjars/**", "/swagger/**");
		
		super.configure(web);
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
    private static class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    	T2UserService userService = null;
    	
    	public CustomAuthenticationSuccessHandler(T2UserService userService) {
    		this.userService = userService;
		}
    	
    	@Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
    		// Security session에 추가 정보(Cusom)를 저장한다(Map형태)
            SecurityContext sec = SecurityContextHolder.getContext();
            AbstractAuthenticationToken auth = (AbstractAuthenticationToken)sec.getAuthentication();
            
            //User Detail
            T2Users user = new T2Users();
            user.setUserId(auth.getName());
            T2Users userInfo = userService.getUserAndAuthorities(user);
            
            HashMap<String, Object> info = new HashMap<String, Object>();
			if (StringUtil.isEmptyTrimmed(userInfo.getDivision())){
				userInfo.setDivision(CoConstDef.CD_USER_DIVISION_EMPTY);
			}
            info.put("sessUserInfo", userInfo);
            auth.setDetails(info);
            
            // ajax 로그인 체크시에만 사용
			String accept = request.getHeader("accept");
			String error = "false";
			String message = "로그인성공하였습니다.";
			String locale = userInfo.getDefaultLocale();

			if (StringUtil.indexOf(accept, "json") > -1) {
				response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");

				String data = StringUtil.join(new String[] {
						"{",
							"\"response\" : { ",
								"\"error\" : " + error + ", ",
								"\"message\" : \"" + message + "\", ",
								"\"locale\" : \"" + locale + "\"",
							"}",
						"}"
				});

				PrintWriter out = response.getWriter();
				out.print(data);
				out.flush();
				out.close();
			}
        }
    	
    	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
            this.redirectStrategy = redirectStrategy;
        }
     
        protected RedirectStrategy getRedirectStrategy() {
            return redirectStrategy;
        }
    }
    
	private static class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	    
	    public static String DEFAULT_TARGET_PARAMETER = "spring-security-redirect-login-failure";
	    private String targetUrlParameter = DEFAULT_TARGET_PARAMETER;
	    
		@Override
		public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException exception) throws IOException, ServletException {
			String accept = request.getHeader("accept");
			String error = "true";
			String message = "Invalid ID or password. Please try again.";
			
			if (exception.getMessage().equals("enter email")) {
				message = exception.getMessage();
			}
			
			if ( StringUtil.indexOf(accept, "html") > -1 ) {
				String redirectUrl = request.getParameter(this.targetUrlParameter);
			   
				if (redirectUrl != null) {
					super.logger.debug("Found redirect URL: " + redirectUrl);
					        
					getRedirectStrategy().sendRedirect(request, response, redirectUrl);
				} else {
					super.onAuthenticationFailure(request, response, exception);
				}
			} else if ( StringUtil.indexOf(accept, "xml") > -1 ) {
				response.setContentType("application/xml");
				response.setCharacterEncoding("utf-8");
				
				String data = StringUtil.join(new String[] {
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
					"<response>",
					"<error>" , error , "</error>",
					"<message>" , message , "</message>",
					"</response>"
				});
				
				PrintWriter out = response.getWriter();
				out.print(data);
				out.flush();
				out.close();
			} else if ( StringUtil.indexOf(accept, "json") > -1 ) {
				response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");
				
				String data = StringUtil.join(new String[] {
					" { \"response\" : {",
					" \"error\" : " , error , ", ",
					" \"message\" : \"", message , "\" ",
					"} } "
				});
				
				PrintWriter out = response.getWriter();
				out.print(data);
				out.flush();
				out.close();
			}
		}
	}
	
	public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {
	    @Override
		public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
				Authentication authentication) throws IOException, ServletException {
    		// Default 페이지로 이동
    		String loginSuccUrl = AppConstBean.SECURITY_LOGOUT_SUCCESS_URL;
    		
			response.sendRedirect(request.getContextPath() + loginSuccUrl);
		}
	}
    
}
