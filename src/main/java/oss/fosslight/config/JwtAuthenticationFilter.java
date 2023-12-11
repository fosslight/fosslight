package oss.fosslight.config;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.SESSION;
import oss.fosslight.util.ResponseUtil;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider provider) {
		jwtTokenProvider = provider;
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		AntPathMatcher pathMatcher = new AntPathMatcher();
		String path = request.getServletPath();
		return List.of(CoConstDef.STATIC_RESOURCES_URL_PATTERNS).stream().anyMatch(exclude -> pathMatcher.match(exclude, path))
				|| List.of(CoConstDef.PERMIT_UTL_PATTERNS).stream().anyMatch(exclude -> pathMatcher.match(exclude, path));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
			throws ServletException, IOException {

		String token = jwtTokenProvider.resolveToken(req);
		if (jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);	
		} else {
			if(isAjaxRequest(req)) {
				ResponseUtil.DefaultAlertAndGo(res, "로그인 세션이 만료되었습니다. 로그인 화면으로 이동합니다.", SESSION.LOGIN);
			} else {
				res.sendRedirect(SESSION.LOGIN);
			}
			return;				
		}

		filterChain.doFilter(req, res);
	}
	
    private boolean isAjaxRequest(HttpServletRequest req) {
        return "XMLHttpRequest".equals(req.getHeader("x-requested-with"));
    }
}
