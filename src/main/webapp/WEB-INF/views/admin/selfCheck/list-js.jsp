<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var userList;
	var userIdList;
	var totalRow = 0;
	var refreshParam = {};
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		fn.getUserIdList();
		showHelpLink("Self-Check_List_Main");
	});
	
	//데이터 객체
	var gridTooltip = {
		typeCodes : [],
	    tooltipCont : "<div class=\"tooltipData\">"
		               +"<dl><dt><span class=\"downSet btnReport\">FOSSLight Report</span>FOSSLight Report</dt></dl><br>"
		               +"<dl><dt><span class=\"downSet btnNotice\">OSS Notice</span>OSS Notice</dt></dl><br>"
		               +"<dl><dt><span class=\"downSet btnPackage\">Packaging File</span>Packaging File</dt></dl><br>"
		               +"</div>",
		existTooltip : false,
		init : function(){
			list.load();	// Grid Load
		}
		
	};	
	
	//OSS 쪽에서 프로젝트 검색을 위한 데이터
	var evt = {
		init : function(){

			refreshParam = $('#projectSearch').serializeObject();
			
			$('select[name=distributionType]').val('${searchBean.distributionType}').trigger('change');
			$('select[name=prjDivision]').val('${searchBean.prjDivision}').trigger('change');
			
			$('#search').on('click',function(e){
				e.preventDefault();
				
				var exPostdata = $("#list").jqGrid('getGridParam','postData');
				exPostdata.ossId = ''; 
				var postData=$('#projectSearch').serializeObject();

				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	}
	
	
	var fn = {
		getUserIdList : function(){
			return $.ajax({
				type: 'GET',
				url: '<c:url value="/project/getUserIdList"/>',
				data: {},
				success : function(data){
					if(data != null){
						userIdList = data.slice(0,-1);
						gridTooltip.init();
					}
				}
			});
		},
		// unformater
		unformatter : function(cellvalue, options, rowObject){
			return cellvalue;
		},
		// Grid download cell display
		displayReportDownload : function(cellvalue, options, rowObject){
			var display = "";

			if(rowObject.ossCount) {
				if(parseInt(rowObject.ossCount) > 0) {
					display = "<input type=\"button\" value=\"Report\" class=\"downSet btnReport\" onclick=\"fn.downloadReport(this)\" title=\"FOSSLight Report\">";
				}
			}
			
			return display;
		},
		// Grid vulnerability cell display
		displayVulnerability : function(cellvalue, options, rowObject){
			var display = "";

			if(parseInt(cellvalue) >= 9.0 ) {
				display="<span class=\"iconSet vulCritical\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 7.0 ) {
				display="<span class=\"iconSet vulHigh\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 4.0) {
				display="<span class=\"iconSet vulMiddle\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) > 0) {
				display="<span class=\"iconSet vulLow\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else {
				display="<span style=\"font-size:0;\"></span>";
			}
			
			return display;
		},
		displayComment : function(cellvalue, options, rowObject){
			var display = "";
			
			if(cellvalue != ""){
				var tmpStr = new RegExp();
				tmpStr = /[<][^>]*[>]/gi;
				
				display ="<div style=\"height : 29px; overflow: hidden;\">"+cellvalue.replace(tmpStr , "")+"</div>";
			}
			
			return display;
		},
		downloadExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = JSON.stringify($('#projectSearch').serializeObject());
			
				$.ajax({
					type: "POST",
					url: '<c:url value="/exceldownload/getExcelPost"/>',
					data: JSON.stringify({"type" : "selfCheckList", "parameter" : data}),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
					success: function (data) {
						if("false" == data.isValid) {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							window.location =  '<c:url value="/exceldownload/getFile?id='+data.validMsg+'"/>';
						}
					},
					error: function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		downloadReport : function(obj){
			var prjId = $(obj).closest('tr').attr('id');
			
			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"selfReport", "parameter":prjId}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/exceldownload/getFile?id='+data.validMsg+'"/>';
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		displayProjectInfo : function(cellvalue, options, rowObject){
			var prjName = rowObject['prjName'];
			var prjVersion = rowObject['prjVersion']||'';
			
			if(prjVersion != ''){
				prjName += ' (' + prjVersion + ')';
			}
			
			return prjName;
		}
	}
	
	// jqGrid
	var list = {
		load : function(){
			$("#list").jqGrid({
				url:'<c:url value="/selfCheck/listAjax"/>',
				datatype: 'json',
				jsonReader:{
					repeatitems: false,
					id:'prjId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID','Project Name (Version)', 'Version', 'Operating System'
				           , 'Distribution Type', 'Download', '', 'Vulnerability', 'Division', 'Creator'
				           , 'Created Date','oss count'],
				colModel: [
					{name: 'prjId', index: 'prjId', width: 50, align: 'center', sorttype: 'int'},
					{name: 'prjName', index: 'prjName', width: 300, align: 'left', formatter:fn.displayProjectInfo, unformatter:fn.unformatter},
					{name: 'prjVersion', index: 'prjVersion', width: 50, align: 'left',hidden:true},
					{name: 'osType', index: 'osTypeEtc', width: 100, align: 'left', sortable : true/* , formatter:fn.displayOsType */, hidden:true},
					{name: 'distributionType', index: 'destributionName', width: 100, align: 'left', sortable : true<c:if test="${!distributionFlag}">, hidden:true</c:if>},
					{name: 'download', index: 'download', width: 50, align: 'center', formatter:fn.displayReportDownload, unformatter:fn.unformatter, sortable : false, title:false},
					{name: 'cveId', index: 'cveId', hidden:true},
					{name: 'cvssScore', index: 'cvssScore', width: 50, align: 'center', formatter:fn.displayVulnerability, unformatter:fn.unformatter, sortable : true},
					{name: 'division', index: 'division', width: 70, align: 'left', sortable : true},
					{name: 'creator', index: 'creator', width: 70, align: 'left'},
					{name: 'createdDate', index: 'createdDate', width: 80, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}},
					{name: 'ossCount', index: 'ossCount', width: 50, align: 'left',hidden:true}
				],
				rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")},
				rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}],
				editurl:'clientArray',
	 			autowidth: true,
				pager: '#pager',
				gridview: true,
				sortable: function (permutation) {
				},
				sortname: 'prjId',
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto',
				loadonce:false,
				loadComplete:function(data) {
					totalRow = data.records;
					lastsel=-1;
					
					// color 설정
					var target = $("#list");
					var arr = target.jqGrid('getDataIDs');
					var selectEl = "";
					var status ="";
					var rowid;
					
					$('input[id*="_releaseDate"]').attr('class', 'cal');

					if(totalRow == 0){
						var startDate = $("#schStartDate").val()||0;
						var endDate = $("#schEndDate").val()||0;
						var diffNum = +startDate - +endDate;
						
						if(diffNum > 0 && endDate > 0){
							alertify.alert('<spring:message code="msg.common.search.check.date" />', function(){});
						}
					}
				},
				onCellSelect: function(rowid,iCol,cellcontent,e) {},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					var rowData = $("#list").jqGrid('getRowData',rowid);
					var isAdmin = ${ct:isAdmin()};
					
					createTabInFrame(rowData['prjId']+'_selfCheck', '#<c:url value="/selfCheck/edit/'+rowData['prjId']+'"/>');
				},
				postData : refreshParam
			});
		}
	};
//]]>
</script>