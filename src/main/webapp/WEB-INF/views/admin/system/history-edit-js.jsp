<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script type="text/javascript">
	$(document).ready(function () {
		'use strict';
		
		/* 브라우저 창 크기에 따라 jqGrid Width 자동 조절 */
		$(window).bind('resize', function() {fn_sTblResizer();}).trigger('resize');
		
		// 히스토리 데이터 입력
		var jsonStr = '${historyJson}';
		var history = JSON.parse(jsonStr.replace(/'/g, '"'));
		
		// history main table 생성
		$("#list").jqGrid(fn_mTblOptMake(history.main));
		
		// history sub table 생성
		for(var i=0; i < history.sub.length; i++){
			$("#subAsList"+(i+1)).jqGrid(fn_sTblOptMake(history.sub[i], 'A'));
			$("#subBeList"+(i+1)).jqGrid(fn_sTblOptMake(history.sub[i], 'B'));
		}
		
		fn_sTblResizer();
	});
	
	
	// jqGrid table 옵션 기본정보
	var lv_tblDefOption = {
		  datatype: 'local'
		, data: []
		, colNames: []
		, colModel: []
		, autowidth: true
		, gridview: true
		, sortname: 'no'
		, viewrecords: true
		, sortorder: 'asc'
		, height: 'auto'
	};
	
	// sub table 리사이징 함수
	
	var fn_sTblResizer = function(){
		if($('.jqGridSet2 table').length > 0){
			var width = 0;
			$('.jqGridSet2').each(function(){
				if($(this).width() != 0){
					width = $(this).width();
				}
				
				// 그리드의 width 초기화
				$(this).find('table').jqGrid('setGridWidth', 0, true);
				
				// 그리드의 width를 div 에 맞춰서 적용
				$(this).find('table').jqGrid('setGridWidth', width, true);
			})
		}
	};
	
	var fn_mTblOptMake = function(mData){
		var op = {colNames: ['No', 'Modified Item', 'Before', 'After']
				,colModel: [{name: 'no', index: 'no', width: 40, align: 'center', sorttype: 'int'}
								,{name: 'name', index: 'name', width: 100}
								,{name: 'as', index: 'as', width: 350}
								,{name: 'be', index: 'be', width: 350}]
				,data: mData};
		
		return $.extend(lv_tblDefOption, op);  
	};
	
	var fn_sTblOptMake = function(sData, sType){
		sType = sType ? sType : 'B';
		
		// sData.colNames | sData.subBeList | sData.subAsList
		var op = {colNames: sData.colNames.map(function(e){return e.name})
				, colModel:	sData.colNames.map(function(e){return e.key == 'no' ? {name: e.key, index: e.key, width: 40, align: 'center', sorttype: 'int'} : {name: e.key, index: e.key, width: 100, align: 'center'}; }) 
				, data: sData[sType=='A'?'subAsList':'subBeList']};
		
		return $.extend(lv_tblDefOption, op);  
	};
</script>