/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.util.StringUtil;
@Slf4j
public class ComBean extends CoTopComponent implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	// 로그인 User 아이디
	/**  Login User ID. */
	private String loginUserName;
	// 로그인 User 권한
	/**  Login Users Athority. */
	private String loginUserRole;
	// 현재 페이지번호
	/** The cur page. */
	private int curPage = 1;
	// 한 블록당 보여줄 글 수
	/** The page list size. */
	private int pageListSize = 10;
	// 노출할 블록 수
	/** The block size. */
	private int blockSize = 10;
	// 전체 글의 수
	/** The tot list size. */
	private int totListSize;
	// 전체 블록 수
	/** The tot block size. */
	private int totBlockSize;
	// Start Index
	/** The start index. */
	private int startIndex = 0;
	
	/**  전체 블록수. */
	private int totBlockPage;
	
	/**  현재 페이지의 몇번째 블록. */
	private int blockPage;
	
	/**  현재 블록의 시작 페이지. */
	private int blockStart;
	
	/**  현재 블록의 끝 페이지. */
	private int blockEnd;
	
	
	// 검색조건
	/** The sch condition. */
	private String schCondition;
	// 검색어
	/** The sch value. */
	private String schValue;

	/* 데이터 정렬을 위한 조건 [S] */
	/** The sort field. */
	private String sortField = "";

	/** The sort order. */
	private String sortOrder = "";
	/* 데이터 정렬을 위한 조건 [E] */

	/* 필드 검색 조건 유지 [S] */
	/** The sch query string. */
	private String schQueryString;
	/* 필드 검색 조건 유지 [E] */

	// 공통 변수 영역 [S]
	/**  추천여부. (Y: 추천, N: 추천안함) */
	private String hotYn = "N";

	/**  등록자 ID. */
	private String creator;
	
	/* 2018-07-20 choye 추가 */
	/**  등록자 이름. */
	private String creatorName;
	
	/** 등록자 부서. */
	private String creatorDivisionName;

	/**  등록일. */
	protected String createdDate;

	/**  수정자 ID. */
	public String modifier;
	
	/* 2018-07-20 choye 추가 */
	/**  수정자 이름. */
	public String modifierName;
	
	/** 수정자 부서. */
	private String modifierDivisionName;

	/**  수정일시. */
	public String modifiedDate;
	
	/**  ID 배열. */
	private String[] ids;
	
	private String filters;
	private String filterCondition;
	
	/** The user use yn. */
	private String userUseYn;
	
	/** The dept use yn. */
	private String deptUseYn;
	
	private String domain;

	/**
	 * Gets the ids.
	 *
	 * @return the ids
	 */
	public String[] getIds() {
		return ids != null ? ids.clone() : null;
	}

	/**
	 * Sets the ids.
	 *
	 * @param ids the new ids
	 */
	public void setIds(String[] ids) {
		this.ids = ids != null ? ids.clone() : null;
	}


	/**  삭제 여부. */
	private String delYn;
	// 공통 변수 영역 [E]
	
	  /**  날짜 검색 조건. */
    private String dateCondition;
    
    /**  시작 Date. */
    private String startDate;
    
    /**  끝 Date. */
    private String endDate;
    
    
    // Grid 관련 STAR ----------------------------------------------------- //
    /**  Grid ID. */
    private String gridId;
    
    /** The no. */
    private String no;
    
    /**  Grid Action oper. */
    private String oper;
    
    /**  Grid 정렬 기준 칼럼. */
    private String sidx;
    
    /**  Grid 정렬(내림차순, 오름차순). */
    private String sord;
    
    /**  pager - current page. */
    private int page;
    
    /**  pager - display rows count. */
    private int rows;
    
    private String isPopup;
    // Grid 관련 END ----------------------------------------------------- //
    
    

	/**
     * Gets the sidx.
     *
     * @return the sidx
     */
    public String getSidx() {
    	
		if (!isEmpty(sidx) && CoConstDef.VALIDATION_USE_CAMELCASE) {
			String _sidx = StringUtil.convertToUnderScore(sidx).toUpperCase();
			if (CoCodeManager.getCodeNames(CoConstDef.CD_SYSTEM_GRID_SORT_CAST).contains(_sidx)) {
				_sidx = "CAST("+ _sidx +" AS SIGNED)";
			}
			return _sidx;
		}
		return sidx;
	}
    public String getSidxEx() {
		if (StringUtils.isEmpty(sidx)) {
			return sidx;
		}

    	return sidx.toLowerCase();
    }

    public String getSidxOrg() {
    	return sidx;
    }

	/**
	 * Gets the page.
	 *
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * Sets the page.
	 *
	 * @param page the new page
	 */
	public void setPage(int page) {
		this.page = page;
		this.curPage = page;
	}

	/**
	 * Gets the rows.
	 *
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Sets the rows.
	 *
	 * @param rows the new rows
	 */
	public void setRows(int rows) {
		this.rows = rows;
		this.pageListSize = rows;
	}

	/**
	 * Sets the sidx.
	 *
	 * @param sidx the new sidx
	 */
	public void setSidx(String sidx) {
		this.sidx = sidx;
	}

	/**
	 * Gets the sord.
	 *
	 * @return the sord
	 */
	public String getSord() {
		return sord;
	}

	/**
	 * Sets the sord.
	 *
	 * @param sord the new sord
	 */
	public void setSord(String sord) {
		this.sord = sord;
	}

	/**
	 * 접속한 유저 ID 가져오기.
	 *
	 * @return the login user name
	 */
	public String getLoginUserName() {
		return loginUserName;
	}
	
	
	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}

	/**
	 * 접속한 유저 Authority 가져오기.
	 *
	 * @return the login user role
	 */
	public String getLoginUserRole() {
		return loginUserRole;
	}

	/**
	 * 접속 유저 ID & Athority 세팅.
	 */
	public ComBean() {
		loginUserName = loginUserName();
		loginUserRole = loginUserRole();
	}
	/**
	 * Gets the cur page.
	 *
	 * @return the cur page
	 */
	public int getCurPage() {
		return curPage;
	}

	/**
	 * Sets the cur page.
	 *
	 * @param curPage the new cur page
	 */
	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}

	/**
	 * Gets the page list size.
	 *
	 * @return the page list size
	 */
	public int getPageListSize() {
		return pageListSize;
	}

	/**
	 * Sets the page list size.
	 *
	 * @param pageListSize the new page list size
	 */
	public void setPageListSize(int pageListSize) {
		this.pageListSize = pageListSize;
	}

	/**
	 * Gets the block size.
	 *
	 * @return the block size
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * Sets the block size.
	 *
	 * @param blockSize the new block size
	 */
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	/**
	 * Gets the tot list size.
	 *
	 * @return the tot list size
	 */
	public int getTotListSize() {
		return totListSize;
	}

	/**
	 * Total List Size 등록 (=전체 목록 갯수)
	 * 
	 * 		+ Block Size와 Start Index를 갱신.
	 *
	 * @param totListSize the new tot list size
	 */
	public void setTotListSize(int totListSize) {
		this.totListSize = totListSize;
		this.totBlockSize = totListSize/pageListSize < 1 ? 1 : totListSize%pageListSize==0?totListSize/pageListSize:(totListSize/pageListSize)+1;

		this.startIndex = (curPage-1)*pageListSize;
		
		int totBlockPage = (totBlockSize / blockSize);
		if (totBlockSize != blockSize) {
			totBlockPage++;
		}
		this.totBlockPage = totBlockPage;
		
		int blockPage = ((curPage-1) / blockSize) + 1;
		this.blockPage = blockPage;
		
		int blockStart = ((blockPage-1) * blockSize) + 1;
		int blockEnd = blockStart+blockSize-1;
		if (blockEnd > totBlockSize) {
			blockEnd = totBlockSize;
		}
		
		this.blockStart = blockStart;
		this.blockEnd = blockEnd;
	}

	/**
	 * Gets the tot block size.
	 *
	 * @return the tot block size
	 */
	public int getTotBlockSize() {
		return totBlockSize;
	}

	/**
	 * Sets the tot block size.
	 *
	 * @param totBlockSize the new tot block size
	 */
	public void setTotBlockSize(int totBlockSize) {
		this.totBlockSize = totBlockSize;
	}

	/**
	 * Gets the start index.
	 *
	 * @return the start index
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * Sets the start index.
	 *
	 * @param startIndex the new start index
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	
	/**
	 * Gets the tot block page.
	 *
	 * @return the tot block page
	 */
	public int getTotBlockPage() {
		return totBlockPage;
	}
	
	/**
	 * Sets the tot block page.
	 *
	 * @param totBlockPage the new tot block page
	 */
	public void setTotBlockPage(int totBlockPage) {
		this.totBlockPage = totBlockPage;
	}
	
	/**
	 * Gets the block page.
	 *
	 * @return the block page
	 */
	public int getBlockPage() {
		return blockPage;
	}
	
	/**
	 * Sets the block page.
	 *
	 * @param blockPage the new block page
	 */
	public void setBlockPage(int blockPage) {
		this.blockPage = blockPage;
	}
	
	/**
	 * Gets the block start.
	 *
	 * @return the block start
	 */
	public int getBlockStart() {
		return blockStart;
	}
	
	/**
	 * Sets the block start.
	 *
	 * @param blockStart the new block start
	 */
	public void setBlockStart(int blockStart) {
		this.blockStart = blockStart;
	}
	
	/**
	 * Gets the block end.
	 *
	 * @return the block end
	 */
	public int getBlockEnd() {
		return blockEnd;
	}
	
	/**
	 * Sets the block end.
	 *
	 * @param blockEnd the new block end
	 */
	public void setBlockEnd(int blockEnd) {
		this.blockEnd = blockEnd;
	}
	/**
	 * Gets the sch condition.
	 *
	 * @return the sch condition
	 */
	public String getSchCondition() {
		return schCondition;
	}

	/**
	 * Sets the sch condition.
	 *
	 * @param schCondition the new sch condition
	 */
	public void setSchCondition(String schCondition) {
		this.schCondition = schCondition;
	}

	/**
	 * Gets the sch value.
	 *
	 * @return the sch value
	 */
	public String getSchValue() {
		return schValue;
	}

	/**
	 * Sets the sch value.
	 *
	 * @param schValue the new sch value
	 */
	public void setSchValue(String schValue) {
		this.schValue = schValue;
	}

	/**
	 * Gets the sort field.
	 *
	 * @return the sort field
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * Sets the sort field.
	 *
	 * @param sortField the new sort field
	 */
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	/**
	 * Gets the sort order.
	 *
	 * @return the sort order
	 */
	public String getSortOrder() {
		return sortOrder;
	}

	/**
	 * Sets the sort order.
	 *
	 * @param sortOrder the new sort order
	 */
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * Gets the sch query string.
	 *
	 * @return the sch query string
	 */
	public String getSchQueryString() {
		return schQueryString;
	}

	/**
	 * Sets the sch query string.
	 *
	 * @param schQueryString the new sch query string
	 */
	public void setSchQueryString(String schQueryString) {
		this.schQueryString = schQueryString;
	}

	/**
	 * Gets the hot yn.
	 *
	 * @return the hot yn
	 */
	public String getHotYn() {
		return hotYn;
	}

	/**
	 * Sets the hot yn.
	 *
	 * @param hotYn the new hot yn
	 */
	public void setHotYn(String hotYn) {
		this.hotYn = hotYn;
	}

	/**
	 * Gets the 등록자 ID.
	 *
	 * @return the 등록자 ID
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * Sets the 등록자 ID.
	 *
	 * @param creator the new 등록자 ID
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	/**
	 * Gets the 등록자 이름. 2018-07-20 choye 추가
	 *
	 * @return the 등록자 이름
	 */
	public String getCreatorName() {
		return creatorName;
	}

	/**
	 * Sets the 등록자 이름. 2018-07-20 choye 추가
	 *
	 * @param creator the new 등록자 이름
	 */
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	
	/**
	 * Gets the 등록자 부서. 2018-07-20 choye 추가
	 *
	 * @return the 등록자 부서
	 */
	public String getCreatorDivisonName() {
		return creatorDivisionName;
	}

	/**
	 * Sets the 등록자 부서. 2018-07-20 choye 추가
	 *
	 * @param creator the new 등록자 부서
	 */
	public void setCreatorDivisonName(String creatorDivisionName) {
		this.creatorDivisionName = creatorDivisionName;
	}

	/**
	 * Gets the 등록일.
	 *
	 * @return the 등록일
	 */
	public String getCreatedDate() {
		if (this.createdDate == null){
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(CoConstDef.DATABASE_FORMAT_DATE_ALL);
			this.createdDate = sdf.format(now);
		}
		return this.createdDate;
	}

	/**
	 * Sets the 등록일.
	 *
	 * @param createdDate the new created date
	 */
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Sets the 등록일 as curDate.
	 */
	public void setCreatedDateCurrentTime(){
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(CoConstDef.DATABASE_FORMAT_DATE_ALL);
		this.createdDate = sdf.format(now);
	}

	/**
	 * Sets the 수정일시 as curdate.
	 */
	public void setModDtCunnrentTime() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(CoConstDef.DATABASE_FORMAT_DATE_ALL);
		this.modifiedDate = sdf.format(now);
	}
	
	

	/**
	 * Gets the del yn.
	 *
	 * @return the del yn
	 */
	public String getDelYn() {
		return delYn;
	}

	/**
	 * Sets the del yn.
	 *
	 * @param delYn the new del yn
	 */
	public void setDelYn(String delYn) {
		this.delYn = delYn;
	}

	/**
	 * Gets the upd id.
	 *
	 * @return the upd id
	 */
	public String getModifier() {
		return modifier;
	}
	
	/**
	 * Sets the upd id.
	 *
	 * @param modifier the new upd id
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	
	/**
	 * Gets the 수정자 이름. 2018-07-20 choye 추가
	 *
	 * @return the 수정자 이름
	 */
	public String getModifierName() {
		return modifierName;
	}
	
	/**
	 * Sets the 수정자 이름. 2018-07-20 choye 추가
	 *
	 * @param modifier the new 수정자 이름
	 */
	public void setModifierName(String modifierName) {
		this.modifierName = modifierName;
	}
	
	/**
	 * Gets the 수정자 이름. 2018-07-20 choye 추가
	 *
	 * @return the 수정자 부서
	 */
	public String getModifierDivisionName() {
		return modifierDivisionName;
	}
	
	/**
	 * Sets the 수정자 부서. 2018-07-20 choye 추가
	 *
	 * @param modifier the new 수정자 부서
	 */
	public void setModifierDivisionName(String modifierDivisionName) {
		this.modifierDivisionName = modifierDivisionName;
	}
	
	/**
	 * Gets the upd dt.
	 *
	 * @return the upd dt
	 */
	public String getModifiedDate() {
		return modifiedDate;
	}
	
	/**
	 * Sets the upd dt.
	 *
	 * @param modifiedDate the new modified date
	 */
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	/**
	 * Gets the date condition.
	 *
	 * @return the date condition
	 */
	public String getDateCondition() {
		return dateCondition;
	}

	/**
	 * Sets the date condition.
	 *
	 * @param dateCondition the new date condition
	 */
	public void setDateCondition(String dateCondition) {
		this.dateCondition = dateCondition;
	}

	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * Sets the start date.
	 *
	 * @param startDate the new start date
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * Sets the end date.
	 *
	 * @param endDate the new end date
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	

	/**
	 * Gets the grid id.
	 *
	 * @return the grid id
	 */
	public String getGridId() {
		return gridId;
	}

	/**
	 * Sets the grid id.
	 *
	 * @param gridId the new grid id
	 */
	public void setGridId(String gridId) {
		this.gridId = gridId;
	}

	/**
	 * Gets the oper.
	 *
	 * @return the oper
	 */
	public String getOper() {
		return oper;
	}

	/**
	 * Sets the oper.
	 *
	 * @param oper the new oper
	 */
	public void setOper(String oper) {
		this.oper = oper;
	}

	/**
	 * Gets the no.
	 *
	 * @return the no
	 */
	public String getNo() {
		return no;
	}

	/**
	 * Sets the no.
	 *
	 * @param no the new no
	 */
	public void setNo(String no) {
		this.no = no;
	}
	
	/**
	 * Gets the user use yn.
	 *
	 * @return the use yn
	 */
	public String getUserUseYn() {
		return userUseYn;
	}

	/**
	 * Sets the user use yn.
	 *
	 * @param useYn the new use yn
	 */
	public void setUserUseYn(String userUseYn) {
		this.userUseYn = userUseYn;
	}
	
	/**
	 * Gets the use yn.
	 *
	 * @return the use yn
	 */
	public String getDeptUseYn() {
		return deptUseYn;
	}

	/**
	 * Sets the use yn.
	 *
	 * @param useYn the new use yn
	 */
	public void setDeptUseYn(String deptUseYn) {
		this.deptUseYn = deptUseYn;
	}
	
	/**
	 * Gets the bit array sum.
	 *
	 * @param value
	 *            the value
	 * @return the bit array sum
	 */
	protected static String getBitArraySum(String value){
		int sum = 0;
		
		if (value != null){
			String arr[] = value.split(",");
			if (arr.length > 1){
				
				for (String s : arr) {
					try{
						sum += Integer.parseInt(s.trim());
					}catch(Exception e){
						return "";
					}
				}
			}else{
				sum = Integer.parseInt(value);
			}
		}
		return sum+"";
	}
	
	@Override
	public String toString() {
		return "ComBean [curPage=" + curPage + ", pageListSize=" + pageListSize
				+ ", blockSize=" + blockSize + ", totListSize=" + totListSize
				+ ", totBlockSize=" + totBlockSize + ", startIndex="
				+ startIndex + ", schCondition=" + schCondition + ", schValue="
				+ schValue + ", sortField=" + sortField + ", sortOrder="
				+ sortOrder + ", schQueryString=" + schQueryString + ", hotYn="
				+ hotYn + "]";
	}

	public String getFilters() {
		return filters;
	}

	public Map<String, Object> getFiltersMap() {
		Map<String, Object> filtersMap = null;

		Type collectionType1 = new TypeToken<Map<String, Object>>() {}.getType();
		String[] dateField = {"creationDate", "publDate", "modiDate", "regDt"};

		if (filters != null) {
			filtersMap = (Map<String, Object>) fromJson(filters, collectionType1);
			if (filtersMap.containsKey("rules")) {
				for (Map<String, String> ruleMap : (List<LinkedTreeMap<String, String>>)filtersMap.get("rules")) {
					String field = ruleMap.get("field");
					String data = ruleMap.get("data");
					
					for (String date : dateField) {
						if (date.equalsIgnoreCase(field)) {
							ruleMap.put("data", CommonFunction.formatDateSimple(data));
						}
					}
				}
			}

		}
		
		return filtersMap;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public String getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(String filterCondition) {
		this.filterCondition = filterCondition;
	}
	
	public String getIsPopup() {
		return isPopup;
	}

	public void setIsPopup(String isPopup) {
		this.isPopup = isPopup;
	}

	public void setLoginUserRole(String loginUserRole) {
		this.loginUserRole = loginUserRole;
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
}