<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
var bomValidMsgData;
var bomDiffMsgData;
var bomInfoMsgData;
var groupBuffer='';
var _popupOssAutoAnalysis = null;
var ossAnalysisStatus = "${project.ossAnalysisStatus}";
var analysisStartDate = "${project.analysisStartDate}";
var bom_evt = {
	commentIdx : '${project.commentIdx}',
	commentTemp : '',
	init : function(){
		if(curIdenStatus == "REV" && $("#binaryDB")){
			$("#binaryDB").show();
		}
		
		bom_evt.commentTemp = $('<div>').append($('dl[name=commentClone]').clone());
		$('dl[name=commentClone]').remove();
		
		bom_data.getJqGrid();
		
		// bomReset 버튼 
		$('#bomResetUp').click(function(e){
			e.preventDefault();

 			alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
				if (e) {
					$("#bomList").jqGrid('clearGridData');
				} else {
					return false;
				}
			});
		});
		
		// bomMerge 버튼 
		$('#bomSaveUp').click(function(e){
			if (com_fn.checkStatus()){
				e.preventDefault();
				
	 			alertify.confirm('<spring:message code="msg.common.confirm.mergeAndSave" />', function (e) {
					if (e) {
						$("#mergeYn").val("Y");
						Bom_Save_Flg = false;
						
						bom_fn.save();
					} else {
						return false;
					}
				});
			}else {
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}
		});
		
		$(window).resize(function(){
			bom_fn.bomListHeaderResize();
		});

		if(ossAnalysisStatus.toUpperCase() == "SUCCESS" && curIdenStatus != "CONF"){
			$(".idenAnalysisResult").show();
		}		
	}
}

var bom_fn = {
	save : function(){
		cleanErrMsg("bomList");
		
		var bomList = $("#bomList");
		var str = "";
		arr = bomList.jqGrid('getDataIDs');
		var gridData = new Array();
		
 		for(var i in arr){
 			// mergePreDiv 설정
 			str = bomList.jqGrid('getCell',arr[i],'referenceDiv').split("/");
 			
 			if(str[0] =="3rd") {
 				bomList.jqGrid('setCell',arr[i],'referenceDiv', "10");

 				if(bomList.jqGrid('getCell',arr[i],'mergePreDiv')=="99") {
 	 				bomList.jqGrid('setCell',arr[i],'mergePreDiv', "10");
 				}
 			} else if(str[0] =="SRC") {
 				bomList.jqGrid('setCell',arr[i],'referenceDiv', "11");

 				if(bomList.jqGrid('getCell',arr[i],'mergePreDiv')=="99")  {
 	 				bomList.jqGrid('setCell',arr[i],'mergePreDiv', "11");
 				}
 			} else if(str[0] =="BIN") {
 				bomList.jqGrid('setCell',arr[i],'referenceDiv', "15");

 				if(bomList.jqGrid('getCell',arr[i],'mergePreDiv')=="99")  {
 	 				bomList.jqGrid('setCell',arr[i],'mergePreDiv', "15");
 				}
 			}
 			
 			// obligationType 설정
 			if(bomList.jqGrid('getCell',arr[i],'notify')=="Y") {
 				if(bomList.jqGrid('getCell',arr[i],'source')=="Y") {
 					bomList.jqGrid('setCell',arr[i],'obligationType', "11");
 				} else {
 					bomList.jqGrid('setCell',arr[i],'obligationType', "10");
 				}
 			} else {
 				bomList.jqGrid('setCell',arr[i],'obligationType', "");
 			}
 			
 			if(bomList.jqGrid('getCell',arr[i],'adminCheck') == "Y") {
 				bomList.jqGrid('setCell',arr[i],'adminCheckYn', 'Y');
 				
		 		gridData.push(bomList.getRowData(arr[i]));
			}
 		}
 		
 		gridData.reverse();
 		
		var finalData = {
			referenceId : '${project.prjId}',
			merge : 'Y',
			gridData : JSON.stringify(gridData)
		};
		
		$.ajax({
			url : '<c:url value="/project/saveBom"/>',
			type : 'POST',
			data : JSON.stringify(finalData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				curIdenStatus = data.identificationStatus;
				
				var param = {referenceId : '${project.prjId}', merge : 'N'};
				bom_data.getJqGrid(param);
				
				Bom_Save_Flg = true;
				
				alertify.success('<spring:message code="msg.common.success" />');
				
				var checkObligationFlag = false;
				var arr = [];
				arr = bomList.jqGrid('getRowData');
		 		
		 		for(var i in arr){
		 			if(arr[i].obligationType == "90") {
		 				checkObligationFlag = true;

		 				break;
		 			}
		 		}
		 		
				if(checkObligationFlag) {
					alertify.alert('<spring:message code="msg.info.include.needcheck.license.guide" />', function(){});
				}
				
				// identification 상태가 초기 값인 경우, bom save시 progress 상태로 변경하기 때문에,
				// request review 버튼만 활성화 시킨다.
				if('${project.identificationStatus}' == "" && $(".projdecBtn").css("display") == "none" ) {
					$(".confirm").hide();
					$(".reject").hide();
					$(".restart").hide();
					$(".review").show();
					$(".projdecBtn").show();
				}
			},
			error: function(data){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	downloadExcel : function(){
		$.ajax({
			type: "POST",
			url: '<c:url value="/exceldownload/getExcelPost"/>',
			data: JSON.stringify({"type":"bom", "parameter":'${project.prjId}'}),
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
	displayNotify : function(cellvalue, options, rowObject){
		var display = "";
		var obligationLicense = rowObject["obligationLicense"] || rowObject[20];
		var preObligationType = rowObject["preObligationType"] || rowObject[21];
		var obligationType = rowObject["obligationType"] || rowObject[22];
		var rowId = options.rowId;
		var adminCheck = $("#"+rowId+"_adminCheck").prop("checked");
		
		if(adminCheck == undefined){
			adminCheck = rowObject.adminCheckYn == "Y";
		}
		
		if(adminCheck == false) {
			if(preObligationType == 10 || preObligationType == 11){
				display="<span class=\"iconSet ops\"></span>";
			}

			// admin check이전에 complete나 identification confirm된 대상 중 obligation의 값이 있을면 여기를 탐.
			if(preObligationType == undefined && (obligationType == 10 || obligationType == 11)){
				display="<span class=\"iconSet ops\"></span>";
			}
		} else if(obligationLicense == 90 || adminCheck == true) {
			display = '<select id="'+rowId+'_notify" onchange="bom_fn.onNotifyCboxClick(' + rowId + ')">'
				+ '<option value="Y" '+((obligationType=='10' || obligationType=='11') ? ' selected="selected"' : '') +'>O</option>'
				+ '<option value="N" '+((obligationType!='10' && obligationType!='11') ? ' selected="selected"' : '') +'>X</option>'
				+ '</select>';
		} else if(obligationLicense == 10 || obligationLicense == 11) {
			display="<span class=\"iconSet ops\"></span>";
		}
		
		return display;
	},
	displaySource : function(cellvalue, options, rowObject){
		var display = "";
		var obligationLicense = rowObject["obligationLicense"] || rowObject[20];
		var preObligationType = rowObject["preObligationType"] || rowObject[21];
		var obligationType = rowObject["obligationType"] || rowObject[22];
		var rowId = options.rowId;
		var adminCheck = $("#"+rowId+"_adminCheck").prop("checked");
		
		if(adminCheck == undefined){
			adminCheck = rowObject.adminCheckYn == "Y";
		}
		
		if(adminCheck == false) {
			if(preObligationType == 11) {
				display="<span class=\"iconSet man\"></span>";
			}

			// admin check이전에 complete나 identification confirm된 대상 중 obligation의 값이 있을면 여기를 탐.
			if(preObligationType == undefined && obligationType == 11){
				display="<span class=\"iconSet man\"></span>";
			}
		} else if(obligationLicense == 90 || adminCheck == true) {
			display = '<select id="'+rowId+'_source" onchange="bom_fn.onSourceCboxClick(' + rowId + ')">'
				+ '<option value="Y" '+((obligationType=='11') ? ' selected="selected"' : '') +'>O</option>'
				+ '<option value="N" '+((obligationType!='11') ? ' selected="selected"' : '') +'>X</option>'
				+ '</select>';
		} else if(obligationLicense == 11) {
			display="<span class=\"iconSet man\"></span>";
		}
		
		return display;
	},
	displayAdminCheck : function(cellvalue, options, rowObject){
		var adminCheckYn = rowObject["adminCheckYn"];
		var display = '<input id="'+options.rowId+'_adminCheck" type="checkbox" value="'+adminCheckYn+'"';

		if(adminCheckYn == "Y"){
			display += 'checked '
		}

		if(userRole != "ROLE_ADMIN" || curIdenStatus == "CONF"){
			display += 'disabled ';
		}
		
		display += 'onclick="bom_fn.onAdminCheckClick('+options.rowId+')">';
		
		return display;
	},
	unDisplayNotify : function(cellvalue, options, rowObject){
		var display = $("#"+options.rowId+"_notify").val();
		
		return display;
	},
	unDisplaySource : function(cellvalue, options, rowObject){
		var display = $("#"+options.rowId+"_source").val();

		return display;
	},
	unDisplayAdminCheck : function(cellvalue, options, rowObject){
		var display = $("#"+options.rowId+"_adminCheck").val();

		return display;
	},
	onNotifyCboxClick : function(rowId){
		var id = (typeof(rowId) == 'object') ? rowId.id : rowId;
		var selectedVal = $("#"+id+"_notify").val();
		
		if(selectedVal) {
			if(selectedVal == "N") {
				$("#"+id+"_source").val("N");
			}
			
			$("#"+id+"_notify").parent().css("background", "");
			
			if($("#"+id+"_source").val()) {
				$("#"+id+"_source").parent().css("background", "");
			}
		} else {
			$("#"+id+"_source").val("");
			$("#"+id+"_notify").parent().css("background", "CornflowerBlue");
			$("#"+id+"_source").parent().css("background", "CornflowerBlue");
		}
	},
	onSourceCboxClick : function(rowId){
		var id = (typeof(rowId) == 'object') ? rowId.id : rowId;
		var selectedVal = $("#"+id+"_source").val();
		
		if(selectedVal) {
			if(selectedVal == "Y") {
				$("#"+id+"_notify").val("Y");
			}
			
			$("#"+id+"_source").parent().css("background", "");
			
			if($("#"+id+"_notify").val()) {
				$("#"+id+"_notify").parent().css("background", "");
			}
		} else {
			$("#"+id+"_notify").val("");
			$("#"+id+"_notify").parent().css("background", "CornflowerBlue");
			$("#"+id+"_source").parent().css("background", "CornflowerBlue");
		}
	},
	onAdminCheckClick : function(rowId){
		var id = (typeof(rowId) == 'object') ? rowId.id : rowId;
		var adminCheckYn = $("#"+id+"_adminCheck").prop("checked");
		
		$("#bomList").jqGrid('setCell', rowId, 'notify', "10");
		$("#bomList").jqGrid('setCell', rowId, 'source', "10");

		if(adminCheckYn) {
			$("#"+id+"_adminCheck").val("Y");
		} else {
			$("#"+id+"_adminCheck").val("N");
		}
	},
	bomListHeaderResize : function(rowId){
		// 그리드 그룹 헤더 복원
		$("#bomList").jqGrid('destroyGroupHeader', false);
		// 그리드 그룹 헤더 설정
		$("#bomList").jqGrid('setGroupHeaders', {
			useColSpanStyle: true, 
			groupHeaders:[
				{startColumnName: 'notify', numberOfColumns: 2, titleText: '<label style="font-weight: bold;">Obligation</label>'},
			]
		});
	},
	adminCheck : function(rows, validArr, target){
		var result = false;
		var success = true;
		
		for(var idx in rows){
			var refComponentIds = rows[idx].refComponentId.split(",");
			
			for(var refIdx in refComponentIds) {
				if(refComponentIds[refIdx] == validArr[1]) {
					var msgData = Object.keys(bomValidMsgData).filter(function(cur){
					    return cur.indexOf(rows[0].componentId) > -1;
					});

					for(var i in msgData){
					    if(bomValidMsgData[msgData[i]].toUpperCase() == "UNCONFIRMED LICENSE"){
					        success = false;

					        break;
					    }
					}

					if(success){
						result = true;

						break;
					}
				}
			}
		}

		if(!result) {
			var checkData = target.getRowData(validArr[1]);

			if(Object.keys(checkData).length > 0){
				var key = (checkData.ossName + "|" + checkData.ossVersion).replace(/(<([^>]+)>)/ig,"");

				var adminCheckCnt = rows
										.filter(function(cur){
													return (cur.ossName+"|"+cur.ossVersion).replace(/(<([^>]+)>)/ig,"") == key; 
												})
										.length;
				
				if(adminCheckCnt > 0){
					result = true;
				}
			}
		}
		
		return result;
	},
	analysisValidation : function(){
		if (com_fn.checkStatus()){
			if("Y"!= $("#mergeYn").val()){
				alertify.alert('<spring:message code="msg.project.required.merge" />', function(){});

				return false;
			}
			
			var alertMsg = "";
			
			switch(ossAnalysisStatus.toUpperCase()){
				case "PROGRESS":
					if(analysisStartDate != "") {
						alertMsg = "This analysis has been started at " + analysisStartDate + "<br>It has not been completed yet";
						alertify.alert(alertMsg, function(){});
					} else {
						bom_fn.showOssAutoAnalysis(ossAnalysisStatus);
					}
					
					break;
				case "SUCCESS":
					if(!alertify.commentDialog) {
						//define a new dialog
						alertify.dialog('commentDialog',function factory(){
							return {
								main:function(message){
									this.message = message;
								},
								setup:function(){
									return { 
										focus: { element:0 }
									};
								},
								prepare:function(){
										this.setContent(this.message);
								}
							}
						});
					}
					
					// status가 success일때 다시 자동분석을 할때? -> 기존 list를 활용할 것인지? or 기존 list는 제거 이후 새로 list를 만들 것인지?
					alertMsg = '<spring:message code="msg.project.confirm.bom" />';

					var btnHtml = '<br><b>'+alertMsg+'</b><br><br>';
					btnHtml += '<input type="button" value="Reset & Load" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="bom_fn.showOssAutoAnalysis(\''+ossAnalysisStatus+'-reset\')"/>&nbsp;&nbsp;&nbsp;';
					btnHtml +='<input type="button" value="Cancel" class="btnCancel btnColor" style="height:30px;width:120px;" onclick="$(\'.ajs-close\').trigger(\'click\')"/>';
					
					alertify.commentDialog(btnHtml);
					
					break;
				case "FAIL":
					if(!alertify.commentDialog){
						//define a new dialog
						alertify.dialog('commentDialog',function factory(){
							return {
								main:function(message){
									this.message = message;
								},
								setup:function(){
									return { 
										focus: { element:0 }
									};
								},
								prepare:function(){
										this.setContent(this.message);
								}
							}
						});
					}
					
					// status가 success일때 다시 자동분석을 할때? -> 기존 list를 활용할 것인지? or 기존 list는 제거 이후 새로 list를 만들 것인지?
					alertMsg = 'The Auto analysis of this project was recently failed.<br>If you want to restart the Auto Analysis. click the \'Restart\' button';
					var btnHtml = '<br><b>'+alertMsg+'</b><br><br>';
					btnHtml += '<input type="button" value="Restart" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="bom_fn.showOssAutoAnalysis(\''+ossAnalysisStatus+'\')"/>&nbsp;&nbsp;&nbsp;';
					btnHtml +='<input type="button" value="Cancel" class="btnCancel btnColor" style="height:30px;width:120px;" onclick="$(\'.ajs-close\').trigger(\'click\')"/>';
					
					alertify.commentDialog(btnHtml);
					
					break;
				default:
					bom_fn.showOssAutoAnalysis("NEW");
				
					break;
			}	
		}else {
			alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
		}	
	},
	showOssAutoAnalysis : function(status){
		if(status.toUpperCase().indexOf("SUCCESS") > -1 || status.toUpperCase().indexOf("FAIL") > -1){
			$('.ajs-close').trigger('click');
		}

		if(status.toUpperCase().indexOf("LOAD") > -1){ // status : success & load
			_popupOssAutoAnalysis = window.open('<c:url value="/oss/ossAutoAnalysis?prjId=${project.prjId}"/>', 'OSS Auto Analysis', 'width=1550, height=814, toolbar=no, location=no, resizable=yes, scrollbars=yes');

			if(!_popupOssAutoAnalysis || _popupOssAutoAnalysis.closed || typeof _popupOssAutoAnalysis.closed=='undefined') {
				alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
			}
		}

		// status : (success & reset), progress 
		if(status.toUpperCase().indexOf("RESET") > -1 
				|| status.toUpperCase().indexOf("PROGRESS") > -1 
				|| status.toUpperCase().indexOf("FAIL") > -1
				|| status.toUpperCase().indexOf("NEW") > -1) {
			// unconfirmed OssName & OssVersion의 Data추출
			var componentIdList = Object.keys(bomValidMsgData).reduce(function(arr, cur){
			    if(arr.indexOf(cur) == -1 && (cur.indexOf("ossName") == 0 || cur.indexOf("ossVersion") == 0)){
			        arr.push(cur);
			    }
	
			    return arr;
			}, []).reduce(function(arr, cur){
			    var componentId = cur.split(".")[1];

				if(arr.indexOf(componentId) == -1 && bomValidMsgData[cur].indexOf("Unconfirmed") > -1){
			        arr.push(componentId);
			    }
	
			    return arr;
			}, []);
	
			var data = {
					'componentIdList' : componentIdList
				  , 'prjId' : '${project.prjId}'
				  , 'referenceDiv' : '13'
			};
			
			$.ajax({
				url : '<c:url value="/oss/saveOssAnalysisList/view"/>',
				dataType : 'json',
				type : 'POST',
				cache : false,
				data : JSON.stringify(data),
				contentType : 'application/json',
				success : function(data){
					_popupOssAutoAnalysis = window.open('<c:url value="/oss/ossAutoAnalysis?prjId=${project.prjId}"/>', 'OSS Auto Analysis', 'width=1550, height=814, toolbar=no, location=no, resizable=yes, scrollbars=yes');

					if(!_popupOssAutoAnalysis || _popupOssAutoAnalysis.closed || typeof _popupOssAutoAnalysis.closed=='undefined') {
						alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
					}
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	},
	showAnalysisResult : function(){
		if (com_fn.checkStatus()){
			_popupOssAutoAnalysis = window.open('<c:url value="/oss/ossAutoAnalysis?prjId=${project.prjId}&ossAnalysisStatus=result"/>', 'OSS Auto Analysis', 'width=1550, height=814, toolbar=no, location=no, resizable=yes, scrollbars=yes');

			if(!_popupOssAutoAnalysis || _popupOssAutoAnalysis.closed || typeof _popupOssAutoAnalysis.closed=='undefined') {
				alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
			}
		}else {
			alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
		}
	}
}

var bom_data = {
		getJqGrid : function(param){
			var data = param || {referenceId : '${project.prjId}', merge : 'N'};
				
			$.ajax({
				url : '<c:url value="${suffixUrl}/project/identificationGrid/${project.prjId}/13"/>',
				dataType : 'json',
				type : 'GET',
				cache : false,
				data : data,
				contentType : 'application/json',
				success : function(data){
					$('.ajs-close').trigger("click");
					
					bomMainData = data.rows;
					bomValidMsgData = []; //초기화
					bomDiffMsgData = []; //초기화
					bomInfoMsgData = []; //초기화
					
					if(data.validData) {
						bomValidMsgData = data.validData;
					}
					
					if(data.diffData) {
						bomDiffMsgData = data.diffData;
					}
					
					if(data.infoData) {
						bomInfoMsgData = data.infoData;
					}
					
					// 리로드 대신 그리드 삭제 후 다시 그리기
					$("#bomList").jqGrid('GridUnload');
					
					bom_data.loadBomGrid();
					
					fn_grid_com.addEtcKeyDownEvent($('#bomList'), bomValidMsgData, bomDiffMsgData, bomInfoMsgData, com_fn.getLicenseName);
					
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		loadBomGrid : function(){
			var bomList = $('#bomList');
			bomList.jqGrid({
				  datatype: 'local'
				, data : bomMainData
				, colNames: ['','ID','ID_KEY','groupingColumn','refComponentId','refComponentIdx','refDiv','ReferenceId','MergePreDiv','ReferenceDiv','OSS ID','OSS Name','OSS Version','License'
		             ,'Download Location','Homepage','LicenseId','Copyright Text'
		             ,'CVE ID' ,'Vulnera<br/>bility','obligationLicense','ObligationType','preObligationType','Notify','Source','Restriction','licenseTypeIdx','adminCheckYn','admin<br>check']
				, colModel : [
					{name: 'group', width: 20, align: 'center', search : false, sorttype: 'int',
					    cellattr: function(rowId, tv, rawObject, cm, rdata) {
					        return ' colspan=2';
					    }
					},
					{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', search : false, sorttype: 'int',
					    cellattr: function(rowId, tv, rawObject, cm, rdata) {
					        return ' style="display:none;"';
					    }
					},
					{name: 'componentId', index: 'componentId', width: 40, align: 'center', hidden:true, key:true},
					{name: 'groupingColumn', width: 20, align: 'center', hidden:true},
					{name: 'refComponentId', width: 20, align: 'center', hidden:true},
					{name: 'refComponentIdx', width: 20, align: 'center', hidden:true},
					{name: 'refDiv', width: 20, align: 'center', hidden:true},
					{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
					{name: 'mergePreDiv', index: 'mergePreDiv', width: 29, align: 'center', hidden:true},
					{name: 'referenceDiv', index: 'referenceDiv', width: 50, align: 'center', hidden:false, search : false},
					{name: 'ossId', index: 'ossId', width: 29, align: 'center', hidden:true},
					{name: 'ossName', index: 'ossName', width: 100, align: 'left', template: searchStringOptions},
					{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left', template: searchStringOptions},
					{name: 'licenseName', index: 'licenseName', width: 100, align: 'left', template: searchStringOptions},
					{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions},
					{name: 'homepage', index: 'homepage', width: 70, align: 'left', formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions},
					{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', hidden:true},
					{name: 'copyrightText', index: 'copyrightText', width: 170, align: 'left', template: searchStringOptions},
					{name: 'cveId', index: 'cveId', hidden:true},
					{name: 'cvssScore', index: 'cvssScore', width: 80, align: 'center', sorttype:'float', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, template: searchNumberOptions},
					{name: 'obligationLicense', index: 'obligationLicense', width: 40, align: 'center', hidden:true},
					{name: 'preObligationType', index: 'preObligationType', width: 40, align: 'center', hidden:true},
					{name: 'obligationType', index: 'obligation', width: 40, align: 'center', hidden:true},
					{name: 'notify', index: 'notify', width: 40, align: 'center', formatter: bom_fn.displayNotify, unformat: bom_fn.unDisplayNotify, sortable : false, search : false},
					{name: 'source', index: 'source', width: 40, align: 'center', formatter: bom_fn.displaySource, unformat: bom_fn.unDisplaySource, sortable : false, search : false},
					{name: 'restriction', index: 'restriction', width: 60, align: 'center', formatter: fn_grid_com.displayLicenseRestriction, unformat: fn_grid_com.unformatter, sortable : false, search : false},
					{name: 'licenseTypeIdx', index: 'licenseTypeIdx', width: 60, align: 'center', hidden:true},
					{name: 'adminCheckYn', index: 'adminCheckYn', width: 50, align: 'center', hidden:true},
					{name: 'adminCheck', index: 'adminCheck', width: 50, align: 'center', formatter: bom_fn.displayAdminCheck, unformat: bom_fn.unDisplayAdminCheck, sortable:false, search:false}
				]
				, rowNum: 200
				, rowList: [200, 500, 1000, 5000]
				, autoencode: true
				, editurl:'clientArray'
	 			, autowidth: true
	 			, height: '500px'
				, gridview: true
			   	, pager: '#bomPager'
				, recordpos:'right'
				, loadonce:false
				, ignoreCase: true
			    , onSortCol: function (index, columnIndex, sortOrder) {
			    	isSort = true;
			    }
				, loadComplete: function(data) {
					$("#bomList_source").css({'border-right': '1px solid #cbc7bd'}); // restriction 을 추가하면서 css에서 마지막 th의 border를 삭제하는 처리의 차선책
					var str= "";
			 		var arr = [];
			 		arr = bomList.jqGrid('getDataIDs');

					for(var i in arr){
						
						if(bomList.jqGrid('getCell',arr[i],'refComponentId')==""){ // MERGE 상태
							bomList.jqGrid('setCell',arr[i],'group',bomList.jqGrid('getCell',arr[i],'componentIdx'));
							bomList.jqGrid('setCell',arr[i],'refComponentId',bomList.jqGrid('getCell',arr[i],'componentId'));
						} else { // BOM 상태
							bomList.jqGrid('setCell',arr[i],'group',bomList.jqGrid('getCell',arr[i],'refComponentIdx'));
							bomList.jqGrid('setCell',arr[i],'referenceDiv',bomList.jqGrid('getCell',arr[i],'refDiv'));
						}
						
						// 하위색 설정
						if(i!=0
						   && bomList.jqGrid('getCell',arr[i],'ossName') == bomList.jqGrid('getCell',arr[i-1],'ossName')
						   && bomList.jqGrid('getCell',arr[i],'ossVersion') == bomList.jqGrid('getCell',arr[i-1],'ossVersion')){
						   bomList.jqGrid('setCell',arr[i],'mergePreDiv', "99"); // 하위 referenceDiv 구분 임의 99 설정
						}
						
						// 레퍼런스 DIV 설정
						str = bomList.jqGrid('getCell',arr[i],'referenceDiv');
						var referenceDivList = str.split(",").sort();
						var referenceName = "";
						
						for(var idx in referenceDivList) {
							if(referenceName != "") {
								referenceName += ",";
							}
							
							if(referenceDivList[idx] =="10") {
								referenceName += "3rd";
							} else if(referenceDivList[idx] =="11") {
								referenceName += "SRC";
							} else if(referenceDivList[idx] =="15") {
								referenceName += "BIN";
							} else if(referenceDivList[idx] =="13") {
								referenceName += "BOM";
							}
						}
						
						bomList.jqGrid('setCell',arr[i],'referenceDiv', referenceName);
						
						if(bomList.jqGrid('getCell',arr[i],'obligationType') == 90) {
							$("#"+arr[i]+"_source").parent().css("background", "CornflowerBlue");
							$("#"+arr[i]+"_notify").parent().css("background", "CornflowerBlue");
						}
					}
					
					// 그리드 클릭 해제
					bomList.jqGrid("resetSelection");
					
					//------- 로드 된후 그룹 헤더 설정을 해야 사이즈가 깨지지 않음
					bom_fn.bomListHeaderResize();

					if(bomValidMsgData) {
						gridValidMsgNew(bomValidMsgData, "bomList");
					}
					
					if(bomDiffMsgData) {
						gridDiffMsg(bomDiffMsgData, "bomList");
					}

					// totla record 표시
					$("#bomList_toppager_right, #bomPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+bomMainData.length+'</div>');
					
					var datas = data.rows, rows=this.rows, row, className, rowsCount=rows.length,rowIdx=0;
					var strBomValidMsgData = Object.keys(bomValidMsgData||{}).join("");
					var strBomDiffMsgData = Object.keys(bomDiffMsgData||{}).join("");
					
					for(var _idx=0;_idx<rowsCount;_idx++) {
						row = rows[_idx];
						className = row.className;
						
						if (className.indexOf('jqgrow') !== -1) {
							rowid = row.id;
							rowData = data.rows[rowIdx++];
							var dataObject = datas.filter(function(a){
								return a.componentId==rowid}
							)[0];
							
							if(dataObject.licenseTypeIdx != "1" && className.indexOf('excludeRow') === -1) {
								if(strBomValidMsgData.indexOf(rowid) == -1 /*&& strBomDiffMsgData.indexOf(rowid) == -1*/){
									className= className + ' excludeRow';
								}
							}
							
							row.className = className;
						} else if(className.indexOf('ui-subgrid') !== -1){
							rowIdx++;
						}
					}
				}
				, onSelectRow: function(rowid,status,eventObject) {}
				, beforeSelectRow: function(rowid, e) {
					return true;
				}
				, onCellSelect: function(rowid,iCol,cellcontent,e) {
					if(iCol == "0"){
						fn_grid_com.showOssViewPage(bomList, rowid, true, bomValidMsgData, bomDiffMsgData, null, com_fn.getLicenseName);
					}
				}
				, ondblClickRow: function(rowid,iRow,iCol,e) {
					// 해당 탭 이동 및 선택된 행 셀렉트
					if(iCol != "0" && iCol != "19" && iCol != "20"){
						var tabSeq = bomList.jqGrid('getCell',rowid,'referenceDiv');
						var componentId = bomList.jqGrid('getCell',rowid,'refComponentId');

						if(tabSeq.indexOf(",") > -1){
							tabSeq = tabSeq.split(",")[0];	
						}
						
						if(componentId.indexOf(",") > -1){
							componentId = componentId.split(",")[0];	
						}
						
						switch(tabSeq){
							case "3rd" : //list3
								tabSeq = "0"
									
								$(".tabMenu a").eq(tabSeq).click();
								$("#list3").jqGrid("setSelection", componentId);
								$("#list3 #" +componentId).focus();

								break;
							case "SRC" : //srcList
								tabSeq = "1"
									
								$(".tabMenu a").eq(tabSeq).click();
								$("#srcList").jqGrid("setSelection", componentId);
								$("#srcList #" +componentId).focus();

								break;
							case "BIN" : //binList
								tabSeq = "2"
									
								$(".tabMenu a").eq(tabSeq).click();
								$("#binList").jqGrid("setSelection", componentId);
								$("#binList #" +componentId).focus();

								break;
						}
					}
				}
				, onPaging: function(action) {
					cleanErrMsg("bomList");

					fn_grid_com.totalGridSaveMode('bomList');
				}
				, gridComplete: function(){
					tableRefresh();
				}
			});

			$("#bomList").jqGrid('navGrid',"#bomPager",{add:false,edit:false,del:false,search:false,refresh:false});
			$("#bomList").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn",
				beforeSearch : function () {
					if(!this.p.postData.referenceId) {
						this.p.postData.referenceId = '${project.prjId}';
					}
				}	
			});
		}
	}
</script>