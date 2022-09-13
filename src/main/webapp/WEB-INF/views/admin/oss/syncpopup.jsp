<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- Administrator screen template. --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript" src="<c:url value="/js/ckeditor/ckeditor.js?${jsVersion}"/>"></script>
		<script type="text/javascript">
		$(document).ready(function() {
			var syncRefOssId = '${fn:escapeXml(ossInfo.ossId)}';
			$("input[name=syncRefOssId]").val(syncRefOssId);
			
			var _ossName = '${fn:escapeXml(ossInfo.ossName)}';
			_ossName = _ossName.split("&#034;").join("\"").split("&#039;").join("\'");
			
			$.ajax({
				url : '<c:url value="/oss/getOssListByName"/>',
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
							{name: 'ossNameVerStr', index: 'ossNameVerStr', align: 'left', sortable: false},
							{name: 'ossVersion', index: 'ossVersion', width: 100, align: 'left', hidden:true},
							{name: 'licenseName', index: 'licenseName', width: 300, align: 'left', sortable: false}
						],
						onSelectRow: function(rowid, status, e){
							if (status && $("input[name=initCheck]").val() > 0) {
								$.ajax({
									url : '<c:url value="/oss/ossSyncDetailViewAjax"/>',
									dataType : 'html',
									cache : false,
									data : {ossId : rowid, syncRefOssId : syncRefOssId},
									success : function(detailResult){
										$("#ossDetailInfo").html(detailResult);
									},
									error : function(){
										alertify.error('<spring:message code="msg.common.valid2" />', 0);
									}
								});
							}
						},
						onSelectAll: function(aRowids, status){
							var datas = data.rows, rows=this.rows, className, rowsCount=rows.length;
							for(var i=0; i<rowsCount; i++){
								if (rows[i].className.indexOf("excludeRow") !== -1){
									rows[i].className = rows[i].className.replace("ui-state-highlight", "");
									$(rows[i]).find("input[type=checkbox]").prop("checked", false);
								}
							}
						},
						autowidth: true,
						gridview: true,
						viewrecords: true,
						loadonce: true,
						height: '350px',
						scroll: true,
						multiselect: true,
						rowNum:${ct:getConstDef("DISP_PAGENATION_MAX")},
						sortname: 'ossVersion',
						sortorder: 'desc',
						loadComplete: function(data){
							var standardVersion = '${ossInfo.ossVersion}';
							var ossVersionCheckRowId = "";
							
							if(data.records > 0) {
								var rowIdx = 0, rows = this.rows, rowsCount = rows.length, row, rowid, rowData, className;
								
								for(var _idx=0;_idx<rowsCount;_idx++) {
									row = rows[_idx];
									className = row.className;
									
									if (className.indexOf('jqgrow') !== -1) {
										rowid = row.id;
										rowData = data.rows[rowIdx++];

										if(rowData.ossVersion == standardVersion) {
											ossVersionCheckRowId = rowid;
										}
									}
								}

								var ossIdArr = $("#_ossList").jqGrid('getDataIDs');
								var ossId = $("input[name=syncRefOssId]").val();
								$("input[name=ossIds]").val(ossIdArr);
								var ossIds = $("input[name=ossIds]").val();
								
								$.ajax({
									url : '<c:url value="/oss/ossSyncListValidation"/>',
									type : 'POST',
									dataType : 'json',
									data : {"ossId" : ossId, "ossIds" : ossIds},
									cache : false,
									success : function(data){
										var result = data.resultData;

										var differenceList = ossIdArr.filter(function(element){
											if (result.indexOf(element) === -1){
												return element;
											}
										});
										
										var sameList = ossIdArr.filter(function(element){
											if (result.indexOf(element) !== -1){
												return element;
											}
										});
										
										if (sameList.length > 0){
											for(var _idx=0;_idx<rowsCount;_idx++) {
												row = rows[_idx];
												className = row.className;
												if (className.indexOf('jqgrow') !== -1){
													id = row.id;
													for (var i=0; i<sameList.length; i++) {
														if (id == sameList[i]){
															row.className = className + ' excludeRow';
															$(row).attr("onclick", "event.cancelBubble=true;").find("input[type=checkbox]").attr("disabled", true);
														}
													}
												}
											}
										}
										
										if (differenceList.length > 0){
											for (var i=0; i<differenceList.length; i++) {
												if (i==0){
													$("input[name=initCheck]").val(9);
													$('#_ossList').jqGrid("setSelection", differenceList[i]);
													$("input[name=initCheck]").val(0);
												}else{
													$('#_ossList').jqGrid("setSelection", differenceList[i]);
												}
											}
										}else{
											$("input[name=initCheck]").val(9);
											$('#_ossList').jqGrid("setSelection", ossVersionCheckRowId);
											$('#_ossList').jqGrid("setSelection", ossVersionCheckRowId, false);
										}

										$("input[name=initCheck]").val(9);
									},
									error : function(){
										alertify.error('<spring:message code="msg.common.valid2" />', 0);
									}
								});
							}
						}
					});
				},
				error : function(){
					alertify.error('<spring:message code="msg.common.valid2" />', 0);
				}
			});
		});

		var fn = {
			syncSave : function(){
				var unCheckedList = [];
				var excludeList = $("#_ossList").find(".excludeRow");
				for(var i=0; i<excludeList.length; i++){
					unCheckedList.push(excludeList[i].id);
				}
				
				var checkedOssList = $("#_ossList").jqGrid("getGridParam", "selarrrow").filter(function(element){
					if (unCheckedList.indexOf(element) === -1){
						return element;
					}
				});
				
				var checkedSyncList = $(".dCase tbody tr").find("input[type=checkbox]:checked");
				
				if (checkedOssList.length > 0) { 
					var dataLength = CKEDITOR.instances['syncPopupEditor'].getData().length;
					
					if ((checkedSyncList.length > 0) || (dataLength > 0 && checkedSyncList.length == 0)) {
						$(".loading").show();
						
						$("input[name=ossIds]").val(checkedOssList);
						var editorVal = CKEDITOR.instances['syncPopupEditor'].getData();
						var syncRefOssId = $("input[name=syncRefOssId]").val();
						var ossIds = $("input[name=ossIds]").val();
						var items = []; 
						checkedSyncList.each(function() {
							items.push($(this).attr("id"));
						});

						$("input[name=ossIds]").val(items);
						var syncItem = $("input[name=ossIds]").val();
						
						$.ajax({
							url : '<c:url value="/oss/ossSyncUpdate"/>',
							type : 'POST',
							data : {"ossId" : syncRefOssId, "ossIds" : ossIds, "comment" : editorVal, "syncItem" : syncItem},
							dataType : 'json',
							cache : false,
							success: function(data){
								$(".loading").hide();
								if(data.isValid == 'true') {
									alertify.alert('<spring:message code="msg.common.success" />',function(){
										self.close();
										window.opener.deleteTabInFrame('#<c:url value="/oss/edit/'+syncRefOssId+'"/>');
										window.opener.reloadTabInframe('<c:url value="/oss/list"/>');
									});
								}else{
									alertify.error('<spring:message code="msg.common.valid2" />', 0);
									self.close();
								}
							},
							error : function(){
								alertify.error('<spring:message code="msg.common.valid2" />', 0);
							}
						});
					}else{
						alertify.alert('<spring:message code="msg.oss.select.synchronize" />', function(){});
					}
				} else{
					alertify.alert('<spring:message code="msg.oss.select.project" />', function(){});
				}
			},
			syncClose : function(){
				self.close();
			}
		};
		</script>
	</head>
	<body>
		<div id="wrap" style="padding-top: 10px;">
			<div id="loading_wrap_popup" class="loading" style="display:none;">
				<div class="loadingBlind"></div>
				<img src="${ctxPath}/images/loading.gif" alt="loading" />
			</div>
			<div class="statisticsLayout">
				<input type="hidden" name="syncRefOssId"/>
				<input type="hidden" name="ossIds"/>
				<input type="hidden" name="initCheck" value=0/>
				<div class="grdSetSyncPopup">
					<div class="grdItemSyncPopup first">
						<table id="_ossList"><tr><td></td></tr></table>
					</div>
					<div class="grdItemSyncPopup">
						<span style="text-align:center;"><h2>Synchronize OSS Attributes</h2></span>
						<div id="ossDetailInfo" style="margin-top: 5px;"></div>
					</div>
				</div>
			</div>
			<div class="syncPopupEditor">
				<div id="syncPopupEditor"></div>
				<script type="text/javascript">
            		CKEDITOR.replace('syncPopupEditor', {height: 150, basicEntities: false, fillEmptyBlocks: false});
            	</script>
			</div>
			<div class="btnLayout">
				<span class="btnLayoutSyncPopupBtn">
	            	<input type="button" value="Cancel" class="btnCancel btnColor" onclick="fn.syncClose();"/>
	            	<input type="button" value="OK" class="btnColor red" onclick="fn.syncSave();"/>
	            </span>
	        </div>
		</div>
	</body>
</html>