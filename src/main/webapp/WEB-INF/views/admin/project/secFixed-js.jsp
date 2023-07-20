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
				colNames: ['ID','OSS ID','OSS Name','OSS<br/>Version','CVE ID','CVSS<br/>SCORE','Published<br/>Date','Vulnerability<br/>Resolution'],
				colModel: [
					{name: 'gridId', index: 'gridId', editable:false, hidden:true},
					{name: 'ossId', index: 'ossId', width: 150, align: 'left', hidden:true},
					{name: 'ossName', index: 'ossName', width: 150, align: 'left', editable:false},
					{name: 'ossVersion', index: 'ossVersion', width: 50, align: 'left', editable:false},
					{name: 'cveId', index: 'cveId', width: 70, align: 'left', editable:false},
					{name: 'cvssScore', index: 'cvssScore', width: 70, align: 'left', editable:false},
					{name: 'publDate', index: 'publDate', width: 70, align: 'left', editable:false},
					{name: 'vulnerabilityResolution', index:'vulnerabilityResolution', width:110, formatter: 'select', editable:true, edittype:"select", editoptions:{
							value:{"Unresolved":"Unresolved","Fixed":"Fixed"}
							, dataEvents:[{type:'change',
								fn: function(e){
									$("#fixedList").jqGrid('saveRow', lastsel);
									$("#fixedList").jqGrid("resetSelection");
									lastsel = -1;
								}
							}]
						}
					}
				],
				editurl:'clientArray',
	 			autowidth: true,
	 			multiselect: true,
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
					if (rowid == prevEditSel) {
						$("#jqg_fixedList_" + rowid).prop("checked", true);
					}
					
					var $self = $(this), iCol, cm,
				    $td = $(e.target).closest("tr.jqgrow>td"),
				    $tr = $td.closest("tr.jqgrow"),
				    p = $self.jqGrid("getGridParam");

				    if ($(e.target).is("input[type=checkbox]") && $td.length > 0) {
				       iCol = $.jgrid.getCellIndex($td[0]);
				       cm = p.colModel[iCol];
				       
				       if (cm != null && cm.name === "cb") {
				           // multiselect checkbox is clicked
				           $self.jqGrid("setSelection", $tr.attr("id"), true ,e);
				       }
				    }
				    
				    return true;
				},
				onSelectRow: function(rowid, status, e) {
					var ossVersion = fixedList.jqGrid("getRowData", rowid)["ossVersion"];
				    if ("" == ossVersion) $("#jqg_fixedList_" + rowid).prop("checked", false);
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					sec_com_fn.customOnDblClickRow("fixedList" ,rowid);
				},
				loadComplete: function(data) {
					lastsel = -1;
					if(data.records > 0) {
						var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row;
						for(var _idx=0;_idx<rowsCount;_idx++) {
							row = rows[_idx];
							$("#"+row.id).find("input[type=checkbox]").removeClass("cbox");
						}
					}
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
			var fixedGridData = $("#fixedList").jqGrid('getGridParam','data');
			
			var finalData = {
				referenceId : prjId,
				targetName : "FIXED",
				gridData : JSON.stringify(fixedGridData)
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