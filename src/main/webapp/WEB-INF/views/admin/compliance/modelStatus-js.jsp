<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var modelListArr = new Array();
	var productGroupListArr = new Array();
	var initStatus = "init";
	var gModelName = "";
	var gRows = "";
	var sortKey = new Object();
	var totalRow = 0;
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";

	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		var yearValue = new Date().getFullYear();
		$("#schEndDate").val(yearValue+"0630");
		$("#schStartDate").val((yearValue-1)+"0701");
		
		evt.init();
		modelList.load();
	});
	
	// 이벤트
	var evt = {
		init: function(){
			var accept4 = '';
	 		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
				<c:if test="${file eq '22'}">
				accept4 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
				</c:if>
			</c:forEach>
			$('#modelListFile').uploadFile({
				url:'/compliance/readModelList',
				multiple:false,
				dragDrop:true,
				allowedTypes:accept4,
				fileName:"myfile",
				sequential:true,
				sequentialCount:1,
				dynamicFormData: function(){
					var data ={ "registFileId" : ''}
					return data;
				},
				onSubmit:function(files){						
					var accept4 = "";
					var ext = files[0].split(".")[1];
					
					<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
						<c:if test="${file eq '22'}">
						accept4 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
						</c:if>
					</c:forEach>
					
					if(accept4.indexOf(ext) == -1){
						return false;
					}
				},
				onSuccess:function(files,data,xhr,pd){
					modelListArr = new Array();
					productGroupListArr = new Array();
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
					
					if(data.isValid == 'false') {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else if(fn.checkValid()){
						for(var i in data.resultData){
							var obj = data.resultData[i];
							var duplicateFlag = false;
							if(modelListArr.indexOf(obj.modelName) == -1){ // 중복제거
								modelListArr.push(obj.modelName);
								duplicateFlag = true;
							}
							if(duplicateFlag || obj.productGroup == ""){ // 중복제거
								productGroupListArr.push(obj.productGroup);
							}
						}
						
						var postData = $("#modelListSearch").serializeObject();
						
						if(modelListArr.length >= 0){
							postData.modelName = modelListArr.join(",");
						}
						
						initStatus = "read";
						
						postData["sidx"] = "modelName";
						postData["sord"] = "asc";
						postData["startIndex"] = "0";
						postData["pageListSize"] = "9999999";
						gModelName = modelListArr.join(",");
						
						fn.getModelListAjax(postData);
						
					}
				},
				onError:function(files,status,errMsg,pd){
					var errLength = $('.ajax-file-upload-statusbar').length;
					
					if(errLength > 1){
						$('.ajax-file-upload-statusbar:eq(0)').fadeOut('slow');
						$('.ajax-file-upload-statusbar:eq(0)').remove();
					}
					
					modelListArr = new Array();
					productGroupListArr = new Array();
					$("#list").jqGrid('clearGridData');
					
					window.setTimeout(function(){
						$(".ajax-file-upload-error").text("ERROR: The uploaded file does not contain a sheet named 'All Model(Software) List'.");
					}, 1);
					
					alertify.error("The uploaded file does not contain a sheet named 'All Model(Software) List'.", 0);
				}
			});
			
			$("[name='export']").on('click',function(){
				ajax.getExportModelListExcel();
			});
			
			$("#resetDate").on('click', function(e){
				fn.resetDate();
			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	};
	
	
	// 함수
	var fn = {
		checkValid : function() {
			var searchParam = $("#3rdSearch").serialize().split("&");
			
			for(var i in searchParam){
				var strSplit = searchParam[i].split("=");
				var key = strSplit[0];
				var value = strSplit[1];
				
				switch(key) {
					case "division":
						if(value == "") {
							alertify.alert('<spring:message code="msg.project.required.selectDivision" />', function(){});

							return false;
						}
						
						break;
					case "createdDate1":
						if(value == "") {
							alertify.alert('Please, enter the created date first.', function(){});

							return false;
						}
						
						break;
					case "createdDate2":
						if(value == "") {
							alertify.alert('Please, enter the created date first.', function(){});

							return false;
						}
						
						break;
					default:
						break;
				}
			}
			
			return true;
		},
		// unformater
		unformatter : function(cellvalue, options, rowObject){
			return cellvalue;
		},
		// Grid Status cell display
		displayStatus : function(cellvalue, options, rowObject){
// 			205 COMP	Complete
// 			205 PROG	Progress
// 			205 REQ		Request
// 			205 REV		Review
			var display = "";
			switch(cellvalue){
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_PROGRESS'))}":
					display = "<span class=\"iconSt draft\">"+ cellvalue +"</span>";

					break;
					
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_REQUEST'))}":
					display = "<span class=\"iconSt request\">Request</span>";

					break;
					
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_REVIEW'))}":
					display = "<span class=\"iconSt review\">Review</span>";

					break;
					
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}":
					display = "<span class=\"iconSt complete\">"+cellvalue+"</span>";
					break;
			}
			return display;
		},
		// Grid status cell attr
		cellattrStatus : function(cellvalue, options, rowObject){
			// 프로젝트 상태가 Delay 경우 상태컬럼 노란색 배경으로 설정
			var isDelay = false;
			if(isDelay) { return 'style="background-color:yellow"'; }
		},
		// Grid distribution cell display
		displayDistribution : function(cellvalue, options, rowObject){
			var display = "";
			if(rowObject.identificationStatus == 'Confirm' && rowObject.verificationStatus == "N/A" && cellvalue == "N/A") {
				display = cellvalue;
			} else if( rowObject.verificationStatus != 'Confirm') {
				if(rowObject.status != "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}" ) {
					display = "";
				} else {
					display = "N/A";
				}
			} else {
				switch(cellvalue) {
					case "":
						if(rowObject.status != "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}" ){
							display = "";
						} else {
							display = "N/A";
						}

						break;
						
					default:
						if(rowObject.distributeTarget == "NA"){
							display = "N/A";
						} else {
							display = cellvalue;
						}
					
						break;
				}
			}
			
			return display;
		},
		displayDistributeDate : function(cellvalue, options, rowObject){
			if(cellvalue != null){
				return cellvalue.replace(/(.{4})(.{2})(.{2})/, "$1-$2-$3");
			}else{
				return "";
			}
		},
		// Grid status cell attr
		cellattrStatus : function(cellvalue, options, rowObject){
			// 프로젝트 상태가 Delay 경우 상태컬럼 노란색 배경으로 설정
			var isDelay = false;
			if(isDelay) { return 'style="background-color:yellow"'; }
		},
		resetDate : function(){
			$(".cal").val("");
		},
		getModelListAjax : function(obj){
			$.ajax({
				type: "POST",
   				url: '/compliance/modelListAjax',
   				data: JSON.stringify(obj),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
   				success: function (data) {
	   				if(data.records >= 0){
	   					$("#list").jqGrid('GridUnload');	// Grid 초기화
	   					modelList.load(data.rows);
	   					gRows = data.rows;
	   					
	   					$(".s-ico").css("display", "none");
						$("#jqgh_list_"+obj.sidx + " > .s-ico").css("display", "");
						
						if(obj.sord == "asc"){
							$("#jqgh_list_"+ obj.sidx + " > .s-ico > span:eq(0)").removeClass("ui-state-disabled");
							$("#jqgh_list_"+ obj.sidx + " > .s-ico > span:eq(1)").addClass("ui-state-disabled");
						}else if(orderby == "desc"){
							$("#jqgh_list_"+ obj.sidx + " > .s-ico > span:eq(0)").addClass("ui-state-disabled");
							$("#jqgh_list_"+ obj.sidx + " > .s-ico > span:eq(1)").removeClass("ui-state-disabled");
						}
	   				}else{
	   					alertify.error('<spring:message code="msg.common.valid" />', 0);
	   				}
	   				
   				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	}
	
	//데이터 객체
	var gridTooltip = {
		typeCodes : [],
		tooltipCont : "<div class=\"tooltipData\">"
	                   +"<dl><dt><span class=\"iconSt draft\">Progress</span>Progress</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt request\">Request</span>Request</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt review\">Review</span>Review</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt complete\">Complete</span>Complete</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt delay\">Delay</span>Delay</dt></dl><br>"
	                   +"</div>",
	    tooltipCont1 : "<div class=\"tooltipData\">"
		               +"<dl><dt><span class=\"downSet btnReport\">FOSSLight Report</span>FOSSLight Report</dt></dl><br>"
		               +"<dl><dt><span class=\"downSet btnNotice\">OSS Notice</span>OSS Notice</dt></dl><br>"
		               +"<dl><dt><span class=\"downSet btnPackage\">Packaging File</span>Packaging File</dt></dl><br>"
		               +"</div>",
		existTooltip : false,
		init : function(){
			list.load();	// Grid Load
		}
		
	};	
	
	// http 통신
	var ajax = {
		getExportModelListExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = $('#modelListSearch').serializeObject();
				
				if(modelListArr.length >= 0){
					data.modelName = modelListArr.join(",");		
					data.productGroup = productGroupListArr.join(",");
				}
			
				$.ajax({
					type: "POST",
	   				url: '/exceldownload/getExcelPost',
	   				data: JSON.stringify({"type":"modelStatus", "parameter":JSON.stringify(data)}),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
	   				success: function (data) {
		   				if("false" == data.isValid) {
			   				if(data.validMsg == undefined){
			   					alertify.error('<spring:message code="msg.common.valid" />', 0);
				   			} else if(data.validMsg == "overflow"){
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
	}
	
	var modelList = {
		load: function(mData){
			$("#list").jqGrid({
				  datatype: 'local'
				, data : mData
				, colNames: ['Model(Software) Name', 'Project ID', 'Status', 'Distribution Type', 'Distribution Status', 'Distribute Date']
				, colModel: [
					{name: 'modelName'			, index: 'modelName'			, width: 50	, align: 'left'		, sortable : true, key:true},
					{name: 'prjId'				, index: 'prjId'				, width: 30	, align: 'center'	, sortable : true},
					{name: 'status'				, index: 'status'				, width: 50	, align: 'center'	, formatter: fn.displayStatus, unformatter: fn.unformatter, cellattr: fn.cellattrStatus, sortable : true},
					{name: 'distributionType'	, index: 'distributionType'		, width: 120, align: 'center'	, sortable : true},
					{name: 'destributionStatus'	, index: 'destributionStatus'	, width: 80	, align: 'center'	, formatter: fn.displayDistribution, unformatter: fn.unformatter, sortable : true, title:false},
					{name: 'distributeDeployTime', index: 'distributeDeployTime', width: 30 , align: 'center'	, formatter: fn.displayDistributeDate, sortable : true}
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
				, sortable: false
				, sortorder: 'asc'
				, viewrecords: true
				, height: 'auto'
				, loadonce: false
				// 데이터 로딩 후
				, loadComplete: function(data) {
					totalRow = data.records;
					if(!data.records && initStatus != "init"){
						alertify.error("Information that matches the value written in the 'Model (Software) Name' column can not be found in the FOSSLight system.", 0);
					}
				},
				onSortCol : function(colNm, colIdx, sOrd){
					if(gModelName.length > 0){
						
						if(Object.keys(sortKey).indexOf(colNm) > -1){
							orderby = sortKey[colNm] == "asc" ? "desc" : "asc";
							sortKey[colNm] = orderby;
						}else{
							orderby = "asc";
							sortKey[colNm] = orderby;
						}
						
						var postData = $("#modelListSearch").serializeObject();
						
						postData["sidx"] = colNm;
						postData["sord"] = orderby;
						postData["startIndex"] = "0";
						postData["pageListSize"] = "9999999";
						postData["modelName"] = gModelName;
						
						fn.getModelListAjax(postData);	
					}
					
				}
			});
		}
	};
//]]>
</script>