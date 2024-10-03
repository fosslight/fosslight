package oss.fosslight.util;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CookieUtil {
	
    private static Optional<String> cookieDomain;

	//@Value("${spring.profiles.active}") private String profiles;

    @Value("${server.servlet.session.cookie.domain:}")
    public void setCookieDomain(String v) {
    	cookieDomain = Optional.of(v);
    }
	
	public static String getCookie(HttpServletRequest request, String key) {
		
		String value = ""; 
		if(StringUtil.isEmpty(key)) {
			return value;
		}
		
		Cookie[] cookies = request.getCookies();
		
		if(cookies != null){ 
			for (Cookie cookie : cookies) { 
				if(key.equals(cookie.getName())){ 
					try {
						value = java.net.URLDecoder.decode(cookie.getValue(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						log.debug(e.getMessage());
					} 
					break; 
				} 
			} 
		} 
		return value; 
	}
	
	public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		ResponseCookie cookies = ResponseCookie.from(name, value)
				.path("/")
				.sameSite("Lax")
				.httpOnly(true)
				//.secure("prod".equals(profiles))
				.maxAge(maxAge)
				.build();
		response.addHeader("Set-Cookie", cookies.toString());
	}
	
	public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					if(cookieDomain.isPresent() && !StringUtil.isEmpty(cookieDomain.get())) {
						cookie.setDomain(cookieDomain.get());
					}
					log.info("cookieName :{}", cookie.getName());
					log.info("cookieDomain :{}", cookie.getDomain());
					cookie.setValue(null);
					cookie.setPath("/");
					cookie.setMaxAge(0);
					response.addCookie(cookie);
				}
			}
		}
	}

	public static String serialize(Object object) {
		return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
	}
}
