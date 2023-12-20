package oss.fosslight.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import oss.fosslight.domain.T2Users;
import oss.fosslight.util.CookieUtil;
import oss.fosslight.util.StringUtil;

/** The Constant log. */
@Slf4j
@Component
public class JwtTokenProvider {
	
	@Value("${token.secret.key}") private final String jwtSecret;
	/** The Constant JWT_EXPIRATION_MS. */
	private static final int jwtExpirationMsec = 1000 * 60 * 60 * 8;
	
	private static final String jwtRequireIssuer = "fosslight-hub";

	/** The Constant userFieldUsrId. */
	private static final String userFieldUsrId = "userId";
	
	/** The Constant userFieldUsrNm. */
	private static final String userFieldUsrNm = "userNm";
	
	/** The Constant userFieldAuthId. */
	private static final String userFieldAuthId = "authId";
	
	
    public JwtTokenProvider(
            @Value("${secret.key}") String secret) {
        this.jwtSecret = secret;
    }
	
	public SecretKey getJwtKey() throws Exception {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}
	
	/**
	 * Generate token.
	 *
	 * @param userInfo the user info
	 * @return the string
	 * @throws Exception 
	 * @throws InvalidKeyException 
	 */
	public String generateToken(T2Users userInfo) throws InvalidKeyException, Exception {
		return Jwts.builder()
				.claim(userFieldUsrId, userInfo.getUserId())
				.claim(userFieldUsrNm, userInfo.getUserName())
				.claim(userFieldAuthId, userInfo.getAuthority())
				.subject(userInfo.getUserId())
				.issuer(jwtRequireIssuer)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + jwtExpirationMsec))
				.signWith(getJwtKey())
				.compact();
	}
	
	/**
	 * Gets the claims.
	 *
	 * @param token the token
	 * @return the claims
	 */
	public Claims getClaims(String token) {
		try {
			return Jwts.parser().requireIssuer(jwtRequireIssuer).verifyWith(getJwtKey()).build().parseSignedClaims(token).getPayload();
		} catch (JwtException | IllegalArgumentException e) {
			log.warn(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}
	
	/**
	 * Gets the user info.
	 *
	 * @return the user info
	 */
	public T2Users getUserInfo() {
		String token = resolveToken(getRequest());
		if(!StringUtil.isEmpty(token) && validateToken(token)) {
			Claims claims = getClaims(token);
			if(claims != null) {
				T2Users userInfo = new T2Users();
				userInfo.setUserId((String)claims.get(userFieldUsrId));
				userInfo.setUserName((String)claims.get(userFieldUsrNm));
				userInfo.setAuthority((String)claims.get(userFieldAuthId));
				return userInfo;
			}
		}
		return null;
	}
	
	public String getClaimsObject(String fieldName) {
		String token = resolveToken(getRequest());
		if(!validateToken(token)) {
			log.warn("JWT token is invalid ({})", fieldName);
			return null;
		}
		Claims claims = getClaims(token);
		if(claims != null) {
			return (String)claims.get(fieldName);
		} else {
			log.warn("JWT Claims is invalid ({})", fieldName);
			return "";
		}
	}
	
	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserId() {
		return getClaimsObject(userFieldUsrId);
	}
	
	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return getClaimsObject(userFieldUsrNm);

	}
	
	/**
	 * Gets the auth id.
	 *
	 * @return the auth id
	 */
	public String getAuthId() {
		return getClaimsObject(userFieldAuthId);
	}

	/**
	 * Resolve token.
	 *
	 * @param req the req
	 * @return the string
	 */
	public String resolveToken(HttpServletRequest req) {
		String token = null;
		if(req != null) {
			String value = CookieUtil.getCookie(req, "X-FOSS-AUTH-TOKEN");
			if(value != null) {
				token = value;
			}			
		}
		return token;
	}

	/**
	 * Validate token.
	 *
	 * @param token the token
	 * @return true, if successful
	 */
	public  boolean validateToken(String token) {
		if(StringUtil.isEmpty(token)) {
			return false;
		}
		try {
			Claims claims = getClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (SignatureException se) {
			log.warn("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty.");
        } catch (NullPointerException e){
			log.error("JWT Claims is invalid.");
		}

		return false;
	}
	
	
	/**
	 * Gets the authentication.
	 *
	 * @param token the token
	 * @return the authentication
	 */
	public Authentication getAuthentication(String token) {
		Claims claims = getClaims(token);

		Collection<? extends GrantedAuthority> authorities = Arrays
				.stream(claims.get(userFieldAuthId).toString().split(",")).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		User principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}
	
	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	private HttpServletRequest getRequest() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(servletRequestAttributes != null) {
			return servletRequestAttributes.getRequest();
		}
		
		return null;
	}
}
