<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
	/*global $ */
	/*jslint browser: true, nomen: true */
	var initParam = {};
	var groupBuffer='';
	var totalRow = 0;
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	var linkFlag = "${searchBean.linkFlag}";
	
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt value setting
		evt.init();
		data.init();
		
		showHelpLink("OSS_List_Main");
	});
	
	//데이터 객체
	var data = {
		typeCodes : [],
		tooltipCont : "<div class=\"tooltipData\"><dl><dt><span class=\"iconSet ops\">Notice Obligation</span>Notice Obligation</dt><dd></dd></dl><dl><dt><span class=\"iconSet man\">Source Code Obligation</span>Source Code Obligation</dt><dd></dd></dl></div>",
		tooltipCont2 : "<div class=\"tooltipData350\"><dl><dt><span class=\"iconSet multi\">Multi License</span>Multi License</dt><dd>The OSS contains source codes under multiple licenses.</dd><dd>본 OSS는 여러 License 하의 Source Code를 포함하고 있습니다.</dd><dd>(e.g. \lib is LGPL-2.1 <span style=\"text-decoration : underline;\">and</span> \src is GPL-2.0)</dd></dl><dl><dt><span class=\"iconSet dual\">Dual License</span>Dual License</dt><dd>You can select one of registered licenses.</dd><dd>본 OSS는 등록된 License 중 하나를 선택할 수 있습니다.</dd><dd>(e.g. GPL-2.0 <span style=\"text-decoration : underline;\">or</span> MIT)</dd></dl><dl><dt><span class=\"iconSet vdif\">Version Different License</span>Version Different License</dt><dd>The OSS is distributed under <span style=\"text-decoration : underline;\">different licenses</span> according to its <span style=\"text-decoration : underline;\">versions</span>.</dd><dd>본 OSS는 Version에 따라 다른 License로 배포되고 있습니다.</dd><dd>(e.g. v1.0 is under GPL-2.0 and v2.0 is under BSD-3-Clause)</dd></dl></div>",
		existTooltip : false,
		init : function(){
			list.load();	// Grid Load
		}
		
	};
	//event object
	var evt = {
		init : function(){

			$('select[name=creator]').val('${searchBean.creator}').trigger('change');
			$('select[name=modifier]').val('${searchBean.modifier}').trigger('change');
			
			initParam = serializeObjectHelper();

			var defaultSearchFlag = "${searchBean.defaultSearchFlag}";
			if(defaultSearchFlag != 'Y') {
				// just make grid ui
				initParam.ignoreSearchFlag = "Y";
			}

			var sentMailParam = $("input[name=ossName]").val();
			if("Y" == linkFlag && sentMailParam){
				initParam.ignoreSearchFlag = "N";
				initParam = serializeObjectHelper();
			}
			
			$('#search').on('click',function(e){
				e.preventDefault();

				var searchOSSName = $("input[type=text][name=ossName]").val();
				$("input[type=text][name=ossName]").val(searchOSSName.trim());

				var searchLicenseName = $("input[type=text][name=licenseName]").val();
				$("input[type=text][name=licenseName]").val(searchLicenseName.trim());

				var searchHomepage = $("input[type=text][name=homepage]").val();
				$("input[type=text][name=homepage]").val(searchHomepage.trim());

				var postData = serializeObjectHelper();
				postData.ignoreSearchFlag = "N";
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');

			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});

			$("#ossNameAllSearchFlag").on("change", function(e){
				$("[name='ossNameAllSearchFlag']").val($(this).prop("checked") ? "Y" : "N");
			});

			$("#licenseNameAllSearchFlag").on("change", function(e){
				$("[name='licenseNameAllSearchFlag']").val($(this).prop("checked") ? "Y" : "N");
			});

			$("#deactivateFlag").on("change", function(){
				$("[name='deactivateFlag']").val($(this).prop("checked") ? "Y" : "N");
			});
		}
	};
	
	var fn = {
		showURL:function(){},
		hideURL:function(){},
		downloadExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = serializeObjectHelper();
				
				if(data.copyrights == ''){
					data.copyrights = [];
				}
	
				$('input[name=parameter]').val(JSON.stringify(data));
			
				$("#ossSearch").ajaxForm({
					url :'<c:url value="/exceldownload/getExcelPostOss"/>',
		            type : 'POST',
		            dataType:"json",
		            cache : false,
			        success : function (data) {
						   if("false" == data.isValid) {
							   if(data.validMsg == "overflow") {
								   alertify.error(getMsgMaxRowCnt(), 0);
							   } else {
				                   alertify.error('<spring:message code="msg.common.valid2" />', 0);
							   }
						   } else {
						       window.location =  '<c:url value="/exceldownload/getFile?id='+data.validMsg+'"/>';
						   }
			        },
		            error : function(data){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
			    }).submit();
			}
		},
		validationDate : function(){
			var flag = true;
			var cStart = $('input[name=cStartDate]').val().replace(/\./g,'');
			var cEnd = $('input[name=cEndDate]').val().replace(/\./g,'');
			var mStart = $('input[name=mStartDate]').val().replace(/\./g,'');
			var mEnd = $('input[name=mEndDate]').val().replace(/\./g,'');
			
			//if both empty
			if(!cStart && !cEnd) {
				
			} else {
				if(!cStart) {
					alert('<spring:message code="msg.license.confirm.startdate" />');
					flag = false;
				} else {
					alert('<spring:message code="msg.license.confirm.enddate" />');
					flag = false;
				}
			}
			
			if(flag) {
				if(!mStart && !mEnd) {
					
				} else {
					if(!mStart) {
						alert('<spring:message code="msg.license.confirm.startdate" />');
						flag = false;
					} else {
						alert('<spring:message code="msg.license.confirm.enddate" />');
						flag = false;
					}
				}
			}
			
			return flag;
		},
		ossNameLinkFormat : function(cellvalue, options, rowObject){
			var display = cellvalue;
			var url = '';

			if("${ct:isAdmin()}"){
				url = '<c:url value="/oss/edit/'+rowObject['ossId']+'"/>';
			} else {
				url = '<c:url value="/oss/view/'+rowObject['ossId']+'"/>';
			}

			display = "<a href='" + url + "' class='urlLink' target='_blank'>" + cellvalue + "</a>";
			
			return display;
		}
	}
	
	var list = {
		oldRowNum:20,
		load : function(){
			$("#list").jqGrid({
				url:'<c:url value="/oss/listAjax"/>',
				datatype: 'json',
				jsonReader:{
					repeatitems: false,
					id:'ossId',
					root:function(obj){
						//original RowNum save
						list.oldRowNum = $("#list").jqGrid('getGridParam', 'rowNum');
						
						//Change the rowNumber according to the number of lists.
						$("#list").jqGrid('setGridParam', {rowNum:obj.rows.length});
						$("#list").jqGrid('setGridParam', {defaultRowNum:list.oldRowNum});
						
						return obj.rows; 
					},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['', 'ID','OSS Type','OSS Name','Version','License Name', 'License Type', 'Obligation', 'Download Location', 'Homepage', 'Description', 'CVE ID', 'Vulnera<br/>bility'<c:if test="${ct:isAdmin()}">, 'Creator', 'Created Date','Modifier','Modified Date'</c:if>, 'groupKey'],
				colModel: [
					  {name: 'group', width: 20, align: 'center',
					    cellattr: function(rowId, tv, rawObject, cm, rdata) {
					        return ' colspan=2' 
					    },
					    formatter: function myFormatter(cellvalue, options, rowObject){
					        return rowObject.ossId;
					    }
					  }
					, {name: 'ossId', index: 'ossId', width: 80, align: 'center',
					    cellattr: function(rowId, tv, rawObject, cm, rdata) {
					        return ' style="display:none;"';
					    }
					  }
					, {name: 'ossType', index: 'ossType', width: 70, align: 'center',formatter: 'ossType'}
					<c:if test="${searchBean.linkFlag == 'N'}">
					, {name: 'ossName', index: 'ossName', width: 200, align: 'left', formatter: 'linkOssName'}
					</c:if>
					<c:if test="${searchBean.linkFlag == 'Y'}">
					, {name: 'ossName', index: 'ossName', width: 200, align: 'left', formatter: fn.ossNameLinkFormat}
					</c:if>
					, {name: 'ossVersion', index: 'ossVersion', width: 70, align: 'left'}
					, {name: 'licenseName', index: 'licenseName', width: 200, align: 'left'}
					, {name: 'licenseType', index: 'licenseType', width: 70, align: 'center'}
					, {name: 'obligation', index: 'obligation', width: 70, align: 'left'}
					, {name: 'downloadLocation', index: 'downloadLocation', width: 150, align: 'left', formatter: 'link', formatoptions: {target:'_blank'}}
					, {name: 'homepage', index: 'homepage', width: 65, align: 'left', formatter: 'link2', formatoptions: {target:'_blank'}}
					, {name: 'summaryDescription', index: 'summaryDescription', width: 150, height: 50, align: 'left'}
					, {name: 'cveId', index: 'cveId', hidden:true}
					, {name: 'cvssScore', index: 'cvssScore', width: 50, align: 'center', formatter:'vuln'}
					<c:if test="${ct:isAdmin()}">
					, {name: 'creator', index: 'creator', width: 80, align: 'center'}
					, {name: 'createdDate', index: 'createdDate', width: 75, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}}
					, {name: 'modifier', index: 'modifier', width: 80, align: 'center'}
					, {name: 'modifiedDate', index: 'modifiedDate', width: 75, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}}
					</c:if>
					, {name: 'groupKey', index: 'groupKey', hidden:true
						, cellattr: function(rowId, val, rawObject, cm, rdata) {
							var result;
							var isGroup = false;
							
							if(groupBuffer == val){
								isGroup = true;
							}else{
								isGroup = false;
							}
							
							groupBuffer = val;
							
							if(isGroup){
								result = 'isgroup="true"';
							}else{
								result = 'isgroup="false"';
							}
							
							return result;
						 } 
					 }
				],
				rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")},
				rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}],
	 			autowidth: true,
				pager: '#pager',
				gridview: true,
				viewrecords: true,
				loadonce:false,
				height: 'auto',
				grouping:true,
				groupingView:{
					groupField:['groupKey'],
					groupColumnShow:[false]
				},	// Enter the column name by group.
				gridComplete: function(){
					tableRefresh();
				},
				loadComplete: function(result) {
					totalRow = result.records;
					var rows = result.rows;
					var grid = this;

					if(totalRow == 0){
						var cStartDate = $("#cStartDate").val()||0;
						var cEndDate = $("#cEndDate").val()||0;
						var diffNum = +cStartDate - +cEndDate;
						
						
						var mStartDate = $("#mStartDate").val()||0;
						var mEndDate = $("#mEndDate").val()||0;
						var diffNum2 = +mStartDate - +mEndDate;

						if((diffNum > 0 && cEndDate > 0) 
								|| (diffNum2 > 0 && mEndDate > 0)){
							alertify.alert('<spring:message code="msg.common.search.check.date" />', function(){});
						}
					}
					
					//rowNum initiate @@1
					$("#list").jqGrid('setGridParam', {rowNum:list.oldRowNum});					
					
					// Insert the unfold button in the existing group header into the group column of the first row for each group (customize the first row to function as a group header).
					$('[id^=listghead_0]').each(function(){
						var addBtn = "<span style='cursor:pointer;' class='groupBtns ui-icon ui-icon-plus tree-wrap-ltr' onclick=\"$('#list').jqGrid('groupingToggle','" + $(this).attr("id") + "'); $('#" + $(this).next().attr("id") + "').show(); return false;\"> </span>";
						var position = $(this).next().next().children().eq(1).text();

						if(position != ""){
							$(this).next().children().eq(0).append(addBtn);
						}
					});
					//coloring groups
					//1. exist group button
					$('span.groupBtns').trigger('click').parent().parent().css('background-color' ,'#CDECFA');
					 
					//2. below lists
					$('tr td[isgroup="true"]', grid).parent().css('background-color' ,'#E1F6FA');
					
					//3. Group sub display icon period in the following lists.
					$('tr td[isgroup="true"]', grid).parent().find('td:first')
					.prepend($('<span class="ui-icon ui-icon-carat-1-sw"></span>').css('display','inline-block'));
					
					//group + - toggle
					$('span.groupBtns').on('click', function(e) {
						if($(this).hasClass('ui-icon-plus')) {
							$(this).removeClass('ui-icon-plus').addClass('ui-icon-minus');
						} else {
							$(this).removeClass('ui-icon-minus').addClass('ui-icon-plus');
						}
					});
					
					$('.listghead_0').hide();	// hide basic groupHeader
					
					// add button in header
					if(!data.existTooltip){
						$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_obligation"))
							.attr("title", data.tooltipCont).tooltip({
								content: function () {
									return $(this).prop('title');
								}
						});
						$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_ossType"))
							.attr("title", data.tooltipCont2).tooltip({
								content: function () {
									return $(this).prop('title');
								}
						});
						
						$.ajax({
							type: 'GET',
							url: '<c:url value="/system/processGuide/getProcessGuide"/>',
							data: {"id":"OSS_LIST_License_Type"},
							success : function(data){
								if(data.processGuide){
									var contents = data.processGuide.contents;
									
									if(contents && contents.trim()) {
										$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_licenseType"))
											.attr("title", contents).tooltip({
												content: function () {
													return $(this).prop('title');
												}
										});
									}
								}
							}
						});
						
						$.ajax({
							type: 'GET',
							url: '<c:url value="/system/processGuide/getProcessGuide"/>',
							data: {"id":"OSS_List_Vulnerability"},
							success : function(data){
								if(data.processGuide){
									var contents = data.processGuide.contents;
									
									if(contents && contents.trim()) {
										$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_cvssScore"))
											.attr("title", contents).tooltip({
												content: function () {
													return $(this).prop('title');
												}
										});
									}
								}
							}
						});
						
						data.existTooltip = true;						
					}

					var datas = result.rows, rows=this.rows, row, className, rowsCount=rows.length,rowIdx=0;
					
					for(var _idx=0;_idx<rowsCount;_idx++) {
						row = rows[_idx];
						className = row.className;
						
						if (className.indexOf('jqgrow') !== -1) {
							rowid = row.id;
							rowData = result.rows[rowIdx++];
							var dataObject = datas.filter(function(a){
								return a.ossId==rowid}
							)[0];
							
							if(dataObject.deactivateFlag == "Y" && className.indexOf('excludeRow') === -1) {
								className= className + ' excludeRow';
							}
							
							row.className = className;
						} else if(className.indexOf('ui-subgrid') !== -1){
							rowIdx++;
						}
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					if(iCol!=0){
						var rowData = $("#list").jqGrid('getRowData',rowid);

						if("Y" != linkFlag) {
							createTabInFrame(rowData['ossId']+'_Opensource', '#<c:url value="/oss/edit/'+rowData['ossId']+'"/>');
						} else {
							var openNewWindow = window.open("about:blank");
							if("${ct:isAdmin()}"){
								openNewWindow.location.href	= '<c:url value="/oss/edit/'+rowData['ossId']+'"/>';
							} else {
								openNewWindow.location.href	= '<c:url value="/oss/view/'+rowData['ossId']+'"/>';
							}
						}
					}
				},
				postData: initParam
			});
		}
	};
	
	// 헤더에 버튼 추가
	function displayUrl(cellvalue) {
		var icon1 = "<a href=\""+cellvalue+"\" class=\"urlLink\" target=\"_blank\">"+cellvalue+"</a>";
		
		return icon1;
	}

	function serializeObjectHelper() {
		var postData = $('#ossSearch').serializeObject();

		if(postData.ossTypeSearch != null) {
			postData.ossTypeSearch = JSON.stringify(postData.ossTypeSearch);
			postData.ossTypeSearch = postData.ossTypeSearch.replace(/\"|\[|\]|\,/gi, "");
		} else {
			postData.ossTypeSearch = "";
		}

		return postData;
	}
</script>