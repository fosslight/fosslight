<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
var colors = [];
<c:forEach var="colors" items='${ct:getCodeNames(ct:getConstDef("CD_LICENSE_BACKGROUND"))}' varStatus="vs">
colors.push("${colors}");
</c:forEach>

var _mainLastsel= -1;
var _subLastsel= -1;
var _popup = null;
var _popupLicense = null;
var _popupOss = null;
var _popupBulkOssRef = null;
var _popupVuln = null;

var fn_grid_com = {
		// oss_name 가져오기
		griOssNames : function(data){
			onAjaxLoadingHide = true;
			return $.ajax({
				type: 'GET',
				url: '<c:url value="/project/getOssNames"/>',
				data: data,
				headers: {
					'Content-Type': 'application/json'
				},
			});
		},
		// oss_version 가져오기
		griOssVersions : function(e, ossName, target){
			var ossVersions = [];
			if(ossName=="") return false;
			onAjaxLoadingHide = true;
			
			return $.ajax({
				type: 'GET',
				url: '<c:url value="/project/getOssVersions"/>',
				async: false,
				data: {ossName : ossName },
				headers: {
					'Content-Type': 'application/json'
				},
				success : 
					function(data, status, headers, config){
		 				if(data != null){
	 						data.forEach(function(obj){
	 							ossVersions.push(obj.ossVersion);
	 						});
							$(e).autocomplete({
								source: ossVersions
								, select: function( event, ui ) {
									var rowid = (e.id).split('_')[0];
									fn_grid_com.griOssIds(target, rowid, $('#'+rowid+'_ossName').val(), $('#'+rowid+'_ossVersion').val());
								}
								, minLength: 0
								, open: function() { $(this).attr('state', 'open');}
								, close: function () { $(this).attr('state', 'closed');}
							}).focus(function() {
								if ($(this).attr('state') != 'open') {
									$(this).autocomplete("search");
								}
							}).on('autocompletechange', function() {
								var rowid = (e.id).split('_')[0];
								fn_grid_com.griOssIds(target, rowid, $('#'+rowid+'_ossName').val(), $('#'+rowid+'_ossVersion').val());
							});
		 					ossVersions = [];
		 					$(e).focus();
						}
					}
			});
		},
		// oss 정보 가져오기
		griOssIds : function(div, rowid, ossName, ossVersion){
			var target = $('#'+div);
			if(ossVersion=="N/A") ossVersion="";
			onAjaxLoadingHide = true;
			return $.ajax({
				type: 'GET',
				url: '<c:url value="/project/getOssIdLicenses"/>',
				async: false,
				data: {
						ossName : ossName,
						ossVersion : ossVersion 
					  },
				headers: {
					'Content-Type': 'application/json'
				},
				success : 
					function(data, status, headers, config){
						// OSS 설정
						if(data.prjOssMaster != undefined){
							// 라이센스 설정
							if(data.prjLicense.length!=0){
								
								alertify.confirm('<spring:message code="msg.project.confirm.load.licenseinfo" />', function (e) {
									if(e){
										target.jqGrid('setCell', rowid, 'ossId', data.prjOssMaster.ossId);
										target.jqGrid('setCell', rowid, 'licenseDiv', data.prjOssMaster.licenseDiv);
										target.jqGrid('setCell', rowid, 'homepage', data.prjOssMaster.homepage);
										target.jqGrid('setCell', rowid, 'downloadLocation', data.prjOssMaster.downloadLocation);
										target.jqGrid('setCell', rowid, 'cveId', data.prjOssMaster.cveId);
										target.jqGrid('setCell', rowid, 'cvssScore', data.prjOssMaster.cvssScore);
										target.jqGrid('setCell', rowid, 'copyrightText', data.prjOssMaster.copyrightText);
										// 메인 그리드 로우 저장
										target.jqGrid('setCell', rowid, 'licenseName', data.prjOssMaster.licenseName);
										
										fn_grid_com.saveCellData(div,rowid,'ossId', data.prjOssMaster.ossId,null,null);
										fn_grid_com.saveCellData(div,rowid,'licenseDiv', data.prjOssMaster.licenseDiv,null,null);
										fn_grid_com.saveCellData(div,rowid,'homepage', data.prjOssMaster.homepage,null,null);
										fn_grid_com.saveCellData(div,rowid,'downloadLocation', data.prjOssMaster.downloadLocation,null,null);
										fn_grid_com.saveCellData(div,rowid,'cveId', data.prjOssMaster.cveId,null,null);
										fn_grid_com.saveCellData(div,rowid,'cvssScore', data.prjOssMaster.cvssScore,null,null);
										fn_grid_com.saveCellData(div,rowid,'copyrightText', data.prjOssMaster.copyrightText,null,null);
										fn_grid_com.saveCellData(div,rowid,'licenseName', data.prjOssMaster.licenseName,null,null);

										// 서브 라이센스 그리드 클리어
										if($("#"+div+"_"+rowid+"_t")) {
											$("#"+div+"_"+rowid+"_t").jqGrid('clearGridData');
										}
										
										// 멀티 라이센스 이면 서브 그리드에 저장
										if(data.prjOssMaster.licenseDiv == "M"){
											// 메인 그리드 저장모드 후 데이터 설정
											target.jqGrid('saveRow',rowid);
											// 초기 데이터 설정
											var _tempRandId = "";
											var mainGridId = target.jqGrid('getCell',rowid,'gridId');
											
											$("#"+div+" #"+rowid).find("td:first").addClass("sgcollapsed").find("a").show();
											target.jqGrid('expandSubGridRow',rowid);
										// 싱글 라이센스 이면 메인 그리드에 저장
										}else{
											// 만약 기존 grid id로 multi license가 설정되어 있었다면 삭제
											if($("#"+div+"_"+rowid+"_t").length > 0) {
												$("#"+div+"_"+rowid+"_t").jqGrid('clearGridData');
												$("#"+div+"_"+rowid+"_t").remove();
											}
											// 메인 그리드 조회 데이터 넣기
											target.jqGrid('setCell', rowid, 'licenseId', data.prjLicense[0].licenseId);
											target.jqGrid('setCell', rowid, 'licenseName', data.prjLicense[0].licenseName);
											target.jqGrid('setCell', rowid, 'excludeYn', "N"); // 신규 디폴트 값

											fn_grid_com.saveCellData(div,rowid,'licenseId', data.prjLicense[0].licenseId,null,null);
											fn_grid_com.saveCellData(div,rowid,'licenseName', data.prjLicense[0].licenseName,null,null);
											fn_grid_com.saveCellData(div,rowid,'excludeYn', "N",null,null);
											
											$("#"+div+" #"+rowid + " td:first").removeClass("sgcollapsed");
											$("#"+div+" #"+rowid + " td:first a").hide();
											$("#"+div+" #"+rowid).next("tr.ui-subgrid").hide();
										}
										
										if("binAndroidList" == div) { // Oss 자동완성 후 warning message 삭제 처리
								    		binAndroidValidMsgData = fn_grid_com.deleteWarningMsg(binAndroidValidMsgData, rowid);
								    		binAndroidDiffMsgData = fn_grid_com.deleteWarningMsg(binAndroidDiffMsgData, rowid);
								    		binAndroidInfoMsgData = fn_grid_com.deleteWarningMsg(binAndroidInfoMsgData, rowid);
							    		} else if("srcList" == div) {
							    			srcValidMsgData = fn_grid_com.deleteWarningMsg(srcValidMsgData, rowid);
							    			srcDiffMsgData = fn_grid_com.deleteWarningMsg(srcDiffMsgData, rowid);
							    		} else if("binList" == div) {
							    			binValidMsgData = fn_grid_com.deleteWarningMsg(binValidMsgData, rowid);
							    			binDiffMsgData = fn_grid_com.deleteWarningMsg(binDiffMsgData, rowid);
							    			binInfoMsgData = fn_grid_com.deleteWarningMsg(binInfoMsgData, rowid);
							    		} else if("list" == div) {
							    			partyValidMsgData_e = fn_grid_com.deleteWarningMsg(partyValidMsgData_e, rowid);
							    			partyDiffMsgData_e = fn_grid_com.deleteWarningMsg(partyDiffMsgData_e, rowid);
							    		} else if("batList" == div) {
							    			if(identBat){
							    				batValidMsgData = fn_grid_com.deleteWarningMsg(batValidMsgData, rowid);
							    			}else{
							    				batValidMsgData_e = fn_grid_com.deleteWarningMsg(batValidMsgData_e, rowid);
							    			}
							    		}
										
										target.jqGrid('saveRow',rowid);
										target.jqGrid("setSelection", rowid);

										$("#"+rowid).focus(); // 리로드 후 스크롤 이동 방지
										_mainLastsel = -1;
										
										// 메인 그리드 라이센스 데이터 설정
										fn_grid_com.setMainLicenseData(div, rowid);
									}
								});
							}
						}else{
							target.jqGrid('setCell', rowid, 'ossId', '');
						}
					}
				});
		},
		// 공통 언포메터
		unformatter : function(cellvalue, options, rowObject){
			return cellvalue;
		},
		// url 포메터
		displayUrl : function(cellvalue, options, rowObject){
			var url ="";

			if(cellvalue != null){
				var httpVal = cellvalue;
				if( !(
						cellvalue.toLowerCase().startsWith("http://") 
						|| cellvalue.toLowerCase().startsWith("https://") 
						|| cellvalue.toLowerCase().startsWith("ftp://") 
						|| cellvalue.toLowerCase().startsWith("git://") 
					) ) {
					httpVal = "http://" + cellvalue;
				}
				url = "<a href=\""+httpVal+"\" class=\"urlLink\" target=\"_blank\">"+cellvalue+"</a>";
			}
			
			return url;
		},
		unDisplayUrl : function(cellvalue, options, rowObject){
			return cellvalue;
		},
		// license 포메터
		displayLicense : function(cellvalue, options, rowObject){
			var newVal = '';
			
			if(cellvalue){
				var licenseArr = cellvalue.split(',');
				for(var i = 0; i < licenseArr.length; i++){
					if(licenseArr[i] != ''){
						if(newVal == ''){
							newVal += licenseArr[i];
						}else{
							newVal += ','+licenseArr[i];
						}
					}
				}
			}
			
			return newVal;
		},
		// vulnerability 포메터
		displayVulnerability : function(cellvalue, options, rowObject){
			var display = "";
			var _url = '<c:url value="/vulnerability/vulnpopup?ossName='+rowObject.ossName+'&ossVersion='+rowObject.ossVersion+'&vulnType="/>';
			
			if(parseInt(cellvalue) >= 9.0 ) {
				display="<span class=\"iconSet vulCritical\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 7.0 ) {
				display="<span class=\"iconSet vulHigh\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) >= 4.0) {
				display="<span class=\"iconSet vulMiddle\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) > 0) {
				display="<span class=\"iconSet vulLow\" onclick=\"openNVD2('"+rowObject.ossName+"','"+_url+"')\">"+cellvalue+"</span>";
			} else if(parseInt(cellvalue) == 0 || cellvalue == undefined) {
				display="<span style=\"font-size:0;\"></span>";
			} else {
				display=cellvalue;
			}
			
			return display;
		},
		displayBatGuiReport : function(cellvalue, options, rowObject){
			var display = "";
			
			if(cellvalue == "Y" && rowObject.ossName && "-" != rowObject.ossName && rowObject.batStringMatchPercentage != "Binary DB matched") {
				var url = '<c:url value="/download/batGuiReport/' + rowObject.referenceId + '/' + rowObject.batChecksum + '"/>';
				display="<a class='iconReport' href='"+url+"'>"+rowObject.batChecksum+"</a>";
			}
			
			return display;
		},
		// License Restriction 포메터
		displayLicenseRestriction : function(cellvalue, options, rowObject){
			var display = "";

			if(cellvalue != "" && cellvalue != undefined) {
				display = "<div class=\"tcenter\"><a class=\"iconSt review\" title=\""+cellvalue+"\" " +
					"onclick=\"src_fn_com.showLicenseRestrictionViewPage('"+ options.gid +"','"+options.rowId+"')\">"+cellvalue+"</a></div>";
			}

			return display;
		},
		// comment 포메터
		displayComment : function(cellvalue, options, rowObject){
			var display = "";

			if(cellvalue !="" && cellvalue != undefined) {
				display ="<div class=\"commentDiv\" style=\"height : 29px; overflow: hidden;\">"+cellvalue+"</div>";
			}
			
			return display;
		},
		// 메인 그리드 체크박스 포메터
		cboxFormatter : function(cellvalue, options, rowObject){
			  return '<input id=\"'+options.rowId+'_excludeYn\" type="checkbox"' + (cellvalue=='Y' ? ' value ="Y" checked="checked"' : ' value ="N"') + 
		      'onclick="fn_grid_com.onCboxClick(\'' + options.rowId + '\',\'main\')"/>';
		},
		cboxUnFormatter : function(cellvalue, options, rowObject){
			var cboxValue = $("#"+options.rowId+"_excludeYn").val();
			
			return cboxValue;
		},
		// 서브 그리드 체크박스 포메터
		cboxSubFormatter : function(cellvalue, options, rowObject){
			  return '<input id=\"'+options.rowId+'_excludeYn\" type="checkbox"' + (cellvalue=='Y' ? ' value ="Y" checked="checked"' : ' value ="N"') + 
		      'onclick="fn_grid_com.onCboxClick(\'' + options.rowId + '\',\'sub\')"/>';
		},
		cboxSubUnFormatter : function(cellvalue, options, rowObject){
			var cboxValue = $("#"+options.rowId+"_excludeYn").val();
			
			return cboxValue;
		},
		// 체크박스 클릭시 excludeYn 값 및 음영 처리
		onCboxClick : function(rowId, sub){
			var id = "";
			(typeof(rowId) == 'object') ? id = rowId.id : id = rowId;
			var target = '';
			
			if(sub == 'sub') {
				target = $("#"+id+"_excludeYn").closest('table').attr('id');
			} else {
				target = $("#"+id+"_excludeYn").parent().attr('aria-describedby').split('_')[0];
			}

			var _tr = $('#'+target).jqGrid("getInd", rowId, true);
			var value = "N";

			if($("#"+id+"_excludeYn").is(":checked")) {
				$("#"+id+"_excludeYn").attr('value','Y');
				$(_tr).addClass("excludeRow");
				value = "Y";
			} else {
				$("#"+id+"_excludeYn").attr('value','N');
				$(_tr).removeClass("excludeRow");
			}
			
			fn_grid_com.saveCellData(target,rowId,"excludeYn",value);
		},
		
	    onCboxClickAll : function(allChk, target){
	    	if(event.stopPropagation) {
		    	event.stopPropagation(); //MOZILLA
	    	} else {
		    	event.cancelBubble = true; //IE
	    	}
	    	
	    	var dataArray = $("#"+target).jqGrid('getGridParam', 'data');
	    	
            if($(allChk).is(":checked")) {
                $("#"+target+" input[id*='_excludeYn']").each(function (idx){
                    $(this).attr('value','Y');
                    $(this).prop('checked',true);
                    $(this).parent().parent().addClass("excludeRow");
                    fn_grid_com.saveCellData(target,dataArray[idx].gridId,"excludeYn","Y");        
                });
            } else {
            	$("#"+target+" input[id*='_excludeYn']").each(function (idx){
                    $(this).attr('value','N');
                    $(this).prop('checked',false);
                    $(this).parent().parent().removeClass("excludeRow");
                    fn_grid_com.saveCellData(target,dataArray[idx].gridId,"excludeYn","N");        
                });
            }
        },
		// 전체 excludeYn 에 대한  음영 처리
		checkExclude : function(data, target){
			var arr = [];
			arr = $('#'+target).jqGrid('getDataIDs');
			
			for(var i in arr){
				if($('#'+target).jqGrid('getCell',arr[i], 'excludeYn') == 'Y'){
					$('#'+target).jqGrid("setRowData",arr[i], false, {'background-color' : '#ada9a9'});
				}
			}
		},		
		// 그리드 셀 경고 클래스 설정
		setWarningClass : function(target, rowid, colnames){
			for(var c in colnames){
				var idx=-1;
				var cm = target.jqGrid('getGridParam','colModel');
				
				for(var i in cm){
					if (cm[i].name==colnames[c]) {
						idx=i;
						
						break;
					}
				}
				
				var td = target[0].rows.namedItem(rowid).cells[idx];
				
				if(target.jqGrid('getCell', rowid, idx)==""){
					$(td).addClass("warningStyle");
				}else{
					$(td).removeClass("warningStyle");
				}
			}
		},
		// 서브 그리드 라이센스 배경색 설정
		makeBackGroundColor : function(target){
			var lastColor = colors[0];
			// 배경색 설정
	 		var arr = [];
	 		arr = target.jqGrid('getDataIDs');
	 		var comb = "";
	 		
			for(var i in arr){
				comb = target.jqGrid('getCell',arr[i],'ossLicenseComb');
				
				if(comb == 'AND' || comb == ''){
					target.jqGrid("setRowData",arr[i], false, {'background-color' : lastColor});
				}else if(comb == 'OR'){
					var flag = false;
					
					for(var j = 0; j<colors.length;j++){
						if(lastColor == colors[j]) {
							lastColor = colors[j+1];
							target.jqGrid("setRowData",arr[i], false, {'background-color' : lastColor});

							flag = true;
						} else if(lastColor == colors[colors.length-1]) {
							lastColor = colors[0];
							target.jqGrid("setRowData",arr[i], false, {'background-color' : lastColor});

							flag = true;
						}
						
						if(flag) {
							break;
						}
					}
				}
			}
		},
		// 행추가 메인 & 서브
	 	rowAdd : function(div, target, flag, rowid, callbackFunc){
			if(flag == "main"){
				// 서브 그리드 url 전송 설정(false 전송 안함)
				subGridUrl = false;
				
				// 이전 로우 세이브
				if(_mainLastsel != -1) {
					var licenseName = callbackFunc(target.getRowData(_mainLastsel));
					target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
					fn_grid_com.saveCellData(target.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
					target.jqGrid('saveRow',_mainLastsel);
				} else {
					target.jqGrid('saveRow',_mainLastsel);
				}
				
				// 로우 추가
				var _tempRandId = $.jgrid.randId();
				target.jqGrid("addRowData", _tempRandId, {gridId: _tempRandId, licenseDiv:"S", excludeYn: "N", customBinaryYn: "Y"}, "last");
				// 경고 클래스 설정
				fn_grid_com.setWarningClass(target,_tempRandId,["ossName","licenseName"]);
				// 추가된 로우 에디트 설정
				fn_grid_com.setMainEditMode(target, _tempRandId);
				

				// 만약 client array에도 추가한다.
				var dataArray = $("#"+div).jqGrid('getRowData', _tempRandId);
				if("binAndroidList" == div) {
					binAndroidMainData[binAndroidMainData.length-1] = dataArray;
				} else if("srcList" == div) {
					srcMainData[srcMainData.length-1] = dataArray;
				} else if("binList" == div) {
					binMainData[binMainData.length-1] = dataArray;
				} else if("list" == div) {
					partyMainData[partyMainData.length-1] = dataArray;
				} else if("batList" == div) {
					batMainData[batMainData.length-1] = dataArray;
				}
				
				$.each(dataArray, function(_key, _val){
					fn_grid_com.saveCellData(div,_tempRandId, _key, _val, null, null);
				});
				
				target.jqGrid('editRow',_tempRandId);
				target.jqGrid("setSelection", _tempRandId);
				
				// 서브 그리드 확장 및 숨기기
				$("#"+div+" #"+_tempRandId).find("td:first").removeClass("sgcollapsed").find("a").hide();
				$("#"+div+" #"+_tempRandId).next().hide();
				
				// 라스트 셀 설정
				_mainLastsel=_tempRandId;
				
				// 서브 그리드 url 전송 설정(true 전송)
				subGridUrl = true;
				
				$('#'+_tempRandId+'_licenseName').addClass('autoCom');
				$('#'+_tempRandId+'_licenseName').css({'width' : '60px'});
 	 			
			} else if(flag == "sub") {
				var mainGridId = $("#"+div).jqGrid('getCell',rowid,'gridId');
				var _tempRandId = mainGridId+"-"+$.jgrid.randId();
				target.jqGrid("addRowData", _tempRandId, {gridId: _tempRandId, componentId: mainGridId, excludeYn: "N", editable: "Y"}, "last");
	 			target.jqGrid('setColProp','licenseId', {editable: true});
	 			target.jqGrid('setColProp','licenseName', {editable: true});
	 			target.jqGrid('setColProp','licenseText', {editable: true});
	 			target.jqGrid('setColProp','copyrightText', {editable: true});
	 			
				// client array에도 추가한다.
				var dataArray = target.jqGrid('getRowData', _tempRandId);
				
				if("binAndroidList" == div) {
					binAndroidSubData[binAndroidSubData.length-1] = dataArray;
				} else if("srcList" == div) {
					srcSubData[srcSubData.length-1] = dataArray;
				} else if("binList" == div) {
					binSubData[binSubData.length-1] = dataArray;
				} else if("list" == div) {
					partySubData[partySubData.length-1] = dataArray;
				} else if("batList" == div) {
					batSubData[batSubData.length-1] = dataArray;
				}
	   
				$.each(dataArray, function(_key, _val){
					fn_grid_com.saveCellData(div,_tempRandId, _key, _val, null, null);
				});
	 			
				target.jqGrid('editRow',_tempRandId);
				target.jqGrid("setSelection", _tempRandId);
			}
		},
		// 행추가 메인 & 서브 (new)
	 	rowAddNew : function(div, target, flag, rowid, callbackFunc){
			if(flag == "main"){
				// 서브 그리드 url 전송 설정(false 전송 안함)
				subGridUrl = false;
				
				// 이전 로우 세이브
				if(_mainLastsel != -1) {
					var licenseName = callbackFunc(target.getRowData(_mainLastsel));
					target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
					fn_grid_com.saveCellData(target.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
					target.jqGrid('saveRow',_mainLastsel);
				} else {
					target.jqGrid('saveRow',_mainLastsel);
				}
				
				// 로우 추가
				var _tempRandId = $.jgrid.randId();
				target.jqGrid("addRowData", _tempRandId, {gridId: _tempRandId, licenseDiv:"S", excludeYn: "N", customBinaryYn: "Y"}, "last");
				// 경고 클래스 설정
				fn_grid_com.setWarningClass(target,_tempRandId,["ossName","licenseName"]);
				// 추가된 로우 에디트 설정
				fn_grid_com.setMainEditMode(target, _tempRandId);
				

				// 만약 client array에도 추가한다.
				var dataArray = $("#"+div).jqGrid('getRowData', _tempRandId);
				if("binAndroidList" == div) {
					binAndroidMainData[binAndroidMainData.length-1] = dataArray;
				} else if("srcList" == div) {
					srcMainData[srcMainData.length-1] = dataArray;
				} else if("binList" == div) {
					binMainData[binMainData.length-1] = dataArray;
				} else if("list" == div) {
					partyMainData[partyMainData.length-1] = dataArray;
				} else if("batList" == div) {
					batMainData[batMainData.length-1] = dataArray;
				}
				
				$.each(dataArray, function(_key, _val){
					fn_grid_com.saveCellData(div,_tempRandId, _key, _val, null, null);
				});
				
				target.jqGrid('editRow',_tempRandId);
				target.jqGrid("setSelection", _tempRandId);

				// 서브 그리드 확장 및 숨기기
				$("#"+div+" #"+_tempRandId).find("td:first").removeClass("sgcollapsed").find("a").hide();
				$("#"+div+" #"+_tempRandId).next().hide();
				
				// 라스트 셀 설정
				_mainLastsel=_tempRandId;
				
				// 서브 그리드 url 전송 설정(true 전송)
				subGridUrl = true;
				
				$('#'+_tempRandId+'_licenseName').addClass('autoCom');
				$('#'+_tempRandId+'_licenseName').css({'width' : '60px'});

				// OSS TABLE_ADD_checkbox attr, class edit
				$("#"+_tempRandId).find('input[type=checkbox]').removeAttr("checked");
				$("#"+_tempRandId).find("input[type=checkbox]").removeClass("cbox");
			}
		},
		// 행삭제 메인 & 서브
	 	rowDel : function(target, flag){
	 		var selrow = target.jqGrid('getGridParam', "selrow" );
	 		// 메인 그리드 로우 삭제시 서브 그리드 동시 삭제
			if(flag == "main"){
	    	   var targetDataObj = target.selector;
	    	   
				//bat 일 경우
				if(targetDataObj=="#batList" || targetDataObj=="#binAndroidList"){
					if(target.jqGrid("getCell", selrow, "customBinaryYn") == "Y"){
						target.jqGrid('collapseSubGridRow', selrow);
						target.jqGrid('delRowData', selrow);

			    	   if("#binAndroidList" == targetDataObj) {
							fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
			    	   } else if("#batList" == targetDataObj) {
							fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
			    	   }
					} else {
						alertify.error('<spring:message code="msg.common.cannot.delete" />', 0);
					}
				} else {
					target.jqGrid('collapseSubGridRow', selrow);
					target.jqGrid('delRowData', selrow);
					fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
				}
			// 서브 그리드 로우만 삭제
			} else if(flag == "sub") {
				var editableFlag = target.jqGrid('getCell',selrow,'editable');
				
				if(editableFlag=="Y") {
					target.jqGrid('delRowData', selrow);
					fn_grid_com.deleteLocalDataAfterDelRow(target, selrow, flag);
				} else {
					alertify.error('<spring:message code="msg.common.cannot.registered.delete" />', 0);
				}
			}
		},
		// 행삭제 메인 & 서브 (new)
	 	rowDelNew : function(target, flag){
			$("#loading_wrap").show();
			
			setTimeout(function(){
				try{
					var selarrrow = target.jqGrid('getGridParam', "selarrrow");
			 		
					if(flag == "main"){
			    	   	var targetDataObj = target.selector;
			    	   	onAjaxLoadingHide = false;

			    	   	var dataArray = target.jqGrid('getGridParam', 'data');
						
						for(var i=selarrrow.length-1; i>=0; i--){
							var selrow = selarrrow[i];
							dataArray = fn_grid_com.deleteLocalDataAfterDelRowData(dataArray, selrow);
						}
						//bat 일 경우
						if(targetDataObj=="#batList" || targetDataObj=="#binAndroidList"){
							if(target.jqGrid("getCell", selrow, "customBinaryYn") == "Y"){
								for(var i=selarrrow.length-1; i >= 0; i--){
									var selrow = selarrrow[i];
									target.jqGrid('collapseSubGridRow', selrow);
									target.jqGrid('delRowData', selrow);
								}

					    	   if("#binAndroidList" == targetDataObj) {
									fn_grid_com.deleteLocalDataAfterDelRowNew(target, dataArray, flag);
					    	   } else if("#batList" == targetDataObj) {
									fn_grid_com.deleteLocalDataAfterDelRowNew(target, dataArray, flag);
					    	   }
							} else {
								alertify.error('<spring:message code="msg.common.cannot.delete" />', 0);
							}
						} else {
							for(var i=selarrrow.length-1; i >= 0; i--){
								var selrow = selarrrow[i];
								target.jqGrid('collapseSubGridRow', selrow);
								target.jqGrid('delRowData', selrow);
							}

							fn_grid_com.deleteLocalDataAfterDelRowNew(target, dataArray, flag);
						}
					} 
				}catch(e){
					alertify.error('<spring:message code="msg.common.cannot.delete" />', 0);
				}finally{
					$("#loading_wrap").hide();
				}
			}, 300);
		},
		deleteLocalDataAfterDelRowData : function(dataArray, selrow) {
			var reMakeArrObj=[];
	    	var newIdx = 0;

	    	for(var idx=0; idx < dataArray.length; idx++) {
				if(dataArray[idx].gridId != selrow) {
					reMakeArrObj[newIdx++] = dataArray[idx];
				}
			}
			
			return reMakeArrObj;
		},
		deleteLocalDataAfterDelRowNew : function(target, dataArray, flag) {
			// client array에서 삭제
			var targetDataObj = target.selector;
	       	var reMakeArrObj=dataArray;		
			
			if(flag == "main"){
				target.jqGrid('GridUnload');
				
				if("#binAndroidList" == targetDataObj) {
					binAndroidMainData = reMakeArrObj;
					binAndroid_grid.load();
					// total record 표시
					$("#binAndroidList_toppager_right, #binAndroidPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binAndroidMainData.length+'</div>');
				} else if("#srcList" == targetDataObj) {
					srcMainData = reMakeArrObj;
					src_grid.load();
					// total record 표시
	        		$("#srcList_toppager_right, #srcPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+srcMainData.length+'</div>');
				} else if("#binList" == targetDataObj) {
					binMainData = reMakeArrObj;
					bin_grid.load()
					// total record 표시
					$("#binList_toppager_right, #binPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+binMainData.length+'</div>');
				} else if("#list" == targetDataObj) {
					partyMainData = reMakeArrObj;
					grid.init();
					// total record 표시
					$("#list_toppager_right, #pager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+partyMainData.length+'</div>');
				} else if("#batList" == targetDataObj) {
					batMainData = reMakeArrObj;
					bat_grid_list.load();
					// total record 표시
					$("#batList_toppager_right, #batPager_right").html('<div dir="ltr" style="text-align:right" class="ui-paging-info">Total : '+batMainData.length+'</div>');
				}
			}
		},
		deleteLocalDataAfterDelRow : function(target, selrow, flag) {
			// client array에서 삭제
			var targetDataObj = target.selector;
	       	var dataArray = $(""+targetDataObj).jqGrid('getGridParam', 'data');
	       	
			var reMakeArrObj=[];
			var newIdx = 0;
			
			for(var idx=0; idx < dataArray.length; ++idx) {
				if(dataArray[idx].gridId != selrow) {
					reMakeArrObj[newIdx++] = dataArray[idx];
				}
			}
			
			if(flag == "main"){
				target.jqGrid('GridUnload');
				
				if("#binAndroidList" == targetDataObj) {
					binAndroidMainData = reMakeArrObj;
					binAndroid_grid.load();
				} else if("#srcList" == targetDataObj) {
					srcMainData = reMakeArrObj;
					src_grid.load();
				} else if("#binList" == targetDataObj) {
					binMainData = reMakeArrObj;
					bin_grid.load()
				} else if("#list" == targetDataObj) {
					partyMainData = reMakeArrObj;
					grid.init();
				} else if("#batList" == targetDataObj) {
					batMainData = reMakeArrObj;
					bat_grid_list.load();
				}
			} else {
				// sub
				var targetSubDataObj = targetDataObj.split("_")[0];
				
				if("#binAndroidList" == targetSubDataObj) {
					binAndroidSubData[selrow.split("-")[0]] = reMakeArrObj;
					target.jqGrid('setGridParam', {data: binAndroidSubData}).trigger("reloadGrid");
				} else if("#srcList" == targetSubDataObj) {
					srcSubData[selrow.split("-")[0]] = reMakeArrObj;
					target.jqGrid('setGridParam', {data: srcSubData}).trigger("reloadGrid");
				} else if("#binList" == targetSubDataObj) {
					binSubData[selrow.split("-")[0]] = reMakeArrObj;
					target.jqGrid('setGridParam', {data: binSubData}).trigger("reloadGrid");
				} else if("#list" == targetSubDataObj) {
					partySubData[selrow.split("-")[0]] = reMakeArrObj;
					target.jqGrid('setGridParam', {data: partySubData}).trigger("reloadGrid");
				} else if("#batList" == targetSubDataObj) {
					batSubData[selrow.split("-")[0]] = reMakeArrObj;
					target.jqGrid('setGridParam', {data: batSubData}).trigger("reloadGrid");
				}
			}
		},
		// 멀티라이센스 펼치기 및 싱글 라이센스 서브 그리드 숨기기
	 	multyExpand : function(target){
			var arr = [];
			arr = $('#'+target).jqGrid('getDataIDs');
			
			for(var i in arr){
				$('#'+target).jqGrid('expandSubGridRow',arr[i]);
				if($('#'+target).jqGrid('getCell',arr[i],'licenseDiv') != "M"){
					$("#"+target+" #"+arr[i]).find("td:first").removeClass("sgcollapsed").find("a").hide();
					$("#"+target+" #"+arr[i]).next().hide();
				}
			}
		},
		// 메인 그리드 excludeYn 에디트 모드
	 	setCellEdit : function(target, rowid, msgData, diffMsgData, infoMsgData, callbackFunc){
			if(rowid){
				if(rowid != _mainLastsel) {
					if(_mainLastsel != -1) {
						var licenseName = callbackFunc(target.getRowData(_mainLastsel));
						target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
						fn_grid_com.saveCellData(target.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
						target.jqGrid('saveRow',_mainLastsel);
					} else {
						target.jqGrid('saveRow',_mainLastsel);
					}
					
					gridValidMsgRowId(msgData, target.selector.replace("#", ""), _mainLastsel);
					
					if(diffMsgData) {
						gridDiffMsgRowId(diffMsgData, target.selector.replace("#", ""), _mainLastsel);
					}
					
					if(infoMsgData) {
						gridInfoMsgRowId(infoMsgData, target.selector.replace("#", ""), _mainLastsel);
					}
				}

				fn_grid_com.setMainEditMode(target, rowid);
				target.jqGrid('editRow',rowid);
				_mainLastsel=rowid;
			}
		},
		// 메인 그리드 에디트 모드 설정
	 	setMainEditMode : function(target, rowid){
                        target.jqGrid('setColProp','gridId', {editable: true});
			target.jqGrid('setColProp','licenseId', {editable: true});
			target.jqGrid('setColProp','licenseName', {editable: true});
			target.jqGrid('setColProp','licenseText', {editable: true});
			target.jqGrid('setColProp','copyrightText', {editable: true});
			target.jqGrid('setColProp','filePath', {editable: true});
			target.jqGrid('setColProp','ossName', {editable: true});
			target.jqGrid('setColProp','ossVersion', {editable: true});
			target.jqGrid('setColProp','downloadLocation', {editable: true});
			target.jqGrid('setColProp','homepage', {editable: true});
			
			var customBinary = target.jqGrid('getCell',rowid,'customBinaryYn');
			
			if(customBinary == "Y") {
				target.jqGrid('setColProp','binaryName', {editable: true});
			} else {
				target.jqGrid('setColProp','binaryName', {editable: false});
			}
		},
		// 메인 그리드 OSS 등록/상세 페이지 이동
	 	mvOssPage : function(target, rowid){
			target.jqGrid('saveRow',rowid);
			var row = target.jqGrid('getRowData', rowid);
			var ossName = target.jqGrid('getCell',rowid,'ossName');
			var ossVersion = target.jqGrid('getCell',rowid,'ossVersion');
			
			if(ossVersion=="N/A") {
				ossVersion="";
			}
			
			if(ossName!=""){
				onAjaxLoadingHide = true;
				$.ajax({
					url : '<c:url value="/project/getOssIdCheck"/>',
					type : 'GET',
					dataType : 'json',
					cache : false,
					data : {ossName : ossName, ossVersion : ossVersion},
					contentType : 'application/json',
					success : function(data){
						var _ossId = "";
						
						if(typeof(data.ossIdInfo)!="undefined") {
							target.jqGrid('setCell', rowid, 'ossId', data.ossIdInfo['ossId']);
							_ossId = data.ossIdInfo['ossId'];
						}
						
						// ossId 있을경우 상세 페이지 이동
						if(_ossId != "" && _ossId != undefined){
							createTabInFrame(_ossId+'_Opensource', '#<c:url value="/oss/edit/'+_ossId+'"/>');
						}else{
							if('${sessUserInfo.authority}' == 'ROLE_ADMIN'){
								onAjaxLoadingHide = true;

								var ossData = {ossName:row['ossName'], ossVersion:row['ossVersion'], downloadLocation:row['downloadLocation'],homepage:row['homepage'], licenseName:row['licenseName'], licenseText:row['licenseText'], copyright:row['copyrightText']};

								$.ajax({
									url : '<c:url value="/oss/saveSessionOssInfo"/>',
									type : 'POST',
									dataType : 'json',
									cache : false,
									data : ossData,
									success : function(data){
										if(data.isValid == 'true') {
											if(data.validMsg && data.validMsg != "") {
												_popupOss = window.open('<c:url value="/oss/copy/'+data.validMsg+'?ossVersion='+row['ossVersion']+'"/>', '', 'width=1100, height=700, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes');

												if(!_popupOss || _popupOss.closed || typeof _popupOss.closed=='undefined') {
													alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
												}
											} else {
												_popupOss = window.open("<c:url value="/oss/edit"/>", "", "width=1100, height=700, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes");

												if(!_popupOss || _popupOss.closed || typeof _popupOss.closed=='undefined') {
													alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
												}
											}
										} else {
											alertify.error('<spring:message code="msg.common.valid2" />', 0);
										}
									},
									error : function(){
										alertify.error('<spring:message code="msg.common.valid2" />', 0);
									}
								});
							}
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}else{
				target.jqGrid('editRow',rowid);
			}
		},
		// 메인 그리드 OSS 등록/상세 페이지 이동
	 	showOssViewPage : function(target, rowid, restoreYn, _validMsgData, _diffMsgData, _infoMsgData, callbackFunc){
	 		var _targetId = target.selector.replace("#", "");
	 		cleanErrMsg(_targetId, rowid);

	 		if(_mainLastsel != -1) {
				var licenseName = callbackFunc(target.getRowData(_mainLastsel));
				target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
				fn_grid_com.saveCellData(target.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
				target.jqGrid('saveRow',_mainLastsel);
			} else {
				target.jqGrid('saveRow',_mainLastsel);
			}
			
			var ossName = target.jqGrid('getCell',rowid,'ossName');
			var ossVersion = target.jqGrid('getCell',rowid,'ossVersion');
			if(!restoreYn || restoreYn == undefined) target.jqGrid('editRow',rowid);
			if(ossVersion=="N/A") ossVersion="";
			
			if(_validMsgData) {
				gridValidMsgRowId(_validMsgData, _targetId, rowid);
			}
			
			if(_diffMsgData) {
				gridDiffMsgRowId(_diffMsgData, _targetId, rowid);
			}
			
			if(_infoMsgData) {
				gridInfoMsgRowId(_infoMsgData, _targetId, rowid);
			}
			
			if(ossName!=""){
				onAjaxLoadingHide = true;
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
							var _encUrl = "ossName="+fn_grid_com.replaceGetParamChar(ossName)+"&ossVersion="+fn_grid_com.replaceGetParamChar(ossVersion);

							if(_popup == null || _popup.closed){
								_popup = window.open("<c:url value='/oss/osspopup?"+_encUrl+"'/>", "ossViewPopup_"+ossName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");

								if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
									alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
								}
							} else {
								_popup.close();
								_popup = window.open("<c:url value='/oss/osspopup?"+_encUrl+"'/>", "ossViewPopup_"+ossName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");
							}
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		// [MC요청] 2. Project List – OSS List에서 Binary DB 검색 결과 제공
	 	showBinaryViewPage : function(target, rowid, restoreYn, _validMsgData, _diffMsgData, _infoMsgData){
	 		var _targetId = target.selector.replace("#", "");
	 		cleanErrMsg(_targetId, rowid);
			target.jqGrid('saveRow',rowid);
			var path = target.jqGrid('getCell', rowid, 'binaryName').split("/");
			var filename = path[path.length-1];

			if(!restoreYn || restoreYn == undefined) target.jqGrid('editRow',rowid);
			
			if(_validMsgData) {
				gridValidMsgRowId(_validMsgData, _targetId, rowid);
			}
			if(_diffMsgData) {
				gridDiffMsgRowId(_diffMsgData, _targetId, rowid);
			}
			if(_infoMsgData) {
				gridInfoMsgRowId(_infoMsgData, _targetId, rowid);
			}
			
			if(filename!=""){
				onAjaxLoadingHide = true;
				$.ajax({
					url : '<c:url value="/system/bat/existBinaryName"/>',
					type : 'GET',
					dataType : 'json',
					cache : false,
					async: false,
					data : {filename : filename},
					contentType : 'application/json',
					success : function(data){
						if(data.isValid) {
							var _encUrl = "filename="+fn_grid_com.replaceGetParamChar(filename);
							
							if(_popup == null || _popup.closed) {
								_popup = window.open("<c:url value='/system/bat/binarypopup?"+_encUrl+"'/>", "binaryViewPopup_"+filename, "width=1450, height=650, toolbar=no, location=no, left=100, top=100");
								if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
									alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
								}
							} else {
								_popup.close();
								_popup = window.open("<c:url value='/system/bat/binarypopup?"+_encUrl+"'/>", "binaryViewPopup_"+filename, "width=1450, height=650, toolbar=no, location=no, left=100, top=100");
							}
						}
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		//  메인, 서브 그리드 세이브 모드
		totalGridSaveMode : function(targetstr){
			var target = $("#"+targetstr);

			var subTarget;
			var mArr = [];
			var sArr = [];
			mArr = target.jqGrid('getDataIDs');
			
			for(var m in mArr){
				target.jqGrid('saveRow',mArr[m]);
				subTarget = $("#"+targetstr+"_"+mArr[m]+"_t");
				if(subTarget && subTarget.length > 0) {
					sArr = subTarget.jqGrid('getDataIDs');
					for(var s in sArr){
						subTarget.jqGrid('saveRow',sArr[s]);
						var subDataArray = subTarget.jqGrid('getRowData', sArr[s]);
						if(subDataArray) {
						   $.each(subDataArray, function(_key, _val){
							   fn_grid_com.saveCellData(targetstr+"_"+mArr[m]+"_t",sArr[s], _key, _val, null, null);
			 			 	});
						}
					}
				} else {
				}
			}
			
			// 현재 수정중인 row가 있는 경우 
			if(_mainLastsel && _mainLastsel != -1) {
				var dataArray = target.jqGrid('getRowData', _mainLastsel);
				if(dataArray) {
				   $.each(dataArray, function(_key, _val){
					   fn_grid_com.saveCellData(targetstr,_mainLastsel, _key, _val, null, null);
	 			 	});
				}
			}
		},
		//  메인, 서브 그리드 데이터 스트링
		totalGridDataString : function(gData,target){
			gData = "";
			var arr = [];
			arr = $("#"+target).jqGrid('getDataIDs');
			gData = JSON.stringify($("#"+target).jqGrid("getRowData"));
			for(var i in arr){
				gData += JSON.stringify($("#"+target+"_"+arr[i]+"_t").jqGrid("getRowData"));
			}
			return gData;
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
					(licenseId=="") ? licenseId += subGrid.jqGrid('getCell',arr[i],'licenseId') : licenseId += ","+subGrid.jqGrid('getCell',arr[i],'licenseId');
					(licenseText=="") ? licenseText += subGrid.jqGrid('getCell',arr[i],'licenseText') : licenseText += ","+subGrid.jqGrid('getCell',arr[i],'licenseText');
					(copyrightText=="") ? copyrightText += subGrid.jqGrid('getCell',arr[i],'copyrightText') : copyrightText += ","+subGrid.jqGrid('getCell',arr[i],'copyrightText');
				}else{
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
		},
		saveCellData : function(target,rowid,idName,value,ValidMsgData,diffMsgData,infoMsgData){
			// OSS Table > 편집 후 Save할 때 각 cell별 기입된 사항 중 앞 뒤 Space 제거 요청.
			// value 값이 undefined로 올경우는 undefined로 return, 정상적인 값이 들어오게 되면 trim을 적용.
			value = value && value.replace(/(\&nbsp\;)/g, "").trim();
			var gridNm = "";
			if(target == "batList_e"){
				gridNm = "batList";
			}else{
				gridNm = target;
			}
			
	       var dataArray = $("#"+gridNm).jqGrid('getGridParam', 'data');
	       var cnt = $("#"+gridNm).jqGrid('getGridParam', 'reccount');
	       var row = -1;
	       
	       for(var idx=0; idx < dataArray.length; ++idx) {
	    	   if(dataArray[idx].gridId + "" === rowid + "") {
	    		   row = idx;
	    		   break;
	    	   }
	       }
	       
           if(row > -1 && dataArray[row] && typeof dataArray[row][idName] !== 'undefined') {
	           eval("dataArray["+(row)+"]."+idName+" = value;");

		       if(gridNm.substr(-2) === "_t") {
		    	   var targetSubDataObj = gridNm.split("_")[0];
		    	   if("binAndroidList" == targetSubDataObj) {
			    	   binAndroidSubData[rowid.split("-")[0]] = dataArray;
		    	   } else if("srcList" == targetSubDataObj) {
		    		   srcSubData[rowid.split("-")[0]] = dataArray;
		    	   } else if("binList" == targetSubDataObj) {
		    		   binSubData[rowid.split("-")[0]] = dataArray;
		    	   } else if("list" == targetSubDataObj) {
		    		   partySubData[rowid.split("-")[0]] = dataArray;
		    	   } else if("batList" == targetSubDataObj) {
		    		   batSubData[rowid.split("-")[0]] = dataArray;
		    	   }
		       }
		       
	           if(ValidMsgData != null || ValidMsgData != undefined){
	        	   gridValidMsgChk(rowid,idName,ValidMsgData,target);
	           }
	           if(diffMsgData != null || diffMsgData != undefined){
	        	   gridDiffMsgChk(rowid,idName,diffMsgData,target);
	           }
	           if(infoMsgData != null || infoMsgData != undefined){
	        	   gridInfoMsgChk(rowid,idName,infoMsgData,target);
	           }	           
           }
		},
		addEtcKeyDownEvent : function(target, _validMsgData, _diffMsgData, _infoMsgData, callbackFunc) {
			
			target.keydown(function(e) {
				if(e.keyCode==27) {
					if(_mainLastsel > -1 && e.target){
						if(typeof callbackFunc == "function") {
							var licenseName = callbackFunc(target.getRowData(_mainLastsel));
							target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
							fn_grid_com.saveCellData(target.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
							
							target.jqGrid('saveRow',_mainLastsel);
						} else {
							target.jqGrid('saveRow',_mainLastsel);
						}
						
						if(_validMsgData) {
							gridValidMsgRowId(_validMsgData, target.selector.replace("#", ""), _mainLastsel);
						}
						
						if(_diffMsgData) {
							gridDiffMsgRowId(_diffMsgData, target.selector.replace("#", ""), _mainLastsel);
						}
						
						if(_infoMsgData) {
							gridInfoMsgRowId(_infoMsgData, target.selector.replace("#", ""), _mainLastsel);
						}
									
						_mainLastsel = -1;
						
						return false;
					}
					if(typeof(_mainLastsel)== "string" && _mainLastsel.match("jqg")){
						if(typeof callbackFunc == "function") {
							var licenseName = callbackFunc(target.getRowData(_mainLastsel));
							target.jqGrid("setCell", _mainLastsel, "licenseName", licenseName);
							fn_grid_com.saveCellData(target.attr("id"), _mainLastsel, "licenseName", licenseName, null, null);
							
							target.jqGrid('saveRow',_mainLastsel);
						} else {
							target.jqGrid('saveRow',_mainLastsel);
						}
					}
					if(typeof(_mainLastsel)== "number" && _mainLastsel == -1) {
						var subTarget = $(e.target).parent().parent().parent().parent().parent().find('table').attr('id');
						var selrow = $(e.target).parent().parent().find('td').eq(0).attr('title');
						if(selrow != undefined && selrow.match("jqg")){
							$("#"+subTarget).jqGrid('saveRow',selrow);
						}
					}
				}
			});
		},
		replaceGetParamChar : function(_param) {
			_param = _param.replace(/&/g,"%26");
			_param = _param.replace(/\+/g,"%2B"); 
			 return _param;
		},
		deleteWarningMsg : function(list, rowid) {
			$.each(list,function(key,value) {
				if(key.indexOf(rowid) > -1) {
	    			delete list[key];
				}
			});
			return list;
		},

		// oss 일괄등록
	 	ossBulkReg : function(_prjId, _refDiv){
			
			if(_prjId && _refDiv){
				_popupBulkOssRef = window.open("<c:url value='/oss/ossBulkReg?prjId="+_prjId+"&referenceDiv="+_refDiv+"'/>", "ossBulkRegPopup", "width=1500, height=800, toolbar=no, location=no, left=100, top=100, resizable=yes, scrollbars=yes");

				if(!_popupBulkOssRef || _popupBulkOssRef.closed || typeof _popupBulkOssRef.closed=='undefined') {
					alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
				}
			}
		},
		checkLicenseTextValidation : function(_prjId, _type){
			$.ajax({
				url : '<c:url value="/checkLicenseText/valid" />',
				cache : false,
				async: false,
				data : JSON.stringify({ "prjId" : _prjId , "regType" : _type}),
				type : 'POST',
				dataType : 'json',
				contentType : 'application/json',
				success : function(resultData){
					if(resultData.isValid){
						if(resultData.returnMsg.toUpperCase() == "COMPLETE"){
							$("#checkLicenseTextFile").show();
							downloadCheckLicenseText = resultData.downloadUrl;
						}else{
							if(_type != "load"){
								fn_grid_com.checkLicenseText(_prjId);
							}
						}
						
					}else{
						if(_type != "load"){
							alertify.error(resultData.returnMsg, 0);
						}
						$("#checkLicenseTextFile").hide();
					}
					
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		},
		checkLicenseText : function(_prjId){
			$.ajax({
				url : '<c:url value="/checkLicenseText/start" />',
				cache : false,
				async: false,
				data : JSON.stringify({ "prjId" : _prjId }),
				type : 'POST',
				dataType : 'json',
				contentType : 'application/json',
				success : function(resultData){
					if(resultData.isValid){
						var _url = "${ct:getCodeExpString(ct:getConstDef('CD_CHECK_LICENSETEXT_SERVER_INFO'), ct:getConstDef('CD_SERVER_URL'))}";
						_url += "?pid="+_prjId+"&email="+resultData.email;

						if(""+resultData.isProd == "false"){
							_url += "&dev=ok";
						}
						
						location.href = _url;
						alertify.alert(resultData.returnMsg, function(){});
						createTabInFrame(_prjId+'_Identify', '#<c:url value="/project/identification/'+_prjId+'/1"/>');
					}else{
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
					
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
}

var src_fn_com = {
	// License 상세 페이지 이동
	showLicenseRestrictionViewPage : function(gridNm, rowid){
		var target = $("#"+gridNm);
		target.jqGrid('saveRow',rowid);

		var licenseName = target.jqGrid('getCell',rowid,'licenseName');
		if(licenseName.indexOf("<a class=") > -1) {
			licenseName = $(licenseName).text();
		}
		if(licenseName.indexOf("<div class=") > -1) {
			licenseName = licenseName.substring(0, licenseName.indexOf("<div class="));
		}
		if(licenseName.indexOf("+") > -1) {
			licenseName = licenseName.replace(/\+/g, "%2B");
		}
		if(licenseName.indexOf("&") > -1){
			licenseName = licenseName.replace(/&/g, "%26");
		}
		target.jqGrid('editRow',rowid);

		if(_popup == null || _popup.closed) {
			_popup = window.open('<c:url value="/selfCheck/licensepopup?licenseName='+licenseName+'"/>', "licenseViewPopup_"+licenseName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");

			if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
				alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
			}
		} else {
			_popup.close();
			_popup = window.open('<c:url value="/selfCheck/licensepopup?licenseName='+licenseName+'"/>', "licenseViewPopup_"+licenseName, "width=900, height=700, toolbar=no, location=no, left=100, top=100");
		}

	},
}
</script>