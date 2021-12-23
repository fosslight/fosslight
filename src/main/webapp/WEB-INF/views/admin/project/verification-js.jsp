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
		packaging.init();
		
		showHelpLink("Project_List_Packaging");
		
		if($("#editor2").length > 0) {
			initSample2();
		}
		
		$('.btnCommentHistory').on('click', function(e){
			e.preventDefault();
			openCommentHistory('<c:url value="/comment/popup/prj/${project.prjId}"/>');
		});
		
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
				alertify.alert("<spring:message code='msg.project.upload.verify' />", function(){});
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
			
			$('#registFile_1').uploadFile({
				url : '<c:url value="/project/verification/registFile?prjId='+datas.verify.data.prjId+'&fileSeq=1"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				onSuccess : function(e, data){
					var result = jQuery.parseJSON(data);

					result.forEach(function(item){
						var appendHtml = '<span style="margin-left:20px;">'+item[0].createdDate+'</span>';
						var _url = '<c:url value="/download/'+item[0].registSeq+'/'+item[0].fileName+'"/>';
						
						$('.uploadList_1 ul').append('<li><span><strong><a href="'+_url+'">'+item[0].originalFilename+appendHtml+'</a></strong><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,\''+item[0].registSeq+'\', \'1\')"></span></li>');
						$('input[name=fileSeq_1]').val(item[0].registSeq);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						$("#fileUplWarnMessage_1").hide();
					});

					$('.verifyFile_1').hide();
					$("#uploadAdd_1").show();
					$("#uploadRemove_1").hide();

					// verified 초기화
					verified = false;
					
					evt.uploadStatus = true;

					fn.autoVerify();
				},
			});
			
			$('#registFile_2').uploadFile({
				url : '<c:url value="/project/verification/registFile?prjId='+datas.verify.data.prjId+'&fileSeq=2"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				onSuccess : function(e, data){
					var result = jQuery.parseJSON(data);
					
					result.forEach(function(item){
						var appendHtml = '<span style="margin-left:20px;">'+item[0].createdDate+'</span>';
						var _url = '<c:url value="/download/'+item[0].registSeq+'/'+item[0].fileName+'"/>';
						
						$('.uploadList_2 ul').append('<li><span><strong><a href="'+_url+'">'+item[0].originalFilename+appendHtml+'</a></strong><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,\''+item[0].registSeq+'\', \'2\')"></span></li>');
						$('input[name=fileSeq_2]').val(item[0].registSeq);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						$("#fileUplWarnMessage_2").hide();
					});

					$('.verifyFile_2').hide();
					$("#uploadAdd_2").show();
					$("#uploadRemove_2").hide();

					// verified 초기화
					verified = false;
					
					evt.uploadStatus = true;

					fn.autoVerify();
				},
			});
			
			$('#registFile_3').uploadFile({
				url : '<c:url value="/project/verification/registFile?prjId='+datas.verify.data.prjId+'&fileSeq=3"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				onSuccess : function(e, data){
					var result = jQuery.parseJSON(data);

					result.forEach(function(item){
						var appendHtml = '<span style="margin-left:20px;">'+item[0].createdDate+'</span>';
						var _url = '<c:url value="/download/'+item[0].registSeq+'/'+item[0].fileName+'"/>';
						
						$('.uploadList_3 ul').append('<li><span><strong><a href="'+_url+'">'+item[0].originalFilename+appendHtml+'</a></strong><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,\''+item[0].registSeq+'\', \'3\')"></span></li>');
						$('input[name=fileSeq_3]').val(item[0].registSeq);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						$("#fileUplWarnMessage_3").hide();
					});

					$('.verifyFile_3').hide();
					$("#uploadRemove_3").hide();

					// verified 초기화
					verified = false;
					
					evt.uploadStatus = true;

					fn.autoVerify();
				},
			});
			
			//export
			$("[name='export_path']").click(function(){
				var target = $('#list');
				var arr = [];
				arr = target.jqGrid('getDataIDs');
				
				for(var i in arr){
					target.jqGrid('saveRow',arr[i]);
				}
				
				var data = target.jqGrid('getRowData');
				
				for(var i in arr){
					target.jqGrid('editRow',arr[i], true);
				}
				
				$.ajax({
					type: "POST",
					url: '<c:url value="/exceldownload/getExcelPost"/>',
					data: JSON.stringify({"type":"verification", "parameter":JSON.stringify(data)}),
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
			});
			
			$("[name='upload_path']").click(function(){
				$("#changePathPop").show();
				$("#blind_wrap").show();
			});
			
			$("#btnChangePathCancel").click(function(){
				$("#changePathPop").hide();
				$("#blind_wrap").hide();
			});
			
			//upload Verification File
			var accept = '';
			<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
				<c:if test="${file eq '18'}">
				accept = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
				</c:if>
			</c:forEach>
			
			$('#verificationFile').uploadFile({
				url : '<c:url value="/project/verification/uploadVerification"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				allowedTypes:accept,
				onSubmit:function(files) {
					onAjaxLoadingHide = true;
				},
				onError: function(files,status,errMsg,pd) {
					onAjaxLoadingHide = false;
				},
				onCancel: function(files,pd) {
					onAjaxLoadingHide = false;
				},
				onSuccess : function(e, data){
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
					
					if(data.isValid == 'false') {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						var result = data.resultData;
						var target = $('#list');
						var rowData = target.jqGrid("getRowData");
						var rowid = "";
						
						for(var i = 0; i < result.length; i++){
							for(var j = 0; j < rowData.length; j++){
								if(result[i].ossName == rowData[j].ossName && result[i].ossVersion == rowData[j].ossVersion && result[i].licenseName == rowData[j].licenseName){
									rowid = rowData[j].componentId;
								}
							}
							
							target.jqGrid("setCell", rowid, "filePath", result[i].filePath);
						}
						
						target.jqGrid().trigger('reloadGrid');
						
						$("#changePathPop").hide();
						$("#blind_wrap").hide();
					}
				}
			});
			
			//oss bom list 가 없으면 export, verify, fileupload 숨김
			if(datas.ossList.length < 1){
				$(".tabMenu a").eq("1").click();
			}
			
			//Verify 프로세스
			$("[name='verify']").on('click', function(){
				fn.verify();
			});
			
			

			//Verify Save Path
			$("[name='btnSavePath']").on('click', function(){
				gridCleanErrMsg("list"); // 기존 error message 삭제
				var target = $('#list');
				var arr = [];
				arr = target.jqGrid('getDataIDs');
				
				for(var i in arr){
					target.jqGrid('saveRow',arr[i]);
				}
				
				var filePaths = [];
				var componentIds = [];
								
				// empty check
				for(var i in arr){
					var _path = target.jqGrid('getCell',arr[i],'filePath');
					var _componentId = target.jqGrid('getCell',arr[i],'componentId');

					filePaths.push(_path);
					componentIds.push(_componentId);
				}

				// edit mode 원복
				for(var i in arr){
					target.jqGrid('editRow',arr[i], true);
				}
				
				var fileSeqs = [];
				
				if($('input[name=fileSeq_1]').val() != ''){
					fileSeqs.push($('input[name=fileSeq_1]').val());
				}
				
				if($('input[name=fileSeq_2]').val() != ''){
					fileSeqs.push($('input[name=fileSeq_2]').val());
				}
				
				if($('input[name=fileSeq_3]').val() != ''){
					fileSeqs.push($('input[name=fileSeq_3]').val());
				}
				
				var obj ={
					prjId:'${project.prjId}',
					gridFilePaths:filePaths,
					gridComponentIds:componentIds,
					fileSeqs:fileSeqs,
					deleteFlag:$("#deleteFlag").val(),
					statusVerifyYn:$("#verifyFlag").val(),
					deleteFiles: (deleteFileList.length > 0 ? "Y" : "N")
				}
				
				$.ajax({
					url : '<c:url value="/project/verification/savePath"/>',
					type : 'POST',					
					dataType : 'json',
					contentType : 'application/json',
					cache : false,
					data : JSON.stringify(obj) ,
				 	timeout: 1000 * 60 * 10 ,
					success : function(json){
						loading.hide();
						
						if(json.isValid=='false') {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							alertify.alert('<spring:message code="msg.common.success" />', function(){
								createTabInFrame('${project.prjId}_Packaging', '#<c:url value="/project/verification/${project.prjId}"/>');	
							});
						}
					},
					error : function(data){
						loading.hide();
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			});
			
			//OSS Notice 등록
			$("#save").on('click', function(){
				hideErrMsg();
				fn.saveOrGetNotice('save');
			});
			
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
			
			// verConfirm 버튼 
			$('#verConfirm').click(function(e){
				e.preventDefault();

				if(fn.checkVerify()){
					alertify.error('<spring:message code="msg.project.required.verify" />');

					return false;
				}
				
				if($("#append").prop("checked")){
					$('#noticeForm input[name=appendedTEXT]').val(CKEDITOR.instances.editor2.getData().replace(/(<([^>]+)>)/ig, "").trim());
					$('#noticeForm input[name=appended]').val(CKEDITOR.instances.editor2.getData());
				}
				
				$("#editCompanyName").attr("disabled", false);
				$("#editOssDistributionSite").attr("disabled", false);
				$("#editEmail").attr("disabled", false);
				$("#noticeForm input[name=userComment]").val(CKEDITOR.instances['editor'].getData());
				
				
				$('#noticeForm').ajaxForm({
					url :'<c:url value="/project/verification/noticeAjax?confirm=conf"/>',
		            type : 'POST',
		            dataType:"text",
		            cache : false,
		            success: function(data){
		            	var result =jQuery.parseJSON(data);
		            	
		            	if(result.isValid == 'false') {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
		            	} else {
							reloadTabInframe('<c:url value="/project/list"/>');
			            	loading.hide();
			            	
			            	var checkedFlag = $("[name='btnEditOssNotice']:checked").val() == "Y";
						    fn.defaultNotice(checkedFlag);

							alertify.alert('<spring:message code="msg.common.success" />', function() {
								deleteTabInFrame('#<c:url value="/project/verification/${project.prjId}"/>');
								activeTabInFrameList("PROJECT");
							});
		            	}
		            },
		            error : function(data){
		            	loading.hide();
		            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
		            }
				}).submit();
			});
			
			// verReject 버튼 
			$('#verReject').click(function(e){
				e.preventDefault();
				
				if(distributionStatus == "PROC"){
                    var comment = '<spring:message code="msg.project.distribution.loading" />';
					
					alertify.error(comment, 0);
					
					return false;
				}
				
				var innerHtml = '<div class="grid-container" style="width:470px; height:350px;">Are you sure you want to reject?';
				innerHtml    += '	<div class="grid-width-100" style="width:470px; height:310px; margin-top:10px;">';
				innerHtml    += '		<div id="editor3" style="width:470px; height:300px;">' + CKEDITOR.instances['editor'].getData() + '</div>';
				innerHtml    += '	</div>';
				innerHtml    += '</div>';
				
				alertify.confirm().destroy(); // fullCustomize 와 중첩되는 영역을 초기화
				alertify.confirm(innerHtml, function () {
					if(CKEDITOR.instances['editor3'].getData() == ""){
						alertify.alert('<spring:message code="msg.project.required.comments" />', function(){});
						
						return false;
					} else {
						var data = {"prjId" : '${project.prjId}'
								  , "verificationStatus" : "PROG"
								  , "useCustomNoticeYn" : "N"
								  , "userComment" : CKEDITOR.instances['editor3'].getData()
								  , "referenceDiv":"12"
								  , "allowDownloadNoticeHTMLYn" : $("#allowDownloadNoticeHTMLYn").val()
								  , "allowDownloadNoticeTextYn" : $("#allowDownloadNoticeTextYn").val()
								  , "allowDownloadSimpleHTMLYn" : $("#allowDownloadSimpleHTMLYn").val()
								  , "allowDownloadSimpleTextYn" : $("#allowDownloadSimpleTextYn").val()
								  , "allowDownloadSPDXSheetYn"  : $("#allowDownloadSPDXSheetYn").val()
								  , "allowDownloadSPDXRdfYn" 	: $("#allowDownloadSPDXRdfYn").val()
								  , "allowDownloadSPDXTagYn" 	: $("#allowDownloadSPDXTagYn").val()
								  , "allowDownloadSPDXJsonYn"	: $("#allowDownloadSPDXJsonYn").val()
								  , "allowDownloadSPDXYamlYn"	: $("#allowDownloadSPDXYamlYn").val()
						};
						
						fn.exeProjectStatus(data, "PROG");
					}
				});

				var _editor = CKEDITOR.instances.editor3;
				
				if(_editor) {
					_editor.destroy();
				}
				
				CKEDITOR.replace('editor3', {});
			})
			
			// verRequest 버튼 
			$('#verRequest').click(function(e){
				
				if(fn.checkVerify()){
					alertify.error('<spring:message code="msg.project.required.verify" />');

					return false;
				}
				
				gStatus = "REQ";
				tempHandler = e;
				
				$("#save").trigger("click");
			});
			
			// verReviewStart 버튼 
			$('#verReviewStart').click(function(e){
				e.preventDefault();

				var data = {"prjId" : '${project.prjId}'
						  , "verificationStatus" : "REV"
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
						  , "allowDownloadSPDXJsonYn" 	: $("#allowDownloadSPDXJsonYn").val()
						  , "allowDownloadSPDXYamlYn" 	: $("#allowDownloadSPDXYamlYn").val()
				};
				
				fn.exeProjectStatus(data, "REV");
			});
			
			$('#btnReadme').click(function(e){
				e.preventDefault();
				
				var fileNm = $("#readmeFile").val();
				var downloadFlag = $(this).hasClass("green");

				if(downloadFlag){
					location.href = '<c:url value="/project/verification/downloadFile?prjId='+${project.prjId}+'&fileNm='+fileNm+'"/>';
				}
			});

			$('#btnProprietary').click(function(e){
				e.preventDefault();
				
				var fileNm = $("#exceptFile").val();
				var downloadFlag = $(this).hasClass("green");
				
				if(downloadFlag){
					location.href = '<c:url value="/project/verification/downloadFile?prjId='+${project.prjId}+'&fileNm='+fileNm+'"/>';
				}
			});
			
			$('#btnVerifyFileContent').click(function(e){
				e.preventDefault();
				
				var fileNm = $("#verifyFile").val();
				var downloadFlag = $(this).hasClass("green");
				
				if(downloadFlag){
					location.href = '<c:url value="/project/verification/downloadFile?prjId='+${project.prjId}+'&fileNm='+fileNm+'"/>';
				}
			});
			
			//wgetUrl send
			$('#send_1').on('click', function(){
				var wgetUrl = $("#sendWgetUrl_1").val();
				var obj = {
					prjId:datas.verify.data.prjId,
					wgetUrl:wgetUrl
				};
				
				$.ajax({
					url : '<c:url value="/project/verification/wgetUrl"/>',
					type : 'POST',					
					dataType : 'json',
					contentType : 'application/json',
					cache : false,
					data : JSON.stringify(obj) ,
					success : function(json){
						var result = JSON.parse(json);
						var resultCode = result[0][0].wgetResult;

						if(resultCode == 0){
							result.forEach(function(item){
								var _url = '<c:url value="/download/'+item[0].registSeq+'/'+item[0].fileName+'"/>';
								
								$('.uploadList_1 ul').append('<li><span><strong><a href="'+_url+'">'+item[0].originalFilename+'</a></strong><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,\''+item[0].registSeq+'\', \'1\')"></span></li>');
								$('input[name=fileSeq_1]').val(item[0].registSeq);
								$('.ajax-file-upload-statusbar').fadeOut('slow');
								$('.ajax-file-upload-statusbar').remove();
								$("#fileUplWarnMessage_1").hide();
							});
							
							$('.verifyFile_1').hide();
							$("#uploadAdd_1").show();
						} else {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					},
					error : function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			});
			
			$('#send_2').on('click', function(){
				var wgetUrl = $("#sendWgetUrl_2").val();
				var obj ={
					prjId:datas.verify.data.prjId,
					wgetUrl:wgetUrl
				};
				
				$.ajax({
					url : '<c:url value="/project/verification/wgetUrl"/>',
					type : 'POST',					
					dataType : 'json',
					contentType : 'application/json',
					cache : false,
					data : JSON.stringify(obj) ,
					success : function(json){
						var result = JSON.parse(json);
						var resultCode = result[0][0].wgetResult;
						
						if(resultCode == 0) {
							result.forEach(function(item){
								var _url = '<c:url value="/download/'+item[0].registSeq+'/'+item[0].fileName+'"/>';
								
								$('.uploadList_2 ul').append('<li><span><strong><a href="'+_url+'">'+item[0].originalFilename+'</a></strong><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,\''+item[0].registSeq+'\', \'2\')"></span></li>');
								$('input[name=fileSeq_2]').val(item[0].registSeq);
								$('.ajax-file-upload-statusbar').fadeOut('slow');
								$('.ajax-file-upload-statusbar').remove();
								$("#fileUplWarnMessage_2").hide();
							});
							
							$('.verifyFile_2').hide();
							$("#uploadAdd_2").show();
							$("#uploadRemove_2").hide();
						} else {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					},
					error : function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			});
			
			$('#send_3').on('click', function(){
				var wgetUrl = $("#sendWgetUrl_3").val();
				var obj = {
					prjId:datas.verify.data.prjId,
					wgetUrl:wgetUrl
				};
				
				$.ajax({
					url : '<c:url value="/project/verification/wgetUrl"/>',
					type : 'POST',					
					dataType : 'json',
					contentType : 'application/json',
					cache : false,
					data : JSON.stringify(obj) ,
					success : function(json){
						var result = JSON.parse(json);
						var resultCode = result[0][0].wgetResult;
						
						if(resultCode == 0) {
							result.forEach(function(item){
								var _url = '<c:url value="/download/'+item[0].registSeq+'/'+item[0].fileName+'"/>';
								
								$('.uploadList_3 ul').append('<li><span><strong><a href="'+_url+'">'+item[0].originalFilename+'</a></strong><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,\''+item[0].registSeq+'\', \'3\')"></span></li>');
								$('input[name=fileSeq_3]').val(item[0].registSeq);
								$('.ajax-file-upload-statusbar').fadeOut('slow');
								$('.ajax-file-upload-statusbar').remove();
								$("#fileUplWarnMessage_3").hide();
							});
							
							$('.verifyFile_3').hide();
							$("#uploadRemove_3").hide();
						} else {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					},
					error : function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
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
				} else if(type == 'spdxJson') {
					fn.downloadSpdxJson();
				} else if(type == 'spdxYaml') {
					fn.downloadSpdxYaml();
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

			$('#spdxJson').click(function(e){
				fn.downloadSpdxJson();
			});

			$('#spdxYaml').click(function(e){
				fn.downloadSpdxYaml();
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
			$("#packagingTab").click(function(){
				var prjId = '${project.prjId}';
				var idx = getTabIndex(prjId+"_Packaging");
				
				if(idx != "") {
					changeTabInFrame(idx);
				} else {
					createTabInFrame(prjId+'_Packaging', '#<c:url value="/project/verification/'+prjId+'"/>');
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
			
			$("#uploadAdd_1").click(function(e){
				$(".multiSet > div:eq(1)").show();
				$("#uploadAdd_1").hide();
				$("#uploadRemove_2").show();
			});
			
			$("#uploadAdd_2").click(function(e){
				$(".multiSet > div:eq(2)").show();
				$("#uploadAdd_2").hide();
				$("#uploadRemove_3").show();
			});

			$("#uploadRemove_1").click(function(e){
				var fileSeq = $('input[name=fileSeq_2]').val();
				
				if(fileSeq != ""){
					$("#uploadRemove_1").hide();
					$("#uploadAdd_1").show();
					
					var html = $('.uploadList_2 ul li').html();
					$('.uploadList_1 ul').append('<li>'+html.split("'2'").join("'1'")+'</li>');
					$('input[name=fileSeq_1]').val($('input[name=fileSeq_2]').val());
					
					$(".multiSet > div:eq(1)").hide();
					$('.verifyFile_1').hide();
					$("#fileUplWarnMessage_1").hide();
					$(".uploadList_2 > ul > li > span > .smallDelete").trigger("click");
				}
			});
			
			$("#uploadRemove_2").click(function(e){
				var fileSeq = $('input[name=fileSeq_3]').val();
				
				if(fileSeq == ""){
					$("#uploadAdd_1").show();
					$(".multiSet > div:eq(1)").hide();
				} else {
					$("#uploadAdd_2").show();
					
					var html = $('.uploadList_3 ul li').html();
					$('.uploadList_2 ul').append('<li>'+html.split("'3'").join("'2'")+'</li>');
					$('input[name=fileSeq_2]').val($('input[name=fileSeq_3]').val());
					$(".multiSet > div:eq(2)").hide();
					$('.verifyFile_2').hide();
					$("#fileUplWarnMessage_2").hide();
					
					$(".uploadList_3 > ul > li > span > .smallDelete").trigger("click");
				}
				
			});
			
			$("#uploadRemove_3").click(function(e){
				$("#uploadAdd_2").show();
				$(".multiSet > div:eq(2)").hide();
			});
		}
	}
	
	var fn = {
		deleteFile : function(element, fileSeq, i){
			$('input[name=fileSeq_'+i+']').val('');
			
			var fileSeq_1 = $("[name='fileSeq_1']").val();
			var fileSeq_2 = $("[name='fileSeq_2']").val();
			var fileSeq_3 = $("[name='fileSeq_3']").val();
			
			$(element).parent().parent().remove();
			$('.verifyFile_'+i).show();
			$("#uploadAdd_"+i).hide();
			
			if(!(fileSeq_1 == "" && fileSeq_2 == "" && fileSeq_3 == "")){
				$("#uploadRemove_"+i).show();
			}
			
			// Packaging에서 OSS Package를 다시 올릴 때 UI 개선
			// file이 지워지고 실제로 save를 하지 않은 시점부터도 readme, proprietary, Path or File을 hide 시킴.
			$("#verifyBtnSet").hide();
			$("#verifyFlag").val("N");
			$("#fileUplWarnMessage_"+i).show();
			
			deleteFileList.push(i);
		},
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
				url : '<c:url value="/project/updateProjectStatus"/>',
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
		// 180105 jy-seo
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
			
			if(flag == 'save') {
				customUrl      = '<c:url value="/project/verification/saveAjax"/>';
				customDataType = "json";
				customSucess   = fn.onRegistSuccess;
				customError	   = function(data){
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
				
				if($("#editCompanyName").val() != ""){
					$("#editCompanyName").attr("disabled", false);
				}
				
				if($("#editOssDistributionSite").val() != ""){
					$("#editOssDistributionSite").attr("disabled", false);
				}
				
				if($("#editEmail").val() != ""){
					$("#editEmail").attr("disabled", false);
				}
			} else if(flag == 'preview') {
				customUrl      = '<c:url value="/project/verification/noticeAjax"/>';
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
				customUrl      = '<c:url value="/project/verification/noticeAjax"/>';
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
				customUrl      = '<c:url value="/project/verification/noticeAjax"/>';
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
		sendEditor : function(type){
			//코멘트 저장
			var editorVal = CKEDITOR.instances.editor.getData();
			
			if(!editorVal || editorVal == "") {
				alertify.alert("<spring:message code="msg.project.enter.comment" />", function(){});
				return false;
			}
			
			var param = {referenceId : '${project.prjId}', referenceDiv :'12', contents : editorVal, mailSendType : type};
			
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
		saveEditor : function(){
			//코멘트 임시저장
			var editorVal = CKEDITOR.instances.editor.getData();
			var register = '${sessUserInfo.userId}';
			var param = {referenceId : '${project.prjId}', referenceDiv :'13', contents : editorVal};
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

			if(userRole == "ROLE_ADMIN") {
				btnHtm += '<input type="button" value="Creator Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'C\')"/>&nbsp;&nbsp;&nbsp;';
			} else {
				btnHtm += '<input type="button" value="Reviewer Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'R\')"/>&nbsp;&nbsp;&nbsp;';
			}

			btnHtm +='<input type="button" value="All" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="fn.sendEditor(\'WR\')"/>&nbsp;&nbsp;&nbsp;';

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
		
		// 업로드 화면 컨트롤
		changeSelectOption : function(target){
			var name = $(target).attr("name");
			var value = $("[name='"+name+"']:checked").val()
			var key = name.split("_")[1];
			
			switch(value){
				case "1":
					$('#registFile_' + key).show();
					$('#wgetUrl_' + key).hide();
					$('#projectSearch_' + key).hide();

					break;
				case "2":
					$('#registFile_' + key).hide();
					$('#wgetUrl_' + key).show();
					$('#projectSearch_' + key).hide();

					break;
				case "3":
					$('#registFile_' + key).hide();
					$('#wgetUrl_' + key).hide();
					$('#projectSearch_' + key).show();
				
					switch(key){
						case "1":
							projectList_1.load();
							projectList_1.packagingLoad();
							
							break;
						case "2":
							projectList_2.load();
							projectList_2.packagingLoad();
							
							break;
						case "3":
							projectList_3.load();
							projectList_3.packagingLoad();
							
							break;
						default:
							break;
					}
					
					break;
				default:
					break;
			}
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
				url :'<c:url value="/project/verification/makeNoticePreview"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/project/verification/downloadNoticePreview?id='+data.validMsg+'"/>';

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
				url :'<c:url value="/project/verification/makeNoticeText"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/project/verification/downloadNoticePreview?id='+data.validMsg+'"/>';

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
				url :'<c:url value="/project/verification/makeNoticeSimple"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/project/verification/downloadNoticePreview?id='+data.validMsg+'"/>';

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
				url :'<c:url value="/project/verification/makeNoticeTextSimple"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
	            success: function (data) {
					   if("false" == data.isValid) {
			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
					   } else {
					       window.location =  '<c:url value="/project/verification/downloadNoticePreview?id='+data.validMsg+'"/>';
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
				url: '<c:url value="/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdx", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/spdxdownload/getFile?id='+data.validMsg+'"/>';
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
				url: '<c:url value="/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdxRdf", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/spdxdownload/getFile?id='+data.validMsg+'"/>';
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
				url: '<c:url value="/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdxTag", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/spdxdownload/getFile?id='+data.validMsg+'"/>';
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
				url: '<c:url value="/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdxJson", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/spdxdownload/getFile?id='+data.validMsg+'"/>';
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
				url: '<c:url value="/spdxdownload/getSPDXPost"/>',
				data: JSON.stringify({"type":"spdxYaml", "prjId":'${project.prjId}', "dataStr":dataStr}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					} else {
						window.location =  '<c:url value="/spdxdownload/getFile?id='+data.validMsg+'"/>';
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
				
				$("#chkAllowDownloadNoticeText").attr("disabled", !checked);
				$("#chkAllowDownloadSimpleHTML").attr("disabled", !checked);
				$("#chkAllowDownloadSimpleText").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXSheet").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXRdf").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXTag").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXJson").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXYaml").attr("disabled", !checked);
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
				$("#chkAllowDownloadSPDXJson").attr("disabled", !checked);
				$("#chkAllowDownloadSPDXYaml").attr("disabled", !checked);
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
					  , "allowDownloadSPDXJsonYn" 	: $("#allowDownloadSPDXJsonYn").val()
					  , "allowDownloadSPDXYamlYn" 	: $("#allowDownloadSPDXYamlYn").val()
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
		reuseSearch : function(target){
			var reuseKeyword = $(target).prev().val();
			var targetGrid = $(target).attr("id").split("_")[1];
			var postData = {reuseKeyword : reuseKeyword};
			
			if($("#projectList_"+targetGrid).length > 0) {
				$("#packaging_"+targetGrid).hide();
				$("#projectList_"+targetGrid).jqGrid('setGridParam', {postData:postData, page : 1, url : '<c:url value="/project/verification/reuseProjectSearch"/>'}).trigger('reloadGrid');
			}
		},
		projectPackagingSearch : function(prjId, seq){
			var postData = {prjId:prjId};
			if($("#projectList_"+seq).length > 0) {
				$("#packagingList_"+seq).jqGrid('setGridParam', {postData:postData, url : '<c:url value="/project/verification/reuseProjectPackagingSearch"/>'}).trigger('reloadGrid');
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
		loadReusePackagingFile : function(refPrjId, refFileSeq, targetSeq){
			var data = {
					prjId : '${project.prjId}',
					refPrjId : refPrjId,
					refFileSeq : refFileSeq
			};
			
			$.ajax({
				type: "POST",
				url : '<c:url value="/project/verification/reusePackagingFile"/>',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					var item = data.file[0];
					
					var appendHtml = '<span style="margin-left:20px;">'+item.createdDate+'</span>';
					var _url = '<c:url value="/download/'+item.registSeq+'/'+item.fileName+'"/>';
					
					$('.uploadList_'+targetSeq+' ul').append('<li><span><strong><a href="'+_url+'">'+item.originalFilename+appendHtml+'</a></strong><input type="button" value="Delete" class="smallDelete" onclick="fn.deleteFile(this,\''+item.registSeq+'\', \''+targetSeq+'\')"></span></li>');
					$('input[name=fileSeq_'+targetSeq+']').val(item.registSeq);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
					$('#fileUplWarnMessage_'+targetSeq).hide();
					
					$('.verifyFile_'+targetSeq).hide();
					$("#uploadAdd_"+targetSeq).show();
					$("#uploadRemove_"+targetSeq).hide();

					evt.uploadStatus = true;
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
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
		},
		checkVerify : function(){
			var allData = $("#list").getRowData();
			var filterData = allData.filter(function(cur){ 
													return cur.verifyFileCount.match(/\d+/g) != null 
												}).length;
			
			return filterData != allData.length && !$("#approve").prop("checked");
		}
		// 20210617_autoVerify FN ADD
		, autoVerify : function(){
			var verifyBoolean = $("input:checkbox[name='autoVerify']").is(":checked");

			if (verifyBoolean == true){
				fn.verify();
			}
		}
		, verify : function(){
			var fileSeqs = [];
			
			if($('input[name=fileSeq_1]').val() != ''){
				fileSeqs.push($('input[name=fileSeq_1]').val());
			}
			
			if($('input[name=fileSeq_2]').val() != ''){
				fileSeqs.push($('input[name=fileSeq_2]').val());
			}
			
			if($('input[name=fileSeq_3]').val() != ''){
				fileSeqs.push($('input[name=fileSeq_3]').val());
			}
			
			gridCleanErrMsg("list"); // 기존 error message 삭제

			if(!evt.uploadStatus && fileSeqs.join("").length <= 0){
				alertify.error('should be upload to file.', 0);
				return false;
			}
			
			var target = $('#list');
			var arr = [];
			arr = target.jqGrid('getDataIDs');
			
			for(var i in arr){
				// remove error message
				target.jqGrid('saveRow',arr[i]);
			}
			
			var filePaths = [];
			var componentIds = [];
			var emptyIds = [];
							
			// empty check
			for(var i in arr){
				var _path = target.jqGrid('getCell',arr[i],'filePath');
				var _componentId = target.jqGrid('getCell',arr[i],'componentId');
				
				if(!$.trim(_path)){
					emptyIds.push(arr[i]);
				}
				
				filePaths.push(_path);
				componentIds.push(_componentId);
			}

			// edit mode 원복
			for(var i in arr){
				target.jqGrid('editRow',arr[i], true);
			}
			
			if(emptyIds.length > 0) {
				// show error message
				for(var idx in emptyIds){
					var errRow = $("#"+emptyIds[idx]+" > td[aria-describedby='list_filePath']");

					if(errRow) {
						errRow.append('<div class=\"list_filePath_'+emptyIds[idx]+' retxt"\">Required</div>');
					}
				}
				
				alertify.error('<spring:message code="msg.common.valid" />', 0);
				
				return false;
			}
			
			var obj = {
				fileSeqs:fileSeqs,		     // packaging file이 1~3건일때 전부 담는 array를 보내고
				prjId:'${project.prjId}',
				gridFilePaths:filePaths,
				gridComponentIds:componentIds,
				statusVerifyYn:'Y'
			};
			
			$.ajax({
				url : '<c:url value="/project/verification/verify"/>',
				type : 'POST',					
				dataType : 'json',
				contentType : 'application/json',
				cache : false,
				data : JSON.stringify(obj) ,
			 	timeout: 2*60*60*1000 , // max
				success : function(json){
					json = JSON.parse(json);
					loading.hide();
					
					if(json.resCd=='10'){
						var isReq = true;
						var validArray = json.verifyValid;
						var validMsg = json.verifyValidMsg;
						var fileCountsMap = json.fileCounts;
						var readmeFileName = json.verifyReadme;
						var proprietary = json.verifyProprietary;
						var verifyCheckList = json.verifyCheckList;
						
						validArray.forEach(function(id){
							var errRow = $("#"+id+" > td[aria-describedby='list_filePath']");

							if(errRow) {
								errRow.append('<div class=\"list_filePath_'+id+' retxt"\">'+validMsg+'</div>');
							}
							
							$("#list").jqGrid("setCell", id, "verifyFileCount", " ");

							isReq = false;
						});
						
						$.each(fileCountsMap, function(key,value){
							$("#list").jqGrid("setCell", key, "verifyFileCount", value);
						});
						
						if(isReq){//verify 가 정상이라면 Request Review 노출
							alertify.alert('<spring:message code="msg.common.success" />', function() {
								if(!curIdenStatus || 'PROG' == curIdenStatus) {
									$(".review").show();
								}
								
								createTabInFrame('${project.prjId}_Packaging', '#<c:url value="/project/verification/${project.prjId}"/>');
							});
							
							verified = true;
						} else {
							alertify.alert(json.resMsg, function(){});
							verified = false;
						}
						
						if(readmeFileName.length > 0) {
							$("#readmeFile").val(readmeFileName);
							$("#btnReadme").addClass("green");
						} else {
							$("#btnReadme").removeClass("green");
						}
						
						if(proprietary.length > 0){
							$("#btnProprietary").addClass("green");
						} else {
							$("#btnProprietary").removeClass("green");
						}
						
						if(verifyCheckList.length > 0){
							$("#btnVerifyFileContent").addClass("green");
						} else {
							$("#btnVerifyFileContent").removeClass("green");
						}
						
						$("#deleteFlag").val("N");
						$("#verifyBtnSet").show();
					} else {
						alertify.alert(json.resMsg, function(){});
					}
					
				},
				error : function(data){
					loading.hide();
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	}
	
	var com_fn = {
		tabInit : function(){
			var initDiv = '${initDiv}';
			var tabContent = $(".tabContent");
			var tabMenuA = $(".tabMenu a");
			
			tabContent.hide();
			tabMenuA.click(function () {
				com_fn.fnTabChange(this);
			});
			
			var tabDefaultIdx = (datas.ossList.length < 1 || initDiv.length > 0) ? "1" : "0";
			
			tabMenuA.eq(tabDefaultIdx).click();
		},
		fnTabChange: function(target){
			var tabContent = $(".tabContent");
			var tabMenuA = $(".tabMenu a");
			
			$(".tabMenu a span").remove();
			
			tabMenuA.eq("0").text("Packaging");
			tabMenuA.eq("1").text("Notice");
			
			var tag = "<span>"+$(target).text()+"</span>";
			
			$(target).html(tag);
			
			tabContent.hide();
			activeTabText = $(target).text();
			activeTab = $(target).attr("rel");
			
			if(activeTab == "packaging" && datas.ossList.length < 1) {
				alertify.alert('<spring:message code="msg.project.verification.confirm.package" />', function(){
					tabMenuA.eq("1").click();
				});
			} else if(activeTab == "notice" && isAndroid == "Y") {
				tabMenuA.eq("0").click();
				fn.saveOrGetNotice('previewOnly');
			} else {
				$("#" + activeTab).show();
			}
		}
	}
	
	var packaging = {
		init : function() {
			$(".multiSet > *").hide();
			
			var fileSeq_1 = $("[name='fileSeq_1']").val();
			var fileSeq_2 = $("[name='fileSeq_2']").val();
			var fileSeq_3 = $("[name='fileSeq_3']").val();
			
			$(".multiSet > div:eq(0)").show();
			
			if(fileSeq_1) {
				$("#uploadAdd_1").show();	
			}
			
			if(fileSeq_2) {
				$(".multiSet > div:eq(1)").show();
				$("#uploadAdd_1").hide();
				$("#uploadAdd_2").show();
			}
			
			if(fileSeq_3) {
				$(".multiSet > div:eq(2)").show();
				$("#uploadAdd_1").hide();
				$("#uploadAdd_2").hide();
				$("#uploadAdd_3").show();
			}
			
			fn.changeSelectOption($("[name=selectOption_1]"));
			fn.changeSelectOption($("[name=selectOption_2]"));
			fn.changeSelectOption($("[name=selectOption_3]"));
		}
	}
	
	var projectList_1 = {
		load : function(data){
			$("#projectList_1").jqGrid({
				datatype: 'json',
				jsonReader: {
					repeatitems: false,
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID','Project Name','Project<br>Version','Distribution<br>Type','Creator','Created<br>Date'],
				colModel: [
					{name: 'prjId', index: 'prjId', width: 80, align: 'center', sorttype: 'int'},
					{name: 'prjName', index: 'prjName', width: 243, align: 'left'},
					{name: 'prjVersion', index: 'prjVersion', width: 80, align: 'left'},
					{name: 'distributionType', index: 'distributionType', width: 130, align: 'left', sortable : true},
					{name: 'creator', index: 'creator', width: 100, align: 'left', sortable : true},
					{name: 'createdDate', index: 'createdDate', width: 100, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true}
				],
				onSelectRow: function(id){
					fn.projectPackagingSearch($(this).getCell(id, "prjId"), 1);

					$("#packaging_1").show();
				},
				rowNum: 10,
				rowList: [10, 20, 30],
			   	editurl:'clientArray',
	 			autowidth: true,
				pager: '#pager_1',
				gridview: true,
				sortable: function (permutation) {},
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto',
				loadComplete: function() {}
			});
			
			$("#projectList_1").jqGrid('navGrid',"#pager_1",{add:false,edit:false,del:false,search:false,refresh:false});
		},
		packagingLoad : function(){
			$("#packagingList_1").jqGrid({ 
				datatype: 'json',
				colNames: ['packaging File Name',''],
				colModel: [
					{name: 'origNm', index: 'origNm', width: 300, align: 'left'},
					{name: 'fileSeq', index: 'fileSeq', width: 80, align: 'center'}
				],
				onCellSelect: function(rowid,iCol,cellcontent,e) {},
			   	editurl:'clientArray',
	 			autowidth: true,
				gridview: true,
				sortable: function (permutation) {},
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto',
				loadComplete: function(data) {
					var row = data.rows;
					
					for(var i in row){
						$("#packagingList_1").jqGrid('setCell', +i+1, 'fileSeq', "<input type=\"button\" value=\"load\" class=\"btnCLight w55\" onclick=\"fn.loadReusePackagingFile('"+data.prjId+"', '"+row[i].fileSeq+"', '1')\">");
					}
				}
			});
		}
	};
	
	var projectList_2 = {
		load : function(data){
			$("#projectList_2").jqGrid({ 
				datatype: 'json' ,
				jsonReader: {
					repeatitems: false,
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID','Project Name','Project<br>Version','Distribution<br>Type','Creator','Created<br>Date'],
				colModel: [
					{name: 'prjId', index: 'prjId', width: 80, align: 'center', sorttype: 'int'},
					{name: 'prjName', index: 'prjName', width: 243, align: 'left'},
					{name: 'prjVersion', index: 'prjVersion', width: 80, align: 'left'},
					{name: 'distributionType', index: 'distributionType', width: 130, align: 'left', sortable : true},
					{name: 'creator', index: 'creator', width: 100, align: 'left', sortable : true},
					{name: 'createdDate', index: 'createdDate', width: 100, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true}
				],
				onSelectRow: function(id){
					fn.projectPackagingSearch($(this).getCell(id, "prjId"), 2);
					$("#packaging_2").show();
				},
				rowNum: 10,
				rowList: [10, 20, 30],
			   	editurl:'clientArray',
	 			autowidth: true,
				pager: '#pager_2',
				gridview: true,
				sortable: function (permutation) {},
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto',
				loadComplete: function() {}
			});
			
			$("#projectList_2").jqGrid('navGrid',"#pager_2",{add:false,edit:false,del:false,search:false,refresh:false});
		},
		packagingLoad : function(){
			$("#packagingList_2").jqGrid({ 
				datatype: 'json',
				colNames: ['packaging File Name',''],
				colModel: [
					{name: 'origNm', index: 'origNm', width: 300, align: 'left'},
					{name: 'fileSeq', index: 'fileSeq', width: 80, align: 'center'}
				],
				onCellSelect: function(rowid,iCol,cellcontent,e) {},
			   	editurl:'clientArray',
	 			autowidth: true,
				gridview: true,
				sortable: function (permutation) {},
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto',
				loadComplete: function(data) {
					var row = data.rows;
					
					for(var i in row){
						$("#packagingList_2").jqGrid('setCell', +i+1, 'fileSeq', "<input type=\"button\" value=\"load\" class=\"btnCLight w55\" onclick=\"fn.loadReusePackagingFile('"+data.prjId+"', '"+row[i].fileSeq+"', '2')\">");
					}
				}
			});
		}
	};
	
	var projectList_3 = {
		load : function(data){
			$("#projectList_3").jqGrid({ 
				datatype: 'json' ,
				jsonReader: {
					repeatitems: false,
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID','Project Name','Project<br>Version','Distribution<br>Type','Creator','Created<br>Date'],
				colModel: [
					{name: 'prjId', index: 'prjId', width: 80, align: 'center', sorttype: 'int'},
					{name: 'prjName', index: 'prjName', width: 243, align: 'left'},
					{name: 'prjVersion', index: 'prjVersion', width: 80, align: 'left'},
					{name: 'distributionType', index: 'distributionType', width: 130, align: 'left', sortable : true},
					{name: 'creator', index: 'creator', width: 100, align: 'left', sortable : true},
					{name: 'createdDate', index: 'createdDate', width: 100, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true}
				],
				onSelectRow: function(id){
					fn.projectPackagingSearch($(this).getCell(id, "prjId"), 3);
					$("#packaging_3").show();
				},
				rowNum: 10,
				rowList: [10, 20, 30],
			   	editurl:'clientArray',
	 			autowidth: true,
				pager: '#pager_3',
				gridview: true,
				sortable: function (permutation) {},
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto',
				loadComplete: function() {}
			});
			
			$("#projectList_3").jqGrid('navGrid',"#pager_3",{add:false,edit:false,del:false,search:false,refresh:false});
		},
		packagingLoad : function(){
			$("#packagingList_3").jqGrid({ 
				datatype: 'json',
				colNames: ['packaging File Name',''],
				colModel: [
					{name: 'origNm', index: 'origNm', width: 300, align: 'left'},
					{name: 'fileSeq', index: 'fileSeq', width: 80, align: 'center'}
				],
				onCellSelect: function(rowid,iCol,cellcontent,e) {},
			   	editurl:'clientArray',
	 			autowidth: true,
				gridview: true,
				sortable: function (permutation) {},
				viewrecords: true,
				sortorder: 'desc',
				height: 'auto',
				loadComplete: function(data) {
					var row = data.rows;
					
					for(var i in row){
						$("#packagingList_3").jqGrid('setCell', +i+1, 'fileSeq', "<input type=\"button\" value=\"load\" class=\"btnCLight w55\" onclick=\"fn.loadReusePackagingFile('"+data.prjId+"', '"+row[i].fileSeq+"', '3')\">");
					}
				}
			});
		}
	};
//]]>
</script>