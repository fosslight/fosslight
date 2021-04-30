/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service;

import java.util.List;
import java.util.Map;

import oss.fosslight.domain.CommentsHistory;

public interface CommentService{
	public CommentsHistory registComment(CommentsHistory commentsHistory);

	public CommentsHistory registComment(CommentsHistory commentsHistory, boolean deleteUserComment);
	
	public List<CommentsHistory> getCommentList(CommentsHistory commentHistory);
	
	public int deleteComment(CommentsHistory commentsHistory);

	public String getUserComment(CommentsHistory comHisBean);

	public List<CommentsHistory> getCommentListHis(CommentsHistory commentsHistory);
	
	public int updateComment(CommentsHistory bean);
	
	public int updateComment(CommentsHistory bean, boolean emailSendFlag);

	public List<CommentsHistory> getMoreCommentListHis(CommentsHistory commentsHistory);

	public int getCommentListHisCnt(CommentsHistory bean);
	
	public Map<String, Object> getCommnetInfo(String commId);
}