<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
//<![CDATA[
	/*global $ */
	/*jslint browser: true, nomen: true */
	var lastsel;
	var codeData;
	var userRole = '${sessUserInfo.authority}';
	var arr = new Array();
	var isChangeModelInfo = true;
	var isCheckT = false;
	var modelData;
	var gDateKeyup = false;
	var distributionStatus = '${project.destributionStatus}';
	var etcDomain = "${ct:getConstDef('CD_DTL_ECT_DOMAIN')}";
	var osTypeEtc = "${ct:getConstDef('CD_OS_TYPE_ETC')}";
	var divisionEmptyCd = "${ct:getConstDef('CD_USER_DIVISION_EMPTY')}";
	var completeYn = '${project.completeYn}';
	var verificationStatus = '${project.verificationStatus}';
	var checkPartner = '${project.refPartnerId}';
	var identificationStatusConfFlag = '${project.identificationStatusConfFlag}';
	var verificationStatusConfFlag = '${project.verificationStatusConfFlag}';
	var copyFlag = '${project.copyFlag}';
	
	$(document).ready(function() {
		'use strict';
		data.init();
		initSample();
		initSample2();
		evt.init();

		activeLink();
		if('${project.viewOnlyFlag}' == 'Y') {
            initCKEditorNoToolbar('editor', true);
		}

        var userDivision = $('#division');
        for(var i=0;i<userDivision.children().length;i++){
            if(userDivision.children()[i].value == ${ct:getConstDef('CD_USER_DIVISION_EMPTY')} ) {
                break;
            }
            if(userDivision.children().length - 1 == i ) {
                userDivision.append("<option value='${ct:getConstDef('CD_USER_DIVISION_EMPTY')}' ></option>");
                if('${project.division}' == ${ct:getConstDef('CD_USER_DIVISION_EMPTY')}) {
                    $('#division option:last').attr("selected", "selected");
                    $('#division option:last').change();
                }
            }
        }

        var prjDivision = $("#prjDivision");
        for(var i=0;i<prjDivision.children().length;i++){
            if(prjDivision.children()[i].value == ${ct:getConstDef('CD_USER_DIVISION_EMPTY')} ){
                break;
            }
            if(prjDivision.children().length-1 == i) {
                prjDivision.append("<option value='${ct:getConstDef('CD_USER_DIVISION_EMPTY')}'></option>");
            }
        }

		if('${project.prjId}' != "" && '${project.copyFlag}' != 'Y'){
			$("input[name=creatorNm]").val('${project.prjUserName}');
		}
		
		$('.btnCommentHistory').on('click', function(e){
			e.preventDefault();
			openCommentHistory('<c:url value="/comment/popup/prj/${project.prjId}"/>');
		});
		
		// distribution까지 진행되는 경우
		isChangeModelInfo = "${!(project.destributionStatus ne 'NA' and (not empty project.destributionStatus or project.verificationStatus eq 'CONF') and !empty project.distributeDeployTime)}";

		if(!isChangeModelInfo || "false" == isChangeModelInfo) {
			$("#tr_distribute > td > span.fileex_back").hide();
			$("#allDelete").hide();
			$("#unabledChangeModelTxt").show();
			
		}
		
		showHelpLink("Project_List_BasicInfo_Distribution_type", "helpLink_distributionType");
		showHelpLink("Project_List_BasicInfo_Distribution_Site", "helpLink_distributionSite");
		showHelpLink("Project_List_BasicInfo_Oss_Notice", "helpLink_ossNotice");
		showHelpLink("Project_List_BasicInfo_Priority", "helpLink_priority");
		showHelpLink("Project_List_BasicInfo_Main");
		
		if('${project.destributionStatus}' == 'NA' && '${project.completeYn}' == 'Y'){
			$("#saveModel").show();
		}

		if('Y' == completeYn){
			fn.disabledCompleteRow(true);
			$("#tr_distribute > td > span.fileex_back").hide();
			$("#allDelete").hide();
		}

		// If packaging step equals confirmed, OSS Notice not editable
		if('CONF' == verificationStatus){
			fn.disabledVerificationConfirm(true);
		}

		if(checkPartner != ''){
			$("#noticeType1").attr("disabled", true);
			$("#noticeTypeEtc").attr("disabled", true);
		}

		if(('Y' == copyFlag || 'Y' == identificationStatusConfFlag) && $("[name='noticeType'][value|='80']").is(':checked')){
			$("[name='noticeType']").attr("disabled", true);
			$("#noticeTypeEtc").attr("disabled", true);
		}
	});
	
	// 이벤트
	var evt = {
		init : function(){
            var categoryCd='${ct:getConstDef("CD_MODEL_TYPE")}';
			getCategoryCodeJson(categoryCd);
			modelList.load();
			
			// 왓쳐 추가
			$("#addWatcher").click(function(){
				/* division 정보 */
				var $divSel	= $("#prjDivision"),
					divVal	= $divSel.val(),
					divTxt	= $divSel.find("option[value='"+divVal+"']").text();
				
				/* division 정보 */
				var $userSel	= $("#prjUserId"),
					userVal		= $userSel.val()||"",
					userTxt		= $userSel.find("option[value='"+userVal+"']").text();

				// 선택한 item이 없을 경우.
				if(divVal == "" || userVal == "") {
					return alertify.error('<spring:message code="msg.project.required.selectDivision" />', 0);
				}
				
				/* tag 정보 */
				var isNew = true;
				
				$(".watcherTags").each(function(i, tag){
					var tagDiv = $(tag).val().split("/")[0]
						tagUid = $(tag).val().split("/")[1];
					
					if(divVal == tagDiv) {
						if(tagUid == 'all') { // 선택된 태그 중에 all이 있을 경우 등록 안함
							isNew = false;

							return false;
						}
						
						if(userVal == 'all') { // all을 선택했을 경우 기존 부서의 userId를 가진 tag를 삭제
							$(tag).closest('span').remove();
						} else if(tagUid == userVal) {
							isNew = false;	// 이미 등록된 watcher가 있을경우
						}
					}
				});
				
				if(isNew) {
					var watcherStr = divisionEmptyCd != divVal ? (divTxt + "/" + userVal) : userVal;
					fn.addWatcher(divVal, userVal, '');
					fn.addHtml($("#multiDiv"), watcherStr, divVal, userVal);
				}
			});
			
			// 그리드 클리어
			$("#allDelete").click(function(){
				$("#_modelList").jqGrid('clearGridData');
			});
			
			// 컴플리트 버튼 
			$('#complete').click(function(){
				fn.exeProjectComplete('Y');
			})				
			
			$('#drop').on('click', function(){
				var comment = CKEDITOR.instances.editor2.getData();
				if(comment == ""){
					alertify.alert('<spring:message code="msg.project.confirm.comment" />', function(){});
				}else{
					if(distributionStatus == "PROC"){
						var comment = '<spring:message code="msg.project.distribution.loading" />';
						
						alertify.error(comment, 0);

						return false;
					} else if(distributionStatus == "RSV"){
						var dropMessage = '<spring:message code="msg.project.warn.drop.rsv" />';

						alertify.confirm(dropMessage, function (e) {
							if (e) {
								fn.cancelDistributeReserve();
							} else {
								return false;
							}
						});
					} else if(distributionStatus == "DONE"){
						var dropMessage = '<spring:message code="msg.project.warn.drop.rsv" />';

						alertify.confirm(dropMessage, function (e) {
							if (e) {
								fn.deleteWithOSDD();
							} else {
								return false;
							}
						});
					} else {
						fn.exeProjectDrop();
					}
				}
			});
			
			// 저장
			$("#save").click(function(){
				var confirmMsg = "";
				var ossNotice = $("[name='noticeType']:checked").val();

				if(!isCheckT && ossNotice == "99"){
					confirmMsg  = "OSS 고지문 발급이 필요한 경우에는, 'Cancel' 클릭 후 OSS Notice 및 Distribution type을 다시 확인해주시기 바랍니다.";
					confirmMsg += "<br><br>" + "If it is necessary to generate the OSS Notice, please click 'Cancel' and check the OSS Notice and Distributio type again.";
				} else {
					confirmMsg = '<spring:message code="msg.common.confirm.save" />';
				}

				if('Y' == identificationStatusConfFlag){
					alertify.confirm(confirmMsg, function (e) {
						if (e) {
							fn.showCopyStatusConfirm();
						} else {
							return false;
						}
					});
				}else{
					alertify.confirm(confirmMsg, function (e) {
						if (e) {
							fn.saveSubmit(true);
						} else {
							return false;
						}
					});
				}
			});

			$("#popCopyConfirmSave").click(function(){
				fn.saveSubmit(true);
				$('input:radio[name="confirmStatusCopyRadio"]').prop("checked", false);
				$("#copyConfirmPopup").hide();
			});

			$("#popCopyConfirmCancel").click(function(){
				$('input:radio[name="confirmStatusCopyRadio"]').prop("checked", false);
				$("#copyConfirmPopup").hide();
			});
			
			// 모델 저장
			$("#saveModel").click(function(){
				fn.saveModelSubmit();
			});
			
			// 저장
			$("#reopen").click(function(){
				fn.exeProjectComplete('N');
			});
			
			// 삭제
			$("#delete").click(function(){
				if(data.detail.completeYn == 'Y' || data.detail.identificationStatus == "CONF") {
					alertify.alert('<spring:message code="msg.project.warn.edit.delete" />', function(){});

					return;
				} else {
					var innerHtml = '<div class="grid-container" style="width:470px; height:350px;">Are you sure you want to remove this project?\nThis will permanently delete all datas.';
					innerHtml    += '	<div class="grid-width-100" style="width:470px; height:310px; margin-top:10px;">';
					innerHtml    += '		<div id="editor3" style="width:470px; height:300px;">' + CKEDITOR.instances['editor2'].getData() + '</div>';
					innerHtml    += '	</div>';
					innerHtml    += '</div>';
					
					alertify.confirm(innerHtml, function () {
						if(CKEDITOR.instances['editor3'].getData() == ""){
							alertify.alert('<spring:message code="msg.project.required.comments" />', function(){});

							return false;
						}else{
							fn.deleteSubmit();
						}
					});

					var _editor = CKEDITOR.instances.editor3;
					
					if(_editor) {
						_editor.destroy();
					}
					
					CKEDITOR.replace('editor3', {});
				}
			});
			// 직접입력
			$('#osType').change(function(){
				$("#osType option:selected").each(function () {
					$("#osTypeEtc").val('');
					
					if($(this).val()== osTypeEtc){
						$("#osTypeEtc").attr("disabled",false);
					}else{
						$("#osTypeEtc").attr("disabled",true);
					}
				});

				var selectTxt = $(this).find("option:selected").text();
				var noticeTypeValue = "";
				var noticeTypeEtcValue = "";
				
				// Application Android의 경우는 해당 없음.
				if(/^ANDROID/.test(selectTxt.toUpperCase())){
					// noticeType : platform generated, noticeTypeEtc : android
					noticeTypeValue = "${ct:getConstDef('CD_NOTICE_TYPE_PLATFORM_GENERATED')}";
					noticeTypeEtcValue = "${ct:getConstDef('CD_DTL_DEFAULT_PLATFORM')}";
				} else {
					// default noticeType : general, noticeTypeEtc : ""
					noticeTypeValue = "${ct:getConstDef('CD_NOTICE_TYPE_GENERAL')}";
					noticeTypeEtcValue = "";
				}

				$("[name='noticeType'][value|='"+noticeTypeValue+"']").trigger("click");
				$("#noticeTypeEtc").val(noticeTypeEtcValue).trigger("change");
			});
			
			var code = '';
			<c:forEach var="file" items="${ct:getCodes(ct:getConstDef('CD_FILE_ACCEPT'))}" varStatus="fileStatus">
				<c:if test="${file eq '11'}">
					code = '${ct:getCodeExpString(ct:getConstDef("CD_FILE_ACCEPT"), file)}';
				</c:if>
			</c:forEach> 
			
			$('#modelFile').uploadFile({
				url : '<c:url value="/project/modelFile"/>',
				multiple:false,
				dragDrop:true,
				fileName:'myfile',
				sequential:true,
					allowedTypes:code,
				sequentialCount:1,
				dynamicFormData: function() {
					var data ={ "distributionTarget" :$('input[name=distributeTarget]:checked').val()}

					return data;
				},
				onSuccess:function(files,data,xhr,pd) {
					if(data != null) {
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
						fn.makeModelList(data);	
					} else {
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						$('.ajax-file-upload-statusbar').fadeOut('slow');
						$('.ajax-file-upload-statusbar').remove();
					}
				}
			});
			
			// email 왓쳐 추가
			$("#addEmail").click(function() {
				/* AD ID 정보 */
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
						if("true" == data.isValid) {
							var watcherStr = divisionEmptyCd != data.division ? (data.divisionName + "/" + data.userId) : data.userId;
							fn.addWatcher(data.division, data.userId, '');
							fn.addHtml($("#multiDiv"), watcherStr, data.division, data.userId);
						} else {
							fn.addWatcher('', '', _email);
							fn.addHtml($("#multiDiv"), _email, _email, "Email");
						}
					},
					error: function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
				
				$("#adId").val('');
				$("#domain").val("${ct:getConstDef('CD_DTL_DEFAULT_DOMAIN')}").trigger('change');
			});
	
			$("#identificationTab").click(function(){
				var prjId = $('input[name=prjId]').val();
				var idx = getTabIndex(prjId+"_Identify");
				
				if(idx != ""){
					changeTabInFrame(idx);
				}else{
					createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/4"/>');
				}
			});
			
			$("#packagingTab").click(function(){
				var prjId = $('input[name=prjId]').val();
				var idx = getTabIndex(prjId+"_Packaging");
				
				if(idx != ""){
					changeTabInFrame(idx);
				}else{
					createTabInFrame(prjId+'_Packaging', '#<c:url value="/project/verification/'+prjId+'"/>');
				}
			});
			
			$("#distributionTab, #distributionTabModel").click(function(){
				var prjId = $('input[name=prjId]').val();
				var idx = getTabIndex(prjId+"_Distribute");
				
				if(idx != ""){
					changeTabInFrame(idx);
				}else{
					createTabInFrame(prjId+'_Distribute', '#<c:url value="/project/distribution/'+prjId+'"/>');
				}
			});
			
			$("#editTab").click(function(){
				var prjId = $('input[name=prjId]').val();
				var idx = getTabIndex(prjId+"_Project");
				
				if(idx != ""){
					changeTabInFrame(idx);
				}else{
					createTabInFrame(prjId+'_Project', '#<c:url value="/project/edit/'+prjId+'"/>');
				}
			});
			
			commonAjax.getCreatorDivisionTags().success(function(data, status, headers, config){
				data.forEach(function(obj,index){
					arr[index] = obj.userId+":"+obj.userName;
				});
			});
			
			// project security setting 임시 처리
			if('${project.prjId}' != "" && '${project.viewOnlyFlag}' == 'N'){
				$("[name='publicYn']").on('change',function(e){
					var param = {prjId : '${project.prjId}', publicYn : ($("[name='publicYn']:checked").val())};
					
					$.ajax({
						url : '<c:url value="/project/updatePublicYn"/>',
						type : 'POST',
						data : JSON.stringify(param),
						dataType : 'json',
						cache : false,
						contentType : 'application/json',
						success: function(){
							alertify.success('<spring:message code="msg.common.success" />');
						},
						error : fn.onError
					});
				});
			}
			
			/* 2018-07-19 choye 추가  */
			$("#reqToOpen").click(function(e){
				e.preventDefault();
				
				//prjId로 변경하기
				var commId = $('input[name=commId]').val();
				var statusRequestYn = $('input[name=statusRequestYn]').val();
				
				if(statusRequestYn=="Y" && commId!=null && commId!="") {
					var param = {};
					
					$.ajax({
						url : '<c:url value="/comment/getCommentInfo/'+commId+'"/>',
						type : 'GET',
						dataType : 'json',
						cache : false,
						contentType : 'application/json',
						success: function(data){
							if("${ct:isAdmin()}"=="false" && data.info.creator!="${sessUserInfo.userId}") {
								fn.requestToOpenPop(null);
							} else {
								fn.requestToOpenPop(data.info);
							}
						},
						error : fn.onError
					});
				} else {
					fn.requestToOpenPop(null);
				}
			});
			
			$('#addList').on('click', function(){
				var listKind = $("#listKind").val(),
					listId = $("#listId").val();
					
				if(fn.chkListValidation(listKind, listId)) {
					var obj = {};
					obj["listKind"] = listKind;
					obj["listId"] = listId;
					
					fn.copyWatcher(obj);
				}
			});
			
			$("[name='creatorNm']").on("keyup", function(e) {
				if(e.keyCode != 13) {
					$("[name='creator']").val("");
				}
			});
			
			$(".cal").on("keyup", function(e) {
				calValidation(this, e);
			});

			$("#noticeTypeEtc").on("click", function(e) {
				$("[name='noticeType'][value|='80']").trigger("click");
			});

			$("#domain").on("change", function(e) {
				var value = $(this).find("option:selected").val();
				var domain = $(this).find("option:selected").text();
				
				if(etcDomain == value) {
					$("#emailTemp").val("").show();
				} else {
					$("#emailTemp").val(domain).hide();
				}
			});
		}
	};
	
	var fn = {
		editComment : function() {
			initCKEditorToolbar("editor");
			fn.saveSubmit(false);
		},
		copy : function(){
			var prjId = $('input[name=prjId]').val();
			
			activeDeleteTabInFrame();
			
			createTabInFrame(prjId+'copy_Project', '#<c:url value="/project/copy/'+prjId+'"/>');
		},	
		// 왓쳐 엘리먼트 그리기
		addHtml : function(target, str, division, userId){
			var rlt = division+((userId!="") ? "/"+userId : "");
			var html  = '<span><input class="watcherTags" type="text" name="watchers" value="'+rlt+'" style="display: none;"/>';
			html += '<strong>'+str+'</strong>';
			if('${project.viewOnlyFlag}' != "Y") {
				html += '<input type="button" value="Delete" class="smallDelete" onclick="fn.removeWatcher(\'' + division + '\',\'' + userId + '\');" /></span>';
			}
			target.append(html);

			$('div.multiTxtSet2 .smallDelete').on('click', function(){
				$(this).parent().remove();
			});
		},
		addWatcher : function(uDiv, uId, uEmail) {
			if($('input[name=prjId]').val() == "" || '${project.copyFlag}' == 'Y') {
				return true;
			}
			
			var data = {"prjId" : $('input[name=prjId]').val() , "prjDivision" : uDiv, "prjUserId":uId, "prjEmail":uEmail};
			
			$.ajax({
				url : '<c:url value="/project/addWatcher"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(resultData){
					if(resultData.isValid == "true") {
						alertify.success('<spring:message code="msg.common.success" />');
					}
				},
				error : fn.onError
			});
		},
		removeWatcher : function(uDiv, uId) {
			var uEmail = "";
			if("Email" == uId) {
				uEmail = uDiv;
				uId = "";
				uDiv = "";
			}
			
			if($('input[name=prjId]').val() == "" || '${project.copyFlag}' == 'Y') {
				return true;
			}
			
			var data = {"prjId" : $('input[name=prjId]').val() , "prjDivision" : uDiv, "prjUserId":uId, "prjEmail":uEmail};

			$.ajax({
				url : '<c:url value="/project/removeWatcher"/>',
				type : 'POST',
				data : JSON.stringify(data),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(resultData){
					if(resultData.isValid == "true") {
						alertify.success('<spring:message code="msg.common.success" />');
					}
				},
				error : fn.onError
			});
		},
		copyWatcher : function(obj) {
			var prjId = "${project.prjId}";
			
			obj["prjId"] = prjId;
			
			$.ajax({
				url : '<c:url value="/project/copyWatcher"/>',
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
							  , division = copyWatcher[i].prjDivision || ""
							  , divisionName = copyWatcher[i].prjDivisionName || ""
							  , userId = copyWatcher[i].prjUserId || ""
							  , userName = copyWatcher[i].prjUserName || ""
							  , email = copyWatcher[i].prjEmail || ""
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
								if(email != ""){
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
									
									fn.addHtml($("#multiDiv"), str, division, userId);
								}
							}
						}
						
						if(prjId) {
							alertify.success('<spring:message code="msg.common.success" />');
						}
					}
					
					if(!copyWatcher.length) {
						alertify.warning('<spring:message code="msg.project.required.id" />');
					}
				},
				error : fn.onError
			});
		},
			// 그리드 삭제 버튼
			setDelBtn : function(cellvalue, options, rowObject){
				if(!isChangeModelInfo || "false" == isChangeModelInfo) {
					return "";
				} else {
					return "<input type=\"button\" value=\"delete\" class=\"btnCLight darkgray\" onclick=\"fn.exeDelete('"+options.rowId+"')\" />";
				}
			},
			// 그리드 삭제
			exeDelete : function(rowId){
				$("#_modelList").jqGrid('delRowData', rowId);
			},
			// 모델 데이터 생성
			getModelGridRows : function(elementId){
				var ids = $(elementId).jqGrid('getDataIDs');
				var rows = [];
				
				ids.forEach(function(id){
					$(elementId).jqGrid('saveRow', id);
					var obj = $(elementId).jqGrid('getRowData',id);
					
 					rows.push(obj);
				});
				return rows;
			},
			// 컴플리트 처리
			exeProjectComplete : function(flag){
				var data = {"prjId" : $('input[name=prjId]').val() , "completeYn" : flag, "userComment":CKEDITOR.instances.editor2.getData()};
				
				$.ajax({
					url : '<c:url value="/project/updateProjectStatus"/>',
					type : 'POST',
					data : JSON.stringify(data),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
					success: fn.onRegistSuccess,
					error : fn.onError
				});
			},
			// 컴플리트 처리
			exeProjectDrop : function(){
				var data = {"prjId" : $('input[name=prjId]').val() , "dropYn" : "Y", "userComment":CKEDITOR.instances.editor2.getData()};
				
				$.ajax({
					url : '<c:url value="/project/updateProjectStatus"/>',
					type : 'POST',
					data : JSON.stringify(data),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
					success: fn.onRegistSuccess,
					error : fn.onError
				});
			},
			cancelDistributeReserve : function(){
				var params = {};
				params["prjId"] = $('input[name=prjId]').val();
				params["userComment"] = "";

				$.ajax({
					url : '<c:url value="/project/distribution/distribute/cancel"/>',
					type : "POST",
					dataType: "json",
					cache : false,
					data : params,
					success : function(data){
						if(data.resCd=="10"){
							alertify.alert(data.resMsg, function(){
								this.close();
								
								fn.exeProjectDrop();
							});
						} else {
							alertify.error(data.resMsg, 0);
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			deleteWithOSDD : function(){
				var params = {};
				params["prjId"] = $('input[name=prjId]').val();
				params["userComment"] = "";
				
				$.ajax({
					url : '<c:url value="/project/distribution/distribute/resetWithOSDD"/>',
					type : "POST",
					dataType: "json",
					cache : false,
					data : params,
					success : function(data){
						if(data.resCd=="10"){
							alertify.alert(data.resMsg, function(){
								this.close();
								
								fn.exeProjectDrop();
							});
						} else {
							alertify.error(data.resMsg, 0);
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			// 저장
			saveSubmit : function(reload){
				var prjName = $('input[name=prjName]').val().trim().replace(/[ ]+/g, " "); // ASCII 160 convert -> ASCII 32
				$('input[name=prjName]').val(prjName);
				$('input[name=prjVersion]').val($('input[name=prjVersion]').val().trim());

				var editorVal = CKEDITOR.instances.editor.getData();
				$('input[name=comment]').val(editorVal);

				var editorVal2 = CKEDITOR.instances.editor2.getData();
				$('input[name=userComment]').val(editorVal2);
				cleanErrMsg("_modelList");

				var rows = fn.getModelGridRows('#_modelList');
				$('input[name=prjModelJson]').val(JSON.stringify(rows));
				
				//disabled일경우 제거
				if($("input[name=distributeTarget]").is(":disabled")){
					$("input[name=distributeTarget]").removeAttr("disabled");
				}

				if ('Y' == completeYn){
					fn.disabledCompleteRow(false);
				}

				if((('Y' == copyFlag || 'Y' == identificationStatusConfFlag) && $("[name='noticeType'][value|='80']").is(':checked'))
						|| 'CONF' == verificationStatus){
					fn.disabledVerificationConfirm(false);
				}
				
			<c:if test="${ct:isAdmin() and not empty project.prjId and 'Y' ne project.copyFlag}">
				var creator = $("input[name=creator]").val();
				if(creator == ""){
					alertify.alert('<spring:message code="msg.project.required.autocomplete" />', function(){});
					
					return false;
				}
			</c:if>
				
				var URL = '';

				var confirmStatusCopyFlag = true;
				var confirmStatusCopyVal = "";
				if($('input:radio[name="confirmStatusCopyRadio"]').is(':checked')){
					confirmStatusCopyVal = $('input:radio[name="confirmStatusCopyRadio"]:checked').val();
					fn.disabledVerificationConfirm(false);
				}else{
					confirmStatusCopyFlag = false;
				}
				
				if(data.copy){
					if(confirmStatusCopyFlag){
						URL = '<c:url value="/project/saveAjax?copy=true&confirmStatusCopy='+confirmStatusCopyVal+'"/>';
					} else {
						URL = '<c:url value="/project/saveAjax?copy=true&confirmStatusCopy=false"/>';
					}
				} else {
					URL = '<c:url value="/project/saveAjax?copy=false&confirmStatusCopy=false"/>';
				}
				
				//public 값 넣어주기
				if($('#checkbox3').is(':checked')) {
					$('#checkbox3').val('N');
				} else {
					$('#checkbox3').val('Y');
				}
				
				$("#projectForm").ajaxForm({
					url : URL,
					type : 'POST',
					dataType: "json",
					cache : false,
					success: function(data) {
						if(reload) {
							fn.onRegistSuccess(data);
						}
						else {
							alertify.success('<spring:message code="msg.common.success" />');
						}
					},
					error : fn.onError
				}).submit();
			},
			// 삭제
			deleteSubmit : function(){
				var editorVal = CKEDITOR.instances.editor3.getData();
				$('input[name=userComment]').val(editorVal);
				
				$("#projectForm").ajaxForm({
					url :'<c:url value="/project/delAjax"/>',
					type : 'POST',
					dataType:"json",
					cache : false,
					success:fn.onDeleteSuccess,
					error : fn.onError
				}).submit();
			},
			// 저장
			saveModelSubmit : function(){
				var rows = fn.getModelGridRows('#_modelList');
				$('input[name=prjModelJson]').val(JSON.stringify(rows));
				
				$("#projectForm").ajaxForm({
					url : '<c:url value="/project/saveModelAjax"/>',
					type : 'POST',
					dataType: "json",
					cache : false,
					success: fn.onRegistSuccess,
					error : fn.onError
				}).submit();
			},
			// 성공 콜백
			onRegistSuccess : function(json, status){
				if ('Y' == completeYn){
					fn.disabledCompleteRow(true);
				}

				if((('Y' == copyFlag || 'Y' == identificationStatusConfFlag) && $("[name='noticeType'][value|='80']").is(':checked'))
						|| 'CONF' == verificationStatus){
					fn.disabledVerificationConfirm(true);
				}
				
				if(json.isValid == 'false') {
					var _errMsg = '<spring:message code="msg.common.valid" />';
					
					if(json.validMsg && json.validMsg != "") {
						_errMsg += "<br/>" + json.validMsg;
					}
					
					alertify.error(_errMsg, 0);
					gridListBulkEdit("_modelList", pickdates);
					createValidMsgComplex(json);
				}else if(json.isValid == 'true') {
					if(typeof json.resultData !== "undefined" && typeof json.resultData.confirmCopyStatusSuccess !== "undefined"){
						if(json.resultData.confirmCopyStatusSuccess == 'false'){
							var msg;
							if(typeof json.resultData.confirmCopyStatusFail !== "undefined"){
								var confirmCopyStatusFail = json.resultData.confirmCopyStatusFail;
								
								if("identification" == confirmCopyStatusFail){
									msg = '<spring:message code="msg.project.copy.confirm.status.fail.identification" />';
								}else if("verification" == confirmCopyStatusFail){
									msg = '<spring:message code="msg.project.copy.confirm.status.fail.verification" />';
								}
							}else{
								msg = '<spring:message code="msg.common.success" />';
							}

							alertify.alert(msg, function(){
								reloadTabInframe('<c:url value="/project/list"/>');
								
								deleteTabInFrame('#<c:url value="/project/copy/'+data.copy.prjId+'"/>');
								
								activeTabInFrameList("PROJECT");
							});
						}else if(json.resultData.confirmCopyStatusSuccess == 'true'){
							alertify.alert('<spring:message code="msg.common.success" />', function(){
								reloadTabInframe('<c:url value="/project/list"/>');
								
								deleteTabInFrame('#<c:url value="/project/copy/'+data.copy.prjId+'"/>');
								
								activeTabInFrameList("PROJECT");
							});
						}
					}else{
						var prjId = $('input[name=prjId]').val();
						
						if(data.copy) {
							alertify.alert('<spring:message code="msg.common.success" />', function(){
								reloadTabInframe('<c:url value="/project/list"/>');
								
								deleteTabInFrame('#<c:url value="/project/copy/'+data.copy.prjId+'"/>');
								
								activeTabInFrameList("PROJECT");
							});
						} else {
							alertify.alert('<spring:message code="msg.common.success" />', function(){
								reloadTabInframe('<c:url value="/project/list"/>');
								
								if(prjId == '') {
									deleteTabInFrame('#<c:url value="/project/edit"/>');
									activeTabInFrameList("PROJECT");
								} else {
									var status = data.detail.destributionStatus;
									var flag = "false";
									
									if (json.resultData) {
										flag = json.resultData.isAdd;
									}
									
									if(status == "DONE" && flag == "true") {
										alertify.confirm('<spring:message code="msg.project.required.only" />', function () {
												deleteTabInFrame('#<c:url value="/project/edit/'+prjId+'"/>');
												createTabInFrame(prjId+'_Distribute', '#<c:url value="/project/distribution/'+prjId+'"/>');
											}, function() {
												deleteTabInFrame('#<c:url value="/project/edit/'+prjId+'"/>');
												activeTabInFrameList("PROJECT");
											}
										);
									} else {
										deleteTabInFrame('#<c:url value="/project/edit/'+prjId+'"/>');
										activeTabInFrameList("PROJECT");
									}
								}
							});	
						}
					}
				}
			},
			// 삭제 콜백
			onDeleteSuccess : function(json, status) {
				loading.hide();
				var prjId = $('input[name=prjId]').val();
				
				if(json.resCd=='10') {
					reloadTabInframe('<c:url value="/project/list"/>');
					
					if(data.copy) {
						alertify.alert('<spring:message code="msg.common.success" />', function(){
							deleteTabInFrame('#<c:url value="/project/copy/'+data.copy.prjId+'"/>');
						});
					} else {
						alertify.alert('<spring:message code="msg.common.success" />', function(){
							if(prjId){
								deleteTabInFrame('#<c:url value="/project/edit/'+prjId+'"/>');
							} else {
								deleteTabInFrame('#<c:url value="/project/edit"/>');
							}
						});
					}
				} else {
					alertify.error('<spring:message code="msg.common.valid2" />', 0)
				}
			},
			// 에러 콜백
			onError : function(data, status) {
				if ('Y' == completeYn){
					fn.disabledCompleteRow(true);
				}
				
				if((('Y' == copyFlag || 'Y' == identificationStatusConfFlag) && $("[name='noticeType'][value|='80']").is(':checked'))
						|| 'CONF' == verificationStatus){
					fn.disabledVerificationConfirm(true);
				}
				
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			},
			selectDivision : function() {
				var division = $('#prjDivision').val();
				$('#prjUserId').children().remove();
				if(division == "") {
					$('#prjUserId').val("").prev().text("select User").change();
					return false;
				}
				$('#prjUserId').attr('disabled', false);
				$.ajax({
					url : '<c:url value="/partner/getUserList"/>',
					type : 'GET',
					dataType : 'json',
					cache : false,
					data : {'division' : division},
					success : function(data){
						data.forEach(function(obj){
							$('#prjUserId').append('<option value='+obj.userId+'>'+obj.userName+'('+obj.userId+')</option>');
						});
						
						$('#prjUserId').change();
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			makeModelList : function(data){
				modelData = data.currentModelList;

				// 리로드 대신 그리드 삭제 후 다시 그리기
				$("#_modelList").jqGrid('GridUnload');
				
				modelList.load();

			},
			downloadModelList : function(){
				cleanErrMsg("_modelList");
				var data = fn.getModelGridRows('#_modelList');
				
				$.ajax({
					type: "POST",
					url: '<c:url value="/exceldownload/getExcelPost"/>',
					data: JSON.stringify({"type":"model", "parameter":JSON.stringify(data), "extParam" : $('input[name=distributeTarget]:checked').val()}),
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
			checkUserId : function(userNm){
				var userId = "";
				
				for(var i=0; i < arr.length; i++){
					if(arr[i].indexOf(userNm) > 0){
						var tmp = arr[i].split(":");
						
						if(tmp[1] == userNm){
							userId = tmp[0];
						}
					}
				}
				
				return userId;
			},
			sendEditor : function(type){
				//코멘트 저장
				var editorVal = CKEDITOR.instances.editor2.getData();
				
				if(!editorVal || editorVal == "") {
					alertify.alert("<spring:message code="msg.project.enter.comment" />", function(){});
					
					return false;
				}
				
				var param = {referenceId : '${project.prjId}', referenceDiv :'19', contents : replaceWithLink(editorVal), mailSendType : type};
				
				$.ajax({
					url : '<c:url value="/project/sendComment"/>',
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : param,
					success : function(json){
						if(json.isValid == 'false'){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							$('.ajs-close').trigger("click");
							alertify.success('<spring:message code="msg.project.sent.comments.success" />');
							resetEditor(CKEDITOR.instances.editor2);
							
							$(".commentBtn open").trigger( "click" );
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			saveEditor : function(){
				//코멘트 임시저장
				var editorVal = CKEDITOR.instances.editor2.getData();
				var register = '${sessUserInfo.userId}';
				var param = {referenceId : '${project.prjId}', referenceDiv :'09', contents : editorVal};
				
				$.ajax({
					url : '<c:url value="/project/saveComment"/>',
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : param,
					success : function(json){
						if(json.isValid == 'false'){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							alertify.success('<spring:message code="msg.common.success" />');
							
							$(".commentBtn.open").trigger( "click" );
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			editorDialog : function(){
				var editorVal = CKEDITOR.instances.editor2.getData();
				if(!editorVal || editorVal == "") {
					alertify.alert("<spring:message code="msg.project.enter.comment" />", function(){});
					
					return false;
				}
				//launch it.
				var btnHtm = '<br/><b>Send an email to</b><br/>';
				btnHtm += '<input type="button" value="Watcher Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'W\')"/>&nbsp;&nbsp;&nbsp;';

				if(userRole == "ROLE_ADMIN"){
					btnHtm += '<input type="button" value="Creator Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'C\')"/>&nbsp;&nbsp;&nbsp;';
				}else{
					btnHtm += '<input type="button" value="Reviewer Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="fn.sendEditor(\'R\')"/>&nbsp;&nbsp;&nbsp;';
				}
				
				btnHtm +='<input type="button" value="All" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="fn.sendEditor(\'WR\')"/>&nbsp;&nbsp;&nbsp;';

				if(!alertify.myAlert){
					//define a new dialog
					alertify.dialog('myAlert',function factory(){
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
					}});
				}
				
				alertify.myAlert(btnHtm);
			},
			requestToOpenPop : function(info){
				var type = "";
				var commId = "";
				<c:if test="${project.viewOnlyFlag eq 'N'}">
					var readOnly = false;
					var mode = null;
					var height = "150px";
					
					if(info==null) {
						mode = "insert";
					} else if("${ct:isAdmin()}"=="true") {
						commId = info.commId;
						mode = "reject";
					} else {
						commId = info.commId;
						mode = "update";
					}
					
					var innerHtml = '<div class="grid-container" style="width:560px; height:<c:if test="${ct:isAdmin()}">690</c:if><c:if test="${ct:isAdmin() eq false}">340</c:if>px;">';
					innerHtml    += '	<input type="hidden" id="commentsMode" name="commentsMode" value="'+mode+'"/>';
					innerHtml    += '	<input type="hidden" id="commIdPopup" name="commIdPopup" value="'+commId+'"/>';
					innerHtml    += '	<div class="grid-width-100" style="width:560px; height:<c:if test="${ct:isAdmin()}">370</c:if><c:if test="${ct:isAdmin() eq false}">300</c:if>px; margin-top:10px;"><c:if test="${ct:isAdmin() eq false}">Are you sure you want to reject the request?</c:if>';
					innerHtml    += '		<div id="editor3" style="width:560px; height:150px;">';

					if(info!=null && info.contents!=null) {
						innerHtml    += info.contents;
					}
					
					innerHtml    += '</div>';
					innerHtml    += '	<div class="grid-width-100" style="width:560px;height:20px;margin-top:10px;">';

					if(info!=null && info.referenceDiv!=null && info.referenceDiv=="${ct:getConstDef('CD_DTL_COMMENT_IDENTIFICAITON_HIS')}") {
						innerHtml    += '		<span class="radioSet"><input type="radio" name="referenceDiv" value="${ct:getConstDef('CD_DTL_COMMENT_IDENTIFICAITON_HIS')}" id="referenceDiv1" checked="checked"/><label for="referenceDiv1">Identification</label></span>';
					} else {
						innerHtml    += '		<span class="radioSet"><input type="radio" name="referenceDiv" value="${ct:getConstDef('CD_DTL_COMMENT_IDENTIFICAITON_HIS')}" id="referenceDiv1"/><label for="referenceDiv1">Identification</label></span>';
					}
					
					<c:if test="${project.verificationStatus ne null && project.verificationStatus ne 'NA'}">
						if(info!=null && info.referenceDiv!=null && info.referenceDiv=="${ct:getConstDef('CD_DTL_COMMENT_PACKAGING_HIS')}") {
							innerHtml    += '		<span class="radioSet"><input type="radio" name="referenceDiv" value="${ct:getConstDef('CD_DTL_COMMENT_PACKAGING_HIS')}" id="referenceDiv2" checked="checked"/><label for="referenceDiv2">Packaging</label></span>';
						} else {
							innerHtml    += '		<span class="radioSet"><input type="radio" name="referenceDiv" value="${ct:getConstDef('CD_DTL_COMMENT_PACKAGING_HIS')}" id="referenceDiv2"/><label for="referenceDiv2">Packaging</label></span>';
						}
					</c:if>
					
					var hasDistribution = '${not empty project.distributeOsdKey}';
					
					if(hasDistribution == "true") {
						<c:if test="${ct:isAdmin() eq false}">
						if(info!=null && info.referenceDiv!=null && info.referenceDiv=="${ct:getConstDef('CD_DTL_COMMENT_DISTRIBUTION_HIS')}") {
							innerHtml    += '		<span class="radioSet"><input type="radio" name="referenceDiv" value="${ct:getConstDef('CD_DTL_COMMENT_DISTRIBUTION_HIS')}" id="referenceDiv3" checked="checked"/><label for="referenceDiv3">Distribution</label></span>';
						} else {
							innerHtml    += '		<span class="radioSet"><input type="radio" name="referenceDiv" value="${ct:getConstDef('CD_DTL_COMMENT_DISTRIBUTION_HIS')}" id="referenceDiv3"/><label for="referenceDiv3">Distribution</label></span>';
						}
						</c:if>
						<c:if test="${ct:isAdmin()}">
							innerHtml    += '		<span class="radioSet"><input type="radio" name="referenceDiv" value="" id="referenceDiv4"/><label for="referenceDiv4">Not selected</label></span>';
						</c:if>
						<c:if test="${ct:isAdmin()}">
							innerHtml    += '	</div>';
							innerHtml    += '	<div class="grid-width-100" style="width:560px;height:20px;">';
							innerHtml    += '		<span class="radioSet"><input type="radio" name="deleteOsdd" value="N" id="deleteOsdd1" checked="checked"/><label for="deleteOsdd1">Distribution</label></span>';
							innerHtml    += '		<span class="radioSet"><input type="radio" name="deleteOsdd" value="Y" id="deleteOsdd2"/><label for="deleteOsdd2">Distribution(Delete With OSDD)</label></span>';
						</c:if>
					}
					
					innerHtml    += '	</div>';
					<c:if test="${ct:isAdmin()}">
						innerHtml    += '	<div class="grid-width-100" style="width:560px;height:40px;">';
						innerHtml    += 'Creator : ' + info.creatorDivisionName + ' > ' + info.creatorName + '(' + info.creator + ')<br/>';
						innerHtml    += 'Created Date : ' + info.createdDate;
						innerHtml    += '</div>';
					</c:if>
					innerHtml    += '	</div>';
					<c:if test="${ct:isAdmin()}">
						readOnly = true;
						height = "150px";
						innerHtml    += '		<div id="editor4" style="width:560px; height:150px;"></div>';
					</c:if>
					innerHtml    += '</div>';
					if(!alertify.rejectConfirm){
						alertify.dialog('rejectConfirm', function() {
							var settings;
							
							return {
								setup: function() {
									var settings = alertify.confirm().settings;
									
									for (var prop in settings) {
										this.settings[prop] = settings[prop];
									}
									
									var setup = alertify.confirm().setup();
									
									<c:if test="${ct:isAdmin()}">
									setup.buttons.push({ 
										text: 'IGNORE',scope:'auxiliary',className:'ajs-warning'
									});
									
									setup.focus.element = 1;
									</c:if>
									
									return setup;
								},
								settings: {
									oncontinue: null
								},
								callback: function(closeEvent) {
									<c:if test="${ct:isAdmin()}">
									if (closeEvent.index == 2) {
										if(CKEDITOR.instances['editor4'].getData() == "") {
											alertify.alert('<spring:message code="msg.project.required.comments.reject.ignore" />', function(){});

											closeEvent.cancel = true;
										} else {
											fn.commentsIgnore();
										}
									} else {
										alertify.confirm().callback.call(this, closeEvent);
									}
									</c:if>
									
									<c:if test="${ct:isAdmin() eq false}">
									alertify.confirm().callback.call(this, closeEvent);
									</c:if>
								}
							};
						}, false, 'confirm');
					}
					alertify.rejectConfirm(innerHtml,function(){
						var referenceDiv = $(':radio[name="referenceDiv"]:checked').val();
						var deleteOsdd = $(':radio[name="deleteOsdd"]:checked').val();
						var commentsMode = $('#commentsMode').val();
						var commId = $('#commIdPopup').val();
						var matilType = "";
						
						if(commentsMode=="insert" || commentsMode=="update") {
							if(CKEDITOR.instances['editor3'].getData() == ""){
									alertify.alert('<spring:message code="msg.project.required.comments.reject" />', function(){});
									return false;
							}
							
							if(referenceDiv==undefined) {
								alertify.alert('<spring:message code="msg.project.required.rejectDiv" />', function(){});
								return false;
							}
						} else if(commentsMode=="reject") {
							if(referenceDiv==undefined){
								if(deleteOsdd==undefined){
									alertify.alert('<spring:message code="msg.project.required.rejectDiv" />', function(){});

									return false;
								}
							} else {
								var hasDistribution = '${not empty project.distributeOsdKey}';
								if(hasDistribution == "true") {
									if(deleteOsdd==undefined){
										alertify.alert('<spring:message code="msg.project.required.rejectDiv" />', function(){});

										return false;
									}
								}
							}

							if(CKEDITOR.instances['editor4'].getData() == ""){
								alertify.alert('<spring:message code="msg.project.required.comments.reject.ok" />', function(){});

								return false;
							}
						}
						
						var data;
						if(commentsMode=="reject"){
							if(referenceDiv!=null){
								if(referenceDiv=="${ct:getConstDef('CD_DTL_COMMENT_IDENTIFICAITON_HIS')}") {
									data = {"prjId" : '${project.prjId}', "identificationStatus" : "PROG", "userComment" : CKEDITOR.instances['editor4'].getData(), "referenceDiv": referenceDiv, "completeYn" : 'N', "commentsMode" : commentsMode, "delOsdd" : deleteOsdd, "commId" : null, "statusRequestYn" : "N"};
								} else if(referenceDiv=="${ct:getConstDef('CD_DTL_COMMENT_PACKAGING_HIS')}") {
									data = {"prjId" : '${project.prjId}', "verificationStatus" : "PROG", "useCustomNoticeYn" : '${project.useCustomNoticeYn}', "userComment" : CKEDITOR.instances['editor4'].getData(), "referenceDiv": referenceDiv, "completeYn" : 'N', "commentsMode" : commentsMode, "delOsdd" : deleteOsdd, "commId" : null, "statusRequestYn" : "N"};
								} else if(referenceDiv=="${ct:getConstDef('CD_DTL_COMMENT_DISTRIBUTION_HIS')}") {
									data = {"referenceId" : '${project.prjId}', "contents" : CKEDITOR.instances['editor4'].getData(), "referenceDiv": referenceDiv, "commentsMode" : commentsMode, "delOsdd" : deleteOsdd};
								} else if(referenceDiv=="") {
									data = {"referenceId" : '${project.prjId}', "contents" : CKEDITOR.instances['editor4'].getData(), "referenceDiv": referenceDiv, "commentsMode" : commentsMode, "delOsdd" : deleteOsdd};
								}
							} else {
								data = {"referenceId" : '${project.prjId}', "contents" : CKEDITOR.instances['editor4'].getData(), "referenceDiv": referenceDiv, "commentsMode" : commentsMode, "delOsdd" : deleteOsdd};
							}
						} else {
							data = {"referenceId" : '${project.prjId}', "contents" : CKEDITOR.instances['editor3'].getData(), "referenceDiv" : referenceDiv, "commId" : commId, "commentsMode" : commentsMode};
						}
						
						fn.commentsSave(data);
					}); 
					
					var _editor = CKEDITOR.instances.editor3;
					
					if(_editor) {
						_editor.destroy();
					}
					
					CKEDITOR.replace('editor3', {height:height, readOnly:readOnly});
					
					<c:if test="${ct:isAdmin()}">
						$('input[name=referenceDiv]').on('change',function(e){
							
							if(e.target.value == '${ct:getConstDef("CD_DTL_COMMENT_DISTRIBUTION_HIS")}') {
								$(".deleteOsddSet").show();
							} else {
								$(".deleteOsddSet").hide();
								$("#deleteOsdd").attr("checked" , false );
							}
						});
						
						if(info.referenceDiv == '${ct:getConstDef("CD_DTL_COMMENT_DISTRIBUTION_HIS")}') {
							$(".deleteOsddSet").show();
						} else {
							$(".deleteOsddSet").hide();
						}
					
						_editor = CKEDITOR.instances.editor4;
						if(_editor) {
							_editor.destroy();
						}
						
						CKEDITOR.replace('editor4', {autoGrow_maxHeight:200});
					</c:if>
				</c:if>

				$(".ajs-dialog").css("max-width", "590px");
			},
			commentsSave : function(data){
				/* 2018-07-19 choye 추가  */
				if(data.commentsMode=="insert" || data.commentsMode=="update"){
					$.ajax({
						url : '<c:url value="/project/commentsSave"/>',
						type : 'POST',
						dataType : 'json',
						cache : false,
						data : data,
						success : function(json){
							if(json.isValid == 'false'){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							} else {
								alertify.alert('<spring:message code="msg.common.success" />', function(){
									reloadTabInframe('<c:url value="/project/list"/>');
									deleteTabInFrame('#<c:url value="/project/edit/${project.prjId}"/>');
									activeTabInFrameList("PROJECT");
								});
							}
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
				} else {
					//identification, packing reject
					if(data.identificationStatus != null || data.verificationStatus != null) {
						$.ajax({
							url : '<c:url value="/project/updateProjectStatus"/>',
							type : 'POST',
							data : JSON.stringify(data),
							dataType : 'json',
							cache : false,
							contentType : 'application/json',
							success: function(data){
								reloadTabInframe('<c:url value="/project/list"/>');
								deleteTabInFrame('#<c:url value="/project/edit/${project.prjId}"/>');
								activeTabInFrameList("PROJECT");
							},
							error : function(){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					} else {
						$.ajax({
							url : '<c:url value="/project/commentsSave"/>',
							type : 'POST',
							dataType : 'json',
							cache : false,
							data : data,
							success : function(json) {
								if(json.isValid == 'false') {
									alertify.error('<spring:message code="msg.common.valid2" />', 0);
								} else {
									alertify.alert('<spring:message code="msg.common.success" />', function(){
										reloadTabInframe('<c:url value="/project/list"/>');
										deleteTabInFrame('#<c:url value="/project/edit/${project.prjId}"/>');
										activeTabInFrameList("PROJECT");
									});
								}
							},
							error : function() {
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					}
				}
			},
			commentsIgnore : function(){
				/* 2018-07-30 choye 추가 */
				var data = {"referenceId" : '${project.prjId}', "contents" : CKEDITOR.instances['editor4'].getData()};

				$.ajax({
					url : '<c:url value="/project/commentsIgnore"/>',
					type : 'POST',
					dataType : 'json',
					cache : false,
					data : data,
					success : function(json) {
						if(json.isValid == 'false') {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							alertify.alert('<spring:message code="msg.common.success" />', function(){
								reloadTabInframe('<c:url value="/project/list"/>');
								deleteTabInFrame('#<c:url value="/project/edit/${project.prjId}"/>');
								activeTabInFrameList("PROJECT");
							});
						}
					},
					error : function() {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			chkListValidation : function(listKind, listId){
				if(listKind == ""){
					// TODO : 추후 문구 수정예정
					alertify.error('<spring:message code="msg.project.watcher.selectlist" />', 0);

					return false;
				}
				
				if(listId == ""){
					// TODO : 추후 문구 수정예정
					alertify.error('<spring:message code="msg.project.watcher.required.copyid" />', 0);

					return false;
				}
				
				return true;
			},
			chgOssNotice : function(DetailCd){
				//distribution type의 detail description이 T일 경우 check하도록 변경
				if("T" == DetailCd){
					isCheckT = true;
					
					$("[name='noticeType'][value|='${ct:getConstDef('CD_NOTICE_TYPE_NA')}']").trigger("click");
				} else {
					isCheckT = false;
					
					$("[name='noticeType'][value|='${ct:getConstDef('CD_NOTICE_TYPE_GENERAL')}']").trigger("click");
				}
			},
			CheckChar : function(){
				if(event.keyCode == 64){//@ 특수문자 체크
            				alertify.alert('<spring:message code="msg.login.check.char" />', function(){});
            		
            				event.returnValue = false;
            	}
			},
			shareUrl : function(){
				var copyUrl = "";
				var protocol = window.location.protocol;
				var host =  window.location.host;
				copyUrl = protocol + "//" + host + "/project/view/${project.prjId}";
				$("#copyUrl").val(copyUrl);
				
				//launch it.
				var btnHtm = '<b>Share Link</b><br>';
				btnHtm += '<input type="text" value="'+copyUrl+'" style="width:460px;" disabled/><br><br>';
				btnHtm += '<input type="button" value="Copy" class="btnCancel btnColor red right" style="height:30px;width:100px;"onclick="fn.copyUrl(this)"/>';

				if(!alertify.myAlert){
					//define a new dialog
					alertify.dialog('myAlert',function factory(){
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
					}});
				}
				
				alertify.myAlert(btnHtm);
			},
			copyUrl : function(target){
				var copyUrl = document.getElementById( 'copyUrl' );
				copyUrl.select();
				document.execCommand( 'Copy' );
		        
				$('.ajs-close').trigger("click");
				alertify.success('<spring:message code="msg.common.success" />');
			},
			disabledCompleteRow : function(disabledBoolean){
				var diabledNameList = ['prjName', 'osType', 'osTypeEtc', 'distributionType', 'networkServerType', 'distributeTarget', 'noticeType', 'noticeTypeEtc', 'priority', 'creatorNm', 'reviewer'];
				diabledNameList.forEach(function(names){$('input[name='+names+'], select[name='+names+']').attr('disabled', disabledBoolean)});
			},
			disabledVerificationConfirm : function(flag){
				$('input[name=noticeType]').attr('disabled', flag);
				$('select[name=noticeTypeEtc]').attr('disabled', flag);
			},
			showCopyStatusConfirm : function(){
				var identificationStatusConfFlag = $("#identificationStatusConfFlag").val();
				var verificationStatusConfFlag = $("#verificationStatusConfFlag").val();

				if('Y' != identificationStatusConfFlag){
					$("#CSIdentificationConfirm > input[type=radio]").attr("disabled", true);
				}

				if('Y' != verificationStatusConfFlag){
					$("#CSVerificationConfirm > input[type=radio]").attr("disabled", true);
				}

				$("#CSIdentificationProgress > input[type=radio]").prop("checked", true);
				$("#copyConfirmPopup").show();
			}
		}
	
	// 데이타
	var data = {
		modelValues:'',
		detail : ${empty detail ? 'null':detail},
		copy : ${empty copy ? 'null' :copy},
		init : function(){
			if(data.detail){
				$('input[name=prjId]').val(data.detail.prjId);
				$('input[name=prjName]').val(data.detail.prjName.trim());
				$('input[name=prjVersion]').val(data.detail.prjVersion.trim());
				$('select[name=osType]').val(data.detail.osType).trigger('change');
				$('select[name=division]').trigger('change');
				$("[name='distributionType']:checked").trigger("click");
				$("[name='noticeType'][value|='"+data.detail.noticeType+"']").trigger("click");
				
				if(data.detail.osType == osTypeEtc){
					$('#osTypeEtc').attr("disabled",false);
					$('#osTypeEtc').val(data.detail.osTypeEtc);
				}
				
				// 참조코드값이 삭제된 경우, change 이벤트 에서 val가 null로 등록되기 때문에 default로 다시 선택해준다.
				if($('select[name=osType]').val() == null) {
					$('select[name=osType]').val("").trigger('change');
				}

				$('#editor').css("width", $(".miCase").width());
				$('#editor').html(data.detail.comment);
				$('select[name=category]').val(data.detail.category).trigger('change');
				$('select[name=prjDivision]').val(data.detail.prjDivision).trigger('change');
				//$('select[name=prjUserId]').val(data.detail.prjUserId).trigger('change');
				$("#priority").trigger("change");
				$("#noticeTypeEtc").trigger("change");
				
				data.detail.watcherList.forEach(function(watcher, index, obj){
					var deptUseYn = watcher.deptUseYn || "Y";
					var userUseYn = watcher.userUseYn || "Y";
					
					if(watcher!=""){
						if(watcher.prjEmail){ 
							fn.addHtml($("#multiDiv"), watcher.prjEmail, watcher.prjEmail, "Email");
						} else {
							var str = "";
							
							if(watcher.prjUserName == undefined){
								str = watcher.prjDivisionName;
							} else {
								str = '<b';
								
								if(divisionEmptyCd != watcher.prjDivision) {
									if(deptUseYn == "N"){
										str += ' class="deleteUser"';
									}
									
									str += '>'+watcher.prjDivisionName+'</b>/<b';
								}
								
								if(userUseYn == "N"){
									str += ' class="deleteUser"';
								}
								
								str += '>'+watcher.prjUserName+'</b>';
							}
							
							fn.addHtml($("#multiDiv"), str, watcher.prjDivision, watcher.prjUserId);
						}
					}
				});
				
				// Complete 버튼 처리 
				var status = ["PROG","REQ","REV"];
				var complete = false;
				var drop = false;

				if('${sessUserInfo.authority}'=="ROLE_ADMIN" 
					&& data.detail.identificationStatus=="CONF" 
					&& (!data.detail.verificationStatus 
							|| data.detail.verificationStatus == "" 
							|| data.detail.verificationStatus == "PROG" 
							|| data.detail.verificationStatus=="CONF" 
							|| data.detail.verificationStatus=="NA") 
					&& data.detail.completeYn != 'Y') {
					complete = true;
				}

				if(data.detail.dropYn != 'Y'
					&& data.detail.completeYn != 'Y') {
					drop = true;
				}

				var date = new Date();
				var year  = date.getFullYear();
				var month = date.getMonth() + 1;
				var day   = date.getDate();
				
				if (("" + month).length == 1) { 
					month = "0" + month;
				}
				
				if (("" + day).length   == 1) {
					day   = "0" + day;
				}
				
				var currentYmd = parseInt(""+year+month+day);
				 
				$("#copy").show();
				
				complete ? $("#complete").show() : $("#complete").hide();
				drop ? $("#drop").show() : $("#drop").hide();
				
				$('input[name=distributeTarget]').each(function(){
					var value = $(this).val();
					
					if(value == data.detail.distributeTarget){
						$(this).trigger('click');
					}
				});
				
				/* 2018-07-19 choye 추가 */
				$('input[name=commId]').val(data.detail.commId);
				$('input[name=statusRequestYn]').val(data.detail.statusRequestYn);
			} else if(data.copy) {
				$('input[name=prjId]').val(data.copy.prjId);
				$('select[name=osType]').val(data.copy.osType).trigger('change');
				$("[name='distributionType']:checked").trigger("click");
				$("[name='noticeType'][value|='"+data.copy.noticeType+"']").trigger("click");
				
				if(data.copy.osType == osTypeEtc) {
					$('#osTypeEtc').attr("disabled",false);
					$('#osTypeEtc').val(data.copy.osTypeEtc);
				}
				
				// 참조코드값이 삭제된 경우, change 이벤트 에서 val가 null로 등록되기 때문에 default로 다시 선택해준다.
				if($('select[name=osType]').val() == null) {
					$('select[name=osType]').val("").trigger('change');
				}

				$('#editor').css("width", $(".miCase").width());
				$('#editor').html(data.copy.comment);
				$('textarea[name=comment]').val(data.copy.comment);
				$('select[name=category]').val(data.copy.category).trigger('change');
				$('select[name=prjDivision]').val(data.copy.prjDivision).trigger('change');
				$('select[name=prjUserId]').val(data.copy.prjUserId).trigger('change');
				$("#priority").trigger("change");
				$("#noticeTypeEtc").trigger("change");
				
				$('input[name=distributeTarget]').each(function() {
					var value = $(this).val();
					if(value == data.copy.distributeTarget) {
						$(this).trigger('click');
					}
				});
			} else {
				$("#complete").hide(); 
				$("#copy").hide();
				$("#priority").trigger("change");
				$("#noticeTypeEtc").trigger("change");
			}
			
			if(data.copy) {
				var copyParam = {prjId : $('input[name=prjId]').val(), copy : 'Y'}
				data.getModelGridData(copyParam);
			} else {
				data.getModelGridData();
			}
			
			var _distributeTargetSelectedVal = "";
			$('input[name=distributeTarget]').mouseup(function(){
				_distributeTargetSelectedVal = $('input[name=distributeTarget]:checked').val();
			});
		},
		getModelGridData : function(param){
			$.ajax({
				url:'<c:url value="/project/modellistAjax"/>',
				dataType : 'json',
				cache : false,
				data : (param) ? param : {prjId : $('input[name=prjId]').val()},
				contentType : 'application/json',
				success : function(data){
					modelData = data.currentModelList;
					
					// 리로드 대신 그리드 삭제 후 다시 그리기
					$("#_modelList").jqGrid('GridUnload');
					
					modelList.load();
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
	}
	
	// 모델 그리드
	var modelList = {
		load : function(){
			var Id = '';
			
			$("#_modelList").jqGrid({
				datatype: 'local',
				data : modelData,
				colNames: ['gridId', 'Category', 'Model Name', 'Release Date', 'Last Modified', 'Updated Date', 'Delete', 'osddSyncYn'],
				colModel: [
					{name: 'gridId', index: 'gridId', key:true, hidden:true},
					{name: 'category', index: 'category', align: 'left', width:230, formatter: 'select', editable:true, edittype:"select",editoptions:{value:data.modelValues}},
					{name: 'modelName', index: 'modelName',align: 'left', width:150, editable:true, 
						editoptions:{maxlength:100, dataEvents:[
	                        {
	                        	type: 'blur',
								fn: function(e) {
								 	var key = e.charCode || e.keyCode; // to support all browsers
								 	$(this).val($(this).val().toUpperCase().trim());
							    }
							},
							{  
								type: 'change',
							    fn: function(e) {
							    	//something if value changed
							 		$(this).val($(this).val().toUpperCase().trim());
							    }
							}
						]}
					},
					{name: 'releaseDate', index: 'releaseDate', align: 'center', width:100, editable:true, sorttype:'date',
						editoptions:{maxlength:8, dataEvents:[
							{
								type: 'keyup',
								fn: function(e) {
									var result = $(this).val();
									gDateKeyup = true;
									
									if(/\d+/.test(result)) {
										result = result.match(/\d+/g).join("");
										$(this).val(result);
									} else {
										$(this).val("");
									}
								}
							},
                            {  
	                           type: 'blur',
                               fn: function(e) {
	                               var date_pattern = /^(19|20)\d{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[0-1])$/; 
								   var result = $(this).val();
	                               
	                               if(!date_pattern.test(result) && gDateKeyup) {
	                            	   alertify.error('<spring:message code="msg.common.valid" />', 0);
	                            	   $(this).val("");
	                               } else {
	                            	   gDateKeyup = false;
	                               }
                               }
                            }
						]}
   					},
					{name: 'modifier', index: 'modifier', align: 'center', width:80, editable:false, hidden:true},
					{name: 'modifiedDate', index: 'modifiedDate', align: 'center', width:80, editable:false, sorttype:'date', hidden:true},
					{name: 'delete', index: 'delete', width:80, align: 'center', sortable:false, formatter: fn.setDelBtn},
					{name: 'osddSyncYn', index: 'osddSyncYn', hidden:true},
				],
				autoencode: true,
				editurl:'clientArray',
	 			autowidth: true,
	 			pager: '#pagerModel',
				height: 'auto',
				sortname: 'Category',
				sortorder: 'asc',
				gridview: true,
				rownumbers: true,
				rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
			   	pgtext: false,
			   	pginput:false,
				viewrecords: true,
				loadonce:true,
				loadComplete:function(data) {
					lastsel=-1;
					hidePageNav('pagerModel');
					
					if(isChangeModelInfo == "false") {
						$("#pagerModel").hide();
					}
				},
				onSelectRow: function(rowid) {
					if(rowid){
						if(isChangeModelInfo != "false") {
							$("#_modelList").jqGrid('editRow',rowid,true,pickdates);
						}
						
						lastsel=rowid;
					}
				},
				// 로우 더블클릭 시 편집모드
				ondblClickRow: function(rowid,iRow,iCol,e) {
					if(isChangeModelInfo != "false") {
						gridListBulkEdit("_modelList", pickdates);
					}
				}
			});
			$("#_modelList").jqGrid('navGrid',"#pagerModel",{edit:false,add:true,del:false,search:false,refresh:false
				, addfunc: function (rowid) {
					var _tempRandId = $.jgrid.randId();
					var lastCategory = "";
					var rows = fn.getModelGridRows('#_modelList');
					
					if(rows.length > 0){
						lastCategory = rows[rows.length-1].category;
					}else{
						lastCategory = "";
					}

					$("#_modelList").jqGrid("addRowData", _tempRandId, {gridId: _tempRandId, category: lastCategory}, "last");
                    $("#_modelList").jqGrid('editRow',_tempRandId,true,pickdates);
				}
			});
		},
		changeTarget : function () {
			$("#_modelList").jqGrid('clearGridData');
			//Model Reset
			var site = $('input[name=distributeTarget]:checked').val();
			var siteCd = ${ct:getAllValuesJson(ct:getConstDef('CD_DISTRIBUTE_CODE'))};
			var categoryCd = '';
			
			switch(site){
				case siteCd[0].cdDtlNo:		categoryCd='${ct:getConstDef("CD_MODEL_TYPE")}';	break;
				case siteCd[1].cdDtlNo:		categoryCd='${ct:getConstDef("CD_MODEL_TYPE2")}';	break;
				case siteCd[2].cdDtlNo:		categoryCd='${ct:getConstDef("CD_MODEL_TYPE")}';	break;
			}
			
			getCategoryCodeJson(categoryCd);
		}
	};
	
	//데이트피커
	function pickdates(id){
		jQuery("#"+id+"_releaseDate","#_modelList").datepicker({dateFormat:"yymmdd"});
	}
	
	//ms-kwon
	function checkDate(obj){
		$this = $(obj);
		var date = $this.val();
		
		if($.trim(date) != ""){
			var regExp = /^(19[7-9][0-9]|20\d{2}).(0[0-9]|1[0-2]).(0[1-9]|[1-2][0-9]|3[0-1])$/;
			
			if(!regExp.test(date)){
				$this.val("");
				alert('<spring:message code="msg.project.confirm.wrong.input.date" />');
			}
		}
	}
	
	//2016-11-17 hk-lee
	// 카테고리 코드 가져오기(Json Type)
	function getCategoryCodeJson(cd){
		return $.ajax({
			type: 'GET',
			data: {code:cd},
			async:false,
			dataType:'json',
			url: '<c:url value="/project/getCategoryCodeToJson"/>',
			success : function(json){
				if(json != null){
					var str = '';
					
					$.each(json, function(key,value){
						var keyArr = key.split("|");
						str += keyArr[1]+':'+value+';';
					});
					
					str = str.substring(0, str.length-1);
					data.modelValues = str;					
					
					$('#_modelList').jqGrid('setColProp','category',{editoptions:{value:str}}).trigger('reloadGrid');
				}
			}
		});
	};	
//]]>
</script>
