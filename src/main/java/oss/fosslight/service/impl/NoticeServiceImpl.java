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

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.Notice;
import oss.fosslight.repository.NoticeMapper;
import oss.fosslight.service.NoticeService;

@Service("NoticeService")
public class NoticeServiceImpl extends CoTopComponent implements NoticeService {
	//Mapper
	@Autowired NoticeMapper noticeMapper;
	
	/**
	 * 코드 목록 조회
	 */
	@Override
	public Map<String, Object> getNoticeList(Notice vo) throws Exception {
		Map<String, Object> map = null;
		int records = noticeMapper.selectNoticeTotalCount(vo);
		
		if(records > 0) {
			vo.setTotListSize(records);
			// Grid paging 처리를 위한 기본 param 설정 Map 생성(반드시 totlistsize를 set 하고 나서 생성해야함)
			map = getGridPagerMap(vo);
			List<Notice> noticeList =  noticeMapper.selectNoticeList(vo);
			
	        // 테그 치환
	        if(noticeList != null) {
				for(Notice item : noticeList) {
					item.setReplaceNotice(item.getNotice().replaceAll("<br>", "\n\r"));
					item.setReplaceNotice(item.getNotice().replaceAll("<(/)?([a-zA-Z]*)(\\\\s[a-zA-Z]*=[^>]*)?(\\\\s)*(/)?>", ""));
				}
	        }
	        
			map.put("rows", noticeList);
		}
		
		return map == null ? new HashMap<String, Object>() : map;
	}
	
	@Override
	public void setNotice(Notice vo) throws Exception {
		if(CoConstDef.GRID_OPERATION_ADD.equals(vo.getOper())) { // 추가
			noticeMapper.insertNotice(vo);
		} else if(CoConstDef.GRID_OPERATION_EDIT.equals(vo.getOper())) { // 수정
			noticeMapper.updateNotice(vo);
		} else { // 삭제
			noticeMapper.deleteNotice(vo);
		}
	}
	
	@Override
	public Map<String, Object> getPublishedNotice(Notice vo) throws Exception {
		Map<String, Object> map = null;
		
		int records = noticeMapper.selectPublishedNoticeCount(vo);
		
		if(records > 0) {
			List<Notice> noticeList = noticeMapper.selectPublishedNotice(vo);
			
			map = getGridPagerMap(vo);
	
	        if(noticeList != null) {
				map.put("noticeList", noticeList);
	        }
		}
		
		return map == null ? new HashMap<String, Object>() : map;
	}
}
