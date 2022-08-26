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
var sampleFile =  ${ct:getAllValuesJson(ct:getConstDef('CD_SAMPLE_FILE'))};
var ossAnalysisStatus;
var analysisStartDate;
var partnerStatus = "${detail.status}";

	$(document).ready(function () {
		'use strict';
        if('${detail.viewOnlyFlag}' == 'N') {
            var copyUrl = "";
            var protocol = window.location.protocol;
            var host =  window.location.host;
            copyUrl = protocol + "//" + host + "/index?id=" + '${detail.partnerId}' + "&project=false";
            window.location.href = copyUrl;
        }
		
	<c:if test="${empty message}">
		evt.init();
		evt.tabInit();
		datas.init();
		grid.init();

		<c:if test="${batFlag}">
		//if('${detail.partnerId}' != ""){
			//bat_evt.init();
		//}
		</c:if>

		if(ossAnalysisStatus.toUpperCase() == "SUCCESS" && '${detail.status}' != "CONF"){
			$(".idenAnalysisResult").show();
		}
	</c:if>
	});
	
	var commentTemp = '';
	var modifyCommentId = '${detail.comment}';
	var commentIdx= '${detail.comment}';
	
	var evt = {
		init : function(){
			commentTemp = $('<div>').append($('dl[name=commentClone]').clone());
			$('dl[name=commentClone]').remove();
			
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
			
			
			$('.btnCommentHistory').on('click', function(e){
	            e.preventDefault();
	            openCommentHistory('<c:url value="/comment/popup/3rd/${detail.partnerId}"/>');
	        });
			
			$(window).resize(function(){
				fn.gridHeaderResize();
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
		// party 그리드 데이터
		getPartyGridData : function(){
			$.ajax({
				url : '<c:url value="${suffixUrl}/project/identificationGrid/${detail.partnerId}/20"/>',
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
					
					//fn_grid_com.addEtcKeyDownEvent($('#list'), partyValidMsgData_e, partyDiffMsgData_e, null, com_fn.getLicenseName);
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		
		deleteComment : function(obj){
			if(!confirm("Are you sure you want to delete this comment?")) return;
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
		binaryTab : function(){
			createTabInFrame('BAT List', '#<c:url value="/bat/list"/>');
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
			html += '<strong>'+str+'</strong></span>';

			target.append(html);
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
		displayPartnerStatus : function(){
			var status = "";
			
			switch(partnerStatus){
				case "${ct:getConstDef('CD_DTL_PARTNER_STATUS_PROGRESS')}":
					status = "${ct:getCodeString(ct:getConstDef('CD_PARTNER_STATUS'), ct:getConstDef('CD_DTL_PARTNER_STATUS_PROGRESS'))}";
					$("#partnerStatus").css('background-color','rgb(255, 255, 255)').css('color','rgb(106, 106, 106)');

					break;
				case "${ct:getConstDef('CD_DTL_PARTNER_STATUS_REQUEST')}":
					status = "${ct:getCodeString(ct:getConstDef('CD_PARTNER_STATUS'), ct:getConstDef('CD_DTL_PARTNER_STATUS_REQUEST'))}";
					$("#partnerStatus").css('background-color','rgb(157, 165, 184)').css('color','rgb(255, 255, 255)');

					break;
				case "${ct:getConstDef('CD_DTL_PARTNER_STATUS_REVIEW')}":
					status = "${ct:getCodeString(ct:getConstDef('CD_PARTNER_STATUS'), ct:getConstDef('CD_DTL_PARTNER_STATUS_REVIEW'))}";
					$("#partnerStatus").css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');

					break;
				case "${ct:getConstDef('CD_DTL_PARTNER_STATUS_CONFIRM')}":
					status = "${ct:getCodeString(ct:getConstDef('CD_PARTNER_STATUS'), ct:getConstDef('CD_DTL_PARTNER_STATUS_CONFIRM'))}";
					$("#partnerStatus").css('background-color','rgb(56, 79, 123)').css('color','rgb(255, 255, 255)');

					break;
				default:
					break;
			}

			$("#partnerStatus").val(status);
		}
	};
	
	var datas = {
		init : function(){
			fn.getPartyGridData();

			ossAnalysisStatus = "${detail.ossAnalysisStatus}";
			analysisStartDate = "${detail.analysisStartDate}";

			fn.displayPartnerStatus();
		}
	};
	
	var grid = {
		init : function(){
			var currentOssName = '';
			var ondblClickRowBln = false;
			var partnerList = $("#list");
			
			partnerList.jqGrid({
				datatype: 'local',
				data : partyMainData,
				colNames: ['gridId', 'ID_KEY', 'ID', 'ReferenceId', 'ReferenceDiv', 'OssId', 'Binary Name or Source Path', 'OSS Name','OSS Version','Download Location'
				           ,'Homepage','LicenseId','License','Copyright Text', 'CVE ID', 'Vulnera<br/>bility','<input type="checkbox" onclick="fn_grid_com.onCboxClickAll(this,\'list\');">Exclude','LicenseDiv','obligationLicense','ObligationType','Notify','Source','Restriction'],
				colModel: [
					{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
					{name: 'componentId', index: 'componentId', width: 40, align: 'center', hidden:true},
					{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
					{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
					{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
					{name: 'ossId', index: 'ossId', width: 29, align: 'center', editable:false, hidden:true},
					{name: 'filePath', index: 'filePath', width: 140, align: 'left', editable:false, template: searchStringOptions},
					{name: 'ossName', index: 'ossName', width: 140, align: 'left', editable:false, edittype:'text', template: searchStringOptions},
					{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'center', editable:false, edittype:'text', template: searchStringOptions},
					{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', editable:false, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions},
					{name: 'homepage', index: 'homepage', width: 100, align: 'left', editable:false, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions},
					{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', editable:false, edittype:'text', hidden:true},
	 				{name: 'licenseName', index: 'licenseName', width: 150, align: 'left', editable:false, edittype:'text', template: searchStringOptions},
					{name: 'copyrightText', index: 'copyrightText', width: 140, align: 'left', editable:false, template: searchStringOptions, edittype:"textarea"},
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
						}
						
						// 한번에 처리
						$("#list tr.singleLicenseClass").find("td:first").removeClass("sgexpanded sgcollapsed").find("a").hide();
						
						tableRefresh();
					}
					
					if(isSort){
						isSort = false;
					}
					
					fn.gridHeaderResize();
				},
				onSelectRow: function(rowid,status,eventObject) {
				},
				beforeSelectRow: function(rowid, e) {
					// 경고 클래스 설정
					fn_grid_com.setWarningClass(partnerList,rowid,["ossName","licenseName"]);
					return true;
				},
				onCellSelect: function(rowid,iCol,cellcontent,e) {},
				ondblClickRow: function(rowid,iRow,iCol,e) {},
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
			
			$('#list').closest(".ui-jqgrid-bdiv").css({"height":"500px", "overflow-y" : "scroll"});
		}
	};
//]]>
</script>