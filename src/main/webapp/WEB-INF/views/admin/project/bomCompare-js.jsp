<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
	var totalRow = 0;
	
	$(document).ready(function () {
		$("#beforePrjId").val("${beforePrjId}");
		$("#afterPrjId").val("${afterPrjId}");

		'use strict';
		
		evt.init();
		setGrid.load();
	});
	
	// 이벤트
	var evt = {
		// ready event
		init: function(){
			$("#beforePrjId, #afterPrjId").on("keypress", function(e){
				if(e.keyCode == "13"){
					$('#search').trigger('click');
				}
			});

			$('#search').click(function(){
				bomCompareList.search();
			});
		}
	};

	var fn = {
		// excel download function
		downloadExcel : function(){
			var bfPrjId = $("#beforePrjId").val();
			var afPrjId = $("#afterPrjId").val();
			
			if (bfPrjId == "" || afPrjId == "") {
				alertify.error('<spring:message code="msg.common.valid" />', 0);
				return false;
			}
			
			var searchParam = $('#bomcompareSearch').serializeObject();

			var param = {
					type:"bomcompare", 
					parameter:JSON.stringify(searchParam)
			};

			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify(param),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						if(data.validMsg == "overflow"){
							alertify.error(getMsgMaxRowCnt(), 0);
						}else{
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					} else {
						window.location =  '<c:url value="/exceldownload/getFile?id='+data.validMsg+'"/>';
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	};
	
	var setGrid = {
		load: function(data){
			//그리드 생성
			$('#list').jqGrid({
				datatype: 'local'
				, data: data 
				, colNames: ['Status', 'OSS_Before', 'License_Before', 'OSS_After', 'License_After']
				, colModel: [
					{name: 'status', index: 'status', width: 100, allign: 'center'},
					{name: 'beforeossname', index: 'beforeossname', width: 150, allign: 'center'},
					{name: 'beforelicense', index: 'beforelicense', width: 150, allign: 'center'},
					{name: 'afterossname', index: 'afterossname', width: 150, allign: 'center'},
					{name: 'afterlicense', index: 'afterlicense', width: 150, allign: 'center'}
				]
				, autowidth: true
				, height : 'auto'
				, sortorder : 'desc'
				, pager : '#pager'
				, rowNum : 15
				, rowList : [15, 30, 50]
				, loadonce : true // reload 여부, true 로 설정하면 한번만 데이터를 받아오고 그 다음부터는 데이터를 받아오지 않는다
				, viewrecords : true
			});
		}
	};
	
	// bom compare grid
	var bomCompareList = {
		search : function(){
			var bfPrjId = $("#beforePrjId").val();
			var afPrjId = $("#afterPrjId").val();
			
			if (bfPrjId != "" && afPrjId != "") {
				var param = $('#bomcompareSearch').serializeObject();
				
				$.ajax({
					url : '<c:url value="/project/bomCompare/listAjax"/>'
					, data : param
					, dataType : "json"
					, success : function (data){
						var isValid = data.isValid;
						
						if (isValid == "true"){
							var contents = data.resultData.contents;
							var validMsg = data.validMsg;
							
							$("#list").clearGridData();
							$("#list").jqGrid('setGridParam', {data: contents}).trigger('reloadGrid');

							totalRow = contents.length;
						}else{
							$("#list").clearGridData();
						}
					}
				});
			}
		}
	};
</script>