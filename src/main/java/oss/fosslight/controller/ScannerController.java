/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.service.ResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url;
import oss.fosslight.domain.T2Users;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.RequestUtil;
import oss.fosslight.util.StringUtil;

@RestController
@Slf4j
public class ScannerController {
	
	@Autowired ResponseService responseService;
	@Autowired T2UserService userService;
	
	@PostMapping(value = {Url.EXTERNAL.REQUEST_FL_SCAN})
	public CommonResult requestFlScanService(
    		@RequestParam(name = "prjId", required = true) String prjId,
    		@RequestParam(name = "wgetUrl", required = true) String wgetUrl){
		
		try {

			log.info("fl scanner start pid : " + prjId + ", url:" + wgetUrl);
			String scanServiceUrl = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_FL_SCANNER_URL);
			String adminToken = CoCodeManager.getCodeExpString(CoConstDef.CD_EXTERNAL_ANALYSIS_SETTING, CoConstDef.CD_DTL_ADMIN_TOKEN);
			if (StringUtil.isEmpty(scanServiceUrl) || StringUtil.isEmpty(adminToken)) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "FL Scanner Url or Admin token is not configured");
			}
			
			T2Users user = userService.getLoginUserInfo();
			if (user == null || StringUtil.isEmpty(user.getEmail())) {
				return responseService.getFailResult(CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE, "Login User Email is not configured");
			}
			
			String resBody = "";
 			if (scanServiceUrl.startsWith("https://")) {
 				resBody = getDataForRestApiConnection(prjId, wgetUrl, scanServiceUrl, adminToken, user);
 			} else {
 				MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
 				parts.add("pid", prjId);
 				parts.add("link", wgetUrl);
 				parts.add("email", user.getEmail());
 				parts.add("admin", adminToken);
 				resBody = RequestUtil.post(scanServiceUrl, parts);
 			}

			log.info("fl scanner response : " + resBody);
			
			return responseService.getSuccessResult();
		} catch (Exception e) {
			log.error("failed request fl scan, pid:" + prjId + ", url:" + wgetUrl, e);
			return responseService.getFailResult(CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE, e.getMessage());
		}
	}

	private String getDataForRestApiConnection(String prjId, String wgetUrl, String scanServiceUrl, String adminToken, T2Users user) {
		String urlString = scanServiceUrl + "?pid=" + prjId + "&link=" + wgetUrl + "&email=" + user.getEmail() + "&admin=" + adminToken;
		HttpsURLConnection httpsURLConnection = null;
		String body = "";
		
		try {
			URL url = new URL(urlString);
			ignoreSsl();
			httpsURLConnection = (HttpsURLConnection) url.openConnection();
			httpsURLConnection.setRequestMethod("POST");
			httpsURLConnection.setConnectTimeout(1000 * 15);
			
			if (httpsURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				body = response.toString();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return body;
	}
	
	private void ignoreSsl() {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
	    		return true;
	    	}
		};
		try {
			trustAllHttpsCertificates();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
	
	private static void trustAllHttpsCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
	
	static class miTM implements TrustManager,X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }
}
