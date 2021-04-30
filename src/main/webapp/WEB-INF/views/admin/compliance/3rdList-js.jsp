<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var totalRow = 0;
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		var yearValue = new Date().getFullYear();
		$("#createdDate2").val(yearValue+"0630");
		$("#createdDate1").val((yearValue-1)+"0701");
		partnerList.load();
	});
	
	
	// 이벤트
	var evt = {
		// ready event
		init: function(){
			$("#search").on("click", function(e){
				e.preventDefault();
				var postData = $('#3rdPartySearch').serializeObject();
									
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
			
			$("[name='export']").on("click", function(){
				ajax.getExportPartnerListExcel();
			});
			
			$("#resetDate").on('click', function(e){
				$(".cal").val("");
			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	};
	
	// http 통신
	var ajax = {
		getExportPartnerListExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = $('#3rdPartySearch').serializeObject();
				
				$.ajax({
					type: "POST",
					url: '/exceldownload/getExcelPost',
					data: JSON.stringify({"type":"3rdModel", "parameter":JSON.stringify(data)}),
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
							$("#search").trigger("click");
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
	
	// 코드 Grid
	var partnerList = {
		load: function(){
			$("#list").jqGrid({
				url: '/compliance/listAjax'
				, datatype: 'json'
				, jsonReader: {
					repeatitems: false,
					id:'gridId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				}
				, colNames: ['3rd Party ID', 'Status', '3rd Party Name', 'Software Name', 'Software Version', 'Used Project ID', 'Create Date']
				, colModel: [
                    {name: 'partnerId'		, index: 'partnerId'		, width: 30	, align: 'center'	, sortable : true, key:true},
                    {name: 'status'			, index: 'status'			, width: 30	, align: 'center'	, sortable : true},
                    {name: 'partnerName'	, index: 'partnerName'		, width: 120, align: 'left'		, sortable : true},
					{name: 'softwareName'	, index: 'softwareName'		, width: 120, align: 'left'		, sortable : true},
					{name: 'softwareVersion', index: 'softwareVersion'	, width: 50	, align: 'left'		, sortable : true},
					{name: 'prjId'			, index: 'prjId'			, width: 70 , align: 'center'	, sortable : true},
					{name: 'createdDate'	, index: 'createdDate'		, width: 30 , align: 'center'	, formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true},
				]
				, rownumbers: true
				, emptyrecords: "Nothing to display"
				, autoencode: true
				, editurl: 'clientArray'
				, rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")}
				, rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}]
  				, autowidth: true
				, pager: '#pager'
				, gridview: true
				, sortable: function (permutation) {}
				, sortname: 'partnerId'
				, sortorder: 'desc'
				, viewrecords: true
				, height: 'auto'
				, loadonce: false
				, postData : $('#3rdPartySearch').serializeObject()
				, loadComplete: function(data){
					totalRow = data.records;
					data = data.rows;
					for(var i=0; i<data.length; i++){
						// 조건에 따라 cell 색상 변경
						if(data[i].status){
							if(data[i].status.indexOf("PROG") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", 'Progress');
							}
							if(data[i].status.indexOf("REV") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", "", {'background-color':'#6d7e9c', 'color':'#fff'});
								$("#list").jqGrid("setCell", data[i].partnerId, "status", 'Review');
							}
							if(data[i].status.indexOf("REQ") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", "", {'background-color':'#9da5b8', 'color':'#fff'});
								$("#list").jqGrid("setCell", data[i].partnerId, "status",'Request');
							}
							if(data[i].status.indexOf("CONF") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", "", {'background-color':'#384f7b', 'color':'#fff'});
								$("#list").jqGrid("setCell", data[i].partnerId, "status",'Confirm');
							}							
						}
						if(data[i].deliveryForm){
							if(data[i].deliveryForm == 'SRC'){
								$("#list").jqGrid("setCell", data[i].partnerId, "deliveryForm",'source form');
							}else if(data[i].deliveryForm == 'BIN'){
								$("#list").jqGrid("setCell", data[i].partnerId, "deliveryForm",'binary form');
							}
						}
					}
				}
			});
		}
	};
//]]>
</script>