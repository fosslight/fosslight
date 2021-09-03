/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.COMMENT;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;

@Controller
public class CommentController extends CoTopComponent {

	@Autowired CommentService commentService;
	@Autowired ProjectService projectService;
	@Autowired PartnerService partnerService;
	
	@GetMapping(value={COMMENT.COMMENT_LIST}, produces = "text/html; charset=utf-8")
	public String getCommentList(CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model){
		model.addAttribute("commentList", commentService.getCommentListHis(commentsHistory));
		model.addAttribute("commentListCnt", commentService.getCommentListHisCnt(commentsHistory));
		model.addAttribute("moreYn", false);
		
		return COMMENT.COMMENT_LIST_JSP;
	}
	
	@GetMapping(value=COMMENT.MORE_COMMENT_LIST, produces = "text/html; charset=utf-8")
	public String getMoreCommentList(CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model){
		model.addAttribute("commentList", commentService.getMoreCommentListHis(commentsHistory));
		model.addAttribute("commentListCnt", 0);
		model.addAttribute("moreYn", true);
		
		return COMMENT.COMMENT_LIST_JSP;
	}
	
	@RequestMapping(value=COMMENT.POPUP)
	public String index(HttpServletRequest req, HttpServletResponse res, Model model
			, @PathVariable String rDiv, @PathVariable String rId){
		CommentsHistory commentsHistory = new CommentsHistory();
		commentsHistory.setReferenceDiv(rDiv);
		commentsHistory.setReferenceId(rId);
		model.addAttribute("basicInfo", commentsHistory);
		
		if("prj".equalsIgnoreCase(rDiv)) {
			model.addAttribute("project", projectService.getProjectBasicInfo(rId));
		} else if("3rd".equalsIgnoreCase(rDiv)) {
		    PartnerMaster partnerMaster = new PartnerMaster();
	        partnerMaster.setPartnerId(rId);
	        
		    model.addAttribute("partner", partnerService.getPartnerMasterOne(partnerMaster));
		} else if("oss".equalsIgnoreCase(rDiv)) {
			
		} else if("license".equalsIgnoreCase(rDiv)) {
			
		}
		
		return COMMENT.POPUP_JSP;
	}
	
	@PostMapping(value = COMMENT.UPDATE_COMMENT)
	public @ResponseBody ResponseEntity<Object> updateComment(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		commentService.updateComment(commentsHistory);
		
		return makeJsonResponseHeader(true);
	}
	
	@PostMapping(value = COMMENT.DELETE_COMMENT)
	public @ResponseBody ResponseEntity<Object> deleteComment(@ModelAttribute CommentsHistory commentsHistory,
			HttpServletRequest req, HttpServletResponse res, Model model) {
		commentService.deleteComment(commentsHistory);
		
		return makeJsonResponseHeader(true);
	}
	
	@GetMapping(value=COMMENT.COMMENT_INFO_ID)
	public @ResponseBody ResponseEntity<Object> getCommentInfo(HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String commId) {
		Map<String, Object> map = commentService.getCommnetInfo(commId);
		
		return makeJsonResponseHeader(map);
	}
	
	@GetMapping(value=COMMENT.DIV_COMMENT_LIST)
	public @ResponseBody ResponseEntity<Object> getDivCommentList(CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model) {
		List<CommentsHistory> result = commentService.getCommentList(commentsHistory);
		
		return makeJsonResponseHeader(result);
	}
}
