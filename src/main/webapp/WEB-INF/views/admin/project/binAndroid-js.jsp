<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//==========================================================================================
// PROJECT SRC
// REFERENCE_ID = PROJECT_ID
// REFERENCE_DIV = 11 SRC
//==========================================================================================
var globalBinAndroidNoticeFileId = "";
var globalBinAndroidResultFileId = "";
var ossNames = [];
var objs = [];
var licenseNames = [];

//2018-08-10 choye 추가
var binAndroidData;
//src 그리드 데이터 전역 변수
var binAndroidMainData;
var binAndroidValidMsgData;
var binAndroidDiffMsgData;
var binAndroidInfoMsgData;
var downloadCheckLicenseText;

$(document).ready(function(){
	'use strict';
	
	com_evt.init();
	binAndroid_evt.init();
	//bat_evt.init();
});

// SRC 이벤트
var binAndroid_evt = {
	init: function(){
		$("#binAndroidList").jqGrid('GridUnload');

		doNotUseAutoLoadingFlag = "Y"; // loading is not displayed, so loading operation manually occurs.

		loading.show();
		binAndroid_grid.load();
		
		binAndroid_fn.getBinAndroidGridData();
		
		var binAndroidList = $("#binAndroidList");
		//Not aplicable
		var aplicable = $('#applicableBinAndroid').is(':checked');

		if(aplicable){
			$('.binAndroidBtn').hide();
		}
		
		// Grid reset button
		$("#binAndroidReset, #binAndroidResetUp").click(function(e){
 			e.preventDefault();
 			
			alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
				if (e) {
					$("#binAndroidList").jqGrid('clearGridData');
					globalBinAndroidNoticeFileId = "";
					globalBinAndroidResultFileId = "";
				} else {
					return false;
				}
			});
		});
		// 그리드 저장 버튼
		$("#binAndroidSave, #binAndroidSaveUp").click(function(e){
			if (com_fn.checkStatus()){
				e.preventDefault();

				com_fn.exitCell(_mainLastsel, "binAndroidList");
				
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						// 메인, 서브 그리드 세이브 모드
						fn_grid_com.totalGridSaveMode('binAndroidList');
						// 닉네임 체크
						binAndroid_fn.saveMakeData();
					} else {
						return false;
					}
				});
			}else {
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}
		});
		// 프로젝트 조회 버튼
		$('#binAndroidProjectSearchBtn').click(function(e){
			e.preventDefault();
			
			var postData = $('#binAndroidProjectForm').serializeObject();
			
			$('#_binAndroidProjectList1').jqGrid('setGridParam', {postData:postData}).trigger("reloadGrid");
			$('#binAndroidDiv > div > div.orangeBox > div > div.binAndroidProject1').show();
			$('#binAndroidDiv > div > div.orangeBox > div > div.binAndroidProject2').hide();
		});
		
		// csv파일 업로드
		if($('.androidCsvFileArea').find('li').length == 0){
			$('#androidCsvFile').show();
		}else{
			$('#androidCsvFile').hide();
		}
		
		var accept2 = '';
		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
			<c:if test="${file eq '13'}">
			accept2 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
			</c:if>
		</c:forEach>
		
		$('#androidCsvFile').uploadFile({
			url:'<c:url value="/project/androidFile?fileType=csv"/>',
			multiple:false,
			dragDrop:true,
			fileName:"myfile",
			allowedTypes:accept2,
			sequential:true,
			sequentialCount:1,
			dynamicFormData: function() {
				var data ={ "registFileId" :$('#srcAndroidCsvFileId').val()}

				return data;
			},
			onSuccess:function(files,data,xhr,pd){
				var result = jQuery.parseJSON(data);

				if(result == null) {
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
				} else {
					if(result[0] == "FILE_SIZE_LIMIT_OVER"){
						alertify.alert(result[1], function() {
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();
						});
					} else {
						result = result[0][0];
						$('#srcAndroidCsvFileId').val(result.registFileId);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();

						binAndroid_fn.makeFileTag2(result);
						
						if($('.androidCsvFileArea').find('li').length == 0) {
							$('#androidCsvFile').show();
						} else {
							$('#androidCsvFile').hide();
						}
					}
				}
			},
			onError:function(files,status,errMsg,pd){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
		
		// Notice 파일 업로드
		if($('.androidNoticeFileArea').find('li').length == 0) {
			$('#androidNoticeFile').show();
		} else {
			$('#androidNoticeFile').hide();
		}
		
		var accept3 = '';
		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
			<c:if test="${file eq '14' or file eq '20'}">
			if(accept3 != ""){
				accept3 += ",";
			}
			
			accept3 += '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
			</c:if>
		</c:forEach>
		
		$('#androidNoticeFile').uploadFile({
			url:'<c:url value="/project/androidFile?fileType=notice"/>',
			multiple:false,
			dragDrop:true,
			allowedTypes:accept3,
			fileName:"myfile",
			sequential:true,
			sequentialCount:1,
			dynamicFormData: function(){
				var data ={ "registFileId" : '', "prjId": "${project.prjId}"};
				
				return data;
			},
			onSuccess:function(files,data,xhr,pd){
				var result = jQuery.parseJSON(data);
				var parseNoticeHtml = null;

				if(result == null) {
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
				} else {
					result = result[0]; // notice file object

					if(result.length > 1){
						parseNoticeHtml = result[1]; // parseNotice file
					}
					
					result = result[0]; // notice file
					
					$('#srcAndroidNoticeFileId').val(result.registFileId);
					binAndroid_fn.makeFileTag3(result, true);

					if(parseNoticeHtml != null){
						$('#srcAndroidNoticeXmlId').val(parseNoticeHtml.registFileId);
						binAndroid_fn.makeFileTag3(parseNoticeHtml , false);
					}
					
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
					
					if($('.androidNoticeFileArea').find('li').length == 0) {
						$('#androidNoticeFile').show();
					} else {
						$('#androidNoticeFile').hide();
					}					
				}
			},
			onError:function(files,status,errMsg,pd){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
		
		// Result 파일 업로드
		if($('.androidResultFileArea').find('li').length == 0) {
			$('#androidResultFile').show();
		} else {
			$('#androidResultFile').hide();
		}
		
		var accept4 = '';
 		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
			<c:if test="${file eq '19'}">
			accept4 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
			</c:if>
		</c:forEach>
		
		$('#androidResultFile').uploadFile({
			url:'<c:url value="/project/androidFile?fileType=result"/>',
			multiple:false,
			dragDrop:true,
			allowedTypes:accept4,
			fileName:"myfile",
			sequential:true,
			sequentialCount:1,
			dynamicFormData: function(){
				var data ={ "registFileId" : ''};
				
				return data;
			},
			onSuccess:function(files,data,xhr,pd){
				var result = jQuery.parseJSON(data);
				
				if(result == null) {
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
				} else {
					result = result[0][0];
					$('#srcAndroidResultFileId').val(result.registFileId);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
					binAndroid_fn.makeFileTag4(result);
					
					if($('.androidResultFileArea').find('li').length == 0){
						$('#androidResultFile').show();
					}else{
						$('#androidResultFile').hide();
					}					
				}
			},
			onError:function(files,status,errMsg,pd){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
		
		initAndroidDummyFileUpload();
		
		// ossNames auto complete
		fn_grid_com.griOssNames().success(function(data, status, headers, config){
			if(data != null && ossNames == ""){
				data.forEach(function(obj){
					ossNames.push(obj.ossName);
				})
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

		fn_grid_com.checkLicenseTextValidation("${project.prjId}", "load");
	}
}	
// SRC 함수
var binAndroid_fn = {
	// src 그리드 데이터
	getBinAndroidGridData : function(param){
		$.ajax({
			url : '<c:url value="/project/identificationGrid/${project.prjId}/14"/>',
			dataType : 'json',
			cache : false,
			data : (param) ? param : {referenceId : '${project.prjId}'},
			contentType : 'application/json',
			success : function(data){
				binAndroidMainData = data.mainData;
				
				binAndroidValidMsgData = []; //초기화
				binAndroidDiffMsgData = []; //초기화
				binAndroidInfoMsgData = []; //초기화
				
				if(data.validData) {
					binAndroidValidMsgData = data.validData;
				}
				
				if(data.diffData) {
					binAndroidDiffMsgData = data.diffData;
				}
				
				if(data.infoData) {
					binAndroidInfoMsgData = data.infoData;
				}				
				
				// 리로드 대신 그리드 삭제 후 다시 그리기
				$("#binAndroidList").jqGrid('GridUnload');
				binAndroid_grid.load();

				// totla record 표시
				$("#binAndroidList_toppager_right, #binAndroidPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binAndroidMainData.length+'</div>')
				
				fn_grid_com.addEtcKeyDownEvent($('#binAndroidList'), binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData, com_fn.getLicenseName);
				
				doNotUseAutoLoadingFlag = "N";
				
				loading.hide();
			},
			error : function(){
				doNotUseAutoLoadingFlag = "N";
				loading.hide();
				
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	// 저장 데이터 만들기
	saveMakeData : function(){
		cleanErrMsg("binAndroidList");
		
		var target = $("#binAndroidList");
		var prjId = '${project.prjId}';
		var androidCsvFileId = $('#srcAndroidCsvFileId').val();
		var androidNoticeFileId = $('#srcAndroidNoticeFileId').val();
		var androidNoticeXmlId = $('#srcAndroidNoticeXmlId').val();
		var androidResultFileId = $('#srcAndroidResultFileId').val();
		var loadFromAndroidProjectFlag = "N";
		
		if(androidNoticeFileId == ""){
			alertify.alert('<spring:message code="msg.project.required.upload" />', function(){});
			
			return;
		}
		
		// When loaded in another project, the notice file and the result file are replaced with the uploaded file when loading the project.
		if(globalBinAndroidNoticeFileId != "") {
			androidNoticeFileId = globalBinAndroidNoticeFileId;
			androidResultFileId = globalBinAndroidResultFileId;
			
			loadFromAndroidProjectFlag = "Y";
		}
		
		var identificationSubStatusBinAndroid = $("#applicableBinAndroid:checked").val();
		// 메인 그리드
		var mainData = target.jqGrid('getGridParam','data');
		// 최종 데이터
		var finalData = {"prjId" : prjId, "androidCsvFileId" : androidCsvFileId, "androidResultFileId" : androidResultFileId, "androidNoticeXmlId" : androidNoticeXmlId, "androidNoticeFileId" : androidNoticeFileId
					   , "loadFromAndroidProjectFlag" : loadFromAndroidProjectFlag
					   , "identificationSubStatusAndroid" : identificationSubStatusBinAndroid, "mainData" : JSON.stringify(mainData)};

		// 닉네임 체크
		binAndroid_fn.nickNameValid(mainData, finalData);
	},
	// 저장
	exeSave : function(finalData){
		$.ajax({
			url : '<c:url value="/project/saveBinAndroid"/>',
			type : 'POST',
			data : JSON.stringify(finalData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				_mainLastsel = -1;
				
				if("false" == data.isValid) {
					binAndroidValidMsgData = data.resultData;
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){});
					}
				} else {
					if("10" == data.resCd){
						binAndroid_fn.getBinAndroidGridData();
						com_fn.saveFlagObject["ANDROID"] = true;
						
						alertify.success('<spring:message code="msg.common.success" />');

						curIdenStatus = data.resultData||"";
						if(curIdenStatus == "PROG"){
							$(".projdecBtn").show();
							com_fn.btnCtl(userRole, curIdenStatus);
						}
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
	// 업로드 화면 컨트롤
	changeSelectOption : function(){
		var value = $('input[name=binAndroidSelectOption]:checked').val();
		
		if(value == 1) {
			$('#binAndroidUploadSearch').show();
			$('#binAndroidProjectSearch').hide();
			$('#binAndroidDiv > div > div.orangeBox > div.binAndroidProjectSearch').hide();
		} else {
			$('#binAndroidUploadSearch').hide();
			$('#binAndroidProjectSearch').show();
			$('#binAndroidDiv > div > div.orangeBox > div.binAndroidProjectSearch').show();
			$('#_binAndroidProjectList1').jqGrid(binAndroid_fn.setParamProject1());
			$('#_binAndroidProjectList2').jqGrid(binAndroid_fn.setParamProject2());
			$('#_binAndroidProjectList2').jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
		}
	},
	// 프로젝트 검색
	setParamProject1 : function(){
		return {
			url: '<c:url value="/project/identificationProject/14"/>',
			datatype: 'json',
			jsonReader:{
				repeatitems: false,
				id: 'prjId',
			},
			colNames: ['ID','Project Name','Project Version','Distribution Type', 'Creator','Created Date', 'Comment', 'SrcAndroidCsvFileId', 'SrcAndroidNoticeFileId', 'SrcAndroidResultFileId'],
			colModel: [
				{name: 'prjId', index: 'prjId', width: 42, align: 'center', key:true},
				{name: 'prjName', index: 'prjName', width: 240, align: 'left'},
				{name: 'prjVersion', index: 'prjVersion', width: 200, align: 'left'},
				{name: 'distributionType', index: 'distributionType', width: 110, align: 'left'},
				{name: 'creator', index: 'creator', width: 110, align: 'center'},
				{name: 'createdDate', index: 'createdDate', width: 110, align: 'center',  formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}},
				{name: 'comment', index: 'comment', width: 139, align: 'left', formatter: fn_grid_com.displayComment, unformat: fn_grid_com.unFormatter},
				{name: 'srcAndroidCsvFileId', index: 'srcAndroidCsvFileId', width: 0, align: 'left', hidden:true},
				{name: 'srcAndroidNoticeFileId', index: 'srcAndroidNoticeFileId', width: 0, align: 'left', hidden:true},
				{name: 'srcAndroidResultFileId', index: 'srcAndroidResultFileId', width: 0, align: 'left', hidden:true}
			],
			onSelectRow: function(id){
				var ret = $('#_binAndroidProjectList1').jqGrid('getRowData',id);
				binAndroid_fn.identificationProjectSearch(id);
				initAndroidDummyFileUpload();
				
				$('#binAndroidDiv > div > div.orangeBox > div > div.binAndroidProject2').show();
			},
			autoencode: true,
			autowidth: true,
			gridview: true,
			sortable: function(permutation){
			},
			rowNum: 20,
			rowList: [20, 40, 60],
			pager: '#binAndroidProjectPager1',
			sortname: 'prjId',
			viewrecords: true,
			sortorder: 'asc',
			loadonce: false,
			height: 'auto',
			mtype: 'GET',
			loadComplete: function(data){
				$('.commentDiv').find('p').css('margin','0px 0px');
			}
		}
	},
	// 프로젝트 디테일 검색
	setParamProject2 : function(){
		return {
			datatype: 'local',
			colNames: ['ID_KEY','ID','ReferenceId','Binary Name','OSS Name','OSS Version','License', 'Vulnerability','Exclude'],
			colModel: [
				{name: 'componentId', index: 'componentId', width: 40, align: 'center', key:true, hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
				{name: 'referenceId', index: 'referenceId', width: 40, align: 'center', hidden:true},
				{name: 'binaryName', index: 'binaryName', width: 150, align: 'left', template: searchStringOptions},
				{name: 'ossName', index: 'ossName', width: 300, align: 'left', template: searchStringOptions},
				{name: 'ossVersion', index: 'ossVersion', width: 150, align: 'center', template: searchStringOptions},
				{name: 'licenseName', index: 'licenseName', width: 218, align: 'left', formatter: fn_grid_com.displayLicense, template: searchStringOptions},
				{name: 'cvssScore', index: 'cvssScore', width: 98, align: 'center', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : true, sorttype:'float', template: searchNumberOptions},
				{name: 'excludeYn', index: 'excludeYn', width: 40, align: 'center', hidden:true}
			],
			onSelectRow: function(id){},
			autoencode: true,
			autowidth: true,
			gridview: true,
			sortable: function(permutation){},
			rowNum: 10000,
			rowTotal: -1,
			scroll: 1,
 			sortname: 'componentIdx',
			viewrecords: true,
			loadonce: false,
			height: '500',
			mtype: 'GET',
			loadComplete: function(data){
				if(data != null){
					var fileData = data.project;
					
					if(fileData != null){
						var noticeFileId = fileData.androidNoticeFile;
						var resultFileId = fileData.androidResultFile;

						if(noticeFileId.length > 0){
							$('.androidNoticeFileAreaDummy').append('<li><span><strong><a href="/download/'+noticeFileId[0].fileSeq+'/'+noticeFileId[0].logiNm+'">'+noticeFileId[0].origNm+'</a></strong><input type="hidden" value="'+noticeFileId[0].fileSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'5\')"/></span></li>');

							if($('.androidNoticeFileAreaDummy').find('li').length == 0) {
								$('#androidNoticeFileDummy').show();
							} else {
								$('#androidNoticeFileDummy').hide();
							}	
						}
						
						if(resultFileId.length > 0){
							$('.androidResultFileAreaDummy').append('<li><span><strong><a href="/download/'+resultFileId[0].fileSeq+'/'+resultFileId[0].logiNm+'">'+resultFileId[0].origNm+'</a></strong><input type="hidden" value="'+resultFileId[0].fileSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'6\')"/></span></li>');

							if($('.androidResultFileAreaDummy').find('li').length == 0) {
								$('#androidResultFileDummy').show();
							} else {
								$('#androidResultFileDummy').hide();
							}
						}
					}
					
				}
			},
			gridComplete:function (){
				//2018-08-10 choye 추가
				var target = $("#_binAndroidProjectList2");
				var data = target.jqGrid("getRowData");
				
				for(var i=0; i<data.length; i++){
					if(data[i].excludeYn=="Y"){
						target.jqGrid('setRowData', data[i].componentId, false, { background:'gray'});
					}
				}
			}
		}
	},			
	// 닉네임 팝업 띄우기
	makeNickNamePopup : function(obj, finalData){
		if(obj.validMsg && obj.validMsg.length != 0) {
			alertify.confirm(obj.validMsg, function() {
				binAndroid_fn.exeSave(finalData);
			});
		} else {
			binAndroid_fn.exeSave(finalData);
		}
	},
	// 닉네임 체크
	nickNameValid : function(mainData, finalData){
		var prjId = '${project.prjId}';
		var postData = {"mainData" : JSON.stringify(mainData), "prjId" : prjId};
		
		$.ajax({
			url : '<c:url value="/project/nickNameValid/14"/>',
			type : 'POST',
			data : JSON.stringify(postData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				binAndroid_fn.makeNickNamePopup(data, finalData);
			},
			error: function(data){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	makeFileTag2 : function(obj){
		var appendHtml = '&nbsp;&nbsp;'+obj.createdDate;
		
		$('.androidCsvFileArea').append('<li><span><strong style="max-width:752px;"><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a>'+appendHtml+'<input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'2\')"/></strong></span></li>');
	},
	makeFileTag3 : function(obj, deleteFlag){
		var createdDate = '&nbsp;&nbsp;'+obj.createdDate;
		// 여기에 xml,tar.gz,zip file의 경우 변환된 html file인지 확인하고 해당 될경우 delete button을 제거함.
		var appendHtml = '<li><span><strong style="max-width:752px;"><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a>'+createdDate+'</strong><input type="hidden" value="'+obj.registSeq+'"/>';

		if(deleteFlag) {
			appendHtml += '<input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'3\')"/>';
		}
		
		appendHtml += '</span></li>';
		
		$('.androidNoticeFileArea').append(appendHtml);
	},
	makeFileTag4 : function(obj){
		var appendHtml = '&nbsp;&nbsp;'+obj.createdDate;
		
		$('.androidResultFileArea').append('<li><span><strong style="max-width:752px;"><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a>'+appendHtml+'</strong><input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'4\')"/></span></li>');
	},
	makeFileTagDummy1 : function(obj){
		$('.androidNoticeFileAreaDummy').append('<li><span><strong><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a></strong><input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'5\')"/></span></li>');
	},
	makeFileTagDummy2 : function(obj){
		$('.androidResultFileAreaDummy').append('<li><span><strong><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a></strong><input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'6\')"/></span></li>');
	},
	deleteCsv : function(obj, type){
		var Seq = $(obj).prev().val();
		var object = {fileSeq : Seq};
		
		if(type != "3"){
			$(obj).closest('li').remove();
		}
		
		if(type == "1") {
		} else if(type == "2") {
			$('#srcAndroidCsvFileId').val("");
		} else if(type == "3") {
			$(".androidNoticeFileArea > li").remove(); // xml 변환한 file 때문에 파일 개수가 2개가 될 수 있음. 해당 file 지우는방법 변경함.
			
			$('#srcAndroidNoticeFileId').val("");
			$('#srcAndroidNoticeXmlId').val("");
		} else if(type == "4") {
			$('#srcAndroidResultFileId').val("");
		} else if(type == "5") {
			globalBinAndroidNoticeFileId = "";
		} else if(type == "6") {
			globalBinAndroidResultFileId = "";
		}
		
		// csv파일 업로드
		if($('.androidCsvFileArea').find('li').length == 0) {
			$('#androidCsvFile').show();
		} else {
			$('#androidCsvFile').hide();
		}
		
		if($('.androidNoticeFileArea').find('li').length == 0) {
			$('#androidNoticeFile').show();
		} else {
			$('#androidNoticeFile').hide();
		}
		
		if($('.androidResultFileArea').find('li').length == 0) {
			$('#androidResultFile').show();
		} else {
			$('#androidResultFile').hide();
		}
		
		if($('.androidNoticeFileAreaDummy').find('li').length == 0) {
			$('#androidNoticeFileDummy').show();
		} else {
			$('#androidNoticeFileDummy').hide();
		}
		
		if($('.androidResultFileAreaDummy').find('li').length == 0) {
			$('#androidResultFileDummy').show();
		} else {
			$('#androidResultFileDummy').hide();
		}
	},
	loadToList : function(){
		$("#androidNoticeFileDummy > div.retxt").remove();
		var _noticeDummy = globalBinAndroidNoticeFileId;
		var _resultDummy = globalBinAndroidResultFileId;
		// 상단 프로젝트 서치 그리드 선택 로우
		var selrow = $('#_binAndroidProjectList1').jqGrid('getGridParam', "selrow" );
		var ret = $('#_binAndroidProjectList1').jqGrid('getRowData',selrow);
		
		if(_noticeDummy == "") {
			if($('.androidNoticeFileAreaDummy').find('li input').val() == ""){
				$("#androidNoticeFileDummy").append(makeErrMsg('Required'));
				return false;
			} else {
				_noticeDummy = ret.srcAndroidNoticeFileId;
			}
		}
		
		if(_resultDummy == "") {
			_resultDummy = ret.srcAndroidResultFileId;
		}

		// 로드 플래그
		loadBln = true;

		var param = {refPrjId : selrow, loadFromAndroidProjectFlag : 'Y', androidNoticeFileId : _noticeDummy, androidResultFileId : _resultDummy, androidCsvFileId : ret.srcAndroidCsvFileId}

		binAndroid_fn.getBinAndroidGridData(param);
		binAndroid_fn.getFileInfo(param);

		$('#binAndroidDiv > div > div.orangeBox > div > div.binAndroidProject2').hide();
		$("#binAndroidR1").click();
	},
	// 다운로드 엑셀
	downloadExcel : function(){
		$.ajax({
			type: "POST",
			url: '<c:url value="/exceldownload/getExcelPost"/>',
			data: JSON.stringify({"type":"binAndroid", "parameter":'${project.prjId}'}),
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
			
			var target = $("#binAndroidList");
			
			fn_grid_com.totalGridSaveMode('binAndroidList');
			cleanErrMsg("binAndroidList");
			
			// 메인 그리드
			var mainData = target.jqGrid('getRowData');
			var finalData = {"readType":"android","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};

			binAndroid_fn.exeLoadReportData(finalData);
		}
	},
	getAndroidData : function(){
		var sheetNum = [];
		
		$('input:checkbox[name="sheetNameSelect"]').each(function(){
			if($(this).is(':checked')){
				sheetNum.push($(this).val());
			}
		});
		
		var srcAndroidCsvFileId 	= $('#srcAndroidCsvFileId').val();
		var srcAndroidNoticeFileId 	= $('#srcAndroidNoticeFileId').val();
		var srcAndroidResultFileId 	= $('#srcAndroidResultFileId').val();
		
		loading.show();
		
		var target = $("#binAndroidList");
		fn_grid_com.totalGridSaveMode('binAndroidList');
		cleanErrMsg("binAndroidList");
		
		var mainData = target.jqGrid('getGridParam','data');
		var finalData = {"prjId" : '${project.prjId}', "sheetNums" : sheetNum , "mainData" : JSON.stringify(mainData)
				, "androidFileSeq" : ""+srcAndroidCsvFileId, "androidNoticeFileSeq" : ""+srcAndroidNoticeFileId, "androidResultFileSeq" : ""+srcAndroidResultFileId};
		
		binAndroid_fn.exeLoadBuildImageReportData(finalData);
	},
	// load report data
	exeLoadReportData : function(finalData) {
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
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){});
					} else {
						alertify.error('<spring:message code="msg.common.valid" />', 0);
					}
				} else {
					$('.sheetSelectPop').hide();
					binAndroid_fn.makeOssList(data.resultData);
					
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){});
					}
				}
			},
			error: function(data){
				loading.hide();
				$('.ossUpload').children().remove();
				alertify.error('<spring:message code="msg.common.valid" />', 0);
			}
		});
	},
	// load report data
	exeLoadBuildImageReportData : function(finalData){
		$.ajax({
			url : '<c:url value="${suffixUrl}/project/androidApply"/>',
			type : 'POST',
			data : JSON.stringify(finalData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				loading.hide();
				
				if("false" == data.isValid) {
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){});
					} else {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				} else {
					gridCleanErrMsg("binAndroidList");
					$('.sheetSelectPop').hide();
					
					if(data.externalData) { // validData 표시
						binAndroidValidMsgData = data.externalData;
					}
					
					if(data.externalData2) { // validData 표시
						binAndroidDiffMsgData = data.externalData2;
					}
					
					if(data.externalData3) { // validData 표시
						binAndroidInfoMsgData = data.externalData3;
					}			
							
					binAndroid_fn.makeOssList(data.resultData);
					
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){
							if(data.resultData.changehisLicenseName && data.resultData.changehisLicenseName != "") {
								alertify.alert(data.resultData.changehisLicenseName, function(){});
							}
						});
					} else {
						if(data.resultData.changehisLicenseName && data.resultData.changehisLicenseName != "") {
							alertify.alert(data.resultData.changehisLicenseName, function(){});
						}
					}
				}
			},
			error: function(data){
				loading.hide();
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},

	makeOssList : function(data){
		binAndroidMainData = data.mainData;	
		
		// 리로드 대신 그리드 삭제 후 다시 그리기
		$("#binAndroidList").jqGrid('GridUnload');
		
		binAndroid_grid.load();

		// totla record 표시
		$("#binAndroidList_toppager_right, #binAndroidPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binAndroidMainData.length+'</div>');
	},
	//@@Apply
	androidApply : function(type){
		$('.ajs-close').trigger("click");
				
		var srcAndroidCsvFileId 	= $('#srcAndroidCsvFileId').val();
		var srcAndroidNoticeFileId 	= $('#srcAndroidNoticeFileId').val();
		var srcAndroidResultFileId 	= $('#srcAndroidResultFileId').val();

		if(srcAndroidCsvFileId == "") {
			alertify.alert('<spring:message code="msg.project.android.bulidimagefile.required" />', function(){});

			return false;
		}
		
		if(srcAndroidNoticeFileId == "") {
			alertify.alert('<spring:message code="msg.project.android.noticefile.required" />', function(){});

			return false;
		}
		
		var applyData = {
			prjId : '${project.prjId}',
			srcAndroidCsvFileId : srcAndroidCsvFileId,
			srcAndroidNoticeFileId : srcAndroidNoticeFileId,
			srcAndroidResultFileId : srcAndroidResultFileId
		}
		
		$.ajax({
			url : '<c:url value="/project/androidSheetName"/>',
			type : 'POST',
			data : JSON.stringify(applyData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				if(type.indexOf("resetLoad") > -1){
					$("#binAndroidList").jqGrid('clearGridData');
				}
				
				if(data && data.length != 0){
					$('.sheetSelectPop').show();
					$('.sheetSelectPop .sheetNameArea').children().remove();
					$('.sheetSelectPop .sheetNameArea').text('');
					
					for(var i = 0; i < data.length; i++){
						var num = i+1;
						var checkedTxt = "";
						
						if(data[i].name.toUpperCase().trim() == "BIN (ANDROID)"){
							checkedTxt = "checked";
						}
						
						$('.sheetSelectPop .sheetNameArea').append('<li><input type="checkbox" name="sheetNameSelect" value="'+data[i].no+'" id="sheet'+data[i].no+'" class="sheetNum"'+checkedTxt+' >'
								+'<label for="sheet'+data[i].no+'">'+data[i].name+'</label></li>');
						$(".sheetSelectPop .btnCancel").attr('onclick', 'binAndroid_fn.closePop()');
						$('.sheetSelectPop .sheetApply').attr('onclick', 'binAndroid_fn.getAndroidData()');
					}
				} else {
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			},
			error: function(data){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	//file Info 가져오기
	getFileInfo : function(param){
		$.ajax({
			url : '<c:url value="/project/getFileInfo"/>',
			dataType : 'json',
			cache : false,
			data : param,
			contentType : 'application/json',
			success : function(data){
				if(data != null){
					var fileData = data.project;
					
					if(fileData != null){
						var csvFileId = fileData.androidCsvFile;
						var noticeFileId = fileData.androidNoticeFile;
						var resultFileId = fileData.androidResultFile;
						var _url ="";
						
						if(csvFileId.length > 0){
							_url = '<c:url value="/download/'+csvFileId[0].fileSeq+'/'+csvFileId[0].logiNm+'"/>';
							$('.androidCsvFileArea').html('<li><span><strong><a href="'+_url+'">'+csvFileId[0].origNm+'</a></strong><input type="hidden" value="'+csvFileId[0].fileSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'2\')"/></span></li>');

							if($('.androidCsvFileArea').find('li').length == 0){
								$('#androidCsvFile').show();
							}else{
								$('#androidCsvFile').hide();
							}
							
							$('#srcAndroidCsvFileId').val(csvFileId[0].fileId);
						}
						
						if(noticeFileId.length > 0){
							_url = '<c:url value="/download/'+noticeFileId[0].fileSeq+'/'+noticeFileId[0].logiNm+'"/>';
							$('.androidNoticeFileArea').html('<li><span><strong><a href="'+_url+'">'+noticeFileId[0].origNm+'</a></strong><input type="hidden" value="'+noticeFileId[0].fileSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'5\')"/></span></li>');

							if($('.androidNoticeFileArea').find('li').length == 0){
								$('#androidNoticeFile').show();
							}else{
								$('#androidNoticeFile').hide();
							}
								
							$('#srcAndroidNoticeFileId').val(noticeFileId[0].fileId);
						}
						
						if(resultFileId.length > 0){
							_url = '<c:url value="/download/'+resultFileId[0].fileSeq+'/'+resultFileId[0].logiNm+'"/>';
							$('.androidResultFileArea').html('<li><span><strong><a href="'+_url+'">'+resultFileId[0].origNm+'</a></strong><input type="hidden" value="'+resultFileId[0].fileSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="binAndroid_fn.deleteCsv(this, \'6\')"/></span></li>');

							if($('.androidResultFileArea').find('li').length == 0){
								$('#androidResultFile').show();
							}else{
								$('#androidResultFile').hide();
							}
							
							$('#srcAndroidResultFileId').val(resultFileId[0].fileId);
						}
					}
				}
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	identificationProjectSearch : function(id){
		//2018-07-25 choye 추가
		$("#_binAndroidProjectList2").jqGrid('clearGridData');
		var postData = $("#_binAndroidProjectList2").jqGrid('getGridParam', 'postData');
		postData.referenceId = id;
		
        $.ajax({
        	url : '<c:url value="/project/identificationProjectSearch/14"/>',
            type : 'GET',
            dataType : 'json',
            data : postData,
            cache : false,
            success : function(data){
				binAndroidData = data.rows;
				
				var target = $("#_binAndroidProjectList2");
				
				target.jqGrid('setGridParam', {data:binAndroidData}).trigger('reloadGrid');
            },
            error : function(xhr, ajaxOptions, thrownError){
                alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
        });
    },
    showDialog : function(type){
        type = type.toUpperCase();
        
		if(type == ""){
			type = "APPLY";
		}
		
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

		var btnHtm = "";
        switch(type){
	        case "APPLY":
				//launch it.
				btnHtm = '<br/><b></b><br/>';
				btnHtm += '<input type="button" value="Reset & Load" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="binAndroid_fn.androidApply(\'resetLoad\')"/>&nbsp;&nbsp;&nbsp;';
				btnHtm += '<input type="button" value="Load & Append" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="binAndroid_fn.androidApply(\'append\')"/>&nbsp;&nbsp;&nbsp;';
				btnHtm +='<input type="button" value="Cancel" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="$(\'.ajs-close\').trigger(\'click\')"/>&nbsp;&nbsp;&nbsp;';
				
				alertify.commentDialog(btnHtm);
				$(".ajs-dialog").css("height", "");

				break;
	        case "NOTICE":
		        if(binAndroid_fn.ValidNoticeData()){
		        	btnHtm  = '<br><b>Download NOTICE of binaries for adding to attached NOTICE.html&nbsp;<span class="iconSet help" onclick="binAndroid_fn.loadCollab();"></span></b><br>';
			        btnHtm += '첨부된 notice.html에 추가 할 바이너리의 NOTICE를 다운로드 합니다.<br><br>';
		        	btnHtm += '<input type="radio" name="selectionRadio" id="selectionRadio1" value="N" checked>&nbsp;';
			        btnHtm += '<label for="selectionRadio1">All notices in one file<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(.html)</label><br><br>';
		        	btnHtm += '<input type="radio" name="selectionRadio" id="selectionRadio2" value="Y">&nbsp;';
		        	btnHtm += '<label for="selectionRadio2">Each NOTICE of binaries<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(LICENSES/*, BinaryName_Files)</label><br>';
		        	btnHtm += '<input type="button" value="OK" class="btnColor blue" style="height:30px;width:120px;float:right;" onclick="binAndroid_fn.checkNoticeList();"/>';

					alertify.commentDialog(btnHtm);
					$(".ajs-dialog").css("height", "300px");
				}
				
	            break;
	        default:
		        break;
        }
    },
    downloadFile : function(){
        location.href = downloadCheckLicenseText;
    },
    ValidNoticeData : function(){
    	var ValidCnt = Object.keys(binAndroidValidMsgData).reduce(binAndroid_fn.checkErrorData, []).length;

		if(ValidCnt > 0){
			alertify.alert("<spring:message code='msg.project.download.notice'>", function(){});

			return false;	
		}
		
		var diffMsgDataList = JSON.parse(JSON.stringify(binAndroidDiffMsgData), function(key, value){
			if(key.toUpperCase().indexOf("LICENSENAME") > -1){
				if(value.indexOf("Declared") == -1){
					return value;
				}
			}else{
				return value;
			}
		});
		
		var DiffCnt = Object.keys(diffMsgDataList).reduce(binAndroid_fn.checkErrorData, []).length;
		
        if(DiffCnt > 0){
        	alertify.alert("<spring:message code='msg.project.download.notice' />", function(){});

			return false;
        }

        var notExcludeRow = $("#binAndroidList").getRowData().filter(function(a){ return a.excludeYn == "N"; }).length;

        if(notExcludeRow == 0){ // excludeYn이 N인 대상이 0 건일 경우(전체 Data가 excludeYn:Y 이거나 binAndroid의 Data가 0건 인 경우)
        	alertify.alert("<spring:message code='msg.project.no.binary' />", function(){});

			return false;
        }

        var noticeCheckRow = Object.keys(binAndroidDiffMsgData).reduce(function(arr, cur){
			if(cur.toUpperCase().indexOf("BINARYNOTICE") > -1){ // binaryNotice
				arr.push(cur);
			}

			return arr;
        }, []).length;

        if(noticeCheckRow == 0){ //  "NOTICE Should be "ok" in case OSS is used" 인 대상이 0건 일 경우
			alertify.alert("<spring:message code='msg.project.no.binary' />", function(){});

			return false;
        }
        
        return true;
    },
   	checkErrorData : function(arr, cur){
		if(cur.toUpperCase().indexOf("OSSNAME") > -1 
				|| cur.toUpperCase().indexOf("OSSVERSION") > -1 
				|| cur.toUpperCase().indexOf("LICENSENAME") > -1){
			arr.push(cur);
		}

		return arr;
    },
    checkNoticeList : function(){
        var type = $("[name='selectionRadio']:checked").val();
        var param = {referenceId : '${project.prjId}', zipFlag : type.toUpperCase()};
        
		$.ajax({
			url : '<c:url value="/project/getSupplementNoticeFile"/>',
			type : 'POST',
			dataType : 'json',
			cache : false,
			data : JSON.stringify(param),
			contentType : 'application/json',
			success : function(data){
				if(data.isValid == "false") {
					$(".ajs-close").trigger("click"); // dialog popup close
					$(".ajs-dialog").css("height", ""); // height rollback

					alertify.alert(data.validMsg, function(){});
				} else {
					$(".ajs-close").trigger("click"); // dialog popup close

					window.location =  '<c:url value="/project/verification/downloadNoticePreview?id='+data.validMsg+'"/>';
				}
			},
			error : function() {
				// TODO - 미정
			}
		});
    },
    loadCollab : function() {
    	collabUrl = "${ct:getCodeExpString(ct:getConstDef('CD_COLLAB_INFO'), ct:getConstDef('CD_SUPPLEMENT_NOTICE_HELP_URL'))}";

    	window.open(collabUrl);
    }
}

//SRC 그리드
var binAndroid_grid = {
	load: function(){
		var currentOssName = "";
		var ondblClickRowBln = false;
		var binAndroidList = $("#binAndroidList");
		
		binAndroidList.jqGrid({
			datatype: 'local',
			data : binAndroidMainData,
			colNames: ['gridId', 'ID_KEY', 'ID','Binary Name', 'Source Path','Notice', 'ReferenceId', 'ReferenceDiv', 'OssId', 'OSS Name','OSS Version','License','Download Location'
			           ,'Homepage','LicenseId','Copyright Text'
			           //,'Size'
			           ,'CVE ID','Vulnera<br/>bility','<input type="checkbox" onclick="fn_grid_com.onCboxClickAll(this,\'binAndroidList\');">Exclude','LicenseDiv', 'customBinaryYn', 'Restriction', 'Comment'],
			colModel: [
				{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
				{name: 'componentId', index: 'componentId', width: 40, align: 'center', hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
				{name: 'binaryName', index: 'binaryName', width: 100, align: 'left', editable:false, edittype:'text', template: searchStringOptions,
					editoptions: {
						dataInit:
							function (e) { 
								$(e).on("blur", function() {
									var rowid = (e.id).split('_')[0];

									fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value.trim(),binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
								});
							}
					}},
				{name: 'filePath', index: 'filePath', width: 170, align: 'left', editable:true, template: searchStringOptions,
					editoptions: {
						dataInit:
							function (e) { 
								$(e).on("change", function() {
									var rowid = (e.id).split('_')[0];

									fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
								});
							}
					}
				},
				{name: 'binaryNotice', index: 'binaryNotice', width: 70, align: 'center', editable:false , formatter: 'select', edittype:"select", editoptions:{value : ":;ok:ok;ok(NA):ok(NA);nok:nok;nok(NA):nok(NA)"}, searchoptions:{sopt:['eq','ne']} },
				{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
				{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
				{name: 'ossId', index: 'ossId', width: 29, align: 'center', editable:true, hidden:true},
				{name: 'ossName', index: 'ossName', width: 150, align: 'left', editable:true, edittype:'text', template: searchStringOptions, 
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
											
											fn_grid_com.griOssVersions($('#'+rowid+'_ossVersion')[0], e.value, 'binAndroidList');
											fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
										}
									}).dblclick(function(){
										var rowid = (e.id).split('_')[0];
										var licenseName = com_fn.getLicenseName(binAndroidList.getRowData(rowid));
										
										binAndroidList.jqGrid("setCell", rowid, "licenseName", licenseName);
										fn_grid_com.saveCellData(binAndroidList.attr("id"), rowid, "licenseName", licenseName, null, null);
										binAndroidList.jqGrid('saveRow',rowid);
										
										fn_grid_com.mvOssPage(binAndroidList, rowid);
									});
									
									currentOssName = e.value;
								}
						}
				},
				{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left', editable: true, edittype: 'text', template: searchStringOptions,
					editoptions: {
						dataInit:
							function (e) { 
								fn_grid_com.griOssVersions(e, currentOssName, 'binAndroidList');
								$(e).on( "autocompletechange", function() {
									var rowid = (e.id).split('_')[0];

									fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
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
									
									fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
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
									
									fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
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
										$("#"+rowid+"_downloadLocation").val(value);
									}
									
									fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
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
				{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', editable:true, edittype:'text', hidden:true},
				{name: 'copyrightText', index: 'copyrightText', width: 120, align: 'left', editable:false, edittype:"textarea", editoptions:{rows:"5",cols:"24", template: searchStringOptions, 
					dataInit:
						function (e) { 
							$(e).on("change", function() {
								var rowid = (e.id).split('_')[0];
								
								fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
							});
						}
					}
				},
				{name: 'cveId', index: 'cveId', hidden:true},
				{name: 'cvssScore', index: 'cvssScore', width: 80, align: 'center', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : true, sorttype:'float', template: searchNumberOptions},
				{name: 'excludeYn', index: 'excludeYn', width: 60, align: 'center', formatter: fn_grid_com.cboxFormatter, unformat: fn_grid_com.cboxUnFormatter, search: false},
				{name: 'licenseDiv', index: 'licenseDiv', width: 100, align: 'left', editable:false, hidden:true},
				{name: 'customBinaryYn', index: 'customBinaryYn', editable:false, hidden:true},
				{name: 'restriction', index: 'restriction', width: 60, align: 'center', formatter: fn_grid_com.displayLicenseRestriction, unformat: fn_grid_com.unformatter, sortable : true, search : false},
				{name: 'comments', index: 'comments', width: 150, align: 'left', editable:true, edittype:"textarea", editoptions:{rows:"5",cols:"24", template: searchStringOptions, 
					dataInit:
						function (e) { 
							$(e).on("change", function() {
								var rowid = (e.id).split('_')[0];

								fn_grid_com.saveCellData("binAndroidList",rowid,e.name,e.value,binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
							});
						}
					}}
			],
			autoencode: true,
			editurl:'clientArray',
 			autowidth: false,
 			shrinkToFit : false,
			maxHeight: 400,
			height: 'auto',
			//scroll: 1,
			gridview: true,
		   	pager: '#binAndroidPager',
			rowNum: 200,
			rowList: [200, 500, 1000, 5000],
			recordpos:'right',
			loadonce:true,
			ignoreCase: true,
			multiselect: true,
			hoverrows:false,
			toppager:true,
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
					$("#binAndroidList tr.singleLicenseClass").find("td:first").removeClass("sgexpanded sgcollapsed").find("a").hide();
					
					tableRefresh();
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
			    fn_grid_com.setWarningClass(binAndroidList,rowid,["ossName","licenseName"]);
//			    return true;
			},
			onCellSelect: function(rowid,iCol,cellcontent,e) {
				if(iCol=="3") {
					com_fn.exitCell(_mainLastsel, "binAndroidList");
					
					fn_grid_com.showOssViewPage(binAndroidList, rowid, true, binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData, com_fn.getLicenseName);
				}
			},
			ondblClickRow: function(rowid,iRow,iCol,e) {
				if(iCol=="4"){
					com_fn.exitCell(_mainLastsel, "binAndroidList");
					
					fn_grid_com.showBinaryViewPage(binAndroidList, rowid, true, binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData);
				}
				
				cleanErrMsg("binAndroidList", rowid);
				fn_grid_com.setCellEdit(binAndroidList, rowid, binAndroidValidMsgData, binAndroidDiffMsgData, binAndroidInfoMsgData, com_fn.getLicenseName);

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
				
				$('#'+rowid+'_licenseName').val("");
                                var nextCol = binAndroidList.jqGrid('getGridParam', 'colModel')[iCol].name
                                var nextRow = rowid
                                $('#'+nextRow+"_"+nextCol).focus();
			},
			onPaging: function(action) {
				cleanErrMsg("binAndroidList");
				fn_grid_com.totalGridSaveMode('binAndroidList');
			},
			gridComplete : function() {
				cleanErrMsg("binAndroidList");
				
				if(binAndroidValidMsgData) {
					gridValidMsgNew(binAndroidValidMsgData, "binAndroidList");
				}
				
				if(binAndroidDiffMsgData) {
					gridDiffMsg(binAndroidDiffMsgData, "binAndroidList");
				}
				
				if(binAndroidInfoMsgData) {
					gridInfoMsg(binAndroidInfoMsgData, "binAndroidList");
				}
			},
			removeHighLight : true
		});
		
		binAndroidList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
		binAndroidList.jqGrid('navGrid',"#binAndroidPager",{add:true,edit:false,del:true,search:false,refresh:false
												  , addfunc: function () {
													  com_fn.saveFlagObject["ANDROID"] = false; 
													  fn_grid_com.rowAddNew('binAndroidList',binAndroidList,"main", null, com_fn.getLicenseName);
												  }, delfunc: function () { 
													  fn_grid_com.rowDelNew(binAndroidList,"main");
												  }, cloneToTop:true
		});
		
		
		$('#binAndroidList').closest(".ui-jqgrid-bdiv").css({"height":"500px", "overflow-y" : "scroll"});
	}
}

function initAndroidDummyFileUpload() {

	$('.androidNoticeFileAreaDummy').find('li').remove();
	$('#androidNoticeFileDummy').show();
	$('.androidResultFileAreaDummy').find('li').remove();
	$('#androidResultFileDummy').show();
	
	var accept3 = '';
	<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
		<c:if test="${file eq '14'}">
		accept3 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
		</c:if>
	</c:forEach>
	
	var accept4 = '';
		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
		<c:if test="${file eq '19'}">
		accept4 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
		</c:if>
	</c:forEach>
	
	$('#androidNoticeFileDummy').uploadFile({
		url:'<c:url value="/project/androidFile?fileType=notice"/>',
		multiple:false,
		dragDrop:true,
		allowedTypes:accept3,
		fileName:"myfile",
		maxFileCount:1,
		onSuccess:function(files,data,xhr,pd){
			var result = jQuery.parseJSON(data);
			
			if(result == null) {
				alertify.error('<spring:message code="msg.common.valid" />', 0);
				$('.ajax-file-upload-statusbar').fadeOut('slow');
				$('.ajax-file-upload-statusbar').remove();
			} else {
				result = result[0][0];
				globalBinAndroidNoticeFileId = result.registFileId;
				
				$('.ajax-file-upload-statusbar').fadeOut('slow');
				$('.ajax-file-upload-statusbar').remove();
				
				binAndroid_fn.makeFileTagDummy1(result);
				
				if($('.androidNoticeFileAreaDummy').find('li').length == 0) {
					$('#androidNoticeFileDummy').show();
				} else {
					$('#androidNoticeFileDummy').hide();
				}					
			}
		},
		onError:function(files,status,errMsg,pd){
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
		}
	});

	$('#androidResultFileDummy').uploadFile({
		url:'<c:url value="/project/androidFile?fileType=result"/>',
		multiple:false,
		dragDrop:true,
		allowedTypes:accept4,
		fileName:"myfile",
		onSuccess:function(files,data,xhr,pd){
			var result = jQuery.parseJSON(data);
			
			if(result == null) {
				alertify.error('<spring:message code="msg.common.valid" />', 0);
				$('.ajax-file-upload-statusbar').fadeOut('slow');
				$('.ajax-file-upload-statusbar').remove();
			} else {
				result = result[0][0];
				globalBinAndroidResultFileId = result.registFileId;
				
				$('.ajax-file-upload-statusbar').fadeOut('slow');
				$('.ajax-file-upload-statusbar').remove();
				
				binAndroid_fn.makeFileTagDummy2(result);
				
				if($('.androidResultFileAreaDummy').find('li').length == 0) {
					$('#androidResultFileDummy').show();
				} else {
					$('#androidResultFileDummy').hide();
				}					
			}
		},
		onError:function(files,status,errMsg,pd){
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
		}
	});
}
</script>
