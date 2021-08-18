<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
		var ossAnalysisStatus = "${project.ossAnalysisStatus}";
		var groupBuffer = '';
		
		$(document).ready(function() {
			if(ossAnalysisStatus.toUpperCase() == "RESULT") {
				Ctrl_fn.loadAnalysisResultGrid();
				
				window.setTimeout(function(){
					var postData = Ctrl_fn.makeParam();
					$("#ossList").jqGrid('setGridParam', {postData:postData, page : 0, url:'/oss/getAnalysisResultList'}).trigger('reloadGrid');
				}, 1000);
			} else {
				Ctrl_fn.loadAnalysisListGrid();
			}
			
			evt_fn.init();
		});
		
		var lastSelection;
		var ossValidMsg;
		var ossDiffMsg;
		var evt_fn = {
			init : function(){				
				$("span[class='ui-icon ui-icon-seek-first'], span[class='ui-icon ui-icon-seek-prev'], span[class='ui-icon ui-icon-seek-next'], span[class='ui-icon ui-icon-seek-end']")
					.on("click", function(){
					    $("#loading_wrap_popup").show()
					});
				
				$("#pg_pager .ui-pg-input").on("keypress", function(e){
					var keyCode = e.keyCode;

					if(keyCode == "13"){
						$("#loading_wrap_popup").show();
					}
				});
				
				$("#pg_pager .ui-pg-selbox").on("change", function(){
					$("#loading_wrap_popup").show();
				});
			}
		};
		
		var Ctrl_fn = {
			oldRowNum:20,
			loadAnalysisResultGrid : function(){
				$('#loading_wrap_popup').show();
				
				$("#ossList").jqGrid({
					datatype: 'json',
					jsonReader:{
						repeatitems: false,
						id:'ossId',
						root:function(obj){
							//기존의 RowNum 저장
							Ctrl_fn.oldRowNum = $("#ossList").jqGrid('getGridParam', 'rowNum');

							//리스트 갯수에 따른 rowNum 변경@@1
							$("#ossList").jqGrid('setGridParam', {rowNum:obj.rows.length});
							$("#ossList").jqGrid('setGridParam', {defaultRowNum:Ctrl_fn.oldRowNum});

							return obj.rows; 
						},
						page:function(obj){return obj.page;},
						total:function(obj){return obj.total;},
						records:function(obj){return obj.records;}
					},
					colNames: ['', 'Title', 'Result', 'gridId', 'OSS Name', 'OSS Version', 'OSS NickName', 'Declared License Name', 'Copyright', 'Download Location'
						 , 'Homepage', 'Description', 'Comment', '', '', ''],
					colModel: [
						{name:'checkbox', align: 'center', width:13
							, formatter: function radio(cellvalue, options, rowObject) {
								var btnCheckbox = '<input type="checkbox" name="checkbox_' + rowObject.groupId+ '" onclick="Ctrl_fn.setDuplicateUnCheck(this)"';
								
								if(rowObject.title == "취합정보") {
									btnCheckbox += 'checked';
								}

								btnCheckbox += ' />';

		                        return btnCheckbox;
	                       }
						},
						{name: 'title', width: 100, align: 'right'},
						{name: 'result', index: 'result', width: 33, align: 'center', 
							formatter: function(cellvalue, options, rowObject){
								var result = "";

								if(cellvalue != undefined){
									if(cellvalue.toUpperCase() == 'TRUE' || cellvalue.toUpperCase() == 'FALSE') {
										result = "";
									}
								} else {
									result = cellvalue;
								}
								
								return result;
							}
						},
						{name: 'gridId', index: 'gridId', width: 20, align: 'center', hidden:true, key:true},
						{name: 'ossName', index: 'ossName', width: 120, align: 'left', editable: true},
						{name: 'ossVersion', index: 'ossVersion', width: 40, align: 'left', editable: true},
						{name: 'ossNickname', index: 'ossNickname', width: 120, align: 'left', editable: true},
						{name: 'licenseName', index: 'licenseName', width: 95, align: 'left', editable: true},
						{name: 'ossCopyright', index: 'ossCopyright', width: 100, align: 'left', editable: true, edittype:"textarea", editoptions:{rows:"3",cols:"15"}, 
							formatter: function radio(cellvalue, options, rowObject) {
								return (rowObject.ossCopyright||"")
															.split("\\n").join("\n")
															.split("<").join("&lt;")
															.split(">").join("&gt;");
							}
						},
						{name: 'downloadLocation', index: 'downloadLocation', width: 120, align: 'left', editable: true, 
							editoptions: {
								dataInit: function (e) {
									$(e).on("blur", function() {
										var value = e.value;
										
										if(value.charAt(value.length-1) == "/"){
											value = value.slice(0, -1); // 마지막 문자열 제거
											$("#"+e.id).val(value);
										}
									});
								}
							}
						},
						{name: 'homepage', index: 'homepage', width: 120, align: 'left', editable: true, 
							editoptions: {
								dataInit: function (e) { 
									$(e).on("blur", function() {
										var value = e.value;
										
										if(value.charAt(value.length-1) == "/"){
											value = value.slice(0, -1); // 마지막 문자열 제거
											$("#"+e.id).val(value);
										}
									});
								}
							}
						},
						{name: 'summaryDescription', index: 'summaryDescription', width: 50, height: 50, align: 'left'},
						{name: 'comment', index: 'comment', width: 100, editable: true, edittype:"textarea", editoptions:{rows:"3",cols:"15"}, 
							formatter: function radio(cellvalue, options, rowObject) {
								return (rowObject.comment||"")
														.split("\\n").join("\n")
														.split("<").join("&lt;")
														.split(">").join("&gt;");
							}
						},
						{name: 'detectedLicense', index: 'detectedLicense', width: 0, hidden:true},
						{name: 'completeYn', index: 'completeYn', width: 0, hidden:true},
						{name: 'groupId', index: 'groupId', width:1, hidden:true
							, cellattr: function(rowId, val, rawObject, cm, rdata) {
								var result;
								var isGroup = false;
								
								if(groupBuffer == val){
									isGroup = true;
								}else{
									isGroup = false;
								}
								
								groupBuffer = val;
								
								if(isGroup){
									result = 'isgroup="true"';
								}else{
									result = 'isgroup="false"';
								}
								
								return result;
							} 
						}
					],
					rowNum : ${ct:getConstDef("DISP_PAGENATION_DEFAULT")},
					rowList : [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}],
					autowidth : true,
					pager : '#pager',
					gridview : true,
					viewrecords : true,
					loadonce : false,
					height : '650px',
					grouping:true,
					autoencode: true,
					groupingView:{
						groupField:['groupId'],
						groupColumnShow:[false]
					},	// group by 하는 컬럼명 입력
					loadComplete: function(data){
						if(data.rows.length == 0){
							alertify.error(data.errorMsg);
						}
						
						ossValidMsg = data.validMap;
						ossDiffMsg = data.diffMap;
						
						groupBuffer = '';
						
						$('#loading_wrap_popup').hide();

						$("#ossList").jqGrid('setGridParam', {rowNum:Ctrl_fn.oldRowNum});
						
						var grid = this;
						
						if(ossValidMsg) {
							gridValidMsgNew(ossValidMsg, "ossList");
						}
						
						if(ossDiffMsg){
							gridDiffMsg(ossDiffMsg, "ossList");
						}
						
						
						$('tr td[isgroup="false"]', grid).parent().css('background-color' ,'#E1F6FA');
						
						$('tr td[isgroup="true"]', grid).parent().find('td:eq(1)')
						.prepend($('<span class="ui-icon ui-icon-carat-1-sw left"></span>').css('display','inline-block'));
						
						$('.ossListghead_0').hide();	// 기존 그룹헤더 숨김
						$('#btnRegist').show();

						var successList = $("#ossList").getRowData()
												.filter(function(cur){ return cur.completeYn == "Y"; })
												.map(function(cur){ return cur.groupId; });
						
						if(data.records > 0) {
						    var multRowIds = []; 
						    var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row, rowid, rowData, className;
						    for(var _idx=0;_idx<rowsCount;_idx++) {
						        row = rows[_idx];
						        className = row.className;
						        
						        if (className.indexOf('jqgrow') !== -1) {
						            rowid = row.id;
						            rowData = data.rows[rowIdx++];
						            
						            if(successList.indexOf(rowData.groupId) > -1) {
						                className = className + ' excludeRow';
						                
						                if(rowData.licenseName == undefined){
						                	$('#ossList').jqGrid('setCell', rowData.gridId, 'result', "Deleted (after success)");
							            } else {
						                	$('#ossList').jqGrid('setCell', rowData.gridId, 'result', "Success");
							            }

				        			    $("#"+ rowData.gridId + " > td > [type='checkbox']").attr({"disabled": true, "checked": false});
						            }
						            
						            row.className = className;
						        } else if(className.indexOf('ui-subgrid') !== -1) {
						            rowIdx++;
						        }
						    }
						}
					},
					onSelectRow: function(rowid){
						var completeYn = "SUCCESS" == $("#ossList").getRowData(rowid).result.toUpperCase();
						
						if(!completeYn){
							if (rowid) {
								var grid = $("#ossList");
								
								if(lastSelection != undefined) {
									cleanErrMsg("ossList", lastSelection);
									grid.jqGrid('saveRow',lastSelection);
									gridValidMsgRowId(ossValidMsg, "ossList", lastSelection);
									gridDiffMsgRowId(ossDiffMsg, "ossList", lastSelection);
								}
								          
								cleanErrMsg("ossList", rowid);
								grid.jqGrid('editRow',rowid, {keys: true} );
								
								lastSelection = rowid;
							}
							        
							// 이미 처리완료된 경우 체크박스의 값을 초기화한다.
							if($("input:checkbox[id='jqg_ossList_"+rowid+"']").attr( 'disabled') == "disabled") {
								$("input:checkbox[id='jqg_ossList_"+rowid+"']").attr( 'checked', false );
							}
						}
					},
					ondblClickRow: function(rowid,iRow,iCol,e) {
						if(iCol==1){ // title을 dblclick시에 detailview로 전송
							cleanErrMsg("ossList");
							
							$("#ossList").jqGrid('saveRow',lastSelection);
							
							var completeYn = "SUCCESS" == $("#ossList").getRowData(rowid).result.toUpperCase();
							
							if(!completeYn){
								var groupId = $("#ossList").getRowData(rowid).groupId;
								
								var postData = $("#ossList").getRowData().filter(function(cur,idx){
								    return cur.groupId == groupId;
								});
								for (var i=0; i<postData.length; i++){
									postData[i].licenseName = postData[i].licenseName.replace(/ /gi,"");
								}
								var param = {"dataString":JSON.stringify(postData), "groupId":groupId};
							
							    $.ajax({
									url : '/oss/setSessionAnalysisResultData',
									dataType : 'json',
									type:'POST',
									cache : false,
									data : JSON.stringify(param),
									contentType : 'application/json',
									success : function(resultData){
										if(resultData){
											_popupAnalysisDetailData = window.open("/oss/getAnalysisResultDetail/"+groupId, "OSS Auto Analysis Result Detail", "width=1550, height=814, toolbar=no, location=no, resizable=yes, scrollbars=yes");

											if(!_popupAnalysisDetailData || _popupAnalysisDetailData.closed || typeof _popupAnalysisDetailData.closed=='undefined') {
												alertify.alert('<spring:message code="msg.common.window.allowpopup" />');
											}
										} else {
											alertify.error('<spring:message code="msg.common.valid2" />', 0);
										}
	
										if(ossValidMsg) {
											gridValidMsgNew(ossValidMsg, "ossList");
										}
										
										if(ossDiffMsg){
											gridDiffMsg(ossDiffMsg, "ossList");
										}
										
										cleanErrMsg("ossList", lastSelection);
										
										$("#ossList").jqGrid('editRow',lastSelection, {keys: true} );
									},
									error : function(){
										alertify.error('<spring:message code="msg.common.valid2" />', 0);
									}
								});
							}
						}
					},
					postData: Ctrl_fn.makeParam()
				});
			},
			loadAnalysisListGrid : function(){
				$('#loading_wrap_popup').show();

				$.ajax({
					url : '/oss/getAutoAnalysisList',
					dataType : 'json',
					cache : false,
					data : Ctrl_fn.makeParam(),
					contentType : 'application/json',
					success : function(resultData){
						ossValidMsg = resultData.validMap;
						ossDiffMsg = resultData.diffMap;
						
						$('#ossList').jqGrid({
								datatype: 'local',
								data : resultData.rows,
								colNames: ['ID', 'project ID', 'OSS Name', 'Oss Version', 'License', 'Download Location', 'Home Page'],
								colModel: [
									{name: 'componentId', index: 'componentId', hidden:true, key:true},
									{name: 'prjId', index: 'prjId', width: 50, align: 'left', hidden: true},
									{name: 'ossName', index: 'ossName', width: 50, align: 'left', editable: true},
									{name: 'ossVersion', index: 'ossVersion', width: 30, align: 'left', editable: true},
									{name: 'licenseName', index: 'licenseName', width: 100, align: 'left', editable: true},
									{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', editable: true, 
										editoptions: {
											dataInit: function (e) {
												$(e).on("blur", function() {
													var value = e.value;
													
													if(value.charAt(value.length-1) == "/"){
														value = value.slice(0, -1); // 마지막 문자열 제거
														$("#"+e.id).val(value);
													}
												});
											}
										}
									},
									{name: 'homepage', index: 'homepage', width: 100, align: 'left', editable: true, 
										editoptions: {
											dataInit: function (e) { 
												$(e).on("blur", function() {
													var value = e.value;
													
													if(value.charAt(value.length-1) == "/"){
														value = value.slice(0, -1); // 마지막 문자열 제거
														$("#"+e.id).val(value);
													}
												});
											}
										}
									}
								],
								autoencode: true,
								gridview: true,
								editurl:'clientArray',
								loadonce: true,
								autowidth: true,
								height: 'auto',
								rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
								multiselect: true,
								loadComplete: function(data){
									$('#loading_wrap_popup').hide();
									
									if(data.records > 0){
										$("#btnAnalysis").css("display", "block");

										data.rows.forEach(function(cur,idx){
										    if(cur.analysisYn == "Y"){
										        $("#jqg_ossList_"+cur.gridId).attr("checked", true);
										    }
										});
									}

									if(ossValidMsg) {
										gridValidMsgNew(ossValidMsg, "ossList");
									}
									
									if(ossDiffMsg){
										gridDiffMsg(ossDiffMsg, "ossList");
									}
								},
								onSelectRow: function(rowid){
					               if (rowid) {
					                    var grid = $("#ossList");

					                    if(lastSelection != undefined) {
						    				cleanErrMsg("ossList", lastSelection);
						                    grid.jqGrid('saveRow',lastSelection);
						                    gridValidMsgRowId(ossValidMsg, "ossList", lastSelection);
						                    gridDiffMsgRowId(ossDiffMsg, "ossList", lastSelection);
					                    }
					                  
					    				cleanErrMsg("ossList", rowid);
					                    grid.jqGrid('editRow',rowid, {keys: true} );
					                    
					                    lastSelection = rowid;
					                }
					                
			                    	// 이미 처리완료된 경우 체크박스의 값을 초기화한다.
			                    	if($("input:checkbox[id='jqg_ossList_"+rowid+"']").attr( 'disabled') == "disabled") {
				                    	$("input:checkbox[id='jqg_ossList_"+rowid+"']").attr( 'checked', false );
			                    	}
								},
								gridComplete:function (){}
						});
						
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});	
			},
			editAnalysis:function(){
				$('#loading_wrap_popup').show();
				var params = new Array();
				var idArry = $("#ossList").jqGrid ('getGridParam', 'selarrrow')
				.reduce(function(arr, item) {
					if($("input:checkbox[id='jqg_ossList_"+item+"']").is(":checked")){
						arr.push(item);
					}
					
					return arr;
				}, []); // checkbox를 선택한 행만 가져옴.

				var result = [];
				
				// check 여부 
				var hasChecked = false;
				var gridObj = $("#ossList");
				var gridStr = "ossList";
				
				window.setTimeout(function(){
					for (var i = 0; i < idArry.length; i++) { //row id수만큼 실행
				    	var rowId = idArry[i];
				    	cleanErrMsg("ossList", rowId);
				    	gridObj.jqGrid('saveRow',rowId);
					    var rowdata = gridObj.getRowData(rowId); // 해당 id의 row 데이터를 가져옴
					    
					    hasChecked = true;
					    
						$.ajax({
							url : "/oss/saveOssAnalysisList/popup", 
							type : 'POST',
							dataType : 'json',
							cache : false,
							data : JSON.stringify(rowdata),
							contentType : 'application/json',
							success : function(resultData){
								result.push(resultData);

								if(result.length == idArry.length){
									$('#loading_wrap_popup').hide();
									Ctrl_fn.startAnalysis();
								}
							},
							error : function(){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					}

					if(!hasChecked) {
						alertify.alert('Please select OSS to register');
						$('#loading_wrap_popup').hide();
					}
				}, 0);
			},
			startAnalysis : function(){
				$('#loading_wrap_popup').show();
				$.ajax({
					url : "/oss/startAnalysis", 
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : JSON.stringify({prjId : '${projectInfo.prjId}'}),
					contentType : 'application/json',
					success : function(resultData){
						$('#loading_wrap_popup').hide();
						
						if(""+resultData.isValid == "true") {
						    alertify.success(resultData.returnMsg);
						    opener.ossAnalysisStatus = resultData.prjInfo.ossAnalysisStatus;
						    opener.analysisStartDate = resultData.prjInfo.analysisStartDate;
						    opener.$(".idenAnalysisResult").hide();
						    $("#btnAnalysis").hide();

						    window.close();
						} else {
							alertify.error(resultData.returnMsg);
							$("#btnAnalysis").show();
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			makeParam : function(){
				var obj = {
					prjId : '${projectInfo.prjId}'
				};

				if(ossAnalysisStatus.toUpperCase() == "RESULT"){
					obj["startAnalysisFlag"] = "Y";
				}

				return obj;
			},
			setDuplicateUnCheck : function(target){
				var name = $(target).attr("name");
				var isChecked = $(target).prop("checked");
				
				$("[name='"+name+"']").attr("checked", false); // 동일한 group Id를 가진 row는 1건 초과하여 check를 할 수 없음.
				
				if(isChecked){
					$(target).attr("checked", true);
				}
			},
			registOss : function(){
				$("#loading_wrap_popup").show();
				var checkedRows = $("#ossList").getRowData().reduce(function(arr, item){
					if(item["checkbox"].indexOf("checked") > -1){
				       arr.push(item);
				    }

				    return arr;
				}, []);

				// 완료시 쌓는 data
				var result = [];

				// check 여부
				var hasChecked = false;
				var gridObj = $("#ossList");
				var gridStr = "ossList";

				window.setTimeout(function(){
					for (var i = 0; i < checkedRows.length; i++) { //row id수만큼 실행
					    	var rowId = checkedRows[i].gridId;
					    	var groupId = checkedRows[i].groupId;
					    	
					    	cleanErrMsg("ossList", rowId);
					    	gridObj.jqGrid('saveRow',rowId);
						    var rowdata = $("#ossList").getRowData(rowId); // 해당 id의 row 데이터를 가져옴

						    hasChecked = true;
							
						    $("#ossList").getRowData()
						    	.filter(function(cur){ return cur.groupId == groupId})
						    	.forEach(function(cur){
							        $("#"+cur.gridId).removeClass("excludeRow");
							    }
							);
							
						    $.ajax({
								url : '/oss/saveOssAnalysisData',
								type : 'POST',
								data : JSON.stringify(rowdata),
								dataType : 'json',
								cache : false,
								async: false,
								contentType : 'application/json',
								success: function(resultData){
									result.push(resultData);
									if(""+resultData.isValid == "true"){
										$('#ossList').getRowData().filter(function(cur){
						        			return cur.groupId == groupId;
							        	}).forEach(function(cur){
								        	var gridId = cur.gridId;
					        			    $('#ossList').jqGrid('setCell', gridId, 'result', "Success");
					        			    $("#"+gridId).addClass("excludeRow");
					        			    $("#"+gridId + " > td > [type='checkbox']").attr({"disabled": true, "checked": false});
					        			});
									}else {
										try {
											$('#ossList').jqGrid('setCell', rowId, 'result', resultData.validMsg);
					        			    $("#"+rowId + " > td > [type='checkbox']").attr("checked", false);
											
					        			    if(resultData.validMsg.toUpperCase() == "FAIL"){
						        			    var resultValidMap = resultData.resultData.validMapResult;
						        			    var resultDiffMap = resultData.resultData.diffMapResult;

						        			    if(resultValidMap) {
						        			    	$.each(resultValidMap,function(key,value) {
						        			    		if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key && "externalData3" != key) {
							        			    		var errRow = $("#"+rowId+" > td[aria-describedby='ossList_" + key+"']");

							        			    		if(errRow) {
							        			    			errRow.append('<div class=\"ossList_'+rowId+' retxt\">'+ value +'</div>');
							        			    		}
						        			    		}
						        			    	});
						        			    }

						        			    if(resultDiffMap){
						        			    	$.each(resultDiffMap,function(key,value) {
						        			    		if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key && "externalData3" != key) {
						        			    			var diffRow = $("#"+rowId+" > td[aria-describedby='ossList_" + key+"']");
						        			    			
						        			    			if(diffRow) {
						        			    				diffRow.append('<div class=\"ossList_'+rowId+' retxtb\">'+ value +'</div>');
						        			    			}
						        			    		}
						        			    	});
						        			    }
						        			}
										} catch(e) {
											$('#ossList').jqGrid('setCell', rowId, 'result', "Failed");
					        			    $("#"+rowId).addClass("excludeRow");
					        			    $("#"+rowId + " > td > [type='checkbox']").attr({"disabled": true, "checked": false});
					        			    
											alertify.error(e);
										}
									}

									if(result.length == checkedRows.length){
										$('#loading_wrap_popup').hide();
									}
								},
								error: function(resultData){}
							});
					}
					
					if(!hasChecked) {
						alertify.alert('Please select OSS to register');

						$('#loading_wrap_popup').hide();
					}
				}, 0);
			}
		};
		</script>
	</head>
	<body>
		<div id="loading_wrap_popup" class="loading" style="display:none;">
			<div class="loadingBlind"></div>
			<img src="/images/loading.gif" alt="loading" />
		</div>
		<div id="wrap" style="padding-top: 15px;">
			<div  align="center" >
				<div class="jqGridSet" style="overflow: auto; width: 97%; height: 730px;">
					<table id="ossList"><tr><td></td></tr></table>
					<c:if test="${project.ossAnalysisStatus eq 'result'}">
						<div id="pager"></div>
					</c:if>
				</div>
			</div>
			<div align="left" style="padding-left: 13px;">
				<input type="button" value="start Analysis" id="btnAnalysis" onclick="Ctrl_fn.editAnalysis()" class="btnColor red" style="display: none;width:90px;margin:10px;" />
				<input type="button" value="save" id="btnRegist" onclick="Ctrl_fn.registOss()" class="btnColor red" style="display: none;width:90px;margin:10px;" />
			</div>
		</div>
	</body>
</html>