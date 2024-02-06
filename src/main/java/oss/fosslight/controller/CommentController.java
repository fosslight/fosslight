/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.jsonldjava.utils.Obj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.Url.COMMENT;
import oss.fosslight.domain.CommentsHistory;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.service.CommentService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.StringUtil;

@Controller
public class CommentController extends CoTopComponent {

    @Autowired
    CommentService commentService;
    @Autowired
    ProjectService projectService;
    @Autowired
    PartnerService partnerService;

    @GetMapping(value = {COMMENT.COMMENT_LIST}, produces = "text/html; charset=utf-8")
    public String getCommentList(CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model) {
        model.addAttribute("commentList", commentService.getCommentListHis(commentsHistory));
        model.addAttribute("commentListCnt", commentService.getCommentListHisCnt(commentsHistory));
        model.addAttribute("moreYn", false);

        return "fragments/comment-fragments :: commentAreaFragment";
    }
    
    @GetMapping(value={COMMENT.CUS_COMMENT_LIST})
	public String getCusCommentList(CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model){
    	model.addAttribute("commentList", commentService.getCommentListHis(commentsHistory));
    	model.addAttribute("commentListCnt", commentService.getCommentListHisCnt(commentsHistory));
		model.addAttribute("commentsHistory", commentsHistory);
		
		return "comment/commentList";
	}
    
    @GetMapping(value = COMMENT.MORE_COMMENT_LIST, produces = "text/html; charset=utf-8")
    public String getMoreCommentList(CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model) {
        model.addAttribute("commentList", commentService.getMoreCommentListHis(commentsHistory));
        model.addAttribute("commentListCnt", 0);
        model.addAttribute("moreYn", true);

        return "fragments/comment-fragments :: commentAreaFragment";
    }

    @RequestMapping(value = COMMENT.POPUP)
    public String index(HttpServletRequest req, HttpServletResponse res, Model model
            , @PathVariable String rDiv, @PathVariable String rId) {
        CommentsHistory commentsHistory = new CommentsHistory();
        commentsHistory.setReferenceDiv(rDiv);
        commentsHistory.setReferenceId(rId);
        model.addAttribute("basicInfo", commentsHistory);

        if ("prj".equalsIgnoreCase(rDiv) || rDiv.equalsIgnoreCase("security")) {
            model.addAttribute("project", projectService.getProjectBasicInfo(rId));
        } else if ("3rd".equalsIgnoreCase(rDiv)) {
            PartnerMaster partnerMaster = new PartnerMaster();
            partnerMaster.setPartnerId(rId);

            model.addAttribute("partner", partnerService.getPartnerMasterOne(partnerMaster));
        }

        return "fragments/comment-fragments :: commentHistoryAreaFragment";
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

    @GetMapping(value = COMMENT.COMMENT_INFO_ID)
    public @ResponseBody ResponseEntity<Object> getCommentInfo(HttpServletRequest req, HttpServletResponse res, Model model, @PathVariable String commId) {
        Map<String, Object> map = commentService.getCommnetInfo(commId);

        return makeJsonResponseHeader(map);
    }

    @GetMapping(value = COMMENT.DIV_COMMENT_LIST)
    public String getDivCommentList(CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model) {
        List<CommentsHistory> result = commentService.getCommentList(commentsHistory);
        model.addAttribute("commentList", result);

        return "fragments/comment-fragments :: commentAreaFragment";
    }

    @GetMapping(value = COMMENT.DIV_COMMENT_BY_ID)
    public String getDivCommentByCommId(@RequestParam String commId, @RequestParam String referenceDiv, Model model) {
        String contents = commentService.getContents(commId);
        model.addAttribute("commId", commId);
        model.addAttribute("referenceDiv", referenceDiv);
        model.addAttribute("contents", contents);

        return "fragments/comment-fragments :: commentPopupFragment";
    };

    @GetMapping(value = COMMENT.DIV_USER_COMMENT)
    public String getDivUserComment(CommentsHistory comHisBean, Model model) {
        model.addAttribute("contents", commentService.getUserComment(comHisBean));

        return "fragments/comment-fragments :: userCommentPopupFragment";
    };
    
    @PostMapping(value = COMMENT.EDIT_POPUP)
    public String editPopup(@ModelAttribute CommentsHistory commentsHistory, HttpServletRequest req, HttpServletResponse res, Model model) {
    	Map<String, Object> map = commentService.getCommnetInfo(commentsHistory.getCommId());
    	model.addAttribute("comments", map);
        return "comment/editPopupFragments";
    }
}

