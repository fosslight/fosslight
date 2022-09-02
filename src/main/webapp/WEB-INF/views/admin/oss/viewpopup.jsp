<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- Administrator screen template. --%>
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
							{name: 'ossNameVerStr', index: 'ossNameVerStr', align: 'left'},
							{name: 'ossVersion', index: 'ossVersion', width: 100, align: 'left', hidden:true},
							{name: 'licenseName', index: 'licenseName', width: 300, align: 'left'}
						],
						onSelectRow: function(id){
							$.ajax({
								url : '<c:url value="/oss/ossDetailViewAjax"/>',
								dataType : 'html',
								cache : false,
								data : {ossId : id},
								success : function(detailResult){
									$("#ossDetailInfo").html(detailResult);
									$("#viewPopupOssId").val(id);
									fn_commemt.getCommentList();
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
								if(data.records < 11) {
									$(".jqGridSet").height(30*data.records + 60);
								} else {
									$(".jqGridSet").height(360);
								}

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

		var fn_commemt = {
			    getCommentList : function(){
			        $.ajax({
			            url : '/comment/getCommentList',
			            type : 'GET',
			            dataType : 'html',
			            cache : false,
			            data : {
			                referenceId : $('input[name=viewPopupOssId]').val(),
			                referenceDiv : 'oss'
			            },
			            success : function(data){
			                $('#viewPopupCommentListArea').html(data);
			                $('#viewPopupCommentListArea').css('height', 'auto');
			            },
			            error : function(xhr, ajaxOptions, thrownError){
			                alertify.error('<spring:message code="msg.common.valid2" />', 0);
			            }
			        });
			    },
			    deleteComment : function(_commId){
			        if(!confirm('<spring:message code="msg.oss.confirm.delete.comment" />')) return;
			        $.ajax({
			            url : '/comment/deleteComment',
			            type : 'POST',
			            dataType : 'json',
			            cache : false,
			            data : {'commId' : _commId},
			            success : function(data){
			                alertify.success('<spring:message code="msg.common.success" />');
			                fn_commemt.getCommentList();
			            },
			            error : function(){
			                alertify.error('<spring:message code="msg.common.valid2" />', 0);
			            }
			        });
			    },
			    editComment : function(_commId){
			        if(CKEDITOR.instances['comm_editor_'+_commId]) {
			            var _editor = CKEDITOR.instances['comm_editor_'+_commId];
			            _editor.destroy();
			        }
			        _editor = CKEDITOR.replace('comm_editor_'+_commId);

			        $("#spanBtnArea_"+_commId+" > .btnViewMode").hide();
			        $("#spanBtnArea_"+_commId+" > .btnEditMode").show();
			        
			        $("#spanBtnArea_"+_commId+" > .closeModComment").click(function(e){
			            e.preventDefault();
			            fn_commemt.createNonToolbarEditor(_commId);
			            $("#spanBtnArea_"+_commId+" > .btnViewMode").show();
			            $("#spanBtnArea_"+_commId+" > .btnEditMode").hide();
			        });
			        
			        $("#spanBtnArea_"+_commId+" > .modifyComment").click(function(e){
			            e.preventDefault();
			            var _referenceId = $('input[name=viewPopupOssId]').val();
			            var param = {commId : _commId, contents : _editor.getData(), referenceDiv: '40', referenceId: _referenceId};
			            $.ajax({
			                url : '/comment/updateComment',
			                type : 'POST',
			                dataType : 'json',
			                cache : false,
			                data : param,
			                success : function(json){
			                    fn_commemt.createNonToolbarEditor(_commId);
			                    $("#spanBtnArea_"+_commId+" > .btnViewMode").show();
			                    $("#spanBtnArea_"+_commId+" > .btnEditMode").hide();
			                    alertify.success('<spring:message code="msg.common.success" />');
			                },
			                error : function(){
			                    alertify.error('<spring:message code="msg.common.valid2" />', 0);
			                }
			            });
			            
			            return false;
			        });
			        
			    },
			    createNonToolbarEditor : function(_commId) {

			        var _editor = CKEDITOR.instances['comm_editor_'+_commId];
			        if(_editor) {
			            _editor.destroy();
			        }
			        if($('#comm_editor_'+_commId).html().length > 0) {
			            CKEDITOR.replace('comm_editor_'+_commId, {customConfig:'/js/customEditorConf_Comment.js'});
			        }
			    }
			}
		</script>
	</head>
	<body>
		<div id="wrap" style="padding-top: 10px;">
			<div  align="center" >
			<div class="jqGridSet" style="overflow: auto; width: 850px; height: 150px;">
				<table id="_ossList"><tr><td></td></tr></table>
			</div>
			</div>
			<div id="ossDetailInfo" style="padding: 3% 6% 6% 6%;">
			</div>
		<c:if test="${ct:isAdmin()}">
			<input type="hidden" id="viewPopupOssId" name="viewPopupOssId"/>
        	<div align="center">
           		<div class="commentList" align="left" style="overflow: auto; width: 90%; height: 200px; margin-bottom:0px;">
              		<strong class="tit">Comments</strong>
               		<div class="commentBack" id="viewPopupCommentListArea"></div>
           		</div>
        	</div>
		</c:if>
		</div>
	</body>
</html>