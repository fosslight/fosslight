/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import oss.fosslight.config.AppConstBean;
import oss.fosslight.util.StringUtil;


@Component("imageView")
@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
public class ImageView extends AbstractView {
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		T2File imageFile = (T2File)model.get("imageFile");
		// 응답 메시지에 파일의 길이를 넘겨줍니다.
		res.setContentLength(StringUtil.string2integer(imageFile.getSize()));
		// 응답의 타입이 이미지임을 알려줍니다.
		res.setContentType(StringUtil.avoidNull(imageFile.getContentType(), MediaType.IMAGE_JPEG_VALUE));
		
		// 파일로부터 byte를 읽어옵니다.
		byte[] bytes = readFile(imageFile.getLogiPath(), imageFile.getLogiNm());
		write(res, bytes);
	}

	/**
	 * 파일로부터 byte 배열 읽어오기 
	 */
	private byte[] readFile(String filePath, String fileName) throws IOException {
		String path = filePath + "/" + fileName;
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
		int length = bis.available();
		byte[] bytes = new byte[length];
		bis.read(bytes);
		bis.close();
		
		return bytes;
	}

	/**
	 * 응답 OutputStream에 파일 내용 쓰기
	 */
	private void write(HttpServletResponse res, byte[] bytes) throws IOException {
		OutputStream output = res.getOutputStream();
		output.write(bytes);
		output.flush();
	}
}
