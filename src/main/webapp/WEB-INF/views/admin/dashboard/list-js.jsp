<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
	/*global $ */
	/*jslint browser: true, nomen: true */	
	$(document).ready(function () {
		$(window).bind('resize', function() {fn_sTblResizer();}).trigger('resize');

		<c:if test="${projectFlag || partnerFlag}">
			list.getJobsList();
			list.getCommentsList();
		</c:if>
		
		list.getOssList();
		list.getLicenseList();
		//2018-08-17 choye 추가
		evt.init();
		
		fn_sTblResizer();
	});
	
	//2018-08-17 choye 추가
	var evt = {
		init : function(){
			$(".btnConfirmedHistory").on('click',function(){
				var commentsCount = $('#commentsList').getGridParam('records');
				if(commentsCount>0){
					alertify.confirm('<spring:message code="msg.dashboard.comments.confirm.read" />', function (e) {
						if (e) {
							readSubmit();
						} else {
							return false;
						}
					});
				}else{
					alertify.alert('<spring:message code="msg.dashboard.comments.confirm.nocount" />', function(){});
					return false;
				}
			});
		}			
	};
	
	var list = {
		getJobsList : function(){
			$("#jobsList").jqGrid({
				url:"/dashboard/jobsListAjax",
				datatype: 'json',
				jsonReader:{
					repeatitems: false,
					id:'prjId',
					root:function(obj){return obj.rows;},
					page:function(obj){return obj.page;},
					total:function(obj){return obj.total;},
					records:function(obj){return obj.records;}
				},
				colNames: ['ID', 'Name', 'Status', 'Stage', 'Creator', 'Reviewer', 'prjId', 'prjDivision', 'reviewer', 'vStage'],
				colModel: [
					{name: 'prjDivisionId', index: 'prjDivisionId', width: 40, align: 'center', search: false},
					{name: 'prjName', index: 'prjName', width: 200, align: 'left', template: searchStringOptions},
					{name: 'status', index: 'status', width: 50, align: 'center', formatter: fn.displayStatus, unformatter: fn.unformatter, cellattr: fn.cellattrStatus, sortable : true, search: false},
					{name: 'stage', index: 'stage', width: 50, align: 'center', formatter: fn.displayStage, search: false},
					{name: 'creator', index: 'creator', width: 60, align: 'center', template: searchStringOptions},
					{name: 'reviewerName', index: 'reviewerName', width: 60, align: 'center', template: searchStringOptions},
					{name: 'prjId', index: 'prjId', hidden: true},
					{name: 'prjDivision', index: 'prjDivision', hidden: true},
					{name: 'reviewer', index: 'reviewer', hidden: true, search: true},
					{name: 'vStage', index: 'vStage', 
						formatter: function(cellVal, options, rowObj){
							return rowObj.stage;
						},
					hidden: true}
				],
	 			autowidth: true,
				pager: '#jobsPager',
                rowNum: 10,
                rowList: 10,
				gridview: true,
				sortable: function (permutation) {},
				loadonce : true,
                viewrecords: true,
				height: 'auto' ,
				loadComplete: function(){},
				onSelectRow: function(id){},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					var rowData = $("#jobsList").jqGrid('getRowData',rowid);
					
					if(iCol==3 && rowData['prjDivision']=='PRJ') {
						if(rowData['vStage']=="I"){
							createTabInFrame(rowData['prjId']+'_Identify', '#/project/identification/'+rowData['prjId']+'/4');
						}else if(rowData['vStage']=="P"){
							createTabInFrame(rowData['prjId']+'_Packaging', '#/project/verification/'+rowData['prjId']);
						}else if(rowData['vStage']=="D"){
							createTabInFrame(rowData['prjId']+'_Distribute', '#/project/distribution/'+rowData['prjId']);
						}else if(rowData['vStage']=="B"){
							createTabInFrame(rowData['prjId']+'_Project', '#/project/edit/'+rowData['prjId']);
						}
					} else {
						if(rowData['prjDivision']=='3RD'){
							createTabInFrame(rowData['prjId']+'_3rdParty', '#/partner/edit/'+rowData['prjId']);
						}else{
							createTabInFrame(rowData['prjId']+'_Project','#/project/edit/'+rowData['prjId']);
						}
					}
				}
			});
			
			// 툴팁 설정
			gridTooltip.existTooltip = false;
			
			if(!gridTooltip.existTooltip){
				$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_jobsList_status")).attr("title", gridTooltip.tooltipCont
				).tooltip({
					content: function () {
						return $(this).prop('title');
					}
				});
			}
			gridTooltip.existTooltip = true;
			
			$("#jobsList").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
			
            // 히든 값 reviewer를 필터로 사용 
            if(${ct:isAdmin()}){
	            $('#reviewerOnly').change(function() {
	            	var options = null;
	                if($(this).is(":checked")) {
	                	$('#gs_reviewer').val('${userId}');
	                	$("#jobsList")[0].triggerToolbar();
	                }else{
	                	$('#gs_reviewer').val("");
	                	$("#jobsList")[0].triggerToolbar();
	                }        
	            });
            }
		},
		
		getCommentsList : function(){
            $("#commentsList").jqGrid({
                url:"/dashboard/commentsListAjax",
                datatype: 'json',
                jsonReader:{
                    repeatitems: false,
                    id:'referenceId',
                    root:function(obj){return obj.rows;},
                    page:function(obj){return obj.page;},
                    total:function(obj){return obj.total;},
                    records:function(obj){return obj.records;}
                },
                colNames: ['ID','Name', 'Comments', 'Time', 'Creator', 'Reviewer', 'Comment by', 'readYn', 'referenceDiv', 'referenceId'],
                colModel: [
                    {name: 'prjDivisionId', index: 'referenceId', width: 40, align: 'center', search: false},
                    {name: 'prjName', index: 'prjName', width: 240, align: 'left', template: searchStringOptions},
                    {name: 'contents', index: 'contents', width: 70, align: 'left', formatter:displayComment, template: searchStringOptions},
                    {name: 'modifiedDate', index: 'modifiedDate', width: 70, align: 'center', formatter:'date', 
                    	formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, template: searchDateOptions
                    },
                    {name: 'creator', index: 'creator', width: 70, align: 'center', template: searchStringOptions},
                    {name: 'reviewer', index: 'reviewer', width: 70, align: 'center', template: searchStringOptions},
                    {name: 'user', index: 'user', width: 80, align: 'center', template: searchStringOptions},
                    {name: 'readYn', index: 'readYn', hidden: true, search: true},
                    {name: 'referenceDiv', index: 'referenceDiv', hidden: true},
                    {name: 'referenceId', index: 'referenceId', hidden: true}
                ],
                autowidth: true,
                pager: '#commentsPager',
                gridview: true,
                rowNum: 10,
                rowList: 10,
                sortable: function (permutation) {},
                loadonce : true,
                viewrecords: true,
                height: 'auto' ,
                loadComplete: function(){},
                onSelectRow: function(id){},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					if(iCol==0 || iCol==1) {
						var rowData = $("#commentsList").jqGrid('getRowData',rowid);
						if(rowData['referenceDiv']=='20'){
							createTabInFrame(rowData['referenceId']+'_3rdParty', '#/partner/edit/'+rowData['referenceId']);
						}else{
							createTabInFrame(rowData['referenceId']+'_Project','#/project/edit/'+rowData['referenceId']);
						}
					} else {
						var rowData = $("#commentsList").jqGrid('getRowData',rowid);
						if(rowData['referenceDiv']=='20'){
		    	            openCommentHistory('<c:url value="/comment/popup/3rd/'+rowData['referenceId']+'"/>');
						}else{
		    	            openCommentHistory('<c:url value="/comment/popup/prj/'+rowData['referenceId']+'"/>');
						}
					}
				}
            });
            
            $("#commentsList").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
            
            // 히든 값 readYn을 필터로 사용 
            $('#readYnFilter').change(function() {
            	var options = null;
                if($(this).is(":checked")) {
                	$('#gs_readYn').val("N");
                	$("#commentsList")[0].triggerToolbar();
                }else{
                	$('#gs_readYn').val("");
                	$("#commentsList")[0].triggerToolbar();
                }        
            });
		},
            
        getOssList : function(){
            $("#ossList").jqGrid({
                url:"/dashboard/ossListAjax",
                datatype: 'json',
                jsonReader:{
                    repeatitems: false,
                    id:'ossId',
                    root:function(obj){return obj.rows;},
                    page:function(obj){return obj.page;},
                    total:function(obj){return obj.total;},
                    records:function(obj){return obj.records;}
                },
                colNames: ['ID', 'Type','Name', 'Version', 'License', 'Modifier', 'Date'],
                colModel: [
                    {name: 'ossId', index: 'ossId',sorttype: 'int', hidden:true},
                    {name: 'ossType', index: 'ossType', width: 70, align: 'center', formatter: 'ossType', search: false},
                    {name: 'ossName', index: 'ossName', width: 170, align: 'left', template: searchStringOptions},
                    {name: 'ossVersion', index: 'ossVersion', width: 90, align: 'center', template: searchStringOptions},
                    {name: 'licenseName', index: 'licenseName', width: 170, align: 'left', template: searchStringOptions},
                    {name: 'modifier', index: 'modifier', width: 80, align: 'center', template: searchStringOptions},
                    {name: 'modifiedDate', index: 'modifiedDate', width: 90, align: 'center', formatter:'date', 
                    	formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}, template: searchDateOptions
                    }
                ],
                autowidth: true,
                pager: '#ossPager',
                gridview: true,
                rowNum: 10,
                rowList: 10,
                sortable: function (permutation) {},
                loadonce : true,
                viewrecords: true,
                height: 'auto' ,
                loadComplete: function(){},
                onSelectRow: function(id){},
                ondblClickRow: function(rowid,iRow,iCol,e) {
					var rowData = $("#ossList").jqGrid('getRowData',rowid);
					
					createTabInFrame(rowData['ossId']+'_Opensource', '#/oss/edit/'+rowData['ossId']);
                }
            });
            
			// 툴팁 설정
			gridTooltip.existTooltip = false;
			
			if(!gridTooltip.existTooltip){
				$('<span class="iconSet help right">Help</span>').appendTo($("#jqgh_ossList_ossType")).attr("title", gridTooltip.tooltipCont2
				).tooltip({
					content: function () {
						return $(this).prop('title');
					}
				});
			}
			
			gridTooltip.existTooltip = true;
            
            $("#ossList").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
        },
                
        getLicenseList : function(){
            $("#licenseList").jqGrid({
                url:"/dashboard/licenseListAjax",
                datatype: 'json',
                jsonReader:{
                    repeatitems: false,
                    id:'licenseId',
                    root:function(obj){return obj.rows;},
                    page:function(obj){return obj.page;},
                    total:function(obj){return obj.total;},
                    records:function(obj){return obj.records;}
                },
                colNames: ['ID', 'TypeFull','Type','Name', 'Identifier', 'Website', 'Modifier', 'Date'],
                colModel: [
                    {name: 'licenseId', index: 'licenseId', hidden:true},
                    {name: 'licenseTypeFull', index: 'licenseTypeFull', width: 60, align: 'center', search: false, hidden:true},
                    {name: 'licenseType', index: 'licenseType', width: 60, align: 'center', search: false,
                    	cellattr: function(rowId, val, rawObject, cm, rdata){
                    		return 'title="' + rawObject.licenseTypeFull + '"';
                    	}},
                    {name: 'licenseName', index: 'licenseName', width: 170, align: 'left', template: searchStringOptions},
                    {name: 'shortIdentifier', index: 'shortIdentifier', width: 80, align: 'center', template: searchStringOptions},
                    {name: 'webpage', index: 'webpage', width: 70, align: 'center', formatter: 'link2', template: searchStringOptions},
                    {name: 'modifier', index: 'modifier', width: 70, align: 'center', template: searchStringOptions},
                    {name: 'modifiedDate', index: 'modifiedDate', width: 70, align: 'center', formatter:'date', template: searchDateOptions,
                    	formatoptions: {srcformat: 'Y-m-d H:i:s.t', newformat: 'Y-m-d'}
                    }
                ],
                autowidth: true,
                pager: '#licensePager',
                gridview: true,
                rowNum: 10,
                rowList: 10,
                sortable: function (permutation) {},
                loadonce : true,
                viewrecords: true,
                height: 'auto' ,
                loadComplete: function(){},
                onSelectRow: function(id){},
                ondblClickRow: function(rowid,iRow,iCol,e) {
                	if(iCol!=4){
						var rowData = $("#licenseList").jqGrid('getRowData',rowid);
						createTabInFrame(rowData['licenseId']+'_License', '#/license/edit/'+rowData['licenseId']);
                	}
                }
            });
            
            $("#licenseList").jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
		}
	};
	
	var fn = {
		// unformater
		unformatter : function(cellvalue, options, rowObject){
			return cellvalue;
		},
		// Grid Status cell display
		displayStatus : function(cellvalue, options, rowObject){
// 			205 COMP	Complete
// 			205 PROG	Progress
// 			205 REQ		Request
// 			205 REV		Review
			var display = "";
			
			switch(cellvalue){
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_PROGRESS'))}":
					display = "<span class=\"iconSt draft\">"+ cellvalue +"</span>";
					break;
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_REQUEST'))}":
					display = "<span class=\"iconSt request\">Request</span>";
					break;
					
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_REVIEW'))}":
					display = "<span class=\"iconSt review\">Review</span>";
					break;
					
				case "${ct:getCodeString(ct:getConstDef('CD_PROJECT_STATUS'), ct:getConstDef('CD_DTL_PROJECT_STATUS_COMPLETE'))}":
					display = "<span class=\"iconSt complete\">"+cellvalue+"</span>";
					break;
			}
			
			return display;
		},
		// Grid Stage cell display
		displayStage : function(cellvalue, options, rowObject){
			var display = "";
			
			switch(cellvalue){
				case "B":
					display = "<span class=\"btnIcon basic\" style='cursor:default;'>Basic Info</span>";
					break;
				case "I":
					display = "<span class=\"btnIcon identi\" style='cursor:default;'>Identification</span>";
					break;
				case "P":
					display = "<span class=\"btnIcon packag\" style='cursor:default;'>Packaging</span>";
					break;
				case "D":
					display = "<span class=\"btnIcon distr\" style='cursor:default;'>Distribution</span>";
					break;
			}
			
			return display;
		},
		// Grid status cell attr
		cellattrStatus : function(cellvalue, options, rowObject){
			// 프로젝트 상태가 Delay 경우 상태컬럼 노란색 배경으로 설정
			var styleStr = "";
			var priority = (rowObject["priority"]||"").toUpperCase();
			
			switch(priority){
				case "P0":	styleStr = 'style="background-color:#FEAEC9"';	break; // pink
				case "P1":	styleStr = 'style="background-color:#FFFCB7"';	break; // light yellow
				default:	break; 
			}

			return styleStr;
		}
	}
	
	//데이터 객체
	var gridTooltip = {
		typeCodes : [],
		tooltipCont : "<div class=\"tooltipData\">"
	                   +"<dl><dt><span class=\"iconSt draft\">Progress</span>Progress</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt request\">Request</span>Request</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt review\">Review</span>Review</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt complete\">Complete</span>Complete</dt></dl><br>"
	                   +"<dl><dt><span class=\"iconSt delay\">Delay</span>Delay</dt></dl><br>"
	                   +"</div>",
	    tooltipCont1 : "<div class=\"tooltipData\">"
		               +"<dl><dt><span class=\"downSet btnReport\">FOSSLight Report</span>FOSSLight Report</dt></dl><br>"
		               +"<dl><dt><span class=\"downSet btnNotice\">OSS Notice</span>OSS Notice</dt></dl><br>"
		               +"<dl><dt><span class=\"downSet btnPackage\">Packaging File</span>Packaging File</dt></dl><br>"
		               +"</div>",
		tooltipCont2 : "<div class=\"tooltipData350\">"
					   +"<dl><dt><span class=\"iconSet multi\">Multi License</span>Multi License</dt>" +
                            "<dd><spring:message code='msg.oss.include.license' /></dd>"+
                            "<dd>(e.g. \lib is LGPL-2.1 <span style=\"text-decoration : underline;\">and</span> \src is GPL-2.0)</dd></dl>"
					   +"<dl><dt><span class=\"iconSet dual\">Dual License</span>Dual License</dt>" +
                            "<dd><spring:message code='msg.oss.select.license' /></dd>"+
                            "<dd>(e.g. GPL-2.0 <span style=\"text-decoration : underline;\">or</span> MIT)</dd></dl>"
					   +"<dl><dt><span class=\"iconSet vdif\">Version Different License</span>Version Different License</dt><dd>The OSS is distributed under <span style=\"text-decoration : underline;\">different licenses</span> according to its <span style=\"text-decoration : underline;\">versions</span>.</dd><dd>본 OSS는 Version에 따라 다른 License로 배포되고 있습니다.</dd><dd>(e.g. v1.0 is under GPL-2.0 and v2.0 is under BSD-3-Clause)</dd></dl>"
					   +"</div>",
		existTooltip : false,
		init : function(){
			jobsList.load();	// Grid Load
			ossList.load();		// Grid Load
		}	
	};
	
	reLode = function(gridId){
		$.ajax({
			url : '/dashboard/' + gridId + 'Ajax',
			dataType : 'json',
			cache : false,
			data : {page:1, rows:10},
			contentType : 'application/json',
			success : function(data){
				$('#' + gridId).jqGrid('clearGridData');
				$('#' + gridId).jqGrid('setGridParam',{data:data.rows}).trigger('reloadGrid');
			},
			error : function(){
			}
		});
	}

	var fn_sTblResizer = function(){
		if($('.jqGridSet2 table').length > 0){
			var width = 0;
			$('.jqGridSet2').each(function(){
				if($(this).width() != 0){
					width = $(this).width();
				}
				// 그리드의 width 초기화
				$(this).find('table').jqGrid('setGridWidth', 0, true);
				// 그리드의 width를 div 에 맞춰서 적용
				$(this).find('table').jqGrid('setGridWidth', width, true);
			})
		}
	};
	
	displayComment = function(cellvalue){ // Comment 태그 치환 
		var display = "";
		if(cellvalue !=""){
			var tmpStr = new RegExp();
			tmpStr = /[<][^>]*[>]/gi;
			display = cellvalue.replace(tmpStr , "");
		}
		return display;
	}
	
	//2018-08-17 choye 추가
	function readSubmit(){
	    $.ajax({
			url : '/dashboard/readConfirmAll',
			type : 'POST',
			dataType : 'json',
			cache : false,
			success : onReadSuccess,
			error : onError
		});
	};
	
	function onReadSuccess(json, status){
		loading.hide();
		
		if(json.resCd == '10'){
			alertify.alert('<spring:message code="msg.common.success" />', function(){
				reLode('commentsList');
			}); 
		}else{
			alertify.error('<spring:message code="msg.common.valid2" />', 0);
		}
	};
	
	function onError(data, status){
		alertify.error('<spring:message code="msg.common.valid2" />', 0);
    };
</script>