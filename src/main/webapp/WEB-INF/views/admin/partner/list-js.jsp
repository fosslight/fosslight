<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var userIdList;
	var pastEmpList;
	var totalRow = 0;
	var refreshParam = {};
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		ajax.getUserIdList("Y", "REVIEWER"); // 근무자
		ajax.getUserIdList("N", "PASTEMP"); // 퇴사자

		if('${sessUserInfo.authority}' == "ROLE_VIEWER"){
			$(".btnAdd").hide();
		}
		
		showHelpLink("3rd-Party_List_Main");
	});
	
	
	//이벤트
	var evt = {
		init : function(){
			refreshParam = $('#3rdSearch').serializeObject();
			
			$('#search').on('click',function(e){
				e.preventDefault();
				var postData=$('#3rdSearch').serializeObject();
				
				//public 값 넣어주기
				if($('#checkbox3').is(':checked')){
					postData.publicYn = 'N';
				}else{
					postData.publicYn = 'Y';
				}
				
				if(postData.status) {
					postData.status = JSON.stringify(postData.status);
					postData.status = postData.status.replace(/\"|\[|\]/gi, "");
				}else{
					postData.status = "";
				}
				
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
			
			$('select[name=division]').val('${searchBean.division}').trigger('change');
			$('select[name=status]').val('${searchBean.status}').trigger('change');
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	}
	
	var fn = {
		downloadExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = $('#3rdSearch').serializeObject();
				
				//public 값 넣어주기
				if($('#checkbox3').is(':checked')){
					data.publicYn = 'N';
				}else{
					data.publicYn = 'Y';
				}
	
				if(data.status) {
					data.status = JSON.stringify(data.status);
					data.status = data.status.replace(/\"|\[|\]/gi, "");
				}
				
				$.ajax({
					type: "POST",
					url: '/exceldownload/getExcelPost',
					data: JSON.stringify({"type":"3rd", "parameter":JSON.stringify(data)}),
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
		displayComment : function(cellvalue, options, rowObject){
			var display = "";
			
			if(cellvalue !="" && cellvalue != undefined){
				display ="<div style=\"height : 29px; overflow: hidden;\">"+cellvalue+"</div>";
			}
			
			return display;
		},
		reviewerChg : function(){
			var partnerId = (this.id).replace(/[^0-9]/g,'');
			var reviewer = Object.keys(userIdList)[Object.keys(userIdList)
														.map(function(e) {
															  return userIdList[e]
														})
														.indexOf(this.value)];
			var data = {"partnerId" : partnerId, "reviewer" : reviewer};
			
			$.ajax({
				url : '/partner/updateReviewer',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(data){
					$("#list").jqGrid('saveRow',partnerId);
					$("#list").jqGrid('setCell', partnerId, "reviewer", reviewer);
					
					alertify.success('<spring:message code="msg.common.success" />');
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			})
		}, getUserName : function(cellvalue, options, rowObject){
			return userIdList[cellvalue] || pastEmpList[cellvalue] || "";
		}
	}
	
	//http
	var ajax = {
		getUserIdList : function(adminYn, type){
			return $.ajax({
				type: 'GET',
				url: "/project/getUserIdList",
				data: {adminYn : adminYn},
				success : function(data){
					temp = data.split(";").reduce(function(obj, cur){
					    var keys = Object.keys(obj);
					    var pairData = cur.split(":");

					    if(keys.indexOf(pairData[0]) == -1 && pairData[0].trim() != ""){
					        obj[pairData[0]] = pairData[1];
					    }

					    return obj;
					}, {});
					
					if(type == "REVIEWER") {
						userIdList = temp;
					} else {
						pastEmpList = temp;
						partnerList.load();
					}
					
				}
			});
		}
	};

	//3rdParty 그리드
	var partnerList = {
			modifyRowId: [],		//수정된 모든 rowID
			lastEditRowId: '',		//마지막 수정된 rowId 저장 변수
			lastIdNo: '',			//서버에서 가져온 마지막 ID
			load : function(){
			$('#list').jqGrid({
				url: '/partner/listAjax',
				datatype: 'json',
				jsonReader:{
					repeatitems: false,
					id: 'partnerId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID','3rd Party Name','Software Name','Software<br/>Version', 'Status', 'Delivery Form','Description', 'Division', 'Creator', 'Created Date', 'Updated Date', 'Reviewer', 'Comment', 'fileName'],
				colModel: [
					{name: 'partnerId', index: 'partnerId', width: 30, align: 'center', key:true, sortable : true},
					{name: 'partnerName', index: 'partnerName', width: 100, align: 'left', sortable : true},
					{name: 'softwareName', index: 'softwareName', width: 100, align: 'left', sortable : true},
					{name: 'softwareVersion', index: 'softwareVersion', width: 40, align: 'left', sortable : true},
					{name: 'status', index: 'status', width: 50, align: 'center', sortable : true},
					{name: 'deliveryForm', index: 'deliveryForm', width: 100, align: 'center', sortable : true},
					{name: 'description', index: 'description', width: 100, align: 'left', sortable : true},
					{name: 'division', index: 'division', width: 100, align: 'left', sortable : true},
					{name: 'creator', index: 'creator', width: 70, align: 'center', sortable : true},
					{name: 'createdDate', index: 'createdDate', width: 80, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true},
					{name: 'modifiedDate', index: 'modifiedDate', width: 80, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, sortable : true},
					{name: 'reviewer', index: 'reviewer', width: 80, align: 'left', formatter: 'select', editable:'${sessUserInfo.authority}'=="ROLE_ADMIN" ? true : false, edittype:'text', formatter:fn.getUserName
						, editoptions: {
							dataInit:
								function (e) {
									$(e).autocomplete({
										source: function(req, res){
											res(
												$.grep(Object.keys(userIdList)
														.map(function(e) {
															  return userIdList[e]
														}), function(cur){
												    return cur;
												})
											);
										}
										, minLength: 0
										, open: function() { $(this).attr('state', 'open');}
										, close: function () { $(this).attr('state', 'closed');}
									}).focus(function() {
										if ($(this).attr('state') != 'open') {
											$(this).autocomplete("search");
										}
									}).blur(function(){
										$(this).attr('state', 'closed');
									}).on('autocompletechange', fn.reviewerChg);
									
									currentOssName = e.value;
								}
						}
						, sortable : true
					},
					{name: 'comment', index: 'comment', width: 100, align: 'left', formatter:fn.displayComment, sortable : true},
					{name: 'fileName', index: 'fileName', width: 70, align: 'left', hidden:true}
				],
				onSelectRow: function(id){},
				rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")},
				rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}],
				editurl:'clientArray',
				autowidth: true,
				pager: '#pager',
				gridview: true,
				sortable: function(permutation){
				},
				sortname: 'partnerId',
				viewrecords: true,
				sortorder: 'desc',
				loadonce: false,
				height: 'auto',
				onCellSelect: function(rowid,iCol,cellcontent,e) {
					var role = '${sessUserInfo.authority}';

					if(role=="ROLE_ADMIN"){
						$("#list").jqGrid('saveRow',lastsel);
						$("#list").jqGrid('editRow',rowid);
						lastsel=rowid;
					}
				},
				loadComplete: function(data){
					totalRow = data.records;
					data = data.rows;

					if(totalRow == 0){
						var startDate = $("#createdDate1").val()||0;
						var endDate = $("#createdDate2").val()||0;
						var diffNum = +startDate - +endDate;
						
						if(diffNum > 0 && endDate > 0){
							alertify.alert('<spring:message code="msg.common.search.check.date" />');
						}
					}
					for(var i=0; i<data.length; i++){
						// 조건에 따라 cell 색상 변경
						if(data[i].status){
							if(data[i].status.indexOf("PROG") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", 'Progress');
							}
							
							if(data[i].status.indexOf("REV") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", "", {'background-color':'#6d7e9c', 'color':'#fff'});
								$("#list").jqGrid("setCell", data[i].partnerId, "status", 'Review');
							}
							
							if(data[i].status.indexOf("REQ") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", "", {'background-color':'#9da5b8', 'color':'#fff'});
								$("#list").jqGrid("setCell", data[i].partnerId, "status",'Request');
							}
							
							if(data[i].status.indexOf("CONF") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", "", {'background-color':'#384f7b', 'color':'#fff'});
								$("#list").jqGrid("setCell", data[i].partnerId, "status",'Confirm');
							}							
						}
						
						if(data[i].deliveryForm){
							if(data[i].deliveryForm == 'SRC') {
								$("#list").jqGrid("setCell", data[i].partnerId, "deliveryForm",'source form');
							} else if(data[i].deliveryForm == 'BIN') {
								$("#list").jqGrid("setCell", data[i].partnerId, "deliveryForm",'binary form');
							}
						}
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					createTabInFrame(rowid+'_3rdParty', '#/partner/edit/'+rowid);
				},
				postData : refreshParam
			})
		}
	}
//]]>
</script>
