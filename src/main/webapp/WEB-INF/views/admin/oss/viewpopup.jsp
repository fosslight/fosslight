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
			var _ossName = '${fn:escapeXml(ossInfo.ossName)}';
			_ossName = _ossName.split("&#034;").join("\"").split("&#039;").join("\'");
			var ossList = [];

			$.ajax({
				url : '/oss/getOssListByName',
				dataType : 'json',
				cache : false,
				data : {ossName : _ossName},
				contentType : 'application/json',
				success : function(data){
					$('#_ossList').jqGrid({
						datatype: 'local',
						data : data.ossList,
						jsonReader:{
							repeatitems: false,
							id: 'ossId',
						},
						colNames: ['ID','OSS Name (version)','OSS Version', 'Declared License'],
						colModel: [
							{name: 'ossId', index: 'ossId', key:true, hidden:true},
							{name: 'ossNameVerStr', index: 'ossNameVerStr', align: 'left'},
							{name: 'ossVersion', index: 'ossVersion', width: 100, align: 'left', hidden:true},
							{name: 'licenseName', index: 'licenseName', width: 300, align: 'left'}
						],
						onSelectRow: function(id){
							$.ajax({
								url : '/oss/ossDetailViewAjax',
								dataType : 'html',
								cache : false,
								data : {ossId : id},
								success : function(detailResult){
									$("#ossDetailInfo").html(detailResult);
								},
								error : function(request,status,error){
									alertify.error('<spring:message code="msg.common.valid2" />', 0);
								}
							});
						},
						autowidth: true,
						gridview: true,
						viewrecords: true,
						loadonce: true,
						height: 'auto',
						rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
						sortname: 'ossVersion',
						sortorder: 'desc',
						loadComplete: function(data){
							var isSelectedRow = false;
							
							if(data.records > 0) {
								var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row, rowid, rowData, className;
								
								for(var _idx=0;_idx<rowsCount;_idx++) {
									row = rows[_idx];
									className = row.className;
									
									if (className.indexOf('jqgrow') !== -1) {
										rowid = row.id;
										rowData = data.rows[rowIdx++];
										
										if(rowData.ossVersion == '${ossInfo.ossVersion}') {
											$('#_ossList').jqGrid("setSelection", rowid);
											$("#_ossList #" +rowid).focus();

											isSelectedRow = true;

											break;
										}
									}
								}
								
								if(!isSelectedRow) {
									for(var _idx=0;_idx<rowsCount;_idx++) {
										row = rows[_idx];
										className = row.className;

										if (className.indexOf('jqgrow') !== -1) {
											rowid = row.id;
											rowData = data.rows[rowIdx++];
											$('#_ossList').jqGrid("setSelection", rowid);

											break;
										}
									}
								}
							}
						}
					});
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
			<div class="jqGridSet" style="overflow: auto; width: 90%; height: 150px;">
				<table id="_ossList"><tr><td></td></tr></table>
			</div>
			</div>
			<div id="ossDetailInfo" style="padding: 10%;">
			</div>
		</div>
	</body>
</html>