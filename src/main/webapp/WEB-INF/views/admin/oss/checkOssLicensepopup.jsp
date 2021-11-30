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
									colNames: ['ID','Result','Download location','OSS name', 'OSS version', 'License<br>(current)', 'License<br>(to be changed)', 'Evidence', 'changeFlag', 'addFlag', 'referenceId', 'referenceDiv', 'componentIdList', 'checkedRuleType'],
									colModel: [
										{name: 'gridId', index: 'gridId', hidden:true, key:true},
										{name: 'result', index: 'result', width:20, align: 'center',editable: false, formatter: grid_fn.displayStatus, unformatter: grid_fn.unformatter, sortable:false},
										{name: 'downloadLocation', index: 'downloadLocation', width: 90, align: 'left',editable: false, formatter:grid_fn.displayDownloadLocation, sortable:false},
										{name: 'ossName', index: 'ossName', width: 50, align: 'left',editable: false, sortable:false},
										{name: 'ossVersion', index: 'ossVersion', width: 40, align: 'left',editable:false, sortable:false},
										{name: 'licenseName', index: 'licenseName', width: 50, align: 'left',editable:false, sortable:false},
										{name: 'checkLicense', index: 'checkLicense', width: 50, align: 'left',editable: false, formatter:grid_fn.displayCheckLicense, sortable:false},
										{name: 'checkedEvidence', index: 'checkedEvidence',  width: 20, align: 'left',editable: false, formatter:grid_fn.displayCheckedEvidence, sortable:false},
										{name: 'changeFlag', index: 'changeFlag', width: 50, hidden:true, sortable:false},
										{name: 'addFlag', index: 'addFlag', width: 50, hidden:true, sortable:false},
										{name: 'referenceId', index: 'referenceId', width: 50, hidden: true, sortable: false},
										{name: 'referenceDiv', index: 'referenceDiv', width: 50, hidden: true, sortable: false},
										{name: 'componentIdList', index: 'componentIdList', width: 50, hidden: true, sortable: false},
										{name: 'checkedEvidenceType', index: 'checkedEvidenceType', width: 50, hidden: true, sortable: false},
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
													
													if(dataObject.checkLicense.indexOf("|") > -1) {
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
				},
				changeProc : function(){
					$('#loading_wrap_popup').show();

					var target = $("#ossList");
					var result = [];
					var failFlag = false;
					var idArry = grid_fn.getCheckedRow("CHANGE");

					if(idArry.length > 0){
						window.setTimeout(function(){
							for(var i = 0 ; i < idArry.length ; i++){
								var rowId = idArry[i];

								cleanErrMsg("ossList", rowId);

								var rowdata = target.getRowData(rowId);
								rowdata["checkLicense"] = rowdata["checkLicense"].replace(/(<([^>]+)>)/ig,"");
								rowdata["componentIdList"] = rowdata["componentIdList"].split(",");							
								<c:if test="${projectInfo.targetName eq 'identification'}">
								rowdata["refPrjId"] = rowdata["referenceId"];
								rowdata["referenceId"] = commentId;
								rowdata["referenceDiv"] = referenceDiv.split("-")[0];
								</c:if>

								$.ajax({
									url : '/oss/saveOssCheckLicense/${projectInfo.targetName}',
									type : 'POST',
									data : JSON.stringify(rowdata),
									dataType : 'json',
									cache : false,
									async: false,
									contentType : 'application/json',
									success: function(resultData){
										if(resultData.isValid == true){
											$("#ossList").jqGrid('setCell', idArry[i], 'changeFlag', 'Y'); // popup grid Data change => success
											$("#ossList").jqGrid('setCell', idArry[i], 'result', 'Y');
										} else {
											$("#ossList").jqGrid('setCell', idArry[i], 'changeFlag', 'N');
											$("#ossList").jqGrid('setCell', idArry[i], 'result', 'N');
											alertify.error('<spring:message code="msg.common.valid2" />', 0);
											failFlag = true;
										}

										result.push(resultData);
										<c:if test="${projectInfo.targetName eq 'identification'}">
										commentId = resultData.commentId;
										</c:if>

										if(result.length == idArry.length){
											if(!failFlag){
												var successMsg = 'Successfully changed OSS License <br> into OSS table';

												<c:if test="${projectInfo.targetName eq 'identification'}">
												successMsg += '.<br>(It moves to the tab you entered first.)';
												</c:if>

												alertify.success(successMsg, 5); // 5sec동안 message 출력
												opener.location.reload();
											}

											$('#loading_wrap_popup').hide();
										}
									},
									error: function(resultData){}
								});
							}
						}, 0);
					} else {
						alertify.alert('<spring:message code="msg.oss.required.select" />', function(){});
						$('#loading_wrap_popup').hide();
					}
				}
			};
			var grid_fn = {
					displayStatus : function(cellvalue, options, rowObject){
						var display = "";
						var changeFlag = rowObject.changeFlag || rowObject[8];

						if(changeFlag =="Y"){
							display += "<a class='btnPG wAnd onAnd'>Change</a>";
						} else {
							display += "<a class='btnPG wAnd off'>Change</a>";
						}
						
						return display;
					},
					displayDownloadLocation : function(cellvalue, options, rowObject){
						var display = "";
						var downloadLocation = rowObject["downloadLocation"];

						display = downloadLocation.split(",");

						return display.join("<br>");
					},
					unformatter : function(cellvalue, options, rowObject){
						return cellvalue;
					},
					displayCheckLicense : function(cellvalue, options, rowObject){
						var checkLicense = rowObject["checkLicense"];
						var display = checkLicense.split("|");

						return display.join("<br>");
					},
					displayCheckedEvidence : function(cellvalue, options, rowObject){
						var checkedEvidenceType = rowObject["checkedEvidenceType"];
						var display = "";

						console.log(checkedEvidenceType);

						if(cellvalue != "" && cellvalue != undefined) {
							if(checkedEvidenceType == "CD") {
								display = "<div class=\"tcenter\"><a class=\"evidenceIcon clearlydefined\" title=\""+cellvalue+"\" >"+cellvalue+"</a></div>";
							} else if(checkedEvidenceType == "GH") {
								display = "<div class=\"tcenter\"><a class=\"evidenceIcon github\" title=\""+cellvalue+"\" >"+cellvalue+"</a></div>";
							} else if(checkedEvidenceType == "DB") {
								display = "<div class=\"tcenter\"><a class=\"evidenceIcon existdb\" title=\""+cellvalue+"\" >"+cellvalue+"</a></div>";
							}
						}

						return display;
					},
					getCheckedRow : function(processType){
						var seq = processType.toUpperCase() == "CHANGE" ? 8 : 9;
						return $("#ossList").jqGrid ('getGridParam', 'selarrrow').reduce(function(arr, item) {
							if(!$("#"+item).hasClass("excludeRow") && $("#"+item+" > td:eq("+seq+")").text() != "Y"){
								arr.push(item);
							}

							return arr;
						}, []);
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
						<b><spring:message code="msg.project.check.license" /></b>
					</div>
					<table id="ossList"><tr><td></td></tr></table>
					<div align="left" style="padding-top: 10px;">
						<input type="button" value="Change License" id="btnChangeOss" class="btnColor red" style="display: none; width:150px;" />
					</div>
				</div>
			</div>
		</div>
	</body>
</html>