<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
var userRole = '${sessUserInfo.authority}';
var curIdenStatus = '${project.identificationStatus}';
var projectStatus = '${project.status}';
var distributionStatus = '${project.destributionStatus}';
var isAndroidModel = '${project.androidFlag}' == "Y";
var _popupCheckOssName = null;
var _popupCheckOssLicense = null;

//==========================================================================================
//COMMON
//==========================================================================================
var com_evt = {
	// init
	init: function(){
		com_fn.tabInit();

		if(isAndroidModel) {
			$("#mergeYn").val("Y");
			
			Bom_Save_Flg =true;
		}
		
		$('.btnCommentHistory').on('click', function(e){
			e.preventDefault();
			openCommentHistory('<c:url value="/comment/popup/prj/${project.prjId}"/>');
		});

		// bomConfirm button
		$('#bomConfirm').click(function(e){
			if (com_fn.checkStatus()){
				e.preventDefault();
				
				if(com_fn.isAndroidOnly()) {
					var data = {"prjId" : '${project.prjId}', "identificationStatus" : "CONF", "userComment" : CKEDITOR.instances['editor'].getData()};
					
	 				if($("#ignoreBinaryDbFlag")) {
						data = {"prjId" : '${project.prjId}', "identificationStatus" : "CONF", "userComment" : CKEDITOR.instances['editor'].getData(), "ignoreBinaryDbFlag" : $("#ignoreBinaryDbFlag").val()};
					}
					
					// no opensource license를 선택한 경우 bom merge 쿼리에 포함하지 않기 때문에, 3rd, src, bin, binandroid 에 error message가 있는지 확인해야함
					if(!com_fn.validBomChangeStatus(true)) {
						return false;
					}
					
					com_fn.checkSave(data, "CONF");	
				} else {		 		
					// 머지 체크
					if("Y"!= $("#mergeYn").val()){
						alertify.alert('<spring:message code="msg.project.required.merge" />', function(){});
						com_fn.fnTabChange($(".tabMenu a:eq(4)"));	

						return false;
					}
					
					if(Bom_Save_Flg){
						if($('#bomList').jqGrid('getDataIDs').length == 0 && "Y"!= $("#mergeYn").val()) {
							alertify.alert('<spring:message code="msg.project.required.merge" />', function(){});

							return false;
						}

						// no opensource license를 선택한 경우 bom merge 쿼리에 포함하지 않기 때문에, 3rd, src, bin, binandroid 에 error message가 있는지 확인해야함
						if(!com_fn.validBomChangeStatus(false)) {
							return false;
						}

						var data = {"prjId" : '${project.prjId}', "identificationStatus" : "CONF", "userComment" : CKEDITOR.instances['editor'].getData()};

	 					if($("#ignoreBinaryDbFlag")) {
							data = {"prjId" : '${project.prjId}', "identificationStatus" : "CONF", "userComment" : CKEDITOR.instances['editor'].getData(), "ignoreBinaryDbFlag" : $("#ignoreBinaryDbFlag").val()};
						}

	 					com_fn.checkSave(data, "CONF");	
					} else {
						alertify.alert('<spring:message code="msg.project.check.save" />', function(){});
					}				
				}
			}else {
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}	
		});
		
		// bomReject 버튼 
		$('#bomReject').click(function(e){
			if (com_fn.checkStatus()){
				e.preventDefault();

				if(distributionStatus == "PROC"){
					var comment = '<spring:message code="msg.project.distribution.loading" />';
					
					alertify.error(comment, 0);

					return false;
				}
				
				var innerHtml = '<div class="grid-container" style="width:470px; height:350px;">Are you sure you want to reject?';
				innerHtml    += '	<div class="grid-width-100" style="width:470px; height:310px; margin-top:10px;">';
				innerHtml    += '		<div id="editor2" style="width:470px; height:300px;">' + CKEDITOR.instances['editor'].getData() + '</div>';
				innerHtml    += '	</div>';
				innerHtml    += '</div>';
				
				alertify.confirm(innerHtml, function () {
					if(CKEDITOR.instances['editor2'].getData() == "") {
						alertify.alert('<spring:message code="msg.project.required.comments" />', function(){});

						return false;
					} else {
						var data = {"prjId" : '${project.prjId}', "identificationStatus" : "PROG", "userComment" : CKEDITOR.instances['editor2'].getData()};

						com_fn.checkSave(data, "PROG");
					}
				});

				var _editor = CKEDITOR.instances.editor2;
				
				if(_editor) {
					_editor.destroy();
				}
				
				CKEDITOR.replace('editor2', {});
			}else{
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}
		});
		
		// bomRequest 버튼 
		$('#bomRequest').click(function(e){
			if (com_fn.checkStatus()){
				e.preventDefault();
				
				// 머지 체크
				if("Y"!= $("#mergeYn").val()){
					alertify.alert('<spring:message code="msg.project.required.merge" />', function(){
						// Identification 내 모든 탭 우측 상단에 request review 버튼 표시
						// tab전환 하도록 함수를 새로만듦. (기존 tabMenuA.click callbac fucntion -> fnTabChange 으로 변경)
						com_fn.fnTabChange($(".tabMenu a:eq(4)"));
					});
					
					return false;
				}
				
				if(Bom_Save_Flg) {
					if($('#bomList').jqGrid('getDataIDs').length == 0 && "Y"!= $("#mergeYn").val()) {
						alertify.alert('<spring:message code="msg.project.required.merge" />', function(){
							// Identification 내 모든 탭 우측 상단에 request review 버튼 표시
							// tab전환 하도록 함수를 새로만듦. (기존 tabMenuA.click callbac fucntion -> fnTabChange 으로 변경)
							com_fn.fnTabChange($(".tabMenu a:eq(4)"));
						});
						
						return false;
					}
					
					alertify.confirm('<spring:message code="msg.common.confirm.continue" />', function(e){
						if(e) {
							if(userRole != "ROLE_ADMIN"){
								if(!com_fn.validBomChangeStatus(isAndroidModel)) {
									return false;
								}
							}
							
							var data = {"prjId" : '${project.prjId}', "identificationStatus" : "REQ", "userComment" : CKEDITOR.instances['editor'].getData()};

							com_fn.checkSave(data, "REQ");
						}
					});
					
				} else {
					alertify.alert('<spring:message code="msg.project.check.save" />', function(){});
				}
			}else {
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}
		});
		
		// bomReviewStart 버튼 
		$('#bomReviewStart').click(function(e) {
			if (com_fn.checkStatus()){
				e.preventDefault();
				
				// 머지 체크
				if("N"== $("#mergeYn").val()){
					alertify.alert('<spring:message code="msg.project.required.merge2" />', function(){});

					return false;
				}
				
				if(Bom_Save_Flg) {
					var data = {"prjId" : '${project.prjId}', "identificationStatus" : "REV", "userComment" : CKEDITOR.instances['editor'].getData()};

					com_fn.checkSave(data, "REV");
				} else {
					alertify.alert('<spring:message code="msg.project.check.save" />', function(){});
				}
			}else {
				alertify.alert('<spring:message code="msg.project.warn.project.status" />', function(){});
			}
		});

		$("#editTab").click(function(){
			var prjId = '${project.prjId}';
			var idx = getTabIndex(prjId+"_Project");

			if(idx != "") {
				changeTabInFrame(idx);
			} else {
				createTabInFrame(prjId+'_Project', '#<c:url value="/project/edit/'+prjId+'"/>');
			}
		});

		$("#packagingTab").click(function(){
			var prjId = '${project.prjId}';
			var idx = getTabIndex(prjId+"_Packaging");
			
			if(idx != "") {
				changeTabInFrame(idx);
			} else {
				createTabInFrame(prjId+'_Packaging', '#<c:url value="/project/verification/'+prjId+'"/>');
			}
		});
		
		$("#distributionTab").click(function(){
			var prjId = '${project.prjId}';
			var idx = getTabIndex(prjId+"_Distribute");
			
			if(idx != "") {
				changeTabInFrame(idx);
			} else {
				createTabInFrame(prjId+'_Distribute', '#<c:url value="/project/distribution/'+prjId+'"/>');
			}
		});

		$("#ignoreBinaryDbFlag").on("click",function(){
			if($("#ignoreBinaryDbFlag").prop("checked")) {
				$("#ignoreBinaryDbFlag").val("Y");
			} else {
				$("#ignoreBinaryDbFlag").val("N");
			}
		});

		com_fn.saveFlagObject["SRC"] = false;
		com_fn.saveFlagObject["BIN"] = false;
		com_fn.saveFlagObject["ANDROID"] = false;
	}
}
	
//공통 함수
var com_fn = {
	saveFlagObject : {},
	// 탭 초기화
	tabInit: function(){
		var tabContent = $(".tabContent");
		var tabMenuA = $(".tabMenu a");
		var initDiv = '${initDiv}';
		var vPrjName = "${project.prjName}"+(('${project.prjVersion}'!="")?' ('+'${project.prjVersion}'+')':"");
		var createdDate = "${ct:formatDateSimple(project.createdDate)}";
		var vCreated = '${project.prjUserName}'+" "+'${project.prjDivisionName }'+' ('+createdDate+')';
		var preActiveTab;
		
		$("#vPrjName").text(vPrjName);
		$("#vCreated").text(vCreated); 
		
		// 이용가능 여부 체크 박스
		com_fn.chkApplicable();

		tabContent.hide();
		
		com_fn.btnCtl(userRole, curIdenStatus);
		
		tabMenuA.click(function () {
			com_fn.fnTabChange(this);
		});
		
		tabMenuA.eq(initDiv).click(); 
	},
	fnTabChange: function(target){
		var tabContent = $(".tabContent");
		var tabMenuA = $(".tabMenu a");
		
		$(".tabMenu a span").remove();
		tabMenuA.eq("0").text("3rd party");
		tabMenuA.eq("1").text("SRC");
		tabMenuA.eq("2").text("BIN");
		tabMenuA.eq("3").text("BIN(${project.noticeTypeEtc})");
		tabMenuA.eq("4").text("BOM");
		tabMenuA.eq("5").text("BAT(Optional)");
		
		<c:if test="${!partnerFlag}">
			tabMenuA.eq("0").hide();
		</c:if>
		<c:if test="${project.androidFlag eq 'N'}">
			tabMenuA.eq("3").hide();
		</c:if>
		// android model 의 경우 3rd/src/bin tab을 미표시
		<c:if test="${project.androidFlag eq 'Y'}">
			tabMenuA.eq("0").hide();
			tabMenuA.eq("1").hide();
			tabMenuA.eq("2").hide();
			tabMenuA.eq("4").hide();				
		</c:if>

		var tag = "<span>"+$(target).text()+"</span>";
		$(target).html(tag);
		
		tabContent.hide();
		activeTabText = $(target).text();
		activeTab = $(target).attr("rel");
		
		if(activeTab == "bomDiv"){
			$(".projdecBtn").show();
			$('.commentBtn').show();
			com_fn.btnCtl(userRole, curIdenStatus);
		} else if(isAndroidModel && activeTab == "binAndroidDiv") {
			$(".projdecBtn").show();
			$(".commentBtn").show();
			com_fn.btnCtl(userRole, curIdenStatus);
		} else {
			// Identification 내 모든 탭 우측 상단에 request review 버튼 표시
			$(".projdecBtn").show();
			$(".commentBtn").show();
		}
		
		$("#" + activeTab).show();
		com_fn.btnControl(activeTab);
					
		// _mainLastsel 꼬이는것 방지
		if(activeTab == "srcDiv") {
			_mainLastsel = $('#srcList').jqGrid('getGridParam', "selrow");
		} else if(activeTab == "batDiv") {
			_mainLastsel = $('#batList').jqGrid('getGridParam', "selrow");
		} else if(activeTab == "binAndroidDiv") {
			_mainLastsel = $('#binAndroidList').jqGrid('getGridParam', "selrow");
		} else if(activeTab == "binDiv") {
			_mainLastsel = $('#binList').jqGrid('getGridParam', "selrow");
		}
		
		// 선택된 로우가 없을경우 _mainLastsel 초기화
		if(typeof(_mainLastsel)!="string") {
			_mainLastsel = -1;
		}
	},
	btnCtl: function(role, status){
//			206 CONF	Confirm
//			206 NA		N/A
//			206 PROG	Progress
//			206 REQ		Request
//			206 REV		Review
		// 상태에 따른 버튼 컨트롤 
		var btn_div = $(".projdecBtn");
		var btn_confirm = $(".btnSet.confirm");
		var btn_reject = $(".btnSet.reject");
		var btn_review = $(".btnSet.review");
		var btn_restart = $(".btnSet.restart");
		var btn_Reset = $(".idenReset");
		var btn_Merge = $(".idenMerge");
		var btn_Save = $(".idenSave");
		var btn_Analysis = $(".idenAnalysis");
		var btn_Analysis_Result = $(".idenAnalysisResult");
		var btn_check = $(".btnCheck");
		var btn_supplement_Notice = $(".supplementNotice");
		
		if(role == "ROLE_ADMIN"){ // 관리자 권한 일 경우
			switch(status){
				case "":
					btn_div.hide();
					btn_confirm.hide();btn_reject.hide();btn_review.show();btn_restart.hide();
					btn_Reset.show();btn_Merge.show();btn_Save.show();btn_check.show();

					break;
				case "PROG":
					btn_confirm.hide();btn_reject.hide();btn_review.show();btn_restart.hide();
					btn_Reset.show();btn_Merge.show();btn_Save.show();btn_check.show();

					break;
				case "REQ":
					btn_confirm.hide();btn_reject.hide();btn_review.hide();btn_restart.show();
					btn_Reset.show();btn_Merge.show();btn_Save.show();btn_check.show();

					break;
				case "REV":
					btn_confirm.show();btn_reject.show();btn_review.hide();btn_restart.hide();
					btn_Reset.show();btn_Merge.show();btn_Save.show();btn_check.show();

					break;
				case "CONF":
					btn_confirm.hide();btn_reject.show();btn_review.hide();btn_restart.hide();
					btn_Reset.hide();btn_Merge.hide();btn_Save.hide();btn_Analysis.hide();
					btn_Analysis_Result.hide(); btn_supplement_Notice.hide();
					btn_check.hide();

					break;
			}
		} else if('${project.viewOnlyFlag}' == 'Y'){
			btn_confirm.hide();btn_reject.hide();btn_review.hide();btn_restart.hide();
			btn_Reset.hide();btn_Merge.hide();btn_Save.hide();btn_check.hide();
		} else { // 일반 사용자 일 경우
			switch(status){
				case "":
					btn_div.hide();
					btn_confirm.hide();btn_reject.hide();btn_review.show();btn_restart.hide();
					btn_Reset.show();btn_Merge.show();btn_Save.show();btn_check.show();

					break;
				case "PROG":
					btn_confirm.hide();btn_reject.hide();btn_review.show();btn_restart.hide();
					btn_Reset.show();btn_Merge.show();btn_Save.show();btn_check.show();

					break;
				case "REQ":
					btn_confirm.hide();btn_reject.show();btn_review.hide();btn_restart.hide();
					btn_Reset.hide();btn_Merge.hide();btn_Save.hide();btn_check.hide();

					break;
				case "REV":
					btn_confirm.hide();btn_reject.hide();btn_review.hide();btn_restart.hide();
					btn_Reset.hide();btn_Merge.hide();btn_Save.hide();btn_check.hide();

					break;
				case "CONF":
					btn_confirm.hide();btn_reject.show();btn_review.hide();btn_restart.hide();
					btn_Reset.hide();btn_Merge.hide();btn_Save.hide(); btn_Analysis.hide();
					btn_Analysis_Result.hide(); btn_supplement_Notice.hide();
					btn_check.hide();

					break;
			}
		}
	},
	// 사용 여부 체크 박스
	chkApplicable : function(){
		if('${project.identificationSubStatusPartner}' == 'N'){
			$("#applicableParty").trigger('click');
		}
		
		if('${project.identificationSubStatusSrc}' == 'N'){
			$("#applicableSrc").trigger('click');
		}
		
		if('${project.identificationSubStatusBat}' == 'N'){
			$("#applicableBat").trigger('click');
		}
		
		if('${project.identificationSubStatusBin}' == 'N'){
			$("#applicableBin").trigger('click');
		}
		
		if('${project.identificationSubStatusAndroid}' == 'N'){
			$("#applicableBinAndroid").trigger('click');
		}
		
		<c:if test="${project.verificationStatus eq 'CONF'}">
			$("#applicableParty").attr("disabled", true);
			$("#applicableSrc").attr("disabled", true);
			$("#applicableBat").attr("disabled", true);
			$("#applicableBin").attr("disabled", true);
			$("#applicableBinAndroid").attr("disabled", true);
		</c:if>
	},
	// 메인 그리드 라이센스 데이터 설정(멀티:구분자 , 로 연결|싱글: 단건) 
	setMainLicenseData : function(target,rowid){
		var mainGrid = $("#"+target);
		var subGrid = $("#"+target+"_"+rowid+"_t");
		var licenseDiv =  mainGrid.jqGrid('getCell',rowid,'licenseDiv');
		var licenseId = "";
		var licenseName = "";
		var licenseText = "";
		var copyrightText = "";
		
		// 데이터 추출
		var arr = [];
		arr = subGrid.jqGrid('getDataIDs');
		
		for(var i in arr){
			if(licenseDiv == "M"){
				licenseId += ((licenseId != "") ? "," : "") + subGrid.jqGrid('getCell',arr[i],'licenseId');
				licenseText += ((licenseText != "") ? "," : "") + subGrid.jqGrid('getCell',arr[i],'licenseText');
				copyrightText += ((copyrightText != "") ? "," : "") + subGrid.jqGrid('getCell',arr[i],'copyrightText');
			} else {
				if(i==0){
					licenseId = subGrid.jqGrid('getCell',arr[i],'licenseId');
					licenseText = subGrid.jqGrid('getCell',arr[i],'licenseText');
					copyrightText = subGrid.jqGrid('getCell',arr[i],'copyrightText');
					break;
				}
			}
		}
		
		// 데이터 넣기
		mainGrid.jqGrid('setCell', rowid, 'licenseId', licenseId);
		mainGrid.jqGrid('setCell', rowid, 'licenseText', licenseText);
		mainGrid.jqGrid('setCell', rowid, 'copyrightText', copyrightText);
	},
	//  상단 데이터 로드시 처리
	exeLoadToList : function(target , rDiv){
		if(loadBln){ // 상단 로드 데이터 일시
			// 기본 데이터 설정
			var _tempRandId = ""; // 랜덤ID
			var _sub_tempRandId = "";  //서브ID
			var referenceId = '${project.prjId}'; // 프로젝트ID
			var referenceDiv = rDiv;
			var mArr = [];
			var sArr = [];
			mArr = $("#"+target).jqGrid('getDataIDs');
			
	 		for(var m in mArr){
	 			_tempRandId = $.jgrid.randId();
	 			
	 			$("#"+target).jqGrid("setCell", mArr[m], "gridId", _tempRandId);
	 			$("#"+target).jqGrid("setCell", mArr[m], "componentId", null);
	 			$("#"+target).jqGrid("setCell", mArr[m], "referenceId", referenceId);
	 			$("#"+target).jqGrid("setCell", mArr[m], "referenceDiv", referenceDiv);
	 			
	 			if(rDiv == "10") {
	 				var selrow = $('#_list-1').jqGrid('getGridParam', "selrow" );
	 				var refPrjId = $('#_list-1').jqGrid('getCell',selrow,'prjId');

	 				$("#"+target).jqGrid("setCell", mArr[m], "refPrjId", refPrjId);
	 			} else {
					sArr = $("#"+target+"_"+mArr[m]+"_t").jqGrid('getDataIDs');

					for(var s in sArr){
						_sub_tempRandId = $.jgrid.randId();
						$("#"+target+"_"+mArr[m]+"_t").jqGrid("setCell", sArr[s], "gridId", _tempRandId+"-"+_sub_tempRandId);
						$("#"+target+"_"+mArr[m]+"_t").jqGrid("setCell", sArr[s], "componentLicenseId", "", "","", true);
						$("#"+target+"_"+mArr[m]+"_t").jqGrid("setCell", sArr[s], "componentId", _tempRandId);
					}
	 			}
	 		}
		}
		
		loadBln = false;
	},
	saveEditor : function(){
		//코멘트 임시저장
		var editorVal = CKEDITOR.instances.editor.getData();
		var register = '${sessUserInfo.userId}';
		var param = {referenceId : '${project.prjId}', referenceDiv :'11', contents : editorVal};

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
	sendEditor : function(type){
		var editorVal = CKEDITOR.instances.editor.getData(); //코멘트 저장

		if(!editorVal || editorVal == "") {
			alertify.alert("<spring:message code="msg.project.enter.comment" />", function(){});

			return false;
		}
		
		var param = {referenceId : '${project.prjId}', referenceDiv :'10', contents : editorVal, mailSendType : type, expansion1 : activeTabText};
		
		$.ajax({
			url : '<c:url value="/project/sendComment"/>',
			type : 'POST',
			dataType : 'json',
			cache : false,
			data : param,
			success : function(json){
				if(json.isValid == 'false') {
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				} else {
					$('.ajs-close').trigger("click");

					alertify.success('<spring:message code="msg.project.sent.comments.success" />');
					resetEditor(CKEDITOR.instances.editor);

					$(".commentBtn open").trigger( "click" );
				}
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	editorDialog : function(){
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
		
		var editorVal = CKEDITOR.instances.editor.getData();
		
		if(!editorVal || editorVal == "") {
			alertify.alert("<spring:message code="msg.project.enter.comment" />", function(){});

			return false;
		}
		//launch it.
		var btnHtm = '<br/><b>Send an email to</b><br/>';
		btnHtm += '<input type="button" value="Watcher Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="com_fn.sendEditor(\'W\')"/>&nbsp;&nbsp;&nbsp;';

		if(userRole == "ROLE_ADMIN") {
			btnHtm += '<input type="button" value="Creator Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="com_fn.sendEditor(\'C\')"/>&nbsp;&nbsp;&nbsp;';
		} else {
			btnHtm += '<input type="button" value="Reviewer Only" class="btnCancel btnColor red" style="height:30px;width:100px;"onclick="com_fn.sendEditor(\'R\')"/>&nbsp;&nbsp;&nbsp;';
		}
		
		btnHtm +='<input type="button" value="All" class="btnCancel btnColor red" style="height:30px;width:120px;" onclick="com_fn.sendEditor(\'WR\')"/>&nbsp;&nbsp;&nbsp;';

		alertify.commentDialog(btnHtm);
	},
	checkAplicable :function(obj, target, flag){
		var aplicable = $(obj).val();

		if($(obj).is(':checked')) {
			$('.'+target).hide();
		} else {
			$('.'+target).show();
			
			if(target=="batBtn") {
				bat_fn.changeSearch();
			}
		}
	},
	// 버튼 제어
	btnControl : function(tabNm){
		$("#partyDivBtn").hide();
		$("#srcDivBtn").hide();
		$("#binDivBtn").hide();
		$("#binAndroidDivBtn").hide();
		$("#bomDivBtn").hide();
		$("#batDivBtn").hide();
		$("#" + tabNm+"Btn").show();
	},
	displayDelete : function(cellvalue, options, rowObject){
		return '<input type="button" value="Delete" class="btnCLight" onclick="party_evt.removeList('+options.rowId+', '+rowObject.partnerId+')"/>';
	},
	deleteLicense : function(target){
		$(target).parent().remove();
	},
	deleteLicenseRenewal : function(target){
		$(target).parent().next().remove();
		$(target).parent().remove();
	},
	getLicenseName : function(obj){
		return obj.licenseName.replace(/(<([^>]+)>)/ig, ",").split(",").reduce(function(arr, cur){
		    if(cur.toUpperCase() != "X" && cur != ""){
		        arr.push(cur);
		    }

		    return arr;
		}, []).join(",");
	},
	isAndroidOnly : function() {
		var rtnFlag = false;
		
		if("${project.androidFlag}" == "Y") {
			rtnFlag = true;
			var target;
	 		var arr = [];
	 		
			if(!($("#applicableParty").is(":checked")) ) {
				target = $('#list3');
		 		arr = target.jqGrid('getRowData');
		 		
		 		for(var i in arr){
			 		if(arr[i].excludeYn != "Y") {
			 			rtnFlag = false;
			 			
			 			return false;
			 		}
		 		}
			}
			
			if(rtnFlag && !($("#applicableSrc").is(":checked")) ) {
				target = $('#srcList');
				arr = target.jqGrid('getRowData');
				
		 		for(var i in arr){
			 		if(arr[i].excludeYn != "Y") {
			 			rtnFlag = false;
			 			
			 			return false;
			 		}
		 		}
			}
			
			if(rtnFlag && !($("#applicableBin").is(":checked")) ) {
				target = $('#binList');
				arr = target.jqGrid('getRowData');
				
		 		for(var i in arr){
			 		if(arr[i].excludeYn != "Y") {
			 			rtnFlag = false;
			 			
			 			return false;
			 		}
		 		}
			}
		}
		
		return rtnFlag;
	},
	checkSave : function(data, status){
		<c:if test="${project.androidFlag eq 'N'}">
		cleanErrMsg("bomList");
		cleanErrMsg("srcList");
		cleanErrMsg("binList");
		</c:if>
		<c:if test="${project.androidFlag eq 'Y'}">
		cleanErrMsg("binAndroidList");
		</c:if>
		
		// request review 또는 confirm 시에만 변경 여부를 확인한다.
		// '/project/getCheckChangeData' 에서 androidModel의 경우는 check 하는 부분이 없어서 우회 시킴. 추후 해당 기능이 필요하게되면 다시 변경예정
		if("REQ" != status && "CONF" != status || isAndroidModel) { 
			com_fn.exeProjectStatus(data, status);
		} else {
			var checkObligationFlag = false;
			
			if("CONF" == status || "REQ" == status) {
				var arr = [];
				var bomList = $("#bomList");
				arr = bomList.jqGrid('getRowData');
		 		
		 		for(var i in arr){
		 			if(arr[i].obligationType == "90") {
		 				checkObligationFlag = true;
		 				
		 				break;
		 			}
		 		}
			}
			
			if(checkObligationFlag) {
				loading.hide();
				alert('<spring:message code="msg.warn.include.needcheck.license" />');
				
				return false;
			}
			
			loading.show();
			
			// PARTY Data
			cleanErrMsg("list3");
			
			var partyGrid = [];
			var partnerCheck = '${project.identificationSubStatusPartner}';
			<c:if test="${partnerFlag}">
				partyGrid = $('#list3').jqGrid('getRowData');
				partnerCheck = ($("#applicableParty").is(":checked") ? 'N' : 'Y');
			</c:if>

			var srcMainGrid = $('#srcList').jqGrid('getGridParam','data');
			var binMainGrid = $('#binList').jqGrid('getGridParam','data'); 
	 		
			// finalData
			var finalData = { 
				referenceId : '${project.prjId}',
				partyGrid : JSON.stringify(partyGrid),
				srcMainGrid : JSON.stringify(srcMainGrid),
				srcSubGrid : "",
				binMainGrid : JSON.stringify(binMainGrid),
				binSubGrid : "",
				applicableParty : partnerCheck,
				applicableSrc : ($("#applicableSrc").is(":checked") ? 'N' : 'Y'),
				applicableBin : ($("#applicableBin").is(":checked") ? 'N' : 'Y'),
				status : status
			}
			
			$.ajax({
				url : '<c:url value="/project/getCheckChangeData"/>',
				type : 'POST',
				data : JSON.stringify(finalData),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function(json){
					loading.hide();
					
					if("false" == json.isValid) {
						alertify.alert(json.validMsg == "" ? '<spring:message code="msg.project.required.merge2" />' : json.validMsg, function(){});

						if(srcValidMsgData) {
							gridValidMsgNew(srcValidMsgData, "srcList");
						}
						
						if(binValidMsgData) {
							gridValidMsgNew(binValidMsgData, "binList");
						}
					} else {
						com_fn.exeProjectStatus(data, status);
					}
				},
				error: function(json){
					loading.hide();
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
			
		}
	},
	// no opensource license를 선택한 경우 bom merge 쿼리에 포함하지 않기 때문에, 3rd, src, bin, binandroid 에 error message가 있는지 확인해야함
	validBomChangeStatus : function(_androidBinFlag) {
		var rtnFlag = true;
		
		// android model인 경우
		if(_androidBinFlag) {
			if(binAndroidValidMsgData) {
				$.each(binAndroidValidMsgData,function(key,value) {
					if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key) {
						var seqSuffix = key.split(".");

						if(seqSuffix.length  > 1) {
							rtnFlag = false;
							alertify.alert(com_fn.setMessage("There is an error in BIN(${project.noticeTypeEtc})."), function(){});
							return false;
						}
					}
				});
			}
		} else { // src, bin에 error level message 존재여부 확인
			var arr = $("#bomList").jqGrid('getDataIDs');
			var gridData = new Array();
			var applicableSrcChecked = $("#applicableSrc").prop("checked");
			var applicableBinChecked = $("#applicableBin").prop("checked");
			
			for(var i in arr){
				var adminCheckYn = $("#bomList").jqGrid('getCell',arr[i],'adminCheck');
				
				if(adminCheckYn == "Y"){
					$("#bomList").jqGrid('setCell',arr[i],'adminCheckYn', adminCheckYn);

		 			gridData.push($("#bomList").getRowData(arr[i]));
				}
	 		}
	 		
			if(!applicableSrcChecked) {
				if(srcValidMsgData) {
					$.each(srcValidMsgData,function(key,value) {
						if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key) {
							var seqSuffix = key.split(".");

							if(seqSuffix.length  > 1) {
								var result = bom_fn.adminCheck(gridData, seqSuffix, $("#srcList"));
	
								if(!result){
									rtnFlag = false;
									$(".ajs-cancel").trigger("click");
									
									window.setTimeout(function(){
										alertify.alert(com_fn.setMessage("There is an error in src."), function(){});
										com_fn.fnTabChange($(".tabMenu a:eq(1)"));	
									}, 100);
									
									return false;
								}
							}
						}
					});
					
					if(!rtnFlag) {
						return false;
					}
				}
			}
			
			if(!applicableBinChecked){
				if(binValidMsgData) {
					$.each(binValidMsgData,function(key,value) {
						if("isValid" != key && "validMsg" != key && "resultData" != key && "externalData" != key && "externalData2" != key) {
							var seqSuffix = key.split(".");
							
							if(seqSuffix.length  > 1) {
								var result = bom_fn.adminCheck(gridData, seqSuffix, $("#binList"));
	
								if(!result){
									rtnFlag = false;
									$(".ajs-cancel").trigger("click");
									
									window.setTimeout(function(){
										alertify.alert(com_fn.setMessage("There is an error in bin."), function(){});
										com_fn.fnTabChange($(".tabMenu a:eq(2)"));	
									}, 100);
									
									return false;
								}
							}
						}
					});
					
					if(!rtnFlag) {
						return false;
					}
				}
			}
		}
		
		return rtnFlag;
	},
	exeProjectStatus : function(data, status){
		loading.show();
		
		$.ajax({
			url : '<c:url value="/project/updateProjectStatus"/>',
			type : 'POST',
			data : JSON.stringify(data),
			dataType : 'json',
			cache : false,
			contentType : 'application/json',
			success: function(data){
				loading.hide();
				$(".commentBtn.open").trigger( "click" );
				
				if((status == "CONF" || status == "REQ") && data.isValid == "false") {
					<c:if test="${project.androidFlag eq 'Y'}">
						if(binAndroidValidMsgData) {
							gridValidMsgNew(binAndroidValidMsgData, "binAndroidList");
						}
						
						if(binAndroidDiffMsgData) {
							gridDiffMsg(binAndroidDiffMsgData, "binAndroidList");
						}
						
						if(binAndroidInfoMsgData) {
							gridInfoMsg(binAndroidInfoMsgData, "binAndroidList");
						}
					</c:if>
					
					<c:if test="${project.androidFlag eq 'N'}">
						if(bomValidMsgData) {
							gridValidMsgNew(bomValidMsgData, "bomList");
						}
						
						if(bomDiffMsgData) {
							gridDiffMsg(bomDiffMsgData, "bomList");
						}
						
						// error message를 재표시
						if(srcValidMsgData) {
							gridValidMsgNew(srcValidMsgData, "srcList");
						}
						
						if(batValidMsgData) {
							gridValidMsgNew(batValidMsgData, "batList");
						}
						
						if(binValidMsgData) {
							gridValidMsgNew(binValidMsgData, "binList");
						}
						
						// diff msg
						if(srcDiffMsgData) {
							gridDiffMsg(srcDiffMsgData, "srcList");
						}
						
						if(binDiffMsgData) {
							gridDiffMsg(binDiffMsgData, "binList");
						}
					</c:if>
				
					if(data.validMsg && data.validMsg != "") {
						alertify.error(data.validMsg);
					} else {
						alertify.error('<spring:message code="msg.common.valid" />', 0);
					}
				} else {
					resetEditor(CKEDITOR.instances.editor);
					var prjId = ${project.prjId};
					reloadTabInframe('<c:url value="/project/list"/>');
					
					if(data.validMsg == "goPackaging") {
						alertify.alert('<spring:message code="msg.project.autoredirect.packaging" />', function() {
							deleteTabInFrame('#<c:url value="/project/identification/'+prjId+'"/>');
							createTabInFrame(prjId+'_Packaging', '#<c:url value="/project/verification/'+prjId+'"/>');
						});
					} else if(status == "PROG") { //reject한 경우는 새로고침 (save 버튼등이 문제)
						alertify.alert('<spring:message code="msg.common.success" />', function() {
							if(isAndroidModel) {
								createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/3"/>');
							} else {
								createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/4"/>');
							}
						});
					} else if(userRole != "ROLE_ADMIN" && status == "REQ") { // 일반인이 request review한 경우
						alertify.alert('<spring:message code="msg.common.success" />', function() {
							deleteTabInFrame('#<c:url value="/project/identification/'+prjId+'"/>');	
						});	
					} else if(userRole == "ROLE_ADMIN" && status == "REQ") { // admin이 request review한 경우
						alertify.alert('<spring:message code="msg.common.success" />', function() {
							if(isAndroidModel) {
								createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/3"/>');
							} else {
								createTabInFrame(prjId+'_Identify', '#<c:url value="/project/identification/'+prjId+'/4"/>');
							}
						});
					} else if(status == "CONF") { // Admin이 confirm한 경우
						alertify.alert('<spring:message code="msg.common.success" />', function() {
							deleteTabInFrame('#<c:url value="/project/identification/'+prjId+'"/>');	
						});
					} else {
						com_fn.btnCtl(userRole, status);
						curIdenStatus = status;
						
						// admin이 review start 한 경우 $("#mergeYn").val() 초기화
						if(status == "REV") {
							$("#mergeYn").val("N");
							
							if($("#binaryDB")){
								$("#binaryDB").show();
							}
						}
						
					<c:if test="${project.androidFlag eq 'Y'}">
						if(binAndroidValidMsgData) {
							gridValidMsgNew(binAndroidValidMsgData, "binAndroidList");
						}
						
						if(binAndroidDiffMsgData) {
							gridDiffMsg(binAndroidDiffMsgData, "binAndroidList");
						}
						
						if(binAndroidInfoMsgData) {
							gridInfoMsg(binAndroidInfoMsgData, "binAndroidList");
						}
						
						com_fn.saveFlagObject["ANDROID"] = false;
					</c:if>
						
					<c:if test="${project.androidFlag eq 'N'}">
						// error message를 재표시
						if(bomValidMsgData) {
							gridValidMsgNew(bomValidMsgData, "bomList");
						}
						
						if(bomDiffMsgData) {
							gridDiffMsg(bomDiffMsgData, "bomList");
						}
						
						if(srcValidMsgData) {
							gridValidMsgNew(srcValidMsgData, "srcList");
						}
						
						if(srcDiffMsgData) {
							gridDiffMsg(srcDiffMsgData, "srcList");
						}
						
						if(batValidMsgData) {
							gridValidMsgNew(batValidMsgData, "batList");
						}
						
						if(binValidMsgData) {
							gridValidMsgNew(binValidMsgData, "binList");
						}
						
						if(binDiffMsgData) {
							gridDiffMsg(binDiffMsgData, "binList");
						}
						
						if(binInfoMsgData) {
							gridInfoMsg(binInfoMsgData, "binList");
						}
						
						com_fn.saveFlagObject["SRC"] = false;
						com_fn.saveFlagObject["BIN"] = false;
					</c:if>
						
						alertify.alert('<spring:message code="msg.common.success" />', function(){});
					}
				}
			},
			error: function(data){
				loading.hide();
				
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
	exitCell : function(_mainLastsel, target){
		if(_mainLastsel != -1){
			var grid = $("#" + target);
			var licenseName = com_fn.getLicenseName(grid.getRowData(_mainLastsel));
			
			grid.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
			fn_grid_com.saveCellData(grid.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
			
			grid.jqGrid('saveRow',_mainLastsel);
		}
	},
	setMessage : function(msg){
		var alertMsg  = msg;
		<c:if test="${not empty ct:getCodeValues(ct:getConstDef('CD_COLLAB_INFO'))}">
		var	collabUrl = "${ct:getCodeExpString(ct:getConstDef('CD_COLLAB_INFO'), ct:getConstDef('CD_HELP_URL'))}";
		  
		alertMsg += "<br>";
		alertMsg += "HELP : <a href='" + collabUrl + "' style='color:#0070c0; text-decoration: underline;'>" + collabUrl + "</a>";
		</c:if>
		
		return alertMsg; 
	},
	CheckOssViewPage : function(target){
		var saveFlag = com_fn.saveFlagObject[target.toUpperCase()];
		var referenceDiv;
				
		if(!saveFlag){
			alertify.alert('<spring:message code="msg.project.required.checkOssName" />', function(){});
			
			return false;
		}

		switch(target.toUpperCase()){
			case "SRC":		referenceDiv = "11";	break;
			case "ANDROID":	referenceDiv = "14";	break;
			case "BIN":		referenceDiv = "15";	break;
		}
		
		if(_popupCheckOssName != null){
			_popupCheckOssName.close();
		}
		
		_popupCheckOssName = window.open('<c:url value="/oss/checkOssName?prjId=${project.prjId}&referenceDiv='+referenceDiv+'-${initDiv}&targetName=identification"/>', 'Check OSS Name', 'width=1100, height=550, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes');

		if(!_popupCheckOssName || _popupCheckOssName.closed || typeof _popupCheckOssName.closed=='undefined') {
			alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
		}
	},
	CheckOssLicenseViewPage : function(target){
		var saveFlag = com_fn.saveFlagObject[target.toUpperCase()];
		var referenceDiv;
				
		if(!saveFlag){
			alertify.alert('<spring:message code="msg.project.required.checkOssLicense" />', function(){});
			
			return false;
		}

		switch(target.toUpperCase()){
			case "SRC":		referenceDiv = "11";	break;
			case "ANDROID":	referenceDiv = "14";	break;
			case "BIN":		referenceDiv = "15";	break;
		}
		
		if(_popupCheckOssLicense != null){
			_popupCheckOssLicense.close();
		}
		
		_popupCheckOssLicense = window.open("/oss/checkOssLicense?prjId=${project.prjId}&referenceDiv="+referenceDiv+"-${initDiv}&targetName=identification", "Check License", "width=1150, height=550, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes");

		if(!_popupCheckOssLicense || _popupCheckOssLicense.closed || typeof _popupCheckOssLicense.closed=='undefined') {
			alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
		}	
	},
    checkStatus : function(){
		var returnFlag = false;
		
		$.ajax({
			url : '<c:url value="/project/getProjectStatus"/>',
			type : 'POST',
			data : JSON.stringify({"prjId" : "${project.prjId}"}),
			dataType : 'json',
			cache : false,
			async : false,
			contentType : 'application/json',
			success : function(data){
				var status = data.identificationStatus||"";
				returnFlag = (status == curIdenStatus);
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
				returnFlag = false;
			}
		});
		
		return returnFlag;
	},
	showLicenseInfo : function(obj){
		var licenseName = $(obj).text();

		$.ajax({
			url : '<c:url value="/license/getLicenseId"/>',
			type : 'POST',
			data : {"licenseName" : licenseName},
			dataType : 'json',
			cache : false,
			success : function(data){
				var _frameId = data.licenseId + "_License";
				var _frameTarget = "#<c:url value='/license/edit/" + data.licenseId + "'/>";
				createTabInFrame(_frameId, _frameTarget);
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	},
    bulkEdit : function(tab){
        var gridList;
        var targetGird = "";
        
        switch(tab){
            case 'SRC' : 
                gridList = $("#srcList"); targetGird = "srcList";
                break;
            case 'BIN' : 
                gridList = $("#binList"); targetGird = "binList";
                break;
            case 'BINANDROID' : 
                gridList = $("#binAndroidList"); targetGird = "binAndroidList";
                break;
        }

        var selarrrow = gridList.jqGrid("getGridParam", "selarrrow");
        var rowCheckedArr = [];
        for(var i=0; i<selarrrow.length; i++){
			if($("input:checkbox[id='jqg_" + targetGird + "_" + selarrrow[i] + "']").is(":checked")){
				rowCheckedArr.push(selarrrow[i]);
			}
        }

        if(rowCheckedArr.length > 0){
            fn_grid_com.totalGridSaveMode(targetGird);

            var url = '<c:url value="/oss/ossBulkEditPopup?rowId=' + rowCheckedArr + '&target=' + targetGird + '"/>';

			var _popup = null;
			
            if(_popup == null || _popup.closed){
                _popup = window.open(url, "bulkEditViewProjectPopup", "width=850, height=430, toolbar=no, location=no, left=100, top=100, resizable=yes");

                if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
                    alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
                }
            } else {
                _popup.close();
                _popup = window.open(url, "bulkEditViewProjectPopup", "width=850, height=430, toolbar=no, location=no, left=100, top=100, resizable=yes");
            }
        }else{
            alertify.alert('<spring:message code="msg.oss.select.ossTable" />', function(){});
            return false;
        }
    },
    bulkEditOssInfo : function(obj){
		var editFlag = false;
	   	try{
    		var ossArr = [];
            var rowId = obj["rowId"];
            var target = obj["target"];
            var param = $('#'+target).jqGrid('getGridParam','data');
            
            if(rowId.indexOf(",") > -1){
                ossArr = rowId.split(",");
                for(var idx in ossArr){
                    com_fn.bulkEditSetCell(target, ossArr[idx], obj);

                    for (var i=0; i<param.length; i++){
                        if(param[i]["gridId"] == ossArr[idx]){
                            for(var key in obj){
                                if(key != "rowId" && key != "target"){
                                	param[i][key] = obj[key];
                                }
                            }
                        }
                    }
                }
            }else{
                com_fn.bulkEditSetCell(target, rowId, obj);

                for (var i=0; i<param.length; i++){
                    if(param[i]["gridId"] == rowId){
                        for(var key in obj){
                            if(key != "rowId" && key != "target"){
                            	param[i][key] = obj[key];
                            }
                        }
                    }
                }
            }

            $("#"+target).jqGrid('setGridParam', {data:param}).trigger('reloadGrid');
    	}catch(e){
    		alertify.error('<spring:message code="msg.common.valid2" />', 0);
    		editFlag = true;
    	}finally{
        	if(!editFlag){
        		alertify.success('<spring:message code="msg.common.success" />');
            }
       	}
    },
    bulkEditSetCell : function (target, rowId, obj){
        for(var key in obj){
            if(key != "rowId" && key != "target"){
            	$('#'+target).jqGrid('setCell', rowId, key, obj[key]);
            }
        }
    },
    bulkEditDelRow : function (target, rowId, flag){
		var delFlag = false;
		try{
        	var selrow = "";
    		var param = $("#"+target).jqGrid('getGridParam', 'data');
    		
        	if(rowId.indexOf(",") > -1){
        		selrow = rowId.split(",");
        		for (var i=0; i<selrow.length; i++){
        			$("#"+target).jqGrid('delRowData', selrow[i]);
        			param = com_fn.bulkEditDeleteLocalDataAfterDelRow(param, selrow[i]);
            	}
            }else{
            	$("#"+target).jqGrid('delRowData', rowId);
            	param = com_fn.bulkEditDeleteLocalDataAfterDelRow(param, rowId);
            }

            if(flag == "main"){
            	$("#"+target).jqGrid('GridUnload');

            	if(target == "srcList"){
                	srcMainData = param;
                	src_grid.load();
                	// total record 표시
            		$("#srcList_toppager_right, #srcPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+srcMainData.length+'</div>');
                } else if(target == "binList"){
                	binMainData = param;
    				bin_grid.load();
    				// total record 표시
    				$("#binList_toppager_right, #binPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binMainData.length+'</div>');
                } else if(target == "binAndroidList"){
                	binAndroidMainData = param;
    				binAndroid_grid.load();
    				// total record 표시
    				$("#binAndroidList_toppager_right, #binAndroidPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binAndroidMainData.length+'</div>');
                }
            }
        }catch(e){
        	alertify.error('<spring:message code="msg.common.valid2" />', 0);
        	delFlag = true;
        }finally{
            if(!delFlag){
    			alertify.success('<spring:message code="msg.common.success" />');
            }
        }
    },
    bulkEditDeleteLocalDataAfterDelRow : function (dataArray, rowId){
    	var reMakeArrObj=[];
    	var newIdx = 0;

    	for(var idx=0; idx < dataArray.length; idx++) {
			if(dataArray[idx].gridId != rowId) {
				reMakeArrObj[newIdx++] = dataArray[idx];
			}
		}

		return reMakeArrObj;
    },
    downloadYaml : function(target) {
        var params = {'prjId':'${project.prjId}', 'prjName' : '${project.prjName}'};

        switch(target.toUpperCase()){
            case "SRC":             referenceDiv = "11";    break;
            case "BOM":             referenceDiv = "13";    break;
            case "ANDROID": referenceDiv = "14";    break;
            case "BIN":             referenceDiv = "15";    break;
        }

        $.ajax({
            type: "POST",
            url: '<c:url value="/project/makeYaml/'+referenceDiv+'"/>',
            data: JSON.stringify(params),
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
    }
}
</script>