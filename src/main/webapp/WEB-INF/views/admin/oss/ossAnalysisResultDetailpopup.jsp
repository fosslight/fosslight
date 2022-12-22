<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<style>
			.groupSet{clear:left; margin-left:15px;}
			.groupSet:after {content:""; display:block; clear:both;}
			.detailView1 {float:left;display:inline-block;width:33%;}
			.detailView2 {float:left;display:inline-block;width:33%;}
			.detailView3 {float:left;display:inline-block;width:33%;}
		</style>
		<script type="text/javascript">
		
		const nickNameCloneDefaultTop = 7;
		const nickNameCloneTopAddVal = 24;
		
		$(document).ready(function() {
			'use strict';
			
			Ctrl_fn.init();
		});
		var lastsel = {
				"1" : "",
				"2" : "",
				"3" : ""
		};
		
		var licenseTags=[];
		var commentTemp = '';
		var gLicenseData = {
				"1" : new Array(),
				"2" : new Array(),
				"3" : new Array()
		};
		var detectedLicenseValid = {
				"1" : true,
				"2" : true,
				"3" : true
		};
		
		var Ctrl_fn = {
			init: function(){
				evt.init();
				Ctrl_fn.getAnalysisResultDetailData();
				common_data.init();
			},
			initEvent : function(){},
			setSelectTitle : function(){
				var options = common_data.detailData.reduce(function(arr, cur){
				    var option = "<option value='" + cur.gridId + "'>"+cur.title.replace(/(<([^>]+)>)/ig,"")+"</option>";
				    arr.push(option);
				    return arr;
				}, ["<option value=''></option>"]).join("");

				$("[id^=selectTitle]").html(options);
			},
			selectTitle : function(seq){
				var gridId = $("#selectTitle"+seq).val();
				var selectData = common_data.detailData.filter(function(cur){
				    return cur.gridId == gridId;
				})[0];
				
				$("#licenseName"+seq).val("");
				$("#detailOssName"+seq).val(Ctrl_fn.removeTag(selectData.ossName));
				$("#detailOssVersion"+seq).val(selectData.ossVersion);
				$("#detailLicenseName"+seq).val(selectData.licenseName);
				$("#detailCopyright"+seq).val(selectData.ossCopyright);
				$("#detailDownloadLocation"+seq).val(selectData.downloadLocation);
				$("#detailHomePage"+seq).val(selectData.homepage);
				$("#detailSummaryDescription"+seq).val(selectData.summaryDescription);
				$("#detailcomment"+seq).val(selectData.comment);

				$(".detailNickName"+seq+" > *").remove(); // nickName clear
				$(".detailDownloadLocation"+seq+" > *").remove(); //downloadLocation clear
				$(".detailDetectedLicense"+seq+" > *").remove(); //detectedLicense clear
				
				$("#lt"+seq+" td").html('');
				$("#ob"+seq+" td").html('');
				
				selectData.ossNickname.split(",").forEach(function(nickName){
				    if(nickName != ''){
				    	$(common_data.nickNameClone).appendTo(".detailNickName"+seq);
						$(".detailNickName"+seq+" input[type=text]:last").val(nickName);
						$(".smallDelete").on('click', function(){
							$(this).parent().parent().remove();
						});
				    }
				});
				
				selectData.downloadLocation.split(",").forEach(function(downloadLocation){
				    if(downloadLocation != ''){
						$(common_data.downloadLocationClone).appendTo(".detailDownloadLocation"+seq);
						$(".detailDownloadLocation"+seq+" input[type=text]:last").val(downloadLocation);
						$(".smallDelete").on('click', function(){
							$(this).parent().parent().remove();
						});

						$("#ossForm"+seq+" .multiDownloadLocationSet > div > span > input[name='downloadLocations']").each(function(idx, cur){
							$(cur).attr("onblur", "Ctrl_fn.urlDuplication(this)");
						});
				    }
				});
				
				selectData.detectedLicense.split(",").forEach(function(licenseName){
					if(licenseName != ''){
						var cloneData = common_data.detectLicenseClone.split("autoComOssLicense1").join("autoComOssLicense"+seq); // autoComplete 기능으로 인해 cloneData를 별도로 가공함.
						$(cloneData).appendTo('.detailDetectedLicense'+seq);
						$(".detailDetectedLicense"+seq+" input[type=text]:last").val(licenseName);
						
						grid_fn.setCustomAutoComplete('single', '', seq);
						
						$('.smallDelete').on('click', function(){
							$(this).parent().parent().remove();
						});
					}
				});

				if(selectData.downloadLocation){
					Ctrl_fn.urlDuplication($("#ossForm"+seq+" input[name=downloadLocations]"));
				}
				
				if(selectData.homepage){
					Ctrl_fn.homepageDuplication($("#ossForm"+seq+" input[name=homepage]"));
				}
				
				var replaceLicenseData = selectData.licenseName.replace(/\,/gi, ' AND ');
				var licenseData = replaceLicenseData.split(/\s(?=AND|OR)/g);
				var licenseDiv = licenseData.length > 1 ? "M" : "S";
				$("[name='licenseDiv"+seq+"']").eq(licenseDiv.toUpperCase() == "M" ? 1 : 0).trigger("click");

				common_data["list"+seq] = {rows:[]};
				var licenseName = "";
				
				for(var i in licenseData){
					var ossLicenseComb = "";
					var filteredLicenseData = "";
					var ossLicenseIdx = ""+(+i+1);
					
					licenseName = licenseData[i];
					
					if(licenseName != "") {
						if(i == 0) {
							filteredLicenseData = licenseTags.filter(function(a){ return a.value.toUpperCase() == licenseName.toUpperCase().trim() 
									|| a.shortIdentifier.toUpperCase() == licenseName.toUpperCase().trim(); })[0];
							ossLicenseComb = "";
						} else {
							var splitData = licenseData[i].split(/\s{1,2}/g);
							filteredLicenseData = licenseTags.filter(function(a){ return a.value.toUpperCase() == splitData[1].toUpperCase().trim() 
									|| a.shortIdentifier.toUpperCase() == splitData[1].toUpperCase().trim(); })[0];
							ossLicenseComb = splitData[0];
							
							if(splitData.length > 2) {
								licenseName = splitData.filter(function(cur) {
								    return cur != "AND" && cur != "OR"
								}).join(" ");
							} else {
								licenseName = splitData[1];
							}
						}
					}
					
					var licenseObject = Ctrl_fn.clone(common_data.cloneLicenseData);
					
					if(filteredLicenseData != "" 
							&& filteredLicenseData != undefined 
							&& filteredLicenseData != null){
						licenseObject["licenseId"] = filteredLicenseData["licenseId"];
						licenseObject["licenseName"] = filteredLicenseData["licenseName"];
						licenseObject["licenseNameEx"] = filteredLicenseData["shortIdentifier"]||filteredLicenseData["licenseName"];
						licenseObject["licenseType"] = filteredLicenseData["licenseType"];
						licenseObject["obligation"] = filteredLicenseData["obligation"];
						licenseObject["obligationChecks"] = filteredLicenseData["obligationChecks"];
						licenseObject["ossLicenseComb"] = ossLicenseComb;
						licenseObject["ossLicenseIdx"] = ossLicenseIdx;
					} else {
						licenseObject["licenseName"] = licenseName;
						licenseObject["licenseNameEx"] = licenseName;
						licenseObject["ossLicenseComb"] = ossLicenseComb;
						licenseObject["ossLicenseIdx"] = ossLicenseIdx;
					}
					
					common_data["list"+seq]["rows"].push(licenseObject);
				}

				$("#_licenseChoice"+seq).jqGrid('clearGridData');
				
				$("#_licenseChoice"+seq).jqGrid('setGridParam',
				        { 
				            datatype: 'local',
				            data : common_data["list"+seq]["rows"]
				        }).trigger("reloadGrid");
				
				Ctrl_fn.setMessage(gridId, seq);
			},
			getAnalysisResultDetailData:function(){
				$("#loading_wrap_popup").show();
				
				var param = {"groupId" : common_data.groupId};
				
				$.ajax({
					url : '<c:url value="/oss/getSessionAnalysisResultData"/>',
					dataType : 'json',
					type:'POST',
					cache : false,
					data : JSON.stringify(param),
					contentType : 'application/json',
					success : function(resultData){
						if(resultData.isValid){
							common_data.detailData = resultData.detailData;
							common_data.cloneLicenseData = resultData.cloneLicenseData;
							common_data.validMapData = resultData.validMap;
							common_data.diffMapData = resultData.diffMap;
							Ctrl_fn.setSelectTitle(); // create title 
							
							{
								// default selected Data
								var datalength = common_data.detailData.length; 
								
								if(datalength > 0){
									$("#selectTitle1 > option:eq(1)").attr("selected", true);
									$("#selectTitle1").trigger("change");

									// 자동분석을 실패한 case, 단 자동분석 대상이 unconfirmed oss version인 경우 최신등록정보를 Detail화면에 보여줌.
									if(datalength == 2){ 
										$("#selectTitle2 > option:eq(2)").attr("selected", true);
										$("#selectTitle2").trigger("change");
									}
								}

								if(datalength <= 5 && datalength >= 4){
									$("#selectTitle2 > option:eq(4)").attr("selected", true);
									$("#selectTitle2").trigger("change");
									
									$("#selectTitle3 > option:eq(2)").attr("selected", true);
									$("#selectTitle3").trigger("change");
								}
								
								if(datalength == 6){
									$("#selectTitle2 > option:eq(5)").attr("selected", true);
									$("#selectTitle2").trigger("change");
									
									$("#selectTitle3 > option:eq(2)").attr("selected", true);
									$("#selectTitle3").trigger("change");
								}
							}

							$("#loading_wrap_popup").hide();
						} else {
							// message 출력
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
					},
					error : Ctrl_fn.onError
				});
			},
			removeTag: function(str){
				return str.replace(/(<([^]+)>)/ig,"");
			},
			clone : function(obj) {
				if (obj === null || typeof(obj) !== 'object') {
					return obj;
				}
					
				var copy = obj.constructor();
				
				for (var attr in obj) {
					if (obj.hasOwnProperty(attr)) {
						copy[attr] = obj[attr];
					}
				}
				
				return copy;
			},
			registSubmit : function(seq){
				var ossForm = '#ossForm'+seq;

				var licenseDiv = "";
				var licenseChoiceLength = $("#_licenseChoice"+seq).jqGrid("getDataIDs").length;

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

				var rows = grid_fn.getOssGridRows($("#_licenseChoice"+seq));
				
				$('#ossForm'+seq+' input[name=licenseDiv]').val(licenseDiv);
				var newRows = [];
				
				var dataIds = $('#_licenseChoice'+seq).jqGrid('getDataIDs');
				
				dataIds.forEach(function(dataId){
					var rowData = $('#_licenseChoice'+seq).jqGrid('getRowData',dataId);
					var licenseName = rowData.licenseName;
					if( licenseName.indexOf('<div') > -1){
						rowData['licenseName'] = '';
					}
					newRows.push(rowData);
				});
				
				$(ossForm + ' input[name=\'ossLicensesJson\']').val(JSON.stringify(newRows));
				
				$(ossForm + ' [name=\'downloadLocations\']').each(function(idx, cur){
					$(cur).val($(cur).val().trim());
				});
				
				$(ossForm + ' [name=\'homepage\']').each(function(idx, cur){
					$(cur).val($(cur).val().trim());
				});

				$(ossForm).ajaxForm({
					url :'<c:url value="/oss/validation"/>',
		            type : 'POST',
		            dataType:"json",
		            cache : false,
			        success : function(json, status){
			        	$("#loading_wrap_popup").hide();
			    		
			    		if(json.isValid == 'false') {
			    			if(json.validMsg == "hasDelNick") {
			    				// oss name을 변경하면서 nick name을 다시 확인 할 필요가 있는 경우
			    				
			    				// nick name 필드 초기화
			    				if($(".detailNickName"+seq+" > div").length > 1) {
			    					$(".detailNickName"+seq+" > div:not(:nth-child(1))").remove();
			    				}
			    				
			    				for(var k in json.resultData.addNickArr) {
			    					$(common_data.nickNameClone).appendTo(".detailNickName"+seq);
									$(".detailNickName"+seq+" input[type=text]:last").val(json.resultData.addNickArr[k]);
			    				}
			    				
			    				$(".smallDelete").on('click', function(){
		    						$(this).parent().parent().remove();
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
			    					$(common_data.nickNameClone).appendTo(".detailNickName"+seq);
									$(".detailNickName"+seq+" input[type=text]:last").val(json.resultData[k]);
			    				}

			    				$('.smallDelete').on('click', function(){
			    					$(this).parent().remove();
			    				});
			    			} else {
			    				alertify.error('<spring:message code="msg.common.valid"/>', 0);
			    				// 커스텀 에러 메세지 
			    				Ctrl_fn.ossGridValidMsg(json, "_licenseChoice"+seq, seq);
			    			}
			    		} else if(json.isValid == 'true') {
			    			var v_flag = Ctrl_fn.checkVdiff(seq);

			    			if(v_flag == "Y") {
			    				alertify.confirm('<spring:message code="msg.oss.confirm.ossVersion" />', function (e) {
			    					if (e) {
			    						Ctrl_fn.onRegist(seq);
			    					} else {
			    						return false;
			    					}
			    				});
			    			} else if(v_flag == "") {
			    				alertify.error('<spring:message code="msg.common.valid2" />', 0);
			    				return;
			    			} else {
			    				Ctrl_fn.onRegist(seq);
			    			}
			    		}				        
				    },
				    error : Ctrl_fn.onError
			    }).submit();
			},
			setMessage : function(gridId, seq){
				var ossForm = "#ossForm"+seq;
				$(ossForm + " span.retxt").hide();
				
				Object.keys(common_data.validMapData).forEach(function(cur, idx){
					var curGridId = cur.split(".")[1];
					
				    if(curGridId == gridId){
				        var message = common_data.validMapData[cur];
				        var key = cur.replace(/[\.\d]+/g, "");
				        
				        $('#ossForm'+seq+' [name=\''+key+'\']').next().show().text(message);
				    }
				});
			},
			urlDuplication : function(target){
				var value = $(target).val();
				var seq = $(target).attr("id").replace(/[^\d]+/g, "");
				
				if(value.charAt(value.length-1) == "/"){
					value = value.slice(0, -1); // 마지막 문자열 제거
					$(target).val(value);
				}
				
		    	$("input[name=validationType]").val('DOWNLOADLOCATION');
		    	$("[name='downloadLocation']").val(value);
				$("#ossForm"+seq).ajaxForm({
		            url :'/oss/urlDuplicateValidation',
		            type : 'POST',
		            dataType:"json",
		            cache : false,
			        success : function(json) {
			        	if(json.externalData2) {
				        	$(target).parent().next("span.urltxt").empty();
							$(target).parent().next("span.urltxt").html(json.externalData2.downloadLocation).show();
			        	} else {
				        	$(target).parent().next("span.urltxt").empty();
			        	}
			        },
			        error : Ctrl_fn.onError
			    }).submit();
			},
			urlDuplicationAll : function(target){
				var seq = $(target).attr("id").replace(/[^\d]+/g, "");
				$("#ossForm"+seq+" [name='downloadLocations']").each(function(idx, cur){
					var value = $(cur).val();

					if(value.charAt(value.length-1) == "/"){
						value = value.slice(0, -1); // delete last string
						$(cur).val(value);
					}
				});
				
				$("#ossForm"+seq+" input[name=validationType]").val('DOWNLOADLOCATIONS');
				
				$("#ossForm"+seq).ajaxForm({
					url :'<c:url value="/oss/urlDuplicateValidation"/>',
		            type : 'POST',
		            dataType:"json",
		            cache : false,
			        success : function(json) {
			        	if(json.externalData2){
			        		var diffMsg = json.externalData2.downloadLocations.split("||");
							
							$("#ossForm"+seq+" [name='downloadLocations']").each(function(idx, cur){
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
			        	} else {
			        		$(".detailDownloadLocation"+seq+" > .required > span.urltxt").empty();
			        	}
			        	
			        	Ctrl_fn.homepageDuplication($("#ossForm"+seq+" input[name=homepage]"));
			        },
		            error : Ctrl_fn.onError
			    }).submit();
			},
			homepageDuplication : function(target){
				var value = $(target).val();
				var seq = $(target).attr("id").replace(/[^\d]+/g, "");
				
				if(value.charAt(value.length-1) == "/"){
					value = value.slice(0, -1); // delete last string
					$(target).val(value);
				}
				
				$("input[name=validationType]").val('HOMEPAGE');
				
				$("#ossForm"+seq).ajaxForm({
					url :'<c:url value="/oss/urlDuplicateValidation"/>',
		            type : 'POST',
		            dataType:"json",
		            cache : false,
			        success : function(json) {
			        	if(json.externalData2) {
							$(target).next("span.urltxt").empty();
							$(target).next("span.urltxt").html(json.externalData2.homepage).show();
			        	} else {
							$(target).next("span.urltxt").empty();
			        	}
			        },
		            error : Ctrl_fn.onError
			    }).submit();
			},
			onSuccess : function(){},
			onError : function(data, status){
        		alertify.error('<spring:message code="msg.common.valid2" />', 0);
            },
            checkValid : function(seq){
            	var result = gLicenseData[seq].filter(function(a){
        			return a == "N";
        		});
        		
        		if(result.length > 0){
        			alertify.alert('<spring:message code="msg.oss.required.auto" />', function(){});
        			return false;
        		}
        		
        		return true;
            },
            checkVdiff : function(seq){
        		var flag = "";
        		if($('#ossForm'+seq+' input[name=ossVersion]').val() == "") {
        			var rows = grid_fn.getOssGridRows('#_licenseChoice'+seq);
        			var postData = {'ossId' : $('#ossForm'+seq+' input[name=ossId]').val(), 'ossName' : $('#ossForm'+seq+' input[name=ossName]').val(), 'license' : JSON.stringify(rows)};
        			
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
        	            error : Ctrl_fn.onError
        			});
        		} else {
        			flag = "N";
        		}

        		return flag;
        	},
        	onRegist : function(seq) {
        		$("#loading_wrap_popup").show();
        		$('#_licenseChoice'+seq).jqGrid('saveRow', lastsel[seq]);
        		$('#_licenseChoice'+seq).jqGrid('resetSelection');
        		
        		$("#ossForm"+seq).ajaxForm({
                    url : '<c:url value="/oss/saveAjax"/>',
                    type : 'POST',
                    dataType:"json",
                    cache : false,
        	        success : function(json, status){
        	        	$("#loading_wrap_popup").hide();
        	    		if(json.resCd == '10'){
       	    				Ctrl_fn.setComplete(json.ossId);
        	    		}else{
        	    			alertify.error('<spring:message code="msg.common.valid2" />', 0);
        	    		}
        	    	},
                    error : Ctrl_fn.onError
        	    }).submit();
        	},
        	ossGridValidMsg : function(msgData,gridStr, seq) {
            	var ossForm = '#ossForm'+seq;
            	
        		$.each(msgData,function(key,value) {
        			if("isValid" != key) {
        				if(key.indexOf(".") > -1){
        					if(key.indexOf("ossNicknames") > -1) {
        						var _nickIndex = key.substring(key.lastIndexOf(".") +1, key.length) -1;
        						
        						$(ossForm + ' input[name=ossNicknames]:eq('+_nickIndex+')').parent().next("span.retxt").html(value).show();
        					} else {
        						var seqSuffix = key.split(".");
        						var rowId = seqSuffix[1];

        						// licenseId 일 경우 에러 메세지 타겟을 licenseNameEx 로 변경
        						if(seqSuffix[0] === "licenseId"){
        							seqSuffix[0] = "licenseNameEx";
        						}

        						var colName = $("#"+gridStr+" #"+rowId).parents("table").attr("id")+"_"+seqSuffix[0];

        						// drawing grid message
        						$("#"+gridStr+" #"+rowId+" td[aria-describedby=\""+colName+"\"]")
        						.append('<div class=\"'+gridStr+"_"+rowId+' retxt"\">'+ value +'</div>');						
        					}
        					
        					$("div.retxt").show();
        				} else {
        					if(key == 'ossNicknames'){
        						$(ossForm + ' input[name=ossNicknames]').parent().next("span.retxt").html(value).show();
        					}
        					
        					if($(ossForm + ' input[name='+key+']').length > 0) {
        						$(ossForm + ' input[name='+key+']').next("span.retxt").html(value).show();
        					} else if($(ossForm + ' textarea[name='+key+']').length > 0) {
        						$(ossForm + ' textarea[name='+key+']').next("span.retxt").html(value).show();
        					} else if($(ossForm  + ' select[name='+k+']').length > 0) {
        						$(ossForm + ' select[name='+key+']').parent().siblings("span.retxt").html(value).show();
        					}
        				}
        				
        				// lastsel initialization.
        				lastsel = -1;
        			}
        		});
        	},
        	setComplete : function(ossId){
				var postData = {'componentId' : common_data.groupId, 'referenceOssId' : ossId};
				
        		$.ajax({
        			url : '<c:url value="/oss/updateAnalysisComplete"/>',
    				type : 'POST',
    				data : JSON.stringify(postData),
    				dataType : 'json',
    				cache : false,
    				async : false,
    				contentType : 'application/json',
    				success : function(data){
    					if("false" == data.isValid) {
							alertify.error('<spring:message code="msg.common.valid2" />', 0);
						} else {
							alertify.alert('<spring:message code="msg.common.success" />', function(){
				        		if(opener){
				        			opener.$('#ossList').getRowData().filter(function(cur){
					        			return cur.groupId == common_data.groupId;
						        	}).forEach(function(cur){
							        	var gridId = cur.gridId;
				        			    opener.$('#ossList').jqGrid('setCell', gridId, 'result', "Success");
				        			    opener.$("#"+gridId).addClass("excludeRow");
				        			    opener.$("#"+gridId + " > td > [type='checkbox']").attr({"disabled": true, "checked": false});
				        			}); // Remove all rows of the same group ID.
				        			
									self.opener = null;self.close(); // If the save is complete, the pop-up is currently closed.
								}
							});
						}
					},
    	            error : Ctrl_fn.onError
    			});
            }
		};

		var evt = {
			init : function(){
				$(".smallDelete").on('click', function(){
					$(this).parent().parent().remove();
				});

				$("input[id^=nickAdd]").on('click', function(){
					var nicknameSeq = $(this).attr("id").replace(/[^\d]+/g, "");
					$(common_data.nickNameClone).appendTo(".detailNickName"+nicknameSeq);
					$(".smallDelete").on('click', function(){
						$(this).parent().parent().remove();
					});
				});

				//Detect License Input 추가
				$('input[id^=detectedLicenseAdd]').on('click', function(){
					var seq = $(this).attr("id").replace(/[^\d]+/g, "");
					var cloneData = common_data.detectLicenseClone.split("autoComOssLicense1").join("autoComOssLicense"+seq); // autoComplete 기능으로 인해 cloneData를 별도로 가공함.
					$(cloneData).appendTo('.detailDetectedLicense'+seq);
					
					grid_fn.setCustomAutoComplete('single', '', seq);
					
					$('.smallDelete').on('click', function(){
						$(this).parent().parent().remove();
					});
				});
				
				$("input[id^=downloadLocationAdd]").on('click', function(){
					var seq = $(this).attr("id").replace(/[^\d]+/g, "");
					$(common_data.downloadLocationClone).appendTo(".detailDownloadLocation"+seq);
					$(".smallDelete").on('click', function(){
						$(this).parent().parent().remove();
					});

					$("#ossForm"+seq+" .multiDownloadLocationSet > div > span > input[name='downloadLocations']").each(function(idx, cur){
						$(cur).attr("onblur", "Ctrl_fn.urlDuplication(this)");
					});
				});

				$("[name='homepage']").on('blur', function(){
					Ctrl_fn.homepageDuplication(this);
				});

				$("[name='ossName']").on('blur', function(){
					Ctrl_fn.urlDuplicationAll(this);
				});

				$("[name='save']").on('click', function(){
					var seq = $(this).attr("id").replace(/[^\d]+/g, "");
					var ossForm = "#ossForm"+seq;

					if(Ctrl_fn.checkValid(seq)){
						// 폼 에러 메세지 숨기기
						$(ossForm + " span.retxt").hide()
						// 그리드 에러 메세지 지우기
						$(ossForm + " div.retxt").remove();
					
						// 멀티 일경우 라이센스는 1건 이상이어야 한다.
						var licenseDiv = "";
						var licenseChoiceLength = $("#_licenseChoice"+seq).jqGrid("getDataIDs").length;

						switch (licenseChoiceLength) {
						case 0:
							alertify.alert('<spring:message code="msg.oss.required.license" />', function(){});
							return false;
							break;
						case 1:
							$(ossForm + " input[name=licenseDiv]").val("S");
							licenseDiv = "S";
							break;
						default:
							$(ossForm + " input[name=licenseDiv]").val("M");
							licenseDiv = "M";
							break;
						}
					
						var rows = grid_fn.getOssGridRows('#_licenseChoice'+seq);
						var dataIds = $('#_licenseChoice'+seq).jqGrid('getDataIDs');
						var gridStr = "_licenseChoice"+seq;
						var jsValidResult = true;
					
						dataIds.forEach(function(dataId){
							var rowData = $('#_licenseChoice'+seq).jqGrid('getRowData',dataId);
							var licenseName = rowData.licenseNameEx;

							if(grid_fn.checkLicenseSelected(licenseName) == null) {
								var errRow = $("#"+gridStr+" #"+dataId+" td[aria-describedby='"+gridStr + "_licenseNameEx']");

								if(errRow) {
									errRow.append('<div class=\"'+gridStr+"_"+dataId+' retxt"\">'+ '<spring:message code="msg.oss.unknown.license" />' +'</div>');
								}

								$("div.retxt._licenseChoice"+seq+"_"+dataId).show();
								jsValidResult = false;
							}
						});
					
						if(!jsValidResult|| !detectedLicenseValid[seq]) {
							alertify.error('<spring:message code="msg.common.valid" />', 0);
							$("span.retxt").show();
							
							return false;
						}
					
						alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
							if (e) {
								// 세이브 전 그리드 처리
								$('#_licenseChoice'+seq).jqGrid('saveRow',lastsel);
								
								// 최종 세이브 호출
								Ctrl_fn.registSubmit(seq);
							} else {
								return false;
							}
						});
					}
				});
			}
		};
		
		var common_data = {
			detailData : "",
			validMapData : "",
			diffMapData : "",
			cloneLicenseData : "",
			groupId : "${groupId}",
			nickNameClone : "",
			downloadLocationClone : "",
			detectLicenseClone : "",
			nickNameCloneIdx : 1,
			list1 : ${'{rows:[]}'},
			list2 : ${'{rows:[]}'},
			list3 : ${'{rows:[]}'},
			init : function(){
				// multi대비한 clone 정보 set
				common_data.nickNameClone = $('.multiTxtSet').clone().html();
				common_data.downloadLocationClone = $('.multiDownloadLocationSet').clone().html();
				common_data.detectLicenseClone = $('.multiDetectedLicenseSet').clone().html();
				
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
						
						grid_fn.loadGrid($("#_licenseChoice1"), "1", true);
						grid_fn.loadGrid($("#_licenseChoice2"), "2", true);
						grid_fn.loadGrid($("#_licenseChoice3"), "3", true);
					}
				});
			}
		}

		var grid_fn = {
			loadGrid : function(_target, seq, initFlag){
				//자동완성 Single License 일때
				if(initFlag){
					grid_fn.setCustomAutoComplete('single', _target, seq);
				}
				// detailLicense Multi loadGrid
				_target.jqGrid(grid_fn.gridData(_target, seq));

				_target.jqGrid('navGrid',"#_licenseChoicePager"+seq,{edit:false,add:true,del:false,search:false,refresh:false
					, addfunc: function (rowid) {
						var ids = _target.jqGrid("getDataIDs");
						var newId = ids.length ? Number(ids[ids.length-1])+1 : 1;
						var row = {no	:newId, ossLicenseIdx	:newId};
						common_data["list"+seq]["rows"].push(row);
						_target.jqGrid("addRowData", newId , row, "last");
						_target.jqGrid("setSelection", newId);
						// 첫 로우 ossLicenseComb 설정
						grid_fn.setFristComb(_target);
					}
				});
			},
			gridData : function(_target, seq){
				var list = common_data["list"+seq]["rows"];
				
				return {
					datatype: 'local',
					data:list,
					colNames:['','','','License', 'Copyright', '','','','','license Type', 'obligationChecks'],
					colModel:[
						{name:'no', index: 'ossLicenseIdx', width:18, hidden:true},
						{name:'ossLicenseIdx', index: 'ossLicenseIdx', width:18, key:true, hidden:true},
						{name:'ossLicenseComb',index:'ossLicenseComb', width:40, align:"center", sortable:false, editable:true, edittype:"select", editoptions:{value:"AND:AND;OR:OR;WITH:WITH", dataEvents:[{type:'change', fn:grid_fn.changeLicenseType(_target, seq)}]}},
						{name:'licenseNameEx',index:'licenseNameEx', width:100, editable:true
							, editoptions: {
								dataInit: function (elem) {
									$(elem).focus(function () { grid_fn.setCustomAutoComplete('multi', _target, seq); }) 
			                	}
							}
						},
						{name:'ossCopyright',index:'ossCopyright', width:150, editable:true, edittype:"textarea", editoptions:{rows:"10",cols:"20"}},
						{name: 'delete', index: 'delete', width:43, align: 'center', sortable:false, formatter: grid_fn.displayButtons},
						{name:'licenseName',index:'licenseName', width:30, hidden:true},
						{name:'licenseId',index:'licenseId', width:30, hidden:true},
						{name:'licenseNameEx',index:'licenseNameOrg', width:30,hidden:true},
						{name:'licenseType',index:'licenseType', width:30,hidden:true},
						{name:'obligationChecks',index:'obligationChecks', width:30,hidden:true}
					],
					autoencode: true,
					editurl:'clientArray',
					pager: '#_licenseChoicePager'+seq,
					autowidth: true,
					height: 'auto',
					gridview: true,
					rownumbers: true,
					viewrecords: true,
					loadonce:false,
					onSelectRow: function(rowid){
						// 로울 클릭시 그리드 에러 메세지 삭제 
						$("div.retxt._licenseChoice_"+rowid).remove();
						// 현재 로우와 라스트 로우가 다를시
						if(rowid && rowid!==lastsel[seq]){
							_target.jqGrid('saveRow',lastsel[seq]);
							
							// license 존재 여부 체크
														
							//라이센스 묶음 텍스트 출력
							grid_fn.createMultiLicenseText(_target);
							// 라이센스 배경색 설정
							var list = common_data["list"+seq]["rows"];
							
							list.forEach(function(row,index){
								if(row.ossLicenseIdx == lastsel[seq] ){
									row.ossLicenseComb = $(_target).jqGrid('getCell',lastsel[seq],'ossLicenseComb');
								};
							});
							
							grid_fn.makeBackGroundColor(_target);
							// 로우 에디트
							_target.jqGrid('editRow',rowid);
							lastsel[seq]=rowid;
							// 첫 로우 ossLicenseComb 설정
							grid_fn.setFristComb(_target);
							//커스터마이징 자동완성 셀렉트
							grid_fn.setCustomAutoComplete('multi', _target, seq);
							//라이센스 타입 및 Obligation 자동 체크
							var type = grid_fn.autoLicense(list);
							var obligationHtml = grid_fn.autoObligation(list);
							
							if(list.length==0){
								type='';
								obligationHtml='';
							}
							
							$('#lt'+seq+' td').html(type);
							$('#ob'+seq+' td').html('');
							$(obligationHtml).appendTo('#ob'+seq+' td');

						}
					},
					afterInsertRow:function(rowid, rowdata, rowelem){},
					loadComplete:function(data){
						lastsel[seq] = -1;
						// 페이징 UI 설정
						hidePageNav('_licenseChoicePager'+seq);
						// 첫 로우 ossLicenseComb 설정
						grid_fn.setFristComb(_target);
						// 라이센스 묶음 텍스트 출력
						grid_fn.createMultiLicenseText(_target);
						// 라이센스 배경색 설정
						grid_fn.makeBackGroundColor(_target);
						//라이센스 타입 및 Obligation 자동 체크
						var type = grid_fn.autoLicense(data.rows);
						var obligationHtml = grid_fn.autoObligation(data.rows);
						
						if(data.rows.length==0){
							type='';
							obligationHtml='';
						}
						
						$('#lt'+seq+' td').html(type);
						$('#ob'+seq+' td').html('');
						
						$(obligationHtml).appendTo('#ob'+seq+' td');

						if (typeof(data.rows[0]) != "undefined"){
							if (data.rows[0].licenseName == "" || typeof(data.rows[0].licenseNameEx) == "undefined"){
								$(_target).clearGridData();
							}
						}
					}
				};
			},
			changeLicenseType : function(_target, seq) {
				$(_target).jqGrid('saveRow',lastsel[seq]);
				var list = $(_target).jqGrid('getRowData');
				var type = grid_fn.autoLicense(list);
				var obligationHtml = grid_fn.autoObligation(list);
				
				if(list.length==0){
					type = '';
					obligationHtml = '';
				}
				
				$('#lt'+seq+' td').html(type);
				$('#ob'+seq+' td').html('');
				$(obligationHtml).appendTo('#ob'+seq+' td');
				$(_target).jqGrid('editRow',lastsel[seq]);
			},
			displayButtons : function(cellvalue, options, rowObject){
				var deleted = "<input type=\"button\" value=\"delete\" class=\"smallDelete\" onclick=\"grid_fn.exeDelete("+options.rowId+", "+options.gid+")\" />";

				return deleted;
			},
			setFristComb : function(_target){
				$(_target).find("tr").eq(1).find("td").eq(3).text("");
			},
			createMultiLicenseText : function(_target){
				var rows = $(_target).jqGrid('getRowData');
				var seq = _target.attr("id").replace(/[^\d]+/g, "");
				var markTxt = '';
				
				rows.forEach(function(row, index, ref){
					 // row로 들어오는 데이터가 텍스트인지 element인지 확인.
					var olc = row.ossLicenseComb, lnm = row.licenseNameEx;
					var ossLicenseComb, licenseName;	

					ossLicenseComb = /<[a-z][\s\S]*>/i.test(olc) ? $("#" + $(olc).attr("id")).val() : olc;
					licenseName = /<[a-z][\s\S]*>/i.test(lnm) ? $("#" + $(lnm).attr("id")).val() : lnm;
					
					// mark 텍스트 그리기
					if(index!=0){
						markTxt += ' <span> '+ossLicenseComb+' </span> ';
					}
					markTxt+='<span style="color:#0070c0">'+licenseName+'</span>';
				});
				
				var andTxts = markTxt.split('<span> OR </span>');
				
				markTxt = '';
				
				andTxts.forEach(function(andTxt, index, ref){
					var isAnd = andTxt.split('AND').length > 1;

					if(isAnd) {
						markTxt += '('+andTxt+')';
					} else {
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
					$('.licenseMulti .mark'+seq).html(markTxt);
				}
			},
			makeBackGroundColor : function(_target){
				var colors = [];
				var seq = _target.attr("id").replace(/[^\d]+/g, "");
				var list = common_data["list"+seq]["rows"];
				
				<c:forEach var="colors" items='${ct:getCodeNames(ct:getConstDef("CD_LICENSE_BACKGROUND"))}' varStatus="vs">
				colors.push("${colors}");
				</c:forEach>
				var lastColor = colors[0];
				
				for(var i=0; i<list.length; i++){
					var comb = list[i].ossLicenseComb;
					
					if(comb == 'AND') {
						$(_target).jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});
					} else if(comb == 'OR' || comb == '') {
						var flag = false;
						for(var j = 0; j<colors.length;j++){
							if(lastColor == colors[j]){
								lastColor = colors[j+1];
								$(_target).jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});

								flag = true;
							} else if(lastColor == colors[colors.length-1]) {
								lastColor = colors[0];
								$(_target).jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});

								flag = true;
							}
							
							if(flag) {
								break;
							}
						}
					}
				}
			}, 
			setCustomAutoComplete : function(div, _target, seq){
				var selected = false;
				
				if(div == 'single'){
					$( '.autoComOssLicense'+seq ).autocomplete({
						source: licenseTags,
						select: function( event, ui ) {
							var target = $(this).attr("id");

							if(target == "licenseName"+seq){
								$("#licenseName"+seq).parent().find("span.retxt:first").hide();
	
								var licenseName = ui.item.shortIdentifier.length > 0 ? ui.item.shortIdentifier : ui.item.licenseName;
								$('#licenseName'+seq).val(licenseName);
								
								grid_fn.showLicenseText(licenseName, _target, seq);
								
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
							var _item = grid_fn.checkLicenseSelected($(this).val());
							var targetId = $(this).attr("id");
							
							if(targetId == "licenseName"+seq){
								if(_item == null) {
									$("#licenseName"+seq).parent().find("span.retxt:first").text('<spring:message code="msg.oss.unknown.license" />').show();
								} else {
									$("#licenseName"+seq).parent().find("span.retxt:first").hide();
	
									var licenseName = _item.shortIdentifier.length > 0 ? _item.shortIdentifier : _item.licenseName;
									$('#licenseName'+seq).val(licenseName);
									$('#licenseType').val(_item.licenseType);
									$('input[name=obligationType]').val(_item.obligationCode);
									$('#lt'+seq+' td').html(_item.licenseType);
									$('#ob'+seq+' td').html('');
									
									$(_item.obligation).appendTo('#ob'+seq+' td');
									
									selected = true;
								}
							} else {
								// detected License
								$(this).parent().next("span.retxt:first").empty();
								detectedLicenseValid[seq] = true; // reset
								
								if(_item != null){
									var licenseName = _item.shortIdentifier.length > 0 ? _item.shortIdentifier : _item.licenseName;
									$(this).val(licenseName);
									
									selected = true;
								} else {
									var value = $(this).val();
									
									if(value != ""){
										$(this).parent().next("span.retxt:first").html('<spring:message code="msg.oss.unknown.license" />').show();
										detectedLicenseValid[seq] = false;
									}
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
		    	} else if(div == 'multi') {
		    		$('#'+lastsel[seq]+'_licenseNameEx').autocomplete({
						source: licenseTags,
						select: function( event, ui ) {
							var licenseName = ui.item.shortIdentifier.length > 0 ? ui.item.shortIdentifier : ui.item.licenseName;
							$('#'+lastsel[seq]+'_licenseNameEx').val(licenseName);
							$('#'+lastsel[seq]+' > td:eq(9)').text(licenseName); // licenseNameEx
							$('#'+lastsel[seq]+' > td:eq(7)').text(licenseName); // licenseName
							$('#'+lastsel[seq]+' > td:eq(10)').text(ui.item.licenseType); // licenseType
							$('#'+lastsel[seq]+' > td:eq(11)').text(ui.item.obligationChecks); // obligationChecks

							grid_fn.showLicenseText(licenseName, _target, seq);
							gLicenseData[seq][lastsel[seq]] = "Y";

							return false;
						},
				   		minLength: 0,
				   		open: function() {
							$(this).attr('state', 'open');
							selected = false;
						},
						change: function(){
							gLicenseData[seq][lastsel[seq]] = "N";
						},
						close: function () { 
							$(this).attr('state', 'closed');
							var _item = grid_fn.checkLicenseSelected($(this).val());
							var gridStr = "_licenseChoice";
							$("div.retxt._licenseChoice_"+lastsel[seq]).remove();

							if(_item == null) {
								$("#"+lastsel[seq]+"_licenseNameEx").after('<div class=\"'+gridStr+"_"+lastsel[seq]+' retxt\">'+ '<spring:message code="msg.oss.unknown.license" />' +'</div>');
								$("div.retxt._licenseChoice_"+lastsel[seq]).show();
							} else {
								var licenseName = _item.shortIdentifier.length > 0 ? _item.shortIdentifier : _item.licenseName; 
								$('#'+lastsel[seq]+'_licenseNameEx').val(licenseName);

								// 리스트 설정
								$(_target).jqGrid('saveRow',lastsel[seq]);
								var list = common_data["list"+seq]["rows"];
								var rowData = $(_target).jqGrid('getRowData', lastsel[seq]);
								rowData['licenseId'] = _item.licenseId;
								rowData['licenseName'] = _item.licenseName;
								rowData['licenseNameEx'] = licenseName;
								rowData['licenseType'] = _item.licenseType;
								rowData['obligation'] = _item.obligation;
								rowData['obligationChecks'] = _item.obligationChecks;
								rowData['ossLicenseIdx'] = lastsel[seq];
								rowData['no'] = lastsel[seq];
								
								list.forEach(function(row,index){
									if(row.ossLicenseIdx == lastsel[seq] ){
										list.splice(index,1,rowData);
									};
								});
								
								$(_target).jqGrid('editRow',lastsel[seq]);
								
								// 첫 로우 ossLicenseComb 설정
								grid_fn.changeLicenseType(_target, seq);
								grid_fn.setFristComb(_target);
								
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
			},
			autoLicense : function(data){
				var licenseCount = 0;
				
				data.forEach(function(data){
					if(data.licenseName){
						licenseCount += 1;
					}
				})
				
				var result = '';
				
				if(licenseCount != 0){
					var numbers = [];
					//1. 그룹별 분류하기
					var groups = grid_fn.distributeGroups(data);
					
					//2. 각 그룹별 내부 비교하기
					groups.forEach(function(group){
						var number = grid_fn.compareLicenseGroupMax(group);
						numbers.push(number);
					});
					
					//3. 각 그룹끼리 비교하기(OR 비교)
					if(numbers.length != 1) {
		 				var min = grid_fn.getMin(numbers);
		 				
		 				switch(min) {
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
						}
					} else {
		 				var min = numbers[0];
		 				
		 				switch(min) {
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
						}
					}
				}
				
				return result;
			},
			distributeGroups : function(data){
				var type='A';
				var groups =[];
				var groupA =[];
				var groupB =[];
				
				data.forEach(function(item,index,ref) {
					if(item.ossLicenseComb == 'AND' || item.ossLicenseComb =="") {
						if(groupA.length > 0){	//AND - AND 배열 결속
							
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

						if(groupB.length > 0) {  //OR - OR 배열 변경 (바로 전 등록된 배열을 그룹에 등록후 배열 초기화)
							groups.push(groupB);
							groupB = [];
						}

						groupB.push(item);
					}
					
					if(index == ref.length-1) {	//마지막 순서일 때 최종 등록
						if(groupA.length > 0) {
							groups.push(groupA);
						}

						if(groupB.length > 0) {
							groups.push(groupB);
						}
					}
				});
				
				return groups;
			},
			compareLicenseGroupMax : function(group){
				var max = 0;
				var constant = {
					'Permissive' : 1,
					'Weak Copyleft' : 2,
					'Copyleft'  : 3
				};
				
				group.forEach(function(item,index,ref){
					if(max < constant[item.licenseType]){
						max = constant[item.licenseType];
					}
				});
				
				return max;
			},
			autoObligation : function(data){
				var licenseCount = 0;
				
				data.forEach(function(data){
					if(data.licenseName){
						licenseCount += 1;
					}
				})
				
				var result = '';
				
				if(licenseCount != 0){
					var numbers = [];
					
					//1. 그룹별 분류하기
					var groups = grid_fn.distributeGroups(data);
					
					//2. 각 그룹별 내부 비교하기
					groups.forEach(function(group){
						var number = grid_fn.compareObligationGroupMax(group);
						numbers.push(number);
					});
					
					//3. 각 그룹끼리 비교하기(OR 비교)
					if(numbers.length != 1){
		 				var min = grid_fn.getMin(numbers);
		 				
		 				switch(min){
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
		 				
		 				switch(min){
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
			},
			distributeGroups : function(data){
				var type='A';
				var groups =[];
				var groupA =[];
				var groupB =[];
				
				data.forEach(function(item,index,ref) {
					if(item.ossLicenseComb == 'AND' || item.ossLicenseComb =="") {
						if(groupA.length > 0) {	//AND - AND 배열 결속
							
						}

						if(groupB.length > 0) {	//OR - AND 배열 결속
							groupA.push(groupB[0]);
							groupB = [];
						}
						
						groupA.push(item);	
					} else if(item.ossLicenseComb == 'OR') {
						if(groupA.length > 0) {	//AND - OR 배열 변경 (이전까지 등록된 배열을 그룹에 등록후 배열 초기화)
							groups.push(groupA);
							groupA = [];
						}
						
						if(groupB.length > 0) {  //OR - OR 배열 변경 (바로 전 등록된 배열을 그룹에 등록후 배열 초기화)
							groups.push(groupB);
							groupB = [];
						}
						
						groupB.push(item);
					}
					
					if(index == ref.length-1) {	//마지막 순서일 때 최종 등록
						if(groupA.length > 0) {
							groups.push(groupA);
						}
						
						if(groupB.length > 0) {
							groups.push(groupB);
						}
					}
				});
				
				return groups;
			},
			compareObligationGroupMax : function(group){
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
			},
			showLicenseText : function(_licenseName, _target, seq){
				if(!_licenseName) {
					var licenseChoiceLength = $(_target).jqGrid("getDataIDs").length;

					switch (licenseChoiceLength) {
						case 0:
							alertify.alert('<spring:message code="msg.oss.required.license" />', function(){});
							return false;
							break;
						case 1:
							$('#ossForm'+seq+' input[name=licenseDiv]').val("S");
							break;
						default:
							$('#ossForm'+seq+' input[name=licenseDiv]').val("M");
							break;
					}
					
					var _selectedRow = $(_target).jqGrid('getGridParam', "selrow" );
					
					if(_selectedRow) {
						var licenseNameEx = $(_target).jqGrid('getRowData',_selectedRow,'licenseNameOrg');
						licenseName = licenseNameEx['licenseNameEx'];
					} else {
						if($(_target).jqGrid("getDataIDs").length > 0) {
							_selectedRow = $(_target).jqGrid("getDataIDs")[0];
							licenseName = $(_target).jqGrid('getCell',_selectedRow,'licenseNameEx');
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
								$("#disp_licenseText"+seq).html(data.validMsg);
							} else {
								$("#disp_licenseText"+seq).html("");
							}
						},
						error : function(){
							$("#disp_licenseText"+seq).html("");
						}
					});
				} else {
					$("#disp_licenseText"+seq).html("");
				}
			},
			checkLicenseSelected : function(val){
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
			},
			getMin : function(numbers){
		 		var min = 3;
		 		
		 		numbers.forEach(function(number){
		 			if(min > number ){
		 				min = number;
		 			}
		 		});
		 		
		 		return min;
			},
			exeDelete : function(rowId, target){
				var _target = $(target);
				var seq = _target.attr("id").replace(/[^\d]+/g, "");
				// lastsel save
				_target.jqGrid('saveRow',lastsel[seq]);
				
				var list = common_data["list"+seq]["rows"];
				
				list.forEach(function(row,index){
					if(row.ossLicenseIdx == rowId ){
						list.splice(index,1);
					};
				});
				
				// 해당 리스트 삭제
				_target.jqGrid('delRowData', rowId);
				// 첫 로우 ossLicenseComb 설정
				grid_fn.setFristComb(_target);
				grid_fn.makeBackGroundColor(_target);
				
				//라이센스 타입 및 Obligation 자동 체크
				var type = grid_fn.autoLicense(list);
				var obligationHtml = grid_fn.autoObligation(list);
				
				if(list.length==0){
					type='';
					obligationHtml='';
				}
				
				$('#lt'+seq+' td').html(type);
				$('#ob'+seq+' td').html('');
				$(obligationHtml).appendTo('#ob'+seq+' td');
				
				//라이센스 묶음 텍스트 출력
				grid_fn.createMultiLicenseText(_target);
				
				lastsel[seq]=-1;
			},
			getOssGridRows : function(elementId){
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
		};
		</script>
	</head>
	<body>
		<div id="loading_wrap_popup" class="loading" style="display:none;">
			<div class="loadingBlind"></div>
			<img src="${ctxPath}/images/loading.gif" alt="loading" />
		</div>
		<div id="wrap" style="padding: 15px 0px;">
			<div class="groupSet">
				<!--  -->
				<div class="detailView1">
					<div class="tbws1 w100P">
						<form name="ossForm1" id="ossForm1">
							<input type="hidden" name="ossLicensesJson"/>
							<input type="hidden" name="ossId"/>
							<input type="hidden" name="licenseType" id="licenseType" />
							<input type="hidden" name="obligationType" id="obligationType" />
							<input type="hidden" name="ossType"/>
							<input type="hidden" name="validationType"/>
							<input type="hidden" name="downloadLocation"/>
							<input type="hidden" name="licenseDiv"/>
							<table class="dCase">
								<colgroup>
									<col width="100px"/>
									<col/>
								</colgroup>
								<tbody>
									<tr>
										<th class="dCase">Title</th>
										<td class="dCase">
											<span class="selectSet" style="width:100% !important">
												<strong for="selectTitle1" title="selected value"></strong>
												<select id="selectTitle1" onchange="Ctrl_fn.selectTitle(1)"></select>
											</span>
										</td>
									</tr>
									<tr>
										<th class="dCase txStr"><spring:message code="msg.common.field.OSS.name" /></th>
										<td class="dCase">
											<div class="required">
												<input name="ossName" type="text" class="autoComOss w100P" id="detailOssName1" />
												<span class="retxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.OSS.version" /></th>
										<td class="dCase">
											<div class="required">	
												<input name="ossVersion" type="text" class="w100P" id="detailOssVersion1" />
												<span class="retxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.nickname" /></th>
										<td class="dCase">
											<div class="multiTxtSet detailNickName1">
												<div class="required">
													<span><input type="text" name="ossNicknames"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="retxt"></span>
												</div>
											</div>
											<input id="nickAdd1" type="button" value="+ Add" class="btnCLight gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase txStr"><spring:message code="msg.common.field.declaredLicense" /></th>
										<td class="dCase">
											<div class="required detailLicense1">
												<div class="licenseMulti">
													<div class="mark1"></div>
													<div class="mt5"><table id="_licenseChoice1"><tr><td></td></tr></table></div>
													<div id="_licenseChoicePager1"></div>
												</div>
											</div>
											<div id="disp_licenseText1" style="display: none;"></div>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.detectedLicense" /></th>
										<td class="dCase">
											<div class="multiItemSet multiDetectedLicenseSet detailDetectedLicense1">
												<div class="required">
													<span><input type="text" name="detectedLicenses" class="autoComOssLicense1 w250"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="retxt"></span>
												</div>
											</div>
											<input id="detectedLicenseAdd1" type="button" value="+ Add" class="btnCLight gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.Copyright" /></th>
										<td class="dCase">
											<textarea name="copyright" class="w100P h150" id="detailCopyright1"></textarea>
										</td>
									</tr>
									<tr id="lt1">
										<th class="dCase"><spring:message code="msg.common.field.licenseType" /></th>
										<td class="dCase"></td>
									</tr>
									<tr id="ob1">
										<th class="dCase"><spring:message code="msg.common.field.obligation" /></th>
										<td class="dCase"></td>
									</tr>
									<tr>
										<th class="dCase">Download<br>Location</th>
										<td class="dCase">
											<div class="multiItemSet multiDownloadLocationSet detailDownloadLocation1">
												<div class="required">
													<span><input type="text" name="downloadLocations" id="downloadLocations1" class="w350"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="urltxt"></span>
												</div>
											</div>
											<input id="downloadLocationAdd1" type="button" value="+" class="btnCLightAnalysis gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.homepage" /></th>
										<td class="dCase">
											<div class="required">
												<input name="homepage" type="text" class="w100P" placeholder="http://"  id="detailHomePage1"/>
												<span class="urltxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<th class="dCase">Summary<br>Description</th>
										<td class="dCase"><textarea name="summaryDescription" class="w100P h150"  id="detailSummaryDescription1"></textarea></td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.comment" /></th>
										<td class="dCase">
											<textarea name="comment" class="w100P h150"  id="detailcomment1"></textarea>
										</td>
									</tr>
									<tr>
										<td class="dCase" colspan="2">
											<input name="save" id="save1" type="button" value="Save" class="btnColor red right" />
										</td>
									</tr>
								</tbody>
							</table>
						</form>
					</div>
				</div>
				<!--  -->
				<!--  -->
				<div class="detailView1">
					<div class="tbws1 w100P">
						<form name="ossForm2" id="ossForm2">
							<input type="hidden" name="ossLicensesJson"/>
							<input type="hidden" name="ossId"/>
							<input type="hidden" name="licenseType" id="licenseType" />
							<input type="hidden" name="obligationType" id="obligationType" />
							<input type="hidden" name="ossType"/>
							<input type="hidden" name="validationType"/>
							<input type="hidden" name="downloadLocation"/>
							<input type="hidden" name="licenseDiv"/>
							<table class="dCase">
								<colgroup>
									<col width="100px"/>
									<col/>
								</colgroup>
								<tbody>
									<tr>
										<th class="dCase">Title</th>
										<td class="dCase">
											<span class="selectSet" style="width:100% !important">
												<strong for="selectTitle2" title="selected value"></strong>
												<select id="selectTitle2" onchange="Ctrl_fn.selectTitle(2)"></select>
											</span>
										</td>
									</tr>
									<tr>
										<th class="dCase txStr"><spring:message code="msg.common.field.OSS.name" /></th>
										<td class="dCase">
											<div class="required">
												<input name="ossName" type="text" class="autoComOss w100P" id="detailOssName2" />
												<span class="retxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.OSS.version" /></th>
										<td class="dCase">
											<div class="required">	
												<input name="ossVersion" type="text" class="w100P" id="detailOssVersion2" />
												<span class="retxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<tr>
										<th class="dCase"><spring:message code="msg.common.field.nickname" /></th>
										<td class="dCase">
											<div class="multiTxtSet detailNickName2">
												<div class="required">
													<span><input type="text" name="ossNicknames"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="retxt"></span>
												</div>
											</div>
											<input id="nickAdd2" type="button" value="+ Add" class="btnCLight gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase txStr"><spring:message code="msg.common.field.declaredLicense" /></th>
										<td class="dCase">
											<div class="required detailLicense2">
												<div class="licenseMulti">
													<div class="mark2"></div>
													<div class="mt5"><table id="_licenseChoice2"><tr><td></td></tr></table></div>
													<div id="_licenseChoicePager2"></div>
												</div>
											</div>
											<div id="disp_licenseText2" style="display: none;"></div>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.detectedLicense" /></th>
										<td class="dCase">
											<div class="multiItemSet multiDetectedLicenseSet detailDetectedLicense2">
												<div class="required">
													<span><input type="text" name="detectedLicenses" class="autoComOssLicense2 w250"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="retxt"></span>
												</div>
											</div>
											<input id="detectedLicenseAdd2" type="button" value="+ Add" class="btnCLight gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.Copyright" /></th>
										<td class="dCase">
											<textarea name="copyright" class="w100P h150" id="detailCopyright2"></textarea>
										</td>
									</tr>
									<tr id="lt2">
										<th class="dCase"><spring:message code="msg.common.field.licenseType" /></th>
										<td class="dCase"></td>
									</tr>
									<tr id="ob2">
										<th class="dCase"><spring:message code="msg.common.field.obligation" /></th>
										<td class="dCase"></td>
									</tr>
									<tr>
										<th class="dCase">Download<br>Location</th>
										<td class="dCase">
											<div class="multiItemSet multiDownloadLocationSet detailDownloadLocation2">
												<div class="required">
													<span><input type="text" name="downloadLocations" id="downloadLocations2" class="w350"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="urltxt"></span>
												</div>
											</div>
											<input id="downloadLocationAdd2" type="button" value="+" class="btnCLightAnalysis gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.homepage" /></th>
										<td class="dCase">
											<div class="required">
												<input name="homepage" type="text" class="w100P" placeholder="http://"  id="detailHomePage2"/>
												<span class="urltxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<th class="dCase">Summary<br>Description</th>
										<td class="dCase"><textarea name="summaryDescription" class="w100P h150"  id="detailSummaryDescription2"></textarea></td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.comment" /></th>
										<td class="dCase">
											<textarea name="comment" class="w100P h150"  id="detailcomment2"></textarea>
										</td>
									</tr>
									<tr>
										<td class="dCase" colspan="2">
											<input name="save" id="save2" type="button" value="Save" class="btnColor red right" />
										</td>
									</tr>
								</tbody>
							</table>
						</form>
					</div>
				</div>
				<!--  -->
				<!--  -->
				<div class="detailView1">
					<div class="tbws1 w100P">
						<form name="ossForm3" id="ossForm3">
							<input type="hidden" name="ossLicensesJson"/>
							<input type="hidden" name="ossId"/>
							<input type="hidden" name="licenseType" id="licenseType" />
							<input type="hidden" name="obligationType" id="obligationType" />
							<input type="hidden" name="ossType"/>
							<input type="hidden" name="validationType"/>
							<input type="hidden" name="downloadLocation"/>
							<input type="hidden" name="licenseDiv"/>
							<table class="dCase">
								<colgroup>
									<col width="100px"/>
									<col/>
								</colgroup>
								<tbody>
									<tr>
										<th class="dCase">Title</th>
										<td class="dCase">
											<span class="selectSet" style="width:100% !important">
												<strong for="selectTitle3" title="selected value"></strong>
												<select id="selectTitle3" onchange="Ctrl_fn.selectTitle(3)"></select>
											</span>
										</td>
									</tr>
									<tr>
										<th class="dCase txStr"><spring:message code="msg.common.field.OSS.name" /></th>
										<td class="dCase">
											<div class="required">
												<input name="ossName" type="text" class="autoComOss w100P" id="detailOssName3" />
												<span class="retxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.OSS.version" /></th>
										<td class="dCase">
											<div class="required">	
												<input name="ossVersion" type="text" class="w100P" id="detailOssVersion3" />
												<span class="retxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<tr>
										<th class="dCase"><spring:message code="msg.common.field.nickname" /></th>
										<td class="dCase">
											<div class="multiTxtSet detailNickName3">
												<div class="required">
													<span><input type="text" name="ossNicknames"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="retxt"></span>
												</div>
											</div>
											<input id="nickAdd3" type="button" value="+ Add" class="btnCLight gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase txStr"><spring:message code="msg.common.field.declaredLicense" /></th>
										<td class="dCase">
											<div class="required detailLicense3">
												<div class="licenseMulti">
													<div class="mark3"></div>
													<div class="mt5"><table id="_licenseChoice3"><tr><td></td></tr></table></div>
													<div id="_licenseChoicePager3"></div>
												</div>
											</div>
											<div id="disp_licenseText3" style="display: none;"></div>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.detectedLicense" /></th>
										<td class="dCase">
											<div class="multiItemSet multiDetectedLicenseSet detailDetectedLicense3">
												<div class="required">
													<span><input type="text" name="detectedLicenses" class="autoComOssLicense3 w250"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="retxt"></span>
												</div>
											</div>
											<input id="detectedLicenseAdd3" type="button" value="+ Add" class="btnCLight gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.Copyright" /></th>
										<td class="dCase">
											<textarea name="copyright" class="w100P h150" id="detailCopyright3"></textarea>
										</td>
									</tr>
									<tr id="lt3">
										<th class="dCase"><spring:message code="msg.common.field.licenseType" /></th>
										<td class="dCase"></td>
									</tr>
									<tr id="ob3">
										<th class="dCase"><spring:message code="msg.common.field.obligation" /></th>
										<td class="dCase"></td>
									</tr>
									<tr>
										<th class="dCase">Download<br>Location</th>
										<td class="dCase">
											<div class="multiItemSet multiDownloadLocationSet detailDownloadLocation3">
												<div class="required">
													<span><input type="text" name="downloadLocations" id="downloadLocations3" class="w350"/><input type="button" value="Delete" class="smallDelete"/></span>
													<span class="urltxt"></span>
												</div>
											</div>
											<input id="downloadLocationAdd3" type="button" value="+" class="btnCLightAnalysis gray"/>
										</td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.homepage" /></th>
										<td class="dCase">
											<div class="required">
												<input name="homepage" type="text" class="w100P" placeholder="http://"  id="detailHomePage3"/>
												<span class="urltxt"></span>
											</div>
										</td>
									</tr>
									<tr>
										<th class="dCase">Summary<br>Description</th>
										<td class="dCase"><textarea name="summaryDescription" class="w100P h150"  id="detailSummaryDescription3"></textarea></td>
									</tr>
									<tr>
										<th class="dCase"><spring:message code="msg.common.field.comment" /></th>
										<td class="dCase">
											<textarea name="comment" class="w100P h150"  id="detailcomment3"></textarea>
										</td>
									</tr>
									<tr>
										<td class="dCase" colspan="2">
											<input name="save" id="save3" type="button" value="Save" class="btnColor red right" />
										</td>
									</tr>
								</tbody>
							</table>
						</form>
					</div>
				</div>
				<!--  -->
			</div>
		</div>
	</body>
</html>