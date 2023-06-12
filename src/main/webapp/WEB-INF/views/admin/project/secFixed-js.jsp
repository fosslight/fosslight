<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">
	var lastsel;
	var fixed = {
		loadSecurityGrid : function(){
			var fixedList = $("#fixedList");
			fixedList.jqGrid({ 
				datatype: 'local',  
				data: fixedMainData,
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
									$("#fixedList").jqGrid('saveRow', lastsel);
									$("#fixedList").jqGrid("resetSelection");
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
				pager: '#fixedListPager',
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
					sec_com_fn.customOnCellSelect("fixedList", iCol);
				},
				beforeSelectRow: function(rowid, e) {
					if (rowid && rowid !== lastsel){
						$("#fixedList").jqGrid('saveRow', lastsel);
						$("#fixedList").jqGrid("resetSelection");
					}
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					sec_com_fn.customOnDblClickRow("fixedList" ,rowid);
				},
				loadComplete: function() {
					lastsel = -1;
					tableRefresh();
				}
			});
			
			fixedList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn",
				beforeSearch : function () {
					if(!this.p.postData.referenceId) {
						this.p.postData.referenceId = '${project.prjId}';
					}
				}	
			});
			
			fixedList.jqGrid('navGrid',"#fixedListPager",{add:false,edit:false,del:true,search:false,refresh:false, cloneToTop:true});
		},
		save : function(){
			alertify.confirm('<spring:message code="msg.common.confirm.save" />', function (e) {
				if (e) {
					fn_grid_com.totalGridSaveMode('fixedList');
					fixed.exeSave();
				} else {
					return false;
				}
			});
		},
		exeSave : function(){
			var prjId = '${project.prjId}';
			var fixedList = $("#fixedList").jqGrid('getGridParam','data');
			
			var finalData = {
				referenceId : prjId,
				targetName : "FIXED",
				gridData : JSON.stringify(fixedList)
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
					$("#fixedList").jqGrid('clearGridData');
				} else {
					return false;
				}
			});
		},
		downloadExcel : function(){
			var data = {
				"prjId" : "${project.prjId}",
				"code" : "fixed"
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
</script>