<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<style>
.required .urltxt {display:none;padding-top:2px;font-size:11px;color:blue !important;}
</style>
<script>
	var lastsel;	
	var tempNewOssId;
	var temp={
		obligation:''
	};
	
	var licenseTags=[];
	var commentTemp = '';
	var gLicenseData = new Array();
	var detectedLicenseValid = true;
	var deactivateFlag = "N";
	var renameFlag = "N";
	var nickNameClone;
	//데이터 객체
	var data = {
		detail : ${empty detail ? 'null':detail},
		list : ${empty list ? '{rows:[]}' : list},
		components : ${empty components ? '[]' : components },
		componentsPartner : ${empty componentsPartner ? '[]' : componentsPartner },
		vulnInfoList : ${empty vulnInfoList ? '[]' : vulnInfoList },
		clone : '',
		typeCodes : [],
		copyData : ${empty copyData ? 'null':copyData},
		init : function(){
			commentTemp = $('<div>').append($('dl[name=commentClone]').clone());
			$('dl[name=commentClone]').remove();
			data.clone = $('.multiTxtSet').clone().html();
			nickNameClone = $('.multiTxtSet').clone().html();
			data.cloneDownloadLocation = $('.multiDownloadLocationSet').clone().html();
			data.cloneDetectLicense = $('.multiDetectedLicenseSet').clone().html();
			deactivateFlag = (data.detail&&data.detail.deactivateFlag)||"N";
			//데이터 초기화(컨트롤러에서 가져온 데이터)
			if(data.detail){
				data.syncData(data.detail, false);
			}else{//신규 등록이나 카피일때
				if(data.copyData){
					data.syncData(data.copyData, true);
				}else{
					if(data.list.rows.length > 1){
						$('input[type=radio]:last').trigger('click');
					}else{
						$('input[type=radio]:first').trigger('click');
					}
					
					// 신규 등록이나, 동일한 oss name으로 등록된 nickname이 존재하는 경우
					var ossNickList = [];
					var downloadLocationList = [];
					ossNickList = ${ossNickList};
					downloadLocationList = ${downloadLocationList};
					if(ossNickList && ossNickList.length > 0) {
						ossNickList.forEach(function(nickName, index, obj){
							if(index == 0){
								$('.multiTxtSet span:first').remove();
							}
							
							if(nickName!=''){
								$(data.clone).appendTo('.multiTxtSet');
								$('.multiTxtSet input[type=text]:last').val(nickName);
								$('.smallDelete').on('click', function(){
									$(this).parent().remove();
								});
							}					
						});
					}
					
					if(downloadLocationList && downloadLocationList.length > 0) {
						downloadLocationList.forEach(function(downloadLocation, index, obj){
							if(index == 0){
								$('.multiDownloadLocationSet span:first').remove();
							}
							
							if(downloadLocation!='') {
								$(data.cloneDownloadLocation).appendTo('.multiDownloadLocationSet');
								$('.multiDownloadLocationSet input[type=text]:last').val(downloadLocation);
								$('.smallDelete').on('click', function(){
									$(this).parent().remove();
								});
							}					
						});
					}
				}
			}
			
			$('textarea[name=ossCopyright]').height(240);
		},
		syncData : function(syncData, copyFlag){
			$.each(syncData, function(k, v){
				$('input[name='+k+'],select[name='+k+'],textarea[name='+k+']').each(function(){
					var $this = $(this);
					$this.val(v);
				});
				
				$('input[type=radio]').each(function(){
					var $this=$(this); 
					if($this.val()== v){
						$(this).trigger('click');
					}
				});
				
				switch(k){
					case 'ossNicknames':
						v.forEach(function(nickName, index, obj){
							if(index == 0){
								$('.multiTxtSet span:first').remove();
							}
							if(nickName!=''){
								$(data.clone).appendTo('.multiTxtSet');
								$('.multiTxtSet input[type=text]:last').val(nickName);
								$('.smallDelete').on('click', function(){
									$(this).parent().remove();
								});
							}					
						});
						
						break;
					case 'downloadLocations':
						v.forEach(function(downloadLocation, index, obj){
							if(index == 0){
								$('.multiDownloadLocationSet span:first').remove();
							}
							if(downloadLocation != '' && downloadLocation != null){
								$(data.cloneDownloadLocation).appendTo('.multiDownloadLocationSet');
								$('.multiDownloadLocationSet input[type=text]:last').val(downloadLocation);
								$('.smallDelete').on('click', function(){
									$(this).parent().remove();
								});
							}
						});
						
						break;
					case 'detectedLicenses':
						v.forEach(function(detectLicense, index, obj){
							if(index == 0){
								$('.multiDetectedLicenseSet span:first').remove();
							}
							if(detectLicense!=''){
								$(data.cloneDetectLicense).appendTo('.multiDetectedLicenseSet');
								$('.multiDetectedLicenseSet input[type=text]:last').val(detectLicense);
								$('.smallDelete').on('click', function(){
									$(this).parent().remove();
								});
							}					
						});
						
						break;
					case 'licenseDiv':
						if(v=='S'){
							syncData.ossLicenses.forEach(function(item){
								$('input[name=licenseName]').val(item.licenseName);
								$('#licenseName').val(item.licenseNameEx);
								$('#lt td').html(item.licenseType);
								$('#ob td').html('');
								$(item.obligation).appendTo('#ob td');
							});
						}else if(v=='M'){
							var licenseType = autoLicense(data.list.rows);
							var obligationHtml = autoObligation(data.list.rows);
							$('#lt td').html(licenseType);
							$('#ob td').html('');
							$(obligationHtml).appendTo('#ob td');
						}
						
						break;
					case 'ossType':
						if(v.toUpperCase() == 'V'){
							$("[name='"+k+"']").html(' / <span class="iconSet vdif">v-Diff</span>');
						}
						
						break;
					case 'deactivateFlag':
						if(v.toUpperCase() == "Y"){
							$("#deactivateFlag").attr("checked", true);
							$("[name='deactivateFlag']").val(v);
						}
						
						break;
				}
			});
			
			if(copyFlag){
				$("input[name=ossId]").val("");
				$(".multiDownloadLocationSet > div > span > input[name='downloadLocations']").each(function(idx, cur){
					$(cur).attr("onblur", "fn.urlDuplication(this)");
				});
			}
		}
	}
	
	//이벤트 객체
	var evt = {
		init : function(){ 
			$('.btnCancel').click(function(){
				$('.pop').hide();
				$('#blind_wrap').hide();
			}); // 팝업 닫기
			
			//닉네임 인풋 추가
			$('#nickAdd').on('click', function(){
				$(data.clone).prependTo('.multiTxtSet');
				
				$('.smallDelete').on('click', function(){
					$(this).parent().parent().remove();
				});
				
			});

			//Detect License Input 추가
			$('#detectedLicenseAdd').on('click', function(){
				$(data.cloneDetectLicense).appendTo('.multiDetectedLicenseSet');

				setCustomAutoComplete("single");
				
				$('.smallDelete').on('click', function(){
					$(this).parent().parent().remove();
				});
			});
			
			//Download Location Input 추가
			$('#downloadLocationAdd').on('click', function(){
				$(data.cloneDownloadLocation).appendTo('.multiDownloadLocationSet');
				
				$('.smallDelete').on('click', function(){
					$(this).parent().parent().remove();
				});
				
				if('${ossId}' == ''){
					$(".multiDownloadLocationSet > div > span > input[name='downloadLocations']").each(function(idx, cur){
						$(cur).attr("onblur", "fn.urlDuplication(this)");
					});
				}
			});
			
			//라이센스 등록
			$("#save").on('click',function(){
				fn.save();
			});
			
			$("#rename").on('click',function(){
				renameFlag = 'Y';
				$('input[name=renameFlag]').val(renameFlag);
				fn.save();
			});
			
			$("#copy").on('click',function(){
				activeDeleteTabInFrame();
				var ossId = $('input[name=ossId]').val();
				createTabInFrame('copy_'+ossId+'_Opensource', '#<c:url value="/oss/copy/'+ossId+'"/>');
			});
			
			//라이센스 삭제
			$('#delete').on('click', function(){
				var editorVal = CKEDITOR.instances.editor.getData();
				if(editorVal == "") {
					alertify.alert('<spring:message code="msg.oss.required.deletion" />', function(){});
					return false;
				}

				if(deactivateFlag == "Y"){
					alertify.alert('<spring:message code="msg.oss.required.deactivate.deletion" />', function(){});
					return false;
				}
				
				alertify.confirm('<spring:message code="msg.oss.warn.remove" />', function (e) {
					if (e) {
						$('input[name=comment]').val(editorVal);
						
						if(validationDataSet()){
							$("#ossForm").ajaxForm({
								url :'<c:url value="/oss/validation"/>',
					            type : 'POST',
					            dataType:"json",
					            cache : false,
						        success : onDeleteValidSuccess,
					            error : onError
						    }).submit();
						}
					} else {
						return false;
					}
				});
			});
			
			//라이센스 삭제용 검색
			$('.search').on('click', function(){
				var postData=$('#listSearch').serializeObject();
				$("#_ossSelectList").jqGrid('setGridParam', {postData:postData}).trigger('reloadGrid');
			});
			
			//select oss 팝업 확인 버튼 
			$("#wrapIframe > div > div.pop.ossSelectPop > div.pbtn input:eq(1)").on('click', function(){
				var rowId = $('#_ossSelectList').jqGrid('getGridParam', 'selrow');
				if(rowId === null) {
					alert('<spring:message code="msg.oss.required.select.grid" />');
				}else{
	 				var rowData = $('#_ossSelectList').jqGrid('getRowData',rowId);
	 				var newOssId = rowData.ossId;
	 				if(newOssId == '${ossId}'){
						alertify.alert('<spring:message code="msg.oss.cannot.select" />', function(){});
	 					return;
	 				}
	 				
	 				tempNewOssId = newOssId;
	 				
	 				// 동일한 OSS의 다른 version으로이관하는 경우
	 				if(data.detail.ossName == rowData.ossNameTemp) {
		 				//1. 기존 OssId를 사용중인 프로젝트의 OssId , Version 교체
		 				//2. 기존 Oss 의 Name 과 Nickname을 현재 선택한 Oss의 Nickname 에 병합
		 				//3. 기존의 Oss 삭제
	
						var editorVal = CKEDITOR.instances['editor'].getData();
	 					var sendData = {
	 						ossId:${empty ossId ? "''":ossId},
	 						newOssId:newOssId,
	 						comment:editorVal
	 					};
		 				
	 					$.ajax({
	 						url :'<c:url value="/oss/delAjax"/>',
	 			            type : 'POST',
	 			            data : sendData,
	 			            dataType:"json",
	 			            cache : false,
	 				        success : function(json){
	 							var ossId = $('input[name=ossId]').val();
	 							if(json.resCd == '10'){
	 								alertify.alert('<spring:message code="msg.common.success" />',function(){
	 									reloadTabInframe('/oss/list');
	 									deleteTabInFrame('#/oss/edit/'+ossId);

	 									$('.ossSelectPop').hide();
	 									$('#blind_wrap').hide();
	 								});
	 							}else{
	 								alertify.error('<spring:message code="msg.common.valid" />', 0);
	 							}
	 				        },
	 			            error : function(){
	 			            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
	 			            }
	 					});
	 				} else {
	 					// 다른 OSS로 이관하는 경우
	 					mergeOssForDelete(newOssId);
	 				}
				}
			});
			
			$("#wrapIframe > div > div.pop.mergeOssCheckPop > div.pbtn input:eq(1)").on('click', function(){
				var editorVal = CKEDITOR.instances['editor'].getData();
				var sendData = {
					ossId:${empty ossId ? "''":ossId},
					newOssId:tempNewOssId,
					comment:editorVal
				};
				
				$.ajax({
					url :'<c:url value="/oss/delOssWithVersionMeregeAjax"/>',
		            type : 'POST',
		            data : sendData,
		            dataType:"json",
		            cache : false,
			        success : function(json){
						var ossId = $('input[name=ossId]').val();
						if(json.resCd == '10'){
							alertify.alert('<spring:message code="msg.common.success" />',function(){
								deleteTabInFrame('#<c:url value="/oss/edit/'+ossId+'"/>');
								reloadTabInframe('<c:url value="/oss/list"/>');
								$('.mergeOssCheckPop').hide();
								$('#blind_wrap').hide();
							});
						}else{
							alertify.error('<spring:message code="msg.common.valid" />', 0);
						}
			        },
		            error : function(){
		            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
		            }
				});
			});
			
			
			
			//프로젝트 리스트 더보기
			$('#listMore').on('click',function(){
				createTabInFrameWithCondition('Project List', '#<c:url value="/project/list"/>', 'OSSLISTMORE', $("input[name=ossId]").val());
			});

			//프로젝트 리스트 더보기
			$('#listMore3rd').on('click',function(){
				createTabInFrameWithCondition('3rd Party List', '#<c:url value="/partner/list"/>', 'OSSLISTMORE', $("input[name=ossId]").val());
			});
			
			if('${ossId}' == ''){ // OSS 신규등록 시 URL 링크 중복 체크
				$(".multiDownloadLocationSet > div > span > input[name='downloadLocations']").each(function(idx, cur){
					$(cur).attr("onblur", "fn.urlDuplication(this)");
				});
				
				$('input[name=homepage]').blur(function(){
					fn.homepageDuplication(this);
				});
			}
			
			$("[name='ossName']").on("blur", function(e){
				fn.urlDuplicationAll();
			});

			$("#deactivateFlag").on("click", function(){
				var isChecked = $("#deactivateFlag").prop("checked");
				var rtnVal = isChecked ? "Y" : "N";
				
				$("[name='deactivateFlag']").val(rtnVal);
			});

			$("#sync").on('click',function(){
				var ossName = $("input[name=ossName]").val();
				var ossVersion = $("input[name=ossVersion]").val();
				var ossId = $("input[name=ossId]").val();

				if(ossName!=""){
					onAjaxLoadingHide = true;
					$.ajax({
						url : '<c:url value="/oss/getOssListByName"/>',
						dataType : 'json',
						cache : false,
						data : {ossName : ossName},
						contentType : 'application/json',
						success : function(data){
							var length = data.ossList.length;
							if (length == 1) {
								alertify.alert('<spring:message code="msg.oss.required.version" />', function(){});
							} else if (length > 1) {
								$.ajax({
									url : '<c:url value="/oss/checkExistsOssByname"/>',
									type : 'GET',
									dataType : 'json',
									cache : false,
									async: false,
									data : {ossName : ossName},
									contentType : 'application/json',
									success : function(data){
										if(data.isValid == 'true') {
											var _popup = null;
											var _encUrl = "ossName="+fn.replaceGetParamChar(encodeURIComponent(ossName))+"&ossVersion="+fn.replaceGetParamChar(ossVersion)+"&ossId="+ossId;
											
											if(_popup == null || _popup.closed){
												_popup = window.open("<c:url value='/oss/osssyncpopup?"+_encUrl+"'/>", "ossSyncViewPopup_"+ossName, "width=1000, height=700, toolbar=no, location=no, left=100, top=100");

												if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
													alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
												}
											} else {
												_popup.close();
												_popup = window.open("<c:url value='/oss/osssyncpopup?"+_encUrl+"'/>", "ossSyncViewPopup_"+ossName, "width=1000, height=700, toolbar=no, location=no, left=100, top=100");
											}
										}
									},
									error : function(){
										alertify.error('<spring:message code="msg.common.valid2" />', 0);
									}
								});
							}
						},
						error : function(){
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					});
				}
			});
		}			
	};
	
	//커스터마이징 자동완성 셀렉트
	function setCustomAutoComplete(div){
		var selected = false;
		if(div == 'single'){
			$( '.autoComOssLicense' ).autocomplete({
				source: licenseTags,
				select: function( event, ui ) {
					var target = $(this).attr("id");

					if(target == "licenseName"){
						$("#licenseName").parent().find("span.retxt:first").hide();
						var licenseName = ui.item.shortIdentifier.length > 0 ? ui.item.shortIdentifier : ui.item.licenseName;
						$('#licenseName').val(licenseName);
						showLicenseText(licenseName);
						
						return false;
					}
				},
				minLength: 0,
				open: function() {
					$(this).attr('state', 'open');
					selected = false;
					
				},
				close: function () {
					$(this).attr('state', 'closed');
					var _item = checkLicenseSelected($(this).val());
					var target = $(this).attr("id");
					
					// detected License
					$(this).parent().next("span.retxt:first").empty();
					detectedLicenseValid = true; // reset
					
					if(_item != null){
						var licenseName = _item.shortIdentifier.length > 0 ? _item.shortIdentifier : _item.licenseName;
						$(this).val(licenseName);
						selected = true;
					} else {
						var value = $(this).val();
						
						if(value != ""){
							$(this).parent().next("span.retxt:first").html('<spring:message code="msg.oss.unknown.license" />').show();
							detectedLicenseValid = false;
						}
					}
				}
		    })
		    .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}})
		    .autocomplete( "instance" )._renderItem = function( ul, item ) {
		    	return $( "<li>" )
		    	.append( "<div>" + item.label + "<strong> (" + item.licenseType + ") </strong>" + item.obligation + item.restriction + "</div>" )
		    	.appendTo( ul );
		    };
    	}else if(div == 'multi'){
    		$('#'+lastsel+'_licenseNameEx').autocomplete({
				source: licenseTags,
				select: function( event, ui ) {
					var licenseName = ui.item.shortIdentifier.length > 0 ? ui.item.shortIdentifier : ui.item.licenseName;
					$('#'+lastsel+'_licenseNameEx').val(licenseName);
					$('#'+lastsel+' > td:eq(9)').text(licenseName); // licenseNameEx
					$('#'+lastsel+' > td:eq(7)').text(licenseName); // licenseName
					$('#'+lastsel+' > td:eq(10)').text(ui.item.licenseType); // licenseType
					$('#'+lastsel+' > td:eq(11)').text(ui.item.obligationChecks); // obligationChecks
					$('#'+lastsel+' > td:eq(8)').text(ui.item.licenseId); // licenseId
					showLicenseText(licenseName);
					gLicenseData[lastsel] = "Y";
					return false;
				},
		   		minLength: 0,
		   		open: function() {
					$(this).attr('state', 'open');
					selected = false;
				},
				change: function(){
					gLicenseData[lastsel] = "N";
				},
				close: function () { 
					$(this).attr('state', 'closed');
					var _item = checkLicenseSelected($(this).val());
					var gridStr = "_licenseChoice";
					$("div.retxt._licenseChoice_"+lastsel).remove();
					if(_item == null) {
						$("#"+lastsel+"_licenseNameEx").after('<div class=\"'+gridStr+"_"+lastsel+' retxt\">'+ '<spring:message code="msg.oss.unknown.license" />' +'</div>');
						$("div.retxt._licenseChoice_"+lastsel).show();
					} else {
						var licenseName = _item.shortIdentifier.length > 0 ? _item.shortIdentifier : _item.licenseName; 
						$('#'+lastsel+'_licenseNameEx').val(licenseName);
						// 리스트 설정
						$('#_licenseChoice').jqGrid('saveRow',lastsel);
						var list = [];
						data.copyData ? list = data.copyData.ossLicenses : list = data.list.rows;
						var rowData = $('#_licenseChoice').jqGrid('getRowData', lastsel);
						rowData['licenseId'] = _item.licenseId;
						rowData['licenseName'] = _item.licenseName;
						rowData['licenseNameEx'] = licenseName;
						rowData['licenseType'] = _item.licenseType;
						rowData['obligation'] = _item.obligation;
						rowData['obligationChecks'] = _item.obligationChecks;
						rowData['ossLicenseIdx'] = lastsel;
						rowData['no'] = lastsel;
						
						list.forEach(function(row,index){
							if(row.ossLicenseIdx == lastsel ){
								list.splice(index,1,rowData);
							};
						});
						
						$('#_licenseChoice').jqGrid('editRow',lastsel);
						// 첫 로우 ossLicenseComb 설정
						changeLicenseType();
						setFristComb();
						selected = true;
					}
				}
		    })
		    .focus(function() {if ($(this).attr('state') != 'open') {$(this).autocomplete("search");}})
		    .autocomplete( "instance" )._renderItem = function( ul, item ) {
		    	return $( "<li>" )
		    	.append( "<div>" + item.label + "<strong> (" + item.licenseType + ") </strong>" + item.obligation  + item.restriction + "</div>" )
		    	.appendTo( ul );
		    };
    	}
	}
	
	function checkLicenseSelected(val) {
		if(val == "") {
			return null;
		}
		
		val = val.toUpperCase().trim();
		
		for (var idx = 0; idx < licenseTags.length; idx++) {
			if (licenseTags[idx].licenseName.toUpperCase().trim() == val || licenseTags[idx].shortIdentifier.toUpperCase().trim() == val) {
				return licenseTags[idx];
			}
		}
		
		return null;
	}
	
	
	$(document).ready(function () {
		'use strict';
		$('span[title]').qtip();
		initSample();
		evt.init();
		data.init();
		
		if($('input[name=ossId]').val()) {
			fn_commemt.getCommentList();
		}
		
		$("#btnShowLicenseText").click(function() {
			if($(this).val() == "Show license text") {
				$(this).val("Hide license text");
				$("#disp_licenseText").show();
				showLicenseText();
			} else {
				$(this).val("Show license text");
				$("#disp_licenseText").hide();
			}
		});
		
		$('#listSearch').on('submit', function(e){
			return false;
		}).on('keypress', function(e){
			if((e.keyCode || e.which) == 13){
				$('.search').trigger('click');	// 검색 엔터
			}
		});
		
		if(data.components.length < 1){
			$('#_projectList').parent().parent().hide();
		}

		if(data.componentsPartner.length < 1){
			$('#_partnerList').parent().parent().hide();
		}
		
		//자동완성 데이터 리스트 가져오기
		commonAjax.getLicenseTags().success(function(data, status, headers, config){
			if(data != null){
				data.forEach(function(obj){
					var tag ={
						value : obj.licenseName,
						licenseId : obj.licenseId,
						label : obj.shortIdentifier.length > 0 ? obj.licenseName+' ('+obj.shortIdentifier+')' : obj.licenseName,
						licenseName : obj.licenseName,
						shortIdentifier : obj.shortIdentifier,
						licenseType : obj.licenseType,
						obligation : obj.obligation,
						obligationChecks : obj.obligationChecks
						,obligationCode : obj.obligationCode
						,licenseTypeVal : obj.licenseTypeVal
						,restriction : obj.restriction
					}
					
					licenseTags.push(tag);
				});
			}
		});
		
		//자동완성 Single License 일때
		setCustomAutoComplete('single'); 
		
		var list = [];
		
		if(data.copyData){
			list = data.copyData.ossLicenses;
			$('#copy').hide();
			$('#delete').hide();
			$('input[name=ossCopyFlag]').val("Y");
		}else{
			list = data.list.rows;
		}
		
		// 라이센스 그리드
		var _licenseChoice = $('#_licenseChoice');
		_licenseChoice.jqGrid({
			datatype: 'local',
			data:list,
			colNames:['','','','License', 'Copyright', '','','','','license Type', 'obligationChecks'],
			colModel:[
				{name:'no', index: 'ossLicenseIdx', width:30, hidden:true},
				{name:'ossLicenseIdx', index: 'ossLicenseIdx', width:30, key:true, hidden:true},
				{name:'ossLicenseComb',index:'ossLicenseComb', width:70, align:"center", sortable:false, editable:true, edittype:"select", editoptions:{value:"AND:AND;OR:OR;WITH:WITH", dataEvents:[{type:'change', fn:changeLicenseType}]}},
				{name:'licenseNameEx',index:'licenseNameEx', width:150, editable:true, editoptions: {
                        dataInit: function (elem) { 
	                               $(elem).focus(function () { setCustomAutoComplete('multi'); }) 
	                         }
	                    }},
				{name:'ossCopyright',index:'ossCopyright', width:250, editable:true, edittype:"textarea", editoptions:{rows:"10",cols:"50"}},
				{name: 'delete', index: 'delete', width:80, align: 'center', sortable:false, formatter: displayButtons},
				{name:'licenseName',index:'licenseName', width:50, hidden:true},
				{name:'licenseId',index:'licenseId', width:50, hidden:true},
				{name:'licenseNameEx',index:'licenseNameOrg', width:50,hidden:true},
				{name:'licenseType',index:'licenseType', width:50,hidden:true},
				{name:'obligationChecks',index:'obligationChecks', width:50,hidden:true}
			],
			autoencode: true,
			editurl:'clientArray',
			pager: '#_licenseChoicePager',
			autowidth: true,
			height: 'auto',
			gridview: true,
			rownumbers: true,
			viewrecords: true,
			loadonce:true,
			onSelectRow: function(rowid){
				// 로울 클릭시 그리드 에러 메세지 삭제 
				$("div.retxt._licenseChoice_"+rowid).remove();
				// 현재 로우와 라스트 로우가 다를시
				showLicenseText();
				if(rowid && rowid!==lastsel){
					_licenseChoice.jqGrid('saveRow',lastsel);
					
					// license 존재 여부 체크
					//라이센스 묶음 텍스트 출력
					createMultiLicenseText();
					// 라이센스 배경색 설정
					var list = [];
					data.copyData ? list = data.copyData.ossLicenses : list = data.list.rows;
					list.forEach(function(row,index){
						if(row.ossLicenseIdx == lastsel ){
							row.ossLicenseComb = $('#_licenseChoice').jqGrid('getCell',lastsel,'ossLicenseComb');
						};
					});
					makeBackGroundColor();
					// 로우 에디트
					_licenseChoice.jqGrid('editRow',rowid);
					lastsel=rowid;
					// 첫 로우 ossLicenseComb 설정
					setFristComb();
					//커스터마이징 자동완성 셀렉트
					setCustomAutoComplete('multi');
					//라이센스 타입 및 Obligation 자동 체크
					var type = autoLicense(list);
					var obligationHtml = autoObligation(list);
					
					if(list.length==0){
						type='';
						obligationHtml='';
					}
					
					$('#lt td').html(type);
					$('#ob td').html('');
					$(obligationHtml).appendTo('#ob td');

				}
			},
			afterInsertRow:function(rowid, rowdata, rowelem){
			},
			loadComplete:function(data){
				lastsel = -1;
				// 페이징 UI 설정
				hidePageNav('_licenseChoicePager');
				// 첫 로우 ossLicenseComb 설정
				setFristComb();
				// 라이센스 묶음 텍스트 출력
				createMultiLicenseText();
				// 라이센스 배경색 설정
				makeBackGroundColor();
				//라이센스 타입 및 Obligation 자동 체크
				var type = autoLicense(list);
				var obligationHtml = autoObligation(list);
				
				if(list.length==0){
					type='';
					obligationHtml='';
				}
				
				$('#lt td').html(type);
				$('#ob td').html('');
				$(obligationHtml).appendTo('#ob td');
			}
		});
		_licenseChoice.jqGrid('navGrid',"#_licenseChoicePager",{edit:false,add:true,del:false,search:false,refresh:false
			, addfunc: function (rowid) {
				var ids = $("#_licenseChoice").jqGrid("getDataIDs");
				var newId = ids.length ? Number(ids[ids.length-1])+1 : 1;
				var row = {no	:newId, ossLicenseIdx	:newId};
				data.list.rows.push(row);
				$("#_licenseChoice").jqGrid("addRowData", newId , row, "last");
				$("#_licenseChoice").jqGrid("setSelection", newId);
				
				// 첫 로우 ossLicenseComb 설정
				setFristComb();
			}
		});
		
		// 프로젝트 그리드
		$("#_projectList").jqGrid({
			datatype: 'local',
			data: data.components,
			colNames:['ID','Project Name', 'Project Version', 'oldsystem'],
			colModel:[
				{name:'prjId',index:'prjId', width:70, sortable:false, align:"center", key:true},
				{name:'prjName',index:'prjName', width:400, sortable:false},
				{name:'prjVersion',index:'prjVersion', width:100, sortable:false, align:"center"},
				{name:'oldSystemFlag',index:'oldSystemFlag', hidden:true}
			],
			rowNum:100,
			viewrecords: true,
			height: 'auto',
			ondblClickRow: function(rowid,iRow,iCol,e) {
				var rowData = $("#_projectList").jqGrid('getRowData',rowid);
				if("Y" != rowData.oldSystemFlag) {
					createTabInFrame(rowData['prjId']+'_Project','#/project/edit/'+rowData['prjId']);
				}
			},
			loadComplete: function(data) {
				if(data.records > 0) {
					var multRowIds = []; 
					var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row, rowid, rowData, className;
					for(var _idx=0;_idx<rowsCount;_idx++) {
						row = rows[_idx];
						className = row.className;
						if (className.indexOf('jqgrow') !== -1) {
							rowid = row.id;
							rowData = data.rows[rowIdx++];
							if(rowData.oldSystemFlag == "Y") {
								className = className + ' excludeRow';
							}
							row.className = className;
						} else if(className.indexOf('ui-subgrid') !== -1){
							rowIdx++;
						}
					}
				}
			}
		});
		
		// 프로젝트 그리드
		$("#_partnerList").jqGrid({
			datatype: 'local',
			data: data.componentsPartner,
			colNames:['ID','Partner Name', '3rd Party Software Name', '3rd Party Software Version'],
			colModel:[
				{name:'partnerId',index:'partnerId', width:70, sortable:false, align:"center", key:true},
				{name:'partnerName',index:'partnerName', width:300, sortable:false},
				{name:'softwareName',index:'softwareName', width:200, sortable:false, align:"center"},
				{name:'softwareVersion',index:'softwareVersion', width:200, sortable:false, align:"center"}
			],
			rowNum:100,
			viewrecords: true,
			height: 'auto',
			ondblClickRow: function(rowid,iRow,iCol,e) {
				var rowData = $("#_partnerList").jqGrid('getRowData',rowid);
				var rowKey = rowData['partnerId'];
				
				createTabInFrame(rowKey+'_3rdParty', '#/partner/edit/'+rowKey);
				
			},
			loadComplete: function(data) {
				// 3rd party는 old system에는 없는 기능 이므로 old system으로 인한 exclude row는 표시 할 수 없음.. 
			}
		});

		// Vulnerability
		<c:if test="${not empty vulnInfoList}">
		$("#_vulnInfoList").jqGrid({
			datatype: 'local',
			data: data.vulnInfoList,
			colNames:['CVE ID','CVSS Score', 'Description', 'Published Date'],
			colModel:[
				{name:'cveId',index:'cveId', width:100, align:"center", formatter: cveIdTag, unformat: unCveIdTag},
				{name:'cvssScore',index:'cvssScore', width:80, align:"center", formatter:'vuln',sorttype: 'number'},
				{name:'vulnSummary',index:'vulnSummary', width:500, align:"center"},
				{name:'modiDate',index:'modiDate', width:100, align:"center", formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}}
			],
			rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
			sortname: 'cvssScore',
			viewrecords: true,
			loadonce:true,
			sortorder: "desc",
			height: 'auto',
			ondblClickRow: function(rowId) {
			}
		});
		</c:if>
		
		if('${ossId}' != '') {
			// oss 그리드
			$('#_ossSelectList').jqGrid({
				url:"/oss/ossPopupList/${ossId}",
				datatype: 'json',
				jsonReader:{
					repeatitems: false,
					id:'ossId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames:['ID','OSS Name(Nick Name)', 'Version', 'OSS Name Temp'],
				colModel:[
					{name:'ossId', index: 'ossId', width:80, align:"center"},
					{name:'ossName',index:'ossName', width:470, align:"left"},
					{name:'ossVersion',index:'ossVersion', width:150, align:"center"},
					{name:'ossNameTemp',index:'ossNameTemp', align:"center", hidden:true}
				],
				autoencode: true,
				rowNum: 5,
				rowList: [5, 10, 15],
	 			autowidth: true,
				pager: '#ossSelectListPager',
				gridview: true,
				sortname: 'ossId',
				viewrecords: true,
				sortorder: 'desc',
				loadonce:false,
				height: 'auto',
				caption : 'Please select the open source you want to set as nickname.', 
				loadComplete: function(id){
					var ids = $('#_ossSelectList').jqGrid('getDataIDs');
					$.each(ids,function(idx,rowId){
						var rowData = $('#_ossSelectList').jqGrid("getRowData",rowId);
						if(rowData.ossId == '${ossId}'){
							$('#_ossSelectList').jqGrid("setRowData",rowId, false, {'background-color' : '#a9a9a9'});
						}
					})
				},
				onSelectRow: function(id){
	 				var rowData = $('#_ossSelectList').jqGrid('getRowData',id);
	 				if(rowData.ossId == '${ossId}'){
	 					alertify.alert('<spring:message code="msg.oss.cannot.select" />', function(){});
					}
				}
			});			
		}

		// comment history
		$('.btnCommentHistory').on('click', function(e){
			e.preventDefault();
			openCommentHistory("/comment/popup/oss/${ossId}");
		});
		
	});
	
	function cveIdTag(cellvalue, options, rowObject){
		var tag = "<a href='https://web.nvd.nist.gov/view/vuln/detail?vulnId="+cellvalue+"' class='urlLink' target='_blank'>"+cellvalue+"</a>";
		return tag;
	}
	
	function unCveIdTag(cellvalue, options, rowObject){
		return cellvalue;
	}
	
	// 그리드 첫 로우 ossLicenseComb 설정
	function setFristComb(){
		$('#_licenseChoice').find("tr").eq(1).find("td").eq(3).text("");
	}
	
	// 그리드 삭제 버튼 표출
	function displayButtons(cellvalue, options, rowObject){
		var deleted = "<input type=\"button\" value=\"delete\" class=\"btnCLight darkgray\" onclick=\"exeDelete("+options.rowId+")\" />";
		return deleted;
	}
	
	// 그리드 삭제 버튼
	function exeDelete(rowId){
		// lastsel save
		$('#_licenseChoice').jqGrid('saveRow',lastsel);
		var list = [];
		data.copyData ? list = data.copyData.ossLicenses : list = data.list.rows;
		list.forEach(function(row,index){
			if(row.ossLicenseIdx == rowId ){
				list.splice(index,1);
			};
		});
		
		// 해당 리스트 삭제
		$('#_licenseChoice').jqGrid('delRowData', rowId);
		// 첫 로우 ossLicenseComb 설정
		setFristComb();
		makeBackGroundColor();
		
		//라이센스 타입 및 Obligation 자동 체크
		var type = autoLicense(list);
		var obligationHtml = autoObligation(list);
		if(list.length==0){
			type='';
			obligationHtml='';
		}
		
		$('#lt td').html(type);
		$('#ob td').html('');
		$(obligationHtml).appendTo('#ob td');
		
		//라이센스 묶음 텍스트 출력
		createMultiLicenseText();
		lastsel=-1;
	}

	function getLicenseGroup(groups){
		var numbers = [];
		
		groups.forEach(function(group){
			var number = compareLicenseGroupMax(group);
			numbers.push(number);
		});

		return numbers;
	}
		
	//라이센스 우선순위 자동 계산
	function autoLicense(data){
		var licenseCount = 0;
		data.forEach(function(data){
			if(data.licenseName){
				licenseCount += 1;
			}
		})
		
		var result = '';
		if(licenseCount != 0){
			//1. 그룹별 분류하기
			var groups = distributeGroups(data);
			var numbers = getLicenseGroup(groups);
			
			//2. 각 그룹끼리 비교하기(OR 비교)
			if(numbers.length != 1){
 				var min = getMin(numbers);
 				switch(min){
					case 1:
						result = 'Permissive';
						$('#licenseType').val("PMS");
						break;
					case 2:
						result = 'Weak Copyleft';
						$('#licenseType').val("WCP");
						break;
					case 3:
						result = 'Copyleft';
						$('#licenseType').val("CP");
						break;
					case 4:
						result = 'Proprietary Free';
						$('#licenseType').val("PF");
						break;
					case 5:	
						result = 'Proprietary';
						$('#licenseType').val("NA");
						break;
				}
			} else {
 				var min = numbers[0];
 				switch(min){
					case 1:
						result = 'Permissive';
						$('#licenseType').val("PMS");
						break;
					case 2:
						result = 'Weak Copyleft';
						$('#licenseType').val("WCP");
						break;
					case 3:
						result = 'Copyleft';
						$('#licenseType').val("CP");
						break;
					case 4:
						result = 'Proprietary Free';
						$('#licenseType').val("PF");
						break;
					case 5:	
						result = 'Proprietary';
						$('#licenseType').val("NA");
						break;
				}
			}
		}
		return result;
	}
	
	//Obligation 우선순위 자동계산
	function autoObligation(data){
		var licenseCount = 0;
		data.forEach(function(data){
			if(data.licenseName){
				licenseCount += 1;
			}
		})
		var result = '';
		if(licenseCount != 0){
			//1. 그룹별 분류하기
			var groups = distributeGroups(data);
			var numbers = getLicenseGroup(groups);
			
			//2. 각 그룹끼리 비교하기(OR 비교)
			if(numbers.length != 1) {
 				var min = getMin(numbers);
 				var number = compareObligationGroupMax(groups[numbers.indexOf(min)]);
 				switch(number){
					case 1:
						result = '<span></span>';
						break;
					case 2:
						result = '<span class=\"iconSet ops\" title=\"Notice\"></span>';
						$('input[name=obligationType]').val("10");
						break;
					case 3:
						result = '<span class=\"iconSet ops\" title=\"Notice\"></span><span class=\"iconSet man\" title=\"Source Code\"></span>';
						$('input[name=obligationType]').val("11");
						break;
				}
			} else {
 				var min = numbers[0];
 				var number = compareObligationGroupMax(groups[0]);
 				
 				switch(number){
					case 1:
						result = '<span></span>';
						break;
					case 2:
						result = '<span class=\"iconSet ops\" title=\"Notice\"></span>';
						$('input[name=obligationType]').val("10");
						break;
					case 3:
						result = '<span class=\"iconSet ops\" title=\"Notice\"></span><span class=\"iconSet man\" title=\"Source Code\"></span>';
						$('input[name=obligationType]').val("11");
						break;
				}
			}
		}
		
		return result;
	}
	
	//AND그룹으로 묶어서 분류하기
	function distributeGroups(data){
		var type='A';
		var groups =[];
		var groupA =[];
		var groupB =[];
		
		data.forEach(function(item,index,ref){
			if(item.ossLicenseComb == 'AND' || item.ossLicenseComb =="") {
				if(groupA.length > 0) {	//AND - AND 배열 결속
					
				}
				
				if(groupB.length > 0) {	//OR - AND 배열 결속
					groupA.push(groupB[0]);
					groupB = [];
				}
				
				groupA.push(item);	
			} else if(item.ossLicenseComb == 'OR') {
				if(groupA.length > 0){	//AND - OR 배열 변경 (이전까지 등록된 배열을 그룹에 등록후 배열 초기화)
					groups.push(groupA);
					groupA = [];
				}
				
				if(groupB.length > 0){  //OR - OR 배열 변경 (바로 전 등록된 배열을 그룹에 등록후 배열 초기화)
					groups.push(groupB);
					groupB = [];
				}
				
				groupB.push(item);
			}
			
			if(index == ref.length-1){	//마지막 순서일 때 최종 등록
				if(groupA.length > 0){
					groups.push(groupA);
				}
				
				if(groupB.length > 0){
					groups.push(groupB);
				}
			}
		});
		
		return groups;
	}
	/*
		AND그룹 내 라이센스 우선순위 비교
		(AND - Copyleft > Weak Copyleft > Permissive)		
	*/
	function compareLicenseGroupMax(group){
		var max = 0;
		var constant = {
			'Permissive' 		: 1,
			'Weak Copyleft' 	: 2,
			'Copyleft'  		: 3,
			'Proprietary Free' 	: 4,
			'Proprietary' 		: 5
		};
		group.forEach(function(item,index,ref){
			if(max < constant[item.licenseType]){
				max = constant[item.licenseType];
			}
		});
		return max;
	}
	/*
		AND그룹 내 Obligation 우선순위 비교
		(AND - Copyleft > Weak Copyleft > Permissive)		
	*/
	function compareObligationGroupMax(group){
		var max = 0;
		var constant = {
			'NNY' : 1,
			'NNN' : 1,
			'YNY' : 2,
			'YNN' : 2,
			'YYY' : 3,
			'YYN' : 3
		};
		group.forEach(function(item,index,ref){
			if(max < constant[item.obligationChecks]){
				max = constant[item.obligationChecks];
			}
		});
		return max;
	}
	
 	//숫자배열 중 최소값 찾기(AND조건의 역순)
 	function getMin(numbers){
 		var min = 9;
 		numbers.forEach(function(number){
 			if(min > number ){
 				min = number;
 			}
 		});
 		return min;
 	}
	
	//숫자배열 중 최대값 찾기(AND조건의 역순)
	function getMax(numbers){
		var max = 0;
		numbers.forEach(function(number){
			if(max < number ){
				max = number;
			}
		});
		return max;
	}
	
	//save동작 처리
	function saveRow(){
		$('#_licenseChoice').jqGrid('saveRow', lastsel);
		$('#_licenseChoice').jqGrid('resetSelection');	// 셀렉트 해제
	}
	
	function changeLicenseType() {

		$('#_licenseChoice').jqGrid('saveRow',lastsel);
		//var list = data.list.rows;
		var list = $('#_licenseChoice').jqGrid('getRowData');
		var type = autoLicense(list);
		var obligationHtml = autoObligation(list);
		
		if(list.length==0){
			type='';
			obligationHtml='';
		}
		
		$('#lt td').html(type);
		$('#ob td').html('');
		$(obligationHtml).appendTo('#ob td');
		$('#_licenseChoice').jqGrid('editRow',lastsel);
	}
	
	
	//라이센스 묶음 처리
	function createMultiLicenseText(){
		var rows = $('#_licenseChoice').jqGrid('getRowData');
		var markTxt = '';
		
		rows.forEach(function(row, index, ref){
			 // row로 들어오는 데이터가 텍스트인지 element인지 확인.
			var olc = row.ossLicenseComb, lnm = row.licenseNameEx;
			var ossLicenseComb, licenseName;
			var url = '#<c:url value="/license/edit/'+row.licenseId+'"/>';

			ossLicenseComb = /<[a-z][\s\S]*>/i.test(olc) ? $("#" + $(olc).attr("id")).val() : olc;
			licenseName = /<[a-z][\s\S]*>/i.test(lnm) ? $("#" + $(lnm).attr("id")).val() : lnm;
			
			// mark 텍스트 그리기
			if(index!=0){
				markTxt += ' <span> '+ossLicenseComb+' </span> ';
			}
			
			markTxt+='<a href="#none" onclick=createTabInFrame("'+row.licenseId+'_License","'+url+'")>'+licenseName+'</a>';
		});
		
		var andTxts = markTxt.split('<span> OR </span>');
		
		markTxt = '';
		
		andTxts.forEach(function(andTxt, index, ref){
			var isAnd = andTxt.split('AND').length > 1;
			if(isAnd){
				markTxt += '('+andTxt+')';
			}else{
				markTxt += andTxt;
			}
			if(index != ref.length-1){
				markTxt += '<span> OR </span>';
			}
		});
		
		if(markTxt.split('OR').length == 1 && markTxt[0] =='(' && markTxt[markTxt.length-1] == ')'){
			markTxt = markTxt.substring(1,markTxt.length-1);
		}
		
		if(markTxt.indexOf('undefined') == -1){
			$('.licenseMulti .mark').html(markTxt);
		}
	}
	
	function getOssGridRows(elementId){
		var grid = $(elementId);
		var dataToSend = [];
		var rows = grid.jqGrid('getDataIDs');
		for(idx=0; idx < rows.length; idx++) {
			grid.jqGrid('saveRow', rows[idx]);
			var rowData = grid.jqGrid('getRowData', rows[idx]);
			dataToSend.push(rowData);
		}
		return dataToSend;
	}
	
	function validationDataSet(){
		var licenseDiv = "";
		var licenseChoiceLength = $("#_licenseChoice").jqGrid("getDataIDs").length;

		switch (licenseChoiceLength) {
			case 0:
				alertify.alert('<spring:message code="msg.oss.required.license" />', function(){});
				return false;
				break;
			case 1:
				licenseDiv = "S";
				break;
			default:
				licenseDiv = "M";
				break;
		}
		
		var rows = getOssGridRows('#_licenseChoice');
		
		$('input[name=licenseDiv]').val(licenseDiv);
				
		var dataIds = $('#_licenseChoice').jqGrid('getDataIDs');
		var newRows = [];
		dataIds.forEach(function(dataId){
			var rowData = $('#_licenseChoice').jqGrid('getRowData',dataId);
			var licenseName = rowData.licenseName;
			if( licenseName.indexOf('<div') > -1){
				rowData['licenseName'] = '';
			}
			newRows.push(rowData);
		});
		
		$('input[name=ossLicensesJson]').val(JSON.stringify(newRows));
		
		$("[name='downloadLocations']").each(function(idx, cur){
			$(cur).val($(cur).val().trim());
		});
		
		$("[name='homepage']").each(function(idx, cur){
			$(cur).val($(cur).val().trim());
		});

		var nicknameFormatErrorFlag = true;
		$("[name='ossNicknames']").each(function(idx, cur){
			var nickname = $(cur).val();
			
			if(nickname.indexOf(",") > -1){
				fn.checkNickName(cur);
				nicknameFormatErrorFlag = false;
			}
		});

		if(!nicknameFormatErrorFlag){
			alertify.alert("Formatting error", function(){});
			
			return false;
		}

		return true;
	}
	
	//OSS등록로직
	function registSubmit(){
		if(validationDataSet()){
			$("#ossForm").ajaxForm({
				url :'<c:url value="/oss/validation"/>',
	            type : 'POST',
	            dataType:"json",
	            cache : false,
		        success : onValidSuccess,
	            error : onError
		    }).submit();
		}
	}
	
	// OSS 삭제 체크 (기존에 CONF 상태로 등록된 OSS가 있는지 여부 검사)
	function checkExistOssConf(){
		var ossId = $('input[name=ossId]').val();
		
		$.ajax({
			url : '<c:url value="/oss/checkExistOssConf"/>',
			type : 'GET',
			dataType : 'json',
			cache : false,
			data : {'ossId' : ossId},
			success : function(data){
				if(parseInt(data) > 0){
					$('.ossSelectPop').show();
					$('#blind_wrap').show();
				}else if(parseInt(data) == 0){
					deleteOssByOne();
				}else{
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	};	
	
	// OSS 삭제
	function deleteOssByOne(){
		loading.show();
		var ossId = $('input[name=ossId]').val();
		var ossName = $('input[name=ossName]').val();
		var editorVal = CKEDITOR.instances['editor'].getData();
		var sendData = {
				ossId:ossId,
				ossName:ossName,
				newOssId:"N", // 단건일때 "N" 로 보냄 
				comment:editorVal
			};
			
		$.ajax({
			url :'<c:url value="/oss/delAjax"/>',
            type : 'POST',
            data : sendData,
            dataType:"json",
            cache : false,
	        success : function(json){
	        	loading.hide();
				if(json.resCd == '10'){
					alertify.alert('<spring:message code="msg.common.success" />',function(){
						deleteTabInFrame('#<c:url value="/oss/edit/'+ossId+'"/>');
						reloadTabInframe('<c:url value="/oss/list"/>');
					});
				}else{
					alertify.error('<spring:message code="msg.common.valid" />', 0);
				}
	        },
            error : function(){
            	loading.hide();
            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
		});
	};
	
	//V-Diff 체크
	function checkVdiff() {
		var flag = "";
		if($('input[name=ossVersion]').val() == "") {
			var rows = getOssGridRows('#_licenseChoice');
			var postData = {'ossId' : $('input[name=ossId]').val(), 'ossName' : $('input[name=ossName]').val(), 'license' : JSON.stringify(rows)};

			$.ajax({
				url : '<c:url value="/oss/checkVdiff"/>',
				type : 'POST',
				data : JSON.stringify(postData),
				dataType : 'json',
				cache : false,
				async : false,
				contentType : 'application/json',
				success : function(json){
					flag = json.vFlag;
		        },
	            error : function(){
	            	alertify.error('<spring:message code="msg.common.valid2" />', 0);
	            }
			});
		} else {
			flag = "N";
		}

		return flag;
	};
	
	// 그리드 체크 메세지( gridStr 그리드 문자열 )
	function ossGridValidMsg(msgData,gridStr) {
		$.each(msgData,function(key,value) {
			if("isValid" != key) {
				if(key.indexOf(".") > -1){
					if(key.indexOf("ossNicknames") > -1){
						var _nickIndex = key.substring(key.lastIndexOf(".") +1, key.length) -1;
						$('input[name=ossNicknames]:eq('+_nickIndex+')').parent().next("span.retxt").html(value).show();
					} else {
						var seqSuffix = key.split(".");
						var rowId = seqSuffix[1];
						// licenseId 일 경우 에러 메세지 타겟을 licenseNameEx 로 변경
						if(seqSuffix[0] === "licenseId"){
							seqSuffix[0] = "licenseNameEx";
						}
						var colName = $("#"+gridStr+" #"+rowId).parents("table").attr("id")+"_"+seqSuffix[0];
						// 그리드 메세지 그리기
						$("#"+gridStr+" #"+rowId+" td[aria-describedby=\""+colName+"\"]")
						.append('<div class=\"'+gridStr+"_"+rowId+' retxt"\">'+ value +'</div>');						
					}
					$("div.retxt").show();
				} else {
					if(key == 'ossNicknames'){
						$('input[name=ossNicknames]').parent().next("span.retxt").html(value).show();
					}
					if($('input[name='+key+']').length > 0) {
						$('input[name='+key+']').next("span.retxt").html(value).show();
					} else if($('textarea[name='+key+']').length > 0) {
						$('textarea[name='+key+']').next("span.retxt").html(value).show();
					} else if($('select[name='+k+']').length > 0) {
						$('select[name='+key+']').parent().siblings("span.retxt").html(value).show();
					}
				}
				// 라셀 초기화
				lastsel = -1;
			}
		});
	};
	
	//유효성 체크 콜백함수
	function onValidSuccess(json, status){
		loading.hide();
		
		if(json.isValid == 'false') {
			if(json.validMsg == "hasDelNick") {
				// oss name을 변경하면서 nick name을 다시 확인 할 필요가 있는 경우
				
				// nick name 필드 초기화
				if($("div.multiTxtSet > div").length > 1) {
					$("div.multiTxtSet > div:not(:nth-child(1))").remove();
				}
				
				for(var k in json.resultData.addNickArr) {
					$(nickNameClone).appendTo('.multiTxtSet');
					$('.multiTxtSet input[type=text]:last').val(json.resultData.addNickArr[k]);
				}
				
				$('.smallDelete').on('click', function(){
					$(this).parent().remove();
				});
				
				var _alertMsg = '<spring:message code="msg.oss.nickname.exists"/>';
				if(json.resultData.delNickArr) {
					_alertMsg += '<br/><br/><b>Removed Nick Name List</b><span style="text-decoration:line-through;">';
					for(var k in json.resultData.delNickArr) {
						_alertMsg += '<br/>' + json.resultData.delNickArr[k];
					}
					_alertMsg += '</span>';
				}
				
				alertify.alert(_alertMsg, function(){});
			} else if(json.validMsg == "rename"){
				if(typeof json.resultData !== 'undefined'){
					var data = json.resultData;
					var _alertMsg  = '<spring:message code="msg.oss.check.rename"/>';
					for(var i in data){
						_alertMsg += '<br> - ' + data[i];
					}
					alertify.alert(_alertMsg, function(){});
				}else{
					alertify.alert('The OSS is the same, so it cannot be changed.', function(){});
				}
				
				renameFlag = 'N';
				$('input[name=renameFlag]').val(renameFlag);
			} else if(json.validMsg == "notChange"){
				alertify.alert('<spring:message code="msg.oss.name.nickname.sametime.notchange"/>', function(){});
			} else if(json.resultData) {
				var _alertMsg  = '<spring:message code="msg.oss.nickname.exists"/>';
					_alertMsg += '<br><br>' + 'When registering a new OSS or adding a version, it is not possible to delete the nickname of the existing OSS.';
				alertify.alert(_alertMsg, function(){});

				for(var k in json.resultData) {
					$(nickNameClone).appendTo('.multiTxtSet');
					$('.multiTxtSet input[type=text]:last').val(json.resultData[k]);
				}
				
				$('.smallDelete').on('click', function(){
					$(this).parent().remove();
				});
			} else if(json.validMsg == "deactivate"){
				alertify.error('<spring:message code="msg.oss.deactivated"/>', 0);
			} else if(json.ossName == "Formatting error"){
				alertify.error('<spring:message code="msg.common.valid"/>', 0);
				// 커스텀 에러 메세지 
				ossGridValidMsg(json, "_licenseChoice");
			}else {
				//alertify.error('<spring:message code="msg.common.valid"/>', 0);
				alertify.error('<spring:message code="msg.oss.duplicated"/>', 0);
				// 커스텀 에러 메세지 
				ossGridValidMsg(json, "_licenseChoice");
			}
		} else if(json.isValid == 'true') {
			var v_flag = checkVdiff();

			if(v_flag == "Y"){
				alertify.confirm('<spring:message code="msg.oss.confirm.ossVersion" />', function (e) {
					if (e) {
						onRegist();
					} else {
						return false;
					}
				});
			}else if(v_flag == ""){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
				return;
			}else{
				onRegist();
			}
		}		
	};

	//유효성 체크 콜백함수
	function onDeleteValidSuccess(json, status){
		loading.hide();
		
		if(json.isValid == 'false') {
			if(json.validMsg == "hasDelNick") {
				// oss name을 변경하면서 nick name을 다시 확인 할 필요가 있는 경우
				
				// nick name 필드 초기화
				if($("div.multiTxtSet > div").length > 1) {
					$("div.multiTxtSet > div:not(:nth-child(1))").remove();
				}
				
				for(var k in json.resultData.addNickArr) {
					$(nickNameClone).appendTo('.multiTxtSet');
					$('.multiTxtSet input[type=text]:last').val(json.resultData.addNickArr[k]);
				}
				$('.smallDelete').on('click', function(){
					$(this).parent().remove();
				});
				var _alertMsg = '<spring:message code="msg.oss.nickname.exists"/>';
				if(json.resultData.delNickArr) {
					_alertMsg += '<br/><br/><b>Removed Nick Name List</b><span style="text-decoration:line-through;">';
					for(var k in json.resultData.delNickArr) {
						_alertMsg += '<br/>' + json.resultData.delNickArr[k];
					}
					_alertMsg += '</span>';
				}
				alertify.alert(_alertMsg, function(){});
			} else if(json.resultData) {
				alertify.alert('<spring:message code="msg.oss.nickname.exists"/>', function(){});
				for(var k in json.resultData) {
					$(nickNameClone).appendTo('.multiTxtSet');
					$('.multiTxtSet input[type=text]:last').val(json.resultData[k]);
				}
				$('.smallDelete').on('click', function(){
					$(this).parent().remove();
				});

			} else {
				alertify.error('<spring:message code="msg.common.valid"/>', 0);
				// 커스텀 에러 메세지 
				ossGridValidMsg(json, "_licenseChoice");
			}

		} else if(json.isValid == 'true') {
			var v_flag = checkVdiff();

			if(v_flag == ""){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
				return;
				
			}else{
				checkExistOssConf();
			}
		}		
	};
	
	//저장함수
	function onRegist(){
		loading.show();
		saveRow();
		var editorVal = CKEDITOR.instances.editor.getData();
		$('input[name=comment]').val(replaceWithLink(editorVal));
		$("#ossForm").ajaxForm({
			url :'<c:url value="/oss/saveAjax"/>',
            type : 'POST',
            dataType:"json",
            cache : false,
	        success : onRegistSuccess,
            error : onError
	    }).submit();
	};
	
	//등록성공콜백함수
	function onRegistSuccess(json, status){
		loading.hide();
		if(json.resCd == '10'){
			alertify.alert('<spring:message code="msg.common.success" />', function(){
				if(opener){
					// 현재 창이 팝업인 경우
					self.opener = null;self.close();
				} else {
					deleteTabInFrame('#<c:url value="/oss/edit/'+json.ossId+'"/>');
					reloadTabInframe('<c:url value="/oss/list"/>');
				}
			});
		}else{
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
			renameFlag = 'N';
			$('input[name=renameFlag]').val(renameFlag);
		}
	};
	// 색 설정
	function makeBackGroundColor(){
		
		var colors = [];
		var list = [];
		if(data.copyData){
			list = data.copyData.ossLicenses;
		}else{
			list = data.list.rows;
		}
		<c:forEach var="colors" items='${ct:getCodeNames(ct:getConstDef("CD_LICENSE_BACKGROUND"))}' varStatus="vs">
		colors.push("${colors}");
		</c:forEach>
		var lastColor = colors[0];
		for(var i=0; i<list.length; i++){
			var comb = list[i].ossLicenseComb;
			if(comb == 'AND'){
				$("#_licenseChoice").jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});
			}else if(comb == 'OR'){
				var flag = false;
				for(var j = 0; j<colors.length;j++){
					if(lastColor == colors[j]){
						lastColor = colors[j+1];
						$("#_licenseChoice").jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});
						flag = true;
					}else if(lastColor == colors[colors.length-1]){
						lastColor = colors[0];
						$("#_licenseChoice").jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});
						flag = true;
					}
					if(flag) break;
				}
			}
		}
	}
	
	function onPopupListSuccess(json, status){
		$('#_ossSelectList').jqGrid('clearGridData');
		$('#_ossSelectList').jqGrid('setGridParam',{data:json}).trigger('reloadGrid');
		
	}
	
	//에러엘럿
	function onError(data, status){
		alertify.error('<spring:message code="msg.common.valid2" />', 0);
		renameFlag = 'N';
		$('input[name=renameFlag]').val(renameFlag);
    };
    
	function deleteComment(obj){
		if(!confirm('<spring:message code="msg.oss.confirm.delete.comment" />')) return;
		var commId = $(obj).next().val();
		$.ajax({
			url : '<c:url value="/oss/deleteComment"/>',
			type : 'POST',
			dataType : 'json',
			cache : false,
			data : {'commId' : commId},
			success : function(){
				fn_commemt.getCommentList();
			},
			error : function(){
				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			}
		});
	}
function modifyComment(obj){
	$('.commModifyPop').show();
	$('#blind_wrap').show();
	var commId = $(obj).next().next().val();
	modifyCommentId = commId;
	var contents = $(obj).parent().parent().next().html();
	CKEDITOR.instances.editor3.setData(contents);
	
	//코멘트 수정
	$('.closeModComment').off("click").on("click", function(){
		$('.commModifyPop').hide();
		$('#blind_wrap').hide();	
	});
	
	$('.modifyComment').off("click").on("click", function(){
        var editorVal = CKEDITOR.instances.editor3.getData();
		var register = '${sessUserInfo.userId}';
		var param = {commId : modifyCommentId, referenceId : data.detail.ossId, referenceDiv :'40', contents : replaceWithLink(editorVal)};
		$.ajax({
			url : '<c:url value="/oss/saveComment"/>',
			type : 'POST',
			dataType : 'json',
			cache : false,
			data : param,
			success : function(json){
				alertify.success('<spring:message code="msg.common.success" />');
				if(json.isValid) {
					if(json.isValid == 'false') {
						createValidMsgComplex(json);
						alertify.error('<spring:message code="msg.common.valid" />', 0);
						
					} else if(json.isValid == 'true') {
						alertify.alert('<spring:message code="msg.common.success" />', function(){});
					}	
				} else {
					alertify.alert('<spring:message code="msg.common.success" />', function(){});
				}
				$('.commModifyPop').hide();
				$('#blind_wrap').hide();

				fn_commemt.getCommentList();
			},
			error : function(){}
		});
	});
}

function showLicenseText(_licenseName) {
	if(!_licenseName) {
		var licenseChoiceLength = $("#_licenseChoice").jqGrid("getDataIDs").length;

		switch (licenseChoiceLength) {
			case 0:
				alertify.alert('<spring:message code="msg.oss.required.license" />', function(){});
				return false;
				break;
			case 1:
				$("input[name=licenseDiv]").val("S");
				break;
			default:
				$("input[name=licenseDiv]").val("M");
				break;
		}
		
		var _selectedRow = $("#_licenseChoice").jqGrid('getGridParam', "selrow" );
		if(_selectedRow) {
			var licenseNameEx = $('#_licenseChoice').jqGrid('getRowData',_selectedRow,'licenseNameOrg');
			licenseName = licenseNameEx['licenseNameEx'];
		} else {
			if($("#_licenseChoice").jqGrid("getDataIDs").length > 0) {
				_selectedRow = $("#_licenseChoice").jqGrid("getDataIDs")[0];
				licenseName = $('#_licenseChoice').jqGrid('getCell',_selectedRow,'licenseNameEx');
			}
		}
	} else {
		licenseName = _licenseName;
	}

	if(licenseName && licenseName != "") {
		$.ajax({
			url : '<c:url value="/license/getLicenseText"/>',
			type : 'GET',
			dataType : 'json',
			cache : false,
			data : {licenseName : licenseName},
			success : function(data){
				if("false" != data.isValid) {
					$("#disp_licenseText").html(data.validMsg);
					licenseName = "";
				} else {
					$("#disp_licenseText").html("");
				}
			},
			error : function(){
				$("#disp_licenseText").html("");
			}
		});
	} else {
		$("#disp_licenseText").html("");
	}
}

// 다른 OSS로 이관하는 경우
function mergeOssForDelete(newOssId) {

		$('.ossSelectPop').hide();
		$("#_ossMergeCheckList").jqGrid('GridUnload');
		$('.mergeOssCheckPop').show();
		//$("#div_mergeOssCheckPop_change").text(${ossId} + " => ");
		
		$('#_ossMergeCheckList').jqGrid({
			url:'<c:url value="/oss/ossMergeCheckList/${ossId}/'+newOssId+'"/>',
			datatype: 'json',
			jsonReader:{
				repeatitems: false,
				id:'ossId',
				root:function(obj){return obj.rows;}
			},
			colNames:['ID', 'Merge', 'Version', 'License', 'License Type', 'Obligation', 'Copyright', 'Download Location', 'HomePage', 'delOssId'],
			colModel:[
				{name:'ossId', index: 'ossId', width:70, align:"center"},
				{name:'mergeStr', index: 'mergeStr', width:70, align:"center"},
				{name:'ossVersion',index:'ossVersion', width:80, align:"left"},
				{name:'licenseName',index:'licenseName', width:150, align:"left"},
				{name:'licenseType',index:'licenseType', width:80, align:"left"},
				{name:'obligation',index:'obligation', width:80, align:"left"},
				{name:'copyright',index:'copyright', width:150, align:"left"},
				{name:'downloadLocation',index:'downloadLocation', width:100, align:"left"},
				{name:'homepage',index:'homepage', width:100, align:"left"},
				{name:'delOssId',index:'delOssId', hidden:true}
			],
			autoencode: true,
			rowNum: 9999,
 			autowidth: true,
			gridview: true,
			sortname: 'ossVersion',
			viewrecords: true,
			sortorder: 'desc',
			loadonce:true,
			height: 'auto',
			loadComplete: function(id){
				$('#_ossMergeCheckList').jqGrid("setRowData",'${ossId}', false, {'background-color' : '#ada9a9'});
			},
			onSelectRow: function(id){
			},
			ondblClickRow: function(rowid,iRow,iCol,e) {
				// 체크 박스 영역 제외
				var row = $('#_ossMergeCheckList').jqGrid('getRowData', rowid);
				
				// 자기자신이 아닌 경우만
				if('Duplicated' == row['mergeStr'] && row['delOssId'] && row['delOssId'] != '${ossId}') {
					createTabInFrame(row['delOssId']+'_Opensource', '#<c:url value="/oss/edit/'+row['delOssId']+'"/>');
				}
				else if(row['ossId'] != '${ossId}') {
					createTabInFrame(row['ossId']+'_Opensource', '#<c:url value="/oss/edit/'+row['ossId']+'"/>');
				}

			}
		});	
}


var fn_commemt = {
    getCommentList : function(){
    	if(data.copyData){
    		$('.commentList').remove();
        }else{
        	$.ajax({
            	url : '<c:url value="/comment/getDivCommentList"/>',
                type : 'GET',
                dataType : 'json',
                cache : false,
                data : {
                    referenceId : $('input[name=ossId]').val(),
                    referenceDiv : '40'
                },
                success : function(data){
                	$('#commentListArea').children().remove();
    				
                	if(data.length != 0) {
    					for(var i = 0; i < data.length; i++) {
    						var commId = data[i].commId;
    						$('#commentListArea').append(commentTemp.html());
    						var temp = $('dl[name=commentClone]');
    						
    						if(data[i].status == "" || data[i].status == null || data[i].status == "undefined") {
    							temp.find('.nameArea').text(data[i].creator);
    						} else {
    							temp.find('.nameArea').text(data[i].status).append("</br>"+data[i].creator);
    						}
    						
    						temp.find('.dateArea').text(data[i].createdDate);
    						temp.find('.commentContentsArea').html(replaceWithLink(data[i].contents));
    						temp.find('input[name=commId]').val(commId);
    						temp.removeAttr('name');
    					}	
    				} else {
    					$('#commentListArea').append('<p class="noneTxt">No comments were registered.</p>');
    				}
                },
                error : function(xhr, ajaxOptions, thrownError){
                    alertify.error('<spring:message code="msg.common.valid2" />', 0);
                }
            });
        }
    },
    deleteComment : function(_commId){
        if(!confirm('<spring:message code="msg.oss.confirm.delete.comment" />')) return;
        $.ajax({
        	url : '<c:url value="/comment/deleteComment"/>',
            type : 'POST',
            dataType : 'json',
            cache : false,
            data : {'commId' : _commId},
            success : function(data){
                alertify.success('<spring:message code="msg.common.success" />');
                fn_commemt.getCommentList();
            },
            error : function(){
                alertify.error('<spring:message code="msg.common.valid2" />', 0);
            }
        });
    },
    editComment : function(_commId){
        if(CKEDITOR.instances['comm_editor_'+_commId]) {
            var _editor = CKEDITOR.instances['comm_editor_'+_commId];
            _editor.destroy();
        }
        _editor = CKEDITOR.replace('comm_editor_'+_commId);

        $("#spanBtnArea_"+_commId+" > .btnViewMode").hide();
        $("#spanBtnArea_"+_commId+" > .btnEditMode").show();
        
        $("#spanBtnArea_"+_commId+" > .closeModComment").off("click").on("click", function(e){
            fn_commemt.createNonToolbarEditor(_commId);
            $("#spanBtnArea_"+_commId+" > .btnViewMode").show();
            $("#spanBtnArea_"+_commId+" > .btnEditMode").hide();
        });
        
        $("#spanBtnArea_"+_commId+" > .modifyComment").off("click").on("click", function(e){
            var _referenceId = $('input[name=ossId]').val();
            var param = {commId : _commId, contents : replaceWithLink(_editor.getData()), referenceDiv: '40', referenceId: _referenceId};
            $.ajax({
            	url : '<c:url value="/comment/updateComment"/>',
                type : 'POST',
                dataType : 'json',
                cache : false,
                data : param,
                success : function(json){
                    fn_commemt.createNonToolbarEditor(_commId);
                    $("#spanBtnArea_"+_commId+" > .btnViewMode").show();
                    $("#spanBtnArea_"+_commId+" > .btnEditMode").hide();
                    alertify.success('<spring:message code="msg.common.success" />');
                },
                error : function(){
                    alertify.error('<spring:message code="msg.common.valid2" />', 0);
                }
            });
            
            return false;
        });
        
    },
    createNonToolbarEditor : function(_commId) {

        var _editor = CKEDITOR.instances['comm_editor_'+_commId];
        var editorVal = _editor.getData();
        if(_editor) {
            _editor.destroy();
        }
        if($('#comm_editor_'+_commId).html().length > 0) {
            CKEDITOR.replace('comm_editor_'+_commId, 
                    {
            	customConfig:'<c:url value="/js/customEditorConf_Comment.js"/>'
                    });
        }
        CKEDITOR.instances['comm_editor_'+_commId].setData(replaceWithLink(editorVal));
    }
}

var fn = {
	urlDuplication : function(target){
		var value = $(target).val();

		if(value.charAt(value.length-1) == "/"){
			value = value.slice(0, -1); // 마지막 문자열 제거
			$(target).val(value);
		}
		
    	$("input[name=validationType]").val('DOWNLOADLOCATION');
    	$("[name='downloadLocation']").val(value);
		$("#ossForm").ajaxForm({
			url :'<c:url value="/oss/urlDuplicateValidation"/>',
            type : 'POST',
            dataType:"json",
            cache : false,
	        success : function(json) {
	        	if(json.externalData2){
		        	$(target).parent().next("span.urltxt").empty();
					$(target).parent().next("span.urltxt").html(json.externalData2.downloadLocation).show();
	        	}else{
		        	$(target).parent().next("span.urltxt").empty();
	        	}
	        },
            error : onError
	    }).submit();
	},
	urlDuplicationAll : function(){
		$("[name='downloadLocations']").each(function(idx, cur){
			var value = $(cur).val();

			if(value.charAt(value.length-1) == "/"){
				value = value.slice(0, -1); // 마지막 문자열 제거
				$(cur).val(value);
			}
		});
		
		$("input[name=validationType]").val('DOWNLOADLOCATIONS');
		$("#ossForm").ajaxForm({
			url :'<c:url value="/oss/urlDuplicateValidation"/>',
            type : 'POST',
            dataType:"json",
            cache : false,
	        success : function(json) {
	        	if(json.externalData2){
	        		var diffMsg = json.externalData2.downloadLocations.split("||");
					
					$("[name='downloadLocations']").each(function(idx, cur){
						var downloadLocation = $(cur).val();
						var msg = diffMsg.filter(function(a){
					        return a.indexOf(downloadLocation) > -1;
					    })[0];
						
						if(msg){
							msg = msg.split("@@")[1];	
							$(cur).parent().next("span.urltxt").empty();
							$(cur).parent().next("span.urltxt").html(msg).show();
						}						
					});
					
	        	}else{
	        		$(".multiDownloadLocationSet > .required > span.urltxt").empty();
	        	}
	        	
	        	fn.homepageDuplication($('input[name=homepage]'));
	        },
            error : onError
	    }).submit();
	},
	homepageDuplication : function(target){
		var value = $(target).val();

		if(value.charAt(value.length-1) == "/"){
			value = value.slice(0, -1); // 마지막 문자열 제거
			$(target).val(value);
		}
		
		$("input[name=validationType]").val('HOMEPAGE');
		
		$("#ossForm").ajaxForm({
			url :'<c:url value="/oss/urlDuplicateValidation"/>',
            type : 'POST',
            dataType:"json",
            cache : false,
	        success : function(json) {
	        	if(json.externalData2){
					$(target).next("span.urltxt").empty();
					$(target).next("span.urltxt").html(json.externalData2.homepage).show();
	        	}else{
					$(target).next("span.urltxt").empty();
	        	}
	        },
            error : onError
	    }).submit();
	},
	checkValid : function(){
		var result = gLicenseData.filter(function(a){
			return a == "N";
		});
		
		if(result.length > 0){
			alertify.alert('<spring:message code="msg.oss.required.auto" />', function(){});
			return false;
		}
		return true;
	},
	checkNickName : function(target){
		var targetVal = $(target).val();

		if(targetVal.indexOf(",") > -1){
			$(target).parent().next("span.retxt").html("Formatting error").show();
		} else {
			$(target).parent().next("span.retxt").hide();
		}
	},
	replaceGetParamChar : function(_param) {
		_param = _param.replace(/&/g,"%26");
		_param = _param.replace(/\+/g,"%2B"); 
		 return _param;
	},
	save : function() {
		if(fn.checkValid()){
			// 폼 에러 메세지 숨기기
			$("span.retxt").hide();
			// 그리드 에러 메세지 지우기
			$("div.retxt").remove();

			// 멀티 일경우 라이센스는 1건 이상이어야 한다.
			var licenseDiv = "";
			var licenseChoiceLength = $("#_licenseChoice").jqGrid("getDataIDs").length;

			switch (licenseChoiceLength) {
				case 0:
					alertify.alert('<spring:message code="msg.oss.required.license" />', function(){});
					return false;
					break;
				case 1:
					$("input[name=licenseDiv]").val("S");
					licenseDiv = "S";
					break;
				default:
					$("input[name=licenseDiv]").val("M");
					licenseDiv = "M";
					break;
			}

			var rows = getOssGridRows('#_licenseChoice');
			var dataIds = $('#_licenseChoice').jqGrid('getDataIDs');
			var gridStr = "_licenseChoice";
			var jsValidResult = true;
		
			dataIds.forEach(function(dataId){
				var rowData = $('#_licenseChoice').jqGrid('getRowData',dataId);
				var licenseName = rowData.licenseNameEx;

				if(checkLicenseSelected(licenseName) == null) {
					var errRow = $("#"+gridStr+" #"+dataId+" td[aria-describedby='"+gridStr + "_licenseNameEx']");

					if(errRow) {
						errRow.append('<div class=\"'+gridStr+"_"+dataId+' retxt"\">'+ '<spring:message code="msg.oss.unknown.license" />' +'</div>');
					}

					$("div.retxt._licenseChoice_"+dataId).show();
					
					jsValidResult = false;
				}
			});
			
			if(!jsValidResult || !detectedLicenseValid) {
				alertify.error('<spring:message code="msg.common.valid" />', 0);
				$("span.retxt").show();
				
				return false;
			}

			var editorVal = CKEDITOR.instances.editor.getData();
			var localDeactivateFlag = $("[name='deactivateFlag']").val();

			if(editorVal == "") {
				if(deactivateFlag == "N" 
					&& localDeactivateFlag == "Y"){ // deactivate Flag checked
					alertify.alert('<spring:message code="msg.oss.required.deactivate" />', function(){});
					return false;
				}

				if(deactivateFlag == "Y" 
					&& localDeactivateFlag == "N"){ // deactivate Flag unchecked
					alertify.alert('<spring:message code="msg.oss.required.activate" />', function(){});
					return false;
				}
			}
			
			alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
				if (e) {
					// 세이브 전 그리드 처리
					$('#_licenseChoice').jqGrid('saveRow',lastsel);
					
					// 최종 세이브 호출
					registSubmit();
				} else {
					return false;
				}
			});
		}
	}
}
</script>
