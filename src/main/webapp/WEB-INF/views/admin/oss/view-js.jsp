<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script>
	var lastsel;	
	var temp={
		obligation:''
	};
	var licenseTags=[];
	
	$(document).ready(function () {
		'use strict';
		$('span[title]').qtip();
		
		data.init();
		
		$('#listSearch')
			.on('submit', function(e){
				return false;
			})
			.on('keypress', function(e){
				if((e.keyCode || e.which) == 13){
					$('.search').trigger('click');	// search enter
				}
			});
		
		if(data.components.length < 1){
			$('#_projectList').parent().parent().hide();
		}
		
		//Get auto-completed data list.
		commonAjax.getLicenseTags().success(function(data, status, headers, config){
			if(data != null){
				data.forEach(function(obj){
					var tag = {
						value : obj.licenseId,
						label : obj.licenseName,
						type : obj.licenseType,
						obligation : obj.obligation,
						obligationChecks : obj.obligationChecks
					}
					
					licenseTags.push(tag);
				});
			}
		});
		//When it's an auto-complete single license,
		setCustomAutoComplete('single');

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
					var url = '#<c:url value="/project/edit/'+rowData['prjId']+'"/>';
					createTabInFrame(rowData['prjId']+'_Project', url);
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
		
		// Vulnerability
		<c:if test="${not empty vulnInfoList}">
		$("#_vulnInfoList").jqGrid({
			datatype: 'local',
			data: data.vulnInfoList,
			colNames:['CVE ID','NVD Score', 'Description', 'Published Date'],
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
		
		//Expressing a button on the grid.
		function displayButtons(cellvalue, options, rowObject)
		{
			var deleted = "<input id='licenseBtn"+options.rowId+"' type='button' value='delete' class='btnCLight darkgray' />";
			return deleted;
		}

		//Look at the project list.
		$('#listMore').on('click',function(){
			createTabInFrameWithCondition('Project List', '#<c:url value="/project/list"/>', 'OSSLISTMORE', '${ossId}');
		});
	});
	
	function cveIdTag(cellvalue, options, rowObject){
		var tag = "<a href='https://web.nvd.nist.gov/view/vuln/detail?vulnId="+cellvalue+"' class='urlLink' target='_blank'>"+cellvalue+"</a>";

		return tag;
	}
	
	function unCveIdTag(cellvalue, options, rowObject){
		return cellvalue;
	}
	
	//date object
	var data = {
		detail : ${empty detail ? 'null':detail},
        detectedLicenseIdByName : ${empty detectedLicenseIdByName ? 'null': detectedLicenseIdByName},
		list : ${empty list ? '{rows:[]}' : list},
		components : ${empty components ? '[]' : components },
		vulnInfoList : ${empty vulnInfoList ? '[]' : vulnInfoList },
		clone : '',
		typeCodes : [],
		copyData : ${empty copyData ? 'null':copyData},
		init : function(){
			data.clone = $('.multiTxtSet').clone().html();
			data.cloneDownloadLocation = $('.multiDownloadLocationSet').clone().html();
			data.cloneDetectLicense = $('.multiDetectedLicenseSet').clone().html();

			//Data initialization (data from the controller)
			if(data.detail){
				/*Oss info*/
				$('#ossName').text(data.detail.ossName);
				var ossVersion = data.detail.ossVersion;
				var ossType = data.detail.ossType;
				$('#ossVersion').html(ossVersion);
				$('#downloadLocation a').text(data.detail.downloadLocation);

				$.each(data.detail.downloadLocations, function(idx, cur){
					if(idx == 0){
						$('.multiDownloadLocationSet span:first').remove();
					}

					if(cur != ''){
						$(data.cloneDownloadLocation).appendTo('.multiDownloadLocationSet');
						$("[name='downloadLocations'] > a:last").html(cur).attr("href", cur);
					}
				});

				$.each(data.detail.detectedLicenses, function(idx, cur){
					if(idx == 0){
						$('.multiDetectedLicenseSet span:first').remove();
					}
					if(cur != ''){
						$(data.cloneDetectLicense).appendTo('.multiDetectedLicenseSet');
						var tagForTab = '<a href="#none" onclick=createTabInFrame("'+data.detectedLicenseIdByName[cur]+'_License","#/license/edit/'+data.detectedLicenseIdByName[cur]+'")>'+cur+'</a>'
						$("[name='detectedLicenses']:last").html(tagForTab);
					}
				});
				
				$('#homepage a').text(data.detail.homepage);
				$('#homepage a').attr("href", data.detail.homepage);
				$('#Copyright').text(data.detail.copyright);
				$('#summaryDescription').text(data.detail.summaryDescription);
				$('#attribution').text(data.detail.attribution);
				
				//Check license priorities for imported data
				multiLicense();
				var licenseType = autoLicense(data.list.rows);
				var obligationHtml = autoObligation(data.list.rows);
				$('#lt td div').html(licenseType);
				$('#ob td div').html('');
				$(obligationHtml).appendTo('#ob td div');
				
				//Enter the nickname list data.
				data.detail.ossNicknames.forEach(function(nickName, index, obj){
					if(nickName!=''){
						if(index > 0) $("#nickNames").append(", ");
						$("#nickNames").append(nickName);
					}					
				});
				
				if (typeof ossType != "undefined" && "" != ossType) {
					var colOssType = '';
					if (ossType.toUpperCase().indexOf('M') > -1) {
						colOssType += '<span class="iconSet multi">Multi</span>';
					}
					
					if (ossType.toUpperCase().indexOf('D') > -1) {
						colOssType += '<span class="iconSet dual">Dual</span>';
					}
					
					if (ossType.toUpperCase().indexOf('V') > -1){
						colOssType += '<span class="iconSet vdif">v-Diff</span>';
					}
					$("[name=ossType]").html(colOssType);
				}
			}
		}
	}
	
	function multiLicense(){
		var list = [];
		
		if(data.copyData) {
			list = data.copyData.ossLicenses;
		} else {
			list = data.list.rows;
		}
		
		jQuery("#_licenseChoice").jqGrid({
			datatype: 'local',
			data:list,
			colNames:['','','License', 'Copyright','',''],
			colModel:[
				{name:'no', index: 'no', key:true, hidden:true},
				{name:'ossLicenseComb',index:'ossLicenseComb', width:70, align:"center", sortable:false, edittype:"select", editoptions:{value:"AND:AND;OR:OR"}},
				{name:'licenseNameEx',index:'licenseNameEx', width:300, editable:false},
				{name:'ossCopyright',index:'ossCopyright', width:400, edittype:"textarea"},
				{name:'licenseName',index:'licenseName', width:50, editable:true,hidden:true},
				{name:'licenseId',index:'licenseId', width:50, editable:true,hidden:true}
			],
			sortname: 'ossLicenseIdx',
			viewrecords: true,
			sortorder: "asc",
			height: 'auto',
			onSelectRow: function(id){
				showLicenseText();
			},
			loadComplete:function(){
				$('#1_ossLicenseComb').hide().prop('enabled', false);
				createMultiLicenseText();//License bundle text output.
				makeBackGroundColor();

				jQuery('#_licenseChoice').bind("jqGridInlineAfterSaveRow", function(ref,id){	
					var list = [];
					
					if(data.copyData) {
						list = data.copyData.ossLicenses;
					} else {
						list = data.list.rows;
					}
					
					var row = jQuery('#_licenseChoice').jqGrid('getRowData', id);
					
					row['ossLicenseIdx'] = id;
					row['obligation'] = temp.obligation;
					row['obligationChecks'] = temp.obligationChecks;
					row['licenseType'] = temp.licenseType;
					
					list.splice(id-1,1,row);//Add data to the list.
					
					makeBackGroundColor();
					//License type and Obligation auto check
					var type = autoLicense(list);
					var obligationHtml = autoObligation(list);
					
					if(list.length==0){
						type='';
						obligationHtml='';
					}

					$('#lt td').html(type);
					$('#ob td').html('');
					$(obligationHtml).appendTo('#ob td');
					createMultiLicenseText();//License bundle text output.

					lastsel=-1;	//initialize choice
				});	
			}
		});		
	}
	
	//Customizing autocomplete selection.
	function setCustomAutoComplete(div) {
		if(div == 'single') {
			$( '.autoComOssLicense' ).autocomplete({
				minLength: 0,
				source: licenseTags,
				select: function( event, ui ) {
					$( '.licenseSingle .autoComOssLicense' ).val(ui.item.label);
					$('input[name=licenseName]').val(ui.item.label);
					$('#lt td').html(ui.item.type);
					$('#ob td').html('');
					$(ui.item.obligation).appendTo('#ob td');

					return false;
				}
		    })
		    .autocomplete( "instance" )._renderItem = function( ul, item ) {
		    	return $( "<li>" )
		    	.append( "<div>" + item.label + "<strong> (" + item.type + ") </strong>" + item.obligation + "</div>" )
		    	.appendTo( ul );
		    };
    	} else if(div == 'multi') {
    		$('#'+lastsel+'_licenseName').autocomplete({
				minLength: 0,
				source: licenseTags,
				select: function( event, ui ) {
					$('#'+lastsel+'_licenseName').val(ui.item.label);
					
					var obj = {
						licenseId:ui.item.value,
						licenseName:ui.item.label,
						licenseType:ui.item.type,
						obligation:ui.item.obligation,
						obligationChecks:ui.item.obligationChecks,
						ossLicenseComb:''
					}
					
					temp = obj;
					
					return false;
				}
		    })
		    .autocomplete( "instance" )._renderItem = function( ul, item ) {
		    	return $( "<li>" )
		    	.append( "<div>" + item.label + "<strong> (" + item.type + ") </strong>" + item.obligation + "</div>" )
		    	.appendTo( ul );
		    };
    	}
	}
	
	
	
	//Automatic calculation of license priorities.
	function autoLicense(data){
		var result = '';
		var numbers = [];
		//1. Classifying by group.
		var groups = distributeGroups(data);
		
		//2. Compare the inside of each group.
		groups.forEach(function(group){
			var number = compareLicenseGroupMax(group);
			numbers.push(number);
		});
		
		//3. Compare each group (OR comparison)
		if(numbers.length != 1) {
			var min = getMin(numbers);
			
			switch(min) {
				case 1:
					result = 'Permissive';
					$('input[name=licenseType]').val("PMS");

					break;
				case 2:
					result = 'Weak Copyleft';
					$('input[name=licenseType]').val("WCP");

					break;
				case 3:
					result = 'Copyleft';
					$('input[name=licenseType]').val("CP");

					break;
			}
		} else {
			var min = numbers[0];
			
			switch(min){
				case 1:
					result = 'Permissive';
					$('input[name=licenseType]').val("PMS");

					break;
				case 2:
					result = 'Weak Copyleft';
					$('input[name=licenseType]').val("WCP");

					break;
				case 3:
					result = 'Copyleft';
					$('input[name=licenseType]').val("CP");

					break;
			}
		}
		return result;
		
	}
	
	//Obligation 우선순위 자동계산
	function autoObligation(data){
		var result = '';
		var numbers = [];
		
		//1. 그룹별 분류하기
		var groups = distributeGroups(data);
		
		//2. 각 그룹별 내부 비교하기
		groups.forEach(function(group){
			var number = compareObligationGroupMax(group);
			numbers.push(number);
		});
		
		//3. 각 그룹끼리 비교하기(OR 비교)
		if(numbers.length != 1) {
			var min = getMin(numbers);
			
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
		
		return result;
	}
	
	//AND그룹으로 묶어서 분류하기
	function distributeGroups(data){
		var type='A';
		var groups =[];
		var groupA =[];
		var groupB =[];
		
		data.forEach(function(item,index,ref) {
			if(item.ossLicenseComb == 'AND') {
				if(groupA.length > 0){	//AND - AND 배열 결속
					
				}

				if(groupB.length > 0){	//OR - AND 배열 결속
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
		var min = 3;
		
		numbers.forEach(function(number){
			if(min > number ){
				min = number;
			}
		});
		
		return min;
	}

	//라이센스 묶음 처리
	function createMultiLicenseText(){
		var rows = jQuery('#_licenseChoice').jqGrid('getRowData');
		var markTxt = '';
		
		rows.forEach(function(row, index, ref){
			 // row로 들어오는 데이터가 텍스트인지 element인지 확인.
			var olc = row.ossLicenseComb, lnm = row.licenseNameEx;
			var ossLicenseComb, licenseName;
			var url = '#<c:url value="${suffixUrl}/license/edit/'+row.licenseId+'"/>';
			
			ossLicenseComb = /<[a-z][\s\S]*>/i.test(olc) ? $("#" + getId(olc)).val() : olc;
			licenseName = /<[a-z][\s\S]*>/i.test(lnm) ? $("#" + getId(lnm)).val() : lnm;
			
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
		
		function getId(el){
			return $(el).attr("id");
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

			if(comb == 'AND') {
				$("#_licenseChoice").jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});
			} else if(comb == 'OR') {
				var flag = false;
				
				for(var j = 0; j<colors.length;j++) {
					if(lastColor == colors[j]){
						lastColor = colors[j+1];
						$("#_licenseChoice").jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});
						
						flag = true;
					} else if(lastColor == colors[colors.length-1]) {
						lastColor = colors[0];
						$("#_licenseChoice").jqGrid("setRowData", list[i].ossLicenseIdx, false, {'background-color' : lastColor});

						flag = true;
					}
					
					if(flag) {
						break;
					}
				}
			}
		}
	}

	//에러엘럿
	function onError(data, status){
		alertify.error('<spring:message code="msg.common.valid2" />', 0);
    };

    function showLicenseText(_licenseName) {
    	if(!_licenseName) {
    		var _selectedRow = $("#_licenseChoice").jqGrid('getGridParam', "selrow" ) ;

   			if(_selectedRow) {
   				licenseName = $('#_licenseChoice').jqGrid('getCell',_selectedRow,'licenseNameEx');
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
    
    function showOssViewPage(obj) {
    	var ossName = $(obj).parent().next().find('#ossName').text();
		var ossVersion = $(obj).parent().parent().next().next().find('#ossVersion').text();

		if ("" != ossName) {
			var _popup = null;
			
			if ("N/A" == ossVersion) {
				ossVersion = "";
			}
			
			$.ajax({
				url : '<c:url value="/oss/checkExistsOssByname"/>',
				type : 'GET',
				dataType : 'json',
				cache : false,
				data : {ossName : ossName},
				contentType : 'application/json',
				success : function(data){
					if(data.isValid == 'true') {
						if(_popup == null || _popup.closed) {
							_popup = window.open('<c:url value="/oss/osspopup?ossName='+ossName+'&ossVersion='+ossVersion+'"/>', 'ossViewPopup_'+ossName, 'width=900, height=700, toolbar=no, location=no, left=100, top=100');

							if(!_popup || _popup.closed || typeof _popup.closed=='undefined') {
								alertify.alert('<spring:message code="msg.common.window.allowpopup" />', function(){});
							}
						} else {
							_popup.close();
							_popup = window.open('<c:url value="/oss/osspopup?ossName='+ossName+'&ossVersion='+ossVersion+'"/>', 'ossViewPopup_'+ossName, 'width=900, height=700, toolbar=no, location=no, left=100, top=100');
						}
					} else {
						alertify.alert('<spring:message code="msg.selfcheck.info.unconfirmed.oss" />', function(){});
					}
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		}
    }
</script>
