<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var codeData;
	var userRole = '${sessUserInfo.authority}';
	var arr = new Array();
	var ossNames = [];
	var objs = [];
	var licenseNames = [];
	var isSort = false;
	var _popupCheckOssName = null;
	var _popupCheckOssLicense = null;
	var saveFlag = false;
	var etcDomain = "${ct:getConstDef('CD_DTL_ECT_DOMAIN')}";
	var divisionUseFlag = "${not empty ct:getCodeValues(ct:getConstDef('CD_USER_DIVISION'))}";
	
	$(document).ready(function() {
		'use strict';
		initSample();

		if($("#editor2").length > 0) {
			initSample2();
		}
		
		if('${project.prjId}' != "") {
			fn_self.modeChange('${project.prjId}', 'v');
			showHelpLink("Self-Check_Edit_Export", "helpLink_vulerabiityExport");
		}
		
		data.init();
		evt.init();
		
		if('${project.prjId}' != "") {
			src_evt.init();
			notice_evt.init();
		}

		if($("#listKind > option").length == 1){
			$(".listKindArea").hide();
		}

		com_fn.tabInit();
		fn.appendEditVisible($("#append"));
		fn.initNotice();
	});
	
	var fn_self ={
		modeChange : function(prjId, type) {
			if("v" == type) {
				$.ajax({
					url : '<c:url value="/selfCheck/selfCheckViewAjax"/>',
					dataType : 'html',
					cache : false,
					data : {prjId : prjId},
					success : function(detailResult){
						$("#divViewMode").html(replaceWithLink(detailResult));
						$("#divViewMode").show();
						$("#divEditMode").hide();
					},
					error : function(request,status,error){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			} else if("c" == type) {
				$("#divViewMode").show();
				$("#divEditMode").hide();
			} else {
				$("#divViewMode").hide();
				$("#divEditMode").show();
			}
		}
	}
	
	// 이벤트
	var evt = {
		init : function(){
			// 저장
			$("#save").click(function(){
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						var prjName = $("[name='prjName']").val();

						if(/[^\s]+/.test(prjName)){
							$(".prjName").hide();
							fn.saveSubmit();
						} else {
							$(".prjName").show();
						}
					} else {
						return false;
					}
				});
			});
			
			$("#cancel").click(function(){
				fn_self.modeChange('${project.prjId}', 'v');
			});
			
			$(".selfCheckDelete").click(function(){
				alertify.confirm('<spring:message code="msg.selfcheck.confirm.remove.project" />', function (e) {
					if (e) {
						fn.deleteSubmit();
					} else {
						return false;
					}
				});
			});
			
			// 직접입력
			$('#osType').change(function(){
				$("#osType option:selected").each(function () {
					$("#osTypeEtc").val('');
					
					if($(this).val()== '999') {
						$("#osTypeEtc").attr("disabled",false);
					} else {
						$("#osTypeEtc").attr("disabled",true);
					}
				});
			});
			
			commonAjax.getCreatorDivisionTags().success(function(data, status, headers, config){
				data.forEach(function(obj,index){
					arr[index] = obj.userId+":"+obj.userName;
				});
			});
		}
	};
	
	var fn = {
		// 그리드 삭제 버튼
		setDelBtn : function(cellvalue, options, rowObject){
			return "<input type=\"button\" value=\"delete\" class=\"btnCLight darkgray\" onclick=\"fn.exeDelete('"+options.rowId+"')\" />";
		},
		// 그리드 삭제
		exeDelete : function(rowId){
			$("#_modelList").jqGrid('delRowData', rowId);
		},
		// 저장
		saveSubmit : function(){
			var editorVal = CKEDITOR.instances.editor.getData();
			$('input[name=comment]').val(replaceWithLink(editorVal));
			
			$("#projectForm").ajaxForm({
				url : '<c:url value="/selfCheck/saveAjax"/>',
				type : 'POST',
				dataType: "json",
				cache : false,
				success: fn.onRegistSuccess,
				error : fn.onError
			}).submit();
		},
		// 삭제
		deleteSubmit : function(){
			$("#projectForm").ajaxForm({
				url :'<c:url value="/selfCheck/delAjax"/>',
				type : 'POST',
				dataType:"json",
				cache : false,
				success:fn.onDeleteSuccess,
				error : fn.onError
			}).submit();
		},
		// 성공 콜백
		onRegistSuccess : function(json, status){
			if(json.isValid == 'false') {
				alertify.error('<spring:message code="msg.common.valid" />', 0);
			} else {
				var prjId = $('input[name=prjId]').val();
				alertify.alert('<spring:message code="msg.common.success" />', function(){
					reloadTabInframe('<c:url value="/selfCheck/list"/>');

					if(prjId == '') {
						deleteTabInFrame('#<c:url value="/selfCheck/edit"/>');
					} else {
						fn_self.modeChange(prjId, 'v');
					}
				});	
			}
		},
		// 삭제 콜백
		onDeleteSuccess : function(json, status){
			var prjId = $('input[name=prjId]').val();
			
			if(json.resCd=='10'){
				reloadTabInframe('<c:url value="/selfCheck/list"/>');

				alertify.alert('<spring:message code="msg.common.success" />', function(){
					if(prjId) {
						deleteTabInFrame('#<c:url value="/selfCheck/edit/'+prjId+'"/>');
					} else {
						deleteTabInFrame('#<c:url value="/selfCheck/edit"/>');
					}
				});
			} else {
				alertify.error('<spring:message code="msg.common.valid2" />', 0)
			}
		},
		// 에러 콜백
		onError : function(data, status){
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
		},
		shareUrl : function(){
			var copyUrl = "";
			var protocol = window.location.protocol;
			var host =  window.location.host;
			copyUrl = protocol + "//" + host + "/selfCheck/view/${project.prjId}";
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
		openPreviewPop : function(data){
			var result =jQuery.parseJSON(data);
			
			if(result.isValid == 'false') {
	           	alertify.error('<spring:message code="msg.common.valid2" />', 0);
			} else {
				var noti = document.createElement('pre');

				//custom style.
				noti.style.maxHeight = "400px";
				noti.style.overflowWrap = "break-word";
				noti.style.margin = "-16px -16px -16px 0";
				noti.style.paddingBottom = "24px";
				noti.appendChild(document.createTextNode(result.resultData));
				
				alertify.alert().set({'resizable': true, 'startMaximized':true, 'message':result.resultData}).show();
				alertify.alert().set({'resizable': false, 'startMaximized':false});

				var checkedFlag = $("[name='btnEditOssNotice']:checked").val() == "Y";

				if($("#chkUseCustomNotice").prop("checked")){
					fn.defaultNotice(false);
				}else{
					fn.defaultNotice(checkedFlag);
				}
			}
		},
		openNoticeEditPop : function(data){
			var result =jQuery.parseJSON(data);

			if(result.isValid == 'false') {
	           	alertify.error('<spring:message code="msg.common.valid2" />', 0);
			} else {
				var contents = '<textarea cols="80" id="editor3" name="editor3" rows="10"></textarea>';
				
				alertify.confirm()
						.set({'resizable': true, 'startMaximized':true})
						.set('labels', {ok:'save'})
						.set('onok', function(event){ fn.saveNoticeEditPop(); return false; })
						.set('onshow', function(event){
							CKEDITOR.replace( 'editor3', {
						        fullPage: true,
						        allowedContent: true,
						        startupShowBorders: false,
						        height: 620
						    });
							CKEDITOR.instances.editor3.setData(result.resultData);
						})
						.setContent(contents)
						.show();
				
				fn.defaultNotice(false);
			}
		},
		saveNoticeEditPop : function(){
			$('#noticeHtml').val(CKEDITOR.instances.editor3.getData());
			
			$('#noticeForm').ajaxForm({
				url : '/selfCheck/saveNoticeAjax',
	            type : 'POST',
	            dataType: 'json',
	            cache : false,
	            success: function(data){
	            	loading.hide();
	            	alertify.closeAll();
	            	
	            	if(data.isValid == 'false'){
	            		alertify.error('<spring:message code="msg.common.valid2" />', 0);
	            	}
	            },
	            error : function(data){
	            	loading.hide();
	            	alertify.closeAll();
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
	            }
			}).submit();
		},
		saveOrGetNotice : function(flag){
			// 저장 : save, 프리뷰 : preview, 에디터 : editor
			var customUrl;
			var customDataType;
			var customSucess;
			var customError;
			
			if(flag == 'preview') {
				customUrl      = '/selfCheck/noticeAjax';
				customDataType = "text";
				customSucess   = fn.openPreviewPop;
				customError    = function(data){
					if(!$("#companyName").prop("checked")) {
						$("#editCompanyName").attr("disabled", true);
					}
					
					if(!$("#ossDistributionSite").prop("checked")) {
						$("#editOssDistributionSite").attr("disabled", true);
					}
					
					if(!$("#email").prop("checked")) {
						$("#editEmail").attr("disabled", true);
					}
					
					loading.hide();
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
				
				$("#editCompanyName").attr("disabled", false);
				$("#editOssDistributionSite").attr("disabled", false);
				$("#editEmail").attr("disabled", false);
			} else if(flag == 'previewOnly') {
				customUrl      = '/selfCheck/noticeAjax';
				customDataType = "text";
				customSucess   = fn.openPreviewPop;
				customError    = function(data){
					if(!$("#companyName").prop("checked")) {
						$("#editCompanyName").attr("disabled", true);
					}
					
					if(!$("#ossDistributionSite").prop("checked")) {
						$("#editOssDistributionSite").attr("disabled", true);
					}
					
					if(!$("#email").prop("checked")) {
						$("#editEmail").attr("disabled", true);
					}
					
					loading.hide();
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
				
				$("#editCompanyName").attr("disabled", false);
				$("#editOssDistributionSite").attr("disabled", false);
				$("#editEmail").attr("disabled", false);
				$("#previewOnly").val("Y");
			} else if(flag == 'editor') {
				customUrl      = '/selfCheck/noticeAjax';
				customDataType = "text";
				customSucess   = fn.openNoticeEditPop;
				customError    = function(data){
					if(!$("#companyName").prop("checked")) {
						$("#editCompanyName").attr("disabled", true);
					}
					
					if(!$("#ossDistributionSite").prop("checked")) {
						$("#editOssDistributionSite").attr("disabled", true);
					}
					
					if(!$("#email").prop("checked")) {
						$("#editEmail").attr("disabled", true);
					}
					
					loading.hide();
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
				
				$("#editCompanyName").attr("disabled", false);
				$("#editOssDistributionSite").attr("disabled", false);
				$("#editEmail").attr("disabled", false);
			}
			
			if($("#append").prop("checked")){
				$('#noticeForm input[name=appendedTEXT]').val(CKEDITOR.instances.editor2.getData().replace(/(<([^>]+)>)/ig, "").trim());
				$('#noticeForm input[name=appended]').val(CKEDITOR.instances.editor2.getData());
			}
			
			$('#noticeForm').ajaxForm({
				url :customUrl,
	            type : 'POST',
	            dataType:customDataType,
	            cache : false,
	            success: customSucess,
	            error : customError
			}).submit();
		},
		
		downloadNotice : function(){
			if($("#append").prop("checked")){
				$('#noticeForm input[name=appendedTEXT]').val(CKEDITOR.instances.editor2.getData().replace(/(<([^>]+)>)/ig, "").trim());
				$('#noticeForm input[name=appended]').val(CKEDITOR.instances.editor2.getData());
			}
			
			$("#editCompanyName").attr("disabled", false);
			$("#editOssDistributionSite").attr("disabled", false);
			$("#editEmail").attr("disabled", false);
			$("#isSimpleNotice").val("N");
			
			$('#noticeForm').ajaxForm({
				url :'/selfCheck/makeNoticePreview',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '/selfCheck/downloadNoticePreview?id='+data.validMsg;

						   var checkedFlag = $("[name='btnEditOssNotice']:checked").val() == "Y";

					       fn.defaultNotice(checkedFlag);
					   }
				   },
	            error : function(data){
	            	loading.hide();
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
	            }
			}).submit();
		},
		
		downloadNoticeText : function(){
			if($("#append").prop("checked")){
				$('#noticeForm input[name=appendedTEXT]').val(CKEDITOR.instances.editor2.getData().replace(/(<([^>]+)>)/ig, "").trim());
				$('#noticeForm input[name=appended]').val(CKEDITOR.instances.editor2.getData());
			}
			
			$("#editCompanyName").attr("disabled", false);
			$("#editOssDistributionSite").attr("disabled", false);
			$("#editEmail").attr("disabled", false);
			$("#isSimpleNotice").val("N");
			
			$('#noticeForm').ajaxForm({
				url :'/selfCheck/makeNoticeText',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '/selfCheck/downloadNoticePreview?id='+data.validMsg;

					       var checkedFlag = $("[name='btnEditOssNotice']:checked").val() == "Y";

					       fn.defaultNotice(checkedFlag);
					   }
				   },
	            error : function(data){
	            	loading.hide();
	            	
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
	            }
			}).submit();
		},
		
		downloadNoticeSimple : function(){
			if($("#append").prop("checked")){
				$('#noticeForm input[name=appendedTEXT]').val(CKEDITOR.instances.editor2.getData().replace(/(<([^>]+)>)/ig, "").trim());
				$('#noticeForm input[name=appended]').val(CKEDITOR.instances.editor2.getData());
			}
			
			$("#editCompanyName").attr("disabled", false);
			$("#editOssDistributionSite").attr("disabled", false);
			$("#editEmail").attr("disabled", false);
			$("#isSimpleNotice").val("Y");
			
			$('#noticeForm').ajaxForm({
				url :'/selfCheck/makeNoticeSimple',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '/selfCheck/downloadNoticePreview?id='+data.validMsg;

					       var checkedFlag = $("[name='btnEditOssNotice']:checked").val() == "Y";

					       fn.defaultNotice(checkedFlag);
					   }
				   },
	            error : function(data){
	            	loading.hide();

	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
	            }
			}).submit();
		},
		
		downloadNoticeTextSimple : function(){
			if($("#append").prop("checked")){
				$('#noticeForm input[name=appendedTEXT]').val(CKEDITOR.instances.editor2.getData().replace(/(<([^>]+)>)/ig, "").trim());
				$('#noticeForm input[name=appended]').val(CKEDITOR.instances.editor2.getData());
			}
			
			$("#editCompanyName").attr("disabled", false);
			$("#editOssDistributionSite").attr("disabled", false);
			$("#editEmail").attr("disabled", false);
			$("#isSimpleNotice").val("Y");
			
			$('#noticeForm').ajaxForm({
				url :'/selfCheck/makeNoticeTextSimple',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '/selfCheck/downloadNoticePreview?id='+data.validMsg;
					       var checkedFlag = $("[name='btnEditOssNotice']:checked").val() == "Y";
					       fn.defaultNotice(checkedFlag);
					   }
				   },
	            error : function(data){
	            	loading.hide();
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
	            }
			}).submit();
		},
		downloadSpdxSpreadSheetExcel : function(){
			var dataStr = JSON.stringify($('#noticeForm').serializeObject());
			
			$.ajax({
				type: "POST",
				url: '/spdxdownload/getSelfcheckSPDXPost',
				data: JSON.stringify({"type":"spdx", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '/spdxdownload/getFile?id='+data.validMsg;
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		downloadSpdxRdf : function() {
			var dataStr = JSON.stringify($('#noticeForm').serializeObject());
			
			$.ajax({
				type: "POST",
				url: '/spdxdownload/getSelfcheckSPDXPost',
				data: JSON.stringify({"type":"spdxRdf", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '/spdxdownload/getFile?id='+data.validMsg;
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		
		downloadSpdxTag : function() {
			var dataStr = JSON.stringify($('#noticeForm').serializeObject());
			
			$.ajax({
				type: "POST",				   
				url: '/spdxdownload/getSelfcheckSPDXPost',
				data: JSON.stringify({"type":"spdxTag", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '/spdxdownload/getFile?id='+data.validMsg;
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		downloadSpdxJson : function() {
			var dataStr = JSON.stringify($('#noticeForm').serializeObject());
			
			$.ajax({
				type: "POST",
				url: '/spdxdownload/getSelfcheckSPDXPost',
				data: JSON.stringify({"type":"spdxJson", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '/spdxdownload/getFile?id='+data.validMsg;
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			})
		},
		downloadSpdxYaml : function() {
			var dataStr = JSON.stringify($('#noticeForm').serializeObject());
			
			$.ajax({
				type: "POST",
				url: '/spdxdownload/getSelfcheckSPDXPost',
				data: JSON.stringify({"type":"spdxYaml", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '/spdxdownload/getFile?id='+data.validMsg;
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			})
		},
		appendEditVisible : function(target){
			var checked = $(target).prop("checked");
			
			if(checked) {
				$("#editAppend").show();
			} else {
				$("#editAppend").hide();
			}
		},
		defaultNotice : function(checked, type){
			if(checked){
				$("#companyName").attr("disabled", !checked);
				$("#ossDistributionSite").attr("disabled", !checked);
				$("#email").attr("disabled", !checked);
				$("#hideOssVersion").attr("disabled", !checked);
				$("#append").attr("disabled", !checked);
				
				if($("#companyName").prop("checked")) {
					$("#editCompanyName").attr("disabled", !checked);
				} else {
					$("#editCompanyName").attr("disabled", checked);
				}
				
				if($("#ossDistributionSite").prop("checked")) {
					$("#editOssDistributionSite").attr("disabled", !checked);
				} else {
					$("#editOssDistributionSite").attr("disabled", checked);
				}
				
				if($("#email").prop("checked")) {
					$("#editEmail").attr("disabled", !checked);
				} else {
					$("#editEmail").attr("disabled", checked);
				}
				
				if(type != "init" && $("#append").prop("checked")) {
					if(CKEDITOR.instances.editor2) {
						CKEDITOR.instances.editor2.setReadOnly(false);
					}
				}
			} else {
				$("#companyName").attr("disabled",!checked);
				$("#ossDistributionSite").attr("disabled",!checked);
				$("#email").attr("disabled",!checked);
				$("#hideOssVersion").attr("disabled",!checked);
				$("#append").attr("disabled",!checked);
				
				$("#editCompanyName").attr("disabled", !checked);
				$("#editOssDistributionSite").attr("disabled", !checked);
				$("#editEmail").attr("disabled", !checked);
				
				if(type != "init" && $("#append").prop("checked")){
					if(CKEDITOR.instances.editor2) {
						CKEDITOR.instances.editor2.setReadOnly(true);
					}
				}
			}
		},
		initNotice : function(){
			window.setTimeout(function(){
				var editNoticeYn = $("[name='btnEditOssNotice']:checked").val();
				var editCompanyYn = $("#companyName").val();
				var editDistributionSiteUrlYn = $("#ossDistributionSite").val();
				var editEmailYn = $("#email").val();
				var hideOssVersionYn = $("#hideOssVersion").val();
				var editAppendedYn = $("#append").val();
				
				var checked = ($("[name='btnEditOssNotice']:checked").val() == "Y");
				var btn_Save = $("#save");
				
				if(userRole == "ROLE_ADMIN"){ // 관리자 권한 일 경우					
					btn_Save.show();
					fn.defaultNotice(checked, "init");	
				} else { // 일반 사용자 일 경우
					switch(curIdenStatus){
						case "":
						case "PROG":
							btn_Save.show();
							fn.defaultNotice(checked, "init");

							break;
						default:
							btn_Save.hide();
							$("[name='btnEditOssNotice']").attr("disabled", true)
							fn.defaultNotice(false, "init");

							break;
					}
				}
				
				if(editNoticeYn == "Y") {
					$("[name='btnEditOssNotice']").trigger("click").attr("checked", true);
				}
				
				if(editCompanyYn == "Y") {
					$("#companyName").trigger("click").attr("checked", true);
				}
				
				if(editDistributionSiteUrlYn == "Y") {
					$("#ossDistributionSite").trigger("click").attr("checked", true);
				}
				
				if(editEmailYn == "Y") {
					$("#email").trigger("click").attr("checked", true);
				}
				
				if(hideOssVersionYn == "Y") {
					$("#hideOssVersion").trigger("click").attr("checked", true);
				}
				
				if(editAppendedYn == "Y"){
					$("#append").trigger("click").attr("checked", true);

					if(CKEDITOR.instances.editor2) {
						CKEDITOR.instances.editor2.config.readOnly = false;
					}
				}
			}, 1);
		},
		bulkEdit : function(){
	    	var gridList = $("#srcList");
	        var targetGird = "srcList";

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
	            var url = '<c:url value="/oss/ossBulkEditPopup?rowId=' + rowCheckedArr + '&target=selfCheck"/>';
	            
	            var _popup = null;
	            
	            if(_popup == null || _popup.closed){
	                _popup = window.open(url, "bulkEditViewSelfPopup", "width=850, height=350, toolbar=no, location=no, left=100, top=100, resizable=yes");

	                if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
	                    alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
	                }
	            } else {
	                _popup.close();
	                _popup = window.open(url, "bulkEditViewSelfPopup", "width=850, height=350, toolbar=no, location=no, left=100, top=100, resizable=yes");
	            }
	        }else{
	            alertify.alert('<spring:message code="msg.oss.select.ossTable" />', function(){});
	            return false;
	        }
		},

        changeSelectOption : function(target){
            var name = $(target).attr("name");
            var value = $("[name='"+name+"']:checked").val()
            var key = name.split("_")[1];

            var srcR1 = document.getElementById("srcR1");
            var srcUploadUrl = document.getElementById("2");

            switch(value){
                case "1":
                    $('.checksrcR1').closest('span').show();
                    $('.check2').closest('span').hide();
                    $('.uploadSet').show();
                    $('.wgetUrl').hide();
                    srcUploadUrl.checked = false;
                    break;
                case "2":
                    $('.checksrcR1').closest('span').hide();
                    $('.check2').closest('span').show();
                    $('.uploadSet').hide();
                    $('#wgetUrl_' + key).show();
                    srcR1.checked = false;
                    break;
                default:
                    break;
            }
        },
        downloadYaml : function(){
            var params = {"prjId":"${project.prjId}", "prjName" : "${project.prjName}"};

            $.ajax({
                type: "POST",
                url: '<c:url value="/selfCheck/makeYaml"/>',
                data: JSON.stringify(params),
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
        }
	};

	// 데이타
	var data = {
		modelValues:'',
		detail : ${empty detail ? 'null':detail},
		copy : ${empty copy ? 'null' :copy},
		init : function(){
			if(data.detail){
				$('input[name=prjId]').val(data.detail.prjId);
				$('input[name=prjName]').val(data.detail.prjName);
				$('input[name=prjVersion]').val(data.detail.prjVersion);
			}
		}
	}
	
	// SRC 이벤트
	var src_evt = {
		csvDelFileSeq : [],
		csvFileSeq : [],
		init: function(){
			src_fn.getSrcGridData();
			
			var srcList = $("#srcList");
			
			// 그리드 리셋 버튼
			$("#srcResetUp").click(function(e){
	 			e.preventDefault();
	 			
				alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
					if (e) {
						$("#srcList").jqGrid('clearGridData');
					} else {
						return false;
					}
				});
			});
			
			// 그리드 저장 버튼
			$("#srcSaveUp").click(function(e){
				e.preventDefault();
				
				src_fn.saveAjax();
			});
			
			var accept1 = '';
			<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
				<c:if test="${file eq '12'}">
				accept1 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
				</c:if>
			</c:forEach>
			
			//파일업로드
			$('#srcCsvFile').uploadFile({
				url:'<c:url value="/project/csvFile"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				allowedTypes:accept1,
				sequential:true,
				sequentialCount:1,
				dynamicFormData: function() {
					var data ={ "registFileId" :$('#srcCsvFileId').val(), "tabNm" : "SELF"}
					return data;
				},
				onSuccess:function(files,data,xhr,pd) {
					var result = jQuery.parseJSON(data);
					
					if(result[1] == null){
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					} else {
						if(result[0] == "FILE_SIZE_LIMIT_OVER"){
							alertify.alert(result[1], function(){
								$('.ajax-file-upload-statusbar').fadeOut('slow');
								$('.ajax-file-upload-statusbar').remove();
							});
						} else if(result[2] == "CSV_FILE") {
							src_fn.getCsvData(result[0][0].registSeq);

							$('#binCsvFileId').val(result[0][0].registFileId);
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();

							src_fn.makeFileTag(result[0][0]);
						} else if(result[2] == "EXCEL_FILE") {
							if(result[1].length != 0){
								$('.sheetSelectPop').show();
								$('.sheetSelectPop .sheetNameArea').children().remove();
								$('.sheetSelectPop .sheetNameArea').text('');
								
								for(var i = 0; i < result[1].length; i++){
									var num = i+1;
									
									$('.sheetSelectPop .sheetNameArea').append('<li><input type="checkbox" name="sheetNameSelect" value="'+result[1][i].no+'" id="sheet'+result[1][i].no+'" class="sheetNum">'
											+'<label for="sheet'+result[1][i].no+'">'+result[1][i].name+'</label></li>');
									$('.sheetSelectPop .sheetApply').attr('onclick', 'src_fn.getSheetData('+result[0][0].registSeq+')');
								}
							}
							
							$('#srcCsvFileId').val(result[0][0].registFileId);
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();
							
							src_fn.makeFileTag(result[0][0]);	
						} else if(result[2] == "SPDX_SPREADSHEET_FILE"){
							src_fn.getSpdxSpreadsheetData(result[0][0].registSeq);

							$('#srcCsvFileId').val(result[0][0].registFileId);
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();

							src_fn.makeFileTag(result[0][0]);
						} else {
							alertify.error('<spring:message code="msg.common.valid" />', 0);
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();
						}
					}
				}
			});
			
			// ossNames auto complete
			fn_grid_com.griOssNames().success(function(data, status, headers, config){
				if(data != null && ossNames == ""){
					data.forEach(function(obj){
						ossNames.push(obj.ossName);
					});
				}
			});
			
			// licenseNames auto complete
			commonAjax.getLicenseTags().success(function(data, status, headers, config){
				if(data != null && licenseNames == ""){
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
		}
	};
	
	// src 그리드 데이터 전역 변수
	var srcMainData;
	var srcSubData;
	var srcValidMsgData;
	var srcDiffMsgData;
	var licenseData;
	// SRC 함수
	var src_fn = {
		// src 그리드 데이터
		getSrcGridData : function(param){
			$.ajax({
				url : '<c:url value="/selfCheck/ossGrid/${project.prjId}/10"/>',
				dataType : 'json',
				cache : false,
				data : (param) ? param : {referenceId : '${project.prjId}'},
				contentType : 'application/json',
				success : function(data){
					srcMainData = data.mainData;
					srcValidMsgData = []; //초기화
					srcDiffMsgData = []; //초기화
					
					if(data.validData) {
						srcValidMsgData = data.validData;
					}
					
					if(data.diffData) {
						srcDiffMsgData = data.diffData;
					}

					// 리로드 대신 그리드 삭제 후 다시 그리기
					$("#srcList").jqGrid('GridUnload');
					
					src_grid.load();

					// totla record 표시
					$("#srcList_toppager_right, #srcPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+srcMainData.length+'</div>');
					
					fn_grid_com.addEtcKeyDownEvent($('#srcList'), srcValidMsgData, srcDiffMsgData, null, com_fn.getLicenseName);
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		// 저장 데이터 만들기
		saveMakeData : function(){
			cleanErrMsg("srcList");
			
			var target = $("#srcList");
			var prjId = '${project.prjId}';
			var csvFileId = $('#srcCsvFileId').val();
			var fileData = src_evt.csvDelFileSeq;
			var fileSeq = src_evt.csvFileSeq;
			var identificationSubStatusSrc = "";
			if(fileSeq.length == 0) {
				$("#srcCsvFileId").val("");
				csvFileId = "";
			}
			
			// 메인 그리드
			var mainData = target.jqGrid('getGridParam','data');
			// 최종 데이터
			var finalData = {"prjId" : prjId, "csvFileId" : csvFileId, "csvDelFileIds" : JSON.stringify(fileData), "csvFileSeqs" : JSON.stringify(fileSeq)
					   , "identificationSubStatusSrc" : identificationSubStatusSrc, "mainData" : JSON.stringify(mainData)};

			// 닉네임 체크
			src_fn.nickNameValid(mainData, finalData);
		},
		// 저장
		exeSave : function(finalData){
			$.ajax({
				url : '<c:url value="/selfCheck/saveSrc"/>',
				type : 'POST',
				data : JSON.stringify(finalData),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					_mainLastsel = -1;
					
					if("false" == data.isValid) {
						srcValidMsgData = data.resultData;
						
						alertify.error('<spring:message code="msg.common.valid" />', 0);
					} else {
						if("10" == data.resCd){
							src_fn.getSrcGridData();
							src_evt.csvDelFileSeq = [];
							src_evt.csvFileSeq = [];
							reloadTabInframe('<c:url value="/selfCheck/list"/>');
							
							saveFlag = true;
							
							alertify.success('<spring:message code="msg.common.success" />');
						} else {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		// 닉네임 팝업 띄우기
		makeNickNamePopup : function(obj, finalData) {
			if(obj.validMsg && obj.validMsg.length != 0) {
				alertify.confirm(obj.validMsg, function () {
					src_fn.exeSave(finalData);
				});
			} else {
				src_fn.exeSave(finalData);
			}
		},
		// 닉네임 체크
		nickNameValid : function(mainData, finalData){
			var prjId = '${project.prjId}';
			var postData = {"mainData" : JSON.stringify(mainData), "prjId" : prjId};
			
			$.ajax({
				url : '<c:url value="/project/nickNameValid/10"/>',
				type : 'POST',
				data : JSON.stringify(postData),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					src_fn.makeNickNamePopup(data, finalData);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		makeFileTag : function(obj){
			var appendHtml = '<br>'+obj.createdDate;
			var _url = '<c:url value="/download/'+obj.registSeq+'/'+obj.fileName+'"/>';
			$('.csvFileArea').append('<li><span><strong><a href="'+_url+'">'+obj.originalFilename+'</a>'+appendHtml+'<input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="src_fn.deleteCsv(this, \'1\')"/></strong></span></li>');
		},
		deleteCsv : function(obj, type) {
			var Seq = $(obj).prev().val();
			var object = {fileSeq : Seq};
			
			src_evt.csvDelFileSeq.push(object);
			
			$(obj).closest('li').remove();
		},
		// 다운로드 엑셀
		downloadExcel : function(){
			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"selfReport", "parameter":'${project.prjId}'}),
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
		// 다운로드 엑셀
		downloadExcelVuln : function(){
			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"selfReportVuln", "parameter":'${project.prjId}'}),
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
		closePop : function(){
			src_fn.cancelFileDel();
			
			$('.sheetSelectPop').hide();
		},
		closeAndroidPop : function(){
			$('.ossSelectPop').hide();
		},
		getSheetData : function(seq){
			var sheetNum = [];
			
			$('input:checkbox[name="sheetNameSelect"]').each(function(){
				if($(this).is(':checked')){
					sheetNum.push($(this).val());
				}
			});
			
			if(sheetNum.length == 0) {
				alert('<spring:message code="msg.common.check.sheet" />');
				
				return;
			} else {
				loading.show();
				fn_grid_com.totalGridSaveMode('srcList');
				cleanErrMsg("srcList");
				var target = $("#srcList");
				
				// 메인 그리드
				var mainData = target.jqGrid('getGridParam','data');
				var finalData = {"readType":"self","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};
				var object = {fileSeq : seq};

				src_evt.csvFileSeq.push(object);
				src_fn.exeLoadReportData(finalData);
			}
		},
		getCsvData : function(seq){
			loading.show();
			fn_grid_com.totalGridSaveMode('srcList');
			cleanErrMsg("srcList");
			var target = $("#srcList");

			// 메인 그리드
			var mainData = target.jqGrid('getGridParam','data');
			var sheetNum = ["0"];
			var finalData = {"readType":"self","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};
			var object = {fileSeq : seq};

			src_evt.csvFileSeq.push(object);
			src_fn.exeLoadReportData(finalData);
		},
		getSpdxSpreadsheetData : function(seq){
			var sheetNum = ["1", "4"];

			loading.show();
			fn_grid_com.totalGridSaveMode('srcList');
			cleanErrMsg("srcList");

			var target = $("#srcList");
			var mainData = target.jqGrid('getGridParam','data');
			var finalData = {"readType":"self","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};
			var object = {fileSeq : seq};

			src_evt.csvFileSeq.push(object);
			src_fn.exeLoadReportData(finalData);
        },
		// load report data
		exeLoadReportData : function(finalData){
			$.ajax({
				url : '<c:url value="${suffixUrl}/project/getSheetData"/>',
				type : 'POST',
				data : JSON.stringify(finalData),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					loading.hide();

					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
						
						if(data.validMsg) {
							alertify.alert(data.validMsg, function(){});
						}
					} else {
						$('.sheetSelectPop').hide();

						if(data.externalData) {
							// validData 표시
							srcValidMsgData = data.externalData;
						}

						if(data.externalData2) {
							// validData 표시
							srcDiffMsgData = data.externalData2;
						}

						src_fn.makeOssList(data.resultData);
						
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
		makeOssList : function(data){
			srcMainData = data.mainData;
			srcSubData = data.subData;
			
			//licenseName 재구성
			var subData = new Object();
			$.each(srcSubData,function(key,value) {
				var strVal = "";
				
				for(var i=0; i < value.length; i++){
					if(value[i].excludeYn == "N"){
						strVal += value[i].licenseName+",";	
					}
				}
				
				for(var j=0; j < srcMainData.length; j++){
					if(srcMainData[j]["licenseDiv"] == "M"){
						if(key == srcMainData[j]["gridId"]){
							srcMainData[j]["licenseName"] = strVal.substring(0,strVal.length-1);
						}
					}
				}
			});
			
			// 리로드 대신 그리드 삭제 후 다시 그리기
			$("#srcList").jqGrid('GridUnload');
			src_grid.load();

			// totla record 표시
			$("#srcList_toppager_right, #srcPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+srcMainData.length+'</div>');
		},
		// upload Cancel시 DB삭제
		cancelFileDel : function(){
			var FileSeq = [];
			var tabGubn = $(".tabMenu").find("span").text();
			var seq = "";
			
			if(tabGubn == "SRC") {
				seq = $('.csvFileArea > li:last').find("input[type=hidden]").val();
			} else if(tabGubn == "BIN") {
				seq = $('.binFileArea > li:last').find("input[type=hidden]").val();
			}
			
			if(seq == "") {
				return;
			}
			
			var object = {fileSeq : seq};
			FileSeq.push(object);
			
			var Data = {"csvDelFileIds" : JSON.stringify(FileSeq)};

			$.ajax({
				url : '<c:url value="/project/calcelFileDelSrc"/>',
				type : 'POST',
				data : JSON.stringify(Data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data) {
					if("10"== data.resCd) {
						if(tabGubn == "SRC"){
							$('.csvFileArea > li:last').remove();
						}else if(tabGubn == "BIN"){
							$('.binFileArea > li:last').remove();
						}					
					} else {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				},
				error: function(data) {
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		displayNotify : function(cellvalue, options, rowObject) {
			var display = "";
			var obligationType = rowObject["obligationType"];
			var obligationGrayFlag = rowObject["obligationGrayFlag"];

			if(obligationGrayFlag == "Y") {
				var _obligationMsg = rowObject["obligationMsg"];
				
				if(!_obligationMsg || _obligationMsg == "") {
					_obligationMsg = "unknown obligation";
				}

				display = "<span class=\"iconSet help\">Obligation is unclear</span>";

			} else if(obligationType == 10){
				display="<span class=\"iconSet ops\">Notice</span>";
			} else if(obligationType == 11) {
				display="<span class=\"iconSet ops\"></span><span class=\"iconSet man\">Notice & SourceCode</span>";
			}
			
			return display;
		},
		displaySource : function(cellvalue, options, rowObject) {
			var display = "";
			var obligationType = rowObject["obligationType"];
			var obligationGrayFlag = rowObject["obligationGrayFlag"];

			if(obligationType == 11){
				display="<span class=\"iconSet man\"></span>";
			}
			
			return display;
		},
		unDisplayNotify : function(cellvalue, options, rowObject) {
			var display = $("#"+options.rowId+"_notify").val();
			
			return display;
		},
		unDisplaySource : function(cellvalue, options, rowObject) {
			var display = $("#"+options.rowId+"_source").val();

			return display;
		},		
		// License 상세 페이지 이동
	 	showVulnViewPage : function(_ossName, _ossVersion) {
	 		if(_ossVersion == ""){
	 			_ossVersion = "-";
		 	}
		 	
	 		var _url = '<c:url value="/vulnerability/vulnpopup?ossName='+_ossName+'&ossVersion='+_ossVersion+'"/>'; //+"&isPopup=Y"; // 사용하지 않는 parameter
	 		
			if(_popupVuln == null || _popupVuln.closed){
				_popupVuln = window.open(_url, "vulnViewPopup_"+_ossName, "width=950, height=600, toolbar=no, location=no, left=100, top=100");

				if(!_popupVuln || _popupVuln.closed || typeof _popupVuln.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			} else {
				_popupVuln.close();
				
				_popupVuln = window.open(_url, "vulnViewPopup_"+_ossName, "width=900, height=600, toolbar=no, location=no, left=100, top=100");
			}
		},
	 	showLicenseViewPage : function(gridNm, rowid) {
	 		src_fn.exitCell(_mainLastsel);
			cleanErrMsg("srcList", rowid);

			var target = $("#"+gridNm);
			target.jqGrid('saveRow',rowid);
			
			var licenseName = target.jqGrid('getCell',rowid,'licenseName');
			
			if(_popupLicense == null || _popupLicense.closed){
				_popupLicense = window.open('<c:url value="/selfCheck/licensepopup?licenseName='+licenseName+'"/>', 'licenseViewPopup_'+licenseName, 'width=900, height=700, toolbar=no, location=no, left=100, top=100');

				if(!_popupLicense || _popupLicense.closed || typeof _popupLicense.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			} else {
				_popupLicense.close();
				
				_popupLicense = window.open('<c:url value="/selfCheck/licensepopup?licenseName='+licenseName+'"/>', 'licenseViewPopup_'+licenseName, 'width=900, height=700, toolbar=no, location=no, left=100, top=100');
			}
		},
		// 메인 그리드 OSS 등록/상세 페이지 이동
	 	showOssViewPage : function(gridNm, rowid) {
	 		src_fn.exitCell(_mainLastsel);
			cleanErrMsg("srcList", rowid);
			
	 		var target = $("#"+gridNm);
			target.jqGrid('saveRow',rowid);
			var ossName = target.jqGrid('getCell',rowid,'ossName');
			var ossVersion = target.jqGrid('getCell',rowid,'ossVersion');
			
			if(ossVersion=="N/A") {
				ossVersion = "";
			}
			
			if(ossName != "") {
				$.ajax({
					url : '<c:url value="/oss/checkExistsOssByname"/>',
					type : 'GET',
					dataType : 'json',
					cache : false,
					data : {ossName : ossName},
					contentType : 'application/json',
					success : function(data){
						if(data.isValid == 'true') {
							if(_popup == null || _popup.closed) {
								_popup = window.open('<c:url value="/oss/osspopup?ossName='+ossName+'&ossVersion='+ossVersion+'"/>', 'ossViewPopup_'+ossName, 'width=900, height=700, toolbar=no, location=no, left=100, top=100');

								if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
									alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
								}
							} else {
								_popup.close();
								_popup = window.open('<c:url value="/oss/osspopup?ossName='+ossName+'&ossVersion='+ossVersion+'"/>', 'ossViewPopup_'+ossName, 'width=900, height=700, toolbar=no, location=no, left=100, top=100');
							}
						} else {
							alertify.alert('<spring:message code="msg.selfcheck.info.unconfirmed.oss" />', function(){});
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		checkOssIdByName : function(ossName) {},
		displayGuide : function(cellvalue, options, rowObject) {
			var licenseName = rowObject["licenseName"];
			var licenseUserGuideYn = rowObject["licenseUserGuideYn"];
			
			var display = "";
			
			if("Y" == licenseUserGuideYn) {
				display = "<div class=\"tcenter\"><a class='btnIcon tips' onclick=\"src_fn.popGuide('"+options.rowId+"')\">Tips</a></div>";
			}
			
			return display;
		},
		popGuide : function(row_id) {
			var _guide = $("#srcList").jqGrid('getCell', row_id, 'licenseUserGuideStr');

			if(_guide) {
				alertify.alert(_guide, function(){});
			}
		},
		displayOssDetail : function(cellvalue, options, rowObject) {
			var display = "";
			var ossName = rowObject["ossName"];
			var existsFlag = rowObject["ossNameExistsYn"];

			if(ossName === undefined || ossName == "" || ossName == null) {
			} else {
				if(existsFlag && existsFlag == "Y") {
						display = "<div class=\"tcenter\"><a class='btnIcon ossI' onclick=\"src_fn.showOssViewPage('srcList','"+options.rowId+"')\">Detail Info</a></div>";
				} else {
					display = "";
				}
			}
			
			return display;
		},
		displayLicenseDetail : function(cellvalue, options, rowObject) {
			var display = "";
			var licenseName = rowObject["licenseName"];
			var existsFlag = rowObject["licenseNameExistsYn"];
			if(licenseName === undefined || licenseName == "" || licenseName == null) {
			} else {
				if(existsFlag == "Y") {
					display = "<div class=\"tcenter\"><a class='btnIcon licenseI' onclick=\"src_fn.showLicenseViewPage('srcList','"+options.rowId+"')\">Detail Info</a></div>";
				} else {
					display = "";
				}
			}
			
			return display;
		},
		// vulnerability 
		displayVulnerability : function(cellvalue, options, rowObject) {
			var display = "";
			var _score = rowObject.cvssScore;
			
			if(_score) {
				if(parseInt(_score) >= 9.0 ) {
					display = "<span class=\"iconSet vulCritical\" onclick=\"src_fn.showVulnViewPage('"+ rowObject.ossName +"','"+rowObject.ossVersion+"')\">"+_score+"</span>";
				} else if(parseInt(_score) >= 7.0 ) {
					display = "<span class=\"iconSet vulHigh\" onclick=\"src_fn.showVulnViewPage('"+ rowObject.ossName +"','"+rowObject.ossVersion+"')\">"+_score+"</span>";
				} else if(parseInt(_score) >= 4.0) {
					display = "<span class=\"iconSet vulMiddle\" onclick=\"src_fn.showVulnViewPage('"+ rowObject.ossName +"','"+rowObject.ossVersion+"')\">"+_score+"</span>";
				} else if(parseInt(_score) > 0) {
					display = "<span class=\"iconSet vulLow\" onclick=\"src_fn.showVulnViewPage('"+ rowObject.ossName +"','"+rowObject.ossVersion+"')\">"+_score+"</span>";
				} else {
					display = "<span style=\"font-size:0;\"></span>";
				}
			}

			return display;
		},
		exitCell : function(_mainLastsel) {
			if(_mainLastsel != -1){
				var grid = $("#srcList");
				var licenseName = com_fn.getLicenseName(grid.getRowData(_mainLastsel));
				
				grid.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
				fn_grid_com.saveCellData(grid.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
				grid.jqGrid('saveRow',_mainLastsel);
			}
		},
		CheckOssViewPage : function() {
			if(saveFlag) {
				if(_popupCheckOssName != null) {
					_popupCheckOssName.close();
				}
				
				_popupCheckOssName = window.open('<c:url value="/oss/checkOssName?prjId=${project.prjId}&referenceDiv=10&targetName=self"/>', 'Check OSS Name', 'width=1100, height=550, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes');

				if(!_popupCheckOssName || _popupCheckOssName.closed || typeof _popupCheckOssName.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			} else {
				alertify.alert('<spring:message code="msg.project.required.checkOssName" />', function(){});

				return false;
			}
			
			
		},
		CheckOssLicenseViewPage : function() {
			if(saveFlag) {
				if(_popupCheckOssLicense != null) {
					_popupCheckOssLicense.close();
				}
				
				_popupCheckOssLicense = window.open('<c:url value="/oss/checkOssLicense?prjId=${project.prjId}&referenceDiv=10&targetName=self"/>', 'Check License', 'width=1100, height=550, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes');

				if(!_popupCheckOssLicense || _popupCheckOssLicense.closed || typeof _popupCheckOssLicense.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			} else {
				alertify.alert('<spring:message code="msg.project.required.checkOssLicense" />', function(){});

				return false;
			}
		},
		saveAjax : function() {
			com_fn.exitCell(_mainLastsel, "srcList");
			
			alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
				if (e) {
					// 메인, 서브 그리드 세이브 모드
					fn_grid_com.totalGridSaveMode('srcList');
					// 닉네임 체크
					src_fn.saveMakeData();
				} else {
					return false;
				}
			});
		},
		createNoticeTab : function () {
            if(saveFlag) {
                createTabInFrame('Notice', '#<c:url value="/selfCheck/verification/${project.prjId}"/>');
            } else {
                alertify.error('<spring:message code="msg.project.required.checkOssName" />', 0);
                return false;
            }
        },
        uploadOSSByUrl : function() {

            var wgetUrl = $("#sendWgetUrl").val();
            
            if(wgetUrl == "") {
				alertify.alert('URL is empty');
				return false;
            }
        	
			$.ajax({
				url : '<c:url value="/external/request-fl-scan"/>',
				type : 'POST',
				data : {'prjId' : '${project.prjId}', 'wgetUrl' : wgetUrl},
				dataType : 'json',
				cache : false,
				success : function(data){
					if(data.success) {
						alertify.success('<spring:message code="msg.common.success" />');						
					} else {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
						if(data.msg) {
							alertify.alert(data.msg);
						}
					}
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
        },
	};

	//SRC 그리드
	var src_grid = {
		load: function() {
			var currentOssName = "";
			var ondblClickRowBln = false;
			var srcList = $("#srcList");

			gridTooltip.existTooltip = false;
			srcList.jqGrid({
				datatype: 'local',
				data : srcMainData,
				colNames: ['gridId', 'ID', 'ReferenceId', 'ReferenceDiv', 'ComponentIdx', 'Binary Name or Source Path', 'OssId', 'OSS Name','OSS Version','License', 'OSS Name Exists', 'License Exists'
				           ,'LicenseId','Download Location','Copyright Text','OSS Detail','License Detail','User<br/>Guide','CVE ID'//,'CVSS_SCORE'
				           ,'Vulnera<br/>bility','Obligation','Notify','Source','Restriction','<input type="checkbox" onclick="fn_grid_com.onCboxClickAll(this,\'srcList\');">Exclude','LicenseDiv', 'licenseUserGuideYn', 'licenseUserGuideStr','obligationGrayFlag', 'obligationMsg'],
				colModel: [
					{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
					{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype : 'int', search: false},
					{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
					{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
					{name: 'componentId', index: 'componentId', hidden:true},
					{name: 'filePath', index: 'filePath', width: 170, align: 'left', editable:false, template: searchStringOptions,
						editoptions: {
							dataInit:
								function (e) { 
									$(e).on("change", function() {
										var rowid = (e.id).split('_')[0];
										fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData,srcDiffMsgData);
									});
								}
						}
					},
					{name: 'ossId', index: 'ossId', width: 29, align: 'center', editable:true, hidden:true},
					{name: 'ossName', index: 'ossName', width: 150, align: 'left', editable:false, edittype:'text', template: searchStringOptions, 
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
												
												fn_grid_com.griOssVersions($('#'+rowid+'_ossVersion')[0], e.value, 'srcList');
												fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData, srcDiffMsgData);
											}
										}).dblclick(function(){
											var rowid = (e.id).split('_')[0];

											var licenseName = com_fn.getLicenseName(srcList.getRowData(rowid));
											srcList.jqGrid("setCell", rowid, "licenseName", licenseName);
											fn_grid_com.saveCellData(srcList.attr("id"), rowid, "licenseName", licenseName, null, null);
											srcList.jqGrid('saveRow',rowid);
											
											fn_grid_com.mvOssPage(srcList, rowid);
										});
										
										currentOssName = e.value;
										
										// oss name 변경시 oss detail 표시여부 체크
										$(e).on("change", function() {
											src_fn.checkOssIdByName(currentOssName);
										});
									}
							}
					},
					{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left', editable: false, edittype: 'text', sorttype : 'float', template: searchStringOptions, 
						editoptions: {
							dataInit:
								function (e) { 
									fn_grid_com.griOssVersions(e, currentOssName, 'srcList');
									
									$(e).on( "autocompletechange", function() {
										var rowid = (e.id).split('_')[0];
										fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData, srcDiffMsgData);
									});
								}
						}
					},
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
										
										fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData, srcDiffMsgData);
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
											
											fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData,srcDiffMsgData);
										}
									});
								}
	 					}
	 				},
					{name: 'ossNameExistsYn', index: 'ossNameExistsYn', hidden:true},
					{name: 'licenseNameExistsYn', index: 'licenseNameExistsYn', hidden:true},
					{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', editable:true, edittype:'text', hidden:true},
					{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', edittype:'text'},
					{name: 'copyrightText', index: 'copyrightText', width: 150, align: 'left', editable:false, template: searchStringOptions, edittype:"textarea", editoptions:{rows:"5",cols:"24", 
						dataInit:
							function (e) { 
								$(e).on("change", function() {
									var rowid = (e.id).split('_')[0];

									fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData,srcDiffMsgData);
								});
							}
						}
					},
					{name: 'ossDetail', index: 'ossDetail', width: 50, align: 'center', formatter:src_fn.displayOssDetail, search : false, //template: searchStringOptions,
						sorttype: function (cell, rowData) {
							var rtnVal = rowData.ossNameExistsYn;

							if(!rtnVal) {
								rtnVal = "N";	
							}
							
				            return rtnVal;
                        }			
					},
					{name: 'licenseDetail', index: 'licenseDetail', width: 50, align: 'center', formatter:src_fn.displayLicenseDetail, search : false, //template: searchStringOptions,
						sorttype: function (cell, rowData) {
							var rtnVal = rowData.licenseNameExistsYn;

							if(!rtnVal) {
								rtnVal = "N";	
							}
							
				            return rtnVal;
                        }		
					},
					{name: 'userGuide', index: 'userGuide', width: 50, align: 'center', formatter:src_fn.displayGuide, unformatter:fn_grid_com.unformatter, search : false, //template: searchStringOptions,
						sorttype: function (cell, rowData) {
							var rtnVal = rowData.licenseUserGuideYn;

							if(!rtnVal) {
								rtnVal = "N";	
							}
							
				            return rtnVal;
                        }	
					},
					{name: 'cveId', index: 'cveId', hidden:true},
					//{name: 'cvssScore', index: 'cvssScore', hidden:true},
					{name: 'cvssScore', index: 'cvssScore', width: 50, align: 'center', formatter:src_fn.displayVulnerability, unformatter:fn_grid_com.unformatter, template: searchNumberOptions,
						sorttype: function (cell, rowData) {
							var rtnVal = rowData.cvssScore;

							if(!rtnVal) {
								rtnVal = "0";
							}
							
				            return rtnVal;
                        }
					},
					{name: 'obligation', index: 'obligation', width: 50, align: 'center', formatter: src_fn.displayNotify, unformat: src_fn.unDisplayNotify, search : false, //template: searchStringOptions, 
						sorttype: function (cell, rowData) {
							var rtnVal = rowData.obligationType;

							if(!rtnVal) {
								rtnVal = "99";	
							}
							
				            return rtnVal;
                        }
					},
					{name: 'notify', index: 'notify', width: 40, align: 'center', hidden:true},
					{name: 'source', index: 'source', width: 40, align: 'center', hidden:true},
					{name: 'restriction', index: 'restriction', width: 50, align: 'center', formatter: fn_grid_com.displayLicenseRestriction, unformat: fn_grid_com.unformatter, sortable : false, search : false},
					{name: 'excludeYn', index: 'excludeYn', width: 50, align: 'center', formatter: fn_grid_com.cboxFormatter, unformat: fn_grid_com.cboxUnFormatter, search: false,
	 					editoptions: {
							dataInit:
								function (e) { 
									$(e).on("change", function() {
										var rowid = (e.id).split('_')[0];

										fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData, srcDiffMsgData);
									});
								}
						}	
					},
					{name: 'licenseDiv', index: 'licenseDiv', width: 100, align: 'left', editable:false, hidden:true},
					{name: 'licenseUserGuideYn', index: 'licenseUserGuideYn', hidden:true},
					{name: 'licenseUserGuideStr', index: 'licenseUserGuideStr', hidden:true},
					{name: 'obligationGrayFlag', index: 'obligationGrayFlag', hidden:true},
					{name: 'obligationMsg', index: 'obligationMsg', hidden:true}
				],
				autoencode: true,
				editurl:'clientArray',
	 			autowidth: true,
				height: 'auto',
				gridview: true,
			   	pager: '#srcPager',
				rowNum: 200,
				rowList: [200, 500, 1000, 5000],
				recordpos:'right',
			    toppager:true,
				loadonce:true,
				cellEdit : false,
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
						$("#srcList tr.singleLicenseClass").find("td:first").removeClass("sgexpanded sgcollapsed").find("a").hide();
						
						if(!gridTooltip.existTooltip){
							$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_srcList_obligation"))
								.attr("title", gridTooltip.tooltipCont).tooltip({
									content: function () {
										return $(this).prop('title');
									}
								});
							
							gridTooltip.existTooltip = true;
						}

						tableRefresh();
					}

					//sort후 ValidMsgData reload
					if(isSort){
						isSort = false;
					}
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
					fn_grid_com.setWarningClass(srcList,rowid,["ossName","licenseName"]);
					return true;
				},
				onCellSelect: function(rowid,iCol,cellcontent,e) {
					if(iCol=="2") {
						fn_grid_com.showOssViewPage(srcList, rowid, true, srcValidMsgData, srcDiffMsgData, null, com_fn.getLicenseName);
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					// 체크 박스 영역 제외
					if(iCol!="25") {
						cleanErrMsg("srcList", rowid);
						fn_grid_com.setCellEdit(srcList, rowid, srcValidMsgData, srcDiffMsgData, null, com_fn.getLicenseName);

						// 서브 그리드 제외
						ondblClickRowBln = false;

						$('#'+rowid+'_licenseName').addClass('autoCom');
		 	 			$('#'+rowid+'_licenseName').css({'width' : '100%'});
						var result = $('#'+rowid+'_licenseName').val().split(",");

						result.forEach(function(cur,idx){
							if(cur != ""){
								var mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'><span class=\"btnLicenseShow\" ondblclick='com_fn.showLicenseInfo(this)'>" + cur + "</span><button onclick='com_fn.deleteLicenseRenewal(this)'>x</button></span><br/>";
								$('#'+rowid+'_licenseNameBtn').append(mult);
							}
						});
                                                var nextCol = srcList.jqGrid('getGridParam', 'colModel')[iCol].name
                                                var nextRow = rowid
                                                $('#'+nextRow+"_"+nextCol).focus();
						$('#'+rowid+'_licenseName').val("");
					}
				},
				onPaging: function(action) {
					cleanErrMsg("srcList");
					
					fn_grid_com.totalGridSaveMode('srcList');
				},
				gridComplete : function() {
					cleanErrMsg("srcList");
					
					if(srcValidMsgData) {
						gridValidMsgNew(srcValidMsgData, "srcList", "SELF");
					}
					
					if(srcDiffMsgData) {
						gridDiffMsg(srcDiffMsgData, "srcList", "SELF");
					}
				}
			});
			
			srcList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
			srcList.jqGrid('navGrid',"#srcPager",{add:true,edit:false,del:true,search:false,refresh:false
													  , addfunc: function () { saveFlag = false; fn_grid_com.rowAddNew('srcList',srcList,"main", null, com_fn.getLicenseName);}
													  , delfunc: function () { fn_grid_com.rowDelNew(srcList,"main");}
													  , cloneToTop:true
			});
			
			$('#srcList').closest(".ui-jqgrid-bdiv").css({"height":"500px", "overflow-y" : "scroll"});
		}
	};
	
	var gridTooltip = {
			typeCodes : [],
			tooltipCont : "<div class=\"tooltipData500\"><span style=\"color:red;\">Obligation is unclear. Obliagtion이 불명확한 경우입니다.</span>"
		                   +"<dl><dt><span>non-included license : License is different from registered in FOSSLight Hub.</span></dt></dl><br>"
		                   +"<dl><dt><span>unconfirmed oss : It is a new OSS not registered in FOSSLight Hub.</span></dt></dl><br>"
		                   +"<dl><dt><span>unconfirmed version : It is the new OSS version that is not registered with FOSSLight Hub.</span></dt></dl><br>"
		                   +"<dl><dt><span>unconfirmed license : It is a new license not registered in FOSSLight Hub.</span></dt></dl>"
		                   +"</div>",
			existTooltip : false
	};

	var com_fn = {
		tabInit : function(){
			var initDiv = '${initDiv}';
			var tabContent = $(".tabContent");
			var tabMenuA = $(".tabMenu a");
			
			tabContent.hide();
			tabMenuA.click(function () {
				com_fn.fnTabChange(this);
			});
			
			var tabDefaultIdx = "0";
			
			tabMenuA.eq(tabDefaultIdx).click();
		},
		fnTabChange: function(target){
			var tabContent = $(".tabContent");
			var tabMenuA = $(".tabMenu a");
			
			$(".tabMenu a span").remove();
			
			tabMenuA.eq("0").text("OSS List");
			tabMenuA.eq("1").text("Notice");
			
			var tag = "<span>"+$(target).text()+"</span>";
			
			$(target).html(tag);
			
			tabContent.hide();
			activeTabText = $(target).text();
			activeTab = $(target).attr("rel");
			
			$("#" + activeTab).show();
		},
		deleteLicense : function(target){
			$(target).parent().remove();
		},
		deleteLicenseRenewal : function(target){
			$(target).parent().next().remove();
			$(target).parent().remove();
		},
		getLicenseName : function(obj) {
			return obj.licenseName.replace(/(<([^>]+)>)/ig, ",").split(",").reduce(function(arr, cur){
			    if(cur.toUpperCase() != "X" && cur != ""){
			        arr.push(cur);
			    }

			    return arr;
			}, []).join(",");
		},
		exitCell : function(_mainLastsel, target) {
			if(_mainLastsel != -1) {
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

		        	srcMainData = param;
		        	src_grid.load();

		        	// total record 표시
					$("#srcList_toppager_right, #srcPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+srcMainData.length+'</div>');
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
	};

	var notice_evt = {
		init : function(){
			$("#append").click(function(e){
				var checked = $(this).prop("checked");
				$("#editAppendedYn").val(checked ? "Y" : "N");
				fn.appendEditVisible(this);
				
				if(CKEDITOR.instances.editor2) {
					CKEDITOR.instances.editor2.setReadOnly(!checked);
				}
			});
			
			$("[name='btnEditOssNotice']").change(function(e){
				var checked = ($("[name='btnEditOssNotice']:checked").val() == "Y");
				$("#editNoticeYn").val(checked ? "Y" : "N");
				fn.defaultNotice(checked);
			});
			
			$("#companyName").click(function(e){
				var checked = $(this).prop("checked");
				$("#editCompanyYn").val(checked ? "Y" : "N");
				$("#editCompanyName").attr("disabled", !checked);
			});
			
			$("#ossDistributionSite").click(function(e){
				var checked = $(this).prop("checked");
				$("#editDistributionSiteUrlYn").val(checked ? "Y" : "N");
				$("#editOssDistributionSite").attr("disabled", !checked);
			});
			
			$("#email").click(function(e){
				var checked = $(this).prop("checked");
				$("#editEmailYn").val(checked ? "Y" : "N");
				$("#editEmail").attr("disabled", !checked);
			});
			
			$("#hideOssVersion").click(function(e){
				var checked = $(this).prop("checked");
				$("#hideOssVersionYn").val(checked ? "Y" : "N");
			});

			// noticePreview 버튼 
			$('#noticePreview').click(function(e){
				e.preventDefault();
				
				fn.saveOrGetNotice('preview');
			});

			$('#packageDocDownload').click(function(e){
				var type = $('#docType').val();
				
				if(type == 'noticeDownload') {
					fn.downloadNotice();
				} else if(type == 'noticeTextDownload') {
					fn.downloadNoticeText();
				} else if(type == 'noticeSimpleDownload') {
					fn.downloadNoticeSimple();
				} else if(type == 'noticeTextSimpleDownload') {
					fn.downloadNoticeTextSimple();
				} else if(type == 'spdxSpreadSheet') {
					fn.downloadSpdxSpreadSheetExcel();
				} else if(type == 'spdxRdf') {
					fn.downloadSpdxRdf();
				} else if(type == 'spdxTag') {
					fn.downloadSpdxTag();
				} else if(type == 'spdxJson') {
					fn.downloadSpdxJson();
				} else if(type == 'spdxYaml') {
					fn.downloadSpdxYaml();
				}
			});

			$("#noticeSave").on("click", function(){
				hideErrMsg();
				fn.saveOrGetNotice('save');
			});
		}		
	};
//]]>
</script>