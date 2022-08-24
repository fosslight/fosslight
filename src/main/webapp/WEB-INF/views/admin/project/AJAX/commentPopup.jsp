<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<%@ include file="/WEB-INF/constants.jsp"%>

	<script type="text/javascript">
		$(document).ready(function() {
			fn_commemt.getCommentList();
			
			$('#search').on('click',function(e){
				fn_commemt.getCommentList();
			});
			
			$('.callOpener').on('click',function(e){
				try {
					var paramId = "";
					
					if('${basicInfo.referenceDiv}' == 'prj'){
						paramId = '${project.prjId}';
					}else if('${basicInfo.referenceDiv}' == '3rd'){
						paramId = '${partner.partnerId}';
					}
					
					opener.moveTabInFrameByCommentPopup(paramId, $(this).attr('id'));
					
					window.focus();
				} catch (e) {}
			});
		});
		
		var fn_commemt = {
			getCommentList : function(){
				$.ajax({
					url : '<c:url value="/comment/getCommentList"/>',
					type : 'GET',
					dataType : 'html',
					cache : false,
					data : $('#commentSchForm').serialize(),
					success : function(data){
						$('#commentListArea').html(data);
					},
					error : function(xhr, ajaxOptions, thrownError){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			},
			deleteComment : function(_commId){
				if(!confirm("Are you sure you want to delete this comment?")) return;
				$.ajax({
					url : '<c:url value="/comment/deleteComment"/>',
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
			editComment : function(_commId, _referenceDiv, _referenceId){
				if(CKEDITOR.instances['comm_editor_'+_commId]) {
					var _editor = CKEDITOR.instances['comm_editor_'+_commId];

					_editor.destroy();
				}
				
				_editor = CKEDITOR.replace('comm_editor_'+_commId);
				var originComment = CKEDITOR.instances['comm_editor_'+_commId].getData();

				$("#spanBtnArea_"+_commId+" > .btnViewMode").hide();
				$("#spanBtnArea_"+_commId+" > .btnEditMode").show();
				$("#spanBtnArea_"+_commId+" > .closeModComment").off("click").on("click", function(e){
					fn_commemt.createNonToolbarEditor(_commId);
					CKEDITOR.instances['comm_editor_' +_commId].setData(originComment);
					$("#spanBtnArea_"+_commId+" > .btnViewMode").show();
					$("#spanBtnArea_"+_commId+" > .btnEditMode").hide();
				});

				$("#spanBtnArea_"+_commId+" > .modifyComment").off("click").on("click", function(e){
					var param = {commId : _commId, contents : replaceWithLink(_editor.getData()), referenceDiv: _referenceDiv, referenceId: _referenceId};

					$.ajax({
						url : '<c:url value="/comment/updateComment"/>',
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
				var editorVal = _editor.getData();
				
				if(_editor) {
					_editor.destroy();
				}
				
				if($('#comm_editor_'+_commId).html().length > 0) {
					CKEDITOR.replace('comm_editor_'+_commId, 
							{
						customConfig:'<c:url value="/js/customEditorConf_Comment.js"/>'
							});
				}
				CKEDITOR.instances['comm_editor_'+_commId].setData(replaceWithLink(editorVal));
			},
			getMoreCommentList : function(){
				$.ajax({
					url : '<c:url value="/comment/getMoreCommentList"/>',
					type : 'GET',
					dataType : 'html',
					cache : false,
					data : $('#commentSchForm').serialize(),
					success : function(data){
						$('#commentListArea').append(data);
					},
					error : function(){
						alertify.error('<spring:message code="msg.common.valid2" />', 0);
					}
				});
			}
		}
	</script>
		
	</head>
	<body>
		<div id="wrap" style="padding-top: 10px;">
			<c:if test="${basicInfo.referenceDiv eq 'prj' or basicInfo.referenceDiv eq '3rd'}">
	            <div class="projectInfo" style="margin: 10px;width: 93%">
					<c:choose>
					  <c:when test="${basicInfo.referenceDiv eq 'prj'}">
		                <h2>Project Information</h2>
		                <ul>
						    <li class="first"><span>Project Name</span>
						    	<strong><label>[${project.prjId}] ${project.prjName}<c:if test="${not empty project.prjVersion}"> (${project.prjVersion})</c:if></label>
									<span id="basicInfoPrj" class="btnIcon basic callOpener" style="display:inline-block;width:16px;padding:0;">Basic Info</span>
									<span id="identification" class="btnIcon identi callOpener" style="display:inline-block;width:16px;padding:0;">Identification</span>
									<c:if test="${project.identificationStatus eq 'CONF' and project.verificationStatus ne 'NA'}"><span id="packaging" class="btnIcon packag callOpener" style="display:inline-block;width:16px;padding:0;">Packaging</span></c:if>
									<c:if test="${project.verificationStatus eq 'CONF' and project.destributionStatus ne 'NA'}"><span id="distribution" class="btnIcon distr callOpener" style="display:inline-block;width:16px;padding:0;">Distribution</span></c:if>
								</strong>
						    </li>
						</ul>
				      </c:when>
					  <c:when test="${basicInfo.referenceDiv eq '3rd'}">
		                <h2>3rd Party Information</h2>
		                <ul>
						    <li class="first">
						        <span style="width: 35%">3rd Party Name</span>
						        <strong><label>[${partner.partnerId}] ${partner.partnerName}</label><span id="basicInfo3rd" class="iconSet trd callOpener" style="display:inline-block;width:24px;cursor: pointer;">Basic Info</span></strong>
						    </li>
						</ul>
						<ul>
		                    <li class="first">
		                        <span style="width: 35%">3rd Party Software Name</span>
		                        <strong><label>${partner.softwareName}<c:if test="${not empty partner.softwareVersion}"> (${partner.softwareVersion})</c:if></label></strong>
		                    </li>
		                </ul>
		              </c:when>
		            </c:choose>
				</div>
			</c:if>
			<div class="tabContent">
				<div class="orangeBox" style="margin: 10px;">
					<fieldset class="editSearchUp">
						<form id="commentSchForm" onkeydown="javascript:if(event.keyCode==13){fn_commemt.getCommentList(); return false;}">
							<c:if test="${basicInfo.referenceDiv eq 'prj'}">
								<span>Section : </span>
								<select id="schReferenceDiv" name="schReferenceDiv" style="margin-right: 30px;">
									<option></option>
									<option value="${ct:getConstDef('CD_DTL_COMMENT_PROJECT_HIS')}">${ct:getCodeString(ct:getConstDef('CD_COMMENT_DIVISION'), ct:getConstDef('CD_DTL_COMMENT_PROJECT_HIS'))}</option>
									<option value="${ct:getConstDef('CD_DTL_COMMENT_IDENTIFICAITON_HIS')}">${ct:getCodeString(ct:getConstDef('CD_COMMENT_DIVISION'), ct:getConstDef('CD_DTL_COMMENT_IDENTIFICAITON_HIS'))}</option>
									<option value="${ct:getConstDef('CD_DTL_COMMENT_PACKAGING_HIS')}">${ct:getCodeString(ct:getConstDef('CD_COMMENT_DIVISION'), ct:getConstDef('CD_DTL_COMMENT_PACKAGING_HIS'))}</option>
									<option value="${ct:getConstDef('CD_DTL_COMMENT_DISTRIBUTION_HIS')}">${ct:getCodeString(ct:getConstDef('CD_COMMENT_DIVISION'), ct:getConstDef('CD_DTL_COMMENT_DISTRIBUTION_HIS'))}</option>
								</select>
							</c:if>
							<span>Keyword : </span>
							<input type="text" id="schKeyword" name="schKeyword" />
							
							<input type="hidden" name="referenceDiv" value="${basicInfo.referenceDiv}">
							<input type="hidden" name="referenceId" value="${basicInfo.referenceId}">

							<span class="right"><input id="search" type="button" value="Search" class="btnColor search" /></span>
						</form>
					</fieldset>
				</div>
				<div class="commentList">
					<strong class="tit">Comments</strong>
					<div class="commentBack" id="commentListArea"></div>
					<div id="moreListDiv" class="btnLayout" style="text-align: center; display: none; padding: 0 0; margin-top: 10px;">
						<span>
							<input type="button" value="More" class="btnColor" onclick="fn_commemt.getMoreCommentList()"/>
						</span>
                	</div>
				</div>
			</div>
		</div>
	</body>
</html>

