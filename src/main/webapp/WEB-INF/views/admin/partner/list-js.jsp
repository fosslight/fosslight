<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var userIdList;
	var adminUserList;
	var totalRow = 0;
	var refreshParam = {};
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	var divisionEmptyCd = "${ct:getConstDef('CD_USER_DIVISION_EMPTY')}";
	var changeWatcherArr = [];
	
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		ajax.getUserIdList("Y", "REVIEWER"); // 근무자
		ajax.getUserIdList("N", "ADMIN_USER"); // 관리자(퇴사자 포함)
		
		if('${sessUserInfo.authority}' == "ROLE_VIEWER"){
			$(".btnAdd").hide();
		}
		
		showHelpLink("3rd-Party_List_Main");
	});
	
	var gridTooltip = {
	    typeCodes : [],
		tooltipCont : "<div class=\"tooltipData\">"
			+"<dl><dt><span class=\"iconSt progress\">Progress</span>Progress</dt></dl><br>"
			+"<dl><dt><span class=\"iconSt request\">Request</span>Request</dt></dl><br>"
			+"<dl><dt><span class=\"iconSt review\">Review</span>Review</dt></dl><br>"
			+"<dl><dt><span class=\"iconSt confirm\">Confirm</span>Confirm</dt></dl><br>"
			+"</div>",
		existTooltip : false,
		init : function(){
	        list.load();	// Grid Load
		}
	};
	
	//event
	var evt = {
		init : function(){

			$('select[name=division]').val('${searchBean.division}').trigger('change');
			$('select[name=status]').val('${searchBean.status}').trigger('change');
			
			refreshParam = fn.setGridParam();
			
			$('#search').on('click',function(e){
				e.preventDefault();
				var postData = fn.setGridParam();
				
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
		}
	}
	
	var fn = {
		unformatter : function(cellvalue, options, rowObject){
			return cellvalue;
		},
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
					url: '<c:url value="/exceldownload/getExcelPost"/>',
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
				var tmpStr = new RegExp();
				tmpStr = /[<][^>]*[>]/gi;
				display ="<div style=\"height : 29px; overflow: hidden;\">"+cellvalue.replace(tmpStr , "")+"</div>";
			}
			
			return display;
		},
		// Grid vulnerability cell display
		displayVulnerability : function(cellvalue, options, rowObject){
			var display = "";

			if(parseInt(cellvalue) >= 9.0 ) {
				display="<span class=\"iconSet vulCritical\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 7.0 ) {
				display="<span class=\"iconSet vulHigh\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 4.0) {
				display="<span class=\"iconSet vulMiddle\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) > 0) {
				display="<span class=\"iconSet vulLow\" onclick=\"openNVD('"+ rowObject.cveId +"')\">"+cellvalue+"</span>";
			} else {
				display="<span style=\"font-size:0;\"></span>";
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
				url : '<c:url value="/partner/updateReviewer"/>',
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
			return adminUserList[cellvalue] || "";
		},
		displayStatus: function (cellvalue, options, rowObject) {
			// 			206 COMF	Confirm
			// 			206 PROG	Progress
			// 			206 REQ		Request
			// 			206 REV		Review
			var display = "";
			
			switch (cellvalue) {
				case "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REQUEST'))}":
					display = "<span class=\"iconSt request\">Request</span>";
					break;
				case "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REVIEW'))}":
					display = "<span class=\"iconSt review\">Review</span>";
					break;
				case "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM'))}":
					display = "<span class=\"iconSt confirm\">Confirm</span>";
					break;
				case "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_PROGRESS'))}":
					display = "<span class=\"iconSt progress\">Progress</span>";
					break;
			}
			return display;
		},
		displayDeliveryForm: function (cellvalue, options, rowObject) {
			var display = "";

			switch (cellvalue) {
				case "${ct:getConstDef('CD_DTL_PARTNER_DELIVERY_FORM_SRC')}":
					display = "<span class=\"iconDeliveryForm source\">"
						+ "${ct:getCodeString(ct:getConstDef('CD_PARTNER_DELIVERY_FORM'), ct:getConstDef('CD_DTL_PARTNER_DELIVERY_FORM_SRC'))}"
						+ "</span>";
					break;
				case "${ct:getConstDef('CD_DTL_PARTNER_DELIVERY_FORM_BIN')}":
					display = "<span class=\"iconDeliveryForm binary\">"
						+ "${ct:getCodeString(ct:getConstDef('CD_PARTNER_DELIVERY_FORM'), ct:getConstDef('CD_DTL_PARTNER_DELIVERY_FORM_BIN'))}"
						+ "</span>";
					break;
			}
			return display;
		},
		setGridParam: function() {
			var paramData=$('#3rdSearch').serializeObject();
			
			//public 값 넣어주기
			if($('#checkbox3').is(':checked')){
				paramData.publicYn = 'N';
			}else{
				paramData.publicYn = 'Y';
			}
			
			if(paramData.status) {
				paramData.status = JSON.stringify(paramData.status);
				paramData.status = paramData.status.replace(/\"|\[|\]/gi, "");
			}else{
				paramData.status = "";
			}
			
			return paramData;
		},
		changeDivision : function(){
			var chk = $("#list").jqGrid("getGridParam", "selarrrow").length;

			if(chk > 0){
				$("#partnerChangeDivisionSelect").find("strong").text($("#partnerChangeDivisionSelect select[name='partnerDivision'] option:first").text());
				$("#partnerChangeDivisionPop").show();
			} else {
				alertify.alert('<spring:message code="msg.project.watcher.selectlist" />', function(){});
			}
		}, 
		changeDivisionSave : function(){
			var changeDivisionArr = $("#list").jqGrid("getGridParam", "selarrrow");
			var division = $("#partnerChangeDivisionPop select[name=partnerDivision]").val();

			alertify.confirm('<spring:message code="msg.common.change.division" />', function (e) {
				if (e) {
					$('#changeDivisionPop').hide();
					
					$.ajax({
						type: 'POST',
						url :'<c:url value="/partner/updatePartnerDivision"/>',
						data: JSON.stringify({'partnerIds':changeDivisionArr, 'parDivision':division}),
						contentType : 'application/json',
						success: function (data) {
							if("true" == data.isValid){
								alertify.alert('<spring:message code="msg.common.success" />', function(){
									reloadTabInframe('<c:url value="/partner/list"/>');
									activeTabInFrameList("PARTNER");
								});
							} else {
								var list = [];
								list = data.resultData;
								
								var msg = '<spring:message code="msg.partner.check.division.permissions" />';
								msg += '<br/> - '

								for(var i=0; i<list.length; i++){
									msg += '3rd-' + list[i];
									if(i < list.length - 1){
										msg += ', ';
									}
								}
								
								alertify.alert(msg, function(){});
							}
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
				} else {
					return false;
				}
			});
		}, 
		changeDivisionCancel : function(){
			$('#partnerChangeDivisionPop').hide();
		},
		change : function(obj) {
			var changeListId = '#' + $(obj).siblings("div").attr("id");
	        if ($(changeListId).css('display')=='none') {
	            $(changeListId).show();
	        }else{
	            $(changeListId).hide();
	        }
	        $(changeListId).menu();
		},
		changeWatcher : function() {
			var chk = $("#list").jqGrid("getGridParam", "selarrrow").length;
			if(chk > 0){
				$("#changeWatcherPop").show();
			} else {
				alertify.alert('<spring:message code="msg.project.watcher.selectlist" />', function(){});
			}
		},
		changeWatcherCanCel : function() {
			fn.resetChangeWatcherPop();
		},
		changeWatcherAdd : function() {
			alertify.confirm('<spring:message code="msg.common.change.watcher" />', function (e) {
				if (e) {
					if (changeWatcherArr.length == 0) {
						alertify.alert('<spring:message code="msg.common.change.watcher.add" />', function(){});
						return false;
					}
					
					var partnerIds = $("#list").jqGrid("getGridParam", "selarrrow");
					var data = {"partnerIds" : partnerIds, "changeWatcherList" : changeWatcherArr};
					
					$.ajax({
						url : '<c:url value="/partner/addWatchers"/>',
						type : 'POST',
						data : JSON.stringify(data),
						dataType : 'json',
						cache : false,
						contentType : 'application/json',
						success: function(resultData){
							if(resultData.isValid == "true") {
								alertify.success('<spring:message code="msg.common.success" />');
							} else {
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
					
					fn.resetChangeWatcherPop();
				} else {
					return false;
				}
			});
		},
		changeWatcherDelete : function() {
			alertify.confirm('<spring:message code="msg.common.change.watcher" />', function (e) {
				if (e) {
					if (changeWatcherArr.length == 0) {
						alertify.alert('<spring:message code="msg.common.change.watcher.add" />', function(){});
						return false;
					}
					
					var partnerIds = $("#list").jqGrid("getGridParam", "selarrrow");
					var data = {"partnerIds" : partnerIds, "changeWatcherList" : changeWatcherArr};
					
					$.ajax({
						url : '<c:url value="/partner/removeWatchers"/>',
						type : 'POST',
						data : JSON.stringify(data),
						dataType : 'json',
						cache : false,
						contentType : 'application/json',
						success: function(resultData){
							if(resultData.isValid == "true") {
								alertify.success('<spring:message code="msg.common.success" />');
							} else {
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
					
					fn.resetChangeWatcherPop();
				} else {
					return false;
				}
			});
		},
		selectDivision : function() {
			var division = $('#parDivision').val();
			$('#parUserId').children().remove();
			if(division == "") {
				$('#parUserId').val("").prev().text("select User").change();
				return false;
			}
			$('#parUserId').attr('disabled', false);
			$.ajax({
				url : '<c:url value="/partner/getUserList"/>',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : {'division' : division},
				success : function(data){
					data.forEach(function(obj){
						$('#parUserId').append('<option value='+obj.userId+'>'+obj.userName+'('+obj.userId+')</option>');
					});
					
					$('#parUserId').change();
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		addWatcherClick : function() {
			var prjIds = $("#list").jqGrid("getGridParam", "selarrrow");
			
			var $divSel	= $("#parDivision"),
				divVal	= $divSel.val(),
				divTxt	= $divSel.find("option[value='"+divVal+"']").text();
			
			var $userSel	= $("#parUserId"),
				userVal		= $userSel.val()||"",
				userTxt		= $userSel.find("option[value='"+userVal+"']").text();

			if(divVal == "" || userVal == "") {
				return alertify.error('<spring:message code="msg.project.required.selectDivision" />', 0);
			}
			
			var isNew = true;
			
			$(".watcherTags").each(function(i, tag){
				var tagDiv = $(tag).val().split("/")[0]
				  , tagUid = $(tag).val().split("/")[1];
				
				if(divVal == tagDiv) {
					if(tagUid == 'all') {
						isNew = false;

						return false;
					}
					
					if(userVal == 'all') {
						$(tag).closest('span').remove();
					} else if(tagUid == userVal) {
						isNew = false;
					}
				}
			});
			
			if(isNew) {
				var watcherStr = divisionEmptyCd != divVal ? (divTxt + "/" + userVal) : userVal;
				fn.addWatcherData(divVal, userVal, '');
 				fn.addHtml($("#multiDiv"), watcherStr, divVal, userVal);
			}
		},
		addWatcherData : function (uDiv, uId, uEmail) {
			var dataObj = {};
			dataObj["parDivision"] = uDiv;
			dataObj["parUserId"] = uId;
			dataObj["parEmail"] = uEmail;
			changeWatcherArr.push(dataObj);
		},
		removeWatcher : function (uDiv, uId) {
			var uEmail = "";
			if("Email" == uId) {
				uEmail = uDiv;
				uId = "";
				uDiv = "";
			}
			
			changeWatcherArr.forEach(function(cur, index){
				var obj = cur;
				if (uId === obj["parUserId"] && uDiv === obj["parDivision"] && uEmail === obj["parEmail"]) {
					changeWatcherArr.splice(index, 1);
				}
			});
		},
		addHtml : function(target, str, division, userId){
			var rlt = division+((userId!="") ? "/"+userId : "");
			var html  = '<span id="'+userId+'"><input class="watcherTags" type="text" name="watchers" value="'+rlt+'" style="display: none;"/>';
			html += str;
			if('${project.viewOnlyFlag}' != "Y") {
				html += '<input type="button" value="Delete" class="smallDelete" onclick="fn.removeWatcher(\'' + division + '\',\'' + userId + '\');" /></span>';
			}
			target.append(html);

			$('div.multiTxtSet2 .smallDelete').on('click', function(){
				$(this).parent().remove();
			});
		},
		addEmail : function() {
			var prjIds = $("#list").jqGrid("getGridParam", "selarrrow");
			
			var adId = $("#adId").val();
			var domain = $("#emailTemp").val();

			if(adId == "") {
				$("#adId").focus();
				return alertify.error('<spring:message code="enter.watcher.error" />', 0);
			}
			
			var _email = adId + "@" + domain;
			var regEmail = /([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;

			if (!regEmail.test(_email)) {
				$("#adId").focus();

				return alertify.error('<spring:message code="invalid.email.error" />', 0);
			}
			
			$.ajax({
				type: "POST",
				url: '<c:url value="/system/user/checkEmail"/>', 
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : {'email' : _email},
				success: function (data) {
					var obj = {};
					if("true" == data.isValid) {
						var watcherStr = divisionEmptyCd != data.division ? (data.divisionName + "/" + data.userId) : data.userId;
						obj["parDivision"] = data.division;
						obj["parUserId"] = data.userId;
						obj["parEmail"] = '';
						
						fn.addHtml($("#multiDiv"), watcherStr, data.division, data.userId);
					} else {
						obj["parDivision"] = '';
						obj["parUserId"] = '';
						obj["parEmail"] = _email;
						
						fn.addHtml($("#multiDiv"), _email, _email, "Email");
					}
					
					changeWatcherArr.push(obj);
				},
				error: function(data){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
			
			$("#adId").val('');
			$("#emailTemp").val('');
			$("#domain").val("${ct:getConstDef('CD_DTL_DEFAULT_DOMAIN')}").trigger('change');
		},
		addList : function() {
			var partnerIds = $("#list").jqGrid("getGridParam", "selarrrow");
			var listKind = $("#listKind").val(),
			listId = $("#listId").val();
			
			if(fn.chkListValidation(listKind, listId)) {
				var obj = {};
				obj["listKind"] = listKind;
				obj["listId"] = listId;
			
				fn.copyWatcher(obj, partnerIds);
			}
		},
		chkListValidation : function(listKind, listId){
			if(listKind == ""){
				alertify.error('<spring:message code="msg.project.watcher.selectlist" />', 0);
				return false;
			}
			
			if(listId == ""){
				alertify.error('<spring:message code="msg.project.watcher.required.copyid" />', 0);
				return false;
			}
			
			return true;
		},
		copyWatcher : function(obj, partnerIds) {
			for (var i in partnerIds) {
				var partnerId = partnerIds[i];
				obj["partnerId"] = partnerId;
				obj["copyWatcherLocation"] = "list";
				
				$.ajax({
					url : '<c:url value="/partner/copyWatcher"/>',
					type : 'POST',
					data : JSON.stringify(obj),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
					success: function(resultData){
						var copyWatcher = resultData.copyWatcher;
						
						if(copyWatcher.length){
							for(var i = 0, len = copyWatcher.length ; i < len ; i++){
								var isNew = true
								  , division = copyWatcher[i].division || ""
								  , divisionName = copyWatcher[i].parDivisionName || ""
								  , userId = copyWatcher[i].parUserId || ""
								  , userName = copyWatcher[i].parUserName || ""
								  , email = copyWatcher[i].parEmail || ""
								  , deptUseYn = copyWatcher[i].deptUseYn || "Y"
								  , userUseYn = copyWatcher[i].userUseYn || "Y";
								
								$(".watcherTags").each(function(idx, tag){
									var tagDiv = $(tag).val().split("/")[0]
									  , tagUid = $(tag).val().split("/")[1];
									  
									if(division == tagDiv) {
										if(tagUid == 'all') { // 선택된 태그 중에 all이 있을 경우 등록 안함
											isNew = false;
											
											return false;
										} else if(tagUid == userId) {
											isNew = false;	// 이미 등록된 watcher가 있을경우
										}
									}
									
									if(tagUid == "Email") {
										if(email == tagDiv) {
											isNew = false;
										}
									}
								});
								
								if(isNew){
									var obj = {};
									if(email != ""){
										obj["parDivision"] = '';
										obj["parUserId"] = '';
										obj["parEmail"] = _email;
										
										fn.addHtml($("#multiDiv"), email, email, "Email");
									} else {
										var str = "";
										
										if(userName == "") {
											str = divisionName;
										} else {
											str = '<b';

											if(divisionEmptyCd != division) {
												if(deptUseYn == "N"){
													str += ' class="deleteUser"';
												}
												
												str += '>'+divisionName+'</b>/<b';
											}
											
											if(userUseYn == "N"){
												str += ' class="deleteUser"';
											}
											
											str += '>'+userName+'</b>';
										}
										
										obj["parDivision"] = division;
										obj["parUserId"] = userId;
										obj["parEmail"] = '';
										
										fn.addHtml($("#multiDiv"), str, division, userId);
									}
									
									changeWatcherArr.push(obj);
								}
							}
						}
					},
					error : fn.onError
				});
			}
		},
		checkChar : function(){
			if(event.keyCode == 64){//@ 특수문자 체크
				alertify.alert('<spring:message code="msg.login.check.char" />', function(){});
        		
				event.returnValue = false;
        	}
		},
		selectDomain : function() {
			var adIdDomain = $("select[name=prjDomain] option:checked").text();
			if ("Select Domain" == adIdDomain) adIdDomain = "";
			$("input[name=adIdDomain]").val(adIdDomain);
		},
		resetChangeWatcherPop : function() {
			changeWatcherArr = [];
			$("#multiDiv").children().remove();
			$("select[name=parDivision]").val("").trigger('change');
			$("select[name=parDomain]").val("").trigger('change');
			$("select[name=listKind]").val("").trigger('change');
			$("input[name=listId]").val("");
			$('#changeWatcherPop').hide();
			$("#ChangeList").hide();
		}
	}
	
	//http
	var ajax = {
		getUserIdList : function(reviewerFlag, type){
			return $.ajax({
				type: 'GET',
				url: '<c:url value="/project/getUserIdList"/>',
				data: {reviewerFlag : reviewerFlag},
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
						adminUserList = temp;
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
				url: '<c:url value="/partner/listAjax"/>',
				datatype: 'json',
				jsonReader:{
					repeatitems: false,
					id: 'partnerId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
			  	colNames: ['ID','3rd Party Name','Software Name (Version)','Software<br/>Version', 'Status', 'Delivery<br/>Form','Description'
				  	, 'CVE ID', 'Vulnera<br/>bility', 'Division', 'Creator', 'Created Date', 'Updated Date', 'Reviewer', 'Comment', 'fileName'],
                colModel: [
					{name: 'partnerId', index: 'partnerId', width: 30, align: 'center', key:true, sortable : true},
					{name: 'partnerName', index: 'partnerName', width: 100, align: 'left', sortable : true},
					{name: 'softwareName', index: 'softwareName', width: 100, align: 'left', sortable : true},
					{name: 'softwareVersion', index: 'softwareVersion', width: 40, align: 'left', sortable : true, hidden:true},
					{name: 'status', index: 'status', width: 50, align: 'center', formatter: fn.displayStatus, sortable : true},
					{name: 'deliveryForm', index: 'deliveryForm', width: 50, align: 'center', formatter: fn.displayDeliveryForm, sortable : true},
					{name: 'description', index: 'description', width: 100, align: 'left', sortable : true, formatter:fn.displayComment, unformatter:fn.unformatter},
					{name: 'cveId', index: 'cveId', hidden:true},
					{name: 'cvssScore', index: 'cvssScore', width: 50, align: 'center', formatter:fn.displayVulnerability, unformatter:fn.unformatter, sortable : false},
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
					{name: 'comment', index: 'comment', width: 100, align: 'left', formatter:fn.displayComment, sortable : true, hidden:true},
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
				multiselect : true,
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

					var target = $("#list");
					var arr = target.jqGrid('getDataIDs');
					var rowid;

					for(var idx in arr) {
						rowid = arr[idx];

						//prjName prjVersion 데이터 join
						var swNmVer = data[idx].softwareName;

						if(data[idx].softwareVersion != ""){
							swNmVer += " (ver "+data[idx].softwareVersion+")";
						}

						$("#list").jqGrid("setCell",rowid,"softwareName",swNmVer,"");
					}

					if(totalRow == 0){
						var startDate = $("#createdDate1").val()||0;
						var endDate = $("#createdDate2").val()||0;
						var diffNum = +startDate - +endDate;
						
						if(diffNum > 0 && endDate > 0){
							alertify.alert('<spring:message code="msg.common.search.check.date" />', function(){});
						}
					}
					for(var i=0; i<data.length; i++){
						if(data[i].status){
							if(data[i].status.indexOf("PROG") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", 'Progress');
							}
							
							if(data[i].status.indexOf("REV") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status", 'Review');
							}
							
							if(data[i].status.indexOf("REQ") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status",'Request');
							}
							
							if(data[i].status.indexOf("CONF") != -1){
								$("#list").jqGrid("setCell", data[i].partnerId, "status",'Confirm');
							}							
						}
					}
					if(!gridTooltip.existTooltip){
						$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_status"))
							.attr("title", gridTooltip.tooltipCont).tooltip({
							content: function () {
								return $(this).prop('title');
							}
						});
						gridTooltip.existTooltip = true;
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					createTabInFrame(rowid+'_3rdParty', '#<c:url value="/partner/edit/'+rowid+'"/>');
				},
				postData : refreshParam
			})
		}
	}
//]]>
</script>
