<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
		$(document).ready(function() {
			var binaryList = [];
			
			$.ajax({
				url : '<c:url value="/system/bat/listAjax"/>',
				dataType : 'json',
				type : 'GET',
				cache : false,
				data : {filename : "${batInfo.filename}", sidx : "fileName", sord : "asc", equalFlag : "Y", binaryPopupFlag : "Y"},
				contentType : 'application/json',
				success : function(data){
					var binList = $('#_binaryList');
					
					binList.jqGrid({
							datatype: 'local',
						    data : data.rows,
							jsonReader:{
								repeatitems: false,
								id:'batId',
								root:function(obj){return obj.rows;},
								page:function(obj){return obj.page;},
								total:function(obj){return obj.total;},
								records:function(obj){return obj.records;}
							},
							colNames: ['batId','Binary File name','Path', 'OSS Name', 'OSS Version', 'License', 'Download Location', 'Project Name', 'Platform Name', 'Platform Version', 'Checksum', 'Tlsh', 'Update Date'],
							colModel: [
								{name: 'batId', index: 'batId', hidden:true, key:true},
								{name: 'filename', index: 'filename', width: 150, allign: 'left',editable: true, template: searchStringOptions},
								{name: 'pathname', index: 'pathname', width: 150, allign: 'left',editable: true, template: searchStringOptions},
								{name: 'ossname', index: 'ossname', width: 150, allign: 'left',editable: true, template: searchStringOptions} ,
								{name: 'ossversion', index: 'ossversion', width: 80, align: 'left',editable: true, template: searchStringOptions},
								{name: 'license', index: 'license', width: 150, align: 'left',editable: true, template: searchStringOptions},
								{name: 'downloadlocation', index: 'downloadlocation', width: 100, align: 'left', formatter: 'link', formatoptions: {target:'_blank'}, editable: true, template: searchStringOptions},
								{name: 'parentname', index: 'parentname', width: 100, align: 'left',editable: true, template: searchStringOptions},
								{name: 'platformname', index: 'platformname', width: 100, align: 'left',editable: true, template: searchStringOptions},
								{name: 'platformversion', index: 'platformversion', width: 100, align: 'left',editable: true, template: searchStringOptions},
								{name: 'checksum', index: 'checksum', width: 150, allign: 'center',editable: true,hidden:true, template: searchStringOptions},
								{name: 'tlshchecksum', index: 'tlshchecksum', width: 150, allign: 'center',editable: true,hidden:true, template: searchStringOptions},
								{name: 'updatedate', index: 'updatedate', width: 100, align: 'left',editable: false, template: searchStringOptions}
							]
							, editurl :'clientArray'
							, rowNum: ${ct:getConstDef("DISP_PAGENATION_DEFAULT")}
							, rowList: [${ct:getConstDef("DISP_PAGENATION_LIST_STR")}]
							, autowidth: true
							, pager: '#pager'
							, gridview: true
							, sortable: function (permutation) {
							}
							, sortname: 'fileName'
							, viewrecords: true
							, sortorder: 'asc'
							, loadonce: false
							, height: 'auto'
							, onSelectRow: function(rowid,iRow,iCol,e){
									// 마지막 수정된 row 저장
								if(batList.lastEditRowId && rowid != batList.lastEditRowId){
									fn.saveLastRow();
								}
								
								batList.lastEditRowId = rowid;
							}
							, loadComplete:function(data) {}
							, ondblClickRow: function(rowid,iRow,iCol,e) {}
					});
					
					binList.jqGrid('filterToolbar',{stringResult: true, searchOnEnter: true, searchOperators: true, defaultSearch: "cn"});
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
			
		});
		
		</script>
	</head>
	<body>
		<div id="wrap" style="padding-top: 10px;">
			<div  align="center" >
			<div class="jqGridSet" style="overflow: auto; width: 95%; height: 600px;">
				<table id="_binaryList"><tr><td></td></tr></table>
				<div id="pager"></div>
			</div>
			</div>
		</div>
	</body>
</html>