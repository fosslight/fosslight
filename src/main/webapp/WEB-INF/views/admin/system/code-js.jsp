<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	$(document).ready(function () {
		'use strict';
		
		evt.init();
		
		codeList.load();
	});
	
	
	// 이벤트
	var evt = {
		// ready event
		init: function(){
			$('#btnSaveCode').click(function(){
				$("#list2").jqGrid('GridUnload');
				$("#list2").hide();
				
				fn.saveCode('code');
			});
			
			$('#btnSaveCodeDetail').click(function(){
				fn.saveCode('codeDtl');
			});
			
			$('#search').click(function(){
				var postData=$('#codeSearch').serializeObject();
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
		}
	};
	
	// 함수
	var fn = {
		// Grid rowid의 max값
		getMaxIdNo: function(grid){
			var ids = [];
			
			if(grid == 'code') { 
				ids = $("#list").jqGrid('getDataIDs');
			} else {
				ids = $("#list2").jqGrid('getDataIDs');
			}
			
			return (ids.length == 0) ? 0 : Math.max.apply(Math, ids);
		},
		
		// Grid row 추가
		addRow: function(grid){
			var newIdNo = fn.getMaxIdNo(grid) + 1;
			
			if(grid == 'code') {
				$("#list").jqGrid("addRowData", newIdNo, {cdNo: newIdNo}, "first");	
			} else {
				var codeNo = codeDetailList.nowCodeNo;	// 신규추가 시에는 코드번호가 없으므로 직접 설정
				var _tempRandId = $.jgrid.randId();

				$("#list2").jqGrid("addRowData", _tempRandId, {cdNo: codeNo, gridId: _tempRandId}, "last");
                   $("#list2").jqGrid('editRow',_tempRandId,true,function(){
                });
			}
		},
		// Grid row 삭제
		delRow: function(gid,delRowId){
			// 삭제할 rowid 저장
			if(gid == 'list'){
				$("#list2").jqGrid('GridUnload');
				$("#list2").hide();
				
				// 기존 row인 경우만 deleteRowList에 추가
				if(delRowId <= codeList.lastIdNo) {
					codeList.deleteRowList.push($("#list").jqGrid("getRowData",delRowId));
				}
				
				// 삭제할 rowid가 추가/수정 목록에 있을 경우 삭제
				var idx = codeList.modifyRowId.indexOf(delRowId);
				
				if(idx != -1) {
					codeList.modifyRowId.splice(idx, 1);
				}
				
				$("#list").jqGrid('delRowData',delRowId);
			} else {
				$("#list2").jqGrid('delRowData',delRowId,{editurl: 'clientArray'});
			}
		},
		// 수정된 마지막 row 저장
		saveLastRow: function(grid){
			if(grid == 'code'){
				$("#list").jqGrid("editCell", 0, 0, false);		// $("#list").jqGrid("editCell", row, col, false); 반드시 edit focus를 해제시킨다. 해제 시키지 않을 경우 값이 <input ....>으로 저장된다.
				$("#list").jqGrid("saveRow", codeList.lastEditRowId);
				$("#list").jqGrid("setRowData", codeList.lastEditRowId, false, { background : "#feffc4" });	// 수정된 row 색 변경

				// 수정된 rowid 저장
				if(codeList.modifyRowId.length == 0) {
					codeList.modifyRowId.push(codeList.lastEditRowId);
				} else {
					codeList.modifyRowId.forEach(function(){		// 중복제외
						if(codeList.modifyRowId.indexOf(codeList.lastEditRowId) == -1) {
							codeList.modifyRowId.push(codeList.lastEditRowId);
						}
					});
				}
			} else {
				$("#list2").jqGrid("editCell", 0, 0, false);		// $("#list").jqGrid("editCell", row, col, false); 반드시 edit focus를 해제시킨다. 해제 시키지 않을 경우 값이 <input ....>으로 저장된다.
				$("#list2").jqGrid("saveRow", codeDetailList.lastEditRowId);
				$("#list2").jqGrid("setRowData", codeDetailList.lastEditRowId, false, { background : "#feffc4" });	// 수정된 row 색 변경

				// 수정된 rowid 저장
				if(codeDetailList.modifyRowId.length == 0) {
					codeDetailList.modifyRowId.push(codeDetailList.lastEditRowId);
				} else {
					codeDetailList.modifyRowId.forEach(function() {		// 중복제외
						if(codeDetailList.modifyRowId.indexOf(codeDetailList.lastEditRowId) == -1) {
							codeDetailList.modifyRowId.push(codeDetailList.lastEditRowId);
						}
					});
				}
			}
		},
		
		// 저장된 rowid로 rowdata 가져와서 저장
		saveCode: function(grid){
			var modifyRowList = [];	// 수정된 LIST
			var newRowList = [];	// 추가된 LIST
			
			var newRow = {};
			var modifyRow = {};
			var deleteRow = {};
			var sendRowData = {};
			
			if(grid == 'code'){
				fn.saveLastRow('code');
				
				codeList.modifyRowId.forEach(function(val){
					if(val){
						if(val > codeList.lastIdNo) {
							newRowList.push($("#list").jqGrid("getRowData",val));
						} else {
							modifyRowList.push($("#list").jqGrid("getRowData",val));
						}
					}
				});
				
				// {newRowList: [], modifyRowList: []} 형식으로 변환
				newRow['newRowList'] = newRowList;
				modifyRow['modifyRowList'] = modifyRowList;
				deleteRow['deleteRowList'] = codeList.deleteRowList;

				var data = JSON.stringify($.extend(newRow,modifyRow,deleteRow));
				
				ajax.setCode(data);
			} else {
				var dataToSend = [];
				var grid = $('#list2');
				var rows = grid.jqGrid('getDataIDs');
				
				for(idx=0; idx < rows.length; idx++) {
					grid.jqGrid('saveRow', rows[idx]);
					var rowData = grid.jqGrid('getRowData', rows[idx]);
					dataToSend.push(rowData);
				}
				
				sendRowData['t2codeDtl'] = dataToSend;
				sendRowData['cdNo'] = lastsel;
				
				ajax.setCodeDetail(sendRowData);
			}
		},
		
		// 숫자만 입력가능하게 설정
		numChk: function(){
			if(!( (event.keyCode >= 48 
						&& event.keyCode <= 57) 
			   || (event.keyCode >= 96 
					   	&& event.keyCode <= 105)
			   || event.keyCode == 8 
			   || event.keyCode == 9 ) ) {
				event.returnValue = false;
			}
		}
	};
	
	
	// Grid formatter
	var gridFormatter = {
			// row 삭제버튼 동적 추가
			delBtn: function(cellvalue, options, rowObject){
				var button = "<input type=\"button\" value=\"delete\" class=\"btnCLight darkgray\" onclick=\"fn.delRow("+"'"+options.gid+"'"+",'"+options.rowId+"')\" />";

				return button;
			}
	}
	
	
	// http 통신
	var ajax = {
		// 코드 목록
		getCodeList: function(){
			$("#list").jqGrid('GridUnload');	// Grid 초기화
			
				var codeNo = $('#codeNo').val();
			var codeName = $('#codeName').val();
			var data = "cdNo=" + codeNo + "&cdNm=" + codeName; 
			
				$.ajax({
					type: 'GET',
					url:'<c:url value="/system/code/listAjax"/>',
					data: data
				}).success(function (data, status, headers, config) {
					codeList.load(data.codeList);
				}).error(function (data, status, headers, config) {
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				});
		},
		// 코드상세 목록
		getCodeDetailList: function(rowid){
			$("#list2").jqGrid('GridUnload');	// Grid 초기화
			
			var data = "cdNo=" + rowid;
			
			$.ajax({
					type: 'POST',
					url:'<c:url value="/system/code/detail/listAjax"/>',
					data: data
				}).success(function (data, status, headers, config) {
					codeDetailList.nowCodeNo = rowid;	// 코드상세 신규추가 시 필요한 상위 코드번호 설정
					codeDetailList.load(data.codeDetailList);
				}).error(function (data, status, headers, config) {
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				});
		},
		// 코드 저장
		setCode: function(data){
			$.ajax({
				type: 'POST',
				url:'<c:url value="/system/code/saveAjax"/>',
				data: data,
				headers: {
					'Content-Type': 'application/json'
				}
			}).success(function (data, status, headers, config) {
				ajax.getCodeList();
			}).error(function (data, status, headers, config) {
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			});
		},
		
		// 코드상세 저장
		setCodeDetail: function(data){
			var dataToSend = JSON.stringify(data);
			
			$.ajax({
				type: 'POST',
				url:'<c:url value="/system/code/detail/saveAjax"/>',
				data: dataToSend,
				dataType: "json",
				contentType: "application/json; charset=utf-8"
			}).success(function (data, status, headers, config) {
				if("false" == data.isValid) {
					alertify.error('<spring:message code="msg.common.valid" />', 0);
					gridListBulkEdit("list2")
					
					createValidMsgComplex(data);
				} else {
					alertify.success('<spring:message code="msg.common.success" />');
				}
			}).error(function (data, status, headers, config) {
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			});
		}
	};
	
	var updateDialogAdd = {
            	url:'<c:url value="/system/code/saveAjax"/>'
                , closeAfterAdd: true
                , reloadAfterSubmit: true
                , modal: true
			    , serializeDelData: function(postdata) {
			        return JSON.stringify(postdata);
			    }
			    , beforeShowForm: function(formid) {
			    	// 신규등록시 cdno 입력가능
			        $("#cdNo", formid).removeAttr("readonly");
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
                	// client validation check가 필요한 경우 추가 
                	// client validation 결과 false인 경우 return [false]
                	
                	return [true];
                }
	};
	
	var updateDialogEdit = {
            url:'<c:url value="/system/code/saveAjax"/>'
                , closeAfterEdit: true
                , reloadAfterSubmit: true
                , modal: true
			    , serializeDelData: function(postdata) {
			        return JSON.stringify(postdata);
			    }
                , beforeShowForm: function(formid) {
                	// 코드수정시 cdno 수정불가처리
                    $("#cdNo", formid).attr("readonly","readonly");
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
                    // client validation check가 필요한 경우 추가 
                    // client validation 결과 false인 경우 return [false]
                    
                    return [true];
                }
	};
	
	var updateDialogDel = {
            url:'<c:url value="/system/code/saveAjax"/>'
                , closeAfterDel: true
                , reloadAfterSubmit: true
                , modal: true
                , onclickSubmit: function(params, rowid) {
                    var ajaxData = {};
                    var list = $("#list");
                    var selectedRow = list.jqGrid("getGridParam", "selrow");
                    rowData = list.jqGrid("getRowData", rowid);
                    ajaxData = {cdNo: rowData.cdNo};

                     return ajaxData;
                }
			    , afterSubmit: function(response, postdata) {
                    if("false" == response.responseJSON.isValid) {
                        alertify.error('<spring:message code="msg.common.valid" />', 0);

                        return [false, '<spring:message code="msg.common.valid" />', ""]
                    } else {
                        alertify.success('<spring:message code="msg.common.success" />');
                        $("#list2").jqGrid('GridUnload');

                        return [true,"",""]
                    }
			    }
	};
	
	// 코드 Grid
	var codeList = {
			modifyRowId: [],		// 수정된 모든 rowid
			deleteRowList: [],		// 삭제할 LIST
			lastEditRowId: "",		// 마지막 수정된 rowid 저장 변수
			lastIdNo: "",			// 서버에서 가져온 마지막 id
			
			load: function(){
				codeList.modifyRowId = [];
				codeList.deleteRowList = [];
				codeList.lastEditRowId = "";
				
				$("#list").jqGrid({
					url: '<c:url value="/system/code/listAjax"/>'
					, datatype: 'json'
					, jsonReader: {
						repeatitems: false,
						id:'gridId',
						root:function(obj){return obj.rows;},
						page:function(obj){return obj.page;},
						total:function(obj){return obj.total;},
						records:function(obj){return obj.records;}
					}
					, colNames: ['gridId','Code No','Code Name', 'Code Description']
					, colModel: [
                        {name: 'gridId', index: 'gridId', key:true, editable:false, hidden:true},
						{name: 'cdNo', index: 'cdNo', width: 40, align: 'center', sorttype: "int", editable:true, editrules : {required: true}, editoptions:{dataEvents: [{type:'keydown', fn:fn.numChk}]} },
						{name: 'cdNm', index: 'cdNm', width: 250, align: 'left', editable:true, editrules : {required: true}},
						{name: 'cdExp', index: 'cdExp', width: 250, align: 'left', editable:true}
					]
					, emptyrecords: "Nothing to display"
					, autoencode: true
					, editurl: 'clientArray'
					, rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")}
					, rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}]
   					, autowidth: true
					, pager: '#pager'
					, gridview: true
					, sortable: function (permutation) {
					}
					, sortname: 'cdNo'
					, sortorder: 'asc'
					, viewrecords: true
					, height: 'auto'
					, loadonce: false
					// 데이터 로딩 후
					,loadComplete: function(data) {
						// id(key값)의 max값을 lastIdNo에 설정
						codeList.lastIdNo = fn.getMaxIdNo('code');

                        $("#btnSaveCodeDetail").hide();
                        $("#list2").jqGrid('GridUnload');
					}
					// 다른 row 클릭 시
					, onSelectRow: function(rowid,status,eventObject) {
						// 코드상세 목록 load
						$("#list2").show();
						$("#btnSaveCodeDetail").show();
						
						ajax.getCodeDetailList(rowid);
						lastsel = rowid;
					}
					// row 더블클릭 시 편집모드
					, ondblClickRow: function(rowid,iRow,iCol,e) {
						$("#list").jqGrid('editRow',rowid,true,function(){
							$("input, select",e.target).focus();	// 더블클릭한 cell 위치로 포커스(기본포커스는 맨 앞 cell)
						});
					
						codeList.lastEditRowId = rowid;
					}
				});
                $("#list").jqGrid('navGrid',"#pager",{edit:true,add:true,del:true,search:false,refresh:false},updateDialogEdit, updateDialogAdd,updateDialogDel);
			}
	};
	
	// 코드 상세 Grid
	var codeDetailList = {
			modifyRowId: [],		// 수정된 모든 rowid
			deleteRowList: [],		// 삭제할 LIST
			lastEditRowId: "",		// 마지막 수정된 rowid 저장 변수
			lastIdNo: "",			// 서버에서 가져온 마지막 id
			nowCodeNo: "",			// 현재 상세코드 목록의 상위 코드 번호(codeList의 cdNo)
			load: function(mydata2){
				codeDetailList.modifyRowId = [];
				codeDetailList.deleteRowList = [];
				codeDetailList.lastEditRowId = "";

				$("#list2").jqGrid({
					datatype: 'local',
					data: mydata2,
					colNames: ['','','Detail<br/>No','Detail Name', 'Detail Description', 'Sub Code', 'Order', 'Use YN', ''],
					colModel: [
						{name: 'cdNo', index: 'cdNo', hidden:true},
						{name: 'gridId', index: 'gridId', key:true, hidden:true},
						{name: 'cdDtlNo', index: 'cdDtlNo', width: 40, align: 'center', sorttype: "int", editable:true},
						{name: 'cdDtlNm', index: 'cdDtlNm', width: 200, align: 'left', editable:true},
						{name: 'cdDtlExp', index: 'cdDtlExp', width: 250, align: 'left', editable:true},
						{name: 'cdSubNo', index: 'cdSubNo', width: 70, align: 'left', editable:true, editoptions:{dataEvents: [{type:'keydown', fn:fn.numChk}]}},
						{name: 'cdOrder', index: 'cdOrder', width: 50, align: 'left', sorttype: "int", editable:true, editoptions:{dataEvents: [{type:'keydown', fn:fn.numChk}]}},
						{name: 'useYn', index: 'useYn', width: 50, align: 'center', editable:true, edittype:"select", editoptions:{value:"Y:Y;N:N"}},
						{name: 'btnArea', index: 'btnArea', width: 70, align: 'center', formatter: gridFormatter.delBtn},
					],
					autoencode: true,
   					autowidth: true,
					pager: '#pager2',
		   			rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
					gridview: true,
					sortable: function (permutation) {
						alert ('permutation=' + permutation.join(','));
					},
					sortname: 'cdOrder',
					sortorder: 'asc',
					viewrecords: true,
					height: 'auto',
					editurl: 'clientArray',
					loadonce: true,
					// 데이터 로딩 후
					loadComplete: function(data) {
						// id(key값)의 max값을 lastIdNo에 설정
						codeDetailList.lastIdNo = fn.getMaxIdNo('codeDtl');
						hidePageNav('pager2');
					},
                    ondblClickRow: function(rowid,iRow,iCol,e) {
                    	gridListBulkEdit("list2");
                    	
                        $("input, select",e.target).focus();    // 더블클릭한 cell 위치로 포커스(기본포커스는 맨 앞 cell)
                    }
				});
				
				$("#list2").jqGrid('navGrid',"#pager2",{edit:false,add:true,del:false,search:false,refresh:false,addtext:'Add Detail'
					, addfunc: function (rowid) {
						fn.addRow('codeDtl');
					}
				});
			}
	};
//]]>
</script>