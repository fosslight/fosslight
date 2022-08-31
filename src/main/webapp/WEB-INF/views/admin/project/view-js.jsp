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
	
	$(document).ready(function() {
		'use strict';
        if('${project.viewOnlyFlag}' == 'N') {
            var copyUrl = "";
            var protocol = window.location.protocol;
            var host =  window.location.host;
            copyUrl = protocol + "//" + host + "/index?id=" + '${project.prjId}' + "&project=true";
            window.location.href = copyUrl;
        }
		
	<c:if test="${empty message}">
		//initSample();
		initSample2();
		data.init();
		evt.init();
		
		if(userRole == "ROLE_ADMIN" && '${project.prjId}' != "" && '${project.copyFlag}' != 'Y'){
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
	</c:if>
	});
	
	// 이벤트
	var evt = {
		init : function(){
			var site = "${project.distributeTarget}";
			var siteCd = ${ct:getAllValuesJson(ct:getConstDef('CD_DISTRIBUTE_CODE'))};
			var categoryCd = '';
			if(site) {
				switch(site){
					case siteCd[0].cdDtlNo:	categoryCd='${ct:getConstDef("CD_MODEL_TYPE")}';	break;
					case siteCd[1].cdDtlNo:	categoryCd='${ct:getConstDef("CD_MODEL_TYPE2")}';	break;
				}
			}

			if(categoryCd == '') {
				categoryCd='${ct:getConstDef("CD_MODEL_TYPE")}';

				$("#tr_distribute").hide();
			}

			getCategoryCodeJson(categoryCd);

			modelList.load();
			
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
		}
	};
	
	var fn = {
		// 왓쳐 엘리먼트 그리기
		addHtml : function(target, str, division, userId){
			var rlt = division+((userId!="") ? "/"+userId : "");
			var html  = '<span><input class="watcherTags" type="text" name="watchers" value="'+rlt+'" style="display: none;"/>';
			html += '<strong>'+str+'</strong></span>';

			target.append(html);

			$('div.multiTxtSet2 .smallDelete').on('click', function(){
				$(this).parent().remove();
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
		displayProjectStatus : function(projectInfo){
			var identificationStatus = projectInfo.identificationStatus;
			var verificaitonStatus = projectInfo.verificationStatus;
			var distributionStatus = projectInfo.destributionStatus;
			var dropYn = projectInfo.dropYn;

			var identVal = "";
			if(identificationStatus){
				if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_PROGRESS')}") {
					identVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_PROGRESS'))}";
					$("#identificationStatus").val("Identification : "+identVal).css('background-color','rgb(157, 165, 184)').css('color','rgb(255, 255, 255)');
				} else if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REQUEST')}"){
					identVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REQUEST'))}";
					$("#identificationStatus").val("Identification : "+identVal).css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
				} else if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REVIEW')}") {
					identVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REVIEW'))}";
					$("#identificationStatus").val("Identification : "+identVal).css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
				} else if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}") {
					identVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM'))}";
					$("#identificationStatus").val("Identification : "+identVal).css('background-color','rgb(56, 79, 123)').css('color','rgb(255, 255, 255)');
				}
			} else {
				$("#identificationStatus").val("Identification").addClass("btnPG wauto");
			}
			
			var verifyVal = "";
			if(verificaitonStatus){
				if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}") {
					if((identificationStatus != "${ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE')}"
						|| identificationStatus != "${ct:getConstDef('CD_DTL_PROJECT_STATUS_DROP')}" )
							&& projectInfo.statusRequestYn == "N" 
							&& verificaitonStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_PROGRESS')}") {
						verifyVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_PROGRESS'))}";
						$("#verificationStatus").val("Verification : "+verifyVal).css('background-color','rgb(157, 165, 184)').css('color','rgb(255, 255, 255)');
					} else if(verificaitonStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REQUEST')}") {
						verifyVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REQUEST'))}";
						$("#verificationStatus").val("Verification : "+verifyVal).css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
					} else if(verificaitonStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REVIEW')}") {
						verifyVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_REVIEW'))}";
						$("#verificationStatus").val("Verification : "+verifyVal).css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
					} else if(verificaitonStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}") {
						verifyVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM'))}";
						$("#verificationStatus").val("Verification : "+verifyVal).css('background-color','rgb(56, 79, 123)').css('color','rgb(255, 255, 255)');
					}
				}
			} else {
				$("#verificationStatus").val("Verification").addClass("btnPG wauto");
			}

			var distributeVal = "";
			if(distributionStatus){
				if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}" && verificaitonStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}") {
					if(distributionStatus=="${ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_PROGRESS')}" 
							&& projectInfo.distributeTarget != "NA") {
						distributeVal = "${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_PROGRESS'))}";
						$("#destributionStatus").val("Distribution : "+distributeVal).css('background-color','rgb(157, 165, 184)').css('color','rgb(255, 255, 255)');
					} else if(distributionStatus=="${ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_RESERVE')}") {
						distributeVal = "${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_RESERVE'))}";
						$("#destributionStatus").val("Distribution : "+distributeVal).css('background-color','rgb(109, 126, 156)').css('color','rgb(255, 255, 255)');
					} else if(distributionStatus=="${ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_DEPLOIDED')}") {
						distributeVal = "${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_DEPLOIDED'))}";
						$("#destributionStatus").val("Distribution : "+distributeVal).css('background-color','rgb(56, 79, 123)').css('color','rgb(255, 255, 255)');
					} else if(distributionStatus=="${ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_FAILED')}") {
						distributeVal = "${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_FAILED'))}";
						$("#destributionStatus").val("Distribution : " + distributeVal).addClass("btnPG wauto");
					}
				}
			} else {
				$("#destributionStatus").val("Distribution").addClass("btnPG wauto");
			}

			// NA 처리
			if(verificaitonStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_NA')}") {
				verifyVal = "${ct:getCodeString(ct:getConstDef('CD_IDENTIFICATION_STATUS'), ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_NA'))}";
				$("#verificationStatus").val("Verification : "+verifyVal);
			}
			
			if(distributionStatus=="${ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_NA')}") {
				distributeVal = "${ct:getCodeString(ct:getConstDef('CD_DISTRIBUTE_STATUS'), ct:getConstDef('CD_DTL_DISTRIBUTE_STATUS_NA'))}";
				$("#destributionStatus").val("Distribution : "+distributeVal);
			}

			// Drop	Project check
			if(identificationStatus!="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}"
					&& dropYn == "Y") {
				$("#identificationStatus").val("Identification : Drop");
				$("#verificationStatus").val("Verification : N/A");
				$("#destributionStatus").val("Distribution : N/A");
			}

			if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}"
				&& verificaitonStatus!="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}"
				&& dropYn == "Y") {
				$("#verificationStatus").val("Verification : Drop");
				$("#destributionStatus").val("Distribution : N/A");
			}

			if(identificationStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}"
				&& verificaitonStatus=="${ct:getConstDef('CD_DTL_IDENTIFICATION_STATUS_CONFIRM')}"
				&& dropYn == "Y") {
				$("#destributionStatus").val("Distribution : Drop");
			}
		},
		downloadFileSetting : function(projectInfo){
			var downloadBtnTarget = "";
			
			if(projectInfo.identificationStatus == "CONF"){
				downloadBtnTarget += "<input type=\"button\" value=\"Report\" class=\"downSet btnReport\" onclick=\"fn.downloadReport()\" title=\"FOSSLight Report\">";
			} else {
				downloadBtnTarget += "<input type=\"button\" value=\"Report\" class=\"downSet btnReport dis\" onclick=\"fn.downloadReport()\" disabled>";
			}
			
			if(projectInfo.verificationStatus == "CONF"){
				if(projectInfo.noticeType == "99" || projectInfo.noticeFileId == "") {
					downloadBtnTarget += "<input type=\"button\" value=\"Notice\" class=\"downSet btnNotice dis\" onclick=\"fn.downloadNotice()\" disabled>";	
				} else {
					downloadBtnTarget += "<input type=\"button\" value=\"Notice\" class=\"downSet btnNotice\" onclick=\"fn.downloadNotice()\" title=\"OSS Notice\">";
				}
				
				if(projectInfo.packageFileId){
					downloadBtnTarget += "<input type=\"button\" value=\"Package\" class=\"downSet btnPackage\" onclick=\"fn.downloadPackage()\" title=\"Packaging File1\">";
				} else {
					downloadBtnTarget += "<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackage()\" disabled>";
				}
				
				if(projectInfo.packageFileId2){
					downloadBtnTarget += "<input type=\"button\" value=\"Package\" class=\"downSet btnPackage\" onclick=\"fn.downloadPackageMulti(\'2\')\" title=\"Packaging File2\">";
				} else {
					downloadBtnTarget += "<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackageMulti(\'2\')\" disabled>";
				}
				
				if(projectInfo.packageFileId3){
					downloadBtnTarget += "<input type=\"button\" value=\"Package\" class=\"downSet btnPackage\" onclick=\"fn.downloadPackageMulti(\'3\')\" title=\"Packaging File3\">";
				} else {
					downloadBtnTarget += "<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackageMulti(\'3\')\" disabled>";
				}
			} else {
				downloadBtnTarget += "<input type=\"button\" value=\"Notice\" class=\"downSet btnNotice dis\" onclick=\"fn.downloadNotice()\" disabled>";
				downloadBtnTarget += "<input type=\"button\" value=\"Package\" class=\"downSet btnPackage dis\" onclick=\"fn.downloadPackage()\" disabled>"
			}
			
			$("#downloadBtn").append(downloadBtnTarget);
		},
		downloadReport : function(){
			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"report", "parameter": "${project.prjId}"}),
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
		downloadNotice : function(){			
			//파일 이름 임시
			location.href = '<c:url value="/project/verification/downloadNotice?prjId=${project.prjId}"/>';
		},
		downloadPackage : function(){			
			location.href = '<c:url value="/project/verification/downloadPackage?prjId=${project.prjId}"/>';
		},
		downloadPackageMulti : function(fileIdx){			
			location.href = '<c:url value="/project/verification/downloadPackageMulti?prjId=${project.prjId}&fileIdx='+fileIdx+'"/>';
		}
	};
	
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

				fn.displayProjectStatus(data.detail); // project status setting
				
				fn.downloadFileSetting(data.detail); // report, notice, pacakging file setting
				
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
			}
			
			data.getModelGridData();
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
	};
	
	// 모델 그리드
	var modelList = {
		load : function(){
			var Id = '';
			
			$("#_modelList").jqGrid({
				datatype: 'local',
				data : modelData,
				colNames: ['gridId', 'Category', 'Model Name', 'Release Date', 'Last Modified', 'Updated Date', /*'Delete',*/ 'osddSyncYn'],
				colModel: [
					{name: 'gridId', index: 'gridId', key:true, hidden:true},
					{name: 'category', index: 'category', align: 'left', width:230, formatter: 'select', editable:false, edittype:"select",editoptions:{value:data.modelValues}},
					{name: 'modelName', index: 'modelName',align: 'left', width:150, editable:false, 
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
					{name: 'releaseDate', index: 'releaseDate', align: 'center', width:100, editable:false, sorttype:'date',
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
							}
						]}
   					},
					{name: 'modifier', index: 'modifier', align: 'center', width:80, editable:false},
					{name: 'modifiedDate', index: 'modifiedDate', align: 'center', width:80, editable:false, sorttype:'date'},
//					{name: 'delete', index: 'delete', width:80, align: 'center', sortable:false, formatter: fn.setDelBtn},
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
/*
					if(rowid){
						if(isChangeModelInfo != "false") {
							$("#_modelList").jqGrid('editRow',rowid,true,pickdates);
						}
						
						lastsel=rowid;
					}
*/
				},
				// 로우 더블클릭 시 편집모드
				ondblClickRow: function(rowid,iRow,iCol,e) {
/*
					if(isChangeModelInfo != "false") {
						gridListBulkEdit("_modelList", pickdates);
					}
*/
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
		}
	};
	
	//데이트피커
	function pickdates(id){
		jQuery("#"+id+"_releaseDate","#_modelList").datepicker({dateFormat:"yymmdd"});
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
