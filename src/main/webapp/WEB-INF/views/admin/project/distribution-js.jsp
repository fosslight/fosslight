<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var isMerge=false;
	var isDistribute=true;
	var existsFile=false; // distribute file이 존재하는 경우
	var existsFileNotify=false;
	var existsFileSource=false;
	var isNewDesc=true;
	var modelData;
	var regModelData;
	var modelDeleteData;
	var userRole = '${sessUserInfo.authority}';
	var distributionStatus = '${project.destributionStatus}';
	var completeYn = "${project.completeYn}";
	var isStatusDone = "${ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_DEPLOIDED') eq project.destributionStatus}" == "true";
	var distributeDeployTime = "${project.distributeDeployTime}";
	var descKey = "";
	
	var hasPackageFile2 = false;
	var hasPackageFile3 = false;
	var toDay = new Date().toISOString().replace(/^(\d{4})-(\d{2})-(\d{2}).+/g, "$1$2$3");
	var gDateKeyup = false;
	var currentModelCategoryValues = '';
	var needVerifyFlag = false;
	var withoutVerifyYn = "${project.withoutVerifyYn}";
	var distributeNameChangeFlag = false;
	var swModelChangeFlag = false;
	var modelOnlyChangeFlag = false;
	var releaseDateArr = [];
	
	$(document).ready(function () {
		if(distributionStatus == "PROC"){
			loading.show();
			
			alertify.alert('<spring:message code="msg.project.distribution.loading" />', function(){
				deleteTabInFrame('#/project/edit/'+'${project.prjId}');
			});
		}
		
		evt.init();
		fn_data.init();
		initSample();
		showHelpLink("Project_List_Distribution");
		
		$('.btnCommentHistory').on('click', function(e){
			e.preventDefault();
			
			openCommentHistory('<c:url value="/comment/popup/prj/${project.prjId}"/>');
		});
	});
	
	
	// 이벤트
	var evt = {
		init : function(){
			//Model Reset
			var site = $('input[name=distributeTarget]:checked').val();
			var siteCd = ${ct:getAllValuesJson(ct:getConstDef('CD_DISTRIBUTE_CODE'))};
			var categoryCd = '';
			
			switch(site){
				case siteCd[0].cdDtlNo:	categoryCd="${ct:getConstDef('CD_MODEL_TYPE')}";	break;
				case siteCd[1].cdDtlNo:	categoryCd="${ct:getConstDef('CD_MODEL_TYPE2')}";	break;
			}
			
			getCategoryCode(categoryCd);
			getCategoryCodeJson(categoryCd);
			
			// 그리드 클리어
			$("#allDelete").click(function(){
				cleanErrMsg("_modelList");
				var ids = $("#_modelList").jqGrid('getDataIDs');
				
				ids.forEach(function(id){
					jQuery('#_modelList').jqGrid('saveRow',id,false);
				});
				
				for(var idx in ids){
					fn.exeDelete(ids[idx]);
				}
				
				$("#_modelList").jqGrid('clearGridData');
			});
			
			// 저장
			$("#save").click(function(){
				if(needVerifyFlag){
					alertify.alert('<spring:message code="msg.project.need.verify" />', function(){});
					return false;
				}
				
				cleanErrMsg("_modelList");
				var ids = jQuery('#_modelList').jqGrid('getDataIDs');
				
				ids.forEach(function(id){
					jQuery('#_modelList').jqGrid('saveRow',id,false);
					releaseDateArr.push(jQuery('#_modelList').jqGrid('getCell',ids[element],'releaseDate'));
				});
				
				saveSubmit();
			});
			
			// 체크
			$("#check").click(function(){
				cleanErrMsg("_modelList");
				var ids = jQuery('#_modelList').jqGrid('getDataIDs');
				
				ids.forEach(function(id){
					jQuery('#_modelList').jqGrid('saveRow',id,false);
				});
				
				saveSubmit();
			});
			
			// 삭제
			$("#delete").click(function(){
				var doDelete = confirm('<spring:message code="msg.project.warn.delete" />');

				if(doDelete){
					deleteSubmit();
				}
			});
			
			$('#resetDistribution').on('click', function(){
				var hasDistribution = '${not empty project.distributeOsdKey}';
				if(hasDistribution == "true") {
					if(!alertify.distributionDeleteConfirm){
						alertify.dialog('distributionDeleteConfirm', function() {
							var settings;
							
							return {
								setup: function() {
									var settings = alertify.confirm().settings;
									
									for (var prop in settings) {
										this.settings[prop] = settings[prop];
									}
									
									var setup = alertify.confirm().setup();
									
									setup.buttons.push({ 
										text: 'Delete With OSDD',scope:'auxiliary',className:'ajs-warning'
									});
									
									setup.focus.element = 1;
									
									return setup;
								},
								settings: {
									oncontinue: null
								},
								callback: function(closeEvent) {
									if (closeEvent.index == 2) {
										fn.rejectDistributionWithOSDD();
									} else {
										alertify.confirm().callback.call(this, closeEvent);
									}
								}
							};
						}, false, 'confirm');
					}
					
					alertify.distributionDeleteConfirm('<spring:message code="msg.distribute.confirm.reset" />',function(){
						// FOSSLight System만 삭제
						fn.rejectDistribution();
					}).set('labels', {ok:'Delete FOSSLight Only'}); 
				} else {
 			 		alertify.confirm('<spring:message code="msg.distribute.confirm.reset.warn" />', function(){ fn.rejectDistribution() });
				}
			});
			
			var code = '';
			<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
				<c:if test="${file eq '11'}">
					code = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
				</c:if>
			</c:forEach>
			
			$('#modelFile').uploadFile({
				url : '/project/modelFile',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				sequential:true,
				allowedTypes:code,
				sequentialCount:1,
				dynamicFormData: function() {
					var data ={"prjId" : "${project.prjId}", "distributionTarget" :$('input[name=distributeTarget]:checked').val()}

					return data;
				},
				onSuccess:function(files,data,xhr,pd) {
					if(data != null) {
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						
						fn.makeModelList(data);	
					} else {
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					}
				}
			});
			
			$("#identificationTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Identify");

				if(idx != "") {
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Identify', '#/project/identification/'+prjId+'/4');
				}
			});
			$("#packagingTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Packaging");
				
				if(idx != "") {
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Packaging', '#/project/verification/'+prjId);
				}
			});
			$("#distributionTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Distribute");
				
				if(idx != "") {
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Distribute', '#/project/distribution/'+prjId);
				}
			});
			$("#editTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Project");
				
				if(idx != "") {
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Project', '#/project/edit/'+prjId);
				}
			});

			$('#distributeName').on("blur", function(){
				var beforeDistributeName = "${project.distributeName}";
				var distributionStatus = $("[name='destributionStatus']").val();
				var distributeName = $(this).val();

				if(distributionStatus.toUpperCase() == 'DONE') {
					if(beforeDistributeName != distributeName) {
						$(".smallDelete").hide();
						$("[name='beforeDistributeName']").val(beforeDistributeName);
						$("[name='distributeName']").val(distributeName);
						
						distributeNameChangeFlag = true;
					} else {
						$(".smallDelete").show();
						$("[name='beforeDistributeName']").val("");
						
						distributeNameChangeFlag = false;
					}
				} else {
					$("[name='distributeName']").val(distributeName);
				}
			});

			$("#distributeSoftwareType").on("change", function(){
				var beforeDistributeSoftwareType = "${project.distributeSoftwareType}";
				var distributionStatus = $("[name='destributionStatus']").val();
				var distributeSoftwareType = $(this).val();
				
				if(distributionStatus.toUpperCase() == 'DONE'){
					if(beforeDistributeSoftwareType != distributeSoftwareType){
						$(".smallDelete").hide();
						$("[name='beforeDistributeSoftwareType']").val(beforeDistributeSoftwareType);
						$("[name='distributeSoftwareType']").val(distributeSoftwareType);
						
						swModelChangeFlag = true;
					} else {
						$(".smallDelete").show();
						$("[name='beforeDistributeSoftwareType']").val("");
						
						swModelChangeFlag = false;
					}
				} else {
					$("[name='distributeSoftwareType']").val(distributeSoftwareType);
				}
			});
		}
	}
	
	var fn = {
			// 모델 데이터 생성
			getModelGridRows : function(elementId){
				var ids = $(elementId).jqGrid('getDataIDs');
				var rows = [];
				
				ids.forEach(function(id){
					$(elementId).jqGrid('saveRow', id);
					var obj = $(elementId).jqGrid('getRowData',id);

					rows.push(obj);
				});
				
				return rows;
			},
			// 그리드 삭제 버튼
			setDelBtn : function(cellvalue, options, rowObject){
				return "<input type=\"button\" value=\"delete\" class=\"btnCLight darkgray\" onclick=\"fn.exeDelete('"+options.rowId+"')\" />";
			},
			// 그리드 삭제
			exeDelete : function(rowId){
				// _modelDeleteList 로 이동
				// 이미 list에 존재하는 경우 무시
				cleanErrMsg("_modelList", rowId);
				$("#_modelList").jqGrid('saveRow',rowId,false);
				var _delData = $("#_modelList").jqGrid('getRowData',rowId);
				
				if(!fn.checkExistsModelData("#_modelDeleteList", _delData)) {
					var _tempRandId = $.jgrid.randId();
					_delData.gridId = _tempRandId;
					
					$('#_modelDeleteList').jqGrid('addRowData', _tempRandId, _delData, "last");
				}
				
				$("#_modelList").jqGrid('delRowData', rowId);
			},
			// 그리드 삭제 버튼
			setAddBtn : function(cellvalue, options, rowObject){
				return "<input type=\"button\" value=\"Add\" class=\"btnCLight darkgray\" onclick=\"fn.exeAdd('"+options.rowId+"')\" />";
			},
			// model 삭제 취소
			exeAdd : function(rowId){
				// _modelList 로 이동
				// 이미 list에 존재하는 경우 무시
				cleanErrMsg("_modelDeleteList", rowId);
				var _addData = $("#_modelDeleteList").jqGrid('getRowData',rowId);
				
				if(!fn.checkExistsModelData("#_modelList", _addData)) {
					var _tempRandId = $.jgrid.randId();
					_addData.gridId = _tempRandId;
					
					$('#_modelList').jqGrid('addRowData', _tempRandId, _addData, "last");
				}
				
				$("#_modelDeleteList").jqGrid('delRowData', rowId);
			},
			checkExistsModelData : function(elementId, data) {
				if(!data.modelName || data.modelName == "" || !data.releaseDate || data.releaseDate == "") {
					return true;
				}
				
				var target = $(elementId);
				var ids = target.jqGrid('getDataIDs');
				var _diffStr = data.category + "+" + data.modelName;
				var hasModel = false;
				
				for(var idx in ids){
					var ctgr = target.jqGrid('getCell',ids[idx],'category');
					var md = target.jqGrid('getCell',ids[idx],'modelName');
					var diffStr = ctgr + "+" + md;

					if(_diffStr == diffStr) {
						hasModel = true;
					}
				}
				
				// 삭제인경우 DB에 등록되어 있는 DATA가 아닌경우 row 삭제만 한다.
				if(!hasModel && "#_modelDeleteList" == elementId) {
					if(!modelData || modelData.length == 0) {
						hasModel = true;
					} else {
						var hasOrgModel = false;
						
						for(var idx in modelData) {
							var ctgr = modelData[idx].category;
							var md = modelData[idx].modelName;
							var osddYn = modelData[idx].osddSyncYn;
							var diffStr = ctgr + "+" + md;
							
							if("Y" == osddYn && _diffStr == diffStr) {
								hasOrgModel = true;
							}
						}
						
						if(!hasOrgModel) {
							hasModel = true;
						}
					}
				}
				return hasModel;
			},
			makeModelList : function(data){
				modelData = data.currentModelList;
				modelDeleteData = data.delModelList;

				// 리로드 대신 그리드 삭제 후 다시 그리기
				$("#_modelList").jqGrid('GridUnload');
				$("#_modelDeleteList").jqGrid('GridUnload');

				modelList_grid.load();
			},
			downloadModelList : function(){
				cleanErrMsg("_modelList");
				var data = fn.getModelGridRows('#_modelList');
				
				$.ajax({
					type: "POST",
					url: '/exceldownload/getExcelPost',
					data: JSON.stringify({"type":"model", "parameter":JSON.stringify(data), "extParam" : $('input[name=distributeTarget]:checked').val()}),
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
			rejectDistribution : function() {
					$("#userComment").val(CKEDITOR.instances.editor.getData());
					
					$("#distributionForm").ajaxForm({
 						url : '/project/distribution/distribute/reset',
 						type : 'POST',
 						dataType: "json",
 						cache : false,
 						success: onDistributeSuccess,
 						error : onError
 					}).submit();
			},
			rejectDistributionWithOSDD : function() {
				$("#userComment").val(CKEDITOR.instances.editor.getData());
				
				$("#distributionForm").ajaxForm({
						url : '/project/distribution/distribute/resetWithOSDD',
						type : 'POST',
						dataType: "json",
						cache : false,
						success: onDistributeSuccess,
						error : onError
					}).submit();
			},
			distributeModelOnly : function() {
				if(alertify.distributionCheckResult){
					alertify.distributionCheckResult().close();
				}
				
				if(!checkSetModelInfo()) {
					return false;
				}
				
				// model only의 경우 batch job으로 수행하지 않고 실시간 연동한다.
				$("#distributionForm").ajaxForm({
					url : '/project/distribution/distribute/immediatelyOnly',
					type : 'POST',
					dataType: "json",
					cache : false,
					success: onDistributeSuccess,
					error : onError
				}).submit();
			},
			distributeAll : function() {
				if(alertify.distributionCheckResult){
					alertify.distributionCheckResult().close();
				}
				
				if(!checkSetModelInfo()) {
					return false;
				}
				
				if(!isDistribute){
					alertify.error('<spring:message code="msg.project.required.authorization" />', 0);
					
					return;
				}
				
				if(((distributeDeployTime != null && distributeDeployTime != '')
						|| (descKey != null && descKey != ''))
					&& (distributeNameChangeFlag || swModelChangeFlag || modelOnlyChangeFlag)){ // description or swModelFlag or model info 변경일 경우 popup을 띄우지 않고 distribute를 진행한다.
					distribute();
				} else {
					if(existsFile) {
						var _existsFiles = "";
						if(existsFileNotify) {
							_existsFiles = "OSS Notice";
						}
						
						if(existsFileSource) {
							if(_existsFiles != "") {
								_existsFiles += ", ";
							}
							
							_existsFiles += "OSS Package";
						}

						var confirmMessage = '<spring:message code="msg.distribute.confrim.overwritefile" arguments="'+_existsFiles+'" htmlEscape="false" argumentSeparator=";"/>';
				 		alertify.confirm(confirmMessage, function () { distribute(); });
					} else {
						distribute();
					}
				}
			},
			cancelDistributeReserve : function () {
				if(alertify.distributionCheckResult){
					alertify.distributionCheckResult().close();
				}
				
				//batch 취소
				$("#distributionForm").ajaxForm({
					url : '/project/distribution/distribute/cancel',
					type : 'POST',
					dataType: "json",
					cache : false,
					success: onDistributeSuccess,
					error : onError
				}).submit();
			},
			sendEditor : function(type){
				//코멘트 저장
				var editorVal = CKEDITOR.instances.editor.getData();
				if(!editorVal || editorVal == "") {
					alertify.alert("<spring:message code="msg.project.enter.comment" />", function(){});
					
					return false;
				}
				
				var param = {referenceId : '${project.prjId}', referenceDiv :'14', contents : editorVal, mailSendType : type};
				
				$.ajax({
					url : '/project/sendComment',
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : param,
					success : function(json){
						if(json.isValid == 'false'){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							$('.ajs-close').trigger("click");
							alertify.success('<spring:message code="msg.common.success" />');
							resetEditor(CKEDITOR.instances.editor);
							
							$(".commentBtn open").trigger( "click" );
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			saveEditor : function(){
				//코멘트 임시저장
				var editorVal = CKEDITOR.instances.editor.getData();
				var register = '${sessUserInfo.userId}';
				var param = {referenceId : '${project.prjId}', referenceDiv :'15', contents : editorVal};
				
				$.ajax({
					url : '/project/saveComment',
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : param,
					success : function(json){
						if(json.isValid == 'false'){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							alertify.success('<spring:message code="msg.common.success" />');
							
							$(".commentBtn.open").trigger( "click" );
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			editorDialog : function(){
				var editorVal = CKEDITOR.instances.editor.getData();
				
				if(!editorVal || editorVal == "") {
					alertify.alert("<spring:message code="msg.project.enter.comment" />", function(){});
					
					return false;
				}
				
				//launch it.
				var btnHtm = '<br/><b>Send an email to</b><br/>';
				btnHtm += '<input type="button" value="Watcher Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'W\')"/>&nbsp;&nbsp;&nbsp;';

				if(userRole == "ROLE_ADMIN"){
					btnHtm += '<input type="button" value="Creator Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'C\')"/>&nbsp;&nbsp;&nbsp;';
				}else{
					btnHtm += '<input type="button" value="Reviewer Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'R\')"/>&nbsp;&nbsp;&nbsp;';
				}
				
				btnHtm +='<input type="button" value="All" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="fn.sendEditor(\'WR\')"/>&nbsp;&nbsp;&nbsp;';

				if(!alertify.myAlert){
					//define a new dialog
					alertify.dialog('myAlert',function factory(){
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
				
				alertify.myAlert(btnHtm);
			},
			// release date check function add
			releaseDateCheck : function() {
				var releaseDateCheckMessage = "'<spring:message code="msg.project.confirm.release" />'";
				
				alertify.confirm(releaseDateCheckMessage).set('onok', function(closeEvent){
						availableCheck('save');
					}
				);

				$(".ajs-button").css("font-size", "12").css("cursor", "pointer");
			}

	}
	

	var fn_data = {	
		distribution:${empty distribution ? '{}':distribution},
		init : function(){
			$('#distributeName').val(fn_data.distribution.distributeName);
			
			var _distributeTargetSelectedVal = "";
			
			$('input[name=distributeTarget]').mouseup(function(){
				_distributeTargetSelectedVal = $('input[name=distributeTarget]:checked').val();
			});
			
			$('input[name=distributeTarget]').on('change',function(e){
				if($("#_modelList").jqGrid('getGridParam', 'records') > 0 || $("#_modelDeleteList").jqGrid('getGridParam', 'records') > 0){
					alertify.confirm('<spring:message code="msg.project.confirm.modelinfo.init" />', 
						function(){
							modelList_grid.changeTarget();
						}, 
						function(){
							$('input[name=distributeTarget]:input[value=' + _distributeTargetSelectedVal + ']').attr("checked", true);
						});
				} else {
					modelList_grid.changeTarget();
				}
			});
			
			if(fn_data.distribution.noticeFileInfo != undefined) {
				var noticeFile = fn_data.distribution.noticeFileInfo;
				var _encUrl = "/download/"+noticeFile.fileSeq+"/"+noticeFile.logiNm;
				
				$('.licenseFile').append($('<a href="'+_encUrl+'" class="urlLink">'+noticeFile.origNm+'</a>'));
				$('input[name=licenseFileName]').val(noticeFile.origNm);
			}
			
			var packageInfo =  isStatusDone ? '<ul>' : '<ul class="left">';
					packageInfo += '<div id="packagingFile1">';
						packageInfo += '<li>';
							packageInfo += '<span>';
			
			if(fn_data.distribution.packageFileInfo) {
				var packageFile = fn_data.distribution.packageFileInfo;
								packageInfo += '<a href="/download/'+packageFile.fileSeq+'/'+packageFile.logiNm+'" class="urlLink left">'+packageFile.origNm+'</a>';

							if(isStatusDone){
								packageInfo += '&nbsp;<input type="button" value="Delete" class="smallDelete" onclick="package_fn.uploadPackagingFile(1)">';
							}
							
				$('input[name=openSourceFileName]').val(packageFile.origNm);
			}
			
							packageInfo += '</span>';
						packageInfo += '</li>';
					packageInfo += '</div>';
					packageInfo += '<div id="uploadFile1"></div>';
					
					packageInfo += '<div id="packagingFile2">';
						packageInfo +='<li>';
							packageInfo += '<span>';
							
			if(fn_data.distribution.packageFileInfo2) {
				var packageFile = fn_data.distribution.packageFileInfo2;				
								packageInfo += '<a href="/download/'+packageFile.fileSeq+'/'+packageFile.logiNm+'" class="urlLink left">'+packageFile.origNm+'</a>';
							
							if(isStatusDone){
								packageInfo += '&nbsp;<input type="button" value="Delete" class="smallDelete" onclick="package_fn.uploadPackagingFile(2)">';
							}
							
				$('input[name=openSourceFileName2]').val(packageFile.origNm);
				
				hasPackageFile2 = true;
			}
			
							packageInfo += '</span>';
						packageInfo +='</li>';
					packageInfo += '</div>';
					packageInfo += '<div id="uploadFile2"></div>';

					packageInfo += '<div id="packagingFile3">';
						packageInfo +='<li>';
							packageInfo += '<span>';
			
			if(fn_data.distribution.packageFileInfo3) {
				var packageFile = fn_data.distribution.packageFileInfo3;
								packageInfo += '<a href="/download/'+packageFile.fileSeq+'/'+packageFile.logiNm+'" class="urlLink left">'+packageFile.origNm+'</a>';
							
							if(isStatusDone){
								packageInfo += '&nbsp;<input type="button" value="Delete" class="smallDelete" onclick="package_fn.uploadPackagingFile(3)">';
							}
							
				$('input[name=openSourceFileName3]').val(packageFile.origNm);
				
				hasPackageFile3 = true;
			}
			
							packageInfo += '</span>'; 
						packageInfo += '</li>'; 
					packageInfo += '</div>'; 
					packageInfo += '<div id="uploadFile3"></div>'; 
				packageInfo += '</ul>';
			
			if(isStatusDone){
				packageInfo += '<span class="right"><input type="button" id="btnVerify" value="Start to verify" onclick="package_fn.verify()" class="btnColor red wauto" style="display:none;"></span>';
			}
			
			$('.opensourceFile').append(packageInfo);

			fn_data.getModelGridData();		//jqGrid 목록 로드
		},
		getModelGridData : function(param){
			$.ajax({
				url:"/project/modellistAjax",
				dataType : 'json',
				cache : false,
				data : (param) ? param : {prjId : $('input[name=prjId]').val()},
				contentType : 'application/json',
				success : function(data){
					modelData = data.currentModelList;
					modelDeleteData = data.delModelList;

					// 리로드 대신 그리드 삭제 후 다시 그리기
					$("#_modelList").jqGrid('GridUnload');
					modelList_grid.load();
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	}
	
	// 모델 그리드
	var modelList_grid = {
		load : function(){
			var modelList = $("#_modelList");
			
			modelList.jqGrid({
				datatype: 'local',
				data : modelData,
				colNames: ['gridId', 'Category', 'Model', 'Release Date', 'Last Modified', 'Updated Date', 'Delete', 'osddSyncYn'],
				colModel: [
					{name: 'gridId', index: 'gridId', key:true, hidden:true},
					{name: 'category', index: 'category', align: 'left', width:230, editable: true, formatter: 'select', edittype:"select",editoptions:{value:currentModelCategoryValues}},
					{name: 'modelName', index: 'modelName',align: 'left', width:150, editable: true,
						editoptions:{maxlength:100, dataEvents:[
                        	{
                        		type: 'blur',
	                            fn: function(e) {
	                            	var key = e.charCode || e.keyCode; // to support all browsers

	                            	$(this).val($(this).val().toUpperCase());
                                }
                           },
                           {  type: 'change',
                               fn: function(e) {
								   //something if value changed
                            	   $(this).val($(this).val().toUpperCase());
                               }
                           }
						]}
					},
					{name: 'releaseDate', index: 'releaseDate', align: 'center', width:100, editable: true, editoptions:{value:toDay}/*, editoptions:{readonly:true}*/, sorttype:'date',
						editoptions:{maxlength:8, dataEvents:[
							{
								type: 'keyup',
								fn: function(e) {
									var result = $(this).val();
									gDateKeyup = true;
									
									if(/\d+/.test(result)) {
										result = result.match(/\d+/g).join("");
										
										$(this).val(result);
									} else {
										$(this).val("");
									}
								}
							},
                            {  type: 'blur',
                               fn: function(e) {
	                               var date_pattern = /^(19|20)\d{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[0-1])$/; 
	                               var result = $(this).val();
	                               
	                               if(!date_pattern.test(result) && gDateKeyup) {
	                            	   alertify.error('<spring:message code="msg.common.valid" />', 0);
	                            	   $(this).val("");
	                               } else {
	                            	   gDateKeyup = false;
	                               }
                               }
                            }
						]}
   					},
					{name: 'modifier', index: 'modifier', align: 'center', width:80, editable:false},
					{name: 'modifiedDate', index: 'modifiedDate', align: 'center', width:80, editable:false, sorttype:'date'},
					{name: 'delete', index: 'delete', width:80, align: 'center', sortable:false, formatter: fn.setDelBtn},
					{name: 'osddSyncYn', index: 'osddSyncYn', width:80, align: 'center', hidden:true}
				],
				autoencode: true,
				editurl:'clientArray',
	 			autowidth: true,
				height: 'auto',
				gridview: true,
			   	pager: '#pagerModel',
			   	pgbuttons: false,
			   	pgtext: false,
			   	pginput:false,
			   	rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
				loadonce:true,
				cellEdit : true,
				cellsubmit : 'clientArray',
				ignoreCase: true,
				rownumbers: true,
			    onSortCol: function (index, columnIndex, sortOrder) {
			    	isSort = true;
			    },
				loadComplete:function(data) {
					lastsel=-1;
					hidePageNav('pagerModel');
					
					// 예약상태인 경우
					if("${ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_RESERVE')}" == "${project.destributionStatus}") {
						availableCheck('check');
					}
				},
				onSelectRow: function(rowid) {
				},
				onCellSelect: function(rowid,iCol,cellcontent,e) {
					if(rowid){
						cleanErrMsg("_modelList", rowid);
						$("#_modelList").jqGrid('editRow',rowid,true,pickdates);

						modelList_grid.modelGridListSetEditAble();

						lastsel=rowid;
					}
				},
				// 로우 더블클릭 시 편집모드
				ondblClickRow: function(rowid,iRow,iCol,e) {
					gridListBulkEdit("_modelList", pickdates);
					
					modelList_grid.modelGridListSetEditAble();
				}
			});
			
			$("#_modelList").jqGrid('navGrid',"#pagerModel",{edit:false,add:true,del:false,search:false,refresh:false
				, addfunc: function (rowid) {
					var _tempRandId = $.jgrid.randId();
					var lastCategory = "";
					var rows = fn.getModelGridRows('#_modelList');

					if(rows.length > 0){
						lastCategory = rows[rows.length-1].category;
					}else{
						lastCategory = "";
					}
					
					$("#_modelList").jqGrid("addRowData", _tempRandId, {gridId: _tempRandId, category: lastCategory}, "last");
                    $("#_modelList").jqGrid('editRow',_tempRandId,true,pickdates);
				}
			});

			// 사용자가 명시적으로 삭제처라한 model list
			var modelDeleteList = $("#_modelDeleteList");
			
			modelDeleteList.jqGrid({
				datatype: 'local',
				data : modelDeleteData,
				colNames: ['gridId', 'Category', 'Model', 'Release Date', 'Last Modified', 'Updated Date', 'Add', 'osddSyncYn'],
				colModel: [
					{name: 'gridId', index: 'gridId', key:true, hidden:true},
					{name: 'category', index: 'category', align: 'left', width:230, editable: true, formatter: 'select', edittype:"select",editoptions:{value:currentModelCategoryValues}},
					{name: 'modelName', index: 'modelName',align: 'left', width:150, editable: true},
					{name: 'releaseDate', index: 'releaseDate', align: 'center', width:100, editable: true, editoptions:{readonly:true}, sorttype:'date'},
					{name: 'modifier', index: 'modifier', align: 'center', width:80, editable:false},
					{name: 'modifiedDate', index: 'modifiedDate', align: 'center', width:80, editable:false, sorttype:'date'},
					{name: 'add', index: 'add', width:80, align: 'center', sortable:false, formatter: fn.setAddBtn},
					{name: 'osddSyncYn', index: 'osddSyncYn', hidden:true}
				],
				autoencode: true,
				editurl:'clientArray',
	 			autowidth: true,
				height: 'auto',
				gridview: true,
			   	pgbuttons: false,
			   	pgtext: false,
			   	pginput:false,
			   	rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
				loadonce:true,
				cellEdit : false,
				cellsubmit : 'clientArray',
				ignoreCase: true,
				rownumbers: true,
				caption : 'Model to be deleted',
			    onSortCol: function (index, columnIndex, sortOrder) {
			    	isSort = true;
			    },
				loadComplete:function(data) {},
				onSelectRow: function(rowid) {},
				onCellSelect: function(rowid,iCol,cellcontent,e) {},
				ondblClickRow: function(rowid,iRow,iCol,e) {} // 로우 더블클릭 시 편집모드
			});
		},
		changeTarget : function () {
			$("#_modelList").jqGrid('clearGridData');
			$("#_modelDeleteList").jqGrid('clearGridData');
			
			//Model Reset
			var site = $('input[name=distributeTarget]:checked').val();
			var siteCd = ${ct:getAllValuesJson(ct:getConstDef('CD_DISTRIBUTE_CODE'))};
			var categoryCd = '';
			
			switch(site){
				case siteCd[0].cdDtlNo:	categoryCd='${ct:getConstDef("CD_MODEL_TYPE")}';	break;
				case siteCd[1].cdDtlNo:	categoryCd='${ct:getConstDef("CD_MODEL_TYPE2")}';	break;
			}
			
			getCategoryCode(categoryCd);
			getCategoryCodeJson(categoryCd);
			
			$("#distributeMasterCategory").val('').trigger("change");
		},
		modelGridListSetEditAble : function(rowid) {
			var arr = [];
			var target = $("#_modelList");
	 		arr = target.jqGrid('getDataIDs');
	 		
			for(var i in arr){
				var distabled = target.jqGrid('getCell',arr[i],'osddSyncYn') == "Y";

				$("#_modelList #"+arr[i]+"_modelName").attr("disabled",distabled);
				$("#_modelList #"+arr[i]+"_category").attr("disabled",distabled);
			}
		}
	};
	
	//데이트피커
	function pickdates(id){
		jQuery("#"+id+"_releaseDate","#_modelList").datepicker({dateFormat:"yymmdd"});
	}
	
	// 카테고리 코드 가져오기
	function getCategoryCode(cd){
		return $.ajax({
			type: 'GET',
			data: {code:cd},
			async:false,
			url: "/project/getCategoryCode",
			success : function(json){
				if(json != null){
					$("#category").append(json);
					var htmlStr = '<option value=""></option>';
					var categorys = json.split(';');
					
					categorys.forEach(function(item){
						var cCd = item.split(':')[0];
						var cNm = item.split(':')[1];
						
						if(cCd&&cNm){
							htmlStr += '<option value="'+cCd+'">'+cNm+'</option>';
						}
					});
					
					//셀렉트 생성후 초기화
					$("#distributeMasterCategory").html(htmlStr).val(fn_data.distribution.distributeMasterCategory).trigger('change');
					
				}
			}
		});
	};	
	function getCategoryCodeJson(cd){
		return $.ajax({
			type: 'GET',
			data: {code:cd},
			async:false,
			dataType:'json',
			url: "/project/getCategoryCodeToJson",
			success : function(json){
				if(json != null){
					var str = '';
					
					$.each(json, function(key,value){
						var keyArr = key.split("|");
						str += keyArr[1]+':'+value+';';
					});
					
					str = str.substring(0, str.length-1);
					currentModelCategoryValues = str;
					
					$('#_modelList').jqGrid('setColProp','category',{editoptions:{value:str}}).trigger('reloadGrid');
					$('#_modelDeleteList').jqGrid('setColProp','category',{editoptions:{value:str}}).trigger('reloadGrid');
				}
			}
		});
	};	
	
	// 폼 저장
	function saveSubmit(){
		cleanErrMsg("_modelList");
		cleanErrMsg("_modelDeleteList");
		hideErrMsg();
		
		var rows = fn.getModelGridRows('#_modelList');
		var delRows = fn.getModelGridRows('#_modelDeleteList');
		
		$('input[name=prjModelJson]').val(JSON.stringify(rows));
		$('input[name=prjDeleteModelJson]').val(JSON.stringify(delRows));
		
		$("#distributionForm").ajaxForm({
			url : '/project/distribution/saveAjax',
			type : 'POST',
			dataType: "json",
			cache : false,
			success: onRegistSuccess,
			error : onError
		}).submit();
	};
	
	// 저장 성공시 후 처리
	function onRegistSuccess(json, status) {
		if(json.isValid == 'false') {
			alertify.error('<spring:message code="msg.common.valid" />', 0);
			
			createValidMsgComplex(json);
			
			gridValidMsgNew(json, "_modelList");
		} else if(json.isValid == 'true') {
			var chk = 0;
			releaseDateArr.forEach(function(item){
				if (item == ""){
					chk++;
				}
			});

			if (chk > 0){
				fn.releaseDateCheck();
			} else{
				availableCheck('save');
			}
		}
	};
	
	function availableCheck(saveFlag) {
		cleanErrMsg("_modelList");
		var rows = fn.getModelGridRows('#_modelList');
		$('input[name=prjModelJson]').val(JSON.stringify(rows));

		cleanErrMsg("_modelDeleteList");
		var delRows = fn.getModelGridRows('#_modelDeleteList');
		$('input[name=prjDeleteModelJson]').val(JSON.stringify(delRows));
		
		$('input[name=saveFlag]').val(saveFlag);
		var verifyYn = $("#verifyYn").val();

		if(verifyYn == 'Y'){
			// packaging File Delete 되는 대상에 대한 처리
			var packageFileId = $("[name='packageFileId']").val();
			var packageFileId2 = $("[name='packageFileId2']").val();
			var packageFileId3 = $("[name='packageFileId3']").val();

			if(packageFileId == ""){
				$("[name='openSourceFileName']").val("");
			}

			if(packageFileId2 == ""){
				$("[name='openSourceFileName2']").val("");
			}

			if(packageFileId3 == ""){
				$("[name='openSourceFileName3']").val("");
			}
		}
		
		$("#distributionForm").ajaxForm({
			url : '/project/distribution/availableCheck',
			type : 'POST',
			dataType: "json",
			cache : false,
			success: showCheckResult,
			error : onError
		}).submit();
	}
	
	function showCheckResult(json, status) {
		
		if(json.isValid == "false") {
			if(json.validMsg) {
				if(json.validMsg == "addedModel") {
					// OSDD에 추가된 모델정보가 있다면, model grid를 다시 그린다
					var _msgStr = '<spring:message code="msg.distribute.sync.addedmodel" />';
					
					for(var idx in json.externalData){
						var _addData = json.externalData[idx];
						var _tempRandId = $.jgrid.randId();
						_addData.gridId = _tempRandId;
						
						$('#_modelList').jqGrid('addRowData', _tempRandId, _addData, "last");
						
						_msgStr += "<br/>" + _addData.categoryNm + " : " + _addData.modelName;
					}
					
					if(json.externalData2) {
						modelData = json.externalData2;
					}
					
					alertify.alert(_msgStr, function(){});
				} else {
					alertify.alert(json.validMsg, function(){});
				}
			} else {
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
			
			return false;
		}
		
		isNewDesc=true;
		
		var data = JSON.parse(json.resultData);
		//var data = json.resultData; // TODO - 왜 JSON.parse를 지웠는지 파악이 필요함.
		
		if(data.errorMsg) {
			alertify.alert(data.errorMsg, function(){});

			return false;
		}
		
		var distributeListHTML = "";
		var descriptionHTML = '';
		var softwareModelHTML = '';
		var registerHtml	= '';
		var noticeFileHtml	= '';
		var ossFileHtml		= '';
		
		var verifyYn = $("#verifyYn").val();
		var noticeFileName = $("[name='licenseFileName']").val();
		var packagingFile1 = $("[name='openSourceFileName']").val();
		var packagingFile2 = $("[name='openSourceFileName2']").val();
		var packagingFile3 = $("[name='openSourceFileName3']").val();
					
		var distributeName = $("#distributeName").val();
		var softwareTypeCd = $("#distributeSoftwareType").val();
		var softwareTypeText = $("#distributeSoftwareType > option[value='"+softwareTypeCd+"']").text();
		var newTag = "&nbsp;(<b><i>NEW</i></b>)";
		var updatedTag = "&nbsp;(<b><i>Update</i></b>)";
		var noChangeTag = "&nbsp;(<i>No change</i>)";
		
		if(data.descKey != null && data.descKey != ''){
			isNewDesc = false;
			registerHtml = 
				 'Update ( Created By '
				+ '<em>'+data.descCreatorDivision+'</em> / '
				+ '<em>'+data.descCreator+'</em> '
				+ '<em>'+data.descCreatedDate+'</em> )'
				
				+ ' / ( Last Modified By <em>'+data.descModifierDivision+'</em> / '
				+ '<em>'+data.descModifier+'</em> '
				+ '<em>'+data.descModifiedDate+'</em> )';
			descKey = data.descKey;
		}

		descriptionHTML = distributeName;
		
		if((distributeDeployTime != null && distributeDeployTime != '')
				|| (descKey != null && descKey != '')){
			if(distributeNameChangeFlag){
				descriptionHTML += updatedTag;
			} else {
				descriptionHTML += noChangeTag;
			}
		} else {
			descriptionHTML += newTag;
		}
		
		softwareModelHTML = softwareTypeText;
		
		if((distributeDeployTime != null && distributeDeployTime != '')
				|| (descKey != null && descKey != '')){
			if(swModelChangeFlag){
				softwareModelHTML += updatedTag;
			} else {
				softwareModelHTML += noChangeTag;
			}
		} else {
			softwareModelHTML += newTag;
		}
		
		noticeFileHtml = noticeFileName;
		var chagnedNoticeYn = $("[name='chagnedNoticeYn']").val();
		var noticeFileValid = (data.noticeFileValid || '').toUpperCase();
		
		if(noticeFileValid == 'EXISTS'){
			existsFile = true;
			existsFileNotify = true;
			
			if(chagnedNoticeYn == 'Y'){
				noticeFileHtml += updatedTag;
			} else {
				noticeFileHtml += noChangeTag;
			}
		} else if(noticeFileValid == 'NEW') {
			noticeFileHtml += newTag;
		}

		ossFileHtml = packagingFile1 || 'N/A';
		
		if(packagingFile2 != null && packagingFile2 != ''){
			ossFileHtml += (', ' + packagingFile2); 
		}
		
		if(packagingFile3 != null && packagingFile3 != ''){
			ossFileHtml += (', ' + packagingFile3); 
		}
		
		var ossFileValid = (data.ossFileValid || '').toUpperCase();
		var statusVerifyYn = $("[name='statusVerifyYn']").val();
		
		if(ossFileValid == 'EXISTS'){
			existsFile = true;
			existsFileSource = true;
			
			if(verifyYn == 'Y' || statusVerifyYn == 'C'){
				ossFileHtml += updatedTag;
			} else {
				ossFileHtml += noChangeTag;
			}
		} else if(ossFileValid == 'NEW') {
			ossFileHtml += newTag;
		}

		var newModelFlag = $('#_modelList').jqGrid('getDataIDs').filter(function(cur){ return cur.indexOf("jqg") > -1; }).length > 0;
		var deleteModelFlag = $('#_modelDeleteList').jqGrid('getDataIDs').length > 0;
		var modelHTML = newModelFlag ? 'Add' : '';

		if(deleteModelFlag){
			if(modelHTML != ''){
				modelHTML += ' / ';
			}
			
			modelHTML += 'Delete'; 
		}

		if(modelHTML != '') {
			if((distributeDeployTime != null && distributeDeployTime != '')
					|| (descKey != null && descKey != '')){
				modelHTML += updatedTag;
				if(!distributeNameChangeFlag && !swModelChangeFlag && verifyYn != 'Y' && statusVerifyYn != 'C'){
					modelOnlyChangeFlag = true;
				}
			} else {
				modelHTML += newTag;
			}
		}
		
		// Description Register
		distributeListHTML += '<li>';
			distributeListHTML += '<strong>Description Register type : </strong>';
			distributeListHTML += '<span>';
				distributeListHTML += registerHtml||'New';
			distributeListHTML += '</span>';
		distributeListHTML += '</li>';
		
		// Description
		distributeListHTML += '<li id="li-description">';
			distributeListHTML += '<strong>Description : </strong>';
			distributeListHTML += '<span>';
				distributeListHTML += descriptionHTML;
			distributeListHTML += '</span>';
		distributeListHTML += '</li>';
		
		// software / model
		distributeListHTML += '<li id="li-software-model">';
			distributeListHTML += '<strong>Software/Model : </strong>';
			distributeListHTML += '<span>';
				distributeListHTML += softwareModelHTML;
			distributeListHTML += '</span>';
		distributeListHTML += '</li>';
		
		// oss Notice File
		distributeListHTML += '<li>';
			distributeListHTML += '<strong>OSS Notice : </strong>';
			distributeListHTML += '<span>';
				distributeListHTML += noticeFileHtml;
			distributeListHTML += '</span>';
		distributeListHTML += '</li>';
		
		// oss Packaging file
		distributeListHTML += '<li>';
			distributeListHTML += '<strong>OSS Package : </strong>';
			distributeListHTML += '<span>';
				distributeListHTML += ossFileHtml;
			distributeListHTML += '</span>';
		distributeListHTML += '</li>';
		
		// Model
		if(modelHTML != ''){
			distributeListHTML += '<li>';
				distributeListHTML += '<strong>Model Information : </strong>';
				distributeListHTML += '<span>';
					distributeListHTML += modelHTML;
				distributeListHTML += '</span>';
			distributeListHTML += '</li>';
		}
		
		//Distribute시 전송할 최종수정시간 데이터 입력
		$('input[name=lastModifiedTime]').val(data.lastModifiedTime);
		
		$(".distributeList").html(distributeListHTML);
		
		$("#_list2").jqGrid('clearGridData');

		//결과값에 임시로 lpad
		if(data.permission) {
			for(var i=0; i<data.permission.length; i++){
				data.permission[i].category = lpad(data.permission[i].category,6,'0');  
			}
		}

		printDistributeGrid();

		$("#_list2").jqGrid('setGridParam',{data:data.permission}).trigger('reloadGrid');

		// osd에서 validation check에 걸린경우
		if(data.errorMsg || json.isValid == "false") {
			$("#distribute, #only, #immmediatelyFlag").hide();
		} else {
			$("#distribute, #only, #immmediatelyFlag").show();
		}
		
		if(!alertify.distributionCheckResult){
			alertify.dialog('distributionCheckResult', function() {
				var settings;

				return {
					setup: function() {
						var settings = alertify.alert().settings;
						
						for (var prop in settings) {
							this.settings[prop] = settings[prop];
						}
						
						var setup = alertify.alert().setup();
						
						return setup;
					},
					settings: {
					  oncontinue: null
					},
					callback: function(closeEvent) {}
				};
			}, false, 'alert');
		}
		
		alertify.distributionCheckResult().set({'closableByDimmer': true, 'frameless': true, 'resizable': true, 'message':$('.lastStep').html()}).resizeTo($('.lastStep').width() + 70,$('.lastStep').height() + 50).show(); 
	}
	
	function isAddedModel(merge, modelInfo) {
		var rtnVal = true;
		
		for(var i=0; i<modelInfo.length; i++){
			if(modelInfo[i].category.trim() == merge.category.trim() && modelInfo[i].model.trim() == merge.modelName.trim()){
				rtnVal = false;

				break;
			}
		}
		
		return rtnVal;
	}
	
	function hasCategoryMode(mergeList, data) {
		var rtnVal = null;

		for(var i=0; i<mergeList.length; i++){
			if(data.category.trim() == mergeList[i].category.trim() && data.model.trim() == mergeList[i].modelName.trim()){
				rtnVal = mergeList[i];

				break;
			}
		}
		
		return rtnVal;
	}
	
	function onDistributeSuccess(json, status){
		if(json.resCd=="10") {
			alertify.alert(json.resMsg, function() {
				this.close();

				reloadTabInframe('/project/list');
				reloadTabInframe('/project/distribution/'+'${project.prjId}');

				return false;
			});
		} else {
			alertify.error(json.resMsg, 0);
		}
	}
	
	function checkSetModelInfo() {
		var _subCnt = $("#_modelList").jqGrid('getGridParam', 'reccount');
		if(_subCnt !== undefined && _subCnt > 0) {
			return true;
		}
		
		alertify.alert('<spring:message code="msg.distribute.model.required" />', function(){});
		
		return false;
	}
	
	// 삭제 성공시 후 처리
	function onDeleteSuccess(json, status){
		var prjId = $('input[name=prjId]').val();
			alertify.alert('<spring:message code="msg.common.success" />', function(){
				if(prjId) {
					deleteTabInFrame('#/project/edit/'+prjId);			
				} else {
					deleteTabInFrame('#/project/edit');			
				}
				
				reloadTabInframe();
		});
	};
	
	// 에러 후 처리
	function onError(data, status){
		alertify.error('<spring:message code="msg.common.valid2" />', 0);
	};	
	
	function categoryCodeToObject(){
		var result = {};
		var categorys = currentModelCategoryValues.split(';');

		categorys.forEach(function(item) {
			var cCd = item.split(':')[0]+'';
			var cNm = item.split(':')[1];
			result[cCd] = cNm;
		});
			
		return result;
	};
	
	function lpad(originalstr, length, strToPad) {
	    while (originalstr.length < length) {
	        originalstr = strToPad + originalstr;
	    }
	    
	    return originalstr;
	};
	
	function distribute() {
		//즉시 Batch 실행
		$("#distributionForm").ajaxForm({
			url : '/project/distribution/distribute/immediately',
			type : 'POST',
			dataType: "json",
			cache : false,
			success: onDistributeSuccess,
			error : onError
		}).submit();
	};
	
	function printDistributeGrid(){
		//var json = jsonData;
		var modelGroups = [];
		var models = [];
		var permission = [];
		var modelInfo = [];
		
		$("#_list2").jqGrid({
			datatype: 'local',
			data: permission,
			colNames: ['Category','Authority'],
			colModel: [
				{name: 'category', index: 'category', width: 300, align: 'left', key:true},
				{name: 'authValid', index: 'authValid', width: 164, align: 'center'}
			],
 			autowidth: true,
			gridview: true,
			viewrecords: true,
			sortable: false,
			height: 'auto',	
			loadonce: true,
			loadComplete: function(){
				isDistribute=true;
				var categorys = categoryCodeToObject();
				var rows = $('#_list2').jqGrid('getRowData');

				rows.forEach(function(row, index, ref){
					// 조건에 따라 cell 색상 변경
					if(row.authValid=="OK"){//20161108 - 기존값 OK 
						$("#_list2").jqGrid("setCell", row.category, "authValid", "OK", {'background-color':'#feffc4'});
					} else {
						isDistribute=false;
						
						$("#_list2").jqGrid("setCell", row.category, "authValid", "Failed", {'background-color':'#fcf2f2', 'color':'#ff4646'});
					}
					
					$("#_list2").jqGrid("setCell", row.category, "category", categorys[row.category]);
				});
			}
		});
		$("#_list2").jqGrid('setGroupHeaders', {
			  useColSpanStyle: true, 
			  groupHeaders:[
				{startColumnName: 'Authority', numberOfColumns: 4, titleText: 'Availability'},
			  ]
		});
	}

	var package_fn = {
		verify : function(){
			var fileSeqs = [];
			
			var packageFileId = $("[name='packageFileId']").val();
			var packageFileId2 = $("[name='packageFileId2']").val();
			var packageFileId3 = $("[name='packageFileId3']").val();
			
			if(packageFileId != ''){
				fileSeqs.push(packageFileId);
			}
			
			if(packageFileId2 != ''){
				fileSeqs.push(packageFileId2);
			}
			
			if(packageFileId3 != ''){
				fileSeqs.push(packageFileId3);
			}

			if(fileSeqs.length == 0){
				alertify.error('No uploaded package file', 0);
				
				return false;
			}
			
			var obj = {
				fileSeqs:fileSeqs,		     // packaging file이 1~3건일때 전부 담는 array를 보내고
				prjId:'${project.prjId}'
			}
			
			if(withoutVerifyYn == 'Y'){
				$("#btnVerify").val("Completed").attr("disabled", true).removeClass("red");
				
				needVerifyFlag = false;
				
				alertify.alert('<spring:message code="msg.common.success" />', function(){});
				alertify.success('<spring:message code="msg.project.verification.success" />');
				
				$("#verifyYn").val("Y");

				return false;
			}
			
			$.ajax({
				url : '/project/distribution/verify',
				type : 'POST',					
				dataType : 'json',
				contentType : 'application/json',
				cache : false,
				data : JSON.stringify(obj) ,
			 	timeout: 2*60*60*1000 , // max
				success : function(json){
					json = JSON.parse(json);
					
					if(json.resCd == '10'){
						if(json.noCountOssCnt == 0){ // verify 성공
							$("#btnVerify").val("Completed").attr("disabled", true).removeClass("red");
							needVerifyFlag = false;
							alertify.alert(json.resMsg, function(){});
							alertify.success('<spring:message code="msg.project.verification.success" />');
							
							$("#verifyYn").val("Y");
						} else { // verify의 실패
							$("#btnVerify").val("Failed").attr("disabled", true).removeClass("red");
							var ossCnt = +(json.noCountOssCnt) - 1;
							var ossName = json.noCountOssName + (ossCnt > 0 ? ' and ' + ossCnt + ' other open sources' : '');
							
							var message = 'The path of source code (' + ossName + ') failed to verify.';
							alertify.error(message, 0);
							
							if(!alertify.commentDialog){
								//define a new dialog
								alertify.dialog('commentDialog',function factory(){
									return{
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
							//launch it.
							var btnHtml  = '<br/><b><spring:message code="complete.project.verify.error" /></b><br/>';
								btnHtml += '<input type="button" value="OK" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="location.reload()"/>&nbsp;&nbsp;&nbsp;';
								btnHtml +='<input type="button" value="Try again" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="$(\'.ajs-close\').trigger(\'click\')"/>&nbsp;&nbsp;&nbsp;';
							
							alertify.commentDialog(btnHtml);
							$(".ajs-dialog").css("height", "");
						}
					} else { // resCd가 10이외로 온 case는 일반적인 error임. verify와는 별개의 error
						alertify.error(json.resMsg);
					}
				},
				error : function(data){
					loading.hide();
					
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		uploadPackagingFile : function(seq){
			needVerifyFlag = true;
			
			var fileName = "packageFileId" + (seq == 1 ? "" : seq);				
			$('#packagingFile'+seq).children().remove(); // upload button hide
			$('input[name='+fileName+']').val(""); // before Packaging File Seq > Clear
			$("#btnVerify").val("Start to Verify").attr("disabled", false).addClass("red").show();
			$("#distributeName").attr("disabled", true);
			$("#distributeSoftwareType").attr("disabled", true);
			
			var packagingFileName = $("[name='openSourceFileName"+ (seq == 1 ? "" : seq) + "']").val();
			
			$('#uploadFile'+seq).uploadFile({
				url : '/project/distribution/registFile?prjId=${project.prjId}&fileIdx='+seq+'&packagingFileName='+packagingFileName,
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				onSuccess : function(e, data){
					var result = jQuery.parseJSON(data);
					
					result.forEach(function(item){
						var appendHtml  = '<li>';
								appendHtml += '<span>';
									appendHtml += '<a href="/download/'+item[0].registSeq+'/'+item[0].fileName+'" class="urlLink left">'+item[0].originalFilename+'</a>';
									appendHtml += '&nbsp;<input type="button" value="Delete" class="smallDelete" onclick="package_fn.uploadPackagingFile('+seq+')">&nbsp;Updated';
								appendHtml += '</span>';
							appendHtml += '</li>';
										
						$('#packagingFile'+seq).append(appendHtml);
						$('input[name='+fileName+']').val(item[0].registSeq);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						$("#fileUplWarnMessage_"+seq).hide();
						$('#uploadFile'+seq).children().remove();
						$('#uploadFile'+seq).next().remove();
						$("#btnVerify").val("Start to Verify").attr("disabled", false).addClass("red").show();
					});
				},
			});
		}
	};
//]]>
</script>
