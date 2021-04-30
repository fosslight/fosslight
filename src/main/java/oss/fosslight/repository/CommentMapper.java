/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import oss.fosslight.domain.CommentsHistory;

@Mapper
public interface CommentMapper {
	public int registComment(CommentsHistory commentsHistory);

	public List<CommentsHistory> getCommentList(CommentsHistory commentsHistory);

	public int deleteComment(CommentsHistory commentsHistory);

	public void deleteCommentByReferenceId(CommentsHistory param);

	public CommentsHistory selectLastComment();

	public String getContent(String comment);

	public CommentsHistory getCommentInfo(String commId);

	public void deleteCommentUserTemp(CommentsHistory bean);

	public String getUserComment(CommentsHistory bean);

	public List<CommentsHistory> getCommentListHis(CommentsHistory bean);

	public int updateComment(CommentsHistory bean);

	public int updateHistoryReadYn(CommentsHistory bean);

	public List<CommentsHistory> getMoreCommentListHis(CommentsHistory bean);

	public int getCommentListHisCnt(CommentsHistory bean);
}