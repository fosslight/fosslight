<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
	var lastsel;
	
	$(document).ready(function () {
		'use strict';
		sec_com_evt.dataInit();
		com_fn.tabInit();
		
		initSample();
		showHelpLink("Project_List_Security");
		
		$('.btnCommentHistory').on('click', function(e){
			e.preventDefault();
			openCommentHistory('<c:url value="/comment/popup/security/${project.prjId}"/>');
		});
	});
	
	var total = {
		loadSecurityGrid : function(){
			var totalList = $("#totalList");
			totalList.jqGrid({
				datatype: 'local',
				data: totalMainData,
				colNames: ['ID','OSS ID','OSS Name','OSS<br/>Version','CVE ID','CVSS<br/>SCORE','CPE','Published<br/>Date','Vulnerability<br/>Resolution'
							,'Vulnerabilty<br/>Link','Official CVE<br/>Security Patch Link','Security Patch Link<br/>for Internal','Affected SW<br/>Version Range','Security<br/>Comments','Activate Flag'],
				colModel: [
					{name: 'gridId', index: 'gridId', editable:false, hidden:true},
					{name: 'ossId', index: 'ossId', width: 150, align: 'left', hidden:true},
					{name: 'ossName', index: 'ossName', width: 150, align: 'left', editable:false},
					{name: 'ossVersion', index: 'ossVersion', width: 50, align: 'left', editable:false},
					{name: 'cveId', index: 'cveId', width: 70, align: 'left', editable:false},
					{name: 'cvssScore', index: 'cvssScore', width: 70, align: 'left', editable:false},
					{name: 'cpeName', index: 'cpeName', width: 70, align: 'left', formatter: sec_com_fn.displayLineBreakData, editable:false},
					{name: 'publDate', index: 'publDate', width: 70, align: 'left', editable:false},
					{name: 'vulnerabilityResolution', index:'vulnerabilityResolution', width:110, formatter: 'select', editable:true, edittype:"select", editoptions:{
							value:{"Unresolved":"Unresolved",
								"Fixed":"Fixed",
								"Will be fixed (MR)":"Will be fixed (MR)",
								"Will be fixed (External Dependency)":"Will be fixed (External Dependency)",
								"Deferred (Not Available)":"Deferred (Not Available)",
								"Won't fix (Mitigation)":"Won't fix (Mitigation)",
								"Won't fix (Drop)":"Won't fix (Drop)"
							}
							, dataEvents:[{type:'change',
								fn: function(e){
									$("#totalList").jqGrid('saveRow', lastsel);
									$("#totalList").jqGrid("resetSelection");
									lastsel = -1;
								}
							}]
						}
					},
					{name: 'vulnerabilityLink', index: 'vulnerabilityLink', width: 120, align: 'left', formatter: sec_com_fn.displayVulnerabilityLink, editable:false},
					{name: 'officialPatchLink', index: 'officailPatchLink', width: 120, align: 'left', formatter: sec_com_fn.displayVulnerabilityLink, editable:false},
					{name: 'securityPatchLink', index: 'securityPatchLink', width: 120, align: 'left', formatter: sec_com_fn.displayVulnerabilityLink, editable:true},
					{name: 'verStartEndRange', index: 'verStartEndRange', width: 120, align: 'left', formatter: sec_com_fn.displayLineBreakData, editable:false},
					{name: 'securityComments', index: 'securityComments', width: 50, align: 'left', editable:true},
					{name: 'activateFlag', index: 'activateFlag', editable:false, hidden:true}
				],
			   	editurl:'clientArray',
	 			autowidth: true,
	 			cellEdit: true,
				pager: '#totalListPager',
				rowNum: 200,
				rowList: [200, 500, 1000, 5000],
				gridview: true,
				sortable: function (permutation) {
				},
				viewrecords: true,
				loadonce:true,
				sortorder: 'desc',
				height: 'auto',
				onCellSelect: function(rowid,iCol,cellcontent,e) {
					sec_com_fn.customOnCellSelect("totalList", iCol);
				},
				beforeSelectRow: function(rowid, e) {
					if (rowid && rowid !== lastsel){
						$("#totalList").jqGrid('saveRow', lastsel);
						$("#totalList").jqGrid("resetSelection");
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					sec_com_fn.customOnDblClickRow("totalList" ,rowid);
				},
				loadComplete: function() {
					lastsel = -1;
					tableRefresh();
				}
			});
			
			totalList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn",
				beforeSearch : function () {
					if(!this.p.postData.referenceId) {
						this.p.postData.referenceId = '${project.prjId}';
					}
				}	
			});
			
			totalList.jqGrid('navGrid',"#totalListPager",{add:false,edit:false,del:true,search:false,refresh:false, cloneToTop:true});
		},
		save : function(){
			alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
				if (e) {
					fn_grid_com.totalGridSaveMode('totalList');
					total.exeSave();
				} else {
					return false;
				}
			});
		},
		exeSave : function() {
			var prjId = '${project.prjId}';
			var totalList = $("#totalList").jqGrid('getGridParam','data');
			
			var finalData = {
				referenceId : prjId,
				targetName : "TOTAL",
				gridData : JSON.stringify(totalList)
			};
			
			$.ajax({
				url : '<c:url value="/project/saveSecurity"/>',
				type : 'POST',
				data : JSON.stringify(finalData),
				dataType : 'json',
				cache : false,
				async : false,
				contentType : 'application/json',
				success: function(data){
					if ("true" == data.isValid){
						alertify.success('<spring:message code="msg.common.success" />');
						saveFlag = true;
						sec_com_evt.dataInit();
					} else {
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				}
			});
		},
		reset : function(){
			alertify.confirm('<spring:message code="msg.common.confirm.reset" />', function (e) {
				if (e) {
					$("#totalList").jqGrid('clearGridData');
				} else {
					return false;
				}
			});
		},
		downloadExcel : function(){
			var data = {
				"prjId" : "${project.prjId}",
				"code" : "total"
			}
			
			$.ajax({
				type: "POST",
				url: '<c:url value="/exceldownload/getExcelPost"/>',
				data: JSON.stringify({"type":"security", "parameter":JSON.stringify(data)}),
				dataType : 'json',
				cache : false,
				contentType : 'application/json',
				success: function (data) {
					if("false" == data.isValid) {
						if(data.validMsg == "overflow"){
							alertify.error(getMsgMaxRowCnt(), 0);
						} else {
			               alertify.error('<spring:message code="msg.common.valid2" />', 0);
						}
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
	
	var com_fn = {
		tabInit : function(){
			var prjName = "${project.prjName}"+(('${project.prjVersion}'!="")?' ('+'${project.prjVersion}'+')':"");
			var createdDate = "${ct:formatDateSimple(project.createdDate)}";
			var vCreated = '${project.prjUserName}'+" "+'${project.prjDivisionName }'+' ('+createdDate+')';
			
			$("#prjName").text(prjName);
			$("#creator").text(vCreated);
			
			var initDiv = '${initDiv}';
			var tabContent = $(".tabContent");
			var tabMenuA = $(".tabMenu a");
			
			tabContent.hide();
			tabMenuA.click(function () {
				com_fn.fnTabChange(this);
			});
			
			tabMenuA.eq("0").click();
		},
		fnTabChange: function(target){
			var tabContent = $(".tabContent");
			var tabMenuA = $(".tabMenu a");
			
			$(".tabMenu a span").remove();
			
			tabMenuA.eq("0").text("Total");
			tabMenuA.eq("1").text("Fixed");
			tabMenuA.eq("2").text("Not Fixed");
			
			var tag = "<span>"+$(target).text()+"</span>";
			
			$(target).html(tag);
			
			tabContent.hide();
			activeTabText = $(target).text();
			activeTab = $(target).attr("rel");
			$("#" + activeTab).show();
		}
	}
</script>