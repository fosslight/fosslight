/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.repository.CommentMapper;
import oss.fosslight.service.CommentService;
import oss.fosslight.util.StringUtil;

@Service
public class CommentServiceImpl implements CommentService {
	@Autowired CommentMapper commentMapper;
	
	@Override
	public List<CommentsHistory> getCommentListHis(CommentsHistory bean) {
		List<CommentsHistory> commentsHistoryList = commentMapper.getCommentListHis(bean);
		
		for (CommentsHistory commentsHistory : commentsHistoryList) {
			commentMapper.updateHistoryReadYn(commentsHistory);
		}
		
		return commentsHistoryList;
	}
	
	@Override
	public String getUserComment(CommentsHistory bean) {
		return commentMapper.getUserComment(bean);
	}
	
	@Override
	@Transactional
	public CommentsHistory registComment(CommentsHistory commentsHistory) {
		return registComment(commentsHistory, true);
	}

	@Override
	@Transactional
	public CommentsHistory registComment(CommentsHistory bean, boolean deleteUserComment) {
		boolean isNew = StringUtil.isEmpty(bean.getCommId());
		
		if (!StringUtil.isEmpty(bean.getContents()) || !StringUtil.isEmpty(bean.getStatus())){
			String updatedContents = CommonFunction.addBlankTargetToLink(bean.getContents());
			bean.setContents(updatedContents);
			commentMapper.registComment(bean);
		}
		
		boolean isPartner = CoConstDef.CD_DTL_COMMENT_PARTNER_HIS.equals(bean.getReferenceDiv());
		
		if (deleteUserComment && isNew && (CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS.equals(bean.getReferenceDiv())
				|| CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS.equals(bean.getReferenceDiv())
				|| CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_HIS.equals(bean.getReferenceDiv())
				|| CoConstDef.CD_DTL_COMMENT_PARTNER_HIS.equals(bean.getReferenceDiv()) 
				|| CoConstDef.CD_DTL_COMMENT_PROJECT_HIS.equals(bean.getReferenceDiv())
				|| CoConstDef.CD_DTL_COMMENT_SECURITY_HIS.equals(bean.getReferenceDiv())
				|| CoConstDef.CD_DTL_COMMENT_LICENSE.equals(bean.getReferenceDiv())
				|| CoConstDef.CD_DTL_COMMENT_OSS.equals(bean.getReferenceDiv()))) {
			switch (bean.getReferenceDiv()) {
				case CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICATION_USER);
					
					break;
				case CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_USER);
					
					break;
				case CoConstDef.CD_DTL_COMMENT_PROJECT_HIS:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_USER);
					
					break;
				case CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_HIS:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_USER);
					
					break;
				case CoConstDef.CD_DTL_COMMENT_SECURITY_HIS:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_SECURITY_USER);
					
					break;
				case CoConstDef.CD_DTL_COMMENT_PARTNER_HIS:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_USER);
					
					break;
				case CoConstDef.CD_DTL_COMMENT_LICENSE:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_LICENSE_USER);
					
					break;
				case CoConstDef.CD_DTL_COMMENT_OSS:
					bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_OSS_USER);
					
					break;
				default:
					break;
			}
			
			if (!StringUtil.isEmpty(bean.getReferenceDiv())) {
				commentMapper.deleteCommentUserTemp(bean);
			}
		}
		
		if (CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_ADDED_COMMENT.equals(bean.getMailType()) 
			|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_ADDED_COMMENT.equals(bean.getMailType())
			|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_ADDED_COMMENT.equals(bean.getMailType())
			|| CoConstDef.CD_MAIL_TYPE_PARTER_ADDED_COMMENT.equals(bean.getMailType())
			|| CoConstDef.CD_MAIL_TYPE_PROJECT_ADDED_COMMENT.equals(bean.getMailType())
			|| CoConstDef.CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT.equals(bean.getMailType())) {
			CoMail mailBean = new CoMail(bean.getMailType());
			
			if (isPartner) {
				mailBean.setParamPartnerId(bean.getReferenceId());
			} else {
				mailBean.setParamPrjId(bean.getReferenceId());
			}
			
			mailBean.setComment(bean.getContents());
			
			if (CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS.equals(bean.getReferenceDiv())) {
				mailBean.setStage("Identificaiton");
			} else if (CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS.equals(bean.getReferenceDiv())) {
				mailBean.setStage("Packaging");
			} else if (CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_HIS.equals(bean.getReferenceDiv())) {
				mailBean.setStage("Distribution");
			}
			
			mailBean.setReceiveFlag(bean.getMailSendType());
			
			if (!StringUtil.isEmpty(bean.getContents())){//comment 내용이 있을시만 메일 발송
				CoMailManager.getInstance().sendMail(mailBean);
			}
		}
		
		return commentMapper.getCommentInfo(bean.getCommId());
	}
	
	@Override
	public List<CommentsHistory> getCommentList(CommentsHistory commentsHistory){
		List<CommentsHistory> result = commentMapper.getCommentList(commentsHistory);
		
		return result;
	}
	
	@Override
	@Transactional
	public int deleteComment(CommentsHistory commentsHistory) {
		int result = 0;
		
		result = commentMapper.deleteComment(commentsHistory);
		
		return result;
	}

	@Override
	public String getContents(String commId) { return commentMapper.getContent(commId); }

	@Override
	public int updateComment(CommentsHistory bean) {
		return updateComment(bean, true);
	}
	
	@Override
	public int updateComment(CommentsHistory bean, boolean emailSendFlag) {
		CommentsHistory before = commentMapper.getCommentInfo(bean.getCommId());
		int rtn = commentMapper.updateComment(bean);
		
		if (emailSendFlag) {
			if (rtn > 0 && bean.getReferenceDiv() != null) {
				boolean isPartner = false;
				String paramOssId = null;
				String paramLicenseId = null;
				
				switch (bean.getReferenceDiv()) {
					case CoConstDef.CD_DTL_COMMENT_IDENTIFICAITON_HIS:
						bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_IDENTIFICATION_USER);
						bean.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_MODIFIED_COMMENT);
						
						break;
					case CoConstDef.CD_DTL_COMMENT_PACKAGING_HIS:
						bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PACKAGING_USER);
						bean.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_MODIFIED_COMMENT);
						
						break;
					case CoConstDef.CD_DTL_COMMENT_PROJECT_HIS:
						bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PROJECT_USER);
						bean.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_MODIFIED_COMMENT);
						
						break;
					case CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_HIS:
						bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_DISTRIBUTION_USER);
						bean.setMailType(CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_MODIFIED_COMMENT);
						
						break;
					case CoConstDef.CD_DTL_COMMENT_PARTNER_HIS:
						bean.setReferenceDiv(CoConstDef.CD_DTL_COMMENT_PARTNER_USER);
						bean.setMailType(CoConstDef.CD_MAIL_TYPE_PARTER_MODIFIED_COMMENT);
						isPartner = true;
						break;
					case CoConstDef.CD_DTL_COMMENT_LICENSE:
						bean.setMailType(CoConstDef.CD_MAIL_TYPE_LICENSE_MODIFIED_COMMENT);
						paramLicenseId = bean.getReferenceId();
						
						break;
					case CoConstDef.CD_DTL_COMMENT_OSS:
						bean.setMailType(CoConstDef.CD_MAIL_TYPE_OSS_MODIFIED_COMMENT);
						paramOssId = bean.getReferenceId();
						
						break;
					default:
						return rtn;
				}
				
				CoMail mailBean = new CoMail(bean.getMailType());
				mailBean.setParamExpansion1(before.getContents());
				mailBean.setParamExpansion2(bean.getContents());
				mailBean.setComment(bean.getContents());
				mailBean.setReceiveFlag(bean.getMailSendType());
				
				if (isPartner) {
					mailBean.setParamPartnerId(bean.getReferenceId());
				} else if (!StringUtil.isEmpty(paramLicenseId)) {
					mailBean.setParamLicenseId(paramLicenseId);
				} else if (!StringUtil.isEmpty(paramOssId)) {
					mailBean.setParamOssId(paramOssId);
				} else {
					mailBean.setParamPrjId(bean.getReferenceId());
				}
				
				CoMailManager.getInstance().sendMail(mailBean);
			}
		}
		
		return rtn;
	}
	
	@Override
	public int getCommentListHisCnt(CommentsHistory bean) {
		return commentMapper.getCommentListHisCnt(bean);
	}

	@Override
	public List<CommentsHistory> getMoreCommentListHis(CommentsHistory bean) {
		List<CommentsHistory> commentsHistoryList = commentMapper.getMoreCommentListHis(bean);
		
		for (CommentsHistory commentsHistory : commentsHistoryList) {
			commentMapper.updateHistoryReadYn(commentsHistory);
		}
		
		return commentsHistoryList;
	}

	@Override
	public Map<String, Object> getCommnetInfo(String commId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		CommentsHistory commentsHistory = commentMapper.getCommentInfo(commId);
		map.put("info", commentsHistory);
		
		return map; 
	}

}
