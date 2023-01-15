<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
var ossNames = [];
var objs = [];
var licenseNames = [];
var isAdmin = 'true' == '${ct:isAdmin()}';
var totalRow = 0;
const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
var selectedRow = new Array();
var multiSelectFlag = false;
var confirmFlag = false;
$(document).ready(function () {
		'use strict';
		
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		batList.load();
		showHelpLink("Binary_DB_Main");
	});
//이벤트
var evt = {
		// ready event
		init: function(){
			$('#btnSaveBat').click(function(){
				fn.saveBat();
			});
			
			$('#search').click(function(){
				var postData=$('#batSearch').serializeObject();

				$("#list").jqGrid('setGridParam', {postData:postData, page : 1, url:'<c:url value="/binary/listAjax"/>'}).trigger('reloadGrid');
			});
			
			// ossNames auto complete
			fn.griOssNames().success(function(data, status, headers, config){
				if(data != null){
					data.forEach(function(obj){
						ossNames.push(obj.ossName);
					})
				}
			});
			
			$("#batSearch").on("keypress", function(e){
				if(e.keyCode == 13){
					$('#search').trigger("click");
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
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
			
		}
};

//함수
var fn = {
	// 수정된 마지막 row 저장
	saveLastRow: function(){	
		$("#list").jqGrid("editCell", 0, 0, false);		// $("#list").jqGrid("editCell", row, col, false); 반드시 edit focus를 해제시킨다. 해제 시키지 않을 경우 값이 <input ....>으로 저장된다.
		$("#list").jqGrid("saveRow", batList.lastEditRowId);
		
		// 수정된 rowid 저장
		if(batList.modifyRowId.length == 0) {
			batList.modifyRowId.push(batList.lastEditRowId);
		} else {
			batList.modifyRowId.forEach(function() {		// 중복제외
				if(batList.modifyRowId.indexOf(batList.lastEditRowId) == -1) {
					batList.modifyRowId.push(batList.lastEditRowId);								
				}
			});
		}
	},
	// 저장된 rowid로 rowdata 가져와서 저장
	saveBat: function(){
		var modifyRowList = [];	// 수정된 LIST
		var modifyRow = {};
		
		fn.saveLastRow();
		
		batList.modifyRowId.forEach(function(val){
			if(val){
				modifyRowList.push($("#list").jqGrid("getRowData",val));
			}
		});
		
		// {newRowList: [], modifyRowList: []} 형식으로 변환
		modifyRow['modifyRowList'] = modifyRowList;
		
		var data = JSON.stringify(modifyRowList);
		ajax.setBat(data);
	},
	// Grid rowid의 max값
	getMaxIdNo: function(){
		var ids = [];
		ids = $("#list").jqGrid('getDataIDs');
		
		return (ids.length == 0) ? 0 : Math.max.apply(Math, ids);
	},
	// oss_name 가져오기
	griOssNames : function(data){
		return $.ajax({
			type: 'GET',
			url: '<c:url value="/project/getOssNames"/>',
			data: data,
			headers: {
				'Content-Type': 'application/json'
			}
		});
	},
	// oss_version 가져오기
	griOssVersions : function(e, ossName, target){
		var ossVersions = [];
		
		if(ossName=="") {
			return false;
		}
		
		return $.ajax({
			type: 'GET',
			url: '<c:url value="/project/getOssVersions"/>',
			data: {ossName : ossName },
			headers: {
				'Content-Type': 'application/json'
			},
			success: function(data, status, headers, config){
 				if(data != null){
					data.forEach(function(obj){
						ossVersions.push(obj.ossVersion);
					});
						
					$(e).autocomplete({
						source: ossVersions
						, minLength: 0
						, open: function() { $(this).attr('state', 'open');}
						, close: function () { $(this).attr('state', 'closed');}
					}).focus(function() {
						if ($(this).attr('state') != 'open') {
							$(this).autocomplete("search");
						}
					}).on('autocompletechange', function() {
						var rowid = (e.id).split('_')[0];
					});
					
 					ossVersions = [];
 					
 					$(e).focus();
				}
			}
		});
	},
	downloadExcel : function(){
		if(isMaximumRowCheck(totalRow)){
			var filters = $("#list").getGridParam("postData").filters;
			$("#filters").val(filters);
			var data = $('#batSearch').serializeObject();
			
			$.ajax({
				type: "POST",
				url: '<c:url value="${suffixUrl}/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"binaryDB", "parameter":JSON.stringify(data)}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						if(data.validMsg == "overflow"){
							alertify.error(getMsgMaxRowCnt(), 0);
						}else{
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					} else {
						window.location =  '<c:url value="/exceldownload/getFile?id='+data.validMsg+'"/>';
					}
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	},
	getSelectedRowCnt : function(){
    	selectedRow = new Array();
    	selectedRow = $('#list').jqGrid('getGridParam', 'selarrrow');
    	multiSelectFlag = selectedRow.length > 1 ? true : false;
	}
};

var ajax = {
	//코드 저장
	setBat: function(param) {
		$.ajax({
			type: 'POST',
			url: '<c:url value="/binary/modAjax"/>',
			data: param,
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json' 
			},
		}).success(function (data, status, headers, config) {
			alertify.success('<spring:message code="msg.common.success" />');
			$("#list").jqGrid().trigger('reloadGrid');
		}).error(function (data, status, headers, config) {
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
		});
	}
};


var updateDialogEdit = {
        url:'<c:url value="/binary/modAjax"/>'
            , closeAfterEdit: true
            , reloadAfterSubmit: true
            , modal: true
		    , serializeEditData : function(postdata) {
			    if(multiSelectFlag){
				    var postdataArr = new Array();
				    
			    	for( var i in selectedRow) {				    	
			    		var obj = $('#list').getRowData(selectedRow[i]);
			    		var editRowId = $("#FrmGrid_list  #filename").attr("rowid");
			    		var editRowData = $("#list").getRowData(editRowId);

			    		if(postdata["ossName"] != editRowData["ossName"]){
			    			obj["ossName"] 	= postdata["ossName"];
			    		}
			    		
			    		if(postdata["ossVersion"] != editRowData["ossVersion"]){
			    			obj["ossVersion"] = postdata["ossVersion"];
			    		}
			    		
			    		if(postdata["license"] != editRowData["license"]){
			    			obj["license"] 	= postdata["license"];
			    		}
			    		
			    		if(postdata["comment"] != editRowData["comment"]){
			    			obj["comment"] 	= postdata["comment"];
			    		}
			    		
			    		obj["updateDate"]	= postdata["updateDate"];
			    		obj["oper"]			= postdata["oper"];
			    		// id => concat( filename , '-' , checksum, '-', COALESCE(ossname,''), '-', COALESCE(ossversion,''), '-', COALESCE(license,'') )
						obj["id"]			= selectedRow[i];
						
			    		postdataArr.push(obj);
			    	}

			    	return {"parameter":JSON.stringify(postdataArr)}; 
			    } else {
		        	return postdata;
			    }
		    }
            , beforeShowForm: function(formid) {
                fn.getSelectedRowCnt();
                
                if(multiSelectFlag){
					$("#pathName", formid).attr("disabled","disabled");
					$("#sourcePath", formid).attr("disabled","disabled");
					$("#parentName", formid).attr("disabled","disabled");
					$("#platformName", formid).attr("disabled","disabled");
					$("#platformVersion", formid).attr("disabled","disabled");
                }
                
            	$("#fileName", formid).attr("disabled","disabled");
            	$("#checkSum", formid).attr("disabled","disabled");
            	$("#tlshCheckSum", formid).attr("disabled","disabled");
            	$("#downloadlocation", formid).attr("disabled","disabled");
            }
            , afterSubmit: function(response, postdata) {
                if("false" == response.responseJSON.isValid) {
                    alertify.error('<spring:message code="msg.common.valid" />', 0);
                    createValidMsg('FrmGrid_list', response.responseJSON);

                    return [false, '<spring:message code="msg.common.valid" />', ""]
                } else {
                    alertify.success('<spring:message code="msg.common.success" />');

                    return [true,"",""]
                }
            }
            , beforeSubmit: function(postdata, formid) {
                $("span.retxt", formid).remove();
                
				if(multiSelectFlag && !confirmFlag){
					var result = "";
					
	                alertify.confirm("More than one binary information will be modified.<br>Would you like to continue?"
	                , function(){
	                	confirmFlag = true;
		                $("#sData").trigger("click");           	
		                result = [true];
	                }
	                , function(){
	                    $("#cData").trigger("click");
	                    result = [false];
	                });

	                return result;
				} else {
                	return [true];
				}
            }
};

var updateDialogDel = {
        url:'<c:url value="/binary/modAjax"/>'
            , closeAfterDel: true
            , reloadAfterSubmit: true
            , modal: true
            , onclickSubmit: function(params, rowid) {
                var ajaxData = {};
                var list = $("#list");
                var selectedRow = list.jqGrid("getGridParam", "selarrrow");
                //ajaxData = {ids: selectedRow};
                return ajaxData;
            }
		    , afterSubmit: function(response, postdata) {
                if("false" == response.responseJSON.isValid) {
                    alertify.error('<spring:message code="msg.common.valid" />', 0);

                    return [false, '<spring:message code="msg.common.valid" />', ""]
                } else {
                    alertify.success('<spring:message code="msg.common.success" />');
                    $("#list").jqGrid().trigger('reloadGrid');

                    return [true,"",""]
                }
		    }
};

//유저 그리드
var batList = {
	modifyRowId: [],		//수정된 모든 rowID
	lastEditRowId: '',		//마지막 수정된 rowId 저장 변수
	lastIdNo: '',			//서버에서 가져온 마지막 ID
	load: function(){
		batList.modifyRowId = [];
		batList.lastEditRowId = "";
		
		//그리드 생성
		$('#list').jqGrid({
			  type:"GET"
			, datatype: 'json'
			, jsonReader:{
				repeatitems: false,
				id:'batId',
				root:function(obj){return obj.rows;},
				page:function(obj){return obj.page;},
				total:function(obj){return obj.total;},
				records:function(obj){return obj.records;}
			}
			, colNames: ['batId','Binary File name','Binary location', 'Source path (Android models only)', 'OSS Name', 'OSS Version', 'License', 'Download Location', 'Project Name', 'Platform Name', 'Platform Version', 'Checksum', 'Tlsh', 'Update Date', 'Comment']
			, colModel: [
				{name: 'batId', index: 'batId', hidden:true, key:true},
				{name: 'fileName', index: 'fileName', width: 150, allign: 'left',editable: true, template: searchStringOptions},
				{name: 'pathName', index: 'pathName', width: 150, allign: 'left',editable: true, template: searchStringOptions},
				{name: 'sourcePath', index: 'sourcePath', width: 150, allign: 'left',editable: true, template: searchStringOptions},
				{name: 'ossName', index: 'ossName', width: 150, allign: 'left',editable: true, template: searchStringOptions,
					editoptions: {
						dataInit:
							function (e) { 
								// ossName auto complete
								$(e).autocomplete({
									source: ossNames
									, minLength: 3
									, open: function() { $(this).attr('state', 'open');}
									, close: function () { $(this).attr('state', 'closed');}
								}).focus(function() {
									if ($(this).attr('state') != 'open') {
										$(this).autocomplete("search");
									}
								}).on('autocompletechange', function() {
									if(e.value!=""){
										var rowid = (e.id).split('_')[0];
										fn.griOssVersions($('#ossVersion'), e.value, 'list');
									}
								});
								
								currentOssName = e.value;
							}
					}	
				} ,
				{name: 'ossVersion', index: 'ossVersion', width: 80, align: 'left',editable: true, template: searchStringOptions},
				{name: 'license', index: 'license', width: 150, align: 'left',editable: true, template: searchStringOptions,
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
								$(e).on( "autocompletechange", function() {});
							}
 					}
				},
				{name: 'downloadlocation', index: 'downloadlocation', width: 100, align: 'left', formatter: 'link', formatoptions: {target:'_blank'}, editable: true, template: searchStringOptions},
				{name: 'parentName', index: 'parentName', width: 100, align: 'left',editable: true, template: searchStringOptions},
				{name: 'platformName', index: 'platformName', width: 100, align: 'left',editable: true, template: searchStringOptions},
				{name: 'platformVersion', index: 'platformVersion', width: 100, align: 'left',editable: true, template: searchStringOptions},
				{name: 'checkSum', index: 'checkSum', width: 150, allign: 'center',editable: true,hidden:true},
				{name: 'tlshCheckSum', index: 'tlshCheckSum', width: 150, allign: 'center',editable: true,hidden:true},
				{name: 'updateDate', index: 'updateDate', width: 100, align: 'left',editable: false, formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, template: searchDateOptions},
				{name: 'comment', index: 'comment', width: 150,editable: true,hidden:true, edittype:'textarea', editoptions:{maxlength:2048, rows:5}, editrules: { edithidden: true }, template: searchStringOptions}
			]
			, editurl :'clientArray'
			, rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")}
		<c:if test="${ct:isAdmin() eq true}">
			, rowList:	[${ct:getConstDef('DISP_BINARYDB_PAGENATION_LIST_STR')}]
		</c:if>	
		<c:if test="${ct:isAdmin() ne true}">
			, rowList:	[${ct:getConstDef('DISP_PAGENATION_LIST_STR')}]
		</c:if>
			, autowidth: true
			, pager: '#pager'
			, gridview: true
			, sortable: function (permutation) {}
			, sortname: 'fileName'
			, viewrecords: true
			, sortorder: 'asc'
			, loadonce: false
			, height: 'auto'
			, multiselect: true
			, onSelectRow: function(rowid,iRow,iCol,e){
				// 마지막 수정된 row 저장
				if(batList.lastEditRowId && rowid != batList.lastEditRowId){
					fn.saveLastRow();
				}
				
				batList.lastEditRowId = rowid;
			}
			, loadComplete:function(data) {
				totalRow = data.records;
				// id(key값)의 max값을 lastIdNo에 설정
				batList.lastIdNo = fn.getMaxIdNo();
				
				confirmFlag = false;
			}, ondblClickRow: function(rowid,iRow,iCol,e) {}
		});
		
		$("#list").jqGrid('navGrid',"#pager",{edit:isAdmin,add:false,del:isAdmin,search:false,refresh:false},updateDialogEdit, null, updateDialogDel);
		$("#list").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
	}
}
</script>