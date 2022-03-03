/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CommonFunction;

@Slf4j
public class RequestUtil {

	/**
	 * Gets the rest template.
	 *
	 * @param context the context
	 * @return the rest template
	 */
	public static RestTemplate getRestTemplate(final HttpClientContext context) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setReadTimeout(5000); // milliseconds
		RestTemplate restOperations = new RestTemplate(factory);
		restOperations.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
			@Override
			protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
				if (context.getAttribute(HttpClientContext.COOKIE_STORE) == null) {
					context.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
					Builder builder = RequestConfig.custom()
							// .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
							// .setAuthenticationEnabled(false)
							.setRedirectsEnabled(false);
					context.setRequestConfig(builder.build());
				}
				return context;
			}
		});

		return restOperations;
	}

	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	public static HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept-Encoding", "gzip, deflate, sdch");
		headers.set("Connection", "keep-alive");
		headers.set("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
		headers.set("Accept", "text/html,application/json,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.set("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
		return headers;
	}

	/**
	 * POST 요청.
	 *
	 * @param url     the url
	 * @param parts   the parts
	 * @param charset the charset
	 * @return the string
	 */
	public static String post(String url, MultiValueMap<String, Object> parts, String charset) {
		log.debug("url:{}, param:{}, charset:{}", new Object[] { url, parts, charset });
		HttpClientContext context = HttpClientContext.create();
		RestTemplate restOperations = RequestUtil.getRestTemplate(context);
		restOperations.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(charset)));
		HttpHeaders headers = RequestUtil.getHeaders();
		ResponseEntity<String> exchange = restOperations.exchange(url, HttpMethod.POST,
				new HttpEntity<MultiValueMap<String, Object>>(parts, headers), String.class);
		String body = exchange.getBody();
		HttpStatus statusCode = exchange.getStatusCode();
		if (statusCode != HttpStatus.OK) {
			log.warn("%s - status : {}", url, statusCode);
		}
		
		return body;
	}

	/**
	 * Post.
	 *
	 * @param url   the url
	 * @param parts the parts
	 * @return the string
	 */
	public static String post(String url, MultiValueMap<String, Object> parts) {
		return post(url, parts, "UTF-8");
	}

	/**
	 * Gets 요청.
	 *
	 * @param url the url
	 * @return the string
	 */
	public static String get(String url, String charset) {
		HttpClientContext context = HttpClientContext.create();
		RestTemplate restOperations = RequestUtil.getRestTemplate(context);
		restOperations.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(charset)));
		HttpHeaders headers = RequestUtil.getHeaders();
		ResponseEntity<String> exchange = restOperations.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers),
				String.class);
		HttpStatus statusCode = exchange.getStatusCode();
		if (statusCode != HttpStatus.OK) {
			log.warn("{} - status : {}", url, statusCode);
		}
		return exchange.getBody();
	}
	
	/**
	 * Gets the.
	 *
	 * @param url the url
	 * @param params the params
	 * @param convertCamelCase the convert camel case
	 * @return the json object
	 */
	public static String get(String url, Map<String, Object> params, boolean convertCamelCase) {
		return get(url, params, "UTF-8", convertCamelCase, null);
	}

	/**
	 * Gets the.
	 *
	 * @param url the url
	 * @param params the params
	 * @param charSet the char set
	 * @param convertCamelCase the convert camel case
	 * @param ignoreParams the ignore params
	 * @return the json object
	 */
	public static String get(String url, Map<String, Object> params, String charSet, boolean convertCamelCase, String[] ignoreParams) {

		String result = "";
		try {
			String query = ParameterStringBuilder.getParamsString(params, convertCamelCase, ignoreParams);
			result = get(url + (CommonFunction.isEmpty(query) ? "" : "?") + query, charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}

		return result;
	}

	/**
	 * URL을 호출한 결과값을 String으로 반환한다.
	 *
	 * @param strUrl  url of string
	 * @param body    the body
	 * @param charSet the char set
	 * @return the URL response to string by post
	 * @throws Exception the exception
	 */
	public static String getURLResponseToStringByPost(String strUrl, String body, String charSet) throws Exception {

		String result = null;

		StringBuffer buff = new StringBuffer();
		DataOutputStream out = null;
		BufferedReader in = null;
		InputStreamReader ins = null;
		try {
			URL url = new URL(strUrl);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(30000);
			out = new DataOutputStream(conn.getOutputStream());
			out.write(body.getBytes(charSet));
			out.flush();
			out.close();

			ins = new InputStreamReader(conn.getInputStream(), charSet);
			in = new BufferedReader(ins);

			String read;
			while ((read = in.readLine()) != null) {
				buff.append(read + "\n");
			}

			result = buff.toString();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception ignored) {
					throw new Exception(ignored);
				}
			}

			if (ins != null) {
				try {
					ins.close();
				} catch (Exception ignored) {
					throw new Exception(ignored);
				}
			}
		}

		return result;
	}

	/**
	 * The Class ParameterStringBuilder.
	 */
	public static class ParameterStringBuilder {
		
		/**
		 * Gets the params string.
		 *
		 * @param params the params
		 * @param convertCamelCase the convert camel case
		 * @param ignoreParams the ignore params
		 * @return the params string
		 * @throws UnsupportedEncodingException the unsupported encoding exception
		 */
		public static String getParamsString(Map<String, Object> params, boolean convertCamelCase, String[] ignoreParams)
				throws UnsupportedEncodingException {

			List<String> ignoreParamList = new ArrayList<>();
			if (ignoreParams != null) {
				for(String s : Arrays.asList(ignoreParams)) {
					// 대소문자무시
					ignoreParamList.add(s.toUpperCase());
				}
			}
			
			StringBuilder result = new StringBuilder();

			if(params != null && !params.isEmpty()) {
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					String _key = entry.getKey();
					if (ignoreParamList.contains(_key.toUpperCase())) {
						continue;
					}
					if (entry.getValue() == null) {
						continue;
					}
					
					if(convertCamelCase) {
						_key = StringUtil.convert2CamelCase(_key);
					}
					
					result.append(_key);
					result.append("=");
					result.append((String) entry.getValue());
					result.append("&");
				}				
			}

			String resultString = result.toString();
			return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
		}
	}
	
	/**
	 * Post.
	 *
	 * @param url the url
	 * @param params the params
	 * @param ignoreParams the ignore params
	 * @return the json object
	 */
	public static String post(String url, Map<String, Object> params, String[] ignoreParams) {
		return post(url, params, false, ignoreParams);
	}

	/**
	 * Post.
	 *
	 * @param url the url
	 * @param params the params
	 * @param convertCamalCase the convert camal case
	 * @param ignoreParams the ignore params
	 * @return the json object
	 */
	public static String post(String url, Map<String, Object> params, boolean convertCamalCase, String[] ignoreParams) {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		List<String> ignoreParamList = new ArrayList<>();
		if(ignoreParams != null) {
			for(String s : Arrays.asList(ignoreParams)) {
				ignoreParamList.add(s.toUpperCase());
			}
		}
		if(params != null) {
			for(String _k : params.keySet()) {
				
				if(ignoreParamList.contains(_k.toUpperCase())) {
					continue;
				}
				
				Object _v = params.get(_k);
				if(_v == null) {
					continue;
				}
				if(convertCamalCase) {
					_k = StringUtil.convert2CamelCase(_k);
				}
				
				if (_v instanceof List<?> || _v instanceof String) {
					parts.add(_k, _v);
				} else if(_v instanceof String[]) {
					//2019-06-04 Javaroid 배열로 값을 전송하기 위한 처리. 고객 그룹 등록 등 일괄 처리를 위한 작업 때문에 추가하였다.
					String[] _value = (String[])_v;
					for (String value : _value) {
						parts.add(_k, value);
					}
				} else {
					parts.add(_k, _v + "");
				}
			}
		}

		return post(url, parts);
	}
	
	public static String simpleGet(String url) throws Exception {
		//http client
		CloseableHttpClient httpClient = null;
		BufferedReader reader = null;
		String rtn = "";
		
		try {
			httpClient = createDefault();
			
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Content-Type", "charset=UTF-8");
			httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			
			rtn = response.toString();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {}
			}
			
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (Exception e) {}
			}
		}
		
		return rtn;
	}
	
	private static CloseableHttpClient createDefault() {
		RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(5000).setSocketTimeout(5000).setConnectTimeout(5000)
				.build();
		
		return HttpClients.custom().setDefaultRequestConfig(config).build();
	}
}
