<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	
	var userRole = '${sessUserInfo.authority}';
	var curIdenStatus = '${project.verificationStatus}';
	var projectStatus = '${project.status}';
	var modifyCommentId = '';
	var commentIdx= '';
	var verified = '${project.statusVerifyYn}';
	verified = (verified != "N" ? true : false);
	var isAdmin = ${ct:isAdmin()};
	var $editor;
	var gStatus = "";
	var tempHandler = "";
	var distributionStatus = '${project.destributionStatus}';
	var deleteFileList = new Array();
	var isAndroid = '${project.androidFlag}'; 
	
	$(document).ready(function () {
		'use strict';
		datas.init();
		evt.init();
		initSample();
		
		// 버튼 컨트롤
		fn.btnCtl(userRole, curIdenStatus);
		
		$("#list").jqGrid({ 
			datatype: 'local',
			data: datas.ossList,
			colNames: ['ID_KEY','ID','refComponentId','Reference','OSS ID','OSS Name','OSS<br/>Version','Download Location','Homepage','License','Path of source code in the OSS Package','File Count'],
			colModel: [
				{name: 'componentId', index: 'componentId', width: 40, align: 'center', key:true, hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center'},
				{name: 'refComponentId', index: 'referenceDiv', width: 40, align: 'center', hidden:true},
				{name: 'referenceDiv', index: 'referenceDiv', width: 40, align: 'center', editable:false},
				{name: 'ossId', index: 'ossId', width: 150, align: 'left', hidden:true},
				{name: 'ossName', index: 'ossName', width: 150, align: 'left', editable:false},
				{name: 'ossVersion', index: 'ossVersion', width: 50, align: 'left', editable:false},
				{name: 'downloadLocation', index: 'downloadLocation', width: 70, align: 'left', editable:false},
				{name: 'homepage', index: 'homepage', width: 70, align: 'left', formatter: 'link2', editable:false},
				{name: 'licenseName', index: 'licenseName', width: 150, align: 'left', editable:false},
				{name: 'filePath', index: 'filePath', width: 250, align: 'left', editable:true, formatter: fn.textBox, unformat:fn.unformatterTextBox},
				{name: 'verifyFileCount', index: 'verifyFileCount', width: 50, align: 'left', editable:false}
			],
		   	rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
		   	editurl:'clientArray',
 			autowidth: true,
			pager: '#pager',
			gridview: true,
			sortable: function (permutation) {
			},
			viewrecords: true,
			sortorder: 'desc',
			height: 'auto',
			caption : '* Open Source List to disclose source code',
			loadComplete: function() {
				// load후 filepath editmode로 변경
			    var $this = $(this), ids = $this.jqGrid('getDataIDs'), i, l = ids.length;

			    for (i = 0; i < l; i++) {
			        $this.jqGrid('editRow', ids[i], false); // 	true로 변경하면 엔터키 입력시 saverow되어버림
			    }
			}
		});
		
		$("#list").jqGrid('navGrid',"#pager",{add:false,edit:false,del:false,search:false,refresh:false});

		if(datas.ossList.length < 1){
			$("[name='btnSavePath']").hide();
			
			if(isAdmin && $("#div_approve")) {
				$("#div_approve").hide();
			}
		}
		
		if($(".warningPop").length > 0 && "CONF" != curIdenStatus 
			&& (!$('input[name=fileSeq_1]') || $('input[name=fileSeq_1]').val() == "")){
			alertify.alert($("body > div.pop.warningPop > div.popdata").html(), function(){});
		}
		
		if(userRole == "ROLE_USER"){
			if( curIdenStatus && curIdenStatus != "PROG" && curIdenStatus != "NA"){
				$(".editYn").attr("readonly", true);
				$(".editYn").css("background-color", "#f3f6f8");
				
				if(CKEDITOR.instances.editor2) {
					CKEDITOR.instances.editor2.config.readOnly = true;
				}
			}
		} else {
			if(curIdenStatus == "CONF"){
				$(".editYn").attr("readonly", true);
				$(".editYn").css("background-color", "#f3f6f8");
				if(CKEDITOR.instances.editor2) {
					CKEDITOR.instances.editor2.config.readOnly = true;
				}
			}
		}
		
		if("${ossNotice.editNoticeYn}" == "N"){
			if(CKEDITOR.instances.editor2) {
				CKEDITOR.instances.editor2.config.readOnly = true;
			}
		}
		
		$("#docType > [value^='noticeDownload']").trigger("change");
		
		com_fn.tabInit();

		// 20210617_autoVerify Change Alert ADD
		$("input:checkbox[name='autoVerify']").change(function(){
			if($("input:checkbox[name='autoVerify']").is(":checked") == true){
				alertify.alert('Verify when file is uploaded', function(){});
			}
		});
	});
	
	var datas = {
		verify : ${empty verify ? '{}':verify},
		ossList : ${empty ossList ? '{}':ossList},
		init : function(){
			modifyCommentId = datas.verify.data.commentIdx;
			commentIdx = datas.verify.data.commentIdx;   
			if(isAdmin) {
				if('${project.withoutVerifyYn}' == "Y") {
					$("#approve").prop("checked",true);
				} else {
					$("#approve").prop("checked",false);
				}
				
				$("#approve").click(function(){
					if($("#approve").is(":checked")){
						$("input[name=withoutVerifyYn]").val($("#approve").val());
					} else {
						$("input[name=withoutVerifyYn]").val("N");
					}
				});
				
				$("#ignoreBinaryDb").click(function(){
					if($("#ignoreBinaryDb").is(":checked")){
						$("input[name=ignoreBinaryDbFlag]").val($("#ignoreBinaryDb").val());
					} else {
						$("input[name=ignoreBinaryDbFlag]").val("N");
					}
				});
			} else {
				$("input[name=withoutVerifyYn]").val("N");
			}
			
			if($("[name='fileSeq_1']").val()) {
				$("#fileUplWarnMessage_1").hide();
			} else {
				$("#fileUplWarnMessage_1").show();
			}
			
			if($("[name='fileSeq_2']").val()) {
				$("#fileUplWarnMessage_2").hide();
			} else {
				$("#fileUplWarnMessage_2").show();
			}
			
			if($("[name='fileSeq_3']").val()) {
				$("#fileUplWarnMessage_3").hide();
			} else {
				$("#fileUplWarnMessage_3").show();
			}
			
			fn.appendEditVisible($("#append"));
			fn.initNotice();
			
			$("#deleteFlag").val("N");
		}
	}
	
	
	var evt = {
		uploadStatus:false,
		init : function(){
			$('#registFile_1').uploadFile();
			$('#registFile_2').uploadFile();
			$('#registFile_3').uploadFile();
			$('#wgetUrl').hide();
			$('.OKcolse').click(function(){
				$('#blind_wrap').hide();
				$('.warningPop').hide();
			});
			
			// //OSS Notice 등록
			// $("#save").on('click', function(){
			// 	hideErrMsg();
			// 	fn.saveOrGetNotice('save');
			// });
			
			/** 
				Oss Notice Event Handler (FullCustom)
			**/
			$('input[id*="companyNameFull"]').on('blur', function(){
				var value = $(this).val();
				
				$('input[id*="companyNameFull"]').val(value);
			});
			$('input[id*="companyNameShort"]').on('blur', function(){
				var value = $(this).val();
				
				$('input[id*="companyNameShort"]').val(value);
			});
			$('input[id*="distributionSiteUrl"]').on('blur', function(){
				var value = $(this).val();
				
				$('input[id*="distributionSiteUrl"]').val(value);
			});
			$('input[id*="email"]').on('blur', function(){
				var value = $(this).val();
				
				$('input[id*="email"]').val(value);
			});
			$('#useCompanyNameTitle').on('click', function(){
				var checked = $(this).attr('checked');
				
				if(!checked) {
					$("div.useCompanyNameTitle").hide();
				} else {
					$("div.useCompanyNameTitle").show();
				}
			});
			$('#distributedOtherCompany').on('click', function(){
				var checked = $(this).attr('checked');
				
				if(!checked) {
					$("div.distributedOtherCompany").hide();
				} else {
					$("div.distributedOtherCompany").show();
				}
			});
			$('#mergedOtherOssNotice').on('click', function(){
				var checked = $(this).attr('checked');

				if(!checked) {
					$("div.mergedOtherOssNotice").hide();
				} else {
					$("div.mergedOtherOssNotice").show();
				}
			});
			$('#accompaniedSourceCode').on('click', function(){
				var checked = $(this).attr('checked');
				
				if(!checked) {
					$("div.accompaniedSourceCode").hide();
				} else {
					$("div.accompaniedSourceCode").show();
				}
			});
			
			//다운로드 허용 플래그
			// Allow users to download check event
			$('[name^="chkAllowDownload"]').on('change', function(){
				var targetid = $(this).data('targetid');
				
				$('#'+targetid).val($(this).is(':checked')? 'Y':'N');
			});

			
			// [Pakage Document Download START]
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
				}
			});
			
			$('#chkUseCustomNotice').change(function(e){
				if($(this).is(':checked')) {
					// Use Custom Notice 값 설정
					$('#useCustomNoticeYn').val('Y');
					
					// Notice Editor 버튼 활성화
					$('#noticeEditor').prop('disabled', false);
					$('#noticeEditor').css('opacity', 1);
					$("[name='btnEditOssNotice']").attr("disabled", true);
					
					fn.defaultNotice(false);
				} else {
					// Use Custom Notice 값 설정
					$('#useCustomNoticeYn').val('N');
					
					// Notice Editor 버튼 활성화
					$('#noticeEditor').prop('disabled', true);
					$('#noticeEditor').css('opacity', 0.5);
					$("[name='btnEditOssNotice']").attr("disabled", false);
					
					var checked = ($("[name='btnEditOssNotice']:checked").val() == "Y");
					fn.defaultNotice(checked);
				}
			});
			
			$('#noticeEditor').click(function(e){
				fn.saveOrGetNotice('editor');
			});
			
			// noticePreview 버튼 
			$('#noticePreview').click(function(e){
				e.preventDefault();
				
				fn.saveOrGetNotice('preview');
			});
			
			$('#noticeDownload').click(function(e){
				e.preventDefault();
				
				fn.downloadNotice();
				
				return false;
			});
			
			//notice text download
			$('#noticeTextDownload').click(function(e){
				e.preventDefault();
				
				fn.downloadNoticeText();
				
				return false;
			});
			
			$('#noticeSimpleDownload').click(function(e){
				e.preventDefault();
				
				fn.downloadNoticeSimple();
				
				return false;
			});
			$('#noticeTextSimpleDownload').click(function(e){
				e.preventDefault();
				
				fn.downloadNoticeTextSimple();
				
				return false;
			});
			
			$('#spdxSpreadSheet').click(function(e){
				fn.downloadSpdxSpreadSheetExcel();
			});
			
			$('#spdxRdf').click(function(e){
				fn.downloadSpdxRdf();
			});
			
			$('#spdxTag').click(function(e){
				fn.downloadSpdxTag();
			});
			
			//// [Pakage Document Download END]
			
			$("#identificationTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Identify");
				
				if(idx != "") {
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/4"/>');
				}
			});
			$("#distributionTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Distribute");
				
				if(idx != "") {
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Distribute', '#<c:url value="/project/distribution/'+prjId+'"/>');
				}
			});
			$("#editTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Project");
				
				if(idx != ""){
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Project', '#<c:url value="/project/edit/'+prjId+'"/>');
				}
			});
			
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
		}
	}
	
	var fn = {
		
		onRegistSuccess : function(json, status){
			if(json.isValid == 'false'){
            	alertify.error('<spring:message code="msg.common.valid" />', 0);
				createValidMsgComplex(json);
				
				$.each(json,function(key,value) {
					if("isValid" != key && "validMsg" != key) {
						if($('input[name*='+key+']').length > 0) {
							$('input[name*='+key+']').next("span").next("span.retxt,div.retxt").html(value).show();
						}
					}
				});
				
				var checkedFlag = $("[name='btnEditOssNotice']:checked").val() == "Y";

			    fn.defaultNotice(checkedFlag);
			    
				loading.hide();
			} else {
				if(!$("#companyName").prop("checked")) {
					$("#editCompanyName").attr("disabled", true);
				}
				
				if(!$("#ossDistributionSite").prop("checked")) {
					$("#editOssDistributionSite").attr("disabled", true);
				}
				if(!$("#email").prop("checked")) {
					$("#editEmail").attr("disabled", true);
				}
				
				if(gStatus == "REQ"){
					gStatus = ""; // global status clear
					fn.procRequest(tempHandler);
					tempHandler = ""; // temp clear
				} else {
					alertify.alert('<spring:message code="msg.common.success" />', function(){
						createTabInFrame('${project.prjId}_Packaging', '#<c:url value="/project/verification/1/${project.prjId}"/>');
					});
				}
			}
		},
		btnCtl: function(role, status){
//			206 CONF	Confirm
//			206 NA		N/A
//			206 PROG	Progress
//			206 REQ		Request
//			206 REV		Review

			// 상태에 따른 버튼 컨트롤 
			var btn_div = $(".projdecBtn");
			var btn_confirm = $(".confirm");
			var btn_reject = $(".reject");
			var btn_review = $(".review");
			var btn_restart = $(".restart");
			var btn_save = $("#save");
			var btn_verify = $("[name='verify']");
			var btn_savePath = $("[name='btnSavePath']");
			
			
			btn_div.show();
			if(role=="ROLE_ADMIN"){ // 관리자 권한 일 경우
				switch(status){
					case "":
						btn_confirm.hide();btn_reject.hide();btn_review.show();btn_restart.hide();
						btn_save.show();
						btn_verify.show();
						btn_savePath.show();
						$("#approve").attr("disabled",false);

						break;
					case "PROG":
						btn_confirm.hide();btn_reject.hide();btn_review.show();btn_restart.hide();
						btn_save.show();
						btn_verify.show();
						btn_savePath.show();
						$("#approve").attr("disabled",false);

						break;
					case "REQ":
						btn_confirm.hide();btn_reject.hide();btn_review.hide();btn_restart.show();
						btn_save.show();
						btn_verify.show();
						btn_savePath.show();
	
						$("#approve").attr("disabled",true);

						break;
						
					case "REV":
						btn_confirm.show();btn_reject.show();btn_review.hide();btn_restart.hide();
						btn_save.show();
						btn_verify.show();
						btn_savePath.show();
	
						$("#approve").attr("disabled",true);

						break;
						
					case "CONF":
						btn_confirm.hide();btn_reject.show();btn_review.hide();btn_restart.hide();
						btn_save.hide();
						btn_verify.show();
						btn_savePath.hide();
						
						// 고지문구 버튼 비활성
						$('#chkUseCustomNotice').prop('disabled', true);
						$('#noticeEditor').prop('disabled', true);
						$('#noticeEditor').css('opacity', 0.5);
						
						$("#approve").attr("disabled",true);
						break;
						
				}
			}
			else if('${project.viewOnlyFlag}' == 'Y'){
				btn_confirm.hide();btn_reject.hide();btn_review.hide();btn_restart.hide();
				btn_save.hide();btn_savePath.hide();
			}
			else{	// 일반 사용자 일 경우
				switch(status){
					case "":
						btn_review.show();
						btn_confirm.hide();btn_reject.hide();btn_restart.hide();
						btn_save.show();
						break;
					case "PROG":
						btn_review.show();
						btn_confirm.hide();btn_reject.hide();btn_restart.hide();
						btn_save.show();
						break;
						
					case "REQ":
						btn_confirm.hide();btn_reject.show();btn_review.hide();btn_restart.hide();
						btn_save.hide();
						break;
						
					case "REV":
						btn_confirm.hide();btn_reject.hide();btn_review.hide();btn_restart.hide();
						btn_save.hide();
						break;
						
					case "CONF":
						btn_confirm.hide();btn_reject.show();btn_review.hide();btn_restart.hide();
						btn_save.hide();
						btn_savePath.hide();
						break;
				}
			}
		},
		exeProjectStatus : function(data, status){
			curIdenStatus = status;
			$(".commentBtn.open").trigger( "click" );
			$.ajax({
				url : '<c:url value="${suffixUrl}/project/updateProjectStatus"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					resetEditor(CKEDITOR.instances.editor);
					
					fn.btnCtl(userRole, status);
					reloadTabInframe('<c:url value="/project/list"/>');
					
					alertify.alert('<spring:message code="msg.common.success" />', function(){
						createTabInFrame('${project.prjId}_Packaging', '#<c:url value="/project/verification/${project.prjId}"/>');
					});
				},
				error: function(data){
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
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
				url : '<c:url value="/project/verification/saveNoticeAjax"/>',
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
			
			// if(flag == 'save') {
			// 	customUrl      = '<c:url value="${suffixUrl}/selfCheck/verification/saveAjax"/>';
			// 	customDataType = "json";
			// 	customSucess   = fn.onRegistSuccess;
			// 	customError	   = function(data){
			// 		if(!$("#companyName").prop("checked")) {
			// 			$("#editCompanyName").attr("disabled", true);
			// 		}
					
			// 		if(!$("#ossDistributionSite").prop("checked")) {
			// 			$("#editOssDistributionSite").attr("disabled", true);
			// 		}
					
			// 		if(!$("#email").prop("checked")) {
			// 			$("#editEmail").attr("disabled", true);
			// 		}
					
			// 		loading.hide();
	        //     	alertify.error('<spring:message code="msg.common.valid2" />', 0);
			// 	}
				
			// 	if($("#editCompanyName").val() != ""){
			// 		$("#editCompanyName").attr("disabled", false);
			// 	}
				
			// 	if($("#editOssDistributionSite").val() != ""){
			// 		$("#editOssDistributionSite").attr("disabled", false);
			// 	}
				
			// 	if($("#editEmail").val() != ""){
			// 		$("#editEmail").attr("disabled", false);
			// 	}
			// } else 
			if(flag == 'preview') {
				customUrl      = '<c:url value="/selfCheck/verification/noticeAjax"/>';
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
				customUrl      = '<c:url value="/selfCheck/verification/noticeAjax"/>';
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
				customUrl      = '<c:url value="/selfCheck/verification/noticeAjax"/>';
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
				url :'<c:url value="/selfCheck/verification/makeNoticePreview"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/selfCheck/verification/downloadNoticePreview?id='+data.validMsg+'"/>';

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
				url :'<c:url value="/selfCheck/verification/makeNoticeText"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/selfCheck/verification/downloadNoticePreview?id='+data.validMsg+'"/>';

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
				url :'<c:url value="/selfCheck/verification/makeNoticeSimple"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/selfCheck/verification/downloadNoticePreview?id='+data.validMsg+'"/>';

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
				url :'<c:url value="/selfCheck/verification/makeNoticeTextSimple"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/selfCheck/verification/downloadNoticePreview?id='+data.validMsg+'"/>';
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
			$.ajax({
				type: "POST",
				url: '<c:url value="/selfCheck/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdx", "parameter":'${project.prjId}'}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/selfCheck/spdxdownload/getFile?id='+data.validMsg+'"/>';
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		
		downloadSpdxRdf : function() {
			$.ajax({
				type: "POST",
				url: '<c:url value="/selfCheck/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdxRdf", "parameter":'${project.prjId}'}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/selfCheck/spdxdownload/getFile?id='+data.validMsg+'"/>';
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		
		downloadSpdxTag : function() {
			$.ajax({
				type: "POST",				   
				url: '<c:url value="/selfCheck/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdxTag", "parameter":'${project.prjId}'}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/selfCheck/spdxdownload/getFile?id='+data.validMsg+'"/>';
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
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
				
				$("#chkAllowDownloadNoticeText").attr("disabled", !checked);
				$("#chkAllowDownloadSimpleHTML").attr("disabled", !checked);
				$("#chkAllowDownloadSimpleText").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXSheet").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXRdf").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXTag").attr("disabled", !checked);
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
				
				$("#chkAllowDownloadNoticeText").attr("disabled", !checked);
				$("#chkAllowDownloadSimpleHTML").attr("disabled", !checked);
				$("#chkAllowDownloadSimpleText").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXSheet").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXRdf").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXTag").attr("disabled", !checked);
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
					switch(curIdenStatus){
						case "CONF":
							btn_Save.hide();
							$("[name='btnEditOssNotice']").attr("disabled", true)
							fn.defaultNotice(false, "init");

							break;
						default:
							btn_Save.show();
							fn.defaultNotice(checked, "init");	

							break;
					}
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
		procRequest : function(e){
			e.preventDefault();
			
			var data = {"prjId" : '${project.prjId}'
					  , "verificationStatus" : "REQ"
					  , "useCustomNoticeYn" : $('#useCustomNoticeYn').val()
					  , "userComment" : CKEDITOR.instances['editor'].getData()
					  , "referenceDiv":"12"
					  , "allowDownloadNoticeHTMLYn" : $("#allowDownloadNoticeHTMLYn").val()
					  , "allowDownloadNoticeTextYn" : $("#allowDownloadNoticeTextYn").val()
					  , "allowDownloadSimpleHTMLYn" : $("#allowDownloadSimpleHTMLYn").val()
					  , "allowDownloadSimpleTextYn" : $("#allowDownloadSimpleTextYn").val()
					  , "allowDownloadSPDXSheetYn"  : $("#allowDownloadSPDXSheetYn").val()
					  , "allowDownloadSPDXRdfYn" 	: $("#allowDownloadSPDXRdfYn").val()
					  , "allowDownloadSPDXTagYn" 	: $("#allowDownloadSPDXTagYn").val()
			};
			
			//공개의무 리스트가 있고 파일업로드가 완료된 상태이며 verify가 완료 되었을때
			var fileSeq = $("input[name='fileSeq_1']").val();
			var withoutVerifyYn = $('input[name=withoutVerifyYn]').val();
			
			if(datas.ossList.length > 0) {
				if((fileSeq && verified) || withoutVerifyYn == "Y") {
					fn.exeProjectStatus(data, "REQ");
				} else {
					loading.hide();
					
					alertify.error('<spring:message code="msg.project.required.verify" />', 0);
				}
			} else {
				fn.exeProjectStatus(data, "REQ");
			}
		},
		displayComment : function(cellvalue, options, rowObject){
			var display = "";
			
			if(cellvalue !=""){
				var tmpStr = new RegExp();
				tmpStr = /[<][^>]*[>]/gi;
				
				display ="<div style=\"height : 29px; overflow: hidden;\">"+cellvalue.replace(tmpStr , "")+"</div>";
			}
			
			return display;
		},
		
		// Grid input cell button
		textBox : function(cellvalue, options, rowObject) {
			if(!!rowObject.filePath || "Y" == isAndroid) {
				return rowObject.filePath;
			} else {
				var textBox = rowObject.ossName;
				
				if(!!rowObject.ossVersion) {
					textBox += "-" + rowObject.ossVersion;
				}
				
				textBox += "/";
	
				return textBox;
			}
		},
		unformatterTextBox : function(cellvalue, options, rowObject){
			return cellvalue;
		}
	}
	
	var com_fn = {
		tabInit : function(){
			var tabMenuA = $(".tabMenu a");

			tabMenuA.click(function () {
				com_fn.fnTabChange(this);
			});

			tabMenuA.click();

		},
		fnTabChange: function(target){
			var tabMenuA = $(".tabMenu a");
			
			$(".tabMenu a span").remove();
			
			tabMenuA.eq("0").text("OSS Notice");
			
			var tag = "<span>"+$(target).text()+"</span>";
			
			$(target).html(tag);
			
			activeTabText = $(target).text();
			activeTab = $(target).attr("rel");
			
			$("#" + activeTab).show();

		}
	}
</script>