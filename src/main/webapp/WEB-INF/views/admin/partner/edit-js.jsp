<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
// 	var subGridUrl = true;
var userRole = '${sessUserInfo.authority}';
var ossNames = [];
var objs = [];
var licenseNames = [];
var isStatusChangeFlag = false;
var isSort = false;
var delDocumentsFile = [];
var etcDomain = "${ct:getConstDef('CD_DTL_ECT_DOMAIN')}";
var divisionEmptyCd = "${ct:getConstDef('CD_USER_DIVISION_EMPTY')}";
var partnerData = ${empty detailJson ? '{}' : detailJson};
var prjList = ${empty prjList ? '{}' : prjList};
var sampleFile =  ${ct:getAllValuesJson(ct:getConstDef('CD_SAMPLE_FILE'))};
var _popupCheckOssName = null;
var _popupCheckOssLicense = null;
var saveFlag = false;

	$(document).ready(function () {
		'use strict';
		evt.init();
		evt.tabInit();
		datas.init();
		grid.init();

		<c:if test="${batFlag}">
		//if('${detail.partnerId}' != ""){
			//bat_evt.init();
		//}
		</c:if>

		activeLink();
		if('${detail.viewOnlyFlag}' == 'Y') {
			initCKEditorNoToolbar('editor4', true);
		} else {
			initCKEditorNoToolbar('editor4', false);
		}
		
		// autoConplete 문제로 인한 처리
		$("#partnerName").val(partnerData.partnerName);
		$("#softwareName").val(partnerData.softwareName);
		// ossNames auto complete
		fn_grid_com.griOssNames().success(function(data, status, headers, config){
			if(data != null){
				data.forEach(function(obj){
					ossNames.push(obj.ossName);
				});
			}
		});
		
		// licenseNames auto complete
		commonAjax.getLicenseTags().success(function(data, status, headers, config){
			if(data != null){
				var tag = "";
				
				data.forEach(function(obj){
					if(obj!=null) {
						tag = {
							value : obj.shortIdentifier.length > 0 ? obj.shortIdentifier : obj.licenseName,
							label : obj.licenseName + (obj.shortIdentifier.length > 0 ? (" (" + obj.shortIdentifier + ")") : ""),
							type : obj.licenseType,
							obligation : obj.obligation,
							obligationChecks : obj.obligationChecks
						}
						
						licenseNames.push(tag);
					}
				});
			}
		});
		
		if('${detail.partnerId}' != ""){
			$("input[name=creatorNm]").val('${detail.creatorName }');
		}
		
		$("#_projectList").jqGrid({
			datatype: 'local',
			data: prjList,
			colNames:['ID','Project Name', 'Project Version'],
			colModel:[
				{name:'prjId',index:'prjId', width:70, sortable:false, align:"center", key:true},
				{name:'prjName',index:'prjName', width:400, sortable:false},
				{name:'prjVersion',index:'prjVersion', width:100, sortable:false, align:"center"}
			],
			rowNum:100,
			viewrecords: true,
			height: 'auto',
			ondblClickRow: function(rowid,iRow,iCol,e) {
				var rowData = $("#_projectList").jqGrid('getRowData',rowid);
				if("Y" != rowData.oldSystemFlag) {
					createTabInFrame(rowData['prjId']+'_Project','#<c:url value="/project/edit/'+rowData['prjId']+'"/>');
				}
			},
			loadComplete: function(data) {
				if(data.records > 0) {
					var multRowIds = []; 
					var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row, rowid, rowData, className;
					for(var _idx=0;_idx<rowsCount;_idx++) {
						row = rows[_idx];
						className = row.className;
						if (className.indexOf('jqgrow') !== -1) {
							rowid = row.id;
							rowData = data.rows[rowIdx++];
							if(rowData.oldSystemFlag == "Y") {
								className = className + ' excludeRow';
							}
							row.className = className;
						} else if(className.indexOf('ui-subgrid') !== -1){
							rowIdx++;
						}
					}
				}
			}
		});
	});
	var commentTemp = '';
	var modifyCommentId = '${detail.comment}';
	var commentIdx= '${detail.comment}';
	var evt = {
		init : function(){
			commentTemp = $('<div>').append($('dl[name=commentClone]').clone());
			$('dl[name=commentClone]').remove();
			
			$('select[name=userDivision]').trigger('change');
			$('select[name=division]').trigger('change');
			
			$('select[name=division]').on("change", function(){
				fn.changeDivision()
			});
			
			//와쳐 추가 버튼
			$('#addWatcher').on('click', function(){
				/* division 정보 */
				var $divSel	= $("#userDivision")
				  , divVal	= $divSel.val()
				  , divTxt	= $divSel.find("option[value='"+divVal+"']").text();
								
				/* division 정보 */
				var $userSel	= $("#userName")
				  , userVal		= $userSel.val()||""
				  , userTxt		= $userSel.find("option[value='"+userVal+"']").text();

				// 선택한 item이 없을 경우.
				if(divVal == "" || userVal == "") {
					return alertify.error('<spring:message code="msg.project.required.selectDivision" />', 0);
				}
				
				/* tag 정보 */
				var isNew = true;
				
				$(".watcherTags").each(function(i, tag){
					var tagDiv = $(tag).val().split("/")[0]
					  , tagUid = $(tag).val().split("/")[1];
					
					if(divVal == tagDiv) {
						if(tagUid == 'all') { // 선택된 태그 중에 all이 있을 경우 등록 안함
							isNew = false;

							return false;
						}
						
						if(userVal == 'all') { // all을 선택했을 경우 기존 부서의 userId를 가진 tag를 삭제
							$(tag).closest('span').remove();
						} else if(tagUid == userVal) {
							isNew = false;	// 이미 등록된 watcher가 있을경우
						}
					}
				});
				
				if(isNew) {
					var watcherStr = divisionEmptyCd != divVal ? (divTxt + "/" + userVal) : userVal;
					fn.addWatcher(divVal, userVal, '');
					fn.addHtml($("#nameSpace"), watcherStr, divVal, userVal);
				}
			});
			
			$('div.multiTxtSet2 .smallDelete').on('click', function(){
				$(this).parent().remove();
			});

			//코멘트 임시저장
			$('.saveEditorDraft').click(function(){
				var editorVal = CKEDITOR.instances.editor.getData();
				var register = '${sessUserInfo.userId}';
				var param = { referenceId : $('input[name=partnerId]').val(), referenceDiv :'21', contents : editorVal };
				
				$.ajax({
					url : '<c:url value="/partner/saveComment"/>',
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
			});
			//파일업로드
			var accept1 = '';
			
			<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
				<c:if test="${file eq '21'}">
				accept1 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
				</c:if>
			</c:forEach>
			
			$('#confirmationFile').uploadFile({
				url : '<c:url value="/partner/ossFile"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				sequential:true,
				sequentialCount:1,
				onSuccess:function(files,data,xhr,pd){
					// 161102 jy-seo 파일 업로드 에러처리
					var result = jQuery.parseJSON(data);
					
					if(result == null){
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					} else {
						result = result[0][0];
						if(result && result.uploadSucc) {
							var appendHtml = '<span style="margin-left:20px;">'+result.createdDate+'</span>';
							var _url = '<c:url value="/download/'+result.registSeq+'/'+result.fileName+'"/>';

							$('.confirmationUpload').children().remove();
							$('.confirmationUpload').append('<a href="'+_url+'">'+result.originalFilename+'</a>'+appendHtml);
							$('.confirmationUpload').append(' <span><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteConfirmationFile(this)" style="vertical-align:super;"/></span>');
							$('.confirmationUpload').append('<input type="hidden" name="confirmationFileId" value="'+result.registSeq+'"/>');
						} else {
							alert('<spring:message code="msg.common.upload.failed" />');
						}
						
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					}
				}
			});
			
			//파일업로드
			var accept2 = '';
			
			<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
				<c:if test="${file eq '22'}">
				accept2 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
				</c:if>
			</c:forEach>
			
			$('#ossFile').uploadFile({
				url : '<c:url value="/partner/ossFile?excel=Y"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				sequential:true,
				sequentialCount:1,
				onSuccess:function(files,data,xhr,pd){
					// 161102 jy-seo 파일 업로드 에러처리
					var result = jQuery.parseJSON(data);
					
					if(result == null) {
						alertify.error('<spring:message code="msg.common.valid" />', 0);

						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					} else {
						if(result[0] == "FILE_SIZE_LIMIT_OVER") {
							alertify.alert(result[1], function() {
								$('.ajax-file-upload-statusbar').fadeOut('slow');
								$('.ajax-file-upload-statusbar').remove();
							});
						} else if(result[2] == "CSV_FILE") {
							var result_ = result[0][0];

							if(result && result_.uploadSucc) {
								var appendHtml = '<span style="margin-left:20px;">'+result[0][0].createdDate+'</span>';
								var _url = '<c:url value="/download/'+result[0][0].registSeq+'/'+result[0][0].fileName+'"/>';

								$('.ossUpload').children().remove();
								$('.ossUpload').append('<a href="'+_url+'">'+result[0][0].originalFilename+'</a>'+appendHtml);
								$('.ossUpload').append(' <span><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteOssFile(this)" style="vertical-align:super;"/></span>');
								$('.ossUpload').append('<input type="hidden" name="ossFileId" value="'+result[0][0].registSeq+'"/>');

								fn.getCsvData();
							} else {
								alert('파일 업로드에 실패하였습니다.');
							}
						} else if(result[2] == "EXCEL_FILE") {
							var result_ = result[0][0];
							
							if(result && result_.uploadSucc) {
								if(result[1] && result[1].length != 0) {
									$('.sheetSelectPop').show();
									$('.sheetSelectPop .sheetNameArea').children().remove();
									$('.sheetSelectPop .sheetNameArea').text('');
									
									for(var i = 0; i < result[1].length; i++){
										var num = i+1;
										var checkedTxt = "";
										var sheetName = result[1][i].name.toUpperCase().trim();

										if(sheetName == "OSS LIST" || sheetName == "OPEN SOURCE SOFTWARE LIST"){
											checkedTxt = "checked";
										}
										
										$('.sheetSelectPop .sheetNameArea').append('<li><input type="checkbox" name="sheetNameSelect" value="'+result[1][i].no+'" id="sheet'+result[1][i].no+'" class="sheetNum" '+checkedTxt+'>'
																	+'<label for="sheet'+result[1][i].no+'">'+result[1][i].name+'</label></li>');
									}						
								}
								$('.ossUpload').children().remove();

								var appendHtml = '<span style="margin-left:20px;">'+result[0][0].createdDate+'</span>';

								$('.ossUpload').append('<a href="/download/'+result[0][0].registSeq+'/'+result[0][0].fileName+'">'+result[0][0].originalFilename+'</a>'+appendHtml);
								$('.ossUpload').append(' <span><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteOssFile(this)" style="vertical-align:super;"/></span>');
								$('.ossUpload').append('<input type="hidden" name="ossFileId" value="'+result[0][0].registSeq+'"/>');
							} else {
								alertify.alert('<spring:message code="msg.common.upload.failed" />', function(){});
							}
							
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();
						} else if(result[2] == "SPDX_SPREADSHEET_FILE") {
							var result_ = result[0][0];
							if(result && result_.uploadSucc) {
								var appendHtml = '<span style="margin-left:20px;">'+result[0][0].createdDate+'</span>';
								$('.ossUpload').children().remove();
								$('.ossUpload').append('<a href="/download/'+result[0][0].registSeq+'/'+result[0][0].fileName+'">'+result[0][0].originalFilename+'</a>'+appendHtml);
								$('.ossUpload').append(' <span><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteOssFile(this)" style="vertical-align:super;"/></span>');
								$('.ossUpload').append('<input type="hidden" name="ossFileId" value="'+result[0][0].registSeq+'"/>');
								fn.getSpdxSpreadsheetData();
							} else {
								alert('파일 업로드에 실패하였습니다.');
							}
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();
						} else {
							alertify.error('<spring:message code="msg.common.valid" />', 0);
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();
						}
					}
				}
			});
			
			$('#documentsFile').uploadFile({
				url : '<c:url value="/partner/documentsFile"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				sequential:true,
				sequentialCount:1,
				dynamicFormData: function() {
					var data ={ "registFileId" :$('#documentsFileId').val() }

					return data;
				},
				onSuccess:function(files,data,xhr,pd){
					// 161102 jy-seo 파일 업로드 에러처리
					var result = jQuery.parseJSON(data);
					
					if(result == null) {
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					} else {
						result = result[0];
						
						if(result && result.uploadSucc) {
							var appendHtml = '<span style="margin-left:20px;">'+result.createdDate+'</span>';
							var _url = '<c:url value="/download/'+result.registSeq+'/'+result.fileName+'"/>';
							$('.documentsFileArea').append('<li><a href="'+_url+'">'+result.originalFilename+'</a>'+appendHtml+'<span><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteDocumentsFile(this)" style="vertical-align:super;margin-left: 5px;"/></span><input type="hidden" value="'+result.registSeq+'"/></li>');
							$("#documentsFileId").val(result.registFileId);
							
							if($(".documentsFileArea > li").length >= 5) {
								$("#documentsFile").hide();
							}
						} else {
							alertify.alert('<spring:message code="msg.common.upload.failed" />', function(){});
						}
						
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					}
				}
			});
			
			// email 왓쳐 추가
			$("#addEmail").click(function(){
				/* AD ID 정보 */
				var adId = $("#adId").val();
				var domain = $("#emailTemp").val();

				if(adId == "") {
					$("#adId").focus();
					// return alertify.error('Please enter watcher AD ID', 0);
					return alertify.error('<spring:message code="enter.watcher.error" />', 0);
				}
				
				var _email = adId + "@" + domain;
				var regEmail = /([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;

				if (!regEmail.test(_email)) {
					$("#adId").focus();
					// return alertify.error('Invalid email address.', 0);
					return alertify.error('<spring:message code="invalid.email.error" />', 0);
				}
				
				$.ajax({
					type: "POST",
					url: '<c:url value="/system/user/checkEmail"/>', 
					type : 'GET',
					dataType : 'json',
					cache : false,
					data : {'email' : _email},
					success: function (data) {
						if("true" == data.isValid) {
							var watcherStr = divisionEmptyCd != data.division ? (data.divisionName + "/" + data.userId) : data.userId;
							fn.addWatcher(data.division, data.userId, '');
							fn.addHtml($("#nameSpace"), watcherStr, data.division, data.userId);
						} else {
							fn.addWatcher('', '', _email);
							fn.addHtml($("#nameSpace"), _email, _email, "Email");
						}
					},
					error: function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
				
				$("#adId").val('');
				$("#domain").val("${ct:getConstDef('CD_DTL_DEFAULT_DOMAIN')}").trigger('change');
			});
			
			$('.btnCommentHistory').on('click', function(e){
				e.preventDefault();
				openCommentHistory('<c:url value="/comment/popup/3rd/${detail.partnerId}"/>');
			});
			
			$(window).resize(function(){
				fn.gridHeaderResize();
			});
			
			$('#partnerForm').ajaxForm({
				url : '<c:url value="/partner/saveAjax"/>',
				type : 'POST',
				dataType : 'json',
				cache : false,
				success: fn.onRegistSuccess,
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
			
			if('${detail.partnerId}' != ""){
				$("[name='publicYn']").on('change',function(e){
					var param = {partnerId : '${detail.partnerId}', publicYn : ($("[name='publicYn']:checked").val())};
					
					$.ajax({
						url : '<c:url value="/partner/updatePublicYn"/>',
						type : 'POST',
						data : JSON.stringify(param),
						dataType : 'json',
						cache : false,
						contentType : 'application/json',
						success: function(){
							alertify.success('<spring:message code="msg.common.success" />');
						},
						error : fn.onError
					});
				});
			}
			
			$('#addList').on('click', function(){
				var listKind = $("#listKind").val()
				  , listId = $("#listId").val();
					
				if(fn.chkListValidation(listKind, listId)){
					var obj = {};
					
					obj["listKind"] = listKind;
					obj["listId"] = listId;
					
					fn.copyWatcher(obj);
				}
			});

			$("#domain").on("change", function(e){
				var value = $(this).find("option:selected").val();
				var domain = $(this).find("option:selected").text();
				
				if(etcDomain == value){
					$("#emailTemp").val("").show();
				} else {
					$("#emailTemp").val(domain).hide();
				}
			});

			//프로젝트 리스트 더보기
			$("#listMore").on("click",function(){
				createTabInFrameWithCondition("Project List", '#<c:url value="/project/list"/>', 'PARTNERLISTMORE', $('input[name=partnerName]').val());
			});

			$("#createProject").on("click", function(){
				createTabInFrameWithCondition("New_Project", '#<c:url value="/project/edit"/>', 'PARTNER', encodeURIComponent("${detail.partnerId}||${detail.partnerName}||${detail.softwareName}"));
			});
		},
		tabInit: function(){
			var tabContent = $(".tabContent");
			var tabMenuA = $(".tabMenu a");

			if('${detail.partnerId}' == ''){
				tabMenuA.eq("1").hide();
			}
			
			tabContent.hide();
			tabMenuA.click(function () {
				$(".tabMenu a span").remove();
				tabMenuA.eq("0").text("3rd party");
				<c:if test="${batFlag}">
				tabMenuA.eq("1").text("BAT(Optional)");
				</c:if>
				
				var tag = "<span>"+$(this).text()+"</span>";
				$(this).html(tag);
				
				tabContent.hide();
				activeTabText = $(this).text();
				activeTab = $(this).attr("rel");
				
				$("#" + activeTab).show();

				if(activeTab == "batDiv") {
					$(".projdecBtn").hide();
				} else {
					$(".projdecBtn").show();
				}
				
				// _mainLastsel 꼬이는것 방지
				if(activeTab == "0") {
					_mainLastsel = $('#list').jqGrid('getGridParam', "selrow" );
				} else if(activeTab == "batDiv") {}
				
				// 선택된 로우가 없을경우 _mainLastsel 초기화
				if(typeof(_mainLastsel)!="string") {
					_mainLastsel = -1;
				}
			});
			
			tabMenuA.eq("0").click(); 
		}
	};
	
	// party 그리드 데이터 전역 변수
	var partyMainData;
	var partySubData;
	var partyValidMsgData_e = []; //초기화
	var partyDiffMsgData_e = []; //초기화
	var fn = {
		editDescription : function(){
			var linkText = initCKEditorNoToolbar("editor4", false);
			var data = {"partnerId" : $('input[name=partnerId]').val() , "description" : linkText};
			$.ajax({
				url : '<c:url value="/partner/updateDescription"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(){
					alertify.success('<spring:message code="msg.common.success" />');
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		// party 그리드 데이터
		getPartyGridData : function(){
			$.ajax({
				url : '<c:url value="/project/identificationGrid/${detail.partnerId}/20"/>',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : {referenceId : '${detail.partnerId}'},
				contentType : 'application/json',
				success : function(data){
					partyMainData = data.mainData;
					partyValidMsgData_e = []; //초기화
					partyDiffMsgData_e = []; //초기화
					
					if(data.validData) {
						partyValidMsgData_e = data.validData;
					}
					if(data.diffData) {
						partyDiffMsgData_e = data.diffData;
					}

					// 리로드 대신 그리드 삭제 후 다시 그리기
					$("#list").jqGrid('GridUnload');
					
					grid.init();

					// totla record 표시
					$("#list_toppager_right, #pager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+partyMainData.length+'</div>');
					
					fn_grid_com.addEtcKeyDownEvent($('#list'), partyValidMsgData_e, partyDiffMsgData_e, null, com_fn.getLicenseName);
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		changeDivision : function() {
			var divisionCd = $("#division option:selected").val();
			if("" != divisionCd) {
				var postData = { "partnerId" : '${detail.partnerId}', "division" : divisionCd};
				$.ajax({
					url : '<c:url value="/partner/changeDivisionAjax"/>',
					type : 'POST',
					data : JSON.stringify(postData),
					dataType : 'json',
					contentType : 'application/json',
					cache : false,
					success: function(data) {
						if(data.isValid == "false") {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);	
						} else {
							alertify.success('<spring:message code="msg.common.success" />');	
						}
					},
					error: function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		save : function(){
			if (fn.checkStatus()){
				com_fn.exitCell(_mainLastsel, "list");
				
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						cleanErrMsg("list");
						$("div.retxt").hide();
						
						//public 값 넣어주기
						if($('#checkbox3').is(':checked')) {
							$('#checkbox3').val('N');
						} else {
							$('#checkbox3').val('Y');
						}
						
						fn_grid_com.totalGridSaveMode('list');
						
						var target = $("#list");
						var mainData = target.jqGrid('getGridParam','data'); // 메인 그리드
						
				 		mainData.forEach( function(_rowData){
				 			var _rowId = _rowData['gridId'];
				 			
				 			if(_rowData["obligationLicense"] == "90") {
				 				if(_rowData["notify"] == "Y") {
				 					if(_rowData["source"] == "Y") {
				 						_rowData["obligationType"] = "11";
				 					} else {
				 						_rowData["obligationType"] = "10";
				 					}
				 				} else if(_rowData["notify"] == "N") {
				 					_rowData["obligationType"] = "99";
				 				}
				 			}
				 		});
				 		
						$('#ossComponentsStr').val(JSON.stringify(mainData));
						$('#userComment').val(JSON.stringify(CKEDITOR.instances['editor'].getData()));
						
						var prjId = '${detail.partnerId}';
						var postData = {"mainData" : JSON.stringify(mainData), "prjId" : prjId};
						
						$.ajax({
							url : '<c:url value="/project/nickNameValid/20"/>',
							type : 'POST',
							data : JSON.stringify(postData),
							dataType : 'json',
							cache : false,
							contentType : 'application/json',
							success: function(data){fn.makeNickNamePopup(data);},
							error: function(data){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					} else {
						return false;
					}
				});
			}else{
				alertify.alert('<spring:message code="msg.partner.warning" />', function(){});
			}

		},
		makeNickNamePopup : function(obj) {
			// ajax FormSubmit 사용시 huge String 문자가 포함되면 data가 전송되지 않는 문제가 있어서 formsubmi에서 applicaton/json으로 변경
			var postData = $("#partnerForm").serializeObject();
			
			if(obj.validMsg && obj.validMsg.length != 0){
				alertify.alert(obj.validMsg, function () {
					$.ajax({
						url : '<c:url value="/partner/saveAjax"/>',
						type : 'POST',
						data : JSON.stringify(postData),
						dataType : 'json',
						contentType : 'application/json',
						cache : false,
						success: fn.onRegistSuccess,
						error: function(data){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
				});
			} else {
				$.ajax({
					url : '<c:url value="/partner/saveAjax"/>',
					type : 'POST',
					data : JSON.stringify(postData),
					dataType : 'json',
					contentType : 'application/json',
					cache : false,
					success: fn.onRegistSuccess,
					error: function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		onRegistSuccess : function(data) {
			_mainLastsel = -1;
			
			if("false" == data.isValid) {
				if("99" == data.resCd) { //중복 처리
					alertify.alert('<spring:message code="msg.value.duplicate.partner" />');
					fn.partnerGridValidMsg(data.dupData);

					return;
				}

				if(data.validMsg != ''){
					fn.partnerGridValidMsg(data.resultData);
				}
				
				// 폼 메세지
				if(data.partnerName){
					$('input[name=partnerName]').next().text(data.partnerName).show();
				}
				
				if(data.softwareName){
					$('input[name=softwareName]').next().text(data.softwareName).show();
				}
				
				if(data.confirmationFileId){
					$("div.retxt.confirmationFile").text(data.confirmationFileId).show();
				}
				
				if(data.ossFileId){
					$("div.retxt.ossFileId").text(data.ossFileId).show();
				}
				
				if(data.softwareVersion){
					$('input[name=softwareVersion]').next().text(data.softwareVersion).show();
				}
				
				if(data.duplicatePartner){
					$('input[name=partnerName]').next().text(data.duplicatePartner).show();
					$('input[name=softwareName]').next().text(data.duplicatePartner).show();
					$('input[name=softwareVersion]').next().text(data.duplicatePartner).show();
				}
				
				partyValidMsgData_e = data;
				
				alertify.error('<spring:message code="msg.common.valid" />', 0);
			} else {
				if("10" == data.resCd){
					if(isStatusChangeFlag) {
						var param = {status : 'REQ', partnerId : '${detail.partnerId}', userComment : CKEDITOR.instances['editor'].getData()};
						checkObligationFlag = false;
						var _list = $("#list");
						var mainData = _list.jqGrid('getGridParam','data');

						mainData.forEach( function(_rowData){
							if( _rowData['obligationLicense'] == "90" && _rowData['notify'] == "") {
								checkObligationFlag = true;
							}
						});
						
						if(checkObligationFlag) {
							alert('<spring:message code="msg.warn.include.needcheck.license" />');
							return false;
						}
						
						isStatusChangeFlag = false;
						$.ajax({
							url : '<c:url value="${suffixUrl}/partner/changeStatus"/>',
							type : 'POST',
							data : param,
							dataType : 'json',
							cache : false,
							success : function(data){
								if(data.isValid == "false") {
									gridCleanErrMsg("list");
									partyValidMsgData_e = data.externalData;
									partyDiffMsgData_e = data.externalData2;
									
									if(partyValidMsgData_e) {
										gridValidMsgNew(partyValidMsgData_e, "list");
									}
									
									if(partyDiffMsgData_e) {
										gridDiffMsg(partyDiffMsgData_e, "list");
									}
									
									alertify.error('<spring:message code="msg.common.valid" />', 0);
								} else {
									reloadTabInframe('<c:url value="/partner/list"/>');
									if('${detail.partnerId}' != ''){
										reloadTabInframe('<c:url value="/partner/edit/${detail.partnerId}"/>');
									}
								}
							},
							error : function(){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					} else {
						var partnerId = $('input[name=partnerId]').val();
						alertify.alert('<spring:message code="msg.common.success" />', function(){
							reloadTabInframe('<c:url value="/partner/list"/>');
							
							if(partnerId){
								fn.getPartyGridData();

								saveFlag = true;
							} else {
								deleteTabInFrame('#<c:url value="/partner/edit"/>');	
								if(data.partnerId) {
									createTabInFrame(data.partnerId+'_3rdParty', '#<c:url value="/partner/edit/'+data.partnerId+'"/>');
								}
							}
						});
					}

				}else{
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			}
		},
		deleteWatcher : function(obj){
			$(obj).parent().remove();
		},
		deleteComment : function(obj){
			if(!confirm('<spring:message code="msg.partner.confirm"/>')) return;
			var commId = $(obj).next().val();
			$.ajax({
				url : '<c:url value="/partner/deleteComment"/>',
				type : 'POST',
				dataType : 'json',
				cache : false,
				data : {'commId' : commId},
				success : function(data){},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		editorDialog : function(){
			if(!alertify.myAlert){
				//define a new dialog
				alertify.dialog('myAlert',function factory(){
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
			var btnHtm = '<br/><b>Send an email to</b><br/>';
			btnHtm += '<input type="button" value="Watcher Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'W\')"/>&nbsp;&nbsp;&nbsp;';

			if(userRole == "ROLE_ADMIN") {
				btnHtm += '<input type="button" value="Creator Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'C\')"/>&nbsp;&nbsp;&nbsp;';
			} else {
				btnHtm += '<input type="button" value="Reviewer Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'R\')"/>&nbsp;&nbsp;&nbsp;';
			}
			
			btnHtm +='<input type="button" value="All" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="fn.sendEditor(\'WR\')"/>&nbsp;&nbsp;&nbsp;';

			alertify.myAlert(btnHtm);
		},
		sendEditor : function(type){
			//코멘트 저장
			var editorVal = CKEDITOR.instances.editor.getData();
			var param = {referenceId : $('input[name=partnerId]').val(), referenceDiv :'20', contents : editorVal, mailSendType : type};
			
			$.ajax({
				url : '<c:url value="/partner/sendComment"/>',
				type : 'POST',
				dataType : 'json',
				cache : false,
				data : param,
				success : function(json){
					if(json.isValid == 'false'){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						$('.ajs-close').trigger("click");
						alertify.success('<spring:message code="msg.project.sent.comments.success" />');
						resetEditor(CKEDITOR.instances.editor);
						$(".commentBtn open").trigger( "click" );
					}
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		modifyComment : function(obj){
			$('.commModifyPop').show();
			$('#blind_wrap').show();
			var commId = $(obj).next().next().val();
			modifyCommentId = commId;
			var contents = $(obj).parent().parent().next().html();
			CKEDITOR.instances.editor3.setData(contents);
			
			$('.closeModComment').click(function(){
				$('.commModifyPop').hide();
				$('#blind_wrap').hide();	
			})
			
			$('.modifyComment').click(function(){
				var editorVal = CKEDITOR.instances.editor3.getData();
				var register = '${sessUserInfo.userId}';
				var param = {commId : modifyCommentId, referenceId : $('input[name=partnerId]').val(), referenceDiv :'20', contents : editorVal};

				$.ajax({
					url : '<c:url value="/partner/saveComment"/>',
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : param,
					success : function(json){
						if(json.isValid) {
							if(json.isValid == 'false') {
								createValidMsgComplex(json);
								alertify.error('<spring:message code="msg.common.valid" />', 0);
								
							} else if(json.isValid == 'true') {
								alertify.alert('<spring:message code="msg.common.success" />', function(){
								});
							}	
						} else {
							//코멘트 임시저장
							$('.modifyComment').click(function(){
								var editorVal = CKEDITOR.instances.editor3.getData();
								var register = '${sessUserInfo.userId}';
								var param = {commId : commentIdx, referenceId : $('input[name=partnerId]').val(), referenceDiv :'13', contents : editorVal};

								$.ajax({
									url : '<c:url value="/partner/saveComment"/>',
									type : 'POST',
									dataType : 'json',
									cache : false,
									data : param,
									success : function(json){
										if(json.isValid){
											if(json.isValid == 'false') {
												createValidMsgComplex(json);
												alertify.error('<spring:message code="msg.common.valid" />', 0);
												
											} else if(json.isValid == 'true') {
												alertify.alert('<spring:message code="msg.common.success" />', function(){
												});
											}	
										}else{
											commentIdx = json.commId;
										}
										
									},
									error : function(){
										alertify.error('<spring:message code="msg.common.valid2" />', 0);
									}
								});
							});
						}
						
						$('.commModifyPop').hide();
						$('#blind_wrap').hide();
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				})
			});
		},
		reset : function(){
			alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
				if (e) {
					$("#list").jqGrid('clearGridData');
				} else {
					return false;
				}
			});
		},
		delete : function(){
			if (fn.checkStatus()){
				var innerHtml = '<div class="grid-container" style="width:470px; height:350px;"><spring:message code="msg.partner.delete.warning" />';
				innerHtml    += '	<div class="grid-width-100" style="width:470px; height:310px; margin-top:10px;">';
				innerHtml    += '		<div id="editor2" style="width:470px; height:300px;">' + CKEDITOR.instances['editor'].getData() + '</div>';
				innerHtml    += '	</div>';
				innerHtml    += '</div>';
				
				alertify.confirm(innerHtml, function () {
					if(CKEDITOR.instances['editor2'].getData() == "") {
						alertify.alert('<spring:message code="msg.project.required.comments" />', function(){});

						return false;
					} else {
						var partnerId = $('input[name=partnerId]').val();
						$.ajax({
							url : '<c:url value="/partner/delAjax"/>',
							type : 'POST',
							dataType : 'json',
							cache : false,
							data : {'partnerId' : partnerId, userComment : CKEDITOR.instances['editor2'].getData()},
							success: function(data){
								if(partnerId) {
									deleteTabInFrame('#<c:url value="/partner/edit/'+partnerId+'"/>');			
								} else {
									deleteTabInFrame('#<c:url value="/partner/edit"/>');
								}
								
								reloadTabInframe('<c:url value="/partner/list"/>');
							},
							error: function(){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					}
				});

				var _editor = CKEDITOR.instances.editor2;
				
				if(_editor) {
					_editor.destroy();
				}
				
				CKEDITOR.replace('editor2', {});
			}else{
				alertify.alert('<spring:message code="msg.partner.warning" />', function(){});
			}
		},
		deleteConfirmationFile : function(obj){
			$('.confirmationUpload').children().remove();
			$('.confirmationUpload').append(''
					+'<span class="fileex_back">'
					+'<div id="confirmationFile">+ Add file</div>'
					+'<input type="hidden"  name="confirmationFileId"/>'
				+'</span>');

			var uploadObj = $('#confirmationFile').uploadFile();

			evt.init();
		},
		deleteOssFile : function(obj){
			$('.ossUpload').children().remove();
			$('.ossUpload').append(''
					+'<span class="fileex_back">'
					+'<div id="ossFile">+ Add file</div>'
					+'<input type="hidden" name="ossFileId"/>'
				+'</span>');
			
			evt.init();
		},
		makeOssList : function(data){
			// 서브 그리드 url 전송 설정(false 전송 안함)
			partyMainData = data.mainData;
			partySubData = data.subData;
			
			// 리로드 대신 그리드 삭제 후 다시 그리기
			$("#list").jqGrid('GridUnload');
			
			grid.init();

			// totla record 표시
			$("#list_toppager_right, #pager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+partyMainData.length+'</div>');
		},
		displayButton : function(cellvalue){
			var icon1 = "<input type=\"button\" value=\"Delete\" class=\"btnCLight gray wauto\" onclick=\"fn.deleteOssComponents(this)\"/>";

			return icon1;
		},
		deleteOssComponents : function(obj){
			var id = $(obj).parent().parent().attr('id');

			$('#list').jqGrid('delRowData',id);
		},
		binaryTab : function(){
			window.open('https://github.com/fosslight/fosslight_binary_scanner', '_blank');
		},
		selectDivision : function(){
			var division = $('#userDivision').val();
			$('#userName').children().remove();
			if(division == "") {
				$("#userName").val("").prev().text("select User").change();
				return false;
			}
			$('#userName').attr('disabled', false);
			
			$.ajax({
				url : '<c:url value="/partner/getUserList"/>',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : {'division' : division},
				success : function(data){					
					data.forEach(function(obj){
						$('#userName').append('<option value='+obj.userId+'>'+obj.userName+'('+obj.userId+')</option>');
					});
					
					$('#userName').change();
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		//requestReview
		requestReview : function(){
			if (fn.checkStatus()){
				isStatusChangeFlag = true;
				
				fn.save();
			}else{
				alertify.alert('<spring:message code="msg.partner.warning" />', function(){});
			}
		},
		//reject
		reject : function(){
			if (fn.checkStatus()){
				var innerHtml = '<div class="grid-container" style="width:470px; height:350px;">Are you sure you want to reject?';
				innerHtml    += '	<div class="grid-width-100" style="width:470px; height:310px; margin-top:10px;">';
				innerHtml    += '		<div id="editor2" style="width:470px; height:300px;">' + CKEDITOR.instances['editor'].getData() + '</div>';
				innerHtml    += '	</div>';
				innerHtml    += '</div>';
				
				alertify.confirm(innerHtml, function () {
					if(CKEDITOR.instances['editor2'].getData() == "") {
						alertify.alert('<spring:message code="msg.project.required.comments" />', function(){});

						return false;
					} else {
						var param = {status : 'PROG', partnerId : '${detail.partnerId}', userComment : CKEDITOR.instances['editor2'].getData()};

						$.ajax({
							url : '<c:url value="/partner/changeStatus"/>',
							type : 'POST',
							data : param,
							dataType : 'json',
							cache : false,
							success : function(data){
								reloadTabInframe('<c:url value="/partner/list"/>');
								
								if('${detail.partnerId}' != ''){
									reloadTabInframe('<c:url value="/partner/edit/${detail.partnerId}"/>');
								}
							},
							error : function(){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					}
				});

				var _editor = CKEDITOR.instances.editor2;
				
				if(_editor) {
					_editor.destroy();
				}
				
				CKEDITOR.replace('editor2', {});
			}else{
				alertify.alert('<spring:message code="msg.partner.warning" />', function(){});
			}
		},
		//reviewStart
		reviewStart : function(){
			if (fn.checkStatus()){
				var param = {status : 'REV', partnerId : '${detail.partnerId}'};
				$.ajax({
					url : '<c:url value="/partner/changeStatus"/>',
					type : 'POST',
					data : param,
					dataType : 'json',
					cache : false,
					success : function(data){
						reloadTabInframe('<c:url value="/partner/list"/>');
						
						if('${detail.partnerId}' != ''){
							reloadTabInframe('<c:url value="/partner/edit/${detail.partnerId}"/>');	
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}else{
				alertify.alert('<spring:message code="msg.partner.warning" />', function(){});
			}
		},
		//confirm
		confirm : function(){
			if (fn.checkStatus()){
				cleanErrMsg();
				checkObligationFlag = false;

				var _list = $("#list");
				var mainData = _list.jqGrid('getGridParam','data');
				
				mainData.forEach( function(_rowData){
					if( _rowData['obligationLicense'] == "90" && _rowData['notify'] == "") {
						checkObligationFlag = true;
					}
				});
				
				if(checkObligationFlag) {
					alert('<spring:message code="msg.warn.include.needcheck.license" />');
					
					return false;
				}
				var param = {status : 'CONF', partnerId : '${detail.partnerId}', userComment : CKEDITOR.instances['editor'].getData()};
				$.ajax({
					url : '<c:url value="${suffixUrl}/partner/changeStatus"/>',
					type : 'POST',
					data : param,
					dataType : 'json',
					cache : false,
					success : function(data){
						if(data.isValid == "false") {
							gridValidMsgNew(data, "list");
							
							alertify.error('<spring:message code="msg.common.valid" />', 0);
						} else {
							reloadTabInframe('<c:url value="/partner/list"/>');
							
							if('${detail.partnerId}' != ''){
								reloadTabInframe('<c:url value="/partner/edit/${detail.partnerId}"/>');	
							}
						}
						
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}else{
				alertify.alert('<spring:message code="msg.partner.warning" />', function(){});
			}
		},
		closePop : function(){
			$('.sheetSelectPop').hide();
		},
		getSheetData : function(){
			var sheetNum = [];
			$('input:checkbox[name="sheetNameSelect"]').each(function(){
				if($(this).is(':checked')){
					sheetNum.push($(this).val());
				}
			});
			
			var fileSeq = $('input[name=ossFileId]').val();
			
			if(sheetNum.length == 0){
				alert('<spring:message code="msg.common.check.sheet" />');
				
				return;
			}else{
				loading.show();
				
				fn_grid_com.totalGridSaveMode('list');
				cleanErrMsg("list");
				
				var target = $("#list");
				var mainData = target.jqGrid('getGridParam','data');
				var finalData = {"readType":"partner","prjId" : '${detail.partnerId}', "sheetNums" : sheetNum , "fileSeq" : ""+fileSeq, "mainData" : JSON.stringify(mainData)};

				fn.exeLoadReportData(finalData);
			}
		},
		getCsvData : function(){
			loading.show();

			fn_grid_com.totalGridSaveMode('list');
			cleanErrMsg("list");

			var fileSeq = $('input[name=ossFileId]').val();
			var sheetNum = ["0"];
			var target = $("#list");
			var mainData = target.jqGrid('getGridParam','data');
			var finalData = {"readType":"partner","prjId" : '${detail.partnerId}', "sheetNums" : sheetNum , "fileSeq" : ""+fileSeq, "mainData" : JSON.stringify(mainData)};

			fn.exeLoadReportData(finalData);
		},
		getSpdxSpreadsheetData : function(){
			loading.show();

			fn_grid_com.totalGridSaveMode('list');
			cleanErrMsg("list");

			var fileSeq = $('input[name=ossFileId]').val();
			var sheetNum = ["1", "4"];
			var target = $("#list");
			var mainData = target.jqGrid('getGridParam','data');
			var finalData = {"readType":"partner","prjId" : '${detail.partnerId}', "sheetNums" : sheetNum , "fileSeq" : ""+fileSeq, "mainData" : JSON.stringify(mainData)};

			fn.exeLoadReportData(finalData);
		},
		exeLoadReportData : function(finalData){
			$.ajax({
				url : '<c:url value="${suffixUrl}/project/getSheetData"/>',
				type : 'POST',
				data : JSON.stringify(finalData),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					if("false" == data.isValid) {
						if(data.validMsg) {
							alertify.alert(data.validMsg, function(){});
						} else {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					} else {
						$('.sheetSelectPop').hide();
							
						if(data.externalData) {
							// validData 표시
							partyValidMsgData_e = data.externalData;
							//gridValidMsgNew(partyValidMsgData_e, "list");
						}

						if(data.externalData2) {
							// validData 표시
							partyDiffMsgData_e = data.externalData2;
							//gridDiffMsg(partyDiffMsgData_e, "list");
						}
						
						fn.makeOssList(data.resultData);
						
						if(data.validMsg) {
							alertify.alert(data.validMsg, function(){});
						} else if(data.resultData.systemChangeHisStr && data.resultData.systemChangeHisStr != "") {
							alertify.alert(data.resultData.systemChangeHisStr, function(){});
						}
					}
				},
				error: function(data){
					loading.hide();
					$('.ossUpload').children().remove();
					
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		modeForStatus : function(status){
			var fileDelete = $(".uploadCase .smallDelete");
			var addWatcher = $("#addWatcher");
			var partyDelete = $("#partyDelete");
			var partyReset = $("#partyReset");
			var partySave = $("#partySave");
			var objArr = [];
			
			objArr.push($("#list"));
			
			if(userRole == "ROLE_VIEWER") {
				fileDelete.hide(); addWatcher.hide();
				partyDelete.hide(); partyReset.hide(); partySave.hide();
			} else {
				switch(status){
					case "CONF" :
						fileDelete.hide(); addWatcher.show();
						partyDelete.hide(); partyReset.hide(); partySave.hide();

						break;
					// 추후 추가
					default :
						fileDelete.show(); addWatcher.show();
						partyDelete.show(); partyReset.show(); partySave.show();

						break;
				}
			}
		},
		
		// 그리드 체크 메세지( gridStr 그리드 문자열 )
		partnerGridValidMsg : function(msgData) {
			$.each(msgData,function(key,value) {
				if("isValid" != key) {
					if($('input[name='+key+']').length > 0) {
						$('input[name='+key+']').next("div.retxt").html(value).show();
					} else if($('textarea[name='+key+']').length > 0) {
						$('textarea[name='+key+']').next("div.retxt").html(value).show();
					} 
				}
			});
		},
		
		// 왓쳐 엘리먼트 그리기
		addHtml : function(target, str, division, userId){
			var rlt = division+((userId!="") ? "/"+userId : "");
			var html  = '<span><input class="watcherTags" type="text" name="watchers" value="'+rlt+'" style="display: none;"/>';
			html += '<strong>'+str+'</strong>';
			html +='<input type="button" value="Delete" class="smallDelete" onclick="fn.removeWatcher(\''+division+'\',\''+userId+'\');" /></span>';

			target.append(html);
			
			$('div.multiTxtSet2 .smallDelete').on('click', function(){
				$(this).parent().remove();
			});
		},
		addWatcher : function(uDiv, uId, uEmail) {
			var partnerId = "${detail.partnerId}";

			if(partnerId == "") {
				return true;
			}
			
			var data = {"partnerId" : partnerId , "parDivision" : uDiv, "parUserId":uId, "parEmail":uEmail};
			
			$.ajax({
				url : '<c:url value="/partner/addWatcher"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(resultData){
					if(resultData.isValid == "true") {
						alertify.success('<spring:message code="msg.common.success" />');
					}
				},
				error : fn.onError
			});
		},
		removeWatcher : function(uDiv, uId) {
			var uEmail = "";

			if("Email" == uId) {
				uEmail = uDiv;
				uId = "";
				uDiv = "";
			}

			var partnerId = "${detail.partnerId}";

			if(partnerId == "") {
				return true;
			}
			
			var data = {"partnerId" : partnerId , "parDivision" : uDiv, "parUserId":uId, "parEmail":uEmail};

			$.ajax({
				url : '<c:url value="/partner/removeWatcher"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(resultData){
					if(resultData.isValid == "true") {
						alertify.success('<spring:message code="msg.common.success" />');
					}
				},
				error : fn.onError
			});
		},
		copyWatcher : function(obj) {
			var partnerId = "${detail.partnerId}";
			
			obj["partnerId"] = partnerId;
			
			$.ajax({
				url : '<c:url value="/partner/copyWatcher"/>',
				type : 'POST',
				data : JSON.stringify(obj),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(resultData){
					var copyWatcher = resultData.copyWatcher;
					
					if(copyWatcher.length){
						for(var i = 0, len = copyWatcher.length ; i < len ; i++){
							var isNew = true
							  , division = copyWatcher[i].division || ""
							  , divisionName = copyWatcher[i].parDivisionName || ""
							  , userId = copyWatcher[i].parUserId || ""
							  , userName = copyWatcher[i].parUserName || ""
							  , email = copyWatcher[i].parEmail || ""
							  , deptUseYn = copyWatcher[i].deptUseYn || "Y"
							  , userUseYn = copyWatcher[i].userUseYn || "Y";
							
							$(".watcherTags").each(function(i, tag){
								var tagDiv = $(tag).val().split("/")[0]
								  , tagUid = $(tag).val().split("/")[1];
								
								if(division == tagDiv) {
									if(tagUid == 'all') { // 선택된 태그 중에 all이 있을 경우 등록 안함
										isNew = false;

										return false;
									} else if(tagUid == userId) {
										isNew = false;	// 이미 등록된 watcher가 있을경우
									}
								}
								
								if(tagUid == "Email") {
									if(email == tagDiv) {
										isNew = false;
									}
								}
							});
							
							if(isNew){
								if(email != "") {
									fn.addHtml($("#nameSpace"), email, email, "Email");
								} else {
									var str = "";
									
									if(userName == "") {
										str = divisionName;
									} else {
										str = '<b';

										if(divisionEmptyCd != divVal) {
											if(deptUseYn == "N"){
												str += ' class="deleteUser"';
											}
	
											str += '>'+divisionName+'</b>/<b';
										}
										
										if(userUseYn == "N"){
											str += ' class="deleteUser"';
										}
										
										str += '>'+userName+'</b>';
									}
									
									fn.addHtml($("#nameSpace"), str, division, userId);
								}
							}
						}
						
						if(partnerId) {
							alertify.success('<spring:message code="msg.common.success" />');
						}
					}
					
					if(!copyWatcher.length)
						alertify.warning("<spring:message code='msg.partner.id.warning' />");
				},
				error : fn.onError
			});
		},
		//sample download
		sampleDownload : function(type){
			var logiPath = "";
			var fileName = "";

			if(sampleFile.length > 0) {
				for(var i=0; i < sampleFile.length; i++) {
					if(type == "arg" && sampleFile[i].cdDtlNo == '10') {
						logiPath = sampleFile[i].cdDtlExp ;
						fileName = sampleFile[i].cdDtlNm;

						break;
					} else if(type == "chk" && sampleFile[i].cdDtlNo == '11') {
						logiPath = sampleFile[i].cdDtlExp ;
						fileName = sampleFile[i].cdDtlNm;

						break;
					}
				}
				location.href = '<c:url value="/partner/sampleDownload?fileName='+fileName+'&logiPath='+logiPath+'"/>';
			} else {
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		},
		downloadExcel : function() {
			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"partnerCheckList", "parameter":'${detail.partnerId}'}),
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
        downloadYaml: function () {
            var params = {'partnerId': '${detail.partnerId}', 'partnerName': '${detail.partnerName}'};

            $.ajax({
                type: "POST",
                url: '<c:url value="/partner/makeYaml"/>',
                data: JSON.stringify(params),
                dataType: 'json',
                cache: false,
                contentType: 'application/json',
                success: function (data) {
                    if ("false" == data.isValid) {
                        alertify.error('<spring:message code="msg.common.valid2" />', 0);
                    } else {
                        window.location = '<c:url value="/exceldownload/getFile?id='+data.validMsg+'"/>';
                    }
                },
                error: function (data) {
                    alertify.error('<spring:message code="msg.common.valid2" />', 0);
                }
            });
        },
		displayNotify : function(cellvalue, options, rowObject){
			var display = "";
			var obligationLicense = rowObject["obligationLicense"];
			var obligationType = rowObject["obligationType"];
			
			if(obligationLicense == 10 || obligationLicense == 11) {
				display="<span class=\"iconSet ops\"></span>";
			} else if(obligationLicense == 90) {
				display = '<select id="'+options.rowId+'_notify" onchange="fn.onNotifyCboxClick(' + options.rowId + ')">'
					+ '<option value=""></option>'
					+ '<option value="Y" '+((rowObject["obligationType"]=='10' || rowObject["obligationType"]=='11') ? ' selected="selected"' : '') +'>O</option>'
					+ '<option value="N" '+((rowObject["obligationType"]=='99') ? ' selected="selected"' : '') +'>X</option>'
					+ '</select>';
			}
			
			return display;
		},
		displaySource : function(cellvalue, options, rowObject){
			var display = "";
			var obligationLicense = rowObject["obligationLicense"];
			var obligationType = rowObject["obligationType"];
			
			if(obligationLicense == 11) {
				display="<span class=\"iconSet man\"></span>";
			} else if(obligationLicense == 90) {
				display = '<select id="'+options.rowId+'_source" onchange="fn.onSourceCboxClick(' + options.rowId + ')">'
					+ '<option value=""></option>'
					+ '<option value="Y" '+((rowObject["obligationType"]=='11') ? ' selected="selected"' : '') +'>O</option>'
					+ '<option value="N" '+((rowObject["obligationType"]=='99' || rowObject["obligationType"]=='10') ? ' selected="selected"' : '') +'>X</option>'
					+ '</select>';
			}
			
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
		onNotifyCboxClick : function(rowId){
			var id = (typeof(rowId) == 'object') ? rowId.id : rowId;
			var selectedVal = $("#"+id+"_notify").val();
			var selectedVal2 = $("#"+id+"_source").val();

			fn_grid_com.saveCellData("list",id,'notify',selectedVal,partyValidMsgData_e, partyDiffMsgData_e);

			if(selectedVal) {
				if(selectedVal == "N") {
					selectedVal2 = "N";
				}
				
				$("#"+id+"_notify").parent().css("background", "");
				
				if($("#"+id+"_source").val()) {
					$("#"+id+"_source").parent().css("background", "");
				}
			} else {
				selectedVal2 = "";
				$("#"+id+"_notify").parent().css("background", "CornflowerBlue");
				$("#"+id+"_source").parent().css("background", "CornflowerBlue");
			}
			
			if(selectedVal2 != $("#"+id+"_source").val()) {
				$("#"+id+"_source").val(selectedVal2).trigger('change');
			}
		},
		onSourceCboxClick : function(rowId){
			var id = (typeof(rowId) == 'object') ? rowId.id : rowId;
			var selectedVal = $("#"+id+"_source").val();
			var selectedVal2 = $("#"+id+"_notify").val();
			
			fn_grid_com.saveCellData("list",id,'source',selectedVal,partyValidMsgData_e, partyDiffMsgData_e);
			
			if(selectedVal) {
				if(selectedVal == "Y") {
					selectedVal2 = "Y";
				}
				
				$("#"+id+"_source").parent().css("background", "");
				
				if($("#"+id+"_notify").val()) {
					$("#"+id+"_notify").parent().css("background", "");
				}
			} else {
				selectedVal2 = "";
				
				$("#"+id+"_notify").parent().css("background", "CornflowerBlue");
				$("#"+id+"_source").parent().css("background", "CornflowerBlue");
			}
			
			if(selectedVal2 != $("#"+id+"_notify").val()) {
				$("#"+id+"_notify").val(selectedVal2).trigger('change');
			}

		},
		gridHeaderResize : function(rowId){
			// 그리드 그룹 헤더 복원
			$("#list").jqGrid('destroyGroupHeader', false);
			// 그리드 그룹 헤더 설정
			$("#list").jqGrid('setGroupHeaders', {
				useColSpanStyle: true, 
				groupHeaders:[
					{startColumnName: 'notify', numberOfColumns: 2, titleText: '<label style="font-weight: bold;">Obligation</label>'},
				]
			});
			
			$("#list_source").css({'border-right': '1px solid #cbc7bd'}); // restriction 을 추가하면서 css에서 마지막 th의 border를 삭제하는 처리의 차선책
		},
		chkListValidation : function(listKind, listId){
			if(listKind == ""){
				// TODO : 추후 문구 수정예정
				alertify.error('<spring:message code="msg.project.watcher.selectlist" />', 0);

				return false;
			}
			
			if(listId == ""){
				// TODO : 추후 문구 수정예정
				alertify.error('<spring:message code="msg.project.watcher.required.copyid" />', 0);

				return false;
			}
			
			return true;
		},
		deleteDocumentsFile : function(obj){
			var fileSeq = $(obj).closest("li").find("input:last").val();
			$(obj).closest('li').remove();
			$("#documentsFile").show();
			delDocumentsFile.push(fileSeq);
			$("#delDocumentsFile").val(delDocumentsFile);
		},
		CheckChar : function(){
			if(event.keyCode == 64){//@ 특수문자 체크
        		alertify.alert('<spring:message code="msg.login.check.char" />', function(){});
        		event.returnValue = false;
        	}
		},
		CheckOssViewPage : function(){
			if(saveFlag) {
				if(_popupCheckOssName != null){
					_popupCheckOssName.close();
				}
				
				_popupCheckOssName = window.open("/oss/checkOssName?prjId=${detail.partnerId}&referenceDiv=20&targetName=partner", "Check OSS Name", "width=1100, height=550, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes");

				if(!_popupCheckOssName || _popupCheckOssName.closed || typeof _popupCheckOssName.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			} else {
				alertify.alert('<spring:message code="msg.project.required.checkOssName" />', function(){});

				return false;
			}
			
			
		},
		CheckOssLicenseViewPage : function(){
			if(saveFlag) {
				if(_popupCheckOssLicense != null){
					_popupCheckOssLicense.close();
				}
				
				_popupCheckOssLicense = window.open("/oss/checkOssLicense?prjId=${detail.partnerId}&referenceDiv=20&targetName=partner", "Check License", "width=1100, height=550, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes");

				if(!_popupCheckOssLicense || _popupCheckOssLicense.closed || typeof _popupCheckOssLicense.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			} else {
				alertify.alert('<spring:message code="msg.project.required.checkOssLicense" />', function(){});

				return false;
			}
		},
		checkStatus : function(){
			var partnerId = $("input[name=partnerId]").val();
			var returnFlag = false;

			if (partnerId||"" == ""){
				returnFlag = true;
			}else{
				$.ajax({
					url : '<c:url value="/partner/checkStatus/'+partnerId+'"/>',
					type : 'GET',
					dataType : 'json',
					cache : false,
					async : false,
					success : function(data){
						var status = data.status;
						var editStatus = $("input[name=status]").val();

						returnFlag = (status == editStatus);
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
						returnFlag = false;
					}
				});
			}

			return returnFlag;
		},
		shareUrl : function(){
			var copyUrl = "";
			var protocol = window.location.protocol;
			var host =  window.location.host;
			copyUrl = protocol + "//" + host + "/partner/view/${detail.partnerId}";
			$("#copyUrl").val(copyUrl);
			
			//launch it.
			var btnHtm = '<b>Share Link</b><br>';
			btnHtm += '<input type="text" value="'+copyUrl+'" style="width:460px;" disabled/><br><br>';
			btnHtm += '<input type="button" value="Copy" class="btnCancel btnColor red right" style="height:30px;width:100px;"onclick="fn.copyUrl(this)"/>';

			if(!alertify.myAlert){
				//define a new dialog
				alertify.dialog('myAlert',function factory(){
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
				}});
			}
			
			alertify.myAlert(btnHtm);
		},
		copyUrl : function(target){
			var copyUrl = document.getElementById( 'copyUrl' );
			copyUrl.select();
	        document.execCommand( 'Copy' );
	        
			$('.ajs-close').trigger("click");
			alertify.success('<spring:message code="msg.common.success" />');
		},
	    bulkEdit : function(){
	    	var gridList = $("#list");
	        var targetGird = "list";

	        var selarrrow = gridList.jqGrid("getGridParam", "selarrrow");
	        var rowCheckedArr = [];
	        for(var i=0; i<selarrrow.length; i++){
				if($("input:checkbox[id='jqg_" + targetGird + "_" + selarrrow[i] + "']").is(":checked")){
					rowCheckedArr.push(selarrrow[i]);
				}
	        }

	        if(rowCheckedArr.length > 0){
	            fn_grid_com.totalGridSaveMode(targetGird);
	            
	            var bulkEditArr = gridList.jqGrid("getGridParam", "selarrrow");
	            var url = '<c:url value="/oss/ossBulkEditPopup?rowId=' + rowCheckedArr + '&target=' + targetGird + '"/>';
	            
	            var _popup = null;

	            if(_popup == null || _popup.closed){
	                _popup = window.open(url, "bulkEditViewPartnerPopup", "width=850, height=380, toolbar=no, location=no, left=100, top=100, resizable=yes");

	                if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
	                    alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
	                }
	            } else {
	                _popup.close();
	                _popup = window.open(url, "bulkEditViewPartnerPopup", "width=850, height=380, toolbar=no, location=no, left=100, top=100, resizable=yes");
	            }
	        }else{
	            alertify.alert('<spring:message code="msg.oss.select.ossTable" />', function(){});
	            return false;
	        }
		}
	}
	
	var datas = {
		init : function(){
			if('${detail.partnerId}' != "") {
				fn.getPartyGridData();
			} else {
				partyMainData = [];
				partySubData = [];
			}
			
			var deliveryForm = '${detail.deliveryForm}';
			
			if(deliveryForm == '') {

			} else {
				$('select[name=deliveryForm]').val(deliveryForm).attr('selected', 'true');
				$('select[name=deliveryForm]').trigger('change');	
			}
		},
		getCommentList : function(){
			$.ajax({
				url : '<c:url value="/partner/getCommentList"/>',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : {referenceId : $('input[name=partnerId]').val()},
				success : function(data){
					$('.commentListArea').children().remove();
					
					if(data.length != 0) {
						for(var i = 0; i < data.length; i++) {
							var commId = data[i].commId;
							$('.commentListArea').append(commentTemp.html());
							var temp = $('dl[name=commentClone]');
							
							if(data[i].status == "" || data[i].status == null || data[i].status == "undefined") {
								temp.find('.nameArea').text(data[i].creator);
							} else {
								temp.find('.nameArea').text(data[i].status).append("</br>"+data[i].creator);
							}
							
							temp.find('.dateArea').text(data[i].createdDate);
							temp.find('.commentContentsArea').html(data[i].contents);
							temp.find('input[name=commId]').val(commId);
							temp.removeAttr('name');
						}	
					} else {
						$('.commentListArea').append('<p class="noneTxt">No comments were registered.</p>');
					}
					
					$('.commentBtn').removeClass('open');
					$('.commentEditor').hide();
					$('.projectContents').removeClass('pt255');
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	}
	var grid = {
		init : function(){
			var currentOssName = '';
			var ondblClickRowBln = false;
			var partnerList = $("#list");
			
			partnerList.jqGrid({
				datatype: 'local',
				data : partyMainData,
				colNames: ['gridId', 'ID_KEY', 'ID', 'ReferenceId', 'ReferenceDiv', 'OssId', 'Binary Name or Source Path', 'OSS Name','OSS Version','LicenseId','License','Download Location'
						   ,'Homepage','Copyright Text', 'CVE ID', 'Vulnera<br/>bility','<input type="checkbox" onclick="fn_grid_com.onCboxClickAll(this,\'list\');">Exclude','LicenseDiv','obligationLicense','ObligationType','Notify','Source','Restriction'],
				colModel: [
					{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
					{name: 'componentId', index: 'componentId', width: 40, align: 'center', hidden:true},
					{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
					{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
					{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
					{name: 'ossId', index: 'ossId', width: 29, align: 'center', editable:true, hidden:true},
					{name: 'filePath', index: 'filePath', width: 140, align: 'left', editable:true, template: searchStringOptions, 
						editoptions: {
							dataInit:
								function (e) { 
									$(e).on("change", function() {
										var rowid = (e.id).split('_')[0];
										fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e, partyDiffMsgData_e);
									});
								}
						}
					},
					{name: 'ossName', index: 'ossName', width: 140, align: 'left', editable:true, edittype:'text', template: searchStringOptions, 
							editoptions: {
								dataInit:
									function (e) { 
										// ossName auto complete
										$(e).autocomplete({
											source: ossNames
											, minLength: 3 // IE 스크립트 성능이슈로 0->3 으로 변경 yuns
											, open: function() { $(this).attr('state', 'open');}
											, close: function () { $(this).attr('state', 'closed');}
										}).focus(function() {
											if ($(this).attr('state') != 'open') {
												$(this).autocomplete("search");
											}
										}).on('autocompletechange', function() {
											if(e.value!=""){
												var rowid = (e.id).split('_')[0];
												fn_grid_com.griOssVersions($('#'+rowid+'_ossVersion')[0], e.value, 'list');
												fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e, partyDiffMsgData_e);
											}
										}).dblclick(function(){
											var rowid = (e.id).split('_')[0];
											var licenseName = com_fn.getLicenseName(partnerList.getRowData(rowid));
											
											partnerList.jqGrid("setCell", rowid, "licenseName", licenseName);
											fn_grid_com.saveCellData(partnerList.attr("id"), rowid, "licenseName", licenseName, null, null);
											partnerList.jqGrid('saveRow',rowid);
											
											fn_grid_com.mvOssPage(partnerList, rowid);
										});
										
										currentOssName = e.value;
									}
							}
					},
					{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'center', editable:true, edittype:'text', template: searchStringOptions, 
						editoptions: {
							dataInit:
								function (e) { 
									fn_grid_com.griOssVersions(e, currentOssName, 'list');
									
									$(e).on( "autocompletechange", function() {
										var rowid = (e.id).split('_')[0];
										fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e, partyDiffMsgData_e);
									});
								}
						}
					},
					{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', editable:true, edittype:'text', hidden:true},
					{name: 'licenseName', index: 'licenseName', width: 150, align: 'left', editable:false, edittype:'text', template: searchStringOptions,
						editoptions: {
							dataInit: function (e) {
									var licenseNameId = $(e).attr("id").split('_')[0];
									var licenseNameTd = $(e).parent();

									var displayLicenseNameCell = '<div style="width:100%; display:table; table-layout:fixed;">';
									displayLicenseNameCell += '<div id="'+licenseNameId+'_licenseNameDiv" style="width:60px; display:table-cell; vertical-align:middle;"></div>';
									displayLicenseNameCell += '<div id="'+licenseNameId+'_licenseNameBtn" style="display:table-cell; vertical-align:middle;"></div>';
									displayLicenseNameCell += '</div>';
						
									$(licenseNameTd).empty();
									$(licenseNameTd).html(displayLicenseNameCell);
									$('#'+licenseNameId+'_licenseNameDiv').append(e);
								
									// licenseName auto complete
									$(e).autocomplete({
										source: licenseNames
										, minLength: 0
										, open: function() { $(this).attr('state', 'open'); }
										, close: function () { $(this).attr('state', 'closed'); }
									}).focus(function() {
										if ($(this).attr('state') != 'open') {
											$(this).autocomplete("search");
										}
									});
									
									// set license data
									$(e).on( "autocompletechange", function() {
										var rowid = (e.id).split('_')[0];
										var mult = null;
										var multText = null;
										
										for(var i in licenseNames){
											if("" != e.value && e.value == licenseNames[i].value){
												var licenseIds = $('#'+rowid+'_licenseId').val();
												mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'><span class=\"btnLicenseShow\" ondblclick='com_fn.showLicenseInfo(this)'>" + licenseNames[i].value + "</span><button onclick='com_fn.deleteLicenseRenewal(this)'>x</button></span><br/>";
												multText = licenseNames[i].value;
												break;
											}
										}
										
										if(mult == null){
											mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'><span class=\"btnLicenseShow\" ondblclick='com_fn.showLicenseInfo(this)'>" + e.value + "</span><button onclick='com_fn.deleteLicenseRenewal(this)'>x</button></span><br/>";
											multText = e.value;
										}
										
										var rowLicenseNames = [];
										$('#'+rowid+'_licenseNameBtn').find('.btnLicenseShow').each(function(i, item){
											rowLicenseNames.push($(this).text());
										});
										
										if (multText != null){
											if(rowLicenseNames.length > 0){
												var duplicateFlag = false;
												for(var i in rowLicenseNames){
													if(multText == rowLicenseNames[i]){
														duplicateFlag = true;
														break;
													}
												}
												
												if(!duplicateFlag){
													$('#'+rowid+'_licenseNameBtn').append(mult);
												}
											} else {
												$('#'+rowid+'_licenseNameBtn').append(mult);
											}
										}
										
										$('#'+rowid+'_licenseName').val("");
										
										fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e, partyDiffMsgData_e);
									}).on("keypress", function(evt){
										if(evt.keyCode == 13){
											var rowid = (e.id).split('_')[0];
											var mult = null;
											var multText = null;
											
											for(var i in licenseNames){
												if("" != e.value && e.value == licenseNames[i].value){
													var licenseIds = $('#'+rowid+'_licenseId').val();
													mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'><span class=\"btnLicenseShow\" ondblclick='com_fn.showLicenseInfo(this)'>" + licenseNames[i].value + "</span><button onclick='com_fn.deleteLicenseRenewal(this)'>x</button></span><br/>";
													multText = licenseNames[i].value;
													break;
												}
											}
											
											if(mult == null && "" != e.value){
												mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'><span class=\"btnLicenseShow\" ondblclick='com_fn.showLicenseInfo(this)'>" + e.value + "</span><button onclick='com_fn.deleteLicenseRenewal(this)'>x</button></span><br/>";
												multText = e.value;
											}
											
											var rowLicenseNames = [];
											$('#'+rowid+'_licenseNameBtn').find('.btnLicenseShow').each(function(i, item){
												rowLicenseNames.push($(this).text());
											});
											
											if (multText != null){
												if(rowLicenseNames.length > 0){
													var duplicateFlag = false;
													for(var i in rowLicenseNames){
														if(multText == rowLicenseNames[i]){
															duplicateFlag = true;
															break;
														}
													}
													
													if(!duplicateFlag){
														$('#'+rowid+'_licenseNameBtn').append(mult);
													}
												} else {
													$('#'+rowid+'_licenseNameBtn').append(mult);
												}
											}
											
											$('#'+rowid+'_licenseName').val("");
	
											fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e,partyDiffMsgData_e);
										}
									});
								}
						}
					},
					{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', editable:true, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions, 
						editoptions: {
							dataInit:
								function (e) { 
									$(e).on("change", function() {
										var rowid = (e.id).split('_')[0];
										var value = e.value;
										
										if(value.charAt(value.length-1) == "/"){
											value = value.slice(0, -1); // 마지막 문자열 제거
											$("#"+rowid+"_downloadLocation").val(value);
										}
										
										fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e, partyDiffMsgData_e);
									}).on("blur", function() {
										var value = e.value;
										
										if(value.charAt(value.length-1) == "/"){
											value = value.slice(0, -1); // 마지막 문자열 제거
											$("#"+e.id).val(value);
										}
									});
								}
						}
					},
					{name: 'homepage', index: 'homepage', width: 100, align: 'left', editable:true, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions, 
						editoptions: {
							dataInit:
								function (e) { 
									$(e).on("change", function() {
										var rowid = (e.id).split('_')[0];
										var value = e.value;
										
										if(value.charAt(value.length-1) == "/"){
											value = value.slice(0, -1); // 마지막 문자열 제거
											$("#"+rowid+"_homepage").val(value);
										}
										
										fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e, partyDiffMsgData_e);
									}).on("blur", function() {
										var value = e.value;
										
										if(value.charAt(value.length-1) == "/"){
											value = value.slice(0, -1); // 마지막 문자열 제거
											$("#"+e.id).val(value);
										}
									});
								}
						}
					},
					{name: 'copyrightText', index: 'copyrightText', width: 140, align: 'left', editable:false, template: searchStringOptions, edittype:"textarea", editoptions:{rows:"5",cols:"24", 
						dataInit:
							function (e) { 
								$(e).on("change", function() {
									var rowid = (e.id).split('_')[0];
									
									fn_grid_com.saveCellData("list",rowid,e.name,e.value,partyValidMsgData_e);
								});
							}
						}
					},
					
					{name: 'cveId', index: 'cveId', hidden:true},
					{name: 'cvssScore', index: 'cvssScore', width: 80, align: 'center', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : true, sorttype:'float', template: searchNumberOptions},
					{name: 'excludeYn', index: 'excludeYn', width: 50, align: 'center', formatter: fn_grid_com.cboxFormatter, unformat: fn_grid_com.cboxUnFormatter, search: false},
					{name: 'licenseDiv', index: 'licenseDiv', width: 100, align: 'left', editable:false, hidden:true},
					{name: 'obligationLicense', index: 'obligationLicense', width: 40, align: 'center', hidden:true},
					{name: 'obligationType', index: 'obligation', width: 40, align: 'center', hidden:true},
					{name: 'notify', index: 'notify', width: 40, align: 'center', formatter: fn.displayNotify, unformat: fn.unDisplayNotify, sortable : false, search : false},
					{name: 'source', index: 'source', width: 40, align: 'center', formatter: fn.displaySource, unformat: fn.unDisplaySource, sortable : false, search : false},
					{name: 'restriction', index: 'restriction', width: 70, align: 'center', formatter: fn_grid_com.displayLicenseRestriction, unformat: fn_grid_com.unformatter, sortable : false, search : false}
				],
				autoencode: true,
				editurl:'clientArray',
				autowidth: true,
				height: 'auto',
				gridview: true,
				pager: '#pager',
				rowNum: 200,
				rowList: [200, 500, 1000, 5000],
				recordpos:'right',
				toppager:true,
				loadonce:true,
				cellEdit : true,
				cellsubmit : 'clientArray',
				ignoreCase: true,
				multiselect: true,
				onSortCol: function (index, columnIndex, sortOrder) {
					isSort = true;
				},
				loadComplete: function(data) {
					_mainLastsel = -1;
					
					if(data.records > 0) {
						var multRowIds = []; 
						var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row, rowid, rowData, className;
						for(var _idx=0;_idx<rowsCount;_idx++) {
							row = rows[_idx];
							className = row.className;
							if (className.indexOf('jqgrow') !== -1) {
								rowid = row.id;
								rowData = data.rows[rowIdx++];
								
								if(rowData.licenseDiv != "M") {
									className = className + ' singleLicenseClass';
									
									$("#" + rowid).next("tr.ui-subgrid").hide();
								} else {
									multRowIds.push(rowid);
								}
								
								if(rowData.excludeYn == "Y" && className.indexOf('excludeRow') === -1) {
									className= className + ' excludeRow';
								}
								
								row.className = className;
								
							} else if(className.indexOf('ui-subgrid') !== -1){
								rowIdx++;
							}

							// checkbox click event
							$("#"+row.id).find("input[type=checkbox]").removeClass("cbox");
						}
						
						// 한번에 처리
						$("#list tr.singleLicenseClass").find("td:first").removeClass("sgexpanded sgcollapsed").find("a").hide();
						
						tableRefresh();
					}
					
					// 상태에 따른 화면 처리
					fn.modeForStatus('${detail.status}');
					
					if(isSort){
						isSort = false;
					}
					
					fn.gridHeaderResize();
				},
				onSelectRow: function(rowid,status,eventObject) {
				},
				beforeSelectRow: function(rowid, e) {
					var $self = $(this), iCol, cm,
				    $td = $(e.target).closest("tr.jqgrow>td"),
				    $tr = $td.closest("tr.jqgrow"),
				    p = $self.jqGrid("getGridParam");

				    if ($(e.target).is("input[type=checkbox]") && $td.length > 0) {
				       iCol = $.jgrid.getCellIndex($td[0]);
				       cm = p.colModel[iCol];
				       if (cm != null && cm.name === "cb") {
				           // multiselect checkbox is clicked
				           $self.jqGrid("setSelection", $tr.attr("id"), true ,e);
				       }
				    }
					// 경고 클래스 설정
				    fn_grid_com.setWarningClass(partnerList,rowid,["ossName","licenseName"]);
				    return false;
				},
				onCellSelect: function(rowid,iCol,cellcontent,e) {
					if(iCol=="3") {
						com_fn.exitCell(_mainLastsel, "list");
						
						fn_grid_com.showOssViewPage(partnerList, rowid, true, partyValidMsgData_e, partyDiffMsgData_e, null, com_fn.getLicenseName);
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					if(iCol!="17") {
						cleanErrMsg("list", rowid);
						fn_grid_com.setCellEdit(partnerList, rowid, partyValidMsgData_e, partyDiffMsgData_e, null, com_fn.getLicenseName);

						// 서브 그리드 제외
						ondblClickRowBln = false;
						
						$('#'+rowid+'_licenseName').addClass('autoCom');
						$('#'+rowid+'_licenseName').css({'width' : '60px'});
						var result = $('#'+rowid+'_licenseName').val().split(",");

						result.forEach(function(cur,idx){
							if(cur != ""){
								var mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'><span class=\"btnLicenseShow\" ondblclick='com_fn.showLicenseInfo(this)'>" + cur + "</span><button onclick='com_fn.deleteLicense(this)'>x</button></span><br/>";
								$('#'+rowid+'_licenseName').parent().append(mult);
							}
						});
                                                var nextCol = partnerList.jqGrid('getGridParam', 'colModel')[iCol].name
                                                var nextRow = rowid
                                                $('#'+nextRow+"_"+nextCol).focus();
						$('#'+rowid+'_licenseName').val("");
					}
				},
				onPaging: function(action) {
					cleanErrMsg("list");
					fn_grid_com.totalGridSaveMode('list');
				},
				gridComplete : function() {
					cleanErrMsg("list");

					if(partyValidMsgData_e) {
						gridValidMsgNew(partyValidMsgData_e, "list");
					}

					if(partyDiffMsgData_e) {
						gridDiffMsg(partyDiffMsgData_e, "list");
					}
					
					var arr = [];
					arr = partnerList.jqGrid('getDataIDs');

					for(var i in arr){
						if(partnerList.jqGrid('getCell',arr[i],'obligationType') == 90) {
							$("#"+arr[i]+"_source").parent().css("background", "CornflowerBlue");
							$("#"+arr[i]+"_notify").parent().css("background", "CornflowerBlue");
						}
					}
				},
				removeHighLight : true

			});
			partnerList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
			partnerList.jqGrid('navGrid',"#pager",{add:true,edit:false,del:true,search:false,refresh:false
													  , addfunc: function () { saveFlag = false; fn_grid_com.rowAddNew('list',partnerList,"main", null, com_fn.getLicenseName);}
													  , delfunc: function () { fn_grid_com.rowDelNew(partnerList,"main");}
													  , cloneToTop:true
			});
			
			$('#list').closest(".ui-jqgrid-bdiv").css({"height":"500px", "overflow-y" : "scroll"});
		}
	}

	var com_fn = {
		deleteLicense : function(target){
			$(target).parent().remove();
		},
		deleteLicenseRenewal : function(target){
			$(target).parent().next().remove();
			$(target).parent().remove();
		},
		getLicenseName : function(obj){
			return obj.licenseName.replace(/(<([^>]+)>)/ig, ",").split(",").reduce(function(arr, cur){
				if(cur.toUpperCase() != "X" && cur != ""){
					arr.push(cur);
				}

				return arr;
			}, []).join(",");
		},
		exitCell : function(_mainLastsel, target){
			if(_mainLastsel != -1){
				var grid = $("#" + target);
				var licenseName = com_fn.getLicenseName(grid.getRowData(_mainLastsel));

				grid.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
				fn_grid_com.saveCellData(grid.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
				grid.jqGrid('saveRow',_mainLastsel);
			}
		},
		showLicenseInfo : function(obj){
			var licenseName = $(obj).text();

			$.ajax({
				url : '<c:url value="/license/getLicenseId"/>',
				type : 'POST',
				data : {"licenseName" : licenseName},
				dataType : 'json',
				cache : false,
				success : function(data){
					var _frameId = data.licenseId + "_License";
					var _frameTarget = "#<c:url value='/license/edit/" + data.licenseId + "'/>";
					createTabInFrame(_frameId, _frameTarget);
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		bulkEditOssInfo : function(obj){
			var editFlag = false;
			try{
				var ossArr = [];
		        var rowId = obj["rowId"];
		        var target = obj["target"];
		        var param = $('#'+target).jqGrid('getGridParam','data');
		        
		        if(rowId.indexOf(",") > -1){
		            ossArr = rowId.split(",");
		            for(var idx in ossArr){
		                com_fn.bulkEditSetCell(target, ossArr[idx], obj);

		                for (var i=0; i<param.length; i++){
		                    if(param[i]["gridId"] == ossArr[idx]){
		                        for(var key in obj){
		                            if(key != "rowId" && key != "target"){
		                            	param[i][key] = obj[key];
		                            }
		                        }
		                    }
		                }
		            }
		        }else{
		            com_fn.bulkEditSetCell(target, rowId, obj);

		            for (var i=0; i<param.length; i++){
		                if(param[i]["gridId"] == rowId){
		                    for(var key in obj){
		                        if(key != "rowId" && key != "target"){
		                        	param[i][key] = obj[key];
		                        }
		                    }
		                }
		            }
		        }

		        $("#"+target).jqGrid('setGridParam', {data:param}).trigger('reloadGrid');
			}catch(e){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
	    		editFlag = true;
	    	}finally{
	    		if(!editFlag){
		    		alertify.success('<spring:message code="msg.common.success" />');
	    		}
	       	}
	    },
	    bulkEditSetCell : function (target, rowId, obj){
	        for(var key in obj){
	            if(key != "rowId" && key != "target"){
	            	$('#'+target).jqGrid('setCell', rowId, key, obj[key]);
	            }
	        }
	    },
	    bulkEditDelRow : function (target, rowId, flag){
	    	var delFlag = false;
			try{
				var selrow = "";
				var param = $("#"+target).jqGrid('getGridParam', 'data');
				
		    	if(rowId.indexOf(",") > -1){
		    		selrow = rowId.split(",");
		    		for (var i=0; i<selrow.length; i++){
		    			$("#"+target).jqGrid('delRowData', selrow[i]);
		    			param = com_fn.bulkEditDeleteLocalDataAfterDelRow(param, selrow[i]);
		        	}
		        }else{
		        	$("#"+target).jqGrid('delRowData', rowId);
		        	param = com_fn.bulkEditDeleteLocalDataAfterDelRow(param, rowId);
		        }

		        if(flag == "main"){
		        	$("#"+target).jqGrid('GridUnload');

		        	partyMainData = param;
		        	grid.init();

		        	// total record 표시
					$("#list_toppager_right, #pager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+partyMainData.length+'</div>');
		        }
			}catch(e){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
				delFlag = true;
	    	}finally{
	    		if(!delFlag){
		    		alertify.success('<spring:message code="msg.common.success" />');
	    		}
	       	}
	    },
	    bulkEditDeleteLocalDataAfterDelRow : function (dataArray, rowId){
	    	var reMakeArrObj=[];
	    	var newIdx = 0;

	    	for(var idx=0; idx < dataArray.length; ++idx) {
				if(dataArray[idx].gridId != rowId) {
					reMakeArrObj[newIdx++] = dataArray[idx];
				}
			}
			
			return reMakeArrObj;
	    }
	}
//]]>
</script>