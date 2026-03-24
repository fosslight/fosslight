/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.domain.*;
import oss.fosslight.repository.ApiCodeMapper;
import oss.fosslight.repository.ApiPartnerMapper;
import oss.fosslight.repository.ApiProjectMapper;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.PartnerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.ApiCommonService;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.ProjectService;

import static oss.fosslight.CoTopComponent.isEmpty;

@Service
public class ApiCommonServiceImpl implements ApiCommonService {

	@Autowired CodeMapper codeMapper;
	@Autowired
	ApiProjectMapper apiProjectMapper;
	@Autowired ApiPartnerMapper apiPartnerMapper;
	@Autowired ProjectMapper projectMapper;
	@Autowired PartnerMapper partnerMapper;
	@Autowired ProjectService projectService;
	@Autowired
	CommentService commentService;

    @Override
	public void mergeDivision(String from, String to) throws Exception {
		T2CodeDtl codeDtl = codeMapper.getCodeDetail(CoConstDef.CD_USER_DIVISION,from);
		codeDtl.setUseYn(CoConstDef.FLAG_NO);
		codeMapper.updateCodeDetail(codeDtl);

		Project prj = new Project();
		prj.setDivision(from);

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("division", from);

		List<Map<String, Object>> list = apiProjectMapper.selectProject(paramMap);

		List<Project> projectList = new Vector<>();
		for (Map<String, Object> map : list) {
			String prjId = String.valueOf( map.get("prjId"));
			Project project = projectService.getProjectBasicInfo(prjId);
			Project beforeProject, afterProject = null;
			beforeProject = projectService.getProjectBasicInfo(prjId);
			project.setDivision(to);
			projectMapper.updateProjectDivision(project);
			afterProject = projectService.getProjectBasicInfo(prjId);
			String diffComment = CommonFunction.getDiffItemComment(beforeProject, afterProject);
			try {
				CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED);
				mailBean.setParamPrjId(project.getPrjId());
				mailBean.setCompareDataBefore(beforeProject);
				mailBean.setCompareDataAfter(afterProject);

				if (!isEmpty(diffComment)) {
					CommentsHistory commHisBean = new CommentsHistory();
					commHisBean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_HIS);
					commHisBean.setReferenceId(project.getPrjId());
					commHisBean.setContents(diffComment);
					commHisBean.setStatus("Changed");
					commentService.registComment(commHisBean);
				}
				CoMailManager.getInstance().sendMail(mailBean);

			} catch (Exception e) {
			}
			projectList.add(afterProject);
		}

		// 3rd party(Partner) division 변경
		Map<String, Object> partnerParamMap = new HashMap<String, Object>();
		partnerParamMap.put("division", from);
		partnerParamMap.put("countPerPage", 0);
		List<Map<String, Object>> partnerList = apiPartnerMapper.selectPartnerMaster(partnerParamMap);
		List<PartnerMaster> mailPartnerList = new Vector<>();
		for (Map<String, Object> map : partnerList) {
			String partnerId = String.valueOf(map.get("partnerId"));
			partnerMapper.updateDivision(partnerId, to);
			PartnerMaster mailPartner = new PartnerMaster();
			mailPartner.setPartnerId(partnerId);
			mailPartner.setPartnerName((String) map.get("partnerName"));
			mailPartner.setSoftwareName((String) map.get("softwareName"));
			mailPartner.setSoftwareVersion((String) map.get("softwareVersion"));
			mailPartnerList.add(mailPartner);
		}

		CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_COMMON_DIVISION_MERGE);
		String fromDivisionName = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, from) + "(" + from + ")";
		String toDivisionName = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, to) + "(" + to + ")";
		mailBean.setParamExpansion1(fromDivisionName != null ? fromDivisionName : from);
		mailBean.setParamExpansion2(toDivisionName != null ? toDivisionName : to);
		mailBean.setParamPrjList(projectList);
		mailBean.setParamPartnerList(mailPartnerList);
		CoMailManager.getInstance().sendMail(mailBean);
	}
}