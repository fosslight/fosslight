<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<script type="text/javascript">
	/*global $ */
	/*jslint browser: true, nomen: true */
	var initParam = {};
	var totalRow = 0;
	const G_ROW_CNT = "${ct:getCodeExpString(ct:getConstDef('CD_EXCEL_DOWNLOAD'), ct:getConstDef('CD_MAX_ROW_COUNT'))}";
	$(document).ready(function () {
		'use strict';
		setMaxRowCnt(G_ROW_CNT); // maxRowCnt 값 setting
		evt.init();
		data.init();
		showHelpLink("License_List_Main");
	});
	
	//데이터 객체
	var data = {
		typeCodes : [],
		tooltipCont : "<div class=\"tooltipData\"><dl><dt><span class=\"iconSet ops\">Notice Obligation</span>Notice Obligation</dt><dd></dd></dl><dl><dt><span class=\"iconSet man\">Source Code Obligation</span>Source Code Obligation</dt><dd></dd></dl></div>",
		existTooltip : false,
		init : function(){
			list.load();	// Grid Load
		}
	};
	
	//이벤트 객체
	var evt = {
		init : function(){

			$('select[name=licenseType]').val('${searchBean.licenseType}').trigger('change');
			$('select[name=obligationType]').val('${searchBean.obligationType}').trigger('change');
			$('select[name=creator]').val('${searchBean.creator}').trigger('change');
			$('select[name=modifier]').val('${searchBean.modifier}').trigger('change');
			
			initParam = fn.setGridParam();
			var defaultSearchFlag = "${searchBean.defaultSearchFlag}";
			if(defaultSearchFlag != 'Y') {
				// just make grid ui
				initParam.ignoreSearchFlag = "Y";
			}
			
			$('#search').on('click',function(e){
				e.preventDefault();
				var postData=fn.setGridParam();
				postData.ignoreSearchFlag = "N";
				$("#list").jqGrid('setGridParam', {postData:postData, page : 1}).trigger('reloadGrid');
			});
			
			$(".cal").on("keyup", function(e){
				calValidation(this, e);
			});
			$("#licenseNameAllSearchFlag").on("change", function(e){
				$("[name='licenseNameAllSearchFlag']").val($(this).prop("checked") ? "Y" : "N");
			});
		}
	};
	
	var fn = {
		downloadExcel : function(){
			if(isMaximumRowCheck(totalRow)){
				var data = $('#licenseSearch').serializeObject();
				
				// restrictions은 licenseMaster에서 String으로 선언되어 있기 때문에 gson변환시 에러가 발생
				// array형으로 받을 수 있도록 다른이름에 재설정한다.
				if (typeof data.restrictions !== "undefined") {
					if (Array.isArray(data.restrictions)) {
						data.arrRestriction = data.restrictions;
					} else {
						var restrictionArr = [];
						restrictionArr.push(data.restrictions);
						data.arrRestriction = restrictionArr;
					}
				}
				data.restrictions = "";
				
				$.ajax({
					type: "POST",
					url: '<c:url value="/exceldownload/getExcelPost"/>',
					data: JSON.stringify({"type":"license", "parameter":JSON.stringify(data)}),
					dataType : 'json',
					cache : false,
					contentType : 'application/json',
					success: function (data) {
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
					error: function(data) {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		},
		validationDate : function(){
			var flag = true;
			var cStart = $('input[name=cStartDate]').val().replace(/\-/g,'');
			var cEnd = $('input[name=cEndDate]').val().replace(/\-/g,'');
			var mStart = $('input[name=mStartDate]').val().replace(/\-/g,'');
			var mEnd = $('input[name=mEndDate]').val().replace(/\-/g,'');
			
			//둘다 비었을때
			if(!cStart && !cEnd){
				
			} else {
				if(!cStart){
					alert('<spring:message code="msg.license.confirm.startdate" />');

					flag = false;
				} else {
					alert('<spring:message code="msg.license.confirm.enddate" />');

					flag = false;
				}
			}
			if(flag){
				if(!mStart && !mEnd){
					
				} else {
					if(!mStart){
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
		setGridParam : function() {
			var paramData=$('#licenseSearch').serializeObject();
			
			if(paramData.restrictions != null){
				paramData.restrictions = JSON.stringify(paramData.restrictions);
				paramData.restrictions = paramData.restrictions.replace(/\"|\[|\]/gi, "");
			} else {
				paramData.restrictions = "";
			}
			
			return paramData;
		}
	};
	
	var list = {
		load : function(){
			$("#list").jqGrid({
				url:'<c:url value="/license/listAjax"/>',
				datatype: 'json',
				postData : initParam,
				jsonReader:{
					repeatitems: false,
					id:'licenseId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID','License Name', 'Identifier', 'License Type', 'Restriction', 'Obligation', 'Website', 'User Guide'<c:if test="${ct:isAdmin()}">, 'Creator', 'Created Date', 'Modifier', 'Modified Date'</c:if>],
				colModel: [
					  {name: 'licenseId', index: 'licenseId', width: 40, align: 'center', sorttype: 'int'}
					, {name: 'licenseName', index: 'licenseName', width: 200, align: 'left', formatter: 'linkLicenseName'}
					, {name: 'shortIdentifier', index: 'shortIdentifier', width: 100, align: 'left'}
					, {name: 'licenseType', index: 'licenseType', width: 70, align: 'center'}
					, {name: 'restriction', index: 'restriction', width: 70, align: 'center', formatter: fn_grid_com.displayLicenseRestriction, unformat: fn_grid_com.unformatter, sortable : false, search : false}
					, {name: 'obligation', index: 'obligation', width: 60, align: 'left'}
					, {name: 'webpage', index: 'webpage', width: 60, align: 'left', formatter: 'link2'}
					, {name: 'description', index: 'description', width: 150, align: 'left'}
				<c:if test="${ct:isAdmin()}">
					, {name: 'creator', index: 'creator', width: 80, align: 'center'}
					, {name: 'createdDate', index: 'createdDate', width: 75, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}}
					, {name: 'modifier', index: 'modifier', width: 80, align: 'center'}
					, {name: 'modifiedDate', index: 'modifiedDate', width: 75, align: 'center', formatter:'date', formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}}
				</c:if>
				],
				rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")},
				rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}],
	 			autowidth: true,
				pager: '#pager',
				gridview: true,
				sortable: function (permutation) {},
				sortname: 'licenseId',
				viewrecords: true,
				sortorder: 'desc',
				loadonce:false,
				height: 'auto' ,
				loadComplete: function(result){
					totalRow = result.records;
					// 헤더에 버튼 추가
					
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
					
					if(!data.existTooltip){
						$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_obligation"))
						.attr("title", data.tooltipCont).tooltip({
							content: function () {
								return $(this).prop('title');
							}
						});
						
						$.ajax({
							type: 'GET',
							url: '<c:url value="/system/processGuide/getProcessGuide"/>',
							data: {"id":"License_List_License_Type"},
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
						
						// Restriction 툴팁 추가
						$.ajax({
							type: 'GET',
							url: '<c:url value="/system/processGuide/getProcessGuide"/>',
							data: {"id":"License_List_Restriction"},
							success : function(data){
								if(data.processGuide){
									var contents = data.processGuide.contents;
									
									if(contents && contents.trim()) {
										$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_list_restriction"))
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
				},
				onSelectRow: function(id){
					$('#'+id+'description').width(130).height(140);
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					var rowData = $("#list").jqGrid('getRowData',rowid);
					createTabInFrame(rowData['licenseId']+'_License', '#<c:url value="/license/edit/'+rowData['licenseId']+'"/>');
				}
			});
		}
	};
	
	function displayUrl(cellvalue) {
		return "<a href=\""+cellvalue+"\" class=\"urlLink\" target=\"_blank\">"+cellvalue+"</a>";
	}
</script>