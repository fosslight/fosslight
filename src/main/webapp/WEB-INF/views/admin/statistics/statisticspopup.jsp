<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
			var divisionList = '${ct:getCodeNames(ct:getConstDef("CD_USER_DIVISION"))}';
			var chartName = "${chartName}";
			var startDate = "${startDate}";
			var endDate = "${endDate}";
			var categoryType = "${categoryType}";
			var divisionNo = "${divisionNo}";
			var pieSize = "${pieSize}";
			var chartType = "${chartType}";
			
			$(document).ready(function() {
				common_fn.init();
			});
	
			var common_fn = {
				init : function(){
					if(typeof divisionList == "string"){
						divisionList = divisionList.replace(/[\[\]]/g, "").split(",");
					}

					chart_fn[chartName]();	
				}
			}
	
			var chart_fn = {
					divisionalProjectChart : function(){
						var params = {};
						params["startDate"] = startDate;
						params["endDate"] = endDate;
						params["categoryType"] = categoryType;
						params["isRawData"] = "Y";
						
						$.ajax({
							url : '/statistics/divisionProjectChart',
							type : 'GET',
							dataType : 'json',
							cache : false,
							data : params,
							success : function(data){
								var colNames = ['Division'];
								var colModel = [
									{name: 'divisionNm', index: 'divisionNm', width: 50, align: 'center', key:true}
								];

								for(var idx in data.titleArray){
									colNames.push(data.titleArray[idx]);
									
									var obj = {
										name: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , index: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , width: 50
									  , align: 'center'
									};
									
									colModel.push(obj);
								}
								
								grid_fn.chartRawDataOpen(data.chartData, colNames, colModel);
							},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					},
					mostUsedOssChart : function(){
						var params = {};
						params["startDate"] = startDate;
						params["endDate"] = endDate;
						params["divisionNo"] = divisionNo;
						params["pieSize"] = pieSize;
						params["chartType"] = chartType;
						params["isRawData"] = "Y";
						
						$.ajax({
							url : '/statistics/mostUsedChart',
							type : 'GET',
							dataType : 'json',
							cache : false,
							data : params,
							success : function(data){
								var colNames = ['OSS Name', 'OSS Cnt'];
								var colModel = [
									{name: 'columnName', index: 'columnName', width: 50, align: 'center', key:true},
									{name: 'columnCnt', index: 'columnCnt', width: 50, align: 'center'}
								];
								
								grid_fn.chartRawDataOpen(data.chartData, colNames, colModel, "PIE");
							},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					},
					mostUsedLicenseChart : function(){
						var params = {};
						params["startDate"] = startDate;
						params["endDate"] = endDate;
						params["divisionNo"] = divisionNo;
						params["pieSize"] = pieSize;
						params["chartType"] = chartType;
						params["isRawData"] = "Y";
						
						$.ajax({
							url : '/statistics/mostUsedChart',
							type : 'GET',
							dataType : 'json',
							cache : false,
							data : params,
							success : function(data){
								var colNames = ['License Name', 'License Cnt'];
								var colModel = [
									{name: 'columnName', index: 'columnName', width: 50, align: 'center', key:true},
									{name: 'columnCnt', index: 'columnCnt', width: 50, align: 'center'}
								];
								
								grid_fn.chartRawDataOpen(data.chartData, colNames, colModel, "PIE");
							},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					},
					updatedOssChart : function(){
						var params = {};
						params["startDate"] = startDate;
						params["endDate"] = endDate;
						params["categoryType"] = categoryType;
						params["chartType"] = chartType;
						params["isRawData"] = "Y";
						
						$.ajax({
							url : '/statistics/updatedChart',
							type : 'GET',
							dataType : 'json',
							cache : false,
							data : params,
							success : function(data){
								var colNames = ['Update Date'];
								var colModel = [
									{name: 'columnName', index: 'columnName', width: 50, align: 'center', key:true}
								];
								
								for(var idx in data.titleArray){
									colNames.push(data.titleArray[idx]);
									var obj = {
										name: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , index: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , width: 50
									  , align: 'center'
									};
									colModel.push(obj);
								}
								
								grid_fn.chartRawDataOpen(data.chartData, colNames, colModel);
							},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					},
					updatedLicenseChart : function(){
						var params = {};
						params["startDate"] = startDate;
						params["endDate"] = endDate;
						params["categoryType"] = categoryType;
						params["chartType"] = chartType;
						params["isRawData"] = "Y";
						
						$.ajax({
							url : '/statistics/updatedChart',
							type : 'GET',
							dataType : 'json',
							cache : false,
							data : params,
							success : function(data){
								var colNames = ['Update Date'];
								var colModel = [
									{name: 'columnName', index: 'columnName', width: 50, align: 'center', key:true}
								];

								for(var idx in data.titleArray){
									colNames.push(data.titleArray[idx]);
									var obj = {
										name: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'	
									  , index: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , width: 50
									  , align: 'center'
									};
									
									colModel.push(obj);
								}
								
								grid_fn.chartRawDataOpen(data.chartData, colNames, colModel);
							},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					},
					trdPartyRelatedChart : function(){
						var params = {};
						params["startDate"] = startDate;
						params["endDate"] = endDate;
						params["categoryType"] = categoryType;
						params["isRawData"] = "Y";
						
						$.ajax({
							url : '/statistics/trdPartyRelatedChart',
							type : 'GET',
							dataType : 'json',
							cache : false,
							data : params,
							success : function(data){
								var colNames = ['Division'];
								var colModel = [
									{name: 'divisionNm', index: 'divisionNm', width: 50, align: 'center', key:true}
								];

								for(var idx in data.titleArray){
									colNames.push(data.titleArray[idx]);
									
									var obj = {
										name: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , index: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , width: 50
									  , align: 'center'
									};
									
									colModel.push(obj);
								}
								
								grid_fn.chartRawDataOpen(data.chartData, colNames, colModel);
							},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					},
					userRelatedChart : function(){
						var params = {};
						params["startDate"] = startDate;
						params["endDate"] = endDate;
						params["isRawData"] = "Y";
						
						$.ajax({
							url : '/statistics/userRelatedChart',
							type : 'GET',
							dataType : 'json',
							cache : false,
							data : params,
							success : function(data){
								var colNames = ['Division'];
								var colModel = [
									{name: 'divisionNm', index: 'divisionNm', width: 50, align: 'center', key:true}
								];

								for(var idx in data.titleArray){
									colNames.push(data.titleArray[idx]);
									
									var obj = {
										name: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , index: data.titleArray.length-1 == idx ? 'total' : 'category'+idx+'Cnt'
									  , width: 50
									  , align: 'center'
									};
									
									colModel.push(obj);
								}
								
								grid_fn.chartRawDataOpen(data.chartData, colNames, colModel);
							},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					}
			};

			var grid_fn = {
					chartRawDataOpen : function(mData, colNames, colModel, chartType){
					$("#chartRawData").jqGrid({
						  datatype: 'local'
						, data : mData
						, colNames: colNames
						, colModel: colModel
						, rownumbers: true
						, emptyrecords: "Nothing to display"
						, autoencode: true
						, editurl: 'clientArray'
						, rowNum: 1000
		  				, autowidth: true
						, pager: '#pager'
						, gridview: true
						, sortable: false
						, sortorder: 'asc'
						, viewrecords: true
						, height: 'auto'
						, loadonce: false
						, loadComplete: function(data) { // 데이터 로딩 후
							var rowId = $("#chartRawData").getGridParam("reccount");
							var rowObject = $("#chartRawData").getRowData(rowId);
							rowObject[colModel[0].name] = 'Total';
							
							if(chartType == "PIE"){
								rowObject['columnCnt'] = $("#chartRawData").jqGrid('getCol', 'columnCnt', false, 'sum');
							} else {
								for(var idx in colModel){
									if(idx == 0){
										continue;
									}
									
									if(colModel[idx].name == "total"){
										rowObject.total = $("#chartRawData").jqGrid('getCol', 'total', false, 'sum');
									}else {
										rowObject['category'+(idx-1)+'Cnt'] = $("#chartRawData").jqGrid('getCol', 'category'+(idx-1)+'Cnt', false, 'sum');
									}
								}
							}

							$("#chartRawData").jqGrid("addRowData", rowId+1, rowObject, 'last');
						}
					});
				}	
			};
		</script>
	</head>
	<body>
		<div id="loading_wrap_popup" class="loading" style="display:none;">
			<div class="loadingBlind"></div>
			<img src="/images/loading.gif" alt="loading" />
		</div>
		<div id="wrap" style="padding-top: 10px;">
			<div  align="center" >
			<div class="jqGridSet" style="overflow: auto; width: 95%; height: 650px;"> 
				<table id="chartRawData"><tr><td></td></tr></table>
			</div>
			</div>
		</div>
	</body>
</html>