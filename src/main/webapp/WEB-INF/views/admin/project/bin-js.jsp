<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//==========================================================================================
// PROJECT SRC
// REFERENCE_ID = PROJECT_ID
// REFERENCE_DIV = 11 SRC
//==========================================================================================
var ossNames = [];
var objs = [];
var licenseNames = [];
//2018-08-10 choye 추가
var binData;
// SRC 이벤트
var bin_evt = {
	csvDelFileSeq : [],
	init: function(){
		bin_fn.getBinGridData();
		$('#_binAddList').jqGrid(bin_fn.setParamProject3());
		var binList = $("#binList");
		//Not aplicable
		var aplicable = $('#applicableBin').is(':checked');

		if(aplicable){
			$('.binBtn').hide();
		}
		
		// 그리드 리셋 버튼
		$("#binReset, #binResetUp").click(function(e){
 			e.preventDefault();
 			
			alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
				if (e) {
					$("#binList").jqGrid('clearGridData');
					$("#_binAddList").jqGrid('clearGridData');
				} else {
					return false;
				}
			});
		});
		// 그리드 저장 버튼
		$("#binSave, #binSaveUp").click(function(e){
			if (com_fn.checkStatus()){
				e.preventDefault();
				
				com_fn.exitCell(_mainLastsel, "binList");
				
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						// 메인, 서브 그리드 세이브 모드
						fn_grid_com.totalGridSaveMode('binList');
						// 닉네임 체크
						bin_fn.saveMakeData();
					} else {
						return false;
					}
				});
			}else {
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}
		});
		// 프로젝트 조회 버튼
		$('#binProjectSearchBtn').click(function(e){
			e.preventDefault();
			var postData = $('#binProjectForm').serializeObject();
			
			$('#_binProjectList1').jqGrid('setGridParam', {postData:postData}).trigger("reloadGrid");
			$('.binProject1').show();
			$('.binProject2').hide();
		});
		
		// csv파일 업로드
		var accept1 = '';
		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
			<c:if test="${file eq '12'}">
			accept1 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
			</c:if>
		</c:forEach>
		
		//파일업로드
		$('#binCsvFile').uploadFile({
			url:'<c:url value="/project/binCsvFile"/>',
			multiple:false,
			dragDrop:true,
			fileName:'myfile',
			allowedTypes:accept1,
			sequential:true,
			sequentialCount:1,
			dynamicFormData: function() {
				var data = { "registFileId" :$('#binCsvFileId').val() };
				
				return data;
			},
			onSuccess:function(files,data,xhr,pd){
				var result = jQuery.parseJSON(data);
				if(result[1] == null) {
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
						bin_fn.getCsvData(result[0][0].registSeq);

						$('#binCsvFileId').val(result[0][0].registFileId);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();

						bin_fn.makeFileTag(result[0][0]);
					} else if(result[2] == "EXCEL_FILE") {
						if(result[1].length != 0) {
							$('.sheetSelectPop').show();
							$('.sheetSelectPop .sheetNameArea').children().remove();
							$('.sheetSelectPop .sheetNameArea').text('');
							
							for(var i = 0; i < result[1].length; i++) {
								var num = i+1;
								var checkedTxt = "";
								
								if(result[1][i].name.toUpperCase().trim() == "BIN") {
									checkedTxt = "checked";
								}
								
								$('.sheetSelectPop .sheetNameArea').append('<li><input type="checkbox" name="sheetNameSelect" value="'+result[1][i].no+'" id="sheet'+result[1][i].no+'" class="sheetNum" '+checkedTxt+'>'
										+'<label for="sheet'+result[1][i].no+'">'+result[1][i].name+'</label></li>');
								$('.sheetSelectPop .sheetApply').attr('onclick', 'bin_fn.getSheetData('+result[0][0].registSeq+')')
							}
						}
						
						$('#binCsvFileId').val(result[0][0].registFileId);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						
						bin_fn.makeFileTag(result[0][0]);	
					} else if(result[2] == "SPDX_SPREADSHEET_FILE"){
						bin_fn.getSpdxSpreadsheetData(result[0][0].registSeq);

						$('#binCsvFileId').val(result[0][0].registFileId);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();

						bin_fn.makeFileTag(result[0][0]);
					} else {
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					}
				}
			}
		});
		
		// Result 파일 업로드
		if($('.binBinaryFileArea').find('li').length == 0) {
			$('#binBinaryFile').show();
		} else {
			$('#binBinaryFile').hide();
		}		
		
		var accept4 = '';
 		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
			<c:if test="${file eq '19'}">
			accept4 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
			</c:if>
		</c:forEach>
		
		$('#binBinaryFile').uploadFile({
			url:'<c:url value="/project/binCsvFile?fileType=text"/>',
			multiple:false,
			dragDrop:true,
			allowedTypes:accept4,
			fileName:"myfile",
			sequential:true,
			sequentialCount:1,
			dynamicFormData: function() {
				var data ={ "registFileId" : '' }

				return data;
			},
			onSubmit:function(files){
				// file ext 재확인
				var accept4 = "";
				var ext = files[0].split(".")[1];
				
				<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
					<c:if test="${file eq '19'}">
					accept4 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
					</c:if>
				</c:forEach>
				
				if(accept4 != ext){
					return false;
				}
			},
			onSuccess:function(files,data,xhr,pd){
				var result = jQuery.parseJSON(data);
				
				if(result == null) {
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
				} else {
					result = result[0][0];
					$('#binBinaryFileId').val(result.registFileId);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
					bin_fn.makeFileTag2(result);	

					if($('.binBinaryFileArea').find('li').length == 0) {
						$('#binBinaryFile').show();
					} else {
						$('#binBinaryFile').hide();
					}			
				}
			},
			onError:function(files,status,errMsg,pd){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
		
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
						tag ={
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
}	
// src 그리드 데이터 전역 변수
var binMainData;
var binValidMsgData;
var binDiffMsgData;
var binInfoMsgData;

// SRC 함수
var bin_fn = {
	// src 그리드 데이터
	getBinGridData : function(param, type){
		var url = "append".indexOf(type) > -1  
					? '<c:url value="${suffixUrl}/project/identificationMergedGrid/${project.prjId}/15"/>' 
					: '<c:url value="${suffixUrl}/project/identificationGrid/${project.prjId}/15"/>'
			, type = "append".indexOf(type) > -1  ? 'POST' : 'GET'
			, data = param || { referenceId : '${project.prjId}', typeFlag : 'N' };
		
		$.ajax({
			url : url,
			dataType : 'json',
			type : type,
			cache : false,
			data : data,
			contentType : 'application/json',
			success : function(data){
				$('.ajs-close').trigger("click");
				
				binMainData = data.mainData;
				
				binValidMsgData = []; //초기화
				binDiffMsgData = []; //초기화
				binInfoMsgData = []; //초기화
				
				if(data.validData) {
					binValidMsgData = data.validData;
				}
				
				if(data.diffData) {
					binDiffMsgData = data.diffData;
				}
				
				if(data.infoData) {
					binInfoMsgData = data.infoData;
				}
				
				// 리로드 대신 그리드 삭제 후 다시 그리기
				$("#binList").jqGrid('GridUnload');
				
				bin_grid.load();

				// totla record 표시
				$("#binList_toppager_right, #binPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binMainData.length+'</div>');
				
				fn_grid_com.addEtcKeyDownEvent($('#binList'), binValidMsgData, binDiffMsgData, binInfoMsgData, com_fn.getLicenseName);
				
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	// 저장 데이터 만들기
	saveMakeData : function(){
		cleanErrMsg("binList");
		var target = $("#binList");
		var prjId = '${project.prjId}';
		var binCsvFileId = $('#binCsvFileId').val();
		var binBinaryFileId = $("#binBinaryFileId").val();
		var identificationSubStatusBin = $("#applicableBin:checked").val();
		
		var delFileData = bin_evt.csvDelFileSeq;
		
		// 메인 그리드
		var mainData = target.jqGrid('getGridParam','data');
		// 서브 그리드
		var subData = [];
		var binAddListData = $("#_binAddList").jqGrid("getRowData");
 		
		// 최종 데이터
		var finalData = {"prjId" : prjId, "binCsvFileId" : binCsvFileId , "binBinaryFileId" : binBinaryFileId, "csvDelFileIds" : JSON.stringify(delFileData)
					   , "identificationSubStatusBin" : identificationSubStatusBin, "mainData" : JSON.stringify(mainData), "subData" : JSON.stringify(subData), "binAddListData" : JSON.stringify(binAddListData)};

		// 닉네임 체크
		bin_fn.nickNameValid(mainData, subData, finalData);
	},
	// 저장
	exeSave : function(finalData){
		$.ajax({
			url : '<c:url value="${suffixUrl}/project/saveBin"/>',
			type : 'POST',
			data : JSON.stringify(finalData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				_mainLastsel = -1;
				
				if("false" == data.isValid) {
					binValidMsgData = data.resultData;
					
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){});
					}
				} else {
					if("10" == data.resCd){
						bin_fn.getBinGridData();
						$("#mergeYn").val("N");
						bin_evt.csvDelFileSeq = [];
						com_fn.saveFlagObject["BIN"] = true;
						alertify.success('<spring:message code="msg.common.success" />');
						
						if(data.changeBySystemNotice && data.changeBySystemNotice && data.changeBySystemNotice != "") {
							alertify.alert(data.changeBySystemNotice, function(){});
						}

						curIdenStatus = data.resultData||"";
						if(curIdenStatus == "PROG"){
							com_fn.btnCtl(userRole, curIdenStatus);
						}
					} else {
						alertify.error('<spring:message code="msg.common.valid2" />');
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
		var value = $('input[name=binSelectOption]:checked').val();
		
		if(value == 1) {
			$('#binUploadSearch').show();
			$('#binProjectSearch').hide();
			$('.binProjectSearch').hide();
		} else {
			$('#binUploadSearch').hide();
			$('#binProjectSearch').show();
			$('.binProjectSearch').show();
			$('#_binProjectList1').jqGrid(bin_fn.setParamProject1());
			$('#_binProjectList2').jqGrid(bin_fn.setParamProject2());
			$('#_binAddList').jqGrid(bin_fn.setParamProject3());
			$('#_binProjectList2').jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
		}
	},
	// 프로젝트 검색
	setParamProject1 : function(){
		return {
			url: '<c:url value="/project/identificationProject/15"/>',
			datatype: 'json',
			jsonReader:{
				repeatitems: false,
				id: 'prjId',
			},
			colNames: ['ID','Project Name','Project Version','Distribution Type', 'Creator','Created Date', 'Comment'],
			colModel: [
				{name: 'prjId', index: 'prjId', width: 42, align: 'center', key:true},
				{name: 'prjName', index: 'prjName', width: 240, align: 'left'},
				{name: 'prjVersion', index: 'prjVersion', width: 200, align: 'left'},
				{name: 'distributionType', index: 'distributionType', width: 110, align: 'left'},
				{name: 'creator', index: 'creator', width: 110, align: 'center'},
				{name: 'createdDate', index: 'createdDate', width: 110, align: 'center',  formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}},
				{name: 'comment', index: 'comment', width: 139, align: 'left', formatter: fn_grid_com.displayComment, unformat: fn_grid_com.unFormatter}
			],
			onSelectRow: function(id){
				bin_fn.identificationProjectSearch(id);
				
				$('.binProject2').show();
			},
			autowidth: true,
			gridview: true,
			sortable: function(permutation){
			},
			rowNum: 20,
			rowList: [20, 40, 60],
			pager: '#binProjectPager1',
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
				{name: 'binaryName', index: 'binaryName', width: 168, align: 'left', template: searchStringOptions},
				{name: 'ossName', index: 'ossName', width: 350, align: 'left', template: searchStringOptions},
				{name: 'ossVersion', index: 'ossVersion', width: 150, align: 'center', template: searchStringOptions},
				{name: 'licenseName', index: 'licenseName', width: 150, align: 'left', formatter: fn_grid_com.displayLicense, template: searchStringOptions},
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
			loadonce: true,
			height: '500',
			mtype: 'GET',
			loadComplete: function(data){},
			gridComplete:function (){
				//2018-08-10 choye 추가
				var target = $("#_binProjectList2");
				var data = target.jqGrid("getRowData");
				
				for(var i=0; i<data.length; i++){
					if(data[i].excludeYn=="Y"){
						target.jqGrid('setRowData', data[i].componentId, false, { background:'gray'});
					}
				}
			}
		}
	},			
	setParamProject3 : function(){
		return {
			url: '<c:url value="/project/getAddList"/>',
			datatype: 'json',
			postData : {prjId : '${project.prjId}', referenceDiv : '15'},
			jsonReader:{
				repeatitems: false,
				id: 'prjId',
				root:function(obj){return obj.rows;},
				page:function(obj){return obj.page;},
				total:function(obj){return obj.total;},
				records:function(obj){return obj.records;}
			},
			colNames: ['prjId', 'ID','project Name', 'project Version', 'Component Count', 'referenceDiv'],
			colModel: [
				{name: 'prjId', index: 'prjId', width: 110, align: 'center', formatter: function(){
					return '${project.prjId}';
				}, hidden:true},
				{name: 'referenceId', index: 'referenceId', width: 68, align: 'center', key:true},
				{name: 'prjName', index: 'prjName', width: 390, align: 'left'},
				{name: 'prjVersion', index: 'prjVersion', width: 325, align: 'left'},
				{name: 'componentCount', index: 'componentCount', width: 178, align: 'center'},
				{name: 'referenceDiv', index: 'referenceDiv', width: 110, align: 'center', formatter: function(){
					return '15';
				}, hidden:true}
			],
			onSelectRow: function(id){},
			autoencode: true,
			gridview: true,
 			sortname: 'referenceId',
			viewrecords: true,
 			sortorder: 'desc',
			loadonce: false,
			height: 'auto',
			mtype: 'GET',
			loadComplete: function(data){}
		}
	},	
	// 닉네임 팝업 띄우기
	makeNickNamePopup : function(obj, finalData){
		if(obj.validMsg && obj.validMsg.length != 0){
			alertify.confirm(obj.validMsg, function () {
				bin_fn.exeSave(finalData);
			});
		}else{
			bin_fn.exeSave(finalData);
		}
	},
	// 닉네임 체크
	nickNameValid : function(mainData, subData, finalData){
		var prjId = '${project.prjId}';
		var postData = {"mainData" : JSON.stringify(mainData), "subData" : JSON.stringify(subData), "prjId" : prjId};
		
		$.ajax({
			url : '<c:url value="/project/nickNameValid/15"/>',
			type : 'POST',
			data : JSON.stringify(postData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				bin_fn.makeNickNamePopup(data, finalData);
			},
			error: function(data){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		})
	},
	makeFileTag : function(obj){
		var appendHtml = '<br>'+obj.createdDate;
		
		$('.binFileArea').append('<li><span><strong><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a>'+appendHtml+'<input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="bin_fn.deleteCsv(this, \'1\')"/></strong></span></li>');
	},
	makeFileTag2 : function(obj){
		var appendHtml = '<br>'+obj.createdDate;
		
		$('.binBinaryFileArea').append('<li><span><strong><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a>'+appendHtml+'<input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="bin_fn.deleteCsv(this, \'2\')"/></strong></span></li>');
	},
	deleteCsv : function(obj, type){
		var Seq = $(obj).prev().val();
		var object = {fileSeq : Seq};
		bin_evt.csvDelFileSeq.push(object);

		$(obj).closest('li').remove();
		
		if($('.binBinaryFileArea').find('li').length == 0) {
			$('#binBinaryFile').show();
		} else {
			$('#binBinaryFile').hide();
		}
	},
	loadToList : function(type){
		// 로드 플래그
		loadBln = true;
		// 상단 프로젝트 서치 그리드 선택 로우
		var selrow = $('#_binProjectList1').jqGrid('getGridParam', "selrow" );
		var listData = $('#_binProjectList1').jqGrid('getRowData',selrow);
		listData.referenceId = listData.prjId;
		var param = {refPrjId : selrow, typeFlag : "Y"};
		
		switch(type){
			case "append":
				var mainData =  $("#binList").jqGrid('getGridParam','data')
				  , addListData = $("#_binAddList").jqGrid("getRowData")
				  , duplicateFlag = addListData.filter(function(a){ return a.referenceId == listData.prjId }).length > 0;
				
				if(duplicateFlag){
			    	alertify.alert('<spring:message code="msg.id.duplicate" />', function(){});

			    	return;
			    }
				
				var cnt = $('#_binProjectList2').jqGrid('getGridParam', "records" );
				var ids = $('#_binAddList').jqGrid('getDataIDs');
				listData.componentCount = cnt;

				$('#_binAddList').jqGrid('addRowData',ids.length+1,listData);
				
				param["mainData"] = JSON.stringify(mainData);
				param = JSON.stringify(param);
				
				break;
			case "resetLoad":
				$('#_binAddList').jqGrid('clearGridData');
				
				var cnt = $('#_binProjectList2').jqGrid('getGridParam', "records" );
				var ids = $('#_binAddList').jqGrid('getDataIDs');
				listData.componentCount = cnt;
				
				$('#_binAddList').jqGrid('addRowData',ids.length+1,listData);
				
				break;
			default:
				break;
		}
		
		bin_fn.getBinGridData(param, type);
		
		$('.binProject2').hide();
	},
	// 다운로드 엑셀
	downloadExcel : function(){
		$.ajax({
			type: "POST",
			url: '<c:url value="/exceldownload/getExcelPost"/>',
			data: JSON.stringify({"type":"bin", "parameter":'${project.prjId}'}),
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
	closeBinPop : function(){
		$('.ossSelectPop').hide();
	},
	getSheetData : function(seq){
		var sheetNum = [];
		$('input:checkbox[name="sheetNameSelect"]').each(function(){
			if($(this).is(':checked')){
				sheetNum.push($(this).val());
			}
		});
		
		if(sheetNum.length == 0){
			alert('<spring:message code="msg.common.check.sheet" />');
			
			return;
		}else{
			loading.show();
			
			var target = $("#binList");
			fn_grid_com.totalGridSaveMode('binList');
			cleanErrMsg("binList");
			var mainData = target.jqGrid('getGridParam','data');
			var finalData = {"readType":"bin","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};

			bin_fn.exeLoadReportData(finalData);
		}
	},
	getCsvData : function(seq){
		loading.show();
		fn_grid_com.totalGridSaveMode('binList');
		cleanErrMsg("binList");

		var target = $("#binList");
		var mainData = target.jqGrid('getGridParam','data');
		var sheetNum = ["0"];
		var finalData = {"readType":"bin","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};

		bin_fn.exeLoadReportData(finalData);
	},
	getSpdxSpreadsheetData : function(seq){
		var sheetNum = ["1", "4"];

		loading.show();
		fn_grid_com.totalGridSaveMode('binList');
		cleanErrMsg("binList");

		var target = $("#binList");
		var mainData = target.jqGrid('getGridParam','data');
		var finalData = {"readType":"bin","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};

		bin_fn.exeLoadReportData(finalData);
	},
	getBinData : function(){
		var sheetNum = [];
		
		$('.sheetNum').each(function(){
			if($(this).is(':checked')){
				sheetNum.push($(this).val());
			}
		});
		
		var binCsvFileId 	= $('#binCsvFileId').val();
		var binBinaryFileId 	= $('#binBinaryFileId').val();
		
		loading.show();
		
		var target = $("#binList");
		// 메인 그리드
		var mainData = target.jqGrid('getRowData');
		// 서브 그리드
		var subData = [];
 		var arr = [];
 		var subTable;
 		arr = target.jqGrid('getDataIDs');
 		
		for(var i in arr){
			// 메인 그리드 멀티 라이센스일 경우만 서브 그리드 데이터 넘김
			if(target.jqGrid('getCell',arr[i],'licenseDiv') == "M"){
				subTable = $("#binList_"+arr[i]+"_t");
				if(subTable && subTable.length > 0) {
					subData.push(subTable.jqGrid('getRowData'));
				}
			}
			
		}
		var finalData = { "prjId" : '${project.prjId}', "sheetNums" : sheetNum , "mainData" : JSON.stringify(mainData), "subData" : JSON.stringify(subData)
				, "binFileSeq" : ""+binCsvFileId, "binBinarySeq" : ""+binBinaryFileId };
		
		bin_fn.exeLoadBuildImageReportData(finalData);
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
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){});
					} else {
						alertify.error('<spring:message code="msg.common.valid" />', 0);
					}
				} else {
					$('.sheetSelectPop').hide();
					
					if(data.externalData) { // validData 표시
						binValidMsgData = data.externalData;
					}
					
					if(data.externalData2) { // validData 표시
						binDiffMsgData = data.externalData2;
					}
					

					if(data.externalData3) { // validData 표시
						binInfoMsgData = data.externalData3;
					}
					
					bin_fn.makeOssList(data.resultData);
					
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
				
				alertify.error('<spring:message code="msg.common.valid" />', 0);
			}
		});
	},

	makeOssList : function(data){
		binMainData = data.mainData;
		
		// 리로드 대신 그리드 삭제 후 다시 그리기
		$("#binList").jqGrid('GridUnload');
		
		bin_grid.load();

		// totla record 표시
		$("#binList_toppager_right, #binPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binMainData.length+'</div>');
		
		fn_grid_com.addEtcKeyDownEvent($('#binList'), binValidMsgData, binDiffMsgData, binInfoMsgData, com_fn.getLicenseName);
	},
	identificationProjectSearch : function(id){
		//2018-07-25 choye 추가
		$("#_binProjectList2").jqGrid('clearGridData');
		var postData = $("#_binProjectList2").jqGrid('getGridParam', 'postData');
		postData.referenceId = id;
		
        $.ajax({
        	url : '<c:url value="/project/identificationProjectSearch/15"/>',
            type : 'GET',
            dataType : 'json',
            data : postData,
            cache : false,
            success : function(data){
				binData = data.rows;
				var target = $("#_binProjectList2");
				target.jqGrid('setGridParam', {data:binData}).trigger('reloadGrid');
            },
            error : function(xhr, ajaxOptions, thrownError){
                alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
        });
    },
    showDialog : function(){
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
		var btnHtm = '<br/><b></b><br/>';
		btnHtm += '<input type="button" value="Reset & Load" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="bin_fn.loadToList(\'resetLoad\')"/>&nbsp;&nbsp;&nbsp;';
		btnHtm += '<input type="button" value="Load & Append" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="bin_fn.loadToList(\'append\')"/>&nbsp;&nbsp;&nbsp;';
		btnHtm +='<input type="button" value="Cancel" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="$(\'.ajs-close\').trigger(\'click\')"/>&nbsp;&nbsp;&nbsp;';
		
		alertify.commentDialog(btnHtm);
    }
}

//SRC 그리드
var bin_grid = {
	load: function(){
		var currentOssName = "";
		var ondblClickRowBln = false;
		var binList = $("#binList");
		
		binList.jqGrid({
			datatype: 'local',
			data : binMainData,
			colNames: ['gridId', 'ID_KEY','ID','Binary Name', 'ReferenceId', 'ReferenceDiv', 'OssId', 'OSS Name','OSS Version','License','Download Location'
			           ,'Homepage','LicenseId','Copyright Text'/*,'License Text'*/
			           ,'CVE ID','Vulnera<br/>bility','<input type="checkbox" onclick="fn_grid_com.onCboxClickAll(this,\'binList\');">Exclude','LicenseDiv', 'customBinaryYn', 'Comment'],
			colModel: [
				{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
				{name: 'componentId', index: 'componentId', width: 40, align: 'center', hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
				{name: 'binaryName', index: 'binaryName', width: 100, align: 'left', editable:false, edittype:'text', template: searchStringOptions},
				{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
				{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
				{name: 'ossId', index: 'ossId', width: 29, align: 'center', editable:false, hidden:true},
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
											
											fn_grid_com.griOssVersions($('#'+rowid+'_ossVersion')[0], e.value, 'binList');
											fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData, binInfoMsgData);
										}
									}).dblclick(function(){
										var rowid = (e.id).split('_')[0];
										var licenseName = com_fn.getLicenseName(binList.getRowData(rowid));
										
										binList.jqGrid("setCell", rowid, "licenseName", licenseName);
										fn_grid_com.saveCellData(binList.attr("id"), rowid, "licenseName", licenseName, null, null);
										binList.jqGrid('saveRow',rowid);
										
										fn_grid_com.mvOssPage(binList, rowid);
									});
									
									currentOssName = e.value;
								}
						}
				},
				{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left', editable: false, edittype: 'text', template: searchStringOptions,
					editoptions: {
						dataInit:
							function (e) { 
								fn_grid_com.griOssVersions(e, currentOssName, 'binList');
								$(e).on( "autocompletechange", function() {
									var rowid = (e.id).split('_')[0];

									fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData, binInfoMsgData);
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
									
									fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData, binInfoMsgData);
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
				{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', editable:false, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions, 
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
									
									fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData, binInfoMsgData);
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
				{name: 'homepage', index: 'homepage', width: 100, align: 'left', editable:false, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, template: searchStringOptions, 
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
									
									fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData,binInfoMsgData);
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
				{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', editable:false, edittype:'text', hidden:true},
				{name: 'copyrightText', index: 'copyrightText', width: 150, align: 'left', editable:false, edittype:"textarea", template: searchStringOptions, editoptions:{rows:"5",cols:"24", 
					dataInit:
						function (e) { 
							$(e).on("change", function() {
								var rowid = (e.id).split('_')[0];
								fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData,binInfoMsgData);
							});
						}
					}
				},
				{name: 'cveId', index: 'cveId', hidden:true},
				{name: 'cvssScore', index: 'cvssScore', width: 70, align: 'center', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : true, sorttype:'float', template: searchNumberOptions},
				{name: 'excludeYn', index: 'excludeYn', width: 60, align: 'center', formatter: fn_grid_com.cboxFormatter, unformat: fn_grid_com.cboxUnFormatter, search: false,
					editoptions: {
						dataInit:
							function (e) { 
								$(e).on("change", function() {
									var rowid = (e.id).split('_')[0];

									fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData,binInfoMsgData);
								});
							}
					}
				
				},
				{name: 'licenseDiv', index: 'licenseDiv', width: 100, align: 'left', editable:false, hidden:true},
				{name: 'customBinaryYn', index: 'customBinaryYn', editable:false, hidden:true},
				{name: 'comments', index: 'comments', width: 150, align: 'left', editable:true, edittype:"textarea", template: searchStringOptions, editoptions:{rows:"5",cols:"24", 
					dataInit:
						function (e) { 
							$(e).on("change", function() {
								var rowid = (e.id).split('_')[0];

								fn_grid_com.saveCellData("binList",rowid,e.name,e.value,binValidMsgData,binDiffMsgData,binInfoMsgData);
							});
						}
					}
				}
			],
			autoencode: true,
			editurl:'clientArray',
 			autowidth: true,
			height: 'auto',
			gridview: true,
		   	pager: '#binPager',
			rowNum: 200,
			rowList: [200, 500, 1000, 5000],
			recordpos:'right',
			toppager:true,
			loadonce:true,
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
					$("#binList tr.singleLicenseClass").find("td:first").removeClass("sgexpanded sgcollapsed").find("a").hide();
					
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
			    fn_grid_com.setWarningClass(binList,rowid,["ossName","licenseName"]);		    
//				return true;
			},
			onCellSelect: function(rowid,iCol,cellcontent,e) {
				if(iCol == "3") {
					fn_grid_com.showOssViewPage(binList, rowid, true, binValidMsgData, binDiffMsgData, binInfoMsgData, com_fn.getLicenseName);
				}
				
			},
			ondblClickRow: function(rowid,iRow,iCol,e) {
				if(iCol == "4"){ 
					com_fn.exitCell(_mainLastsel, "binList");
					
					fn_grid_com.showBinaryViewPage(binList, rowid, true, binValidMsgData, binDiffMsgData, binInfoMsgData);
				}
				
				cleanErrMsg("binList", rowid);
				fn_grid_com.setCellEdit(binList, rowid, binValidMsgData, binDiffMsgData, binInfoMsgData, com_fn.getLicenseName);

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

                                var nextCol = binList.jqGrid('getGridParam', 'colModel')[iCol].name
                                var nextRow = rowid
                                $('#'+nextRow+"_"+nextCol).focus();
			},
			onPaging: function(action) {
				cleanErrMsg("binList");
				fn_grid_com.totalGridSaveMode('binList');
			},
			gridComplete : function() {
				cleanErrMsg("binList");

				if(binValidMsgData) {
					gridValidMsgNew(binValidMsgData, "binList");
				}
				
				if(binDiffMsgData) {
					gridDiffMsg(binDiffMsgData, "binList");
				}
				
				if(binInfoMsgData) {
					gridInfoMsg(binInfoMsgData, "binList");
				}				
			},
			removeHighLight : true
		});
		binList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
		binList.jqGrid('navGrid',"#binPager",{add:true,edit:false,del:true,search:false,refresh:false
												  , addfunc: function () {
													  com_fn.saveFlagObject["BIN"] = false;
													  fn_grid_com.rowAddNew('binList',binList,"main", null, com_fn.getLicenseName);
												  }
												  , delfunc: function () { fn_grid_com.rowDelNew(binList,"main");}
												  , cloneToTop:true
		});
		
		$('#binList').closest(".ui-jqgrid-bdiv").css({"height":"500px", "overflow-y" : "scroll"});
	}
}
</script>