<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
	var totalRow = 0;
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		binaryDataHistoryList.load();
	});
	
	// 함수
	var fn = {
		getDefaultIdNo: function(){
			var ids = [];
			ids = $("#list").jqGrid('getDataIDs');
			
			return (ids.length == 0) ? 0 :ids[0];
		},
		searchDatePick : function(element) {
		    $(element).datepicker();
		},
		downloadExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var filters = $("#list").getGridParam("postData").filters;
				$("#filters").val(filters);
				
				var data = $('#binaryDbLog').serializeObject();
				
				$.ajax({
					type: "POST",
					url: '/exceldownload/getExcelPost',
					data: JSON.stringify({"type":"binaryDBLog", "parameter":JSON.stringify(data)}),
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
		}
	};
	
	var evt = {
		// ready event
		init: function(){
			$("#search").on("click", function(){
				var postData=$('#binaryDbLog').serializeObject();

				$("#list").jqGrid('setGridParam', {postData:postData, page : 1, url : '/system/binaryDataHistory/listAjax'}).trigger('reloadGrid');
			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	};
	
	// 코드 Grid
	var binaryDataHistoryList = {
			load: function(){
				$("#list").jqGrid({
					  datatype: 'json'
					, jsonReader: {
						repeatitems: false,
						root:function(obj){return obj.rows;},
						page:function(obj){return obj.page;},
						total:function(obj){return obj.total;},
						records:function(obj){return obj.records;}
					}
					, colNames: ['ID','Type','Binary Name', 'Binary location', 'Source path (Android models only)', 'CheckSum', 'Tlsh', 'OSS Name', 'OSS Version', 'License', 'Parent Name', 'Platform Name', 'Platform Version', 'Previous Date', 'Update Date', 'Modified by', 'Comment']
					, colModel: [
                        {name: 'actionId', index: 'actionId', width: 70, align: 'center', template: searchStringOptions},
						{name: 'actionType', index: 'actionType', width: 40, align: 'left', template: searchStringOptions},
						{name: 'binaryName', index: 'binaryName', width: 120, align: 'left', template: searchStringOptions},
						{name: 'filePath', index: 'filePath', width: 100, align: 'left', template: searchStringOptions},
						{name: 'sourcePath', index: 'sourcePath', width: 100, align: 'left', template: searchStringOptions},
						{name: 'checkSum', index: 'checkSum', width: 100, align: 'left', template: searchStringOptions},
						{name: 'tlsh', index: 'tlsh', width: 100, align: 'left', template: searchStringOptions},
						{name: 'ossName', index: 'ossName', width: 100, align: 'left', template: searchStringOptions},
						{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left', template: searchStringOptions},
						{name: 'license', index: 'license', width: 100, align: 'left', template: searchStringOptions},
						{name: 'parentname', index: 'parentname', width: 100, align: 'left', template: searchStringOptions},
						{name: 'platformname', index: 'platformname', width: 100, align: 'left', template: searchStringOptions},
						{name: 'platformversion', index: 'platformversion', width: 100, align: 'left', template: searchStringOptions},
						{name: 'updatedate', index: 'updatedate', width: 100, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, template: searchDateOptions},
						{name: 'createdDate', index: 'createdDate', width: 100, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, template: searchDateOptions},
						{name: 'modifier', index: 'modifier', width: 100, align: 'left', template: searchStringOptions},
						{name: 'comment', index: 'comment', width: 100, align: 'left', template: searchStringOptions}
					]
					, emptyrecords: "Nothing to display"
					, autoencode: true
					, editurl: 'clientArray'
					, rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")}
					, rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}]
   					, autowidth: true
					, pager: '#pager'
					, gridview: true
					, sortable: function (permutation) {}
					, sortname: 'actionId'
					, sortorder: 'desc'
					, viewrecords: true
					, height: 'auto'
					, loadonce: false
					// 데이터 로딩 후
					,loadComplete: function(data) {
						totalRow = data.records;
					}
					// row 더블클릭 시 편집모드
					, ondblClickRow: function(rowid,iRow,iCol,e) {}
					, onCellSelect: function(rowid,iCol,cellcontent,e) {
						if(iCol=="0") {
							fn_grid_com.showOssViewPage($("#list"), rowid, true);
						}
					}
					
				});
                $("#list").jqGrid('navGrid',"#pager",{edit:false,add:false,del:false,search:false,refresh:false});
        		$("#list").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
			}
			
	}
//]]>
</script>