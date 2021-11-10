<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
			$(document).ready(function() {
				evt_fn.init();
				Ctrl_fn.loadGridData();
			});
			var _popup = null;
			var ossValidMsg;
			var ossDiffMsg;
			var commentId = "";
			var referenceId = '${projectInfo.prjId}';
			var referenceDiv = '${projectInfo.referenceDiv}';
			var evt_fn = {
				init : function(){
					$("#btnChangeOss").on("click", function(){
						Ctrl_fn.changeProc();				
					});
				}
			};
			
			var Ctrl_fn = {
				loadGridData : function(){
					$('#loading_wrap_popup').show();
					
					var params = {};
					params["referenceId"] = referenceId;
					params["referenceDiv"] = referenceDiv;
					
					<c:if test="${projectInfo.targetName eq 'identification'}">
					params["referenceDiv"] = params["referenceDiv"].split("-")[0];
					</c:if>
					
					$.ajax({
						url : '/oss/getCheckOssLicenseAjax/${projectInfo.targetName}',
						dataType : 'json',
						cache : false,
						data : params,
						contentType : 'application/json',
						success : function(resultData){
							ossValidMsg = resultData.validMap;
							ossDiffMsg = resultData.diffMap;
							
							$('#ossList').jqGrid({
									datatype: 'local',
									data : resultData.list,
									colNames: ['ID','Result','Download location','OSS name', 'OSS version', 'License<br>(current)', 'License<br>(to be changed)', 'changeFlag', 'addFlag', 'referenceId', 'referenceDiv'],
									colModel: [
										{name: 'gridId', index: 'gridId', hidden:true, key:true},
										{name: 'result', index: 'result', width:20, align: 'center',editable: false, formatter: grid_fn.displayStatus, unformatter: grid_fn.unformatter, sortable:false},
										{name: 'downloadLocation', index: 'downloadLocation', width: 90, align: 'left',editable: false, formatter:grid_fn.displayDownloadLocation, sortable:false},
										{name: 'ossName', index: 'ossName', width: 50, align: 'left',editable: false, sortable:false},
										{name: 'ossVersion', index: 'ossVersion', width: 40, align: 'left',editable:false, sortable:false},
										{name: 'licenseName', index: 'licenseName', width: 50, align: 'left',editable:false, sortable:false},
										{name: 'checkName', index: 'checkName', width: 50, align: 'left',editable: false, formatter:grid_fn.displayCheckLicense, sortable:false},
										{name: 'changeFlag', index: 'changeFlag', width: 50, hidden:true, sortable:false},
										{name: 'addFlag', index: 'addFlag', width: 50, hidden:true, sortable:false},
										{name: 'referenceId', index: 'referenceId', width: 50, hidden: true, sortable: false},
										{name: 'referenceDiv', index: 'referenceDiv', width: 50, hidden: true, sortable: false}
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
										if(data.total > 0){
											$("#btnChangeOss").show();
											
											var datas = data.rows, rows=this.rows, row, className, rowsCount=rows.length,rowIdx=0;
											for(var _idx=0;_idx<rowsCount;_idx++) {
												row = rows[_idx];
												className = row.className;
												
												if (className.indexOf('jqgrow') !== -1) {
													rowid = row.id;
													rowData = data.rows[rowIdx++];
													var dataObject = datas.filter(function(a){return a.componentId==rowid})[0];
													
													if(dataObject.checkName.indexOf("|") > -1) {
														className= className + ' excludeRow';
													}
													
													row.className = className;
												} else if(className.indexOf('ui-subgrid') !== -1){
													rowIdx++;
												}
											}
										}
										
										$('#loading_wrap_popup').hide();

										if(ossValidMsg) {
											gridValidMsgNew(ossValidMsg, "ossList", "SELF");
										}
										
										if(ossDiffMsg){
											gridDiffMsg(ossDiffMsg, "ossList", "SELF");
										}
										
									},
									onSelectRow: function(rowid){},
									gridComplete:function (){}
							});
							
							$('#ossList').closest(".ui-jqgrid-bdiv").css({"height":"373px", "overflow-y" : "scroll"});
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});	
				}
			};
			var grid_fn = {
					displayStatus : function(cellvalue, options, rowObject){
						var display = "";
						var changeFlag = rowObject.changeFlag || rowObject[6];
						
						if(changeFlag =="Y"){
							display += "<a class='btnPG wAnd onAnd'>Change</a>";
						} else {
							display += "<a class='btnPG wAnd off'>Change</a>";
						}
						
						return display;
					},
					displayCheckLicense : function(cellvalue, options, rowObject){
						var display = "";
						var checkName = rowObject["checkName"];
						var checkOssList = rowObject["checkOssList"];
						
						display = checkName.split("|");

						for(var i in display){
							display[i] = checkOssList == "Y" ? 
								"<a href='#' onclick='Ctrl_fn.showOssViewPage(\""+display[i]+"\")' style='color:#2883f3;text-decoration:underline;'>"+display[i]+"</a>"
								: display[i];
						}

						return display.join("<br>");
					}
			};
		</script>
	</head>
	<body>
		<div id="loading_wrap_popup" class="loading" style="display:none;">
			<div class="loadingBlind"></div>
			<img src="/images/loading.gif" alt="loading" />
		</div>
		<div id="wrap" style="padding-top: 20px;">
			<div  align="center" >
				<div class="jqGridSet" style="overflow: auto; width: 98%; height: 500px;">
					<div align="left" style="padding-bottom: 20px;">
						<b>There exists another OSS which has same download location. Please click "Change OSS Name" if you want to change to the registered OSS Name.</b>
						<br>
						<b>동일한 Download Location으로 등록된 OSS가 있습니다. 시스템에 등록된 OSS 이름으로 변경하시려면 체크 후 "Change OSS Name" 버튼을 클릭하시기 바랍니다.</b>
					</div>
					<table id="ossList"><tr><td></td></tr></table>
					<div align="left" style="padding-top: 10px;">
						<input type="button" value="Change OSS Name" id="btnChangeOss" class="btnColor red" style="display: none; width:150px;" />
						<c:if test="${sessUserInfo.authority eq 'ROLE_ADMIN'}">
							<input type="button" value="Add Nickname" id="btnAddNickname" class="btnColor red" style="display: none; width:120px;" />
						</c:if>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>