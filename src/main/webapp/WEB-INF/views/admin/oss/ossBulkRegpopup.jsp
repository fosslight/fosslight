<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- Administrator screen template. --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
		var lastSelection;
		var ossBulkValidMsg;
		$(document).ready(function() {
			fn_init();
			fn_loadGridData();
		});
		
		// If you select view only data whose os name is unregistered or data whose version is unregistered,
		function fn_init() {
			$("#loadTypeOSS, #loadTypeVER").change(function(){
				fn_hideRowCtrl();
			});
		}
		
		function fn_hideRowCtrl() {
			var grid = $("#ossList");
			var ids = grid.jqGrid('getDataIDs');
			
			var hideUnconfirmedOSS = false;
			var hideUnconfirmedVersion = false;
			
			if($("#loadTypeOSS").is(":checked")){
				hideUnconfirmedVersion = true;
	        }

			if($("#loadTypeVER").is(":checked")){
				hideUnconfirmedOSS = true;
	        }
			
			$.each(ids, function(index, rowId){
				var regType = grid.jqGrid('getCell', rowId, 'regType');
				
				if( ("OSS" == regType && hideUnconfirmedOSS) || ("VER" == regType && hideUnconfirmedVersion) ) {
					$("#"+rowId).hide();
				} else {
					$("#"+rowId).show();
				}
			});

		}
		
		function fn_loadGridData() {
			var params = {
					referenceId : '${projectInfo.prjId}'
					, referenceDiv : '${projectInfo.referenceDiv}'
			};
			
			$.ajax({
				url : '/oss/getOssBulkRegAjax',
				dataType : 'json',
				cache : false,
				data : params,
				contentType : 'application/json',
				success : function(resultData){
					if(resultData.ossList.length > 0) {
						$("#btnAddOss").show();
					}
					
					ossBulkValidMsg = resultData.validMapResult;
					ossBulkDiffMsg = resultData.diffMapResult;
					
					$('#ossList').jqGrid({
							datatype: 'local',
							data : resultData.ossList,
							colNames: ['ID','Result', 'OSS Name','OSS Version', 'Nick Name', 'Declared License', 'Copyright', 'Download Location', 'Home Page', 'Summary Description', 'Comment', 'regType'],
							colModel: [
								{name: 'gridId', index: 'gridId', hidden:true, key:true},
								{name: 'procStatus', index: 'procStatus', width: 40, align: 'left',editable: false},
								{name: 'ossName', index: 'ossName', width: 70, align: 'left',editable: true},
								{name: 'ossVersion', index: 'ossVersion', width: 40, align: 'left',editable: true},
								{name: 'ossNickname', index: 'ossNickname', width: 150, align: 'left',editable: true},
								{name: 'licenseName', index: 'licenseName', width: 100, align: 'left',editable: true},
								{name: 'copyright', index: 'copyright', width: 200, align: 'left',editable: true, edittype:"textarea", editoptions:{rows:"3",cols:"30"}},
								{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left',editable: true,
									editoptions: {
										dataInit:
											function (e) {
												$(e).on("blur", function() {
													var value = e.value;
													
													if(value.charAt(value.length-1) == "/"){
														value = value.slice(0, -1); // Remove the last string.
														$("#"+e.id).val(value);
													}
												});
											}
									}
								},
								{name: 'homepage', index: 'homepage', width: 100, align: 'left',editable: true,
									editoptions: {
										dataInit:
											function (e) {
												$(e).on("blur", function() {
													var value = e.value;
													
													if(value.charAt(value.length-1) == "/"){
														value = value.slice(0, -1); // Remove the last string.
														$("#"+e.id).val(value);
													}
												});
											}
									}
								},
								{name: 'summaryDescription', index: 'summaryDescription', width: 200, align: 'left', editable: true, edittype:"textarea", editoptions:{rows:"3",cols:"30"}},
								{name: 'comment', index: 'comment', width: 200, align: 'left',editable: true, edittype:"textarea", editoptions:{rows:"3",cols:"30"}},
								{name: 'regType', index: 'regType',editable: false, hidden:true}
							],
							autoencode: true,
							autowidth: true,
							gridview: true,
							editurl:'clientArray',
							loadonce: true,
							height: 'auto',
							rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
							multiselect: true,
							loadComplete: function(data){
								if(ossBulkValidMsg) {
									gridValidMsgNew(ossBulkValidMsg, "ossList");
								}
								
								if(ossBulkDiffMsg){
									gridDiffMsg(ossBulkDiffMsg, "ossList");
								}
							},
							onSelectRow: function(rowid){
				               if (rowid) {
				                    var grid = $("#ossList");

				                    if(lastSelection && $("#ossList #"+lastSelection).attr("editable") != "9") {
					    				cleanErrMsg("ossList", lastSelection);
					                    grid.jqGrid('saveRow',lastSelection);
					                    gridValidMsgRowId(ossBulkValidMsg, "ossList", lastSelection);
				                    }
				                    
				                    if($("#ossList #"+rowid).attr("editable") != "9") {
					    				cleanErrMsg("ossList", rowid);
					                    grid.jqGrid('editRow',rowid, {keys: true} );
				                    }
				                    
				                    lastSelection = rowid;
				                }
				                
		                    	// If processing is already completed, the value of the check box is initialized.
		                    	if($("input:checkbox[id='jqg_ossList_"+rowid+"']").attr( 'disabled') == "disabled") {
			                    	$("input:checkbox[id='jqg_ossList_"+rowid+"']").attr( 'checked', false );
		                    	}
							},
							gridComplete:function (){
								fn_hideRowCtrl();
							}
					});
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
		
		function fn_registration() {
			$('#loading_wrap_popup').show();
			var params = new Array();
			var idArry = $("#ossList").jqGrid ('getGridParam', 'selarrrow').reduce(function(arr, item) {
				if($("input:checkbox[id='jqg_ossList_"+item+"']").is(":checked") && $("#"+item).css("display") != "none"){
					arr.push(item);
				}
				
				return arr;
			}, []); // Imported only rows that selected checkbox.
			
			var result = [];
			
			// Check whether it's okay or not.
			var hasChecked = false;
			var gridObj = $("#ossList");
			var gridStr = "ossList";
			
			window.setTimeout(function(){
				for (var i = 0; i < idArry.length; i++) { //Run as many times as row id.
				    	var rowId = idArry[i];
				    	cleanErrMsg("ossList", rowId);
				    	gridObj.jqGrid('saveRow',rowId);
				    	
					    var rowdata = gridObj.getRowData(rowId); // Obtained row data of the id.
					    hasChecked = true;
		
					    // 1. checked 된 row의 데이터를 그대로 배열에 담는 경우
					    //params.push(rowdata); //배열에 맵처럼 담김
					    $("#ossList #"+rowId+" td[aria-describedby='ossList_procStatus']").text("");
					    $.ajax({
							url : '/oss/saveOssBulkReg',
							type : 'POST',
							data : JSON.stringify(rowdata),
							dataType : 'json',
							cache : false,
							async: false,
							contentType : 'application/json',
							success: function(resultData){
								result.push(resultData);
								
								if(resultData.isValid == "true") {
									ossBulkDiffMsg = resultData.resultData;
									
									$("#ossList #"+rowId+" td[aria-describedby='ossList_procStatus']").append(resultData.validMsg);
									$("#ossList #"+rowId).attr("editable", "9"); // view mode로 변경
									$("input:checkbox[id='jqg_ossList_"+rowId+"']").attr( 'checked', false ).attr( 'disabled', true );
								} else {
									ossBulkValidMsg = resultData.validMapResult;
									ossBulkDiffMsg = resultData.diffMapResult;
									
									if(ossBulkValidMsg) {
										$("#ossList #"+rowId+" td[aria-describedby='ossList_procStatus']").append("Fail");
										$.each(ossBulkValidMsg,function(key,value) {
											var errRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + key+"']");
											if(errRow) {
												errRow.append('<div class=\"'+gridStr+"_"+rowId+' retxt"\">'+ value +'</div>');
											}
										});
									}
								}	
								
								if(ossBulkDiffMsg){
									$.each(ossBulkDiffMsg,function(key,value) {
										if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key && "externalData3" != key) {
											var diffRow;
											
											if(rowId.indexOf("-") == -1) {
												diffRow = $("#"+rowId+" > td[aria-describedby='"+gridStr + "_" + key+"']");
											}
											
											// 그리드 메세지 그리기
											if(diffRow) {
												diffRow.append('<div class=\"'+gridStr+"_"+rowId+' retxtb"\">'+ value +'</div>');
											}
										}
									});
								}
								
								if(result.length == idArry.length){
									$('#loading_wrap_popup').hide();
								}
							},
							error: function(resultData){}
						});
				}
				
				if(!hasChecked) {
					alertify.alert('<spring:message code="msg.oss.required.select" />', function(){});

					$('#loading_wrap_popup').hide();
				}
			}, 0);
		}
		</script>
	</head>
	<body>
		<div id="loading_wrap_popup" class="loading" style="display:none;">
			<div class="loadingBlind"></div>
			<img src="/images/loading.gif" alt="loading" />
		</div>
		<div id="wrap" style="padding-top: 20px;">
			<div  align="center" >
				<div class="jqGridSet" style="overflow: auto; width: 95%; height: 750px;">
					<div align="left" style="padding-bottom: 10px;">
						<span class="checkSet">
							<input type="checkbox" id="loadTypeOSS" ><b>Show Unconfirmed open source only</b>
							<input type="checkbox" id="loadTypeVER" ><b>Show Unconfirmed version only</b>
						</span>
					</div>
					<table id="ossList"><tr><td></td></tr></table>
					<div align="left" style="padding-top: 10px;">
						<input type="button" value="Add" id="btnAddOss" onclick="fn_registration();" class="btnColor red" style="display: none;" />
					</div>
				</div>
			</div>

		</div>
	</body>
</html>