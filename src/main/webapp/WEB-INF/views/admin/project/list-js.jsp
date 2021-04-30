<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var userList;
	var userIdList;
	var pastEmpList;
	var refreshParam = {};
	var totalRow = 0;
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		
		fn.getUserIdList("Y", "REVIEWER"); // 근무자
		fn.getUserIdList("N", "PASTEMP"); // 퇴사자

		// TODO - 추후 기능이 정리되면 주석제거할 예정
		//if('${sessUserInfo.authority}'=="ROLE_ADMIN"){
			//$('.btnReject').show();
		//}else{
			$('.btnReject').hide();
		//}
		
		showHelpLink("Project_List_Main");
	});	
	
	//데이터 객체
	var gridTooltip = {
		typeCodes : [],
		tooltipCont : "<div class=\"tooltipData\">"
	                   +"<dl><dt><span class=\"iconSt draft\">Progress</span>Progress</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt request\">Request</span>Request</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt review\">Review</span>Review</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt complete\">Complete</span>Complete</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt drop\">Drop</span>Drop</dt></dl><br>"
	                   +"</div>",
	    tooltipCont1 : "<div class=\"tooltipData\">"
		               +"<dl><dt><span class=\"downSet btnReport\">OSS Report</span>OSS Report</dt></dl><br>"
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
			
			refreshParam = fn.setGridParam();
			
			$('#search').on('click',function(e){
				e.preventDefault();
				var exPostdata = $("#list").jqGrid('getGridParam','postData');
				exPostdata.ossId = ''; 
				var postData = fn.setGridParam();
				
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
			
			$('.btnReject').on('click',function(e){
				fn.rejectPopOpen();
			});
			
			$('#popReject').click(function(){
				fn.exeReject();
			});				
			
			$('#popCancel').click(function(){
				$('#rejectPop').hide();
			});
			
			$('select[name=distributionType]').val('${searchBean.distributionType}').trigger('change');
			$('select[name=prjDivision]').val('${searchBean.prjDivision}').trigger('change');
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	}
	
	
	var fn = {
		getUserIdList : function(adminYn, type){
			return $.ajax({
				type: 'GET',
				url: "/project/getUserIdList",
				data: {adminYn : adminYn},
				success : function(data){
					if(data != null){
						temp = data.split(";").reduce(function(obj, cur){
						    var keys = Object.keys(obj);
						    var pairData = cur.split(":");

						    if(keys.indexOf(pairData[0]) == -1 && pairData[0].trim() != ""){
						        obj[pairData[0]] = pairData[1];
						    }

						    return obj;
						}, {});
						
						if(type == "REVIEWER"){
							userIdList = temp;
						} else {
							pastEmpList = temp;
							gridTooltip.init();
						}
					}
				}
			});
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
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}":
					display = "<span class=\"iconSt drop\">"+cellvalue+"</span>";

					break;
			}
			
			return display;
		},
		// Grid status cell attr
		cellattrStatus : function(cellvalue, options, rowObject){
			// project Priority의 값을 기준으로 background의 색상을 표기함.
			var styleStr = "";
			var priority = (rowObject["priority"]).toUpperCase();
			
			switch(priority){
				case "P0":	styleStr = 'style="background-color:#FEAEC9"';	break; // pink
				case "P1":	styleStr = 'style="background-color:#FFFCB7"';	break; // light yellow
				default:	break; 
			}

			return styleStr;
		},
		// Grid identification cell display
		displayIdentification : function(cellvalue, options, rowObject){
// 			206 CONF	Confirm
// 			206 NA		N/A
// 			206 PROG	Progress
// 			206 REQ		Request
// 			206 REV		Review
			var display = "";
			var hasOss = false;
			
			switch(cellvalue){
				case "":
					if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}"){
						display = "<div class=\"tcenter\">Drop</div>";
					} else {
						if("Y" == rowObject.androidFlag) {
							display = "<div class=\"tcenter\"><a class='btnPG wauto' onclick=\"fn.mvIdentification("+options.rowId+",3)\">Start</a></div>";
						} else {
							display = "<div class=\"tcenter\"><a class='btnPG wauto' onclick=\"fn.mvIdentification("+options.rowId+",0)\">Start</a></div>";
						}
					}
					
					break;
				case "N/A":
					display = "<div class=\"tcenter\">"+cellvalue+"</div>";
										
					break;
				default:
					if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}" && cellvalue != "Confirm"){
						display = "<span class=\" indentStep\">Drop</span>&nbsp;";
					} else {
						display = "<span class=\" indentStep\">"+cellvalue+"</span>&nbsp;";
					}

					if("Y" != rowObject.androidFlag) {
						// 버튼 생성 3rd, SRC, BAT
						// 2018-08-28 choye 수정
						<c:if test="${partnerFlag}">
						if(rowObject.identificationSubStatusPartner =="Y" ) {
							display += "<a class='btnPG on3rd' onclick=\"fn.mvIdentification("+options.rowId+",0)\">3rd</a>"; hasOss = true;
						} else if(rowObject.identificationSubStatusPartner == "N") {
							display += "<a class='btnPG off' onclick=\"fn.mvIdentification("+options.rowId+",0)\">3rd</a>";
						} else {
							display += "<a class='btnPG' onclick=\"fn.mvIdentification("+options.rowId+",0)\">3rd</a>";
						}
						</c:if>
					
						// 2018-08-28 choye 수정
						if(rowObject.identificationSubStatusSrc =="Y" ) {
							display += "<a class='btnPG onSrc' onclick=\"fn.mvIdentification("+options.rowId+",1)\">SRC</a>"; hasOss = true;
						} else if(rowObject.identificationSubStatusSrc == "N") {
							display += "<a class='btnPG off' onclick=\"fn.mvIdentification("+options.rowId+",1)\">SRC</a>";
						} else {
							display += "<a class='btnPG' onclick=\"fn.mvIdentification("+options.rowId+",1)\">SRC</a>";
						}

						// 2018-08-28 choye 수정
						if(rowObject.identificationSubStatusBin =="Y" ) {
							display += "<a class='btnPG onBin' onclick=\"fn.mvIdentification("+options.rowId+",2)\">BIN</a>"; hasOss = true;
						} else if(rowObject.identificationSubStatusBin == "N") {
							display += "<a class='btnPG off' onclick=\"fn.mvIdentification("+options.rowId+",2)\">BIN</a>";
						} else {
							display += "<a class='btnPG' onclick=\"fn.mvIdentification("+options.rowId+",2)\">BIN</a>";
						}

						if(rowObject.identificationSubStatusBom == 0) {
							display += "<a class='btnPG off' onclick=\"fn.mvIdentification("+options.rowId+",4)\">BOM</a>";
						} else {
							display += "<a class='btnPG' onclick=\"fn.mvIdentification("+options.rowId+",4)\">BOM</a>";
						}
					} else if("Y" == rowObject.androidFlag) {
						if(rowObject.identificationSubStatusAndroid =="Y" ) {
							display += "<a class='btnPG wAnd onAnd' onclick=\"fn.mvIdentification("+options.rowId+",3)\">"+rowObject.noticeTypeEtc+"</a>";
						} else if(rowObject.identificationSubStatusAndroid == "N") {
							display += "<a class='btnPG wAnd off' onclick=\"fn.mvIdentification("+options.rowId+",3)\">"+rowObject.noticeTypeEtc+"</a>";
						} else {
							display += "<a class='btnPG wAnd' onclick=\"fn.mvIdentification("+options.rowId+",3)\">"+rowObject.noticeTypeEtc+"</a>";
						}
					}
					
					if(rowObject.identificationSubStatusBat =="Y" ) {
						display += "<a class='btnPG onBat' onclick=\"fn.mvIdentification("+options.rowId+",5)\">BAT</a>";
					}
					
					break;
			}
			
			return display;
		},
		// Grid veritification cell display
		displayVerification : function(cellvalue, options, rowObject){
			var display = "";
			
			if(rowObject.identificationStatus != 'Confirm') {
				if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}"
					|| rowObject.statusRequestYn == "Y" ) {
					display = "N/A";
				} else {
					display = "";
				}
			} else {
				if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}") {
					switch(cellvalue) {
						case "":
							display = "N/A";

							break;
						case "Progress":
						case "Request":
						case "Review":
							display = "Drop";

							break;
						default:
							display = cellvalue;

						break;
					}
				} else {
					switch(cellvalue) {
						case "":
							if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}" 
								|| rowObject.statusRequestYn == "Y" ) {
								display = "N/A";
							} else if(rowObject.identificationStatus!="") {
								display = "<div class=\"tcenter\"><a class='btnPG wauto' onclick=\"fn.mvVerification("+options.rowId+")\">Start</a></div>";
							}
							
							break;
						case "Progress":
							if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}" 
								|| rowObject.statusRequestYn == "Y" ) {
								display = "N/A";
							} else {
								display = cellvalue;
							}
							
							break;
						case "N/A":
							display = "N/A";
							
							break;
						default:
							display = cellvalue;
						
							break;
					}
				}
			}
			
			return display;
		},
		// Grid distribution cell display
		displayDistribution : function(cellvalue, options, rowObject){
			var display = "";
			if(rowObject.identificationStatus == 'Confirm' && rowObject.verificationStatus == "N/A" && cellvalue == "N/A") {
				display = cellvalue;
			} else if( rowObject.verificationStatus != 'Confirm'){
				if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}"){
					switch(cellvalue){
						case "":
							display = (rowObject.verificationStatus != "Confirm") ? "N/A" : "Drop";
							
							break;
						case "Progress":
							display = "Drop";
							
							break;
						default:
							display = cellvalue;
						
							break;
					}
				} else {
					if(rowObject.status != "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}"
						 && rowObject.statusRequestYn != "Y" ) {
						display = "";
					} else {
						display = "N/A";
					}
				}
			} else {
				if(rowObject.status == "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}"){
					display = "Drop";
				} else {
					switch(cellvalue){
						case "":
							if(rowObject.status != "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}"){
								display = "<div class=\"tcenter\"><a class='btnPG wauto' onclick=\"fn.mvDistribution("+options.rowId+")\">Start</a></div>";
							} else {
								display = "N/A";
							}
							
							break;
						case "Reserve":
							display = cellvalue;
							
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
			}
			
			return display;
		},
		// Grid download cell display
		displayReportDownload : function(cellvalue, options, rowObject){
			var display = "";
			
			if(rowObject.identificationStatus == "Confirm"){
				display+="<input type=\"button\" value=\"Report\" class=\"downSet btnReport\" onclick=\"fn.downloadReport(this)\" title=\"OSS Report\">";
			} else {
				display+="<input type=\"button\" value=\"Report\" class=\"downSet btnReport dis\" onclick=\"fn.downloadReport(this)\" disabled>";
			}
			
			if(rowObject.verificationStatus == "Confirm"){
				if(rowObject.noticeType == "99" || rowObject.noticeFileId == "") {
					display+="<input type=\"button\" value=\"Notice\" class=\"downSet btnNotice dis\" onclick=\"fn.downloadNotice(this)\" disabled>";	
				} else {
					display+="<input type=\"button\" value=\"Notice\" class=\"downSet btnNotice\" onclick=\"fn.downloadNotice(this)\" title=\"OSS Notice\">";
				}
				
				if(rowObject.packageFileId!=""){
					display+="<input type=\"button\" value=\"Package\" class=\"downSet btnPackage\" onclick=\"fn.downloadPackage(this)\" title=\"Packaging File1\">";
				} else {
					display+="<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackage(this)\" disabled>";
				}
				
				if(rowObject.packageFileId2!=""){
					display+="<input type=\"button\" value=\"Package\" class=\"downSet btnPackage\" onclick=\"fn.downloadPackageMulti(this, \'2\')\" title=\"Packaging File2\">";
				} else {
					display+="<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackageMulti(this, \'2\')\" disabled>";
				}
				
				if(rowObject.packageFileId3!=""){
					display+="<input type=\"button\" value=\"Package\" class=\"downSet btnPackage\" onclick=\"fn.downloadPackageMulti(this, \'3\')\" title=\"Packaging File3\">";
				} else {
					display+="<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackageMulti(this, \'3\')\" disabled>";
				}
			} else {
				display+="<input type=\"button\" value=\"Notice\" class=\"downSet btnNotice dis\" onclick=\"fn.downloadNotice(this)\" disabled>";
				display+="<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackage(this)\" disabled>"
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
			
			if(cellvalue !="") {
				var tmpStr = new RegExp();
				tmpStr = /[<][^>]*[>]/gi;
				display ="<div style=\"height : 29px; overflow: hidden;\">"+cellvalue.replace(tmpStr , "")+"</div>";
			}
			
			return display;
		},
		// Grid identification display event
		mvIdentification : function(prjId, initDiv){
			createTabInFrame(prjId+'_Identify', '#/project/identification/'+prjId+'/'+initDiv);
		},
		// Grid veritification display event
		mvVerification : function(prjId){
			createTabInFrame(prjId+'_Packaging', '#/project/verification/'+prjId);
		},
		// Grid distribution display event
		mvDistribution : function(prjId){
			createTabInFrame(prjId+'_Distribute', '#/project/distribution/'+prjId);
		},
		// Grid reviewer change event
		reviewerChg : function(){
			var prjId = (this.id).replace(/[^0-9]/g,'');
			var reviewer = Object.keys(userIdList)[Object.keys(userIdList)
														.map(function(e) {
															  return userIdList[e]
														})
														.indexOf(this.value)];
			var data = {"prjId" : prjId, "reviewer" : reviewer};
			
			$.ajax({
				url : '/project/updateReviewer',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					$("#list").jqGrid('saveRow',prjId);
					$("#list").jqGrid('setCell', prjId, "reviewer", reviewer);
					
					alertify.success('<spring:message code="msg.common.success" />');
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			})
		},
		// Reject pop open
		rejectPopOpen : function(){
			var selrows = $("#list").jqGrid('getGridParam','selarrrow');
			
			if(selrows.length > 0){
				// 초기화
				$("input:radio[name='radioName']:radio[value='0']").attr("checked",true);
				$('#rejectPop').show();
			} else {
				alert("대상 프로젝트를 선택하여 주십시오.");
				return false;
			}
		},
		exeReject : function(selrows){
			var selrows = $("#list").jqGrid('getGridParam','selarrrow');
			// 라디오 identification = 0, verification = 1
			var rValue = $("input:radio[name='radioName']:checked").val();
			var data = {"prjIds" : selrows, "identificationStatus" : rValue};
			
			$.ajax({
				url : '/project/updateReject',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					$('#rejectPop').hide();
					$('#search').click();
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
					$('#rejectPop').hide();
				}
			})
		},
		downloadExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = fn.setGridParam();
				
				$.ajax({
					   type: "POST",
					   url: '/exceldownload/getExcelPost',
					   data: JSON.stringify({"type":"project", "parameter":JSON.stringify(data)}),
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
		},
		downloadReport : function(obj){
			var prjId = $(obj).closest('tr').attr('id'); 
			
			$.ajax({
				type: "POST",
				url: '/exceldownload/getExcelPost',
				data: JSON.stringify({"type":"report", "parameter":prjId}),
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
		downloadNotice : function(obj){
			var prjId = $(obj).closest('tr').attr('id'); 
			
			//파일 이름 임시
			location.href = '<c:url value="/project/verification/downloadNotice?prjId='+prjId+'"/>';
		},
		downloadPackage : function(obj){
			var prjId = $(obj).closest('tr').attr('id');
			
			location.href = '<c:url value="/project/verification/downloadPackage?prjId='+prjId+'"/>';
		},
		downloadPackageMulti : function(obj, fileIdx){
			var prjId = $(obj).closest('tr').attr('id');
			
			location.href = '<c:url value="/project/verification/downloadPackageMulti?prjId='+prjId+'&fileIdx='+fileIdx+'"/>';
		},
		setGridParam : function(){
			var paramData=$('#projectSearch').serializeObject();
			
			//public 값 넣어주기
			if($('#checkbox3').is(':checked')) {
				paramData.publicYn = 'N';
			} else {
				paramData.publicYn = 'Y';
			}
			
			if(paramData.statuses != null) {
				paramData.statuses = JSON.stringify(paramData.statuses);
				paramData.statuses = paramData.statuses.replace(/\"|\[|\]/gi, "");
			} else {
				paramData.statuses = "";
			}
			
			return paramData;
		}, getUserName : function(cellvalue, options, rowObject){
			return userIdList[cellvalue] || pastEmpList[cellvalue] || "";
		}
	}
	
	// jqGrid
	var list = {
		load : function(){
			$("#list").jqGrid({
				url:"/project/listAjax",
				datatype: 'json',
				jsonReader:{
					repeatitems: false,
					id:'prjId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID','Project Name (Version)', 'Project<br/>Version', 'Status', 'Identification'
				           , 'Packaging', 'Distribution', 'Download', 'Distribution Type', 'CVE ID'
				           , 'Vulnera<br/>bility', 'Division', 'Creator', 'Created Date', 'Updated Date', 'Reviewer', 'Additional<br>Information', 'distributionTypeOfCodeDtlExp', 'statusRequestYn', 'priority'],
				colModel: [
					{name: 'prjId', index: 'prjId', width: 50, align: 'center', sorttype: 'int'},
					{name: 'prjName', index: 'prjName', width: 200, align: 'left'},
					{name: 'prjVersion', index: 'prjVersion', width: 50, align: 'left',hidden:true},
					{name: 'status', index: 'status', width: 50, align: 'center', formatter: fn.displayStatus, unformatter: fn.unformatter, cellattr: fn.cellattrStatus, sortable : true},
					{name: 'identificationStatus', index: 'identificationStatus', width: 200, align: 'left', formatter: fn.displayIdentification, unformatter: fn.unformatter, sortable : true, title:false},
					{name: 'verificationStatus', index: 'verificationStatus', width: 80, align: 'center', formatter: fn.displayVerification, unformatter: fn.unformatter, sortable : true, title:false},
					{name: 'destributionStatus', index: 'destributionStatus', width: 80, align: 'center', formatter: fn.displayDistribution, unformatter: fn.unformatter, sortable : true, title:false<c:if test="${!distributionFlag}">, hidden:true</c:if>},
					{name: 'download', index: 'download', width: 120, align: 'center', formatter:fn.displayReportDownload, unformatter:fn.unformatter, sortable : false, title:false},
					{name: 'distributionType', index: 'distributionType', width: 100, align: 'left', sortable : true},
					{name: 'cveId', index: 'cveId', hidden:true},
					{name: 'cvssScore', index: 'cvssScore', width: 50, align: 'center', formatter:fn.displayVulnerability, unformatter:fn.unformatter, sortable : false},
					{name: 'division', index: 'division', width: 70, align: 'left', sortable : true},
					{name: 'creator', index: 'creator', width: 70, align: 'left', sortable : true},
					{name: 'createdDate', index: 'createdDate', width: 80, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true},
					{name: 'modifiedDate', index: 'modifiedDate', width: 80, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true},
					{name: 'reviewer', index: 'reviewer', width: 80, align: 'left', editable:'${sessUserInfo.authority}'=="ROLE_ADMIN" ? true : false, edittype:'text', formatter:fn.getUserName
							, editoptions: {
								dataInit:
									function (e) {
										$(e).autocomplete({
											source: function(req, res){
												res(
													$.grep(Object.keys(userIdList)
															.map(function(e) {
																  return userIdList[e]
															}), function(cur){
													    return cur;
													})
												);
											}
											, minLength: 0
											, open: function() { $(this).attr('state', 'open');}
											, close: function () { $(this).attr('state', 'closed');}
										}).focus(function() {
											if ($(this).attr('state') != 'open') {
												$(this).autocomplete("search");
											}
										}).blur(function(){
											$(this).attr('state', 'closed');
										}).on('autocompletechange', fn.reviewerChg);
										
										currentOssName = e.value;
									}
							}
							, sortable : true
						},
					{name: 'comment', index: 'comment', width: 70, align: 'left', formatter:fn.displayComment, unformatter:fn.unformatter, sortable : true},
					{name: 'distributionTypeOfCodeDtlExp', index: 'distributionTypeOfCodeDtlExp', width: 50,hidden:true},
					{name: 'statusRequestYn', index: 'statusRequestYn', width: 50,hidden:true},
					{name: 'priority', index: 'priority', width: 50,hidden:true}
				],
				rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")},
				rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}],
				editurl:'clientArray',
	 			autowidth: true,
				pager: '#pager',
				gridview: true,
				sortable: function (permutation) {},
				sortname: 'prjId',
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto' ,
				multiselect : (('${sessUserInfo.authority}'=="ROLE_ADMIN") ? true: false),
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

					if(totalRow == 0){
						var startDate = $("#schStartDate").val()||0;
						var endDate = $("#schEndDate").val()||0;
						var diffNum = +startDate - +endDate;
						
						if(diffNum > 0 && endDate > 0){
							alertify.alert('<spring:message code="msg.common.search.check.date" />');
						}
					}
					
					for(var idx in arr){
						rowid = arr[idx];
						selectEl = $("#list #"+arr[idx]);
						status = data.rows[idx].identificationStatus;

	 					if(status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_PROGRESS'))}") {
	 						selectEl.find("td[aria-describedby=list_identificationStatus]").css('background-color','rgb(157, 165, 184)').css('color','rgb(255, 255, 255)');
						} else if(status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REQUEST'))}" 
								|| status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REVIEW'))}") {
							selectEl.find("td[aria-describedby=list_identificationStatus]").css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
						} else if(status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM'))}") {
							selectEl.find("td[aria-describedby=list_identificationStatus]").css('background-color','rgb(56, 79, 123)').css('color','rgb(255, 255, 255)');
						}
						
	 					if(data.rows[idx].identificationStatus == 'Confirm') {
	 						status = data.rows[idx].verificationStatus;
	 						if((data.rows[idx].status != "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}"
		 						|| data.rows[idx].status != "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP'))}" )
		 							&& data.rows[idx].statusRequestYn == "N" 
	 								&& status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_PROGRESS'))}") {
		 						selectEl.find("td[aria-describedby=list_verificationStatus]").css('background-color','rgb(157, 165, 184)').css('color','rgb(255, 255, 255)');
		 					} else if(status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REQUEST'))}" 
		 							|| status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REVIEW'))}") {
								selectEl.find("td[aria-describedby=list_verificationStatus]").css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
							} else if(status=="${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM'))}") {
								selectEl.find("td[aria-describedby=list_verificationStatus]").css('background-color','rgb(56, 79, 123)').css('color','rgb(255, 255, 255)');
							}	
	 					}
						
	 					if(data.rows[idx].identificationStatus == 'Confirm' && data.rows[idx].verificationStatus == 'Confirm') {
	 						status = data.rows[idx].destributionStatus;
		 					if(status=="${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_PROGRESS'))}" 
			 					&& data.rows[idx].distributeTarget != "NA") {
		 						selectEl.find("td[aria-describedby=list_destributionStatus]").css('background-color','rgb(157, 165, 184)').css('color','rgb(255, 255, 255)');
							} else if(status=="${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_RESERVE'))}") {
								selectEl.find("td[aria-describedby=list_destributionStatus]").css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
							} else if(status=="${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_DEPLOIDED'))}") {
								selectEl.find("td[aria-describedby=list_destributionStatus]").css('background-color','rgb(56, 79, 123)').css('color','rgb(255, 255, 255)');
							}	 						
	 					}
	 					
	 					//prjName prjVersion 데이터 join
	 					var prjNmVer = data.rows[idx].prjName;
	 					
	 					if(data.rows[idx].prjVersion != ""){
	 						prjNmVer += " (ver "+data.rows[idx].prjVersion+")";
	 					}
	 					
	 					$("#list").jqGrid("setCell",rowid,"prjName",prjNmVer,"");
					}
					
					// 툴팁 설정
					if(!gridTooltip.existTooltip){
						$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_status"))
							.attr("title", gridTooltip.tooltipCont).tooltip({
								content: function () {
									return $(this).prop('title');
								}
							});
						$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_download"))
							.attr("title", gridTooltip.tooltipCont1).tooltip({
								content: function () {
									return $(this).prop('title');
								}
							});
						
							gridTooltip.existTooltip = true;
						}
					
					$('input[id*="_releaseDate"]').attr('class', 'cal');
				},
				onCellSelect: function(rowid,iCol,cellcontent,e) {
					var role = '${sessUserInfo.authority}';
					
					if(role=="ROLE_ADMIN"){
						$("#list").jqGrid('saveRow',lastsel);
						$("#list").jqGrid('editRow',rowid);
						lastsel=rowid;
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					var rowData = $("#list").jqGrid('getRowData',rowid);
					var isAdmin = ${ct:isAdmin()};
					
					// identification의 status tex를 더블클릭시 bom으로 이동
					if(isAdmin && iCol == 5 || !isAdmin && iCol == 4) {
						fn.mvIdentification(rowid, "4");
					} else if(isAdmin && iCol == 6 || !isAdmin && iCol == 5) {
						if(rowData.verificationStatus == ''|| rowData.verificationStatus == 'N/A') {
							
						} else {
							fn.mvVerification(rowData['prjId']);
						}
					} else if(isAdmin && iCol == 7 || !isAdmin && iCol == 6) {
						if(rowData.destributionStatus == '' || rowData.destributionStatus == 'N/A') {
							
						} else {						
							fn.mvDistribution(rowData['prjId']);
						}
					} else if(iCol != 0 && iCol != 5 && iCol != 14) {
						createTabInFrame(rowData['prjId']+'_Project','#/project/edit/'+rowData['prjId']);
					}
				},
				postData : refreshParam
			});
		}
	};
	
//]]>
</script>