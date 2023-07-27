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
									$("#totalList").jqGrid('saveRow', lastsel);
									$("#totalList").jqGrid("resetSelection");
									lastsel = -1;
								}
							}]
						}
					}
				],
			   	editurl:'clientArray',
	 			autowidth: true,
	 			multiselect: true,
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
					if (rowid == prevEditSel) {
						$("#jqg_totalList_" + rowid).prop("checked", true);
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
					var ossVersion = totalList.jqGrid("getRowData", rowid)["ossVersion"];
				    if ("" == ossVersion) $("#jqg_totalList_" + rowid).prop("checked", false);
				},
				ondblClickRow: function(rowid,iRow,iCol,e) {
					sec_com_fn.customOnDblClickRow("totalList" ,rowid);
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
					var gridData = sec_com_fn.setSaveGridData('totalList');
					total.exeSave(gridData);
				} else {
					return false;
				}
			});
		},
		exeSave : function(data) {
			var finalData = {
				referenceId : '${project.prjId}',
				targetName : "TOTAL",
				gridData : JSON.stringify(data)
			};
			
			$.ajax({
				url : '<c:url value="/project/saveSecurity"/>',
				type : 'POST',
				data : JSON.stringify(finalData),
				dataType : 'json',
				cache : false,
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