<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<script type="text/javascript">
//==========================================================================================
// PROJECT SRC
// REFERENCE_ID = PROJECT_ID
// REFERENCE_DIV = 11 SRC
//==========================================================================================
var ossNames = [];
var objs = [];
var licenseNames = [];
var isSort = false;
var guideData =  ${ct:getAllValuesJson(ct:getConstDef('CD_LICENSE_GUID'))};
$(document).ready(function () {
	'use strict';
	
	src_evt.init();
});

// SRC 이벤트
var src_evt = {
	csvDelFileSeq : [],
	csvFileSeq : [],
	init: function(){
		src_fn.getSrcGridData();
		
		var srcList = $("#srcList");
		var vPrjName = "${project.prjName}"+(('${project.prjVersion}'!="")?' ('+'${project.prjVersion}'+')':"");
		var createdDate = "${ct:formatDateSimple(project.createdDate)}";
		var vCreated = '${project.prjUserName}'+" "+'${project.prjDivisionName }'+' ('+createdDate+')';
		var vOsType = '${project.osTypeEtc}';
		var vDistribution = '${project.destributionName}';
		var vWatcher = '';
		
		<c:forEach var="watcher" items="${project.watcherList}" varStatus="Status">
			<c:if test="${not empty watcher.prjDivision}">
			vWatcher += '${watcher.prjDivisionName}'+'/'+'${watcher.prjUserName}';
			</c:if>
			<c:if test="${empty watcher.prjDivision}">
			vWatcher += '${watcher.prjEmail}'+'/'+'${watcher.prjUserName}';
			</c:if>
			<c:if test="${!Status.last}">
			vWatcher += ', '
			</c:if>
		</c:forEach>
		
		$("#vPrjName").text(vPrjName);
		$("#vCreated").text(vCreated); 
		$("#vOsType").text(vOsType);
		$("#vDistribution").text(vDistribution);
		$("#vWatcher").text(vWatcher);
		
		// 그리드 리셋 버튼
		$("#srcReset, #srcResetUp").click(function(e){
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
		$("#srcSave, #srcSaveUp").click(function(e){
			e.preventDefault();

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
		});
		
		// 프로젝트 조회 버튼
		$('#srcProjectSearchBtn').click(function(e){
			e.preventDefault();

			var postData = $('#srcProjectForm').serializeObject();

			$('#_srcProjectList1').jqGrid('setGridParam', {postData:postData}).trigger("reloadGrid");
			$('.srcProject1').show();
			$('.srcProject2').hide();
		});
		
		var accept1 = '';
		<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
			<c:if test="${file eq '12'}">
			accept1 = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
			</c:if>
		</c:forEach>
		
		//파일업로드
		$('#srcCsvFile').uploadFile({
			url:'/project/csvFile',
			multiple:false,
			dragDrop:true,
			fileName:'myfile',
			allowedTypes:accept1,
			sequential:true,
			sequentialCount:1,
			dynamicFormData: function() {
				var data ={ "registFileId" :$('#srcCsvFileId').val(), "tabNm" : "SRC"}

				return data;
			},
			onSuccess:function(files,data,xhr,pd) {
				var result = jQuery.parseJSON(data);
				if(result[1] == null) {
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					$('.ajax-file-upload-statusbar').fadeOut('slow');
					$('.ajax-file-upload-statusbar').remove();
				} else {
					if(result[1].length != 0) {
						$('.sheetSelectPop').show();
						$('.sheetSelectPop .sheetNameArea').children().remove();
						$('.sheetSelectPop .sheetNameArea').text('');
						for(var i = 0; i < result[1].length; i++) {
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
var licenseData;
// SRC 함수
var src_fn = {
	// src 그리드 데이터
	getSrcGridData : function(param){
		$.ajax({
			url : '/selfCheck/ossGrid/${project.prjId}/11',
			dataType : 'json',
			cache : false,
			data : (param) ? param : {referenceId : '${project.prjId}'},
			contentType : 'application/json',
			success : function(data){
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
				
				if(data.validData) {
					srcValidMsgData = data.validData;
					gridValidMsgNew(data.validData, "srcList");
				}
				
				if(data.diffData) {
					gridDiffMsg(data.diffData, "srcList");
				}
				
				fn_grid_com.addEtcKeyDownEvent($('#srcList'), srcValidMsgData);
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
				subTable = $("#srcList_"+arr[i]+"_t");

				if(subTable && subTable.length > 0) {
					subData.push(subTable.jqGrid('getRowData'));
				}
			}
		}
		
		// 최종 데이터
		var finalData = {"prjId" : prjId, "csvFileId" : csvFileId, "csvDelFileIds" : JSON.stringify(fileData), "csvFileSeqs" : JSON.stringify(fileSeq)
				   , "identificationSubStatusSrc" : identificationSubStatusSrc, "mainData" : JSON.stringify(mainData), "subData" : JSON.stringify(subData)};

		// 닉네임 체크
		src_fn.nickNameValid(mainData, subData, finalData);
	},
	// 저장
	exeSave : function(finalData){
		$.ajax({
			url : '/selfCheck/saveSrc',
			type : 'POST',
			data : JSON.stringify(finalData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				_mainLastsel = -1;
				
				if("false" == data.isValid) {
					gridValidMsgNew(data, "srcList");
					alertify.error('<spring:message code="msg.common.valid" />', 0);
				} else {
					if("10" == data.resCd) {
						src_fn.getSrcGridData();
						src_evt.csvDelFileSeq = [];
						src_evt.csvFileSeq = [];
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
	nickNameValid : function(mainData, subData, finalData){
		var prjId = '${project.prjId}';
		var postData = {"mainData" : JSON.stringify(mainData), "subData" : JSON.stringify(subData), "prjId" : prjId};
		
		$.ajax({
			url : '/project/nickNameValid/11',
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
		})
	},
	makeFileTag : function(obj){
		var appendHtml = '<br>'+obj.createdDate;

		$('.csvFileArea').append('<li><span><strong><a href="/download/'+obj.registSeq+'/'+obj.fileName+'">'+obj.originalFilename+'</a>'+appendHtml+'<input type="hidden" value="'+obj.registSeq+'"/><input type="button" value="Delete" class="smallDelete" onclick="src_fn.deleteCsv(this, \'1\')"/></strong></span></li>');
	},
	
	deleteCsv : function(obj, type){
		var Seq = $(obj).prev().val();
		var object = {fileSeq : Seq};
		
		src_evt.csvDelFileSeq.push(object);
		
		$(obj).closest('li').remove();
	},

	// 다운로드 엑셀
	downloadExcel : function(){
		$.ajax({
			type: "POST",
			url: '/exceldownload/getExcelPost',
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
			cleanErrMsg("srcList");
			
			var target = $("#srcList");
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
					subTable = $("#srcList_"+arr[i]+"_t");
					
					if(subTable && subTable.length > 0) {
						subData.push(subTable.jqGrid('getRowData'));
					}
				}
			}
			
			var finalData = {"readType":"src","prjId" : '${project.prjId}', "sheetNums" : sheetNum , "fileSeq" : ""+seq, "mainData" : JSON.stringify(mainData), "subData" : JSON.stringify(subData)};
			var object = {fileSeq : seq};

			src_evt.csvFileSeq.push(object);
			src_fn.exeLoadReportData(finalData);
		}
	},
	// load report data
	exeLoadReportData : function(finalData){
		$.ajax({
			url : '/project/getSheetData',
			type : 'POST',
			data : JSON.stringify(finalData),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				loading.hide();

				if("false" == data.isValid) {
					alertify.error((data.validMsg != "" ? data.validMsg : '<spring:message code="msg.common.valid2" />'), 0);
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
			url : '/project/calcelFileDelSrc',
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
	
	displayNotify : function(cellvalue, options, rowObject){
		var display = "";
		var obligationType = rowObject["obligationType"];
		
		if(obligationType == 10 || obligationType == 11){
			display="<span class=\"iconSet ops\"></span>";
		}
		
		return display;
	},
	displaySource : function(cellvalue, options, rowObject){
		var display = "";
		var obligationType = rowObject["obligationType"];
		
		if(obligationType == 11){
			display="<span class=\"iconSet man\"></span>";
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
	mvEdit : function(){
		createTabInFrame(${project.prjId}+'_Edit', '#/selfCheck/edit/'+${project.prjId});
	},
	// License 상세 페이지 이동
 	showLicenseViewPage : function(gridNm, rowid){
		var target = $("#"+gridNm);
		target.jqGrid('saveRow',rowid);

		var licenseName = target.jqGrid('getCell',rowid,'licenseName');
		target.jqGrid('editRow',rowid);
		
		if(_popup == null || _popup.closed) {
			_popup = window.open("/selfCheck/licensepopup?licenseName="+licenseName, "licenseViewPopup_"+licenseName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");

			if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
				alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
			}
		} else {
			_popup.close();
			_popup = window.open("/selfCheck/licensepopup?licenseName="+licenseName, "licenseViewPopup_"+licenseName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");
		}
		
	},
	// 메인 그리드 OSS 등록/상세 페이지 이동
 	showOssViewPage : function(gridNm, rowid){
 		var target = $("#"+gridNm);
		target.jqGrid('saveRow',rowid);

		var ossName = target.jqGrid('getCell',rowid,'ossName');
		var ossVersion = target.jqGrid('getCell',rowid,'ossVersion');
		target.jqGrid('editRow',rowid);

		if(ossVersion == "N/A") {
			ossVersion = "";
		}
		
		if(ossName!=""){
			var title =  "ossViewPopup_"+ossName;
			
			$.ajax({
				url : '/oss/checkExistsOssByname',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : {ossName : ossName},
				contentType : 'application/json',
				success : function(data){
					if(data.isValid == 'true') {
						if(_popup == null || _popup.closed) {
							_popup = window.open("/oss/osspopup?ossName="+ossName+"&ossVersion="+ossVersion, title, "width=900, height=700, toolbar=no, location=no, left=100, top=100");

							if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
								alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
							}
						} else {
							_popup.close();
							_popup = window.open("/oss/osspopup?ossName="+ossName+"&ossVersion="+ossVersion, title, "width=900, height=700, toolbar=no, location=no, left=100, top=100");
						}
					}
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	},
	displayGuide : function(cellvalue, options, rowObject){
		var licenseName = rowObject["licenseName"];
		var display = "";
		var cnt = 0;

		if(licenseName === undefined || licenseName == "" || licenseName == null) {
			
		} else {
			var names = licenseName.split(',');
			
			if(names.length > 1){
				for(var i=0; i < names.length; i++){
					for(var j=0; j < guideData.length; j++){
						if(guideData[j].cdDtlNm == names[i]){
							cnt++;
						}
					}
				}
			} else {
				cnt = 1;
			}
		}

		if(cnt > 0){
			display = "<div class=\"tcenter\"><a class='btnIcon tips' onclick=\"src_fn.popGuide('"+licenseName+"')\">Tips</a></div>";
		} else {
			display = "";
		}
		
		return display;
	},
	
	popGuide : function(licenseName){
		var displayHtml = "";

		if(licenseName != ""){
			var names = licenseName.split(',');
			
			if(names.length > 1) {
				for(var i=0; i < names.length; i++) {
					displayHtml += names[i]+'<br/><br/>';
					displayHtml += src_fn.getGuideData(names[i])+'<br/><br/>';
				}
			} else {
				displayHtml = names+'<br/><br/>';
				displayHtml += src_fn.getGuideData(names)+'<br/>';
			}
		}
		
		alertify.alert(displayHtml, function(){});
	},
	
	getGuideData : function(licenseName){
		var getData = "";
		
		for(var i=0; i < guideData.length; i++){
			if(guideData[i].cdDtlNm == licenseName){
				getData = guideData[i].cdDtlExp;
				break;
			}
		}

		return getData.replace(/\n/g,'<br>');
	},
	
	displayOssDetail : function(cellvalue, options, rowObject){
		var display = "";
		var licenseName = rowObject["licenseName"];
		
		if(licenseName === undefined || licenseName == "" || licenseName == null) {
				
		} else {
			if(options.rowId != "") {
					display = "<div class=\"tcenter\"><a class='btnIcon ossI' onclick=\"src_fn.showOssViewPage('srcList',"+options.rowId+")\">I</a></div>";
			} else {
				display = "";
			}
		}
		
		return display;
	},
	
	displayLicenseDetail : function(cellvalue, options, rowObject){
		var display = "";
		var ossName = rowObject["ossName"];

		if(ossName === undefined || ossName == "" || ossName == null){
			
		} else {
			if(options.rowId != ""){
				display = "<div class=\"tcenter\"><a class='btnIcon licenseI' onclick=\"src_fn.showLicenseViewPage('srcList',"+options.rowId+")\">I</a></div>";
			} else {
				display = "";
			}
		}
		
		return display;
	},
	
	// vulnerability 
	displayVulnerability : function(cellvalue, options, rowObject){
		var display = "";
		var vulnerability = rowObject["vulnerability"];
		
		if( vulnerability == 'Y'){
			display="<span class=\"iconSet vulHigh\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
		}
		
		return display;
	}
}

//SRC 그리드
var src_grid = {
	load: function(){
		var currentOssName = "";
		var ondblClickRowBln = false;
		var srcList = $("#srcList");
		
		srcList.jqGrid({
			datatype: 'local',
			data : srcMainData,
			colNames: ['gridId', 'ID', 'ReferenceId', 'ReferenceDiv', 'OssId', 'OSS Name','OSS Version','License'
			           ,'LicenseId','OSS Detail','License Detail','User<br/>Guide','CVE ID'
			           ,'Vulnera<br/>bility','ObligationType','Notify','Source','Exclude','LicenseDiv'],
			colModel: [
				{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
				{name: 'componentId', index: 'componentId', width: 40, align: 'center'},
				{name: 'referenceId', index: 'referenceId', width: 29, align: 'center', hidden:true},
				{name: 'referenceDiv', index: 'referenceDiv', width: 29, align: 'center', hidden:true},
				{name: 'ossId', index: 'ossId', width: 29, align: 'center', editable:true, hidden:true},
				{name: 'ossName', index: 'ossName', width: 150, align: 'left', editable:true, edittype:'text', 
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
											$("#srcList").focus();
											fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData);
										}
									});
									currentOssName = e.value;
								}
						}
				},
				{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left', editable: true, edittype: 'text',
					editoptions: {
						dataInit:
							function (e) { 
								fn_grid_com.griOssVersions(e, currentOssName, 'srcList');
								$(e).on( "autocompletechange", function() {
									var rowid = (e.id).split('_')[0];
									fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData);
								});
							}
					}
				},
 				{name: 'licenseName', index: 'licenseName', width: 150, align: 'left', editable:false, edittype:'text', 
 					editoptions: {
 						dataInit: function (e) {
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
									
									for(var i in objs){
										if("" != e.value && e.value == objs[i].licenseName){
											$('#'+rowid+'_licenseId').val(objs[i].licenseId);
											$('#'+rowid+'_licenseText').val(objs[i].licenseText);
										}
									}
									
									fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData);
								});
							}
 					}
 				},
				{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', editable:true, edittype:'text', hidden:true},
				{name: 'ossDetail', index: 'ossDetail', width: 50, align: 'center', formatter:src_fn.displayOssDetail, sortable : false},
				{name: 'licenseDetail', index: 'licenseDetail', width: 50, align: 'center', formatter:src_fn.displayLicenseDetail, sortable : false},
				{name: 'userGuide', index: 'userGuide', width: 50, align: 'center', formatter:src_fn.displayGuide, unformatter:fn_grid_com.unformatter, sortable : false},
				{name: 'cveId', index: 'cveId', hidden:true},
				{name: 'vulnerability', index: 'vulnerability', width: 50, align: 'center', formatter:src_fn.displayVulnerability, unformatter:fn_grid_com.unformatter, sortable : false},
				{name: 'obligationType', index: 'obligation', width: 40, align: 'center', hidden:true},
				{name: 'notify', index: 'notify', width: 40, align: 'center', formatter: src_fn.displayNotify, unformat: src_fn.unDisplayNotify},
				{name: 'source', index: 'source', width: 40, align: 'center', formatter: src_fn.displaySource, unformat: src_fn.unDisplaySource},
				{name: 'excludeYn', index: 'excludeYn', width: 50, align: 'center', formatter: fn_grid_com.cboxFormatter, unformat: fn_grid_com.cboxUnFormatter,
 					editoptions: {
						dataInit:
							function (e) { 
								$(e).on("change", function() {
									var rowid = (e.id).split('_')[0];
									
									fn_grid_com.saveCellData("srcList",rowid,e.name,e.value,srcValidMsgData);
								});
							}
					}	
				},
				{name: 'licenseDiv', index: 'licenseDiv', width: 100, align: 'left', editable:false, hidden:true}
			],
			autoencode: true,
			editurl:'clientArray',
 			autowidth: true,
			height: 'auto',
			gridview: true,
		   	pager: '#srcPager',
		   	pgbuttons: false,
		   	pgtext: false,
		   	pginput:false,
		   	rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
			loadonce:true,			
			cellEdit : true,
			cellsubmit : 'clientArray',
			ignoreCase: true,
		    onSortCol: function (index, columnIndex, sortOrder) {
		    	isSort = true;
		    },
			subGrid: true,
			subGridOptions:{
				reloadOnExpand : false
			},
			subGridRowExpanded: function(subgrid_id, row_id) {
				var subgrid_table_id;
				var pager_id;
				subgrid_table_id = subgrid_id+"_t";
				pager_id = "p_"+subgrid_table_id;
				
				$("#"+subgrid_id).html("<table id='"+subgrid_table_id+"' class='scroll'></table><div id='"+pager_id+"' class='scroll'></div>");

				$("#"+subgrid_table_id).jqGrid({
					datatype: 'local',
					data : srcSubData[row_id],
					colNames: ['gridId', 'ComponentLicenseId','ComponentId','OssLicenseComb','LicenseId','License','License Text','Copyright Text','Exclude','Editable'],
					colModel: [
						{name: 'gridId', index: 'gridId', editable:false, hidden:true, key:true},
						{name: 'componentLicenseId', index: 'componentLicenseId', width: 70, align: 'center', hidden:true},
						{name: 'componentId', index: 'componentId', width: 50, align: 'center', hidden:true},
						{name: 'ossLicenseComb', index: 'ossLicenseComb', width: 100, align: 'center', hidden:true},
						{name: 'licenseId', index: 'licenseId', width: 50, align: 'center', editable:true, edittype:'text', hidden:true},
		 				{name: 'licenseName', index: 'licenseName', width: 200, align: 'left', editable:true, edittype:'text' , 
		 					editoptions: {
		 						dataInit: function (e) {
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
	 									
	 									for(var i in objs){
	 										if("" != e.value && e.value == objs[i].licenseName){
	 											$('#'+rowid+'_licenseId').val(objs[i].licenseId);
	 											$('#'+rowid+'_licenseText').val(objs[i].licenseText);
	 										}
	 									}
	 								});
	 							}
		 					}
		 				},
						{name: 'licenseText', index: 'licenseText', width: 200, align: 'left', editable:true, edittype:"textarea", editoptions:{rows:"2",cols:"24"}},
						{name: 'copyrightText', index: 'copyrightText', width: 200, align: 'left', editable:true, edittype:"textarea", editoptions:{rows:"2",cols:"24"}},
						{name: 'excludeYn', index: 'excludeYn', width: 50, align: 'center', formatter: fn_grid_com.cboxSubFormatter, unformat: fn_grid_com.cboxSubUnFormatter},
						{name: 'editable', index: 'editable', width: 50, align: 'center', editable:false, hidden:true}
					],
					height: '100%',
				    editurl:'clientArray',
				   	pager: pager_id,
				   	pgbuttons: false,
				   	pgtext: false,
				   	pginput:false,
				   	rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
				   	loadonce:true,
				   	loadComplete: function(data) {
						// 서브 그리드 배경색 설정
						fn_grid_com.makeBackGroundColor($("#"+subgrid_table_id));
						
						// exclude에 따른 음영설정
						fn_grid_com.checkExclude(data, subgrid_table_id);
					},
					onSelectRow: function(rowid,status,eventObject) {},
					beforeSelectRow: function(rowid, e) {
						return true;
					},
					onCellSelect: function(rowid,iCol,cellcontent,e) {
						// 서브 그리드 수정 모드(등록되지 않은 라이센스만 수정)
						if($("#"+subgrid_table_id).jqGrid('getCell',rowid,'editable')=="Y"){
							cleanErrMsg("srcList", rowid);

							$("#"+subgrid_table_id).jqGrid('editRow',rowid);
						}
					},
					ondblClickRow: function(rowid,iRow,iCol,e) {
						ondblClickRowBln = true; // 서브그리드 제외
					}
				});
				$("#"+subgrid_table_id).jqGrid('navGrid',"#"+pager_id,{add:true,edit:false,del:true,search:false,refresh:false
																	 , addfunc: function () { fn_grid_com.rowAdd('srcList',$("#"+subgrid_table_id),"sub", row_id);}
																	 , delfunc: function () { fn_grid_com.rowDel($("#"+subgrid_table_id),"sub");}
				});
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
					$("#srcList tr.singleLicenseClass").find("td:first").removeClass("sgexpanded sgcollapsed").find("a").hide();
				}
				
				//sort후 ValidMsgData reload
				if(isSort){
					if(srcValidMsgData != null){
						gridValidMsgNew(srcValidMsgData, "srcList");
					}
					
					isSort = false;
				}
			},
			beforeSelectRow: function(rowid, e) {
				// 경고 클래스 설정
				fn_grid_com.setWarningClass(srcList,rowid,["ossName","licenseName"]);
				return true;
			},
			onCellSelect: function(rowid,iCol,cellcontent,e) {
				cleanErrMsg("srcList", rowid);
				fn_grid_com.setCellEdit(srcList, rowid, srcValidMsgData);
			},
			ondblClickRow: function(rowid,iRow,iCol,e) {
				// 체크 박스 영역 제외
				if(iCol=="7") {
					fn_grid_com.mvOssPage(srcList, rowid);
				}
				
				// 서브 그리드 제외
				ondblClickRowBln = false;
			}
		});
		srcList.jqGrid('navGrid',"#srcPager",{add:true,edit:false,del:true,search:false,refresh:false
												  , addfunc: function () { fn_grid_com.rowAdd('srcList',srcList,"main");fn_grid_com.addEtcKeyDownEvent($('#srcList'), srcValidMsgData);}
												  , delfunc: function () { fn_grid_com.rowDel(srcList,"main");}
		});
	}
}
</script>