<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//==========================================================================================
// PROJECT DEP
// REFERENCE_ID = PROJECT_ID
// REFERENCE_DIV = 16 DEP
//==========================================================================================
var ossNames = [];
var objs = [];
var licenseNames = [];
var depData;

$(document).click(function(e){
	if(!$("#depExportContainer").has(e.target).length) {
		$("#depExportList").hide();
	}
});

// DEP 이벤트
var dep_evt = {
	csvDelFileSeq : [],
	csvFileSeq : [],
	init: function(){
		dep_fn.getDepGridData();
		
		$('#_depAddList').jqGrid(dep_fn.setParamProject3());
		
		var depList = $("#depList");
		//Not aplicable
		var aplicable = $('#applicableDep').is(':checked');
		
		if(aplicable){
			$('.depBtn').hide();
		}

		// 그리드 리셋 버튼
		$("#depResetUp").click(function(e){
			e.preventDefault();
	 		
			alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
				if (e) {
					$("#depList").jqGrid('clearGridData');
					$("#_depAddList").jqGrid('clearGridData');
				} else {
					return false;
				}
			});
		});
		
		// 그리드 저장 버튼
		$("#depSaveUp").click(function(e){
			if (com_fn.checkStatus()){
				e.preventDefault();
				
				com_fn.exitCell(_mainLastsel, "depList");
				
				alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
					if (e) {
						com_fn.exitRow("depList");						
						// 메인, 서브 그리드 세이브 모드
						fn_grid_com.totalGridSaveMode('depList');
						// 닉네임 체크
						dep_fn.saveMakeData();
					} else {
						return false;
					}
				});
			}else {
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}
		});
		
		// 프로젝트 조회 버튼
		$('#depProjectSearchBtn').click(function(e){
			e.preventDefault();
			
			var postData = $('#depProjectForm').serializeObject();
			
			$('#_depProjectList1').jqGrid('setGridParam', {postData:postData}).trigger("reloadGrid");
			$('.depProject1').show();
			$('.depProject2').hide();
		});
		
		var accept1 = '';
		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
			<c:if test="${file eq '12'}">
			accept1 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
			</c:if>
		</c:forEach>
		
		//파일업로드
		$('#depCsvFile').uploadFile({
			url:'<c:url value="/project/csvFile"/>',
			multiple:false,
			dragDrop:true,
			fileName:'myfile',
			allowedTypes:accept1,
			sequential:true,
			sequentialCount:1,
			dynamicFormData: function(){
				var data ={ "registFileId" :$('#depCsvFileId').val(), "tabNm" : "DEP"}
				
				return data;
			},
			onSuccess:function(files,data,xhr,pd) {
				var result = jQuery.parseJSON(data);
				
				if(result[1] == null) {
					if(result[0] == "TAB_GUBN_ERROR") {
						alertify.error('<spring:message code="msg.common.upload.failed.separator" />', 0);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					}else{
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					} 
				} else {
					if(result[0] == "FILE_SIZE_LIMIT_OVER") {
						alertify.alert(result[1], function(){
							$('.ajax-file-upload-statusbar').fadeOut('slow');
							$('.ajax-file-upload-statusbar').remove();
						});
					} else if(result[2] == "CSV_FILE") {
						dep_fn.getCsvData(result[0][0].registSeq);

						$('#depCsvFileId').val(result[0][0].registFileId);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();

						dep_fn.makeFileTag(result[0][0]);
					} else if(result[2] == "EXCEL_FILE") {
						if(result[1].length != 0) {
							$('.sheetSelectPop').show();
							$('.sheetSelectPop .sheetNameArea').children().remove();
							$('.sheetSelectPop .sheetNameArea').text('');
							for(var i = 0; i < result[1].length; i++) {
								var num = i+1;
								var checkedTxt = "";
								
								var sheetName = result[1][i].name.toUpperCase().trim();
								if(sheetName.startsWith("DEP") || sheetName.indexOf("DEPENDENCY") > -1){
									checkedTxt = "checked";
								}
								
								$('.sheetSelectPop .sheetNameArea').append('<li><input type="checkbox" name="sheetNameSelect" value="'+result[1][i].no+'" id="sheet'+result[1][i].no+'" class="sheetNum" '+checkedTxt+'>'
										+'<label for="sheet'+result[1][i].no+'">'+result[1][i].name+'</label></li>');
								$('.sheetSelectPop .sheetApply').attr('onclick', 'dep_fn.getSheetData('+result[0][0].registSeq+')');
							}
						}
						
						$('#depCsvFileId').val(result[0][0].registFileId);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						
						dep_fn.makeFileTag(result[0][0]);	
					} else if(result[2] == "SPDX_SPREADSHEET_FILE"){
						dep_fn.getSpdxSpreadsheetData(result[0][0].registSeq);

						$('#depCsvFileId').val(result[0][0].registFileId);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();

						dep_fn.makeFileTag(result[0][0]);
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
};

// dep 그리드 데이터 전역 변수
var depMainData;
var depValidMsgData;
var depDiffMsgData;

// dep 함수
var dep_fn = {
	// dep 그리드 데이터
	getDepGridData : function(param, type){
		var url = "append".indexOf(type) > -1  
					? '<c:url value="/project/identificationMergedGrid/${project.prjId}/16"/>'
					: '<c:url value="/project/identificationGrid/${project.prjId}/16"/>'
		  , type = "append".indexOf(type) > -1  ? 'POST' : 'GET'
		  , data = param || {referenceId : '${project.prjId}', typeFlag : 'N'};
		
		$.ajax({
			url : url,
			type : type,
			dataType : 'json',
			cache : false,
			data : data,
			contentType : 'application/json',
			success : function(data){
				$('.ajs-close').trigger("click");
				
				depMainData = data.mainData;
				depValidMsgData = []; //초기화
				depDiffMsgData = []; //초기화
				
				if(data.validData) {
					depValidMsgData = data.validData;
				}
				
				if(data.diffData) {
					depDiffMsgData = data.diffData;
				}
				
				// 리로드 대신 그리드 삭제 후 다시 그리기
				$("#depList").jqGrid('GridUnload');
				dep_grid.load();

				// totla record 표시
				$("#depList_toppager_right, #depPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+depMainData.length+'</div>');
				
				fn_grid_com.addEtcKeyDownEvent($('#depList'), depValidMsgData, depDiffMsgData, null, com_fn.getLicenseName);
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	// 저장 데이터 만들기
	saveMakeData : function(){
		cleanErrMsg("depList");
		var target = $("#depList");
		var prjId = '${project.prjId}';
		var csvFileId = $('#depCsvFileId').val();
		var fileData = dep_evt.csvDelFileSeq;
		var csvFiles = $(".depCsvFileArea").find("input[type=hidden]");
		var fileSeq = [];
		for(var i=0; i<csvFiles.length; i++){
			var obj = {fileSeq : csvFiles.eq(i).val()};
			fileSeq.push(obj);
		}
		if(fileSeq.length == 0) {
			$("#depCsvFileId").val("");
			csvFileId = "";
		}
		var identificationSubStatusDep = $("#applicableDep:checked").val();
		var mainData = target.jqGrid('getGridParam','data');
		var depAddListData = $("#_depAddList").jqGrid("getRowData");
		// 최종 데이터
		var finalData = {"prjId" : prjId, "csvFileId" : csvFileId, "csvDelFileIds" : JSON.stringify(fileData), "csvFileSeqs" : JSON.stringify(fileSeq)
				   , "identificationSubStatusDep" : identificationSubStatusDep, "mainData" : JSON.stringify(mainData), "depAddListData" : JSON.stringify(depAddListData)};
		
		dep_fn.exeSave(finalData);
	},
	// 저장
	exeSave : function(finalData){
		$.ajax({
			url : '<c:url value="/project/saveDep"/>',
			type : 'POST',
			data : JSON.stringify(finalData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				_mainLastsel = -1;
				
				if("false" == data.isValid) {
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					if(data.validMsg) {
						alertify.alert(data.validMsg, function(){});
					}
					
					if(depValidMsgData) {
						gridValidMsgNew(depValidMsgData, "depList");
					}
					
					if(depDiffMsgData) {
						gridDiffMsg(depDiffMsgData, "depList");
					}
				} else {
					if("10" == data.resCd) {
						dep_fn.getDepGridData();
						$("#mergeYn").val("N");
						dep_evt.csvDelFileSeq = [];
						dep_evt.csvFileSeq = [];
						com_fn.saveFlagObject["DEP"] = true;
						alertify.success('<spring:message code="msg.common.success" />');

						curIdenStatus = data.resultData||"";
						if(curIdenStatus == "PROG"){
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
		var value = $('input[name=depSelectOption]:checked').val();
		
		if(value == 1){
			$('#depUploadSearch').show();
			$('#depProjectSearch').hide();
			$('.depProjectSearch').hide();
		} else {
			$('#depUploadSearch').hide();
			$('#depProjectSearch').show();
			$('.depProjectSearch').show();
			$('#_depProjectList1').jqGrid(dep_fn.setParamProject1());
			$('#_depProjectList2').jqGrid(dep_fn.setParamProject2());
			$('#_depAddList').jqGrid(dep_fn.setParamProject3());
			$('#_depProjectList2').jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
		}
	},
	// 프로젝트 검색
	setParamProject1 : function(){
		return {
			url: '<c:url value="/project/identificationProject/16"/>',
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
				dep_fn.identificationProjectSearch(id);
				$('.depProject2').show();
			},
			autoencode: true,
			autowidth: true,
			gridview: true,
			sortable: function(permutation){
			},
			rowNum: 20,
			rowList: [20, 40, 60],
			pager: '#depProjectPager1',
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
			colNames: ['ID_KEY','ID','ReferenceId','Source Name or Path','OSS Name','OSS Version','License', 'Vulnerability','Exclude'],
			colModel: [
				{name: 'componentId', index: 'componentId', width: 40, align: 'center', key:true, hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
				{name: 'referenceId', index: 'referenceId', width: 40, align: 'center', hidden:true},
				{name: 'filePath', index: 'filePath', width: 150, align: 'left', template: searchStringOptions},
				{name: 'ossName', index: 'ossName', width: 300, align: 'left', template: searchStringOptions},
				{name: 'ossVersion', index: 'ossVersion', width: 150, align: 'center', template: searchStringOptions},
				{name: 'licenseName', index: 'licenseName', width: 218, align: 'left', formatter: fn_grid_com.displayLicense, template: searchStringOptions},
				{name: 'cvssScore', index: 'cvssScore', width: 98, align: 'center', formatter:fn_grid_com.displayVulnerability, unformatter:fn_grid_com.unformatter, template: searchNumberOptions, sortable : true, sorttype:'float'},
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
				var target = $("#_depProjectList2");
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
			postData : {prjId : '${project.prjId}', referenceDiv : '16'},
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
					return '16';
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
	makeFileTag : function(obj){
		var appendHtml = '<br>'+obj.createdDate;
		var _url = '<c:url value="/download/'+obj.registSeq+'/'+obj.fileName+'"/>';
		$('.depCsvFileArea').append('<li><span><strong><a href="'+_url+'">'+obj.originalFilename+'</a>'+appendHtml+'<input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="com_fn.deleteFiles(this, \'1\')"/></strong></span></li>');
	},
	deleteCsv : function(obj, type){
		var Seq = $(obj).prev().val();
		var object = {fileSeq : Seq};
		dep_evt.csvDelFileSeq.push(object);
		$(obj).closest('li').remove();
	},
	loadToList : function(type){
		// 로드 플래그
		loadBln = true;
		// 상단 프로젝트 서치 그리드 선택 로우
		var selrow = $('#_depProjectList1').jqGrid('getGridParam', "selrow" );
		var listData = $('#_depProjectList1').jqGrid('getRowData',selrow);
		listData.referenceId = listData.prjId;
		var param = {refPrjId : selrow, typeFlag : "Y"};
		
		switch(type){
			case "append":
				var mainData =  $("#depList").jqGrid('getGridParam','data');
				var addListData = $("#_depAddList").jqGrid("getRowData");
				var duplicateFlag = addListData.filter(function(a){ 
					return a.referenceId == listData.prjId;
				}).length > 0;
				
				if(duplicateFlag){
			    	alertify.alert('<spring:message code="msg.id.duplicate" />', function(){});

			    	return;
			    }
				
				var cnt = $('#_depProjectList2').jqGrid('getGridParam', "records" );
				var ids = $('#_depAddList').jqGrid('getDataIDs');
				listData.componentCount = cnt;
				$('#_depAddList').jqGrid('addRowData',ids.length+1,listData);
				
				param["mainData"] = JSON.stringify(mainData);				
				param = JSON.stringify(param);
				
				break;
			case "resetLoad":
				$('#_depAddList').jqGrid('clearGridData');
				
				var cnt = $('#_depProjectList2').jqGrid('getGridParam', "records" );
				var ids = $('#_depAddList').jqGrid('getDataIDs');
				listData.componentCount = cnt;
				
				$('#_depAddList').jqGrid('addRowData',ids.length+1,listData);
				
				break;
			default:
				break;
		}
		
		dep_fn.getDepGridData(param, type);
		$('.depProject2').hide();
	},
	exportList : function(obj) {
    	var exportListId = '#' + $(obj).siblings("div").attr("id");
        if ($(exportListId).css('display')=='none') {
            $(exportListId).show();
        }else{
            $(exportListId).hide();
        }
        $(exportListId).menu();
    },
    selectDownloadFile : function(target) {
    	// download file
        if (target === "report_sub") {
        	dep_fn.downloadExcel();
        } else {
        	var identificationStatus = '${project.identificationStatus}';
        	if ("CONF" != identificationStatus) {
        		alertify.confirm('<spring:message code="msg.common.check.sbom.export" />', function (e) {
    				if (e) {
    					dep_fn.selectDownloadFileValidation();
    				} else {
    					return false;
    				}
    			});
        	} else {
        		dep_fn.selectDownloadFileValidation();
        	}
        }
        // hide list
        $("#depExportList").hide();
    },
    selectDownloadFileValidation : function() {
    	if (com_fn.checkSelectDownloadFile('DEP')) {
    		com_fn.downloadYaml('DEP');
    	} else {
    		alertify.error('<spring:message code="msg.common.check.sbom.export2" />', 0);
    	}
    },
	// 다운로드 엑셀
	downloadExcel : function(){
		$.ajax({
			type: "POST",
			url: '<c:url value="/exceldownload/getExcelPost"/>',
			data: JSON.stringify({"type":"dep", "parameter":'${project.prjId}'}),
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
		dep_fn.cancelFileDel();
		$('.sheetSelectPop').hide();
	},
	closeAndroidPop : function(){
		$('.ossSelectPop').hide();
	},
	getCsvData : function (seq){
		loading.show();
		fn_grid_com.totalGridSaveMode('depList');
		cleanErrMsg("depList");

		var target = $("#depList");
		var mainData = target.jqGrid('getGridParam','data');
		var sheetNum = ["0"];
		var finalData = {"readType":"dep","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};
		var object = {fileSeq : seq};

		dep_evt.csvFileSeq.push(object);
		dep_fn.exeLoadReportData(finalData);
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
			fn_grid_com.totalGridSaveMode('depList');
			cleanErrMsg("depList");
			
			var target = $("#depList");
			var mainData = target.jqGrid('getGridParam','data');
			var finalData = {"readType":"dep","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};
			var object = {fileSeq : seq};

			dep_evt.csvFileSeq.push(object);
			dep_fn.exeLoadReportData(finalData);
		}
	},
	getSpdxSpreadsheetData : function(seq){
		var sheetNum = ["1", "4"];

		loading.show();
		fn_grid_com.totalGridSaveMode('depList');
		cleanErrMsg("depList");

		var target = $("#depList");
		var mainData = target.jqGrid('getGridParam','data');
		var finalData = {"readType":"dep","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData)};
		var object = {fileSeq : seq};

		dep_evt.csvFileSeq.push(object);
		dep_fn.exeLoadReportData(finalData);
	},
	// load report data
	exeLoadReportData : function(finalData){
		$.ajax({
			url : '<c:url value="/project/getSheetData"/>',
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
						depValidMsgData = data.externalData;
					}

					if(data.externalData2) { // validData 표시
						depDiffMsgData = data.externalData2;
					}

					dep_fn.makeOssList(data.resultData);

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

		depMainData = data.mainData;
		
		// 리로드 대신 그리드 삭제 후 다시 그리기
		$("#depList").jqGrid('GridUnload');
		dep_grid.load();

		// totla record 표시
		$("#depList_toppager_right, #depPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+depMainData.length+'</div>');
	},
	
	// upload Cancel시 DB삭제
	cancelFileDel : function(){
		var FileSeq = [];
		var tabGubn = $(".tabMenu").find("span").text();
		var seq = "";
		
		if(tabGubn == "SRC"){
			seq = $('.csvFileArea > li:last').find("input[type=hidden]").val();
		} else if(tabGubn == "BIN"){
			seq = $('.binFileArea > li:last').find("input[type=hidden]").val();
		} else if (tabGubn == "DEP") {
			seq = $('.depFileArea > li:last').find("input[type=hidden]").val();
		}
		
		if(seq == ""){
			return;
		}
		
		var object = {fileSeq : seq};
		FileSeq.push(object);
		var Data = {"csvDelFileIds" : JSON.stringify(FileSeq)};

		$.ajax({
			url : '<c:url value="/project/cancelFileDelDep"/>',
			type : 'POST',
			data : JSON.stringify(Data),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				if("10"== data.resCd){
					$('.depFileArea > li:last').remove();				
				}else{
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			},
			error: function(data){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	identificationProjectSearch : function(id){
		//2018-07-25 choye 추가
		$("#_depProjectList2").jqGrid('clearGridData');
		var postData = $("#_depProjectList2").jqGrid('getGridParam', 'postData');
		postData.referenceId = id;
        $.ajax({
            url : '<c:url value="/project/identificationProjectSearch/16"/>',
            type : 'GET',
            dataType : 'json',
            data : postData,
            cache : false,
            success : function(data){
				depData = data.rows;
				
				var target = $("#_depProjectList2");
				
				target.jqGrid('setGridParam', {data:depData}).trigger('reloadGrid');
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
    			
		//launch it.
		var btnHtm = '<br/><b></b><br/>';
		btnHtm += '<input type="button" value="Reset & Load" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="dep_fn.loadToList(\'resetLoad\')"/>&nbsp;&nbsp;&nbsp;';
		btnHtm += '<input type="button" value="Load & Append" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="dep_fn.loadToList(\'append\')"/>&nbsp;&nbsp;&nbsp;';
		btnHtm +='<input type="button" value="Cancel" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="$(\'.ajs-close\').trigger(\'click\')"/>&nbsp;&nbsp;&nbsp;';
		
		alertify.commentDialog(btnHtm);
    },
    displayVulnerability : function(cellvalue, options, rowObject){
    	var display = "";
		var _url = "";
		var prjId = '${project.prjId}';
		var ossName = rowObject.ossName;
		
		if (prjId) {
			_url = '<c:url value="/vulnerability/vulnpopup?prjId='+prjId+'&ossName='+ossName+'&ossVersion='+rowObject.ossVersion+'&vulnType="/>';
		} else {
			_url = '<c:url value="/vulnerability/vulnpopup?ossName='+ossName+'&ossVersion='+rowObject.ossVersion+'&vulnType="/>';
		}
		
		if(parseInt(cellvalue) >= 9.0 ) {
			display="<span class=\"iconSet vulCritical\" onclick=\"openNVD2('"+ossName+"','"+_url+"')\">"+cellvalue+"</span>";
		} else if(parseInt(cellvalue) >= 7.0 ) {
			display="<span class=\"iconSet vulHigh\" onclick=\"openNVD2('"+ossName+"','"+_url+"')\">"+cellvalue+"</span>";
		} else if(parseInt(cellvalue) >= 4.0) {
			display="<span class=\"iconSet vulMiddle\" onclick=\"openNVD2('"+ossName+"','"+_url+"')\">"+cellvalue+"</span>";
		} else if(parseInt(cellvalue) > 0) {
			display="<span class=\"iconSet vulLow\" onclick=\"openNVD2('"+ossName+"','"+_url+"')\">"+cellvalue+"</span>";
		} else if(parseInt(cellvalue) == 0 || cellvalue == undefined) {
			display="<span style=\"font-size:0;\"></span>";
		} else {
			display=cellvalue;
		}
		
		return display;
    }
}

// 그리드
var dep_grid = {
	load: function(){
		var currentOssName = "";
		var ondblClickRowBln = false;
		var depList = $("#depList");
		
		depList.jqGrid({
			datatype: 'local',
			data : depMainData,
			colNames: ['gridId', 'ID_KEY', 'ID', 'Manifest file', 'ReferenceId', 'ReferenceDiv', 'OssId', 'OSS Name','OSS Version','License','Download Location'
			           ,'Homepage','LicenseId','Copyright Text'
			           ,'CVE ID','Vulnera<br/>bility','<input type="checkbox" onclick="fn_grid_com.onCboxClickAll(this,\'depList\');">Exclude','LicenseDiv', 'Comment', 'Dependencies', 'refOssName'],
			colModel: [
				{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
				{name: 'componentId', index: 'componentId', width: 40, align: 'center', hidden:true},
				{name: 'componentIdx', index: 'componentIdx', width: 40, align: 'center', sorttype: 'int', search: false},
				{name: 'filePath', index: 'filePath', width: 170, align: 'left', editable:false, template: searchStringOptions,
					editoptions: {
						dataInit:
							function (e) { 
								$(e).on("change", function() {
									var rowid = (e.id).split('_')[0];
									fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
								});
							}
					}
				},
				{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
				{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
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
											
											fn_grid_com.griOssVersions($('#'+rowid+'_ossVersion')[0], e.value, 'depList');
											fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
										}
									
									}).dblclick(function(){
										var rowid = (e.id).split('_')[0];
										var licenseName = com_fn.getLicenseName(depList.getRowData(rowid));
										
										depList.jqGrid("setCell", rowid, "licenseName", licenseName);
										fn_grid_com.saveCellData(depList.attr("id"), rowid, "licenseName", licenseName, null, null);
										depList.jqGrid('saveRow',rowid);
										
										fn_grid_com.mvOssPage(depList, rowid);
									});
									
									currentOssName = e.value;
								}
						}
				},
				{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left', editable: false, edittype: 'text', template: searchStringOptions,
					editoptions: {
						dataInit:
							function (e) { 
								fn_grid_com.griOssVersions(e, currentOssName, 'depList');
								$(e).on( "autocompletechange", function() {
									var rowid = (e.id).split('_')[0];
									
									fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
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
									
									fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
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

										fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
									}
								});
							}
 					}
 				},
				{name: 'downloadLocation', index: 'downloadLocation', width: 100, align: 'left', editable:false, template: searchStringOptions, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, 
 					editoptions: {
						dataInit: function (e) { 
							$(e).on("change", function() {
								var rowid = (e.id).split('_')[0];
								var value = e.value;
								
								if(value.charAt(value.length-1) == "/"){
									value = value.slice(0, -1); // 마지막 문자열 제거

									$("#"+rowid+"_downloadLocation").val(value);
								}

								fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
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
				{name: 'homepage', index: 'homepage', width: 100, align: 'left', editable:false, template: searchStringOptions, formatter: fn_grid_com.displayUrl, unformat: fn_grid_com.unDisplayUrl, 
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
									
									fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
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
				{name: 'copyrightText', index: 'copyrightText', width: 150, align: 'left', editable:false, template: searchStringOptions, edittype:"textarea", editoptions:{rows:"5",cols:"24", 
					dataInit:
						function (e) { 
							$(e).on("change", function() {
								var rowid = (e.id).split('_')[0];

								fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
							});
						}
					}
				},
				{name: 'cveId', index: 'cveId', hidden:true},
				{name: 'cvssScore', index: 'cvssScore', width: 80, align: 'center', template: searchNumberOptions, formatter:dep_fn.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : true, sorttype:'float'},
				{name: 'excludeYn', index: 'excludeYn', width: 50, align: 'center', search: false, formatter: fn_grid_com.cboxFormatter, unformat: fn_grid_com.cboxUnFormatter,
 					editoptions: {
						dataInit:
							function (e) { 
								$(e).on("change", function() {
									var rowid = (e.id).split('_')[0];

									fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
								});
							}
					}	
				},
				{name: 'licenseDiv', index: 'licenseDiv', width: 100, align: 'left', editable:false, hidden:true},
				{name: 'comments', index: 'comments', width: 150, align: 'left', editable:true, template: searchStringOptions, edittype:"textarea", editoptions:{rows:"5",cols:"24", 
					dataInit:
						function (e) {
							$(e).on("change", function() {
								var rowid = (e.id).split('_')[0];

								fn_grid_com.saveCellData("depList",rowid,e.name,e.value,depValidMsgData,depDiffMsgData);
							});
						}
					}
				},
				{name: 'dependencies', index: 'dependencies', width: 150, align: 'left', editable:true, template: searchStringOptions, edittype:"text"},
				{name: 'refOssName', index: 'refOssName', width: 50, hidden:true}
			],
			autoencode: true,
			editurl:'clientArray',
 			autowidth: true,
			height: 'auto',
			gridview: true,
		   	pager: '#depPager',
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
					$("#depList tr.singleLicenseClass").find("td:first").removeClass("sgexpanded sgcollapsed").find("a").hide();
					
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
			    fn_grid_com.setWarningClass(depList,rowid,["ossName","licenseName"]);
			    return false;
			},
			onCellSelect: function(rowid,iCol,cellcontent,e) {
				if(iCol=="3") {
					fn_grid_com.showOssViewPage(depList, rowid, true, depValidMsgData, depDiffMsgData, null, com_fn.getLicenseName);
				}
			},
			ondblClickRow: function(rowid,iRow,iCol,e) {
				// 체크 박스 영역 제외
				cleanErrMsg("depList", rowid);

				fn_grid_com.setCellEdit(depList, rowid, depValidMsgData, depDiffMsgData, null, com_fn.getLicenseName);

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
				var nextCol = depList.jqGrid('getGridParam', 'colModel')[iCol].name
				var nextRow = rowid
				$('#'+nextRow+"_"+nextCol).focus();
			},
			onPaging: function(action) {
				cleanErrMsg("depList");
				
				fn_grid_com.totalGridSaveMode('depList');
			},
			gridComplete : function() {
				cleanErrMsg("depList");
				
				if(depValidMsgData) {
					gridValidMsgNew(depValidMsgData, "depList");
				}
				
				if(depDiffMsgData) {
					gridDiffMsg(depDiffMsgData, "depList");
				}
			},
			removeHighLight : true
		});
		
		depList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn",
			afterSearch: function () {
				if(depValidMsgData.length > 0){ 
					cleanErrMsg("depList");
					gridValidMsgNew(depValidMsgData, "depList");
				}
			}
		});
		
		depList.jqGrid('navGrid',"#depPager",{add:true,edit:false,del:true,search:false,refresh:false
												  , addfunc: function () { 
													  com_fn.saveFlagObject["DEP"] = false;
													  fn_grid_com.rowAddNew('depList',depList,"main", null, com_fn.getLicenseName);
												  }
												  , delfunc: function () { fn_grid_com.rowDelNew(depList,"main");}
												  , cloneToTop:true
		});
		
		$('#depList').closest(".ui-jqgrid-bdiv").css({"height":"500px", "overflow-y" : "scroll"});
	}
}
</script>