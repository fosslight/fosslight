/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.common.Url.COMPLIANCE;
import oss.fosslight.domain.File;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.service.ComplianceService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.ExcelUtil;

@Controller
@Slf4j
public class ComplianceController extends CoTopComponent {
	
	@Autowired PartnerService partnerService;
	@Autowired ProjectService projectService;
	@Autowired ComplianceService complianceService;
	
	@Resource private Environment env;
	
	@GetMapping(value=COMPLIANCE.MODEL_STATUS, produces = "text/html; charset=utf-8")
	public String getModelStatus(HttpServletRequest req, HttpServletResponse res, Model model){
		return COMPLIANCE.MODEL_STATUS_JSP;
	}
	
	@GetMapping(value=COMPLIANCE.PARTNER_LIST_STATUS, produces = "text/html; charset=utf-8")
	public String get3rdStatus(HttpServletRequest req, HttpServletResponse res, Model model){
		return COMPLIANCE.PARTNER_LIST_STATUS_JSP;
	}
	
	@RequestMapping(value=COMPLIANCE.LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> listAjax(
			PartnerMaster partnerMaster
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		int page = Integer.parseInt(req.getParameter("page"));
		int rows = Integer.parseInt(req.getParameter("rows"));
		String sidx = req.getParameter("sidx");
		String sord = req.getParameter("sord");
		partnerMaster.setCurPage(page);
		partnerMaster.setPageListSize(rows);
		partnerMaster.setSortField(sidx);
		partnerMaster.setSortOrder(sord);
		
		if(partnerMaster.getStatus() != null) {
			String statuses = partnerMaster.getStatus();
			
			if(!isEmpty(statuses)) {
				String[] arrStatuses = statuses.split(",");
				partnerMaster.setArrStatuses(arrStatuses);
			}
		}
		
		Map<String, Object> map = null;
		
		try{
			partnerMaster.setModelFlag(CoConstDef.FLAG_YES);
			
			map = partnerService.getPartnerStatusList(partnerMaster);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		
		return makeJsonResponseHeader(map);
	}
	
	@PostMapping(value = COMPLIANCE.MODEL_LIST_AJAX)
	public @ResponseBody ResponseEntity<Object> modelListAjax(@RequestBody Project project, HttpServletRequest req,
			HttpServletResponse res, Model model) {
		if(!isEmpty(project.getModelName())){
			String[] modelNames = project.getModelName().split(",");
			List<String> modelListInfo = new ArrayList<String>();
			
			for(String modelName : modelNames){
				if(modelListInfo.indexOf(modelName) == -1){
					modelListInfo.add(modelName);
				}
			}
			
			project.setModelListInfo(modelListInfo);
			
			Map<String, Object> map = complianceService.getModelList(project);

			return makeJsonResponseHeader(map);
		} else {
			return makeJsonResponseHeader(false, "");
		}
	}
	
	@PostMapping(value=COMPLIANCE.READ_MODEL_LIST)
	public @ResponseBody ResponseEntity<Object> readModelList(File file, MultipartHttpServletRequest req, HttpServletRequest request, HttpServletResponse res, Model model) throws Exception{
		//엑셀 분석
		List<Project> modelList = ExcelUtil.getModelList(req, CommonFunction.emptyCheckProperty("upload.path", "/upload"));
		
		if(modelList == null) {
			return makeJsonResponseHeader(false, "");
		}
		
		return makeJsonResponseHeader(true, "", modelList);
	}
	
}
