<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	$(document).ready(function () {
		'use strict';
		
		processGuideList.load();
		
		$("#add_list").hide();
		$("#del_list").hide();
	});	
	
	// 함수
	var fn = {
		// Grid rowid의 max값
		getMaxIdNo: function(grid){
			var ids = [];
			ids = $("#list").jqGrid('getDataIDs');
			
			return (ids.length == 0) ? 0 : Math.max.apply(Math, ids);
		},
		// Grid row 추가
		addRow: function(grid){
			var newIdNo = fn.getMaxIdNo(grid) + 1;
			
			$("#list").jqGrid("addRowData", newIdNo, {cdNo: newIdNo}, "first");	
			
		},
		// 수정된 마지막 row 저장
		saveLastRow: function(grid){
			$("#list").jqGrid("editCell", 0, 0, false);		// $("#list").jqGrid("editCell", row, col, false); 반드시 edit focus를 해제시킨다. 해제 시키지 않을 경우 값이 <input ....>으로 저장된다.
			$("#list").jqGrid("saveRow", processGuideList.lastEditRowId);
			$("#list").jqGrid("setRowData", processGuideList.lastEditRowId, false, { background : "#feffc4" });	// 수정된 row 색 변경

			// 수정된 rowid 저장
			if(processGuideList.modifyRowId.length == 0) {
				processGuideList.modifyRowId.push(processGuideList.lastEditRowId);
			} else {
				processGuideList.modifyRowId.forEach(function(){		// 중복제외
					if(processGuideList.modifyRowId.indexOf(processGuideList.lastEditRowId) == -1) {
						processGuideList.modifyRowId.push(processGuideList.lastEditRowId);
					}
				});
			}
		}
	};
	
	var updateDialogEdit = {
            url:'/system/processGuide/saveAjax'
                , closeAfterEdit: true
                , reloadAfterSubmit: true
                , modal: true
			    , serializeDelData: function(postdata) {
			        return JSON.stringify(postdata);
			    }
			    , beforeShowForm: function(formid) {
			        var appendHtml = '<div id="contentsEdit" style="width:600px; height:255px;">'+$("#contents", formid).val()+'</div>';
        			$("#tr_contents", formid).show();
    				$("#contents", formid).hide();
			        $("#tr_contents", formid).children().eq(1).append(appendHtml);

			        if(CKEDITOR.instances.contentsEdit) {
			    		var _editor = CKEDITOR.instances.contentsEdit;
			    		if(_editor) {
    						_editor.destroy();
    					}
			    	}
					
					CKEDITOR.replace('contentsEdit', {});
			    }
			    , onClose: function() {
			    	if(CKEDITOR.instances.contentsEdit) {
			    		var _editor = CKEDITOR.instances.contentsEdit;
			    		if(_editor) {
    						_editor.destroy();
    					}
			    	}

			    	$("#tr_contents", "list").children().eq(1).remove();
			    }
                , afterSubmit: function(response, postdata) {
                	if(CKEDITOR.instances.contentsEdit) {
                		var _editor = CKEDITOR.instances.contentsEdit;
    					if(_editor) {
    						_editor.destroy();
    					}
                	}

                	if("false" == response.responseJSON.isValid) {
                        alertify.error('<spring:message code="msg.common.valid" />', 0);
                        createValidMsg('FrmGrid_list', response.responseJSON);
                        
                        return [false, '<spring:message code="msg.common.valid" />', ""]
                    } else {
                        alertify.success('<spring:message code="msg.common.success" />');
                        
                        return [true,"",""]
                    }

                	processGuideList.load();
                }
                , beforeSubmit: function(postdata, formid) {
                	postdata.contents = CKEDITOR.instances.contentsEdit.getData();
                    $("span.retxt", formid).remove();

                    return [true];
                }
                ,width: "600"
                ,viewPagerButtons: false
	};
	

	var updateDialogAdd = {};
	var updateDialogDel = {};
	
	var processGuideList = {
		modifyRowId: [],		// 수정된 모든 rowid
		deleteRowList: [],		// 삭제할 LIST
		lastEditRowId: "",		// 마지막 수정된 rowid 저장 변수
		lastIdNo: "",			// 서버에서 가져온 마지막 id
		load: function(){
			processGuideList.modifyRowId = [];
			processGuideList.deleteRowList = [];
			processGuideList.lastEditRowId = "";
			
			$("#list").jqGrid({
				url: '/system/processGuide/listAjax'
				, datatype: 'json'
				, jsonReader: {
					repeatitems: false,
					id:'id',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				}
				, colNames: ['ID', 'Page Target','Contents', 'URL', 'useYn']
				, colModel: [
                    {name: 'id', index: 'id', width: 150, align: 'left', editable:false},
					{name: 'pageTarget', index: 'pageTarget', width: 70, align: 'left', editable:false},
                    {name: 'contents', index: 'contents',  width: 350, editable:true, hidden:false},
                    {name: 'url', index: 'url', width: 250, editable:true, editrules : {required: false}},
					{name: 'useYn', index: 'useYn', width: 50, align: 'center', editable:true, edittype:"checkbox", editoptions: {value: "Y:N:N"}}
				]
				, emptyrecords: "Nothing to display"
				, autoencode: false
				, editurl: 'clientArray'
				, rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")}
				, rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}]
  				, autowidth: true
				, pager: '#pager'
				, gridview: true
				, sortable: function (permutation) {}
				, sortname: 'id'
				, sortorder: 'desc'
				, viewrecords: true
				, height: 'auto'
				, loadonce: false
				// 데이터 로딩 후
				, loadComplete: function(data) {}
			});
			
            $("#list").jqGrid('navGrid', "#pager", {edit:true, search:false, refresh:false}, updateDialogEdit);
		}		
	};

</script>