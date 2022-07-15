/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.IMAGE_VIEW;
import oss.fosslight.domain.ImageView;
import oss.fosslight.domain.T2File;
import oss.fosslight.service.FileService;


@Controller
@Slf4j
public class ImageViewController extends CoTopComponent {
	@Autowired FileService fileService;
	
	@Resource(name="imageView") ImageView imageView;

	@RequestMapping(value=IMAGE_VIEW.IMAGE)
	private ImageView getImage(@PathVariable String imageName, ModelMap modelMap) {
		T2File param = new T2File();
		param.setLogiNm(imageName);
		param.setGubn(CoConstDef.FILE_GUBUN_EDITOR_IMAGE);
		
		modelMap.put("imageFile", fileService.selectFileInfoByLogiName(param));
		
		return imageView;
	}
	
	@RequestMapping(value=IMAGE_VIEW.GUI_REPORT_ID_NM)
	private ImageView getGuiReportImage(@PathVariable String batId, @PathVariable String imageName, ModelMap modelMap) {
		String batPath = CommonFunction.emptyCheckProperty("vat.root.path", CommonFunction.getProperty("root.dir") + "/batsystem/");
		String dirPath = Paths.get(batPath).resolve("out").resolve(batId+"_dir").resolve("images").toString();
		Path reportImagePath = Paths.get(dirPath).resolve(imageName);
		File reportImageFile = reportImagePath.toFile();
		
		if(reportImageFile.exists()) {
			T2File f = new T2File();
			
			try {
				f.setContentType(Files.probeContentType(reportImagePath));
				f.setSize(Long.toString(reportImageFile.length()));
				f.setLogiPath(dirPath);
				f.setLogiNm(imageName);
				modelMap.put("imageFile", f);
			} catch (IOException e) {
				log.error(e.getMessage());
				
				return null;
			}
		} else {
			return null;
		}
		
		return imageView;
	}
}
