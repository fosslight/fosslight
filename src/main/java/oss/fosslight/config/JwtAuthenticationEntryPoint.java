package oss.fosslight.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	@Override
	public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException{
		// 유효한 자격증명을 제공하지 않고 접근하려 할때 401
		res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}	
}
