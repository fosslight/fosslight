<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var divisionList = "";
	
	$(document).ready(function () {
		'use strict';
		
		evt.init();
		
		fn.getDivisionList();
		userList.load({});
	});

	
	//이벤트
	var evt = {
		//ready evnet
		init : function(){

			$("#userName, #userId").on("keypress", function(e){
				if(e.keyCode == "13"){
					$('#search').trigger('click');
				}
			});
			
			$('#search').on('click',function(e){
	            e.preventDefault();
	            
	            var postData=$('#userSearch').serializeObject();
	            
	            $('#list').jqGrid('setGridParam', {postData:postData, page : 1, datatype:"json"}).trigger('reloadGrid');
	        });			
		}
	}
	
	//함수
	var fn = {
		// 수정된 마지막 row 저장
		saveLastRow: function(){	
			$("#list").jqGrid("editCell", 0, 0, false);		// $("#list").jqGrid("editCell", row, col, false); 반드시 edit focus를 해제시킨다. 해제 시키지 않을 경우 값이 <input ....>으로 저장된다.
			$("#list").jqGrid("saveRow", userList.lastEditRowId);
			$("#list").jqGrid("setRowData", userList.lastEditRowId, false, { background : "#feffc4" });	// 수정된 row 색 변경
			
			// 수정된 rowid 저장
			if(userList.modifyRowId.length == 0){
				userList.modifyRowId.push(userList.lastEditRowId);
			} else {
				userList.modifyRowId.forEach(function(){		// 중복제외
					if(userList.modifyRowId.indexOf(userList.lastEditRowId) == -1) {
						userList.modifyRowId.push(userList.lastEditRowId);								
					}
				});
			}
		},
		// 저장된 rowid로 rowdata 가져와서 저장
		saveUser: function(){
			var modifyRowList = [];	// 수정된 LIST
			var modifyRow = {};
			
			fn.saveLastRow();
			
			userList.modifyRowId.forEach(function(val){
				if(val){
					modifyRowList.push($("#list").jqGrid("getRowData",val));
				}
			});
			
			// {newRowList: [], modifyRowList: []} 형식으로 변환
			modifyRow['modifyRowList'] = modifyRowList;
			
			var data = JSON.stringify(modifyRowList);
			
			ajax.setUser(data);
		},
		// Grid rowid의 max값
		getMaxIdNo: function(){
			var ids = [];
			ids = $("#list").jqGrid('getDataIDs');
			
			return (ids.length == 0) ? 0 : Math.max.apply(Math, ids);
		},
		downloadExcel : function(){
			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"user"}),
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
		
		getDivisionList: function(){
			var code = ${ct:getConstDef("CD_USER_DIVISION")};
			
			return $.ajax({
				type: 'POST',
				url: '<c:url value="/system/user/getDivisionList"/>',
				data: JSON.stringify({"cdNo" : code}),
				async: false,
				contentType : 'application/json',
				success : function(data){
					if(data != null){
						var tmpData = " : ;";
						
						for(var i=0; i < data.length; i++){
							tmpData += data[i].cdDtlNo+":"+data[i].cdDtlNm+";";
						}
						
						divisionList = tmpData.slice(0,-1);
					}
				}
			});
		},
		TokenProc : function(type){
			var params = {};
			
			switch(type){
				case "CREATE": break;
				case "DELETE": break;
				case "CHANGE": break;
			}

			$.ajax({
				url: '<c:url value="/system/user/tokenProc"/>',
				type : 'POST',
				dataType : 'json',
				cache : false,
				data : JSON.stringify(param),
				contentType : 'application/json',
				success : function(data){
					if(data.isValid == "false"){
						$(".ajs-close").trigger("click"); // dialog popup close
						$(".ajs-dialog").css("height", ""); // height rollback
						
						alertify.alert(data.validMsg, function(){});
					}else {
						$(".ajs-close").trigger("click"); // dialog popup close
						$("#list").jqGrid().trigger('reloadGrid');
					}
				},
				error : function(){}
			});
		},
		procToken : function(rowId, type){
			$("#list").jqGrid('saveRow', rowId); // editMode exit

			if(rowId != ""){
				var url = '<c:url value="/system/user/tokenProc/'+type+'"/>';
				var mainData = $("#list").getRowData(rowId);
				var params = {
					mainData : JSON.stringify(mainData)
				};

				$.ajax({
					url : url,
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : JSON.stringify(params),
					contentType : 'application/json',
					success : function(data){
						if(data.isValid == true){
							$('#search').trigger('click');
						} else {
							alertify.error('<spring:message code="msg.common.valid" />', 0);
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			} else {
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		},
		resetPassword : function(rowId){
			if("${'Y' eq adminLockFlag}" && rowId == 'admin') {
				alert("Changing admin information is blocked.");
				return false;
			}
				$.ajax({
					type: 'POST',
					url: '<c:url value="/system/user/changePassword"/>',
					data: JSON.stringify({'userId': rowId}),
					contentType : 'application/json',
			        success: function(json){
			        	loading.hide();
						if(json.resCd == '10'){
							alertify.alert('Change the password to be the same as ID.', function(){}); 
						}else{
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
				    },
		            error : function(data, status){
		    			alertify.error('<spring:message code="msg.common.valid2" />', 0);
		    	    }
				});
		}
	};
	
	//Grid formatter
	var gridFormatter = {};
	
	//http 통신
	var ajax = {
		// 코드 저장
		setUser: function(param){
			$.ajax({
				type: 'POST',
				url: '<c:url value="/system/user/modAjax"/>',
				data: param,
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json' 
				},
			}).success(function (data, status, headers, config) {
				$("#list").jqGrid().trigger('reloadGrid');
			}).error(function (data, status, headers, config) {});
		}
	};

	//유저 그리드
	var userList = {
		modifyRowId: [],		//수정된 모든 rowID
		lastEditRowId: '',		//마지막 수정된 rowId 저장 변수
		lastIdNo: '',			//서버에서 가져온 마지막 ID
		load: function(data){
			userList.modifyRowId = [];
			userList.lastEditRowId = "";

			//그리드 생성
			$('#list').jqGrid({
				url: '<c:url value="/system/user/listAjax"/>'
				, datatype: 'json'
				, data: data
				, jsonReader:{
					repeatitems: false,
					id:'userId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				}
				, colNames: ['AD ID', 'E-mail', 'Name', 'Division', 'Registered Date', 'Token', 'Expire Date', 'Token Proc'
					<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_LDAP_USED_FLAG')) eq 'N'}">
					, 'password'
					</c:if>
					, 'Use YN', 'Admin']
				, colModel: [
					{name: 'userId', index: 'userId', width: 150, allign: 'center'},
					<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_HIDE_EMAIL_FLAG')) ne 'Y'}">
					{name: 'email', index: 'email', width: 200, allign: 'center'},
					</c:if>
					<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_HIDE_EMAIL_FLAG')) eq 'Y'}">
					{name: 'email', index: 'email', width: 200, allign: 'center', hidden: true},
					</c:if>
					{name: 'userName', index: 'userName', width: 100, allign: 'center', editable: true},
					{name: 'division', index: 'division', width: 150, allign: 'right', editable: true, formatter: 'select', edittype:"select",
						editoptions:{value:divisionList}
					},
					{name: 'createdDate', index: 'createdDate', width: 100, align: 'center'},
					{name: 'token', index: 'token', width:100},
					{name: 'expireDate', index: 'expireDate', width:100, align: 'center', editable: true},
					{name: '', index : '', width : 100, align: 'center', formatter: function(cellvalue, options, rowObject){
						var btnHTML = '';
						var tokenFlag = rowObject.token != undefined ? true : false;
						var rowId = options.rowId||"";

						if(tokenFlag){
							btnHTML += '<input id="search" type="button" value="Delete" class="btnColor" style="width:60px;"  onclick="fn.procToken(\''+rowId+'\', \'DELETE\')"/>';
						} else {
							btnHTML += '<input id="search" type="button" value="Create" class="btnColor" style="width:60px;" onclick="fn.procToken(\''+rowId+'\', \'CREATE\')"/>';
						}

						return btnHTML;
					}},
					<c:if test="${ct:getCodeExpString(ct:getConstDef('CD_SYSTEM_SETTING'), ct:getConstDef('CD_LDAP_USED_FLAG')) eq 'N'}">
					{name: 'password', index: 'password', width:150, align: 'center', formatter: function(cellvalue, options, rowObject){
						var rowId = options.rowId||"";
						var btnHTML = '<input id="search" type="button" value="reset" class="btnColor" style="width:60px;"  onclick="fn.resetPassword(\''+rowId+'\')"/>';
						return btnHTML;
					}},
					</c:if>
					{name: 'useYn', index: 'useYn', width: 80, align: 'center', editable:true, edittype:"select", editoptions:{value:"Y:Y;N:N"}},
					{name: 'authority', index: 'authority', width: 80, align: 'center', editable:true, edittype:"checkbox",editoptions: {value: "V::"}},
				]
				, editurl :'clientArray'
				, rowNum: 20
				, rowList: [20, 40, 60]
				, autowidth: true
				, pager: '#pager'
				, gridview: true
				, sortable: function (permutation) {}
				, sortname: 'userId'
				, viewrecords: true
				, sortorder: 'asc'
				, loadonce: true
				, height: 'auto'
				, onSelectRow: function(rowid,iRow,iCol,e){
					var userId = ($(this).jqGrid('getCell', rowid, 'userId')).toUpperCase();
					// 마지막 수정된 row 저장
					if(userList.lastEditRowId && rowid != userList.lastEditRowId){
						fn.saveLastRow();
					}
					$("#list").jqGrid('editRow',rowid,true,function(){});
					userList.lastEditRowId = rowid;
				}
				, loadComplete:function(data) {
					// id(key값)의 max값을 lastIdNo에 설정
					userList.lastIdNo = fn.getMaxIdNo();
				}
			})
		}
	};
//]]>
</script>