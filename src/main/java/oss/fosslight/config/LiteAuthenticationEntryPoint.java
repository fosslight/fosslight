package oss.fosslight.config;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Component;
import oss.fosslight.common.Url;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LiteAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final String UNAUTHORIZED_OUTPUT = "{ \"error\": \"Unauthorized\" }";

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        var origin = request.getHeader("Origin");
        if (Url.LITE_HUB_ORIGINS.stream().anyMatch(liteOrigin -> liteOrigin.equals(origin))) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().println(UNAUTHORIZED_OUTPUT);
        } else {
            response.sendRedirect(AppConstBean.SECURITY_LOGIN_PAGE);
        }
    }
}
