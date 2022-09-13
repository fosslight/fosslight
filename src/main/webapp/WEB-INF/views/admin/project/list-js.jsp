<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript" src="${ctxPath}/js/tutorial/tutorial-project.js?${jsVersion}"></script>

<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var userList;
	var userIdList;
	var adminUserList;
	var refreshParam = {};
	var totalRow = 0;
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		
		fn.getUserIdList("Y", "REVIEWER"); // 근무자
		fn.getUserIdList("N", "ADMIN_USER"); // 관리자(퇴사자 포함)

		// TODO - 추후 기능이 정리되면 주석제거할 예정
		//if('${sessUserInfo.authority}'=="ROLE_ADMIN"){
			//$('.btnReject').show();
		//}else{
			$('.btnReject').hide();
		//}
		
		showHelpLink("Project_List_Main");


		//ㅊㄱ

		//ㅊㄱ
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
	                   +"<dl><dt><span class=\"priority priority_1\"></span>Priority 0</dt></dl><br>"
	                   +"<dl><dt><span class=\"priority priority_2\"></span>Priority 1</dt></dl><br>"
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
	
	//OSS 쪽에서 프로젝트 검색을 위한 데이터
	var evt = {
		init : function(){
			$('select[name=distributionType]').val('${searchBean.distributionType}').trigger('change');
			$('select[name=prjDivision]').val('${searchBean.prjDivision}').trigger('change');
			$('select[name=networkServerType]').val('${searchBean.networkServerType}').trigger('change');
			$('select[name=priority]').val('${searchBean.priority}').trigger('change');
			
			refreshParam = fn.setGridParam();
			
			$('#search').on('click',function(e){
				e.preventDefault();
				var exPostdata = $("#list").jqGrid('getGridParam','postData');
				exPostdata.ossId = ''; 
				var postData = fn.setGridParam();
				
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
			
			$('#popCancel').click(function(){
				$('#changeStatusPop').hide();
			});

			$('#popChangeStatus').on('click', function(){
				fn.changeStatusProc();
			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	};
	
	
	var fn = {
		getUserIdList : function(reviewerFlag, type){
			return $.ajax({
				type: 'GET',
				url: '<c:url value="/project/getUserIdList"/>',
				data: {reviewerFlag : reviewerFlag},
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
							adminUserList = temp;
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
								display = "<div class=\"tcenter\"><a class='btnPG wauto' onclick=\"fn.mvVerification("+options.rowId+")\">Progress</a></div>";
							}
							
							break;
						case "N/A":
							display = "N/A";
							
							break;
						case "Error":
							display = "Error";

							break;
						case "Drop":
							display = "Drop";

							break;
						default:
							if(rowObject.distributeTarget == "N/A"){
								display = "N/A";
							} else{
								display = "<div class=\"tcenter\"><a class='btnPG wauto' onclick=\"fn.mvVerification("+options.rowId+")\">"+cellvalue+"</a></div>";
							}
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
						case "Error":
							display = cellvalue;

							break;
						case "N/A":
							display = cellvalue;

							break;
						default:
							if(rowObject.distributeTarget == "N/A"){
								display = "N/A";
							} else{
								display = "<div class=\"tcenter\"><a class='btnPG wauto' onclick=\"fn.mvDistribution("+options.rowId+")\">"+cellvalue+"</a></div>";
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
				display+="<input type=\"button\" value=\"Report\" class=\"downSet btnReport\" onclick=\"fn.downloadReport(this)\" title=\"FOSSLight Report\">";
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
			var _url = '<c:url value="/vulnerability/vulnpopup?ossName='+rowObject.ossName+'&ossVersion='+rowObject.ossVersion+'&vulnType="/>';
			
			if(parseInt(cellvalue) >= 9.0 ) {
				display="<span class=\"iconSet vulCritical\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 7.0 ) {
				display="<span class=\"iconSet vulHigh\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 4.0) {
				display="<span class=\"iconSet vulMiddle\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) > 0) {
				display="<span class=\"iconSet vulLow\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) == 0 || cellvalue == undefined) {
				display="<span style=\"font-size:0;\"></span>";
			} else {
				display=cellvalue;
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
			createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/'+initDiv+'"/>');
		},
		// Grid veritification display event
		mvVerification : function(prjId){
			createTabInFrame(prjId+'_Packaging', '#<c:url value="/project/verification/'+prjId+'"/>');
		},
		// Grid distribution display event
		mvDistribution : function(prjId){
			createTabInFrame(prjId+'_Distribute', '#<c:url value="/project/distribution/'+prjId+'"/>');
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
				url : '<c:url value="/project/updateReviewer"/>',
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
		downloadExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = fn.setGridParam();
				
				$.ajax({
					   type: "POST",
					   url: '<c:url value="/exceldownload/getExcelPost"/>',
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
				url: '<c:url value="/exceldownload/getExcelPost"/>',
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
			return adminUserList[cellvalue] || "";
		}, 
		checkProjectCnt : function(){
			var isValid = true;
			var checkProjectArr = $("#list").getGridParam("selarrrow");
			
			switch(checkProjectArr.length){
				case 1:
					break;
				case 0:
					isValid = false;
					alertify.alert('<spring:message code="msg.oss.select.project" />', function(){});
					break;
				default: // 2개 이상
					isValid = false;
					alertify.alert('<spring:message code="msg.project.select.only.project" />', function(){});
					break;
			}

			return isValid;
		},
		checkProjectStatus : function(){
			if(fn.checkProjectCnt()) {
				var prjId = $("#list").getGridParam("selrow");
				var rtnParameter = {};
				
				$.ajax({
					url : '<c:url value="/project/getProjectStatus"/>',
					type : 'POST',
					data : JSON.stringify({"prjId" : prjId}),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
					success: function(data){
						var distributionStatus = data.distributionStatus;
						
						if((distributionStatus||"").toUpperCase() == "PROC"){
							alertify.alert('<spring:message code="msg.project.distribution.loading" />', function(){});
							return false;
						}
						
						fn.showChangeStatus(data);
					},
					error: function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		copy : function(){
			if(fn.checkProjectCnt()){
				var prjId = $("#list").getGridParam("selrow");
				
				createTabInFrame(prjId+'copy_Project', '#<c:url value="/project/copy/'+prjId+'"/>');
			}
		},
		showChangeStatus : function(paramObj){
			var projectStatus = (paramObj.projectStatus||"").toUpperCase();
			var identificationStatus = (paramObj.identificationStatus||"").toUpperCase();
			var verificationStatus = (paramObj.verificationStatus||"").toUpperCase();
			var distributionStatus = (paramObj.distributionStatus||"").toUpperCase();
			var completeFlag = paramObj.completeFlag == "Y";
			var dropFlag = paramObj.dropFlag == "Y";
			var commId = paramObj.commId;
			var viewOnlyFlag = paramObj.viewOnlyFlag;

			$("#identificationStatus").val(identificationStatus);
			$("#distributionStatus").val(distributionStatus);
			$("#completeFlag").val(paramObj.completeFlag);
			$("#dropFlag").val(paramObj.dropFlag);
			$("#commId").val(commId);

			$("#CSIdentification > input[type=radio]").attr("disabled", false);
			$("#CSDrop > input[type=radio]").attr("disabled", false);
			$("#CSDelete > input[type=radio]").attr("disabled", false);
			$("#CSComplete > input[type=radio]").attr("disabled", false);
			
			if (projectStatus == "PROG"){
				if (identificationStatus == "" || identificationStatus == "PROG"){
					$("#CSIdentification > input[type=radio]").attr("disabled", true);
				}
			}
			
			if('${sessUserInfo.authority}'=="ROLE_ADMIN"
				&& identificationStatus == "CONF"
				&& (!verificationStatus
						|| verificationStatus == ""
						|| verificationStatus == "PROG"
						|| verificationStatus == "CONF"
						|| verificationStatus == "NA")
				&& !completeFlag){
				$("#CSComplete > input[type=radio]").attr("disabled", false);
			} else {
				$("#CSComplete > input[type=radio]").attr("disabled", true);
				$("#CSDelete > input[type=radio]").attr("disabled", true);
			}

			if(!dropFlag && !completeFlag){
				$("#CSDrop > input[type=radio]").attr("disabled", false);
			}else{
				$("#CSDrop > input[type=radio]").attr("disabled", true);
			}
			
			if(projectStatus == "REV"){
				$("#CSIdentification > input[type=radio]").attr("disabled", true);
				$("#CSDrop > input[type=radio]").attr("disabled", true);
				$("#CSDelete > input[type=radio]").attr("disabled", true);
			}
			
			$("#changeStatusPop").show();
		},
		changeStatusProc : function(){
			$("#reason").next(".retxt").hide(); // required reset
			
			var prjId = $("#list").getGridParam("selrow");
			var rowData = $("#list").getRowData(prjId);
			var changeSeq = $("input:radio[name='radioName']:checked").val();
			var reason = $("#reason").val();
			var identificationStatus = $("#identificationStatus").val();
			var distributionStatus = $("#distributionStatus").val();
			var completeFlag = $("#completeFlag").val();
			var dropFlag = $("#dropFlag").val();
			var commentFlag = true;

			if('${sessUserInfo.authority}'=="ROLE_ADMIN") {
				if(changeSeq == 1 || changeSeq == 4) {
					commentFlag = false;
				}
			}

			if(commentFlag && reason.split(" ").join("") == "") {
				alertify.alert('<spring:message code="msg.project.confirm.comment" />', function(){});
				$("#reason").next(".retxt").show();
				return false;
			}

			switch(changeSeq){
				case "1": // restart identification
					var param = {
						"prjId" : prjId
					  , "userComment" : reason
					  , "identificationStatus" : "PROG"
					};
					if ('${sessUserInfo.authority}'=="ROLE_ADMIN"){
						if(identificationStatus == "CONF"){
							if(distributionStatus == "DONE"){
								param["delOsdd"] = "Y"; // -> delete with OSDD
								
								if(completeFlag != "Y"){ // -> delete with OSDD & identification reject 후 end 
									param["changeStatusFlag"] = "Y";
								}
							}

							if(completeFlag == "Y"){
								param["completeYn"] = "N"; // -> reopen 동작
							}
						}

						if (dropFlag == "Y"){
							param["completeYn"] = "N"; // -> reopen 동작
						}
						
						fn.updateProjectStatus(param);
					}else{
						if(completeFlag == "Y"){
							param["commId"] = $("#commId").val();
							fn.reqToOpenUser(param);
						}else {
							if (dropFlag == "Y"){
								param["completeYn"] = "N"; // -> reopen 동작
							}
							
							fn.updateProjectStatus(param);
						}
					}
					
					break;
				case "2": // drop -> distribution Done 일경우 delete with OSDD를 실행 후 drop 처리함.
					if(distributionStatus == "DONE"){
						var param = {
							"prjId" : prjId
							, "identificationStatus" : "PROG"
							, "userComment" : reason
						};
							
						param["delOsdd"] = "Y"; // -> delete with OSDD

						var dropMessage = '<spring:message code="msg.project.warn.drop.rsv" />';

						alertify.confirm(dropMessage, function (e) {
							if (e) {
								fn.updateProjectStatus(param, fn.exeProjectDrop);
							} else {
								return false;
							}
						});
					}else if (distributionStatus == "RSV"){
						var dropMessage = '<spring:message code="msg.project.warn.drop.rsv" />';

						alertify.confirm(dropMessage, function (e) {
							if (e) {
								fn.cancelDistributeReserve();
							} else {
								return false;
							}
						});
					}else{
						var param = {
							"prjId" : prjId
							, "userComment" : reason
						};
						
						fn.exeProjectDrop(param);
					}

					break;
				case "3": // delete
					var param = {
						"prjId" : prjId
					  , "userComment" : reason
					  , "identificationStatus" : "PROG"
					};

					if(identificationStatus == "CONF"){ // identification > confirm 일 경우 reject 후 delete
						if(distributionStatus == "DONE"){
							param["delOsdd"] = "Y"; // -> delete with OSDD
							
							if(completeFlag != "Y"){
								param["changeStatusFlag"] = "Y"; // -> delete with OSDD & identification reject 후 end 
							}
						} 

						if(completeFlag == "Y"){
							param["completeYn"] = "N"; // -> reopen 동작
						}
						
						fn.updateProjectStatus(param, fn.projectDelete);
					} else { // identification > !confirm 일 경우 즉시 delete
						fn.projectDelete({"prjId" : prjId, "userComment" : reason});
					}
					
					break;
				case "4": // complete
					fn.updateProjectStatus({"prjId" : prjId, "completeYn" : "Y", "userComment" : reason});
					
					break;
				default:
					break;
			}
		},
		projectDelete : function(data){
			var _url = '/project/delAjax?prjId=' + data.prjId + '&userComment=' + data.userComment;
			
			$.ajax({
				url : '<c:url value="'+_url+'"/>',
				type : 'POST',
				data : '',
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					reloadTabInframe('<c:url value="/project/list"/>');
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		updateProjectStatus : function(data, callbackFunc){
			$.ajax({
				url : '<c:url value="/project/updateProjectStatus"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(){
					if(typeof callbackFunc == "function"){
						callbackFunc(data);
					} else {
						reloadTabInframe('<c:url value="/project/list"/>');
					}
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		exeProjectDrop : function (data){
			data["dropYn"] = "Y";
			
			$.ajax({
				url : '<c:url value="/project/updateProjectStatus"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					reloadTabInframe('<c:url value="/project/list"/>');
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}, bomCompare : function(){ /* BOM Compare ADD */
			// 체크박스 선택 체크
			var chk = $("#list").jqGrid("getGridParam", "selarrrow").length;
		
			if(chk < 3){
				var bomCompareArr = $("#list").jqGrid("getGridParam", "selarrrow");
				
				var beforePrjId = "";
				var afterPrjId = "";

				switch (chk) {
					case 0:
						beforePrjId = "0000";
						afterPrjId = "0000";
						break;
					case 1:
						beforePrjId = bomCompareArr[0];
						afterPrjId = "0000";
						break;
					case 2:
						bomCompareArr.sort();
						
						beforePrjId = bomCompareArr[0];
						afterPrjId = bomCompareArr[1];
						
						break;
				}
				
				var tabNm = "BOM_Compare";
				var tabLk = '#<c:url value="/project/bomCompare/'+beforePrjId+'/'+afterPrjId+'"/>';
		
				createTabInFrame(tabNm, tabLk);
			}else {
				alertify.alert('<spring:message code="msg.project.choose" />', function(){});
				return false;
			}
		}, reqToOpenUser : function(data) { /* role_user 실행 시 */
			var commentsMode = data.commId != "" ? "update" : "insert";
			var param = {"referenceId" : data.prjId, "contents" : data.userComment, "referenceDiv" : "10", "commId" : data.commId, "commentsMode" : commentsMode};
			
			fn.commentsSave(param);
		}, commentsSave : function(data){
			$.ajax({
				url : '<c:url value="/project/commentsSave"/>',
				type : 'POST',
				dataType : 'json',
				cache : false,
				data : data,
				success : function(json){
					if(json.isValid == 'false'){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						alertify.alert('<spring:message code="msg.common.success" />', function(){
							reloadTabInframe('<c:url value="/project/list"/>');
							activeTabInFrameList("PROJECT");
						});
					}
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}, changeDivision : function(){
			var chk = $("#list").jqGrid("getGridParam", "selarrrow").length;

			if(chk > 0){
				$("#changeDivisionSelect").find("strong").text($("#changeDivisionPop select[name=division] option:first").text());
				$("#changeDivisionPop").show();
			} else {
				alertify.alert('<spring:message code="msg.project.watcher.selectlist" />', function(){});
			}
		}, changeDivisionSave : function(){
			var changeDivisionArr = $("#list").jqGrid("getGridParam", "selarrrow");
			var division = $("#changeDivisionPop select[name=division]").val();

			alertify.confirm('<spring:message code="msg.common.change.division" />', function (e) {
				if (e) {
					$('#changeDivisionPop').hide();
					
					$.ajax({
						type: 'POST',
						url :'<c:url value="/project/updateProjectDivision"/>',
						data: JSON.stringify({'prjIds':changeDivisionArr, 'prjDivision':division}),
						contentType : 'application/json',
						success: function (data) {
							if("true" == data.isValid){
								alertify.alert('<spring:message code="msg.common.success" />', function(){
									reloadTabInframe('<c:url value="/project/list"/>');
									activeTabInFrameList("PROJECT");
								});
							} else {
								var list = [];
								list = data.resultData;
								
								var msg = '<spring:message code="msg.project.check.division.permissions" />';
								msg += '<br/> - '

								for(var i=0; i<list.length; i++){
									msg += 'PRJ-' + list[i];
									if(i < list.length - 1){
										msg += ', ';
									}
								}
								
								alertify.alert(msg, function(){});
							}
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
				} else {
					return false;
				}
			});
		}, changeDivisionCancel : function(){
			$("#changeDivisionPop select[name=division] option").remove();
			$('#changeDivisionPop').hide();
		}, toolTipDoubleclick: function () {
                    return 'title="' + "Double click" + '"';
            }
	};
	
	// jqGrid
	var list = {
		load : function(){
			$("#list").jqGrid({
				url:'<c:url value="/project/listAjax"/>',
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
					{name: 'prjName', index: 'prjName', width: 200, align: 'left',cellattr:fn.toolTipDoubleclick},
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
				multiselect : true,
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
							alertify.alert('<spring:message code="msg.common.search.check.date" />', function(){});
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
						createTabInFrame(rowData['prjId']+'_Project', '#<c:url value="/project/edit/'+rowData['prjId']+'"/>');
					}
				},
				postData: refreshParam
			});
		}
	};
	
//]]>
</script>
