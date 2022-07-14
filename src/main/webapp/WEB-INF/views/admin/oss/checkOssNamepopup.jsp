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
			var ossDataList;
			var commentId = "";
			var referenceId = '${projectInfo.prjId}';
			var referenceDiv = '${projectInfo.referenceDiv}';
			var evt_fn = {
				init : function(){
					$("#btnChangeOss").on("click", function(){
						Ctrl_fn.changeProc();				
					});

					$("#btnAddNickname").on("click", function(){
						Ctrl_fn.addProc();				
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
						url : '<c:url value="/oss/getCheckOssNameAjax/${projectInfo.targetName}"/>',
						dataType : 'json',
						cache : false,
						data : params,
						contentType : 'application/json',
						success : function(resultData){
							ossValidMsg = resultData.validMap;
							ossDiffMsg = resultData.diffMap;
							ossDataList = resultData.list;

							grid_fn.load();
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
								rowdata["checkName"] = rowdata["checkName"].replace(/(<([^>]+)>)/ig,"");
								rowdata["componentIdList"] = rowdata["componentIdList"].split(",");
								<c:if test="${projectInfo.targetName eq 'identification'}">
								rowdata["refPrjId"] = rowdata["referenceId"];
								rowdata["referenceId"] = commentId;
								rowdata["referenceDiv"] = referenceDiv.split("-")[0];
								</c:if>
								<c:if test="${projectInfo.targetName eq 'partner'}">
								rowdata["referenceId"] = rowdata["referenceId"];
								rowdata["refPrjId"] = commentId;
								rowdata["referenceDiv"] = referenceDiv;
								</c:if>
								
								$.ajax({
									url : '<c:url value="/oss/saveOssCheckName/${projectInfo.targetName}"/>',
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
										<c:if test="${projectInfo.targetName eq 'identification' || projectInfo.targetName eq 'partner'}">
										commentId = resultData.commentId;
										</c:if>
										
										if(result.length == idArry.length){
											if(!failFlag){
												var successMsg = '<spring:message code="msg.project.check.oss.name.success" />';

												/**
												 * identifcaiton tab order you're working on
												 */
												var identificationTabOrder;

												/**
												 * params["referenceDiv"] is id of identification type
												 * 3RD, SRC, BIN, BIN-Android, BOM
												 * 10 (3RD) -> tab index: 0
												 * 11 (SRC) -> tab index: 1
												 * 15 (BIN) -> tab index: 2
												 * 14 (ANDROID) -> tab index: 3
												 * 13 (BOM) -> tab index: 4
												 */
												switch(rowdata["referenceDiv"]) {
												    case "10":    identificationTabOrder = "0";    break;
												    case "11":    identificationTabOrder = "1";    break;
												    case "15":    identificationTabOrder = "2";    break;
												    case "14":    identificationTabOrder = "3";    break;
												    case "13":    identificationTabOrder = "4";    break;
												}

												<c:if test="${projectInfo.targetName eq 'identification' || projectInfo.targetName eq 'partner'}">
												successMsg += '<br>(<spring:message code="msg.project.entered.first.tab" />)';
												</c:if>

												/**
												 * reload identication tab you're working on
												 */
												opener.location.href = `/project/identification/\${rowdata["refPrjId"]}/\${identificationTabOrder}`
												alertify.success(successMsg, 5); // 5sec동안 message 출력
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
				},
				addProc : function(){
					$('#loading_wrap_popup').show();
					
					var target = $("#ossList");
					var result = [];
					var failFlag = false;
					var idArry = grid_fn.getCheckedRow("ADD");

					var idArryOnlyInOssList = grid_fn.getCheckedRow("ADD").filter(i => {
						let input = $("#ossList").getRowData(i)['checkName'];
						let regexp = /^(<a).*(<\/a>)$/;
						return regexp.test(input);
					});

					if(idArryOnlyInOssList.length > 0){
						window.setTimeout(function(){
							for(var i = 0 ; i < idArryOnlyInOssList.length ; i++){
								var rowId = idArryOnlyInOssList[i];

								cleanErrMsg("ossList", rowId);
								
								var rowdata = target.getRowData(rowId);
								rowdata["checkName"] = rowdata["checkName"].replace(/(<([^>]+)>)/ig,"");
								
								$.ajax({
									url : '<c:url value="/oss/saveOssNickname"/>',
									type : 'POST',
									data : JSON.stringify(rowdata),
									dataType : 'json',
									cache : false,
									async: false,
									contentType : 'application/json',
									success: function(resultData){
										if(resultData.isValid == "true"){
											$("#ossList").jqGrid('setCell', idArryOnlyInOssList[i], 'addFlag', 'Y');
											$("#ossList").jqGrid('setCell', idArryOnlyInOssList[i], 'result', 'Y');
										}
										else {
											$("#ossList").jqGrid('setCell', idArryOnlyInOssList[i], 'addFlag', 'N');
											$("#ossList").jqGrid('setCell', idArryOnlyInOssList[i], 'result', 'N');
											alertify.error('<spring:message code="already.added.nickname" />', 0);
											failFlag = true;
										}
										
										result.push(resultData);
																
										if(result.length == idArryOnlyInOssList.length){
											if(!failFlag){
												alertify.success('<spring:message code="msg.selfcheck.nickname.success" />');
												opener.location.reload();
											}
											
											$('#loading_wrap_popup').hide();
										}
									},
									error: function(resultData){}
								});
							}
						}, 0);
					} else if(idArry.length > 0){
						alertify.error('<spring:message code="msg.oss.warn.unregistered" />');
						$('#loading_wrap_popup').hide();
					} else {
						alertify.alert('<spring:message code="msg.oss.required.select" />', function(){});
						$('#loading_wrap_popup').hide();
					}
				},
				showOssViewPage : function(ossName){
					if(ossName!=""){
						onAjaxLoadingHide = true;
						$.ajax({
							url : '<c:url value="/oss/checkExistsOssByname"/>',
							type : 'GET',
							dataType : 'json',
							cache : false,
							async: false,
							data : {ossName : ossName},
							contentType : 'application/json',
							success : function(data){
								if(data.isValid == 'true') {
									var _encUrl = "ossName="+Ctrl_fn.replaceGetParamChar(ossName);
									
									if(_popup == null || _popup.closed) {
										_popup = window.open("<c:url value='/oss/osspopup?"+_encUrl+"'/>", "ossViewPopup_"+ossName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");

										if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
											alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
										}
									} else {
										_popup.close();
										
										_popup = window.open("<c:url value='/oss/osspopup?"+_encUrl+"'/>", "ossViewPopup_"+ossName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");
									}
								}
							},
							error : function(){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					}
				},
				replaceGetParamChar : function(_param) {
					_param = _param.replace(/&/g,"%26");
					_param = _param.replace(/\+/g,"%2B"); 
					
					 return _param;
				}
			};

			var grid_fn = {
					load : function(){
						$('#ossList').jqGrid({
							datatype: 'local',
							data : ossDataList,
							colNames: ['ID','Result','Download location','OSS name (now)', 'Registered OSS name<br>(to be changed)', 'changeFlag', 'addFlag', 'referenceId', 'referenceDiv', 'componentIdList'],
							colModel: [
								{name: 'gridId', index: 'gridId', hidden:true, key:true},
								{name: 'result', index: 'result', <c:if test="${sessUserInfo.authority eq 'ROLE_ADMIN'}">width: 20</c:if><c:if test="${sessUserInfo.authority ne 'ROLE_ADMIN'}">width:12</c:if>, align: 'center',editable: false, formatter: grid_fn.displayStatus, unformatter: grid_fn.unformatter, sortable:false},
								{name: 'downloadLocation', index: 'downloadLocation', width: 90, align: 'left',editable: false, formatter:grid_fn.displayDownloadLocation, sortable:false},
								{name: 'ossName', index: 'ossName', width: 50, align: 'left',editable: false, sortable:false},
								{name: 'checkName', index: 'checkName', width: 50, align: 'left',editable: false, formatter:grid_fn.displayCheckName, sortable:false},
								{name: 'changeFlag', index: 'changeFlag', width: 50, hidden:true, sortable:false},
								{name: 'addFlag', index: 'addFlag', width: 50, hidden:true, sortable:false},
								{name: 'referenceId', index: 'referenceId', width: 50, hidden: true, sortable: false},
								{name: 'referenceDiv', index: 'referenceDiv', width: 50, hidden: true, sortable: false},
								{name: 'componentIdList', index: 'componentIdList', width: 50, hidden: true, sortable: false}
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
									$("#btnAddNickname").show();
									
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
							gridComplete:function (){},
							onCellSelect: function(rowid,iCol,cellcontent,e) {
								if(iCol=="5"){
									var popupVisible = $(".sheetSelectPop").is(':visible');
									if(cellcontent.indexOf("<br>") > -1 && !popupVisible){
										var checkNameArr = cellcontent.split("<br>");
										for(var i=0; i < checkNameArr.length; i++){
											$('.sheetSelectPop .sheetNameArea').append('<li><input type="checkbox" name="checkNameSelect" id="checkName'+i+'" value="'+rowid+'" class="sheetNum">'
													+'<label for="checkName'+i+'">'+checkNameArr[i]+'</label></li>');
										}

										$('input:checkbox[name=checkNameSelect]').click(function(){
											if($(this).prop('checked')){
												$('input:checkbox[name=checkNameSelect]').prop('checked', false);
												$(this).prop('checked', true);
											}
										});
										
										$(".pop").css("left", "40%");
										$(".sheetSelectPop").show();
									}
								}
							}
						});
					
						$('#ossList').closest(".ui-jqgrid-bdiv").css({"height":"373px", "overflow-y" : "scroll"});
					},
					displayStatus : function(cellvalue, options, rowObject){
						var display = "";
						var changeFlag = rowObject.changeFlag || rowObject[6];
						var addFlag = rowObject.addFlag || rowObject[7];
						
						if(changeFlag =="Y"){
							display += "<a class='btnPG wAnd onAnd'>Change</a>";
						} else {
							display += "<a class='btnPG wAnd off'>Change</a>";
						}

						if('${sessUserInfo.authority}' == 'ROLE_ADMIN'){
							if(addFlag =="Y" ){
								display += "<a class='btnPG onBin'>Add</a>";
							} else {
								display += "<a class='btnPG off'>Add</a>";
							}
						}
						
						return display;
					},
					displayCheckName : function(cellvalue, options, rowObject){
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
					getCheckedRow : function(processType){
						var seq = processType.toUpperCase() == "CHANGE" ? 6 : 7;
						return $("#ossList").jqGrid ('getGridParam', 'selarrrow').reduce(function(arr, item) {
						    if(!$("#"+item).hasClass("excludeRow") && $("#"+item+" > td:eq("+seq+")").text() != "Y"){
						        arr.push(item);
						    }
						    
						    return arr;
						}, []);
					},
					selectChangedOssName : function(){
						var param = $("#ossList").jqGrid('getGridParam', 'data');
						var rowId = $('input:checkbox[name=checkNameSelect]:checked').val();
						var checkedValue = $('input:checkbox[name=checkNameSelect]:checked').next().text();

						for (var i=0; i<param.length; i++){
							if(param[i]["componentId"] == rowId){
								param[i]["checkName"] = checkedValue;
							}
						}

						$('#ossList').jqGrid('GridUnload');

						ossDataList = param;
						grid_fn.load();

						$('#'+rowId).removeClass('excludeRow');

						$('.sheetNameArea').empty();
						$('.sheetSelectPop').hide();
					},
					closePop : function(){
						$('input:checkbox[name=ossNameSelect]').prop('checked', false);
						$('.sheetNameArea').empty();
						$('.sheetSelectPop').hide();
					}
			};
		</script>
	</head>
	<body>
		<div id="loading_wrap_popup" class="loading" style="display:none;">
			<div class="loadingBlind"></div>
			<img src="${ctxPath}/images/loading.gif" alt="loading" />
		</div>
		<div id="wrap" style="padding-top: 20px;">
			<div  align="center" >
				<div class="jqGridSet" style="overflow: auto; width: 98%; height: 500px;">
					<div align="left" style="padding-bottom: 20px;">
						<b><spring:message code="msg.project.check.oss.name" /></b>
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
		<div class="pop sheetSelectPop" style="width:400px; display:none;">
			<h1>Select, To Be Changed OSS Name</h1>
			<div class="popdata">
				<ol class="sheetNameArea"></ol>
			</div>
			<div class="pbtn">
				<input type="button" value="Cancel" class="btnCancel btnColor" onclick="grid_fn.closePop()">
				<input type="button" value="OK" class="btnColor red sheetApply" onclick="grid_fn.selectChangedOssName()">
			</div>
		</div>
	</body>
</html>
